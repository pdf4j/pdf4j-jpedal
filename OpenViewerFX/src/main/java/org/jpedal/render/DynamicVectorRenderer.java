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
 * DynamicVectorRenderer.java
 * ---------------
 */
package org.jpedal.render;

import org.jpedal.color.PdfPaint;
import org.jpedal.exception.PdfException;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.GraphicsState;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;
import org.jpedal.external.FontHandler;



@SuppressWarnings("UnusedDeclaration")
public interface DynamicVectorRenderer  {

    public void eliminateHiddenText(Shape currentShape, GraphicsState gs, int segmentCount, boolean ignoreScaling);

    public void saveImageData(boolean b);

    public boolean saveImageData();

    public void setIsRenderingToImage(boolean b);

    public FontHandler getFontHandler();

    
    enum Mode{PDF,XFA,SMASK}

    public static final int TEXT=1;
	public static final int SHAPE=2;
	public static final int IMAGE=3;
	public static final int TRUETYPE=4;
	public static final int TYPE1C=5;
	public static final int TYPE3=6;
	public static final int CLIP=7;
	public static final int COLOR=8;
	public static final int AF=9;
	public static final int TEXTCOLOR=10;
	public static final int FILLCOLOR=11;
	public static final int STROKECOLOR=12;
	public static final int STROKE=14;
	public static final int TR=15;
	public static final int STRING=16;
	public static final int STROKEOPACITY=17;
	public static final int FILLOPACITY=18;

	public static final int STROKEDSHAPE=19;
	public static final int FILLEDSHAPE=20;

	public static final int FONTSIZE=21;
	public static final int LINEWIDTH=22;

	public static final int CUSTOM=23;

	public static final int fontBB=24;

    public static final int DELETED_IMAGE = 27;
    public static final int REUSED_IMAGE = 29;

    public static final int BLENDMODE = 31;

    public static final int SAVE_EMBEDDED_FONT = 10;
    public static final int TEXT_STRUCTURE_OPEN = 40;
    public static final int TEXT_STRUCTURE_CLOSE = 42;

    public static final int IsSVGMode = 44;
    public static final int IsTextSelectable = 45;
    public static final int IsRealText = 46;
    public static final int MARKER=200;

	/**flag to enable debugging of painting*/
	public static boolean debugPaint=false;

	/**
	 * various types of DVR which we have
	 */
	public static final int DISPLAY_SCREEN = 1;//
	public static final int DISPLAY_IMAGE = 2;//
	public static final int CREATE_PATTERN =3;
	public static final int CREATE_HTML =4;
	public static final int CREATE_SVG =5;
	public static final int CREATE_EPOS =7;
    public static final int CREATE_SMASK =8;
	
	
	/**
	 * Keys for use with set value
	 */
    public static final int ALT_BACKGROUND_COLOR=1;
	public static final int ALT_FOREGROUND_COLOR=2;
	public static final int FOREGROUND_INCLUDE_LINEART=3; //Alt foreground color changes lineart as well
	public static final int COLOR_REPLACEMENT_THRESHOLD=4;
	
    /**
     * used to pass in Graphics2D for all versions
     * @param g2
     */
    public void setG2(Graphics2D g2);

    /**
	 * set optimised painting as true or false and also reset if true
	 * @param optimsePainting
	 */
	public abstract void setOptimsePainting(boolean optimsePainting);

	/* remove all page objects and flush queue */
	public abstract void flush();

	/* remove all page objects and flush queue */
	public abstract void dispose();

    /**
     * only needed for screen display
     * @param x
     * @param y
     */
	public abstract void setInset(int x, int y);

	/*renders all the objects onto the g2 surface for screen display*/
	public abstract void paint(Rectangle[] highlights,AffineTransform viewScaling, Rectangle userAnnot);

	/**
	 * allow user to set component for waring message in renderer to appear -
	 * if unset no message will appear
	 * @param frame
	 */
	public abstract void setMessageFrame(Container frame);

	public abstract void paintBackground(Shape dirtyRegion);

	/* saves text object with attributes for rendering*/
	public abstract void drawText(float[][] Trm, String text, GraphicsState currentGraphicsState, float x, float y, Font javaFont);

	/**workout combined area of shapes in an area*/
	//public abstract Rectangle getCombinedAreas(Rectangle targetRectangle, boolean justText);

	/*setup renderer*/
	public abstract void init(int x, int y, int rawRotation, Color backgroundColor);
	
	/* save image in array to draw */
	public abstract int drawImage(int pageNumber, BufferedImage image,GraphicsState currentGraphicsState, boolean alreadyCached,String name, int optionsApplied,int previousUse);

	/**
	 * return which part of page drawn onto
	 * @return
	 */
	public abstract Rectangle getOccupiedArea();

	/*save shape in array to draw cmd is Cmd.F or Cmd.S */
	public abstract void drawShape(Shape currentShape,GraphicsState currentGraphicsState, int cmd);

        /*save shape in array to draw cmd is Cmd.F or Cmd.S */
	public abstract void drawShape(Object currentShape,GraphicsState currentGraphicsState, int cmd);

	/**reset on colorspace change to ensure cached data up to data*/
	public abstract void resetOnColorspaceChange();

	/*save shape colour*/
	public abstract void drawFillColor(PdfPaint currentCol);

	/*save opacity settings*/
	public abstract void setGraphicsState(int fillType, float value, int BM);

	/*Method to add Shape, Text or image to main display on page over PDF - will be flushed on redraw*/
	public abstract void drawAdditionalObjectsOverPage(int[] type, Color[] colors, Object[] obj) throws PdfException;

	public abstract void flushAdditionalObjOnPage();

	/*save shape colour*/
	public abstract void drawStrokeColor(Paint currentCol);

	/*save custom shape*/
	public abstract void drawCustom(Object value);

	/*save shape stroke*/
	public abstract void drawTR(int value);

	/*save shape stroke*/
	public abstract void drawStroke(Stroke current);

	/*save clip in array to draw*/
	public abstract void drawClip(GraphicsState currentGraphicsState, Shape defaultClip, boolean alwaysApply);

	/**
	 * store glyph info
	 */
	public abstract void drawEmbeddedText(float[][] Trm, int fontSize,
                                          PdfGlyph embeddedGlyph, Object javaGlyph, int type,
                                          GraphicsState gs, double[] at, String glyf, PdfFont currentFontData, float glyfWidth);

	/**
	 * store fontBounds info
	 */
	public abstract void drawFontBounds(Rectangle newfontBB);

	/**
	 * store af info
	 */
	public abstract void drawAffine(double[] afValues);

	/**
	 * store af info
	 */
	public abstract void drawFontSize(int fontSize);

	/**
	 * store line width info
	 */
	public abstract void setLineWidth(int lineWidth);

	/**
	 * Screen drawing using hi res images and not down-sampled images but may be slower
	 * and use more memory<br> Default setting is <b>false</b> and does nothing in
	 * OS version
	 */
	public abstract void setHiResImageForDisplayMode(boolean useHiResImageForDisplay);

	public abstract void setScalingValues(double cropX, double cropH,float scaling);

	/**stop screen bein cleared on repaint - used by Canoo code
	 *
	 * NOT PART OF API and subject to change (DO NOT USE)
	 **/
	public abstract void stopClearOnNextRepaint(boolean flag);

	public abstract void setCustomImageHandler(org.jpedal.external.ImageHandler customImageHandler);

	public abstract void setCustomColorHandler(org.jpedal.external.ColorHandler colorController);

	/**
	 * operations to do once page done
	 */
	public abstract void flagDecodingFinished();

	//used internally - please do not use
	public abstract ObjectStore getObjectStore();

	public abstract void flagImageDeleted(int i);

	public abstract void setOCR(boolean isOCR);

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * turn object into byte[] so we can move across
	 * this way should be much faster than the stadard Java serialise.
	 *
	 * NOT PART OF API and subject to change (DO NOT USE)
	 *
	 * @throws java.io.IOException
	 */
	public abstract byte[] serializeToByteArray(Set fontsAlreadyOnClient) throws IOException;

	/**
	 * for font if we are generatign glyph on first render
	 */
	public abstract void checkFontSaved(Object glyph, String name, PdfFont currentFontData);

	public abstract boolean hasObjectsBehind(float[][] CTM);

	/**
     * This method is deprecated, please use getAreaAsArray and
     * create fx/swing rectangles where needed.
     * @deprecated 
     * @param i
     * @return 
     */
    public abstract Rectangle getArea(int i);
    
    public abstract int[] getAreaAsArray(int i);

	/**
	 * return number of image in display queue
	 * or -1 if none
	 * @return
	 */
	public abstract int isInsideImage(int x, int y);

	public abstract void saveImage(int id, String des, String type);

	/**
	 * return number of image in display queue
	 * or -1 if none
	 * @return
	 */
	public abstract int getObjectUnderneath(int x, int y);


    public void setneedsVerticalInvert(boolean b);

    public void setneedsHorizontalInvert(boolean b);

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * just for printing
     */
    public abstract void stopG2HintSetting(boolean isSet);

    public abstract void setPrintPage(int currentPrintPage);

    public void writeCustom (int section, Object str);

    /**allow us to identify different types of renderer (ie HTML, Screen, Image)*/
	public int getType();

	/** allow tracking of specific commands **/
	public void flagCommand(int commandID, int tokenNumber);

    //generic method used by HTML to pass in values
    void setValue(int key, int i);

    //generic method used by HTML for getting values
    public int getValue(int key);

    BufferedImage getSingleImagePattern();

    /**used by JavaFX and HTML5 conversion to override scaling*/
    boolean isScalingControlledByUser();

    public boolean avoidDownSamplingImage();

    /**allow user to read*/
    public boolean getBooleanValue(int key);

    float getScaling();

    /**
     * only used in HTML5 and SVG conversion
     * @param baseFontName
     * @param s
     * @param potentialWidth
     */
    public void saveAdvanceWidth(String baseFontName, String s, int potentialWidth);
        
    public void setMode(Mode pdfType);
    public Mode getMode();

    Object getObjectValue(int id);


}

