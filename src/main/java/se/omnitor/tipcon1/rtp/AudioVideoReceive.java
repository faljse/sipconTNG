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

import java.util.logging.Logger;
import se.omnitor.tipcon1.gui.RatioPanel;
import se.omnitor.tipcon1.MediaManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.GainControl;
import javax.media.NoPlayerException;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.protocol.DataSource;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.SessionEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Recieves audio or video by using JMF.
 *
 * @author Andreas Piirimets, Omnitor AB
 * @author Erik Zetterstrm, Omnitor AB
 */
public class AudioVideoReceive
    implements ReceiveStreamListener,
	       SessionListener,
	       ControllerListener,
	       ActionListener {

    /**
     * Defines video media type in the constuctor.
     */
    public static final int VIDEO = 1;

    /**
     * Defines audio media type in the constructor.
     */
    public static final int AUDIO = 2;

    // Session information
    String remoteIp;
    int localPort;

    // "Semaphore"
    Object dataSync;

    // State variables
    boolean dataReceived;

    // RTP and data handling
    RTPManager manager;
    int mediaType;


    // GUI stuff
    JPanel playerPanel;
    Frame playerWindow;
    Vector audioGUIVector = null;
    Vector videoGUIVector = null;
    Component[] componentList;
    Color oldBackground;

    private JSlider slider;
    private JToggleButton mute;
    private boolean muteState;

    private Logger logger;

    private GainControl gain;

    private MediaManager parent = null;


    /**
     * Initializes the class
     *
     * @param ipAddress The IP address to the sending host
     * @param port The local port to use
     * @param p The panel for displaying received video and audiocontrols.
     * @param mediaType Either AUDIO or VIDEO.
     */
    public AudioVideoReceive(MediaManager parent,
			     RTPManager manager,
			     String ipAddress,
			     int port,
			     JPanel p,
			     int mediaType) {

	logger = Logger.getLogger("se.omnitor.tipcon1.rtp");

	this.parent  = parent;
	this.manager = manager;


	if (mediaType != VIDEO && mediaType != AUDIO) {
	    this.mediaType = AUDIO;
	}
	else {
	    this.mediaType = mediaType;
	}

        playerPanel = p;

        // Initialize session information
        remoteIp = ipAddress;
        localPort = port;

        // Initialize "semaphore"
        dataSync = new Object();

        // Initialize state variables
        dataReceived = false;

        // Initialize RTP and data handling
        //manager = null;
        audioGUIVector = new Vector(1,1);
        videoGUIVector = new Vector(1,1);

	logger.finest("AudioVideoReceive started on port: "+port);

    }

    /**
     * Starts the receiver.
     *
     * @return True if start OK, false if failed.
     */
    public boolean start() {
        if (!initialize()) {
            stop(true);
	    return false;
        }
	if (mediaType == AUDIO)
		getCustomAudioControl();

	return true;
    }

    /**
     * Initializes the receiver
     *
     * @return True if initialization ok, false if not
     */
    private boolean initialize() {
        long timeStamp;

/*	if (playerPanel != null) {
	    playerPanel.removeAll();
	    playerPanel.setLayout(new java.awt.GridBagLayout());
	    java.awt.GridBagConstraints constraints =
		new java.awt.GridBagConstraints();
	    constraints.fill = java.awt.GridBagConstraints.NONE;
	    constraints.anchor = java.awt.GridBagConstraints.CENTER;
	}*/

        // Fetch and check local and remote address for errors
        /*try {
            localAddr =
                new SessionAddress(InetAddress.getLocalHost(), localPort);
            destAddr =
                new SessionAddress(InetAddress.getByName(remoteIp), localPort);
            //FIX THIS

        } catch(UnknownHostException uhe) {
	    logger.throwing(this.getClass().getName(), "initialize", uhe);
            return false;
        }*/

        // Create manager
        //manager = (RTPManager)RTPManager.newInstance();
        manager.addSessionListener(this);
        manager.addReceiveStreamListener(this);

        /*try {
            manager.initialize(localAddr);

        } catch(InvalidSessionAddressException isae) {
	    logger.throwing(this.getClass().getName(), "initialize", isae);
            return false;

        } catch(IOException ioe) {
	    logger.throwing(this.getClass().getName(), "initialize", ioe);
            return false;

        }*/
	/*
        bc = (BufferControl)manager.getControl
            ("javax.media.control.BuffercControl");
        if (bc != null) {
            //bc.setBufferLength(350);
	    // Temporary testing a larger buffer /AP
            bc.setBufferLength(3000);
        }
        */

        /*try {
            manager.addTarget(destAddr);

        } catch(InvalidSessionAddressException isae) {
	    logger.throwing(this.getClass().getName(), "initialize", isae);
            return false;

        } catch(IOException ioe) {
	    logger.throwing(this.getClass().getName(), "initialize", ioe);
            return false;

	    }*/

        // Wait for incoming data
        timeStamp = System.currentTimeMillis();
        try {
            synchronized(dataSync) {
		long deltaTime;
                deltaTime = System.currentTimeMillis() - timeStamp;
                while (!dataReceived &&
                       deltaTime<30000) {

                    dataSync.wait(1000);
		    deltaTime = System.currentTimeMillis() - timeStamp;

                }
            }
        } catch(InterruptedException ie) {
            /*
             * If interrupted, ignore exception and continue
             */
        }
	logger.finest("AudioVideoReceive initialized on port: " + localPort);


        // If no data has arrived after 30 sec, close
        if (!dataReceived) {
            return false;
        }

	logger.finest("AudioVideoReceive returns on port: " + localPort);

	return true;

    }

    /**
     * Checks if the session has ended
     *
     * @return True if session has ended, false if it is still up
     */
    public boolean isDone() {
        return playerPanel == null;
    }

    /**
     * Returns the local port
     *
     * @return The local port
     */
    public int getPort() {
        return localPort;
    }

    /**
     * Close all
     *
     * @param noDispose Set to true to avoid disposing the RTPManager
     */
    public void stop(boolean noDispose) {
        int cnt;

        // Remove all element from the GUI panel, add the stored elements

/*        if (playerPanel != null) {
            playerPanel.removeAll();

            playerPanel.setBackground(oldBackground);

            if (componentList != null) {
                for (cnt=0; cnt<componentList.length; cnt++) {
                    playerPanel.add(componentList[cnt]);
                }
            }

            playerPanel.getParent().repaint();
        }


        // Remove connections to objects
        playerPanel = null;
        componentList = null;*/

        // Close the RTP session
        if (manager!=null) {
            manager.removeTargets("Closing session");
            if (!noDispose) {
                manager.dispose();
            }
            manager = null;
        }
    }

    /**
     * Does nothing. This is just a part of the SessionListener interface.
     *
     * @param evt An incoming session event
     */
    public synchronized void update(SessionEvent evt) {
        // Do nothing
    }

    /**
     * Handles new incoming streams, this is just a part of the
     * ReceiveListener interface.
     *
     * @param evt The incoming receive stream event
     */
    public synchronized void update(ReceiveStreamEvent evt) {
        DataSource ds;
        ReceiveStream stream;
        Processor processor;

        // If a new stream is incoming
        if (evt instanceof NewReceiveStreamEvent) {

            // Store stream info
            stream = ((NewReceiveStreamEvent)evt).getReceiveStream();

	    // If the stream is H.263, use an own datasource that throws
	    // all mode B and mode C packets. JMF doesn't seem to understand
	    // other packets than mode A.
            ds = stream.getDataSource();

            // Catch and realize player, just exit if something goes wrong.
            try {
                processor = javax.media.Manager.createProcessor(ds);
            } catch(IOException e) {
            	logger.throwing(this.getClass().getName(), "update", e);
                return;
            } catch(NoPlayerException e) {
            	logger.throwing(this.getClass().getName(), "update", e);
                return;
            }
            if (processor == null) {
                return;
            }

            //dataReceived = true;
            processor.addControllerListener(this);

	    processor.configure();

        }

    }

    /**
     * Controller listener for the player. This is a part of the
     * ControllerListener interface.
     *
     * @param ce The incoming controller event
     */
    public synchronized void controllerUpdate(ControllerEvent ce) {
        Processor processor;
        int width;
        int vcWidth;
        int vcHeight;


        // Fetch and check player
        processor = (Processor)ce.getSourceController();

        if (processor == null) {
            return;
        }

	// The processor is configured. Realize it!
	if (ce instanceof ConfigureCompleteEvent) {

	    // Use the processor as a player
	    processor.setContentDescriptor(null);

	    // Put the H.263 mode A effect into the video codec chain
	    /*
	    if (mediaType == VIDEO) {
		try {
		    TrackControl tc = processor.getTrackControls()[0];
		    tc.setCodecChain(new Codec[] { new H263ModeAEffect() });
		}
		catch (Exception e) {
		    // No track controls, cannot set effect. Continue anyway.
		}
	    }
	    */

           // Realize
           processor.realize();

           // Wake up threads
           synchronized(dataSync) {
               dataReceived = true;
               dataSync.notifyAll();
           }

        }

        // The player is realized, setup GUI and start all stuff!
        if (ce instanceof RealizeCompleteEvent) {

            Component vc = processor.getVisualComponent();
            Component cc = processor.getControlPanelComponent();

	    if (mediaType == AUDIO) { //Style == 1
	    	System.out.println("Start audio player");
		gain = processor.getGainControl();
		float gl = 5;
		gain.setLevel(gl);
		muteState=gain.getMute();
                if (mute != null) {
                    mute.setEnabled(true);
                    mute.setSelected(muteState);
                }
		if(!muteState) {
			if(null != slider){
		    slider.setEnabled(true);
			}
		}
		//getCustomAudioControl(processor);
	    }
	    else if (mediaType == VIDEO) { //Style == 0
                System.out.println("Start video player");


                /*
                //---------------------------------------------------------
                Frame f = new Frame("control");
                f.add(processor.getControlPanelComponent());
                f.setSize(150,100);
                f.setVisible(true);
                //---------------------------------------------------------
                */


                // Empty the panel and remember the contence
                if (playerPanel != null) {
                    componentList = playerPanel.getComponents();
                    oldBackground = playerPanel.getBackground();
                    playerPanel.removeAll();

                    //Audio
                    //if (vc==null && cc!=null) {
                    //    playerPanel.add(cc);
                    //}

                    //Video
                    if (vc!=null && cc!=null) {

                    /*
                    // Calculate the correct width and height for video. The
                    // video has to suite inside the playerPanel. Try to set
                    // video height to playerPanel's height and increase the width
                    // with the same factor. If it's too wide, set the width to
                    // playerPanel's width and increase the height with the same
                    // factor instead.
                    width = (int)((float)vc.getPreferredSize().getWidth() *
                    (float)(playerPanel.getHeight() /
                    vc.getPreferredSize().getHeight()));

                    if (width > playerPanel.getWidth()) {
                    mPanel =
                    new MustPanel
                           (playerPanel.getWidth(),
                           (int)(vc.getHeight() *
                           (float)(playerPanel.getWidth() /
                            vc.getPreferredSize().getWidth())),
                                        new GridLayout(1,1));

                    }
                    else {
                        mPanel =
                              new MustPanel(width, playerPanel.getHeight(),
                        new GridLayout(1,1));
                    }

                    // Setup the GUI
                    mPanel.add(vc);
                    playerPanel.setBackground
                             (playerPanel.getParent().getBackground());
                    playerPanel.add(mPanel);
                    */
                    // Temporary trying without calculation
                    playerPanel.setLayout(new GridLayout(1, 0, 0, 0));

                    RatioPanel rp = new RatioPanel();
                    rp.setRatio((int)vc.getPreferredSize().getWidth(),
                                (int)vc.getPreferredSize().getHeight());
                    /*
                    java.awt.Insets ppInsets = playerPanel.getInsets();
                    rp.setSize(playerPanel.getWidth()-ppInsets.left-
                            ppInsets.right,
                            playerPanel.getHeight()-ppInsets.top-
                            ppInsets.bottom);
                    */
                    rp.add(vc);
                    playerPanel.add(rp);
                    rp.initSize();
                    //playerPanel.getParent().repaint();

                }

                // Start the player and update the GUI
                playerPanel.validate();
            }
           }
           processor.start();

        }

            // Handle internal errors.
            if (ce instanceof ControllerErrorEvent) {

                processor.removeControllerListener(this);
                playerPanel = null;

                logger.severe("Internal error: " + ce);
            }
        }

        /**
         * Check if the receive streams is started or not.
         *
         * @return True if started, false if not.
         */
        public boolean isStarted() {
            return dataReceived;
        }

        /**
         * Provides audio controls.
         * Maps a JSlider to the volume and a button for mute.
         *
         * @param p The Player to control.
         */
        private void getCustomAudioControl() {//Player p) {

            if (playerPanel != null) {
                playerPanel.removeAll();
                playerPanel.setLayout(new java.awt.GridBagLayout());

                /*
                java.awt.GridBagConstraints constraints =
                   new java.awt.GridBagConstraints();
                constraints.fill = java.awt.GridBagConstraints.NONE;
                constraints.anchor = java.awt.GridBagConstraints.CENTER;
                */

                //gain = p.getGainControl();
                //float gl = 5;
                //gain.setLevel(gl);

                slider = new JSlider(JSlider.HORIZONTAL,0,10,5);
                slider.setMajorTickSpacing(10);
                slider.setMinorTickSpacing(1);
                slider.setPaintTicks(false);

                //Create the label table
                Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
                labelTable.put( Integer.valueOf( 0 ), new JLabel("-") );
                labelTable.put( Integer.valueOf( 10 ), new JLabel("+") );
                slider.setLabelTable( labelTable );
                slider.setPaintLabels(true);

                slider.addChangeListener(new SliderListener());

                int height = (int)slider.getMinimumSize().getHeight();

                slider.setMinimumSize(new Dimension(100, height));
                slider.setPreferredSize(new Dimension(100, height));
                slider.getAccessibleContext().setAccessibleDescription("Speaker volume control, left arrow to decrease  and right arrow to increase.");

                muteState = false;//gain.getMute();
                if (muteState) {
                    mute = new JToggleButton("Silent",true);
                    mute.getAccessibleContext().setAccessibleDescription("Unmute speakers.");
                }
                else {
                    mute = new JToggleButton("Silent",false);
                    mute.getAccessibleContext().setAccessibleDescription("Mute speakers.");
                }
                mute.addActionListener(this);
                mute.setEnabled(false);
                mute.setMargin(new Insets(1, 10, 1, 10));
                height = (int)mute.getMinimumSize().getHeight();
                mute.setMinimumSize(new Dimension(60, height));
                mute.setPreferredSize(new Dimension(60, height));

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.insets = new Insets(2, 2, 2, 2);
                gbc.weightx = 1.0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                playerPanel.add(slider,gbc);

                gbc = new GridBagConstraints();
                gbc.gridx = 1;
                gbc.gridy = 0;
                gbc.insets = new Insets(2, 2, 2, 2);
                gbc.fill = GridBagConstraints.NONE;
                playerPanel.add(mute,gbc);
                playerPanel.repaint();

            }

        }

        /**
         * Allows the speakers to be muted from other controls.
         *
         * @param state The requested state
         */
        public void setMute(boolean state) {
            if(muteState && !state) {
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
                if(muteState) {
                    mute.getAccessibleContext().setAccessibleDescription("Unmute speakers.");
                    slider.getAccessibleContext().setAccessibleDescription("Speaker volume control, disabled");
                }
                else {
                    mute.getAccessibleContext().setAccessibleDescription("Mute speakers.");
                    slider.getAccessibleContext().setAccessibleDescription("Speaker volume control, left arrow to decrease and right arrow to increase.");
                }
                parent.muteMicrophone(muteState);
                gain.setMute(muteState);
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
                    float volume = (float)source.getValue();
                    float gl = (float)volume/10;
                    gain.setLevel(gl);
                }
            }
        }
    }


