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
 * MultiDisplayFX.java
 * ---------------
 */

package org.jpedal.display.javafx;

import org.jpedal.FileAccess;
import org.jpedal.PdfDecoderFX;
import org.jpedal.display.*;
import org.jpedal.gui.GUIFactory;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.render.DynamicVectorRenderer;

/**
 *
 * @author markee
 */
public class MultiDisplayFX extends SingleDisplayFX implements MultiPagesDisplay {

    public MultiDisplayFX(final GUIFactory gui, final int pageNumber, final DynamicVectorRenderer currentDisplay, final int displayView, final PdfDecoderFX pdf, final DecoderOptions options, final FileAccess fileAccess) {

        super(pageNumber, currentDisplay, pdf, options);

    }

}
