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
 * Properties.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui;

import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.GUI.PageCounter;
import org.jpedal.examples.viewer.gui.generic.GUIButtons;
import org.jpedal.examples.viewer.gui.generic.GUIMenuItems;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.gui.GUIFactory;

public class Properties {

    private static boolean propertyExists(final PropertiesFile properties, final String propName) {
        return !properties.getValue(propName).isEmpty();
    }

    private static int loadIntValue(final PropertiesFile properties, final String propName) {
        return Integer.parseInt(properties.getValue(propName));
    }

    private static boolean loadBooleanValue(final PropertiesFile properties, final String propName) {
        boolean value = propertyExists(properties, "ShowButtons");
        return value && properties.getValue(propName).equalsIgnoreCase("true");

    }

    private static void setButtonEnabledAndVisible(final GUIButtons buttons, final int type, final boolean set) {
        buttons.getButton(type).setEnabled(set);
        buttons.getButton(type).setVisible(set);
    }
    
    private static void removeUnwantedTabs(final int tabCount, final GUIFactory currentGUI, final boolean set, final String title){
        for (int i = 0; i < tabCount; i++) {

            if (currentGUI.getSidebarTabTitleAt(i).equals(currentGUI.getTitles(title)) && !set) {
                currentGUI.removeSidebarTabAt(i);
            }
        }
    }
    
    private static void loadNavigationPanel(final PropertiesFile properties, final GUIFactory currentGUI, final GUIButtons buttons){

        boolean set = loadBooleanValue(properties, "Firstbottom");
        setButtonEnabledAndVisible(buttons, Commands.FIRSTPAGE, set);
        
        set = loadBooleanValue(properties, "Back10bottom");
        setButtonEnabledAndVisible(buttons, Commands.FBACKPAGE, set);

        set = loadBooleanValue(properties, "Backbottom");
        setButtonEnabledAndVisible(buttons, Commands.BACKPAGE, set);

        set = loadBooleanValue(properties, "Gotobottom");
        currentGUI.enablePageCounter(PageCounter.ALL, set, set);

        set = loadBooleanValue(properties, "Forwardbottom");
        setButtonEnabledAndVisible(buttons, Commands.FORWARDPAGE, set);

        set = loadBooleanValue(properties, "Forward10bottom");
        setButtonEnabledAndVisible(buttons, Commands.FFORWARDPAGE, set);

        set = loadBooleanValue(properties, "Lastbottom");
        setButtonEnabledAndVisible(buttons, Commands.LASTPAGE, set);

        set = loadBooleanValue(properties, "Singlebottom");
        buttons.getButton(Commands.SINGLE).setVisible(set);

        set = loadBooleanValue(properties, "Continuousbottom");
        buttons.getButton(Commands.CONTINUOUS).setVisible(set);

        set = loadBooleanValue(properties, "Continuousfacingbottom");
        buttons.getButton(Commands.CONTINUOUS_FACING).setVisible(set);

        set = loadBooleanValue(properties, "Facingbottom");
        buttons.getButton(Commands.FACING).setVisible(set);

        set = loadBooleanValue(properties, "PageFlowbottom");
        buttons.getButton(Commands.PAGEFLOW).setVisible(set);

        set = loadBooleanValue(properties, "Memorybottom");
        currentGUI.enableMemoryBar(set, set);
        
    }
    
    private static void loadTopPanels(final PropertiesFile properties, final GUIFactory currentGUI, final GUIMenuItems menuItems){
    
        boolean set = loadBooleanValue(properties, "ShowMenubar");
        menuItems.setMenuItem(Commands.CURRENTMENU, set, set);

        set = loadBooleanValue(properties, "ShowButtons");
        currentGUI.getButtons().setEnabled(set);
        currentGUI.getButtons().setVisible(set);

        set = loadBooleanValue(properties, "ShowNavigationbar");
        currentGUI.enableNavigationBar(set, set);

        if (currentGUI.getDisplayPane() != null) {
            set = loadBooleanValue(properties, "ShowSidetabbar");
            if (!set) {
                currentGUI.setupSplitPaneDivider(0, set);
            } else {
                currentGUI.setupSplitPaneDivider(5, set);
            }
        }
    }
    
    private static void loadButtonBar(final PropertiesFile properties, final GUIFactory currentGUI, final GUIButtons buttons){
        boolean set = loadBooleanValue(properties, "Scalingdisplay");
        currentGUI.getCombo(Commands.SCALING).setEnabled(set);
        currentGUI.getCombo(Commands.SCALING).setVisibility(set);

        set = loadBooleanValue(properties, "Rotationdisplay");
        currentGUI.getCombo(Commands.ROTATION).setEnabled(set);
        currentGUI.getCombo(Commands.ROTATION).setVisibility(set);

        set = loadBooleanValue(properties, "Imageopdisplay");

        if (currentGUI.getCombo(Commands.QUALITY) != null) {
            currentGUI.getCombo(Commands.QUALITY).setVisibility(set);
            currentGUI.getCombo(Commands.QUALITY).setEnabled(set);
        }

        set = loadBooleanValue(properties, "Openfilebutton");
        setButtonEnabledAndVisible(buttons, Commands.OPENFILE, set);

        set = loadBooleanValue(properties, "Printbutton");
        setButtonEnabledAndVisible(buttons, Commands.PRINT, set);

        set = loadBooleanValue(properties, "Searchbutton");
        setButtonEnabledAndVisible(buttons, Commands.FIND, set);

        set = loadBooleanValue(properties, "Propertiesbutton");
        setButtonEnabledAndVisible(buttons, Commands.DOCINFO, set);

        set = loadBooleanValue(properties, "Aboutbutton");
        setButtonEnabledAndVisible(buttons, Commands.ABOUT, set);

        set = loadBooleanValue(properties, "Snapshotbutton");
        setButtonEnabledAndVisible(buttons, Commands.SNAPSHOT, set);

        //

        //

        set = loadBooleanValue(properties, "CursorButton");
        currentGUI.enableCursor(set, set);

        set = loadBooleanValue(properties, "MouseModeButton");
        setButtonEnabledAndVisible(buttons, Commands.MOUSEMODE, set);
    }
    
    private static void loadSideTabBar(final PropertiesFile properties, final GUIFactory currentGUI){
        boolean set = (loadBooleanValue(properties, "Pagetab") && currentGUI.getSidebarTabCount() != 0);
        removeUnwantedTabs(currentGUI.getSidebarTabCount(), currentGUI, set, "pageTitle");

        set = (loadBooleanValue(properties, "Bookmarkstab") && currentGUI.getSidebarTabCount() != 0);
        removeUnwantedTabs(currentGUI.getSidebarTabCount(), currentGUI, set, "bookmarksTitle");

        set = (loadBooleanValue(properties, "Layerstab") && currentGUI.getSidebarTabCount() != 0);
        removeUnwantedTabs(currentGUI.getSidebarTabCount(), currentGUI, set, "layersTitle");

        set = (loadBooleanValue(properties, "Signaturestab") && currentGUI.getSidebarTabCount() != 0);
        removeUnwantedTabs(currentGUI.getSidebarTabCount(), currentGUI, set, "signaturesTitle");

    }
    
    private static void loadFileMenuItems(final PropertiesFile properties, final GUIFactory currentGUI, final Values commonValues, final GUIMenuItems menuItems){
    
        boolean set = loadBooleanValue(properties, "FileMenu");
        menuItems.setMenuItem(Commands.FILEMENU, set, set);

        set = loadBooleanValue(properties, "OpenMenu");
        menuItems.setMenuItem(Commands.OPENMENU, set, set);

        set = loadBooleanValue(properties, "Open");
        menuItems.setMenuItem(Commands.OPENFILE, set, set);

        set = loadBooleanValue(properties, "Openurl");
        menuItems.setMenuItem(Commands.OPENURL, set, set);

        set = loadBooleanValue(properties, "Save");
        menuItems.setMenuItem(Commands.SAVE, set, set);

        set = loadBooleanValue(properties, "Resaveasforms");
        menuItems.setMenuItem(Commands.RESAVEASFORM, set, set);

        set = loadBooleanValue(properties, "Find");
        menuItems.setMenuItem(Commands.FIND, set, set);

        set = loadBooleanValue(properties, "Documentproperties");
        menuItems.setMenuItem(Commands.DOCINFO, set, set);

        //@SIGNING
        if (menuItems.isMenuItemExist(Commands.SIGN)) {
            set = loadBooleanValue(properties, "Signpdf");
            menuItems.setMenuItem(Commands.SIGN, commonValues.isEncrypOnClasspath(), set);
        }

        set = loadBooleanValue(properties, "Print");
        menuItems.setMenuItem(Commands.PRINT, set, set);

        set = loadBooleanValue(properties, "Recentdocuments");
        currentGUI.getRecentDocument().enableRecentDocuments(set);

        set = loadBooleanValue(properties, "Exit");
        menuItems.setMenuItem(Commands.EXIT, set, set);

        //Ensure none of the menus start with a separator
        menuItems.ensureNoSeperators(Commands.FILEMENU);
    }
    
    private static void loadEditMenuItems(final PropertiesFile properties, final GUIMenuItems menuItems){
        
        boolean set = loadBooleanValue(properties, "EditMenu");
        menuItems.setMenuItem(Commands.EDITMENU, set, set);

        set = loadBooleanValue(properties, "Copy");
        menuItems.setMenuItem(Commands.COPY, set, set);

        set = loadBooleanValue(properties, "Selectall");
        menuItems.setMenuItem(Commands.SELECTALL, set, set);

        set = loadBooleanValue(properties, "Deselectall");
        menuItems.setMenuItem(Commands.DESELECTALL, set, set);

        set = (loadBooleanValue(properties, "Preferences")) && (!loadBooleanValue(properties, "readOnly"));
        menuItems.setMenuItem(Commands.PREFERENCES, set, set);

        //Ensure none of the menus start with a separator
        menuItems.ensureNoSeperators(Commands.EDITMENU);
    }
    
    private static void loadViewMenuItems(final PropertiesFile properties, final GUIMenuItems menuItems){
    
        boolean set = loadBooleanValue(properties, "ViewMenu");
        menuItems.setMenuItem(Commands.VIEWMENU, set, set);

        set = loadBooleanValue(properties, "GotoMenu");
        menuItems.setMenuItem(Commands.GOTOMENU, set, set);

        set = loadBooleanValue(properties, "Firstpage");
        menuItems.setMenuItem(Commands.FIRSTPAGE, set, set);

        set = loadBooleanValue(properties, "Backpage");
        menuItems.setMenuItem(Commands.BACKPAGE, set, set);

        set = loadBooleanValue(properties, "Forwardpage");
        menuItems.setMenuItem(Commands.FORWARDPAGE, set, set);

        set = loadBooleanValue(properties, "Lastpage");
        menuItems.setMenuItem(Commands.LASTPAGE, set, set);

        set = loadBooleanValue(properties, "Goto");
        menuItems.setMenuItem(Commands.GOTO, set, set);

        set = loadBooleanValue(properties, "Previousdocument");
        menuItems.setMenuItem(Commands.PREVIOUSDOCUMENT, set, set);

        set = loadBooleanValue(properties, "Nextdocument");
        menuItems.setMenuItem(Commands.NEXTDOCUMENT, set, set);

        if (menuItems.isMenuItemExist(Commands.PAGELAYOUTMENU)) {
            set = loadBooleanValue(properties, "PagelayoutMenu");
            menuItems.setMenuItem(Commands.PAGELAYOUTMENU, set, set);
        }

        if (menuItems.isMenuItemExist(Commands.SINGLE)) {
            set = loadBooleanValue(properties, "Single");
            menuItems.setMenuItem(Commands.SINGLE, set, set);
        }

        if (menuItems.isMenuItemExist(Commands.CONTINUOUS)) {
            set = loadBooleanValue(properties, "Continuous");
            menuItems.setMenuItem(Commands.CONTINUOUS, set, set);
        }

        if (menuItems.isMenuItemExist(Commands.FACING)) {
            set = loadBooleanValue(properties, "Facing");
            menuItems.setMenuItem(Commands.FACING, set, set);
        }

        if (menuItems.isMenuItemExist(Commands.CONTINUOUS_FACING)) {
            set = loadBooleanValue(properties, "Continuousfacing");
            menuItems.setMenuItem(Commands.CONTINUOUS_FACING, set, set);
        }

        if (menuItems.isMenuItemExist(Commands.PAGEFLOW)) {
            set = loadBooleanValue(properties, "PageFlow");
            menuItems.setMenuItem(Commands.PAGEFLOW, set, set);
        }

        if (menuItems.isMenuItemExist(Commands.TEXTSELECT)) {
            set = loadBooleanValue(properties, "textSelect");
            menuItems.setMenuItem(Commands.TEXTSELECT, set, set);
        }

        if (menuItems.isMenuItemExist(Commands.SEPARATECOVER)) {
            set = loadBooleanValue(properties, "separateCover");
            menuItems.setMenuItem(Commands.SEPARATECOVER, set, set);
        }

        if (menuItems.isMenuItemExist(Commands.PANMODE)) {
            set = loadBooleanValue(properties, "panMode");
            menuItems.setMenuItem(Commands.PANMODE, set, set);
        }

        if (menuItems.isMenuItemExist(Commands.FULLSCREEN)) {
            set = loadBooleanValue(properties, "Fullscreen");
            menuItems.setMenuItem(Commands.FULLSCREEN, set, set);
        }

        if (menuItems.isMenuItemExist(Commands.WINDOWMENU)) {

            set = loadBooleanValue(properties, "WindowMenu");
            menuItems.setMenuItem(Commands.WINDOWMENU, set, set);

            set = loadBooleanValue(properties, "Cascade");
            menuItems.setMenuItem(Commands.CASCADE, set, set);

            set = loadBooleanValue(properties, "Tile");
            menuItems.setMenuItem(Commands.TILE, set, set);
        }

        //Ensure none of the menus start with a separator
        menuItems.ensureNoSeperators(Commands.VIEWMENU);
        menuItems.ensureNoSeperators(Commands.GOTOMENU);
    }
    
    private static void loadExportMenuItems(final PropertiesFile properties, final GUIMenuItems menuItems){
    
        boolean set = loadBooleanValue(properties, "ExportMenu");
        menuItems.setMenuItem(Commands.EXPORTMENU, set, set);

        set = loadBooleanValue(properties, "PdfMenu");
        menuItems.setMenuItem(Commands.PDFMENU, set, set);

        set = loadBooleanValue(properties, "Oneperpage");
        menuItems.setMenuItem(Commands.ONEPERPAGE, set, set);

        set = loadBooleanValue(properties, "Nup");
        menuItems.setMenuItem(Commands.NUP, set, set);

        set = loadBooleanValue(properties, "Handouts");
        menuItems.setMenuItem(Commands.HANDOUTS, set, set);

        set = loadBooleanValue(properties, "ContentMenu");
        menuItems.setMenuItem(Commands.CONTENTMENU, set, set);

        set = loadBooleanValue(properties, "Images");
        menuItems.setMenuItem(Commands.IMAGES, set, set);

        set = loadBooleanValue(properties, "Text");
        menuItems.setMenuItem(Commands.TEXT, set, set);

        set = loadBooleanValue(properties, "Bitmap");
        menuItems.setMenuItem(Commands.BITMAP, set, set);

    }
    
    private static void loadPageToolsMenuItems(final PropertiesFile properties, final GUIMenuItems menuItems){
    
        boolean set = loadBooleanValue(properties, "PagetoolsMenu");
        menuItems.setMenuItem(Commands.PAGETOOLSMENU, set, set);

        set = loadBooleanValue(properties, "Rotatepages");
        menuItems.setMenuItem(Commands.ROTATE, set, set);

        set = loadBooleanValue(properties, "Deletepages");
        menuItems.setMenuItem(Commands.DELETE, set, set);

        set = loadBooleanValue(properties, "Addpage");
        menuItems.setMenuItem(Commands.ADD, set, set);

        set = loadBooleanValue(properties, "Addheaderfooter");
        menuItems.setMenuItem(Commands.ADDHEADERFOOTER, set, set);

        set = loadBooleanValue(properties, "Stamptext");
        menuItems.setMenuItem(Commands.STAMPTEXT, set, set);

        set = loadBooleanValue(properties, "Stampimage");
        menuItems.setMenuItem(Commands.STAMPIMAGE, set, set);

        set = loadBooleanValue(properties, "Crop");
        menuItems.setMenuItem(Commands.CROP, set, set);

    }
    
    private static void loadHelpMenuItems(final PropertiesFile properties, final GUIMenuItems menuItems){
    
        boolean set = loadBooleanValue(properties, "HelpMenu");
        menuItems.setMenuItem(Commands.HELP, set, set);

        set = loadBooleanValue(properties, "Visitwebsite");
        menuItems.setMenuItem(Commands.VISITWEBSITE, set, set);

        set = loadBooleanValue(properties, "Tipoftheday");
        menuItems.setMenuItem(Commands.TIP, set, set);

        set = loadBooleanValue(properties, "About");
        menuItems.setMenuItem(Commands.ABOUT, set, set);

        //

    }
    
    public static void load(final PropertiesFile properties, final GUIFactory currentGUI, final Values commonValues, final GUIButtons buttons, final GUIMenuItems menuItems) {

        try {

//		default value used to load props
            boolean set;

            //Disable entire section
            if (propertyExists(properties, "sideTabBarCollapseLength")) {
                currentGUI.setStartSize(loadIntValue(properties, "sideTabBarCollapseLength"));
            }

            if (propertyExists(properties, "sideTabBarExpandLength")) {
                GUI.expandedSize = loadIntValue(properties, "sideTabBarExpandLength");

                //commenting out this one breaks viewer....
                currentGUI.reinitialiseTabs(false);
                //properties.setValue("sideTabBarCollapseLength", String.valueOf(value));
            }
            
            set = loadBooleanValue(properties, "Progressdisplay");
            currentGUI.enableStatusBar(set, set);

            set = loadBooleanValue(properties, "Downloadprogressdisplay");
            currentGUI.enableDownloadBar(set, set);
            
            //Top Level GUI Panels
            loadTopPanels(properties, currentGUI, menuItems);
            
            //Items on nav pane
            loadNavigationPanel(properties, currentGUI, buttons);
            
            //Items on button bar
            loadButtonBar(properties, currentGUI, buttons);
            
            //Items on side tab bar
            loadSideTabBar(properties, currentGUI);
            
            /**
             * Items from the menu item
             */
            if (menuItems.isMenuItemExist(Commands.FILEMENU)) { //all of these will be null in 'Wrapper' mode so ignore
                loadFileMenuItems(properties, currentGUI, commonValues, menuItems);
            }
            
            if (menuItems.isMenuItemExist(Commands.EDITMENU)) {
                loadEditMenuItems(properties, menuItems);
            }
            
            if (menuItems.isMenuItemExist(Commands.VIEWMENU)) {
                loadViewMenuItems(properties, menuItems);
            }
            
            if (menuItems.isMenuItemExist(Commands.EXPORTMENU)) {
                loadExportMenuItems(properties, menuItems);
            }
            
            if (menuItems.isMenuItemExist(Commands.PAGETOOLSMENU)) {
                loadPageToolsMenuItems(properties, menuItems);
            }
            
            if (menuItems.isMenuItemExist(Commands.HELP)) {
                loadHelpMenuItems(properties, menuItems);
            }

            currentGUI.getButtons().checkButtonSeparators();

        } catch (final Exception ee) {
            ee.printStackTrace();
        }
    }

    public static void alterProperty(final String value, final boolean set, final PropertiesFile properties, final GUIFactory currentGUI, final boolean isSingle, final GUIButtons buttons, final GUIMenuItems menuItems) {

        //Disable entire section
        if (value.equals("ShowMenubar")) {
            menuItems.setMenuItem(Commands.CURRENTMENU, set, set);
            properties.setValue("ShowMenubar", String.valueOf(set));
        }
        if (value.equals("ShowButtons")) {
            currentGUI.getButtons().setEnabled(set);
            currentGUI.getButtons().setVisible(set);
            properties.setValue("ShowButtons", String.valueOf(set));
        }
        if (value.equals("ShowDisplayoptions")) {
            properties.setValue("ShowDisplayoptions", String.valueOf(set));
        }
        if (value.equals("ShowNavigationbar")) {
            currentGUI.enableNavigationBar(set, set);
            properties.setValue("ShowNavigationbar", String.valueOf(set));
        }

        if (isSingle && value.equals("ShowSidetabbar")) {
                if (!set) {
                    currentGUI.setupSplitPaneDivider(0, set);
                } else {
                    currentGUI.setupSplitPaneDivider(5, set);
                }
                properties.setValue("ShowSidetabbar", String.valueOf(set));
            }

        /**
         * Items on nav pane
         */
        if (value.equals("Firstbottom")) {
            setButtonEnabledAndVisible(buttons, Commands.FIRSTPAGE, set);
        }
        if (value.equals("Back10bottom")) {
            setButtonEnabledAndVisible(buttons, Commands.FBACKPAGE, set);
        }
        if (value.equals("Backbottom")) {
            setButtonEnabledAndVisible(buttons, Commands.BACKPAGE, set);
        }
        if (value.equals("Gotobottom")) {
            currentGUI.enablePageCounter(PageCounter.ALL, set, set);
        }
        if (value.equals("Forwardbottom")) {
            setButtonEnabledAndVisible(buttons, Commands.FORWARDPAGE, set);
        }
        if (value.equals("Forward10bottom")) {
            setButtonEnabledAndVisible(buttons, Commands.FFORWARDPAGE, set);
        }
        if (value.equals("Lastbottom")) {
            setButtonEnabledAndVisible(buttons, Commands.LASTPAGE, set);
        }
        if (value.equals("Singlebottom")) {
            //			singleButton.setEnabled(set);
            buttons.getButton(Commands.SINGLE).setVisible(set);
        }
        if (value.equals("Continuousbottom")) {
            //			continuousButton.setEnabled(set);
            buttons.getButton(Commands.CONTINUOUS).setVisible(set);
        }
        if (value.equals("Continuousfacingbottom")) {
            //			continuousFacingButton.setEnabled(set);
            buttons.getButton(Commands.CONTINUOUS_FACING).setVisible(set);
        }
        if (value.equals("Facingbottom")) {
            //			facingButton.setEnabled(set);
            buttons.getButton(Commands.FACING).setVisible(set);
        }
        if (value.equals("PageFlowbottom")) {
            //            pageFlowButton.setEnabled(set);
            buttons.getButton(Commands.PAGEFLOW).setVisible(set);
        }
        if (value.equals("Memorybottom")) {
            currentGUI.enableMemoryBar(set, set);
        }

        /**
         * Items on option pane
         */
        if (value.equals("Scalingdisplay")) {
            currentGUI.getCombo(Commands.SCALING).setEnabled(set);
            currentGUI.getCombo(Commands.SCALING).setVisibility(set);
            properties.setValue("Scalingdisplay", String.valueOf(set));
        }
        if (value.equals("Rotationdisplay")) {
            currentGUI.getCombo(Commands.ROTATION).setEnabled(set);
            currentGUI.getCombo(Commands.ROTATION).setVisibility(set);
            properties.setValue("Rotationdisplay", String.valueOf(set));
        }
        if (value.equals("Imageopdisplay")) {
            currentGUI.getCombo(Commands.QUALITY).setVisibility(set);
            currentGUI.getCombo(Commands.QUALITY).setEnabled(set);
            properties.setValue("Imageopdisplay", String.valueOf(set));
        }
        if (value.equals("Progressdisplay")) {
            currentGUI.enableStatusBar(set, set);
            properties.setValue("Progressdisplay", String.valueOf(set));
        }
        if (value.equals("Downloadprogressdisplay")) {
            currentGUI.enableDownloadBar(set, set);
            properties.setValue("Downloadprogressdisplay", String.valueOf(set));
        }

        /**
         * Items on button bar
         */
        if (value.equals("Openfilebutton")) {
            setButtonEnabledAndVisible(buttons, Commands.OPENFILE, set);
        }
        if (value.equals("Printbutton")) {
            setButtonEnabledAndVisible(buttons, Commands.PRINT, set);
        }
        if (value.equals("Searchbutton")) {
            setButtonEnabledAndVisible(buttons, Commands.FIND, set);
        }
        if (value.equals("Propertiesbutton")) {
            setButtonEnabledAndVisible(buttons, Commands.DOCINFO, set);
        }
        if (value.equals("Aboutbutton")) {
            setButtonEnabledAndVisible(buttons, Commands.ABOUT, set);
        }
        if (value.equals("Snapshotbutton")) {
            setButtonEnabledAndVisible(buttons, Commands.SNAPSHOT, set);
        }
        //

        if (value.equals("CursorButton")) {
            currentGUI.enableCursor(set, set);
        }

        if (value.equals("MouseModeButton")) {
            setButtonEnabledAndVisible(buttons, Commands.MOUSEMODE, set);
        }

        /**
         * Items on signature tab
         */
        if (value.equals("Pagetab") && currentGUI.getSidebarTabCount() != 0) {
            removeUnwantedTabs(currentGUI.getSidebarTabCount(), currentGUI, set, "pageTitle");
        }
        
        if (value.equals("Bookmarkstab") && currentGUI.getSidebarTabCount() != 0) {
            removeUnwantedTabs(currentGUI.getSidebarTabCount(), currentGUI, set, "bookmarksTitle");
        }
        
        if (value.equals("Layerstab") && currentGUI.getSidebarTabCount() != 0) {
            removeUnwantedTabs(currentGUI.getSidebarTabCount(), currentGUI, set, "layersTitle");
        }
        
        if (value.equals("Signaturestab") && currentGUI.getSidebarTabCount() != 0) {
            removeUnwantedTabs(currentGUI.getSidebarTabCount(), currentGUI, set, "signaturesTitle");
        }

        /**
         * Items from the menu item
         */
        if (value.equals("FileMenu")) {
            menuItems.setMenuItem(Commands.FILEMENU, set, set);
        }
        if (value.equals("OpenMenu")) {
            menuItems.setMenuItem(Commands.OPENMENU, set, set);
        }
        if (value.equals("Open")) {
            menuItems.setMenuItem(Commands.OPENFILE, set, set);
        }
        if (value.equals("Openurl")) {
            menuItems.setMenuItem(Commands.OPENURL, set, set);
        }

        if (value.equals("Save")) {
            menuItems.setMenuItem(Commands.SAVE, set, set);
        }

        //added check to code (as it may not have been initialised)
        if (value.equals("Resaveasforms") && menuItems.isMenuItemExist(Commands.RESAVEASFORM)) { //will not be initialised if Itext not on path
            menuItems.setMenuItem(Commands.RESAVEASFORM, set, set);
        }

        if (value.equals("Find")) {
            menuItems.setMenuItem(Commands.FIND, set, set);
        }
        if (value.equals("Documentproperties")) {
            menuItems.setMenuItem(Commands.DOCINFO, set, set);
        }
        //@SIGNING
        if (value.equals("Signpdf")) {
            menuItems.setMenuItem(Commands.SIGN, set, set);
        }

        if (value.equals("Print")) {
            menuItems.setMenuItem(Commands.PRINT, set, set);
        }
        if (value.equals("Recentdocuments")) {
            currentGUI.getRecentDocument().enableRecentDocuments(set);
        }
        if (value.equals("Exit")) {
            menuItems.setMenuItem(Commands.EXIT, set, set);
        }

        if (value.equals("EditMenu")) {
            menuItems.setMenuItem(Commands.EDITMENU, set, set);
        }
        if (value.equals("Copy")) {
            menuItems.setMenuItem(Commands.COPY, set, set);
        }
        if (value.equals("Selectall")) {
            menuItems.setMenuItem(Commands.SELECTALL, set, set);
        }
        if (value.equals("Deselectall")) {
            menuItems.setMenuItem(Commands.DESELECTALL, set, set);
        }
        if (value.equals("Preferences")) {
            menuItems.setMenuItem(Commands.PREFERENCES, set, set);
        }

        if (value.equals("ViewMenu")) {
            menuItems.setMenuItem(Commands.VIEWMENU, set, set);
        }
        if (value.equals("GotoMenu")) {
            menuItems.setMenuItem(Commands.GOTOMENU, set, set);
        }
        if (value.equals("Firstpage")) {
            menuItems.setMenuItem(Commands.FIRSTPAGE, set, set);
        }
        if (value.equals("Backpage")) {
            menuItems.setMenuItem(Commands.BACKPAGE, set, set);
        }
        if (value.equals("Forwardpage")) {
            menuItems.setMenuItem(Commands.FORWARDPAGE, set, set);
        }
        if (value.equals("Lastpage")) {
            menuItems.setMenuItem(Commands.LASTPAGE, set, set);
        }
        if (value.equals("Goto")) {
            menuItems.setMenuItem(Commands.GOTO, set, set);
        }
        if (value.equals("Previousdocument")) {
            menuItems.setMenuItem(Commands.PREVIOUSDOCUMENT, set, set);
        }
        if (value.equals("Nextdocument")) {
            menuItems.setMenuItem(Commands.NEXTDOCUMENT, set, set);
        }

        if (isSingle) {
            if (value.equals("PagelayoutMenu")) {
                menuItems.setMenuItem(Commands.PAGELAYOUTMENU, set, set);
            }
            if (value.equals("Single")) {
                menuItems.setMenuItem(Commands.SINGLE, set, set);
            }
            if (value.equals("Continuous")) {
                menuItems.setMenuItem(Commands.CONTINUOUS, set, set);
            }
            if (value.equals("Facing")) {
                menuItems.setMenuItem(Commands.FACING, set, set);
            }
            if (value.equals("Continuousfacing")) {
                menuItems.setMenuItem(Commands.CONTINUOUS_FACING, set, set);
            }

            if (menuItems.isMenuItemExist(Commands.PAGEFLOW) && value.equals("PageFlow")){
                    menuItems.setMenuItem(Commands.PAGEFLOW, set, set);
            }
        }

        if (menuItems.isMenuItemExist(Commands.PANMODE) && value.equals("panMode")) {
                menuItems.setMenuItem(Commands.PANMODE, set, set);
            }
        
        if (menuItems.isMenuItemExist(Commands.TEXTSELECT) && value.equals("textSelect")) {
                menuItems.setMenuItem(Commands.TEXTSELECT, set, set);
            }
        
        if (value.equals("Fullscreen")) {
            menuItems.setMenuItem(Commands.FULLSCREEN, set, set);
        }
        if (menuItems.isMenuItemExist(Commands.SEPARATECOVER) && value.equals("separateCover")) {
                menuItems.setMenuItem(Commands.SEPARATECOVER, set, set);
            }
     
        if (menuItems.isMenuItemExist(Commands.WINDOWMENU)) {
            if (value.equals("WindowMenu")) {
                menuItems.setMenuItem(Commands.WINDOWMENU, set, set);
            }
            if (value.equals("Cascade")) {
                menuItems.setMenuItem(Commands.CASCADE, set, set);
            }
            if (value.equals("Tile")) {
                menuItems.setMenuItem(Commands.TILE, set, set);
            }
        }
        //		if(commonValues.isItextOnClasspath()){
        if (value.equals("ExportMenu")) {
            menuItems.setMenuItem(Commands.EXPORTMENU, set, set);
        }
        if (value.equals("PdfMenu")) {
            menuItems.setMenuItem(Commands.PDFMENU, set, set);
        }
        if (value.equals("Oneperpage")) {
            menuItems.setMenuItem(Commands.ONEPERPAGE, set, set);
        }
        if (value.equals("Nup")) {
            menuItems.setMenuItem(Commands.NUP, set, set);
        }
        if (value.equals("Handouts")) {
            menuItems.setMenuItem(Commands.HANDOUTS, set, set);
        }

        if (value.equals("ContentMenu")) {
            menuItems.setMenuItem(Commands.CONTENTMENU, set, set);
        }
        if (value.equals("Images")) {
            menuItems.setMenuItem(Commands.IMAGES, set, set);
        }
        if (value.equals("Text")) {
            menuItems.setMenuItem(Commands.TEXT, set, set);
        }

        if (value.equals("Bitmap")) {
            menuItems.setMenuItem(Commands.BITMAP, set, set);
        }

        if (value.equals("PagetoolsMenu")) {
            menuItems.setMenuItem(Commands.PAGETOOLSMENU, set, set);
        }
        if (value.equals("Rotatepages")) {
            menuItems.setMenuItem(Commands.ROTATE, set, set);
        }
        if (value.equals("Deletepages")) {
            menuItems.setMenuItem(Commands.DELETE, set, set);
        }
        if (value.equals("Addpage")) {
            menuItems.setMenuItem(Commands.ADD, set, set);
        }
        if (value.equals("Addheaderfooter")) {
            menuItems.setMenuItem(Commands.ADDHEADERFOOTER, set, set);
        }
        if (value.equals("Stamptext")) {
            menuItems.setMenuItem(Commands.STAMPTEXT, set, set);
        }
        if (value.equals("Stampimage")) {
            menuItems.setMenuItem(Commands.STAMPIMAGE, set, set);
        }
        if (value.equals("Crop")) {
            menuItems.setMenuItem(Commands.CROP, set, set);
        }
        //		}
        if (value.equals("HelpMenu")) {
            menuItems.setMenuItem(Commands.HELP, set, set);
        }
        if (value.equals("Visitwebsite")) {
            menuItems.setMenuItem(Commands.VISITWEBSITE, set, set);
        }
        if (value.equals("Tipoftheday")) {
            menuItems.setMenuItem(Commands.TIP, set, set);
        }
//           if (value.equals("Checkupdates")) {
//               menuItems.setMenuItem(Commands.UPDATE,set,set);
//           }
        if (value.equals("About")) {
            menuItems.setMenuItem(Commands.ABOUT, set, set);
        }
        //

        currentGUI.getButtons().checkButtonSeparators();
    }
}
