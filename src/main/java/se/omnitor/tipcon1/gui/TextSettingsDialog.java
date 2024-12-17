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

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import se.omnitor.tipcon1.AppConstants;
import se.omnitor.tipcon1.AppController;
import se.omnitor.tipcon1.AppSettings;

/**
 * This is a modal dialog that enables the user to change the text settings.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class TextSettingsDialog extends JDialog implements ActionListener,
							   KeyListener, ChangeListener {

    private AppController appController;
    private AppSettings appSettings;
    private Properties language;

    // GUI components
    private JButton cancel;
    private ImagePanel logo;
    private JButton ok;
    private JButton defaultButton;
    private JFrame parent;
    private JCheckBox useRTPV;
    private JCheckBox useRtpvTimer;
    private JTextField buffertext;
    //private JTextField porttext;
    private JTextField redtext;
    private JTextField cpstext;
    private JRadioButton useCR;
    private JRadioButton useRealTime;
    private ButtonGroup sendOnCRRadioButtonGroup;


    /**
     * Initializes the text settings dialog. Fetches the settings, initializes
     * the GUI and centers the dialog.
     *
     * @param owner The parent dialog
     * @param controller The application controller that holds all settings
     */
    public TextSettingsDialog(JFrame owner, AppSettings appSettings, AppController controller) {
	super(owner,
	      controller.getLanguage().getProperty("se.omnitor.tipcon1." +
						   "gui.TextSettingsDialog." +
						   "TEXT_SETTINGS"),
	      true);

	this.appController = controller;
        this.appSettings = appSettings;
	language = controller.getLanguage();

        setResizable(false);

	initComponents();

	setLocation(GuiToolkit.getCenterX() - getWidth()/2,
		    GuiToolkit.getCenterY() - getHeight()/2);

    }

    /**
     * Fetches the settings from the main program.
     *
     */
    private void fetchSettings() {

	int cps = appController.getMaxIncomingCps();
	if (cps == 0) {
	    cpstext.setText("");
	}
	else {
	    cpstext.setText("" + cps);
	}
        //porttext.setText("" + appController.getLocalTextPort());
        buffertext.setText("" + appSettings.getBufferTime());
        redtext.setText("" + appSettings.getRedundantRtpGenerations());
	useRTPV.setSelected(appSettings.isRealtimePreviewEnabled());
	useRtpvTimer.setSelected(appSettings.isRealtimePreviewTimerEnabled());
		if (useRTPV.isSelected()) {
			useRtpvTimer.setEnabled(true);			
		}
		else {
			useRtpvTimer.setEnabled(false);
		}
    }

    /**
     * Saves the settings. If an error in the user input was found, an error
     * message dialog will be shown and the function will return immediately.
     *
     * @return True if OK, false if user input is not ok
     */
    private boolean saveSettings() {
	int cps;
	int bufferTime;
	int redGen;

	/* save the checkbox */
	appController.changeLayout(useRTPV.isSelected());
	appController.guiPrepare();
	appController.setupT140panel();
	appSettings.setRealtimePreview(useRTPV.isSelected());
	appSettings.setRealtimePreviewTimerEnabled(useRtpvTimer.isSelected());


	/*
	 * Prepare the silent time
	 */
	if (cpstext.getText().trim().length() == 0) {
	    cps = 0;
	}
	else {
	    try {
		cps = Integer.parseInt(cpstext.getText().trim());
	    }
	    catch (Exception e) {
		DialogFactory.showErrorMessageDialog
		    (language.getProperty("se.omnitor.tipcon1.gui." +
					  "TextSettingsDialog.CPS_DIGITS"));
		return false;
	    }
	}
	if (cps < 0) {
	    DialogFactory.showErrorMessageDialog
		(language.getProperty("se.omnitor.tipcon1.gui." +
				      "TextSettingsDialog.DIGITS_ONLY"));
	    return false;
	}

	/*
	 * Prepare the port
	 */
        /*
	if (porttext.getText().trim().length() == 0) {
	    port = 0;
	    for (cnt=5060; cnt<65535; cnt++) {
		if (appController.getLocalTextPort() == cnt) {
		    port = cnt;
		    break;
		}

		if (NetToolkit.portIsAvailable(cnt)) {
		    port = cnt;
		    break;
		}
	    }
	}
	else {
	    try {
		port = Integer.parseInt(porttext.getText().trim());
	    }
	    catch (Exception e) {
		DialogFactory.showErrorMessageDialog
		    (language.getProperty("se.omnitor.tipcon1.gui." +
					  "TextSettingsDialog.PORT_DIGITS"));
		return false;
	    }
	}
	if (port <= 0) {
	    DialogFactory.showErrorMessageDialog
		(language.getProperty("se.omnitor.tipcon1.gui." +
				      "TextSettingsDialog.PORT_RANGE"));
	    return false;
	}
	if (port > 65535) {
	    DialogFactory.showErrorMessageDialog
		(language.getProperty("se.omnitor.tipcon1.gui." +
				      "TextSettingsDialog.PORT_RANGE"));
	    return false;
	}
       */

	/*
	 * Prepare the buffer time
	 */
	if (buffertext.getText().trim().length() == 0) {
	    bufferTime = 0;
	}
	else {
	    try {
		bufferTime = Integer.parseInt(buffertext.getText().trim());
	    }
	    catch (Exception e) {
		DialogFactory.showErrorMessageDialog
		    (language.getProperty("se.omnitor.tipcon1.gui." +
					  "TextSettingsDialog.BT_DIGITS"));
		return false;
	    }
	}
	if (bufferTime < 0) {
	    DialogFactory.showErrorMessageDialog
		(language.getProperty("se.omnitor.tipcon1.gui." +
				      "TextSettingsDialog.BT_NEGATIVE"));
	    return false;
	}

	/*
	 * Prepare the redundant generations
	 */
	if (redtext.getText().trim().length() == 0) {
	    redGen = 0;
	}
	else {
	    try {
		redGen = Integer.parseInt(redtext.getText().trim());
	    }
	    catch (Exception e) {
		DialogFactory.showErrorMessageDialog
		    (language.getProperty("se.omnitor.tipcon1.gui." +
					  "TextSettingsDialog.RG_DIGITS"));
		return false;
	    }
	}
	if (redGen < 0) {
	    DialogFactory.showErrorMessageDialog
		(language.getProperty("se.omnitor.tipcon1.gui." +
				      "TextSettingsDialog.RG_NEGATIVE"));
	    return false;
	}

	appController.setMaxIncomingCps(cps);
	//appController.setLocalTextPort(port);
	appSettings.setRedundantRtpGenerations(redGen);
        if(useCR.isSelected()) {
            appSettings.setSendOnCR(true);
            appSettings.setBufferTime(100);
        }
        else {
            appSettings.setSendOnCR(false);
            appSettings.setBufferTime(bufferTime);
            appController.setTextBufferTime(bufferTime);
        }

	appController.deploySdpSettings(true);
	appSettings.save();

	return true;
    }

    /**
     * Disposes the dialog.
     *
     */
    private void closeDialog() {
	dispose();
    }

    /**
     * Takes care of the actions when the user clicks on "OK" or "Cancel"
     * buttons.
     *
     * @param ae The incoming action event.
     */
    public void actionPerformed(ActionEvent ae) {
	if (ae.getActionCommand().equals("ok")) {
	    if (saveSettings()) {
		closeDialog();
	    }
	}

	if (ae.getActionCommand().equals("cancel")) {
	    closeDialog();
	}
    	if (ae.getActionCommand().equals("default")) {
    		int result =
    		    JOptionPane.showConfirmDialog
    		    (parent,
    		     language.getProperty("se.omnitor.tipcon1.gui." +
    					  "SipSettingsDialog.RESTORE_DEFAULT_OPTION"),
    		     language.getProperty("se.omnitor.tipcon1.gui." +
    					  "SipSettingsDialog.RESTORE_DEFAULT"),
    		     JOptionPane.YES_NO_OPTION);
    		if (result == JOptionPane.YES_OPTION) {
    			useCR.setSelected(false);
                useRealTime.setSelected(true);
                buffertext.setEnabled(true);
                buffertext.setText("300");
                cpstext.setText("");
                redtext.setText("2");
                useRTPV.setEnabled(true);
    			useRTPV.setSelected(true);
    			useRtpvTimer.setEnabled(false);
    			useRtpvTimer.setSelected(false);
    		}
    	}

        if (ae.getActionCommand().equals("useCR")) {
            buffertext.setEnabled(false);
        }

        if (ae.getActionCommand().equals("realTime")) {
            buffertext.setEnabled(true);
        }
    }

    /**
     * Initializes the GUI.
     *
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        logo = new ImagePanel(AppConstants.TEXT_IMAGE_URL);
        ok = new JButton();
        cancel = new JButton();
        defaultButton = new JButton();
        useRTPV = new JCheckBox();
        useRtpvTimer = new JCheckBox();
        //porttext = new JTextField("00000000");
        buffertext = new JTextField();
        cpstext = new JTextField();
        redtext = new JTextField();
        sendOnCRRadioButtonGroup = new ButtonGroup();

        useCR = new JRadioButton(language.getProperty
            ("se.omnitor.tipcon1.gui.TextSettingsDialog.SEND_ON_RETURN"));

        useRealTime = new JRadioButton(language.getProperty
            ("se.omnitor.tipcon1.gui.TextSettingsDialog.REAL_TIME"));
        useCR.setActionCommand("useCR");
        useRealTime.setActionCommand("realTime");
        useCR.addActionListener(this);
        useRealTime.addActionListener(this);
        useRTPV.addKeyListener(this);
        useRTPV.addChangeListener(this);
	useRTPV.setText(language.getProperty
        	("se.omnitor.tipcon1.gui.TextSettingsDialog.ENABLE_REALTIME_PREVIEW"));
		useRtpvTimer.addKeyListener(this);
		useRtpvTimer.setText(language.getProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.ENABLE_REALTIME_PREVIEW_TIMER"));

        // if use CR
        if(appSettings.useSendOnCR()) {
            buffertext.setEnabled(false);
            useCR.setSelected(true);
            useRealTime.setSelected(false);
        }
        else {
            buffertext.setEnabled(true);
            useCR.setSelected(false);
            useRealTime.setSelected(true);
        }

        sendOnCRRadioButtonGroup.add(useRealTime);
        sendOnCRRadioButtonGroup.add(useCR);

        JPanel sendOnCRRadioButtonPanel = new JPanel(new GridLayout(1, 0, 10, 0));
        sendOnCRRadioButtonPanel.add(useRealTime);
        sendOnCRRadioButtonPanel.add(useCR);

	ok.setActionCommand("ok");
	ok.addActionListener(this);
	cancel.setActionCommand("cancel");
	cancel.addActionListener(this);
	defaultButton.setActionCommand("default");
	defaultButton.addActionListener(this);
        getContentPane().setLayout(new java.awt.GridBagLayout());
	addKeyListener(this);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        getContentPane().add(logo, gridBagConstraints);




        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(10, 5, 2, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(sendOnCRRadioButtonPanel, gridBagConstraints);

/*
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
	gridBagConstraints.insets = new Insets(10, 5, 2, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(new JLabel(language.getProperty
					("se.omnitor.tipcon1.gui." +
					 "TextSettingsDialog.RTP_PORT")),
			     gridBagConstraints);
*/
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
	gridBagConstraints.insets = new Insets(10, 5, 2, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(new JLabel(language.getProperty
					("se.omnitor.tipcon1.gui." +
					 "TextSettingsDialog.BUFFER_TIME")),
			     gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
	gridBagConstraints.insets = new Insets(2, 5, 2, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(new JLabel(language.getProperty
					("se.omnitor.tipcon1.gui." +
					 "TextSettingsDialog.MAX_IN_CPS")),
			     gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
	gridBagConstraints.insets = new Insets(2, 5, 10, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(new JLabel(language.getProperty
					("se.omnitor.tipcon1.gui." +
					 "TextSettingsDialog.RED_GENS")),
			     gridBagConstraints);

	gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.gridx = 1;
	gridBagConstraints.gridy = 5;
	gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	getContentPane().add(useRTPV,gridBagConstraints);

	gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.gridx = 1;
	gridBagConstraints.gridy = 6;
	gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints.insets = new Insets(2, 20, 2, 2);
	getContentPane().add(useRtpvTimer,gridBagConstraints);

	JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));
	JPanel buttonPanelDefault = new JPanel(new GridLayout(1, 0, 10, 0));

        ok.setText(language.getProperty("se.omnitor.tipcon1.gui." +
					"TextSettingsDialog.OK"));
	ok.setMargin(new Insets(0, 10, 0, 10));
	ok.addKeyListener(this);
        buttonPanel.add(ok);

        cancel.setText(language.getProperty("se.omnitor.tipcon1.gui." +
					    "TextSettingsDialog.CANCEL"));
	cancel.setMargin(new Insets(0, 10, 0, 10));
	cancel.addKeyListener(this);
        buttonPanel.add(cancel);

        defaultButton.setText(language.getProperty("se.omnitor.tipcon1.gui." +
	    		"TextSettingsDialog.DEFAULT"));
        defaultButton.setMargin(new Insets(0, 10, 0, 10));
        defaultButton.addKeyListener(this);
        buttonPanelDefault.add(defaultButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
	gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(buttonPanelDefault, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
	gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 10, 10);
        getContentPane().add(buttonPanel, gridBagConstraints);

        /*
	porttext.addKeyListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 2, 10);
        getContentPane().add(porttext, gridBagConstraints);
*/

	buffertext.addKeyListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 2, 10);
        getContentPane().add(buffertext, gridBagConstraints);

	cpstext.addKeyListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 10);
        getContentPane().add(cpstext, gridBagConstraints);

	redtext.addKeyListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 10, 10);
        getContentPane().add(redtext, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.gridx = 2;
	gridBagConstraints.gridy = 5;
	gridBagConstraints.gridwidth = 1;
	//gridBagConstraints.insets = new Insets(2, 5, 10, 10);
	gridBagConstraints.anchor = GridBagConstraints.HORIZONTAL;


	fetchSettings();

        pack();

    }

    /**
     * Handles ENTER and ESC.
     *
     * @param ke The event.
     */
    public void keyPressed(KeyEvent ke) {
	switch (ke.getKeyCode()) {
	case KeyEvent.VK_ESCAPE:
	    closeDialog();
	    break;
	case KeyEvent.VK_ENTER:
	    if (saveSettings()) {
		closeDialog();
	    }
	    break;
	default:
	}
    }

    /**
     * Does nothing.
     *
     * @param ke The event.
     */
    public void keyReleased(KeyEvent ke) {
    }

    /**
     * Does nothing.
     *
     * @param ke The event.
     */
    public void keyTyped(KeyEvent ke) {
    }

    public void stateChanged(ChangeEvent e) {
    	if (e.getSource() == useRTPV) {
    		if (useRTPV.isSelected()) {
    			useRtpvTimer.setEnabled(true);
    		}
    		else {
    			useRtpvTimer.setEnabled(false);
    		}
    	}
    }
}





