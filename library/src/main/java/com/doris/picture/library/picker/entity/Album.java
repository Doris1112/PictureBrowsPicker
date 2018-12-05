package com.doris.picture.library.picker.entity;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.doris.picture.library.R;
import com.doris.picture.library.picker.loader.AlbumLoader;

/**
 * @author Doris
 * @date 2018/12/4
 */
public class Album implements Parcelable {

    private static final String ALBUM_CAMERA = "camera";
    private static final String ALBUM_SCREENSHOTS = "screenshots";
    private static final String ALBUM_DOWNLOAD = "download";

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Nullable
        @Override
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    public static final String ALBUM_ID_ALL = String.valueOf(-1);
    public static final String ALBUM_NAME_ALL = "All";

    private final String mId;
    private final String mCoverPath;
    private final String mDisplayName;
    private long mCount;

    Album(String id, String coverPath, String albumName, long count) {
        mId = id;
        mCoverPath = coverPath;
        mDisplayName = albumName;
        mCount = count;
    }

    Album(Parcel source) {
        mId = source.readString();
        mCoverPath = source.readString();
        mDisplayName = source.readString();
        mCount = source.readLong();
    }

    public static Album valueOf(Cursor cursor) {
        return new Album(cursor.getString(cursor.getColumnIndex("bucket_id")),
                cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)),
                getAlbumName(cursor.getString(cursor.getColumnIndex("bucket_display_name"))),
                cursor.getLong(cursor.getColumnIndex(AlbumLoader.COLUMN_COUNT)));
    }

    private static String getAlbumName(String name) {
        if (ALBUM_CAMERA.equalsIgnoreCase(name)) {
            return "相机";
        } else if (ALBUM_SCREENSHOTS.equalsIgnoreCase(name)) {
            return "截图";
        } else if (ALBUM_DOWNLOAD.equalsIgnoreCase(name)) {
            return "下载";
        } else {
            return name;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mCoverPath);
        dest.writeString(mDisplayName);
        dest.writeLong(mCount);
    }

    public String getId() {
        return mId;
    }

    public String getCoverPath() {
        return mCoverPath;
    }

    public long getCount() {
        return mCount;
    }

    public void addCaptureCount() {
        mCount++;
    }

    public String getDisplayName(Context context) {
        if (isAll()) {
            return context.getString(R.string.album_name_all);
        }
        return mDisplayName;
    }

    public boolean isAll() {
        return ALBUM_ID_ALL.equals(mId);
    }

    public boolean isEmpty() {
        return mCount == 0;
    }
}
