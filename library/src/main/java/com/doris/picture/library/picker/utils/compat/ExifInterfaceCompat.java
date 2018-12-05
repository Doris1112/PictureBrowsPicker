package com.doris.picture.library.picker.utils.compat;

import android.media.ExifInterface;
import android.text.TextUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Doris
 * @date 2018/12/4
 */
public final class ExifInterfaceCompat {

    private static final int EXIF_DEGREE_FALLBACK_VALUE = -1;

    private ExifInterfaceCompat() { }

    public static ExifInterface newInstance(String filename) throws IOException {
        if (filename == null){
            throw new NullPointerException("filename should not be null");
        }
        return new ExifInterface(filename);
    }

    private static Date getExifDateTime(String filepath) {
        ExifInterface exif;
        try {
            exif = newInstance(filepath);
        } catch (IOException ex) {
            return null;
        }

        String date = exif.getAttribute(ExifInterface.TAG_DATETIME);
        if (TextUtils.isEmpty(date)) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatter.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getExifDateTimeInMillis(String filepath) {
        Date datetime = getExifDateTime(filepath);
        if (datetime == null) {
            return -1;
        }
        return datetime.getTime();
    }

    public static int getExifOrientation(String filepath) {
        ExifInterface exif;
        try {
            exif = newInstance(filepath);
        } catch (IOException ex) {
            return EXIF_DEGREE_FALLBACK_VALUE;
        }

        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, EXIF_DEGREE_FALLBACK_VALUE);
        if (orientation == EXIF_DEGREE_FALLBACK_VALUE) {
            return 0;
        }
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }
}
