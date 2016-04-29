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
 * PropertiesFile.java
 * ---------------
 */
package org.jpedal.examples.viewer.utils;

import java.awt.Color;
import java.util.StringTokenizer;
import org.jpedal.utils.LogWriter;

public class CSSToJavaValues {
    
    /**
     * Accepts a valid CSS1 color value and return a Color object.
     * @param cssValue A string representation of a valid CSS1 color string
     * @return A color object or null if string was null, empty or not valid css
     */
    public static Color convertToColor(String cssValue){
        Color col = null;

        if (cssValue != null && !cssValue.isEmpty()) {

            if (cssValue.startsWith("#")) {
                //Handle hex values
                col = Color.decode(cssValue);
            } else {
                if (cssValue.startsWith("rgb")) {
                    col = handleRGBColor(cssValue);
                } else {
                    col = handleNamedColor(cssValue.toLowerCase());
                }
            }
        }

        return col;
    }
    
    private static Color handleRGBColor(String cssValue){

        //Handle rgb values
        String rgbValues = cssValue.substring(cssValue.indexOf('(') + 1, cssValue.indexOf(')'));
        StringTokenizer rgbTokens = new StringTokenizer(rgbValues, " ,");

        int r, g, b;
        int a = 255;
        if (rgbValues.contains("%")) {
            r = (int) (255 * (Float.parseFloat(rgbTokens.nextToken().replace("%", "")) / 100));
            g = (int) (255 * (Float.parseFloat(rgbTokens.nextToken().replace("%", "")) / 100));
            b = (int) (255 * (Float.parseFloat(rgbTokens.nextToken().replace("%", "")) / 100));
            if (rgbTokens.hasMoreTokens()) {
                a = (int) (255 * Float.parseFloat(rgbTokens.nextToken()));
            }
            return new Color(r, g, b, a);
        } else {
            r = Integer.parseInt(rgbTokens.nextToken());
            g = Integer.parseInt(rgbTokens.nextToken());
            b = Integer.parseInt(rgbTokens.nextToken());
            if (rgbTokens.hasMoreTokens()) {
                a = (int) (255 * Float.parseFloat(rgbTokens.nextToken()));
            }
            return new Color(r, g, b, a);
        }
    }
    
    private static Color handleNamedColor(String cssValue) {
        
        switch (cssValue.charAt(0)) {
            case 'a':
                //aqua
                return new Color(0, 255, 255);
            case 'b':
                if (cssValue.charAt(2) == 'a') {
                    //black
                    return new Color(0, 0, 0);
                } else {
                    //blue
                    return new Color(0, 0, 255);
                }
            case 'f':
                //fuchisa
                return new Color(255, 0, 255);
            case 'g':
                if (cssValue.charAt(2) == 'a') {
                    //gray
                    return new Color(128, 128, 128);
                } else {
                    //green
                    return new Color(0, 128, 0);
                }
            case 'l':
                //lime
                return new Color(0, 255, 0);
            case 'm':
                //maroon
                return new Color(128, 0, 0);
            case 'n':
                //navy
                return new Color(0, 0, 128);
            case 'o':
                //olive
                return new Color(128, 128, 0);
            case 'p':
                //purple
                return new Color(128, 0, 128);
            case 'r':
                //red
                return new Color(255, 0, 0);
            case 's':
                //silver
                return new Color(192, 192, 192);
            case 't':
                //teal
                return new Color(0, 128, 128);
            case 'w':
                //white
                return new Color(255, 255, 255);
            case 'y':
                //yellow
                return new Color(255, 255, 0);
            default:
                LogWriter.writeLog("Unknown color name");
        }

        return null;
    }
}
