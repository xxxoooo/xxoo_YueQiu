package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.bean.Activities;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.ActivitiesDao;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yinfeng on 15/1/6.
 */
public class ActivitiesDetail extends Activity {
    private ActionBar mActionBar;
    private TextView mUserName, mSex, mLookNum, mDate, mTime, mTitle, mType,
            mSite, mBeginTime, mEndTime, mModel, mContact, mPhone, mInfo;
    private Intent mIntent;
    private Bundle bundle;
    private ImageView mIv;
    private int mId;
    private ActivitiesDao mDao;
    private LinearLayout mLinearLayout;
    private ProgressBar mPb;
    private Drawable mProgressDrawable;
    private String mCreateTime;
    private TextView mGetDataError;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PublicConstant.NO_RESULT:
                    mGetDataError.setVisibility(View.VISIBLE);
                    mPb.setVisibility(View.GONE);
                    break;
                case PublicConstant.GET_SUCCESS:
                    Activities activities = ((Activities) msg.obj);
                    if(activities.getType().equals("1"))
                    {
                        mType.setText(getString(R.string.group_activity));
                    }else if(activities.getType().equals("2"))
                    {
                        mType.setText(getString(R.string.star_meet));
                    }
                    else if(activities.getType().equals("3"))
                    {
                        mType.setText(getString(R.string.billiard_show));
                    }
                    else if(activities.getType().equals("4"))
                    {
                        mType.setText(getString(R.string.complete));
                    }else
                    {
                        mType.setText(getString(R.string.billiard_other));
                    }

                    mTitle.setText(activities.getTitle());
                    mSite.setText(activities.getAddress());
                    mSex.setText(Integer.valueOf(activities.getSex()) == 1 ? getString(R.string.man) : getString(R.string.woman));
                    mBeginTime.setText(activities.getBegin_time());
                    mEndTime.setText(activities.getEnd_time());
                    mInfo.setText(activities.getContent());
                    mLookNum.setText(String.valueOf(activities.getLook_num()));
                    mLinearLayout.setVisibility(View.VISIBLE);
                    mUserName.setText(activities.getUsername());
                    if(activities.getModel().equals("1"))
                    {
                        mModel.setText(getString(R.string.search_dating_detailed_model_1));
                    }else if(activities.getModel().equals("2"))
                    {
                        mModel.setText(getString(R.string.search_dating_detailed_model_2));
                    }else
                    {
                        mModel.setText(getString(R.string.search_dating_detailed_model_3));
                    }
                    String [] when = mCreateTime.split(" ");
                    String date = when[0].substring(5, when[0].length()).toString();
                    String time = when[1].substring(0, 5).toString();
                    mTime.setText(time);
                    mDate.setText(date);
//                    if (activities.getImg_url().equals("") || activities.getImg_url() == null) {
//                        mIv.setImageResource(R.drawable.default_head);
//                    } else {
//                        mIv.setImageBitmap(bitmapFromInternet(activities.getImg_url()));
//                    }
                    mPb.setVisibility(View.GONE);
                    mGetDataError.setVisibility(View.GONE);
                    break;
                case PublicConstant.REQUEST_ERROR:
                    mGetDataError.setText(getString(R.string.no_data));
                    mGetDataError.setVisibility(View.VISIBLE);
                    mPb.setVisibility(View.GONE);
                    break;
                case PublicConstant.TIME_OUT:
                    mGetDataError.setText(getString(R.string.http_request_time_out));
                    mGetDataError.setVisibility(View.VISIBLE);
                    mPb.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private Bitmap bitmapFromInternet(String filepath) {
        InputStream in = HttpUtil.getInputStream(filepath);
        Bitmap bp = BitmapFactory.decodeStream(in);
        return bp;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_detail);
        mIntent = getIntent();
        bundle = mIntent.getExtras();
        mId = bundle.getInt("id");
        mCreateTime = bundle.getString("create_time");
        mDao = DaoFactory.getActivities(ActivitiesDetail.this);
        initActionBar();
        initView();
        if(Utils.networkAvaiable(ActivitiesDetail.this))
        {
            new Thread(getDataFromInternet).start();
        }
        else
        {
            new Thread(getDataFromLocal).start();
        }
    }

    private void initActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(getString(R.string.activities_detail));
    }

    private Runnable getDataFromInternet = new Runnable() {
        @Override
        public void run() {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", mId);
            String retStr = HttpUtil.urlClient(HttpConstants.Play.GETDETAIL,
                    map, HttpConstants.RequestMethod.GET);

            JSONObject object = Utils.parseJson(retStr);
            Message msg = new Message();
            try {
                if(!object.isNull("code")) {
                    if (object.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                        JSONObject activitiesObject = object.getJSONObject("result");
                        if(activitiesObject != null) {
                            Activities activities = Utils.mapingObject(Activities.class, activitiesObject);
                            msg.obj = activities;
                            activities.setCreate_time(mCreateTime);
                            msg.what = PublicConstant.GET_SUCCESS;
                            mDao.updateActivities(activities);
                        }else{
                            msg.what = PublicConstant.NO_RESULT;
                        }
                    }else if (object.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                        msg.what = PublicConstant.NO_RESULT;
                    }else if (object.getInt("code") == HttpConstants.ResponseCode.REQUEST_ERROR){
                        msg.what = PublicConstant.REQUEST_ERROR;
                    }else if (object.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                        msg.what = PublicConstant.TIME_OUT;
                    }else{
                        msg.what = PublicConstant.REQUEST_ERROR;
                    }
                }else{
                    msg.what = PublicConstant.REQUEST_ERROR;
                }
                handler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    private Runnable getDataFromLocal = new Runnable() {
        @Override
        public void run() {
            Activities activities = mDao.getActivities(mId);
            Message msg = new Message();
            if (null == activities || activities.getModel() == null) {
                msg.what = PublicConstant.NO_RESULT;
            } else {
                msg.what = PublicConstant.GET_SUCCESS;
                msg.obj = activities;
            }
            handler.sendMessage(msg);
        }
    };


    private void initView() {
        mUserName = (TextView) findViewById(R.id.tv_detail_username);
        mBeginTime = (TextView) findViewById(R.id.tv_detail_begin_time);
        mContact = (TextView) findViewById(R.id.tv_detail_contact);
        mDate = (TextView) findViewById(R.id.tv_detail_date);
        mEndTime = (TextView) findViewById(R.id.tv_detail_end_time);
        mInfo = (TextView) findViewById(R.id.tv_detail_info);
        mLookNum = (TextView) findViewById(R.id.tv_detail_looknum);
        mModel = (TextView) findViewById(R.id.tv_detail_model);
        mPhone = (TextView) findViewById(R.id.tv_detail_phone);
        mSex = (TextView) findViewById(R.id.tv_detail_sex);
        mSite = (TextView) findViewById(R.id.tv_detail_site);
        mTime = (TextView) findViewById(R.id.tv_detail_time);
        mTitle = (TextView) findViewById(R.id.tv_detail_title);
        mType = (TextView) findViewById(R.id.tv_detail_type);
        mLinearLayout = (LinearLayout) findViewById(R.id.detail_ll);
        mLinearLayout.setVisibility(View.GONE);
        mPb = (ProgressBar) findViewById(R.id.pb_detail);
        mGetDataError = (TextView) findViewById(R.id.detail_error);
        mGetDataError.setVisibility(View.INVISIBLE);
        mPb.setVisibility(View.VISIBLE);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(ActivitiesDetail.this).build();
        Rect bounds = mPb.getIndeterminateDrawable().getBounds();
        mPb.setIndeterminateDrawable(mProgressDrawable);
        mPb.getIndeterminateDrawable().setBounds(bounds);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.setActivityMenuColor(ActivitiesDetail.this);
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
                break;
            case R.id.menu_activities_share:
                Dialog dlg = Utils.showSheet(this);
                dlg.show();
                break;
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
}
