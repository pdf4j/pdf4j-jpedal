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
* ExampleErrorTracker.java
* ---------------
*/
package org.jpedal.examples.handlers;

 //<start-thin><end-thin>
import org.jpedal.external.ErrorTracker;
import org.jpedal.io.DefaultErrorTracker;


public class ExampleErrorTracker{
    
    public static void main(final String[] args) {
        new ExampleErrorTracker();
    }
    
    public ExampleErrorTracker() {
        
        /**
        //<start-thin><end-thin>
        
        // create and initialise JPedal viewer component
        
        final Viewer myViewer =new Viewer();
        myViewer.setupViewer();
        
        // add in a monitor
        //can also be PdfDecoderServer of PdfDecoderFX
        PdfDecoder decode_pdf=(PdfDecoder) myViewer.getPdfDecoder();
        
        decode_pdf.addExternalHandler(new UserErrorTracker(),org.jpedal.external.Options.ErrorTracker);
        
        //open a file
        myViewer.executeCommand(Commands.OPENFILE, new Object[]{new File("/PDFdata/test_data/Hand_Test/awjune2003.pdf")});
        
        /**/
        
    }
    
    /**
     * example implementation
     */
    private class UserErrorTracker extends DefaultErrorTracker implements ErrorTracker {
        
        long timeAtStart;
        
        boolean userFailedPage;
        
        UserErrorTracker() {}
        
        
        @Override
        public String getPageFailureMessage() {
            
            if(userFailedPage) {
                return "Timed out on page";
            } else {
                return super.getPageFailureMessage();
            }
        }
        
        @Override
        //use to see if page failed
        public boolean ispageSuccessful() {
            if(userFailedPage) {
                return false;
            } else {
                return super.ispageSuccessful();
            }
        }
        
        @Override
        // called every time we execute a Postscript command in data streams
        // (dataPointer/streamSize) gives indicator of amount decoded but page can
        // contain multiple streams
        public boolean checkForExitRequest(final int dataPointer, final int streamSize) {
            
            //gracefully allow us to fail if over certain time to decode
            final long timeElapsed=(System.currentTimeMillis()-timeAtStart);
            System.out.println("Mill-Seconds elapsed="+timeElapsed);
            if(timeElapsed>1500){
                userFailedPage=true;
                return true;
            }else {
                return false;
            }
        }
        
        @Override
        public void finishedPageDecoding(final int rawPage) {
            System.out.println("----Completed page "+rawPage);
        }
        
        @Override
        //called when we start decoding page
        public void startedPageDecoding(final int rawPage) {
            System.out.println("----Started page "+rawPage);
            
            // get the current time
            timeAtStart=System.currentTimeMillis();
        }
    } 
}
