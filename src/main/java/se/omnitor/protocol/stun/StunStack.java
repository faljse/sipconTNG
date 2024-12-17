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
package se.omnitor.protocol.stun;

import net.java.stun4j.StunAddress;
import net.java.stun4j.StunException;
import net.java.stun4j.client.SimpleAddressDetector;
import net.java.stun4j.client.NetworkConfigurationDiscoveryProcess;
import net.java.stun4j.client.StunDiscoveryReport;
import java.util.logging.Logger;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class StunStack {

    /**
     * Indicates that NAT detection has failed or not yet initiated.
     */
    public static final String UNKNOWN = "Unknown Network Configuration";

    /**
     * Means, there's no NAT or firewall.
     */
    public static final String OPEN_INTERNET = "Open Internet Configuration";

    /**
     * Indicates that UDP communication is not possible.
     */
    public static final String UDP_BLOCKING_FIREWALL = "UDP Blocking Firewall";

    /**
     * Means we are behind a symmetric udp firewall.
     */
    public static final String SYMMETRIC_UDP_FIREWALL =
            "Symmetric UDP Firewall";

    /**
     * NAT type is full cone.
     */
    public static final String FULL_CONE_NAT = "Full Cone NAT";

    /**
     * We are behind a symmetric nat.
     */
    public static final String SYMMETRIC_NAT = "Symmetric NAT";

    /**
     * NAT type is Restricted Cone.
     */
    public static final String RESTRICTED_CONE_NAT = "Restricted Cone NAT";

    /**
     * NAT type is port restricted cone.
     */
    public static final String PORT_RESTRICTED_CONE_NAT =
            "Port Restricted Cone NAT";

    private String natType = UNKNOWN;

    private Logger logger;


    private String stunServer;
    private String localIp;
    private NetworkConfigurationDiscoveryProcess ncdp;
    private boolean isStarted = false;

    private String extIp;

    public StunStack(String localIp, String stunServer) {
        logger = Logger.getLogger("se.omnitor.protocol.stun");
        this.stunServer = stunServer;
        this.localIp = localIp;
    }


    private int getFreePort() {
        for (int cnt=1024; cnt<65535; cnt+=2) {
            try {
                DatagramSocket dgs = new DatagramSocket(cnt);
                dgs.close();
                return cnt;
            } catch (SocketException se) {
                // Try next port
            }
        }
        return -1;
    }

    public void start() throws StunStackException {
        StunAddress lIp = new StunAddress(localIp, getFreePort());
        StunAddress ssIp = new StunAddress(stunServer, 3478);
        ncdp = new NetworkConfigurationDiscoveryProcess(lIp, ssIp);
        try {
            ncdp.start();
            StunDiscoveryReport sdr = ncdp.determineAddress();
            natType = sdr.getNatType();

            StunAddress tempAddr = sdr.getPublicAddress();
            if (tempAddr == null) {
                throw new StunStackException("STUN server does not answer, probably cauesd by wrong STUN server setting or a UDP blocking firewall.");
            }
            extIp = InetAddress.getByName(tempAddr.getHostName()).getHostAddress();
            /*
            if (extIp.endsWith("in-addr.arpa")) {
                extIp = reverse(extIp.split("\\."));
            }
            */

            isStarted = true;
        } catch (StunException se) {
            throw new StunStackException(se.getMessage());
        }
        catch (UnknownHostException uhe) {
            throw new StunStackException(uhe.getMessage());
        }

    }

    public void stop() {
        ncdp.shutDown();
        isStarted = false;
    }

    public String getExternalIp() {
        if (!isStarted) {
            logger.severe(
                    "ERROR: StunStack.getExternalIp() is called when StunStack is stopped!");
        }
        return extIp;
    }

    public String getNatType() {
        if (!isStarted) {
            logger.severe(
                    "ERROR: StunStack.getNatType() is called when StunStack is stopped!");
        }
        return natType;
    }

    public int getMappedPort(int localPort) throws StunStackException {
        StunAddress sa = new StunAddress(stunServer, 3478);
        SimpleAddressDetector sad = new SimpleAddressDetector(sa);
        try {
            sad.start();
            StunAddress mappedAddress = sad.getMappingFor(localPort);
            sad.shutDown();

            return mappedAddress.getPort();
        } catch (StunException se) {
            throw new StunStackException(se.getMessage());
        }
    }

    /*
    private String reverse(String[] s) {
        String string1 = s[0];
        String string2 = s[1];
        String string3 = s[2];
        String string4 = s[3];

        String address = string4 + "." + string3 + "." + string2 + "." +
                         string1;

        return address;
    }
    */

    public boolean isStarted() {
        return isStarted;
    }
}
