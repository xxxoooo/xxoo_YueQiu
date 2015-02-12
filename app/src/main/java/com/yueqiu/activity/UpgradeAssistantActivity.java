package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.UserDao;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;

/**
 * Created by doushuqi on 15/1/8.
 */
public class UpgradeAssistantActivity extends Activity {

    private NetworkImageView mPhotoImageView;
    private ImageLoader mImgLoader;
    private UserDao mUserDao;
    private UserInfo mUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_assistant);
        mImgLoader = VolleySingleton.getInstance().getImgLoader();
        mUserDao = DaoFactory.getUser(this);
        mUserInfo = mUserDao.getUserByUserId(String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.update_to_assistant));
        initView();
    }

    private void initView() {
        mPhotoImageView = (NetworkImageView) findViewById(R.id.upgrade_assistant_photo);
        String str = mUserInfo.getImg_url();
        mPhotoImageView.setImageUrl(str, mImgLoader);
        //TODO:后续接口到了，要完成等多控件的初始化和显示效果
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.setActivityMenuColor(this);
        getMenuInflater().inflate(R.menu.commit, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return true;
            case R.menu.commit:
                //提交升级助教的资料

                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
