package com.yueqiu.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.app.ActionBar;

import com.yueqiu.R;

/**
 * Created by wangyun on 15/1/5.
 */
public class SelectChargeModuleActivity extends Activity implements View.OnClickListener{
    public  static final String MODULE_KEY = "charge_module";
    public  static final int MODULE_FREE = 0;
    public  static final int MODULE_PAY  = 1;
    public  static final int MODULE_AA   = 2;
    private View mFree,mPay,mAA;
    private ImageView mIvFree,mIvPay,mIvAA;
    private ActionBar mActionBar;
    private Intent mIntent;
    private int mChargeModule;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_charge_module);

        mActionBar = getActionBar();
        mActionBar.setTitle(getString(R.string.activities_charge_module));
        mActionBar.setDisplayHomeAsUpEnabled(true);

        mFree = findViewById(R.id.activity_charge_module_free);
        mPay = findViewById(R.id.activity_charge_module_pay);
        mAA = findViewById(R.id.activity_charge_module_aa);

        mIvFree = (ImageView) findViewById(R.id.activity_charge_module_iv_free);
        mIvPay = (ImageView) findViewById(R.id.activity_charge_module_iv_pay);
        mIvAA = (ImageView) findViewById(R.id.activity_charge_module_iv_aa);

        mFree.setOnClickListener(this);
        mPay.setOnClickListener(this);
        mAA.setOnClickListener(this);

        mIntent = getIntent();
        mChargeModule = mIntent.getIntExtra(MODULE_KEY,0);
        if(mChargeModule == MODULE_FREE){
            mIvPay.setVisibility(View.GONE);
            mIvAA.setVisibility(View.GONE);
            mIvFree.setVisibility(View.VISIBLE);
        }else if(mChargeModule == MODULE_PAY){
            mIvFree.setVisibility(View.GONE);
            mIvAA.setVisibility(View.GONE);
            mIvPay.setVisibility(View.VISIBLE);
        }else{
            mIvFree.setVisibility(View.GONE);
            mIvPay.setVisibility(View.GONE);
            mIvAA.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        mIntent = new Intent();
        switch(v.getId()){
            case R.id.activity_charge_module_free:
                mIntent.putExtra(MODULE_KEY,MODULE_FREE);
                break;
            case R.id.activity_charge_module_pay:
                mIntent.putExtra(MODULE_KEY,MODULE_PAY);
                break;
            case R.id.activity_charge_module_aa:
                mIntent.putExtra(MODULE_KEY,MODULE_AA);
                break;
        }
        setResult(RESULT_OK, mIntent);
        finish();
        overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mIntent = new Intent();
                mIntent.putExtra(MODULE_KEY, mChargeModule);
                setResult(RESULT_OK, mIntent);
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return super.onOptionsItemSelected(item);
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
