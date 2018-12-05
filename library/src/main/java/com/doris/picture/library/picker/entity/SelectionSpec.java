package com.doris.picture.library.picker.entity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.doris.picture.library.R;
import com.doris.picture.library.picker.engine.GlideEngine;
import com.doris.picture.library.picker.engine.ImageEngine;
import com.doris.picture.library.picker.filter.Filter;
import com.doris.picture.library.picker.listener.OnCheckedListener;
import com.doris.picture.library.picker.listener.OnSelectedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Doris
 * @date 2018/12/4
 */
public final class SelectionSpec {

    private static final String TAG = SelectionSpec.class.getSimpleName();

    public Set<PicturePickerMediaType> picturePickerMediaTypeSet;
    public boolean mediaTypeExclusive;
    public boolean showSingleMediaType;
    public int themeId, maxSelectable, maxImageSelectable, maxVideoSelectable,
            spanCount, gridExpectedSize, originalMaxSize;
    public boolean countable, capture, originalMap, autoHideToolbar, crop, isOval, isRefresh;
    public String saveImagePath, saveCropImageName;
    public Uri cropUri;
    public Bitmap cropBitmap;
    public int cropWidth, cropHeight, cropQuality;
    public Bitmap.CompressFormat cropCompressFormat;
    public List<Filter> filters;
    public float thumbnailScale;
    public List<Item> selectorList;
    public ImageEngine imageEngine;
    public OnSelectedListener onSelectedListener;
    public OnCheckedListener onCheckedListener;

    private SelectionSpec() { }

    public static SelectionSpec getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static SelectionSpec getCleanInstance() {
        SelectionSpec selectionSpec = getInstance();
        selectionSpec.reset();
        return selectionSpec;
    }

    private void reset() {
        picturePickerMediaTypeSet = null;
        mediaTypeExclusive = true;
        showSingleMediaType = false;
        themeId = R.style.PicturePicker_default;
        countable = false;
        maxSelectable = 1;
        maxImageSelectable = 0;
        maxVideoSelectable = 0;
        filters = null;
        capture = false;
        spanCount = 3;
        gridExpectedSize = 0;
        thumbnailScale = 0.5f;
        imageEngine = new GlideEngine();
        originalMap = false;
        autoHideToolbar = false;
        originalMaxSize = Integer.MAX_VALUE;
        saveImagePath = getSaveImagePath();
        selectorList = new ArrayList<>();
        isRefresh = true;
        crop = false;
        cropUri = null;
        cropBitmap = null;
        isOval = false;
        cropWidth = -1;
        cropHeight = -1;
        saveCropImageName = "";
        cropCompressFormat = Bitmap.CompressFormat.JPEG;
        cropQuality = 80;
    }

    private static String getSaveImagePath(){
        String filePath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/crop/";
        File file = new File(filePath);
        if (!file.exists()){
            if (!file.mkdirs()){
                Log.e(TAG, "SelectionSpec: 创建文件夹失败");
            }
        }
        return filePath;
    }

    public boolean singleSelectionModeEnabled() {
        return !countable && (maxSelectable == 1 || (maxImageSelectable == 1 && maxVideoSelectable == 1));
    }

    public boolean onlyShowImages() {
        return showSingleMediaType && PicturePickerMediaType.ofImage().containsAll(picturePickerMediaTypeSet);
    }

    public boolean onlyShowVideos() {
        return showSingleMediaType && PicturePickerMediaType.ofVideo().containsAll(picturePickerMediaTypeSet);
    }

    private static final class InstanceHolder {
        private static final SelectionSpec INSTANCE = new SelectionSpec();
    }
}
