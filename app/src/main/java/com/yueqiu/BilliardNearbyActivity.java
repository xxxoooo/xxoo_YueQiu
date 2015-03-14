package com.yueqiu;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeChatTarget;
import com.gotye.api.GotyeMessage;
import com.gotye.api.GotyeNotify;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.GotyeUser;
import com.gotye.api.listener.ChatListener;
import com.gotye.api.listener.LoginListener;
import com.gotye.api.listener.NotifyListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.activity.DateIssueActivity;
import com.yueqiu.activity.FavorActivity;
import com.yueqiu.activity.SearchResultActivity;
import com.yueqiu.activity.PlayIssueActivity;
import com.yueqiu.activity.FeedbackActivity;
import com.yueqiu.activity.LoginActivity;
import com.yueqiu.activity.MyParticipationActivity;
import com.yueqiu.activity.MyProfileActivity;
import com.yueqiu.activity.PublishedInfoActivity;
import com.yueqiu.adapter.SlideViewAdapter;
import com.yueqiu.fragment.nearby.common.NearbyParamsPreference;
import com.yueqiu.im.GotyeService;
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
import com.yueqiu.util.AppUtil;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.menudrawer.MenuDrawer;
import com.yueqiu.view.menudrawer.Position;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 首页的SearchActivity
 */
public class BilliardNearbyActivity extends FragmentActivity implements ActionBar.TabListener, LoginListener ,NotifyListener,ChatListener{
    private static final String TAG = "BilliardNearbyActivity";

    private static final String STATE_MENUDRAWER = "com.yueqiu.menuDrawer";
    private static final int NUM_OF_FRAGMENTS = 5;

    private static final String FRAGMENT_PAGER_LAST_POSITION = "fragmentPagerLastPosition";
    private static final String CURRENT_KEY = "nearby_current_item";

    private static final int NEARBY = 1;
    private static final int CHATBAR = 2;
    private static final int PLAY = 3;
    private static final int GROUP = 4;

    private ViewPager mViewPager;
    private String[] mTitles;
    private SectionPagerAdapter mPagerAdapter;
    private ActionBar mActionBar;
    private Context mContext;
    private RadioGroup mGroup;
    private RadioButton mNearbyRadio,mChatRadio;
    private Intent mIntent = new Intent();
    private MenuDrawer mMenuDrawer;
    private SlideViewAdapter mAdapter;
    private ListView mMenuList;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private List<ISlideListItem> mItemList = new ArrayList<ISlideListItem>();

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private RelativeLayout mNearby,mChatBar,mPlay,mGroupRe;
    private TextView mNotifyView;


    private SearchView mSearchView;

    // 这个变量用于保存每次当SearchActivity被切换到别的地方的时候，回来的时候，还能确保我们回到最后一次滑动到的Fragment的position
    private static int sPagerPos = 0;
    private int mType;
    private NearbyParamsPreference mPreference = NearbyParamsPreference.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, " the onCreate has been called ");
        super.onCreate(savedInstanceState);
        mPreference.setFirstEnterTag(this, false);

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();

        mContext = this;

        mSharedPreferences = getSharedPreferences(PublicConstant.USERBASEUSER, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mActionBar = getActionBar();
        mTitles = new String[]{getString(R.string.nearby_billiard_mate_str),
                getString(R.string.nearby_billiard_dating_str),
                getString(R.string.nearby_billiard_assist_coauch_str),
                getString(R.string.nearby_billiard_coauch_str),
                getString(R.string.nearby_billiard_room_str)};
        mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.BEHIND, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
        mMenuDrawer.setContentView(R.layout.activity_nearby_billiard);
        mMenuDrawer.setMenuView(R.layout.slide_drawer_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mViewPager = (ViewPager) findViewById(R.id.search_parent_fragment_view_pager);

        mGroup = (RadioGroup) findViewById(R.id.search_parent_radio_group);
//        mNearbyRadio = (RadioButton) findViewById(R.id.first_title_nearby);
//        mChatRadio = (RadioButton) findViewById(R.id.first_title_chatbar);

        mNearby = (RelativeLayout) findViewById(R.id.nearby_bottom_re);
        mChatBar = (RelativeLayout) findViewById(R.id.nearby_chat_bar_re);
        mPlay = (RelativeLayout) findViewById(R.id.nearby_play_re);
        mGroupRe = (RelativeLayout) findViewById(R.id.nearby_group_re);
        mNotifyView = (TextView) findViewById(R.id.nearby_chat_bar_unread_count);

//        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                switch (checkedId) {
//                    case R.id.first_title_nearby:
//                        break;
//                    case R.id.first_title_chatbar:
//                        if (checkUserId()) {
//                            mIntent.setClass(BilliardNearbyActivity.this, ChatBarActivity.class);
//                        } else {
//                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
//                            mIntent.setClass(BilliardNearbyActivity.this, LoginActivity.class);
//                        }
//                        startActivity(mIntent);
//                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
//                        break;
//                    case R.id.first_title_activity:
//                        mIntent.setClass(BilliardNearbyActivity.this, PlayMainActivity.class);
//                        startActivity(mIntent);
//                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
//                        break;
//                    case R.id.first_title_group:
//                        mIntent.setClass(BilliardNearbyActivity.this, BilliardGroupActivity.class);
//                        startActivity(mIntent);
//                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
//                        break;
//                }
//
//            }
//        });

        mNearby.setOnClickListener(mClickListener);
        mChatBar.setOnClickListener(mClickListener);
        mPlay.setOnClickListener(mClickListener);
        mGroupRe.setOnClickListener(mClickListener);

        setupTabs();
        initDrawer();

        ViewTreeObserver observer = mGroup.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                YueQiuApp.sBottomHeight = mGroup.getHeight();
                Log.d("wy", "bottom height ->" + YueQiuApp.sBottomHeight);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(PublicConstant.SLIDE_PART_IN_ACTION);
        filter.addAction(PublicConstant.SLIDE_FAVOR_ACTION);
        filter.addAction(PublicConstant.SLIDE_PUBLISH_ACTION);
        filter.addAction(PublicConstant.SLIDE_ACCOUNT_ACTION);
        registerReceiver(mReceiver, filter);


    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.nearby_bottom_re:
                    break;
                case R.id.nearby_chat_bar_re:
                    setBottomBackgroud(mChatBar,mNearby,mPlay,mGroupRe);
                    if(mNotifyView.getVisibility() == View.VISIBLE){
                        mNotifyView.setVisibility(View.GONE);
                    }
                    if (checkUserId()) {
                        mIntent.setClass(BilliardNearbyActivity.this, ChatBarActivity.class);
                    } else {
                        Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                        mIntent.setClass(BilliardNearbyActivity.this, LoginActivity.class);
                    }
                    startActivity(mIntent);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    break;
                case R.id.nearby_play_re:
                    setBottomBackgroud(mPlay,mNearby,mChatBar,mGroupRe);
                    mIntent.setClass(BilliardNearbyActivity.this, PlayMainActivity.class);
                    startActivity(mIntent);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    break;
                case R.id.nearby_group_re:
                    setBottomBackgroud(mGroupRe,mNearby,mChatBar,mPlay);
                    mIntent.setClass(BilliardNearbyActivity.this, BilliardGroupActivity.class);
                    startActivity(mIntent);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
//        mNearbyRadio.setChecked(true);
        mType = NEARBY;
        setBottomBackgroud(mNearby,mChatBar,mPlay,mGroupRe);
        getBgColor();
        mViewPager.setCurrentItem(sPagerPos);
        if (checkUserId()) {
            //登录IM
            GotyeAPI.getInstance().addListener(this);
            login();
        } else {
            Log.e(TAG, "IM已经退出了");
            resetUserInfo();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //mActionBar.removeAllTabs();
        sPagerPos = mViewPager.getCurrentItem();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, " the onDestroy has been called ");
        super.onDestroy();
        mContext = null;
        sPagerPos = mViewPager.getCurrentItem();
        unregisterReceiver(mReceiver);
    }

    private void setupTabs() {
        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setIcon(R.drawable.slide_menu_icon);
        mActionBar.setTitle(getString(R.string.app_name));

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
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
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    private static final String TAG_MATE_FRAGMENT = "searchMateFragment";
    private static final String TAG_DATING_FRAGMENT = "searchDatingFragment";
    private static final String TAG_ASSISTCOAUCH_FRAGMENT = "searchAssistCoauchFragment";
    private static final String TAG_COAUCH_FRAGMENT = "searchCoauchFragment";
    private static final String TAG_ROOM_FRAGMENT = "searchRoomFragment";

    /**
     * 登录IM
     */
    private void login() {
        int loginState = GotyeAPI.getInstance().getOnLineState();
        Log.e(TAG, "login -> login state -> " + loginState);
        if (loginState == 0) {
            GotyeAPI.getInstance().login(YueQiuApp.sUserInfo.getPhone(), null);
        }
    }

    /**
     * IM 登出
     *
     * @param code 状态码 参见 {@link com.gotye.api.GotyeStatusCode}
     */
    @Override
    public void onLogout(int code) {

//        GotyeAPI.getInstance().logout();
////        if (code == GotyeStatusCode.CODE_FORCELOGOUT) {
////            Toast.makeText(this, getString(R.string.im_login_other_device), Toast.LENGTH_SHORT).show();
////        } else if (code == GotyeStatusCode.CODE_NETWORD_DISCONNECTED) {
////            Toast.makeText(this, getString(R.string.im_user_offline), Toast.LENGTH_SHORT).show();
////        }
//        if (Utils.networkAvaiable(BilliardNearbyActivity.this)) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    logout();
//                }
//            }).start();
//        } else {
//            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
//        }

    }

    /**
     * IM 登录
     *
     * @param code             状态码 参见 {@link com.gotye.api.GotyeStatusCode}
     * @param currentLoginUser 当前登录用户
     */
    @Override
    public void onLogin(int code, GotyeUser currentLoginUser) {
        Log.e(TAG, ">>>>>>>>>>>>>>>>>Billiardnearbayactivity------onLogin<<<<<<<<<<<<<<<<<<");
        // 判断登陆是否成功
        if (code == GotyeStatusCode.CODE_OK
                || code == GotyeStatusCode.CODE_OFFLINELOGIN_SUCCESS
                || code == GotyeStatusCode.CODE_RELOGIN_SUCCESS) {
            //由于掉线YueQiuApp类中的监听器会被迫remove掉，这里登录成功在注册一次
            ((YueQiuApp) YueQiuApp.getAppContext()).registerListener();

            if (code == GotyeStatusCode.CODE_OFFLINELOGIN_SUCCESS) {
//                Toast.makeText(this, "您当前处于离线状态", Toast.LENGTH_SHORT).show();
            } else if (code == GotyeStatusCode.CODE_OK) {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                Intent toService = new Intent(this, GotyeService.class);
                startService(toService);
            }
        } else {
            // 失败,可根据code定位失败原因
            Log.d(TAG, "IM登录失败");
            Toast.makeText(this, "登录失败 code=" + code, Toast.LENGTH_SHORT).show();
        }


    }




    private class SectionPagerAdapter extends FragmentPagerAdapter {
        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {
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
        public int getCount() {
            return NUM_OF_FRAGMENTS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.billiard_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        //ToDo:不起作用，得重新找方法

        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) mSearchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.LTGRAY);

        // TODO: 以下的设置是为了改变SearchView底部的bottom line的颜色的
        // TODO: 按StackOverflow上面的教程时可以实现我们的需求的，但是需要美工提供的EditText的bottom Line的
        // TODO: 图片才可以完成，现在还是不可以完成的,仅仅是测试
        // 得到'search_plate'的background
//        View searchPlate = searchView.findViewById(searchSrcTextId);
//        searchPlate.setBackgroundResource(R.drawable.edit_test);

        mSearchView.setIconifiedByDefault(false);
        try {
            Field searchField = SearchView.class.getDeclaredField("mSearchHintIcon");
            searchField.setAccessible(true);
            ImageView searchHintIcon = (ImageView) searchField.get(mSearchView);
            searchHintIcon.setImageResource(R.drawable.search);
        } catch (NoSuchFieldException e) {
            Log.d(TAG, " Exception happened while we retrieving the mSearchHintIcon, and the reason goes to : " + e.toString());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.d(TAG, " Exception happened as we have no right to access this filed, and the reason goes to : " + e.toString());
            e.printStackTrace();
        } catch (final Exception e) {
            Log.d(TAG, " exception happened while we make the search button : " + e.toString());
        }

//        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mMenuDrawer.toggleMenu();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void initDrawer() {

        //mItemList.clear();
        Log.d("wy", "YueQiu img_url ->" + YueQiuApp.sUserInfo.getImg_url());
        SlideAccountItemISlide accountItem = new SlideAccountItemISlide(YueQiuApp.sUserInfo.getImg_url(), YueQiuApp.sUserInfo.getUsername(),
                100, YueQiuApp.sUserInfo.getTitle(), YueQiuApp.sUserInfo.getUser_id());
        mItemList.add(accountItem);

        String[] values = new String[]{
                getString(R.string.search_my_profile_str),
                getString(R.string.search_my_participation_str),
                getString(R.string.search_my_favor_collection_str),
                getString(R.string.search_my_published_info_str),
                getString(R.string.issue_date),
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
            boolean hasMsg = mSharedPreferences.getBoolean(spf_values[i], false);
            otherItem = new SlideOtherItemISlide(resIds[i], values[i], hasMsg);
            mItemList.add(otherItem);
        }

        mAdapter = new SlideViewAdapter(this, mItemList);

        mMenuList = (ListView) findViewById(R.id.menu_drawer_list);
        mMenuList.setAdapter(mAdapter);

        mMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                switch (position) {
                    case 0:
                        int user_id = YueQiuApp.sUserInfo.getUser_id();
                        if (user_id < 1) {
                            intent.setClass(BilliardNearbyActivity.this, LoginActivity.class);
                        } else {
                            intent.setClass(BilliardNearbyActivity.this, MyProfileActivity.class);
                        }
                        startActivity(intent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;
                    case 1:
                        if (checkUserId()) {
                            intent.setClass(BilliardNearbyActivity.this, MyProfileActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
                        } else {
                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:
                        if (checkUserId()) {
                            intent.setClass(BilliardNearbyActivity.this, MyParticipationActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
                            SlideOtherItemISlide partinItem = (SlideOtherItemISlide) mItemList.get(2);
                            if (partinItem.hasMsg()) {
                                partinItem.setHasMsg(false);
                                mAdapter.notifyDataSetChanged();
                                mEditor.putBoolean(spf_values[1], false);
                                mEditor.apply();
                            }
                        } else {
                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3:
                        if (checkUserId()) {
                            intent.setClass(BilliardNearbyActivity.this, FavorActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
                            SlideOtherItemISlide favorItem = (SlideOtherItemISlide) mItemList.get(3);
                            if (favorItem.hasMsg()) {
                                favorItem.setHasMsg(false);
                                mAdapter.notifyDataSetChanged();
                                mEditor.putBoolean(spf_values[2], false);
                                mEditor.apply();
                            }
                        } else {
                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 4:
                        if (checkUserId()) {
                            intent.setClass(BilliardNearbyActivity.this, PublishedInfoActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
                            SlideOtherItemISlide publishedItem = (SlideOtherItemISlide) mItemList.get(4);
                            if (publishedItem.hasMsg()) {
                                publishedItem.setHasMsg(false);
                                mAdapter.notifyDataSetChanged();
                                mEditor.putBoolean(spf_values[3], false);
                                mEditor.apply();
                            }
                        } else {
                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 5:
                        if (checkUserId()) {
                            //TODO:不是PlayIssueActivity
                            intent.setClass(BilliardNearbyActivity.this, DateIssueActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
                        } else {
                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 6:
                        if (checkUserId()) {
                            intent.setClass(BilliardNearbyActivity.this, FeedbackActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
                        } else {
                            Toast.makeText(BilliardNearbyActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 7:
                        if (!checkUserId())
                            return;
                        if (Utils.networkAvaiable(BilliardNearbyActivity.this)) {
                           logout();
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

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case YueQiuApp.LOGOUT_SUCCESS:
                    resetUserInfo();
                    GotyeAPI.getInstance().logout();
                    Toast.makeText(BilliardNearbyActivity.this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
                    break;
                case YueQiuApp.LOGOUT_FAILED:
                    Toast.makeText(BilliardNearbyActivity.this, getString(R.string.logout_failed), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void logout() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(DatabaseConstant.UserTable.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        HttpUtil.requestHttp(HttpConstants.LogoutConstant.URL,
                map, HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            int rtCode = response.getInt("code");
                            if (rtCode == HttpConstants.ResponseCode.NORMAL) {
                                mHandler.sendEmptyMessage(YueQiuApp.LOGOUT_SUCCESS);
                            } else {
                                mHandler.sendEmptyMessage(YueQiuApp.LOGOUT_FAILED);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                    }
                });
    }

    private void resetUserInfo() {
        YueQiuApp.sUserInfo.setImg_url("");
        YueQiuApp.sUserInfo.setUsername(getString(R.string.guest));
        YueQiuApp.sUserInfo.setUser_id(0);
        YueQiuApp.sUserInfo.setPhone("");
        YueQiuApp.sUserInfo.setNick("");
        YueQiuApp.sUserInfo.setDistrict("");
        YueQiuApp.sUserInfo.setLevel(-1);
        YueQiuApp.sUserInfo.setBall_type(-1);
        YueQiuApp.sUserInfo.setBallArm(-1);
        YueQiuApp.sUserInfo.setUsedType(-1);
        YueQiuApp.sUserInfo.setBallAge("");
        YueQiuApp.sUserInfo.setIdol("");
        YueQiuApp.sUserInfo.setIdol_name("");
        YueQiuApp.sUserInfo.setCost("");
        YueQiuApp.sUserInfo.setMy_type(1);
        YueQiuApp.sUserInfo.setWork_live("");
        YueQiuApp.sUserInfo.setZizhi(4);

        mEditor.putString(DatabaseConstant.UserTable.USERNAME, getString(R.string.guest));
        mEditor.putString(DatabaseConstant.UserTable.IMG_URL, "");
        mEditor.putInt(DatabaseConstant.UserTable.SEX, -1);
        mEditor.putString(DatabaseConstant.UserTable.USER_ID, "0");
        mEditor.putString(DatabaseConstant.UserTable.IMG_URL, "");
        mEditor.putString(DatabaseConstant.UserTable.NICK, "");
        mEditor.putString(DatabaseConstant.UserTable.PHONE, "");
        mEditor.putString(DatabaseConstant.UserTable.DISTRICT,"");
        mEditor.putInt(DatabaseConstant.UserTable.LEVEL,-1);
        mEditor.putInt(DatabaseConstant.UserTable.BALL_TYPE,-1);
        mEditor.putInt(DatabaseConstant.UserTable.BALLARM,-1);
        mEditor.putInt(DatabaseConstant.UserTable.USERDTYPE,-1);
        mEditor.putString(DatabaseConstant.UserTable.BALLAGE,"");
        mEditor.putString(DatabaseConstant.UserTable.IDOL,"");
        mEditor.putString(DatabaseConstant.UserTable.IDOL_NAME,"");
        mEditor.putString(DatabaseConstant.UserTable.COST,"");
        mEditor.putInt(DatabaseConstant.UserTable.MY_TYPE,1);
        mEditor.putString(DatabaseConstant.UserTable.WORK_LIVE,"");
        mEditor.putInt(DatabaseConstant.UserTable.ZIZHI,4);
        mEditor.apply();


        mItemList.remove(0);
        SlideAccountItemISlide accountItem = new SlideAccountItemISlide(YueQiuApp.sUserInfo.getImg_url(), YueQiuApp.sUserInfo.getUsername(),
                0, YueQiuApp.sUserInfo.getTitle(), YueQiuApp.sUserInfo.getUser_id());
        mItemList.add(0, accountItem);
        mAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (null != state) {
            mMenuDrawer.restoreState(state.getParcelable(STATE_MENUDRAWER));
            int pos = state.getInt(FRAGMENT_PAGER_LAST_POSITION, 0);
            mViewPager.setCurrentItem(pos);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_MENUDRAWER, mMenuDrawer.saveState());
        outState.putInt(FRAGMENT_PAGER_LAST_POSITION, mViewPager.getCurrentItem());

    }

    @Override
    public void onBackPressed() {
        final int drawerState = mMenuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mMenuDrawer.closeMenu();
            return;
        }
        super.onBackPressed();
    }

    private boolean checkUserId() {
        int user_id = YueQiuApp.sUserInfo.getUser_id();
        if (user_id < 1)
            return false;
        return true;

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(PublicConstant.SLIDE_PART_IN_ACTION)) {
                SlideOtherItemISlide partInItem = (SlideOtherItemISlide) mItemList.get(2);
                partInItem.setHasMsg(true);
                mAdapter.notifyDataSetChanged();
                mEditor.putBoolean("part_in", true);
                mEditor.apply();
            } else if (action.equals(PublicConstant.SLIDE_FAVOR_ACTION)) {
                SlideOtherItemISlide favorItem = (SlideOtherItemISlide) mItemList.get(3);
                favorItem.setHasMsg(true);
                mAdapter.notifyDataSetChanged();
                mEditor.putBoolean("favor", true);
                mEditor.apply();
            } else if (action.equals(PublicConstant.SLIDE_PUBLISH_ACTION)) {
                SlideOtherItemISlide publishItem = (SlideOtherItemISlide) mItemList.get(4);
                publishItem.setHasMsg(true);
                mAdapter.notifyDataSetChanged();
                mEditor.putBoolean("publish", true);
                mEditor.apply();
            } else if (action.equals(PublicConstant.SLIDE_ACCOUNT_ACTION)) {
                SlideAccountItemISlide accountItemISlide = (SlideAccountItemISlide) mItemList.get(0);
                accountItemISlide.setName(YueQiuApp.sUserInfo.getUsername());
                accountItemISlide.setUserId(YueQiuApp.sUserInfo.getUser_id());
                accountItemISlide.setTitle(YueQiuApp.sUserInfo.getTitle());
                accountItemISlide.setImg(YueQiuApp.sUserInfo.getImg_url());
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onReconnecting(int i, GotyeUser gotyeUser) {
        if (i == 0) {
            Log.d(TAG, "current user " + gotyeUser.getName() + " ReLogin success by network available!!");
        }
    }

    @Override
    public void onReceiveMessage(int i, GotyeMessage gotyeMessage) {
        if (mNotifyView != null){
            String currentActivityName = AppUtil.getCurrentActivityName(getBaseContext());
            if (currentActivityName.equals("com.yueqiu.im.ChatPage") ||
                    currentActivityName.equals("com.yueqiu.ChatBarActivity")) {
                return;
            }
            mNotifyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDownloadMessage(int i, GotyeMessage gotyeMessage) {

    }

    @Override
    public void onReleaseMessage(int i) {

    }

    @Override
    public void onReport(int i, GotyeMessage gotyeMessage) {

    }

    @Override
    public void onStartTalk(int i, boolean b, int i2, GotyeChatTarget gotyeChatTarget) {

    }

    @Override
    public void onStopTalk(int i, GotyeMessage gotyeMessage, boolean b) {

    }

    @Override
    public void onDecodeMessage(int i, GotyeMessage gotyeMessage) {

    }

    @Override
    public void onGetMessageList(int i, List<GotyeMessage> gotyeMessages) {

    }

    @Override
    public void onOutputAudioData(byte[] bytes) {

    }

    @Override
    public void onGetCustomerService(int i, GotyeUser gotyeUser, int i2, String s) {

    }

    @Override
    public void onReceiveMessage(int i, GotyeMessage gotyeMessage, boolean b) {

    }

    @Override
    public void onSendMessage(int i, GotyeMessage gotyeMessage) {

    }

    @Override
    public void onReceiveNotify(int i, GotyeNotify gotyeNotify) {

    }

    @Override
    public void onRemoveFriend(int i, GotyeUser gotyeUser) {

    }

    @Override
    public void onAddFriend(int i, GotyeUser gotyeUser) {

    }

    @Override
    public void onNotifyStateChanged() {

    }

    private void setBottomBackgroud(RelativeLayout greenView, RelativeLayout blackView1, RelativeLayout blackView2,RelativeLayout blackview3) {
        greenView.setBackgroundColor(getResources().getColor(R.color.actionbar_color));
        blackView1.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
        blackView2.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
        blackview3.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
    }

    private void getBgColor(){
        if(mType == NEARBY){
            mNearby.setBackgroundColor(getResources().getColor(R.color.actionbar_color));
            mChatBar.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
            mPlay.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
            mGroupRe.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
        }else if(mType == CHATBAR){
            mChatBar.setBackgroundColor(getResources().getColor(R.color.actionbar_color));
            mNearby.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
            mPlay.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
            mGroupRe.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
        }else if(mType == PLAY){
            mPlay.setBackgroundColor(getResources().getColor(R.color.actionbar_color));
            mNearby.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
            mChatBar.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
            mGroupRe.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
        }else if(mType == GROUP){
            mGroupRe.setBackgroundColor(getResources().getColor(R.color.actionbar_color));
            mNearby.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
            mChatBar.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
            mPlay.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
        }
    }

}

























































