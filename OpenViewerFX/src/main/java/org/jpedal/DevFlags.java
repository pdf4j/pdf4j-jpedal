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
 * DevFlags.java
 * ---------------
 */
package org.jpedal;


public class DevFlags {
    
    //flag to say we are using fest testing so need to customise some actions.
    public static boolean GUITESTINGINPROGRESS;
    
    public static Thread thread;
    
    public static String currentFile;
    
    public static int currentPage;
    
    /** Flag to indicate when a file has finished "being" open */
    public static boolean fileLoaded;
    
    public static boolean formsLoaded = true;
    
    /**
     * Flag to indicate when a file has finished "being" printed
     */
    public static boolean printingDone;
    
    public static boolean testing;
    
    public static boolean isTesting;
    
    /** displays the viewport border */
    public static boolean displayViewportBorder;
    
    
    public static void addShutdownHook() {
        if (!DevFlags.isTesting) {
            
            if (thread != null) {
                Runtime.getRuntime().removeShutdownHook(thread);
            }
            
            thread = new Thread() {
                @Override
                public void run() {
                    System.out.println("{internal only} filename="
                            + currentFile + ' ' + currentPage);
                }
            };
            Runtime.getRuntime().addShutdownHook(thread);
            
        }
    }
}
