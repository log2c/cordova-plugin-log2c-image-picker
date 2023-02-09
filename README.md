# cordova-plugin-log2c-image-picker

图片选择 `Plugin`

* Android： [EasyPhotos](https://github.com/joker-fu/EasyPhotos)
* iOS：[TZImagePickerController 3.0.9](https://github.com/banchichen/TZImagePickerController)

## Install

```
cordova plugin add cordova-plugin-log2c-image-picker
```

## Usage

const options = {
    imageCount: 1,             // 最大选择图片数目，默认1
    isCamera: true,            // 是否允许用户在内部拍照，默认true
    isCrop: false,             // 是否允许裁剪，默认false, imageCount 为 1才生效
    compress: true,            // 是否压缩
    minimumCompressSize: 100,  // 小于100kb的图片不压缩
    quality: 90,               // 压缩质量
    enableBase64: true,       // 是否返回base64编码
};


// 直接调用拍照
ImagePicker.openCamera(options,successCallback,errorCallback);

// 调用图片选择
ImagePicker.showImagePicker(options,successCallback,errorCallback);
