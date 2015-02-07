package com.yueqiu.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ActionBar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.GroupNoteInfo;
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

public class BilliardGroupDetailActivity extends Activity implements View.OnClickListener{
    private View mPraiseView,mReplyView;
    private TextView mTvYueqiu, mTvYueqiuCircle, mTvFriendCircle,
            mTvWeichat, mTvQQZone, mTvTencentWeibo, mTvSinaWeibo, mTvRenren;
    private ImageView mOwnerImg;
    private TextView mOwnerTv,mOwnerSexTv,mReadCountTv,mCreateTimeTv,mPaiseCountTv,mReplyCountTv;
    private ListView mListView;
    private Button mBtnCancel;
    private Dialog mShareDlg;
    private int mNoteId,mReplyCount;
    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;
    private GroupNoteInfo mGroupInfo;

    private Map<String,Integer> mParamsMap = new HashMap<String,Integer>();
    private Map<String,String>  mUrlAndMethodMap = new HashMap<String, String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiard_group_detail);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.billiard_group_detail));

        initView();

        Bundle args = getIntent().getExtras();
        mNoteId = args.getInt(DatabaseConstant.GroupInfo.NOTE_ID);
        mReplyCount = args.getInt(DatabaseConstant.GroupInfo.COMMENT_COUNT);

        if(Utils.networkAvaiable(this)){
            requestDetail();
        }else{
            mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
        }
    }

    private void initView(){
        mPraiseView = findViewById(R.id.billiard_group_praise_view);
        mReplyView = findViewById(R.id.billiard_group_reply_view);

        mOwnerImg = (ImageView) findViewById(R.id.billiard_group_detail_img);
        mOwnerTv = (TextView) findViewById(R.id.billiard_group_detail_owner);
        mOwnerSexTv = (TextView) findViewById(R.id.billiard_group_detail_ower_sex);
        mReadCountTv = (TextView) findViewById(R.id.billiard_group_detail_read_count_tv);
        mCreateTimeTv = (TextView) findViewById(R.id.billiard_group_detail_publish_time);
        mPaiseCountTv = (TextView) findViewById(R.id.billiard_group_detail_praise_count_tv);
        mReplyCountTv = (TextView) findViewById(R.id.billiard_group_detail_reply_count_tv);

        mListView = (ListView) findViewById(R.id.billiard_group_detail_listview);
        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);
        mPreTextView = (TextView) findViewById(R.id.pre_text);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);

        mPraiseView.setOnClickListener(this);
        mReplyView.setOnClickListener(this);
    }

    private void requestDetail(){
        //TODO:id值要根据intent传进来的id设置，这里先设为
        mParamsMap.put(HttpConstants.GroupDetail.ID,mNoteId);

        mUrlAndMethodMap.put(PublicConstant.URL, HttpConstants.GroupDetail.URL);
        mUrlAndMethodMap.put(PublicConstant.METHOD, HttpConstants.RequestMethod.GET);

        new DetailRequestTask(mParamsMap).execute(mUrlAndMethodMap);
    }
    private GroupNoteInfo setGroupInfo(JSONObject object){
        GroupNoteInfo info = new GroupNoteInfo();
        try{
            JSONObject result = object.getJSONObject("result");
            info.setNoteId(Integer.parseInt(result.getString("id")));
            info.setUserName(result.getString("username"));
            info.setSex(Integer.valueOf(result.getString("sex")));
            info.setBrowseCount(result.getInt("look_number"));
            info.setIssueTime(result.getString("create_time"));
            info.setTitle(result.getString("title"));
            info.setContent(result.getString("content"));
            info.setLoveNums(result.getInt("number"));
            //TODO:服务器那边傻×，commentCount没有，img_url也没有
//            info.setCommentCount(result.getInt("comment_count"));
//            info.setImg_url(result.getString("img_url"));
        }catch(JSONException e){
            e.printStackTrace();
        }
        return info;
    }

    private class DetailRequestTask extends AsyncTaskUtil<Integer>{

        public DetailRequestTask(Map<String, Integer> map) {
            super(map);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreProgress.setVisibility(View.VISIBLE);
            mPreTextView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mPreProgress.setVisibility(View.GONE);
            mPreTextView.setVisibility(View.GONE);
            try{
                if(!jsonObject.isNull("code")){
                    if(jsonObject.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                        if(jsonObject.getString("result") != null) {
                            GroupNoteInfo info = setGroupInfo(jsonObject);
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS, info).sendToTarget();
                        }else{
                            mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        }
                    }else if(jsonObject.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                        mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                    }else if(jsonObject.getInt("code") == HttpConstants.ResponseCode.REQUEST_ERROR){
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,jsonObject.getString("msg")).sendToTarget();
                    }
                }else{
                    mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void updateUI(GroupNoteInfo info){
        mOwnerTv.setText(info.getUserName());
        mOwnerSexTv.setText(info.getSex() == 1 ? getString(R.string.man) : getString(R.string.woman));
        if(info.getSex() == 1){
            mOwnerSexTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.male, 0);
        }else{
            mOwnerSexTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.female, 0);
        }
        mReadCountTv.setText(String.valueOf(info.getBrowseCount()));
        mCreateTimeTv.setText(info.getIssueTime());
        mPaiseCountTv.setText(getString(R.string.praise,info.getLoveNums()));
        mReplyCountTv.setText(getString(R.string.reply,mReplyCount));
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    mGroupInfo = (GroupNoteInfo) msg.obj;
                    updateUI(mGroupInfo);
                    break;
                case PublicConstant.REQUEST_ERROR:
                    Utils.showToast(BilliardGroupDetailActivity.this, getString(R.string.http_request_error));
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(BilliardGroupDetailActivity.this, getString(R.string.http_request_time_out));
                    break;
                case PublicConstant.NO_NETWORK:
                    //TODO:如果用缓存的话，这里是有逻辑上的问题，但是目前不需要缓存，所以暂时没问题
                    Utils.showToast(BilliardGroupDetailActivity.this, getString(R.string.network_not_available));
                    break;
                case PublicConstant.FAVOR_SUCCESS:
                    //TODO:如果有缓存功能的话，这里还得插入收藏的数据库
                    Intent shareIntent = new Intent(PublicConstant.SLIDE_FAVOR_ACTION);
                    sendBroadcast(shareIntent);
                    Utils.showToast(BilliardGroupDetailActivity.this, getString(R.string.store_success));
                    break;
                case PublicConstant.NO_RESULT:
                    //TODO:还有listview的emptyview
                    Utils.showToast(BilliardGroupDetailActivity.this,getString(R.string.no_group_detail_info));
                    break;
            }
        }
    };
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
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }else if(id == R.id.billiard_detail_action_share){
            mShareDlg = Utils.showSheet(this);
            mShareDlg.show();

        }else if(id == R.id.billiard_detail_action_collect){
            int user_id = YueQiuApp.sUserInfo.getUser_id();
            if(user_id < 1 ){
                Utils.showToast(this,getString(R.string.please_login_first));
            }else{
                if(Utils.networkAvaiable(this)){
                    store();
                }else{
                    mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
                }
            }
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

    private void store(){
        mParamsMap.put(HttpConstants.Play.TYPE,PublicConstant.GROUP);
        mParamsMap.put(HttpConstants.Play.ID,mNoteId);
        mParamsMap.put(HttpConstants.Play.USER_ID,YueQiuApp.sUserInfo.getUser_id());

        mUrlAndMethodMap.put(PublicConstant.URL,HttpConstants.Favor.STORE_URL);
        mUrlAndMethodMap.put(PublicConstant.METHOD,HttpConstants.RequestMethod.POST);
        //mStroe = true;

        new StoreTask(mParamsMap).execute(mUrlAndMethodMap);
    }

    private class StoreTask extends AsyncTaskUtil<Integer>{

        public StoreTask(Map<String, Integer> map) {
            super(map);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreProgress.setVisibility(View.VISIBLE);
            mPreTextView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mPreProgress.setVisibility(View.GONE);
            mPreTextView.setVisibility(View.GONE);

            try{
                if(!jsonObject.isNull("code")){
                    if(jsonObject.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                        mHandler.sendEmptyMessage(PublicConstant.FAVOR_SUCCESS);
                    }else if(jsonObject.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                        mHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                    }else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,jsonObject.getString("msg")).sendToTarget();
                    }
                }else{
                    mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }


}
