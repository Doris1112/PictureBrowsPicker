package com.doris.picture.library.picker.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.doris.picture.library.R;
import com.doris.picture.library.picker.entity.Album;
import com.doris.picture.library.picker.entity.SelectionSpec;

import java.io.File;

/**
 * @author Doris
 * @date 2018/12/4
 */
public class AlbumsAdapter extends CursorAdapter {

    private final Drawable mPlaceholder;

    public AlbumsAdapter(Context context, Cursor c, boolean autoQuery) {
        super(context, c, autoQuery);
        TypedArray ta = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.albumThumbnailPlaceholder});
        mPlaceholder = ta.getDrawable(0);
        ta.recycle();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.picture_picker_item_album_list, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Album album = Album.valueOf(cursor);
        ((TextView) view.findViewById(R.id.album_name)).setText(album.getDisplayName(context));
        ((TextView) view.findViewById(R.id.album_media_count)).setText(String.valueOf(album.getCount()));
        SelectionSpec.getInstance().imageEngine.loadThumbnail(context,
                context.getResources().getDimensionPixelSize(R.dimen.media_grid_size), mPlaceholder,
                (ImageView) view.findViewById(R.id.album_cover),
                Uri.fromFile(new File(album.getCoverPath())));
    }
}
