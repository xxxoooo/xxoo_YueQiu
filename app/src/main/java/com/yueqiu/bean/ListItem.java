package com.yueqiu.bean;

/**
 * Created by wangyun on 14/12/29.
 * ListView单元视图
 */
public interface ListItem {
    public static final int ITEM_ACCOUNT = 0;
    public static final int ITEM_BASIC   = 1;
    int getType();
    int getImgId();
    String getName();

}
