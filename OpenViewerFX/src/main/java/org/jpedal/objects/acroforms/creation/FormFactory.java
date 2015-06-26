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
 * FormFactory.java
 * ---------------
 */
package org.jpedal.objects.acroforms.creation;

import org.jpedal.objects.Javascript;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.acroforms.GUIData;

import java.util.EnumSet;
import java.util.Map;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.PdfPageData;
import org.jpedal.render.DynamicVectorRenderer;

@SuppressWarnings("UnusedDeclaration")
public interface FormFactory {
    
    /**
     * matches types of PDF form objects in Integer form
     */
    public static final Integer UNKNOWN = -1;
    public static final Integer LIST = 1;
    public static final Integer COMBOBOX = 2;
    public static final Integer SINGLELINETEXT = 3;
    public static final Integer SINGLELINEPASSWORD = 4;
    public static final Integer MULTILINETEXT = 5;
    public static final Integer MULTILINEPASSWORD = 6;
    public static final Integer PUSHBUTTON = 7;
    public static final Integer RADIOBUTTON = 8;
    public static final Integer CHECKBOXBUTTON = 9;
    public static final Integer ANNOTATION = 10;
    public static final Integer SIGNATURE = 11;
    
    /** match types for PDF objects in raw int form */
    public static final int unknown = -1;
    //LIST
    public static final int list = 1;
    public static final int combobox = 2;
    //TEXT
    public static final int singlelinetext = 3;
    public static final int singlelinepassword = 4;
    public static final int multilinetext = 5;
    public static final int multilinepassword = 6;
    //BUTTON
    public static final int pushbutton = 7;
    public static final int radiobutton = 8;
    public static final int checkboxbutton = 9;
    public static final int annotation = 10;
    public static final int signature = 11;
    
    public static int SWING = 1;
    
    public static int ULC = 2;
    
    public static int HTML = 3;
    
    public static int SVG = 4;
    
    public static int JAVAFX = 5;
    
    /**
     * setup and return a List component, from the specified formObject
     * @see FormObject
     */
    public Object listField(FormObject formObject);
    
    /**
     * setup and return a ComboBox component, from the specified formObject
     * @see FormObject
     */
    public Object comboBox(FormObject formObject);
    
    /**
     * setup and return a single line Text component, from the specified formObject
     * @see FormObject
     */
    public Object singleLineText(FormObject formObject);
    
    /**
     * setup and return a single line Password component, from the specified formObject
     * @see FormObject
     */
    public Object singleLinePassword(FormObject formObject);
    
    /**
     * setup and return a multi line Text component, from the specified formObject
     * @see FormObject
     */
    public Object multiLineText(FormObject formObject);
    
    /**
     * setup and return a multi line Password component, from the specified formObject
     * @see FormObject
     */
    public Object multiLinePassword(FormObject formObject);
    
    /**
     * setup and return a push button component, from the specified formObject
     * @see FormObject
     */
    public Object pushBut(FormObject formObject);
    
    /**
     * setup and return a single radio button component, from the specified formObject
     * @see FormObject
     */
    public Object radioBut(FormObject formObject);
    
    /**
     * setup and return a single checkBox button component, from the specified formObject
     * @see FormObject
     */
    public Object checkBoxBut(FormObject formObject);
    
    /**
     * setup annotations display with pop-ups, etc
     */
    public Object annotationButton(FormObject formObject);
    
    /**
     * setup the signature field
     */
    public Object signature(FormObject formObject);
    
    /**
     * user can instance own value so we need to pass in these objects
     * @param AcroRes
     * @param actionHandler
     * @param pageData
     * @param currentPdfFile
     */
    public void reset(Object[] AcroRes, ActionHandler actionHandler,PdfPageData pageData, PdfObjectReader currentPdfFile);
    
    /**
     * return new instance of GUIData implementation to support component set
     */
    public GUIData getCustomCompData();
    
    /** return Formfactory.<br>ULC, SWING or HTML<br>constant */
    int getType();
    
    //public void setDecoder(PdfDecoder decode_pdf);
    
    public void indexAllKids();
    
    /**pass in Map contains annot field list in order to set tabindex*/
    public void setAnnotOrder(Map<String, String> annotOrder);
    
    @SuppressWarnings("UnusedDeclaration")
    public void setOptions(EnumSet formSettings);
    
    void setDVR(DynamicVectorRenderer htmLoutput, Javascript javaScript);

}

