/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 ******************************************************************************/
package se.omnitor.tipcon1.sip;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.*;
import javax.sip.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.sip.message.*;
import javax.sip.address.*;

import java.util.logging.Logger;


/**
 * This is a modified version of the NIST SIP default router, modifications
 * are to fit SIPcon1's needs.
 *
 * When the implementation wants to forward
 * a request and  had run out of othe options, then it calls this method
 * to figure out where to send the request. The default router implements
 * a simple "default routing algorithm" which just forwards to the configured
 * proxy address. Use this for UAC/UAS implementations and use the ProxyRouter
 * for proxies.
 *
 *
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.6 $ $Date: 2008/02/26 11:36:37 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @author Andreas Piirimets, Omnitor AB
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class SipRouter implements Router {

  	protected SipStack sipStack;
    protected HopImpl defaultRoute;

    private Logger logger;


    /**
     * Constructor.
     */
    public SipRouter(SipStack sipStack, String defaultRoute) {
	logger = Logger.getLogger("se.omnitor.tipcon1.sip");

	this.sipStack = sipStack;
	if (defaultRoute != null) {
	    this.defaultRoute = new HopImpl(defaultRoute);
	}}

    /**
     * Constructor given SIPStack as an argument (this is only for the
     * protocol tester. Normal implementation should not need this.
     */
     /*public SipRouter(SIPMessageStack sipStack, String defaultRoute) {
	logger = Logger.getLogger("se.omnitor.tipcon1.sip");

	this.sipStack = sipStack;
		if (defaultRoute != null) {
	    	this.defaultRoute = new HopImpl(defaultRoute);
		}
    }*/

    /**
     *  Return true if I have a listener listening here.
     */
    private boolean hopsBackToMe(String host, int port) {
	Iterator it = ((SipStackImpl) sipStack).getListeningPoints();
	while (it.hasNext()) {
	    ListeningPoint lp = (ListeningPoint) it.next();
	    if (((SipStackImpl) sipStack).getIPAddress().equalsIgnoreCase(host)
		&& lp.getPort() == port)
		return true;
	}
	return false;

    }

    /**
     * get next Hop
     *
     */
    public Hop getNextHop(Request request)
            throws SipException {
        return (Hop)getNextHops(request).next();
    }

    /**
     * Return  addresses for default proxy to forward the request to.
     * The list is organized in the following priority.
     * If the requestURI refers directly to a host, the host and port
     * information are extracted from it and made the next hop on the
     * list.
     * If the default route has been specified, then it is used
     * to construct the next element of the list.
     * Bug reported by Will Scullin -- maddr was being ignored when
     * routing requests. Bug reported by Antonis Karydas - the RequestURI can
     * be a non-sip URI.
     *
     * @param request is the sip request to route.
     * @deprecated
     */
    public ListIterator getNextHops(Request request) {

	SIPRequest sipRequest = (SIPRequest) request;

	RequestLine requestLine = sipRequest.getRequestLine();
	if (requestLine == null) {
	    throw new IllegalArgumentException("Bad message");
	}
	javax.sip.address.URI requestURI = requestLine.getUri();
	if (requestURI == null) {
	    throw new IllegalArgumentException("Bad message: Null requestURI");
	}

	RouteList routes = sipRequest.getRouteHeaders();


	/*
	 * Changed by Andreas Piirimets
	 *
	 * The if clause below was moved from bottomt to top!
	 *
	 */

	LinkedList<HopImpl> ll = null;
	
	logger.finer("Default route = " + defaultRoute);
	if (defaultRoute != null) {

	    logger.finest("Adding default route!");
		ll = new LinkedList<HopImpl>();
	    ll.add(defaultRoute);
	    logger.finest("Added Hop = " + defaultRoute.toString());
	}
	if (routes != null) {
	    // This has a  route header so lets use the address
	    // specified in the record route header.
	    // Bug reported by Jiang He

	    // Changed by Andreas Piirimets
	    //	ll = new LinkedList();
	    if (ll == null) {
	    	ll = new LinkedList<HopImpl>();
	    }
	    // EOC

	    Route route = (Route) routes.getFirst();
	    SipUri uri = (SipUri) route.getAddress().getURI();
	    int port;
	    if (uri.getHostPort().hasPort()) {
		logger.finest("routeHeader = " + uri.encode());
		logger.finest("port = " + uri.getHostPort().getPort());
		port = uri.getHostPort().getPort();
	    } else {
		port = 5060;
	    }
	    String host = uri.getHost();
	    String transport = uri.getTransportParam();
	    if (transport == null) {
	    	transport = SIPConstants.UDP;
	    }
	    HopImpl hop = new HopImpl(host, port, transport);
	    ll.add(hop);
	    //  routes.removeFirst();
	    //  sipRequest.setRequestURI(uri);
	    logger.finest("We use the Route header to " +
			      "forward the message");
	} else if (
		   requestURI instanceof SipURI
		   && ((SipURI) requestURI).getMAddrParam() != null) {
	    String maddr = ((SipURI) requestURI).getMAddrParam();
	    String transport = ((SipURI) requestURI).getTransportParam();
	    if (transport == null) {
	    	transport = SIPConstants.UDP;
	    }
	    int port = 5060;
	    HopImpl hop = new HopImpl(maddr, port, transport);
	    hop.setURIRouteFlag();
	    // Changed by Andreas Piirimets
	    //ll = new LinkedList();
	    if (ll == null) {
	    	ll = new LinkedList<HopImpl>();
	    }
	    // EOC
	    ll.add(hop);
	    logger.finest("Added Hop = " + hop.toString());
	} else if (requestURI instanceof SipURI) {
	    String host = ((SipURI) requestURI).getHost();

	    int port = ((SipURI) requestURI).getPort();
	    if (port == -1) {
	    	port = 5060;
	    }
	    if (hopsBackToMe(host, port)) {
				// Egads! route points back at me - better bail.
		return null;
	    }
	    String transport = ((SipURI) requestURI).getTransportParam();
	    if (transport == null) {
	    	transport = SIPConstants.UDP;
	    }
	    HopImpl hop = new HopImpl(host, port, transport);
	    // Changed by Andreas Piirimets
	    //ll = new LinkedList();
	    if (ll == null) {
	    	ll = new LinkedList<HopImpl>();
	    }
	    // EOC
	    ll.add(hop);
	    logger.finest("Added Hop = " + hop.toString());

	}
	
	/*
	 * If clause was originally here!
	 *
	 */

	return ll == null ? null : ll.listIterator();

    }

    /**
     * Get the default hop.
     *
     * @return defaultRoute is the default route.
     * public java.util.Iterator getDefaultRoute(Request request)
     * { return this.getNextHops((SIPRequest)request); }
     */

    public javax.sip.address.Hop getOutboundProxy() {
	return this.defaultRoute;
    }

    /**
     * Get the default route (does the same thing as getOutboundProxy).
     *
     * @return the default route.
     */
    public Hop getDefaultRoute() {
	return this.defaultRoute;
    }
}
