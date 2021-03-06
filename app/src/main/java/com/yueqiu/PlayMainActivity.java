package com.yueqiu;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;

import com.yueqiu.activity.PlayBusinessActivity;
import com.yueqiu.activity.PlayIssueActivity;
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.dao.PlayDao;
import com.yueqiu.fragment.play.PlayBasicFragment;
import com.yueqiu.util.Utils;

import java.lang.reflect.Field;
import java.util.List;

public class PlayMainActivity extends FragmentActivity implements ActionBar.TabListener {
    private ViewPager mViewPager;
    private String[] mTitles;
    private SectionPagerAdapter mPagerAdapter;
    private ActionBar mActionBar;
    private PlayDao mPlayDao;
    private List<PlayInfo> mCacheAllList;
    private static int sCurrentItem = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiard_group);
        /////////////////////////////////////////////////
        //TODO:由于IOS那边没做缓存功能，这里先不做缓存，缓存属于
        //TODO:后期功能，不过这里可以考虑一下用更好的异步方法，而不是简单地线程
        /**
         * 逻辑：从数据库中读取出play表的全部信息，并根据tableId和type生成唯一的key
         * 存入全局的play map中，这个map保存全部play信息，并保证每条play信息都是唯一的
         * ps:由于有5个type，所以type必然是一个区分不同play信息的判断条件，另外从服务器返回
         * 的每条play信息的tableId肯定是唯一的，所以这里用tableId和type生成一个对象作为
         * 唯一标识的key，该对象一定要覆盖equals和hashCode方法
         */
//        mPlayDao = DaoFactory.getPlay(this);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mCacheAllList = mPlayDao.getAllPlayInfo();
//                for(PlayInfo info : mCacheAllList){
//                    PlayIdentity identity = new PlayIdentity();
//                    identity.table_id = info.getTable_id();
//                    identity.type = info.getType();
//                    YueQiuApp.sPlayMap.put(identity,info);
//                }
//            }
//        }).start();
        //////////////////////////////////////////////////


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

        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) searchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.LTGRAY);

        // 用于改变SearchView当中的icon
        searchView.setIconifiedByDefault(false);
        try {
            Field searchField = SearchView.class.getDeclaredField("mSearchHintIcon");
            searchField.setAccessible(true);
            ImageView searchHintIcon = (ImageView) searchField.get(searchView);
            searchHintIcon.setImageResource(R.drawable.search);
        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        //TODO:底下这句是发送系统定义好的搜索Intent.ACTION_SEARCH
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
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
        //mActionBar.removeAllTabs();
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
