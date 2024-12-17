/*
 * Stun4j, the OpenSource Java Solution for NAT and Firewall Traversal.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.stun4j.attribute;

import java.util.Hashtable;
import net.java.stun4j.StunException;

/**
 * The ERROR-CODE attribute is present in the Binding Error Response and
 * Shared Secret Error Response.  It is a numeric value in the range of
 * 100 to 699 plus a textual reason phrase encoded in UTF-8, and is
 * consistent in its code assignments and semantics with SIP [10] and
 * HTTP [15].  The reason phrase is meant for user consumption, and can
 * be anything appropriate for the response code.  The lengths of the
 * reason phrases MUST be a multiple of 4 (measured in bytes).  This can
 * be accomplished by added spaces to the end of the text, if necessary.
 * Recommended reason phrases for the defined response codes are
 * presented below.
 *
 * To facilitate processing, the class of the error code (the hundreds
 * digit) is encoded separately from the rest of the code.
 *
 *   0                   1                   2                   3
 *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |                   0                     |Class|     Number    |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |      Reason Phrase (variable)                                ..
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * The class represents the hundreds digit of the response code.  The
 * value MUST be between 1 and 6.  The number represents the response
 * code modulo 100, and its value MUST be between 0 and 99.
 *
 * The following response codes, along with their recommended reason
 * phrases (in brackets) are defined at this time:
 *
 * 400 (Bad Request): The request was malformed.  The client should not
 *      retry the request without modification from the previous
 *      attempt.
 *
 * 401 (Unauthorized): The Binding Request did not contain a MESSAGE-
 *      INTEGRITY attribute.
 *
 * 420 (Unknown Attribute): The server did not understand a mandatory
 *      attribute in the request.
 *
 * 430 (Stale Credentials): The Binding Request did contain a MESSAGE-
 *      INTEGRITY attribute, but it used a shared secret that has
 *      expired.  The client should obtain a new shared secret and try
 *      again.
 *
 * 431 (Integrity Check Failure): The Binding Request contained a
 *      MESSAGE-INTEGRITY attribute, but the HMAC failed verification.
 *      This could be a sign of a potential attack, or client
 *      implementation error.
 *
 * 432 (Missing Username): The Binding Request contained a MESSAGE-
 *      INTEGRITY attribute, but not a USERNAME attribute.  Both must be
 *      present for integrity checks.
 *
 * 433 (Use TLS): The Shared Secret request has to be sent over TLS, but
 *      was not received over TLS.
 *
 * 500 (Server Error): The server has suffered a temporary error. The
 *      client should try again.
 *
 * 600 (Global Failure:) The server is refusing to fulfill the request.
 *      The client should not retry.
 *
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: Louis Pasteur University, Strasbourg, France</p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr)</p></p>
 * @author Emil Ivov
 * @version 0.1
 */
public class ErrorCodeAttribute extends Attribute
{
    // Common error codes
    public static final char BAD_REQUEST   = 400;
    public static final char UNAUTHORIZED  = 401;
    public static final char UNKNOWN_ATTRIBUTE = 420;
    public static final char STALE_CREDENTIALS = 430;
    public static final char INTEGRITY_CHECK_FAILURE = 431;
    public static final char MISSING_USERNAME = 432;
    public static final char USE_TLS = 433;
    public static final char SERVER_ERROR = 500;
    public static final char GLOBAL_FAILURE = 600;


    /**
     * The class represents the hundreds digit of the response code.  The
     * value MUST be between 1 and 6.
     */
    private byte errorClass = 0;

    /**
     * The number represents the response
     * code modulo 100, and its value MUST be between 0 and 99.
     */
    private byte errorNumber = 0;

    /**
     * The reason phrase is meant for user consumption, and can
     * be anything appropriate for the response code.  The lengths of the
     * reason phrases MUST be a multiple of 4 (measured in bytes).
     */
    private String reasonPhrase = null;

    /**
     * Constructs a new ERROR-CODE attribute
     */
    ErrorCodeAttribute()
    {
        super(ERROR_CODE);
    }

    /**
     * A convenience method that sets error class and number according to the
     * specified errorCode.The class represents the hundreds digit of the error code.
     * The value MUST be between 1 and 6.  The number represents the response
     * code modulo 100, and its value MUST be between 0 and 99.
     *
     * @param errorCode the errorCode that this class encapsulates.
     * @throws StunException if errorCode is not a valid error code.
     */
    public void setErrorCode(char errorCode)
        throws StunException
    {
        setErrorClass((byte)(errorCode/100));
        setErrorNumber((byte)(errorCode % 100));
    }

    /**
     * A convenience method that constructs an error code from this Attribute's
     * class and number.
     * @return the code of the error this attribute represents.
     */
    public char getErrorCode()
    {
        return (char)(getErrorClass()*100 + getErrorNumber());
    }


    /**
     * Sets this attribute's error number.
     * @param errorNumber the error number to assign this attribute.
     * @throws StunException if errorNumber is not a valid error number.
     */
    public void setErrorNumber(byte errorNumber)
        throws StunException
    {
        if(errorNumber < 0 || errorNumber > 99)
            throw new StunException(StunException.ILLEGAL_ARGUMENT,
                                    errorNumber + " is not a valid error number!");
        this.errorNumber = errorNumber;
    }

    /**
     * Returns this attribute's error number.
     * @return  this attribute's error number.
     */
    public byte getErrorNumber()
    {
        return this.errorNumber;
    }

    /**
     * Sets this error's error class.
     * @param errorClass this error's error class.
     * @throws StunException if errorClass is not a valid error class.
     */
    public void setErrorClass(byte errorClass)
        throws StunException
    {
        if(errorClass < 0 || errorClass > 99)
            throw new StunException(StunException.ILLEGAL_ARGUMENT,
                                    errorClass + " is not a valid error number!");
        this.errorClass = errorClass;
    }

    /**
     * Returns this error's error class.
     * @return this error's error class.
     */
    public byte getErrorClass()
    {
        return errorClass;
    }


    /**
     * Returns a default reason phrase corresponding to the specified error
     * code, as described by rfc 3489.
     * @param errorCode the code of the error that the reason phrase must
     *                  describe.
     * @return a default reason phrase corresponding to the specified error
     * code, as described by rfc 3489.
     */
    public static String getDefaultReasonPhrase(char errorCode)
    {
        switch(errorCode)
        {
            case 400: return  "(Bad Request): The request was malformed.  The client should not "
                             +"retry the request without modification from the previous attempt.";
            case 401: return  "(Unauthorized): The Binding Request did not contain a MESSAGE-"
                             +"INTEGRITY attribute.";
            case 420: return  "(Unknown Attribute): The server did not understand a mandatory "
                             +"attribute in the request.";
            case 430: return  "(Stale Credentials): The Binding Request did contain a MESSAGE-"
                             +"INTEGRITY attribute, but it used a shared secret that has "
                             +"expired.  The client should obtain a new shared secret and try"
                             +"again";
            case 431: return  "(Integrity Check Failure): The Binding Request contained a "
                             +"MESSAGE-INTEGRITY attribute, but the HMAC failed verification. "
                             +"This could be a sign of a potential attack, or client "
                             +"implementation error.";
            case 432: return  "(Missing Username): The Binding Request contained a MESSAGE-"
                             +"INTEGRITY attribute, but not a USERNAME attribute.  Both must be"
                             +"present for integrity checks.";
            case 433: return  "(Use TLS): The Shared Secret request has to be sent over TLS, but"
                             +"was not received over TLS.";
            case 500: return  "(Server Error): The server has suffered a temporary error. The"
                             +"client should try again.";
            case 600: return "(Global Failure:) The server is refusing to fulfill the request."
                             +"The client should not retry.";

            default:  return "Unknown Error";
        }
    }

    /**
     * Set's a reason phrase. The reason phrase is meant for user consumption,
     * and can be anything appropriate for the response code.  The lengths of
     * the reason phrases MUST be a multiple of 4 (measured in bytes).
     *
     * @param reasonPhrase a reason phrase that describes this error.
     */
    public void setReasonPhrase(String reasonPhrase)
    {
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Returns the reason phrase. The reason phrase is meant for user consumption,
     * and can be anything appropriate for the response code.  The lengths of
     * the reason phrases MUST be a multiple of 4 (measured in bytes).
     *
     * @return reasonPhrase a reason phrase that describes this error.
     */
    public String getReasonPhrase()
    {
        return this.reasonPhrase;
    }

    /**
     * Returns the human readable name of this attribute. Attribute names do
     * not really matter from the protocol point of view. They are only used
     * for debugging and readability.
     * @return this attribute's name.
     */
    public String getName()
    {
        return NAME;
    }

    public static final String NAME = "ERROR-CODE";

    /**
     * Returns the length of this attribute's body.
     * @return the length of this attribute's value.
     */
    public char getDataLength()
    {
        char len = (char)( 4 //error code numbers
                           + (char)(
                                    reasonPhrase == null? 0
                                                          :reasonPhrase.length()*2
                                    ));

        /*
         * According to rfc 3489 The length of the
         * reason phrases MUST be a multiple of 4 (measured in bytes)
         */
        len += 4 - (len%4);
        return len;
    }

    /**
     * Returns a binary representation of this attribute.
     * @return a binary representation of this attribute.
     */
    public byte[] encode()
    {
        byte binValue[] =  new byte[HEADER_LENGTH + getDataLength()];

        //Type
        binValue[0] = (byte) (getAttributeType() >> 8);
        binValue[1] = (byte) (getAttributeType() & 0x00FF);
        //Length
        binValue[2] = (byte) (getDataLength() >> 8);
        binValue[3] = (byte) (getDataLength() & 0x00FF);

        //Not used
        binValue[4] = 0x00;
        binValue[5] = 0x00;

        //Error code
        binValue[6] = getErrorClass();
        binValue[7] = getErrorNumber();

        int offset = 8;
        char chars[] = reasonPhrase.toCharArray();
        for (int i = 0; i < reasonPhrase.length(); i++, offset += 2) {
            binValue[offset]   = (byte)(chars[i]>>8);
            binValue[offset+1] = (byte)(chars[i] & 0xFF);
        }

        //The lengths of the reason phrases MUST be a multiple of 4 (measured
        //in bytes)
        if( reasonPhrase.length()%4 != 0)
        {
            binValue[binValue.length - 2] = (byte) ( ( (int) ' ') >> 8);
            binValue[binValue.length - 1] = (byte) ( ( (int) ' ') & 0x00FF);
        }

        return binValue;
    }

    /**
     * Compares two STUN Attributes. Attributeas are considered equal when their
     * type, length, and all data are the same.
     *
     * @param obj the object to compare this attribute with.
     * @return true if the attributes are equal and false otherwise.
     */
     public boolean equals(Object obj)
     {
         if (! (obj instanceof ErrorCodeAttribute)
             || obj == null)
             return false;

         if (obj == this)
             return true;

         ErrorCodeAttribute att = (ErrorCodeAttribute) obj;
         if (att.getAttributeType() != getAttributeType()
             || att.getDataLength() != getDataLength()
             //compare data
             || att.getErrorClass() != getErrorClass()
             || att.getErrorNumber()!= getErrorNumber()
             || ( att.getReasonPhrase() != null
                  && !att.getReasonPhrase().equals(getReasonPhrase()))
             )
             return false;

         return true;
    }

    /**
     * Sets this attribute's fields according to attributeValue array.
     *
     * @param attributeValue a binary array containing this attribute's field
     *                       values and NOT containing the attribute header.
     * @param offset the position where attribute values begin (most often
     * 				 offset is equal to the index of the first byte after
     * 				 length)
     * @param length the length of the binary array.
     * @throws StunException if attrubteValue contains invalid data.
     */
    void decodeAttributeBody(byte[] attributeValue, char offset, char length) throws
        StunException
    {

        offset += 2; //skip the 0s

        //Error code
        setErrorClass(attributeValue[offset++]);
        setErrorNumber(attributeValue[offset++]);

        //Reason Phrase
        char reasonPhrase[] = new char[(length-4)/2];

        for (int i = 0; i < reasonPhrase.length; i++, offset+=2) {
            reasonPhrase[i] =
                (char)(attributeValue[offset] | attributeValue[offset+1]);
        }
        setReasonPhrase(new String(reasonPhrase).trim());

    }


}
