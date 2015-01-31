package com.yueqiu;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
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

import com.yueqiu.activity.GroupIssueTopic;
import com.yueqiu.activity.NearbyResultActivity;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.GroupInfoDao;
import com.yueqiu.fragment.group.BilliardGroupBasicFragment;
import com.yueqiu.util.Utils;

import java.util.List;

/**
 * Created by wangyun on 14/12/17.
 * 台球圈Activity
 */
public class BilliardGroupActivity extends FragmentActivity implements ActionBar.TabListener{
    private ViewPager mViewPager;
    private String[] mTitles;
    private SectionPagerAdapter mPagerAdapter;
    private ActionBar mActionBar;
    private GroupInfoDao mGroupDao;
    private List<GroupNoteInfo> mCacheList;
    private static int sCurrentItem = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiard_group);

        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        ////////////////////////////////////////////////////////////////////
        //TODO:由于目前不需要缓存，这里先将缓存的功能去掉，这里考虑可以使用loader等
        //TODO:更有效率的异步方式
        /**
         * 逻辑：从数据库中读取出group表的全部信息，这里使用noteId作为唯一的key
         * 存入全局的group map中，这个map保存全部group信息，并保证每条group信息都是唯一的
         * ps:这里使用noteId作为key值是因为，服务器返回的noteId值是唯一的，而台球圈第一个fragment的type为全部，
         * 也就是说后面几个fragment的数据有可能和第一个fragment重合，所以没使用noteId和type联合的方式作为key作为
         * 唯一标识的key，该对象一定要覆盖equals和hashCode方法
         */
//        mGroupDao = DaoFactory.getGroupDao(this);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mCacheList = mGroupDao.getAllGroupInfo();
//                for(GroupNoteInfo info : mCacheList){
//                    YueQiuApp.sGroupDbMap.put(info.getNoteId(),info);
//                }
//            }
//        }).start();
        ////////////////////////////////////////////////////////////////////
        mTitles = new String[]{getString(R.string.billiard_all),
                getString(R.string.billiard_get_master),
                getString(R.string.billiard_be_master),
                getString(R.string.billiard_find_friend),
                getString(R.string.billiard_equipment),
                getString(R.string.billiard_other)
        };
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
        //mViewPager.setCurrentItem(sCurrentItem);


    }

    public class SectionPagerAdapter extends FragmentStatePagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            Fragment fragment = new BilliardGroupBasicFragment();
            Bundle args = new Bundle();
            args.putInt("type",i);
            fragment.setArguments(args);
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
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, NearbyResultActivity.class)));
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
                int user_id = YueQiuApp.sUserInfo.getUser_id();
                if(user_id < 1){
                    Utils.showToast(this, getString(R.string.please_login_first));
                }else{
                    Intent intent = new Intent(this, GroupIssueTopic.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.group_in_to_left,R.anim.group_out_to_left);
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setCurrentItem(sCurrentItem);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
