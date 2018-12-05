package com.doris.picture.library.picker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.doris.picture.library.picker.activity.CropActivity;
import com.doris.picture.library.picker.activity.PicturePickerActivity;
import com.doris.picture.library.picker.engine.ImageEngine;
import com.doris.picture.library.picker.entity.Item;
import com.doris.picture.library.picker.entity.PicturePickerMediaType;
import com.doris.picture.library.picker.entity.SelectionSpec;
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
public final class SelectionCreator {

    private static final String TAG = SelectionCreator.class.getSimpleName();

    private final PicturePicker mPicturePicker;
    private final SelectionSpec mSelectionSpec;

    SelectionCreator(PicturePicker picturePicker, Set<PicturePickerMediaType> picturePickerMediaTypes, boolean mediaTypeExclusive) {
        mPicturePicker = picturePicker;
        mSelectionSpec = SelectionSpec.getCleanInstance();
        mSelectionSpec.picturePickerMediaTypeSet = picturePickerMediaTypes;
        mSelectionSpec.mediaTypeExclusive = mediaTypeExclusive;
    }

    public SelectionCreator showSingleMediaType(boolean showSingleMediaType) {
        mSelectionSpec.showSingleMediaType = showSingleMediaType;
        return this;
    }

    public SelectionCreator theme(@StyleRes int themeId) {
        mSelectionSpec.themeId = themeId;
        return this;
    }

    public SelectionCreator countable(boolean countable) {
        mSelectionSpec.countable = countable;
        return this;
    }

    public SelectionCreator maxSelectable(int maxSelectable) {
        mSelectionSpec.maxSelectable = maxSelectable;
        return this;
    }

    public SelectionCreator maxSelectablePerMediaType(int maxImageSelectable, int maxVideoSelectable) {
        mSelectionSpec.maxSelectable = -1;
        mSelectionSpec.maxImageSelectable = maxImageSelectable;
        mSelectionSpec.maxVideoSelectable = maxVideoSelectable;
        return this;
    }

    public SelectionCreator addFilter(@NonNull Filter filter) {
        if (mSelectionSpec.filters == null) {
            mSelectionSpec.filters = new ArrayList<>();
        }
        mSelectionSpec.filters.add(filter);
        return this;
    }

    public SelectionCreator capture(boolean enable) {
        mSelectionSpec.capture = enable;
        return this;
    }

    public SelectionCreator isRefresh(boolean enable) {
        mSelectionSpec.isRefresh = enable;
        return this;
    }

    public SelectionCreator crop(boolean enable) {
        mSelectionSpec.crop = enable;
        return this;
    }

    public SelectionCreator cropUri(Uri uri) {
        mSelectionSpec.cropUri = uri;
        return this;
    }

    public SelectionCreator cropBitmap(Bitmap bitmap) {
        mSelectionSpec.cropBitmap = bitmap;
        return this;
    }

    public SelectionCreator cropIsOval(boolean isOval){
        mSelectionSpec.isOval = isOval;
        return this;
    }

    public SelectionCreator cropWidthAndrHeight(int width, int height){
        mSelectionSpec.cropWidth = width;
        mSelectionSpec.cropHeight = height;
        return this;
    }

    public SelectionCreator cropSaveName(String cropSaveName){
        mSelectionSpec.saveCropImageName = cropSaveName;
        return this;
    }

    public SelectionCreator cropCompressFormat(Bitmap.CompressFormat compressFormat){
        mSelectionSpec.cropCompressFormat = compressFormat;
        return this;
    }

    public SelectionCreator cropQuality(int quality){
        mSelectionSpec.cropQuality = quality;
        return this;
    }

    public SelectionCreator originalEnable(boolean enable) {
        mSelectionSpec.originalMap = enable;
        return this;
    }

    public SelectionCreator autoHideToolbarOnSingleTap(boolean enable) {
        mSelectionSpec.autoHideToolbar = enable;
        return this;
    }

    public SelectionCreator maxOriginalSize(int size) {
        mSelectionSpec.originalMaxSize = size;
        return this;
    }

    public SelectionCreator saveImagePath(String saveImagePath) {
        mSelectionSpec.saveImagePath = saveImagePath;
        File file = new File(mSelectionSpec.saveImagePath);
        if (!file.exists()) {
            if (!file.mkdirs()){
                Log.e(TAG, "SelectionCreator: 创建文件夹失败");
            }
        }
        return this;
    }

    public SelectionCreator spanCount(int spanCount) {
        if (spanCount >= 1) {
            mSelectionSpec.spanCount = spanCount;
        }
        return this;
    }

    public SelectionCreator gridExpectedSize(int size) {
        mSelectionSpec.gridExpectedSize = size;
        return this;
    }

    public SelectionCreator thumbnailScale(float scale) {
        if (scale > 0f && scale <= 1f) {
            mSelectionSpec.thumbnailScale = scale;
        }
        return this;
    }

    public SelectionCreator imageEngine(ImageEngine imageEngine) {
        mSelectionSpec.imageEngine = imageEngine;
        return this;
    }

    public SelectionCreator selectorList(List<Item> items) {
        mSelectionSpec.selectorList = items;
        return this;
    }

    @NonNull
    public SelectionCreator setOnSelectedListener(@Nullable OnSelectedListener listener) {
        mSelectionSpec.onSelectedListener = listener;
        return this;
    }

    public SelectionCreator setOnCheckedListener(@Nullable OnCheckedListener listener) {
        mSelectionSpec.onCheckedListener = listener;
        return this;
    }

    public void forResult(int requestCode) {
        Activity activity = mPicturePicker.getActivity();
        Fragment fragment = mPicturePicker.getFragment();
        if (activity == null && fragment == null) {
            return;
        }
        Intent intent = new Intent(activity, PicturePickerActivity.class);
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public void onlyCropForResult(int requestCode){
        Activity activity = mPicturePicker.getActivity();
        Fragment fragment = mPicturePicker.getFragment();
        if (activity == null && fragment == null) {
            return;
        }
        Intent intent = new Intent(activity, CropActivity.class);
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }
}
