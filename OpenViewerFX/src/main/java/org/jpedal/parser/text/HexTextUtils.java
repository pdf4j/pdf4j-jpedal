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
 * HexTextUtils.java
 * ---------------
 */

package org.jpedal.parser.text;

import org.jpedal.fonts.PdfFont;
import org.jpedal.parser.ParserOptions;

/**
 *
 * @author markee
 */
class HexTextUtils {
   
    static int getHexValue(final byte[] stream, int i, GlyphData glyphData, PdfFont currentFontData, ParserOptions parserOptions ) {
        //'<'=60
        
        int val=0,chars=0,nextInt;
        
        int charSize=glyphData.getCharSize();
        //get number of chars
        for (int i2 = 1; i2 < charSize; i2++) {
            nextInt = stream[i + i2];
            
            if(nextInt==62){ //allow for less than 4 chars at end of stream (ie 6c>)
                i2=4;
                charSize=2;
                glyphData.setCharSize(2);
            }else if(nextInt==10 || nextInt==13){ //avoid any returns
                i++;
                i2--;
            }else{
                chars++;
            }
        }
        //now convert to value
        int topHex, ptr=0;
        for(int aa=0;aa<chars+1;aa++){
            
            topHex=stream[i+chars-aa];
            
            //convert to number
            if(topHex>='A' && topHex<='F'){
                topHex -= 55;
            }else if(topHex>='a' && topHex<='f'){
                topHex -= 87;
            }else if(topHex>='0' && topHex<='9'){
                topHex -= 48;
            }else{    //ignore 'bum' values
                continue;
            }
            val += (topHex << TD.multiply16[ptr]);
            ptr++;
        }
        glyphData.setRawInt(val);
        i = i + charSize-1; //move offset
        glyphData.setRawChar((char) val);
        glyphData.setDisplayValue(currentFontData.getGlyphValue(val));
        if(currentFontData.isCIDFont() && currentFontData.getCMAP()!=null && currentFontData.getUnicodeMapping(val)==null){
            glyphData.setRawChar(glyphData.getDisplayValue().charAt(0));
            glyphData.setRawInt(glyphData.getRawChar());
        }
        if(parserOptions.isTextExtracted()) {
            glyphData.setUnicodeValue(currentFontData.getUnicodeValue(glyphData.getDisplayValue(), glyphData.getRawInt()));
        }
        
        return i;
    }
}
