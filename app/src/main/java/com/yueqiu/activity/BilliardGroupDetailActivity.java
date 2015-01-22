package com.yueqiu.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    private String mImgUrl,mOwnerUserName,mOwnerSex,mCreateTime,mTitle,mContent;
    private int mLoveNums,mNoteId,mOwnerSexInt,mReadCount,mReplyCount;
    private ProgressBar mPreProgress;
    private Drawable mProgressDrawable;

    private Map<String,String> mParamsMap = new HashMap<String, String>();
    private Map<String,String>  mUrlAndMethodMap = new HashMap<String, String>();
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

        mOwnerImg = (ImageView) findViewById(R.id.billiard_group_detail_img);
        mOwnerTv = (TextView) findViewById(R.id.billiard_group_detail_owner);
        mOwnerSexTv = (TextView) findViewById(R.id.billiard_group_detail_ower_sex);
        mReadCountTv = (TextView) findViewById(R.id.billiard_group_detail_read_count_tv);
        mCreateTimeTv = (TextView) findViewById(R.id.billiard_group_detail_publish_time);
        mPaiseCountTv = (TextView) findViewById(R.id.billiard_group_detail_praise_count_tv);
        mReplyCountTv = (TextView) findViewById(R.id.billiard_group_detail_reply_count_tv);

        mListView = (ListView) findViewById(R.id.billiard_group_detail_listview);
        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);

        mPraiseView.setOnClickListener(this);
        mReplyView.setOnClickListener(this);
    }

    private void requestDetail(){
        //TODO:id值要根据intent传进来的id设置，这里先设为
        mParamsMap.put(HttpConstants.GroupDetail.ID,String.valueOf(1));

        mUrlAndMethodMap.put(PublicConstant.URL, HttpConstants.GroupDetail.URL);
        mUrlAndMethodMap.put(PublicConstant.METHOD, HttpConstants.RequestMethod.GET);
    }
    private void setGroupInfo(JSONObject object){

    }

    private class DetailRequestTask extends AsyncTaskUtil<String>{

        public DetailRequestTask(Map<String, String> map) {
            super(map);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mPreProgress.setVisibility(View.GONE);
            try{
                if(!jsonObject.isNull("code")){
                    if(jsonObject.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                        //TODO:对bean操作
                        mHandler.obtainMessage(PublicConstant.GET_SUCCESS).sendToTarget();
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

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    break;
                case PublicConstant.REQUEST_ERROR:
                    break;
                case PublicConstant.TIME_OUT:
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






}
