/*
 * T.140 Presentation Library
 *
 * Copyright (C) 2004-2008 Board of Regents of the University of Wisconsin System
 * (Univ. of Wisconsin-Madison, Trace R&D Center)
 * Copyright (C) 2004-2008 Omnitor AB
 *
 * This software was developed with support from the National Institute on
 * Disability and Rehabilitation Research, US Dept of Education under Grant
 * # H133E990006 and H133E040014
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Please send a copy of any improved versions of the library to:
 * Gunnar Hellstrom, Omnitor AB, Renathvagen 2, SE 121 37 Johanneshov, SWEDEN
 * Gregg Vanderheiden, Trace Center, U of Wisconsin, Madison, Wi 53706
 *
 */
package se.omnitor.protocol.t140;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.SwingUtilities;

//import se.omnitor.tipcon1.AppSettings;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.ConsoleHandler;
import java.util.Properties;

/**
 * This is a dialog for selecting fonts to be used in
 * the textareas available in T140Panel.
 *
 * It allows  the user to select font, font size, font color
 * and font background color for each of the two text areas.
 * 
 * The constructor accepts a specified language parameter for
 * defining all the texts. Here are all the properties and
 * their default values that should be used in the Property
 * language object. If no property is given, the default values
 * stated below will be used by the class automatically:
 * 
 * Property: se.omnitor.tipcon1.gui.fontdialog.FONT_SETTINGS
 * Default: "Text settings"
 *
 * Property: se.omnitor.tipcon1.gui.fontdialog.OK
 * Default: "OK"
 *
 * Property: se.omnitor.tipcon1.gui.fontdialog.CANCEL
 * Default: "Cancel"
 *
 * Property: se.omnitor.tipcon1.gui.fontdialog.FONT
 * Default: "Font"
 *
 * Property: se.omnitor.tipcon1.gui.fontdialog.SIZE
 * Default: "Size"
 *
 * Property: se.omnitor.tipcon1.gui.fontdialog.TEXT
 * Default: "Text"
 *
 * Property: se.omnitor.tipcon1.gui.fontdialog.BACKGROUND
 * Default: "Background"
 * 
 * 
 * @author Erik Zetterstrom, Andreas Piirimets, Omnitor AB
 */
public class T140FontDialog extends JDialog implements ActionListener {

    private static final int OBJ_SPACE = 2;

    private Font currentFont;
    //private Font currentFontRemote;
    private Font[] allFonts;

    private GraphicsEnvironment graphEnv;

    private GridBagConstraints gbc;
    private Properties language ;
    private T140FontDialogListener listener;

    private int[] fontSizes = {
	6,8,9,10,11,12,14,16,18,20,22,24,26,28,26,48,72 };

    private int currentFontSize;

    private JButton okButton;
    private JButton cancelButton;
    private JButton defaultButton;
    private JFrame parent;

    private JComboBox fontComboBox;
    private JComboBox fontSizeComboBox;
    private JComboBox fontColorComboBox;
    private JComboBox fontBackgroundComboBox;

    private JLabel fontLabel;
    private JLabel fontSizeLabel;
    private JLabel fontColorLabel;
    private JLabel fontBackgroundLabel;

    private Logger logger;

    private Color currentFontColor;
    private Color currentFontBackground;

    private ImageIcon[] colorIcons;
    private String[] colorIconNames = { "black",
					"white",
					"blue",
					"cyan",
					"darkGray",
					"gray",
					"green",
					"lightGray",
					"magenta",
					"orange",
					"pink",
					"red",
					"yellow" };

    private T140Panel t140Panel;

    public T140FontDialog(T140FontDialogListener listener,T140Panel t140Panel, JFrame parent) {
    	super(parent, "Font settings", true);
    	this.listener = listener;
    	init(listener,t140Panel, parent, null);
    }
    
    /**
     * Initializes.
     * Acquires all available fonts.
     * Creates and inits GUI components.
     *
     * @param t140Panel The panel containing the
     *                  T.140 textareas to set the font in.
     */
    public T140FontDialog(T140FontDialogListener listener,T140Panel t140Panel, JFrame parent, Properties lang) {
	super(parent,
              lang.getProperty("se.omnitor.tipcon1.gui.fontdialog.FONT_SETTINGS"),
              true);
	this.language = lang;
	this.listener = listener;
	init(listener, t140Panel, parent, lang);
    }
    
    private void init(T140FontDialogListener listener, T140Panel t140Panel, JFrame parent, Properties lang) {
    	this.listener=listener;
	this.t140Panel=t140Panel;
	currentFont=t140Panel.getTaFont();
	currentFontSize=t140Panel.getTaFontSize();

	currentFontColor = t140Panel.getTaFontColor();
	currentFontBackground = t140Panel.getTaFontBackground();

	logger = Logger.getLogger("");
	configureLogger();

	graphEnv = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
	allFonts = graphEnv.getAllFonts();

	fontComboBox = new JComboBox();

	for (int i=0;i<allFonts.length;i++) {
	    fontComboBox.addItem(allFonts[i].getFontName());
	    if ((allFonts[i].getFontName()).equals(currentFont.getFontName())) {
		fontComboBox.setSelectedIndex(i);
	    }
	}

	fontSizeComboBox = new JComboBox();
	for (int i=0;i<fontSizes.length;i++) {
	    fontSizeComboBox.addItem(Integer.valueOf(fontSizes[i]));
	    if (fontSizes[i]==currentFontSize) {
		fontSizeComboBox.setSelectedIndex(i);
	    }
	    /*
	    remoteFontSizeComboBox.addItem(new Integer(fontSizes[i]));
	    if (fontSizes[i]==currentFontSizeRemote) {
		remoteFontSizeComboBox.setSelectedIndex(i);
	    }
	    */
	}

	String okTxt;
	String cancelTxt;
	String defaultTxt;
	String fontTxt;
	String fontSizeTxt;
	String fontColorTxt;
	String fontBackgroundTxt;
	if (lang == null) {
		okTxt = "OK";
		cancelTxt = "Cancel";
		defaultTxt = "Restore Default";
		fontTxt = "Font";
		fontSizeTxt = "Size";
		fontColorTxt = "Color";
		fontBackgroundTxt = "Background";
	}
	else {
		okTxt = lang.getProperty("se.omnitor.tipcon1.gui.fontdialog.OK");
		cancelTxt = lang.getProperty("se.omnitor.tipcon1.gui.fontdialog.CANCEL");
		defaultTxt = lang.getProperty("se.omnitor.tipcon1.gui.fontdialog.DEFAULT");
		fontTxt = lang.getProperty("se.omnitor.tipcon1.gui.fontdialog.FONT");
		fontSizeTxt = lang.getProperty("se.omnitor.tipcon1.gui.fontdialog.SIZE");
		fontColorTxt = lang.getProperty("se.omnitor.tipcon1.gui.fontdialog.TEXT");
		fontBackgroundTxt = lang.getProperty("se.omnitor.tipcon1.gui.fontdialog.BACKGROUND");
	}
	okButton = new JButton(okTxt);
	okButton.addActionListener(this);

	cancelButton = new JButton(cancelTxt);
	cancelButton.addActionListener(this);

	defaultButton = new JButton(defaultTxt);
	defaultButton.addActionListener(this);
	fontLabel = new JLabel(fontTxt);
	fontSizeLabel = new JLabel(fontSizeTxt);
	fontColorLabel = new JLabel(fontColorTxt);
	fontBackgroundLabel = new JLabel(fontBackgroundTxt);

	fontBackgroundComboBox = new JComboBox();

	fontBackgroundComboBox = new JComboBox();

	fontColorComboBox = new JComboBox();

	colorIcons = new ImageIcon[colorIconNames.length];

	String baseDir = getBaseDir();

	for (int i=0;i<colorIconNames.length;i++) {

	    colorIcons[i] = new ImageIcon(baseDir + colorIconNames[i]+".gif");
	    if (colorIcons[i] != null) {
                colorIcons[i].setDescription(colorIconNames[i]);
            }

	    fontColorComboBox.addItem(colorIcons[i]);
	    if ((stringToColor(colorIconNames[i])).
		equals(currentFontColor)) {

		fontColorComboBox.setSelectedIndex(i);
	    }

	    fontBackgroundComboBox.addItem(colorIcons[i]);
	    if ((stringToColor(colorIconNames[i])).
		equals(currentFontBackground)) {

		fontBackgroundComboBox.setSelectedIndex(i);
	    }

	}

	initGui();

    }
    
    /**
     * Handler for the buttons.
     *
     * @param e An ActionEvent from one of the buttons.
     */
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == okButton) {
	    onOK();
	}
	else if (e.getSource() == cancelButton) {
	    dispose();
		}else if(e.getSource() == defaultButton){
				int result =
			    JOptionPane.showConfirmDialog
			    (parent,
			    	language.getProperty("se.omnitor.tipcon1.gui." +
						  "fontdialog.RESTORE_DEFAULT_OPTION")+ "?",
					language.getProperty("se.omnitor.tipcon1.gui." +
						 	"fontdialog.RESTORE_DEFAULT"),
			       		JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				fontComboBox.setSelectedItem(new String("Arial Unicode MS"));
				fontSizeComboBox.setSelectedItem(new Integer(12));
				fontColorComboBox.setSelectedItem(colorIcons[0]);
				fontBackgroundComboBox.setSelectedItem(colorIcons[1]);
			}
		}
    }

    /**
     * Updates the fonts and colors of the two text areas.
     * Then closes the dialog.
     */
    private void onOK() {

	t140Panel.setTaFont(new Font((String)fontComboBox.
					getSelectedItem(),
					Font.PLAIN,
					((Integer)fontSizeComboBox.getSelectedItem()).
					intValue()));

	t140Panel.setTaFontColor
	    (stringToColor(((ImageIcon)fontColorComboBox.getSelectedItem()).getDescription()));

	t140Panel.setTaFontBackground
	    (stringToColor(((ImageIcon)fontBackgroundComboBox.getSelectedItem()).getDescription()));

     String font = new String((String)fontComboBox.getSelectedItem());
     Integer textsize =(Integer)fontSizeComboBox.getSelectedItem();
     int size= textsize.intValue();
     Color textcolor=stringToColor(((ImageIcon)fontColorComboBox.getSelectedItem()).getDescription());
     Color bgcolor=stringToColor(((ImageIcon)fontBackgroundComboBox.getSelectedItem()).getDescription());
     
     listener.saveFontSettings(font, size, textcolor, bgcolor);
	/*
	t140Panel.setRemoteFont
	    (new Font((String)remoteFontComboBox.getSelectedItem(),
		      Font.PLAIN,
		      ((Integer)remoteFontSizeComboBox.getSelectedItem()).
		      intValue()));


	t140Panel.setRemoteFontColor
	    (stringToColor(((ImageIcon)remoteFontColorComboBox.
			    getSelectedItem()).getDescription()));


	t140Panel.setRemoteFontBackground
	    (stringToColor(((ImageIcon)remoteFontBackgroundComboBox.
			    getSelectedItem()).getDescription()));

	*/
	dispose();
    }

    /**
     * Translates the filenames to a standard Java color.
     * Useful in the future if we wish to define other colors.
     *
     * @param colorString A filename, without suffix, describbing the color.
     * @return The color.
     */
    public static Color stringToColor(String colorString) {
	if (colorString.equals("black")) {
	    return Color.black;
	}
	else if (colorString.equals("blue")) {
	    return Color.blue;
	}
	else if (colorString.equals("cyan")) {
	    return Color.cyan;
	}
	else if (colorString.equals("darkGray")) {
	    return Color.darkGray;
	}
	else if (colorString.equals("gray")) {
	    return Color.gray;
	}
	else if (colorString.equals("green")) {
	    return Color.green;
	}
	else if (colorString.equals("lightGray")) {
	    return Color.lightGray;
	}
	else if (colorString.equals("magenta")) {
	    return Color.magenta;
	}
	else if (colorString.equals("orange")) {
	    return Color.orange;
	}
	else if (colorString.equals("pink")) {
	    return Color.pink;
	}
	else if (colorString.equals("red")) {
	    return Color.red;
	}
	else if (colorString.equals("white")) {
	    return Color.white;
	}
	else if (colorString.equals("yellow")) {
	    return Color.yellow;
	}

	return Color.black;

    }

    /**
     * Configures the logger to write to console.
     *
     */
    private void configureLogger() {

        // Set log level to include all.
        logger.setLevel(Level.INFO);

	// Disable parent handlers
	logger.setUseParentHandlers(false);

	// Activate logging to console
	ConsoleHandler ch = new ConsoleHandler();
	ch.setLevel(Level.INFO);
	logger.addHandler(ch);

    }

    /**
     * Initializes, layouts and shows the GUI.
     *
     */
    private void initGui() {

	try {
	    UIManager.setLookAndFeel
		(UIManager.getSystemLookAndFeelClassName());
	}
	catch (IllegalAccessException e) {
	    // Cannot set system's look'n'feel. Ignore and continue.
	}
	catch (ClassNotFoundException e) {
	    // Cannot set system's look'n'feel. Ignore and continue.
	}
	catch (InstantiationException e) {
	    // Cannot set system's look'n'feel. Ignore and continue.
	}
	catch (UnsupportedLookAndFeelException e) {
	    // Cannot set system's look'n'feel. Ignore and continue.
	}

	getContentPane().setLayout(new GridBagLayout());

	gbc = new GridBagConstraints();
	gbc.gridx = 1;
	gbc.gridy = 1;
	gbc.insets = new Insets(OBJ_SPACE, 5, OBJ_SPACE, OBJ_SPACE);
	gbc.weightx = 1.0;
	gbc.anchor = GridBagConstraints.WEST;
	getContentPane().add(fontLabel,gbc);

	gbc = new GridBagConstraints();
	gbc.gridx = 2;
	gbc.gridy = 1;
	gbc.insets = new Insets(OBJ_SPACE, 5, OBJ_SPACE, OBJ_SPACE);
	gbc.weightx = 1.0;
	gbc.anchor = GridBagConstraints.WEST;
	getContentPane().add(fontSizeLabel,gbc);

	gbc = new GridBagConstraints();
	gbc.gridx = 3;
	gbc.gridy = 1;
	gbc.insets = new Insets(OBJ_SPACE, 5, OBJ_SPACE, OBJ_SPACE);
	gbc.weightx = 1.0;
	gbc.anchor = GridBagConstraints.WEST;
	getContentPane().add(fontColorLabel,gbc);

	gbc = new GridBagConstraints();
	gbc.gridx = 4;
	gbc.gridy = 1;
	gbc.insets = new Insets(OBJ_SPACE, 5, OBJ_SPACE, OBJ_SPACE);
	gbc.weightx = 1.0;
	gbc.anchor = GridBagConstraints.WEST;
	getContentPane().add(fontBackgroundLabel,gbc);

	/*
	gbc = new GridBagConstraints();
	gbc.gridx = 1;
	gbc.gridy = 2;
	gbc.insets = new Insets(OBJ_SPACE, 5, OBJ_SPACE, OBJ_SPACE);
	gbc.weightx = 1.0;
	gbc.anchor = GridBagConstraints.WEST;
	getContentPane().add(localFontLabel,gbc);
	*/

	gbc = new GridBagConstraints();
	gbc.gridx = 1;
	gbc.gridy = 2;
	gbc.insets = new Insets(OBJ_SPACE, 5, OBJ_SPACE, OBJ_SPACE);
	gbc.weightx = 1.0;
	gbc.anchor = GridBagConstraints.WEST;
	getContentPane().add(fontComboBox,gbc);

	gbc = new GridBagConstraints();
	gbc.gridx = 2;
	gbc.gridy = 2;
	gbc.insets = new Insets(OBJ_SPACE, 5, OBJ_SPACE, OBJ_SPACE);
	gbc.weightx = 1.0;
	gbc.anchor = GridBagConstraints.WEST;
	getContentPane().add(fontSizeComboBox,gbc);

	gbc = new GridBagConstraints();
	gbc.gridx = 3;
	gbc.gridy = 2;
	gbc.insets = new Insets(OBJ_SPACE, 5, OBJ_SPACE, OBJ_SPACE);
	gbc.weightx = 1.0;
	gbc.anchor = GridBagConstraints.WEST;
	getContentPane().add(fontColorComboBox,gbc);


	gbc = new GridBagConstraints();
	gbc.gridx = 4;
	gbc.gridy = 2;
	gbc.insets = new Insets(OBJ_SPACE, 5, OBJ_SPACE, OBJ_SPACE);
	gbc.weightx = 1.0;
	gbc.anchor = GridBagConstraints.WEST;
	getContentPane().add(fontBackgroundComboBox,gbc);

	// Layout buttons
	JPanel buttonPaneldefault = new JPanel(new GridLayout(1, 0, 10, 0));
	JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));
	okButton.setMargin(new Insets(0, 10, 0, 10));
	//okButton.addKeyListener(this);
	///////okButton.addActionListener(this);
        buttonPanel.add(okButton);


	cancelButton.setMargin(new Insets(0, 10, 0, 10));
	//cancelButton.addKeyListener(this);
	///////cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);
    defaultButton.setMargin(new Insets(0, 10, 0, 10));
    buttonPaneldefault.add(defaultButton);

	/*
	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new FlowLayout());
	buttonPanel.add(okButton);
	buttonPanel.add(cancelButton);
	*/

	gbc = new GridBagConstraints();
	gbc.gridx = 1;
	gbc.gridwidth=5;
	gbc.gridy = 4;
	gbc.insets = new Insets(OBJ_SPACE+10, 5, OBJ_SPACE+5, OBJ_SPACE);
	gbc.weightx = 1.0;
	gbc.anchor = GridBagConstraints.CENTER;
	getContentPane().add(buttonPaneldefault,gbc);
    gbc = new GridBagConstraints();
	gbc.gridx = 1;
	gbc.gridwidth=5;
	gbc.gridy = 5;
	gbc.insets = new Insets(OBJ_SPACE, 5, OBJ_SPACE+5, OBJ_SPACE);
	gbc.weightx = 1.0;
	gbc.anchor = GridBagConstraints.CENTER;
	getContentPane().add(buttonPanel,gbc);

	SwingUtilities.updateComponentTreeUI(this);
	pack();

	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screen.width - getWidth()) / 2,
		    (screen.height - getHeight()) / 2);

	setResizable(false);

    }

    public String getBaseDir() {
	String baseDir = null;

	baseDir =
	    T140FontDialog.class.getProtectionDomain().
	    getCodeSource().getLocation().toString();

	// Windows 2000 machines won't make UTF-8 code of URL.toString(),
	// all other platforms do, though.
	String[] bSpl = baseDir.split(" ");
	if (bSpl.length > 1) {
	    baseDir = "";
	    for (int cnt=0; cnt<bSpl.length; cnt++) {
		baseDir += bSpl[cnt];
		if (cnt+1 < bSpl.length) {
		    baseDir += "%20";
		}
	    }
	}

	if (baseDir.endsWith(".jar")) {

	    String[] splitted = baseDir.split("/");
	    baseDir = "";

	    for (int cnt=0; cnt<splitted.length-1; cnt++) {
		baseDir += splitted[cnt] + "/";
	    }
	}

	try {
	    baseDir =
		new File(new URI(baseDir)).getCanonicalPath() + File.separator;
	}
	catch (URISyntaxException e) {
	    logger.throwing(this.getClass().getName(), "getBaseDir", e);
	    throw new RuntimeException("Cannot initiate base directory!");
	}
	catch (IOException e) {
	    logger.throwing(this.getClass().getName(), "getBaseDir", e);
	    throw new RuntimeException("Cannot initiate base directory!");
	}

	String s = File.separator;

	return baseDir + "se"+s+"omnitor"+s+"protocol"+s+"t140"+s+"images"+s;
    }

}
