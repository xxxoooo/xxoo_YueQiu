package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
 * Created by yinfeng on 15/1/19.
 */
public class Register1Activity extends Activity implements View.OnClickListener {
    private ActionBar mActionBar;
    private Intent mIntent;
    private String mPhone, mCode;
    private Bundle bundle;

    private EditText mEtAccount;
    private EditText mEtPwd;
    private TextView mTvSex;
    private CheckBox mCheckBox;
    private Button mBtnRegister;
    private TextView mTvAgree;
    private TextView mTvLogin;

    private Map<String, String> mMap;


    private static final int REQUESTCODE = 0x03;
    private static final int REGISTER_SUCCESS = 0x00;
    private static final int REGISTER_ERROR = 0x01;
    private static final int CODE_MAN = 0;
    private static final int CODE_WOMAN = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case REGISTER_ERROR:
                    Toast.makeText(Register1Activity.this, (String)msg.obj,Toast.LENGTH_SHORT).show();
                    break;
                case REGISTER_SUCCESS:
                    Toast.makeText(Register1Activity.this, "注册成功，请登录",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register1);
        mIntent = getIntent();
        bundle = mIntent.getExtras();
        mPhone = bundle.getString("phone");
        mCode = bundle.getString("verfication_code");
        mMap = new HashMap<String, String>();
        initActionBar();
        initView();

    }

    private void initView() {
        mEtAccount = (EditText) findViewById(R.id.activity_register1_et_account);
        mEtPwd = (EditText) findViewById(R.id.activity_register1_et_password);
        mTvAgree = (TextView) findViewById(R.id.register1_read_and_agree_the_article);
        mTvLogin = (TextView) findViewById(R.id.activity_register1_tv_login);
        mTvSex = (TextView) findViewById(R.id.activity_register1_et_sex);
        mCheckBox = (CheckBox) findViewById(R.id.register1_agree_article_check);
        mBtnRegister = (Button) findViewById(R.id.activity_register1_btn_register);
        SpannableString spanStr = new SpannableString(getString(R.string.already_read_and_agree_the_article));
        spanStr.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.have_agree_and_read)), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new URLSpan("http://www.baidu.com"), 7, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvAgree.setText(spanStr);
        mTvAgree.setMovementMethod(LinkMovementMethod.getInstance());
        mTvLogin.setOnClickListener(this);
        mTvSex.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);
    }

    private void initActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(getString(R.string.register));
    }


    private Runnable register = new Runnable() {
        @Override
        public void run() {
            String retStr = HttpUtil.urlClient(HttpConstants.RegisterConstant.URL,
                    mMap, HttpConstants.RequestMethod.POST);
            if(null != retStr)
            {
                try {
                    JSONObject object = new JSONObject(retStr);
                    Message msg = new Message();
                    if(object.getInt("code") != 1001)
                    {
                        msg.what = REGISTER_ERROR;
                        msg.obj = object.getString("msg");
                    }
                    else
                    {
                        msg.what = REGISTER_SUCCESS;

                    }
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_register1_btn_register:
                String account = mEtAccount.getText().toString().trim();
//                Map<String, String> map = new HashMap<String, String>();
                if (account.equals("") || account == null) {
                    Toast.makeText(Register1Activity.this, "请输入账户", Toast.LENGTH_SHORT).show();
                    return;
                }
                mMap.put("username", account);
                String sex = mTvSex.getText().toString().trim();
                if (sex.equals("") || sex == null) {
                    Toast.makeText(Register1Activity.this, "请选择性别", Toast.LENGTH_SHORT).show();
                    return;
                }
                mMap.put("sex", sex);

                String password = mEtPwd.getText().toString().trim();
                if (password.equals("") || password == null) {
                    Toast.makeText(Register1Activity.this, "请输入6-30位密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6 || password.length() > 30) {
                    Toast.makeText(Register1Activity.this, "请输入6-30位密码", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (!mCheckBox.isChecked()) {
                    Toast.makeText(Register1Activity.this,
                            getString(R.string.please_read_article), Toast.LENGTH_SHORT).show();
                    return;
                }
                mMap.put("password", password);
                mMap.put("phone", mPhone);
                mMap.put("verfication_code", mCode);
                new Thread(register).start();
                break;

            case R.id.activity_register1_tv_login:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;

            case R.id.activity_register1_et_sex:
                Intent intent = new Intent();
                intent.setClass(Register1Activity.this, ActivitySelectSex.class);
                if (mTvSex.getText().toString().trim().equals(getString(R.string.man))) {
                    intent.putExtra("sex", CODE_MAN);
                } else {
                    intent.putExtra("sex", CODE_WOMAN);
                }
                startActivityForResult(intent, REQUESTCODE);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE && resultCode == RESULT_OK) {
            mTvSex.setText(data.getIntExtra("sex", 0) == 0 ?
                    getString(R.string.man) : getString(R.string.woman));
        }
    }
}
//01-19 11:45:26.396: I/HttpUtil(2426): http://hxu0480201.my3w.com/index.php/v1/user/register?verfication_code=964390&phone=13810191597&password=123456&account=USSR&sex=%E7%94%B7

