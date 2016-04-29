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
 * PdfPrintTransform.java
 * ---------------
 */

package org.jpedal.utils;

/*
 * Encapsulates the creation of a <tt>AffineTransform</tt> that mimics the effects
 * of the printing options available in Adobe Acrobat.
 *
 * <p>The available options additional are auto rotate and centre, fit to page and shrink to page.
 * If page is larger than the page when asked to fit or smaller than the page when asked to shrink
 * it is unaffected apart from being centred.  This is same effect found in Acrobat printing.<p>
 * <p>Currently implements Printable for testing purposes</p>
 *
 */

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import javax.print.PrintServiceLookup;
import org.jpedal.objects.PrinterOptions;


public final class PrintUtils
{
    /**
     * Create a <tt>AffineTransform</tt> to effect page according to scaling and
     * centring options.
     * 
     * @param cropBoxX The crop box x position
     * @param cropBoxY The crop box y position
     * @param width The crop box width position
     * @param height The crop box height position
     * @param pageRotation The pages rotation
     * @param format The print page format
     * @param autoRotate The print auto rotate option
     * @param centerOnScaling The print centre on scaling option
     * @param chooseSourceByPDFSize Use PDF page size for print page format
     * @param scalingMode The print scaling option
     * @return Page transform for the given options
     */
    public static AffineTransform getPageTransform(final int cropBoxX, final int cropBoxY, final int width, final int height, final int pageRotation, final PageFormat format, boolean autoRotate, boolean centerOnScaling, boolean chooseSourceByPDFSize, int scalingMode){
        return getPageTransform(new Rectangle(cropBoxX, cropBoxY, width, height), pageRotation, format, autoRotate, centerOnScaling, chooseSourceByPDFSize, scalingMode);
    }
    
    /**
     * Create a <tt>AffineTransform</tt> to effect page according to scaling and
     * centring options.
     * 
     * @param crop The pages crop box
     * @param pageRotation The pages rotation
     * @param format The print page format
     * @param autoRotate The print auto rotate option
     * @param centerOnScaling The print centre on scaling option
     * @param chooseSourceByPDFSize Use PDF page size for print page format
     * @param scalingMode The print scaling option
     * @return Page transform for the given options
     */
    public static AffineTransform getPageTransform(final Rectangle crop, int pageRotation, final PageFormat format, boolean autoRotate, boolean centerOnScaling, boolean chooseSourceByPDFSize, int scalingMode){
        
        final AffineTransform result = new AffineTransform();
        double scalingFactor = 1; //Reset scaling factor
        
        boolean shouldRotate = false;
        
        //Find whether the page needs to be rotated for best fit
        if(autoRotate || chooseSourceByPDFSize) {
            final boolean isPageWidthLong = format.getImageableWidth() > format.getImageableHeight();
            boolean isImageWidthLong = crop.width > crop.height;
            
            //As adobe does we print page based on the rotation the page is displayed at
            //so take this into account when determining if the width is longer than height
            if(pageRotation==90 || pageRotation==270){
                //Use height as width
                isImageWidthLong = crop.width < crop.height;
            }
            
            shouldRotate = isPageWidthLong != isImageWidthLong;
        }
        
        //Printed page will output all upside down to factor this out we add 180 on to all page rotations
        if(!shouldRotate){
            pageRotation += 180;
            if(pageRotation>360) {
                pageRotation-=360;
            }
        }else{
            //To rotate to match adobe requires a 270 degree turn on top of the 180 to turn page upright.
            //Instead of adding 270 we ignore the 180 rotation and instead only rotate by 90 (as total is 90 over a full 360 spin).
            pageRotation += 90;
            if(pageRotation>360) {
                pageRotation-=360;
            }
        }
        
        //Calculate scaling factor
        final double w = pageRotation%180==0 ? crop.width : crop.height;
        final double h = pageRotation%180==0 ? crop.height : crop.width;
        
        final double widthRatio = (format.getImageableWidth() - 1) / w;
        final double heightRatio = (format.getImageableHeight() - 1) / h;
        
        if(((widthRatio < 1 || heightRatio < 1) && scalingMode == PrinterOptions.PAGE_SCALING_REDUCE_TO_PRINTER_MARGINS) ||
                (scalingMode == PrinterOptions.PAGE_SCALING_FIT_TO_PRINTER_MARGINS)) {
            
            scalingFactor = widthRatio < heightRatio ? widthRatio : heightRatio;
        }
        
        
        //If page is had rotation, rotate content for printing.
        if(pageRotation!=0 || shouldRotate){
            //Apply rotation to transform
            PrintUtils.applyRotation(result, pageRotation, crop, scalingFactor);
        }
        
        //Factor out crop box
        if(crop.x!=0 || crop.y!=0){
            PrintUtils.applyCrop(result, pageRotation, crop, scalingFactor);
        }
        
        //Offset based on imagable area
        PrintUtils.applyOffset(result, pageRotation, format);

        //if we center the page, center it here
        if(centerOnScaling){
            PrintUtils.applyCentering(result, pageRotation, format, crop, scalingFactor);
        }
        
        //Scale page
        if(scalingMode != PrinterOptions.PAGE_SCALING_NONE) {
            result.scale(scalingFactor, scalingFactor);
        }
        
        //Flip page
        result.scale(-1, 1);
        result.translate(-crop.width, 0);
        
        //Return transform for the page.
        return result;
    }
    
    //Add the rotation of the page to the transform so page can appear in the imageable area
    private static void applyRotation(final AffineTransform result, final int rotation, Rectangle crop, double scalingFactor){
        final int factor = rotation / 90;
        
        //Perform rotation
        result.rotate(factor * Math.PI / 2.0);
        
        //Move page back into display, this varies based on rotation
        switch(factor){
            case 1 : // 90 degree rotation
                result.translate(0, -(crop.height*scalingFactor));
                break;
                
            case 2 : //180 degree rotation
                result.translate(-(crop.width*scalingFactor), -(crop.height*scalingFactor));
                break;
                
            case 3 : //270 degree rotation
                result.translate(-(crop.width*scalingFactor), 0);
                break;
        }
        
    }
    
    //Add the crop of the page to the transform so page can appear in the imageable area
    private static void applyCrop(final AffineTransform result, final int rotation, Rectangle crop, double scalingFactor){
        
        /*
         * Annoyingly I do not understand why the code below works.
         * By my reckoning the crop x / y values should change from positive to negative based on rotation.
         * How ever when I tried this the crop values were broke in all cases except 90 degrees.
         * When I tested this combination in other broken cases, they worked correctly..
         *
         * I have kept the switch in here in case I am proved wrong at any point so we can quickly tweak
         * the values for different rotations.
         */
        
        switch(rotation){
            case 0 :
                result.translate(crop.x*scalingFactor, -crop.y*scalingFactor);
                break;
            case 90 :
                result.translate(crop.x*scalingFactor, -crop.y*scalingFactor);
                break;
            case 180 :
                result.translate(crop.x*scalingFactor, -crop.y*scalingFactor);
                break;
            case 270 :
                result.translate(crop.x*scalingFactor, -crop.y*scalingFactor);
                break;
        }
    }
    
    //Add the offset of the imageable area
    private static void applyOffset(final AffineTransform result, final int rotation, PageFormat format){
        switch(rotation){
            case 0 :
                result.translate(format.getImageableX(), format.getImageableY());
                break;
            case 90 :
                result.translate(format.getImageableX(), -format.getImageableY());
                break;
            case 180 :
                result.translate(-format.getImageableX(), -format.getImageableY());
                break;
            case 270 :
                result.translate(-format.getImageableX(), format.getImageableY());
                break;
        }
        
    }
    
    //Add an offset so page will be centered in the iamgeable area
    private static void applyCentering(final AffineTransform result, final int rotation, PageFormat format, Rectangle crop, double scalingFactor){
        
        double centerOnX = 0;
        double centerOnY = 0;
        
        switch (rotation) {
            case   0:
                centerOnX = (format.getImageableWidth()-(crop.width*scalingFactor))/2;
                centerOnY = (format.getImageableHeight()-(crop.height*scalingFactor))/2;
                break;
            case  90:
                centerOnX = (format.getImageableHeight()-(crop.width*scalingFactor))/2;
                centerOnY = -((format.getImageableWidth()-(crop.height*scalingFactor))/2);
                break;
            case 180:
                centerOnX = -(format.getImageableWidth()-(crop.width*scalingFactor))/2;
                centerOnY = -(format.getImageableHeight()-(crop.height*scalingFactor))/2;
                break;
            case 270:
                centerOnX = -(format.getImageableHeight()-(crop.width*scalingFactor))/2;
                centerOnY = (format.getImageableWidth()-(crop.height*scalingFactor))/2;
                break;
        }
        
        result.translate(centerOnX, centerOnY);
        
    }
    
    public static void refreshPrinterList() {
        Class<?>[] classes = PrintServiceLookup.class.getDeclaredClasses();
        for (Class<?> classe : classes) {
            if (classe.getName().equals("javax.print.PrintServiceLookup$Services")) {
                sun.awt.AppContext.getAppContext().remove(classe);
                break;
            }
        }
    }
}