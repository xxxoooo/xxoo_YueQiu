package com.yueqiu.bean;

/**
 * Created by wangyun on 15/3/30.
 */
public class NewPhotoUpload implements INewPhotoItem{

    private BitmapBean mBitmapBean;

    private boolean mUploaded;

    private String mImgUrl;

    @Override
    public int getType() {
        return UPLOAD_IMG;
    }

    public BitmapBean getBitmapBean() {
        return mBitmapBean;
    }

    public void setBitmapBean(BitmapBean bitmapBean) {
        this.mBitmapBean = bitmapBean;
    }

    public boolean isUploaded() {
        return mUploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.mUploaded = uploaded;
    }

    public String getImgUrl() {
        return mImgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.mImgUrl = imgUrl;
    }
}
