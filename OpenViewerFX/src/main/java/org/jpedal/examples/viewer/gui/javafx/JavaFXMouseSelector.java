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
 * JavaFXMouseSelector.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.javafx;

import java.io.File;
import java.util.Date;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.effect.Light.Point;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jpedal.PdfDecoderFX;
import org.jpedal.display.Display;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.commands.generic.GUICopy;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.JavaFxGUI;
import org.jpedal.examples.viewer.gui.MouseSelector;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXOptionDialog;
import org.jpedal.exception.PdfException;
import org.jpedal.external.Options;
import org.jpedal.grouping.SearchType;
import org.jpedal.gui.GUIFactory;
import org.jpedal.io.Speech;
import org.jpedal.objects.PdfPageData;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.text.TextLines;
import org.jpedal.utils.Messages;

public class JavaFXMouseSelector extends MouseSelector implements JavaFXMouseFunctionality {

    private final PdfDecoderFX decode_pdf;
    private final GUIFactory currentGUI;
    private final Values commonValues;
    private final Commands currentCommands;
    private PdfPageData page_data;

    //Experimental multi page highlight flag
    public static final boolean activateMultipageHighlight = true;

    //Variables to keep track of multiple clicks
    private int clickCount;
    private long lastTime = -1;

    //Page currently under the mouse
    private int pageMouseIsOver = -1;

    //Page currently being highlighted
    private int pageOfHighlight = -1;

    //Find current highlighted page
    private boolean startHighlighting;

    /*
     * ID of objects found during selection
     */
    public int id = -1;
    public int lastId = -1;

    //used to track changes when dragging rectangle around
    private int old_m_x2 = -1, old_m_y2 = -1;

    //Use alt to extract only within exact area
    boolean altIsDown;

    //Right click options
    MenuItem copy;
    //======================================
    MenuItem selectAll, deselectall;
    //======================================
    Menu extract;
    MenuItem extractText, extractImage;
    Image snapshotIcon;
    MenuItem snapShot;
    //======================================
    MenuItem find;
    //======================================
    MenuItem speakHighlighted;

    public JavaFXMouseSelector(final PdfDecoderFX decode_pdf, final GUIFactory currentGUI,
            final Values commonValues, final Commands currentCommands) {

        this.decode_pdf = decode_pdf;
        this.currentGUI = currentGUI;
        this.commonValues = commonValues;
        this.currentCommands = currentCommands;
        this.page_data = decode_pdf.getPdfPageData();

    }
    ContextMenu cm = null;
    private ContextMenu createRightClickMenu() {
        
        /**
         * Setup Items and Menus.
         */
        cm = new ContextMenu();
        copy = new MenuItem(Messages.getMessage("PdfRightClick.copy"));
        selectAll = new MenuItem(Messages.getMessage("PdfRightClick.selectAll"));
        deselectall = new MenuItem(Messages.getMessage("PdfRightClick.deselectAll"));
        extract = new Menu(Messages.getMessage("PdfRightClick.extract"));
        extractText = new MenuItem(Messages.getMessage("PdfRightClick.extractText"));
        extractImage = new MenuItem(Messages.getMessage("PdfRightClick.extractImage"));
        snapshotIcon = new Image("/org/jpedal/examples/viewer/res/snapshot_menu.gif");
        snapShot = new MenuItem(Messages.getMessage("PdfRightClick.snapshot"), new ImageView(snapshotIcon));
        if(GUI.debugFX){
            extract.getItems().addAll(extractText, extractImage, snapShot);
        }else{
            extract.getItems().addAll(extractText, extractImage);
        }
        find = new MenuItem(Messages.getMessage("PdfRightClick.find"));
        speakHighlighted = new MenuItem("Speak Highlighted text");

        /**
         * Setup Menu Item Listeners.
         */
        copy.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE) {
                    currentCommands.executeCommand(Commands.COPY, null);
                } else if (GUI.showMessages) {
                    currentGUI.showMessageDialog("Copy is only avalible in single page display mode");
                }
            }
        });
        selectAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                currentCommands.executeCommand(Commands.SELECTALL, null);
            }
        });
        deselectall.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                currentCommands.executeCommand(Commands.DESELECTALL, null);
            }
        });
        extractText.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE) {
                    currentCommands.executeCommand(Commands.EXTRACTTEXT, null);
                } else {
                    if (GUI.showMessages) {
                        currentGUI.showMessageDialog("Text Extraction is only avalible in single page display mode");
                    }
                }
            }
        });
        extractImage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                if ((decode_pdf.getPages().getHighlightedImage() != null) &&
                     (decode_pdf.getDisplayView() == 1)) {
                        final FileChooser jf = new FileChooser();
                        final FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG (*jpg)", "*.jpg");
                        final FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG (*PNG)", "*.png");
                        final FileChooser.ExtensionFilter extFilterTIFF = new FileChooser.ExtensionFilter("Tiff (*TIFF)", "*.tiff");
                        jf.getExtensionFilters().addAll(extFilterJPG, extFilterPNG, extFilterTIFF);
                       
                        final File file = jf.showSaveDialog((Stage)currentGUI.getFrame());
                        String fileExt = file.getName();
                        fileExt = fileExt.substring(fileExt.indexOf('.')+1, fileExt.length());
                        decode_pdf.getDynamicRenderer().saveImage(id, file.getAbsolutePath(), fileExt);
                  
                    }
                }
            
        });
        snapShot.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                currentCommands.executeCommand(Commands.SNAPSHOT, null);
            }
        });
        find.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                /**
                 * ensure co-ords in right order
                 */
                final int[] coords = decode_pdf.getPages().getCursorBoxOnScreenAsArray();
                if (coords == null) {
                    if (GUI.showMessages) {
                        currentGUI.showMessageDialog("There is no text selected.\nPlease highlight the text you wish to search.", "No Text selected", FXOptionDialog.ERROR_MESSAGE);
                    }
                    return;
                }

                String textToFind = currentGUI.showInputDialog(Messages.getMessage("PdfViewerMessage.GetUserInput"));

                //if cancel return to menu.
                if (textToFind == null || textToFind.length() < 1) {
                    //textToFind=null;
                    return;
                }

                //Modify coords to ensure fully encompassing highlighted text
                final int yMod = 2;
                //int xMod = 2;

                int t_x1 = coords[0];
                int t_x2 = coords[0] + coords[2];
                int t_y1 = coords[1] - yMod;
                int t_y2 = coords[1] + coords[3] + yMod;

                if (t_y1 < t_y2) {
                    final int temp = t_y2;
                    t_y2 = t_y1;
                    t_y1 = temp;
                }

                if (t_x1 > t_x2) {
                    final int temp = t_x2;
                    t_x2 = t_x1;
                    t_x1 = temp;
                }

                page_data = decode_pdf.getPdfPageData();
                final int cropX = page_data.getCropBoxX(commonValues.getCurrentPage());
                final int cropY = page_data.getCropBoxY(commonValues.getCurrentPage());
                final int mediaW = page_data.getMediaBoxWidth(commonValues.getCurrentPage());
                final int mediaH = page_data.getMediaBoxHeight(commonValues.getCurrentPage());

                if (t_x1 < cropX) {
                    t_x1 = cropX;
                }
                if (t_x1 > mediaW - cropX) {
                    t_x1 = mediaW - cropX;
                }

                if (t_x2 < cropX) {
                    t_x2 = cropX;
                }
                if (t_x2 > mediaW - cropX) {
                    t_x2 = mediaW - cropX;
                }

                if (t_y1 < cropY) {
                    t_y1 = cropY;
                }
                if (t_y1 > mediaH - cropY) {
                    t_y1 = mediaH - cropY;
                }

                if (t_y2 < cropY) {
                    t_y2 = cropY;
                }
                if (t_y2 > mediaH - cropY) {
                    t_y2 = mediaH - cropY;
                }

                //<start-demo>
                /**
                  <end-demo>
                if(GUI.showMessages)
                    currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.FindDemo"));
                textToFind=null;
                /**/
                int searchType = SearchType.DEFAULT;

                final int caseSensitiveOption = currentGUI.showConfirmDialog(Messages.getMessage("PdfViewercase.message"),
                        null, FXOptionDialog.YES_NO_OPTION);

                if (caseSensitiveOption == FXOptionDialog.YES_OPTION) {
                    searchType |= SearchType.CASE_SENSITIVE;
                }

                final int findAllOption = currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerfindAll.message"),
                        null, FXOptionDialog.YES_NO_OPTION);

                if (findAllOption == FXOptionDialog.NO_OPTION) {
                    searchType |= SearchType.FIND_FIRST_OCCURANCE_ONLY;
                }

                final int hyphenOption = currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerfindHyphen.message"),
                        null, FXOptionDialog.YES_NO_OPTION);

                if (hyphenOption == FXOptionDialog.YES_OPTION) {
                    searchType |= SearchType.MUTLI_LINE_RESULTS;
                }

                if (textToFind != null) {
                    try {
                        final float[] co_ords;

                        co_ords = decode_pdf.getGroupingObject().findText(t_x1, t_y1, t_x2, t_y2, new String[]{textToFind}, searchType);

                        if (co_ords.length >= 2) {
                            if (co_ords.length < 3) {
                                currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.Found") + ' ' + co_ords[0] + ',' + co_ords[1]);
                            } else {
                                final StringBuilder displayCoords = new StringBuilder();
                                String coordsMessage = Messages.getMessage("PdfViewerMessage.FoundAt");
                                for (int i = 0; i < co_ords.length; i += 5) {
                                    displayCoords.append(coordsMessage).append(' ');
                                    displayCoords.append(co_ords[i]);
                                    displayCoords.append(',');
                                    displayCoords.append(co_ords[i + 1]);

                                    displayCoords.append('\n');
                                    if (co_ords[i + 4] == -101) {
                                        coordsMessage = Messages.getMessage("PdfViewerMessage.FoundAtHyphen");
                                    } else {
                                        coordsMessage = Messages.getMessage("PdfViewerMessage.FoundAt");
                                    }

                                }
                                currentGUI.showMessageDialog(displayCoords.toString());
                            }
                        } else {
                            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NotFound"));
                        }

                    } catch (final PdfException e1) {
                        e1.printStackTrace();
                    }

                }
            }
        });

        final Speech speech = (Speech)decode_pdf.getExternalHandler(Options.SpeechEngine);
        final boolean useSpeech = speech!=null;
        if (useSpeech) {
            //option = option.substring(0, option.indexOf('('));
            speech.setVoice(currentGUI.getProperties().getValue("voice"));
        }
            speakHighlighted.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent t) {
                    if (useSpeech) {
                        if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE) {
                            final Thread speak = new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    final String text = GUICopy.copySelectedText(decode_pdf, currentGUI, commonValues);
                                    speech.speakText(text);
                                }
                            });
                            speak.setDaemon(true);
                            speak.start();
                        } else {
                            if (GUI.showMessages) {
                                currentGUI.showMessageDialog("Speak text is only avalible in single page display mode");
                            }
                        }
                    }
                }
            });
        
        
        /**
         * Add Items to Main Menu.
         */
        if (decode_pdf != null && decode_pdf.isOpen()) {
            if(GUI.debugFX){
                cm.getItems().add(copy);
                cm.getItems().addAll(new SeparatorMenuItem(), selectAll, deselectall);
                cm.getItems().addAll(new SeparatorMenuItem(),extract);
                cm.getItems().addAll(new SeparatorMenuItem(),find);
                cm.getItems().addAll(new SeparatorMenuItem(),speakHighlighted);
            }else{
                cm.getItems().add(copy);
                cm.getItems().addAll(new SeparatorMenuItem(), selectAll, deselectall);
                cm.getItems().addAll(new SeparatorMenuItem(),extract);
                //cm.getItems().addAll(new SeparatorMenuItem(),find);
                //cm.getItems().addAll(new SeparatorMenuItem(),speakHighlighted);
            }
        }
        return cm;
    }
    
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE || (activateMultipageHighlight && decode_pdf.getDisplayView() == Display.CONTINUOUS && decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING)) {
            final long currentTime = new Date().getTime();

            if (lastTime + 500 < currentTime) {
                clickCount = 0;
            }

            lastTime = currentTime;

            if (isOtherKey(e)) {
                //Single mode actions
                if (clickCount != 4) {
                    clickCount++;
                }

                //Point mousePoint = getCoordsOnPage(e.getX(), e.getY(), commonValues.getCurrentPage());
                final int pagenumber = decode_pdf.getPageNumber();
                final int crx = page_data.getCropBoxX(pagenumber);
                final int cry = page_data.getCropBoxY(pagenumber);
                commonValues.m_x1 = (int)e.getX()+crx;
                commonValues.m_y1 = (int)e.getY()+cry;

                if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE) {
                    id = decode_pdf.getDynamicRenderer().isInsideImage(commonValues.m_x1, commonValues.m_y1);
                } else {
                    id = -1;
                } 

                if (lastId != id && id != -1) {
                    final int[] imageArea = decode_pdf.getDynamicRenderer().getAreaAsArray(id);

                    if (imageArea != null) {
                        int h = imageArea[3];
                        int w = imageArea[2];

                        int x = imageArea[0]; 
                        int y = imageArea[1];
                        decode_pdf.getDynamicRenderer().setneedsHorizontalInvert(false);
                        decode_pdf.getDynamicRenderer().setneedsVerticalInvert(false);
                        //						Check for negative values
                        if (w < 0) {
                            decode_pdf.getDynamicRenderer().setneedsHorizontalInvert(true);
                            w = -w;
                            x -= w;
                        }
                        if (h < 0) {
                            decode_pdf.getDynamicRenderer().setneedsVerticalInvert(true);
                            h = -h;
                            y -= h;
                        }
                        
                        decode_pdf.getPages().setHighlightedImage(new int[]{x, y, w, h});
                    }
                    lastId = id;
                } else {
                    decode_pdf.getPages().setHighlightedImage(null);
                    lastId = -1;
                }
                
                if ((id == -1) &&  (clickCount > 1)) {
                        switch (clickCount) {
                            case 1: //single click adds caret to page
                                /**
                                 * Does nothing yet. IF above prevents this case
                                 * from ever happening Add Caret code here and
                                 * add shift click code for selection. Also
                                 * remember to comment out "if(clickCount>1)"
                                 * from around this switch to activate
                                 */
                                break;
                            case 2: //double click selects line
                                final int[][] lineAreas = decode_pdf.getTextLines().getLineAreasAs2DArray(commonValues.getCurrentPage());

                                if (lineAreas != null) { //Null is page has no lines
                                    final int[] point = {commonValues.m_x1, commonValues.m_y1, 1, 1};
                                    for (int i = 0; i != lineAreas.length; i++) {
                                        if (TextLines.intersects(point, lineAreas[i])) {
                                            decode_pdf.updateCursorBoxOnScreen(lineAreas[i], DecoderOptions.highlightColor.getRGB());
                                            decode_pdf.getTextLines().addHighlights(new int[][]{lineAreas[i]}, false, commonValues.getCurrentPage());
                                        }
                                    }
                                }
                                break;
                            case 3: //triple click selects paragraph
                                final int[] para = decode_pdf.getTextLines().setFoundParagraphAsArray(commonValues.m_x1, commonValues.m_y1, commonValues.getCurrentPage());
                                if (para != null) {
                                    decode_pdf.updateCursorBoxOnScreen(para, DecoderOptions.highlightColor.getRGB());
                                }
                                break;
                            case 4: //quad click selects page
                                currentCommands.executeCommand(Commands.SELECTALL, null);
                                break;
                        }
                    }
                
                decode_pdf.repaintPane(commonValues.getCurrentPage());
            }
        }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        //Stub
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        //Stub
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        if ((decode_pdf.getDisplayView() == Display.SINGLE_PAGE || (activateMultipageHighlight && decode_pdf.getDisplayView() == Display.CONTINUOUS && decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING)) 
            && (isOtherKey(e))) {
                /**
                 * remove any outline and reset variables used to track change
                 */
                decode_pdf.updateCursorBoxOnScreen(null, 0); //remove box
                decode_pdf.getPages().setHighlightedImage(null);// remove image highlight
                decode_pdf.getTextLines().clearHighlights();

                //Remove focus from form is if anywhere on pdf panel is clicked / mouse dragged
                decode_pdf.requestFocus();

                //Point values = getCoordsOnPage(e.getX(), e.getY(), commonValues.getCurrentPage());
                final int pagenumber = decode_pdf.getPageNumber();
                final int crx = page_data.getCropBoxX(pagenumber);
                final int cry = page_data.getCropBoxY(pagenumber);
                commonValues.m_x1 = (int)e.getX()+crx;
                commonValues.m_y1 = (int)e.getY()+cry;

                final int[][] rectParams = decode_pdf.getTextLines().getHighlightedAreasAs2DArray(commonValues.getCurrentPage());
                if (rectParams != null && rectParams.length > 0) {
                    decode_pdf.getPages().refreshDisplay();
                }
            }
        }
    

    @Override
    public void mouseReleased(final MouseEvent e) {
        if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE || (activateMultipageHighlight && decode_pdf.getDisplayView() == Display.CONTINUOUS && decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING)) {
            
            if(cm!=null && cm.isShowing()){
                cm.hide();
            }
            
            if (isOtherKey(e)) {

                //If we have been highlighting, stop now and reset all flags
                if (startHighlighting) {
                    decode_pdf.setCursor(Cursor.DEFAULT);
                    startHighlighting = false;
                }

                this.page_data = decode_pdf.getPdfPageData();

                if (currentCommands.extractingAsImage) {

                    /**
                     * remove any outline and reset variables used to track
                     * change
                     */
                    decode_pdf.updateCursorBoxOnScreen(null, 0); //remove box
                    decode_pdf.getTextLines().clearHighlights(); //remove highlighted text
                    decode_pdf.getPages().setHighlightedImage(null);// remove image highlight

                    decode_pdf.setCursor(Cursor.DEFAULT);

                    currentCommands.executeCommand(Commands.EXTRACTASIMAGE, null);
                    currentCommands.extractingAsImage = false;
                    DecoderOptions.showMouseBox = false;

                }

                //Ensure this is reset to -1 regardless
                pageOfHighlight = -1;

            } else if ((e.getButton().equals(MouseButton.SECONDARY)) &&
                 (currentGUI.getProperties().getValue("allowRightClick").toLowerCase().equals("true"))) {
                    if(cm==null){
                        createRightClickMenu();
                    }
                    
                    cm.show(((JavaFxGUI)currentGUI).getRoot(),e.getScreenX(), e.getScreenY());

                    if (decode_pdf.getPages().getHighlightedImage() == null) {
                        extractImage.setDisable(true);
                    } else {
                        extractImage.setDisable(false);
                    }

                    if (decode_pdf.getTextLines().getHighlightedAreasAs2DArray(commonValues.getCurrentPage()) == null) {
                        extractText.setDisable(true);
                        find.setDisable(true);
                        speakHighlighted.setDisable(true);
                        copy.setDisable(true);
                    } else {
                        extractText.setDisable(false);
                        find.setDisable(false);
                        speakHighlighted.setDisable(false);
                        copy.setDisable(false);
                    }

                }
            }
        }
    

    @Override
    public void mouseDragged(final MouseEvent e) {
        if (isOtherKey(e)) {

            altIsDown = e.isAltDown();
            if (!startHighlighting) {
                startHighlighting = true;
            }

            //Point values = getCoordsOnPage(e.getX(), e.getY(), commonValues.getCurrentPage());
            if (pageMouseIsOver == pageOfHighlight) {
                // Adjust to center of decode_pdf
                final int pagenumber = decode_pdf.getPageNumber();
                final int crx = decode_pdf.getPdfPageData().getCropBoxX(pagenumber);
                final int cry = decode_pdf.getPdfPageData().getCropBoxY(pagenumber);
                commonValues.m_x2 = (int) e.getX() + crx;
                commonValues.m_y2 = (int) e.getY() + cry;
            }

            if (commonValues.isPDF()) {
                decode_pdf.setCursor(Cursor.TEXT);
                generateNewCursorBox();
            }

        }
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        //Stub
    }
    
    /**
     * Find and updates coords for the current page
     * @param x :: The x coordinate of the cursors location in display area coordinates
     * @param y :: The y coordinate of the cursors location in display area coordinates
     * @param page :: The page we are currently on
     * @return Point object of the cursor location in page coordinates
     */
    @SuppressWarnings("MethodMayBeStatic")
    public Point getCoordsOnPage(final double x, final double y, final int page){
        
        //Update cursor position if over page
        throw new UnsupportedOperationException("We do not currently use this method for JavaFX as it appears the X and Y coords\n" +
                                                    " can be retrieved from the bottom left in JavaFX compared to the top left in Swing");
        
//        Point pagePosition;
//        switch(decode_pdf.getDisplayView()){
//            case Display.SINGLE_PAGE:
//                pagePosition = getPageCoordsInSingleDisplayMode(x, y, page);
//                x = pagePosition.getX();
//                y = pagePosition.getY();
//                break;
//            case Display.CONTINUOUS:
//                pagePosition = getPageCoordsInContinuousDisplayMode(x, y, page);
//                x = pagePosition.getX();
//                y = pagePosition.getY();
//                break;
//                
//            case Display.FACING:
//                pagePosition = getPageCoordsInFacingDisplayMode(x, y);
//                x = pagePosition.getX();
//                y = pagePosition.getY();
//                break;
//                
//            case Display.CONTINUOUS_FACING:
//                pagePosition = getPageCoordsInContinuousFacingDisplayMode(x, y, page);
//                x = pagePosition.getX();
//                y = pagePosition.getY();
//                break;
//            default : break;
//        }
//        
//        Point point = new Point();
//        point.setX(x);
//        point.setY(y);
//        return point;
    }
    
    @SuppressWarnings("unused")
    private Point getPageCoordsInContinuousDisplayMode(double x, double y, int page){
        
        final Display pages= decode_pdf.getPages();
        
       
            final int[] flag = new int[2];
            
            flag[0] = GUI.CURSOR;
            flag[1]=0;
            
            //In continuous pages are centred so we need make
            double xAdjustment = (decode_pdf.getWidth()/2) - (decode_pdf.getPdfPageData().getScaledCropBoxWidth(page)/2);
            if(xAdjustment<0) {
                xAdjustment = 0;
            } else{
                //This adjustment is the correct position.
                //Offset removed to that when used later we get either offset unaltered or correct position
                xAdjustment -= pages.getXCordForPage(page);
            }
            Rectangle pageArea = new Rectangle(pages.getXCordForPage(page)+xAdjustment,
                    pages.getYCordForPage(page),
                    decode_pdf.getPdfPageData().getScaledCropBoxWidth(page),
                    decode_pdf.getPdfPageData().getScaledCropBoxHeight(page));
            if(pageArea.contains(x,y)){
                //set displayed
                flag[1] = 1;
            }
            
            
            
            if(flag[1]==0){
                if(y<pageArea.getY() && page>1){
                    while(flag[1]==0 && page>1){
                        page--;
                        pageArea = new Rectangle(pages.getXCordForPage(page)+xAdjustment,
                                pages.getYCordForPage(page),
                                decode_pdf.getPdfPageData().getScaledCropBoxWidth(page),
                                decode_pdf.getPdfPageData().getScaledCropBoxHeight(page));
                        if(pageArea.contains(x,y)){
                            //set displayed
                            flag[1] = 1;
                        }
                    }
                }else{
                    if(y>pageArea.getY()+pageArea.getHeight() && page<commonValues.getPageCount()){
                        while(flag[1]==0 && page<commonValues.getPageCount()){
                            page++;
                            pageArea = new Rectangle(pages.getXCordForPage(page)+xAdjustment,
                                    pages.getYCordForPage(page),
                                    decode_pdf.getPdfPageData().getScaledCropBoxWidth(page),
                                    decode_pdf.getPdfPageData().getScaledCropBoxHeight(page));
                            if(pageArea.contains(x,y)){
                                //set displayed
                                flag[1] = 1;
                            }
                        }
                    }
                }
            
            
            //Set highlighting page
            if(pageOfHighlight==-1 && startHighlighting){
                pageOfHighlight = page;
            }
            
            //Keep track of page mouse is over at all times
            pageMouseIsOver = page;
            
        }
            
        //Tidy coords for multipage views
        y= ((pages.getYCordForPage(page)+decode_pdf.getPdfPageData().getScaledCropBoxHeight(page))+decode_pdf.getInsetH())-y;
            
        currentGUI.setMultibox(flag);

        final float scaling=currentGUI.getScaling();
        final int inset= GUI.getPDFDisplayInset();
        final int rotation=currentGUI.getRotation();
        
        //Apply inset to values
        int ex=adjustForAlignment((int)x, decode_pdf)-inset;
        double ey=y-inset;
        
        this.page_data = decode_pdf.getPdfPageData();
        final int mediaH = page_data.getMediaBoxHeight(commonValues.getCurrentPage());
        
        //undo any viewport scaling
        if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
            ex=(int)(((ex-(commonValues.dx*scaling))/commonValues.viewportScale));
            ey=(int)((mediaH-((mediaH-(ey/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
        }
        
        //Apply page scale to value
        x=(int)((ex)/scaling);
        y=(int)((ey/scaling));
        
        
        final int cropX = page_data.getCropBoxX(commonValues.getCurrentPage());
        final int cropY = page_data.getCropBoxY(commonValues.getCurrentPage());
        final int cropW = page_data.getCropBoxWidth(commonValues.getCurrentPage());
        final int cropH = page_data.getCropBoxHeight(commonValues.getCurrentPage());
        //Apply rotation to values
        if(rotation==90){
            final double tmp=(x+cropY);
            x = (y+cropX);
            y =tmp;
        }else if((rotation==180)){
            x =(cropW+cropX)-x;
            y =(y+cropY);
        }else if((rotation==270)){
            final double tmp=(cropH+cropY)-x;
            x =(cropW+cropX)-y;
            y =tmp;
        }else{
            x = (x+cropX);
            if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE) {
                y = (cropH + cropY) - y;
            } else {
                y = (cropY) + y;
            }
        }
        
        final Point point = new Point();
        point.setX(x);
        point.setY(y);
        return point;
    }
    
    @SuppressWarnings("unused")
    private Point getPageCoordsInSingleDisplayMode(double x, double y, final int page){
        
            final int[] flag = new int[2];
            
            flag[0] = GUI.CURSOR;
            flag[1]=0;
            
            final int pageWidth;
        final int pageHeight;
        if (currentGUI.getRotation()%180==90) {
                pageWidth = decode_pdf.getPdfPageData().getScaledCropBoxHeight(page);
                pageHeight = decode_pdf.getPdfPageData().getScaledCropBoxWidth(page);
            } else {
                pageWidth = decode_pdf.getPdfPageData().getScaledCropBoxWidth(page);
                pageHeight = decode_pdf.getPdfPageData().getScaledCropBoxHeight(page);
            }
            
            final Rectangle pageArea = new Rectangle(
                    (decode_pdf.getBoundsInLocal().getWidth()/2) - (pageWidth/2),
                    decode_pdf.getInsetH(),
                    pageWidth,
                    pageHeight);
            
            if (pageArea.contains(x,y))
                //set displayed
            {
                flag[1] = 1;
            } else
                //set not displayed
            {
                flag[1] = 0;
            }
            
            //Set highlighting page
            if(pageOfHighlight==-1 && startHighlighting){
                pageOfHighlight = page;
            }
            
            //Keep track of page the mouse is over at all times
            pageMouseIsOver = page;
            
            currentGUI.setMultibox(flag);
        
        
        final float scaling=currentGUI.getScaling();
        final int inset= GUI.getPDFDisplayInset();
        final int rotation=currentGUI.getRotation();
        
        
        //Apply inset to values
        int ex=adjustForAlignment((int)x,decode_pdf)-inset;
        double ey=y-inset;
        
        this.page_data = decode_pdf.getPdfPageData();
        final int mediaH = page_data.getMediaBoxHeight(commonValues.getCurrentPage());
        
        //undo any viewport scaling
        if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
            ex=(int)(((ex-(commonValues.dx*scaling))/commonValues.viewportScale));
            ey=(int)((mediaH-((mediaH-(ey/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
        }
        
        //Apply page scale to value
        x=(int)((ex)/scaling);
        y=(int)((ey/scaling));
        
        final int cropX = page_data.getCropBoxX(commonValues.getCurrentPage());
        final int cropY = page_data.getCropBoxY(commonValues.getCurrentPage());
        final int cropW = page_data.getCropBoxWidth(commonValues.getCurrentPage());
        final int cropH = page_data.getCropBoxHeight(commonValues.getCurrentPage());
        
        //Apply rotation to values
        if(rotation==90){
            final double tmp=(x+cropY);
            x = (y+cropX);
            y =tmp;
        }else if((rotation==180)){
            x =(cropW+cropX)-x;
            y =(y+cropY);
        }else if((rotation==270)){
            final double tmp=(cropH+cropY)-x;
            x =(cropW+cropX)-y;
            y =tmp;
        }else{
            x = (x+cropX);
            if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE) {
                y = (cropH + cropY) - y;
            } else {
                y = (cropY) + y;
            }
        }
        
        final Point point = new Point();
        point.setX(x);
        point.setY(y);
        return point;
    }
    
    @SuppressWarnings("unused")
    private Point getPageCoordsInFacingDisplayMode(double x, double y){
        
        final int[] flag = new int[2];
        flag[0] = GUI.CURSOR;
        
            //get raw w and h
            final int rawW;
        final int rawH;
        if (currentGUI.getRotation()%180==90) {
                rawW = decode_pdf.getPdfPageData().getCropBoxHeight(1);
                rawH = decode_pdf.getPdfPageData().getCropBoxWidth(1);
            } else {
                rawW = decode_pdf.getPdfPageData().getCropBoxWidth(1);
                rawH = decode_pdf.getPdfPageData().getCropBoxHeight(1);
            }
            
            float scaling = decode_pdf.getScaling();
            
            final double pageHeight = scaling*rawH;
            final double pageWidth = scaling*rawW;
            final int yStart = decode_pdf.getInsetH();
            
            //move so relative to center
            double left = (decode_pdf.getWidth()/2) - (pageWidth/2);
            double right = (decode_pdf.getWidth()/2) + (pageWidth/2);
            
            if(decode_pdf.getDisplayView()==Display.FACING){
            	 left = (decode_pdf.getWidth()/2);
            	 if(decode_pdf.getPageNumber()!=1 || decode_pdf.getPageCount()==2) {
                     left -= (pageWidth);
                 }
            	 
                 right = (decode_pdf.getWidth()/2) + (pageWidth);
            }
            
            if (x >= left && x <= right &&
                    y >= yStart && y <= yStart + pageHeight)
                //set displayed
            {
                flag[1] = 1;
            } else
                //set not displayed
            {
                flag[1] = 0;
            }
        
        currentGUI.setMultibox(flag);
    
        scaling=currentGUI.getScaling();
        final int inset= GUI.getPDFDisplayInset();
        final int rotation=currentGUI.getRotation();
        
        
        //Apply inset to values
        int ex=adjustForAlignment((int)x,decode_pdf)-inset;
        double ey=y-inset;
        
        this.page_data = decode_pdf.getPdfPageData();
        final int mediaH = page_data.getMediaBoxHeight(commonValues.getCurrentPage());
        
        //undo any viewport scaling
        if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
            ex=(int)(((ex-(commonValues.dx*scaling))/commonValues.viewportScale));
            ey=(int)((mediaH-((mediaH-(ey/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
        }
        
        //Apply page scale to value
        x=(int)((ex)/scaling);
        y=(int)((ey/scaling));
        
        final int cropX = page_data.getCropBoxX(commonValues.getCurrentPage());
        final int cropY = page_data.getCropBoxY(commonValues.getCurrentPage());
        final int cropW = page_data.getCropBoxWidth(commonValues.getCurrentPage());
        final int cropH = page_data.getCropBoxHeight(commonValues.getCurrentPage());
        
        //Apply rotation to values
        if(rotation==90){
            final double tmp=(x+cropY);
            x = (y+cropX);
            y =tmp;
        }else if((rotation==180)){
            x =(cropW+cropX)-x;
            y =(y+cropY);
        }else if((rotation==270)){
            final double tmp=(cropH+cropY)-x;
            x =(cropW+cropX)-y;
            y =tmp;
        }else{
            x = (x+cropX);
            if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE) {
                y = (cropH + cropY) - y;
            } else {
                y = (cropY) + y;
            }
        }
        
        final Point point = new Point();
        point.setX(x);
        point.setY(y);
        return point;
    }
    
    @SuppressWarnings("unused")
    private Point getPageCoordsInContinuousFacingDisplayMode(double x, double y, int page){
        
        final Display pages= decode_pdf.getPages();
        

            final int[] flag = new int[2];
            
            flag[0] = GUI.CURSOR;
            flag[1]=0;
            
            //Check if we are in the region of the left or right pages
            if(page != 1 && x>(decode_pdf.getWidth()/2) && page<commonValues.getPageCount()){// && x>pageArea.x){
                page++;
            }
            
            //Set the adjustment for page position
            double xAdjustment = (decode_pdf.getWidth()/2) - (decode_pdf.getPdfPageData().getScaledCropBoxWidth(page))-(decode_pdf.getInsetW());
            
            //Unsure if this is needed. Still checking
            if(xAdjustment<0){
                System.err.println("x adjustment is less than 0");
                xAdjustment = 0;
            }
            
            //Check to see if pagearea contains the mouse
            Rectangle pageArea = new Rectangle(pages.getXCordForPage(page)+xAdjustment,
                    pages.getYCordForPage(page),
                    decode_pdf.getPdfPageData().getScaledCropBoxWidth(page),
                    decode_pdf.getPdfPageData().getScaledCropBoxHeight(page));
            if(pageArea.contains(x,y)){
                //set displayed
                flag[1] = 1;
            }
            
            
            //If neither of the two current pages contain the mouse start checking the other pages
            //Could be improved to minimise on the loops and calls to decode_pdf.getPageOffsets(page)
            if(flag[1]==0){
                if(y<pageArea.getY() && page>1){
                    while(flag[1]==0 && page>1){
                        page--;
                        xAdjustment = (decode_pdf.getWidth()/2) - (decode_pdf.getPdfPageData().getScaledCropBoxWidth(page))-(decode_pdf.getInsetW());
                        if(xAdjustment<0) {
                            xAdjustment = 0;
                        }
                        pageArea = new Rectangle(pages.getXCordForPage(page)+xAdjustment,
                                pages.getYCordForPage(page),
                                decode_pdf.getPdfPageData().getScaledCropBoxWidth(page),
                                decode_pdf.getPdfPageData().getScaledCropBoxHeight(page));
                        if(pageArea.contains(x,y)){
                            //set displayed
                            flag[1] = 1;
                        }
                        
                    }
                }else{
                    if(y>pageArea.getY()+pageArea.getHeight() && page<commonValues.getPageCount()){
                        while(flag[1]==0 && page<commonValues.getPageCount()){
                            page++;
                            xAdjustment = (decode_pdf.getWidth()/2) - (decode_pdf.getPdfPageData().getScaledCropBoxWidth(page))-(decode_pdf.getInsetW());
                            if(xAdjustment<0) {
                                xAdjustment = 0;
                            }
                            pageArea = new Rectangle(pages.getXCordForPage(page)+xAdjustment,
                                    pages.getYCordForPage(page),
                                    decode_pdf.getPdfPageData().getScaledCropBoxWidth(page),
                                    decode_pdf.getPdfPageData().getScaledCropBoxHeight(page));
                            if(pageArea.contains(x,y)){
                                //set displayed
                                flag[1] = 1;
                            }
                            
                        }
                    }
                }
            }
            
            //Set highlighting page
            if(pageOfHighlight==-1 && startHighlighting){
                pageOfHighlight = page;
            }
            
            //Keep track of page mouse is over at all times
            pageMouseIsOver = page;
            
            //Tidy coords for multipage views
            y= (((pages.getYCordForPage(page)+decode_pdf.getPdfPageData().getScaledCropBoxHeight(page))+decode_pdf.getInsetH()))-y;
            
            x -= ((pages.getXCordForPage(page))-decode_pdf.getInsetW());
            
            currentGUI.setMultibox(flag);
            
        
        
        final float scaling=currentGUI.getScaling();
        final int inset= GUI.getPDFDisplayInset();
        final int rotation=currentGUI.getRotation();
        
        
        //Apply inset to values
        int ex=adjustForAlignment((int)x,decode_pdf)-inset;
        double ey=y-inset;
        this.page_data = decode_pdf.getPdfPageData();
        final int cropW = page_data.getCropBoxWidth(commonValues.getCurrentPage());
        final int cropH = page_data.getCropBoxHeight(commonValues.getCurrentPage());
        
        //undo any viewport scaling
        if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
            ex=(int)(((ex-(commonValues.dx*scaling))/commonValues.viewportScale));
            ey=(int)((cropH-((cropH-(ey/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
        }
        
        //Apply page scale to value
        x=(int)((ex)/scaling);
        y=(int)((ey/scaling));
        
        final int cropX = page_data.getCropBoxX(commonValues.getCurrentPage());
        final int cropY = page_data.getCropBoxY(commonValues.getCurrentPage());
        
        //Apply rotation to values
        if(rotation==90){
            final double tmp=(x+cropY);
            x = (y+cropX);
            y =tmp;
        }else if((rotation==180)){
            x =(cropW+cropX)-x;
            y =(y+cropY);
        }else if((rotation==270)){
            final double tmp=(cropH+cropY)-x;
            x =(cropW+cropX)-y;
            y =tmp;
        }else{
            x = (x+cropX);
            if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE) {
                y = (cropH + cropY) - y;
            } else {
                y = (cropY) + y;
            }
        }
        final Point point = new Point();
        point.setX(x);
        point.setY(y);
        return point;
    }
    
    /**
     * Checks to see whether the primary mouse button or any other key that
     * is not the secondary mouse button or the middle mouse button is pressed,
     * if it is then return true, otherwise return false.
     * @param e
     * @return 
     */
    private static boolean isOtherKey(final MouseEvent e){

        return e.getButton().equals(MouseButton.PRIMARY) || e.getButton().equals(MouseButton.NONE);
    }
    
    Pane pane = new Pane();
    
    /**
     * generate new  cursorBox and highlight extractable text,
     * if hardware acceleration off and extraction on<br>
     * and update current cursor box displayed on screen
     */
    protected void generateNewCursorBox() {
        
        //redraw rectangle of dragged box onscreen if it has changed significantly
        if ((old_m_x2!=-1)||(old_m_y2!=-1)||(Math.abs(commonValues.m_x2-old_m_x2)>5)||(Math.abs(commonValues.m_y2-old_m_y2)>5)) {
            
            //allow for user to go up
            int top_x = commonValues.m_x1;
            if (commonValues.m_x1 > commonValues.m_x2) {
                top_x = commonValues.m_x2;
            }
            int top_y = commonValues.m_y1;
            if (commonValues.m_y1 > commonValues.m_y2) {
                top_y = commonValues.m_y2;
            }
            final int w = Math.abs(commonValues.m_x2 - commonValues.m_x1);
            final int h = Math.abs(commonValues.m_y2 - commonValues.m_y1);
            
            //add an outline rectangle  to the display
            final int[] currentRectangle={top_x,top_y,w,h};
            
            //tell JPedal to highlight text in this area (you can add other areas to array)
            decode_pdf.updateCursorBoxOnScreen(currentRectangle,DecoderOptions.highlightColor.getRGB());
            if (!currentCommands.extractingAsImage) {
                final int[] r = {commonValues.m_x1, commonValues.m_y1, commonValues.m_x2 - commonValues.m_x1, commonValues.m_y2 - commonValues.m_y1};

                decode_pdf.getTextLines().addHighlights(new int[][]{r}, false, commonValues.getCurrentPage());

            }
            //reset tracking
            old_m_x2=commonValues.m_x2;
            old_m_y2=commonValues.m_y2;
            
        }
        decode_pdf.repaintPane(commonValues.getCurrentPage());
    }
}
