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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.Locale;
import java.util.Vector;
import java.util.Properties;

import javax.media.Format;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import se.omnitor.tipcon1.AppController;
import se.omnitor.tipcon1.DataToolkit;
import se.omnitor.tipcon1.DeviceContainer;
import se.omnitor.tipcon1.AppSettings;

/**
 * This class is a superclass for the audio and video settings dialog. Those
 * dialogs has lots of common code, therefore everything is programmed in
 * this class. Then, the audio and video settings dialog classes extends
 * this class to get the main functionality.
 *
 * The dialog is modal.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public abstract class AudioVideoSettingsDialog
    extends JDialog implements ActionListener, KeyListener {

    private static final int WIDTH = 240;
    private static final int HEIGHT = 175;

    /**
     * The AppController that holds all settings for the application.
     */
    protected AppController settings;

    /**
     * All active codecs. This list does not consist of all codecs, only those
     * that the user has chosen in the GUI by checking the checkboxes.
     */
    protected Vector<String> codecs;

    /**
     * An array of current active formats for all devices. Device N's current
     * active format is in the N:th array position.
     */
    protected Format[] format;

    /**
     * The internal index number for the current active device. This number
     * is used in the format array.
     */
    protected int device;

    /**
     * The local port for this media
     */
    //protected int localPort;

    /**
     * An URL to the media icon
     */
    protected String imageFile;

    private Vector devices;
    private String media;

    // GUI components
    private JLabel audiodevicelabel;
    private JLabel audioformatlabel;
    private JPanel buttonspanel;
    private JButton cancel;
    private JComponent codecpanel;
    private JComponent deviceChoice;
    private JComponent formatChoice;
    private JPanel logo;
    private JButton ok;
    //private JTextField porttext;
    protected JTextField videofps;
    protected JTextField videobitrate;
    protected int bitrate;

    protected static final String [] description = { "32 kbps","48 kbps","64 kbps","128 kbps","192 kbps","256 kbps","384 kbps" };
    protected static final int [] bitrates =       {     32000,    48000,    64000,    128000,    192000,    256000,    384000 };
    protected JComboBox cobitrates;

    private Properties language;
    protected AppSettings ap;

    private String type = "";

    /**
     * Creates the dialog, builds the GUI, gets information about all
     * supported media from AppController and finally centers the dialog.
     * The function does not show the dialog.
     *
     * @param owner The parent dialog for this dialog.
     * @param ac The AppController, which holds all settings.
     * @param media Either "Audio" or "Video". This is used for GUI printouts.
     */
    public AudioVideoSettingsDialog(JFrame owner, AppSettings appSettings, AppController ac,
				    String media) {

	super(owner,
	      media + ac.getLanguage().
	      getProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog." +
			  "SETTINGS"),
	      true);

	this.settings = ac;
	this.media = media;
        this.ap = appSettings;
	language = ac.getLanguage();

	setImageFile();



	devices = (Vector)settings.getSupportedMedia();

        setSize(WIDTH,HEIGHT);
        setResizable(false);

	initComponents();

	setLocation(GuiToolkit.getCenterX() - getWidth()/2,
		    GuiToolkit.getCenterY() - getHeight()/2);

    }

    /**
     * Sets the GUI to show "no devices", "no formats" and "no codecs". This
     * is used when the media type is not available on the system.
     *
     */
    private void setNoDevicesGui() {
	deviceChoice =
	    new JLabel(language.getProperty("se.omnitor.tipcon1.gui." +
					    "AudioVideoSettingsDialog." +
					    "NO_DEVICES"));
	((JLabel)deviceChoice).setForeground(SystemColor.textInactiveText);
	formatChoice =
	    new JLabel(language.getProperty("se.omnitor.tipcon1.gui." +
					    "AudioVideoSettingsDialog." +
					    "NO_FORMATS"));
	((JLabel)formatChoice).setForeground(SystemColor.textInactiveText);
	codecpanel =
	    new JLabel(language.getProperty("se.omnitor.tipcon1.gui." +
					    "AudioVideoSettingsDialog." +
					    "NO_CODECS"));
	((JLabel)codecpanel).setForeground(SystemColor.textInactiveText);
	//porttext.setEnabled(false);
    }

    /**
     * Gets all settings from the AppController.
     *
     */
     abstract void fetchSettings();



    /**
     * Assigns the variable imageFile with an URL to the correct icon to use
     * for the current media type (e.g. video icon or audio icon).
     *
     */
    abstract void setImageFile();

    /**
     * Gets all settings from the AppConroller and then puts all settings to
     * the GUI. The codec list is sorted and the format choice list is
     * updated.
     *
     */
    private void applySettings() {
	fetchSettings();

	/*
	 * Get local port
	 */
        //porttext.setText("" + localPort);


	/*
	 * Get video devices, formats and codecs
	 */
	if (devices == null) {
	    setNoDevicesGui();
	    return;
	}

	Vector<String> codecList = new Vector<String>(1,1);
	Vector formats;
	int cnt1;
	int cnt2;
	Format[] tempFormat;
	int deviceCnt = -1;
	int deviceToUse = -1;

	if (format == null) {
	    setNoDevicesGui();
	    return;
	}

	tempFormat = format;

	// Avoid changes in the global settings when changes are made in the
	// local array.
	format = new Format[tempFormat.length];

	for (cnt1=0; cnt1<tempFormat.length; cnt1++) {
	    format[cnt1] = tempFormat[cnt1];
	}

	int dSize = devices.size();

	for (cnt1=0; cnt1<dSize; cnt1++) {

	    if ((((DeviceContainer)devices.get(cnt1)).getType()).
		equals(media.toLowerCase(Locale.US))){

		deviceCnt++;
		if (device == deviceCnt) {
		    deviceToUse = cnt1;
		}


		// Add to device list
		((JComboBox)deviceChoice).
		    addItem(((DeviceContainer)devices.get(cnt1)).getName());

		// Add codecs to codec list
		formats =
		    (Vector)((DeviceContainer)devices.get(cnt1)).
		    getOutputFormats();

		int fSize = formats.size();

		for (cnt2=0; cnt2<fSize; cnt2++) {
		    if (!codecList.contains((String)formats.get(cnt2))) {
			codecList.add((String)formats.get(cnt2));
		    }
		}

	    }

	}

	if (((JComboBox)deviceChoice).getItemCount() == 0) {
	    setNoDevicesGui();
	    return;
	}

	((JComboBox)deviceChoice).setSelectedIndex(device);

	// Create codec list
	((JPanel)codecpanel).setLayout
	    (new java.awt.GridLayout(0, 4, 10, 3));

	DataToolkit.sort(codecList);

	int clSize = codecList.size();

	for (cnt1=0; cnt1<clSize; cnt1++) {
	    JCheckBox cb = new JCheckBox();
	    cb.addKeyListener(this);
	    cb.setText(((String)codecList.get(cnt1)));
	    cb.setSelected(codecs.contains((String)codecList.get(cnt1)));
	    ((JPanel)codecpanel).add(cb);
	}

	// Update format list
	if (deviceToUse == -1) {
	    deviceToUse = 0;
	}

        type = ((DeviceContainer)devices.get(deviceToUse)).getType();
	updateFormatList(((DeviceContainer)devices.get(deviceToUse)).
                         getName());


    }

    /**
     * Checks user input for errors. The port number has to consist of
     * digits and exist in the interval 0 < port < 65536. Active codecs are
     * stored in a Vector and, by that, prepared to be saved to the
     * AppController.
     *
     * @return True if port is OK, false otherwise
     */
    private boolean prepareSettingsToSave() {
	int cnt;


	/*
	 * Prepare the port
	 */
        /*
	if (porttext.getText().trim().length() == 0) {
	    localPort = 0;
	}
	else {
	    try {
		localPort = Integer.parseInt(porttext.getText().trim());
	    }
	    catch (Exception e) {
		DialogFactory.showErrorMessageDialog
		    (language.getProperty("se.omnitor.tipcon1.gui." +
					  "AudioVideoSettingsDialog." +
					  "DIGITS_ONLY"));

		return false;
	    }
	}
	if (localPort <= 0) {
	    DialogFactory.showErrorMessageDialog
		(language.getProperty("se.omnitor.tipcon1.gui." +
				      "AudioVideoSettingsDialog.PORT_RANGE"));
	    return false;
	}
	if (localPort > 65535) {
	    DialogFactory.showErrorMessageDialog
		(language.getProperty("se.omnitor.tipcon1.gui." +
				      "AudioVideoSettingsDialog.PORT_RANGE"));

	    return false;
	}
       */


	/*
	 * Prepare codecs
	 */
	codecs = new Vector<String>(0, 1);

	if (codecpanel instanceof JPanel) {
	    int cCount = ((JPanel)codecpanel).getComponentCount();

	    for (cnt=0; cnt<cCount; cnt++) {
		if (((JCheckBox)((JPanel)codecpanel).getComponent(cnt)).
		    isSelected()) {

		    codecs.add(((JCheckBox)((JPanel)codecpanel).
				getComponent(cnt)).getText());
		}
	    }
	}

	return true;

    }

    /**
     * Stores all settings to the AppController.
     *
     */
    abstract void storeSettings();

    /**
     * Disposes the dialog.
     *
     */
    private void closeDialog() {
	dispose();
    }

    /**
     * Handles actions when the user clicks "OK" or "Cancel". "OK" will
     * store the settings and close the dialog. "Cancel" will just close the
     * dialog.
     *
     * @param ae The incoming action event.
     */
    public void actionPerformed(ActionEvent ae) {
	if (ae.getActionCommand().equals("ok")) {
	    if (prepareSettingsToSave()) {
		storeSettings();
		closeDialog();
	    }
	}

	if (ae.getActionCommand().equals("cancel")) {
	    closeDialog();
	}
    }

    /**
     * Updates the format list. This function should be run whenever the
     * device choice list is changed. This function fills the format choice
     * list with all the formats, which are supported by the selected device.
     *
     * @param updateDevice The name of the device, as given by JMF. E.g.
     * "vfw:Microsoft WDM Image Capture (Win32):0".
     */
    private void updateFormatList(String updateDevice) {

	int cnt;
	int cnt2;
	Format[] formats;
	String[] formatString;
	Vector outputFormats;

	String currentFormat = "";

	((JComboBox)formatChoice).removeAllItems();

	int dSize = devices.size();

	for (cnt=0; cnt<dSize; cnt++) {
	    if (((DeviceContainer)devices.get(cnt)).getName().
		equals(updateDevice)) {

		formats = ((DeviceContainer)devices.get(cnt)).
		    getCaptureFormats();

		formatString = new String[formats.length];

		for (cnt2=0; cnt2<formats.length; cnt2++) {

		    formatString[cnt2] =
			formatToString(formats[cnt2]);

		    if (formats[cnt2].equals(format[device])) {
			currentFormat = formatString[cnt2];
		    }
		}

		DataToolkit.sort(formatString);

		for (cnt2=0; cnt2<formatString.length; cnt2++) {

                    //EZ: Fixed, ONLY for video
                    //Only add CIF and QCIF
                    if(type.equals("video")) {
                        if ((formatString[cnt2].indexOf("144") >= 0) || (formatString[cnt2].indexOf("288")>=0)) {
                            ((JComboBox) formatChoice).addItem(formatString[
                                    cnt2]);
                        }
                    }
                    else {
                        ((JComboBox) formatChoice).addItem(formatString[
                                    cnt2]);
                    }
                    /*
		    if (formatString[cnt2].equals(currentFormat)) {
			currentFormatIndex = cnt2;
		    }*/

		}


		// Update codec list
		outputFormats =
		    (Vector)((DeviceContainer)devices.get(cnt)).
		    getOutputFormats();

		JCheckBox tempCb;

		int cCount = ((JPanel)codecpanel).getComponentCount();

		for (cnt2=0; cnt2<cCount; cnt2++) {

		    tempCb =
			(JCheckBox)((JPanel)codecpanel).getComponent(cnt2);

		    tempCb.setEnabled
			(outputFormats.contains(tempCb.getText()));
		}

		break;
	    }
	}

        //System.out.println("Current format is: " + currentFormat);

        // Sets the resolution that is currently in us according to appsettings
        // The resolution seems to allways be "176x144(rgb)" after startup,
        // before some other resolution is selcted.
	((JComboBox)formatChoice).setSelectedItem(ap.getResolution());

    }

    /**
     * Gives a String representation of the given format
     *
     * @param format The format, which should be represented by a String
     *
     * @return A String describing the given format
     */
    abstract String formatToString(Format format);

    /**
     * Builds the GUI layout
     *
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        audiodevicelabel = new JLabel();
        audioformatlabel = new JLabel();
        buttonspanel = new JPanel();
        ok = new JButton();
        cancel = new JButton();
        codecpanel = new JPanel();
        formatChoice = new JComboBox();
        deviceChoice = new JComboBox();
        logo = new ImagePanel(imageFile);
        //porttext = new JTextField();
        videofps = new JTextField();;
        videobitrate = new JTextField();

        getContentPane().setLayout(new java.awt.GridBagLayout());
	addKeyListener(this);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });

	((JComboBox)deviceChoice).addItemListener
	    (new AudioVideoSettingsDialogListener("device"));
	((JComboBox)formatChoice).addItemListener
	    (new AudioVideoSettingsDialogListener("format"));

	ok.setActionCommand("ok");
	ok.addActionListener(this);
	ok.addKeyListener(this);

	cancel.setActionCommand("cancel");
	cancel.addActionListener(this);
	cancel.addKeyListener(this);

	codecpanel.setBorder
	    (BorderFactory.createTitledBorder
	     (BorderFactory.createEtchedBorder(),
	      language.getProperty("se.omnitor.tipcon1.gui." +
				   "AudioVideoSettingsDialog.CODECS_TO_USE")));

	applySettings();


	// Arrange GUI
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
	gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        getContentPane().add(logo, gridBagConstraints);

/*
        rtplabel.setText(language.getProperty
			 ("se.omnitor.tipcon1.gui." +
			  "AudioVideoSettingsDialog.RTP_PORT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
	gridBagConstraints.insets = new Insets(10, 5, 2, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(rtplabel, gridBagConstraints);
 */

if (media.equals("Video")) {

    cobitrates = new JComboBox();
    cobitrates.addKeyListener(this);
    for (int i = 0; i < bitrates.length; i++) {
        cobitrates.addItem(description[i]);
    }
    cobitrates.setEnabled(true);

    videobitrate.addKeyListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(10, 5, 2, 10);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    getContentPane().add(cobitrates, gridBagConstraints);
    //getContentPane().add(videobitrate, gridBagConstraints);

    gridBagConstraints.gridx = 1;
    getContentPane().add(new JLabel(
            	language.getProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.BITRATE")),
                gridBagConstraints);

    videofps.addKeyListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(2, 5, 5, 10);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    //getContentPane().add(videofps, gridBagConstraints);

    gridBagConstraints.gridx = 1;
    //getContentPane().add(new JLabel("Frames per second"),gridBagConstraints);

    for (int i = 0; i < bitrates.length; i++) {
        if (bitrate == bitrates[i]) {
            cobitrates.setSelectedIndex(i);
        }
    }

}
        audiodevicelabel.setText(media + language.getProperty
				 ("se.omnitor.tipcon1.gui." +
				  "AudioVideoSettingsDialog.DEVICE"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
	gridBagConstraints.insets = new Insets(10, 5, 2, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(audiodevicelabel, gridBagConstraints);

	deviceChoice.addKeyListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 2, 10);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(deviceChoice, gridBagConstraints);

        audioformatlabel.setText(media + language.getProperty
				 ("se.omnitor.tipcon1.gui." +
				  "AudioVideoSettingsDialog.FORMAT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
	gridBagConstraints.insets = new Insets(2, 5, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(audioformatlabel, gridBagConstraints);

	formatChoice.addKeyListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 5, 10);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(formatChoice, gridBagConstraints);

	/*
        codecstouselabel.setText("Codecs to use:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(codecstouselabel, gridBagConstraints);
	*/

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
	gridBagConstraints.insets = new Insets(5, 5, 5, 10);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(codecpanel, gridBagConstraints);

        buttonspanel.setLayout(new java.awt.GridLayout(1, 0, 10, 0));

        ok.setText(language.getProperty("se.omnitor.tipcon1.gui." +
					"AudioVideoSettingsDialog.OK"));
	ok.setMargin(new Insets(0, 10, 0, 10));
        buttonspanel.add(ok);

        cancel.setText(language.getProperty("se.omnitor.tipcon1.gui." +
					    "AudioVideoSettingsDialog." +
					    "CANCEL"));
	cancel.setMargin(new Insets(0, 10, 0, 10));
        buttonspanel.add(cancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
	gridBagConstraints.insets = new Insets(5, 10, 10, 10);
        getContentPane().add(buttonspanel, gridBagConstraints);

	/*
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 5;
        add(panel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 8;
        add(panel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 6;
        add(panel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 4;
        add(panel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        add(panel8, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 10;
        add(panel10, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        add(panel11, gridBagConstraints);
	*/

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
	    if (prepareSettingsToSave()) {
		storeSettings();
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
     * A listener class for choice lists in the GUI.
     *
     * @author Andreas Piirimets, Omnitor AB
     */
    private class AudioVideoSettingsDialogListener implements ItemListener {

	private String listId;

	/**
	 * Takes care of the listId, nothing else.
	 *
	 * @param listId Either "device" or "format" depending on which list
	 * this listener class belongs to.
	 */
	public AudioVideoSettingsDialogListener(String listId) {
	    this.listId = listId;
	}

	/**
	 * Takes care of changes in the device and format choice lists.
	 * If a device has changed, change the formats. If the format changes,
	 * the correct format is put into internal variables.
	 *
	 * @param ie The incoming item event
	 */
	public void itemStateChanged(ItemEvent ie) {
	    if (ie.getStateChange() == ItemEvent.SELECTED) {

		if (listId == "device") {

		    if (((JComboBox)deviceChoice).getSelectedIndex() !=
			device) {

			device = ((JComboBox)deviceChoice).getSelectedIndex();
			updateFormatList((String)ie.getItem());
		    }

		}


		if (listId == "format") {
		    Format[] formats;
		    int cnt;
		    int cnt2;

		    int dSize = devices.size();

		    for (cnt=0; cnt<dSize; cnt++) {
			if (((DeviceContainer)devices.get(cnt)).getName().
                            equals(((JComboBox)deviceChoice).
                                   getSelectedItem())) {

                            formats = ((DeviceContainer)devices.get(cnt)).
				getCaptureFormats();

			    for (cnt2=0; cnt2<formats.length; cnt2++) {

				if (((String)ie.getItem()).
                                    equals(formatToString(formats[cnt2]))) {

				    format[device] = formats[cnt2];
				}
			    }
			}
		    }

		}

	    }
	}

    }


}
