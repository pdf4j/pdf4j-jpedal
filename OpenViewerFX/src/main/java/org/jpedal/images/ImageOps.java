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
 * ImageOps.java
 * ---------------
 */
package org.jpedal.images;

public class ImageOps {

	private static final int[] bitCheck={128,64,32,16,8,4,2,1};

	private static byte[] invertByteImage(final int w, final int h, int componentCount, final byte[] data) {
		
		
//		System.out.println("w="+w);
//		System.out.println("h="+h);
//		System.out.println("components="+componentCount);
//		System.out.println("data="+data.length);
		
		//Double check the componentCount
		final int temp = (data.length/(w*h));
		
		if(temp!=componentCount){
			componentCount=temp;
		}
		
		
		final int bytesInRow=w*componentCount;
		int topByte=0,bottomByte=((h-1)*bytesInRow);
		byte tempByte;

		while(topByte<bottomByte){

			for(int row=0;row<(bytesInRow);row+=componentCount){
				for(int c=0;c<componentCount;c++){
					tempByte=data[topByte+row+c];
					data[topByte+row+c]=data[bottomByte+row+c];
					data[bottomByte+row+c]=tempByte;
				}
			}
			topByte += bytesInRow;
			bottomByte -= bytesInRow;
		}
		return data;
	}

	private static byte[] invertOneBitImage(final int w, final int h, final byte[] data) {

		final int bytesInRow=(w+7)/8;
		int topByte=0,bottomByte=(h-1)*bytesInRow;
		byte tempByte;

		while(topByte<bottomByte){

			for(int row=0;row<bytesInRow;row++){
				tempByte=data[topByte+row];
				data[topByte+row]=data[bottomByte+row];
				data[bottomByte+row]=tempByte;
			}
			topByte += bytesInRow;
			bottomByte -= bytesInRow;
		}
		return data;
	}

	public static byte[] invertImage(byte[] data, final int w, final int h, final int d,int comp, final byte[] index) {
        
        //either process or just return
		if(d==8){ //add in next
			if(index!=null) {
                comp = 1;
            } else //will fail if wrong size
            {
                data = checkSize(data, w, h, comp);
            }
            
			return ImageOps.invertByteImage(w, h, comp, data);

		//}else if(d==4){



		}else if(d==1){

			return ImageOps.invertOneBitImage(w, h, data);

		}
		return null;
	}

    public static byte[] checkSize(byte[] data, final int w, final int h, final int comp) {

        final int correctSize=w*h*comp;
        if(data.length<correctSize){
            final byte[] newData=new byte[correctSize];
            System.arraycopy(data,0,newData,0,data.length);
            data=newData;
        }

        return data;
    }
}
