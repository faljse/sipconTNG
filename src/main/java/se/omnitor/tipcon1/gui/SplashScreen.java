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
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import java.io.IOException;
import java.net.URISyntaxException;

import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingConstants;

import se.omnitor.tipcon1.AppConstants;

/**
 * Displays a window while the application is initializing.
 *
 * @author Erik Zetterstrm, Omnitor AB
 * @author Andreas Piirimets, Omnitor AB
 */
public class SplashScreen extends JFrame {

    private Image image;
    private boolean paintHasBeenCalled;
    private String splashText = "";
    private String programText = "";
    private boolean initOk;
    private Logger logger = Logger.getLogger("se.omnitor.tipcon1.gui");

    int programTextX;
    int programTextY;
    Color programColor;
    Font programFont;
    int programAlign;
    int splashTextX;
    int splashTextY;
    Color splashColor;
    Font splashFont;
    int splashAlign;

    /**
     * Shows the splash
     *
     * @param imageFile The file to use as splash
     * @param programName The name of the program to show to the user
     *
     * @return The created object
     */
    public static SplashScreen showSplash(String imageFile,
					  String programName,
					  int programTextX,
					  int programTextY,
					  Color programColor,
					  String programFont,
					  int programAlign,
					  int splashTextX,
					  int splashTextY,
					  Color splashColor,
					  String splashFont,
					  int splashAlign,
					  String iconFileName,
                                          String settingsFile,
                                          String helpsetPath) throws URISyntaxException, IOException {
        Frame owner = new Frame();

        SplashScreen sw = new SplashScreen(owner, imageFile, programName,
					   programTextX, programTextY,
					   programColor, programFont,
					   programAlign,
					   splashTextX, splashTextY,
					   splashColor, splashFont,
					   splashAlign, iconFileName,
                                           settingsFile,
                                           helpsetPath);


	if (sw.isInitOk()) {

	    sw.toFront();
	    sw.setVisible(true);
	    
	    // Disabling the right click close functionality on the taskbar splash screen icon
	    sw.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

	    // Wait for the splash screen to be painted
	    if (!EventQueue.isDispatchThread()) {
		synchronized(sw) {
		    while (!sw.paintHasBeenCalled) {
			try {
			    sw.wait();
			} catch(InterruptedException ie) {
			    // Ignore interruptions, just continue
			}
		    }
		}
	    }

	}


        return sw;
    }

    /**
     * Changes the text in the splash screen
     *
     * @param text The new text which should appear on the splash screen
     */
    public void changeText(String text) {
	if (isInitOk()) {
	    splashText = text;

	    paintHasBeenCalled = false;

	    repaint();
	}
    }

    /**
     * Sets the program name to the splash screen
     *
     * @param text The program name which should appear on the splash screen
     */
    public void setProgramName(String text) {
	if (isInitOk()) {
	    programText = text;

	    paintHasBeenCalled = false;

	    repaint();
	}
    }


    /**
     * Constructs a new splashscreen.
     *
     * @param owner The owner frame of the splash screen window
     * @param imageFile The file the image to display is located in.
     * @param programName The name of the program to show to the user
     */
    public SplashScreen(Frame owner, String imageFile, String programName,
			int programTextX, int programTextY,
			Color programColor, String programFont,
			int programAlign,
			int splashTextX, int splashTextY,
			Color splashColor, String splashFont,
			int splashAlign, String iconFileName,
                        String customSettingsFile,
                        String customHelpsetPath) throws URISyntaxException, IOException {

        //super(owner);
	super();

	AppConstants.setConstants(customSettingsFile, customHelpsetPath);

	this.programTextX = programTextX;
	this.programTextY = programTextY;
	this.programColor = programColor;
	this.programFont = Font.decode(programFont);
	this.programAlign = programAlign;
	this.splashTextX = splashTextX;
	this.splashTextY = splashTextY;
	this.splashColor = splashColor;
	this.splashFont = Font.decode(splashFont);
	this.splashAlign = splashAlign;

	initOk = true;

	Toolkit toolkit = Toolkit.getDefaultToolkit();

	// Apply icon
	Image icon = toolkit.getImage(iconFileName);
	setIconImage(icon);
	setUndecorated(true);
	setTitle(programName);

	try {
	    image = toolkit.getImage(imageFile);

	    MediaTracker tracker = new MediaTracker(this);
	    tracker.addImage(image, 0);

	    tracker.waitForID(0);

	    if (tracker.isErrorAny()) {
		initOk = false;
	    }
	}
	catch (InterruptedException e) {
	    // Something went wrong - don't show the splash screen!
		logger.throwing(this.getClass().getName(), "<init>", e);
	    initOk = false;
	}

	if (initOk) {
	    int w = image.getWidth(this);
	    int h = image.getHeight(this);
	    this.setSize(w,h);

	    this.setLocation(GuiToolkit.getCenterX() - w/2,
			     GuiToolkit.getCenterY() - h/2);

	    paintHasBeenCalled = false;
	}

    }

    /**
     * Checks if the initalization was OK or not.
     *
     * @return True if splash screen initialization was ok.
     */
    public boolean isInitOk() {
	return initOk;
    }

    /**
     * Paints the splash screen.
     *
     * @param g The graphics to paint.
     */
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);

	FontMetrics fm;
	int x;

	g.setFont(splashFont);
	g.setColor(splashColor);
	if (splashAlign == SwingConstants.CENTER) {
	    fm = g.getFontMetrics();
	    x = splashTextX - fm.stringWidth(splashText) / 2;
	}
	else if (splashAlign == SwingConstants.RIGHT) {
	    fm = g.getFontMetrics();
	    x = splashTextX - fm.stringWidth(splashText);
	}
	else {
	    x = splashTextX;
	}
	g.drawString(splashText, x, splashTextY);

	g.setFont(programFont);
	g.setColor(programColor);
	if (programAlign == SwingConstants.CENTER) {
	    fm = g.getFontMetrics();
	    x = programTextX - fm.stringWidth(programText) / 2;
	}
	else if (programAlign == SwingConstants.RIGHT) {
	    fm = g.getFontMetrics();
	    x = programTextX - fm.stringWidth(programText);
	}
	else {
	    x = programTextX;
	}
	g.drawString(programText, x, programTextY);

	/*
	g.drawString(splashText,
		     getWidth()/2 -
		     g.getFontMetrics().stringWidth(splashText)/2,
		     190);
	*/

	/*
	g.drawString(programText,
		     getWidth()/2 -
		     g.getFontMetrics().stringWidth(programText)/2,
		     170);
	*/

        if (!paintHasBeenCalled) {
            paintHasBeenCalled = true;
            synchronized(this) {
                notifyAll();
            }
        }
    }

    /**
     * Does not repaint the background color, this removes flicker.
     *
     * @param g The graphics area to paint on.
     */
    public void update(Graphics g) {
        paint(g);
    }

}
