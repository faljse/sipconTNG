/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
* See ../../../../doc/uncopyright.html for conditions of use.                  *
* Author: M. Ranganathan (mranga@nist.gov)                                     *
* Modified By:  O. Deruelle (deruelle@nist.gov), added JAVADOC                 *                                                                                   
* Questions/Comments: nist-sip-dev@antd.nist.gov                               *
*******************************************************************************/

package gov.nist.javax.sip.header;

/** 
 * Product Token class
 * @version JAIN-SIP-1.1 $Revision: 1.1.1.1 $ $Date: 2005/05/17 08:13:59 $
 */
public class ProductToken extends SIPObject {

	/**
	 * name field
	 */
	protected String name;

	/**
	 * version field
	 */
	protected String version;

	/**
	 * Return canonical form.
	 * @return String
	 */
	public String encode() {
		if (version != null)
			return name + SLASH + version;
		else
			return name;
	}

	/**
	 * Return the name field.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the version field.
	 * @return String
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Set the name member
	 * @param n String to set
	 */
	public void setName(String n) {
		name = n;
	}

	/**
	 * Set the version member
	 * @param v String to set
	 */
	public void setVersion(String v) {
		version = v;
	}

}
/*
 * $Log: ProductToken.java,v $
 * Revision 1.1.1.1  2005/05/17 08:13:59  apiirimets
 * no message
 *
 * Revision 1.2  2005/01/14 08:22:18  andreas
 * Updated to latest version from NIST CVS.
 *
 * Revision 1.2  2004/01/22 13:26:29  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
