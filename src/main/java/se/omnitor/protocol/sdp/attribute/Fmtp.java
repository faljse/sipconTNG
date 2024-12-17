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
package se.omnitor.protocol.sdp.attribute;

import se.omnitor.protocol.sdp.SdpAttribute;

public class Fmtp implements SdpAttribute {

    private int format;
    private String parameters;

    public Fmtp(String value) {
    	int indexOfSpace = value.indexOf(" ");
    	int indexOfSemicolon = value.indexOf(";");
    	
    	if (indexOfSpace == -1 && indexOfSemicolon == -1) {
    		format = Integer.parseInt(value);
    	}
    	else if (indexOfSpace != -1 && indexOfSemicolon == -1) {
    		format = Integer.parseInt(value.substring(0, indexOfSpace));
    		parameters = value.substring(indexOfSpace).trim();
    	}
    	else if (indexOfSpace == -1 && indexOfSemicolon != -1) {
    		format = Integer.parseInt(value.substring(0, indexOfSemicolon));
    		parameters = value.substring(indexOfSemicolon).trim();
    	}
    	else {
    		if (indexOfSpace < indexOfSemicolon) {
        		format = Integer.parseInt(value.substring(0, indexOfSpace));
        		parameters = value.substring(indexOfSpace).trim();
    		}
        	else {
        		format = Integer.parseInt(value.substring(0, indexOfSemicolon));
        		parameters = value.substring(indexOfSemicolon).trim();
        	}
    	}
    	
    	System.out.println("Value: " + value + ", format: " + format);
    }

    public int getType() {
	return FMTP;
    }

    public int getFormat() {
	return format;
    }

    public String getParameters() {
	return parameters;
    }

}
