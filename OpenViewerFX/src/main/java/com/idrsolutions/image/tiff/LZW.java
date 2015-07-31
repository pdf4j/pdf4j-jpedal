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
 * LZW.java
 * ---------------
 */
package com.idrsolutions.image.tiff;

public class LZW {

    private static final int[] TABLE = new int[]{511, 1023, 2047, 4095};
    private int bp, op, tp, nextBits, nextData, bitsToGet = 9;
    private byte stringTable[][];
    private byte[] input, output;

    public void decompress(byte[] input, byte[] output) {
        init();
        this.input = input;
        this.output = output;

        // Initialize pointers
        bp = 0;
        op = 0;
        nextData = 0;
        nextBits = 0;
        int code, oldCode = 0;
        byte string[];
        while (((code = getNextCode()) != 257) && op < output.length) {
            if (code == 256) {
                init();
                code = getNextCode();
                if (code == 257) {
                    break;
                }
                writeString(stringTable[code]);
                oldCode = code;
            } else {
                if (code < tp) {
                    string = stringTable[code];
                    writeString(string);
                    addStringToTable(stringTable[oldCode], string[0]);
                    oldCode = code;
                } else {
                    string = stringTable[oldCode];
                    string = composeString(string, string[0]);
                    writeString(string);
                    addStringToTable(string);
                    oldCode = code;
                }
            }
        }
    }

    public int getNextCode() {
        try {
            nextData = (nextData << 8) | (input[bp++] & 0xff);
            nextBits += 8;
            if (nextBits < bitsToGet) {
                nextData = (nextData << 8) | (input[bp++] & 0xff);
                nextBits += 8;
            }
            final int code = (nextData >> (nextBits - bitsToGet)) & TABLE[bitsToGet - 9];
            nextBits -= bitsToGet;
            return code;
        } catch (final ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return 257;
        }
    }

    private static byte[] composeString(final byte[] oldString, final byte newString) {
        final int length = oldString.length;
        final byte[] string = new byte[length + 1];
        System.arraycopy(oldString, 0, string, 0, length);
        string[length] = newString;
        return string;
    }

    private void init() {
        stringTable = new byte[4096][];
        for (int i = 0; i < 256; i++) {
            stringTable[i] = new byte[1];
            stringTable[i][0] = (byte) i;
        }
        tp = 258;
        bitsToGet = 9;
    }

    private void writeString(final byte[] string) {
        for (final byte aString : string) {
            output[op++] = aString;
        }
    }

    private void addStringToTable(final byte[] oldString, final byte newString) {
        final int length = oldString.length;
        final byte[] string = new byte[length + 1];
        System.arraycopy(oldString, 0, string, 0, length);
        string[length] = newString;

        stringTable[tp++] = string;
        if (tp == 511) {
            bitsToGet = 10;
        } else if (tp == 1023) {
            bitsToGet = 11;
        } else if (tp == 2047) {
            bitsToGet = 12;
        }
    }

    private void addStringToTable(final byte[] string) {

        stringTable[tp++] = string;
        if (tp == 511) {
            bitsToGet = 10;
        } else if (tp == 1023) {
            bitsToGet = 11;
        } else if (tp == 2047) {
            bitsToGet = 12;
        }
    }
}
