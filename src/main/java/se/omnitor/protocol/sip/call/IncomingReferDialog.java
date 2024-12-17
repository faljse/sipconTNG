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
import se.omnitor.protocol.sip.event.*;
import java.util.ListIterator;
import java.util.logging.Logger;

/**
 * @author Andreas Piirimets, Omnitor AB
 *
 * @todo Change the NOTIFY stuff to use subscription events instead!
 */
public class IncomingReferDialog extends DialogHandler {

    /*
     * This dialog can be in the following states:
     *
     * NOT_STARTED
     * The first incoming BYE has not yet been received.
     *
     * FINISHED
     * BYE has arrived, OK has been sent
     *
     */

    public static int nbr = 1;
    public int cnbr;
    private Logger logger;
    private Address referTo;
    private Request originalRequest;
    private ServerTransaction originalTransaction;

    /**
     * Initializes.
     *
     * @param sc The Sipontroller that handles this SIP session
     * @param cp The CappProcessor that handles this call
     */
    public IncomingReferDialog(SipController sc, CallProcessor cp) {
	super(sc);

	logger = Logger.getLogger("se.omnitor.protocol.sip.call");

	cnbr = nbr;
	nbr++;
	referTo = null;

    }

    /**
     * Process an incoming REFER request.
     *
     * @param request The INVITE or CANCEL request
     * @param serverTransaction The server transaction connected to the request
     *
     * @todo Take care of all INVITE headers and send them to UA.
     */
    public synchronized void processRequest(Request request,
					       ServerTransaction
					       serverTransaction) {

	String method = request.getMethod();

	if (method.equals("REFER")) {

	    switch (getState()) {
	    case NOT_STARTED:
		processReferRequest(request, serverTransaction);

		/*
		sc.signalTerminatedCall(cp);

		// Send OK
		sendResponse(Response.OK, request, serverTransaction);

		setState(FINISHED);
		*/
		break;

	    default:
		// Ignoring re-transmissions of INVITE
	    }

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
	    // Stop sending and do nothing.
	}
	else {
	    // Re-send transaction
	    resendLastResponse();
	}

    }

    /**
     * Processes an incoming REFER
     *
     * @param request The incoming request
     * @param st The server transaction assigned to the request
     */
    private void processReferRequest(Request request, ServerTransaction st) {

	// If the request contains zero or more than one Refer-To header
	// field values, respond with 400.
	ListIterator li = request.getHeaders("Refer-To");

	if (li.hasNext()) {
	    referTo = ((ReferToHeader)li.next()).getAddress();
	}
	else {
	    signalReferToFailure(request, st, 0);
	    return;
	}

	if (li.hasNext()) {
	    signalReferToFailure(request, st, 2);
	    return;
	}

	// Check that the Refer-To header is OK
	if (!referTo.getURI().isSipURI()) {
	    sendResponse(Response.NOT_IMPLEMENTED, request, st);
	    return;
	}

	// Sending Trying
	sendResponse(Response.TRYING, request, st);

	// Save variables
	originalRequest = request;
	originalTransaction = st;

	// Check with user if it's OK to refer.
	sc.askForCallTransferAcceptance(this);

    }

    /**
     * Signals that there is zero or more than one Refer-To header (sends a
     * 400 Bad Request).
     *
     * @param request The request
     * @param st The ServerTransaction assigned to the request
     * @param nbrOfHeaders The number of Refer-To headers found in the request
     */
    private void signalReferToFailure(Request request, ServerTransaction st,
				      int nbrOfHeaders) {

	String reasonPhrase;

	if (nbrOfHeaders == 0) {
	    reasonPhrase = "Refer-To Header Missing";
	}
	else {
	    reasonPhrase = "Too Many Refer-To Headers";
	}

	sendResponse(Response.BAD_REQUEST, request, st, new Header[0],
		     null, null, reasonPhrase);

    }


    /**
     * Accept and perform the call transfer. <br>
     * <br>
     * When accepting a call transfer, a call invitation will be sent to the
     * new destination. This will result in a new media negotiation, why
     * media data has to be delivered to this function from UA.
     *
     * @param contentTypeBase The content type base. For "application/sdp",
     * this would be "application".
     * @param contentTypeExtension The content type extension. For
     * "application/sdp", this would be "sdp".
     * @param data The data, formatted as described in contentTypeBase and
     * contentTypeExtension.
     */
    public void acceptCallTransfer(String contentTypeBase,
				   String contentTypeExtension,
				   String data) {

	// Send 202 to referrer
	String name =
	    ((SipURI)((ToHeader)originalRequest.getHeader("To")).
	     getAddress().getURI()).getUser();
	try {
	    ContactHeader ch = sc.createContactHeader(name);

	    sendResponse(Response.ACCEPTED, originalRequest,
			 originalTransaction, new Header[] { ch }, null,
			 null, null);
	}
	catch (ParseException pe) {
	    logger.throwing(this.getClass().getName(), "acceptCallTransfer",
			    pe);

	    sendResponse(Response.ACCEPTED, originalRequest,
			 originalTransaction);
	}


	// Add remote to event manager
	try {
	    FromHeader origFrom =
		(FromHeader)originalRequest.getHeader("From");
	    ToHeader to =
		sc.headerFactory.createToHeader(origFrom.getAddress(),
						origFrom.getTag());
	    ToHeader origTo = (ToHeader)originalRequest.getHeader("To");
	    FromHeader from =
		sc.headerFactory.createFromHeader(origTo.getAddress(),
						  origTo.getTag());
	    CallIdHeader callId =
		(CallIdHeader)originalRequest.getHeader("Call-ID");

	    URI requestUri =
		((ContactHeader)originalRequest.getHeader("Contact")).
		getAddress().getURI();

	    long idParam =
		((CSeqHeader)originalRequest.getHeader("CSeq")).
		getSeqNumber();


	    EventSubscriber es =
		new EventSubscriber(from, to, callId, requestUri, idParam, sc);

	    sc.eventManager.addSubscription(Event.REFER, es);
	}
	catch (ParseException pe) {
	    logger.throwing(this.getClass().getName(), "acceptCallTransfer",
			    pe);
	}

	// Invite the new destination
	SipURI referToUri = (SipURI)referTo.getURI();
	String referToName =
	    referToUri.getUser() + "@" +
	    referToUri.getHost() + ":" +
	    referToUri.getPort();

	// TODO:
	// Fix SDP below

	sc.invite(referToName, null);

    }


}
