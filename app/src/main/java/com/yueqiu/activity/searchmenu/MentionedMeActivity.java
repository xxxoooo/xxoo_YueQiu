package com.yueqiu.activity.searchmenu;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.fragment.FriendRequestFragment;
import com.yueqiu.fragment.ReplyMentionMeFragment;
import com.yueqiu.fragment.group.BilliardGroupBasicFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * created by doushuqi on 14/12/19.
 */
public class MentionedMeActivity extends FragmentActivity implements ActionBar.TabListener {
    private static final String TAG = "MentionedMeActivity";
    private ViewPager mViewPager;
    //private List<Fragment> mFragments;
    private Fragment mFragment;
//    private TextView mReply, mFriendRequest, mBack;
//    FragmentPagerAdapter adapter;
//    private int currIndex = 0;
//    private int bottomLineWidth;
//    private int offset = 0;
//    private int position_one;
//    public final static int num = 2;
//    private ImageView ivBottomLine;
//
//    private LinearLayout.LayoutParams mRequestBottomLine = new LinearLayout.LayoutParams(160, 4);
//    private LinearLayout.LayoutParams mReplyBottomLine = new LinearLayout.LayoutParams(80, 4);
    private String mTitles[];
    private SectionPagerAdapter mPagerAdapter;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentioned_me);
        //init();
        initActionBar();
        //initWidth();

        mTitles = new String[]{
          getString(R.string.mentioned_me_reply),
          getString(R.string.mentioned_me_friend_request)
        };
        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.mention_me_container);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                mActionBar.setSelectedNavigationItem(position);
            }
        });

        ActionBar.Tab tab;
        for(int i=0; i<mPagerAdapter.getCount();i++){
            tab = mActionBar.newTab().setText(mPagerAdapter.getPageTitle(i)).setTabListener(this);
            mActionBar.addTab(tab);
        }

//        TranslateAnimation animation = new TranslateAnimation(position_one, offset, 0, 0);
//        animation.setFillAfter(true);
//        animation.setDuration(300);
//        ivBottomLine.startAnimation(animation);
//        mFragments = new ArrayList<Fragment>();
//        mFragments.add(new ReplyMentionMeFragment());
//        mFragments.add(new FriendRequestFragment());
//
//        adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
//            @Override
//            public Fragment getItem(int i) {
//                return mFragments.get(i);
//            }
//
//            @Override
//            public int getCount() {
//                return mFragments.size();
//            }
//        };
        mViewPager.setAdapter(mPagerAdapter);

        //mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

//    private void init() {
//
//        mReply = (TextView) findViewById(R.id.mention_me_tv_reply);
//        mReply.setTextColor(getResources().getColor(R.color.red));
//        mFriendRequest = (TextView) findViewById(R.id.mention_me_tv_friend_request);
//        mReply.setOnClickListener(this);
//        mFriendRequest.setOnClickListener(this);
//    }
    private void initActionBar(){
        mActionBar = getActionBar();
        if (mActionBar != null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.custom_actionbar_layout, null);
            View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
            saveMenuItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MentionedMeActivity.this.finish();
                }
            });
            TextView title = (TextView) customActionBarView.findViewById(R.id.action_bar_title);
            title.setText(getString(R.string.search_mentioned_me_str));
            mActionBar.setDisplayShowCustomEnabled(true);
            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mActionBar.setCustomView(customActionBarView,params);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }


    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    public class SectionPagerAdapter extends FragmentPagerAdapter{

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch(i){
                case 0:
                    mFragment = new ReplyMentionMeFragment();
                    break;
                case 1:
                    mFragment = new FriendRequestFragment();
                    break;
            }
            return mFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }

//    private void initWidth() {
//        ivBottomLine = (ImageView) findViewById(R.id.iv_bottom_line);
//        bottomLineWidth = ivBottomLine.getLayoutParams().width;
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int screenW = dm.widthPixels;
//        offset = (int) ((screenW / num - bottomLineWidth) / 2);
//        int avg = (int) (screenW / num);
//        position_one = avg + offset;
//    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.mention_me_tv_reply:
//                mViewPager.setCurrentItem(0);
//                break;
//            case R.id.mention_me_tv_friend_request:
//                mViewPager.setCurrentItem(1);
//                break;
//        }
//    }


//    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
//
//        @Override
//        public void onPageSelected(int arg0) {
//            Animation animation = null;
//            switch (arg0) {
//                case 0:
//                    if (currIndex == 1) {
//                        animation = new TranslateAnimation(position_one, offset, 0, 0);
//                        mFriendRequest.setTextColor(getResources().getColor(R.color.black));
//                    }
//                    ivBottomLine.setLayoutParams(mReplyBottomLine);
//                    mReply.setTextColor(getResources().getColor(R.color.red));
//                    break;
//                case 1:
//                    if (currIndex == 0) {
//                        animation = new TranslateAnimation(offset, position_one - 40, 0, 0);
//                        mReply.setTextColor(getResources().getColor(R.color.black));
//                    }
//                    ivBottomLine.setLayoutParams(mRequestBottomLine);
//                    mFriendRequest.setTextColor(getResources().getColor(R.color.red));
//                    break;
//            }
//            currIndex = arg0;
//            animation.setFillAfter(true);
//            animation.setDuration(300);
//            ivBottomLine.startAnimation(animation);
//        }

//        @Override
//        public void onPageScrolled(int arg0, float arg1, int arg2) {
//        }
//
//        @Override
//        public void onPageScrollStateChanged(int arg0) {
//        }
//    }
}
