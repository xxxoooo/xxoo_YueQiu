package com.yueqiu.activity.searchmenu;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.constant.HttpConstants;
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

    private static final int LOGIN_SUCCESS = 0x01;
    private static final int LOGIN_ERROR = 0x02;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case LOGIN_ERROR:
                    Toast.makeText(LoginActivity.this,"登录失败，请重新登录！",
                            Toast.LENGTH_SHORT).show();
                    break;
                case LOGIN_SUCCESS:
                    Toast.makeText(LoginActivity.this,"登录成功！",
                            Toast.LENGTH_SHORT).show();
                    Utils.getOrUpdateUserBaseInfo(LoginActivity.this,(Map<String, String>)msg.obj);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }


    private void initView()
    {
        mBtnLogin = (Button)findViewById(R.id.login_btn_login);
        mTvRegister = (TextView)findViewById(R.id.login_tv_register);
        mTvForgetPwd = (TextView)findViewById(R.id.login_tv_forgetpwd);
        mEtUserId = (EditText)findViewById(R.id.login_et_userid);
        mEtPwd = (EditText)findViewById(R.id.login_et_pwd);
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setTitle(getString(R.string.login));

        mBtnLogin.setOnClickListener(this);
        mTvForgetPwd.setOnClickListener(this);
        mTvRegister.setOnClickListener(this);
    }


    private void log(String str)
    {
        Log.i(TAG,"-------"+str+"---------");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_btn_login:
                final String userId = mEtUserId.getText().toString().trim();
                if(TextUtils.isEmpty(userId)) {
                    Toast.makeText(LoginActivity.this, "请输入手机号或账号",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                final String pwd = mEtPwd.getText().toString().trim();
                if(TextUtils.isEmpty(pwd))
                    Toast.makeText(LoginActivity.this,"请输入密码",
                            Toast.LENGTH_SHORT).show();


                new Thread()
                {
                    @Override
                    public void run() {
                        super.run();
                        Map<String, String> map = new HashMap<String, String>();
                        map.put(HttpConstants.LoginConstant.USERNAME,userId);
                        map.put(HttpConstants.LoginConstant.PASSWORD,pwd);
                        String result = HttpUtil.urlClient(HttpConstants.LoginConstant.URL,
                                map, HttpConstants.RequestMethod.POST);
                        JSONObject object = Utils.parseJson(result);
                        Message message = new Message();
                        if(null == object)
                        {
                            message.what = LOGIN_ERROR;
                        }
                        else
                        {
                            message.what = LOGIN_SUCCESS;
                            Map<String,String> successObj = new HashMap<String, String>();
                            successObj.put("username",userId);
                            successObj.put("password",pwd);
                            try {
                                successObj.put("token",object.getJSONObject("result").
                                        getString("token"));
                                successObj.put("token",object.getJSONObject("result").
                                        getString("login_time"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        mHandler.sendMessage(message);
                    }
                }.start();


                    Toast.makeText(LoginActivity.this,"登录失败，请重新登录！",
                            Toast.LENGTH_SHORT).show();
                break;
            case R.id.login_tv_forgetpwd:
                break;
            case R.id.login_tv_register:
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                break;
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
