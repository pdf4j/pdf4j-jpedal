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
 * SMask.java
 * ---------------
 */
package org.jpedal.parser.image.mask;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import org.jpedal.io.ColorSpaceConvertor;

/**
 *
 */
public class SMask {
        
    public static BufferedImage applyLuminosityMask(BufferedImage image, BufferedImage smask){
                
        if(smask==null){
            return image;
        }
        
        if(smask.getType() != BufferedImage.TYPE_INT_ARGB){
            smask = ColorSpaceConvertor.convertToARGB(smask);
        }
        if(image.getType() != BufferedImage.TYPE_INT_ARGB){
            image = ColorSpaceConvertor.convertToARGB(image);
        }
        
        int iw = image.getWidth();
        int ih = image.getHeight();
        int imageDim = iw * ih;
        
        int sw = smask.getWidth();
        int sh = smask.getHeight();
        int smaskDim = sw * sh;
        
        if(imageDim <smaskDim){
            image = scaleImage(image, sw, sh, BufferedImage.TYPE_INT_ARGB);
        }else if(smaskDim < imageDim){
            smask = scaleImage(smask, iw, ih, BufferedImage.TYPE_INT_ARGB);
        }
        
        int [] imagePixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int [] maskPixels = ((DataBufferInt) smask.getRaster().getDataBuffer()).getData();
        
        int ip,mp,r,g,b,a,y;
        for (int i = 0; i < imagePixels.length; i++) {
            mp = maskPixels[i];
            if(mp!=0){
                r = (mp >> 16) & 0xff;
                g = (mp >> 8) & 0xff;
                b = mp & 0xff;
                y = (r * 77) + (g * 152) + (b * 28);
                ip = imagePixels[i];
                a = (ip >> 24) & 0xff;
                a = (a * y) >> 16;
                imagePixels[i] = (a<<24)|(ip & 0xffffff);
            }            
        }
        
        return image;
    }
    
     public static BufferedImage applyAlphaMask(BufferedImage image, BufferedImage smask){
                       
        if(smask==null){
            return image;
        }
        
        if(smask.getType() != BufferedImage.TYPE_INT_ARGB){
            smask = ColorSpaceConvertor.convertToARGB(smask);
        }
        if(image.getType() != BufferedImage.TYPE_INT_ARGB){
            image = ColorSpaceConvertor.convertToARGB(image);
        }
        
        int iw = image.getWidth();
        int ih = image.getHeight();
        int imageDim = iw * ih;
        
        int sw = smask.getWidth();
        int sh = smask.getHeight();
        int smaskDim = sw * sh;
        
        if(imageDim <smaskDim){
            image = scaleImage(image, sw, sh, BufferedImage.TYPE_INT_ARGB);
        }else if(smaskDim < imageDim){
            smask = scaleImage(smask, iw, ih, BufferedImage.TYPE_INT_ARGB);
        }
        
        int [] imagePixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int [] maskPixels = ((DataBufferInt) smask.getRaster().getDataBuffer()).getData();
        
        int ip,mp,ia,ma,a;
        float sc = 1/255f;
        for (int i = 0; i < imagePixels.length; i++) {
            mp = maskPixels[i];
            ip = imagePixels[i];
            ia = (ip >> 24) & 0xff;
            ma = (mp >> 24) & 0xff;
            a = (int) (ia * ma * sc);
            imagePixels[i] = (a<<24)|(ip & 0xffffff);                     
        }
        return image;
    }
    
    private static BufferedImage scaleImage(BufferedImage src, int w, int h, int imageType){
        BufferedImage dimg = new BufferedImage(w, h, imageType);
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dimg;      
    }
    
}
