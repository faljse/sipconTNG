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

/**
 * This class defines a custom format, which could be used if there is no
 * own class for the format to be used.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class CustomFormat extends Format {

    private String name;

    /**
     * Initializes.
     *
     * @param rtpPayloadNumber The RTP payload number to assign
     * @param name The MIME extension name to assign (eg. for "text/t140" this
     * name should be "t140")
     */
    public CustomFormat(int rtpPayloadNumber, String name) {
	this.payloadNbr = rtpPayloadNumber;
	this.name = name;

	clockRate = 0;
	type = SEND_RECEIVE;
	ptime = 0;
    }

    /**
     * Initializes.
     *
     * @param rtpPayloadNumber The RTP payload number to assign.
     * @param name The MIME extension name to assign (eg. for "text/t140" this
     * name should be "t140")
     * @param clockRate The clock rate to assign
     */
    public CustomFormat(int rtpPayloadNumber, String name, int clockRate) {
	this.payloadNbr = rtpPayloadNumber;
	this.name = name;
	this.clockRate = clockRate;

	type = SEND_RECEIVE;
	ptime = 0;
    }

    /**
     * Initializes
     *
     * @param rtpmap An Rtpmap object to fetch all information from
     */
    public CustomFormat(Rtpmap rtpmap) {
	payloadNbr = rtpmap.getPayloadType();
	name = rtpmap.getEncodingName();
	clockRate = rtpmap.getClockRate();

	type = SEND_RECEIVE;
	ptime = 0;
    }

    public void copyInfoFrom(Format f) {
	type = f.getType();
	ptime = f.getPtime();
    }

    public Format duplicate() {
	CustomFormat cf = new CustomFormat(payloadNbr, name, clockRate);
	cf.setType(type);
	cf.setPtime(ptime);

	return cf;
    }

    /**
     * Sets Rtpmap info
     *
     * @param rtpmap The Rtpmap object to fetch info from
     */
    public void setInfo(Rtpmap rtpmap) {
	this.name = rtpmap.getEncodingName();
	this.clockRate = rtpmap.getClockRate();
    }

    /**
     * Sets Fmtp info
     *
     * @param fmtp The Fmtp object to fetch info from
     */
    public void setInfo(Fmtp fmtp) {
    }

    /**
     * Gets the MIME extension name for this format, eg. for "text/t140" the
     * name will be "t140".
     *
     * @return The MIME extension name
     */
    public String getName() {
	return name;
    }

    /**
     * Does nothing at the moment.
     *
     * @return null
     */
    /*
    public Format getResponse(Format inFormat) {
	return null;
    }
    */


    /**
     * Gets SDP for this format
     *
     * @return SDP
     */
    public String getSdp() {
	String sdp = "a=rtpmap:" + payloadNbr + " " + name + "/" + clockRate;
	return sdp + "\r\n";
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
	String sdp = "a=rtpmap:" + remoteFormat.getPayloadNumber() + " " +
	    name + "/" + clockRate;

	resultFormat[0] = new CustomFormat(remoteFormat.getPayloadNumber(),
					   name,
					   clockRate);

	return sdp + "\r\n";
    }



}


