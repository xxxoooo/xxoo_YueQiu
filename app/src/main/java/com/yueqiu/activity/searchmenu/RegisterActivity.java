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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yinfeng on 14/12/18.
 */
public class RegisterActivity extends Activity  implements View.OnClickListener{
    private static final String TAG = "RegisterActivity";
    private static final int REQUESTCODE = 0x03;
    private static final int REGISTER_SUCCESS = 0x00;
    private static final int REGISTER_ERROR = 0x01;
    private static final int CODE_MAN = 0;
    private static final int CODE_WOMAN = 1;
    private EditText mEtUserName,mEtPwd,mEtNumber,mEtSex;
    private Button mBtnRegister;
    private TextView mTvLogin;
    private Intent mIntent;
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case REGISTER_ERROR:
                    toast(msg.obj.toString());
                    break;
                case REGISTER_SUCCESS:
                    toast("注册成功，请登录！");
                    RegisterActivity.this.finish();
                    overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
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
                    RegisterActivity.this.finish();
                    overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
                }
            });
            TextView title = (TextView) customActionBarView.findViewById(R.id.action_bar_title);
            title.setText(getString(R.string.register));
            actionBar.setDisplayShowCustomEnabled(true);
            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(customActionBarView,params);
        }
    }

    private void Log(String msg)
    {
        Log.i(TAG,"------------"+msg+"-------------");
    }

    private void initView()
    {
        mEtUserName = (EditText) findViewById(R.id.activity_register_et_account);
        mEtPwd = (EditText) findViewById(R.id.activity_register_et_password);
        mEtNumber = (EditText) findViewById(R.id.activity_register_et_phone);
        mEtSex = (EditText)findViewById(R.id.activity_register_et_sex);
        mTvLogin = (TextView) findViewById(R.id.activity_register_tv_login);
        mBtnRegister = (Button) findViewById(R.id.activity_register_btn_register);
        mEtSex.setText(getString(R.string.man));
        mIntent = getIntent();
        mBtnRegister.setOnClickListener(this);
        mEtSex.setOnClickListener(this);
        mTvLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.activity_register_btn_register:
                String account = mEtUserName.getText().toString().trim();
                if(TextUtils.isEmpty(account))
                {
                    toast(getString(R.string.account_null));
                    return ;
                }
                String phone = mEtNumber.getText().toString().trim();
                if(TextUtils.isEmpty(phone))
                {
                    toast(getString(R.string.phone_null));
                    return ;
                }
                String password = mEtPwd.getText().toString().trim();
                if(TextUtils.isEmpty(password))
                {
                    toast(getString(R.string.password_null));
                    return ;
                }
                if(password.length() < 6 || phone.length() > 30)
                {
                    toast("请输入6-30位的密码！");
                    return ;
                }
                register(account, phone,
                        mEtSex.getText().toString().trim(), password);
                break;
            case R.id.activity_register_et_sex:
                Intent intent = new Intent();
                intent.setClass(RegisterActivity.this,ActivitySelectSex.class);
                Log( mEtSex.getText().toString().trim());
                if( mEtSex.getText().toString().trim().equals(getString(R.string.man)))
                {    intent.putExtra("sex",CODE_MAN); }
                else {
                    intent.putExtra("sex", CODE_WOMAN);
                }
                startActivityForResult(intent,REQUESTCODE);
                overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
                break;
            case R.id.activity_register_tv_login:
                finish();
                overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
                break;
        }
    }

    private void toast(String msg)
    {
        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT)
                .show();;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUESTCODE && resultCode == RESULT_OK)
        {
            mEtSex.setText(data.getIntExtra("sex",0) == 0 ?
                    getString(R.string.man) : getString(R.string.woman));
        }
    }


    private void register(final String account, final String phone,
                          final String sex, final String pwd)
    {
        final Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put(HttpConstants.RegisterConstant.ACCOUNT, account);
        requestMap.put(HttpConstants.RegisterConstant.PHONE, phone);
        requestMap.put(HttpConstants.RegisterConstant.SEX, sex.equals(
                getString(R.string.man)) ? String.valueOf(1) : String.valueOf(2));
        requestMap.put(HttpConstants.RegisterConstant.PASSWORD, pwd);
        new Thread()
        {
            @Override
            public void run() {
                super.run();
                String result = HttpUtil.urlClient(HttpConstants.RegisterConstant.URL,
                        requestMap,HttpConstants.RequestMethod.POST);
                try {
                    JSONObject object = new JSONObject(result);
                    Message msg = new Message();
                    if(object.getInt("code") != HttpConstants.ResponseCode.NORMAL)
                    {
                        msg.what = REGISTER_ERROR;
                        msg.obj = object.getString("msg");
                    }
                    else
                    {
                        msg.what = REGISTER_SUCCESS;
                        msg.obj = object.getString("msg");
                    }
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

}
