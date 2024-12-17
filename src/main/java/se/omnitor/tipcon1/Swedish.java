/*
 * Open Source Exemplar Softwarest
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

/**
 * @author Andreas Piirimets, Omnitor AB
 */
public class Swedish extends English {

    public Swedish() {

        setProperty("se.omnitor.tipcon1.gui.fontdialog.FONT_SETTINGS",
                    "TextinstOllningar"); //TODO: wtf

        setProperty("se.omnitor.tipcon1.gui.fontdialog.FONT",
                    "Typsnitt");

        setProperty("se.omnitor.tipcon1.gui.fontdialog.SIZE",
            "Storlek");

	setProperty("se.omnitor.tipcon1.gui.fontdialog.TEXT",
	            "Text");

	setProperty("se.omnitor.tipcon1.gui.fontdialog.BACKGROUND",
	            "Bakgrund");

	setProperty("se.omnitor.tipcon1.gui.fontdialog.OUTGOING_TEXT",
	            "Utg�ende text");

	setProperty("se.omnitor.tipcon1.gui.fontdialog.INCOMING_TEXT",
	            "Inkommande text");

	setProperty("se.omnitor.tipcon1.gui.fontdialog.OK",
	            "OK");

	setProperty("se.omnitor.tipcon1.gui.fontdialog.CANCEL",
	            "Avbryt");

	setProperty("se.omnitor.tipcon1.gui.fontdialog.DEFAULT",
	"Standardinst�llningar");
	
	setProperty("se.omnitor.tipcon1.gui.fontdialog.RESTORE_DEFAULT_OPTION",
				"Alla nuvarande inst�llningar byts mot\nstandardinst�llningar. �r du s�ker?");
	  
	setProperty("se.omnitor.tipcon1.gui.fontdialog.RESTORE_DEFAULT",
				"Standardinst�llningar");
	
    setProperty("se.omnitor.tipcon1.ProgramWindow.FILE",
                    "Arkiv");

        setProperty("se.omnitor.tipcon1.ProgramWindow.SAVE_TEXT", "Spara text");

        setProperty("se.omnitor.tipcon1.ProgramWindow.EXIT", "Avsluta");

        setProperty("se.omnitor.tipcon1.ProgramWindow.SETTINGS", "Inst�llningar");

	setProperty("se.omnitor.tipcon1.ProgramWindow.AUDIO", "Ljud");

	setProperty("se.omnitor.tipcon1.ProgramWindow.VIDEO", "Video");

	setProperty("se.omnitor.tipcon1.ProgramWindow.TEXT_SETTINGS", "Textinst�llningar");

	setProperty("se.omnitor.tipcon1.ProgramWindow.ALERT", "Varseblivning");

        setProperty("se.omnitor.tipcon1.ProgramWindow.alert.INFO_TEXT",
            "F�r att Varseblivning ska fungera beh�ver\n" +
	    "du placera en k�rbar bin�rfil som heter\n" +
	    "alert2.exe i c:\\windows\\system32. Kryssa\n" +
	    "i rutan och Sipcon1 kommer att k�ra \n" +
	    "bin�rfilen vid inkommande samtal.");

	setProperty("se.omnitor.tipcon1.ProgramWindow.TEXT_FONT", "Text typsnitt");

	setProperty("se.omnitor.tipcon1.ProgramWindow.SIP", "SIP");

	setProperty("se.omnitor.tipcon1.ProgramWindow.DETECT_DEVICES", "Initialisera enheter ");

	setProperty("se.omnitor.tipcon1.ProgramWindow.LANGUAGE", "Spr�k");

	setProperty("se.omnitor.tipcon1.ProgramWindow.REGISTER", "Registrera");

	setProperty("se.omnitor.tipcon1.ProgramWindow.HELP", "Hj�lp");

	setProperty("se.omnitor.tipcon1.ProgramWindow.HELP_TOPICS", "Hj�lpavsnitt");

	setProperty("se.omnitor.tipcon1.ProgramWindow.FUNDING_CREDITS", "Om bidragsgivarna");

	setProperty("se.omnitor.tipcon1.ProgramWindow.ABOUT", "Om");


        // languages to choose from
	setProperty("se.omnitor.tipcon1.language.swedish",
            "Svenska");

	setProperty("se.omnitor.tipcon1.language.english",
            "Engelska");

	setProperty("se.omnitor.tipcon1.gui.RESTART_REQUIRED",
             "omstart kr�vs");

	setProperty("se.omnitor.tipcon1.AppController.GUI_ERROR",
		    "ALLVARLIGT FEL!\n\nKan inte skapa anv�ndargr�nssnitt!\n" +
		    "Programmet avslutas.");

	setProperty("se.omnitor.tipcon1.AppController.INIT_TEXT",
		    "Initialiserar textkommunikation");

	setProperty("se.omnitor.tipcon1.AppController.INIT_SIP",
		    "Initialiserar SIP");

	setProperty("se.omnitor.tipcon1.AppController.SIP_ERROR",
		    "ALLVARLIGT FEL!\n\n" +
		    "SIP-klienten kunde inte bli initialiserad!\n" +
		    "Programmet avslutas.");

	setProperty("se.omnitor.tipcon1.AppController.INIT_NETWORK",
		    "Initialiserar n�tverk");

	setProperty("se.omnitor.tipcon1.AppController.NETWORK_ERROR",
		    "VARNING:\n\n" +
		    "Inget n�tverk hittades p� denna dator. Du kommer\n" +
		    "kanske inte kunna ringa eller ta emot samtal.");

	setProperty("se.omnitor.tipcon1.AppController.INIT_CAPTURE",
		    "Initialiserar enheter");

	setProperty("se.omnitor.tipcon1.AppController.SETUP_NA",
		    "Enhetskonfiguration saknades, s�ker enheter .. ");

	setProperty("se.omnitor.tipcon1.AppController.DEVICE_CHANGE",
		    "Enhetskonfigurationen har �ndrats! S�ker enheter ..");

	setProperty("se.omnitor.tipcon1.AppController.LOAD_SETTINGS",
		    "Laddar inst�llningar");

	setProperty("se.omnitor.tipcon1.AppController.CREATE_GUI",
		    "Skapar anv�ndargr�nssnitt");

	setProperty("se.omnitor.tipcon1.AppController.CLOSING",
		    "St�nger ..");

	setProperty("se.omnitor.tipcon1.AppController.MEDIA_SETUP",
		    "Mottagaren har svarat, kopplar upp media ..");

	setProperty("se.omnitor.tipcon1.AppController.SIP_SEND_PROBLEM",
		    "Ett problem uppstod vid pakets�ndning, \n" +
		    "samtalet kopplas ner.");

	setProperty("se.omnitor.tipcon1.AppController.CONNECTED",
		    "Ansluten");

	setProperty("se.omnitor.tipcon1.AppController.MISSED_CALL_FROM",
		    "Missat samtal fr�n");

	setProperty("se.omnitor.tipcon1.AppController.TERMINATED",
		    "Samtalet nerkopplat.");

	setProperty("se.omnitor.tipcon1.AppController.HOST_CONTACTED",
		    "Ansluten");

	setProperty("se.omnitor.tipcon1.AppController.CALLING",
		    "Ringer");

	setProperty("se.omnitor.tipcon1.AppController.TIMEOUT",
		    "Mottagarens utrustning svarar inte.");

	setProperty("se.omnitor.tipcon1.AppController.REJECTED",
			"Ditt samtal blev avvisat.");

	setProperty("se.omnitor.tipcon1.AppController.USER_NOT_ONLINE",
    		"Anv�ndaren har terminalen avst�ngd.");

	setProperty("se.omnitor.tipcon1.AppController.QUEUED",
		    "Ditt samtal har placerats i k�.");

	setProperty("se.omnitor.tipcon1.AppController.WAITING_FOR_ANSWER",
		    "V�ntar p� att mottagaren skall svara ..");

	setProperty("se.omnitor.tipcon1.AppController.INTERNAL_ERROR",
		    "Internt fel");

	setProperty("se.omnitor.tipcon1.AppController.DOES_NOT_EXIST",
		    "�r inte inloggad.");

	setProperty("se.omnitor.tipcon1.AppController.BUSY",
		    "Mottagaren �r upptagen i ett samtal.");

	setProperty("se.omnitor.tipcon1.AppController.NO_MEDIA_MATCH",
		    "Mottagarens utrustning �r inte kompatibel!\n\n" +
		    "Om audio eller text �r inaktiverat, aktivera\n" +
		    "och f�rs�k igen.");

	setProperty("se.omnitor.tipcon1.AppController.REG_TO",
		    "Registrerar till");

	setProperty("se.omnitor.tipcon1.AppController.PASSWORD_REQUIRED_FOR" ,
		    "L�senord kr�vs f�r");

	setProperty("se.omnitor.tipcon1.AppController.TRYING" ,
		    "Trying ..");

	setProperty("se.omnitor.tipcon1.AppController.FAILED" ,
		    "Misslyckades!");

	setProperty("se.omnitor.tipcon1.AppController.SIP_PORT_ERROR",
		    "Ett allvarligt fel uppstod n�r SIP-porten �ndrades!");

	setProperty("se.omnitor.tipcon1.AppController.INVALID_PROXY",
		    "Ogiltig proxy-adress!");

	setProperty("se.omnitor.tipcon1.AppController.USER_SET_ERROR",
		    "Kan inte �ndra anv�ndaren!");

	setProperty("se.omnitor.tipcon1.AppController.NO_SIP_ADDRESS",
		    "Ingen SIP-adress har angivits.");

	setProperty("se.omnitor.tipcon1.AppController.NO_NETWORK" ,
		    "Det g�r inte att ringa, datorn �r inte kopplad\n" +
		    "till ett n�tverk!\n");

	setProperty("se.omnitor.tipcon1.AppController.TIPCON1_DEMO_VIDEO" ,
		    "Du anv�nder en demoversion av TIPcon1. Videobilden i samtal\n" +
		    "fungerar endast i fem minuter.\n \n" +
		    "V�nligen kontakta info@omnitor.se om du �r intresserad\n" +
		    "av en licensierad version utan begr�nsningar.");

	setProperty("se.omnitor.tipcon1.AppController.ACTIVATE_MEDIA" ,
		    "Minst ett media (audio eller text) m�ste vara " +
		    "aktiverat f�r att samtal skall kunna g�ras.");

        setProperty("se.omnitor.tipcon1.AppController.CALL_ERROR",
                    "Ett fel uppstod n�r samtalsf�rfr�gan skickades!\n\n" +
                    "Var god kontrollera n�tverksinst�llningarna och " +
                    "f�rs�k igen.");

        setProperty("se.omnitor.tipcon1.AppController.ADDRESS_ERROR",
                    "Ett fel uppstod n�r samtalsf�rfr�gan skickades!\n\n" +
                    "Var god kontrollera mottagaradressen och " +
                    "f�rs�k igen.");

        setProperty("se.omnitor.tipcon1.AppController.WAIT_FOR_DETECT",
                    "Detekteringen av n�tverket �r ej f�rdig, vad god v�nta.");

        setProperty("se.omnitor.tipcon1.AppController.STUN_ERROR",
                    "Kan inte n� STUN-servern, avaktiverar STUN.\n \n" +
                    "Antingen finns det en UDP-blockerande brandv�gg som\n" +
                    "hindrar SIPcon1 att n� Internet, eller s� �r STUN-\n" +
                    "serverinst�llningen felaktig.\n \n" +
                    "Var god kontrollera STUN-serverinst�llningarna och f�rs�k igen.");

        setProperty("se.omnitor.tipcon1.AppController.STUN_SERVER_NOT_FOUND",
                    "Kan inte hitta STUN server, avaktiverar STUN.\n \n" +
                    "Var god skriv in the STUN server in SIP-inst�llningarna och f�rs�k igen.");

        setProperty("se.omnitor.tipcon1.AppController.UDP_BLOCKING_FW_FOUND",
                    "Det finns en UDP-blockerande brandv�gg i n�tverket som inte till�ter\n" +
                    "SIPcon1 att n� Internet! Du kommer kanske inte att kunna ringa eller\n" +
                    " ta emot samtal.\n \n" +
                    "Var god aktivera SIP-st�d i brandv�ggen, eller till�t utg�ende UDP-trafik.");

        setProperty("se.omnitor.tipcon1.AppController.SYMMETRIC_FW_FOUND",
                    "En symmetrisk NAT eller brandv�gg har detekterats,\n" +
                    "du kommer kanske inte att kunna ringa eller ta emot samtal.\n \n" +
                    "Byt brandv�gg eller anv�nd annan internet-anslutning.");

        setProperty("se.omnitor.tipcon1.AppController.SDP_PARSE_ERROR",
                    "Felaktig data fr�n motpart, kan ej koppla upp samtalet!");

        setProperty("se.omnitor.tipcon1.AppController.DETECT", "Detekterar n�tverksinst�llningar");

        setProperty("se.omnitor.tipcon1.AppController.NOT_ACCEPTABLE_HERE",
					"Kan inte hitta gemensamma codecs med din samtalspartner,\nsamtalet kan inte kopplas upp.");

        // Added by Luan, 24 Maj 2007
        setProperty("se.omnitor.protocol.t140.SEND_TEXT", "S�nd text");

        setProperty("se.omnitor.protocol.t140.RECEIVE_TEXT", "Mottagen text");


        setProperty("se.omnitor.tipcon1.gui.DialogFactory.ERROR",
		    "Fel");

	setProperty("se.omnitor.tipcon1.gui.DialogFactory.INCOMING_CALL",
		    "Inkommande samtal");

	setProperty("se.omnitor.tipcon1.gui.DialogFactory.ANSWER",
		    "Svara");

	setProperty("se.omnitor.tipcon1.gui.DialogFactory.DECLINE",
		    "Avvisa");

	setProperty("se.omnitor.tipcon1.gui.DialogFactory.CALLING",
		    "Ringer");

	setProperty("se.omnitor.tipcon1.gui.DialogFactory.ADDRESS",
		    "Adress");

	setProperty("se.omnitor.tipcon1.gui.DialogFactory.ABORT",
		    "Avbryt");

        setProperty("se.omnitor.tipcon1.gui.t140.LOGWINDOW",
                    "Logg f�nster");

        setProperty("se.omnitor.tipcon1.gui.t140.RECEIVEWINDOW",
                    "Mottagen text");

        setProperty("se.omnitor.tipcon1.gui.t140.SENDWINDOW",
                    "S�nd text");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.SELECT_ROW_DEL",
		    "V�lj f�rst en rad som skall tas bort.");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.SELECT_ROW_EDIT",
		    "V�lj f�rst en rad som skall �ndras.");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.DELETE",
		    "Ta bort");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.EDIT",
		    "�ndra");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.DEL_CONFIRM",
		    "�r du s�ker p� att du vill ta bort");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.NAME_HEADER",
		    "Namn");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.ADDRESS_HEADER",
		    "SIP-adress");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.NEW_ADDR",
		    "L�gg till adress");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.EDIT_ADDR",
		    "�ndra adress");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.OK_BUTTON",
		    "OK");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.CANCEL_BUTTON",
		    "Avbryt");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.NEW_BUTTON",
		    "L�gg till");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.EDIT_BUTTON",
		    "�ndra");

	setProperty("se.omnitor.tipcon1.gui.AddressPanel.DEL_BUTTON",
		    "Ta bort");

        // Added by Luan, 25 Maj 2007
        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_AUDIO",
                    "Ljud");

        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_VIDEO",
                    "Video");

        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_TEXT",
                    "Text");

        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_ON",
                    "p�");

        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_TRYING",
                    "F�rs�ker ..");


        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_OUT_ONLY",
                    "Ut endast");

        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_IN_ONLY",
                    "In endast");

        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.MEDIA_STATUS_FAILED",
                    "Misslyckades");

        setProperty("se.omnitor.tipcon1.gui.ProgramWindow.NET_INACTIVE",
		    "N�tverk inaktivt");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.NET_ACTIVE",
		    "N�tverk aktivt");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.AUDIO_INACTIVE",
		    "Ljud inaktivt");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.AUDIO_ACTIVE",
		    "Ljud aktivt");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.VIDEO_INACTIVE",
		    "Video inaktivt");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.TEXT_INACTIVE",
		    "Text inaktivt");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.USER_INACTIVE",
		    "Anv�ndare inaktiv");

	setProperty("se.omnitor.tipcon1.gui.ProgramWindow.USER_ACTIVE",
		    "Anv�ndare aktiv");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.DIGITS_ONLY",
		    "Portnumret f�r bara inneh�lla siffror.");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.RESTORE_DEFAULT_OPTION",
    			"Alla dina nuvarande inst�llningar byts mot\nstandardinst�llningar. �r du s�ker?");
	
	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.RESTORE_DEFAULT",
    			"Standardinst�llningar");
	
	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.PORT_IN_RANGE",
		    "Portnumret m�ste vara inom intervallet 1-65535.");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.PORT_CHANGE",
		    "Byter port, v�nta ..");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.INVALID_ADDR",
		    "Du har skrivit in en ogiltig SIP-adress!\n\n" +
		    "Syntax:\n" +
		    "anv�ndare@dom�n.com  eller\n" +
		    "anv�ndare@dom�n.com:port");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.APPLYING",
		    "Applicerar inst�llningar ..");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.NETWORK",
		    "N�tverk");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.SIP_PORT",
		    "SIP-port:");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.DIAL_DOMAIN",
		    "Samtalsdom�n:");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.OUTBOUND_PROXY",
		    "Utg�ende proxy:");
        
    setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.NETWORK_INTERFACE",
	    		"N�tverkskort:");
        
	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.NET_INTERFACE_AUTO",
	    		"Automatiskt");
	
	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.USER",
		    "Anv�ndare");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.FULL_NAME",
		    "Fullst�ndigt namn:");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.ERROR_FULL_NAME",
    "Fullst�ndigt namn f�r endast inneh�lla bokst�verna A-Z och 0-9.");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.UNKNOWN_USER",
		    "Okand anvandare");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.SIP_ADDRESS",
		    "Prim�r SIP-adress:");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.REG_ADDRESS",
		    "Registrar-adress:");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.USERNAME",
		    "Anv�ndarnamn:");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.PASSWORD",
		    "L�senord:");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.OK_BUTTON",
		    "OK");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.CANCEL_BUTTON",
		    "Avbryt");

	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.DEFAULT_BUTTON",
	" Standardinst�llningar");
	
	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.TITLE",
		    "SIP-inst�llningar");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.STUN",
                    "STUN");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.DISABLED_STUN",
                    "St�ng av STUN-funktionalitet");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.AUTO_STUN",
                    "Automatisk STUN (rekommenderat)");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.FORCED_STUN",
                    "P�tvinga anv�ndning av STUN");

        setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.STUN_SERVER_ADDRESS",
                    "Adress till STUN-server:");

    	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.STUN_STATUS",
                    "STUN-status");

        setProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.DETECTING",
                    "Identifierar, det kan ta en stund. Var god v�nta ...");

        setProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.AUDIO_SETUP_CHANGED",
            "Ljud inst�llningar har �ndrats!");

	setProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.VIDEO_SETUP_CHANGED",
            "Video inst�llningar har �ndrats!");

	setProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.AUDIO_AND_VIDEO_SETUP_CHANGED",
            "Ljud och video inst�llningar har �ndrats!");

	setProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.NO_NEW_DEVICES",
                    "Inga nya enheter har identifierats.");

        setProperty("se.omnitor.tipcon1.gui.DetectDevicesThread.DETECTION_COMPLETE",
                   "Identifiering slutf�rd");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.SETTINGS",
		    "-inst�llningar");

        setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.BITRATE",
                   "Bittakt:");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog." +
		    "NO_DEVICES",
		    "Det finns inga tillg�ngliga enheter");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog." +
		    "NO_FORMATS",
		    "Det finns inga tillg�ngliga format");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog." +
		    "NO_CODECS",
		    "Det finns inga tillg�ngliga codecs");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog." +
		    "DIGITS_ONLY",
		    "Portnumret f�r endast best� av siffror.");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog." +
		    "PORT_RANGE",
		    "Portnumret m�ste vara inom intervallet 1-65535.");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog." +
		    "CODECS_TO_USE",
		    "Codecs");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.OK",
		    "OK");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.CANCEL",
		    "Avbryt");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.RTP_PORT",
		    "RTP-port:");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.DEVICE",
		    "enhet:");

	setProperty("se.omnitor.tipcon1.gui.AudioVideoSettingsDialog.FORMAT",
		    "format:");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.TEXT_SETTINGS",
		    "Inst�llningar f�r texttransport");

	setProperty("se.omnitor.tipcon1.gui.AlertSettingsDialog.ALERT_SETTINGS",
		    "Inst�llningar f�r varseblivning");
	setProperty("se.omnitor.tipcon1.gui.AlertSettingsDialog.ALERT_ENABLE",
		    "Aktivera varseblivning");

        setProperty("se.omnitor.tipcon1.gui.LanguageDialog.LANGUAGE_SETTINGS",
                    "Spr�k inst�llning");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.PORT_DIGITS",
		    "Antalet tecken per sekund m�ste best� av siffror.");

        setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.REAL_TIME",
            "Realtid");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.SEND_ON_RETURN",
            "Skicka vid return");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.ENABLE_REALTIME_PREVIEW",
                    "Aktivera Realtidsvy");

    setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.ENABLE_REALTIME_PREVIEW_TIMER",
					"Aktivera timer");

    setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.NOT_NEGATIVE",
		    "Antalet tecken per sekund f�r inte vara negativt.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.CPS_DIGITS",
		    "Portnumret m�ste best� av siffror.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.PORT_RANGE",
		    "Portnumret m�ste vara inom intervallet 1-65535.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.BT_DIGITS",
		    "Buffringstiden m�ste best� av siffror.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.BT_NEGATIVE",
		    "Buffringstiden f�r inte vara negativ.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.RG_DIGITS",
		    "Antalet redundanta generationer m�ste best� av siffror.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.RG_NEGATIVE",
		    "Antalet redundanta generationer f�r inte vara negativt.");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.RTP_PORT",
		    "RTP-port:");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.BUFFER_TIME",
		    "Buffringstid (ms):");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.MAX_IN_CPS",
		    "Max inkommande CPS:");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.RED_GENS",
		    "Red. generationer:");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.OK",
		    "OK");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.CANCEL",
		    "Avbryt");

	setProperty("se.omnitor.tipcon1.gui.TextSettingsDialog.DEFAULT",
	"Standardinst�llningar");
	
	setProperty("se.omnitor.tipcon1.gui.SipSettingsDialog.REGS",
		    "Registreringar");

	setProperty("se.omnitor.tipcon1.gui.SipRegistrationPanel.SIP_ADDRESS",
		    "SIP-adress");

	setProperty("se.omnitor.tipcon1.gui.SipRegistrationPanel.REGISTRAR",
		    "Registrator");

	setProperty("se.omnitor.tipcon1.gui.SipRegistrationPanel.USER_NAME",
		    "Anv�ndarnamn");

	setProperty("se.omnitor.tipcon1.gui.SipRegistrationPanel.PASSWORD",
		    "L�senord");

	setProperty("se.omnitor.tipcon1.gui.SipRegistrationPanel.NEW_REG",
		    "Ny registrering");

	setProperty("se.omnitor.tipcon1.gui.SipRegistrationPanel.EDIT_REG",
		    "�ndra registrering");

	setProperty("se.omnitor.tipcon1.gui.FundingCredits.HEADER",
		    "Funding for the development of this reference design Voice+Text IP Phone\nwas provided by the following sponsors");

	setProperty("se.omnitor.tipcon1.gui.FundingCredits.SPONSOR1",
		    "National Institute on Disability and Rehabilitation Research,\nUS Department of Education under grant # H133E040013");

	setProperty("se.omnitor.tipcon1.gui.Register.TITLE",
		    "Registration info");

	setProperty("se.omnitor.tipcon1.gui.Register.INFO",
                    "F�r en fullt fungerande SIPcon1 beh�ver du en SIP-adress.\n"+
                    "SIP-adressen ser ut som en e-postadress och ger dig m�jlighet\n"+
                    "att n� dina v�nner oavsett var de befinner sig.\n\n"+
                    "Om texten \"Inte registrerad hos en SIP-server\"\n"+
                    "visas i det nedre h�gra h�rnet, �r SIPcon1 inte konfigurerad\n"+
                    "f�r att anv�nda en SIP-adress.\n\n"+
                    "M�nga f�retag tillhandah�ller SIP-adresser, men de flesta\n"+
                    "tar ut en avgift f�r denna tj�nst.\n"+
                    "Du kan testa en SIP-adress genom att registrera dig p�\n"+
                    "dittnamn@trysip.ingate.com, med anv�ndarnamnet dittnamn\n"+
                    "och utan l�senord.\n\n"+
                    "L�s mer p� http://www.ingate.com/trysip.php\n\n"+
                    "F�r mer information om hur du registrerar dig p� en SIP-server,\n"+
                    "v�nligen se anv�ndarmanualen f�r SIPcon1.\n\n"+
                    "F�r att beh�ndigt ringa och ta emot samtal �r det n�dv�ndingt\n"+
                    "att du registrerar din SIP-adress hos en SIP-server.\n"+
                    "Det �r m�jligt att anv�nda SIPcon1 utan en SIP-adress.\n"+
                    "- Du kan ringa till dina v�nner som har en SIP-adress och\n"+
                    "  de kan ringa till dig om de k�nner till din IP-adress.\n\n"+
                    "* Att ringa och ta ta emot samtal kommer inte att vara m�jligt\n"+
                    "  om SIPcon1 �r bakom en brandv�gg eller annan adress-\n"+
                    "  �vers�ttning som �r symmetrisk.");

        setProperty("se.omnitor.tipcon1.gui.Register.INFO_TIPCON1",
                    "F�r en fullt fungerande Tipcon1 beh�ver du en SIP-adress.\n"+
                    "SIP-adressen ser ut som en e-postadress och ger dig m�jlighet\n"+
                    "att n� dina v�nner oavsett var de befinner sig.\n\n"+
                    "Om texten \"Inte registrerad hos en SIP-server\"\n"+
                    "visas i det nedre h�gra h�rnet, �r Tipcon1 inte konfigurerad\n"+
                    "f�r att anv�nda en SIP-adress.\n\n"+
                    "M�nga f�retag tillhandah�ller SIP-adresser, men de flesta\n"+
                    "tar ut en avgift f�r denna tj�nst.\n"+
                    "Du kan testa en SIP-adress genom att registrera dig p�\n"+
                    "dittnamn@trysip.ingate.com, med anv�ndarnamnet dittnamn\n"+
                    "och utan l�senord.\n\n"+
                    "L�s mer p� http://www.ingate.com/trysip.php\n\n"+
                    "F�r mer information om hur du registrerar dig p� en SIP-server,\n"+
                    "v�nligen se anv�ndarmanualen f�r Tipcon1.\n\n"+
                    "F�r att beh�ndigt ringa och ta emot samtal �r det n�dv�ndingt\n"+
                    "att du registrerar din SIP-adress hos en SIP-server.\n"+
                    "Det �r m�jligt att anv�nda Tipcon1 utan en SIP-adress.\n"+
                    "- Du kan ringa till dina v�nner som har en SIP-adress och\n"+
                    "  de kan ringa till dig om de k�nner till din IP-adress.\n\n"+
                    "* Att ringa och ta ta emot samtal kommer inte att vara m�jligt\n"+
                    "  om Tipcon1 �r bakom en brandv�gg eller annan adress-\n"+
                    "  �vers�ttning som �r symmetrisk.");



	setProperty("se.omnitor.tipcon1.gui.AboutDialog.FUNDING",
		    "This work was partially funded by the NIDRR, US Dept of Education\n"+
		    "under Grant H133E040013 as part of the Telecommunication Access RERC of\n"+
		    "the Univ. of Wisconsin-Madison's Trace Center joint with Gallaudet University.");

        setProperty("se.omnitor.tipcon1.ProgramWindow.CALL", "Ring upp");

	setProperty("se.omnitor.tipcon1.ProgramWindow.HANGUP", "L�gg p�");

        setProperty("se.omnitor.tipcon1.ProgramWindow.EMPTY", "T�m textf�nster");

	setProperty("se.omnitor.tipcon1.ProgramWindow.SILENT", "Tyst");

        setProperty("se.omnitor.tipcon1.ProgramWindow.MUTE", "D�mpa");

        setProperty("se.omnitor.tipcon1.ProgramWindow.CONVERSATION_NONE",
                    " Konversation med: Ingen");

        setProperty("se.omnitor.tipcon1.ProgramWindow.DISCONNECTED", "Ej ansluten");

    	setProperty("se.omnitor.tipcon1.ProgramWindow.REGISTERED",
                    "Registrerad");

        setProperty("se.omnitor.tipcon1.ProgramWindow.FAILED",
                    "Misslyckat");

	setProperty("se.omnitor.tipcon1.ProgramWindow.NOT_REGISTRERED_WITH_SIPSERVER",
                "Inte registrerad hos en SIP-server");

        setProperty("se.omnitor.tipcon1.ProgramWindow.CONVERSATION_WITH",
                " Konversation med: ");

        setProperty("se.omnitor.tipcon1.ProgramWindow.NO_LOCAL_VIDEO", "Ingen lokal video");

        setProperty("se.omnitor.tipcon1.ProgramWindow.NO_REMOTE_VIDEO", "Ingen fj�rran video");

	setProperty("se.omnitor.tipcon1.ProgramWindow.CALL_CONTROL", "Samtalskontroll");

        setProperty("se.omnitor.tipcon1.ProgramWindow.ADDRESS", "Adress");

        setProperty("se.omnitor.tipcon1.ProgramWindow.AUDIO_CONTROL", "Ljudkontroll");

	setProperty("se.omnitor.tipcon1.ProgramWindow.SETTINGS", "Inst�llningar");
    }

}
