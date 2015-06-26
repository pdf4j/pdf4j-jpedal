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
 * ConversionOptions.java
 * ---------------
 */
package org.jpedal.render.output;

import org.jpedal.render.output.options.TextMode;

import javax.print.attribute.standard.PageRanges;
import java.util.*;

public abstract class ConversionOptions {

    public enum FontMode {
        EMBED_ALL_EXCEPT_BASE_FAMILIES(FontMapper.EMBED_ALL_EXCEPT_BASE_FAMILIES),
        EMBED_ALL(FontMapper.EMBED_ALL),
        DEFAULT_ON_UNMAPPED(FontMapper.DEFAULT_ON_UNMAPPED),
        FAIL_ON_UNMAPPED(FontMapper.FAIL_ON_UNMAPPED);

        private final int value;
        FontMode(final int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
    public enum ScalingMode {
        SCALE,
        FITWIDTH,
        FITHEIGHT,
        FITWIDTHHEIGHT
    }

    public enum Font {
        WOFF,
        OTF,
        EOT,
        CFF,
        WOFF_BASE64(WOFF),
        OTF_BASE64(OTF);

        private final Font baseType;
        private final boolean isBase64;
        Font() {
            this(null);
        }
        Font(final Font baseType) {
            this.baseType = baseType;
            isBase64 = baseType != null;
        }
        public boolean isBase64() {
            return isBase64;
        }
        public Font getBaseType() {
            return baseType == null ? this : baseType;
        }
    }

    private ScalingMode scalingMode = ScalingMode.SCALE;

    
    //Fields
    private PageRanges realPageRange;
    private PageRanges logicalPageRange;
    private boolean addIDRLogo;
    private float scaling;
    private int scalingFitWidth;
    private int scalingFitHeight;
    private int[] scalingFitWidthHeight;
    private boolean embedImagesAsBase64Stream;
    private boolean convertSpacesToNbsp;
    private boolean convertPDFExternalFileToOutputType;
    private String formTag;
    private FontMode fontMode;
    private boolean keepGlyfsSeparate;
    private boolean separateToWords;
    private String[] encoding;
    private String fontsToRasterizeInTextMode;
    private boolean keepOriginalImage;
    private Font[] includedFonts;
    private String encryptedPassword;
    private boolean disableLinkGeneration;
    private boolean useLegacyImageFileType;
    private boolean preserveExtractionValues;

    private Integer decimalsToKeep;

    protected boolean userSetFonts;//If user set font, do not update to use EOT if IE text mode used.

    //Constructors & other required methods
    protected final Map<String, String> jvmOptions;

    public ConversionOptions(Map<String, String> jvmOptions) {
        setDefaults();


        // We need to combine the JVM options here otherwise the new API entry point will not observe JVM set options.
        // ExtractPagesAsHTML will do this work twice, but there should be no side effects.
        if (jvmOptions == null) {
            jvmOptions = new HashMap<String, String>();
        }
        //Combine system properties and passed in properties into a single Map
        final Properties sysProps = System.getProperties();
        final Enumeration<String> e = (Enumeration<String>) sysProps.propertyNames();
        while (e.hasMoreElements()) {
            final String key = e.nextElement();
            final String value = sysProps.getProperty(key);
            jvmOptions.put(key, value);
        }

        //add both codes if some client is checking
        encryptedPassword = jvmOptions.remove("org.jpedal.pdf2html.password");
        if(encryptedPassword==null || encryptedPassword.isEmpty()){
            encryptedPassword = jvmOptions.remove("org.jpedal.password");
        }

        this.jvmOptions = jvmOptions;
        setValuesFromJVMProperties();
    }

    /**
     * 
     * @param key is of type String.
     * @return boolean used internally to show if valid.
     *
     * @deprecated - kept for backwards code support (do not use)*/
    public boolean notSet(final String key) {
        return jvmOptions == null || !jvmOptions.containsKey(key);
    }

    protected abstract void setDefaults();

    //Setters

    /**
     * Set a page range using real page numbering.
     * <p>
     * For example, let's assume a 7 page document with a page range of "2,4,6".
     * <p>
     * When using this setting, you will get 2.html, 4.html and 6.html, and navigation
     * bars would behave as if pages 1, 3, 5 and 7 exist.
     * <p>
     * Possible values:
     * <ul>
     *      <li>Comma separated values including ranges. E.g. "1,3,5-8,10"</li>
     * </ul>
     * <p>
     * <b>Default value: all pages, e.g. "1-39"</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.realPageRange=</i></b>
     *
     * @param value is of Object type PageRanges.
     */
    public void setRealPageRange(final PageRanges value) {
        realPageRange = value;
    }

    /**
     * Set a page range using logical page numbering.
     * <p>
     * For example, let's assume a 7 page document with a page range of "2,4,6".
     * <p>
     * When using this setting, you will get 1.html, 2.html and 3.html, and navigation
     * bars would assume that only these 3 pages exist.
     * <p>
     * Possible values:
     * <ul>
     *      <li>comma separated values including ranges. E.g. "1,3,5-8,10"</li>
     * </ul>
     * <p>
     * <b>Default: null</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.logicalPageRange=</i></b>
     *
     * @param value is of Object type PageRanges.
     */
    public void setLogicalPageRange(final PageRanges value) {
        logicalPageRange = value;
    }

    /**
     * Sets the Scaling value of the converted content.
     * <p>
     * Possible values:
     * <ul>
     *      <li>1 (float multiplier)</li>
     *      <li>1000x1000 (best fit width/height)</li>
     *      <li>fitWidth1000 (best fit width)</li>
     *      <li>fitHeight1000 (best fit height)</li>
     * </ul>
     * <p>
     * <b>Default: 1</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.scaling=</i></b>
     *
     * @param value is of type float.
     */
    public void setScaling(final float value) {
        scaling = value;
        scalingMode = ScalingMode.SCALE;
    }

    public void setScalingFitWidth(final int value) {
        scalingFitWidth = value;
        scalingMode = ScalingMode.FITWIDTH;
    }

    public void setScalingFitHeight(final int value) {
        scalingFitHeight = value;
        scalingMode = ScalingMode.FITHEIGHT;
    }

    public void setScalingFitWidthHeight(final int width, final int height) {
        scalingFitWidthHeight = new int[]{width, height};
        scalingMode = ScalingMode.FITWIDTHHEIGHT;
    }

    /**
     * Embed images as Base64 stream.
     * <p>
     * This works for image or svg specific text modes. (image_* or svg_*_nofallback modes).
     * <p>
     * It does not work for svg modes with an image fallback as it would generate unnecessarily large files.
     * <p>
     * Possible values: 
     * <ul>
     *      <li>true/false</li>
     * </ul>
     * <p>
     * <b>Default: false</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.embedImagesAsBase64Stream=</i></b>
     *
     * @param value is of type boolean.
     */
    public void setEmbedImagesAsBase64Stream(final boolean value) {
        embedImagesAsBase64Stream = value;
    }

    /**
     * Convert spaces to &nbsp; in the output.
     * <p>
     * Possible values:
     * <ul>
     *      <li>true/false</li>
     * </ul>
     * <p>
     * <b>Default: false</b>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.convertSpacesToNbsp=</i></b>
     *
     * @param value is of type boolean.
     */
    public void setConvertSpacesToNbsp(final boolean value) {
        convertSpacesToNbsp = value;
    }

    /**
     * Set whether we convert links pointing to file.pdf into file.html (default is true).
     * <p>
     * Possible values: 
     * <ul>
     *      <li>true/false</li>
     * </ul>
     * <p>
     * <b>Default: true</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.convertPDFExternalFileToOutputType=</i></b>
     *
     * @param value is of type boolean.
     */
    public void setConvertPDFExternalFileToOutputType(final boolean value) {
        convertPDFExternalFileToOutputType = value;
    }

    /**
     * Replacing &lt;form&gt; with your version.
     * <p>
     * Possible values:
     *  <ul>
     *      <li>&lt;form **your code here**&gt;</li>
     * </ul>
     * <p>
     * <b>Default: &lt;form&gt;</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.formTag=</i></b>
     *
     * @param value is of type String.
     */
    public void setFormTag(final String value) {
        formTag = value;
    }

    /**
     * Set mode used to determine which fonts are converted.
     * <p>
     * Possible values:
     * <ul>
     *      <li>embed_all_except_base_families</li>
     *      <li>embed_all</li>
     *      <li>default_on_unmapped</li>
     *      <li>fail_on_unmapped</li>
     * </ul>
     * <p>
     * <b>Default: embed_all</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.fontMode=</i></b>
     *
     * @param value  if Object of type FontMode.
     * @see <a href="http://blog.idrsolutions.com/2011/06/pdf-to-html5-conversion-text-and-fonts-part-2/">Learn about font modes</a>
     */
    public void setFontMode(final FontMode value) {
        fontMode = value;
    }

    /**
     * Set fonts to convert to and include in HTML and CSS output.
     * <p>
     * Possible values:
     * <p>
     * Any comma separated list containing:
     * <ul>
     *      <li>woff,</li>
     *      <li>otf,</li>
     *      <li>eot,</li>
     *      <li>cff,</li>
     *      <li>woff_base64,</li>
     *      <li>otf_base64</li>
     * </ul>
     * <b>Default: woff,eot</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.includedFonts=</i></b>
     *
     * @param includedFonts is an array of Object type Font.
     */
    public void setIncludedFonts(final Font[] includedFonts) {
        this.includedFonts = includedFonts;
        userSetFonts = true;
    }

    /**
     * Position each glyph individually for more accurate positioning.
     * <p>
     * Produces a larger file but may suit some use cases
     * <p>
     * Possible values: 
     * <ul>
     *      <li>true/false</li>
     * </ul>
     * <b>Default: false</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.keepGlyfsSeparate=</i></b>
     *
     * @param value is of type boolean.
     */
    public void setKeepGlyfsSeparate(final boolean value) {
        keepGlyfsSeparate = value;
    }

    /**
     * Divide text chunks into words based on spaces
     * This improves accuracy but not as much as keepGlyfsSeparate,
     * It Produces a larger file but may suit some use cases.
     * <p>
     * Possible values: 
     * <ul>
     *      <li>true/false</li>
     * </ul>
     * <p>
     * <b>Default: false</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.separateTextToWords=</i></b>
     *
     * @param value is of type boolean.
     */
    public void setSeparateToWords(final boolean value) {
    	separateToWords = value;
    }

    /**
     * Set the encoding for Java and HTML
     * Expects 2 values comma separated - java,html.
     * <p>
     * Possible values: 
     * <ul>
     *      <li>e.g. "UTF-16,utf-16"</li>
     * </ul>
     * <p>
     * <b>Default: "UTF-8,utf-8"</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.encoding=</i></b>
     *
     * @param java is of type String.
     * @param output is of type String.
     */
    public void setEncoding(final String java, final String output) {
        encoding = new String[]{java, output};
    }

    /**
     * Keep original images (Text Modes SVG_* only).
     * <p>
     * E.g. if a PDF contains an 1000x1000 image that is only drawn at 100x100,
     * setting to true will output the image at 1000x1000 instead of 100x100.
     * <p>
     * Possible values:
     * <ul>
     *      <li>true/false</li>
     * </ul>
     * <p>
     * <b>Default: false</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.keepOriginalImage=</i></b>
     *
     * @param value is of type boolean.
     */
    public void setKeepOriginalImage(final boolean value) {
        keepOriginalImage = value;
    }
    
     /**
     * If enabled, text will be remapped (ie ligatures) and not kept as single glyf value in real text modes but
     * it can potentially cause display issues so we recommend not using it
     * <p>
     * Possible value:
     * <ul>
     *      <li>true/false</li>
     * </ul>
     * <p>
     * <b>Default: false</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.preserveExtractionValues=</i></b>
     *
     * @param value is of type boolean.
     * @deprecated Not recommended for general usage - added for specific customer use case scenario
     */
    @Deprecated
    public void setPreserveExtractionValues(final boolean value) {
        preserveExtractionValues = value;
    }

    /**
     * If enabled, all images output will use the PNG file type.
     * <p>
     * Possible value:
     * <ul>
     *      <li>true/false</li>
     * </ul>
     * <p>
     * <b>Default: false</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.useLegacyImageFileType=</i></b>
     *
     * @param value is of type boolean.
     * @deprecated Due for removal in July 2015 release.
     */
    @Deprecated
    public void setUseLegacyImageFileType(final boolean value) {
        useLegacyImageFileType = value;
    }


    //Getters
    
    public PageRanges getRealPageRange() {
        return realPageRange;
    }

    public PageRanges getLogicalPageRange() {
        return logicalPageRange;
    }

    protected abstract TextMode getTextMode();

    protected ScalingMode getScalingMode() {
        return scalingMode;
    }

    protected float getScaling() {
        return scaling;
    }

    protected int getScalingFitWidth() {
        return scalingFitWidth;
    }

    protected int getScalingFitHeight() {
        return scalingFitHeight;
    }

    protected int[] getScalingFitWidthHeight() {
        return scalingFitWidthHeight;
    }

    protected boolean getEmbedImagesAsBase64Stream() {
        return embedImagesAsBase64Stream;
    }

    protected boolean getConvertSpacesToNbsp() {
        return convertSpacesToNbsp;
    }

    protected boolean getConvertPDFExternalFileToOutputType() {
        return convertPDFExternalFileToOutputType;
    }

    protected String getFormTag() {
        return formTag;
    }

    protected FontMode getFontMode() {
        return fontMode;
    }

    protected Font[] getIncludedFonts() {
        return includedFonts;
    }

    protected boolean getKeepGlyfsSeparate() {
        return keepGlyfsSeparate;
    }

    protected boolean getSeparateToWords(){
    	return separateToWords;
    }

    protected boolean getAddIDRViewerLogo(){
        return addIDRLogo;
    }

    protected String[] getEncoding() {
        return encoding;
    }

    public String getPassword(){
    	return encryptedPassword;
    }

    protected boolean getDisableLinkGeneration(){
    	return disableLinkGeneration;
    }

    /**
     * In Text Modes, you can override real text on a font by font basis,
     * <p>
     * In svg_realtext text will appear as part of SVG as Vectors
     * This setting has no effect in other modes,
     * <p>
     * Needs to start EXCLUDE= or INCLUDE=
     * followed by a list of comma separated fonts
     * Comparison is case-insensitive so Arial, ARIAL and arial are the same.
     * <p>
     * Possible values:
     * <ul>
     *      <li>INCLUDE=font1,font2,font3 ... (rasterized JUST these fonts)</li>
     *      <li>EXCLUDE=font1,font2,font3 ... (rasterized all fonts EXCEPT these fonts)</li>
     * </ul>
     * <p>
     * For example:
     * <ul>
     *      <li>options.setFontsToRasterizeInTextMode("EXCLUDE=PTSerif-Regular,Arial");</li>
     *      <li>options.setFontsToRasterizeInTextMode("INCLUDE=PTSerif-Regular,Arial");</li>
     * </ul>
     * <p>
     * <b>Default: no effect</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.fontsToRasterizeInTextMode=</i></b>
     *
     * @param value is of type String.
     */
    public void setFontsToRasterizeInTextMode(final String value) {
        fontsToRasterizeInTextMode=value;
    }

    protected String getFontsToRasterizeInTextMode() {
        return fontsToRasterizeInTextMode;
    }

    public boolean getKeepOriginalImage() {
        return keepOriginalImage;
    }

    protected boolean getPreserveExtractionValues() {
        return preserveExtractionValues;
    }

    public boolean getUseLegacyImageFileType() {
        return useLegacyImageFileType;
    }



    //JVM Settings
    protected abstract void setValuesFromJVMProperties();

    protected void setValuesFromJVMProperties(final String key) {
        final String prefix = "org.jpedal.pdf2html.";
        if (key.equals("realPageRange")) {
            setJVMRealPageRange("realPageRange");
        } else if (key.equals("logicalPageRange")) {
            setJVMLogicalPageRange("logicalPageRange");
        } else if(key.equals("idrLogo")){
            addIDRLogo = setJVMBooleanValue("idrLogo");
        } else if (key.equals("textMode")) {
            setJVMTextMode("textMode");
        } else if (key.equals("scaling")) {
            setJVMScaling("scaling");
        } else if (key.equals("embedImagesAsBase64Stream")) {
            embedImagesAsBase64Stream = setJVMBooleanValue("embedImagesAsBase64Stream");
        } else if (key.equals("convertSpacesToNbsp")) {
            convertSpacesToNbsp = setJVMBooleanValue("convertSpacesToNbsp");
        } else if (key.equals("convertPDFExternalFileToOutputType")) {
            convertPDFExternalFileToOutputType = setJVMBooleanValue("convertPDFExternalFileToOutputType");
        } else if (key.equals("formTag")) {
            formTag = jvmOptions.get(prefix + "formTag");
        } else if (key.equals("fontMode")) {
            setJVMFontMode("fontMode");
        } else if (key.equals("convertOTFFonts")) {
            System.out.println("Setting convertOTFFonts failed. Please use includedFonts instead.");
        } else if (key.equals("includedFonts")) {
            setJVMIncludedFonts("includedFonts");
        } else if (key.equals("keepGlyfsSeparate")) {
            keepGlyfsSeparate = setJVMBooleanValue("keepGlyfsSeparate");
        } else if (key.equals("separateTextToWords")) {
        	separateToWords  = setJVMBooleanValue("separateTextToWords");
        } else if (key.equals("encoding")) {
            setJVMEncoding("encoding");
        } else if (key.equals("fontsToRasterizeInTextMode")) {
            setFontsToRasterize("fontsToRasterizeInTextMode");
        } else if (key.equals("keepOriginalImage")) {
            keepOriginalImage = setJVMBooleanValue("keepOriginalImage");
        } else if (key.equals("disableLinkGeneration")){
        	disableLinkGeneration = setJVMBooleanValue("disableLinkGeneration");
        } else if(key.equals("decimalsToRetainInShapePositions")){
            decimalsToKeep=setJVMIntValue("decimalsToRetainInShapePositions");
        } else if(key.equals("useLegacyImageFileType")){
            useLegacyImageFileType=setJVMBooleanValue("useLegacyImageFileType");
        } else if(key.equals("preserveExtractionValues")){
            preserveExtractionValues=setJVMBooleanValue("preserveExtractionValues");
        }
    }

    //Helper error method
    String errors = "";
    protected void addError(final String error) {
        errors = errors + error + '\n';
    }

    public String getErrors() {
        return errors;
    }

    //Private JVM setters
    protected boolean setJVMBooleanValue(final String key) {
        String value = jvmOptions.get("org.jpedal.pdf2html." + key);
        value = value.toLowerCase();
        if (value.equals("true")) {
            return true;
        } else if (value.equals("false")) {
            return false;
        } else {
            addError("Value \"" + value + "\" for " + key + " was not recognised. Use true or false.");
            return false;
        }
    }

    //Private JVM setters
    protected int setJVMIntValue(final String key) {
        final String value = jvmOptions.get("org.jpedal.pdf2html." + key);
        try{
           return Integer.parseInt(value);
        }catch(final Exception e){
            addError("Value \"" + value + "\" for " + key + " was not recognised. Use an integer value. "+e);
            return 0;
        }
    }

    private void setJVMRealPageRange(final String key) {
        final String value = jvmOptions.get("org.jpedal.pdf2html." + key);
        try {
            realPageRange = new PageRanges(value);
        } catch (final Exception e) {
            addError("Setting page range failed with value: " + value+ ' ' +e);
        }
    }

    private void setJVMLogicalPageRange(final String key) {
        final String value = jvmOptions.get("org.jpedal.pdf2html." + key);
        try {
            logicalPageRange = new PageRanges(value);
        } catch (final Exception e) {
            addError("Setting page range failed with value: " + value+ ' ' +e);
        }
    }

    protected abstract void setJVMTextMode(String value);

    private void setJVMScaling(final String key) {
        final String value = jvmOptions.get("org.jpedal.pdf2html." + key);
        if (value.contains("x")) {
            final String[] temp = value.split("x");
            setScalingFitWidthHeight(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]));
        } else if (value.toLowerCase().contains("fitwidth")) {
            setScalingFitWidth(Integer.parseInt(value.substring(8)));
        } else if (value.toLowerCase().contains("fitheight")) {
            setScalingFitHeight(Integer.parseInt(value.substring(9)));
        } else {
            setScaling(Float.parseFloat(value));
        }
    }

    private void setJVMFontMode(final String key) {
        boolean wasSet = false;
        String value = jvmOptions.get("org.jpedal.pdf2html." + key);
        value = value.toUpperCase();
        for (final FontMode m : FontMode.values()) {
            if (value.equals(m.toString())) {
                fontMode = m;
                wasSet = true;
                break;
            }
        }
        if (!wasSet) {
            addError("Setting font mode failed with value: " + value);
        }
    }

    private void setJVMEncoding(final String key) {
        final String value = jvmOptions.get("org.jpedal.pdf2html." + key);
        try {
            final String[] values = value.split(",");
            encoding = new String[]{values[0], values[1]};
        } catch (final Exception e) {
            addError("Setting encoding failed with value: " + value+' '+e);
        }
    }
    
    private void setFontsToRasterize(final String key) {
        final String value = jvmOptions.get("org.jpedal.pdf2html." + key);
        
        if (!value.startsWith("INCLUDE=") && !value.startsWith("EXCLUDE=")) {
            addError("Setting fontsToRasterizeInTextMode failed - must start with INCLUDE= or EXCLUDE= " + value);
        }
        
        fontsToRasterizeInTextMode=value;
    }

    private void setJVMIncludedFonts(final String key) {
        String value = jvmOptions.get("org.jpedal.pdf2html." + key);
        // Using a Set makes sure there are no duplicates
        final HashSet<Font> fonts = new HashSet<Font>();
        value = value.toLowerCase();
        boolean error = false;
        for(final String font : value.split(",")) {
            if(font.equals("woff")) {
                fonts.add(Font.WOFF);
            } else if(font.equals("woff_base64")) {
                fonts.add(Font.WOFF_BASE64);
            } else if(font.equals("eot")) {
                fonts.add(Font.EOT);
            } else if(font.equals("otf")) {
                fonts.add(Font.OTF);
            } else if(font.equals("otf_base64")) {
                fonts.add(Font.OTF_BASE64);
            } else if(font.equals("cff")) {
                fonts.add(Font.CFF);
            } else {
                addError("Unsupported font type: " + font);
                error = true;
            }
        }
        if(error || fonts.isEmpty()) {
            addError("Setting included fonts failed with value: " + value);
        } else {
            this.includedFonts = fonts.toArray(new Font[fonts.size()]);
            userSetFonts = true;
        }
    }
    
    /**
     * Allow user to control precision of numbers used to draw shapes in SVG and SVG mode in HTML
     * 
     * Default behaviour is a hybrid mode where shapes consisting only of horizontal and vertical lines are aligned to
     * the pixel grid to increase the sharpness. All other shapes have accuracy of 1 decimal place.
     *
     * Possible values:
     * <ul>
     *      <li>0 rounds to int</li>
     *      <li>&lt;0 returns all float values (making very accurate but large files)</li>
     *      <li>&gt;0 returns number of decimal points specified so a value of 2 will changed 12.345678 to 12.34</li>
     * </ul>
     * <p>
     * <b>Default value: unset</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.decimalsToRetainInShapePositions=</i></b>
     *
     * @param value is of Object type PageRanges.
     * @deprecated The default setting should fulfil all use cases now. If you are a customer and you disagree please
     * let us know, otherwise we will remove this setting in a future update.
     */
    @Deprecated
    public void setDecimalsToRetainInShapes(final int value) {
        decimalsToKeep = value;
    }
    
    /**
     * zero will result in int and negative number will leave all decimals
     * @return num decimals to retain
     */
    protected Integer getDecimalsToRetainInShapes() {
        return decimalsToKeep;
    }
}
