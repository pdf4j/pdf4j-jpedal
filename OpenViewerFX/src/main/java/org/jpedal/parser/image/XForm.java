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
 * XForm.java
 * ---------------
 */
package org.jpedal.parser.image;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.Matrix;

/**
 *
 */
public class XForm {
    
    private static boolean hasEmptySMask(final GraphicsState gs){
        return gs.SMask!=null && gs.SMask.getGeneralType(PdfDictionary.SMask)==PdfDictionary.None;
    }
    
    public static PdfObject getSMask(final float[] BBox, final GraphicsState gs, final PdfObjectReader currentPdfFile) {
        PdfObject newSMask=null;

        //ignore if none
        if(hasEmptySMask(gs)){

            return null;
        }

        if(gs.SMask!=null && BBox!=null){ //see if SMask to cache to image & stop negative cases such as Milkshake StckBook Activity disX.pdf
           
            //if(gs.SMask.getParameterConstant(PdfDictionary.Type)!=PdfDictionary.Mask || gs.SMask.getFloatArray(PdfDictionary.BC)!=null){ //fix for waves file
                newSMask= gs.SMask.getDictionary(PdfDictionary.G);
                currentPdfFile.checkResolved(newSMask);
                
           // }
        }
        return newSMask;
    }
    
    private static final float[] matches={1f,0f,0f,1f,0f,0f};

    public static boolean isIdentity(final float[] matrix) {

        boolean isIdentity=true;// assume right and try to disprove

        if(matrix!=null){

            //see if it matches if not set flag and exit
            for(int ii=0;ii<6;ii++){
                if(matrix[ii]!=matches[ii]){
                    isIdentity=false;
                    break;
                }
            }
        }

        return isIdentity;
    }

    
    public static Area setClip(final Shape defaultClip, final float[] BBox, final GraphicsState gs, final DynamicVectorRenderer current) {
        
        Rectangle rect = new Rectangle();
        rect.setFrameFromDiagonal(BBox[0],BBox[1],BBox[2],BBox[3]);
        
        float minX = (float)rect.getMinX();
        float minY = (float)rect.getMinY();
        
        float maxX = (float)rect.getMaxX();
        float maxY = (float)rect.getMaxY();
        
        float[] p1 = Matrix.transformPoint(gs.CTM, minX, minY);
        float[] p2 = Matrix.transformPoint(gs.CTM, maxX, minY);
        float[] p3 = Matrix.transformPoint(gs.CTM, maxX, maxY);
        float[] p4 = Matrix.transformPoint(gs.CTM, minX, maxY);
        
        GeneralPath gp = new GeneralPath();
        gp.moveTo(p1[0], p1[1]);
        gp.lineTo(p2[0], p2[1]);
        gp.lineTo(p3[0], p3[1]);
        gp.lineTo(p4[0], p4[1]);
        gp.closePath();
        
        final Area clip;

        if(gs.getClippingShape()==null) {
            clip=null;
        }
        else {
            clip= (Area) gs.getClippingShape().clone();
        }
        
        gs.updateClip(new Area(gp));
        current.drawClip(gs, defaultClip,false) ;

        return clip;
    }

}


