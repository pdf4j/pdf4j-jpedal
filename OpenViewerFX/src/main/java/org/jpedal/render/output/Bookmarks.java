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
 * Bookmarks.java
 * ---------------
 */
package org.jpedal.render.output;

import org.json.JSONWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.UnsupportedEncodingException;
import org.jpedal.utils.LogWriter;

public class Bookmarks {
    private static final boolean DEBUG_BOOKMARKS = false;

    /**
     * Determines whether the specified Document object contains any bookmarks.
     * @param bookmarks The Document object potentially contains bookmarks to check
     * @return Whether the provided bookmarks object actually contains bookmarks.
     */
    public static boolean hasBookmarks(final Document bookmarks) {
        return bookmarks != null && bookmarks.getDocumentElement() != null;
    }

    /**
     * Method to write specified bookmarks to specified JSONWriter
     * @param jsonWriter The JSONWriter to write the bookmarks to
     * @param bookmarks The bookmarks to add to json
     */
    public static void extractBookmarksAsJSON(final JSONWriter jsonWriter, final Document bookmarks){

        if(!hasBookmarks(bookmarks)){
            if (DEBUG_BOOKMARKS) {
                System.out.println("null Document - No bookmarks available");
            }
            return;
        }

        bookmarks.getDocumentElement().normalize();
        final NodeList nodeList = bookmarks.getChildNodes().item(0).getChildNodes();

        jsonWriter.array();
        for (int i = 0; i < nodeList.getLength(); i++){
            writeNode(nodeList.item(i), jsonWriter);
        }
        jsonWriter.endArray();
    }

    /**
     * Recursive method which writes out the value of the title node and it's children, if it has any.
     * It ignores any node which isn't named "title"
     */
    protected static void writeNode(final Node node, final JSONWriter jsonWriter){
        if(node == null) {
            return;
        }

        final Element e = (Element)node;

        String pageNum="";
        String linkName="";
        try {
            // Ensures the output is written in UTF-8, stops encoding issues on donhost box
            linkName = new String(e.getAttribute("title").getBytes("UTF-8"));
            pageNum = new String(e.getAttribute("page").getBytes("UTF-8"));
        } catch (final UnsupportedEncodingException ex) {
            //
            
            if(LogWriter.isOutput()){
                LogWriter.writeLog("Exception " + ex + " setting booksmarks for IDRviewer");
            }
        }

        linkName = linkName.replace("'", "\\'");
        if(DEBUG_BOOKMARKS){
            System.out.println( "Page: " + e.getAttribute("page") + " || Title: " + e.getAttribute("title"));
        }

        try {
            final int pageNumber = Integer.parseInt(pageNum);

            jsonWriter.object();
            jsonWriter.key("title").value(linkName);
            jsonWriter.key("page").value(pageNumber);

            if(node.hasChildNodes()){
                jsonWriter.key("children");
                jsonWriter.array();
                final NodeList nodeList = node.getChildNodes();
                for(int i = 0; i < nodeList.getLength(); i++){
                    writeNode(nodeList.item(i), jsonWriter);
                }
                jsonWriter.endArray();
            }

            jsonWriter.endObject();
        } catch (final NumberFormatException ex) {
            if(LogWriter.isOutput()){
                LogWriter.writeLog("Malformed bookmark: " + ex);
            }
        }

    }


}
