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
 * TextBlock.java
 * ---------------
 */

package org.jpedal.render.output;

import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.utils.Matrix;

public class TextBlock {

    private final float startXCoord, startYCoord;
    private float lastXUsed, lastYUsed;
    
    private float expectedXCoord;
    private float stringWidth;
    private float lastGlyphWidth;
    private String lastGlyf;

    private int lastTokenNumber;

    //Fields for separating dot trails from other text ........................
    private int dotCount;
    private boolean allowDotTrails = true;

    private boolean textOverDrawn;

    //Font metrics
    private final FontMapper fontMapper;
    private final float spaceWidth;
    private float charSpacing;
    private float wordSpacing;
    private float kerning;

    private int numSpaces;
    private int numChars = 1;

    private final StringBuilder text;
    private final int textColRGB;
    private int overDrawcolor;

    private final float[][] Trm;

    // The type of text e.g. GraphicsState.INVISIBLE
    private final int renderType;

    private boolean isComplex;
    private final boolean isSVG;
    private final boolean separateToWords;
    private final boolean convertSpacesTonbsp;

    public TextBlock(final String glyf, final int color,
                     final float spaceWidth, final float[][] Trm, final float charSpacing, final float wordSpacing,
                     final int renderType, final boolean isSVG, final boolean separateToWords,
                     final boolean convertSpacesTonbsp, final float glyphWidth, final FontMapper fontMapper,
                     final int currentTokenNumber) {

        text = new StringBuilder(glyf);

        this.Trm = Trm;
        final float xScale = getScaleX(Trm);

        expectedXCoord = Trm[2][0] + (glyphWidth * xScale);
//        expectedYCoord = Trm[2][1];

        stringWidth = glyphWidth * xScale;
        lastGlyphWidth = glyphWidth;
        lastGlyf = glyf;

        this.fontMapper = fontMapper;

        textColRGB = color;
        this.spaceWidth = spaceWidth * xScale;
        this.charSpacing = charSpacing * xScale;
        this.wordSpacing = wordSpacing * xScale;

        startXCoord = Trm[2][0];
        startYCoord = Trm[2][1];
        lastXUsed = Trm[2][0];
        lastYUsed = Trm[2][1];

        this.renderType = renderType;
        this.isSVG = isSVG;
        this.separateToWords = separateToWords;
        this.convertSpacesTonbsp = convertSpacesTonbsp;
        this.lastTokenNumber = currentTokenNumber;
    }

    public boolean isEmpty() {
        return text.length() == 0;
    }

    public float getWidth() {

        // If text is rotated then we need to use pythagoras' theorem to find the width of the
        // transformed text block. Otherwise, we can use standard width calculation which is faster.

        if (isComplex) {
            // Use pythagoras theorem to find the width of the rotated text block
            float lastGlyphX = 0;
            float lastGlyphY = 0;

            // Calculate scales
            final float scaleX = (float)Math.sqrt((Trm[0][0] * Trm[0][0]) + (Trm[0][1] * Trm[0][1]));
            final float scaleY = (float)Math.sqrt((Trm[1][0] * Trm[1][0]) + (Trm[1][1] * Trm[1][1]));
            final float hScale = scaleY / scaleX;// Is it possible there's a bug here when text is rotated and skewed?

            // We only have the x & y coordinates of the bottom left of the text block
            // and of the bottom left of the last glyph. Therefore we need to find the
            // coordinate of the bottom right of the last glyph to accurately calculate
            // the length of the text block.
            if (!lastGlyf.equals(" ")) {
                float[][] lastGlyf = {{1,0,0},{0,1,0},{lastGlyphWidth,0,1}};
                final float[][] rotation = {{Trm[0][0],Trm[0][1],0},{Trm[1][0],Trm[1][1],0},{0,0,1}};

                // Rotate the glyph to find the X & Y coord of the bottom right
                lastGlyf = Matrix.multiply(lastGlyf, rotation);
                lastGlyphX = lastGlyf[2][0];
                lastGlyphY = lastGlyf[2][1];
            }

            float x = Math.abs(lastXUsed - startXCoord);
            x += Math.abs(lastGlyphX);
            x *= x;
            float y = Math.abs(lastYUsed - startYCoord);
            y += Math.abs(lastGlyphY);
            y *= y;

            return (float)Math.sqrt(x + y) * hScale;
        } else {
            if (lastGlyf.equals(" ")) {
                return stringWidth - (lastGlyphWidth * Trm[0][0]);
            } else {
                return stringWidth;
            }
        }
    }

    private static float getScaleX(float[][] Trm) {
        return (float)Math.sqrt((Trm[0][0] * Trm[0][0]) + (Trm[0][1] * Trm[0][1]));
    }

    private static float getFontSize(final float[][] Trm) {
        if(Trm[0][1] != 0 || Trm[1][0] != 0 || Trm[0][0] < 0 || Trm[1][1] < 0) {
            return (float)Math.sqrt((Trm[1][0] * Trm[1][0]) + (Trm[1][1] * Trm[1][1]));// scaleY
        }
        return Trm[0][0];
    }

    public float getFontSize() {
        return getFontSize(Trm);
    }

    public float[][] getTrm() {
        return Trm;
    }

    public int getLastX() {
        return (int) lastXUsed; // TODO (Leon) This method should not exist.
    }

    public int getLastY() {
        return (int) lastYUsed; // TODO (Leon) This method should not exist.
    }

    public int getColor() {
        //if we have drawn red text over grey we want to use red not grey
        if(textOverDrawn) {
            return overDrawcolor;
        } else {
            return textColRGB;
        }
    }

    /**
     * @return output string
     */
    public String getOutputString(final boolean isOutput) {
        String result = text.toString();

        if(isOutput && OutputDisplay.Helper!=null) {
            result=OutputDisplay.Helper.tidyText(result);

            if(convertSpacesTonbsp) {
                result = result.replaceAll(" ", "&nbsp;");
            }
        }

        return result;
    }

    public float getCharSpacing() {
        return charSpacing/numChars;
    }

    public float getWordSpacing() {
        if (numSpaces == 0) {
            return 0;
        }
        return wordSpacing/(numSpaces+1);
    }

    public float getKerning() {
        return kerning / numChars;
    }

    public int getNumSpaces() {
        return numSpaces;
    }

    public boolean appendText(final String glyf, final int currentTokenNumber, float kerning, float wordSpacing,
                              float charSpacing, final int textFillType, final int color, final float[][] newTrm,
                              final float glyphWidth, final FontMapper fontMapper) {

        final float yScale = getFontSize();
        final float xScale = getScaleX(Trm);

        final float charWidth  = glyphWidth * xScale;
        kerning *= xScale;
        charSpacing *= xScale;
        wordSpacing *= xScale;

        //System.out.println(text + " >" + glyf + "< " + charWidth + " " + kerning + " " + charSpacing + " " + wordSpacing);

        final boolean isOCRText = textFillType == GraphicsState.INVISIBLE;

        if (yScale != getFontSize(newTrm) && (!isOCRText || yScale > 12)) {
            return false; // Potential for an optimisation here - have an allowed tolerance on the font size
        }

        // Reject duplicate text used to create text bold by creating slighlty offset second character
        // This code also has the effect of removing any duplicate characters
        if(!fontMapper.isFontSubstituted() &&
                Trm[0][0] == newTrm[0][0] && Trm[0][1] == newTrm[0][1] && Trm[1][0] == newTrm[1][0] && Trm[1][1] == newTrm[1][1]
                && glyf.equals(lastGlyf)){

            //work out absolute diffs
            final float xDiff = Math.abs(lastXUsed - newTrm[2][0]);
            final float yDiff = Math.abs(lastYUsed - newTrm[2][1]);

            //if does not look like new char, ignore
            final float fontDiffAllowed = 1;
            if(xDiff < fontDiffAllowed && yDiff < fontDiffAllowed){
                setOverDrawColor(color);//if we have drawn red text over grey we want to use red not grey
                return true;
            }
        }

        final boolean newTJ = currentTokenNumber != lastTokenNumber;
        lastTokenNumber = currentTokenNumber;
        if(!newTJ && glyf.equals(" ") && lastGlyf.equals(" ")){
            return false; // ignore blocks of multiple spaces
        }

        if (!this.fontMapper.equals(fontMapper)) {
            return false;
        }

        if (textColRGB != color) {
            return false;
        }

        if (separateToWords && dotCount > 5) {
            return false;
        }

        if (Math.abs(charSpacing - getCharSpacing()) >= 10) {
            return false; // If the charSpacing is significantly different now, split the divs.
        }

        if (lastGlyf.equals(" ") && kerning > 5) {
            return false;
        }

        if (charSpacing > 14 && (kerning > 1.6f || kerning < -2)) {
            return false;// See Case 15156 - Fixes text in tables positioned in a really crazy way
        }

        if (dotCount > 3 && glyf.charAt(0) != '.') {
            return false;
        }

        if (!allowDotTrails && lastGlyf.charAt(0) == '.' && glyf.charAt(0) == '.') {
            return false;
        }

        final float x = newTrm[2][0];
        final float y = newTrm[2][1];

        /**
         * workout X and Y change compared to last glyf
         */
        float lineYChange = lastYUsed - y;

        //ignore tiny changes
        if(Math.abs(lineYChange)<1.0f){
            lineYChange = 0f;
        }

        float lineXChange = x - expectedXCoord;
        if (!isSVG) {
            lineXChange -= charSpacing;//We write out the charSpacing in HTML but not SVG, so this adjusts the calculations to account for that.
        }

        final float lineXChangeUnaltered = lineXChange;

        //Allow for slight variation
        if(lineXChange < 0 && lineXChange > -charWidth) {
            lineXChange = 0;
        }

        isComplex = Trm[0][1] != 0 || Trm[1][0] != 0 || Trm[0][0] < 0 || Trm[1][1] < 0;
        //usual rotated case
        if (isComplex){

            //Specifically for 17213 (general-May2014/Pages from SVA-ALL-SLM_NoRestriction.pdf)
            if (glyf.charAt(0) == 8195) {
                return false;
            }

            if (newTJ) {

                float estimatedYDiff = (lastYUsed - y) / yScale;
                if (estimatedYDiff < 0) {
                    estimatedYDiff = -estimatedYDiff;
                }

                float estimatedXDiff = (lastXUsed - x) / yScale; // Unclear why this is not xScale
                if (estimatedXDiff < 0) {
                    estimatedXDiff = -estimatedXDiff;
                }

                //avoid big gap as probably cursor moving
                if (estimatedYDiff > 1 || estimatedXDiff > 1) {
                    return false;
                }

//                System.out.println(glyf + ": estXDiff: " + estimatedXDiff +  " expXDiff: " + ((expectedXCoord - x) / yScale));
//                System.out.println(glyf + ": estYDiff: " + estimatedYDiff +  " expYDiff: " + ((expectedYCoord - y) / yScale));


                if (Trm[0][0] != 0 || Trm[1][1] != 0 || (startXCoord != x)) {
                    if (lineYChange != 0 || lineXChange < -1.5f) {//costena space fix. Commenting this out doesn't affect costena now...
                        return false;
                    }
                }

                lineXChange = 0;
            } else {
                //System.out.println(kerning/fontSize + " " + text);

                // Temporary fix for Pages from SVA-ALL-SLM_NoRestriction/index.html
                if (kerning / yScale > 0.8f) {
                    return false;
                }

                lineXChange = kerning;
                //lineXChangeUnaltered = kerning;
            }

        } else {
            // Pass any lost accuracy in as kerning. Only use for simple untransformed text (otherwise lineXChange is inaccurate)
            kerning = lineXChangeUnaltered;

            if (lastGlyf.equals(" ")) {
                kerning = 0;//Let the wordspacing adjustment pick this up otherwise text will have incorrect spacing
            }

            //note duplication above if we ever change
            if((lineYChange != 0 || lineXChange < -1.5f)) {
                return false;
            }
        }

        /**
         * insert any spaces due to large gaps between text
         */
        float spaceCountExact = lineXChange / spaceWidth;
        int spaceCount = (int) spaceCountExact;

        //Close enough to be a space
        if(spaceCountExact - spaceCount > PdfStreamDecoder.currentThreshold) {//0.595
            spaceCount += 1;
        }

        if (spaceCount > 3) {
            return false;
        }

        if (newTJ && spaceCount > 2) {
            return false;
        }

        if (separateToWords && spaceCount > 0) {
            // If we detect a space needs to be added then using separateToWords will not include the space in the output
            // Including the space will make this code very complex as all calculations
            // (such as widths, wordspacing/kerning/charspacing) are based on the next character to be added
            return false;
        }

        if (glyf.equals(" ")) {
            numSpaces++;
            this.wordSpacing += wordSpacing;
        }


        if (spaceCount > 0 && !glyf.equals(" ") && !lastGlyf.equals(" ")) {
            numSpaces++;
            numChars++;
            text.append(' ');

            // A space may be more or less than the width we need to account for, so account for that with kerning
            //kerning -= spaceWidth + charSpacing;

            kerning = 0;//Currently ignore.
        }

        this.kerning += kerning;
        this.charSpacing += charSpacing;
        numChars++;

        //stick the glyf on the end
        text.append(glyf);

        if (!isComplex) {
            stringWidth = x - startXCoord + charWidth;
            expectedXCoord = x + charWidth;
//        } else {
//
//            float[][] point2 = {{1, 0, 0}, {0, 1, 0}, {glyphWidth + kerning + charSpacing, 0, 1}};//bottom right
//
//            //If text has a rotation/skew then apply it to the points
//            if (Trm[0][1] != 0 || Trm[1][0] != 0 || Trm[0][0] != Trm[1][1]) {
//
//                //Remove translate from the Trm
//                final float[][] rotation = {{Trm[0][0],Trm[0][1],0},{Trm[1][0],Trm[1][1],0},{0,0,1}};
//                point2 = Matrix.multiply(point2, rotation);
//            } else {
//                point2[2][0] *= Trm[0][0];//Multiply glyphWidth by xScale from Trm
//            }
//
//            expectedXCoord = x + point2[0][0];
//            expectedYCoord = y + point2[0][1];
        }

        /**
         * update global pointers used
         */
        lastGlyphWidth = glyphWidth;
        lastGlyf = glyf;

        if (glyf.charAt(0) == '.') {
            dotCount++;
        } else {
            dotCount = 0;
            allowDotTrails = false;
        }

        this.lastYUsed = y;
        this.lastXUsed = x;


        // When the space is declared in the PDF it will be included in the output, but not if we detect an extra space needs adding
        // We can return false here despite having actually added the space to the output because the code will not start a new TextBlock with a space
        //isWhiteSpace used to pick up any space character. (Case 17213 - general-May2014/Pages from SVA-ALL-SLM_NoRestriction.pdf)
        return !separateToWords || glyf.length() != 1 || !Character.isWhitespace(glyf.charAt(0));

    }

    public FontMapper getFontMapper() {
        return fontMapper;
    }

    /*
     * @return true if glfy is on ignore list
     */
    public static boolean ignoreGlyf(final String glfy) {
        return glfy.codePointAt(0) == 65533;
    }
    
    public int getRenderType() {
        return renderType;
    }

    protected void setOverDrawColor(final int overDrawcolor) {
        this.overDrawcolor=overDrawcolor;
        textOverDrawn=true;
    }

}
