package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.yueqiu.R;

/**
 * Created by yinfeng on 15/1/9.
 */
public class ActivitySelectType extends Activity implements View.OnClickListener {
    private View type00, type01, type02, type03, type04;
    private static final String TYPE = "type";
    private ImageView iv00, iv01, iv02, iv03, iv04;
    private ActionBar mActionBar;
    private Intent mIntent;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_activities_type);
        initActionBar();
        initView();
        mIntent = getIntent();
        if (mIntent.getStringExtra(TYPE) == null || mIntent.getStringExtra(TYPE).equals("")) {
            iv00.setVisibility(View.VISIBLE);
            index = 0;
        } else if (mIntent.getStringExtra(TYPE).equals("1")) {
            iv01.setVisibility(View.VISIBLE);
            index = 1;
        } else if (mIntent.getStringExtra(TYPE).equals("2")) {
            iv02.setVisibility(View.VISIBLE);
            index = 2;
        } else if (mIntent.getStringExtra(TYPE).equals("3")) {
            iv03.setVisibility(View.VISIBLE);
            index = 3;
        } else if (mIntent.getStringExtra(TYPE).equals("4")) {
            iv04.setVisibility(View.VISIBLE);
            index = 4;
        } else {
            iv00.setVisibility(View.VISIBLE);
            index = 0;
        }

    }

    private void initView() {
        type00 = (View) findViewById(R.id.activity_type00);
        type01 = (View) findViewById(R.id.activity_type01);
        type02 = (View) findViewById(R.id.activity_type02);
        type03 = (View) findViewById(R.id.activity_type03);
        type04 = (View) findViewById(R.id.activity_type04);
        iv00 = (ImageView) findViewById(R.id.iv_activity_select_type_00);
        iv01 = (ImageView) findViewById(R.id.iv_activity_select_type_01);
        iv02 = (ImageView) findViewById(R.id.iv_activity_select_type_02);
        iv03 = (ImageView) findViewById(R.id.iv_activity_select_type_03);
        iv04 = (ImageView) findViewById(R.id.iv_activity_select_type_04);
        type00.setOnClickListener(this);
        type01.setOnClickListener(this);
        type02.setOnClickListener(this);
        type03.setOnClickListener(this);
        type04.setOnClickListener(this);

    }


    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.select_activity_type));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finishThis(index);
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finishThis(index);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void finishThis(int index) {
        mIntent = new Intent();
        mIntent.putExtra(TYPE,String.valueOf(index));
        setResult(RESULT_OK, mIntent);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.activity_type00:
                finishThis(0);
                break;
            case R.id.activity_type01:
                finishThis(1);
                break;
            case R.id.activity_type02:
                finishThis(2);
                break;
            case R.id.activity_type03:
                finishThis(3);
                break;
            case R.id.activity_type04:
                finishThis(4);
                break;
        }
    }
}
