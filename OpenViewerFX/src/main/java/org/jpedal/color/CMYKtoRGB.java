/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2015 IDRsolutions and Contributors.
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
 * CMYKtoRGB.java
 * ---------------
 */

package org.jpedal.color;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.jpedal.parser.DecoderOptions;

/**
 * handle image conversion and optimise for JDK
 */
public class CMYKtoRGB {
    
    public static BufferedImage convert(final Raster ras, final int w, final int h) {
        
        final BufferedImage image;
        
        if(DecoderOptions.javaVersion==1.8f){
            image=convertCMYKImageToRGB(((DataBufferByte)ras.getDataBuffer()).getData(),w,h);
        }else{ //faster on all except Java8 where it is much slower (I have posted bug report to Oracle)             
            image = filter(w, h, ras);
        }
        
        return image;
    }

    public static BufferedImage convert(final byte[] buffer, final int w, final int h) {
        
        final BufferedImage image;
        
        if(DecoderOptions.javaVersion==1.8f){
            image=convertCMYKImageToRGB(buffer,w,h);
        }else{ //faster on all except Java8 where it is much slower (I have posted bug report to Oracle)             
            
            final Raster ras = Raster.createInterleavedRaster(new DataBufferByte(buffer,buffer.length), w,h,w * 4,4, new int[]{ 0, 1, 2, 3 },null);
        
            image = filter(w, h, ras);
        }
        
        return image;
    }
    
    private static BufferedImage filter(final int w, final int h, final Raster ras) {
        
        final ColorSpace CMYK=DeviceCMYKColorSpace.getColorSpaceInstance();
        final ColorSpace rgbCS=GenericColorSpace.getColorSpaceInstance();
        final ColorModel rgbModel = new ComponentColorModel(rgbCS, new int[] { 8, 8, 8 }, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
        final ColorConvertOp CSToRGB = new ColorConvertOp(CMYK, rgbCS, ColorSpaces.hints);
        
        /**generate the rgb image*/
        final WritableRaster rgbRaster =rgbModel.createCompatibleWritableRaster(w, h);
        CSToRGB.filter(ras, rgbRaster);
        
        final BufferedImage image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        image.setData(rgbRaster);
        
        return image;
    }
    
    
    /**
     * convert YCC to CMY via formula and the CMYK to sRGB via profiles
     */
    static BufferedImage convertCMYKImageToRGB(final byte[] buffer, final int w, final int h) {

        /**
         * set colorspaces and color models using profiles if set
         */

        final ColorSpace CMYK=DeviceCMYKColorSpace.getColorSpaceInstance();

        final int pixelCount = w * h*4;
        int C,M,Y,K,lastC=-1,lastM=-1,lastY=-1,lastK=-1;

        int j=0;
        float[] RGB= {0f,0f,0f};
        //turn YCC in Buffer to CYM using profile
        for (int i = 0; i < pixelCount; i += 4) {

            C=(buffer[i] & 255);
            M = (buffer[i+1] & 255);
            Y = (buffer[i+2] & 255);
            K = (buffer[i+3] & 255);

            //cache last value, black and white
            if(C==lastC && M==lastM && Y==lastY && K==lastK){
                //no change so use last value
            }else if(C==0 && M==0 && Y==0 && K==0){
                RGB=new float[]{1f,1f,1f};
            }else if(C==255 && M==255 && Y==255 && K==255){
                RGB=new float[]{0f,0f,0f};    
            }else{ //new value

                RGB=CMYK.toRGB(new float[]{C/255f,M/255f,Y/255f,K/255f});

                //flag so we can just reuse if next value the same
                lastC=C;
                lastM=M;
                lastY=Y;
                lastK=K;
            }

            //put back as CMY
            buffer[j] = (byte) (RGB[0]*255f );
            buffer[j + 1] = (byte) (RGB[1]*255f);
            buffer[j + 2] = (byte) (RGB[2]*255f );

            j += 3;

        }

        /**
         * create CMYK raster from buffer
         */
        final Raster raster = Raster.createInterleavedRaster(new DataBufferByte(buffer,j), w,h,w * 3,3, new int[]{ 0, 1, 2 },null);

        //data now sRGB so create image
        final BufferedImage image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        image.setData(raster);

        return image;
    }

}
