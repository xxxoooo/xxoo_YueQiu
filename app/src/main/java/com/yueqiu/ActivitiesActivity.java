package com.yueqiu;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.fragment.FriendRequestFragment;
import com.yueqiu.fragment.ReplyMentionMeFragment;
import com.yueqiu.fragment.activities.ActivitiesFragment;
import com.yueqiu.fragment.activities.ActivitiesFragment1;
import com.yueqiu.fragment.group.BilliardGroupBasicFragment;


/**
 * Created by yinfeng on 14/12/18.
 */
public class ActivitiesActivity extends FragmentActivity implements View.OnClickListener,ActionBar.TabListener {
    private static final String TAG = "ActivitiesActivity";
    private ActivitiesFragment1 mFragment;
    private ActionBar mActionBar;
    private String mTitles[];
    private ViewPager mViewPager;
    private SectionPagerAdapter mPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activites);
        mTitles = new String[]{
                getString(R.string.time),
                getString(R.string.distance)
        };
        initActionBar();
        initView();


    }

    @Override
    protected void onResume() {
        super.onResume();
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.activities));
    }

    private void initView()
    {
        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.activitis_pager);
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
    }

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
                    ActivitiesActivity.this.finish();
                }
            });
            TextView title = (TextView) customActionBarView.findViewById(R.id.action_bar_title);
            title.setText(getString(R.string.tab_title_activity));
            mActionBar.setDisplayShowCustomEnabled(true);
            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mActionBar.setCustomView(customActionBarView,params);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    public class SectionPagerAdapter extends FragmentPagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            mFragment = new ActivitiesFragment1();
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
}
