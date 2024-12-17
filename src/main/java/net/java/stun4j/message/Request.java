/*
 * Stun4j, the OpenSource Java Solution for NAT and Firewall Traversal.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.stun4j.message;

import net.java.stun4j.StunException;

/**
 * A request descendant of the message class. The primary purpose of the
 * Request class is to allow better functional definition of the classes in the
 * stack package.
 *
 * <p>Organisation: <p> Louis Pasteur University, Strasbourg, France</p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr)</p></p>
 * @author Emil Ivov
 * @version 0.1
 */

public class Request extends Message
{

    Request()
    {

    }

    /**
     * Checks whether requestType is a valid request type and if yes sets it
     * as the type of the current instance.
     * @param requestType the type to set
     * @throws StunException ILLEGAL_ARGUMENT if requestType is not a valid
     * request type
     */
    public void setMessageType(char requestType)
        throws StunException
    {
        if(!isRequestType(requestType))
            throw new StunException(StunException.ILLEGAL_ARGUMENT,
                                    (int)(requestType)
                                    + " - is not a valid request type.");


        super.setMessageType(requestType);
    }

}
