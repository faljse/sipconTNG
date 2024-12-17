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
/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
*******************************************************************************/
package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.message.*;
import java.text.ParseException;

/**
 * A listener interface that enables customization of parse error handling.
 * An class that implements this interface is registered with the 
 * parser and is called back from the parser handle parse errors.
 *
 * @version 1.2 $Revision: 1.3 $ $Date: 2006/09/25 07:12:23 $
 */
public interface ParseExceptionListener {
	/**
	 * This gets called from the parser when a parse error is generated.
	 * The handler is supposed to introspect on the error class and 
	 * header name to handle the error appropriately. The error can
	 * be handled by :
	 *<ul>
	 * <li>1. Re-throwing an exception and aborting the parse.
	 * <li>2. Ignoring the header (attach the unparseable header to
	 * the SIPMessage being parsed).
	 * <li>3. Re-Parsing the bad header and adding it to the sipMessage
	 * </ul>
	 *
	 * @param  ex - parse exception being processed.
	 * @param  sipMessage -- sip message being processed.
	 * @param headerText --  header/RL/SL text being parsed.
	 * @param messageText -- message where this header was detected.
	 */
	public void handleException(
		ParseException ex,
		SIPMessage sipMessage,
		Class headerClass,
		String headerText,
		String messageText)
		throws ParseException;
}
/*
 * $Log: ParseExceptionListener.java,v $
 * Revision 1.3  2006/09/25 07:12:23  apiirimets
 * Missed some things in forst commit, here is the complete new version.
 *
 * Revision 1.6  2006/07/13 09:01:55  mranga
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
 * Revision 1.3  2006/06/19 06:47:27  mranga
 * javadoc fixups
 *
 * Revision 1.2  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.1.1.1  2005/10/04 17:12:35  mranga
 *
 * Import
 *
 *
 * Revision 1.4  2004/01/22 13:26:31  sverker
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
