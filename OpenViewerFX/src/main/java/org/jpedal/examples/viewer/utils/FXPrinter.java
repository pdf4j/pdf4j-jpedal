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
 * FXPrinter.java
 * ---------------
 */
package org.jpedal.examples.viewer.utils;

import org.jpedal.PdfDecoderInt;
import org.jpedal.gui.GUIFactory;

public class FXPrinter implements PrinterInt {

    @Override
    public void printPDF(final PdfDecoderInt decodePdf, final GUIFactory currentGUI, final String blacklist, final String defaultPrinter) {
        throw new UnsupportedOperationException("Printing is not available in OpenViewerFX");
    }

    /**
     * @return prints flag
     */
    public static boolean isPrinting() {

        return false;
    }

}
