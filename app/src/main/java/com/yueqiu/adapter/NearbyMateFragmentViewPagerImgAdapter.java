package com.yueqiu.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.awt.font.TextAttribute;

/**
 * Created by scguo on 14/12/20.
 */
public class NearbyMateFragmentViewPagerImgAdapter extends PagerAdapter
{
    private static final String TAG = "NearbyMateFragmentViewPagerImgAdapter";

    private ImageView[] mImgList;

    public NearbyMateFragmentViewPagerImgAdapter(ImageView[] imgList)
    {
        this.mImgList = imgList;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        // TODO: 以下的逻辑有一些问题，需要进一步确定
        // TODO: 因为我现在还不确定这里是否真的是需要removeView()的操作
        if (mImgList.length > 0)
        {
            container.removeView(mImgList[position % mImgList.length]);
        }

        Log.d(TAG, " the current init item are : " + position);
        if (mImgList.length > 0)
        {
            ((ViewPager) container).addView(mImgList[position % mImgList.length], 0);
            return mImgList[position % mImgList.length];
        }
        return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        Log.d(TAG, " the position of : " + position + " is destroyed ");
        if (mImgList.length > 0)
        {
            ((ViewPager) container).removeView(mImgList[position % mImgList.length]);
        }
    }

    @Override
    public int getCount()
    {
        Log.d(TAG, " the count value are : " + mImgList.length);
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object o)
    {
        return view == o;
    }


}




































































