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
 * DeviceCMYKColorSpace.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.color.ColorSpace;
import java.awt.image.*;
import org.jpedal.JDeliHelper;
import org.jpedal.examples.handlers.DefaultImageHelper;
import org.jpedal.objects.raw.PdfObject;

import org.jpedal.exception.PdfException;

/**
 * handle DeviceCMYKColorSpace
 */
public class DeviceCMYKColorSpace extends GenericColorSpace {

    private static final long serialVersionUID = 4054062852632000027L;

    private float lastC = -1, lastM = -1, lastY = -1, lastK = -1;

    private static final ColorSpace CMYK = new FastColorSpaceCMYK();

    /**
     * ensure next setColor will not match with old color as value may be out of
     * sync
     */
    @Override
    public void clearCache() {
        lastC = -1;
    }

    public DeviceCMYKColorSpace() {

        componentCount = 4;

        cs = CMYK;

        setType(ColorSpaces.DeviceCMYK);

    }

    /**
     * set CalRGB color (in terms of rgb)
     */
    @Override
    public final void setColor(final String[] number_values, final int items) {

        final float[] colValues = new float[items];

        for (int ii = 0; ii < items; ii++) {
            colValues[ii] = Float.parseFloat(number_values[ii]);
        }

        setColor(colValues, items);
    }

    /**
     * convert CMYK to RGB as defined by Adobe (p354 Section 6.2.4 in Adobe 1.3
     * spec 2nd edition) and set value
     */
    @Override
    public final void setColor(final float[] operand, final int length) {

        //default of black
        c = 1;
        y = 1;
        m = 1;
        k = 1;

        if (length > 3) {
            //get values
            c = operand[0];
            // the cyan
            m = operand[1];
            // the magenta
            y = operand[2];
            // the yellow
            k = operand[3];
        } else {
            //get values
            if (length > 0) {
                c = operand[0];
            }
            // the cyan
            if (length > 1) {
                m = operand[1];
            }
            // the magenta
            if (length > 2) {
                y = operand[2];
            }
            // the yellow
            if (length > 3) {
                k = operand[3];
            }

        }

        if ((lastC == c) && (lastM == m) && (lastY == y) && (lastK == k)) {
            //no change
        } else {
            rawValues = new float[4];
            rawValues[0] = c;
            rawValues[1] = m;
            rawValues[2] = y;
            rawValues[3] = k;

            lastC = c;
            lastM = m;
            lastY = y;
            lastK = k;

            int cc = (int) (c * 255);
            int mm = (int) (m * 255);
            int yy = (int) (y * 255);
            int kk = (int) (k * 255);

            int[] bb = JDeliHelper.convertCMYKtoRGB(cc, mm, yy, kk);
            if (bb == null) {
                bb = new int[3];
                bb[0] = (int) (255 * (1 - c) * (1 - k));
                bb[1] = (int) (255 * (1 - m) * (1 - k));
                bb[2] = (int) (255 * (1 - y) * (1 - k));
            }
            this.currentColor = new PdfColor(bb[0], bb[1], bb[2]);

        }
    }

    /**
     * <p>
     * Convert DCT encoded image bytestream to sRGB
     * </p>
     * <p>
     * It uses the internal Java classes and the Adobe icm to convert CMYK and
     * YCbCr-Alpha - the data is still DCT encoded.
     * </p>
     * <p>
     * The Sun class JPEGDecodeParam.java is worth examining because it contains
     * lots of interesting comments
     * </p>
     * <p>
     * I tried just using the new IOImage.read() but on type 3 images, all my
     * clipping code stopped working so I am still using 1.3
     * </p>
     */
    @Override
    public final BufferedImage JPEGToRGBImage(
            final byte[] data, final int w, final int h, final float[] decodeArray, final int pX, final int pY, final boolean arrayInverted, final PdfObject XObject) {

        return nonRGBJPEGToRGBImage(data, w, h, decodeArray, pX, pY);

    }

    /**
     * default RGB implementation just returns data
     */
    @Override
    public byte[] dataToRGBByteArray(final byte[] data, final int w, final int h, boolean arrayInverted) {

        int pixelCount = w * h * 4;
        final int dataSize = data.length;
        if (pixelCount > dataSize) { //allow for mis-sized
            pixelCount = dataSize - 3;
        }

        final byte[] output = JDeliHelper.convertCMYK2RGB(w, h, pixelCount, data);
        if (output != null) {
            return output;
        } else {
            return convertCMYK2RGBWithSimple(w, h, pixelCount, data);
        }
    }

    public static byte[] convertCMYK2RGBWithSimple(final int w, final int h, int pixelCount, final byte[] data) {
        byte[] output = new byte[w * h * 3];
        float cc, mm, yy, kk;
        int pp = 0;

        for (int i = 0; i < pixelCount; i += 4) {
            cc = (data[i] & 0xff) / 255f;
            mm = (data[i + 1] & 0xff) / 255f;
            yy = (data[i + 2] & 0xff) / 255f;
            kk = (data[i + 3] & 0xff) / 255f;

            output[pp++] = (byte) (255 * (1 - cc) * (1 - kk));
            output[pp++] = (byte) (255 * (1 - mm) * (1 - kk));
            output[pp++] = (byte) (255 * (1 - yy) * (1 - kk));
        }
        return output;
    }

    /**
     * convert byte[] datastream JPEG to an image in RGB
     *
     * @throws PdfException
     */
    @Override
    public BufferedImage JPEG2000ToRGBImage(final byte[] data, int w, int h, final float[] decodeArray, final int pX, final int pY, final int d) throws PdfException {

        BufferedImage image = DefaultImageHelper.JPEG2000ToRGBImage(data, w, h, decodeArray, pX, pY);

        if (image != null) {
            return image;
        } else {
            return JPEG2000ToImage(data, pX, pY);
        }
    }

    /**
     * convert Index to RGB
     */
    @Override
    public final byte[] convertIndexToRGB(final byte[] index) {

        isConverted = true;

        return convert4Index(index);
    }

}
