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

import java.util.Collection;
import java.util.Vector;

import javax.media.Format;

/**
 * Holds information about capture devices
 *
 * @author Erik Zetterstrm, Omnitor AB
 */
public class DeviceContainer {
    
    private String locator          = null;
    private Vector outputFormats    = null;
    private Format[] captureFormats = null;
    private String type             = null;
    private String name;

    
    /**
     * Contructor
     *
     * @param locator A string locator.
     */ 

    public DeviceContainer(String locator) {
        this.locator=locator;
    }


    /**
     * Contructor 
     * 
     * @param locator        A string locator.
     * @param outputFormats  The output formats of this device.
     * @param captureFormats The capture formats of this device.
     * @param type           The type of device.
     */

    public DeviceContainer(String locator,
                           Vector outputFormats,
                           Format[] captureFormats,
                           String type) {
        this.locator=locator;
        this.outputFormats=outputFormats;
        this.captureFormats=captureFormats;
        this.type=type;
    }
    

    /**
     * Contructor 
     * 
     * @param locator        A string locator.
     * @param outputFormats  The output formats of this device.
     * @param captureFormats The capture formats of this device.
     * @param type           The type of device.
     * @param name           The name of the device
     */

    public DeviceContainer(String locator,
                           Vector outputFormats,
                           Format[] captureFormats,
                           String type,
			   String name) {
        this.locator=locator;
        this.outputFormats=outputFormats;
        this.captureFormats=captureFormats;
        this.type=type;
	this.name = name;
    }
    

    /**
     * Returns the locator string for this device.
     *
     * @return The locator string of this device.
     */

    public String getLocator() {
        return locator;
    }


    /**
     * Sets the output formats of this device.
     * 
     * @param outputFormats A vector containing the output formats.
     */

    public void setOutputFormats(Vector outputFormats) {
        this.outputFormats=outputFormats;
    }


    /**
     * Returns a vector containing the output formats of this device.
     *
     * @return The output formats of this device.
     */

    public Collection getOutputFormats() {
        return outputFormats;
    }


    /**
     * Sets the capture formats of this device.
     * 
     * @param captureFormats An array containing the capture formats.
     */
    
    public void setCaptureFormats(Format[] captureFormats) {
        this.captureFormats=captureFormats;
    }


    /**
     * Returns the capture formats of this device.
     * 
     * @return The capture formats of this device.
     */
    
    public Format[] getCaptureFormats() {
        return captureFormats;
    }


    /**
     * Sets the type of device.
     * 
     * @param type The type of device.
     */

    public void setType(String type) {
        this.type=type;
    }


    /**
     * Gets the type of device.
     *
     * @return The type.
     */
    
    public String getType() {
        return type;
    }

    /**
     * Set the name of the device.
     *
     * @param name The name of the device.
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * Get the name of the device.
     *
     * @return The name of the device
     */
    public String getName() {
	return name;
    }
	
}
