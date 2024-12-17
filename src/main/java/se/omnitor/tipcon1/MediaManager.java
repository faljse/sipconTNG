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
package se.omnitor.tipcon1;

import java.awt.Component;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;
import javax.media.rtp.InvalidSessionAddressException;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SessionAddress;
import javax.media.protocol.DataSource;
import se.omnitor.protocol.rtp.RtpTextTransmitter;
import se.omnitor.protocol.rtp.RtpTextReceiver;
import se.omnitor.protocol.rtp.Session;
import se.omnitor.protocol.rtp.text.SyncBuffer;
import se.omnitor.protocol.sdp.SdpMedia;
import se.omnitor.protocol.sdp.Format;
import se.omnitor.protocol.sdp.format.T140Format;
import se.omnitor.tipcon1.gui.ProgramWindow;
import se.omnitor.util.FifoBuffer;
import se.omnitor.tipcon1.rtp.AudioVideoTransmit;
import se.omnitor.tipcon1.rtp.AudioVideoReceive;

/**
 * The start-up class for T-Client.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class MediaManager {

    private Vector media;
    private AppController ac;
    private AppSettings appSettings;

    private RTPManager audioManager;
    private RTPManager videoManager;
    private Session rtpSession;
    private boolean rtpSessionCreated;

    private RtpTextTransmitter rtpTextTransmitter;
    private RtpTextReceiver rtpTextReceiver;
    private AudioVideoTransmit audioTransmit;
    private AudioVideoReceive audioReceive;
    private AudioVideoTransmit videoTransmit;
    private AudioVideoReceive videoReceive;

    private boolean useAudio;
   private boolean useVideo;

    private SyncBuffer txTextBuffer;
    private FifoBuffer rxTextBuffer;

    private String audioDescriptor;
    private String videoDescriptor;

    private int localVideoPort = 0;
    private int localAudioPort = 0;
    private int localTextPort = 0;
    private int bitrate = 0;
    private int fps = 0;

    private Vector<MediaStarter> mediaStarters;

    private boolean allStopped;

    private Component audioTxControlPanel;

    private InetAddress ipAddr;
    private SessionAddress localVideoAddr;
    private SessionAddress localAudioAddr;
    private SessionAddress destAudioAddr;
    private SessionAddress destVideoAddr;
    private DataSource transmitVideoDataSource;

    private boolean isEconf351 = false;

    // declare package and classname
    public final static String CLASS_NAME = MediaManager.class.getName();
    // get an instance of Logger
    private static Logger logger = Logger.getLogger(CLASS_NAME);


    //private MulticastSocket textSocket;



    public MediaManager(AppController ac,
                        AppSettings appSettings,
                        Vector negotiatedMedia,
                        SyncBuffer txTextBuffer,
                        FifoBuffer rxTextBuffer,
                        String audioDescriptor,
                        String videoDescriptor,
                        boolean aEconf351,
                        DataSource transmitVideoDataSource,
                        int bitrate,
                        int fps) {
        this.fps = fps;
        this.bitrate = bitrate;
        this.ac = ac;
        this.transmitVideoDataSource = transmitVideoDataSource;
        // write methodname
        final String METHOD = "MediaManager(AppController ac, ......)";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD, new Object[] {ac, negotiatedMedia,
                      audioDescriptor, videoDescriptor});

        media = negotiatedMedia;
        this.txTextBuffer = txTextBuffer;
        this.rxTextBuffer = rxTextBuffer;
        this.audioDescriptor = audioDescriptor;
        this.videoDescriptor = videoDescriptor;
        /*
        this.localVideoPort = localVideoPort;
        this.localAudioPort = localAudioPort;
        this.localTextPort = localTextPort;
        */
        this.isEconf351 = aEconf351;
        this.appSettings = appSettings;

        // Get all physical ports and media to use
        useVideo = false;
        useAudio = false;
        int mlen = media.size();
        for (int mcnt = 0; mcnt < mlen; mcnt++) {
            SdpMedia sdpMedia = (SdpMedia) media.elementAt(mcnt);

            String type = sdpMedia.getType().toLowerCase(Locale.US);

            if (type.equals("text")) {
                localTextPort = sdpMedia.getPhysicalPort();
            }
            else if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && type.equals("video")) {
                localVideoPort = sdpMedia.getPhysicalPort();
                useVideo = true;
            }
            else if (type.equals("audio")) {
                localAudioPort = sdpMedia.getPhysicalPort();
                useAudio = true;
            }
        }
        mediaStarters = new Vector<MediaStarter>(0, 1);

        allStopped = false;


        InetAddress localInetAddr = null;
        try {
        	localInetAddr = GetInetAddr(appSettings.getNetIfMacAddr());
        	if(null == localInetAddr){
        		localInetAddr = java.net.InetAddress.getLocalHost();
        	}
        } catch (Exception e) {
            System.out.println(
                    "MediaManager: Could not determine local address: " + e);
        }
        System.out.println("Local addr: " + localInetAddr);
       
        if (useAudio) {
            localAudioAddr = new SessionAddress(localInetAddr, localAudioPort);
        }
        if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && useVideo) {
            localVideoAddr = new SessionAddress(localInetAddr, localVideoPort);
        }
        //localInetAddr,localAudioPort+1);


        rtpSession = null;
        rtpSessionCreated = false;

        logger.exiting(CLASS_NAME, METHOD);
    }

    public void startAll() {

        int mlen = media.size();
        SdpMedia sdpMedia;
        Vector formats;
        se.omnitor.protocol.sdp.Format format;
        String type;
        MediaStarter ms;

        if (useAudio) {
            audioManager = (RTPManager) RTPManager.newInstance();
        }
       if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && useVideo) {
            videoManager = (RTPManager) RTPManager.newInstance();
        }
        try {
            if (useAudio) {
                audioManager.initialize(localAudioAddr);
            }
            if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && useVideo) {
                videoManager.initialize(localVideoAddr);
            }
        } catch (InvalidSessionAddressException e) {
        	logger.throwing(this.getClass().getName(), "startAll", e);
		} catch (IOException e) {
        	logger.throwing(this.getClass().getName(), "startAll", e);
		}

        for (int mcnt = 0; mcnt < mlen; mcnt++) {
            sdpMedia = (SdpMedia) media.elementAt(mcnt);

            formats = sdpMedia.getFormats();

            if (formats.size() > 0) {
                format = (se.omnitor.protocol.sdp.Format) formats.elementAt(0);

                type = sdpMedia.getType().toLowerCase(Locale.US);

                if (type.equals("text")) {
                    /*if(rtpSession==null) {
                        rtpSession = new Session(sdpMedia.getRemoteIp(),64000);
                           }
                           rtpSession.start(localTextPort,
                       localTextPort+1,
                       sdpMedia.getPort(),
                       sdpMedia.getPort()+1);*/

                    // Sets the format to RedFormat if present. Needed for T140
                    // dynamic payload to operate properly
                    if (format.getName().equalsIgnoreCase("T140")) {
                        int redPl = ((T140Format)format).getRedundancyPayloadType();
                        if (redPl > 0) {
                            for (int i=0; i<formats.size(); i++)
                                if (formats.get(i) instanceof se.omnitor.protocol.sdp.format.RedFormat) {
                                    format = (Format)formats.get(i);
                                }
                        }
                    }

                    ms = new MediaStarter(MediaStarter.TEXT_IN,
                                          sdpMedia, format);
                    ms.start();
                    mediaStarters.add(ms);

                    ms = new MediaStarter(MediaStarter.TEXT_OUT,
                                          sdpMedia, format);
                    ms.start();
                    mediaStarters.add(ms);
                }

                else if (type.equals("audio")) {
                    ms = new MediaStarter(MediaStarter.AUDIO_IN,
                                          sdpMedia, format);
                    ms.start();
                    mediaStarters.add(ms);

                    ms = new MediaStarter(MediaStarter.AUDIO_OUT,
                                          sdpMedia, format);
                    ms.start();
                    mediaStarters.add(ms);
                }

                else if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && type.equals("video")) {
                    ms = new MediaStarter(MediaStarter.VIDEO_IN,
                                          sdpMedia, format);
                    ms.start();
                    mediaStarters.add(ms);

                    ms = new MediaStarter(MediaStarter.VIDEO_OUT,
                                          sdpMedia, format);
                    ms.start();
                    mediaStarters.add(ms);
                }

            }
        }

    }

    /**
     * Stops all media.
     *
     */
    public void stopAll() {

        allStopped = true;

        int len = mediaStarters.size();
        for (int cnt = 0; cnt < len; cnt++) {
            ((MediaStarter) mediaStarters.elementAt(cnt)).stop();
        }

        if (rtpSession != null) {
            rtpSession.stop();
            rtpSession = null;
        }

        if (rtpTextTransmitter != null) {
            rtpTextTransmitter.stop();
            rtpTextTransmitter = null;
        }

        if (audioTransmit != null) {
            audioTransmit.stop();
            audioTransmit = null;
            audioTxControlPanel = null;
        }

        if (rtpTextReceiver != null) {
            rtpTextReceiver.stop();
            rtpTextReceiver = null;
        }

        if (audioReceive != null) {
            audioReceive.stop(false);
            audioReceive = null;
        }

        if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && videoReceive != null) {
            videoReceive.stop(false);
            videoReceive = null;
        }

        if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && videoTransmit != null) {
            videoTransmit.stop();
            videoTransmit = null;
            //videoTxControlPanel = null;
        }
    }


    /**
     * Starts text transmission.
     *
     */
    private void startTextOut(SdpMedia media, Format format) {
        createRTPSession(media.getRemoteIp());

        rtpSession.openRTPTransmitSocket(localTextPort, media.getPort());

        rtpSession.createAndStartRTCPSenderThread(localTextPort + 1,
                                                  media.getPort() + 1);
        ac.getProgramWindow().
                changeStatusLabel(ProgramWindow.LABEL_TEXT_OUT, "Trying");

        txTextBuffer.empty();

        boolean useT140Red = false;
        boolean useRed = false;
        int redT140Gen = 0;
        int t140Pt = 0;
        int redPt = 0;
        int redGen = 0;

        if (format instanceof T140Format) {
            useT140Red = ((T140Format) format).useRedundancy();
            //redPt = ((T140Format) format).getRedundancyPayloadType();
            redT140Gen = ((T140Format) format).getRedundantGenerations();
            t140Pt = ((T140Format) format).getPayloadNumber();
        }
        else if (format instanceof se.omnitor.protocol.sdp.format.RedFormat) {
            useRed = true;
            redGen = ((se.omnitor.protocol.sdp.format.RedFormat) format).
                     getGenerations();
            redPt = ((se.omnitor.protocol.sdp.format.RedFormat) format).getPayloadNumber();
            t140Pt = ((se.omnitor.protocol.sdp.format.RedFormat) format).getFormatPayloadNumber();
        }

        if (allStopped) {
            return;
        }

        rtpTextTransmitter =
                new RtpTextTransmitter(rtpSession,
                                       false,
                                       media.getRemoteIp(),
                                       localTextPort, //media.getPort() + 2,
                                       media.getPort(),
                                       t140Pt,
                                       useRed, // Red flag
                                       redPt, // Red pt
                                       redGen, // Red gens
                                       useT140Red, // T.140 red flag
                                       redT140Gen, // T.140 red gens
                                       txTextBuffer, isEconf351);

        rtpTextTransmitter.setCName(appSettings.getUserRealName());
        rtpTextTransmitter.setEmail("sip:" + appSettings.getPrimarySipAddress());

        if (allStopped) {
            return;
        }

        // The text receiver can inform about whether the remote
        // receiver is running. Therefore, we wait for the receiver.
        int waitingTime = 10;
        while (rtpTextReceiver == null && waitingTime > 0) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                return;
            }

            waitingTime--;
        }

        if (allStopped) {
            return;
        }

        // Now, wait for the receiver to receive a report from the
        // remote receiver before we start out transmitter.
        if (rtpTextReceiver != null) {
            rtpTextReceiver.waitForRemoteReceiver(5);
        }

        if (allStopped) {
            return;
        }

        //if (sc.getState() == SipController.IS_ESTABLISHED) {
        if (rtpTextTransmitter != null) {
            rtpTextTransmitter.start();
            ac.getProgramWindow().changeStatusLabel(ProgramWindow.
                    LABEL_TEXT_OUT,
                    "T.140");
            ac.getT140Panel().getLocalTextArea().setEditable(true);
            ac.getT140Panel().getLocalTextArea().setActiveLook(true);

            logger.finest("Text transmission started.");
        }

        //}

    }

    /**
     * To preevent both the sending and receiving thread from creating
     * separate RTP session this synchronized method is used.
     *
     * @param ip The ip address to use.
     */
    private synchronized void createRTPSession(String ip) {
        if (!rtpSessionCreated) {
            rtpSession = new Session(ip, 64000, localTextPort);
            rtpSessionCreated = true;
        }
    }

    private void startTextIn(SdpMedia media, Format format) {

        boolean useRed = false;
        int t140Pt = 0;
        int redPt = 0;

        createRTPSession(media.getRemoteIp());

        rtpSession.openRTPReceiveSocket(localTextPort);
        rtpSession.startRTPThread();
        rtpSession.createAndStartRTCPReceiverThread(localTextPort + 1);

        if (allStopped) {
            return;
        }

        if (format instanceof T140Format) {
            t140Pt = ((T140Format) format).getPayloadNumber();
            /*System.err.println("format instanceof T140Format");
            System.err.println("useRed " + useRed);
            System.err.println("redPt " + redPt);
            System.err.println("getPayloadNumber() -> " + format.getPayloadNumber());*/
        } else if (format instanceof se.omnitor.protocol.sdp.format.RedFormat) {
            useRed = true;
            redPt = ((se.omnitor.protocol.sdp.format.RedFormat) format).getPayloadNumber();
            t140Pt = ((se.omnitor.protocol.sdp.format.RedFormat) format).getFormatPayloadNumber();
            /*System.err.println("format instanceof RedFormat");
            System.err.println("useRed " + useRed);
            System.err.println("redPt " + redPt);
            System.err.println("getPayloadNumber() -> " + format.getPayloadNumber()); */

        }

        rtpTextReceiver =
                new RtpTextReceiver(rtpSession,
                                    media.getRemoteIp(),
                                    localTextPort,
                                    useRed, // Red flag
                                    t140Pt,//format.getPayloadNumber(),
                                    redPt, // Red payload type
                                    rxTextBuffer);

        if (allStopped) {
            return;
        }

        rtpTextReceiver.setCName(appSettings.getUserRealName());
        rtpTextReceiver.setEmail("sip:" + appSettings.getPrimarySipAddress());
        rtpTextReceiver.start();

        // Now, wait for the receiver to receive a report from the
        // remote receiver before we start out transmitter.
        /*
          if (rtpTextReceiver != null) {
            rtpTextReceiver.waitForLocalReceiver(20);
          }
         */

        if (!allStopped) {
            ac.getProgramWindow().changeStatusLabel(ProgramWindow.
                    LABEL_TEXT_IN,
                    "T.140");
            ac.getT140Panel().getRemoteTextArea().setActiveLook(true);
        }

    }


    private void startAudioOut(SdpMedia media, Format format) {

        try {
            ipAddr = InetAddress.getByName(media.getRemoteIp());
            destAudioAddr = new SessionAddress(ipAddr, media.getPort());
            audioManager.addTarget(destAudioAddr);
        } catch (InvalidSessionAddressException isae) {
            System.err.println(
                    "MediaManager, error adding destination to RTPManager. " +
                    isae);
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (allStopped) {
            return;
        }

        // Initialize and start transmitter
        System.out.println("Starting audiotransmit. remoteport: " +
                           media.getPort());
        audioTransmit = new AudioVideoTransmit(this,
                                               audioManager,
                                               null,
                                               audioDescriptor,
                                               media.getRemoteIp(),
                                               localAudioPort, //media.getPort(),
                                               media.getPort(),
                                               format.getName(),
                                               ac.getProgramWindow().
                                               getAudioLocalPanel(),
                                               0,//bitrate
                                               0); //fps

        if (allStopped) {
            return;
        }

        String errorMsg = audioTransmit.start();

        // If success
        if (errorMsg == null) {

            // Inform user
            if (!allStopped) {
                ac.getProgramWindow().changeStatusLabel
                        (ProgramWindow.LABEL_AUDIO_OUT, format.getName());

                audioTxControlPanel = audioTransmit.getControlPanel();
            }

        }

        // If failiure
        else if (!allStopped) {
            logger.warning("Error starting audio: " + errorMsg);

            // Inform user
            ac.getProgramWindow().changeStatusLabel
                    (ProgramWindow.LABEL_AUDIO_OUT, "Failed");
        }

    }


   private void startVideoOut(SdpMedia media, Format format) {

        try {
            ipAddr = InetAddress.getByName(media.getRemoteIp());
            destVideoAddr = new SessionAddress(ipAddr, media.getPort());
            videoManager.addTarget(destVideoAddr);
        } catch (IOException e) {
        	logger.throwing(this.getClass().getName(), "startVideoOut", e);
        } catch (InvalidSessionAddressException e) {
        	logger.throwing(this.getClass().getName(), "startVideoOut", e);
		}

        if (allStopped) {
            return;
        }

        // Initialize and start transmitter
        System.out.println("Starting videotransmit. remoteport: " +
                           media.getPort());
        videoTransmit = new AudioVideoTransmit(this,
                                               videoManager,
                                               transmitVideoDataSource,
                                               videoDescriptor,
                                               media.getRemoteIp(),
                                               localVideoPort, //media.getPort(),
                                               media.getPort(),
                                               format.getName(),
                                               null,
                                               bitrate,
                                               fps);

        if (allStopped) {
            return;
        }

        String errorMsg = videoTransmit.start();

        // If success
        if (errorMsg == null) {

            // Inform user
             if (!allStopped) {
                ac.getProgramWindow().changeStatusLabel
              (ProgramWindow.LABEL_VIDEO_OUT, format.getName());

                //audioTxControlPanel = videoTransmit.getControlPanel();
             }

        }

        // If failiure
        else if (!allStopped) {
            logger.warning("Error starting video: " + errorMsg);

            // Inform user
            ac.getProgramWindow().changeStatusLabel
              (ProgramWindow.LABEL_VIDEO_OUT, "Failed");
        }

    }


    /**
     * Receive audio.
     *
     */
    private void startAudioIn(SdpMedia media, Format format) {

        // Initialize and start receiver
        audioReceive = new AudioVideoReceive(this,
                                             audioManager,
                                             media.getRemoteIp(),
                                             localAudioPort,
                                             ac.getProgramWindow().
                                             getAudioRemotePanel(),
                                             AudioVideoReceive.AUDIO);

        if (allStopped) {
            return;
        }

        if (audioReceive.start()) {
            ac.getProgramWindow().changeStatusLabel
                    (ProgramWindow.LABEL_AUDIO_IN, format.getName());
        }
        else if (!allStopped) {
            ac.getProgramWindow().changeStatusLabel
                    (ProgramWindow.LABEL_AUDIO_IN, "Failed");
        }

    }

  private void startVideoIn(SdpMedia media, Format format) {

        // Initialize and start receiver
         videoReceive = new AudioVideoReceive(this,
                                             videoManager,
                                             media.getRemoteIp(),
                                             localVideoPort,
                                             ac.getProgramWindow().
                                             getVideoRemotePanel(),
                                             AudioVideoReceive.VIDEO);

        if (allStopped) {
            return;
        }

        System.out.println("lvp:" + localVideoPort + " ");
         if (videoReceive.start()) {
            if (!allStopped) {
                ac.getProgramWindow().changeStatusLabel
                        (ProgramWindow.LABEL_VIDEO_IN, format.getName());
            }
        } else if (!allStopped) {
            ac.getProgramWindow().changeStatusLabel
                    (ProgramWindow.LABEL_VIDEO_IN, "Failed");
        }
    }

    /**
     * Mute the microphone
     *
     * @param state True is muted.
     */

    public void muteMicrophone(boolean state) {
        if (audioTransmit != null) {
            audioTransmit.setMute(state);
        }
    }

    /**
     * Mute the speakers
     *
     * @param state True is muted
     */
    public void muteSpeakers(boolean state) {
        if (audioReceive != null) {
            audioReceive.setMute(state);
        }
    }

    class MediaStarter implements Runnable {

        public static final int TEXT_IN = 1;
        public static final int TEXT_OUT = 2;
        public static final int AUDIO_IN = 3;
        public static final int AUDIO_OUT = 4;
        public static final int VIDEO_IN = 5;
        public static final int VIDEO_OUT = 6;

        private Thread t;
        private int type;
        private SdpMedia media;
        private Format format;
        private boolean running;

        public MediaStarter(int type, SdpMedia media, Format format) {
            this.type = type;
            this.media = media;
            this.format = format;

            t = new Thread(this, "Media starter: " + getType(type));
            running = false;
        }

        public void start() {
            if (!running) {
                running = true;
                t.start();
            }
        }

        public void stop() {
            if (running) {
                t.interrupt();
            }
        }

        public void run() {
            switch (type) {
            case TEXT_IN:
                startTextIn(media, format);
                break;
            case TEXT_OUT:
                startTextOut(media, format);
                break;
            case AUDIO_IN:
                startAudioIn(media, format);
                break;
            case AUDIO_OUT:
                startAudioOut(media, format);
                break;
            case VIDEO_IN:
                startVideoIn(media, format);
                break;
            case VIDEO_OUT:
                startVideoOut(media, format);
                break;

            default:
                logger.warning("Cannot start unknown media: " + type);
            }

            running = false;

        }

        public String getType(int type) {
            switch (type) {
            case TEXT_IN:
                return "TEXT_IN";
            case TEXT_OUT:
                return "TEXT_OUT";
            case AUDIO_IN:
                return "AUDIO_IN";
            case AUDIO_OUT:
                return "AUDIO_OUT";
            case VIDEO_IN:
                return "VIDEO IN";
            case VIDEO_OUT:
                return "VIDEO OUT";
            default:
                return "(Unknown type: " + type + ")";
            }
        }

    }


    public void localMute(boolean mute) {
        if (mute) {
            audioTransmit.stop();
        } else {
            audioTransmit.start();
        }
    }

    public Component getAudioTxControlPanel() {
        return audioTxControlPanel;
    }

    public int dropOneRtpTextSeqNo() {
    	if (rtpTextTransmitter != null) {
    		return rtpTextTransmitter.dropOneRtpTextSeqNo();
    	}
    	
    	return 0;
    }
    
    public void sendHexCode(String hexCode) {
    	if (rtpTextTransmitter != null) {
    		rtpTextTransmitter.sendHexCode(hexCode);
    	}
    }
    
    public InetAddress GetInetAddr(String MacAddr)
    {
    	InetAddress Inet = null;
    	String macValue = null;
    	
    	if(null == MacAddr){
    		return null;
    	}
    	
    	try{
    		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
    		for (NetworkInterface netIf : Collections.list(nets)) {
    			Enumeration<InetAddress> inetAddresses = netIf.getInetAddresses();
	            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
	            	if(!(inetAddress.isLoopbackAddress())){
	            		if(netIf.getHardwareAddress() != null){
            				String singleValue = null, finalValue = "";
            				String mac = Arrays.toString(netIf.getHardwareAddress());
	            			StringTokenizer token = new StringTokenizer(mac,"[,]");
	            	
	            			try
	            			{
	            				while(token.hasMoreTokens())
	            				{
	            					singleValue = token.nextToken();
	            					String hexString = Integer.toHexString(Integer.parseInt(singleValue.trim()));
	            					if(hexString.length() > 2){
	            						hexString = hexString.substring(hexString.length()-2, hexString.length());
	            					}else if(hexString.length() == 1){
	            						hexString = "0" + hexString;
	            					}			            		
	            					finalValue += hexString;
	            				}
	            				finalValue.trim();
	            				macValue = finalValue;
	            				macValue.trim();
	            				
	            				if(macValue.compareTo(MacAddr) == 0){
	            					return inetAddress;
	            				}
	            			}catch(NumberFormatException ne){
	            				return Inet;
	            			}
	            		}
	            	}
	            }
    		}
    	}catch (Exception e){
    		return Inet;
    	}
    	return Inet;
    }
}