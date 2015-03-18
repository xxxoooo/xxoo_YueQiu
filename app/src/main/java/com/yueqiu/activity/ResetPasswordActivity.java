package com.yueqiu.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.ActionBar;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResetPasswordActivity extends Activity implements View.OnClickListener{
    private EditText mNewPwd,mAgainPwd;
    private TextView mRegister,mPreText;
    private Button mSure;
    private String mPhone,mNewPassword;
    private int mCode;
    private ProgressBar mPreProgress;
    private Drawable mProgressDrawable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mPhone = getIntent().getStringExtra(HttpConstants.ResetPwd.PHONE);
        mCode = getIntent().getIntExtra(HttpConstants.ResetPwd.VERFICATION, 0);

        initActionBar();
        initView();
    }

    private void initActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.reset_pwd));
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void initView(){
        mNewPwd = (EditText) findViewById(R.id.reset_new_pwd);
        mAgainPwd = (EditText) findViewById(R.id.reset_again_pwd);
        mRegister = (TextView) findViewById(R.id.reset_register);
        mSure = (Button) findViewById(R.id.reset_sure);

        mPreText = (TextView) findViewById(R.id.pre_text);
        mPreText.setText(getString(R.string.resetting));
        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);

        mRegister.setOnClickListener(this);
        mSure.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.reset_register:
                Intent registerIntent = new Intent(this,GetCaptchaActivity.class);
                registerIntent.putExtra(PublicConstant.GET_CAPTCHA_TYPE,PublicConstant.GET_CAPTCHA_TYPE_REGIST);
                startActivity(registerIntent);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                break;
            case R.id.reset_sure:
                resetPwd();
                break;
        }
    }
    private void resetPwd(){

        mNewPassword = mNewPwd.getText().toString();
        if(TextUtils.isEmpty(mNewPassword)){
            Utils.showToast(this,getString(R.string.new_pwd_can_not_empty));
            return;
        }
        String againPwd = mAgainPwd.getText().toString();
        if(TextUtils.isEmpty(againPwd)){
            Utils.showToast(this,getString(R.string.please_input_new_pwd_again));
            return;
        }

        if(!againPwd.equals(mNewPassword)){
            Utils.showToast(this,getString(R.string.two_pwd_not_same));
            return;
        }

        Map<String,String> param = new HashMap<String, String>();
        param.put(HttpConstants.ResetPwd.PHONE,mPhone);
        param.put(HttpConstants.ResetPwd.VERFICATION, String.valueOf(mCode));
        param.put(HttpConstants.ResetPwd.PASSWORD,mNewPassword);

        Log.d("wy","param is -》" + param);

        mPreProgress.setVisibility(View.VISIBLE);
        mPreText.setVisibility(View.VISIBLE);
        mSure.setEnabled(false);

        HttpUtil.requestHttp(HttpConstants.ResetPwd.URL,param,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","reset response ->" + response);
                try{
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            mHandler.sendEmptyMessage(PublicConstant.GET_SUCCESS);
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPreProgress.setVisibility(View.GONE);
            mPreText.setVisibility(View.GONE);
            mSure.setEnabled(true);
            switch (msg.what){
                case PublicConstant.GET_SUCCESS:
                    Utils.showToast(ResetPasswordActivity.this,getString(R.string.reset_success));
                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(ResetPasswordActivity.this,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(ResetPasswordActivity.this, (String) msg.obj);
                    }
                    break;
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return true;
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
