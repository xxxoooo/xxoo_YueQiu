package com.yueqiu.activity.searchmenu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.ActionBar;

import com.yueqiu.R;


/**
 * Created by yinfeng on 14/12/17.
 */
public class LoginActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    private Button mBtnLogin;
    private TextView mTvRegister;
    private TextView mTvForgetPwd;
    private EditText mEtUserId;
    private EditText mEtPwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initActionBar();
    }

    private void initActionBar(){
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.custom_actionbar_layout, null);
            View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
            saveMenuItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoginActivity.this.finish();
                }
            });
            TextView title = (TextView) customActionBarView.findViewById(R.id.action_bar_title);
            title.setText(getString(R.string.search_login_str));
            actionBar.setDisplayShowCustomEnabled(true);
            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(customActionBarView,params);
        }
    }

    private void initView()
    {
        mBtnLogin = (Button)findViewById(R.id.login_btn_login);
        mTvRegister = (TextView)findViewById(R.id.login_tv_register);
        mTvForgetPwd = (TextView)findViewById(R.id.login_tv_forgetpwd);
        mEtUserId = (EditText)findViewById(R.id.login_et_userid);
        mEtPwd = (EditText)findViewById(R.id.login_et_pwd);
        mBtnLogin.setOnClickListener(this);
        mTvForgetPwd.setOnClickListener(this);
        mTvRegister.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_btn_login:
                break;
            case R.id.login_tv_forgetpwd:
                break;
            case R.id.login_tv_register:
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                break;
        }

    }
}
