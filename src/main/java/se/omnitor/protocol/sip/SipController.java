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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.Properties;
import java.util.TooManyListenersException;
import java.text.ParseException;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.SipListener;
import javax.sip.SipStack;
import javax.sip.SipProvider;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.SipFactory;
import javax.sip.PeerUnavailableException;
import javax.sip.ListeningPoint;
import javax.sip.TransportNotSupportedException;
import javax.sip.ObjectInUseException;
import javax.sip.ServerTransaction;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionUnavailableException;
import javax.sip.message.MessageFactory;
import javax.sip.message.Response;
import javax.sip.message.Request;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ContactHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.address.SipURI;
import javax.sip.address.Address;
import se.omnitor.protocol.sip.call.IncomingCallDialog;
import se.omnitor.protocol.sip.call.IncomingOptionsDialog;
import se.omnitor.protocol.sip.call.OutgoingCallDialog;
import se.omnitor.protocol.sip.call.CallProcessor;
import se.omnitor.protocol.sip.call.IncomingReferDialog;
import se.omnitor.protocol.sip.register.RegisterDialog;
import se.omnitor.protocol.sip.register.RegisterProcessor;
import se.omnitor.protocol.sip.event.EventManager;
import se.omnitor.protocol.stun.StunStack;
import se.omnitor.protocol.stun.StunStackException;

// import LogClasses and Classes
import java.util.logging.Logger;

/**
 * @author Andreas Piirimets, Omnitor AB
 */
public class SipController implements SipListener {

	String dnsServer = null;
	
    /**
     * The version of this SIP controller
     */
    public static final String VERSION = "v1.1";

    /**
     * Defines any port
     */
    public static final int ANY = 0;

    /**
     * Defines UDP transport
     */
    public static final int UDP = 1;

    /**
     * Defines TCP transport
     */
    public static final int TCP = 2;

    SipStack sipStack;
    ListeningPoint listeningPoint;
    public SipProvider sipProvider;
    public MessageFactory messageFactory;
    public HeaderFactory headerFactory;
    public AddressFactory addressFactory;
    private String fullName;
    private String primarySipAddress;
    private String outboundProxy;

    public EventManager eventManager;
    private Properties properties;

    int localPort; // The local port for SIP (not the STUN mapped one)
    int stunMappedPort;
    int originalPort; // This is the port that the user at first time requested to use
    String transport;
    String localIpAddress; // The local IP (not the STUN mapped one)
    StunStack stunStack;

    Hashtable<String, Processor> processorTable;
    Hashtable<Transaction, DialogHandler> dialogTable;

    SipControllerListener listener;

    private AuthInfo[] authInfo;
    private FromAddress[] fromAddressList;

    private Vector<RegisterProcessor> registerProcessorList;

    // declare package and classname
    public final static String CLASS_NAME = SipController.class.getName();
    // get an instance of Logger
    private static Logger logger = Logger.getLogger(CLASS_NAME);


    /**
     * Initializes.
     *
     * @param listener The listener class, which should receive events from
     * this SIP controller.
     *
     * @throws UnknownHostException If the local IP address cannot be resolved.
     */
    public SipController(SipControllerListener listener) throws
            UnknownHostException, PeerUnavailableException {

        // write methodname
        final String METHOD = "SipController(SipControllerListener listener)";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD, listener);

        startSipController(listener, getSystemIpAddress(), null, ANY, null);

        logger.exiting(CLASS_NAME, METHOD);
    }

    /**
     * Initializes.
     *
     * @param listener The listener class, which should receive events from
     * this SIP controller.
     * @param ipAddress The IP address to bind this SIP controller to.
     */
    public SipController(SipControllerListener listener, String ipAddress) throws PeerUnavailableException {
        // write methodname
        final String METHOD =
                "SipController(SipControllerListener listener, String ipAddress)";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD, new Object[] {listener, ipAddress});

        startSipController(listener, ipAddress, null, ANY, null);

        logger.exiting(CLASS_NAME, METHOD);
    }

    /**
     * Initializes.
     *
     * @param listener The listener class, which should receive events from
     * this SIP controller.
     * @param ipAddress The IP address to bind this SIP controller to.
     * @param outboundProxy The outbound proxy to use for all SIP packets.
     */
    public SipController(SipControllerListener listener, String ipAddress,
                         String outboundProxy) throws PeerUnavailableException {
        // write methodname
        final String METHOD = "SipController(SipControllerListener listener, String ipAddress, String outboundProxy)";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD, new Object[] {listener, ipAddress,
                        outboundProxy});

        startSipController(listener, ipAddress, outboundProxy, ANY, null);

        logger.exiting(CLASS_NAME, METHOD);
    }

    /**
     * Initializes.
     *
     * @param listener The listener class, which should receive events from
     * this SIP controller.
     * @param ipAddress The IP address to bind this SIP controller to.
     * @param outboundProxy The outbound proxy to use for all SIP packets.
     * @param stunServerAddress The address to a STUN server
     */
    public SipController(SipControllerListener listener, String ipAddress,
                         String outboundProxy, StunStack stunStack) throws PeerUnavailableException {
        // write methodname
        final String METHOD =
                "SipController(SipControllerListener listener, String ipAddress, String outboundProxy, StunStack stunStack)";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD, new Object[] {listener, ipAddress,
                        outboundProxy, stunStack});

        startSipController(listener, ipAddress, outboundProxy, ANY, stunStack);

        logger.exiting(CLASS_NAME, METHOD);
    }

    /**
     * Initializes
     *
     * @param listener The listener class, which should receive events from
     * this SIP controller.
     * @param ipAddress The IP address to bind this SIP controller to.
     * @param outboundProxy The outbound proxy to use
     * @param port The port to start the SIP proxy on
     */
    public SipController(SipControllerListener listener, String ipAddress,
                         String outboundProxy, int port) throws PeerUnavailableException {
        // write methodname
        final String METHOD = "SipController(SipControllerListener listener, String ipAddress, String outboundProxy, int port)";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD, new Object[] {listener, ipAddress,
                        outboundProxy});

        startSipController(listener, ipAddress, outboundProxy, port, null);

        logger.exiting(CLASS_NAME, METHOD);
    }

    /**
     * Inits the SIP controller.
     *
     * @param listener The listener class, which should receive events from
     * this SIP controller.
     * @param ipAddress The IP address to bind this SIP controller to.
     * @param outboundProxy The outbound proxy to use
     * @param desiredPort The port to start the SIP proxy on
     * @param stunServerAddress The address to a STUN server, set to null to
     * inactivate STUN
     */
    public void startSipController(SipControllerListener listener,
                                   String ipAddress,
                                   String outboundProxy, int desiredPort,
                                   StunStack stunStack) throws PeerUnavailableException {

        init(listener, ipAddress, outboundProxy, desiredPort, stunStack);
    }

    /**
     * Inits everything
     *
     * @param listener The listener class, which should receive events from
     * this SIP controller.
     * @param ipAddress The IP address to bind this SIP controller to.
     * @param outboundProxy The outbound proxy to use
     * @param desiredPort The port to start the SIP proxy on
     * @param stunServerAddress The address to a STUN server, set to null to
     * inactivate STUN
     */
    private void init(SipControllerListener listener, String ipAddress,
                      String outboundProxy, int desiredPort,
                      StunStack stunStack) throws PeerUnavailableException {

        // write methodname
        final String METHOD = "init(SipControllerListener listener, String ipAddress, String outboundProxy, int desiredPort)";
        //log when entering a method
        logger.entering(CLASS_NAME, METHOD, new Object[] {listener, ipAddress,
                        outboundProxy});

        registerProcessorList = new Vector<RegisterProcessor>(0, 1);

        originalPort = desiredPort;
        transport = "udp";
        primarySipAddress = null;
        authInfo = null;

        this.listener = listener;
        this.localIpAddress = ipAddress;
        this.outboundProxy = outboundProxy;
        this.stunStack = stunStack;

        processorTable = new Hashtable<String, Processor>();
        dialogTable = new Hashtable<Transaction, DialogHandler>();

        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.resetFactory();
        sipFactory.setPathName("gov.nist");

        properties = new Properties();
        properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "ON");

        properties.setProperty("javax.sip.IP_ADDRESS", ipAddress);
        properties.setProperty("javax.sip.STACK_NAME", "Omnitor_" + VERSION);
        /*
          properties.setProperty("javax.sip.OUTBOUND_PROXY",
                 ipAddress + "/" + transport);
         */
        properties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
                               "false");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                               "nistSipDebug.log");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                               "nistSipServer.log");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "on");
        properties.setProperty("javax.sip.ROUTER_PATH",
                               "se.omnitor.tipcon1.sip.SipRouter");
        if (outboundProxy != null && !outboundProxy.equals("")) {
            properties.setProperty("javax.sip.OUTBOUND_PROXY",
                                   outboundProxy + "/" + transport);

            logger.finest("Using outbound proxy: " + outboundProxy +
                          "/" + transport);
        }

        else {
            logger.finest("Not using outbound proxy!");
        }

        sipStack = sipFactory.createSipStack(properties);
        messageFactory = sipFactory.createMessageFactory();
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();

        eventManager = new EventManager();

        logger.exiting(CLASS_NAME, METHOD);
    }

    /**
     * Empty function implements abstract function in interface SipListener
     *
     */
    /*public void processDialogTerminated(javax.sip.DialogTerminatedEvent dte) {
       System.out.println("inne i ny funktion" +dte);
      }*/

    /**
     * Empty function implements abstract function in interface SipListener
     *
     */
    /*public void processTransactionTerminated(javax.sip.TransactionTerminatedEvent tte) {
       System.out.println("inne i ny funktion" +tte);
     }*/

    /**
     * Empty function implements abstract function in interface SipListener
     *
     */
    /*public void processIOException(javax.sip.IOExceptionEvent ioee) {
     System.out.println("inne i ny funktion" +ioee);
      }*/


    /**
     * Starts the SIP controller. Binds the port and starts listening.
     *
     * @throws InvalidArgumentException If the port cannot be bound. If ANY
     * is chosen as port, this exception is thrown if no port can be bound.
     */
    public void start() throws InvalidArgumentException {
        listeningPoint = null;

        startListeningPoint();

        logger.fine("SIP kernel started.");
    }

    /**
     * Stops the SIP kernel
     *
     */
    public void stop() {
        stopListeningPoint();
    }

    /**
     * Stops all listening points.
     *
     */
    private void stopListeningPoint() {
        Iterator it;

        it = sipStack.getListeningPoints();
        while (it.hasNext()) {
            try {
                sipStack.deleteListeningPoint((ListeningPoint) it.next());
            } catch (ObjectInUseException e) {
                logger.throwing(CLASS_NAME, "stop", e);
            }
        }

        /*
          it = sipStack.getSipProviders();
          while (it.hasNext()) {
            try {
         sipStack.deleteSipProvider((SipProvider)it.next());
            }
            catch (ObjectInUseException e) {
         logger.throwing(this.getClass().getName(), "stop", e);
            }
          }
         */

    }

    /**
     * Starts listening point
     *
     * @param port int
     */
    private void startListeningPoint() throws InvalidArgumentException {

        if (originalPort == ANY) {
            boolean bound = false;
            int tempPort = 5060;
            InetAddress ia = null;
            try {
                ia = InetAddress.getByName(localIpAddress);
            } catch (UnknownHostException uhe) {
                logger.severe("ERROR: Cannot use local IP address!");
                uhe.printStackTrace();
            } while (!bound) {
                try {
                    DatagramSocket ds = new DatagramSocket(tempPort, ia);
                    ds.close();
                    bound = true;
                    localPort = tempPort;

                } catch (SocketException se) {
                    // Could not bind, try another port
                    tempPort++;
                }
            }
        } else {
            localPort = originalPort;
        }

        if (stunStack != null) {
            try {
                stunMappedPort = stunStack.getMappedPort(localPort);
            } catch (StunStackException sse) {
                sse.printStackTrace();
            }
        }

        try {
            listeningPoint =
                    sipStack.createListeningPoint(localIpAddress, localPort, transport);

        } catch (InvalidArgumentException iae) {
            throw iae;
        } catch (TransportNotSupportedException tnse) {
            logger.severe("Transport " + transport + " is not supported!");
            throw new RuntimeException(tnse.getMessage());
        }

        if (listeningPoint != null) {
            try {
                if (stunStack != null) {
                    listeningPoint.setSentBy(stunStack.getExternalIp() + ":" + getStunMappedPort());
                }
                sipProvider = sipStack.createSipProvider(listeningPoint);
                sipProvider.addSipListener(this);
            } catch (ObjectInUseException oiue) {
                throw new RuntimeException(oiue.getMessage());
            } catch (TooManyListenersException tmle) {
                throw new RuntimeException(tmle.getMessage());
            } catch (ParseException pe) {
                throw new RuntimeException(pe.getMessage());
            }
        }
    }

    /**
     * Set the port for the SIP controller. In order to apply the change,
     * stop and start the SIP controller.
     *
     * @param port The new port number
     */
    public void setPort(int port) {
        this.originalPort = port;
    }

    /**
     * Sets the transport for the SIP controller, this should be one of the
     * constants in this class. Apply the changes by restart (stop and start)
     * the SIP controller.
     *
     * @param transport The transport to use, expressed as one of the constants
     * in this class.
     */
    public void setTransport(int transport) {
        if (transport == UDP) {
            this.transport = "udp";
        } else if (transport == TCP) {
            this.transport = "tcp";
        }
    }

    /**
     * Gets the system's IP address
     *
     * @return The system's IP address
     */
    private String getSystemIpAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    /**
     * Processes an incoming request
     *
     * @param requestEvent The incoming request
     */
    public void processRequest(RequestEvent requestEvent) {
        MessageEventHandler meh = new MessageEventHandler(this, requestEvent);
        Thread t = new Thread(meh, "Process incoming request: " + requestEvent.getRequest().getMethod());
        t.start();
    }

    /**
     * Processes and incoming response
     *
     * @param responseEvent The incoming response
     */
    public void processResponse(ResponseEvent responseEvent) {
        logger.finest(responseEvent.getResponse().toString());

        MessageEventHandler meh = new MessageEventHandler(this, responseEvent);
        Thread t = new Thread(meh, "Process incoming response: " + responseEvent.getResponse().getStatusCode());
        t.start();
    }

    /**
     * Processes a SIP kernel timeout.
     *
     * @param timeoutEvent The timeout.
     */
    public void processTimeout(TimeoutEvent timeoutEvent) {
        Transaction t;
        if (timeoutEvent.isServerTransaction()) {
            t = timeoutEvent.getServerTransaction();
        } else {
            t = timeoutEvent.getClientTransaction();
        }

        DialogHandler d = getDialogHandler(t);
        if (d != null) {
            d.processTimeout(timeoutEvent);
        } else {
            try {
                getProcessor(t.getDialog().getCallId().getCallId()).
                        processTimeout(timeoutEvent);
            } catch (NullPointerException npe) {
                // Cannot find dialog, just ignore.
            }
        }
    }

    /**
     * Signalls that an incoming call has arrived.
     *
     * @param dialog The call dialog connected to this call.
     */
    public void signalIncomingCall(IncomingCallDialog dialog) {
        logger.finest("Signalling incoming call!");

        listener.signalIncomingCall(dialog);
    }

    /**
     * Gets the processor that is connected to a call-id.
     *
     * @param callId The call-id
     * @return The processor connected to the given call-id.
     */
    private Processor getProcessor(String callId) {
        return (Processor) processorTable.get(callId);
    }

    /**
     * Adds a processor to the list and binds it to a call-id.
     *
     * @param callId The call-id
     * @param p The processor
     */
    private void addProcessor(String callId, Processor p) {
        processorTable.put(callId, p);
    }

    /**
     * Gets the dialog that is connected to a transaction.
     *
     */
    private DialogHandler getDialogHandler(Transaction transaction) {
        return (DialogHandler) dialogTable.get(transaction);
    }

    /**
     * Adds a dialog to the list of dialogs, and binds it to a transaction.
     *
     * @param transaction The transaction to add
     * @param dialog The dialog to associate with the transaction
     */
    public void addDialogHandler(Transaction transaction,
                                 DialogHandler dialog) {
        dialogTable.put(transaction, dialog);
    }

    /**
     * Removes a dialog handler from the list of dialogs.
     *
     * @param transaction The transaction to remove
     */
    public void removeDialogHandler(Transaction transaction) {
        dialogTable.remove(transaction);
    }

    /**
     * Gets the local IP address, which the SIP controller uses.
     *
     * @return The SIP controller's IP address
     */
    public String getLocalIpAddress() {
        return localIpAddress;
    }

    /**
     * Gets the port that the SIP controller uses.
     *
     * @return The SIP controller's port.
     */
    public int getLocalPort() {
        return localPort;
    }

    public int getStunMappedPort() {
        return stunMappedPort;
    }

    public StunStack getStunStack() {
        return stunStack;
    }

    /**
     * Gets the transport, that the SIP controller uses.
     *
     * @return The transport.
     */
    protected String getTransport() {
        return transport;
    }

    /**
     * Register SIP controller to a registrar.
     *
     * @param sipAddress The SIP address to register
     * @param registrarHost The registrar host name, either an IP address or
     * a domain name.
     * @param username The username for the registration, used in
     * authentication.
     * @param password The password for the username of the registration, used
     * in authentication.
     *
     * @return The processor associated with the current registration.
     */
    public RegisterProcessor register(String sipAddress, String registrarHost,
                                      String username, String password) {

        // Force NIST SIP to make a new STUN test
        /*
                 if (!isRegistered()) {
            try {
                stopListeningPoint();
                startListeningPoint();
                port = getPort();
            }
            catch (Exception e) {
                logger.throwing(this.getClass().getName(), "register", e);
            }
                 }
         */
    	
    	String srvHost = findSipSrvServer(registrarHost);
    	if (srvHost != null) {
    		registrarHost = srvHost;
    	}

        try {
            URI requestUri = addressFactory.createURI("sip:" + registrarHost);

            Address toAddress =
                    addressFactory.createAddress("sip:" + sipAddress);
            ToHeader to = headerFactory.createToHeader(toAddress, null);

            String tag = "" + System.currentTimeMillis();
            Address fromAddress =
                    addressFactory.createAddress("sip:" + sipAddress);
            FromHeader from = headerFactory.createFromHeader(fromAddress, tag);

            ContactHeader contact = createContactHeader(username);

            RegisterProcessor rp =
                    new RegisterProcessor(this, requestUri, to, from, contact);
            
            addProcessor(rp.getCallId(), rp);

            // Register with short interval if STUN is used
            if (stunIsInUse()) {
                rp.register(username, password, 105);
            } else {
                rp.register(username, password, 3600);
            }
            return rp;
        } catch (ParseException pe) {
            pe.printStackTrace();
            signalRegistrationError(null, 0);
        }

        return null;

    }

    /**
     * Calls a SIP address
     *
     * @param sipAddress The SIP address to call
     * @param username The username for the registration, used in
     * authentication.
     * @param password The password for the username of the registration, used
     * in authentication.
     * @param sdp The SDP to use in the INVITE
     */
    public OutgoingCallDialog invite(String sipAddress, String sdp) {

        // If not registered, force NIST SIP to make a STUN test
        if (stunIsInUse() && !isRegistered()) {

            try {
                stop();
                init(listener, localIpAddress, outboundProxy, originalPort,
                     stunStack);
                startListeningPoint();
            } catch (Exception e) {
                logger.throwing(this.getClass().getName(), "invite", e);
            }
        }

        try {
            Address toAddress =
                    addressFactory.createAddress("sip:" + sipAddress);
            ToHeader to = headerFactory.createToHeader(toAddress, null);

            URI requestUri = toAddress.getURI();
            
            if (requestUri instanceof SipURI) {
            	String srvHost = findSipSrvServer(((SipURI)requestUri).getHost());
            	if (srvHost != null) {
            		((SipURI)requestUri).setHost(srvHost);
            	}
            }

            String tag = "" + System.currentTimeMillis();

            SipURI toUri = (SipURI) toAddress.getURI();
            String fromAddressStr = getFromAddress(toUri.getHost());
            if (fromAddressStr == null) {
                fromAddressStr = getPrimarySipAddress();
            }

            Address fromAddress =
                    addressFactory.createAddress("\"" + fullName + "\"" + "<sip:" + fromAddressStr+">");
            FromHeader from =
                    headerFactory.createFromHeader(fromAddress, tag);

            ContactHeader contact = createContactHeader("user");

            CallProcessor cp = new CallProcessor(this);

            addProcessor(cp.getCallId(), cp);
            
            return cp.invite(requestUri, to, from, contact, sdp);
        } catch (ParseException pe) {
            pe.printStackTrace();
            signalOutgoingCallError(null, 0);
        }

        return null;

    }

    /**
     * Signals that the registration did not succeed.
     *
     * @param dialog The associated dialog
     * @param code The SIP response code
     */
    public void signalRegistrationError(RegisterDialog dialog, int code) {
        logger.finer("Registration error signalled: " + code);

        listener.signalRegistrationError(dialog, code);
    }

    /**
     * Signals that the registration suceeded.
     *
     * @param rp The RegisterProcessor connected to this registration.
     */
    public void signalRegistrationSuccess(RegisterProcessor rp) {
        logger.finer("Registration completed signalled!");

        if (rp.getLastExpires() == 0) {
            registerProcessorList.remove(rp);
        } else {
            registerProcessorList.add(rp);
        }

        listener.signalRegistrationSuccess(rp);
    }

    public boolean isRegistered() {
        return!registerProcessorList.isEmpty();
    }

    /**
     * Creates a Contact header for the local user.
     *
     * @param username The username to put in the contact header.
     *
     * @rturn The Contact header
     *
     * @todo Check if port is not 5060
     */
    public ContactHeader createContactHeader(String username) throws
            ParseException {

        Address contactAddress;

        int port;
        String ip;
        if (stunStack != null) {
            port = stunMappedPort;
            ip = stunStack.getExternalIp();
        } else {
            port = localPort;
            ip = localIpAddress;
        }
        if (port == 5060) {
            contactAddress =
                    addressFactory.createAddress("sip:" + username + "@" + ip);
        } else {
            contactAddress =
                    addressFactory.createAddress("sip:" + username + "@" + ip +
                                                 ":" + port);
        }

        ContactHeader contact =
                headerFactory.createContactHeader(contactAddress);

        return contact;

    }

    /**
     * Signals that a call has been established!
     *
     * @param cp The CallProcessor assigned to the call
     */
    public void signalEstablishedCall(CallProcessor cp) {
        logger.finer("Established call signalled!");

        listener.signalEstablishedCall(cp);
    }

    /**
     * Signals that a call has been terminated.
     *
     * @param cp The CallProcessor assigned to the call
     */
    public void signalTerminatedCall(CallProcessor cp) {
        logger.finer("Terminated call signalled!");

        listener.signalTerminatedCall(cp);
    }

    /**
     * Signals that an outgoing call has not been sucessful.
     *
     * @param dialog The associated dialog
     * @param statusCode The SIP status code of the error
     */
    public void signalOutgoingCallError(OutgoingCallDialog dialog,
                                        int statusCode) {
        logger.finer("Call error: " + statusCode);

        listener.signalOutgoingCallError(dialog, statusCode);
    }

    /**
     * Signals progress in the outgoing call. All 1xx SIP responses are
     * signalled.
     *
     * @param cp The associated processor
     * @param statusCode The SIP status code (1xx) of the progres
     */
    public void signalOutgoingCallProgress(CallProcessor cp, int statusCode) {
        listener.signalOutgoingCallProgress(cp, statusCode);
    }

    public void setFullName(String name) {
        fullName = name;
    }

    public String getFullName() {
        return fullName;
    }


    /**
     * Signals incoming OPTIONS
     *
     * @param options The OPTIONS dialog
     */
    public void signalIncomingOptions(IncomingOptionsDialog options) {
        listener.signalIncomingOptions(options);
    }

    /**
     * Sets the primary SIP address. <br>
     * <br>
     * This address is used when calling other hosts and should be an address
     * that the user is registered as, in a registrar. <br>
     * <br>
     * If user is not registered anywhere, the address may be set to null.
     *
     * @param sipAddress The SIP address to use in the next call without the
     * preceeding "sip:", or null if there is no SIP address.
     */
    public void setPrimarySipAddress(String sipAddress) {
        primarySipAddress = sipAddress;
    }


    /**
     * Gets the primary SIP address for this user. If no primary SIP address
     * is set (or if primary SIP address is set to null), this function will
     * return "user@<local-ip>". If STUN is used, it will return
     * "user@<external-ip>:<external-port>".
     *
     * @return The primary SIP address without "sip:".
     */
    public String getPrimarySipAddress() {
        if (primarySipAddress != null && primarySipAddress.length() > 0) {
            return primarySipAddress;
        }

        if (stunStack != null) {
            return "user@" + stunStack.getExternalIp() + ":" + stunMappedPort;
        }

        if (localPort != 5060) {
            return "user@" + localIpAddress + ":" + localPort;
        }

        return "user@" + localIpAddress;
    }

    /**
     * This class is used to handle incoming responses and requests in new
     * threads. This way, NIST SIP will never be blocked.
     *
     * @author Andreas Piirimets, Omnitor AB
     */
    class MessageEventHandler implements Runnable {

        private RequestEvent requestEvent;
        private ResponseEvent responseEvent;
        private SipController sc;

        /**
         * Initializes. Does not start the thread.
         *
         * @param sc The SipController to use for feedback.
         * @param re The incoming event
         */
        public MessageEventHandler(SipController sc, RequestEvent re) {
            this.sc = sc;
            requestEvent = re;
            responseEvent = null;
        }

        /**
         * Initializes. Does not start the thread.
         *
         * @param sc The SipController to use for feedback
         * @param re The incoming event
         */
        public MessageEventHandler(SipController sc, ResponseEvent re) {
            this.sc = sc;
            requestEvent = null;
            responseEvent = re;
        }

        /**
         * Runs the thread.
         *
         */
        public void run() {
            if (requestEvent != null) {
                processRequest();
            } else if (responseEvent != null) {
                processResponse();
            }
        }

        /**
         * Processes an incoming response
         *
         */
        private void processResponse() {
            Response response = responseEvent.getResponse();

            CallIdHeader cid = (CallIdHeader) response.getHeader("Call-ID");
            Processor p = getProcessor(cid.getCallId());

            if (p == null) {
                // TBD: Send som kind of error message. Messag/Transaction does
                // not exist, maybe?

                logger.finer("Incoming response: " +
                             response.getStatusCode() +
                             " * Cannot find Call-ID!");
            } else {
                logger.finest("Sending " +
                              responseEvent.getResponse().getStatusCode() +
                              " to " + p.getClass().getName());

                p.processResponse(response,
                                  responseEvent.getClientTransaction());
            }
        }

        /**
         * Processes an incoming request.
         *
         */
        private void processRequest() {
            Request request = requestEvent.getRequest();
            ServerTransaction serverTransaction =
                    requestEvent.getServerTransaction();

            if (serverTransaction == null) {
                try {
                    serverTransaction =
                            sipProvider.getNewServerTransaction(request);
                } catch (TransactionAlreadyExistsException taee) {
                    // The message that arrived was a re-sent message. Just
                    // ignore this.
                    System.out.println("A re-sent request arrived: " +
                                       requestEvent.getRequest().getMethod());
                    return;
                } catch (TransactionUnavailableException tue) {
                    // Transaction cannot be created, probably because we
                    // cannot determine the next hop. Just ignore and do
                    // nothing.
                    logger.fine("Cannot find or create a transaction for an " +
                                "incoming request that is out-of-state. " +
                                "This is probably due to that we cannot " +
                                "determine the next hop.");
                    return;
                }
            }

            // Get the correct processor for the call, or create one if there
            // is no existing processor yet for this call.
            CallIdHeader cid = (CallIdHeader) request.getHeader("Call-ID");
            Processor p = getProcessor(cid.getCallId());
            
            if (p != null &&
            		request.getMethod().equals("INVITE") && 
            		p instanceof CallProcessor && 
            		((CallProcessor)p).getState() == CallProcessor.OUTGOING_CALL) {
            	
            	// This INVITE origins from myself! Return 486 Busy Here immediately.
            	
            	p = new CallProcessor(sc);
            	((CallProcessor)p).setImmediateBye(true);
            }

            if (p == null) {
                String method = request.getMethod();
                if (method.equals("INVITE") || method.equals("OPTIONS")) {
                    p = new CallProcessor(sc);
                    addProcessor(cid.getCallId(), p);
                }
            }

            if (p != null) {
            	// Process the request
            	p.processRequest(request, serverTransaction);
            }

        }
    }

    /**
     * Signals that an incoming call transfer request has arrived, we now
     * ask the client for acceptance.
     *
     * @param referDialog The associated dialog.
     */
    public void askForCallTransferAcceptance(IncomingReferDialog referDialog) {
        listener.askForCallTransferAcceptance(referDialog);
    }

    /**
     * Signals successful call transfer
     *
     * @param cp The call processor for the new call.
     */
    public void signalCallTransferSuccess(CallProcessor cp) {
        listener.signalCallTransferSuccess(cp);
    }

    /**
     * Signals a call transfer error.
     *
     * @param code The SIP code that explains the error.
     */
    public void signalCallTransferError(int code) {
        listener.signalCallTransferError(code);
    }

    /**
     * Signalls that an incoming call has been cancelled.
     *
     * @param icd The associated dialog.
     */
    public void signalCancelledIncomingCall(IncomingCallDialog icd) {
        listener.signalCancelledIncomingCall(icd);
    }

    /**
     * Sets the list of usernames and passwords.
     *
     * @param The list
     */
    public void setAuthInfo(AuthInfo[] authInfo) {
        this.authInfo = authInfo;
    }

    /**
     * Gets authentication information for a realm
     *
     * @param realm The realm to lookup info for
     *
     * @return The authInfo, null if none was available for the given realm
     */
    public AuthInfo getAuthInfo(String realm, String host) {
        if (authInfo == null) {
            return null;
        }

        for (int cnt = 0; cnt < authInfo.length; cnt++) {
            if (realm.equals(authInfo[cnt].getRealm())) {
                return authInfo[cnt];
            }
        }

        // If no corresponding realm was found, check if the host name can
        // give us the correct authInfo row.
        
        for (int cnt = 0; cnt < authInfo.length; cnt++) {
            if (host.equals(authInfo[cnt].getRealm())) {
                return authInfo[cnt];
            }
        }

        
        return null;
    }

    /**
     * Sets the list of from addresses, that are connected to a dial domain
     *
     * @param sipAddressList The from address list
     */
    public void setFromAddressList(FromAddress[] list) {
        this.fromAddressList = list;
    }

    /**
     * Gets an address, connected to the given dial domain.
     *
     * @param dialDomain The dial domain to find an address for
     *
     * @return The from address, connected to the dial domain. null, if no
     * address was found.
     */
    public String getFromAddress(String dialDomain) {
        if (fromAddressList == null) {
            return null;
        }

        for (int cnt = 0; cnt < fromAddressList.length; cnt++) {
            if (dialDomain.equals(fromAddressList[cnt].getDialDomain())) {
                return fromAddressList[cnt].getFromAddress();
            }
        }

        return null;
    }


    public boolean stunIsInUse() {
        return stunStack != null;
    }

    /**
     * Not used, only to implement the SipListener interface.
     * This is for the newest version of NIST SIP
     *
     * @param dte DialogterminatedEvent
     */
    public void processDialogTerminated(DialogTerminatedEvent dte) {
    }

    /**
     * Not used, only to implement the SipListener interface.
     * This is for the newest version of NIST SIP.
     *
     * @param tte TransactionTerminatedEvent
     */
    public void processTransactionTerminated(TransactionTerminatedEvent tte) {
    }

    /**
     * Not used, only to implement the SipListener interface.
     * This is for the newest version of NIST SIP.
     *
     * @param iee IOExceptionEvent
     */
    public void processIOException(IOExceptionEvent iee) {
    }

    /**
     * Finds a STUN server by looking at STUN SRV records of a given host name.
     *
     * @param host The host name to search SRV records on
     * @return STUN server address. Null if no server was found.
     */
    private String findSipSrvServer(String host) {
        if (findDns() == null) {
            return null;
        }
        boolean foundStunServer = false;
        String ssa = null;

        try {
            java.util.Hashtable<String, String> env = new java.util.Hashtable<String, String>();
            env.put("java.naming.factory.initial",
                    "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url",
                    "dns://" + findDns());

            javax.naming.directory.DirContext ctx = new javax.naming.directory.
                    InitialDirContext(env);

            javax.naming.directory.Attributes attrs = ctx.getAttributes(
                    "_sip._udp." + host, new String[] {"SRV"});

            for (javax.naming.NamingEnumeration ae = attrs.getAll();
                    ae.hasMoreElements() &&
                    !foundStunServer; ) {
                javax.naming.directory.Attribute attr = (javax.naming.directory.
                        Attribute) ae.next();
                if (attr.getID().equals("SRV")) {
                    for (java.util.Enumeration vals = attr.getAll();
                            vals.hasMoreElements(); ) {
                        String e = (String) vals.nextElement();
                        String el[] = e.split(" ");
                        if (el.length > 3) {
                            if (el[3].endsWith(".")) {
                                ssa = el[3].substring(0, el[3].length() - 1);
                            } else {
                                ssa = el[3];
                            }
                            foundStunServer = true;
                        }
                    }

                }
            }

            ctx.close();
        }
        catch (NameNotFoundException nnfe) {
            // DNS name not found, just continue
        }
        catch (NamingException ne) {
            // DNS problems just continue
        }
        /*catch (Exception e) {
            System.err.println("Problem querying DNS: " + e);
            e.printStackTrace();
        }*/

        return ssa;
    }

    /**
     * Returns the IP of a DNS server.
     *
     * @return IP of DNS server, null if no DNS server was found.
     */
    private String findDns() {
        int delayTime = -1000;

        for (int cnt=0; cnt<2; cnt++) {
            delayTime += 3000;

            // If this function has been run before, return the cached result to
            // speed up the application.
            if (dnsServer != null) {
                return dnsServer;
            }

            BufferedReader in = null;
            try {
                // Run nslookup and look at the printout to achieve system's DNS
                // server setting. There seems not to be any better solution! If
                // there is, please replace this.
                Process p = Runtime.getRuntime().exec("nslookup");

                // Sleep for 2 secs in order to wait for nslooup to start and
                // generate output.
                Thread.sleep(delayTime);
                p.destroy();

                // Read the output of nslookup
                in = new BufferedReader(new InputStreamReader(p.
                        getInputStream()));
                String readerString = "";

                while (((readerString = in.readLine()) != null)) {
                    String spl[] = readerString.split("Address:");
                    if (spl.length > 1) {
                        return spl[1].trim();
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            finally {
            	if (in != null) {
            		try {
						in.close();
					} catch (IOException e) {
						// Ignore exception
					}
            	}
            }
        }

        return null;
    }


}
