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
 * NameWriter.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;


import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.tt.Name;

import java.awt.*;
import org.jpedal.utils.repositories.FastByteArrayOutputStream;
import java.io.IOException;

public class NameWriter extends Name implements FontTableWriter{

    final String[] strings = new String[7];

    /**
     * used to turn Ps into OTF
     */
    public NameWriter(final PdfFont currentFontData, final PdfJavaGlyphs glyphs, String name) {

    	name = name.replaceAll("[.,<>*#]", "-");
        //copyright
        if (currentFontData.getCopyright() != null) {
            strings[0] = currentFontData.getCopyright();
        } else {
            strings[0] = "No copyright information found.";
        }

        //familyName
        strings[1] = name;

        //subName
        switch (glyphs.style) {
            case Font.PLAIN:
                strings[2] = "Roman";
                break;
            case Font.BOLD:
                strings[2] = "Bold";
                break;
            case Font.ITALIC:
                strings[2] = "Italic";
                break;
            default:
                strings[2] = "Roman";
                break;
        }

        //uid
        strings[3] = "JPedal PDF2HTML "+name;

        //fullName
        strings[4] = name;

        //version
        strings[5] = "Version 1.0";

        //psName
        strings[6] = name;
    }


    @Override
    public byte[] writeTable() throws IOException {

        final FastByteArrayOutputStream bos=new FastByteArrayOutputStream();

        bos.write(FontWriter.setNextUint16(0));       //format
        bos.write(FontWriter.setNextUint16(14));      //record count
        bos.write(FontWriter.setNextUint16(174));     //Offset to strings

        int offset = 0;
        for (int i=0; i<7; i++) {
            bos.write(FontWriter.setNextUint16(1));   //mac
            bos.write(FontWriter.setNextUint16(0));   //Roman
            bos.write(FontWriter.setNextUint16(0));   //English
            bos.write(FontWriter.setNextUint16(i));   //String name id
            bos.write(FontWriter.setNextUint16(strings[i].length()));
            bos.write(FontWriter.setNextUint16(offset));

            offset += strings[i].length();
        }

        for (int i=0; i<7; i++) {
            bos.write(FontWriter.setNextUint16(3));       //windows
            bos.write(FontWriter.setNextUint16(1));       //UCS-2
            bos.write(FontWriter.setNextUint16(1033));    //US English
            bos.write(FontWriter.setNextUint16(i));       //String name id
            bos.write(FontWriter.setNextUint16(strings[i].length() * 2));
            bos.write(FontWriter.setNextUint16(offset));

            offset += strings[i].length() * 2;
        }

        for (int i=0; i<7; i++) {
            final byte[] s = strings[i].getBytes("US-ASCII");     //Identical to MacRoman for the fist 7 bytes - doesn't use the 8th, so it can be substituted safely
            for (final byte value : s) {
                bos.write(FontWriter.setNextUint8(value));
            }
        }

        for (int i=0; i<7; i++) {
            final byte[] s = strings[i].getBytes("UTF-16BE");
            for (final byte value : s) {
                bos.write(FontWriter.setNextUint8(value));
            }
        }

        return bos.toByteArray();
    }

    @Override
    public int getIntValue(final int key) {

        final int value=0;

        switch(key){
            default:
                break;
        }

        return value;
    }

    public String getString(final int index) {
        return strings[index];
    }
}
