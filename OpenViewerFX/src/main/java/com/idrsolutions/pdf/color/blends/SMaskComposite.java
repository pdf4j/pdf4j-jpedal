/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.idrsolutions.pdf.color.blends;

import java.awt.CompositeContext;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 *
 * @author suda
 */
public class SMaskComposite implements CompositeContext {

    private final float fixedAlpha;
    private final ColorModel srcModel;
    private final ColorModel dstModel;

    public SMaskComposite(final ColorModel srcColorModel, final ColorModel dstColorModel, float alpha) {
        srcModel = srcColorModel;
        dstModel = dstColorModel;
        fixedAlpha = alpha;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {

        int snComp = srcModel.getNumComponents();
        int bnComp = dstModel.getNumComponents();
        int bnColors = dstModel.getNumColorComponents();

        int width = Math.min(Math.min(src.getWidth(), dstIn.getWidth()), dstOut.getWidth());
        int height = Math.min(Math.min(src.getHeight(), dstIn.getHeight()), dstOut.getHeight());

        float[] sColors = new float[snComp]; //src colors
        float[] bColors = new float[bnComp]; //backdrop colors

        boolean hasAlphaB = dstModel.hasAlpha();

        Object srcPixel = null, dstPixel = null;

        float aB, aR, cB, qS, qM;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                dstPixel = dstIn.getDataElements(x, y, dstPixel);
                bColors = dstModel.getNormalizedComponents(dstPixel, bColors, 0);

                srcPixel = src.getDataElements(x, y, srcPixel);
                sColors = srcModel.getNormalizedComponents(srcPixel, sColors, 0);

                qM = sColors[0];
                qS = qM * fixedAlpha;

                aB = hasAlphaB ? bColors[bnColors] : 1f;
                aR = 0;

                if (aB != 0) {
                    aR =  aB + qS - (aB * qS);
                    for (int i = 0; i < bnColors; i++) {
                        cB = bColors[i];
                        bColors[i] = cB + qS - (qS * cB);
                    }
                }
                
                if (hasAlphaB) {
                    bColors[bnColors] = aR;
                }

                dstPixel = dstModel.getDataElements(bColors, 0, dstPixel);
                dstOut.setDataElements(x, y, dstPixel);

            }
        }
    }

}
