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
 * SwingCommands.java
 * ---------------
 */
package org.jpedal.examples.viewer;

import org.jpedal.PdfDecoderInt;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.examples.viewer.utils.PrinterInt;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.gui.GUIFactory;

/**
 *
 * @author markee
 */
class SwingCommands extends Commands {

    public SwingCommands(Values commonValues, GUIFactory currentGUI, PdfDecoderInt decode_pdf, GUIThumbnailPanel thumbnails, PropertiesFile properties, GUISearchWindow searchFrame, PrinterInt currentPrinter) {
       super(commonValues, currentGUI, decode_pdf, thumbnails, properties, searchFrame, currentPrinter);
     
    }
    
     Object executeSwingCommand(int ID, Object[] args, Object status) {
         throw new UnsupportedOperationException("executeSwingCommand should not be called");
     }
    
}
