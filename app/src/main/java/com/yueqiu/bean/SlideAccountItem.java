package com.yueqiu.bean;


/**
 * Created by wangyun on 14/12/29.
 */
public class SlideAccountItem implements ListItem{
    private int mResId;
    private String mName;
    private int mGolden;
    private String mTitle;
    private String mImg;
    public SlideAccountItem(String img,String name,int golden,String title){
        this.mImg = img;
        this.mName = name;
        this.mGolden = golden;
        this.mTitle = title;
    }
    @Override
    public int getType() {
        return ITEM_ACCOUNT;
    }

    public String getImg() {
        return mImg;

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
    public String getTitle(){
        return mTitle;
    }
    public void setTitle(String title){
        this.mTitle = title;
    }
}
