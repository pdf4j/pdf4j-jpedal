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
 * Shape67.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to store shading type 6 and shading type 7 shape points and
 * perform memory efficient and speed execution
 *
 * @author suda
 */
@SuppressWarnings("ALL")
public class Shape67 {

    private final GeneralPath shape;
    private final Color colorsArr[];
    private final Point2D pointsArr[];
    private int nSteps = 10; // used to find c1,c2,d1,d2 length steps
    private final List<TinyPatch> patches = new ArrayList<TinyPatch>();
    private TinyPatch lastFound; //cache last found to

    /**
     * @param sp size 12 points array
     * @param colors size 4 color array
     *
     */
    public Shape67(final Point2D[] sp, final Color[] colors) {
        this.pointsArr = sp;
        this.colorsArr = colors;
        shape = new GeneralPath();
        shape.moveTo(sp[0].getX(), sp[0].getY());
        shape.curveTo(sp[1].getX(), sp[1].getY(), sp[2].getX(), sp[2].getY(), sp[3].getX(), sp[3].getY());
        shape.curveTo(sp[4].getX(), sp[4].getY(), sp[5].getX(), sp[5].getY(), sp[6].getX(), sp[6].getY());
        shape.curveTo(sp[7].getX(), sp[7].getY(), sp[8].getX(), sp[8].getY(), sp[9].getX(), sp[9].getY());
        shape.curveTo(sp[10].getX(), sp[10].getY(), sp[11].getX(), sp[11].getY(), sp[0].getX(), sp[0].getY());
        shape.closePath();
    }

    public GeneralPath getShape() {
        return shape;
    }

    public Point2D[] getPointsArray() {
        return pointsArr;
    }

   

//    private double getC1Length() {
//        return curveLength(pointsArr[9], pointsArr[10], pointsArr[11], pointsArr[0], nSteps);
//    }
//    private double getC2Length() {
//        return curveLength(pointsArr[3], pointsArr[4], pointsArr[5], pointsArr[6], nSteps);
//    }
//    private double getD1Length() {
//        return curveLength(pointsArr[0], pointsArr[1], pointsArr[2], pointsArr[3], nSteps);
//    }
//    private double getD2Length() {
//        return curveLength(pointsArr[6], pointsArr[7], pointsArr[8], pointsArr[9], nSteps);
//    }
//    private static double curveLength(Point2D p1, Point2D p2, Point2D p3, Point2D p4, int nSteps) {
//        double distance = 0;
//        Point2D oldPoint = p1;
//        for (int i = 1; i <= nSteps; i++) {
//            float t = (1.0f / nSteps) * i;
//            Point2D p = ShadingUtils.findDistancedPoint(t, p1, p2, p3, p4);
//            distance += oldPoint.distance(p);
//            oldPoint = p;
//        }
//        return distance;
//    }
    private static Point2D[] curvePoints(Point2D p1, Point2D p2, Point2D p3, Point2D p4, int nSteps) {
        Point2D[] arr = new Point2D[nSteps + 1];
        arr[0] = p1;
        for (int i = 1; i <= nSteps; i++) {
            float t = (1.0f / nSteps) * i;
            Point2D p = ShadingUtils.findDistancedPoint(t, p1, p2, p3, p4);
            arr[i] = p;
        }
        return arr;
    }

    private static float[][] linePoints(Point2D p1, Point2D p2) {       
        float[][] arr = new float[3][2];
        arr[0][0] = (float) p1.getX();
        arr[0][1] = (float) p1.getY();

        arr[1][0] = (float) (p1.getX() + p2.getX()) / 2;
        arr[1][1] = (float) (p1.getY() + p2.getY()) / 2;

        arr[2][0] = (float) p2.getX();
        arr[2][1] = (float) p2.getY();
        return arr;
    }

    private Point2D[] getC1Points() {
        return curvePoints(pointsArr[0], pointsArr[11], pointsArr[10], pointsArr[9], nSteps);
    }

    private Point2D[] getC2Points() {
        return curvePoints(pointsArr[3], pointsArr[4], pointsArr[5], pointsArr[6], nSteps);
    }

    private Point2D[] getD1Points() {
        return curvePoints(pointsArr[0], pointsArr[1], pointsArr[2], pointsArr[3], nSteps);
    }

    private Point2D[] getD2Points() {
        return curvePoints(pointsArr[9], pointsArr[8], pointsArr[7], pointsArr[6], nSteps);
    }

    public void generateBilinearMapping() {

        final Point2D[] C1 = getC1Points();
        final Point2D[] C2 = getC2Points();
        final Point2D[] D1 = getD1Points();
        final Point2D[] D2 = getD2Points();

        final int szu = C1.length;
        final int szv = D1.length;

        Point2D[][] xy = new Point2D[szv][szu];
        Color[][] cc = new Color[szv][szu];

        float stepV = 1f / (szv - 1);
        float stepU = 1f / (szu - 1);
        float v = -stepV;
        int[][] pointColors = new int[4][4];
        for (int i = 0; i < 4; i++) {
            pointColors[i] = new int[]{colorsArr[i].getRed(), colorsArr[i].getGreen(), colorsArr[i].getBlue(), colorsArr[i].getAlpha()};
        }

        float vMinus, uMinus, scx, scy, sdx, sdy, sbx, sby;

        for (int i = 0; i < szv; i++) {
            v += stepV;
            vMinus = 1 - v;
            float u = -stepU;
            for (int j = 0; j < szu; j++) {
                u += stepU;
                uMinus = 1 - u;
                scx = (float) (vMinus * C1[j].getX() + v * C2[j].getX());
                scy = (float) (vMinus * C1[j].getY() + v * C2[j].getY());

                sdx = (float) (uMinus * D1[i].getX() + u * D2[i].getX());
                sdy = (float) (uMinus * D1[i].getY() + u * D2[i].getY());

                sbx = (float) (vMinus * (uMinus * C1[0].getX() + u * C1[C1.length - 1].getX())
                        + v * (uMinus * C2[0].getX() + u * C2[C2.length - 1].getX()));
                sby = (float) (vMinus * ((1 - u) * C1[0].getY() + u * C1[C1.length - 1].getY())
                        + v * (uMinus * C2[0].getY() + u * C2[C2.length - 1].getY()));

                float sx = scx + sdx - sbx;
                float sy = scy + sdy - sby;

                xy[i][j] = new Point2D.Float(sx, sy);

                int[] temp = new int[4];
                for (int ci = 0; ci < 4; ci++) {
                    temp[ci] = (int) (vMinus * (uMinus * pointColors[0][ci] + u * pointColors[3][ci])
                            + v * (uMinus * pointColors[1][ci] + u * pointColors[2][ci]));
                }
                cc[i][j] = new Color(temp[0], temp[1], temp[2], temp[3]);

            }
        }

        int d = xy.length - 1; //dimensional length we consider as equal

        for (int i = 0; i < d; i++) {
            for (int j = 0; j < d; j++) {
                Point2D[] patchPoints = {xy[i][j], xy[i][j + 1], xy[i + 1][j + 1], xy[i + 1][j]};
                Color[] patchColors = {cc[i][j], cc[i][j + 1], cc[i + 1][j + 1], cc[i + 1][j]};
                patches.add(new TinyPatch(patchPoints, patchColors));
            }
        }
    }

    public Color findPointColor(Point2D p, boolean isRecursive) {

        if (patches.isEmpty()) {
            generateBilinearMapping();
        }
        Color pColor = null;
        if (lastFound != null && lastFound.path.contains(p)) {
            Color[] colors = lastFound.colors;
            Point2D[] points = lastFound.points;
            return recurseTrapezoidal(p, points, colors, isRecursive, 0);
        }

        for (TinyPatch patch : patches) {
            if (patch.path.contains(p)) {
                lastFound = patch;
                Color[] colors = patch.colors;
                Point2D[] points = patch.points;
                return recurseTrapezoidal(p, points, colors, isRecursive, 0);
            }
            lastFound = null;
        }
        return pColor;
    }

    private static Color recurseTrapezoidal(Point2D p, Point2D[] points, Color[] colors, boolean isRecursive, int depth) {

        if (depth > 2 || !isRecursive) {
            return colors[0];
        }

        float[][] C1 = linePoints(points[0], points[3]);
        float[][] C2 = linePoints(points[1], points[2]);
        float[][] D1 = linePoints(points[0], points[1]);
        float[][] D2 = linePoints(points[3], points[2]);

        final int szu = C1.length;
        final int szv = D1.length;

        Point2D[][] xy = new Point2D[szv][szu];
        Color[][] cc = new Color[szv][szu];

        float stepV = 1f / (szv - 1);
        float stepU = 1f / (szu - 1);
        float v = -stepV;
        int[][] pColors = new int[4][4];
        for (int i = 0; i < 4; i++) {
            pColors[i] = new int[]{colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), colors[i].getAlpha()};
        }

        float vMinus, uMinus, scx, scy, sdx, sdy, sbx, sby, u, sx, sy;

        for (int i = 0; i < szv; i++) {
            v += stepV;
            vMinus = 1 - v;
            u = -stepU;
            for (int j = 0; j < szu; j++) {
                u += stepU;
                uMinus = 1 - u;
                scx = vMinus * C1[j][0] + v * C2[j][0];
                scy = vMinus * C1[j][1] + v * C2[j][1];

                sdx = uMinus * D1[i][0] + u * D2[i][0];
                sdy = uMinus * D1[i][1] + u * D2[i][1];

                sbx = vMinus * (uMinus * C1[0][0] + u * C1[2][0]) + v * (uMinus * C2[0][0] + u * C2[2][0]);
                sby = vMinus * (uMinus * C1[0][1] + u * C1[2][1]) + v * (uMinus * C2[0][1] + u * C2[2][1]);

                sx = scx + sdx - sbx;
                sy = scy + sdy - sby;

                xy[i][j] = new Point2D.Float(sx, sy);

                int[] temp = new int[4];
                for (int ci = 0; ci < 4; ci++) {
                    temp[ci] = (int) (vMinus * (uMinus * pColors[0][ci] + u * pColors[3][ci])
                            + v * (uMinus * pColors[1][ci] + u * pColors[2][ci]));
                }
                cc[i][j] = new Color(temp[0], temp[1], temp[2], temp[3]);

            }
        }

        int d = xy.length - 1; //dimensional length we consider as equal

        for (int i = 0; i < d; i++) {
            for (int j = 0; j < d; j++) {
                Point2D[] patchPoints = {xy[i][j], xy[i][j + 1], xy[i + 1][j + 1], xy[i + 1][j]};
                Color[] patchColors = {cc[i][j], cc[i][j + 1], cc[i + 1][j + 1], cc[i + 1][j]};
                TinyPatch tiny = new TinyPatch(patchPoints, patchColors);
                if (tiny.path.contains(p)) {
                    depth++;
                    return recurseTrapezoidal(p, patchPoints, patchColors, isRecursive, depth);
                }
            }
        }

        return colors[0];

    }


    private static class TinyPatch {

        final GeneralPath path;
        final Color[] colors;
        final Point2D[] points;

        public TinyPatch(Point2D[] points, Color[] colors) {

            this.colors = colors;
            this.points = points;

            this.path = new GeneralPath();
            path.moveTo(points[0].getX(), points[0].getY());
            path.lineTo(points[1].getX(), points[1].getY());
            path.lineTo(points[2].getX(), points[2].getY());
            path.lineTo(points[3].getX(), points[3].getY());
            path.closePath();
        }

    }
    
    public void setSteps(int steps){
        nSteps = steps;
    }

}
