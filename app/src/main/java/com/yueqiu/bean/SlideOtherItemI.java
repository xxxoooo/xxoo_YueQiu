package com.yueqiu.bean;

/**
 * Created by wangyun on 14/12/29.
 */
public class SlideOtherItemI implements IListItem {
    private String mName;
    private boolean mHasMsg;
    private int mResId;
    public SlideOtherItemI(int res, String name, boolean hasMsg){
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
