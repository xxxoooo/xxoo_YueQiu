package com.yueqiu.adapter;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by scguo on 15/3/7.
 */
public class SplashScreenViewPager extends ViewPager
{
    private static final String TAG = "SplashScreenViewPager";
    private OnSwipeOutLisener mSwipeOutLisener;
    private float mStartDragX;
    public void setOnSwipeOutLisener(OnSwipeOutLisener listener)
    {
        this.mSwipeOutLisener = listener;
        Log.d(TAG, " inside the SplashViewpager set the listener ");
        this.mStartDragX = 0;
    }

    public SplashScreenViewPager(Context context)
    {
        this(context, null);
    }
    // 以下的这个方法一定要重写，因为Android中的View系统需要通过读取AttributeSet来进行属性配置加载
    // 如果不重写这个方法的话就会爆出类似于inflate exception之类的错误，一定要注意(如果我们经常开发一些
    // Custom View的话，对这个的体验会更深刻一些)
    public SplashScreenViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        float x = ev.getX();
        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mStartDragX = x;
            case MotionEvent.ACTION_MOVE:
                if (mStartDragX > x && getCurrentItem() == getAdapter().getCount() - 1)
                {
                    Log.d(TAG, " the user has move to the right most : " + (mSwipeOutLisener == null));
                    if (mSwipeOutLisener != null)
                    {
                        mSwipeOutLisener.onSwipeRightMost();
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    // 我们用于回调启动主程序的接口
    public interface OnSwipeOutLisener
    {
        /**
         * YueQiuSplashScreen需要实现这个方法用于回调
         */
        public void onSwipeRightMost();
    }

}
