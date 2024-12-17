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
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.swing.JFrame;

import se.omnitor.tipcon1.AppConstants;
import se.omnitor.tipcon1.AppController;
import se.omnitor.tipcon1.AppSettings;

/**
 * This is a modal dialog that enables the user to change the video settings.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class VideoSettingsDialog extends AudioVideoSettingsDialog {

    /**
     * Initializes the dialog.
     *
     * @param owner The parent dialog
     * @param ac The application controller that holds all settings
     */
    public VideoSettingsDialog(JFrame owner, AppSettings ap, AppController ac) {
	super(owner, ap, ac, "Video");
    }

    /**
     * Fetches settings from the application controller.
     *
     */
    public void fetchSettings() {

	//localPort = settings.getLocalVideoPort();
	 codecs = settings.getVideoCodecs();
	 device = settings.getVideoDevice();
	 format = settings.getVideoFormats();
         videofps.setText("" + settings.getvideofps());
        //videobitrate.setText("" + settings.getvideobitrate());
         bitrate = settings.getVideoBitrate();

    }

    /**
     * Stores all settings in the application controller.
     *
     */
    public void storeSettings() {
	int cnt;
	VideoFormat[] vf = new VideoFormat[format.length];
	boolean videoHasChanged = false;

	for (cnt=0; cnt<format.length; cnt++) {
	    vf[cnt] = (VideoFormat)format[cnt];
	}

	 if (device != settings.getVideoDevice()) {
	     videoHasChanged = true;
	 }
	 else {
		 VideoFormat[] svf = settings.getVideoFormats();
		 System.out.println("device=" + device);
		 if ((vf != null && svf != null) &&
			((vf.length > (device)) && (svf.length > device)) &&
			(vf[device] != settings.getVideoFormats()[device])) {
			 videoHasChanged = true;
		 }
	 }

	//settings.setLocalVideoPort(localPort);
	 settings.setVideoDevice(device);
	 settings.setVideoFormat(vf);
	 settings.setVideoCodecs(codecs);
        //settings.setvideobitrate(Integer.parseInt(videobitrate.getText()));
         settings.setvideofps(Integer.parseInt(videofps.getText()));
         settings.setVideoBitrate(bitrates[cobitrates.getSelectedIndex()]);
	 settings.deploySdpSettings(true);
	if (videoHasChanged) {
	     settings.deployVideoSettings();
	}
        settings.restartVideo();
        ap.save();
    }



    /**
     * Gives a string representation of the given video format.
     *
     * @param format A VideoFormat
     *
     * @return A string representation of the format
     */
    public String formatToString(Format format) {
	Dimension tempDim;
	String returnStr;

	tempDim = ((VideoFormat)format).getSize();

	returnStr =
	    (int)tempDim.getWidth() + "x" +
	    (int)tempDim.getHeight() + " (" +
	    format.getEncoding() + ")";
        //System.out.println(returnStr);
	return returnStr;

    }

    /**
     * Assigns the imageFile variable with an URL to the video icon
     *
     */
    public void setImageFile() {
	imageFile = AppConstants.VIDEO_IMAGE_URL;
    }

}
