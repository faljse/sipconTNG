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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import se.omnitor.tipcon1.English;

/**
 * This class is a factory for creating dialogs. By using this, all dialogs
 * in the application will have a similar layout.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class DialogFactory {

    /**
     * This indicates that the user has pressed the "Answer" button.
     */
    public static final int ANSWER = 1;

    /**
     * This indicates that the user has pressed the "Decline" button.
     */
    public static final int DECLINE = 2;

    /**
     * This indicates that the user has pressed an "Abort" or "Cancel" button
     * o maybe closed the dialog window.
     */
    public static final int ABORTED = 3;

    /**
     * This indicates that an abort button should not be used.
     */
    public static final double NO_ABORT_BUTTON = 1;

    /**
     * This indicates that an abort button shall be used.
     */
    public static final double ABORT_BUTTON = 2;


    private static JFrame programFrame = null;
    //private static JLabel answerAddr = null;
    //private static boolean answerDialogAborted;
    private static Properties lang = new English();

    private static Logger logger = Logger.getLogger("se.omnitor.tipcon1.gui");
    
    /**
     * Registers a language.
     *
     * @param language The language to use.
     */
    public static void registerLanguage(Properties language) {
	lang = language;
    }

    /**
     * This function should be used by the application's main frame to inform
     * all small dialogs about what frame to use as parent.
     *
     * @param frame The application's main frame
     */
    public static void registerProgramFrame(JFrame frame) {
	programFrame = frame;
    }

    /**
     * Gets the application's main frame. If the application's main frame has
     * not been reported to the DialogFactory by using registerProgramFrame(),
     * this function will return a new JFrame.
     *
     * @return The application's main frame.
     */
    private static JFrame getFrame() {
	if (programFrame != null) {
	    return programFrame;
	}

	JFrame frame = new JFrame();
	try {
	    UIManager.setLookAndFeel
		(UIManager.getSystemLookAndFeelClassName());
	    SwingUtilities.updateComponentTreeUI(frame);
	} catch (ClassNotFoundException e) {
	    // If the system's look and feel could not be set, ignore it
	    // and continue with the default look and feel.
		logger.throwing("se.omnitor.tipcon1.gui.DialogFactory", "registerProgramFrame", e);
	} catch (InstantiationException e) {
	    // If the system's look and feel could not be set, ignore it
	    // and continue with the default look and feel.
		logger.throwing("se.omnitor.tipcon1.gui.DialogFactory", "registerProgramFrame", e);
	} catch (IllegalAccessException e) {
	    // If the system's look and feel could not be set, ignore it
	    // and continue with the default look and feel.
		logger.throwing("se.omnitor.tipcon1.gui.DialogFactory", "registerProgramFrame", e);
	} catch (UnsupportedLookAndFeelException e) {
	    // If the system's look and feel could not be set, ignore it
	    // and continue with the default look and feel.
		logger.throwing("se.omnitor.tipcon1.gui.DialogFactory", "registerProgramFrame", e);
	}

	return frame;
    }

    /**
     * This function creates an error message dialog.
     *
     * @param errorMessage The error message to show
     */
    public static void showErrorMessageDialog(String errorMessage) {

	String[] strList = (errorMessage + "\n ").split("\n");

	JOptionPane.showMessageDialog(getFrame(), strList,
				      lang.getProperty("se.omnitor.tipcon1." +
						       "gui.DialogFactory." +
						       "ERROR"),
				      JOptionPane.ERROR_MESSAGE);

    }

    /**
     * This function creates a simple information message dialog.
     *
     * @param title The dialog's title
     * @param infoMessage The information message to show
     */
    public static void showInformationMessageDialog(String title,
						    String infoMessage) {

	String[] strList = (infoMessage + "\n ").split("\n");

	JOptionPane.showMessageDialog(getFrame(), strList, title,
				      JOptionPane.INFORMATION_MESSAGE);

    }

    /**
     * This function creates an arranged information message dialog.
     * Strings A, B, C, D, E and F in the array are formatted as:
     * A B
     * C D
     * E F
     *
     * @param title The dialog's title
     * @param infoMessage The information message to show
     */
    public static void showArrangedInformationMessageDialog(String title,
            String[] infoMessage) {

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc;
        int x=0;
        int y=0;

        for (int cnt=0; cnt<infoMessage.length; cnt+=2) {
            gbc = new GridBagConstraints();
            gbc.gridx = x;
            gbc.gridy = y;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(2, 2, 2, 5);
            panel.add(new JLabel(infoMessage[cnt]), gbc);
            x++;

            if ((cnt+1) < infoMessage.length) {
                gbc = new GridBagConstraints();
                gbc.gridx = x;
                gbc.gridy = y;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(2, 5, 2, 2);
                panel.add(new JLabel(infoMessage[cnt+1]), gbc);
            }
            x--;
            y++;
        }
        gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(" "), gbc);

        JOptionPane.showMessageDialog(getFrame(), panel, title,
                                      JOptionPane.INFORMATION_MESSAGE);

    }



    /**
     * This function creates an answer dialog, which shows the remote's
     * address and options to answer or decline the incoming call.
     *
     * @param address The remote's address
     *
     * @return A ThreadedDialog that is already shown to the user.
     */
    public static ThreadedDialog showAnswerDialog(String address) {

	JFrame frame = getFrame();

	JLabel answerAddr = new JLabel(address);
	answerAddr.setBorder(new ShallowBorder(2, 2, 2, 2));

	Component[] message =
	    new Component[] {
		new JLabel(lang.getProperty("se.omnitor.tipcon1.gui." +
					    "DialogFactory.INCOMING_CALL")),
		answerAddr,
		new JLabel(" ")};

	ThreadedDialog td =
	    new ThreadedDialog
		(frame, message,
		 lang.getProperty("se.omnitor.tipcon1.gui. " +
				  "DialogFactory.INCOMING_CALL"),
		 JOptionPane.YES_NO_OPTION,
		 JOptionPane.PLAIN_MESSAGE,
		 null,
		 new String[] {
		     lang.getProperty("se.omnitor.tipcon1." +
				      "gui.DialogFactory." +
				      "ANSWER"),
		     lang.getProperty("se.omnitor.tipcon1." +
				      "gui.DialogFactory." +
				      "DECLINE"),
		 },
		 null,
		 null,
		 new int[] {ThreadedDialog.YES,
			    ThreadedDialog.NO},
		 true);

	td.show();

	return td;
    }

    /**
     * This function creates an outgoing call dialog, which will show
     * a progress bar and some information about the call progress.
     *
     * @param address The SIP address that the client is calling
     *
     * @return A ThreadedDialog object that is already shown to the user.
     */
    public static ThreadedDialog showOutgoingCallDialog(String address) {

	JFrame frame = getFrame();

	JLabel addr = new JLabel(address);
	addr.setBorder(new ShallowBorder(2, 2, 2, 2));

	JLabel callInfoLabel =
	    new JLabel(lang.getProperty("se.omnitor.tipcon1.gui." +
					"DialogFactory.CALLING"));

	JProgressBar pb = new JProgressBar(JProgressBar.HORIZONTAL);
	pb.setIndeterminate(true);

	Component[] message =
	    new Component[] {
		new JLabel(lang.getProperty("se.omnitor.tipcon1.gui." +
					    "DialogFactory.ADDRESS")),
		addr,
		new JLabel(" "),
		callInfoLabel,
		pb};

	ThreadedDialog td =
	    new ThreadedDialog
		(frame, message,
		 lang.getProperty("se.omnitor.tipcon1.gui.DialogFactory." +
				  "CALLING"),
		 JOptionPane.YES_NO_OPTION,
		 JOptionPane.PLAIN_MESSAGE,
		 null,
		 new String[] {
		     lang.getProperty("se.omnitor.tipcon1.gui.DialogFactory." +
				      "ABORT")
		 },
		 null,
		 callInfoLabel,
		 new int[] {ThreadedDialog.NO},
		 true);

	td.show();

	return td;
    }

    /**
     * Displays a wait dialog, which may have an "Abort" button or no button
     * at all. It also has a progress bar and should mainly be used to make
     * the user wait for something.
     *
     * @param infoText The information text
     * @param abortType Either NO_ABORT_BUTTON or other constant.
     *
     * @return A ThreadedDialog object that has been shown to the user.
     */
    public static ThreadedDialog showWaitDialog(String infoText,
						double abortType) {

	JFrame frame = getFrame();

	JLabel infoLabel = new JLabel(infoText);
	infoLabel.setHorizontalAlignment(JLabel.CENTER);

	JProgressBar pb = new JProgressBar(JProgressBar.HORIZONTAL);
	pb.setIndeterminate(true);

	Component[] message = new Component[] {
	    infoLabel,
	    new JLabel(" "),
	    pb};

	String[] options;
	int[] results;
	if (abortType == NO_ABORT_BUTTON) {
	    options = new String[] {};
	    results = new int[] {};
	}
	else {
	    options = new String[] {
		lang.getProperty("se.omnitor.tipcon1.gui.DialogFactory." +
				 "ABORT")
	    };
	    results = new int[] {ThreadedDialog.ABORT};
	}

	ThreadedDialog td =
	    new ThreadedDialog
		(frame, message, "",
		 JOptionPane.YES_NO_OPTION,
		 JOptionPane.PLAIN_MESSAGE,
		 null,
		 options,
		 null,
		 infoLabel,
		 results,
		 false);

	td.show();

	return td;
    }

}
