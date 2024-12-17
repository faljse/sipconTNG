/*
 * Copyright (c) 2004-2008 Omnitor AB (www.omnitor.se)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package se.omnitor.tipcon1.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//import se.omnitor.sip.SipController;
//import se.omnitor.sip.MessageErrorException;
//import se.omnitor.sip.UserErrorException;
//import se.omnitor.sip.SipErrorException;

import se.omnitor.tipcon1.AppConstants;
import se.omnitor.tipcon1.AppController;

/**
 * A modal settings dialog window. This settings dialog uses other dialogs
 * to show video, audio, and text settings (AudioVideoSettingsDialog,
 * AudioSettingsDialog, VideoSettingsDialog and TextSettingsDialog). The
 * settingsdialog saves all settings to the main program only if the user
 * clicks "OK". Regardless if the user clicks "OK" or "Cancel", the current
 * settings in the main program are always re-applied.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class SettingsPanel extends JPanel implements ChangeListener,
						     ItemListener,
						     ActionListener,
						     KeyListener {

    private boolean isGettingSettings = false;

    // GUI elements
    private JCheckBox audioCheckbox;
    private JTextField fullNameTextField;
    private JTextField sipAddressTextField;
    private JCheckBox textCheckbox;
    private JCheckBox videoCheckbox;
    private JButton applyButton;

    private AppController settings;

    /**
     * Initializes the dialog. Saves the variables layouts the GUI and centers
     * the window.
     *
     * @param settings The application controller that holds all settings
     * @param owner The parent window of this dialog.
     */
    public SettingsPanel(AppController settings,
                          JFrame owner) {

        super();


        this.settings = settings;

	initComponents();

    }

    /**
     * Saves the text in the text fields. This is used to determine if the
     * text has been changed or not when the focus is lost.
     *
     * @param fe The incoming event.
     */
    /*
    public void focusGained(FocusEvent fe) {
	tempFullNameContents = fullNameTextField.getText();
	tempSipAddressContents = sipAddressTextField.getText();
    }
    */

    /**
     * Takes care of actions when the user clicks on the different buttons.
     *
     * @param fe The incoming event.
     */
    /*
    public void focusLost(FocusEvent fe) {

	//processTextFields();

    }
    */

    /**
     * Takes care of ENTER in a text field.
     *
     * @param ae The incoming event.
     */
    public void actionPerformed(ActionEvent ae) {

	processTextFields();

    }

    private void processTextFields() {

	// Prepare the SIP address
	String saddr = sipAddressTextField.getText().trim();

	if (!saddr.equals("")) {
	    if (saddr.split("@").length != 2) {
		DialogFactory.showErrorMessageDialog
		    ("You have entered an invalid SIP address! \n\n" +
		     "Syntax: \n" +
		     "user@domain.com or \n" +
		     "user@domain.com:port");

		//tf.requestFocus();

		return;
	    }

	}

	// Save user info
	//settings.setUser(fullNameTextField.getText().trim(), saddr, null);
	//settings.saveData();

	applyButton.setEnabled(false);

    }

    /**
     * Closes the dialog and deploys the settings in the main program.
     *
     */
    /*
    private void closeDialog() {
	settings.deploySettings();
    }
    */

    /**
     * Gets the settings from the main program.
     *
     */
    private void getSettings() {

	isGettingSettings = true;

	/*
	String sipAddress = settings.getUserSipAddressSetting();
	if (sipAddress.startsWith("unknown@" + settings.getLocalIpAddress())) {
	    sipAddress = "";
	}
	else if (sipAddress.startsWith("unknown@" +
				       settings.getLocalHostAddress())) {
	    sipAddress = "";
	}
	*/
	//String sipAddress = settings.getLocalSipUserSipAddress();
	//if (sipAddress == null) {
	//    sipAddress = "";
	//}

	//sipAddressTextField.setText(sipAddress);
	//fullNameTextField.setText(settings.getUserRealName());
	//fullNameTextField.setText(settings.getLocalSipUserRealName());

	textCheckbox.setSelected(settings.isTextActivated());
        videoCheckbox.setSelected(settings.isVideoActivated());
	audioCheckbox.setSelected(settings.isAudioActivated() &&
				  settings.getNbrOfAudioDevices() > 0);

	updateDeviceGui();

	isGettingSettings = false;
    }

    /**
     * Updates the device GUI. This should be done when a detection process
     * has ended and media types may have been added or removed.
     *
     */
    public void updateDeviceGui() {
	int nbrOfDevices;

	textCheckbox.setEnabled(true);

	/*
	nbrOfDevices = settings.getNbrOfVideoDevices();
	if (nbrOfDevices > 0) {
	    videoCheckbox.setEnabled(true);
	    videoSetupButton.setEnabled(true);
	}
	else {
	    videoCheckbox.setState(false);
	    videoCheckbox.setEnabled(false);
	    videoSetupButton.setEnabled(false);
	}
	*/

	nbrOfDevices = settings.getNbrOfAudioDevices();
	if (nbrOfDevices > 0) {
	    audioCheckbox.setEnabled(true);
	}
	else {
	    audioCheckbox.setSelected(false);
	    audioCheckbox.setEnabled(false);
	}

    }

    /**
     * Initializes the GUI components.
     *
     */
    private void initComponents() {
        fullNameTextField = new JTextField();
        sipAddressTextField = new JTextField();
        textCheckbox = new JCheckBox();
        videoCheckbox = new JCheckBox();
        audioCheckbox = new JCheckBox();
	applyButton = new JButton();

	setLayout(new GridBagLayout());
	GridBagConstraints gridBagConstraints;

	JPanel userPanel = new JPanel(new BorderLayout());

	JPanel userPanelHeading = new JPanel(new BorderLayout(3, 0));
        userPanelHeading.add(new JLabel("User"), java.awt.BorderLayout.WEST);
        userPanelHeading.add(new SeparatorPanel(),
			     java.awt.BorderLayout.CENTER);
	//userPanel.add(userPanelHeading, BorderLayout.NORTH);
	//userPanel.add(new ImagePanel(AppConstants.USER_IMAGE_URL, 5, 3), BorderLayout.WEST);

	JPanel userSettingsPanel = new JPanel(new GridBagLayout());
	GridBagConstraints gbc;
	JPanel userLabels = new JPanel(new GridLayout(2, 0));
	userLabels.add(new JLabel("Full name:"));
	userLabels.add(new JLabel("SIP address:"));
	gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.fill = GridBagConstraints.BOTH;
	userSettingsPanel.add(userLabels, gbc);

	JPanel userTextFields = new JPanel(new GridLayout(2, 0));
	fullNameTextField.addActionListener(this);
	fullNameTextField.addKeyListener(this);
	userTextFields.add(fullNameTextField);
	sipAddressTextField.addActionListener(this);
	sipAddressTextField.addKeyListener(this);
	userTextFields.add(sipAddressTextField);
	gbc = new GridBagConstraints();
	gbc.gridx = 1;
	gbc.gridy = 0;
	gbc.weightx = 1.0;
	gbc.fill = GridBagConstraints.BOTH;
	//userSettingsPanel.add(userTextFields, gbc);

	applyButton.setText("Apply");
	applyButton.setEnabled(false);
	applyButton.addActionListener(this);
	applyButton.setMargin(new Insets(0, 10, 0, 10));
	gbc = new GridBagConstraints();
	gbc.gridx = 1;
	gbc.gridy = 1;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.insets = new Insets(2, 0, 0, 0);
	gbc.fill = GridBagConstraints.NONE;
	//userSettingsPanel.add(applyButton, gbc);

	//userPanel.add(userSettingsPanel, BorderLayout.CENTER);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
	gridBagConstraints.insets = new Insets(5, 5, 15, 5);
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	add(userPanel, gridBagConstraints);


	JPanel mediaPanelHeading = new JPanel(new BorderLayout(3, 0));
        mediaPanelHeading.add(new JLabel("Media"), java.awt.BorderLayout.WEST);
        mediaPanelHeading.add(new SeparatorPanel(),
			      java.awt.BorderLayout.CENTER);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
	gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	add(mediaPanelHeading, gridBagConstraints);


	JPanel mediaPanel = new JPanel(new GridLayout(2, 3));
	mediaPanel.add(new ImagePanel(AppConstants.AUDIO_IMAGE_URL));
        if (AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            mediaPanel.add(new ImagePanel(AppConstants.VIDEO_IMAGE_URL));
        }
	mediaPanel.add(new ImagePanel(AppConstants.TEXT_IMAGE_URL));
	//audioCheckbox.setText("Use audio");
	audioCheckbox.addItemListener(this);
	mediaPanel.add(audioCheckbox);
	//videoCheckbox.setText("Use video");
        if (AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            videoCheckbox.addItemListener(this);
            mediaPanel.add(videoCheckbox);
        }
	//textCheckbox.setText("Use text");
        //textCheckbox.
	textCheckbox.addItemListener(this);
	mediaPanel.add(textCheckbox);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
	gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	add(mediaPanel, gridBagConstraints);

	// Add padding to the bottom
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weighty = 1.0;
	add(new JPanel(), gridBagConstraints);


	getSettings();
    }

    public void stateChanged(ChangeEvent e) {

	//getSettings();

    }

    public void itemStateChanged(ItemEvent e) {

	Object item = e.getItem();

	if (item == audioCheckbox) {
	    settings.setAudioActivate(audioCheckbox.isSelected());
	}
	else  if (item == videoCheckbox) {
	    if (!isGettingSettings) {
                settings.destroyAudioVideoPlayer();
		 settings.setVideoActivate(videoCheckbox.isSelected());
		 settings.deployVideoSettings();
	    }
	}
	else if (item == textCheckbox) {
	    settings.setTextActivate(textCheckbox.isSelected());
	}

	//settings.deploySdpSettings();

    }

    public void keyTyped(KeyEvent ke) {
    }

    public void keyReleased(KeyEvent ke) {
    }

    public void keyPressed(KeyEvent ke) {
	applyButton.setEnabled(true);
    }

}
