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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import se.omnitor.protocol.sip.*;
import se.omnitor.protocol.t140.*;
import se.omnitor.tipcon1.*;

// import LogClasses
import java.util.Locale;
import java.util.logging.Logger;

/**
 * This class contains the GUI for the application. It creates and holds
 * all GUI elements and also listener functions.
 *
 * @author Andreas Piirimets, Omnitor AB
 * @author Erik Zetterstrom, Omnitor AB
 */
public class ProgramWindow extends JFrame implements AddressPanelListener,
						     ActionListener, MouseListener{

    /**
     * Defines the width och the right control tab
     *
     */
    //private static final int CONTROL_WIDTH = 150;

    /**
     * Defines the label for outgoing text.
     */
    public static final int LABEL_TEXT_OUT  = 1;

    /**
     * Defines the label for incoming text.
     */
    public static final int LABEL_TEXT_IN   = 2;

    /**
     * Defines the label for outgoing audio.
     */
    public static final int LABEL_AUDIO_OUT = 3;

    /**
     * Defines the label for incoming audio.
     */
    public static final int LABEL_AUDIO_IN  = 4;

    /**
     * Defines the label for outgoing video.
     */
    public static final int LABEL_VIDEO_OUT = 5;

    /**
     * Defines the label for incoming video.
     */
    public static final int LABEL_VIDEO_IN  = 6;

    private static final int OBJ_SPACE = 2;


    protected SipController sc;
    protected AppController mainProgram;
    protected AppSettings appSettings;
    private TextControlPanel textControlPanel;
    private Button hangUpButton;
    //private MustPanel audioRemotePanel;
    private Button localMuteButton;

    private boolean videoIsActivated;

    // Variables declaration - do not modify
    private JPanel logoPanel;
    private JPanel noLocalVideoPanel;
    private JPanel statusBarPanel;
    private T140Panel textPanel;
    private JPanel callControlPanel;
    private JPanel videoTextPanel;
    private JPanel localVideoCallControlPanel;
    private AddressPanel addressPanel;
    private JButton callButton;
    private JButton hangupButton;
    private JPanel localVideoPanel;
    private JTextField sipAddressTextField;
    private JTabbedPane tabPane;
    private JPanel remoteVideoPanel;
    private JLabel infoLabel;
    private Icon networkInactiveIcon;
    private Icon audioInactiveIcon;
    private Icon videoInactiveIcon;
    private Icon textInactiveIcon;
    private Icon userInactiveIcon;
    private Icon userActiveIcon;
    private JLabel audioLabel;
    private JLabel videoLabel;
    private JLabel textLabel;
    private JLabel registrarLabel;
    private JMenuItem saveMenuItem;
    private JMenuItem exitMenuItem;
    private JMenuItem audioMenuItem;
    private JMenuItem videoMenuItem;
    private JMenuItem textMenuItem;
    private JMenuItem alertMenuItem;
    private JMenuItem fontMenuItem;
    private JMenuItem sipMenuItem;
    private JMenuItem registerMenuItem;
    private JMenuItem networkMenuItem;
    private JMenuItem helpMenuItem;
    private JMenuItem fundingCreditsMenuItem;
    private JMenuItem aboutMenuItem;
    private JMenuItem detectMenuItem;
    private JMenuItem callMenuItem;
    private JMenuItem dialMenuItem;
    private JMenuItem hangupMenuItem;
    private JMenuItem addressMenuItem;
    private JMenuItem sendMenuItem;
    private JMenuItem receiveMenuItem;
    private JMenuItem languageMenuItem;

    private SettingsPanel settingsPanel;
    // End of variables declaration
    private JToggleButton muteCb;
    private JToggleButton muteCbSelf;
    private JSlider dummySlider;
    private JSlider dummySliderSelf;
    private Hashtable<Integer, JLabel> labelTable;
    private JPanel audioControlPanel;
    private JPanel audioControlMain;

    private JPanel selfControlPanel;

    // Variables declaring status of each media
    private int audioOut;
    private String audioOutCodec;
    private int audioIn;
    private String audioInCodec;
    private int videoOut;
    private String videoOutCodec;
    private int videoIn;
    private String videoInCodec;
    private int textOut;
    private String textOutCodec;
    private int textIn;
    private String textInCodec;

    private Properties language;

    private DetectingMove detectingMove;

    // sets a good size of the statusbars icons
    final Dimension DIMENSION = (new JLabel("SOME_TEXT")).getPreferredSize();

    // declare package and classname
    public final static String CLASS_NAME = ProgramWindow.class.getName();
    // get an instance of Logger
    private static Logger logger = Logger.getLogger(CLASS_NAME);

 

    /**
     * Initializes variables and GUI.
     *
     * @param sc The SIP controller used in this application
     * @param mainProgram The main program, which holds all settings.
     */
    public ProgramWindow() {
     	super();

     	Toolkit.getDefaultToolkit().addAWTEventListener( new AWTEventListener()
    	{
    		public void eventDispatched(AWTEvent e) {
    			if (e.getID() == KeyEvent.KEY_PRESSED) {
    				KeyEvent keyEvent = (KeyEvent)e;
    				if (keyEvent.isControlDown()) {
    					if (keyEvent.getKeyCode() == 49) {
    						int nbr = mainProgram.skipOneRtpTextSeqNo();
    						DialogFactory.showInformationMessageDialog("Skipping RTP seq no", "Skipping RTP seq no: " + nbr);
    					}
    					if (keyEvent.getKeyCode() == 48) {
    						String str = JOptionPane.showInputDialog("Enter HEX code to send");
    						System.out.println("Fetched: " + str);
    						mainProgram.sendHexCode(str);
    					}
    					keyEvent.consume();
  					}	
    			}
    		}
    	}, AWTEvent.KEY_EVENT_MASK);

    }

    public void init(SipController sc, AppController mainProgram, AppSettings appSettings) {
	this.sc = sc;
	this.mainProgram = mainProgram;
        this.appSettings = appSettings;
        
	language = mainProgram.getLanguage();
	textPanel = mainProgram.getT140Panel();

	textPanel.setColumns(20);

        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            setTitle(AppConstants.PROGRAM_NAME_TIPCON1);
        }
        else {
            setTitle(AppConstants.PROGRAM_NAME);
        }

        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            videoIsActivated = mainProgram.isVideoActivated();
        }
        else {
            videoIsActivated = false;
        }

	//initializeGui();
	initComponents();

	setTerminatedGui();

	DialogFactory.registerProgramFrame(this);

    }

    /**
     * Refreshes the GUI and assures that video is shown or not shown,
     * depending on user settings.
     *
     */
    public void refreshGui() {

        if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) &&  mainProgram.isVideoActivated() != videoIsActivated) {
	    videoIsActivated = mainProgram.isVideoActivated();

	     createLocalVideoCallControlPanel();
	     createVideoTextPanel();
	     validate();
	     repaint();

	 }
    }

    /**
     * Initializes the components.
     *
     */
     private void initComponents() {

	try {
	    UIManager.setLookAndFeel
		(UIManager.getSystemLookAndFeelClassName());
	} catch (ClassNotFoundException e) {
	    // If the system's look and feel could not be set, ignore it and
	    // continue with the default look and feel.
		logger.throwing(this.getClass().getName(), "initComponents", e);
	} catch (InstantiationException e) {
	    // If the system's look and feel could not be set, ignore it and
	    // continue with the default look and feel.
		logger.throwing(this.getClass().getName(), "initComponents", e);
	} catch (IllegalAccessException e) {
	    // If the system's look and feel could not be set, ignore it and
	    // continue with the default look and feel.
		logger.throwing(this.getClass().getName(), "initComponents", e);
	} catch (UnsupportedLookAndFeelException e) {
	    // If the system's look and feel could not be set, ignore it and
	    // continue with the default look and feel.
		logger.throwing(this.getClass().getName(), "initComponents", e);
	}

	callControlPanel = new JPanel();
        localVideoPanel = new JPanel();
	videoTextPanel = new JPanel();
	localVideoCallControlPanel = new JPanel(new GridBagLayout());
	//textPanel = new JPanel(new GridLayout(1, 1));

        sipAddressTextField = new JTextField();        
        hangupButton = new javax.swing.JButton();
	hangupButton.getAccessibleContext().setAccessibleDescription("Hangup");
	hangupButton.setMnemonic(KeyEvent.VK_H);
        callButton = new JButton();
	callButton.getAccessibleContext().setAccessibleDescription("Call");
	callButton.setMnemonic(KeyEvent.VK_C);
        tabPane = new javax.swing.JTabbedPane();
        remoteVideoPanel = new JPanel();
	infoLabel = new JLabel();
	audioLabel = new JLabel();
	videoLabel = new JLabel();
	textLabel = new JLabel();
	registrarLabel = new JLabel();
	//localTextArea = mainProgram.getLocalTextArea();
	//remoteTextArea = mainProgram.getRemoteTextArea();

	networkInactiveIcon =
	    new ImageIcon(AppConstants.NETWORK_INACTIVE_ICON_URL,
			  language.getProperty("se.omnitor.tipcon1." +
					       "NET_INACTIVE"));
/*
	networkActiveIcon =
	    new ImageIcon(AppConstants.NETWORK_ACTIVE_ICON_URL,
			  language.getProperty("se.omnitor.tipcon1." +
					       "NET_ACTIVE"));
*/
	   videoInactiveIcon =
	    new ImageIcon(AppConstants.VIDEO_INACTIVE_ICON_URL,
			  language.getProperty("se.omnitor.tipcon1." +
			       "VIDEO_INACTIVE"));
	audioInactiveIcon =
	    new ImageIcon(AppConstants.AUDIO_INACTIVE_ICON_URL,
			  language.getProperty("se.omnitor.tipcon1." +
					       "AUDIO_INACTIVE"));
	textInactiveIcon =
	    new ImageIcon(AppConstants.TEXT_INACTIVE_ICON_URL,
			  language.getProperty("se.omnitor.tipcon1." +
					       "TEXT_INACTIVE"));
	if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            userInactiveIcon =
                    new ImageIcon(AppConstants.USER_INACTIVE_ICON_URL,
                                  language.getProperty("se.omnitor.tipcon1." +
                                  "USER_INACTIVE"));
	}

	userActiveIcon =
	    new ImageIcon(AppConstants.USER_ACTIVE_ICON_URL,
			  language.getProperty("se.omnitor.tipcon1." +
					       "USER_ACTIVE"));


        getContentPane().setLayout(new java.awt.GridBagLayout());

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
		closeRequested();
            }
        });

	// Apply icon
	try {
	    Image icon =
		Toolkit.getDefaultToolkit().getImage
		(AppConstants.LOGO_ICON_URL);
	    setIconImage(icon);
	}
	catch (Exception e) {}

	applyGui();

	 createVideoTextPanel();
	 createLocalVideoCallControlPanel();


	SwingUtilities.updateComponentTreeUI(this);

     }

    /**
     * Prepares the GUI before showing it by packing it and setting it's
     * location to the center of the screen.
     *
     */
    public void prepare() {
	pack();
	refreshGui();

	// Set start coordinates
        this.setLocation(GuiToolkit.getCenterX() - getWidth()/2,
			 GuiToolkit.getCenterY() - getHeight()/2);

    }

    private void createVideoTextPanel() {
	videoTextPanel.removeAll();

	// Arrange remote video and text
	if (videoIsActivated) {
	    resetRemoteVideoPanel();
            videoTextPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 0.4;
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.NORTH;
            videoTextPanel.add(remoteVideoPanel, c);

            c.weighty = 0.6;
            c.gridx = 0;
            c.gridy = 1;
            videoTextPanel.add(textPanel, c);
	}
	else {
            videoTextPanel.setLayout(new BorderLayout());
	    videoTextPanel.add(textPanel, BorderLayout.CENTER);
	}
    }


    private void createLocalVideoCallControlPanel() {
	localVideoCallControlPanel.removeAll();

	localVideoCallControlPanel.setLayout(new BorderLayout());

	if (videoIsActivated) {
	    resetLocalVideoPanel();
	    localVideoCallControlPanel.add(localVideoPanel, BorderLayout.WEST);
	    localVideoCallControlPanel.add(callControlPanel,
					   BorderLayout.CENTER);

	    /*removed prev
	    resetLocalVideoPanel();

	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.insets = new Insets(0, OBJ_SPACE,
						   0, OBJ_SPACE);
	    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	    localVideoCallControlPanel.add(localVideoPanel,
					   gridBagConstraints);

	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.insets = new Insets(0, OBJ_SPACE,
						   0, OBJ_SPACE);
	    gridBagConstraints.gridx = 1;
	    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	    localVideoCallControlPanel.add(callControlPanel,
					   gridBagConstraints);
	    */
	}
	else {
	    localVideoCallControlPanel.add(callControlPanel,
					   BorderLayout.CENTER);

	    /*removed prev
	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
						   OBJ_SPACE, OBJ_SPACE);
	    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	    localVideoCallControlPanel.add(callControlPanel,
					   gridBagConstraints);
	    */
	}
    }

    /**
    * Creates a JMenu with all the elements/choises of the Help menu
    *
    * @return a JMenu
    */
    private JMenu createHelpMenu() {

        final String HELP = mainProgram.getLanguage().getProperty("se.omnitor.tipcon1.ProgramWindow.HELP");
        final String HELP_TOPICS = mainProgram.getLanguage().getProperty("se.omnitor.tipcon1.ProgramWindow.HELP_TOPICS");
        final String FUNDING_CREDITS = mainProgram.getLanguage().getProperty("se.omnitor.tipcon1.ProgramWindow.FUNDING_CREDITS");
        String ABOUT = "";
        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            ABOUT = mainProgram.getLanguage().getProperty(
                    "se.omnitor.tipcon1.ProgramWindow.ABOUT") + " " +
                    AppConstants.PROGRAM_NAME_TIPCON1;
        }
        else {
            ABOUT = mainProgram.getLanguage().getProperty(
                    "se.omnitor.tipcon1.ProgramWindow.ABOUT") + " " +
                    AppConstants.PROGRAM_NAME;
        }
        JMenu helpMenu = new JMenu(HELP);

        helpMenu.setMnemonic(KeyEvent.VK_E);

        // create and add help topics
        helpMenuItem = new JMenuItem(HELP_TOPICS);
        helpMenuItem.setMnemonic(KeyEvent.VK_H);
        helpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
        helpMenuItem.setEnabled(false);


        // create and add funding credits
        fundingCreditsMenuItem = new JMenuItem(FUNDING_CREDITS);
        fundingCreditsMenuItem.setMnemonic(KeyEvent.VK_U);
        fundingCreditsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,KeyEvent.CTRL_MASK));
        fundingCreditsMenuItem.addActionListener(this);
        helpMenu.add(fundingCreditsMenuItem);

        // create and add about
        helpMenu.addSeparator();
        aboutMenuItem = new JMenuItem(ABOUT);
        aboutMenuItem.setMnemonic(KeyEvent.VK_B);
        aboutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,KeyEvent.CTRL_MASK));
        aboutMenuItem.addActionListener(this);
        helpMenu.add(aboutMenuItem);

        return helpMenu;
    }

    /**
     * Creates a JMenu with all the elements/choises of the File menu
     *
     * @return a JMenu
     */
    private JMenu createFileMenu() {

        final String FILE = mainProgram.getLanguage().getProperty("se.omnitor.tipcon1.ProgramWindow.FILE");
        final String SAVE_TEXT = mainProgram.getLanguage().getProperty("se.omnitor.tipcon1.ProgramWindow.SAVE_TEXT");
        final String EXIT = mainProgram.getLanguage().getProperty("se.omnitor.tipcon1.ProgramWindow.EXIT");
        JMenu fileMenu = new JMenu(FILE);

        fileMenu.setMnemonic(KeyEvent.VK_F);

        // create and add save text choise
        saveMenuItem = new JMenuItem(SAVE_TEXT);
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.CTRL_MASK));
        saveMenuItem.addActionListener(this);

        // create and add exit choise
        fileMenu.add(saveMenuItem);
        exitMenuItem = new JMenuItem(EXIT);
        exitMenuItem.setMnemonic(KeyEvent.VK_E);
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,KeyEvent.CTRL_MASK));
        exitMenuItem.addActionListener(this);
        fileMenu.add(exitMenuItem);

        return fileMenu;
    }

      /**
       * Creates a JMenu with all the elements/choises of the Settings menu
       *
       * @return a JMenu
       */
      private JMenu createSettingsMenu() {

          final String SETTINGS = mainProgram.getLanguage().getProperty(
                  "se.omnitor.tipcon1.ProgramWindow.SETTINGS");
          final String AUDIO = mainProgram.getLanguage().getProperty(
                  "se.omnitor.tipcon1.ProgramWindow.AUDIO");
          final String VIDEO = mainProgram.getLanguage().getProperty(
                  "se.omnitor.tipcon1.ProgramWindow.VIDEO");
          final String TEXT_SETTINGS = mainProgram.getLanguage().getProperty(
                  "se.omnitor.tipcon1.ProgramWindow.TEXT_SETTINGS");
          final String ALERT = mainProgram.getLanguage().getProperty(
                  "se.omnitor.tipcon1.ProgramWindow.ALERT");
          final String TEXT_FONT = mainProgram.getLanguage().getProperty(
                  "se.omnitor.tipcon1.ProgramWindow.TEXT_FONT");
          final String SIP = mainProgram.getLanguage().getProperty(
                  "se.omnitor.tipcon1.ProgramWindow.SIP");
          final String DETECT_DEVICES = mainProgram.getLanguage().getProperty(
                  "se.omnitor.tipcon1.ProgramWindow.DETECT_DEVICES");
          final String REGISTER = mainProgram.getLanguage().getProperty(
                  "se.omnitor.tipcon1.ProgramWindow.REGISTER");
          final String LANGUAGE = mainProgram.getLanguage().getProperty(
	          "se.omnitor.tipcon1.ProgramWindow.LANGUAGE");

          JMenu settingsMenu = new JMenu(SETTINGS);

          settingsMenu.setMnemonic(KeyEvent.VK_S);

          // create and add audio settings
          audioMenuItem = new JMenuItem(AUDIO);
          audioMenuItem.setMnemonic(KeyEvent.VK_M);
          audioMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
                  KeyEvent.CTRL_MASK));
          audioMenuItem.addActionListener(this);
          settingsMenu.add(audioMenuItem);


          // create and add video settings
          if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
              videoMenuItem = new JMenuItem(VIDEO);
              videoMenuItem.setMnemonic(KeyEvent.VK_W);
              videoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                      KeyEvent.CTRL_MASK));
              videoMenuItem.addActionListener(this);
              settingsMenu.add(videoMenuItem);
          }

          // create and add text settings
          textMenuItem = new JMenuItem(TEXT_SETTINGS);
          textMenuItem.setMnemonic(KeyEvent.VK_T);
          textMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,KeyEvent.CTRL_MASK));
          textMenuItem.addActionListener(this);
          settingsMenu.add(textMenuItem);

          // create and add alert settings
          alertMenuItem = new JMenuItem(ALERT);
          alertMenuItem.setMnemonic(KeyEvent.VK_G);
          alertMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,KeyEvent.CTRL_MASK));
          alertMenuItem.addActionListener(this);
          settingsMenu.add(alertMenuItem);

          // create and add text font settings
          fontMenuItem = new JMenuItem(TEXT_FONT);
          fontMenuItem.setMnemonic(KeyEvent.VK_J);
          fontMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J,KeyEvent.CTRL_MASK));
          fontMenuItem.addActionListener(this);
          settingsMenu.add(fontMenuItem);

	  // create and add SIP settings
          sipMenuItem = new JMenuItem(SIP);
          sipMenuItem.setMnemonic(KeyEvent.VK_I);
          sipMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,KeyEvent.CTRL_MASK));
          sipMenuItem.addActionListener(this);
          settingsMenu.add(sipMenuItem);

          // create and add detect devices
          detectMenuItem = new JMenuItem(DETECT_DEVICES);
          detectMenuItem.setMnemonic(KeyEvent.VK_D);
          detectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,KeyEvent.CTRL_MASK));
          detectMenuItem.addActionListener(this);
          settingsMenu.add(detectMenuItem);

          // create and add register
          registerMenuItem = new JMenuItem(REGISTER);
          registerMenuItem.setMnemonic(KeyEvent.VK_R);
          registerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.CTRL_MASK));
          registerMenuItem.addActionListener(this);
          settingsMenu.add(registerMenuItem);

          // create and add language
          languageMenuItem = new JMenuItem(LANGUAGE);
          languageMenuItem.setMnemonic(KeyEvent.VK_L);
          languageMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,KeyEvent.CTRL_MASK));
          languageMenuItem.addActionListener(this);
	  settingsMenu.add(languageMenuItem);

          return settingsMenu;
      }

    /**
    * Creates a JMenu with all the elements/choises of a HIDDEN quickmenu.
    * This menu is hidden so there is no languagesupport implemented
    * for this menu.
    *
    * @return a JMenu
    */
    private JMenu createQuickMenu() {

        JMenu quickMenu = new JMenu("Quick jump");

        /*sendMenuItem = new JMenuItem("Send text (out)");
        sendMenuItem.setMnemonic(KeyEvent.VK_O);
        sendMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                KeyEvent.ALT_MASK));
        sendMenuItem.addActionListener(this);
        quickMenu.add(sendMenuItem); */

        /* receiveMenuItem = new JMenuItem("Receive text (in)");
        receiveMenuItem.setMnemonic(KeyEvent.VK_I);
        receiveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
                KeyEvent.ALT_MASK));
        receiveMenuItem.addActionListener(this);
        quickMenu.add(receiveMenuItem); */

        quickMenu.setVisible(false);

        return quickMenu;
    }

    JPanel createStatusBar() {

        // Arrange status panel
        statusBarPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints;

        final String DISCONNECTED = mainProgram.getLanguage().getProperty(
                  "se.omnitor.tipcon1.ProgramWindow.DISCONNECTED");

        infoLabel.setText(DISCONNECTED);
        infoLabel.setIcon(networkInactiveIcon);
        infoLabel.setHorizontalAlignment(SwingConstants.LEFT);
        infoLabel.setBorder(new ShallowBorder(2, 2, 2, 2));
        infoLabel.setFocusable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 0, OBJ_SPACE);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        statusBarPanel.add(infoLabel, gridBagConstraints);

        audioLabel.setIcon(audioInactiveIcon);
        audioLabel.setHorizontalAlignment(SwingConstants.LEFT);
        audioLabel.setBorder(new ShallowBorder(2, 2, 2, 2));
        audioLabel.setFocusable(false);
        audioLabel.setMinimumSize(DIMENSION);
        audioLabel.setPreferredSize(DIMENSION);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, OBJ_SPACE, 0, OBJ_SPACE);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        statusBarPanel.add(audioLabel, gridBagConstraints);

        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            videoLabel.setIcon(videoInactiveIcon);
            videoLabel.setHorizontalAlignment(SwingConstants.LEFT);
            videoLabel.setBorder(new ShallowBorder(2, 2, 2, 2));
            videoLabel.setPreferredSize(DIMENSION);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new Insets(0, OBJ_SPACE, 0, OBJ_SPACE);
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            statusBarPanel.add(videoLabel, gridBagConstraints);
        }

        textLabel.setIcon(textInactiveIcon);
        textLabel.setHorizontalAlignment(SwingConstants.LEFT);
        textLabel.setBorder(new ShallowBorder(2, 2, 2, 2));
        textLabel.setFocusable(false);
        textLabel.setPreferredSize(DIMENSION);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, OBJ_SPACE, 0, OBJ_SPACE);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        statusBarPanel.add(textLabel, gridBagConstraints);

        setRegNa();
        registrarLabel.setHorizontalAlignment(SwingConstants.LEFT);
        registrarLabel.setBorder(new ShallowBorder(2, 2, 2, 2));
        registrarLabel.setFocusable(false);
        registrarLabel.setPreferredSize(registrarLabel.getPreferredSize());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, OBJ_SPACE, 0, 0);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        statusBarPanel.add(registrarLabel, gridBagConstraints);

        return statusBarPanel;
    }

    private void applyGui() {

        GridBagConstraints gridBagConstraints;

	// Arrange the menubar (File, Settings...)
        // start with creating FileMenu
        JMenu fileMenu = createFileMenu();
        JMenu quickMenu = createQuickMenu();
        JMenu settingsMenu = createSettingsMenu();
        JMenu helpMenu = createHelpMenu();

        // add all JMenus to the menubar
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(quickMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);

	// add menubar to main container
    	getRootPane().setJMenuBar(menuBar);

        statusBarPanel = createStatusBar();

	// Arrange local video panel
        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            JLabel noLocalVideoLabel =
            new JLabel(language.getProperty("se.omnitor.tipcon1.ProgramWindow.NO_LOCAL_VIDEO"));

            noLocalVideoLabel.setForeground(SystemColor.controlShadow);
            noLocalVideoLabel.setHorizontalAlignment(SwingConstants.CENTER);

            RatioPanel rp = new RatioPanel(RatioPanel.FIXED_HEIGHT);
            rp.setRatio(4, 3);
            rp.add(noLocalVideoLabel);

            noLocalVideoPanel = new JPanel(new GridLayout(1, 0, 0, 0));
            noLocalVideoPanel.setBorder(BorderFactory.createLoweredBevelBorder());
            noLocalVideoPanel.add(rp);
            rp.initSize();
        }

	// Arrange text panel
	//textPanel = new JPanel(new GridLayout(1, 1));

	//textPanel =  mainProgram.getT140Panel();
	//remoteTextArea.setPreferredSize(new Dimension(275, 85));
	//remoteTextArea.setRows(7);
	//remoteTextArea.setColumns(15);
	//textPanel.add(remoteTextArea);
	//localTextArea.setPreferredSize(new Dimension(275, 85));
	//localTextArea.setRows(7);
	//localTextArea.setColumns(15);
	//textPanel.add(localTextArea);




	// Arrange the tab panel
	addressPanel =
	    new AddressPanel(this, this, 200, 275,
			     AppConstants.ADDRESS_BOOK_DATA_FILE_URL,
			     language);
	tabPane.addTab(language.getProperty("se.omnitor.tipcon1.ProgramWindow.ADDRESS"), addressPanel);
	settingsPanel = new SettingsPanel(mainProgram, this);
	tabPane.addChangeListener(settingsPanel);
	tabPane.addTab(language.getProperty("se.omnitor.tipcon1.ProgramWindow.SETTINGS"), settingsPanel);
	tabPane.setSelectedIndex(0);
	tabPane.setPreferredSize(new Dimension(200, 275));
	
	// Arrange call control panel
	callControlPanel = new JPanel(new GridBagLayout());
	callControlPanel.setBorder
	    (BorderFactory.createTitledBorder
	     (BorderFactory.createEtchedBorder(),
              language.getProperty("se.omnitor.tipcon1.ProgramWindow.CALL_CONTROL")));
	
	sipAddressTextField.addActionListener(this);
	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 0;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.gridwidth = 2;
	gridBagConstraints.weightx = 1.0;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	callControlPanel.add(sipAddressTextField, gridBagConstraints);
		
    	final String CALL = mainProgram.getLanguage().getProperty(
                  "se.omnitor.tipcon1.ProgramWindow.CALL");
        final String HANGUP = mainProgram.getLanguage().getProperty(
                  "se.omnitor.tipcon1.ProgramWindow.HANGUP");

        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, OBJ_SPACE, 0));
    	callButton.setText(CALL);
	callButton.addActionListener(this);
	callButton.setMargin(new Insets(0, 10, 0, 10));
	buttonPanel.add(callButton);
        hangupButton.setText(HANGUP);
	hangupButton.addActionListener(this);
	hangupButton.setMargin(new Insets(0, 10, 0, 10));
	buttonPanel.add(hangupButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
	gridBagConstraints.anchor = GridBagConstraints.CENTER;
	gridBagConstraints.weightx = 1.0;
        callControlPanel.add(buttonPanel, gridBagConstraints);


	// Arrange audio control panel
	audioControlPanel = new JPanel(new GridBagLayout());
	selfControlPanel  = new JPanel(new GridBagLayout());
	audioControlMain  = new JPanel(new GridBagLayout());
	audioControlMain.setBorder
	    (BorderFactory.createTitledBorder
	     (BorderFactory.createEtchedBorder(),
              language.getProperty("se.omnitor.tipcon1.ProgramWindow.AUDIO_CONTROL")));

	//Dummy controls
	muteCb = new JToggleButton(
                   language.getProperty("se.omnitor.tipcon1.ProgramWindow.SILENT"),
                   false);
	muteCb.getAccessibleContext().setAccessibleDescription("Mute speakers, disabled.");
	muteCb.setMargin(new Insets(1, 10, 1, 10));
	//muteCb.addActionListener(this);
	muteCb.setEnabled(false);
	int height = (int)muteCb.getMinimumSize().getHeight();
	muteCb.setMinimumSize(new Dimension(60, height));
	muteCb.setPreferredSize(new Dimension(60, height));

	muteCbSelf = new JToggleButton(
        	   language.getProperty("se.omnitor.tipcon1.ProgramWindow.MUTE"),
                   false);
	muteCbSelf.getAccessibleContext().setAccessibleDescription("Mute microphone, disabled.");
	muteCbSelf.setMargin(new Insets(1, 10, 1, 10));
	muteCbSelf.setEnabled(false);
	muteCbSelf.setMinimumSize(new Dimension(60, height));
	muteCbSelf.setPreferredSize(new Dimension(60, height));

	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 1;
	gridBagConstraints.gridy = 0;
	//gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	audioControlPanel.add(muteCb, gridBagConstraints);

	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 1;
	gridBagConstraints.gridy = 0;
	//gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	selfControlPanel.add(muteCbSelf, gridBagConstraints);

	labelTable = new Hashtable<Integer, JLabel>();
	labelTable.put( Integer.valueOf( 0 ), new JLabel("-") );
	labelTable.put( Integer.valueOf( 10 ), new JLabel("+") );

	dummySlider = new JSlider(JSlider.HORIZONTAL,0,10,5);
	dummySlider.getAccessibleContext().setAccessibleDescription("Speaker volume control, disabled.");
	dummySlider.setMajorTickSpacing(10);
	dummySlider.setMinorTickSpacing(1);
	dummySlider.setPaintTicks(false);
	dummySlider.setLabelTable(labelTable);
	dummySlider.setPaintLabels(true);
	dummySlider.setEnabled(false);
	height = (int)dummySlider.getMinimumSize().getHeight();
	dummySlider.setMinimumSize(new Dimension(100, height));
	dummySlider.setPreferredSize(new Dimension(100, height));

        dummySliderSelf = new JSlider(JSlider.HORIZONTAL,0,10,5);
	dummySliderSelf.getAccessibleContext().setAccessibleDescription("Microphone volume control, disabled.");
	dummySliderSelf.setMajorTickSpacing(10);
	dummySliderSelf.setMinorTickSpacing(1);
	dummySliderSelf.setPaintTicks(false);
	dummySliderSelf.setLabelTable(labelTable);
	dummySliderSelf.setPaintLabels(true);
	dummySliderSelf.setEnabled(false);
	height = (int)dummySliderSelf.getMinimumSize().getHeight();
	dummySliderSelf.setMinimumSize(new Dimension(100, height));
	dummySliderSelf.setPreferredSize(new Dimension(100, height));

	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 0;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.weightx = 1.0;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	audioControlPanel.add(dummySlider, gridBagConstraints);

	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 0;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.weightx = 1.0;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	selfControlPanel.add(dummySliderSelf, gridBagConstraints);

	/*
	JToggleButton voiceCb = new JToggleButton("Voice", false);
	voiceCb.setMargin(new Insets(1, 10, 1, 10));
	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 2;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.gridwidth = 1;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	//audioControlPanel.add(voiceCb, gridBagConstraints);
	*/

	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       15, OBJ_SPACE);
	gridBagConstraints.gridx = 0;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.anchor = GridBagConstraints.CENTER;
	//audioControlSymbols.add(new ImagePanel(AppConstants.
	//				     AUDIO_INACTIVE_ICON_URL),
	//		      gridBagConstraints);
	ImagePanel audioPanel =new ImagePanel(AppConstants.
					      AUDIO_INACTIVE_ICON_URL);
	audioControlMain.add(audioPanel,
			     gridBagConstraints);

	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 0;
	gridBagConstraints.gridy = 1;
	gridBagConstraints.anchor = GridBagConstraints.CENTER;
	//audioControlSymbols.add(new ImagePanel(AppConstants.
	//				     AUDIO_INACTIVE_ICON_URL),
	//		      gridBagConstraints);

	ImagePanel micPanel = new ImagePanel(AppConstants.MIC_IMAGE_URL);

	micPanel.setFocusable(false);
	audioControlMain.add(micPanel,gridBagConstraints);

	/*
	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 3;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.gridwidth = 1;
	gridBagConstraints.weightx = 1.0;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    //audioControlPanel.add(new JPanel(), gridBagConstraints);
	*/

	/*
	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 0;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.gridheight = 2;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	audioControlMain.add(audioControlSymbols,gridBagConstraints);
	*/

	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 1;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.weightx = 1.0;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	audioControlMain.add(audioControlPanel,gridBagConstraints);

	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 1;
	gridBagConstraints.gridy = 1;
	gridBagConstraints.weightx = 1.0;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	audioControlMain.add(selfControlPanel,gridBagConstraints);

	// Position GUI elements
        gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
        gridBagConstraints.gridheight = 4;
	gridBagConstraints.weightx = 1.0;
	gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(videoTextPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(statusBarPanel, gridBagConstraints);

	 gridBagConstraints = new java.awt.GridBagConstraints();
	 gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
	 				       OBJ_SPACE, OBJ_SPACE);
	 gridBagConstraints.gridx = 1;
	 gridBagConstraints.gridy = 0;
	 gridBagConstraints.gridwidth = 2;
	 gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	 getContentPane().add(localVideoCallControlPanel, gridBagConstraints);

	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 1;
	gridBagConstraints.gridy = 1;
	gridBagConstraints.gridwidth = 2;
	gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	getContentPane().add(audioControlMain, gridBagConstraints);

	/*
        sipAddressLabel.setText("Address");
        gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        callControlPanel.add(sipAddressLabel, gridBagConstraints);
	*/

	/*
        gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        callControlPanel.add(callIconPanel, gridBagConstraints);
	*/

        gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
	gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	gridBagConstraints.weighty = 1.0;
        getContentPane().add(tabPane, gridBagConstraints);
        //getContentPane().add(addressPanel, gridBagConstraints);

        textControlPanel = new TextControlPanel(this,language.getProperty("se.omnitor.tipcon1.ProgramWindow.EMPTY"));
        GridBagConstraints gbc;
	gbc = new GridBagConstraints();
	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
	gbc.gridx = 0;
	gbc.gridy = 3;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.anchor = GridBagConstraints.WEST;
	callControlPanel.add(textControlPanel, gbc);

	ImagePanel logoIconPanel =
	    new ImagePanel(AppConstants.TRACECENTER_LOGO_URL);
	logoIconPanel.setFocusable(false);

	logoPanel = new JPanel(new GridBagLayout());
        logoPanel.addMouseListener(this);
	logoPanel.setBorder
	    (BorderFactory.createTitledBorder
	     (BorderFactory.createEtchedBorder()));
	logoPanel.setFocusable(false);
	logoPanel.setBackground(new Color(250, 250, 250));
	gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.gridx = 0;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.anchor = GridBagConstraints.CENTER;
	gridBagConstraints.fill = GridBagConstraints.NONE;
	logoPanel.add(logoIconPanel, gridBagConstraints);

	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
	gridBagConstraints.gridwidth = 2;
	gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	getContentPane().add(logoPanel,gridBagConstraints);

	this.setFocusTraversalPolicy(new TerminatedFocusTraversalPolicy());

     }
    
    /**
     * Changes a codec status label. "None" and strings starting with "Trying"
     * will be in light gray, "Failed" will be in red and all others in black.
     *
     * @param labelType The label to change, one of the constants in this class
     * @param text The new codec name. An empty string gives "None".
     */
    public void changeStatusLabel(int labelType, String text) {
	JLabel label;
	int info;
	String mediaName;
	String inCodec;
	String outCodec;
	int in;
	int out;

	final int NONE = 1;
	final int FAILED = 2;
	final int TRYING = 3;
	final int OK = 4;

        if (text.equals("") || text.equals("None")) {
	    text = "";
	    info = NONE;
        }
        else if (text.startsWith("Trying")) {
	    info = TRYING;
	    text = "";
	}
        else if (text.equals("Failed")) {
	    info = FAILED;
	    text = "";
        }
        else {
	    info = OK;
        }

	switch (labelType) {
	case LABEL_TEXT_OUT:
	    textOut = info;
	    textOutCodec = text;
	    in = textIn;
	    out = textOut;
	    inCodec = textInCodec;
	    outCodec = textOutCodec;
	    mediaName = language.getProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_TEXT");
	    label = textLabel;
	    break;
	case LABEL_TEXT_IN:
	    textIn = info;
	    textInCodec = text;
	    in = textIn;
	    out = textOut;
	    inCodec = textInCodec;
	    outCodec = textOutCodec;
	    mediaName = language.getProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_TEXT");
	    label = textLabel;
	    break;
	case LABEL_AUDIO_OUT:
	    audioOut = info;
	    audioOutCodec = text;
	    in = audioIn;
	    out = audioOut;
	    inCodec = audioInCodec;
	    outCodec = audioOutCodec;
	    mediaName = language.getProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_AUDIO");
	    label = audioLabel;
	    break;
	case LABEL_AUDIO_IN:
	    audioIn = info;
	    audioInCodec = text;
	    in = audioIn;
	    out = audioOut;
	    inCodec = audioInCodec;
	    outCodec = audioOutCodec;
	    mediaName = language.getProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_AUDIO");
	    label = audioLabel;
	    break;
	 case LABEL_VIDEO_OUT:
	    videoOut = info;
	    videoOutCodec = text;
	    in = videoIn;
	    out = videoOut;
	    inCodec = videoInCodec;
	    outCodec = videoOutCodec;
	    mediaName = language.getProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_VIDEO");
	    label = videoLabel;
	    break;
	case LABEL_VIDEO_IN:
	    videoIn = info;
	    videoInCodec = text;
	    in = videoIn;
	    out = videoOut;
	    inCodec = videoInCodec;
	    outCodec = videoOutCodec;
	    mediaName = language.getProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_VIDEO");
	    label = videoLabel;
	    break;
	default:
	    return;
	}

	String outStr;
	String inStr;
	switch (out) {
	case OK:
	    outStr = outCodec;
	    break;
	case FAILED:
	    outStr = "<font color=#c00000>Failed</font>";
	    break;
	case TRYING:
	    outStr = "<font color=#606060>Trying ..</font>";
	    break;
	default:
	    outStr = "-";
	}

	switch (in) {
	case OK:
	    inStr = inCodec;
	    break;
	case FAILED:
	    inStr = "<font color=#c00000>Failed</font>";
	    break;
	case TRYING:
	    inStr = "<font color=#606060>Trying ..</font>";
	    break;
	default:
	    inStr = "-";
	}

	label.setToolTipText("<html><b>In:</b> " + inStr + "<br>" +
			     "<b>Out:</b> " + outStr + "</html>");

	if (out == OK && in == OK) {
	    label.setForeground(SystemColor.textText);
	    label.setText(mediaName + " " + language.getProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_ON"));
	}
	else if (out == TRYING || in == TRYING) {
	    label.setForeground(SystemColor.textInactiveText);
	    label.setText(language.getProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_TRYING"));
	}
	else if (out == OK) {
	    label.setForeground(SystemColor.textText);
	    label.setText(language.getProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_OUT_ONLY"));
	}
	else if (in == OK) {
	    label.setForeground(SystemColor.textText);
	    label.setText(language.getProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_IN_ONLY"));
	}
	else if (in == FAILED || out == FAILED) {
	    label.setForeground(Color.RED);
	    label.setText(language.getProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_FAILED"));
	}
	else {
	    label.setText("");
	    label.setToolTipText(null);
	}

    }

    /**
     * Sets the GUI to look like no connection is established.
     *
     */
    public void setTerminatedGui() {
    sipAddressTextField.setEnabled(true);
	callButton.setEnabled(true);
	hangupButton.setEnabled(false);
	//settingsMenu.setEnabled(true);
	audioMenuItem.setEnabled(true);
	if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            videoMenuItem.setEnabled(true);
	}
        textMenuItem.setEnabled(true);
	sipMenuItem.setEnabled(true);
	//networkMenuItem.setEnabled(true);
	detectMenuItem.setEnabled(true);

	audioControlPanel.removeAll();
	selfControlPanel.removeAll();

        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            setTitle(AppConstants.PROGRAM_NAME_TIPCON1 + language.getProperty("se.omnitor.tipcon1.ProgramWindow.CONVERSATION_NONE"));

        }
        else {
            setTitle(AppConstants.PROGRAM_NAME + language.getProperty("se.omnitor.tipcon1.ProgramWindow.CONVERSATION_NONE"));
        }

	GridBagConstraints gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 0;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.weightx = 1.0;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	audioControlPanel.add(dummySlider,gridBagConstraints);

	gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 0;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.weightx = 1.0;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	selfControlPanel.add(dummySliderSelf,gridBagConstraints);

	gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 1;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.gridwidth = 1;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	audioControlPanel.add(muteCb,gridBagConstraints);

	gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 1;
	gridBagConstraints.gridy = 0;
	gridBagConstraints.gridwidth = 1;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	selfControlPanel.add(muteCbSelf,gridBagConstraints);

	//muteCb.setEnabled(false);
	changeStatusLabel(LABEL_TEXT_IN, "");
	changeStatusLabel(LABEL_TEXT_OUT, "");
        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            changeStatusLabel(LABEL_VIDEO_IN, "");
            changeStatusLabel(LABEL_VIDEO_OUT, "");
        }
	changeStatusLabel(LABEL_AUDIO_IN, "");
	changeStatusLabel(LABEL_AUDIO_OUT, "");
	/*
        callLabel.setForeground(Color.BLACK);
        callInput.setEnabled(true);
        callButton.setEnabled(true);
        hangUpButton.setEnabled(false);
	*/
	mainProgram.getLocalTextArea().setEditable(false);
	mainProgram.getLocalTextArea().setActiveLook(false);
	mainProgram.getRemoteTextArea().setActiveLook(false);
	if (appSettings.isRealtimePreviewEnabled()) {
		textPanel.stopRtpvTimer();
	}
	/*
	tabPane.setEnabledAt(1, true);
	*/
	/*
	settingsMenuItem.setEnabled(true);
	repaint();
	*/

        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            resetRemoteVideoPanel();
        }
        sipAddressTextField.requestFocus();

	this.setFocusTraversalPolicy(new TerminatedFocusTraversalPolicy());
    }

    /**
     * Sets the GUI to look like a session is established
     *
     */
    public void setEstablishedGui() {

        mainProgram.getRemoteTextArea().setWText("");
        mainProgram.getLocalTextArea().setWText("");

	sipAddressTextField.setEnabled(false);
	callButton.setEnabled(false);
	hangupButton.setEnabled(true);
	//settingsMenu.setEnabled(false);
	audioMenuItem.setEnabled(false);

        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            videoMenuItem.setEnabled(false);
        }
        textMenuItem.setEnabled(false);
	sipMenuItem.setEnabled(false);
	//networkMenuItem.setEnabled(false);
	detectMenuItem.setEnabled(false);

	if (appSettings.isRealtimePreviewTimerEnabled()) {
		textPanel.startRtpvTimer();
	}
	
	//muteCb.setEnabled(true);
	/*
	tabPane.setEnabledAt(1, false);
	if (tabPane.getSelectedIndex() == 1) {
	    tabPane.setSelectedIndex(0);
	}
	*/
	/*
        callLabel.setForeground(Color.GRAY);
        callLabel.repaint();
        callInput.setEnabled(false);
        callInput.repaint();
        callButton.setEnabled(false);
        callButton.repaint();
        hangUpButton.setEnabled(true);
        hangUpButton.repaint();
	settingsMenuItem.setEnabled(false);
	*/
	this.setFocusTraversalPolicy(new EstablishedFocusTraversalPolicy());
    }

    /**
     * Sets the GUI to look like a session is pending
     *
     */
    public void setPendingGui() {
	sipAddressTextField.setEnabled(false);
	callButton.setEnabled(false);
	hangupButton.setEnabled(false);
	//settingsMenu.setEnabled(false);
	audioMenuItem.setEnabled(false);
        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            videoMenuItem.setEnabled(false);
        }
	textMenuItem.setEnabled(false);
	sipMenuItem.setEnabled(false);
	//networkMenuItem.setEnabled(false);
	detectMenuItem.setEnabled(false);

        /*audioLabel.setPreferredSize(DIMENSION);
        videoLabel.setPreferredSize(DIMENSION);
        textLabel.setPreferredSize(DIMENSION);*/


	/*
	tabPane.setEnabledAt(1, false);
	if (tabPane.getSelectedIndex() == 1) {
	    tabPane.setSelectedIndex(0);
	}
	*/
	/*
        callLabel.setForeground(Color.GRAY);
        callLabel.repaint();
        callInput.setEnabled(false);
        callInput.repaint();
        callButton.setEnabled(false);
        callButton.repaint();
        hangUpButton.setEnabled(false);
        hangUpButton.repaint();
	settingsMenuItem.setEnabled(false);
	*/
	//this.setFocusTraversalPolicy(new EstablishedFocusTraversalPolicy());
    }

    /**
     * Clears the text areas.
     *
     */
    public void clearTextAreas() {
    	T140TextArea area = mainProgram.getRemoteTextArea();
    	if (area != null) {
    		area.setText("");
    	}

    	area = mainProgram.getLocalTextArea();
    	if (area != null) {
    		area.setText("");
    	}

    	T140LogArea larea = mainProgram.getLogTextArea();
    	if (larea != null) {
    		larea.clearArea();
    	}
    }

    /**
     * Sets the given text into the info label
     *
     * @param text The new message
     */
    public void changeInfoLabel(String text) {
        infoLabel.setText(text);
        infoLabel.repaint();
    }

    /**
     * Gets the remote video panel
     *
     * @return The remote video panel
     */
    public JPanel getVideoRemotePanel() {
	return remoteVideoPanel;
    }

    /**
     * Gets the remote audio control panel
     *
     * @return The remote audio control panel
     */
    public JPanel getAudioRemotePanel() {
	return audioControlPanel;
    }


    public JPanel getAudioLocalPanel() {
	return selfControlPanel;
    }


    /**
     * Gets the local video panel
     *
     * @return The local video panel
     */

    public JPanel getVideoSelfPanel() {
	return localVideoPanel;
    }

    /**
     * This function changes the layout of the call control buttons. Should
     * they be enabled or not?
     *
     * @param isEnabled Whether the call control buttons should be enabled.
     */
    protected void buttonsEnabled(boolean isEnabled) {
	localMuteButton.setEnabled(isEnabled);
	hangUpButton.setEnabled(isEnabled);

    }

    /**
     * Sets mute on or mute off layout in the GUI. This functions does not
     * control the audio mute at all, it only changes the GUI layout.
     *
     * @param isMuted Whether the audio is muted.
     */
    public void setMute(boolean isMuted) {
	/*if (isMuted) {
	    muteCb.setSelected(true);
	}
	else {
	    muteCb.setSelected(false);
	    }*/
    }

    /**
     * This function is executed when a new record in the address panel has
     * been selected by the user.
     *
     * @param name The name value of the record
     * @param address The address value of the record
     */
    public void addressPanelSelectionChanged(String name, String address) {
    	if (sipAddressTextField.isEnabled()) {
    		sipAddressTextField.setText(address);
    	}
    }

    /**
     * Handles all mouse clicks.
     *
     * @param ev The incoming mouse event.
     */

    public void mouseClicked(MouseEvent ev) {
	Object source = ev.getSource();

	if (source == logoPanel) {
	   FundingCreditsDialog fcd =
		new FundingCreditsDialog(this,mainProgram,appSettings);
	    fcd.setVisible(true);
	}
    }


    /**
     * Handles events when the mouse pointer enters an object.
     *
     * @param ev The incoming mouse event.
     */

    public void mouseEntered(MouseEvent ev) {

    }


   /**
     * Handles events when the mouse pointer exits an object.
     *
     * @param ev The incoming mouse event.
     */

    public void mouseExited(MouseEvent ev) {

    }

  /**
     * Handles events when a mouse button is pressed.
     *
     * @param ev The incoming mouse event.
     */

    public void mousePressed(MouseEvent ev) {

    }

/**
     * Handles events when a mouse button is released.
     *
     * @param ev The incoming mouse event.
     */

    public void mouseReleased(MouseEvent ev) {

    }


    /**
     * Handles all actions from the buttons
     *
     * @param ae The incoming action
     */
    public void actionPerformed(ActionEvent ae) {
	Object source = ae.getSource();

	if (source == saveMenuItem) {
	    JFileChooser chooser = new JFileChooser();
	    chooser.setFileFilter(new TxtFileFilter());

	    int returnVal = chooser.showSaveDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		saveTextFile(chooser.getSelectedFile());
	    }

	}

    if (source == alertMenuItem) {
       AlertSettingsDialog alsd = new AlertSettingsDialog(this, appSettings, mainProgram);
       alsd.setVisible(true);
    }


	if (source == exitMenuItem) {
	    closeRequested();
	}

	if (source == audioMenuItem) {
	    AudioSettingsDialog asd =
		new AudioSettingsDialog(this, appSettings, mainProgram);
	    asd.setVisible(true);
	}

	 if (source == videoMenuItem) {
	    VideoSettingsDialog vsd =
		new VideoSettingsDialog(this, appSettings, mainProgram);
	    vsd.setVisible(true);
	}

	if (source == textMenuItem) {
	    TextSettingsDialog tsd = new TextSettingsDialog(this, appSettings, mainProgram);
	    tsd.setVisible(true);
	}

	if (source == fontMenuItem) {
	    T140FontDialog t140fd = new T140FontDialog(appSettings, textPanel, this, mainProgram.getLanguage());
	    t140fd.setVisible(true);
	}

	if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && source == detectMenuItem) {
            //Turn of the local video while detecting new video devices
            mainProgram.destroyAudioVideoPlayer();
	    (new Thread(new DetectThread(this), "Detect devices")).start();
            //The local video is restarted in the DetectThread
	}

	if (source == sipMenuItem) {
	    SipSettingsDialog ssd =
		new SipSettingsDialog(this, appSettings, mainProgram.getLanguage(), mainProgram);
	    ssd.setVisible(true);
	}

        if (source == languageMenuItem) {
            LanguageDialog ld =
        new LanguageDialog(this, appSettings, mainProgram);
        ld.setVisible(true);
}


	if (source == networkMenuItem) {
	    NetworkSettingsDialog nsd =
		new NetworkSettingsDialog(this, appSettings, mainProgram);
	    nsd.setVisible(true);
	}

	if (source == registerMenuItem) {
	    RegisterDialog rd = new RegisterDialog(this, mainProgram, appSettings);
	    rd.setVisible(true);
	}

	if (source == fundingCreditsMenuItem) {
	    FundingCreditsDialog fcd =
		new FundingCreditsDialog(this, mainProgram, appSettings);
	    fcd.setVisible(true);
	}

	if (source == aboutMenuItem) {
            AboutDialog ad=null;
            if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
                ad = new AboutDialog(this, mainProgram, AppConstants.PROGRAM_NAME_TIPCON1);
            }
            else {
                ad = new AboutDialog(this, mainProgram, AppConstants.PROGRAM_NAME);
            }
	    ad.setVisible(true);
	}

	if ((source == callButton ||
	    source == callMenuItem ||
	    source == sipAddressTextField) &&
	    callButton.isEnabled()) {

	    clearTextAreas();

	    // Run the call process in a new thread, this enables GUI to work
	    Thread t = new Thread() {
		    public void run() {
			mainProgram.call(sipAddressTextField.getText());
		    }
		};
            t.setName("Call");
	    t.start();
	}

	if(source == dialMenuItem &&
	   sipAddressTextField.isEnabled()) {
	    sipAddressTextField.requestFocus();
	}

	if ((source == hangupButton ||
	    source == hangupMenuItem) &&
	    hangupButton.isEnabled()) {

              changeInfoLabel("Hanging up ..");
              setPendingGui();
              mainProgram.getCurrentCallProcessor().bye();

	}

	if(source == addressMenuItem) {
	    addressPanel.setFocus();
	}

	if(source == sendMenuItem) {
	    textPanel.getLocalTextArea().setFocus();
	}

	if(source == receiveMenuItem) {
	    textPanel.getRemoteTextArea().setFocus();
	}

	if (source == muteCb) {
	    //mainProgram.localMute(muteCb.isSelected());
	}

	if(source == logoPanel) {
	     FundingCreditsDialog fcd =
		new FundingCreditsDialog(this,mainProgram,appSettings);
	    fcd.setVisible(true);
	}

    }

    private void closeRequested() {
	mainProgram.stop();
    }


    /**
     * Saves the contents of the T140 text areas to the given file.
     *
     *(MOVE SOMEWHERE ELSE?)
     *
     * @param f The file to save to.
     */
    private void saveTextFile(File f) {

	PrintWriter pw = null;

	//For now, use a maximum row length.
	int maxRowLength = 39;

	if (!(f.getPath().toLowerCase(Locale.US).endsWith(".txt") ||
            f.getPath().indexOf('.')!=-1)) {
	    String path = f.getPath();
	    path+=".txt";
	    f = new File(path);
	}

	try {
	    if (!f.createNewFile()) {
		System.out.println("Could not create file.");
		return;
	    }

	} catch (IOException e) {
	    e.printStackTrace();
	}

	if (!f.canWrite()) {
	    System.out.println("File is not writeable.");
	    return;
	}


	try {
	    pw = new PrintWriter(
		 new BufferedWriter(
		 new FileWriter(f)));
	} catch (IOException ioe) {
		logger.throwing(this.getClass().getName(), "saveTextFile", ioe);
	    return;
	}
	if (appSettings.isRealtimePreviewEnabled()) {

            // Changed by Luan Avdulla 2007-04-25.
            // Added a StringBuffer containing the content to be saved/written
            // file. All occurences of '\n' are replaced by an system independent
            // lineseparator in order to be properly presented in for example
            // windows notepad.
            String content = textPanel.getLogTextArea().getText();
            String lineSeparator = System.getProperty("line.separator");
            StringBuffer writeBuffer = new StringBuffer();

            for (int i=0; i<content.length(); i++) {
                char c = content.charAt(i);
                if (c == '\n')
                    writeBuffer.append(lineSeparator);
                else
                    writeBuffer.append(c);
            }

            pw.println(writeBuffer.toString());
            pw.flush();
            pw.close();
        } else {
            String localText  = textPanel.getLocalTextArea().getText();
            int localColumns  = textPanel.getLocalTextArea().getColumns();
            maxRowLength = localColumns;

            String remoteText = textPanel.getRemoteTextArea().getText();

            //System.out.println("local col:  "+localColumns);
            //System.out.println("remote col: "+remoteColumns);

            //Find the widest row in remoteText
            int maxWidth=0;
            /*int col=0;y
              for (int i=0;i<remoteText.length();i++) {
                  if (remoteText.charAt(i)=='\n') {
               if (col>maxWidth) {
		    maxWidth=col;
                }
                col=0;
                   }
                  else {
               col++;
                  }
              }
              //Check the last row
              if (col>maxWidth) {
                  maxWidth=col;
                     }

              if (maxWidth>maxRowLength) {*/
            maxWidth = maxRowLength;
	    //}

            //System.out.println("Max row length: "+maxWidth);

            int localCounter=0;
            String localRow="";
            int remoteCounter=0;
            String remoteRow="";
            String writeBuffer="";
            String partLocalRow;
            String partRemoteRow;
            while(localCounter<=localText.length() ||
                  remoteCounter<=remoteText.length()) {
                writeBuffer="";
                localRow="";
                remoteRow="";
                partRemoteRow="";
                partLocalRow="";

                //Get the next row from local
                while(localCounter<localText.length() &&
                      localText.charAt(localCounter)!='\n') {
                    localRow+=localText.charAt(localCounter);
                    localCounter++;
                }
                localCounter++;

                //Get the next row from remote
                while(remoteCounter<remoteText.length() &&
                      remoteText.charAt(remoteCounter)!='\n') {
                    remoteRow+=remoteText.charAt(remoteCounter);
                    remoteCounter++;
                }
                remoteCounter++;



                while (maxWidth<remoteRow.length() ||
                       maxWidth<localRow.length()) {
                    if(maxWidth<remoteRow.length()) {
                            //get part
                            partRemoteRow = remoteRow.substring(0,maxWidth);
                            remoteRow = remoteRow.substring(maxWidth,
						    remoteRow.length());

                    }
                    else {
                            //Last part. Pad remote row for nice print out
                            partRemoteRow=remoteRow;
                            for (int i=0;i<maxWidth-remoteRow.length();i++) {
                                partRemoteRow+=" ";
                            }
                            remoteRow="";
                        }

                        if(maxWidth<localRow.length()) {
                                //get part
                                partLocalRow = localRow.substring(0,maxWidth);
                                localRow = localRow.substring(maxWidth,
						  localRow.length());
                                //delete part from remote
                        }


                        writeBuffer=partRemoteRow+" | "+partLocalRow;

                        pw.println(writeBuffer);
                    } // end while

                    partRemoteRow=remoteRow;
                    for (int i=0;i<maxWidth-remoteRow.length();i++) {
                        partRemoteRow+=" ";
                    }
                    remoteRow=partRemoteRow;

                    writeBuffer=remoteRow+" | "+localRow;

                    pw.println(writeBuffer);

                }

            }
            pw.close();
            //System.out.println("Save complete");

    }

    private class EstablishedFocusTraversalPolicy extends FocusTraversalPolicy {

	public Component getComponentAfter(Container focusCycleRoot,
					   Component c) {
	    //Size 2, JScrollPane, JPanel
	    Component[] addressPanelComponents      = addressPanel.getComponents();
	    Component[] addressPanelTable           = ((Container)(addressPanelComponents[0])).getComponents();
	    Component[] addressPanelButtons         = ((Container)(addressPanelComponents[1])).getComponents();
	    Component[] selfControlPanelComponents  = selfControlPanel.getComponents();
	    Component[] audioControlPanelComponents = audioControlPanel.getComponents();
	    Component[] localTextPanel              = ((Container)(textPanel.getLocalTextArea())).getComponents();
	    Component[] remoteTextPanel             = ((Container)(textPanel.getRemoteTextArea())).getComponents();
	    Component localText                     = ((JViewport)(localTextPanel[2])).getView();
	    Component remoteText                    = ((JViewport)(remoteTextPanel[2])).getView();


	    if(c.equals(localText)) {
		return hangupButton;
	    } else if(c.equals(hangupButton)) {
	    	if(audioControlPanelComponents!=null &&
	    			audioControlPanelComponents[0]!=null) {
	    		return audioControlPanelComponents[0];
	    	} else if(audioControlPanelComponents!=null && audioControlPanelComponents[1]!=null) {
	    		return audioControlPanelComponents[1];
	    	} else if(selfControlPanelComponents!=null &&
	    			selfControlPanelComponents[0]!=null) {
	    		return selfControlPanelComponents[0];
	    	} else if(selfControlPanelComponents!=null &&
	    			selfControlPanelComponents[1]!=null) {
	    		return selfControlPanelComponents[1];
	    	}
		else {
		    return (((JViewport)(addressPanelTable[0])).getView());
		}
	    } else if(audioControlPanelComponents!=null &&
		      audioControlPanelComponents[0]!=null &&
		      c.equals(audioControlPanelComponents[0])) {
		if(audioControlPanelComponents[1]!=null) {
		    return audioControlPanelComponents[1];
		} else if(selfControlPanelComponents!=null &&
			  selfControlPanelComponents[0]!=null) {
		    return selfControlPanelComponents[0];
		} else if(selfControlPanelComponents!=null &&
			  selfControlPanelComponents[1]!=null) {
		    return selfControlPanelComponents[1];
		} else {
		    return (((JViewport)(addressPanelTable[0])).getView());
		}
	    } else if(audioControlPanelComponents!=null &&
		      audioControlPanelComponents[1]!=null &&
		      c.equals(audioControlPanelComponents[1])) {
		if(selfControlPanelComponents!=null &&
		   selfControlPanelComponents[0]!=null) {
		    return selfControlPanelComponents[0];
		} else if(selfControlPanelComponents!=null &&
			  selfControlPanelComponents[1]!=null) {
		    return selfControlPanelComponents[1];
		} else {
		    return (((JViewport)(addressPanelTable[0])).getView());
		}
	    } else if(selfControlPanelComponents!=null &&
		      selfControlPanelComponents[0]!=null &&
		      c.equals(selfControlPanelComponents[0])) {
		if(selfControlPanelComponents[1]!=null) {
		    return selfControlPanelComponents[1];
		} else {
		    return (((JViewport)(addressPanelTable[0])).getView());
		}
	    } else if(selfControlPanelComponents!=null &&
		      selfControlPanelComponents[1]!=null &&
		      c.equals(selfControlPanelComponents[1])) {
		return (((JViewport)(addressPanelTable[0])).getView());
	    } else if(c.equals(((JViewport)(addressPanelTable[0])).getView())) {
		return addressPanelButtons[0];
	    } else if(c.equals(addressPanelButtons[0])) {
		return addressPanelButtons[1];
	    } else if(c.equals(addressPanelButtons[1])) {
		return addressPanelButtons[2];
	    } else if(c.equals(addressPanelButtons[2])) {
	        return remoteText;
	    }  else if(c.equals(remoteText)) {
		return localText;
	    }
	    return localText;
	}

	public Component getComponentBefore(Container focusCycleRoot,
					   Component c) {

	    //Size 2, JScrollPane, JPanel
	    Component[] addressPanelComponents      = addressPanel.getComponents();
	    Component[] addressPanelTable           = ((Container)(addressPanelComponents[0])).getComponents();
	    Component[] addressPanelButtons         = ((Container)(addressPanelComponents[1])).getComponents();
	    Component[] selfControlPanelComponents  = selfControlPanel.getComponents();
	    Component[] audioControlPanelComponents = audioControlPanel.getComponents();
	    Component[] localTextPanel              = ((Container)(textPanel.getLocalTextArea())).getComponents();
	    Component[] remoteTextPanel             = ((Container)(textPanel.getRemoteTextArea())).getComponents();
	    Component localText                     = ((JViewport)(localTextPanel[2])).getView();
	    Component remoteText                    = ((JViewport)(remoteTextPanel[2])).getView();


	    if(c.equals(localText)) {
		System.out.println("Going remote");
		remoteText.setFocusable(true);
		return remoteText;
	    } else if(c.equals(remoteText)) {
		return addressPanelButtons[2];
	    } else if(c.equals(addressPanelButtons[2])) {
		return addressPanelButtons[1];
	    } else if(c.equals(addressPanelButtons[1])) {
		return addressPanelButtons[0];
	    } else if(c.equals(addressPanelButtons[0])) {
		return (((JViewport)(addressPanelTable[0])).getView());
	    } else if(c.equals(((JViewport)(addressPanelTable[0])).getView())) {
		if(selfControlPanelComponents!=null &&
		   selfControlPanelComponents[1]!=null) {
		    return selfControlPanelComponents[1];
		} else if(selfControlPanelComponents!=null &&
			  selfControlPanelComponents[0]!=null) {
		    return selfControlPanelComponents[0];
		} else if(audioControlPanelComponents!=null &&
			  audioControlPanelComponents[1]!=null) {
		    return audioControlPanelComponents[1];
		}  else if(audioControlPanelComponents!=null &&
			   audioControlPanelComponents[0]!=null) {
		    return audioControlPanelComponents[0];
		} else {
		    return hangupButton;
		}
	    } else if(selfControlPanelComponents!=null &&
		      selfControlPanelComponents[1]!=null &&
		      c.equals(selfControlPanelComponents[1])) {
		if(selfControlPanelComponents[0]!=null) {
		    return selfControlPanelComponents[0];
		} else if(audioControlPanelComponents!=null &&
			  audioControlPanelComponents[1]!=null) {
		    return audioControlPanelComponents[1];
		} else if(audioControlPanelComponents!=null &&
			  audioControlPanelComponents[0]!=null) {
		    return audioControlPanelComponents[0];
		} else {
		   return hangupButton;
		}
	    } else if(selfControlPanelComponents!=null &&
		      selfControlPanelComponents[0]!=null &&
		      c.equals(selfControlPanelComponents[0])) {
		if(audioControlPanelComponents!=null &&
		   audioControlPanelComponents[1]!=null) {
		    return audioControlPanelComponents[1];
		} else if(audioControlPanelComponents!=null &&
			  audioControlPanelComponents[0]!=null) {
		    return audioControlPanelComponents[0];
		} else {
		    return hangupButton;
		}
	    } else if(audioControlPanelComponents!=null &&
		      audioControlPanelComponents[1]!=null &&
		      c.equals(audioControlPanelComponents[1])) {
		if(audioControlPanelComponents[0]!=null) {
		    return audioControlPanelComponents[0];
		} else {
		    return hangupButton;
		}
	    } else if(audioControlPanelComponents!=null &&
		      audioControlPanelComponents[0]!=null &&
		      c.equals(audioControlPanelComponents[0])) {
		return hangupButton;
	    } else if(c.equals(hangupButton)) {
		return localText;
	    }
	    return localText;
	}


	public Component getDefaultComponent(Container focusCycleRoot) {
	    Component[] localTextPanel              = ((Container)(textPanel.getLocalTextArea())).getComponents();
	    Component localText                     = ((JViewport)(localTextPanel[2])).getView();
	    return localText;
	}

	public Component getLastComponent(Container focusCycleRoot) {
	    Component[] remoteTextPanel             = ((Container)(textPanel.getRemoteTextArea())).getComponents();
	    Component remoteText                    = ((JViewport)(remoteTextPanel[2])).getView();
            return remoteText;
        }

        public Component getFirstComponent(Container focusCycleRoot) {
	    Component[] localTextPanel              = ((Container)(textPanel.getLocalTextArea())).getComponents();
	    Component localText                     = ((JViewport)(localTextPanel[2])).getView();
            return localText;
        }


    }



    /**
     * This class regulates the tab order when the gui si in terminated state.
     * Maybe not the best solution but it works...
     * Please note that this class has to be changed if the GUI is changed!
     *
     * @author Erik Zetterstrom
     */
    private class TerminatedFocusTraversalPolicy extends FocusTraversalPolicy {

	public Component getComponentAfter(Container focusCycleRoot,
					   Component c) {
	    //Size 2, JScrollPane, JPanel
	    Component[] addressPanelComponents      = addressPanel.getComponents();
	    Component[] addressPanelTable           = ((Container)(addressPanelComponents[0])).getComponents();
	    Component[] addressPanelButtons         = ((Container)(addressPanelComponents[1])).getComponents();
	    Component[] localTextPanel              = ((Container)(textPanel.getLocalTextArea())).getComponents();
	    Component[] remoteTextPanel             = ((Container)(textPanel.getRemoteTextArea())).getComponents();
	    Component localText                     = ((JViewport)(localTextPanel[2])).getView();
	    Component remoteText                    = ((JViewport)(remoteTextPanel[2])).getView();

	    if(c.equals(sipAddressTextField)) {
		return callButton;
	    } else if(c.equals(callButton)) {
		/*return hangupButton;
	    } else if(c.equals(hangupButton)) {
		return audioControlPanelComponents[0];
	    } else if(c.equals(audioControlPanelComponents[0])) {
		return audioControlPanelComponents[1];
	    } else if(c.equals(audioControlPanelComponents[1])) {
		return selfControlPanelComponents[0];
	    } else if(c.equals(selfControlPanelComponents[0])) {
		return selfControlPanelComponents[1];
		} else if(c.equals(audioControlPanelComponents[1])) {*/
		return (((JViewport)(addressPanelTable[0])).getView());
	    } else if(c.equals(((JViewport)(addressPanelTable[0])).getView())) {
		return addressPanelButtons[0];
	    } else if(c.equals(addressPanelButtons[0])) {
		return addressPanelButtons[1];
	    } else if(c.equals(addressPanelButtons[1])) {
		return addressPanelButtons[2];
	    } else if(c.equals(addressPanelButtons[2])) {
	        return remoteText;
	    }  else if(c.equals(remoteText)) {
		return localText;
	    } else if(c.equals(localText)) {
		return sipAddressTextField;
	    }
	    return sipAddressTextField;
	}

	public Component getComponentBefore(Container focusCycleRoot,
					    Component c) {

	    Component[] addressPanelComponents      = addressPanel.getComponents();
	    Component[] addressPanelTable           = ((Container)(addressPanelComponents[0])).getComponents();
	    Component[] addressPanelButtons         = ((Container)(addressPanelComponents[1])).getComponents();
	    Component[] localTextPanel              = ((Container)(textPanel.getLocalTextArea())).getComponents();
	    Component[] remoteTextPanel             = ((Container)(textPanel.getRemoteTextArea())).getComponents();
	    Component localText                     = ((JViewport)(localTextPanel[2])).getView();
	    Component remoteText                    = ((JViewport)(remoteTextPanel[2])).getView();

	    if(c.equals(sipAddressTextField)) {
		return localText;
	    } else if(c.equals(localText)) {
		return remoteText;
	    }  else if(c.equals(remoteText)) {
		return addressPanelButtons[2];
	    } else if(c.equals(addressPanelButtons[2])) {
		return addressPanelButtons[1];
	    } else if(c.equals(addressPanelButtons[1])) {
		return addressPanelButtons[0];
	    } else if(c.equals(addressPanelButtons[0])) {
		return (((JViewport)(addressPanelTable[0])).getView());
	    } else if(c.equals(((JViewport)(addressPanelTable[0])).getView())) {
		return callButton;
	    } else if(c.equals(callButton)) {
		return sipAddressTextField;
	    }
	    return sipAddressTextField;
	}

	public Component getDefaultComponent(Container focusCycleRoot) {
	    return sipAddressTextField;
	}

	public Component getLastComponent(Container focusCycleRoot) {
	    Component[] localTextPanel              = ((Container)(textPanel.getLocalTextArea())).getComponents();
	    Component localText                     = ((JViewport)(localTextPanel[2])).getView();
            return localText;
        }

        public Component getFirstComponent(Container focusCycleRoot) {
            return sipAddressTextField;
        }



    }

    /**
     * This class is used by JFileChooser to display only .txt files.
     *
     * @author Erik Zetterstrom, Omnitor AB
     */
    private class TxtFileFilter extends FileFilter {

	/**
	 * Checks if the given file is accepted by this file filter.
	 * Accept only non hidden fiels with a suffix of txt.
	 *
	 * @return True if the file is accepted, otherwise false.
	 */
	public boolean accept(File f) {
	    if (f.isDirectory()) {
		return true;
	    }

	    String name = f.getName().toLowerCase(Locale.US);
	    return name.endsWith("txt");
	}


	/**
	 * Return the description of this file filter.
	 *
	 * @return The description.
	 */
	public String  getDescription() {
	    return ("Text documents (*.txt)");
	}
    }


    /**
     * A class used for detecting media. Since that detection is run in a
     * separate class (due to GUI repainting issues), the code is gathered into
     * this class.
     *
     * @author Andreas Piirimets, Omnitor AB
     */
    private class DetectThread implements Runnable {

	JFrame parentFrame;

	/**
	 * Initializes the class.
	 *
	 * @param parent The parent frame
	 */
	public DetectThread(JFrame parent) {
	    parentFrame = parent;
	}

	/**
	 * Runs the thread.
	 *
	 */
	public void run() {

	    ThreadedDialog td =
		DialogFactory.showWaitDialog(
                    language.getProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.DETECTING"),
		 DialogFactory.NO_ABORT_BUTTON);

	    int nbrOfAudioDevices = mainProgram.getNbrOfAudioDevices();
	    int nbrOfVideoDevices = mainProgram.getNbrOfVideoDevices();
	    String infoStr = null;

	    mainProgram.detectDevices();
	    mainProgram.initializeSupportedMedia();

	    if (nbrOfAudioDevices != mainProgram.getNbrOfAudioDevices()) {
		mainProgram.restoreSavedAudioSettings();
		infoStr = language.getProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.AUDIO_SETUP_CHANGED");
	    }

	     if ((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) && nbrOfVideoDevices != mainProgram.getNbrOfVideoDevices()) {
		mainProgram.restoreSavedVideoSettings();
		if (infoStr != null) {
                    infoStr = language.getProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.AUDIO_AND_VIDEO_SETUP_CHANGED");
		}
		else {
                    infoStr = language.getProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.VIDEO_SETUP_CHANGED");
		}
	    }

	    if (infoStr == null) {
		infoStr = language.getProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.NO_NEW_DEVICES");
	    }

	    settingsPanel.updateDeviceGui();
	    repaint();

            //restart the local video
            if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
                mainProgram.restartVideo();
            }
	    td.dispose();

	    DialogFactory.showInformationMessageDialog(
                   language.getProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.DETECTION_COMPLETE"),
                   infoStr);
	}
    }

    /**
     * Sets "Register OK" status in the status panel.
     *
     * @param regAddress The SIP address that is registered.
     */
    public void setRegOk(String regAddress) {
        stopDetectingMove();
	registrarLabel.setText(
                language.getProperty("se.omnitor.tipcon1.ProgramWindow.REGISTERED"));
	registrarLabel.setForeground(SystemColor.textText);
	//registrarLabel.setToolTipText("Registered as " + regAddress);
	registrarLabel.setIcon(userActiveIcon);
	/*
	registrarLabel.setHorizontalAlignment(SwingConstants.LEFT);
	registrarLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	*/
    }

    /**
     * Set "Registration failed" status in the status label.
     *
     * @param regAddress The SIP address that failed to register.
     */
    public void setRegFail(String regAddress) {
        stopDetectingMove();
	Dimension d = registrarLabel.getPreferredSize();
	registrarLabel.setText(
                language.getProperty("se.omnitor.tipcon1.ProgramWindow.FAILED"));
        registrarLabel.setPreferredSize(d);
	registrarLabel.setForeground(Color.RED);
	//registrarLabel.setToolTipText("Failed to register " + regAddress);
	registrarLabel.setIcon(userInactiveIcon);
	/*
	registrarLabel.setHorizontalAlignment(SwingConstants.LEFT);
	registrarLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	*/
    }

    public void setRegDetecting(String text) {
        stopDetectingMove();
        registrarLabel.setText("");
        registrarLabel.setForeground(SystemColor.textText);
        registrarLabel.setIcon(userInactiveIcon);
        startDetectingMove(text);
    }

    /**
     * Sets "Not registered" status in the status label.
     *
     */
    public void setRegNa() {
        stopDetectingMove();
	registrarLabel.setText(
                language.getProperty("se.omnitor.tipcon1.ProgramWindow.NOT_REGISTRERED_WITH_SIPSERVER"));
	registrarLabel.setForeground(SystemColor.textInactiveText);
	registrarLabel.setIcon(userInactiveIcon);
	/*
	registrarLabel.setHorizontalAlignment(SwingConstants.LEFT);
	registrarLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	*/
    }

    public void setRegPending() {
        stopDetectingMove();
    }

    public void setRegPartlyOk() {
        stopDetectingMove();
	setRegFail("");
    }


    private void resetRemoteVideoPanel() {
	remoteVideoPanel.removeAll();

	/*remove prev
	JLabel noRemoteVideoLabel = new JLabel("No remote video available");
	noRemoteVideoLabel.setForeground(SystemColor.controlShadow);
	noRemoteVideoLabel.setBackground(Color.YELLOW);
	noRemoteVideoLabel.setHorizontalAlignment(SwingConstants.CENTER);
	remoteVideoPanel.setLayout(new GridLayout(1, 0, 0, 0));
	remoteVideoPanel.setBorder(BorderFactory.createLoweredBevelBorder());
	remoteVideoPanel.add(noRemoteVideoLabel);
	videoTextPanel.add(remoteVideoPanel, BorderLayout.CENTER);
	*/

	JLabel noRemoteVideoLabel =
                   new JLabel(language.getProperty("se.omnitor.tipcon1.ProgramWindow.NO_REMOTE_VIDEO"));
	noRemoteVideoLabel.setForeground(SystemColor.controlShadow);
	noRemoteVideoLabel.setHorizontalAlignment(SwingConstants.CENTER);

	/*removed prev
	JPanel fillPanel = new JPanel(new GridBagLayout());
	fillPanel.setLayout(new GridBagLayout());
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.anchor = GridBagConstraints.CENTER;
	fillPanel.add(rp, gbc);
	*/

	remoteVideoPanel.setLayout(new GridLayout(1, 0, 0, 0));
	remoteVideoPanel.setBorder(BorderFactory.createLoweredBevelBorder());
	remoteVideoPanel.add(noRemoteVideoLabel);
    }

    /**
     * Resets the local video panel
     *
     */
    public void resetLocalVideoPanel() {

	localVideoPanel = noLocalVideoPanel;

    }

    /**
     * Shows a password dialog for registrations.
     *
     * @param title The dialog's title
     * @param infoText The information text to show
     *
     * @return The answer to the question. null if cancel was pressed.
     */
    public String showPasswordDialog(String title, String infoText) {

	JPasswordField pf = new JPasswordField();
	Object[] message = new Object[] {infoText, pf};
	Object[] options = new String[] {"OK", "Cancel"};
	JOptionPane op = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE,
					 JOptionPane.OK_CANCEL_OPTION, null,
					 options);
	JDialog dialog = op.createDialog(this, title);
	dialog.setVisible(true);

	String value = (String)op.getValue();

	if (value == null) {
	    return null;
	}

	if (value.equals("Cancel")) {
	    return null;
	}

	return String.valueOf(pf.getPassword());
    }

    /**
     * Indicates the name of the remote user, this is to be set somewhere in
     * the gui.
     *
     * @param name The name of the remote user.
     */
    public void setRemoteUserInfo(String name) {
        if (AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            setTitle(AppConstants.PROGRAM_NAME_TIPCON1 +
                             language.getProperty("se.omnitor.tipcon1.ProgramWindow.CONVERSATION_WITH") +
                             name);

        } else {
            setTitle(AppConstants.PROGRAM_NAME +
                     language.getProperty("se.omnitor.tipcon1.ProgramWindow.CONVERSATION_WITH") +
                     name);
        }
    }
    public void setMediaStatus(boolean useAudio, boolean useText) {
    }

    // AddressPanelListener
    public void handleDoubleClick(String name, String address) {
	clearTextAreas();

	final String a = address;

	// Run the call process in a new thread, this enables GUI to work
	Thread t = new Thread() {
		public void run() {
		    mainProgram.call(a);
		}
	    };
        t.setName("Call");
	t.start();
    }

    public void setAudioTxControlPanel(Component c) {

	/*
	GridBagConstraints gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.insets = new Insets(OBJ_SPACE, OBJ_SPACE,
					       OBJ_SPACE, OBJ_SPACE);
	gridBagConstraints.gridx = 0;
	gridBagConstraints.gridy = 1;
	gridBagConstraints.gridwidth = 3;
	gridBagConstraints.weightx = 1.0;
	audioControlPanel.add(c, gridBagConstraints);*/
    }

    private void startDetectingMove(String text) {
        detectingMove = new DetectingMove(text);
        Thread t = new Thread(detectingMove);
        t.start();
    }

    private void stopDetectingMove() {
        if (detectingMove != null) {
            detectingMove.stop();
            detectingMove = null;
        }
    }

    class DetectingMove implements Runnable {
        boolean isRunning = false;
        String labelText;

        public DetectingMove(String text) {
            labelText = text;
            isRunning = true;
        }
        public void run() {

            while (isRunning) {
                try {
                    registrarLabel.setText(labelText + " .");
                    Thread.sleep(1000);
                    if (!isRunning) continue;
                    registrarLabel.setText(labelText + " ..");
                    Thread.sleep(1000);
                    if (!isRunning) continue;
                    registrarLabel.setText(labelText + " ...");
                    Thread.sleep(1000);
                }
                catch (InterruptedException ie) {
                    // Ignore
                }
            }
        }

        public void stop() {
            isRunning = false;
        }
    }


    
}
