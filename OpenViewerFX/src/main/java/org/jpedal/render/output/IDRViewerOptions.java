package org.jpedal.render.output;

import java.util.Map;

public class IDRViewerOptions extends OutputModeOptions {

    public IDRViewerOptions(final Map<String, String> jvmOptions) {
        super(jvmOptions);
    }

    public IDRViewerOptions() {
        this(null);
    }

    @Override
    protected int getValue() {
        return OutputDisplay.VIEW_IDR;
    }

    @Override
    protected void setValuesFromJVMProperties(final Map<String, String> jvmOptions) {
        this.jvmOptions = jvmOptions;
        final String prefix = "org.jpedal.pdf2html.";
        for (String key : jvmOptions.keySet()) {
            if (key.startsWith(prefix)) {
                key = key.substring(20);
                if (key.equals("googleAnalyticsID")) {
                    setJVMGoogleAnalyticsID("googleAnalyticsID");
                } else if (key.equals("pageTurningAnalyticsPrefix")) {
                    pageTurningAnalyticsPrefix = jvmOptions.get(prefix + "pageTurningAnalyticsPrefix");
                } else if (key.equals("insertIntoHead")) {
                    insertIntoHead = jvmOptions.get(prefix + "insertIntoHead");
                } else if (key.equals("toolBarPDFLink")){
                    toolBarPDFLink = setJVMBooleanValue(key);
                } else if (key.equals("toolBarLink")) {
                    setJVMToolBarLink(key);
                }
            }
        }

        jvmOptions.remove(prefix + "googleAnalyticsID");
        jvmOptions.remove(prefix + "pageTurningAnalyticsPrefix");
        jvmOptions.remove(prefix + "insertIntoHead");
        jvmOptions.remove(prefix + "toolBarPDFLink");
        jvmOptions.remove(prefix + "toolBarLink");
        jvmOptions.remove(prefix + "userHeadIndex");
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



    private String googleAnalyticsID;

    /**
     * Viewer specific setting only available when VIEWMODE is not set to CONTENT.
     * <p>
     * Pass in Google Analytics tracking ID to enable analytics.
     * <p>
     * Possible values:
     * <ul>
     *      <li>UA-#######-#</li>
     * </ul>
     * <p>
     * <b>Default: null</b>
     * <p>
     * Also set in the JVM options with <b><i>-Dorg.jpedal.pdf2html.googleAnalyticsID=</i></b>
     *
     * @param value The Google Analytics ID
     */
    public void setGoogleAnalyticsID(final String value) {
        googleAnalyticsID = value;
    }
    protected String getGoogleAnalyticsID() {
        return googleAnalyticsID;
    }
    protected void setJVMGoogleAnalyticsID(final String key) {
        String value = jvmOptions.get("org.jpedal.pdf2html." + key);
        value = value.toUpperCase();
        if (value.startsWith("UA-")) {
            googleAnalyticsID = value;
        } else {
            addError("Setting googleAnalyticsID failed with value: " + value + ". ID must start with UA");
        }
    }


    private String pageTurningAnalyticsPrefix;
    /**
     * Viewer specific setting only available when VIEWMODE is set to PAGETURNING.
     * <p>
     * Pass in a value to prefix the Analytics callback in PageTurning.
     * <p>
     * By default the analytics callback are the page numbers e.g, 01,02,03
     * however, by adding an analytics prefix with a value of "june" they
     * will become june/01, june/02, june/03, etc.
     * <p>
     * Possible values:
     * <ul>
     *      <li>Any string value</li>
     * </ul>
     * <p>
     * <b>Default: page</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.pageTurningAnalyticsPrefix=</i></b>
     *
     * @param value The Analytics Prefix
     */
    public void setPageTurningAnalyticsPrefix(final String value) {
        pageTurningAnalyticsPrefix = value;
    }
    protected String getPageTurningAnalyticsPrefix() {
        return pageTurningAnalyticsPrefix;
    }


    private String insertIntoHead;
    /**
     * Viewer specific setting only available when VIEWMODE is not set to CONTENT.
     * <p>
     * Pass in some HTML to add into the Head tag.
     * <p>
     * Possible values:
     * <ul>
     *      <li>Any String value</li>
     * </ul>
     * <p>
     * <b>Default: null</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.insertIntoHead=</i></b>
     *
     * @param value The HTML
     */
    public void setInsertIntoHead(final String value) {
        insertIntoHead = value;
    }
    protected String getInsertIntoHead() {
        return insertIntoHead;
    }


    private boolean toolBarPDFLink;
    /**
     * Viewer specific setting only available when VIEWMODE is not set to CONTENT and NAVMODE is set to IDRVIEWER.
     * <p>
     * Allow the original PDF to be downloaded from the Viewer.
     * <p>
     * Possible example values:
     * <ul>
     *      <li>toolBarPDFLink=true</li>
     *      <li>toolBarPDFLink=false</li>
     * </ul>
     * <p>
     * <b>Default: false</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.toolBarPDFLink=</i></b>
     *
     * @param b Boolean true/false
     */
    public void setEnableToolBarPDFDownload(final boolean b) {
        toolBarPDFLink = b;
    }

    public boolean getEnableToolBarPDFDownload() {
        return toolBarPDFLink;
    }


    private String[] toolBarLink;
    /**
     * Viewer specific setting only available when VIEWMODE is not set to CONTENT and NAVMODE is set to IDRVIEWER.
     * <p>
     * Alter the links for the tool bar home-button.
     * <p>
     * Links must be seperated by a comma.
     * <p>
     * Link one (before the comma) should be link to IMG url
     * Link two (after the comma) should be link to homepage
     * <p>
     * Possible example values:
     * <ul>
     *      <li>"www.linktologoimage.com,www.linktohomepage.com"</li>
     * </ul>
     * <p>
     * <b>Default: none</b>
     * <p>
     * Also set with <b><i>-Dorg.jpedal.pdf2html.toolBarLink=</i></b>
     *
     * @param imgURL The url of the image
     * @param homepageURL The url of the homepage
     */
    public void setToolBarLink(final String imgURL, final String homepageURL) {
        toolBarLink = new String[2];
        toolBarLink[0] = imgURL;
        toolBarLink[1] = homepageURL;
    }

    /**
     * Sets the toolBarLink from the JVM options.
     * key is everything after org.jpedal.pdf2html.
     * @param key is of type String
     */
    private void setJVMToolBarLink(final String key){

        final String value = jvmOptions.get("org.jpedal.pdf2html." + key);

        //Split the string before and after the comma
        final String imgURL = value.substring(0, value.indexOf(','));
        final String homeURL = value.substring(value.indexOf(',')+1);

        //setup the toolBarLinks.
        setToolBarLink(imgURL, homeURL);
    }
    protected String[] getToolBarLink() {
        return toolBarLink;
    }


    /**
     * Called from the constructor of ConversionOptions before JVM values are set
     */
    @Override
    protected void setDefaults() {
        setToolBarLink(null, null);
    }

}
