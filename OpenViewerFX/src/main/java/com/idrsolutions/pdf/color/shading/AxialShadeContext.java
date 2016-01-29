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
 * AxialShadeContext.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.function.PDFFunction;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author suda
 */
public class AxialShadeContext implements PaintContext {

    private final GenericColorSpace shadingColorSpace;
    private final PDFFunction[] function;

    private final float[] background;
    private float[] domain = {0.0f, 1.0f};
    private boolean[] extension;
    final float t0;
    final float t1;
    final double x0;
    final double y0;
    final double x1;
    final double y1;
    final double deltaX;
    final double deltaY;
    final double multiXY;
    //final double textX;
    //final double textY;

    private final AffineTransform shadeAffine;
    private AffineTransform inversed = new AffineTransform();

    public AxialShadeContext(AffineTransform xform, GenericColorSpace shadingColorSpace, float[] background, PdfObject shadingObject, float[][] mm, PDFFunction[] function) {
        
        
        this.shadingColorSpace = shadingColorSpace;
        this.function = function;
        //this.textX = textX;
        //this.textY = textY;

        final float[] newDomain = shadingObject.getFloatArray(PdfDictionary.Domain);
        if (newDomain != null) {
            domain = newDomain;
        }
        this.background = background;
        extension = shadingObject.getBooleanArray(PdfDictionary.Extend);
        if (extension == null) {
            extension = new boolean[]{false, false};
        }

        t0 = domain[0];
        t1 = domain[1];
        
        if (mm == null) {
            shadeAffine = new AffineTransform();
        } else {
            shadeAffine = new AffineTransform(mm[0][0], mm[0][1], mm[1][0], mm[1][1], mm[2][0], mm[2][1]);
        }
        
        try {
            AffineTransform invXF = xform.createInverse();
            AffineTransform invSH = shadeAffine.createInverse();
            invSH.concatenate(invXF);
            inversed = (AffineTransform)invSH.clone();
        } catch (NoninvertibleTransformException ex) {
            LogWriter.writeLog("Exception "+ex+ ' ');
        }
        
        float[] coords = shadingObject.getFloatArray(PdfDictionary.Coords);
       
        x0 = coords[0];
        y0 = coords[1];
        x1 = coords[2];
        y1 = coords[3];
       
        deltaX = (x1 - x0);
        deltaY = (y1 - y0);
        multiXY = deltaX * deltaX + deltaY * deltaY;

    }

    @Override
    public void dispose() {

    }

    @Override
    public ColorModel getColorModel() {
        return ColorModel.getRGBdefault();
    }

    @Override
    public Raster getRaster(int startX, int startY, int w, int h) {

        final int rastSize = (w * h * 4);
        final int[] data = new int[rastSize];

        if (background != null) {
            shadingColorSpace.setColor(background, 4);
            final Color c = (Color) shadingColorSpace.getColor();
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    final int base = (i * w + j) * 4;
                    data[base] = c.getRed();
                    data[base + 1] = c.getGreen();
                    data[base + 2] = c.getBlue();
                    data[base + 3] = 255;
                }
            }
        }
        
        double x,y,xp;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                boolean render = true;
                double[] src = { startX + j, startY + i};
                inversed.transform(src, 0, src, 0, 1);
                
                x = src[0];
                y = src[1];
                float t = 0;                
                xp = (deltaX*(x-x0)+ deltaY*(y-y0))/multiXY;
                
                if (xp >= 0 && xp <= 1) {
                    t = (float) (t0 + (t1 - t0) * xp);
                } else if (xp < 0 && extension[0]) {
                    t = t0;
                } else if (xp > 1 && extension[1]) {
                    t = t1;
                } else {
                    render = false;
                }
                if (render) {
                    Color c = calculateColor(t);
                    final int base = (i * w + j) * 4;
                    data[base] = c.getRed();
                    data[base + 1] = c.getGreen();
                    data[base + 2] = c.getBlue();
                    data[base + 3] = 255;
                }
            }
        }

        final WritableRaster raster = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB).getRaster();
        raster.setPixels(0, 0, w, h, data);
        return raster;

    }

    private Color calculateColor(final float val) {
        final Color col;
        final float[] colValues = ShadingFactory.applyFunctions(function, new float[]{val});
        shadingColorSpace.setColor(colValues, colValues.length);
        col = (Color) shadingColorSpace.getColor();
        return col;
    }

    
}
