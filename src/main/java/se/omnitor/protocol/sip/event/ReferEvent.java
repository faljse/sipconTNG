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
import java.util.logging.Logger;
import javax.sip.header.*;
import se.omnitor.protocol.sip.SipController;
import se.omnitor.protocol.sip.call.CallProcessor;

/**
 * @author Andreas Piirimets, Omnitor AB
 */
public class ReferEvent extends Event {

    private byte[] data;
    private Logger logger;
    private int statusCode;
    private CallProcessor cp;

    public ReferEvent(SipController sc, CallProcessor cp, String callId, 
		      byte[] data, int statusCode) {
	super(sc);

	logger = Logger.getLogger("se.omnitor.protocol.sip.event");

	this.cp = cp;
	this.data = data;
	this.statusCode = statusCode;

    }

    public byte[] getData() {
	return data;
    }

    public int getEventId() {
	return REFER;
    }

    /**
     * @param parameters All parameters that should be added to the Event
     * header without trailing semi-colon. All parameters must be separated by
     * semi-colon. If null or empty string is given, no parameters will be
     * attached to the header.
     */
    public EventHeader getEventHeader(String parameters) {
	EventHeader eventHeader;

	String name = "refer";
	if ((parameters != null) && !parameters.equals("")) {
	    name += ";" + parameters;
	}

	try {
	    eventHeader = 
		sc.headerFactory.createEventHeader(name);
	}
	catch (ParseException pe) {
	    logger.throwing(this.getClass().getName(), "<init>", pe);
	    eventHeader = null;
	}

	return eventHeader;
    }

    public void notifySubscriber(EventSubscriber subscriber) {

	String subscriptionState;

	if (statusCode >= 300) {
	    subscriptionState = "terminated;reason=noresource";
	    sc.signalCallTransferError(statusCode);
	}
	else {
	    subscriptionState = "active";
	    sc.signalCallTransferSuccess(cp);
	}

	SubscriptionStateHeader ssh = null;
	try {
	    ssh = 
		sc.headerFactory.createSubscriptionStateHeader
		(subscriptionState);
	}
	catch (ParseException pe) {
	    logger.throwing(this.getClass().getName(), "notifySubscriber", pe);
	}

	sendNotify(subscriber, ssh);
    }


}







