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
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;

import se.omnitor.tipcon1.AppController;
import se.omnitor.tipcon1.AppSettings;

/**
 * This is a modal dialog that enables the user to change the Alert settings.
 *
 * @author Benjamin Larsson, Omnitor AB
 */
public class AlertSettingsDialog extends JDialog implements ActionListener,
							   KeyListener {

    private AppSettings appSettings;
    private Properties language;

    // GUI components
    private JButton cancel;
    private JButton ok;

    private JCheckBox useAlert;
    private JTextArea infoArea;


    /**
     * Initializes the alert settings dialog. Fetches the settings, initializes
     * the GUI and centers the dialog.
     *
     * @param owner The parent dialog
     * @param controller The application controller that holds all settings
     */
    public AlertSettingsDialog(JFrame owner, AppSettings appSettings, AppController controller) {
	super(owner,
	      controller.getLanguage().getProperty("se.omnitor.tipcon1." +
						   "gui.AlertSettingsDialog." +
						   "ALERT_SETTINGS"),
	      true);

	language = controller.getLanguage();
        this.appSettings = appSettings;

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
	    useAlert.setSelected(appSettings.isAlertingEnabled());
    }

    /**
     * Saves the settings. If an error in the user input was found, an error
     * message dialog will be shown and the function will return immediately.
     *
     * @return True if OK, false if user input is not ok
     */
    private boolean saveSettings() {
	    if (useAlert.isSelected()) {
		System.out.println("Alerting enabled.");
	    } else {
		System.out.println("Alerting disabled.");
    	    }
    	    appSettings.setAlerting(useAlert.isSelected());
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

    }

    /**
     * Initializes the GUI.
     *
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        ok = new JButton();
        cancel = new JButton();

	//useAlert.setActionCommand("useAlert");
	//useAlert.addActionListener(this);
	useAlert = new JCheckBox();
	//this.add(useAlert);
	useAlert.addKeyListener(this);
	useAlert.setText(language.getProperty("se.omnitor.tipcon1.gui." +
        				"AlertSettingsDialog.ALERT_ENABLE"));

        GridBagConstraints constraints;
        constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(20, 12, 20, 20);
	constraints.anchor = GridBagConstraints.SOUTH;
        infoArea = new JTextArea(language.getProperty(
            "se.omnitor.tipcon1.ProgramWindow.alert.INFO_TEXT"))
                   ;
	infoArea.setEditable(false);

	ok.setActionCommand("ok");
	ok.addActionListener(this);
	cancel.setActionCommand("cancel");
	cancel.addActionListener(this);

        getContentPane().setLayout(new java.awt.GridBagLayout());
	addKeyListener(this);

	//Add the checkbox
	getContentPane().add(useAlert);

	getContentPane().add(infoArea);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });


	JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
	gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(buttonPanel, gridBagConstraints);

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

}
