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
 * PageFlowDisplayFX.java
 * ---------------
 */

package org.jpedal.display.javafx;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import org.jpedal.*;
import org.jpedal.display.Display;
import org.jpedal.display.PageFlowFX;
import org.jpedal.display.PageOffsets;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.JavaFxGUI;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.exception.PdfException;
import org.jpedal.gui.GUIFactory;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.text.TextLines;
import org.jpedal.utils.Messages;

/**
 *
 */
public class PageFlowDisplayFX implements Display {
    
    final PageFlowFX pageFlowFX;
    final JavaFxGUI fxGUI;
    
    public PageFlowDisplayFX(final GUIFactory currentGUI, final PdfDecoderInt pdf) {
        
        fxGUI = (JavaFxGUI)currentGUI;
        
        pageFlowFX = new PageFlowFX(pdf, true);
        
        //Update page number on navbar
        pageFlowFX.getPageNumber().addListener(new ChangeListener() {
            @Override
            public void changed(final ObservableValue o, final Object oldVal, final Object newVal) {
                fxGUI.setPage((int)pageFlowFX.getPageNumber().doubleValue());
                if (pageFlowFX.isUpdateMemory()) {
                    fxGUI.showMessageDialog(pageFlowFX.getMemoryMessage());
                }
            }
        });
        
        pageFlowFX.setCursors(fxGUI.getGUICursor().getCursorImageForFX(GUI.GRAB_CURSOR), fxGUI.getGUICursor().getCursorImageForFX(GUI.GRABBING_CURSOR));

        ((PdfDecoderFX)pdf).getChildren().clear();
        
        final ListChangeListener lcl = new ListChangeListener() {

            @Override
            public void onChanged(final ListChangeListener.Change change) {
                
                final double width = fxGUI.getDisplayPane().getItems().get(1).getBoundsInLocal().getWidth();
                final double height = fxGUI.getDisplayPane().getItems().get(1).getBoundsInLocal().getHeight();
                
                pageFlowFX.setMinSize(width, height);
            }
        };
        fxGUI.getDisplayPane().getItems().addListener(lcl);
        final double width = fxGUI.getDisplayPane().getItems().get(1).getBoundsInLocal().getWidth();
        final double height = fxGUI.getDisplayPane().getItems().get(1).getBoundsInLocal().getHeight();
        pageFlowFX.setMinSize(width, height);
        
        try {
            fxGUI.getDisplayPane().getItems().add(pageFlowFX);
            fxGUI.getDisplayPane().getItems().remove(fxGUI.getPageContainer());
            
            //Enable Memory Bar
            fxGUI.enableCursor(true, false);
            fxGUI.enableMemoryBar(true, true);
            fxGUI.setMultibox(new int[]{});
            
        } catch (final IllegalArgumentException e) {

            fxGUI.showMessageDialog(Messages.getMessage("PdfViewer.PageFlowIllegalArgument")+e);

            if (Platform.isFxApplicationThread()) {

                currentGUI.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);

            } else {
                final Runnable doPaintComponent = new Runnable() {

                    @Override
                    public void run() {
                        currentGUI.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);
                    }
                };
                Platform.runLater(doPaintComponent);
            }
        }
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
        final int[] pageSize = new int[2];
        pageSize[0] = (int) ((PdfDecoderFX) pageFlowFX.getPdfDecoderInt()).getWidth();
        pageSize[1] = (int) ((PdfDecoderFX) pageFlowFX.getPdfDecoderInt()).getHeight();
        return pageSize;
    }

    @Override
    public void decodeOtherPages(final int pageNumber, final int pageCount) {
    }

    @Override
    public void stopGeneratingPage() {
        pageFlowFX.stop();
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
        pageFlowFX.setRotation(displayRotation);
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
        if (pageFlowFX.getPdfDecoderInt().getPageNumber() != pageFlowFX.getPageNumber().doubleValue()) {
            pageFlowFX.goTo(pageFlowFX.getPdfDecoderInt().getPageNumber());
        }
    }

    @Override
    public void setPageOffsets(final int page) {
    }

    @Override
    public void dispose() {
        /**Could probably go somewhere better when other other viewmodes are
        implemented, or when bugs becomes apparent, but for now it works.**/
        fxGUI.getDisplayPane().getItems().add(fxGUI.getPageContainer());    
        fxGUI.getDisplayPane().getItems().remove(pageFlowFX);
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
