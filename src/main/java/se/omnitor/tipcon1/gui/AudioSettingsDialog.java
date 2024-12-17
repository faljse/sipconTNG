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

import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.swing.JFrame;

import se.omnitor.tipcon1.AppConstants;
import se.omnitor.tipcon1.AppController;
import se.omnitor.tipcon1.AppSettings;

/**
 * This class creates a modal audio settings dialog box by deriving the
 * general audio/video settings dialog box.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class AudioSettingsDialog extends AudioVideoSettingsDialog {

    /**
     * Initializes the settings dialog.
     *
     * @param owner The parent dialog of this dialog
     * @param appController The AppController, which is holding all settings
     */
    public AudioSettingsDialog(JFrame owner, AppSettings appSettings, AppController appController) {
	super(owner, appSettings, appController, "Audio");
    }

    /**
     * Retrieves the settings from the AppController.
     *
     */
    protected void fetchSettings() {

	//localPort = settings.getLocalAudioPort();
	codecs = settings.getAudioCodecs();
	device = settings.getAudioDevice();
	format = settings.getAudioFormats();
    }

    /**
     * Stores settings in the AppController.
     *
     */
    protected void storeSettings() {
	int cnt;
	AudioFormat[] af = new AudioFormat[format.length];

	for (cnt=0; cnt<format.length; cnt++) {
	    af[cnt] = (AudioFormat)format[cnt];
	}

	//settings.setLocalAudioPort(localPort);
	settings.setAudioDevice(device);
	settings.setAudioFormat(af);
	settings.setAudioCodecs(codecs);

	settings.deploySdpSettings(true);
    }


    /**
     * Shows a String representation of the given format.
     *
     * @param format The format which should be expressed as a String
     *
     * @return The String
     */
    protected String formatToString(Format format) {
	return format.toString();
    }

    /**
     * Assigns an audio icon file with the internal imageFile variable.
     *
     */
    protected void setImageFile() {
	imageFile = AppConstants.AUDIO_IMAGE_URL;
    }
}


































