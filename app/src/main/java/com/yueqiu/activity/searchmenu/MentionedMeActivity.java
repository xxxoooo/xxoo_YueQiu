package com.yueqiu.activity.searchmenu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.fragment.FriendRequestFragment;
import com.yueqiu.fragment.ReplyMentionMeFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * created by doushuqi on 14/12/19.
 */
public class MentionedMeActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = "MentionedMeActivity";
    private ViewPager mViewPager;
    private List<Fragment> mFragments;
    private TextView mReply, mFriendRequest, mBack;
    FragmentPagerAdapter adapter;
    private int currIndex = 0;
    private int bottomLineWidth;
    private int offset = 0;
    private int position_one;
    public final static int num = 2;
    private ImageView ivBottomLine;
    private LinearLayout.LayoutParams mRequestBottomLine = new LinearLayout.LayoutParams(160, 4);
    private LinearLayout.LayoutParams mReplyBottomLine = new LinearLayout.LayoutParams(80, 4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mentioned_me);
        init();
        initWidth();
        TranslateAnimation animation = new TranslateAnimation(position_one, offset, 0, 0);
        animation.setFillAfter(true);
        animation.setDuration(300);
        ivBottomLine.startAnimation(animation);
        mFragments = new ArrayList<Fragment>();
        mFragments.add(new ReplyMentionMeFragment());
        mFragments.add(new FriendRequestFragment());
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

        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void init() {
        mReply = (TextView) findViewById(R.id.mention_me_tv_reply);
        mReply.setTextColor(getResources().getColor(R.color.red));
        mFriendRequest = (TextView) findViewById(R.id.mention_me_tv_friend_request);
        mBack = (TextView) findViewById(R.id.mention_me_btn_back);
        mReply.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mFriendRequest.setOnClickListener(this);
    }

    private void initWidth() {
        ivBottomLine = (ImageView) findViewById(R.id.iv_bottom_line);
        bottomLineWidth = ivBottomLine.getLayoutParams().width;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;
        offset = (int) ((screenW / num - bottomLineWidth) / 2);
        int avg = (int) (screenW / num);
        position_one = avg + offset;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mention_me_btn_back:
                finish();
                break;
            case R.id.mention_me_tv_reply:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.mention_me_tv_friend_request:
                mViewPager.setCurrentItem(1);
                break;
        }
    }


    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            switch (arg0) {
                case 0:
                    if (currIndex == 1) {
                        animation = new TranslateAnimation(position_one, offset, 0, 0);
                        mFriendRequest.setTextColor(getResources().getColor(R.color.black));
                    }
                    ivBottomLine.setLayoutParams(mReplyBottomLine);
                    mReply.setTextColor(getResources().getColor(R.color.red));
                    break;
                case 1:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, position_one - 40, 0, 0);
                        mReply.setTextColor(getResources().getColor(R.color.black));
                    }
                    ivBottomLine.setLayoutParams(mRequestBottomLine);
                    mFriendRequest.setTextColor(getResources().getColor(R.color.red));
                    break;
            }
            currIndex = arg0;
            animation.setFillAfter(true);
            animation.setDuration(300);
            ivBottomLine.startAnimation(animation);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
