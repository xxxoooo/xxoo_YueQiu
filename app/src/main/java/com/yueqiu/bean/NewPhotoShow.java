package com.yueqiu.bean;

/**
 * Created by wangyun on 15/3/30.
 */
public class NewPhotoShow implements INewPhotoItem{
    private String mImgUrl;
    @Override
    public int getType() {
        return SHOW_IMG;
    }

    public String getImgUrl() {
        return mImgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.mImgUrl = imgUrl;
    }
}
