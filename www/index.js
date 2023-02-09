var exec = require('cordova/exec');

/**
 * 默认参数
 */
const defaultOptions = {
    imageCount: 1,             // 最大选择图片数目，默认1
    isCamera: true,            // 是否允许用户在内部拍照，默认true
    isCrop: false,             // 是否允许裁剪，默认false, imageCount 为 1才生效
    compress: true,            // 是否压缩
    minimumCompressSize: 100,  // 小于100kb的图片不压缩
    quality: 90,               // 压缩质量
    enableBase64: true,       // 是否返回base64编码
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
    ImagePicker: ImagePicker
}
module.exports = ImagePicker;