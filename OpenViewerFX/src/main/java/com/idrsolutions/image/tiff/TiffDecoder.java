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
 * TiffEncoder.java
 * ---------------
 */
package com.idrsolutions.image.tiff;

import com.idrsolutions.image.jpeg2000.JPXBitReader;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Class reads Tiff image as BufferedImage
 * <p>
 * Here is an example of how the code can be used:-
 * </p>
 * <br>
 * <pre><code>
 * TiffDecoder decoder = new TiffDecoder(rawTiffData);
 * BufferedImage decodedImage = decoder.read();
 * </code></pre>
 * <p>
 * Here is an example of how the code can be used to extract all images from
 * multi page tiff file:-
 * </p>
 * <br>
 * <pre><code>
 * TiffDecoder decoder = new TiffDecoder(rawTiffData);
 * for(int i=0;i&lt;decoder.getPageCount();i++){
 *      BufferedImage decodedImage = decoder.read(i+1);//page number should start with 1;
 *      //insert your bufferimage handling code here;
 * }
 * </code></pre>
 *
 */
public class TiffDecoder {

    private final ByteBuffer reader;
    private int pageCount = 0;
    List<IFD> ifds = new ArrayList<IFD>();

    public TiffDecoder(byte[] rawTiffData) throws Exception {
        reader = ByteBuffer.wrap(rawTiffData);
        int endian = 1;

        int a = reader.get() & 0xff;
        int b = reader.get() & 0xff;
        if (a == 77 && b == 77) {
            reader.order(ByteOrder.BIG_ENDIAN);
        } else if (a == 73 && b == 73) {
            endian = 2;
            reader.order(ByteOrder.LITTLE_ENDIAN);
        }
        int magicNumber = reader.getShort();//42 magic number;
        if (magicNumber != 42) {
            throw new Exception("This is not a valid Tiff File");
        }
        int ifdOffset = reader.getInt();

        while (ifdOffset != 0) {
            IFD ifd = getIFD(reader, ifdOffset);
            ifds.add(ifd);
            ifdOffset = ifd.nextIFD;
            pageCount++;
        }
    }

    /**
     * decodes Tiff image data as BufferedImage. Make NO assumptions about type
     * of BufferedImage type returned (may change)
     *
     * @return BufferedImage to read image
     * @throws IOException
     */
    public BufferedImage read() throws Exception {
        return read(1);
    }

    /**
     * Method capable of decoding single image from multi page tiff file;
     * <strong>Please Note: pageNumber start from 1</strong>
     *
     * @param pageNumber the page number to be decoded (starting from 1)
     * @return BufferedImage to read image at given page number
     * @throws Exception
     */
    public BufferedImage read(int pageNumber) throws Exception {
        if (pageNumber == 0) {
            throw new Exception("PageNumber should start from 1");
        } else if (pageNumber > pageCount) {
            throw new Exception("PageNumber should not be greater than Total page count");
        }
        IFD ifd = ifds.get(pageNumber - 1);
        return generateImageFromIFD(reader, ifd);
    }

    private static IFD getIFD(final ByteBuffer reader, int ifdOffset) {
        reader.position(ifdOffset);
        int nEntries = reader.getShort();
        IFD ifd = new IFD();

        for (int i = 0; i < nEntries; i++) {
            int fieldName = reader.getShort() & 0xffff;
            int fieldType = reader.getShort() & 0xffff;
            int nValues = reader.getInt();

//            System.out.println(fieldName + " " + fieldType + " " + nValues);
            switch (fieldName) {
                case Tags.NewSubfileType:
                    reader.getInt();
                    break;
                case Tags.SubfileType:
                    reader.getShort();//read and ignore;
                    reader.getShort();//read and ignore;
                    break;
                case Tags.ImageWidth:
                    if (fieldType == 3) {
                        ifd.imageWidth = reader.getShort();
                        reader.getShort();//read and ignore;
                    } else {
                        ifd.imageWidth = reader.getInt();
                    }
                    break;
                case Tags.ImageHeight:
                    if (fieldType == 3) {
                        ifd.imageHeight = reader.getShort();
                        reader.getShort();//read and ignore;
                    } else {
                        ifd.imageHeight = reader.getInt();
                    }
                    break;
                case Tags.BitsPerSample:
                    ifd.bps = new int[nValues];
                    if (nValues == 1) {
                        ifd.bps[0] = reader.getShort();
                        reader.getShort();
                    } else if (nValues == 2) {
                        ifd.bps[0] = reader.getShort();
                        ifd.bps[1] = reader.getShort();
                    } else {
                        int sampleOffset = reader.getInt();
                        int current = reader.position();
                        ifd.bps = readBitsPerSamples(reader, sampleOffset, nValues);
                        reader.position(current);
                    }
                    break;
                case Tags.Compression:
                    ifd.compressionType = reader.getShort() & 0xffff;
                    reader.getShort();
                    break;
                case Tags.PhotometricInterpolation:
                    ifd.photometric = reader.getShort() & 0xffff;
                    reader.getShort();
                    break;

                case Tags.RowsPerStrip:
                    if (fieldType == 3) {
                        ifd.rowsPerStrip = reader.getShort() & 0xffff;
                        reader.getShort();//read and ignore;
                    } else {
                        ifd.rowsPerStrip = reader.getInt();
                    }
                    break;
                case Tags.StripOffsets:
                    if (nValues == 1) {
                        ifd.stripOffsets = new int[1];
                        ifd.stripOffsets[0] = reader.getInt();
                    } else {
                        int stripOffset = reader.getInt();
                        int current = reader.position();
                        ifd.stripOffsets = readStripOffsets(reader, stripOffset, nValues, fieldType);
                        reader.position(current);
                    }
                    break;
                case Tags.StripByteCounts:
                    if (nValues == 1) {
                        ifd.stripByteCounts = new int[1];
                        ifd.stripByteCounts[0] = reader.getInt();
                    } else {
                        int stripOffset = reader.getInt();
                        int current = reader.position();
                        ifd.stripByteCounts = readStripByteCounts(reader, stripOffset, nValues, fieldType);
                        reader.position(current);
                    }
                    break;
                case Tags.SamplesPerPixel:
                    ifd.samplesPerPixel = reader.getShort();
                    reader.getShort();
                    break;
                case Tags.ColorMap:
                    int cmapOffset = reader.getInt();
                    int current = reader.position();
                    ifd.colorMap = readColorMap(reader, cmapOffset, nValues);
                    reader.position(current);
                    break;
                case Tags.PlanarConfiguration:
                    ifd.planarConfiguration = reader.getShort();
                    reader.getShort();//read and ignore;
                    break;
                case Tags.Threshholding:
                    reader.getInt();
                    break;
                case Tags.CellWidth:
                    reader.getInt();
                    break;
                case Tags.CellLength:
                    reader.getInt();
                    break;
                case Tags.FillOrder:
                    ifd.fillOrder = reader.getInt();
                    break;
                case Tags.DocumentName:
                    reader.getInt();
                    break;
                case Tags.ImageDescription:
                    reader.getInt();
                    break;
                case Tags.Make:
                    reader.getInt();
                    break;
                case Tags.Model:
                    reader.getInt();
                    break;
                case Tags.Orientation:
                    reader.getInt();
                    break;
                case Tags.MinSampleValue:
                    reader.getInt();
                    break;
                case Tags.MaxSampleValue:
                    reader.getInt();
                    break;
                case Tags.Xresolution:
                    reader.getInt();
                    break;
                case Tags.Yresolution:
                    reader.getInt();
                    break;
                case Tags.PageName:
                    reader.getInt();
                    break;
                case Tags.Xposition:
                    reader.getInt();
                    break;
                case Tags.Yposition:
                    reader.getInt();
                    break;
                case Tags.FreeOffsets:
                    reader.getInt();
                    break;
                case Tags.FreeByteCounts:
                    reader.getInt();
                    break;
                case Tags.GrayResponseUnit:
                    reader.getInt();
                    break;
                case Tags.GrayResponseCurve:
                    reader.getInt();
                    break;
                case Tags.T4Options:
                    reader.getInt();
                    break;
                case Tags.T6Options:
                    reader.getInt();
                    break;
                case Tags.ResolutionUnit:
                    reader.getInt();
                    break;
                case Tags.PageNumber:
                    reader.getInt();
                    break;
                case Tags.TransferFunction:
                    reader.getInt();
                    break;
                case Tags.Software:
                    reader.getInt();
                    break;
                case Tags.DateTime:
                    reader.getInt();
                    break;
                case Tags.Artist:
                    reader.getInt();
                    break;
                case Tags.HostComputer:
                    reader.getInt();
                    break;
                case Tags.Predictor:
                    reader.getInt();
                    break;
                case Tags.WhitePoint:
                    reader.getInt();
                    break;
                case Tags.PrimaryChromaticities:
                    reader.getInt();
                    break;
                case Tags.HalftoneHints:
                    reader.getInt();
                    break;
                case Tags.TileWidth:
                    reader.getInt();
                    break;
                case Tags.TileLength:
                    reader.getInt();
                    break;
                case Tags.TIleOffsets:
                    reader.getInt();
                    break;
                case Tags.TIleByteCounts:
                    reader.getInt();
                    break;
                case Tags.SubIFDs:
                    reader.getInt();
                    break;
                case Tags.InkSet:
                    reader.getInt();
                    break;
                case Tags.InkNames:
                    reader.getInt();
                    break;
                case Tags.NumberOfInks:
                    reader.getInt();
                    break;
                case Tags.DotRange:
                    reader.getInt();
                    break;
                case Tags.TargetPrinter:
                    reader.getInt();
                    break;
                case Tags.ExtraSamples:
                    reader.getInt();
                    break;
                case Tags.SampleFormat:
                    reader.getInt();
                    break;
                case Tags.SMinSampleValue:
                    reader.getInt();
                    break;
                case Tags.SMaxSampleValue:
                    reader.getInt();
                    break;
                case Tags.TransferRange:
                    reader.getInt();
                    break;
                case Tags.ClipPath:
                    reader.getInt();
                    break;
                case Tags.XClipPathUnits:
                    reader.getInt();
                    break;
                case Tags.YClipPathUnits:
                    reader.getInt();
                    break;
                case Tags.Indexed:
                    reader.getInt();
                    break;
                case Tags.JPEGTables:
                    reader.getInt();
                    break;
                case Tags.JPEGProc:
                    reader.getInt();
                    break;
                case Tags.JPEGInterchangeFormat:
                    reader.getInt();
                    break;
                case Tags.JPEGInterchangeFormatLength:
                    reader.getInt();
                    break;
                case Tags.JPEGRestartInterval:
                    reader.getInt();
                    break;
                case Tags.JPEGLosslessPredictors:
                    reader.getInt();
                    break;
                case Tags.JPEGPointTransforms:
                    reader.getInt();
                    break;
                case Tags.JPEGQTables:
                    reader.getInt();
                    break;
                case Tags.JPEGDCTables:
                    reader.getInt();
                    break;
                case Tags.JPEGACTables:
                    reader.getInt();
                    break;
                case Tags.YCbCrCoefficients:
                    reader.getInt();
                    break;
                case Tags.YCbCrSubSampling:
                    reader.getInt();
                    break;
                case Tags.YCbCrPositioning:
                    reader.getInt();
                    break;
                case Tags.ReferenceBlackWhite:
                    reader.getInt();
                    break;
                case Tags.StripRowCounts:
                    reader.getInt();
                    break;
                case Tags.XMP:
                    reader.getInt();
                    break;
                case Tags.ImageID:
                    reader.getInt();
                    break;
                case Tags.Copyright:
                    reader.getInt();
                    break;
                case Tags.ICC:
                    reader.getInt();
                    break;
                case Tags.Exif_IFD:
                    reader.getInt();
                    break;
                case Tags.ExifVersion:
                    reader.getInt();
                    break;
                case Tags.DateTimeOriginal:
                    reader.getInt();
                    break;
                case Tags.DateTimeDigitized:
                    reader.getInt();
                    break;
                case Tags.ComponentConfiguration:
                    reader.getInt();
                    break;
                case Tags.CompressedBitsPerPixel:
                    reader.getInt();
                    break;
                case Tags.ApertureValue:
                    reader.getInt();
                    break;
                case Tags.ImageNumber:
                    reader.getInt();
                    break;
                case Tags.ImageHistory:
                    reader.getInt();
                    break;
                case Tags.ColorSpace:
                    reader.getInt();
                    break;
                case Tags.PixelXDimension:
                    reader.getInt();
                    break;
                case Tags.PixelYDimension:
                    reader.getInt();
                    break;
                default:
                    reader.getInt();
                //
            }
        }
        ifd.nextIFD = reader.getInt();
        //some files contains rowsperstrip as zero;
        if (ifd.rowsPerStrip == 0) {
            ifd.rowsPerStrip = ifd.imageHeight;
        }
        return ifd;
    }

    private static BufferedImage generateImageFromIFD(final ByteBuffer reader, IFD ifd) throws IOException {

        BufferedImage image = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        final int iw = ifd.imageWidth;
        final int ih = ifd.imageHeight;
        int balance = ih;

        switch (ifd.compressionType) {
            case Tags.Uncompressed:
                for (int i = 0; i < ifd.stripOffsets.length; i++) {
                    reader.position(ifd.stripOffsets[i]);
                    byte temp[] = new byte[ifd.stripByteCounts[i]];
                    reader.get(temp);
                    bos.write(temp);
                }
                break;
            case Tags.CCITT_ID:
                for (int i = 0; i < ifd.stripOffsets.length; i++) {
                    reader.position(ifd.stripOffsets[i]);
                    byte temp[] = new byte[ifd.stripByteCounts[i]];
                    reader.get(temp);
                    int height = balance < ifd.rowsPerStrip ? balance : ifd.rowsPerStrip;
                    byte[] output = new byte[(iw * height + 7) / 8];
                    CCITT fax = new CCITT(ifd.fillOrder, iw, height);
                    fax.decompress1D(output, temp, 0, height);
                    bos.write(output);
                    balance -= ifd.rowsPerStrip;
                }
                break;
            case Tags.Group_3_Fax:
                for (int i = 0; i < ifd.stripOffsets.length; i++) {
                    reader.position(ifd.stripOffsets[i]);
                    byte temp[] = new byte[ifd.stripByteCounts[i]];
                    reader.get(temp);
                    int height = balance < ifd.rowsPerStrip ? balance : ifd.rowsPerStrip;
                    byte[] output = new byte[(iw * height + 7) / 8];
                    CCITT fax = new CCITT(ifd.fillOrder, iw, height);
                    fax.decompressFax3(output, temp, height);
                    bos.write(output);
                    balance -= ifd.rowsPerStrip;
                }
                break;
            case Tags.Group_4_Fax:
                for (int i = 0; i < ifd.stripOffsets.length; i++) {
                    reader.position(ifd.stripOffsets[i]);
                    byte temp[] = new byte[ifd.stripByteCounts[i]];
                    reader.get(temp);
                    int height = balance < ifd.rowsPerStrip ? balance : ifd.rowsPerStrip;
                    byte[] output = new byte[(iw * height + 7) / 8];
                    CCITT fax = new CCITT(ifd.fillOrder, iw, height);
                    fax.decompressFax4(output, temp, height);
                    bos.write(output);
                    balance -= ifd.rowsPerStrip;
                }
                break;
            case Tags.LZW:
                for (int i = 0; i < ifd.stripOffsets.length; i++) {
                    reader.position(ifd.stripOffsets[i]);
                    byte temp[] = new byte[ifd.stripByteCounts[i]];
                    reader.get(temp);
                    int expectation = calculatePackBitExpectation(ifd, i);
                    byte[] output = new byte[expectation];
                    LZW lzw = new LZW();
                    lzw.decompress(temp, output);
                    bos.write(output);
                }
                break;
            case Tags.JPEG:
                System.err.println("jpeg decompression not implemented yet");
                break;
            case Tags.ADOBEDEFLATE:
                for (int i = 0; i < ifd.stripOffsets.length; i++) {
                    reader.position(ifd.stripOffsets[i]);
                    byte temp[] = new byte[ifd.stripByteCounts[i]];
                    reader.get(temp);
                    byte[] output = Deflate.decompress(temp);
                    bos.write(output);
                }
                break;
            case Tags.PackBits:
                for (int i = 0; i < ifd.stripOffsets.length; i++) {
                    reader.position(ifd.stripOffsets[i]);
                    byte temp[] = new byte[ifd.stripByteCounts[i]];
                    reader.get(temp);
                    int expectation = calculatePackBitExpectation(ifd, i);
                    temp = PackBits.decompress(temp, expectation);
                    bos.write(temp);
                }
                break;
            case Tags.Deflate:
                for (int i = 0; i < ifd.stripOffsets.length; i++) {
                    reader.position(ifd.stripOffsets[i]);
                    byte temp[] = new byte[ifd.stripByteCounts[i]];
                    reader.get(temp);
                    byte[] output = Deflate.decompress(temp);
                    bos.write(output);
                }
                break;
            default:
                System.out.println("unrecognized decompression called");
        }

        byte[] data = bos.toByteArray();
        bos.reset();
        int[] intPixels;
        byte[] bytePixels;
        int n = 0, bps, shift;
        int dim = ifd.imageWidth * ifd.imageHeight;

        int r, g, b, a;
        JPXBitReader bitReader;

        //rgb palette  has to be handled in different way
        if (ifd.photometric == Tags.RGB_Palette) {
            System.out.println(ifd.colorMap.length);
            IndexColorModel indexedCM = new IndexColorModel(ifd.bps[0], ifd.colorMap.length / 3, ifd.colorMap, 0, false);
            WritableRaster ras = indexedCM.createCompatibleWritableRaster(ifd.imageWidth, ifd.imageHeight);
            bytePixels = ((DataBufferByte) ras.getDataBuffer()).getData();
            System.arraycopy(data, 0, bytePixels, 0, bytePixels.length);
            image = new BufferedImage(indexedCM, ras, false, null);
            return image;
        }

        //planar configuration speration mode: contains r full, g full, b full values;
        if (ifd.planarConfiguration == 2 && ifd.samplesPerPixel > 1) {
            int nComp = ifd.samplesPerPixel;
            byte[][] comp = new byte[nComp][dim];
            int t = 0;
            for (int i = 0; i < nComp; i++) {
                System.arraycopy(data, t, comp[i], 0, dim);
                t += dim;
            }
            int p = 0;
            for (int i = 0; i < dim; i++) {
                for (int j = 0; j < nComp; j++) {
                    data[p++] = comp[j][i];
                }
            }
        }

        //invert the colors to make white is zero;
        if (ifd.photometric == Tags.WhiteIsZero) {
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) ((data[n++] & 0xff) ^ 0xff);
            }
        }

        switch (ifd.samplesPerPixel) {            
            case 0://sometimes this may be 0;
            case 1:
                switch (ifd.bps[0]) {
                    case 0://sometimes this may be 0;
                    case 1:
                        image = new BufferedImage(ifd.imageWidth, ifd.imageHeight, BufferedImage.TYPE_BYTE_GRAY);
                        bytePixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                        bitReader = new JPXBitReader(data);
                        n = 0;
                        bps = ifd.bps[0];
                        if (ifd.compressionType == Tags.Uncompressed) {
                            for (int i = 0; i < ih; i++) {
                                for (int j = 0; j < iw; j++) {
                                    bytePixels[n++] = (byte) (bitReader.readBits(bps) * 255);
                                }
                                int iw8 = (iw * bps) % 8;
                                if (iw8 != 0) {
                                    bitReader.readBits(8 - iw8);
                                }
                            }
                        } else {
                            balance = ih;
                            for (int i = 0; i < ifd.stripOffsets.length; i++) {
                                int height = balance < ifd.rowsPerStrip ? balance : ifd.rowsPerStrip;
                                int dd = height * iw;
                                for (int z = 0; z < dd; z++) {
                                    bytePixels[n++] = (byte) (bitReader.readBits(bps) * 255);
                                }
                                int iw8 = (dd * bps) % 8;
                                if (iw8 != 0) {
                                    bitReader.readBits(8 - iw8);
                                }
                                balance -= ifd.rowsPerStrip;
                            }
                        }
                        break;
                    case 2:
                    case 4:
                    case 6:
                        image = new BufferedImage(ifd.imageWidth, ifd.imageHeight, BufferedImage.TYPE_BYTE_GRAY);
                        bytePixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                        bitReader = new JPXBitReader(data);
                        n = 0;
                        bps = ifd.bps[0];
                        shift = 8 - bps;
                        if (ifd.compressionType == Tags.Uncompressed) {
                            for (int i = 0; i < ih; i++) {
                                for (int j = 0; j < iw; j++) {
                                    bytePixels[n++] = (byte) (bitReader.readBits(bps) << shift);
                                }
                                int iw8 = (iw * bps) % 8;
                                if (iw8 != 0) {
                                    bitReader.readBits(8 - iw8);
                                }
                            }
                        } else {
                            balance = ih;
                            for (int i = 0; i < ifd.stripOffsets.length; i++) {
                                int height = balance < ifd.rowsPerStrip ? balance : ifd.rowsPerStrip;
                                int dd = height * iw;
                                for (int z = 0; z < dd; z++) {
                                    bytePixels[n++] = (byte) (bitReader.readBits(bps) << shift);
                                }
                                int iw8 = (dd * bps) % 8;
                                if (iw8 != 0) {
                                    bitReader.readBits(8 - iw8);
                                }
                                balance -= ifd.rowsPerStrip;
                            }
                        }
                        break;
                    case 8:
                        image = new BufferedImage(ifd.imageWidth, ifd.imageHeight, BufferedImage.TYPE_BYTE_GRAY);
                        bytePixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                        System.arraycopy(data, 0, bytePixels, 0, data.length);
                        break;
                    case 10:
                    case 12:
                    case 14:
                    case 16:
                        image = new BufferedImage(ifd.imageWidth, ifd.imageHeight, BufferedImage.TYPE_BYTE_GRAY);
                        bytePixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                        bitReader = new JPXBitReader(data);
                        n = 0;
                        bps = ifd.bps[0];
                        shift = bps - 8;
                        for (int i = 0; i < ih; i++) {
                            for (int j = 0; j < iw; j++) {
                                bytePixels[n++] = (byte) (bitReader.readBits(bps) >> shift);
                            }
                            int iw8 = (iw * bps) % 8;
                            if (iw8 != 0) {
                                bitReader.readBits(8 - iw8);
                            }
                        }
                        break;
                }
                break;
            case 2:
                break;
            case 3:
                switch (ifd.photometric) {
                    case Tags.RGB:
                        image = new BufferedImage(ifd.imageWidth, ifd.imageHeight, BufferedImage.TYPE_INT_RGB);
                        intPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

                        if (needBitReader(ifd.bps)) {
                            bitReader = new JPXBitReader(data);
                            for (int i = 0; i < dim; i++) {
                                r = bitReader.readBits(ifd.bps[0]);
                                g = bitReader.readBits(ifd.bps[1]);
                                b = bitReader.readBits(ifd.bps[2]);
                                intPixels[i] = r << 16 | g << 8 | b;
                            }
                        } else {
                            for (int i = 0; i < dim; i++) {
                                r = data[n++] & 0xff;
                                g = data[n++] & 0xff;
                                b = data[n++] & 0xff;
                                intPixels[i] = r << 16 | g << 8 | b;
                            }
                        }

                        break;
                    default:
                        System.err.println("Photometric value " + ifd.photometric + " not implemented yet");
                }
                break;
            case 4:
                switch (ifd.photometric) {
                    case Tags.RGB:
                        image = new BufferedImage(ifd.imageWidth, ifd.imageHeight, BufferedImage.TYPE_INT_ARGB);
                        intPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

                        if (needBitReader(ifd.bps)) {
                            bitReader = new JPXBitReader(data);
                            for (int i = 0; i < dim; i++) {
                                r = bitReader.readBits(ifd.bps[0]);
                                g = bitReader.readBits(ifd.bps[1]);
                                b = bitReader.readBits(ifd.bps[2]);
                                a = bitReader.readBits(ifd.bps[0]);
                                intPixels[i] = a << 24 | r << 16 | g << 8 | b;
                            }
                        } else {
                            for (int i = 0; i < dim; i++) {
                                r = data[n++] & 0xff;
                                g = data[n++] & 0xff;
                                b = data[n++] & 0xff;
                                a = data[n++] & 0xff;
                                intPixels[i] = a << 24 | r << 16 | g << 8 | b;
                            }
                        }

                        break;
                    default:
                        System.err.println("Photometric value " + ifd.photometric + " not implemented yet");
                }
                break;
        }

        return image;
    }

    private static int calculatePackBitExpectation(IFD ifd, int current) {

        int sampleLen = 0;
        if (ifd.planarConfiguration == 2) {
            sampleLen = ifd.bps[0];
        } else {
            for (int i = 0; i < ifd.bps.length; i++) {
                sampleLen += ifd.bps[i];
            }
        }

        int rps = ifd.rowsPerStrip;
        int iw = ifd.imageWidth;
        int ih = ifd.imageHeight;
        int totalOffsets = ifd.stripOffsets.length;
        int totalBits;
        if (totalOffsets == 1) {
            totalBits = iw * ih * sampleLen;
        } else if (current + 1 == totalOffsets) {
            if (rps == 1) {
                totalBits = iw * sampleLen;
            } else {
                int mod = (ih % rps);
                if (mod == 0) {
                    totalBits = rps * iw * sampleLen;
                } else {
                    totalBits = mod * iw * sampleLen;
                }
            }
        } else {
            totalBits = rps * iw * sampleLen;
        }
//        System.out.println(totalBits + " total " + ((totalBits + 7) / 8));
        return (totalBits + 7) / 8;
    }

    private static boolean needBitReader(int[] bps) {
        for (int i = 0; i < bps.length; i++) {
            if (bps[i] != 8) {
                return true;
            }
        }
        return false;
    }

    private static int[] readBitsPerSamples(final ByteBuffer reader, int offset, int nSamples) {
        reader.position(offset);
        int temp[] = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            temp[i] = reader.getShort();
        }
        return temp;
    }

    private static int[] readStripOffsets(final ByteBuffer reader, int offset, int nOffsets, int fieldType) {
        reader.position(offset);
        int temp[] = new int[nOffsets];
        if (fieldType == 3) {
            for (int i = 0; i < nOffsets; i++) {
                temp[i] = reader.getShort() & 0xff;
            }
        } else {
            for (int i = 0; i < nOffsets; i++) {
                temp[i] = reader.getInt();
            }
        }
        return temp;
    }

    private static int[] readStripByteCounts(final ByteBuffer reader, int offset, int nCount, int fieldType) {
        reader.position(offset);
        int temp[] = new int[nCount];
        if (fieldType == 3) {
            for (int i = 0; i < nCount; i++) {
                temp[i] = reader.getShort() & 0xff;
            }
        } else {
            for (int i = 0; i < nCount; i++) {
                temp[i] = reader.getInt();
            }
        }
        return temp;
    }

    private static byte[] readColorMap(final ByteBuffer reader, int cmapOffset, int nValues) {
        reader.position(cmapOffset);
        int totalColors = nValues / 3;

        byte rr[] = new byte[totalColors];
        byte gg[] = new byte[totalColors];
        byte bb[] = new byte[totalColors];

        for (int j = 0; j < totalColors; j++) {
            int sv = reader.getShort() & 0xffff;
            rr[j] = (byte) (sv >> 8);
        }
        for (int j = 0; j < totalColors; j++) {
            int sv = reader.getShort() & 0xffff;
            gg[j] = (byte) (sv >> 8);
        }
        for (int j = 0; j < totalColors; j++) {
            int sv = reader.getShort() & 0xffff;
            bb[j] = (byte) (sv >> 8);
        }

        byte temp[] = new byte[nValues];
        int p = 0;
        for (int i = 0; i < totalColors; i++) {
            temp[p++] = rr[i];
            temp[p++] = gg[i];
            temp[p++] = bb[i];
        }
        return temp;
    }

    public int getPageCount() {
        return pageCount;
    }

}
