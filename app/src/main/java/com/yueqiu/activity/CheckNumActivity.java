package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.MyURLSpan;
import com.yueqiu.view.YueQiuDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yinfeng on 15/1/17.
 */
public class CheckNumActivity extends Activity implements View.OnClickListener {

    private EditText mEtPhone;
    private EditText mEtCheckNum;
    private TextView mGetCheckNum, mTvLogin, mTvAgree;
    private Button mBtnNext;
    private String mPhone;
    private String mCode;
    private ActionBar mActionBar;
    private CheckBox mCheckBox;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case PublicConstant.GET_SUCCESS:
                    mCode = (String) msg.obj;
//                    new AlertDialog.Builder(CheckNumActivity.this).
//                            setMessage("验证码为：" +  msg.obj).create().show();
//                    Toast.makeText(CheckNumActivity.this,
//                            "验证码为：" + msg.obj, Toast.LENGTH_LONG).show();
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(CheckNumActivity.this,getString(R.string.http_request_time_out));
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(CheckNumActivity.this,getString(R.string.retry_after_seconds,msg.obj));
                    }else{
                        Utils.showToast(CheckNumActivity.this,getString(R.string.http_request_error));
                    }
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkphone);
        mCode = new String();
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
        initActionBar();
    }

    private void initActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(getString(R.string.register));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_register_getchecknum:
                mPhone = mEtPhone.getText().toString();
                if (mPhone.length() != 11) {
                    Utils.showToast(CheckNumActivity.this,getString(R.string.please_input_right_number));
                    return;
                }
                new Thread(getCheckNum).start();
                break;
            case R.id.activity_checkphone_btn_register:
                String code = mEtCheckNum.getText().toString().trim();
                if (code == null || code.equals("")) {
                    Utils.showToast(CheckNumActivity.this,getString(R.string.please_input_captcha));
                    return;
                }
                if (!code.equals(mCode)) {
                    Utils.showToast(CheckNumActivity.this,getString(R.string.captcha_is_wrong));
                    return;
                }

                if (!mCheckBox.isChecked()) {
                    Utils.showToast(CheckNumActivity.this,getString(R.string.please_read_article));
                    return;
                }

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString(HttpConstants.RegisterConstant.PHONE, mPhone);
                bundle.putString(HttpConstants.RegisterConstant.VERFICATION_CODE, mCode);
                intent.setClass(CheckNumActivity.this, Register1Activity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                break;
            case R.id.activity_checkphone_tv_login:
                this.finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
    }

    private Runnable getCheckNum = new Runnable() {
        @Override
        public void run() {
            Map<String, String> map = new HashMap<String, String>();
            map.put(HttpConstants.RegisterConstant.PHONE, mPhone);
            map.put(HttpConstants.RegisterConstant.ACTION_TYPE, "3");
            String retStr = HttpUtil.urlClient(HttpConstants.RegisterConstant.CODEURL,
                    map, HttpConstants.RequestMethod.POST);
            JSONObject object = Utils.parseJson(retStr);
            Message msg = new Message();
            try {
                if(!object.isNull("code")) {
                    if (object.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                        msg.what = PublicConstant.GET_SUCCESS;
                        msg.obj = object.getJSONObject("result").getString("code");
                    }else if(object.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                        msg.what = PublicConstant.TIME_OUT;
                    }else {
                        msg.what = PublicConstant.REQUEST_ERROR;
                        msg.obj = object.getJSONObject("result").getString("leftTime");
                    }
                }else{
                    msg.what = PublicConstant.REQUEST_ERROR;
                }
                handler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
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
}
