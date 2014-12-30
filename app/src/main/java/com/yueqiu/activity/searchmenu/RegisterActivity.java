package com.yueqiu.activity.searchmenu;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.app.ActionBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.util.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yinfeng on 14/12/18.
 */
public class RegisterActivity extends Activity  implements View.OnClickListener, RadioGroup.OnCheckedChangeListener{
    private static final String TAG = "RegisterActivity";
    private EditText mEtUserName,mEtPwd,mEtNumber;
    private RadioGroup mRGroup;
    private Button mBtnRegister;
    private String account, phone, sex, pwd;
    private ActionBar mActionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initActionBar();
        initView();
    }

    private void Log(String msg)
    {
        Log.i(TAG,"------------"+msg+"-------------");
    }
    private void initActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.register));
    }
    private void initView()
    {

        mEtNumber = (EditText) findViewById(R.id.register_et_username);
        mEtUserName = (EditText) findViewById(R.id.register_et_username);
        mEtPwd = (EditText) findViewById(R.id.register_et_pwd);
        mEtNumber = (EditText) findViewById(R.id.register_et_phonenumber);
        mRGroup = (RadioGroup) findViewById(R.id.register_rg_sex);
        mBtnRegister = (Button) findViewById(R.id.register_btn_register);
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setTitle(getString(R.string.register));
        mRGroup.setOnCheckedChangeListener(this);

        mBtnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.register_btn_register:
                account = mEtUserName.getText().toString().trim();
                if("".equals(account))
                {
                    Toast.makeText(RegisterActivity.this,
                            "账号不能为空，请输入账号！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if("".equals(sex))
                {
                    Toast.makeText(RegisterActivity.this,
                            "请选择性别！",Toast.LENGTH_SHORT).show();
                    return;
                }
                phone = mEtNumber.getText().toString().trim();
                if("".equals(phone))
                {
                    Toast.makeText(RegisterActivity.this,
                            "手机号不能为空，请输入手机号！",Toast.LENGTH_SHORT).show();
                    return;
                }
                pwd = mEtPwd.getText().toString().trim();
                if("".equals(account))
                {
                    Toast.makeText(RegisterActivity.this,
                            "密码不能为空，请输入密码！",Toast.LENGTH_SHORT).show();
                    return;
                }
                final Map<String,String> map = new HashMap<String, String>();
                map.put(HttpConstants.RegisterConstant.ACCOUNT,account);
                map.put(HttpConstants.RegisterConstant.PASSWORD,pwd);
                map.put(HttpConstants.RegisterConstant.PHONE,phone);
                map.put(HttpConstants.RegisterConstant.SEX, sex == "男" ? "1" : "2");
                new Thread()
                {
                    @Override
                    public void run() {
                        super.run();
                        String result = HttpUtil.urlClient(HttpConstants.RegisterConstant.URL,
                                map, HttpConstants.RequestMethod.POST);
                        Log(result);
                    }
                }.start();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int radioButtonId = group.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton)RegisterActivity.this.findViewById(radioButtonId);
        sex = radioButton.getText().toString().trim();

    }
}
