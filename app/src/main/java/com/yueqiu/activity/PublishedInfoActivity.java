package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.yueqiu.R;
import com.yueqiu.fragment.slidemenu.PublishBasicFragment;
import com.yueqiu.fragment.slidemenu.PublishedFragment;


public class PublishedInfoActivity extends FragmentActivity implements ActionBar.TabListener
{

    private ViewPager mViewPager;
    private String[] mTitles;
    private SectionPagerAdapter mPagerAdapter;
    private ActionBar mActionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myfavor_coll);
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setTitle(getString(R.string.search_my_published_info_str));

        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mTitles = new String[]{
                getString(R.string.search_billiard_dating_str),
                getString(R.string.tab_title_activity),
                getString(R.string.billiard_group)
        };
        mViewPager = (ViewPager) findViewById(R.id.favor_coll_pager);
       // mViewPager = $(R.id.favor_coll_pager);

    }


    @Override
    protected void onResume() {
        super.onResume();
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
            //Fragment fragment = new PublishBasicFragment();
            Fragment fragment = new PublishedFragment();
            Bundle args = new Bundle();
            args.putInt("type",i+1);
            fragment.setArguments(args);
            return fragment;

        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.billiard_search, menu);

        SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =(SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
