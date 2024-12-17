/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright  2003 Sun Microsystems, Inc. All rights reserved.
 * Copyright  2005 BEA Systems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties. 
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JSIP Specification
 * File Name     : FromHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;



import java.text.ParseException;



/**
 * The From header field indicates the logical identity of the initiator

 * of the request, possibly the user's address-of-record. This may be different

 * from the initiator of the dialog.  Requests sent by the callee to the caller

 * use the callee's address in the From header field.

 * <p>

 * Like the To header field, it contains a URI and optionally a display name,

 * encapsulated in a {@link javax.sip.address.Address}.  It is used by SIP

 * elements to determine which processing rules to apply to a request (for

 * example, automatic call rejection). As such, it is very important that the

 * From URI not contain IP addresses or the FQDN of the host on which the UA is

 * running, since these are not logical names.

 * <p>

 * The From header field allows for a display name.  A UAC SHOULD use

 * the display name "Anonymous", along with a syntactically correct, but

 * otherwise meaningless URI (like sip:thisis@anonymous.invalid), if the

 * identity of the client is to remain hidden.

 * <p>

 * Usually, the value that populates the From header field in requests

 * generated by a particular UA is pre-provisioned by the user or by the

 * administrators of the user's local domain.  If a particular UA is used by

 * multiple users, it might have switchable profiles that include a URI

 * corresponding to the identity of the profiled user. Recipients of requests

 * can authenticate the originator of a request in order to ascertain that

 * they are who their From header field claims they are.

 * <p>

 * Two From header fields are equivalent if their URIs match, and their

 * parameters match. Extension parameters in one header field, not present in

 * the other are ignored for the purposes of comparison. This means that the

 * display name and presence or absence of angle brackets do not affect

 * matching.

 * <ul>

 * <li> The "Tag" parameter - is used in the To and From header fields of SIP

 * messages.  It serves as a general mechanism to identify a dialog, which is

 * the combination of the Call-ID along with two tags, one from each

 * participant in the dialog.  When a User Agent sends a request outside of a dialog,

 * it contains a From tag only, providing "half" of the dialog ID. The dialog

 * is completed from the response(s), each of which contributes the second half

 * in the To header field. When a tag is generated by a User Agent for insertion into

 * a request or response, it MUST be globally unique and cryptographically

 * random with at least 32 bits of randomness. Besides the requirement for

 * global uniqueness, the algorithm for generating a tag is implementation

 * specific.  Tags are helpful in fault tolerant systems, where a dialog is to

 * be recovered on an alternate server after a failure.  A UAS can select the

 * tag in such a way that a backup can recognize a request as part of a dialog

 * on the failed server, and therefore determine that it should attempt to

 * recover the dialog and any other state associated with it.

 * </ul>
 * For Example:<br>
 * <code>From: "Bob" sips:bob@biloxi.com ;tag=a48s<br>
 * From: sip:+12125551212@phone2net.com;tag=887s<br>
 * From: Anonymous sip:c8oqz84zk7z@privacy.org;tag=hyh8</code>
 *
 * @author BEA Systems, Inc.
 * @author NIST
 * @version 1.2
 */
public interface FromHeader extends HeaderAddress, Parameters, Header {



    /**

     * Sets the tag parameter of the FromHeader. The tag in the From field of a
     * request identifies the peer of the dialog. When a UA sends a request
     * outside of a dialog, it contains a From tag only, providing "half" of
     * the dialog Identifier.
     * <p>
     * The From Header MUST contain a new "tag" parameter, chosen by the UAC 
     * applicaton. Once the initial From "tag" is assigned it should not be 
     * manipulated by the application. That is on the client side for outbound 
     * requests the application is responsible for Tag assigmennment, after 
     * dialog establishment the stack will take care of Tag assignment.
     *
     * @param tag - the new tag of the FromHeader
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the Tag value.
     */
    public void setTag(String tag) throws ParseException;



    /**
     * Gets the tag of FromHeader. The Tag parameter identified the Peer of the
     * dialogue and must always be present.
     *
     * @return the tag parameter of the FromHeader.
     */
    public String getTag();


    /**
     * Compare this FromHeader for equality with another. This method 
     * overrides the equals method in javax.sip.Header. This method specifies 
     * object equality as outlined by  
     * <a href = "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a>. 
     * Two From header fields are equivalent if their URIs match, and their 
     * parameters match. Extension parameters in one header field, not present 
     * in the other are ignored for the purposes of comparison. This means that 
     * the display name and presence or absence of angle brackets do not affect 
     * matching. When comparing header fields, field names are always 
     * case-insensitive. Unless otherwise stated in the definition of a 
     * particular header field, field values, parameter names, and parameter 
     * values are case-insensitive. Tokens are always case-insensitive. Unless 
     * specified otherwise, values expressed as quoted strings are case-sensitive.
     *
     * @param obj the object to compare this FromHeader with.
     * @return <code>true</code> if <code>obj</code> is an instance of this class
     * representing the same FromHeader as this, <code>false</code> otherwise.
     * @since v1.2
     */
    public boolean equals(Object obj);    


    /**

     * Name of FromHeader

     */

    public final static String NAME = "From";

}
