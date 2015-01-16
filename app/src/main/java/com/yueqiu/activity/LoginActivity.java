package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.UserDao;
import com.yueqiu.db.DBUtils;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;



/**
 * 登录Activity
 * Created by yinfeng on 14/12/17.
 */
public class LoginActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    private Button mBtnLogin;
    private TextView mTvRegister;
    private TextView mTvForgetPwd,mPreText;
    private ProgressBar mPreProgress;
    private Drawable mProgressDrawable;
    private EditText mEtUserId;
    private EditText mEtPwd;
    private InputMethodManager mImm;
    private String mUserName;
    private String mPwd;
    private DBUtils mDbUtil;
    private UserDao mUserDao;

    private static final int LOGIN_SUCCESS = 0x01;
    private static final int LOGIN_ERROR = 0x02;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOGIN_ERROR:
                    Toast.makeText(LoginActivity.this, msg.obj.toString(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case LOGIN_SUCCESS:
                    Toast.makeText(LoginActivity.this, getString(R.string.login_success),Toast.LENGTH_SHORT).show();
                    Map<String,String> map = (Map<String, String>) msg.obj;
                    Utils.getOrUpdateUserBaseInfo(LoginActivity.this,map);
                    if(!mUserDao.queryUserId(map)){
                        mUserDao.insertUserInfo(map);
                    }
                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initActionBar();

        mDbUtil = DBUtils.getInstance(this);
        mUserDao = DaoFactory.getUser(this);

    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.login));

    }

    private void initView() {
        mBtnLogin = (Button) findViewById(R.id.activity_login_btn_login);
        mTvRegister = (TextView) findViewById(R.id.activity_login_tv_register);
        mEtUserId = (EditText) findViewById(R.id.activity_login_et_username);
        mEtPwd = (EditText) findViewById(R.id.activity_login_et_password);


        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mPreText = (TextView) findViewById(R.id.pre_text);
        mPreText.setText(getString(R.string.pre_login_text));
////
        mBtnLogin.setOnClickListener(this);
       mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mTvRegister.setOnClickListener(this);
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_login_btn_login:
                 mUserName = mEtUserId.getText().toString().trim();
                if (TextUtils.isEmpty(mUserName)) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_without_phone_or_account),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mPwd = mEtPwd.getText().toString().trim();
                if (TextUtils.isEmpty(mPwd)) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_without_pasword),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, String> requestMap = new HashMap<String, String>();
                requestMap.put(HttpConstants.LoginConstant.USERNAME, mUserName);
                requestMap.put(HttpConstants.LoginConstant.PASSWORD, mPwd);

                Map<String,String> paramMap = new HashMap<String, String>();
                paramMap.put(PublicConstant.URL,HttpConstants.LoginConstant.URL);
                paramMap.put(PublicConstant.METHOD,HttpConstants.RequestMethod.POST);
                if(Utils.networkAvaiable(LoginActivity.this)) {
                    new LoginAsyncTask(requestMap, mPreProgress, mPreText).execute(paramMap);
                }else{
                    Toast.makeText(LoginActivity.this, getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                }

                mImm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            case R.id.activity_login_tv_register:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                break;
        }

    }


    private class LoginAsyncTask extends AsyncTaskUtil<String>{

        public LoginAsyncTask(Map<String, String> map, ProgressBar progressBar, TextView textView) {
            super(map, progressBar, textView);
        }

        @Override
        protected void onPostExecute(JSONObject object) {
            super.onPostExecute(object);
            try {
                if (!object.isNull("code")){
                    if (object.getInt("code") != HttpConstants.ResponseCode.NORMAL) {
                        mHandler.obtainMessage(LOGIN_ERROR,object.getString("msg")).sendToTarget();
                    } else {
                        Map<String, String> successObj = new HashMap<String, String>();
                        successObj.put(DatabaseConstant.UserTable.USERNAME, mUserName);
                        successObj.put(DatabaseConstant.UserTable.PASSWORD, mPwd);
                        successObj.put(DatabaseConstant.UserTable.USER_ID, object.getJSONObject("result").
                                getString(DatabaseConstant.UserTable.USER_ID));
                        successObj.put(DatabaseConstant.UserTable.LOGIN_TIME, object.getJSONObject("result").
                                getString(DatabaseConstant.UserTable.LOGIN_TIME));
                        successObj.put(DatabaseConstant.UserTable.PHONE, object.getJSONObject("result").
                                getString(DatabaseConstant.UserTable.PHONE));
                        successObj.put(DatabaseConstant.UserTable.IMG_URL, object.getJSONObject("result").
                                getString(DatabaseConstant.UserTable.IMG_URL));

                        mHandler.obtainMessage(LOGIN_SUCCESS,successObj).sendToTarget();

                    }
                }else{
                    mHandler.obtainMessage(LOGIN_ERROR,object.getString("msg")).sendToTarget();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return super.onOptionsItemSelected(item);
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
