/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2016 IDRsolutions and Contributors.
 *
 * This file is part of JPedal/JPDF2HTML5
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * JPeg2000ImageDecoder.java
 * ---------------
 */

package org.jpedal.parser.image;

import java.awt.image.BufferedImage;
import org.jpedal.JDeliHelper;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.exception.PdfException;
import org.jpedal.parser.image.data.ImageData;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author markee
 */
public class JPeg2000ImageDecoder {
    
    
    public static BufferedImage decode(final int w, final int h, final GenericColorSpace decodeColorData, final byte[] data, 
            final float[] decodeArray, final ImageData imageData, int d) throws RuntimeException, PdfException {

        LogWriter.writeLog("JPeg 2000 Image " + w + "W * " + h + 'H');

        return decodeColorData.JPEG2000ToRGBImage(data,w,h,decodeArray,imageData.getpX(),imageData.getpY(),d);

    }
    
    public static byte[] getBytesFromJPEG2000(final byte[] data) {
        
        try {
            return JDeliHelper.getBytesFromJPEG(data);
        } catch (Exception ex) {
            LogWriter.writeLog("Exception with JPeg Image " + ex);
        }
        return null;
        
    }
}
