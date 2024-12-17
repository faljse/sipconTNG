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

import java.util.Properties;

/**
 * @author Andreas Piirimets, Omnitor AB
 */
public class English extends Properties {

    public English() {

        // languages to choose from
        setProperty("se.omnitor.tipcon1.language.swedish",
                    "Swedish");

        setProperty("se.omnitor.tipcon1.language.english",
                    "English");

        setProperty("se.omnitor.tipcon1.gui.fontdialog.FONT_SETTINGS",
                    "Text settings");

        setProperty("se.omnitor.tipcon1.gui.fontdialog.FONT",
                    "Font");

        setProperty("se.omnitor.tipcon1.gui.fontdialog.SIZE",
                    "Size");

        setProperty("se.omnitor.tipcon1.gui.fontdialog.TEXT",
                    "Text");

        setProperty("se.omnitor.tipcon1.gui.fontdialog.BACKGROUND",
                    "Background");

        setProperty("se.omnitor.tipcon1.gui.fontdialog.OUTGOING_TEXT",
                    "Outgoing text");

        setProperty("se.omnitor.tipcon1.gui.fontdialog.INCOMING_TEXT",
                    "Incoming text");

        setProperty("se.omnitor.tipcon1.gui.fontdialog.OK",
                    "OK");

        setProperty("se.omnitor.tipcon1.gui.fontdialog.CANCEL",
                    "Cancel");
        
        setProperty("se.omnitor.tipcon1.gui.fontdialog.DEFAULT",
        			"Restore Default");
        
        setProperty("se.omnitor.tipcon1.gui.fontdialog.RESTORE_DEFAULT_OPTION",
        			"All your current settings will be set to default. Are you sure?");
    	
    	setProperty("se.omnitor.tipcon1.gui.fontdialog.RESTORE_DEFAULT",
        			"Restore Default");
    	
        setProperty("se.omnitor.tipcon1.gui.RESTART_REQUIRED",
                "restart required");

        setProperty("se.omnitor.tipcon1.ProgramWindow.FILE",
                    "File");

        setProperty("se.omnitor.tipcon1.ProgramWindow.REGISTERED",
                    "Registered");

        setProperty("se.omnitor.tipcon1.ProgramWindow.FAILED",
            "Failed");

	setProperty("se.omnitor.tipcon1.ProgramWindow.NOT_REGISTRERED_WITH_SIPSERVER",
                "Not registered with a SIP server");

    	setProperty("se.omnitor.tipcon1.ProgramWindow.CONVERSATION_WITH",
                " Conversation with: ");

        setProperty("se.omnitor.tipcon1.ProgramWindow.SAVE_TEXT", "Save Text");

        setProperty("se.omnitor.tipcon1.ProgramWindow.EXIT", "Exit");

	setProperty("se.omnitor.tipcon1.ProgramWindow.SETTINGS", "Settings");

        setProperty("se.omnitor.tipcon1.ProgramWindow.AUDIO", "Audio");

        setProperty("se.omnitor.tipcon1.ProgramWindow.VIDEO", "Video");

        setProperty("se.omnitor.tipcon1.ProgramWindow.TEXT_SETTINGS", "Text settings");

        setProperty("se.omnitor.tipcon1.ProgramWindow.ALERT", "Alert");

        setProperty("se.omnitor.tipcon1.ProgramWindow.alert.INFO_TEXT",
            "For Alerting to work you need to place\n" +
            "an executable binary named alert2.exe\n" +
            "in c:\\windows\\system32. Enabling the\n" +
            "checkbox will make Sipcon1 execute the\n" +
            "binary when receiving incomming calls.");

        setProperty("se.omnitor.tipcon1.ProgramWindow.TEXT_FONT", "Text font");

        setProperty("se.omnitor.tipcon1.ProgramWindow.SIP", "SIP");

        setProperty("se.omnitor.tipcon1.ProgramWindow.DETECT_DEVICES", "Detect Devices");

        setProperty("se.omnitor.tipcon1.ProgramWindow.LANGUAGE", "Language");

        setProperty("se.omnitor.tipcon1.ProgramWindow.REGISTER", "Register");

        setProperty("se.omnitor.tipcon1.ProgramWindow.HELP", "Help");

        setProperty("se.omnitor.tipcon1.ProgramWindow.HELP_TOPICS", "Help topics");

        setProperty("se.omnitor.tipcon1.ProgramWindow.FUNDING_CREDITS", "Funding credits");

        setProperty("se.omnitor.tipcon1.ProgramWindow.ABOUT", "About");

        setProperty("se.omnitor.tipcon1.ProgramWindow.CALL", "Call");

        setProperty("se.omnitor.tipcon1.ProgramWindow.HANGUP", "Hangup");

        setProperty("se.omnitor.tipcon1.ProgramWindow.EMPTY", "Empty text windows");

        setProperty("se.omnitor.tipcon1.ProgramWindow.NO_LOCAL_VIDEO", "No local video");

	setProperty("se.omnitor.tipcon1.ProgramWindow.NO_REMOTE_VIDEO", "No remote video");

        setProperty("se.omnitor.tipcon1.ProgramWindow.CALL_CONTROL", "Call control");

        setProperty("se.omnitor.tipcon1.ProgramWindow.ADDRESS", "Address");

	setProperty("se.omnitor.tipcon1.ProgramWindow.AUDIO_CONTROL", "Audio control");

        setProperty("se.omnitor.tipcon1.ProgramWindow.SETTINGS", "Settings");

        setProperty("se.omnitor.tipcon1.ProgramWindow.SILENT", "Silent");

        setProperty("se.omnitor.tipcon1.ProgramWindow.MUTE",
                   "Mute");

        setProperty("se.omnitor.tipcon1.ProgramWindow.CONVERSATION_NONE",
                    " Conversation with: None");

        setProperty("se.omnitor.tipcon1.ProgramWindow.DISCONNECTED", "Idle");

    	setProperty("se.omnitor.tipcon1.AppController.GUI_ERROR",
		    "FATAL ERROR!\n\nCannot initiate user interface!\n" +
		    "Terminating program.");

	setProperty("se.omnitor.tipcon1.AppController.INIT_TEXT",
		    "Initializing text transmission");

	setProperty("se.omnitor.tipcon1.AppController.INIT_SIP",
		    "Initializing SIP");

	setProperty("se.omnitor.tipcon1.AppController.SIP_ERROR",
		    "FATAL ERROR:\n \n" +
		    "The SIP client could not be initialized!\n" +
		    "Terminating program.");

	setProperty("se.omnitor.tipcon1.AppController.INIT_NETWORK",
		    "Initializing network");

	setProperty("se.omnitor.tipcon1.AppController.NETWORK_ERROR",
		    "WARNING:\n \n" +
		    "No network was found on this computer. You may not\n" +
		    "be able to receive or place calls!");

	setProperty("se.omnitor.tipcon1.AppController.INIT_CAPTURE",
		    "Initializing capture devices");

	setProperty("se.omnitor.tipcon1.AppController.SETUP_NA",
		    "No setup found! Detecting devices ..");

	setProperty("se.omnitor.tipcon1.AppController.DEVICE_CHANGE",
		    "A device has changed! Detecting ..");

	setProperty("se.omnitor.tipcon1.AppController.LOAD_SETTINGS",
		    "Loading settings");

	setProperty("se.omnitor.tipcon1.AppController.CREATE_GUI",
		    "Creating GUI");

        setProperty("se.omnitor.tipcon1.gui.LanguageDialog.LANGUAGE_SETTINGS",
                    "Language setting");

	setProperty("se.omnitor.tipcon1.AppController.CLOSING",
		    "Closing ..");

	setProperty("se.omnitor.tipcon1.AppController.MEDIA_SETUP",
		    "Connected, setting up media ..");

	setProperty("se.omnitor.tipcon1.AppController.SIP_SEND_PROBLEM",
		    "A problem occured when sending packets to remote.\n" +
		    "Answer process aborted.");

	setProperty("se.omnitor.tipcon1.AppController.CONNECTED",
		    "Connected");

	setProperty("se.omnitor.tipcon1.AppController.MISSED_CALL_FROM",
		    "Missed call from");

	setProperty("se.omnitor.tipcon1.AppController.TERMINATED",
		    "Call terminated.");

	setProperty("se.omnitor.tipcon1.AppController.HOST_CONTACTED",
		    "Host contacted");

	setProperty("se.omnitor.tipcon1.AppController.CALLING",
		    "Calling");

	setProperty("se.omnitor.tipcon1.AppController.TIMEOUT",
		    "Could not establish connection to remote computer.");

	setProperty("se.omnitor.tipcon1.AppController.REJECTED",
		    "Remote user rejected your call.");

	setProperty("se.omnitor.tipcon1.AppController.USER_NOT_ONLINE",
			"Remote user is not online.");

	setProperty("se.omnitor.tipcon1.AppController.QUEUED",
		    "Your call has been placed in a queue.");

	setProperty("se.omnitor.tipcon1.AppController.WAITING_FOR_ANSWER",
		    "Waiting for remote user to answer .. ");

	setProperty("se.omnitor.tipcon1.AppController.INTERNAL_ERROR",
		    "Internal error");

	setProperty("se.omnitor.tipcon1.AppController.DOES_NOT_EXIST",
		    "does not exist!");

	setProperty("se.omnitor.tipcon1.AppController.BUSY",
		    "Remote host is busy.");

	setProperty("se.omnitor.tipcon1.AppController.NO_MEDIA_MATCH",
		    "No matching media available, call is terminated.");

	setProperty("se.omnitor.tipcon1.AppController.REG_TO",
		    "Registering to");

	setProperty("se.omnitor.tipcon1.AppController.PASSWORD_REQUIRED_FOR" ,
		    "Password required for");

	setProperty("se.omnitor.tipcon1.AppController.TRYING" ,
		    "Trying ..");

	setProperty("se.omnitor.tipcon1.AppController.FAILED" ,
		    "Failed!");

	setProperty("se.omnitor.tipcon1.AppController.SIP_PORT_ERROR",
		    "A fatal error occured when setting the SIP port!");

	setProperty("se.omnitor.tipcon1.AppController.INVALID_PROXY",
		    "Invalid proxy address!");

	setProperty("se.omnitor.tipcon1.AppController.USER_SET_ERROR",
		    "Unable to set user!");

	setProperty("se.omnitor.tipcon1.AppController.NO_SIP_ADDRESS",
		    "No SIP address has been entered.");

	setProperty("se.omnitor.tipcon1.AppController.NO_NETWORK" ,
		    "This computer is not connected to a network!\n" +
		    "Calls cannot be placed.");

	setProperty("se.omnitor.tipcon1.AppController.TIPCON1_DEMO_VIDEO" ,
		    "You are using a demo version of TIPcon1. Video in a call\n" +
		    "will only work for five minutes.\n \n" +
		    "If you are interested in a licensed version with no\n" +
		    "video limitations, please contact info@omnitor.se.");

	setProperty("se.omnitor.tipcon1.AppController.ACTIVATE_MEDIA" ,
		    "Please activate at least one media before placing " +
		    "a call!");

        setProperty("se.omnitor.tipcon1.AppController.CALL_ERROR",
                    "Error when sending call invitation!\n \n" +
                    "Please check your network connection and try again.");

        setProperty("se.omnitor.tipcon1.AppController.ADDRESS_ERROR",
                    "Error when sending call invitation!\n \n" +
                    "Please check the recipient address and try again.");

        setProperty("se.omnitor.tipcon1.AppController.WAIT_FOR_DETECT",
                    "Network detection is not yet completed, please wait.");

        setProperty("se.omnitor.tipcon1.AppController.STUN_ERROR",
                    "Cannot reach STUN server, deactivating STUN.\n \n" +
                    "Either you have a UDP blocking firewall that does not\n" +
                    "allow SIPcon1 to reach internet or wrong STUN server\n" +
                    "setting.\n \n" +
                    "Please check your STUN server address setting and try again.");

        setProperty("se.omnitor.tipcon1.AppController.STUN_SERVER_NOT_FOUND",
                    "Cannot find STUN server, deactivating STUN.\n \n" +
                    "Please provide a STUN server in the SIP settings dialog and try again.");

        setProperty("se.omnitor.tipcon1.AppController.UDP_BLOCKING_FW_FOUND",
                    "A UDP blocking firewall is not allowing SIPcon1 to reach internet!\n" +
                    "You may not be able to call or receive calls.\n \n" +
                    "Please configure the firewall to allow SIP traffic.");

        setProperty("se.omnitor.tipcon1.AppController.SYMMETRIC_FW_FOUND",
                    "A symmetric NAT och firewall is found,\n" +
                    "you may not be able to call or receive calls.\n \n" +
                    "Please change firewall or use another internet connection.");

        setProperty("se.omnitor.tipcon1.AppController.SDP_PARSE_ERROR",
                    "Ambiguous data was received, cannot connect the call.");

        setProperty("se.omnitor.tipcon1.AppController.DETECT","Detect network");
        
        setProperty("se.omnitor.tipcon1.AppController.NOT_ACCEPTABLE_HERE",
        			"Your media doesn't match caller's media.\n \nCall could not be established.");

        // Added by Luan, 24 Maj 2007
        setProperty("se.omnitor.protocol.t140.SEND_TEXT", "Send text");

        setProperty("se.omnitor.protocol.t140.RECEIVE_TEXT", "Receive text");

        setProperty("se.omnitor.tipcon1.gui.t140.LOGWINDOW",
            "Log window");

        setProperty("se.omnitor.tipcon1.gui.t140.RECEIVEWINDOW",
            "Receive text");

        setProperty("se.omnitor.tipcon1.gui.t140.SENDWINDOW",
            "Send text");

	setProperty("se.omnitor.tipcon1.gui.DialogFactory.ERROR",
		    "Error");

	setProperty("se.omnitor.tipcon1.gui.DialogFactory.INCOMING_CALL",
		    "Incoming call");

	setProperty("se.omnitor.tipcon1.gui.DialogFactory.ANSWER",
		    "Answer");

	setProperty("se.omnitor.tipcon1.gui.DialogFactory.DECLINE",
		    "Decline");

	setProperty("se.omnitor.tipcon1.gui.DialogFactory.CALLING",
		    "Calling");

	setProperty("se.omnitor.tipcon1.gui.DialogFactory.ADDRESS",
		    "Address");

	setProperty("se.omnitor.tipcon1.gui.DialogFactory.ABORT",
		    "Abort");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.SELECT_ROW_DEL",
		    "Please select a row to delete.");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.SELECT_ROW_EDIT",
		    "Please select a row to edit.");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.DELETE",
		    "Delete");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.EDIT",
		    "Edit");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.DEL_CONFIRM",
		    "Are you sure you want to delete");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.NAME_HEADER",
		    "Name");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.ADDRESS_HEADER",
		    "SIP address");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.NEW_ADDR",
		    "New address");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.EDIT_ADDR",
		    "Edit address");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.OK_BUTTON",
		    "OK");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.CANCEL_BUTTON",
		    "Cancel");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.NEW_BUTTON",
		    "New");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.EDIT_BUTTON",
		    "Edit");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.DEL_BUTTON",
		    "Delete");

        // Added by Luan, 25 Maj 2007
        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_AUDIO",
                    "Audio");

        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_VIDEO",
                    "Video");

        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_TEXT",
                    "Text");

        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_ON",
                    "on");

        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_TRYING",
                    "Trying ..");


        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_OUT_ONLY",
                    "Out only");

        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_IN_ONLY",
                    "In only");

        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_FAILED",
                    "Failed");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.NET_INACTIVE",
		    "Network inactive");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.NET_ACTIVE",
		    "Network active");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.AUDIO_INACTIVE",
		    "Audio inactive");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.AUDIO_ACTIVE",
		    "Audio active");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.VIDEO_INACTIVE",
		    "Video inactive");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.TEXT_INACTIVE",
		    "Text inactive");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.USER_INACTIVE",
		    "User inactive");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.USER_ACTIVE",
		    "User active");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.DIGITS_ONLY",
		    "The port number may only consist of digits.");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.RESTORE_DEFAULT_OPTION",
    "All your current settings will be set to default. Are you sure?");
	
	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.RESTORE_DEFAULT",
    "Restore Default");
	
	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.PORT_IN_RANGE",
		    "Please specify a port in the range 1-65535.");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.PORT_CHANGE",
		    "Changing port, please wait ..");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.INVALID_ADDR",
		    "You have entered an invalid SIP address!\n\n" +
		    "Syntax:\n" +
		    "user@domain.com or \n" +
		    "user@domain.com:port");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.APPLYING",
		    "Applying settings ..");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.NETWORK",
		    "Network");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.SIP_PORT",
		    "SIP port:");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.DIAL_DOMAIN",
		    "Dial domain:");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.OUTBOUND_PROXY",
		    "Outbound proxy:");
        
	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.NETWORK_INTERFACE",
	    "Network Interface:");
        
	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.NET_INTERFACE_AUTO",
	    "Automatic");
        
	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.USER",
		    "User");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.FULL_NAME",
		    "Full name:");
	
	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.ERROR_FULL_NAME",
		    "Username may only contain characters A-Z and 0-9.");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.UNKNOWN_USER",
		    "Unknown user");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.SIP_ADDRESS",
		    "Primary SIP address:");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.REG_ADDRESS",
		    "Registrar address:");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.USERNAME",
		    "Username:");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.PASSWORD",
		    "Password:");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.OK_BUTTON",
		    "OK");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.CANCEL_BUTTON",
		    "Cancel");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.DEFAULT_BUTTON",
    		" Restore Default");
	
	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.TITLE",
		    "SIP Settings");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.STUN",
                    "STUN");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.DISABLED_STUN",
                    "Turn off STUN functionality");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.AUTO_STUN",
                    "Automatic STUN (recommended)");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.FORCED_STUN",
                    "Force enabled STUN");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.STUN_SERVER_ADDRESS",
                    "STUN server address:");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.STUN_STATUS",
                    "STUN status");

        setProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.DETECTING",
                    "Detecting, this may take a while. Please wait ...");

        setProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.AUDIO_SETUP_CHANGED",
                    "Audio setup has been changed!");

        setProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.VIDEO_SETUP_CHANGED",
                    "Video setup has been changed!");

        setProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.AUDIO_AND_VIDEO_SETUP_CHANGED",
                    "Audio and video setup has been changed!");

        setProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.NO_NEW_DEVICES",
                    "No new devices detected.");

        setProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.DETECTION_COMPLETE",
                   "Detection complete");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.SETTINGS",
		    " settings");

        setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.BITRATE",
            "Bitrate:");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog." +
		    "NO_DEVICES",
		    "No devices");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog." +
		    "NO_FORMATS",
		    "No formats");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog." +
		    "NO_CODECS",
		    "No codecs");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog." +
		    "DIGITS_ONLY",
		    "The port number may only consist of digits.");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog." +
		    "PORT_RANGE",
		    "Please specify a port in the range 1-65535.");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog." +
		    "CODECS_TO_USE",
		    "Codecs to use");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.OK",
		    "OK");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.CANCEL",
		    "Cancel");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.DEFAULT",
			"Restore Default");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.RTP_PORT",
		    "RTP port:");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.DEVICE",
		    " device:");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.FORMAT",
		    " format:");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.TEXT_SETTINGS",
		    "Text settings");

	setProperty("se.omnitor.tipcon1.gui.AlertSettingsDialog.ALERT_SETTINGS",
		    "Alert settings");

	setProperty("se.omnitor.tipcon1.gui.AlertSettingsDialog.ALERT_ENABLE",
		    "Enable Alert");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.PORT_DIGITS",
		    "The max CPS value may only consist of digits.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.NOT_NEGATIVE",
		    "The max CPS value may not be negative.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.CPS_DIGITS",
		    "The port number may only consist of digits.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.PORT_RANGE",
		    "Please specify a port in the range 1-65535.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.BT_DIGITS",
		    "The buffer time value may only consist of digits.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.BT_NEGATIVE",
		    "The buffer time value may not be negative.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.RG_DIGITS",
		    "The redundant generations number may only consist of " +
		    "digits.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.RG_NEGATIVE",
		    "The redundant generations number may not be negaitve.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.RTP_PORT",
		    "RTP port:");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.BUFFER_TIME",
		    "Buffer time (ms):");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.MAX_IN_CPS",
		    "Max incoming CPS:");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.RED_GENS",
		    "Redundance level:");

        setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.REAL_TIME",
		    "Real time");

        setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.SEND_ON_RETURN",
                    "Send on return");

        setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.ENABLE_REALTIME_PREVIEW",
        			"Enable Realtime Preview");

        setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.ENABLE_REALTIME_PREVIEW_TIMER",
        			"Enable timer");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.OK",
		    "OK");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.CANCEL",
		    "Cancel");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.DEFAULT",
			"Restore Default");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.REGS",
		    "Registrations");

	setProperty("se.omnitor.tipcon1.gui.SipRegistrationPanel.SIP_ADDRESS",
		    "SIP address");

	setProperty("se.omnitor.tipcon1.gui.SipRegistrationPanel.REGISTRAR",
		    "Registrar");

	setProperty("se.omnitor.tipcon1.gui.SipRegistrationPanel.USER_NAME",
		    "Username");

	setProperty("se.omnitor.tipcon1.gui.SipRegistrationPanel.PASSWORD",
		    "Password");

	setProperty("se.omnitor.tipcon1.gui.SipRegistrationPanel.NEW_REG",
		    "New registration");

	setProperty("se.omnitor.tipcon1.gui.SipRegistrationPanel.EDIT_REG",
		    "Edit registration");

	setProperty("se.omnitor.tipcon1.gui.FundingCredits.HEADER",
		    "Funding for the development of this reference design Voice+Text IP Phone\n"+
		    "was provided by the following sponsors");

        setProperty("se.omnitor.tipcon1.gui.FundingCredits.HEADER_TIPCON1",
                            "Funding for the development of this reference design Video+Voice+Text IP Phone\n"+
		    "was provided by the following sponsors");

	setProperty("se.omnitor.tipcon1.gui.FundingCredits.SPONSOR1",
		    "National Institute on Disability and Rehabilitation Research,\n"+
		    "US Department of Education under grant # H133E040013");

	setProperty("se.omnitor.tipcon1.gui.Register.TITLE",
		    "Registration info");

        setProperty("se.omnitor.tipcon1.gui.Register.INFO_TIPCON1",
                            "For a fully functional Tipcon1 you need a SIP address.\n"+
                            "The SIP address looks like an e-mail address and allows your\n"+
                            "friends to reach you whereever you are*.\n\n"+
                            "If the text \"Not registered with a SIP server\"\n"+
                            "is displayed in the bottom right hand corner, Tipcon1\n"+
                            "is currently not configured to use a SIP address.\n\n"+
                            "Many companies provide SIP addresses but most companies\n"+
                            "charge a small fee for this service.\n"+
                            "You can try SIP by registering to yourname@trysip.ingate.com\n"+
                            "with username yourname and no password.\n\n"+
                            "Read more on: http://www.ingate.com/trysip.php\n\n"+
                            "Please refer to the Tipcon1 manual for info on how to\n"+
                            "register to a SIP server.\n\n"+
                            "To conveniently place and receive calls it is necessary\n"+
                            "be registered to a SIP server with your SIP address.\n"+
                            "It is possible to use Tipcon1 without a SIP address.\n"+
                            "- You can call friends who have a SIP address and\n"+
                            "  they can call you if you have a public IP address.\n\n"+
                            "  Placing and receiving calls will not be possible\n"+
                            "  if Tipcon1 is behind a firewall or some other NAT using\n"+
                    "  symmetric address translation.");

	setProperty("se.omnitor.tipcon1.gui.Register.INFO",
                    "For a fully functional SIPcon1 you need a SIP address.\n"+
                    "The SIP address looks like an e-mail address and allows your\n"+
                    "friends to reach you whereever you are*.\n\n"+
                    "If the text \"Not registered with a SIP server\"\n"+
                    "is displayed in the bottom right hand corner, SIPcon1\n"+
                    "is currently not configured to use a SIP address.\n\n"+
                    "Many companies provide SIP addresses but most companies\n"+
                    "charge a small fee for this service.\n"+
                    "You can try SIP by registering to yourname@trysip.ingate.com\n"+
                    "with username yourname and no password.\n\n"+
                    "Read more on: http://www.ingate.com/trysip.php\n\n"+
                    "Please refer to the SIPcon1 manual for info on how to\n"+
                    "register to a SIP server.\n\n"+
                    "To conveniently place and receive calls it is necessary\n"+
                    "be registered to a SIP server with your SIP address.\n"+
                    "It is possible to use SIPcon1 without a SIP address.\n"+
                    "- You can call friends who have a SIP address and\n"+
                    "  they can call you if you have a public IP address.\n\n"+
                    "  Placing and receiving calls will not be possible\n"+
                    "  if SIPcon1 is behind a firewall or some other NAT using\n"+
                    "  symmetric address translation.");


	/*setProperty("se.omnitor.tipcon1.gui.AboutDialog.FUNDING",
		    "This work was partially funded by the NIDRR,\n"+
		    "US Dept of Education under Grant H133E040013\n"+
		    "as part of the Telecommunication Access RERC of\n"+
		    "the Univ. of Wisconsin-Madison's Trace Center\n"+
		    "joint with Gallaudet University.");*/

	setProperty("se.omnitor.tipcon1.gui.AboutDialog.FUNDING",
		    "This work was partially funded by the NIDRR, US Dept of Education\n"+
		    "under Grant H133E040013 as part of the Telecommunication Access RERC of\n"+
		    "the Univ. of Wisconsin-Madison's Trace Center joint with Gallaudet University.");
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        /*setProperty("se.omnitor.tipcon1.gui.Register.INFOA",
		    "For a fully functional SIPcon1 you need a SIP address.\n"+
                    "The SIP address looks like an e-mail address and allows your\n"+
                    "friends to reach you whereever you are*.\n\n"+
                    "If the text \"Not registered with a SIP server\"\n"+
		    "is displayed in the bottom right hand corner, SIPcon1\n"+
		    "is currently not configured to use a SIP address.\n\n"+
		    "Many companies provide SIP addresses but most companies\n"+
		    "charge a small fee for this service.\n"+
		    "You can try SIP by registering to yourname@trysip.ingate.com\n"+
		    "with username yourname and no password.\n\n"+
		    "Read more on:\n");

	setProperty("se.omnitor.tipcon1.gui.Register.INGATEURL",
		    "http://www.ingate.com/trysip.php");

	setProperty("se.omnitor.tipcon1.gui.Register.INFOB",
		    "Please refer to the SIPcon1 manual for info on how to\n"+
		    "register to a SIP server.\n\n"+
		    "It is possible to use SIPcon1 without a SIP address.\n"+
		    "- You can call friends who have a SIP address and\n"+
		    "  they can call you if they know your IP address.*\n\n"+
		    "* Placing and receiving calls will not be possible\n"+
		    "  if SIPcon1 is behind a firewall that is not SIP \n"+
		    "  compatible and you wish to call someone outside the\n"+
		    "  firewall.\n"+
		    "  Also SIPcon1 needs to be registered to a SIP-server\n"+
		    "  to place and recieve calls behind a SIP-compatible firewall.");*/

    }

    private void jbInit() throws Exception {
    }

}




