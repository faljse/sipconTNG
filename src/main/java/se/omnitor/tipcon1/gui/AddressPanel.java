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

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.Set;
import java.util.HashSet;


/**
 * This class hold a fully functional address book.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class AddressPanel extends JPanel implements ActionListener,
						    MouseListener,
                                                    FocusListener{

    private JTable table;
    private AddressTableModel model;
    private AddressPanelListener listener;
    private JButton newButton;
    private JButton editButton;
    private JButton deleteButton;
    private JFrame parent;
    private int selectedRow;
    private String filename;

    private Object[][] data;

    private Properties language;

    private Logger logger;



    /**
     * Initializes the panel.
     *
     * @param parentFrame The parent frame
     * @param listener The addres panel listener to use
     * @param width The panel's width
     * @param height The panel's height
     */
    public AddressPanel(JFrame parentFrame, AddressPanelListener listener,
			int width, int height, String filename,
			Properties language) {
	super(new GridBagLayout());
	logger = Logger.getLogger("se.omnitor.tipcon1.gui");

	this.language = language;

	GridBagConstraints constraints;

	this.listener = listener;
	this.parent = parentFrame;
	this.filename = filename;
	selectedRow = -1;

	table = new JTable();

	table.addFocusListener(this);

	Set<AWTKeyStroke> keys = table.getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
	//table.putClientProperty(ORIGINAL_FORWARD_FOCUS_KEYS, keys);
	keys = new HashSet<AWTKeyStroke>(keys);
	keys.add(javax.swing.KeyStroke.getKeyStroke("TAB"));
	table.setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keys);
	keys = table.getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
	//table.putClientProperty(ORIGINAL_BACKWARD_FOCUS_KEYS, keys);
	keys = new HashSet<AWTKeyStroke>(keys);
	keys.add(javax.swing.KeyStroke.getKeyStroke("shift TAB"));
	table.setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, keys);

	table.getAccessibleContext().setAccessibleDescription("Address book. Use arrow up and arrow down to select.");
	Color tableBg = table.getBackground();

	table.setColumnSelectionAllowed(false);
	table.setRowSelectionAllowed(true);
	table.setDefaultRenderer(Object.class, new AddressTableRenderer());
	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	table.setIntercellSpacing(new Dimension(0, 0));
	table.getTableHeader().setReorderingAllowed(false);

	table.setDragEnabled(false);
	table.setShowHorizontalLines(true);
	table.setShowVerticalLines(false);
	table.setGridColor(new Color(tableBg.getRed(),
				     tableBg.getGreen(),
				     tableBg.getBlue()));

	model = new AddressTableModel();
	table.setModel(model);

	JScrollPane scrollPane = new JScrollPane(table);
	scrollPane.setBorder(new ShallowBorder(0, 0, 0, 0));
	scrollPane.getViewport().setBackground(new Color(tableBg.getRed(),
							 tableBg.getGreen(),
							 tableBg.getBlue()));
	table.setPreferredScrollableViewportSize(new Dimension(width, height));

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.weightx = 1.0;
	constraints.weighty = 1.0;
	add(scrollPane, constraints);

	JPanel buttonPanel = new JPanel(new GridLayout(0, 3, 10, 0));
	newButton = new JButton(language.getProperty("se.omnitor.tipcon1." +
						     "gui.AddressPanel." +
						     "NEW_BUTTON"));
	newButton.setMargin(new Insets(0, 5, 0, 5));
	newButton.addActionListener(this);
	buttonPanel.add(newButton);

	editButton = new JButton(language.getProperty("se.omnitor.tipcon1." +
							"gui.AddressPanel." +
							"EDIT_BUTTON"));
	editButton.setMargin(new Insets(0, 5, 0, 5));
	editButton.addActionListener(this);
	buttonPanel.add(editButton);

	deleteButton = new JButton(language.getProperty("se.omnitor.tipcon1." +
						      "gui.AddressPanel." +
						      "DEL_BUTTON"));
	deleteButton.setMargin(new Insets(0, 5, 0, 5));
	deleteButton.addActionListener(this);
	buttonPanel.add(deleteButton);

	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.fill = GridBagConstraints.NONE;
	constraints.insets = new Insets(5, 0, 5, 0);
	add(buttonPanel, constraints);

	this.table.addMouseListener(this);
    }

    public void mouseClicked(MouseEvent e) {
    	Point origin = e.getPoint();
    	int row = table.rowAtPoint(origin);
    	if (row > -1) {
    		if (e.getClickCount() == 1) {
    			listener.addressPanelSelectionChanged((String)data[row][0],
					      (String)data[row][1]);
    		}
    		if (e.getClickCount() == 2) {
    			listener.handleDoubleClick((String)data[row][0],
    					(String)data[row][1]);
    		}
    	}
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void setFocus() {
	table.requestFocus();
    }


    /**
     * Takes care of actions.
     *
     * @param ae The action event.
     */
    public void actionPerformed(ActionEvent ae) {
	Object source = ae.getSource();

	if (source == newButton) {

	    Point location = getLocationOnScreen();
	    AddressTableFormDialog dialog =
		new AddressTableFormDialog(parent,
					   (int)location.getX()+getWidth()/2,
					   (int)location.getY()+getHeight()/2);
	    dialog.setVisible(true);
	}

	if (source == editButton) {
	    if (selectedRow == -1) {
		JOptionPane.showMessageDialog
		    (parent,
		     language.getProperty("se.omnitor.tipcon1.gui." +
					  "AddressPanel.SELECT_ROW_EDIT"),
		     language.getProperty("se.omnitor.tipcon1.gui." +
					  "AddressPanel.EDIT"),
		     JOptionPane.ERROR_MESSAGE);
	    }
	    else {
		Point location = getLocationOnScreen();
		AddressTableFormDialog dialog =
		    new AddressTableFormDialog
			(parent,
			 (int)location.getX()+getWidth()/2,
			 (int)location.getY()+getHeight()/2,
			 selectedRow);

		dialog.setVisible(true);
	    }
	}

	if (source == deleteButton) {
	    if (selectedRow == -1) {
		JOptionPane.showMessageDialog
		    (parent,
		     language.getProperty("se.omnitor.tipcon1.gui." +
					  "AddressPanel.SELECT_ROW_DEL"),
		     language.getProperty("se.omnitor.tipcon1.gui." +
					  "AddressPanel.DELETE"),
		     JOptionPane.ERROR_MESSAGE);
	    }
	    else {
		int result =
		    JOptionPane.showConfirmDialog
		    (parent,
		     language.getProperty("se.omnitor.tipcon1.gui." +
					  "AddressPanel.DEL_CONFIRM") + " '" +
		     data[selectedRow][0] + "'?",
		     language.getProperty("se.omnitor.tipcon1.gui." +
					  "AddressPanel.DELETE"),
		     JOptionPane.YES_NO_OPTION);

		if (result == JOptionPane.YES_OPTION) {
		    model.removeData(selectedRow);
		}
	    }

	}
    }

    boolean firstFocus = true;

    public void focusGained(FocusEvent e) {

	if(firstFocus && table.getRowCount()>0) {
	    table.setRowSelectionInterval(0,0);
	    firstFocus = false;
	}
    }

    public void focusLost(FocusEvent e) {

    }

    private class AddressTableModel extends AbstractTableModel {

	/**
	 * Initializes the table model by reading stored data.
	 *
	 */
	public AddressTableModel() {
	    super();

	    getStoredData();

	}

	/**
	 * Reads data from the address book XML file.
	 *
	 */
	private void getStoredData() {

	    try {
		DocumentBuilderFactory factory =
		    DocumentBuilderFactory.newInstance();

		DocumentBuilder parser = factory.newDocumentBuilder();
		Document doc = parser.parse(filename);
		NodeList nodes = doc.getElementsByTagName("Person");

		int dataCnt = 0;
		int pLength = nodes.getLength();
		data = new Object[pLength][2];
		Node tempNode;
		NodeList nl;
		Node tempNode2;
		String name;
		String address;
		int nlLength;
		int nlcnt;

		for (int pcnt=0; pcnt<pLength; pcnt++) {
		    name = null;
		    address = null;

		    tempNode = nodes.item(pcnt);
		    if (tempNode != null) {

			nl = tempNode.getChildNodes();

			nlLength = nl.getLength();
			for (nlcnt=0; nlcnt<nlLength; nlcnt++) {

			    tempNode2 = nl.item(nlcnt);

			    if (tempNode2.getNodeName().equals("Name")) {
				try {
				    name =
					tempNode2.getFirstChild().
					getNodeValue().trim();
				}
				catch (NullPointerException npe) {
				    name = "";
				}
			    }
			    if (tempNode2.getNodeName().
				equals("SipAddress")) {

				try {
				    address =
					tempNode2.getFirstChild().
					getNodeValue().trim();
				}
				catch (NullPointerException npe) {
				    address = "";
				}
			    }

			}

			if (name != null || address != null) {
			    data[dataCnt][0] = name;
			    data[dataCnt][1] = address;
			    dataCnt++;
			}
		    }
		}

		if (dataCnt != pLength) {
		    Object[][] correctedData = new Object[dataCnt][2];
		    System.arraycopy(data, 0, correctedData, 0, dataCnt);
		    data = correctedData;
		}


	    }
	    catch (IOException e) {
			logger.fine("Could not open address book file " + filename);
			// Couldn't read from file. That's OK.
	    }
	    catch (ParserConfigurationException pce) {
			logger.fine("Could not open address book file " + filename);
			// Couldn't read from file. That's OK.
	    }
	    catch (SAXException se) {
			logger.fine("Could not open address book file " + filename);
			// Couldn't read from file. That's OK.
	    }

	    if (data == null) {
		data = new Object[0][0];
	    }

	    sortData();

	}

	/**
	 * Sorts the rows of data.
	 *
	 */
	private void sortData() {

	    if (data.length < 2) {
		return;
	    }

	    Object[] temp = new Object[2];
	    boolean hasChanged = true;
	    int cnt;
	    int length = data.length;
	    int compRes;


	    // A simple bubblesort

	    while (hasChanged) {
		hasChanged = false;

		for (cnt=1; cnt<length; cnt++) {
		    compRes =
			((String)data[cnt-1][0]).compareToIgnoreCase
			((String)data[cnt][0]);

		    if (compRes > 0 ||
			(compRes == 0 &&
			 ((String)data[cnt-1][1]).compareToIgnoreCase
			 ((String)data[cnt][1]) > 0)) {

			temp[0] = data[cnt][0];
			temp[1] = data[cnt][1];
			data[cnt][0] = data[cnt-1][0];
			data[cnt][1] = data[cnt-1][1];
			data[cnt-1][0] = temp[0];
			data[cnt-1][1] = temp[1];

			if (selectedRow == cnt) {
			    selectedRow--;
			}
			else if (selectedRow == cnt-1) {
			    selectedRow++;
			}

			hasChanged = true;
		    }
		}
	    }

	    if (selectedRow != -1) {
		table.setRowSelectionInterval(selectedRow, selectedRow);
	    }
	}

	/**
	 * Since there are two columns, this function will always return '2'.
	 *
	 * @return 2, always.
	 */
	public int getColumnCount() {
	    return 2;
	}

	/**
	 * Gets the column name of the given column index, the name will
	 * either be "Name" or "SIP address".
	 *
	 * @param columnIndex 0 or 1
	 *
	 * @return "Name" or "SIP address"
	 */
	public String getColumnName(int columnIndex) {
	    if (columnIndex == 0) {
		return language.getProperty("se.omnitor.tipcon1.gui." +
					    "AddressPanel.NAME_HEADER");
	    }

	    if (columnIndex == 1) {
		return language.getProperty("se.omnitor.tipcon1.gui." +
					    "AddressPanel.ADDRESS_HEADER");
	    }

	    return "";
	}

	/**
	 * Gets the number of rows in the table.
	 *
	 * @return The number of rows in the table.
	 */
	public int getRowCount() {
	    return data.length;
	}

	/**
	 * Gets the value of a given cell
	 *
	 * @param rowIndex The cell's row index
	 * @param columnIndex The cell's column index
	 *
	 * @return The data att the given cell
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
	    return data[rowIndex][columnIndex];
	}

	/**
	 * Since all cells are editable, this function always returns true.
	 *
	 * @param rowIndex The cell's row index
	 * @param columnIndex The cell's column index
	 *
	 * @return True, always.
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
	    return false;
	}

	/**
	 * Changes data in the table for a specific cell. The function will
	 * also save the changes to the XML file.
	 *
	 * @param aValue The new value
	 * @param rowIndex The row index for the cell
	 * @param columnIndex The column index for the cell
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	    data[rowIndex][columnIndex] = ((String)aValue).trim();
	    sortData();
	    fireTableDataChanged();
	    listener.addressPanelSelectionChanged
		((String)data[selectedRow][0],
		 (String)data[selectedRow][1]);
	    saveData();

	}

	public void editData(int row, String name, String address) {
	    data[row][0] = name;
	    data[row][1] = address;
	    sortData();
	    fireTableDataChanged();
	    listener.addressPanelSelectionChanged
		((String)data[selectedRow][0],
		 (String)data[selectedRow][1]);
	    saveData();
	}

	/**
	 * Adds new data to the table, it also saves it to the XML file.
	 *
	 * @param name The name field data
	 * @param address The address field data
	 */
	public void addData(String name, String address) {
	    Object[][] newData = new Object[data.length+1][2];

	    System.arraycopy(data, 0, newData, 0, data.length);
	    newData[data.length][0] = name.trim();
	    newData[data.length][1] = address.trim();

	    data = newData;

	    sortData();
	    fireTableDataChanged();
	    selectedRow = -1;
	    saveData();
	}

	/**
	 * Removes a row of data
	 *
	 * @param row The row to remove
	 */
	public void removeData(int row) {
	    Object[][] newData = new Object[data.length-1][2];

	    int newCnt = 0;
	    for (int oldCnt=0; oldCnt<data.length; oldCnt++) {
		if (oldCnt != row) {
		    newData[newCnt][0] = data[oldCnt][0];
		    newData[newCnt][1] = data[oldCnt][1];
		    newCnt++;
		}
	    }

	    data = newData;

	    fireTableDataChanged();
	    selectedRow = -1;
	    saveData();
	}

	/**
	 * Saves all data to an address book XML file.
	 *
	 */
	private void saveData() {

	    File file;
	    FileWriter fw;

	    try {
		file =
		    new File(filename);
		if (file.exists()) {
		    file.delete();
		}

		file.createNewFile();

		fw = new FileWriter(file);
	    }
	    catch (IOException e) {
		logger.throwing(this.getClass().getName(), "saveData", e);
		return;
	    }

	    String docType = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\r";
	    String addressBookStart = "<AddressBook>\n\r";
	    String addressBookEnd = "</AddressBook>\n\r";
	    int addressBookLength = addressBookStart.length();
	    String personStart = "  <Person>\n\r";
	    String personEnd = "  </Person>\n\r";
	    int personLength = personStart.length();
	    String nameStart = "    <Name>";
	    int nameStartLength = nameStart.length();
	    String nameEnd = "</Name>\n\r";
	    int nameEndLength = nameEnd.length();
	    String sipAddressStart = "      <SipAddress>";
	    int sipAddressStartLength = sipAddressStart.length();
	    String sipAddressEnd = "</SipAddress>\n\r";
	    int sipAddressEndLength = sipAddressEnd.length();

	    try {
		fw.write(docType, 0, docType.length());
		fw.write(addressBookStart, 0, addressBookLength);

		String tempStr;

		for (int cnt=0; cnt<data.length; cnt++) {
		    fw.write(personStart, 0, personLength);

		    fw.write(nameStart, 0, nameStartLength);
		    tempStr =
			new String(encodeStringToXml
				   ((String)data[cnt][0]).getBytes("UTF-8"));
		    fw.write(tempStr, 0, tempStr.length());
		    fw.write(nameEnd, 0, nameEndLength);

		    fw.write(sipAddressStart, 0, sipAddressStartLength);
		    tempStr =
			new String(encodeStringToXml
				   ((String)data[cnt][1]).getBytes("UTF-8"));
		    fw.write(tempStr, 0, tempStr.length());
		    fw.write(sipAddressEnd, 0, sipAddressEndLength);

		    fw.write(personEnd, 0, personLength+1);
		}

		fw.write(addressBookEnd, 0, addressBookLength+1);
		fw.close();
	    }
	    catch (Exception e) {
		logger.throwing(this.getClass().getName(), "saveData", e);

		// Cannot write, just continue.
	    }

	}

	/**
	 * Encodes a String to make it suitable for writing to an XML file.
	 *
	 * @param s The String to encode
	 *
	 * @return The encoded String, which is suitable for writing to an XML
	 * file.
	 */
	public String encodeStringToXml(String s) {

	    if (s == null) {
		return "";
	    }

	    String encodedStr = "";
	    int sLen = s.length();

	    for (int i=0; i<sLen; i++) {
		switch (s.charAt(i)) {
		case '&':
		    encodedStr += "&amp;";
		    break;
		case '\'':
		    encodedStr += "&apos;";
		    break;
		case '\"':
		    encodedStr += "&quot;";
		    break;
		case '>':
		    encodedStr += "&gt;";
		    break;
		case '<':
		    encodedStr += "&lt;";
		    break;
		default:
		    encodedStr += s.charAt(i);
		}

	    }

	    return encodedStr;
	}

    }

    /**
     * This class handles the rendering of the table cells.
     *
     * @author Andreas Piirimets, Omnitor AB
     */
    private class AddressTableRenderer implements TableCellRenderer {

	/**
	 * Does nothing.
	 *
	 */
	public AddressTableRenderer() {
	}

	/**
	 * Renders a table cell
	 *
	 * @param table The table
	 * @param value The value to write
	 * @param isSelected Whether the cell is selected
	 * @param hsFocus Whether the cell has focus
	 * @param row Which row the cell is in
	 * @param column Which column the cell is in
	 *
	 * @return The rendered component
	 */
	public Component getTableCellRendererComponent(JTable table,
						       Object value,
						       boolean isSelected,
						       boolean hasFocus,
						       int row,
						       int column) {
	    JLabel label = new JLabel(" " + (String)value);
	    label.setToolTipText((String)data[row][column]);

	    if (isSelected) {
		label.setBackground(Color.BLUE);
		label.setForeground(Color.WHITE);
		label.setOpaque(true);

			if (selectedRow != row) {
				listener.addressPanelSelectionChanged((String)data[row][0],
						(String)data[row][1]);
			}
			selectedRow = row;
	    }
	    else if (hasFocus) {
		label.setBackground(Color.BLUE);
		label.setForeground(Color.WHITE);
		label.setOpaque(true);
	    }


	    return label;
	}
    }

    /**
     * This class is a dialog box asking the user for a new entry to the
     * address book.
     *
     * @author Andreas Piirimets, Omnitor AB
     */
    private class AddressTableFormDialog extends JDialog
	implements ActionListener,
		   KeyListener {

	private JTextField nameTf;
	private JTextField addressTf;
	private JButton okButton;
	private JButton cancelButton;
	private int centerX;
	private int centerY;

	private boolean isEditing;
	private int dataRow;

	/**
	 * Initializes the dialog
	 *
	 * @param parent The parent frame
	 * @param centerX The X coordinate for the center point for the dialog
	 * @param centerY The Y coordinate for the center point for the dialog
	 */
	public AddressTableFormDialog(JFrame parent, int centerX,
				      int centerY) {
	    super(parent,
		  language.getProperty("se.omnitor.tipcon1.gui." +
				       "AddressPanel.NEW_ADDR"),
		  true);

	    this.centerX = centerX;
	    this.centerY = centerY;

	    initComponents();

	    isEditing = false;
	}

	/**
	 * Initializes the dialog as an edit dialog
	 */
	public AddressTableFormDialog(JFrame parent, int centerX,
				      int centerY, int dataRow) {
	    super(parent,
		  language.getProperty("se.omnitor.tipcon1.gui." +
				       "AddressPanel.EDIT_ADDR"),
		  true);

	    this.centerX = centerX;
	    this.centerY = centerY;

	    initComponents();

	    nameTf.setText((String)data[dataRow][0]);
	    addressTf.setText((String)data[dataRow][1]);

	    isEditing = true;
	    this.dataRow = dataRow;
	}

	/**
	 * Initializes all components.
	 *
	 */
	private void initComponents() {
	    getContentPane().setLayout(new GridBagLayout());
	    GridBagConstraints constraints;

	    getContentPane().addKeyListener(this);

	    constraints = new GridBagConstraints();
	    constraints.gridx = 0;
	    constraints.gridy = 0;
	    constraints.anchor = GridBagConstraints.WEST;
	    constraints.insets = new Insets(10, 10, 2, 2);
	    getContentPane().add(new JLabel
		(language.getProperty("se.omnitor.tipcon1.gui.AddressPanel." +
				      "NAME_HEADER")), constraints);

	    nameTf = new JTextField();
	    nameTf.addActionListener(this);
	    nameTf.addKeyListener(this);
	    constraints = new GridBagConstraints();
	    constraints.gridx = 1;
	    constraints.gridy = 0;
	    constraints.fill = GridBagConstraints.HORIZONTAL;
	    constraints.weightx = 1.0;
	    constraints.insets = new Insets(10, 2, 2, 10);
	    getContentPane().add(nameTf, constraints);

	    constraints = new GridBagConstraints();
	    constraints.gridx = 0;
	    constraints.gridy = 1;
	    constraints.anchor = GridBagConstraints.WEST;
	    constraints.insets = new Insets(2, 10, 5, 2);
	    getContentPane().add(new JLabel
		(language.getProperty("se.omnitor.tipcon1.gui.AddressPanel." +
				      "ADDRESS_HEADER")), constraints);

	    addressTf = new JTextField("andreas.piirimets@omnitor.se");
	    addressTf.addActionListener(this);
	    addressTf.addKeyListener(this);
	    constraints = new GridBagConstraints();
	    constraints.gridx = 1;
	    constraints.gridy = 1;
	    constraints.fill = GridBagConstraints.HORIZONTAL;
	    constraints.weightx = 1.0;
	    constraints.insets = new Insets(2, 2, 5, 10);
	    getContentPane().add(addressTf, constraints);

	    JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 10, 0));
	    okButton = new JButton(language.getProperty("se.omnitor.tipcon1." +
							"gui.AddressPanel." +
							"OK_BUTTON"));
	    okButton.addActionListener(this);
	    okButton.addKeyListener(this);
	    okButton.setMargin(new Insets(0, 10, 0, 10));
	    buttonPanel.add(okButton);
	    cancelButton = new JButton(language.getProperty("se.omnitor." +
							    "tipcon1.gui." +
							    "AddressPanel." +
							    "CANCEL_BUTTON"));
	    cancelButton.addActionListener(this);
	    cancelButton.addKeyListener(this);
	    cancelButton.setMargin(new Insets(0, 10, 0, 10));
	    buttonPanel.add(cancelButton);

	    constraints = new GridBagConstraints();
	    constraints.gridx = 0;
	    constraints.gridy = 2;
	    constraints.fill = GridBagConstraints.NONE;
	    constraints.gridwidth = 2;
	    constraints.insets = new Insets(5, 10, 10, 10);
	    getContentPane().add(buttonPanel, constraints);

	    pack();
	    addressTf.setText("");

	    setLocation(centerX - getWidth()/2, centerY - getHeight()/2);
	}

	/**
	 * Takes care of actions
	 *
	 * @param ae The event
	 */
	public void actionPerformed(ActionEvent ae) {
	    Object source = ae.getSource();

	    if (source == okButton ||
		source == nameTf ||
		source == addressTf) {

		String name = nameTf.getText().trim();
		String address = addressTf.getText().trim();

		if (isEditing) {
		    model.editData(dataRow, name, address);
		}
		else {
		    if (!name.equals("") || !address.equals("")) {
			model.addData(name, address);
		    }
		}

	    }

	    dispose();
	}

	/**
	 * Does nothing.
	 *
	 * @param ke The event
	 */
	public void keyTyped(KeyEvent ke) {
	}

	/**
	 * Does nothing.
	 *
	 * @param ke The event
	 */
	public void keyReleased(KeyEvent ke) {
	}

	/**
	 * Takes care of ESC
	 *
	 * @param ke The event
	 */
	public void keyPressed(KeyEvent ke) {

	    if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
		dispose();
	    }

	}

    }

}
