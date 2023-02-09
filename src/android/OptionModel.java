package com.log2c.cordova.plugin.imagepicker;

import java.util.Objects;

public class OptionModel {
    private int imageCount = 1;
    private boolean isCamera = true;
    private boolean isCrop;
    private boolean compress = true;
    private int minimumCompressSize = 100;
    private int quality = 90;
    private boolean enableBase64 = true;


    public int getImageCount() {
        return imageCount;
    }

    public OptionModel setImageCount(int imageCount) {
        this.imageCount = imageCount;
        return this;
    }

    public boolean isCamera() {
        return isCamera;
    }

    public OptionModel setCamera(boolean camera) {
        isCamera = camera;
        return this;
    }

    public boolean isCrop() {
        return isCrop;
    }

    public OptionModel setCrop(boolean crop) {
        isCrop = crop;
        return this;
    }

    public boolean isCompress() {
        return compress;
    }

    public OptionModel setCompress(boolean compress) {
        this.compress = compress;
        return this;
    }

    public int getMinimumCompressSize() {
        return minimumCompressSize;
    }

    public OptionModel setMinimumCompressSize(int minimumCompressSize) {
        this.minimumCompressSize = minimumCompressSize;
        return this;
    }

    public int getQuality() {
        return quality;
    }

    public OptionModel setQuality(int quality) {
        this.quality = quality;
        return this;
    }

    public boolean isEnableBase64() {
        return enableBase64;
    }

    public OptionModel setEnableBase64(boolean enableBase64) {
        this.enableBase64 = enableBase64;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionModel that = (OptionModel) o;
        return imageCount == that.imageCount && isCamera == that.isCamera && isCrop == that.isCrop && compress == that.compress && minimumCompressSize == that.minimumCompressSize && quality == that.quality && enableBase64 == that.enableBase64;
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageCount, isCamera, isCrop, compress, minimumCompressSize, quality, enableBase64);
    }
}
