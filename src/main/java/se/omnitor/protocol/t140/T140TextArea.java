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
import java.awt.SystemColor;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.util.logging.Logger;


/**
 * This is a normal TextArea extended with functionality for adding/removing
 * lines. This is to achieve T.140 level synchronization. <br>
 * <br>
 * The synchronization mechanism is partly here and partly in <i>T140GUI</i>
 * class. <br>
 *
 * @author Erik Zetterstrom, Omnitor AB
 * @author Andreas Piirimets, Omnitor AB
 */
public class T140TextArea extends JScrollPane implements KeyListener, AdjustmentListener, ActionListener{

    /**
     * Last char typed in the area. Only set before erasing a char.
     */
    private char erasedCharacter;
    
    private T140EventHandler outHandler;

    /**
     * Specifies whether this text area belongs to the local user or not.
     */
    private boolean isLocal;

    /**
     * Holds information about what text the user has written. Since extra
     * newlines may have been written to the JTextArea by the synchronization
     * functions, we must know what to remove when user presses backspace.
     */
    private StringBuffer writtenText;
    
    private String clipBoardStringData = null;

    /**
     * The JTextArea that this JScrollPane contains.
     */
    protected JTextArea textArea=null;

    private JViewport viewPort=null;
    private Font font;
    private String lostCharacter;
    private Logger logger;

    private Color activeBg;
    private Color inactiveBg;
    private boolean isActiveBg;
    private boolean isScrollingAlwaysEnabled = false;

    private boolean lastAppendedCharWasLostCharacter = false;
    
    private TextNotificationPanel textNotificationPanel;

    private JPopupMenu T140TextAreaPasteMenu;
    private JMenuItem  T140TextAreaPasteMenuItem;
    
    /**
     * Initializes.
     *
     * @param rows    The width of this TextArea.
     * @param columns The height of this TextArea.
     * @param isLocal Defines whether the text area is filled with text written
     * by the local user
     */
    public T140TextArea(T140EventHandler outHandler,int rows, int columns, boolean isLocal, int vertscrollbar){
        super(vertscrollbar, HORIZONTAL_SCROLLBAR_NEVER);
	logger = Logger.getLogger("se.omnitor.protocol.t140");

	this.outHandler = outHandler;
	
	this.isLocal = isLocal;
        textArea = new JTextArea(rows, columns);

	//Handle TAB
	textArea.addKeyListener(this);


        viewPort = new JViewport();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        //textArea.setSize(200,100);
        //viewPort.setSize(200,100);

        font = new Font("Arial Unicode MS",Font.PLAIN,12);
	textArea.setFont(font);

        viewPort.setView(textArea);
        setViewport(viewPort);

        getVerticalScrollBar().setUnitIncrement(textArea.
                                                getFontMetrics(font).
                                                getHeight());

	writtenText = new StringBuffer();

	// If font has lost char (0xFFFD), use it! Otherwise, use the textual
	// representation ("<?>").
	String str = "" + (char)0xFFFD;
	if (font.canDisplayUpTo(str) == -1) {
	    lostCharacter = str;
	}
	else {
	    lostCharacter = "<?>";
	}

	activeBg = SystemColor.info;
	inactiveBg = SystemColor.control;
	isActiveBg = false;

	textArea.setBackground(inactiveBg);


	/*Set keys = textArea.getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
	//table.putClientProperty(ORIGINAL_FORWARD_FOCUS_KEYS, keys);
	keys = new HashSet(keys);
	keys.add(javax.swing.KeyStroke.getKeyStroke("TAB"));
	textArea.setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keys);
	keys = textArea.getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
	//table.putClientProperty(ORIGINAL_BACKWARD_FOCUS_KEYS, keys);
	keys = new HashSet(keys);
	keys.add(javax.swing.KeyStroke.getKeyStroke("shift TAB"));
	textArea.setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, keys);*/
	
		this.getVerticalScrollBar().addAdjustmentListener(this);

		if (isLocal) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
			}
			T140TextAreaPasteMenu = new JPopupMenu();
			T140TextAreaPasteMenuItem = new JMenuItem("Paste");
			T140TextAreaPasteMenu.add(T140TextAreaPasteMenuItem);
			
			addMouseListener(new MouseAdapter(){
				public void mouseReleased(MouseEvent Me){
					if(Me.isPopupTrigger() && isActiveBg){
			        	T140TextAreaPasteMenu.show(Me.getComponent(), Me.getX(), Me.getY());
			        	Clipboard clipboard = getToolkit().getSystemClipboard();
			        	Transferable clipboardContents = clipboard.getContents(this);
			        	DataFlavor flavor[] = clipboardContents.getTransferDataFlavors();
			        	if (flavor == null || flavor.length == 0) {
			        		//	clip board is empty
			        		T140TextAreaPasteMenuItem.setEnabled(false);
			        	} else {
			        		//	 clip board has content.	        		
			        		T140TextAreaPasteMenuItem.setEnabled(true);
			        		try
			        		{
			        			Object data = clipboardContents.getTransferData(DataFlavor.stringFlavor);
			        			clipBoardStringData = data.toString();
			        		}
			        		catch(IOException e)
			        		{
			        			logger.throwing(this.getClass().getName(), "IOException", e);
			        		}
			        		catch(UnsupportedFlavorException e)
			        		{
			        			logger.throwing(this.getClass().getName(), "UnsupportedFlavorException", e);
			        		}
			        	}
			        }
				}
			});
			T140TextAreaPasteMenuItem.addActionListener(this);
		}
	}
	
	public void actionPerformed(ActionEvent ae){
		textArea.paste();
		outHandler.newEvent(new T140Event(T140Event.TEXT, clipBoardStringData));
	}
		
	public JMenuItem getCallControlMenuItem(){
		return T140TextAreaPasteMenuItem;
	}
	
	public JPopupMenu CallControlMenu(){
		return T140TextAreaPasteMenu;
	}

    /**
     * Sets the accessible description of this text area,
     *
     * @param desc The description.
     */
    public void setAccessibleDescription(String desc) {
	textArea.getAccessibleContext().setAccessibleDescription(desc);
    }

	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		
		int width = 20;
		int height = 150;
		if (d != null) {
			width = (int)d.getWidth();
		}
		
		return new Dimension(width, height);
	}
    /**
     * Sets the focus of this text area.
     */
    public void setFocus() {
	textArea.requestFocus();
    }

    public void keyPressed(KeyEvent e) {
    
	if(e.getKeyCode() == KeyEvent.VK_TAB) {
		if(e.isShiftDown()) {
		javax.swing.FocusManager.getCurrentManager().focusPreviousComponent();
	    } else {
		javax.swing.FocusManager.getCurrentManager().focusNextComponent();
	    }
	    e.consume();
	}
    }

    public void keyReleased(KeyEvent e) {
	if(e.getKeyCode() == KeyEvent.VK_TAB) {
	    e.consume();
	}
    }

    public void keyTyped(KeyEvent e) {
	if(e.getKeyCode() == KeyEvent.VK_TAB) {
	    e.consume();
	}
    }

    /**
     * Gets the font of the text area.
     *
     * @return The font used.
     */
    public Font getFont() {
	if(textArea!=null) {
	    return textArea.getFont();
	}
	return null;
    }

    /**
     * Gets the foreground color used in the text area.
     *
     * @return The foreground color used.
     */
    public Color getForeground() {
	if(textArea!=null) {
	    return textArea.getForeground();
	}
	return null;
    }

    /**
     * Gets the background color used in this text area.
     *
     * @return The background color used.
     */
    public Color getBackground() {
	return activeBg;
    }
    
    /**
     * Sets the font of this text area.
     *
     * @param f The font to use.
     */
    public void setFont(Font f) {
	if(textArea!=null) {
	    textArea.setFont(f);
	}
    }

    /**
     * Sets the foreground color of this text area.
     *
     * @param c The color to use.
     */
    public void setForeground(Color c) {
	if(textArea!=null) {
	    textArea.setForeground(c);
	}
    }

    /**
     * Sets the background color of this text area.
     *
     * @param c The color to use.
     */
    public void setBackground(Color c) {
	activeBg = c;

	int r = (c.getRed() + 3*SystemColor.control.getRed()) / 4;
	int g = (c.getGreen() + 3*SystemColor.control.getGreen()) / 4;
	int b = (c.getBlue() + 3*SystemColor.control.getBlue()) / 4;
	inactiveBg = new Color(r, g, b);

	if(textArea!=null) {
	    if (isActiveBg) {
		textArea.setBackground(activeBg);
	    }
	    else {
		textArea.setBackground(inactiveBg);
	    }
	}
    }

    /**
     * Appends the "lost character" to the text area. This is a special
     * character, which should be shown when there are missing data. This
     * character may not be available in all fonts. If the character is
     * unavailable, the following is appended instead: "<?>".
     *
     */
    public void appendLostCharacter() {
	textArea.append(lostCharacter);
    }

    /**
     * Set the number of rows in the text area
     *
     * @param rows The number of rows to use
     */
    public void setRows(int rows) {
	textArea.setRows(rows);
    }

    /**
     * Set the number of columns in the text areas
     *
     * @param columns the number of columns
     */
    public void setColumns(int columns) {
	textArea.setColumns(columns);
    }


    /**
     * Gets the number of columns of the text area.
     *
     * @return The number of columns.
     */
    public int getColumns() {
	return textArea.getColumns();
    }

    /**
     * Sets the position of the caret to the end of the text.
     *
     */
    public void setEndCaretPosition() {
    	if (isScrollingEnabled()) {
	textArea.setCaretPosition(textArea.getText().length());
    	}
    }
    
    public void setEndCaretPositionForced() {
    	textArea.setCaretPosition(textArea.getText().length());
    }
  
    

    /**
     * Sets if the textarea should be editable or not.
     *
     * @param state True for editable, false otherwise.
     */
    public void setEditable(boolean state) {
    	textArea.setEditable(state);
    }

    /**
     * Returns a boolean indicating if this component is editable or not.
     *
     * @return The boolean value
     */
    public boolean isEditable() {
	return textArea.isEditable();
    }

    /**
     * Sets if the text area should have an inactive look or not.
     *
     * @param state True for active look, false for inactive look
     */
    public void setActiveLook(boolean state) {
	if (state) {
	    isActiveBg = true;
	    textArea.setBackground(activeBg);
	    if (isLocal) {
		textArea.requestFocus();
	    }
	}
	else {
	    isActiveBg = false;
	    textArea.setBackground(inactiveBg);
	}
    }

    /**
     * Set a KeyListener to the textarea.
     *
     * @param kl The KeyListener.
     */
    public void addKeyListener(KeyListener kl) {
	textArea.addKeyListener(kl);
    }

    /**
     * Fetches the text of the textarea.
     *
     * @return The text
     */
    public String getText() {
	return textArea.getText();
    }

    /**
     * Sets the text area to contain only this text.
     *
     * @param txt The text to set.
     */
    public void setText(String txt) {
	textArea.setText(txt);
	if (isScrollingEnabled()) {
        textArea.setCaretPosition(textArea.getText().length());
	}
    }

    /**
     * Appends text to the textarea.
     *
     * @param txt The text to append.
     */
    public void append(String txt) {

	if ((int)txt.charAt(0) == 0xFFFD) {
	    if (!lastAppendedCharWasLostCharacter ) {

		textArea.append(lostCharacter);
		lastAppendedCharWasLostCharacter = true;
	    }
	}
	else {
	    textArea.append(txt);
	    lastAppendedCharWasLostCharacter = false;
	}
	
	if ((textNotificationPanel != null) && !isScrollingEnabled()) {
		textNotificationPanel.setShow(true, txt);
	}

    }

    public String getAllText() {
	    String tmptext = writtenText.toString();
	    writtenText.delete(0, writtenText.length());
	    textArea.setText("");
	    return tmptext;
    }

    public String getWText() {
	    return writtenText.toString();
    }

    public void setWText(String s) {
        writtenText = new StringBuffer(s);
    }


    public void setAllText(String txt) {
        writtenText.delete(0, writtenText.length());
        writtenText.append(txt);
        textArea.setText(writtenText.toString());
    }


    /**
     * Synchs this TextArea to a given row.
     * Adds \n until this TextArea has reached the given row.
     *
     * @param rowNumber The row to sync to.
     */
    public void syncToRow(int rowNumber) {
	int lastSentenceDelimiter =
	    getLastSentenceDelimiter(textArea.getText());
	int cpos = getCurrentPosition();
	boolean isScroll = isScrollingEnabled();
	
	while (cpos < rowNumber) {
	    try {
		// Append new lines at the end if the last written character
		// is a new line
		if (!writtenText.toString().equals("") &&
			writtenText.charAt(writtenText.length()-1) == '\n') {

		    textArea.append("\n");
		}

		//we have a sentence delimiter somewhere in the text
		else if (lastSentenceDelimiter <
			 textArea.getText().length()-1) {
		    textArea.insert("\n",lastSentenceDelimiter + 1);
		}
		else {
		    textArea.append("\n");
		}
		if (isScroll) {
			textArea.setCaretPosition(textArea.getText().length());
		}
	    }
	    catch(Exception e) {
		logger.throwing(this.getClass().getName(), "syncToRow", e);
	    }
	    cpos=getCurrentPosition();
	}//end while
    }//end method syncToRow


    /**
     * Gets the line number of the caret position in the TextArea.
     *
     * @return The current line number.
     */
    public int getCurrentPosition() {
/*
    int position = 0;
	char[] txt = textArea.getText().toCharArray();
	int dot = textArea.getText().length();
	int rowLength = 0;
	int numberOfRows=0;

	for (int i=0;i<txt.length;i++) {
	    //complete row
	    if (txt[i]=='\n') {
		if (rowLength>columns) {
		    numberOfRows+=(int)(rowLength/columns)+1;
		}
		else {
		    numberOfRows++;
		}
		rowLength=0;;
	    } else {
		rowLength++;
	    }
	}
	
	return numberOfRows;
*/
    	/*
    	int lines = 0;
    	javax.swing.text.View view = textArea.getUI().getRootView(textArea).getView(0);
    	int paragraphs = view.getViewCount();
    	for (int cnt=0; cnt<paragraphs; cnt++) {
    		lines += view.getView(cnt).getViewCount();
    	}
    	
    	return lines;
    	*/
    	
    	int height = textArea.getFontMetrics(textArea.getFont()).getHeight();
    	
    	//return textArea.getLineCount()+1;
    	return (int)(textArea.getPreferredSize().getHeight() / height);
    }//end method getCurrentPosition

    
    public void setScrollingAlwaysEnabled(boolean value) {
    	isScrollingAlwaysEnabled = value;
    }
	private boolean isScrollingEnabled() {
		if (isScrollingAlwaysEnabled) {
			return true;
		}
		JScrollBar scrollBar = this.getVerticalScrollBar();

		// If scrollbar is not in use (due to text has not reached end of text area), enabled scroll
		if (scrollBar.getModel().getExtent() == scrollBar.getMaximum()) {
			return true;
		}
		
		int rowHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
		
		return (scrollBar.getValue() + scrollBar.getVisibleAmount() + 2*rowHeight) > scrollBar.getMaximum(); 
	}
	
	

    /**
     * Erases all \n at the end of the text back
     * to the last sentence delimiter.
     *
     */
    public void eraseAutoNewlines() {
    	if (writtenText.length() == 0) {
    		textArea.setText("");
    	}
    	else if (writtenText.charAt(writtenText.length()-1) == '\n') {
    		int wcnt = 0;
    		int walker = writtenText.length()-1;
    		while (writtenText.charAt(walker) == '\n') {
    			wcnt++;
    			walker--;
    		}
    		
    		int tcnt = 0;
    		String taText = textArea.getText();
    		walker = taText.length()-1;
    		while (taText.charAt(walker) == '\n') {
    			tcnt++;
    			walker--;
    		}
    		
    		int diff = tcnt - wcnt;
    		for (int cnt=0; cnt<diff; cnt++) {
    			removeLastGuiCharacter();
    		}
    	}
    	else {
    		String taText = textArea.getText();
    		while (taText.charAt(taText.length()-1) == '\n') {
    			System.out.println("TaText = \"" + taText + "\"");
    			taText = taText.substring(0, taText.length()-1);
    		}
    		textArea.setText(taText);
    	}
    	/*
	int n = 0;
	boolean endsWithReturn = textArea.getText().endsWith("\n");

	// If there is no characters left typed by the user, move the caret
	// to the top of the text area. Trust the sync functionality to move
	// the caret to the right position, still it's good to move the caret
	// to the top here in case top is the right position. Else, it will
	// never get there.
	if (writtenText.length() == 0) {
	    textArea.setText("");
	    return;
	}

        // If the last character typed was not a new line character, remove
	// the last character from the GUI also.
	if (endsWithReturn) {
	    //removeLastGuiCharacter();
	}

	// If there is only one character and it's a new line, remove it and
	// move up the caret. Trust the sync function to set the caret at the
	// right position later. The last line may have been typed by this
	// user when he/she pressed ENTER.
	else if (writtenText.length() == 1) {
	    removeLastGuiCharacter();
	    removeLastWrittenTextCharacter();
	    return;
	}

	// If there is two new line characters in a row, remove it from GUI
	// but don't move down last sentence.
	else if (writtenText.charAt(writtenText.length()-2) == '\n') {
	    removeLastGuiCharacter();
	    removeLastWrittenTextCharacter();
	    return;
	}

        // Move down last sentence!
        while (endsWithReturn) {
            String newText = new String(textArea.getText().toCharArray(),
                                        0,
                                        textArea.getText().length() - 1);
            textArea.setText(newText);
            n++;
            endsWithReturn = textArea.getText().endsWith("\n");
        }


	int lastSentenceDelimiter =
	    getLastSentenceDelimiter(textArea.getText());

	for (int i = 0; i<n; i++) {
	    try {
		textArea.insert("\n",lastSentenceDelimiter + 1);
	    }
	    catch (Exception e) {
		logger.throwing(this.getClass().getName(),
				"eraseAutoNewLines", e);
	    }
	}

	removeLastWrittenTextCharacter();
*/
    }//end method eraseAutoNewlines

    /**
     * Gets the last sentence delimiter. A delimiter is one of the following:
     * <br><br>
     * . <br>
     * : <br>
     * ; <br>
     * ! <br>
     * ? <br>
     * * <br>
     * <newline> <br>
     *
     * @param text The string to search for delimiters in
     *
     * @return The position in the string where the last delimiter was found.
     */
    private int getLastSentenceDelimiter(String text) {
	int textEnd = text.length();

	/*
	while (textEnd > 0 &&
	       (text.charAt(textEnd-1) == '.' ||
		text.charAt(textEnd-1) == ':' ||
		text.charAt(textEnd-1) == ';' ||
		text.charAt(textEnd-1) == '!' ||
		text.charAt(textEnd-1) == '?' ||
		text.charAt(textEnd-1) == '*' ||
		text.charAt(textEnd-1) == '\n')) {

	    textEnd--;
	}
	*/

	int lsd =
	    Math.max
	    (text.lastIndexOf('.', textEnd-1),
	     Math.max
	     (text.lastIndexOf(':', textEnd-1),
	      Math.max
	      (text.lastIndexOf(';', textEnd-1),
	       Math.max
	       (text.lastIndexOf('!', textEnd-1),
		Math.max
		(text.lastIndexOf('?', textEnd-1),
		 Math.max
		 (text.lastIndexOf('*', textEnd-1),
		  text.lastIndexOf('\n', textEnd-1)))))));

	// Put the new lines after space characters, but not on the first row.
	if (lsd != -1 && (lsd+1)<(text.length()-1)) {
	    while (text.charAt(lsd+1) == ' ') {
		lsd++;
	    }
	}
	return lsd;
    }

    /**
     * Removes the last character in the text area.
     *
     */
    public void removeLastGuiCharacter(){
	// Unfortunately, I did not find any easier way to do it.
	String txt = textArea.getText();
	int length = txt.length();

	if (length>0) {
	    String newText = new String(txt.toCharArray(),0,length - 1);
	    textArea.setText(newText);
	}
    }


    /**
     * Adds a character as a written character
     *
     * @param chr The character to add
     */
    public void addWrittenText(char chr) {
	writtenText.append(chr);
    }

    /**
     * Removes the last character in the written text.
     *
     */
    public void removeLastWrittenTextCharacter() {
        if (writtenText.length() > 0) {
            writtenText = writtenText.deleteCharAt(writtenText.length() - 1);
        }
    }


    /**
     * Adds a mouse listener to the text area
     *
     * @param listener The listener to add
     */
    public void addMouseListener(MouseListener listener) {
	textArea.addMouseListener(listener);
    }

	protected void pushDownScrollBar() {
		Runnable doScroll = new T140TextAreaScroller(this.getVerticalScrollBar());
		SwingUtilities.invokeLater(doScroll);
	}

	class T140TextAreaScroller implements Runnable {
		private JScrollBar sb;
		
		public T140TextAreaScroller(JScrollBar sb) {
			this.sb = sb;
		}
		
		public void run() {
			sb.setValue(sb.getMaximum());
		}
	}
	
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if ((textNotificationPanel != null) && isScrollingEnabled()) {
			textNotificationPanel.setShow(false, null);
		}
		
	}
	
	public void setTextNotificationPanel(TextNotificationPanel panel) {
		this.textNotificationPanel = panel;
	}
	
	public void setErasedCharacter(char erased) {
		this.erasedCharacter = erased;
	}
	
	public char getErasedCharacter() {
		return erasedCharacter;
	}
}
