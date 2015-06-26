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
 * CFFWriter.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.fonts.Type1;
import org.jpedal.fonts.Type1C;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.glyph.T1Glyphs;
import org.jpedal.utils.LogWriter;

import org.jpedal.utils.repositories.FastByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("UnusedDeclaration")
public class CFFWriter extends Type1 implements FontTableWriter{


    private static final boolean debugTopDictOffsets = false;

    private final String name;
    private CharstringContext context;
    private final byte[][] subrs;
    private String[] glyphNames;
    private byte[][] charstrings;
    private int[] charstringXDisplacement, charstringYDisplacement;
    private byte[] header, nameIndex, topDictIndex, globalSubrIndex, encodings, charsets, charStringsIndex, privateDict, localSubrIndex, stringIndex;
    private final ArrayList<String> strings = new ArrayList<String>();
    private int[] widthX, widthY, lsbX, lsbY;
    private int defaultWidthX, nominalWidthX;
    private float[] bbox = new float[4];

    //Values for dealing with incorrect em square
    private double scale = 1;

    public CFFWriter(final PdfJavaGlyphs glyphs, final byte[] rawFontData, final String name, final Collection<GlyphMapping> mappings) {
        //Initiate font
        try {
            renderPage = true;
            readType1FontFile(rawFontData);
        } catch (final Exception e) {
            //Do nothing - this just means a few non-essential values won't be picked up.
            if (LogWriter.isOutput()) {
                LogWriter.writeLog("Caught an Exception " + e);
            }

        }

        this.glyphs = glyphs;
        this.name = name;

        //Fix for bad blueValues
        if (blueValues != null && (blueValues.length % 2) != 0) {
            final int[] newBlues = new int[blueValues.length-1];
            System.arraycopy(blueValues, 1, newBlues, 0, newBlues.length);
            blueValues = newBlues;
        }

        //Fix for bad otherBlues
        if (otherBlues != null && (otherBlues.length % 2) != 0) {
            final int[] newOtherBlues = new int[otherBlues.length-1];
            System.arraycopy(otherBlues, 1, newOtherBlues, 0, newOtherBlues.length);
            otherBlues = newOtherBlues;
        }

        //Fetch charstrings and subrs
        final Map charStringSegments = glyphs.getCharStrings();

        //Count subrs and chars
        final Object[] keys = charStringSegments.keySet().toArray();
        Arrays.sort(keys);
        int maxSubrNum=0, maxSubrLen=0, charCount=0;
        for (int i=0; i<charStringSegments.size(); i++) {
            final String key = (String) keys[i];
            if (key.startsWith("subrs")) {
                final int num = Integer.parseInt(key.replaceAll("[^0-9]",""));
                if (num > maxSubrNum) {
                    maxSubrNum = num;
                }
                final int len = ((byte[])charStringSegments.get(key)).length;
                if (len > maxSubrLen) {
                    maxSubrLen = len;
                }
            } else {
                charCount++;
            }
        }

        //Move to array
        subrs = new byte[maxSubrNum+1][];
        glyphNames = new String[charCount];
        charstrings = new byte[charCount][];
        charstringXDisplacement = new int[charCount];
        charstringYDisplacement = new int[charCount];
        int notdefIndex=-1;

        for (int i=0; i<charStringSegments.size(); i++) {
            final String key = (String) keys[i];
            final Object obj = charStringSegments.get(key);
            final byte[] cs = ((byte[])obj);
            if (key.startsWith("subrs")) {
                final int num = Integer.parseInt(key.replaceAll("[^0-9]",""));
                subrs[num] = cs;
            } else {
                final int num = ((T1Glyphs)glyphs).getGlyphNumber(key);
                glyphNames[num] = key;
                charstrings[num] = cs;
                //Record .notdef location as we need to move it to the start (CFF must start with .notdef)
                if (".notdef".equals(key)) {
                    notdefIndex = num;
                }
            }
        }

        if (notdefIndex == -1) {    //create notdef glyph & move everything down one (including mappings)

            //Update names
            final String[] newNames = new String[glyphNames.length+1];
            newNames[0] = ".notdef";
            System.arraycopy(glyphNames, 0, newNames, 1, glyphNames.length);
            glyphNames = newNames;

            //Update charstrings
            final byte[][] newCharstrings = new byte[charstrings.length+1][];
            newCharstrings[0] = new byte[]{14};
            System.arraycopy(charstrings, 0, newCharstrings, 1, charstrings.length);
            charstrings = newCharstrings;

            //Update mappings
            if (mappings != null) {
                for (final GlyphMapping m : mappings) {
                    m.setGlyphNumber(m.getGlyphNumber()+1);
                }
            }

        } else if (notdefIndex != 0) {  //move to start (including mappings)

            //Update names
            final String[] newNames = new String[glyphNames.length];
            newNames[0] = ".notdef";
            System.arraycopy(glyphNames, 0, newNames, 1, notdefIndex);
            System.arraycopy(glyphNames, notdefIndex+1, newNames, notdefIndex+1, glyphNames.length-1-notdefIndex);
            glyphNames = newNames;

            //Update charstrings
            final byte[][] newCharstrings = new byte[charstrings.length][];
            newCharstrings[0] = charstrings[notdefIndex];
            System.arraycopy(charstrings, 0, newCharstrings, 1, notdefIndex);
            System.arraycopy(charstrings, notdefIndex+1, newCharstrings, notdefIndex+1, charstrings.length-1-notdefIndex);
            charstrings = newCharstrings;

            //Update mappings
            if (mappings != null) {
                for (final GlyphMapping m : mappings) {
                    if (m.getGlyphNumber() < notdefIndex) {
                        m.setGlyphNumber(m.getGlyphNumber()+1);
                    } else if (m.getGlyphNumber() == notdefIndex) {
                        m.setGlyphNumber(0);
                    }
                }
            }
        }

        convertCharstrings();

    }

    /**
     * Convert the charstrings from type 1 to type 2.
     */
    private void convertCharstrings() {

        /**
         * Convert instructions
         */
        try {

            widthX = new int[charstrings.length];
            widthY = new int[charstrings.length];
            lsbX = new int[charstrings.length];
            lsbY = new int[charstrings.length];

            //Create a context to read the charstrings in
            context = new CharstringContext(lsbX, lsbY, widthX, widthY, charstringXDisplacement, charstringYDisplacement, glyphNames, subrs, this);

            //Perform initial conversion
            final byte[][] newCharstrings = new byte[charstrings.length][];
            for (int charstringID=0; charstringID<charstrings.length; charstringID++) {
                newCharstrings[charstringID] = convertCharstring(charstringID);
            }

            //Check em square size and reconvert while scaling if necessary
            if (FontMatrix != null && Math.abs(FontMatrix[0] - 0.001d) > 0.00005d) {

                scale = FontMatrix[0] * 1000;

                //scale values
                if (blueValues != null) {
                    for (int i=0; i<blueValues.length; i++) {
                        blueValues[i] = (int)(blueValues[i]*scale);
                    }
                }
                if (otherBlues != null) {
                    for (int i=0; i< otherBlues.length; i++) {
                        otherBlues[i] = (int)(otherBlues[i]*scale);
                    }
                }
                if (familyBlues != null) {
                    for (int i=0; i<familyBlues.length; i++) {
                        familyBlues[i] = (int)(familyBlues[i]*scale);
                    }
                }
                if (familyOtherBlues != null) {
                    for (int i=0; i<familyOtherBlues.length; i++) {
                        familyOtherBlues[i] = (int)(familyOtherBlues[i]*scale);
                    }
                }
                if (stemSnapH != null) {
                    for (int i=0; i<stemSnapH.length; i++) {
                        stemSnapH[i] = (int)(stemSnapH[i]*scale);
                    }
                }
                if (stemSnapV != null) {
                    for (int i=0; i<stemSnapV.length; i++) {
                        stemSnapV[i] = (int)(stemSnapV[i]*scale);
                    }
                }
                if (stdHW != null) {
                    stdHW *= scale;
                }
                if (stdVW != null) {
                    stdVW *= scale;
                }

                //Reset displacements & bbox
                charstringXDisplacement = new int[charstringXDisplacement.length];
                charstringYDisplacement = new int[charstringYDisplacement.length];
                bbox = new float[4];

                //Re-convert charstrings now scale is set
                for (int charstringID=0; charstringID<charstrings.length; charstringID++) {
                    newCharstrings[charstringID] = convertCharstring(charstringID);
                }
                charstrings = newCharstrings;

            } else {
                charstrings = newCharstrings;
            }

        } catch (final Exception e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        /**
         * Calculate values for defaultWidthX and nominalWidthX & add widths to start of charstrings
         */

        //Find counts for each value
        final HashMap<Integer, Integer> valueCount = new HashMap<Integer, Integer>();
        for (int i=0; i<charstrings.length; i++) {
            Integer count = valueCount.get(Integer.valueOf(widthX[i]));
            if (count == null) {
                count = 1;
            } else {
                count += 1;
            }
            valueCount.put(widthX[i], count);
        }

        //Find most common value to use as defaultWidthX
        final Object[] values = valueCount.keySet().toArray();
        int maxCount=0;
        defaultWidthX=0;
        for (final Object value : values) {
            final int count = valueCount.get(value);
            if (count > maxCount) {
                maxCount = count;
                defaultWidthX = (Integer)value;
            }
        }

        //Find average for nominalWidthX
        int total=0;
        int count=0;
        for (final Object value : values) {
            if ((Integer)value != defaultWidthX) {
                count++;
                total += (Integer)value;
            }
        }
        if (count != 0) {
            nominalWidthX = total / count;
        } else {
            nominalWidthX = 0;
        }

        //Blank default widths and update other widths
        for (int i=0; i<widthX.length; i++) {
            if (widthX[i] == defaultWidthX) {
                widthX[i] = Integer.MIN_VALUE;
            } else {
                widthX[i] -= nominalWidthX;
            }
        }

        //Append widths to start of charstrings (but not if it's 0 as this signifies default)
        for (int i=0; i<widthX.length; i++) {
            if (widthX[i] != Integer.MIN_VALUE) {
                final byte[] width = CFFUtils.storeCharstringType2Integer(widthX[i]);
                final byte[] newCharstring = new byte[width.length+charstrings[i].length];
                System.arraycopy(width, 0, newCharstring, 0, width.length);
                System.arraycopy(charstrings[i], 0, newCharstring, width.length, charstrings[i].length);
                charstrings[i] = newCharstring;
            }
        }
    }

    /**
     * Convert a charstring from type 1 to type 2.
     * @param charstringID The number of the charstring to convert
     * @return The converted charstring
     */
    protected byte[] convertCharstring(final int charstringID) {

        final int[] cs = new int[charstrings[charstringID].length];
        for (int i=0; i<charstrings[charstringID].length; i++) {
            cs[i] = charstrings[charstringID][i];
            if (cs[i] < 0) {
                cs[i] += 256;
            }
        }

        final FastByteArrayOutputStream bos = new FastByteArrayOutputStream();

        //Reset charstring context for new charstring
        context.setCharstring(charstringID);

        //Convert to CharstringElements
        CharstringContext.CharstringElement element;
        for (int i=0; i < cs.length; i+=element.getLength()) {
            element = context.createElement(cs, i);
        }

        //Get charstring from context
        final ArrayList<CharstringContext.CharstringElement> currentCharString = context.getCurrentCharString();

        //Rescale commands if necessary
        if (scale != 1 && !context.inSeac()) {
            for (final CharstringContext.CharstringElement e : currentCharString) {
                e.scale(scale);
            }
            widthX[charstringID] = (int)(scale*widthX[charstringID]);
            widthY[charstringID] = (int)(scale*widthY[charstringID]);
            lsbX[charstringID] = (int)(scale*lsbX[charstringID]);
            lsbY[charstringID] = (int)(scale*lsbY[charstringID]);
        }

        //Calculate and store displacement and bbox
        for (final CharstringContext.CharstringElement e : currentCharString) {
            final int[] d = e.getDisplacement();
            charstringXDisplacement[charstringID] += d[0];
            charstringYDisplacement[charstringID] += d[1];
            bbox[0] = charstringXDisplacement[charstringID] < bbox[0] ? charstringXDisplacement[charstringID] : bbox[0];
            bbox[1] = charstringYDisplacement[charstringID] < bbox[1] ? charstringYDisplacement[charstringID] : bbox[1];
            bbox[2] = charstringXDisplacement[charstringID] > bbox[2] ? charstringXDisplacement[charstringID] : bbox[2];
            bbox[3] = charstringYDisplacement[charstringID] > bbox[3] ? charstringYDisplacement[charstringID] : bbox[3];
        }

        //Print for debug
//                System.out.println("Charstring "+charstringID);
//                for (CharstringElement currentElement : currentCharString) {
//                    byte[] e = currentElement.getType2Bytes();
//                    for (byte b : e) {
//                        String bin = Integer.toBinaryString(b);
//                        int addZeros = 8 - bin.length();
//                        for (int k=0; k<addZeros; k++)
//                            System.out.print("0");
//                        if (addZeros < 0)
//                            bin = bin.substring(-addZeros);
//                        int val = b;
//                        if (val < 0)
//                            val += 256;
//                        System.out.println(bin+"\t"+val);
//                    }
//                    System.out.println(currentElement);
//                }
//                    System.out.println();
//                System.out.println();

        //Convert to type 2
        for (final CharstringContext.CharstringElement currentElement : currentCharString) {
            bos.write(currentElement.getType2Bytes());
        }


        return bos.toByteArray();
    }


    /**
     * Returns a string ID for a given string. It first checks in the standard strings, and if it isn't there places it
     * in the array of custom strings.
     * @param text String to fetch ID for
     * @return String ID
     */
    public int getSIDForString(final String text) {
        if (text == null) {
            return 390;
        }

        for (int i=0; i<Type1C.type1CStdStrings.length; i++) {
            if (text.equals(Type1C.type1CStdStrings[i])) {
                return i;
            }
        }

        for (int i=0; i<strings.size(); i++) {
            if (text.equals(strings.get(i))) {
                return 391+i;
            }
        }

        strings.add(text);
        return 390+strings.size();
    }


    /**
     * Retrieve the final whole table.
     * @return the new CFF table
     * @throws IOException
     */
    @Override
    public byte[] writeTable() throws IOException {

        final FastByteArrayOutputStream bos=new FastByteArrayOutputStream();

        //Set as empty array for top Dict generator
        topDictIndex = globalSubrIndex = stringIndex = encodings = charsets = charStringsIndex = privateDict = localSubrIndex = new byte[]{};

        /**
         * Generate values
         */
        header = new byte[]{FontWriter.setNextUint8(1),                   //major
                FontWriter.setNextUint8(0),                               //minor
                FontWriter.setNextUint8(4),                               //headerSize
                FontWriter.setNextUint8(2)};                              //offSize
        nameIndex = CFFUtils.createIndex(new byte[][]{name.getBytes()});

        if (debugTopDictOffsets) {
            System.out.println("Generating first top dict...");
        }

        topDictIndex = CFFUtils.createIndex(new byte[][]{createTopDict()});
        globalSubrIndex = CFFUtils.createIndex(new byte[][]{});
        //Global Subr INDEX                                             -Probably don't need
        encodings = createEncodings();
        charsets = createCharsets();
        //FDSelect                                              //CIDFonts only
        charStringsIndex = CFFUtils.createIndex(charstrings);            //per-font                                            //Might need to reorder, although .notdef does seem to be first
        //Font DICT INDEX                                       //per-font, CIDFonts only
        privateDict = createPrivateDict();                      //per-font
//        localSubrIndex = createIndex(subrs);                    //per-font or per-Private DICT for CIDFonts - Subr's are currently inlined
        //Copyright and Trademark Notices
        stringIndex = CFFUtils.createIndex(createStrings());             //Generate last as strings are added as required by other sections


        //Regenerate private dict until length is stable
        byte[] lastPrivateDict;
        do {
            lastPrivateDict = new byte[privateDict.length];
            System.arraycopy(privateDict, 0, lastPrivateDict, 0, privateDict.length);
            privateDict = createPrivateDict();
        } while (!Arrays.equals(privateDict, lastPrivateDict));


        //Regenerate top dict index until length is stable
        byte[] lastTopDictIndex;
        do {
            lastTopDictIndex = new byte[topDictIndex.length];
            System.arraycopy(topDictIndex, 0, lastTopDictIndex, 0, topDictIndex.length);
            if (debugTopDictOffsets) {
                System.out.println("Current length is "+lastTopDictIndex.length+". Testing against new...");
            }
            topDictIndex = CFFUtils.createIndex(new byte[][]{createTopDict()});
        } while (!Arrays.equals(lastTopDictIndex, topDictIndex));

        if (debugTopDictOffsets) {
            System.out.println("Length matches, offsets are now correct.");
        }

        /**
         * Write out
         */
        bos.write(header);
        bos.write(nameIndex);
        bos.write(topDictIndex);
        bos.write(stringIndex);
        bos.write(globalSubrIndex);
        bos.write(encodings);
        bos.write(charsets);
        bos.write(charStringsIndex);
        bos.write(privateDict);
//        bos.write(localSubrIndex);   //Subr's are currently inlined so this is not needed

        return bos.toByteArray();
    }


    /**
     * Create the Top Dict.
     * @return a byte array representing the Top DICT
     * @throws IOException is FastByteArrayOutputStream breaks
     */
    private byte[] createTopDict() {

        final FastByteArrayOutputStream bos = new FastByteArrayOutputStream();

        //Version                       0
        bos.write(CFFUtils.storeInteger(getSIDForString("1")));
        bos.write((byte)0);

        //Notice                        1
        if (copyright != null) {
            bos.write(CFFUtils.storeInteger(getSIDForString(copyright)));
            bos.write((byte)1);
        }

        //FontBBox                      5
        bos.write(CFFUtils.storeInteger((int) bbox[0]));
        bos.write(CFFUtils.storeInteger((int) bbox[1]));
        bos.write(CFFUtils.storeInteger((int) bbox[2]));
        bos.write(CFFUtils.storeInteger((int) bbox[3]));
        bos.write((byte)5);

//        //FontMatrix
//        //Commented out as mac doesn't support FontMatrix :(
//        //Reduce font size if em square is incorrect
//        if (emSquareSize != 1000) {
//            bos.write(CFFUtils.storeReal(1d/emSquareSize));
//            bos.write(CFFUtils.storeInteger(0));
//            bos.write(CFFUtils.storeInteger(0));
//            bos.write(CFFUtils.storeReal(1d/emSquareSize));
//            bos.write(CFFUtils.storeInteger(0));
//            bos.write(CFFUtils.storeInteger(0));
//            bos.write(new byte[]{12,7});
//        }

        //encoding                      16
        int loc = header.length+nameIndex.length+topDictIndex.length+stringIndex.length+globalSubrIndex.length;
        if (encodings.length != 0) {
            bos.write(CFFUtils.storeInteger(loc));
            if (debugTopDictOffsets) {
                System.out.println("Encoding offset: "+loc);
            }
            bos.write((byte)16);
        }

        //charset                       15
        loc += encodings.length;
        bos.write(CFFUtils.storeInteger(loc));
        if (debugTopDictOffsets) {
            System.out.println("Charset offset: "+loc);
        }
        bos.write((byte)15);

        //charstrings                   17
        loc += charsets.length;
        bos.write(CFFUtils.storeInteger(loc));
        if (debugTopDictOffsets) {
            System.out.println("Charstrings offset: "+loc);
        }
        bos.write((byte)17);

        //private                       18
        loc += charStringsIndex.length;
        bos.write(CFFUtils.storeInteger(privateDict.length));
        bos.write(CFFUtils.storeInteger(loc));
        if (debugTopDictOffsets) {
            System.out.println("Private offset: "+loc);
        }
        bos.write((byte)18);


        return bos.toByteArray();
    }

    /**
     * Create the Strings ready to place into an index
     * @return The strings as an array of byte arrays
     */
    private byte[][] createStrings() {
        final byte[][] result = new byte[strings.size()][];

        for (int i=0; i<strings.size(); i++) {
            result[i] = strings.get(i).getBytes();
        }

        return result;
    }

    /**
     * Create charsets table.
     * @return byte array representing the Charsets
     */
    private byte[] createCharsets() {

        //Create array for result
        final byte[] result = new byte[(glyphNames.length*2)+1];

        //Leave first byte blank for format 0, then fill rest of array with 2-byte SIDs
        for (int i=0; i<glyphNames.length; i++) {
            final byte[] sid = FontWriter.setUintAsBytes(getSIDForString(glyphNames[i]), 2);

            result[1+(i*2)] = sid[0];
            result[2+(i*2)] = sid[1];
        }

        return result;
    }

    /**
     * Create Encodings table
     * @return byte array representing the Encodings
     */
    private static byte[] createEncodings() {
        return new byte[0];
    }

    /**
     * Create the Private dictionary
     * @return byte array representing the Private dict
     * @throws IOException if ByteOutputStream breaks
     */
    private byte[] createPrivateDict() {

        final FastByteArrayOutputStream bos = new FastByteArrayOutputStream();

        if (blueValues != null && blueValues.length > 0) {
            bos.write(CFFUtils.storeDeltas(blueValues));
            bos.write((byte)6);			        //BlueValues
        }

        if (otherBlues != null && otherBlues.length > 0) {
            bos.write(CFFUtils.storeDeltas(otherBlues));
            bos.write((byte)7);			        //OtherBlues
        }

        if (familyBlues != null && familyBlues.length > 0) {
            bos.write(CFFUtils.storeDeltas(familyBlues));
            bos.write((byte)8);			        //FamilyBlues
        }

        if (familyOtherBlues != null && familyOtherBlues.length > 0) {
            bos.write(CFFUtils.storeDeltas(familyOtherBlues));
            bos.write((byte)9);                 //FamilyOtherBlues
        }

        if (blueScale != null) {
            bos.write(CFFUtils.storeReal(blueScale));
            bos.write(new byte[]{12,9});        //BlueScale
        }

        if (blueShift != null && blueShift != 7) {
            bos.write(CFFUtils.storeInteger(blueShift));
            bos.write(new byte[]{12,10});       //BlueShift
        }

        if (blueFuzz != null && blueFuzz != 1) {
            bos.write(CFFUtils.storeInteger(blueFuzz));
            bos.write(new byte[]{12,11});       //BlueFuzz
        }

        if (stdHW != null) {
            bos.write(CFFUtils.storeReal(stdHW));
            bos.write((byte)10);		        //StdHW
        }

        if (stdVW != null) {
            bos.write(CFFUtils.storeReal(stdVW));
            bos.write((byte)11);			    //StdVW
        }

        if (stemSnapH != null && stemSnapH.length > 0) {
            bos.write(CFFUtils.storeDeltas(stemSnapH));
            bos.write(new byte[]{12,12});  		//StemSnapH
        }

        if (stemSnapV != null && stemSnapV.length > 0) {
            bos.write(CFFUtils.storeDeltas(stemSnapV));
            bos.write(new byte[]{12,13});  		//StemSnapV
        }

        if (forceBold != null && forceBold) {
            bos.write(CFFUtils.storeInteger(forceBold ? 1 : 0));
            bos.write(new byte[]{12,14});       //ForceBold
        }

        if (languageGroup != null && languageGroup != 0) {
            bos.write(CFFUtils.storeInteger(languageGroup));
            bos.write(new byte[]{12,17});       //LanguageGroup
        }

        //ExpansionFactor?
        //initialRandomSeed?

        bos.write(CFFUtils.storeInteger(defaultWidthX));
        bos.write((byte)20);                    //defaultWidthX

        bos.write(CFFUtils.storeInteger(nominalWidthX));
        bos.write((byte)21);                    //nominalWidthX

        return bos.toByteArray();
    }


    @Override
    public int getIntValue(final int i) {
        return -1;
    }

    /**
     * Return a list of the names of the glyphs in this font.
     * @return List of glyph names
     */
    public String[] getGlyphList() {
        return glyphNames;
    }

    /**
     * Return the widths of all of the glyphs of this font.
     * @return List of glyph widths
     */
    public int[] getWidths() {
        final int[] widths = new int[widthX.length];

        for (int i=0; i<widthX.length; i++) {
            if (widthX[i] == Integer.MIN_VALUE) {
                widths[i] = defaultWidthX;
            } else {
                widths[i] = widthX[i] + nominalWidthX;
            }
        }

        return widths;
    }

    /**
     * Return the left side bearings of all of the glyphs of this font.
     * @return List of left side bearings.
     */
    public int[] getBearings() {
        return lsbX;
    }

    /**
     * Return a bounding box calculated from the outlines.
     * @return the calculated bbox
     */
    public float[] getBBox() {
        return bbox;
    }

    /**
     * Return the size of the em square calculated from the outlines.
     * @return the calculated em square size
     */
    public double getScale() {
        return scale;
    }
}
