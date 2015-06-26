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
 * FXDisplayCanvas.java
 * ---------------
 */

package org.jpedal.render;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Paint;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import org.jpedal.color.PdfColor;
import org.jpedal.color.PdfPaint;
import org.jpedal.constants.PDFImageProcessing;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.fonts.tt.TTGlyph;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfDictionary;

/**
 * Experimental class which utilises the JavaFX canvas to get better performance.
 * See case 17677 for notes on this class.
 * 
 * To enable this code, go to Parser.java and change the reference to FXDisplay to FXDisplayCanvas.
 * 
 * @author Simon
 */
public class FXDisplayCanvas extends SwingDisplay {
    
    
    Group pdfContent = new Group();

    private Canvas canvas = new Canvas(1000,1000);
    
    public FXDisplayCanvas() {
        init();
    }

    public FXDisplayCanvas(final int pageNumber, final boolean addBackground, final int defaultSize, final ObjectStore newObjectRef) {

        this.rawPageNumber =pageNumber;
        this.objectStoreRef = newObjectRef;
        this.addBackground=addBackground;
        
        init();
        
        setupArrays(defaultSize);

    }
    
    @Override
    public void flushAdditionalObjOnPage(){
        throw new RuntimeException("NOt used in JavaFX implementation - please redecode the page");
    }
    

    public FXDisplayCanvas(final int pageNumber, final ObjectStore newObjectRef, final boolean isPrinting) {

        this.rawPageNumber =pageNumber;
        this.objectStoreRef = newObjectRef;
        this.isPrinting=isPrinting;
        
        init();
        
        setupArrays(defaultSize);

    }
    
    private void init(){
         isSwing=false;
    }


    /* remove all page objects and flush queue */
    @Override
    public void flush() {
        super.flush();
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    void renderEmbeddedText(final int text_fill_type, final Object rawglyph, final int glyphType,
	    final AffineTransform glyphAT, final java.awt.Rectangle textHighlight,
	    PdfPaint strokePaint, PdfPaint fillPaint,
	    final float strokeOpacity, final float fillOpacity, final int lineWidth) {

        //lock out type3
        if(glyphType==DynamicVectorRenderer.TYPE3) {
            System.out.println("Type3 not yet implemented in FX");
            return;
        }
        
        final Path path=((PdfGlyph)rawglyph).getPath();
             Affine at;

            path.setFillRule(FillRule.EVEN_ODD);

            double[] textScaling=new double[6];
            glyphAT.getMatrix(textScaling);
            
            if(type!=DynamicVectorRenderer.TRUETYPE){
                  at = Transform.affine(textScaling[0],textScaling[1],textScaling[2],textScaling[3],textScaling[4],textScaling[5]);        
            }else{
                final double r=1d;
                
                if(!TTGlyph.useHinting) {
                    at = Transform.affine(textScaling[0], textScaling[1], textScaling[2], textScaling[3], textScaling[4], textScaling[5]);
                } else {
                    at = Transform.affine(textScaling[0] * r, textScaling[1] * r, textScaling[2] * r, textScaling[3] * r, textScaling[4], textScaling[5]);
                }
            }
            
            setFXParams(path,text_fill_type,strokePaint,fillPaint,strokeOpacity,fillOpacity);
            setBlendMode(blendMode, path);
                
            parseToCanvas(path, at);
    }

     /* save image in array to draw */
    @Override
    void renderImage(final AffineTransform imageAf, BufferedImage image, final float alpha,
	    final GraphicsState currentGraphicsState, final float x, final float y, final int optionsApplied) {

        final WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
        
        final double imageW= fxImage.getWidth();
        final double imageH= fxImage.getHeight();
        
        // Stores the affine used on the image to use on the clip later
        double[] affine = new double[6];
        imageAf.getMatrix(affine);
        
        Affine t;
        if(optionsApplied==PDFImageProcessing.IMAGE_INVERTED){
            t=Transform.affine(affine[0], affine[1], affine[2], -affine[3],affine[4], affine[5]);            
        }else if(optionsApplied==PDFImageProcessing.NOTHING){           
            t=Transform.affine(affine[0], affine[1], affine[2], affine[3],affine[4], affine[5]);
        }else if(optionsApplied==PDFImageProcessing.IMAGE_ROTATED){ 
            t=Transform.affine(affine[0], affine[1], affine[2], affine[3],affine[4], affine[5]);         
        }else{
            t=Transform.affine(affine[0], affine[1], affine[2], -affine[3],affine[4], affine[5]+(imageH*affine[3]));
        }
        
        //setClip(currentGraphicsState, affine, im1View);
        setBlendMode(blendMode, canvas);
       
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Affine orig=gc.getTransform();
        gc.transform(t);
        gc.drawImage(fxImage, 0, 0);
        gc.setTransform(orig);
       
        
    }
  
    public Group getFXPane(int w,int h) {
        setupCanvas(w, h);
        
        return pdfContent;
    }
    private void setupCanvas(int w,int h){
        
        getFXPane().getChildren().remove(canvas);
        canvas = new Canvas(w,h);
        
     //  canvas.setCache(true);
       //canvas.setCacheHint(CacheHint.QUALITY);
     //   canvas.getGraphicsContext2D().setTransform(Transform.affine(0.5, 0, 0, 0.5, 0, 0));
//        canvas.getGraphicsContext2D().setFill(Color.BLACK);
//        canvas.getGraphicsContext2D().fillRect(0, 0, 1000, 1000);
        
       
        getFXPane().getChildren().add(canvas);
//              
        
       this.paint(null, null, null);
    }
    
    /**
     * Takes the path and looks through the elements, mapping the points onto the appropriate GraphicsContext methods
     * 
     * @param path
     * @param at 
     */
    private void parseToCanvas(final Path path, final Affine at) {
        final Paint fill = path.getFill();
        final Paint stroke = path.getStroke();
        
        final GraphicsContext gc = canvas.getGraphicsContext2D();

        final FillRule fr = path.getFillRule();
        // Default transformation which we go back to after we've drawn the glyph
        final Affine defaultAf = gc.getTransform();
        
        if(fill != null) {
            gc.setFill(fill);
        }
        if(stroke != null) {
            gc.setStroke(stroke);
        }
        
        gc.setFillRule(fr);
        
        if(at!=null){
            gc.setTransform(at);
        }
        gc.beginPath();
        
        // Go through the path and write each element to the canvas
        for(final PathElement ele : path.getElements()){
            if(ele instanceof CubicCurveTo){
                final CubicCurveTo cct = (CubicCurveTo)ele;
                gc.bezierCurveTo(cct.getControlX1(), cct.getControlY1(), cct.getControlX2(), cct.getControlY2(), cct.getX(), cct.getY());
            }else if(ele instanceof LineTo){
                final LineTo lt = (LineTo)ele;
                gc.lineTo(lt.getX(), lt.getY());
            }else if(ele instanceof MoveTo){
                final MoveTo mt = (MoveTo)ele;
                gc.moveTo(mt.getX(), mt.getY());
            }
        }
        gc.closePath();
        if(fill != null) {
            gc.fill();
        }
        if(stroke != null) {
            gc.stroke();
        }
        
        
        gc.setTransform(defaultAf);
        
    }
    
    
    protected static void setFXParams(final Shape currentShape, final int fillType,PdfPaint strokePaint, PdfPaint fillPaint, float strokeOpacity, float fillOpacity){

        // Removes the default black stroke on shapes
        currentShape.setStroke(null);
        
        if (fillType == GraphicsState.FILL || fillType == GraphicsState.FILLSTROKE) {
            
            //get fill colour
            final int fillCol=fillPaint.getRGB();
        
            //get value as rgb and set current colour used in fill
            final int r = ((fillCol >> 16) & 255);    //red
            final int g = ((fillCol >> 8) & 255);     //green
            final int b = ((fillCol) & 255);          //blue
            //final double a=currentGraphicsState.getAlpha(GraphicsState.FILL);     //alpha
            currentShape.setFill(javafx.scene.paint.Color.rgb(r,g,b,fillOpacity));
        }
        
        if (fillType == GraphicsState.STROKE && strokePaint!=null) {

            //get fill colour
            final int strokeCol=strokePaint.getRGB();
        
            //get value as rgb and set current colour used in fill
            final int r = ((strokeCol >> 16) & 255);    //red
            final int g = ((strokeCol >> 8) & 255);     //green
            final int b = ((strokeCol) & 255);          //blue
           // final double a=currentGraphicsState.getAlpha(GraphicsState.STROKE);     //alpha
        
            currentShape.setStroke(javafx.scene.paint.Color.rgb(r,g,b,strokeOpacity));
          //  currentGraphicsState.applyFXStroke(currentShape);
        }
    }

    
    protected static void setBlendMode(int blendMode, final Node n){
        
        switch(blendMode){
            case PdfDictionary.Multiply:
                n.setBlendMode(BlendMode.MULTIPLY);
                break;
//            case PdfDictionary.Screen:
//                n.setBlendMode(BlendMode.SCREEN);
//                break;
            case PdfDictionary.Overlay:
                n.setBlendMode(BlendMode.OVERLAY);
                break;
//            case PdfDictionary.Darken:
//                n.setBlendMode(BlendMode.DARKEN);
//                break;
//            case PdfDictionary.Lighten:
//                n.setBlendMode(BlendMode.LIGHTEN);
//                break;
//            case PdfDictionary.ColorDodge:
//                n.setBlendMode(BlendMode.COLOR_DODGE);
//                break;
//            case PdfDictionary.ColorBurn:
//                n.setBlendMode(BlendMode.COLOR_BURN);
//                break;
//            case PdfDictionary.HardLight:
//                n.setBlendMode(BlendMode.HARD_LIGHT);
//                break;
//            case PdfDictionary.SoftLight:
//                n.setBlendMode(BlendMode.SOFT_LIGHT);
//                break;
//            case PdfDictionary.Difference:
//                n.setBlendMode(BlendMode.DIFFERENCE);
//                break;
//            case PdfDictionary.Exclusion:
//                n.setBlendMode(BlendMode.EXCLUSION);
//                break;
            default:
                n.setBlendMode(null);
                break;
        }
    }
    
     
    /*save shape in array to draw*/
    @Override
    public void drawShape(Object rawShape, final GraphicsState currentGraphicsState, final int cmd) {
        
        Path currentShape=(Path) rawShape;
        final int fillType=currentGraphicsState.getFillType();
        PdfPaint currentCol;
        
        int newCol;
        
        //check for 1 by 1 complex shape and replace with dot
//        if(currentShape.getBounds().getWidth()==1 &&
//                currentShape.getBounds().getHeight()==1 && currentGraphicsState.getLineWidth()<1) {
//            currentShape = new Rectangle(currentShape.getBounds().x, currentShape.getBounds().y, 1, 1);
//        }
        
        //stroke and fill (do fill first so we don't overwrite Stroke)
        if (fillType == GraphicsState.FILL || fillType == GraphicsState.FILLSTROKE) {
            
            currentCol=currentGraphicsState.getNonstrokeColor();
            
            if(currentCol==null) {
                currentCol = new PdfColor(0, 0, 0);
            }
            
            if(currentCol.isPattern()){
                
                drawFillColor(currentCol);
                fillSet=true;
            }else{
                newCol=currentCol.getRGB();
                if((!fillSet) || (lastFillCol!=newCol)){
                    lastFillCol=newCol;
                    drawFillColor(currentCol);
                    fillSet=true;
                }
            }
        }
        
        if ((fillType == GraphicsState.STROKE) || (fillType == GraphicsState.FILLSTROKE)) {
            
            currentCol=currentGraphicsState.getStrokeColor();
            
            if(currentCol instanceof Color){
                newCol=(currentCol).getRGB();
                
                if((!strokeSet) || (lastStrokeCol!=newCol)){
                    lastStrokeCol=newCol;
                    drawStrokeColor(currentCol);
                    strokeSet=true;
                }
            }else{
                drawStrokeColor(currentCol);
                strokeSet=true;
            }
        }
        
        Stroke newStroke=currentGraphicsState.getStroke();
        if((lastStroke!=null)&&(lastStroke.equals(newStroke))){
            
        }else{
            //Adjust line width to 1 if less than 1 
            //ignore if using T3Display (such as ap image generation in html / svg conversion
//            if(((BasicStroke)newStroke).getLineWidth()<1 && !(this instanceof T3Display)){
//                newStroke = new BasicStroke(1);
//            }
            lastStroke=newStroke;
            drawStroke((newStroke));
        }
        
        pageObjects.addElement(currentShape);
        objectType.addElement(DynamicVectorRenderer.SHAPE);
        
        final int[] shapeParams = {(int)currentShape.getBoundsInLocal().getMinX(), (int)currentShape.getBoundsInLocal().getMinY()
                , (int)currentShape.getBoundsInLocal().getWidth(), (int)currentShape.getBoundsInLocal().getHeight()};
        areas.addElement(shapeParams);
        
        //checkWidth(shapeParams);
        
        
        x_coord=RenderUtils.checkSize(x_coord,currentItem);
        y_coord=RenderUtils.checkSize(y_coord,currentItem);
        x_coord[currentItem]=currentGraphicsState.x;
        y_coord[currentItem]=currentGraphicsState.y;
        
        shapeType.addElement(fillType);
        currentItem++;
        
        resetTextColors=true;
        
    }
   
     /*save shape in array to draw*/
    void renderShape(final java.awt.Shape defaultClip, final int fillType, PdfPaint strokeCol, PdfPaint fillCol,
	    final Stroke shapeStroke, final Object currentShape, final float strokeOpacity,
	    final float fillOpacity) {
       
        final Path path=((Path)currentShape);
             Affine at;

            path.setFillRule(FillRule.EVEN_ODD);

//            double[] textScaling=new double[6];
//            glyphAT.getMatrix(textScaling);
//            
//            if(type!=DynamicVectorRenderer.TRUETYPE){
//                  at = Transform.affine(textScaling[0],textScaling[1],textScaling[2],textScaling[3],textScaling[4],textScaling[5]);        
//            }else{
//                final double r=1d;
//                
//                if(!TTGlyph.useHinting) {
//                    at = Transform.affine(textScaling[0], textScaling[1], textScaling[2], textScaling[3], textScaling[4], textScaling[5]);
//                } else {
//                    at = Transform.affine(textScaling[0] * r, textScaling[1] * r, textScaling[2] * r, textScaling[3] * r, textScaling[4], textScaling[5]);
//                }
//            }
            
            setFXParams(path,fillType,strokeCol,fillCol,strokeOpacity,fillOpacity);
            setBlendMode(blendMode, path);
                
            parseToCanvas(path, null);
    }
    
    public Group getFXPane() {
        return pdfContent;
    }
}
