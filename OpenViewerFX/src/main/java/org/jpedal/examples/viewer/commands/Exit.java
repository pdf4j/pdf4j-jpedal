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
 * Exit.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.*;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.gui.GUIFactory;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;
import org.jpedal.display.Display;
import org.jpedal.examples.viewer.utils.Printer;

/**
 * Clean up and exit programo
 */
public class Exit {

    public static void execute(final Object[] args, final GUIThumbnailPanel thumbnails, final GUIFactory currentGUI, final Values commonValues, final PdfDecoderInt decode_pdf, final PropertiesFile properties) {
        if (args == null) {
            if (Printer.isPrinting()) {
                currentGUI.showMessageDialog(Messages.getMessage("PdfViewerStillPrinting.text"));
            }else{
                exit(thumbnails, currentGUI, commonValues, decode_pdf, properties);
            }
        }
    }


    public static void exit(final GUIThumbnailPanel thumbnails, final GUIFactory currentGUI, final Values commonValues, final PdfDecoderInt decode_pdf, final PropertiesFile properties) {

        thumbnails.terminateDrawing();

        /**
         * warn user on forms
         */
        SaveForm.handleUnsaveForms(currentGUI, commonValues, decode_pdf);

        /**
         * create the dialog
         */
        if(LogWriter.isRunningFromIDE){
            final int choice;
            if (!org.jpedal.DevFlags.GUITESTINGINPROGRESS && currentGUI.confirmClose()) {
                choice = javax.swing.JOptionPane.showConfirmDialog(null, new javax.swing.JLabel(Messages.getMessage("PdfViewerExiting")),
                        Messages.getMessage("PdfViewerprogramExit"), javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
            } else {
                choice = javax.swing.JOptionPane.OK_OPTION;
            }

            if (choice == javax.swing.JOptionPane.CANCEL_OPTION) {
                return;
            }

            if(decode_pdf.getDisplayView()==Display.PAGEFLOW){
                decode_pdf.getPages().stopGeneratingPage();
            }
        }

        
        /**
         * cleanup
         */
        decode_pdf.closePdfFile();

        //needed to save recent files
        try {
            properties.setValue("lastDocumentPage", String.valueOf(commonValues.getCurrentPage()));
            if (properties.getValue("trackViewerSize").equalsIgnoreCase("true")) {
                properties.setValue("startViewerWidth", String.valueOf(((Container)currentGUI.getFrame()).getWidth()));
                properties.setValue("startViewerHeight", String.valueOf(((Container)currentGUI.getFrame()).getHeight()));
            }

            if (properties.getValue("trackScaling").equalsIgnoreCase("true")) {
                properties.setValue("startScaling", String.valueOf(((GUI) currentGUI).getSelectedComboItem(Commands.SCALING)));
            }

            if (properties.getValue("trackView").equalsIgnoreCase("true")) {
                properties.setValue("startView", String.valueOf(decode_pdf.getDisplayView()));
            }

            if (properties.getValue("startSideTabOpen").equalsIgnoreCase("true")) {
                properties.setValue("startSideTabOpen", "true");
            }

            if (properties.getValue("trackSelectedSideTab").equalsIgnoreCase("true")) {
                final JTabbedPane tabs = (JTabbedPane)currentGUI.getSideTabBar();
                if (DecoderOptions.isRunningOnMac) {
                    properties.setValue("startSelectedSideTab", tabs.getTitleAt(tabs.getSelectedIndex()));
                } else {
                    properties.setValue("startSelectedSideTab", tabs.getIconAt(tabs.getSelectedIndex()).toString());
                }
            }

            if (properties.getValue("trackSideTabExpandedSize").equalsIgnoreCase("true")) {
                properties.setValue("sideTabBarExpandLength", String.valueOf(currentGUI.getSplitDividerLocation()));
            }

            properties.writeDoc();
        } catch (final Exception e1) {
            LogWriter.writeLog("Exception attempting to Write proterties: " + e1);
        }

        //formClickTest needs this so that it does not exit after first test.
        if (org.jpedal.DevFlags.GUITESTINGINPROGRESS || !SharedViewer.exitOnClose) {
            
            ((Container)currentGUI.getFrame()).setVisible(false);
            if (currentGUI.getFrame() instanceof JFrame) {
                ((JFrame) currentGUI.getFrame()).dispose();
            }
            
            decode_pdf.dispose();
            currentGUI.dispose();

        } else {
            ((Container)currentGUI.getFrame()).setVisible(false);
            if (currentGUI.getFrame() instanceof JFrame) {
                ((JFrame) currentGUI.getFrame()).dispose();
            }

            decode_pdf.dispose();
            currentGUI.dispose();

            //Added this one for now to remove a delay being experienced.
            System.exit(0);
        }
    }
}
