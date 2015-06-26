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
 * CharstringContext.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.fonts.StandardFonts;

import org.jpedal.utils.repositories.FastByteArrayOutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@SuppressWarnings("PointlessBooleanExpression")
class CharstringContext {

    private boolean isType1=true;
    private CFFWriter converter;
    private int currentCharStringID;
    private ArrayList<CharstringElement> currentCharString;
    private ArrayList<CharstringElement> currentCharStringHints;
    private int hintsIncluded;
    private CharstringElement currentFlexCommand;
    private int[] lsbX, lsbY, widthX, widthY;
    private int[] charstringXDisplacement, charstringYDisplacement;
    private String[] glyphNames;
    private boolean inFlex;
    private boolean widthTaken;
    private CharstringElement width;
    private boolean inSeac;
    private boolean firstArgsAdded;
    byte[][] subrs;

    /**
     * Create a context in which type 2 charstrings elements can be read.
     */
    CharstringContext() {
        isType1 = false;
    }


    /**
     * Create a context in which charstring elements to be read and converted from Type 1 to Type 2 in.
     * @param lsbX An array to hold the x components of the left side bearings for the glyphs.
     * @param lsbY An array to hold the y components of the left side bearings for the glyphs.
     * @param widthX An array to hold the horizontal widths of the glyphs.
     * @param widthY An array to hold the vertical widths of the glyphs.
     * @param charstringXDisplacement An array to hold the total x displacements of the glyphs.
     * @param charstringYDisplacement An array to hold the total y displacements of the glyphs.
     * @param glyphNames An array containing the names of the glyphs.
     * @param subrs An array containing the subroutines of the font.
     * @param converter The converter to use for the subroutines.
     */
    CharstringContext(final int[] lsbX, final int[] lsbY, final int[] widthX, final int[] widthY,
                             final int[] charstringXDisplacement, final int[] charstringYDisplacement,
                             final String[] glyphNames, final byte[][] subrs, final CFFWriter converter) {
        this.lsbX = lsbX;
        this.lsbY = lsbY;
        this.widthX = widthX;
        this.widthY = widthY;
        this.charstringXDisplacement = charstringXDisplacement;
        this.charstringYDisplacement = charstringYDisplacement;
        this.glyphNames = glyphNames;
        this.subrs = subrs;
        this.converter = converter;
    }

    /**
     * Initialise the context for a new charstring.
     * @param charstringNo The number of the charstring to create.
     */
    public void setCharstring(final int charstringNo) {
        currentCharString = new ArrayList<CharstringElement>();
        currentCharStringHints = new ArrayList<CharstringElement>();
        hintsIncluded = 0;
        currentCharStringID = charstringNo;
        inFlex = false;
        inSeac = false;
        widthTaken = false;
        width = null;
        firstArgsAdded = false;
    }

    /**
     * Create a charstring element from data at a specified position in the byte stream.
     * @param charstring The byte array of the charstring to use.
     * @param pos Where in the byte array to look.
     * @return A charstring element representing the element at the specified position.
     */
    public CharstringElement createElement(final int[] charstring, final int pos) {
        return new CharstringElement(charstring, pos);
    }

    /**
     * @return whether we are currently creating a composite glyph
     */
    public boolean inSeac() {
        return inSeac;
    }

    /**
     * Fetch the charstring which has just been created.
     * @return The completed charstring
     */
    public ArrayList<CharstringElement> getCurrentCharString() {

        final ArrayList<CharstringElement> result = new ArrayList<CharstringElement>();

        //Add in width if needed (type1 does this later in the process - this is just for 1c.)
        if (!isType1 && width != null) {
            result.add(width);
        }

        //Sort hints
        final CharstringElement[] hints = currentCharStringHints.toArray(new CharstringElement[currentCharStringHints.size()]);
        Arrays.sort(hints);

        //Add hints and charstring & return
        result.addAll(Arrays.asList(hints));
        result.addAll(currentCharString);

        //Add endchar if needed
        if (!"endchar".equals(result.get(result.size()-1).commandName)) {
            result.add(new CharstringElement(new int[]{14}, 0));
        }

        return result;
    }

    /**
     * @return The number of arguments on the stack
     */
    private int countArgs() {
        int result = 0;
        for (final CharstringElement e : currentCharString) {
            if (!e.isCommand) {
                result++;
            }
        }
        return result;
    }

    /**
     * Contains a single element of a charstring - a number, a command, or a result of a command. Also stores arguments.
     */
    public class CharstringElement implements Comparable {

        private boolean isCommand=true;
        private String commandName;
        private int numberValue;
        private int fractionalComponent;
        private byte[] additionalBytes;
        private int length=1;
        private ArrayList<CharstringElement> args = new ArrayList<CharstringElement>();
        private final boolean isResult;
        private CharstringElement parent;

        /**
         * Constructor used for generating an integer parameter.
         * @param number The number this element should represent
         */
        private CharstringElement(final int number) {
            isResult = false;
            isCommand = false;
            numberValue = number;
        }

        /**
         * Constructor used for generating placeholder result elements.
         * @param parent The element this is a result of
         */
        private CharstringElement(final CharstringElement parent) {
            isResult = true;
            isCommand = false;
            this.parent = parent;
            currentCharString.add(this);
        }

        /**
         * Normal constructor used when converting from Type 1 stream.
         * @param charstring byte array to copy from
         * @param pos starting position for this element
         */
        @SuppressWarnings("OverlyLongMethod")
        private CharstringElement(final int[] charstring, final int pos) {
            isResult = false;
            currentCharString.add(this);
            boolean moveToStart = false;

            final int b = charstring[pos];

            if (b >= 32 && b <= 246) {                                      //Single byte number

                numberValue = b - 139;
                isCommand = false;

            } else if((b >= 247 && b <= 250) || (b >= 251 && b <= 254)) {   //Two byte number

                if (b < 251) {
                    numberValue = ((b - 247) * 256) + charstring[pos+1] + 108;
                } else {
                    numberValue = -((b - 251) * 256) - charstring[pos+1] - 108;
                }

                isCommand = false;
                length = 2;

            } else {

                boolean mergePrevious=false;

                switch (b) {
                    case 1:             //hstem
                        commandName = "hstem";
                        moveToStart = true;
                        if (isType1) {
                            claimArguments(2, true, true);
                        } else {
                            final int argsToGet = (countArgs() / 2) * 2;
                            hintsIncluded += argsToGet / 2;
                            claimArguments(argsToGet, true, true);
                        }
                        break;
                    case 3:             //vstem
                        commandName = "vstem";
                        moveToStart = true;
                        if (isType1) {
                            claimArguments(2, true, true);
                        } else {
                            final int argsToGet = (countArgs() / 2) * 2;
                            hintsIncluded += argsToGet / 2;
                            claimArguments(argsToGet, true, true);
                        }
                        break;
                    case 4:             //vmoveto
                        commandName = "vmoveto";
                        claimArguments(1, true, true);

                        //If in a flex section channel arg and 0 into current flex command
                        if (inFlex) {
                            //If second pair found add to the first and store back
                            if (currentFlexCommand.args.size()==2 && !firstArgsAdded) {
                                final int arg0 = currentFlexCommand.args.get(0).numberValue;
                                final int arg1 = args.get(0).numberValue + currentFlexCommand.args.get(1).numberValue;
                                currentFlexCommand.args.clear();
                                currentFlexCommand.args.add(new CharstringElement(arg0));
                                currentFlexCommand.args.add(new CharstringElement(arg1));
                                firstArgsAdded = true;
                            } else {
                                currentFlexCommand.args.add(new CharstringElement(0));
                                currentFlexCommand.args.add(args.get(0));
                            }
                            commandName = "";
                        }
                        break;
                    case 5:             //rlineto
                        commandName = "rlineto";
                        if (isType1) {
                            claimArguments(2, true, true);
                            mergePrevious=true;
                        } else {
                            final int argsToTake = (countArgs()/2)*2;
                            claimArguments(argsToTake, true, true);
                        }
                        break;
                    case 6:             //hlineto
                        commandName = "hlineto";
                        if (isType1) {
                            claimArguments(1, true, true);
                        } else {
                            claimArguments(countArgs(), true, true);
                        }
                        break;
                    case 7:             //vlineto
                        commandName = "vlineto";
                        if (isType1) {
                            claimArguments(1, true, true);
                        } else {
                            claimArguments(countArgs(), true, true);
                        }
                        break;
                    case 8:             //rrcurveto
                        commandName = "rrcurveto";
                        if (isType1) {
                            claimArguments(6, true, true);
                        } else {
                            claimArguments((countArgs() / 6) * 6, true, true);
                        }
                        mergePrevious=true;
                        break;
                    case 9:             //closepath
                        if (isType1) {
                            commandName = "closepath";
                            claimArguments(0, false, true);
                        } else {
                            //
                        }
                        break;
                    case 10:            //callsubr
                        commandName = "callsubr";
                        claimArguments(1, false, false);

                        if (isType1) {

                            final int subrNumber = args.get(0).numberValue;

                            //Handle starting a section of flex code
                            if (!inFlex && subrNumber == 1) {
                                //Repurpose this as flex command and set flag for processing following commands
                                args.clear();
                                commandName = "t1flex";
                                currentFlexCommand = this;
                                inFlex = true;
                            }

                            //Handle subr calls during flex sections
                            if (inFlex && subrNumber >= 0 && subrNumber <= 2) {

                                //Handle endind flex section
                                if (subrNumber == 0) {
                                    claimArguments(3, false, false);
                                    if (args.size() >= 4) {
                                        currentFlexCommand.args.add(args.get(3));
                                    } else {
                                        currentFlexCommand.args.add(new CharstringElement(0));
                                    }
                                    inFlex = false;
                                    firstArgsAdded = false;
                                }

                                //Handle other cases
                            } else {

                                final byte[] rawSubr = subrs[subrNumber];

                                //Deal with top byte being negative
                                final int[] subr = new int[rawSubr.length];
                                for (int i=0; i<rawSubr.length; i++) {
                                    subr[i] = rawSubr[i];
                                    if (subr[i] < 0) {
                                        subr[i] += 256;
                                    }
                                }

                                //Convert to CharstringElements
                                CharstringElement element;
                                for (int i=0; i < subr.length; i+=element.length) {
                                    element = new CharstringElement(subr, i);
                                }
                            }
                        }
                        break;
                    case 11:            //return
                        currentCharString.remove(this);
                        break;
                    case 12:            //2 byte command
                        length = 2;
                        moveToStart = construct2ByteCommand(charstring, pos);
                        break;
                    case 13:            //hsbw
                        if (isType1) {
                            commandName = "hsbw";
                            claimArguments(2, true, true);
                            lsbX[currentCharStringID] = args.get(0).evaluate();
                            widthX[currentCharStringID] = args.get(1).evaluate();

                            //Remove it and any args
                            currentCharString.remove(this);
                            for (final CharstringElement arg : args) {
                                if (arg.isResult) {
                                    currentCharString.remove(arg.parent);
                                }
                            }
                        } else {
                            //
                        }
                        break;
                    case 14:            //endchar
                        commandName = "endchar";
                        if (!isType1 && countArgs() >= 4) {
                            //Allow for deprecated use of endchar in T2 charstrings which effectively uses T1's 'seac'
                            claimArguments(4, true, true);
                        } else {
                            claimArguments(0, false, true);
                        }
                        break;
                    case 18:            //hstemhm
                        if (!isType1) {
                            commandName = "hstemhm";
                            final int argsToGet = (countArgs() / 2) * 2;
                            hintsIncluded += argsToGet / 2;
                            claimArguments(argsToGet, true, true);
                        } else {
                            //
                        }
                        break;
                    case 19:            //hintmask
                        if (!isType1) {
                            commandName = "hintmask";
                            final int argsToGet = (countArgs() / 2) * 2;
                            hintsIncluded += argsToGet / 2;
                            claimArguments(argsToGet, true, true);
                            final int bytesOfData = (hintsIncluded + 7) / 8;
                            length = bytesOfData + 1;
                            additionalBytes = new byte[bytesOfData];
                            for (int i=0; i<bytesOfData; i++) {
                                additionalBytes[i] = (byte)charstring[pos+1+i];
                            }
                        } else {
                            //
                        }
                        break;
                    case 20:            //cntrmask
                        if (!isType1) {
                            commandName = "cntrmask";
                            final int argsToGet = (countArgs() / 2) * 2;
                            hintsIncluded += argsToGet / 2;
                            claimArguments(argsToGet, true, true);
                            final int bytesOfData = (hintsIncluded + 7) / 8;
                            length = bytesOfData + 1;
                            additionalBytes = new byte[bytesOfData];
                            for (int i=0; i<bytesOfData; i++) {
                                additionalBytes[i] = (byte)charstring[pos+1+i];
                            }
                        } else {
                            //
                        }
                        break;
                    case 21:            //rmoveto
                        commandName = "rmoveto";
                        claimArguments(2, true, true);

                        //If in a flex section channel args into current flex command
                        if (inFlex) {
                            //If second pair found add to the first and store back
                            if (currentFlexCommand.args.size()==2 && !firstArgsAdded) {
                                final int arg0 = args.get(0).numberValue + currentFlexCommand.args.get(0).numberValue;
                                final int arg1 = args.get(1).numberValue + currentFlexCommand.args.get(1).numberValue;
                                currentFlexCommand.args.clear();
                                currentFlexCommand.args.add(new CharstringElement(arg0));
                                currentFlexCommand.args.add(new CharstringElement(arg1));
                                firstArgsAdded = true;
                            } else {
                                currentFlexCommand.args.add(args.get(0));
                                currentFlexCommand.args.add(args.get(1));
                            }
                            commandName = "";
                        }
                        break;
                    case 22:            //hmoveto
                        commandName = "hmoveto";
                        claimArguments(1, true, true);

                        //If in a flex section channel arg and 0 into current flex command
                        if (inFlex) {
                            //If second pair found add to the first and store back
                            if (currentFlexCommand.args.size()==2 && !firstArgsAdded) {
                                final int arg0 = args.get(0).numberValue + currentFlexCommand.args.get(0).numberValue;
                                final int arg1 = currentFlexCommand.args.get(1).numberValue;
                                currentFlexCommand.args.clear();
                                currentFlexCommand.args.add(new CharstringElement(arg0));
                                currentFlexCommand.args.add(new CharstringElement(arg1));
                                firstArgsAdded = true;
                            } else {
                                currentFlexCommand.args.add(args.get(0));
                                currentFlexCommand.args.add(new CharstringElement(0));
                            }
                            commandName = "";
                        }
                        break;
                    case 23:            //vstemhm
                        if (!isType1) {
                            commandName = "vstemhm";
                            final int argsToGet = (countArgs() / 2) * 2;
                            hintsIncluded += argsToGet / 2;
                            claimArguments(argsToGet, true, true);
                        } else {
                            //
                        }
                        break;
                    case 24:            //rcurveline
                        if (!isType1) {
                            commandName = "rcurveline";
                            final int argsToGet = (((countArgs() - 2) / 6) * 6) + 2;
                            claimArguments(argsToGet, true, true);
                        } else {
                            //
                        }
                        break;
                    case 25:            //rlinecurve
                        if (!isType1) {
                            commandName = "rlinecurve";
                            final int argsToGet = (((countArgs() - 6) / 2) * 2) + 6;
                            claimArguments(argsToGet, true, true);
                        } else {
                            //
                        }
                        break;
                    case 26:            //vvcurveto
                        if (!isType1) {
                            commandName = "vvcurveto";
                            int argsToGet = (countArgs() / 4) * 4;
                            if (argsToGet < countArgs()) {
                                argsToGet++;
                            }
                            claimArguments(argsToGet, true, true);
                        } else {
                            //
                        }
                        break;
                    case 27:            //hhcurveto
                        if (!isType1) {
                            commandName = "hhcurveto";
                            int argsToGet = (countArgs() / 4) * 4;
                            if (argsToGet < countArgs()) {
                                argsToGet++;
                            }
                            claimArguments(argsToGet, true, true);
                        } else {
                            //
                        }
                        break;
                    case 28:            //shortint
                        if (!isType1) {
                            commandName = "shortint";
                            isCommand = false;
                            numberValue = (charstring[pos+2] & 0xFF) +
                                    ((charstring[pos+1] & 0xFF) << 8);
                            if ((charstring[pos+1] & 0x80) == 0x80) {
                                numberValue -= 0x10000;
                            }
                            length = 3;
                        } else {
                            //
                        }
                        break;
                    case 29:            //callgsubr
                        if (!isType1) {
                            commandName = "callgsubr";
                            claimArguments(1, false, false);
                        } else {
                            //
                        }
                        break;
                    case 30:            //vhcurveto
                        commandName = "vhcurveto";
                        if (isType1) {
                            claimArguments(4, true, true);
                        } else {
                            int argsToGet = (countArgs() / 4) * 4;
                            if (argsToGet < countArgs()) {
                                argsToGet++;
                            }
                            claimArguments(argsToGet, true, true);
                        }
                        break;
                    case 31:            //hvcurveto
                        commandName = "hvcurveto";
                        if (isType1) {
                            claimArguments(4, true, true);
                        } else {
                            int argsToGet = (countArgs() / 4) * 4;
                            if (argsToGet < countArgs()) {
                                argsToGet++;
                            }
                            claimArguments(argsToGet, true, true);
                        }
                        break;
                    case 255:           //5 byte number
                        length = 5;
                        isCommand = false;
                        numberValue = (charstring[pos+4] & 0xFF) +
                                ((charstring[pos+3] & 0xFF) << 8) +
                                ((charstring[pos+2] & 0xFF) << 16) +
                                ((charstring[pos+1] & 0xFF) << 24);
                        if (!isType1) {
                            fractionalComponent = numberValue & 0xFFFF;
                            numberValue >>= 16;
                        }

                        break;
                    default:
                        //
                }

                if (mergePrevious) {
                    final CharstringElement previous = currentCharString.get(currentCharString.indexOf(this) - 1);
                    if (commandName.equals(previous.commandName) && previous.args.size()<=(39-args.size())) {
                        currentCharString.remove(previous);
                        for (final CharstringElement e : args) {
                            previous.args.add(e);
                        }
                        args = previous.args;
                    }
                }

                //If it's a hint, move it to a separate list so we can append it to the start
                if (moveToStart) {
                    currentCharString.remove(this);
                    currentCharStringHints.add(this);

                    //If any of the args is a result, move the command into the hints too
                    for (int i=0; i<args.size(); i++) {
                        final CharstringElement e = args.get(i);
                        if (e.isResult) {
                            args.remove(e);
                            currentCharString.remove(e.parent);
                            args.add(i, e.parent);
                        }
                    }
                }
            }
        }

        /**
         * Initiate object for 2 byte commands
         * @param charstring The charstring to read from
         * @param pos The position of the first byte (12)
         * @return Whether this element needs to be moved to the start of the charstring
         */
        private boolean construct2ByteCommand(int[] charstring, int pos) {
            boolean moveToStart = false;
            final int b = charstring[pos+1];
            
            switch(b) {
                case 0:     //dotsection
                    commandName = "dotsection";
                    claimArguments(0, false, true);
                    break;
                case 1:     //vstem3
                    if (isType1) {
                        commandName = "vstem3";
                        moveToStart = true;
                        claimArguments(6, true, true);
                    } else {
                        //
                    }
                    break;
                case 2:     //hstem3
                    if (isType1) {
                        commandName = "hstem3";
                        moveToStart = true;
                        claimArguments(6, true, true);
                    } else {
                        //
                    }
                    break;
                case 3:     //and
                    if (!isType1) {
                        commandName = "and";
                        claimArguments(2, false, false);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 4:     //or
                    if (!isType1) {
                        commandName = "or";
                        claimArguments(2, false, false);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 5:     //not
                    if (!isType1) {
                        commandName = "not";
                        claimArguments(1, false, false);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 6:     //seac
                    if (isType1) {
                        commandName = "seac";
                        claimArguments(5, true, true);
                    } else {
                        //
                    }
                    break;
                case 7:     //sbw
                    if (isType1) {
                        commandName = "sbw";
                        claimArguments(4, true, true);
                        lsbX[currentCharStringID] = args.get(0).evaluate();
                        lsbY[currentCharStringID] = args.get(1).evaluate();
                        widthX[currentCharStringID] = args.get(2).evaluate();
                        widthY[currentCharStringID] = args.get(3).evaluate();

                        //Remove it and any args
                        currentCharString.remove(this);
                        for (final CharstringElement arg : args) {
                            if (arg.isResult) {
                                currentCharString.remove(arg.parent);
                            }
                        }
                    } else {
                        //
                    }
                    break;
                case 9:     //abs
                    if (!isType1) {
                        commandName = "abs";
                        claimArguments(1, false, false);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 10:    //add
                    if (!isType1) {
                        commandName = "add";
                        claimArguments(2, false, false);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 11:    //sub
                    if (!isType1) {
                        commandName = "sub";
                        claimArguments(2, false, false);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 12:    //div
                    commandName = "div";
                    claimArguments(2, false, false);
                    new CharstringElement(this);
                    break;
                case 14:    //neg
                    if (!isType1) {
                        commandName = "neg";
                        claimArguments(1, false, false);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 15:    //eq
                    if (!isType1) {
                        commandName = "eq";
                        claimArguments(2, false, false);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 16:    //callothersubr
                    if (isType1) {
                        commandName = "callothersubr";
                        claimArguments(2, false, false);
                        if (args.size() > 1) {
                            final int count = args.get(1).numberValue;
                            final boolean foundEnough = claimArguments(count, false, false);

                            if (!foundEnough) {
                                currentCharString.remove(this);
                            } else {
                                //Place arguments back on stack
                                for (int i=0; i<count; i++) {
                                    new CharstringElement(args.get((1+count)-i).numberValue);
                                }
                            }
                        }
                    } else {
                        //
                    }
                    break;
                case 17:    //pop
                    if (isType1) {
                        commandName = "pop";
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 18:    //drop
                    if (!isType1) {
                        commandName = "drop";
                        claimArguments(1, false, false);
                    } else {
                        //
                    }
                    break;
                case 20:    //put
                    if (!isType1) {
                        commandName = "put";
                        claimArguments(2, false, false);
                    } else {
                        //
                    }
                    break;
                case 21:    //get
                    if (!isType1) {
                        commandName = "get";
                        claimArguments(1, false, false);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 22:    //ifelse
                    if (!isType1) {
                        commandName = "ifelse";
                        claimArguments(4, false, false);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 23:    //random
                    if (!isType1) {
                        commandName = "random";
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 24:    //mul
                    if (!isType1) {
                        commandName = "mul";
                        claimArguments(2, false, false);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 26:    //sqrt
                    if (!isType1) {
                        commandName = "sqrt";
                        claimArguments(1, false, false);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 27:    //dup
                    if (!isType1) {
                        commandName = "dup";
                        claimArguments(1, false ,false);
                        new CharstringElement(this);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 28:    //exch
                    if (!isType1) {
                        commandName = "exch";
                        claimArguments(2, false, false);
                        new CharstringElement(this);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 29:    //index
                    if (!isType1) {
                        commandName = "index";
                        claimArguments(1, false, false);
                        new CharstringElement(this);
                    } else {
                        //
                    }
                    break;
                case 30:    //roll
                    if (!isType1) {
                        commandName = "roll";
                        claimArguments(2, false, false);
                        final int elementCount = args.get(1).numberValue;
                        claimArguments(elementCount, false, false);
                        for (int i=0; i<elementCount; i++) {
                            new CharstringElement(this);
                        }
                    } else {
                        //
                    }
                    break;
                case 33:    //setcurrentpoint
                    if (isType1) {
                        commandName = "setcurrentpoint";
                        claimArguments(2, true, true);
                    } else {
                        //
                    }
                    break;
                case 34:    //hflex
                    if (!isType1) {
                        commandName = "hflex";
                        claimArguments(7, true, true);
                    } else {
                        //
                    }
                    break;
                case 35:    //flex
                    if (!isType1) {
                        commandName = "flex";
                        claimArguments(13, true, true);
                    } else {
                        //
                    }
                    break;
                case 36:    //hflex1
                    if (!isType1) {
                        commandName = "hflex1";
                        claimArguments(9, true, true);
                    } else {
                        //
                    }
                    break;
                case 37:    //flex1
                    if (!isType1) {
                        commandName = "flex1";
                        claimArguments(11, true, true);
                    } else {
                        //
                    }
                    break;
                default:
                    //
            }
            
            return moveToStart;
        }

        /**
         * Evaluate the numerical value of this element. This is used for hsbw and sbw where the value is being funneled
         * into a data structure rather than remaining in the converted charstring.
         * @return The numerical value of the element.
         */
        private int evaluate() {
            if (isResult) {
                return parent.evaluate();
            }

            if (isCommand) {
                if ("div".equals(commandName)) {
                    return (args.get(1).evaluate() / args.get(0).evaluate());
                }

                //<start-demo><end-demo>

            }

            return numberValue;
        }

        /**
         * @return The number of bytes used in the original stream for just this element (not it's arguments).
         */
        public int getLength() {
            return length;
        }


        /**
         * Get the displacement created by this CharstringElement.
         * @return An int array pair of values for the horizontal and vertical displacement values.
         */
        public int[] getDisplacement() {

            if (!isCommand) {
                return new int[]{0, 0};
            }

            if ("hstem".equals(commandName)) {
            } else if ("vstem".equals(commandName)) {
            } else if ("vmoveto".equals(commandName)) {
                return new int[]{0, args.get(0).evaluate()};
            } else if ("rlineto".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                for (int i=0; i<args.size()/2; i++) {
                    dx += args.get(i*2).evaluate();
                    dy += args.get(1+(i*2)).evaluate();
                }
                return new int[]{dx, dy};
            } else if ("hlineto".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                boolean isX = true;
                for (final CharstringElement arg : args) {
                    if (isX) {
                        dx += arg.evaluate();
                    } else {
                        dy += arg.evaluate();
                    }
                    isX = !isX;
                }
                return new int[]{dx, dy};
            } else if ("vlineto".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                boolean isX = false;
                for (final CharstringElement arg : args) {
                    if (isX) {
                        dx += arg.evaluate();
                    } else {
                        dy += arg.evaluate();
                    }
                    isX = !isX;
                }
                return new int[]{dx, dy};
            } else if ("rrcurveto".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                for (int i=0; i<args.size()/2; i++) {
                    dx += args.get(i*2).evaluate();
                    dy += args.get(1+(i*2)).evaluate();
                }
                return new int[]{dx, dy};
            } else if ("closepath".equals(commandName)) {
            } else if ("callsubr".equals(commandName)) {
            } else if ("return".equals(commandName)) {
            } else if ("dotsection".equals(commandName)) {
            } else if ("vstem3".equals(commandName)) {
            } else if ("hstem3".equals(commandName)) {
            } else if ("and".equals(commandName)) {
            } else if ("or".equals(commandName)) {
            } else if ("not".equals(commandName)) {
            } else if ("seac".equals(commandName)) {
                //Hopefully won't have to implement this...
            } else if ("sbw".equals(commandName)) {
            } else if ("abs".equals(commandName)) {
            } else if ("add".equals(commandName)) {
            } else if ("sub".equals(commandName)) {
            } else if ("div".equals(commandName)) {
            } else if ("neg".equals(commandName)) {
            } else if ("eq".equals(commandName)) {
            } else if ("callothersubr".equals(commandName)) {
            } else if ("pop".equals(commandName)) {
            } else if ("drop".equals(commandName)) {
            } else if ("put".equals(commandName)) {
            } else if ("get".equals(commandName)) {
            } else if ("ifelse".equals(commandName)) {
            } else if ("random".equals(commandName)) {
            } else if ("mul".equals(commandName)) {
            } else if ("sqrt".equals(commandName)) {
            } else if ("dup".equals(commandName)) {
            } else if ("exch".equals(commandName)) {
            } else if ("index".equals(commandName)) {
            } else if ("roll".equals(commandName)) {
            } else if ("setcurrentpoint".equals(commandName)) {
            } else if ("hflex".equals(commandName)) {
                int dx = 0;
                for (int i=0; i<args.size(); i++) {
                    if (i != 2) {
                        dx += args.get(i).evaluate();
                    }
                }
                return new int[]{dx, 0};
            } else if ("flex".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                for (int i=0; i<args.size()/2; i++) {
                    dx += args.get(i*2).evaluate();
                    dy += args.get(1+(i*2)).evaluate();
                }
                return new int[]{dx, dy};
            } else if ("hflex1".equals(commandName)) {
                int dx = 0;
                dx += args.get(0).evaluate();
                dx += args.get(2).evaluate();
                dx += args.get(4).evaluate();
                dx += args.get(5).evaluate();
                dx += args.get(6).evaluate();
                dx += args.get(8).evaluate();
                return new int[]{dx, 0};
            } else if ("flex1".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                for (int i=0; i<args.size()/2; i++) {
                    dx += args.get(i*2).evaluate();
                    dy += args.get(1+(i*2)).evaluate();
                }
                if (Math.abs(dx) > Math.abs(dy)) {
                    dx += args.get(args.size()-1).evaluate();
                    return new int[]{dx, 0};
                } else {
                    dy += args.get(args.size()-1).evaluate();
                    return new int[]{0, dy};
                }
            } else if ("hsbw".equals(commandName)) {
            } else if ("endchar".equals(commandName)) {
            } else if ("hstemhm".equals(commandName)) {
            } else if ("hintmask".equals(commandName)) {
            } else if ("cntrmask".equals(commandName)) {
            } else if ("rmoveto".equals(commandName)) {
                return new int[]{args.get(0).evaluate(), args.get(1).evaluate()};
            } else if ("hmoveto".equals(commandName)) {
                return new int[]{args.get(0).evaluate(), 0};
            } else if ("vstemhm".equals(commandName)) {
            } else if ("rcurveline".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                for (int i=0; i<args.size()/2; i++) {
                    dx += args.get(i*2).evaluate();
                    dy += args.get(1+(i*2)).evaluate();
                }
                return new int[]{dx, dy};
            } else if ("rlinecurve".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                for (int i=0; i<args.size()/2; i++) {
                    dx += args.get(i*2).evaluate();
                    dy += args.get(1+(i*2)).evaluate();
                }
                return new int[]{dx, dy};
            } else if ("vvcurveto".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                final boolean isX = (args.size() % 2) == 1;
                for (final CharstringElement arg : args) {
                    if (isX) {
                        dx += arg.evaluate();
                    } else {
                        dy += arg.evaluate();
                    }
                }
                return new int[]{dx, dy};
            } else if ("hhcurveto".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                final boolean isX = (args.size() % 2) == 0;
                for (final CharstringElement arg : args) {
                    if (isX) {
                        dx += arg.evaluate();
                    } else {
                        dy += arg.evaluate();
                    }
                }
                return new int[]{dx, dy};
            } else if ("shortint".equals(commandName)) {
            } else if ("callgsubr".equals(commandName)) {
            } else if ("vhcurveto".equals(commandName)) {
                boolean hasExtraArg = false;
                int mainCount = args.size();
                if ((args.size() % 2) == 1) {
                    hasExtraArg = true;
                    mainCount--;
                }

                int dx = 0;
                int dy = 0;
                if ((mainCount % 8) == 0) {
                    //form2
                    for (int i=0; i < (mainCount / 8); i++) {
                        final int base = i * 8;
                        dx += args.get(base+1).evaluate() + args.get(base+3).evaluate()
                                + args.get(base+4).evaluate() + args.get(base+5).evaluate();
                        dy += args.get(base).evaluate() + args.get(base+2).evaluate()
                                + args.get(base+6).evaluate() + args.get(base+7).evaluate();
                    }
                    if (hasExtraArg) {
                        dx += args.get(args.size()-1).evaluate();
                    }

                } else if ((mainCount % 4) == 0) {
                    //form1
                    dx += args.get(1).evaluate() + args.get(3).evaluate();
                    dy += args.get(0).evaluate() + args.get(2).evaluate();

                    for (int i=0; i < ((mainCount - 4) / 8); i++) {
                        final int base = 4 + (i * 8);
                        dx += args.get(base).evaluate() + args.get(base+1).evaluate()
                                + args.get(base+5).evaluate() + args.get(base+7).evaluate();
                        dy += args.get(base+2).evaluate() + args.get(base+3).evaluate()
                                + args.get(base+4).evaluate() + args.get(base+6).evaluate();
                    }
                    if (hasExtraArg) {
                        dy += args.get(args.size()-1).evaluate();
                    }

                } else {
                    throw new RuntimeException("vhcurveto command has an unexpected number of args ("+args.size()+ ')');
                }

                return new int[]{dx, dy};
            } else if ("hvcurveto".equals(commandName)) {
                boolean hasExtraArg = false;
                int mainCount = args.size();
                if ((args.size() % 2) == 1) {
                    hasExtraArg = true;
                    mainCount--;
                }

                int dx = 0;
                int dy = 0;
                if ((mainCount % 8) == 0) {
                    //form2
                    for (int i=0; i < (mainCount / 8); i++) {
                        final int base = i * 8;
                        dx += args.get(base).evaluate() + args.get(base+1).evaluate()
                                + args.get(base+5).evaluate() + args.get(base+7).evaluate();
                        dy += args.get(base+2).evaluate() + args.get(base+3).evaluate()
                                + args.get(base+4).evaluate() + args.get(base+6).evaluate();
                    }
                    if (hasExtraArg) {
                        dy += args.get(args.size()-1).evaluate();
                    }

                } else if ((mainCount % 4) == 0) {
                    //form1
                    dx += args.get(0).evaluate() + args.get(1).evaluate();
                    dy += args.get(2).evaluate() + args.get(3).evaluate();

                    for (int i=0; i < ((mainCount - 4) / 8); i++) {
                        final int base = 4 + (i * 8);
                        dx += args.get(base+1).evaluate() + args.get(base+3).evaluate()
                                + args.get(base+4).evaluate() + args.get(base+5).evaluate();
                        dy += args.get(base).evaluate() + args.get(base+2).evaluate()
                                + args.get(base+6).evaluate() + args.get(base+7).evaluate();
                    }
                    if (hasExtraArg) {
                        dx += args.get(args.size()-1).evaluate();
                    }

                } else {
                    throw new RuntimeException("hvcurveto command has an unexpected number of args ("+args.size()+ ')');
                }

                return new int[]{dx, dy};
            } else if ("t1flex".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                for (int i = 0; i < 6; i++) {
                    dx += args.get(i * 2).evaluate();
                    dy += args.get(1 + (i * 2)).evaluate();
                }
                return new int[]{dx, dy};
            } else if (commandName.isEmpty()) {
                return new int[]{0, 0};
            } else {
                //
            }

            return new int[]{0,0};
        }

        /**
         * Scale this element according to a precalculated scale value. Works recursively.
         */
        public void scale(final double scale) {

            //If result, ignore
            if (isResult) {
                return;
            }

            //If number, scale it
            if (!isCommand) {
                numberValue = (int)(numberValue*scale);
                return;
            }

            //Check how to handle args if command
            boolean scaleAll=false;
            if ("hstem".equals(commandName)) {
                scaleAll = true;
            } else if ("vstem".equals(commandName)) {
                scaleAll = true;
            } else if ("vmoveto".equals(commandName)) {
                scaleAll = true;
            } else if ("rlineto".equals(commandName)) {
                scaleAll = true;
            } else if ("hlineto".equals(commandName)) {
                scaleAll = true;
            } else if ("vlineto".equals(commandName)) {
                scaleAll = true;
            } else if ("rrcurveto".equals(commandName)) {
                scaleAll = true;
            } else if ("closepath".equals(commandName)) {
            } else if ("callsubr".equals(commandName)) {
            } else if ("return".equals(commandName)) {
            } else if ("dotsection".equals(commandName)) {
            } else if ("vstem3".equals(commandName)) {
                scaleAll = true;
            } else if ("hstem3".equals(commandName)) {
                scaleAll = true;
            } else if ("and".equals(commandName)) {
            } else if ("or".equals(commandName)) {
            } else if ("not".equals(commandName)) {
            } else if ("seac".equals(commandName)) {
                for (int i=0; i<3; i++) {
                    args.get(i).scale(scale);
                }
            } else if ("sbw".equals(commandName)) {
            } else if ("abs".equals(commandName)) {
                scaleAll = true;
            } else if ("add".equals(commandName)) {
                scaleAll = true;
            } else if ("sub".equals(commandName)) {
                scaleAll = true;
            } else if ("div".equals(commandName)) {
                scaleAll = true;
            } else if ("neg".equals(commandName)) {
                scaleAll = true;
            } else if ("eq".equals(commandName)) {
                scaleAll = true;
            } else if ("callothersubr".equals(commandName)) {
            } else if ("pop".equals(commandName)) {
            } else if ("drop".equals(commandName)) {
                scaleAll = true;
            } else if ("put".equals(commandName)) {
                args.get(0).scale(scale);
            } else if ("get".equals(commandName)) {
            } else if ("ifelse".equals(commandName)) {
                args.get(2).scale(scale);
                args.get(3).scale(scale);
            } else if ("random".equals(commandName)) {
            } else if ("mul".equals(commandName)) {
                scaleAll = true;
            } else if ("sqrt".equals(commandName)) {
                scaleAll = true;
            } else if ("dup".equals(commandName)) {
                scaleAll = true;
            } else if ("exch".equals(commandName)) {
                scaleAll = true;
            } else if ("index".equals(commandName)) {
                scaleAll = true;
            } else if ("roll".equals(commandName)) {
                scaleAll = true;
            } else if ("setcurrentpoint".equals(commandName)) {
                scaleAll = true;
            } else if ("hflex".equals(commandName)) {
                scaleAll = true;
            } else if ("flex".equals(commandName)) {
                scaleAll = true;
            } else if ("hflex1".equals(commandName)) {
                scaleAll = true;
            } else if ("flex1".equals(commandName)) {
                scaleAll = true;
            } else if ("hsbw".equals(commandName)) {
            } else if ("endchar".equals(commandName)) {
            } else if ("hstemhm".equals(commandName)) {
                scaleAll = true;
            } else if ("hintmask".equals(commandName)) {
                scaleAll = true;
            } else if ("cntrmask".equals(commandName)) {
                scaleAll = true;
            } else if ("rmoveto".equals(commandName)) {
                scaleAll = true;
            } else if ("hmoveto".equals(commandName)) {
                scaleAll = true;
            } else if ("vstemhm".equals(commandName)) {
                scaleAll = true;
            } else if ("rcurveline".equals(commandName)) {
                scaleAll = true;
            } else if ("rlinecurve".equals(commandName)) {
                scaleAll = true;
            } else if ("vvcurveto".equals(commandName)) {
                scaleAll = true;
            } else if ("hhcurveto".equals(commandName)) {
                scaleAll = true;
            } else if ("shortint".equals(commandName)) {
                scaleAll = true;
            } else if ("callgsubr".equals(commandName)) {
            } else if ("vhcurveto".equals(commandName)) {
                scaleAll = true;
            } else if ("hvcurveto".equals(commandName)) {
                scaleAll = true;
            } else if ("t1flex".equals(commandName)) {
                scaleAll = true;
            } else if (commandName.isEmpty()) {
            } else {
                //
            }

            if (scaleAll) {
                for (final CharstringElement e : args) {
                    e.scale(scale);
                }
            }
        }

        /**
         * Return the type 2 bytes required to match the effect of the instruction and it's arguments.
         * @return the type 2 bytes required to match the effect of the instruction and it's arguments
         */
        public byte[] getType2Bytes() {

            if (!isCommand) {

                if (isResult) {
                    return new byte[]{};
                }

                if (fractionalComponent == 0) {
                    return CFFUtils.storeCharstringType2Integer(numberValue);
                } else {
                    final byte[] result = new byte[5];
                    result[0] = (byte)255;
                    System.arraycopy(FontWriter.setNextUint16(numberValue), 0, result, 1, 2);
                    System.arraycopy(FontWriter.setNextUint16(fractionalComponent), 0, result, 3, 2);
                    return result;
                }
            }

            boolean noChange=false;
            byte[] commandNumber= {};

            if ("hstem".equals(commandName)) {
                if (isType1) {
                    args.get(0).numberValue += lsbY[currentCharStringID];
                }
                noChange=true;
                commandNumber=new byte[]{1};

            } else if ("vstem".equals(commandName)) {
                if (isType1) {
                    args.get(0).numberValue += lsbX[currentCharStringID];
                }
                noChange=true;
                commandNumber=new byte[]{3};

            } else if ("vmoveto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{4};

            } else if ("rlineto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{5};

            } else if ("hlineto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{6};

            } else if ("vlineto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{7};

            } else if ("rrcurveto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{8};

            } else if ("closepath".equals(commandName)) {
                //Remove moveto automatically closes paths in Type 2
                return new byte[]{};

            } else if ("callsubr".equals(commandName)) {
                if (isType1) {
                    return new byte[]{};
                } else {
                    noChange = true;
                    commandNumber=new byte[]{10};
                }

            } else if ("return".equals(commandName)) {
                if (isType1) {
                    //Unsupported othersubrs
                    return new byte[]{};
                } else {
                    noChange=true;
                    commandNumber=new byte[]{11};
                }

            } else if ("dotsection".equals(commandName)) {
                //Deprecated - remove
                return new byte[]{};

            } else if ("vstem3".equals(commandName)) {

            } else if ("hstem3".equals(commandName)) {

            } else if ("and".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,3};

            }else if ("or".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,4};

            } else if ("not".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,5};

            } else if ("seac".equals(commandName)) {    //Create accented character by merging specified charstrings

                //Get args
//                int asb = args.get(0).numberValue;
                final int adx = args.get(1).numberValue;
                final int ady = args.get(2).numberValue;
                final int bchar = args.get(3).numberValue;
                final int achar = args.get(4).numberValue;

                //Look up character code for specified location in standard encoding
                final int aCharUnicode = (int) StandardFonts.getEncodedChar(StandardFonts.STD, achar).charAt(0);
                final int bCharUnicode = (int)StandardFonts.getEncodedChar(StandardFonts.STD, bchar).charAt(0);
                int accentIndex = -1;
                int baseIndex = -1;

                //Run through glyph names comparing character codes to those for the accent and base to find glyph indices
                for (int i=0; i<glyphNames.length; i++) {
                    final int adobePos = StandardFonts.getAdobeMap(glyphNames[i]);
                    if (adobePos >= 0 && adobePos < 512) {

                        if (adobePos == aCharUnicode) {
                            accentIndex = i;
                        }
                        if (adobePos == bCharUnicode) {
                            baseIndex = i;
                        }
                    }
                }

                //Check both glyphs found
                if (accentIndex == -1 || baseIndex == -1) {
                    return new byte[]{};
                }

                //Merge glyphs
                final FastByteArrayOutputStream bos = new FastByteArrayOutputStream();

                final int charstringStore = currentCharStringID;

                //Fetch base charstring, convert, and remove endchar command
                charstringXDisplacement[baseIndex] = 0;
                charstringYDisplacement[baseIndex] = 0;
                inSeac = true;
                final byte[] rawBaseCharstring = converter.convertCharstring(baseIndex);
                inSeac = false;
                currentCharStringID = charstringStore;
                final byte[] baseCharstring = new byte[rawBaseCharstring.length-1];
                System.arraycopy(rawBaseCharstring, 0, baseCharstring, 0, baseCharstring.length);
                bos.write(baseCharstring);

                //Move to the origin plus the offset
                bos.write(CFFUtils.storeCharstringType2Integer(-(charstringXDisplacement[baseIndex]) + adx));
                bos.write(CFFUtils.storeCharstringType2Integer(-(charstringYDisplacement[baseIndex]) + ady));
                bos.write((byte)21);

                //Fetch accent charstring and convert
                charstringXDisplacement[accentIndex] = 0;
                charstringYDisplacement[accentIndex] = 0;
                final byte[] accentCharstring = converter.convertCharstring(accentIndex);
                currentCharStringID = charstringStore;
                bos.write(accentCharstring);

                return bos.toByteArray();


            } else if ("sbw".equals(commandName)) {
                //Might need to moveto arg coordinates?
                return new byte[]{};

            } else if ("abs".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,9};

            } else if ("add".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,10};

            } else if ("sub".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,11};

            } else if ("div".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{12,12};

            } else if ("neg".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,14};

            } else if("eq".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,15};

            } else if ("callothersubr".equals(commandName)) {

            } else if ("pop".equals(commandName)) {

            } else if ("drop".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,18};

            } else if ("put".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,20};

            } else if ("get".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,21};

            } else if ("ifelse".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,22};

            } else if ("random".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,23};

            } else if  ("mul".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,24};

            } else if ("sqrt".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,26};

            } else if ("dup".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,27};

            } else if ("exch".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,28};

            } else if ("index".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,29};

            } else if ("roll".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,30};

            } else if ("setcurrentpoint".equals(commandName)) {

            } else if ("hflex".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,34};

            } else if ("flex".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,35};

            } else if ("hflex1".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,36};

            } else if ("flex1".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{12,37};

            } else if ("hsbw".equals(commandName)) {
                //Might need to moveto arg coordinates?
                return new byte[]{};

            } else if ("endchar".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{14};

            } else if ("hstemhm".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{18};

            } else if ("hintmask".equals(commandName)) {
                final FastByteArrayOutputStream bos = getStreamWithArgs();

                bos.write(new byte[]{19});
                bos.write(additionalBytes);

                return bos.toByteArray();

            } else if ("cntrmask".equals(commandName)) {
                final FastByteArrayOutputStream bos = getStreamWithArgs();

                bos.write(new byte[]{20});
                bos.write(additionalBytes);

                return bos.toByteArray();

            } else if ("rmoveto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{21};

            } else if ("hmoveto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{22};

            } else if ("vstemhm".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{23};

            } else if ("rcurveline".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{24};

            } else if ("rlinecurve".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{25};

            } else if ("vvcurveto".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{26};

            } else if ("hhcurveto".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{27};

            } else if ("shortint".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{28};

            } else if ("callgsubr".equals(commandName)) {
                noChange = true;
                commandNumber = new byte[]{29};

            } else if ("vhcurveto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{30};

            } else if ("hvcurveto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{31};

            } else if ("t1flex".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{12,35};

            } else if (commandName.isEmpty()) {
                return new byte[]{};

            } else {
                //
            }

            if (noChange) {
                //No change - return args and command
                final FastByteArrayOutputStream bos = getStreamWithArgs();

                bos.write(commandNumber);

                return bos.toByteArray();
            }

            //

            return new byte[]{};
        }

        private FastByteArrayOutputStream getStreamWithArgs() {
            final FastByteArrayOutputStream result = new FastByteArrayOutputStream();


            for (final CharstringElement arg : args) {
                result.write(arg.getType2Bytes());
            }


            return result;
        }

        /**
         * Return a representation of the element as a string.
         * @return Element as string
         */
        public String toString() {
            if (isCommand) {
                return commandName + args;
            }

            if (isResult) {
                return "result of "+parent;
            }

            if (fractionalComponent != 0) {
                return String.valueOf(numberValue + ((double)fractionalComponent/0xFFFF));
            }

            return String.valueOf(numberValue);
        }

        /**
         * Removes arguments from the stack (in other words, numbers and results from the instruction stream) and places
         * them in this element's argument list.
         * @param count The number of arguments to take
         * @param takeFromBottom Where to take the arguments from
         * @param clearStack Whether to clear the stack after
         * @return whether enough arguments were found
         */
        private boolean claimArguments(final int count, final boolean takeFromBottom, final boolean clearStack) {

            //Check first non-subr command for width value & store if found
            if (!widthTaken && !isType1 && !commandName.contains("subr")) {
                widthTaken = true;
                if (countArgs() >= count + 1) {
                    claimArguments(1, true, false);
                    width = args.get(0);
                    args.remove(0);
                }
            }

            if (count > 0) {
                int currentIndex = currentCharString.indexOf(this);
                if (currentIndex == -1) {
                    throw new RuntimeException("Not in list!");
                }

                int argsFound = 0;
                boolean failed = false;
                while (argsFound < count && !failed) {

                    boolean found = false;
                    if (takeFromBottom) {
                        int pos=0;
                        while (!found && pos <= currentIndex) {
                            final CharstringElement e = currentCharString.get(pos);
                            if (!e.isCommand) {
                                argsFound++;
                                args.add(e);
                                currentCharString.remove(e);
                                found = true;
                            }
                            pos++;
                        }
                    } else {
                        int pos = currentIndex;
                        while (!found && pos >= 0) {
                            final CharstringElement e = currentCharString.get(pos);
                            if (!e.isCommand) {
                                argsFound++;
                                args.add(e);
                                currentCharString.remove(e);
                                found = true;
                                currentIndex--;
                            }
                            pos--;
                        }
                    }
                    if (!found) {
                        failed = true;
                    }
                }

                if (argsFound < count) {
//                    System.out.println("Not enough arguments! ("+argsFound+" of "+count+") "+ (currentCharStringID > charstrings.length ? "subr "+(currentCharStringID-charstrings.length) : "charstring "+currentCharStringID));
//                    throw new RuntimeException("Not enough arguments!");
                    return false;
                }
            }

            if (clearStack) {
                for (int i=0; i<currentCharString.size(); i++) {
                    final CharstringElement e = currentCharString.get(i);
                    if (!e.isCommand) {
                        currentCharString.remove(e);
                    }
                }
            }

            return true;

        }


        /**
         * This is currently only used for comparing numbers and hints. ('hstem' needs to come before 'vstem'.)
         * @param e Element to compare with
         * @return An integer representing which element is greater (or should appear lower in the charstring)
         */
        @Override
        public int compareTo(final Object e) {
            if (e instanceof CharstringElement) {
                final CharstringElement element = (CharstringElement)e;
                if (isCommand && element.isCommand) {
                    final HashMap<String, Integer> map = new HashMap<String, Integer>();
                    map.put("hstem",0);
                    map.put("hstemhm",0);
                    map.put("vstem", 1);
                    map.put("vstemhm", 1);
                    final Integer firstCommand = map.get(commandName);
                    final Integer secondCommand = map.get(element.commandName);

                    if (firstCommand == null || secondCommand == null) {
                        return -1;
                    }

                    if (firstCommand.equals(secondCommand)) {
                        return args.get(0).compareTo(element.args.get(0));
                    }

                    return firstCommand.compareTo(secondCommand);
                } else if (!isCommand && !element.isCommand && !isResult && !element.isResult) {
                    return ((Integer)numberValue).compareTo(element.numberValue);
                }
            }

            return -1;
        }
    }
}