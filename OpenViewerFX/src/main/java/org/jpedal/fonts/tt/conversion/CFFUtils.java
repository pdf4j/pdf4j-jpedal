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
 * CFFUtils.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.utils.repositories.FastByteArrayOutputStream;

/**
 * Contains static methods associated with CFF files.
 */
public class CFFUtils {

    /**
     * Create an index as a byte array
     * @param data Array of byte array data chunks
     * @return Byte array of index
     * @throws java.io.IOException if cannot write data
     */
    public static byte[] createIndex(final byte[][] data) {

        //Check for null data
        if (data == null) {
            return new byte[]{0,0};
        }

        final int count = data.length;

        //Check for empty index
        if (count == 0) {
            return new byte[]{0,0};
        }

        //Generate offsets
        final int[] offsets = new int[count+1];
        offsets[0] = 1;
        for (int i=1; i<count+1; i++) {
            final byte[] cs = data[i-1];
            if (cs != null) {
                offsets[i] = offsets[i-1] + cs.length;
            } else {
                offsets[i] = offsets[i-1];
            }
        }
        //Generate offSize
        final int offSize = getOffsizeForMaxVal(offsets[count]);

        final int len = 3+(offSize*offsets.length)+offsets[count];
        final FastByteArrayOutputStream bos = new FastByteArrayOutputStream(len);

        //Write out
        bos.write(FontWriter.setNextUint16(count));                       //count
        bos.write(FontWriter.setNextUint8(offSize));                      //offSize
        for (final int offset : offsets) {
            bos.write(FontWriter.setUintAsBytes(offset, offSize));          //offsets
        }
        for (final byte[] item : data) {
            if (item != null) {
                bos.write(item);                                             //data
            }
        }

        return bos.toByteArray();
    }


    /**
     * Calculate the offSize required to encode a value
     * @param i Max value to encode
     * @return Number of bytes required
     */
    private static byte getOffsizeForMaxVal(int i) {
        byte result = 1;
        while (i > 256) {
            result++;
            i /= 256;
        }

        return result;
    }

    /**
     * Creates a Type 1c number as a byte array.
     * @param num Number to encode
     * @return byte array of number in type 1c format
     */
    static byte[] storeInteger(int num) {
        final byte[] result;

        if (num >= -107 && num <= 107) {
            result = new byte[]{(byte)(num + 139)};

        } else if (num >= 108 && num <= 1131) {
            num -= 108;
            result = new byte[]{(byte)(247+(num/256)),
                    (byte)(num & 0xFF)};

        } else if (num >= -1131 && num <= -108) {
            num += 108;
            result = new byte[]{(byte)(251+(num/-256)),
                    (byte)(-num & 0xFF)};

        } else if (num >= -32768 && num <= 32767) {
            result = new byte[]{28,
                    (byte)((num/256)&0xFF),
                    (byte)(num&0xFF)};

        } else {
            result = new byte[]{29,
                    (byte)((((num/256)/256)/256)&0xFF),
                    (byte)(((num/256)/256)&0xFF),
                    (byte)((num/256)&0xFF),
                    (byte)(num&0xFF)};
        }

        return result;
    }

    /**
     * Creates a real type 1c as a byte array.
     * @param num Number to encode
     * @return byte array of number in type 1c real format
     */
    static byte[] storeReal(final double num) {
        String n = Double.toString(num);

        //Reduce length of number
        final int maxLength = 10;
        if (n.length() > maxLength) {
            if (n.contains("E")) {
                final String[] parts = n.split("E");
                n = n.substring(0, maxLength - (parts[1].length() + 1))+'E'+parts[1];
            } else {
                n = n.substring(0, maxLength);
            }
        }

        //Append f to end
        n += 'f';

        //Find length in bytes
        int len = n.length();
        if ((len%2) == 1) {
            len++;
        }
        len /= 2;
        final byte[] result = new byte[1+len];

        result[0] = 30;

        //Add nibbles
        for (int i=0; i<len; i++) {
            final int charLoc = i*2;
            final char a = n.charAt(charLoc);
            final char b;
            if (charLoc+1 < n.length()) {
                b = n.charAt(charLoc+1);
            } else {
                b = a;
            }

            final byte aByte = getNibble(a);
            final byte bByte = getNibble(b);

            result[i+1] = (byte)(((aByte & 0xF) << 4) | (bByte & 0xF));
        }

        return result;
    }


    /**
     * Store an int array as delta. (Each element except the first is the difference between them.)
     * @param deltas The int[] to encode
     * @return A byte[] representing the int[] as deltas
     */
    static byte[] storeDeltas(final int[] deltas) {
        final FastByteArrayOutputStream bos = new FastByteArrayOutputStream();

            bos.write(storeInteger(deltas[0]));
            for (int i=1; i<deltas.length; i++) {
                bos.write(CFFUtils.storeInteger(deltas[i]-deltas[i-1]));
            }


        return bos.toByteArray();
    }

    /**
     * Creates a Charstring Type 2 number as a byte array.
     * @param num Number to encode
     * @return byte array of number in charstring type 2 format
     */
    static byte[] storeCharstringType2Integer(int num) {
        final byte[] result;

        if (num >= -107 && num <= 107) {
            result = new byte[]{(byte)(num + 139)};

        } else if (num >= 108 && num <= 1131) {
            num -= 108;
            result = new byte[]{(byte)(247+(num/256)),
                    (byte)(num & 0xFF)};

        } else if (num >= -1131 && num <= -108) {
            num += 108;
            result = new byte[]{(byte)(251+(num/-256)),
                    (byte)(-num & 0xFF)};

        } else {
            if (num >= 0) {
                result = new byte[]{(byte)255,
                        (byte)((num/256)&0xFF),
                        (byte)(num&0xFF),
                        0,
                        0};
            } else {
                final int add = num + 32768;
                result = new byte[]{(byte)255,
                        (byte)(0x80 | ((add/256)&0x7F)),
                        (byte)(add & 0xFF),
                        0,
                        0};
            }
        }

        return result;
    }

    /**
     * Get the nibble which represents a character in a type 1c real number
     * @param c Character to represent
     * @return Representation as nibble in bottom 4 bits
     */
    private static byte getNibble(final char c) {
        switch (c) {
            case '.':
                return 0xa;
            case 'E':
                return 0xb;
            case '-':
                return 0xe;
            case 'f':
                return 0xf;
            default:
                return (byte)Integer.parseInt(String.valueOf(c));
        }
    }

    /**
     * Read an integer number from a Type 1c dictionary
     * @param data the font
     * @param offset the location the number starts at
     * @return the number as an integer
     */
    public static int read1cInteger(final int[] data, final int offset) {
        final int b = data[offset];

        if (b >= 32 && b <= 246) {                                      //Single byte number

            return b - 139;

        } else if((b >= 247 && b <= 250) || (b >= 251 && b <= 254)) {   //Two byte number

            if (b < 251) {
                return ((b - 247) * 256) + data[offset+1] + 108;
            } else {
                return -((b - 251) * 256) - data[offset+1] - 108;
            }
        } else if (b == 28) {
            int numberValue = (data[offset+2] & 0xFF) +
                    ((data[offset+1] & 0xFF) << 8);
            if ((data[offset+1] & 0x80) == 0x80) {
                numberValue -= 0x10000;
            }
            return numberValue;
        } else if (b == 29) {
            return (data[offset+4] & 0xFF) +
                    ((data[offset+3] & 0xFF) << 8) +
                    ((data[offset+2] & 0xFF) << 16) +
                    ((data[offset+1] & 0xFF) << 24);
        }

        return 0;
    }
}
