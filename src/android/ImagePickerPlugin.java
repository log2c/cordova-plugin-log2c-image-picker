package com.log2c.cordova.plugin.imagepicker;

import android.app.Activity;
import android.content.Intent;
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
import com.luck.picture.lib.tools.PictureFileUtils;

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
    private static final String TAG = ImagePickerPlugin.class.getSimpleName();
    private Gson mGson;
    private OptionModel mOption;
    private List<LocalMedia> selectList = new ArrayList<>();
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
        PictureFileUtils.deleteAllCacheDirFile(currentActivity);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                new Thread(() -> onGetResult(data)).start();
            } else if (requestCode == PictureConfig.REQUEST_CAMERA) {
                onGetVideoResult(data);
            }
        } else {
            invokeError(resultCode + "");
        }
    }

    private void openCamera() {
        Activity currentActivity = cordova.getActivity();
        PictureSelector.create(currentActivity)
                .openCamera(PictureMimeType.ofImage())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                .enableCrop(mOption.isCrop())// 是否裁剪 true or false
                .compress(mOption.isCompress())// 是否压缩 true or false
                .glideOverride(160, 160)// int glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                .withAspectRatio(mOption.getCropW(), mOption.getCropH())// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                .hideBottomControls(mOption.isCrop())// 是否显示uCrop工具栏，默认不显示 true or false
                .freeStyleCropEnabled(mOption.isFreeStyleCropEnabled())// 裁剪框是否可拖拽 true or false
                .circleDimmedLayer(mOption.isShowCropCircle())// 是否圆形裁剪 true or false
                .showCropFrame(mOption.isShowCropFrame())// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                .showCropGrid(mOption.isShowCropGrid())// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                .openClickSound(false)// 是否开启点击声音 true or false
                .cropCompressQuality(mOption.getQuality())// 裁剪压缩质量 默认90 int
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
            modeValue = 1;
        } else {
            modeValue = 2;
        }

        PictureSelector.create(currentActivity)
                .openGallery(PictureMimeType.ofImage())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .loadImageEngine(GlideEngine.createGlideEngine())
                .maxSelectNum(mOption.getImageCount())// 最大图片选择数量 int
                .minSelectNum(0)// 最小选择数量 int
                .imageSpanCount(4)// 每行显示个数 int
                .selectionMode(modeValue)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                .previewImage(true)// 是否可预览图片 true or false
                .previewVideo(false)// 是否可预览视频 true or false
                .enablePreviewAudio(false) // 是否可播放音频 true or false
                .isCamera(mOption.isCamera())// 是否显示拍照按钮 true or false
                .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                .enableCrop(mOption.isCrop())// 是否裁剪 true or false
                .compress(mOption.isCompress())// 是否压缩 true or false
                .glideOverride(160, 160)// int glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                .withAspectRatio(mOption.getCropW(), mOption.getCropH())// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                .hideBottomControls(mOption.isCrop())// 是否显示uCrop工具栏，默认不显示 true or false
                .isGif(mOption.isGif())// 是否显示gif图片 true or false
                .freeStyleCropEnabled(mOption.isFreeStyleCropEnabled())// 裁剪框是否可拖拽 true or false
                .circleDimmedLayer(mOption.isShowCropCircle())// 是否圆形裁剪 true or false
                .showCropFrame(mOption.isShowCropFrame())// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                .showCropGrid(mOption.isShowCropGrid())// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                .openClickSound(false)// 是否开启点击声音 true or false
                .cropCompressQuality(mOption.getQuality())// 裁剪压缩质量 默认90 int
                .minimumCompressSize(mOption.getMinimumCompressSize())// 小于100kb的图片不压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                .rotateEnabled(mOption.isRotateEnabled()) // 裁剪是否可旋转图片 true or false
                .scaleEnabled(mOption.isScaleEnabled())// 裁剪是否可放大缩小图片 true or false
                .selectionMedia(selectList) // 当前已选中的图片 List
                .isWeChatStyle(mOption.isWeChatStyle())
                .forResult(PictureConfig.CHOOSE_REQUEST); //结果回调onActivityResult code
    }


    private void onGetVideoResult(Intent data) {
        List<LocalMedia> mVideoSelectList = PictureSelector.obtainMultipleResult(data);
        boolean isRecordSelected = mOption.isRotateEnabled();
        if (!mVideoSelectList.isEmpty() && isRecordSelected) {
            selectList = mVideoSelectList;
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
            videoMap.addProperty("uri", "file://" + media.getPath());
            videoMap.addProperty("type", "video");
            videoList.add(videoMap);
        }

        invokeSuccessWithResult(videoList);
    }

    private void onGetResult(Intent data) {
        List<LocalMedia> tmpSelectList = PictureSelector.obtainMultipleResult(data);
        boolean isRecordSelected = mOption.isRecordSelected();
        if (!tmpSelectList.isEmpty() && isRecordSelected) {
            selectList = tmpSelectList;
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
        String path = media.getPath();

        if (media.isCompressed() || media.isCut()) {
            path = media.getCompressPath();
        }

        if (media.isCut()) {
            path = media.getCutPath();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        imageMap.addProperty("width", options.outWidth);
        imageMap.addProperty("height", options.outHeight);
        imageMap.addProperty("type", "image");
        imageMap.addProperty("uri", "file://" + path);
        imageMap.addProperty("original_uri", "file://" + media.getPath());
        imageMap.addProperty("size", (int) new File(path).length());

        if (enableBase64) {
            String encodeString = getBase64StringFromFile(path);
            imageMap.addProperty("base64", encodeString);
        }

        return imageMap;
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
            inputStream = new FileInputStream(new File(absoluteFilePath));
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