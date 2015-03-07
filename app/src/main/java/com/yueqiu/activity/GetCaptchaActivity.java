package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.MyURLSpan;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class GetCaptchaActivity extends Activity implements View.OnClickListener {

    private EditText mEtPhone;
    private EditText mEtCheckNum;
    private TextView mGetCheckNum, mTvLogin, mTvAgree;
    private Button mBtnNext;
    private String mPhone;
    private int mCode;
    private ActionBar mActionBar;
    private CheckBox mCheckBox;
    private ProgressBar mPreProgress;
    private Drawable mProgressDrawable;
    private Map<String,String> mParamMap = new HashMap<String, String>();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPreProgress.setVisibility(View.GONE);
            switch (msg.what) {

                case PublicConstant.GET_SUCCESS:
                    Bundle arg = (Bundle) msg.obj;
                    String smsCode = arg.getString("smsCode");
                    mCode = arg.getInt("code");
//                    if(smsCode.equals("false")){
//                        Utils.showToast(GetCaptchaActivity.this,getString(R.string.send_sms_failed));
//                    }else{
                        Utils.showToast(GetCaptchaActivity.this,getString(R.string.wait_for_captcha_sms));
//                    }
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(GetCaptchaActivity.this,getString(R.string.http_request_time_out));
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(GetCaptchaActivity.this,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(GetCaptchaActivity.this, (String) msg.obj);
                    }
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_with_captcha);

        initActionBar();
        initView();
    }

    private void initActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(getString(R.string.register));
    }

    private void initView(){

        mEtPhone = (EditText) findViewById(R.id.activity_checkphone_et_phone);
        mEtCheckNum = (EditText) findViewById(R.id.activity_checkphone_et_password);
        mGetCheckNum = (TextView) findViewById(R.id.tv_register_getchecknum);
        mBtnNext = (Button) findViewById(R.id.activity_checkphone_btn_register);
        mTvLogin = (TextView) findViewById(R.id.activity_checkphone_tv_login);
        mTvAgree = (TextView) findViewById(R.id.checkphone_read_and_agree_the_article);
        mCheckBox = (CheckBox) findViewById(R.id.checkphone_agree_article_check);
        SpannableString spanStr = new SpannableString(getString(R.string.already_read_and_agree_the_article));
        spanStr.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.have_agree_and_read)), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new MyURLSpan("file://android_asset/policy.html"), 7, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvAgree.setText(spanStr);
        mTvAgree.setMovementMethod(LinkMovementMethod.getInstance());
        mTvLogin.setOnClickListener(this);
        mGetCheckNum.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);

        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_register_getchecknum:
                mPhone = mEtPhone.getText().toString();
                if (mPhone.length() != 11) {
                    Utils.showToast(GetCaptchaActivity.this,getString(R.string.please_input_right_number));
                    return;
                }
                if(Utils.networkAvaiable(this)) {
                    getCaptcha();
                }else{
                    Utils.showToast(this,getString(R.string.network_not_available));
                }
                Utils.dismissInputMethod(this,mEtPhone);
                break;
            case R.id.activity_checkphone_btn_register:
                String code = mEtCheckNum.getText().toString().trim();
                if (code == null || code.equals("")) {
                    Utils.showToast(GetCaptchaActivity.this,getString(R.string.please_input_captcha));
                    return;
                }
                if (!code.equals(String.valueOf(mCode))) {
                    Utils.showToast(GetCaptchaActivity.this,getString(R.string.captcha_is_wrong));
                    return;
                }

                if (!mCheckBox.isChecked()) {
                    Utils.showToast(GetCaptchaActivity.this,getString(R.string.please_read_article));
                    return;
                }

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString(HttpConstants.RegisterConstant.PHONE, mPhone);
                bundle.putString(HttpConstants.RegisterConstant.VERFICATION_CODE, String.valueOf(mCode));
                intent.setClass(GetCaptchaActivity.this, Register1Activity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                break;
            case R.id.activity_checkphone_tv_login:
                this.finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
    }

    private void getCaptcha(){

        mPreProgress.setVisibility(View.VISIBLE);

        mParamMap.put(HttpConstants.Captcha.PHONE,mPhone);
        mParamMap.put(HttpConstants.Captcha.ACTION_TYPE,String.valueOf(3));

        //TODO:暂时用testHttp这个方法，等所有接口都部署到新服务器地址后改回requestHttp
        HttpUtil.testHttp(HttpConstants.Captcha.URL,mParamMap,HttpConstants.RequestMethod.POST,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","get captcha response ->" + response);
                try {
                    if(!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            Bundle arg = new Bundle();
                            arg.putString("smsCode",response.getJSONObject("result").getString("smsCode"));
                            arg.putInt("code", response.getJSONObject("result").getInt("code"));
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS,arg).sendToTarget();
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        }else {
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                } catch (JSONException e) {
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
}
