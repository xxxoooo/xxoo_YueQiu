package com.yueqiu.activity.searchmenu;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.yueqiu.R;

/**
 * Created by yinfeng on 14/12/18.
 */
public class RegisterActivity extends Activity  implements View.OnClickListener{
    private static final String TAG = "RegisterActivity";
    private TextView mTvBack;
    private EditText mEtUserName,mEtPwd,mEtNumber;
    private RadioGroup mRGroup;
    private Button mBtnRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        initView();
    }

    private void Log(String msg)
    {
        Log.i(TAG,msg);
    }

    private void initView()
    {
        mTvBack = (TextView) findViewById(R.id.register_tv_back);
        mEtNumber = (EditText) findViewById(R.id.register_et_username);
        mEtPwd = (EditText) findViewById(R.id.register_et_pwd);
        mEtNumber = (EditText) findViewById(R.id.register_et_phonenumber);
        mRGroup = (RadioGroup) findViewById(R.id.register_rg_sex);
        mBtnRegister = (Button) findViewById(R.id.register_btn_register);
        mTvBack.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.register_tv_back:
                finish();
                break;
            case R.id.register_btn_register:
                break;
        }
    }
}
