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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import se.omnitor.tipcon1.AppConstants;
import se.omnitor.tipcon1.AppController;
import se.omnitor.tipcon1.AppSettings;

/**
 * A modal dialog box showing information about the program.
 *
 * @author Erik Zetterstrm, Omnitor AB
 */
class RegisterDialog extends JDialog implements ActionListener {

    private Properties language;

    private AppSettings appSettings;

    /**
     * Initializes the dialog, it does not show it.
     *
     * @param owner The parent frame of this dialog
     * @param language Language specific properties
     */
    public RegisterDialog(JFrame owner,
                          AppController controller,
                          AppSettings appSettings) {
	super(owner, controller.getLanguage().getProperty(
				  "se.omnitor.tipcon1." +
				  "gui.Register." +
				  "TITLE"), true);

	this.language = controller.getLanguage();

        this.appSettings = appSettings;

	initGui();

        setLocation(GuiToolkit.getCenterX() - getWidth()/2,
		    GuiToolkit.getCenterY() - getHeight()/2);

        addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent we) {
		    dispose();
		}
	    });

    }

    private void initGui() {

	String info = "";

        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            info = language.getProperty("se.omnitor.tipcon1." +
                                               "gui.Register." +
                                               "INFO_TIPCON1");
        }
        else {
            info = language.getProperty("se.omnitor.tipcon1." +
					     "gui.Register." +
					     "INFO");
        }
	JLabel urlLabel = new JLabel();
	Font jLabelFont = urlLabel.getFont();


	getContentPane().setLayout(new GridBagLayout());
	GridBagConstraints constraints;

	getContentPane().setBackground(Color.WHITE);

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(20, 12, 20, 20);
	constraints.anchor = GridBagConstraints.WEST;
	JTextArea infoArea = new JTextArea(info);
	infoArea.setEditable(false);
	infoArea.setFont(jLabelFont);
	getContentPane().add(infoArea,constraints);

	JButton okButton = new JButton("OK");
	okButton.addActionListener(this);
	okButton.setMargin(new Insets(0, 10, 0, 10));
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	constraints.insets = new Insets(20, 20, 20, 20);
	constraints.anchor = GridBagConstraints.CENTER;
	getContentPane().add(okButton, constraints);

	pack();
    }


    /**
     * Handles actions, this is a part of the ActionListener interface.
     *
     * @param ae The incoming action event
     */
    public void actionPerformed(ActionEvent ae) {

        // Error message OK button. The error message box are of two different
        // types, the killing and the friendly. The killing is a serious error
        // and the program should exit. The friendly just shows an error msg.

	dispose();
    }


}






