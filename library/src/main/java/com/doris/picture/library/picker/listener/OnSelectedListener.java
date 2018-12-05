package com.doris.picture.library.picker.listener;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * @author Doris
 * @date 2018/12/4
 */
public interface OnSelectedListener {

    /**
     * 选中事件
     * @param uriList 选中Uri集合
     * @param pathList 选中（路径）String集合
     */
    void onSelected(@NonNull List<Uri> uriList, @NonNull List<String> pathList);
}
