## 图片浏览
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
    // 是否可以保存图片(默认false不可以保存)
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

## 媒体选择
```
  PicturePicker.from(this)
    // 选择类型
    // PicturePickerMediaType.ofImage() 图片
    // PicturePickerMediaType.ofVideo() 视频
    // PicturePickerMediaType.ofAll() 视频
    .choose(PicturePickerMediaType.ofAll())
    // 更改主题
    .theme(R.style.xxx)
    // 预览图片时点击图片隐藏其他控件(默认false不隐藏)
    .autoHideToolbarOnSingleTap(true)
    // 设置每个图片显示大小(px)
    .gridExpectedSize(300)
    // 图片加载器(默认GlideEngine)
    .imageEngine(new GlideEngine())
    // 选择框是否显示数字
    .countable(true)
    // 最大选择数量(默认为1)
    .maxSelectable(4)
    // 第一个参数为图片最大选择数量，第二个参数为视频最大选择数量
    .maxSelectablePerMediaType(1,1)
    // 是否可以拍照(默认false不可以)
    .capture(true)
    // 是否只选择指定格式媒体文件(默认false显示全部)
    .showSingleMediaType(true)
    // 设置拍照图片保存路径(以“/”结束，不包含图片名称)
    .saveImagePath(PictureData.SAVE_IMGE_PATH)
    // 图片过滤器(提供GifSizeFilter)
    .addFilter(new GifSizeFilter(200, 200, Integer.MAX_VALUE))
    // 图片不能大于多少M
    .maxOriginalSize(5)
    // 模糊图片(取值0-1，默认0.5)
    .thumbnailScale(0.3f)
    // 是否显示原图选项(默认false不显示)
    .originalEnable(true)
   // 原图按钮选择回调
    .setOnCheckedListener(new OnCheckedListener() {
      @Override
      public void onCheck(boolean isChecked) { } })
    // 设置已经选中图片
    .selectorList(List<Item>)
    // 设置每行列数(如果已经设置gridExpectedSize，此设置无效)
    .spanCount(4)
    // 图片选择
    .setOnSelectedListener(new OnSelectedListener() {
      @Override
      public void onSelected(@NonNull List<Uri> uriList, @NonNull List<String> pathList) { } })
    // 是否需要裁剪(默认false不需要)
    .crop(true)
    // 是否需要圆形裁剪(默认false不需要)
    .cropIsOval(true)
    // 裁剪的比例(默认1：1)
    .cropWidthAndrHeight(1, 1)
    // 图片裁剪后保存格式(默认Bitmap.CompressFormat.JPEG)
    .cropCompressFormat(Bitmap.CompressFormat.JPEG)
    // 图片裁剪后保存的清晰度(取值0-100，默认80)
    .cropQuality(80)
    // 指定裁剪图片Uri(选择图片裁剪不需要设置此项)
    .cropUri(uri)
    // 设置裁剪图片保存名称(以“.图片格式”结束，不包含图片路径)
    .cropSaveName("temp.jpg")
    // 裁剪图片后保存成功是否需要刷新媒体库(默认true刷新)
    .isRefresh(true)
    // 跳转
    .forResult(REQUEST_PICTURE_PICKER);
```
