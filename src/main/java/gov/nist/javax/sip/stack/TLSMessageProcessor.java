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
/* This class is entirely derived from TCPMessageProcessor,
 *  by making some minor changes.
 *
 *               Daniel J. Martinez Manzano <dani@dif.um.es>
 * Acknowledgement: Jeff Keyser suggested that a
 * Stop mechanism be added to this. Niklas Uhrberg suggested that
 * a means to limit the number of simultaneous active connections
 * should be added. Mike Andrews suggested that the thread be
 * accessible so as to implement clean stop using Thread.join().
 *
*/

/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip.stack;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.SocketException;
import gov.nist.core.*;
import java.net.*;
import java.util.*;


/**
 * Sit in a loop waiting for incoming tls connections and start a
 * new thread to handle each new connection. This is the active
 * object that creates new TLS MessageChannels (one for each new
 * accept socket).  
 *
 * @version 1.2 $Revision: 1.3 $ $Date: 2006/09/25 07:12:32 $
 *
 * @author M. Ranganathan   <br/>
 * 
 */
public class TLSMessageProcessor extends MessageProcessor {

	
	protected int nConnections;

	private boolean isRunning;

	private Hashtable tlsMessageChannels;

	private SSLServerSocket sock;

	protected int useCount=0;
	
	/**
	 * The SIP Stack Structure.
	 */
	protected SIPTransactionStack sipStack;

    

	/**
	 * Constructor.
	 * 
	 * @param ipAddress -- inet address where I am listening.
	 * @param sipStack SIPStack structure.
	 * @param port port where this message processor listens.
	 */
	protected TLSMessageProcessor(InetAddress ipAddress, SIPTransactionStack sipStack, int port) {
		super( ipAddress,port,"tls");
	    this.sipStack = sipStack;
		this.tlsMessageChannels = new Hashtable();
		
	}

	/**
	 * Start the processor.
	 */
	public void start() throws IOException {
		Thread thread = new Thread(this);
		thread.setName("TLSMessageProcessorThread");
		thread.setDaemon(true);
		this.sock = sipStack.getNetworkLayer().createSSLServerSocket(this.getPort(), 0, 
		        			this.getIPAddress());
		this.isRunning = true;
		thread.start();

	}

	
	
	/**
	 * Run method for the thread that gets created for each accept
	 * socket.
	 */
	public void run() {
		// Accept new connectins on our socket.
		while (this.isRunning) {
			try {
				synchronized (this)
				{
					// sipStack.maxConnections == -1 means we are
					// willing to handle an "infinite" number of
					// simultaneous connections (no resource limitation).
					// This is the default behavior.
					while (this.isRunning
						&& sipStack.maxConnections != -1
						&& this.nConnections >= sipStack.maxConnections) {
						try {
							this.wait();

							if (!this.isRunning)
								return;
						} catch (InterruptedException ex) {
							break;
						}
					}
					this.nConnections++;
				}

				SSLSocket newsock = (SSLSocket) sock.accept();
				if ( sipStack.logWriter.isLoggingEnabled())
					 sipStack.logWriter.logDebug("Accepting new connection!");
			
				// Note that for an incoming message channel, the
				// thread is already running
				TLSMessageChannel tlsMessageChannel =
					new TLSMessageChannel(newsock, sipStack, this);
			} catch (SocketException ex) {
				this.isRunning = false;
			} catch (IOException ex) {
				// Problem accepting connection.
			    sipStack.logWriter.logError("Problem Accepting Connection",ex);
				continue;
			} catch (Exception ex) {
				sipStack.logWriter.logError("Unexpected Exception!",ex);
			}
		}
	}

	

	

	/**
	 * Returns the stack.
	 * @return my sip stack.
	 */
	public SIPTransactionStack getSIPStack() {
		return sipStack;
	}

	/**
	 * Stop the message processor.
	 * Feature suggested by Jeff Keyser.
	 */
	public synchronized void stop() {
		isRunning = false;
		try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Collection en = tlsMessageChannels.values();
		for ( Iterator it = en.iterator(); it.hasNext(); ) {
		       TLSMessageChannel next = 
				(TLSMessageChannel)it.next() ;
			next.close();
		}
		this.notify();

	}


	protected synchronized void remove
		(TLSMessageChannel tlsMessageChannel) {

		String key = tlsMessageChannel.getKey();
		if (sipStack.isLoggingEnabled()) {
		   sipStack.logWriter.logDebug	
		   ( Thread.currentThread() + " removing " + key);
		}

		/** May have been removed already */
		if (tlsMessageChannels.get(key) == tlsMessageChannel) 
			this.tlsMessageChannels.remove(key);
	}



	public synchronized  
		MessageChannel createMessageChannel(HostPort targetHostPort)
		throws IOException {
		String key = MessageChannel.getKey(targetHostPort,"TLS");
		if (tlsMessageChannels.get(key) != null)  {
			return (TLSMessageChannel) 
			this.tlsMessageChannels.get(key);
		} else {
		     TLSMessageChannel retval = new TLSMessageChannel(
			targetHostPort.getInetAddress(),
			targetHostPort.getPort(),
			sipStack,
			this);
		     this.tlsMessageChannels.put(key,retval);
		     retval.isCached = true;
		     if (sipStack.isLoggingEnabled() ) {
			  sipStack.logWriter.logDebug
				("key " + key);
		          sipStack.logWriter.logDebug("Creating " + retval);
		      }
		     return retval;
		}
	}


	protected synchronized  void cacheMessageChannel 
		(TLSMessageChannel messageChannel) {
		String key = messageChannel.getKey();
		TLSMessageChannel currentChannel = 
			(TLSMessageChannel) tlsMessageChannels.get(key);
		if (currentChannel != null)  {
		        if (sipStack.isLoggingEnabled()) 
				sipStack.logWriter.logDebug("Closing " + key);
			currentChannel.close();
		}
		if (sipStack.isLoggingEnabled()) 
			sipStack.logWriter.logDebug("Caching " + key);
	        this.tlsMessageChannels.put(key,messageChannel);

	}

	public  synchronized MessageChannel 
	       createMessageChannel(InetAddress host, int port)
		throws IOException {
		try {
		   String key = MessageChannel.getKey(host,port,"TLS");
		   if (tlsMessageChannels.get(key) != null)  {
			return (TLSMessageChannel) 
				this.tlsMessageChannels.get(key);
		   } else {
		        TLSMessageChannel retval  = new TLSMessageChannel(host, port, sipStack, this);
			this.tlsMessageChannels.put(key,retval);
		        retval.isCached = true;
			if (sipStack.isLoggingEnabled()) {
		        	sipStack.getLogWriter().logDebug("key " + key);
		        	sipStack.getLogWriter().logDebug("Creating " + retval);
			}
			return retval;
		   }
		} catch (UnknownHostException ex) {
			throw new IOException (ex.getMessage());
		}
	}



	/**
	 * TLS can handle an unlimited number of bytes.
	 */
	public int getMaximumMessageSize() {
		return Integer.MAX_VALUE;
	}


	public boolean inUse() {
		return this.useCount != 0;
	}

	/**
	 * Default target port for TLS
	 */
	public int getDefaultTargetPort() {
		return 5061;
	}

	/**
	 * TLS is a secure protocol.
	 */
	public boolean isSecure() {
		return true;
	}
}
