package com.yueqiu;

import android.app.ActionBar;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.activity.searchmenu.ActivitiesIssueActivity;
import com.yueqiu.activity.searchmenu.FeedbackActivity;
import com.yueqiu.activity.searchmenu.LoginActivity;
import com.yueqiu.activity.searchmenu.MentionedMeActivity;
import com.yueqiu.activity.searchmenu.MyProfileActivity;
import com.yueqiu.activity.searchmenu.MyfavorCollActivity;
import com.yueqiu.activity.searchmenu.PublishedInfoActivity;
import com.yueqiu.view.ActionBarDrawToggle;
import com.yueqiu.view.DrawerArrowDrawable;

import java.util.ArrayList;


public class HomeTabActivity extends TabActivity implements TabHost.OnTabChangeListener
{
    private static final String TAG = "HomeTabActivity";

    private TabHost mTabHost;
    private ArrayList<TabHost.TabSpec> mTabList = new ArrayList<TabHost.TabSpec>();

    private DrawerLayout mDrawerLayout;
    private ListView mDrawList;
    private ActionBarDrawToggle mDrawerToggle;
    private DrawerArrowDrawable mDrawerArrow;
    private boolean mDrawerArrowColor;
    public static ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_tab);

        initTabHost();
        initDrawer();

    }

    private static int[] sTabImgResIds = {R.drawable.tab_search, R.drawable.tab_chat_bar, R.drawable.tab_activity, R.drawable.tab_billiard_circle};
    private static int[] sTabNameResId = {R.string.tab_title_search, R.drawable.tab_chat_bar, R.drawable.tab_activity, R.drawable.tab_billiard_circle};

    private static final String TAB_TAG = "tab_tag";

    private void initTabHost()
    {
        mTabHost = getTabHost();
        int i;
        TabHost.TabSpec tabSpec;
        for (i = 0; i < 4; i++)
        {
            View tabLayout = getLayoutInflater().inflate(R.layout.tab_widget_layout, null);
            TextView tvName = (TextView) tabLayout.findViewById(R.id.tab_tv_item);
            tvName.setText(sTabNameResId[i]);
            ImageView img = (ImageView) tabLayout.findViewById(R.id.tab_img_item);
            img.setImageResource(sTabImgResIds[i]);
            tabSpec = mTabHost.newTabSpec(TAB_TAG + i);
            tabSpec.setIndicator(tabLayout);
            mTabList.add(tabSpec);
        }

        // add the tab in
        mTabHost.addTab(mTabList.get(0).setContent(new Intent(HomeTabActivity.this, BilliardSearchActivity.class)));
        mTabHost.addTab(mTabList.get(1).setContent(new Intent(HomeTabActivity.this, ChatBarActivity.class)));
        mTabHost.addTab(mTabList.get(2).setContent(new Intent(HomeTabActivity.this, ActivitiesActivity.class)));
        mTabHost.addTab(mTabList.get(3).setContent(new Intent(HomeTabActivity.this, BilliardGroupActivity.class)));

        mTabHost.setOnTabChangedListener(this);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                if(mDrawerLayout.isDrawerOpen(mDrawList)){
                    mDrawerLayout.closeDrawer(mDrawList);
                }else{
                    mDrawerLayout.openDrawer(mDrawList);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabChanged(String tabId)
    {
        Toast.makeText(this, "changing tab", Toast.LENGTH_LONG).show();
    }

    private void initDrawer(){
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawList = (ListView) findViewById(R.id.drawer_list);

        mDrawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };


        mDrawerToggle = new ActionBarDrawToggle(this,mDrawerLayout,mDrawerArrow,R.string.drawer_open,R.string.drawer_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        String[] values = new String[]{
          getString(R.string.search_account_name_str),
          getString(R.string.search_my_profile_str),
          getString(R.string.search_mentioned_me_str),
          getString(R.string.search_my_favor_collection_str),
          getString(R.string.search_my_published_info_str),
          getString(R.string.search_publishing_dating_billiards_info_str),
          getString(R.string.search_feed_back_str),
          getString(R.string.search_logout_str)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                android.R.id.text1,values);
        mDrawList.setAdapter(adapter);
        mDrawList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                switch(i){
                    case 0:
                        intent.setClass(HomeTabActivity.this, LoginActivity.class);
                        break;
                    case 1:
                        intent.setClass(HomeTabActivity.this, MyProfileActivity.class);
                        break;
                    case 2:
                        intent.setClass(HomeTabActivity.this, MentionedMeActivity.class);
                        break;
                    case 3:
                        intent.setClass(HomeTabActivity.this, MyfavorCollActivity.class);
                        break;
                    case 4:
                        intent.setClass(HomeTabActivity.this, PublishedInfoActivity.class);
                        break;
                    case 5:
                        intent.setClass(HomeTabActivity.this, ActivitiesIssueActivity.class);
                        break;
                    case 6:
                        intent.setClass(HomeTabActivity.this, FeedbackActivity.class);
                        break;
                    case 7:
                        break;

                }
                startActivity(intent);
                if(mDrawerLayout.isDrawerOpen(mDrawList)){
                    mDrawerLayout.closeDrawer(mDrawList);
                }
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle icicle) {
        super.onPostCreate(icicle);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}





























