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
 * BaseT1Glyph.java
 * ---------------
 */
package org.jpedal.fonts.glyph;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>defines the current shape which is created by command stream</p> 
 * <p><b>This class is NOT part of the API</b></p>.
 * Shapes can be drawn onto pdf or used as a clip on other image/shape/text.
 * Shape is built up by storing commands and then turning these commands into a
 * shape. Has to be done this way as Winding rule is not necessarily
 * declared at start.
  */
public abstract class BaseT1Glyph extends PdfGlyph implements Serializable
{
	
    protected float  glyfwidth=1000f;

    protected boolean isStroked;

    protected final Map strokedPositions=new HashMap();

    public BaseT1Glyph(){}
    
    @Override
    public void setStrokedOnly(final boolean flag) {
        isStroked=flag;

    }

    @Override
    public void setWidth(final float width) {
		this.glyfwidth=width;
		
	}

    /* (non-Javadoc)
     * @see org.jpedal.fonts.PdfGlyph#getmaxWidth()
     */
    @Override
    public float getmaxWidth() {
    	
        return glyfwidth;
    }
}
