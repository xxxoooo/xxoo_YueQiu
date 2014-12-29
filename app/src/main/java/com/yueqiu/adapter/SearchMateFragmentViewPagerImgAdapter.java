package com.yueqiu.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by scguo on 14/12/20.
 */
public class SearchMateFragmentViewPagerImgAdapter extends PagerAdapter
{
    private ImageView[] mImgList;

    public SearchMateFragmentViewPagerImgAdapter(ImageView[] imgList)
    {
        this.mImgList = imgList;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        ((ViewPager) container).addView(mImgList[position % mImgList.length], 0);
        return mImgList[position % mImgList.length];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        ((ViewPager) container).removeView(mImgList[position % mImgList.length]);
    }

    @Override
    public int getCount()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object o)
    {
        return view == o;
    }


}




































































