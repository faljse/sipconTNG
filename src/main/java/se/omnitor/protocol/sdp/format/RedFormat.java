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
package se.omnitor.protocol.sdp.format;

import se.omnitor.protocol.sdp.Format;
import se.omnitor.protocol.sdp.attribute.Rtpmap;
import se.omnitor.protocol.sdp.attribute.Fmtp;


public class RedFormat extends Format {

    private static final String NAME = "red";

    private Format format;
    private int generations;
    private int formatPayloadNumber;

    public RedFormat(int rtpPayloadNumber) {
	payloadNbr = rtpPayloadNumber;

	clockRate = 0;
	type = SEND_RECEIVE;
	ptime = 0;
	format = null;
	generations = 0;
	formatPayloadNumber = 0;
    }

    public RedFormat(Rtpmap rtpMap) {
	payloadNbr = rtpMap.getPayloadType();
	clockRate = rtpMap.getClockRate();

	type = SEND_RECEIVE;
	ptime = 0;
	format = null;
	generations = 0;
	formatPayloadNumber = 0;
    }

    public Format duplicate() {
	RedFormat rf = new RedFormat(payloadNbr);
	rf.setClockRate(clockRate);
	rf.setType(type);
	rf.setPtime(ptime);
	rf.setFormat(format);
	rf.setGenerations(generations);
	rf.setFormatPayloadNumber(formatPayloadNumber);

	return rf;
    }

    public void copyInfoFrom(Format f) {
	type = f.getType();
	ptime = f.getPtime();
    }

    public String getName() {
	return NAME;
    }

    /*
    public Format getResponse(SdpMedia inMedia) {
	return null;
    }
    */

    public void setInfo(Fmtp fmtp) {
	String nbrs[] = fmtp.getParameters().split("/");

	generations = nbrs.length;
	formatPayloadNumber = Integer.parseInt(nbrs[0]);
    }

    public void setInfo(Rtpmap rtpmap) {
	this.payloadNbr = rtpmap.getPayloadType();
	this.clockRate = rtpmap.getClockRate();
    }

    public void setGenerations(int generations) {
	this.generations = generations;
    }

    public int getGenerations() {
	return generations;
    }

    public void setFormat(Format format) {
	this.format = format;
	clockRate = format.getClockRate();
    }

    public Format getFormat() {
	return format;
    }

    protected void setFormatPayloadNumber(int nbr) {
	formatPayloadNumber = nbr;
    }

    public int getFormatPayloadNumber() {
	if (format == null) {
	    return formatPayloadNumber;
	}

	return format.getPayloadNumber();
    }

    /**
     * Gets SDP for this format.
     *
     * @return SDP
     */
    public String getSdp() {
	String sdp =
	    "a=rtpmap:" + payloadNbr + " " + NAME + "/" + clockRate + "\r\n";
	sdp += "a=fmtp:" + payloadNbr + " ";

	if (format != null) {
	    int fpnbr = format.getPayloadNumber();

	    for (int cnt=0; cnt<=generations; cnt++) {
		sdp += fpnbr;
		if (cnt < generations) {
		    sdp += "/";
		}
	    }

            /*
            for (int cnt=0; cnt<generations; cnt++) {
                sdp += fpnbr;
                if (cnt+1 < generations) {
                    sdp += "/";
                }
            }
            */
	}

	sdp += "\r\n";

	return sdp;
    }

    /**
     * Performs negotiation. Checks if the remote format's information is
     * OK and returns a response SDP. This function also returns a resulting
     * format, which should be handled locally when starting media.
     *
     * @param remoteFormat The remote's format
     * @param resultFormat This MUST be an initialized array with at least
     * one element. This function returns a resulting format in the first
     * element of this vector.
     *
     * @return Response SDP. Also see the "resultFormat" parameter.
     */
    public String negotiate(Format remoteFormat, Format[] resultFormat) {
	Format remoteRedFormat = ((RedFormat)remoteFormat).getFormat();

	if (remoteRedFormat instanceof CustomFormat) {

	    if (format instanceof CustomFormat) {
		if (remoteRedFormat.getPayloadNumber() !=
		    format.getPayloadNumber()) {

		    return "";
		}
	    }
	    else {
		return "";
	    }
	}
	else if (remoteRedFormat == null ||
	    !remoteRedFormat.getClass().getName().equals
	    (format.getClass().getName())) {

	    return "";
	}

	String sdp = "a=rtpmap:" + remoteFormat.getPayloadNumber() +
	    " " + NAME + "/" + clockRate + "\r\n";

	// Create a new format, link with the red format
	resultFormat[0] = new RedFormat(remoteFormat.getPayloadNumber());
	((RedFormat)resultFormat[0]).setFormat(remoteRedFormat);
	((T140Format)remoteRedFormat).setRedFormat((RedFormat)resultFormat[0]);

	// Set the number of redundant generations to the local value
	int fpnbr = remoteRedFormat.getPayloadNumber();
	int gen = generations;
	((RedFormat)resultFormat[0]).setGenerations(gen);
	if (generations > gen) {
	    gen = generations;
	}

	sdp += "a=fmtp:" + resultFormat[0].getPayloadNumber() + " ";
	
	for (int cnt=0; cnt<=gen; cnt++) {
	    sdp += fpnbr;
	    if (cnt < gen) {
		sdp += "/";
	    }
	}

	return sdp + "\r\n";
    }

    public String toString() {
	String str = super.toString();
	String fname = "(n/a)";
	if (format != null) {
	    fname = format.getName();
	}

	str += ", " +
	    "format=" + fname + ", " +
	    "gen=" + generations;

	return str;
    }



}
