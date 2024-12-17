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

import java.net.DatagramSocket;
import java.net.SocketException;

import se.omnitor.protocol.sip.register.RegisterProcessor;
import se.omnitor.protocol.sip.call.OutgoingCallDialog;
import se.omnitor.protocol.sip.call.IncomingCallDialog;
import se.omnitor.protocol.sip.call.IncomingOptionsDialog;
import se.omnitor.protocol.sip.register.RegisterDialog;
import se.omnitor.protocol.sip.call.IncomingReferDialog;
import se.omnitor.protocol.sip.call.CallProcessor;
import net.java.stun4j.client.SimpleAddressDetector;
import net.java.stun4j.StunAddress;
import net.java.stun4j.StunException;

import javax.sip.InvalidArgumentException;
import javax.sip.PeerUnavailableException;

/**
 * This class is used for analyzing whether there is a SIP compatible NAT
 * between client and internet. Unfortunately, the analyze doesn't work, so
 * this class is currently not in use.
 *
 * @author Andreas Piirimets, Omnitor AB
 * @version 1.0
 */
public class SipNatChecker implements SipControllerListener {

    private SipController sc;
    private String externalIp = null;


    public SipNatChecker(String ipAddress, String outboundProxy,
                         String stunServerAddress) throws PeerUnavailableException {
        sc = new SipController(this, ipAddress, outboundProxy);

        if (stunServerAddress != null) {
            try {
                StunAddress sa = new StunAddress(stunServerAddress, 3478);
                SimpleAddressDetector sad = new SimpleAddressDetector(sa);
                sad.start();

                StunAddress mappedAddress =
                        sad.getMappingFor(new DatagramSocket());
                externalIp = mappedAddress.getHostName();
                if (externalIp.split("\\.").length > 3) {
                    externalIp = reverse(externalIp.split("\\."));
                }
            } catch (StunException se) {
                se.printStackTrace();
            } catch (SocketException se) {
                se.printStackTrace();
            }

        }

    }

    public static String reverse(String[] s) {
        String string1 = s[0];
        String string2 = s[1];
        String string3 = s[2];
        String string4 = s[3];

        String address = string4 + "." + string3 + "." + string2 + "." +
                         string1;

        return address;
    }


    public void start() {
        try {
            sc.start();
        } catch (InvalidArgumentException iae) {
            iae.printStackTrace();
        }
    }

    public void stop() {
        sc.stop();
    }

    /**
     * @todo Testa f�rst att regga, k�r sedan STUN och kolla om det finns n�gon contact-post som st�mmer med den externa IP som man har
     * @param sipAddress String
     * @param registrarHost String
     * @param username String
     * @param password String
     * @return boolean
     */
    public boolean probe(String sipAddress, String registrarHost,
                         String username, String password) {

        /*
                 RegisterProcessor rp = sc.register(sipAddress, registrarHost, username, password);

         System.out.println("Waiting for registration to be complete.");
                 try {
            wait(10000);
                 }
                 catch (InterruptedException ie) {
            // Ignore
                 }
                 System.out.println("Returning from wait.");

                 //rp.get)
         */
        return false;
    }


    public void signalIncomingOptions(IncomingOptionsDialog options) {

    }

    public void signalOutgoingCallError(OutgoingCallDialog ocd, int i) {

    }

    public void signalCancelledIncomingCall(IncomingCallDialog icd) {

    }

    public void signalCallTransferError(int i) {

    }

    public void signalCallTransferSuccess(CallProcessor cp) {

    }

    public void askForCallTransferAcceptance(IncomingReferDialog ird) {

    }

    public void signalOutgoingCallProgress(CallProcessor cp, int i) {

    }

    public void signalTerminatedCall(CallProcessor cp) {

    }

    public void signalEstablishedCall(CallProcessor cp) {

    }

    public void signalRegistrationError(RegisterDialog rd, int i) {
        System.out.println("Error, notifying.");
        notify();
    }

    public void signalRegistrationSuccess(RegisterProcessor rp) {
        System.out.println("Success, notifying!");
        notify();
    }

    public void signalIncomingCall(IncomingCallDialog icd) {

    }


}
