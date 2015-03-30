package com.yueqiu.bean;

/**
 * Created by wangyun on 15/3/30.
 */
public class NewPhotoAddImage implements INewPhotoItem{
    private boolean mEnable;
    @Override
    public int getType() {
        return ADD_IMG;
    }

    public boolean isEnable() {
        return mEnable;
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
    }
}
