package com.coderslagoon.badpeggy.scanner;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.event.IIOReadWarningListener;

import com.coderslagoon.baselib.util.BinUtils;
import com.coderslagoon.baselib.util.Log;
import com.coderslagoon.baselib.util.StopException;

public class ImageScanner implements IIOReadWarningListener, IIOReadProgressListener {
    public void imageComplete    (ImageReader a) { }
    public void imageStarted     (ImageReader a, int b) { }
    public void readAborted      (ImageReader a) { }
    public void sequenceComplete (ImageReader a) { }
    public void sequenceStarted  (ImageReader a, int b) { }
    public void thumbnailComplete(ImageReader a) { }
    public void thumbnailStarted (ImageReader a, int b, int c) { }
    public void thumbnailProgress(ImageReader a, float b) { }

    public void imageProgress(ImageReader source, float percentage) {
        if (null != this.callback) {
            if (!this.callback.onProgress(
                    (percentage + (this.imageIndex * 100.0)) /
                                   this.imageCount)) {
                throw new StopException();
            }
        }
    }

    public void warningOccurred(ImageReader source, String warning) {
        this.lastResult.msgs.add(warning);
    }

    int      imageIndex;
    int      imageCount;
    Result   lastResult;
    Callback callback;

    final static String LOG_CTX = "imgscan";
    static int nextLogId = 0;
    public Log log = new Log(LOG_CTX + "-" + nextLogId++);

    static {
        ImageIO.setUseCache(false);
    }

    static List<ImageReader> newReaders(ImageFormat ifmt) {
        List<ImageReader> result = new ArrayList<>();
        Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(ifmt.name);
        while (it.hasNext()) {
            result.add(it.next());
        }
        return result;
    }

    public static class Result {
        public enum Type {
            INVALID(-1),
            OK(0),
            WARNING(1),
            ERROR(2),
            UNEXPECTED_ERROR(3);
            Type(int code) {
                this.code = code;
            }
            int code;
            public int code() {
                return this.code;
            }
        }

        protected List<String> msgs = new ArrayList<>();
        public Iterable<String> messages() {
            return this.msgs;
        }
        protected HashSet<String> cmsgs;
        public Iterable<String> collapsedMessages() {
            if (null == this.cmsgs) {
                this.cmsgs = new HashSet<>();
                this.cmsgs.addAll(this.msgs);
            }
            return this.cmsgs;
        }
        public Type type() {
            return this.type;
        }
        protected Type type = Type.INVALID;

        public Object tag;
    }

    public Result lastResult() {
        return this.lastResult;
    }

    @FunctionalInterface
    public interface Callback {
        boolean onProgress(double percent);
    }

    public interface InputStreamSource {
        InputStream get() throws IOException;
    }

    public Boolean scan(InputStreamSource iss, ImageFormat ifmt, Callback callback) {

        this.lastResult = new Result();
        this.callback = callback;
        List<ImageReader> ireaders = newReaders(ifmt);
        if (0 == ireaders.size()) {
            return null;
        }
        for (ImageReader ireader : ireaders) {
            int lastResultMsgsSize = this.lastResult.msgs.size();
            InputStream ins = null;
            try {

                ireader.removeAllIIOReadProgressListeners();
                ireader.removeAllIIOReadUpdateListeners();
                ireader.removeAllIIOReadWarningListeners();

                ireader.addIIOReadWarningListener(this);
                ireader.addIIOReadProgressListener(this);

                ins = iss.get();
                ireader.setInput(ImageIO.createImageInputStream(ins));

                this.imageCount = ireader.getNumImages(true);

                for (this.imageIndex = 0; this.imageIndex < this.imageCount;) {
                    BufferedImage bimg = ireader.read(this.imageIndex++);

                    this.log.debug("image decoded (" + bimg.getWidth () + "x" +
                                                  bimg.getHeight() + ")");
                }

                this.lastResult.type = lastResultMsgsSize == this.lastResult.msgs.size() ?
                        Result.Type.OK :
                        Result.Type.WARNING;
                break;
            }
            catch (NegativeArraySizeException nase) {
                if (new Exception().getStackTrace().length <
                    nase           .getStackTrace().length) {
                    this.lastResult.msgs.add("Internal decoder error 1");
                    this.lastResult.type = Result.Type.ERROR;
                }
                else {
                    throw nase;
                }
            }
            catch (ArrayIndexOutOfBoundsException aioobe) {
                if (new Exception().getStackTrace().length <
                    aioobe         .getStackTrace().length) {
                    this.lastResult.msgs.add("Internal decoder error 2");
                    this.lastResult.type = Result.Type.ERROR;
                }
                else {
                    throw aioobe;
                }
            }
            catch (IIOException iioe) {
                this.log.infof("decoding failed (%s)", iioe.getMessage());
                this.lastResult.msgs.add(iioe.getMessage());
                this.lastResult.type = Result.Type.ERROR;
            }
            catch (EOFException eofe) {
                final String MSG = "premature file end";
                this.log.infof(MSG);
                this.lastResult.msgs.add(MSG);
                this.lastResult.type = Result.Type.ERROR;
            }
            catch (Throwable e) {
                System.gc();
                Log.exception(Log.Level.ERROR, "unexpected error", e);
                String msg = e.getMessage();
                if (null == msg) {
                    this.lastResult.msgs.add(e.toString());
                    this.lastResult.type = Result.Type.UNEXPECTED_ERROR;
                }
                else {
                    this.lastResult.msgs.add(msg);
                    this.lastResult.type = Result.Type.ERROR;
                }
            }
            finally {
                if (null != ins) try { ins.close(); } catch (IOException ioe) { }
                ireader.dispose();
            }
        }
        this.callback = null;
        return Result.Type.OK == this.lastResult.type();
    }

    public final static byte[] SELFTEST_DATA =
        BinUtils.base64Decode("/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAKBueIx4ZKCMgoy0qqC+8P//8Nzc8P//////////////////////////////////////////////////////////2wBDAaq0tPDS8P//////////////////////////////////////////////////////////////////////////////wAARCAAQABADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwBOppfp0/z/AJ8AAAAAAAAAAAAA");

    public static boolean selfTest() {
        try {
            ImageScanner imgScan = new ImageScanner();
            Boolean res = imgScan.scan(new InputStreamSource() {
                public InputStream get() {
                    return new ByteArrayInputStream(SELFTEST_DATA);
                }
            }, ImageFormat.JPEG, null);
            if (null == res) {
                imgScan.log.fatal("selftest failed: image scanner not functioning");
                return false;
            }
            if (res) {
                imgScan.log.fatal("selftest failed: scan returned true");
                return false;
            }
            if (imgScan.lastResult.type() != Result.Type.WARNING) {
                imgScan.log.fatalf("selftest failed (result is '%s')", imgScan.lastResult.type());
                return false;
            }
        }
        catch (Exception e) {
            Log.exception(Log.Level.FATAL, "selftest exception", e);
            return false;
        }
        return true;
    }
}
