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
 * CMAPWriter.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.tt.*;
import org.jpedal.utils.Sorts;

import org.jpedal.utils.repositories.FastByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("UnusedDeclaration")
public class
        CMAPWriter extends CMAP implements FontTableWriter{

    //set in code for use in OS2 table
    int minCharCode=65536;
    int maxCharCode;

    String fontName;
    private PdfFont originalFont;

    public CMAPWriter(final FontFile2 currentFontFile, final int startPointer) {
        super(currentFontFile, startPointer);

    }
    
    /**
     *Creates format 0 subtable for true type fonts [only]
     *@param PdfJavaGlyphs
     */
    private void createFormat0MapForTT(final PdfJavaGlyphs glyphs, final Collection<GlyphMapping> mappings){
    	final TTGlyphs ttGlyphs = (TTGlyphs)glyphs;
    	final CMAP currentCmap = (CMAP) ttGlyphs.getTable(FontFile2.CMAP);
    	final Map glyphMap;
    	if(currentCmap==null){
    		glyphMap = new HashMap<Integer,Integer>();
    		if(originalFont.getToUnicode()==null){
    			final int gCount = glyphs.getGlyphCount();
    			for(int z=0;z<gCount;z++){
    				glyphMap.put(z,z);
    			}    		
    		}
    		else{
    			for(int z=0;z<65536;z++){
    				if(originalFont.getUnicodeMapping(z)!=null){
    					final String str = originalFont.getUnicodeMapping(z);
    					final int adjValue = getAdjustedUniValue(str);
    					if(adjValue>=0){
    						glyphMap.put(adjValue,z);
    					}
    				}
    			}
    		}
    	}
    	else{
    		glyphMap = currentCmap.buildCharStringTable();
    	}

        //Add in any mapping objects
        if (mappings != null) {
            for (final GlyphMapping mapping : mappings) {
                if (mapping.getGlyphNumber()-1 < glyphs.getGlyphCount()) {
                    glyphMap.put(mapping.getOutputChar().codePointAt(0), mapping.getGlyphNumber()-1);
                }
            }
        }

    	for(int z=0;z<256;z++){    		
    		glyphIndexToChar[1][z] = glyphMap.get(z)!=null?(Integer)glyphMap.get(z):0;
    	}
    }

    //@see createFormat4Map
    private int createFormat4MapForTT(final PdfJavaGlyphs glyphs, final Collection<GlyphMapping> mappings){

        //Initialise array
        final int[] unicodeToGlyph = new int[65536];
        for (int i=0; i<unicodeToGlyph.length;i++) {
            unicodeToGlyph[i]=Integer.MAX_VALUE;
        }

        //Add in generated mappings
        if (mappings != null) {
            for (final GlyphMapping mapping : mappings) {
                if (mapping.getGlyphNumber() <= glyphs.getGlyphCount()) {
                    unicodeToGlyph[mapping.getOutputChar().codePointAt(0)] = mapping.getGlyphNumber();
                }
            }
        }

        return createFormat4Map(unicodeToGlyph);
    }

    private int createFormat4Map(final int[] unicodeToGlyph) {

        //Find ranges of unicode characters with valid glyphs
        final int[] rangeStart = new int[40000];
        final int[] rangeEnd = new int[40000];
        int segCount = 0;
        boolean inRange = false;
        for (int i=0; i<65536; i++) {
            if (inRange && unicodeToGlyph[i] == Integer.MAX_VALUE) {
                inRange = false;
                rangeEnd[segCount] = i-1;
                segCount++;
            } else if (!inRange && unicodeToGlyph[i] != Integer.MAX_VALUE) {
                inRange = true;
                rangeStart[segCount] = i;
            }
        }

        //Handle unicode replacement character
        if (unicodeToGlyph[0xFFFD] == Integer.MAX_VALUE) {
            rangeStart[segCount] = 0xFFFD;
            rangeEnd[segCount] = 0xFFFD;
            unicodeToGlyph[0xFFFD] = 1;
            segCount++;
        }

        //Deal with end character
        if (unicodeToGlyph[0xFFFF] == Integer.MAX_VALUE) {
            rangeStart[segCount] = 0xFFFF;
            rangeEnd[segCount] = 0xFFFF;
            unicodeToGlyph[0xFFFF] = 1;
            segCount++;
        }

        //Initialise values
        final int segX2 = segCount*2;
        CMAPsegCount = new int[]{segX2,0,segX2};

        int searchRange = 1;
        while (searchRange*2 <= segCount) {
            searchRange *= 2;
        }
        searchRange *= 2;
        CMAPsearchRange = new int[]{searchRange, 0, searchRange};

        int entrySelector = 0;
        int working = searchRange/2;
        while (working > 1) {
            working /= 2;
            entrySelector++;
        }
        CMAPentrySelector = new int[]{entrySelector,0,entrySelector};

        final int rangeShift = segX2 - searchRange;
        CMAPrangeShift = new int[]{rangeShift,0,rangeShift};

        endCode = rangeEnd;

        CMAPreserved = new int[]{0,0,0};

        startCode = rangeStart;

        idRangeOffset = new int[segCount];

        //Check ranges are sequential in key and value and flag if not
        int glyphIdArrayLength=0;
        for (int i=0; i<segCount; i++) {
            final int diff = unicodeToGlyph[rangeStart[i]] - rangeStart[i];
            for (int j=rangeStart[i]+1; j <= rangeEnd[i]; j++) {
                if (unicodeToGlyph[j] - j != diff) {
                    idRangeOffset[i] = -1;
                    glyphIdArrayLength += (rangeEnd[i]+1) - rangeStart[i];
                    break;
                }
            }
        }

        //Deal with non-zero idRangeOffset values! (flagged as -1)
        //WARNING! This is currently untested so results may vary!
        glyphIdArray = new int[glyphIdArrayLength];

        //Calculate deltas
        idDelta = new int[segCount];

        //Go through segments for deltas or offsets
        int addressPointer = 16 + (segCount * 8);
        int arrayPointer = 0;
        for (int i=0; i<idRangeOffset.length; i++){
            if (idRangeOffset[i] == 0) {
                //Use a direct offset from the unicode values
                idDelta[i] = unicodeToGlyph[rangeStart[i]] - rangeStart[i] - 1;

                while (idDelta[i]+startCode[i] < 0) {
                    idDelta[i]++;
                }
            } else {
                //Use glyphIdArray
                idRangeOffset[i] = addressPointer - (16 + (segCount * 6) + (i * 2));

                for (int j=rangeStart[i]; j<=rangeEnd[i]; j++) {
                    glyphIdArray[arrayPointer] = unicodeToGlyph[j]-1;

                    addressPointer+=2;
                    arrayPointer++;
                }
            }
        }

        //Return table length
        return 16 + (segCount * 8) + (glyphIdArrayLength * 2);
    }


    private void getNonTTGlyphData(final PdfJavaGlyphs glyphs, final boolean is1C, final String[] glyphList, final int[] unicodeToGlyph) {
        //Get glyphs present and put in unicode array
        int cid=0;
        for (int i=0; i<glyphs.getGlyphCount()+1; i++) {
            String val=null;
            if (originalFont != null && originalFont.getGlyphData().isIdentity() && originalFont.hasToUnicode() && i>1) {
                val = originalFont.getUnicodeMapping(cid);
                while (val == null && cid < 0xd800) {
                    cid++;
                    val = originalFont.getUnicodeMapping(cid);
                }
                if (val != null) {
                    unicodeToGlyph[val.charAt(0)] = i;
                }
                //

                cid++;
            } else {
                if (val==null && is1C) {
                    val = glyphs.getIndexForCharString(i);
                } else if (val==null && i < glyphList.length) {
                    val = glyphList[i];
                }

                if (val != null) {
                    final int uc = StandardFonts.getIDForGlyphName(fontName, val);

                    if (uc >= 0 && uc < unicodeToGlyph.length) {
                        if (is1C) {
                            unicodeToGlyph[uc] = i;
                        } else {
                            unicodeToGlyph[uc] = i + 1;
                        }
                    }
                }
            }
        }
    }

    /**
     * Used to turn a collection of mappings into a cmap.
     * @param mappings A collection of mappings to build into a cmap table
     */
//    public CMAPWriter(Collection<GlyphMapping> mappings) {
//
//        /**
//         * initialise the 3 tables we will need for out fonts in browser
//         */
//        numberSubtables=3;
//        CMAPformats=new int[]{4,0,4};
//        glyphIndexToChar=new int[3][256];
//        platformID=new int[]{0,1,3};
//        platformSpecificID=new int[]{3,0,1};
//        CMAPlang=new int[]{0,0,0};
//
//
//        /**
//         * Fill glyphIndexToChar for Type 0 cmaps
//         */
//        for (GlyphMapping mapping : mappings) {
//
//            String glyphName = StandardFonts.getNameFromUnicode(mapping.getOutputChar());
//            if (mapping.getOutputChar().contains("ä")) {
//                System.out.println("name = "+glyphName);
//            }
//
//            int id = StandardFonts.lookupCharacterIndex(glyphName, StandardFonts.MAC);
//
//            if (id >= 0 && id < 256) {
//                glyphIndexToChar[1][id] = mapping.getGlyphNumber()-1;
//            }
//        }
//
//        //create array and fill with dummy values
//        int[] unicodeToGlyph = new int[65536];
//        for (int i=0; i<unicodeToGlyph.length;i++) {
//            unicodeToGlyph[i]=Integer.MAX_VALUE;
//        }
//
//        //Fill with values from mappings
//        for (GlyphMapping mapping : mappings) {
//            unicodeToGlyph[mapping.getOutputChar().codePointAt(0)] = mapping.getGlyphNumber();
//        }
//
//        /**
//         * Initialise format 4 fields
//         */
//        int format4Length = createFormat4Map(unicodeToGlyph);
//
//        CMAPlength=new int[]{format4Length,262,format4Length};
//        CMAPsubtables=new int[]{28,28+(format4Length*2),28+format4Length};
//    }

    /**
     * used to turn Ps into OTF
     */
    public CMAPWriter(final String fontName, final PdfFont currentFontData, final PdfFont originalFont, final PdfJavaGlyphs glyphs, final String[] glyphList, final Collection<GlyphMapping> mappings) {

        this.fontName = fontName;

        this.originalFont = originalFont;

        /**make a list of glyfs from TT font*/
        //commented out because data manipulation is handled differently in TT fonts
        //@see createFormat4MapForTT in CMAPWriter and
        //@see buildCharStringTable() in CMAP
//        if(glyphList==null && originalFont.getFontType()==StandardFonts.TRUETYPE){
//            Map charStringsFoundInTTFont=originalFont.getGlyphData().getCharStrings();
//
//            if(charStringsFoundInTTFont!=null){
//                int size=charStringsFoundInTTFont.size();
//                glyphList=new String[size];
//                Iterator i=charStringsFoundInTTFont.keySet().iterator();
//                int ptr=0;
//                while(i.hasNext()){
//                    glyphList[ptr]=i.next().toString();
//                    ptr++;
//                }
//            }
//        }

        /**
         * initialise the 3 tables we will need for out fonts in browser
         */
        numberSubtables=3;
        CMAPformats=new int[]{4,0,4};
        glyphIndexToChar=new int[3][256];
        platformID=new int[]{0,1,3};
        platformSpecificID=new int[]{3,0,1};
        CMAPlang=new int[]{0,0,0};


        //Initialise format 4 fields
        final int format4Length;

        if(originalFont.getFontType() == StandardFonts.TRUETYPE){
            format4Length = createFormat4MapForTT(glyphs, mappings);
            createFormat0MapForTT(glyphs, mappings);
        }else{ // for cff font handling
            //create array and fill with dummy values
            final int[] unicodeToGlyph = new int[65536];
            for (int i=0; i<unicodeToGlyph.length;i++) {
                unicodeToGlyph[i]=Integer.MAX_VALUE;
            }

            getNonTTGlyphData(glyphs, currentFontData.is1C(), glyphList, unicodeToGlyph);

            //Check for mappings and add to unicodeToGlyph array
            if (mappings != null) {
                for (final GlyphMapping mapping : mappings) {
                    final int num = mapping.getGlyphNumber()+(originalFont.is1C()?0:1);
                    if (num <= glyphs.getGlyphCount() && num >= 0) {
                        unicodeToGlyph[mapping.getOutputChar().codePointAt(0)] = num;
                    }
                }
            }

            //Create format 4
        	format4Length = createFormat4Map(unicodeToGlyph);
        	final int enc=StandardFonts.MAC;
            StandardFonts.checkLoaded(enc);


            //Get glyphs present and put in mac encoding array for format 0
            for (int i=0; i<glyphs.getGlyphCount()+1; i++) {
                String val=null;
                if (currentFontData.is1C()) {
                    val = glyphs.getIndexForCharString(i);
                } else if (i < glyphList.length) {
                    val = glyphList[i];
                }

                if (val != null) {
                    final int id = StandardFonts.lookupCharacterIndex(val, StandardFonts.MAC);

                    int glyph = i;
                    if (currentFontData.is1C()) {
                        glyph -= 1;
                        if (i == 1) {
                            glyph = 1;
                        }
                    }
                    if (id >= 0 && id < 256) {
                        glyphIndexToChar[1][id] = glyph;
                    }
                }
            }

            //add in any mapping objects for format 0
            if (mappings != null) {
                for (final GlyphMapping mapping : mappings) {
                    if (mapping.getGlyphNumber() < glyphs.getGlyphCount()) {
                        final String glyphName = StandardFonts.getNameFromUnicode(mapping.getOutputChar());

                        final int id = StandardFonts.lookupCharacterIndex(glyphName, StandardFonts.MAC);

                        final int num = mapping.getGlyphNumber()+(originalFont.is1C()?-1:0);
                        if (id >= 0 && id < 256 && num >= 0 && num <= glyphs.getGlyphCount()) {
                            glyphIndexToChar[1][id] = num;
                        }
                    }
                }
            }

        }
                
        CMAPlength=new int[]{format4Length,262,format4Length};
        CMAPsubtables=new int[]{28,28+(format4Length*2),28+format4Length};
        

    }


    @Override
    public byte[] writeTable() throws IOException {

        final FastByteArrayOutputStream bos=new FastByteArrayOutputStream();

        final boolean debug=false;

        if(debug) {
            System.out.println("write CMAP " + this);
        }

        //LogWriter.writeMethod("{readCMAPTable}", 0);

        //read 'cmap' table
        {

            final int numberSubtables=this.numberSubtables;

            final ArrayList tables=new ArrayList();
            for(int i=0;i<numberSubtables;i++){
                tables.add(i);
            }

            bos.write(FontWriter.setNextUint16(id));
            bos.write(FontWriter.setNextUint16(numberSubtables));

            for(int j=0;j<numberSubtables;j++){

                int i=(Integer)tables.get(j);
                final boolean isDuplicate=i<0;
                if(i<0) {
                    i = -i;
                }

                if(isDuplicate){
                    bos.write(FontWriter.setNextUint16(0));
                    bos.write(FontWriter.setNextUint16(3));
                }else{
                    bos.write(FontWriter.setNextUint16(platformID[i]));
                    bos.write(FontWriter.setNextUint16(platformSpecificID[i]));
                }

                bos.write(FontWriter.setNextUint32(CMAPsubtables[i]));

                if(debug) {
                    System.out.println("platformID[i]=" + platformID[i] + " platformSpecificID[i]=" + platformSpecificID[i] + " CMAPsubtables[i]=" + CMAPsubtables[i]);
                }
            }

            //work our correct order for subtables
            final int[] offset=new int[numberSubtables];
            int[] order=new int[numberSubtables];
            for(int j=0;j<numberSubtables;j++){
                int i=(Integer)tables.get(j);
                if(i<0) {
                    i = -i;
                }

                offset[i]=CMAPsubtables[i];
                order[j]=i;
            }
            order= Sorts.quicksort(offset, order);

            //now write back each subtable
            for(int j=0;j<numberSubtables;j++){
                final int i=order[j];

                //any padding
                while(bos.size()<CMAPsubtables[i]){
                    bos.write((byte) 0);
                }
                //}

                //assume 16 bit format to start
                bos.write(FontWriter.setNextUint16(CMAPformats[i]));

                //length
                bos.write(FontWriter.setNextUint16(CMAPlength[i]));

                //lang
                bos.write(FontWriter.setNextUint16(CMAPlang[i]));

                //actual data
                if(CMAPformats[i]==0 && CMAPlength[i]==262){

                   for(int glyphNum=0;glyphNum<256;glyphNum++){
                       bos.write(FontWriter.setNextUint8(glyphIndexToChar[i][glyphNum]));


                   }


                }else if(CMAPformats[i]==4){

                    //@sam -works for TT.
                    //to make it work for OTF we need to setup the values for all
                    //the variables in the  OTF COnstructor
                    //public CMAPWriter(PdfFont currentFontData,PdfJavaGlyphs glyphs)


                    //segcount
                    final int segCount=CMAPsegCount[i]/2;
                    bos.write(FontWriter.setNextUint16(CMAPsegCount[i]));

                    bos.write(FontWriter.setNextUint16(CMAPsearchRange[i]));
                    bos.write(FontWriter.setNextUint16(CMAPentrySelector[i]));
                    bos.write(FontWriter.setNextUint16(CMAPrangeShift[i]));

                    for (int jj = 0; jj < segCount; jj++) {
                        bos.write(FontWriter.setNextUint16(endCode[jj]));
                    }

                    bos.write(FontWriter.setNextUint16(CMAPreserved[i]));

                    for (int jj = 0; jj < segCount; jj++) {
                        bos.write(FontWriter.setNextUint16(startCode[jj]));
                    }

                    for (int jj = 0; jj < segCount; jj++) {
                        bos.write(FontWriter.setNextUint16(idDelta[jj]));
                    }

                    for (int jj = 0; jj < segCount; jj++) {
                        bos.write(FontWriter.setNextUint16(idRangeOffset[jj]));
                    }
                    
                    if(glyphIdArray != null){
                    	for (final int aGlyphIdArray : glyphIdArray) {
                            bos.write(FontWriter.setNextUint16(aGlyphIdArray));
                        }
                    }
                    
//                }else if(CMAPformats[j]==6){
//                    int firstCode=currentFontFile.getNextUint16();
//                    int entryCount=currentFontFile.getNextUint16();
//
//                    f6glyphIdArray = new int[firstCode+entryCount];
//                    for(int jj=0;jj<entryCount;jj++)
//                        f6glyphIdArray[jj+firstCode]=currentFontFile.getNextUint16();
//
//                }else{
//                    //System.out.println("Unsupported Format "+CMAPformats[j]);
//                    //reset to avoid setting
//                    CMAPformats[j]=-1;
//
                }
            }
        }

        /**validate format zero encoding*/
        //if(formatFour!=-1)
        //validateMacEncoding(formatZero,formatFour);

        return bos.toByteArray();
    }

    @Override
    public int getIntValue(final int key) {

        int value=0;

        switch(key){
            case MIN_CHAR_CODE:
            value=minCharCode;
                break;

            case MAX_CHAR_CODE:
            value=maxCharCode;
                break;
        }

        return value;
    }
    
    /**
     *return the adjusted unicode value based on String
     *which also adjusts some composite glyph values 
     */
    private static int getAdjustedUniValue(final String str){
    	if(str.length()==1){
    		return str.charAt(0);
    	}
    	else{
    		if(str.equals("ff")){
    			return 64256;
    		}
    		else if(str.equals("fi")){
    			return 64257;
    		}
    		else if(str.equals("fl")){
    			return 64258;
    		}
    		else if(str.equals("ffi")){
    			return 64259;
    		}
    		else if(str.equals("ffl")){
    			return 64260;
    		}
    		else{
    			return -1;
    		}
    	}
    }

    public int getMaxCharCode() {
        return maxCharCode;
    }
}
