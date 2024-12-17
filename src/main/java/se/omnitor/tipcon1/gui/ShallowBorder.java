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
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.Insets;

/**
 * This class draws a shallow border. That is, a border containing only one
 * pixel of width/height and still making a 3D windows-look-alike effect. <br>
 * <br>
 * The Swing look'n'feel only supports deeper borders, therefore this was
 * created.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class ShallowBorder extends javax.swing.border.AbstractBorder {

    public static final int LOWERED = 1;
    public static final int RAISED = 2;

    private Insets insets;
    private int type;

    /**
     * Initializes a lowered border.
     *
     */
    public ShallowBorder() {
	super();

	insets = new Insets(1, 1, 1, 1);
	type = LOWERED;
    }

    /**
     * Initializes a lowered border with insets.
     *
     * @param top The top margin
     * @param left The left margin
     * @param bottom The bottom margin
     * @param right The right margin
     */
    public ShallowBorder(int top, int left, int bottom, int right) {
	this.insets = new Insets(top+1, left+1, bottom+1, right+1);
	type = LOWERED;
    }

    /**
     * Sets the type of border, this is either LOWERED or RAISED.
     *
     * @param type
     */
    public void setType(int type) {
	this.type = type;
    }

    /**
     * Sets the border's insets
     *
     * @param top The top margin
     * @param left The left margin
     * @param bottom The bottom margin
     * @param right The right margin
     */
    public void setBorderInsets(int top, int left, int bottom, int right) {
	this.insets = new Insets(top+1, left+1, bottom+1, right+1);
    }

    /**
     * Gets the border's insets.
     *
     * @param c Component?
     *
     * @return The insets
     */
    public Insets getBorderInsets(Component c) {
	return insets;
    }

    /**
     * Gets the border's insets.
     *
     * @param c Component?
     * @param i Insets?
     *
     * @return The insets
     */
    public Insets getBorderInsets(Component c, Insets i) {
	return insets;
    }

    /**
     * Paints the border.
     *
     * @param c The component
     * @param g The graphics environment
     * @param x The X coordinate for the border
     * @param y The Y coordinate for the border
     * @param width The width
     * @param height The height
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width,
			    int height) {

	if (type == LOWERED) {
	    g.setColor(SystemColor.controlShadow);
	    g.drawLine(0, 0, width-1, 0);
	    g.drawLine(0, 0, 0, height-2);
	    g.setColor(SystemColor.controlLtHighlight);
	    g.drawLine(0, height-1, width-1, height-1);
	    g.drawLine(width-1, 0, width-1, height-1);
	}

	if (type == RAISED) {
	    g.setColor(SystemColor.controlLtHighlight);
	    g.drawLine(0, 0, width-1, 0);
	    g.drawLine(0, 0, 0, height-2);
	    g.setColor(SystemColor.controlShadow);
	    g.drawLine(0, height-1, width-1, height-1);
	    g.drawLine(width-1, 0, width-1, height-1);
	}

    }

}
