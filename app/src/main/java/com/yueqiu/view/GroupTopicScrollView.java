package com.yueqiu.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by wangyun on 15/2/3.
 */
public class GroupTopicScrollView extends ScrollView{
    public GroupTopicScrollView(Context context) {
        super(context);
    }

    public GroupTopicScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GroupTopicScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(mOnSizeChangeListener != null){
            mOnSizeChangeListener.onSizeChanged(w,h,oldw,oldh);
        }
    }

    public interface OnSizeChangedListener{
        void onSizeChanged(int w,int h,int oldw,int oldh);
    }

    private OnSizeChangedListener mOnSizeChangeListener;

    public void setOnSizeChangeListener(OnSizeChangedListener listener){
        this.mOnSizeChangeListener = listener;
    }
}
