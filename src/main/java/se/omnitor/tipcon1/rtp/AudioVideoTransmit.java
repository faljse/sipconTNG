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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.rtp.*;
import javax.swing.*;
import javax.swing.event.*;

import se.omnitor.tipcon1.*;

/**
 * Transmits audio and video using RTP.
 *
 * @author Andreas Piirimets, Omnitor AB
 * @author Erik Zetterstrm, Omnitor AB
 */
public class AudioVideoTransmit implements ActionListener {

    // RTP functionality variables
    private Processor processor;
    //private Processor renderer;
    private DataSource dataOutput;

    //private Player player;
    private DataSource videoTransmitDataSource=null;
    //private DataSource playerds;
    private DataSource transmitds;
    //private DataSource cloneableds;
    //private RTPManager[] rtpMgrs;

    private Component controlPanel;

    // User settings
    private MediaLocator locator;
    //private String ipAddress;
    //private int localPort;
    //private int remotePort;
    private String codec;
    private int fps = 0;
    private int bitrate = 0;
    Vector videoControls;
    Control encoderControls [];
    Control control;

    // Processor's state change variables
    private Integer stateLock;
    private boolean processorChangeFailed;

    private String locatorStr;

    private Logger logger;

    private JPanel playerPanel;
    private JSlider slider;
    private JToggleButton mute;
    private boolean muteState = false;

    private GainEffect gainEffect;
    private float oldGain = 0;

    private MediaManager parent = null;
    private RTPManager manager;

    private boolean isStopped = false;



    /**
     * Initializes transmission
     *
     * @param videoTransmitDataSource If a data source already exists.
     *                                For example when captured video is
     *                                viewed and transmitted.
     * @param locator The media locator to get media from, e.g. file://film.mpg
     * or vfw://0
     * @param ipAddress IP address to remote host
     * @param localPort Port on local host
     * @param remotePort Port on remote host
     * @param codec Which codec to use, e.g. H263, G723 etc.
     */
    public AudioVideoTransmit(MediaManager parent,
			      RTPManager manager,
			      DataSource videoTransmitDataSource,
                              String locator,
                              String ipAddress,
                              int localPort,
                              int remotePort,
                              String codec,
			      JPanel panel,
                              int bitrate,
                              int fps) {

        this.fps = fps;
        this.bitrate = bitrate;
	this.parent  = parent;
	this.manager = manager;

	playerPanel = panel;

	logger = Logger.getLogger("se.omnitor.tipcon1.rtp");

        this.videoTransmitDataSource=videoTransmitDataSource;

        // Initialize the processor's state change variables
        stateLock = new Integer(0);
        processorChangeFailed = false;

        // Initialize the RTP functionality variables
        processor = null;
        dataOutput = null;
        //playerds=null;
        transmitds=null;
        //cloneableds=null;

	controlPanel = null;

        // Initialize the user setting
        if (panel != null) {
        	locator = "dsound://8000";
	}
        locatorStr=locator;
        this.locator = new MediaLocator(locator);
        //this.ipAddress = ipAddress;
        //this.localPort = localPort;
        //this.remotePort = remotePort;
        this.codec = codec;
        this.videoControls = new Vector ();

	// PCMU -> ULAW, as ULAW is the name that JMF uses.
	if (codec.toUpperCase(Locale.US).equals("PCMU")) {
	    this.codec = "ULAW";
	}

	// DVI4 -> DVI, as DVI is the name that JMF uses
	if (codec.toUpperCase().equals("DVI4")) {
	    this.codec = "DVI";
	}

	// MPA -> MPEGAUDIO as MPEGAUDIO is the name that JMF
	// uses
	if (codec.toUpperCase().equals("MPA")) {
	    this.codec = "MPEGAUDIO";
	}

	System.out.println("codec: " + codec + " " + locator);

	gainEffect = new GainEffect();
    }

    /**
     * Returns the codec which is used.
     *
     * @return The codec
     */

    public String getCodec() {
        return codec;
    }


    /**
     * Retruns the locator used.
     *
     * @return The locator
     */

    public String getLocator() {
        return locatorStr;
    }


    /**
     * Start transmission.
     *
     * @return null if start was ok, otherwise an error message
     */
    public synchronized String start() {
        String returnStr;
        Component guicomponent;
        Component panelcomponent;
        Component subcomponent;
        Panel basepanel;
        Panel nodepanel;
        returnStr = null;
        boolean next = false;

        if (videoTransmitDataSource!=null) {
            transmitds=videoTransmitDataSource;
        } else {
            // Create datasource
            try {
                transmitds = javax.media.Manager.createDataSource(locator);
            } catch (NoDataSourceException e) {
            	logger.throwing(this.getClass().getName(), "start", e);
                return "Couldn't create DataSouce";
			} catch (IOException e) {
            	logger.throwing(this.getClass().getName(), "start", e);
                return "Couldn't create DataSouce";
			}
        }

        // Create processor
        returnStr = createProcessor();
        if (returnStr != null) {
            return returnStr;
        }

	if (codec.toUpperCase(Locale.US).equals("H263")) {

            encoderControls = processor.getControls();
            for ( int i = 0;  i < encoderControls.length ;  i++ ) {
                control = encoderControls[i];
                guicomponent = control.getControlComponent();
                if (guicomponent == null) {
                    System.out.println("GuiComponent " + i + " is null");
                } else {
                    //System.out.println("GuiComponent " + i + " is " + guicomponent.toString());
                    if (guicomponent instanceof java.awt.Panel) {
                        basepanel = (Panel)guicomponent;
                        System.out.println("GuiComponent: " + basepanel.getName() +" nr " + i + " has " + basepanel.getComponentCount() + " components.");
                        for ( int j = 0;  j < basepanel.getComponentCount() ;  j++ ) {
                            panelcomponent = basepanel.getComponent(j);
                            if (panelcomponent instanceof Panel) {
                                nodepanel = (Panel) panelcomponent;
                                System.out.println("  " + j + " " +
                                        panelcomponent.getName());
                                    for (int k = 0;
                                                 k < nodepanel.getComponentCount();
                                                 k++) {
                                        subcomponent = nodepanel.getComponent(k);
                                        if (next) {
                                            next = false;
                                            Checkbox subbox;
                                            com.sun.media.ui.SliderComp slider;
                                            if (subcomponent instanceof Checkbox) {
                                                subbox = (Checkbox)subcomponent;
                                                subbox.setState(true);
                                                ItemListener items[] = subbox.getItemListeners();
                                                System.out.println("    " + items.length + " itemlisteners");
                                                items[0].itemStateChanged(new ItemEvent(subbox,ItemEvent.ITEM_STATE_CHANGED,subbox,ItemEvent.SELECTED));

                                                //postEvent(context, new ItemEvent(cbox, ItemEvent.ITEM_STATE_CHANGED, cbox, cbox.getState()
                                                //? ItemEvent.SELECTED
                                                //: ItemEvent.DESELECTED));
                                            }

                                            if (subcomponent instanceof com.sun.media.ui.SliderComp) {
                                                slider = (com.sun.media.ui.SliderComp)subcomponent;
                                                System.out.println("    Slidervalue = " + slider.getFloatValue());
                                                //slider.setValue(30.0f);
                                                slider.actionPerformed(new ActionEvent(slider,ActionEvent.ACTION_PERFORMED,""));

                                                slider.setValue(20.0f);
                                                System.out.println("    Slidervalue = " + slider.getFloatValue());
                                                //slider.
                                                slider.actionPerformed(new ActionEvent(slider,ActionEvent.ACTION_PERFORMED,"panel5"));
                                                System.out.println("    Slidervalue = " + slider.getFloatValue());
                                            }
                                        }
                                        if (subcomponent instanceof Label) {
                                            Label nodelabel = (Label)subcomponent;
                                            System.out.println("    " + nodelabel.getText());
                                            if (nodelabel.getText().toString().equals("Low Std Compliance"))
                                                next = true;
                                            if (nodelabel.getText().toString().equals("Min Quality"))
                                                next = true;


                                        } else {
                                            try {
                                                System.out.println("    " +
                                                        subcomponent.toString());
                                            } catch (Exception e) {
                                                System.out.println("Exception");
                                            }
                                        }
                                    }
                            }

                        }
                    }
                }
                //encoderControls.get
            }
            System.out.println(encoderControls.length);
            //videoControls

            /*java.lang.Object[] objects = */ processor.getControls(); // don't know if getControls() has to be run??

            //javax.media.control.CompatibilityControl compctrl =
            //(javax.media.control.CompatibilityControl)processor.getControl("javax.media.control.CompatibilityControl");
            javax.media.control.BitRateControl ctrl =
            (javax.media.control.BitRateControl)processor.getControl("javax.media.control.BitRateControl");
            if (ctrl != null) {
                    //DEBUG
                    System.out.println("bitrate: " + ctrl.getBitRate());
                    System.out.println("max: " + ctrl.getMaxSupportedBitRate());
                    System.out.println("min: " + ctrl.getMinSupportedBitRate());
                    System.out.println("applied bitrate: " + ctrl.setBitRate(bitrate));
                } else {
                        //DEBUG
                        System.out.println("ctrl = null");
                    }


                    javax.media.control.FrameRateControl frmctrl =
            (javax.media.control.FrameRateControl)processor.getControl("javax.media.control.FrameRateControl");
            if (frmctrl != null) {
                System.out.println("Framerate: " + frmctrl.getFrameRate());
                System.out.println("Setting rate to " + fps);
                frmctrl.setFrameRate(fps);
                System.out.println("Framerate: " + frmctrl.getFrameRate());
            } else {
                System.out.println("frmctrl == null");
            }


            javax.media.control.QualityControl qctrl =
            (javax.media.control.QualityControl)processor.getControl("javax.media.control.QualityControl");
            System.out.println("QualityControl: " + qctrl.getQuality());
            System.out.println("QualityControl: " + qctrl.setQuality(1));
            System.out.println("QualityControl: " + qctrl.getQuality());

            System.out.println("Codec: " + codec);
	}

	// Create RTP session
	returnStr = createTransmitter();

        if (returnStr != null) {
            processor.close();
            processor = null;
            return returnStr;
        }

	// Get control panel component
	//controlPanel = processor.getControlPanelComponent();
	if(playerPanel != null) {
	    playerPanel.removeAll();
	    playerPanel.setLayout(new GridBagLayout());

	    //playerPanel.add(processor.getControlPanelComponent(),gbc);

	    slider = new JSlider(JSlider.HORIZONTAL,0,40,20);
	    slider.setMajorTickSpacing(40);
	    slider.setMinorTickSpacing(1);
	    slider.setPaintTicks(false);

	    Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
	    labelTable.put( new Integer( 0 ), new JLabel("-") );
	    labelTable.put( new Integer( 40 ), new JLabel("+") );
	    slider.setLabelTable( labelTable );
	    slider.setPaintLabels(true);

	    slider.addChangeListener(new SliderListener());

	    int height = (int)slider.getMinimumSize().getHeight();
	    slider.setMinimumSize(new Dimension(100, height));
	    slider.setPreferredSize(new Dimension(100, height));

	    slider.getAccessibleContext().setAccessibleDescription("Microphone volume control, left arrow to decrease and right arrow to increase.");

	    mute = new JToggleButton("Mute",false);

	    mute.addActionListener(this);
	    mute.setMargin(new Insets(1,10,1,10));
	    height = (int)mute.getMinimumSize().getHeight();
	    mute.setMinimumSize(new Dimension(60, height));
	    mute.setPreferredSize(new Dimension(60, height));

	    mute.getAccessibleContext().setAccessibleDescription("Mute microphone.");

	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.insets = new Insets(2,2,2,2);
	    gbc.weightx = 1.0;
	    gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
	    playerPanel.add(slider,gbc);

	    gbc = new GridBagConstraints();
	    gbc.gridx = 1;
	    gbc.gridy = 0;
	    gbc.insets = new Insets(2,2,2,2);
	    gbc.fill = java.awt.GridBagConstraints.NONE;
	    playerPanel.add(mute,gbc);

	    playerPanel.repaint();
	    //playerPanel.add(processor.getControlPanelComponent(),gbc);
	}


        // Start transmission
        if (!isStopped) {
            processor.start();
        }

        return returnStr;
    }


    /**
     * Stop transmission.
     *
     */
    public void stop() {
        isStopped = true;

        synchronized(this) {

            if (processor != null) {
                processor.stop();
                processor.close();
                processor = null;
            }

	    if(manager!=null) {
		manager.removeTargets("Closin session");
	    }
            /*if (rtpMgrs != null) {
                for (cnt=0; cnt<rtpMgrs.length; cnt++) {
                    rtpMgrs[cnt].removeTargets("Closing session");
                    rtpMgrs[cnt].dispose();
                }
		}*/

	    controlPanel = null;
        }
    }


    /**
     * Create processor
     *
     * @return null if ok, otherwise an error message
     */
    private String createProcessor() {

        int cnt;
        int scnt;
        Format chosen;
        boolean aTrackIsSet;
        Format[] supported;
        TrackControl[] tracks;


        // Create processor
        try {
            processor = javax.media.Manager.createProcessor(transmitds);
        } catch(NoProcessorException npe) {
            return "Couldn't create processor";

        } catch(IOException ioe) {
            return "IOException creating processor";
        }

        // Wait for the processor to configure
        if (!waitForState(processor, Processor.Configured)) {
            return "Couldn't configure processor";
        }

        // Get the tracks
        tracks = processor.getTrackControls();
        if  (tracks == null || tracks.length < 1) {
            return "Couldn't find tracks in processor";
        }


        // Get the correct codec
        processor.setContentDescriptor
            (new ContentDescriptor(ContentDescriptor.RAW_RTP));

        // Loop through all tracks
        chosen = null;
        aTrackIsSet = false;
        for (cnt=0; cnt<tracks.length; cnt++) {
            if (tracks[cnt].isEnabled()) {

                // Loop through all supported formats in the track
                supported = tracks[cnt].getSupportedFormats();

                for (scnt=0; scnt<supported.length; scnt++) {


                    if (supported[scnt].isSameEncoding(codec + "/rtp")) {

                        // If it's a video codec, check the sizes
                        if (supported[scnt] instanceof VideoFormat) {
                            chosen =
                                checkForVideoSizes(tracks[cnt].getFormat(),
                                                   supported[scnt]);
                        }
                        else {
                            chosen = supported[scnt];
                        }
                        break;
                    }
                }
            if (codec.toUpperCase(Locale.US).equals("ULAW")) {
                if (tracks[cnt].getFormat() instanceof AudioFormat) {
                    Codec codec[] = new Codec[4];
                    codec[0] = gainEffect;
                    codec[1] = new com.ibm.media.codec.audio.rc.RCModule();
                    codec[2] = new com.ibm.media.codec.audio.ulaw.JavaEncoder();
                    codec[3] = new com.sun.media.codec.audio.ulaw.Packetizer ();
                    ((com.sun.media.codec.audio.ulaw.Packetizer)codec[3]).setPacketSize(160);
                    try {
                        tracks[cnt].setCodecChain(codec);
                    }
                    catch (UnsupportedPlugInException upie) {
                        logger.throwing(this.getClass().getName(),"createProcessor", upie);
                    }
                }
            }
                if (chosen != null) {
                    tracks[cnt].setFormat(chosen);
                    aTrackIsSet = true;

/*
		    // For G.723, use the SeqNoFixer in order to prevent
		    // JMF dropping sequence numbers.
		    if (codec.toUpperCase().equals("G723")) {
			try {
			    tracks[cnt].setCodecChain(new Codec[]
				{
				    //COMMENT THE 2 ROWS BELOW FOR
				    //FIX OF PACKET LOSS PROBLEM.
				  gainEffect,
				  new com.ibm.media.codec.audio.rc.RCModule(),
				  new SeqNoFixer() });
			    //{ new UlawSyncRemovalEncoder() });
			}
			catch (UnsupportedPlugInException upie) {
			    logger.throwing(this.getClass().getName(),
					    "createProcessor",
					    upie);
			}
		    }

		    else {
		    try {
			    tracks[cnt].setCodecChain(new Codec[]
				{ gainEffect });
			    //{ new UlawSyncRemovalEncoder() });
			}
			catch (UnsupportedPlugInException upie) {
			    logger.throwing(this.getClass().getName(),
					    "createProcessor",
					    upie);
			}

		    }
*/
                    // Borde det inte vara chosen=null precis hr ox?

                }
                else {
                    tracks[cnt].setEnabled(false);
                }
            }
        }
        if (!aTrackIsSet) {
            return "Didn't find the given codec in system";
        }

        // Wait until the processor is realized
        if (!waitForState(processor, Controller.Realized)) {
            return "Couldn't realize processor";
        }

        // Set the JPEG quality to 0.5
        setJpegQuality(processor, 0.1f);

        // Get the data output source
        dataOutput = processor.getDataOutput();

        return null;
    }


    /**
     * Creates a session for each track
     */
    private String createTransmitter() {

        SendStream sendStream;
	System.out.println("***************************Creating send stream.");
	try {
	    sendStream = manager.createSendStream(dataOutput, 0);
	    sendStream.start();
	} catch (Exception e) {
	    return e.getMessage();
	}

	/*
        pbds = (PushBufferDataSource)dataOutput;
        pbss = pbds.getStreams();

        // Create managers
        rtpMgrs = new RTPManager[pbss.length];

        // Loop through all streams
        for (cnt=0; cnt<pbss.length; cnt++) {

            try {
                rtpMgrs[cnt] = RTPManager.newInstance();

                // Local port will be same as remote port
                currentPort = remotePort + 2*cnt;
                ipAddr = InetAddress.getByName(ipAddress);

                localAddr =
                    new SessionAddress(InetAddress.getLocalHost(),
                                       currentPort+2); //FIX THIS

                destAddr = new SessionAddress(ipAddr, currentPort);
                rtpMgrs[cnt].initialize(localAddr);
                rtpMgrs[cnt].addTarget(destAddr);

                sendStream = rtpMgrs[cnt].createSendStream(dataOutput, cnt);

                sendStream.start();
            } catch(Exception e) {
                return e.getMessage();
            }

	    }*/

        return null;
    }


    /**
     * Check the video size. JPEG and H263 only work with particular sizes
     *
     * @param original The current format with size information
     * @param newFmt The new format to set to correct size
     */
    private Format checkForVideoSizes(Format original, Format newFmt) {
        int width;
        int height;
        Dimension size;

        // Get the size
        size = ((VideoFormat)original).getSize();

        // Check for JPEG
        if (newFmt.matches(new Format(VideoFormat.JPEG_RTP))) {

            // The width and height has to be divisible by 8
            if (size.width % 8 == 0) {
                width = size.width;
            }
            else {
                width = (int)(size.width / 8) * 8;
            }

            if (size.width % 8 == 0) {
                height = size.height;
            }
            else {
                height = (int)(size.height / 8) * 8;
            }
        }

        // Check for H263
        else if (newFmt.matches(new Format(VideoFormat.H263_RTP))) {

            // There are some specific sizes for H263 which JMF support
            if (size.width <= 128) {
                width = 128;
                height = 96;
            }
            else if (size.width <= 176) {
                width = 176;
                height = 144;
            }
            else {
                width = 352;
                height = 288;
            }
        }

        // Ignore all other formats
        else {
            return newFmt;
        }

        // Return the corrected sizes
        return (new VideoFormat(null, new Dimension(width, height),
                                Format.NOT_SPECIFIED, null,
                                Format.NOT_SPECIFIED)).intersects(newFmt);
    }


    /**
     * Sets the encoding quality.
     *
     * @param player The player which handles the format
     * @param quality The encoding quality, e.g. 0.5
     */
    void setJpegQuality(Player player, float quality) {
        Control[] cs;
        QualityControl qc;
        VideoFormat jpegFmt;
        int cnt;
        int fcnt;
        Object owner;
        Format[] fmts;

        cs = player.getControls();
        qc = null;
        jpegFmt = new VideoFormat(VideoFormat.JPEG);

        // Search for the JPEg quality control
        for (cnt=0; cnt<cs.length; cnt++) {

            if (cs[cnt] instanceof QualityControl &&
                cs[cnt] instanceof Owned) {

                owner = ((Owned)cs[cnt]).getOwner();

                // If owner is a codec, then we can check the output format
                if (owner instanceof Codec) {
                    fmts = ((Codec)owner).getSupportedOutputFormats(null);

                    for (fcnt=0; fcnt<fmts.length; fcnt++) {

                        if (fmts[fcnt].matches(jpegFmt)) {
                            qc = (QualityControl)cs[cnt];
                            qc.setQuality(quality);
                            break;
                        }
                    }
                }

                // Exit if we've found the quality control
                if (qc != null) {
                    break;
                }
            }
        }

    }


    /**
     * Wait for the processor to reach the given state
     *
     * @param processor The processor
     * @param state The state to be reached
     */
    private synchronized boolean waitForState(Processor processor, int state) {

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
        int processorState = processor.getState();
        while ( processorState < state && !processorChangeFailed) {
            synchronized(getStateLock()) {
                try {
                    getStateLock().wait();
                } catch(InterruptedException ie) {
                    return false;
                }
            }
            processorState = processor.getState();
        }

        return !processorChangeFailed;
    }

    /**
     * Returns the state lock. This thing is used as a semaphore.
     *
     * @return The state lock
     */
    private Integer getStateLock() {
        return stateLock;
    }

    /**
     * Set failed flag for the processors state change
     *
     */
    public void setFailed() {
        processorChangeFailed = true;
    }



    /**
     * Listener class for processor's state changes.
     *
     */
    private class StateListener implements ControllerListener {


        /**
         * Notifies waiting threads and handles errors.
         *
         * @param ce The ControllerEvent
         */

        public void controllerUpdate(ControllerEvent ce) {

            // If an error occured, signal the error
            if (ce instanceof ControllerClosedEvent) {
                setFailed();
            }

            // Notify waiting thread
            synchronized(getStateLock()) {
                getStateLock().notifyAll();
            }
        }
    }

    /**
     * A sync removal codec that adds Buffer.FLAG_NO_SYNC to each buffer. This
     * is used to make JMF send every sequence number.
     *
     * @author Andreas Piirimets, Omnitor AB
     */
/*
    private class SeqNoFixer implements Effect {

	public Format[] getSupportedInputFormats() {
	    return new Format[] {
*/ 
	    /*new AudioFormat(
	        AudioFormat.LINEAR,
                Format.NOT_SPECIFIED,
                16,
                Format.NOT_SPECIFIED,
                AudioFormat.BIG_ENDIAN,
                AudioFormat.SIGNED,
                16,
                Format.NOT_SPECIFIED,
                Format.byteArray
		) };*/
/*
	    new AudioFormat(AudioFormat.ULAW),//,
	    new AudioFormat(AudioFormat.G723) };//,
	                        //new AudioFormat(AudioFormat.ALAW) };
	}

	public Format[] getSupportedOutputFormats(Format format) {
	    if (format == null) {
	    return new Format[] {
*/ 
	    /*new AudioFormat(
	        AudioFormat.LINEAR,
                Format.NOT_SPECIFIED,
                16,
                Format.NOT_SPECIFIED,
                AudioFormat.BIG_ENDIAN,
                AudioFormat.SIGNED,
                16,
                Format.NOT_SPECIFIED,
                Format.byteArray
		) };*/
/*
		new AudioFormat(AudioFormat.ULAW),
		new AudioFormat(AudioFormat.G723) };//,
				      //new AudioFormat(AudioFormat.ALAW) };
	    }
	    if (format.getEncoding().equals(AudioFormat.ULAW)) {
		return new Format[] { new AudioFormat(AudioFormat.ULAW) };
	    }
	    if (format.getEncoding().equals(AudioFormat.G723)) {
	    	return new Format[] { new AudioFormat(AudioFormat.G723) };
	    }
		//if (format.getEncoding().equals(AudioFormat.LINEAR)) {

		//return new Format[] { new AudioFormat(AudioFormat.LINEAR) };
		//}
	    //if (format.getEncoding().equals(AudioFormat.ALAW)) {
	    //return new Format[] { new AudioFormat(AudioFormat.ALAW) };
	    //}

	    return new Format[0];
	}

	public int process(Buffer input, Buffer output) {

	    output.copy(input);
*/
	    /*
	    output.setFlags(input.getFlags() | Buffer.FLAG_NO_SYNC);
	    */
/*
	    return BUFFER_PROCESSED_OK;
	}

	public Format setInputFormat(Format format) {
	    return format;
	}

	public Format setOutputFormat(Format format) {
	    return format;
	}

	public void close() {
	}

	public String getName() {
	    return "Sync removal codec";
	}

	public void open() {
	}

	public void reset() {
	}

	public Object getControl(String controlType) {
	    return null;
	}

	public Object[] getControls() {
	    return new Object[0];
	}


    }
    */

    class UlawSyncRemovalEncoder
	extends com.ibm.media.codec.audio.ulaw.JavaEncoder {

	public UlawSyncRemovalEncoder() {
	    super();
	}

	public int process(Buffer in, Buffer out) {
	    int res = super.process(in, out);
	    out.setFlags(out.getFlags() | Buffer.FLAG_NO_SYNC);

	    return res;
	}

    }

    public Component getControlPanel() {
	return controlPanel;
    }

    /**
     * Allows the microphone to be muted from other controls.
     *
     * @param state true is muted.
     */
    public void setMute(boolean state) {
	//Currently muted  and unmute requested
	if(muteState && !state) {
	    mute.doClick();
	}
	//Currently unmuted and mute requested,
	else if(!muteState && state) {
	    mute.doClick();
	}
    }

   /**
     * Called when the mute button is pressed.
     * Mute and unmutes the sound.
     *
     * @param ae The action event generated.
     */
    public void actionPerformed(ActionEvent ae) {
	Object source = ae.getSource();

	if (source == mute) {
	    muteState = !muteState;
	    if (muteState) {
		mute.getAccessibleContext().setAccessibleDescription("Unmute microphone.");
		slider.getAccessibleContext().setAccessibleDescription("Microphone volume control, disabled");
		oldGain= slider.getValue()-10;
		gainEffect.setGain(0);
	    }
	    else {
		mute.getAccessibleContext().setAccessibleDescription("Mute microphone.");
		slider.getAccessibleContext().setAccessibleDescription("Microphone volume control, left arrow to decrease and right arrow to increase.");
		float gainInput = (float)(1+(oldGain/10));
		gainEffect.setGain(gainInput);
		parent.muteSpeakers(muteState);
	    }
	    slider.setEnabled(!muteState);
	}
    }

    /**
     * This listener handles changes on the slider.
     *
     * @author Erik Zetterstrm
     */
   class SliderListener implements ChangeListener {

       public void stateChanged(ChangeEvent e) {
	    JSlider source = (JSlider)e.getSource();
	    if (!source.getValueIsAdjusting()) {
		float volume = (float)source.getValue()-10;
		float gainInput = (float)(1+(volume/10));
		gainEffect.setGain(gainInput);
	    }
	}
    }

}
