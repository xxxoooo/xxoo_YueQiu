package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.PlayDao;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yinfeng on 15/1/6.
 */
public class PlayDetailActivity extends Activity {

    private ActionBar mActionBar;
    private TextView mUserNameTv,mSexTv,mBrowseCountTv,mCreateTimeTv;
    private TextView mTitleTv,mTypeTv,mAddressTv,mBeginTimeTv,mEndTimeTv,
            mModuleTv,mContactTv,mPhoneTv,mContentTv,mPreText,mNoResultTv;
    private ImageView mHeadImgIv;
    private PlayDao mPlayDao;
    private ProgressBar mPreProgressBar;
    private Drawable mProgressDrawable;
    private GridView mPartInGridView;
    private int mTableId,mInfoType;
    private String mCreateTime;
    private boolean mStroe = false;

    private Map<String,Integer> mParamMap = new HashMap<String, Integer>();
    private Map<String,String> mUrlAndMethodMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_detail);
        initActionBar();
        initView();
        Bundle args = getIntent().getExtras();
        mTableId = args.getInt(DatabaseConstant.PlayTable.TABLE_ID);
        mCreateTime = args.getString(DatabaseConstant.PlayTable.CREATE_TIME);
        mInfoType = Integer.parseInt(args.getString(DatabaseConstant.PlayTable.TYPE));
        mPlayDao = DaoFactory.getPlay(this);


        if(Utils.networkAvaiable(this)){
            requestDetail();
        }else{
            mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
        }

    }

    private void initActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(getString(R.string.activities_detail));
    }

    private void initView(){

        mHeadImgIv = Utils.$(this,R.id.play_detail_img_iv);
        mUserNameTv = Utils.$(this,R.id.play_detail_name_tv);
        mSexTv = Utils.$(this,R.id.play_detail_gender);
        mBrowseCountTv = Utils.$(this,R.id.play_look_num);
        mCreateTimeTv = Utils.$(this,R.id.play_create_time_tv);
        mTitleTv = Utils.$(this,R.id.play_detail_title_tv);
        mTypeTv = Utils.$(this,R.id.play_detail_type_info_tv);
        mAddressTv = Utils.$(this,R.id.play_detail_address_info_tv);
        mBeginTimeTv = Utils.$(this,R.id.play_detail_begin_info);
        mEndTimeTv = Utils.$(this,R.id.play_detail_end_info);
        mModuleTv = Utils.$(this,R.id.play_detail_model_info);
        mContactTv = Utils.$(this,R.id.play_detail_contact_info);
        mPhoneTv = Utils.$(this,R.id.play_detail_phone_info);
        mContentTv = Utils.$(this,R.id.play_detail_illustration_info);
        mPartInGridView = Utils.$(this,R.id.play_detail_gridview);
        mNoResultTv = Utils.$(this,R.id.play_detail_no_result_tv);

        mPreProgressBar = Utils.$(this,R.id.pre_progress);
        mPreText = Utils.$(this,R.id.pre_text);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(PlayDetailActivity.this).build();
        Rect bounds = mPreProgressBar.getIndeterminateDrawable().getBounds();
        mPreProgressBar.setIndeterminateDrawable(mProgressDrawable);
        mPreProgressBar.getIndeterminateDrawable().setBounds(bounds);
    }

    private void requestDetail(){
        mParamMap.put(HttpConstants.Play.ID,mTableId);
        mUrlAndMethodMap.put(PublicConstant.URL,HttpConstants.Play.GETDETAIL);
        mUrlAndMethodMap.put(PublicConstant.METHOD,HttpConstants.RequestMethod.GET);

        new RequestDetailTask(mParamMap).execute(mUrlAndMethodMap);
    }

    private PlayInfo setDetailInfoByJSON(JSONObject object){
        PlayInfo info = new PlayInfo();
        try{
            JSONObject result = object.getJSONObject("result");
            info.setTable_id(result.getString("id"));
            info.setImg_url(result.getString("img_url"));
            info.setTitle(result.getString("title"));
            info.setUsername(result.getString("username"));
            info.setCreate_time(mCreateTime);
            info.setType(result.getString("type"));
            info.setBegin_time(result.getString("begin_time"));
            info.setEnd_time(result.getString("end_time"));
            info.setModel(result.getString("model"));
            info.setContent(result.getString("content"));
            info.setLook_num(result.getInt("look_num"));
            info.setAddress(result.getString("address"));
            info.setSex(result.getString("sex"));

        }catch(JSONException e){
            e.printStackTrace();
        }
        return info;
    }

    private class RequestDetailTask extends AsyncTaskUtil<Integer>{

        public RequestDetailTask(Map<String, Integer> map) {
            super(map);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreProgressBar.setVisibility(View.VISIBLE);
            mPreText.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mPreProgressBar.setVisibility(View.GONE);
            mPreText.setVisibility(View.GONE);
            try{
                if(!jsonObject.isNull("code")){
                    if(jsonObject.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                        if(jsonObject.getString("result") != null){
                            PlayInfo info = setDetailInfoByJSON(jsonObject);
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS,info).sendToTarget();
                        }else{
                            mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        }
                    }else if(jsonObject.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                        mHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                    }else if(jsonObject.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                        mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                    }else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                }else{
                    mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
    private void updateUI(PlayInfo info){
        mUserNameTv.setText(info.getUsername());
        mSexTv.setText(info.getSex().equals("1") ? getString(R.string.man) : getString(R.string.woman));
        mBrowseCountTv.setText(info.getLook_num() + "");
        mCreateTimeTv.setText(mCreateTime);
        mTitleTv.setText(info.getTitle());
        mTypeTv.setText(getDetailTypeStr(info.getType()));
        mAddressTv.setText(info.getAddress());
        mBeginTimeTv.setText(info.getBegin_time());
        mEndTimeTv.setText(info.getEnd_time());
        mModuleTv.setText(getModeStr(Integer.parseInt(info.getModel())));
        mContentTv.setText(info.getContent());
    }
    private String getDetailTypeStr(String type){
        String typeStr;
        switch(Integer.parseInt(type)){
            case PublicConstant.PLAY_GROUP:
                typeStr = getString(R.string.group_activity);
                break;
            case PublicConstant.PLAY_MEET_STAR:
                typeStr = getString(R.string.star_meet);
                break;
            case PublicConstant.PLAY_BILLIARD_SHOW:
                typeStr = getString(R.string.billiard_show);
                break;
            case PublicConstant.PLAY_COMPETITION:
                typeStr = getString(R.string.complete);
                break;
            case PublicConstant.PLAY_OTHER_ACTIVITY:
                typeStr = getString(R.string.billiard_other);
                break;
            default:
                typeStr = getString(R.string.billiard_other);
                break;
        }
        return typeStr;
    }
    private int getModeStr(int mode){
        int resId = R.string.charge_module_free;
        switch (mode){
            case SelectChargeModuleActivity.MODULE_AA:
                resId = R.string.charge_module_aa;
                break;
            case SelectChargeModuleActivity.MODULE_FREE:
                resId = R.string.charge_module_free;
                break;
            case SelectChargeModuleActivity.MODULE_PAY:
                resId = R.string.charge_module_pay;
                break;
        }
        return resId;
    }
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.USE_CACHE:
                    break;
                case PublicConstant.GET_SUCCESS:
                    PlayInfo info = (PlayInfo) msg.obj;
                    updateUI(info);
                    break;
                case PublicConstant.TIME_OUT:
                    mNoResultTv.setVisibility(View.VISIBLE);
                    Utils.showToast(PlayDetailActivity.this,getString(R.string.http_request_time_out));
                    break;
                case PublicConstant.REQUEST_ERROR:
                    mNoResultTv.setVisibility(View.VISIBLE);
                    Utils.showToast(PlayDetailActivity.this,getString(R.string.http_request_error));
                    break;
                case PublicConstant.NO_RESULT:
                    mNoResultTv.setVisibility(View.VISIBLE);
                    Utils.showToast(PlayDetailActivity.this,getString(R.string.no_detail_info));
                    break;
                case PublicConstant.NO_NETWORK:
                    mNoResultTv.setVisibility(View.VISIBLE);
                    Utils.showToast(PlayDetailActivity.this,getString(R.string.network_not_available));
                    break;
                case PublicConstant.SHARE_SUCCESS:
                    Utils.showToast(PlayDetailActivity.this,getString(R.string.store_success));
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.setActivityMenuColor(PlayDetailActivity.this);
        getMenuInflater().inflate(R.menu.menu_activities_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
            case R.id.menu_activities_collect:
                int user_id = YueQiuApp.sUserInfo.getUser_id();
                if(user_id < 1) {
                    Utils.showToast(this,getString(R.string.please_login_first));
                }else{
                    store();
                }
                break;
            case R.id.menu_activities_share:
                Dialog dlg = Utils.showSheet(this);
                dlg.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
//
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
        mParamMap.put(HttpConstants.Play.TYPE,PublicConstant.PLAY);
        mParamMap.put(HttpConstants.Play.ID,mTableId);
        mParamMap.put(HttpConstants.Play.USER_ID,YueQiuApp.sUserInfo.getUser_id());

        mUrlAndMethodMap.put(PublicConstant.URL,HttpConstants.Play.GETDETAIL);
        mUrlAndMethodMap.put(PublicConstant.METHOD,HttpConstants.RequestMethod.GET);
        mStroe = true;

        new StoreTask(mParamMap).execute(mUrlAndMethodMap);
    }

    private class StoreTask extends AsyncTaskUtil<Integer>{

        public StoreTask(Map<String, Integer> map) {
            super(map);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreProgressBar.setVisibility(View.VISIBLE);
            mPreText.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mPreProgressBar.setVisibility(View.GONE);
            mPreText.setVisibility(View.GONE);

            try{
                if(!jsonObject.isNull("code")){
                    if(jsonObject.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                        mHandler.sendEmptyMessage(PublicConstant.SHARE_SUCCESS);
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