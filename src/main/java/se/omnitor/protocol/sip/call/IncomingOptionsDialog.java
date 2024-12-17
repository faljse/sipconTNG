/*
 * Open Source Exemplar Software
 *
 * Copyright (C) 2004-2008 University of Wisconsin (Trace R&D Center)
 * Copyright (C) 2004-2008 Omnitor AB
 *
 * This reference design was developed under funding from the National
 * Institute on Disability and Rehabilitation Research US Dept of Education
 * and the European Commission.
 *
 * This piece of software is a part of a package that was developed as a joint
 * effort of Omnitor AB and the Trace Center - University of Wisconsin and is
 * released to the public domain with only the following restrictions:
 *
 * 1) That the following acknowledgement be included in the source code and
 * documentation for the program or package that use this code
 *
 * "Parts of this program were based on reference designs developed by
 * Omnitor AB and the Trace Center, University of Wisconsin-Madison under
 * funding from the National Institute on Disability and Rehabilitation
 * Research US Dept of Education and the European Commission."
 *
 * 2) That this program not be modified unless it is plainly marked as
 * modified from the original distributed by Trace/Omnitor.
 *
 * (NOTE: This release applies only to the files that contain this notice -
 * not necesarily to any other code or libraries associated with this file.
 * Please check individual files and libraries for the rights to use each)
 *
 * THIS PIECE OF THE SOFTWARE PACKAGE IS EXPERIMENTAL/DEMONSTRATION IN NATURE.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF THIRD PARTY RIGHTS.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR HOLDERS INCLUDED IN THIS NOTICE
 * BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL INDIRECT OR CONSEQUENTIAL DAMAGES,
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 *
 */
package se.omnitor.protocol.sip.call;

import java.text.ParseException;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.header.AllowHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import se.omnitor.protocol.sip.DialogHandler;
import se.omnitor.protocol.sip.SipController;
import se.omnitor.protocol.sip.call.CallProcessor;

import java.util.logging.Logger;

/**
 * @author Erik Zetterstrom, Omnitor AB
 *
 * TODO: Answer OPTIONS within a dialog with 200 OK
 * TODO: Add support for accept-encoding, accept-language and supported headers.
 */
public class IncomingOptionsDialog extends DialogHandler {

    	private Logger logger;
	private Request request;
	private ServerTransaction serverTransaction;
	private SipController sc;

     /**
      * Initializes.
      *
      * @param sc The Sipontroller that handles this SIP session
      * @param cp The CallProcessor that handles this call
      */
    	public IncomingOptionsDialog(SipController sc, CallProcessor cp) {
		super(sc);

		this.sc = sc;

		logger = Logger.getLogger("se.omnitor.protocol.sip.call");		
    	}

     /**
     	* Process an incoming OPTIONS
     	*
     	* @param request The OPTIONS request
     	* @param serverTransaction The server transaction connected to the request
     	*
      * TODO: answer with SDP, accept
     	*/
    	public synchronized void processRequest(Request request,
					    ServerTransaction
					    serverTransaction) {
		String method = request.getMethod();
		this.request=request;
		this.serverTransaction=serverTransaction;

		if (method.equals("OPTIONS")) {
			sc.signalIncomingOptions(this);
		}
		else {
			//Serious error, ignore
		}
	}

     /**
      * Callback from the SipControllerListener to signal the state of the client.
	*
	* @param busy If true the client is busy.
	*/
	public void sendResponse(boolean busy, String sdp) { 

		ContentTypeHeader contentTypeHeader = null;

		Header incomingAcceptHeader = request.getHeader("Accept");

		
		String incomingAcceptHeaderString = "" + incomingAcceptHeader;

		//If there is no request for sdp, do not send sdp.
		if(incomingAcceptHeaderString.indexOf("application/sdp")==-1) {
			sdp=null;
		}
		else {
			// Create Content-Type header
			try {
	    			contentTypeHeader = sc.headerFactory.createContentTypeHeader("application","sdp");
			}
			catch (ParseException pe) {
		    		logger.throwing(this.getClass().getName(), "options", pe);
			}
		}
		
		// Create allow header
		AllowHeader aHeader = getAllowHeader();

		Header[] headers = new Header[] { aHeader };

		byte[] sdpBytes = null;
		if(sdp!=null) {
			sdpBytes = sdp.getBytes();
		}

		//Send NOT ACCEPTABLE if all incoming accept types are unrecognized.
		if(incomingAcceptHeader==null ||
		   incomingAcceptHeaderString.indexOf("application/sdp")!=-1) {
			if(!busy) {
				sendResponse(Response.OK, 
                  	             request, 
                        	       serverTransaction,
                              	 headers,
                              	 contentTypeHeader,
                              	 sdpBytes,
                              	 null);
			}
			else {
		     		sendResponse(Response.BUSY_HERE, 
                  	             request, 
                        	       serverTransaction,
                              	 headers,
                             	       contentTypeHeader,
	                               sdpBytes,
      	                         null);
			}
		}
		else {
			sendResponse(Response.NOT_ACCEPTABLE, 
                 	             request, 
                       	       serverTransaction,
                             	 headers,
                             	 contentTypeHeader,
                             	 sdpBytes,
                             	 null);
		}
	}

     /**
      * Process a response.
      *
      * @param response The response to process
      */
     	public void processResponse(Response response) {
     	}

     /**
     	* Process a timeout event from the SIP kernel.
     	*
     	* @param timeoutEvent The timeout event
     	*/
    	public void processTimeout(TimeoutEvent timeoutEvent) {
	}	

}
		

