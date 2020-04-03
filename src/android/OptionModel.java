package com.log2c.cordova.plugin.imagepicker;

public class OptionModel {
    private int imageCount;
    private boolean isCamera;
    private boolean isCrop;
    private int CropW;
    private int CropH;
    private boolean isGif;
    private boolean showCropCircle;
    private boolean showCropFrame;
    private boolean showCropGrid;
    private boolean compress;
    private boolean freeStyleCropEnabled;
    private boolean rotateEnabled;
    private boolean scaleEnabled;
    private int minimumCompressSize;
    private int quality;
    private boolean isWeChatStyle;
    private boolean isRecordSelected;
    private boolean enableBase64;

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public boolean isCamera() {
        return isCamera;
    }

    public void setCamera(boolean camera) {
        isCamera = camera;
    }

    public boolean isCrop() {
        return isCrop;
    }

    public void setCrop(boolean crop) {
        isCrop = crop;
    }

    public int getCropW() {
        return CropW;
    }

    public void setCropW(int cropW) {
        CropW = cropW;
    }

    public int getCropH() {
        return CropH;
    }

    public void setCropH(int cropH) {
        CropH = cropH;
    }

    public boolean isGif() {
        return isGif;
    }

    public void setGif(boolean gif) {
        isGif = gif;
    }

    public boolean isShowCropCircle() {
        return showCropCircle;
    }

    public void setShowCropCircle(boolean showCropCircle) {
        this.showCropCircle = showCropCircle;
    }

    public boolean isShowCropFrame() {
        return showCropFrame;
    }

    public void setShowCropFrame(boolean showCropFrame) {
        this.showCropFrame = showCropFrame;
    }

    public boolean isShowCropGrid() {
        return showCropGrid;
    }

    public void setShowCropGrid(boolean showCropGrid) {
        this.showCropGrid = showCropGrid;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public boolean isFreeStyleCropEnabled() {
        return freeStyleCropEnabled;
    }

    public void setFreeStyleCropEnabled(boolean freeStyleCropEnabled) {
        this.freeStyleCropEnabled = freeStyleCropEnabled;
    }

    public boolean isRotateEnabled() {
        return rotateEnabled;
    }

    public void setRotateEnabled(boolean rotateEnabled) {
        this.rotateEnabled = rotateEnabled;
    }

    public boolean isScaleEnabled() {
        return scaleEnabled;
    }

    public void setScaleEnabled(boolean scaleEnabled) {
        this.scaleEnabled = scaleEnabled;
    }

    public int getMinimumCompressSize() {
        return minimumCompressSize;
    }

    public void setMinimumCompressSize(int minimumCompressSize) {
        this.minimumCompressSize = minimumCompressSize;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public boolean isWeChatStyle() {
        return isWeChatStyle;
    }

    public void setWeChatStyle(boolean weChatStyle) {
        isWeChatStyle = weChatStyle;
    }

    public boolean isRecordSelected() {
        return isRecordSelected;
    }

    public void setRecordSelected(boolean recordSelected) {
        isRecordSelected = recordSelected;
    }

    public boolean isEnableBase64() {
        return enableBase64;
    }

    public void setEnableBase64(boolean enableBase64) {
        this.enableBase64 = enableBase64;
    }

    @Override
    public String toString() {
        return "OptionModel{" +
                "imageCount=" + imageCount +
                ", isCamera=" + isCamera +
                ", isCrop=" + isCrop +
                ", CropW=" + CropW +
                ", CropH=" + CropH +
                ", isGif=" + isGif +
                ", showCropCircle=" + showCropCircle +
                ", showCropFrame=" + showCropFrame +
                ", showCropGrid=" + showCropGrid +
                ", compress=" + compress +
                ", freeStyleCropEnabled=" + freeStyleCropEnabled +
                ", rotateEnabled=" + rotateEnabled +
                ", scaleEnabled=" + scaleEnabled +
                ", minimumCompressSize=" + minimumCompressSize +
                ", quality=" + quality +
                ", isWeChatStyle=" + isWeChatStyle +
                ", isRecordSelected=" + isRecordSelected +
                ", enableBase64=" + enableBase64 +
                '}';
    }
}
