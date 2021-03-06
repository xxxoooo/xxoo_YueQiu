package com.yueqiu.bean;


/**
 * Created by wangyun on 14/12/29.
 */
public class SlideAccountItemISlide implements ISlideListItem {
    private String mName;
    private int mGolden;
    private String mTitle;
    private String mImg;
    private int mUserId;
    private String mNickName;
    public SlideAccountItemISlide(String img, String name, int golden, String title, int userId,String nickName){
        this.mImg = img;
        this.mName = name;
        this.mGolden = golden;
        this.mTitle = title;
        this.mUserId = userId;
        this.mNickName = nickName;
    }
    @Override
    public int getType() {
        return ITEM_ACCOUNT;
    }

    public String getImg() {
        return mImg;

    }
    public void setImg(String img){
        this.mImg = img;
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

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int userId){
        this.mUserId = userId;
    }

    public String getmNickName() {
        return mNickName;
    }

    public void setmNickName(String mNickName) {
        this.mNickName = mNickName;
    }
}
