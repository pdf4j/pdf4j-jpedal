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
 * HTMLFontUtils.java
 * ---------------
 */
package org.jpedal.fonts;

import org.jpedal.fonts.tt.conversion.GlyphMapping;
import org.jpedal.fonts.tt.conversion.PS2OTFFontWriter;
import org.jpedal.fonts.tt.conversion.TTFontWriter;

import java.util.Collection;
import java.util.HashMap;


public class HTMLFontUtils {

    public static byte[] convertTTForHTML(final byte[] rawFontData) {

        //create new version which works on IPad
        return new TTFontWriter(rawFontData).writeFontToStream(); //use 1.0
    }


// --Commented out by Inspection START (14/04/2013 00:48):
//    public static byte[] convertPSForHTML(PdfFont pdfFont, byte[] rawFontData, String fileType, HashMap<String, Integer> widths) throws Exception {
//    		 return new PS2OTFFontWriter(pdfFont,rawFontData, fileType, widths).writeFontToStream(); //use OTTO as start
//    }
// --Commented out by Inspection STOP (14/04/2013 00:48)

    public static byte[] convertPSForHTMLOTF(final PdfFont pdfFont, final byte[] rawFontData, final String fileType, final HashMap<String, Integer> widths, final Collection<GlyphMapping> mappings) throws Exception {
   		 	return new PS2OTFFontWriter(pdfFont,rawFontData, fileType, widths, mappings).writeFontToStream();
    }
    
    public static byte[] convertPSForHTMLWOFF(final PdfFont pdfFont, final byte[] rawFontData, final String fileType, final HashMap<String, Integer> widths, final Collection<GlyphMapping> mappings) throws Exception {
    		return new PS2OTFFontWriter(pdfFont,rawFontData, fileType, widths, mappings).writeFontToWoffStream();
    }

    public static byte[] convertPSForHTMLEOT(final PdfFont pdfFont, final byte[] rawFontData, final String fileType, final HashMap<String, Integer> widths, final Collection<GlyphMapping> mappings) throws Exception {
//            System.out.println(rawFontData.length);
    		return new PS2OTFFontWriter(pdfFont,rawFontData, fileType, widths, mappings).writeFontToEotStream();
    }

}
