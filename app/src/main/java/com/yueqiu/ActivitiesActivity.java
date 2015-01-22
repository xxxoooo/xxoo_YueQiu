package com.yueqiu;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.yueqiu.activity.ActivitiesDetail;
import com.yueqiu.activity.SearchResultActivity;
import com.yueqiu.adapter.ActivitiesListViewAdapter;
import com.yueqiu.bean.Activities;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.ActivitiesDao;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by yinfeng on 14/12/18.
 */
public class ActivitiesActivity extends Activity{
    private static final String TAG = "ActivitiesActivity";
    private ActionBar mActionBar;
    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;
    private TextView mEmptyView;
    private ActivitiesListViewAdapter mAdapter;
    private ArrayList<Activities> mListData;

    private static final int LENGTH = 10;
    private ActivitiesDao mDao;
    private ProgressBar mPb;
    private int mNetStart = 0,mNetEnd = 9;
    private int mLocalStart,mLocalEnd = 10;
    private boolean mIsHead,mLoadMore;
    private Drawable mProgressDrawable;
    private boolean mNetworkAvailable;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPb.setVisibility(View.GONE);
            switch (msg.what)
            {
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(ActivitiesActivity.this,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(ActivitiesActivity.this, (String) msg.obj);
                    }
                    onLoad();
                    break;
                case PublicConstant.GET_SUCCESS:

                    mListData = (ArrayList<Activities>)msg.obj;
                    if(mAdapter == null)
                    {
                        mAdapter = new ActivitiesListViewAdapter(mListData ,ActivitiesActivity.this);
                        mListView.setAdapter(mAdapter);

                    }
                    else
                    {
                        mAdapter.notifyDataSetChanged();
                    }
                    onLoad();
                    break;
                case PublicConstant.NO_RESULT:
                    if(mListData.isEmpty()){
                        setEmptyViewVisible();
                    }
                    if(!mIsHead && mLoadMore){
                        Utils.showToast(ActivitiesActivity.this,getString(R.string.no_more_business_activity));
                    }
                    onLoad();
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(ActivitiesActivity.this, getString(R.string.http_request_time_out));
                    onLoad();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activites);
        initActionBar();
        mListData = new ArrayList<Activities>();
        mDao = DaoFactory.getActivities(ActivitiesActivity.this);
        initView();
        mNetworkAvailable = Utils.networkAvaiable(ActivitiesActivity.this);
        new Thread(mNetworkAvailable ? getNetworkData : getLocalData).start();
    }





    private void initView()
    {
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.activity_activities_lv);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshListView.setOnRefreshListener(mRefreshListener);
        mListView = mPullToRefreshListView.getRefreshableView();
        mPb = (ProgressBar) findViewById(R.id.pb_loading);
        mPb.setVisibility(View.VISIBLE);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPb.getIndeterminateDrawable().getBounds();
        mPb.setIndeterminateDrawable(mProgressDrawable);
        mPb.getIndeterminateDrawable().setBounds(bounds);
        mListView.setOnItemClickListener(itemClickListener);

    }


    protected void setEmptyViewVisible(){
        mEmptyView = new TextView(this);
        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        mEmptyView.setTextColor(getResources().getColor(R.color.md__defaultBackground));
        mEmptyView.setText(getString(R.string.your_published_info_is_empty,getString(R.string.no_business_activity)));
        mPullToRefreshListView.setEmptyView(mEmptyView);
    }

    private Runnable getNetworkData = new Runnable() {
        @Override
        public void run() {
            getDatafromInternet();
        }
    };

    private Runnable getLocalData = new Runnable() {
        @Override
        public void run() {
            ArrayList<Activities> list = mDao.getActivities(mLocalStart, mLocalEnd);
            mLocalStart += LENGTH;
            mLocalEnd += LENGTH;
            Message msg = new Message();
            if(list == null)
            {
                msg.what = PublicConstant.NO_RESULT;
            }
            else
            {
                msg.what = PublicConstant.GET_SUCCESS;
                mListData.addAll(list);
                msg.obj = mListData;

            }
            mHandler.sendMessage(msg);
        }
    };

    private void getDatafromInternet()
    {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("start_no",mNetStart);
        map.put("end_no",mNetEnd);
        String retStr = HttpUtil.urlClient(
                HttpConstants.Play.GETLISTEE, map, HttpConstants.RequestMethod.GET);
        JSONObject object = Utils.parseJson(retStr);
        try {
            Message msg = new Message();
            if(!object.isNull("code")) {
                if (object.getInt("code") == HttpConstants.ResponseCode.NORMAL) {

                    JSONObject result = object.getJSONObject("result");
                    JSONArray array = result.getJSONArray("list_data");
                    int length = array.length();
                    if (length == 0) {
                        msg.what = PublicConstant.NO_RESULT;
                    } else {
                        ArrayList<Activities> list = new ArrayList<Activities>();
                        for (int i = 0; i < length; i++) {

                            JSONObject item = array.getJSONObject(i);
                            Activities activityItem = Utils.mapingObject(Activities.class, item);
                            boolean flag = false;
                            for (int j = 0; j < mListData.size(); j++) {
                                if (mListData.get(j).getId().equals(activityItem.getId())) {
                                    flag = true;
                                }
                            }
                            if (!flag) {
                                list.add(activityItem);
                            }
                        }
                        mDao.insertActiviesList(mListData);
                        msg.what = PublicConstant.GET_SUCCESS;
                        if (!mIsHead) {
                            mListData.addAll(list);
                        } else {
                            mListData.addAll(0, list);
                        }
                        msg.obj = mListData;
                        mNetStart += LENGTH;
                        mNetEnd += LENGTH;
                    }
                } else if (object.getInt("code") != HttpConstants.ResponseCode.TIME_OUT) {
                    msg.what = PublicConstant.TIME_OUT;
                }else if(object.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                    msg.what = PublicConstant.NO_RESULT;
                }
                else {
                    msg.what = PublicConstant.REQUEST_ERROR;
                    msg.obj = object.getString("msg");
                }
            }else{
                msg.what = PublicConstant.REQUEST_ERROR;
            }
            mHandler.sendMessage(msg);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            startActivity(new Intent(ActivitiesActivity.this, ActivitiesDetail.class));
            overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
        }
    };


    private void initActionBar(){
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(getString(R.string.tab_title_activity));
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.billiard_search, menu);

        SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =(SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
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


    private PullToRefreshBase.OnRefreshListener2<ListView> mRefreshListener = new PullToRefreshBase.OnRefreshListener2<ListView>() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
            String label = DateUtils.formatDateTime(ActivitiesActivity.this, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            // Update the LastUpdatedLabel
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            new Thread(Utils.networkAvaiable(ActivitiesActivity.this)
                    ? getNetworkData : getLocalData).start();
            mNetStart = 0;
            mNetEnd = 9;
            mIsHead = true;
            mLoadMore = false;
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            String label = DateUtils.formatDateTime(ActivitiesActivity.this, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            // Update the LastUpdatedLabel
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            new Thread(Utils.networkAvaiable(ActivitiesActivity.this)
                    ? getNetworkData : getLocalData).start();

            mIsHead = false;
            mLoadMore = true;
        }
    };

    private void onLoad() {
        mPullToRefreshListView.onRefreshComplete();
    }
}
