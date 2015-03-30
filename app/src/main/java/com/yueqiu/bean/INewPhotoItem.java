package com.yueqiu.bean;

/**
 * Created by wangyun on 15/3/30.
 */
public interface INewPhotoItem {
    public static final int ADD_IMG = 0;
    public static final int UPLOAD_IMG = 1;
    public static final int SHOW_IMG = 2;
    public int getType();
}
