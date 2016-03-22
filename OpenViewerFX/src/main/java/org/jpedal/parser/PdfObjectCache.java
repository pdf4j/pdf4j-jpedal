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
 * PdfObjectCache.java
 * ---------------
 */
package org.jpedal.parser;

import org.jpedal.color.GenericColorSpace;
import org.jpedal.exception.PdfException;
import org.jpedal.fonts.PdfFont;
import org.jpedal.objects.raw.*;
import org.jpedal.utils.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * caches for data
 */
public class PdfObjectCache {

    public static final int ColorspacesUsed=1;
    public static final int Colorspaces=2;
    public static final int ColorspacesObjects=3;
    public static final int GlobalShadings=4;
    public static final int LocalShadings=5;

    //init size of maps
    private static final int initSize=50;

    //int values for all colorspaces
    private final Map<java.io.Serializable, Object> colorspacesUsed=new HashMap<java.io.Serializable, Object>(initSize);

    private final Map<java.io.Serializable, Object> colorspacesObjects=new HashMap<java.io.Serializable, Object>(initSize);

    /**colors*/
    private Map<Object, PdfObject> colorspaces=new HashMap<Object, PdfObject>(initSize);

    private Map<Object, PdfObject> globalXObjects = new HashMap<Object, PdfObject>(initSize);
    private Map<Object, PdfObject> localXObjects=new HashMap<Object, PdfObject>(initSize);

    public final Map<String, GenericColorSpace> XObjectColorspaces=new HashMap<String, GenericColorSpace>(initSize);

    public final Map<String, PdfObject> patterns=new HashMap<String, PdfObject>(initSize);
    private final Map<String, PdfObject> globalShadings=new HashMap<String, PdfObject>(initSize);
    private Map<String, PdfObject> localShadings=new HashMap<String, PdfObject>(initSize);

    final Map<String, Integer> imposedImages = new HashMap<String, Integer>(initSize);

    public PdfObject groupObj;

    /**fonts*/
    public Map<Object, PdfObject> unresolvedFonts=new HashMap<Object, PdfObject>(initSize);
    public Map<String, PdfObject> directFonts=new HashMap<String, PdfObject>(initSize);
    public Map<String, PdfFont> resolvedFonts=new HashMap<String, PdfFont>(initSize);

    /**GS*/
    Map<Object, PdfObject> GraphicsStates=new HashMap<Object, PdfObject>(initSize);

    public PdfObjectCache copy() {

        final PdfObjectCache copy=new PdfObjectCache();

        copy.localShadings=localShadings;
        copy.unresolvedFonts=unresolvedFonts;
        copy.GraphicsStates= GraphicsStates;
        copy.directFonts= directFonts;
        copy.resolvedFonts= resolvedFonts;
        copy.colorspaces= colorspaces;

        copy.localXObjects= localXObjects;
        copy.globalXObjects= globalXObjects;

        copy.groupObj= groupObj;


        return copy;

    }

    public PdfObjectCache() {}

    public void put(final int type, final int key, final Object value){
        switch(type){
            case ColorspacesUsed:
                colorspacesUsed.put(key,value);
                break;
            case ColorspacesObjects:
                colorspacesObjects.put(key,value);
                break;
        }
    }
    
    public void put(final int type, final String key, final Object value){
        switch(type){
            case ColorspacesUsed:
                colorspacesUsed.put(key,value);
                break;
            case ColorspacesObjects:
                colorspacesObjects.put(key,value);
                break;
        }
    }
    
    public boolean containsKey(final int key){

        boolean returnValue=true;

        switch(key){
            case ColorspacesObjects:
                returnValue=colorspacesObjects.containsKey(key);
                break;
        }

        return returnValue;
    }

    public Iterator<java.io.Serializable> iterator(final int type){

        Iterator<java.io.Serializable> returnValue=null;

        switch(type){
            case ColorspacesUsed:
                returnValue=colorspacesUsed.keySet().iterator();
                break;
            case ColorspacesObjects:
                returnValue=colorspacesObjects.keySet().iterator();
                break;
        }

        return returnValue;
    }

    public Object get(final int key, final Object value){

        Object returnValue=null;

        switch(key){
            case ColorspacesUsed:
                returnValue=colorspacesUsed.get(value);
                break;
            case Colorspaces:
                returnValue=colorspaces.get(value);
                break;
            case ColorspacesObjects:
                returnValue=colorspacesObjects.get(value);
                break;
            case GlobalShadings:
                returnValue=globalShadings.get(value);
                break;
            case LocalShadings:
                returnValue=localShadings.get(value);
                break;

        }

        return returnValue;
    }

    public void resetFonts() {
        resolvedFonts.clear();
        unresolvedFonts.clear();
        directFonts.clear();
    }

    public PdfObject getXObjects(final String localName) {

        PdfObject XObject = localXObjects.get(localName);
        if (XObject == null) {
            XObject = globalXObjects.get(localName);
        }

        return XObject;
    }

    public void readResources(final PdfObject Resources, final boolean resetList)  throws PdfException{


        //decode
        final String[] names={"ColorSpace","ExtGState","Font", "Pattern","Shading","XObject"};
        final int[] keys={PdfDictionary.ColorSpace, PdfDictionary.ExtGState, PdfDictionary.Font,
                PdfDictionary.Pattern, PdfDictionary.Shading,PdfDictionary.XObject};

        for(int ii=0;ii<names.length;ii++){

            if(keys[ii]==PdfDictionary.Font || keys[ii]==PdfDictionary.XObject) {
                readArrayPairs(Resources, resetList, keys[ii]);
            } else {
                readArrayPairs(Resources, false, keys[ii]);
            }
        }
    }

    private void readArrayPairs(final PdfObject Resources, final boolean resetFontList, final int type) {

        final boolean debugPairs=false;

        if(debugPairs){
            System.out.println("-------------readArrayPairs-----------"+type);
            System.out.println("new="+Resources+ ' '+Resources.getObjectRefAsString());
        }
        String id,value;

        /**
         * new code
         */
        if(Resources!=null){

            final PdfObject resObj=Resources.getDictionary(type);

            if(debugPairs) {
                System.out.println("new res object=" + resObj);
            }

            if(resObj!=null){

                /**
                 * read all the key pairs for Glyphs
                 */
                final PdfKeyPairsIterator keyPairs=resObj.getKeyPairsIterator();

                PdfObject obj;

                if(debugPairs){
                    System.out.println("New values");
                    System.out.println("----------");
                }

                while(keyPairs.hasMorePairs()){

                    id=keyPairs.getNextKeyAsString();
                    value=keyPairs.getNextValueAsString();
                    obj=keyPairs.getNextValueAsDictionary();

                    if(debugPairs) {
                        System.out.println(id + ' ' + obj + ' ' + value + ' ' + Resources.isDataExternal());
                    }

                    if(Resources.isDataExternal()){ //check and flag if missing

                        //ObjectDecoder objectDecoder=new ObjectDecoder(currentPdfFile.getObjectReader());

                        if(obj==null && value==null){
                            Resources.setFullyResolved(false);
                            return;
                        }else if(obj==null){

                            final PdfObject childObj= ObjectFactory.createObject(type, value, type, -1);

                            childObj.setStatus(PdfObject.UNDECODED_DIRECT);
                            childObj.setUnresolvedData(StringUtils.toBytes(value), type);

//                            if(!objectDecoder.resolveFully(childObj)){
//                                Resources.setFullyResolved(false);
//                                return;
//                            }

                            //cache if setup
                            if(type==PdfDictionary.Font){
                                directFonts.put(id,childObj);
                            }
//                        }else if(!objectDecoder.resolveFully(obj)){
//                            Resources.setFullyResolved(false);
//                            return;
                        }
                    }

                    switch(type){

                        case PdfDictionary.ColorSpace:
                            colorspaces.put(id,obj);
                            break;

                        case PdfDictionary.ExtGState:
                            GraphicsStates.put(id,obj);
                            break;

                        case PdfDictionary.Font:

                            unresolvedFonts.put(id,obj);

                            break;

                        case PdfDictionary.Pattern:
                            patterns.put(id,obj);

                            break;

                        case PdfDictionary.Shading:
                            if(resetFontList) {
                                globalShadings.put(id, obj);
                            } else {
                                localShadings.put(id, obj);
                            }

                            break;

                        case PdfDictionary.XObject:
                            if(resetFontList) {
                                globalXObjects.put(id, obj);
                            } else {
                                localXObjects.put(id, obj);
                            }

                            break;

                    }

                    keyPairs.nextPair();
                }
            }
        }
    }


    public void reset(final PdfObjectCache newCache) {

        //reset copies
        localShadings=new HashMap<String, PdfObject>(initSize);
        resolvedFonts=new HashMap(initSize);
        unresolvedFonts=new HashMap<Object, PdfObject>(initSize);
        directFonts=new HashMap<String, PdfObject>(initSize);
        colorspaces=new HashMap<Object, PdfObject>(initSize);
        GraphicsStates=new HashMap<Object, PdfObject>(initSize);
        localXObjects=new HashMap<Object, PdfObject>(initSize);

        Iterator<Object> keys=newCache.GraphicsStates.keySet().iterator();
        while(keys.hasNext()){
            final Object key=keys.next();
            GraphicsStates.put(key,newCache.GraphicsStates.get(key));
        }

        keys=newCache.colorspaces.keySet().iterator();
        while(keys.hasNext()){
            final Object key=keys.next();
            colorspaces.put(key, newCache.colorspaces.get(key));
        }


        keys=newCache.localXObjects.keySet().iterator();
        while(keys.hasNext()){
            final Object key=keys.next();
            localXObjects.put(key, newCache.localXObjects.get(key));
        }

        keys=newCache.globalXObjects.keySet().iterator();
        while(keys.hasNext()){
            final Object key=keys.next();
            globalXObjects.put(key, newCache.globalXObjects.get(key));
        }

        //allow for no fonts in FormObject when we use any global
        if(unresolvedFonts.isEmpty()){
            //unresolvedFonts=rawFonts;
            keys=newCache.unresolvedFonts.keySet().iterator();
            while(keys.hasNext()){
                final Object key=keys.next();
                unresolvedFonts.put(key,newCache.unresolvedFonts.get(key));
            }
        }
    }

    public void restore(final PdfObjectCache mainCache) {

        directFonts= mainCache.directFonts;
        unresolvedFonts= mainCache.unresolvedFonts;
        resolvedFonts= mainCache.resolvedFonts;
        GraphicsStates= mainCache.GraphicsStates;
        colorspaces= mainCache.colorspaces;
        localShadings= mainCache.localShadings;
        localXObjects= mainCache.localXObjects;
        globalXObjects= mainCache.globalXObjects;

        groupObj= mainCache.groupObj;

    }

    public void setImposedKey(final String key, final int id) {
        if (imposedImages != null) {
            imposedImages.put(key, id);
        }
    }
}
