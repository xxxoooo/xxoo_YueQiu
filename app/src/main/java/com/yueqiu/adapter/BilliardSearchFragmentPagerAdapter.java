package com.yueqiu.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 14/12/19.
 *
 * SearchActivity当中的RadioGroup直接控制的Fragment的FragmentPagerAdapter
 *
 */
public class BilliardSearchFragmentPagerAdapter extends FragmentPagerAdapter
{
    private static final String TAG = "BilliardSearchFragmentPagerAdapter";

    private ArrayList<Fragment> mFragmentList;

    public BilliardSearchFragmentPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    public BilliardSearchFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments)
    {
        super(fm);
        this.mFragmentList = fragments;
    }

    @Override
    public Fragment getItem(int i)
    {
        return mFragmentList.get(i);
    }

    @Override
    public int getCount()
    {
        return mFragmentList.size();
    }
}


























