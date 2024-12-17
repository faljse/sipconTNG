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

import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import javax.sip.*;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;
import se.omnitor.protocol.sip.*;

// import LogClasses and Classes
import java.util.logging.Level;

public class RegisterDialog extends DialogHandler {

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
    private String username;
    private String password;
    private int expires;

    private RegisterProcessor rp;

    private int requestNbr;
    private boolean firstAuthTry;
    private ClientTransaction currentTransaction;

    private boolean isCancelled;

    // declare package and classname
    public final static String CLASS_NAME = RegisterDialog.class.getName();
    // get an instance of Logger
    private static Logger logger = Logger.getLogger(CLASS_NAME);


    public RegisterDialog(SipController sc, RegisterProcessor rp) {

        super(sc);

        // write methodname
        final String METHOD = "RegisterDialog(SipController sc, RegisterProcessor rp)";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD, new Object[]{sc, rp});

	this.rp = rp;

	cnbr = nbr;
	nbr++;

	requestNbr = 1;
	firstAuthTry = true;

	isCancelled = false;


	logger.logp(Level.FINER, CLASS_NAME, METHOD, "RegisterDialog created:" + cnbr);
        logger.exiting (CLASS_NAME, METHOD);
    }

    public void processRequest(Request request,
				  ServerTransaction transaction) {
	logger.finest("RegisterDialog " + cnbr + ": " + request.getMethod());
    }

    public void processResponse(Response response) {
	logger.finest("RegisterDialog " + cnbr + ": " +
		      response.getStatusCode());

	switch (response.getStatusCode()) {
	case 200:
	    if (getState() == EXPECTING_OK) {
		process200Response(response);
	    }
	    break;
	case 401:
	    if (getState() == EXPECTING_OK) {
		process401Response(response);
	    }
	    break;
	default:
	    if (getState() == EXPECTING_OK) {
		if (response.getStatusCode() > 199) {
		    sc.signalRegistrationError(this, response.getStatusCode());
		    rp.removeTransaction(currentTransaction);
		}
	    }
	}
    }

    public void processTimeout(TimeoutEvent timeoutEvent) {
	Timeout timeout = timeoutEvent.getTimeout();

	if (timeout.getValue() == Timeout._TRANSACTION) {
	    if (!isCancelled) {
		sc.signalRegistrationError(this, 0);
	    }
	    rp.removeTransaction(currentTransaction);
	}

    }

    private void process200Response(Response response) {

	setState(FINISHED);

	ListIterator contactList = response.getHeaders("Contact");
	SipURI origUri = (SipURI)contact.getAddress().getURI();

	String origUser = origUri.getUser();
	if (origUser == null) {
	    origUser = "";
	}
	String origPass = origUri.getUserPassword();
	if (origPass == null) {
	    origPass = "";
	}
	String origHost = origUri.getHost();
	if (origHost == null) {
	    origHost = "";
	}
	int origPort = origUri.getPort();


	ContactHeader tmpContact;
	URI tempUri;

	try {
	    while (true) {
		tmpContact = (ContactHeader)contactList.next();

		tempUri = tmpContact.getAddress().getURI();

		if (tempUri.isSipURI()) {
		    String tempUser = ((SipURI)tempUri).getUser();
		    if (tempUser == null) {
			tempUser = "";
		    }
		    String tempPass = ((SipURI)tempUri).getUserPassword();
		    if (tempPass == null) {
			tempPass = "";
		    }
		    String tempHost = ((SipURI)tempUri).getHost();
		    if (tempHost == null) {
			tempHost = "";
		    }
		    int tempPort = ((SipURI)tempUri).getPort();

		    if (tempUser.equals(origUser) &&
			tempPass.equals(origPass) &&
			tempHost.equals(origHost) &&
			tempPort == origPort) {

			expires = tmpContact.getExpires();
			break;
		    }
		}

	    }
	}
	catch (NoSuchElementException nsee) {
	    // End of list
	}

	rp.signalRegistrationSuccess(expires);
	rp.removeTransaction(currentTransaction);
    }

    private void process401Response(Response response) {

	// Don't try this in an endless loop, give up after a few tries.
	if (requestNbr > 5) {
	    sc.signalRegistrationError(this, 401);
	    return;
	}

	AuthorizationHeader authHeader =
	    createAuthorizationHeader(response, "REGISTER",
				      requestUri, requestNbr,
				      username, password, firstAuthTry);

	firstAuthTry = false;
	requestNbr++;

	if (authHeader == null) {
	    sc.signalRegistrationError(this, 401);
	    return;
	}


	ClientTransaction ct = send(authHeader);

	rp.removeTransaction(currentTransaction);
	rp.addTransaction(ct, this);
	currentTransaction = ct;
    }


    protected ClientTransaction register(URI requestUri, ToHeader to,
					 FromHeader from,
					 CallIdHeader callId,
					 ContactHeader contact,
					 String username, String password,
					 int expires) {

	this.requestUri = requestUri;
	this.to = to;
	this.from = from;
	this.callId = callId;
	this.contact = contact;
	this.username = username;
	this.password = password;
	this.expires = expires;

	ClientTransaction ct = send(null);

	if (ct == null) {
	    sc.signalRegistrationError(this, -1);
	}

	currentTransaction = ct;

	return ct;

    }

    /**
     * Cancels an outgoing request.
     *
     */
    public void cancel() {
	if (getState() == EXPECTING_OK) {
	    isCancelled = true;
	    //cancelOriginalRequest();
	}
    }


    private ClientTransaction send(AuthorizationHeader authHeader) {

	ClientTransaction transaction;
	Header[] headers;

	try {
	    ExpiresHeader expiresHeader =
		sc.headerFactory.createExpiresHeader(expires);

	    if (authHeader == null) {
		headers = new Header[] { contact, expiresHeader };
	    }
	    else {
		headers = new Header[] { contact, expiresHeader, authHeader };
	    }
	}
	catch (InvalidArgumentException iae) {
	    if (authHeader == null) {
		headers = new Header[] { contact };
	    }
	    else {
		headers = new Header[] { contact, authHeader };
	    }
	}

	transaction =
	    sendRequest(requestUri, "REGISTER", callId, rp.getCseq(),
			from, to, headers, null, null);

	setState(EXPECTING_OK);

	return transaction;

    }

    /**
     * Returns the SIP address. This is a nicely written one, intended
     * to be used in GUI.
     *
     */
    public String getSipAddress() {
	return to.getAddress().toString();
    }

    /**
     * Gets the register processor
     *
     * @return The register processor
     */
    public RegisterProcessor getRegisterProcessor() {
	return rp;
    }


}
