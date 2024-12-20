/*
 * T.140 Presentation Library
 * 
 * Copyright (C) 2004-2008 Board of Regents of the University of Wisconsin System
 * (Univ. of Wisconsin-Madison, Trace R&D Center)
 * Copyright (C) 2004-2008 Omnitor AB
 *
 * This software was developed with support from the National Institute on
 * Disability and Rehabilitation Research, US Dept of Education under Grant
 * # H133E990006 and H133E040014  
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Please send a copy of any improved versions of the library to: 
 * Gunnar Hellstrom, Omnitor AB, Renathvagen 2, SE 121 37 Johanneshov, SWEDEN
 * Gregg Vanderheiden, Trace Center, U of Wisconsin, Madison, Wi 53706
 *
 */
package se.omnitor.protocol.t140;

/**
 * This class contains global constants that need to be available to all
 * classes in the package.
 *
 * @author Erik Zetterstrom, Omnitor AB
 * @author Andreas Piirimets, Omnitor AB
 */
public abstract class T140Constants {

    /**
     * Special character to indicate lost data, in character form
     */
    public static final char LOSS_CHAR_CHAR = 0xFFFD;

    /**
     * Zero width no break space, transmitted at the beginning of a T.140 
     * session to ensure that the byte order is correct.
     */
    public static final char ZERO_WIDTH_NO_BREAK_SPACE_CHAR  = 0xFEFF; 

    /**
     * Backspace
     */
    public static final char BACKSPACE       = 0x8;

    /**
     * Line seperator
     */
    public static final char LINE_SEPERATOR  = 0x2028;

    /**
     * escape - used in combination with other controls (like intterupt)
     */
    public static final char ESC             = 0x1B;

    /**
     * Line feed
     */  
    public static final char LINE_FEED       = 0xA;

    /**
     * Carraige return
     */
    public static final char CARRIAGE_RETURN = 0xD;

    /**
     * CRLF
     */
    public static final char CR_LF           = 0x0d0a;

    /**
     * Second char in intterupt message.
     */
    public static final char INTERRUPT2      = 0x61;

    /**
     * Bell
     */
    public static final char BELL            = 0x7;

    /**
     * Start of string (used as a general protcol element introducer).
     */
    public static final char SOS             = 0x98;

    /**
     * String terminator (end of sos string).
     */
    public static final char ST              = 0x9C;

    /**
     * Start of string used for (graphic rendition).
     */
    public static final char GRAPHIC_START   = 0x9B;

    /**
     * String terminator (used for grapich rendition).
     */
    public static final char GRAPHIC_END     = 0x6D;

}













