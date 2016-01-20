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
 * RhinoParser.java
 * ---------------
 */
package org.jpedal.objects.javascript;

import java.util.List;
import java.util.StringTokenizer;

import org.jpedal.objects.Javascript;
import org.jpedal.objects.acroforms.ReturnValues;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.javascript.functions.JSFunction;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.javascript.defaultactions.DisplayJavascriptActions;
import org.jpedal.objects.javascript.defaultactions.JpedalDefaultJavascript;
import org.jpedal.objects.layers.Layer;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.StringUtils;

import javax.swing.*;
import org.jpedal.objects.raw.PdfDictionary;


public class RhinoParser extends DefaultParser implements ExpressionEngine{

    /** version details of our library, so javascript knows what it can do,
     * also adds static adobe methods to javascript for execution if called
     */
    private static final String viewerSettings = AformDefaultJSscript.getViewerSettings()+
            AformDefaultJSscript.getstaticScript();

    private org.mozilla.javascript.Context cx;

    private org.mozilla.javascript.Scriptable scope;

    private String functions="";

    /** used to stop the thread that called execute from returning
     * before the javascript has been executed on the correct thread.
     */
    private boolean javascriptRunning;
	private final Javascript JSObj;

	public RhinoParser(final Javascript js) {
		JSObj = js;
    }


    /** make sure the contaxt has been exited */
    @Override
    public void flush() {

        if(acro!=null && acro.getFormFactory()!=null){
            if (SwingUtilities.isEventDispatchThread()){
                flushJS();
            }else {
                final Runnable doPaintComponent = new Runnable() {
                    @Override
                    public void run() {
                        flushJS();
                    }
                };
                try {
                    SwingUtilities.invokeAndWait(doPaintComponent);
                } catch (final Exception e) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
            }
        }
    }

    /** should only be called from our Thread code and not by any other access, as it wont work properly */
    public void flushJS(){
        //clear the stored functions when moving between files
        functions="";

        // Make sure we exit the current context
        if (cx != null){
            // The context could be in a different thread,
            // we need to check for this and exit in a set way.
            try{
                org.mozilla.javascript.Context.exit();
                // remembering to reset cx to null so that we recreate the contaxt for the next file
                cx = null;
            }catch(final IllegalStateException e){
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
        }
    }

    /** NOT TO BE USED, is a dummy method for HTML only, WILL BE DELETED ONE DAY */
    public void setJavaScriptEnded(){
        javascriptRunning = false;
    }

    /**
     * store and execute code
     */
    public void executeFunctions(final String code, final FormObject ref, final AcroRenderer acro) {

        //set to false at end of executeJS method
        javascriptRunning=true;

        if(acro.getFormFactory().getType()== FormFactory.SWING){

            if (SwingUtilities.isEventDispatchThread()){
                executeJS( code,  ref,  acro);
            }else {
                final Runnable doPaintComponent = new Runnable() {
                    @Override
                    public void run() {
                        executeJS( code,  ref,  acro);
                    }
                };
                try {
                    SwingUtilities.invokeAndWait(doPaintComponent);
                } catch (final Exception e) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
            }

            while(javascriptRunning){
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
            }
        }
    }

    /** should only be called from our Thread code and not by any other access, as it wont work properly */
    public void executeJS(String code, final FormObject ref, final AcroRenderer acro) {

        final String defSetCode;
        //NOTE - keep everything inside thr try, catch, finally, as the finally tidy's up so that the code will return properly.
        try {
            //if we have no code dont do anything
            if(code.isEmpty() && functions.isEmpty()) {
                return;
            }

            //check if any functions defined in code and save
            String func = "";
            int index1 = code.indexOf("function ");
            while(index1!=-1){//if we have functions
                int i = index1+8, bracket=0;
                char chr = code.charAt(i);
                while(true){//find the whole function
                    if(chr=='{'){
                        bracket++;
                    }
                    if(chr=='}'){
                        bracket--;
                        if(bracket==0) {
                            break;
                        }
                    }

                    //remember to get next char before looping again
                    chr = code.charAt(i++);
                    
                }

                //find beginning of line for start
                int indR = code.lastIndexOf('\r', index1);
                int indN = code.lastIndexOf('\n', index1);
                final int indS = ((indN<indR) ? indR : indN)+1;

                //find end of line for end
                indR = code.indexOf('\r', i);
                if(indR==-1) {
                    indR = code.length();
                }
                indN = code.indexOf('\n', i);
                if(indN==-1) {
                    indN = code.length();
                }
                final int indE = ((indN<indR) ? indN : indR)+1;

                //store the function and remove from main code
                func += code.substring(indS, indE);
                code = code.substring(0,indS)+code.substring(indE);

                //remember to check for another function before looping again
                index1 = code.indexOf("function ");
            }
            if(!func.isEmpty()){
                addCode(func);
            }

            code = preParseCode(code);

            //code = checkAndAddParentToKids(code,acro);

            // Creates and enters a Context. The Context stores information
            // about the execution environment of a script.
            if(cx==null){

                cx = org.mozilla.javascript.Context.enter();

                // Initialize the standard objects (Object, Function, etc.)
                // This must be done before scripts can be executed. Returns
                // a scope object that we use in later calls.
                scope = cx.initStandardObjects();

                // add std objects, ie- access to fields, layers, default functions
                addStdObject(acro);
            }

            // add this formobject to rhino
            if(ref!=null){
                //if flag true then it will always return a PdfProxy
                //PdfProxy proxy = (PdfProxy)acro.getField(ref.getObjectRefAsString()/*TextStreamValue(PdfDictionary.T)*/);

                final String name=ref.getTextStreamValue(PdfDictionary.T);

                //added to Rhino
                // add the current form object by name and by event, as this is the calling object
                final Object formObj = org.mozilla.javascript.Context.javaToJS(new PDF2JS(ref), scope );

                org.mozilla.javascript.ScriptableObject.putProperty( scope, "event", formObj );

                //by its name ( maybe not needed)
                if(name!=null) //stops crash on layers/houseplan final
                {
                    org.mozilla.javascript.ScriptableObject.putProperty(scope, name, formObj);
                }
            }
            
            //execute functions and add them to rhino
            //added seperate as allows for easier debugging of main code.
           // defSetCode = checkAndAddParentToKids(viewerSettings+functions,acro);
            defSetCode = viewerSettings+functions;
            cx.evaluateString(scope, defSetCode, "<JS viewer Settings>", 1, null);

            // Now evaluate the string we've collected.
            cx.evaluateString(scope, code, "<javascript>", 1, null);

        } catch (final Exception e) {
            LogWriter.writeLog("Exception: " + e.getMessage());
        }finally{

            //sync any changes made in Layers (we need to get as method static at moment)
            final PdfLayerList layersObj=acro.getActionHandler().getLayerHandler();
            if(layersObj!=null && layersObj.getChangesMade()){

                if(Layer.debugLayer) {
                    System.out.println("changed");
                }

                try {
                    //if we call decode page, i'm pritty sure we will recall the layers code, as we would recall all the JS
                    //hence the infinate loop
                    acro.getActionHandler().getPDFDecoder().decodePage(-1);

                    //@fixme
                    //final org.jpedal.gui.GUIFactory swingGUI=((org.jpedal.examples.viewer.gui.SwingGUI)acro.getActionHandler().getPDFDecoder().getExternalHandler(Options.GUIContainer));

                   // if(swingGUI!=null) {
                     //   swingGUI.rescanPdfLayers();
                    //}
                    
                    //repaint pdf decoder to make sure the layers are repainted
                    //((org.jpedal.PdfDecoder)acro.getActionHandler().getPDFDecoder()).repaint();//good idea mark
                    
                } catch (final Exception e) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
            }

            //run through all forms and see if they have changed
            //acro.updateChangedForms();

            //always set the javascript flag to false so that the execute calling thread can resume from its endless loop.
            javascriptRunning = false;
        }
    }

    /** replace javascript variables with our own so rhino can easily identify them and pass excution over to us */
    private static String preParseCode(String script) {
        final String[] searchFor = {"= (\"%.2f\",","this.ADBE"," getField(","\ngetField(","\rgetField(",
                "(getField(","this.getField(","this.resetForm(","this.pageNum"," this.getOCGs(","\nthis.getOCGs(",
                "\rthis.getOCGs("," getOCGs(","\ngetOCGs(","\rgetOCGs(",".state="};
        final String[] replaceWith = {"= util.z(\"%.2f\",","ADBE"," acro.getField(","\nacro.getField(","\racro.getField(",
                "(acro.getField(","acro.getField(","acro.resetForm(","acro.pageNum"," layers.getOCGs(","\nlayers.getOCGs(",
                "\rlayers.getOCGs("," layers.getOCGs(","\nlayers.getOCGs(","\rlayers.getOCGs(","\rlayers.getOCGs("};

        for(int i=0;i<searchFor.length;i++){
            script = checkAndReplaceCode(searchFor[i], replaceWith[i], script);
        }

        //check for printf and put all argumants into an array and call with array
        final int indexs = script.indexOf("printf");
        printf:
        if(indexs!=-1){
            final StringBuilder buf = new StringBuilder();
            int indexStart = script.lastIndexOf(';', indexs);
            final int indextmp = script.lastIndexOf('{',indexs);
            if(indexStart==-1 || (indextmp!=-1 && indextmp>indexStart)) {
                indexStart = indextmp;
            }

            buf.append(script.substring(0,indexStart+1));

            //find the end of the string
            int speech = script.indexOf('\"',indexs);
            speech = script.indexOf('\"',speech+1);
            while(script.charAt(speech-1)=='\\') {
                speech = script.indexOf('\"', speech);
            }

            //make sure there is an argument ',' after it
            final int startArgs = script.indexOf(',',speech);
            final int endArgs = script.indexOf(')',startArgs);

            //setup arguments string so we can setup in javascript
            final String arguments = script.substring(startArgs+1, endArgs);
            if(arguments.equals("printfArgs")) {
                break printf;
            }

            final StringTokenizer tok = new StringTokenizer(arguments,", ");

            //create array in javascript code
            buf.append("var printfArgs=new Array();\n");

            //add arguments to the array
            int i=0;
            while(tok.hasMoreTokens()){
                buf.append("printfArgs[");
                buf.append(i++);
                buf.append("]=");
                buf.append(tok.nextToken());
                buf.append(";\n");
            }

            //add printf command with new array as argument
            buf.append(script.substring(indexStart+1, startArgs+1));
            buf.append("printfArgs");
            buf.append(script.substring(endArgs));

            script = buf.toString();
        }

        script = checkAndReplaceCode("event.value=AFMakeNumber(acro.getField(\"sum\").value)(8)","", script);

        script = checkAndReplaceCode("calculate = false", "calculate = 0", script);
        script = checkAndReplaceCode("calculate = true", "calculate = 1", script);
        script = checkAndReplaceCode("calculate=false", "calculate=0", script);
        script = checkAndReplaceCode("calculate=true", "calculate=1", script);

        return script;
    }

//    private static String checkAndAddParentToKids(String script, AcroRenderer acro){
//
//        String startCode = "acro.getField(\"";
//        //find start of GetField statement
//        int startIndex = script.indexOf(startCode);
//        if(startIndex!=-1){
//            int startNameInd = startIndex+15;
//            int endNameInd = script.indexOf("\")", startIndex);
//            int endIndex = script.indexOf(';', startIndex)+1;
//
//            //get the name its calling
//            String name = script.substring(startNameInd,  endNameInd);
//            //if it ends with a . then we have to replace with all kids.
//            if(name.endsWith(".")){
//                
//                //removed by Mark 16042013
//                String[] allFieldNames = acro.getChildNames(name);
//
//                // add start of script
//                StringBuilder buf = new StringBuilder();
//                buf.append(script.substring(0,startIndex));
//
//                // add modified script with all fieldnames
//                for (int j = 0; j < allFieldNames.length; j++) {
//                    if(j>0)
//                        buf.append('\n');
//                    buf.append(startCode);
//                    buf.append(allFieldNames[j]);
//                    buf.append(script.substring(endNameInd,endIndex));
//                }
//
//                // add end of script
//                buf.append(script.substring(endIndex,script.length()));
//                script = buf.toString();
//            }
//        }
//        return script;
//    }

    /** replace the searchFor string with the replaceWith string within the code specified */
    private static String checkAndReplaceCode(final String searchFor, final String replaceWith,String script) {
        final int index = script.indexOf(searchFor);
        if(index!=-1){
            final StringBuilder buf = new StringBuilder(script.length());
            buf.append(script.substring(0, index));
            buf.append(replaceWith);
            buf.append(checkAndReplaceCode(searchFor, replaceWith, script.substring(index+searchFor.length(),script.length())));

            script = buf.toString();
        }
        return script;
    }

    /** add the javascript standard execution objects, that acrobat app has defined functions for. */
    private void addStdObject(final AcroRenderer acro) {

        Object objToJS = org.mozilla.javascript.Context.javaToJS( new JpedalDefaultJavascript(scope,cx), scope );
        //util added for jpedal use ONLY
        org.mozilla.javascript.ScriptableObject.putProperty( scope, "util", objToJS );
        // app added so that methods difined within adobes javascript can be implemented
        org.mozilla.javascript.ScriptableObject.putProperty( scope, "app", objToJS );

        final Object globalObj = cx.newObject(scope);
        //global is added to allow javascript to define and create its own variables
        org.mozilla.javascript.ScriptableObject.putProperty( scope, "global", globalObj );

        final Object ADBE = cx.newObject(scope);
        //global is added to allow javascript to define and create its own variables
        org.mozilla.javascript.ScriptableObject.putProperty( scope, "ADBE", ADBE );

        objToJS = org.mozilla.javascript.Context.javaToJS( new DisplayJavascriptActions(), scope );
        // display adds constant definitions of values
        org.mozilla.javascript.ScriptableObject.putProperty( scope, "display", objToJS );
        // color adds default constant colors.
        org.mozilla.javascript.ScriptableObject.putProperty( scope, "color", objToJS );

        // add layers to rhino
        final PdfLayerList layerList = acro.getActionHandler().getLayerHandler();
        if(layerList!=null){
            objToJS = org.mozilla.javascript.Context.javaToJS( layerList, scope );
            //not working yet.
            org.mozilla.javascript.ScriptableObject.putProperty( scope, "layers", objToJS );//CHRIS @javascript
        }

        // add a wrapper for accessing the forms etc
        objToJS = org.mozilla.javascript.Context.javaToJS( acro, scope );
        //acro added to replace 'this' and to allow access to form objects via the acroRenderer
        org.mozilla.javascript.ScriptableObject.putProperty( scope, "acro", objToJS );

    }

    /** add functions to the javascript code to be executed within rhino */
    @Override
    public int addCode(final String value) {
        functions += preParseCode(value);

        return 0;
    }

    /** typeToReturn
     * 0 = String,
     * 1 = Double,
     * -1 = guess
     */
    public Object generateJStype(final String textString, final boolean returnAsString){
        if(returnAsString){
            return cx.newObject(scope, "String", new Object[] { textString });
        }else {
            if(textString!=null && !textString.isEmpty() && StringUtils.isNumber(textString) &&
                    !(textString.length()==1 && textString.indexOf('.')!=-1)){//to stop double trying to figure out "."
                final Double retNum = Double.valueOf(textString);
                return cx.newObject(scope, "Number", new Object[] { retNum });
            }else {
                return cx.newObject(scope, "String", new Object[] { textString });
            }
        }
    }

    /**
     * execute javascript and reset forms values
     */
    @Override
    public int execute(final FormObject form, final int type, final Object code, final int eventType, final char keyPressed) {

        int messageCode;

        final String js = (String) code;

        //convert into args array
        final String[] args= JSFunction.convertToArray(js);

        final String command=args[0];

        if(command.startsWith("AF")) {
            messageCode = handleAFCommands(form, command, js, args, eventType, keyPressed);
        } else {
            executeFunctions(js, form, acro);
            messageCode = ActionHandler.VALUESCHANGED;
        }


		if(type == PdfDictionary.F) {
			calcualteEvent();
			messageCode = ActionHandler.VALUESCHANGED;
		}
		return messageCode;
	}

	private void calcualteEvent() {
//		System.out.println("CALC");
		final List<FormObject> obs = acro.getCompData().getFormComponents(null, ReturnValues.FORMOBJECTS_FROM_REF, -1);
		final Object[] formObjects = obs.toArray();
		for(final Object o : formObjects) {
			final FormObject formObject = (FormObject) o;
			final String ref = formObject.getObjectRefAsString();
			final String name = formObject.getTextStreamValue(PdfDictionary.T);
			final String command = (String) JSObj.getJavascriptCommand( (name != null ? name : ref), PdfDictionary.C2);

			if(command != null) {
//				System.out.println(command);
				execute(formObject, PdfDictionary.C2, command, ActionHandler.FOCUS_EVENT, ' ');
			}
		}
	}
}