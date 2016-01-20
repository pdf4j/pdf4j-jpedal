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
 * Snapshot.java
 * ---------------
 */

package org.jpedal.examples.viewer.commands.generic;

import org.jpedal.*;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.gui.GUIFactory;

/**
 * Takes an Image Snapshot of the Selected Area
 */
public class ZoomOut {
    
    private static float[] scalingValues = {25f, 50f, 75f, 100f, 125f, 150f, 200f, 250f, 500f, 750f, 1000f};
    public static boolean execute(final Object[] args, final GUIFactory currentGUI, final PdfDecoderInt decode_pdf) {
        
        if (args == null) {
            float scaling = 100 * currentGUI.getScaling();
            scaling = (int)(decode_pdf.getDPIFactory().removeScaling(scaling)+0.5f);
            
            if (scaling > scalingValues[scalingValues.length-1]) {
                currentGUI.getCombo(Commands.SCALING).setSelectedItem(String.valueOf(scalingValues[scalingValues.length - 1]));
            } else {
                int scalingToUse = -1;
                for (int i = scalingValues.length-1; i != 0; i--) {
                    if (scaling <= scalingValues[i] && scaling > scalingValues[i - 1]) {
                        scalingToUse = i - 1;
                        break;
                    }
                }
                if (scalingToUse != -1) {
                    currentGUI.getCombo(Commands.SCALING).setSelectedItem(String.valueOf(scalingValues[scalingToUse]));
                }
            }
        }

        return false;
    }
}
