package com.log2c.cordova.plugin.imagepicker;

import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.callback.SelectCallback;
import com.huantansheng.easyphotos.models.album.entity.Photo;

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

public class ImagePickerPlugin extends CordovaPlugin {
    @SuppressWarnings("unused")
    private static final String TAG = ImagePickerPlugin.class.getSimpleName();
    private Gson mGson;
    private OptionModel mOption;
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
                cordova.getActivity().runOnUiThread(this::showImagePicker);
                break;
            case "openCamera":
                cordova.getActivity().runOnUiThread(this::openCamera);
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
        // TODO 删除缓存
    }

    private void openCamera() {
        AppCompatActivity currentActivity = cordova.getActivity();
        cordova.setActivityResultCallback(this);
        EasyPhotos.createCamera(currentActivity)
                .isCompress(mOption.isCompress())
                .isCrop(mOption.isCrop())
                .setCompressEngine(LubanCompressEngine.getInstance(mOption.getMinimumCompressSize()))
                .setCompressionQuality(mOption.getQuality())
                .start(new SelectCallback() {
                    @Override
                    public void onResult(ArrayList<Photo> photos, ArrayList<String> paths, boolean isOriginal) {
                        onGetImageResult(photos, paths, isOriginal);
                    }
                });
    }


    private void showImagePicker() {
        FragmentActivity currentActivity = cordova.getActivity();
        cordova.setActivityResultCallback(this);

        EasyPhotos.createAlbum(currentActivity, mOption.isCamera(), GlideEngine.getInstance())
                .enableSingleCheckedBack(mOption.getImageCount() == 1)
                .setCount(mOption.getImageCount())
                .isCompress(mOption.isCompress())
                .isCrop(mOption.isCrop())
                .setCompressEngine(LubanCompressEngine.getInstance(mOption.getMinimumCompressSize()))
                .setCompressionQuality(mOption.getQuality())
                .start(new SelectCallback() {
                    @Override
                    public void onResult(ArrayList<Photo> photos, ArrayList<String> paths, boolean isOriginal) {
                        onGetImageResult(photos, paths, isOriginal);
                    }
                });
    }


    private void onGetImageResult(ArrayList<Photo> photos, ArrayList<String> paths, boolean isOriginal) {
        new Thread(() -> {
            boolean enableBase64 = mOption.isEnableBase64();
            JsonArray imageList = new JsonArray();
            for (Photo photo : photos) {
                imageList.add(getImageResult(photo, enableBase64));
            }
            invokeSuccessWithResult(imageList);
        }).start();
    }

    private JsonObject getImageResult(Photo photo, Boolean enableBase64) {
        JsonObject imageMap = new JsonObject();
        String path = photo.getAvailablePath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        imageMap.addProperty("width", options.outWidth);
        imageMap.addProperty("height", options.outHeight);
        imageMap.addProperty("type", "image");
        imageMap.addProperty("path", path);
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