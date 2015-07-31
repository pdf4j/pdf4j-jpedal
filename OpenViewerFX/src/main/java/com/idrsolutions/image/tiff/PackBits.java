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
 * PackBits.java
 * ---------------
 */
package com.idrsolutions.image.tiff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PackBits {
    
    public static byte[] decompress(byte[] input, int expected) throws IOException{
 	int total = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length*2);
        int i = 0;
        while (total < expected) {
            if (i >= input.length) {
                throw new IOException("Error in packbit decompression ");
            }
            int n = input[i++];
            if ((n >= 0) && (n <= 127)) {
                int cc = n + 1;
                total += cc;
                for (int j = 0; j < cc; j++) {
                    bos.write(input[i++]);
                }
            } else if ((n >= -127) && (n <= -1)) {
                int b = input[i++];
                int count = -n + 1;

                total += count;
                for (int j = 0; j < count; j++) {
                    bos.write(b);
                }
            } else if (n == -128) {
                throw new IOException("Error in packbit decompression ");
            }
        }
        return bos.toByteArray();
    }

}
