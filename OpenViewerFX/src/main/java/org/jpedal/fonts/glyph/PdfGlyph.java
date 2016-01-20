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
 * PdfGlyph.java
 * ---------------
 */
package org.jpedal.fonts.glyph;

import java.awt.*;
import java.awt.geom.Area;

import org.jpedal.color.PdfPaint;

/**
 * base glyph used by T1 and Truetype fonts
 */
public abstract class PdfGlyph {

    private int glyphNumber = -1;

    public final int getGlyphNumber() {
        return glyphNumber;
    }

    public final void setGlyphNumber(final int no) {
        glyphNumber = no;
    }

    public static final int FontBB_X=1;
    public static final int FontBB_Y=2;
    public static final int FontBB_WIDTH=3;
    public static final int FontBB_HEIGHT=4;

    /**draw the glyph*/
	public void render(int text_fill_type, Graphics2D g2, float scaling, boolean isFormGlyph){
        
    }

	/**
	 * return max possible glyph width in absolute units
	 */
	public float getmaxWidth(){
        return 0;
    }

	/**
	 * used by type3 glyphs to set colour if required
	 */
	public void setT3Colors(PdfPaint strokeColor, PdfPaint nonstrokeColor, boolean lockColours){
        
    }

	/**
	 * see if we ignore colours for type 3 font
	 */
	public boolean ignoreColors(){
        return true;
    }

	public Area getShape(){
        return null;
    }

	public void setWidth(float width){
        
    }

    /**
     * retrun fontBounds paramter where type is a contant in PdfGlyh
     * @param type
     * @return
     */
    int getFontBB(int type){
        return 0;
    }

    public void setStrokedOnly(boolean b){
        
    }

    public boolean containsBrokenData(){
        return false;
    }

    public Object getPath() {
        throw new UnsupportedOperationException("getPath Not supported yet."); 
    }

    public boolean hasHintingApplied(){
        return false;
    }
}