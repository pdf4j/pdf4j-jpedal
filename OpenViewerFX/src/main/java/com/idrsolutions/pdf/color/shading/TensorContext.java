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
 * TensorContext.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.function.PDFFunction;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author suda
 */
public class TensorContext implements PaintContext {

    private GenericColorSpace shadingColorSpace;
    private final float[] background;
    private int bitsPerCoordinate;
    private int bitsPerComponent;
    private int bitsPerFlag;
    private int colCompCount;
    private float[] decodeArr;
//    private final float[][] CTM;
    private ArrayList<Point2D> pp; //patch points
    private ArrayList<Color> pc; // patch colors
    private final ArrayList<Shape67> shapes;
    private BitReader reader;
    private PDFFunction[] function;
    private final boolean isRecursive;
    private AffineTransform invAffine = invAffine = new AffineTransform();

    /**
     * constructor uses cached shapes values to create a context
     *
     * @param shapes
     * @param background
     * @param pageHeight
     * @param scaling
     * @param offX
     * @param offY
     */
    TensorContext(final AffineTransform xform, final GenericColorSpace shadingColorSpace, final ArrayList<Shape67> shapes, final float[] background, float[][] mm, PDFFunction[] function) {

        this.shapes = shapes;
        this.background = background;
        this.shadingColorSpace = shadingColorSpace;
        isRecursive = shapes.size() < 50;

        AffineTransform shadeAffine = new AffineTransform();
        if (mm != null) {
            shadeAffine = new AffineTransform(mm[0][0], mm[0][1], mm[1][0], mm[1][1], mm[2][0], mm[2][1]);
        }

        try {
            AffineTransform invXF = xform.createInverse();
            AffineTransform invSH = shadeAffine.createInverse();
            invSH.concatenate(invXF);
            invAffine = (AffineTransform) invSH.clone();
        } catch (NoninvertibleTransformException ex) {
            LogWriter.writeLog("Exception " + ex + ' ');
        }
        this.function = function;

    }

    TensorContext(AffineTransform xform, final GenericColorSpace shadingColorSpace, final float[] background, final PdfObject shadingObject, float[][] mm, PDFFunction[] function) {
        this.shadingColorSpace = shadingColorSpace;
        this.background = background;
        bitsPerComponent = shadingObject.getInt(PdfDictionary.BitsPerComponent);
        bitsPerFlag = shadingObject.getInt(PdfDictionary.BitsPerFlag);
        bitsPerCoordinate = shadingObject.getInt(PdfDictionary.BitsPerCoordinate);
        decodeArr = shadingObject.getFloatArray(PdfDictionary.Decode);
        boolean hasSmallBits = bitsPerFlag < 8 || bitsPerComponent < 8 || bitsPerCoordinate < 8;
        reader = new BitReader(shadingObject.getDecodedStream(), hasSmallBits);
        colCompCount = shadingColorSpace.getColorComponentCount();
        if (decodeArr != null) {
            colCompCount = (decodeArr.length - 4) / 2;
        }

        AffineTransform shadeAffine = new AffineTransform();
        if (mm != null) {
            shadeAffine = new AffineTransform(mm[0][0], mm[0][1], mm[1][0], mm[1][1], mm[2][0], mm[2][1]);
        }

        try {
            AffineTransform invXF = xform.createInverse();
            AffineTransform invSH = shadeAffine.createInverse();
            invSH.concatenate(invXF);
            invAffine = (AffineTransform) invSH.clone();
        } catch (NoninvertibleTransformException ex) {
            LogWriter.writeLog("Exception " + ex + ' ');
        }

        this.function = function;

        pp = new ArrayList<Point2D>();
        pc = new ArrayList<Color>();
        shapes = new ArrayList<Shape67>();

        process();
        adjustPoints();

        isRecursive = shapes.size() < 50;

    }

    /**
     * process the datastream and update the variables
     */
    private void process() {
        while (reader.getPointer() < reader.getTotalBitLen()) {
            int flag = reader.getPositive(bitsPerFlag);
            Point2D a4[] = new Point2D[4];
            Color a2[] = new Color[2];
            float[] cc = new float[colCompCount];
            switch (flag) {
                case 0:
                    for (int i = 0; i < 12; i++) {
                        Point2D p = getPointCoords();
                        pp.add(p);
                    }

                    for (int i = 0; i < 4; i++) {
                        getPointCoords(); //ignore this data at the moment
                    }

                    for (int i = 0; i < 4; i++) {
                        for (int z = 0; z < colCompCount; z++) {
                            cc[z] = reader.getFloat(bitsPerComponent);
                        }
                        Color color = calculateColor(cc);
                        pc.add(color);
                    }
                    break;
                case 1:
                    a4[0] = pp.get(pp.size() - 9);
                    a4[1] = pp.get(pp.size() - 8);
                    a4[2] = pp.get(pp.size() - 7);
                    a4[3] = pp.get(pp.size() - 6);

                    pp.addAll(Arrays.asList(a4).subList(0, 4));
                    for (int i = 0; i < 8; i++) {
                        Point2D p = getPointCoords();
                        pp.add(p);
                    }
                    for (int i = 0; i < 4; i++) {
                        getPointCoords();
                    }
                    a2[0] = pc.get(pc.size() - 3);
                    a2[1] = pc.get(pc.size() - 2);

                    pc.addAll(Arrays.asList(a2));

                    for (int i = 0; i < 2; i++) {
                        for (int z = 0; z < colCompCount; z++) {
                            cc[z] = reader.getFloat(bitsPerComponent);
                        }
                        Color color = calculateColor(cc);
                        pc.add(color);
                    }

                    //do color mapping
                    break;
                case 2:
                    a4[0] = pp.get(pp.size() - 6);
                    a4[1] = pp.get(pp.size() - 5);
                    a4[2] = pp.get(pp.size() - 4);
                    a4[3] = pp.get(pp.size() - 3);

                    pp.addAll(Arrays.asList(a4).subList(0, 4));
                    for (int i = 0; i < 8; i++) {
                        Point2D p = getPointCoords();
                        pp.add(p);
                    }
                    for (int i = 0; i < 4; i++) {
                        getPointCoords();
                    }
                    a2[0] = pc.get(pc.size() - 2);
                    a2[1] = pc.get(pc.size() - 1);

                    pc.addAll(Arrays.asList(a2));

                    for (int i = 0; i < 2; i++) {
                        for (int z = 0; z < colCompCount; z++) {
                            cc[z] = reader.getFloat(bitsPerComponent);
                        }
                        Color color = calculateColor(cc);
                        pc.add(color);
                    }
                    // do color mapping                    

                    break;
                case 3:
                    a4[0] = pp.get(pp.size() - 3);
                    a4[1] = pp.get(pp.size() - 2);
                    a4[2] = pp.get(pp.size() - 1);
                    a4[3] = pp.get(pp.size() - 12);

                    pp.addAll(Arrays.asList(a4).subList(0, 4));
                    for (int i = 0; i < 8; i++) {
                        Point2D p = getPointCoords();
                        pp.add(p);
                    }
                    for (int i = 0; i < 4; i++) {
                        getPointCoords();
                    }

                    a2[0] = pc.get(pc.size() - 1);
                    a2[1] = pc.get(pc.size() - 4);

                    pc.addAll(Arrays.asList(a2));

                    for (int i = 0; i < 2; i++) {
                        for (int z = 0; z < colCompCount; z++) {
                            cc[z] = reader.getFloat(bitsPerComponent);
                        }
                        Color color = calculateColor(cc);
                        pc.add(color);
                    }
                    break;
            }
        }
    }

    /**
     * convert small points to x,y full space points
     */
    private void adjustPoints() {

        if (decodeArr != null) { //some odd files have decode array as null
            float xMin = decodeArr[0];
            float xMax = decodeArr[1];
            float yMin = decodeArr[2];
            float yMax = decodeArr[3];

            float xw = xMax - xMin;
            float yw = yMax - yMin;

            ArrayList<Point2D> tempPoints = new ArrayList<Point2D>();
            for (Point2D p : pp) {
                float xx = (float) p.getX();
                float yy = (float) p.getY();
                xx = (xw * xx) + xMin;
                yy = (yw * yy) + yMin;
                tempPoints.add(new Point2D.Float(xx, yy));
            }
            pp.clear();
            for (Point2D t : tempPoints) {
                pp.add(t);
            }
        }

        Point2D[] pArr = new Point2D[pp.size()];
        for (int i = 0; i < pArr.length; i++) {
            pArr[i] = pp.get(i);
        }
        int totalPatches = pp.size() / 12;
        int offset = 0;
        for (int i = 0; i < totalPatches; i++) {
            Point2D[] pointArr = new Point2D[12];
            Color[] colors = {pc.get(i * 4), pc.get(i * 4 + 1), pc.get(i * 4 + 2), pc.get(i * 4 + 3)};
            System.arraycopy(pArr, offset, pointArr, 0, 12);
            Shape67 sh = new Shape67(pointArr, colors);
            shapes.add(sh);
            offset += 12;
        }

    }

    private Point2D getPointCoords() {
        float x = 0;
        float y = 0;

        for (int z = 0; z < 2; z++) {
            switch (z) {
                case 0:
                    x = reader.getFloat(bitsPerCoordinate);
                    break;
                case 1:
                    y = reader.getFloat(bitsPerCoordinate);
                    break;
            }
        }
        return new Point2D.Float(x, y);
    }

    @Override
    public void dispose() {
        reader = null;
    }

    @Override
    public ColorModel getColorModel() {
        return ColorModel.getRGBdefault();
    }

    @Override
    public Raster getRaster(int xStart, int yStart, int w, int h) {

        final WritableRaster raster = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB).getRaster();
        int[] data = ((DataBufferInt) raster.getDataBuffer()).getData();

        if (background != null) {
            int pos = 0;
            shadingColorSpace.setColor(background, 4);
            final Color c = (Color) shadingColorSpace.getColor();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    data[pos++] = 255 << 24 | c.getRGB();
                }
            }
        }

        Rectangle rect = new Rectangle(xStart, yStart, w, h);
        Shape rr = invAffine.createTransformedShape(rect);
        Rectangle2D rect2D = rr.getBounds2D();

        List<Shape67> foundList = new ArrayList<Shape67>();
        for (Shape67 sh : shapes) {
            if (sh.getShape().intersects(rect2D)) {
                foundList.add(sh);
            }
        }
        float xx, yy;
        for (Shape67 sh : foundList) {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    float[] src = {x + xStart, y + yStart};
                    invAffine.transform(src, 0, src, 0, 1);
                    xx = src[0];
                    yy = src[1];
                    GeneralPath path = sh.getShape();
                    // check with bounds first before going to shape to speedup execution
                    if (path.contains(xx, yy)) {
                        Point2D p = new Point2D.Float(xx, yy);
                        Color result = sh.findPointColor(p, isRecursive);
                        if (result != null) {
                            final int base = (y * w + x);
                            data[base] = 255 << 24 | result.getRGB();
                        }
                    }
                }
            }
        }
        return raster;
    }

    private Color calculateColor(final float[] val) {
        final Color col;
        if (function == null) {
            shadingColorSpace.setColor(val, colCompCount);
            col = new Color(shadingColorSpace.getColor().getRGB());
        } else {
            final float[] colValues = ShadingFactory.applyFunctions(function, val);
            shadingColorSpace.setColor(colValues, colValues.length);
            col = (Color) shadingColorSpace.getColor();
        }
        return col;
    }

    public ArrayList<Shape67> getShapes() {
        return shapes;
    }

}
