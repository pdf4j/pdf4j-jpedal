package org.jpedal.render.output;

import org.jpedal.render.output.io.DefaultIO;

import java.util.Map;

public class ContentOptions extends OutputModeOptions {

    public ContentOptions(final Map<String, String> jvmOptions) {
        super(jvmOptions);
    }

    public ContentOptions() {
        this(null);
    }

    @Override
    protected int getValue() {
        return OutputDisplay.VIEW_CONTENT;
    }

    @Override
    protected void setValuesFromJVMProperties(final Map<String, String> jvmOptions) {
        this.jvmOptions = jvmOptions;
        for (String key : jvmOptions.keySet()) {
            final String prefix = "org.jpedal.pdf2html.";
            if (key.startsWith(prefix)) {
                key = key.substring(20);

                if (key.equals("outputThumbnails")) {
                    outputThumbnails = setJVMBooleanValue(key);
                } else if (key.equals("completeDocument")) {
                    completeDocument = setJVMBooleanValue(key);
                }

            }
        }
    }


    private boolean outputThumbnails;
    /**
     * Output thumbnails of pages in /thumbnails/.
     * <p>
     * Possible value:
     * <ul>
     *      <li>true/false</li>
     * </ul>
     * <p>
     * <b>Default: false</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.outputThumbnails=</i></b>
     *
     * @param value is of type boolean.
     */
    public void setOutputThumbnails(final boolean value) {
        outputThumbnails = value;
    }
    protected boolean getOutputThumbnails() {
        return outputThumbnails;
    }

    private boolean completeDocument;
    /**
     * Enabling this setting will output html, head and body tags.
     * <p>
     * This mode is recommended if content will be displayed within iframes.
     * <p>
     * This setting has no behavior in SVG conversion.
     * <p>
     * Possible value:
     * <ul>
     *      <li>true/false</li>
     * </ul>
     * <p>
     * <b>Default: false</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.completeDocument=</i></b>
     *
     * @param value is of type boolean
     */
    public void setCompleteDocument(final boolean value) {
        completeDocument = value;
    }
    protected boolean getCompleteDocument() {
        return completeDocument;
    }




    /**
     * Called from the constructor of ConversionOptions before JVM values are set
     */
    @Override
    protected void setDefaults() {
        if (DefaultIO.isTest) {
            completeDocument = true;
        }
    }

    // Duplicate in OutputDisplay, remove this one in future
    protected boolean setJVMBooleanValue(final String key) {
        String value = jvmOptions.get("org.jpedal.pdf2html." + key);
        value = value.toLowerCase();
        if (value.equals("true")) {
            return true;
        } else if (value.equals("false")) {
            return false;
        } else {
            addError("Value \"" + value + "\" for " + key + " was not recognised. Use true or false.");
            return false;
        }
    }

}
