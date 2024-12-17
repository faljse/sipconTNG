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


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.Font;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Codec;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.NoProcessorException;
import javax.media.PackageManager;
import javax.media.PlugInManager;
import javax.media.Processor;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.SourceCloneable;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import se.omnitor.tipcon1.gui.AudioVideoPlayer;
import se.omnitor.tipcon1.gui.DialogFactory;
import se.omnitor.tipcon1.gui.ProgramWindow;
import se.omnitor.tipcon1.gui.SplashScreen;
import se.omnitor.tipcon1.gui.ThreadedDialog;

import se.omnitor.tipcon1.rtp.RTPPayloadConstants;
import se.omnitor.tipcon1.sip.SipRegistrarInfo;

import se.omnitor.protocol.rtp.text.SyncBuffer;
import se.omnitor.protocol.t140.T140LogArea;
import se.omnitor.protocol.t140.T140TextArea;
import se.omnitor.protocol.t140.T140Panel;
import se.omnitor.protocol.t140.T140Packetizer;
import se.omnitor.protocol.t140.T140DePacketizer;
import se.omnitor.util.FifoBuffer;

import se.omnitor.protocol.sip.AuthInfo;
import se.omnitor.protocol.sip.FromAddress;
import se.omnitor.protocol.sip.SipControllerListener;
import se.omnitor.protocol.sip.SipController;
import se.omnitor.protocol.sip.call.IncomingCallDialog;
import se.omnitor.protocol.sip.call.IncomingOptionsDialog;
import se.omnitor.protocol.sip.call.IncomingReferDialog;
import se.omnitor.protocol.sip.call.OutgoingCallDialog;
import se.omnitor.protocol.sip.call.CallProcessor;
import se.omnitor.protocol.sip.register.RegisterProcessor;
import se.omnitor.protocol.sip.register.RegisterDialog;

import se.omnitor.protocol.sdp.SdpManager;
import se.omnitor.protocol.sdp.SdpMedia;
import se.omnitor.protocol.sdp.media.CustomMedia;
import se.omnitor.protocol.sdp.media.TextMedia;
import se.omnitor.protocol.sdp.format.CustomFormat;
import se.omnitor.protocol.sdp.format.T140Format;
import se.omnitor.protocol.sdp.format.RedFormat;

import se.omnitor.protocol.stun.StunStack;
import se.omnitor.protocol.stun.StunStackException;

import javax.sdp.SdpException;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Dimension;


/**
 * The main class for a multimedia SIP user client.
 *
 * This class connects RTP, SIP and GUI. How this program works is described
 * in a document at Omnitor AB.
 *
 * @author Andreas Piirimets, Omnitor AB
 * @author Erik Zetterstrom, Omnitor AB
 */
public class AppController implements SipControllerListener {

    // A registrarion's status
    private static final int FAIL = 1;
    private static final int TRYING = 2;
    private static final int OK = 3;
    private static final int NOT_REG = 4;

    // Semaphore
    private Integer stateLock = Integer.valueOf(0);

    // The local video
    private AudioVideoPlayer avPlayer = null;

    // General RTP variables
    private boolean processorChangeFailed;
    private DataSource transmitVideoDataSource = null;
    private Vector supportedMedia;

    // RTP Text transmit variables
    private int maxIncomingCps;
    private boolean textIsActivated;

    // Video RTP transmit variables
    private VideoFormat[] videoFormat;
    private Vector<String> videoCodecs;
    private int videoDevice;
    private boolean videoIsActivated;
    private int nbrOfVideoDevices;
    private String[] videoLocator;
    private int videofps = 25;

    // Audio RTP transmit variables
    private AudioFormat[] audioFormat;
    private Vector<String> audioCodecs;
    private int audioDevice;
    private boolean audioIsActivated;
    private int nbrOfAudioDevices;
    private String[] audioLocator;
    private boolean isMuted;

    // GUI dialogs, windows and labels
    protected ProgramWindow gui;
    private ThreadedDialog incomingCallDialog = null;
    private ThreadedDialog outgoingCallDialog;

    // SIP variables
    protected SipController sc;
    private String remoteUserAddress;
    private RegisterProcessor[] registerProcessor;
    private CallProcessor callProcessor;
    private int[] sipRegistrarStatus;
    //boolean sipCompatibleNatIsProbed = false;
    //boolean isBehindSipCompatibleNat = false;
    Thread sipStarterThread = null;
    Integer sipStartStopMutex;
    boolean sipSystemIsRunning = false;
    boolean queuedSipSystemRestart;

    // SDP
    private SdpManager currentSdpManager;
    private Vector<SdpMedia> negotiatedMedia;
    private int sdpCallCounter;

    // Network
    private String localIpAddress = null;
    private String localHostAddress;
    private String dnsServer = null;
    private int nextFreePort = 1024;
    private boolean isSipCompatibleNatDetected = false;
    private String sipCompatibleNatName = null;

    // Runtime variables
    // These variables control system settings during runtime, these settings
    // may and should differ from settings saved to disk.
    private boolean runtimeIsStunActivated;

    private SyncBuffer txTextBuffer;
    private FifoBuffer rxTextBuffer;
    private T140Panel t140Panel;
    private T140Packetizer t140Packetizer;
    private T140DePacketizer t140DePacketizer;

    private Properties language;

    private MediaManager mediaManager;

    private boolean outgoingCallProgressReported;

    private boolean isEconf351 = false;

    private AppSettings appSettings;
    private StunStack stunStack;


    //private static CertCallback certCallback = new CertCallback();
    //************************************************************************

    // declare package and classname
    public final static String CLASS_NAME = AppController.class.getName();
    // get an instance of Logger
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    /**
     * Registers the text plug-in to JMF.
     *
     */
    private void registerTextPlugIn() {

        // Add new package prefix
        Vector<String> packagePrefix = PackageManager.getProtocolPrefixList();
        String myPackagePrefix = "se.omnitor";
        if (packagePrefix.indexOf(myPackagePrefix) == -1) {
            packagePrefix.addElement(myPackagePrefix);
            PackageManager.setProtocolPrefixList(packagePrefix);
            PackageManager.commitProtocolPrefixList();
        }

        // Add new content prefix
        Vector<String> contentPrefix = PackageManager.getContentPrefixList();
        if (contentPrefix.indexOf(myPackagePrefix) == -1) {
            contentPrefix.addElement(myPackagePrefix);
            PackageManager.setContentPrefixList(contentPrefix);
            PackageManager.commitContentPrefixList();
        }

        // Save the changes to the plug-in registry
        try {
            PlugInManager.commit();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }

        // Register Packetizer
        /*
               TextPacketizer textPacketizer = new TextPacketizer(0,0);
               try {
         PlugInManager.removePlugIn(textPacketizer.getClass().getName(),
                                              PlugInManager.CODEC);
                   PlugInManager.addPlugIn(textPacketizer.getClass().getName(),
         textPacketizer.getSupportedInputFormats(),
                                           textPacketizer.
                                           getSupportedOutputFormats(null),
                                           PlugInManager.CODEC);
                   PlugInManager.commit();

               } catch (Exception e2) {
                   if (DEBUG) {
         System.err.println("Cannot register TextPacketizer: " + e2);
                   }
               }

               if (DEBUG) {
                   System.out.println("TextPacketizer registered.");
               }
         */

        // Register DePacketizer
        /*
         TextDePacketizer textDePacketizer = new TextDePacketizer(0, false);
               try {
         PlugInManager.removePlugIn(textDePacketizer.getClass().getName(),
                                              PlugInManager.CODEC);
         PlugInManager.addPlugIn(textDePacketizer.getClass().getName(),
                                           textDePacketizer.
                                           getSupportedInputFormats(),
                                           textDePacketizer.
                                           getSupportedOutputFormats(null),
                                           PlugInManager.CODEC);
                   PlugInManager.commit();

               } catch (Exception e2) {
                   if (DEBUG) {
         System.err.println("Cannot register TextDePacketizer: " + e2);
                   }
               }

               if (DEBUG) {
                   System.out.println("TextDePacketizer registered.");
               }
         */

        // Register player
        /*
               TextPlayer textPlayer= new TextPlayer();
               try {
                   PlugInManager.removePlugIn(textPlayer.getClass().getName(),
                                              PlugInManager.RENDERER);
                   PlugInManager.addPlugIn(textPlayer.getClass().getName(),
         textPlayer.getSupportedInputFormats(),
         textPlayer.getSupportedOutputFormats(null),
                                           PlugInManager.RENDERER);
                   PlugInManager.commit();
               } catch (Exception e2) {
                   if (DEBUG) {
         System.err.println("Cannot register TextDePacketizer: " + e2);
                   }
               }

               if (DEBUG) {
                   System.out.println("TextPlayer registered.");
               }
         */

    }

    /**
     * Registers the H263 mode A effect to JMF.
     *
     */
/*
     private void registerH263ModeAEffect() {

      H263ModeAEffect effect = new H263ModeAEffect();
      String name = effect.getClass().getName();

        try {
            PlugInManager.removePlugIn(name, PlugInManager.EFFECT);
            PlugInManager.addPlugIn(name,
                                    effect.getSupportedInputFormats(),
                                    effect.getSupportedOutputFormats(null),
                                    PlugInManager.EFFECT);
            PlugInManager.commit();
        }
      catch (Exception e2) {
            if (DEBUG) {
                System.err.println("Cannot register H263 mode A effect: " +
       e2);
       e2.printStackTrace();
            }
        }

        if (DEBUG) {
            System.out.println("H263 mode A effect registered.");
        }
     }
*/


    /**
     * Initializes text plugin, creates SipController, and creates GUI.
     * It also reports the status to the given splash screen through all the
     * tasks.
     *
     * @param splash The splash screen which should receive all the reports
     */
    public AppController(Properties aLanguage, SplashScreen splash,
                         String classRoot,
                         String customSettingsFile, String customHelpsetPath) {

        // write methodname
        final String METHOD =
                "AppController(Properties language, SplashScreen splash, String classRoot)";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD, new Object[] {aLanguage, splash,
                        classRoot});

        try {
           AppConstants.setConstants(customSettingsFile, customHelpsetPath);
        } 
        catch (IOException e) {
            logger.throwing(this.getClass().getName(), "<init>", e);
            DialogFactory.showErrorMessageDialog
                    (aLanguage.getProperty("se.omnitor.tipcon1.AppController." +
                                          "GUI_ERROR"));
            throw new RuntimeException("");
        } 
        catch (URISyntaxException e) {
            logger.throwing(this.getClass().getName(), "<init>", e);
            DialogFactory.showErrorMessageDialog
                    (aLanguage.getProperty("se.omnitor.tipcon1.AppController." +
                                          "GUI_ERROR"));
            throw new RuntimeException("");
        }

        sipStartStopMutex = Integer.valueOf(0);

        appSettings = new AppSettings(AppConstants.SETTINGS_DATA_FILE_URL);

    	if (appSettings.getLanguageCode() == AppConstants.ENGLISH) {
            language = new English();
        }
        else if (appSettings.getLanguageCode() == AppConstants.SWEDISH) {
            language = new Swedish();
        }

        DialogFactory.registerLanguage(language);

        sdpCallCounter = 1;

        isMuted = false;

        // Initialize text plug-in
        splash.changeText(language.getProperty("se.omnitor.tipcon1." +
                                               "AppController.INIT_TEXT"));
        registerTextPlugIn();

        // Initialize text plug-in
        /*
          splash.changeText("Initializing video transmission");
               registerH263ModeAEffect();
         */

        // Get the local IP address
        splash.changeText(language.getProperty("se.omnitor.tipcon1." +
                                               "AppController.INIT_NETWORK"));
        if (!detectLocalIp()) {
            DialogFactory.showErrorMessageDialog
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "NETWORK_ERROR"));
        }

        /*
          catch (UnknownHostException uhe) {
            logger.throwing(this.getClass().getName(), "<init>", uhe);
            DialogFactory.showErrorMessageDialog
         (language.getProperty("se.omnitor.tipcon1.AppController." +
                 "SIP_ERROR"));
                   System.exit(-1);
          }
         */

        //try {

            // Retrieve all supported media types of the local computer
            splash.changeText(language.getProperty("se.omnitor.tipcon1." +
                    "AppController." +
                    "INIT_CAPTURE"));
            supportedMedia = (Vector) initializeSupportedMedia();
            if (supportedMedia.isEmpty()) {
                splash.changeText(language.getProperty("se.omnitor.tipcon1." +
                        "AppController." +
                        "SETUP_NA"));
                detectDevices();
                supportedMedia = (Vector) initializeSupportedMedia();

            }
/*
        } catch (Exception e1) {
            splash.changeText(language.getProperty("se.omnitor.tipcon1." +
                    "AppController." +
                    "DEVICE_CHANGE"));
            detectDevices();
            supportedMedia = (Vector) initializeSupportedMedia();

        }
       */

        // Create text transport

        txTextBuffer = new SyncBuffer(appSettings.getRedundantRtpGenerations(),
                                      (int) appSettings.getBufferTime());
        logger.finer("buffertime is :" + appSettings.getBufferTime());

        // Initialize the GUI
        splash.changeText(language.getProperty("se.omnitor.tipcon1." +
                                               "AppController.CREATE_GUI"));

        t140Packetizer = new T140Packetizer(0);
        t140Panel = new T140Panel(t140Packetizer,
                                  appSettings.isRealtimePreviewEnabled(),
                                  language.getProperty("se.omnitor.tipcon1.gui.t140.LOGWINDOW"),
                                  language.getProperty("se.omnitor.tipcon1.gui.t140.RECEIVEWINDOW"),
                                  language.getProperty("se.omnitor.tipcon1.gui.t140.SENDWINDOW"),
                                  language);
        t140Packetizer.setOutBuffer(txTextBuffer);
        rxTextBuffer = new FifoBuffer();
        t140DePacketizer = new T140DePacketizer(0);
        t140DePacketizer.setInBuffer(rxTextBuffer);
        t140DePacketizer.setEventHandler(t140Panel);
        t140DePacketizer.start();

        setupT140panel();

        // Get settings
        splash.changeText(language.getProperty("se.omnitor.tipcon1." +
                                               "AppController.LOAD_SETTINGS"));
        restoreSavedSettings();
        try {
            Class cls = Class.forName(classRoot + ".gui.ProgramWindow");

            gui = (ProgramWindow) cls.newInstance();
            logger.logp(Level.FINER, CLASS_NAME, METHOD, "SipController sc is",
                        sc);
            gui.init(sc, this, appSettings);
            deploySettings(); // to use  don't use this line (here)
            gui.prepare();
        } catch (ClassNotFoundException e) {
            logger.throwing(this.getClass().getName(), "<init>", e);

            DialogFactory.showErrorMessageDialog
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "GUI_ERROR"));
            throw new RuntimeException("GUI error!");
        } catch (InstantiationException e) {
            logger.throwing(this.getClass().getName(), "<init>", e);

            DialogFactory.showErrorMessageDialog
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "GUI_ERROR"));
            throw new RuntimeException("GUI error!");
		} catch (IllegalAccessException e) {
            logger.throwing(this.getClass().getName(), "<init>", e);

            DialogFactory.showErrorMessageDialog
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "GUI_ERROR"));
            throw new RuntimeException("GUI error!");
		}

        // Initialize the SIP client
        splash.changeText(language.getProperty("se.omnitor.tipcon1." +
                                               "AppController.INIT_SIP"));

        threadedStartSipSystem();
        deploySettings();
        
        logger.exiting(CLASS_NAME, METHOD);
    }

    private void restoreSavedSettings() {
        appSettings.load();
        txTextBuffer.setRedGen(appSettings.getRedundantRtpGenerations());
        setTextBufferTime(appSettings.getBufferTime());
        t140Panel.useMsrpSmoother(appSettings.isMsrpSmootherActive());
        txTextBuffer.setSendOnCR(appSettings.useSendOnCR());
        maxIncomingCps = appSettings.getMaxIncomingCps();

        textIsActivated = true;
        videoIsActivated = true;
        //videoIsActivated = false;
        audioIsActivated = true;

        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            restoreSavedVideoSettings();
        }
        restoreSavedAudioSettings();

    }

    public void guiPrepare()
    {
    	gui.prepare();
    }
    /**
     * Gets the local IP address and the local host name. These values are
     * stored in a global variable in the system. If the local IP address is
     * 127.0.0.1, the function assumes that no network is present and false
     * will be returned.
     *
     * @return True if an address was retrieved, false if not.
     */
    private boolean detectLocalIp() {

        String oldLocalIp = localIpAddress;

        try {
        	if(null != appSettings.getNetIfMacAddr()){
	        	localIpAddress = getMatchingIpAddress(appSettings.getNetIfMacAddr());
	        	if(null == localIpAddress || "" == localIpAddress){
	        		localIpAddress = InetAddress.getLocalHost().getHostAddress();
	        	}
        	}else{
        		localIpAddress = InetAddress.getLocalHost().getHostAddress();
        	}
        	
            localHostAddress = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhe) {
            return false;
        }

        if (!localIpAddress.equals(oldLocalIp) && oldLocalIp != null) {
            if (localIpAddress.equals("127.0.0.1")) {
                //sc.stop();
            	stopSipSystem();
            } else if (oldLocalIp.equals("127.0.0.1")) {
                try {
                    //sc.start();
                	startSipSystem();
                } catch (Exception e) {
                    return false;
                }
            } else {
                try {
                    //sc.stop();
                    //sc.start();
                	stopSipSystem();
                    startSipSystem();
                } catch (Exception e) {
                    return false;
                }
            }
        }

        if (localIpAddress.equals("127.0.0.1")) {
            return false;
        }

        return true;
    }


    public void setupT140panel() {
        t140Panel.useMsrpSmoother(true);
        t140Panel.setTaFont
                (new Font(appSettings.getLocalFont(), Font.PLAIN,
                          appSettings.getLocalTextSize()));
        /*
              t140Panel.setRemoteFont
           (new Font(remoteFont, Font.PLAIN, remoteTextSize));
         */

        try {
            t140Panel.setTaFontColor(appSettings.getLocalTextColor());
        } catch (NumberFormatException nfe) {
        }

        /*
              t140Panel.setRemoteFontColor
           (new Color(Integer.parseInt(remoteTextColor[0]),
         Integer.parseInt(remoteTextColor[1]),
         Integer.parseInt(remoteTextColor[2])));
         */

        try {
            t140Panel.setTaFontBackground(appSettings.getLocalBgColor());
        } catch (NumberFormatException nfe) {
        }

        /*
              t140Panel.setRemoteFontBackground
           (new Color(Integer.parseInt(remoteBgColor[0]),
         Integer.parseInt(remoteBgColor[1]),
         Integer.parseInt(remoteBgColor[2])));
         */

        /*
           if (dataCnt != pLength) {
         Object[][] correctedData = new Object[dataCnt][2];
         System.arraycopy(data, 0, correctedData, 0, dataCnt);
         data = correctedData;
           }
         */

//        t140Panel.setTaFont
//                (new Font("Arial Unicode MS", Font.PLAIN, 12));
        /*
          t140Panel.setRemoteFont
          (new Font("Arial Unicode MS", Font.PLAIN, 12));
         */

        t140Panel.setTaFontColor(appSettings.getLocalTextColor());
        //t140Panel.setRemoteFontColor(new Color(0, 0, 0));
        t140Panel.setTaFontBackground(appSettings.getLocalBgColor());
        //t140Panel.setRemoteFontBackground(new Color(255, 255, 255));

    }

    /**
     * Restores saved audio settings. At this moment, this function just
     * sets default settings for audio. Save and restore to harddrive has
     * not been implemented yet.
     *
     */
    public void restoreSavedAudioSettings() {
        int cnt;
        int fcnt;

        // Count devices
        audioCodecs = new Vector<String>(0, 1);

        // Set formats and codecs
        audioFormat = new AudioFormat[nbrOfAudioDevices];
        audioLocator = new String[nbrOfAudioDevices];

        Format[] captureFormat;
        int audioCnt = 0;
        Vector outputFormats;
        DeviceContainer tempDc;

        int smSize;
        if (supportedMedia == null) {
            smSize = 0;
        } else {
            smSize = supportedMedia.size();
        }

        for (cnt = 0; cnt < smSize; cnt++) {

            tempDc = (DeviceContainer) supportedMedia.get(cnt);

            captureFormat = tempDc.getCaptureFormats();
            outputFormats = (Vector) tempDc.getOutputFormats();

            if (tempDc.getType().equals("audio")) {

                if (captureFormat != null) {

                    for (fcnt = 0; fcnt < captureFormat.length; fcnt++) {

                        if (audioFormat[audioCnt] == null) {
                            audioFormat[audioCnt] =
                                    (AudioFormat) captureFormat[fcnt];
                        } else {
                            if (((AudioFormat) captureFormat[fcnt]).
                                getSampleSizeInBits() >
                                audioFormat[audioCnt].getSampleSizeInBits()) {

                                audioFormat[audioCnt] =
                                        (AudioFormat) captureFormat[fcnt];
                            }
                        }
                    }

                }

                if (outputFormats != null) {

                    int ofSize = outputFormats.size();

                    for (fcnt = 0; fcnt < ofSize; fcnt++) {
                        if (!audioCodecs.contains
                            ((String) outputFormats.get(fcnt))) {
                        	
                        	// Disable DVI as default setting 
                        	if (!((String)outputFormats.get(fcnt)).toUpperCase(Locale.US).equals("DVI")) {
                        		audioCodecs.add((String) outputFormats.get(fcnt));
                        	}
                        }
                    }

                }

                audioLocator[audioCnt] = tempDc.getLocator();

                audioCnt++;

            }

        }

    }

    /**
     * Restores saved video settings.
     *
     */
    public void restoreSavedVideoSettings() {
        int cnt;
        int fcnt;

        // Count devices
        videoCodecs = new Vector<String>(0, 1);

        // Set formats and codecs
        videoFormat = new VideoFormat[nbrOfVideoDevices];
        videoLocator = new String[nbrOfVideoDevices];

        Format[] captureFormat;
        int videoCnt = 0;
        Vector outputFormats;
        DeviceContainer tempDc;
        boolean vfIsSet = false;

        int smSize;
        if (supportedMedia == null) {
            smSize = 0;
        } else {
            smSize = supportedMedia.size();
        }

        for (cnt = 0; cnt < smSize; cnt++) {

            tempDc = (DeviceContainer) supportedMedia.get(cnt);

            captureFormat = tempDc.getCaptureFormats();
            outputFormats = (Vector) tempDc.getOutputFormats();

            if (tempDc.getType().equals("video")) {
                System.out.println("restoreSavedVideoSettings()");

                if (captureFormat != null) {

                    for (fcnt = 0; fcnt < captureFormat.length; fcnt++) {

                        if (videoFormat[videoCnt] == null) {
                            videoFormat[videoCnt] = (VideoFormat) captureFormat[fcnt];
                            //VideoFormat[] vf = new VideoFormat[1];
                            //vf[0] = new VideoFormat("YUV", new Dimension(176,144),Format.NOT_SPECIFIED,null, 25.0F);
                            //setVideoFormat(vf);
                        } else {
                            // 188 kbit/s is a suitable speed for video
                            // transmission. Try to find a format as close as
                            // possible to 188 kbit/s.

                            //System.out.println(appSettings.getResolution() + ":" +formatToString(captureFormat[fcnt]));
                            String resolution = appSettings.getResolution();
                            //if (resolution.equals("176x144 (yuv)")) {
                            //    VideoFormat[] vf = new VideoFormat[1];
                            //    vf[0] = new VideoFormat("YUV", new Dimension(176,144),Format.NOT_SPECIFIED,null, 25.0F);
                            //    setVideoFormat(vf);
                            //    vfIsSet = true;
                            //}
                            //else
                            if (resolution.equalsIgnoreCase(formatToString((VideoFormat)captureFormat[fcnt]))) {
                                VideoFormat[] vf = new VideoFormat[1];
                                vf[0] = (VideoFormat) captureFormat[fcnt];
                                setVideoFormat(vf);
                                vfIsSet = true;
                            } else if (Math.abs(((VideoFormat) captureFormat[fcnt]).
                                         getMaxDataLength()) <
                                Math.abs(videoFormat[videoCnt].
                             getMaxDataLength() - 316000) && !vfIsSet) {

                                if (((VideoFormat) captureFormat[fcnt]).
                                    getEncoding().equals("yuv")) {
                                    VideoFormat[] vf = new VideoFormat[1];
                                    vf[0] = (VideoFormat) captureFormat[fcnt];
                                    setVideoFormat(vf);
                                    //videoFormat[videoCnt] = (VideoFormat) captureFormat[fcnt];
                                }
                            }
                        }
                    }
                }


                if (outputFormats != null) {

                    int ofSize = outputFormats.size();

                    for (fcnt = 0; fcnt < ofSize; fcnt++) {
                        if (!videoCodecs.contains
                            ((String) outputFormats.get(fcnt))) {

                            videoCodecs.add((String) outputFormats.get(fcnt));
                        }
                    }

                }
                videoLocator[videoCnt] = tempDc.getLocator();
                videoCnt++;
            }
        }
    }

    /**
     * This function is aimed to be used after all settings has been entered
     * to this class. This function will insert all codecs into the SIP
     * controller and, if activated, restart the local video image.
     *
     */
    public void deploySettings() {
        // write methodname
        final String METHOD = "deploySettings()";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD);

        // Now, all SDP settings are deployed immediately before calling or
        // answering a call in order to allow STUN requests probe for port
        // mappings.
        //deploySdpSettings(true);
         if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
             deployVideoSettings();
         }

        logger.exiting(CLASS_NAME, METHOD);
    }

    /**
     * Deploy video settings. The video is restarted.
     *
     */
    public void deployVideoSettings() {
        Thread t = new Thread() {
            public void run() {
                restartVideo();
            }
        };

        t.setName("Video restarter");
        t.start();
    }

    /**
     * Deploys the current SDP settings, which are read from the variables.
     * This function should be run after the variables has been read from the
     * settings XML file or after the user has committed changes.
     *
     */
    public void deploySdpSettings(boolean useTextIfActivated) {

        // write methodname
        final String METHOD = "deploySdpSettings(boolean useTextIfActivated)";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD);

        int plType;
        String plName;
        long plClockRate;
        RTPPayloadConstants constants = new RTPPayloadConstants();
        int cnt;
        String codecName;

        // Higher than 98 since Allan eC requires text to use 98
        int nextDynamicPayloadType = 99;

        SdpManager sdpm;

        try {
            logger.logp(Level.INFO, CLASS_NAME, METHOD, "userRealName is",
                        appSettings.getUserRealName());

            if (isRuntimeStunActivated()) {
                sdpm = new SdpManager(appSettings.getUserRealName().replace(' ',
                        '_'),
                                      "" + sdpCallCounter,
                                      "" + sdpCallCounter,
                                      "IN",
                                      "IP4",
                                      stunStack.getExternalIp());
            } else {
                sdpm = new SdpManager(appSettings.getUserRealName().replace(' ',
                        '_'),
                                      "" + sdpCallCounter,
                                      "" + sdpCallCounter,
                                      "IN",
                                      "IP4",
                                      localIpAddress);
            }
        } catch (SdpException se) {
            logger.throwing(this.getClass().getName(), "deploySdpSettings",
                            se);
            return;
        }

        SdpMedia sdpMedia;
        se.omnitor.protocol.sdp.Format format;
        Vector<se.omnitor.protocol.sdp.Format> formats;

        // Video descriptors
        if (videoIsActivated && AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {

            if (videoCodecs != null &&
                videoCodecs.size() > 0) {

                int port = -1;

                if (isRuntimeStunActivated()) {
                    int mappedPort = 1;

                    try {
                        // Do five attempts finding an even mapped port (as RTP should be sent from an even port)
                        for (int mcnt=0; mcnt<5 && (mappedPort%2 != 0); mcnt++) {
                            port = getFreePort();
                            mappedPort = stunStack.getMappedPort(port);
                        }

                        sdpMedia = new CustomMedia("video",
                                                   mappedPort,
                                                   "RTP/AVP");
                        // As the NAT often picks port numbers in series,
                        // lets force it to hold a port open for the incoming
                        // RTCP.
                        stunStack.getMappedPort(port+1);
                    } catch (StunStackException se) {
                        sdpMedia = new CustomMedia("video", port, "RTP/AVP");
                        setRuntimeStunActivated(false);
                        DialogFactory.showErrorMessageDialog
                                (language.getProperty(
                                        "se.omnitor.tipcon1.AppController." +
                                        "STUN_ERROR"));
                    }
                } else {
                    port = getFreePort();
                    sdpMedia = new CustomMedia("video", port, "RTP/AVP");
                }
                sdpMedia.setPhysicalPort(port);
                formats = new Vector<se.omnitor.protocol.sdp.Format>(0, 1);

                int vcSize = videoCodecs.size();

                for (cnt = 0; cnt < vcSize; cnt++) {
                    codecName = (String) videoCodecs.get(cnt);

                    plType = constants.getPayloadType(codecName);
                    if (plType != -1) {

                        if (plType == RTPPayloadConstants.DYNAMIC) {
                            plType = nextDynamicPayloadType;
                            nextDynamicPayloadType++;

                            plClockRate = constants.getClockRate(codecName);
                            if (plClockRate == RTPPayloadConstants.VARIABLE) {
                                plClockRate = 8000; // Check this!?
                            }

                            plName = constants.getSdpName(codecName);

                            format = new CustomFormat(plType,
                                    plName,
                                    (int) plClockRate);

                        } else {

                            format =
                                    se.omnitor.protocol.sdp.Format.
                                    getFormat(plType);

                        }

                        if (format != null) {
                            formats.add(format);
                        }

                    }
                }

                sdpMedia.setFormats(formats);
                sdpm.addMedia(sdpMedia);

            }

            else {

                // TODO:
                // Set these to receive only!

                //formats.add(FOrmat.getFormat(34));
                //formats.add(Format.getFormat(26));

            }

        }

        // Audio descriptors

        if (audioIsActivated &&
            audioCodecs != null &&
            audioCodecs.size() > 0) {

            int port = -1;

            if (isRuntimeStunActivated()) {
                int mappedPort = 1;

                try {
                    stunStack.getMappedPort(5059);
                    // Do five attempts finding an even mapped port (as RTP should be sent from an even port)
                    for (int mcnt=0; mcnt<5 && (mappedPort%2 != 0); mcnt++) {
                        port = getFreePort();
                        mappedPort = stunStack.getMappedPort(port);
                    }

                    sdpMedia = new CustomMedia("audio",
                                               mappedPort,
                                               "RTP/AVP");
                    // As the NAT often picks port numbers in series,
                    // lets force it to hold a port open for the incoming
                    // RTCP.
                    stunStack.getMappedPort(port+1);
                } catch (StunStackException se) {
                    sdpMedia = new CustomMedia("audio", port, "RTP/AVP");
                    setRuntimeStunActivated(false);
                    DialogFactory.showErrorMessageDialog
                            (language.getProperty(
                                    "se.omnitor.tipcon1.AppController." +
                                    "STUN_ERROR"));
                }
            } else {
                port = getFreePort();
                sdpMedia = new CustomMedia("audio", port, "RTP/AVP");
            }
            sdpMedia.setPhysicalPort(port);
            formats = new Vector<se.omnitor.protocol.sdp.Format>(0, 1);

            int acSize = audioCodecs.size();

            for (cnt = 0; cnt < acSize; cnt++) {
                codecName = (String) audioCodecs.get(cnt);

                plType = constants.getPayloadType(codecName);
                if (plType != -1) {

                    if (plType == RTPPayloadConstants.DYNAMIC) {
                        plType = nextDynamicPayloadType;
                        nextDynamicPayloadType++;

                        plClockRate = constants.getClockRate(codecName);
                        if (plClockRate == RTPPayloadConstants.VARIABLE) {
                            plClockRate = 8000; // Check this!?
                        }

                        plName = constants.getSdpName(codecName);

                        format = new CustomFormat(plType,
                                                  plName,
                                                  (int) plClockRate);

                    } else {

                   		format =
                                se.omnitor.protocol.sdp.Format.getFormat(plType);

                    }

                    if (format != null) {
                        formats.add(format);
                    }
                }

            }

            sdpMedia.setFormats(formats);
            sdpm.addMedia(sdpMedia);

        }

        // Text descriptors
        if (textIsActivated && useTextIfActivated) {

            int port = -1;

            if (isRuntimeStunActivated()) {
                int mappedPort = 1;

                try {
                    // Do five attempts finding an even mapped port (as RTP should be sent from an even port)
                    for (int mcnt=0; mcnt<5 && (mappedPort%2 != 0); mcnt++) {
                        port = getFreePort();
                        mappedPort = stunStack.getMappedPort(port);
                    }

                    sdpMedia = new TextMedia("text",
                                               mappedPort,
                                               "RTP/AVP");
                    // As the NAT often picks port numbers in series,
                    // lets force it to hold a port open for the incoming
                    // RTCP.
                    stunStack.getMappedPort(port+1);
                } catch (StunStackException se) {
                    sdpMedia = new TextMedia("text", port, "RTP/AVP");
                    setRuntimeStunActivated(false);
                    DialogFactory.showErrorMessageDialog
                            (language.getProperty(
                                    "se.omnitor.tipcon1.AppController." +
                                    "STUN_ERROR"));
                }
            } else {
                port = getFreePort();
                sdpMedia = new TextMedia("text", port, "RTP/AVP");
            }
            sdpMedia.setPhysicalPort(port);

            formats = new Vector<se.omnitor.protocol.sdp.Format>(0, 1);

            plType = constants.getPayloadType("T140");

            if (plType != -1) {

                if (plType == RTPPayloadConstants.DYNAMIC) {
                    plType = nextDynamicPayloadType;
                    nextDynamicPayloadType++;
                }

                T140Format tf = new T140Format(plType);
                RedFormat rf = null;

                if (appSettings.getRedundantRtpGenerations() > 0) {
                    rf = new RedFormat(nextDynamicPayloadType);
                    nextDynamicPayloadType++;
                    rf.setGenerations(appSettings.getRedundantRtpGenerations());

                    tf.setRedFormat(rf);
                    rf.setFormat(tf);

                    formats.add(rf);
                }

                formats.add(tf);

                /*
                   plClockRate = constants.getClockRate("T140");
                   if (plClockRate == constants.VARIABLE) {
                    plClockRate = 1000; // Check this!?
                   }

                   plName = constants.getSdpName("T140");

                   format = new T140Format(plType, (int)plClockRate);

                   if (format != null) {
                    formats.add(format);
                   }

                 */
                /*
                   try {
                    if (maxIncomingCps > 0) {

                    sc.addMedia("",
                  plName,
                  "T140",
                  "text",
                  plType,
                  plClockRate,
                  redundantGenerations != 0,
                  "cps=" + maxIncomingCps);
                    }
                    else {


                 sc.addMedia("",
                      plName,
                      "T140",
                      "text",
                      plType,
                      plClockRate,
                      redundantGenerations != 0);
                    }
                   }
                   catch (Exception e) {
                    // Ignore any errors, as there is nothing to do about it.
                   }


                 */

            }

            sdpMedia.setFormats(formats);
            sdpm.addMedia(sdpMedia);

        }

        currentSdpManager = sdpm;

        logger.exiting(CLASS_NAME, METHOD);

    }


    private int getFreePort() {
        for (int cnt=nextFreePort; cnt<65535; cnt+=2) {
            try {
                DatagramSocket dgs = new DatagramSocket(cnt);
                dgs.close();
                dgs = new DatagramSocket(cnt+1);
                dgs.close();
                nextFreePort = cnt+2;
                return cnt;
            } catch (SocketException se) {
                // Try next port
            }
        }
        for (int cnt=1024; cnt<nextFreePort; cnt+=2) {
            try {
                DatagramSocket dgs = new DatagramSocket(cnt);
                dgs.close();
                nextFreePort = cnt+2;
                return cnt;
            } catch (SocketException se) {
                // Try next port
            }
        }
        return -1;
    }

    /**
     * Sets the number of redundant generations in the outgoing RTP Text
     * packets.
     *
     * @param red The number of generations.
     */
    /*
         public void xxsetTextRedundantGenerations(int red) {
        redundantRtpGenerations = red;
        txTextBuffer.setRedGen(red);
         }
     */

    /**
     * Activates the whole program. Shows the GUI and starts to listen for
     * incoming SIP packets.
     *
     */
    public void start() {

        // The program is ready for user input, show the GUI.
        gui.setVisible(true);

    }

    /**
     * Starts all SIP handing in a new thread (sipStarterThread).
     *
     */
    private void threadedStartSipSystem() {
        final Thread oldThread = sipStarterThread;

        sipStarterThread = new Thread() {
            public void run() {
                // Wait until last SIP starter thread stops, 20 seconds
                if (oldThread != null) {
                    for (int cnt = 0; cnt < 30 && oldThread.isAlive(); cnt++) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                                // Do nothing
                            }
                        }

                        // Finally, interrupt it if still not stopped after 20 secs
                        if (oldThread.isAlive()) {
                            oldThread.interrupt();
                        }
                }
                try {
                    startSipSystem();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        sipStarterThread.setName("SIP starter thread");
        sipStarterThread.start();
    }

    /**
     * Starts all SIP handing in a new thread (sipStarterThread).
     *
     */
    public void threadedRestartSipSystem() {
        final Thread oldThread = sipStarterThread;

        sipStarterThread = new Thread() {
            public void run() {
                // Wait until last SIP starter thread stops, 20 seconds
                if (oldThread != null) {
                    for (int cnt = 0; cnt < 30 && oldThread.isAlive(); cnt++) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                                // Do nothing
                            }
                        }

                        // Finally, interrupt it if still not stopped after 20 secs
                        if (oldThread.isAlive()) {
                            oldThread.interrupt();
                        }
                }

                stopSipSystem();
                try {
                    startSipSystem();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        sipStarterThread.setName("SIP restarter thread");
        sipStarterThread.start();
    }

    private void startSipSystem() throws SocketException, UnknownHostException {

        synchronized (sipStartStopMutex) {
            gui.setRegDetecting("Detecting SIP environment");

            int stunMode = appSettings.getStunMode();

            if (stunMode == AppSettings.STUN_DISABLED) {
                setRuntimeStunActivated(false);
            } else {
                setRuntimeStunActivated(true);
            }
            isSipCompatibleNatDetected = false;
            boolean possibleSipNat = false;

            // If STUN is activated, but no STUN server is chosen, try to find a
            // STUN server by looking att the register settings.
            if (isRuntimeStunActivated() &&
                (appSettings.getStunServerAddress() == null ||
                 appSettings.getStunServerAddress().trim().equals(""))) {

                gui.setRegDetecting("Finding STUN server");

                boolean foundStunServer = false;
                if (appSettings.getSipRegistrarInfo() != null) {
                    for (int cnt = 0;
                                   (cnt < appSettings.getSipRegistrarInfo().length) &&
                                   !foundStunServer;
                                   cnt++) {
                        String host = appSettings.getSipRegistrarInfo()[cnt].
                                      getRegistrarHost();

                        String stunServerAddress = findStunServer(host);
                        if (stunServerAddress != null) {
                            foundStunServer = true;
                            appSettings.setStunServerAddress(stunServerAddress);
                        }

                    }
                }

                if (!foundStunServer) {
                    appSettings.setStunServerAddress(AppConstants.PUBLIC_STUN_SERVER);
                    /*
                    DialogFactory.showErrorMessageDialog(language.getProperty(
                            "se.omnitor.tipcon1.AppController." +
                            "STUN_SERVER_NOT_FOUND"));
                    setRuntimeStunActivated(false);

                    if (possibleSipNat) {
                        isSipCompatibleNatDetected = true;
                    }
*/

                }
            }

            // Do STUN tests
            if (appSettings.getStunServerAddress() != null &&
                !appSettings.getStunServerAddress().trim().equals("") &&
                isRuntimeStunActivated()) {

                // First check if we might have a SIP compatible NAT here
                possibleSipNat = detectSipCompatibleNat();

                stunStack = new StunStack(getLocalIpAddress(),
                                          appSettings.getStunServerAddress());
                try {
                    gui.setRegDetecting(language.getProperty("se.omnitor.tipcon1.AppController.DETECT"));
                    stunStack.start();

                    if (stunStack.getNatType().equals(StunStack.OPEN_INTERNET)) {
                        if (stunMode == AppSettings.STUN_AUTO) {
                            setRuntimeStunActivated(false);
                        }
                        isSipCompatibleNatDetected = false;
                    } else {
                        if (possibleSipNat) {
                            isSipCompatibleNatDetected = true;
                            if (stunMode == AppSettings.STUN_AUTO) {
                                setRuntimeStunActivated(false);
                            }
                        } else if (stunStack.getNatType().equals(StunStack.
                                UDP_BLOCKING_FIREWALL)) {
                            if (stunMode == AppSettings.STUN_AUTO) {
                                setRuntimeStunActivated(false);
                            }
                            DialogFactory.showErrorMessageDialog(language.
                                    getProperty(
                                            "se.omnitor.tipcon1.AppController." +
                                            "UDP_BLOCKING_FW_FOUND"));
                        } else if (stunStack.getNatType().equals(StunStack.
                                SYMMETRIC_NAT) ||
                                   stunStack.getNatType().equals(StunStack.
                                SYMMETRIC_UDP_FIREWALL)) {
                            if (stunMode == AppSettings.STUN_AUTO) {
                                setRuntimeStunActivated(false);
                            }
                            DialogFactory.showErrorMessageDialog(language.
                                    getProperty(
                                            "se.omnitor.tipcon1.AppController." +
                                            "SYMMETRIC_FW_FOUND"));
                        }
                    }
                } catch (StunStackException sse) {
                    if (stunMode == AppSettings.STUN_AUTO) {
                        setRuntimeStunActivated(false);
                    }
                    if (possibleSipNat) {
                        if (stunMode == AppSettings.STUN_AUTO) {
                            isSipCompatibleNatDetected = true;
                        }
                    } else {
                        DialogFactory.showErrorMessageDialog(language.getProperty(
                                "se.omnitor.tipcon1.AppController." +
                                "STUN_ERROR"));
                    }
                }

            }

            gui.setRegDetecting("Initializing SIP");

            try {
            	if (isRuntimeStunActivated()) {
            		sc = new SipController(this, localIpAddress,
            				appSettings.getOutboundProxy(),
            				stunStack);
            	} else {
            		sc = new SipController(this, localIpAddress,
                                       	appSettings.getOutboundProxy(), null);
            	}

                sc.start();
            } catch (javax.sip.InvalidArgumentException iae) {
                logger.throwing(this.getClass().getName(), "startSip", iae);
            } catch (javax.sip.PeerUnavailableException e) {
                logger.throwing(this.getClass().getName(), "startSip", e);
            }

            sc.setPrimarySipAddress("unknown@" + localIpAddress);

            sc.setAuthInfo(convertToAuthInfo(appSettings.getSipRegistrarInfo()));

            sc.setFromAddressList(convertToFromAddress(appSettings.
                                                       getSipRegistrarInfo()));

            gui.setRegNa();

            sipRegisterAll();

            sipSystemIsRunning = true;
        }
    }


    /**
     * Finds a STUN server by looking at STUN SRV records of a given host name.
     *
     * @param host The host name to search SRV records on
     * @return STUN server address. Null if no server was found.
     */
    private String findStunServer(String host) {
        if (findDns() == null) {
            return null;
        }
        boolean foundStunServer = false;
        String ssa = null;

        try {
            java.util.Hashtable<String, String> env = new java.util.Hashtable<String, String>();
            env.put("java.naming.factory.initial",
                    "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url",
                    "dns://" + findDns());

            javax.naming.directory.DirContext ctx = new javax.naming.directory.
                    InitialDirContext(env);

            javax.naming.directory.Attributes attrs = ctx.getAttributes(
                    "_stun._udp." + host, new String[] {"SRV"});

            for (javax.naming.NamingEnumeration ae = attrs.getAll();
                    ae.hasMoreElements() &&
                    !foundStunServer; ) {
                javax.naming.directory.Attribute attr = (javax.naming.directory.
                        Attribute) ae.next();
                if (attr.getID().equals("SRV")) {
                    for (java.util.Enumeration vals = attr.getAll();
                            vals.hasMoreElements(); ) {
                        String e = (String) vals.nextElement();
                        String el[] = e.split(" ");
                        if (el.length > 3) {
                            if (el[3].endsWith(".")) {
                                ssa = el[3].substring(0, el[3].length() - 1);
                            } else {
                                ssa = el[3];
                            }
                            foundStunServer = true;
                        }
                    }

                }
            }

            ctx.close();
        }
        /*
        catch (NameNotFoundException nnfe) {
            // DNS name not found, just continue
        }
        catch (NamingException ne) {
            // DNS problems just continue
        }*/
        catch (Exception e) {
            System.err.println("Problem querying DNS: " + e);
            e.printStackTrace();
        }

        return ssa;
    }

    /**
     * Returns the IP of a DNS server.
     *
     * @return IP of DNS server, null if no DNS server was found.
     */
    private String findDns() {
        int delayTime = -1000;

        for (int cnt=0; cnt<2; cnt++) {
            delayTime += 3000;

            // If this function has been run before, return the cached result to
            // speed up the application.
            if (dnsServer != null) {
                return dnsServer;
            }

            BufferedReader in = null;
            try {
                // Run nslookup and look at the printout to achieve system's DNS
                // server setting. There seems not to be any better solution! If
                // there is, please replace this.
                Process p = Runtime.getRuntime().exec("nslookup");

                // Sleep for 2 secs in order to wait for nslooup to start and
                // generate output.
                Thread.sleep(delayTime);
                p.destroy();

                // Read the output of nslookup
                in = new BufferedReader(new InputStreamReader(p.
                        getInputStream()));
                String readerString = "";

                while (((readerString = in.readLine()) != null)) {
                    String spl[] = readerString.split("Address:");
                    if (spl.length > 1) {
                        return spl[1].trim();
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            finally {
            	if (in != null) {
            		try {
						in.close();
					} catch (IOException e) {
						// Ignore exception
					}
            	}
            }
        }

        return null;
    }

    /**
     * Ends the program by shutting down the SIP client and then exiting.
     *
     */
    public void stop() {

        // Hide the frame
        gui.setTitle(gui.getTitle() + " - " +
                     language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "CLOSING"));
        gui.setVisible(false);

        // Stop the local video

        if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && avPlayer != null) {
            avPlayer.close();
            avPlayer = null;
        }
        /*
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            // Ignore interruptions
        }*/

        // If detect is caused to program start, wait for it
        if (sipStarterThread != null && sipStarterThread.isAlive()) {
            for (int cnt=0; cnt<30&&sipStarterThread.isAlive(); cnt++) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException ie) {
                    // Ignore
                }
            }

            if (sipStarterThread.isAlive()) {
                sipStarterThread.interrupt();
            }
        }

        // If detect is caused by SIP settings dialog, wait for it
        while (!sipSystemIsRunning || isQueuedSipSystemRestart()) {
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException ie) {
                    // Ignore
            }

        }

        stopSipSystem();

        // End the program
        System.exit(0);
    }

    /**
     * Stops the SIP controller, disconnects any call and unregisters.
     *
     */
    private void stopSipSystem() {

        synchronized (sipStartStopMutex) {
            gui.setRegDetecting("Stopping SIP");

            sipSystemIsRunning = false;

            // Disconnect
            if (callProcessor != null) {
                callProcessor.bye();
                callProcessor = null;
            }

            // Unregister
            sipUnregisterAll();

            // Allow 2 seconds for the unregister process
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                // Ignore this.
            }

            // Stop the SIP client
            if (sc != null) {
                sc.stop();
                sc = null;
            }

            // Stop the STUN stack
            if (stunStack != null) {
                stunStack.stop();
            }

            gui.setRegNa();
        }
    }

     /**
	* Handles incoming options.
	* 
	* @param options The options dialog.
	*/
	public void signalIncomingOptions(IncomingOptionsDialog options) {
		if(currentSdpManager==null) {
			deploySdpSettings(true);
		}

		String sdp = "";
		
		if(currentSdpManager!=null) {
			sdp=currentSdpManager.getSdp();
		}

		if(callProcessor!=null) {
			options.sendResponse(true,sdp);
		}
		else {
			options.sendResponse(false,sdp);
		}
	}

    /**
     * Handles incoming call.
     *
     */
    public void signalIncomingCall(IncomingCallDialog call) {

        if (call.getSdp().indexOf("eConf 3.5.1") >= 0) {
            isEconf351 = true;
        }

        if (callProcessor != null) {
            call.busy();
            return;
        }
        callProcessor = call.getCallProcessor();
        remoteUserAddress = call.getRemoteUserAddress();

        // Show a popup dialog to the user
        gui.toFront();
        incomingCallDialog =
                DialogFactory.showAnswerDialog(remoteUserAddress);

        if (appSettings.isAlertingEnabled()) {
            System.out.println("Activating the Alert process\n");
            alert();
        }

        Thread ringThread = new Thread(new RingSignal());
        ringThread.setName("Ring signal");
        ringThread.start();

        call.ringing();

        int result = incomingCallDialog.getResult();

        ringThread.interrupt();

        // Handle the result
        if (result == ThreadedDialog.YES) {
            gui.clearTextAreas();
            gui.changeInfoLabel(language.getProperty
                                ("se.omnitor.tipcon1.AppController." +
                                 "MEDIA_SETUP"));

            negotiatedMedia = new Vector<SdpMedia>(0, 1);
            System.out.println("<<signalIncomingCall::" + call.getSdp() + ">>");
            if (call.getSdp().indexOf("eConf 3.5.1") >= 0) {
                isEconf351 = true;
            }
            try {
                deploySdpSettings(true);

                String sdp =
                        currentSdpManager.negotiate(call.getSdp(),
                        negotiatedMedia);

                // Check that there are media available for the call.
                int a = negotiatedMedia.size();
                
                if (a == 0) {
                	call.notAcceptableHere("");
                    callProcessor = null;
                    gui.changeInfoLabel("");
                    DialogFactory.showErrorMessageDialog(language.getProperty("se.omnitor.tipcon1.AppController.NOT_ACCEPTABLE_HERE"));
                    gui.setTerminatedGui();
                }
                else {
                
                	call.answer("application", "sdp", sdp);

                	SdpMedia m;
                	Vector v;
                	for (int cnt = 0; cnt < a; cnt++) {
                		m = (SdpMedia) negotiatedMedia.elementAt(cnt);
                		v = m.getFormats();
                		System.out.println("Media " + cnt + ": " + v);
                		System.out.println("(PP = " + m.getPhysicalPort() + ")");
                	}
                }

            } catch (SdpException se) {
                logger.throwing(this.getClass().getName(),
                                "signalIncomingCall",
                                se);

                call.badRequest("SDP parse error");

                DialogFactory.showErrorMessageDialog
                        (language.getProperty("se.omnitor.tipcon1." +
                                              "AppController." +
                                              "SIP_SEND_PROBLEM"));
                callProcessor = null;
                gui.setTerminatedGui();
            }

            /* Error handling should be done here, see below for example.
                  catch (Exception see) {
               try {
             sc.sipCancel();
               }
               catch (Exception e) {
             // Ignore exceptions here, there is anyway nothing
             // here to do if an error occurs.
               }
               DialogFactory.showErrorMessageDialog
             (language.getProperty("se.omnitor.tipcon1." +
              "AppController." +
              "SIP_SEND_PROBLEM"));
               gui.setTerminatedGui();
                  }
             */
        } else if (result == ThreadedDialog.NO ||
                   result == ThreadedDialog.USER_CLOSED_WINDOW) {

            call.decline();
            callProcessor = null;
        }

    }

    public String getRemoteUserAddress() {
        return remoteUserAddress;
    }

    /**
     * Handles an established call.
     *
     */
    public void signalEstablishedCall(CallProcessor cp) {

        this.callProcessor = cp;

        System.out.println("<<signalEstablishedCall::" + cp.getRemoteSdp() +
                           ">>");
        // nr client ringer sipcon blir d nullpexception
        String cpString = cp.getRemoteSdp();
        if (cpString != null && cpString.indexOf("eConf 3.5.1") >= 0) {
            isEconf351 = true;
        }
        System.out.println("<<signalEstablishedCall::isEconf351" + isEconf351 +
                           ">>");
        gui.setPendingGui();

        // Inform the user
        if (outgoingCallDialog != null) {
            outgoingCallDialog.setInfoText
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "MEDIA_SETUP"));
        }


        // Store the remote address
        if (appSettings.isRealtimePreviewEnabled()) {

            // Get a nice name which can be presented in GUI
            String addr = cp.getNiceRemoteName();
            if (addr == null || addr.trim().equals("")) {
                addr = "You";
            }
            else {
                int ix1 = addr.indexOf("<");
                int ix2 = addr.indexOf(">");
                if (ix1 < ix2 && (ix2-ix1 > 1)) {
                    addr = addr.substring(ix1+1, ix2-ix1);
                }

                if (addr.startsWith("sip:")) {
                    addr = addr.substring(4);
                }

                addr = addr.split("@")[0];
                String[] addrSplit = addr.split("\\.");
                if (addrSplit.length == 4) {
                    addr = "You";
                }
                else {
                    addr = addrSplit[0];
                }
            }
            t140Panel.setRemoteLogName(addr);

        }



        // An outgoing call has no negotiated media (as negotiation was taken
        // place at remote's side)
        if (negotiatedMedia == null) {
            try {
                negotiatedMedia = SdpManager.getFormats(cp.getRemoteSdp());

                if (negotiatedMedia == null) {
                    // We want to get into the catch clause.
                    throw new SdpException("Dummy exception");
                }

                // Now, move the psysical ports from local config to current
                // negotiated media.
                // Also make number of redundant generations in T140 RED become the value
                // of the local setting, instead of using remote's value.
                int len = negotiatedMedia.size();
                for (int cnt=0; cnt<len; cnt++) {
                    SdpMedia m1 = (SdpMedia) negotiatedMedia.get(cnt);
                    SdpMedia m2 = currentSdpManager.getMedia(m1.getType());
                    if (m2 != null) {
                        m1.setPhysicalPort(m2.getPhysicalPort());
                    }
                    
                    // Look for T140 RED, in order to change value of redundant generations
                    // to the application's settings (instead of using remote's value)
                    Vector tmpVector = m1.getFormats();
                    int tmpvLen = tmpVector.size();
                    for (int vcnt=0; vcnt<tmpvLen; vcnt++) {
                    	se.omnitor.protocol.sdp.Format format = (se.omnitor.protocol.sdp.Format)tmpVector.elementAt(vcnt);
                    	if (format instanceof RedFormat) {
                    		((RedFormat)format).setGenerations(appSettings.getRedundantRtpGenerations());
                    	}
                    }
                }
            } catch (SdpException e) {
                logger.throwing(this.getClass().getName(),
                                "signalEstablishedCall", e);

                // TODO:
                // Strange SDP from remote, disconnect call here!

                negotiatedMedia = null;

                outgoingCallDialog.dispose();
                DialogFactory.showErrorMessageDialog(language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "SDP_PARSE_ERROR"));

                cp.bye();
                return;
            }

        }

        startMedia();

        if (outgoingCallDialog != null) {
            outgoingCallDialog.dispose();
        }

        // Change GUI
        gui.setEstablishedGui();
        gui.setRemoteUserInfo(cp.getNiceRemoteName());
        gui.changeInfoLabel(language.getProperty("se.omnitor.tipcon1." +
                                                 "AppController." +
                                                 "CONNECTED"));
    }

    /**
     * Handles incoming cancellation.
     *
     */
    public void signalCancelledIncomingCall(IncomingCallDialog dialog) {

        // Hide call popup
        if (incomingCallDialog != null) {
            incomingCallDialog.dispose();
        }

        // Inform the user
        gui.changeInfoLabel
                (language.getProperty("se.omnitor.tipcon1.AppController." +
                                      "MISSED_CALL_FROM") + " " +
                 remoteUserAddress);

        callProcessor = null;

    }

    /**
     * Handles terminated call signal.
     *
     */
    public void signalTerminatedCall(CallProcessor cp) {

        // Change GUI
        gui.setTerminatedGui();
        gui.changeInfoLabel(language.getProperty("se.omnitor." +
                                                 "tipcon1." +
                                                 "AppController." +
                                                 "TERMINATED"));

        isMuted = false;
        gui.setMute(false);

        gui.repaint();

        if (mediaManager != null) {
            mediaManager.stopAll();
            mediaManager = null;
        }
        negotiatedMedia = null;
        callProcessor = null;

        // Remove the remote address
        remoteUserAddress = null;
         if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
             restartVideo();
         }
        //settingsDialog.actionPerformed
        // (new ActionEvent(new Button("OK"), 666,"OK"));

        //deploySdpSettings(true);

    }

    /**
     * Handles call progress signal.
     *
     */
    public void signalOutgoingCallProgress(CallProcessor cp, int statusCode) {

        System.out.println("<<signalOutgoingCallProgress::" + cp.getRemoteSdp() +
                           ">>");

        switch (statusCode) {
        case 100:

            // Inform the user only if nothing has been informed yet (NIST
            // SIP tends to turn the order of 100 and 180, so that 100 comes
            // after 180).
            if (outgoingCallDialog != null && !outgoingCallProgressReported) {
                outgoingCallDialog.setInfoText
                        (language.getProperty(
                                "se.omnitor.tipcon1.AppController." +
                                "HOST_CONTACTED"));
            }
            break;

        case 182:

            // Inform the user
            if (outgoingCallDialog != null) {
                outgoingCallDialog.setInfoText
                        (language.getProperty(
                                "se.omnitor.tipcon1.AppController." +
                                "QUEUED"));
            }
            break;

        case 180:

            // Inform the user
            if (outgoingCallDialog != null) {
                outgoingCallDialog.setInfoText
                        (language.getProperty(
                                "se.omnitor.tipcon1.AppController." +
                                "WAITING_FOR_ANSWER"));
            }
            break;

        default:
        }

        if (statusCode != 100) {
            outgoingCallProgressReported = true;
        }

    }

    /**
     * Handle outgoing call error signal.
     *
     */
    public void signalOutgoingCallError(OutgoingCallDialog dialog,
                                        int statusCode) {

        if (outgoingCallDialog != null) {
            outgoingCallDialog.dispose();
        }
        callProcessor = null;

        switch (statusCode) {

        case 400:
            DialogFactory.showErrorMessageDialog("Bad request.");
            break;

        case 403:

            // Allan eC always responds wtith 403 when rejecting a call.
            // To make this application compatible with Allan eC, we think
            // 403 means "rejected". In fact, it isn't completely wrong.
            DialogFactory.showInformationMessageDialog
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "CALLING"),
                     language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "REJECTED"));
            break;

        case 404:

            DialogFactory.showErrorMessageDialog
                    (dialog.getRemoteSipAddress() + " " +
                     language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "DOES_NOT_EXIST"));
            break;

        case 408:

            DialogFactory.showInformationMessageDialog
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "CALLING"),
                     language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "TIMEOUT"));
            break;

        case 480:

            DialogFactory.showInformationMessageDialog
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "CALLING"),
                     language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "USER_NOT_ONLINE"));
            break;

        case 486:
        case 600:

            DialogFactory.showInformationMessageDialog
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "CALLING"),
                     language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "BUSY"));
            break;

        case 487:

            // Request terminated, probably due to our cancellation.
            // Be happy, do nothing here.
            break;

        case 488:
        case 606:
            DialogFactory.showInformationMessageDialog
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "CAlLING"),
                     language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "NO_MEDIA_MATCH"));
            break;

        case 603:

            // Inform the user, change the GUI
            DialogFactory.showInformationMessageDialog
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "CALLING"),
                     language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "REJECTED"));
            break;

        default:
            DialogFactory.showErrorMessageDialog("SIP response code " + statusCode);

        }

        // Reset GUI
        gui.setTerminatedGui();

        // Remove the remote address
        remoteUserAddress = null;
    }

    /**
     * Handles registration failiure signal.
     *
     */
    public void signalRegistrationError(RegisterDialog dialog,
                                        int statusCode) {
        // Ask user for password
        /*
              if (sme.getEventName().startsWith("401")) {
           final String address = sme.getRemoteUserAddress();
           Thread t = new Thread() {
            public void run() {
          String password =
             gui.showPasswordDialog
             (language.getProperty("se.omnitor.tipcon1." +
               "AppController.REG_TO") +
              " " + address.split("@")[1],
              language.getProperty("se.omnitor.tipcon1." +
               "AppController." +
               "PASSWORD_REQUIRED_" +
               "FOR") + address);

          if (password == null) {
             gui.setRegFail(address);
          }
          else {
             sc.sipRegister(password);
             gui.setRegPending();
          }
            }
         };
         t.setName("Password dialog");
           t.start();
              }
         */

        int failed = 0;
        int trying = 0;
        int ok = 0;

        RegisterProcessor rp = dialog.getRegisterProcessor();

        for (int cnt = 0; cnt < registerProcessor.length; cnt++) {
            if (rp == registerProcessor[cnt]) {
                sipRegistrarStatus[cnt] = FAIL;
                failed++;
            } else {
                if (sipRegistrarStatus[cnt] == FAIL) {
                    failed++;
                } else if (sipRegistrarStatus[cnt] == TRYING) {
                    trying++;
                } else if (sipRegistrarStatus[cnt] == OK) {
                    ok++;
                }
            }
        }

        if (trying == 0) {
            if (ok == 0) {
                gui.setRegFail("");
            } else {
                gui.setRegPartlyOk();
            }
        }

    }

    /**
     * Handles registration success signal.
     *
     */
    public void signalRegistrationSuccess(RegisterProcessor rp) {

        int failed = 0;
        int trying = 0;
        int ok = 0;

        if (rp.getLastExpires() > 0) {
            for (int cnt = 0; cnt < registerProcessor.length; cnt++) {
                if (rp == registerProcessor[cnt]) {
                    sipRegistrarStatus[cnt] = OK;
                    ok++;
                } else {
                    if (sipRegistrarStatus[cnt] == FAIL) {
                        failed++;
                    } else if (sipRegistrarStatus[cnt] == TRYING) {
                        trying++;
                    } else if (sipRegistrarStatus[cnt] == OK) {
                        ok++;
                    }
                }
            }

            if (trying == 0) {
                if (failed == 0) {
                    gui.setRegOk("");
                } else {
                    gui.setRegPartlyOk();
                }
            }

        }

    }

    /**
     * Handles call transfer error signal.
     *
     */
    public void signalCallTransferError(int statusCode) {

    }

    /**
     * Handles call transfer success signal.
     *
     */
    public void signalCallTransferSuccess(CallProcessor cp) {

    }

    /**
     * Handles questions about call transfer.
     *
     */
    public void askForCallTransferAcceptance(IncomingReferDialog dialog) {

    }

    /*
        case SipMessageEvent.MOVED: // Remote has moved

     if (outgoingCallDialog != null) {
       outgoingCallDialog.dispose();
     }

            // Inform user
     DialogFactory.showInformationMessageDialog
       ("Calling", "User has moved to " + sme.getDetails());
            break;


        case SipMessageEvent.TRANSFERRING: // Remote is transferring call

            // Inform user
            gui.changeInfoLabel(sme.getDetails());
            break;


        case SipMessageEvent.NOT_TRANSFERRED: // Transfer has been aborted

            // Inform user
            gui.changeInfoLabel(sme.getDetails());
            break;
     */



    /**
     * Starts all media.
     *
     */
    private void startMedia() {

        System.out.println("<<startMedia::>>");
        String al;
        String vl;

        if (audioIsActivated) {
            try {
                al = audioLocator[audioDevice];
            } catch (Exception e) {
                logger.throwing(this.getClass().getName(), "startMedia", e);

                al = "";
            }
        } else {
            al = "";
        }


        if (AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1 && videoIsActivated) {
            try {
                vl = videoLocator[videoDevice];
            } catch (Exception e) {
                logger.throwing(this.getClass().getName(), "startMedia", e);

                vl = "";
            }
        } else {
            vl = "";
        }


        if (mediaManager != null) {
            mediaManager.stopAll();
        }

        mediaManager = new MediaManager(this,
                                 appSettings,
                                 negotiatedMedia,
                                 txTextBuffer,
                                 rxTextBuffer,
                                 al,
                                 vl,
                                 this.isEconf351,
                                 transmitVideoDataSource,
                                 appSettings.getVideoBitrate(),
                                 videofps);

        mediaManager.startAll();

    }

    /**
     * Checks what type of media is supported by the computer and
     * informs the SIP client about it.
     *
     * @return A Vector with all supported codecs as DeviceContainer objects.
     */
    public Collection initializeSupportedMedia() {

        Vector deviceList;
        Vector codecList;
        Codec codec = null;

        Format[] outputFormats;

        Vector<String> rtpFormats = new Vector<String>(1, 1);

        DataSource ds = null;
        Processor processor = null;
        TrackControl[] tracks;

        Vector<DeviceContainer> deviceContainers = new Vector<DeviceContainer>(1, 1);
        int deviceListSize;
        int codecListSize;

        RTPPayloadConstants rtpPc = new RTPPayloadConstants();
        String tempCodec;

        //Insert the wdm devices, FIXME get the list first and then check for civil devices
        //if there are non add them with the following command
        //new CaptureDevicePlugger().addCaptureDevices();
        
        if (AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
        	AppControllerSpecific.initSjmf(language);
        }

        //Get capture devices
        deviceList = CaptureDeviceManager.getDeviceList(null);

        //Get available codecs
        codecList =
                PlugInManager.getPlugInList(null, null, PlugInManager.CODEC);
            System.out.println("Codeclist: " + codecList.toString());



        // Loop through every codec
        codecListSize = codecList.size();

        for (int i = 0; i < codecListSize; i++) {
        //for (int i = codecListSize - 1; i >= 0; i--) {

            // Get the next codec in the list
            try {
                codec = (Codec) Class.forName((String) codecList.elementAt(i)).newInstance();


                // Get all outputformats for the codec, loop through them
                outputFormats = codec.getSupportedOutputFormats(null);
                // Only pick the usable formats we want

                if (((codec.toString().indexOf("g723")>=0) ||
                    (codec.toString().indexOf("gsm")>=0) ||
                    (codec.toString().indexOf("ulaw")>=0) ||
                    (codec.toString().indexOf("dvi")>=0) ||
                    ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && codec.toString().indexOf("sjmf")>=0) ||
                    (codec.toString().indexOf("h263")>=0) ||
                    (codec.toString().indexOf("ima4")>=0))) {

                    for (int j = 0; j < outputFormats.length; j++) {

                            // Check if the output format if of an RTP type
                            if (outputFormats[j].getEncoding() != null &&
                                outputFormats[j].getEncoding().toUpperCase(Locale.US).
                                endsWith("RTP") &&
                                !rtpFormats.contains(outputFormats[j].getEncoding())) {
                                    // Add the format to the list of available formats
                                    rtpFormats.add(outputFormats[j].getEncoding());
                                }
                    }
                }
            } catch (InstantiationException e) {
                //  Ignore the error, move on to the next codec instead.
            	logger.throwing(this.getClass().getName(), "initializeSupportedMedia", e);
			} catch (IllegalAccessException e) {
                //  Ignore the error, move on to the next codec instead.
            	logger.throwing(this.getClass().getName(), "initializeSupportedMedia", e);
			} catch (ClassNotFoundException e) {
                //  Ignore the error, move on to the next codec instead.
            	logger.throwing(this.getClass().getName(), "initializeSupportedMedia", e);
			}
        }
        //new Thread(new TestThread()).start();

        // Verify which codecs works for which device, try to start the device
        // with every codec
        deviceListSize = deviceList.size();
        for (int k = 0; k < deviceListSize; k++) {
            Vector<String> compatibleFormats = new Vector<String>(1, 1);
            String type = "";
            int rtpFormatsSize;

            // Create dataSource
            String locator = null;
            try {
                System.out.println("Object = " +
                                   ((CaptureDeviceInfo) deviceList.get(k)).
                                   getLocator());
                locator = ((CaptureDeviceInfo) deviceList.get(k)).getLocator().
                          toString();

            } catch (NullPointerException e) {
                System.out.println("Object = null");
            }

            if ((locator != null && !locator.startsWith("javasound:"))) {
                boolean allgood = true;
                try {
                    ds = javax.media.Manager.createDataSource
                         (((CaptureDeviceInfo) deviceList.get(k)).getLocator());
                } catch (Exception e) {
                    // This was ignored before, I don't think ignoration is a good
                    // idea. Lets exit here instead. /Andreas
                    allgood = false;
                }

                if (allgood){
                    // Create processor
                    try {
                        processor = javax.media.Manager.createProcessor(ds);
                    } catch (NoProcessorException npe) {

                        // Couldn't create processor
                        return null;

                    } catch (IOException ioe) {

                        // Couldn't create processor
                        return null;
                    }

                    // Connect the datasource
                    try {
                        ds.connect();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        // Ignore the error
                    }

                    // Wait for the processor to configure
                    if (!waitForState(processor, Processor.Configured)) {

                        // Couldn't configure processor
                        return null;
                    }

                    // Get the tracks
                    tracks = processor.getTrackControls();
                    if (tracks == null || tracks.length < 1) {

                        // Couldn't find tracks in processor
                        return null;
                    }

                    // Get the correct codec
                    processor.setContentDescriptor(new ContentDescriptor
                                                   (ContentDescriptor.RAW_RTP));

                    // Loop through all tracks
                    for (int cnt = 0; cnt < tracks.length; cnt++) {
                        if (tracks[cnt].isEnabled()) {

                            // Loop through all supported formats in the track
                            Format[] supported = tracks[cnt].getSupportedFormats();

                            if (supported != null && supported.length > 0) {
                                if (supported[0] instanceof AudioFormat) {
                                    type = "audio";
                                } else if (supported[0] instanceof VideoFormat) {
                                    type = "video";
                                }

                                for (int scnt = 0; scnt < supported.length; scnt++) {

                                    rtpFormatsSize = rtpFormats.size();
                                    for (int l = 0; l < rtpFormatsSize; l++) {

                                        // Check for compatible format
                                        tempCodec = (String) rtpFormats.get(l);
                                        if (supported[scnt].isSameEncoding(tempCodec) &&
                                            !compatibleFormats.contains
                                            (tempCodec.split("/")[0]) &&
                                            (rtpPc.getPayloadType(tempCodec.split("/")[
                                                                  0])
                                             != -1)) {

                                            // Add to compatible formats list
                                            compatibleFormats.add
                                                    (tempCodec.split("/")[0]);
                                        }
                                    }
                                }
                            }
                        }
                    }


                    // Add all compatible formats to the device container
                    deviceContainers.add
                            (new DeviceContainer(((CaptureDeviceInfo) deviceList.
                                                  get(k)).
                                                 getLocator().toExternalForm(),
                                                 compatibleFormats,
                                                 ((CaptureDeviceInfo) deviceList.
                                                  get(k))
                                                 .getFormats(),
                                                 type,
                                                 ((CaptureDeviceInfo) deviceList.
                                                  get(k))
                                                 .getName()));

                    // Stop the datasource
                    try {
                        ds.stop();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();

                        /*
                         * Stop failed, ignore exception and continue.
                         *
                         */
                    }


                    // Remove the processor
                    processor.deallocate();
                    processor = null;

                    // Sleep for a second to be sure the removal is done
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        /*
                         * The sleep was interrupted, ignore exception and continue.
                         *
                         */
                    }

                    // Disconnect the datasource
                    ds.disconnect();
                    ds = null;

                    // Reset the list of compatible formats
                    compatibleFormats = null;
                }
            }
        }

        // Count number of devices for audio and video
        nbrOfAudioDevices = 0;
        nbrOfVideoDevices = 0;
        DeviceContainer tempDc;

        supportedMedia = deviceContainers;

        if (supportedMedia != null) {
            int smSize = supportedMedia.size();

            for (int cnt = 0; cnt < smSize; cnt++) {

                tempDc = (DeviceContainer) supportedMedia.get(cnt);
                if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && tempDc.getType().equals("video")) {
                    nbrOfVideoDevices++;
                } else if (tempDc.getType().equals("audio")) {
                    nbrOfAudioDevices++;
                }
            }
        }

        return deviceContainers;
    }

    /**
     * Wait for the JMF processor to reach a given state.
     *
     * @param processor The processor
     * @param state The state to wait for
     */
    private synchronized boolean waitForState(Processor processor, int state) {
        boolean continueWaiting;

        processor.addControllerListener(new StateListener());
        processorChangeFailed = false;

        switch (state) {

        case Processor.Configured:
            processor.configure();
            break;

        case Processor.Realized:
            processor.realize();
            break;

        default:
            // Do nothing for other types
        }

        // Wait for event
        continueWaiting =
                processor.getState() < state && !processorChangeFailed;

        while (continueWaiting) {
            synchronized (getStateLock()) {
                try {
                    getStateLock().wait();
                } catch (InterruptedException ie) {
                    return false;
                }
            }

            continueWaiting =
                    processor.getState() < state && !processorChangeFailed;
        }

        return!processorChangeFailed;
    }

    /**
     * Change the mute on the local audio.
     *
     * @param mute Whether the audio should be muted
     */
    public void localMute(boolean mute) {
        if (mute == isMuted) {
            // Nothing to do.

            return;
        }

        mediaManager.localMute(mute);

    }

    /*
        int outStreamsSize;

        outStreamsSize = outStreams.size();

        // Find the outgoing audio stream
        for (int i=0;i<outStreamsSize;i++) {
            if (outStreams.get(i) instanceof AudioVideoTransmit) {
                if (!(((AudioVideoTransmit)(outStreams.get(i))).getLocator().
                     toUpperCase().split(":")[0]).equals("VFW")) {
     */

    /*
     * Start or stop RTP stream depending on if the audio
     * is muted already or not. Also change the GUI. The
     * hang up button will remain unenabled until the stream
     * has been started/stopped, this will prevent the user to
     * hang up the call while a stream is not correctly
     * established.
     */
    /*
       if (!mute) {
      isMuted = false;
      ((AudioVideoTransmit)(outStreams.get(i))).start();
       } else {
      isMuted = true;
      ((AudioVideoTransmit)(outStreams.get(i))).stop();
       }

                }

                // Mute only one stream, therefore break out here.
                break;
            }

        }
         }
     */

    /**
     * Gets the state lock
     *
     * @return The state lock
     */
    private Integer getStateLock() {
        return stateLock;
    }

    /**
     * Sets the maximum incoming CPS value
     *
     * @param maxCps The value
     */
    public void setMaxIncomingCps(int maxCps) {
        this.maxIncomingCps = maxCps;
        appSettings.setMaxIncomingCps(maxCps);
    }

    /**
     * Retrieves the maximum incoming CPS value
     *
     * @return The value
     */
    public int getMaxIncomingCps() {
        return maxIncomingCps;
    }

    /**
     * Sets the buffer time.
     *
     * @param bufferTime The value for the buffer time in milliseconds
     */
    public void setTextBufferTime(long bufferTime) {
        if (appSettings.useSendOnCR()) {
            txTextBuffer.setBufferTime(100);
        } else {
            txTextBuffer.setBufferTime((int) bufferTime);
            appSettings.setBufferTime(bufferTime);
        }
    }


    /**
     * Sets the default local text
     *
     * @param port The default local text port
     */
    /*
    public void setLocalTextPort(int port) {
        this.localTextPort = port;
    }
*/

    /**
     * Retrieves the local text port
     *
     * @return The local text port
     */
    /*
    public int getLocalTextPort() {
        return localTextPort;
    }
*/

    /**
     * Method to call the external alerting binary
     */
    public void alert() {
        try {
            Runtime.getRuntime().exec("C:\\WINDOWS\\system32\\alert2.exe");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Sets the default local video port
     *
     * @param port The default local video port
     *//*
    public void setLocalVideoPort(int port) {
        this.localVideoPort = port;
    }
*/

    /**
     * Retrieves the local video port
     *
     * @return The local video port
     *//*
    public int getLocalVideoPort() {
        return localVideoPort;
    }
*/

    /**
     * Sets the default local audio port
     *
     * @param port The default local audio port
     */
    /*
    public void setLocalAudioPort(int port) {
        this.localAudioPort = port;
    }
*/

    /**
     * Retrieves the local audio port
     *
     * @return The local audio port
     */
    /*
    public int getLocalAudioPort() {
        return localAudioPort;
    }
*/

    /**
     * Sets all active audio formats
     *
     * @param formatList An array with all active audio formats. The active
     * format for audio device N should be in the N:th position in the array.
     * N is an internal number representing an audio device.
     */
    public void setAudioFormat(AudioFormat[] formatList) {
        audioFormat = formatList;
    }

    /**
     * Gets all active audio formats
     *
     * @return An array with all active audio formats. The active audio format
     * for audio device N is in the N:th position in the array.
     * N is an internal number representing an audio device.
     */
    public AudioFormat[] getAudioFormats() {
        return audioFormat;
    }

    /**
     * Sets all active audio codecs
     *
     * @param codecs A Vector containing String objects of all active audio
     * codecs.
     */
    public void setAudioCodecs(Vector<String> codecs) {
        audioCodecs = codecs;
    }

    /**
     * Gets all active audio codecs
     *
     * @return A Vector containing String objects of all active audio codecs.
     */
    public Vector<String> getAudioCodecs() {
        return audioCodecs;
    }

    /**
     * Sets the active audio device
     *
     * @param device The internal index number of a local audio device
     */
    public void setAudioDevice(int device) {
        audioDevice = device;
    }

    /**
     * Gets the active audio device.
     *
     * @return An internal index number of the current active audio device.
     */
    public int getAudioDevice() {
        return audioDevice;
    }

    /**
     * Sets all active video formats
     *
     * @param formatList An array with all active video formats. The active
     * format for video device N should be in the N:th position in the array.
     * N is an internal number representing a video device.
     */

    public void setVideoFormat(VideoFormat[] formatList) {
        videoFormat = formatList;
        if (videoFormat != null && videoFormat.length > 0) {
        	appSettings.setResolution(formatToString(videoFormat[0]));
        }
    }

    /**
     * Gives a string representation of the given video format.
     *
     * @param format A VideoFormat
     *
     * @return A string representation of the format
     */

    public String formatToString(VideoFormat format) {
        Dimension tempDim;
        String returnStr;

        tempDim = format.getSize();

        returnStr =
            (int)tempDim.getWidth() + "x" +
            (int)tempDim.getHeight() + " (" +
            format.getEncoding() + ")";
        //System.out.println(returnStr);
        return returnStr;

    }

    /**
     * Gets all active video formats
     *
     * @return An array with all active video formats. The active video format
     * for video device N is in the N:th position in the array.
     * N is an internal number representing a video device.
     */

    public VideoFormat[] getVideoFormats() {
        return videoFormat;
    }

    /**
     * Sets active video codecs.
     *
     * @param codecs A Vector containing String objects  of all active video
     * codecs.
     */

    public void setVideoCodecs(Vector<String> codecs) {
        videoCodecs = codecs;
    }


    /**
     * Gets all active video codecs.
     *
     * @return A Vector containing String objects with all active video codecs.
     */

    public Vector<String> getVideoCodecs() {
        return videoCodecs;
    }

    public int getvideofps (){
        return videofps;
    }

    public void setvideofps (int fps) {
        videofps = fps;
    }

    public void setVideoBitrate (int bitrate) {
        appSettings.setVideoBitrate(bitrate);
    }

    public int getVideoBitrate () {
        return appSettings.getVideoBitrate();
    }


    /**
     * Specifies which video device should be in use.
     *
     * @param device The internal index number of a local video device
     */

    public void setVideoDevice(int device) {
        videoDevice = device;
    }

    /**
     * Gets the internal index number of the current local video device.
     *
     * @return The internal index number of the local video device.
     */

    public int getVideoDevice() {
        return videoDevice;
    }

    /**
     * Gets the local SIP port.
     *
     * @return The local SIP port.
     */
    public int getLocalPort() {
        return sc.getLocalPort();
    }

    /**
     * Sets the local SIP port. The default port for SIP traffic is 5060. If
     * the SIP port is already in use, an error message dialog will be shown.
     * This function also resets the SIP kernel.
     *
     * @param port The port number
     */
    /*
         public void setLocalSipPort(int port) {
      boolean reg = false;

      try {
     sc.setPort(port);
     this.sipPort = port;

     if (registerProcessor != null) {
       registerProcessor.register(registrarUsername,
        registrarPassword, 0);
       reg = true;
     }

     sc.stop();
     sc.start();

     if (reg) {
       sc.register(userSipAddress, registrarAddress,
       registrarUsername, registrarPassword);
     }


      }
      catch (Exception e) {
     DialogFactory.showErrorMessageDialog
       (language.getProperty("se.omnitor.tipcon1.AppController." +
          "SIP_PORT_ERROR"));
     System.exit(-1);
      }
         }
     */

    /**
     * Restarts SIP kernel
     *
     */
    public void restartSipSystem() {
    	String NewLocalIpAddress;
    	stopSipSystem();
    	
    	try {
    		if(null != appSettings.getNetIfMacAddr()){
	    		NewLocalIpAddress = getMatchingIpAddress(appSettings.getNetIfMacAddr());
	    		if(null != NewLocalIpAddress){
		    		if(NewLocalIpAddress.compareTo(localIpAddress) != 0){
		    			if("" == NewLocalIpAddress){
		            		localIpAddress = InetAddress.getLocalHost().getHostAddress();
		            	}else{
		            		localIpAddress = NewLocalIpAddress;
		            	}
		    		}
	    		}else{
	    			localIpAddress = InetAddress.getLocalHost().getHostAddress();
	    		}
    		}else{
    			localIpAddress = InetAddress.getLocalHost().getHostAddress();
    		}	
    	} catch (UnknownHostException e){
    		e.printStackTrace();
        }

        try {
            startSipSystem();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setQueuedSipSystemRestart (boolean queuedRestart) {
        this.queuedSipSystemRestart = queuedRestart;
    }

    public boolean isQueuedSipSystemRestart() {
        return queuedSipSystemRestart;
    }

    /**
     * Specifies whether the text should be activated.
     *
     * @param activated true if the text should be activated, false otherwise
     */
    public void setTextActivate(boolean activated) {
        textIsActivated = activated;
    }

    /**
     * Indicated whether the text is activated.
     *
     * @return true if the text is activated, false otherwise
     */
    public boolean isTextActivated() {
        return textIsActivated;
    }


    /**
     * Specifies whether the video should be activated.
     *
     * @param activated true if the video should be activated, false otherwise
     */

    public void setVideoActivate(boolean activated) {
        videoIsActivated = activated;
        //videoIsActivated = false;
        gui.refreshGui();
    }

    /**
     * Indicates whether the video is activated.
     *
     * @return true if video is activated, false otherwise
     */

    public boolean isVideoActivated() {
        return videoIsActivated;
    }


    /**
     * Specifies whether the audio should be activated.
     *
     * @param activated true is the audio should be activated, false otherwise
     */

    public void setAudioActivate(boolean activated) {
        audioIsActivated = activated;
    }

    /**
     * Indicates whether the audio is activated.
     *
     * @return true if audio is activated, false otherwise
     */
    public boolean isAudioActivated() {
        return audioIsActivated;
    }

    /**
     * Returns the number of video devices in the system
     *
     * @return The number of video devices in the system
     */

    public int getNbrOfVideoDevices() {
        return nbrOfVideoDevices;
    }

    /**
     * Returns the number of audio devices in the system
     *
     * @return The number of audio devices in the system
     */
    public int getNbrOfAudioDevices() {
        return nbrOfAudioDevices;
    }

    /**
     * Creates an audio video player for the local video
     *
     * @param locator The locator to the video source, e.g. vfw://1.
     */
    private void createAudioVideoPlayer(String locator) {

        DataSource ds = null;
        DataSource cloneableDataSource = null;

        // Create datasource
        try {
            ds = javax.media.Manager.createDataSource
                 (new MediaLocator(locator));
                 //(new MediaLocator("civil:\\\\?\\usb#vid_0471&pid_0308&mi_00#6&cad1b2f&0&0000#{65e8773d-8f56-11d0-a3b9-00a0c9223196}\\global"));

            cloneableDataSource =
                    javax.media.Manager.createCloneableDataSource(ds);

            if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
                transmitVideoDataSource =
                        ((SourceCloneable) cloneableDataSource).createClone();
            }
        } 
        catch (javax.media.NoDataSourceException e) {

            // Datasource could not be created, exit function.
            return;
        }
        catch (IOException e) {

            // Datasource could not be created, exit function.
            return;
        }

        // Initialize the local video
        System.out.println(
                "****************************************************CREATING");

         if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
             avPlayer = new AudioVideoPlayer(cloneableDataSource,
                                             videoFormat[videoDevice],
                                             gui.getVideoSelfPanel(),
                                             //gui.getAudioRemotePanel(),
                                             1);
         }
    }

    /**
     * Remove the audio video player, the local video
     *
     */
    public void destroyAudioVideoPlayer() {
        if (avPlayer != null) {
            avPlayer.close();
            avPlayer = null;
        }

         gui.resetLocalVideoPanel();

        //System.gc();
    }

    /**
     * Calls anoter host. This is usually invoked by the call button listsener.
     * First, it checks that the sipAddress is ok.
     *
     * @param sipAddress The SIP address to call
     */
    public void call(String sipAddress) {
    	outgoingCallProgressReported = false;
        
    	// Give an error message if the address is empty
        if (sipAddress.trim().length() == 0) {
            DialogFactory.showErrorMessageDialog
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "NO_SIP_ADDRESS"));
            return;
        }

        // If no network is present, don't call
        if (!detectLocalIp()) {
            DialogFactory.showErrorMessageDialog
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "NO_NETWORK"));
            return;
        }

        // Give an error message if no medias are chosen
        if ((!videoIsActivated && (AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1))&& !textIsActivated && !audioIsActivated) {
            DialogFactory.showErrorMessageDialog
                    (language.getProperty("se.omnitor.tipcon1.AppController." +
                                          "ACTIVATE_MEDIA"));
            return;
        }

        // Check if SIP address has @ sign
        boolean hasAtSign = (sipAddress.indexOf('@') != -1);

        // Add dial domain if no @ sign is present in the address and the
        // address is not a host address
        if (!hasAtSign) {
            if (appSettings.getDialDomain() != null &&
                !appSettings.getDialDomain().equals("")) {
                sipAddress += "@" + appSettings.getDialDomain();
            }
        }

        // Wait if SIP kernel is being restarted
        if (!sipSystemIsRunning || isQueuedSipSystemRestart()) {

            ThreadedDialog td =
              DialogFactory.showWaitDialog(language.getProperty
                                           ("se.omnitor.tipcon1.AppController." +
                                            "WAIT_FOR_DETECT"),
                                           DialogFactory.ABORT_BUTTON);

            while (!sipSystemIsRunning || isQueuedSipSystemRestart()) {
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException ie) {
                    // Ignore
                }
            }

            td.dispose();

            if (td.getResult() == ThreadedDialog.ABORT) {
                return;
            }
        }

        // Inform user and change GUI
        gui.setPendingGui();

        // Special setting for wx3.se, their server cannot handle text
        if (hasAtSign) {
            if (sipAddress.split("@")[1].equals("wx3.se")) {
                deploySdpSettings(false);
            } else {
                deploySdpSettings(true);
            }
        } else {
            if (sipAddress.equals("wx3.se")) {
                deploySdpSettings(false);
            } else {
                deploySdpSettings(true);
            }
        }

        // Callcall
        outgoingCallDialog =
                DialogFactory.showOutgoingCallDialog(sipAddress.trim());

        //try {
            OutgoingCallDialog sipDialog =
                    sc.invite(sipAddress.trim(), currentSdpManager.getSdp());

            // Something is wrong, maybe the domain of the calee's address
            if (sipDialog == null) {
            	outgoingCallDialog.dispose();

            	DialogFactory.showErrorMessageDialog
            	(language.getProperty("se.omnitor.tipcon1.AppController." +
            	"ADDRESS_ERROR"));

                gui.setTerminatedGui();
                return;
            }

            callProcessor = sipDialog.getCallProcessor();

            int res = outgoingCallDialog.getResult();

            if (res == ThreadedDialog.NO ||
                res == ThreadedDialog.USER_CLOSED_WINDOW) {

                sipDialog.cancel();
                gui.setTerminatedGui();
                callProcessor = null;
            }
/*
        }
        
        catch (Exception e) {
            callProcessor = null;

            // An error occured, show error message and reset the GUI
            outgoingCallDialog.dispose();
            int res = outgoingCallDialog.getResult();

            if (res != ThreadedDialog.NO &&
                res != ThreadedDialog.USER_CLOSED_WINDOW) {

                DialogFactory.showErrorMessageDialog
                        (language.getProperty("se.omnitor.tipcon1.AppController." +
                                              "CALL_ERROR"));
            }
            gui.setTerminatedGui();
        }
        */
    }

    /**
     * Signal a failiure in the processor change
     *
     */
    public void setFailed() {
        processorChangeFailed = true;
    }

    /**
     * Listener class for processor's state changes. If an error occures,
     * the error is signalled to other parts.
     *
     * @author Andreas Piirimets, Omnitor AB
     */
    private class StateListener implements ControllerListener {

        /**
         * Handles incoming controller update events. This is a part of the
         * ControllerListener interface.
         *
         * @param ce The incoming controller update event.
         */
        public void controllerUpdate(ControllerEvent ce) {

            // If an error occured, signal the error
            if (ce instanceof ControllerClosedEvent) {
                setFailed();
            }

            // Notify waiting thread
            synchronized (getStateLock()) {
                getStateLock().notifyAll();
            }
        }
    }


    /**
     * This class should be run as an own thread. It polls an RTP receive or
     * transmit class, waits for incoming RTP media and then informs the GUI
     * whether it failed or succeeded.
     *
     * @author Andreas Piirimets, Omnitor AB
     */
    /*
         private class CheckRtpStart implements Runnable {

        TextReceive textRcv;
        TextTransmit textTx;
        AudioVideoReceive avRcv;
        String codecName;
        int label;
     */
    /**
     * Initializes the class.
     *
     * @param receiveClass The class to poll, this should either be an
     * TextReceive, TextTransmit or an AudioVideoReceive object.
     * @param label The GUI status label to write the result in. (one of
     * the constants in ProgramWindow)
     * @param codecName The name of the codec which is trying to start
     */
    /*
        public CheckRtpStart(Object receiveClass, int label,
                             String codecName) {

            this.label = label;
            this.codecName = codecName;

            if (receiveClass instanceof TextReceive) {
                textRcv = (TextReceive)receiveClass;
                textTx = null;
                avRcv = null;
            }

            if (receiveClass instanceof TextTransmit) {
                textRcv = null;
                textTx = (TextTransmit)receiveClass;
                avRcv = null;
            }

            if (receiveClass instanceof AudioVideoReceive) {
                textRcv = null;
                textTx = null;
                avRcv = (AudioVideoReceive)receiveClass;
            }

        }
     */

    /**
     * Runs the thread
     *
     */
    /*
        public void run() {

            boolean continueWaiting;

            // If the class is a TextReceive
            if (textRcv != null) {

                continueWaiting = !textRcv.isStarted() && !textRcv.isDone();
                // Poll every second
                while (continueWaiting) {
                    try {
                        java.lang.Thread.sleep(1000);
                    } catch(InterruptedException ie) {
                        // Ignore the interruption
                    }


                    if (textRcv.isStarted()) {
                        gui.changeStatusLabel(label, codecName);
                        continueWaiting = false;
                    }

                    if (textRcv.isDone()) {
                        gui.changeStatusLabel(label, "Failed");
                        continueWaiting = false;
                    }
                }

            }

            // If the class is a TextTransmit
            if (textTx != null) {

     continueWaiting = !textTx.isStarted();
//continueWaiting = !textTx.isStarted() && !textTx.isDone();

                // Poll every second
                while (continueWaiting) {
                    try {
                        java.lang.Thread.sleep(1000);
                    } catch(InterruptedException ie) {
                        // Ignore the interruption
                    }


                    if (textTx.isStarted()) {
                        gui.changeStatusLabel(label, codecName);
                        continueWaiting = false;
                    }

     */
    /*if(textTx.isDone()) {
        guiChangeStatusLabel(label, "Failed");
        continueWaiting = false;
        }*/
    /*
              }
          }

          // If the class is a AudioVideoReceive
          if (avRcv != null) {

              continueWaiting = !avRcv.isStarted() && !avRcv.isDone();

              // Poll every second
              while (continueWaiting) {
                  try {
                      java.lang.Thread.sleep(1000);
                  } catch(InterruptedException ie) {
                      // Ignore the interruption
                  }


                  if (avRcv.isStarted()) {
                      gui.changeStatusLabel(label, codecName);
                      continueWaiting = false;
                  }

                  if (avRcv.isDone()) {
                      gui.changeStatusLabel(label, "Failed");
                      continueWaiting = false;
                  }
              }

          }

      }

         }
     */

    /**
     * Detects devices and updates the internal variables.
     *
     */
    public void detectDevices() {
        Class directAudio = null;
        Class autoAudio = null;
        Class autoVideo = null;
        Class autoVideoPlus = null;

        // Check if VFWAuto or SunVideoAuto is available
        try {
            directAudio = Class.forName("DirectSoundAuto");
        } catch (ClassNotFoundException e) {
        }

        try {
            autoAudio = Class.forName("JavaSoundAuto");
        } catch (ClassNotFoundException e) {
        }

        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            try {
                autoVideo = Class.forName("VFWAuto");
            } catch (ClassNotFoundException e) {
            }

            if (autoVideo == null) {
                try {
                    autoVideo = Class.forName("SunVideoAuto");
                } catch (ClassNotFoundException ee) {
                }
                try {
                    autoVideoPlus = Class.forName("SunVideoPlusAuto");
                } catch (ClassNotFoundException ee) {
                }
            }
            if (autoVideo == null) {
                try {
                    autoVideo = Class.forName("V4LAuto");
                } catch (ClassNotFoundException eee) {
                }
            }
        }

        if (directAudio == null && autoAudio == null &&
            autoVideo == null && autoVideoPlus == null) {
            return;
        }

        try {
            if (directAudio != null) {
                directAudio.newInstance();
            }
            if (autoAudio != null) {
                autoAudio.newInstance();
            }

            if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && autoVideo != null) {
                autoVideo.newInstance();
            }
            if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && autoVideoPlus != null) {
                autoVideoPlus.newInstance();
            }
        } catch (Throwable t) {
        	logger.throwing(this.getClass().getName(), "detectDevices", t);
        }
    }

    /**
     * Returns the main GUI.
     *
     * @return The main GUI.
     */
    protected ProgramWindow getProgramWindow() {
        return gui;
    }


    /**
     * Returns a Vector containing all supported media in DeviceContainer
     * objects.
     *
     * @return A Vector filled with DeviceContainer objects of all supported
     * media
     */
    public Collection getSupportedMedia() {
        return supportedMedia;
    }

    /**
     * Returns the local IP address.
     *
     * @return The local IP address, e.g. "192.168.0.13".
     */
    public String getLocalIpAddress() {
        return localIpAddress;
    }

    /**
     * Returns the local host name. If no host name is present, the local IP
     * address is returned.
     *
     * @return The host name, e.g. "myhost.domain.com" or "192.168.0.13"
     */
    public String getLocalHostAddress() {
        return localHostAddress;
    }

    /**
     * Destroys the old video player and starts a new one. If video is not
     * activated or if no video device is present, it only tries to destroy
     * the existing player.
     *
     */
    public void restartVideo() {

        destroyAudioVideoPlayer();
        if (videoIsActivated && videoLocator.length > 0) {
            createAudioVideoPlayer(videoLocator[videoDevice]);
        } else {
            gui.resetLocalVideoPanel();
        }

    }

    /*
     * Checks which medias that will be used, and informs GUI.
     *
     * @param allEvents All media events at once in a Vector.
     */
    /*
         public void allStartingMediaEvents(Vector allEvents) {
      int size = allEvents.size();

      SipMediaEvent sme;

      boolean useText = false;
      boolean useAudio = false;

      for (int cnt=0; cnt<size; cnt++) {
     sme = (SipMediaEvent)allEvents.elementAt(cnt);

     if (sme.getCodecs()[0].getParent().getName().toUpperCase().
       equals("AUDIO")) {

       useAudio = true;
     }

     else if (sme.getCodecs()[0].getParent().getName().toUpperCase().
       equals("TEXT")) {

       useText = true;
     }

      }

      gui.setMediaStatus(useAudio, useText);


         }
     */

    /**
     * Gets the T.140 panel
     *
     * @return The T.140 panel
     */
    public T140Panel getT140Panel() {
        return t140Panel;
    }

    /**
     * Gets the local text area
     *
     * @return The local text area
     */
    public T140TextArea getLocalTextArea() {
        return t140Panel.getLocalTextArea();
    }
    
    /**
     * Gets the local log text area where history is stored.
     * 
     * @return The log text area object
     */
    public T140LogArea getLocalLogArea() {
    	return t140Panel.getLogTextArea();
    }

    /**
     * Gets the remote text area
     *
     * @return The remote text area
     */
    public T140TextArea getRemoteTextArea() {
        return t140Panel.getRemoteTextArea();
    }

    public T140LogArea getLogTextArea() {
        if (appSettings.isRealtimePreviewEnabled()) {
            return t140Panel.getLogTextArea();
        } else {
            return null; //t140Panel.getLocalTextArea();
        }

    }

    /**
     * Gets the current language set
     *
     * @return The current language set
     */
    public Properties getLanguage() {
        return language;
    }

    /**
    * Sets the current language set
    *
    * @param aLanguage The new language
    */
    public void setLanguage(Properties aLanguage) {
    	language = aLanguage;
    }


    /**
     * Gets the call processor for the current call
     *
     * @return The call processor for the current call
     */
    public CallProcessor getCurrentCallProcessor() {
        return callProcessor;
    }


    /**
     * Hangs up the current call
     *
     */
    public void hangup() {
        if (callProcessor != null) {
            callProcessor.bye();
            callProcessor = null;
        }
    }

    /**
     * Sets whether STUN should be activated or not during this current runtime.
     *
     * @param isActivated True if STUN should be activated, false if not
     */
    public void setRuntimeStunActivated(boolean isActivated) {
        runtimeIsStunActivated = isActivated;
    }

    public boolean isRuntimeStunActivated() {
        return runtimeIsStunActivated;
    }

    /**
     * Converts SIP registrar info to AuthInfo
     *
     * @param ri The registrar info
     *
     * @return The AuthInfo, generated from ri
     */
    private AuthInfo[] convertToAuthInfo(SipRegistrarInfo[] ri) {
        if (ri == null) {
            return new AuthInfo[0];
        }

        AuthInfo[] ai = new AuthInfo[ri.length];

        for (int cnt = 0; cnt < ri.length; cnt++) {
            ai[cnt] = new AuthInfo(ri[cnt].getRegistrarHost(),
                                   ri[cnt].getUsername(),
                                   ri[cnt].getPassword());
        }

        return ai;
    }

    /**
     * Converts SIP registrar info to FromAddress
     *
     * @param ri The registrar info
     *
     * @return The FromAddress, generated from ri
     */
    private FromAddress[] convertToFromAddress(SipRegistrarInfo[] ri) {
        if (ri == null) {
            return new FromAddress[0];
        }

        FromAddress[] fa = new FromAddress[ri.length];

        for (int cnt = 0; cnt < ri.length; cnt++) {
            fa[cnt] = new FromAddress(ri[cnt].getRegistrarHost(),
                                      ri[cnt].getSipAddress());
        }

        return fa;
    }

    /**
     * Registers all names that are in sipRegistrarInfo variable.
     *
     */
    private void sipRegisterAll() {
        if (appSettings.getSipRegistrarInfo() == null) {
            return;
        }

        sipRegistrarStatus = new int[appSettings.getSipRegistrarInfo().length];
        registerProcessor = new RegisterProcessor[appSettings.
                            getSipRegistrarInfo().length];

        if (appSettings.getSipRegistrarInfo().length > 0) {
            gui.setRegPending();
        } else {
            gui.setRegNa();
        }

        for (int cnt = 0; cnt < appSettings.getSipRegistrarInfo().length; cnt++) {
            sipRegistrarStatus[cnt] = TRYING;
            registerProcessor[cnt] =
                    sc.register(appSettings.getSipRegistrarInfo()[cnt].
                                getSipAddress(),
                                appSettings.getSipRegistrarInfo()[cnt].
                                getRegistrarHost(),
                                appSettings.getSipRegistrarInfo()[cnt].
                                getUsername(),
                                appSettings.getSipRegistrarInfo()[cnt].
                                getPassword());
        }

        sc.setFullName(appSettings.getUserRealName());
        sc.setPrimarySipAddress(appSettings.getPrimarySipAddress());
    }

    /**
     * Unregisters from all registrars that are in the sipRegistrarInfo
     * variable.
     *
     * @return The number of (un)registrations sent
     */
    private int sipUnregisterAll() {
        if (registerProcessor == null) {
            return 0;
        }

        int unregs = 0;

        for (int cnt = 0; cnt < registerProcessor.length; cnt++) {
            if (registerProcessor[cnt] != null) {
                registerProcessor[cnt].unregister();
                registerProcessor[cnt] = null;
                sipRegistrarStatus[cnt] = NOT_REG;
                unregs++;
            }
        }

        if (sc != null) {
            sc.setPrimarySipAddress("unknown@" + localIpAddress);
            gui.setRegNa();
        }
        return unregs;

    }

    public StunStack getStunStack() {
        return stunStack;
    }


    /**
     * Gets the next network hop for packets. This will be the system's default
     * gateway or the outbound proxy in SIP settings depending on whether the
     * outbound proxy can be reached without passing default gw or not.
     *
     * @return IP address of next network hop
     */
    private String getNextNetworkHop() throws SocketException, UnknownHostException {
        try(DatagramSocket s=new DatagramSocket()) {
            s.connect(InetAddress.getByAddress(new byte[]{1,1,1,1}), 0);
            var adr=NetworkInterface.getByInetAddress(s.getLocalAddress()).getInetAddresses().nextElement();
            return adr.getHostAddress();
        }
    }


    private boolean detectSipCompatibleNat() throws SocketException, UnknownHostException {
        boolean foundSipCompatibleNat = false;

        // Get the default gw setting
        String dgw = getNextNetworkHop(); //untested

        // Send OPTIONS to default gw
        DatagramSocket socket = null;
        try {
            int port = getFreePort();
            socket = new DatagramSocket(port);
            String request =
              "OPTIONS sip:" + dgw + " SIP/2.0\r\n" +
              "Via: SIP/2.0/UDP " + localIpAddress + ":" + port + ";branch=z9hG4bKhjhs8ass877\r\n" +
              "Max-Forwards: 70\r\n" +
              "To: <sip:" + dgw + ">\r\n" +
              "From: <sip:user@" + localIpAddress + ":" + port + ">;tag=1928301774\r\n" +
              "Call-ID: a84b4c76e66710\r\n" +
              "CSeq: 1 OPTIONS\r\n" +
              "Contact: <sip:user@" + localIpAddress + ":" + port + ">\r\n" +
              "Accept: application/sdp\r\n" +
              "Content-Length: 0\r\n\r\n";

            byte[] requestBytes = request.getBytes();
            DatagramPacket outPacket = new DatagramPacket(requestBytes,
                                                          requestBytes.length,
                                                          InetAddress.getByName(dgw),
                                                          5060);
            DatagramPacket inPacket = new DatagramPacket(new byte[1314], 1314);
            socket.setSoTimeout(500);

            // Try to send five times
            for (int cnt = 0; cnt < 5; cnt++) {
                socket.send(outPacket);
                try {
                    socket.receive(inPacket);
                    cnt = 5;
                } catch (SocketTimeoutException ste) {
                    // Ignore and just try again
                }
            }

            // Check answer
            String response = new String(inPacket.getData(), 0, inPacket.getLength());
            if (response.startsWith("SIP")) {
                foundSipCompatibleNat = true;

                int ix1 = response.indexOf("\r");
                int ix2 = response.indexOf("\n");
                String[] lines;
                if (ix1!=-1 && ix2!=-1) {
                    if (ix1 > ix2) {
                        lines = response.split("\n\r");
                    }
                    else {
                        lines = response.split("\r\n");
                    }
                }
                else if (ix1 != -1) {
                    lines = response.split("\r");
                }
                else {
                    lines = response.split("\n");
                }

                sipCompatibleNatName = null;
                for (int cnt=0; cnt<lines.length; cnt++) {
                    if (lines[cnt].startsWith("Server:")) {
                        sipCompatibleNatName = lines[cnt].substring(7).trim();
                    }
                }

            }
        }
        catch (UnknownHostException uhe) {
            // Cannot arrange address for default gw, ignore and continue.
        }
        catch (SocketException se) {
            // Problem using datagram socket, ignore and continue.
        }
        catch (IOException ioe) {
            // Problem sending datagram packet, ignore and continue
        }

        if (socket != null) {
            socket.close();
        }

        return foundSipCompatibleNat;
    }

    public boolean isSipCompatibleNatDetected() {
        return isSipCompatibleNatDetected;
    }

    public String getSipCompatibleNatName() {
        return sipCompatibleNatName;
    }

    public void waitUntilDetectComplete() {
        if (sipStarterThread != null) {
            while (sipStarterThread.isAlive()) {
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException ie) {
                    // Ignore
                }
            }
        }
    }

    /*
    public boolean getTipcon1Mode() {
        return appSettings.getTipcon1Mode();
    }
*/
    
    public int skipOneRtpTextSeqNo() {
    	if (mediaManager != null) {
    		return mediaManager.dropOneRtpTextSeqNo();
    	}
    	
    	return 0;
    }
    
    public void sendHexCode(String hexCode) {
    	if (mediaManager != null) {
    		mediaManager.sendHexCode(hexCode);
    	}
    }
    
    class RingSignal implements Runnable {
    	public RingSignal() {
    	}
        public void run() {
            try {
                while (true) {

                    try {
                        AudioInputStream ais =
                                AudioSystem.getAudioInputStream
                                (new File(AppConstants.RING_SOUND_URL));
                        Line.Info info =
                                new Line.Info(Clip.class);
                        Clip clip =
                                (Clip) AudioSystem.getLine(info);
                        clip.open(ais);
                        clip.start();
                    } catch (UnsupportedAudioFileException e) {
                        logger.throwing(this.getClass().getName(),
                                "run", e);
                // If something fails,
                // just don't play the clip.
					} catch (LineUnavailableException e) {
                        logger.throwing(this.getClass().getName(),
                                "run", e);
                // If something fails,
                // just don't play the clip.
					} catch (IOException e) {
                        logger.throwing(this.getClass().getName(),
                                "run", e);
                // If something fails,
                // just don't play the clip.
					}

                    Thread.sleep(4000);
                }
            } catch (InterruptedException e) {
                // Jump out from the thread when interrupted.
            }
        }
    }

    private String getMatchingIpAddress(String macAddress){
    	String IpAddress = null;
    	String macValue = null;
    	boolean IsRealEthPresent = false;
    	
    	try{
    		Enumeration<NetworkInterface> ethIf = NetworkInterface.getNetworkInterfaces();
   		 	for (NetworkInterface netIf : Collections.list(ethIf)) {
   		 		Enumeration<InetAddress> inetAddres = netIf.getInetAddresses();
	            for (InetAddress inetAddress : Collections.list(inetAddres)) {
	            	if(!(inetAddress.isLoopbackAddress())){
	            		IsRealEthPresent = true;
	            	}
	            }
   	     }   
    		if(true == IsRealEthPresent){
	    		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
	    		for (NetworkInterface netIf : Collections.list(nets)) {
	    			String IpAddr = null;
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
		            				
		            				if(macValue.compareTo(macAddress) == 0){
		            					try{
		        		            		IpAddr = inetAddress.getHostAddress();
		        		               	}catch(Exception e){
		        	            			e.printStackTrace();
		        	            		}
		        	            		
		        	            		if(null == IpAddr || IpAddr.equals("0.0.0.0") || IpAddr.equals("")){
		        	            			continue;
		        	            		}
		        		            	IpAddr.trim();
		        		            	if(!ValidateIPAddress(IpAddr))
		        	            		{
		        	            			continue;
		        	            		}
		            					
		        	            		IpAddress = IpAddr ;
		        	            		IpAddress.trim();
		        	            		return IpAddress;
		            				}
		            			}catch(NumberFormatException ne){
		            				return null;
		            			}
		            		}
		            	}
		            }
	    		}
    		}
	    }catch(SocketException se){
	    	return null;
	    }
	    return IpAddress;
    }
    
    public boolean ValidateIPAddress( String  ipAddress )
    {
         String[] parts = ipAddress.split( "\\." );
         int count = 0;
         if ( parts.length != 4 ){
             return false;
         }

         for ( String s : parts ){
         	int i = 0;
         	try {
         		i = Integer.parseInt( s );
         	}catch(NumberFormatException e){
         		return false;
            }
         	
         	if(count > 0){	
	            if ( (i < 0) || (i > 255) ){
	                return false;
	            }
         	}else{
         		if ( (i < 1) || (i > 255) ){
                    return false;
                }
         	}
         	
             count++;
         }
         return true;
     }
    public void changeLayout(boolean realtimepv)
	{
		t140Panel.changeLayout(realtimepv) ;
    }
}