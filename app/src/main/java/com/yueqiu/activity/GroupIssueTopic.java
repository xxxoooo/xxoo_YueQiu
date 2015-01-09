package com.yueqiu.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ActionBar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.yueqiu.R;
import com.yueqiu.util.Utils;

public class GroupIssueTopic extends Activity implements View.OnClickListener{

    private EditText    mTitleEdit,mContentEdit;
    private TextView    mTopicType;
    private ImageView   mIvExpression,mIvAddImg;
    private View        mLinearType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_issue_topic);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initView();
    }

    private void initView(){
        mTitleEdit = (EditText) findViewById(R.id.group_issue_title);
        mContentEdit = (EditText) findViewById(R.id.group_issue_content);
        mTopicType = (TextView) findViewById(R.id.group_issue_type);
        mLinearType = findViewById(R.id.group_issue_type_linear);

        mIvExpression = (ImageView) findViewById(R.id.group_issue_express);
        mIvAddImg = (ImageView) findViewById(R.id.group_issue_add_img);

        mLinearType.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Utils.setActivityMenuColor(this);
        getMenuInflater().inflate(R.menu.menu_group_issue_topic, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
            //overridePendingTransition(R.anim.push_up_out, R.anim.push_up_in);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.group_issue_type_linear:
                Intent intent = new Intent();
                intent.setClass(this,GroupSelectTopicTypeActivity.class);
                if(mTopicType.getText().equals(getString(R.string.billiard_get_master))){
                    intent.putExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.GET_MASTER);
                }else if(mTopicType.getText().equals(getString(R.string.billiard_be_master))){
                    intent.putExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.BE_MASTER);
                }else if(mTopicType.getText().equals(getString(R.string.billiard_find_friend))){
                    intent.putExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.FIND_FRIEND);
                }else if(mTopicType.getText().equals(getString(R.string.billiard_equipment))){
                    intent.putExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.EQUIP);
                }else{
                    intent.putExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.OTHER);
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
            int type = data.getIntExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.GET_MASTER);
            if(type == GroupSelectTopicTypeActivity.GET_MASTER){
                mTopicType.setText(getString(R.string.billiard_get_master));
            }else if(type == GroupSelectTopicTypeActivity.BE_MASTER){
                mTopicType.setText(getString(R.string.billiard_be_master));
            }else if(type == GroupSelectTopicTypeActivity.FIND_FRIEND){
                mTopicType.setText(getString(R.string.billiard_find_friend));
            }else if(type == GroupSelectTopicTypeActivity.EQUIP){
                mTopicType.setText(getString(R.string.billiard_equipment));
            }else{
                mTopicType.setText(getString(R.string.billiard_other));
            }
        }
    }
}
