/**
* ===========================================
* Java Pdf Extraction Decoding Access Library
* ===========================================
*
* Project Info:  http://www.jpedal.org
* (C) Copyright 1997-2008, IDRsolutions and Contributors.
* Main Developer: Simon Barnett
*
* 	This file is part of JPedal
*
* Copyright (c) 2008, IDRsolutions
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*     * Neither the name of the IDRsolutions nor the
*       names of its contributors may be used to endorse or promote products
*       derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY IDRsolutions ``AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL IDRsolutions BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* Other JBIG2 image decoding implementations include
* jbig2dec (http://jbig2dec.sourceforge.net/)
* xpdf (http://www.foolabs.com/xpdf/)
* 
* The final draft JBIG2 specification can be found at http://www.jpeg.org/public/fcd14492.pdf
* 
* All three of the above resources were used in the writing of this software, with methodologies,
* processes and inspiration taken from all three.
*
* ---------------
* JDeliHelper.java
* ---------------
*/
package org.jpedal;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.jpedal.exception.PdfException;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author markee
 */
public class JDeliHelper {
    
    
    public static void processJPEG(int dim, byte[] data, int p, int[] maskArray, int[] output) {
    }
    
    /**
     * convert byte[] datastream JPEG to an image in RGB
     * @throws PdfException
     */
    public static BufferedImage JPEG2000ToRGBImage(byte[] data) throws PdfException {

        return null;
    }
    
    public static int[] convertCMYKtoRGB(int cc, int mm, int yy, int kk) {
       return null;
    }    
     
    public static byte[] convertCMYK2RGB(final int w, final int h, int pixelCount, final byte[] data) {
        
        return null;
    }
    
    public static byte[] getBytesFromJPEG(boolean isInverted, final byte[] data, boolean isMask) throws Exception {
        
        return null;
    }
    
    public static byte[] getBytesFromJPEG(final byte[] data) throws Exception {
        
        Raster ras= getRasterFromJPEG2000(data);
        
        return ((DataBufferByte)ras.getDataBuffer()).getData();
        
    }
    
     
    private static Raster getRasterFromJPEG2000(final byte[] data) {
        
        final ByteArrayInputStream in;
        
        ImageReader iir=null;
        final ImageInputStream iin;
        
        Raster ras=null;
        
        try {
            
            //read the image data
            in = new ByteArrayInputStream(data);
            
            //suggestion from Carol
            final Iterator iterator = ImageIO.getImageReadersByFormatName("JPEG2000");
            
            while (iterator.hasNext()){
                final Object o = iterator.next();
                iir = (ImageReader) o;
                if (iir.canReadRaster()) {
                    break;
                }
            }
            
            ImageIO.setUseCache(false);
            iin = ImageIO.createImageInputStream((in));
            iir.setInput(iin, true);
            ras=iir.read(0).getRaster();
            
            in.close();
            iir.dispose();
            iin.close();
            
        }catch(final Exception ee){
            LogWriter.writeLog("Problem closing  " + ee);
        }
        
        return ras;
    }
    
    public static BufferedImage getTiffImage(final int tiffImageToLoad, final String file) {
        return null;
    }
    
    public static int getTiffPageCount(final String file) {
        
        return 0;
    }
    
    public static void write(BufferedImage image, String type, String file_name, boolean fasterPNG) throws IOException {
        ImageIO.write(image,type,new File(file_name));
    }
}
