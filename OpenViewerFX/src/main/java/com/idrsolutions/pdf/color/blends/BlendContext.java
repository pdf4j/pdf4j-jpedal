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
 * BlendContext.java
 * ---------------
 */
package com.idrsolutions.pdf.color.blends;

import java.awt.CompositeContext;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.jpedal.objects.raw.PdfDictionary;

/**
 *
 */
public class BlendContext implements CompositeContext {

//    private final float alpha;
    private final int blendMode;

    public BlendContext(int blendMode) {
        this.blendMode = blendMode;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {

        int width = Math.min(src.getWidth(), dstIn.getWidth());
        int height = Math.min(src.getHeight(), dstIn.getHeight());

        int[] srcPixels = new int[width];
        int[] dstInPixels = new int[width];
        int[] dstOutPixels = new int[width];

        for (int y = 0; y < height; y++) {
            src.getDataElements(0, y, width, 1, srcPixels);
            dstIn.getDataElements(0, y, width, 1, dstInPixels);

            int oldS = 0;
            int oldD = 0;
            int oldR = 0;

            for (int x = 0; x < width; x++) {
                int s = srcPixels[x];
                int d = dstInPixels[x];

                if (s == oldS && d == oldD) {
                    dstOutPixels[x] = oldR;
                } else {
                    oldS = s;
                    oldD = d;
                    int[] sp = getRGBA(s);
                    int[] dp = getRGBA(d);
                    int[] result = new int[4];

//                    if (sp[3] != 255) { //transparency involved convert to rgb
////                        int r = sp[0] * sp[3] + dp[0] * (255 - sp[3]);
////                        int g = sp[1] * sp[3] + dp[1] * (255 - sp[3]);
////                        int b = sp[2] * sp[3] + dp[2] * (255 - sp[3]);
////                        sp = new int[]{r / 255, g / 255, b / 255, sp[3]};
////                        dp = new int[]{0,0,0,dp[3]};
//                        dp[3] = 255;
////                        sp[3] = 255;
////                        System.out.println(dp[0]+" "+dp[1]+" "+dp[2]+" "+dp[3]);
//                    }
                    switch (blendMode) {
                        case PdfDictionary.Normal:
                            break;                        
                        case PdfDictionary.Hue:
                            result = doHue(sp, dp);
                            break;
                        case PdfDictionary.Saturation:
                            result = doSaturation(sp, dp);
                            break;
                        case PdfDictionary.Color:
                            result = doColor(sp, dp);
                            break;
                        case PdfDictionary.Luminosity:
                            result = doLuminosity(sp, dp);
                            break;
                        default:
                            break;
                    }

                    if (sp[3] != 255) {
                        double sr = result[0] / 255.0;
                        double sg = result[1] / 255.0;
                        double sb = result[2] / 255.0;
                        double sa = sp[3] / 255.0;

                        double dr = dp[0] / 255.0;
                        double dg = dp[1] / 255.0;
                        double db = dp[2] / 255.0;

                        sr = ((1 - sa) * dr) + (sa * sr);
                        sg = ((1 - sa) * dg) + (sa * sg);
                        sb = ((1 - sa) * db) + (sa * sb);

                        result[0] = (int) (sr * 255);
                        result[1] = (int) (sg * 255);
                        result[2] = (int) (sb * 255);

                    }

                    dstOutPixels[x] = oldR = (Math.min(255, sp[3] + dp[3]) << 24 | result[0] << 16 | result[1] << 8 | result[2]);

                }
//                dstOutPixels[x] = sp[3] << 24 | sp[0] << 16 | sp[1] << 8 | sp[2];
//                
            }
            dstOut.setDataElements(0, y, width, 1, dstOutPixels);
        }
    }

    private static int[] getRGBA(int argb) {
        return new int[]{(argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff, (argb >> 24) & 0xff};
    }
   

    private static int[] doColor(int[] src, int[] dst) {
        
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }

        int[] result = new int[3];
        double sr = src[0] / 255.0;
        double sg = src[1] / 255.0;
        double sb = src[2] / 255.0;

        double dr = dst[0] / 255.0;
        double dg = dst[1] / 255.0;
        double db = dst[2] / 255.0;

        double[] rgb = setLum(sr, sg, sb, lum(dr, dg, db));

        result[0] = (int) (255 * rgb[0]);
        result[1] = (int) (255 * rgb[1]);
        result[2] = (int) (255 * rgb[2]);

        return result;
    }

    private static int[] doLuminosity(int[] src, int[] dst) {
                
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }

        int[] result = new int[3];
        double sr = src[0] / 255.0;
        double sg = src[1] / 255.0;
        double sb = src[2] / 255.0;

        double dr = dst[0] / 255.0;
        double dg = dst[1] / 255.0;
        double db = dst[2] / 255.0;

        double[] rgb = setLum(dr, dg, db, lum(sr, sg, sb));

        result[0] = (int) (255 * rgb[0]);
        result[1] = (int) (255 * rgb[1]);
        result[2] = (int) (255 * rgb[2]);

        return result;
    }

    private static int[] doHue(int[] src, int[] dst) {
                
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }
        
        double[] srcHSL = new double[3];
        rgbToHSL(src[0], src[1], src[2], srcHSL);
        double[] dstHSL = new double[3];
        rgbToHSL(dst[0], dst[1], dst[2], dstHSL);

        int[] result = new int[4];
        hslToRGB(srcHSL[0], dstHSL[1], dstHSL[2], result);

        return result;
    }

    private static int[] doSaturation(int[] src, int[] dst) {
                
        if (dst[0] == 255 && dst[1] == 255 && dst[2] == 255) {
            return new int[]{src[0], src[1], src[2]};
        }
        
        double[] srcHSL = new double[3];
        rgbToHSL(src[0], src[1], src[2], srcHSL);
        double[] dstHSL = new double[3];
        rgbToHSL(dst[0], dst[1], dst[2], dstHSL);

        int[] result = new int[4];
        hslToRGB(dstHSL[0], srcHSL[1], dstHSL[2], result);

        return result;
    }

    private static double lum(double r, double g, double b) {
        return 0.3 * r + 0.59 * g + 0.11 * b;
    }

    private static double[] setLum(double r, double g, double b, double l) {
        double d = l - lum(r, g, b);
        r += d;
        g += d;
        b += d;
        return clipColor(r, g, b);
    }

    private static double[] clipColor(double r, double g, double b) {
        double l = lum(r, g, b);
        double n = Math.min(Math.min(r, g), b);
        double x = Math.max(Math.max(r, g), b);
        if (n < 0.0) {
            r = l + (((r - l) * l) / (l - n));
            g = l + (((g - l) * l) / (l - n));
            b = l + (((b - l) * l) / (l - n));
        }
        if (x > 1.0) {
            r = l + (((r - l) * (1 - l)) / (x - l));
            g = l + (((g - l) * (1 - l)) / (x - l));
            b = l + (((b - l) * (1 - l)) / (x - l));
        }
        return new double[]{r, g, b};
    }

    private static void rgbToHSL(int r, int g, int b, double[] hsl) {
        double rr = (r / 255.0);
        double gg = (g / 255.0);
        double bb = (b / 255.0);

        double var_Min = Math.min(Math.min(rr, gg), bb);
        double var_Max = Math.max(Math.max(rr, gg), bb);
        double del_Max = var_Max - var_Min;

        double H, S, L;
        L = (var_Max + var_Min) / 2.0;

        if (del_Max - 0.01 <= 0.0) {
            H = 0;
            S = 0;
        } else {
            if (L < 0.5) {
                S = del_Max / (var_Max + var_Min);
            } else {
                S = del_Max / (2 - var_Max - var_Min);
            }

            double del_R = (((var_Max - rr) / 6.0) + (del_Max / 2.0)) / del_Max;
            double del_G = (((var_Max - gg) / 6.0) + (del_Max / 2.0)) / del_Max;
            double del_B = (((var_Max - bb) / 6.0) + (del_Max / 2.0)) / del_Max;

            if (rr == var_Max) {
                H = del_B - del_G;
            } else if (gg == var_Max) {
                H = (1 / 3f) + del_R - del_B;
            } else {
                H = (2 / 3f) + del_G - del_R;
            }
            if (H < 0) {
                H += 1;
            }
            if (H > 1) {
                H -= 1;
            }
        }

        hsl[0] = H;
        hsl[1] = S;
        hsl[2] = L;
    }

    private static void hslToRGB(double h, double s, double l, int[] rgb) {
        int R, G, B;

        if (s - 0.01 <= 0.0) {
            R = (int) (l * 255.0f);
            G = (int) (l * 255.0f);
            B = (int) (l * 255.0f);
        } else {
            double v1, v2;
            if (l < 0.5f) {
                v2 = l * (1 + s);
            } else {
                v2 = (l + s) - (s * l);
            }
            v1 = 2 * l - v2;

            R = (int) (255.0 * hueToRGB(v1, v2, h + (1.0 / 3.0)));
            G = (int) (255.0 * hueToRGB(v1, v2, h));
            B = (int) (255.0 * hueToRGB(v1, v2, h - (1.0 / 3.0)));
        }

        rgb[0] = R;
        rgb[1] = G;
        rgb[2] = B;
    }

    private static double hueToRGB(double v1, double v2, double vH) {
        if (vH < 0.0) {
            vH += 1.0;
        }
        if (vH > 1.0) {
            vH -= 1.0;
        }
        if ((6.0 * vH) < 1.0) {
            return (v1 + (v2 - v1) * 6.0 * vH);
        }
        if ((2.0 * vH) < 1.0) {
            return v2;
        }
        if ((3.0 * vH) < 2.0) {
            return (v1 + (v2 - v1) * ((2.0 / 3.0) - vH) * 6.0);
        }
        return v1;
    }

}
