package com.yueqiu.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.util.Utils;
import java.util.Calendar;

/**
 * Created by yinfeng on 14/12/19.
 */
public class ActivitiesIssueActivity extends FragmentActivity implements View.OnClickListener,DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
    private static final int START_FLAG = 0;
    private static final int END_FLAG   = 1;
    private EditText mTitle,mContactEdit,mPhoneEdit,mIllustration;
    private TextView mLocation,mStartTime,mEndTime,mChargeModule;
    private String mAccount;
    private String mPhoneNumber;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private StringBuilder mStartTimeStr = new StringBuilder(),
            mEndTimeStr = new StringBuilder();
    private int mTimeFlag;

    public ActivitiesIssueActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issues);
        initActionBar();
        initView();
    }
    private void initActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.search_publishing_dating_billiards_info_str));

    }
    private void initView(){
        mTitle = (EditText) findViewById(R.id.activitie_title_edit_text);
        mLocation = (TextView) findViewById(R.id.activity_location_text);
        mContactEdit = (EditText) findViewById(R.id.activity_contact_edit_text);
        mPhoneEdit = (EditText) findViewById(R.id.activity_contact_phone_edit_text);

        mStartTime = (TextView) findViewById(R.id.activity_start_time_text);
        mEndTime = (TextView) findViewById(R.id.activity_end_time_text);
        mChargeModule = (TextView) findViewById(R.id.activity_charge_module_text);

        final Calendar calendar = Calendar.getInstance();

        mDatePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        mTimePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);

        mAccount = YueQiuApp.sUserInfo.getAccount();
        mPhoneNumber = YueQiuApp.sUserInfo.getPhone();

        mContactEdit.setText(mAccount);
        mPhoneEdit.setText(mPhoneNumber);

        mLocation.setOnClickListener(this);
        mStartTime.setOnClickListener(this);
        mEndTime.setOnClickListener(this);
        mChargeModule.setOnClickListener(this);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                finish();
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

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
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
                if(mChargeModule.getText().equals(getString(R.string.charge_module_free))){
                    intent.putExtra(SelectChargeModuleActivity.MODULE_KEY,SelectChargeModuleActivity.MODULE_FREE);
                }else if(mChargeModule.getText().equals(getString(R.string.charge_module_pay))){
                    intent.putExtra(SelectChargeModuleActivity.MODULE_KEY,SelectChargeModuleActivity.MODULE_PAY);
                }else{
                    intent.putExtra(SelectChargeModuleActivity.MODULE_KEY,SelectChargeModuleActivity.MODULE_AA);
                }
                startActivityForResult(intent,0);
                overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            int module = data.getIntExtra(SelectChargeModuleActivity.MODULE_KEY,SelectChargeModuleActivity.MODULE_FREE);
            if(module == SelectChargeModuleActivity.MODULE_FREE){
                mChargeModule.setText(getString(R.string.charge_module_free));
            }else if(module == SelectChargeModuleActivity.MODULE_PAY){
                mChargeModule.setText(getString(R.string.charge_module_pay));
            }else{
                mChargeModule.setText(getString(R.string.charge_module_aa));
            }
        }
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
            mStartTime.setText(mStartTimeStr.toString());
        }else if(mTimeFlag == END_FLAG){
            mEndTimeStr.append(" ").append(hourStr).append("-").append(minuteStr);
            mEndTime.setText(mEndTimeStr.toString());
        }
    }
}
