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
import android.text.style.URLSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.BilliardNearbyActivity;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.UserDao;
import com.yueqiu.dao.daoimpl.UserDaoImpl;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
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
    private TextView mTvLogin,mReadArticle,mEtSex,mPreText;
    private ProgressBar mPreProgress;
    private Drawable mProgressDrawable;
    private CheckBox mCheckBox;
    private Intent mIntent;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private String mAccount;
    private String mPhone;
    private String mPassword;
    private UserDao mUserDao;
    private Handler mHandler = new Handler()
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
                    Intent intent = new Intent(RegisterActivity.this, BilliardNearbyActivity.class);
                    startActivity(intent);
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
        mSharedPreferences = getSharedPreferences(PublicConstant.USERBASEUSER, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        mUserDao = new UserDaoImpl(this);

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

        mPreText = (TextView) findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mPreText.setText(getString(R.string.pre_register_text));

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
                mAccount = mEtUserName.getText().toString().trim();
                if(TextUtils.isEmpty(mAccount))
                {
                    toast(getString(R.string.account_null));
                    return ;
                }
                mPhone = mEtNumber.getText().toString().trim();
                if(TextUtils.isEmpty(mPhone))
                {
                    toast(getString(R.string.phone_null));
                    return ;
                }
                mPassword = mEtPwd.getText().toString().trim();
                if(TextUtils.isEmpty(mPassword))
                {
                    toast(getString(R.string.password_null));
                    return ;
                }
                if(mPassword.length() < 6 )
                {
                    toast(getString(R.string.please_input_password));
                    return ;
                }
                if(!mCheckBox.isChecked()){
                    toast(getString(R.string.please_read_article));
                    return;
                }

                Map<String, String> requestMap = new HashMap<String, String>();
                requestMap.put(HttpConstants.RegisterConstant.ACCOUNT, mAccount);
                requestMap.put(HttpConstants.RegisterConstant.PHONE, mPhone);
                requestMap.put(HttpConstants.RegisterConstant.SEX, mEtSex.getText().toString().trim().equals(
                        getString(R.string.man)) ? String.valueOf(1) : String.valueOf(2));
                requestMap.put(HttpConstants.RegisterConstant.PASSWORD, mPassword);

                Map<String,String> paramMap = new HashMap<String, String>();
                paramMap.put(PublicConstant.URL,HttpConstants.RegisterConstant.URL);
                paramMap.put(PublicConstant.METHOD,HttpConstants.RequestMethod.POST);
                if(Utils.networkAvaiable(RegisterActivity.this)) {
                    new RegisterAsyncTask(requestMap).execute(paramMap);
                }else{
                    Toast.makeText(RegisterActivity.this, getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.activity_register_et_sex:
                Intent intent = new Intent();
                intent.setClass(RegisterActivity.this,PlaySelectSex.class);
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


    private class RegisterAsyncTask extends AsyncTaskUtil<String>{

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
                if(object.getInt("code") != HttpConstants.ResponseCode.NORMAL)
                {
                    mHandler.obtainMessage(REGISTER_ERROR,object.getString("msg")).sendToTarget();
                }
                else
                {
                    Map<String,String> map = new HashMap<String, String>();
                    map.put(DatabaseConstant.UserTable.USERNAME,mAccount);
                    map.put(DatabaseConstant.UserTable.USER_ID,object.getJSONObject("result").
                            getString(DatabaseConstant.UserTable.USER_ID));
                    map.put(DatabaseConstant.UserTable.PHONE, object.getJSONObject("result").getString(DatabaseConstant.UserTable.PHONE));
                    map.put(DatabaseConstant.UserTable.LOGIN_TIME,object.getJSONObject("result").getString(DatabaseConstant.UserTable.LOGIN_TIME));
                    mHandler.obtainMessage(REGISTER_SUCCESS,map).sendToTarget();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
