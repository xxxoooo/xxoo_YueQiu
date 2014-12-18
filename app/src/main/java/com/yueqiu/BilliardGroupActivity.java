package com.yueqiu;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;

import com.yueqiu.fragment.group.BilliardGroupBasicFragment;
import com.yueqiu.fragment.group.BilliardGroupChildFragment;

/**
 * Created by wangyun on 14/12/17.
 * 台球圈Activity
 */
public class BilliardGroupActivity extends FragmentActivity implements ActionBar.TabListener{

    private ViewPager mViewPager;
    private String[] mTitles;
    private SectionPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiard_group);

        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        mTitles = new String[]{getString(R.string.billiard_all),getString(R.string.billiard_get_master),
                getString(R.string.billiard_be_master),getString(R.string.billiard_find_friend),getString(R.string.billiard_equipment)};
        final ActionBar actionBar = getActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setTitle(getString(R.string.billiard_group));
        actionBar.setDisplayHomeAsUpEnabled(true);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        Tab tab;
        for(int i=0; i<mPagerAdapter.getCount();i++){
            tab = actionBar.newTab().setText(mPagerAdapter.getPageTitle(i)).setTabListener(this);
            actionBar.addTab(tab);
        }

    }

    public class SectionPagerAdapter extends FragmentPagerAdapter{

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            Fragment mFragment = new BilliardGroupBasicFragment();
//            Bundle mArgs = new Bundle();
//            mArgs.putString(BilliardGroupBasicFragment.BILLIARD_TAB_NAME,mTitles[i]);
//            mFragment.setArguments(mArgs);
            return mFragment;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }


    @Override
    public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.billiard_group, menu);
        return true;
    }
}
