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
 * JavaFXPathSerializer.java
 * ---------------
 */
package org.jpedal.io.javafx;

import com.sun.javafx.geom.PathIterator;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;

public class JavaFXPathSerializer {

    
    public static void serializePathFromArray(final ObjectOutput os, final double[] arr, final FillRule windingRule) throws IOException {
        
        if(windingRule.equals(FillRule.EVEN_ODD)){
            os.write(0);
        }else if(windingRule.equals(FillRule.NON_ZERO)){
            os.write(1);
        }
        
        os.writeObject(arr);
        
    }
    
    /**
     * method to serialize a path. This method will iterate through the iterator
     * passed in, and write out the base components of the path to the
     * ObjectOutput
     *
     * @param os - ObjectOutput to write to
     * @param pi - PathIterator path components to iterate over
     * @throws IOException
     */
    public static void serializePath(final ObjectOutput os, final PathIterator pi) throws IOException {

        os.writeObject(pi.getWindingRule());

        final List list = new ArrayList();

        while (!pi.isDone()) {
            final float[] array = new float[6];
            final int type = pi.currentSegment(array);

            /**
             * add details of each segment to the list
             */
            list.add(type);
            list.add(array);

            pi.next();
        }

        /**
         * writes out the list which contains all the components of the path
         */
        os.writeObject(list);

    }

    /**
     * method to deserialize a path from an ObjectInput.
     *
     * @param os - ObjectInput that contains the serilized path
     * @return - the deserialize GeneralPath
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static Path deserializePath(final ObjectInput os) throws ClassNotFoundException, IOException {

        /**
         * deserialize the windingRule for the path
         */
        final Integer windingRule = (Integer) os.readObject();
        if (windingRule == null) {
            return null;
        }

        /**
         * deserialize the list which contains all the raw path data
         */
        final List list = (List) os.readObject();

        final Path path = new Path();
        
        /**
         * This code needs testing.
         */
        if(windingRule.equals(FillRule.EVEN_ODD)){
            path.setFillRule(FillRule.EVEN_ODD);
        }else if(windingRule.equals(FillRule.NON_ZERO)){
            path.setFillRule(FillRule.NON_ZERO);
        }

        /**
         * iterate over the list, and rebuild the path from the individual
         * movements stored inside the list
         */
        for (final Iterator iter = list.iterator(); iter.hasNext();) {
            final int pathType = (Integer) iter.next();
            final float[] array = (float[]) iter.next();

            switch (pathType) {
                case PathIterator.SEG_LINETO:
                    path.getElements().add(new LineTo(array[0], array[1]));
                    break;
                case PathIterator.SEG_MOVETO:
                    path.getElements().add(new MoveTo(array[0], array[1]));
                    break;
                case PathIterator.SEG_QUADTO:
                    path.getElements().add(new QuadCurveTo(array[0], array[1], array[2], array[3]));
                    break;
                case PathIterator.SEG_CUBICTO:
                    path.getElements().add(new CubicCurveTo(array[0], array[1], array[2], array[3], array[4], array[5]));
                    break;
                case PathIterator.SEG_CLOSE:
                    path.getElements().add(new ClosePath());
                    break;
                default:
                    System.out.println("unrecognized general path type");
                    break;
            }
        }

        /**
         * return the rebuilt path
         */
        return path;
    }

}
