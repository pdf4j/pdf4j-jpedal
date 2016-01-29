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
 * FloatMatrix.java
 * ---------------
 */
package org.jpedal.utils;

import java.awt.geom.AffineTransform;


public class FloatMatrix {

    public static final float[][] multiply(final float[][] m1, final float[][] m2) {
        final float[][] result = new float[3][3];
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                result[row][col] = (m1[row][0] * m2[0][col]) + (m1[row][1] * m2[1][col]) + (m1[row][2] * m2[2][col]);
            }
        }
        return result;
    }

    public static final float[][] inverse(final float[][] inp) {

        float d = (inp[2][0] * inp[0][1] * inp[1][2] - inp[2][0] * inp[0][2] * inp[1][1] - inp[1][0] * inp[0][1] * inp[2][2] + inp[1][0] * inp[0][2] * inp[2][1] + inp[0][0] * inp[1][1] * inp[2][2] - inp[0][0] * inp[1][2] * inp[2][1]);
        float t00 = (inp[1][1] * inp[2][2] - inp[1][2] * inp[2][1]) / d;
        float t01 = -(inp[0][1] * inp[2][2] - inp[0][2] * inp[2][1]) / d;
        float t02 = (inp[0][1] * inp[1][2] - inp[0][2] * inp[1][1]) / d;
        float t10 = -(-inp[2][0] * inp[1][2] + inp[1][0] * inp[2][2]) / d;
        float t11 = (-inp[2][0] * inp[0][2] + inp[0][0] * inp[2][2]) / d;
        float t12 = -(-inp[1][0] * inp[0][2] + inp[0][0] * inp[1][2]) / d;
        float t20 = (-inp[2][0] * inp[1][1] + inp[1][0] * inp[2][1]) / d;
        float t21 = -(-inp[2][0] * inp[0][1] + inp[0][0] * inp[2][1]) / d;
        float t22 = (-inp[1][0] * inp[0][1] + inp[0][0] * inp[1][1]) / d;

        float[][] output = new float[3][3];

        output[0][0] = t00;
        output[0][1] = t01;
        output[0][2] = t02;
        output[1][0] = t10;
        output[1][1] = t11;
        output[1][2] = t12;
        output[2][0] = t20;
        output[2][1] = t21;
        output[2][2] = t22;

        return output;
    }
    
    public static AffineTransform toAffine(float[][] mm){
       return new AffineTransform(mm[0][0], mm[0][1], mm[1][0], mm[1][1], mm[0][2], mm[1][2]);
    }
    
    public static float[][] toMatrix(AffineTransform at){
        return new float[][]{
            {(float)at.getScaleX(),(float)at.getShearX(),(float)at.getTranslateX()},
            {(float)at.getShearY(),(float)at.getScaleY(),(float)at.getTranslateY()},
            {0,0,1}
        };
    }
    
    public static final void show(final float[][] matrix1) {
        System.out.println("[...............................");
        for (int row = 0; row < 3; row++) {
            System.out.println( "\t[ " + matrix1[row][0] + " , " + matrix1[row][1] + " , " + matrix1[row][2] + " ]" );
        }
        System.out.println("...............................]");
    }
    
    public static void main(String[] args) {
        float[][] matrix1 = {{10,0,5},{0,10,5},{0,0,1}};
        float[][] matrix2 = {{1,0,100},{0,1,150},{0,0,1}};
        AffineTransform a1 = toAffine(matrix1);
        AffineTransform a2 = toAffine(matrix2);
        
        a1.concatenate(a2);
        System.out.println(a1);
//        show(matrix);
    }
    
}
