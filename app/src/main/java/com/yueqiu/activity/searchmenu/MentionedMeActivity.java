package com.yueqiu.activity.searchmenu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.fragment.FriendRequestFragment;
import com.yueqiu.fragment.ReplyMentionMeFragment;

import java.util.ArrayList;
import java.util.List;


public class MentionedMeActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = "MentionedMeActivity";
    private ViewPager mViewPager;
    private List<Fragment> mFragments;
    private TextView mReply, mFriendRequest, mBack;
    FragmentPagerAdapter adapter;
    int current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentioned_me);
        init();
        mFragments = new ArrayList<Fragment>();
        mFragments.add(new ReplyMentionMeFragment());
        mFragments.add(new FriendRequestFragment());
    }

    private void init() {
        mReply = (TextView) findViewById(R.id.mention_me_tv_reply);
        mFriendRequest = (TextView) findViewById(R.id.mention_me_tv_friend_request);
        mBack = (TextView) findViewById(R.id.mention_me_btn_back);
        mReply.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mFriendRequest.setOnClickListener(this);
        mViewPager = (ViewPager) findViewById(R.id.mention_me_container);
        adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return mFragments.get(i);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        };
        mViewPager.setAdapter(adapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                current = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mention_me_btn_back:
                finish();
                break;
            case R.id.mention_me_tv_reply:
                switchFragment(0);
                break;
            case R.id.mention_me_tv_friend_request:
                switchFragment(1);
                break;
        }
    }

    private void switchFragment(int pos) {
        if (pos == current)
            return;
        mViewPager.setCurrentItem(pos);
    }
}
