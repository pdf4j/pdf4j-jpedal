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
 * GUIFactory.java
 * ---------------
 */
package org.jpedal.gui;

import java.util.Map;

import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.*;
import org.jpedal.examples.viewer.gui.GUI.PageCounter;
import org.jpedal.examples.viewer.gui.GUI.ScrollPolicy;
import org.jpedal.examples.viewer.gui.SwingCursor;
import org.jpedal.examples.viewer.gui.generic.GUIButtons;
import org.jpedal.examples.viewer.gui.generic.GUICombo;
import org.jpedal.examples.viewer.gui.generic.GUIMenuItems;
import org.jpedal.examples.viewer.gui.generic.GUISearchList;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.examples.viewer.paper.PaperSizes;
import org.jpedal.examples.viewer.utils.PropertiesFile;

@SuppressWarnings("UnusedDeclaration")
public interface GUIFactory {

    public int BUTTONBAR = 0;
    public int NAVBAR = 1;
    public int PAGES = 2;
    
    
    //<start-demo><end-demo>
    
    
    /**
     * flag used to show opening of multiple PDFs
     */
    public static final Integer MULTIPAGE = 1;

    /**
     * access to command object
     */
    public org.jpedal.examples.viewer.Commands getCommand();
    
    /**
     * align rotation combo box to default for page
     */
    public void resetRotationBox();

    /**
     * main method to initialise Swing specific code and create GUI display
     */
    public void init(Commands currentCommands, Object currentPrinter);

    /**
     * set title or over-ride with message
     */
    public void setViewerTitle(String title);

    /**
     * set all 3 combo boxes to isEnabled(value)
     */
    public void resetComboBoxes(boolean value);

    /**
     * zoom into page
     */
    public void scaleAndRotate();

    /**
     * get current rotation
     */
    public int getRotation();

    /**
     * get current scaling
     */
    public float getScaling();

    /**
     * get inset between edge of JPanel and PDF page
     */
    //	public int getPDFDisplayInset();
    /**
     * read value from rotation box and apply - called by combo listener
     */
    public void rotate();

    /**
     * toggle state of autoscrolling on/off
     */
    public void toogleAutoScrolling();

    //	<start-thin>
    public void setupThumbnailPanel();
    //	<end-thin>
    
    public void setAutoScrolling(boolean autoScroll);
    /**
     * remove outlines and flag for redraw
     */
    //public void removeOutlinePanels();
    /**
     * flush list of pages decoded
     */
    public void setNoPagesDecoded();

    /**
     * set text displayed in cursor co-ordinates box
     */
    public void setCoordText(String string);

    /**
     * set page number at bottom of screen
     */
    public void setPageNumber();

    //

    /**
     * allow access to root frame if required
     */
    public Object getFrame();

    public void resetNavBar();

    public void showMessageDialog(String message1);
    
    public int showMessageDialog(Object message1,Object[] options, int selectedChoice);

    public void showMessageDialog(Object message, String title, int type);

    public String showInputDialog(Object message, String title, int type);

    public String showInputDialog(String message);

    public void showMessageDialog(Object info);

    public int showConfirmDialog(String message, String message2, int option);

    public int showOverwriteDialog(String file, boolean yesToAllPresent);

    public void showFirstTimePopup();

    public int showConfirmDialog(Object message, String title, int optionType, int messageType);

    /**
     * show if user has set auto-scrolling on or off - if on moves at edge of
     * panel to show more
     */
    public boolean allowScrolling();

    /**
     * show is user has set the option to have exit confirmed with a dialog
     */
    public boolean confirmClose();

    /**
     * message to show in status object
     */
    public void updateStatusMessage(String message);

    public void resetStatusMessage(String message);

    /**
     * set current status value 0 -100
     */
    public void setStatusProgress(int size);

    public Object printDialog(String[] printersList, String defaultPrinter);

    public void setQualityBoxVisible(boolean visible);

    public void setPage(int newPage);

    Enum getType();

    public Object getMultiViewerFrames();
        
    public void setBookmarks(boolean alwaysGenerate);
    
    public String getBookmark(String bookmark);

    public void alterProperty(String value, boolean show);
    
    public SwingCursor getGUICursor();

    public void setRotationFromExternal(int rotation);
    
    public void setResults(GUISearchList results);
    
    public void setMultibox(int[] flags);
    
    /**
    * This method returns the object that stores and handles the various preferences for the viewer.
    * The returned object can be used to get property values and set them.
    * @return The PropertiesFile object currently in use by the viewer.
    */
    public PropertiesFile getProperties();
    
    public void snapScalingToDefaults(float newScaling);
    
    public Object getVerticalScrollBar();
    
    public String getPropertiesFileLocation();
    
    public boolean isSingle();
    
    public Object getPageContainer();
    
    public PaperSizes getPaperSizes();
    
    public void setPropertiesFileLocation(String file);
    
    /**
    * return comboBox or nul if not (QUALITY, SCALING or ROTATION
    * @param ID
    * @return
    */
    public GUICombo getCombo(int ID);
    
    /**
    * Method to enable / disable search options on the toolbar.
    */
    public void enableSearchItems(boolean enabled);
    
    public Object getDisplayPane();
    
    public void reinitialiseTabs(boolean showVisible);
    
    public void scrollToPage(int page);
    
    public Object getStatusBar();
    
    public void setTabsNotInitialised(boolean b);
   
    public void selectBookmark();
    
    public void setScalingFromExternal(String scale);
    
    public boolean getPageTurnScalingAppropriate();
    
    public void resetPageNav();
    
    public void removeSearchWindow(boolean justHide);
    
    public Map getHotspots();
     
     public void setSearchText(Object searchText);
     
     public void stopThumbnails();
     
     public Object getThumbnailPanel();
     
     public Object getOutlinePanel();
     
     public void setDownloadProgress(String message,int percentage);
     
     public void reinitThumbnails();
     
     public Object getThumbnailScrollBar();
     
     public void setThumbnailScrollBarVisibility(boolean v);
     
     public void setThumbnailScrollBarValue(int pageNum);
     
     public Object getSideTabBar();
     
     public int getSplitDividerLocation();
     
     public void dispose();
     
     public void rescanPdfLayers();
     
     public void setRootContainer(Object rawValue);
     
     public void setSearchFrame(GUISearchWindow searchFrame);
     
     public void searchInTab(GUISearchWindow searchFrame);
     
     public void setDisplayMode(Integer mode);
     /**
      * @return a boolean commandInThread
      */
     public boolean isCommandInThread();
     
     /**
      * @param b
      * assigns commandInThread to b
      */
     public void setCommandInThread(boolean b);
     
     /**
      * @return a boolean executingCommand
      */
     public boolean isExecutingCommand();
     
     /**
      * @param b
      * assigns executingCommand to b
      */
     public void setExecutingCommand(boolean b);

    public PdfDecoderInt getPdfDecoder();
    
    public GUIButtons getButtons();
    
    public Values getValues();
    
    public void setScrollBarPolicy(ScrollPolicy pol);
    
    public GUIMenuItems getMenuItems();
    
    public void decodePage();

    public RecentDocumentsFactory getRecentDocument();
    
    public void setRecentDocument();

    public void openFile(String fileToOpen);
    
    public void open(String fileName);
    
    public void enablePageCounter(PageCounter value, boolean enabled, boolean visibility);
    
    public void setPageCounterText(PageCounter value, String text);
    
    public Object getPageCounter(PageCounter value);
    
    public void updateTextBoxSize();
    
    public String getTitles(String title);
    
    public void enableStatusBar(boolean enabled, boolean visible);
    
    public void enableCursor(boolean enabled, boolean visible);
    
    public void enableMemoryBar(boolean enabled, boolean visible);
    
    public void enableNavigationBar(boolean enabled, boolean visible);
    
    public void enableDownloadBar(boolean enabled, boolean visible);
    
    public int getSidebarTabCount();
    
    public String getSidebarTabTitleAt(int pos);
    
    public void removeSidebarTabAt(int pos);

    public double getDividerLocation();
    
    public float scaleToVisible(float left, float right, float top, float bottom);
    
    public int getDropShadowDepth();
    
    public void setPannable(boolean pan);
    
    public void setupSplitPaneDivider(int size, boolean visibility);
    
    public double getStartSize();
    
    public void setStartSize(int size);

    public void setDisplayView(int SINGLE_PAGE, int DISPLAY_CENTERED);

    public void removePageListener();
    
}
