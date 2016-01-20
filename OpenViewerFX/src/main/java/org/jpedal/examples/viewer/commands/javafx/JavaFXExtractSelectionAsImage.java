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
 * JavaFXExtractSelectionAsImage.java
 * ---------------
 */

package org.jpedal.examples.viewer.commands.javafx;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.handlers.DefaultImageHelper;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.commands.generic.GUIExtractSelectionAsImage;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.javafx.dialog.FXDialog;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/**
 * This class is a JavaFX specific class to hold the JavaFX code for
 * Extracting the drawn CursorBox as an Image.
 */
public class JavaFXExtractSelectionAsImage extends GUIExtractSelectionAsImage {
    
    protected static final int BUTTONWIDTH = 55;
    
    public static void execute(final Values commonValues, final GUIFactory currentGUI, final PdfDecoderInt decode_pdf) {
        extractSelectedScreenAsImage(commonValues,currentGUI,decode_pdf); //Calls the generic code.

        
        final VBox pane = new VBox();
        final FXDialog dialog = new FXDialog((Stage)currentGUI.getFrame(), Modality.APPLICATION_MODAL, pane);
        dialog.setTitle(Messages.getMessage("PdfViewerMessage.SaveImage"));
        dialog.setResizeable(false);
        String style;
        
        //wrap image so we can display
        if (snapShot != null) {

            final HBox imgBox = new HBox();
            imgBox.setAlignment(Pos.CENTER);

            String propValue = currentGUI.getProperties().getValue("replacePdfDisplayBackground");
            if (!propValue.isEmpty()
                    && propValue.equalsIgnoreCase("true")) {
                //decode_pdf.useNewGraphicsMode = false;
                propValue = currentGUI.getProperties().getValue("pdfDisplayBackground");

                int col = Integer.parseInt(propValue);
                final int r = ((col >> 16) & 255);
                final int g = ((col >> 8) & 255);
                final int b = ((col) & 255);
                style = "-fx-background-color:rgb(" + r + ',' + g + ',' + b + ");";

            } else {

                if (decode_pdf.getDecoderOptions().getDisplayBackgroundColor() != null) {
                    int col = decode_pdf.getDecoderOptions().getDisplayBackgroundColor().getRGB();
                    final int r = ((col >> 16) & 255);
                    final int g = ((col >> 8) & 255);
                    final int b = ((col) & 255);
                    style = "-fx-background-color:rgb(" + r + ',' + g + ',' + b + ");";
                } else if (decode_pdf.useNewGraphicsMode()) {
                    style = "-fx-background-color:#555565;";
                } else {
                    style = "-fx-background-color:#190190190;";
                }
            }

            //IconiseImage icon_image = new IconiseImage( snapShot );
            final ImageView imv1 = new ImageView();
            imv1.setImage(SwingFXUtils.toFXImage(snapShot, null));
            dialog.setWidth(imv1.getImage().getWidth());
            dialog.setHeight(imv1.getImage().getHeight() + 50);
            //add image to pane if there is one
            imgBox.getChildren().add(imv1);
            imgBox.setPadding(new Insets(5));
            imgBox.setStyle(style);
            pane.getChildren().add(imgBox);
        } else {
            return;
        }
        
        final HBox btnBox = new HBox();
        final Button copyBtn = new Button(Messages.getMessage("PdfSnapshotPreview.Copy"));
        final Button saveBtn = new Button(Messages.getMessage("PdfSnapshotPreview.Save"));
        final Button cancelBtn = new Button(Messages.getMessage("PdfSnapshotPreview.Cancel"));
        btnBox.getChildren().addAll(copyBtn, saveBtn, cancelBtn);
        btnBox.setAlignment(Pos.BOTTOM_CENTER);
        btnBox.setSpacing(5);
        btnBox.setPadding(new Insets(5));
        
        //Set colors for display
        if(style!=null){
            btnBox.setStyle(style);
        }
        
        //Prevent button resize from hiding text
        copyBtn.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        saveBtn.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        cancelBtn.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        
        pane.getChildren().add(btnBox);
        
        //Ensure window is never so small as to hide buttons
        dialog.getDialog().sizeToScene();
        
        copyBtn.setPrefWidth(BUTTONWIDTH);
        copyBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(final ActionEvent t) {
                
            dialog.getDialog().hide();

            ClipboardImage clipboardImage = new ClipboardImage(snapShot);
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            c.setContents(clipboardImage, null);
            }
        });
        
        saveBtn.setPrefWidth(BUTTONWIDTH);
        saveBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent t) {

                dialog.getDialog().hide();

                final FileChooser chooser = new FileChooser();
                chooser.setTitle("Open PDF file");
                chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                final FileChooser.ExtensionFilter extFilter1 = new FileChooser.ExtensionFilter("TIFF (*.tif)", "*.tif", "*.tiff");
                chooser.getExtensionFilters().add(extFilter1);
                final FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("JPEG (*.jpg)", "*.jpg", "*.jpeg");
                chooser.getExtensionFilters().add(extFilter2);

                File outputFile = chooser.showSaveDialog(dialog.getDialog());
                
                    if (outputFile != null) {
                        StringBuilder outName = new StringBuilder(outputFile.getAbsolutePath());

                        FileChooser.ExtensionFilter filter = chooser.getSelectedExtensionFilter();

                        String format = "tif";
                        if (filter.getDescription().toLowerCase().contains("jp")) {
                            format = "jpg";
                        }

                        if (!outName.toString().toLowerCase().endsWith(('.' + format).toLowerCase())) {
                            outName.append('.').append(format);
                        }

                        //Do the actual save
                        if (snapShot != null) {

                            try {
                                DefaultImageHelper.write(snapShot, format, outName.toString());
                            } catch (IOException ex) {
                                LogWriter.writeLog("Exception in writing image " + ex);
                            }
                        }
                    }
            }
        });
        
        cancelBtn.setPrefWidth(BUTTONWIDTH);
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent t) {
                dialog.close();
            }
        });
        
        dialog.show();
        
        if(GUI.debugFX){
            System.out.println("Save Dialog required for JavaFXExtractSelectionAsImage.java");
        }
    }
}
