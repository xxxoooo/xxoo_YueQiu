package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.yueqiu.ActivitiesActivity;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;

/**
 * Created by yinfeng on 14/12/19.
 */
public class ActivitiesIssueActivity extends Activity implements View.OnClickListener{

    private EditText mTitle,mContactEdit,mPhoneEdit,mIllustration;
    private TextView mLocation,mStartTime,mEndTime,mChargeModule;
    private String mAccount;
    private String mPhoneNumber;

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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.issue_activity,menu);
        return super.onCreateOptionsMenu(menu);

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
                break;
            case R.id.activity_end_time_text:
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
}
