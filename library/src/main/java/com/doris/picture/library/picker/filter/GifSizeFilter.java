package com.doris.picture.library.picker.filter;

import android.content.Context;
import android.graphics.Point;

import com.doris.picture.library.R;
import com.doris.picture.library.picker.entity.IncapableCause;
import com.doris.picture.library.picker.entity.Item;
import com.doris.picture.library.picker.entity.PicturePickerMediaType;
import com.doris.picture.library.picker.utils.PhotoMetadataUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Doris
 * @date 2018/12/4
 */
public class GifSizeFilter extends Filter {

    private int mMinWidth;
    private int mMinHeight;
    private int mMaxSize;

    public GifSizeFilter(int minWidth, int minHeight, int maxSizeInBytes) {
        mMinWidth = minWidth;
        mMinHeight = minHeight;
        mMaxSize = maxSizeInBytes;
    }

    @Override
    public Set<PicturePickerMediaType> constraintTypes() {
        return new HashSet<PicturePickerMediaType>() {{
            add(PicturePickerMediaType.GIF);
        }};
    }

    @Override
    public IncapableCause filter(Context context, Item item) {
        if (!needFiltering(context, item)){
            return null;
        }

        Point size = PhotoMetadataUtils.getBitmapBound(context.getContentResolver(), item.getContentUri());
        if (size.x < mMinWidth || size.y < mMinHeight || item.size > mMaxSize) {
            return new IncapableCause(IncapableCause.DIALOG, context.getString(R.string.error_gif, mMinWidth,
                    String.valueOf(PhotoMetadataUtils.getSizeInMB(mMaxSize))));
        }
        return null;
    }
}
