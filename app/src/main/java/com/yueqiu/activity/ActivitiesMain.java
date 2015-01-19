package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.yueqiu.ActivitiesActivity;
import com.yueqiu.R;
import com.yueqiu.fragment.activities.CompleteFragment;
import com.yueqiu.fragment.activities.ExhibitionFragment;
import com.yueqiu.fragment.activities.GroupActvitiesFragment;
import com.yueqiu.fragment.activities.OtherFragment;
import com.yueqiu.fragment.activities.StarMeetFragment;
import com.yueqiu.fragment.group.BilliardGroupBasicFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinfeng on 15/1/12.
 */
public class ActivitiesMain extends FragmentActivity implements ActionBar.TabListener {
    private ViewPager mViewPager;
    private String[] mTitles;
    private SectionPagerAdapter mPagerAdapter;
    private ActionBar mActionBar;
    private CompleteFragment completeFragment;
    private ExhibitionFragment exhibitionFragment;
    private GroupActvitiesFragment groupActvitiesFragment;
    private OtherFragment otherFragment;
    private StarMeetFragment starMeetFragment;

    private List<Fragment> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiard_group);
        mList = new ArrayList<Fragment>();
        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        mTitles = new String[]{getString(R.string.group_activity),
                getString(R.string.star_meet),
                getString(R.string.billiard_show),
                getString(R.string.complete),
                getString(R.string.billiard_other)
        };
    }


    public class SectionPagerAdapter extends FragmentStatePagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            Bundle bundle = new Bundle();
            switch (i)
            {
                case 0:
                    fragment = new GroupActvitiesFragment();
                    bundle.putInt("type", 1);
                    break;
                case 1:
                    fragment = new StarMeetFragment();
                    bundle.putInt("type", 2);
                    break;
                case 2:
                    fragment = new ExhibitionFragment();
                    bundle.putInt("type", 3);
                    break;
                case 3:
                    fragment = new CompleteFragment();
                    bundle.putInt("type", 4);
                    break;
                case 4:
                    fragment = new OtherFragment();
                    bundle.putInt("type", 5);
                    break;
            }
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }


    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
            case R.id.menu_activities:
                startActivity(new Intent(this, ActivitiesActivity.class));
                overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
                break;
            case R.id.menu_issue_activities:
                startActivity(new Intent(this, ActivitiesIssueActivity.class));
                overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activities, menu);

        SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =(SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActionBar = getActionBar();

        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setTitle(getString(R.string.tab_title_activity));
        mActionBar.setDisplayHomeAsUpEnabled(true);

        mViewPager = (ViewPager) findViewById(R.id.pager);
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

    @Override
    protected void onPause() {
        super.onPause();
        mActionBar.removeAllTabs();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
