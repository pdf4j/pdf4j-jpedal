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
 * OutputDisplay.java
 * ---------------
 */
package org.jpedal.render.output;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.print.attribute.standard.PageRanges;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.PdfPaint;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfFileInformation;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.TextState;
import org.jpedal.parser.Cmd;
import org.jpedal.parser.image.ImageUtils;
import org.jpedal.render.BaseDisplay;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.render.ImageDisplay;
import org.jpedal.render.ShapeFactory;
import org.jpedal.render.output.io.*;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Matrix;
import org.jpedal.utils.StringUtils;
import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;
import org.json.JSONStringer;
import org.w3c.dom.Document;

@SuppressWarnings("PMD")
public abstract class OutputDisplay extends BaseDisplay{

    protected Integer decimalsAllowed;

    //page range
    protected int endPage;

    /**used to filter out empty tags in Structured content*/
    private String lastStructuredElement="";
    private boolean hasContentInStructure=false;

    private boolean keepOriginalImage;

    private ImageDisplay thumbnailDisplay, imageDisplay, compatabilityDisplay;

    protected boolean addIDRLogo;

    protected boolean requiresTransform;
    protected boolean requiresTextGlobal;

    public static final int TEXT_SVG_REALTEXT = 1;
    public static final int TEXT_SVG_SHAPETEXT_SELECTABLE = 2;
    public static final int TEXT_SVG_SHAPETEXT_NONSELECTABLE = 3;
    public static final int TEXT_SVG_REALTEXT_WITH_IE8FALLBACK = 4;
    public static final int TEXT_SVG_SHAPETEXT_NONSELECTABLE_WITH_IE8FALLBACK = 6;
    public static final int TEXT_IMAGE_REALTEXT = 7;
    public static final int TEXT_IMAGE_SHAPETEXT_SELECTABLE = 8;
    public static final int TEXT_IMAGE_SHAPETEXT_NONSELECTABLE = 9;
    public static final int TEXT_IMAGE_REALTEXT_WITH_IE8FALLBACK = 10;
    public static final int TEXT_IMAGE_SHAPETEXT_NONSELECTABLE_WITH_IE8FALLBACK = 12;

    public static final int VIEW_CONTENT = 0;
    public static final int VIEW_PAGETURNING = 11;
    public static final int VIEW_IDR = 12;

    protected String clip;
    //The following fields used to cache clip paths
    private String currentClip = "";
    protected String currentOpenClip = "";
    private String clipForDiffOnly = "";
    private final HashMap<String, String> clipPathsWithoutID = new HashMap<String, String>();

    //provides common static functions
    protected static org.jpedal.render.output.OutputHelper Helper;

//    private FontMapper fontMapper;
//    private String lastFontUsed="";


    private final Map<String, String> usedFontIDs = new HashMap<String, String>();

    /**track if JS for glyf already inserted*/
    private final Set<String> glyfsRasterized=new HashSet<String>();

    public static final int FontMode=1;
    public static final int OutputThumbnails = 2;


    /**ie file we Launch via button from form*/
    public static final int ConvertPDFExternalFileToOutputType=38;

    protected boolean isSVGMode;
    protected boolean isTextSelectable;
    protected boolean isRealText;
    protected boolean disableImageFallback = true;

    public static final int IsSVGMode = 44;
    public static final int IsTextSelectable = 45;
    public static final int IsRealText = 46;

    public static final int DisableImageFallback = 47;
//    public static final int PageTurning = 55;
    public static final int AddJavaScript = 56;
    public static final int EmbedImagesAsBase64 = 58;
    public static final int Base64Background = 59;
    public static final int DrawInvisibleText = 61;

    public static final int FontsToRasterizeInTextMode = 62;
    public static final int ViewMode = 63;


    /** debug flags */
    public static final boolean debugForms = false;

    private static final boolean DISABLE_SHAPE = false;

    public static final int TOFILE = 0;
    public static final int ANNOTATION_SCRIPT = 1;
    public static final int FORM = 3;
    public static final int CSS = 4;
    public static final int FORM_CSS = 5;
    public static final int TEXT = 6;
    public static final int JSIMAGESPECIAL = 9;
    public static final int SAVE_EMBEDDED_FONT = 10;
    public static final int IMAGE_CONTROLLER = 12;
    public static final int FONT_AS_SHAPE = 14;
    public static final int FORMJS = 16;
    public static final int FORMJS_ONLOAD = 17;
    public static final int TEXTJS = 20;
    public static final int CALCULATION_ORDER = 21;
    
    public static final int SVGINHTML = 22;
    public static final int SVGCLIPS = 23;

    public static final int SVGBUFFER = 25;

    //public static final int DVR = 26;

    public static final int LEGACY_CSS = 28;

    public static final int BOOKMARK = 29;
    public static final int PDF_FILE_INFORMATION = 30;

    public static final int CustomIO = 31;

    public static final int HasJavaScript =32;

    public static final int THUMBNAIL_DISPLAY=35;

    public static final int IMAGE_DISPLAY=37;

    public static final int COMPATABILITY_DISPLAY=38;
    
    public static final int TEXT_STRUCTURE_OPEN=40;
    public static final int TEXT_STRUCTURE_CLOSE=42;

    protected OutputImageController imageController;

    protected final StringBuilder svgInHTML = new StringBuilder(10000);
    protected final StringBuilder svgClips = new StringBuilder(10000);
    protected final ArrayList<String> calculationOrder = new ArrayList<String>();

    protected final StringBuilder fonts_as_shapes = new StringBuilder(10000);
    protected final StringBuilder formJS = new StringBuilder(10000);
    protected final StringBuilder formJSOnLoad = new StringBuilder(10000);
    protected final StringBuilder form = new StringBuilder(10000);

    protected final ArrayList<String> listOfTextDivs = new ArrayList<String>(10000);

    protected final Map<String, String> textDivs = new HashMap<String, String>();
    protected final Map<String, Rectangle> textDivLocation=new HashMap<String, Rectangle>();
    protected final Map<String, Integer> textDivLocationCount=new HashMap<String, Integer>();

    protected final StringBuilder topSection = new StringBuilder(10000);
    protected StringBuilder idrViewer = new StringBuilder(10000);
    protected final StringBuilder form_css = new StringBuilder(10000);
    protected final StringBuilder css = new StringBuilder(10000);
    protected final ArrayList<String> textJS = new ArrayList<String>(3000);
    protected final Map<String, String> textJSDivs = new HashMap<String, String>();

    protected final StringBuilder svgBuffer = new StringBuilder(10000);
    protected final StringBuilder legacyCss = new StringBuilder(10000);

    /**allow user to control scaling of images*/
    protected boolean userControlledImageScaling;

    /**current text element number if using Divs. Used as link to CSS*/
    protected int textID=1;

    protected int shadeId;
    protected int imageId;

    protected int clipCount; // used for clipping id in SVG
    private boolean newClip; //Used to only write clip once

    public final String rootDir;
    public final String fileName;

    protected final int pageRotation;

    protected float imageScaleX,imageScaleY;

    private TextBlock currentTextBlock;

    protected final Rectangle2D cropBox;
    protected final Rectangle2D cropBoxWithOffset;
    protected final Point2D midPoint;

    //Final width/height with scaling taken into account
    protected final int canvasHeightScaled;
    protected final int canvasWidthScaled;

    /**control for encodings Java/CSS*/
    protected String[] encodingType= {"UTF-8","utf-8"};
    protected static final int JAVA_TYPE=0;
    protected static final int OUTPUT_TYPE =1;


    //flag to say if JS has been added to allow images to work for checkboxes and radio buttons.
    protected boolean jsImagesAdded;

    protected boolean hasJavascript;

    protected final String pageNumberAsString;
    protected final PdfPageData pageData;
    private int currentTokenNumber=-1;

    protected String imageName;

    /**handles IO so user can override*/
    protected CustomIO customIO;

    protected String imageArray;

    protected int[] currentImage;
    protected int[] currentPatternedShape;
    protected String currentPatternedShapeName;

    protected BufferedImage base64Background;

    protected boolean enableAnalytics;

    //Settings begin here
    protected PageRanges pageRange;//Default set in setSettings()
    protected boolean fullNumbering;//Default set in setSettings()
    protected int viewMode;//Default set in setSettings()
    private int textMode;//Default set in setSettings(). Please do not change visibility. Get what you need from isRealText, isTextSelectable and isSVGMode.
    protected float scaling;//Default set in setSettings(). Scale too large and images may be lost due to lack of memory.
    protected String googleAnalyticsID;//Default set in setSettings()
    protected String pageTurningAnalyticsPrefix;//Default set in setSettings()
    protected String insertIntoHead;//Default set in setSettings()
    protected boolean enableComments;//Default set in setSettings()
    protected boolean embedImageAsBase64;//Default set in setSettings()
    protected boolean useWordSpacing;//Default set in setSettings()
    protected boolean addJavaScript;//Default set in setSettings()
    private boolean convertSpacesTonbsp;//Default set in setSettings()
    private boolean convertPDFExternalFileToOutputType;//Default set in setSettings()
    protected String formTag;//Default set in setSettings()
    protected int fontMode;//Default set in setSettings()
    protected boolean writeEveryGlyf;//Default set in setSettings()
    protected boolean separateToWords;
    protected boolean addBorder;//Default set in setSettings(). SVG only.
    protected boolean addCut;//Default set in setSettings(). EPOS only.
    protected boolean inlineSVG;//Default set in setSettings()
    protected boolean outputThumbnails;//Default set in setSettings()
    protected boolean addOverlay;//Default set in setSettings()
    protected String[] toolBarLink;//Default set in setSettings).
    protected boolean toolBarPDFLink;//Default set in setSettings()
    protected boolean disableLinkGeneration;//Default set in setSettings()
    protected boolean useLegacyImageFileType;//Default set in setSettings()
    protected boolean completeDocument;//Default set in setSettings()
    //Settings end here


    protected Integer[] pageNums;
    protected int outputPageNumber;
    protected int pageCount;

    /**
     * used in realtext modes where we actually put some fonts onto image or svg layer
     */
    protected boolean hasComplexText;

    protected boolean ieCompatibilityMode;

    protected Document bookmarks;
    protected PdfFileInformation pdfFileInformation;

    protected FontHelper fontHelper;

    public OutputDisplay(final int type, final String rootDir, final int pgNumber, final int pgCount, final PdfPageData pageData, final ConversionOptions settings, final OutputModeOptions outputModeOptions) {


        this.type = type;
        this.rootDir = rootDir;
        pageCount = pgCount;
        endPage = pageCount;
        this.pageData = pageData;
        rawPageNumber = pgNumber;
        outputPageNumber = rawPageNumber;
        this.objectStoreRef = new ObjectStore();
        this.addBackground = false;

        setSettings(settings, outputModeOptions);
        validateSettings();

        pageNumberAsString = String.valueOf(outputPageNumber);//No padded 0's and ignore firstPageName
        fileName = pageNumberAsString;//Both fileName and pageNumberAsString are the same.

        final float cropX = pageData.getCropBoxX2D(rawPageNumber);
        final float cropY = pageData.getCropBoxY2D(rawPageNumber);
        final float cropW = pageData.getCropBoxWidth2D(rawPageNumber);
        final float cropH = pageData.getCropBoxHeight2D(rawPageNumber);
        pageRotation = pageData.getRotation(rawPageNumber);

        //Create Rectangle object to match width and height of cropbox
        this.cropBox = new Rectangle2D.Double(0, 0, cropW, cropH);
        this.cropBoxWithOffset = new Rectangle2D.Double(cropX, cropY, cropW, cropH);
        //Find middle of cropbox in Pdf Coordinates
        this.midPoint = new Point2D.Double((cropW / 2) + cropX, (cropH / 2) + cropY);

        // Calculate the canvas width & heights for use in the HTML output for canvas, divs etc
        if (pageRotation == 90 || pageRotation == 270) {
            canvasHeightScaled = (int)(cropW * scaling);
            canvasWidthScaled = (int)(cropH * scaling);
        } else {
            canvasWidthScaled = (int)(cropW * scaling);
            canvasHeightScaled = (int)(cropH * scaling);
        }

        //setupArrays(100);
        areas = new Vector_Rectangle_Int(100);
        imageAndShapeAreas = new Vector_Rectangle_Int(100);

    }

    protected void setSettings(final ConversionOptions conversionOptions, final OutputModeOptions outputModeOptions) {

        addIDRLogo = conversionOptions.getAddIDRViewerLogo();

        // Generic settings
        final PageRanges logicalPageRanges = conversionOptions.getLogicalPageRange();
        if (logicalPageRanges != null) {
            fullNumbering = false;
            pageRange = logicalPageRanges;
        } else {
            fullNumbering = true;
            pageRange = conversionOptions.getRealPageRange();
        }
        //If no page range set, use default (1 - pageCount)
        if (pageRange == null) {
            pageRange = new PageRanges("1 - " + pageCount);
        }

        viewMode = outputModeOptions.getValue();
        textMode = conversionOptions.getTextMode().getValue();
        switch (conversionOptions.getScalingMode()) {
            case SCALE:
                scaling = getScalingValue(String.valueOf(conversionOptions.getScaling()), rawPageNumber);
                break;
            case FITWIDTH:
                scaling = getScalingValue("fitwidth" + conversionOptions.getScalingFitWidth(), rawPageNumber);
                break;
            case FITHEIGHT:
                scaling = getScalingValue("fitheight" + conversionOptions.getScalingFitHeight(), rawPageNumber);
                break;
            case FITWIDTHHEIGHT:
                final int[] widthHeight = conversionOptions.getScalingFitWidthHeight();
                scaling = getScalingValue(widthHeight[0] + "x" + widthHeight[1], rawPageNumber);
                break;
        }
        embedImageAsBase64 = conversionOptions.getEmbedImagesAsBase64Stream();
        convertSpacesTonbsp = conversionOptions.getConvertSpacesToNbsp();
        convertPDFExternalFileToOutputType = conversionOptions.getConvertPDFExternalFileToOutputType();
        formTag = conversionOptions.getFormTag();
        fontMode = conversionOptions.getFontMode().getValue();
        writeEveryGlyf = conversionOptions.getKeepGlyfsSeparate();
        separateToWords = conversionOptions.getSeparateToWords();
        encodingType = conversionOptions.getEncoding();
        keepOriginalImage = conversionOptions.getKeepOriginalImage();
        disableLinkGeneration = conversionOptions.getDisableLinkGeneration();
        useLegacyImageFileType = conversionOptions.getUseLegacyImageFileType();


        //allow user to control amount of rounding on shapes
        decimalsAllowed=conversionOptions.getDecimalsToRetainInShapes();


        if (viewMode == VIEW_CONTENT) {
            final ContentOptions contentOptions = (ContentOptions) outputModeOptions;
            outputThumbnails = contentOptions.getOutputThumbnails();
            completeDocument = contentOptions.getCompleteDocument();
        }

        if (viewMode == VIEW_IDR) {
            final IDRViewerOptions idrViewerOptions = (IDRViewerOptions) outputModeOptions;

            googleAnalyticsID = idrViewerOptions.getGoogleAnalyticsID();
            if (googleAnalyticsID != null) {
                enableAnalytics = true;
            }
            pageTurningAnalyticsPrefix = idrViewerOptions.getPageTurningAnalyticsPrefix();
            insertIntoHead = idrViewerOptions.getInsertIntoHead();
            toolBarLink = idrViewerOptions.getToolBarLink();
            toolBarPDFLink = idrViewerOptions.getEnableToolBarPDFDownload();
        }


        // Setup fontHelper
        final ConversionOptions.Font[] includedFonts = conversionOptions.getIncludedFonts();
        final String fontsToRasterize = conversionOptions.getFontsToRasterizeInTextMode();//setup font override in realtext modes
        final boolean preserveExtractionValues = conversionOptions.getPreserveExtractionValues();
        fontHelper = new FontHelper(rawPageNumber, includedFonts, fontMode, preserveExtractionValues, fontsToRasterize);
    }

    private void validateSettings() {

        //Populate our pageNums array
        final ArrayList<Integer> pgNumberBuilder = new ArrayList<Integer>();
        int pgNum = pageRange.next(0);
        while(pgNum != -1) {
            pgNumberBuilder.add(pgNum);
            pgNum = pageRange.next(pgNum);
        }
        pageNums = pgNumberBuilder.toArray(new Integer[pgNumberBuilder.size()]);

        if (!fullNumbering) {
            //Calculate the page number relative to the other pages
            pageCount = pageNums.length;
            endPage = pageCount;
            outputPageNumber = convertRawPageToOutputPageNumber(rawPageNumber);
        }

        /**
         * Text Mode
         */
        // isSVGMode determines if we need SVG output
        // isRealText determines if we need text output or not
        // isTextSelectable determines if the text is visible or invisible
        switch(textMode) {
            case TEXT_SVG_REALTEXT_WITH_IE8FALLBACK:
                disableImageFallback = false;
                ieCompatibilityMode = true;
                // Deliberate fall-through
            case TEXT_SVG_REALTEXT:
                isSVGMode = true;
                isTextSelectable = true;
                isRealText = true;
                break;

            case TEXT_SVG_SHAPETEXT_SELECTABLE:
                isSVGMode = true;
                isTextSelectable = true;
                isRealText = false;
                break;

            case TEXT_SVG_SHAPETEXT_NONSELECTABLE_WITH_IE8FALLBACK:
                disableImageFallback = false;
                ieCompatibilityMode = true;
                // Deliberate fall-through
            case TEXT_SVG_SHAPETEXT_NONSELECTABLE:
                isSVGMode = true;
                isTextSelectable = false;
                isRealText = false;
                break;

            case TEXT_IMAGE_REALTEXT_WITH_IE8FALLBACK:
                ieCompatibilityMode = true;
                // Deliberate fall-through
            case TEXT_IMAGE_REALTEXT:
                isSVGMode = false;
                isTextSelectable = true;
                isRealText = true;
                break;
            case TEXT_IMAGE_SHAPETEXT_SELECTABLE:
                isSVGMode = false;
                isTextSelectable = true;
                isRealText = false;
                break;
            case TEXT_IMAGE_SHAPETEXT_NONSELECTABLE_WITH_IE8FALLBACK:
                ieCompatibilityMode = true;
                // Deliberate fall-through
            case TEXT_IMAGE_SHAPETEXT_NONSELECTABLE:
                isSVGMode = false;
                isTextSelectable = false;
                isRealText = false;
                break;
        }

        //Thumbnails are a requirement of the IDRViewer
        if (viewMode != VIEW_CONTENT) {
            outputThumbnails = true;
        }

        //Inline SVG is only allowed if textMode is svg_*_nofallback
        if (inlineSVG) {
            inlineSVG = isSVGMode && disableImageFallback;
        }

    }

    private float getScalingValue(final String userScaling, final int pageToDecode) {
        // See http://blog.idrsolutions.com/2013/05/what-size-is-100-scaling-in-pdf/ for an explanation of PDF scaling

        //allow user to pass in value from command line
        try {
            // Set scaling taking into account Java/HTML DPI of 72 & Adobe default of 110.
            float scaling = 110f / 72; //same size as PDF

            if(userScaling!=null){
                if(userScaling.contains("x")){

                    //Values separated by the x
                    final String delimiter = "x";

                    //Temp storage for prefPageSize before converting to float
                    final String[] temp = userScaling.split(delimiter);

                    //store the first and second values as individual float values
                    float prefWidth = Integer.parseInt(temp[0]);
                    float prefHeight = Integer.parseInt(temp[1]);

                    // Knowing that we always round the final width/height down, this will help us achieve a width and height
                    // as close as possible to those asked for and avoid the smaller value being 1px too small.
                    prefWidth += .99f;
                    prefHeight += .99f;

                    //Actual size of the PDF document
                    final float actualWidth = pageData.getCropBoxWidth2D(pageToDecode);
                    final float actualHeight = pageData.getCropBoxHeight2D(pageToDecode);

                    //Scaling to apply to actual size of PDF to achieve preferred size
                    final float dScaleW = prefWidth / actualWidth;
                    final float dScaleH = prefHeight / actualHeight;

                    //Use the smaller scaling in case the aspect ratio is wrong which would cause us to get a larger width or height
                    scaling=dScaleW;
                    if(scaling>dScaleH) {
                        scaling = dScaleH;
                    }

                }else if(userScaling.contains("fitwidth")){
                    final String preferWidth = userScaling.substring(8);
                    final float pw = Integer.parseInt(preferWidth) + .99f;
                    final float actualWidth = pageData.getCropBoxWidth2D(pageToDecode);
                    scaling = pw/actualWidth;

                }else if(userScaling.contains("fitheight")){
                    final String preferHeight = userScaling.substring(9);
                    final float ph = Integer.parseInt(preferHeight) + .99f;
                    final float actualHeight = pageData.getCropBoxHeight2D(pageToDecode);
                    scaling = ph/actualHeight;
                }else{
                    if (userScaling.equals("none")) {
                        scaling = 1;
                    } else {
                        scaling *= Float.parseFloat(userScaling);
                    }
                }
            }
            return scaling;
        } catch (final Exception e) {

            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception in handling scaling value "+e);
            }
            return -1;
        }
    }

    @Override
    public Object getObjectValue(final int key) {

        switch (key) {
            case FontsToRasterizeInTextMode:
                return fontHelper.getFontRasterizer();

            case CustomIO:
                return customIO;

            default:
                return super.getObjectValue(key);
        }
    }

    @Override
    public boolean getBooleanValue(final int key){

        switch(key){
            case AddJavaScript:
                return addJavaScript;

            case ConvertPDFExternalFileToOutputType:
                return convertPDFExternalFileToOutputType;

            case DisableImageFallback:
                return disableImageFallback;

            case EmbedImagesAsBase64:
                return embedImageAsBase64;

            case HasJavaScript:
                return hasJavascript;

            case IsSVGMode:
                return isSVGMode;

            case IsRealText:
                return isRealText;

            case IsTextSelectable:
                return isTextSelectable;

            case OutputThumbnails:
                return outputThumbnails;

            default:
                return super.getBooleanValue(key);
        }
    }

    //allow user to control various values
    @Override
    public int getValue(final int key){

        int value=-1;

        switch(key){

            case ViewMode:
                value = viewMode;
                break;

            case FontMode:
                value=fontMode;
                break;

        }

        return value;
    }

    /*setup renderer*/
    @Override
    public void init(final int width, final int height, final int rawRotation, final Color backgroundColor) {

        this.backgroundColor = backgroundColor;

    }

    /**
     * Add output to correct area so we can assemble later.
     * Can also be used for any specific code features (ie setting a value)
     */
    @Override
    public synchronized void writeCustom(final int section, final Object str) {

        switch(section){
            case TOFILE:
                customIO.writeString(str.toString());
                break;

            case FORM_CSS:
                // <start-demo><end-demo>

                form_css.append(str);
                break;
            case IMAGE_CONTROLLER:

                this.imageController=(OutputImageController)str;
                this.userControlledImageScaling=imageController!=null;
                break;

            case Base64Background:
                this.base64Background = (BufferedImage)str;
                break;

            case BOOKMARK:
                bookmarks = (Document) str;
                break;

            case CustomIO:
                this.customIO=(CustomIO)str;
                break;

            case HasJavaScript:
                hasJavascript=(Boolean)str;
                break;

            case FONT_AS_SHAPE:
                fonts_as_shapes.append('\t').append(str).append('\n');
                break;

            case PDF_FILE_INFORMATION:
                pdfFileInformation = (PdfFileInformation) str;
                break;

            case THUMBNAIL_DISPLAY:
                thumbnailDisplay= (ImageDisplay) str;
                break;

            case IMAGE_DISPLAY:
                imageDisplay= (ImageDisplay) str;
                break;

            case COMPATABILITY_DISPLAY:
                compatabilityDisplay= (ImageDisplay) str;
                break;

            //special case used from PdfStreamDecoder to get font data
            case SAVE_EMBEDDED_FONT:

                //save font data as file
                final Object[] fontData = (Object[]) str;
                final PdfFont pdfFont = (PdfFont)fontData[0];
                final byte[] rawFontData= (byte[]) fontData[1];
                final String fileType = (String)fontData[2];

                fontHelper.saveEmbeddedFont(pdfFont, rawFontData, fileType);

                break;

            case TEXT:
                hasContentInStructure=true;
                listOfTextDivs.add(str.toString());
                break;

            case TEXT_STRUCTURE_OPEN:
                hasContentInStructure=false;
                lastStructuredElement=str.toString();
                flushText();
                listOfTextDivs.add("<div data-struct=\""+lastStructuredElement+"\">");
                break;
                
            case TEXT_STRUCTURE_CLOSE:
                flushText();
                final String nextKey=str.toString();
                if(hasContentInStructure || !lastStructuredElement.equals(nextKey)){
                    listOfTextDivs.add("</div>");
                }else{
                    listOfTextDivs.remove(listOfTextDivs.size()-1);
                }
                lastStructuredElement="";
                break;    
                
            default:
                throw new RuntimeException("Option "+section+" not recognised");
        }
    }



    @Override
    public synchronized void flagDecodingFinished(){

        //flush any cached text before we write out
        flushText();

        if(customIO !=null && customIO.isOutputOpen()){
            fontHelper.flagDecodingFinished(rootDir, fileName, customIO);

            completeOutput();

            // Finish writing & close the output stream
            customIO.flush();
        }
    }

    protected int objectAreaCount =-1;

    /**used to ensure unique number name for all XFA images*/
    private int imageNumber;

    // save image in array to draw
    @Override
    public int drawImage(final int pageNumber, final BufferedImage image, final GraphicsState gs, final boolean alreadyCached, final String name, final int optionsApplied, final int previousUse) {

        final boolean debug=false;

        if(debug){
            System.out.println('\n' +name+" page rot="+pageRotation+" CTM="+gs.CTM[0][0]+ ' ' +gs.CTM[0][1]+ ' ' +gs.CTM[1][0]+ ' ' +gs.CTM[1][1]+" useHiResImageForDisplay="+useHiResImageForDisplay+" optionsApplied="+optionsApplied);
            try {
                new File("/Users/markee/desktop/imgs/").mkdir();
                ImageIO.write(image, "PNG", new File("/Users/markee/desktop/imgs/"+name+".png"));
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }

        /**
         * we can generate up to 2 images so we draw images onto them
         */
        if(previousUse==-3 && (thumbnailDisplay!=null || imageDisplay!=null)){

            if(thumbnailDisplay!=null){
                thumbnailDisplay.drawImage(pageNumber, image, gs, alreadyCached, name, optionsApplied, previousUse);
            }

            if(imageDisplay!=null){
                imageDisplay.drawImage(pageNumber, image, gs, alreadyCached, name, optionsApplied, previousUse);
            }

            return -1;
        }else{

            /**
             * also write out image if needed
             */
            if(previousUse==-2 && !isSVGMode){
                return -1;
            }else {
                // CTMwithPageRotation=getCombinedCTM(gs.CTM,pageNumber);

                return generateExternalImageForSVG(optionsApplied, image, gs, name, pageNumber);
            }
        }
    }

    private int generateExternalImageForSVG(final int optionsApplied, BufferedImage image, final GraphicsState gs, String name, final int pageNumber) {

        final boolean debug=false;

        if(debug){
            System.out.println('\n' +name+" page rot="+pageRotation+" CTM="+gs.CTM[0][0]+ ' ' +gs.CTM[0][1]+ ' ' +gs.CTM[1][0]+ ' ' +gs.CTM[1][1]+" useHiResImageForDisplay="+useHiResImageForDisplay+" optionsApplied="+optionsApplied);
            try {
                new File("/Users/markee/desktop/imgs/").mkdir();
                ImageIO.write(image, "PNG", new java.io.File("/Users/markee/desktop/imgs/"+name+".png"));
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }

        /**
         * name will be null from XFA images (causing issue with cache code below
         * so we create unique name for null names to ensure all XFA files
         * include all images correctly (and should make output more readable)
         */
        if (name == null) {
            //ensure we have a unique name
            name = "XFA_IM" + imageNumber;
            imageNumber++;
        }

        //flush any cached text before we write out
        flushText();

        //figure out co-ords
        float x = (gs.CTM[2][0] * scaling);
        float y = (gs.CTM[2][1] * scaling);
        float iw,ih;
        if(gs.CTM[0][0]<0 && gs.CTM[1][1]<0 && gs.CTM[0][1]<0 && gs.CTM[1][0]>0 ){
            iw = (Math.abs(gs.CTM[0][0])+Math.abs(gs.CTM[1][0])) * scaling;
            ih = (Math.abs(gs.CTM[1][1])+Math.abs(gs.CTM[0][1])) * scaling;
        }else{
            iw = (gs.CTM[0][0]+Math.abs(gs.CTM[1][0])) * scaling;
            ih = (gs.CTM[1][1]+Math.abs(gs.CTM[0][1])) * scaling;
        }
        //value can also be set this way but we need to adjust the x coordinate
        if (iw == 0){
            iw = gs.CTM[1][0] * scaling;

            if (iw < 0) {
                iw = -iw;
            }
        }
        //again value can be set this way
        if (ih == 0){
            ih = gs.CTM[0][1] * scaling;
        }
        //Reset with to positive if negative
        if(iw<0) {
            iw *= -1;
        }
        if(iw < 1) {
            iw = 1;
        }
        if(ih == 0) {
            ih = 1;
        }
        //Account for negative widths (ie ficha_acceso_a_ofimatica_e-portafirma.pdf)
        if(ih < 1) {
            y += ih;
            ih = Math.abs(ih);
        }
        //add negative width value to of the x coord problem_document2.pdf page 3
        if(gs.CTM[0][0] < 0){
            x -= iw;
        }

        //factor in offset if not done above
        if(gs.CTM[1][0]<0 && gs.CTM[0][0]!=0){
            x += (gs.CTM[1][0]*scaling);
        }

        if(gs.CTM[1][0]<0 && gs.CTM[0][0]==0){
            x -= iw;
        }
        
        //(guess for bottom right image on general/forum/2.html)
        if(gs.CTM[0][1]<0 && gs.CTM[1][1]!=0 ){// && gs.CTM[0][0]<gs.CTM[1][0]){
            y += (gs.CTM[0][1]*scaling);
        }

        if(gs.CTM[0][1]<0 && gs.CTM[1][1]==0){
            y -= ih;
        }

        double[] coords;
        // Covert PDF coords (factor out scaling in calc)
        switch(pageRotation){

            //adjust co-ords
            //image actually rotated in rotateImage()

            case 180:
                coords =new double[]{cropBox.getWidth()-((iw+x)/scaling),cropBox.getHeight()-((ih+y)/scaling)};
                break;

            //
            case 270:
                // System.out.println(gs.CTM[0][0]+" "+gs.CTM[1][0]+" "+" "+gs.CTM[0][1]+" "+gs.CTM[1][1]);

                //special case /PDFdata/sample_pdfs_html/samsung/rotate270.pdf
                if(gs.CTM[0][0]!=0 && gs.CTM[1][1]!=0 && gs.CTM[1][0]==0 && gs.CTM[0][1]==0){
                    coords =new double[]{cropBox.getWidth()-((x+iw)/scaling),y/scaling};
                }else{
                    coords =new double[]{x/scaling,y/scaling};
                }
                break;

            default:
                coords =new double[]{x/scaling,y/scaling};
                break;
        }
        correctCoords(coords);

        //add back in scaling
        coords[0] *= scaling;
        coords[1] *= scaling;

        //subtract image height as y co-ordinate inverted
        coords[1] -= ih;

        final Rectangle2D rect = new Rectangle2D.Double(coords[0], coords[1], iw, ih);
        final Rectangle2D cropBoxScaled = new Rectangle2D.Double(cropBox.getX()*scaling, cropBox.getY()*scaling,
                cropBox.getWidth()*scaling, cropBox.getHeight()*scaling);
        int finalWidth = (int) ((coords[0] - ((int) coords[0])) + iw + 0.99);
        int finalHeight = (int) ((coords[1] - ((int) coords[1])) + ih + 0.99);

        final int fw=finalWidth;
        final int fh=finalHeight;

        if(cropBoxScaled.intersects(rect) && finalWidth > 0 && finalHeight > 0) {

            //swap values
            if(pageRotation==90 || pageRotation==270){
                final float a = iw;
                iw=ih;
                ih=a;
            }

            // From the PDF spec - If an Image overlaps a pixel by more than 50%, the pixel gets filled, otherwise not.
            // Slight difference here, if an image overlaps a pixel at all, it gets filled in this code.
            final int finalX;
            final int finalY;
            switch(pageRotation){
                case 90:
                    final double tmpX = pageData.getCropBoxHeight2D(pageNumber)*scaling - coords[1] - iw;
                    final double tmpY = coords[0];
                    finalWidth = (int)((tmpX - ((int)tmpX)) + iw + 0.99);
                    finalHeight = (int)((tmpY - ((int)tmpY)) + ih + 0.99);
                    finalX = (int)tmpX;
                    finalY = (int)tmpY;
                    break;

                case 270:
                      finalWidth = (int) ((coords[1] - ((int) coords[1])) + iw + 0.99);
                    finalHeight = (int) ((coords[0] - ((int) coords[0])) + ih + 0.99);
                    finalX = (int) (coords[1]);
                    if (gs.CTM[0][1] != 0 && gs.CTM[1][0] != 0) {
                        if (gs.CTM[0][0] > 0 && gs.CTM[0][1] > 0 && gs.CTM[1][0] < 0 && gs.CTM[1][1] > 0) {
                            
                            finalY = (int) ((int) ((cropBox.getWidth() * scaling) - coords[0] - ih)+gs.CTM[1][1]);
                        } else {
                            finalY = (int) ((cropBox.getWidth() * scaling) - coords[0] - ih);
                        }

                    } else {
                        finalY = (int) (coords[0]);
                    }
                    break;

                default:
                    finalWidth = (int)((coords[0] - ((int)coords[0])) + iw + 0.99);
                    finalHeight = (int)((coords[1] - ((int)coords[1])) + ih + 0.99);

                    if(gs.CTM[0][0]<0 && gs.CTM[0][1]<0 && gs.CTM[1][0]>0 && gs.CTM[1][1]<0 && pageData.getCropBoxWidth(pageNumber)>pageData.getCropBoxHeight(pageNumber)){                   
                        finalX = (int)(coords[0]+(finalHeight-((cropBox.getWidth()/scaling)-(cropBox.getHeight()/scaling))));
                        finalY = (int)(coords[1]+(((cropBox.getHeight())-(cropBox.getHeight()/scaling))/scaling));                        

                    }else{
                        finalX = (int)(coords[0]);
                        finalY = (int)(coords[1]);                       
                    }                  
                    break;
            }

            imageId++;

            //transform for final image
            image=SVGImageUtilities.applyTransformToImage(scaling,pageRotation,useHiResImageForDisplay, image,optionsApplied,name,gs,fw, fh);
             
            /**/
            //transform for final image
            imageScaleX=((float)finalWidth)/image.getWidth();
            imageScaleY=((float)finalHeight)/image.getHeight();

            // These final values are used by all HTML, SVG etc
            currentImage = new int[]{finalX,finalY,finalWidth,finalHeight};


            /**
             * store image
             */
            final ImageFileType imageFileType = customIO.getImageTypeUsed(ImageType.SVG);
            if(embedImageAsBase64){

                imageArray = createBase64ImageStream(image, imageFileType);

            }else{

                customIO.writeImage(rootDir, fileName + "/img/" + imageId,image, ImageType.SVG);

                // We need to check inlineSVG here because the relative references to the images change
                // inline requires /pgNumber/img/etc
                // non-inline requires /img/etc because the .svg is within /pgNumber/
                if (type == CREATE_HTML && !inlineSVG) {
                    imageName = "img/" + imageId + imageFileType.getFileExtension();
                } else {
                    imageName = fileName + "/img/" +imageId + imageFileType.getFileExtension();
                }
            }
            return -2;
        }else {
            return -1;
        }
    }

    /**
     * Creates a base64 image stream
     *
     * @param image :: BufferedImage to encode as base64 stream
     *
     * @return A string of the image stream
     */
    public static String createBase64ImageStream(final BufferedImage image, final ImageFileType imageFileType){

        String stream = "";

        try {
            final ByteArrayOutputStream bos=new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image,imageFileType.getIoType(),bos);
            bos.close();
            stream ="data:image/" + imageFileType.getFileExtension().substring(1) + ";base64," + javax.xml.bind.DatatypeConverter.printBase64Binary(bos.toByteArray());
            stream = stream.replaceAll("\r\n", "");
        } catch (final IOException e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }

        return stream;
    }

    protected void drawSVGImage(final GraphicsState gs) {

        /**
         * We actually have files which set clip to 0,0,0,0 so this traps it 
         * 
         * (ie cat file)
         */
        final Area clip=gs.getClippingShape();
        if(clip!=null && (clip.getBounds2D().getWidth()==0 || clip.getBounds2D().getHeight()==0 )){
            return;
        }
        
        int toWhere = SVGBUFFER;
        if (type == CREATE_HTML) {
            toWhere = SVGINHTML;
        }

        // If a shape clip path is open we must close it
        if (!currentOpenClip.isEmpty()) {
            writeCustom(toWhere, "</g>");//Close clip
            currentOpenClip = "";
        }

        final String href;
        if (embedImageAsBase64) {
            href = imageArray;
        } else {
            href = imageName;
        }

        //add in any image transparency. Used below.
        final float fillOpacity=gs.getAlpha(GraphicsState.FILL);
        String opacity = "";
        if (fillOpacity != 1) {
            opacity = "opacity=\"" +fillOpacity + "\" ";
        }

        /**
         * image may need a scale if aspect ratio on image changed. At moment
         * lock down to specific cases as makes files larger (may need tuning)
         */
        if ((Math.abs(imageScaleX - imageScaleY) > 0.1f)){// || //allow for aspect ratio altered by transform
            // ((useHiResImageForDisplay || hasComplexRotation)
            //&& (imageScaleX < 1 && imageScaleY < 1) && pageRotation == 0 && (Math.abs(imageScaleX - imageScaleY) > 0.1f))) {

            //add in scaling for image
            final String transform = "transform=\"scale(" + round(imageScaleX) + " , " + round(imageScaleY) + ")\"";

            writeCustom(toWhere, "<image x=\"" + round(currentImage[0] / imageScaleX) + "\" y=\"" + round(currentImage[1] / imageScaleY) + "\" width=\""
                    + round(currentImage[2] / imageScaleX) + "\" height=\"" + round(currentImage[3] / imageScaleY)
                    + "\" " + opacity + "xlink:href=\"" + href + "\" " + transform + " />");
        } else {
            writeCustom(toWhere, "<image x=\"" + currentImage[0] + "\" y=\"" + currentImage[1] + "\" width=\"" + currentImage[2]
                    + "\" height=\"" + currentImage[3] + "\" " + opacity + "xlink:href=\"" + href + "\" />");
        }
    }

    /**
     * round scaling
     * (we need this level of dpi - see file:///PDFdata/MAC/hires_svgf/awjune2003/10.svg)
     */
    private static String round(final float r){

        return removeEmptyDecimals(String.valueOf(((int)(r*1000))/1000f));
    }

    static final int[] indices= {1,10,100,1000};

    /**
     * trim if needed
     * @param i Value to trim
     * @return Trimmed value to specified number of decimal places
     * * (note duplicate in OutputShape)
     */
    protected static String setPrecision(final double i, final int dpCount) {


        if(dpCount>3) {
            throw new RuntimeException("dp count must be less than 4");
        }

        final double roundedValue=(double)(((int)(i*indices[dpCount])))/indices[dpCount];

        if(roundedValue>0.98 && roundedValue<1.01){
            return "1";
        }else if(roundedValue==0){
            return "0";
        }else if(roundedValue==-0){
            return "0";
        }else if(roundedValue<-0.98 && roundedValue>-1.01){
            return "-1";
        }else{
            return String.valueOf((double)(((int)(i*indices[dpCount])))/indices[dpCount]);
        }


    }

    /*save clip in array to draw*/
    @Override
    public void drawClip(final GraphicsState currentGraphicsState, final Shape defaultClip, final boolean canBeCached) {
        //RenderUtils.renderClip(currentGraphicsState.getClippingShape(), null, defaultClip, g2);

        if(thumbnailDisplay!=null) {
            thumbnailDisplay.drawClip(currentGraphicsState, defaultClip, canBeCached);
        }

        if(imageDisplay!=null) {
            imageDisplay.drawClip(currentGraphicsState, defaultClip, canBeCached);
        }
    }

    protected void drawSVGClip(final GraphicsState gs, final Shape defaultClip) {
        clip=null;
        currentClip = "";

        Shape currentShape=gs.getClippingShape();
        if(currentShape==null && defaultClip!=null) {
            currentShape = defaultClip;
        }
        
        if(currentShape!=null){
            //if(currentShape==null)
            //  currentShape=new Rectangle(pageData.getCropBoxX(pageNumber),pageData.getCropBoxY(pageNumber),pageData.getCropBoxWidth(pageNumber),pageData.getCropBoxHeight(pageNumber));

            //flush any cached text before we write out
            //flushText();

            final String clipId = "c" + clipCount + '_' + outputPageNumber;
            final ShapeFactory shape = generateSVGShape(Cmd.n, currentShape, gs, clipId);


            if(!shape.isEmpty()) {
                clipForDiffOnly = shape.getPathCommands();
                final String cachedClipName = clipPathsWithoutID.get(clipForDiffOnly);
                if (cachedClipName != null) {
                    newClip = false;
                    currentClip = cachedClipName;
                } else {
                    newClip = true;
                    currentClip = clipId;
                }

                clip= (String) shape.getContent();
            }
        }
    }

    public boolean hasComplexText() {
        return hasComplexText;
    }

    @Override
    public void drawEmbeddedText(final float[][] Trm, final int fontSizeInt, final PdfGlyph embeddedGlyph,
                                 final Object javaGlyph, final int type, final GraphicsState gs,
                                 final double[] at, String glyf, final PdfFont currentFontData, float glyfWidth){

        //track objects so we can work out if anything behind and remove hidden text
        //(ignore rotated text for moment)
        if((Trm[2][0] > 0 && Trm[2][1] > 0) &&
                ((Trm[0][0] > 0 && Trm[0][1] == 0 && Trm[1][0] == 0 && Trm[1][1] > 0) ||
                (Trm[1][0] < 0 && Trm[0][0] == 0 && Trm[1][1] == 0 && Trm[0][1] > 0))){
            //System.out.println("TRM "+Trm[2][0]+" "+Trm[2][1]+" "+Trm[0][0]+" "+Trm[0][1]+" "+Trm[1][0]+" "+Trm[1][1]+" ");
            
            addTextShapeToAreas(Trm);
        }

        if(thumbnailDisplay != null) {
            thumbnailDisplay.drawEmbeddedText(Trm, fontSizeInt, embeddedGlyph, javaGlyph, type, gs, at, glyf, currentFontData, glyfWidth);
        }

        if(imageDisplay != null && (!isRealText || gs.getTextRenderType() == GraphicsState.CLIPTEXT)) {
            imageDisplay.drawEmbeddedText(Trm, fontSizeInt, embeddedGlyph, javaGlyph, type, gs, at, glyf, currentFontData, glyfWidth);
        }

        //System.out.println(glyf+" "+Trm[2][0]+" "+Trm[2][1]);
        //if drawn over later by shape or image, ignore
        if(gs.getTextRenderType() == GraphicsState.CLIPTEXT){
            return;
        }

        glyf = fontHelper.getGlyf(glyf, currentFontData, embeddedGlyph, isRealText);

        //if -100 we get value - allows user to override
        if(glyfWidth == -100){
            glyfWidth = currentFontData.getWidth(-1);
        }

        if (isTextOutsideBounds(Trm, glyfWidth, cropBoxWithOffset, gs.getClippingShape())) {
            flushText();
            return;
        }

        // check if we should put this text onto SVG or image layer
        final boolean rasterizeFont = fontHelper.isFontRasterized(currentFontData);

        if(rasterizeFont || (!isRealText && isSVGMode)){ //option to convert to shapes
            rasterizeTextAsShape(Trm, embeddedGlyph, gs, currentFontData); // Output the text instead as shapes

            if (rasterizeFont || !isTextSelectable) { // If text not selectable, we don't need to go any further!
                return;
            }
        }

        //Ignore empty or crappy characters
        if(glyf.isEmpty() || TextBlock.ignoreGlyf(glyf)) {
            return;
        }

        // Bug here. If page is rotated this will not function correctly
        final boolean isComplex = Trm[0][1] != 0 || Trm[1][0] != 0 || Trm[0][0] < 0 || Trm[1][1] < 0;
        if(isComplex && compatabilityDisplay != null ) {
            compatabilityDisplay.drawEmbeddedText(Trm, fontSizeInt, embeddedGlyph,javaGlyph, type, gs, at, glyf, currentFontData, glyfWidth);
            hasComplexText = true;
        }

        final int textFillType = gs.getTextRenderType();
        final int color = textFillType == GraphicsState.STROKE ? gs.getStrokeColor().getRGB() : gs.getNonstrokeColor().getRGB();

        final TextState currentTextState = gs.getTextState();
        float tfs = currentTextState.getTfs();

        final float kerning = currentTextState.getLastKerningAdded();
        final float charSpacing = currentTextState.getCharacterSpacing() / tfs;
        final float wordSpacing = currentTextState.getWordSpacing() / tfs;

        final FontMapper fontMapper = fontHelper.getFontMapper(currentFontData);

        //Append new glyf to text block if we can otherwise flush it
        if(writeEveryGlyf || currentTextBlock == null || !currentTextBlock.appendText(glyf, currentTokenNumber, kerning,
                wordSpacing, charSpacing, textFillType, color, Trm, glyfWidth, fontMapper)) {

            flushText();

            //Set up new block, if it just a space or tab then disregard it.
            if(!Character.isWhitespace(glyf.charAt(0))) {//isWhiteSpace used to pick up any space character. (Case 17213 - general-May2014/Pages from SVA-ALL-SLM_NoRestriction.pdf)
                final float spaceWidth = currentFontData.getSpaceWidthHTML();
                final boolean isSVG = this.type == CREATE_SVG;

                currentTextBlock = new TextBlock(glyf, color, spaceWidth, Trm, charSpacing, wordSpacing, textFillType, isSVG, separateToWords, convertSpacesTonbsp, glyfWidth, fontMapper, currentTokenNumber);
            }
        }
    }


    private static boolean isTextOutsideBounds(final float[][] Trm, final float glyphWidth, final Rectangle2D cropBox, final Area clip) {
        final float[][] point1 = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};//bottom left
        float[][] point2 = {{1, 0, 0}, {0, 1, 0}, {glyphWidth, 0, 1}};//bottom right
        float[][] point3 = {{1, 0, 0}, {0, 1, 0}, {0, 1, 1}};//top left
        float[][] point4 = {{1, 0, 0}, {0, 1, 0}, {glyphWidth, 1, 1}};//top right

        //If text has a rotation/skew then apply it to the points
        if (Trm[0][1] != 0 || Trm[1][0] != 0 || Trm[0][0] != Trm[1][1]) {

            //Remove translate from the Trm
            final float[][] rotation = {{Trm[0][0],Trm[0][1],0},{Trm[1][0],Trm[1][1],0},{0,0,1}};

            //Apply rotations to points (point0 is 0,0 so rotation has no effect)
            point2 = Matrix.multiply(point2, rotation);
            point3 = Matrix.multiply(point3, rotation);
            point4 = Matrix.multiply(point4, rotation);
        } else {
            point2[2][0] *= Trm[0][0];//Multiply glyphWidth by xScale from Trm
            point4[2][0] *= Trm[0][0];//Multiply glyphWidth by xScale from Trm
            point4[2][1] = Trm[1][1];//Set fontSize to yScale from Trm
        }

        //Add translate back in
        point1[2][0] += Trm[2][0];
        point1[2][1] += Trm[2][1];
        point2[2][0] += Trm[2][0];
        point2[2][1] += Trm[2][1];
        point3[2][0] += Trm[2][0];
        point3[2][1] += Trm[2][1];
        point4[2][0] += Trm[2][0];
        point4[2][1] += Trm[2][1];

        //Calculate bounding rectangle
        float minX = point1[2][0];
        float maxX = point1[2][0];
        float minY = point1[2][1];
        float maxY = point1[2][1];
        for (final float[][] point : new float[][][]{point2, point3, point4}) {
            if (minX > point[2][0]) {
                minX = point[2][0];
            }
            if (maxX < point[2][0]) {
                maxX = point[2][0];
            }
            if (minY > point[2][1]) {
                minY = point[2][1];
            }
            if (maxY < point[2][1]) {
                maxY = point[2][1];
            }
        }

        //Combine clip & crop
        double minX2 = cropBox.getMinX();
        double maxX2 = cropBox.getMaxX();
        double minY2 = cropBox.getMinY();
        double maxY2 = cropBox.getMaxY();
        if (clip != null) {
            final Rectangle2D clipBox = clip.getBounds2D();
            if (minX2 < clipBox.getMinX()) {
                minX2 = clipBox.getMinX();
            }
            if (maxX2 > clipBox.getMaxX()) {
                maxX2 = clipBox.getMaxX();
            }
            if (minY2 < clipBox.getMinY()) {
                minY2 = clipBox.getMinY();
            }
            if (maxY2 > clipBox.getMaxY()) {
                maxY2 = clipBox.getMaxY();
            }
        }

        return maxX < minX2 || minX > maxX2 || maxY < minY2 || minY > maxY2;
    }

    protected static boolean testIfVisible(final int x1, final int y1, final int width, final int height, final int objectAreaCount, final Vector_Rectangle_Int objAreas) {

        final boolean debug=false;
        
        boolean isVisible=true;

        if(objAreas!=null){

            //int x2=(int)(Trm[2][0]+Trm[0][0]-1);
            //int y2=(int)(Trm[2][1]+Trm[1][1]-1);
            final int objCount=objAreas.size()-1;
            for(int ii= objectAreaCount +1;ii<objCount;ii++){
                final Rectangle current=new Rectangle(objAreas.elementAt(ii)[0],objAreas.elementAt(ii)[1],objAreas.elementAt(ii)[2],objAreas.elementAt(ii)[3]);

                if(current==null || current.getBounds().height<height || current.getBounds().width<width || (height==0 && current.getBounds().width==width)) {
                    continue;
                }

                if(current.contains(x1,y1)){
                    
                    if(debug){
                        System.out.println("remove "+x1+ ' ' +y1+ ' ' +width+ ' ' +height+" ii="+ii+ ' ' +current.getBounds());
                    }
                    
                    isVisible=false;
                    ii=objCount;
                }
            }
        }
        return isVisible;
    }

    private void rasterizeTextAsShape(float[][] Trm, final PdfGlyph embeddedGlyph, final GraphicsState gs, final PdfFont currentFontData) {

        //if(currentFontData.getBaseFontName().contains("FFAAHC+MSTT31ca9d00")){
        //    System.out.println(fontSize+" "+glyf+" "+currentFontData.getBaseFontName()+" "+currentFontData);
        //}

        /**
         * convert text to shape and draw shape instead
         * Generally text at 1000x1000 matrix so we scale down by 1000 and then up by fontsize
         */
        if (embeddedGlyph != null && embeddedGlyph.getShape() != null && !embeddedGlyph.getShape().isEmpty()) {

            // fix for the occurance of . in fontIDs
            String safeFontID = currentFontData.getFontID();
            safeFontID = StringUtils.makeMethodSafe(safeFontID);
            if(usedFontIDs.containsKey(safeFontID) && !usedFontIDs.get(safeFontID).equals(currentFontData.getBaseFontName())) {
                // add extra fontID stuff
                safeFontID += StringUtils.makeMethodSafe(currentFontData.getBaseFontName());
            }
            else {
                usedFontIDs.put(safeFontID, currentFontData.getBaseFontName());
            }

            //name we will store draw code under as routine
            String jsRoutineName = safeFontID + '_' + embeddedGlyph.getGlyphNumber();

            //            System.out.println(currentFontData.getBaseFontName() + " " + jsRoutineName + "  fontID= " + safeFontID);

            //ensure starts with letter
            if(!Character.isLetter(jsRoutineName.charAt(0))) {
                jsRoutineName = 's' + jsRoutineName;
            }

            //flag to show if generated
            final String cacheKey= currentFontData.getBaseFontName() + '.' + jsRoutineName;

            //see if we have already decoded glyph and use that data to reduce filesize
            final boolean isAlreadyDecoded=glyfsRasterized.contains(cacheKey);

            //get the glyph as textGlyf shape (which we already have to render)
            final Area textGlyf = (Area) embeddedGlyph.getShape().clone();

            //useful to debug
            //textGlyf=new Area(new Rectangle(0,0,1000,1000));

            //adjust GS to work correctly
            gs.setClippingShape(null);
            gs.setFillType(gs.getTextRenderType());

            /**
             *  adjust scaling to factor in font size
             */
            float d= (float) (1f/currentFontData.FontMatrix[0]);

            //allow for rescaled TT which work on a much larger grid
            if(textGlyf.getBounds().height>2000){
                d *= 100;
            }

            writeRasterizedTextPosition(jsRoutineName, Trm, d);

            completeTextShape(gs, jsRoutineName);

            //generate the (svg) shape ONCE for each glyf
            if(!isAlreadyDecoded){
                drawNonPatternedShape(textGlyf, gs, Cmd.Tj, jsRoutineName);
                glyfsRasterized.add(cacheKey); //flag it as now in file
            }
        }
    }

    protected abstract void completeTextShape(final GraphicsState gs, final String jsRoutineName);

    protected void completeSVGTextShape(final GraphicsState gs) {
        int toWhere = SVGBUFFER;
        if (type == CREATE_HTML) {
            toWhere = SVGINHTML;
        }

        final int fillType = gs.getFillType();

        if(fillType==GraphicsState.FILL || fillType==GraphicsState.FILLSTROKE) {
            final PdfPaint col = gs.getNonstrokeColor();
            writeCustom(toWhere, "fill=\""+convertRGBtoHex(col.getRGB())+"\" ");
        }
        else {
            writeCustom(toWhere, "fill=\"none\" ");
        }

        if(fillType==GraphicsState.STROKE) {
            final BasicStroke stroke = (BasicStroke) gs.getStroke();

            final double finalLineWidth = stroke.getLineWidth()*scaling;

            //if line is very thin, it will not appear as black but murky gray
            if(finalLineWidth < 0.1 && gs.getStrokeColor().getRGB() == -16777216) { //attribute double lineWidth; (default 1)
                writeCustom(toWhere,"stroke-width=\""+1+"\" ");
            }else{
                writeCustom(toWhere,"stroke-width=\""+finalLineWidth+"\" ");
            }

            if(stroke.getMiterLimit()!=10) {  //attribute double miterLimit; // (default 10)
                writeCustom(toWhere,"stroke-miterlimit=\"" + ((double) stroke.getMiterLimit()) + "\" ");
            }

            final PdfPaint col = gs.getStrokeColor();
            writeCustom(toWhere,"stroke=\""+convertRGBtoHex(col.getRGB())+ '"');
            //allow for any opacity
            final float strokeOpacity=gs.getAlpha(GraphicsState.STROKE);
            if(strokeOpacity!=1){
                writeCustom(toWhere,"stroke-opacity=\"" +strokeOpacity + '\"');
            }
        }
        else {
            writeCustom(toWhere, "stroke=\"none\" ");
        }

        writeCustom(toWhere, " />");
    }


    /*update counter so we are in sync with our array and can eliminate text hidden by a rectangle*/
    @Override
    public void eliminateHiddenText(final Shape currentShape, final GraphicsState gs, final int count, boolean ignoreScaling) {

        if(ignoreScaling || useShapeToHideText(gs.CTM, count)){

            int x=currentShape.getBounds().x;
            int y=currentShape.getBounds().y;
            int x2=x+currentShape.getBounds().width;
            int y2=y+currentShape.getBounds().height;

            //factor in clip
            final Shape clip=gs.getClippingShape();
            if(clip!=null){
                final int clipX=clip.getBounds().x;
                final int clipY=clip.getBounds().y;
                final int clipX2=clipX+clip.getBounds().width;
                final int clipY2=clipY+clip.getBounds().height;

                if(clipX>x) {
                    x = clipX;
                }

                if(clipY>y) {
                    y = clipY;
                }

                if(clipX2<x2) {
                    x2 = clipX;
                }

                if(clipY2<y) {
                    y2 = clipY2;
                }
            }

            //now work out values
            final int w=x2-x;
            final int h=y2-y;


            /**
             * factor in if reversed on x or y
             */
            if(!ignoreScaling){
                if(gs.CTM[0][0]<0) {
                    x -= w;
                }

                if(gs.CTM[1][1]<0) {
                    y -= h;
                }
            }


            //System.out.println("\n====================================");

            float[][] rect;
            //factor in scaling

            if(gs.scaleFactor!=null){

                rect=new float[][]{{w,0,0},{0,h,0},{x ,y,1}};

                rect[2][0]=x-gs.CTM[2][0];
                rect[2][1]=y-gs.CTM[2][1];
                //Matrix.show(rect);
                //System.out.println("");

                //Matrix.show(gs.CTM);
                rect=Matrix.multiply(rect,gs.scaleFactor);
                }else if(ignoreScaling){
                //factor in scaling
                rect=new float[][]{{w,0,0},{0,h,0},{x ,y,1}};

            }else{
                //factor in scaling
                rect=new float[][]{{w,0,0},{0,h,0},{x-gs.CTM[2][0] ,y-gs.CTM[2][1],1}};

                //Matrix.show(rect);
                //System.out.println("");

                //Matrix.show(gs.CTM);
                rect=Matrix.multiply(rect,gs.CTM);

            }

            //System.out.println("");

            //Matrix.show(rect);

            //System.out.println("");

            imageAndShapeAreas.addElement(new int[]{(int) rect[2][0],(int)rect[2][1],(int)rect[0][0],(int)rect[1][1]});
            //System.out.println(rect[2][0]+" "+rect[2][1]+" "+rect[0][0]+" "+rect[1][1]);

            objectAreaCount++;

            //     System.out.println("COunt "+objectAreaCount);
        }
    }

     /*save shape in array to draw*/

    @Override
    public void drawShape(Shape currentShape, final GraphicsState gs, final int cmd) {

        if(thumbnailDisplay!=null) {
            thumbnailDisplay.drawShape(currentShape, gs, cmd);
        }

        if(imageDisplay!=null) {
            imageDisplay.drawShape(currentShape, gs, cmd);
        }


        if(!isSVGMode || !isObjectVisible(currentShape.getBounds(),gs.getClippingShape())) {
            return;
        }

        //flush any cached text before we write out
        flushText();

        //<start-demo><end-demo>

        final int fillType = gs.getFillType();
        //turn pattern into an image
        if((((fillType == GraphicsState.FILLSTROKE || fillType == GraphicsState.STROKE) && (gs.getStrokeColor().isPattern() || gs.strokeColorSpace.getID() == ColorSpaces.Pattern)) ||
                ((fillType == GraphicsState.FILLSTROKE || fillType == GraphicsState.FILL) && (gs.getNonstrokeColor().isPattern() || gs.nonstrokeColorSpace.getID() == ColorSpaces.Pattern)))) {  //complex stuff

            drawPatternedShape(currentShape, gs);

        }else {  //standard shapes

            /** Start of missing line/pixel perfect fix **/
            if (gs.getFillType() == GraphicsState.FILL && !shapeContainsCurve(currentShape)) {
                final double x=currentShape.getBounds2D().getX();
                final double y=currentShape.getBounds2D().getY();
                final double width=currentShape.getBounds2D().getWidth();
                final double height=currentShape.getBounds2D().getHeight();
                final float lineWidth=gs.getCTMAdjustedLineWidth();

                if(height<=1 && lineWidth <= 1) {
                    gs.setFillType(GraphicsState.STROKE);
                    gs.setStrokeColor(gs.getNonstrokeColor());
                    gs.setCTMAdjustedLineWidth(0.1f);
                    currentShape=new Line2D.Double(x,y,x+width,y);
                }

                if(width<=1 && lineWidth <= 1) {
                    gs.setFillType(GraphicsState.STROKE);
                    gs.setStrokeColor(gs.getNonstrokeColor());
                    gs.setCTMAdjustedLineWidth(0.1f);
                    currentShape=new Line2D.Double(x,y,x,y+height);
                }
            }
            /** End of missing line/pixel perfect fix **/

            drawNonPatternedShape(currentShape, gs, cmd,null);

        }
    }

    protected abstract void drawNonPatternedShape(final Shape currentShape, final GraphicsState gs, final int cmd, final String name);

    private static boolean shapeContainsCurve(final Shape shape) {
        final PathIterator it = shape.getPathIterator(null);

        while(!it.isDone()) {
            final double[] coords = {0,0,0,0,0,0};
            final int pathCommand = it.currentSegment(coords);

            if (pathCommand == PathIterator.SEG_CUBICTO) {
                return true;
            }
            it.next();
        }

        return false;
    }

    /**
     * Checks if the given shape is a rectangle. Consider this to be a cheap and nasty check (there will be some false
     * negatives, but very few false positives).
     * @param shape The shape to check
     * @return Whether the shape is considered a rectangle
     */
    private static boolean isShapeRectangle(final Shape shape) {
        final PathIterator it = shape.getPathIterator(null);
        double lastX = 0, lastY = 0, segCount = 0;

        while(!it.isDone()) {
            final double[] coords = {0,0,0,0,0,0};
            final int pathCommand = it.currentSegment(coords);

            if (segCount > 3) {
                return false;
            } else if (pathCommand == PathIterator.SEG_CUBICTO) {
                return false;
            } else if (pathCommand == PathIterator.SEG_LINETO) {
                if (coords[0] != lastX && coords[1] != lastY) {
                    return false;
                }
                lastX = coords[0];
                lastY = coords[1];
                segCount++;
            } else if (pathCommand == PathIterator.SEG_MOVETO) {
                lastX = coords[0];
                lastY = coords[1];
            }
            it.next();
        }

        return true;
    }

    /**
     * Checks if the shape exists entirely within the clip.
     * Can only return true when the clip is deemed to be a rectangle.
     * @param shape Shape to check
     * @param clip Clip to check
     * @return Whether the shape exists entirely within the clip
     */
    private static boolean isShapeInsideClip(final Shape shape, final Shape clip) {
        // This check would be very slow if we didn't limit clip shapes to just rectangles only.
        if (!isShapeRectangle(clip)) {
            return false;//See general/forum/1.svg for why this is important.
        }

        final Rectangle2D shapeBounds  = shape.getBounds2D();
        final double shapeStartX = shapeBounds.getX();
        final double shapeStartY = shapeBounds.getY();
        final double shapeEndX = shapeBounds.getWidth() + shapeStartX;
        final double shapeEndY = shapeBounds.getHeight() + shapeStartY;

        final Rectangle2D clipBounds  = clip.getBounds2D();
        final double clipStartX = clipBounds.getX();
        final double clipStartY = clipBounds.getY();
        final double clipEndX = clipBounds.getWidth() + clipStartX;
        final double clipEndY = clipBounds.getHeight() + clipStartY;

        return shapeStartX >= clipStartX && shapeStartY >= clipStartY && shapeEndX <= clipEndX && shapeEndY <= clipEndY;
    }

    protected void drawSVGNonPatternedShape(final Shape currentShape, final GraphicsState gs, final int cmd, final String name) {

        final ShapeFactory shape = generateSVGShape(cmd, currentShape, gs, currentClip);

        if(!shape.isEmpty()) {

            int toWhere = SVGBUFFER;
            if (type == CREATE_HTML) {
                toWhere = SVGINHTML;
            }

            final String tempClip = clip;
            final String tempCurrentClip = currentClip;
            final Shape clipShape = gs.getClippingShape();//Something wrong here? 'clipShape' can be null when 'clip' has value
            final boolean isShapeInsideClip = clip != null && clipShape != null && isShapeInsideClip(currentShape, clipShape);
            if (isShapeInsideClip) {
                /* The general idea here is that if the shape exists entirely within the clip shape then we don't need
                 * to apply the clip. This provides a slight reduction in file size & improvement in performance because
                 * there are fewer clips to apply. It also fixes some issues where shapes are clipped harshly because of
                 * the difference in scan conversion (see PDF spec). For example 14nov/18911 clip alignment.pdf
                 * 14nov/17572 letter spacing/1, 14nov/17264 lines missing/1
                 *
                 * In practice, the bounds checking does not account for shapes having a line width, which means that
                 * this could break files that have shapes with large line widths that should be clipped away that no
                 * longer will be. I could not find any file in the baseline which showed a significant negative effect.
                 *
                 * Any questions, ask Leon. */

                clip = null;
                currentClip = "";
            }

            if(cmd==Cmd.Tj){ //put in own subroutine

                writeCustom(FONT_AS_SHAPE, shape.getContent());
                writeCustom(FONT_AS_SHAPE, "id=\"" + name + '"');
                writeCustom(FONT_AS_SHAPE, " />\n");

            }else if(clip!=null && newClip){
                newClip = false;//Only output the clip definition once
                clipCount++;//Only increment the clip count if the clip is actually used.
                clipPathsWithoutID.put(clipForDiffOnly, currentClip);//Cache clip
                writeCustom(SVGCLIPS, clip);
                //Close current clip path if open
                if (!currentOpenClip.isEmpty()) {
                    writeCustom(toWhere, "</g>");//Close clip
                    currentOpenClip = "";
                }
                //Open our new clip path
                if (currentOpenClip.isEmpty()) {
                    writeCustom(toWhere, "<g clip-path=\"url(#" + currentClip + ")\">");//Open clip
                    currentOpenClip = currentClip;
                }
                writeCustom(toWhere, shape.getContent());
            }else{
                //Sometimes the current clip changes back to a previous clip
                if (!currentOpenClip.isEmpty() && !currentOpenClip.equals(currentClip)) {
                    writeCustom(toWhere, "</g>");//Close clip
                    currentOpenClip = "";
                }
                if (currentOpenClip.isEmpty() && !currentClip.isEmpty()) {
                    writeCustom(toWhere, "<g clip-path=\"url(#" + currentClip + ")\">");//Open clip
                    currentOpenClip = currentClip;
                }

                writeCustom(toWhere, shape.getContent());
            }

            if (isShapeInsideClip) {
                clip = tempClip;
                currentClip = tempCurrentClip;
            }

        }
    }

    public abstract ShapeFactory generateSVGShape(final int cmd, final Shape currentShape, final GraphicsState gs, final String clipId);

    protected void drawPatternedShape(final Shape currentShape, final GraphicsState gs) {
        // This is how I have interpreted what the PDF spec says about shapes. Leon

        final double iw = currentShape.getBounds2D().getWidth() + gs.getLineWidth();
        final double ih = currentShape.getBounds2D().getHeight() + gs.getLineWidth();
        final double ix = currentShape.getBounds2D().getX() - gs.getLineWidth() / 2;
        final double iy = currentShape.getBounds2D().getY() - gs.getLineWidth() / 2;

        final double[] coords = new double[]{ix,iy};
        correctCoords(coords);

        int ixScaled = (int)(coords[0] * scaling);
        // The x plus width is the unrounded x position plus the width, then everything rounded up.
        int iwScaled = (int)((((coords[0] + iw) - (int)coords[0]) * scaling) + 1);
        int iyScaled = (int)((coords[1] - ih) * scaling);
        int ihScaled = (int)((((coords[1] + ih) - (int)coords[1]) * scaling) + 1);

        if (iwScaled < 1 || ihScaled <1) {
            return; // Invisible shading - THIS SHOULD BE IMPOSSIBLE GIVEN THE ABOVE CALCULATIONS.
        }

        BufferedImage img = new BufferedImage(iwScaled, ihScaled, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2=img.createGraphics();
        final AffineTransform aff=new AffineTransform();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        aff.scale(scaling,scaling);
        aff.scale(1,-1);
        aff.translate(0, -ih);
        aff.translate(-ix, -iy);
        g2.setTransform(aff);
        final int fillType = gs.getFillType();

        final int pageH=pageData.getCropBoxHeight(rawPageNumber);
        final int pageY=0;

        if (fillType == GraphicsState.FILL || fillType == GraphicsState.FILLSTROKE) {
            final PdfPaint col = gs.getNonstrokeColor();
            col.setScaling(0,pageH,scaling,0,pageY);
            col.setRenderingType(DynamicVectorRenderer.CREATE_HTML);
            g2.setPaint(col);
            g2.fill(currentShape);
        }

        if (fillType == GraphicsState.FILLSTROKE) {
            g2.draw(currentShape);
        }

        if (fillType == GraphicsState.STROKE || fillType == GraphicsState.FILLSTROKE) {
            final PdfPaint col = gs.getStrokeColor();
            col.setScaling(0,pageH,scaling,0,pageY);
            col.setRenderingType(DynamicVectorRenderer.CREATE_HTML);
            g2.setPaint(col);
            g2.setStroke(gs.getStroke());
            g2.draw(currentShape);
        }

        final Rectangle2D shading = new Rectangle(ixScaled, iyScaled, iwScaled, ihScaled);
        final Rectangle2D cropBoxScaled = new Rectangle.Double(cropBox.getX()*scaling, cropBox.getY()*scaling,
                cropBox.getWidth()*scaling, cropBox.getHeight()*scaling);

        // Check shade will be drawn on page.
        if (cropBoxScaled.intersects(shading)) {
            shadeId++;

             /* Crop image if it goes off page.
             boolean crop = false;
             int xxx = 0;
             int yyy = 0;
             if (ixScaled<0) { // Image goes off left
                 xxx = ixScaled * -1;
                 iwScaled = iwScaled+ixScaled;
                 ixScaled = 0;
                 crop = true;
             }
             if (ixScaled+iwScaled > cropBoxScaled.getWidth()) { // Image goes off right
                 iwScaled = round(cropBoxScaled.getWidth() - ixScaledUnrounded, 0.5);
                 crop = true;
             }
             if (ihScaled<0) { // Image goes off top
                 yyy = iyScaled * -1;
                 ihScaled = ihScaled+iyScaled;
                 iyScaled = 0;
                 crop = true;
             }
             if (iyScaled+ihScaled > cropBoxScaled.getHeight()) { // Image goes off bottom
                 ihScaled = round(cropBoxScaled.getHeight() - iyScaledUnrounded, 0.5);
                 crop = true;
             }
             if (crop) {
                 if (xxx + iwScaled > img.getWidth()) {
                     iwScaled = img.getWidth() - xxx;
                 }
                 if (yyy + ihScaled > img.getHeight()) {
                     ihScaled = img.getHeight() - yyy;
                 }
                 img = img.getSubimage(xxx, yyy, iwScaled, ihScaled);
             }
             /**/

            if (pageRotation == 90 || pageRotation == 270) {

                iyScaled = (int) (cropBoxScaled.getHeight() - iyScaled - ihScaled);
                // This needs to be calculated way up there. These X,Y,W,H values were calculated based on the rotation at 0.

                int tmp = iwScaled;
                iwScaled=ihScaled;
                ihScaled=tmp;

                tmp=ixScaled;
                ixScaled=iyScaled;
                iyScaled=tmp;

                img = ImageUtils.rotateImage(img,pageRotation);
            }

            final ImageFileType imageFileType = customIO.getImageTypeUsed(ImageType.SHADE);
            if(embedImageAsBase64){

                imageArray = createBase64ImageStream(img, imageFileType);

            }else{
                // Store image.
                customIO.writeImage(rootDir, fileName + "/shade/" + shadeId, img, ImageType.SHADE);

                if (type == CREATE_HTML && !inlineSVG) {
                    currentPatternedShapeName = "shade/" + shadeId + imageFileType.getFileExtension();
                } else {
                    currentPatternedShapeName = fileName + "/shade/" + shadeId + imageFileType.getFileExtension();
                }
            }

            // These final values are used by all HTML, SVG etc
            currentPatternedShape = new int[]{ixScaled, iyScaled, iwScaled, ihScaled};
        } else {
            currentPatternedShape = new int[]{-1,-1,-1,-1};
        }
    }

    protected void drawSVGPatternedShape() {
        int toWhere = SVGBUFFER;
        if (type == CREATE_HTML) {
            toWhere = SVGINHTML;
        }

        // If a shape clip path is open we must close it
        if (!currentOpenClip.isEmpty()) {
            writeCustom(toWhere, "</g>");//Close clip
            currentOpenClip = "";
        }

        final String href;
        if (embedImageAsBase64) {
            href = imageArray;
        } else {
            href = currentPatternedShapeName;
        }

        writeCustom(toWhere, "<image x=\""+currentPatternedShape[0]+"\" y=\""+currentPatternedShape[1]+"\" width=\""+currentPatternedShape[2]+"\" height=\""+currentPatternedShape[3]+"\" xlink:href=\""+href+"\" />");
    }

    private boolean isObjectVisible(final Rectangle bounds, final Area clip) {

        //get any Clip (only rectangle outline)
        final Rectangle clipBox;
        if(clip!=null) {
            clipBox = clip.getBounds();
        } else {
            clipBox = null;
        }

        // If shape is outside of the clip or crop boxes, shape is not visible.
        // I have chosen to do a manual check rather than use .intersects().
        final int boundsStartX = bounds.x;
        final int boundsStartY = bounds.y;
        final int boundsEndX = bounds.width + boundsStartX;
        final int boundsEndY = bounds.height + boundsStartY;
        if (cropBoxWithOffset != null) {
            final double cropStartX = cropBoxWithOffset.getBounds2D().getX();
            final double cropStartY = cropBoxWithOffset.getBounds2D().getY();
            final double cropEndX = cropBoxWithOffset.getBounds2D().getWidth() + cropStartX;
            final double cropEndY = cropBoxWithOffset.getBounds2D().getHeight() + cropStartY;

            // Note: changing to <= and >= causes an issue on awjune pg2 because the line width makes the shape visible
            if (boundsEndX < cropStartX || boundsStartX > cropEndX || boundsEndY < cropStartY || boundsStartY > cropEndY) {
                //System.out.println("CROP: " + boundsEndX + " < " + cropStartX + " || " + boundsStartX + " > " + cropEndX + " || " + boundsEndY + " < " + cropStartX + " || " + boundsStartY + " > " + cropEndY);
                return false;
            }
        }
        if (clipBox != null) {
            final int clipStartX = clipBox.x;
            final int clipStartY = clipBox.y;
            final int clipEndX = clipBox.width + clipStartX;
            final int clipEndY = clipBox.height + clipStartY;

            if (boundsEndX < clipStartX || boundsStartX > clipEndX || boundsEndY < clipStartY || boundsStartY > clipEndY) {
                //System.out.println("CLIP: " + boundsEndX + " < " + clipStartX + " || " + boundsStartX + " > " + clipEndX + " || " + boundsEndY + " < " + clipStartX + " || " + boundsStartY + " > " + clipEndY);
                return false;
            }
        }

        return true;
    }

    /**
     * add footer and other material to complete
     */
    protected abstract void completeOutput();

    /**
     * Converts coords from Pdf system to java.
     */
    protected void correctCoords(final double[] coords) {
        coords[0] -= midPoint.getX();
        coords[0] += cropBox.getWidth() / 2;

        coords[1] -= midPoint.getY();
        coords[1] = 0 - coords[1];
        coords[1] += cropBox.getHeight() / 2;
    }

    /**
     * Returns r g b values as ints
     */
    protected static int[] rgbToIntArray(final int raw){
        final int r = (raw>>16) & 255;
        final int g = (raw>>8) & 255;
        final int b = raw & 255;

        return new int[] {r, g, b, 255};
    }

    /**
     * allow tracking of specific commands
     **/
    @Override
    public void flagCommand(final int commandID, final int tokenNumber){

        switch(commandID){
            case Cmd.Tj:
                this.currentTokenNumber=tokenNumber;
                break;
        }
    }

    @Override
    public boolean isScalingControlledByUser(){
        return userControlledImageScaling;
    }

    /**
     * Write out text buffer in correct format
     */
    protected void flushText() {

        if(currentTextBlock==null || currentTextBlock.isEmpty()) {
            return;
        }

        writeOutTextBlock(currentTextBlock);

        currentTextBlock = null;
    }


    protected abstract void writeOutTextBlock(final TextBlock textBlock);

    protected float[][] getOutputTextTrm(float[][] Trm, final boolean isShape) {

        /**
         * VERY IMPORTANT EXPLANATION
         *
         * In PDF, page rotations are clockwise. Transform rotations however are anti-clockwise!
         * Therefore, for a 90 degree page rotation, we must apply a 270 transform, and vice versa.
         *
         * In PDF, the origin of skews is the bottom left. In CSS, it is at the top left!
         * Also, the rotation direction of skews are inverted!
         * Therefore, [0][1] & [1][0] must be inverted, and the x/y position adjusted with the skew.
         */

        final double[] coords = { Trm[2][0], Trm[2][1] };
        correctCoords(coords);
        double tx,ty;
        float[][] rotatedTrm;

        switch(pageRotation){
            default:
                tx = coords[0];
                ty = coords[1];
                rotatedTrm = new float[][]{{1,0,0},{0,1,0},{0,0,1}};//Identity matrix
                break;

            case 90:
                tx = cropBox.getHeight() - coords[1];
                ty = coords[0];
                rotatedTrm = new float[][]{{0,-1,0},{1,0,0},{0,0,1}};//270 rotation
                break;

            case 180:
                tx = cropBox.getWidth() - coords[0];
                ty = cropBox.getHeight() - coords[1];
                rotatedTrm = new float[][]{{-1,0,0},{0,-1,0},{0,0,1}};//180 rotation
                break;

            case 270:
                tx = coords[1];
                ty = cropBox.getWidth() - coords[0];
                rotatedTrm = new float[][]{{0,1,0},{-1,0,0},{0,0,1}};//90 rotation
                break;
        }

        // Apply page rotation to Trm
        rotatedTrm = Matrix.multiply(Trm, rotatedTrm);

        if (isShape) {
            rotatedTrm = Matrix.multiply(rotatedTrm, new float[][]{{1,0,0},{0,-1,0},{0,0,1}});//vertical flip
        }

        if(!isShape && type != CREATE_SVG){
            tx += rotatedTrm[1][0];// Subtract skew because origin bottom left in PDF and top left in HTML/CSS
            ty -= rotatedTrm[1][1];// Subtract font height because origin bottom left in PDF and top left in HTML/CSS
        }

        // Apply page scaling to positions
        tx *= scaling;
        ty *= scaling;

        // As explained above, skews must be inverted due to reversed rotation behavior
        if (!isShape) {
            rotatedTrm[0][1] = -rotatedTrm[0][1];
            rotatedTrm[1][0] = -rotatedTrm[1][0];
        }

        // Remove any negative 0's
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                if (rotatedTrm[i][j] == -0.0) {
                    rotatedTrm[i][j] = 0.0f;
                }
            }
        }

        rotatedTrm[2][0] = (float) tx;
        rotatedTrm[2][1] = (float) ty;

        return rotatedTrm;
    }


    protected abstract void writeTextPosition(final TextBlock textBlock);

    protected abstract void writeRasterizedTextPosition(final String JSRoutineName, final float[][] Trm, final float fontScaling);

    protected void writeSVGRasterizedTextPosition(final String JSRoutineName, float[][] Trm, final float fontScaling) {
        //place scaled shape of text on page

        /**
         * adjust placing
         */
        Trm = getOutputTextTrm(Trm, true);

        final int tx = (int) Trm[2][0];
        final int ty = (int) Trm[2][1];

        int toWhere = SVGBUFFER;
        if (type == CREATE_HTML) {
            toWhere = SVGINHTML;
        }

        // If a shape clip path is open we must close it
        if (!currentOpenClip.isEmpty()) {
            writeCustom(toWhere, "</g>");//Close clip
            currentOpenClip = "";
        }

        writeCustom(toWhere, "<use ");
        writeCustom(toWhere, "xlink:href =\"#" + JSRoutineName +"\" ");

        if(Trm[0][0]>0 && Trm[1][1]>0 && Trm[0][1]==0 && Trm[1][0]==0){ //simple case (left to right text)

            writeCustom(toWhere, " transform=\"translate(" + tx + ',' + ty + ") scale(" + tidy(Trm[0][0]/fontScaling) + ',' + tidy(Trm[1][1]/fontScaling) + ")\"");

        }else{ //full monty! Takes more space so not used unless needed

            writeCustom(toWhere, " transform=\"matrix(" + tidy(Trm[0][0]/fontScaling)+" , "+tidy(Trm[0][1]/fontScaling)+", "+tidy(Trm[1][0]/fontScaling)+" , "+tidy(Trm[1][1]/fontScaling)+", "+tx+" , "+ty + ")\"");
        }
    }

    protected static String tidy(final float val) {
        return removeEmptyDecimals(String.valueOf(val));
    }

    private static String removeEmptyDecimals(String numberValue) {
        //remove any .0000)
        final int ptr=numberValue.indexOf('.');
        if(ptr>-1){
            boolean onlyZeros=true;
            final int len=numberValue.length();
            for(int ii=ptr+1;ii<len;ii++){ //test if . followed by just zeros
                if(numberValue.charAt(ii)!='0'){
                    onlyZeros=false;
                    ii=len;
                }
            }

            //if so remove
            if(onlyZeros){
                numberValue=numberValue.substring(0,ptr);
            }
        }

        return numberValue;
    }

    @Override
    public void saveAdvanceWidth(String fontName, final String glyphName, final int width) {
        fontHelper.saveAdvanceWidth(fontName, glyphName, width);
    }

    /**
     * Work out page number to use in the Href tag
     * @param pageNumber Page number to go to.
     * @return Page number as string appends initial "0" if needed
     */
    public String getPageAsHTMLRef(int pageNumber) {
        //validate
        if(pageNumber<1) {
            pageNumber = 1;
        }
        if(pageNumber>endPage) {
            pageNumber = endPage;
        }

        //convert to string
        String pageAsString = String.valueOf(pageNumber);

        //include option to call page one something else
        if(pageNumber==1){
            pageAsString= "index";
        }else{
            //add required zeros
            final String maxNumberOfPages = String.valueOf(endPage);
            final int padding = maxNumberOfPages.length() - pageAsString.length();
            for (int ii = 0; ii < padding; ii++) {
                pageAsString = '0' + pageAsString;
            }
        }

        return pageAsString;
    }

    @Override
    public boolean avoidDownSamplingImage() {
        return keepOriginalImage;
    }

    protected static String convertRGBtoHex(final int color){
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public int getEndPage() {
        return endPage;
    }

    /**
     * page scaling used by HTML code only
     * @return page scaling
     */
    @Override
    public float getScaling() {
        return scaling;
    }

    protected static String getTitle(final String rootDir, final PdfFileInformation pdfFileInformation) {
        String title = new File(rootDir).getName();

        if (pdfFileInformation != null) {
            final String[] values = pdfFileInformation.getFieldValues();
            if (!values[0].isEmpty()) {
                title = values[0];
            }
        }

        return title;
    }

    protected void writeOutPropertiesJSON() {
        final JSONStringer properties = new JSONStringer();
        properties.object();
        properties.key("pagecount").value(pageCount);

        if (pdfFileInformation != null) {
            final String[] values = pdfFileInformation.getFieldValues();
            final String[] fields = PdfFileInformation.getFieldNames();
            for (int i = 0; i < 2; i++) {//title and author only
                properties.key(fields[i].toLowerCase()).value(values[i]);
            }
        }

        properties.key("bounds");
        properties.array();

        for (int i = 1; i <= pageCount; i++) {
            final int tmpRotation = pageData.getRotation(i);
            properties.array();
            properties.value((int)(scaling * (tmpRotation == 0 || tmpRotation == 180 ? pageData.getCropBoxWidth2D(i) : pageData.getCropBoxHeight2D(i))));
            properties.value((int)(scaling * (tmpRotation == 0 || tmpRotation == 180 ? pageData.getCropBoxHeight2D(i) : pageData.getCropBoxWidth2D(i))));
            properties.endArray();
        }

        properties.endArray();

        properties.key("bookmarks");
        if (Bookmarks.hasBookmarks(bookmarks)) {
            Bookmarks.extractBookmarksAsJSON(properties, bookmarks);
        } else {
            properties.array().endArray();
        }

        properties.endObject();

        customIO.writePlainTextFile(rootDir + "/properties.json", new StringBuilder(properties.toString()));
    }

    /**
     * If using a page range, sometimes the output page is not the same as the raw page.
     * This method will convert from rawPage to outputPage number based on the current PageRange.
     * @param rawPage raw page number
     * @return output page number, or -1 if rawPage does not exist in current range
     */
    public int convertRawPageToOutputPageNumber(final int rawPage) {
        if (fullNumbering) {
            return rawPage;
        } else {
            for (int i = 0; i < pageCount; i++) {
                if (pageNums[i] == rawPage) {
                    return i + 1;
                }
            }
            return -1;
        }
    }

    /**
     * Returns whether or not the converter is set in Internet Explorer 8 compatibility mode
     * To enable, use -Dorg.jpedal.pdf2html.IECompatibilityMode=true
     *
     * @return true if set to using IE Compatibility mode
     */
    public boolean isIECompatibilityMode() {
        return ieCompatibilityMode;
    }

    protected void writeOutAssets() {
        //PageTurning only images
        if (viewMode == VIEW_PAGETURNING) {
            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/icons/nav.png"),rootDir + "assets/nav.png");

            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/icons/pgThumbs.png"),rootDir + "assets/pgThumbs.png");
            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/icons/pgNext.png"),rootDir + "assets/pgNext.png");
            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/icons/pgPrev.png"),rootDir + "assets/pgPrev.png");
            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/icons/pgZoomIn.png"),rootDir + "assets/pgZoomIn.png");
            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/icons/pgZoomOut.png"),rootDir + "assets/pgZoomOut.png");

            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/icons/pgShare.png"),rootDir + "assets/pgShare.png");
            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/icons/pgBegin.png"),rootDir + "assets/pgBegin.png");
            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/icons/pgLast.png"),rootDir + "assets/pgLast.png");
        }

        //IDRViewer only images
        if (viewMode == VIEW_IDR) {
            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/icons/assets.png"),rootDir + "assets/assets.png");

            //Loading gif used as placeholder for unloaded thumbnails in sidebar in IDRViewer
            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/icons/loading.gif"), rootDir + "assets/loading.gif");

            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/icons/bg.png"),rootDir + "assets/bg.png");

            //Write out IDRViewer.js & IDRViewer.css
//                customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/IDRViewer.js"), rootDir + "/assets/IDRViewer.js");
            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/IDRViewer-min.js"), rootDir + "/assets/IDRViewer.js");
            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/IDRViewer.css"), rootDir + "/assets/IDRViewer.css");

            customIO.writeFileFromStream(getClass().getResourceAsStream("/org/jpedal/examples/html/jquery-1.11.2.min.js"), rootDir + "/assets/jquery-1.11.2.min.js");
        }
    }

    protected void writeOutIDRViewerIndexFile(final boolean isHTML) {
        writeOutAssets();

        idrViewer.append("<!DOCTYPE html >\n");
        idrViewer.append("<html lang=\"en\">\n");
        idrViewer.append("<head>\n");
        idrViewer.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\" />\n");
        idrViewer.append("<meta charset=\"").append(encodingType[OUTPUT_TYPE]).append("\" />\n");

        idrViewer.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />\n");

        idrViewer.append("<title>").append(getTitle(rootDir, pdfFileInformation)).append("</title>\n");

        if (enableAnalytics) {
            idrViewer.append("<script type=\"text/javascript\">\n" +
                    "  var _gaq = _gaq || [];\n" +
                    "  _gaq.push(['_setAccount', '" + googleAnalyticsID + "']);\n" +
                    "  _gaq.push(['_trackPageview']);\n" +
                    '\n' +
                    "  (function() {\n" +
                    "    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;\n" +
                    "    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';\n" +
                    "    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);\n" +
                    "  })();\n" +
                    "</script> \n");
        }

        if (insertIntoHead != null && !insertIntoHead.isEmpty()) {
            if (enableComments) {
                idrViewer.append("<!-- Begin user code -->\n");
            }
            idrViewer.append(insertIntoHead);
            if (enableComments) {
                idrViewer.append("<!-- End user code -->\n");
            }
        }

        final int pgWidths[] = new int[pageCount];
        final int pgHeights[] = new int[pageCount];
        for (int i = 1; i <= pageCount; i++) {
            final int tmpRotation = pageData.getRotation(i);
            pgWidths[i - 1] = (int)(scaling * (tmpRotation == 0 || tmpRotation == 180 ? pageData.getCropBoxWidth2D(i) : pageData.getCropBoxHeight2D(i)));
            pgHeights[i - 1] = (int)(scaling * (tmpRotation == 0 || tmpRotation == 180 ? pageData.getCropBoxHeight2D(i) : pageData.getCropBoxWidth2D(i)));
        }
        final StringBuilder bounds = new StringBuilder();
        bounds.append(" = [[").append(pgWidths[0]).append(',').append(pgHeights[0]).append(']');
        for (int i = 1; i < pageCount; i++) {
            bounds.append(",[").append(pgWidths[i]).append(',').append(pgHeights[i]).append(']');
        }
        bounds.append(']');

        idrViewer.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"assets/IDRViewer.css\">\n");
        idrViewer.append("<script type=\"text/javascript\">\n");
        idrViewer.append("var isIE = false;\n");
        idrViewer.append("function idrLoad() {\n");
        idrViewer.append("\tvar bounds" + bounds.toString() + ";\n");
        idrViewer.append("\tIDRViewer.makeNavBar(" + pageCount + ",'" + customIO.getImageTypeUsed(ImageType.THUMBNAIL).getFileExtension() + "', bounds, " + isHTML + ");\n");
        if (toolBarLink != null && toolBarLink[0] != null && toolBarLink[1] != null && !toolBarLink[0].isEmpty() && !toolBarLink[1].isEmpty()) {
            idrViewer.append("\tIDRViewer.addToolBarHyperlink('" + toolBarLink[0] + "','" + toolBarLink[1] + "');\n");
        }

        idrViewer.append("}\n");
        idrViewer.append("</script>\n");
        idrViewer.append("<script src=\"assets/IDRViewer.js\" type=\"text/javascript\"></script>\n");
        idrViewer.append("<script src=\"assets/jquery-1.11.2.min.js\" type=\"text/javascript\"></script>\n");

        idrViewer.append("</head>\n\n");
        idrViewer.append("<!-- Background pattern courtesy of http://subtlepatterns.com/grey-washed-wall/ -->\n" +
                "<body style=\"background:url('assets/bg.png') repeat scroll 0 0 transparent;\">");

        idrViewer.append("<div id=\"left\">\n" +
                "<div id=\"leftNav\">\n" +
                "\t<div id=\"btnThumbnails\" onclick=\"IDRViewer.toggleOutlines(false)\" title=\"Thumbnails\" class=\"navBtn inactive\"></div>\n" +
                "\t<div id=\"btnOutlines\" onclick=\"IDRViewer.toggleOutlines(true)\" title=\"Bookmarks\" class=\"navBtn inactive\"></div>\n" +
                "</div>\n" +
                "<div id=\"leftContent\">\n" +
                "\t<div id=\"thumbnailPanel\"></div>\n" +
                "\t<div id=\"outlinePanel\"></div>\n" +
                "</div>\n" +
                "</div>\n" +
                '\n' +
                "<div id=\"main\">\n" +
                '\n' +
                "<div id=\"mainNav\">\n" +
                "\t<div id=\"btnSideToggle\" onclick=\"IDRViewer.toggleThumbnails();\" class=\"navBtn\"><div title=\"Sidebar\"></div></div>\n" +
                "\t\n" +
                "\t<div id=\"btnPrev\" title=\"Previous Page\" onclick=\"IDRViewer.prev()\" class=\"navBtn\"></div>\n" +
                "\t<select id=\"goBtn\" onchange=\"IDRViewer.goToPage(0)\">\n" +
                '\n' +
                "\t</select>\n" +
                "\t<span id=\"pgCount\"></span>\n" +
                "\t<div id=\"btnNext\" title=\"Next Page\" onclick=\"IDRViewer.next()\" class=\"navBtn\"></div>\n" +
                "\t\n" +
                "\t<div id=\"btnSelect\" title=\"Select\" onclick=\"IDRViewer.setSelectMode(0)\" class=\"navBtn\"></div>\n" +
                "\t<div id=\"btnMove\" title=\"Pan\" onclick=\"IDRViewer.setSelectMode(1)\" class=\"navBtn\"></div>\n" +
                '\n' +
                "\t<div id=\"btnZoomOut\" title=\"Zoom Out\" onclick=\"IDRViewer.zoomOut()\" class=\"navBtn\"></div>\n" +
                "\t<select id=\"zoomBtn\" onchange=\"IDRViewer.zoomUpdate()\">\n" +
                "\t\t<option value=\"0\">100%</option>\n" +
                "\t\t<option value=\"1\">Actual Size</option>\n" +
                "\t\t<option value=\"2\">Fit Width</option>\n" +
                "\t\t<option value=\"3\">Fit Height</option>\n" +
                "\t\t<option value=\"4\">Fit Page</option>\n" +
                "\t\t<option value=\"5\">Automatic</option>\n" +
                '\n' +
                "\t</select>\n" +
                "\t<div id=\"btnZoomIn\" title=\"Zoom In\" onclick=\"IDRViewer.zoomIn()\" class=\"navBtn\"></div>\n" +
                "\t<div id=\"btnFullscreen\" title=\"Fullscreen\" onclick=\"IDRViewer.toggleFullScreen();\" class=\"navBtn closed\"></div>\n" +
                (toolBarPDFLink ? "\t<div id=\"btnDownload\" title=\"Download PDF\" onclick=\"document.location.href='downloadPDF.pdf';\" class=\"navBtn\"></div>" : "") +
                "</div>\n" +
                '\n' +
                "<div id=\"mainContent\">\n" +
                '\n' +
                "<div id=\"contentContainer\">\n" +
                "<div id=\"jpedal\"></div>\n" +
                "</div>\n" +
                "</div>\n" +
                "</div>\n");

        idrViewer.append("<script type=\"text/javascript\">idrLoad();</script>\t</body>\n");
        idrViewer.append("</html>\n\n");

        customIO.writePlainTextFile(rootDir + "/index.html", idrViewer);
    }

}
