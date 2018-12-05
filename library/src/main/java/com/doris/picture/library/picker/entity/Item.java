package com.doris.picture.library.picker.entity;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

/**
 * @author Doris
 * @date 2018/12/4
 */
public class Item implements Parcelable {
    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        @Nullable
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
    public static final long ITEM_ID_CAPTURE = -1;
    public static final String ITEM_DISPLAY_NAME_CAPTURE = "Capture";
    public final long id;
    public final String mimeType;
    public final Uri uri;
    public final long size;
    public final long duration;

    private Item(long id, String mimeType, long size, long duration) {
        this.id = id;
        this.mimeType = mimeType;
        Uri contentUri;
        if (isImage()) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (isVideo()) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            // ?
            contentUri = MediaStore.Files.getContentUri("external");
        }
        this.uri = ContentUris.withAppendedId(contentUri, id);
        this.size = size;
        this.duration = duration;
    }

    private Item(Parcel source) {
        id = source.readLong();
        mimeType = source.readString();
        uri = source.readParcelable(Uri.class.getClassLoader());
        size = source.readLong();
        duration = source.readLong();
    }

    public static Item valueOf(Cursor cursor) {
        return new Item(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)),
                cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)),
                cursor.getLong(cursor.getColumnIndex("duration")));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(mimeType);
        dest.writeParcelable(uri, 0);
        dest.writeLong(size);
        dest.writeLong(duration);
    }

    public Uri getContentUri() {
        return uri;
    }

    public boolean isCapture() {
        return id == ITEM_ID_CAPTURE;
    }

    public boolean isImage() {
        if (mimeType == null) return false;
        return mimeType.equals(PicturePickerMediaType.JPEG.toString())
                || mimeType.equals(PicturePickerMediaType.PNG.toString())
                || mimeType.equals(PicturePickerMediaType.GIF.toString())
                || mimeType.equals(PicturePickerMediaType.BMP.toString())
                || mimeType.equals(PicturePickerMediaType.WEBP.toString());
    }

    public boolean isGif() {
        if (mimeType == null) return false;
        return mimeType.equals(PicturePickerMediaType.GIF.toString());
    }

    public boolean isVideo() {
        if (mimeType == null) return false;
        return mimeType.equals(PicturePickerMediaType.MPEG.toString())
                || mimeType.equals(PicturePickerMediaType.MP4.toString())
                || mimeType.equals(PicturePickerMediaType.QUICKTIME.toString())
                || mimeType.equals(PicturePickerMediaType.THREEGPP.toString())
                || mimeType.equals(PicturePickerMediaType.THREEGPP2.toString())
                || mimeType.equals(PicturePickerMediaType.MKV.toString())
                || mimeType.equals(PicturePickerMediaType.WEBM.toString())
                || mimeType.equals(PicturePickerMediaType.TS.toString())
                || mimeType.equals(PicturePickerMediaType.AVI.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Item)) {
            return false;
        }

        Item other = (Item) obj;
        return id == other.id
                && (mimeType != null && mimeType.equals(other.mimeType)
                || (mimeType == null && other.mimeType == null))
                && (uri != null && uri.equals(other.uri)
                || (uri == null && other.uri == null))
                && size == other.size
                && duration == other.duration;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Long.valueOf(id).hashCode();
        if (mimeType != null) {
            result = 31 * result + mimeType.hashCode();
        }
        result = 31 * result + uri.hashCode();
        result = 31 * result + Long.valueOf(size).hashCode();
        result = 31 * result + Long.valueOf(duration).hashCode();
        return result;
    }
}
