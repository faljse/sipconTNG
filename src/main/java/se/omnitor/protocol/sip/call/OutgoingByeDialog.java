/*
 * Open Source Exemplar Software
 *
 * Copyright (C) 2004-2008 University of Wisconsin (Trace R&D Center)
 * Copyright (C) 2004-2008 Omnitor AB
 *
 * This reference design was developed under funding from the National
 * Institute on Disability and Rehabilitation Research US Dept of Education
 * and the European Commission.
 *
 * This piece of software is a part of a package that was developed as a joint
 * effort of Omnitor AB and the Trace Center - University of Wisconsin and is
 * released to the public domain with only the following restrictions:
 *
 * 1) That the following acknowledgement be included in the source code and
 * documentation for the program or package that use this code
 *
 * "Parts of this program were based on reference designs developed by
 * Omnitor AB and the Trace Center, University of Wisconsin-Madison under
 * funding from the National Institute on Disability and Rehabilitation
 * Research US Dept of Education and the European Commission."
 *
 * 2) That this program not be modified unless it is plainly marked as
 * modified from the original distributed by Trace/Omnitor.
 *
 * (NOTE: This release applies only to the files that contain this notice -
 * not necesarily to any other code or libraries associated with this file.
 * Please check individual files and libraries for the rights to use each)
 *
 * THIS PIECE OF THE SOFTWARE PACKAGE IS EXPERIMENTAL/DEMONSTRATION IN NATURE.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF THIRD PARTY RIGHTS.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR HOLDERS INCLUDED IN THIS NOTICE
 * BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL INDIRECT OR CONSEQUENTIAL DAMAGES,
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 *
 */
package se.omnitor.protocol.sip.call;

import javax.sip.*;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;
import se.omnitor.protocol.sip.*;

/**
 * @author Andreas Piirimets, Omnitor AB
 *
 * @todo Send Call/Transaction does not exist on any incoming request
 * @todo Make more testing to assure that the route header stuff works
 * correctly.
 * @todo Use Dialog.sendRequest(..) here instead!
 */
public class OutgoingByeDialog extends DialogHandler {

    /*
     * This dialog can be in the following states:
     *
     * NOT_STARTED
     * REGISTER has not yet been sent.
     *
     * EXPECTING_OK
     * REGISTER has been sent, we are waiting for an answer
     *
     * FINISHED
     * Registration process has finished
     *
     */

    public static int nbr = 1;
    public int cnbr;

    private URI requestUri;
    private ToHeader to;
    /*
    private FromHeader from;
    private CallIdHeader callId;
    private String username;
    private String password;
    */

    private CallProcessor cp;

    //private boolean authIsSent;

    private Dialog dialog;

    private int requestNbr;
    private boolean firstAuthTry;
    private ClientTransaction currentTransaction;

    /**
     * Initializes.
     *
     * @param sc The SipController used
     * @param cp The CallProcessor assigned to this call
     */
    public OutgoingByeDialog(SipController sc, CallProcessor cp, Dialog d) {
	super(sc);

	this.cp = cp;
        this.dialog = d;

	//authIsSent = false;

	cnbr = nbr;
	nbr++;

	requestNbr = 1;
	firstAuthTry = true;


    }

    /**
     * Processes an incoming request.
     *
     * @param request The incoming request
     * @param transaction The incoming transaction
     */
    public void processRequest(Request request,
				  ServerTransaction transaction) {
    }

    /**
     * Processes an incoming response
     *
     * @param response The response
     */
    public void processResponse(Response response) {

	switch (response.getStatusCode()) {
	case 200:
	    process200Response(response);
	    break;
	    /*
	case 401:
	    process401Response(response);
	    break;
	    */
	case 407:
	    process407Response(response);
	    break;
	default:
	    // Ignore anything else
	}
    }

    /**
     * Processes a timeout event
     *
     * @param timeoutEvent The timeout event
     */
    public void processTimeout(TimeoutEvent timeoutEvent) {
	Timeout timeout = timeoutEvent.getTimeout();

	System.out.println("RegisterDialog " + cnbr + ": " + timeout.toString());

	//resendLastResponse();

	//(new RuntimeException()).printStackTrace();
    }

    /**
     * Processes a 200 OK response
     *
     * @param response The 200 OK response
     */
    private void process200Response(Response response) {
	setState(FINISHED);
    }

    /**
     * Processes a 407 answer
     *
     * @param response The answer
     */
    private void process407Response(Response response) {

	// Don't try this in an endless loop, give up after a few tries.
	if (requestNbr > 5) {
	    // Really nothing to do here ..
	    //sc.signalOutgoingCallError(this, 407);
	    return;
	}

	String username;
	String password;

	try {
	    String realm = ((ProxyAuthenticateHeader)response.
		     getHeader("Proxy-Authenticate")).getRealm();
	    AuthInfo ai = sc.getAuthInfo(realm, ((SipURI)requestUri).getHost());
	    if (ai != null) {
		username = ai.getUsername();
		password = ai.getPassword();
	    }
	    else {
		username = "anonymous";
		password = "";
	    }
	}
	catch (NullPointerException npe) {
	    // No Proxy-Authenticate header exists.
	    username = "anonymous";
	    password = "";
	}

	AuthorizationHeader authHeader =
	    createProxyAuthorizationHeader(response, "BYE",
					   requestUri, requestNbr,
					   username, password, firstAuthTry);

	firstAuthTry = false;
	requestNbr++;

	if (authHeader == null) {
	    // Really nothing to do ..
	    //sc.signalOutgoingCallError(this, 407);
	    return;
	}


	ClientTransaction ct = send(authHeader);

	cp.removeTransaction(currentTransaction);
	cp.addTransaction(ct, this);
	currentTransaction = ct;
    }


    /**
     * Processes a 401 response
     *
     * @param response The 401 response
     */
    /*
    private void process401Response(Response response) {
*/
	// Don't try this in an endless loop, give up after a few tries.

	// Implement this later
	/*
	if (requestNbr > 5) {
	    sc.signalRegistrationError(401);
	    return;
	}

	AuthorizationHeader authHeader =
	    createAuthorizationHeader(response, "REGISTER",
				      requestUri, requestNbr,
				      username, password, firstAuthTry);

	firstAuthTry = false;
	requestNbr++;

	if (authHeader == null) {
	    sc.signalRegistrationError(401);
	    return;
	}


	ClientTransaction ct = send(authHeader);

	rp.removeTransaction(currentTransaction);
	rp.addTransaction(ct, this);
	currentTransaction = ct;
	*/
    /*
    }
    */

    /**
     * Sends BYE to remote client.
     *
     * @param requestUri The request URI
     * @param to The To header
     * @param from The From header
     * @param callId The Call-ID header
     * @param username The username, if authentication is used
     * @param password The password, if authentication is used
     * @param routes The Route headers to include. If this is null, no Route
     * headers will be sent.
     */
    protected ClientTransaction bye(URI requestUri, ToHeader to,
				    FromHeader from,
				    CallIdHeader callId,
				    RouteHeader[] routes,
				    String username, String password) {

	this.requestUri = requestUri;
	this.to = to;
	/*
	this.from = from;
	this.callId = callId;
	this.username = username;
	this.password = password;
	this.routes = routes;
	*/

	sc.signalTerminatedCall(cp);

	ClientTransaction ct = send(null);

	currentTransaction = ct;

	return ct;

    }

    /**
     * This function does the actually sending part.
     *
     * @param authHeader The authorization header to add. If this is null,
     * no authorization header will be send.
     */
    private ClientTransaction send(AuthorizationHeader authHeader) {

	ClientTransaction transaction;
/*
	Header[] headers;

	if (authHeader == null && routes == null) {
	    headers = new Header[0];
	}
	else if (authHeader == null) {
	    headers = routes;
	}
	else if (routes == null) {
	    headers = new Header[] { authHeader };
	}
	else {
	    headers = new Header[routes.length + 1];
	    System.arraycopy(routes, 0, headers, 0, routes.length);
	    headers[routes.length] = authHeader;
	}
*/

	transaction = sendByeRequest(dialog, requestUri, to);
                   /*
	    sendRequest(requestUri, "BYE", callId, cp.getCSeq(),
			from, to, headers, null, null);
                   */

	setState(EXPECTING_OK);

	return transaction;

    }


}
