/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2016 IDRsolutions and Contributors.
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
 * PageLabels.java
 * ---------------
 */
package org.jpedal.io;

import java.util.HashMap;
import org.jpedal.objects.raw.PageLabelObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

/**
 * convert names to refs
 */
public class PageLabels extends HashMap<Integer, String>{

    final PdfFileReader objectReader;
    
    final int pageCount;
    
    static final String symbolLowerCase[]={"m","cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv", "i"};
    static final String symbolUpperCase[]={"M","CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    static final String lettersLowerCase[]={"a","b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m","n", 
                                                "o", "p", "q", "r", "s", "t", "u", "v", "x", "w", "y", "z"};
    static final String lettersUpperCase[]={"A","B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M","N", 
                                                "O", "P", "Q", "R", "S", "T", "U", "V", "X", "W", "Y", "Z"};
    static final int[] power={1000,900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        
    PageLabels(PdfFileReader objectReader, int pageCount) {
        this.objectReader=objectReader;
        this.pageCount=pageCount;
    }
    
    void readLabels(PdfObject pageObj) {
      
        PdfArrayIterator numList =pageObj.getMixedArray(PdfDictionary.Nums);
        
        if(numList!=null && objectReader!=null){
             
            int startPage,endPage,numbType,ST,pageNum=1;
            String convertedPage,pageLabel;
            while(numList.hasMoreTokens()){
                
                //read Page 
                startPage=numList.getNextValueAsInteger()+1;
                
                //read LabelObject
                String key=numList.getNextValueAsString(true);
                final PageLabelObject labelObj  = new PageLabelObject(key);
                objectReader.readObject(labelObj);

                numbType=labelObj.getNameAsConstant(PdfDictionary.S);
                pageLabel=labelObj.getTextStreamValue(PdfDictionary.P);
               
                ST=labelObj.getInt(PdfDictionary.ST);
                if(ST>0){
                    pageNum=ST;
                }else{
                    pageNum=1;
                }
                
                if(numList.hasMoreTokens()){
                   endPage=numList.getNextValueAsInteger(false)+1; 
                }else{
                    endPage=pageCount+1;
                }
                
                //now decode type of naming and fill range
                for(int page=startPage;page<endPage;page++){
                    
                    if(pageLabel!=null){
                        convertedPage= pageLabel;
                        if(ST>0){
                            pageLabel=pageLabel.concat(getNumberValue(numbType, ST));  
                            ST++;
                        }
                    }else{
                        convertedPage = getNumberValue(numbType, pageNum);
                    }
                        
                    this.put(page, convertedPage);
                    
                    pageNum++;
                }            
            }
        }
    }

    static String getNumberValue(int numbType, int page) {
        
        String convertedPage;
    
        switch(numbType){
            case PdfDictionary.a:
                convertedPage=convertLetterToNumber(page, lettersLowerCase);
                break;
                
            case PdfDictionary.A:
                convertedPage=convertLetterToNumber(page, lettersUpperCase);
                break;
                
            case PdfDictionary.D:
                convertedPage=String.valueOf(page);
                break;
                
            case PdfDictionary.R:
                convertedPage=convertToRoman(page,symbolUpperCase);
                break;
                
            case PdfDictionary.r:
                convertedPage=convertToRoman(page,symbolLowerCase);
                break;
                
            default:
                convertedPage=String.valueOf(page);
        }
        return convertedPage;
    }
      
    static String convertToRoman(int arabicNumber, final String[] symbols){
        
        final StringBuilder romanNumeral=new StringBuilder();
        int repeat;
        
        for(int x=0; arabicNumber>0; x++){
            repeat=arabicNumber/power[x];
            
            for(int chars=1; chars<=repeat; chars++){
                romanNumeral.append(symbols[x]);
            }
            arabicNumber %= power[x];
        }
        
        return romanNumeral.toString();
    }
    
    private static String convertLetterToNumber(int page, final String[] letters) {
       
        final StringBuilder finalLetters=new StringBuilder();
        int repeat = page/26;               
        int remainder = page%26;
        
        if (repeat>0) {
            for (int x=0; x<repeat; x++) {
                finalLetters.append(letters[25]);
            }
        }
        if (remainder!=0) {
            finalLetters.append(letters[remainder-1]);
        }
        return finalLetters.toString();
    }
}
