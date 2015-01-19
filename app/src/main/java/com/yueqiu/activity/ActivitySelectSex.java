package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.yueqiu.R;
import com.yueqiu.constant.HttpConstants;

/**
 * Created by yinfeng on 15/1/3.
 */
public class ActivitySelectSex extends Activity implements View.OnClickListener{
    private static final String TAG = "ActivitySelectSex";
    private RelativeLayout mMan;
    private RelativeLayout mWoman;
    private ImageView mIvMan;
    private ImageView mIvWoman;
    private Intent mIntent;
    private int mSex;//0 man , 1 woman

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sex);
        initView();
        initActionBar();
        mIntent = getIntent();
        mSex = mIntent.getIntExtra(HttpConstants.RegisterConstant.SEX,0);
        Log.i(TAG,String.valueOf(mSex));
        if(mSex == 0)
        {
            mIvMan.setVisibility(View.VISIBLE);
        }
        else
        {
            mIvWoman.setVisibility(View.VISIBLE);
        }

    }

    private void initView()
    {
        mMan = (RelativeLayout) findViewById(R.id.activity_select_man);
        mWoman = (RelativeLayout) findViewById(R.id.activity_select_woman);
        mIvMan = (ImageView) findViewById(R.id.activity_select_sex_iv_man);
        mIvWoman = (ImageView) findViewById(R.id.activity_select_sex_iv_woman);
        mIvMan.setVisibility(View.GONE);
        mIvWoman.setVisibility(View.GONE);
        mMan.setOnClickListener(this);
        mWoman.setOnClickListener(this);
    }

    private void initActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.select_sex));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mIntent = new Intent();
                mIntent.putExtra(HttpConstants.RegisterConstant.SEX, mSex);
                setResult(RESULT_OK, mIntent);
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {
        mIntent = new Intent();

        switch (v.getId())
        {
            case R.id.activity_select_man:
                mIntent.putExtra(HttpConstants.RegisterConstant.SEX, 0);
                break;
            case R.id.activity_select_woman:
                mIntent.putExtra(HttpConstants.RegisterConstant.SEX, 1);
                break;
        }

        setResult(RESULT_OK, mIntent);
        finish();
        overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                mIntent = new Intent();
                mIntent.putExtra(HttpConstants.RegisterConstant.SEX, mSex);
                setResult(RESULT_OK, mIntent);
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
