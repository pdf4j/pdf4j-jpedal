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
 * GUISearchList.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.generic;

import java.util.Map;

public interface GUISearchList {

    public static final int NO_RESULTS_FOUND = 1;
    public static final int SEARCH_COMPLETE_SUCCESSFULLY = 2;
    public static final int SEARCH_INCOMPLETE = 4;
    public static final int SEARCH_PRODUCED_ERROR = 8;
    
	public Map getTextPages();

	public Map textAreas();
	
	/**
	 * Find out the current amount of results found
	 * @return the amount of search results found
	 */
	public int getResultCount();
	
	public void setSearchTerm(String term);
	
	public String getSearchTerm();
	
	public int getSelectedIndex();
	
	public void setSelectedIndex(int index);
	
	//public int getStatus();

	//public void setStatus(int status);
}
