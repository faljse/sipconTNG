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

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;


import se.omnitor.tipcon1.AppController;
import se.omnitor.tipcon1.AppSettings;

/**
 * This is a modal dialog that enables the user to change language.
 *
 * @author Christer Ulfsparre, Omnitor AB
 */
public class LanguageDialog extends JDialog implements ActionListener,
                                                           KeyListener {

    private AppSettings appSettings;
    private Properties language;

    // GUI components
    private JButton cancelButton;
    private JButton okButton;
    private JButton defaultButton;
    private JFrame parent;
    private JComboBox languageComboBox;


    /**
     * Initializes the language dialog. Fetches the settings, initializes
     * the GUI and centers the dialog.
     *
     * @param owner The parent dialog
     * @param controller The application controller that holds all settings
     */
    public LanguageDialog(JFrame owner, AppSettings appSettings, AppController controller) {
        super(owner, controller.getLanguage().getProperty("se.omnitor.tipcon1.gui.LanguageDialog.LANGUAGE_SETTINGS"),
              true);

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

    }

    /**
     * Saves the settings. If an error in the user input was found, an error
     * message dialog will be shown and the function will return immediately.
     *
     * @return True if OK, false if user input is not ok
     */
    private boolean saveSettings() {

    	int selection = languageComboBox.getSelectedIndex();

        appSettings.setLanguageCode(selection);
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
    			languageComboBox.setSelectedIndex(0);
    		}
        }
    }

    /**
     * Initializes the GUI.
     *
     */
    private void initComponents() {

        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        okButton = new JButton();
        cancelButton = new JButton();
        defaultButton = new JButton();
        languageComboBox = new JComboBox(loadLanguageArray());
        languageComboBox.setSelectedIndex(appSettings.getLanguageCode());
        int preferredHeight = (int)(new JComboBox()).getPreferredSize().getHeight();

        gridBagConstraints.insets = new Insets(10, 10, 10, 10);

        languageComboBox.setPreferredSize(new Dimension(200, preferredHeight));
    	gridBagConstraints.gridx = 0;
	gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
 	getContentPane().add(languageComboBox, gridBagConstraints);

 	defaultButton.setActionCommand("default");
    defaultButton.addActionListener(this);
    defaultButton.setText(language.getProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.DEFAULT"));
    defaultButton.setMargin(new Insets(0, 10, 0, 10));
    defaultButton.addKeyListener(this);
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    getContentPane().add(defaultButton, gridBagConstraints);
    okButton.setActionCommand("ok");
	okButton.addActionListener(this);
        okButton.setText(language.getProperty(
            "se.omnitor.tipcon1.gui.TextSettingsDialog.OK")
            + " (" +  language.getProperty("se.omnitor.tipcon1.gui.RESTART_REQUIRED") + ")" );
        okButton.setMargin(new Insets(0, 10, 0, 10));
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        getContentPane().add(okButton, gridBagConstraints);

        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        cancelButton.setText(language.getProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.CANCEL"));
        cancelButton.setMargin(new Insets(0, 10, 0, 10));
        cancelButton.addKeyListener(this);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        getContentPane().add(cancelButton, gridBagConstraints);

        getContentPane().addKeyListener(this);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });


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

    private String[] loadLanguageArray() {
    	return new String[]{
            language.getProperty("se.omnitor.tipcon1.language.english"),
            language.getProperty("se.omnitor.tipcon1.language.swedish")};
    }

}
