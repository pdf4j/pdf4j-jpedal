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
 * PageFlowDisplayFX.java
 * ---------------
 */

package org.jpedal.display.javafx;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import org.jpedal.*;
import org.jpedal.display.Display;
import org.jpedal.display.PageOffsets;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.exception.PdfException;
import org.jpedal.gui.GUIFactory;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.text.TextLines;

/**
 *
 */
public class PageFlowDisplayFX implements Display {
    
    public PageFlowDisplayFX(final GUIFactory currentGUI, final PdfDecoderInt pdf) {
    
    }
    
    //public PageFlowFX getPageFlowFX(){
    //    return pageFlowFX;
    //}
    
    @Override
    public double getIndent() {
        throw new UnsupportedOperationException("getIndent Not supported yet."); 
    }

    /**
     * Please use public int[] getCursorBoxOnScreenAsArray() instead.
     * @deprecated on 04/07/2014
     */
    @Override
    public Rectangle getCursorBoxOnScreen() {
        throw new UnsupportedOperationException("getCursorBoxOnScreen Not supported yet."); 
    }

    @Override
    public int[] getCursorBoxOnScreenAsArray() {
        throw new UnsupportedOperationException("getCursorBoxOnScreenAsArray Not supported yet."); 
    }

    @Override
    public void setCursorBoxOnScreen(final Rectangle cursorBoxOnScreen, final boolean isSamePage) {
        throw new UnsupportedOperationException("setCursorBoxOnScreen Not supported yet."); 
    }

    @Override
    public void forceRedraw() {
    }

    @Override
    public void setPageRotation(final int displayRotation) {
    }

    @Override
    public void resetViewableArea() {
    }

    @Override
    public void paintPage(final Object box, final AcroRenderer formRenderer, final TextLines textLines) {
    }

    @Override
    public void paintPage(final Graphics2D g2, final AcroRenderer formRenderer, final TextLines textLines) {
    }

    @Override
    public void updateCursorBoxOnScreen(final int[] newOutlineRectangle, final int outlineColor, final int pageNumber, final int x_size, final int y_size) {
    }

    
    /**
     * Deprecated on 04/07/2014, please use 
     * updateCursorBoxOnScreen(int[] newOutlineRectangle, int outlineColor, int pageNumber,int x_size,int y_size) instead.
     * @deprecated
     */
    @Override
    public void updateCursorBoxOnScreen(final Rectangle newOutlineRectangle, final Color outlineColor, final int pageNumber, final int x_size, final int y_size) {
    }

    @Override
    public void drawCursor(final Graphics g, final float scaling) {
    }

    /**
     * Deprecated on 07/07/2014.
     * Please use setViewableArea(int[] viewport) instead.
     * @deprecated
     */
    @Override
    public AffineTransform setViewableArea(final Rectangle viewport) throws PdfException {
        return null;
    }

    @Override
    public AffineTransform setViewableArea(final int[] viewport) throws PdfException {
        return null;
    }

    @Override
    public void drawFacing(final Rectangle visibleRect) {
    }

    @Override
    public int[] getPageSize(final int displayView) {
        return null;
    }

    @Override
    public void decodeOtherPages(final int pageNumber, final int pageCount) {
    }

    @Override
    public void stopGeneratingPage() {
        
    }

    @Override
    public void refreshDisplay() {
    }

    @Override
    public void disableScreen() {
    }

    @Override
    public void flushPageCaches() {
    }

    @Override
    public void init(final float scaling, final int displayRotation, final int pageNumber, final DynamicVectorRenderer currentDisplay, final boolean isInit) {
    }

    @Override
    public void drawBorder() {
    }

    @Override
    public void setup(final boolean useAcceleration, final PageOffsets currentOffset) {
    }

    @Override
    public int getYCordForPage(final int page) {
        return 0;
    }

    @Override
    public int getYCordForPage(final int page, final float scaling) {
        return 0;
    }

    @Override
    public int getXCordForPage(final int currentPage) {
        return 0;
    }

    @Override
    public void setThumbnailPanel(final GUIThumbnailPanel thumbnails) {
    }

    @Override
    public void setScaling(final float scaling) {
    }

    @Override
    public void setPageOffsets(final int page) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void setAcceleration(final boolean enable) {
    }

    @Override
    public void setAccelerationAlwaysRedraw(final boolean enable){
    }
    
    @Override
    public void setObjectValue(final int type, final Object newValue) {
    }

    @Override
    public int[] getHighlightedImage() {
        return new int[0];
    }

    @Override
    public void setHighlightedImage(final int[] i) {
    }

    @Override
    public float getOldScaling() {
        return 0;
    }

    @Override
    public boolean getBoolean(final BoolValue option) {
        return false;
    }

    @Override
    public void setBoolean(final BoolValue option, final boolean value) {
        throw new UnsupportedOperationException("Attempting to set unknown boolean in PageFlowDisplay.");
    }

    @Override
    public Rectangle getDisplayedRectangle() {
        throw new UnsupportedOperationException("getDisplayedRectangle Not supported yet."); 
    }
    
}
