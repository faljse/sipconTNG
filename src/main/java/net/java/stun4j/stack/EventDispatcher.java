
/*
 * Stun4j, the OpenSource Java Solution for NAT and Firewall Traversal.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.stun4j.stack;

import java.util.*;

import net.java.stun4j.*;

/**
 * This is a utility class used for dispatching incoming request events. We use
 * this class mainly (and probably solely) for its ability to handle listener
 * proxies  (i.e. listeners interested in requests recevied on a particular
 * NetAccessPoint only).
 *
 * @author Emil Ivov
 */
public class EventDispatcher
{

    /**
     * All property change listeners registered so far.
     */
    private Vector requestListeners;

    /**
     * Hashtable for managing property change listeners registered for specific
     * properties. Maps property names to PropertyChangeSupport objects.
     */
    private Hashtable requestListenersChildren;

    /**
     * Constructs an <code>EventDispatcher</code> object.
     */
    public EventDispatcher()
    {
    }

    /**
     * Add a RequestListener to the listener list. The listener is registered
     * for requests coming from no matter which NetAccessPoint.
     *
     * @param listener  The ReuqestListener to be added
     */
    public synchronized void addRequestListener(RequestListener listener)
    {
        if (requestListeners == null)
        {
            requestListeners = new Vector();
        }

        requestListeners.addElement(listener);
    }

    /**
     * Add a RequestListener for a specific NetAccessPoint. The listener
     * will be invoked only when a call on fireRequestReceived is issued for
     * that specific NetAccessPoint.
     *
     * @param descriptor  The NETAP descriptor that we're interested in.
     * @param listener  The ConfigurationChangeListener to be added
     */

    public synchronized void addRequestListener(
        NetAccessPointDescriptor descriptor,
        RequestListener listener)
    {
        if (requestListenersChildren == null)
        {
            requestListenersChildren = new Hashtable();
        }
        EventDispatcher child = (EventDispatcher) requestListenersChildren.get(
            descriptor);
        if (child == null)
        {
            child = new EventDispatcher();
            requestListenersChildren.put(descriptor, child);
        }
        child.addRequestListener(listener);
    }

    /**
     * Remove a RquestListener from the listener list.
     * This removes a RequestListener that was registered
     * for all NetAccessPoints and would not remove lsiteners registered for
     * specific NetAccessPointDescriptors.
     *
     * @param listener The RequestListener to be removed
     */
    public synchronized void removeRequestListener(
        RequestListener listener)
    {

        if (requestListeners == null)
        {
            return;
        }
        requestListeners.removeElement(listener);
    }

    /**
     * Remove a RequestListener for a specific NetAccessPointDescriptor. This
     * would only remove the listener for the specified NetAccessPointDescriptor
     * and would not remove it if it was also registered as a wildcard listener.
     *
     * @param apDescriptor  The NetAPDescriptor that was listened on.
     * @param listener  The RequestListener to be removed
     */
    public synchronized void removeRequestListener(
        NetAccessPointDescriptor apDescriptor,
        RequestListener listener)
    {
        if (requestListenersChildren == null)
        {
            return;
        }
        EventDispatcher child =
            (EventDispatcher)requestListenersChildren.get( apDescriptor );

        if (child == null)
        {
            return;
        }
        child.removeRequestListener(listener);
    }


    /**
     * Dispatch a StunMessageEvent to any registered listeners.
     *
     * @param evt  The request event to be delivered.
     */
    public void fireMessageEvent(StunMessageEvent evt)
    {
        NetAccessPointDescriptor apDescriptor = evt.getSourceAccessPoint();
        if (requestListeners != null)
        {
            Iterator iterator = requestListeners.iterator();
            while (iterator.hasNext())
            {
                RequestListener target =
                    (RequestListener) iterator.next();
                target.requestReceived(evt);
            }
        }

        if (requestListenersChildren != null && apDescriptor != null)
        {
            EventDispatcher child = (EventDispatcher) requestListenersChildren.
                                                            get(apDescriptor);
            apDescriptor.getAddress().toString();

            Enumeration enumer = requestListenersChildren.elements();
            while (enumer.hasMoreElements())
            {
                Object item = (Object) enumer.nextElement();

            }

            if (child != null)
            {
                child.fireMessageEvent(evt);
            }
        }
    }

    /**
     * Check if there are any listeners for a specific NetAccessPointDescriptor.
     * (Generic listeners count as well)
     *
     * @param apDescriptor  the NetAccessPointDescriptor.
     * @return true if there are one or more listeners for the specified
     * NetAccessPointDescriptor
     */
    public synchronized boolean hasRequestListeners(
        NetAccessPointDescriptor apDescriptor)
    {
        if(requestListeners != null && !requestListeners.isEmpty())
        {
            // there is a generic listener
            return true;
        }
        if (requestListenersChildren != null)
        {
            EventDispatcher child = (EventDispatcher)
                requestListenersChildren.get(apDescriptor);
            if (child != null && child.requestListeners != null)
            {
                return!child.requestListeners.isEmpty();
            }
        }
        return false;
    }

    /**
     * Removes (absolutely all listeners for this event dispatcher).
     */
    public void removeAllListeners()
    {
        if(requestListeners != null)
            requestListeners.removeAllElements();
        requestListenersChildren = null;
    }
}
