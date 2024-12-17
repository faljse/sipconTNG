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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * This is a class handling a dialog which is run in a separate thread. The
 * separate thread enables the rest of the program to continue doing other
 * work while the user may choose something in the dialog. <br>
 * <br>
 * The class should not be created in any other way than by the DialogFactory.
 * In order to create a dialog, use one of the functions in the DialogFactory
 * class.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class ThreadedDialog implements Runnable, ActionListener, KeyListener {

    /**
     * Indicates that it is unknown what the user has chosen.
     */
    public static final int UNKNOWN = 0;

    /**
     * Indicates that the dialog was disposed by the application, it has
     * executed the dispose() function.
     */
    public static final int MANUALLY_DISPOSED = 1;

    /**
     * Indicates that the user has chosen the "Yes" alternative.
     */
    public static final int YES = 2;

    /**
     * Indicates that the user has chosen the "No" alternative.
     */
    public static final int NO = 3;

    /**
     * Indicates that the user closed the window by pressing the window
     * close button.
     */
    public static final int USER_CLOSED_WINDOW = 4;

    /**
     * Indicates that the user has chosen the "Abort" alternative.
     */
    public static final int ABORT = 5;

    JFrame frame;
    Component[] message;
    String title;
    int optionType;
    int messageType;
    Icon icon;
    String[] options;
    Object initialValue;
    private String result;
    boolean isDisposed;
    private boolean isShown;
    JLabel infoLabel;
    private boolean wasManualDispose;
    private boolean wasClosedWindow;
    private JDialog dialog;
    private int[] results;
    private boolean enableWindowCloseButton;

    /**
     * Initializes the dialog.
     *
     * @param frame The parent frame
     * @param message The components to show
     * @param title The dialos's title
     * @param optionType The option type (see JOptionPane)
     * @param messageType The message type (see JOptionPane)
     * @param icon The dialog's icon
     * @param options User's choices
     * @param initialValue The initial value for choices
     * @param infoLabel An information label
     * @param results The different results, which should correspond to the
     * user's choices. These should be expressed as the constants in this
     * class.
     * @param enableWindowCloseButton Whether the window close button should
     * be enabled.
     */
    public ThreadedDialog(JFrame frame,
			  Component[] message,
			  String title,
			  int optionType,
			  int messageType,
			  Icon icon,
			  String[] options,
			  Object initialValue,
			  JLabel infoLabel,
			  int[] results,
			  boolean enableWindowCloseButton) {

	this.frame = frame;
	this.message = message;
	this.title = title;
	this.messageType = messageType;
	this.optionType = optionType;
	this.icon = icon;
	this.options = options;
	this.initialValue = initialValue;
	this.infoLabel = infoLabel;
	this.results = results;
	this.enableWindowCloseButton = enableWindowCloseButton;

	if (results.length != options.length) {
	    throw new IllegalArgumentException
		("The length of options and results has the be equal!");
	}

	result = null;
	isDisposed = false;
	isShown = false;


    }

    /**
     * Signal that nothing shall be done on close.
     *
     */
    public void setDoNothingOnClose() {
    	//doNothingOnClose = true;
    }

    /**
     * Runs the dialog thread, which will show the dialog.
     *
     */
    public void show() {
	Thread t = new Thread(this, "ThreadedDialog: " + title);
	t.start();
    }

    /**
     * Runs the dialog - shows it.
     *
     */
    public void run() {
	wasManualDispose = false;
	wasClosedWindow = false;

	JOptionPane op = new JOptionPane(message,
					 messageType,
					 optionType,
					 icon,
					 options,
					 initialValue);


	addActionListenerRecursive(op);

	dialog = new JDialog(frame, title, true);
	dialog.setContentPane(op);
	dialog.setResizable(false);
	if (enableWindowCloseButton) {
	    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
	else {
	    dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}
	dialog.pack();
	dialog.setLocation(GuiToolkit.getCenterX() - dialog.getWidth()/2,
			   GuiToolkit.getCenterY() - dialog.getHeight()/2);
	isShown = true;
	dialog.setVisible(true);

	if (wasManualDispose) {
	    result = null;
	}
	else if (result == null) {
	    wasClosedWindow = true;
	}

	isDisposed = true;

	/*
	wasManualDispose = false;

	result = JOptionPane.showOptionDialog(frame,
					      message,
					      title,
					      optionType,
					      messageType,
					      icon,
					      options,
					      initialValue);

	if (wasManualDispose) {
	    result = ABORTED;
	}
	else {
	    if (result == JOptionPane.YES_OPTION) {
		result = YES;
	    }
	    else {
		result = NO;
	    }
	}

	isDisposed = true;
	*/
    }

    /**
     * Gets the result of the window. If the windows has not yet been
     * disposed, the function will block until it has.
     *
     * @return The window's result (the user's choice)
     */
    public int getResult() {
	while (!isDisposed) {
	    try {
		Thread.sleep(100);
	    }
	    catch (InterruptedException e) {
		// Ignore any exception here
	    }
	}

	if (wasClosedWindow) {
	    return USER_CLOSED_WINDOW;
	}

	if (result == null) {
	    return MANUALLY_DISPOSED;
	}

	for (int cnt=0; cnt<options.length; cnt++) {
	    if (result.equals(options[cnt])) {
		return results[cnt];
	    }
	}

	return UNKNOWN;
    }

    /**
     * Disposes the window.
     *
     */
    public void dispose() {
	while (!isShown) {
	    try {
		Thread.sleep(100);
	    }
	    catch (InterruptedException e) {
		// Ignore exceptions here
	    }
	}
	wasManualDispose = true;
	dialog.dispose();
	/*
	Component comp = message[0];

	if (comp != null) {
	    while (comp.getParent() != null &&
		   !(comp.getParent() instanceof Dialog)) {
		comp = comp.getParent();
	    }

	    if (comp != null) {
		wasManualDispose = true;
		((Dialog)comp.getParent()).dispose();
	    }
	}
	*/
    }

    /**
     * Sets the information text.
     *
     * @param text The new information text.
     */
    public void setInfoText(String text) {
	infoLabel.setText(text);
    }

    /**
     * Handles actions from the components / disposes the dialog.
     *
     * @param ae The event.
     */
    public void actionPerformed(ActionEvent ae) {
	result = ((JButton)ae.getSource()).getText();
	dialog.dispose();
    }

    /**
     * Handles ESC.
     *
     * @param ke The event.
     */
    public void keyPressed(KeyEvent ke) {
	if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
	    result = null;
	    dialog.dispose();
	}
    }

    /**
     * Does nothing.
     *
     * @param ke The event.
     */
    public void keyTyped(KeyEvent ke) {
    }

    /**
     * Does nothing.
     *
     * @param ke The event.
     */
    public void keyReleased(KeyEvent ke) {
    }

    private void addActionListenerRecursive(Container container) {
	int cc = container.getComponentCount();
	Component c;
	for (int cnt=0; cnt<cc; cnt++) {

	    c = container.getComponent(cnt);

	    if (c instanceof JButton) {
		((JButton)c).addActionListener(this);
		if (enableWindowCloseButton) {
		    ((JButton)c).addKeyListener(this);
		}
	    }
	    else if (c instanceof JPanel) {
		addActionListenerRecursive((JPanel)c);
	    }

	}
    }

}
