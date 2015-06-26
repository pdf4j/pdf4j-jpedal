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
 * HheaWriter.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.fonts.tt.Hhea;

import org.jpedal.utils.repositories.FastByteArrayOutputStream;
import java.io.IOException;

public class HheaWriter extends Hhea implements FontTableWriter{

    private final int numberOfHMetrics;
    private final double xMaxExtent;
    private final double minRightSideBearing;
    private final double minLeftSideBearing;
    private final double advanceWidthMax;
    private final double lowestDescender;
    private final double highestAscender;

    public HheaWriter(final double xMaxExtent, final double minRightSideBearing, final double minLeftSideBearing, final double advanceWidthMax, final double lowestDescender, final double highestAscender, final int numberOfHMetrics) {
        this.xMaxExtent = xMaxExtent;
        this.minRightSideBearing = minRightSideBearing;
        this.minLeftSideBearing = minLeftSideBearing;
        this.advanceWidthMax = advanceWidthMax;
        this.lowestDescender = lowestDescender;
        this.highestAscender = highestAscender;
        this.numberOfHMetrics = numberOfHMetrics;
    }

    @Override
    public byte[] writeTable() throws IOException {

        final FastByteArrayOutputStream bos=new FastByteArrayOutputStream();

        bos.write(FontWriter.setNextUint32(65536));                           //version               65536

        //Designer specified values
        bos.write(FontWriter.setFWord((int) highestAscender));                 //ascender
        bos.write(FontWriter.setFWord((int) lowestDescender));                 //descender
        bos.write(FontWriter.setFWord(0));                                    //lineGap               0

        //Calculated values
        bos.write(FontWriter.setUFWord((int) advanceWidthMax));                //advanceWidthMax
        bos.write(FontWriter.setFWord((int) minLeftSideBearing));              //minLeftSideBearing
        bos.write(FontWriter.setFWord((int) minRightSideBearing));             //minRightSideBearing
        bos.write(FontWriter.setFWord((int) xMaxExtent));                      //xMaxExtent

        //Italicise caret?
        bos.write(FontWriter.setNextInt16(1));                                //caretSlopeRise        1
        bos.write(FontWriter.setNextInt16(0));                                //caretSlopeRun         0
        bos.write(FontWriter.setFWord(0));                                    //caretOffset           0

        //reserved values
        for( int i = 0; i < 4; i++ ) {
            bos.write(FontWriter.setNextUint16(0)); //0
        }

        bos.write(FontWriter.setNextInt16(0));//metricDataFormat
        bos.write(FontWriter.setNextUint16(numberOfHMetrics)); //count

        return bos.toByteArray();
    }

    @Override
    public int getIntValue(final int key) {
        return 0;
    }
}
