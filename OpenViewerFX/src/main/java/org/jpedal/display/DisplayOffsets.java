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
 * DisplayOffsets.java
 * ---------------
 */
package org.jpedal.display;

// <start-adobe><start-thin><start-ulc>
import org.jpedal.gui.GUIFactory;
//<end-ulc><end-thin><end-adobe>
import org.jpedal.external.ExternalHandlers;
import org.jpedal.external.Options;

import java.awt.*;
import org.jpedal.*;

public class DisplayOffsets {

    /**allow user to displace display*/
    private int userOffsetX, userOffsetY,userPrintOffsetX, userPrintOffsetY;

    //store cursor position for facing drag
    private int facingCursorX=10000, facingCursorY=10000;

    final PdfDecoderInt pdf;
    
    final ExternalHandlers externalHandlers;

    public DisplayOffsets(final PdfDecoderInt pdf, final ExternalHandlers externalHandlers) {
        this.pdf=pdf;
        this.externalHandlers=externalHandlers;
    }

    public void setUserOffsets(final int x, final int y, final int mode) {
        switch(mode){

            case org.jpedal.external.OffsetOptions.DISPLAY:
                userOffsetX=x;
                userOffsetY=y;
                break;

            case org.jpedal.external.OffsetOptions.PRINTING:
                userPrintOffsetX=x;
                userPrintOffsetY=-y; //make it negative so both work in same direction
                break;

            // <start-adobe><start-thin><start-ulc><end-ulc><end-thin><end-adobe>

            default:
                throw new RuntimeException("No such mode - look in org.jpedal.external.OffsetOptions for valid values");
        }
    }

    public Point getUserOffsets(final int mode) {

        switch(mode){

            case org.jpedal.external.OffsetOptions.DISPLAY:
                return new Point(userOffsetX,userOffsetY);

            case org.jpedal.external.OffsetOptions.PRINTING:
                return new Point(userPrintOffsetX,userPrintOffsetY);

            case org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_RIGHT:
                return new Point(facingCursorX,facingCursorY);

            default:
                throw new RuntimeException("No such mode - look in org.jpedal.external.OffsetOptions for valid values");
        }
    }

    public int getUserPrintOffsetX() {
        return userPrintOffsetX;
    }

    public int getUserPrintOffsetY() {
        return userPrintOffsetY;
    }

    public int getUserOffsetX() {
        return userOffsetX;
    }

    public int getUserOffsetY() {
        return userOffsetY;
    }
}
