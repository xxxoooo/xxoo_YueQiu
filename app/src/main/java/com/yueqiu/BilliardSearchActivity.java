package com.yueqiu;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.yueqiu.fragment.search.BilliardsSearchMateFragment;

/**
 * 首页的SearchActivity
 */
public class BilliardSearchActivity extends FragmentActivity implements ActionBar.TabListener
{
    private static final String TAG = "BilliardSearchActivity";

    private static final int NUM_OF_FRAGMENTS = 5;

    // make the instances of the basic fragment that directly loaded in the BilliardSearchActivity
    private BilliardsSearchMateFragment mMateFragment;

    private ViewPager mViewPager;
    private String[] mTitles;
    private SectionPagerAdapter mPagerAdapter;
    private ActionBar mActionBar;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_billiard_search);
        mActionBar = getParent().getActionBar();
        mTitles = new String[]{getString(R.string.search_billiard_mate_str),
                getString(R.string.search_billiard_assist_coauch_str),
                getString(R.string.search_billiard_coauch_str),
                getString(R.string.search_billiard_room_str),
                getString(R.string.search_billiard_dating_str)};
        mViewPager = (ViewPager) findViewById(R.id.search_parent_fragment_view_pager);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mActionBar.removeAllTabs();
        mActionBar.setTitle(getString(R.string.billiard_search));
        setupTabs();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mActionBar.removeAllTabs();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mContext = null;
    }

    private void setupTabs()
    {
        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setTitle(getString(R.string.billiard_search));

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                mActionBar.setSelectedNavigationItem(position);
            }
        });

        ActionBar.Tab tab;

        int i;
        final int count = mPagerAdapter.getCount();
        for (i = 0; i < count; i++)
        {
            tab = mActionBar.newTab()
                    .setText(mPagerAdapter.getPageTitle(i))
                    .setTabListener(this);
            mActionBar.addTab(tab);
        }
    }



    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft)
    {
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft)
    {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft)
    {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    private class SectionPagerAdapter extends FragmentPagerAdapter
    {

        public SectionPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int index)
        {
            Fragment fragment = BilliardsSearchMateFragment.newInstance(mContext, "testguoshichao");
            Bundle args = new Bundle();
            args.putString("test", mTitles[index]);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount()
        {
            return NUM_OF_FRAGMENTS;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return mTitles[position];
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.billiard_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

}

























































