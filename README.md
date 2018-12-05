1、图片浏览
```
  PictureBrows.build(this)
    // 设置图片or图片集合(可添加Base64图片，这个操作会清空所有图片只留下设置图片)
    .setImage(PictureData.IMAGE1)
    .setImages(getImages())
    // 添加图片or图片集合(在原有图片的基础上再继续添加图片)
    .addImages(getImages())
    .addImage(PictureData.IMAGE1)
    // 设置进入时展示第position张图片(以0开始)
    .setPosition(1)
    // 是否可以保存图片(默认不可以保存)
    .isSave(true)
    // 设置图片保存路径(以“/”结束，不包含图片名称)
    .setSavePath(PictureData.SAVE_IMGE_PATH)
    // 保存图片成功是否需要刷新媒体库(默认刷新)
    .isRefresh(true)
    // 设置保存图片名称or名称集合(以“.图片格式”结束，不包含路径) 图片名称数量必须与图片数量对应
    .setName(String.format("JPEG_%s.jpg", PictureUtils.getDataTimeString()))
    .setNames(getNames())
    // 添加保存图片名称or名称集合
    .addName(String.format("JPEG_%s.jpg", PictureUtils.getDataTimeString()))
    .addNames(getNames())
    // 设置图片保存成功或失败监听
    .setSaveImageListener(new SaveImageListener() {
      @Override
      public void onSuccess(String absolutePath) {
        // 下载成功：absolutePath图片保存绝对路径
        // 如果页面有滑动，成功回调是你点击过下载的图片，不一定是当前显示图片
      }
  
      @Override
      public void onFail() { }
    })
    // 跳转
    .start();
```
