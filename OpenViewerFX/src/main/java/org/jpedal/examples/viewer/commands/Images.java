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
 * Images.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import org.jpedal.*;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.examples.viewer.Values;
import org.jpedal.gui.GUIFactory;

/**
 * Extract/Save Images (empty in OpenViewerFX version)
 */
public class Images {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final Values commonValues, final PdfDecoderInt decode_pdf) {
        throw new UnsupportedOperationException("Not supported in OpenViewerFX");
    }
    
    /**
     * called by nav functions to decode next page
     */
    public static void decodeImage(final boolean resizePanel, final PdfDecoderInt decode_pdf, final GUIFactory currentGUI, final GUIThumbnailPanel thumbnails, final Values commonValues) {
        throw new UnsupportedOperationException("Not supported in OpenViewerFX");
    }
    
    public static void addImage(final PdfDecoderInt decode_pdf, final Values commonValues) {
        throw new UnsupportedOperationException("Not supported in OpenViewerFX");
    }
}
