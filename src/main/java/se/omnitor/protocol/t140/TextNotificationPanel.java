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

import javax.swing.JLabel;
import javax.swing.BorderFactory;
import java.awt.Color;

public class TextNotificationPanel extends JLabel implements Runnable {
	
	private boolean doShow;
	private boolean isShown;
	private boolean isRunning;
	private String text;
	private int maxWidth = 0;
	private int maxHeight = 0;
	private int leftInsets;
	private int rightInsets;
	private int bottomInsets;
	private boolean newBoundsApplied;
	
	public TextNotificationPanel(int leftInsets, int rightInsets, int bottomInsets) {
		doShow = false;
		isRunning = true;
		isShown = false;
		newBoundsApplied = false;
		this.leftInsets = leftInsets;
		this.rightInsets = rightInsets;
		this.bottomInsets = bottomInsets;
		
		(new Thread(this)).start();

		setText("");
		text = "";
		this.setOpaque(true);
		this.setBackground(Color.LIGHT_GRAY);
		this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
	}
	
	public synchronized void setShow(boolean doShow, String intext) {
		this.doShow = doShow;
		if (intext != null) {
			text += intext;
			this.setText(text);
		}
	}
	
	public void finalize() {
		isRunning = false;
	}
	
	public void run() {
		while (isRunning) {
			try {
				Thread.sleep(10);
			}
			catch (InterruptedException e) {
				// Ignore
			}

			boolean action;
			synchronized(this) {
				action = doShow;
			}
			if (action != isShown) {
				if (action) {
					showNotification();
				}
				else {
					hideNotification();
				}
			}
			else if (newBoundsApplied && isShown) {
				this.setBounds(leftInsets, maxHeight-bottomInsets-20, maxWidth-leftInsets-rightInsets, 20);				
			}
		}
	}
	
	private void showNotification() {
		elevate(0, 20);

		System.out.println("Showing notification!");
		isShown = true;
		
	}
	
	private void hideNotification() {
		elevate(19, -1);
		
		System.out.println("Hiding notification!");
		text = "";
		isShown = false;
	}
	
	private void elevate(int start, int end) {
		int step;
		if (start > end) {
			step = -1;
		}
		else {
			step = 1;
		}
		
		for (int cnt=start; cnt!=end; cnt+=step) {
			if (maxHeight > 20) {
				System.out.println("Bounds: 0, " + (maxHeight-cnt) + ", " + maxWidth + ", " + cnt);
				this.setBounds(leftInsets, maxHeight-bottomInsets-cnt, maxWidth-leftInsets-rightInsets, cnt);
			}
			
			try {
				Thread.sleep(20);
			}
			catch (InterruptedException e) {
				// Ignore
			}
		}
		
	}
	public void setMaxBounds(int width, int height) {
		this.maxWidth = width;
		this.maxHeight = height;
		
		this.newBoundsApplied = true;
	}
}
