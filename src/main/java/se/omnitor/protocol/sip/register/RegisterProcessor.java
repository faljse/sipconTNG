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
package se.omnitor.protocol.sip.register;

import java.util.Hashtable;
import javax.sip.ServerTransaction;
import javax.sip.ClientTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ContactHeader;
import javax.sip.address.URI;
import se.omnitor.protocol.sip.Processor;
import se.omnitor.protocol.sip.SipController;
import se.omnitor.protocol.sip.DialogHandler;

// import LogClasses and Classes
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles all outgoing REGISTER requests.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class RegisterProcessor extends Processor {

    private static final int NONE = 1;
    private static final int REGISTERING = 2;
    private static final int REGISTERED = 3;

    public static int nbr = 1;
    public int cnbr;

    Hashtable<ClientTransaction, RegisterDialog> dialogTable;

    CallIdHeader callId;
    int lastCseq;

    URI requestUri;
    ToHeader to;
    FromHeader from;
    ContactHeader contact;

    private String lastUsername;
    private String lastPassword;

    private int lastExpires;

    private RegisterRetransmitter regRetransmit;

    private int state;
    private RegisterDialog currentDialog;

    // declare package and classname
    public final static String CLASS_NAME = RegisterProcessor.class.getName();
    // get an instance of Logger
    private static Logger logger = Logger.getLogger(CLASS_NAME);


    /**
     * Initializes.
     *
     * @param sc The SipController to give feedback to
     * @param requestUri The Request URI to use in all packets
     * @param to The To header to use
     * @param from The From header to use
     * @param contact The Contact header to use
     */
    public RegisterProcessor(SipController sc, URI requestUri, ToHeader to, FromHeader from, ContactHeader contact) {
	super(sc);

        // write methodname
        final String METHOD = "RegisterProcessor(SipController sc, URI requestUri, ToHeader to, FromHeader from, ContactHeader contact)";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD, new Object[]{sc, requestUri, to, from, contact});

        cnbr = nbr;
	nbr++;

	this.requestUri = requestUri;
	this.to = to;
	this.from = from;
	this.contact = contact;

	dialogTable = new Hashtable<ClientTransaction, RegisterDialog>();
	regRetransmit = null;

        if (sc.stunIsInUse()) {
            callId = createCallId(sc.getStunStack().getExternalIp());
        }
        else {
            callId = createCallId(sc.getLocalIpAddress());
        }
	lastCseq = 0;

	lastExpires = -1;

	state = NONE;

        logger.logp(Level.FINER, CLASS_NAME, METHOD, "RegisterProcessor created:" + cnbr);
        logger.exiting (CLASS_NAME, METHOD);
    }

    /**
     * Processes an incoming request.
     *
     * @param request The incoming request
     * @param serverTransaction The transaction associated to the request
     */
    public void processRequest(Request request,
			       ServerTransaction serverTransaction) {

	System.out.println("RegisterProcessor " + cnbr + ": " +
			   request.getMethod());

	DialogHandler dh = (DialogHandler)dialogTable.get(serverTransaction);


	if (dh == null) {
	    logger.fine("RegisterProcessor " + cnbr +
			": Unknown incoming method: " + request.getMethod());
	}
	else {
	    dh.processRequest(request, serverTransaction);
	}

    }

    /**
     * Processes an incoming response
     *
     * @param response The incoming response
     * @param clientTransaction The trasnaction associated to the response
     */
    public void processResponse(Response response,
				ClientTransaction clientTransaction) {
	if (clientTransaction != null) {
	    DialogHandler dh =
		(DialogHandler)dialogTable.get(clientTransaction);

	    if (dh != null) {
		dh.processResponse(response);
	    }
	    else {
		logger.fine("RegisterProcessor " + cnbr +
			    ": No dialog handler found for responseEvent!");
	    }
	}
	else {
	    logger.fine("Incoming clientTransaction was null!");
	}

    }

    /**
     * Handles an incoming timeout event.
     *
     * @param timeoutEvent The incoming timeout event.
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
     * Registers to a host. Starts a register retransmitter.
     *
     * @param username The username to use
     * @param password The password to use
     * @param expires The expires value to use.
     */
    public void register(String username, String password, int expires) {

	if (regRetransmit != null) {
	    regRetransmit.stop();
	}

	regRetransmit =
	    new RegisterRetransmitter(this, username, password, expires);

	regRetransmit.start();

	lastUsername = username;
	lastPassword = password;
	state = REGISTERING;


    }

    /**
     * Unregisters from the SIP server.
     *
     */
    public void unregister() {
	switch (state) {
	case NONE:
	    break;
	case REGISTERING:
	    if (currentDialog != null) {
		currentDialog.cancel();
	    }
	    break;
	case REGISTERED:
	    if (lastUsername != null && lastPassword != null) {
		register(lastUsername, lastPassword, 0);
	    }
	    break;
	default:
	}
    }

    /**
     * Gets the last used expires value.
     *
     * @return The last used expires value.
     */
    public int getLastExpires() {
	return lastExpires;
    }

    /**
     * Gets the call ID value
     *
     * @return The call ID value
     */
    public String getCallId() {
	return callId.getCallId();
    }

    /**
     * Adds a transaction to the dialog table.
     *
     * @param ct The transaction to connect to the dialog in the table
     * @param dialog The dialog to connect to the transaction in the table
     */
    protected void addTransaction(ClientTransaction ct,
				  RegisterDialog dialog) {

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

    /**
     * Gets the Cseq value of the next register packet.
     *
     * @return The cseq value to use in the net register packet.
     */
    protected int getCseq() {
	lastCseq++;
	return lastCseq;
    }

    /**
     * Signals a successfur registration.
     *
     * @param expires The number of seconds before the registration expires.
     */
    protected void signalRegistrationSuccess(int expires) {

	state = REGISTERED;

	lastExpires = expires;

	if (expires == 0) {
	    regRetransmit.stop();
	    regRetransmit = null;
	}
	else {
	    regRetransmit.setExpires(expires);
	}

	sc.signalRegistrationSuccess(this);

    }

    /**
     * A register retransmitter class, that will re-register any registration
     * before it expires.
     *
     * @author Andreas Piirimets, Omnitor AB
     */
    class RegisterRetransmitter implements Runnable {

	RegisterProcessor rp;
	int nextRegister = 0;
	boolean running;

	String username;
	String password;
	int expires;

	Thread thread;

	/**
	 * Initializes. Does not start the thread.
	 *
	 * @param rp The RegisterProcessor that the dialogs should belong to
	 * @param username The username to use
	 * @param password The password to use
	 * @param expires The expires value to use
	 */
	public RegisterRetransmitter(RegisterProcessor rp, String username,
				     String password, int expires) {

	    this.rp = rp;
	    this.username = username;
	    this.password = password;
	    this.expires = expires;

	    thread = new Thread(this, "Register retransmitter");

	}

	/**
	 * Runs the thread.
	 *
	 */
	public void run() {

	    while (running) {

		if (nextRegister <= 0) {
		    register();
		    synchronized (this) {
			while (nextRegister <= 0 && running) {
			    try {
				wait();
			    }
			    catch (InterruptedException ie) {
			    }
			}
		    }
		}

		try {
		    Thread.sleep(1000);
		}
		catch (InterruptedException ie) {
		}

		nextRegister -= 1;
/*
                if (sc.stunIsInUse() && (nextRegister % 5 == 0)) {
                    sendNatHolePacket();
                }
 */
	    }

	}

        /**
         * Sends a 1 byte packet to the SIP server to keep the hole in the
         * NAT alive.
         */
        public void sendNatHolePacket() {
            String address;
            int port;

            if (requestUri.toString().startsWith("sip:")) {
                address = requestUri.toString().split("sip:")[1];
            }
            else {
                address = requestUri.toString();
            }
            if (address.split(":").length > 1) {
                port = Integer.parseInt(address.split(":")[1]);
                address = address.split(":")[0];
            }
            else {
                port = 5060;
            }

            byte b[] = new byte[1];
            b[0] = 0;
            try {
                java.net.DatagramPacket packet = new java.net.DatagramPacket(b, 1, java.net.InetAddress.getByName(address), port);
                java.net.DatagramSocket dgs = new java.net.DatagramSocket();
                dgs.send(packet);
                dgs.close();
            }
            catch (java.io.IOException e) {
            	logger.throwing(this.getClass().getName(), "sendNatHolePacket", e);
            }


        }

	/**
	 * Sets a new expires value.
	 *
	 * @param expires The new expires value.
	 */
	public synchronized void setExpires(int expires) {

	    nextRegister = (int)(0.66*(double)expires);
	    notify();

	}

	/**
	 * Starts the thread.
	 *
	 */
	public void start() {
	    running = true;
	    thread.start();
	}

	/**
	 * Stops the thread.
	 *
	 */
	public void stop() {
	    running = false;
	    synchronized (this) {
		notify();
	    }
	}

	/**
	 * Sends a register.
	 *
	 */
	private void register() {
	    RegisterDialog dialog = new RegisterDialog(sc, rp);

	    ClientTransaction ct =
		dialog.register(requestUri, to, from, callId, contact,
				username, password, expires);

	    if (ct != null) {
		addTransaction(ct, dialog);
	    }

	    currentDialog = dialog;
	}


    }

    /**
     * Returns the SIP address. This is a nicely written one, intended
     * to be used in GUI.
     *
     * @return Nice SIP address, to be used in GUI
     */
    public String getSipAddress() {
	return to.getAddress().toString();
    }


}
