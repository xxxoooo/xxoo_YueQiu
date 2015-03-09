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
import android.net.Uri;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;


import com.yueqiu.activity.SearchResultActivity;
import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeGroup;
import com.gotye.api.GotyeMessage;
import com.gotye.api.GotyeNotify;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.GotyeUser;
import com.gotye.api.PathUtil;
import com.gotye.api.listener.NotifyListener;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.chatbar.AddPersonFragment;
import com.yueqiu.fragment.chatbar.ContactFragment;
import com.yueqiu.fragment.chatbar.MessageFragment;
import com.yueqiu.util.BeepManager;
import com.yueqiu.util.BitmapUtil;
import com.yueqiu.util.FileUtil;
import com.yueqiu.util.Utils;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.Field;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧Activity
 */
public class ChatBarActivity extends FragmentActivity implements NotifyListener,View.OnClickListener, ContactFragment.FriendsListChanged {
    private static final String TAG = "ChatBarActivity";
    private ActionBar mActionBar;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private Fragment mCurrentFragment;
    private MessageFragment mMessageFragment = new MessageFragment();
    private ContactFragment mContactFragment = new ContactFragment();
    private AddPersonFragment mAddPersonFragment = new AddPersonFragment();
    private String mUserName = YueQiuApp.sUserInfo.getUsername();
    private String mPassword;//暂时不需要密码
    private LinearLayout mBottomContainer;
    private TextView mUnreadCountTv;
    private RelativeLayout mMsgView,mContactView,mAddPersonView;

    private BeepManager beep;
    private GotyeAPI api;
    private boolean returnNotify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = GotyeAPI.getInstance();
        api.addListerer(this);
        setContentView(R.layout.activity_chatbar_main);
        beep = new BeepManager(this);
        beep.updatePrefs();

        fragmentManager = getSupportFragmentManager();
        initView();

        IntentFilter filter = new IntentFilter();
        filter.addAction(PublicConstant.CHAT_HAS_NEW_MSG);
        filter.addAction(PublicConstant.CHAT_HAS_NO_MSG);
        registerReceiver(mReceiver,filter);

    }

    private void switchFragment(Fragment fragment) {
        if (mCurrentFragment == fragment)
            return;
        transaction = fragmentManager.beginTransaction();
        if (fragment.isAdded())
            transaction.hide(mCurrentFragment).show(fragment).commit();
        else
            transaction.hide(mCurrentFragment).add(R.id.chatbar_fragment_container, fragment).commit();
        mCurrentFragment = fragment;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActionBar();
            mActionBar.setTitle(getString(R.string.btn_liaoba_message));
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        getBgColor();
        returnNotify = false;
        mainRefresh();
        Log.e(TAG, "IM isOnline? " + GotyeAPI.getInstance().isOnline());
        if (!GotyeAPI.getInstance().isOnline()){
            Utils.showToast(this, getString(R.string.im_user_offline));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.billiard_search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.near_nemu_search).getActionView();

        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) searchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.LTGRAY);

        searchView.setIconifiedByDefault(false);
        try {
            Field searchField = SearchView.class.getDeclaredField("mSearchHintIcon");
            searchField.setAccessible(true);
            ImageView searchHintIcon = (ImageView) searchField.get(searchView);
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

        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
        return true;
    }

    private void initView() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,YueQiuApp.sBottomHeight);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mBottomContainer = (LinearLayout) findViewById(R.id.chat_bar_bottom_view);
        mBottomContainer.setWeightSum(3);
        mBottomContainer.setOrientation(LinearLayout.HORIZONTAL);
        mBottomContainer.setLayoutParams(params);

        mUnreadCountTv = (TextView) findViewById(R.id.chat_bar_unread_count);
        mUnreadCountTv.setVisibility(View.GONE);

        mMsgView = (RelativeLayout) findViewById(R.id.chat_bar_msg_re);
        mContactView = (RelativeLayout) findViewById(R.id.chat_bar_contact_re);
        mAddPersonView = (RelativeLayout) findViewById(R.id.chat_bar_add_re);
        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.chatbar_fragment_container, mMessageFragment);
        transaction.commit();
        mCurrentFragment = mMessageFragment;

        mMsgView.setOnClickListener(this);
        mContactView.setOnClickListener(this);
        mAddPersonView.setOnClickListener(this);

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

    // 更新提醒
    public void updateUnReadTip() {
        int unreadCount = api.getTotalUnreadMsgCount();
        int unreadNotifyCount = api.getUnreadNotifyCount();
        unreadCount += unreadNotifyCount;
//        msgTip.setVisibility(View.VISIBLE);
//        if (unreadCount > 0 && unreadCount < 100) {
//            msgTip.setText(String.valueOf(unreadCount));
//        } else if (unreadCount >= 100) {
//            msgTip.setText("99");
//        } else {
//            msgTip.setVisibility(View.GONE);
//        }
    }

    // 页面刷新
    private void mainRefresh() {
        updateUnReadTip();
        mMessageFragment.refresh();
//        if (mContactFragment != null) {
//            mContactFragment.refresh();
//        }

    }


    @Override
    protected void onPause() {
        returnNotify = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // 保持好习惯，销毁时请移除监听
        api.removeListener(this);
        // 告诉service已经处于后台运行状态
        // Intent toService=new Intent(this, GotyeService.class);
        // toService.setAction(GotyeService.ACTION_RUN_BACKGROUND);
        // startService(toService);
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }


    private static final String CONFIG = "chatbar_login_config";

    public void saveUser(String name, String password) {
        SharedPreferences sp = getSharedPreferences(CONFIG,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("username", name);
        edit.putString("password", password);
        edit.commit();
    }

    public static String[] getUser(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CONFIG,
                Context.MODE_PRIVATE);
        String name = sp.getString("username", null);
        String password = sp.getString("password", null);
        String[] user = new String[2];
        user[0] = name;
        user[1] = password;
        return user;
    }

    /**
     * 收到消息时的回调方法，这里该方法是主要是用来更新MessageFragment的界面
     *
     * @param code    状态码 参见 {@link GotyeStatusCode}
     * @param message 消息对象
     * @param unRead  是否已读
     */
    @Override
    public void onReceiveMessage(int code, GotyeMessage message, boolean unRead) {
        if (returnNotify) {
            return;
        }
        mMessageFragment.refresh();
        Log.d("wy", "onReceiveMessage");
        if (unRead) {
            updateUnReadTip();

            if (!api.isNewMsgNotify()) {
                return;
            }
            if (message.getReceiverType() == 2) {
                if (api.isNotReceiveGroupMsg()) {
                    return;
                }
                if (api.isGroupDontdisturb(((GotyeGroup) message.getReceiver())
                        .getGroupID())) {
                    return;
                }
            }
            beep.playBeepSoundAndVibrate();
        }
    }

    // 自己发送的信息统一在此处理
    @Override
    public void onSendMessage(int code, GotyeMessage message) {
        if (returnNotify) {
            return;
        }
        mMessageFragment.refresh();
    }

    // 收到群邀请信息
    @Override
    public void onReceiveNotify(int code, GotyeNotify notify) {
        if (returnNotify) {
            return;
        }
        mMessageFragment.refresh();
        updateUnReadTip();
        if (!api.isNotReceiveGroupMsg()) {
            beep.playBeepSoundAndVibrate();
        }
    }

    @Override
    public void onRemoveFriend(int code, GotyeUser user) {
        if (returnNotify) {
            return;
        }
        api.deleteSession(user);
        mMessageFragment.refresh();
//        contactsFragment.refresh();
    }

    @Override
    public void onAddFriend(int code, GotyeUser user) {
        if (returnNotify) {
            return;
        }
//        if (currentPosition == 1) {
//            contactsFragment.refresh();
//        }
    }

    @Override
    public void onNotifyStateChanged() {
        mainRefresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 选取图片的返回值
        if (resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    String path = FileUtil.uriToPath(this, selectedImage);
                    setPicture(path);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setPicture(String path) {
        File f = new File(PathUtil.getAppFIlePath());
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        File file = new File(PathUtil.getAppFIlePath()
                + System.currentTimeMillis() + "jpg");
        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Bitmap smaillBit = BitmapUtil.getSmallBitmap(path, 50, 50);
        String smallPath = BitmapUtil.saveBitmapFile(smaillBit);
//        settingFragment.modifyUserIcon(smallPath);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.chat_bar_msg_re:
                switchFragment(mMessageFragment);
                mActionBar.setTitle(R.string.btn_liaoba_message);
                setBottomBackgroud(mMsgView,mContactView,mAddPersonView);
                break;
            case R.id.chat_bar_contact_re:
                switchFragment(mContactFragment);
                mActionBar.setTitle(R.string.btn_liaoba_contact);
                setBottomBackgroud(mContactView,mMsgView,mAddPersonView);
                break;
            case R.id.chat_bar_add_re:
                switchFragment(mAddPersonFragment);
                 mActionBar.setTitle(R.string.btn_liaoba_add_friend);
                setBottomBackgroud(mAddPersonView,mMsgView,mContactView);
                break;
        }
    }

    private void setBottomBackgroud(RelativeLayout greenView,RelativeLayout blackView1,RelativeLayout blackView2){
        greenView.setBackgroundColor(getResources().getColor(R.color.actionbar_color));
        blackView1.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
        blackView2.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
    }

    private void getBgColor(){
        if(mCurrentFragment == mMessageFragment){
            mMsgView.setBackgroundColor(getResources().getColor(R.color.actionbar_color));
            mContactView.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
            mAddPersonView.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
        }else if(mCurrentFragment == mContactFragment){
            mContactView.setBackgroundColor(getResources().getColor(R.color.actionbar_color));
            mMsgView.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
            mAddPersonView.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
        }else if(mCurrentFragment == mAddPersonFragment){
            mAddPersonView.setBackgroundColor(getResources().getColor(R.color.actionbar_color));
            mContactView.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
            mMsgView.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(PublicConstant.CHAT_HAS_NEW_MSG)){
                mUnreadCountTv.setVisibility(View.VISIBLE);
            }else if(action.equals(PublicConstant.CHAT_HAS_NO_MSG)){
                mUnreadCountTv.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onFriendsListChanged() {
        Log.e("ddd", "更新消息列表头像");
        mMessageFragment.mAdapter.notifyDataSetChanged();

    }
}
