package com.doris.picture.library;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Doris
 * @date 2018/12/3
 */
public class PictureUtils {

    /**
     * 图片浏览
     */
    public static final int MAX_INDICATOR = 12,
            SAVE_STATUS_DOWNLOAD = 1,
            SAVE_STATUS_SUCCESS = 2,
            SAVE_STATUS_FAIL = 3,
            START_LENGTH = 4,
            REQUEST_WRITE_PERMISSION = 1001,
            REQUEST_READ_PERMISSION = 1002,
            REQUEST_CAMERA_PERMISSION = 1003,
            REQUEST_CODE_PREVIEW = 1004,
            REQUEST_CODE_CAPTURE = 1005,
            REQUEST_CODE_CROP = 1006;
    ;
    public static final String START_HTTP = "http", START_DATA = "data",
            EXTRA_IMAGE_URL = "imageUrl",
            EXTRA_IMAGE_PATH = "imagePath",
            EXTRA_IMAGE = "image",
            EXTRA_POSITION = "imagePosition",
            EXTRA_SAVE = "imageSave",
            EXTRA_SAVE_PATH = "imageSavePath",
            EXTRA_SAVE_NAME = "imageSaveName",
            EXTRA_REFRESH = "imageRefresh",
            EXTRA_RESULT_SELECTION = "extra_result_selection",
            EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path",
            EXTRA_RESULT_SELECTION_ITEM = "extra_result_selection_item",
            EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable",
            EXTRA_DEFAULT_BUNDLE = "extra_default_bundle",
            EXTRA_RESULT_BUNDLE = "extra_result_bundle",
            EXTRA_RESULT_APPLY = "extra_result_apply",
            CHECK_STATE = "check_state",
            EXTRA_RESULT_CROP_PATH = "extra_crop_path",
            EXTRA_RESULT_CROP_URI = "extra_crop_uri";

    /**
     * 保存图片
     *
     * @param bitmap 需要保存Bitmap
     * @param path   保存的绝对路径
     */
    public static boolean saveImg(Bitmap bitmap, String path) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将字符串转换成Bitmap类型
     *
     * @param string 需要转换成Bitmap的字符串
     * @return Bitmap
     */
    public static Bitmap stringToBitmap(String string) {
        Bitmap bitmap = null;
        final String startData = "data:";
        try {
            byte[] bitmapArray;
            if (string.startsWith(startData)) {
                string = string.split(",")[1];
            }
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 同步媒体库
     *
     * @param context  Context
     * @param filePath 图片路径
     */
    public static void updateMedia(final Context context, String filePath) {
        try {
            String[] paths = new String[]{Environment.getExternalStorageDirectory().toString(),
                    filePath};
            MediaScannerConnection.scanFile(context, paths, null,
                    new MediaScannerConnection.OnScanCompletedListener(){
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            context.sendBroadcast(new Intent(
                                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取时间格式String
     *
     * @return String
     */
    public static String getDataTimeString() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
    }

    /**
     * 检查是否拥有指定的所有权限
     *
     * @param context     Context
     * @param permissions 权限数组
     */
    public static boolean checkPermissionAllGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
