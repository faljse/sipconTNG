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
package se.omnitor.tipcon1.rtp;

import java.util.Locale;

/**
 * This class contains data about all RTP payload types
 * defined by IANA as of 2003-05-16.
 *
 * @author Erik Zetterstrm, Omnitor AB
 */
public class RTPPayloadConstants {

    /**
     * Defines a apyload type to be dynamic.
     */
    public static final int DYNAMIC=96;

    /**
     * Defines a channel or sample frequency to be variable.
     */
    public static final int VARIABLE=128;

    private RTPPayload[] rtpPayloads = {
	new RTPPayload(0,"PCMU","A",8000,1, "PCMU"),
	new RTPPayload(0,"ULAW","A",8000,1, "PCMU"),
	new RTPPayload(3,"GSM","A",8000,1, "GSM"),
	new RTPPayload(4,"G723","A",8000,1, "G723"),
	new RTPPayload(5,"DVI4","A",8000,1, "DVI4"),
	new RTPPayload(5,"DVI","A",8000,1, "DVI4"),
	new RTPPayload(6,"DVI4","A",16000,1, "DVI4"),
	new RTPPayload(6,"DVI","A",16000,1, "DVI4"),
	new RTPPayload(7,"LPC","A",8000,1, "LPC"),
	new RTPPayload(8,"PCMA","A",8000,1, "PCMA"),
	new RTPPayload(8,"ALAW","A",8000,1, "PCMA"),
	new RTPPayload(9,"G722","A",8000,1, "G722"),
	new RTPPayload(10,"L16","A",44100,2, "L16"),
	new RTPPayload(11,"L16","A",44100,1, "L16"),
	new RTPPayload(12,"QCELP","A",8000,1, "QCELP"),
	new RTPPayload(13,"CN","A",8000,1, "CN"),
	new RTPPayload(14,"MPA","A",90000,VARIABLE, "MPA"),
	new RTPPayload(14,"MPEGAUDIO","A",90000,VARIABLE, "MPA"),
	new RTPPayload(15,"G728","A",8000,1, "G728"),
	new RTPPayload(16,"DVI4","A",11025,1, "DVI4"),
	new RTPPayload(16,"DVI","A",11025,1, "DVI4"),
	new RTPPayload(17,"DVI4","A",22050,1, "DVI4"),
	new RTPPayload(17,"DVI","A",22050,1, "DVI4"),
	new RTPPayload(18,"G729","A",8000,1, "G729"),
	new RTPPayload(25,"CELB","V",90000,0, "CelB"),
	new RTPPayload(26,"JPEG","V",90000,0, "JPEG"),
	new RTPPayload(28,"NV","V",90000,0, "nv"),
	new RTPPayload(31,"H261","V",90000,0, "H261"),
	new RTPPayload(32,"MPV","V",90000,0, "MPV"),
	new RTPPayload(33,"MP2T","AV",90000,0, "MP2T"),
	new RTPPayload(34,"H263","V",90000,0, "H263"),
	new RTPPayload(DYNAMIC,"GSM-HR","A",8000,1, "GSM-HR"),
	new RTPPayload(DYNAMIC,"BT656","V",90000,0, "BT656"),
	new RTPPayload(DYNAMIC,"MP1S","V",90000,0, "MP1S"),
	new RTPPayload(DYNAMIC,"MP2P","V",90000,0, "MP2P"),
	new RTPPayload(DYNAMIC,"BMPEG","V",90000,0, "BMPEG"),
	new RTPPayload(DYNAMIC,"G726-40","A",8000,1, "G726-40"),
	new RTPPayload(DYNAMIC,"G726-32","A",8000,1, "G726-32"),
	new RTPPayload(DYNAMIC,"G726-24","A",8000,1, "G726-24"),
	new RTPPayload(DYNAMIC,"G726-16","A",8000,1, "G726-16"),
	new RTPPayload(DYNAMIC,"G729D","A",8000,1, "G729D"),
	new RTPPayload(DYNAMIC,"G729E","A",8000,1, "G729E"),
	new RTPPayload(DYNAMIC,"GSM-EFR","A",8000,1, "GSM-EFR"),
	new RTPPayload(DYNAMIC,"L8","A",VARIABLE,VARIABLE, "L8"),
	new RTPPayload(DYNAMIC,"VDVI","A",VARIABLE,1, "VDVI"),
	new RTPPayload(DYNAMIC,"RED","VARIABLE",VARIABLE,VARIABLE, "RED"),
	new RTPPayload(98,"T140", "T", 1000, 0, "T140")
	    // Allan eC requires T140 to have payload type 98!
	    };


    /**
     * Returns the payload type of a specfied codec.
     *
     * @param codec The specified codec.
     *
     * @return The corresponding payload type.
     *         If several payload types exist for the given codec
     *         the one with lowest payload number is returned.
     *         -1 if no corresponding payload type is found.
     */
    public int getPayloadType(String codec) {
        for (int i=0;i<rtpPayloads.length;i++) {
            if (rtpPayloads[i].getName().equals(codec.toUpperCase(Locale.US))) {
                return rtpPayloads[i].getPayloadNumber();
            }
        }
        return -1;
    }

    /**
     * Returns the payload type of the specfied codec and frequency.
     *
     * @param codec The specified codec.
     * @param hz    The specfied frequency.
     *
     *  @return The corresponding payload type.
     *          -1 if no corresponding payload type is found.
     */
    public int getPayloadType(String codec,int hz) {
        for (int i=0;i<rtpPayloads.length;i++) {
            if (rtpPayloads[i].getName().equals(codec.toUpperCase(Locale.US)) &&
                rtpPayloads[i].getClockRate()==hz) {
                return rtpPayloads[i].getPayloadNumber();
            }
        }
        return -1;
    }

    /**
     * Returns the clock rate of the specified codec.
     *
     * @param codec The specified codec.
     *
     * @return The corresponding clock rate.
     *         -1 if no corresponding clock rate was found.
     */
    public long getClockRate(String codec) {
        for (int i=0;i<rtpPayloads.length;i++) {
            if (rtpPayloads[i].getName().equals(codec.toUpperCase(Locale.US))) {
                return rtpPayloads[i].getClockRate();
            }
        }
        return -1;
    }

    /**
     * Returns the name of the codec to be used in SDP.
     *
     * @param codecName The name of a codec
     *
     * @return The correct SDP name of the given codec. An empty string
     * if codec was not found.
     */
    public String getSdpName(String codecName) {
        int i;

        for (i=0; i<rtpPayloads.length; i++) {
            if (rtpPayloads[i].getName().equals(codecName.toUpperCase(Locale.US))) {
                return rtpPayloads[i].getSdpName();
            }
        }

        return "";
    }

    /**
     * This class holds information about a payload type.
     *
     * @author Erik Zetterstrom, Omnitor AB
     */
    class RTPPayload {

        private int payloadNumber;
        private String name;
        private String type;
        private long clockRate;
        private int channels;
        private String sdpName;


        /**
         * Constructor
         *
         * @param payloadNumber The payload number of this payload type.
         * @param name        The name of this payload type.
         * @param type        The type of payload (A=audio, V=video or both AV)
         * @param clockRate   The clock rate of this payload type.
         * @param channels    The number of channels used by this payload type.
         * @param sdpName     The codec name to be used in SDP
         */
        public RTPPayload(int payloadNumber,
                          String name,
                          String type,
                          long clockRate,
                          int channels,
                          String sdpName) {
            this.payloadNumber=payloadNumber;
            this.name=name;
            this.type=type;
            this.clockRate=clockRate;
            this.channels=channels;
            this.sdpName = sdpName;
        }

        /**
         * Returns the payload number of this payload type.
         *
         * @return The paylaod number.
         */
        public int getPayloadNumber() {
            return payloadNumber;
        }

        /**
         * Returns the name of this payload type.
         *
         * @return The name of this paylaod type.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the media type of this payload type (A,V or AV).
         *
         * @return The media type of this payload type (A,V or AV).
         */
        public String getType() {
            return type;
        }

        /**
         * Returns the clockrate of this payload type.
         *
         * @return The clock rate of this payload type.
         */
        public long getClockRate() {
            return clockRate;
        }

        /**
         * Returns the number of channels used by this payload type.
         *
         * @return The number of channels used by this payload type.
         */
        public int getChannels() {
            return channels;
        }

        /**
         * Returns the name to be used in SDP.
         *
         * @return The name to be used in SDP.
         */
        public String getSdpName() {
            return sdpName;
        }

    }

}
