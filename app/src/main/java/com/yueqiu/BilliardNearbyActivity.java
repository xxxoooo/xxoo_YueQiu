package com.yueqiu;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.yueqiu.activity.FavorActivity;
import com.yueqiu.activity.SearchResultActivity;
import com.yueqiu.activity.PlayIssueActivity;
import com.yueqiu.activity.FeedbackActivity;
import com.yueqiu.activity.LoginActivity;
import com.yueqiu.activity.MyParticipationActivity;
import com.yueqiu.activity.MyProfileActivity;
import com.yueqiu.activity.PublishedInfoActivity;
import com.yueqiu.adapter.SlideViewAdapter;
import com.yueqiu.bean.ISlideListItem;
import com.yueqiu.bean.SlideAccountItemISlide;
import com.yueqiu.bean.SlideOtherItemISlide;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.nearby.BilliardsNearbyAssistCoauchFragment;
import com.yueqiu.fragment.nearby.BilliardsNearbyCoachFragment;
import com.yueqiu.fragment.nearby.BilliardsNearbyDatingFragment;
import com.yueqiu.fragment.nearby.BilliardsNearbyMateFragment;
import com.yueqiu.fragment.nearby.BilliardsNearbyRoomFragment;
import com.yueqiu.fragment.nearby.common.NearbySubFragmentConstants;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.menudrawer.MenuDrawer;
import com.yueqiu.view.menudrawer.Position;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 首页的SearchActivity
 */
public class BilliardNearbyActivity extends FragmentActivity implements ActionBar.TabListener
{
    private static final String TAG = "BilliardNearbyActivity";

    private static final String STATE_MENUDRAWER = "com.yueqiu.menuDrawer";
    private static final int NUM_OF_FRAGMENTS = 5;

    private static final String FRAGMENT_PAGER_LAST_POSITION = "fragmentPagerLastPosition";
    private static final String CURRENT_KEY = "nearby_current_item";

    private static final int LOGOUT_SUCCESS = 0;
    private static final int LOGOUT_FAILED = 1;

    private ViewPager mViewPager;
    private String[] mTitles;
    private SectionPagerAdapter mPagerAdapter;
    private ActionBar mActionBar;
    private Context mContext;
    private RadioGroup mGroup;
    private RadioButton mNearbyRadio;
    private Intent mIntent = new Intent();
    private MenuDrawer mMenuDrawer;
    private SlideViewAdapter mAdapter;
    private ListView mMenuList;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private List<ISlideListItem> mItemList = new ArrayList<ISlideListItem>();

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    // 这个变量用于保存每次当SearchActivity被切换到别的地方的时候，回来的时候，还能确保我们回到最后一次滑动到的Fragment的position
    private static int sPagerPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, " the onCreate has been called ");
        super.onCreate(savedInstanceState);

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();

        mContext = this;
        mSharedPreferences = getSharedPreferences(PublicConstant.USERBASEUSER, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mActionBar = getActionBar();
        mTitles = new String[]{getString(R.string.search_billiard_mate_str),
                getString(R.string.search_billiard_dating_str),
                getString(R.string.search_billiard_assist_coauch_str),
                getString(R.string.search_billiard_coauch_str),
                getString(R.string.search_billiard_room_str)};
        mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.BEHIND, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
        mMenuDrawer.setContentView(R.layout.activity_nearby_billiard);
        mMenuDrawer.setMenuView(R.layout.slide_drawer_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mViewPager = (ViewPager) findViewById(R.id.search_parent_fragment_view_pager);

        mGroup = (RadioGroup) findViewById(R.id.search_parent_radio_group);
        mNearbyRadio = (RadioButton) findViewById(R.id.first_title_nearby);
        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId) {
                    case R.id.first_title_nearby:
                        break;
                    case R.id.first_title_chatbar:
                        mIntent.setClass(BilliardNearbyActivity.this, ChatBarActivity.class);
                        startActivity(mIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;
                    case R.id.first_title_activity:
                        mIntent.setClass(BilliardNearbyActivity.this, PlayMainActivity.class);
                        startActivity(mIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;
                    case R.id.first_title_group:
                        mIntent.setClass(BilliardNearbyActivity.this, BilliardGroupActivity.class);
                        startActivity(mIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;
                }

            }
        });
        setupTabs();
        initDrawer();

        IntentFilter filter = new IntentFilter();
        filter.addAction(PublicConstant.SLIDE_PART_IN_ACTION);
        filter.addAction(PublicConstant.SLIDE_FAVOR_ACTION);
        filter.addAction(PublicConstant.SLIDE_PUBLISH_ACTION);
        filter.addAction(PublicConstant.SLIDE_ACCOUNT_ACTION);
        registerReceiver(mReceiver,filter);


    }

    @Override
    protected void onResume()
    {
        super.onResume();
//        setupTabs();
//        initDrawer();
        mNearbyRadio.setChecked(true);
        mViewPager.setCurrentItem(sPagerPos);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        //mActionBar.removeAllTabs();
        sPagerPos = mViewPager.getCurrentItem();
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, " the onDestroy has been called ");
        super.onDestroy();
        mContext = null;
        sPagerPos = mViewPager.getCurrentItem();
        unregisterReceiver(mReceiver);
    }

    private void setupTabs()
    {
        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setIcon(R.drawable.slide_menu_icon);
        mActionBar.setTitle(getString(R.string.app_name));

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
        for (i = 0; i < count; i++) {
            tab = mActionBar.newTab()
                    .setText(mPagerAdapter.getPageTitle(i))
                    .setTabListener(this);
            mActionBar.addTab(tab);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft)
    {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft)
    {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft)
    {

    }

    private static final String TAG_MATE_FRAGMENT = "searchMateFragment";
    private static final String TAG_DATING_FRAGMENT = "searchDatingFragment";
    private static final String TAG_ASSISTCOAUCH_FRAGMENT = "searchAssistCoauchFragment";
    private static final String TAG_COAUCH_FRAGMENT = "searchCoauchFragment";
    private static final String TAG_ROOM_FRAGMENT = "searchRoomFragment";


    private class SectionPagerAdapter extends FragmentPagerAdapter
    {
        public SectionPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int index)
        {
            Log.d(TAG, " the current Fragment index are : " + index);
            Fragment fragment;
            Bundle args;

            switch (index) {
                case 0:
                    fragment = BilliardsNearbyMateFragment.newInstance(mContext, "MateFragment");
                    args = new Bundle();
                    args.putString(NearbySubFragmentConstants.MATE_FRAGMENT_INIT, mTitles[index]);
                    fragment.setArguments(args);
                    Log.d(TAG, "mate fragment has been created ");

                    break;
                case 1:
                    fragment = BilliardsNearbyDatingFragment.newInstance(mContext, "DatingFragment");
                    args = new Bundle();
                    args.putString(NearbySubFragmentConstants.DATING_FRAGMENT_INIT, mTitles[index]);
                    fragment.setArguments(args);
                    Log.d(TAG, "dating fragment has been created ");

                    break;
                case 2:
                    fragment = BilliardsNearbyAssistCoauchFragment.newInstance(mContext, "AssistCoauchFragment");
                    args = new Bundle();
                    args.putString(NearbySubFragmentConstants.ASSIST_COAUCH_FRAGMENT_INIT, mTitles[index]);
                    fragment.setArguments(args);
                    Log.d(TAG, "assist coauch fragment has been created ");

                    break;
                case 3:
                    fragment = BilliardsNearbyCoachFragment.newInstance(mContext, "CoauchFragment");
                    args = new Bundle();
                    args.putString(NearbySubFragmentConstants.COAUCH_FRAGMENT_INIT, mTitles[index]);
                    fragment.setArguments(args);
                    Log.d(TAG, "coauch fragment has been created ");

                    break;
                case 4:
                    fragment = BilliardsNearbyRoomFragment.newInstance(mContext, "RoomFragment");
                    args = new Bundle();
                    args.putString(NearbySubFragmentConstants.ROOM_FRAGMENT_INIT, mTitles[index]);
                    fragment.setArguments(args);
                    Log.d(TAG, "room fragment has been created ");

                    break;
                default:
                    fragment = BilliardsNearbyMateFragment.newInstance(mContext, "MateFragment");
                    args = new Bundle();
                    args.putString(NearbySubFragmentConstants.MATE_FRAGMENT_INIT, mTitles[index]);
                    fragment.setArguments(args);
                    Log.d(TAG, "mate fragment has been created ");
                    break;
            }


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
        getMenuInflater().inflate(R.menu.billiard_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        //ToDo:不起作用，得重新找方法

        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) searchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.LTGRAY);

        // TODO: 以下的设置是为了改变SearchView底部的bottom line的颜色的
        // TODO: 按StackOverflow上面的教程时可以实现我们的需求的，但是需要美工提供的EditText的bottom Line的
        // TODO: 图片才可以完成，现在还是不可以完成的,仅仅是测试
        // 得到'search_plate'的background
//        View searchPlate = searchView.findViewById(searchSrcTextId);
//        searchPlate.setBackgroundResource(R.drawable.edit_test);

        searchView.setIconifiedByDefault(false);
        try {
            Field searchField = SearchView.class.getDeclaredField("mSearchHintIcon");
            searchField.setAccessible(true);
            ImageView searchHintIcon = (ImageView) searchField.get(searchView);
            searchHintIcon.setImageResource(R.drawable.search);
        } catch (NoSuchFieldException e)
        {
            Log.d(TAG, " Exception happened while we retrieving the mSearchHintIcon, and the reason goes to : " + e.toString());
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            Log.d(TAG, " Exception happened as we have no right to access this filed, and the reason goes to : " + e.toString());
            e.printStackTrace();
        } catch (final Exception e)
        {
            Log.d(TAG, " exception happened while we make the search button : " + e.toString());
        }


//
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mMenuDrawer.toggleMenu();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
    }

    private void initDrawer()
    {

        //mItemList.clear();
        SlideAccountItemISlide accountItem = new SlideAccountItemISlide(YueQiuApp.sUserInfo.getImg_url(), YueQiuApp.sUserInfo.getUsername(),
                100, YueQiuApp.sUserInfo.getTitle(),YueQiuApp.sUserInfo.getUser_id());
        mItemList.add(accountItem);

        String[] values = new String[]{
                getString(R.string.search_my_profile_str),
                getString(R.string.search_my_participation_str),
                getString(R.string.search_my_favor_collection_str),
                getString(R.string.search_my_published_info_str),
                getString(R.string.search_publishing_dating_billiards_info_str),
                getString(R.string.search_feed_back_str),
                getString(R.string.search_logout_str)
        };
        final String[] spf_values = new String[]{
                "profile",
                "part_in",
                "favor",
                "publish",
                "play",
                "feedback",
                "logout"
        };
        int[] resIds = new int[]{
                R.drawable.more_my_information,
                R.drawable.more_my_part_in,
                R.drawable.more_my_collection,
                R.drawable.more_my_issue,
                R.drawable.more_publish_ball,
                R.drawable.more_feed_back,
                R.drawable.more_exit
        };

        SlideOtherItemISlide otherItem;
        for (int i = 0; i < values.length; i++) {
            boolean hasMsg = mSharedPreferences.getBoolean(spf_values[i],false);
            otherItem = new SlideOtherItemISlide(resIds[i], values[i], hasMsg);
            mItemList.add(otherItem);
        }

        mAdapter = new SlideViewAdapter(this, mItemList);

        mMenuList = (ListView) findViewById(R.id.menu_drawer_list);
        mMenuList.setAdapter(mAdapter);

        mMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent();
                switch (position) {
                    case 0:
                        int user_id = YueQiuApp.sUserInfo.getUser_id();
                        if(user_id < 1) {
                            intent.setClass(BilliardNearbyActivity.this, LoginActivity.class);
                        }else{
                            intent.setClass(BilliardNearbyActivity.this,MyProfileActivity.class);
                        }
                        startActivity(intent);
                        overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
                        break;
                    case 1:
                        if(checkUserId()) {
                            intent.setClass(BilliardNearbyActivity.this, MyProfileActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_bottom_in,R.anim.push_bottom_out);
                        }else{
                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:
                        if(checkUserId()) {
                            intent.setClass(BilliardNearbyActivity.this, MyParticipationActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_bottom_in,R.anim.push_bottom_out);
                            SlideOtherItemISlide partinItem = (SlideOtherItemISlide) mItemList.get(1);
                            if(partinItem.hasMsg()){
                                partinItem.setHasMsg(false);
                                mAdapter.notifyDataSetChanged();
                                mEditor.putBoolean(spf_values[1],false);
                                mEditor.apply();
                            }
                        }else{
                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3:
                        if(checkUserId()) {
                            intent.setClass(BilliardNearbyActivity.this, FavorActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_bottom_in,R.anim.push_bottom_out);
                            SlideOtherItemISlide favorItem = (SlideOtherItemISlide) mItemList.get(3);
                            if(favorItem.hasMsg()){
                                favorItem.setHasMsg(false);
                                mAdapter.notifyDataSetChanged();
                                mEditor.putBoolean(spf_values[2],false);
                                mEditor.apply();
                            }
                        }else{
                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 4:
                        if(checkUserId()) {
                            intent.setClass(BilliardNearbyActivity.this, PublishedInfoActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_bottom_in,R.anim.push_bottom_out);
                            SlideOtherItemISlide publishedItem = (SlideOtherItemISlide) mItemList.get(4);
                            if(publishedItem.hasMsg()) {
                                publishedItem.setHasMsg(false);
                                mAdapter.notifyDataSetChanged();
                                mEditor.putBoolean(spf_values[3],false);
                                mEditor.apply();
                            }
                        }else{
                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 5:
                        if(checkUserId()) {
                            intent.setClass(BilliardNearbyActivity.this, PlayIssueActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_bottom_in,R.anim.push_bottom_out);
                        }else{
                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 6:
                        if(checkUserId()) {
                            intent.setClass(BilliardNearbyActivity.this, FeedbackActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_bottom_in,R.anim.push_bottom_out);
                        }else{
                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 7:
                        if (Utils.networkAvaiable(BilliardNearbyActivity.this)) {
                            new Thread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    logout();
                                }
                            }).start();
                        } else {
                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }

                final int drawerState = mMenuDrawer.getDrawerState();
                if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
                    mMenuDrawer.closeMenu();
                    return;
                }

            }
        });
    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case LOGOUT_SUCCESS:
                    resetUSerInfo();
                    Toast.makeText(BilliardNearbyActivity.this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
                    break;
                case LOGOUT_FAILED:
                    Toast.makeText(BilliardNearbyActivity.this, getString(R.string.logout_failed), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void logout()
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put(DatabaseConstant.UserTable.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        String result = HttpUtil.urlClient(HttpConstants.LogoutConstant.URL,
                map, HttpConstants.RequestMethod.GET);
        try {
            JSONObject resultJson = new JSONObject(result);
            int rtCode = resultJson.getInt("code");
            if (rtCode == HttpConstants.ResponseCode.NORMAL) {
                mHandler.sendEmptyMessage(LOGOUT_SUCCESS);
            } else {
                mHandler.sendEmptyMessage(LOGOUT_FAILED);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void resetUSerInfo()
    {
        YueQiuApp.sUserInfo.setImg_url("");
        YueQiuApp.sUserInfo.setUsername(getString(R.string.guest));
        YueQiuApp.sUserInfo.setUser_id(0);
        YueQiuApp.sUserInfo.setPhone("");

        mEditor.putString(DatabaseConstant.UserTable.USERNAME,getString(R.string.guest));
        mEditor.putString(DatabaseConstant.UserTable.USER_ID,"0");
        mEditor.putString(DatabaseConstant.UserTable.IMG_URL,"");
        mEditor.putString(DatabaseConstant.UserTable.PHONE,"");
        mEditor.apply();


        mItemList.remove(0);
        SlideAccountItemISlide accountItem = new SlideAccountItemISlide(YueQiuApp.sUserInfo.getImg_url(), YueQiuApp.sUserInfo.getUsername(),
                0, YueQiuApp.sUserInfo.getTitle(),YueQiuApp.sUserInfo.getUser_id());
        mItemList.add(0, accountItem);
        mAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onRestoreInstanceState(Bundle state)
    {
        super.onRestoreInstanceState(state);
        if (null != state)
        {
            mMenuDrawer.restoreState(state.getParcelable(STATE_MENUDRAWER));
            int pos = state.getInt(FRAGMENT_PAGER_LAST_POSITION, 0);
            mViewPager.setCurrentItem(pos);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_MENUDRAWER, mMenuDrawer.saveState());
        outState.putInt(FRAGMENT_PAGER_LAST_POSITION, mViewPager.getCurrentItem());

    }

    @Override
    public void onBackPressed()
    {
        final int drawerState = mMenuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mMenuDrawer.closeMenu();
            return;
        }
        super.onBackPressed();
    }
    private boolean checkUserId(){
        int user_id = YueQiuApp.sUserInfo.getUser_id();
        if(user_id < 1)
            return false;
        return true;

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(PublicConstant.SLIDE_PART_IN_ACTION)){
                SlideOtherItemISlide partInItem = (SlideOtherItemISlide) mItemList.get(2);
                partInItem.setHasMsg(true);
                mAdapter.notifyDataSetChanged();
                mEditor.putBoolean("part_in",true);
                mEditor.apply();
            }else if(action.equals(PublicConstant.SLIDE_FAVOR_ACTION)){
                SlideOtherItemISlide favorItem = (SlideOtherItemISlide) mItemList.get(3);
                favorItem.setHasMsg(true);
                mAdapter.notifyDataSetChanged();
                mEditor.putBoolean("favor",true);
                mEditor.apply();
            }else if(action.equals(PublicConstant.SLIDE_PUBLISH_ACTION)){
                SlideOtherItemISlide publishItem = (SlideOtherItemISlide) mItemList.get(4);
                publishItem.setHasMsg(true);
                mAdapter.notifyDataSetChanged();
                mEditor.putBoolean("publish",true);
                mEditor.apply();
            }else if(action.equals(PublicConstant.SLIDE_ACCOUNT_ACTION)){
                SlideAccountItemISlide accountItemISlide = (SlideAccountItemISlide) mItemList.get(0);
                accountItemISlide.setName(YueQiuApp.sUserInfo.getUsername());
                accountItemISlide.setUserId(YueQiuApp.sUserInfo.getUser_id());
                accountItemISlide.setTitle(YueQiuApp.sUserInfo.getTitle());
                accountItemISlide.setImg(YueQiuApp.sUserInfo.getImg_url());
                mAdapter.notifyDataSetChanged();
            }
        }
    };

}

























































