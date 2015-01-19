package com.yueqiu.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.yueqiu.R;

public class GroupSelectTopicTypeActivity extends Activity implements View.OnClickListener{
    public static final String TOPIC_TYPE_KEY = "topic_type";
    public static final int GET_MASTER  =  1;
    public static final int BE_MASTER   =  2;
    public static final int FIND_FRIEND =  3;
    public static final int EQUIP       =  4;
    public static final int OTHER       =  5;

    private View mGetMaster,mBeMaster,mFindFriend,mEquip,mOther;
    private ImageView mIvGetMaster,mIvBeMaster,mIvFindFriend,mIvEquip,mIvOther;
    private Intent mIntent;
    private int mTopicType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_select_topic_type);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.billiard_topic_type));

        mGetMaster  = findViewById(R.id.group_topic_type_get_master);
        mBeMaster   = findViewById(R.id.group_topic_type_be_master);
        mFindFriend = findViewById(R.id.group_topic_type_find_friend);
        mEquip      = findViewById(R.id.group_topic_type_equip);
        mOther      = findViewById(R.id.group_topic_type_other);

        mIvGetMaster  = (ImageView) findViewById(R.id.group_get_master_iv);
        mIvBeMaster   = (ImageView) findViewById(R.id.group_be_master_iv);
        mIvFindFriend = (ImageView) findViewById(R.id.group_fin_friend_iv);
        mIvEquip      = (ImageView) findViewById(R.id.group_equipment_iv);
        mIvOther      = (ImageView) findViewById(R.id.group_other_iv);

        mGetMaster.setOnClickListener(this);
        mBeMaster.setOnClickListener(this);
        mFindFriend.setOnClickListener(this);
        mEquip.setOnClickListener(this);
        mOther.setOnClickListener(this);

        mIntent = getIntent();
        mTopicType = mIntent.getIntExtra(TOPIC_TYPE_KEY,1);
        if(mTopicType == GET_MASTER){
            mIvBeMaster.setVisibility(View.GONE);
            mIvFindFriend.setVisibility(View.GONE);
            mIvEquip.setVisibility(View.GONE);
            mIvOther.setVisibility(View.GONE);
            mIvGetMaster.setVisibility(View.VISIBLE);
        }else if(mTopicType == BE_MASTER){
            mIvBeMaster.setVisibility(View.VISIBLE);
            mIvFindFriend.setVisibility(View.GONE);
            mIvEquip.setVisibility(View.GONE);
            mIvOther.setVisibility(View.GONE);
            mIvGetMaster.setVisibility(View.GONE);
        }else if(mTopicType == FIND_FRIEND){
            mIvBeMaster.setVisibility(View.GONE);
            mIvFindFriend.setVisibility(View.VISIBLE);
            mIvEquip.setVisibility(View.GONE);
            mIvOther.setVisibility(View.GONE);
            mIvGetMaster.setVisibility(View.GONE);
        }else if(mTopicType == EQUIP){
            mIvBeMaster.setVisibility(View.GONE);
            mIvFindFriend.setVisibility(View.GONE);
            mIvEquip.setVisibility(View.VISIBLE);
            mIvOther.setVisibility(View.GONE);
            mIvGetMaster.setVisibility(View.GONE);
        }else{
            mIvBeMaster.setVisibility(View.GONE);
            mIvFindFriend.setVisibility(View.GONE);
            mIvEquip.setVisibility(View.GONE);
            mIvOther.setVisibility(View.VISIBLE);
            mIvGetMaster.setVisibility(View.GONE);
        }

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
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

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.group_topic_type_get_master:
                mIntent.putExtra(TOPIC_TYPE_KEY,GET_MASTER);
                break;
            case R.id.group_topic_type_be_master:
                mIntent.putExtra(TOPIC_TYPE_KEY,BE_MASTER);
                break;
            case R.id.group_topic_type_find_friend:
                mIntent.putExtra(TOPIC_TYPE_KEY,FIND_FRIEND);
                break;
            case R.id.group_topic_type_equip:
                mIntent.putExtra(TOPIC_TYPE_KEY,EQUIP);
                break;
            case R.id.group_topic_type_other:
                mIntent.putExtra(TOPIC_TYPE_KEY,OTHER);
                break;
        }
        setResult(RESULT_OK, mIntent);
        finish();
        overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
    }
}
