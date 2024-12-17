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

import java.text.ParseException;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.logging.Logger;
import javax.sip.*;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;

import se.omnitor.protocol.sip.*;
import se.omnitor.protocol.sip.event.*;

/**
 * @author Andreas Piirimets, Omnitor AB
 *
 * @todo If a request arrives to this class, respond with 487 Call/Transaction
 * Does Not Exist
 * @todo Make different usernames and passwords based on realms
 * @todo Figure out why 180 always comes before 100 (NIST SIP turns the order
 * of the arrived packets)
 */
public class OutgoingCallDialog extends DialogHandler {

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
    private FromHeader from;
    private CallIdHeader callId;
    private ContactHeader contact;

    private CallProcessor cp;

    private int requestNbr;
    private boolean firstAuthTry;
    private ClientTransaction currentTransaction;
    private boolean isCancelled = false;
    private boolean receivedProvisionalResponse = false;
    private boolean delayedCancel = false;

    private Logger logger;

    private String sdp;


    /**
     * Initializes.
     *
     * @param sc The SIP controller used
     * @param cp The CallProcessor assigned to this call
     */
    public OutgoingCallDialog(SipController sc, CallProcessor cp) {
	super(sc);

	logger = Logger.getLogger("se.omnitor.protocol.sip.call");

	this.cp = cp;

	cnbr = nbr;
	nbr++;

	requestNbr = 1;
	firstAuthTry = true;


    }

    /**
     * Processes a request
     *
     * @param request The request
     * @param transaction The request's transaction
     */
    public void processRequest(Request request,
				  ServerTransaction transaction) {
    }

    /**
     * Processes a response
     *
     * @param response The response
     */
    public synchronized void processResponse(Response response) {

	if (getState() != FINISHED) {
	    Event event =
		new ReferEvent(sc, cp,
			       ((CallIdHeader)response.getHeader("Call-ID")).
			       getCallId(),
			       response.toString().split("\n")[0].
			       split("\r")[0].getBytes(),
			       response.getStatusCode());
	    sc.eventManager.signalEvent(event);
	}

	switch (response.getStatusCode()) {
	case 200:
	    process200Response(response);
	    break;
	case 407:
	    process407Response(response);
	    break;
	default:
	    if (response.getStatusCode() < 200) {
                receivedProvisionalResponse = true;
                if (delayedCancel) {
                    cancel();
                }
                else {
                    sc.signalOutgoingCallProgress(cp, response.getStatusCode());
                }
	    }
	    else {
		sc.signalOutgoingCallError(this, response.getStatusCode());
		setState(FINISHED);
	    }
	    //rp.removeTransaction(currentTransaction);
	}
    }

    /**
     * Processes a timeout event
     *
     * @param timeoutEvent The event
     */
    public void processTimeout(TimeoutEvent timeoutEvent) {
	Timeout timeout = timeoutEvent.getTimeout();

        if (isCancelled) {
            setState(FINISHED);
        }
        else {
            if (timeout.getValue() == Timeout._TRANSACTION) {
                sc.signalOutgoingCallError(this, 408);
                setState(FINISHED);
            } else {
                resendLastRequest();
            }
        }
	//(new RuntimeException()).printStackTrace();
    }

    /**
     * Processes a 200 OK answer
     *
     * @param response The answer
     */
    private void process200Response(Response response) {

	Dialog dialog = currentTransaction.getDialog();
	cp.setDialog(dialog);

	String toTag = ((ToHeader)response.getHeader("To")).getTag();
        cp.saveSessionInfo
                ((FromHeader)originalRequest.getHeader("From"),
                 (ToHeader)originalRequest.getHeader("To"),
                 (CallIdHeader)response.getHeader("Call-ID"),
                 ((CSeqHeader)response.getHeader("CSeq")).
                 getSeqNumber(),
                 response.getHeaders("Record-Route"),
                 (ContactHeader)response.getHeader("Contact"));
        cp.setToTag(toTag);


	try {
	    Request ackRequest = dialog.createRequest(Request.ACK);
	    
	    // Bug workaround:
	    // NIST SIP returns remote's User-Agent field instead of using
	    // our own. We have to find it and change it to our own.
	    UserAgentHeader uaHeader = (UserAgentHeader)ackRequest.getHeader("User-Agent");
	    if (uaHeader != null) {
	    	try {
				Vector<String> uaVector = new Vector<String>(0, 1);
				uaVector.add("SIPcon1");
				uaHeader.setProduct(uaVector);
			} catch (ParseException e) {
				e.printStackTrace();
			}
	    }
	    
	    
	    /*
	    ClientTransaction ct =
		sc.sipProvider.getNewClientTransaction(ackRequest);
	    dialog.sendRequest(ct);
	    */
	    dialog.sendAck(ackRequest);

	    setState(FINISHED);

	    cp.setRemoteSdp(new String(response.getRawContent()));

	    sc.signalEstablishedCall(cp);
	}
	catch (SipException se) {
	    logger.throwing(this.getClass().getName(), "process200Response",
			    se);
	    sc.signalOutgoingCallError(this, 0);
	}

	/*
	rp.signalRegistrationSuccess(expires);
	rp.removeTransaction(currentTransaction);
	*/
    }

    /**
     * Processes a 407 answer
     *
     * @param response The answer
     */
    private void process407Response(Response response) {

	// Don't try this in an endless loop, give up after a few tries.
	if (requestNbr > 5) {
	    sc.signalOutgoingCallError(this, 407);
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
	    createProxyAuthorizationHeader(response, "INVITE",
					   requestUri, requestNbr,
					   username, password, firstAuthTry);

	firstAuthTry = false;
	requestNbr++;

	if (authHeader == null) {
	    sc.signalOutgoingCallError(this, 407);
	    return;
	}


	ClientTransaction ct = send(authHeader);

	cp.removeTransaction(currentTransaction);
	cp.addTransaction(ct, this);
	currentTransaction = ct;
    }

    /**
     * Places a call
     *
     * @param requestUri The request URI
     * @param to The To header
     * @param from The From header
     * @param callId The Call-ID header
     * @param contact The Contact header
     * @param username The username, if authentication is used
     * @param password The password, if authentication is used
     */
    protected ClientTransaction invite(URI requestUri, ToHeader to,
				       FromHeader from,
				       CallIdHeader callId,
				       ContactHeader contact,
				       String sdp) {

	this.requestUri = requestUri;
	this.to = to;
	this.from = from;
	this.callId = callId;
	this.contact = contact;
	this.sdp = sdp;

	ClientTransaction ct = send(null);

	currentTransaction = ct;

	return ct;

    }

    /**
     * Actually sends the INVITE
     *
     * @param authHeader The Authentiction header to use
     */
    private ClientTransaction send(AuthorizationHeader authHeader) {

	ClientTransaction transaction;
	Header[] headers;

	AllowHeader aHeader = getAllowHeader();

	if (authHeader == null && aHeader == null) {
	    headers = new Header[] { contact };
	}
	else if (authHeader == null) {
	    headers = new Header[] { contact, aHeader };
	}
	else if (aHeader == null) {
	    headers = new Header[] { contact, authHeader };
	}
	else {
	    headers = new Header[] { contact, authHeader, aHeader };
	}

	ContentTypeHeader ctHeader = null;

	try {
	    ctHeader =
		sc.headerFactory.createContentTypeHeader("application", "sdp");
	}
	catch (ParseException pe) {
	    logger.throwing(this.getClass().getName(), "send", pe);
	}


   	transaction =
	    sendRequest(requestUri, "INVITE", callId, cp.getCSeq(),
			from, to, headers, ctHeader, sdp.getBytes());

	setState(EXPECTING_OK);

	return transaction;

    }

    /**
     * Returns remote's SIP address. This is a nicely written one, intended
     * to be used in GUI.
     *
     */
    public String getRemoteSipAddress() {
	return to.getAddress().toString();
    }

    /**
     * Cancels an outgoing INVITE
     *
     */
    public void cancel() {

        /* RFC3261:
         *   If no provisional response has been received, the CANCEL request MUST
         *   NOT be sent; rather, the client MUST wait for the arrival of a
         *   provisional response before sending the request.
         */

        isCancelled = true;

        if (receivedProvisionalResponse) {
            cancelOriginalRequest();
        }
        else {
            delayedCancel = true;
        }
    }


    /**
     * Gets the call processor associated with this dialog.
     *
     */
    public CallProcessor getCallProcessor() {
	return cp;
    }

}
