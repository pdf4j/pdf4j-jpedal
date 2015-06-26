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
* SMaskDecoder.java
* ---------------
*/

package org.jpedal.parser.image;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.color.JPEGDecoder;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.image.data.ImageData;

/**
 *
 * @author markee
 */
class SMaskDecoder {
    
    static BufferedImage applyJPX_JBIG_Smask(final ImageData imageData, final ImageData smaskData, byte[] maskData, PdfObject maskObject){
        byte[] objectData = imageData.getObjectData();
        
        int iw=imageData.getWidth();
        int ih=imageData.getHeight();
        
        int sw=smaskData.getWidth();
        int sh=smaskData.getHeight();
        int sd=smaskData.getDepth();
        
        float [] decodeArr = maskObject.getFloatArray(PdfDictionary.Decode);
        
        
        
        if(decodeArr!=null && decodeArr[0]==1 && decodeArr[1]==0){ // data inverted refer to dec2011/example.pdf
            for (int i = 0; i < maskData.length; i++) {
                maskData[i]^= 0xff;
            }
        }
        
        maskData = ColorSpaceConvertor.normaliseTo8Bit(sd,sw, sh, maskData);
        
        if(iw!=sw || ih!=sh){
            BufferedImage scaleImage = new BufferedImage(sw, sh,BufferedImage.TYPE_BYTE_GRAY);
            byte [] temp = ((DataBufferByte) scaleImage.getRaster().getDataBuffer()).getData();
            System.arraycopy(maskData, 0, temp, 0, maskData.length);            
            scaleImage = getScaledImage(scaleImage, iw, ih);
            maskData = ((DataBufferByte) scaleImage.getRaster().getDataBuffer()).getData();
        }
        int p = 0;
        
        BufferedImage img = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
        int [] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        
        if ((iw * ih) == objectData.length) {
            for (int i = 0; i < maskData.length; i++) {
                int a = maskData[i] & 0xff;
                int r = objectData[p++] & 0xff;
                int g = r;
                int b = r;
                pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        } else {
            for (int i = 0; i < maskData.length; i++) {
                int a = maskData[i] & 0xff;
                int r = objectData[p++] & 0xff;
                int g = objectData[p++] & 0xff;
                int b = objectData[p++] & 0xff;
                pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
        
        
        return img;
    }
    
    static byte[] applySMask(byte[] maskData, final ImageData imageData,final GenericColorSpace decodeColorData, final PdfObject newSMask, final PdfObject XObject) {
        
        byte[] objectData=imageData.getObjectData();
        
        /*
        * Image data
        */
        int w=imageData.getWidth();
        int h=imageData.getHeight();
        int d=imageData.getDepth();
        
        /*
        * Smask data (ASSUME single component at moment)
        */
        final int maskW=newSMask.getInt(PdfDictionary.Width);
        final int maskH=newSMask.getInt(PdfDictionary.Height);
        final int maskD=newSMask.getInt(PdfDictionary.BitsPerComponent);
       
        objectData = MaskDataDecoder.convertData(decodeColorData, objectData, w, h, imageData, d, maskD, maskData);
        
        //needs to be 'normalised to 8  bit'
        if(maskD!=8){
            maskData=ColorSpaceConvertor.normaliseTo8Bit(maskD, maskW, maskH, maskData);
        }
        
        //add mask as a element so we now have argb
        if(w==maskW && h==maskH){
            //System.out.println("Same size");
            objectData=buildUnscaledByteArray(w, h, objectData, maskData);
        }else if(w<maskW){ //mask bigger than image
            //System.out.println("Mask bigger");
            objectData=upScaleImageToMask(w, h, maskW,maskH,objectData, maskData);
            
            XObject.setIntNumber(PdfDictionary.Width,maskW);
            XObject.setIntNumber(PdfDictionary.Height,maskH);
            
        }else{
            //System.out.println("Image bigger");
            objectData=upScaleMaskToImage(w, h, maskW,maskH,objectData, maskData);
        }
        
        XObject.setIntNumber(PdfDictionary.BitsPerComponent, 8);
       
//        BufferedImage img= ColorSpaceConvertor.createARGBImage( XObject.getInt(PdfDictionary.Width), XObject.getInt(PdfDictionary.Height), objectData);
//        
//        try{
//        ImageIO.write(img, "PNG", new java.io.File("/Users/markee/Desktop/img.png"));
//        }catch(Exception e){}
//        
        
        
        return objectData;
    }

    static void check4BitData(final byte[] objectData) {
        final int size=objectData.length;
        
        boolean is4Bit=true;
        
        for(byte b:objectData){
            if(b<0 || b>15){
                is4Bit=false;
                break;
            }
        }
        
        if(is4Bit){
            for (int ii=0;ii<size;ii++){
                objectData[ii]=(byte) (objectData[ii]<<4);
            }
        }
    }
    
    
    private static byte[] upScaleMaskToImage(final int w, final int h, final int maskW, final int maskH, final byte[] objectData, final byte[] maskData) {
        
        int rgbPtr=0, aPtr=0;
        int i=0;
        float ratioW=(float)maskW/(float)w;
        float ratioH=(float)maskH/(float)h;
        byte[] combinedData=new byte[w*h*4];
        
        final int rawDataSize=objectData.length;
        
        try{
            for(int iY=0;iY<h;iY++){
                for(int iX=0;iX<w;iX++){
                    
                    //rgb
                    for(int comp=0;comp<3;comp++){
                        if(rgbPtr<rawDataSize){
                            combinedData[i+comp]=objectData[rgbPtr];
                        }
                        rgbPtr++;
                    }
                    
                    aPtr=(((int)(iX*ratioW)))+(((int)(iY*ratioH))*w);
                    
                    combinedData[i+3]=maskData[aPtr];
                    
                    i += 4;
                    
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return combinedData;
    }
    
    
    private static byte[] upScaleImageToMask(final int w, final int h, final int maskW, final int maskH, final byte[] objectData, final byte[] maskData) {
        
        int rgbPtr=0, aPtr=0;
        int i=0;
        float ratioW=(float)w/(float)maskW;
        float ratioH=(float)h/(float)maskH;
        byte[] combinedData=new byte[maskW*maskH*4];
        final int rawDataSize=objectData.length;
        final int maskSize=maskData.length;
        
        try{
            for(int mY=0;mY<maskH;mY++){
                for(int mX=0;mX<maskW;mX++){
                    
                    rgbPtr=(((int)(mX*ratioW))*3)+(((int)(mY*ratioH))*w*3);
                    
                    // System.err.println(mX+"/"+maskW+" "+mY+"/"+maskH+" "+ratioW+" mask="+((int)(mX*ratioW))+" "+((int)(mY*ratioH)));
                    //rgb
                    for(int comp=0;comp<3;comp++){
                        if(rgbPtr<rawDataSize){
                            combinedData[i+comp]=objectData[rgbPtr];
                        }
                        rgbPtr++;
                    }
                    
                    if(aPtr<maskSize){
                        combinedData[i+3]=maskData[aPtr];
                        aPtr++;
                    }
                    
                    i += 4;
                    
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return combinedData;
    }
    
     static byte[] getSMaskData(byte[] maskData,ImageData smaskData, PdfObject newSMask,GenericColorSpace maskColorData) {
        smaskData.getFilter(newSMask);
       
        if(smaskData.isDCT()){
            maskData=JPEGDecoder.getBytesFromJPEG(maskData,maskColorData,newSMask);
            newSMask.setMixedArray(PdfDictionary.Filter,null);
            newSMask.setDecodedStream(maskData);
        }else if(smaskData.isJPX()){
            maskData=JPeg2000ImageDecoder.getBytesFromJPEG2000(maskData,maskColorData,newSMask);
            newSMask.setMixedArray(PdfDictionary.Filter,null);
            newSMask.setDecodedStream(maskData);
        }
        return maskData;
    }
     
    private static byte[] buildUnscaledByteArray(final int w, final int h, final byte[] objectData, final byte[] maskData) {
        
        int pixels=w*h*4;
        int rgbPtr=0, aPtr=0;
        byte[] combinedData=new byte[w*h*4];
        final int rawDataSize=objectData.length;
        final int maskSize=maskData.length;
        
        try{
            for(int i=0;i<pixels;i += 4){
                
                //rgb
                for(int comp=0;comp<3;comp++){
                    if(rgbPtr<rawDataSize){
                        combinedData[i+comp]=objectData[rgbPtr];
                    }
                    rgbPtr++;
                }
                
                if(aPtr<maskSize){
                    //System.out.println(maskData[aPtr]);
                    combinedData[i+3]=maskData[aPtr];
                    aPtr++;
                }
                
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return combinedData;
    }
    
    private static BufferedImage getScaledImage(BufferedImage image, int width, int height) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        double scaleX = (double) width / imageWidth;
        double scaleY = (double) height / imageHeight;
        AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
        AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);
        return bilinearScaleOp.filter(image, new BufferedImage(width, height, image.getType()));
    }
}
