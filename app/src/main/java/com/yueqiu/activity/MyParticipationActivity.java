package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.yueqiu.R;
import com.yueqiu.fragment.participation.MyPlayFragment;

/**
 * created by doushuqi on 14/12/19.
 */
public class MyParticipationActivity extends FragmentActivity implements ActionBar.TabListener {
    private static final String TAG = "MyParticipationActivity";
    private ViewPager mViewPager;
    private Fragment mFragment;
    private String mTitles[];
    private SectionPagerAdapter mPagerAdapter;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiard_group);
        initActionBar();
        mTitles = new String[]{
                getString(R.string.billiard_dating),
                getString(R.string.participation_activities)
        };
        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mActionBar.setSelectedNavigationItem(position);
            }
        });

        ActionBar.Tab tab;
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            tab = mActionBar.newTab().setText(mPagerAdapter.getPageTitle(i)).setTabListener(this);
            mActionBar.addTab(tab);
        }

        mViewPager.setAdapter(mPagerAdapter);
    }

    private void initActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setTitle(getString(R.string.search_my_participation_str));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.top_in,R.anim.top_out);
                break;
        }
        return true;
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

    public class SectionPagerAdapter extends FragmentPagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
//                    mFragment = new BilliardDatingFragment();
                    mFragment = new MyPlayFragment();//should to be replaced!
                    break;
                case 1:
                    mFragment = new MyPlayFragment();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.top_in,R.anim.top_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
