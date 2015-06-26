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
 * DefaultIO.java
 * ---------------
 */
package org.jpedal.render.output.io;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
//
import org.jpedal.utils.LogWriter;

public class DefaultIO implements CustomIO {

    /**used as part of our internal testing to limit pages to first 15 -
     please ignore*/
    public static boolean isTest;

    private BufferedWriter output;

    // Put image output inside a thread to speed up conversion
    private ExecutorService executorService;
    private Semaphore blocker;

    private final boolean useImageOutputThread;
    private final boolean useLegacyImageFileType;

    /**
     * Constructs a DefaultIO that will use a separate image output thread.
     * Only safe to use if waitForImages() is called after page decodes are finished.
     */
    public DefaultIO() {
        this(true, false);
    }

    /**
     * Constructs a DefaultIO
     * @param useImageOutputThread Whether a separate image output thread should be used. (Not for ExtractPagesAsHTML)
     * @param useLegacyImageFileType Whether to use legacy image file type (png)
     */
    public DefaultIO(final boolean useImageOutputThread, final boolean useLegacyImageFileType) {
        this.useImageOutputThread = useImageOutputThread;
        this.useLegacyImageFileType = useLegacyImageFileType;
    }
    
    @Override
    public void writeFont(final String path, final byte[] rawFontData) {

        //make sure dir exists
        final File dir = new File(path).getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {

            final BufferedOutputStream fontOutput = new BufferedOutputStream(new FileOutputStream(path));
            fontOutput.write(rawFontData);
            fontOutput.flush();
            fontOutput.close();

        } catch (final Exception e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
    }

    @Override
    public void writePlainTextFile(final String path, final StringBuilder content) {

        //make sure dir exists
        final File dir = new File(path).getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {

            final BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(path));
            output.write(content.toString().getBytes());
            output.flush();
            output.close();

        } catch (final Exception e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
    }

    @Override
    public void writeFileFromStream(final InputStream is, final String path) {
        try{
            OutputStream os = null;
            try {
                new File(path).getParentFile().mkdirs();
                os = new FileOutputStream(path);
                final byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } finally {
                is.close();
                if (os != null) {
                    os.close();
                }
            }
        }catch(final Exception e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
    }

    @Override
    public boolean isOutputOpen() {
        return output!=null;
    }

    @Override
    public void setupOutput(final String path, final boolean append, final String encodingUsed) throws FileNotFoundException, UnsupportedEncodingException {

        output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path,append), encodingUsed));

    }
    
    @Override
    public void setupOutput(final OutputStream path, final boolean append, final String encodingUsed) throws FileNotFoundException, UnsupportedEncodingException {

        output = new BufferedWriter(new OutputStreamWriter(path, encodingUsed));

    }

    @Override
    public void flush() {

        try{
            output.flush();
            output.close();

            output =null;
        }catch(final Exception e){
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
    }

    @Override
    public void writeString(final String str) {

        try {
            output.write(str);
            output.write('\n');
            output.flush();
        } catch (final Exception e) {
            //tell user and log
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
    }

    //@here - lots in routine
    @Override
    public void writeImage(final String rootDir, final String path, final BufferedImage image, final ImageType imageType) {

        final ImageFileType imageFileType = getImageTypeUsed(imageType);
        final String file=path+imageFileType.getFileExtension();
        final String fullPath=rootDir+file;

        //make sure img Dir exists
        final File imgDir = new File(fullPath).getParentFile();
        if (!imgDir.exists()) {
            imgDir.mkdirs();
        }

        // Put image output inside a thread to speed up conversion (reuse the same thread for all images)
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    if (imageFileType == ImageFileType.JPG) {
                        final ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
                        final ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
                        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        jpgWriteParam.setCompressionQuality(0.9f);// Higher quality than default

                        final FileImageOutputStream imageOutputStream = new FileImageOutputStream(new File(fullPath));
                        jpgWriter.setOutput(imageOutputStream);

                        final IIOImage outputImage = new IIOImage(image, null, null);
                        jpgWriter.write(null, outputImage, jpgWriteParam);
                        jpgWriter.dispose();
                        imageOutputStream.close();
                    //
                    } else{
                        final BufferedOutputStream imageOutputStream = new BufferedOutputStream(new FileOutputStream(new File(fullPath)));

                        // Write out image using ImageIO
                        ImageIO.write(image, imageFileType.getIoType(), imageOutputStream);

                        imageOutputStream.flush();
                        imageOutputStream.close();
                    }

                    if (useImageOutputThread) {
                        blocker.release();
                    }

                    // See http://blog.idrsolutions.com/2014/01/reducing-the-file-size-of-converted-pdfs-using-pngquant/
                    // Example code for pngquant:
                    /*
                    if (imageFileType == ImageFileType.PNG) {
                        try {
                            Process p = Runtime.getRuntime().exec("/path/to/pngquant --ext .png --force --speed 10 \"" + fullPath + "\"");
                            //
                            p.waitFor();
                        } catch (IOException e) {
                            if (LogWriter.isOutput())
                                LogWriter.writeLog("Exception: " + e.getMessage());
                            //
                        } catch (InterruptedException e) {
                            if (LogWriter.isOutput())
                                LogWriter.writeLog("Exception: " + e.getMessage());
                            //
                        }
                    }
                    /**/
                    
                }catch(final IOException e){
                    //tell user and log
                    if(LogWriter.isOutput()) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                    //
                }
            }
        };

        if (useImageOutputThread) {
            if (executorService == null) {
                executorService = Executors.newFixedThreadPool(1);// Single thread as ImageIO is not thread safe
                blocker = new Semaphore(10);// Limit to 10 images in the queue
            }

            try {
                blocker.acquire();// Blocks if the queue is too long
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception in handling thread "+e);
                }
            }
            executorService.submit(r);
        } else {
            r.run();
        }

    }
    
    /**
     * Grab file type to use for output for specific type of image
     * @return ImageFileType to use
     */
    @Override
    public ImageFileType getImageTypeUsed(final ImageType imageType){
        if (useLegacyImageFileType) {
            return ImageFileType.PNG;
        } else {
            switch(imageType) {
                case BACKGROUND:
                case THUMBNAIL:
                    return ImageFileType.JPG;
                case SVG:
                case FORM:
                case SHADE:
                case IEOVERLAY:
                    return ImageFileType.PNG;
                default:
                    throw new RuntimeException("Unknown image type used in getImageTypeUsed()");
            }
        }
    }

    /**
     * If using a separate image output thread, this should be called after pages have
     * finished decoding to wait for image images to finish outputting.
     */
    @Override
    public void waitForImages() {
        if (useImageOutputThread && executorService != null) {
            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.DAYS);
            } catch (final InterruptedException e) {
                //tell user and log
                if(LogWriter.isOutput()) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
                //
            }
            executorService = null;
        }
    }
    
}
