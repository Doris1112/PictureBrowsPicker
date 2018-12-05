package com.doris.picture.library.picker.filter;

import android.content.Context;

import com.doris.picture.library.picker.entity.IncapableCause;
import com.doris.picture.library.picker.entity.Item;
import com.doris.picture.library.picker.entity.PicturePickerMediaType;

import java.util.Set;

/**
 * @author Doris
 * @date 2018/12/4
 */
public abstract class Filter {

    public static final int MIN = 0;
    public static final int MAX = Integer.MAX_VALUE;
    public static final int K = 1024;

    protected boolean needFiltering(Context context, Item item) {
        for (PicturePickerMediaType type : constraintTypes()) {
            if (type.checkType(context.getContentResolver(), item.getContentUri())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 约束类型
     * @return Set<PicturePickerMediaType>
     */
    protected abstract Set<PicturePickerMediaType> constraintTypes();

    public abstract IncapableCause filter(Context context, Item item);
}
