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
import se.omnitor.protocol.sdp.SdpMedia;
import javax.sdp.MediaDescription;
import javax.sdp.Media;
import javax.sdp.SdpParseException;
import se.omnitor.protocol.sdp.Format;
import java.util.Locale;


public abstract class MediaTemplate implements SdpMedia {

	protected String mediaType;
	protected int port;
	protected int portCount;
	protected Vector formats;
	protected String protocol;
	protected int localPort;
	protected String remoteIp;
	protected int physicalPort = -1;


	public MediaTemplate(String mediaType) {
		this.mediaType = mediaType;
		port = 0;
		portCount = 0;
		protocol = "";
		remoteIp = "";

		formats = new Vector(0, 1);
	}

	public MediaTemplate(String mediaType, int port, String protocol) {
		this.mediaType = mediaType;
		this.port = port;
		portCount = 0;
		this.protocol = protocol;
		remoteIp = "";

		formats = new Vector(0, 1);
	}

	public MediaTemplate(Media media) throws SdpParseException {
		mediaType = media.getMediaType();
		port = media.getMediaPort();
		portCount = media.getPortCount();
		protocol = media.getProtocol();
		remoteIp = "";

		formats = new Vector(0, 1);
	}

	public String getType() {
		return mediaType;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Vector getFormats() {
		return formats;
	}

	public void setFormats(Vector formats) {
		this.formats = formats;
	}

	public int getRtcpPort() {
		return port+1;
	}

	public MediaDescription getResultMediaDescription(MediaDescription md) {
		return null;
	}

	public String toString() {
		return mediaType + ", " + port + ", " + portCount + ", " + protocol;
	}

	protected Format getFormat(Format remoteFormat) {
		int payloadNumber = remoteFormat.getPayloadNumber();

		if (payloadNumber < 96) {

			int fLen = formats.size();
			for (int cnt=0; cnt<fLen; cnt++) {
				if (payloadNumber ==
					((Format)formats.elementAt(cnt)).getPayloadNumber()) {

					return (Format)formats.elementAt(cnt);
				}
			}

		}
		else {

			String name = remoteFormat.getName();
			Format tmpFormat;

			int fLen = formats.size();
			for (int cnt=0; cnt<fLen; cnt++) {
				tmpFormat = (Format)formats.elementAt(cnt);

				if (name.toUpperCase(Locale.US).equals(tmpFormat.getName().
						toUpperCase(Locale.US)) &&
						tmpFormat.getPayloadNumber() >= 96) {

					return tmpFormat;
				}
			}

		}

		return null;

	}


	public String getSdp() {
		String sdp = "";

		sdp = "m=" + mediaType + " " + port;

		if (portCount > 0) {
			sdp += "/" + portCount;
		}

		sdp += " " + protocol;

		int flen = formats.size();
		for (int cnt=0; cnt<flen; cnt++) {
			sdp += " " + ((Format)formats.elementAt(cnt)).getPayloadNumber();
		}

		sdp += "\r\n";

		for (int cnt=0; cnt<flen; cnt++) {
			sdp += ((Format)formats.elementAt(cnt)).getSdp();
		}

		return sdp;

	}

	public String getDecliningSdp() {
		String sdp = "";

		sdp = "m=" + mediaType + " 0 " + protocol;

		int flen = formats.size();
		for (int cnt=0; cnt<flen; cnt++) {
			sdp += " " + ((Format)formats.elementAt(cnt)).getPayloadNumber();
		}

		sdp += "\r\n";

		return sdp;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public int getLocalPort() {
		return localPort;
	}

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String ip) {
		remoteIp = ip;
	}

	public void setPhysicalPort(int port) {
		physicalPort = port;
	}

	public int getPhysicalPort() {
		if (physicalPort == -1) {
			return localPort;
		}

		return physicalPort;
	}

}


