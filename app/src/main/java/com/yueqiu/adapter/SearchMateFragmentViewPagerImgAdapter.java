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
public class SearchMateFragmentViewPagerImgAdapter extends PagerAdapter
{
    private static final String TAG = "SearchMateFragmentViewPagerImgAdapter";

    private ImageView[] mImgList;

    public SearchMateFragmentViewPagerImgAdapter(ImageView[] imgList)
    {
        this.mImgList = imgList;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        Log.d(TAG, " the current init item are : " + position);
        ((ViewPager) container).addView(mImgList[position % mImgList.length], 0);
        return mImgList[position % mImgList.length];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        Log.d(TAG, " the position of : " + position + " is destroyed ");
        ((ViewPager) container).removeView(mImgList[position % mImgList.length]);
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




































































