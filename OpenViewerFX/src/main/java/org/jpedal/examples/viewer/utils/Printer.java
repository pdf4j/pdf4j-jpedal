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
 @LICENSE@
 *
 * ---------------
 * Printer.java
 * ---------------
 */
package org.jpedal.examples.viewer.utils;

import java.awt.print.PrinterJob;

import javax.print.*;

import org.jpedal.*;

import org.jpedal.gui.GUIFactory;

public class Printer implements PrinterInt{
    
    @Override
    public void printPDF(final PdfDecoderInt decodePdf, final GUIFactory currentGUI, final String blacklist, final String defaultPrinter) {
    
    }
    
    public static String[] getAvailablePrinters(final String blacklist) {
        final PrintService[] service=PrinterJob.lookupPrintServices();
        
        final int noOfPrinters = service.length;
        String[] serviceNames = new String[noOfPrinters];
        
        //check blacklist
        if (blacklist != null) {
            final String[] bl = blacklist.split(",");
            
            int count = 0;
            //loop through printservices
            for (final PrintService aService : service) {
                boolean pass = true;
                final String name = aService.getName();
                
                //loop through blacklist items
                for (final String aBl : bl) {
                    
                    //check for wildcard
                    if (aBl.contains("*")) {
                        final String term = aBl.replace("*", "").trim();
                        if (name.contains(term)) {
                            pass = false;
                        }
                        
                    } else if (name.equalsIgnoreCase(aBl.toLowerCase())) {
                        pass = false;
                    }
                }
                
                //Add to array
                if (pass) {
                    serviceNames[count] = name;
                    count++;
                }
            }
            
            //Trim array
            final String[] temp = serviceNames;
            serviceNames = new String[count];
            System.arraycopy(temp,0,serviceNames,0,count);
        } else {
            for(int i=0;i<noOfPrinters;i++) {
                serviceNames[i] = service[i].getName();
            }
        }
        
        return serviceNames;
    }
    
    public static boolean isPrinting() {
        
        return false;
    }
    
}
