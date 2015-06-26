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
 * Type1CAdjustment.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.utils.repositories.FastByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.Type1C;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.tt.FontFile2;


public class Type1CAdjustment implements FontTableWriter {

	private final Type1C font;
	private final PdfJavaGlyphs cffGlyphs;
	private final String name ; 
	  
	private byte[] header, nameIndex, topDictIndex, globalSubrIndex, encodings, charsets, charStringsIndex, privateDict, localSubrIndex, stringIndex;	    
	//private int[] widthX, widthY, lsbX, lsbY;	   
		
	private int padding = 50;//make 50 as padding
	
	//private HashMap<Integer,String> stringMap = new HashMap<Integer,String>();
	//private ArrayList<String> stringsList = new ArrayList<String>();
	//int [] mod = null;
	boolean hasNotDef;
	
	
	//int gCount = 0;
	//int strCount = 0;
	
	
	LinkedHashMap<Integer,byte[]> codeByteMap = new LinkedHashMap<Integer,byte[]>();
	
	
	public Type1CAdjustment(final Type1C pdfFont, final PdfJavaGlyphs glyphs, final String name ){
				
		this.font 		= pdfFont;
		this.cffGlyphs 	= glyphs; 
		this.name		= name;	  
		//System.out.println(name+" firstChar= "+pdfFont.getFirstCharNumber()); 
		
		codeByteMap = getOrderedGlyphMapForAdobe(pdfFont);
					
	}

    @Override
	public byte[] writeTable() throws IOException {
		 		
		
		header = nameIndex = topDictIndex = globalSubrIndex = stringIndex = encodings = charsets = charStringsIndex = privateDict = localSubrIndex = new byte[]{};
		final FastByteArrayOutputStream bos=new FastByteArrayOutputStream();
		//CFFWriter cw 			 = new CFFWriter(glyphs,name);
								
		header 				= createHeadIndex();
		nameIndex 			= createNameIndex(name);
		
		stringIndex			= CFFUtils.createIndex(null);
		globalSubrIndex 	= CFFUtils.createIndex(null);
			
		charsets 			= createCharsets();
		charStringsIndex 	= createCharStrings();
		
		privateDict 		= createPrivateDict();
		//int encValue		= (Integer)font.getKeyValue(Type1C.ENCODING);
		
		final byte[] topDict  	= fillTopDictionary(header.length, nameIndex.length,stringIndex.length,
												globalSubrIndex.length, charsets.length, charStringsIndex.length, privateDict.length);		
		topDictIndex		= CFFUtils.createIndex(new byte[][]{topDict});
		
		padding -= (topDictIndex.length-topDict.length);
				
		//System.out.println(" header "+header.length+" name "+nameIndex.length+" charset "+charsets.length+" charStringIndex "+charStringsIndex.length+ "padding"+ padding);
				
		bos.write(header);
	    bos.write(nameIndex);
	    bos.write(topDictIndex);
	    bos.write(stringIndex);
	    bos.write(globalSubrIndex);
	    
	    for(int z=0;z<padding;z++){
	    	final byte nb = (byte)1;
	    	bos.write(nb);
	    }
	       
	    bos.write(charsets);
	    bos.write(charStringsIndex);
	    bos.write(privateDict);

		//	    FileOutputStream fis = new FileOutputStream("C:\\Users\\dev2\\Desktop\\created\\outputCFF\\"+System.currentTimeMillis()+".otf");
//		fis.write(c);
//		fis.close();
//		fis.flush();		
		return bos.toByteArray();
	       
	}
	
	private static byte[] createHeadIndex() {
		return new byte[]{FontWriter.setNextUint8(1),               //major
                FontWriter.setNextUint8(0),                 //minor
                FontWriter.setNextUint8(4),                 //headerSize
                FontWriter.setNextUint8(2)};                //offSize
	}
	
	
	
	private byte[] fillTopDictionary(final int headLen, final int nameLen, final int stringLen, final int globalLen, final int charsetsLen, final int charStrLen, final int privLen) {
		
		int currentLen 				=	headLen+nameLen+stringLen+globalLen;
		final FastByteArrayOutputStream bos   = 	new FastByteArrayOutputStream();
		
		//charstringtype eg: 2 
		final byte[] charStringTypeKeyByte	= Type1C.getOperatorBytes(Type1C.CHARSTRINGTYPE);
		final byte[] charStringTypeValueBytes = CFFUtils.storeInteger(2);//set it to two for otf
		currentLen+=charStringTypeKeyByte.length+charStringTypeValueBytes.length;
		bos.write(charStringTypeValueBytes);
		bos.write(charStringTypeKeyByte);		
							
		//FontBBox
		final float[] topFontBBox 		=	font.getFontBounds();
		final FastByteArrayOutputStream bboxArr	=	new FastByteArrayOutputStream();
        for (final float aTopFontBBox : topFontBBox) {
            bboxArr.write(CFFUtils.storeReal(aTopFontBBox));
        }
		final byte[] fontBBoxKeyByte 		=	Type1C.getOperatorBytes(Type1C.FONTBBOX);
		final byte[] fontBBoxValueBytes	=	bboxArr.toByteArray();
		currentLen+=fontBBoxKeyByte.length+fontBBoxValueBytes.length;
		bos.write(fontBBoxValueBytes);
		bos.write(fontBBoxKeyByte);
				
		//fontMatrix
		final double[] topFontMatrix 		=	(double[]) font.getKeyValue(Type1C.FONTMATRIX);
		final FastByteArrayOutputStream maxArr	=	new FastByteArrayOutputStream();
        for (final double aTopFontMatrix : topFontMatrix) {
            maxArr.write(CFFUtils.storeReal(aTopFontMatrix));
        }

		final byte[] fontMaxKeyByte 		=	Type1C.getOperatorBytes(Type1C.FONTMATRIX);
		final byte[] fontMaxValueBytes	=	maxArr.toByteArray();
		currentLen+=fontMaxKeyByte.length+fontMaxValueBytes.length;
		bos.write(fontMaxValueBytes);
		bos.write(fontMaxKeyByte);
		
		//ignore charset and create from string index		
		//charset 
		final byte[] charsetsKeyByte		=	Type1C.getOperatorBytes(Type1C.CHARSET);
		final byte[] charsetsValueBytes	=	CFFUtils.storeInteger(currentLen + 50);
		padding = padding - charsetsKeyByte.length-charsetsValueBytes.length;
		bos.write(charsetsValueBytes);
		bos.write(charsetsKeyByte);		
				
		
		//charStrings
		final byte[] charStrKeyByte		=	Type1C.getOperatorBytes(Type1C.CHARSTRINGS);
		final byte[] charStrValueBytes	=	CFFUtils.storeInteger(currentLen + 50 + charsetsLen);
		padding = padding - charStrKeyByte.length-charStrValueBytes.length;
		bos.write(charStrValueBytes);
		bos.write(charStrKeyByte);		
		
		//privateDict
		final byte[] privDictKeyByte		=	Type1C.getOperatorBytes(Type1C.PRIVATE);
		final byte[] privDictLenValueBytes =  CFFUtils.storeInteger(privLen);
		final byte[] privDictValueBytes	=	CFFUtils.storeInteger(currentLen + 50 + charsetsLen + charStrLen);
		padding = padding - privDictKeyByte.length-privDictValueBytes.length-privDictLenValueBytes.length;
		bos.write(privDictLenValueBytes); 
		bos.write(privDictValueBytes);    
		bos.write(privDictKeyByte);

		return bos.toByteArray();
	}
	
	
	
	private byte[] createCharsets() {
		final FastByteArrayOutputStream bos  = new FastByteArrayOutputStream();
		final int csFormat = 0 ;
		final int[] iter = new int[codeByteMap.size()];
		final Object[] obj = codeByteMap.keySet().toArray();
		for(int z=0;z<iter.length;z++){
			iter[z] = (Integer)obj[z];
		}
		Arrays.sort(iter);
		
		bos.write(FontWriter.setNextUint8(csFormat));//for format type 0

        for (final int anIter : iter) {
            if (anIter != 0) {
                //System.out.println("Encoding "+(Integer)font.getKeyValue(Type1C.ENCODING)+"charsetFormat "+csFormat+"  "+csNames[z]+" ==  "+Type1C.type1CStdStrings[csNames[z]]);
                if (csFormat == 0) {
                    bos.write(FontWriter.setNextUint16(anIter));
                } else if (csFormat == 1) {
                    bos.write(FontWriter.setNextUint32(anIter));
                }
            }
        }

		return bos.toByteArray();
		
	}
				
	private byte[] createCharStrings() {
		final int[] iter = new int[codeByteMap.size()];
		final Object[] obj = codeByteMap.keySet().toArray();
		for(int z=0;z<iter.length;z++){
			iter[z] = (Integer)obj[z];
			if(iter[z]==0){
				hasNotDef = true;
			}
		}
		Arrays.sort(iter);
		
		final FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
		if(hasNotDef){
			bos.write(FontWriter.setNextInt16(iter.length));//count
		}
		else{
			bos.write(FontWriter.setNextInt16(iter.length+1));//count
		}
		
		bos.write(FontWriter.setNextUint8(2));
		
		final FastByteArrayOutputStream objArr = new FastByteArrayOutputStream();
		int count = 1;		
		
		final byte [] notDefByte = {1,14};
		
		if(!hasNotDef){
			bos.write(FontWriter.setNextUint16(count));
			objArr.write(notDefByte);
			count += notDefByte.length;
		}

        for (final int anIter : iter) {
            final byte[] b = codeByteMap.get(anIter);
            //System.out.print(" "+iter[z]);

            bos.write(FontWriter.setNextUint16(count));
            objArr.write(b);
            count += b.length;

        }
		bos.write(FontWriter.setNextUint16(count));

		bos.write(objArr.toByteArray());

		return bos.toByteArray();		
	}
	
	private static byte[] createNameIndex(final String nameStr) {
		return CFFUtils.createIndex(new byte[][] {nameStr.getBytes()});
	}
		
	private byte[] createPrivateDict() {
		 	final FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
		    //defaultWidthX                 20
		 	final int dx =	(Integer)font.getKeyValue(Type1C.DEFAULTWIDTHX);
		    bos.write(CFFUtils.storeInteger(dx));
		    bos.write((byte)20);

		    //nominalWidthX                 21
		    final int nx =	(Integer)font.getKeyValue(Type1C.NOMINALWIDTHX);
		    bos.write(CFFUtils.storeInteger(nx));
		    bos.write((byte)21);
		    
		    //blue values					06
		    final int [] blueValues = 	(int[])font.getKeyValue(Type1C.BLUEVALUES);
		    if(blueValues!=null){
                for (final int blueValue : blueValues) {
                    bos.write(CFFUtils.storeReal(blueValue));
                }
			    bos.write((byte)6);
		    }
		    
		    //other blue values				07
		    final int [] otherBlueValues = 	(int[])font.getKeyValue(Type1C.OTHERBLUES);
		    if(otherBlueValues != null){
                for (final int otherBlueValue : otherBlueValues) {
                    bos.write(CFFUtils.storeReal(otherBlueValue));
                }
			    bos.write((byte)7);
		    }		    
		    
		    //family blues					08
		    final int [] familyBlues = 	(int[])font.getKeyValue(Type1C.FAMILYBLUES);
		    if(familyBlues != null){
                for (final int familyBlue : familyBlues) {
                    bos.write(CFFUtils.storeReal(familyBlue));
                }
			    bos.write((byte)8);
		    }  		    
		    
		    //family other blues			09
		    final int [] familyOtherBlues = 	(int[])font.getKeyValue(Type1C.FAMILYOTHERBLUES);
		    if(familyOtherBlues != null){
                for (final int familyOtherBlue : familyOtherBlues) {
                    bos.write(CFFUtils.storeReal(familyOtherBlue));
                }
			    bos.write((byte)9);
		    }
		    
		    //StdHW							10
		    bos.write(CFFUtils.storeInteger((Integer) font.getKeyValue(Type1C.STDHW)));
		    bos.write((byte)10);
		    
		    //StdVW							11
		    bos.write(CFFUtils.storeInteger((Integer) font.getKeyValue(Type1C.STDVW)));
		    bos.write((byte)11);

		    return bos.toByteArray();
		    
	}
	


public static LinkedHashMap<Integer,String> getUniOrderedGlyphNames(final PdfFont pdfFont){
		
		final LinkedHashMap<Integer,String> codeNameMap = new LinkedHashMap<Integer,String>();
		
		Object[] keys = pdfFont.glyphs.getCharStrings().keySet().toArray();	
		
		final HashMap<Integer,String> uniCodeMap = new HashMap<Integer,String>();
		final HashMap<Integer,String> specialCodeMap = new HashMap<Integer,String>();

    for (final Object key1 : keys) {
        final String str = StandardFonts.getUnicodeName((String) key1);
        if (str != null) {
            final int code = getAdjustedUniValue(str);
            if (code > 0) {//ignore notedef
                uniCodeMap.put(code, (String) key1);
            }
        } else {
            if (uniCodeMap.isEmpty()) {
                specialCodeMap.put(specialCodeMap.size(), (String) key1);
            } else {
                specialCodeMap.clear();
            }
        }
    }
		
		keys = uniCodeMap.keySet().toArray();
		
		if(keys.length>0){
			Arrays.sort(keys);
            for (final Object key : keys) {
                codeNameMap.put((Integer) key, uniCodeMap.get(key));
            }
		}
		else{
			keys = specialCodeMap.keySet().toArray();
			Arrays.sort(keys);
            for (final Object key : keys) {
                codeNameMap.put((Integer) key, specialCodeMap.get(key));
            }
		}
		
		return codeNameMap;
		
	}
	
	
	public static LinkedHashMap<Integer,byte[]> getOrderedGlyphMapForAdobe(final PdfFont pdfFont){
		
		final LinkedHashMap<Integer,byte[]> charListMap = new LinkedHashMap<Integer,byte[]>();
		
		Object[] keys = getUniOrderedGlyphNames(pdfFont).values().toArray();
		final Object[] values = new Object[keys.length];
		for(int z=0;z<keys.length;z++ ){
			values[z] = pdfFont.glyphs.getCharStrings().get(keys[z]);
		}
		 
		
		final HashMap<Integer,byte[]> charMap = new HashMap<Integer,byte[]>();
		final HashMap<Integer,byte[]> specialCharMap = new HashMap<Integer,byte[]>();
		
		for(int z=0;z<keys.length;z++){
			final String str = (String)keys[z];
			final int code = getSIDForString(str);
			if(code>0){//ignore notedef
				charMap.put(code, (byte[])values[z]);
			}
			else if((code == -1) && (charMap.isEmpty())){
					specialCharMap.put(specialCharMap.size()+1,  (byte[])values[z]);
				}
			}
		
		keys = charMap.keySet().toArray();
		
		if(keys.length>0){
			Arrays.sort(keys);
            for (final Object key : keys) {
                charListMap.put((Integer) key, charMap.get(key));
            }
		}
		else{
			keys = specialCharMap.keySet().toArray();
			Arrays.sort(keys);
            for (final Object key : keys) {
                charListMap.put((Integer) key, specialCharMap.get(key));
            }
		}
		return charListMap;
		
	}
	
	
	/** 
	 * w? {hs* vs* cm* hm* mt subpath}? {mt subpath}* endchar	 
	 * @param data
	 * @return widthOfGlyph
	 *
	public static int calculateWidth(final byte[] data){
		
		
		//float[] bbox = pdfFont.FontBBox;
		//Type1C font = (Type1C)pdfFont;
		//int dx = (Integer)font.getKeyValue(Type1C.DEFAULTWIDTHX);
		//int e = (Integer)font.getKeyValue(Type1C.NOMINALWIDTHX);
		//int currentWidth = 0 ;
		final FontFile2 reader = new FontFile2(data, true);
		
		
		final ArrayList<Object>drawList = new ArrayList<Object>();
		
		while(reader.hasValuesLeft()){
			final ArrayList<Object>item = readGlyphTopDictItem(reader);
			drawList.add(item);
			if((Integer)item.get(item.size()-1)==14){
				break;
			}
		}
		
		return 100;
		
	} /**/
	
	
	public static int getSIDForString(final String text) {
        for (int i=0; i<Type1C.type1CStdStrings.length; i++) {
            if (text.equals(Type1C.type1CStdStrings[i])) {
                return i;
            }
        }
        return -1; 
       
	}
	
	
	
	/**
     *return the adjusted unicode value based on String
     *which also adjusts some composite glyph values 
     */
    private static int getAdjustedUniValue(final String str){
    	if(str.length()==1){    		
    		return str.charAt(0);
    	}
    	else{
    		if(str.equals("ff")){
    			return 64256;
    		}
    		else if(str.equals("fi")){
    			return 64257;
    		}
    		else if(str.equals("fl")){
    			return 64258;
    		}
    		else if(str.equals("ffi")){
    			return 64259;
    		}
    		else if(str.equals("ffl")){
    			return 64260;
    		}
    		else{
    			return -1;
    		}
    	}
    }
	
    /**
     *this method is totally different from type1c top dictionary 
     */
    public static ArrayList<Object> readGlyphTopDictItem(final FontFile2 reader){
		
		 final ArrayList <Object> objList = new ArrayList<Object>();
		 boolean isKeyFound = false;
		 
		 while (reader.getBytesLeft()>0 && !isKeyFound){
			
			 final int oper;
			 final int b0 = reader.getNextUint8();
			 
			 if (b0 == 28) {
				 final int b1 = reader.getNextUint8();
				 final int b2 = reader.getNextUint8();
				 oper =  (b1<<8|b2);				 
				 objList.add(oper);				 
			 }
//			 else if (b0 == 29) {
//				 int b1 = reader.getNextUint8();
//				 int b2 = reader.getNextUint8();
//				 int b3 = reader.getNextUint8();
//				 int b4 = reader.getNextUint8();
//				 oper =  b1<<24|b2<<16|b3<<8|b4 ;
//				 objList.add(oper);
//			 }
			 
			 else if (b0 >= 32 && b0 <= 246) {
				  oper =  (b0-139);
				  objList.add(oper);
			}
			 			  
			 else if(b0 >= 247 && b0 <= 250) {
				 final int b1 = reader.getNextUint8();
				 oper  = ((b0 - 247)*256+b1+108) ;
				 objList.add(oper);
			 }
			 else if (b0 >= 251 && b0 <= 254) {
				 final int b1 = reader.getNextUint8();
				 oper =  (-((b0-251)*256)-b1-108);
				 objList.add(oper);
			 }
			
			 else if (b0 == 30) {
				 final StringBuilder sb = new StringBuilder();
			        boolean done = false;
			       
			        while (!done)
			        {			        	
			            final int b = reader.getNextUint8();
			            final int[] nibbles = { b / 16, b % 16 };
			            for (final int nibble : nibbles)
			            {
			                switch (nibble)
			                {
			                case 0x0:   case 0x1:        case 0x2:
			                case 0x3:   case 0x4: 	     case 0x5:
			                case 0x6:   case 0x7:        case 0x8:
			                case 0x9:   sb.append(nibble);   break;
			                case 0xa:   sb.append('.');      break;
			                case 0xb:   sb.append('E');      break;
			                case 0xc:   sb.append("E-");     break;
			                case 0xd:   break;
			                case 0xe:   sb.append('-');      break;
			                case 0xf:   done = true;         break;
			                default:
			                   // throw new IllegalArgumentException();
			                }
			            }
			        }
			        //System.out.println(sb.toString());
			        final double d  = Double.valueOf(sb.toString());
			        final int a = (int) d;
			        objList.add(a);
			      
			 }
			 if( b0 <= 31 && b0!=28 ){
				 final int sec;
				 if(b0!=12){				 
					 sec = b0;					
				 }else{
					 sec = 3072+reader.getNextUint8();					 				 		
				 }
				 objList.add(sec);				 
				 isKeyFound = true;
			 }
			 			 
		 }
		 return objList;
				 
	 }

    @Override
    public int getIntValue(final int key) {
        return 0;
    }

}

