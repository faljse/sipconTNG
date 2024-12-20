/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
package gov.nist.javax.sip.address;

import gov.nist.core.*;
import java.util.Iterator;

/**
 * Telephone number class.
 * @version 1.2
 * @version 1.2 $Revision: 1.3 $ $Date: 2006/09/25 07:12:14 $
 *
 * @author M. Ranganathan 
 * 
 */
public class TelephoneNumber extends NetObject {
	public static final String POSTDIAL = ParameterNames.POSTDIAL;
	public static final String PHONE_CONTEXT_TAG =
		ParameterNames.PHONE_CONTEXT_TAG;
	public static final String ISUB = ParameterNames.ISUB;
	public static final String PROVIDER_TAG = ParameterNames.PROVIDER_TAG;

	/** isglobal field
	 */
	protected boolean isglobal;

	/** phoneNumber field
	 */
	protected String phoneNumber;

	/** parmeters list
	 */
	protected NameValueList parms;

	/** Creates new TelephoneNumber */
	public TelephoneNumber() {
		parms = new NameValueList("telparms");
	}

	/** delete the specified parameter.
	 * @param name String to set
	 */
	public void deleteParm(String name) {
		parms.delete(name);
	}

	/** get the PhoneNumber field
	 * @return String
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/** get the PostDial field
	 * @return String
	 */
	public String getPostDial() {
		return (String) parms.getValue(POSTDIAL);
	}

	/**
	 * Get the isdn subaddress for this number.
	 * @return String
	 */
	public String getIsdnSubaddress() {
		return (String) parms.getValue(ISUB);
	}

	/** returns true if th PostDial field exists
	 * @return boolean
	 */
	public boolean hasPostDial() {
		return parms.getValue(POSTDIAL) != null;
	}

	/** return true if this header has parameters.
	 * @param pname String to set
	 * @return boolean
	 */
	public boolean hasParm(String pname) {
		return parms.hasNameValue(pname);
	}

	/**
	 * return true if the isdn subaddress exists.
	 * @return boolean
	 */
	public boolean hasIsdnSubaddress() {
		return hasParm(ISUB);
	}

	/**
	 * is a global telephone number.
	 * @return boolean
	 */
	public boolean isGlobal() {
		return isglobal;
	}

	/** remove the PostDial field
	 */
	public void removePostDial() {
		parms.delete(POSTDIAL);
	}

	/**
	 * Remove the isdn subaddress (if it exists).
	 */
	public void removeIsdnSubaddress() {
		deleteParm(ISUB);
	}

	/**
	 * Set the list of parameters.
	 * @param p NameValueList to set
	 */
	public void setParameters(NameValueList p) {
		parms = p;
	}

	/** set the Global field
	 * @param g boolean to set
	 */
	public void setGlobal(boolean g) {
		isglobal = g;
	}

	/** set the PostDial field
	 * @param p String to set
	 */
	public void setPostDial(String p) {
		NameValue nv = new NameValue(POSTDIAL, p);
		parms.add(nv);
	}

	/** set the specified parameter
	 * @param name String to set
	 * @param value Object to set
	 */
	public void setParm(String name, Object value) {
		NameValue nv = new NameValue(name, value);
		parms.add(nv);
	}

	/**
	 * set the isdn subaddress for this structure.
	 * @param isub String to set
	 */
	public void setIsdnSubaddress(String isub) {
		setParm(ISUB, isub);
	}

	/** set the PhoneNumber field
	 * @param num String to set
	 */
	public void setPhoneNumber(String num) {
		phoneNumber = num;
	}

	public String encode() {
		String retval = "";
		if (isglobal)
			retval += "+";
		retval += phoneNumber;
		if (!parms.isEmpty()) {
			retval += SEMICOLON;
			retval += parms.encode();
		}
		return retval;
	}

	/**
	 * Returns the value of the named parameter, or null if it is not set. A
	 * zero-length String indicates flag parameter.
	 *
	 * @param name name of parameter to retrieve
	 *
	 * @return the value of specified parameter
	 *
	 */
	public String getParameter(String name) {
		Object val = parms.getValue(name);
		if (val == null)
			return null;
		if (val instanceof GenericObject)
			return ((GenericObject) val).encode();
		else
			return val.toString();
	}

	/**
	 *
	 * Returns an Iterator over the names (Strings) of all parameters.
	 *
	 * @return an Iterator over all the parameter names
	 *
	 */
	public Iterator getParameterNames() {
		return this.parms.getNames();
	}

	public void removeParameter(String parameter) {
		this.parms.delete(parameter);
	}

	public void setParameter(String name, String value) {
		NameValue nv = new NameValue(name, value);
		this.parms.add(nv);
	}

	public Object clone() {
		TelephoneNumber retval = (TelephoneNumber) super.clone();
		if (this.parms != null)
			retval.parms = (NameValueList) this.parms.clone();
		return retval;
	}
}
/*
 * $Log: TelephoneNumber.java,v $
 * Revision 1.3  2006/09/25 07:12:14  apiirimets
 * Missed some things in forst commit, here is the complete new version.
 *
 * Revision 1.5  2006/07/13 09:02:25  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  jeroen van bemmel
 * Reviewed by:   mranga
 * Moved some changes from jain-sip-1.2 to java.net
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
 * Revision 1.3  2006/06/19 06:47:26  mranga
 * javadoc fixups
 *
 * Revision 1.2  2006/06/16 15:26:29  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.1.1.1  2005/10/04 17:12:34  mranga
 *
 * Import
 *
 *
 * Revision 1.3  2005/04/16 20:38:47  dmuresan
 * Canonical clone() implementations for the GenericObject and GenericObjectList hierarchies
 *
 * Revision 1.2  2004/01/22 13:26:28  sverker
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
