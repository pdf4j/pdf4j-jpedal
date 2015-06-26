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
 * HmtxWriter.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.tt.Hmtx;

import org.jpedal.utils.repositories.FastByteArrayOutputStream;
import java.io.IOException;

public class HmtxWriter extends Hmtx implements FontTableWriter{

    final int glyphCount;
    final int[] advanceWidth;
    final int[] leftSideBearing;

    public HmtxWriter(final PdfJavaGlyphs glyphs, final int[] advanceWidth, final int[] lsbs) {
        glyphCount=glyphs.getGlyphCount();
        this.advanceWidth = advanceWidth;

        if (lsbs == null) {
            leftSideBearing = new int[65535];
        } else {
            leftSideBearing = lsbs;
        }
    }

    @Override
    public byte[] writeTable() throws IOException {

        final FastByteArrayOutputStream bos=new FastByteArrayOutputStream();

        for (int i=0; i<glyphCount; i++) {
            bos.write(FontWriter.setNextUint16(advanceWidth[i]));
            bos.write(FontWriter.setNextInt16(leftSideBearing[i]));
        }

        return bos.toByteArray();
    }

    @Override
    public int getIntValue(final int key) {
        return 0;
    }
}
