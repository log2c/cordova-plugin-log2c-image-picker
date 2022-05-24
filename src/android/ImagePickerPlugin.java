package com.log2c.cordova.plugin.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.manager.PictureCacheManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImagePickerPlugin extends CordovaPlugin {
    @SuppressWarnings("unused")
    private static final String TAG = ImagePickerPlugin.class.getSimpleName();
    private static final String FILE_PROTOCOL_PREFIX = "file://";
    private Gson mGson;
    private OptionModel mOption;
    private final List<LocalMedia> selectList = new ArrayList<>();
    private CallbackContext mCallbackContext;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        mGson = new Gson();
        cordova.setActivityResultCallback(this);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        mCallbackContext = callbackContext;
        if (TextUtils.isEmpty(action)) {
            return false;
        }
        String json;
        try {
            JSONObject jsonObject = args.getJSONObject(0);
            json = jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (TextUtils.isEmpty(json)) {
            return false;
        }
        mOption = mGson.fromJson(json, OptionModel.class);

        switch (action) {
            case "showImagePicker":
                showImagePicker();
                break;
            case "openCamera":
                openCamera();
                break;
            case "deleteCache":
                deleteCache();
                break;
            default:
                return false;
        }
        return true;
    }

    private void deleteCache() {
        Activity currentActivity = cordova.getActivity();
        PictureCacheManager.deleteAllCacheDirFile(currentActivity);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                new Thread(() -> onGetImageResult(data)).start();
            } else if (requestCode == PictureConfig.REQUEST_CAMERA) {
                onGetVideoResult(data);
            }
        }
    }

    private void openCamera() {
        Activity currentActivity = cordova.getActivity();
        cordova.setActivityResultCallback(this);
        PictureSelector.create(currentActivity)
                .openCamera(PictureMimeType.ofImage())
                .imageEngine(GlideEngine.createGlideEngine())
                .setCameraImageFormat(PictureMimeType.JPEG)
                .cutCompressFormat(String.valueOf(Bitmap.CompressFormat.JPEG))
                .isEnableCrop(mOption.isCrop())// 是否裁剪 true or false
                .isCompress(mOption.isCompress())// 是否压缩 true or false
                .withAspectRatio(mOption.getCropW(), mOption.getCropH())// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                .hideBottomControls(mOption.isCrop())// 是否显示uCrop工具栏，默认不显示 true or false
                .circleDimmedLayer(mOption.isShowCropCircle())// 是否圆形裁剪 true or false
                .showCropFrame(mOption.isShowCropFrame())// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                .showCropGrid(mOption.isShowCropGrid())// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                .isOpenClickSound(false)// 是否开启点击声音 true or false
                .cutOutQuality(mOption.getQuality())// 裁剪压缩质量 默认90 int
                .minimumCompressSize(mOption.getMinimumCompressSize())// 小于100kb的图片不压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                .rotateEnabled(mOption.isRotateEnabled()) // 裁剪是否可旋转图片 true or false
                .scaleEnabled(mOption.isScaleEnabled())// 裁剪是否可放大缩小图片 true or false
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    private void showImagePicker() {
        Activity currentActivity = cordova.getActivity();
        int modeValue;
        if (mOption.getImageCount() == 1) {
            modeValue = PictureConfig.SINGLE;
        } else {
            modeValue = PictureConfig.MULTIPLE;
        }
        cordova.setActivityResultCallback(this);

        final List<LocalMedia> selectionData = new ArrayList<>();
        if (mOption.getSelectionData() != null && mOption.getSelectionData().size() > 0) {
            for (LocalMedia media : selectList) {
                int index = -1;
                if (!TextUtils.isEmpty(media.getPath())) {
                    index = mOption.getSelectionData().indexOf(FILE_PROTOCOL_PREFIX + media.getPath());
                }
                if (index == -1 && !TextUtils.isEmpty(media.getCompressPath())) {
                    index = mOption.getSelectionData().indexOf(FILE_PROTOCOL_PREFIX + media.getCompressPath());
                }
                if (index == -1 && !TextUtils.isEmpty(media.getCutPath())) {
                    index = mOption.getSelectionData().indexOf(FILE_PROTOCOL_PREFIX + media.getCutPath());
                }
                if (index != -1) {
                    selectionData.add(media);
                }
            }
        }

        PictureSelector.create(currentActivity)
                .openGallery(PictureMimeType.ofImage())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .imageEngine(GlideEngine.createGlideEngine())
                .maxSelectNum(mOption.getImageCount())// 最大图片选择数量 int
                .minSelectNum(0)// 最小选择数量 int
                .imageSpanCount(4)// 每行显示个数 int
                .selectionMode(modeValue)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                .isPreviewImage(true)// 是否可预览图片 true or false
                .isPreviewVideo(false)// 是否可预览视频 true or false
                .isEnablePreviewAudio(false) // 是否可播放音频 true or false
                .isCamera(mOption.isCamera())// 是否显示拍照按钮 true or false
                .setCameraImageFormat(PictureMimeType.JPEG)// 拍照保存图片格式后缀,默认jpeg
                .cutCompressFormat(String.valueOf(Bitmap.CompressFormat.JPEG))// 拍照保存图片格式后缀,默认jpeg
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .isEnableCrop(mOption.isCrop())// 是否裁剪 true or false
                .isCompress(mOption.isCompress())// 是否压缩 true or false
                .withAspectRatio(mOption.getCropW(), mOption.getCropH())// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                .hideBottomControls(mOption.isCrop())// 是否显示uCrop工具栏，默认不显示 true or false
                .isGif(mOption.isGif())// 是否显示gif图片 true or false
                .circleDimmedLayer(mOption.isShowCropCircle())// 是否圆形裁剪 true or false
                .showCropFrame(mOption.isShowCropFrame())// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                .showCropGrid(mOption.isShowCropGrid())// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                .isOpenClickSound(false)// 是否开启点击声音 true or false
                .cutOutQuality(mOption.getQuality())// 裁剪压缩质量 默认90 int
                .minimumCompressSize(mOption.getMinimumCompressSize())// 小于100kb的图片不压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                .rotateEnabled(mOption.isRotateEnabled()) // 裁剪是否可旋转图片 true or false
                .scaleEnabled(mOption.isScaleEnabled())// 裁剪是否可放大缩小图片 true or false
                .selectionData(selectionData) // 当前已选中的图片 List
                .isWeChatStyle(mOption.isWeChatStyle())
                .forResult(PictureConfig.CHOOSE_REQUEST); //结果回调onActivityResult code
        cordova.setActivityResultCallback(this);
    }


    private void onGetVideoResult(Intent data) {
        List<LocalMedia> mVideoSelectList = PictureSelector.obtainMultipleResult(data);
        selectList.clear();
        if (!mVideoSelectList.isEmpty()) {
            selectList.addAll(mVideoSelectList);
        }
        JsonArray videoList = new JsonArray();
        for (LocalMedia media : mVideoSelectList) {
            if (TextUtils.isEmpty(media.getPath())) {
                continue;
            }
            JsonObject videoMap = new JsonObject();
            videoMap.addProperty("size", new File(media.getPath()).length() + "");
            videoMap.addProperty("duration", media.getDuration() + "");
            videoMap.addProperty("fileName", new File(media.getPath()).getName());
            videoMap.addProperty("uri", FILE_PROTOCOL_PREFIX + media.getPath());
            videoMap.addProperty("type", "video");
            videoMap.addProperty("originalPath", media.getOriginalPath());
            videoList.add(videoMap);
        }

        invokeSuccessWithResult(videoList);
    }

    private void onGetImageResult(Intent data) {
        List<LocalMedia> tmpSelectList = PictureSelector.obtainMultipleResult(data);
        selectList.clear();
        if (!tmpSelectList.isEmpty()) {
            selectList.addAll(tmpSelectList);
        }

        JsonArray imageList = new JsonArray();
        boolean enableBase64 = mOption.isEnableBase64();

        for (LocalMedia media : tmpSelectList) {
            imageList.add(getImageResult(media, enableBase64));
        }
        invokeSuccessWithResult(imageList);
    }

    private JsonObject getImageResult(LocalMedia media, Boolean enableBase64) {
        JsonObject imageMap = new JsonObject();
        String path = getImagePathByMedia(media);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        imageMap.addProperty("width", options.outWidth);
        imageMap.addProperty("height", options.outHeight);
        imageMap.addProperty("type", "image");
        imageMap.addProperty("uri", FILE_PROTOCOL_PREFIX + path);
        imageMap.addProperty("original_uri", FILE_PROTOCOL_PREFIX + media.getPath());
        imageMap.addProperty("size", (int) new File(path).length());

        if (enableBase64) {
            String encodeString = getBase64StringFromFile(path);
            imageMap.addProperty("base64", encodeString);
        }

        return imageMap;
    }

    private String getImagePathByMedia(LocalMedia media) {
        String path = media.getPath();

        if (media.isCompressed() || media.isCut()) {
            path = media.getCompressPath();
        }
        if (media.isCut()) {
            path = media.getCutPath();
        }
        return path;
    }

    /**
     * 获取图片base64编码字符串
     *
     * @param absoluteFilePath 文件路径
     * @return base64字符串
     */
    private String getBase64StringFromFile(String absoluteFilePath) {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(absoluteFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bytes = output.toByteArray();
        return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    /**
     * 选择照片成功时触发
     *
     * @param imageList 图片数组
     */
    private void invokeSuccessWithResult(JsonArray imageList) {
        if (mCallbackContext == null) {
            return;
        }
        try {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, new JSONArray(imageList.toString()));
            mCallbackContext.sendPluginResult(pluginResult);
        } catch (JSONException e) {
            e.printStackTrace();
            invokeError(e.getLocalizedMessage());
        }
    }

    /**
     * 取消选择时触发
     */
    private void invokeError(String error) {
        if (mCallbackContext == null) {
            return;
        }
        PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, error);
        mCallbackContext.sendPluginResult(pluginResult);
    }
}