package com.yueqiu.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ActionBar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GroupIssueTopic extends Activity implements View.OnClickListener{

    private EditText    mTitleEdit,mContentEdit;
    private TextView    mTopicTypeTv;
    private ImageView   mIvExpression,mIvAddImg;
    private View        mLinearType;

    private ProgressBar mPreProgress;
    private TextView mPreText;
    private Drawable mProgressDrawable;

    private String mTitle;
    private String mContent;
    private int mTopicType;
    private Map<String,String> mParamsMap = new HashMap<String, String>();
    private Map<String,String>  mUrlAndMethodMap = new HashMap<String, String>();


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
        mTopicTypeTv = (TextView) findViewById(R.id.group_issue_type);
        mLinearType = findViewById(R.id.group_issue_type_linear);

        mIvExpression = (ImageView) findViewById(R.id.group_issue_express);
        mIvAddImg = (ImageView) findViewById(R.id.group_issue_add_img);

        mPreText = (TextView) findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mPreText.setText(getString(R.string.activity_issuing));

        mLinearType.setOnClickListener(this);
    }
    private void requestPublish(){


        mTitle = mTitleEdit.getText().toString();
        if(TextUtils.isEmpty(mTitle)){
            Utils.showToast(this,getString(R.string.please_input_title));
            return;
        }
        mContent = mContentEdit.getText().toString();
        if(TextUtils.isEmpty(mContent)){
            Utils.showToast(this,getString(R.string.please_input_content));
            return;
        }

        if(TextUtils.isEmpty(mTopicTypeTv.getText())){
            Utils.showToast(this,getString(R.string.please_select_topic_type));
            return;
        }

        if(mTopicTypeTv.getText().equals(getString(R.string.billiard_get_master))){
            mTopicType = GroupSelectTopicTypeActivity.GET_MASTER;
        }else if(mTopicTypeTv.getText().equals(getString(R.string.billiard_be_master))){
            mTopicType = GroupSelectTopicTypeActivity.BE_MASTER;
        }else if(mTopicTypeTv.getText().equals(getString(R.string.billiard_find_friend))){
            mTopicType = GroupSelectTopicTypeActivity.FIND_FRIEND;
        }else if(mTopicTypeTv.getText().equals(getString(R.string.billiard_equipment))){
            mTopicType = GroupSelectTopicTypeActivity.EQUIP;
        }else{
            mTopicType = GroupSelectTopicTypeActivity.OTHER;
        }

        mParamsMap.put(HttpConstants.GroupIssue.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        mParamsMap.put(HttpConstants.GroupIssue.TYPE,String.valueOf(mTopicType));
        mParamsMap.put(HttpConstants.GroupIssue.TITLE,mTitle);
        mParamsMap.put(HttpConstants.GroupIssue.CONTENT,mContent);

        mUrlAndMethodMap.put(PublicConstant.URL, HttpConstants.GroupIssue.URL);
        mUrlAndMethodMap.put(PublicConstant.METHOD, HttpConstants.RequestMethod.POST);

        new IssueTopicTask(mParamsMap).execute(mUrlAndMethodMap);
    }


    private class IssueTopicTask extends AsyncTaskUtil<String> {

        public IssueTopicTask(Map<String, String> map) {
            super(map);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreProgress.setVisibility(View.VISIBLE);
            mPreText.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mPreProgress.setVisibility(View.GONE);
            mPreText.setVisibility(View.GONE);
            try {
                if(!jsonObject.isNull("code")){
                    if(jsonObject.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                         mHandler.obtainMessage(PublicConstant.GET_SUCCESS).sendToTarget();
                    }else if(jsonObject.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                        mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                    }else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,jsonObject.getString("msg")).sendToTarget();
                    }
                }else{
                    mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    Utils.showToast(GroupIssueTopic.this,getString(R.string.activity_submit_success));
                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(GroupIssueTopic.this,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(GroupIssueTopic.this, (String) msg.obj);
                    }
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(GroupIssueTopic.this,getString(R.string.http_request_time_out));
                    break;
            }
        }
    };
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
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }else if(id == R.id.action_group_issue_topic){
            requestPublish();
        }

        return true;
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
            case R.id.group_issue_type_linear:
                Intent intent = new Intent();
                intent.setClass(this,GroupSelectTopicTypeActivity.class);
                if(mTopicTypeTv.getText().equals(getString(R.string.billiard_get_master))){
                    intent.putExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.GET_MASTER);
                }else if(mTopicTypeTv.getText().equals(getString(R.string.billiard_be_master))){
                    intent.putExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.BE_MASTER);
                }else if(mTopicTypeTv.getText().equals(getString(R.string.billiard_find_friend))){
                    intent.putExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.FIND_FRIEND);
                }else if(mTopicTypeTv.getText().equals(getString(R.string.billiard_equipment))){
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
            mTopicType = data.getIntExtra(GroupSelectTopicTypeActivity.TOPIC_TYPE_KEY,GroupSelectTopicTypeActivity.GET_MASTER);
            if(mTopicType == GroupSelectTopicTypeActivity.GET_MASTER){
                mTopicTypeTv.setText(getString(R.string.billiard_get_master));
            }else if(mTopicType == GroupSelectTopicTypeActivity.BE_MASTER){
                mTopicTypeTv.setText(getString(R.string.billiard_be_master));
            }else if(mTopicType == GroupSelectTopicTypeActivity.FIND_FRIEND){
                mTopicTypeTv.setText(getString(R.string.billiard_find_friend));
            }else if(mTopicType == GroupSelectTopicTypeActivity.EQUIP){
                mTopicTypeTv.setText(getString(R.string.billiard_equipment));
            }else{
                mTopicTypeTv.setText(getString(R.string.billiard_other));
            }
        }
    }
}
