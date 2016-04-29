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
 * ImageDataToJavaImage.java
 * ---------------
 */
package org.jpedal.parser.image;

import org.jpedal.parser.image.mask.MaskDecoder;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import org.jpedal.color.ColorSpaces;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.color.PatternColorSpace;
import org.jpedal.exception.PdfException;
import org.jpedal.external.ErrorTracker;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.ParserOptions;
import org.jpedal.parser.image.data.ImageData;
import org.jpedal.parser.image.utils.ConvertImageToShape;
import org.jpedal.parser.image.utils.ConvertMaskToShape;
import org.jpedal.parser.image.utils.JPegImageDecoder;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author markee
 */
class ImageDataToJavaImage {
    
     
    static BufferedImage convertImageDataToJavaImage(PdfObjectReader currentPdfFile, ParserOptions parserOptions, GraphicsState gs, DynamicVectorRenderer current,
                                                     final ImageData imageData, final float[] decodeArray, PdfArrayIterator Filters,
                                                     GenericColorSpace decodeColorData, final boolean imageMask,
                                                     final byte[] maskCol, boolean arrayInverted,
                                                     final PdfObject XObject, final int rawd, ErrorTracker errorTracker) throws RuntimeException, PdfException {
        
        BufferedImage image=null;
        
        byte[] index=decodeColorData.getIndexedMap();
        
        int w=imageData.getWidth();
        int h=imageData.getHeight();
        int d=imageData.getDepth();
        byte[] data=imageData.getObjectData();
        
        //handle any decode array
        if(decodeArray==null || decodeArray.length == 0){
        }else if(Filters!=null &&(imageData.isJPX()||imageData.isDCT())){ //don't apply on jpegs
        }else if(index==null){ //for the moment ignore if indexed (we may need to recode)
            ImageCommands.applyDecodeArray(data, d, decodeArray,decodeColorData.getID());
        }
        if (imageMask) {
            image = makeMaskImage(parserOptions, gs, current,w, h, d, data, imageData, imageMask, decodeColorData, maskCol);
            
        } else if (imageData.isDCT()) {
            
            //get image data,convert to BufferedImage from JPEG & save out
//            if(decodeColorData.getID()== ColorSpaces.DeviceCMYK && extractRawCMYK){
//                
//                LogWriter.writeLog("Raw CMYK image " + name + " saved.");
//                
//                if(!objectStoreStreamRef.saveRawCMYKImage(data, name)) {
//                    errorTracker.addPageFailureMessage("Problem saving Raw CMYK image " + name);
//                }
//                
//            }
            image = JPegImageDecoder.decode(w, h, arrayInverted, decodeColorData, data, decodeArray, imageData, XObject, errorTracker, parserOptions);
           
        }else if(imageData.isJPX()){
            
            image = JPeg2000ImageDecoder.decode(w, h, decodeColorData, data, decodeArray, imageData, d);
            
        } else { //handle other types
            
            LogWriter.writeLog( w + "W * " + h + "H BPC=" + d + ' ' + decodeColorData);
            
            image =makeImage(decodeColorData,w,h,d,data,imageData.getCompCount());
            
        }
        
        if (image != null) {
            
            image = addOverPrint(decodeColorData,  data, image, imageData, current, gs);

            if(image==null) {
                return null;
            }     
        }
        
        if(image == null && !imageData.isRemoved()){
            parserOptions.imagesProcessedFully=false;
        }
        //apply any transfer function
        final PdfObject TR=gs.getTR();
        if(TR!=null){ //array of values
            image = ImageCommands.applyTR(image, TR, currentPdfFile);
        }
        
        PdfObject DecodeParms=XObject.getDictionary(PdfDictionary.DecodeParms);
        //try to simulate some of blend by removing white if not bottom image
        if(DecodeParms!=null  && DecodeParms.getInt(PdfDictionary.Blend)!=PdfDictionary.Unknown && current.hasObjectsBehind(gs.CTM) && image!=null && image.getType()!=2 && image.getType()!=1 && (!imageData.isDCT() || DecodeParms.getInt(PdfDictionary.QFactor)==0)) {
            image = ImageCommands.makeBlackandWhiteTransparent(image);
        }
        
        //sharpen 1 bit
        if(rawd==1 && imageData.getpX()>0 && imageData.getpY()>0 && ImageCommands.sharpenDownsampledImages && (decodeColorData.getID()==ColorSpaces.DeviceGray || decodeColorData.getID()==ColorSpaces.DeviceRGB)){
            
            final Kernel kernel = new Kernel(3, 3,
                    new float[] {
                        -1, -1, -1,
                        -1, 9, -1,
                        -1, -1, -1});
            final BufferedImageOp op = new ConvolveOp(kernel);
            image = op.filter(image, null);
            
        }
        
        //transparency slows down printing so try to reduce if possible in printing
        if(!ImageDecoder.allowPrintTransparency && imageData.getMode()==ImageCommands.ID && parserOptions.isPrinting() && image!=null && d==1 && maskCol!=null && maskCol[0]==0 && maskCol[1]==0 && maskCol[2]==0 && maskCol[3]==0){
            
            final int iw=image.getWidth();
            final int ih=image.getHeight();
            final BufferedImage newImage=new BufferedImage(iw,ih,BufferedImage.TYPE_BYTE_GRAY);
            
            newImage.getGraphics().setColor(Color.WHITE);
            newImage.getGraphics().fillRect(0,0,iw,ih);
            newImage.getGraphics().drawImage(image, 0, 0, null);
            image=newImage;
        }
        
        if (imageMask && gs.nonstrokeColorSpace.getColor().isTexture()) {  //case 19095 vistair
            
            float mm[][] = gs.CTM;
            AffineTransform affine = new AffineTransform(mm[0][0], mm[0][1], mm[1][0], mm[1][1], mm[2][0], mm[2][1]);
                        
            BufferedImage temp = ((PatternColorSpace)gs.nonstrokeColorSpace).getRawImage(affine);
            BufferedImage scrap = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            
            if(temp!=null){
                TexturePaint tp = new TexturePaint(temp, new Rectangle(0,0,temp.getWidth(),temp.getHeight()));
                Graphics2D g2 = scrap.createGraphics();
                g2.setPaint(tp);
                Rectangle rect = new Rectangle(0,0,w,h);
                g2.fill(rect);
            }
           
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (image.getRGB(x, y) == -16777216) { //255 0 0 0
                        int pRGB = scrap.getRGB(x, y);
                        image.setRGB(x, y, pRGB);
                    }
                }
            }
        }
        
        return image;
    }
    
    private static byte[] correctDataArraySize(final int d, final int w, final int h, byte[] data) {
        if(d==1){
            final int requiredSize=((w+7)>>3)*h;
            final int oldSize=data.length;
            if(oldSize<requiredSize){
                final byte[] oldData=data;
                data=new byte[requiredSize];
                System.arraycopy(oldData,0,data,0,oldSize);
                
                //and fill rest with 255 for white
                for(int aa=oldSize;aa<requiredSize;aa++) {
                    data[aa] = (byte) 255;
                }
            }
            
        }else if(d==8){
            final int requiredSize=w*h;
            final int oldSize=data.length;
            if(oldSize<requiredSize){
                final byte[] oldData=data;
                data=new byte[requiredSize];
                System.arraycopy(oldData,0,data,0,oldSize);
            }
        }
        return data;
    }
    
    
     
    /**
     * turn raw data into a BufferedImage
     */
    private static BufferedImage makeImage(final GenericColorSpace decodeColorData, int w, int h, int d, byte[] data, final int comp) {
        
        //ensure correct size
        if(decodeColorData.getID()== ColorSpaces.DeviceGray){
            data = correctDataArraySize(d, w, h, data);
        }
        
        //final ColorSpace cs=decodeColorData.getColorSpace();
        final int ID=decodeColorData.getID();
        
        BufferedImage image = null;
        byte[] index =decodeColorData.getIndexedMap();
        
        if (index != null) {
            image = IndexedImage.make(w, h, decodeColorData, index, d, data);
            
        } else if (d == 1) {
            image =BinaryImage.make(w, h, data, decodeColorData, d);
            
        }else if(ID==ColorSpaces.Separation || ID==ColorSpaces.DeviceN){
            LogWriter.writeLog("Converting Separation/DeviceN colorspace to sRGB ");
            
            image=decodeColorData.dataToRGB(data,w,h);
            
        } else{
            
            switch(comp){
                case 4:  //handle CMYK or ICC or ARGB
                    if(decodeColorData.getID()==ColorSpaces.DeviceRGB){
                       image = ColorSpaceConvertor.createARGBImage(w,h,data); 
                    }else{
                        image =ColorSpaceConvertor.convertFromICCCMYK(w,h,data);
                    }
                    break;
                    
                case 3:
                    image = ThreeComponentImage.make(d, data, index, w, h);
                    break;
                    
                case 1:
                    image =OneBitImage.make(d, w, h, data);
                    break;
            }
        }
        
        return image;
    }
   
    private static BufferedImage makeMaskImage(final ParserOptions parserOptions, final GraphicsState gs, final DynamicVectorRenderer current, final int w, final int h, final int d, 
            final byte[] data, final ImageData imageData, final boolean imageMask, GenericColorSpace decodeColorData, final byte[] maskCol) {
        
         BufferedImage image=null;
        /*
         * allow for 1 x 1 pixels scaled up or fine lines
         */
        final float ratio=h/(float)w;
        if((parserOptions.isPrinting() && ratio<0.1f && w>4000 && h>1) || (ratio<0.001f && w>4000 && h>1)  || (w==1 && h==1)){// && data[0]!=0){
            
            ConvertMaskToShape.convert(gs, current, parserOptions);
            imageData.setRemoved(true);
        }else if(h==2 && d==1 && ImageCommands.isRepeatingLine(data, h)) {
            ConvertImageToShape.convert(data, h, gs, current, parserOptions);
            
            imageData.setRemoved(true);
        }else {
            image = MaskDecoder.createMaskImage((parserOptions.isPrinting() && !ImageDecoder.allowPrintTransparency), gs, parserOptions.isType3Font(), current,data, w, h, imageData, imageMask, d, decodeColorData, maskCol);
        }
        return image;
    }
    
    
    private static BufferedImage addOverPrint(GenericColorSpace decodeColorData, byte[] data, BufferedImage image, final ImageData imageData,final DynamicVectorRenderer current, final GraphicsState gs) {
        
        //handle any soft mask
        final int colorspaceID=decodeColorData.getID();
        
        if(image!=null) {
            image = ImageCommands.simulateOverprint(decodeColorData, data, imageData.isDCT(), imageData.isJPX(), image, colorspaceID, current, gs);
        }
        return image;
    }
  
}
