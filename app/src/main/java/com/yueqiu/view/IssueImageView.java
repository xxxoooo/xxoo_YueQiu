package com.yueqiu.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.yueqiu.bean.BitmapBean;

/**
 * Created by wangyun on 15/2/9.
 */
public class IssueImageView extends ImageView{
    private BitmapBean mBitmapBean;
    public IssueImageView(Context context) {
        super(context);
    }

    public IssueImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IssueImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BitmapBean getBitmapBean() {
        return mBitmapBean;
    }

    public void setBitmapBean(BitmapBean mBitmapBean) {
        this.mBitmapBean = mBitmapBean;
    }

}
