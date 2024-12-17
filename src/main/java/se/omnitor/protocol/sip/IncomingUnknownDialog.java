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
package se.omnitor.protocol.sip;

import javax.sip.*;
import javax.sip.message.*;
import javax.sip.header.*;

/**
 * @author Andreas Piirimets, Omnitor AB
 *
 * @todo Change the NOTIFY stuff to use subscription events instead!
 */
public class IncomingUnknownDialog extends DialogHandler {

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



    /**
     * Initializes.
     *
     * @param sc The Sipontroller that handles this SIP session
     * @param cp The CappProcessor that handles this call
     */
    public IncomingUnknownDialog(SipController sc, Processor p) {
	super(sc);

	cnbr = nbr;
	nbr++;
    }

    /**
     * Process an incoming request that is not allowed.
     *
     * @param request The incoming request
     * @param serverTransaction The server transaction connected to the request
     */
    public synchronized void processRequest(Request request,
					       ServerTransaction
					       serverTransaction) {

	AllowHeader ah = getAllowHeader();

	Header[] headers;

	if (ah != null) {
	    headers = new Header[] { ah };
	}
	else {
	    headers = new Header[0];
	}

	sendResponse(Response.METHOD_NOT_ALLOWED, request, serverTransaction,
		     headers, null, null, null);

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

	System.out.println("IncomingUnknownDialog " + cnbr + ": " + timeout.toString());

	if (timeout.getValue() == Timeout._TRANSACTION) {
	    // Stop sending and do nothing.
	}
	else {
	    // Re-send transaction
	    resendLastResponse();
	}

    }


}

