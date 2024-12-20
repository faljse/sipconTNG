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

import java.awt.SystemColor;
import java.awt.LayoutManager;

import javax.swing.JTextPane;
import javax.swing.JViewport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class T140LogArea extends JScrollPane {
	
	protected String remoteUsername = "Remote";
	private JTextPane logArea;
	private JPanel logPanel;
	private T140LogAreaRowHandler rowHandler;
	
	
	public T140LogArea(int rows, int columns, boolean isLocal, int vertscrollbar) {
        super(vertscrollbar, HORIZONTAL_SCROLLBAR_NEVER);

        logArea = new JTextPane();
        
        logPanel = new T140LogAreaPanel(new BorderLayout());
        logPanel.add(logArea, BorderLayout.SOUTH);

        JViewport viewPort = new JViewport();
        viewPort.setView(logPanel);
		setViewport(viewPort);

        StyledDocument doc = new DefaultStyledDocument();
        
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");
        Style s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);

		logArea.setStyledDocument(doc);
		
		rowHandler = new T140LogAreaRowHandler(doc);

	}
	
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (logArea != null) {
			logArea.setBackground(bg);
		}
		if (logPanel != null) {
			logPanel.setBackground(bg);
		}
	}
	
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (logArea != null) {
			logArea.setForeground(fg);
		}
		if (logPanel != null) {
			logPanel.setForeground(fg);
		}
	}
	
	public void setFont(Font f) {
		super.setFont(f);
		if (logArea != null) {
			logArea.setFont(f);
		}
		if (logPanel != null) {
			logPanel.setFont(f);
		}		
	}
	
    /**
     * Sets if the text area should have an inactive look or not.
     *
     * @param state True for active look, false for inactive look
     */
    public void setActiveLook(boolean state) {
    	if (state) {
    		setBackground(SystemColor.info);
    	}
    	else {
    		setBackground(SystemColor.control);
    	}
   	}
    

    /**
     * Sets the username that should be shown for the remote 
     * user.
     * 
     * @param username The user name for the remote user in
     * the current call.
     */
    public void setRemoteUsername(String username) {
    	if (username.length() > 20) {
    		username = username.substring(0, 18) + "..";
    	}
    	this.remoteUsername = username;
    	rowHandler.setRemoteUsername(username);
    }

	public void addRemoteText(String text) {
		rowHandler.addRemoteText(text);
		pushDownScrollBar();
	}
	
	public void moveRemoteTextToLog(boolean isCr) {
		rowHandler.moveRemoteTextToLog(isCr);
		pushDownScrollBar();
	}
	
	public void addLocalTextToLog(String text, boolean isCr) {
		rowHandler.addRow(new T140LogAreaRow(true, text, isCr));
		pushDownScrollBar();
	}
	
	public void processRemoteBs() {
		if (rowHandler.getRemoteText().length() > 0) {
			rowHandler.eraseFromRemoteText();
		}
		else {
			String prevText = rowHandler.getLastRow(false);
			if (prevText != null && prevText.length() > 0) {
				rowHandler.addRemoteText(prevText);
			}
		}
		pushDownScrollBar();
	}

	public String getLastLocalRow() {
		String text = rowHandler.getLastRow(true);
		pushDownScrollBar();
		return text;
	}



	/*
	private void updateLogPanel() {
		int vlen = logRows.size();
		String[] initString = new String[vlen*2+2];
		String[] initStyles = new String[vlen*2+2];
		
		for (int cnt=0; cnt<vlen; cnt++) {
			T140LogAreaRow row = (T140LogAreaRow)logRows.elementAt(cnt);
			initString[cnt*2] = row.getUsername() + ": ";
			initString[cnt*2+1] = row.getText();
			if (cnt < vlen-1) {
				initString[cnt*2+1] += "\n";
			}
			initStyles[cnt*2] = "bold";
			initStyles[cnt*2+1] = "regular";
			//windowText += logRows.elementAt(cnt).toString();
		}
		if (remoteText.length() > 0) {
			initString[vlen*2] = "\n" + remoteUsername + " typing: ";
			initString[vlen*2+1] = remoteText;
		}
		else {
			initString[vlen*2] = "";
			initString[vlen*2+1] = "";
		}
		initStyles[vlen*2] = "bold";
		initStyles[vlen*2+1] = "regular";

        try {
            for (int i=0; i < initString.length; i++) {
                doc.insertString(doc.getLength(), initString[i],
                                 doc.getStyle(initStyles[i]));
            }
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }		
		
	}
*/
	
	public String getText() {
		return logArea.getText();
	}
	
	public void setEditable(boolean isEditable) {
		logArea.setEditable(isEditable);
	}
	
	public void clearArea() {
		logArea.setText("");
		remoteUsername = "Remote";
		rowHandler.clear();
	}

	private void pushDownScrollBar() {
		Runnable doScroll = new T140LogAreaScroller(this.getVerticalScrollBar());
		SwingUtilities.invokeLater(doScroll);
	}

    /**
	 * This class is used by all log area rows
	 * 
	 * @author Andreas Piirimets, Omnitor AB
	 */
	class T140LogAreaRow {
		private boolean isLocal;
		private String text;
		private String formattedText;
		private boolean isCr;
		
		public T140LogAreaRow(boolean isLocal, String text, boolean isCr) {
			this.isLocal = isLocal;
			this.text = text;
			this.isCr = isCr;
		}
		
		public boolean getIsCr() {
			return isCr;
		}
		public boolean getIsLocal() {
			return isLocal;
		}
		
		public String getUserName() {
			return (isLocal?"Me":remoteUsername);
		}
		
		public String getText() {
			return text;
		}
		
		public String toString() {
			return formattedText; 
		}
	}
	
	class T140LogAreaRowHandler {
		private Vector<T140LogAreaRow> logRows;
		private StyledDocument doc;
		private String remoteText;
		private String remoteUsername;

		public T140LogAreaRowHandler(StyledDocument doc) {
			logRows = new Vector<T140LogAreaRow>(0, 1);
			this.doc = doc;
			remoteText = "";
		}
		
		public void addRow(T140LogAreaRow row) {
			boolean logRowsWasEmpty = logRows.isEmpty();
			logRows.add(row);

			int position;
			if (remoteText.equals("")) {
				position = doc.getLength();
			}
			else {
				position = doc.getLength() - (remoteUsername + " typing: " + remoteText).length() - 1;
				if (position < 0) {
					position = 0;
				}
			}
			
            try {
            	boolean addCrAfterString = (position == 0) && (remoteText.length() > 0);
    			if (!logRowsWasEmpty) {
    				doc.insertString(position, "\n", doc.getStyle("regular"));
    				position++;
    			}
            	doc.insertString(position, row.getUserName() + ": ", doc.getStyle("bold"));
            	position += (row.getUserName() + ": ").length();
	            doc.insertString(position, row.getText(), doc.getStyle("regular"));
	            if (addCrAfterString) {
	            	position += row.getText().length();
		            doc.insertString(position, "\n", doc.getStyle("regular"));	            	
	            }
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void clear() {
			logRows.clear();
			remoteText = "";

			try {
				doc.remove(0, doc.getLength());
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void addRemoteText(String text) {
			try {
				if (remoteText.length() > 0) {
					doc.insertString(doc.getLength(), text, doc.getStyle("regular"));
				}
				else {
					if (doc.getLength() > 0) {
						doc.insertString(doc.getLength(), "\n", doc.getStyle("regular"));
					}
					doc.insertString(doc.getLength(), remoteUsername + " typing: ", doc.getStyle("bold"));
					doc.insertString(doc.getLength(), text, doc.getStyle("regular"));
				}
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			remoteText += text;
			
		}
		
		public void setRemoteUsername(String name) {
			remoteUsername = name;
		}
		
		public void moveRemoteTextToLog(boolean isCr) {
			int len = 0;
			if (!remoteText.equals("")) {
				len = (remoteUsername + " typing: " + remoteText).length() + 1;				
			}
			int offset = doc.getLength() - len;
			if (offset < 0) {
				offset = 0;
				len--;
			}
			String text = remoteText;
			remoteText = "";
			try {
				if (len > 0) {
					doc.remove(offset, len);
				}
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			addRow(new T140LogAreaRow(false, text, isCr));
		}
		
		public String getLastRow(boolean isLocal) {
			String returnStr = "";
			
			int vlen = logRows.size();
			for (int cnt=vlen-1; cnt>=0; cnt--) {
				T140LogAreaRow row = (T140LogAreaRow)logRows.elementAt(cnt);
				if (row.getIsLocal() == isLocal) {
					returnStr = row.getText();
					if (!row.getIsCr()) {
						returnStr = returnStr.substring(0, returnStr.length()-1);
					}
					int len = (row.getText() + row.getUserName()).length() + 3;
					int offset = doc.getLength() - len;
					logRows.removeElementAt(cnt);
					vlen--;
					for (int cnt2=cnt; cnt2<vlen; cnt2++) {
						T140LogAreaRow row2 = (T140LogAreaRow)logRows.elementAt(cnt2);
						offset -= (row2.getText() + row2.getUserName()).length() + 3;
					}
					if (!remoteText.equals("")) {
						offset -= (remoteUsername + " typing: " + remoteText).length() + 1;
					}
					if (offset < 0) {
						offset = 0;
						if (remoteText.length() == 0) {
							len--;
						}
					}
					try {
						System.out.println("Removing from offset " + offset + " with len " + len + ", which is: " + " " + doc.getText(offset, len));
						doc.remove(offset, len);
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					cnt=-1;
				}
			}

			return returnStr;
		}

		/*
		public String dgetLastRemoteRow() {
			int vlen = logRows.size();
			for (int cnt=vlen-1; cnt>=0; cnt--) {
				T140LogAreaRow row = (T140LogAreaRow)logRows.elementAt(cnt);
				if (!row.getIsLocal()) {
					returnStr = row.getText();
					logRows.removeElementAt(cnt);
					updateLogPanel();
					cnt=-1;
				}
			}

		}
		*/
		public String getRemoteText() {
			return remoteText;
		}
		
		public void eraseFromRemoteText() {
			remoteText = remoteText.substring(0, remoteText.length()-1);
			try {
				doc.remove(doc.getLength()-1, 1);
				if (remoteText.equals("")) {
					int len = (remoteUsername + " typing: ").length() + 1;
					int offset = doc.getLength() - len;
					if (offset < 0) {
						offset = 0;
						len--;
					}
					doc.remove(offset, len);
				}
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	class T140LogAreaScroller implements Runnable {
		private JScrollBar sb;
		
		public T140LogAreaScroller(JScrollBar sb) {
			this.sb = sb;
		}
		
		public void run() {
			sb.setValue(sb.getMaximum());
		}
	}
	
	class T140LogAreaPanel extends JPanel {
		public T140LogAreaPanel(LayoutManager lm) {
			super(lm);
		}
		
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			
			int width = 40;
			int height = 40;
			if (d != null) {
				height = (int)d.getHeight();
			}
			
			return new Dimension(width, height);
		}
	}
}
