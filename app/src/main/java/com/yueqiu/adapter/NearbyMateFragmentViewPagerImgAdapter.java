package com.yueqiu.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.toolbox.NetworkImageView;

import java.awt.font.TextAttribute;

/**
 * Created by scguo on 14/12/20.
 */
public class NearbyMateFragmentViewPagerImgAdapter extends PagerAdapter
{
    private static final String TAG = "NearbyMateFragmentViewPagerImgAdapter";

    private NetworkImageView[] mImgList;

    public NearbyMateFragmentViewPagerImgAdapter(NetworkImageView[] imgList)
    {
        this.mImgList = imgList;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        Log.d(TAG, " the current init item are : " + position);
        if (mImgList.length > 0)
        {

            if(mImgList.length == 1){
                container.addView(mImgList[position]);
                return mImgList[position];
            }else {
                ViewGroup parent = (ViewGroup) mImgList[position % mImgList.length].getParent();
                if (parent != null) {
                    parent.removeView(mImgList[position % mImgList.length]);
                }
                container.addView(mImgList[position % mImgList.length], 0);
                return mImgList[position % mImgList.length];
            }
        }
        return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        Log.d(TAG, " the position of : " + position % mImgList.length + " is destroyed ");
        if (mImgList.length > 2)
        {
            container.removeView(mImgList[position % mImgList.length]);
        }
    }

    @Override
    public int getCount()
    {
        Log.d(TAG, " the count value are : " + mImgList.length);
        if(mImgList.length > 1){
            return Integer.MAX_VALUE;
        }else{
            return mImgList.length;
        }

    }

    @Override
    public boolean isViewFromObject(View view, Object o)
    {
        return view == o;
    }


}




































































