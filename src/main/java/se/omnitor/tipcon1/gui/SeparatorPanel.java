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

import java.awt.Graphics;
import java.awt.SystemColor;
import javax.swing.JPanel;

/**
 * This class defines a separator GUI element in Windows style. The separator
 * is simply a 3D line, which adjusts itself to the current width. The width
 * may be changed, but the height will never be more than two pixels.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class SeparatorPanel extends JPanel {

    /**
     * Initializes the panel.
     *
     */
    public SeparatorPanel() {
	super();
    }
    
    /**
     * Updates the panel. This function simply calls the paint(g) function.
     *
     * @param g The graphics environment to paint in.
     */
    public void update(Graphics g) {
	paint(g);
    }
    
    /**
     * Paints the separator panel.
     *
     * @param g The graphics environment to paint in.
     */
    public void paint(Graphics g) {
	int width;
	int y;

	width = this.getWidth();
	y = this.getHeight()/2;

	g.setColor(SystemColor.controlShadow);
	g.drawLine(0, y, width-1, y);
	g.setColor(SystemColor.controlLtHighlight);
	g.drawLine(0, y+1, width, y+1);
	g.drawLine(width, y, width, y+1);
    }
}









