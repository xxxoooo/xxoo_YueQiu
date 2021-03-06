package com.yueqiu;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;


import com.gotye.api.GotyeChatTarget;
import com.gotye.api.listener.ChatListener;
import com.gotye.api.listener.LoginListener;
import com.yueqiu.activity.SearchResultActivity;
import com.gotye.api.GotyeAPI;
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
import com.yueqiu.im.GotyeService;
import com.yueqiu.util.BeepManager;
import com.yueqiu.util.BitmapUtil;
import com.yueqiu.util.FileUtil;
import com.yueqiu.util.Utils;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧Activity
 */
public class ChatBarActivity extends FragmentActivity implements NotifyListener,
        LoginListener, ChatListener, View.OnClickListener{
    private static final String TAG = "ChatBarActivity";
    private static final int MESSAGE = 1;
    private static final int CONTACT = 2;
    private static final int ADD_PERSON = 3;
    private ActionBar mActionBar;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mTransaction;
//    private Fragment mCurrentFragment;
    private MessageFragment mMessageFragment = new MessageFragment();
    private String mUserName = YueQiuApp.sUserInfo.getUsername();
    private String mPassword;//暂时不需要密码
    private LinearLayout mBottomContainer;
    private TextView mUnreadCountTv;
    private RelativeLayout mMsgView, mContactView, mAddPersonView;

    private BeepManager beep;
    private GotyeAPI api;
    private boolean returnNotify = false;
    public static SearchView mSearchView;
    private int mType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = GotyeAPI.getInstance();
        api.addListener(this);
        setContentView(R.layout.activity_chatbar_main);
        beep = new BeepManager(this);
        beep.updatePrefs();

        mFragmentManager = getSupportFragmentManager();
        initView();

        IntentFilter filter = new IntentFilter();
        filter.addAction(PublicConstant.CHAT_HAS_NEW_MSG);
        filter.addAction(PublicConstant.CHAT_HAS_NO_MSG);
        registerReceiver(mReceiver, filter);

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

        // -1   offline, API will reconnect when network becomes valid
        // 0    not login or logout already
        // 1    online
        int loginState = api.getOnLineState();
        Log.e("ddd", "ChatBarActivity -> login state -> " + loginState);
        if (loginState == 0) {
            Utils.showToast(this, getString(R.string.im_user_offline));
            //需重新登录
            api.login(YueQiuApp.sUserInfo.getPhone(), null);
        } else {
            Intent toService = new Intent(this, GotyeService.class);
            startService(toService);
            api.beginRcvOfflineMessge();
        }

        if(mSearchView != null){
            mSearchView.clearFocus();

        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, " notification called ");
//        initView();
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
        mSearchView = (SearchView) menu.findItem(R.id.near_nemu_search).getActionView();

        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) mSearchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.LTGRAY);

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
            e.printStackTrace();
        }
        mSearchView = (SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        mSearchView.setIconified(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (Utils.networkAvaiable(ChatBarActivity.this)) {
                    Intent intent = new Intent(ChatBarActivity.this, SearchResultActivity.class);
                    Bundle args = new Bundle();
                    args.putInt(PublicConstant.SEARCH_TYPE, PublicConstant.SEARCH_FRIEND);
                    args.putString(PublicConstant.SEARCH_KEYWORD, query);
                    intent.putExtras(args);
                    startActivity(intent);
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                } else {
                    Utils.showToast(ChatBarActivity.this, getString(R.string.network_not_available));
                }
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
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

    private void initView() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, YueQiuApp.sBottomHeight);
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
        mTransaction = mFragmentManager.beginTransaction();
        mTransaction.replace(R.id.chatbar_fragment_container, mMessageFragment,"message");
        mTransaction.commit();
        mType = MESSAGE;

        mMsgView.setOnClickListener(this);
        mContactView.setOnClickListener(this);
        mAddPersonView.setOnClickListener(this);

    }

    private void switchFragment(int type) {

        if (mType == type)
            return;
        mTransaction = mFragmentManager.beginTransaction();
        switch(type){
            case MESSAGE:
                mMessageFragment = new MessageFragment();
                mTransaction.replace(R.id.chatbar_fragment_container,mMessageFragment,"message").commit();
                mType = MESSAGE;
                break;
            case CONTACT:
                ContactFragment contact = new ContactFragment();
                mTransaction.replace(R.id.chatbar_fragment_container,contact,"contact").commit();
                mType = CONTACT;
                break;
            case ADD_PERSON:
                mType = ADD_PERSON;
                AddPersonFragment addPersonFragment = new AddPersonFragment();
                mTransaction.replace(R.id.chatbar_fragment_container,addPersonFragment,"addPerson").commit();
                break;
        }
    }



//    // 更新提醒
//    public void updateUnReadTip() {
//        int unreadCount = api.getTotalUnreadMsgCount();
//        int unreadNotifyCount = api.getUnreadNotifyCount();
//        unreadCount += unreadNotifyCount;
////        msgTip.setVisibility(View.VISIBLE);
////        if (unreadCount > 0 && unreadCount < 100) {
////            msgTip.setText(String.valueOf(unreadCount));
////        } else if (unreadCount >= 100) {
////            msgTip.setText("99");
////        } else {
////            msgTip.setVisibility(View.GONE);
////        }
//    }

    // 页面刷新
    private void mainRefresh() {
//        updateUnReadTip();
        mMessageFragment.refresh();

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
        Log.d("ddd", "onReceiveMessage type = " + message.getReceiverType());
        if (unRead) {
//            updateUnReadTip();

//            if (!api.isNewMsgNotify()) {
//                return;
//            }
//            if (message.getReceiverType() == 2) {
//                if (api.isNotReceiveGroupMsg()) {
//                    return;
//                }
//                if (api.isGroupDontdisturb(((GotyeGroup) message.getReceiver())
//                        .getGroupID())) {
//                    return;
//                }
//            }
            beep.playBeepSoundAndVibrate();
        }

        Intent intent = new Intent();
        intent.setAction(PublicConstant.CHAT_HAS_NEW_MSG);
        sendBroadcast(intent);
    }

    // 自己发送的信息统一在此处理
    @Override
    public void onSendMessage(int code, GotyeMessage message) {
        if (returnNotify) {
            return;
        }
        mMessageFragment.refresh();
    }

    @Override
    public void onReceiveMessage(int i, GotyeMessage gotyeMessage) {
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
        //得到离线消息
        if (i== 0) {
            Log.d(TAG, "offline message receiver : gotyeMessage = " + gotyeMessages);
            mainRefresh();
        }
    }

    @Override
    public void onOutputAudioData(byte[] bytes) {
    }

    @Override
    public void onGetCustomerService(int i, GotyeUser gotyeUser, int i2, String s) {
    }

    // 收到群邀请信息
    @Override
    public void onReceiveNotify(int code, GotyeNotify notify) {

//        if (returnNotify) {
//            return;
//        }
//        mMessageFragment.refresh();
//        updateUnReadTip();
//        if (!api.isNotReceiveGroupMsg()) {
//            beep.playBeepSoundAndVibrate();
//        }
    }

    @Override
    public void onRemoveFriend(int code, GotyeUser user) {
        if (returnNotify) {
            return;
        }
        api.deleteSession(user, false);
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

        if(mSearchView != null){
            mSearchView.clearFocus();
            mSearchView.setIconified(true);
        }
        switch(v.getId()){

            case R.id.chat_bar_msg_re:
                switchFragment(MESSAGE);
                mActionBar.setTitle(R.string.btn_liaoba_message);
                setBottomBackgroud(mMsgView, mContactView, mAddPersonView);
                break;
            case R.id.chat_bar_contact_re:
                switchFragment(CONTACT);
                mActionBar.setTitle(R.string.btn_liaoba_contact);
                setBottomBackgroud(mContactView, mMsgView, mAddPersonView);
                break;
            case R.id.chat_bar_add_re:
                switchFragment(ADD_PERSON);
                 mActionBar.setTitle(R.string.btn_liaoba_add_friend);
                setBottomBackgroud(mAddPersonView,mMsgView,mContactView);

                break;
        }
    }

    private void setBottomBackgroud(RelativeLayout greenView, RelativeLayout blackView1, RelativeLayout blackView2) {
        greenView.setBackgroundColor(getResources().getColor(R.color.actionbar_color));
        blackView1.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
        blackView2.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
    }

    private void getBgColor(){
        if(mType == MESSAGE){
            mMsgView.setBackgroundColor(getResources().getColor(R.color.actionbar_color));
            mContactView.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
            mAddPersonView.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
        }else if(mType == CONTACT){
            mContactView.setBackgroundColor(getResources().getColor(R.color.actionbar_color));
            mMsgView.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
            mAddPersonView.setBackgroundColor(getResources().getColor(R.color.search_radio_normal_bg));
        }else if(mType == ADD_PERSON){
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
                Fragment fragment = mFragmentManager.findFragmentByTag("message");
                if(fragment == null) {
                    mUnreadCountTv.setVisibility(View.VISIBLE);
                }
            }else if(action.equals(PublicConstant.CHAT_HAS_NO_MSG)){
                mUnreadCountTv.setVisibility(View.GONE);
            }
        }
    };


    @Override
    public void onLogout(int i) {
        Log.d(TAG, "IM logout!");
    }

    @Override
    public void onLogin(int i, GotyeUser gotyeUser) {
        if (i == 0) {
            Log.d(TAG, "current user " + gotyeUser.getName() + " ReLogin success !!");
            Intent toService = new Intent(this, GotyeService.class);
            startService(toService);
            api.beginRcvOfflineMessge();
        }
    }

    @Override
    public void onReconnecting(int i, GotyeUser gotyeUser) {
        if (i == 0) {
            Log.d(TAG, "current user " + gotyeUser.getName() + " ReLogin success by network available!!");
        }
    }
}
