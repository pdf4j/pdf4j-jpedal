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
 * IFD.java
 * ---------------
 */
package com.idrsolutions.image.tiff;

public class IFD {

    public int imageWidth;
    public int imageHeight;
    public int samplesPerPixel = 1;
    public int[] bps = {0};
    public int compressionType;
    public int photometric;
    public int rowsPerStrip;
    public int[] stripOffsets;
    public int[] stripByteCounts;
    public int fillOrder = 1;
    public int nextIFD;
    public byte[] colorMap;
    public int planarConfiguration = 1;
    public int tileWidth;
    public int tileLength;
    public int[] tileOffsets;
    public int[] tileByteCounts;
    public int predictor = 1;
    public int[] sampleFormat = {1,1,1};
    public byte[] iccProfile;
    
    //old jpeg tables
    public int[] jpegQOffsets;
    public int[] jpegDCOffsets;
    public int[] jpegACOffsets;
    public byte[][] jpegQData;
    public byte[][] jpegDCData;
    public byte[][] jpegACData;
    public int[] jpegFrequency = {2,1,1};
    //technote 2 jpegtables
    public byte[] jpegTables;
    
            
    @Override
    public String toString() {
        String str = "ImageWidth: " + imageWidth + '\n'
                + "ImageHeight: " + imageHeight + '\n'
                + "SamplesPerPixel: " + samplesPerPixel + '\n'
                + "CompressionType: " + compressionType + '\n'
                + "Photomtric: " + photometric + '\n'
                + "RowsPerStrip: " + rowsPerStrip + '\n'
                + "PlanarConfig: " + planarConfiguration + '\n'
                + "FillOrder: " + fillOrder + '\n'
                + "bits per sample: ";
        for (int i = 0; i < bps.length; i++) {
            str += "\t"+bps[i];
        }
        str+="\n";
        if(tileOffsets!=null){
            str += "Tile Width: "+tileWidth+"\n Tile Height: "+tileLength+ '\n';
        }
        
        return str;
    }
}
