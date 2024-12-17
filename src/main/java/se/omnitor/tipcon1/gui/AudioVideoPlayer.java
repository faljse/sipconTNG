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
package se.omnitor.tipcon1.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import java.util.logging.Logger;

import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Format;
import javax.media.GainControl;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.control.FormatControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Plays audio and video from a given data source.
 *
 * @author Erik Zetterstrm, Omnitor AB
 */
public class AudioVideoPlayer implements ActionListener,
					 ControllerListener {

    private JPanel playerPanel = null;

    private Format captureFormat = null;
    private DataSource ds = null;
    private Player player = null;

    private Logger logger;

    private JButton mute = null;

    private boolean muteState = false;

    private GainControl gain = null;



    /**
     * Constructor
     *
     * @param ds The data source to use.
     * @param cf The capture format to use.
     * @param p  The panel to display appropriate control and video on.
     */

    public AudioVideoPlayer(DataSource ds, Format cf, JPanel p) {

	logger = Logger.getLogger("se.omnitor.tipcon1.gui");
	System.out.println("*****************************************CONSTRUCTOR AudioVideoPlayer");
        this.ds=ds;
        captureFormat=cf;
        playerPanel = p;
        init();
    }


    /**
     * Constructor
     *
     * @param ds The data source to use.
     * @param cf The capture format to use.
     * @param p  The panel to display appropriate control and video on.
     * @param s  0 displays the statndard audio, video control panel.
     *           1 displays a custom audio controlpanel using GainControl.
     */
    public AudioVideoPlayer(DataSource ds, Format cf, JPanel p, int s) {

	logger = Logger.getLogger("se.omnitor.tipcon1.gui");

        this.ds=ds;
        captureFormat=cf;
        playerPanel = p;
	System.out.println("INIT STYLE: "+s);

        init();
	}


    /**
     * Initialized the player.
     *
     */
    public void init() {

	System.out.println("***************************************INIT AudioVideoPlayer");
        FormatControl[] formatControls=((CaptureDevice)ds).getFormatControls();
        for (int i=0;i<formatControls.length;i++) {
            formatControls[i].setFormat(captureFormat);
        }

        try {
            player = javax.media.Manager.createPlayer(ds);
        } catch(NoPlayerException e) {
        	logger.throwing(this.getClass().getName(), "init", e);
            return;
        } catch(IOException e) {
        	logger.throwing(this.getClass().getName(), "init", e);
            return;
        }

        if (player == null) {
            return;
        }

        player.addControllerListener(this);
        player.realize();
    }


    /**
     * Handles state changes of the player.
     *
     * @param ce An event signaling a state change for this player.
     */
    public synchronized void controllerUpdate(ControllerEvent ce) {
        Player player;

        player = (Player)ce.getSourceController();

        if (player == null) {
            return;
        }

        if (ce instanceof RealizeCompleteEvent) {

            Component vc = player.getVisualComponent();
            Component cc = player.getControlPanelComponent();

            // Empty the panel and remember the contence
            if (playerPanel!=null) {
                playerPanel.removeAll();

                //playerPanel.setLayout(new GridLayout(1, 0, 0, 0));
                /*
                playerPanel.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.BOTH;
                gbc.anchor = GridBagConstraints.CENTER;
                */
                playerPanel.setLayout(new GridLayout(1, 0, 0, 0));
                playerPanel.setEnabled(false);


                //Audio
                if (vc==null && cc!=null) {
                    playerPanel.add(cc);
                }

                //Video
                else if (vc!=null && cc!=null) {

                    // Calculate the correct width and height for video
                    /*
                    width = (int)((float)vc.getPreferredSize().getWidth() *
                                  (float)(playerPanel.getHeight() /
                                          vc.getPreferredSize().getHeight()));

                    if (width > playerPanel.getWidth()) {
                        height = (int)(vc.getPreferredSize().getHeight() *
                                       ((float)playerPanel.getWidth() /
                                        (float)vc.getPreferredSize().
                                        getWidth()));

                        vc.setSize(playerPanel.getWidth(), height);
                        mPanel = new MustPanel(playerPanel.getWidth(),
                                               height,
                                               new GridLayout(0, 1, 0, 0));
                    }
                    else {
                        vc.setSize(width, playerPanel.getHeight());
                        mPanel = new MustPanel(width,
                                               playerPanel.getHeight(),
                                               new GridLayout(0, 1, 0, 0));
                    }
                    */

                    // Setup the panel
                    RatioPanel rp = new RatioPanel(RatioPanel.FIXED_HEIGHT);
                    rp.setRatio((int)vc.getPreferredSize().getWidth(),
                                (int)vc.getPreferredSize().getHeight());
                    /*
                    java.awt.Insets ppInsets = playerPanel.getInsets();
                    mPanel.setSize(playerPanel.getWidth()-ppInsets.left-
                                   ppInsets.right,
                                   playerPanel.getHeight()-ppInsets.top-
                                   ppInsets.bottom);
                    */
                    rp.add(vc);
                    playerPanel.add(rp);
                    rp.initSize();
                    /*
                    playerPanel.setBackground(playerPanel.
                                              getParent().
                                              getBackground());
                    */
                    //playerPanel.add(mPanel, gbc);
                    //playerPanel.repaint();
                }

                player.start();

                playerPanel.validate();

                playerPanel.repaint();



            }
        }

        if (ce instanceof ControllerErrorEvent) {
            player.removeControllerListener(this);
            playerPanel = null;
        }

        if (playerPanel != null) {
            Component parent = playerPanel.getParent();
            if (parent != null) {
                parent.repaint();
            }
        }

    }


    /**
     * Closes the player.
     */

    public void close() {
	playerPanel=null;
        player.close();
    }

/*
    private void getCustomAudioControl(Player p) {
	System.out.println("************************************GETCUSTOMAUDIOCONTROL");
	gain = p.getGainControl();
	float gl = 5^-1;
	gain.setLevel(gl);
	System.out.println("***********************Start gain: "+gain.getLevel());
	int g = 5;//(int)gain.getLevel()*10;

	slider = new JSlider(JSlider.HORIZONTAL,0,10,g);
	int height = (int)slider.getMinimumSize().getHeight();
	slider.setMinimumSize(new Dimension(100, height));
	slider.setPreferredSize(new Dimension(100, height));
	slider.setMajorTickSpacing(10);
	slider.setMinorTickSpacing(1);
	slider.setPaintTicks(false);
	slider.setPaintLabels(false);
	slider.addChangeListener(new SliderListener());

	muteState = gain.getMute();
	if (muteState) {
	    mute = new JButton("�nmute");
	}
	else {
	    mute = new JButton(" Mute ");
	}

	GridBagConstraints gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.insets = new Insets(0, 10, 0, 10);
	gbc.fill = GridBagConstraints.BOTH;;
	playerPanel.add(slider,gbc);
	playerPanel.add(mute,gbc);

    }
*/
    public void actionPerformed(ActionEvent ae) {
	Object source = ae.getSource();

	if (source == mute) {
	    muteState = !muteState;
	    if (muteState == true) {
		mute.setText("Unmute");
	    }
	    else {
		mute.setText(" Mute ");

	    }
	    gain.setMute(muteState);
	}
    }

   class SliderListener implements ChangeListener {

       public void stateChanged(ChangeEvent e) {
	    JSlider source = (JSlider)e.getSource();
	    if (!source.getValueIsAdjusting()) {
		int volume = (int)source.getValue();
		float gl = volume/(float)10;
		gain.setLevel(gl);
	    }
	}
    }
}

