package com.yueqiu;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
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
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.yueqiu.activity.GroupIssueTopic;
import com.yueqiu.activity.SearchResultActivity;
import com.yueqiu.fragment.group.BilliardGroupBasicFragment;

/**
 * Created by wangyun on 14/12/17.
 * 台球圈Activity
 */
public class BilliardGroupActivity extends FragmentActivity implements ActionBar.TabListener{
    public static final String BILLIARD_TAB_NAME = "billiard_tab_name";
    private ViewPager mViewPager;
    private String[] mTitles;
    private SectionPagerAdapter mPagerAdapter;
    private ActionBar mActionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiard_group);

        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        mTitles = new String[]{getString(R.string.billiard_all),
                getString(R.string.billiard_get_master),
                getString(R.string.billiard_be_master),
                getString(R.string.billiard_find_friend),
                getString(R.string.billiard_equipment),
                getString(R.string.billiard_other)
        };
    }

    public class SectionPagerAdapter extends FragmentPagerAdapter{

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            Fragment mFragment = new BilliardGroupBasicFragment();
            return mFragment;
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

        SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =(SearchView) menu.findItem(R.id.group_nemu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                this.finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
            case R.id.group_menu_editor:
                Intent intent = new Intent(this, GroupIssueTopic.class);
                startActivity(intent);
                overridePendingTransition(R.anim.group_in_to_left,R.anim.group_out_to_left);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActionBar = getActionBar();

        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setTitle(getString(R.string.billiard_group));
        mActionBar.setDisplayHomeAsUpEnabled(true);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                mActionBar.setSelectedNavigationItem(position);
            }
        });

        Tab tab;
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
