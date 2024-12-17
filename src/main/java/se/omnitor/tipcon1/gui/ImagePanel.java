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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import javax.swing.JPanel;

/**
 * A panel totally filled by an image.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class ImagePanel extends JPanel {
    
    private Image image;
    private int width;
    private int height;
    private int xPad;
    private int yPad;

    private boolean initOk;

    /**
     * Initializes the panel.
     *
     * @param imageFileName The file name to the image to paint.
     */
    public ImagePanel(String imageFileName) {
	super();

	initPanel(imageFileName, 0, 0);
    }

    /**
     * Initializes the panel.
     *
     * @param imageFileName The image file name
     * @param xPadding Horizontal padding in pixels
     * @param yPadding Vertical padding in pixels
     */
    public ImagePanel(String imageFileName, int xPadding, int yPadding) {
	super();

	initPanel(imageFileName, xPadding, yPadding);
    }

    private void initPanel(String imageFileName, int xPadding, int yPadding) {
	initOk = true;

	this.xPad = xPadding;
	this.yPad = yPadding;

	    image = 
		Toolkit.getDefaultToolkit().getImage(imageFileName);

	    MediaTracker tracker = new MediaTracker(this);
	    tracker.addImage(image, 0);
	    try {
		tracker.waitForID(0);
	    }
	    catch (InterruptedException e) {
		initOk = false;
	    }

	    if (tracker.isErrorAny()) {
		initOk = false;
	    }


	if (initOk) {
	
	    width = image.getWidth(this) + 2*xPadding;
	    height = image.getHeight(this) + 2*yPadding;
	    
	}
	else {
	    width = 0;
	    height = 0;
	}

	super.setSize(width, height);

    }
    
    /**
     * Updates the image. This function simply calls paint(g).
     *
     * @param g The graphics environment to paint in.
     */
    public void update(Graphics g) {
	paint(g);
    }
    
    /**
     * Paints the image.
     *
     * @param g The graphics environment to paint in.
     */
    public void paint(Graphics g) {
	if (initOk) {
	    g.drawImage(image, xPad, yPad, this);
	}
    }

    /**
     * Gives the size of the image.
     *
     * @return The size of the image.
     */
    public Dimension getPreferredSize() {
	return new Dimension(width, height);
    }

    /**
     * Gives the size of the image.
     *
     * @return The size of the image.
     */
    public Dimension getMinimumSize() {
	return getPreferredSize();
    }

    /**
     * Gives the size of the image.
     *
     * @return The size of the image.
     */
    public Dimension getMaximumSize() {
	return getPreferredSize();
    }

    /**
     * Does nothing. It is not possible to resize the image. This function is
     * for blocking the setSize(..) in java.awt.Panel.
     *
     * @param width The desired width
     * @param height The desired height
     */
    public void setSize(int width, int height) {
	
	// Does nothing.
    }
}









