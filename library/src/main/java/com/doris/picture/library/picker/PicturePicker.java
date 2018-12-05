package com.doris.picture.library.picker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.doris.picture.library.PictureUtils;
import com.doris.picture.library.picker.activity.PicturePickerActivity;
import com.doris.picture.library.picker.entity.Item;
import com.doris.picture.library.picker.entity.PicturePickerMediaType;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

/**
 * @author Doris
 * @date 2018/12/4
 */
public class PicturePicker {

    private final WeakReference<Activity> mContext;
    private final WeakReference<Fragment> mFragment;

    private PicturePicker(Activity activity) {
        this(activity, null);
    }

    private PicturePicker(Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private PicturePicker(Activity activity, Fragment fragment) {
        mContext = new WeakReference<>(activity);
        mFragment = new WeakReference<>(fragment);
    }

    public static PicturePicker from(Activity activity) {
        return new PicturePicker(activity);
    }

    public static PicturePicker from(Fragment fragment) {
        return new PicturePicker(fragment);
    }

    public static List<Uri> obtainResult(Intent data) {
        return data.getParcelableArrayListExtra(PictureUtils.EXTRA_RESULT_SELECTION);
    }

    public static List<Item> obtainItemResult(Intent data) {
        return data.getParcelableArrayListExtra(PictureUtils.EXTRA_RESULT_SELECTION_ITEM);
    }

    public static List<String> obtainPathResult(Intent data) {
        return data.getStringArrayListExtra(PictureUtils.EXTRA_RESULT_SELECTION_PATH);
    }

    /**
     * 是否选择原图
     * @param data Intent
     * @return boolean
     */
    public static boolean obtainOriginalState(Intent data) {
        return data.getBooleanExtra(PictureUtils.EXTRA_RESULT_ORIGINAL_ENABLE, false);
    }

    public SelectionCreator choose(Set<PicturePickerMediaType> picturePickerMediaTypes) {
        return this.choose(picturePickerMediaTypes, true);
    }

    public SelectionCreator choose(Set<PicturePickerMediaType> picturePickerMediaTypes, boolean mediaTypeExclusive) {
        return new SelectionCreator(this, picturePickerMediaTypes, mediaTypeExclusive);
    }

    @Nullable
    Activity getActivity() {
        return mContext.get();
    }

    @Nullable
    Fragment getFragment() {
        return mFragment != null ? mFragment.get() : null;
    }
}
