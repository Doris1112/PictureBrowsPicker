package com.doris.picture.library.brows.listener;

/**
 * @author Doris
 * @date 2018/12/4
 */
public interface SaveImageListener {

    /**
     * 保存图片成功
     * @param absolutePath 图片绝对路径
     */
    void onSuccess(String absolutePath);

    /**
     * 保存图片失败
     */
    void onFail();

}
