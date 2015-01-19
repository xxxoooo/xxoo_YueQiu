package com.yueqiu.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ActionBar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.util.Utils;

public class BilliardGroupDetailActivity extends Activity implements View.OnClickListener{
    private View mPraiseView,mReplyView;
    private TextView mTvYueqiu, mTvYueqiuCircle, mTvFriendCircle,
            mTvWeichat, mTvQQZone, mTvTencentWeibo, mTvSinaWeibo, mTvRenren;
    private Button mBtnCancel;
    private Dialog mShareDlg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiard_group_detail);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.billiard_group_detail));

        initView();
    }

    private void initView(){
        mPraiseView = findViewById(R.id.billiard_group_praise_view);
        mReplyView = findViewById(R.id.billiard_group_reply_view);

        mPraiseView.setOnClickListener(this);
        mReplyView.setOnClickListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_billiard_group_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }else if(id == R.id.billiard_detail_action_share){
            mShareDlg = Utils.showSheet(this);
            mShareDlg.show();

        }

        return true;
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.billiard_group_praise_view:
                break;
            case R.id.billiard_group_reply_view:
                break;
        }
    }






}
