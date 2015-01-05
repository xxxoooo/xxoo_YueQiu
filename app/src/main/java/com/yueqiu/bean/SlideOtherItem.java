package com.yueqiu.bean;

/**
 * Created by wangyun on 14/12/29.
 */
public class SlideOtherItem implements ListItem{
    private String mName;
    private boolean mHasMsg;
    private int mResId;
    public SlideOtherItem(int res,String name,boolean hasMsg){
        this.mName = name;
        this.mHasMsg = hasMsg;
        this.mResId = res;
    }
    @Override
    public int getType() {
        return ITEM_BASIC;
    }

    public int getImgId() {
        return mResId;
    }

    @Override
    public String getName() {
        return mName;
    }
    public boolean hasMsg(){
        return mHasMsg;
    }
    public void setHasMsg(boolean hasMsg){
        this.mHasMsg = hasMsg;
    }
}
