package com.yueqiu.activity.searchmenu;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yueqiu.R;

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
        mSex = mIntent.getIntExtra("sex",0);
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
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.custom_actionbar_layout, null);
            View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
            saveMenuItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIntent = new Intent();
                    mIntent.putExtra("sex", mSex);
                    setResult(RESULT_OK,mIntent);
                    ActivitySelectSex.this.finish();
                    overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
                }
            });
            TextView title = (TextView) customActionBarView.findViewById(R.id.action_bar_title);
            title.setText(getString(R.string.gender));
            actionBar.setDisplayShowCustomEnabled(true);
            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(customActionBarView,params);
        }
    }


    @Override
    public void onClick(View v) {
        mIntent = new Intent();

        switch (v.getId())
        {
            case R.id.activity_select_man:
                mIntent.putExtra("sex", 0);
                break;
            case R.id.activity_select_woman:
                mIntent.putExtra("sex", 1);
                break;
        }

        setResult(RESULT_OK, mIntent);
        finish();
        overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
    }
}
