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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Holds static information for the whole application.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class AppConstants {

  /**
    * supported languages
    */
    public static final int ENGLISH = 0;
    public static final int SWEDISH = 1;

    /**
     * Specifies if debug text should be printed on the screen or not.
     *
     */
    public static final boolean DEBUG = false;

    /**
     * Specifies the name of the program
     *
     */
    public static final String PROGRAM_NAME = "SIPcon1";
    public static final String PROGRAM_NAME_TIPCON1 = "TIPcon1";

    /**
     * Specifies the program type
     */
    public static final int SIPCON1 = 1;
    public static final int TIPCON1 = 2;
    public static final int PROGRAM_TYPE = AppConstantsSpecific.PROGRAM_TYPE;
    
    /**
     * Specifies the program version.
     *
     */
    public static final String PROGRAM_VERSION = "v1.4.75";

    /**
     * Default public STUN server
     *
     */
    public static final String PUBLIC_STUN_SERVER = "stun.fwdnet.net";

    /**
     * The root directory for the application
     */
    public static String ROOT_DIR_URL;

    /**
     * The URL to the image directory
     */
    public static String IMAGE_DIR_URL;

    /**
     * The URL to the splash image
     */
    public static  String SPLASH_IMAGE_URL;
    public static  String SPLASH_IMAGE_URL_TIPCON1;

    /**
     * The URL to the application's logotype
     */
    public static String LOGO_ICON_URL;

    /**
     * The URL to the Omnitor logotype
     */
    public static String OMNITOR_LOGO_URL;

    /**
     * The URL to the Trace Center logotype
     */
    public static String TRACECENTER_LOGO_URL;

    /**
     * The URL to the audio image
     */
    public static String AUDIO_IMAGE_URL;

    /**
     * The URL to the microphone image
     */
    public static String MIC_IMAGE_URL;

    /**
     * The URL to the inactive audio image
     */
    public static String AUDIO_INACTIVE_ICON_URL;

    /**
     * The URL to the network image
     */
    public static String NETWORK_IMAGE_URL;

    /**
     * The URL to the active network icon
     */
    public static String NETWORK_ACTIVE_ICON_URL;

    /**
     * The URL to the inactive network icon
     */
    public static String NETWORK_INACTIVE_ICON_URL;

    /**
     * The URL to the text image
     */
    public static String TEXT_IMAGE_URL;

    /**
     * The URL to the inactive text icon
     */
    public static String TEXT_INACTIVE_ICON_URL;

    /**
     * The URL to the user image
     */
    public static String USER_IMAGE_URL;

    /**
     * The URL to the active user icon
     */
    public static String USER_ACTIVE_ICON_URL;

    /**
     * The URL to the inactive user icon
     */
    public static String USER_INACTIVE_ICON_URL;

    /**
     * The URL to the video image
     */
    public static String VIDEO_IMAGE_URL;

    /**
     * The URL to the inactive video icon
     */
    public static String VIDEO_INACTIVE_ICON_URL;

    /**
     * The URL to the address book XML data file
     */
    public static String ADDRESS_BOOK_DATA_FILE_URL;

    /**
     * The URL to the settings XML data file
     */
    protected static String SETTINGS_DATA_FILE_URL;

    /**
     * The URL to the ring sound audio file
     */
    public static String RING_SOUND_URL;

    public static String HELPSET_URL;

    private static String customSettingsFile = null;

    /**
     * Sets all constants.
     *
     */
    public static void setConstants(String customSettingsFile, String customHelpsetPath) throws IOException,URISyntaxException {
	String baseDir = null;

	baseDir =
	    AppConstants.class.getProtectionDomain().
	    getCodeSource().getLocation().toString();

	// Windows 2000 machines won't make UTF-8 code of URL.toString(),
	// all other platforms do, though.
	String[] bSpl = baseDir.split(" ");
	if (bSpl.length > 1) {
	    baseDir = "";
	    for (int cnt=0; cnt<bSpl.length; cnt++) {
		baseDir += bSpl[cnt];
		if (cnt+1 < bSpl.length) {
		    baseDir += "%20";
		}
	    }
	}

	if (baseDir.endsWith(".jar")) {

	    String[] splitted = baseDir.split("/");
	    baseDir = "";

	    for (int cnt=0; cnt<splitted.length-1; cnt++) {
		baseDir += splitted[cnt] + "/";
	    }

	}

    baseDir =
		new File(new URI(baseDir)).getCanonicalPath() + File.separator;

	String s = File.separator;

	ROOT_DIR_URL = baseDir + "se"+s+"omnitor"+s+"tipcon1"+s;


	IMAGE_DIR_URL =
	    ROOT_DIR_URL + "gui" + File.separator + "images" + File.separator;

	SPLASH_IMAGE_URL = IMAGE_DIR_URL + "sipcon1-splash.gif";
        SPLASH_IMAGE_URL_TIPCON1 = IMAGE_DIR_URL + "splash.gif";
	LOGO_ICON_URL = IMAGE_DIR_URL + "logoIcon.gif";
	OMNITOR_LOGO_URL = IMAGE_DIR_URL + "omnitorLogo.gif";
	TRACECENTER_LOGO_URL = IMAGE_DIR_URL + "traceCenterLogo.gif";

	AUDIO_IMAGE_URL = IMAGE_DIR_URL + "audio.gif";
	MIC_IMAGE_URL   = IMAGE_DIR_URL + "mic.gif";
	AUDIO_INACTIVE_ICON_URL = IMAGE_DIR_URL + "audio-small.gif";
	NETWORK_IMAGE_URL = IMAGE_DIR_URL + "network.gif";
	NETWORK_ACTIVE_ICON_URL = IMAGE_DIR_URL + "network-small.gif";
	NETWORK_INACTIVE_ICON_URL = IMAGE_DIR_URL + "network-small.gif";
	TEXT_IMAGE_URL =	IMAGE_DIR_URL + "text.gif";
	TEXT_INACTIVE_ICON_URL = IMAGE_DIR_URL + "text-small.gif";
	USER_IMAGE_URL =	IMAGE_DIR_URL + "user.gif";
	USER_ACTIVE_ICON_URL = IMAGE_DIR_URL + "user-small.gif";
	USER_INACTIVE_ICON_URL = IMAGE_DIR_URL + "user-small.gif";
	VIDEO_IMAGE_URL = IMAGE_DIR_URL + "video.gif";
	VIDEO_INACTIVE_ICON_URL = IMAGE_DIR_URL + "video-small.gif";

	ADDRESS_BOOK_DATA_FILE_URL = ROOT_DIR_URL + "AddressBook.xml";
        if (customSettingsFile == null) {
            SETTINGS_DATA_FILE_URL = ROOT_DIR_URL + "Settings.xml";
        }
        else {
            SETTINGS_DATA_FILE_URL = customSettingsFile;
        }
	RING_SOUND_URL = ROOT_DIR_URL + "gui"+s+"sounds"+s+"ring.wav";

        if (customHelpsetPath != null) {
            if (!customHelpsetPath.endsWith(s)) {
                HELPSET_URL = customHelpsetPath+s+"Master.hs";

            }
            else {
                HELPSET_URL = customHelpsetPath+"Master.hs";

            }
        }
        else {
            HELPSET_URL = ROOT_DIR_URL + "helpset" + s + "Master.hs";
        }
    }

}
