package com.yueqiu;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.yueqiu.activity.NearbyResultActivity;
import com.yueqiu.activity.PlayBusinessActivity;
import com.yueqiu.activity.PlayIssueActivity;
import com.yueqiu.bean.PlayIdentity;
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.PlayDao;
import com.yueqiu.fragment.play.PlayBasicFragment;
import com.yueqiu.util.Utils;

import java.util.List;


/**
 * Created by yinfeng on 15/1/12.
 */
public class PlayMainActivity extends FragmentActivity implements ActionBar.TabListener {
    private ViewPager mViewPager;
    private String[] mTitles;
    private SectionPagerAdapter mPagerAdapter;
    private ActionBar mActionBar;
    private PlayDao mPlayDao;
    private List<PlayInfo> mDBAllList;
    private static int sCurrentItem = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiard_group);

        mPlayDao = DaoFactory.getPlay(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mDBAllList = mPlayDao.getAllPlayInfo();
                for(PlayInfo info : mDBAllList){
                    PlayIdentity identity = new PlayIdentity();
                    identity.table_id = info.getTable_id();
                    identity.type = info.getType();
                    YueQiuApp.sPlayMap.put(identity,info);
                }
            }
        }).start();

        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mTitles = new String[]{getString(R.string.group_activity),
                getString(R.string.star_meet),
                getString(R.string.billiard_show),
                getString(R.string.complete),
                getString(R.string.billiard_other)
        };

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


    public class SectionPagerAdapter extends FragmentStatePagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new PlayBasicFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("type",i+1);
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
                startActivity(new Intent(this, PlayBusinessActivity.class));
                overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
                break;
            case R.id.menu_issue_activities:
                int user_id = YueQiuApp.sUserInfo.getUser_id();
                if(user_id < 1) {
                    Utils.showToast(this,getString(R.string.please_login_first));
                }else{
                    startActivity(new Intent(this, PlayIssueActivity.class));
                    overridePendingTransition(R.anim.group_in_to_left,R.anim.group_out_to_left);
                }

                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activities, menu);

        SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =(SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, NearbyResultActivity.class)));
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();


        mViewPager.setCurrentItem(sCurrentItem);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActionBar.removeAllTabs();
        sCurrentItem = mViewPager.getCurrentItem();
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
