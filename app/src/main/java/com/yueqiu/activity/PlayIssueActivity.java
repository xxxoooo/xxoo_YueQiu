package com.yueqiu.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;

import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PlayIssueActivity extends FragmentActivity implements View.OnClickListener,DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
    private static final int START_FLAG = 0;
    private static final int END_FLAG   = 1;
    private EditText mTitleEdit,mContactEdit,mPhoneEdit,mIllustrationEdit;
    private TextView mLocationTv,mStartTimeTv,mEndTimeTv,mChargeModuleTv,mPreTextView;
    private String mContactStr,mPhoneNumberStr;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private StringBuilder mStartTimeStr = new StringBuilder(),
            mEndTimeStr = new StringBuilder();
    private int mTimeFlag;
    private TextView mEtActivityType;
    private ImageView mIvAddImg, mIvExpression;
    private static final int SELECT_TYPE = 0x02;
    private int mType = 0;
    private int mModel = 0;

    private ProgressBar mPreProgress;
    private Drawable mProgressDrawable;


    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case PublicConstant.GET_SUCCESS:
                    Toast.makeText(PlayIssueActivity.this,
                            getString(R.string.activity_submit_success),Toast.LENGTH_SHORT).show();
                    mPreProgress.setVisibility(View.GONE);
                    mPreTextView.setVisibility(View.GONE);
                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    break;
                case PublicConstant.REQUEST_ERROR:
                    Toast.makeText(PlayIssueActivity.this,
                            getString(R.string.activity_submit_failed), Toast.LENGTH_LONG).show();
                    mPreProgress.setVisibility(View.GONE);
                    mPreTextView.setVisibility(View.GONE);
                    break;
                case PublicConstant.TIME_OUT:
                    Toast.makeText(PlayIssueActivity.this,
                            getString(R.string.http_request_time_out), Toast.LENGTH_LONG).show();
                    mPreProgress.setVisibility(View.GONE);
                    mPreTextView.setVisibility(View.GONE);
                    break;

            }
        }
    };


    public PlayIssueActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_issues);
        initActionBar();
        initView();
    }
    private void initActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.search_publishing_dating_billiards_info_str));

    }
    private void initView(){
        mTitleEdit          = (EditText) findViewById(R.id.activitie_title_edit_text);
        mContactEdit    = (EditText) findViewById(R.id.activity_contact_edit_text);
        mPhoneEdit      = (EditText) findViewById(R.id.activity_contact_phone_edit_text);
        mIllustrationEdit   = (EditText) findViewById(R.id.activity_illustrate_edit_text);

        mStartTimeTv      = (TextView) findViewById(R.id.activity_start_time_text);
        mEndTimeTv        = (TextView) findViewById(R.id.activity_end_time_text);
        mChargeModuleTv   = (TextView) findViewById(R.id.activity_charge_module_text);
        mLocationTv       = (TextView) findViewById(R.id.activity_location_text);

        mEtActivityType = (TextView) findViewById(R.id.activitie_title_edit_type);
        mIvAddImg = (ImageView) findViewById(R.id.activitiy_issues_iv_add_img);
        mIvExpression = (ImageView) findViewById(R.id.activity_issues_expression);

        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mPreTextView = (TextView) findViewById(R.id.pre_text);
        mPreTextView.setText(getString(R.string.activity_issuing));

        final Calendar calendar = Calendar.getInstance();

        mDatePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        mTimePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);

        mContactStr = YueQiuApp.sUserInfo.getUsername();
        mPhoneNumberStr = YueQiuApp.sUserInfo.getPhone();

        mContactEdit.setText(mContactStr);
        mPhoneEdit.setText(mPhoneNumberStr);

        mLocationTv.setOnClickListener(this);
        mStartTimeTv.setOnClickListener(this);
        mEndTimeTv.setOnClickListener(this);
        mChargeModuleTv.setOnClickListener(this);
        mEtActivityType.setOnClickListener(this);
        mIvExpression.setOnClickListener(this);
        mIvAddImg.setOnClickListener(this);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.top_in,R.anim.top_out);
                break;
            case R.id.issue_activity:
                final Map<String, Object> requests = getActivityInfo();
                if(Utils.networkAvaiable(PlayIssueActivity.this)) {
                    mPreProgress.setVisibility(View.VISIBLE);
                    mPreTextView.setVisibility(View.VISIBLE);
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();

                            if (requests != null) {
                                String result = HttpUtil.urlClient(HttpConstants.Play.PUBLISH, requests, HttpConstants.RequestMethod.GET);
                                if (result != null) {
                                    JSONObject object = Utils.parseJson(result);
                                    Message msg = new Message();
                                    try {
                                        if (!object.isNull("code")) {
                                            if (object.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                                                msg.what = PublicConstant.GET_SUCCESS;
                                            } else if (object.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                                                msg.what = PublicConstant.TIME_OUT;
                                            } else {
                                                msg.what = PublicConstant.REQUEST_ERROR;
                                            }
                                        }
                                        mHandler.sendMessage(msg);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }.start();
                }else{
                    Toast.makeText(PlayIssueActivity.this,getString(R.string.network_not_available),Toast.LENGTH_SHORT).show();
                }

                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.setFragmentActivityMenuColor(this);
        getMenuInflater().inflate(R.menu.issue_activity,menu);
        return true;

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.activity_issues_expression:
                break;
            case R.id.activitiy_issues_iv_add_img:
                break;
            case R.id.activitie_title_edit_type:
                Intent intentType = new Intent();
                intentType.setClass(this,PlaySelectType.class);
                intentType.putExtra("type",getType(mEtActivityType.getText().toString().trim()));
                startActivityForResult(intentType, SELECT_TYPE);
                overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
                break;

            case R.id.activity_location_text:
                break;
            case R.id.activity_start_time_text:
                mDatePickerDialog.setVibrate(false);
                mDatePickerDialog.setYearRange(1985, 2028);
                mDatePickerDialog.setCloseOnSingleTapDay(false);
                mDatePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                mTimeFlag = START_FLAG;
                mStartTimeStr.delete(0,mStartTimeStr.length());
                break;
            case R.id.activity_end_time_text:
                mDatePickerDialog.setVibrate(false);
                mDatePickerDialog.setYearRange(1985, 2028);
                mDatePickerDialog.setCloseOnSingleTapDay(false);
                mDatePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                mTimeFlag = END_FLAG;
                mEndTimeStr.delete(0,mEndTimeStr.length());
                break;
            case R.id.activity_charge_module_text:
                Intent intent = new Intent();
                intent.setClass(this,SelectChargeModuleActivity.class);
                if(mChargeModuleTv.getText().equals(getString(R.string.charge_module_free))){
                    intent.putExtra(SelectChargeModuleActivity.MODULE_KEY,SelectChargeModuleActivity.MODULE_FREE);
                    mModel = SelectChargeModuleActivity.MODULE_FREE;
                }else if(mChargeModuleTv.getText().equals(getString(R.string.charge_module_pay))){
                    intent.putExtra(SelectChargeModuleActivity.MODULE_KEY,SelectChargeModuleActivity.MODULE_PAY);
                    mModel = SelectChargeModuleActivity.MODULE_PAY;
                }else{
                   intent.putExtra(SelectChargeModuleActivity.MODULE_KEY,SelectChargeModuleActivity.MODULE_AA);
                    mModel = SelectChargeModuleActivity.MODULE_AA;
                }
                startActivityForResult(intent,0);
                overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
                break;
        }
    }

    private Map<String, Object> getActivityInfo()
    {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("user_id",YueQiuApp.sUserInfo.getUser_id());
        if(mType == 0)
        {
            Toast.makeText(PlayIssueActivity.this,getString(R.string.please_write_type),Toast.LENGTH_SHORT).show();
            return null;
        }
        map.put("type",mType);
        String title = mTitleEdit.getText().toString().trim();
        if(title.equals(""))
        {
            Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_title_cannot_empty),Toast.LENGTH_SHORT).show();
            return null;
        }
        if(title.length() < 4){
            Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_title_length_less),Toast.LENGTH_SHORT).show();
            return null;
        }
        if(title.length() > 30){
            Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_title_length_more),Toast.LENGTH_SHORT).show();
            return null;
        }
        map.put("title", title);
        int address = 0;
        map.put("address",address);
        String beginTime = mStartTimeTv.getText().toString().trim();
        if(beginTime.equals(""))
        {
            Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_start_time_cannot_empty),Toast.LENGTH_SHORT).show();
            return null;
        }
        map.put("begin_time",beginTime);
        String datetime = mEndTimeTv.getText().toString().trim();
        if(datetime.equals(""))
        {
            Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_end_time_cannot_empty),Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            if(Utils.stringToLong(beginTime,"yyyy-MM-dd HH-mm") > Utils.stringToLong(datetime,"yyyy-MM-dd HH-mm")){

                Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_start_cannot_more_than_end),Toast.LENGTH_SHORT).show();
                return null;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        map.put("datetime",datetime);

        if(mModel == 0)
        {
            Toast.makeText(PlayIssueActivity.this,getString(R.string.activity_charge_module_cannot_empty),Toast.LENGTH_SHORT).show();
            return null;
        }
        map.put("model", mModel);
        String content = mIllustrationEdit.getText().toString();
        if(content.equals(""))
        {
            Toast.makeText(PlayIssueActivity.this,getString(R.string.please_write_content),Toast.LENGTH_SHORT).show();
            return null;
        }
        map.put("content", content);
        map.put("lat",  0);
        map.put("lng", 0);
        return map;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            int module = data.getIntExtra(SelectChargeModuleActivity.MODULE_KEY,SelectChargeModuleActivity.MODULE_FREE);
            if(module == SelectChargeModuleActivity.MODULE_FREE){
                mChargeModuleTv.setText(getString(R.string.charge_module_free));
                mModel = SelectChargeModuleActivity.MODULE_FREE;
            }else if(module == SelectChargeModuleActivity.MODULE_PAY){
                mModel = SelectChargeModuleActivity.MODULE_PAY;
                mChargeModuleTv.setText(getString(R.string.charge_module_pay));
            }else{
                mChargeModuleTv.setText(getString(R.string.charge_module_aa));
                mModel = SelectChargeModuleActivity.MODULE_AA;
            }
        }


        else if(requestCode == SELECT_TYPE && resultCode == RESULT_OK)
        {
            String type = data.getStringExtra("type");
            if(type.equals("0")) {
                mEtActivityType.setText(getString(R.string.group_activity));
                mType = 1;
            }
            else if(type.equals("1")) {
                mEtActivityType.setText(getString(R.string.meet_star));
                mType = 2;
            }
            else if(type.equals("2")) {
                mEtActivityType.setText(getString(R.string.billiard_show));
                mType = 3;
            }
            else if(type.equals("3")) {
                mEtActivityType.setText(getString(R.string.complete));
                mType = 4;
            }
            else if(type.equals("4")) {
                mEtActivityType.setText(getString(R.string.billiard_other));
                mType = 5;
            }
        }
    }

    private String getType(String type)
    {
        if(type.equals(getString(R.string.group_activity)))
            return "0";
        else if(type.equals(getString(R.string.meet_star)))
            return "1";
        else if(type.equals(getString(R.string.billiard_show)))
            return "2";
        else if(type.equals(getString(R.string.complete)))
            return "3";
        else if(type.equals(getString(R.string.billiard_other)))
            return "4";
        else if(type.equals(""));
            return "0";
    }


    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        String monthStr = month < 9 ? "0" + ++month : String.valueOf(++month);
        String dayStr = day < 10 ? "0" + day : String.valueOf(day);
        if(mTimeFlag == START_FLAG){
            mStartTimeStr.append(year).append("-").append(monthStr).append("-").append(dayStr);
        }else if(mTimeFlag == END_FLAG){
            mEndTimeStr.append(year).append("-").append(monthStr).append("-").append(dayStr);
        }
        mTimePickerDialog.setVibrate(false);
        mTimePickerDialog.setCloseOnSingleTapMinute(false);
        mTimePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
    }


    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        String hourStr = hourOfDay < 10 ? "0" + hourOfDay : String.valueOf(hourOfDay);
        String minuteStr = minute < 10 ? "0" + minute : String.valueOf(minute);
        if(mTimeFlag == START_FLAG){
            mStartTimeStr.append(" ").append(hourStr).append("-").append(minuteStr);
            mStartTimeTv.setText(mStartTimeStr.toString());
        }else if(mTimeFlag == END_FLAG){
            mEndTimeStr.append(" ").append(hourStr).append("-").append(minuteStr);
            mEndTimeTv.setText(mEndTimeStr.toString());

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.top_in,R.anim.top_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
