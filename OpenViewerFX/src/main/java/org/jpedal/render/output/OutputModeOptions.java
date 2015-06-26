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
 * ViewMode.java
 * ---------------
 */
package org.jpedal.render.output;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class OutputModeOptions {
    protected abstract int getValue();
    protected abstract void setDefaults();
    protected abstract void setValuesFromJVMProperties(final Map<String, String> jvmOptions);

    public OutputModeOptions(Map<String, String> jvmOptions) {
        setDefaults();

        // We need to combine the JVM options here otherwise the new API entry point will not observe JVM set options.
        // ExtractPagesAsHTML will do this work twice, but there should be no side effects.
        if (jvmOptions == null) {
            jvmOptions = new HashMap<String, String>();
        }
        //Combine system properties and passed in properties into a single Map
        final Properties sysProps = System.getProperties();
        final Enumeration<String> e = (Enumeration<String>) sysProps.propertyNames();
        while (e.hasMoreElements()) {
            final String key = e.nextElement();
            final String value = sysProps.getProperty(key);
            jvmOptions.put(key, value);
        }

        this.jvmOptions = jvmOptions;
        setValuesFromJVMProperties(jvmOptions);
    }

    private String errors = "";
    protected void addError(final String error) {
        errors = errors + error + '\n';
    }
    protected String getErrors() {
        return errors;
    }

    protected Map<String, String> jvmOptions;
}
