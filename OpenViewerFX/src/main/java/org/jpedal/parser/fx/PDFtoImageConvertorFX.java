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
 * PDFtoImageConvertorFX.java
 * ---------------
 */
package org.jpedal.parser.fx;

import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotResult;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.util.Callback;
import org.jpedal.exception.PdfException;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.parser.PDFtoImageConvertor;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.render.FXDisplay;
import org.jpedal.render.FXDisplayForRasterizing;

public class PDFtoImageConvertorFX extends PDFtoImageConvertor{

    int pageIndex;
    
     public PDFtoImageConvertorFX(final float multiplyer, final DecoderOptions options) {
        super(multiplyer, options);
        isFX = true;
        
    }
   
    @Override
    public DynamicVectorRenderer getDisplay(final int pageIndex, final ObjectStore localStore, boolean isTransparent) {
        this.pageIndex=pageIndex;
        return imageDisplay = new FXDisplayForRasterizing(pageIndex,!isTransparent, 5000, localStore); //note !isTransparent as actually addBackground
       
    }
    
    @Override
    public BufferedImage pageToImage(final boolean imageIsTransparent, final PdfStreamDecoder currentImageDecoder,
            final float scaling, final PdfObject pdfObject,final AcroRenderer formRenderer) throws PdfException {

        final SimpleObjectProperty<BufferedImage> imageProperty = new SimpleObjectProperty<BufferedImage>();
        // Locks the Thread until the image is generated
        final CountDownLatch latch = new CountDownLatch(1);
        
        if(Platform.isFxApplicationThread()){
            snapshot(currentImageDecoder, scaling, pdfObject, null, imageProperty,formRenderer);
        }else{
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    snapshot(currentImageDecoder, scaling, pdfObject, latch, imageProperty,formRenderer);
                }
            });
        }     
        
        try {
            // This will hang if we're on the FX Thread
            if(!Platform.isFxApplicationThread()) {
                latch.await();
            }
        } catch (final InterruptedException ex) {
            ex.printStackTrace();
        }
        return imageProperty.get();
    }
    
    private void snapshot(final PdfStreamDecoder currentImageDecoder, final float scaling, final PdfObject pdfObject, 
            final CountDownLatch latch, final SimpleObjectProperty<BufferedImage> imageProperty,final AcroRenderer formRenderer){
        
        Pane g=new Pane();
        
        try {
            formRenderer.getCompData().setRootDisplayComponent(g);
            
          // currentImageDecoder.setObjectValue(ValueTypes.DirectRendering, null);//(Graphics2D) graphics);
            currentImageDecoder.decodePageContent(pdfObject);
            
            formRenderer.createDisplayComponentsForPage(pageIndex, currentImageDecoder);
            
            formRenderer.displayComponentsOnscreen(pageIndex, pageIndex);
            formRenderer.getCompData().resetScaledLocation(scaling, 0, 0);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        
        Group group;
        group=((FXDisplay)imageDisplay).getFXPane();
       
        group.getChildren().addAll(g.getChildren());
        
        // Transform from PDF coordinates and apply scaling
        group.getTransforms().add(Transform.affine(1 * scaling,0,0,-1 * scaling,crx,h+cry));
        final Scene scene=new Scene(group,w,h);

        // Fixes blending
        scene.setFill(Color.rgb(255, 255, 255, 1.0));
        
        if(latch != null){
            // Async call to snapshot
            scene.snapshot(new Callback<SnapshotResult, Void>() {
                @Override public Void call(final SnapshotResult p) {
                    imageProperty.set(SwingFXUtils.fromFXImage(p.getImage(), null));
                    latch.countDown();
                    return null;
                }
            },null);
        }else{ // If we're on the FX Thread, get the snapshot straight away.
            imageProperty.set(SwingFXUtils.fromFXImage(scene.snapshot(null),null));
        }
    }
}
