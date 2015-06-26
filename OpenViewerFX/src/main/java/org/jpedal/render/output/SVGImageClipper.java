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
 * SVGImageClipper.java
 * ---------------
 */
package org.jpedal.render.output;

import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.GraphicsState;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import org.jpedal.render.BaseDisplay;

public class SVGImageClipper {

    static final  boolean debugClipping = false;

    static BufferedImage applyClipToImage(Area clip, final boolean useHiResImageForDisplay, float scaling, final GraphicsState gs, final int w, final int h, final int pageRotation,final float[][] hiresAdjustment, final float[][] ctm, BufferedImage image) {

        clip = (Area) clip.clone();
        if (useHiResImageForDisplay) {
            scaling = 1;
        }

        //page rotation, scaling and offset
        int Xpolarity = 1;
        int Ypolarity = 1;
        int scaleWidthpolarity = 1;
        int scaleHeigthpolarity = 1;

        switch (pageRotation) {

            case 90:
                Xpolarity *= 1;
                Ypolarity *= -1;
                scaleWidthpolarity *= 1;
                scaleHeigthpolarity *= 1;
                break;
//
            case 180:
                Xpolarity *= -1;
                Ypolarity *= -1;
                scaleWidthpolarity *= -1;
                scaleHeigthpolarity *= 1;
                break;

            case 270:
                Xpolarity *= -1;
                Ypolarity *= 1;
                scaleWidthpolarity *= -1;
                scaleHeigthpolarity *= -1;
                break;

            default:
                Xpolarity = 1;
                Ypolarity = 1;
                scaleWidthpolarity *= 1;
                scaleHeigthpolarity *= -1;
        }
        final boolean complexMatrix = ctm[0][0] * ctm[0][1] * ctm[1][0] * ctm[1][1] != 0;
        //page rotation, scaling and offset
        if(useHiResImageForDisplay){
            transformHiresClip(complexMatrix,w, hiresAdjustment, gs, ctm, pageRotation, h, scaling, clip, scaleWidthpolarity, scaleHeigthpolarity, Xpolarity, Ypolarity);
        }else{
            transformLowResClip(complexMatrix,ctm, gs, scaling, pageRotation, h, w, clip, scaleWidthpolarity, scaleHeigthpolarity, Xpolarity, Ypolarity);
        }

        /**
         * show clip and image
         */
        if (debugClipping) {
            showImageForDebugging(clip, ctm, image);
        }

        return applyClipToImage(clip, image);
    }

    private static void transformLowResClip(final Boolean complexMatrix,final float[][] ctm, final GraphicsState gs, float scaling, final int pageRotation, 
            final int h, final int w, Area clip, int scaleWidthpolarity, int scaleHeigthpolarity, int Xpolarity, int Ypolarity) {
        
        
        double clipW=-gs.CTM[2][0]*scaling;
        double clipH=(gs.CTM[2][1]+gs.CTM[1][1])*scaling;
        //double shearX =0;
        //double shearY =0;
        if(!complexMatrix){
            if (ctm[0][0] < 0 ) {
                clipW -= (gs.CTM[0][0] * scaling);
                
            }
            
            if (ctm[1][0] < 0 ) {
                clipW -= (gs.CTM[1][0] * scaling);
                if (pageRotation == 270) {
                    clipW -= gs.CTM[0][0] * scaling;
                }
            }
            
            if (ctm[0][0] > 0 ) {
                clipW -= gs.CTM[1][0] * scaling;
                
            }
            
            if (ctm[1][1] < 0 ) {
                clipH -= (gs.CTM[1][1] * scaling);
                if (pageRotation == 90) { //some images in 180 have the same ctm
                    clipH -= (Math.abs(gs.CTM[0][1]) * scaling);
                }
                if (pageRotation == 270) { //some images in 180 have the same ctm
                    clipH -= (gs.CTM[1][1] * scaling) - (gs.CTM[0][1] * scaling);
                }
            }
            
            if (ctm[0][1] > 0 ) {
                clipH += (gs.CTM[0][1] * scaling);
            }
            
            if (ctm[1][0] > 0 && pageRotation == 90) {
                    clipH -= gs.CTM[1][1] * scaling;
                }
                
            }
        
        
        if (complexMatrix) {
            switch (pageRotation) {
                case 90:
                    if (ctm[0][0] > 0 && ctm[0][1] < 0 && ctm[1][0] > 0 && ctm[1][1] > 0) {
                        clipW -= gs.CTM[1][0] * scaling;
                        clipH += ctm[0][1] * h;
                        
                    }
                    if (ctm[0][0] < 0 && ctm[1][0] > 0 && ctm[0][1] < 0 && ctm[0][0] * ctm[0][1] != 0) {
                        clipH +=(gs.CTM[0][1] * scaling)-(gs.CTM[1][1]*scaling);
                        
                    }
                    break;
                case 180:
                    if (ctm[0][0] < 0 && ctm[0][1] < 0 && ctm[1][0] > 0 && ctm[1][1] < 0) {
                        clipW -= (gs.CTM[0][0] * scaling);
                        clipH += (ctm[1][1] * w);
                    }
                    if (ctm[0][0] < 0 && ctm[0][1] > 0 && ctm[1][0] < 0 && ctm[1][1] < 0) {
                        clipW -= (gs.CTM[0][0] + gs.CTM[1][0]) * scaling;
                        clipH -= (Math.abs(gs.CTM[0][1]) + gs.CTM[1][1]) * scaling;
                    }
                    
                    break;
                case 270:
                    
                    if (ctm[0][0] > 0 && ctm[0][1] > 0 && ctm[1][0] < 0 && ctm[1][1] > 0) {
                        clipW -= ((gs.CTM[0][0] + gs.CTM[1][0]) * scaling);
                        
                    }
                    if (ctm[0][0] < 0 && ctm[1][0] < 0 && ctm[0][1] > 0 && ctm[1][1] < 0) {
                        clipH += (gs.CTM[0][1] * scaling);
                        
                    }
                    break;
                default:
                    if (ctm[0][0] > 0 && ctm[0][1] > 0 && ctm[1][0] < 0 && ctm[1][1] > 0) {
                        clipW -= gs.CTM[1][0] * scaling;
                        clipH += gs.CTM[0][1] * scaling;
                    }
                    // special case for \PDFdata\test_data\sample_pdfs_html\14may\17441.pdf
                    if (ctm[0][0] < 0 && ctm[0][1] < 0 && ctm[1][0] < 0 && ctm[2][1] < 0) {
                        
                        clipW -=(gs.CTM[0][0]+gs.CTM[1][0]) *scaling;
                        clipH +=gs.CTM[0][1] * scaling;
                    }
                    if (ctm[0][0] < 0 && ctm[0][1] < 0 && ctm[1][0] > 0 && ctm[1][1] < 0) {
                        //   clipW = -379;//gs.CTM[1][0] * scaling;
                        //  clipH =1050;// gs.CTM[0][1] * scaling;
                    }
            }
        }
        
        
        if(pageRotation == 0 || pageRotation == 180){
            clip.transform(new AffineTransform(new double[]{(scaling*scaleWidthpolarity), 0, 0, (scaling*scaleHeigthpolarity),(clipW*Xpolarity),(clipH*Ypolarity)}));
        }else {
            clip.transform(new AffineTransform(new double[]{0, (scaling*scaleHeigthpolarity),(scaling*scaleWidthpolarity), 0, (Ypolarity*clipH),(Xpolarity*clipW)}));
        }
    }

    private static void transformHiresClip(final Boolean complexMatrix, final int w, final float[][] hiresAdjustment, final GraphicsState gs, final float[][] ctm,
            final int pageRotation, final int h, float scaling, Area clip, int scaleWidthpolarity, int scaleHeigthpolarity, int Xpolarity, int Ypolarity) {
        double clipH=0;
        // double clipW=0;
        double clipW=-w*hiresAdjustment[0][0]*(gs.CTM[2][0]);
        
        
        if (!complexMatrix) {
            if (ctm[0][0] != 0 && ctm[1][1] != 0) {
                switch (pageRotation) {
                    case 90:
                        if (ctm[0][0] < 0) {
                            clipW = -h * hiresAdjustment[0][0] * (gs.CTM[2][1] + gs.CTM[1][1] + gs.CTM[0][1]);
                            clipH = w * hiresAdjustment[1][1] * (gs.CTM[2][0] + gs.CTM[1][0] - gs.CTM[1][0]);
                            
                        } else {
                            clipW = -h * hiresAdjustment[0][0] * (gs.CTM[2][1] + gs.CTM[1][1]);
                            clipH = w * hiresAdjustment[1][1] * (gs.CTM[2][0] + gs.CTM[1][0]);
                        }
                        break;
                    case 180:
                        clipW = -(w * hiresAdjustment[0][0] * (gs.CTM[2][0])) - w;
                        clipH = (h * Math.abs(hiresAdjustment[1][1]) * (gs.CTM[2][1] + gs.CTM[1][1])) - h;
                        break;
                    case 270:
                        if (ctm[0][0] > 0) {
                            clipW = (-h * hiresAdjustment[0][0] * (gs.CTM[2][1] + gs.CTM[1][1] + gs.CTM[0][1])) - h;
                            clipH = (w * hiresAdjustment[1][1] * (gs.CTM[2][0] + gs.CTM[1][0] - gs.CTM[1][0]) + w);
                            
                        } else {
                            clipW = -h * hiresAdjustment[0][0] * (gs.CTM[2][1] + gs.CTM[1][1] + gs.CTM[0][1]);
                            clipH = w * hiresAdjustment[1][1] * (gs.CTM[2][0] + gs.CTM[1][0] - gs.CTM[1][0]);
                        }
                        break;
                    default:
                        if(ctm[0][0]>0 && ctm[1][1]<0){
                            clipW = -w * hiresAdjustment[0][0] * (gs.CTM[2][0]);
                            clipH =  h * Math.abs(hiresAdjustment[1][1]) * (gs.CTM[2][1] );
                            
                        }else if(ctm[0][0]<0 && ctm[1][1]>0){
                            clipW = -w * hiresAdjustment[0][0] * (gs.CTM[2][0])+w;
                            clipH =  h * Math.abs(hiresAdjustment[1][1]) * (gs.CTM[2][1] + gs.CTM[1][1]);
                        }else{
                            clipW = -w * hiresAdjustment[0][0] * (gs.CTM[2][0]);
                            clipH =  h * Math.abs(hiresAdjustment[1][1]) * (gs.CTM[2][1] + gs.CTM[1][1]);
                        }
                        
                        
                        break;
                }
            } else {
                switch (pageRotation) {
                    case 90:
                        clipW = -h * hiresAdjustment[0][0] * (gs.CTM[2][1]);
                        clipH = w * Math.abs(hiresAdjustment[1][1]) * (gs.CTM[2][0]);
                        break;
                    case 180:
                        if (ctm[0][1] > 0) {
                            clipW = (-w * hiresAdjustment[0][0] * (gs.CTM[2][0])) - w;
                            clipH = (h * hiresAdjustment[1][1] * (gs.CTM[2][1])) - h;
                        } else {
                            clipW = (-w * hiresAdjustment[0][0] * (gs.CTM[2][0]));
                            clipH = (h * hiresAdjustment[1][1] * (gs.CTM[2][1]));
                            
                        }
                        break;
                    case 270:
                        clipW = (-h * hiresAdjustment[0][0] * (gs.CTM[2][1])) - h;
                        clipH = (w * Math.abs(hiresAdjustment[1][1]) * (gs.CTM[2][0])) + w;
                        break;
                    default:
                        if (ctm[0][1] < 0) {
                            clipW = -w * hiresAdjustment[0][0] * (gs.CTM[2][0]);
                            clipH = h * hiresAdjustment[1][1] * (gs.CTM[2][1]);
                        } else{
                            clipW = w - (w * hiresAdjustment[0][0] * (gs.CTM[2][0]));
                            clipH = h + (h * hiresAdjustment[1][1] * (gs.CTM[2][1]));
                            
                        }
                        
                        break;
                }
                
            }
        }
        if (complexMatrix) {
            switch (pageRotation) {
                case 90:
                    if (ctm[0][0] < 0 && ctm[1][0] > 0 && ctm[0][1] < 0) {
                        clipH = w * Math.abs(hiresAdjustment[1][1]) * (gs.CTM[2][0]);
                        clipW  = -h * hiresAdjustment[0][0] * (gs.CTM[2][1]);
                        clipW -= ((gs.CTM[0][1] / gs.CTM[0][0]) * Math.abs(clipW));
                        
                    }
                    
                    if (ctm[0][0] > 0 && ctm[1][0] > 0 && ctm[0][1] < 0) {
                        clipW = (-h * hiresAdjustment[0][0] * (gs.CTM[2][1]));
                        clipH = w * Math.abs(hiresAdjustment[1][1]) * (gs.CTM[2][0] + gs.CTM[1][0]);
                        
                    }
                    break;
                case 180:
                    if (ctm[0][0] < 0 && ctm[1][0] > 0 && ctm[0][1] < 0) {
                        clipH = h * Math.abs(hiresAdjustment[1][1]) * (gs.CTM[2][1]);
                        clipW = (-w * (hiresAdjustment[0][0] * (gs.CTM[2][0])) - (hiresAdjustment[0][0] * gs.CTM[0][1]));
                    }
                    if (ctm[0][0] < 0 && ctm[1][0] < 0 && ctm[0][1] > 0) {
                        clipW =- (w * hiresAdjustment[0][0] * (ctm[2][0]) );
                        clipH = h * hiresAdjustment[1][1] * (gs.CTM[2][1]);
                        clipH +=((gs.CTM[0][1]/gs.CTM[0][0])*Math.abs(clipH));
                    }
                    
                    break;
                case 270:
                    if (ctm[0][0] < 0 && ctm[1][0] < 0 && ctm[0][1] > 0) {
                        clipW = -h * hiresAdjustment[0][0] * (gs.CTM[0][1] + gs.CTM[2][1] + gs.CTM[1][1]);
                        clipH = w * Math.abs(hiresAdjustment[1][1]) * (gs.CTM[2][0]);
                    }
                    if (ctm[0][0] > 0 && ctm[1][0] < 0 && ctm[0][1] > 0) {
                        clipW = -h * hiresAdjustment[0][0] * (gs.CTM[2][1] + gs.CTM[1][1]);
                        clipH = w * Math.abs(hiresAdjustment[1][1]) * (gs.CTM[2][0] + gs.CTM[1][0]) + h;
                    }
                    break;
                default:
                    
                    if (ctm[0][0] > 0 && ctm[1][0] < 0 && ctm[0][1] > 0) {
                        clipW -= w * hiresAdjustment[0][0] * (gs.CTM[1][0]);
                        clipH = h * Math.abs(hiresAdjustment[1][1]) * (gs.CTM[2][1] + gs.CTM[1][1]+gs.CTM[0][1]);
                        
                    }
                    if (ctm[0][0] > 0 && ctm[1][0] > 0 && ctm[0][1] < 0) {
                        clipW = -w * hiresAdjustment[0][0] * (gs.CTM[2][0]) - scaling;
                        clipH = h * Math.abs(hiresAdjustment[1][1]) * (gs.CTM[2][1] + gs.CTM[1][1]);
                        
                    }
//
                    if (ctm[0][0] < 0 && ctm[1][0] < 0 && ctm[0][1] < 0 && ctm[2][1] <0) {
                        
                        clipH = (h * Math.abs(hiresAdjustment[1][1]) * (Math.abs(gs.CTM[2][1]) + gs.CTM[1][1]));
                        clipW += w * Math.abs(hiresAdjustment[0][0])* ( + Math.abs(gs.CTM[2][0])-
                                (Math.abs(gs.CTM[1][0]) -Math.abs(gs.CTM[2][1])));
                        
                    }
                    
                    break;
             }
        }
        
        if (pageRotation == 0 || pageRotation == 180) {
            
            clip.transform(new AffineTransform(scaleWidthpolarity*w* hiresAdjustment[0][0], scaleWidthpolarity * w * hiresAdjustment[0][1], scaleHeigthpolarity * h * hiresAdjustment[1][0],scaleHeigthpolarity* h * hiresAdjustment[1][1],(clipW*Xpolarity),(clipH*Ypolarity))); //142,130 for 1.5, 202,190 for 2
        }else {
            clip.transform(new AffineTransform(scaleWidthpolarity *w* hiresAdjustment[0][1],scaleHeigthpolarity *  w * hiresAdjustment[1][1],scaleWidthpolarity *  h * hiresAdjustment[0][0],scaleHeigthpolarity * h * hiresAdjustment[1][0],(clipW*Xpolarity),(clipH*Ypolarity))); //142,130 for 1.5, 202,190 for 2
        }

    }

    private static BufferedImage applyClipToImage(Area clip, BufferedImage image) {

        final int iw = image.getWidth();
        final int ih = image.getHeight();
        final Area inverseClip = new Area(new Rectangle(0, 0, iw, ih));
        inverseClip.exclusiveOr(clip);
        image = ColorSpaceConvertor.convertToARGB(image);//make sure has opacity
        final Graphics2D image_g2 = image.createGraphics(); //g2 of canvas
        image_g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        image_g2.setComposite(AlphaComposite.Clear);
        image_g2.fill(inverseClip);
        //  image_g2.setPaint(Color.CYAN);
        // image_g2.fill(clip);

        if (debugClipping) {
            org.jpedal.gui.ShowGUIMessage.showGUIMessage("", image, "3b. image after clip " + image.getType());
        }

        return image;
    }
    
    static boolean ignoreClipping(boolean ignoreScaling, Area clip, int clipWidth, final GraphicsState gs, int clipHeight) {
        return ignoreScaling || clip == null
                || ((Math.abs(clipWidth - Math.abs(gs.CTM[0][0])) < 2f || Math.abs(clipWidth - Math.abs(gs.CTM[1][0])) < 2)
                && (Math.abs(clipHeight - Math.abs(gs.CTM[1][1])) < 2f || Math.abs(clipHeight - Math.abs(gs.CTM[0][1])) < 2)
                && BaseDisplay.isSimpleOutline(clip));
    }
    
    private static void showImageForDebugging(Area clip, float[][] ctm, BufferedImage image) {

        final BufferedImage i = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g2debug = i.createGraphics();

        g2debug.setPaint(Color.GREEN);
        g2debug.drawRect(0, 250, 250, 250);
        g2debug.drawRect(250, 0, 250,250);

        g2debug.translate(250,250);
        g2debug.scale(0.25f, 0.25f);

        g2debug.drawImage(image, null,0,0);

        g2debug.setPaint(Color.RED);
        g2debug.draw(clip);

        org.jpedal.gui.ShowGUIMessage.showGUIMessage("", i, "Clip over page" + ' ' + ctm[1][0]+ ' ' +ctm[0][1]);
    }

}
