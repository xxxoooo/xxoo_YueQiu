package com.yueqiu.activity.searchmenu;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.ActionBar;

import com.yueqiu.R;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * 登录Activity
 * Created by yinfeng on 14/12/17.
 */
public class LoginActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    private Button mBtnLogin;
    private TextView mTvRegister;
    private TextView mTvForgetPwd;
    private EditText mEtUserId;
    private EditText mEtPwd;
    private ActionBar mActionBar;
    private InputMethodManager mImm;

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
                    Log.i(TAG, msg.obj.toString() );
                    Utils.getOrUpdateUserBaseInfo(LoginActivity.this,(Map<String, String>) msg.obj);
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
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.login));

    }

    private void initView() {
        mBtnLogin = (Button) findViewById(R.id.activity_login_btn_login);
        mTvRegister = (TextView) findViewById(R.id.activity_login_tv_register);
//        mTvForgetPwd = (TextView)findViewById(R.id.login_tv_forgetpwd);
        mEtUserId = (EditText) findViewById(R.id.activity_login_et_username);
        mEtPwd = (EditText) findViewById(R.id.activity_login_et_password);
////
        mBtnLogin.setOnClickListener(this);
//        mTvForgetPwd.setOnClickListener(this);
       mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mTvRegister.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_login_btn_login:
                final String userId = mEtUserId.getText().toString().trim();
                if (TextUtils.isEmpty(userId)) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_without_phone_or_account),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                final String pwd = mEtPwd.getText().toString().trim();
                if (TextUtils.isEmpty(pwd))
                    Toast.makeText(LoginActivity.this, getString(R.string.login_without_pasword),
                            Toast.LENGTH_SHORT).show();


                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Map<String, String> map = new HashMap<String, String>();
                        map.put(HttpConstants.LoginConstant.USERNAME, userId);
                        map.put(HttpConstants.LoginConstant.PASSWORD, pwd);
                        String result = HttpUtil.urlClient(HttpConstants.LoginConstant.URL,
                                map, HttpConstants.RequestMethod.POST);
                        JSONObject object = Utils.parseJson(result);
                        Message message = new Message();
                        try {
                            if (object.getInt("code") != HttpConstants.ResponseCode.NORMAL) {
                                message.what = LOGIN_ERROR;
                                message.obj = object.getString("msg");
                            } else {
                                Map<String, String> successObj = new HashMap<String, String>();
                                successObj.put(PublicConstant.USER_NAME, userId);
                                successObj.put(PublicConstant.PASSWORD, pwd);
                                successObj.put(PublicConstant.USER_ID,object.getJSONObject("result").
                                        getString("user_id"));
                                successObj.put(PublicConstant.TOKEN, object.getJSONObject("result").
                                        getString("token"));
                                successObj.put(PublicConstant.LOGIN_TIME, object.getJSONObject("result").
                                        getString("login_time"));
                                successObj.put(PublicConstant.PHONE, object.getJSONObject("result").
                                        getString("phone"));
                                successObj.put(PublicConstant.IMG_URL, object.getJSONObject("result").
                                        getString("img_url"));
                                message.what = LOGIN_SUCCESS;
                                message.obj = successObj;

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        mHandler.sendMessage(message);
                    }
                }.start();

                mImm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            case R.id.activity_login_tv_register:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                break;
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
