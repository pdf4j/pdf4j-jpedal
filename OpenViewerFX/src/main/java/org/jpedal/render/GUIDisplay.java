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
 * GUIDisplay.java
 * ---------------
 */

package org.jpedal.render;

import java.awt.Color;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import org.jpedal.color.PdfColor;
import org.jpedal.examples.handlers.DefaultImageHelper;
import org.jpedal.exception.PdfException;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.SwingShape;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.parser.Cmd;
import org.jpedal.utils.LogWriter;

/**
 *
 * functions shared by Swing and FX but not lower level Display implementations
 */
abstract class GUIDisplay extends BaseDisplay implements DynamicVectorRenderer{

    
    private boolean needsHorizontalInvert;

    private boolean needsVerticalInvert;
    

    public void saveImage(final int id, final String des, final String type) {
        final String name = imageIDtoName.get(id);
        BufferedImage image=objectStoreRef.loadStoredImage(name);

        //if not stored, try in memory
        if(image==null) {
            image = (BufferedImage) pageObjects.elementAt(id);
        }
        
        if(image!=null){

            if(image.getType()==BufferedImage.TYPE_CUSTOM || (type.equals("jpg") && image.getType()==BufferedImage.TYPE_INT_ARGB)){
                image=ColorSpaceConvertor.convertToRGB(image);
            }

            if(needsHorizontalInvert){
                image = RenderUtils.invertImageBeforeSave(image, true);
            }

            if(needsVerticalInvert){
                image = RenderUtils.invertImageBeforeSave(image, false);
            }

            try {
                DefaultImageHelper.write(image, type, des);
            } catch (IOException ex) {
                LogWriter.writeLog("Exception in writing image "+ex);
            }
        }
    }
    
    public void setneedsVerticalInvert(final boolean b) {
        needsVerticalInvert = b;
    }

    public void setneedsHorizontalInvert(final boolean b) {
        needsHorizontalInvert=b;
    }
    
    

    /**
     * return number of image in display queue or -1 if none.
     *
     * @return
     */
    public int getObjectUnderneath(final int x, final int y) {
        int typeFound = -1;
        final int[][] areas = this.areas.get();
        //Rectangle possArea = null;
        final int count = areas.length;

        if(objectType!=null){
            final int[] types = objectType.get();
            boolean nothing = true;
            for (int i = count - 1; i > -1; i--) {
                if ((areas[i] != null && RenderUtils.rectangleContains(areas[i], x, y)) &&
                         (types[i] != DynamicVectorRenderer.SHAPE && types[i] != DynamicVectorRenderer.CLIP)) {
                            nothing = false;
                            typeFound = types[i];
                            i = -1;
                        }
                    }



            if (nothing) {
                return -1;
            }
        }
        return typeFound;
    }

    
    /**
     * Returns a Rectangles X,Y,W,H as an Array of integers
     * Where 0 = x, 1 = y, 2 = w, 3 = h.
     */
    public int[] getAreaAsArray(final int i){

        return areas.elementAt(i);
    }

    /**
     * return number of image in display queue
     * or -1 if none
     * @return
     */
    public int isInsideImage(final int x, final int y){
        int outLine=-1;

        final int[][] areas=this.areas.get();
        int[] possArea = null;
        final int count=areas.length;

        if(objectType!=null){
            final int[] types=objectType.get();
            for(int i=0;i<count;i++){
                if((areas[i]!=null) &&
                    (RenderUtils.rectangleContains(areas[i],x, y) && types[i]==DynamicVectorRenderer.IMAGE)){
                        //Check for smallest image that contains this point
                        if(possArea!=null){
                            final int area1 = possArea[3] * possArea[2];
                            final int area2 = areas[i][3] * areas[i][2];
                            if(area2<area1) {
                                possArea = areas[i];
                            }
                            outLine=i;
                        }else{
                            possArea = areas[i];
                            outLine=i;
                        }
                    }
                }
            }

        return outLine;
    }

    public void drawUserContent(final int[] type1, final Object[] obj, final Color[] colors) throws PdfException {
        
        /*
         * cycle through items and add to display - throw exception if not valid
         */
        final int count = type1.length;
        int currentType;
        GraphicsState gs;
        
        for (int i = 0; i<count; i++) {
            
            currentType = type1[i];
            
            switch(currentType){
                case DynamicVectorRenderer.FILLOPACITY:
                    setGraphicsState(GraphicsState.FILL, ((Number)obj[i]).floatValue(),PdfDictionary.Normal);
                    break;
                    
                case DynamicVectorRenderer.STROKEOPACITY:
                    setGraphicsState(GraphicsState.STROKE, ((Number)obj[i]).floatValue(),PdfDictionary.Normal);
                    break;
                    
                case DynamicVectorRenderer.STROKEDSHAPE:
                    gs=new GraphicsState();
                    gs.setFillType(GraphicsState.STROKE);
                    drawShape(new SwingShape((Shape) obj[i]),gs, Cmd.S);
                                        
                    break;
                    
                case DynamicVectorRenderer.FILLEDSHAPE:
                    gs=new GraphicsState();
                    gs.setFillType(GraphicsState.FILL);
                    gs.setNonstrokeColor(new PdfColor(colors[i].getRed(),colors[i].getGreen(),colors[i].getBlue()));
                    drawShape(new SwingShape((Shape) obj[i]),gs, Cmd.F);
                     
                    break;
                    
                case DynamicVectorRenderer.CUSTOM:
                    drawCustom(obj[i]);
                    
                    break;
                    
                case DynamicVectorRenderer.IMAGE:
                    final ImageObject imgObj=(ImageObject)obj[i];
                    gs=new GraphicsState();
                    
                    gs.CTM=new float[][]{ {imgObj.image.getWidth(),0,1}, {0,imgObj.image.getHeight(),1}, {0,0,0}};
                    
                    gs.x=imgObj.x;
                    gs.y=imgObj.y;
                    
                    drawImage(this.rawPageNumber,imgObj.image, gs,false,"extImg"+i, -1);
                    
                    break;
                    
                case DynamicVectorRenderer.STRING:
                    final TextObject textObj=(TextObject)obj[i];
                    gs=new GraphicsState();
                    final float fontSize=textObj.font.getSize();
                    final double[] afValues={fontSize,0f,0f,fontSize,0f,0f};
                    drawAffine(afValues);
                    
                    drawTR(GraphicsState.FILL);
                    gs.setTextRenderType(GraphicsState.FILL);
                    gs.setNonstrokeColor(new PdfColor(colors[i].getRed(),colors[i].getGreen(),colors[i].getBlue()));
                    drawText(null,textObj.text,gs,textObj.x,-textObj.y,textObj.font); //note y is negative
                    
                    break;
                    
                case 0:
                    break;
                    
                default:
                    throw new PdfException("Unrecognised type "+currentType);
            }
        }
    }
    
    public void drawCustom(final Object value) {
       throw new UnsupportedOperationException("drawCustom base method in GUI data should not be called "+value); 
        
    }

    void drawAffine(final double[] afValues) {
        throw new UnsupportedOperationException("drawAffine only supported in Swing Viewer "+Arrays.toString(afValues));
    }
}
