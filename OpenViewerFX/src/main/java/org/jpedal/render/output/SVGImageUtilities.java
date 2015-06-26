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
 * SVGImageUtilities.java
 * ---------------
 */
package org.jpedal.render.output;


import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import org.jpedal.color.ColorSpaces;
import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.image.ImageUtils;
import org.jpedal.utils.Matrix;

/**
 *
 */
class SVGImageUtilities {

    private static final boolean debugImage = false;

    public static BufferedImage applyTransformToImage(final float scaling, final int pageRotation, final boolean useHiResImageForDisplay, BufferedImage image,
            final int optionsApplied, final String name, final GraphicsState gs, final float finalWidth, final float finalHeight) {

        /**
         * raw data
         */
        if (SVGImageClipper.debugClipping || debugImage) {
            System.out.println("NEW " + name + " optionsApplied= " + optionsApplied + " rot=" + pageRotation + ' ' + " useHiResImageForDisplay=" + useHiResImageForDisplay);
            System.out.println("sizes= " + ' ' + (gs.CTM[0][0]) + ' ' + (gs.CTM[0][1]) + ' ' + (gs.CTM[1][0]) + ' ' + (gs.CTM[1][1]) + " x=" + gs.CTM[2][0] + " y=" + gs.CTM[2][1]);
            System.out.println("scaled sizes= " + ' ' + (gs.CTM[0][0] * scaling) + ' ' + (gs.CTM[0][1] * scaling) + ' ' + (gs.CTM[1][0] * scaling) + ' ' + (gs.CTM[1][1] * scaling) + " x=" + (gs.CTM[2][0] * scaling) + " y=" + (gs.CTM[2][1] * scaling));

            System.out.println("\n\n" + name + " Raw Image (" + image.getWidth() + ',' + image.getHeight() + ") finalW=" + finalWidth + " finalH=" + finalHeight + " useHires=" + useHiResImageForDisplay);

            System.out.println("---Raw ctm matrix---");
            Matrix.show(gs.CTM);

            if (gs.getClippingShape() != null) {
                System.out.println("clip=" + gs.getClippingShape().getBounds2D());
            }

            org.jpedal.gui.ShowGUIMessage.showGUIMessage("", image, name + " Raw Image (" + image.getWidth() + ',' + image.getHeight() + ')');

        }

        /**
         * page rotation (adjustment of signs needed on sheer)
         */
        float[][] ctm = {{gs.CTM[0][0], -gs.CTM[1][0], gs.CTM[0][2]},
        {-gs.CTM[0][1], gs.CTM[1][1], gs.CTM[1][2]},
        {gs.CTM[2][0], gs.CTM[2][1], gs.CTM[2][2]}};

        switch (pageRotation) {

            case 90:
                ctm = Matrix.multiply(new float[][]{{0, -1, 0}, {1, 0, 0}, {0, 0, 1}}, ctm);
                break;

            case 180:
                ctm = Matrix.multiply(new float[][]{{-1, 0, 0}, {0, -1, 0}, {1, 1, 1}}, ctm);
                break;

            case 270:
                ctm = Matrix.multiply(new float[][]{{0, 1, 0}, {-1, 0, 0}, {0, 0, 1}}, ctm);
                break;

            default: //0
                ctm = Matrix.multiply(new float[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}}, ctm);
        }
        final boolean switchWidthandHeigth = switchWidthandHeigth(gs.CTM,ctm,pageRotation);
        
        /**
         * factor in page scaling
         */
        if (!useHiResImageForDisplay) {
            ctm = Matrix.multiply(new float[][]{{scaling, 0, 0}, {0, scaling, 0}, {0, 0, 1}}, ctm);
        }

        return applyTransformToRawImage(ctm, scaling, pageRotation, useHiResImageForDisplay, image,switchWidthandHeigth , gs);

    }

    static BufferedImage applyTransformToRawImage(float[][] ctm, final float scaling, final int pageRotation, final boolean useHiResImageForDisplay, BufferedImage image, boolean switchWidthandHeigth,
            final GraphicsState gs) {

        final int w,h;

        /**
         * choose correct w and h
         */
        if (switchWidthandHeigth) {

            h = image.getWidth();
            w = image.getHeight();

        } else {
            w = image.getWidth();
            h = image.getHeight();
        }

        /**
         * image scaling
         */
        float[][] hiresAdjustment = null;
        if (useHiResImageForDisplay) {
            
            hiresAdjustment = factorHiresAdjustmentValues(ctm, pageRotation);

            resetHiresCTMValues(ctm);

        } else {

       final boolean scalew_h = pageRotation == 90 && ctm[0][0] < 0&& ctm[0][1] < 0 &&ctm [1][0] > 0 &&ctm [1][1] < 0 ||
                          pageRotation == 270 && ctm[0][0] > 0&& ctm[0][1] > 0 &&ctm [1][0] < 0 &&ctm [1][1] > 0;

            if ( (pageRotation == 0 || pageRotation ==180 || scalew_h) ) {
                ctm = Matrix.multiply(new float[][]{{1f / w, 0, 0}, {0, 1f / h, 0}, {0, 0, 1}}, ctm);
            } else {

                ctm = Matrix.multiply(new float[][]{{1f / h, 0, 0}, {0, 1f /w, 0}, {0, 0, 1}}, ctm);
            }
        }

        /**
         * work out adjustments for image (ix,iy)
         */
        double ix = 0, iy = 0;
        double[] adjustmetX_Y;
        int ww = w;
        int hh = h;

        if ((pageRotation == 90 || pageRotation == 270) ) {

            ww = h;
            hh = w;
        }

        if (ctm[0][0] < 0) {

            adjustmetX_Y = routineA(ctm, pageRotation, ww, hh, ix, iy);
            ix = adjustmetX_Y[0];
            iy = adjustmetX_Y[1];

        }

        if (ctm[0][1] < 0 && ctm[0][0] == 0) {
            ix -= (ww * ctm[0][1]);
            //  cx=ix;
        }

        if (ctm[1][0] < 0) {

            adjustmetX_Y = routineB( ctm, gs,pageRotation, ww, hh, ix, iy );
            ix = adjustmetX_Y[0];
            iy = adjustmetX_Y[1];
        }

        if (ctm[1][1] < 0 && ctm[1][0] == 0) {
            iy -= (hh * ctm[1][1]);
        }

        if (ctm[0][0] * ctm[0][1] > 0 && ctm[1][0] > 0 && pageRotation == 180) {
            iy = Math.abs((gs.CTM[1][1] / gs.CTM[1][0]) * w);
        }

        if (ctm[0][0] * ctm[0][1] < 0 && ctm[1][0] > 0 && pageRotation == 90) {
            ix = Math.abs((gs.CTM[1][1] / gs.CTM[1][0]) * w);

        }
        if (ctm[0][0] > 0 && ctm[1][0] > 0 && ctm[0][1] < 0 && pageRotation ==0) {
            ix = Math.abs((gs.CTM[1][0] / gs.CTM[0][0]) * w);

        }

        if (!useHiResImageForDisplay) {
            adjustmetX_Y = routineE(ctm, gs, pageRotation, scaling, ix, iy);
            ix = adjustmetX_Y[0];
            iy = adjustmetX_Y[1];
        }

        boolean ignoreScaling = false;
        if (ctm[1][1] > 0 && ctm[1][1] < 1 && image.getHeight() <= 1) { //1 pixel high
            ignoreScaling = true;
        }

        if (ctm[0][0] > 1.5 && ctm[1][1] > 0 && ctm[1][1] < 0.2) {
            //avoid scaling on heavy distortion as better to leave to browser
            // ie /PDFdata/test_data/sample_pdfs_html/general-July2012/Geoffrey Moore TPSA Summit Keynote - Managing Services in a Product Company.pdf
            ignoreScaling = true;
        }

        AffineTransformOp SVGimageTransform;
        final boolean complexMatrix = ctm[0][0] * ctm[0][1] * ctm[1][0] * ctm[1][1] != 0;
        final float [][] shearMatrix;
     
        if (complexMatrix) {
            // figure out the width and heigth and Shears
            if (Math.abs(gs.CTM[0][0]) > Math.abs(gs.CTM[0][1])) {
                shearMatrix = routineC(ctm, gs, pageRotation, h, w, scaling, useHiResImageForDisplay);

            } else {
                shearMatrix = routineD(ctm, gs, pageRotation, h, w, scaling, useHiResImageForDisplay);
            }

            if (pageRotation == 90 || pageRotation == 270) { //when 4 ctm's are set
                SVGimageTransform = new AffineTransformOp(new AffineTransform(shearMatrix[0][1], shearMatrix[0][0], shearMatrix[1][0], shearMatrix[1][1], ix, iy), ColorSpaces.hints); //90 imgae 5 & 1
            } else {
                SVGimageTransform = new AffineTransformOp(new AffineTransform(shearMatrix[0][0], shearMatrix[0][1], shearMatrix[1][1], shearMatrix[1][0], ix, iy), ColorSpaces.hints); //180 working imgae 5 & 1
            }

        } else { //only when two ctm's are set

            SVGimageTransform = new AffineTransformOp(new AffineTransform(ctm[0][0], ctm[1][0], ctm[0][1], ctm[1][1], ix, iy), ColorSpaces.hints);
        }

        if (!ignoreScaling) {

            //spot case where we just rotate image
            if (gs.CTM[0][0] == 0 && gs.CTM[1][1] == 0 && Math.abs(gs.CTM[0][1] - image.getWidth()) < 1 && Math.abs(gs.CTM[1][0] - image.getHeight()) < 1) {
                image = ImageUtils.invertImage(image);
            } else {
                image = SVGimageTransform.filter(image, null);
            }
        }

        int clipWidth = 0, clipHeight = 0;
        /**
         * sort out clip
         */
        final Area clip = gs.getClippingShape();
        if (clip != null) {

            clipWidth = clip.getBounds().width;
            clipHeight = clip.getBounds().height;
        }

        final boolean ignoreClip = SVGImageClipper.ignoreClipping(ignoreScaling, clip, clipWidth, gs, clipHeight);

        if (ignoreScaling || ignoreClip) {

            return image;
        } else {

            return SVGImageClipper.applyClipToImage(clip, useHiResImageForDisplay, scaling, gs, w, h, pageRotation, hiresAdjustment, ctm, image);
        }
    }

    private static void resetHiresCTMValues(float[][] ctm) {
        if (ctm[0][0] > 0) {
            ctm[0][0] = 1;
        } else if (ctm[0][0] < 0) {
            ctm[0][0] = -1;
        }
        
        if (ctm[0][1] > 0) {
            ctm[0][1] = 1;
        } else if (ctm[0][1] < 0) {
            ctm[0][1] = -1;
        }
        
        if (ctm[1][0] > 0) {
            ctm[1][0] = 1;
        } else if (ctm[1][0] < 0) {
            ctm[1][0] = -1;
        }
        
        if (ctm[1][1] > 0) {
            ctm[1][1] = 1;
        } else if (ctm[1][1] < 0) {
            ctm[1][1] = -1;
        }
    }

    private static float[][] factorHiresAdjustmentValues(final float[][] ctm, final int pageRotation) {
        float[][] hiresAdjustment;
        float a = 0;
        final float b = 0;
        final float c = 0;
        float d = 0;
        float temp;
        if (ctm[0][0] != 0  && ctm[0][0] * ctm[0][1] == 0) {
            a = Math.abs(1f / ctm[0][0]);
            //System.out.println("a1");
        }
        if (ctm[0][1] != 0 && ctm[0][0] * ctm[0][1] == 0) {
            a = Math.abs(1f / ctm[0][1]);
            // System.out.println("a1");
        }
        if ((ctm[0][1]*ctm[0][0]) != 0 && Math.abs(ctm[0][0]) > Math.abs(ctm[0][1])) {
            a = Math.abs(1f / ctm[0][0]);
            //System.out.println("a1");
        }
        if ((ctm[0][1]*ctm[0][0]) != 0 && Math.abs(ctm[0][0]) < Math.abs(ctm[0][1])) {
            a = Math.abs(1f / ctm[0][1]);
            
        }
        if (ctm[1][0] != 0 && ctm[1][0] * ctm[1][1] == 0) {
            d = Math.abs(1f / ctm[1][0]);
        }
        if ((ctm[1][1]*ctm[1][0]) != 0 && Math.abs(ctm[1][0]) > Math.abs(ctm[1][1])) {
            d = Math.abs(1f / ctm[1][0]);
        }
        if (ctm[1][1] != 0 && ctm[1][0] * ctm[1][1] == 0) {
            d = Math.abs(1f / ctm[1][1]);
        }
        if ((ctm[1][1]*ctm[1][0]) != 0 && Math.abs(ctm[1][0]) < Math.abs(ctm[1][1])) {
            d = Math.abs(1f / ctm[1][1]);
            //System.out.println("d1");
        }
        if ((ctm[1][1]*ctm[1][0]) != 0 && Math.abs(ctm[1][0]) == Math.abs(ctm[1][1])) {
            d = Math.abs(1f / ctm[1][0]);
            //System.out.println("d1");
        }
        if ((ctm[0][1]*ctm[0][0]) != 0 && Math.abs(ctm[0][0]) == Math.abs(ctm[0][1])) {
            a = Math.abs(1f / ctm[1][1]);
            //System.out.println("d1");
        }
        if(((ctm[0][0] > 0 && ctm [0][1] >0  && ctm[0][0]*ctm[0][1]!=0 )||(ctm[0][0] < 0 && ctm [0][1] <0 && ctm[0][0]*ctm[0][1]!=0 )) &&   
            (pageRotation ==90 || pageRotation ==270)){
                temp = a;
                a = d;
                d = temp;
            }
        
        hiresAdjustment = new float[][]{{a, c, 0}, {b, d, 0}, {0, 0, 1}};
        return hiresAdjustment;
    }
    
   
    private static boolean switchWidthandHeigth(final float[][] gsCTM, final float[][] ctm, final int pageRotation) {
        boolean result = false;

        if (pageRotation == 0 || pageRotation == 180) {

            if (gsCTM[0][1] !=0 && Math.abs(gsCTM[0][1]) == Math.abs(ctm[1][0])) {
                result = !((gsCTM[0][1] * gsCTM[0][0]) != 0 && (Math.abs(gsCTM[0][0]) > Math.abs(gsCTM[0][1])) );
            }

        }else {
            result = true;
            if (gsCTM[0][0] !=0 && Math.abs(gsCTM[0][0]) == Math.abs(ctm[1][0])) {

                result = (gsCTM[0][1] * gsCTM[0][0]) != 0;
            }

        }

        return result;
    }
    
     private static double[] routineA(final float[][] ctm, int pageRotation, final int width, final int heigth, double ix,  double iy ){
        final double [] coords = new double[2]; 
          ix -= (width * ctm[0][0]);

            if (ctm[0][0] < 0 && ctm[1][1] < 0 && ctm[1][0] > 0 && pageRotation == 90) {
                ix = -(heigth * ctm[0][0]); //fixes 90

            }
            if (ctm[0][0] < 0 && ctm[1][1] < 0 && ctm[1][0] < 0 && pageRotation == 180) {
                ix = -(heigth * ctm[0][0]); // fixes 180

            }
            if (ctm[0][0] < 0 && ctm[1][1] < 0 && ctm[1][0] < 0 && pageRotation == 270) {
                ix += (width * ctm[1][1]); // fixes 270
            }
            if (ctm[0][0] < 0 && ctm[1][0] > 0 && ctm[0][1] < 0 && pageRotation == 0) {
                ix = -(heigth * ctm[0][0]); // 
                iy = (width * ctm[1][0]);
            }

            if (ctm[0][0] < 0 && ctm[1][0] < 0 && ctm[0][1] < 0 && pageRotation == 0) {
                 ix = -(width*ctm[0][1]); // 
                iy = (heigth * ctm[1][0]);

            }
        coords[0] = ix;
        coords[1] = iy;        
        return coords;
    } 
    
    
     private static double[] routineB(final float[][] ctm,final GraphicsState gs,int pageRotation, final int width, final int heigth, double ix,  double iy ){
        final double [] coords = new double[2]; 
           iy -= (heigth * ctm[1][0]);

            if (ctm[0][0] < 0 && ctm[0][1] > 0 && ctm[1][1] < 0 && pageRotation == 180) {
                ix = -(width * ctm[1][0]); // fixes 180

            }
            if ((ctm[0][0] > 0 && ctm[0][1] > 0 && ctm[1][1] > 0) && pageRotation == 270) {
                iy = -(width * ctm[1][0]);

            }
            if ((ctm[0][0] < 0 && ctm[0][1] > 0 && ctm[1][1] < 0) && pageRotation == 270) {

                ix = -(width * ctm[1][0]);
                iy = (heigth * ctm[0][1]);
            }
            if ((ctm[0][0] > 0 && ctm[0][1] > 0 && ctm[1][0] < 0 && pageRotation == 0) &&
                 (Math.abs(gs.CTM[0][0]) > Math.abs(gs.CTM[0][1]))){
                 iy =(gs.CTM[0][1]/gs.CTM[1][1])*heigth;
                }

            if(ctm[0][0] < 0 && ctm[1][0] < 0 && ctm[0][1] < 0 & ctm[2][1] < 0 && pageRotation == 0){
                
                iy=(gs.CTM[0][1]/gs.CTM[1][1])*heigth;
            }
        coords[0] = ix;
        coords[1] = iy;        
        return coords;
    } 
     
    
    private static float[][] routineC(final float [][] ctm,final GraphicsState gs,int pageRotation,final int h,final int w,final float scaling,
            final boolean useHiResImageForDisplay){
        
       final float [][] transform = new float[2][2];
       float width, heigth , widthShear, heigthShear;
       
        heigth = ctm[0][1];
                width = ctm[1][0];
                switch (pageRotation) {
                    case 90:

                        widthShear = (gs.CTM[0][1] / gs.CTM[0][0]);// fixes 90
                        heigthShear = (gs.CTM[1][0] / gs.CTM[1][1]);// fixes 90
                        if (!useHiResImageForDisplay) {
                            widthShear = -(gs.CTM[1][1] / h) * (gs.CTM[1][0] / gs.CTM[1][1]) * scaling; //90
                            heigthShear = -(gs.CTM[0][0] / w) * (gs.CTM[0][1] / gs.CTM[0][0]) * scaling;//90
                        }

                        break;
                    case 180:
                        heigth = ctm[1][1];
                        width = ctm[0][0];
                        widthShear = -(gs.CTM[1][0] / gs.CTM[1][1]);
                        heigthShear = -(gs.CTM[0][1] / gs.CTM[0][0]);
                        break;
                    case 270:
                        widthShear = -(gs.CTM[0][1] / gs.CTM[0][0]);
                        heigthShear = (gs.CTM[1][0] / gs.CTM[1][1]);
                        if (!useHiResImageForDisplay) {
                            heigthShear = (gs.CTM[1][1] / w) * (gs.CTM[1][0] / gs.CTM[1][1]) * scaling; //270
                            widthShear = -(gs.CTM[0][0] / h) * (gs.CTM[0][1] / gs.CTM[0][0]) * scaling; //270
                        }
                        break;
                    default:
                        heigth = ctm[1][1];
                        width = ctm[0][0];

                        // \PDFdata\test_data\sample_pdfs_html\14may\17441.pdf
                        if (ctm[0][0] < 0 && ctm[0][1] < 0 && ctm[1][0] < 0 && ctm[2][1] < 0) {

                            heigthShear =  (gs.CTM[0][1] / gs.CTM[0][0]);
                            widthShear =-(gs.CTM[1][0] / gs.CTM[1][1]);
                        } else {
                            heigthShear = -(gs.CTM[1][0] / gs.CTM[1][1]);
                            widthShear = -(gs.CTM[0][1] / gs.CTM[0][0]);
                        }
                        if (!useHiResImageForDisplay) {
                            // \PDFdata\test_data\sample_pdfs_html\14may\17441.pdf
                            if (ctm[0][0] < 0 && ctm[0][1] < 0 && ctm[1][0] < 0 && ctm[2][1] < 0) {
                                heigthShear = -(gs.CTM[0][0] / w) * (gs.CTM[0][1] / gs.CTM[0][0]) * scaling;
                                widthShear =-(gs.CTM[1][1] / h) * (gs.CTM[1][0] / gs.CTM[1][1]) * scaling;
                            } else {
                                heigthShear = (gs.CTM[0][0] / w) * (gs.CTM[0][1] / gs.CTM[0][0]) * scaling; //0 degree   
                                widthShear = (gs.CTM[1][1] / h) * (gs.CTM[1][0] / gs.CTM[1][1]) * scaling;// 0 degree
                            }
                        }

                        break;
                }
                
       transform[0][0] = width;
       transform[0][1] = widthShear;
       transform[1][0] = heigth;
       transform[1][1] = heigthShear;
       
       
       return transform;
       
    } 
    
    
     private static float[][] routineD(final float [][] ctm,final GraphicsState gs,int pageRotation,final int h,final int w,final float scaling,
            final boolean useHiResImageForDisplay){
        
       final float [][] transform = new float[2][2];
       float width, heigth , widthShear, heigthShear;
       
         switch (pageRotation) {
                    case 90:
                        heigth = (gs.CTM[1][1] / gs.CTM[1][0]);
                        widthShear = ctm[0][0];
                        width = (gs.CTM[0][0] / gs.CTM[0][1]);
                        heigthShear = ctm[1][1];
                        if (!useHiResImageForDisplay) {
                            width = -(gs.CTM[1][0] / w) * (gs.CTM[0][0] / gs.CTM[0][1]) * scaling; //90 page 
                            heigth = (gs.CTM[0][1] / h) * (gs.CTM[1][1] / gs.CTM[1][0]) * scaling; //90 page 
                        }
                        break;
                    case 180:
                        widthShear = ctm[1][0];
                        heigthShear = ctm[0][1];
                         width =-(gs.CTM[0][0] / gs.CTM[0][1]);
                         heigth =(gs.CTM[1][1] / gs.CTM[1][0]);
                        if (!useHiResImageForDisplay) {
                            width = (gs.CTM[1][0] / w) * (gs.CTM[0][0] / gs.CTM[0][1]) * scaling;//////////////// 180 degree
                            heigth = (gs.CTM[0][1] / h) * (gs.CTM[1][1] / gs.CTM[1][0]) * scaling;/////////// 180 degree
                        }
                        break;
                    case 270:
                        widthShear = ctm[0][0];
                        heigthShear = ctm[1][1];
                        heigth = (gs.CTM[0][0] / gs.CTM[0][1]);
                        width = (gs.CTM[1][1] / gs.CTM[1][0]);
                        if (!useHiResImageForDisplay) {
                            heigth = -(gs.CTM[1][0] / w) * (gs.CTM[0][0] / gs.CTM[0][1]) * scaling; //270 page 
                            width = (gs.CTM[0][1] / h) * (gs.CTM[1][1] / gs.CTM[1][0]) * scaling; //270 page 
                        }
                        break;
                    default:
                        widthShear = ctm[1][0];
                        heigthShear = ctm[0][1];
                        width = (gs.CTM[0][0] / gs.CTM[0][1]);
                        heigth = -(gs.CTM[1][1] / gs.CTM[1][0]);
                        if (!useHiResImageForDisplay) {
                            if (gs.CTM[0][0] == gs.CTM[0][1]) {
                                width = (gs.CTM[0][1] / w) * (gs.CTM[0][0] / gs.CTM[0][1]) * scaling;//////////////// 0 degree
                                heigth = (gs.CTM[1][0] / h) * (gs.CTM[1][1] / gs.CTM[1][0]) * scaling;/////////// 0 degree
                            } else {
                                width = -(gs.CTM[1][0] / w) * (gs.CTM[0][0] / gs.CTM[0][1]) * scaling;//////////////// 0 degree
                                heigth = -(gs.CTM[0][1] / h) * (gs.CTM[1][1] / gs.CTM[1][0]) * scaling;/////////// 0 degree
                            }
                        }
                        break;
                }
             
       transform[0][0] = width;
       transform[0][1] = widthShear;
       transform[1][0] = heigth;
       transform[1][1] = heigthShear;
       
       
       return transform;
       
    }


    private static double[] routineE(final float[][] ctm, final GraphicsState gs, int pageRotation, final float scaling, double ix, double iy) {
        final double[] coords = new double[2];
        switch (pageRotation) {
            case 90:
                if (ctm[0][0] < 0 && ctm[1][0] > 0 && ctm[0][1] < 0) {
                    ix = (gs.CTM[1][1] + Math.abs(gs.CTM[0][1])) * scaling;

                }

                if (ctm[0][0] > 0 && ctm[1][0] > 0 && ctm[0][1] < 0) {
                    ix = (gs.CTM[1][1]) * scaling;

                }
                break;
            case 180:
                if (ctm[0][0] < 0 && ctm[1][0] > 0 && ctm[0][1] < 0) {
                    ix -= gs.CTM[1][0] * scaling;
                }
                if (ctm[0][0] < 0 && ctm[1][0] < 0 && ctm[0][1] > 0) {
                    ix = (gs.CTM[0][0]) * scaling;
                    iy += (gs.CTM[1][1]) * scaling;
                }
                break;
            case 270:
                if (ctm[0][0] < 0 && ctm[1][0] < 0 && ctm[0][1] > 0) {
                    ix = (gs.CTM[0][1]) * scaling;
                    iy = (Math.abs(gs.CTM[1][0])) * scaling;

                }
                break;
            default:
                if (ctm[0][0] < 0 && ctm[1][0] > 0 && ctm[0][1] < 0) {
//                      sort out  aeroplanes here
                    // ix =(Math.abs(gs.CTM[1][1]*scaling)+Math.abs(gs.CTM[0][1]));//Math.abs(gs.CTM[0][0]*scaling)+Math.abs(gs.CTM[0][1]*scaling);
                    // iy =(Math.abs(gs.CTM[0][0]*scaling)+Math.abs(gs.CTM[1][0]));//-(gs.CTM[1][1]*scaling)+(gs.CTM[1][0]*scaling);
                    //                       System.out.println("iy = "+iy);
                }
                if (ctm[0][0] > 0 && ctm[1][0] < 0 && ctm[0][1] > 0) {

                    iy = ((gs.CTM[0][1])) * scaling;

                }
                if (ctm[0][0] > 0 && ctm[1][0] > 0 && ctm[0][1] < 0) {
                    ix = ((gs.CTM[1][0])) * scaling;

                }
                if (ctm[0][0] < 0 && ctm[1][0] < 0 && ctm[0][1] < 0) {
                    ix = -((gs.CTM[0][0])) * scaling;
                    iy = (gs.CTM[0][1]) * scaling;
                }

                break;

        }
        coords[0] = ix;
        coords[1] = iy;
        return coords;
    }
}
