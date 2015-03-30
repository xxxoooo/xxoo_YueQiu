package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.UserDao;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomNetWorkImageView;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by doushuqi on 15/1/8.
 */
public class UpgradeAssistantActivity extends Activity {

    private TextView mAccountTv,mSexTv,mNickNameTv,mDistrictTv,mLevelTv
            ,mBallType,mBallArm,mUsedTypeTv,mBallAge,mIdolTv,mSignTv,mCostTv
            ,mTypeTv,mExperienceTv;
    private CustomNetWorkImageView mPhotoView;//mNewerPhotoView;
    private ProgressBar mPreProgress;
    private TextView mPreText;
    private Drawable mProgressDrawable;
    private ImageLoader mImgLoader;
    private UserDao mUserDao;
    private UserInfo mUserInfo;

    private SharedPreferences mSharedPreference;
    private SharedPreferences.Editor mEditor;

    private String mTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_assistant);

        mImgLoader = VolleySingleton.getInstance().getImgLoader();

        mSharedPreference = getSharedPreferences(PublicConstant.USERBASEUSER, Context.MODE_PRIVATE);
        mEditor = mSharedPreference.edit();

        mTag = getIntent().getStringExtra(DatabaseConstant.UserTable.TITLE);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (mTag.equals(getString(R.string.nearby_billiard_mate_str))) {
            actionBar.setTitle(getString(R.string.back_to_mate));
        } else if(mTag.equals(getString(R.string.nearby_billiard_assist_coauch_str))){
            actionBar.setTitle(getString(R.string.update_to_assistant));
        }else{
            actionBar.setTitle(getString(R.string.update_to_coach));
        }

        initView();
    }

    private void initView(){

        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mPreText = (TextView) findViewById(R.id.pre_text);
        mPreText.setText(getString(R.string.feed_backing));

        mAccountTv = (TextView) findViewById(R.id.upgrade_account_name);
        mSexTv = (TextView) findViewById(R.id.upgrade_sex);
        mNickNameTv = (TextView) findViewById(R.id.upgrade_nickname);
        mDistrictTv = (TextView) findViewById(R.id.upgrade_district);
        mLevelTv = (TextView) findViewById(R.id.upgrade_level);
        mBallType = (TextView) findViewById(R.id.upgrade_ball_type);
        mBallArm = (TextView) findViewById(R.id.upgrade_ball_arm);
        mUsedTypeTv = (TextView) findViewById(R.id.upgrade_use_type);
        mBallAge = (TextView) findViewById(R.id.upgrade_ball_age);
        mIdolTv = (TextView) findViewById(R.id.upgrade_idol);
        mSignTv = (TextView) findViewById(R.id.upgrade_sign);
        mCostTv = (TextView) findViewById(R.id.upgrade_cost);
        mTypeTv = (TextView) findViewById(R.id.upgrade_type);
        mExperienceTv = (TextView) findViewById(R.id.upgrade_experience);

        mPhotoView = (CustomNetWorkImageView) findViewById(R.id.upgrade_photo_view);
//        mNewerPhotoView = (NetworkImageView) findViewById(R.id.upgrade_newer_photo);

        String unset = getString(R.string.unset);
        mPhotoView.setDefaultImageResId(R.drawable.default_head);
        mPhotoView.setImageUrl("http://" + YueQiuApp.sUserInfo.getImg_url(), mImgLoader);
        mAccountTv.setText(YueQiuApp.sUserInfo.getUsername());
        mSexTv.setText(YueQiuApp.sUserInfo.getSex() == 1
                ? getString(R.string.man) : getString(R.string.woman));
        mNickNameTv.setText("".equals(YueQiuApp.sUserInfo.getNick())
                ? unset : YueQiuApp.sUserInfo.getNick());
        mDistrictTv.setText("".equals(YueQiuApp.sUserInfo.getDistrict())
                ? unset : YueQiuApp.sUserInfo.getDistrict());
        mLevelTv.setText(1 == YueQiuApp.sUserInfo.getLevel()
                ? getString(R.string.level_base) : ((2 == YueQiuApp.sUserInfo.getLevel()) ?
                getString(R.string.level_middle) : getString(R.string.level_master)));
        mBallType.setText(1 == YueQiuApp.sUserInfo.getBall_type()
                ? getString(R.string.ball_type_1) : (2 == YueQiuApp.sUserInfo.getBall_type() ?
                getString(R.string.ball_type_2) : getString(R.string.ball_type_3)));
        mBallArm.setText(1 == YueQiuApp.sUserInfo.getBallArm()
                ? getString(R.string.cue_1) : getString(R.string.cue_2));
        mUsedTypeTv.setText(1 == YueQiuApp.sUserInfo.getUsedType()
                ? getString(R.string.habit_1) : (2 == YueQiuApp.sUserInfo.getUsedType() ?
                getString(R.string.habit_2) : getString(R.string.habit_3)));
        mBallAge.setText(YueQiuApp.sUserInfo.getBallAge());
        mIdolTv.setText("".equals(YueQiuApp.sUserInfo.getIdol())
                ? unset : YueQiuApp.sUserInfo.getIdol());
        //TODO:最新照片、费用、经历、类型没数据，类型是啥？定义不明确

        mImgLoader = VolleySingleton.getInstance().getImgLoader();
        mUserDao = DaoFactory.getUser(this);
        mUserInfo = mUserDao.getUserByUserId(String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.update_to_assistant));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.setActivityMenuColor(this);
        getMenuInflater().inflate(R.menu.commit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    return true;
                case R.id.commit:
                    //提交升级助教的资料
                    upgrade();
                    return true;
            }
        return true;
    }

    private void upgrade(){

        mPreProgress.setVisibility(View.VISIBLE);
        mPreText.setVisibility(View.VISIBLE);

        Map<String,String> params = new HashMap<String, String>();
        params.put(HttpConstants.SetUserUp.USER_ID,String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        if(mTag.equals(getString(R.string.nearby_billiard_assist_coauch_str))) {
            params.put(HttpConstants.SetUserUp.USER_TYPE, String.valueOf(PublicConstant.UPGRADE_ASSITANT));
        }
        else if(mTag.equals(getString(R.string.nearby_billiard_coauch_str))){
            params.put(HttpConstants.SetUserUp.USER_TYPE,String.valueOf(PublicConstant.UPGRADE_COACH));
        }
        else{
            params.put(HttpConstants.SetUserUp.USER_TYPE,String.valueOf(PublicConstant.UPGRADE_MATE));
        }


        HttpUtil.requestHttp(HttpConstants.SetUserUp.URL,params,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","upgrade response is ->" + response);
                try{
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            mHandler.sendEmptyMessage(PublicConstant.GET_SUCCESS);
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.d("wy","fail response String->"  + responseString);
                mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPreProgress.setVisibility(View.GONE);
            mPreText.setVisibility(View.GONE);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    if(mTag.equals(getString(R.string.nearby_billiard_assist_coauch_str))) {
                        mEditor.putString(DatabaseConstant.UserTable.TITLE,getString(R.string.nearby_billiard_assist_coauch_str));
                        YueQiuApp.sUserInfo.setTitle(getString(R.string.nearby_billiard_assist_coauch_str));
                    }
                    else if(mTag.equals(getString(R.string.nearby_billiard_coauch_str))){
                        mEditor.putString(DatabaseConstant.UserTable.TITLE, getString(R.string.nearby_billiard_coauch_str));
                        YueQiuApp.sUserInfo.setTitle(getString(R.string.nearby_billiard_coauch_str));
                    }else{
                        mEditor.putString(DatabaseConstant.UserTable.TITLE,getString(R.string.nearby_billiard_mate_str));
                        YueQiuApp.sUserInfo.setTitle(getString(R.string.nearby_billiard_mate_str));
                    }

                    mEditor.apply();

                    Intent intent = new Intent();
                    intent.setAction(PublicConstant.SLIDE_ACCOUNT_ACTION);
                    sendBroadcast(intent);

                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(msg.obj == null){
                        Utils.showToast(UpgradeAssistantActivity.this,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(UpgradeAssistantActivity.this, (String) msg.obj);
                    }
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
