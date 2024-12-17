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
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
/**
 * This is a panel that always will try to maintain a given ratio.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class RatioPanel extends JPanel implements ComponentListener {

    /**
     * Indicates that the panel should maintain the ratio by not exceeding
     * the width or height, it will always decrease itself to fit in the
     * new environments.
     */
    public static final int FLEXIBLE = 1;

    /**
     * Indicates that the panel should maintain a fixed height and only
     * change the width in order to maintain the ratio.
     */
    public static final int FIXED_HEIGHT = 2;

    private double hRatio;

    private int width;
    private int height;

    private Container parent;
    private Insets pInsets;

    private JPanel mainPanel;

    private int type;

    private boolean isBeingResized;

    /**
     * Initializes the panel as FLEXIBLE type.
     *
     */
    public RatioPanel() {
	super();

	type = FLEXIBLE;

	init();
    }

    /**
     * Initializes the panel.
     *
     * @param type The type of the panel
     */
    public RatioPanel(int type) {
	super();

	this.type = type;

	init();
    }

    private void init() {
	isBeingResized = false;

	setLayout(new GridBagLayout());
	mainPanel = new FixedPanel();
	mainPanel.setLayout(new GridLayout(1, 0, 0, 0));

	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.anchor = GridBagConstraints.CENTER;
	add(mainPanel, gbc);

    }

    /**
     * Initializes the size of the panel.
     *
     */
    public void initSize() {
	parent = getParent();

        try {
            if (parent == null) {
                throw new Exception("Add the RatioPanel to another Component, otherwise it won't know what size to set");
            }

            pInsets = parent.getInsets();

            parent.addComponentListener(this);
            setInternalSize(parent.getWidth() - pInsets.left - pInsets.right,
                            parent.getHeight() - pInsets.top - pInsets.bottom);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void setInternalSize(int pWidth, int pHeight) {

	if (type == FIXED_HEIGHT) {
	    width = (int)((double)pHeight / hRatio);
	    height = pHeight;
	}

	else if (type == FLEXIBLE) {

	    if ((double)pHeight / (double)pWidth > hRatio) {
		width = pWidth;
		height = (int)((double)pWidth * hRatio);
	    }
	    else {
		width = (int)((double)pHeight / hRatio);
		height = pHeight;
	    }
	}

	mainPanel.setSize(width, height);

	if (type != FLEXIBLE) {
	    parent.setSize(width + pInsets.left + pInsets.right,
			   height + pInsets.top + pInsets.bottom);
	}
    }

    /**
     * Sets the ratio for the panel by taking a width, and a height, and
     * comparing them.
     *
     * @param widthRatio A width
     * @param heightRatio A height
     */
    public void setRatio(int widthRatio, int heightRatio) {
	hRatio = (double)heightRatio / (double)widthRatio;
    }

    /**
     * Does nothing.
     *
     * @param event The event.
     */
    public void componentShown(ComponentEvent event) {
    }

    /**
     * Takes care of resizes
     *
     * @param event The resize event
     */
    public void componentResized(ComponentEvent event) {
	if (getParent() == null) {
	    parent.removeComponentListener(this);
	    return;
	}

	if (isBeingResized) {
	    return;
	}

	isBeingResized = true;

	JComponent source = (JComponent)event.getSource();
	Insets i = source.getInsets();
	setInternalSize(source.getWidth() - i.left - i.right,
			source.getHeight() - i.top - i.bottom);
	validate();

	isBeingResized = false;
    }

    /**
     * Does nothing.
     *
     * @param event The event.
     */
    public void componentMoved(ComponentEvent event) {
    }

    /**
     * Does nothing.
     *
     * @param event The event
     */
    public void componentHidden(ComponentEvent event) {
    }

    /**
     * Adds a component to the panel
     *
     * @param c The component to add
     *
     * @return The added component.
     */
    public Component add(Component c) {
	Component retC = mainPanel.add(c);

	Thread t = new Thread() {
		public void run() {
		    try {
			Thread.sleep(1000);
		    }
		    catch (InterruptedException e) {
			// Ignore any exceptions.
		    }

		    validate();
		}
	    };
        t.setName("Ratio Panel add");
	t.start();

	return retC;
    }


    /**
     * This is a Java AWT GUI Panel extension. This class has the properties of
     * a standard Panel, but it differs in one way - it has a fixed size. This
     * size is given in the constructor and then the class will always report
     * that size.
     *
     * @author Andreas Piirimets, Omnitor AB
     */
    public class FixedPanel extends javax.swing.JPanel {

	// The sizes
	double fixWidth;
	double fixHeight;

	/**
	 * Does nothing.
	 *
	 */
	public FixedPanel() {
	    super();
	}

	/**
	 * Initializes the panel with fixed sizes
	 *
	 * @param fixedWidth The width
	 * @param fixedHeight The height
	 */
	public FixedPanel(int fixedWidth, int fixedHeight) {
	    super();

	    fixWidth = (double)fixedWidth;
	    fixHeight = (double)fixedHeight;
	}

	/**
	 * Initializes the panel with fixed sizes and a layout manager
	 *
	 * @param fixedWidth The width
	 * @param fixedHeight The height
	 * @param layout The layout manager to use with the panel
	 */
	public FixedPanel(int fixedWidth, int fixedHeight,
			  java.awt.LayoutManager layout) {
	    super(layout);

	    fixWidth = (double)fixedWidth;
	    fixHeight = (double)fixedHeight;
	}

	/**
	 * Gives the size given in the constructor.
	 *
	 * @return The size given in the constructor.
	 */
	public java.awt.Dimension getPreferredSize() {
	    int returnWidth;
	    int returnHeight;

	    returnWidth = (int)fixWidth;
	    returnHeight = (int)fixHeight;

	    return new java.awt.Dimension(returnWidth, returnHeight);
	}

	/**
	 * Gives the size given in the constructor.
	 *
	 * @return The size given in the constructor.
	 */
	public java.awt.Dimension getMinimumSize() {

	    return getPreferredSize();
	}

	/**
	 * Gives the size given in the constructor.
	 *
	 * @return The size given in the constructor.
	 */
	public java.awt.Dimension getMaximumSize() {

	    return getPreferredSize();
	}

	/**
	 * Changes the size of the panel
	 *
	 * @param width The new fixed width to use
	 * @param height THe new fixed height to use
	 */
	public void setSize(int width, int height) {
	    super.setSize(width, height);

	    fixWidth = width;
	    fixHeight = height;

	    validate();
	}

    }

}