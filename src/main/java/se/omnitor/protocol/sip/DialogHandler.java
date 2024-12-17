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
package se.omnitor.protocol.sip;

import java.util.Vector;
import java.util.Locale;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import javax.sip.*;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;

// import LogClasses and Classes
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DialogHandler {

    public static final int NOT_STARTED = 0;
    public static final int WAITING_FOR_USER = 1;
    public static final int EXPECTING_ACK = 2;
    public static final int EXPECTING_OK = 3;
    public static final int FINISHED = 4;

    private int state;
    protected SipController sc;
    //protected CallProcessor cp;
    protected Response lastResponse;
    protected ServerTransaction lastServerTransaction;
    protected ClientTransaction lastClientTransaction;
    protected Transaction originalTransaction;
    protected Request originalRequest;

    // declare package and classname
    public final static String CLASS_NAME = DialogHandler.class.getName();
    // get an instance of Logger
    private static Logger logger = Logger.getLogger(CLASS_NAME);


    public DialogHandler(SipController sc) {
	this.sc = sc;

	lastResponse = null;
	state = NOT_STARTED;
	logger = Logger.getLogger("se.omnitor.protocol.sip");
    }

    public abstract void processRequest(Request request,
					   ServerTransaction
					   serverTransaction);

    public abstract void processResponse(Response response);

    public abstract void processTimeout(TimeoutEvent timeoutEvent);

    public Request getRequest() {
        return originalRequest;
    }

    protected int getState() {
	return state;
    }

    protected void setState(int state) {
	this.state = state;
    }

    protected void sendResponse(int responseType, Request request,
				ServerTransaction transaction) {

	Response response = createResponse(responseType, request, null, null);
	if (response != null) {
	    try {
			Vector<String> uaVector = new Vector<String>(0, 1);
			uaVector.add("SIPcon1");
			response.addHeader(sc.headerFactory.createUserAgentHeader(uaVector));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	    sendResponseToTransactionLayer(response, transaction);
	}
    }



    /**
     * @param data The data to send (normally SDP). If this is null, neither
     * Content-Type or data is sent.
     * @param reasonPhrase A custom reason phrase. To use the standard reason
     * phrase, set this to null.
     *
     * @return If this is a 200 OK response to an INVITE, the To header's tag
     * is returned. Else, null is returned.
     */
    protected String sendResponse(int responseType, Request request,
				ServerTransaction transaction,
				Header[] headers,
				ContentTypeHeader contentType, byte[] data,
				  String reasonPhrase) {

	String tag = null;

	Response response =
	    createResponse(responseType, request, contentType, data);

	if (response != null) {
	    if (reasonPhrase != null) {
		try {
		    response.setReasonPhrase(reasonPhrase);
		}
		catch (ParseException pe) {
		    logger.throwing(this.getClass().getName(), "sendResponse",
				    pe);
		}
	    }

	    
	    try {
			Vector<String> uaVector = new Vector<String>(0, 1);
			uaVector.add("SIPcon1");
			response.addHeader(sc.headerFactory.createUserAgentHeader(uaVector));
		} catch (ParseException e) {
			e.printStackTrace();
		}
   
	    
	    for (int cnt=0; cnt<headers.length; cnt++) {
		response.addHeader(headers[cnt]);
	    }

	    // If the response is a 200 OK to an INVITE, create a To tag.
	    if (responseType == 200 && request.getMethod().equals("INVITE")) {
		ToHeader to = (ToHeader)response.getHeader("To");
		try {
		    tag = createTag();
		    to.setTag(tag);
		}
		catch (ParseException pe) {
		    // Ignore this.
		}
	    }

	    sendResponseToTransactionLayer(response, transaction);
	}

	return tag;
    }

    /**
     * @param contentType The Content-Type header. If this is null, neither
     * Content-Type header or data is sent.
     * @param data Packet data. If this is null, neither Content-Type header
     * or data is sent.
     */
    protected ClientTransaction sendRequest(URI requestUri, String method,
					    CallIdHeader callId, long cseq,
					    FromHeader from, ToHeader to,
					    Header[] headers,
					    ContentTypeHeader contentType,
					    byte[] data) {

	try {

            ViaHeader via;
            if (sc.stunIsInUse()) {
                via =
                    sc.headerFactory.createViaHeader(sc.getStunStack().getExternalIp(),
                                                     sc.getStunMappedPort(),
                                                     sc.getTransport(),
                                                     createBranch());

            }
            else {
                via =
                        sc.headerFactory.createViaHeader(sc.getLocalIpAddress(),
                        sc.getLocalPort(),
                        sc.getTransport(),
                        createBranch());
            }
	    Vector<ViaHeader> viaList = new Vector<ViaHeader>(0, 1);
	    viaList.add(via);

	    MaxForwardsHeader maxForwards =
		sc.headerFactory.createMaxForwardsHeader(70);

	    CSeqHeader cSeqHeader =
		sc.headerFactory.createCSeqHeader(cseq, method);

	    Request request;

	    if (contentType != null && data != null) {
		request =
		    sc.messageFactory.createRequest(requestUri, method,
						    callId, cSeqHeader, from,
						    to, viaList, maxForwards,
						    contentType, data);
	    }
	    else {
		request =
		    sc.messageFactory.createRequest(requestUri, method,
						    callId, cSeqHeader, from,
						    to, viaList, maxForwards);
	    }
	    
	    Vector<String> uaVector = new Vector<String>(0, 1);
	    uaVector.add("SIPcon1");
	    request.addHeader(sc.headerFactory.createUserAgentHeader(uaVector));

	    for (int cnt=0; cnt<headers.length; cnt++) {
		request.addHeader(headers[cnt]);
	    }

	    ClientTransaction transaction =
		sc.sipProvider.getNewClientTransaction(request);

	    sendRequestToTransactionLayer(transaction);
	    originalRequest = request;
	    originalTransaction = transaction;

	    return transaction;
	}
	catch (ParseException pe) {
	    logger.throwing(this.getClass().getName(), "sendRequest", pe);
	}
	catch (InvalidArgumentException iae) {
	    logger.throwing(this.getClass().getName(), "sendRequest", iae);
	}
	catch (TransactionUnavailableException tue) {
	    logger.throwing(this.getClass().getName(), "sendRequest", tue);
	}
	catch (SipException se) {
	    logger.throwing(this.getClass().getName(), "sendRequest", se);
	}

	return null;
    }

    public ClientTransaction sendByeRequest(Dialog dialog, URI requestUri, ToHeader to) {

        try {

            Request request = dialog.createRequest("BYE");
            
            // It seems NIST SIP inserts remote SIP address instead of Contact in BYE request URI, let's override NIST SIP.
            request.setRequestURI(requestUri);
            
            // It seems NIST SIP sometimes inserts wrong To tag in BYE packets, let's override NIST SIP.
            //request.setHeader(to);
    	    
            
            try {
				Vector<String> uaVector = new Vector<String>(0, 1);
				uaVector.add("SIPcon1");
				request.addHeader(sc.headerFactory.createUserAgentHeader(uaVector));
			} catch (ParseException e) {
				// Ignore
			}

			AllowHeader ah = getAllowHeader();
			if (ah != null) {
				request.addHeader(ah);
			}
			
    	    ClientTransaction ct = sc.sipProvider.getNewClientTransaction(request);
            dialog.sendRequest(ct);
            return ct;
        } catch (SipException se) {
            se.printStackTrace();
        }

        return null;
    }

    /**
     * @param data The data (normally SDP). If this is null, neither
     * Content-Type or data is sent.
     */
    private Response createResponse(int responseType, Request request,
				    ContentTypeHeader contentType,
				    byte[] data) {
	try {
	    Response response;

	    if (data == null) {
		response =
		    sc.messageFactory.createResponse(responseType, request);
	    }
	    else {
		response =
		    sc.messageFactory.createResponse(responseType, request,
						     contentType, data);
	    }

	    return response;
	}
	catch (ParseException pe) {
	    pe.printStackTrace();
	}

	return null;
    }


    private void sendResponseToTransactionLayer(Response response,
						ServerTransaction
						serverTransaction) {
	logger.finest("Sending response: " + response.toString());

	try {
	    serverTransaction.sendResponse(response);
	    lastResponse = response;
	    lastServerTransaction = serverTransaction;
	}
	catch (SipException se) {
	    se.printStackTrace();
	}
	catch (InvalidArgumentException i) {
            i.printStackTrace();
	}

    }


    private void sendRequestToTransactionLayer(ClientTransaction clientTransaction) {

        // write methodname
        final String METHOD = "sendRequestToTransactionLayer(ClientTransaction clientTransaction)";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD, clientTransaction);

	try {
	    clientTransaction.sendRequest();
	    lastClientTransaction = clientTransaction;
	}
	catch (SipException se) {
	    logger.logp(Level.WARNING, CLASS_NAME, METHOD, "exception thrown when sending request", se);
	}

        logger.exiting(CLASS_NAME, METHOD);
    }


    protected void resendLastResponse() {
	sendResponseToTransactionLayer(lastResponse, lastServerTransaction);
    }

    protected ClientTransaction cancelOriginalRequest() {

	try {
	    Request cancelRequest =
		((ClientTransaction)originalTransaction).createCancel();
	    ClientTransaction ct =
		sc.sipProvider.getNewClientTransaction(cancelRequest);
	    ct.sendRequest();

	    return ct;
	}
	catch (SipException se) {
	    logger.throwing(this.getClass().getName(),
			    "cancelOriginalTransaction",
			    se);
	}

	return null;

	/*


	try {
	    URI requestUri = originalRequest.getRequestURI();
	    CallIdHeader callId =
		(CallIdHeader)originalRequest.getHeader("Call-ID");
	    CSeqHeader cSeqHeader =
		sc.headerFactory.createCSeqHeader(((CSeqHeader)originalRequest.
						   getHeader("CSeq")).
						  getSequenceNumber(),
						  "CANCEL");
	    FromHeader from = (FromHeader)originalRequest.getHeader("From");
	    ToHeader to = (ToHeader)originalRequest.getHeader("To");
	    Vector viaList = new Vector(0, 1);
	    viaList.add(originalRequest.getHeader("Via"));

	    MaxForwardsHeader maxForwards =
		sc.headerFactory.createMaxForwardsHeader(70);

	    Request request;

	    request =
		sc.messageFactory.createRequest(requestUri, "CANCEL",
						callId, cSeqHeader, from,
						to, viaList, maxForwards);

	    ClientTransaction transaction =
		sc.sipProvider.getNewClientTransaction(request);

	    sendRequestToTransactionLayer(transaction);

	    return transaction;
	}
	catch (ParseException pe) {
	    pe.printStackTrace();
	}
	catch (InvalidArgumentException iae) {
	    iae.printStackTrace();
	}
	catch (TransactionUnavailableException tue) {
	    tue.printStackTrace();
	}

	return null;
	*/
    }


    protected void resendLastRequest() {
	sendRequestToTransactionLayer(lastClientTransaction);
    }

    private String createBranch() {
	String branch = "z9hG4bK" + System.currentTimeMillis();

	return branch;
    }

    private String createTag() {
	return "" + (Math.random() * Integer.MAX_VALUE);
    }



    /**
     * @param requestNbr The first request should have nbr 1, the second
     * nbr 2 and so on. This is used by remote to detect resent requests.
     * @param firstTry This indicates whether this authorization is the first
     * try or not. Even when we are unREGISTERing, we want to know if this
     * authorization is the first try or not. Since an unREGISTER has a
     * requestNbr > 1, we cannot use requestNbr to determine whether this
     * request is first or not. Therefore, we use this parameter instead.
     */
    protected AuthorizationHeader createAuthorizationHeader(
    		Response responseMessage,
    		String method,
    		URI uri,
    		int requestNbr,
    		String username,
    		String password,
    		boolean firstTry) {

    	WWWAuthenticateHeader wwwAuthHeader =
    		(WWWAuthenticateHeader)responseMessage.
    		getHeader("WWW-Authenticate");

    	if (!firstTry && wwwAuthHeader.isStale()) {

    		// This indicates that the username and password was wrong, even
    		// after a second try.
    		return null;

    	}
    	else if (!wwwAuthHeader.getScheme().equals("Digest")) {

    		// We do not support anything but digest authentication
    		return null;

    	}

    	AuthorizationHeader authHeader;
    	try {
    		authHeader =
    			sc.headerFactory.createAuthorizationHeader("Digest");

    		createAuthorizationResponse(
    				authHeader, 
    				responseMessage, 
    				method, 
    				uri, 
    				requestNbr, 
    				username, 
    				password, 
    				firstTry,
    				wwwAuthHeader.getRealm(),
    				wwwAuthHeader.getNonce(),
    				wwwAuthHeader.getOpaque(),
    				wwwAuthHeader.getQop(),
    				wwwAuthHeader.getAlgorithm());

    		return authHeader;

    	} catch (ParseException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    		logger.throwing(this.getClass().getName(), "createAuthorizationHeader", e);
    	}

    	return null;
    }

	
	private void createAuthorizationResponse(
			AuthorizationHeader authHeader,
			Response responseMessage,
			String method,
			URI uri,
			int requestNbr,
			String username,
			String password,
			boolean firstTry,
			String realm,
			String nonce,
			String opaque,
			String qop,
			String algorithm) {
		
	
	String response = "";


	String cnonce =
	    Integer.toHexString((int)(Math.random() * Integer.MAX_VALUE));

	String a1 = "";
	if (algorithm != null && algorithm.toUpperCase(Locale.US).equals("MD5-SESS")) {
	    a1 = md5(username + ":" + realm + ":" + password) + ":" +
		nonce + ":" + cnonce;
	}
	else {

	    // In any other case, use MD5.
	    algorithm = "MD5";
	    a1 = username + ":" + realm + ":" + password;
	}

	String a2 = "";
	if (qop != null && qop.toUpperCase().equals("AUTH-INT")) {
	    a2 = method + ":" + uri.toString() + ":" +
		md5("entity-body here");
	}
	else {

	    // In any other case, assume that qop = "auth"
	    a2 = method + ":" + uri.toString();
	}


	String ncStr = Integer.toHexString(requestNbr);
	int diff = 8 - ncStr.length();
	if (diff > 0) {
	    for (int cnt=0; cnt<diff; cnt++) {
		ncStr = "0" + ncStr;
	    }
	}


	if (qop != null) {

	    if (qop.toUpperCase().equals("AUTH") ||
		qop.toUpperCase().equals("AUTH-INT")) {

		response = md5(md5(a1) + ":" +
			       nonce + ":" +
			       ncStr + ":" +
			       cnonce + ":" +
			       qop + ":" +
			       md5(a2));
	    }
	}
	else {

	    response = md5(md5(a1) + ":" + nonce + ":" + md5(a2));

	}


	try {
	    if (opaque != null) {
	    	authHeader.setOpaque(opaque);
	    }
	    authHeader.setRealm(realm);
	    authHeader.setNonce(nonce);
	    authHeader.setResponse(response);
	    authHeader.setUsername(username);
	    authHeader.setAlgorithm(algorithm);

	    if (qop != null) {
		authHeader.setQop(qop);
		authHeader.setCNonce(cnonce);
		authHeader.setNonceCount(requestNbr);
	    }
	}
	catch (ParseException pe) {
	    return;
	}

	authHeader.setURI(uri);
    }

    /**
     * @param requestNbr The first request should have nbr 1, the second
     * nbr 2 and so on. This is used by remote to detect resent requests.
     * @param firstTry This indicates whether this authorization is the first
     * try or not. Even when we are unREGISTERing, we want to know if this
     * authorization is the first try or not. Since an unREGISTER has a
     * requestNbr > 1, we cannot use requestNbr to determine whether this
     * request is first or not. Therefore, we use this parameter instead.
     */
    protected AuthorizationHeader createProxyAuthorizationHeader
	(Response responseMessage,
	 String method,
	 URI uri,
	 int requestNbr,
	 String username,
	 String password,
	 boolean firstTry) {

	ProxyAuthenticateHeader proxyAuthHeader =
	    (ProxyAuthenticateHeader)responseMessage.
	    getHeader("Proxy-Authenticate");

	if (!firstTry && proxyAuthHeader.isStale()) {

	    // This indicates that the username and password was wrong, even
	    // after a second try.
	    return null;

	}
	else if (!proxyAuthHeader.getScheme().equals("Digest")) {

	    // We do not support anything but digest authentication
	    return null;

	}

	AuthorizationHeader authHeader;
	try {
	    authHeader =
			sc.headerFactory.createProxyAuthorizationHeader("Digest");

		createAuthorizationResponse(
				authHeader, 
				responseMessage, 
				method, 
				uri, 
				requestNbr, 
				username, 
				password, 
				firstTry,
				proxyAuthHeader.getRealm(),
				proxyAuthHeader.getNonce(),
				proxyAuthHeader.getOpaque(),
				proxyAuthHeader.getQop(),
				proxyAuthHeader.getAlgorithm());

		return authHeader;

	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		logger.throwing(this.getClass().getName(), "createAuthorizationHeader", e);
	}

	return null;
    }
    

/*
	String realm = proxyAuthHeader.getRealm();
	String nonce = proxyAuthHeader.getNonce();
	String opaque = proxyAuthHeader.getOpaque();
	String qop = proxyAuthHeader.getQop();
	String response = "";
	String algorithm = proxyAuthHeader.getAlgorithm();



	String cnonce =
	    Integer.toHexString((int)(Math.random() * Integer.MAX_VALUE));

	String a1 = "";
	if (algorithm != null && algorithm.toUpperCase(Locale.US).equals("MD5-SESS")) {
	    a1 = md5(username + ":" + realm + ":" + password) + ":" +
		nonce + ":" + cnonce;
	}
	else {

	    // In any other case, use MD5.
	    algorithm = "MD5";
	    a1 = username + ":" + realm + ":" + password;
	}

	String a2 = "";
        if (qop != null) {
	if (qop.toUpperCase().equals("AUTH-INT")) {
	    a2 = method + ":" + uri.toString() + ":" +
		md5("entity-body here");
	}
	else {

	    // In any other case, assume that qop = "auth"
	    a2 = method + ":" + uri.toString();
	}
        }

	String ncStr = Integer.toHexString(requestNbr);
	int diff = 8 - ncStr.length();
	if (diff > 0) {
	    for (int cnt=0; cnt<diff; cnt++) {
		ncStr = "0" + ncStr;
	    }
	}


	if (qop != null) {

	    if (qop.toUpperCase().equals("AUTH") ||
		qop.toUpperCase().equals("AUTH-INT")) {

		response = md5(md5(a1) + ":" +
			       nonce + ":" +
			       ncStr + ":" +
			       cnonce + ":" +
			       qop + ":" +
			       md5(a2));
	    }
	}
	else {

	    response = md5(md5(a1) + ":" + nonce + ":" + md5(a2));

	}


	try {
	    if (opaque != null) {
		authHeader.setOpaque(opaque);
	    }
	    authHeader.setRealm(realm);
	    authHeader.setNonce(nonce);
	    authHeader.setResponse(response);
	    authHeader.setUsername(username);
	    authHeader.setAlgorithm(algorithm);

	    if (qop != null) {
		authHeader.setQop(qop);
		authHeader.setCNonce(cnonce);
		authHeader.setNonceCount(requestNbr);
	    }
	}
	catch (ParseException pe) {
	    //DEBUG
	    pe.printStackTrace();
	    return null;
	}

	authHeader.setURI(uri);

	System.out.println("Auth: " +  authHeader.toString());
	
	return authHeader;
    }
*/
    
    /**
     * Creates a MD5 encoded string of the given plaintext data.
     *
     * @param plaintextData The data to encode with MD5
     *
     * @return The MD5 encoded string
     */
    public static String md5(String plaintextData) {
	try {
	    MessageDigest md5 = MessageDigest.getInstance("MD5");
	    byte[] md5digest = md5.digest(plaintextData.getBytes());
	    StringBuffer sb = new StringBuffer();
	    for (int cnt=0; cnt<md5digest.length; cnt++) {
		sb.append((Integer.toHexString
			   ((md5digest[cnt] & 0xFF | 0x100)).
			   substring(1, 3)));
	    }
	    return sb.toString();
	}
	catch (NoSuchAlgorithmException e) {
	    return "";
	}
    }


    /**
     * Creates an Allow header with all capabilities of this SIP kernel.
     *
     * @return An Allow header with all capabilities of this SIP kernel
     */
    public AllowHeader getAllowHeader() {

	try {
	    return
		sc.headerFactory.createAllowHeader("INVITE, ACK, CANCEL, " +
						   "BYE, REFER, NOTIFY, OPTIONS");
	}
	catch (ParseException pe) {
	    return null;
	}

    }

}
