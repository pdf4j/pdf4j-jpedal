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
 @LICENSE@
 *
 * ---------------
 * JBIG2.java
 * ---------------
 */
package org.jpedal.jbig2.io;

public class JBIG2 {
    
    /**
     * if this values is set to any values other than -1 than JBIGDecoder
     * will write the data to temporary files which is defined in tempdir
     * recommended value is 1024 for better performance.
     */
    public static int MAXIMUM_FILESIZE_IN_MEMORY = -1;
    public static boolean IS_BITMAPS_ON_FILE;
    
    /**
     * JBIG decode using our own class
     *
     */
    public static byte[] JBIGDecode(final byte[] data, final byte[] globalData, final String temp_dir) throws Exception { 
       
        final org.jpedal.jbig2.JBIG2Decoder decoder = new org.jpedal.jbig2.JBIG2Decoder();
        if (globalData != null && globalData.length > 0) {
            decoder.setGlobalData(globalData);
        }        
        decoder.decodeJBIG2(data);
        
        return decoder.getPageAsJBIG2Bitmap(0).getData(true);
       
    }    
}
