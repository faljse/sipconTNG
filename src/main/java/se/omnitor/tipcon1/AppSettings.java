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

import org.w3c.dom.Document;
import se.omnitor.tipcon1.sip.SipRegistrarInfo;
import se.omnitor.protocol.t140.T140FontDialogListener;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import java.util.Locale;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Vector;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileWriter;
import java.awt.Color;
import java.io.File;
import java.util.Random;

/**
 * This class handles all settings that are stored in the settings file for SIPcon1.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class AppSettings implements T140FontDialogListener {

    private static final String CLASS_NAME = "AppSettings";

    public static final int STUN_DISABLED = 1;
    public static final int STUN_AUTO = 2;
    public static final int STUN_FORCED = 3;

    private Logger logger;

    private String filename;

    private int bitrate = 64000;
    private String resolution = "";
    private int redundantRtpGenerations;
    private long bufferTime;
    private int maxIncomingCps;
    int stunMode = STUN_AUTO;   
    private String stunServerAddress;
    private String primarySipAddress;
    private boolean sendOnCR = false;
    private int redundantT140Generations = 0;
    private boolean redT140FlagOutgoing = false;
    private String userRealName;
    private boolean isMsrpSmootherActive;
    private SipRegistrarInfo[] sipRegistrarInfo;
    private String dialDomain;
    private String outboundProxy;
    private String localFont = "Arial Unicode MS";
    private String remoteFont = "Arial Unicode MS";
    private int localTextColor[] = {0, 0, 0};
    private int remoteTextColor[] = {0, 0, 0};
    private int localBgColor[] = {255, 255, 255};
    private int remoteBgColor[] = {255, 255, 255};
    private int localTextSize = 12;
    private int remoteTextSize = 12;
    private boolean alerting = false;
    // T140 variables
    private boolean realtimepreview = true;
    private boolean isRtpvTimerEnabled = false;

    private int languageCode = 0;

    private String configurationTypeString = "Tipcon1";
    //private boolean tipcon1Mode = false;
    public static final int NET_IFACE_AUTO = 1;
    public static final int NET_IFACE_MANUAL = 2;
    int netIfaceMode = NET_IFACE_AUTO;
    private String netIfaceMacAddr = null;
    public AppSettings(String filename) {
        logger = Logger.getLogger("se.omnitor.tipcon1");

        this.filename = filename;
        load();
    }

    /**
     * Restores saved settings from file.
     *
     */
    public void load() {

        // write methodname
        final String METHOD = "load()";
        // log when entering a method
        logger.entering(CLASS_NAME, METHOD);

        // Default settings
        redundantRtpGenerations = 2;
        bufferTime = 300;
        stunMode = STUN_AUTO;
        stunServerAddress = "";
        maxIncomingCps = 0;

        try {

            // default is to use TextSmoother
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            DocumentBuilder parser = factory.newDocumentBuilder();
            Document doc =
                    parser.parse(new File(filename));

            int pLength;
            //data = new Object[pLength][2];
            Node tempNode;
            NodeList nl;
            NodeList nl2;
            Node tempNode2;
            Node tempNode3;
            String name;
            String address;
            int nlLength;
            int nlcnt;
            NodeList nodes;
            int ver = 1;

            Element e = doc.getDocumentElement();

            if (e != null) {
                try {
                    ver = Integer.parseInt(e.getAttribute("v"));
                } catch (NumberFormatException nfe) {
                }
            }

            //EZ 20070723: Get the mode from config-file SIPcon1=text and audio only. Tipcon1=total conversation.
            nodes = doc.getElementsByTagName("Configuration");
            pLength = nodes.getLength();

            for (int pcnt = 0; pcnt < pLength; pcnt++) {
                name = null;
                address = null;

                tempNode = nodes.item(pcnt);
                if (tempNode != null) {

                    nl = tempNode.getChildNodes();

                    nlLength = nl.getLength();
                    for (nlcnt = 0; nlcnt < nlLength; nlcnt++) {

                        tempNode2 = nl.item(nlcnt);

                        /*
                        if (tempNode2.getNodeName().equals
                            ("ConfigurationType")) {

                            try {
                                configurationTypeString =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim();
                            } catch (NullPointerException npe) {
                                configurationTypeString = "Tipcon1";
                            }

                            if(configurationTypeString.toUpperCase().equals("SIPCON1")) {
                                tipcon1Mode=false;
                            }
                            else if(configurationTypeString.toUpperCase().equals("TIPCON1"))
                            {
                                tipcon1Mode=true;
                            }
                            else {
                                configurationTypeString = "SIPcon1";
                                tipcon1Mode=false;
                            }

                        }
                        */

                    }
                }
            }


            nodes = doc.getElementsByTagName("User");
            pLength = nodes.getLength();
            for (int pcnt = 0; pcnt < pLength; pcnt++) {
                name = null;
                address = null;

                tempNode = nodes.item(pcnt);
                if (tempNode != null) {

                    nl = tempNode.getChildNodes();

                    nlLength = nl.getLength();
                    for (nlcnt = 0; nlcnt < nlLength; nlcnt++) {

                        tempNode2 = nl.item(nlcnt);

                        if (tempNode2.getNodeName().equals("FullName")) {
                            try {
                                name =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim();
                            } catch (NullPointerException npe) {
                                name = "";
                            }
                        }
                        if (tempNode2.getNodeName().
                            equals("SipAddress")) {

                            try {
                                address =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim();
                            } catch (NullPointerException npe) {
                                address = "";
                            }
                        }
                        if (tempNode2.getNodeName().
			    equals("Language")) {

			    try {
			        languageCode = Integer.parseInt(
                                        tempNode2.getFirstChild().getNodeValue().trim());
			    } catch (NullPointerException npe) {
			        languageCode = 0;
			    }
			}


                    }

                    logger.logp(Level.FINER, CLASS_NAME, METHOD, "name is",
                                name);
                    if (name != null || address != null) {
                        userRealName = name;
                        primarySipAddress = address;
                        //dataCnt++;
                    }
                }
            }

            // Get RTP text settings
            nodes = doc.getElementsByTagName("RtpText");
            pLength = nodes.getLength();
            for (int pcnt = 0; pcnt < pLength; pcnt++) {
                name = null;
                address = null;

                tempNode = nodes.item(pcnt);
                if (tempNode != null) {

                    nl = tempNode.getChildNodes();

                    nlLength = nl.getLength();
                    for (nlcnt = 0; nlcnt < nlLength; nlcnt++) {

                        tempNode2 = nl.item(nlcnt);

                        if (tempNode2.getNodeName().equals
                            ("RedundantGenerations")) {
                            try {
                                redundantRtpGenerations =
                                        Integer.parseInt
                                        (tempNode2.getFirstChild().
                                         getNodeValue().trim());

                            } catch (NullPointerException npe) {

                            } catch (NumberFormatException nfe) {
                            }
                        }
                        if (tempNode2.getNodeName().
                            equals("BufferTime")) {
                            try {
                                bufferTime = Integer.parseInt
                                             (tempNode2.getFirstChild().
                                              getNodeValue().trim());
                            } catch (NullPointerException npe) {
                            } catch (NumberFormatException nfe) {
                            }
                        }
                        
                        if (tempNode2.getNodeName().
                        	equals("MaxIncomingCps")) {
                            try {
                            	maxIncomingCps = Integer.parseInt
                                             (tempNode2.getFirstChild().
                                                  getNodeValue().trim());
                            } catch (NullPointerException npe) {
                            } catch (NumberFormatException nfe) {
                            }
                        }
                        
                        if (tempNode2.getNodeName().
                            equals("DontUseTextSmoother")) {
                            // If the XML-tag DontUseTextSmoother is set. For testing probably.
                            isMsrpSmootherActive = false;
                        }
                        // if xml-tag return exist, send on return, otherwise use realtimetexttransport
                        if (tempNode2.getNodeName().
                            equals("SendOnCR")) {
                            this.sendOnCR = true;
                        }
                    }

                }
            }
            
            //EZ 041115: Extract T140 redundant values from settings file.
            nodes = doc.getElementsByTagName("T140");
            String redundantT140GenerationsStr = null;
            pLength = nodes.getLength();

            for (int pcnt = 0; pcnt < pLength; pcnt++) {
                name = null;
                address = null;

                tempNode = nodes.item(pcnt);
                if (tempNode != null) {

                    nl = tempNode.getChildNodes();

                    nlLength = nl.getLength();
                    for (nlcnt = 0; nlcnt < nlLength; nlcnt++) {

                        tempNode2 = nl.item(nlcnt);

                        if (tempNode2.getNodeName().equals
                            ("RedundantGenerations")) {

                            try {
                                redundantT140GenerationsStr =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim();
                            } catch (NullPointerException npe) {
                                redundantT140GenerationsStr = "0";
                            }
                        }

                    }

                    if (redundantT140GenerationsStr != null) {
                        try {
                            redundantT140Generations =
                                    Integer.parseInt(
                                            redundantT140GenerationsStr);
                        } catch (NumberFormatException nfe) {
                        }

                        if (redundantT140Generations > 0) {
                            redT140FlagOutgoing = true;
                        }
                    }

                }
            }

            // Registrar settings

            nodes = doc.getElementsByTagName("SipRegistrar");
            pLength = nodes.getLength();

            Vector<SipRegistrarInfo> registrars = new Vector<SipRegistrarInfo>(0, 1);

            for (int pcnt = 0; pcnt < pLength; pcnt++) {
                String registrarHost = "";
                String username = "";
                String password = "";
                String sipAddress = primarySipAddress;

                name = null;
                address = null;

                tempNode = nodes.item(pcnt);
                if (tempNode != null) {

                    nl = tempNode.getChildNodes();

                    nlLength = nl.getLength();
                    for (nlcnt = 0; nlcnt < nlLength; nlcnt++) {

                        tempNode2 = nl.item(nlcnt);

                        if (tempNode2.getNodeName().equals("SipAddress")) {
                            try {
                                sipAddress =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim();
                            } catch (NullPointerException npe) {
                                // Do nothing
                            }
                        }

                        if (tempNode2.getNodeName().equals("Address")) {
                            try {
                                registrarHost =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim();
                            } catch (NullPointerException npe) {
                                registrarHost = "";
                            }
                        }

                        if (tempNode2.getNodeName().equals("Username")) {
                            try {
                                username =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim();
                            } catch (NullPointerException npe) {
                                username = "";
                            }
                        }

                        if (tempNode2.getNodeName().equals("Password")) {
                            try {
                                if (ver == 1) {
                                    password =
                                            decodePasswordV1(tempNode2.
                                            getFirstChild().
                                            getNodeValue().
                                            trim());
                                } else if (ver == 2) {
                                    password =
                                            decodePasswordV2(tempNode2.
                                            getFirstChild().
                                            getNodeValue().
                                            trim());
                                }

                            } catch (NullPointerException npe) {
                                password = "";
                            }
                        }

                    }

                    /*
                           registrarAddress = registrarHost;
                           registrarUsername = username;
                           registrarPassword = password;
                     */

                    registrars.add(new SipRegistrarInfo(sipAddress,
                            registrarHost,
                            username,
                            password));

                }
            }

            int rlen = registrars.size();
            sipRegistrarInfo = new SipRegistrarInfo[rlen];
            for (int rcnt = 0; rcnt < rlen; rcnt++) {
                sipRegistrarInfo[rcnt] =
                        (SipRegistrarInfo) registrars.elementAt(rcnt);
            }

            // Network settings
       	
            nodes = doc.getElementsByTagName("Network");
            pLength = nodes.getLength();

            for (int pcnt = 0; pcnt < pLength; pcnt++) {
                name = null;
                address = null;

                tempNode = nodes.item(pcnt);
                if (tempNode != null) {

                    nl = tempNode.getChildNodes();

                    nlLength = nl.getLength();
                    for (nlcnt = 0; nlcnt < nlLength; nlcnt++) {

                        tempNode2 = nl.item(nlcnt);
                        if (tempNode2.getNodeName().equals("OutboundProxy")) {
                            try {
                                outboundProxy =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim();
                            } catch (NullPointerException npe) {
                                outboundProxy = null;
                            }
                        }

                        else if (tempNode2.getNodeName().equals("DialDomain")) {
                            try {
                                dialDomain =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim();
                            } catch (NullPointerException npe) {
                                dialDomain = "";
                           //     npe.printStackTrace();
                            }
                        }

                        else if (tempNode2.getNodeName().
                                 equals("Stun")) {

                            nl2 = tempNode2.getChildNodes();

                            int nlLength2 = nl2.getLength();
                            for (int nlcnt2 = 0; nlcnt2 < nlLength2; nlcnt2++) {

                                tempNode3 = nl2.item(nlcnt2);

                                if (tempNode3.getNodeName().equals("Mode")) {
                                    try {
                                        String isActive =
                                                tempNode3.getFirstChild().
                                                getNodeValue().trim();
                                        if (isActive.toUpperCase(Locale.US).
                                            equals("DISABLED")) {
                                            stunMode = STUN_DISABLED;
                                        }
                                        else if (isActive.toUpperCase().
                                            equals("AUTO")) {
                                            stunMode = STUN_AUTO;
                                        }
                                        else if (isActive.toUpperCase().equals("FORCED")) {
                                            stunMode = STUN_FORCED;
                                        }
                                    } catch (NullPointerException npe) {
                                        stunMode = STUN_AUTO;
                                    }
                                }
                                if (tempNode3.getNodeName().equals("Server")) {
                                    try {
                                        stunServerAddress =
                                                tempNode3.getFirstChild().
                                                getNodeValue().trim();
                                    } catch (NullPointerException npe) {
                                        stunServerAddress = "";
                                    }
                                }
                            }
                        }
                        else if (tempNode2.getNodeName().
                                equals("NetworkInterface")) {

                           nl2 = tempNode2.getChildNodes();

                           int nlLength2 = nl2.getLength();
                           for (int nlcnt2 = 0; nlcnt2 < nlLength2; nlcnt2++) {

                               tempNode3 = nl2.item(nlcnt2);

                               if (tempNode3.getNodeName().equals("Mode")) {
                                   try {
                                       String isActive =tempNode3.getFirstChild().getNodeValue().trim();
                                       if (isActive.toUpperCase(Locale.US).equals("AUTOMATIC")) {
                                    	   netIfaceMode = NET_IFACE_AUTO;
                                       }
                                       else if (isActive.toUpperCase().equals("MANUAL")) {
                                    	   netIfaceMode = NET_IFACE_MANUAL;
                                       }
                                   } catch (NullPointerException npe) {
                                	   netIfaceMode = NET_IFACE_AUTO;
                                   }
                               }
                           }
                    	}else if (tempNode2.getNodeName().
                                			equals("NetIfMac")) {
                        	try{
                        		netIfaceMacAddr = tempNode2.getFirstChild().getNodeValue().trim();
                        	}catch(NullPointerException npe){
                        		netIfaceMacAddr = "";
                        	}
                        }
                    }
                }
            }

            // Text GUI settings

            nodes = doc.getElementsByTagName("TextGui");
            pLength = nodes.getLength();

            for (int pcnt = 0; pcnt < pLength; pcnt++) {
                name = null;
                address = null;

                tempNode = nodes.item(pcnt);
                if (tempNode != null) {

                    nl = tempNode.getChildNodes();

                    nlLength = nl.getLength();
                    for (nlcnt = 0; nlcnt < nlLength; nlcnt++) {

                        tempNode2 = nl.item(nlcnt);

                        if (tempNode2.getNodeName().equals("LocalFont")) {
                            try {
                                localFont =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim();
                            } catch (NullPointerException npe) {
                                // Do nothing
                            }
                        }

                        else if (tempNode2.getNodeName().
                                 equals("RemoteFont")) {

                            try {
                                remoteFont =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim();
                            } catch (NullPointerException npe) {
                                // Do nothing
                            }
                        }

                        else if (tempNode2.getNodeName().
                                 equals("LocalTextSize")) {

                            try {
                                localTextSize =
                                        Integer.parseInt(tempNode2.
                                        getFirstChild().
                                        getNodeValue().trim());
                            } catch (NullPointerException npe) {
                                // Do nothing
                            } catch (NumberFormatException nfe) {
                            }
                        }

                        else if (tempNode2.getNodeName().
                                 equals("RemoteTextSize")) {

                            try {
                                remoteTextSize =
                                        Integer.parseInt(tempNode2.
                                        getFirstChild().
                                        getNodeValue().trim());
                            } catch (NullPointerException npe) {
                                // Do nothing
                            } catch (NumberFormatException nfe) {
                            }
                        }

                        else if (tempNode2.getNodeName().
                                 equals("LocalTextColor")) {

                            try {
                                String[] localTextColorStr =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim().split(",");
                                localTextColor[0] = Integer.parseInt(
                                        localTextColorStr[0]);
                                localTextColor[1] = Integer.parseInt(
                                        localTextColorStr[1]);
                                localTextColor[2] = Integer.parseInt(
                                        localTextColorStr[2]);
                            } catch (NullPointerException npe) {
                                // Do nothing
                            }
                        }

                        else if (tempNode2.getNodeName().
                                 equals("RemoteTextColor")) {

                            try {
                                String[] remoteTextColorStr =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim().split(",");
                                remoteTextColor[0] = Integer.parseInt(
                                        remoteTextColorStr[0]);
                                remoteTextColor[1] = Integer.parseInt(
                                        remoteTextColorStr[1]);
                                remoteTextColor[2] = Integer.parseInt(
                                        remoteTextColorStr[2]);
                            } catch (NullPointerException npe) {
                                // Do nothing
                            }
                        }

                        else if (tempNode2.getNodeName().
                                 equals("LocalBgColor")) {

                            try {
                                String[] localBgColorStr =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim().split(",");
                                localBgColor[0] = Integer.parseInt(
                                        localBgColorStr[0]);
                                localBgColor[1] = Integer.parseInt(
                                        localBgColorStr[1]);
                                localBgColor[2] = Integer.parseInt(
                                        localBgColorStr[2]);
                            } catch (NullPointerException npe) {
                                // Do nothing
                            }
                        }

                        else if (tempNode2.getNodeName().
                                 equals("RemoteBgColor")) {

                            try {
                                String[] remoteBgColorStr =
                                        tempNode2.getFirstChild().
                                        getNodeValue().trim().split(",");
                                remoteBgColor[0] = Integer.parseInt(
                                        remoteBgColorStr[0]);
                                remoteBgColor[1] = Integer.parseInt(
                                        remoteBgColorStr[1]);
                                remoteBgColor[2] = Integer.parseInt(
                                        remoteBgColorStr[2]);
                            } catch (NullPointerException npe) {
                                // Do nothing
                            }
                        }

                        else if (tempNode2.getNodeName().
                                 equals("Alerting")) {
                            try {
                                if (Integer.parseInt(tempNode2.getFirstChild().
                                        getNodeValue().trim()) == 0) {
                                    alerting = false;
                                } else {
                                    alerting = true;
                                    System.out.println("Alerting enabled.");
                                }
                            } catch (NullPointerException npe) {
                                // Do nothing
                            }
                        } else if (tempNode2.getNodeName().
                                   equals("RealtimePreview")) {
                            try {
                                if (Integer.parseInt(tempNode2.getFirstChild().
                                        getNodeValue().trim()) == 0) {
                                    realtimepreview = false;
                                } else {
                                    realtimepreview = true;
                                }
                            } catch (NullPointerException npe) {
                                // Do nothing
                            }
                        } else if (tempNode2.getNodeName().
                                   equals("RealtimePreviewTimerEnabled")) {
                            try {
                                if (Integer.parseInt(tempNode2.getFirstChild().
                                        getNodeValue().trim()) == 0) {
                                    isRtpvTimerEnabled = false;
                                } else {
                                    isRtpvTimerEnabled = true;
                                }
                            } catch (NullPointerException npe) {
                                // Do nothing
                            }
                        } else if (tempNode2.getNodeName().
                                    equals("VideoBitrate")) {
                             try {
                                 bitrate = Integer.parseInt
                                           (tempNode2.getFirstChild().
                                            getNodeValue().trim());
                             } catch (NullPointerException npe) {
                             } catch (NumberFormatException nfe) {
                             }

                         } else if (tempNode2.getNodeName().
                                    equals("VideoResolution")) {
                             try {
                                 resolution = tempNode2.getFirstChild().
                                                getNodeValue().trim();
                             } catch (NullPointerException npe) {
                             }
                         }


                     }
                }
            }

        } catch (java.io.IOException e) {
            // Couldn't read from file. That's OK.
            logger.fine("Could not open settings XML file " + filename + ".");
        }
        catch (ParserConfigurationException e) {
            // Couldn't read from file. That's OK.
            logger.fine("Settings file XML parser problem, filename = " + filename + ".");
		} catch (SAXException e) {
            // Couldn't read from file. That's OK.
            logger.fine("Settings file XML parser problem, filename = " + filename + ".");
		}

        logger.exiting(CLASS_NAME, METHOD);

    }

    /**
     * Saves data to the settings XML file.
     *
     */
    public void save() {
        File file;
        FileWriter fw;

        try {
            file =
                    new File(filename);
            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            fw = new FileWriter(file);
        } catch (Exception e) {
            logger.throwing(this.getClass().getName(), "saveData", e);
            return;
        }

        String docType = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\r";
        String settingsStart = "<Settings v=\"2\">\n\r";
        String settingsEnd = "</Settings>\n\r";
        int settingsLength = settingsStart.length();

        String configurationStart = "  <Configuration>\n\r";
        String configurationEnd   = "  </Configuration>\n\r";
        int configurationStartLength = configurationStart.length();
        int configurationEndLength   = configurationEnd.length();
        String configurationTypeStart = "        <ConfigurationType>";
        String configurationTypeEnd   = "</ConfigurationType>\n\r";
        int configurationTypeStartLength = configurationTypeStart.length();
        int configurationTypeEndLength   = configurationTypeEnd.length();
        String configurationType="SIPcon1";
        if(AppConstants.PROGRAM_TYPE == AppConstants.TIPCON1) {
            configurationType="TIPcon1";
        }

        String userStart = "  <User>\n\r";
        String userEnd = "  </User>\n\r";
        String languageStart = "        <Language>";
        int languageStartLength = languageStart.length();
        String languageEnd = "</Language>\n\r";
        int languageEndLength = languageEnd.length();
    	int userLength = userStart.length();
        String fullNameStart = "    <FullName>";
        int fullNameStartLength = fullNameStart.length();
        String fullNameEnd = "</FullName>\n\r";
        int fullNameEndLength = fullNameEnd.length();
        String sipAddressStart = "        <SipAddress>";
        int sipAddressStartLength = sipAddressStart.length();
        String sipAddressEnd = "</SipAddress>\n\r";
        int sipAddressEndLength = sipAddressEnd.length();
        String sipRegistrarStart = "      <SipRegistrar>\n\r";
        String sipRegistrarEnd = "      </SipRegistrar>\n\r";
        String addressStart = "        <Address>";
        String addressEnd = "</Address>\n\r";
        String usernameStart = "        <Username>";
        String usernameEnd = "</Username>\n\r";
        String passwordStart = "        <Password>";
        String passwordEnd = "</Password>\n\r";

        String t140Start = "  <T140>\n\r";
        int t140StartLength = t140Start.length();
        String t140End = "  </T140>\n\r";
        int t140EndLength = t140End.length();
        String redundantGenerationsStart = "      <RedundantGenerations>";
        int redundantGenerationsStartLength = redundantGenerationsStart.length();
        String redundantGenerationsEnd = "</RedundantGenerations>\n\r";
        int redundantGenerationsEndLength = redundantGenerationsEnd.length();
        String redundantT140GenerationsStr = "" + redundantT140Generations;

        String rtpTextStart = "  <RtpText>\n\r";
        String rtpTextEnd = "  </RtpText>\n\r";
        String bufferTimeStart = "    <BufferTime>";
        String bufferTimeEnd = "</BufferTime>\n\r";
        String maxIncomingCpsStart = "    <MaxIncomingCps>";
        String maxIncomingCpsEnd = "</MaxIncomingCps>\n\r";
        String sendOnCRString = "<SendOnCR/>\n\r";

        String networkStart = "  <Network>\n\r";
        String networkEnd = "  </Network>\n\r";
        String outboundProxyStart = "    <OutboundProxy>";
        String outboundProxyEnd = "</OutboundProxy>\n\r";
        String dialDomainStart = "    <DialDomain>";
        String netIfaceStart = "    <NetworkInterface>\n\r";
        String netIfaceEnd = "    </NetworkInterface>\n\r";
        String netIfaceModeStart = "      <Mode>";
        String netIfaceModeEnd = "</Mode>\n\r";
        String netIfMacAddrStart= "<NetIfMac>";
        String netIfMacAddrEnd= "</NetIfMac>\n\r";
        String dialDomainEnd = "</DialDomain>\n\r";
        String stunStart = "    <Stun>\n\r";
        String stunEnd = "    </Stun>\n\r";
        String stunIsActiveStart = "      <Mode>";
        String stunIsActiveEnd = "</Mode>\n\r";
        String stunServerAddressStart = "      <Server>";
        String stunServerAddressEnd = "</Server>\n\r";

        String textGuiStart = "  <TextGui>\n\r";
        String textGuiEnd = "  </TextGui>\n\r";
        String localFontStart = "    <LocalFont>";
        String localFontEnd = "</LocalFont>\n\r";
        /*
          String remoteFontStart = "    <RemoteFont>";
          String remoteFontEnd = "</RemoteFont>\n\r";
         */
        String localBgColorStart = "    <LocalBgColor>";
        String localBgColorEnd = "</LocalBgColor>\n\r";
        /*
          String remoteBgColorStart = "    <RemoteBgColor>";
          String remoteBgColorEnd = "</RemoteBgColor>\n\r";
         */
        String alertingStart = "    <Alerting>";
        String alertingEnd = "</Alerting>\n\r";
        String realtimeStart = "    <RealtimePreview>";
        String realtimeEnd = "</RealtimePreview>\n\r";
        String rtpvTimerEnabledStart = "    <RealtimePreviewTimerEnabled>";
        String rtpvTimerEnabledEnd = "</RealtimePreviewTimerEnabled>\n\r";
        String localTextColorStart = "    <LocalTextColor>";
        String localTextColorEnd = "</LocalTextColor>\n\r";
        /*
          String remoteTextColorStart = "    <RemoteTextColor>";
          String remoteTextColorEnd = "</RemoteTextColor>\n\r";
         */
        String localTextSizeStart = "    <LocalTextSize>";
        String localTextSizeEnd = "</LocalTextSize>\n\r";
        /*
          String remoteTextSizeStart = "    <RemoteTextSize>";
          String remoteTextSizeEnd = "</RemoteTextSize>\n\r";
         */

        String stunServerToSave = stunServerAddress;
        if (stunServerToSave.equals(AppConstants.PUBLIC_STUN_SERVER)) {
            stunServerToSave = "";
        }
        
        try {
            // Start settings
            fw.write(docType, 0, docType.length());
            fw.write(settingsStart, 0, settingsLength);

            String tempStr;

            //Write configuration
            fw.write(configurationStart,0,configurationStartLength);
            fw.write(configurationTypeStart,0,configurationTypeStartLength);
            tempStr =
                    new String(encodeStringToXml
                               (configurationType).getBytes("UTF-8"));

            fw.write(tempStr,0,tempStr.length());
            fw.write(configurationTypeEnd,0,configurationTypeEndLength);
            fw.write(configurationEnd,0,configurationEndLength);

            // Write user
            fw.write(userStart, 0, userLength);

            fw.write(fullNameStart, 0, fullNameStartLength);
            tempStr =
                    new String(encodeStringToXml
                               (userRealName).getBytes("UTF-8"));
            fw.write(tempStr, 0, tempStr.length());
            fw.write(fullNameEnd, 0, fullNameEndLength);

            fw.write(sipAddressStart, 0, sipAddressStartLength);
            tempStr =
                    new String(encodeStringToXml
                               (primarySipAddress).getBytes("UTF-8"));
            fw.write(tempStr, 0, tempStr.length());
            fw.write(sipAddressEnd, 0, sipAddressEndLength);

            fw.write(languageStart, 0, languageStartLength);
            tempStr =
                    new String(encodeStringToXml
                               (String.valueOf(languageCode)).getBytes("UTF-8"));
            fw.write(tempStr, 0, tempStr.length());
            fw.write(languageEnd, 0, languageEndLength);

            fw.write(userEnd, 0, userLength + 1);

            // T.140 details

            fw.write(t140Start, 0, t140StartLength);

            fw.write(redundantGenerationsStart, 0,
                     redundantGenerationsStartLength);
            fw.write(redundantT140GenerationsStr, 0,
                     redundantT140GenerationsStr.length());
            fw.write(redundantGenerationsEnd, 0,
                     redundantGenerationsEndLength);

            fw.write(t140End, 0, t140EndLength);

            // RTP text details
            String rtpRedGenStr = "" + redundantRtpGenerations;
            String rtpBufferTimeStr = "" + bufferTime;
            String rtpMaxIncomingCps = "" + maxIncomingCps;
            fw.write(rtpTextStart, 0, rtpTextStart.length());
            fw.write(redundantGenerationsStart, 0,
                     redundantGenerationsStart.length());
            fw.write(rtpRedGenStr, 0, rtpRedGenStr.length());
            fw.write(redundantGenerationsEnd, 0,
                     redundantGenerationsEnd.length());
            fw.write(bufferTimeStart, 0, bufferTimeStart.length());
            fw.write(rtpBufferTimeStr, 0, rtpBufferTimeStr.length());
            fw.write(bufferTimeEnd, 0, bufferTimeEnd.length());

            fw.write(maxIncomingCpsStart, 0, maxIncomingCpsStart.length());
            fw.write(rtpMaxIncomingCps, 0, rtpMaxIncomingCps.length());
            fw.write(maxIncomingCpsEnd, 0, maxIncomingCpsEnd.length());
            
            if (sendOnCR) {
                fw.write(sendOnCRString);
            }

            fw.write(rtpTextEnd, 0, rtpTextEnd.length());

            // Registrar details
            String addr;
            String host;
            String user;
            String pwd;
            if (sipRegistrarInfo != null) {
                for (int cnt = 0; cnt < sipRegistrarInfo.length; cnt++) {

                    addr = sipRegistrarInfo[cnt].getSipAddress();
                    host = sipRegistrarInfo[cnt].getRegistrarHost();
                    user = sipRegistrarInfo[cnt].getUsername();
                    pwd = encodePasswordV2(sipRegistrarInfo[cnt].
                                           getPassword());

                    fw.write(sipRegistrarStart, 0, sipRegistrarStart.length());

                    fw.write(sipAddressStart, 0, sipAddressStart.length());
                    fw.write(addr, 0, addr.length());
                    fw.write(sipAddressEnd, 0, sipAddressEnd.length());

                    fw.write(addressStart, 0, addressStart.length());
                    fw.write(host, 0, host.length());
                    fw.write(addressEnd, 0, addressEnd.length());

                    fw.write(usernameStart, 0, usernameStart.length());
                    fw.write(user, 0, user.length());
                    fw.write(usernameEnd, 0, usernameEnd.length());

                    fw.write(passwordStart, 0, passwordStart.length());
                    fw.write(pwd, 0, pwd.length());
                    fw.write(passwordEnd, 0, passwordEnd.length());

                    fw.write(sipRegistrarEnd, 0, sipRegistrarEnd.length());

                }
            }

            // Network details
            String ob = outboundProxy;
            if (ob == null) {
                ob = "";
            }
            String dd = dialDomain;
            if (dd == null) {
                dd = "";
            }
            fw.write(networkStart, 0, networkStart.length());
            fw.write(outboundProxyStart, 0, outboundProxyStart.length());
            fw.write(ob, 0, ob.length());
            fw.write(outboundProxyEnd, 0, outboundProxyEnd.length());
            fw.write(dialDomainStart, 0, dialDomainStart.length());
            fw.write(dd, 0, dd.length());
            fw.write(dialDomainEnd, 0, dialDomainEnd.length());

            
            fw.write(stunStart, 0, stunStart.length());
            fw.write(stunIsActiveStart, 0, stunIsActiveStart.length());
            switch (stunMode) {
            case STUN_DISABLED:
                fw.write("Disabled", 0, 8);
                break;
            case STUN_AUTO:
                fw.write("Auto", 0, 4);
                break;
            case STUN_FORCED:
                fw.write("Forced", 0, 6);
                break;
            default:
            }
            fw.write(stunIsActiveEnd, 0, stunIsActiveEnd.length());
            fw.write(stunServerAddressStart, 0, stunServerAddressStart.length());
            fw.write(stunServerToSave, 0, stunServerToSave.length());
            fw.write(stunServerAddressEnd, 0, stunServerAddressEnd.length());
            fw.write(stunEnd, 0, stunEnd.length());
            
            fw.write(netIfaceStart, 0, netIfaceStart.length());
            fw.write(netIfaceModeStart, 0, netIfaceModeStart.length());
            switch(netIfaceMode){
            case NET_IFACE_AUTO:
            	fw.write("automatic", 0, 9);
            	break;
            case NET_IFACE_MANUAL:
            	fw.write("manual", 0, 6);
            	break;
            default:
            }
            
            fw.write(netIfaceModeEnd, 0, netIfaceModeEnd.length());
            fw.write(netIfaceEnd, 0, netIfaceEnd.length());
            
            if(netIfaceMode == NET_IFACE_AUTO){
            	netIfaceMacAddr = "";
            }	
            
            fw.write(netIfMacAddrStart, 0 ,netIfMacAddrStart.length());
            fw.write(netIfaceMacAddr, 0, netIfaceMacAddr.length());
            fw.write(netIfMacAddrEnd, 0, netIfMacAddrEnd.length());
            	
            fw.write(networkEnd, 0, networkEnd.length());

            // Text GUI details
            //CHECK String font = t140Panel.getTaFont().getFontName();
            //String rFont = t140Panel.getRemoteFont().getFontName();
            //CHECK
            /*
                         Color color;
                         color = t140Panel.getTaFontColor();
                         String tCol =
                    color.getRed() + "," +
                    color.getGreen() + "," +
                    color.getBlue();
             */
            String tCol = localTextColor[0] + "," + localTextColor[1] + "," +
                          localTextColor[2];
            /*
                  color = t140Panel.getRemoteFontColor();
                  String rTCol =
               color.getRed() + "," +
               color.getGreen() + "," +
               color.getBlue();
             */
            //CHECK
            /*
                         color = t140Panel.getTaFontBackground();
                         String bCol =
                    color.getRed() + "," +
                    color.getGreen() + "," +
                    color.getBlue();
             */
            String bCol = localBgColor[0] + "," + localBgColor[1] + "," +
                          localBgColor[2];
            /*
                  color = t140Panel.getRemoteFontBackground();
                  String rBCol =
               color.getRed() + "," +
               color.getGreen() + "," +
               color.getBlue();
             */
            //CHECK String fSiz = "" + t140Panel.getTaFontSize();
            String fSiz = "" + localTextSize;
            //String rFSiz = ""+t140Panel.getRemoteFontSize();

            fw.write(textGuiStart, 0, textGuiStart.length());

            fw.write(localFontStart, 0, localFontStart.length());
            fw.write(localFont, 0, localFont.length());
            fw.write(localFontEnd, 0, localFontEnd.length());

            /*
                  fw.write(remoteFontStart, 0, remoteFontStart.length());
                  fw.write(rFont, 0, rFont.length());
                  fw.write(remoteFontEnd, 0, remoteFontEnd.length());
             */

            fw.write(localBgColorStart, 0, localBgColorStart.length());
            fw.write(bCol, 0, bCol.length());
            fw.write(localBgColorEnd, 0, localBgColorEnd.length());

            /*
                  fw.write(remoteBgColorStart, 0, remoteBgColorStart.length());
                  fw.write(rBCol, 0, rBCol.length());
                  fw.write(remoteBgColorEnd, 0, remoteBgColorEnd.length());
             */
            fw.write(alertingStart, 0, alertingStart.length());
            if (alerting) {
                fw.write("1", 0, 1);
            } else {
                fw.write("0", 0, 1);
            }
            fw.write(alertingEnd, 0, alertingEnd.length());

            fw.write("    <VideoBitrate>", 0, "    <VideoBitrate>".length());
            fw.write(Integer.toString(bitrate) , 0, Integer.toString(bitrate).length());
            fw.write("</VideoBitrate>\n\r", 0, "</VideoBitrate>\n\r".length());

            fw.write("    <VideoResolution>", 0, "    <VideoResolution>".length());
            fw.write(resolution , 0, resolution.length());
            fw.write("</VideoResolution>\n\r", 0, "</VideoResolution>\n\r".length());

            fw.write(realtimeStart, 0, realtimeStart.length());
            if (realtimepreview) {
                fw.write("1", 0, 1);
            } else {
                fw.write("0", 0, 1);
            }
            fw.write(realtimeEnd, 0, realtimeEnd.length());

            fw.write(rtpvTimerEnabledStart, 0, rtpvTimerEnabledStart.length());
            if (isRtpvTimerEnabled) {
                fw.write("1", 0, 1);
            } else {
                fw.write("0", 0, 1);
            }
            fw.write(rtpvTimerEnabledEnd, 0, rtpvTimerEnabledEnd.length());

            fw.write(localTextColorStart, 0, localTextColorStart.length());
            fw.write(tCol, 0, tCol.length());
            fw.write(localTextColorEnd, 0, localTextColorEnd.length());

            /*
             fw.write(remoteTextColorStart, 0, remoteTextColorStart.length());
                  fw.write(rTCol, 0, rTCol.length());
                  fw.write(remoteTextColorEnd, 0, remoteTextColorEnd.length());
             */

            fw.write(localTextSizeStart, 0, localTextSizeStart.length());
            fw.write(fSiz, 0, fSiz.length());
            fw.write(localTextSizeEnd, 0, localTextSizeEnd.length());

            /*
             fw.write(remoteTextSizeStart, 0, remoteTextSizeStart.length());
                  fw.write(rFSiz, 0, rFSiz.length());
                  fw.write(remoteTextSizeEnd, 0, remoteTextSizeEnd.length());
             */

            fw.write(textGuiEnd, 0, textGuiEnd.length());

            // End settings
            fw.write(settingsEnd, 0, settingsEnd.length());
            fw.close();
        } catch (java.io.IOException e) {
            // Error writing. Nothing to do, just continue.
            logger.throwing(this.getClass().getName(), "saveData", e);
        }

    }

    public int getRedundantRtpGenerations() {
        return redundantRtpGenerations;
    }

    public void setRedundantRtpGenerations(int gen) {
        this.redundantRtpGenerations = gen;
    }

    public long getBufferTime() {
        return bufferTime;
    }

    public void setBufferTime(long bt) {
        this.bufferTime = bt;
    }
    
    public int getMaxIncomingCps() {
        return maxIncomingCps;
    }

    public void setMaxIncomingCps(int maxCps) {
        this.maxIncomingCps = maxCps;
    }

    public int getStunMode() {
        return stunMode;
    }

    public int getnetIfaceMode() {
        return netIfaceMode;
    }
    public int getLanguageCode() {
    	return languageCode;
    }

    public void setLanguageCode(int aLanguageCode) {
    	languageCode = aLanguageCode;
    }



    public void setStunMode(int mode) {
        this.stunMode = mode;
    }
    
    public void SetNetIfaceMode(int mode){
    	this.netIfaceMode = mode;
    }
    
    public void setNetIfMacAddr(String MacAddr){
    	this.netIfaceMacAddr = MacAddr;
    }
    
    public String getNetIfMacAddr(){
    	return netIfaceMacAddr;
	}

    public String getStunServerAddress() {
        return stunServerAddress;
    }

    public void setStunServerAddress(String address) {
        this.stunServerAddress = address;
    }

    public String getPrimarySipAddress() {
        return primarySipAddress;
    }

    public void setPrimarySipAddress(String address) {
        this.primarySipAddress = address;
    }

    public boolean useSendOnCR() {
        return sendOnCR;
    }

    public void setSendOnCR(boolean sendOnCR) {
        this.sendOnCR = sendOnCR;

    }

    public int getRedundantT140Generations() {
        return redundantT140Generations;
    }

    public void setRedundantT140Generations(int gen) {
        this.redundantT140Generations = gen;
    }

    public boolean getRedT140FlagOutgoing() {
        return redT140FlagOutgoing;
    }

    public void setRedT140FlagOutgoing(boolean flag) {
        this.redT140FlagOutgoing = flag;
    }

    /**
     * Returns the user's real name.
     * Default if value is not set: "Unknown user".
     *
     * @return The user's real name
     */
    public String getUserRealName() {
        if (userRealName == null || userRealName.trim().length() == 0) {
            userRealName = "";
        }
        return userRealName;
    }

    public void setUserRealName(String name) {
        this.userRealName = name;
    }

    public boolean isMsrpSmootherActive() {
        return isMsrpSmootherActive;

    }

    public void useMsrpSmoother(boolean active) {
        this.isMsrpSmootherActive = active;
    }

    /**
     * Decodes the password according to version 2
     *
     * @param pwd The password to decode
     *
     * @return The decoded password
     */
    private String decodePasswordV2(String pwd) {

        String encStr =
                "ZdKafQ7ClkJzOY629RxBcv3UIXPNWiwreGHMEnpV58" +
                "DouT1LysFgSq4hj0Abmt";
        String alphabet =
                "abcdefghijklmnopqrstuvwxyz1234567890ABCDEF" +
                "GHIJKLMNOPQRSTUVWXYZ";

        int chars = 0;

        String result = "";
        int idx;

        for (int cnt = 0; cnt < pwd.length(); cnt++) {

            idx = alphabet.indexOf((int) pwd.charAt(cnt));
            if (idx == -1) {
                result += pwd.charAt(cnt);
            } else {
                result += encStr.charAt(idx);
            }

            chars++;

            for (int i = 0; i < (chars / 7 + 1); i++) {
                cnt++;
            }
        }

        return result;

    }

    /**
     * Encodes the password according to version 1
     *
     * @param password The password to encode
     *
     * @return The encoded password
     */
    /*
    private String encodePasswordV1(String password) {
        return password;
    }
    */

    /**
     * Decodes the password according to version 1
     *
     * @param password The password to decode
     *
     * @return The decoded password
     */
    private String decodePasswordV1(String password) {
        return password;
    }

    /**
     * Encodes the password according to version 2
     *
     * @param pwd The password to encode
     *
     * @return The encoded password
     */
    private String encodePasswordV2(String pwd) {

        Random rand = new Random(System.currentTimeMillis());

        String encStr =
                "ZdKafQ7ClkJzOY629RxBcv3UIXPNWiwreGHMEnpV58" +
                "DouT1LysFgSq4hj0Abmt";
        String alphabet =
                "abcdefghijklmnopqrstuvwxyz1234567890ABCDEF" +
                "GHIJKLMNOPQRSTUVWXYZ";

        String result = "";
        int idx;
        int chars = 0;

        for (int cnt = 0; cnt < pwd.length(); cnt++) {

            idx = encStr.indexOf((int) pwd.charAt(cnt));

            if (idx == -1) {
                result += pwd.charAt(cnt);
            } else {
                result += alphabet.charAt(idx);
            }

            chars++;

            for (int i = 0; i < (chars / 7 + 1); i++) {
                result += alphabet.
                        charAt((int) (rand.nextDouble() * alphabet.length()));
            }
        }

        return result;
    }

    /**
     * Encode a String to be suitable for an XML file
     *
     * @param s The String to encode
     *
     * @return The encoded String, which is useable for saving in an XML file
     */
    private String encodeStringToXml(String s) {

        if (s == null) {
            return "";
        }

        String encodedStr = "";
        int sLen = s.length();

        for (int i = 0; i < sLen; i++) {
            switch (s.charAt(i)) {
            case '&':
                encodedStr += "&amp;";
                break;
            case '\'':
                encodedStr += "&apos;";
                break;
            case '\"':
                encodedStr += "&quot;";
                break;
            case '>':
                encodedStr += "&gt;";
                break;
            case '<':
                encodedStr += "&lt;";
                break;
            default:
                encodedStr += s.charAt(i);
            }

        }

        return encodedStr;
    }

    public boolean isRealtimePreviewEnabled() {
        return realtimepreview;
    }

    public void setRealtimePreview(boolean preview) {
        this.realtimepreview = preview;
    }

    public boolean isRealtimePreviewTimerEnabled() {
        return isRtpvTimerEnabled;
    }

    public void setRealtimePreviewTimerEnabled(boolean isEnabled) {
        this.isRtpvTimerEnabled = isEnabled;
    }

    public String getLocalFont() {
        return localFont;
    }

    public void setLocalFont(String font) {
        this.localFont = font;
    }

    public void setVideoBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getVideoBitrate() {
        return this.bitrate;
    }

    public String getResolution() {
        return this.resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public int getLocalTextSize() {
        return localTextSize;

    }

    public void setLocalTextSize(int size) {
        this.localTextSize = size;
    }

    public Color getLocalTextColor() {
        return new Color(localTextColor[0], localTextColor[1], localTextColor[2]);
    }

    public void setLocalTextColor(Color c) {
        localTextColor[0] = c.getRed();
        localTextColor[1] = c.getGreen();
        localTextColor[2] = c.getBlue();

    }

    public void setLocalTextColor(int red, int green, int blue) {
        localTextColor[0] = red;
        localTextColor[1] = green;
        localTextColor[2] = blue;
    }

    public Color getLocalBgColor() {
        return new Color(localBgColor[0], localBgColor[1], localBgColor[2]);
    }

    public void setLocalBgColor(Color c) {
        localBgColor[0] = c.getRed();
        localBgColor[1] = c.getGreen();
        localBgColor[2] = c.getBlue();

    }

    public void setLocalBgColor(int red, int green, int blue) {
        localBgColor[0] = red;
        localBgColor[1] = green;
        localBgColor[2] = blue;
    }

    public SipRegistrarInfo[] getSipRegistrarInfo() {
        return sipRegistrarInfo;
    }

    public void setSipRegistrarInfo(SipRegistrarInfo[] info) {
        this.sipRegistrarInfo = info;
    }

public String getOutboundProxy() {
    return outboundProxy;
}

public void setOutboundProxy(String proxy) {
    this.outboundProxy = proxy;
}

public boolean isAlertingEnabled() {
    return alerting;
}

public void setAlerting(boolean enableAlerting) {
    this.alerting = enableAlerting;
}

public String getDialDomain() {
    return dialDomain;
}

public void setDialDomain(String dialDomain) {
    this.dialDomain = dialDomain;
}


/**
 * EZ: Returns the configuration type string. Currently only "SIPcon1" or
 * "Tipcon1" is allowed. SIPcon1 is audi+text. Tipcon1 is totalconversation.
 *
 * @return The configuration type string.
 */
public String getConfigurationString() {
    return configurationTypeString;
}

/**
 * EZ: Returns the configuration type.
 * true  = Tipcon1, totalconversation.
 * false = SIPcon1, audio + text only
 *
 * @return The configuration.
 */
/*
public boolean getTipcon1Mode() {
    return tipcon1Mode;
}
*/


	public void saveFontSettings(String font, int localTextSize, Color textColor, Color bgColor) {
		setLocalFont(font);
		setLocalTextSize(localTextSize);
		setLocalTextColor(textColor);
		setLocalBgColor(bgColor);
		save();
	}

}

