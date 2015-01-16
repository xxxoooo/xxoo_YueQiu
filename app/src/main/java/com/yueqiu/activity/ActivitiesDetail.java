package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
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
    private static final int GET_DATA_ERROR = 0x00;
    private static final int GET_DATA_SUCCESS = 0x01;
    private LinearLayout mLinearLayout;
    private ProgressBar mPb;
    private Drawable mProgressDrawable;
    private String mCreateTime;
    private TextView mgetDataError;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_DATA_ERROR:
                    mgetDataError.setVisibility(View.VISIBLE);
                    mPb.setVisibility(View.GONE);
                    break;
                case GET_DATA_SUCCESS:
                    Activities activities = ((Activities) msg.obj);
                    if(activities.getType().equals("1"))
                    {
                        mType.setText("群活动");
                    }else if(activities.getType().equals("2"))
                    {
                        mType.setText("球星汇");
                    }
                    else if(activities.getType().equals("3"))
                    {
                        mType.setText("台球展");
                    }
                    else if(activities.getType().equals("4"))
                    {
                        mType.setText("赛事");
                    }else
                    {
                        mType.setText("其它");
                    }

                    mTitle.setText(activities.getTitle());
                    mSite.setText(activities.getAddress());
                    mSex.setText(Integer.valueOf(activities.getSex()) == 1 ? "男" : "女");
                    mBeginTime.setText(activities.getBegin_time());
                    mEndTime.setText(activities.getEnd_time());
                    mInfo.setText(activities.getContent());
                    mLookNum.setText(String.valueOf(activities.getLook_num()));
                    mLinearLayout.setVisibility(View.VISIBLE);
                    mUserName.setText(activities.getUsername());
                    if(activities.getModel().equals("1"))
                    {
                        mModel.setText("免费");
                    }else if(activities.getModel().equals("2"))
                    {
                        mModel.setText("收费");
                    }else
                    {
                        mModel.setText("AA");
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
                    mgetDataError.setVisibility(View.GONE);
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
            try {
                Message msg = new Message();
                if (object.getInt("code") != 1001) {
                    msg.what = GET_DATA_ERROR;
                } else {
                    JSONObject activitiesObject = object.getJSONObject("result");
                    Activities activities = Utils.mapingObject(Activities.class, activitiesObject);
                    msg.obj = activities;
                    activities.setCreate_time(mCreateTime);
                    msg.what = GET_DATA_SUCCESS;
                    mDao.updateActivities(activities);
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
                msg.what = GET_DATA_ERROR;
            } else {
                msg.what = GET_DATA_SUCCESS;
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
        mgetDataError = (TextView) findViewById(R.id.detail_error);
        mgetDataError.setVisibility(View.INVISIBLE);
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
