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
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.logging.Logger;
import javax.sip.*;
import javax.sip.message.*;
import javax.sip.address.*;
import javax.sip.header.*;
import se.omnitor.protocol.sip.*;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Andreas Piirimets, Omnitor AB
 *
 * @todo Make all dialogs signal to this class when they are finished, and
 * then remove them from the dialogTable when appropriate. Note that they
 * should stay in the table for a little while to allow re-sending when
 * timers expire.
 */
public class CallProcessor extends Processor {

    /**
     * We have not yet started any processing.
     */
    public static final int NOT_STARTED = 1;

    /**
     * An incoming INVITE has arrived, waiting for user action
     */
    public static final int INCOMING_CALL = 2;

    /**
     * Incoming call has been answered, waiting for ACK from remote.
     */
    public static final int ANSWERED = 3;

    /**
     * An outgoing INVITE has been or will be sent, waiting for response
     */
    public static final int OUTGOING_CALL = 3;

    /**
     * A call is established
     */
    public static final int ESTABLISHED = 4;

    /**
     * An outgoing BYE has been sent, waiting for response
     */
    public static final int TERMINATING = 5;

    /**
     * The call has been terminated by one of the parties.
     */
    public static final int TERMINATED = 6;


    public static int nbr = 1;
    public int cnbr;

    private Hashtable<Transaction, DialogHandler> dialogTable;
    private IncomingCallDialog inCall;
    private int state;
    private long nextCSeq;

    private URI requestUri;
    private ToHeader to;
    private FromHeader from;
    private CallIdHeader callId;
    private RouteHeader[] routes;
    private Dialog dialog;

    private String remoteSdp;

    private Logger logger;
    
    private boolean immediateBye = false;

    /**
     * Initializes.
     *
     * @param sc The SipController handling this SIP session
     */
    public CallProcessor(SipController sc) {
	super(sc);

	logger = Logger.getLogger("se.omnitor.protocol.sip.call");

	cnbr = nbr;
	nbr++;

	dialogTable = new Hashtable<Transaction, DialogHandler>(0, 1);
	inCall = null;
	state = NOT_STARTED;
	nextCSeq = 1;

        if (sc.stunIsInUse()) {
            callId = createCallId(sc.getStunStack().getExternalIp());
        }
        else {
            callId = createCallId(sc.getLocalIpAddress());
        }
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

	DialogHandler dh = (DialogHandler)dialogTable.get(serverTransaction);

	if (dh == null) {
	    String method = request.getMethod();
	    if (method.equals("INVITE")) {
		switch (state) {
		case NOT_STARTED:
		    state = INCOMING_CALL;
		    break;
		case INCOMING_CALL:
		    // Do nothing.
		    break;
		default:
		    // Strange .. but do nothing.
		    break;
		}

		inCall = new IncomingCallDialog(sc, this);
		if (immediateBye) {
			inCall.setImmediateBye(true);
		}
		dh = inCall;
                dialog = serverTransaction.getDialog();
		dialogTable.put(serverTransaction, dh);
		dh.processRequest(request, serverTransaction);
	    }

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
		else {
		    logger.warning("ACK received, no incoming call found!");
		}
		inCall = null;
	    }

	    else if (method.equals("BYE")) {
		switch (state) {
		case ESTABLISHED:
		    state = TERMINATED;
		    break;
		default:
		    // Strange, but do nothing
		}

		dh = new IncomingByeDialog(sc, this);
		dialogTable.put(serverTransaction, dh);
		dh.processRequest(request, serverTransaction);
	    }

	    else if (method.equals("REFER")) {
		dh = new IncomingReferDialog(sc, this);
		dialogTable.put(serverTransaction, dh);
		dh.processRequest(request, serverTransaction);
	    }

            // Added by Luan Avdulla 2007-05-02.
            else if (method.equals("CANCEL")) {
                switch (state) {
                case INCOMING_CALL:
                    Set set = dialogTable.keySet();

                    // Find the SeverTransaction with the same Call-ID as the current
                    // request.
                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()) {
                        ServerTransaction key = (ServerTransaction) iterator.next();
                        DialogHandler dialog = (DialogHandler) dialogTable.get(key);
                        if (dialog.getRequest().getHeader("Call-ID").equals(
                                serverTransaction.getRequest().getHeader("Call-ID")))
                            dialog.processRequest(request, serverTransaction);
                    }
                    break;
                default:
                    // Strange, but do nothing
                }
            }
		
		//EZ: 2007-11-26
		else if(method.equals("OPTIONS")) {
     			dh = new IncomingOptionsDialog(sc, this);
			dialogTable.put(serverTransaction, dh);
			dh.processRequest(request, serverTransaction);
		}

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
     * Terminate a session by sending a BYE request.
     *
     */
    public void bye() {

        // Wait max 3 seconds for call to be established before sending BYE
        for (int cnt=0; cnt<3 && (state != ESTABLISHED); cnt++) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ie) {
                // Do nothing here.
            }
        }

	state = TERMINATING;

	OutgoingByeDialog byeDialog = new OutgoingByeDialog(sc, this, dialog);

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

	ClientTransaction ct = byeDialog.bye(requestUri, to, from, callId,
					     routes, "", "");

	if (ct != null) {
	    dialogTable.put(ct, byeDialog);
	}

    }


    /**
     * Send an ACK request
     *
     */
    /*
    public void ack() {
	state = TERMINATING;

	OutgoingAckDialog ackDialog = new OutgoingAckDialog(sc, this);

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

	ClientTransaction ct = ackDialog.ack(requestUri, to, from, callId,
					     routes);

	dialogTable.put(ct, ackDialog);

    }
    */

    /**
     * Saves call information from a XxCallDialog, these parameters are
     * then used when creating new requests in the same dialog.
     *
     * @param from Local URI and tag
     * @param to Remote URI and tag
     * @param callId The call id
     * @param cseq The last cseq used when establishing the call
     * @param recordRoutes All record route headers
     * @param contact The Contact header of remote's message
     */
    protected void saveSessionInfo(FromHeader from, ToHeader to,
				   CallIdHeader callId, long cseq,
				   ListIterator recordRoutes,
				   ContactHeader contact) {


	this.from = from;
	this.to = to;
	this.callId = callId;
	nextCSeq = cseq+1;

	// Count the number of record routes
	int rrCnt = 0;
	if (recordRoutes != null) {
	    while (recordRoutes.hasNext()) {
		recordRoutes.next();
		rrCnt++;
	    }
	    while (recordRoutes.hasPrevious()) {
		recordRoutes.previous();
	    }
	}

	if (rrCnt == 0) {
	    requestUri = contact.getAddress().getURI();
	    routes = null;
	}
	else {
	    RecordRouteHeader tmpRr = (RecordRouteHeader)recordRoutes.next();
	    URI uri = tmpRr.getAddress().getURI();

	    if (uri.isSipURI() && ((SipURI)uri).hasLrParam()) {
		requestUri = contact.getAddress().getURI();

		routes = new RouteHeader[rrCnt];
		recordRoutes.previous();

		for (int cnt=0; cnt<rrCnt; cnt++) {
		    routes[cnt] =
			sc.headerFactory.
			createRouteHeader(((RecordRouteHeader)recordRoutes.
					   next()).getAddress());
		}

	    }
	    else {
		requestUri = uri;

		routes = new RouteHeader[rrCnt - 1];

		for (int cnt=0; cnt<rrCnt-1; cnt++) {
		    routes[cnt] =
			sc.headerFactory.
			createRouteHeader(((RecordRouteHeader)recordRoutes.
					   next()).getAddress());
		}
	    }
	}
	
    }

    /**
     * Sets the To header's tag. If the current To header is null, nothing is
     * done.
     *
     * @param tag The tag
     */
    public void setToTag(String tag) {
	if (to != null) {
	    try {
		to.setTag(tag);
	    }
	    catch (ParseException pe) {
		logger.throwing(this.getClass().getName(), "setToTag", pe);
	    }
	}
    }

    /**
     * Gets a new CSeq number, it increases with 1 every time
     *
     * @return A new CSeq number
     */
    protected long getCSeq() {
	nextCSeq++;
	return nextCSeq-1;
    }

    /**
     * Calls a SIP address
     *
     * @param requestUri Request-URI
     * @param to To header
     * @param from From header
     * @param contact Contact header
     */
    public OutgoingCallDialog invite(URI requestUri, ToHeader to,
				     FromHeader from,
				     ContactHeader contact, String sdp) {

	this.requestUri = requestUri;
	this.to = to;
	this.from = from;

	state = OUTGOING_CALL;
	
	OutgoingCallDialog outDialog = new OutgoingCallDialog(sc, this);

        ClientTransaction ct =
	    outDialog.invite(requestUri, to, from, callId, contact, sdp);

        // Check if SIP domain was invalid or similar problem
        if (ct == null) {
            outDialog = null;
        }
        else {
            dialogTable.put(ct, outDialog);
            dialog = ct.getDialog();
        }
	return outDialog;
    }

    /**
     * Returns remote's SIP address. This is a nicely written one, intended
     * to be used in GUI.
     *
     */
    public String getRemoteSipAddress() {
	return to.getAddress().toString();
    }


    public String getNiceRemoteName() {
        String display;
        try {
            display = to.getAddress().getDisplayName();
        }
        catch (NullPointerException npe) {
            // Displayname or similar was not available, let's return "User"
            return "User";
        }
	if (display == null || display.equals("")) {
	    return to.getAddress().toString();
	}

	return display;
    }

    public void setRemoteSdp(String sdp) {
	this.remoteSdp = sdp;
    }

    public String getRemoteSdp() {
        System.out.println("Remote SDP:\n" + remoteSdp);
	return remoteSdp;
    }

    public ToHeader getToHeader() {
	return to;
    }

    /**
     * Adds a transaction to the dialog table.
     *
     * @param ct The transaction to connect to the dialog in the table
     * @param dialog The dialog to connect to the transaction in the table
     */
    protected void addTransaction(ClientTransaction ct,
				  DialogHandler dialog) {

	dialogTable.put(ct, dialog);
	sc.addDialogHandler(ct, dialog);

    }

    /**
     * Removes a transaction from the dialog table.
     *
     * @param ct The transaction to remove.
     */
    protected void removeTransaction(ClientTransaction ct) {
	sc.removeDialogHandler(ct);
	dialogTable.remove(ct);
    }
    
    public int getState() {
    	return state;
    }

    public void setImmediateBye(boolean immediateBye) {
    	this.immediateBye = immediateBye;
    }
    
    public void setDialog(Dialog dialog) {
    	this.dialog = dialog;
    }
}
