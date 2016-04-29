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
 * DynamicVectorRenderer.java
 * ---------------
 */
package org.jpedal.render;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;
import org.jpedal.exception.PdfException;
import org.jpedal.external.FontHandler;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfShape;

public interface DynamicVectorRenderer  {

    int TEXT=1;
	int SHAPE=2;
	int IMAGE=3;
	int TRUETYPE=4;
	int TYPE1C=5;
	int TYPE3=6;
	int CLIP=7;
	int COLOR=8;
	int AF=9;
	int TEXTCOLOR=10;
	int FILLCOLOR=11;
	int STROKECOLOR=12;
	int STROKE=14;
	int TR=15;
	int STRING=16;
	int STROKEOPACITY=17;
	int FILLOPACITY=18;

	int STROKEDSHAPE=19;
	int FILLEDSHAPE=20;

	int FONTSIZE=21;
	int LINEWIDTH=22;

	int CUSTOM=23;

	//int fontBB=24;

    //int DELETED_IMAGE = 27;
    int REUSED_IMAGE = 29;

    int BLENDMODE = 31;

    int SAVE_EMBEDDED_FONT = 10;
    int TEXT_STRUCTURE_OPEN = 40;
    int TEXT_STRUCTURE_CLOSE = 42;

    int IsSVGMode = 44;
    int IsTextSelectable = 45;
    int IsRealText = 46;
    int MARKER=200;

	/**flag to enable debugging of painting*/
	boolean debugPaint=false;

	/**
	 * various types of DVR which we have
	 */
	int DISPLAY_SCREEN = 1;//
	int DISPLAY_IMAGE = 2;//
	int CREATE_PATTERN =3;
	int CREATE_HTML =4;
	int CREATE_SVG =5;
	int CREATE_EPOS =7;
        //int CREATE_SMASK =8;
        int CREATE_T3 =9;
	
	/**
	 * Keys for use with set value
	 */
	int ALT_BACKGROUND_COLOR=1;
	int ALT_FOREGROUND_COLOR=2;
	int FOREGROUND_INCLUDE_LINEART=3; //Alt foreground color changes lineart as well
	int COLOR_REPLACEMENT_THRESHOLD=4;
	int ENHANCE_FRACTIONAL_LINES=5; //Any line with width <1 is set to 1 to ensure visible (0 to turn off, 1 to turn on)
	
    /**
     * Used to pass in Graphics2D for all versions
     * @param g2
     */
	void setG2(Graphics2D g2);
    
    void eliminateHiddenText(Shape currentShape, GraphicsState gs, int segmentCount, boolean ignoreScaling);

    /**
     * Used by HTML/SVG to convert fonts
     * @return
     */
    FontHandler getFontHandler();

	/** 
     * Remove all dynamic page objects and flush queue 
     */
	void flush();

	/**
     * Dispose method should only be called once component finished with as
     * removes static resources as well
     */
	void dispose();

    /**
     * Renders all the objects onto the g2 surface for screen display
     * 
     * @param highlights
     * @param viewScaling
     * @param userAnnot
     */
	void paint(Rectangle[] highlights, AffineTransform viewScaling, Rectangle userAnnot);

    /**
     *
     * @param dirtyRegion
     */
    void paintBackground(final Shape dirtyRegion);

	/** Saves text object with attributes for rendering
     *
     * @param Trm
     * @param text
     * @param currentGraphicsState
     * @param x
     * @param y
     * @param javaFont
     */

	void drawText(float[][] Trm, String text, GraphicsState currentGraphicsState, float x, float y, Font javaFont);

	/**
     *
     * Set page values
     * 
     * @param width
     * @param height
     * @param backgroundColor
     */
	void init(int width, int height, Color backgroundColor);
	
	/** 
     * save image in array to draw 
     *
     * @param pageNumber
     * @param image
     * @param currentGraphicsState
     * @param alreadyCached
     * @param name
     * @param previousUse
     * @return
     */

	int drawImage(int pageNumber, BufferedImage image, GraphicsState currentGraphicsState, boolean alreadyCached, String name, int previousUse);

	/**
     * Used by Swing to Draw shape
     * @param currentShape
     * @param currentGraphicsState
     * @param cmd
     */
    void drawShape(PdfShape currentShape, GraphicsState currentGraphicsState, int cmd);
    
    /**
     * Used by FX to Draw shape
     * @param currentShape
     * @param currentGraphicsState
     */
    void drawShape(Object currentShape, GraphicsState currentGraphicsState);

	/** 
     * Reset on colorspace change to ensure cached data up to data
     */
	void resetOnColorspaceChange();
	
	/**
     * save opacity settings
     *
     * @param fillType
     * @param value
     * @param BM
     */
	void setGraphicsState(int fillType, float value, int BM);

    /**
     * Method to add Shape, Text or image to main display on page over PDF - will be flushed on redraw
     * @param type
     * @param colors
     * @param obj
     * @throws PdfException
     */

	void drawAdditionalObjectsOverPage(int[] type, Color[] colors, Object[] obj) throws PdfException;

    /**
     * Remove all GUI display values added by user for page
     */
    void flushAdditionalObjOnPage();

    /**
     *
     * @param value
     */
	void drawTR(int value);

    /**
     *
     * @param currentGraphicsState
     * @param defaultClip
     * @param alwaysApply
     */
    void drawClip(GraphicsState currentGraphicsState, Shape defaultClip, boolean alwaysApply);

	/**
	 * Store glyph info
     * @param Trm
     * @param fontSize
     * @param embeddedGlyph
     * @param javaGlyph
     * @param type
     * @param gs
     * @param at
     * @param glyf
     * @param currentFontData
     * @param glyfWidth
	 */
	void drawEmbeddedText(float[][] Trm, int fontSize,
						  PdfGlyph embeddedGlyph, Object javaGlyph, int type,
						  GraphicsState gs, double[] at, String glyf, PdfFont currentFontData, float glyfWidth);

    /**
     *
     * @param cropX
     * @param cropH
     * @param scaling
     */
    void setScalingValues(double cropX, double cropH, float scaling);

    /**
     *
     * @param customImageHandler
     */
    void setCustomImageHandler(org.jpedal.external.ImageHandler customImageHandler);

    /**
     *
     * @param colorController
     */
    void setCustomColorHandler(org.jpedal.external.ColorHandler colorController);

	/**
	 * Execute operations to do once page done
	 */
	void flagDecodingFinished();

	/**
	 * Turn object into byte[] so we can move across
	 * this way should be much faster than the stadard Java serialise.
	 *
	 * NOT PART OF API and subject to change (DO NOT USE)
	 *
     * @param fontsAlreadyOnClient
     * @return 
	 * @throws java.io.IOException
	 */
	byte[] serializeToByteArray(Set<String> fontsAlreadyOnClient) throws IOException;

    /**
     * Checks to see if an object is hidden behind the rectangular outline of 
     * another (does not allow for any transparency)
     * 
     * @param CTM
     * @return
     */
    boolean hasObjectsBehind(float[][] CTM);

    /**
     * Generic method to set any values as an object
     * 
     * @param key key value defined in this interface or inheriting class
     * @param obj value for key settings
     */
    void writeCustom(int key, Object obj);

    /**
     * Allow us to identify different types of renderer (ie HTML, Screen, Image)
     * @return value defined in this interface or inheriting class 
     */
	int getType();

	/** Allow tracking of specific commands so we can spot separete TJ commands
     *
     * @param tokenNumber
     */
	void updateTokenNumber(int tokenNumber);

    /**
     * Generic method to set int values
     * 
     * @param key key value defined in this interface or inheriting class
     * @param i value for key settings
     */
    void setValue(int key, int i);

    /**
     * Generic method to access int values
     * 
     * @param key - int key value defined in this interface or inheriting class
     * @return - an int value
     */
	int getValue(int key);

    /**
     * Generic method to access boolean flags
     * 
     * @param key - int key value defined in this interface or inheriting class
     * @return true/false boolean
     */
	boolean getBooleanValue(int key);

    /**
     * Passes in width value for font glyphs  - only used in HTML5 and SVG conversion
     * @param fontObjID
     * @param glyphName
     * @param potentialWidth
     */
	void saveAdvanceWidth(int fontObjID, String glyphName, int potentialWidth);
    
    /**
     * Allows generic code to identify if it is doing HTML/SVG conversion
     * 
     * @return boolean which will be true for HTML/SVG conversion and otherwise false 
     */
    boolean isHTMLorSVG();


}

