/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jpedal.parser.image.downsample;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.io.ObjectStore;
import org.jpedal.parser.ParserOptions;
import org.jpedal.parser.image.data.ImageData;

/**
 *
 * @author markee
 */
public class RawImageSaver {
    
    public static void saveRawOneBitDataForResampling(final int imageCount, final ParserOptions parserOptions, final ObjectStore objectStoreStreamRef,boolean saveData, 
            final ImageData imageData, boolean arrayInverted, GenericColorSpace decodeColorData, final byte[] maskCol) {
        
        //cache if binary image (not Mask)
        if(decodeColorData.getID()==ColorSpaces.DeviceRGB && maskCol!=null && imageData.getDepth()==1 ){  //avoid cases like Hand_test/DOC028.PDF
        }else{// if(((imageData.getWidth()<4000 && imageData.getHeight()<4000) || decodeColorData.getID()==ColorSpaces.DeviceGray)){ //limit added after silly sizes on Customers3/1773_A2.pdf
        
            final byte[] data=imageData.getObjectData();

            //copy and turn upside down first
            final int count=data.length;

            final byte[] turnedData=new byte[count];
            System.arraycopy(data,0,turnedData,0,count);

            //invert all the bits if needed before we store
            if(arrayInverted){
                for(int aa=0;aa<count;aa++) {
                    turnedData[aa] = (byte) (turnedData[aa] ^ 255);
                }
            }
        
            final String key = parserOptions.getPageNumber() + String.valueOf(imageCount);
            
            if(saveData){
                objectStoreStreamRef.saveRawImageData(key,turnedData,imageData.getWidth(),imageData.getHeight(),imageData.getpX(), imageData.getpY(),maskCol,decodeColorData.getID());
            }else{
                objectStoreStreamRef.saveRawImageData(key,turnedData,imageData.getWidth(),imageData.getHeight(),imageData.getpX(), imageData.getpY(),null,decodeColorData.getID());
            }
        }
    }
    
}
