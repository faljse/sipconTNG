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
import java.awt.Dimension;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import se.omnitor.protocol.stun.StunStack;
import se.omnitor.tipcon1.AppController;
import se.omnitor.tipcon1.AppSettings;
import se.omnitor.tipcon1.gui.AddressPanelListener;
import se.omnitor.tipcon1.sip.SipRegistrarInfo;

/**
 * This is a modal dialog that enables the user to change the text settings.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class SipSettingsDialog extends JDialog implements ActionListener,
							   KeyListener,
							  AddressPanelListener,
                                                          ItemListener, SipRegistrationPanelListener {

    private static final int OBJ_SPACE = 2;

    private JFrame owner;
    private AppSettings appSettings;

    // GUI components
    private JButton cancel;
    private JButton ok;
    private JButton defaultButton;
    private JFrame parent;
    //private JLabel sipPort;
    private JLabel stunServerAddressLabel;
    private JTextField outboundProxy;
    private JTextField dialDomain;
    private JTextField fullName;
    private JComboBox sipAddress;
    private JRadioButton stunDisabled;
    private JRadioButton stunAuto;
    private JRadioButton stunForced;
    private ButtonGroup stunMode;
    private JTextField stunServerAddress;
    private SipRegistrationPanel sipRegPanel;
    private JButton stunStatusButton;
    private SipRegistrationPanelListener listListener;

    private Properties language;
    private AppController appController;

    private static ApplyThread applyThread;
    private JRadioButton netIfaceAuto;
    private JRadioButton netIfaceManual;
    private JComboBox avilableNetworkList;
    private ButtonGroup networkIfaceMode;
    String DisplayString = null;
    String netIfaceHardwareAddr = null;

    /**
     * Initializes the text settings dialog. Fetches the settings, initializes
     * the GUI and centers the dialog.
     *
     * @param owner The parent dialog
     * @param controller The application controller that holds all settings
     */
    public SipSettingsDialog(JFrame owner, AppSettings appSettings, Properties language, AppController appController) {
	super(owner, language.getProperty
	      ("se.omnitor.tipcon1.gui.SipSettingsDialog.TITLE"), true);

	this.owner = owner;
	this.appSettings = appSettings;
	this.language = language;
        this.appController = appController;

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

            /*
	 int port = appSettings.getLocalSipPort();
	 if (port > 0) {
	     sipPort.setText(""+port);
	 }
	 else {
	     sipPort.setText("");
	 }
         */

        String name = appSettings.getUserRealName();

        if (name.equals(""))
            fullName.setText(language.getProperty
                             ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
                              "UNKNOWN_USER"));
        else
            fullName.setText(name);

    String primSipAddr = appSettings.getPrimarySipAddress();
    SipRegistrarInfo sipRegs[] = sipRegPanel.getTableData();
    for (int cnt=0; cnt<sipRegs.length; cnt++) {
    	if (sipRegs[cnt].getSipAddress().equals(primSipAddr)) {
    		sipAddress.setSelectedIndex(cnt);
    		continue;
    	}
    }
    
	outboundProxy.setText(appSettings.getOutboundProxy());
	dialDomain.setText(appSettings.getDialDomain());
        switch (appSettings.getStunMode()) {
        case AppSettings.STUN_DISABLED:
            stunDisabled.setSelected(true);
            stunAuto.setSelected(false);
            stunForced.setSelected(false);
            break;
        case AppSettings.STUN_AUTO:
            stunDisabled.setSelected(false);
            stunAuto.setSelected(true);
            stunForced.setSelected(false);
            break;
        case AppSettings.STUN_FORCED:
            stunDisabled.setSelected(false);
            stunAuto.setSelected(false);
            stunForced.setSelected(true);
            break;
        default:
        }
        stunServerAddress.setText(appSettings.getStunServerAddress());
        
        switch (appSettings.getnetIfaceMode()) {
        case AppSettings.NET_IFACE_AUTO:
        	netIfaceAuto.setSelected(true);
        	netIfaceManual.setSelected(false);
        	avilableNetworkList.setEnabled(false);
            break;
        case AppSettings.NET_IFACE_MANUAL:
        	netIfaceManual.setSelected(true);
        	netIfaceAuto.setSelected(false);
        	avilableNetworkList.setEnabled(true);
            break;
        default:
        }
        
        netIfaceHardwareAddr = appSettings.getNetIfMacAddr();
        DisplayString = getMatchingDisplayString(netIfaceHardwareAddr);
        /*aA:: 	If no display string is found that means the NIC is not active.
         * 		Select Automatic mode due to this error condition
         * */
        if(null != DisplayString){
        	avilableNetworkList.setSelectedItem(DisplayString);
        }else{
        	netIfaceAuto.setSelected(true);
        	netIfaceManual.setSelected(false);
        	avilableNetworkList.setEnabled(false);
        	netIfaceHardwareAddr = "";
        }
    }

    /**
     * Saves the settings. If an error in the user input was found, an error
     * message dialog will be shown and the function will return immediately.
     *
     * @return True if OK, false if user input is not ok
     */
    private boolean saveSettings() {

	// Prepare the port
	/*
	int port;
	if (sipPort.getText().trim().length() == 0) {
	    port = 0;
	    for (int cnt=5060; cnt<65535; cnt++) {
		if (settings.getLocalTextPort() == cnt) {
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
		port = Integer.parseInt(sipPort.getText().trim());
	    }
	    catch (Exception e) {
		DialogFactory.showErrorMessageDialog
		    (language.getProperty("se.omnitor.tipcon1." +
					  "SipSettingsDialog.gui." +
					  "DIGITS_ONLY"));
		return false;
	    }
	}
	if (port <= 0) {
	    DialogFactory.showErrorMessageDialog
		(language.getProperty("se.omnitor.tipcon1.gui." +
				      "SipSettingsDialog." +
				      "PORT_IN_RANGE"));
	    return false;
	}
	if (port > 65535) {
	    DialogFactory.showErrorMessageDialog
		(language.getProperty("se.omnitor.tipcon1.gui." +
				      "SipSettingsDialog." +
				      "PORT_IN_RANGE"));
	    return false;
	}
	if (settings.getLocalSipPort() != port) {
	    final int newPort = port;

	    Thread t = new Thread() {
		    public void run() {
			ThreadedDialog td =
			    DialogFactory.showWaitDialog
			    (language.getProperty("se.omnitor.tipcon1.gui." +
						  "SipSettingsDialog." +
						  "PORT_CHANGE"),
			     DialogFactory.NO_ABORT_BUTTON);

			settings.setLocalSipPort(newPort);

			td.dispose();
		    }
		};
            t.setName("Port change dialog");
	    t.start();
	}
	*/


		// Prepare SIP address
    	Object itemObject=sipAddress.getSelectedItem();
    	if(itemObject==null)
    		itemObject=new String("");
		String saddr = ((String)itemObject).trim();
	
		if (!saddr.equals("")) {
		    if (saddr.split("@").length != 2) {
			DialogFactory.showErrorMessageDialog
			    (language.getProperty("se.omnitor.tipcon1.gui." +
						  "SipSettingsDialog.INVALID_ADDR"));
	
			return false;
		    }
		}
	
		// Validating the Full Name Text for allowing [a-z][A-Z][0-9] and space only
		String strFullName = fullName.getText().trim();
		
		Matcher m = Pattern.compile("[a-zA-Z0-9 ]*").matcher(strFullName);
		
		if(!m.matches()){
			DialogFactory.showErrorMessageDialog
		    (language.getProperty("se.omnitor.tipcon1.gui." +
					  "SipSettingsDialog.ERROR_FULL_NAME"));	
			fullName.requestFocus();
			return false;
		}
	
        if (applyThread == null) {
            applyThread = new ApplyThread();
        }

        int stunMode;
        if (stunDisabled.isSelected()) {
            stunMode = AppSettings.STUN_DISABLED;
        } else if (stunAuto.isSelected()) {
            stunMode = AppSettings.STUN_AUTO;
        } else {
            stunMode = AppSettings.STUN_FORCED;
        }

        int netIfaceMode;
        if(netIfaceManual.isSelected()){
        	netIfaceMode = AppSettings.NET_IFACE_MANUAL;
        }else{
        	netIfaceMode = AppSettings.NET_IFACE_AUTO;
        }
        
        
        if(null == netIfaceHardwareAddr || "" == netIfaceHardwareAddr){
        	if(AppSettings.NET_IFACE_MANUAL == netIfaceMode){
        		String SelectedItem = avilableNetworkList.getSelectedItem().toString();
        	
        		if(null != SelectedItem){
        			SelectedItem.trim();
        			netIfaceHardwareAddr = getMatchingMac(SelectedItem);
        			if(null == netIfaceHardwareAddr){
        				netIfaceHardwareAddr = "";
        			}
        		}
        	}else{
        		netIfaceHardwareAddr = "";
        	}
        }
        
        
        
        
        applyThread.applyData(outboundProxy.getText().trim(),
                              appSettings,
                              appController,
                              saddr,
                              dialDomain.getText().trim(),
                              fullName.getText().trim(),
                              sipRegPanel.getTableData(),
                              stunMode,
                              stunServerAddress.getText(),
                              netIfaceMode,
                              netIfaceHardwareAddr.trim());

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
	if (ae.getSource() == ok) {
	    if (saveSettings()) {
		closeDialog();
	    }
	}

	if (ae.getSource() == cancel) {
	    closeDialog();
	}

	if(ae.getSource() == defaultButton)
	{
		int result =
		    JOptionPane.showConfirmDialog
		    (parent,
		     language.getProperty("se.omnitor.tipcon1.gui." +
					  "SipSettingsDialog.RESTORE_DEFAULT_OPTION"),
		     language.getProperty("se.omnitor.tipcon1.gui." +
					  "SipSettingsDialog.RESTORE_DEFAULT"),
		     JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION) {
			fullName.setText(language.getProperty
                    ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
                     "UNKNOWN_USER"));
			outboundProxy.setText("");
			dialDomain.setText("");
			stunAuto.setSelected(true);
			stunServerAddress.setText("stun.fwdnet.net");
			netIfaceAuto.setSelected(true);
        	netIfaceManual.setSelected(false);
        	avilableNetworkList.setEnabled(false);
			javax.swing.JScrollPane scrollPane=(javax.swing.JScrollPane)sipRegPanel.getComponent(0);
			JTable table=(JTable)scrollPane.getViewport().getView();
			while(0 != table.getRowCount()){
				sipRegPanel.delete(0);
			}
			sipAddress.removeAllItems();
		}
	}
        if (ae.getSource() == stunStatusButton) {

            String natType = null;
            String extIp = null;

            String status;
            if (appController.isRuntimeStunActivated()) {
                status = "Activated";

                StunStack ss = appController.getStunStack();
                if (ss != null && ss.isStarted()) {
                    natType = ss.getNatType();
                    extIp = ss.getExternalIp();
                    if (extIp == null) {
                        extIp = "(Unknown)";
                    }
                } else {
                    natType = "(Unknown)";
                    extIp = "(Unknown)";
                }

                if (appController.isSipCompatibleNatDetected()) {
                    natType = "SIP compatible NAT";
                    String natName = appController.getSipCompatibleNatName();
                    if (natName != null) {
                        natType += " (" + natName + ")";
                    }
                }

            }
            else {
                status = "Inactivated";

                StunStack ss = appController.getStunStack();
                if (ss != null && ss.isStarted()) {
                    natType = ss.getNatType();
                    extIp = ss.getExternalIp();
                    if (extIp == null) {
                        extIp = "(Unknown)";
                    }
                } else {
                    natType = "(Unknown)";
                    extIp = "(Unknown)";
                }

                if (appController.isSipCompatibleNatDetected()) {
                    natType = "SIP compatible NAT";
                    String natName = appController.getSipCompatibleNatName();
                    if (natName != null) {
                        natType += " (" + natName + ")";
                    }
                    extIp = "(Unknown)";
                }

            }

            DialogFactory.showArrangedInformationMessageDialog(language.getProperty
                    ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
                     "STUN_STATUS"),
                     new String[] {"STUN status:", status, "Detected NAT:", natType, "External IP:", extIp});

        }
		
    }

    /**
     * Initializes the GUI.
     *
     */
    private void initComponents() {
        GridBagConstraints gbc;

	int preferredHeight =
	    (int)(new JTextField()).getPreferredSize().getHeight();

	getContentPane().setLayout(new GridBagLayout());
	addKeyListener(this);

	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent evt) {
		    closeDialog();
		}
	    });

        stunMode = new ButtonGroup();
        networkIfaceMode = new ButtonGroup();
        
	Border eBorder;
	Border tBorder;




	// Layout buttons
	JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));
    JPanel buttonPane2 = new JPanel(new GridLayout(1, 0, 10, 0));
    defaultButton = new JButton(language.getProperty
		     ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
		      "DEFAULT_BUTTON"));
    defaultButton.setMargin(new Insets(0, 10, 0, 10));
    defaultButton.addKeyListener(this);
    defaultButton.addActionListener(this);
    buttonPanel.add(defaultButton); 
	ok = new JButton(language.getProperty("se.omnitor.tipcon1.gui." +
					      "SipSettingsDialog.OK_BUTTON"));
	ok.setMargin(new Insets(0, 10, 0, 10));
	ok.addKeyListener(this);
	ok.addActionListener(this);
    buttonPane2.add(ok);

	cancel = new JButton(language.getProperty
			     ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
			      "CANCEL_BUTTON"));
	cancel.setMargin(new Insets(0, 10, 0, 10));
	cancel.addKeyListener(this);
	cancel.addActionListener(this);
    buttonPane2.add(cancel);

    
    // Create tabbed pane
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab(language.getProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.USER"), createUserTab(preferredHeight));
    tabbedPane.addTab(language.getProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.NETWORK"), createNetworkTab(preferredHeight));
    tabbedPane.addTab(language.getProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.STUN"), createStunTab(preferredHeight));
    
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(10, 10, OBJ_SPACE, 10);
    getContentPane().add(tabbedPane, gbc);
    

	// Layout all
/*
	gbc = new GridBagConstraints();
	gbc.gridx = 1;
	gbc.gridy = 1;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.insets = new Insets(10, 10, OBJ_SPACE, 10);
	getContentPane().add(networkPanel, gbc);

	gbc = new GridBagConstraints();
	gbc.gridx = 1;
	gbc.gridy = 2;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.insets = new Insets(OBJ_SPACE, 10, OBJ_SPACE, 10);
	getContentPane().add(userPanel, gbc);

	gbc = new GridBagConstraints();
	gbc.gridx = 1;
	gbc.gridy = 3;
	gbc.fill = GridBagConstraints.NONE;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.insets = new Insets(OBJ_SPACE, 10, OBJ_SPACE, 10);
	getContentPane().add(registrarPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(OBJ_SPACE, 10, 10, 10);
        getContentPane().add(stunPanel, gbc);
*/
	gbc = new GridBagConstraints();
	gbc.gridx = 1;
	gbc.gridy = 2;
	gbc.fill = GridBagConstraints.NONE;
	gbc.insets = new Insets(10, 10, 10, 10);
	getContentPane().add(buttonPanel, gbc);
        
        gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 3;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.insets = new Insets(OBJ_SPACE, 10, 10, 10);
    	getContentPane().add(buttonPane2, gbc);
	  

	fetchSettings();

        pack();

        refreshStunPanelActivation();
        //refreshNetworkIfaceActivation();
    }
    
    private JPanel createStunTab(int preferredHeight) {

    	JPanel stunTab = new JPanel(new GridBagLayout());
    	GridBagConstraints gbc = new GridBagConstraints();
    	
    	
        // Layout STUN settings
        JPanel stunPanel = new JPanel(new GridBagLayout());
        Border eBorder = BorderFactory.createEtchedBorder();
        Border tBorder = BorderFactory.createTitledBorder
            (eBorder, language.getProperty("se.omnitor.tipcon1.gui." +
                                           "SipSettingsDialog.STUN"));
        stunPanel.setBorder(tBorder);

        stunDisabled = new JRadioButton(language.getProperty
                                 ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
                                  "DISABLED_STUN"));
        stunDisabled.addKeyListener(this);
        stunDisabled.addItemListener(this);
        stunMode.add(stunDisabled);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, 0, OBJ_SPACE);
        gbc.anchor = GridBagConstraints.WEST;
        //gbc.fill = GridBagConstraints.HORIZONTAL;
        stunPanel.add(stunDisabled, gbc);

        stunAuto = new JRadioButton(language.getProperty
                                 ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
                                  "AUTO_STUN"));
        stunAuto.addKeyListener(this);
        stunAuto.addItemListener(this);
        stunMode.add(stunAuto);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, OBJ_SPACE, 0, OBJ_SPACE);
        gbc.anchor = GridBagConstraints.WEST;
        //gbc.fill = GridBagConstraints.HORIZONTAL;
        stunPanel.add(stunAuto, gbc);

        stunForced = new JRadioButton(language.getProperty
                                 ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
                                  "FORCED_STUN"));
        stunForced.addKeyListener(this);
        stunForced.addItemListener(this);
        stunMode.add(stunForced);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
        gbc.anchor = GridBagConstraints.WEST;
        //gbc.fill = GridBagConstraints.HORIZONTAL;
        stunPanel.add(stunForced, gbc);

        stunServerAddressLabel = new JLabel(language.getProperty
                                 ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
                                  "STUN_SERVER_ADDRESS"));

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
        gbc.anchor = GridBagConstraints.WEST;
        //gbc.fill = GridBagConstraints.HORIZONTAL;
        stunPanel.add(stunServerAddressLabel, gbc);

        stunServerAddress = new JTextField();
        stunServerAddress.addKeyListener(this);
        stunServerAddress.setPreferredSize(new Dimension(300, preferredHeight));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
        gbc.anchor = GridBagConstraints.WEST;
        //gbc.fill = GridBagConstraints.HORIZONTAL;
        stunPanel.add(stunServerAddress, gbc);

        stunStatusButton = new JButton(language.getProperty
                                 ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
                                  "STUN_STATUS"));
        stunStatusButton.setMargin(new Insets(0, 10, 0, 10));
        stunStatusButton.addKeyListener(this);
        stunStatusButton.addActionListener(this);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
        gbc.anchor = GridBagConstraints.WEST;
        //gbc.fill = GridBagConstraints.HORIZONTAL;
        stunPanel.add(stunStatusButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridheight = 5;
        gbc.weightx = 1.0;
        stunPanel.add(new JLabel(""), gbc);

        
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
        gbc.anchor = GridBagConstraints.WEST;
        stunTab.add(stunPanel, gbc);
        
    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 2;
    	gbc.weighty = 1.0;
    	gbc.anchor = GridBagConstraints.WEST;
    	stunTab.add(new JPanel(), gbc);

        
        return stunTab;
    }
    
    private JPanel createNetworkTab(int preferredHeight) {
    	JPanel networkTab = new JPanel(new GridBagLayout());
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	
    	
    	// Network panel
    	JPanel networkPanel = new JPanel(new GridBagLayout());
    	Border eBorder = BorderFactory.createEtchedBorder();
    	Border tBorder =
    	    BorderFactory.createTitledBorder
    	    (eBorder, language.getProperty("se.omnitor.tipcon1.gui." +
    					   "SipSettingsDialog.NETWORK"));
    	networkPanel.setBorder(tBorder);

            /*
    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 1;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, 5);
    	gbc.anchor = GridBagConstraints.WEST;
    	networkPanel.add(new JLabel(language.getProperty
    				    ("se.omnitor.tipcon1.gui." +
    				     "SipSettingsDialog." +
    				     "SIP_PORT")), gbc);
            */

    	//sipPort = new JLabel();
    	//sipPort.addKeyListener(this);
    	//sipPort.setPreferredSize(new Dimension(50, preferredHeight));
            /*
    	gbc = new GridBagConstraints();
    	gbc.gridx = 2;
    	gbc.gridy = 1;
    	gbc.insets = new Insets(OBJ_SPACE, 5, OBJ_SPACE, OBJ_SPACE);
    	gbc.weightx = 1.0;
    	gbc.anchor = GridBagConstraints.WEST;
    	networkPanel.add(sipPort, gbc);
           */

    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 2;
    	gbc.anchor = GridBagConstraints.WEST;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, 5);
    	networkPanel.add(new JLabel(
            	language.getProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.OUTBOUND_PROXY")), gbc);

    	outboundProxy = new JTextField();
    	outboundProxy.addKeyListener(this);
    	outboundProxy.setPreferredSize(new Dimension(318, preferredHeight));
    	gbc = new GridBagConstraints();
    	gbc.gridx = 2;
    	gbc.gridy = 2;
    	gbc.insets = new Insets(OBJ_SPACE, 5, OBJ_SPACE, OBJ_SPACE);
    	gbc.weightx = 1.0;
    	gbc.anchor = GridBagConstraints.WEST;
    	networkPanel.add(outboundProxy, gbc);

    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 3;
    	gbc.anchor = GridBagConstraints.WEST;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, 5);
    	networkPanel.add(new JLabel(language.
    				    getProperty("se.omnitor.tipcon1.gui." +
    						"SipSettingsDialog." +
    						"DIAL_DOMAIN")), gbc);

    	dialDomain = new JTextField();
            dialDomain.addKeyListener(this);
    	dialDomain.setPreferredSize(new Dimension(318, preferredHeight));
    	gbc = new GridBagConstraints();
    	gbc.gridx = 2;
    	gbc.gridy = 3;
    	gbc.insets = new Insets(OBJ_SPACE, 5, OBJ_SPACE, OBJ_SPACE);
    	gbc.weightx = 1.0;
    	gbc.anchor = GridBagConstraints.WEST;
    	networkPanel.add(dialDomain, gbc);

    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 4;
    	gbc.anchor = GridBagConstraints.WEST;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, 5);
    	networkPanel.add(new JLabel(language.
    				    getProperty("se.omnitor.tipcon1.gui." +
    						"SipSettingsDialog." +
    						"NETWORK_INTERFACE")), gbc);
    	
    	netIfaceAuto = new JRadioButton(language.getProperty
                ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
                 "NET_INTERFACE_AUTO"));
    	
    	gbc = new GridBagConstraints();
    	gbc.gridx = 2;
    	gbc.gridy = 4;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
    	gbc.weightx = 1.0;
    	gbc.anchor = GridBagConstraints.WEST;
    	networkIfaceMode.add(netIfaceAuto);
    	networkPanel.add(netIfaceAuto, gbc);
    	
    	
    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 5;
    	gbc.anchor = GridBagConstraints.WEST;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, 5);
    	networkPanel.add(new JLabel(""), gbc);
    	
    	netIfaceManual = new JRadioButton(language.getProperty
                ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
                 ""));
    	
    	gbc = new GridBagConstraints();
    	gbc.gridx = 2;
    	gbc.gridy = 5;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
    	gbc.weightx = 1.0;
    	gbc.anchor = GridBagConstraints.WEST;
    	networkIfaceMode.add(netIfaceManual);
    	networkPanel.add(netIfaceManual, gbc);
    	netIfaceManual.addItemListener(this);
    	

    	avilableNetworkList=new JComboBox();
    	gbc = new GridBagConstraints();
    	avilableNetworkList.setPreferredSize(new Dimension(300, preferredHeight));
    	gbc.gridx = 2;
    	gbc.gridy = 5;
    	gbc.insets = new Insets(OBJ_SPACE, 23, OBJ_SPACE, OBJ_SPACE +3);
    	gbc.anchor = GridBagConstraints.WEST;
    	networkPanel.add(avilableNetworkList, gbc);
    	populateCombo();
    	avilableNetworkList.setRenderer( new MyRenderer());
    	avilableNetworkList.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
    	avilableNetworkList.addActionListener(new ActionListener(){
    		public void actionPerformed(ActionEvent ae){
    			avilableNetworkList.hidePopup();
    			String SelectedItem = avilableNetworkList.getSelectedItem().toString();
    			SelectedItem.trim();
    			netIfaceHardwareAddr = getMatchingMac(SelectedItem);
    		}
    	});
    	
    	
    	
    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 1;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
    	gbc.anchor = GridBagConstraints.WEST;
    	networkTab.add(networkPanel, gbc);

    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 2;
    	gbc.weighty = 1.0;
    	gbc.anchor = GridBagConstraints.WEST;
    	networkTab.add(new JPanel(), gbc);

    	return networkTab;
    }

    private JPanel createUserTab(int preferredHeight) {
    	
    	JPanel userTab = new JPanel(new GridBagLayout());
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	
    	
    	// User panel
    	JPanel userPanel = new JPanel(new GridBagLayout());
    	Border eBorder = BorderFactory.createEtchedBorder();
    	Border tBorder = BorderFactory.createTitledBorder
    	    (eBorder, language.getProperty("se.omnitor.tipcon1.gui." +
    					   "SipSettingsDialog.USER"));
    	userPanel.setBorder(tBorder);

    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 1;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
    	gbc.anchor = GridBagConstraints.WEST;
    	gbc.weightx = 1.0;
    	//gbc.fill = GridBagConstraints.HORIZONTAL;
    	userPanel.add(new JLabel(language.getProperty
    				 ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
    				  "FULL_NAME")), gbc);

    	fullName = new JTextField();
    	fullName.addKeyListener(this);
    	fullName.setPreferredSize(new Dimension(313, preferredHeight));
    	gbc = new GridBagConstraints();
    	gbc.gridx = 2;
    	gbc.gridy = 1;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, 5);
    	gbc.anchor = GridBagConstraints.EAST;
    	//gbc.fill = GridBagConstraints.HORIZONTAL;
    	userPanel.add(fullName, gbc);

    	/**
    	gbc = new GridBagConstraints();
    	gbc.gridx = 3;
    	gbc.gridy = 1;
    	gbc.gridheight = 2;
    	//gbc.weightx = 1.0;
    	userPanel.add(new JLabel(""), gbc);
    	*/


    	// Layout registrations
    	sipRegPanel =
    	    new SipRegistrationPanel(owner,
    				     400,
    				     75,
    				     language,
    				     appSettings.getSipRegistrarInfo());
    	sipRegPanel.addListListener(this);
    	JPanel registrarPanel = new JPanel(new GridBagLayout());
    	eBorder = BorderFactory.createEtchedBorder();
    	/*
    	tBorder = BorderFactory.createTitledBorder
    	    (eBorder, language.getProperty("se.omnitor.tipcon1.gui." +
    					   "SipSettingsDialog.USER"));
    	*/
    	tBorder = BorderFactory.createTitledBorder
    	    (eBorder,
    	     language.getProperty("se.omnitor.tipcon1.gui." +
    				  "SipSettingsDialog.REGS"));
    	registrarPanel.setBorder(tBorder);

    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 1;
    	gbc.gridwidth = 2;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
    	gbc.anchor = GridBagConstraints.WEST;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	registrarPanel.add(sipRegPanel, gbc);
    	
    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 2;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
    	gbc.anchor = GridBagConstraints.WEST;
    	//gbc.fill = GridBagConstraints.HORIZONTAL;
    	registrarPanel.add(new JLabel(language.getProperty
    				 ("se.omnitor.tipcon1.gui.SipSettingsDialog." +
    				  "SIP_ADDRESS")), gbc);

    	sipAddress = new JComboBox();
    	sipAddress.addKeyListener(this);
    	sipAddress.setPreferredSize(new Dimension(300, preferredHeight));
    	gbc = new GridBagConstraints();
    	gbc.gridx = 2;
    	gbc.gridy = 2;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
    	gbc.anchor = GridBagConstraints.WEST;
    	//gbc.fill = GridBagConstraints.HORIZONTAL;
    	registrarPanel.add(sipAddress, gbc);
    	
    	refreshSipAddressList();
    	
    	
    	
    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 1;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
    	gbc.anchor = GridBagConstraints.WEST;
    	userTab.add(userPanel, gbc);

    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 2;
    	gbc.insets = new Insets(OBJ_SPACE, OBJ_SPACE, OBJ_SPACE, OBJ_SPACE);
    	gbc.anchor = GridBagConstraints.WEST;
    	userTab.add(registrarPanel, gbc);
    	
    	gbc = new GridBagConstraints();
    	gbc.gridx = 1;
    	gbc.gridy = 3;
    	gbc.weighty = 1.0;
    	gbc.anchor = GridBagConstraints.WEST;
    	userTab.add(new JPanel(), gbc);

    	
    	return userTab; 
    }
    
	/**
	  * Inner class for Combox Box Renderer
	  *
	  **/
	class MyRenderer extends BasicComboBoxRenderer
	{
		 public Component getListCellRendererComponent(
				 JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
			 super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			 if(null != value)
				 setToolTipText( value.toString() );
	         return this;
	     }
	}
	
	private void populateCombo(){
    	 try{
    		 String IpAddr = null;
    		 boolean FoundAcviteInterface = false;
    		 boolean IsRealEthPresent = false;
    		   		  
    		 Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
    		 for (NetworkInterface netIf : Collections.list(nets)) {
    	    	 Enumeration<InetAddress> inetAddres = netIf.getInetAddresses();
 	            for (InetAddress inetAddress : Collections.list(inetAddres)) {
 	            	if(!(inetAddress.isLoopbackAddress())){
 	            		IsRealEthPresent = true;
 	            	}
 	            }
    	     }    
   
    		 Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
    	     for (NetworkInterface netIf : Collections.list(nics)) {
    	    	 Enumeration<InetAddress> inetAddresses = netIf.getInetAddresses();
    	            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
    	            		if(IsRealEthPresent){
    	            			if(inetAddress.isLoopbackAddress()){
    	            				continue;
    	            			}    	            			    	            			
    	            		}else{
    	            			//avilableNetworkList.addItem("No network interfaces available");
    	            			break;
    	            		}
    	            		
    	            		String DisplayString = null;
    	            		try{
    	            			IpAddr = inetAddress.getHostAddress();
    	            		}catch(Exception e){
    	            			e.printStackTrace();
    	            		} 
    	            		
    	            		if(null == IpAddr || IpAddr.equals("0.0.0.0") || IpAddr.equals("")){
    	            			continue;
    	            		}
    	            		IpAddr.trim();	            		
    	            		if(!(appController.ValidateIPAddress(IpAddr)))
    	            		{
    	            			continue;
    	            		}
    	            		
    	            		DisplayString = netIf.getDisplayName() +" " + "(" + IpAddr + ")";
    	            		DisplayString.trim();
    	            		avilableNetworkList.addItem(DisplayString);
    	            		FoundAcviteInterface = true;
    	            			
    	            	}
    	            }
    	     	if(false == FoundAcviteInterface || !IsRealEthPresent){
    	     		avilableNetworkList.addItem("No network interfaces available");
    	     	}
    	 	}catch(SocketException e){
    	    	//e.printStackTrace();
    	    	//handle exception
    	    }
    	}
         	       
    private String getMatchingMac(String SelectedItem){
    	String macValue = null;
    	try{
    		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
    		for (NetworkInterface netIf : Collections.list(nets)) {
    			String IpAddr = null;
    			String DisplayString = null;
	            Enumeration<InetAddress> inetAddresses = netIf.getInetAddresses();
	            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
	            	if(!(inetAddress.isLoopbackAddress())){
	            	
		            	try{
		            		IpAddr = inetAddress.getHostAddress();
		               	}catch(Exception e){
	            			e.printStackTrace();
	            		} 
	            		
	            		if(null == IpAddr || IpAddr.equals("0.0.0.0") || IpAddr.equals("")){
	            			continue;
	            		}
		            	IpAddr.trim();
		            	if(!(appController.ValidateIPAddress(IpAddr)))
		            	{
		            		continue;
		            	}
	            		
	            		DisplayString = netIf.getDisplayName() +" " + "(" + IpAddr + ")";
	            		DisplayString.trim();
	            			            
	            		if(SelectedItem.compareTo(DisplayString) == 0){
	            			if(netIf.getHardwareAddress() != null){
	            				String singleValue = null, finalValue = "";
	            				String mac = Arrays.toString(netIf.getHardwareAddress());
		            			StringTokenizer token = new StringTokenizer(mac,"[,]");
		            	
		            			try
		            			{
		            				while(token.hasMoreTokens())
		            				{
		            					singleValue = token.nextToken();
		            					String hexString = Integer.toHexString(Integer.parseInt(singleValue.trim()));
		            					if(hexString.length() > 2){
		            						hexString = hexString.substring(hexString.length()-2, hexString.length());
		            					}else if(hexString.length() == 1){
		            						hexString = "0" + hexString;
		            					}          		
		            					finalValue += hexString;
		            				}
		            				
		            				finalValue.trim();
		            				macValue = finalValue;
		            				macValue.trim();
		            			}catch(NumberFormatException ne){}
		            		}
	            		}
	            	}
	            }
    		}
	    }catch(Exception e){
	    	e.printStackTrace();
	    	//handle exception
	    }
	    return macValue;
    }
    
    
    private String getMatchingDisplayString(String macAddress){
    	String DisplayString = null;
    	String macValue = null;
    	
    	try{
    		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
    		for (NetworkInterface netIf : Collections.list(nets)) {
    			String IpAddr = null;
    			Enumeration<InetAddress> inetAddresses = netIf.getInetAddresses();
	            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
	            	if(!(inetAddress.isLoopbackAddress())){
	            		if(netIf.getHardwareAddress() != null){
            				String singleValue = null, finalValue = "";
            				String mac = Arrays.toString(netIf.getHardwareAddress());
	            			StringTokenizer token = new StringTokenizer(mac,"[,]");
	            	
	            			try
	            			{
	            				while(token.hasMoreTokens())
	            				{
	            					singleValue = token.nextToken();
	            					String hexString = Integer.toHexString(Integer.parseInt(singleValue.trim()));
	            					if(hexString.length() > 2){
	            						hexString = hexString.substring(hexString.length()-2, hexString.length());
	            					}else if(hexString.length() == 1){
	            						hexString = "0" + hexString;
	            					}			            		
	            					finalValue += hexString;
	            				}
	            				
	            				finalValue.trim();
	            				macValue = finalValue;
	            				macValue.trim();
	            				
	            				if(macValue.compareTo(macAddress) == 0){
	            					IpAddr = inetAddress.getHostAddress();
	        	            		
	            					try{
	        		            		IpAddr = inetAddress.getHostAddress();
	        		               	}catch(Exception e){
	        	            			e.printStackTrace();
	        	            		} 
	        	            		
	        	            		if(null == IpAddr || IpAddr.equals("0.0.0.0") || IpAddr.equals("")){
	        	            			continue;
	        	            		}
	        		            	IpAddr.trim();
	            					
	        		            	if(!(appController.ValidateIPAddress(IpAddr)))
	        	            		{
	        	            			continue;
	        	            		}
	        	            		
	        	            		DisplayString = netIf.getDisplayName() +" " + "(" + IpAddr + ")";
	        	            		DisplayString.trim();
	        	            		return DisplayString;
	            				}
	            			}catch(NumberFormatException ne){
	            				return null;
	            			}
	            		}
	            	}
	            }
    		}
	    }catch(Exception e){
	    	return null;
	    }
	    return DisplayString;
    }
     
    private void refreshStunPanelActivation() {
      if (stunDisabled.isSelected()) {
          stunServerAddressLabel.setEnabled(false);
          stunServerAddress.setEnabled(false);
          //stunStatusButton.setEnabled(false);
      }
      else {
          stunServerAddressLabel.setEnabled(true);
          stunServerAddress.setEnabled(true);
          //stunStatusButton.setEnabled(true);
      }
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

    /**
     */
    class ApplyThread implements Runnable {

	private boolean running;
        private boolean newData;

	private String saddr;
        private String outboundProxy;
        private AppSettings appSettings;
        private AppController appController;
        private String dialDomain;
        private String userRealName;
        private SipRegistrarInfo[] registrarInfo;
        private int stunMode;
        private String stunServerAddress;
        private int netIfaceMode;
        private String netIfaceMacAddr;
        
	/**
	 * Initializes.
	 *
	 * @param saddr User's SIP address
	 */
	public ApplyThread() {
	    running = false;
            newData = false;
	}

	/**
	 * Starts the thread. If it is already started, nothing is done.
	 *
	 */
	public void applyData(String outboundProxy,
                              AppSettings appSettings,
                              AppController appController,
                              String saddr,
                              String dialDomain,
                              String userRealName,
                              SipRegistrarInfo[] registrarInfo,
                              int stunMode,
                              String stunServerAddress,
                              int netIfaceMode,
                              String netIfaceMacAddr) {

            synchronized(this) {
                this.outboundProxy = outboundProxy;
                this.appSettings = appSettings;
                this.appController = appController;
                this.saddr = saddr;
                this.dialDomain = dialDomain;
                this.userRealName = userRealName;
                this.registrarInfo = registrarInfo;
                this.stunMode = stunMode;
                this.stunServerAddress = stunServerAddress;
                this.netIfaceMode = netIfaceMode;
                this.netIfaceMacAddr = netIfaceMacAddr;
                newData = true;

                if (!running) {
                    running = true;
                    Thread t = new Thread(this, "Apply SIP changes");
                    t.start();
                }

            }


	}

	/**
	 * Runs the thread.
	 *
	 */
	public void run() {
            /*
	    ThreadedDialog td =
		DialogFactory.showWaitDialog(language.getProperty
					     ("se.omnitor.tipcon1.gui." +
					      "SipSettingsDialog." +
					      "APPLYING"),
					     DialogFactory.NO_ABORT_BUTTON);
                                   */

            appController.waitUntilDetectComplete();

            boolean continueLoop;

            do {
                continueLoop = false;

                synchronized (this) {
                    String ob = outboundProxy;
                    String oldOb = appSettings.getOutboundProxy();

                    if (ob.equals("")) {
                        if (oldOb != null && !oldOb.equals("")) {
                            appSettings.setOutboundProxy("");
                        }
                    } else if (!ob.equals(appSettings.getOutboundProxy())) {
                        appSettings.setOutboundProxy(ob);
                    }

                    appSettings.setDialDomain(dialDomain);
                    appSettings.setUserRealName(userRealName);
                    appSettings.setPrimarySipAddress(saddr);
                    appSettings.setSipRegistrarInfo(registrarInfo);
                    appSettings.setStunMode(stunMode);
                    appSettings.setStunServerAddress(stunServerAddress);
                    appSettings.SetNetIfaceMode(netIfaceMode);
                    appSettings.setNetIfMacAddr(netIfaceMacAddr);
                    
                    newData = false;
                }
                appSettings.save();
                appController.setQueuedSipSystemRestart(true);
                appController.restartSipSystem();

                synchronized (this) {
                    if (newData) {
                        continueLoop = true;
                    }
                    else {
                        running = false;
                        appController.setQueuedSipSystemRestart(false);
                    }
                }
            } while (continueLoop);

	    //td.dispose();
	}
    }

    /**
     * This is for making this class a listener class only. Do nothing.
     *
     */
    public void addressPanelSelectionChanged(String name, String address) {
    }

    /**
     * This is for making this class a listener class only. Do nothing.
     *
     */
    public void handleDoubleClick(String name, String address) {
    }

    /**
     * 
     *
     */
    public void itemStateChanged(ItemEvent ie) {
    	Object source = ie.getItemSelectable();
    	if(netIfaceManual == source ||
    				netIfaceAuto == source	){
    		if(netIfaceManual.isSelected()){
    			avilableNetworkList.setEnabled(true);
    		}else{
    			avilableNetworkList.setEnabled(false);
    		}
    	}

    	//Handles changes in the STUN Server Address checkbox
    	refreshStunPanelActivation();
    	
    	
    }
    
    private void refreshSipAddressList() {
    	String value = (String)sipAddress.getSelectedItem();
    	
    	SipRegistrarInfo sipRegs[] = sipRegPanel.getTableData();

    	sipAddress.removeAllItems();

    	int selected = -1;
    	if (sipRegs.length == 0) {
    		sipAddress.addItem(" ");
    	}
    	else for (int cnt=0; cnt<sipRegs.length; cnt++) {
    		String addr = sipRegs[cnt].getSipAddress();
    		sipAddress.addItem(addr);
    		if (addr.equals(value)) {
    			selected = cnt;
    		}
    	}
    	
    	if (selected != -1) {
    		sipAddress.setSelectedIndex(selected);
    	}
    	
    	sipAddress.repaint();
    }
    
    public void sipRegistrationListChanged() {
    	refreshSipAddressList();
    }
}
