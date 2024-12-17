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
import javax.sip.address.*;
import javax.sip.header.*;
import se.omnitor.protocol.sip.SipController;

/**
 * @author Andreas Piirimets, Omnitor AB
 */
public abstract class Event {

    /*
     * Event constants has to start with 0 and increase with one, since they
     * are used as index numbers in arrays.
     *
     */

    public static final int NBR_OF_EVENTS = 1;

    public static final int REFER = 0;


    protected SipController sc;
    private Logger logger;

    public Event(SipController sc) {
	logger = Logger.getLogger("se.omnitor.protocol.sip.event");

	this.sc = sc;
    }

    public abstract byte[] getData();

    public abstract int getEventId();

    public abstract EventHeader getEventHeader(String parameters);

    public abstract void notifySubscriber(EventSubscriber subscriber);


    public void sendNotify(EventSubscriber subscriber,
			   SubscriptionStateHeader subscriptionState) {

	String name = 
	    ((SipURI)subscriber.getFromHeader().getAddress().getURI()).
	    getUser();

	ContactHeader ch = null;
	try {
	    ch = sc.createContactHeader(name);
	}
	catch (ParseException pe) {
	    logger.throwing(this.getClass().getName(), "sendNotify", pe);
	}

	NotifyProcessor np = subscriber.getNotifyProcessor();
	np.notify(subscriber.getRequestUri(), subscriber.getFromHeader(), 
		  subscriber.getToHeader(), subscriber.getCallIdHeader(), ch, 
		  getAllowEvents(), 
		  getEventHeader("id=" + subscriber.getIdParam()), 
		  subscriptionState, getData());

    }

    private AllowEventsHeader getAllowEvents() {
	AllowEventsHeader aeh = null;

	try {
	    aeh = sc.headerFactory.createAllowEventsHeader("refer");
	}
	catch (ParseException pe) {
	    logger.throwing(this.getClass().getName(), "getAllowEvents", pe);
	}

	return aeh;
    }

}

/*

	Header[] headers = 
	    new Header[] { getAllowEvents(), event, subscriptionState };

	

	// CallId
	Contact
	    CSeq
	    From
	    Max-Forwards
To
Via
Allow-Events
Event
Subscription-State

*/
