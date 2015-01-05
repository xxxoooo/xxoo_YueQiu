package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
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
 * Created by yinfeng on 14/12/18.
 */
public class RegisterActivity extends Activity  implements View.OnClickListener{
    private static final String TAG = "RegisterActivity";
    private static final int REQUESTCODE = 0x03;
    private static final int REGISTER_SUCCESS = 0x00;
    private static final int REGISTER_ERROR = 0x01;
    private static final int CODE_MAN = 0;
    private static final int CODE_WOMAN = 1;
    private EditText mEtUserName,mEtPwd,mEtNumber;
    private Button mBtnRegister;
    private TextView mTvLogin,mReadArticle,mEtSex;
    private CheckBox mCheckBox;
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
                    toast(getString(R.string.register_success));
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
        initActionBar();
        initView();
    }

    private void initActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.register));
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
        mEtSex = (TextView)findViewById(R.id.activity_register_et_sex);
        mTvLogin = (TextView) findViewById(R.id.activity_register_tv_login);
        mBtnRegister = (Button) findViewById(R.id.activity_register_btn_register);
        mCheckBox = (CheckBox) findViewById(R.id.register_agree_article_check);
        mReadArticle = (TextView) findViewById(R.id.register_read_and_agree_the_article);

        SpannableString spanStr = new SpannableString(getString(R.string.already_read_and_agree_the_article));
        spanStr.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.have_agree_and_read)), 0, 7,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new URLSpan("http://www.baidu.com"),7,16,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mReadArticle.setText(spanStr);
        mReadArticle.setMovementMethod(LinkMovementMethod.getInstance());

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
                if(password.length() < 6 )
                {
                    toast(getString(R.string.please_input_password));
                    return ;
                }
                if(!mCheckBox.isChecked()){
                    toast(getString(R.string.please_read_article));
                    return;
                }
                if(Utils.networkAvaiable(RegisterActivity.this)) {
                    register(account, phone, mEtSex.getText().toString().trim(), password);
                }else{
                    Toast.makeText(RegisterActivity.this, getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                }
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
        if (requestCode == REQUESTCODE && resultCode == RESULT_OK) {
            mEtSex.setText(data.getIntExtra("sex", 0) == 0 ?
                    getString(R.string.man) : getString(R.string.woman));
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


    private void register(final String account,final String phone,
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
