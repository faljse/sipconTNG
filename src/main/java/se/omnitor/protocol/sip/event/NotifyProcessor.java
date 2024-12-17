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

import java.util.Hashtable;
import java.util.logging.Logger;
import javax.sip.*;
import javax.sip.message.*;
import javax.sip.address.*;
import javax.sip.header.*;
import se.omnitor.protocol.sip.*;

/**
 * @author Andreas Piirimets, Omnitor AB
 *
 * @todo Make all dialogs signal to this class when they are finished, and
 * then remove them from the dialogTable when appropriate. Note that they
 * should stay in the table for a little while to allow re-sending when
 * timers expire.
 */
public class NotifyProcessor extends Processor {

    public static int nbr = 1;
    public int cnbr;

    private Hashtable<Transaction, DialogHandler> dialogTable;
    private int nextCSeq;

    private CallIdHeader callId;

    private Logger logger;

    /**
     * Initializes.
     *
     * @param sc The SipController handling this SIP session
     */
    public NotifyProcessor(SipController sc) {
	super(sc);

	logger = Logger.getLogger("se.omnitor.protocol.sip.event");

	cnbr = nbr;
	nbr++;

	dialogTable = new Hashtable<Transaction, DialogHandler>(0, 1);
	nextCSeq = 1;

        if (sc.stunIsInUse()) {
            callId = createCallId(sc.getStunStack().getExternalIp());
        }
        else {
            callId = createCallId(sc.getLocalIpAddress());
        }
	System.out.println("NotifyProcessor " + cnbr + " created.");
    }

    /**
     * Gets the call id.
     *
     * @return The call id.
     */
    public String getCallId() {
	return callId.getCallId();
    }


    /**
     * Process a request.
     *
     * @param request The request to process
     * @param serverTransaction The server transaction connected to the request
     *
     * @todo Remove IncomingCallDialog from dialogTable when ACK is received.
     */
    public void processRequest(Request request,
			       ServerTransaction serverTransaction) {

	System.out.println("NotifyProcessor " + cnbr + ": " +
			   request.getMethod());

	DialogHandler dh = (DialogHandler)dialogTable.get(serverTransaction);

	if (dh == null) {
	    String method = request.getMethod();

	    if (method.equals("NOTIFY")) {

		/*
		inCall = new IncomingCallDialog(sc, this);
		dh = inCall;
		dialogTable.put(serverTransaction, dh);
		dh.processRequest(request, serverTransaction);
		*/
	    }
	    /*
	    else if (method.equals("ACK")) {
		switch (state) {
		case ANSWERED:
		    state = ESTABLISHED;
		    break;
		default:
		    // Strange, but do nothing.
		}

		if (inCall != null) {
		    inCall.ackReceived();
		}
		inCall = null;
	    }
	    */
	    else {

		dh = new IncomingUnknownDialog(sc, this);
		dialogTable.put(serverTransaction, dh);
		dh.processRequest(request, serverTransaction);
	    }
	}
	else {
	    dh.processRequest(request, serverTransaction);
	}

    }

    /**
     * Process a response.
     *
     */
    public void processResponse(Response response, ClientTransaction ct) {

	DialogHandler dh = null;

	if (ct != null) {
	    dh = (DialogHandler)dialogTable.get(ct);
	}

	if (dh == null) {
	    // Strange, do nothing.
	    logger.fine("Received " + response.getStatusCode() +
			" with an unknown client transaction!");
	}
	else {
	    dh.processResponse(response);
	}


    }

    /**
     * Process a timeout from the SIP kernel
     *
     * @param timeoutEvent The timeout event
     */
    public void processTimeout(TimeoutEvent timeoutEvent) {
	Transaction t;

	if (timeoutEvent.isServerTransaction()) {
	    t = timeoutEvent.getServerTransaction();
	}
	else {
	    t = timeoutEvent.getClientTransaction();
	}

	DialogHandler dh = (DialogHandler)dialogTable.get(t);

	if (dh != null) {
	    dh.processTimeout(timeoutEvent);
	}
    }

    /**
     * Sends a NOTIFY
     *
     * @param data Packet data, formatted in message/sipfrag type.
     */
    public void notify(URI requestUri, FromHeader from, ToHeader to,
		       CallIdHeader callId, ContactHeader contact,
		       AllowEventsHeader allowEvents,
		       EventHeader event,
		       SubscriptionStateHeader subscriptionState,
		       byte[] data) {

	OutgoingNotifyDialog notifyDialog = new OutgoingNotifyDialog(sc, this);

	// To: Remote's URI och tag
	// From: Local URI och tag
	// Call-ID: Samtalets Call-ID
	// CSeq: Samtalets
	// Om Route-set r tom, remote target URI mste vara i Request-URI
	// Om Route-set inte r tom och det frsta URIt i route-set innehller
	//   inte lr-parameter, mste man stta remote target URI i Request-URI och
	//   mste inkludera en routeheader field innehllandes alla route-set-
	//   vrden i ordnin, inkl. alla parametrar. Sedan mste vi stta
	//   remote target URI in i route header field som det sista vrdet.
	// Max-Forwards: 70
	// Via: Som i REGISTER
	//

	ClientTransaction ct =
	    notifyDialog.notify(requestUri, from, to, callId, contact,
				allowEvents, event, subscriptionState, data);

	if (ct != null) {
	    dialogTable.put(ct, notifyDialog);
	}

    }


    /**
     * Gets a new CSeq number, it increases with 1 every time
     *
     * @return A new CSeq number
     */
    protected int getCSeq() {
	nextCSeq++;
	return nextCSeq-1;
    }

}
