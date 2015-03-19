package com.yueqiu.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.yueqiu.R;
import com.yueqiu.YueQiuSplashScreen;

/**
 * Created by scguo on 15/3/7.
 */
public class SplashScreenPagerAdapter extends FragmentPagerAdapter
{
    private static final String TAG = "SplashScreenPagerAdapter";

    private static final int PAGE_COUNT = 3;
    private static Context sContext;
    public SplashScreenPagerAdapter(Context context, FragmentManager fm)
    {
        super(fm);
        this.sContext = context;
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position)
    {
        SplashScreenPageFragment fragment = new SplashScreenPageFragment();
        Bundle data = new Bundle();
        Log.d(TAG, " the current position of the fragemnt are : " + position);
        data.putParcelable(YueQiuSplashScreen.KEY_CUREENT_SCREEN_BITMAP, getBitmap(position));
        fragment.setArguments(data);
        return fragment;
    }

    private static Bitmap getBitmap(final int pos)
    {
        final int resId;
        switch (pos)
        {
            case 0:
                resId = R.drawable.splash_launch_page_1;
                break;
            case 1:
                resId = R.drawable.splash_launch_page_2;
                break;
            case 2:
                resId = R.drawable.splash_launch_page_3;
                break;
            default:
                // TODO: 所实话，我也不知道在默认情况下应该选择哪张图片作为默认图片
                // TODO: 因为理论上我们的可选只有0， 1， 2.而不应该出现第四种情况
                resId = R.drawable.splash_launch_page_1;
                break;
        }
        Bitmap resultBitmap = BitmapFactory.decodeResource(sContext.getResources(), resId);
        return resultBitmap;
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount()
    {
        return PAGE_COUNT;
    }
}
