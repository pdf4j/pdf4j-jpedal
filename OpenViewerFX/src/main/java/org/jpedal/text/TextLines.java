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
 * TextLines.java
 * ---------------
 */
package org.jpedal.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jpedal.objects.PdfData;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;

public class TextLines {

    /**stores area of arrays in which text should be highlighted*/
    private Map<Integer, int[][]> lineAreas = new HashMap<Integer, int[][]>();
    private Map<Integer, int[]> lineWritingMode = new HashMap<Integer, int[]>();

    /**Highlight Areas stored here*/
    public Map<Integer, int[][]> areas = new HashMap<Integer, int[][]>();
    
    /**Track if highlgiht localAreas has changed since last call to getHighlightedAreas(int)*/
    boolean hasHighlightAreasUpdated;
 
    /**
	 * Highlights a section of lines that form a paragraph and
 returns the area that encloses all highlight localAreas.
     * @param x Coord x value to begin looking for a paragraph
     * @param y Coord y value to begin looking for a paragraph
     * @param page Page number of page to look for a paragraph on
	 * @return int[] that contains x,y,w,h of all localAreas highlighted
	 */
    public int[] setFoundParagraphAsArray(final int x, final int y, final int page){

    final int[][] lines = getLineAreasAs2DArray(page);

    if(lines!=null){

        final int[] point = {x,y,1,1};
        final int[] current = {0,0,0,0};
        boolean lineFound = false;
        int selectedLine = 0;

        for(int i=0; i!=lines.length; i++){
            if(intersects(lines[i],point)){
                selectedLine = i;
                lineFound = true;
                break;
            }
        }

        if(lineFound){
            int left = lines[selectedLine][0];
            int cx = lines[selectedLine][0]+(lines[selectedLine][2]/2);
            int right = lines[selectedLine][0]+lines[selectedLine][2];
            int cy = lines[selectedLine][1]+(lines[selectedLine][3]/2);
            int h = lines[selectedLine][3];

            current[0]=lines[selectedLine][0];
            current[1]=lines[selectedLine][1];
            current[2]=lines[selectedLine][2];
            current[3]=lines[selectedLine][3];

            boolean foundTop = true;
            boolean foundBottom = true;
            final Vector_Rectangle_Int selected = new Vector_Rectangle_Int(0);
            selected.addElement(lines[selectedLine]);

            while(foundTop){
                foundTop = false;
                for(int i=0; i!=lines.length; i++){
                    if(contains(left, cy+h, lines[i]) || contains(cx, cy+h, lines[i]) || contains(right, cy+h, lines[i])){
                        selected.addElement(lines[i]);
                        foundTop = true;
                        cy = lines[i][1] + (lines[i][3]/2);
                        h = lines[i][3];

                        if(current[0]>lines[i][0]){
                            current[2] = (current[0]+current[2])-lines[i][0];
                            current[0] = lines[i][0];
                        }
                        if((current[0]+current[2])<(lines[i][0]+lines[i][2])) {
                            current[2] = (lines[i][0] + lines[i][2]) - current[0];
                        }
                        if(current[1]>lines[i][1]){
                            current[3] = (current[1]+current[3])-lines[i][1];
                            current[1] = lines[i][1];
                        }
                        if((current[1]+current[3])<(lines[i][1]+lines[i][3])){
                            current[3] = (lines[i][1]+lines[i][3])-current[1];
                        }

                        break;
                    }
                }
            }

            //Return to selected item else we have duplicate highlights
            left = lines[selectedLine][0];
            cx = lines[selectedLine][0]+(lines[selectedLine][2]/2);
            right = lines[selectedLine][0]+lines[selectedLine][2];
            cy = lines[selectedLine][1] + (lines[selectedLine][3]/2);
            h = lines[selectedLine][3];

            while(foundBottom){
                foundBottom = false;
                for(int i=0; i!=lines.length; i++){
                    if(contains(left, cy-h, lines[i]) || contains(cx,cy-h, lines[i]) || contains(right,cy-h, lines[i])){
                        selected.addElement(lines[i]);
                        foundBottom = true;
                        cy = lines[i][1] + (lines[i][3]/2);
                        h = lines[i][3];

                        if(current[0]>lines[i][0]){
                            current[2] = (current[0]+current[2])-lines[i][0];
                            current[0] = lines[i][0];
                        }
                        if((current[0]+current[2])<(lines[i][0]+lines[i][2])) {
                            current[2] = (lines[i][0] + lines[i][2]) - current[0];
                        }
                        if(current[1]>lines[i][1]){
                            current[3] = (current[1]+current[3])-lines[i][1];
                            current[1] = lines[i][1];
                        }
                        if((current[1]+current[3])<(lines[i][1]+lines[i][3])){
                            current[3] = (lines[i][1]+lines[i][3])-current[1];
                        }

                        break;
                    }
                }
            }
            selected.trim();
            addHighlights(selected.get(), true, page);
            return current;
        }
        return null;
    }
    return null;
}

    public void addToLineAreas(final int[] area, final int writingMode, final int page) {
        boolean addNew = true;

        if(lineAreas==null){ //If null, create array

            //Set area
            lineAreas = new HashMap<Integer, int[][]>();
            lineAreas.put(page, new int[][]{area});

            //Set writing direction
            lineWritingMode = new HashMap<Integer, int[]>();
            lineWritingMode.put(page, new int[]{writingMode});

        }else{
            final int[][] lastAreas = lineAreas.get(page);
            final int[] lastWritingMode = (lineWritingMode.get(page));

            //Check for objects close to or intersecting each other
            if(area!=null){ //Ensure actual area is selected
                if(lastAreas!=null){
                    for(int i=0; i!= lastAreas.length; i++){
                        final int lwm = lastWritingMode[i];
                        int cx = area[0];
                        int cy = area[1];
                        int cw = area[2];
                        int ch = area[3];
                        //int cm = cy+(ch/2);

                        int lx = lastAreas[i][0];
                        int ly = lastAreas[i][1];
                        int lw = lastAreas[i][2];
                        int lh = lastAreas[i][3];
                        //int lm = ly+(lh/2);

                        final int currentBaseLine;
                        final int lastBaseLine;
                        final float heightMod = 5f;
                        final float widthMod = 1.1f;

                        switch(writingMode){
                            case PdfData.HORIZONTAL_LEFT_TO_RIGHT :

                                if(lwm== writingMode && ((ly>(cy-(ch/heightMod))) && (ly<(cy+(ch/heightMod)))) && //Ensure this is actually the same line and are about the same size
                                        (((lh<ch+(ch/heightMod) && lh>ch-(ch/heightMod))) && //Check text is the same height
                                                (((lx>(cx + cw-(ch*widthMod))) && (lx<(cx + cw+(ch*widthMod)))) || //Check for object at end of this object
                                                        ((lx + lw>(cx-(ch*widthMod))) && (lx + lw<(cx+(ch*widthMod)))) ||//Check for object at start of this object
                                                        intersects(lastAreas[i], area)))//Check to see if it intersects at all
                                        ){
                                    addNew = false;

                                    //No need to reset the writing mode as already set
                                    lastAreas[i]=mergePartLines(lastAreas[i], area);
                                }
                                break;
                            case PdfData.HORIZONTAL_RIGHT_TO_LEFT :

                                lx = lastAreas[i][0];
                                ly = lastAreas[i][1];
                                lw = lastAreas[i][2];
                                lh = lastAreas[i][3];
                                cx = area[0];
                                cy = area[1];
                                cw = area[2];
                                ch = area[3];

                                if(lwm== writingMode && ((ly>(cy-5)) && (ly<(cy+5)) && lh<=(ch+(ch/5)) && lh>=(ch-(ch/5))) && //Ensure this is actually the same line and are about the same size
                                        (((lx>(cx + cw-(ch*0.6))) && (lx<(cx + cw+(ch*0.6)))) || //Check for object at end of this object
                                                ((lx + lw>(cx-(ch*0.6))) && (lx + lw<(cx+(ch*0.6)))) ||//Check for object at start of this object
                                                intersects(lastAreas[i], area))//Check to see if it intersects at all
                                        ){
                                    addNew = false;

                                    //No need to reset the writing mode as already set
                                    lastAreas[i]=mergePartLines(lastAreas[i], area);
                                }
                                break;
                            case PdfData.VERTICAL_TOP_TO_BOTTOM :

                                lx = lastAreas[i][1];
                                ly = lastAreas[i][0];
                                lw = lastAreas[i][3];
                                lh = lastAreas[i][2];
                                cx = area[1];
                                cy = area[0];
                                cw = area[3];
                                ch = area[2];

                                if(lwm== writingMode && ((ly>(cy-5)) && (ly<(cy+5)) && lh<=(ch+(ch/5)) && lh>=(ch-(ch/5))) && //Ensure this is actually the same line and are about the same size
                                        (((lx>(cx + cw-(ch*0.6))) && (lx<(cx + cw+(ch*0.6)))) || //Check for object at end of this object
                                                ((lx + lw>(cx-(ch*0.6))) && (lx + lw<(cx+(ch*0.6)))) ||//Check for object at start of this object
                                                intersects(lastAreas[i], area))//Check to see if it intersects at all
                                        ){
                                    addNew = false;

                                    //No need to reset the writing mode as already set
                                    lastAreas[i]=mergePartLines(lastAreas[i], area);
                                }

                                break;

                            case PdfData.VERTICAL_BOTTOM_TO_TOP :

                                //Calculate the coord value at the bottom of the text
                                currentBaseLine = cx + cw;
                                lastBaseLine = lx + lw;

                                if(
                                        lwm== writingMode //Check the current writing mode
                                                && (currentBaseLine >= (lastBaseLine-(lw/3))) && (currentBaseLine <= (lastBaseLine+(lw/3))) //Check is same line
                                                && //Only check left or right if the same line is shared
                                                (
                                                        ( //Check for text on either side
                                                                ((ly+(lh+(lw*0.6))>cy) && (ly+(lh-(lw*0.6))<cy))// Check for text to left of current area
                                                                        || ((ly+(lw*0.6)>(cy+ch)) && (ly-(lw*0.6)<(cy+ch)))// Check for text to right of current area
                                                        )
                                                                || intersects(area, lastAreas[i])
                                                )
                                        ){
                                    addNew = false;

                                    //No need to reset the writing mode as already set
                                    lastAreas[i]=mergePartLines(lastAreas[i], area);
                                }

                                break;

                        }

                    }
                }else{
                    addNew = true;
                }

                //If no object near enough to merge, start a new area
                if(addNew){

                    final int[][] localLineAreas;
                    final int[] localLineWritingMode;

                    if(lastAreas!=null){
                        localLineAreas = new int[lastAreas.length+1][4];
                        for(int i=0; i!= lastAreas.length; i++){
                            localLineAreas[i] = lastAreas[i];
                        }
                        localLineAreas[localLineAreas.length-1] = area;

                        localLineWritingMode = new int[lastWritingMode.length+1];
                        for(int i=0; i!= lastWritingMode.length; i++){
                            localLineWritingMode[i] = lastWritingMode[i];
                        }
                        localLineWritingMode[localLineWritingMode.length-1] = writingMode;

                    }else{
                        localLineAreas = new int[1][];
                        localLineAreas[0] = area;

                        localLineWritingMode = new int[1];
                        localLineWritingMode[0] = writingMode;
                    }

                    //Set area
                    this.lineAreas.put(page, localLineAreas);

                    //Set writing direction
                    this.lineWritingMode.put(page, localLineWritingMode);
                }

            }
        }
    }

    /**
     * remove zone on page for text localAreas if present
     * @param rectArea Text area to remove
     * @param page Page to check for these localAreas
     */
    public void removeFoundTextArea(final int[] rectArea, final int page){

        //clearHighlights();
        if(rectArea==null|| areas==null) {
            return;
        }

        final Integer p = page;
        final int[][] localAreas = this.areas.get(p);
        if(localAreas!=null){
            final int size=localAreas.length;
            for(int i=0;i<size;i++){
                if(localAreas[i]!=null && (contains(rectArea[0],rectArea[1], localAreas[i]) || (localAreas[i][0] ==rectArea[0] && localAreas[i][1] ==rectArea[1] && localAreas[i][2] ==rectArea[2] &&
                        localAreas[i][3] ==rectArea[3]))){
                    localAreas[i]=null;
                    i=size;
                }
            }
            this.areas.put(p, localAreas);
            
            //Flag that highlights have changed
            hasHighlightAreasUpdated = true;
        }
    }
   
    /**
     * remove highlight zones on page for text localAreas on single pages null value will totally reset
     * @param rectArea Text localAreas to remove
     * @param page Page to check for these localAreas
     */
    public void removeFoundTextAreas(final int[][] rectArea, final int page){

        if(rectArea==null){
            areas=null;
        }else{
            for (final int[] aRectArea : rectArea) {
                removeFoundTextArea(aRectArea, page);
            }
            boolean allNull = true;
            final Integer p = page;
            int[][] localAreas = this.areas.get(p);
            if(localAreas!=null){
                for(int ii=0;ii<localAreas.length;ii++){
                    if(localAreas[ii]!=null){
                        allNull=false;
                        ii=localAreas.length;
                    }
                }
                if(allNull){
                    localAreas = null;
                    this.areas.put(p, localAreas);

                    //Flag that highlights have changed
                    hasHighlightAreasUpdated = true;
                }
            }
        }
    }
   
    /**
     * Clear all highlights that are being displayed
     */
    public void clearHighlights(){
        
        areas = null;
            
        //Flag that highlights have changed
        hasHighlightAreasUpdated = true;

    }

    /**
     * Method to highlight text on page.
     *
     * If areaSelect = true then the Rectangle array will be highlgihted on screen unmodified.
 areaSelect should be true if being when used with values returned from the search as these localAreas
 are already corrected and modified for display.

 If areaSelect = false then all lines between the top left point and bottom right point
 will be selected including two partial lines the top line starting from the top left point of the rectangle
 and the bottom line ending at the bottom right point of the rectangle.
     *
     * @param highlights :: The 2DArray contains the raw x,y,w,h params of a set of rectangles that you wish to have highlighted
     * @param areaSelect :: The flag that will either select text as line between points if false or characters within an area if true.
     * @param page       :: The page to add highlights to.
     */
    public void addHighlights(final int[][] highlights, final boolean areaSelect, final int page){

        if(highlights!=null){ //If null do nothing to clear use the clear method

        	//Flag that highlights have changed
            hasHighlightAreasUpdated = true;
            
            if(!areaSelect){
                //Ensure highlighting takes place
//				boolean nothingToHighlight = false;

                for(int j=0; j!=highlights.length; j++){
                    if(highlights[j]!=null){

                        //Ensure that the points are adjusted so that they are within line area if that is sent as rectangle
                        int[] startPoint = {highlights[j][0]+1, highlights[j][1]+1};
                        int[] endPoint = {highlights[j][0]+highlights[j][2]-1, highlights[j][1]+highlights[j][3]-1};
                        //both null flushes localAreas

                        if(areas==null){
                            areas = new HashMap<Integer, int[][]>();
                        }

                        final int[][] lines = getLineAreasAs2DArray(page);
                        final int[] writingMode = this.getLineWritingMode(page);

                        int start = -1;
                        int finish = -1;
                        boolean backward = false;
                        //Find the first selected line and the last selected line.
                        if(lines!=null){
                            for(int i=0; i!= lines.length; i++){
                                if(contains(startPoint[0], startPoint[1], lines[i])) {
                                    start = i;
                                }

                                if(contains(endPoint[0], endPoint[1], lines[i])) {
                                    finish = i;
                                }

                                if(start!=-1 && finish!=-1){
                                    break;
                                }
                            }

                            if(start>finish){
                                final int temp = start;
                                start = finish;
                                finish = temp;
                                backward = true;
                            }

                            if(start==finish){
                                if(startPoint[0]>endPoint[0]){
                                    final int[] temp = startPoint;
                                    startPoint = endPoint;
                                    endPoint = temp;
                                }
                            }

                            if(start!=-1 && finish!=-1){
                                //Fill in all the lines between
                                final Integer p = page;
                                final int[][] localAreas = new int[finish-start+1][4];

                                System.arraycopy(lines, start + 0, localAreas, 0, finish - start + 1);

                                if(localAreas.length>0){
                                    final int top = 0;
                                    final int bottom = localAreas.length-1;

                                    if(localAreas[top]!=null && localAreas[bottom]!=null){

                                        switch(writingMode[start]){
                                            case PdfData.HORIZONTAL_LEFT_TO_RIGHT :
                                                // if going backwards
                                                if(backward){
                                                    if((endPoint[0]-15)<=localAreas[top][0]){
                                                        //Do nothing to localAreas as we want to pick up the start of a line
                                                    }else{
                                                        localAreas[top][2] -= (endPoint[0]-localAreas[top][0]);
                                                        localAreas[top][0] = endPoint[0];
                                                    }

                                                }else{
                                                    if((startPoint[0]-15)<=localAreas[top][0]){
                                                        //Do nothing to localAreas as we want to pick up the start of a line
                                                    }else{
                                                        localAreas[top][2] -= (startPoint[0]-localAreas[top][0]);
                                                        localAreas[top][0] = startPoint[0];
                                                    }

                                                }
                                                break;
                                            case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
                                                LogWriter.writeLog("THIS TEXT DIRECTION HAS NOT BEEN IMPLEMENTED YET (Right to Left)");
                                                break;
                                            case PdfData.VERTICAL_TOP_TO_BOTTOM:
                                                if(backward){
                                                    if((endPoint[1]-15)<=localAreas[top][1]){
                                                        //Do nothing to localAreas as we want to pick up the start of a line
                                                    }else{
                                                        localAreas[top][3] -= (endPoint[1]-localAreas[top][1]);
                                                        localAreas[top][1] = endPoint[1];
                                                    }

                                                }else{
                                                    if((startPoint[1]-15)<=localAreas[top][1]){
                                                        //Do nothing to localAreas as we want to pick up the start of a line
                                                    }else{
                                                        localAreas[top][3] -= (startPoint[1]-localAreas[top][1]);
                                                        localAreas[top][1] = startPoint[1];
                                                    }

                                                }
                                                break;
                                            case PdfData.VERTICAL_BOTTOM_TO_TOP :
                                                if(backward){
                                                    if((endPoint[1]-15)<=localAreas[top][1]){
                                                        //Do nothing to localAreas as we want to pick up the start of a line
                                                    }else{
                                                        localAreas[top][3] -= (endPoint[1]-localAreas[top][1]);
                                                        localAreas[top][1] = endPoint[1];
                                                    }

                                                }else{
                                                    if((startPoint[1]-15)<=localAreas[top][1]){
                                                        //Do nothing to localAreas as we want to pick up the start of a line
                                                    }else{
                                                        localAreas[top][3] -= (startPoint[1]-localAreas[top][1]);
                                                        localAreas[top][1] = startPoint[1];
                                                    }

                                                }
                                                break;
                                        }


                                        switch(writingMode[finish]){
                                            case PdfData.HORIZONTAL_LEFT_TO_RIGHT :
                                                // if going backwards
                                                if(backward){
                                                    if((startPoint[0]+15)>=localAreas[bottom][0]+localAreas[bottom][2]){
                                                        //Do nothing to localAreas as we want to pick up the end of a line
                                                    }else{
                                                        localAreas[bottom][2] = startPoint[0] - localAreas[bottom][0];
                                                    }

                                                }else{
                                                    if((endPoint[0]+15)>=localAreas[bottom][0]+localAreas[bottom][2]){
                                                        //Do nothing to localAreas as we want to pick up the end of a line
                                                    }else {
                                                        localAreas[bottom][2] = endPoint[0] - localAreas[bottom][0];
                                                    }
                                                }
                                                break;
                                            case PdfData.HORIZONTAL_RIGHT_TO_LEFT:
                                                LogWriter.writeLog("THIS TEXT DIRECTION HAS NOT BEEN IMPLEMENTED YET (Right to Left)");
                                                break;
                                            case PdfData.VERTICAL_TOP_TO_BOTTOM:
                                                // if going backwards
                                                if(backward){
                                                    if((startPoint[1]+15)>=localAreas[bottom][1]+localAreas[bottom][3]){
                                                        //Do nothing to localAreas as we want to pick up the end of a line
                                                    }else{
                                                        localAreas[bottom][3] = startPoint[1] - localAreas[bottom][1];
                                                    }

                                                }else{
                                                    if((endPoint[1]+15)>=localAreas[bottom][1]+localAreas[bottom][3]){
                                                        //Do nothing to localAreas as we want to pick up the end of a line
                                                    }else {
                                                        localAreas[bottom][3] = endPoint[1] - localAreas[bottom][1];
                                                    }
                                                }
                                                break;
                                            case PdfData.VERTICAL_BOTTOM_TO_TOP :
                                                // if going backwards
                                                if(backward){
                                                    if((startPoint[1]+15)>=localAreas[bottom][1]+localAreas[bottom][3]){
                                                        //Do nothing to localAreas as we want to pick up the end of a line
                                                    }else{
                                                        localAreas[bottom][3] = startPoint[1] - localAreas[bottom][1];
                                                    }

                                                }else{
                                                    if((endPoint[1]+15)>=localAreas[bottom][1]+localAreas[bottom][3]){
                                                        //Do nothing to localAreas as we want to pick up the end of a line
                                                    }else {
                                                        localAreas[bottom][3] = endPoint[1] - localAreas[bottom][1];
                                                    }
                                                }
                                                break;
                                        }
                                    }
                                }
                                this.areas.put(p, localAreas);
                            }
//							else {
//								//This is the first highlight and nothing was selected
//								if(nothingToHighlight){
//									System.out.println("Area == null");
//									//Prevent text extraction on nothing
//									this.localAreas = null;
//								}
//							}
                        }
                    }
                }
            }else{
                //if inset add in difference transparently
                for(int v=0; v!=highlights.length; v++){
                    if(highlights[v]!=null){
                        if(highlights[v][2]<0){
                            highlights[v][2] = -highlights[v][2];
                            highlights[v][0] -=highlights[v][2];
                        }

                        if(highlights[v][3]<0){
                            highlights[v][3] = -highlights[v][3];
                            highlights[v][1] -=highlights[v][3];
                        }

                        if(areas!=null){
                            final Integer p = page;
                            int[][] localAreas = this.areas.get(p);
                            if(localAreas!=null){
                                boolean matchFound=false;

                                //see if already added
                                final int size=localAreas.length;
                                for(int i=0;i<size;i++){
                                    if(localAreas[i]!=null){
                                        //If area has been added before please ignore
                                        if(localAreas[i]!=null && (localAreas[i][0] ==highlights[v][0] && localAreas[i][1] ==highlights[v][1] && localAreas[i][2] ==highlights[v][2] &&
                                                localAreas[i][3] ==highlights[v][3])){
                                            matchFound=true;
                                            i=size;
                                        }
                                    }
                                }

                                if(!matchFound){
                                    final int newSize=localAreas.length+1;
                                    final int[][] newAreas=new int[newSize][4];
                                    for(int i=0;i<localAreas.length;i++){
                                        if(localAreas[i]!=null) {
                                            newAreas[i] = new int[]{localAreas[i][0], localAreas[i][1], localAreas[i][2], localAreas[i][3]};
                                        }
                                    }
                                    localAreas = newAreas;

                                    localAreas[localAreas.length-1] = highlights[v];
                                }
                                this.areas.put(p, localAreas);
                            }else{
                                this.areas.put(p, highlights);
                            }
                        }else{
                            areas = new HashMap<Integer, int[][]>();
                            final Integer p = page;
                            final int[][] localAreas = new int[1][4];
                            localAreas[0] = highlights[v];
                            this.areas.put(p, localAreas);
                        }
                    }
                }
            }
        }
    }


    public boolean hasHighlightAreasUpdated() {
		return hasHighlightAreasUpdated;
	}

    /**
     * Get all the highlights currently stored. The returned Map 
     * using the page numbers as the keys for the values.
     * 
     * @return A Map containing all highlights currently stored.
     */
    public Map getAllHighlights(){
    	hasHighlightAreasUpdated = false;
        if(areas==null){
            return null;
        }else{
            return Collections.unmodifiableMap(areas);
        }
    }
    
    /**
     * Creates a two-dimensional int array containing x,y,width and height
 values for each rectangle that is stored in the localAreas map,
 which allows us to create a swing/fx rectangle on these values.
     * @param page of type int.
     * @return an int[][] Containing x,y,w,h of Highlights on Page.
     */
    public int[][] getHighlightedAreasAs2DArray(final int page) {

        if(areas==null) {
            return null;
        } else{
            final Integer p = page;
            final int[][] localAreas = this.areas.get(p);
            if(localAreas!=null){
                final int count=localAreas.length;

                final int[][] returnValue=new int[count][4];

                for(int ii=0;ii<count;ii++){
                    if(localAreas[ii]==null) {
                        returnValue[ii] = null;
                    } else {
                        returnValue[ii] = new int[]{localAreas[ii][0], localAreas[ii][1],
                                localAreas[ii][2], localAreas[ii][3]};
                    }
                }
                
                //Reset flag as localAreas has been retrieved
                hasHighlightAreasUpdated = false;
                
                return returnValue;
            }else{
                return null;
            }
        }

    }
    

    public void setLineAreas(final Map<Integer, int[][]> la) {
        lineAreas = la;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setLineWritingMode(final Map<Integer, int[]> lineOrientation) {
        lineWritingMode = lineOrientation;
    }


    /**
     * Creates a two-dimensional int array containing x,y,width and height
 values for each rectangle that is stored in the localLineAreas map,
 which allows us to create a swing/fx rectangle on these values.
     * @param page of type int.
     * @return an int[][] Containing x,y,w,h of line localAreas on Page.
     */
    public int[][] getLineAreasAs2DArray(final int page){
        
        if(lineAreas==null || lineAreas.get(page) == null) {
            return null;
        } else{
            final int[][] localLineAreas = this.lineAreas.get(page);

            if(localLineAreas==null) {
                return null;
            }

            final int count=localLineAreas.length;

            final int[][] returnValue=new int[count][4];

            for(int ii=0;ii<count;ii++){
                if(localLineAreas[ii]==null) {
                    returnValue[ii] = null;
                } else {
                    returnValue[ii] = new int[]{localLineAreas[ii][0], localLineAreas[ii][1],
                            localLineAreas[ii][2], localLineAreas[ii][3]};
                }
            }
            
            return returnValue;
        }
        
    }
    
    public int[] getLineWritingMode(final int page) {

        if(lineWritingMode==null) {
            return null;
        } else{
            final int[] localLineWritingMode = (this.lineWritingMode.get(page));

            if(localLineWritingMode==null) {
                return null;
            }

            final int count=localLineWritingMode.length;

            final int[] returnValue=new int[count];

            System.arraycopy(localLineWritingMode, 0, returnValue, 0, count);

            return returnValue;
        }
    }

    private static int[] mergePartLines(final int[] lastArea, final int[] area){
        /*
         * Check coords from both areas and merge them to make
         * a single larger area containing contents of both
         */
        final int x1 =area[0];
        final int x2 =area[0] + area[2];
        final int y1 =area[1];
        final int y2 =area[1] + area[3];
        final int lx1 =lastArea[0];
        final int lx2 =lastArea[0] + lastArea[2];
        final int ly1 =lastArea[1];
        final int ly2 =lastArea[1] + lastArea[3];

        //Ensure the highest and lowest values are selected
        if(x1<lx1) {
            area[0] = x1;
        } else {
            area[0] = lx1;
        }

        if(y1<ly1) {
            area[1] = y1;
        } else {
            area[1] = ly1;
        }

        if(y2>ly2) {
            area[3] = y2 - area[1];
        } else {
            area[3] = ly2 - area[1];
        }

        if(x2>lx2) {
            area[2] = x2 - area[0];
        } else {
            area[2] = lx2 - area[0];
        }

        return area;
    }
    
    /**
     * Checks whether two rectangles intersect
     * Takes the raw x,y,w,h data of the rectangles in array form.
     * @param paramsOne
     * @param paramsTwo
     * @return boolean
     */
    public static boolean intersects(final int[] paramsOne, final int[] paramsTwo){
        
        final int X1 = paramsOne[0];
        final int Y1 = paramsOne[1];
        final int W1 = paramsOne[2];
        final int H1 = paramsOne[3];
        final int X2 = paramsTwo[0];
        final int Y2 = paramsTwo[1];
        final int W2 = paramsTwo[2];
        final int H2 = paramsTwo[3];

        return !(X1 + W1 < X2 || X2 + W2 < X1 || Y1 + H1 < Y2 || Y2 + H2 < Y1);
    }

    /**
     * Checks whether a point at (x,y) lies within the
     * bounds of an unrotated rectangles raw x,y,w,h values.
     * @return
     */
    private static boolean contains(final int x, final int y, final int[] rectParams){
        
        final int minX = rectParams[0]; //x
        final int minY = rectParams[1]; //y
        final int maxX = rectParams[0] + rectParams[2]; //x + width
        final int maxY = rectParams[1] + rectParams[3]; //y + height

        return (x >= minX && x <= maxX) && (y >= minY && y <= maxY);
    }

}
