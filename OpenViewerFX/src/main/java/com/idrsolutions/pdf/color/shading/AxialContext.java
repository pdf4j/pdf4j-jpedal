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
 * AxialContext.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.function.PDFFunction;

public class AxialContext implements PaintContext {
	
	private float maxPDFX=-9999,maxPDFY=-9999,minPDFX=9999,minPDFY=9999;
	
	final GenericColorSpace shadingColorSpace;
	
	private float scaling=1f;
	
	private final boolean[] isExtended;
    
	private final float x0,x1,y0,y1,t0;
	private float t1=1.0f;

	private final PDFFunction[] function;

    private final boolean isPrinting;
    //private static Object[][] cachedXY=new Object[1000][1000];

    
	private final int pageHeight;

	private final boolean colorsReversed;
	
	private final int minX;

	private final float[] background;

	private final float offX,offY;
	
    int  pageRotation;
    //private float printScale=4;

    //private static float lastScaling=-1;
    
    //private float[][] matrix=null;

    //@printIssue - this is where we pass values through
    AxialContext(final AffineTransform xform, final boolean isPrinting, final int offX, final int offY, final int minX, final int pHeight, final float scaling, final boolean[] isExtended, final float[] domain,
                        final float[] coords, final GenericColorSpace shadingColorSpace, final boolean colorsReversed, final float[] background, final PDFFunction[] function){


        final double[] aff=new double[6];
        xform.getMatrix(aff);
        
        if(aff[0]==0 && aff[1]>0 && aff[2]>0 && aff[3]==0) {
            pageRotation = 90;
        }
        
        //currently bit o hack but fits 18992
        //isInverted=aff[4]<0 && aff[5]<0 && renderingType==DynamicVectorRenderer.CREATE_SH;
        
        //this.isInverted= aff[0]>0 && aff[1]==0 && aff[2]==0 && aff[3]>0 && renderingType==DynamicVectorRenderer.CREATE_SH;
        
    	this.isPrinting=isPrinting;

        this.offX=offX;
		this.offY=offY;
		
		this.colorsReversed=colorsReversed;
		this.pageHeight=pHeight;
		this.isExtended=isExtended;
		this.t0=domain[0];
		this.t1=domain[1];
		this.background=background;
		
		x0=coords[0];
		x1=coords[2];
		y0=coords[1];
		y1=coords[3];
		
		this.shadingColorSpace=shadingColorSpace;
		
		this.function = function;
		this.scaling=scaling;
		
		this.minX=minX;

    }
	@Override
    public void dispose() {}
	
	@Override
    public ColorModel getColorModel() { return ColorModel.getRGBdefault(); }
    
	@Override
    public Raster getRaster(int xstart, int ystart, final int w, final int h) {
        
        //swap over if rotated
        if(pageRotation==90){
            final int tmp=xstart;
            xstart=ystart;
            ystart=tmp;
        }
        
		//just average if tiny and not visible (note we cannot use this in HTML so first test to avoid)
		final boolean isTooSmall= (w/scaling<=1f || h/scaling<=1f); //
////@mark
        
        //create buffer to hold all this data
		final int rastSize=(w * h * 4);
		
		final int[] data = new int[rastSize];

		//System.out.println("Area="+xstart+" ystart="+ystart+" w="+w+" h="+h+" "+data.length);
        
		float xx;
        float t;
        float lastT=-1f;
        final float dx=x1-x0;
        final float dy=y1-y0;

        //workout outside loop as constant
		final float divisor=((dx*dx)+(dy*dy));

        //workout color range
		final Color col0;
		
		if(colorsReversed) {
            col0 = calculateColor(t1);
        } else {
            col0 = calculateColor(t0);
        }

		//set current calues to default
		int cr=col0.getRed(),cg=col0.getGreen(),cb=col0.getBlue();

		if(background!=null){

            shadingColorSpace.setColor(background,4);
            final Color c=(Color) shadingColorSpace.getColor();

            //y co-ordinates
            for (int y = 0; y < h; y++) {

                //x co-ordinates
                for (int x = 0; x < w; x++) {
			
					//set color for the pixel with values
					final int base = (y * w + x) * 4;
					data[base] = c.getRed();
					data[base + 1] = c.getGreen();
					data[base + 2] = c.getBlue();
					data[base + 3] = 255;
					
				}
			}
		}
		
		float pdfX;
        float pdfY;
        
        //y co-ordinates
		for (int y = 0; y < h; y++) {
			
			//x co-ordinates			
			for (int x = 0; x < w; x++) {


				//cache what is quite a slow operation
				final float[] xy;

				if(pageRotation != 90){
					xy = PixelFactory.convertPhysicalToPDF(isPrinting, x, y, offX, offY, scaling, xstart, ystart, minX, pageHeight);
				}else{
					// Fix for debug2/StampsProblems and 13jun/A380PDP-pg1
					xy = PixelFactory.convertPhysicalToPDF(isPrinting, y, x, offX, offY, scaling, xstart, ystart, minX, pageHeight);
				}

				pdfX = xy[0];
				pdfY = xy[1];

				if(isTooSmall){
					xx=0.5f;
				}else {
					xx = ((dx * (pdfX - x0)) + (dy * ((pdfY) - y0))) / divisor;
				}

				//invert for print as wrong way round
				final float yDiff=y0-y1;

				if(isPrinting && yDiff<0) {
					xx = 1 - xx;
				}

				if(pdfX>maxPDFX) {
					maxPDFX = pdfX;
				}
				if(pdfX<minPDFX) {
					minPDFX = pdfX;
				}

				if(pdfY>maxPDFY) {
					maxPDFY = pdfY;
				}
				if(pdfY<minPDFY) {
					minPDFY = pdfY;
				}


                if(xx<0 && isExtended[0]){
					t=t0;
				}else if(xx>1 && isExtended[1]){
					t=t1;
				}else{
					t=t0+((t1-t0)*xx);
				}

                if(isTooSmall) {
                    t = 0.5f;
                }

				if(t>=t0 && t<=t1){

                    if(colorsReversed) {
                        t = 1 - t;
                    }

                    if(t!=lastT){ //cache unchanging values
						
						lastT=t;

						final Color c=calculateColor(t);
						cr=c.getRed();
						cg=c.getGreen();
						cb=c.getBlue();
					}
					
					//set color for the pixel with values
					
                    final int base = (y * w + x) * 4;
                    data[base] = cr;
					data[base + 1] = cg;
					data[base + 2] = cb;
					data[base + 3] = 255;//(int)(col.getAlpha());
                    
				}
			}		
		}
		
		//set values
		//we have to get the raster this way for java me to use the raster as me does not have createCompatableWritableRaster
		final WritableRaster raster = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB).getRaster();
		raster.setPixels(0, 0, w, h, data);

        return raster;
	}
	
	/**workout rgb color*/
	private Color calculateColor(final float val) {

		final Color col;

        final float[] colValues = ShadingFactory.applyFunctions(function,new float[]{val});

		shadingColorSpace.setColor(colValues,colValues.length);

        col=(Color) shadingColorSpace.getColor();

        return col;

	}	
}
