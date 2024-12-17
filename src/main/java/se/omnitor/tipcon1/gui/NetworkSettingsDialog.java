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

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import se.omnitor.tipcon1.AppConstants;
import se.omnitor.tipcon1.AppController;
import se.omnitor.tipcon1.AppSettings;

/**
 * This is a modal dialog that enables the user to change the network settings.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class NetworkSettingsDialog extends JDialog implements ActionListener,
							      KeyListener {

    private AppController appController;
    private AppSettings appSettings;

    // GUI components
    private JButton cancel;
    private ImagePanel logo;
    private JButton ok;
    private JTextField registrarText;
    //private JLabel portText;
    private JComboBox nwDevice;
    private JTextField proxyText;

    /**
     * Initializes the text settings dialog. Fetches the settings, initializes
     * the GUI and centers the dialog.
     *
     * @param owner The parent dialog
     * @param controller The application controller that holds all settings
     */
    public NetworkSettingsDialog(JFrame owner, AppSettings appSettings, AppController controller) {
	super(owner, "Network settings", true);

        this.appSettings = appSettings;
	this.appController = controller;

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

	//portText.setText(appSettings.getLocalSipPort() + "  ");
	proxyText.setText(appSettings.getOutboundProxy());

	registrarText.setEnabled(false);

	// Point out the correct network device
	try {
	    NetworkInterface nwif =
		NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
	    int nwitems = nwDevice.getItemCount();
	    for (int cnt=0; cnt<nwitems; cnt++) {
		if (((String)nwDevice.getItemAt(cnt)).
		    startsWith(nwif.getDisplayName())) {

		    nwDevice.setSelectedIndex(cnt);
		    break;
		}
	    }
	}
	catch (UnknownHostException uhe) {
	    // Couldn't resolve local host. Skip all this and just continue.
	}
	catch (SocketException se) {
	    // Couldn't resolve network interface. Skip all this and continue.
	}

	nwDevice.setEnabled(false);

    }

    /**
     * Saves the settings. If an error in the user input was found, an error
     * message dialog will be shown and the function will return immediately.
     *
     * @return True if OK, false if user input is not ok
     */
    private boolean saveSettings() {

	// Prepare port setting
	/*
	if (portText.getText().trim().length() == 0) {
	    localPort = 0;

	    if (settings.getLocalSipPort() == 5060) {
		localPort = 5060;
	    }
	    else {
		for (int cnt=5060; cnt<65535; cnt++) {
		    if (NetToolkit.portIsAvailable(cnt)) {
			localPort = cnt;
			break;
		    }
		}
	    }
	}
	else {
	    try {
		localPort =
		    Integer.parseInt(portText.getText().trim());
	    }
	    catch (Exception e) {
		DialogFactory.showErrorMessageDialog
		    ("The port number may only consist of digits.");
		return false;
	    }
	}
	if (localPort <= 0) {
	    DialogFactory.showErrorMessageDialog
		("Please specify a port in the range 1-65535.");
	    return false;
	}
	if (localPort > 65535) {
	    DialogFactory.showErrorMessageDialog
		("Please specify a port in the range 1-65535.");
	    return false;
	}
	*/


	// Prepare the proxy address
	String paddr = proxyText.getText().trim();

	if (paddr.split("@").length != 1) {
	    DialogFactory.showErrorMessageDialog
		("Invalid proxy address! \n \nSyntax: \n" +
		 "domain.com or \ndomain.com:port");

	    return false;
	}

	if (paddr.split(":").length > 2) {
	    DialogFactory.showErrorMessageDialog
		("Invalid proxy address! \n \nSyntax: \n" +
		 "domain.com or \ndomain.com:port");

	    return false;
	}


	// Prepare the registrar address
	String raddr = registrarText.getText().trim();

	if (raddr.split("@").length != 1) {
	    DialogFactory.showErrorMessageDialog
		("Invalid registrar address! \n \nSyntax: \n" +
		 "domain.com or \ndomain.com:port");

	    return false;
	}

	if (raddr.split(":").length > 2) {
	    DialogFactory.showErrorMessageDialog
		("Invalid registrar address! \n\nSyntax: \n" +
		 "domain.com or \ndomain.com:port");

	    return false;
	}


/*
	if (!appSettings.setSipOutboundProxyAddress(paddr)) {
	    return false;
	}
        */

       appSettings.setOutboundProxy(paddr);
        // When outbound proxy is changed, SIP has to be restarted
        appController.restartSipSystem();
	/*
	if (settings.getLocalSipPort() != localPort) {
	    final int newPort = localPort;

	    Thread t = new Thread() {
		    public void run() {
			ThreadedDialog td =
			    DialogFactory.showWaitDialog
			    ("Changing port, please wait ..",
			     DialogFactory.NO_ABORT_BUTTON);

			settings.setLocalSipPort(newPort);

			td.dispose();
		    }
		};
            t.setName("Changing port dialog");
	    t.start();
	}
	*/

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

        logo = new ImagePanel(AppConstants.NETWORK_IMAGE_URL);
        ok = new JButton();
        cancel = new JButton();
        registrarText = new JTextField();
        //portText = new JLabel();
        nwDevice = new JComboBox();
	proxyText = new JTextField("sip.omnitor.se");

	ok.setActionCommand("ok");
	ok.addActionListener(this);
	cancel.setActionCommand("cancel");
	cancel.addActionListener(this);

        getContentPane().setLayout(new java.awt.GridBagLayout());
	addKeyListener(this);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });

	// Prepare network devices combo box
	try {
	    Enumeration ifs = NetworkInterface.getNetworkInterfaces();
	    NetworkInterface nwif;
	    Enumeration addr;
	    String addrStr;
	    while (true) {
		nwif = (NetworkInterface)ifs.nextElement();
		addr = nwif.getInetAddresses();
		addrStr = "";
		try {
		    while (true) {
			addrStr +=
			    ((InetAddress)addr.nextElement()).getHostAddress();
			if (addr.hasMoreElements()) {
			    addrStr += ", ";
			}
		    }
		}
		catch (NoSuchElementException nsee) {
		    // This is catched when there are no more elements in the
		    // enumeration. This catch clause simply quits the while
		    // loop.
		}
		nwDevice.addItem(nwif.getDisplayName() + " [" + addrStr + "]");
	    }
	}
	catch (NoSuchElementException nsee) {
	    // This is catched when there are no more elements in the
	    // enumeration. This catch clause simply quits the while loop.
	}
	catch (SocketException se) {
	    // I/O error when catching all network interfaces. Nothing to do,
	    // just leave the combobox empty.
	}


	// Arrange GUI
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
	gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        getContentPane().add(logo, gridBagConstraints);

	JPanel sipPanel = new JPanel(new GridBagLayout());
	sipPanel.setBorder
	    (BorderFactory.createTitledBorder
	     (BorderFactory.createEtchedBorder(), "SIP"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
	gridBagConstraints.insets = new Insets(2, 2, 2, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        sipPanel.add(new JLabel("Local port:"), gridBagConstraints);

        JLabel regAddrLabel = new JLabel("Registrar address:");
	regAddrLabel.setEnabled(false);
	gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
	gridBagConstraints.insets = new Insets(2, 2, 2, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        sipPanel.add(regAddrLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
	gridBagConstraints.insets = new Insets(2, 2, 2, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        sipPanel.add(new JLabel("Outbound proxy:"),
			     gridBagConstraints);

	//portText.addKeyListener(this);
        /*
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
	gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 2);
        sipPanel.add(portText, gridBagConstraints);
*/

	registrarText.setEnabled(true);
	registrarText.addKeyListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
	gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 2);
        sipPanel.add(registrarText, gridBagConstraints);

	proxyText.addKeyListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
	gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 2);
        sipPanel.add(proxyText, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
	gridBagConstraints.insets = new Insets(10, 5, 5, 10);
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(sipPanel, gridBagConstraints);

	JPanel nwDevicePanel = new JPanel(new BorderLayout());
	nwDevicePanel.setBorder
	    (BorderFactory.createTitledBorder
	     (BorderFactory.createEtchedBorder(), "Network device"));

	nwDevice.addKeyListener(this);
        nwDevicePanel.add(nwDevice, BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
	gridBagConstraints.insets = new Insets(5, 5, 10, 10);
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(nwDevicePanel, gridBagConstraints);

	JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));

        ok.setText("OK");
	ok.setMargin(new Insets(0, 10, 0, 10));
	ok.addKeyListener(this);
        buttonPanel.add(ok);

        cancel.setText("Cancel");
	cancel.setMargin(new Insets(0, 10, 0, 10));
	cancel.addKeyListener(this);
        buttonPanel.add(cancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
	gridBagConstraints.gridwidth = 2;
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





