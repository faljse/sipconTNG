/*
 * Stun4j, the OpenSource Java Solution for NAT and Firewall Traversal.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.stun4j.stack;

import java.util.Random;
import java.util.Arrays;

/**
 * This class encapsulates a STUN transaction ID. It is useful for storing
 * transaction ids in collection objects as it implements the equals method.
 * It also provies a utility for creating unique transaction ids.
 *
 * <p>Organisation: <p> Louis Pasteur University, Strasbourg, France</p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr)</p></p>
 * @author Emil Ivov
 * @version 0.1
 */

class TransactionID
{
    /**
     * The id itself
     */
    private byte transactionID[] = new byte[16];

    /**
     * The object to use to generate the rightmost 8 bytes of the id.
     */
    private static Random random = new Random(System.currentTimeMillis());

    /**
     * A hashcode for hashtable storage.
     */
    private int hashCode = 0;

    private TransactionID()
    {
    }

    /**
     * Creates a transaction id object.The transaction id itself is genereated
     * using the folloing algorithm:
     *
     * The first 8 bytes of the id are given the value of System.currentTimeMillis()
     * Putting the right most bits first so that we get a more optimized equals()
     * method.
     *
     * @return A TransactionID object with a unique transaction id.
     */
    static TransactionID createTransactionID()
    {
        TransactionID tid = new TransactionID();

        long left  = System.currentTimeMillis();//the first 8 bytes of the id
        long right = random.nextLong();//the last 8 bytes of the id

        for(int i = 0; i < 8; i++)
        {
            tid.transactionID[i]   = (byte)((left  >> (i*8))& 0xFFl);
            tid.transactionID[i+8] = (byte)((right >> (i*8))& 0xFFl);
        }

        //calculate hashcode for Hashtable storage.
        tid.hashCode =   (tid.transactionID[3] << 24 & 0xFF000000)
                       | (tid.transactionID[2] << 16 & 0x00FF0000)
                       | (tid.transactionID[1] << 8  & 0x0000FF00)
                       | (tid.transactionID[0]       & 0x000000FF);

        return tid;
    }

    /**
     * Creates a transaction identifier object with the specified id.
     * @param transactionID the id to give to the new TransactionID
     * @return a new TransactionID object with the specified id value;
     */
    static TransactionID createTransactionID(byte[] transactionID)
    {
        TransactionID tid = new TransactionID();

        System.arraycopy(transactionID, 0, tid.transactionID, 0, 16);

        //calculate hashcode for Hashtable storage.
        tid.hashCode =   (tid.transactionID[3] << 24 & 0xFF000000)
                       | (tid.transactionID[2] << 16 & 0x00FF0000)
                       | (tid.transactionID[1] << 8  & 0x0000FF00)
                       | (tid.transactionID[0]       & 0x000000FF);


        return tid;
    }


    /**
     * Returns the transaction id byte array (length 16).
     * @return the transaction ID byte array.
     */
    public byte[] getTransactionID()
    {
        return transactionID;
    }

    /**
     * Compares two TransactionID objects.
     * @param obj the object to compare with.
     * @return true if the objects are equal and false otherwise.
     */
    public boolean equals(Object obj)
    {

        if(   obj == null
           || !(obj instanceof TransactionID))
            return false;

        if(this == obj)
            return true;

        byte targetBytes[] = ((TransactionID)obj).transactionID;

        return Arrays.equals(transactionID, targetBytes);
    }

    /**
     * Compares the specified byte array with this transaction id.
     * @param targetID the id to compare with ours.
     * @return true if targetID matches this transaction id.
     */
    public boolean equals(byte[] targetID)
    {
        return Arrays.equals(transactionID, targetID);
    }

    /**
     * Returns the first four bytes of the transactionID to ensure proper
     * retrieval from hashtables;
     * @return the hashcode of this object - as advised by the Java Platform
     * Specification
     */
    public int hashCode()
    {
        return hashCode;
    }

    /**
     * Returns a string representation of the ID
     * @return a hex string representing the id
     */
    public String toString()
    {
        StringBuffer idStr = new StringBuffer();

        for(int i = 0; i < transactionID.length; i++)
        {
            idStr.append("0x");
            if((transactionID[i]&0xFF) <= 15)
            {
                idStr.append("0");
            }

            idStr.append(Integer.toHexString(transactionID[i]&0xff).toUpperCase());

            if(i < transactionID.length)
                idStr.append(" ");
        }

        return idStr.toString();
    }
}
