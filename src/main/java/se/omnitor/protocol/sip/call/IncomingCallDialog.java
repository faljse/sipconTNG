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
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.message.*;
import javax.sip.header.*;
import se.omnitor.protocol.sip.*;
import java.util.logging.Logger;

/**
 * @author Andreas Piirimets, Omnitor AB
 *
 * @todo Add support for re-INVITE
 * @todo Add support for Expries header in INVITE (that is, session expires
 * functionality)
 * @todo Make it send 487 Request Terminated when a CANCEL arrives.
 */
public class IncomingCallDialog extends DialogHandler {

    /*
     * This dialog can be in the following states:
     *
     * NOT_STARTED
     * The first incoming INVITE has not yet been received.
     *
     * WAITING_FOR_USER
     * INVITE has been received, user has been notified. Waiting for user
     * action.
     *
     * EXPECTING_ACK
     * Any response (3xx-6xx) has been sent to remote and we are awaiting an
     * ACK to arrive. The response sent can be found in "lastResponse"
     * variable.
     *
     * FINISHED
     * ACK has arrived and everything is finished.
     *
     */

    public static int nbr = 1;
    public int cnbr;
    private Logger logger;
    private CallProcessor cp;

    private String remoteUserAddress;
    private String remoteNiceName;

    private String incomingSdp;

    private ToHeader to;
    
    private boolean immediateBye = false;


    /**
     * Initializes.
     *
     * @param sc The Sipontroller that handles this SIP session
     * @param cp The CappProcessor that handles this call
     */
    public IncomingCallDialog(SipController sc, CallProcessor cp) {
	super(sc);

	this.cp = cp;

	logger = Logger.getLogger("se.omnitor.protocol.sip.call");


	cnbr = nbr;
	nbr++;

    }

    /**
     * Process an incoming INVITE or CANCEL request.
     *
     * @param request The INVITE or CANCEL request
     * @param serverTransaction The server transaction connected to the request
     *
     * @todo Take care of all INVITE headers and send them to UA.
     */
    public synchronized void processRequest(Request request,
					    ServerTransaction
					    serverTransaction) {

	remoteUserAddress =
	    ((FromHeader)request.getHeader("From")).getAddress().toString();

        // Get a nice name which can be presented in GUI
        Address address = ((FromHeader)request.getHeader("From")).getAddress();
        remoteNiceName = address.getDisplayName();
        if (remoteNiceName == null || remoteNiceName.trim().equals("")) {
            remoteNiceName = address.toString();
            int ix1 = remoteNiceName.indexOf("<");
            int ix2 = remoteNiceName.indexOf(">");
            if (ix1 < ix2 && (ix2-ix1 > 1)) {
                remoteNiceName = remoteNiceName.substring(ix1+1, ix2-ix1);
            }

            if (remoteNiceName.startsWith("sip:")) {
                remoteNiceName = remoteNiceName.substring(4);
            }

            remoteNiceName = remoteNiceName.split("@")[0];
            remoteNiceName = remoteNiceName.split("\\.")[0];
        }

	String method = request.getMethod();

	if (method.equals("INVITE")) {

	    // Send 100 Trying
	    sendResponse(Response.TRYING, request, serverTransaction);

	    switch (getState()) {
	    case NOT_STARTED:
		originalTransaction = serverTransaction;
		originalRequest = request;
		incomingSdp = new String(request.getRawContent());
		to = (ToHeader)request.getHeader("To");

		setState(WAITING_FOR_USER);
		
		if (immediateBye) {
			sendResponse(Response.BUSY_HERE, request, serverTransaction);
		}
		else {
			final IncomingCallDialog icd = this;
			Thread t = new Thread() {
				public void run() {
					sc.signalIncomingCall(icd);
				}
		    	};
		   	t.setName("Incoming Call Dialog signal");
		   	t.start();
		}
		break;

	    default:
		// Ignoring re-transmissions of INVITE
	    }

	}

	else if (method.equals("CANCEL")) {

	    switch (getState()) {
	    case NOT_STARTED:
		sendResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST,
			     request, serverTransaction);
		break;

	    case WAITING_FOR_USER:
		sendResponse(Response.OK, request, serverTransaction);
		sendResponse(Response.REQUEST_TERMINATED, originalRequest,
			     (ServerTransaction)originalTransaction);
		sc.signalCancelledIncomingCall(this);
		setState(EXPECTING_ACK);
		break;

	    case EXPECTING_ACK:
		// CANCEL has no effect here, as we already have answered the
		// INVITE.
		ackReceived();
		break;

	    case FINISHED:
		sendResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST,
			     request, serverTransaction);
		break;

	    default:
	    }
	}

	else if (method.equals("ACK")) {
	    ackReceived();
	}

    }

    /**
     * Process a response.
     *
     * @param response The response to process
     */
    public void processResponse(Response response) {
    }

    /**
     * Process a timeout event from the SIP kernel.
     *
     * @param timeoutEvent The timeout event
     */
    public void processTimeout(TimeoutEvent timeoutEvent) {
	Timeout timeout = timeoutEvent.getTimeout();

	System.out.println("IncomingCallDialog " + cnbr + ": " + timeout.toString());

	if (timeout.getValue() == Timeout._TRANSACTION) {
	    // Close the transaction with BYE.
	    cp.bye();
	    setState(FINISHED);
	}
	else {
	    // Re-send transaction
	    resendLastResponse();
	}

    }

    /**
     * Sending Ringing to remote. This should be sent as soon as a ring signal
     * is produced, to let the other party know about the call's progress.
     *
     * @todo Add support for all allowed headers to 180
     * @todo Check whether 180 should be resent with short intervals
     */
    public void ringing() {
	sendResponse(Response.RINGING, originalRequest,
		     (ServerTransaction)originalTransaction);
    }

    /**
     * Answers a call with 200 OK. This packet MUST contain an answer if
     * INVITE contained an offer. If INVITE did not contain an offer, this
     * packet MUST contain an offer.
     *
     * @param contentTypeBase The MIME name base for the Content-Type of the
     * answer. For "application/sdp", this would be "application"
     * @param contentTypeExt The MIME name extension for the Content-Type of
     * the answer. For "application/sdp", this would be "sdp".
     * @param answer An answer to the offer (normally in SDP).
     *
     * @todo 200 SHOULD contain the Allow header field
     * @todo 200 SHOULD contain the Supported header field
     * @todo 200 MUST contain an answer if INVITE contained an offer. If INVITE
     * did not contain an ofer, 200 MUST contain an offer
     */
    public void answer(String contentTypeBase, String contentTypeExt,
		       String answer) {

	// Create Content-Type header
	ContentTypeHeader ctHeader = null;
	try {
	    ctHeader =
		sc.headerFactory.createContentTypeHeader(contentTypeBase,
							 contentTypeExt);
	}
	catch (ParseException pe) {
	    logger.throwing(this.getClass().getName(), "answer", pe);
	}

	// Create allow header
	AllowHeader aHeader = getAllowHeader();

	// Create Contact header
	ToHeader to = (ToHeader)originalRequest.getHeader("To");
	String username = ((SipURI)to.getAddress().getURI()).getUser();
	ContactHeader cHeader = null;
	try {
	    cHeader = sc.createContactHeader(username);
	}
	catch (ParseException pe) {
	    logger.throwing(this.getClass().getName(), "answer", pe);
	    // Just ignore exception
	}

	// Merge headers to an array
	Header[] headers;
	if (aHeader != null && cHeader != null) {
	    headers = new Header[] { aHeader, cHeader };
	}
	else if (aHeader != null) {
	    headers = new Header[] { aHeader };
	}
	else if (cHeader != null) {
	    headers = new Header[] { cHeader };
	}
	else {
	    headers = new Header[0];
	}

	String localTag =
	    sendResponse(Response.OK, originalRequest,
			 (ServerTransaction)originalTransaction, headers,
			 ctHeader, answer.getBytes(), null);


	// Saved data requires swapping the from and to (since in
	// outgoing requests in this dialog such as BYE, they must be
	// swapped)
	FromHeader fromHdr = (FromHeader)originalRequest.getHeader("From");
	ToHeader toHdr = (ToHeader)originalRequest.getHeader("To");

	FromHeader localFrom = null;
	try {
	    localFrom =
		sc.headerFactory.createFromHeader(toHdr.getAddress(),
						  localTag);
	}
	catch (ParseException pe) {
	    logger.throwing(this.getClass().getName(),
			    "processRequest", pe);
	}

	ToHeader localTo = null;
	try {
	    localTo =
		sc.headerFactory.createToHeader(fromHdr.getAddress(),
						fromHdr.getTag());
	}
	catch (ParseException pe) {
	    logger.throwing(this.getClass().getName(),
			    "processRequest", pe);
	}

	cp.saveSessionInfo
	    (localFrom,
	     localTo,
	     (CallIdHeader)originalRequest.getHeader("Call-ID"),
	     ((CSeqHeader)originalRequest.getHeader("CSeq")).
	     getSeqNumber(),
	     originalRequest.getHeaders("Record-Route"),
	     (ContactHeader)originalRequest.getHeader("Contact"));



	setState(EXPECTING_ACK);
    }

    /**
     * Declines a call.
     *
     */
    public void decline() {
	sendResponse(Response.DECLINE, originalRequest,
		     (ServerTransaction)originalTransaction);
	setState(EXPECTING_ACK);
    }

    /**
     * Declines a call
     *
     * @param retryAfter Indicates when the called party anticipates being
     * available again. The value of this field is a positive integer number
     * of seconds (in decimal) after the time of the response.
     * @param comment Comment to leave to remote. If empty or null, no comment
     * will be sent. NOTE! Some clients have problems parsing a retry after
     * header that contains a comment.
     * @param errorInfoUri A URI that points to additional information about
     * the decline response. If null, empty or malformed, no error info will be
     * sent.
     *
     * @todo Add support for all allowed headers for 603
     */
    public void decline(int retryAfter, String comment, String errorInfoUri) {
	RetryAfterHeader raHeader = null;
	ErrorInfoHeader eiHeader = null;

	try {
	    raHeader = sc.headerFactory.createRetryAfterHeader(retryAfter);
	    if (comment != null && !comment.equals("")) {
		raHeader.setComment(comment);
	    }

	}
	catch (InvalidArgumentException iae) {
	    iae.printStackTrace();
	}
	catch (ParseException pe) {
	    pe.printStackTrace();
	}

	if (errorInfoUri != null && !errorInfoUri.equals("")) {
	    try {
		URI eiUri = sc.addressFactory.createURI(errorInfoUri);
		eiHeader = sc.headerFactory.createErrorInfoHeader(eiUri);
	    }
	    catch (ParseException pe) {
		// Just ignore and don't send any error info.
	    }
	}


	Header headers[] = new Header[0];

	if (eiHeader != null && raHeader != null) {
	    headers = new Header[] { eiHeader, raHeader };
	}
	else if (eiHeader != null) {
	    headers = new Header[] { eiHeader };
	}
	else if (raHeader != null) {
	    headers = new Header[] { raHeader };
	}


	sendResponse(Response.DECLINE, originalRequest,
		     (ServerTransaction)originalTransaction, headers, null,
		     null, null);

    }

    /**
     * Signals that we are busy here.
     *
     */
    public void busy() {
	sendResponse(Response.BUSY_HERE, originalRequest,
		     (ServerTransaction)originalTransaction);

	setState(EXPECTING_ACK);
    }

    /**
     * Signals that we are busy here.
     *
     * @param retryAfter Indicates when the called party anticipates being
     * available again. The value of this field is a positive integer number
     * of seconds (in decimal) after the time of the response.
     * @param comment Comment to leave to remote. If empty or null, no comment
     * will be sent. NOTE! Some clients have problems parsing a retry after
     * header that contains a comment.
     * @param errorInfoUri A URI that points to additional information about
     * the decline response. If null, empty or malformed, no error info will be
     * sent.
     *
     * @todo Add support for all allowed headers for 486
     */
    public void busy(int retryAfter, String comment, String errorInfoUri) {
	RetryAfterHeader raHeader = null;
	ErrorInfoHeader eiHeader = null;

	try {
	    raHeader = sc.headerFactory.createRetryAfterHeader(retryAfter);
	    if (comment != null && !comment.equals("")) {
		raHeader.setComment(comment);
	    }

	}
	catch (InvalidArgumentException iae) {
	    iae.printStackTrace();
	}
	catch (ParseException pe) {
	    pe.printStackTrace();
	}

	if (errorInfoUri != null && !errorInfoUri.equals("")) {
	    try {
		URI eiUri = sc.addressFactory.createURI(errorInfoUri);
		eiHeader = sc.headerFactory.createErrorInfoHeader(eiUri);
	    }
	    catch (ParseException pe) {
		// Just ignore and don't send any error info.
	    }
	}

	Header headers[] = new Header[0];

	if (eiHeader != null && raHeader != null) {
	    headers = new Header[] { eiHeader, raHeader };
	}
	else if (eiHeader != null) {
	    headers = new Header[] { eiHeader };
	}
	else if (raHeader != null) {
	    headers = new Header[] { raHeader };
	}


	sendResponse(Response.BUSY_HERE, originalRequest,
		     (ServerTransaction)originalTransaction, headers,
		     null, null, null);

    }

    /**
     * Returns a "Bad Request" due to some kind of malformed syntax in SIP or
     * SDP. The reason phrase SHOULD identify the syntax problem in more
     * detail, for example "Missing Call-ID header field".
     *
     * @param reasonPhrase A detailed description of the syntax problem
     *
     * @todo Use the reasonPhrase!
     */
    public void badRequest(String reasonPhrase) {
	sendResponse(Response.BAD_REQUEST, originalRequest,
		     (ServerTransaction)originalTransaction);

	setState(EXPECTING_ACK);

    }

    /**
     * Returns a "Not Acceptable Here", which means that there are no media
     * types that match remote's request.
     *
     * @param mediaCapabilities Description of local media capabilities, which
     * is formatted according to the Accept header field in the INVITE. This
     * is the same as a message body in a response to an OPTIONS request.
     *
     * @todo Use the mediaCapabilities!
     * @todo Add support for all allowed headers for 488
     * @todo 488 SHOULD include a Warning header field value explaining why
     * the offer was rejected.
     */
    public void notAcceptableHere(String mediaCapabilities) {
	sendResponse(Response.NOT_ACCEPTABLE_HERE, originalRequest,
		     (ServerTransaction)originalTransaction);

	setState(EXPECTING_ACK);
    }

    /**
     * Signals that an ACK has been received.
     *
     */
    protected void ackReceived() {

	switch (getState()) {
	case EXPECTING_ACK:
	    setState(FINISHED);
	    sc.signalEstablishedCall(cp);
	    break;

	default:
	    // Ignore it.
	}

    }

    public String getRemoteUserAddress() {
	return remoteUserAddress;
    }

    public String getRemoteNiceName() {
        return remoteNiceName;
    }

    public CallProcessor getCallProcessor() {
	return cp;
    }

    public String getSdp() {
	return incomingSdp;
    }

    public ToHeader getToHeader() {
	return to;
    }	
    
    public void setImmediateBye(boolean immediateBye) {
    	this.immediateBye = immediateBye;
    }
}

