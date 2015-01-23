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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yueqiu.BilliardSearchActivity;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.UserDao;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.MyURLSpan;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
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
    private TextView mTvLogin,mPreText;
    private ProgressBar mPreProgress;
    private Drawable mProgressDrawable;
    private String mAccount;

    private Map<String, String> mMap;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private UserDao mUserDao;

    private static final int REQUESTCODE = 0x03;
    private static final int REGISTER_SUCCESS = 0x00;
    private static final int REGISTER_ERROR = 0x01;
    private static final int CODE_MAN = 0;
    private static final int CODE_WOMAN = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case REGISTER_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(Register1Activity.this,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(Register1Activity.this,(String)msg.obj);
                    }
                    break;
                case REGISTER_SUCCESS:
                    Utils.showToast(Register1Activity.this,getString(R.string.register_success));
                    Map<String,String> map = (Map<String, String>) msg.obj;

                    YueQiuApp.sUserInfo.setUser_id(Integer.valueOf(map.get(DatabaseConstant.UserTable.USER_ID)));
                    YueQiuApp.sUserInfo.setPhone(map.get(DatabaseConstant.UserTable.PHONE));
                    YueQiuApp.sUserInfo.setLogin_time(map.get(DatabaseConstant.UserTable.LOGIN_TIME));
                    YueQiuApp.sUserInfo.setUsername(map.get(DatabaseConstant.UserTable.USERNAME));

                    Iterator iter = map.entrySet().iterator();
                    while(iter.hasNext()){
                        Map.Entry<String,String> entry = (Map.Entry<String, String>) iter.next();
                        mEditor.putString(entry.getKey(), entry.getValue());
                    }
                    mEditor.apply();

                    mUserDao.insertUserInfo(map);
                    Intent intent = new Intent(Register1Activity.this, BilliardSearchActivity.class);
                    startActivity(intent);
                    Register1Activity.this.finish();
                    overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
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
        mPhone = bundle.getString(HttpConstants.RegisterConstant.PHONE);
        mCode = bundle.getString(HttpConstants.RegisterConstant.VERFICATION_CODE);
        mMap = new HashMap<String, String>();
        mSharedPreferences = getSharedPreferences(PublicConstant.USERBASEUSER, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mUserDao = DaoFactory.getUser(Register1Activity.this);
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

        mPreText = (TextView) findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mPreText.setText(getString(R.string.pre_register_text));

        SpannableString spanStr = new SpannableString(getString(R.string.already_read_and_agree_the_article));
        spanStr.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.have_agree_and_read)), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new MyURLSpan("file://android_asset/policy.html"), 7, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                mAccount = mEtAccount.getText().toString().trim();
                if(TextUtils.isEmpty(mAccount))
                {
                    Utils.showToast(Register1Activity.this,getString(R.string.account_null));
                    return ;
                }
                mMap.put(HttpConstants.RegisterConstant.ACCOUNT, mAccount);
                String sex = mTvSex.getText().toString().trim();
                if (TextUtils.isEmpty(sex)) {
                    Utils.showToast(Register1Activity.this,getString(R.string.please_select_sex));
                    return;
                }
                mMap.put(HttpConstants.RegisterConstant.SEX, sex);

                String password = mEtPwd.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    Utils.showToast(Register1Activity.this,getString(R.string.password_null));
                    return;
                }

                if (password.length() < 6 || password.length() > 30) {
                    Utils.showToast(Register1Activity.this,getString(R.string.password_null));
                    return;
                }

                if (!mCheckBox.isChecked()) {
                    Utils.showToast(Register1Activity.this,
                            getString(R.string.please_read_article));
                    return;
                }
                mMap.put(HttpConstants.RegisterConstant.PASSWORD, password);
                mMap.put(HttpConstants.RegisterConstant.PHONE, mPhone);
                mMap.put(HttpConstants.RegisterConstant.VERFICATION_CODE, mCode);

                Map<String,String> paramMap = new HashMap<String, String>();
                paramMap.put(PublicConstant.URL,HttpConstants.RegisterConstant.URL);
                paramMap.put(PublicConstant.METHOD,HttpConstants.RequestMethod.POST);

                if(Utils.networkAvaiable(Register1Activity.this)){
                    new RegisterAsyncTask(mMap).execute(paramMap);
                }else{
                    Utils.showToast(Register1Activity.this, getString(R.string.network_not_available));
                }
                break;

            case R.id.activity_register1_tv_login:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;

            case R.id.activity_register1_et_sex:
                Intent intent = new Intent();
                intent.setClass(Register1Activity.this, PlaySelectSex.class);
                if (mTvSex.getText().toString().trim().equals(getString(R.string.man))) {
                    intent.putExtra(HttpConstants.RegisterConstant.SEX, CODE_MAN);
                } else {
                    intent.putExtra(HttpConstants.RegisterConstant.SEX, CODE_WOMAN);
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
            mTvSex.setText(data.getIntExtra(HttpConstants.RegisterConstant.SEX, 0) == 0 ?
                    getString(R.string.man) : getString(R.string.woman));
        }
    }


    private class RegisterAsyncTask extends AsyncTaskUtil<String> {

        public RegisterAsyncTask(Map<String, String> map) {
            super(map);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreProgress.setVisibility(View.VISIBLE);
            mPreText.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject object) {
            super.onPostExecute(object);
            mPreProgress.setVisibility(View.GONE);
            mPreText.setVisibility(View.GONE);
            try {
                if(!object.isNull("code")) {
                    if (object.getInt("code") != HttpConstants.ResponseCode.NORMAL) {


                        Map<String, String> map = new HashMap<String, String>();
                        map.put(DatabaseConstant.UserTable.USERNAME, mAccount);
                        map.put(DatabaseConstant.UserTable.USER_ID, object.getJSONObject("result").
                                getString(DatabaseConstant.UserTable.USER_ID));
                        map.put(DatabaseConstant.UserTable.PHONE, object.getJSONObject("result").getString(DatabaseConstant.UserTable.PHONE));
                        map.put(DatabaseConstant.UserTable.LOGIN_TIME, object.getJSONObject("result").getString(DatabaseConstant.UserTable.LOGIN_TIME));
                        mHandler.obtainMessage(REGISTER_SUCCESS, map).sendToTarget();


                    }else if(object.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                        mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                    }
                    else {
                        mHandler.obtainMessage(REGISTER_ERROR, object.getString("msg")).sendToTarget();
                    }
                }else{
                    mHandler.obtainMessage(REGISTER_ERROR).sendToTarget();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
//01-19 11:45:26.396: I/HttpUtil(2426): http://hxu0480201.my3w.com/index.php/v1/user/register?verfication_code=964390&phone=13810191597&password=123456&account=USSR&sex=%E7%94%B7

