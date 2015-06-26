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
 * PdfShape.java
 * ---------------
 */
package org.jpedal.objects;

import java.awt.Shape;
import javafx.scene.shape.Path;

/**
 * allow us to have both Swing or javaFX implementations of Shape
 */
public interface PdfShape {

    public void setEVENODDWindingRule();

    public void setNONZEROWindingRule();

    public void closeShape();

    public Shape generateShapeFromPath(float[][] CTM, float lineWidth, int B, int type);

    public void setClip(boolean b);

    public void resetPath();

    public boolean isClip();

    public int getSegmentCount();

    public int getComplexClipCount();

    public void lineTo(float parseFloat, float parseFloat0);

    public void moveTo(float parseFloat, float parseFloat0);

    public void appendRectangle(float parseFloat, float parseFloat0, float parseFloat1, float parseFloat2);

    public void addBezierCurveC(float x, float y, float x2, float y2, float x3, float y3);

    public void addBezierCurveV(float parseFloat, float parseFloat0, float parseFloat1, float parseFloat2);

    public void addBezierCurveY(float parseFloat, float parseFloat0, float parseFloat1, float parseFloat2);

    public Path getPath();

    public boolean adjustLineWidth();

    public boolean isClosed();
}
