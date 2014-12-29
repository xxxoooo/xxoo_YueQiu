package com.yueqiu.bean;

/**
 * Created by wangyun on 14/12/29.
 */
public class SlideAccountItem implements ListItem{
    private int mResId;
    private String mName;
    private int mGolden;
    public SlideAccountItem(int res,String name,int golden){
        this.mResId = res;
        this.mName = name;
        this.mGolden = golden;
    }
    @Override
    public int getType() {
        return ITEM_ACCOUNT;
    }

    @Override
    public int getImgId() {
        return mResId;
    }

    @Override
    public String getName() {
        return mName;
    }
    public int getGolden(){
        return mGolden;
    }
    public void setName(String name){
        this.mName = name;
    }
}
