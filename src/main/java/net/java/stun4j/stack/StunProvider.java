
/*
 * Stun4j, the OpenSource Java Solution for NAT and Firewall Traversal.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.stun4j.stack;

import java.util.*;
import java.util.logging.*;
import net.java.stun4j.*;
import net.java.stun4j.message.*;

/**
 * The StunProvider class is an implementation of a Stun Transaction Layer. STUN
 * transactions are extremely simple and are only used to correlate requests and
 * responses. In the Stun4J implementation it is the transaction layer that
 * ensures reliable delivery.
 *
 * <p>Organisation: <p> Louis Pasteur University, Strasbourg, France</p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr)</p></p>
 * @author Emil Ivov
 * @version 0.1
 */

public class StunProvider
    implements MessageEventHandler
{
    private static final Logger logger =
        Logger.getLogger(StunProvider.class.getName());

    /**
     * Stores active client transactions mapped against TransactionID-s.
     */
    private Hashtable clientTransactions = new Hashtable();

    /**
     * Currently open server transactions. The vector contains transaction ids
     * for transactions corresponding to all non-answered received requests.
     */
    private Hashtable serverTransactions = new Hashtable();

    /**
     * The stack that created us.
     */
    private StunStack stunStack                  = null;

    /**
     * A dispatcher for incoming requests event;
     */
    private EventDispatcher  eventDispatcher    = new EventDispatcher();



    //------------------ public interface
    /**
     * Creates the provider.
     * @param stunStack The currently active stack instance.
     */
    StunProvider(StunStack stunStack)
    {
        this.stunStack = stunStack;
    }

    /**
     * Sends the specified request through the specified access point, and
     * registers the specified ResponseCollector for later notification.
     * @param  request     the request to send
     * @param  sendTo      the destination address of the request.
     * @param  sendThrough the access point to use when sending the request
     * @param  collector   the instance to notify when a response arrives or the
     *                     the transaction timeouts
     * @throws StunException
     * ILLEGAL_STATE if the stun stack is not started. <br/>
     * ILLEGAL_ARGUMENT if the apDescriptor references an access point that had
     * not been installed <br/>
     * NETWORK_ERROR if an error occurs while sending message bytes through the
     * network socket. <br/>

     */
    public void sendRequest( Request                  request,
                             StunAddress              sendTo,
                             NetAccessPointDescriptor sendThrough,
                             ResponseCollector        collector )
        throws StunException
    {
        stunStack.checkStarted();

        StunClientTransaction clientTransaction =
                                        new StunClientTransaction(this,
                                                                  request,
                                                                  sendTo,
                                                                  sendThrough,
                                                                  collector);

        clientTransactions.put(clientTransaction.getTransactionID(),
                               clientTransaction);
        clientTransaction.sendRequest();
    }

    /**
     * Sends the specified response message through the specified access point.
     *
     * @param transactionID the id of the transaction to use when sending the
     *    response. Actually we are getting kind of redundant here as we already
     *    have the id in the response object, but I am bringing out as an extra
     *    parameter as the user might otherwise forget to explicitly set it.
     * @param response      the message to send.
     * @param sendThrough   the access point to use when sending the message.
     * @param sendTo        the destination of the message.
     * @throws StunException TRANSACTION_DOES_NOT_EXIST if the response message
     * has an invalid transaction id. <br/>
     * ILLEGAL_STATE if the stun stack is not started. <br/>
     * ILLEGAL_ARGUMENT if the apDescriptor references an access point that had
     * not been installed <br/>
     * NETWORK_ERROR if an error occurs while sending message bytes through the
     * network socket. <br/>
     */
    public void sendResponse(byte[]                   transactionID,
                             Response                 response,
                             NetAccessPointDescriptor sendThrough,
                             StunAddress                  sendTo)
        throws StunException
    {
        stunStack.checkStarted();

        TransactionID tid = TransactionID.createTransactionID(transactionID);
        StunServerTransaction sTran =
            (StunServerTransaction)serverTransactions.get(tid);

        if(sTran == null || sTran.isReransmitting()){
            throw new StunException(StunException.TRANSACTION_DOES_NOT_EXIST,
                                    "The transaction specified in the response "
                                    + "object does not exist or has already "
                                    + "transmitted a response.");
        }
        else{
            sTran.sendResponse(response, sendThrough, sendTo);
        }



    }

    /**
     * Sets the listener that should be notified when a new Request is received.
     * @param requestListener the listener interested in incoming requests.
     */
    public  void addRequestListener(RequestListener requestListener)
    {
        this.eventDispatcher.addRequestListener( requestListener );
    }

    /**
     * Removes the specified listener from the local listener list. (If any
     * instances of this listener have been registered for a particular
     * access point, they will not be removed).
     * @param listener the RequestListener listener to unregister
     */
    public void removeRequestListener(RequestListener listener)
    {
        this.eventDispatcher.removeRequestListener(listener);
    }

    /**
     * Add a RequestListener for requests coming from a specific NetAccessPoint.
     * The listener will be invoked only when a request event is received on
     * that specific property.
     *
     * @param apDescriptor  The descriptor of the NetAccessPoint to listen on.
     * @param listener  The ConfigurationChangeListener to be added
     */

    public synchronized void addRequestListener(
        NetAccessPointDescriptor apDescriptor,
        RequestListener listener)
    {
        eventDispatcher.addRequestListener(apDescriptor, listener);
    }



//------------- stack internals ------------------------------------------------
    /**
     * Returns the currently active instance of NetAccessManager. Used by client
     * transactions when sending messages.
     * @return the currently active instance of NetAccessManager.
     */
    NetAccessManager getNetAccessManager()
    {
        return stunStack.getNetAccessManager();
    }

    /**
     * Removes a client transaction from this providers client transactions list.
     * Method is used by StunClientTransaction-s themselves when a timeout occurs.
     * @param tran the transaction to remove.
     */
    synchronized void removeClientTransaction(StunClientTransaction tran)
    {
        clientTransactions.remove(tran.getTransactionID());
    }

    /**
     * Removes a server transaction from this provider's server transactions
     * list.
     * Method is used by StunServerTransaction-s themselves when they expire.
     * @param tran the transaction to remove.
     */
    synchronized void removeServerTransaction(StunServerTransaction tran)
    {
        serverTransactions.remove(tran.getTransactionID());
    }

    /**
     * Called to notify this provider for an incoming message.
     * @param event the event object that contains the new message.
     */
    public void handleMessageEvent(StunMessageEvent event)
    {
        Message msg = event.getMessage();

        if(logger.isLoggable(Level.FINEST))
            logger.finest("Received a message on NetAP"
                        + event.getSourceAccessPoint()
                        + " of type:"
                        + msg.getMessageType());

        //request
        if(msg instanceof Request)
        {
            TransactionID serverTid = TransactionID.
                                    createTransactionID(msg.getTransactionID());

            StunServerTransaction sTran  =
                (StunServerTransaction)serverTransactions.get(serverTid);
            if( sTran != null){
                //requests from this transaction have already been seen
                //retransmit the response if there was any
                try
                {
                    sTran.retransmitResponse();
                    logger.finest("Response retransmitted");
                }
                catch (StunException ex)
                {
                    //we couldn't really do anything here .. apart from logging
                    logger.log(Level.WARNING,
                               "Failed to retransmit a stun response", ex);
                }

                String propagate = System.getProperty(
                    "net.java.stun4j.PROPAGATE_RECEIVED_RETRANSMISSIONS");
                if(propagate == null || !propagate.trim().equalsIgnoreCase("true"))
                    return;
            }
            else{

                sTran =
                    new StunServerTransaction(this, serverTid);

                serverTransactions.put(serverTid, sTran);
                sTran.start();
            }
            eventDispatcher.fireMessageEvent(event);
        }
        //response
        else if(msg instanceof Response)
        {
            TransactionID tid = TransactionID.
                                    createTransactionID(msg.getTransactionID());

            StunClientTransaction tran =
                (StunClientTransaction)clientTransactions.remove(tid);

            if(tran != null)
            {
                tran.handleResponse(event);
            }
            else
            {
                //do nothing - just drop the phantom response.
                logger.fine("Dropped response - no matching client tran found.");
                logger.fine("response tid was - " + tid.toString());
                logger.fine("all tids in stock were" + clientTransactions.toString());
            }
        }

    }

    /**
     * Cancels all running transactions and prepares for garbage collection
     */
    void shutDown()
    {
        eventDispatcher.removeAllListeners();

        Enumeration tids = clientTransactions.keys();
        while (tids.hasMoreElements()) {
            TransactionID item = (TransactionID)tids.nextElement();
            StunClientTransaction tran =
                        (StunClientTransaction)clientTransactions.remove(item);
            if(tran != null)
                tran.cancel();

        }

        tids = serverTransactions.keys();
        while (tids.hasMoreElements()) {
            TransactionID item = (TransactionID)tids.nextElement();
            StunServerTransaction tran =
                        (StunServerTransaction)clientTransactions.remove(item);
            if(tran != null)
                tran.expire();

        }
    }

}
