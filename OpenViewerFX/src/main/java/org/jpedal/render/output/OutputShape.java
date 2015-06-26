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
 * OutputShape.java
 * ---------------
 */

package org.jpedal.render.output;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.Cmd;

public abstract class OutputShape {

	protected final List<String> pathCommands;
	protected final Rectangle2D cropBox;
	private final Point2D midPoint;

	private final int pageRotation;
    protected final int pageNumber;
	protected final float scaling;// applied to the whole page. Default= 1

	private int pathCommand;
	private final Shape currentShape;

    protected int cmd=-1;// Cmd.F or Cmd.S or Cmd.B for type of shape or Cmd.Tj for shapetext

    private Integer decimalsAllowed;
    protected double pow;

    private boolean isFilled;
    private boolean evenStroke = true;
    
    public OutputShape(final int cmd, final float scaling, final Shape currentShape, final Point2D midPoint,
                       final Rectangle2D cropBox, final int pageRotation, final int pageNumber, final Integer decimalsAllowed) {

        //allow user to trade size versus accuracy on images
        this.decimalsAllowed = decimalsAllowed;
        if(decimalsAllowed != null && decimalsAllowed > 0){
            pow = Math.pow(10, decimalsAllowed);
        }

		this.cmd = cmd;

		this.currentShape = currentShape;
		this.scaling = scaling;

        if (cmd == Cmd.Tj) {
            this.pageRotation = 0;// Do not rotate the shape if drawing text as shapes
        } else {
            this.pageRotation = pageRotation;
        }
        this.cropBox = cropBox;
        this.midPoint = midPoint;
        this.pageNumber = pageNumber;

		pathCommands = new ArrayList<String>();
	}

	protected void generateShapeFromG2Data(final GraphicsState gs) {
		
		/** Start of fix for pixel perfect lines. Ask Leon if confused **/
        isFilled = gs.getFillType() == GraphicsState.FILL;
        double ctmAdjustedLineWidth = gs.getCTMAdjustedLineWidth();
        if (ctmAdjustedLineWidth == 0) {
            ctmAdjustedLineWidth = 0.1;//Another crazy PDF feature. Even if the PDF specifies lineWidth=0, draw the line anyway. Case 15799.
        }
        final int strokeWidth = (int)((ctmAdjustedLineWidth * (scaling))+0.99); // It is possible it may be worth dividing the scaling by 1.33f (To get same thickness as PDF @ 100%)
        evenStroke = strokeWidth != 0 && strokeWidth % 2 == 0;
        gs.setOutputLineWidth(strokeWidth);
		/** End of fix for pixel perfect lines **/

        if (decimalsAllowed == null) {
            decimalsAllowed = getDecimalsAllowed(currentShape);
            if(decimalsAllowed > 0){
                pow = Math.pow(10, decimalsAllowed);
            }
        }

		final PathIterator it = currentShape.getPathIterator(null);

        beginShape();

		while(!it.isDone()) {
			final double[] coords = {0,0,0,0,0,0};

			pathCommand = it.currentSegment(coords);

            if (pathCommand == PathIterator.SEG_CUBICTO) {
                bezierCurveTo(coords);
			} else if (pathCommand == PathIterator.SEG_LINETO) {
                lineTo(coords);
			} else if (pathCommand == PathIterator.SEG_QUADTO) {
                quadraticCurveTo(coords);
			} else if (pathCommand == PathIterator.SEG_MOVETO) {
                moveTo(coords);
			} else if (pathCommand == PathIterator.SEG_CLOSE) {
				closePath();
			}
			it.next();
		}

		if(!containsDrawCommands()) {
			pathCommands.clear();
		} else {
			applyGraphicsStateToPath(gs);
		}
	}

    public abstract boolean containsDrawCommands();

	protected abstract void moveTo(final double[] coords);

	protected abstract void quadraticCurveTo(final double[] coords);

	protected abstract void lineTo(final double[] coords);

	protected abstract void closePath();

	protected abstract void beginShape();

	protected abstract void bezierCurveTo(final double[] coords);

    /**
     * This method is checking whether the shape consists only of vertical and horizontal lines.
     * If that is the case, we can align the shape to the pixel grid which will prove sharper lines, for example around
     * tables.
     * If the shape contains anything other than vertical and horizontal lines, then 1 decimal place will be used.
     * @param shape Shape to check
     * @return Number of decimals to use in output
     */
    private static int getDecimalsAllowed(final Shape shape) {

        final PathIterator it = shape.getPathIterator(null);

        double lastX = 0, lastY = 0;

        while(!it.isDone()) {
            final double[] coords = {0,0,0,0,0,0};

            final int pathCommand = it.currentSegment(coords);

            if (pathCommand == PathIterator.SEG_CUBICTO) {
                return 1;
            } else if (pathCommand == PathIterator.SEG_LINETO) {
                if (coords[0] != lastX && coords[1] != lastY) {
                    return 1;
                }
                lastX = coords[0];
                lastY = coords[1];
            } else if (pathCommand == PathIterator.SEG_MOVETO) {
                lastX = coords[0];
                lastY = coords[1];
            }
            it.next();
        }

        return 0;
    }

	/**
	 * trim if needed
	 * @param i value to set precision on
	 * @return value with precision set
	 *
	 * (note duplicate in HTMLDisplay)
	 */
	private double setPrecision(final double i) {
		
		// New pixel perfect version
		final double value;

        if (decimalsAllowed==0) {
            // Rectangles require int precision, whereas lines require .5 treatment.
            // Because clips contain lines with .5 treatment, we need to give clips .5 treatment to avoid incorrectly
            // clipping lines we have given .5 treatment.
            if (this.cmd != Cmd.n && (isFilled || evenStroke)) {
                value = (int) (i);
            } else {
                final int num = (int) i;
                value = num + 0.5d;
            }
        } else if(decimalsAllowed<0) {
            value= i;
        } else {
            value= ((int) ((i * pow) + 0.5f)) / pow;
        }

		return value;
	}
	


	/**
	 * Extracts information out of graphics state to use in HTML - empty version
	 */
	protected abstract void applyGraphicsStateToPath(final GraphicsState gs);

	/**
	 * Extract line cap attribute
	 */
	protected static String determineLineCap(final BasicStroke stroke)
	{
		//attribute DOMString lineCap; // "butt", "round", "square" (default "butt")
		final String attribute;

		switch(stroke.getEndCap()) {
		case(BasicStroke.CAP_ROUND):
			attribute = "round";
		break;
		case(BasicStroke.CAP_SQUARE):
			attribute = "square";
		break;
		default:
			attribute = "butt";
			break;
		}
		return attribute;
	}

	/**
	 * Extract line join attribute
	 */
	protected static String determineLineJoin(final BasicStroke stroke)
	{
		//attribute DOMString lineJoin; // "round", "bevel", "miter" (default "miter")
		final String attribute;
		switch(stroke.getLineJoin()) {
		case(BasicStroke.JOIN_ROUND):
			attribute = "round";
		break;
		case(BasicStroke.JOIN_BEVEL):
			attribute = "bevel";
		break;
		default:
			attribute = "miter";
			break;
		}
		return attribute;
	}

    /**
	 * Convert from PDF coords to java coords.
	 */
	private double[] correctCoords(final double[] coords) {

        if(cmd != Cmd.Tj){
            int offset;

            switch(pathCommand) {
            case(PathIterator.SEG_CUBICTO):
                offset = 4;
            break;
            case(PathIterator.SEG_QUADTO):
                offset = 2;
            break;
            default:
                offset = 0;
                break;
            }

            //ensure fits
            if(offset>coords.length) {
                offset = coords.length - 2;
            }

            for(int i = 0; i < offset + 2; i+=2) {

                coords[i] -= midPoint.getX();
                coords[i] += cropBox.getWidth() / 2;

                coords[i+1] -= midPoint.getY();
                coords[i+1] = 0 - coords[i+1];
                coords[i+1] += cropBox.getHeight() / 2;

            }
        }

		return coords;
	}

	/**
	 *
	 * Removes cropbox offset.
	 *
	 * @param coords Numbers to change
	 * @param count Use up to count doubles from coords array
	 * @return String Bracketed stringified version of coords
	 * (note numbers rounded to nearest int to keep down filesize)
	 */
	protected double[] removeCropboxOffset(double[] coords, final int count) {
		//make copy factoring in size
		final int size=coords.length;
		final double[] copy=new double[size];
		System.arraycopy(coords, 0, copy, 0, size);

		coords=correctCoords(copy);

		return convertCoords(coords, count);

	}

    /**
     * Puts a comma between the coords
     * Chops off any .0's
     * @param coords Coordinates to have a comma put between
     * @return Coords with comma put between them
     */
    protected static String convertCoordsToOutputString(final double[] coords) {
        final StringBuilder result =new StringBuilder();
        for (int i = 0; i < coords.length; i++) {

            if (coords[i] == (int)coords[i]) {
                result.append((int)coords[i]);
            } else {
                result.append(coords[i]);
            }
            if (i < coords.length - 1) {
                result.append(',');
            }
        }
        return result.toString();
    }

    private double[] convertCoords(final double[] coords, final int count) {

        final double[] result = new double[count];

        final double cropBoxW = cropBox.getWidth();
        final double cropBoxH = cropBox.getHeight();

        switch(pageRotation){
            case 90:
                //for each set of coordinates, set value
                for(int i = 0; i<count; i += 2) {
                    result[i] = setPrecision(((cropBoxH-coords[i+1])*scaling));
                    result[i + 1] = setPrecision(coords[i]*scaling);
                }
                break;

            case 180:
                //convert x and y values to output coords from PDF
                for(int i = 0; i<count; i += 2) {
                    result[i] = setPrecision((cropBoxW-coords[i])*scaling);
                    result[i + 1] = setPrecision(((cropBoxH-coords[i+1])*scaling));
                }
                break;

            case 270:
                //for each set of coordinates, set value
                for(int i = 0; i<count; i += 2) {
                    result[i] = setPrecision(((coords[i+1])*scaling));
                    result[i + 1] = setPrecision((cropBoxW-coords[i])*scaling);
                }
                break;

            default:
                //convert x and y values to output coords from PDF
                for(int i = 0; i<count; i += 2) {
                    result[i] = setPrecision(coords[i]*scaling);
                    result[i + 1] = setPrecision((coords[i+1]*scaling));
                }
                break;
        }
        return result;
    }

	public boolean isEmpty() {
		return !containsDrawCommands();
	}

}
