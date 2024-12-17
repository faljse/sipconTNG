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
package se.omnitor.protocol.sip.event;

import java.text.ParseException;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
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
 * @todo Currently, all responses to this notify request goes to a
 * CallProcessor, since SipController only checks the Call-ID. Make the
 * responses go to this class instead!
 */
public class OutgoingNotifyDialog extends DialogHandler {

    /*
     * This dialog can be in the following states:
     *
     * NOT_STARTED
     * NOTIFY has not yet been sent.
     *
     * EXPECTING_OK
     * NOTIFY has been sent, we are waiting for an answer
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
    private AllowEventsHeader allowEvents;
    private EventHeader event;
    private SubscriptionStateHeader subscriptionState;
    private byte[] data;

    private Logger logger;

    private NotifyProcessor np;

    /**
     * Initializes.
     *
     * @param sc The SipController used
     * @param cp The CallProcessor assigned to this call
     */
    public OutgoingNotifyDialog(SipController sc, NotifyProcessor np) {
	super(sc);

	logger = Logger.getLogger("se.omnitor.protocol.sip.event");

	this.np = np;

	cnbr = nbr;
	nbr++;

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

	System.out.println("OutgoingNotifyDialog " + cnbr + ": " + timeout.toString());

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
     * Sends NOTIFY to remote client.
     *
     * @param from The From header
     * @param to The To header
     * @param callId The Call-ID header
     * @param allowEvents The Allow-Events header
     * @param event The Event header
     * @param subscriptionState The Subscription-State header
     */
    protected ClientTransaction notify(URI requestUri,
				       FromHeader from,
				       ToHeader to,
				       CallIdHeader callId,
				       ContactHeader contact,
				       AllowEventsHeader allowEvents,
				       EventHeader event,
				       SubscriptionStateHeader
				       subscriptionState,
				       byte[] data) {

	this.requestUri = requestUri;
	this.from = from;
	this.to = to;
	this.callId = callId;
	this.contact = contact;
	this.allowEvents = allowEvents;
	this.event = event;
	this.subscriptionState = subscriptionState;
	this.data = data;

	ClientTransaction ct = send(null);

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

	int hdrCnt = 0;
	if (authHeader != null) {
	    hdrCnt++;
	}
	if (allowEvents != null) {
	    hdrCnt++;
	}
	if (event != null) {
	    hdrCnt++;
	}
	if (subscriptionState != null) {
	    hdrCnt++;
	}
	if (contact != null) {
	    hdrCnt++;
	}

	Header[] headers = new Header[hdrCnt];

	if (authHeader != null) {
	    hdrCnt--;
	    headers[hdrCnt] = authHeader;
	}
	if (allowEvents != null) {
	    hdrCnt--;
	    headers[hdrCnt] = allowEvents;
	}
	if (event != null) {
	    hdrCnt--;
	    headers[hdrCnt] = event;
	}
	if (subscriptionState != null) {
	    hdrCnt--;
	    headers[hdrCnt] = subscriptionState;
	}
	if (contact != null) {
	    hdrCnt--;
	    headers[hdrCnt] = contact;
	}

	ContentTypeHeader contentType = null;

	try {
	    contentType =
		sc.headerFactory.createContentTypeHeader("message",
							 "sipfrag");
	}
	catch (ParseException pe) {
	    logger.throwing(this.getClass().getName(), "send", pe);
	}

	transaction =
	    sendRequest(requestUri, "NOTIFY", callId, np.getCSeq(),
			from, to, headers, contentType, data);

	setState(EXPECTING_OK);

	return transaction;

    }


}
