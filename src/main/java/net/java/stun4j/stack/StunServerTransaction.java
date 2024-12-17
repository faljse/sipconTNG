/*
 * Stun4j, the OpenSource Java Solution for NAT and Firewall Traversal.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.stun4j.stack;

import java.util.TimerTask;
import net.java.stun4j.message.Request;
import net.java.stun4j.NetAccessPointDescriptor;
import net.java.stun4j.ResponseCollector;
import net.java.stun4j.*;
import net.java.stun4j.message.*;

/**
 * A STUN client retransmits requests as specified by the protocol.
 *
 * Once formulated and sent, the client sends the Binding Request.  Reliability
 * is accomplished through request retransmissions.  The ClientTransaction
 * retransmits the request starting with an interval of 100ms, doubling
 * every retransmit until the interval reaches 1.6s.  Retransmissions
 * continue with intervals of 1.6s until a response is received, or a
 * total of 9 requests have been sent. If no response is received by 1.6
 * seconds after the last request has been sent, the client SHOULD
 * consider the transaction to have failed. In other words, requests
 * would be sent at times 0ms, 100ms, 300ms, 700ms, 1500ms, 3100ms,
 * 4700ms, 6300ms, and 7900ms. At 9500ms, the client considers the
 * transaction to have failed if no response has been received.
 *
 * A server transaction is therefore responsible for retransmitting the same
 * response that was saved for the original request, and not let any
 * retransmissions go through to the user application.
 *
 * <p>Organisation: <p> Louis Pasteur University, Strasbourg, France</p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr)</p></p>
 * @author Emil Ivov
 * @version 0.1
 */

class StunServerTransaction
    implements Runnable
{
    /**
     * The time that we keep server transactions active.
     */
    private long transactionLifetime = 16000;

    /**
     * The StunProvider that created us.
     */
    private StunProvider      providerCallback  = null;

    /**
     * The source of the transaction request.
     */
    private StunAddress responseDestination = null;

    /**
     * The response sent in response to the request.
     */
    private Response response = null;

    /**
     * The ap descriptor used when sending the response
     */
    private NetAccessPointDescriptor apDescriptor = null;

    /**
     * The id of the transaction.
     */
    private TransactionID    transactionID      = null;

    /**
     * The date (in millis) when the next retransmission should follow.
     */
    private long expirationDate = -1;

    /**
     * The thread that this transaction runs in.
     */
    private Thread runningThread = null;

    /**
     * Determines whether or not the transaction has expired.
     */
    private boolean expired = true;

    /**
     * Determines whether or not the transaction is in a retransmitting state.
     * In other words whether a response has already been sent once to the
     * transaction request.
     */
    private boolean isRetransmitting = false;

    /**
     * Creates a server transaction
     * @param providerCallback the provider that created us.
     * @param tranID the transaction id contained by the request that was the
     * cause for this transaction.
     */
    public StunServerTransaction(StunProvider            providerCallback,
                                 TransactionID           tranID)
    {
        this.providerCallback  = providerCallback;

        this.transactionID = tranID;

        runningThread = new Thread(this);
    }

    /**
     * Start the transaction. This launches the countdown to the moment the
     * transaction would expire.
     */
    public void start()
    {
        expired = false;
        runningThread.start();
    }

    /**
     * Actually this method is simply a timer waiting for the server transaction
     * lifetime to come to an end.
     */
    public void run()
    {
        runningThread.setName("ServTran");

        schedule(transactionLifetime);
        waitNextScheduledDate();

        //let's get lost
        expire();
        providerCallback.removeServerTransaction(this);
    }

    /**
     * Sends the specified response through the <code>sendThrough</code>
     * NetAccessPoint descriptor to the specified destination and changes
     * the transaction's state to retransmitting.
     *
     * @param response the response to send the transaction to.
     * @param sendThrough the NetAccessPoint through which the response is to
     * be sent
     * @param sendTo the destination of the response.
     *
     * @throws StunException if sending this response fails for some reason.
     */
    void sendResponse(Response response,
                      NetAccessPointDescriptor sendThrough,
                      StunAddress sendTo)
        throws StunException
    {
        if(!isRetransmitting){
            this.response = response;
            //the transaction id might already have been set, but its our job
            //to make sure of that
            response.setTransactionID(this.transactionID.getTransactionID());
            this.apDescriptor = sendThrough;
            this.responseDestination = sendTo;
        }

        isRetransmitting = true;
        retransmitResponse();
    }

    /**
     * Retransmits the response that was originally sent to the request that
     * caused this transaction.
     * @throws StunException
     */
    void retransmitResponse()
        throws StunException
    {
        //don't retransmit if we are expired or if the user application
        //hasn't yet transmitted a first response
        if(expired || !isRetransmitting)
            return;

        providerCallback.getNetAccessManager().sendMessage(response,
                                                           apDescriptor,
                                                           responseDestination);
    }

    /**
     * Waits until next retransmission is due or until the transaction is
     * cancelled (whichever comes first).
     */
    synchronized void waitNextScheduledDate()
    {
        long current = System.currentTimeMillis();
        while(expirationDate - current > 0)
        {
            try
            {
                wait(expirationDate - current);
            }
            catch (InterruptedException ex)
            {
            }

            //did someone ask us to get lost?
            if(expired)
                return;
            current = System.currentTimeMillis();
        }
    }

    /**
     * Sets the expiration date for this server transaction.
     * @param timeout the number of millis to wait before expiration.
     */
    void schedule(long timeout)
    {
        this.expirationDate = System.currentTimeMillis() + timeout;
    }

    /**
     * Cancels the transaction. Once this method is called the transaction is
     * considered terminated and will stop retransmissions.
     */
    synchronized void expire()
    {
        this.expired = true;
        notifyAll();
    }


    /**
     * Returns the ID of the current transaction.
     *
     * @return the ID of the transaction.
     */
    TransactionID getTransactionID()
    {
        return this.transactionID;
    }

    /**
     * Specifies whether this server transaction is in the retransmitting state.
     * Or in other words - has it already sent a first response or not?
     * @return boolean
     */
    boolean isReransmitting()
    {
        return isRetransmitting;
    }
}
