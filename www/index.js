var exec = require('cordova/exec');

/**
 * 默认参数
 */
const defaultOptions = {
    allowPickingImage: true,
    imageCount: 6,             // 最大选择图片数目，默认6
    isRecordSelected: false,   // 是否已选图片(iOS Only, Android use selectionData
    selectionData: [], // 已选中图片、视频的 uri
    isCamera: true,            // 是否允许用户在内部拍照，默认true
    isCrop: false,             // 是否允许裁剪，默认false, imageCount 为1才生效
    CropW: ~~(window.innerWidth * 0.6),    // 裁剪宽度，默认屏幕宽度60%
    CropH: ~~(window.innerHeight * 0.6),    // 裁剪高度，默认屏幕宽度60%
    isGif: false,              // 是否允许选择GIF，默认false，暂无回调GIF数据
    showCropCircle: false,     // 是否显示圆形裁剪区域，默认false
    circleCropRadius: ~~(window.innerWidth / 4), // 圆形裁剪半径，默认屏幕宽度一半
    showCropFrame: true,       // 是否显示裁剪区域，默认true
    showCropGrid: false,       // 是否隐藏裁剪区域网格，默认false
    freeStyleCropEnabled: false, // 裁剪框是否可拖拽
    rotateEnabled: true,       // 裁剪是否可旋转图片
    scaleEnabled: true,        // 裁剪是否可放大缩小图片
    compress: true,
    minimumCompressSize: 100,  // 小于100kb的图片不压缩
    quality: 90,               // 压缩质量
    enableBase64: false,       // 是否返回base64编码，默认不返回
    allowPickingOriginalPhoto: false,
    allowPickingMultipleVideo: false, // 可以多选视频/gif/图片，和照片共享最大可选张数maxImagesCount的限制
    videoMaximumDuration: 10 * 60, // 视频最大拍摄时间，默认是10分钟，单位是秒
    isWeChatStyle: false,      // 是否是微信风格选择界面 Android Only
    sortAscendingByModificationDate: true // 对照片排序，按修改时间升序，默认是YES。如果设置为NO,最新的照片会显示在最前面，内部的拍照按钮会排在第一个
};

var ImagePicker = {

    errorCallback: function (msg) {
        console.log('ImagePicker Callback Error: ' + msg)
    },

    callNative: function (name, args, successCallback, errorCallback) {
        if (errorCallback) {
            cordova.exec(successCallback, errorCallback, 'ImagePicker', name, args)
        } else {
            cordova.exec(successCallback, this.errorCallback, 'ImagePicker', name, args)
        }
    },
    showImagePicker: function (options, successCallback, errorCallback) {
        this.callNative('showImagePicker', [{
            ...defaultOptions,
            ...options
        }], successCallback, errorCallback);
    },
    openCamera: function (options, successCallback, errorCallback) {
        this.callNative('openCamera', [{
            ...defaultOptions,
            ...options
        }], successCallback, errorCallback);
    },
    deleteCache: function (successCallback, errorCallback) {
        this.callNative('deleteCache', [], successCallback, errorCallback);
    },
    ImagePicker: ImagePicker
}
module.exports = ImagePicker;