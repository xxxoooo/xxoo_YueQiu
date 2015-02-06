package com.yueqiu;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeGroup;
import com.gotye.api.GotyeMessage;
import com.gotye.api.GotyeNotify;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.GotyeUser;
import com.gotye.api.PathUtil;
import com.gotye.api.listener.LoginListener;
import com.gotye.api.listener.NotifyListener;
import com.yueqiu.activity.NearbyResultActivity;
import com.yueqiu.chatbar.GotyeService;
import com.yueqiu.fragment.chatbar.AddPersonFragment;
import com.yueqiu.fragment.chatbar.ContactFragment;
import com.yueqiu.fragment.chatbar.MessageFragment;
import com.yueqiu.util.BeepManager;
import com.yueqiu.util.BitmapUtil;
import com.yueqiu.util.FileUtil;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.Field;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧Activity
 */
public class ChatBarActivity extends FragmentActivity implements LoginListener, NotifyListener {
    private static final String TAG = "ChatBarActivity";
    private ActionBar mActionBar;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private RadioGroup radioGroup;
    private Fragment mCurrentFragment;
    private MessageFragment mMessageFragment = new MessageFragment();
    private ContactFragment mContactFragment = new ContactFragment();
    private AddPersonFragment mAddPersonFragment = new AddPersonFragment();
    private String mUserName = YueQiuApp.sUserInfo.getUsername();
    private String mPassword;//暂时不需要密码

    private BeepManager beep;
    private GotyeAPI api;
    private boolean returnNotify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = GotyeAPI.getInstance();
        api.addListerer(this);
        login();
        setContentView(R.layout.activity_chatbar_main);
        beep = new BeepManager(this);
        beep.updatePrefs();

        fragmentManager = getSupportFragmentManager();
        initView();
    }

    private void login() {
        Log.e(TAG, "isOnline --> " + api.isOnline());
        if (!GotyeAPI.getInstance().isOnline()) {
            int i = GotyeAPI.getInstance().login(mUserName, null);
            // 根据返回的code判断
            if (i == GotyeStatusCode.CODE_OK) {
                // 已经登陆
                onLogin(i, null);
            }
        }
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
        returnNotify = false;
        mainRefresh();
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

        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, NearbyResultActivity.class)));
        return true;
    }

    private void initView() {
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
        ((RadioButton) radioGroup.findViewById(R.id.radio0)).setChecked(true);
        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.chatbar_fragment_container, mMessageFragment);
        transaction.commit();
        mCurrentFragment = mMessageFragment;
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio0:
                        switchFragment(mMessageFragment);
                        mActionBar.setTitle(R.string.btn_liaoba_message);
                        break;
                    case R.id.radio1:
                        switchFragment(mContactFragment);
                        mActionBar.setTitle(R.string.btn_liaoba_contact);
                        break;
                    case R.id.radio2:
                        switchFragment(mAddPersonFragment);
                        mActionBar.setTitle(R.string.btn_liaoba_add_friend);
                        break;
                }
            }
        });
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
        super.onDestroy();
    }

    // 此处处理账号在另外设备登陆造成的被动下线
    @Override
    public void onLogout(int code) {
        /*if (code == GotyeStatusCode.CODE_FORCELOGOUT) {
            Toast.makeText(this, "您的账号在另外一台设备上登录了！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getBaseContext(), LoginPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else if (code == GotyeStatusCode.CODE_NETWORD_DISCONNECTED) {
            Toast.makeText(this, "您的账号掉线了！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getBaseContext(), LoginPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else{
            Intent i = new Intent(this, LoginPage.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Toast.makeText(this, "退出登陆！", Toast.LENGTH_SHORT).show();
            startActivity(i);
        }
        finish();*/
    }

    @Override
    public void onLogin(int code, GotyeUser currentLoginUser) {
        // 判断登陆是否成功
        if (code == GotyeStatusCode.CODE_OK) {
            saveUser(mUserName, mPassword);

            Intent toService = new Intent(this, GotyeService.class);
            startService(toService);
            Toast.makeText(this, "已经登录成功。。。", Toast.LENGTH_SHORT).show();
        } else {
            // 失败,可根据code定位失败原因
            Toast.makeText(this, "尚未登录或者登录失败。。。", Toast.LENGTH_SHORT).show();
        }
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
     * @param code 状态码 参见 {@link GotyeStatusCode}
     * @param message  消息对象
     * @param unRead 是否已读
     */
    @Override
    public void onReceiveMessage(int code, GotyeMessage message, boolean unRead) {
        if (returnNotify) {
            return;
        }
        mMessageFragment.refresh();
        Log.d("wy","onReceiveMessage");
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
}
