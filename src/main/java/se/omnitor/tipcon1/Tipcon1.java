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
package se.omnitor.tipcon1;

import se.omnitor.tipcon1.gui.DialogFactory;
import se.omnitor.tipcon1.gui.SplashScreen;

import java.awt.Color;
import java.net.URISyntaxException;
import java.io.IOException;

import javax.swing.SwingConstants;

// import LogClasses and Classes to set properties
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;

/**
 * The start-up class for T-Client.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class Tipcon1 {

    // declare package and classname
    public final static String CLASS_NAME = Tipcon1.class.getName();

    // get an instance of Logger
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    // logFile, only used in mainClass
    private final static String logFile = "tipcon1_log.xml";
    private static  String settingFilePath = null;
    // static init for logging, only needed in main-class
    static {

        // write methodname
        final String METHOD = "static { }";

        // declare methodvariables
        FileHandler fileHandler = null;
        Logger rootLogger = null;

        // rootLogger set defaults to all Logger-objects
        rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.FINE);

        //add a FileHandler, so that we log to a File too. (the Console is logged to as default)
        try {
            fileHandler = new FileHandler(logFile);
        }
        catch (IOException ioException) {
            rootLogger.logp(Level.FINE, CLASS_NAME, METHOD, "Error creating/reading log-file", ioException);
        }

        if (fileHandler != null) {
        	rootLogger.addHandler(fileHandler);
        }

        // The root logger's handlers default to INFO. We have to
        // crank them up. We could crank up only some of them
        // if we wanted, but we will turn them all up.
        // set logLevel for handlers

        Handler[] handlers = rootLogger.getHandlers();
        for ( int index = 0; index < handlers.length; index++ ) {
            handlers[index].setLevel(Level.CONFIG);
        }
    }


    /**
     * Checks if the correct version of JMF is install and runs T-Client.
     *
     * @param argv Incoming parameters from the prompt
     */
    public static void main(String[] argv) {
        String settingsFile = null;
        String helpsetPath = null;


        // write methodname
        final String METHOD = "main(String[] argv)";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD, argv);

        // taking care of log file argument
        for (int cnt=0; cnt<argv.length; cnt++) {
            if (argv[cnt].equals("-settings")) {
                if (argv.length > cnt + 1) {
                    settingsFile = argv[cnt+1];
                }
                else {
                    printSyntax();
                    System.exit(0);
                }
            }
            else if (argv[cnt].equals("-helpset")) {
                if (argv.length > cnt+1) {
                    helpsetPath = argv[cnt+1];
                }
                else {
                    printSyntax();
                    System.exit(0);
                }
            }
            else if (argv[cnt].equals("/?")) {
                printSyntax();
                System.exit(0);
            }
        }

    try { 
    	AppConstants.setConstants(settingsFile, helpsetPath);
    }
    catch (IOException e) {
    	logger.throwing("se.omnitor.tipcon1.TIPcon1", "main", e);
    }
    catch (URISyntaxException e) {
    	logger.throwing("se.omnitor.tipcon1.TIPcon1", "main", e);
    }

	String noJmf =
	    "JMF is not installed!\n \n" +
	    "This program requires Java Media Framework v2.1.1e or later.\n" +
	    "The latest version of JMF can be downloaded from: " +
	    "http://java.sun.com";

	String wrongVersion =
	    "And old version of JMF is installed!\n \n" +
	    "This program requires Java Media Framework v2.1.1e or later.\n" +
	    "The latest version of JMF can be downloaded from: " +
	    "http://java.sun.com";

        // Display the splash screen

        SplashScreen splash = null;
        if(settingsFile==null) {
            settingsFile=AppConstants.SETTINGS_DATA_FILE_URL;
        }
        settingFilePath = settingsFile;
        AppSettings appSettings = new AppSettings(settingsFile);
        
        try {
			if((AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1)) {
			    splash = SplashScreen.showSplash(AppConstants.SPLASH_IMAGE_URL_TIPCON1,
			                            AppConstants.PROGRAM_NAME_TIPCON1,
			                            72, 196, Color.LIGHT_GRAY, "",
			                            SwingConstants.LEFT,
			                            70, 216, Color.BLACK, "",
			                            SwingConstants.LEFT,
			                            AppConstants.LOGO_ICON_URL,
			                            settingsFile,
			                            helpsetPath);
			}
			else{
			    splash = SplashScreen.showSplash(AppConstants.SPLASH_IMAGE_URL,
					    AppConstants.PROGRAM_NAME,
					    72, 196, Color.LIGHT_GRAY, "",
					    SwingConstants.LEFT,
					    70, 216, Color.BLACK, "",
					    SwingConstants.LEFT,
					    AppConstants.LOGO_ICON_URL,
			                            settingsFile,
			                            helpsetPath);

			}
		} catch (URISyntaxException e) {
			logger.throwing("se.omnitor.tipcon1.TIPcon1", "main", e);
			throw new RuntimeException("Cannot initiate application paths.");
		} catch (IOException e) {
			logger.throwing("se.omnitor.tipcon1.TIPcon1", "main", e);
			throw new RuntimeException("Cannot initiate application paths.");
		}
	splash.setProgramName(AppConstants.PROGRAM_VERSION);

	splash.changeText("Searching for JMF");

	// Before starting the program, check that JMF is installed with
	// the correct version.
	if (!checkJmf()) {
	    /*
	    try {
		includeJmfJar();
	    }
	    catch (IOException ioe) {
	    */
	    DialogFactory.showErrorMessageDialog(noJmf);
	    /*
	    ErrorMessageDialog emd =
		new ErrorMessageDialog(new Frame(), noJmf);
	    emd.show();
	    */
	    System.exit(-1);
	    /*
	    }
	    */
	}
	if (!checkJmfVersion()) {
	    /*
	    try {
		includeJmfJar();
	    }
	    catch (IOException ioe) {
	    */
	    DialogFactory.showErrorMessageDialog(wrongVersion);
	    /*
	    ErrorMessageDialog emd =
		new ErrorMessageDialog(new Frame(), wrongVersion);
	    emd.show();
	    */
	    System.exit(-1);
	    /*
	    }
	    */
	}

	AppController ac = new AppController(new English(), splash,
					     "se.omnitor.tipcon1",
                                             settingsFile,
                                             helpsetPath);

	// Remove splash
	splash.dispose();

	// Start application
        ac.start();
        logger.exiting(CLASS_NAME, METHOD);
    }
    /**
     * Checks that JMF is installed in the system.
     *
     * @return True if JMF is installed in the system, false otherwise.
     */
    private static boolean checkJmf() {

	// Check for JMF
	try {
	    Class.forName("javax.media.Player");
	}
	catch (Throwable t1) {
	    return false;
	}

	return true;
    }

    /**
     * Checks that the system has JMF 2.1.1e or later installed.
     *
     * @return True if the system has JMF 2.1.1e or later installed, false
     * otherwise.
     */
    private static boolean checkJmfVersion() {

	// Check for JMF 2.0
	try {
	    Class.forName("com.sun.media.util.LoopThread");
	    Class.forName("javax.media.Codec");
	}
	catch (Throwable t2) {
	    return false;
	}

	// Check for JMF 2.1.1e
	if (javax.media.Manager.getVersion().compareToIgnoreCase("2.1.1e")
	    < 0) {

	    return false;
	}

	return true;

    }

    /**
     * Includes the JMF.jar in the classpath.
     *
     */
    /*
    private static void includeJmfJar() throws IOException {
	Class[] parameters = new Class[]{URL.class};
	File f1 = new File("jmf/jmf.jar");
	File f2 = new File("jmf/customizer.jar");
	File f3 = new File("jmf/mediaplayer.jar");
	File f4 = new File("jmf/multiplayer.jar");
	File f5 = new File("jmf/sound.jar");
	URLClassLoader sysloader =
	    (URLClassLoader)ClassLoader.getSystemClassLoader();

	Class sysclass = URLClassLoader.class;

	try {
	    Method method =
		sysclass.getDeclaredMethod("addURL",parameters);

	    method.setAccessible(true);
	    method.invoke(sysloader, new Object[]{ f1.toURL() });
	    method.invoke(sysloader, new Object[]{ f2.toURL() });
	    method.invoke(sysloader, new Object[]{ f3.toURL() });
	    method.invoke(sysloader, new Object[]{ f4.toURL() });
	    method.invoke(sysloader, new Object[]{ f5.toURL() });

	} catch (Throwable t) {

	    t.printStackTrace();
	    throw new IOException
		("Error, could not add URL to system classloader");
	}
    }
    */

    private static void printSyntax() {
        System.out.println("SIPcon1 [-settings <settingsfile>] [-helpset <helpsetfile>] [/?]");
        System.out.println("");
        System.out.println("<settingsfile>  Full path to settings XML file");
        System.out.println("<helpsetfile>   Full directory path to helpset files");
        System.out.println("/?              Show this help screen");
    }
    
    /**
     * This function returns the path of settings file (Settings.xml) 
     * @return String file path of settings file.
     */
    public static String getPathName(){
    	return settingFilePath;
    }
}
