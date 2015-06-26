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
 * FontHelper.java
 * ---------------
 */
package org.jpedal.render.output;

import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.fonts.tt.conversion.GlyphMapping;
import org.jpedal.render.output.io.CustomIO;
import org.jpedal.utils.LogWriter;

import java.util.*;

@SuppressWarnings("PMD")
public class FontHelper {

    //Flag to enable experimental glyph remapping code for Type 1
    private static final boolean useT1Mapping=true;

    protected final ArrayList<String> fontFaces = new ArrayList<String>();

    private final HashMap<String, HashMap<String, GlyphMapping>> newFontCmaps = new HashMap<String, HashMap<String, GlyphMapping>>();
    private final HashMap<String, HashSet<String>> newFontMappedValues = new HashMap<String, HashSet<String>>();

    //IE only allows for fonts with names under 32 characters long
    private static final int FONT_NAME_LENGTH_LIMIT = 31;

    protected final HashMap<String, FontToConvert> fontsToConvert = new HashMap<String, FontToConvert>();

    protected final HashMap<String,HashMap<String,Integer>> widths = new HashMap<String, HashMap<String, Integer>>();

    protected final HashSet<String> usedFonts = new HashSet<String>();

    /**used in text mode to allow use to override on a font by font basis*/
    private FontRasterizer fontRasterizer;

    //flag to show embedded fonts present so need to use full name
    protected boolean hasEmbeddedFonts;

    protected final HashSet<String> embeddedFonts = new HashSet<String>();

    protected final HashMap<String, Base64EncodedFont> base64EncodedFonts = new HashMap<String, Base64EncodedFont>();

    private String lastFontUsed = "";
    private FontMapper fontMapper;

    private final int rawPageNumber;
    private final ConversionOptions.Font[] includedFonts;
    private final int fontMode;
    private final boolean preserveExtractionValues;

    public FontHelper(int rawPageNumber, final ConversionOptions.Font[] includedFonts, final int fontMode, final boolean preserveExtractionValues, final String fontsToRasterize) {
        this.rawPageNumber = rawPageNumber;
        this.includedFonts = includedFonts;
        this.fontMode = fontMode;
        this.preserveExtractionValues = preserveExtractionValues;

        if(fontsToRasterize != null){
            fontRasterizer = new FontRasterizer(fontsToRasterize);
        }
    }

    public void saveEmbeddedFont(final PdfFont pdfFont, byte[] rawFontData, final String fileType) {
        String fontName = sanitizeFontName(pdfFont.getBaseFontName());
        if (fontName.isEmpty()) {
            fontName = "font";
        }

        if (pdfFont.isFontSubstituted()) {
            return;
        }

        pdfFont.resetNameForHTML(fontName);


        /**
         * check if we should put ignore this font
         */
        final boolean rasterizeFont=fontRasterizer!=null && fontRasterizer.isFontRasterized(pdfFont.getTruncatedName());

        //the fonts with , in name from Ghostscript do not work so ignore
        //add 1==2 && to line below and it will now cascade into Sam's font handler
        if(rasterizeFont){ //ignore the font
            rawFontData=null;

            //System.out.println("ignore font "+fontName);

        } else if(fileType.equals("t1") || fileType.equals("cff") || (fileType.equals("ttf")&& !fontName.contains(","))){ // postscript to otf

            //tell software name needs to be unique
            hasEmbeddedFonts=true;
            //Generate a unique font name if this font name matches another font with the same name but different data
            if (fontsToConvert.get(fontName) != null &&
                    !Arrays.equals(fontsToConvert.get(fontName).getRawFontData(), rawFontData)) {

                int i = 2;
                while (fontsToConvert.get(fontName+ '_' +i) != null &&
                        !Arrays.equals(fontsToConvert.get(fontName+ '_' +i).getRawFontData(), rawFontData)) {
                    i++;
                }
                fontName += "_"+i;
                pdfFont.resetNameForHTML(fontName);
            }
            fontsToConvert.put(fontName, new FontToConvert(pdfFont, rawFontData, fileType));
        }

        if(rawFontData!=null){
            embeddedFonts.add(fontName);
        }
    }

    /**
     * Make sure that the font name we use can be used in the file system. <br>
     * i.e. Remove all special/unusable characters and replace them with a '-' and
     * reduce the length if neccessary for IE.
     * @param baseFontName font name to sanitize
     * @return A file safe FontName
     */
    private String sanitizeFontName(String baseFontName) {

        //Reduce the length of the font name if necessary
        final int limit = FONT_NAME_LENGTH_LIMIT -String.valueOf(rawPageNumber).length();
        if (baseFontName.length() > limit) {
            baseFontName = baseFontName.substring(0, limit);
        }

        //Replace all unacceptable characters
        return baseFontName.replaceAll("[^a-zA-Z0-9_ ]", "-");
    }

    /**
     * Generates the @font-face css for a given font and set of types to convert to
     * @param fontName the name of the font
     */
    private void generateFontFaceCSS(final String fontName, final String fileName) {
        final StringBuilder sb = new StringBuilder();
        sb.append("@font-face {\n");
        sb.append("\tfont-family: ").append(fontName).append(rawPageNumber).append(";\n");

        ConversionOptions.Font[] conversionTypes = includedFonts;

        // if there is only one font just output it without any extra data
        if(conversionTypes.length == 1) {
            makeFontURL(sb, fontName, conversionTypes[0], true, fileName);
            sb.append(";\n");
        } else {
            // Otherwise we need to check if eot font support is wanted
            boolean hasEot = false;
            // Rebuild the list of wanted font formats without eot if it's present
            final ArrayList<ConversionOptions.Font> conversionTypesList = new ArrayList<ConversionOptions.Font>(conversionTypes.length);
            for(final ConversionOptions.Font type : conversionTypes) {
                if(type == ConversionOptions.Font.EOT) {
                    hasEot = true;
                } else {
                    conversionTypesList.add(type);
                }
            }
            conversionTypes = conversionTypesList.toArray(new ConversionOptions.Font[conversionTypesList.size()]);
            if(hasEot) {
                // If eot font format is wanted we will need to add a few special cases so it works in older web browsers
                makeFontURL(sb, fontName, ConversionOptions.Font.EOT, false, fileName);
            } else {
                // Otherwise we just need to start our list of fonts
                sb.append("\tsrc: ");
            }
            for(int i = 0; i < conversionTypes.length; i++) {
                // Here we append the font family rule for a specific file type
                makeFontURL(sb, fontName, conversionTypes[i], false, fileName);

                if(i < conversionTypes.length - 1) {
                    sb.append(",\n"); // add the comma and new line for the next entry
                }
            }
            sb.append(";\n");
        }
        sb.append("}\n");
        fontFaces.add(sb.toString());
    }

    /**
     * Method that adds the correct font url data to a StringBuilder based on the font name, type and whether it needs to be a base64 encoded font or not.
     * @param sb The StringBuilder to use
     * @param fontName The fonts name
     * @param conversionType The fonts type, e.g. otf, woff, eot
     */
    private void makeFontURL(final StringBuilder sb, final String fontName, final ConversionOptions.Font conversionType, final boolean prependSrc, final String fileName) {
        final String conversionTypeAsString = conversionType.getBaseType().toString().toLowerCase();
        if(conversionType.isBase64()) {

            if(prependSrc) {
                sb.append("src: ");
            }

            final String encodedData;
            final Base64EncodedFont base64Font = base64EncodedFonts.get(fontName);
            if(base64Font == null) {
                System.err.println("Error in base64 encoded fonts");
                encodedData = "";
            } else {
                encodedData = base64Font.getBase64Data(conversionType);
            }

            final String mimeType;
            if(conversionType.getBaseType() == ConversionOptions.Font.WOFF) {
                mimeType = "application/font-woff";
            } else if(conversionType.getBaseType() == ConversionOptions.Font.OTF) {
                mimeType = "font/truetype";
            } else {
                mimeType = "application/x-font-" + conversionTypeAsString;
            }

            sb.append("url(data:").append(mimeType).append(";charset=utf-8;base64,").append(encodedData).append(") format(\"").append(conversionTypeAsString).append("\")");
        } else {
            if(conversionType == ConversionOptions.Font.EOT) {
                sb.append("\tsrc: url(\"").append(fileName).append("/fonts/").append(fontName).append('.').append("eot").append("\");\n");
                sb.append("\tsrc: url(\"").append(fileName).append("/fonts/").append(fontName).append('.').append("eot?#iefix").append("\") format(\"embedded-opentype\"),\n");
            } else {
                final String format = (conversionType == ConversionOptions.Font.OTF) ? "opentype" : conversionTypeAsString;
                if(prependSrc) {
                    sb.append("\tsrc :").append("url(\"").append(fileName).append("/fonts/").append(fontName).append('.').append(conversionTypeAsString).append("\") format(\"").append(format).append("\")");
                } else {
                    sb.append("\t\turl(\"").append(fileName).append("/fonts/").append(fontName).append('.').append(conversionTypeAsString).append("\") format(\"").append(format).append("\")");
                }
            }
        }
    }

    /**
     * New font writing method. Takes an array of types and does the conversion on each one
     * @param fontToConvert The font to convert information
     */
    private synchronized void writeOutFont(final FontToConvert fontToConvert, final String rootDir, final String fileName, final CustomIO customIO) {
        final PdfFont pdfFont = fontToConvert.getPdfFont();
        final String fileType = fontToConvert.getFileType();
        final String fontName = pdfFont.getBaseFontName();
        byte[] rawFontData = fontToConvert.getRawFontData();

        final String fontPath = rootDir + fileName + "/fonts/";

        // If it's a ttf font we need to convert it for HTML
        if(fileType.equals("ttf")) {

            rawFontData = org.jpedal.fonts.HTMLFontUtils.convertTTForHTML(rawFontData);

            if(rawFontData == null) {
                return; // error in the conversion so cannot continue
            }
        }

        // Get the Glyph mappings for the font
        Collection<GlyphMapping> mappings = null;
        if (newFontCmaps.get(fontName) != null) {
            mappings = newFontCmaps.get(fontName).values();
        }

        // Loop through and output all the right fonts
        for(final ConversionOptions.Font type : includedFonts) {
            if(type == ConversionOptions.Font.CFF) {
                // this is the raw font data as a file
                customIO.writeFont(fontPath + fontName + ".cff",rawFontData);
            } else {
                // Every other font file
                // make a copy of the font data to ensure the original is not changed/mutated by applied methods
                byte[] rawFontDataCopy = rawFontData.clone();

                // Font conversion can throw exceptions, we will still want the other font files if one conversion fails
                // So capture the exception and log it
                try {
                    final ConversionOptions.Font baseType = type.getBaseType();
                    if(baseType == ConversionOptions.Font.OTF) {
                        rawFontDataCopy = org.jpedal.fonts.HTMLFontUtils.convertPSForHTMLOTF(pdfFont, rawFontData, fileType, widths.get(fontName), mappings);
                    } else if(baseType == ConversionOptions.Font.WOFF) {
                        rawFontDataCopy = org.jpedal.fonts.HTMLFontUtils.convertPSForHTMLWOFF(pdfFont, rawFontData, fileType, widths.get(fontName), mappings);
                    } else if(baseType == ConversionOptions.Font.EOT) {
                        rawFontDataCopy = org.jpedal.fonts.HTMLFontUtils.convertPSForHTMLEOT(pdfFont, rawFontDataCopy, fileType, widths.get(fontName), mappings);
                    }

                    // Finally write out the font
                    if(type.isBase64()) {
                        final String encodedData = javax.xml.bind.DatatypeConverter.printBase64Binary(rawFontDataCopy);
                        Base64EncodedFont base64Font = base64EncodedFonts.get(fontName);
                        if(base64Font == null) {
                            base64Font = new Base64EncodedFont(fontName);
                            base64EncodedFonts.put(fontName, base64Font);
                        }
                        base64Font.putFontDats(type, encodedData);
                    } else {
                        customIO.writeFont(fontPath + fontName + '.' + baseType.toString().toLowerCase(), rawFontDataCopy);
                    }
                } catch (final Exception e) {
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                    //
                }
            }
        }
    }

    public void saveAdvanceWidth(String fontName, final String glyphName, final int width) {

        fontName = sanitizeFontName(fontName);

        HashMap<String,Integer> font = widths.get(fontName);

        if (font == null) {
            font = new HashMap<String, Integer>();
            widths.put(fontName, font);
        }

        font.put(glyphName, width);
    }

    public void flagDecodingFinished(final String rootDir, final String fileName, final CustomIO customIO) {
        for (final Map.Entry<String, FontToConvert> fontToConvert : fontsToConvert.entrySet()) {
            if (usedFonts.contains(fontToConvert.getValue().getPdfFont().getBaseFontName())) {
                // Write out the font
                writeOutFont(fontToConvert.getValue(), rootDir, fileName, customIO);

                // Generate font-face (after calling writeOutFont which generates the base64 data)
                generateFontFaceCSS(fontToConvert.getKey(), fileName);
            }
        }
    }

    public FontRasterizer getFontRasterizer() {
        return fontRasterizer;
    }

    public boolean isFontRasterized(final PdfFont currentFontData) {
        return fontRasterizer != null && fontRasterizer.isFontRasterized(currentFontData.getTruncatedName());
    }

    /**
     * Takes a glyph, character and font name and chooses a new character to use in order to avoid conflicts and thus
     * preserve the visual appearance of the final output.
     * @param glyphNo The glyph number to use
     * @param originalChar The character specified in the PDF
     * @param baseFontName The name of the font
     * @param allowReMapping Allow changes to the output character
     * @return
     */
    private String remapGlyph(final int glyphNo, String originalChar, final String baseFontName, final boolean allowReMapping) {

        //TODO: Check whether HTML-ized chars are occasionaly passed through and deal with them if so

        if (glyphNo != -1) {

           /*  //Convert pairs of characters into ligatures
             if (originalChar.codePointCount(0, originalChar.length()) > 1) {
                 originalChar = originalChar.trim();
                 if ("ff".equals(originalChar)) {
                     originalChar = "\ufb00";
                 } else if ("fi".equals(originalChar)) {
                     originalChar = "\ufb01";
                 } else if ("fl".equals(originalChar)) {
                     originalChar = "\ufb02";
                 } else if ("ffi".equals(originalChar)) {
                     originalChar = "\ufb03";
                 } else if ("ffl".equals(originalChar)) {
                     originalChar = "\ufb04";
                 } else if ("st".equals(originalChar)) {
                     originalChar = "\ufb06";
                 }
                 //
             }*/

            //Fetch cmap for this font
            HashMap<String, GlyphMapping> newCmap = newFontCmaps.get(baseFontName);
            HashSet<String> mappedValues = newFontMappedValues.get(baseFontName);
            if (newCmap == null) {
                newCmap = new HashMap<String, GlyphMapping>();
                newFontCmaps.put(baseFontName, newCmap);
                //                System.out.println("Placing "+baseFontName);
                mappedValues = new HashSet<String>();
                newFontMappedValues.put(baseFontName, mappedValues);
            }

            //Create composite key
            final String key = originalChar+ '-' +glyphNo;

            //Check if already mapped
            GlyphMapping mapping = newCmap.get(key);
            if (mapping != null) {
                mapping.use();
                return mapping.getOutputChar();
            }

            //If we can't remap store the mapping & return the original character
            if (!allowReMapping) {

                //Ignore if empty or if more than 1 codepoint (ligatures etc) to avoid mapping 'fi' glyph to 'f' char etc.
                if (originalChar.isEmpty() || originalChar.codePointCount(0, originalChar.length()) > 1) {
                    return originalChar;
                }

                mapping = new GlyphMapping(originalChar, glyphNo, originalChar, true);
                newCmap.put(key, mapping);
                mappedValues.add(originalChar);
                return originalChar;
            }

            //Reduce to first character for ligatures
            if (originalChar.codePointCount(0, originalChar.length()) > 1) {
                originalChar = originalChar.substring(0,1);
            }

            //If out of range, reset
            if (!originalChar.isEmpty() && originalChar.codePointAt(0) >= 0xFFFF) {
                originalChar = new String(Character.toChars(0xb0));
            }

            //Check this value isn't already assigned & use if not, unless it's in a control range
            if (!originalChar.isEmpty()) {
                final int c = originalChar.charAt(0);
                if (!mappedValues.contains(originalChar) && !originalChar.isEmpty() && c >= 0x20 && !(c >= 0x80 && c < 0xa0)) {
                    mapping = new GlyphMapping(originalChar, glyphNo, originalChar, true);
                    newCmap.put(key, mapping);
                    mappedValues.add(originalChar);
                    return originalChar;
                }
            }

            //            System.err.println("Font "+ baseFontName+" cannot use "+originalChar+" ("+(int)(originalChar.charAt(0))+") for "+glyphNo+" as it is already used");
            String newChar = null;

            //Check if there's an upper case version
            if (!originalChar.toUpperCase().equals(originalChar)) {
                newChar = originalChar.toUpperCase();

                //Check if there's a lower case version
            } else if (!originalChar.toLowerCase().equals(originalChar)) {
                newChar = originalChar.toLowerCase();
            }

            //If a different case version is found, check it's suitability
            if (newChar != null) {
                boolean useNewGlyf = false;

                //Check if it's free
                if (!mappedValues.contains(newChar)) {
                    useNewGlyf = true;

                    //Or if it has the same glyph number
                } else {

                    //Find mapping using this value
                    GlyphMapping conflictingMapping = null;
                    for (final GlyphMapping m : newCmap.values()) {
                        if (m.getOutputChar().equals(newChar)) {
                            conflictingMapping = m;
                        }
                    }

                    if (conflictingMapping.getGlyphNumber() == glyphNo) {
                        useNewGlyf = true;
                    }
                }

                if (useNewGlyf) {
                    //                    System.err.println("\tFound alternative! Using "+newChar+"("+(int)(newChar.charAt(0))+") instead of "+originalChar+" ("+(int)(originalChar.charAt(0))+")");
                    mapping = new GlyphMapping(originalChar, glyphNo, newChar, false);
                    newCmap.put(key, mapping);
                    mappedValues.add(newChar);
                    return newChar;
                }
            }

            //TODO: MARK FONT AS NEEDING A REPASS

            //Pick new value instead
            int start = 0xb0;
            do {
                start++;
                newChar = new String(Character.toChars(start));
            } while (mappedValues.contains(newChar));

            //            System.err.println("\tUsing randomly selected alternative: "+newChar);
            mapping = new GlyphMapping(originalChar, glyphNo, newChar, false);
            newCmap.put(key, mapping);
            mappedValues.add(newChar);
            return newChar;

        }
        //
        return originalChar;
    }

    public FontMapper getFontMapper(final PdfFont currentFontData) {

            final String name = currentFontData.getFontName();

            final boolean fontIsEmbedded = currentFontData.isFontEmbedded && !currentFontData.isFontSubstituted() && embeddedFonts.contains(currentFontData.getBaseFontName()) ;

            return new GenericFontMapper(name, fontMode, fontIsEmbedded, currentFontData.isFontSubstituted());

    }

    public ArrayList<String> getFontFaces() {
        return fontFaces;
    }

    public String getGlyf(String glyf, final PdfFont currentFontData, final PdfGlyph embeddedGlyph, final boolean isRealText) {
        /**
         * type 3 are a special case and need to be rendered as images in all modes
         * So we will need to detect and render here for both
         */
        //        if(currentFontData.getFontType()==StandardFonts.TYPE3){
        //
        //        }

       /*  //Temporary hack to run ligatures through the new cmap code
         if (glyf.codePointCount(0, glyf.length()) > 1 && glyf.charAt(0) != '&') {
             glyf = remapGlyph(embeddedGlyph.getGlyphNumber(), glyf, currentFontData.getBaseFontName());
         }*/

        //Remap the glyphs to avoid conflicts
        if (!currentFontData.isFontSubstituted() &&
                embeddedGlyph != null && glyf != null &&
                currentFontData.getFontType() != StandardFonts.TYPE3 &&
                (currentFontData.getFontType() != StandardFonts.TYPE1 || useT1Mapping) &&
                (fontMode == FontMapper.EMBED_ALL ||
                        (fontMode == FontMapper.EMBED_ALL_EXCEPT_BASE_FAMILIES &&
                                !StandardFonts.isStandardFont(currentFontData.getFontName(),true) &&
                                !currentFontData.getFontName().contains("Arial")))) {

            glyf = remapGlyph(embeddedGlyph.getGlyphNumber(), glyf, currentFontData.getBaseFontName(), isRealText && !preserveExtractionValues);

        } else if (glyf.length() > 1 && glyf.endsWith(" ")) {
            //Strip out extra whitespace
            String newGlyf = glyf.substring(0,1);
            glyf = glyf.substring(1).trim();
            newGlyf += glyf;
            glyf = newGlyf;
        }

        //Temporary fix for extraction values under 0x20 (control characters)
        if (!glyf.isEmpty() && (glyf.charAt(0) < 0x20 || glyf.startsWith("&#"))) {
            glyf = " ";
        }

        //trap any non-standard glyfs
        if(OutputDisplay.Helper != null && glyf.length() > 3 && !StandardFonts.isValidGlyphName(glyf)){
            glyf = OutputDisplay.Helper.mapNonstandardGlyfName(glyf,currentFontData);
        }

        return glyf;
    }


    public HashSet<String> getUsedFonts() {
        return usedFonts; // TODO (Leon) This method needs to go
    }
    public HashSet<String> getEmbeddedFonts() {
        return embeddedFonts; // TODO (Leon) This method needs to go
    }


    /**
     * Helper class to store data on base64 encoded font data
     */
    @SuppressWarnings("InnerClassMayBeStatic")
    private class Base64EncodedFont {
        private final String fontName;
        private final EnumMap<ConversionOptions.Font, String> fontTypeToEncoded = new EnumMap<ConversionOptions.Font, String>(ConversionOptions.Font.class);

        Base64EncodedFont(final String fontName) {
            this.fontName = fontName;
        }

        public void putFontDats(final ConversionOptions.Font fontType, final String base64Data) {
            fontTypeToEncoded.put(fontType, base64Data);
        }

        public String getBase64Data(final ConversionOptions.Font fontType) {
            return fontTypeToEncoded.get(fontType);
        }

        public String getFontName() {
            return fontName;
        }
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    private class FontToConvert {
        private final PdfFont pdfFont;
        private final byte[] rawFontData;
        private final String fileType;

        private FontToConvert(final PdfFont pdfFont, final byte[] rawFontData, final String fileType) {
            this.rawFontData = rawFontData;
            this.pdfFont = pdfFont;
            this.fileType = fileType;
        }

        private String getFileType() {
            return fileType;
        }

        private PdfFont getPdfFont() {
            return pdfFont;
        }

        private byte[] getRawFontData() {
            return rawFontData;
        }
    }
}
