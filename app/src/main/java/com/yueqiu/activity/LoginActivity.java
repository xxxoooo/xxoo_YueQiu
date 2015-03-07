package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.UserDao;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import java.util.Map;

public class LoginActivity extends Activity implements View.OnClickListener{
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
    private UserDao mUserDao;
    private View mRootView;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPreProgress.setVisibility(View.GONE);
            mPreText.setVisibility(View.GONE);
            switch (msg.what) {
                case PublicConstant.REQUEST_ERROR:
                    if(msg.obj == null){
                        Toast.makeText(LoginActivity.this, getString(R.string.http_request_error),
                                Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(LoginActivity.this, msg.obj.toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case PublicConstant.GET_SUCCESS:
                    Toast.makeText(LoginActivity.this, getString(R.string.login_success),Toast.LENGTH_SHORT).show();
                    Map<String,String> map = (Map<String, String>) msg.obj;
                    //TODO:用来更新全局userinfo
                    Utils.getOrUpdateUserBaseInfo(LoginActivity.this,map);
                    Intent intent = new Intent(PublicConstant.SLIDE_ACCOUNT_ACTION);
                    sendBroadcast(intent);
                    if(!mUserDao.queryUserId(map)){
                        mUserDao.insertUserInfo(map);
                    }
                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    break;
                case PublicConstant.TIME_OUT:
                    Toast.makeText(LoginActivity.this, getString(R.string.http_request_time_out),
                            Toast.LENGTH_SHORT).show();
                    break;
                case PublicConstant.NO_RESULT:
                    Toast.makeText(LoginActivity.this, msg.obj.toString(),
                            Toast.LENGTH_SHORT).show();
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
        mRootView = findViewById(R.id.login_root_view);


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

        ViewTreeObserver observer = mRootView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                mRootView.getRootView().getWindowVisibleDisplayFrame(rect);
                int screenHeight = mRootView.getRootView().getHeight();
                int keyboardHeight = screenHeight - (rect.bottom - rect.top);
                if(keyboardHeight != rect.top){
                    YueQiuApp.sKeyboardHeight = keyboardHeight;
                }
            }
        });
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

                if(Utils.networkAvaiable(LoginActivity.this)) {
                    login();
                }else{
                    Toast.makeText(LoginActivity.this, getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                }

                mImm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            case R.id.activity_login_tv_register:
                startActivity(new Intent(LoginActivity.this, GetCaptchaActivity.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                break;
        }

    }


    private void login(){

        mPreProgress.setVisibility(View.VISIBLE);
        mPreText.setVisibility(View.VISIBLE);

        Map<String, String> params = new HashMap<String, String>();
        params.put(HttpConstants.LoginConstant.USERNAME, mUserName);
        params.put(HttpConstants.LoginConstant.PASSWORD, mPwd);

        HttpUtil.requestHttp(HttpConstants.LoginConstant.URL,params,HttpConstants.RequestMethod.POST,new JsonHttpResponseHandler(){
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","login response ->" + response);
                try {
                    if (!response.isNull("code")){
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            Map<String, String> successObj = new HashMap<String, String>();
                            successObj.put(DatabaseConstant.UserTable.USERNAME, response.getJSONObject("result").
                                    getString(DatabaseConstant.UserTable.USERNAME));
                            successObj.put(DatabaseConstant.UserTable.PASSWORD, mPwd);
                            successObj.put(DatabaseConstant.UserTable.USER_ID, response.getJSONObject("result").
                                    getString(DatabaseConstant.UserTable.USER_ID));
                            successObj.put(DatabaseConstant.UserTable.LOGIN_TIME, response.getJSONObject("result").
                                    getString(DatabaseConstant.UserTable.LOGIN_TIME));
                            successObj.put(DatabaseConstant.UserTable.PHONE, response.getJSONObject("result").
                                    getString(DatabaseConstant.UserTable.PHONE));
                            successObj.put(DatabaseConstant.UserTable.IMG_URL, response.getJSONObject("result").
                                    getString(DatabaseConstant.UserTable.IMG_URL));
                            successObj.put(DatabaseConstant.UserTable.TITLE,response.getJSONObject("result").getString("title"));
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS,successObj).sendToTarget();
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.REQUEST_ERROR){
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                        }
                        else {
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
//    @Override
//    public void onLogout(int code) {
//
//    }
//
//    @Override
//    public void onLogin(int code, GotyeUser currentLoginUser) {
//        Log.e("gotyeapi", "onLogin-->callback   code = " + code);
//        // 判断登陆是否成功
//        if (code == GotyeStatusCode.CODE_OK) {
//            saveUser(mUserName, mPwd);
//
//            Intent toService = new Intent(this, GotyeService.class);
//            startService(toService);
//            Log.d(TAG, "登录时。。IM服务启动");
//        } else {
//            Log.d(TAG, "登录时。。IM服务启动失败，code = " + code);
//            // 失败,可根据code定位失败原因
////            Toast.makeText(this, "IM系统登录失败....", Toast.LENGTH_SHORT).show();
//        }
//    }

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

    @Override
    protected void onDestroy() {
        // 移除监听
        super.onDestroy();
    }
}
