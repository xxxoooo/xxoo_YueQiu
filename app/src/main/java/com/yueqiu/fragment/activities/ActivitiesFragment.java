package com.yueqiu.fragment.activities;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;


import com.yueqiu.R;
import com.yueqiu.adapter.ActivitiesViewPagerAdapter;

import java.util.ArrayList;



/**
 * Created by yinfeng on 14/12/18.
 */
public class ActivitiesFragment extends Fragment {
    private static final String TAG = "ActivitiesFragment";
    private String mIndex;

    Resources resources;
    private ViewPager mPager;
    private ArrayList<Fragment> fragmentsList;

    private int currIndex = 0;
    private int bottomLineWidth;
    private int offset = 0;
    private int position_one;
    public final static int num = 2 ;

    private ImageView mIvBottomLine;
    private TextView mTvTime, mTvDistance;
    private Fragment mTimeFragment;
    private Fragment mDistanceFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activities_fragment, null);
        resources = getResources();
        InitWidth(view);
        InitTextView(view);
        InitViewPager(view);
        TranslateAnimation animation = new TranslateAnimation(position_one, offset, 0, 0);
        mTvTime.setTextColor(resources.getColor(R.color.lightwhite));
        animation.setFillAfter(true);
        animation.setDuration(300);
        mIvBottomLine.startAnimation(animation);
        return view;
    }

    private void InitTextView(View parentView) {
        mTvTime = (TextView) parentView.findViewById(R.id.activities_tv_time);
        mTvDistance = (TextView) parentView.findViewById(R.id.activities_tv_distance);

        mTvTime.setOnClickListener(new MyOnClickListener(0));
        mTvDistance.setOnClickListener(new MyOnClickListener(1));
    }

    private void InitViewPager(View parentView) {
        mPager = (ViewPager) parentView.findViewById(R.id.activities_vp);
        fragmentsList = new ArrayList<Fragment>();

        mTimeFragment = new ActivitiesFragment1();
        mDistanceFragment = new ActivitiesFragment1();

        fragmentsList.add(mTimeFragment);
        fragmentsList.add(mDistanceFragment);

        mPager.setAdapter(new ActivitiesViewPagerAdapter(getChildFragmentManager(), fragmentsList));
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        mPager.setCurrentItem(0);

    }

    private void InitWidth(View parentView) {
        mIvBottomLine = (ImageView) parentView.findViewById(R.id.activities_iv_bottom_line);
        bottomLineWidth = mIvBottomLine.getLayoutParams().width;
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;
        offset = (int) ((screenW / num - bottomLineWidth) / 2);
        int avg = (int) (screenW / num);
        position_one = avg + offset;


    }

    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }
    };

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            switch (arg0) {
                case 0:
                    if (currIndex == 1) {
                        animation = new TranslateAnimation(position_one, offset, 0, 0);
                        mTvTime.setTextColor(resources.getColor(R.color.lightwhite));
                    }
                    mTvDistance.setTextColor(resources.getColor(R.color.white));
                    break;
                case 1:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, position_one, 0, 0);
                        mTvDistance.setTextColor(resources.getColor(R.color.lightwhite));
                    }
                    mTvTime.setTextColor(resources.getColor(R.color.white));
                    break;
            }
            currIndex = arg0;
            animation.setFillAfter(true);
            animation.setDuration(300);
            mIvBottomLine.startAnimation(animation);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
