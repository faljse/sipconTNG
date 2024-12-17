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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.AWTKeyStroke;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


import se.omnitor.tipcon1.sip.SipRegistrarInfo;


/**
 * This class hold a fully functional address book.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class SipRegistrationPanel extends JPanel implements ActionListener,
                                                            FocusListener,
							    MouseListener {

    private JTable table;
    private AddressTableModel model;
    private JButton newButton;
    private JButton changeButton;
    private JButton deleteButton;
    private JFrame parent;
    private int selectedRow;

    private Object[][] data;

    private Properties language;

    private SipRegistrationPanelListener listListener;



    /**
     * Initializes the panel.
     *
     * @param parentFrame The parent frame
     * @param listener The addres panel listener to use
     * @param width The panel's width
     * @param height The panel's height
     */
    public SipRegistrationPanel(JFrame parentFrame,
				int width,
				int height,
				Properties language,
				SipRegistrarInfo[] sipRegistrarInfo) {

	super(new GridBagLayout());

	this.language = language;

	GridBagConstraints constraints;

	this.parent = parentFrame;
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

	table.getAccessibleContext().setAccessibleDescription("SIP registrations. Use arrow up and arrow down to select.");


	Color tableBg = table.getBackground();

	table.setColumnSelectionAllowed(false);
	table.setRowSelectionAllowed(true);
	table.setDefaultRenderer(Object.class, new AddressTableRenderer());
	table.setDefaultEditor(Object.class, new AddressTableEditor());
	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	table.setIntercellSpacing(new Dimension(0, 0));
	table.getTableHeader().setReorderingAllowed(false);

	table.setDragEnabled(false);
	table.setShowHorizontalLines(true);
	table.setShowVerticalLines(false);
	table.setGridColor(new Color(tableBg.getRed(),
				     tableBg.getGreen(),
				     tableBg.getBlue()));

	model = new AddressTableModel(sipRegistrarInfo);
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
	newButton.setMargin(new Insets(0, 10, 0, 10));
	newButton.addActionListener(this);
	buttonPanel.add(newButton);
	changeButton = new JButton(language.getProperty("se.omnitor.tipcon1." +
							"gui.AddressPanel." +
							"EDIT_BUTTON"));
	changeButton.setMargin(new Insets(0, 10, 0, 10));
	changeButton.addActionListener(this);
	buttonPanel.add(changeButton);
	deleteButton = new JButton(language.getProperty("se.omnitor.tipcon1." +
							"gui.AddressPanel." +
							"DEL_BUTTON"));
	deleteButton.setMargin(new Insets(0, 10, 0, 10));
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

    boolean firstFocus = true;

    public void focusGained(FocusEvent e) {

	if(firstFocus && table.getRowCount()>0) {
	    table.setRowSelectionInterval(0,0);
	    firstFocus = false;
	}
    }

    public void focusLost(FocusEvent e) {

    }

    public void mouseClicked(MouseEvent e) {
	if (e.getClickCount() == 2) {
	    Point origin = e.getPoint();
	    int row = table.rowAtPoint(origin);

	    if (row > -1) {
		Point location = getLocationOnScreen();
		SipTableInfoDialog dialog =
		    new SipTableInfoDialog(parent,
					   (int)location.getX()+getWidth()/2,
					   (int)location.getY()+getHeight()/2,
					   row);
		dialog.setVisible(true);
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

    /**
     * Gets all registrars
     *
     * @return All registrars
     */
    public SipRegistrarInfo[] getTableData() {

	SipRegistrarInfo[] registrars = new SipRegistrarInfo[data.length];

	for (int cnt=0; cnt<data.length; cnt++) {
	    registrars[cnt] = new SipRegistrarInfo((String)data[cnt][0],
						   (String)data[cnt][1],
						   (String)data[cnt][2],
						   (String)data[cnt][3]);
	}

	return registrars;
    }

    public void addListListener(SipRegistrationPanelListener l) {
    	listListener = l;
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
	    SipTableInfoDialog dialog =
		new SipTableInfoDialog(parent,
				       (int)location.getX()+getWidth()/2,
				       (int)location.getY()+getHeight()/2);
	    dialog.setVisible(true);
	}

	if (source == changeButton) {
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
		SipTableInfoDialog dialog =
		    new SipTableInfoDialog(parent,
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
		    listListener.sipRegistrationListChanged();
		}
	    }

	}
    }
	public void delete(int row)
	{
		model.removeData(row);
		
	}


    private class AddressTableModel extends AbstractTableModel {

	/**
	 * Initializes the table model by reading stored data.
	 *
	 */
	public AddressTableModel(SipRegistrarInfo[] info) {
	    super();

	    if (info == null) {
		data = new Object[0][4];
	    }
	    else {

		data = new Object[info.length][4];

		for (int cnt=0; cnt<info.length; cnt++) {
		    data[cnt][0] = info[cnt].getSipAddress();
		    data[cnt][1] = info[cnt].getRegistrarHost();
		    data[cnt][2] = info[cnt].getUsername();
		    data[cnt][3] = info[cnt].getPassword();
		}

		sortData();
	    }

	}

	/**
	 * Sorts the rows of data.
	 *
	 */
	private void sortData() {

	    if (data.length < 2) {
		return;
	    }

	    Object[] temp = new Object[4];
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
			temp[2] = data[cnt][2];
			temp[3] = data[cnt][3];
			data[cnt][0] = data[cnt-1][0];
			data[cnt][1] = data[cnt-1][1];
			data[cnt][2] = data[cnt-1][2];
			data[cnt][3] = data[cnt-1][3];
			data[cnt-1][0] = temp[0];
			data[cnt-1][1] = temp[1];
			data[cnt-1][2] = temp[2];
			data[cnt-1][3] = temp[3];

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
	 * Since there are four columns, this function will always return '4'.
	 *
	 * @return 4, always.
	 */
	public int getColumnCount() {
	    return 4;
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
	    switch (columnIndex) {
	    case 0:
		/*
		return language.getProperty("se.omnitor.tipcon1.gui." +
					    "AddressPanel.NAME_HEADER");
		*/
		return language.getProperty("se.omnitor.tipcon1.gui." +
					    "SipRegistrationPanel." +
					    "SIP_ADDRESS");

	    case 1:
		/*
		  return language.getProperty("se.omnitor.tipcon1.gui." +
		  "AddressPanel.ADDRESS_HEADER");
		*/
		return language.getProperty("se.omnitor.tipcon1.gui." +
					    "SipRegistrationPanel.REGISTRAR");

	    case 2:
		return language.getProperty("se.omnitor.tipcon1.gui." +
					    "SipRegistrationPanel.USER_NAME");

	    case 3:
		return language.getProperty("se.omnitor.tipcon1.gui." +
					    "SipRegistrationPanel.PASSWORD");

	    default:
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
	 * Since no cells are editable, this function always returns false.
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
	    /*
	    listener.addressPanelSelectionChanged
		((String)data[selectedRow][0],
		 (String)data[selectedRow][1]);
	    */
	    //saveData();

	}

	/**
	 * Adds new data to the table, it also saves it to the XML file.
	 *
	 * @param name The name field data
	 * @param address The address field data
	 */
	public void addData(String sipAddress,
			    String registrarAddress,
			    String username,
			    String password) {
	    Object[][] newData = new Object[data.length+1][4];

	    System.arraycopy(data, 0, newData, 0, data.length);
	    newData[data.length][0] = sipAddress.trim();
	    newData[data.length][1] = registrarAddress.trim();
	    newData[data.length][2] = username.trim();
	    newData[data.length][3] = password.trim();

	    data = newData;

	    sortData();
	    fireTableDataChanged();
	    selectedRow = -1;
	    //saveData();
	}

	/**
	 * Edits a row of data, sorts and saves
	 *
	 * @param editRow The row to edit
	 * @param sipAddress The new SIP address
	 * @param registrarHost The new registrar host name
	 * @param username The new username
	 * @param password The new password
	 */
	public void editData(int editRow,
			     String sipAddress,
			     String registrarHost,
			     String username,
			     String password) {

	    data[editRow][0] = sipAddress;
	    data[editRow][1] = registrarHost;
	    data[editRow][2] = username;
	    data[editRow][3] = password;

	    sortData();
	    fireTableDataChanged();
	    table.changeSelection(selectedRow, 0, false, false);
	    //selectedRow = editRow;
	}

	/**
	 * Removes a row of data
	 *
	 * @param row The row to remove
	 */
	public void removeData(int row) {
	    Object[][] newData = new Object[data.length-1][4];

	    int newCnt = 0;
	    for (int oldCnt=0; oldCnt<data.length; oldCnt++) {
		if (oldCnt != row) {
		    newData[newCnt][0] = data[oldCnt][0];
		    newData[newCnt][1] = data[oldCnt][1];
		    newData[newCnt][2] = data[oldCnt][2];
		    newData[newCnt][3] = data[oldCnt][3];
		    newCnt++;
		}
	    }

	    data = newData;

	    fireTableDataChanged();
	    selectedRow = -1;
	    //saveData();
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

	    JLabel label;

	    if (column == 3) {
		label = new JLabel("***********");
	    }
	    else {
		label = new JLabel(" " + (String)value);
		label.setToolTipText((String)data[row][column]);
	    }

	    if (isSelected) {
		label.setBackground(Color.BLUE);
		label.setForeground(Color.WHITE);
		label.setOpaque(true);

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
     * This class prevents cell editing by specifying all cells as
     * non-editable.
     *
     * @author Andreas Piirimets, Omnitor AB
     */
    private class AddressTableEditor extends AbstractCellEditor
	implements TableCellEditor {

	/**
	 * Does nothing.
	 *
	 * @param table The table
	 * @param value The value to put in the component
	 * @param isSelected Whether the component should be selected
	 * @param row The row of the component
	 * @param column The column of the component
	 *
	 * @return null
	 */
	public Component getTableCellEditorComponent(JTable table,
						     Object value,
						     boolean isSelected,
						     int row,
						     int column) {

	    return null;
	}

	/**
	 * Does nothing.
	 *
	 * @return null
	 */
	public Object getCellEditorValue() {
	    return null;
	}

	/**
	 * Checks if a cell is editable. All cells are ediable, this function
	 * will always return true.
	 *
	 * @return True, always.
	 */
	public boolean isCellEditable(EventObject anEvent) {
	    return false;
	}

    }

    /**
     * This class is a dialog box asking the user for a new entry to the
     * address book.
     *
     * @author Andreas Piirimets, Omnitor AB
     */
    private class SipTableInfoDialog extends JDialog
	implements ActionListener, KeyListener, FocusListener {

	private JTextField sipAddressTf;
	private JTextField registrarHostTf;
	private JTextField usernameTf;
	private JTextField passwordTf;
	private JButton okButton;
	private JButton cancelButton;
	private int centerX;
	private int centerY;

	private boolean isAddDialog;
	private int editRow;

	/**
	 * Initializes the dialog as a add dialog
	 *
	 * @param parent The parent frame
	 * @param centerX The X coordinate for the center point for the dialog
	 * @param centerY The Y coordinate for the center point for the dialog
	 */
	public SipTableInfoDialog(JFrame parent, int centerX, int centerY) {
	    super(parent,
		  /*
		  language.getProperty("se.omnitor.tipcon1.gui." +
		  "AddressPanel.NEW_ADDR"), */
		  language.getProperty("se.omnitor.tipcon1.gui." +
				       "SipRegistrationPanel.NEW_REG"),
		  true);

	    isAddDialog = true;

	    this.centerX = centerX;
	    this.centerY = centerY;

	    initComponents();
	}

	/**
	 * Initializes the dialog as an edit dialog
	 *
	 * @param parent The parent frame
	 * @param centerX The X coordinate for the center point for the dialog
	 * @param centerY The Y coordinate for the center point for the dialog
	 * @param editRow The data row to be edited
	 */
	public SipTableInfoDialog(JFrame parent, int centerX, int centerY,
				  int editRow) {
	    super(parent, language.getProperty("se.omnitor.tipcon1.gui." +
					       "SipRegistrationPanel." +
					       "EDIT_REG"),
		  true);

	    isAddDialog = false;
	    this.editRow = editRow;

	    this.centerX = centerX;
	    this.centerY = centerY;

	    initComponents();

	    sipAddressTf.setText((String)data[editRow][0]);
	    registrarHostTf.setText((String)data[editRow][1]);
	    usernameTf.setText((String)data[editRow][2]);
	    passwordTf.setText((String)data[editRow][3]);
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
	    		(language.getProperty("se.omnitor.tipcon1.gui." +
	  	      "SipRegistrationPanel.SIP_ADDRESS")),
		 constraints);

	    sipAddressTf = new JTextField();
	    int prefH = (int)sipAddressTf.getPreferredSize().getHeight();
	    sipAddressTf.setPreferredSize(new Dimension(200, prefH));
	    sipAddressTf.addActionListener(this);
	    sipAddressTf.addKeyListener(this);
	    sipAddressTf.addFocusListener(this);
	    constraints = new GridBagConstraints();
	    constraints.gridx = 1;
	    constraints.gridy = 0;
	    constraints.fill = GridBagConstraints.HORIZONTAL;
	    constraints.weightx = 1.0;
	    constraints.insets = new Insets(10, 2, 2, 10);
	    getContentPane().add(sipAddressTf, constraints);

	    constraints = new GridBagConstraints();
	    constraints.gridx = 0;
	    constraints.gridy = 1;
	    constraints.anchor = GridBagConstraints.WEST;
	    constraints.insets = new Insets(2, 10, 2, 2);
	    getContentPane().add(new JLabel
		(language.getProperty("se.omnitor.tipcon1.gui." +
				      "SipRegistrationPanel.REGISTRAR")),
				 constraints);

	    registrarHostTf = new JTextField();
	    registrarHostTf.addActionListener(this);
	    registrarHostTf.addKeyListener(this);
	    constraints = new GridBagConstraints();
	    constraints.gridx = 1;
	    constraints.gridy = 1;
	    constraints.fill = GridBagConstraints.HORIZONTAL;
	    constraints.weightx = 1.0;
	    constraints.insets = new Insets(2, 2, 2, 10);
	    getContentPane().add(registrarHostTf, constraints);

	    constraints = new GridBagConstraints();
	    constraints.gridx = 0;
	    constraints.gridy = 2;
	    constraints.anchor = GridBagConstraints.WEST;
	    constraints.insets = new Insets(2, 10, 2, 2);
	    getContentPane().add(new JLabel
		(language.getProperty("se.omnitor.tipcon1.gui." +
				      "SipRegistrationPanel.USER_NAME")),
				 constraints);

	    usernameTf = new JTextField();
	    usernameTf.addActionListener(this);
	    usernameTf.addKeyListener(this);
	    constraints = new GridBagConstraints();
	    constraints.gridx = 1;
	    constraints.gridy = 2;
	    constraints.fill = GridBagConstraints.HORIZONTAL;
	    constraints.weightx = 1.0;
	    constraints.insets = new Insets(2, 2, 2, 10);
	    getContentPane().add(usernameTf, constraints);

	    constraints = new GridBagConstraints();
	    constraints.gridx = 0;
	    constraints.gridy = 3;
	    constraints.anchor = GridBagConstraints.WEST;
	    constraints.insets = new Insets(2, 10, 2, 2);
	    getContentPane().add(new JLabel
		(language.getProperty("se.omnitor.tipcon1.gui." +
				      "SipRegistrationPanel.PASSWORD")),
				 constraints);

	    passwordTf = new JPasswordField();
	    passwordTf.addActionListener(this);
	    passwordTf.addKeyListener(this);
	    constraints = new GridBagConstraints();
	    constraints.gridx = 1;
	    constraints.gridy = 3;
	    constraints.fill = GridBagConstraints.HORIZONTAL;
	    constraints.weightx = 1.0;
	    constraints.insets = new Insets(2, 2, 2, 10);
	    getContentPane().add(passwordTf, constraints);

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
	    constraints.gridy = 4;
	    constraints.fill = GridBagConstraints.NONE;
	    constraints.gridwidth = 2;
	    constraints.insets = new Insets(10, 10, 10, 10);
	    getContentPane().add(buttonPanel, constraints);

	    pack();

	    setResizable(false);

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
		source == sipAddressTf ||
		source == registrarHostTf ||
		source == usernameTf ||
		source == passwordTf) {

		if (isAddDialog) {
		    model.addData(sipAddressTf.getText().trim(),
				  registrarHostTf.getText().trim(),
				  usernameTf.getText().trim(),
				  passwordTf.getText().trim());
		}
		else {
		    model.editData(editRow,
				   sipAddressTf.getText().trim(),
				   registrarHostTf.getText().trim(),
				   usernameTf.getText().trim(),
				   passwordTf.getText().trim());
		}
	    }

	    if (listListener != null) {
	    	listListener.sipRegistrationListChanged();
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
	
	/**
	 * Does nothing.
	 * 
	 * @param e The event
	 */
    public void focusGained(FocusEvent e) {
    	// Does nothing
    }

    /**
     * Only used for the top address field
     * 
     * @param e The event
     */
    public void focusLost(FocusEvent e) {
    	if (e.getComponent() == sipAddressTf) {
    		String text = sipAddressTf.getText();
    		
    		if (text != null && !text.equals("")) {
    			String spl[] = text.split("@");
    			if (spl.length == 2) {
    				if (usernameTf.getText().equals("")) {
    					usernameTf.setText(spl[0]);
    				}
    				if (registrarHostTf.getText().equals("")) {
    					registrarHostTf.setText(spl[1]);
    				}
    			}
    		}
    		
    	}

    }
    }
    

}
