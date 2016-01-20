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
 * UnrendererGlyph.java
 * ---------------
 */
package org.jpedal.fonts.glyph;

import java.io.Serializable;

/**
 * holds data so we can draw glyph on first appearance
 *
 */
public class UnrendererGlyph extends PdfGlyph implements Serializable {

    public final float x;
    public final float y;
    public final int rawInt;
    public final float currentWidth;
	
	public UnrendererGlyph(final float x, final float y, final int rawInt, final float currentWidth) {

		this.x=x;
		this.y=y;
		this.rawInt=rawInt;
		this.currentWidth=currentWidth;
		
	}
}