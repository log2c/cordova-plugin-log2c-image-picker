package com.log2c.cordova.plugin.imagepicker;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.huantansheng.easyphotos.callback.CompressCallback;
import com.huantansheng.easyphotos.engine.CompressEngine;
import com.huantansheng.easyphotos.models.album.entity.Photo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import top.zibin.luban.Luban;

public class LubanCompressEngine implements CompressEngine {
    private int minimumCompressSize;

    //单例模式，私有构造方法
    private LubanCompressEngine(int minimumCompressSize) {
        this.minimumCompressSize = minimumCompressSize;
    }

    //获取单例
    public static LubanCompressEngine getInstance(int minimumCompressSize) {
        return new LubanCompressEngine(minimumCompressSize);
    }

    private String getPath(Context context) {
        String path;
        //Android 10 存在在应用内
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            path = Environment.getExternalStorageDirectory() + "/Luban/image/";
        } else {
            path = context.getFilesDir() + "/Luban/image/";
        }
        File file = new File(path);
        if (file.mkdirs()) {
            return path;
        }
        return path;
    }

    @Override
    public void compress(final Context context, final ArrayList<Photo> photos, final CompressCallback callback) {
        callback.onStart();
        new Thread(() -> {
            try {
                ArrayList<String> paths = new ArrayList<>();
                for (Photo photo : photos) {
                    if (photo.selectedOriginal) continue;
                    if (!TextUtils.isEmpty(photo.cropPath)) {
                        paths.add(photo.cropPath);
                    } else {
                        paths.add(photo.filePath);
                    }
                }
                if (paths.isEmpty()) {
                    callback.onSuccess(photos);
                    return;
                }

                List<File> files = Luban.with(context).load(paths)
                        .ignoreBy(minimumCompressSize)
                        .setTargetDir(getPath(context))
                        .filter(path -> !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif") || path.toLowerCase().endsWith(".mp4"))).get();
                for (int i = 0, j = photos.size(); i < j; i++) {
                    Photo photo = photos.get(i);
                    photo.compressPath = files.get(i).getPath();
                }
                callback.onSuccess(photos);
            } catch (IOException e) {
                e.printStackTrace();
                callback.onFailed(photos, e.getMessage());
            }
        }).start();
    }
}