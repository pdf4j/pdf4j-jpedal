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
 * PageLabelObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.StringUtils;

/**
 *
 * @author markee
 */
public class PageLabelObject extends PdfObject {

    private int ST=-1; //note default is 1
    
    String P;
    byte[] rawP;
    
    public PageLabelObject(String key) {
        super(key);
        
        objType=PdfDictionary.PageLabels;
    }
   
   
     @Override
    public String getTextStreamValue(final int id) {
        
        switch(id){
         
            case PdfDictionary.P:
                
                //setup first time
                if(P==null && rawP!=null) {
                    P = StringUtils.getTextString(rawP, false);
                }
                
                return P;
                
            default:
                return super.getTextStreamValue(id);
        }
    }
    
    @Override
    public void setTextStreamValue(final int id, final byte[] value) {

        switch(id){


            case PdfDictionary.P:
                rawP=value;
            break;

            default:
                super.setTextStreamValue(id,value);

        }
    }
    
    
    @Override
    public void setIntNumber(final int id, final int value){

        switch(id){

	        case PdfDictionary.ST:
	        	ST=value;
	        break;
	

            default:
            	super.setIntNumber(id, value);
        }
    }
    
     @Override
    public int getInt(final int id){

        switch(id){

	        case PdfDictionary.ST:
	            return ST;
	        
            default:
            	return super.getInt(id);
        }
    }
}
