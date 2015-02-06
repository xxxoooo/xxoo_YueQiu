package com.yueqiu.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by scguo on 15/1/26.
 *
 * 用于创建符合屏幕分辨率的ImageView同时又不会影响图片的长宽比
 *
 */
public class AspectRatioImgView extends ImageView
{
    private static final String TAG = "AspectRatioImgView";

    public AspectRatioImgView(Context context)
    {
        super(context);
    }

    public AspectRatioImgView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public AspectRatioImgView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
        setMeasuredDimension(width, height);
    }
}



































