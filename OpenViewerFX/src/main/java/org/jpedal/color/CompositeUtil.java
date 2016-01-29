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
 * CompositeUtil.java
 * ---------------
 */
package org.jpedal.color;

public class CompositeUtil {

    public static float composite(float aS, float aB, float aR, float cS, float cB, float blendRes) {
        float ratioA = aS / aR;
        return (1 - ratioA) * cB + ratioA * ((1 - aB) * cS + aB * blendRes);
    }

    public static float blendNormal(float cS, float cB) {
        return cS;
    }

    public static float blendMultiply(float cS, float cB) {
        return cB * cS;
    }

    public static float blendScreen(float cS, float cB) {
        float kk = cB + cS - (cB * cS);
        return kk;
    }

    public static float blendOverLay(float cS, float cB) {
        return blendHardLight(cB, cS);
    }

    public static float blendDarken(float cS, float cB) {
        return Math.min(cS, cB);
    }

    public static float blendLighten(float cS, float cB) {
        return Math.max(cS, cB);
    }

    public static float blendColorDodge(float cS, float cB) {
        if (cS == 1) {
            return 1;
        } else {
            return Math.min(1, cB / (1 - cS));
        }
    }

    public static float blendColorBurn(float cS, float cB) {
        if (cS == 0) {
            return 0;
        } else {
            return 1f - Math.min(1, (1 - cB) / cS);
        }
    }

    public static float blendHardLight(float cS, float cB) {
        if (cS <= 0.5f) {
            return blendMultiply(cB, 2 * cS);
        } else {
            return blendScreen(cB, 2 * cS - 1);
        }
    }

    public static float blendSoftLight(float cS, float cB) {
        if (cS <= 0.5f) {
            return cB - (1 - 2 * cS) * cB * (1 - cB);
        } else {
            return cB + (2 * cS - 1) * (blendDX(cB) - cB);
        }
    }

    private static float blendDX(float x) {
        if (x <= 0.25f) {
            return ((16 * x - 12) * x + 4) * x;
        } else {
            return (float) Math.sqrt(x);
        }
    }

    public static float blendDifference(float cS, float cB) {
        return Math.abs(cB - cS);
    }

    public static float blendExclusion(float cS, float cB) {
        return cB + cS - 2 * cB * cS;
    }

}
