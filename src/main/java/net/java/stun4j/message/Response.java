/*
 * Stun4j, the OpenSource Java Solution for NAT and Firewall Traversal.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.stun4j.message;

import net.java.stun4j.StunException;

/**
 * A response descendant of the message class. The primary purpose of the
 * Response class is to allow better functional definition of the classes in the
 * stack package.
 *
 * <p>Organisation: <p> Louis Pasteur University, Strasbourg, France</p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr)</p></p>
 * @author Emil Ivov
 * @version 0.1
 */

public class Response extends Message
{

    Response()
    {

    }

    /**
     * Checks whether responseType is a valid response type and if yes sets it
     * as the type of the current instance.
     * @param responseType the type to set
     * @throws StunException ILLEGAL_ARGUMENT if responseType is not a valid
     * response type
     */
    public void setMessageType(char responseType)
        throws StunException
    {
        if(!isResponseType(responseType))
            throw new StunException(StunException.ILLEGAL_ARGUMENT,
                                    (int)(responseType)
                                    + " - is not a valid response type.");


        super.setMessageType(responseType);
    }

}
