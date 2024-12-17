/*
 * Stun4j, the OpenSource Java Solution for NAT and Firewall Traversal.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.stun4j.stack;

import net.java.stun4j.StunMessageEvent;

/**
 * The class is used for collecting incoming STUN messages from the
 * NetAccessManager (and more precisely - MessageProcessors). This is our
 * way of keeping scalable network and stun layers.
 *
 * <p>Organisation: <p> Louis Pasteur University, Strasbourg, France</p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr)</p></p>
 * @author Emil Ivov
 * @version 0.1
 */

interface MessageEventHandler
{
    /**
     * Called when an incoming message has been received, parsed and is ready
     * for delivery.
     * @param evt the Event object that encapsulates the newly received message.
     */
    public void handleMessageEvent(StunMessageEvent evt);
}
