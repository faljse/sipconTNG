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


public class T140Format extends Format {

    private static final String NAME = "t140";

    private RedFormat redFormat;

    public T140Format(int rtpPayloadNumber) {
	payloadNbr = rtpPayloadNumber;

	clockRate = 1000;
	type = SEND_RECEIVE;
	ptime = 0;
	redFormat = null;
    }

    public T140Format(int rtpPayloadNumber, int clockRate) {
	payloadNbr = rtpPayloadNumber;
	this.clockRate = clockRate;

	type = SEND_RECEIVE;
	ptime = 0;
	redFormat = null;
    }

    public T140Format(Rtpmap rtpMap) {
	payloadNbr = rtpMap.getPayloadType();
	clockRate = rtpMap.getClockRate();

	type = SEND_RECEIVE;
	ptime = 0;
	redFormat = null;
    }

    public void copyInfoFrom(Format f) {
	type = f.getType();
	ptime = f.getPtime();
    }

    public Format duplicate() {
	T140Format tf = new T140Format(payloadNbr, clockRate);
	tf.setType(type);
	tf.setPtime(ptime);
	tf.setRedFormat(redFormat);

	return tf;
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
    }

    public void setInfo(Rtpmap rtpmap) {
	this.payloadNbr = rtpmap.getPayloadType();
	this.clockRate = rtpmap.getClockRate();
    }

    public String getSdp() {
	return "a=rtpmap:" + payloadNbr + " " + NAME + "/" + clockRate +
	    "\r\n";
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
	String sdp = "a=rtpmap:" + remoteFormat.getPayloadNumber() +
	    " " + NAME + "/" + clockRate;

	// Create a new format and link
	resultFormat[0] = new T140Format(remoteFormat.getPayloadNumber(),
					 clockRate);
	RedFormat rf = ((T140Format)remoteFormat).getRedFormat();
	if (rf != null && this.getRedFormat() != null) {
	    ((T140Format)resultFormat[0]).setRedFormat(rf);
	    rf.setFormat((T140Format)resultFormat[0]);
	}

	return sdp + "\r\n";
    }


    public void setRedFormat(RedFormat redFormat) {
	this.redFormat = redFormat;
    }

    public RedFormat getRedFormat() {
	return redFormat;
    }

    public boolean useRedundancy() {
	return redFormat != null;
    }

    public int getRedundancyPayloadType() {
	if (redFormat != null) {
	    return redFormat.getPayloadNumber();
	}

	return 0;
    }

    public int getRedundantGenerations() {
	if (redFormat != null) {
	    return redFormat.getGenerations();
	}

	return 0;
    }

    public String toString() {
	String str = super.toString();
	str +=
	    ", " +
	    (useRedundancy()? "use red": "no red") + ", " +
	    "redPt=" + getRedundancyPayloadType() + ", " +
	    "regGen=" + getRedundantGenerations();

	return str;
    }

}
