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


package se.omnitor.tipcon1.rtp;

import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Control;
import javax.media.Effect;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;

public class GainEffect implements Effect {

    /** The effect name **/
    private static String effectName = "GainEffect";

    /** chosen input Format **/
    protected AudioFormat inputFormat;

    /** chosen output Format **/
    protected AudioFormat outputFormat;

    /** supported input Formats **/
    protected Format[] supportedInputFormats=new Format[0];

    /** supported output Formats **/
    protected Format[] supportedOutputFormats=new Format[0];

    /** selected Gain **/
    protected float gain = 2.0F;

    private Logger logger;
    
    /**
     * initialize the formats
     */
    public GainEffect() {
    	logger = Logger.getLogger("se.omnitor.tipcon1.rtp");
        supportedInputFormats = new Format[] {
	    new AudioFormat(
	        AudioFormat.LINEAR,
                Format.NOT_SPECIFIED,
                16,
                Format.NOT_SPECIFIED,
                AudioFormat.BIG_ENDIAN,
                AudioFormat.SIGNED,
                16,
                Format.NOT_SPECIFIED,
                Format.byteArray
	    )
	};

        supportedOutputFormats = new Format[] {
	    new AudioFormat(
	        AudioFormat.LINEAR,
                Format.NOT_SPECIFIED,
                16,
                Format.NOT_SPECIFIED,
                AudioFormat.BIG_ENDIAN,
                AudioFormat.SIGNED,
                16,
                Format.NOT_SPECIFIED,
                Format.byteArray
	    )
	};

	System.err.println("Set gain by: " + gain);
    }


    /**
     * get the resources needed by this effect
     */
    public void open() throws ResourceUnavailableException {
    }


    /**
     * free the resources allocated by this codec
     */
    public void close() {
    }


    /**
     * reset the codec
     */
    public void reset() {
    }


    /**
     * no controls for this simple effect
     */
    public Object[] getControls() {
        return (Object[]) new Control[0];
    }


    /**
     * Return the control based on a control type for the effect.
     *
     *@param controlType The type of control.
     */
    public Object getControl(String controlType) {
        try {
            Class cls = Class.forName(controlType);
            Object cs[] = getControls();
            for (int i = 0; i < cs.length; i++) {
                if (cls.isInstance(cs[i]))
                return cs[i];
            }
            return null;
        } catch (ClassNotFoundException e) { // no such controlType or such control
        	logger.throwing(this.getClass().getName(), "getControl", e);
            return null;
        }
    }

    /************** format methods *************/

    /**
     * Set the input format
     *
     * @param input The input format.
     */
    public Format setInputFormat(Format input) {
        // the following code assumes valid Format
        inputFormat = (AudioFormat)input;
        return (Format)inputFormat;
    }


    /**
     * Set the output format
     *
     * @param output The output format
     */
    public Format setOutputFormat(Format output) {
        // the following code assumes valid Format
        outputFormat = (AudioFormat)output;
        return (Format)outputFormat;
    }


    /**
     * Get the input format
     *
     * @return Returns the input format.
     */
    protected Format getInputFormat() {
        return inputFormat;
    }


    /**
     * Get the output format
     *
     * @return Returns the output format.
     */
    protected Format getOutputFormat() {
        return outputFormat;
    }


    /**
     * Supported input formats
     *
     * @return Returns the supported input formats.
     */
    public Format [] getSupportedInputFormats() {
        return supportedInputFormats;
    }


    /**
     * Output Formats for the selected input format
     *
     * @param in The requested input format.
     *
     * @return Returns the supported output formats.
     */
    public Format [] getSupportedOutputFormats(Format in) {
        if (! (in instanceof AudioFormat) )
            return new Format[0];

        AudioFormat iaf=(AudioFormat) in;

        if (!iaf.matches(supportedInputFormats[0]))
            return new Format[0];

	AudioFormat oaf= new AudioFormat(
	        AudioFormat.LINEAR,
                iaf.getSampleRate(),
                16,
                iaf.getChannels(),
                AudioFormat.BIG_ENDIAN,
                AudioFormat.SIGNED,
                16,
                Format.NOT_SPECIFIED,
                Format.byteArray
        );

        return new Format[] {oaf};
    }


    /**
     * Gain accessor method
     *
     * @param newGain Sets the gain.
     */
    public void setGain(float newGain){
        gain=newGain;
    }


    /**
     * return effect name
     */
    public String getName() {
        return effectName;
    }


    /**
     * Do the processing
     *
     * @param inputBuffer The incoming buffer.
     * @param outputBuffer The processed buffer.
     *
     * @return A status code..
     */
    public int process(Buffer inputBuffer, Buffer outputBuffer){

        // == prolog
        byte[] inData = (byte[])inputBuffer.getData();
        int inLength = inputBuffer.getLength();
        int inOffset = inputBuffer.getOffset();

        byte[] outData = validateByteArraySize(outputBuffer, inLength);
        int outOffset = outputBuffer.getOffset();
	int j = outOffset;
        int outLength = inLength;

	int samplesNumber = inLength / 2 ;

        // == main

	int tempH, tempL;
	short sample;

        for (int i=0; i< samplesNumber;i++) {

            tempH = inData[inOffset ++] & 0xff;
            tempL = inData[inOffset ++] & 0xff;

	    sample = (short)((tempH << 8) | tempL);
	    sample = (short)(sample * gain);

            outData[j ++]=(byte)(sample >> 8);
            outData[j ++]=(byte)(sample & 0xff);
        }

        // == epilog
        updateOutput(outputBuffer,outputFormat, outLength, outOffset);
        return BUFFER_PROCESSED_OK;
    }


    /**
     * Utility: validate that the Buffer object's data size is at least
     * newSize bytes.
     *
     * @param buffer The Buffer to validate.
     * @param newSize The requested size.
     *
     * @return array with sufficient capacity
     **/
    protected byte[] validateByteArraySize(Buffer buffer,int newSize) {
        Object objectArray=buffer.getData();
        byte[] typedArray;
        if (objectArray instanceof byte[]) { // is correct type AND not null
            typedArray=(byte[])objectArray;
            if (typedArray.length >= newSize ) { // is sufficient capacity
                return typedArray;
            }
        }
        typedArray = new byte[newSize];
        buffer.setData(typedArray);
        return typedArray;
    }


    /**
     * Utility: update the output buffer fields
     *
     * @param outputBuffer The buffer to work on.
     * @param format The format to set for the buffer.
     * @param length The length to set for this buffer.
     * @param offset The offset to set for this buffer.
     */
    protected void updateOutput(Buffer outputBuffer,
                                Format format,int length, int offset) {

        outputBuffer.setFormat(format);
        outputBuffer.setLength(length);
        outputBuffer.setOffset(offset);
    }
}
