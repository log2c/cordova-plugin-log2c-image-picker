package com.log2c.cordova.plugin.imagepicker;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.huantansheng.easyphotos.engine.ImageEngine;

/**
 * Glide4.x的加载图片引擎实现,单例模式
 * Glide4.x的缓存机制更加智能，已经达到无需配置的境界。如果使用Glide3.x，需要考虑缓存机制。
 * Created by huan on 2018/1/15.
 */
public class GlideEngine implements ImageEngine {

    //单例模式，私有构造方法
    private GlideEngine() {
    }

    private static final class InstanceHolder {
        //单例
        static final GlideEngine instance = new GlideEngine();
    }

    //获取单例
    public static GlideEngine getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    public void loadPhoto(Context context, String path, ImageView imageView) {
        Glide.with(context).load(path).transition(withCrossFade()).into(imageView);
    }

    @Override
    public void loadGifAsBitmap(Context context, String path, ImageView imageView) {
        Glide.with(context).asBitmap().load(path).into(imageView);
    }

    @Override
    public void loadGif(Context context, String path, ImageView imageView) {
        Glide.with(context).asGif().load(path).transition(withCrossFade()).into(imageView);
    }

    @Override
    public Bitmap getCacheBitmap(Context context, String path, int width, int height) throws Exception {
        return Glide.with(context).asBitmap().load(path).submit(width, height).get();
    }

}