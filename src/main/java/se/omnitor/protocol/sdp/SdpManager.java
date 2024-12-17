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
import java.util.Hashtable;
import javax.sdp.*;

import se.omnitor.protocol.sdp.media.*;
import se.omnitor.protocol.sdp.attribute.*;
import se.omnitor.protocol.sdp.format.*;
import gov.nist.javax.sdp.*;

// import LogClasses and Classes
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SdpManager {

    private static final String VERSION = "v1.1";

    public Vector<SdpMedia> mediaVector;
    private SdpFactory sdpFactory;

    private String username;
    private String sessId;
    private String sessVersion;
    private String netType;
    private String addrType;
    private String unicastAddress;

    // declare package and classname
    public final static String CLASS_NAME = SdpManager.class.getName();
    // get an instance of Logger
    private static Logger logger = Logger.getLogger(CLASS_NAME);


    public SdpManager(String username,
		      String sessId,
		      String sessVersion,
		      String netType,
		      String addrType,
		      String unicastAddress) throws SdpException {

	this.username = username;
	this.sessId = sessId;
	this.sessVersion = sessVersion;
	this.netType = netType;
	this.addrType = addrType;
	this.unicastAddress = unicastAddress;

	mediaVector = new Vector<SdpMedia>(0, 1);
	sdpFactory = SdpFactory.getInstance();

    }

    /*
    public String getResponseSdp(String inSdp)
	throws SdpParseException, SdpException {

	SessionDescription sd = sdpFactory.createSessionDescription(inSdp);
	Vector mdVector = sd.getMediaDescriptions(true);
	int mdvLen = mdVector.size();
	MediaDescription md;
	Media tempMedia;
	boolean useMedia;

	Vector returnMediaVector = new Vector(0, 1);


	for (int cnt=0; cnt<mdvLen; cnt++) {
	    md = (MediaDescription)mdVector.elementAt(cnt);

	    useMedia = false;

	    String transport = md.getMedia().getProtocol().toUpperCase();

	    if (transport.equals("RTP/AVP")) {

		tempMedia = getMedia(md.getMedia().getMediaType());
		if (tempMedia != null) {
    */
		    /*
		    returnMediaVector.add
			(tempMedia.getResultMediaDescription(md));
		    */
    /*
		    useMedia = true;
		}
	    }

	    if (!useMedia) {
		returnMediaVector.add(sdpFactory.createMediaDescription(md.getMedia().getMediaType(), 0, 0, transport, new int[] { 1, 2 }));
	    }

	}


	// Continue here ..

	return "";
    }
    */

    public SdpMedia getMedia(String type) {
	type = type.toUpperCase(Locale.US);

	int mvLen = mediaVector.size();

	for (int cnt=0; cnt<mvLen; cnt++) {
	    if (type.equals(((SdpMedia)mediaVector.elementAt(cnt)).
			    getType().toUpperCase())) {

		return (SdpMedia)mediaVector.elementAt(cnt);
	    }
	}

	return null;
    }

    public String negotiate(String sdpIn, Vector<SdpMedia> media) throws SdpException {
	String sdpOut = getGlobalSdp();

	Vector mediaList = getFormats(sdpIn);
	int mlen = mediaList.size();

	SdpMedia remoteMedia;
	SdpMedia localMedia;
	SdpMedia[] resultMedia;

	for (int cnt=0; cnt<mlen; cnt++) {

	    remoteMedia = (SdpMedia)mediaList.elementAt(cnt);

	    localMedia = getMedia(remoteMedia.getType());

	    if (localMedia != null) {
		resultMedia = new SdpMedia[1];
		sdpOut += localMedia.negotiate(remoteMedia, resultMedia);
		if (resultMedia[0] != null) {
		    ((SdpMedia)resultMedia[0]).
			setRemoteIp(remoteMedia.getRemoteIp());
		    media.add(resultMedia[0]);
		}

	    }
	    else {
		sdpOut += remoteMedia.getDecliningSdp();
	    }

	}
        System.out.println("Negotiated SDP:\n" + sdpOut);
	return sdpOut;
    }



    /**
     * Gets all Media objects to use in the session, collected in a Vector.
     *
     * @param inSdp Information about the session
     *
     * @return Media to use in a session
     */
    public Vector getMediaToUse(String inSdp) {
	return new Vector(0, 1);
    }

    public void addMedia(SdpMedia media) {
	mediaVector.add(media);
    }

    private String getGlobalSdp() {
	String sdp = "";

	sdp =
	    "v=0\r\n" +
	    "o=" + username + " " + sessId + " " + sessVersion + " " +
	    netType + " " + addrType + " " + unicastAddress + "\r\n" +
	    "s=Omnitor_SDP_" + VERSION + "\r\n" +
	    "c=" + netType + " " + addrType + " " + unicastAddress + "\r\n" +
	    "t=0 0\r\n";

	return sdp;
    }


    public String getSdp() {

	String sdp = getGlobalSdp();

	int mlen = mediaVector.size();

	for (int cnt=0; cnt<mlen; cnt++) {
	    sdp += ((SdpMedia)mediaVector.elementAt(cnt)).getSdp();
	}

	return sdp;
    }

    /**
     * Checks the SDP and points out which media and which formats that should
     * be used in a session.
     *
     * @param sdp The SDP in the 200 OK packet
     */

    public static Vector<SdpMedia> getFormats(String sdp) throws SdpException {

        // write methodname
        final String METHOD = "getFormats(String sdp) throws SdpException";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD, sdp);

        String remoteIp = null;
        SessionDescription sd = null;

        try {
            SdpFactory sf = SdpFactory.getInstance();
            sd = sf.createSessionDescription(sdp);
            if (sd.getConnection()==null) {
                System.out.println(
                        "No session connection availible, use media connection!");
                Vector stofe = sd.getMediaDescriptions(false);
                MediaDescriptionImpl mdi = null;
                for (int i = 0; i < stofe.size(); i++) {
                    mdi = (MediaDescriptionImpl)stofe.elementAt(i);

                    //This is not completely right but good enough, we assume that
                    //remote medias should be opened to the same remote ip
                    if (mdi.getMedia().getMediaPort() != 0)
                        remoteIp = mdi.getConnection().getAddress();

                }
                System.out.println("Remote connection ip is: " + remoteIp);
            } else {
                //session connection was found
                remoteIp = sd.getConnection().getAddress();
            }
        }
        catch(Throwable t) {
            logger.logp(Level.SEVERE, CLASS_NAME, METHOD, "error creating sdp", sdp);

            // Parser error
            return null;
        }

	Vector mdList = sd.getMediaDescriptions(true);

	int mdLen = mdList.size();
	SdpMedia sdpMedia;
	MediaDescription md;
	SdpAttribute attrib;
	Hashtable<String, Format> table;

	Vector<SdpMedia> mediaList = new Vector<SdpMedia>(0, 1);

	for (int mcnt=0; mcnt<mdLen; mcnt++) {
	    table = new Hashtable<String, Format>(0, 1);
	    md = (MediaDescription)mdList.elementAt(mcnt);
	    Media media = md.getMedia();


	    if (media.getMediaPort() != 0) {

		// Create media

	    if (media.getMediaType().equals("text")) {
	    	sdpMedia = new TextMedia(media);
	    }
	    else {
	    	sdpMedia = new CustomMedia(media);
	    }
		((SdpMedia)sdpMedia).setRemoteIp(remoteIp);

		Vector mediaFormats = md.getMedia().getMediaFormats(true);
		int mfLen = mediaFormats.size();
		for (int mfcnt=0; mfcnt<mfLen; mfcnt++) {
		    String mfStr = (String)mediaFormats.elementAt(mfcnt);
		    table.put(mfStr,
			      Format.getFormat(Integer.parseInt(mfStr)));
		}

		Vector attribList = md.getAttributes(true);
		int attribLen = attribList.size();

		for (int acnt=0; acnt<attribLen; acnt++) {
		    attrib = AttributeParser.parse((Attribute)attribList.
						   elementAt(acnt));

		    switch (attrib.getType()) {
		    case SdpAttribute.RTPMAP:
			Format rtpmapF =
			    (Format)table.get(((Rtpmap)attrib).
					      getPayloadType()+"");
			if (rtpmapF instanceof CustomFormat) {
			    Format newf = Format.getFormat((Rtpmap)attrib);
			    newf.copyInfoFrom(rtpmapF);
			    table.put(((Rtpmap)attrib).getPayloadType()+"",
				      newf);
			}
			else {
			    rtpmapF.setInfo((Rtpmap)attrib);
			}
			break;
		    case SdpAttribute.FMTP:
			Format fmtpF =
			    (Format)table.get(((Fmtp)attrib).
					      getFormat()+"");
			fmtpF.setInfo((Fmtp)attrib);
			break;
		    default:
		    }
		}

		Vector<Object> fv = new Vector<Object>(0, 1);
		for (int mfcnt=0; mfcnt<mfLen; mfcnt++) {
		    String mfStr = (String)mediaFormats.elementAt(mfcnt);
		    Format fmt = (Format)table.get(mfStr);

			if (fmt instanceof RedFormat) {

			Format f = (Format)table.get(((RedFormat)fmt).
						     getFormatPayloadNumber()+"");

			if (f != null && f instanceof T140Format) {
			    ((T140Format)f).setRedFormat((RedFormat)fmt);
			    ((RedFormat)fmt).setFormat((T140Format)f);

			}
		    }

		    fv.add(fmt);

		}

		sdpMedia.setFormats(fv);
		mediaList.add(sdpMedia);
	    }

	}
        logger.exiting(CLASS_NAME, METHOD, mediaList);
	return mediaList;
    }





    public static void main(String argv[]) {

	try {
	    SdpManager sdpm = new SdpManager("Andreas_Piirimets",
					     "1",
					     "1",
					     "IN",
					     "IP4",
					     "192.168.1.30");
	    SdpMedia sdpMedia;
	    Format format;
	    Vector<Format> formats;

	    sdpMedia = new CustomMedia("audio", 6000, "RTP/AVP");

	    formats = new Vector<Format>(0, 1);
	    format = Format.getFormat(8);
	    formats.add(format);
	    format = Format.getFormat(0);
	    formats.add(format);
	    sdpMedia.setFormats(formats);

	    sdpm.addMedia(sdpMedia);


	    sdpMedia = new TextMedia("text", 11000, "RTP/AVP");

	    formats = new Vector<Format>(0, 1);
	    RedFormat rformat = new RedFormat(99);
	    rformat.setGenerations(2);
	    T140Format tformat = new T140Format(98);
	    rformat.setFormat(tformat);
	    tformat.setRedFormat(rformat);
	    formats.add(rformat);
	    formats.add(tformat);

	    sdpMedia.setFormats(formats);

	    sdpm.addMedia(sdpMedia);


	    sdpMedia = new CustomMedia("video", 8000, "RTP/AVP");

	    formats = new Vector<Format>(0, 1);
	    format = Format.getFormat(31);
	    formats.add(format);
	    format = Format.getFormat(34);
	    formats.add(format);

	    sdpMedia.setFormats(formats);

	    sdpm.addMedia(sdpMedia);

	} catch (SdpException e) {
		logger.throwing("se.omnitor.protocol.sdp.SdpManager", "main", e);
	}

    }

}
