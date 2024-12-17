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

import javax.swing.JScrollPane;
import java.awt.*;

import javax.swing.BorderFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

/**
 * This class provides T.140 presentation functionality. <br>
 * <br>
 * The JPanel holds two T140TextAreas, which are level synchronized according
 * to T.140. <br>
 * <br>
 * Incoming data is received with the T140EventHandler functions (preferrable
cErik Zetterstrom, Omnitor AB
 * @author Andreas Piirimets, Omnitor AB
 *
 * @todo Make it send T.140 events correctly.
 */
public class T140Panel extends JPanel implements KeyListener,
T140EventHandler,
MouseListener,
ActionListener {

	private T140TextArea localTa = null;
	private T140TextArea remoteTa = null;
	private T140LogArea logTa = null;
	private IMtext IManchor = null;
	private String lineSeparator = System.getProperty("line.separator");
	private T140EventHandler outHandler;

	// realtime preview mode
	private boolean realtimepv;


	// Variables used by thisUserTextArea_KeyTyped() for managing text
	private boolean skipbs = false;

	// Variables for right-click popup menu
	private JMenuItem textSettingsItem;

	// ??
	private int currentNoOfChar = 0;

	private MsrpSmoother msrpSmoother;
	private RTPV_timer RTPVtimer_local;
	private RTPV_timer RTPVtimer_remote;
	private boolean useMsrpSmoother;
	private Properties language;
	private String logwindow;
	private String sendwindow;
	private JLayeredPane remoteTaLayeredPane;
	private TextNotificationPanel textNotificationPanel;

	// declare package and classname
	public final static String CLASS_NAME = T140Panel.class.getName();
	// get an instance of Logger
	private static Logger logger = Logger.getLogger(CLASS_NAME);



	/**
	 * Makes data transfer objects for output (synchObject)
	 * and input (playerSynchObject).
	 * It also makes a thread that will update the GUI.
	 *
	 */
	public T140Panel(T140EventHandler outHandler, boolean rtpv, String logwindow, String recivewindow,
			String sendwindow, Properties language) {

		// write methodname
		final String METHOD = "T140Panel(T140EventHandler outHandler)";
		// log when entering a method
		logger.entering(CLASS_NAME, METHOD, outHandler);


		this.outHandler = outHandler;
		this.logwindow = logwindow;
		this.sendwindow = sendwindow;
		this.language = language;
		/*
        t140GUIUpdate = new T140GUIUpdate(this,playerSynchObject);
		 */
		realtimepv = rtpv;
		//language = controller.getLanguage();

		remoteTaLayeredPane = new JLayeredPane();
		remoteTaLayeredPane.setLayout(new T140LayoutManager());
		
		localTa = new T140TextArea(this.outHandler,1, 1, true, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		localTa.addKeyListener(this);
		localTa.setScrollingAlwaysEnabled(true);
		remoteTa = new T140TextArea(this.outHandler,1, 1, false, JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		remoteTa.setEditable(false);
		logTa = new T140LogArea(1, 1, false, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		IManchor = new IMtext(2,"");
		if (realtimepv) {
			//IManchor = new IMtext(2,"");
			//logTa = new T140LogArea(1, 1, false, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			logTa.setEditable(false);
			logTa.setActiveLook(true);
			startRtpvTimer();
			
		} else {
			localTa.getAccessibleContext().setAccessibleDescription("Send text");
			localTa.setAccessibleDescription("Send text");
			localTa.addMouseListener(this);

			remoteTa.getAccessibleContext().setAccessibleDescription("Receive text");
			remoteTa.setAccessibleDescription("Receive text");
			remoteTa.addMouseListener(this);
		}

		//initRcMenu();

		layoutPanel();
		// Start the Realtime preview timers, they handle the altreasons
		msrpSmoother = new MsrpSmoother();
		useMsrpSmoother = false;

		logger.exiting(CLASS_NAME, METHOD);
	}
	
	public void startRtpvTimer() {
		stopRtpvTimer();
		
		RTPVtimer_local = new RTPV_timer(0);
		RTPVtimer_remote = new RTPV_timer(1);
	}
	
	/**
	 * Creates the right-click menu.
	 *
	 */
	/*
	private void initRcMenu() {
		rcMenu = new JPopupMenu();

		textSettingsItem = new JMenuItem("Text settings");
		textSettingsItem.addActionListener(this);

		rcMenu.add(textSettingsItem);

		try {
			LookAndFeel laf =
				(LookAndFeel)Class.forName
				(UIManager.getSystemLookAndFeelClassName()).
				newInstance();

			rcMenu.setUI((PopupMenuUI)laf.getDefaults().getUI(rcMenu));
		}
		catch (ClassNotFoundException e) {
			// Cannot set system's look'n'feel. Ignore and continue.
			logger.throwing(this.getClass().getName(), "initRcMenu", e);
		}
		catch (IllegalAccessException e) {
			// Cannot set system's look'n'feel. Ignore and continue.
			logger.throwing(this.getClass().getName(), "initRcMenu", e);
		}
		catch (InstantiationException e) {
			// Cannot set system's look'n'feel. Ignore and continue.
			logger.throwing(this.getClass().getName(), "initRcMenu", e);
		}

	}
*/
	
	/**
	 * Gets the font of the local text area.
	 *
	 * @return The font of the local text area.
	 */
	public Font getTaFont() {
		return localTa.getFont();
	}
	
	public void stopRtpvTimer() {
		if (RTPVtimer_local != null) {
			RTPVtimer_local.stop();
		}
		if (RTPVtimer_remote != null) {
			RTPVtimer_remote.stop();
		}
	}

	/**
	 * Gets the font size of the font in the local text area.
	 *
	 * @return The font size.
	 */
	public int getTaFontSize() {
		return localTa.getFont().getSize();
	}


	/**
	 * Gets the foreground color of the local text area.
	 *
	 * @return The foreground color of the local text area.
	 */
	public Color getTaFontColor() {
		return localTa.getForeground();
	}

	/**
	 * Gets the background color of the local text area.
	 *
	 * @return The background color of the local text area.
	 */
	public Color getTaFontBackground() {
		return localTa.getBackground();
	}

	/**
	 * Gets the font of the remote text area.
	 *
	 * @return The font of the remote text area.
	 */
	/*
    public Font getRemoteFont() {
	return remoteTa.getFont();
    }
	 */

	/**
	 * Gets the font size of the font in the remote text area.
	 *
	 * @return The font size.
	 */
	/*
    public int getRemoteFontSize() {
	return remoteTa.getFont().getSize();
    }
	 */

	/**
	 * Gets the foreground color of the remote text area.
	 *
	 * @return The foreground color of the remote text area.
	 */
	/*
    public Color getRemoteFontColor() {
	return remoteTa.getForeground();
    }
	 */

	/**
	 * Gets the background color of the remote text area.
	 *
	 * @return The background color of the remote text area.
	 */
	/*
    public Color getRemoteFontBackground() {
	return remoteTa.getBackground();
    }
	 */


	/**
	 * Sets the font of the local text area.
	 *
	 * @param newFont The font to set.
	 */
	public void setTaFont(Font newFont) {
		localTa.setFont(newFont);
		remoteTa.setFont(newFont);
		if (realtimepv)
			logTa.setFont(newFont);
	}

	/**
	 * Sets the font of the remote text area.
	 *
	 * @param newFont The font to set.
	 */
	/*
    public void setRemoteFont(Font newFont) {
	remoteTa.setFont(newFont);
    }
	 */

	/**
	 * Sets the foreground color of the local text area.
	 *
	 * @param textColor The color to use.
	 */
	public void setTaFontColor(Color textColor) {
		localTa.setForeground(textColor);
		remoteTa.setForeground(textColor);
		if (realtimepv)
			logTa.setForeground(textColor);
	}

	/**
	 * Sets the foreground color of the remote text area.
	 *
	 * @param textColor The color to use.
	 */
	/*
    public void setRemoteFontColor(Color textColor) {
	remoteTa.setForeground(textColor);
    }
	 */

	/**
	 * Sets the background color of the local text area.
	 *
	 * @param backgroundColor The color to use.
	 */
	public void setTaFontBackground(Color backgroundColor) {
		localTa.setBackground(backgroundColor);
		remoteTa.setBackground(backgroundColor);
		if (realtimepv) {
			logTa.setBackground(backgroundColor);
		}
	}

	/**
	 * Sets the background color of the remote text area.
	 *
	 * @param backgroundColor The color to use.
	 */
	/*
    public void setRemoteFontBackground(Color backgroundColor) {
	remoteTa.setBackground(backgroundColor);
    }
	 */
	public void changeLayout(boolean realtimepv) 
	{
		this.realtimepv = realtimepv;
		if(realtimepv){
			remoteTaLayeredPane.remove(remoteTa);
			this.remove(remoteTaLayeredPane);
			this.remove(localTa);
			this.validate();
		} else {
			remoteTaLayeredPane.remove(remoteTa);
			this.remove(remoteTaLayeredPane);
			this.remove(logTa);
			this.remove(localTa);
			this.validate();
		}
		layoutPanel();
	}

	/**
	 * Makes the layout of this panel.
	 *
	 */
	private void layoutPanel() {
		if (realtimepv) {
			logTa.setBorder(BorderFactory.createTitledBorder
					(BorderFactory.createEtchedBorder(), logwindow));
			/*
			remoteTa.setBorder(BorderFactory.createTitledBorder
					(BorderFactory.createEtchedBorder(), recivewindow));
					*/
			localTa.setBorder(BorderFactory.createTitledBorder
					(BorderFactory.createEtchedBorder(), sendwindow));
			localTa.setMinimumSize(new Dimension(20,150));

			GridBagConstraints gridBagConstraints;
			gridBagConstraints = new java.awt.GridBagConstraints();
			this.setLayout(new GridBagLayout());
			gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weighty=1.0;
			gridBagConstraints.weightx=1.0;
			this.add(logTa, gridBagConstraints);
			/*
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 1;
			gridBagConstraints.weighty=0.3;
			gridBagConstraints.fill = gridBagConstraints.BOTH;
			this.add(remoteTa, gridBagConstraints);
			 */
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 1;
			gridBagConstraints.weighty=0.0;
			gridBagConstraints.weightx=1.0;
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			this.add(localTa, gridBagConstraints);
		} else {
			Border remoteTaBorder = BorderFactory.createTitledBorder
				(BorderFactory.createEtchedBorder(),
					language.getProperty("se.omnitor.protocol.t140."
							+ "RECEIVE_TEXT"));
			remoteTa.setBorder(remoteTaBorder);
		
			remoteTaLayeredPane.add(remoteTa, JLayeredPane.DEFAULT_LAYER);
						
			Insets remoteTaBorderInsets = remoteTaBorder.getBorderInsets(remoteTa);
			
			textNotificationPanel = new TextNotificationPanel(remoteTaBorderInsets.left-4, remoteTaBorderInsets.right-3+18, remoteTaBorderInsets.bottom-3);
			remoteTa.setTextNotificationPanel(textNotificationPanel);
			remoteTaLayeredPane.add(textNotificationPanel, JLayeredPane.POPUP_LAYER);
		
			localTa.setBorder(BorderFactory.createTitledBorder
					(BorderFactory.createEtchedBorder(),
							language.getProperty("se.omnitor.protocol.t140."
									+ "SEND_TEXT")));
			this.setLayout(new GridLayout(1, 2));
			localTa.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){
				public void adjustmentValueChanged(AdjustmentEvent e){
					  setScrollBarValues(remoteTa.getVerticalScrollBar(), e.getValue());
				  }
			});
			this.add(remoteTaLayeredPane);
			this.add(localTa);
		}
	}

	private void setScrollBarValues(JScrollBar scrollbar, int value){
		scrollbar.setValue(value);
	}

	/**
	 * Sets the number of rows per text area
	 *
	 * @param rows The number of rows
	 */
	public void setRows(int rows) {
		localTa.setRows(rows);
		remoteTa.setRows(rows);
	}

	/**
	 * Sets the number of columns per text area
	 *
	 * @param columns The number of columns
	 */
	public void setColumns(int columns) {
		localTa.setColumns(columns);
		remoteTa.setColumns(columns);
	}

	/**
	 * Handles incoming T.140 events.
	 *
	 * @param event The event
	 */
	public void newEvent(T140Event event) {
		if (useMsrpSmoother) {
			msrpSmoother.newEvent(event);
		}
		else {
			switch (event.getType()) {

			case T140Event.TEXT:
				processText((String)event.getData());
				break;

			case T140Event.BELL:
				processBell();
				break;

			case T140Event.BS:
				processBs();
				break;

			case T140Event.NEW_LINE:
			case T140Event.CR_LF:
				processNewLine();
				break;

			default:
				// Ignore the rest
			}
		}
	}

	/**
	 * Processes an incoming backspace character.
	 *
	 */
	private void processBs() {
		int theEnd = remoteTa.getText().length() - 1;

		// Continue only if there are characters in the remote text
		// panel to remove
		if (!realtimepv) {
			if (theEnd >= 0) {
				remoteTa.setErasedCharacter(remoteTa.getText().charAt(theEnd));

				if (remoteTa.getErasedCharacter() == '\n') {
					remoteTa.removeLastWrittenTextCharacter();
					remoteTa.eraseAutoNewlines();
					return;
				}
				else {
					remoteTa.removeLastGuiCharacter();
					remoteTa.removeLastWrittenTextCharacter();
				}

			}
		}

		else if (theEnd >= 0) {
			remoteTa.removeLastGuiCharacter();
			remoteTa.removeLastWrittenTextCharacter();
		}

		//Sync to other panel.
		remoteTa.syncToRow(localTa.getCurrentPosition());

		if (realtimepv) {
			if (RTPVtimer_remote != null) {
				RTPVtimer_remote.setTimer(30);
			}
			
			logTa.processRemoteBs();
			/*
			if (theEnd < 0) {
				String prevEntry = IManchor.getPrevEntryText(1);
				if (prevEntry.length() > 1) {
					remoteTa.setAllText(prevEntry.substring(0, prevEntry.length() - 2));
				}
				else if(prevEntry.length() > 0) {
					remoteTa.setAllText(prevEntry.substring(0, prevEntry.length() - 1));
				}
				logTa.setText(IManchor.getLog());
			}
			*/
		}
	}

	/**
	 * Processes an incoming alert event.
	 *
	 */
	private void processBell() {
		// Beep
		java.awt.Toolkit.getDefaultToolkit().beep();
	}

	/**
	 * Processes a received new line.
	 *
	 */
	private void processNewLine() {
		if (realtimepv) {
			/*
			IManchor.insertIMtextObject(new IMtext(1,remotewhoname));
			IManchor.prevEntry.addText(remoteTa.getAllText() + "  ");
			//processText(" ");
			//System.out.println(IManchor.getLog() + "\n");
			logTa.setText(IManchor.getLog());
			*/

			logTa.moveRemoteTextToLog(true);
			
			if (RTPVtimer_remote != null) {
				RTPVtimer_remote.setTimer(0);
			}
		
		} else {
			int length = remoteTa.getText().length();
			System.out.println("Antal tecken = " + length);
			processText("\n");
		}
	}

	/**
	 * Processes received data and handles synchronisation between the two
	 * T140TextAreas.
	 *
	 * @param theData Input data.
	 */
	private void processText(String inText) {

		if (realtimepv) {

			logTa.addRemoteText(inText);
			
			if (RTPVtimer_remote != null) {
				RTPVtimer_remote.setTimer(30);
			}
			// Check for altreasons for realtime preview
			/*if ((lastTypedChar == 'A') ||
	    (lastTypedChar == '*') ||
	    (lastTypedChar == 'K') ||
	    (lastTypedChar == '+') ||
	    (lastTypedChar == '.')) {
		if (remoteTa.getText().length() > 2) {
			//System.out.println("Text string longer then 4 chars.");
			String loString = remoteTa.getText().substring(remoteTa.getText().length()-4);
			//System.out.println(loString);
			if(loString.equals("GASK") || loString.equals("SKSK")) {
				//System.out.println("GASK detected.");
				RTPVtimer_remote.setTimer(7);
			}
		}
		if (localTa.getText().length() > 1) {
                    if((remoteTa.getText().substring(remoteTa.getText().length()-2)).equals("GA")) {
                        //System.out.println("GA detected.");
                        RTPVtimer_remote.setTimer(7);
                    }
		}
		if (lastTypedChar == '.') {System.out.println(". detected"); RTPVtimer_remote.setTimer(7);}
		if (lastTypedChar == '+') {System.out.println("+ detected"); RTPVtimer_remote.setTimer(7);}
		if (lastTypedChar == '*') {System.out.println("* detected"); RTPVtimer_remote.setTimer(7);}
	}*/

		}
		else {
			char[] cArray = inText.toCharArray();

			for (int i = 0; i < cArray.length; i++) {
				remoteTa.append(String.valueOf(cArray[i]));
				remoteTa.addWrittenText(cArray[i]);
				remoteTa.setEndCaretPosition();

				// Sync other panel to this panel's line
				localTa.syncToRow(remoteTa.getCurrentPosition());

			}
		}

	}

	/**
	 * Gets the remote user's text area
	 *
	 * @return The remote user's text area
	 */
	public T140TextArea getRemoteTextArea() {
		return remoteTa;
	}

	/**
	 * Gets the local user's text area
	 *
	 * @return The local user's text area
	 */
	public T140TextArea getLocalTextArea() {
		return localTa;
	}

	public T140LogArea getLogTextArea() {
		IManchor.prevEntry = null;
		return logTa;
	}

	public void setRemoteLogName(String name) {
		/*
	//This logic might be in the caller instead
	String tempname = name.substring(5,name.indexOf("@"));
	if (tempname.indexOf(".") > 0)
		tempname = tempname.substring(0,tempname.indexOf("."));
    	remotewhoname = "<" + tempname + "> ";
		 */
		logTa.setRemoteUsername(name);
	}

	/**
	 * Display interrupt dialog box.
	 *
	 */
	public void interrupt() {
		//display interrupt dialog box
	}


	/************
     KeyListener
	 ************/


	/**
	 * Handles a typed key.
	 *
	 * @param event A key event.
	 */
	public void keyTyped(KeyEvent event) {
		thisUserTextArea_KeyTyped(event);
	}


	/**
	 * Handles a pressed key.
	 *
	 * @param event The key event.
	 */
	public void keyPressed(KeyEvent event) {
				
		//Dispatch event to parent in order to make
		//shortcuts work.
		this.getParent().dispatchEvent(event);

		//Don't send TAB over the net
		if(event.getKeyCode() == KeyEvent.VK_TAB) {
			event.consume();
			return;
		}
		
		//If a deletion is done, first save the last char in the text
		//for possibility to know what has been deleted
		if (event.getKeyCode() == T140Constants.BACKSPACE) {
			String content=null;
			content = localTa.getText();

			if (realtimepv) {
				if (content.length() == 0) {
					String prevText = logTa.getLastLocalRow() + " ";
								
					if (prevText == null || prevText.length() == 0) {
						localTa.setText("");
					}
					else {
						localTa.setText(prevText);
						localTa.setWText(prevText);
					}
				}
			}

			if (content.length() > 0) {

				localTa.setErasedCharacter(content.charAt(localTa.getText().length()-1));
			}
		}

		// Changed by Andreas Piirimets 2004-02-13
		// Now, the action arrow keys are working. They are not sent to the
		// stream, that is controlled in thisUserTextArea_keyTyped(..).
		// Also, characters are always put at the end of the window.
		if(event.isShiftDown() && event.getKeyCode() == KeyEvent.VK_INSERT){
			int noOfChar = localTa.getText().length();
			String theText = null;
			localTa.textArea.paste();

			if(noOfChar > 0){
				theText =localTa.getText().substring(noOfChar);
			}else{
				theText =localTa.getText();
			}
			if (theText.length() > 0) {
				outHandler.newEvent(new T140Event(T140Event.TEXT, theText));
			}
			event.consume();
		}else if (event.isActionKey()){
			if (event.getKeyCode() != KeyEvent.VK_UP &&
					event.getKeyCode() != KeyEvent.VK_DOWN &&
					event.getKeyCode() != KeyEvent.VK_LEFT &&
					event.getKeyCode() != KeyEvent.VK_RIGHT) {

				event.consume();
			}
		}

		// Changed by Andreas Piirimets 2004-02-24
		// Now, the cut'n paste functionality works with CTRL-X, CTRL-C and
		// CTRL-V.
		else if (event.isControlDown() &&
				((int)event.getKeyChar() != 22 ||
				(int)event.getKeyChar() != 3)) {
			
			event.consume();
		}
		
		else {

			if (event.getKeyCode() != KeyEvent.VK_SHIFT &&
					!event.isAltDown() &&
					!event.isAltGraphDown() &&
					!event.isMetaDown() &&
					((int)event.getKeyChar() != 3)) {					
				// Assure that the pointer is at the bottom end of the
				// window. The user is not allowed to write in the middle
				// of the text window.
				//
				// Only SHIFT-down should not invoke this. SHIFT+Key should,
				// though.
				localTa.setEndCaretPosition();			
			}
		}
	}


	/**
	 * Does nothing when a key is released.
	 *
	 * @param event KeyEvent
	 */
	public void keyReleased(KeyEvent event) {

	}


	/**
	 * Handles typed keys.
	 *
	 */
	private void thisUserTextArea_KeyTyped(KeyEvent event) {

		// write methodname
		final String METHOD = "thisUserTextArea_KeyTyped(KeyEvent event)";
		// log when entering a method
		logger.entering(CLASS_NAME, METHOD, event);

		char[] inChar = new char[1];
		inChar[0] = event.getKeyChar();
		String txt = (KeyEvent.getKeyText(event.getKeyCode()));
		int noOfChar = localTa.getText().length();
		logger.logp(Level.FINEST, CLASS_NAME, METHOD, "a key was pressed in a textarea", txt);
		
		if (realtimepv) {
			//reset the Realtime preview timer when a key is pressed
			if (RTPVtimer_local != null) {
				RTPVtimer_local.setTimer(10);
			}
		}

		if(inChar[0]=='\t') {
			logger.logp(Level.FINEST, CLASS_NAME, METHOD, "key was '\t'", txt);
			event.consume();
			return;
		}

		// if the T140Panel is not allowed to be edited, just return
		if (!localTa.isEditable()) {
			logger.logp(Level.FINEST, CLASS_NAME, METHOD, "T140Panel is not editable");
			return;
		}

		if (event.isActionKey()) {
			// We don't want action keys in buffer
			// This check is done twice. Here and also in keyPressed(..).
			// This is for letting the non-numpad arrow keys through
			logger.logp(Level.FINEST, CLASS_NAME, METHOD, "key was actionkey, we don't want to buffer that");
			event.consume();
			return;
		}

		if (event.isShiftDown()) {
			//do not want the Shift char into the buffer
			if (txt.compareTo("Shift") == 0) {
				logger.logp(Level.FINEST, CLASS_NAME, METHOD, "key was shift, we don't want to buffer that");
				return;
			}
		}

		// Changed by Andreas Piirimets 2004-03-03
		// The DELETE key may not be sent over the network.
		if ((int)event.getKeyChar() == 127) {
			logger.logp(Level.FINEST, CLASS_NAME, METHOD, "key was delete, we don't want to buffer that");
			return;
		}
		
		if ((event.isControlDown() && inChar[0]!=22 && inChar[0]!=3) ||
				(event.isAltDown())) {

			//we do not want to send letters when the "CTRL" or "ALT" is
			//pressed. Exception: CTRL-V and CTRL-C.
			if (inChar[0] >=0x20) {

				//shall we send it? Let's check if a new
				//character has been displayed since last time.
				if ((noOfChar +1) == (currentNoOfChar)) {
					//let's send the char.
				}
				else {
					event.consume();
					return;
				}
			}
			else {
				event.consume();
				return;
			}
		}
		else {
			if(event.isControlDown() && inChar[0]==22){
				localTa.textArea.paste();
			}
		}

		boolean IMpreview = false;
		if ((inChar[0] == T140Constants.CARRIAGE_RETURN) ||
				(inChar[0] == T140Constants.LINE_FEED) ||
				(inChar[0] == lineSeparator.charAt(0))) {
			if (realtimepv) {
				/*
				IManchor.insertIMtextObject(new IMtext(0, "<me> "));
				String text = localTa.getAllText();

				if ((text.length() > 0) && (consumeNewline))
					IManchor.prevEntry.addText(' ' + text);
				else
					IManchor.prevEntry.addText(text);

				System.out.println("Adding line + [" + text + "]");

				logTa.setText(IManchor.getLog());
				System.out.println("Return pressed.\n");
				IMpreview = true;
				consumeNewline = true;
				ignoreDelete = false;
				*/
				
				//logTa.addLocalTextToLog(localTa.getAllText());
				String text = localTa.getText();
				text = text.substring(0, text.length()-1);
				logTa.addLocalTextToLog(text, true);
				localTa.setText("");
				if (RTPVtimer_local != null) {
					RTPVtimer_local.setTimer(0);
				}
			}
			inChar[0] =  T140Constants.LINE_SEPERATOR; //new line
		}

		if (inChar[0] == T140Constants.BACKSPACE) {

			System.out.println("Backspace pressed " + localTa.getText().length() + "\n");

			if (localTa.getText().length() == 0) {
				if (realtimepv) {
					/*
					if (! ignoreDelete) {
						String prevEntry = IManchor.getPrevEntryText(0);
						localTa.setAllText(prevEntry);
						logTa.setText(IManchor.getLog());
					}
					skipbs = true;
					ignoreDelete = false;
					*/
					
					//consumeNewline = true;
					//}
				/*else {
                        localTa.setAllText(prevEntry + " ");
                        logTa.setText(IManchor.getLog());
                    }*/

					/*if (prevEntry.length() > 0) {
                        event.consume();
                    }*/
					//localTa.removeLastWrittenTextCharacter();
				} else {
					//return;
				}

			}

			if (localTa.getErasedCharacter() == '\n') {

				System.out.println("Text area: \"" + localTa.getText() + "\"");
				System.out.println("Written  : \"" + localTa.getWText() + "\"");
				
				/*//-----------------------------------
                String text = localTa.getText();
                int size = text.length();
                int nrOfNewLines = 0;
                for (int i=size-1; i>=0; i--) {
                    if (text.charAt(i) == '\n')
                        nrOfNewLines++;
                    else
                        break;
                }
				 */
				localTa.removeLastWrittenTextCharacter();
                localTa.eraseAutoNewlines();

				System.out.println("Text area: \"" + localTa.getText() + "\"");
				System.out.println("Written  : \"" + localTa.getWText() + "\"");
				/*
                System.err.println("nrOfNewLines = " + nrOfNewLines);

                for (int i=0; i<nrOfNewLines; i++) {
                    localTa.append("\n");
                    localTa.addWrittenText('\n');
                }
                */
                //-----------------------------------*/

				//localTa.removeLastWrittenTextCharacter();
				//localTa.setText(localTa.getWText() + "\n");
				//consumeBackspace = true;
				//System.out.println("Last was NEWLINE");
			}
			else {
				System.out.println("Last was NOT NEWLINE");
				//localTa.setAllText(localTa.getAllText());
				if (skipbs) {
					System.out.println("skipbs");
					skipbs = false;
				}
				else {
					localTa.removeLastWrittenTextCharacter();
					System.out.println("NOT skipbs");
				}
			}

			//a automatic newline might have been erased. Sync to other panel.
			//localTa.syncToRow(remoteTa.getCurrentPosition());
		}


		// If not backspace, save the written character as written text
		else {

			if (inChar[0] == T140Constants.LINE_SEPERATOR) {
				System.out.println("Adding lineseprator");
				localTa.addWrittenText('\n');
			}
			else {
				localTa.addWrittenText(inChar[0]);
			}
		}

		if (realtimepv) {
			// Check for altreasons for realtime preview
			if ((inChar[0] == 'A') ||
					(inChar[0] == '*') ||
					(inChar[0] == 'K') ||
					(inChar[0] == '+') ||
					(inChar[0] == '.')) {
				//System.out.println("Last char trigged altreason timer.");
				//String teststring = localTa.getText();
				if (localTa.getText().length() > 2) {
					//System.out.println("Text string longer then 4 chars.");
					String loString = localTa.getText().substring(localTa.getText().length()-3) + inChar[0];
					if(loString.equals("GASK") || loString.equals("SKSK")) {
						//System.out.println("GASK detected.");
						if (RTPVtimer_local != null) {
							RTPVtimer_local.setTimer(7);
						}
					}
				}
				if (localTa.getText().length() > 1) {
					if((localTa.getText().substring(localTa.getText().length()-1) + inChar[0]).equals("GA")) {
						if (RTPVtimer_local != null) {
							RTPVtimer_local.setTimer(7);
						}
					}
				}
				if (inChar[0] == '.') {
					System.out.println(". detected");
					if (RTPVtimer_local != null) {
						RTPVtimer_local.setTimer(7);
					}
				}
				if (inChar[0] == '+') {
					System.out.println("+ detected");
					if (RTPVtimer_local != null) {
						RTPVtimer_local.setTimer(7);
					}
				}
				if (inChar[0] == '*') {
					System.out.println("* detected");
					if (RTPVtimer_local != null) {
						RTPVtimer_local.setTimer(7);
					}
				}
			}
		}

		if (inChar[0] != 3) {

			String theText;
			if ((int)inChar[0] == 22) {
				if(noOfChar > 0){
					theText =localTa.getText().substring(noOfChar);
					//theText = localTa.getText().substring(currentNoOfChar);
				}else{
					theText =localTa.getText();
				}
			}
			else {
				theText = new String(inChar);
			}

			if (theText.length() > 0) {
				outHandler.newEvent
				(new T140Event(T140Event.TEXT, theText));
			}
		}

		if (IMpreview) {
			localTa.getAllText();
			IMpreview = false;
		}


		currentNoOfChar = noOfChar;

		// Added by Andreas Piirimets 2004-02-25
		// Make sure that the last character is not removed in case of a
		// backspace on a new line.
		/*if (consumeBackspace) {
	    event.consume();
	}*/

		//sync other panel to this panel's row
		remoteTa.pushDownScrollBar();
		
		Runnable doSync = new T140PanelDelayedSyncer(localTa, remoteTa);
		SwingUtilities.invokeLater(doSync);
		logger.exiting(CLASS_NAME, METHOD);
	}

	public void useMsrpSmoother(boolean use) {
		useMsrpSmoother = use;
	}


	//
	// MouseListener
	//

	/**
	 * Handles right-clicks and displays a menu.
	 *
	 * @param e The event
	 */
	public void mouseClicked(MouseEvent e) {
		/*
		if (e.getButton() == MouseEvent.BUTTON3) {
			rcMenu.show(e.getComponent(), e.getX(), e.getY());
		}
		*/
	}

	/**
	 * Does nothing, ignores the event.
	 *
	 * @param e The event
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Does nothing, ignores the event.
	 *
	 * @param e The event
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Does nothing, ignores the event.
	 *
	 * @param e The event
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * Does nothing, ignores the event.
	 *
	 * @param e The event
	 */
	public void mouseReleased(MouseEvent e) {
	}

	//
	// ActionListener
	//

	/**
	 * Handles events of items in right-click popup menu
	 *
	 * @param e The event
	 */
	public void actionPerformed(ActionEvent e) {		
		if (e.getSource() == textSettingsItem) {
			//T140FontDialog dialog = new T140FontDialog(this);
		}
	}


	class MsrpSmoother implements Runnable {
		Thread thread;

		long[] times;
		long lastTimeStamp;
		long delay;
		T140Event[] eventList;

		boolean running;

		public MsrpSmoother() {
			times = new long[3];
			times[0] = 1;
			times[1] = 1;
			times[2] = 1;

			delay = 0;
			lastTimeStamp = System.currentTimeMillis();

			eventList = new T140Event[0];

			running = true;
			thread = new Thread(this, "MsrpSmoother");
			thread.start();
		}

		public void newEvent(T140Event event) {

			T140Event[] events;

			switch (event.getType()) {

			case T140Event.TEXT:
				String data = (String)event.getData();
				int len = data.length();
				events = new T140Event[len];
				for (int cnt=0; cnt<len; cnt++) {
					events[cnt] =
						new T140Event(T140Event.TEXT, ""+data.charAt(cnt));
				}

				setEvents(events);
				break;
			case T140Event.BELL:
			case T140Event.BS:
			case T140Event.NEW_LINE:
			case T140Event.CR_LF:
				setEvents(new T140Event[] { event });
				break;
			default:
				// Ignore the rest

			}
		}

		private synchronized void setEvents(T140Event[] events) {
			int len = eventList.length;
			T140Event[] newEl = new T140Event[len+events.length];
			System.arraycopy(eventList, 0, newEl, 0, eventList.length);
			System.arraycopy(events, 0, newEl, eventList.length,
					events.length);
			eventList = newEl;

			long diff = System.currentTimeMillis() - lastTimeStamp;
			if (diff > 50) {
				lastTimeStamp = System.currentTimeMillis();

				if (diff > 500) {
					diff = 500;
				}
				times[0] = times[1];
				times[1] = times[2];
				times[2] = diff;
			}

			long mean = (times[0] + times[1] + times[2]) / (long)3;
			delay = mean / eventList.length;

			notify();
		}

		public void run() {
			T140Event event;
			T140Event[] newEl;

			while (running) {
				synchronized (this) {
					if (eventList.length == 0) {
						try {
							wait();
						}
						catch (InterruptedException ie) {
						}
					}

					event = eventList[0];
					if (eventList.length == 1) {
						eventList = new T140Event[0];
					}
					else {
						newEl = new T140Event[eventList.length - 1];
						System.arraycopy(eventList, 1, newEl, 0,
								eventList.length-1);
						eventList = newEl;
					}

				}

				switch (event.getType()) {

				case T140Event.TEXT:
					processText((String)event.getData());
					break;

				case T140Event.BELL:
					if (!localTa.getText().equals(""))
						processBell();
					break;

				case T140Event.BS:
					processBs();
					break;

				case T140Event.NEW_LINE:
				case T140Event.CR_LF:
					processNewLine();
					break;

				default:
					// Ignore the rest

				}

				try {
					Thread.sleep(delay);
				}
				catch (InterruptedException ie) {
				}
			}
		}
	}

	class RTPV_timer implements Runnable {
		Thread thread;
		long lastTimeStamp;
		boolean running;
		boolean timer_active;
		int delta_milliseconds;
		boolean remote;

		public RTPV_timer(int state) {
			lastTimeStamp = System.currentTimeMillis();
			running = true;
			if(state == 0) remote = false;
			else remote = true;
			thread = new Thread(this, "RTPV_timer");
			thread.start();
		}

		public void run() {
			while (running) {
				if((System.currentTimeMillis()-lastTimeStamp) > delta_milliseconds) {
					if(timer_active) {
						if (remote) {
							/*
							String temp = remoteTa.getAllText();
							if (temp.equals("")) break;
							IManchor.insertIMtextObject(new IMtext(1, remotewhoname));
							IManchor.prevEntry.addText(temp + "  ");
							//System.out.println("timer_active remote");
							logTa.setText(IManchor.getLog());
							*/
							logTa.moveRemoteTextToLog(false);
							timer_active = false;
						} else {
							/*
							String temp = localTa.getAllText();
							if (temp.equals("")) break;
							IManchor.insertIMtextObject(new IMtext(0, "<me> "));
							IManchor.prevEntry.addText(" " + temp);
							//System.out.println("timer_active local");
							logTa.setText(IManchor.getLog());
							ignoreDelete = false;
							*/
							logTa.addLocalTextToLog(localTa.getText(), false);
							localTa.setText("");
							timer_active = false;
						}
					}
				}
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException ie) {
				}
			}
		}
		
		public void stop() {
			running = false;
		}

		public synchronized void setTimer(int seconds) {
			lastTimeStamp = System.currentTimeMillis();
			delta_milliseconds = seconds*1000;
			if (seconds == 0)
				timer_active = false;
			else
				timer_active = true;
		}
	}
	
	class T140PanelDelayedSyncer implements Runnable {
		private T140TextArea localTa;
		private T140TextArea remoteTa;
		
		public T140PanelDelayedSyncer(T140TextArea local, T140TextArea remote) {
			this.localTa = local;
			this.remoteTa = remote;
		}
		
		public void run() {
			remoteTa.syncToRow(localTa.getCurrentPosition());
		}
	}
	
	class T140LayoutManager implements LayoutManager {
		public Dimension minimumLayoutSize(Container parent) {
			Insets i = parent.getInsets();
			
			return new Dimension(i.left + i.right, i.top + i.bottom);
		}
		
		public void addLayoutComponent(String name, Component comp) {
		}
		
		public void layoutContainer(Container parent) {
			Insets i = parent.getInsets();
			int maxWidth = parent.getWidth() - (i.left + i.right);
			int maxHeight = parent.getHeight() - (i.top + i.bottom);
			int nComps = parent.getComponentCount();
			
			for (int cnt=0; cnt<nComps; cnt++) {
				Component c = parent.getComponent(cnt);
				
				if (c instanceof TextNotificationPanel) {
					((TextNotificationPanel)c).setMaxBounds(maxWidth, maxHeight);
				}
				else {
					c.setBounds(0, 0, maxWidth, maxHeight);
				}
			}
		}

		public Dimension preferredLayoutSize(Container arg0) {
			return new Dimension(10, 10);
		}

		public void removeLayoutComponent(Component arg0) {
		}
	}
}
