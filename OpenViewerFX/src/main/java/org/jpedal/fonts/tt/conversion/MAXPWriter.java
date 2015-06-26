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
 * MAXPWriter.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.tt.Maxp;

import org.jpedal.utils.repositories.FastByteArrayOutputStream;
import java.io.IOException;

public class
        MAXPWriter extends Maxp implements FontTableWriter{

    final int glyphCount;


    /**
     * used to turn Ps into OTF
     * @param glyphs
     */
    public MAXPWriter(final PdfJavaGlyphs glyphs) {

        glyphCount=glyphs.getGlyphCount();
    }


    @Override
    public byte[] writeTable() throws IOException {

        final FastByteArrayOutputStream bos=new FastByteArrayOutputStream();
        
        bos.write(FontWriter.setNextUint32(20480)); //revision 5000 hex
        bos.write(FontWriter.setNextUint16(glyphCount)); //number of glyphs

        return bos.toByteArray();
    }

    @Override
    public int getIntValue(final int key) {
        return 0;
    }
}
