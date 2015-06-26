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
 * CFFFixer.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.utils.repositories.FastByteArrayOutputStream;
import java.util.ArrayList;

public class CFFFixer {

    private byte[] data;
    private final byte[] original;
    private int charstringOffset = -1;

    private int privateOffset = -1;
    private int privateOffsetLocation = -1;
    private int privateOffsetLength = -1;
    private int privateLength = -1;
    private int privateLengthLocation = -1;
    private int privateLengthLength = -1;

    private int[] charstringXDisplacement, charstringYDisplacement;
    private final float[] bbox = new float[4];
    private final String name;

    private final double scale;

    //

    CFFFixer(final byte[] cff, final String name, double scale) {

        this.name = name;
        this.scale = scale;
        data = cff;
        original = new byte[cff.length];
        System.arraycopy(cff,0,original,0,cff.length);

        //Fix any problems
        fixData();
    }

    /**
     * Check more for problems and fix them if found.
     */
    private void fixData() {
        try{

            //Find top dict index
            final int nameIndexStart = data[2];
            final int nameIndexCount = FontWriter.getUintFromByteArray(data, nameIndexStart, 2);
            final int nameIndexOffsize = data[nameIndexStart+2];
            final int nameIndexEndOffsetLocation = nameIndexStart+3+(nameIndexOffsize*nameIndexCount);
            final int nameIndexEndOffset = FontWriter.getUintFromByteArray(data, nameIndexEndOffsetLocation, nameIndexOffsize);

            //Find top dict
            final int dictIndexStart = nameIndexEndOffsetLocation+nameIndexEndOffset+(nameIndexOffsize-1);
            final int topDictIndexCount = FontWriter.getUintFromByteArray(data, dictIndexStart, 2);
            if (topDictIndexCount != 1) {
                //
                data = original;
                return;
            }
            final int topDictIndexOffsize = data[dictIndexStart+2];
            final int topDictIndexEndOffsetLocation = dictIndexStart + 3 +(topDictIndexOffsize*topDictIndexCount);
            final int topDictIndexEndOffset = FontWriter.getUintFromByteArray(data, topDictIndexEndOffsetLocation, topDictIndexOffsize);

            //Decode top dict to find some key values
            final int topDictDataStart = topDictIndexEndOffsetLocation+topDictIndexOffsize;
            final byte[] topDict = new byte[topDictIndexEndOffset-1];
            System.arraycopy(data, topDictDataStart, topDict, 0, topDictIndexEndOffset - 1);
            final boolean decodedSuccessfully = decodeDict(topDict);

            //If we failed to decode the top dict, give up
            if (!decodedSuccessfully) {
                return;
            }

            //Copy Top Dict back in with any changes
            System.arraycopy(topDict, 0, data, topDictDataStart, topDictIndexEndOffset - 1);

            /**
             * Check for unknown keys (12 15) in the Private DICT.
             */
            boolean possibleUnknownCommand = false;
            int i=0;
            while (!possibleUnknownCommand && i < data.length-1) {
                if (data[i] == 12 && data[i+1] == 15) {
                    possibleUnknownCommand = true;
                }
                i++;
            }

            if (privateOffset != -1) {
                //Get private dict
                final byte[] privateDict = new byte[privateLength];
                System.arraycopy(data, privateOffset, privateDict, 0, privateLength);

                //Cast to int array so highest byte isn't negative
                int[] d = new int[privateDict.length];
                for (int j=0; j<privateDict.length; j++) {
                    d[j] = privateDict[j] < 0 ? privateDict[j] + 256 : privateDict[j];
                }


                int lastNumLength;
                int lastOpLength;

                final ArrayList<Integer> newDict = new ArrayList<Integer>();
                ArrayList<Integer> currentKey = new ArrayList<Integer>();

                int subrsOffsetLoc = -1;
                int subrsOffsetSize = -1;

                int pointer = 0;
                while (pointer < d.length) {
                    final int chunk = d[pointer];

                    if (chunk >=32 && chunk <=246) {                                            //Number
                        currentKey.add(chunk);
                        lastNumLength = 1;
                        lastOpLength = 0;

                    } else if (chunk >= 247 && chunk <= 250 || chunk >= 251 && chunk <= 254) {  //Number
                        currentKey.add(chunk);
                        currentKey.add(d[pointer+1]);
                        lastNumLength = 2;
                        lastOpLength = 0;

                    } else if (chunk == 28) {                                                   //Number
                        currentKey.add(chunk);
                        currentKey.add(d[pointer+1]);
                        currentKey.add(d[pointer+2]);
                        lastNumLength = 3;
                        lastOpLength = 0;

                    } else if (chunk == 29) {                                                   //Number
                        currentKey.add(chunk);
                        currentKey.add(d[pointer+1]);
                        currentKey.add(d[pointer+2]);
                        currentKey.add(d[pointer+3]);
                        currentKey.add(d[pointer+4]);
                        lastNumLength = 5;
                        lastOpLength = 0;

                    } else if (chunk == 30) {                                                   //Number
                        currentKey.add(chunk);
                        lastNumLength = 1;
                        while (!(((d[pointer+lastNumLength] & 0xF) == 0xF))) {
                            currentKey.add(d[pointer+lastNumLength]);
                            lastNumLength++;
                        }
                        currentKey.add(d[pointer+lastNumLength]);
                        lastNumLength++;
                        lastOpLength = 0;

                    } else if (chunk == 12) {                //Two byte ID's
                        final int subKey = d[pointer+1];
                        if (subKey != 15 && !currentKey.isEmpty()) {
                            currentKey.add(chunk);
                            currentKey.add(subKey);
                            newDict.addAll(currentKey);
                        }
                        currentKey = new ArrayList<Integer>();
                        lastOpLength = 2;
                        lastNumLength = 0;
                    } else {                                 //One byte command
                        if (!currentKey.isEmpty()) {

                            if (chunk == 19) {  //Store reference to subrs key so we can reduce
                                subrsOffsetLoc = (newDict.size() - currentKey.size()) + 1;
                                subrsOffsetSize = currentKey.size();
                            }

                            currentKey.add(chunk);
                            newDict.addAll(currentKey);
                        }
                        currentKey = new ArrayList<Integer>();
                        lastOpLength = 1;
                        lastNumLength = 0;
                    }

                    pointer += lastOpLength + lastNumLength;
                }

                //2 bytes for command + however many for parameter
                final int bytesRemoved = d.length - newDict.size();

                //Convert to array
                d = new int[newDict.size()];
                for (int j=0; j<newDict.size(); j++) {
                    d[j] = newDict.get(j);
                }

                //Update subr offsets
                if (subrsOffsetLoc != -1 && subrsOffsetSize > 0) {
                    int offset = CFFUtils.read1cInteger(d, subrsOffsetLoc);
                    offset -= bytesRemoved;
                    byte[] newOffset = CFFUtils.storeInteger(offset);
                    newOffset = pad1cNumber(subrsOffsetSize, newOffset, offset);
                    for (int j=0; j<subrsOffsetSize; j++) {
                        d[subrsOffsetLoc+j] = newOffset[j];
                    }
                }

                //Reassemble font data
                final byte[] newData = new byte[data.length-bytesRemoved];
                System.arraycopy(data,0,newData,0,privateOffset);
                for (int j=0; j<d.length; j++) {
                    newData[privateOffset+j] = (byte)d[j];
                }
                System.arraycopy(data,privateOffset+privateLength,newData,privateOffset+d.length,data.length-(privateOffset+privateLength));
                data = newData;

                //Update the Private DICT length (if there is one)
                if (privateLength != -1 && privateLengthLength != -1 && privateLengthLocation != -1) {
                    byte[] newPrivateLength = CFFUtils.storeInteger(privateLength - bytesRemoved);
                    newPrivateLength = pad1cNumber(privateLengthLength, newPrivateLength, privateLength - bytesRemoved);
                    System.arraycopy(newPrivateLength, 0, data, topDictDataStart+privateLengthLocation, privateLengthLength);
                }
            }


            //Rewrite the charstrings (removes unknown commands and unused operands)
            rewriteCharstrings(topDictDataStart);


        }catch(final Exception e) {
            //
            data = original;
        }
    }


    /**
     * Decodes the charstrings to an intermediary format, and then writes them back out. This removes unwanted commands
     * like dotsection, ensures they end properly, and removes unused data.
     * @param topDictDataStart Offset for the start of top DICT data
     */
    private void rewriteCharstrings(final int topDictDataStart) {
        if (charstringOffset == -1) {
            //<start-demo><end-demo>
            data = original;
            return;
        }

        final int charstringsCount = FontWriter.getUintFromByteArray(data, charstringOffset, 2);
        final int charstringsOffsize = data[charstringOffset+2];
        final int charstringsOffsetsStart = charstringOffset + 3;
        final int[] charstringOffsets = new int[charstringsCount+1];
        for (int i=0; i<charstringsCount+1; i++) {
            charstringOffsets[i] = FontWriter.getUintFromByteArray(data, charstringsOffsetsStart + (charstringsOffsize * i), charstringsOffsize);
        }
        final int charstringDataStart = charstringsOffsetsStart+((charstringsCount+1)*charstringsOffsize);

        final CharstringContext context = new CharstringContext();
        final byte[][] charstrings = new byte[charstringsCount][];
        charstringXDisplacement = new int[charstringsCount];
        charstringYDisplacement = new int[charstringsCount];

        //Read and rewrite charstrings
        for (int i=0; i<charstringsCount; i++) {

            context.setCharstring(i);

            final int start = charstringDataStart + charstringOffsets[i] - 1;
            final int end = charstringDataStart + charstringOffsets[i+1] - 1;
            final int[] cs = new int[end-start];
            for (int j=0; j<cs.length; j++) {
                cs[j] = data[start+j];

                if (cs[j] < 0) {
                    cs[j] += 256;
                }
            }

            //Convert to CharstringElements
            CharstringContext.CharstringElement element;
            for (int j=0; j < cs.length; j+=element.getLength()) {
                element = context.createElement(cs, j);
            }

            final ArrayList<CharstringContext.CharstringElement> currentCharString = context.getCurrentCharString();

            //Apply scaling & calculate and store displacement and bbox
            for (final CharstringContext.CharstringElement e : currentCharString) {
                e.scale(scale);

                final int[] d = e.getDisplacement();
                charstringXDisplacement[i] += d[0];
                charstringYDisplacement[i] += d[1];
                bbox[0] = charstringXDisplacement[i] < bbox[0] ? charstringXDisplacement[i] : bbox[0];
                bbox[1] = charstringYDisplacement[i] < bbox[1] ? charstringYDisplacement[i] : bbox[1];
                bbox[2] = charstringXDisplacement[i] > bbox[2] ? charstringXDisplacement[i] : bbox[2];
                bbox[3] = charstringYDisplacement[i] > bbox[3] ? charstringYDisplacement[i] : bbox[3];
            }

            final FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
            for (final CharstringContext.CharstringElement e : currentCharString) {
                bos.write(e.getType2Bytes());
            }

            charstrings[i] = bos.toByteArray();
        }

        //Create index
        final byte[] charstringsIndex = CFFUtils.createIndex(charstrings);
        final int endOfCharstringData = (charstringDataStart + charstringOffsets[charstrings.length] - 1);
        final int bytesRemoved = (endOfCharstringData - charstringOffset) - charstringsIndex.length;

        //Replace original charstrings index
        final byte[] newData = new byte[data.length - bytesRemoved];
        System.arraycopy(data, 0, newData, 0, charstringOffset);
        System.arraycopy(charstringsIndex, 0, newData, charstringOffset, charstringsIndex.length);
        System.arraycopy(data, endOfCharstringData, newData, charstringOffset + charstringsIndex.length, data.length - endOfCharstringData);
        data = newData;

        //Update the Private DICT offset (if there is one)
        if (privateOffset != -1 && privateOffset > charstringOffset && privateOffsetLength != -1 && privateOffsetLocation != -1) {
            byte[] newPrivateOffset = CFFUtils.storeInteger(privateOffset - bytesRemoved);
            newPrivateOffset = pad1cNumber(privateOffsetLength, newPrivateOffset, privateOffset - bytesRemoved);
            System.arraycopy(newPrivateOffset, 0, data, topDictDataStart+privateOffsetLocation, privateOffsetLength);
            privateOffset -= bytesRemoved;
        }

        //Rescale defaultWidthX and nominalWidthX if necessary
        if (scale != 1) {
            byte[] privateDict = new byte[privateLength];
            System.arraycopy(data, privateOffset, privateDict, 0, privateLength);
            decodeDict(privateDict);
            System.arraycopy(privateDict, 0, data, privateOffset, privateLength);
        }
    }

    /**
     * Decode the Top DICT for some required values
     * @param dict the dictionary
     * @return Whether the decode was successful
     */
    private boolean decodeDict(final byte[] dict) {

        //Cast to int array so highest byte isn't negative
        final int[] d = new int[dict.length];
        for (int i=0; i<dict.length; i++) {
            d[i] = dict[i] < 0 ? dict[i] + 256 : dict[i];
        }

        int pointer = 0;
        ArrayList<Integer> lastNumLength = new ArrayList<Integer>();
        ArrayList<Integer> lastNum = new ArrayList<Integer>();
        int lastOpLength;

        //Run through array picking out only the bits of data we need
        while (pointer < d.length) {
            final int chunk = d[pointer];

            if (chunk >=32 && chunk <=246) {                                   //Number
                lastNum.add(chunk - 139);
                lastNumLength.add(1);
                lastOpLength = 0;
            } else if (chunk >= 247 && chunk <= 250) {                         //Number
                lastNum.add(((chunk - 247) * 256) + d[pointer+1] + 108);
                lastNumLength.add(2);
                lastOpLength = 0;
            } else if (chunk >= 251 && chunk <= 254) {                         //Number
                lastNum.add(-((chunk - 251) * 256) - d[pointer+1] - 108);
                lastNumLength.add(2);
                lastOpLength = 0;
            } else if (chunk == 28) {                                          //Number
                lastNum.add(FontWriter.getUintFromIntArray(d, pointer + 1, 2));
                lastNumLength.add(3);
                lastOpLength = 0;
            } else if (chunk == 29) {                                          //Number
                lastNum.add(FontWriter.getUintFromIntArray(d, pointer + 1, 4));
                lastNumLength.add(5);
                lastOpLength = 0;
            } else if (chunk == 30) {                                          //Number
                int len = 1;
                while (!(((d[pointer+len] & 0xF) == 0xF))) {
                    len++;
                }
                len++;
                lastNumLength.add(len);
                lastNum.add(0);
                lastOpLength = 0;
            } else if (chunk == 12) {                //Two byte ID's - we're usually not interested in these
                if (d[pointer+1] == 36) {            //Font DICT - we'll probably need to change this offset so store location and length and value
                    //
                    data = original;
                    return false;

                } else if (d[pointer+1] == 7) {      //FontMatrix - we need to set this to default values and apply the scaling elsewhere due to Android Chrome
                    int total = 0;
                    for (int i=1; i<=6; i++) {
                        total += lastNumLength.get(lastNumLength.size()-i);
                    }

                    int fontMatrixStart = pointer-total;

                    //Replace first number with 0.001
                    dict[fontMatrixStart+1] = (byte)0xa0;
                    dict[fontMatrixStart+2] = (byte)0x01;
                    for (int i=0; i<lastNumLength.get(lastNumLength.size()-6)-4; i++) {
                        dict[fontMatrixStart+3+i] = 0;
                    }
                    dict[fontMatrixStart+lastNumLength.get(lastNumLength.size()-6)-1] = (byte)0xFF;

                    //Replace fourth number with 0.001
                    int nextStart = fontMatrixStart + lastNumLength.get(lastNumLength.size()-6) + lastNumLength.get(lastNumLength.size()-5) + lastNumLength.get(lastNumLength.size()-4);
                    dict[nextStart+1] = (byte)0xa0;
                    dict[nextStart+2] = (byte)0x01;
                    for (int i=0; i<lastNumLength.get(lastNumLength.size()-3)-4; i++) {
                        dict[nextStart+3+i] = 0;
                    }
                    dict[nextStart+lastNumLength.get(lastNumLength.size()-3)-1] = (byte)0xFF;
                }

                lastOpLength = 2;
                lastNumLength.add(0);
            } else if (chunk == 16) {                //Encoding - fix the format if it's not 0 or 1
                if (data[lastNum.get(lastNum.size()-1)] != 0 && data[lastNum.get(lastNum.size()-1)] != 1) {
                    data[lastNum.get(lastNum.size()-1)] = 1;
                }
                lastOpLength = 1;
                lastNumLength.add(0);
            } else if (chunk == 17) {                //Charstrings - we need this so we can change the strings
                charstringOffset = lastNum.get(lastNum.size()-1);
                lastOpLength = 1;
                lastNumLength.add(0);
            } else if (chunk == 18) {                //Private dict - we'll probably need to change this offset so store location and length and value
                int numberByteCount = lastNumLength.get(lastNumLength.size() - 1);
                privateOffsetLocation = pointer - numberByteCount;
                privateOffsetLength = numberByteCount;
                privateOffset = lastNum.get(lastNum.size()-1);
                privateLengthLocation = pointer - numberByteCount - lastNumLength.get(lastNumLength.size()-2);
                privateLengthLength = lastNumLength.get(lastNumLength.size()-2);
                privateLength = lastNum.get(lastNum.size()-2);

                lastOpLength = 1;
                lastNumLength.add(0);
            } else if (chunk == 20) {               //defaultWidthX - we need to rescale this if we're getting rid of a non-default fontMatrix
                if (scale != 1) {
                    int numberByteCount = lastNumLength.get(lastNumLength.size() - 1);
                    int newDefault = (int)((lastNum.get(lastNum.size()-1)*scale)+0.5d);
                    byte[] bytes = CFFUtils.storeInteger(newDefault);
                    byte[] newBytes = pad1cNumber(numberByteCount, bytes, newDefault);
                    System.arraycopy(newBytes, 0, dict, pointer-numberByteCount, numberByteCount);
                }

                lastOpLength = 1;
                lastNumLength.add(0);
            } else if (chunk == 21) {               //nominalWidthX - we need to rescale this if we're getting rid of a non-default fontMatrix
                if (scale != 1) {
                    int numberByteCount = lastNumLength.get(lastNumLength.size() - 1);
                    int newNominal = (int)((lastNum.get(lastNum.size()-1)*scale)+0.5d);
                    byte[] bytes = CFFUtils.storeInteger(newNominal);
                    byte[] newBytes = pad1cNumber(numberByteCount, bytes, newNominal);
                    System.arraycopy(newBytes, 0, dict, pointer-numberByteCount, numberByteCount);
                }

                lastOpLength = 1;
                lastNumLength.add(0);
            } else {
                lastOpLength = 1;                    //Other one byte command - not interested
                lastNumLength.add(0);
            }

            pointer += lastOpLength + lastNumLength.get(lastNumLength.size()-1);
        }
        return true;
    }

    /**
     * Return the bounding box generated by reading the charstrings
     * @return float array with minX, minY, maxX and maxY
     */
    public float[] getBBox() {
        return bbox;
    }

    /**
     * Pads a type 1c number to take up the required number of bytes.
     * @param byteCount The number of bytes required.
     * @param currentRepresentation The current representation of the number.
     * @param number The number to encode.
     * @return The representation using the required number of bytes.
     */
    private static byte[] pad1cNumber(final int byteCount, final byte[] currentRepresentation, final int number) {

        //Check if already the same
        if (byteCount == currentRepresentation.length) {
            return currentRepresentation;
        }

        if (byteCount < currentRepresentation.length) {
            throw new IllegalArgumentException("Trying to pad a number to a smaller size. (" + byteCount + " < " + currentRepresentation.length + ')');
        }

        if (byteCount != 2 && byteCount != 3 && byteCount != 5) {
            throw new IllegalArgumentException("Padding to an incorect number of bytes. (" + byteCount + ')');
        }

        if (byteCount == 2) {                      //must be padding from 1 so just append a zero beforehand
            return new byte[]{(byte) 139,
                    currentRepresentation[0]};
        } else if (byteCount == 3) {                 //Generate as 2 byte number
            return new byte[]{28,
                    (byte) ((number >> 8) & 0xFF),
                    (byte) (number & 0xFF)};
        }

        //byteCount must be 5 - Generate as 4 byte number
        return new byte[]{29,
                (byte) ((number >> 24) & 0xFF),
                (byte) ((number >> 16) & 0xFF),
                (byte) ((number >> 8) & 0xFF),
                (byte) (number & 0xFF)};
    }

    /**
     * @return the fixed font
     */
    public byte[] getBytes() {
        return data;
    }

    //
}
