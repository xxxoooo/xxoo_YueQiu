package com.yueqiu.activity.searchmenu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yueqiu.R;


/**
 * Created by yinfeng on 14/12/17.
 */
public class LoginActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    private Button mBtnLogin;
    private TextView mTvBack;
    private TextView mTvRegister;
    private TextView mTvForgetPwd;
    private EditText mEtUserId;
    private EditText mEtPwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        initView();
    }


    private void initView()
    {
        mBtnLogin = (Button)findViewById(R.id.login_btn_login);
        mTvBack = (TextView)findViewById(R.id.login_tv_back);
        mTvRegister = (TextView)findViewById(R.id.login_tv_register);
        mTvForgetPwd = (TextView)findViewById(R.id.login_tv_forgetpwd);
        mEtUserId = (EditText)findViewById(R.id.login_et_userid);
        mEtPwd = (EditText)findViewById(R.id.login_et_pwd);
        mBtnLogin.setOnClickListener(this);
        mTvForgetPwd.setOnClickListener(this);
        mTvBack.setOnClickListener(this);
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
                break;
            case R.id.login_tv_back:
                break;
            case R.id.login_tv_forgetpwd:
                break;
            case R.id.login_tv_register:
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                break;
        }

    }
}
