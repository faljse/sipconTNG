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
package se.omnitor.protocol.sdp;

import java.util.Vector;
import javax.sdp.MediaDescription;

public interface SdpMedia {

    public abstract String getType();

    /**
     * Gets all Format objects associated with this media. In all cases,
     * the formats are listed in order of preference, with the first format
     * listed being preferred.  In this case, preferred means that the
     * recipient of the offer SHOULD use the format with the highest
     * preference that is acceptable to it.
     *
     * @return All Format objects associated with this media, collected in a
     * Vector.
     */
    public Vector getFormats();

    public void setFormats(Vector formatVector);

    /**
     * Gets the remote host's RTP port to send media to.
     *
     * @return The remote host's RTP port to send media to.
     */
    public int getPort();

    /**
     * Gets the remote host's RTCP port to send data to.
     *
     * @return The remote host's RTP port to send data to.
     */
    public int getRtcpPort();

    public int getLocalPort();

    public String getSdp();
    public String getDecliningSdp();

    public String negotiate(SdpMedia remoteMedia, SdpMedia[] resultMedia);

    public String getProtocol();

    public MediaDescription getResultMediaDescription(MediaDescription md);

    public String getRemoteIp();

    public void setRemoteIp(String ip);
    
    /**
     * Gets the actual port on the computer. If STUN mapping is used, this
     * port really tells which physical port to receive data on.
     * @param port int
     */
    public void setPhysicalPort(int port);
    public int getPhysicalPort();

}
