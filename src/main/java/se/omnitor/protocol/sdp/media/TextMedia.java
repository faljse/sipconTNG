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
package se.omnitor.protocol.sdp.media;

import java.util.Vector;
import se.omnitor.protocol.sdp.SdpMedia;
import javax.sdp.Media;
import javax.sdp.SdpParseException;
import se.omnitor.protocol.sdp.Format;
import java.util.Locale;
import se.omnitor.protocol.sdp.MediaTemplate;
import se.omnitor.protocol.sdp.format.RedFormat;


public class TextMedia extends MediaTemplate {

	public TextMedia(String mediaType) {
		super(mediaType);
	}

	public TextMedia(String mediaType, int port, String protocol) {
		super(mediaType, port, protocol);
	}

	public TextMedia(Media media) throws SdpParseException {
		super(media);
	}


	public String negotiate(SdpMedia remoteMedia, SdpMedia[] resultMedia) {

		if (!remoteMedia.getProtocol().toUpperCase(Locale.US).equals
				(protocol.toUpperCase(Locale.US))) {

			return remoteMedia.getDecliningSdp();
		}

		String sdp = "";
		String payloadNumbers = "";

		Vector remoteFormats = remoteMedia.getFormats();
		int flen = remoteFormats.size();
		
		Vector<Format> tempFormats = new Vector<Format>(0, 1);
		int avoidNbr = -1;
		
		// Make sure RedFormat is always first in the list, regardless of what
		// order remote party wants.
		if (flen > 1) {
			for (int cnt=0; cnt<flen; cnt++) {
				if (remoteFormats.elementAt(cnt) instanceof RedFormat) {
					tempFormats.add((Format)remoteFormats.elementAt(cnt));
					avoidNbr = cnt;
				}
			}
			
			if (avoidNbr != -1) {
				for (int cnt=0; cnt<flen; cnt++) {
					if (cnt != avoidNbr) {
						tempFormats.add((Format)remoteFormats.elementAt(cnt));
					}
				}
			}
			
			remoteFormats = tempFormats;
		}
		
		Format remoteFormat;
		Format localFormat;
		Format resultFormat[];
		Vector<Format> resultFormats = new Vector<Format>(0, 1);

		for (int cnt=0; cnt<flen; cnt++) {
			remoteFormat = (Format)remoteFormats.elementAt(cnt);
			localFormat = getFormat(remoteFormat);

			if (localFormat != null) {
				payloadNumbers += " " + remoteFormat.getPayloadNumber();

				resultFormat = new Format[1];
				sdp += localFormat.negotiate(remoteFormat, resultFormat);
				if (resultFormat[0] != null) {
					resultFormats.add(resultFormat[0]);
				}
			}


		}

		if (payloadNumbers.equals("")) {
			return remoteMedia.getDecliningSdp();
		}

		String resultSdp = "m=" + mediaType + " " + port;

		if (portCount > 0) {
			resultSdp += "/" + portCount;
		}

		resultSdp += " " + protocol + payloadNumbers + "\r\n" + sdp;

		resultMedia[0] =
			new TextMedia(mediaType, remoteMedia.getPort(), protocol);
		((TextMedia)resultMedia[0]).setLocalPort(getPort());
		if (physicalPort != -1) {
			resultMedia[0].setPhysicalPort(physicalPort);
		}
		resultMedia[0].setFormats(resultFormats);

		return resultSdp;
	}

}