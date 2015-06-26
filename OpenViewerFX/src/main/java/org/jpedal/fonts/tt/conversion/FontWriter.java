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
 * FontWriter.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.fonts.tt.FontFile2;
import org.jpedal.render.output.io.DefaultIO;
import org.jpedal.utils.Sorts;
import org.jpedal.utils.StringUtils;
import org.jpedal.utils.repositories.FastByteArrayOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;

public class FontWriter extends FontFile2 {

    String name;

    int glyphCount;

    int headCheckSumPos=-1;

    final Map IDtoTable=new HashMap();
    
    @SuppressWarnings("CanBeFinal")
    private static boolean compressWoff = true;
    
    //remove the commas in generated font names in order to support html display
    //private final static boolean removeCommas = true;

    static {
        if(DefaultIO.isTest) {
            compressWoff = false;
        }
    }
    
    //list responsible for holding TTF Table Header information. 
    final ArrayList<TTFDirectory> ttfList = new ArrayList<TTFDirectory>();

    //old table order source kept in case of revert
//    /**
//     * the tsbles need to be in 1 order and 
//     * the tags in the header in another 
//     */
//    static final String[] TTFTableOrder=new String[]{"OS/2",
//            "cmap",
//            "glyf",
//            "head",
//            "hhea",
//            "hmtx",
//            "loca",
//            "maxp",
//            "name",
//            "post",
//    };
    

    /**
     * the tsbles need to be in 1 order and 
     * the tags in the header in another 
     */
    static final String[] TTFTableOrder= {
    		"OS/2",
    		"cmap",
    		"cvt ",
    		"fpgm",            
            "glyf",
            "head",
            "hhea",
            "hmtx",
            "loca",
            "maxp",
            "name",            
            "post",
            "prep"
    };

    private final HashMap<Integer, byte[]> tableStore = new HashMap<Integer, byte[]>();

    protected String styleName; // @Lyndon see EOT spec

    public FontWriter(final byte[] data) {
        super(data);
    }

    public FontWriter() {
    }

    /**
     * Retrieves a positive int from a specified number of bytes in supplied array
     * @param d Byte array to fetch from
     * @param startPoint Location of start of number
     * @param offsetSize Number of bytes occupied
     * @return Number found
     */
    static int getUintFromByteArray(final byte[] d, final int startPoint, final int offsetSize) {
        int shift = (offsetSize-1) * 8;
        int result = 0;
        int offset = 0;
        while (shift >= 0) {
            int part = d[startPoint+offset];
            if (part < 0) {
                part += 256;
            }
            result |= part << shift;
            offset++;
            shift -= 8;
        }

        return result;
    }

    /**
     * Retrieves a binary number of arbitrary length from the supplied int array, treating it as bytes
     * @param d int array to fetch from
     * @param startPoint Location of start of number
     * @param offsetSize Number of bytes occupied
     * @return Number found
     */
    static int getUintFromIntArray(final int[] d, final int startPoint, final int offsetSize) {
        int shift = (offsetSize-1) * 8;
        int result = 0;
        int offset = 0;
        while (shift >= 0) {
            result |= (d[startPoint+offset] & 0xFF) << shift;
            offset++;
            shift -= 8;
        }

        return result;
    }

    /**
     * Encode a number as a byte array of arbitrary length.
     * @param num The number to encode
     * @param byteCount The number of bytes to use
     * @return The number expressed in the required number of bytes
     */
    public static byte[] setUintAsBytes(final int num, final int byteCount) {
        final byte[] result = new byte[byteCount];
        for (int i=byteCount; i>0; i--) {
            int part = num;
            for (int j=1; j<i; j++) {
                part = (part >> 8);
            }
            result[byteCount-i] = (byte)part;
        }

        return result;
    }

    static int createChecksum(final byte[] table) {

        int checksumValue=0;

        final FontFile2 checksum=new FontFile2(table,true);

        final int longCount=((table.length+3)>>2);

        for(int j=0;j<longCount;j++){
            checksumValue += checksum.getNextUint32();
        }

        return checksumValue;
    }

    /**
     * return a short
     */
    public static final byte[] setUFWord(final int rawValue) {

        final short value=(short)rawValue;

        final byte[] returnValue=new byte[2];

        for(int i=0;i<2;i++){
            returnValue[i]= (byte) ((value>>(8*(1-i)))& 255);
        }

        return returnValue;
    }

    /**
     * return a short
     */
    public static final byte[] setFWord(final int rawValue) {

        final short value=(short)rawValue;

        final byte[] returnValue=new byte[2];

        for(int i=0;i<2;i++){
            returnValue[i]= (byte) ((value>>(8*(1-i)))& 255);
        }

        return returnValue;
    }

    /**
     * turn int back into byte[2]
     **/
    public static final byte[] setNextUint16(final int value){

        final byte[] returnValue=new byte[2];

        for(int i=0;i<2;i++){
            returnValue[i]= (byte) ((value>>(8*(1-i)))& 255);
        }
        return returnValue;
    }

    /**
     * turn int back into byte[2]
     **/
    public static final byte[] setNextInt16(final int value){

        final byte[] returnValue=new byte[2];

        for(int i=0;i<2;i++){
            returnValue[i]= (byte) ((value>>(8*(1-i)))& 255);
        }
        return returnValue;
    }

    /**
     * turn int back into byte[2]
     **/
    public static final byte[] setNextSignedInt16(final short value){

        final byte[] returnValue=new byte[2];

        for(int i=0;i<2;i++){
            returnValue[i]= (byte) ((value>>(8*(1-i)))& 255);
        }
        return returnValue;
    }

    /**
     * turn int back into byte
     **/
    public static final byte setNextUint8(final int value){

        return (byte) (value& 255);

    }

    /**
     * turn int back into byte[4]
     **/
    public static final byte[] setNextUint32(final int value){

        final byte[] returnValue=new byte[4];

        for(int i=0;i<4;i++){
            returnValue[i]= (byte) ((value>>(8*(3-i)))& 255);
        }

        return returnValue;
    }

    /**
     * turn int back into byte[8]
     **/
    @SuppressWarnings("UnusedDeclaration")
    public static final byte[] setNextUint64(final int value){

        final byte[] returnValue=new byte[8];

        for(int i=0;i<8;i++){
            returnValue[i]= (byte) ((value>>(8*(7-i)))& 255);
        }

        return returnValue;
    }

    public static final byte[] setNextUint64(final long value){

        final byte[] returnValue=new byte[8];

        for(int i=0;i<8;i++){
            returnValue[i]= (byte) ((value>>(8*(7-i)))& 255);
        }

        return returnValue;
    }

    // Switch from big endian to little endian or vice versa
    public static final byte[] switchEndian(final byte[] original) {
        final byte[] switched = new byte[original.length];
        int count = 0;
        int i = original.length-1;
        while(i > -1) {
            switched[count] = original[i];
            count ++;
            i --;
        }
        return switched;
    }

    /**read the table tableLength*/
    public final byte[] writeFontToStream() {

        readTables();

        final FastByteArrayOutputStream bos=new FastByteArrayOutputStream();

        //version
        if(type==OPENTYPE){ //OTF with glyf data start OTTO otherwise 1.0
            if(subType==PS) {
                bos.write(setNextUint32(1330926671));
            } else {
                bos.write(setNextUint32(65536));
            }
        }else if(type==TTC) {
            bos.write(setNextUint32(1953784678));
        } else {
            bos.write(setNextUint32(65536));
        }

        if(type==TTC){

            System.out.println("TTC write not implemented");

        }else{  //otf or ttf

            //location of tables
            //tables=new int[tableCount][1];
            //tableLength=new int[tableCount][1];

            writeTablesForFont(bos);
        }

        final byte[] fileInBytes=bos.toByteArray();

        /**
        * set HEAD checksum
        */
        final byte[] headCheckSum=setNextUint32((int)(Long.parseLong("B1B0AFBA",16)-createChecksum(fileInBytes)));
        System.arraycopy(headCheckSum, 0, fileInBytes, 0 + headCheckSumPos, 4);

        //Code for writing out whole font
//        if (name.contains("NAAAJN"))
//            new BinaryTool(fileInBytes,"C:/Users/sam/desktop/"+name.replace('+', '-')+".otf.iab");


        return fileInBytes;
    }

    //empty base copy replaced by version from TT or PS
    void readTables() {

        //
    }

    boolean debug;

    private void writeTablesForFont(final FastByteArrayOutputStream bos) {

        //boolean hasValidOffsets=false;

        int[] keys=new int[numTables];
        final int[] offset=new int[numTables];
        int id;
        String tag;
        final int[] checksum=new int[numTables];
        final int[] tableSize=new int[numTables];

        /**
         * write out header
         */
        bos.write(setNextUint16(numTables));
        bos.write(setNextUint16(searchRange));
        bos.write(setNextUint16(entrySelector));
        bos.write(setNextUint16(rangeShift));

        /**
         * calc checksums
         */
        for(int l=0;l<numTables;l++){

            tag= (String) tableList.get(l);

            //read table value (including for head which is done differently at end)
            id= getTableID(tag);

            if(debug) {
                System.out.println("writing out " + tag + " id=" + id);
            }

            if(id!=-1){
                final byte[] tableBytes= this.getTableBytes(id);
                tableStore.put(id, tableBytes);

                //head is set to zero here and replaced later
                if(id!=HEAD) {
                    checksum[l] = createChecksum(tableBytes);
                }

                tableSize[l]=tableBytes.length;

                //needed below to work out order in file
                keys[l]=l;
                offset[l]=tables[id][currentFontID];
//                if(offset[l]>0){ //all set to zero in TTF so ignore sort
//                    hasValidOffsets=true;
//                }
            }
        }


        if(subType==PS){//not used in Marks new code - one for sam to consider?
            keys= Sorts.quicksort(offset, keys);
        }

        int currentOffset=alignOnWordBoundary(bos.size()+(16*numTables));
        final int[] fileOffset=new int[numTables];

        int i;
        /**
         * calc filePointer
         */
        for(int l=0;l<numTables;l++){

            i=keys[l];

            fileOffset[i]=currentOffset;

            offset[i]=currentOffset;

            currentOffset=alignOnWordBoundary(currentOffset+tableSize[i]);

        }

        //write out TTF font data
        if(subType==TTF){

            /**
             * write out pointers
             */
            for(int j=0;j<numTables;j++){

                tag= TTFTableOrder[j];

                bos.write(StringUtils.toBytes(tag));

                //read table value
                id= getTableID(tag);
                final int l=(Integer)(IDtoTable.get(tag));

                if(id!=-1){

                    //flag pos to put in correct value later
                    if(id==HEAD){
                        headCheckSumPos=bos.size();
                    }

                    bos.write(setNextUint32(checksum[l]));//checksum
                    bos.write(setNextUint32(fileOffset[l])); //table pos
                    bos.write(setNextUint32(tableSize[l])); //table length

                    ttfList.add(new TTFDirectory(tag, fileOffset[l], checksum[l], tableSize[l]));

                } else {
                    //Fill with dummy data
                    bos.write(setNextUint32(0));
                    bos.write(setNextUint32(0));
                    bos.write(setNextUint32(0));
                }
            }

        }else{ //old version for PS

            /**
             * write out pointers
             */
            for(int j=0;j<numTables;j++){

                tag= (String) tableList.get(j);

                bos.write(StringUtils.toBytes(tag));

                //read table value
                id= getTableID(tag);

                if(id!=-1){
                    //byte[] table=this.getTableBytes(id);

                    //flag pos to put in correct value later
                    if(id==HEAD){
                        headCheckSumPos=bos.size();
                    }

                    bos.write(setNextUint32(checksum[j]));//checksum
                    bos.write(setNextUint32(fileOffset[j])); //table pos
                    bos.write(setNextUint32(tableSize[j])); //table length

                    ttfList.add(new TTFDirectory(tag, fileOffset[j], checksum[j], tableSize[j]));

                } else {
                    //Fill with dummy data
                    bos.write(setNextUint32(0));
                    bos.write(setNextUint32(0));
                    bos.write(setNextUint32(0));
                }
            }
        }

        /**
         * write out actual tables in order
         */
        int sortedKey;
        byte[] bytes;
        for(int l=0;l<numTables;l++){

            sortedKey=keys[l];
            tag= (String) tableList.get(sortedKey);
            id= getTableID(tag);
            bytes = tableStore.get(id);

            if (bytes == null) {
                continue;
            }

            //fill in any gaps (pad out to multiple of 4 byte blocks)
            while((bos.size() & 3) !=0){
                bos.write((byte)0);
            }

            bos.write(bytes);
        }
    }

    private static int alignOnWordBoundary(int currentOffset){
        //make sure on 4 byte boundary
        final int packing = currentOffset & 3;
        if(packing!=0){
            currentOffset += (4-packing);
        }

        return currentOffset;
    }
    
    
    /*
     * Method does the ttf to woff conversion
     * #########################################################################################
     * ## please read carefully the woff specification prior to any alteration on this method ##
     * #########################################################################################
     * */
    public final byte[] writeFontToWoffStream() throws IOException{
        final byte [] originalSfntData = writeFontToStream();

        for(final TTFDirectory t : ttfList){
     	   if(t.getTag().equals("head")){
     		   final int headChecksum = AdjustWoffChecksum(originalSfntData, t.getOffset(), t.getLength());
     		   t.setChecksum(headChecksum);
     	   }
        }
        
        final int ttfSuffix =	12+(16*numTables);
        final int woffSuffix=	44+(20*numTables);
        final int onlySfntLen = originalSfntData.length-ttfSuffix	;
        final byte [] onlySfntData  = new byte[onlySfntLen];
        
        System.arraycopy(originalSfntData, ttfSuffix, onlySfntData, 0, originalSfntData.length-ttfSuffix);
        
        final ByteArrayOutputStream dbos	=	new ByteArrayOutputStream(8192);
      
        
        int endPos = 0;       
        for(final TTFDirectory d : ttfList){
     	   
     	   final byte [] b = new byte[d.getLength()];
     	   System.arraycopy(originalSfntData, d.getOffset(), b, 0, d.getLength()); 	    	        	  
     	   d.setOffset(endPos+woffSuffix);
     	        	
     	   int compLength = d.getLength();
     	   
     	   if(compressWoff){     		  
     		  final byte[] output = new byte[d.getLength()];
     	      final Deflater compresser = new Deflater();
     	      compresser.setInput(b);
     	      compresser.finish();
     	      compLength = compresser.deflate(output);
     	      if(compLength<d.getLength() && compresser.finished()){
     	    	  dbos.write(output,0,compLength);   
     	      }else{     	    	 
     	    	  dbos.write(b);
     	    	  compLength = d.getLength();     	    	  
     	      }    	           	   	
     	   }
     	   else{
     		  dbos.write(b);	    	 	    	  
     	   }
     	   d.setCompressLength(compLength);
     	  
	    	  
     	   //int padding = (d.getLength()%4);
     	   final int padding = (compLength%4)>0?4-(compLength%4):0;
     	   if(padding>0){
     		   final byte[] n = new byte[padding];
         	   dbos.write(n);         	   
     	   }	   
           endPos = endPos + d.getCompressLength() + padding ;     	   
     	   //System.out.println("printing\t"+d.getTag()+" off "+d.getOffset()+" checksum "+d.getChecksum()+" length "+d.getLength()+" compressLen "+d.getCompressLength()+" --pdding-- "+ padding +" --- end-- "+endPos);
     	   
        }     
        
        final ByteArrayOutputStream wbos	=	new ByteArrayOutputStream(8192);
        wbos.write(setNextUint32(0x774f4646)); //signature
        
        //below lines write sfnt version into woff data
 	       if(type==OPENTYPE){ //OTF with glyf data start OTTO otherwise 1.0
 	           if(subType==PS) {
                    wbos.write(setNextUint32(1330926671));
                } else {
                    wbos.write(setNextUint32(65536));
                }
 	       }else if(type==TTC){
 	           wbos.write(setNextUint32(1953784678));
 	       }
 	       else{
 	           wbos.write(setNextUint32(65536));
 	       }
 	   //end of sfnt version
 	       
        wbos.write(setNextUint32(woffSuffix+endPos));//length
        wbos.write(setNextUint16(numTables));//number of tables
        wbos.write(setNextUint16(0));		//reserved
        
        final int endPadding = (originalSfntData.length%4)>0?4-(originalSfntData.length%4):0;
     
        wbos.write(setNextUint32(endPadding > 0 ? originalSfntData.length+ endPadding : originalSfntData.length));
        wbos.write(setNextUint16(1));		//major version
        wbos.write(setNextUint16(1));		//minor version
        wbos.write(setNextUint32(0));		//metaOffet
        wbos.write(setNextUint32(0));		//metaLength
        wbos.write(setNextUint32(0));		//metaOrigLength
        wbos.write(setNextUint32(0));		//privOffset
        wbos.write(setNextUint32(0));		//privLength
        
        for(final TTFDirectory t : ttfList){
     	   wbos.write(t.getTagBytes());    	 
     	   wbos.write(setNextUint32(t.getOffset()));
     	   wbos.write(setNextUint32(t.getCompressLength()));
     	   wbos.write(setNextUint32(t.getLength()));
     	   wbos.write(setNextUint32(t.getChecksum()));    	      	   
        }                  
        wbos.write(dbos.toByteArray());
        wbos.flush();
        wbos.close();       
        return wbos.toByteArray();
     }
    
    
    /*
     * Method adjust the header table checksum and checkSumAdjustment field in head
     * @return calculated head checksum
     * */
    private static int AdjustWoffChecksum(final byte[] tableBytes, final int headOffset, final int headLength) {
    	
    	if(tableBytes.length>headOffset && headLength>=4){
    		final ByteBuffer data = ByteBuffer.wrap(tableBytes);
        	
        	final byte [] a = new byte[4];
        	for(int z=0;z<4;z++){
        		a[z]=0;
        	}
        	
        	System.arraycopy(a, 0, tableBytes, (headOffset+8), 4);    	
        	final int totalChecksum = (0xB1B0AFBA - createChecksum(a));
        	 	
        	final byte [] b = new byte[headLength];
        	System.arraycopy(data.array(), headOffset, b, 0, headLength);
        	
        	final int headChecksum = createChecksum(b);
        	
        	final ByteBuffer bb = ByteBuffer.allocate(4);
        	bb.putInt(totalChecksum); 
        	final byte[] c =   bb.array();
         	
        	System.arraycopy(c, 0, tableBytes, (headOffset+8), 4);
        	return headChecksum;
        	
    	}else{
    		return 0;
    	}
    	
	}

    public final byte[] writeFontToEotStream() throws IOException {
        final byte [] originalSfntData = writeFontToStream(); // get original font data
        final byte[] nameBytes = name.replace(',', '-').getBytes("UTF-16LE");
        final byte[] styleNameBytes = styleName.getBytes("UTF-16LE");
        final byte[] versionBytes = "Version 1.0".getBytes("UTF-16LE");

        final int RESERVED = 0; // Reserved - must be 0
        final int PADDING = 0x0000; // Padding to maintain long alignment. Padding value must always be set to 0x0000.

        final int FontDataSize = originalSfntData.length; // Length of Open Type font (FontData) in bytes
        final int Version = 0x00020001; // Version number of this format
        final int Flags = 0; // Processing Flags
        final byte[] FontPANOSE = OS2Writer.fontPanose.clone(); // The PANOSE value for this font
        final byte Charset = 0x01; // In Windows this is derived from TEXTMETRIC.tmCharSet. This value specifies the character set of the font.
        byte Italic = 0x0; // If the bit for ITALIC is set in OS/2.fsSelection, the value will be 0x01
        if(styleName.equals("Italic")) {
            Italic = 0x01;
        }
        int Weight = 400; // The weight value for this font
        if(styleName.equals("Bold")) {
            Weight = 700;
        }
        final short fsType = 8; // Type flags that provide information about embedding permissions (Short)
        final short MagicNumber = 0x504C; // Magic number for EOT file - 0x504C. Used to check for data corruption.

        final int UnicodeRange1 = 0; // os/2.UnicodeRange1 (bits 0-31)
        final int UnicodeRange2 = 0; // os/2.UnicodeRange2 (bits 32-63)
        final int UnicodeRange3 = 0; // os/2.UnicodeRange3 (bits 64-95)
        final int UnicodeRange4 = 0; // os/2.UnicodeRange4 (bits 96-127)
        final int CodePageRange1 = 0; // CodePageRange1 (bits 0-31)
        final int CodePageRange2 = 0; // CodePageRange2 (bits 32-63)
        final int CheckSumAdjustment = 0; // head.CheckSumAdjustment
        final int FamilyNameSize = nameBytes.length; //  Number of bytes used by the FamilyName array (Short)
        final byte[] FamilyName = nameBytes.clone(); // Array of UTF-16 characters the length of FamilyNameSize bytes. This is the English language Font Family string found in the name table of the font (name ID = 1)
        final int StyleNameSize = styleNameBytes.length; // Number of bytes used by the StyleName (Short)
        final byte[] StyleName = styleNameBytes.clone(); // Array of UTF-16 characters the length of StyleNameSize bytes. This is the English language Font Subfamily string found in the name table of the font (name ID = 2)
        final int VersionNameSize = versionBytes.length; // Number of bytes used by the VersionName (Short)
        final byte[] VersionName = versionBytes.clone(); // Array of UTF-16 characters the length of VersionNameSize bytes. This is the English language version string found in the name table of the font (name ID = 5)
        final int FullNameSize = nameBytes.length; // Number of bytes used by the FullName (short)
        final byte[] FullName = nameBytes.clone();// Array of UTF-16 characters the length of FullNameSize bytes. This is the English language full name string found in the name table of the font (name ID = 4)
        final int RootStringSize = 0;

        // Now Write out the file's bytes
        final ByteArrayOutputStream wbos = new ByteArrayOutputStream();
        wbos.write(switchEndian(setNextUint32(0))); // Write out total size (unsigned long) (temp value of 0 for now)
        wbos.write(switchEndian(setNextUint32(FontDataSize))); // Write out font data size (unsigned long)
        wbos.write(switchEndian(setNextUint32(Version))); // (unsigned long)
        wbos.write(switchEndian(setNextUint32(Flags))); // (unsigned long)

        // Write out font panose values (unsigned byte array of 10)
        for(final byte b : FontPANOSE) {
            wbos.write(setNextUint8(b)); // (byte)
        } // PANOSE is not important as far as I can tell

        wbos.write(setNextUint8(Charset)); // (byte)
        wbos.write(setNextUint8(Italic)); // (byte)
        wbos.write(switchEndian(setNextUint32(Weight))); // (unsigned long)
        wbos.write(switchEndian(setNextUint16(fsType))); // (unsigned short)
        wbos.write(switchEndian(setNextUint16(MagicNumber))); // (unsigned short)

        wbos.write(switchEndian(setNextUint32(UnicodeRange1))); // (unsigned long)
        wbos.write(switchEndian(setNextUint32(UnicodeRange2))); // (unsigned long)
        wbos.write(switchEndian(setNextUint32(UnicodeRange3))); // (unsigned long)
        wbos.write(switchEndian(setNextUint32(UnicodeRange4))); // (unsigned long)
        wbos.write(switchEndian(setNextUint32(CodePageRange1))); // (unsigned long)
        wbos.write(switchEndian(setNextUint32(CodePageRange2))); // (unsigned long)
        wbos.write(switchEndian(setNextUint32(CheckSumAdjustment))); // (unsigned long)

        wbos.write(switchEndian(setNextUint32(RESERVED))); // (unsigned long)
        wbos.write(switchEndian(setNextUint32(RESERVED))); // (unsigned long)
        wbos.write(switchEndian(setNextUint32(RESERVED))); // (unsigned long)
        wbos.write(switchEndian(setNextUint32(RESERVED))); // (unsigned long)

        wbos.write(setNextUint16(PADDING)); // (unsigned short) // 84th byte
        wbos.write(switchEndian(setNextUint16(FamilyNameSize))); // (unsigned short)
        // Write out FamilyName
        for(final byte b : FamilyName) {
            wbos.write(setNextUint8(b)); // (byte)
        }

        wbos.write(setNextUint16(PADDING)); // (unsigned short)
        wbos.write(switchEndian(setNextUint16(StyleNameSize))); // (unsigned short)
        // Write out StyleName
        for(final byte b : StyleName) {
            wbos.write(setNextUint8(b)); // (byte)
        }

        wbos.write(setNextUint16(PADDING)); // (unsigned short)
        wbos.write(switchEndian(setNextUint16(VersionNameSize))); // (unsigned short)
        // Write out VersionName
        for(final byte b : VersionName) {
            wbos.write(setNextUint8(b)); // (byte)
        }

        wbos.write(setNextUint16(PADDING)); // (unsigned short)
        wbos.write(switchEndian(setNextUint16(FullNameSize))); // (unsigned short)
        // Write out FullName
        for(final byte b : FullName) {
            wbos.write(setNextUint8(b)); // (byte)
        }

        wbos.write(setNextUint16(PADDING)); // (unsigned short)
        wbos.write(setNextInt16(RootStringSize)); // RootStringSize (unsigned short)

        // Finally write out the original font data
        wbos.write(originalSfntData);  // (byte array)

        // Track back in the stream and now write out the actual size of the entire eot file
        //System.out.println(wbos.size());
//        wbos.write(switchEndian(setNextUint32(wbos.size())), 0, 4);

        final byte[] eotFontData = wbos.toByteArray();

        // For some reason wbos.write(switchEndian(setNextUint32(wbos.size())), 0, 4); didn't work
        final byte[] wholeLength = switchEndian(setNextUint32(eotFontData.length));
        System.arraycopy(wholeLength, 0, eotFontData, 0, 4);

        // Testing code
//        BufferedOutputStream testOut = new BufferedOutputStream(new FileOutputStream(new File("C:\\Users\\Lyndon\\Desktop", name + ".eot")));
//        testOut.write(eotFontData);
//        testOut.close();

        return eotFontData;  // @Lyndon EOT Font Writing
    }


    /*
     * This class added in order to access the variable amount in woff Font Conversion     
     * */
	@SuppressWarnings("UnusedDeclaration")
    private static class TTFDirectory{
		private int offset2,checksum2,length2,compressLength;
		
		private String tag2;		

		TTFDirectory(final String tag, final int offset, final int checksum, final int length){
			this.tag2		=	tag;
			this.offset2 	=	offset;
			this.checksum2	=	checksum;
			this.length2	=	length;
		}
		
		public String getTag(){
			return this.tag2;
		}		
		
		public byte[] getTagBytes(){
			final byte [] b = new byte[4];
			for(int z=0;z<4;z++){
				b[z] = (byte) this.tag2.charAt(z);
			}
			return b ;
		}			
		
		public int getOffset(){
			return this.offset2;
		}
		public int getChecksum(){
			return checksum2;
		}
		public int getLength(){
			return length2;
		}
		public int getCompressLength() {
			return compressLength;
		}
		
		public void setOffset(final int offset){
			this.offset2 = offset;
		}		
		public void setChecksum(final int checksum) {
			this.checksum2 = checksum;
		}
		public void setLength(final int length) {
			this.length2 = length;
		}
		public void setTag(final String tag) {
			this.tag2 = tag;
		}		
		public void setCompressLength(final int compressLength) {
			this.compressLength = compressLength;
		}
		
	}
    
}
