/*
 * Stun4j, the OpenSource Java Solution for NAT and Firewall Traversal.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.stun4j.attribute;


/**
 * The CHANGED-ADDRESS attribute indicates the IP address and port where
 * responses would have been sent from if the "change IP" and "change
 * port" flags had been set in the CHANGE-REQUEST attribute of the
 * Binding Request.  The attribute is always present in a Binding
 * Response, independent of the value of the flags.  Its syntax is
 * identical to MAPPED-ADDRESS.
 *
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: Louis Pasteur University, Strasbourg, France</p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr)</p></p>
 * @author Emil Ivov
 * @version 0.1
 */

public class ChangedAddressAttribute extends AddressAttribute
{
    public static final String NAME = "CHANGED-ADDRESS";

    /**
     * Creates a CHANGED_ADDRESS attribute
     */
    public ChangedAddressAttribute()
    {
        super(CHANGED_ADDRESS);
    }

}
