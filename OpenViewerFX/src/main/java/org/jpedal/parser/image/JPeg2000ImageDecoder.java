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

import java.awt.image.*;
import org.jpedal.JDeliHelper;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.image.data.ImageData;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author markee
 */
public class JPeg2000ImageDecoder {
    
    
    public static BufferedImage decode(final String name, int w, int h, GenericColorSpace decodeColorData, byte[] data, final float[] decodeArray, final ImageData imageData, int d) throws RuntimeException, PdfException {
        
        
        BufferedImage image;
        
//needs imageio library
        
        LogWriter.writeLog("JPeg 2000 Image " + name + ' ' + w + "W * " + h + 'H');
        
        /**
         * try {
         * java.io.FileOutputStream a =new java.io.FileOutputStream("/Users/markee/Desktop/"+ name + ".jpg");
         *
         * a.write(data);
         * a.flush();
         * a.close();
         *
         * } catch (Exception e) {
         * LogWriter.writeLog("Unable to save jpeg " + name);
         *
         * }  /**/
        
        
        
        image = decodeColorData.JPEG2000ToRGBImage(data,w,h,decodeArray,imageData.getpX(),imageData.getpY(),d);
        
        return image;
    }
    
    public static byte[] getBytesFromJPEG2000(final byte[] data, GenericColorSpace decodeColorData,final PdfObject XObject) {
        
        try {
            return JDeliHelper.getBytesFromJPEG(data);
        } catch (Exception ex) {
            LogWriter.writeLog("Exception with JPeg Image " + ex);
        }
        return null;
        
    }
}
