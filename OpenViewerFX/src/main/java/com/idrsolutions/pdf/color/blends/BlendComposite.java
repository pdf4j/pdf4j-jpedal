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
 * BlendComposite.java
 * ---------------
 */
package com.idrsolutions.pdf.color.blends;

import java.awt.CompositeContext;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.jpedal.color.CompositeUtil;
import org.jpedal.objects.raw.PdfDictionary;

public class BlendComposite implements CompositeContext {

    private final float fixedAlpha;
    private final ColorModel srcModel;
    private final ColorModel dstModel;
    private final int bm;

    BlendComposite(final ColorModel srcColorModel, final ColorModel dstColorModel, int blendMode, float alpha) {
        srcModel = srcColorModel;
        dstModel = dstColorModel;
        fixedAlpha = alpha;
        bm = blendMode;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {

        int snComp = srcModel.getNumComponents();
        int bnComp = dstModel.getNumComponents();
        int snColors = srcModel.getNumColorComponents();
        int bnColors = dstModel.getNumColorComponents();

        int width = Math.min(Math.min(src.getWidth(), dstIn.getWidth()), dstOut.getWidth());
        int height = Math.min(Math.min(src.getHeight(), dstIn.getHeight()), dstOut.getHeight());

        float[] sColors = new float[snComp]; //src colors
        float[] bColors = new float[bnComp]; //backdrop colors

        boolean hasAlphaS = srcModel.hasAlpha();
        boolean hasAlphaB = dstModel.hasAlpha();

        Object srcPixel = null, dstPixel = null;

        float aS, aB, aR, cS, cB, vv = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                dstPixel = dstIn.getDataElements(x, y, dstPixel);
                bColors = dstModel.getNormalizedComponents(dstPixel, bColors, 0);

                srcPixel = src.getDataElements(x, y, srcPixel);
                sColors = srcModel.getNormalizedComponents(srcPixel, sColors, 0);

                boolean isWhite = isWhiteBackdrop(bColors);

                aS = hasAlphaS ? sColors[snColors] : 1f;
                aS *= fixedAlpha;

                aB = hasAlphaB ? bColors[bnColors] : 1f;
                aR = aB + aS - aS * aB;

                for (int i = 0; i < bnColors; i++) {
                    cS = sColors[i];
                    cB = bColors[i];

                    if (isWhite) {
                        vv = cS;
                    } else {
                        switch (bm) {
                            case PdfDictionary.Multiply:
                                vv = CompositeUtil.blendMultiply(cS, cB);
                                break;
                            case PdfDictionary.Screen:
                                vv = CompositeUtil.blendScreen(cS, cB);
                                break;
                            case PdfDictionary.Overlay:
                                vv = CompositeUtil.blendOverLay(cS, cB);
                                break;
                            case PdfDictionary.Darken:
                                vv = CompositeUtil.blendDarken(cS, cB);
                                break;
                            case PdfDictionary.Lighten:
                                vv = CompositeUtil.blendLighten(cS, cB);
                                break;
                            case PdfDictionary.ColorDodge:
                                vv = CompositeUtil.blendColorDodge(cS, cB);
                                break;
                            case PdfDictionary.ColorBurn:
                                vv = CompositeUtil.blendColorBurn(cS, cB);
                                break;
                            case PdfDictionary.HardLight:
                                vv = CompositeUtil.blendHardLight(cS, cB);
                                break;
                            case PdfDictionary.SoftLight:
                                vv = CompositeUtil.blendSoftLight(cS, cB);
                                break;
                            case PdfDictionary.Difference:
                                vv = CompositeUtil.blendDifference(cS, cB);
                                break;
                            case PdfDictionary.Exclusion:
                                vv = CompositeUtil.blendExclusion(cS, cB);
                                break;
                        }
                    }

                    vv = CompositeUtil.composite(aS, aB, aR, cS, cB, vv);

                    bColors[i] = vv;

                }

                if (hasAlphaB) {
                    bColors[bnColors] = aR;
                }

                dstPixel = dstModel.getDataElements(bColors, 0, dstPixel);
                dstOut.setDataElements(x, y, dstPixel);

            }
        }
    }

    private static boolean isWhiteBackdrop(float[] bColors) {
        for (int i = 0; i < bColors.length; i++) {
            if (bColors[i] != 1) {
                return false;
            }
        }
        return true;
    }

}
