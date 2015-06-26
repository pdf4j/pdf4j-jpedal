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
 * FontRasterizer.java
 * ---------------
 */
package org.jpedal.render.output;

import java.util.HashMap;
import java.util.Map;

/**
 * controls whether we override text in realtext modes and put text on svg or background
 */
public class FontRasterizer {
    
    private Map fontsToInclude,fontsToExclude;


    public FontRasterizer(final String value) {

        final String[] values = value.split("=");

        final String[] fonts = values[1].split(",");

        final Map mapOfFonts = new HashMap();

        if (values[0].equals("INCLUDE")) {
            fontsToInclude = mapOfFonts;
        } else if (values[0].equals("EXCLUDE")) {
            fontsToExclude = mapOfFonts;
        } else{
            throw new RuntimeException("Setting fontsToRasterizeInTextMode failed - must start with INCLUDE= or EXCLUDE= " + values[0]);
        }

        /**
         * note comparison is done in lower case so case insensitive
         */
        for (final String string : fonts) {
            mapOfFonts.put(string.trim().toLowerCase().intern(), string);
        }

    }
    
    //private Map fonts=new HashMap();
    
    public boolean isFontRasterized(String fontName){
       
        if(fontsToInclude==null && fontsToExclude==null){
            return false;
        }else{            
            fontName=fontName.trim().toLowerCase().intern();

//            if(!fonts.containsKey(fontName)){
//                fonts.put(fontName,"x");
//                
//               System.out.println(fontName+" "+flag+" "+fontsToExclude+" "+fontsToExclude.containsKey(fontName));
//            }
            
            return (fontsToInclude!=null && fontsToInclude.containsKey(fontName)) || (fontsToExclude!=null && !fontsToExclude.containsKey(fontName));
        }       
    }
}


