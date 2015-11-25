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
 @LICENSE@
 *
 * ---------------
 * CMYKtoRGB.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;

/**
 * handle image conversion and optimise for JDK
 */
public class CMYKtoRGB {

    public static BufferedImage convert(final Raster ras, final int w, final int h) {
        byte[] rasData = ((DataBufferByte) ras.getDataBuffer()).getData();
        return decode(rasData, w, h);
    }

    public static BufferedImage convert(final byte[] cmyk, final int w, final int h) {
        return decode(cmyk, w, h);
    }

    private static BufferedImage decode(final byte[] rasData, final int w, final int h) {
        
        final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int[] imageData = ((java.awt.image.DataBufferInt) image.getRaster().getDataBuffer()).getData();
        
        
        final java.awt.color.ColorSpace CMYK = DeviceCMYKColorSpace.getColorSpaceInstance();
        int dim = w * h;
        int p = 0;
        int c, m, y, k;
        for (int i = 0; i < dim; i++) {
            c = rasData[p++] & 0xff;
            m = rasData[p++] & 0xff;
            y = rasData[p++] & 0xff;
            k = rasData[p++] & 0xff;
            float[] RGB = CMYK.toRGB(new float[]{c / 255f, m / 255f, y / 255f, k / 255f});
            int r = (int) (RGB[0] * 255);
            int g = (int) (RGB[1] * 255);
            int b = (int) (RGB[2] * 255);

            imageData[i] = (r << 16) | (g << 8) | b;
        }
        return image;
        
    }
}
