package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;

import com.yueqiu.R;
import com.yueqiu.fragment.slidemenu.PartInFragment;

import java.lang.reflect.Field;

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
    private static int sCurrentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiard_group);
        initActionBar();
        mTitles = new String[]{
                getString(R.string.billiard_dating),
                getString(R.string.play)
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
        mViewPager.setCurrentItem(sCurrentItem);
    }

    private void initActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setTitle(getString(R.string.search_my_participation_str));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.billiard_search, menu);

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
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
        return true;
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
            mFragment = new PartInFragment();
            Bundle args = new Bundle();
            args.putInt("type",i+1);
            mFragment.setArguments(args);
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

    @Override
    protected void onPause() {
        super.onPause();
        sCurrentItem = mViewPager.getCurrentItem();

    }
}
