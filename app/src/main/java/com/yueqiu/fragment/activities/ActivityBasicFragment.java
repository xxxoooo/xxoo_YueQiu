package com.yueqiu.fragment.activities;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.activity.ActivitiesDetail;
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

public class ActivityBasicFragment extends Fragment {

    private static final int LENGTH = 10;
    private Activity mActivity;
    private View mView;
    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;
    private ActivitiesListViewAdapter mAdapter;
    private ArrayList<Activities> mListData;
    private ActivitiesDao mDao;
    private ProgressBar mPb;
    private TextView mEmptyView;
    private int mNetStart = 0,mNetEnd = 9;
//    private int mLocalStart = 0,mLocalEnd = 10;
    private boolean mIsHead,mLoadMore;
    private boolean mNetworkAvailable;
    private int mType;
    private Drawable mProgressDrawable;
    private String mEmptyTypeStr;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_activites,null);
        mListData = new ArrayList<Activities>();
        mDao = DaoFactory.getActivities(mActivity);
        initView();
        mNetworkAvailable = Utils.networkAvaiable(mActivity);
        mType = getArguments().getInt("type",1);
        setmEmptyStr();
        new Thread(mNetworkAvailable ? getNetworkData : getLocalData).start();
        return mView;
    }

    private void initView()
    {
        mPullToRefreshListView = (PullToRefreshListView) mView.findViewById(R.id.activity_activities_lv);
        mListView = mPullToRefreshListView.getRefreshableView();
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshListView.setOnRefreshListener(mRefreshListener);
        mPb = (ProgressBar) mView.findViewById(R.id.pb_loading);
        mPb.setVisibility(View.VISIBLE);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(mActivity).build();
        Rect bounds = mPb.getIndeterminateDrawable().getBounds();
        mPb.setIndeterminateDrawable(mProgressDrawable);
        mPb.getIndeterminateDrawable().setBounds(bounds);
        mListView.setOnItemClickListener(itemClickListener);

    }
    private void setmEmptyStr(){
        switch(mType){
            case PublicConstant.GROUP_ACTIVITY:
                mEmptyTypeStr = getString(R.string.group_activity);
                break;
            case PublicConstant.MEET_STAR:
                mEmptyTypeStr = getString(R.string.star_meet);
                break;
            case PublicConstant.BILLIARD_SHOW:
                mEmptyTypeStr = getString(R.string.billiard_show);
                break;
            case PublicConstant.COMPETITION:
                mEmptyTypeStr = getString(R.string.complete);
                break;
            case PublicConstant.OTHER_ACTIVITY:
                mEmptyTypeStr = getString(R.string.billiard_other);
                break;
        }
    }

    protected void setEmptyViewVisible(){
        mEmptyView = new TextView(mActivity);
        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        mEmptyView.setTextColor(getResources().getColor(R.color.md__defaultBackground));
        mEmptyView.setText(getString(R.string.your_published_info_is_empty,mEmptyTypeStr));
        mPullToRefreshListView.setEmptyView(mEmptyView);
    }
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPb.setVisibility(View.GONE);
            switch (msg.what)
            {
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(mActivity,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(mActivity, (String) msg.obj);
                    }
                    onLoad();
                    break;
                case PublicConstant.GET_SUCCESS:

                    mListData = (ArrayList<Activities>)msg.obj;
                    if(mAdapter == null)
                    {
                        mAdapter = new ActivitiesListViewAdapter(mListData ,mActivity);
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
                        Utils.showToast(mActivity,getString(R.string.no_more_type_activity,mEmptyTypeStr));
                    }
                    onLoad();
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(mActivity, getString(R.string.http_request_time_out));
                    onLoad();
                    break;
            }
        }
    };
    private Runnable getNetworkData = new Runnable() {
        @Override
        public void run() {
            getDatafromInternet();
        }
    };

    private Runnable getLocalData = new Runnable() {
        ArrayList<Activities> mList;
        @Override
        public void run() {
            Log.d("wy","start->" + mNetStart);
            Log.d("wy","end->" + mNetEnd);
            mList = mDao.getActivities(mNetStart, mNetEnd + 1);
            mNetStart += LENGTH;
            mNetEnd += LENGTH;
            Message msg = new Message();
            if(mList == null)
            {
                msg.what = PublicConstant.NO_RESULT;
            }
            else
            {
                msg.what = PublicConstant.GET_SUCCESS;
                mListData.addAll(mList);
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
        map.put("type",mType);
        String retStr = HttpUtil.urlClient(
                HttpConstants.Play.GETLISTEE, map, HttpConstants.RequestMethod.GET);
        JSONObject object = Utils.parseJson(retStr);
        try {
            Message msg = new Message();
            //object不包含code
            if(!object.isNull("code")) {
                //获取数据正常
                if (object.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                    JSONObject result = object.getJSONObject("result");
                    JSONArray array = result.getJSONArray("list_data");
                    int length = array.length();
                    if (length == 0) {
                        msg.what = PublicConstant.NO_RESULT;
                    } else {
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
                                if(!mIsHead){
                                    mListData.add(activityItem);
                                }else{
                                    mListData.add(0,activityItem);
                                }
                            }
                        }
                        msg.what = PublicConstant.GET_SUCCESS;
                        mDao.insertActiviesList(mListData);
                        msg.obj = mListData;

                        mNetStart += LENGTH;
                        mNetEnd += LENGTH;
                    }
                }else if(object.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
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
            Intent intent = new Intent();
            intent.setClass(mActivity, ActivitiesDetail.class);
            Bundle bundle = new Bundle();
            bundle.putInt("id", Integer.valueOf(mListData.get(position - 1).getId()));
            bundle.putString("create_time", mListData.get(position - 1).getCreate_time());
            intent.putExtras(bundle);
            startActivity(intent);
            mActivity.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    };

    private PullToRefreshBase.OnRefreshListener2<ListView> mRefreshListener = new PullToRefreshBase.OnRefreshListener2<ListView>() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
            String label = DateUtils.formatDateTime(mActivity, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            mNetStart = 0;
            mNetEnd = 9;
            mIsHead = true;
            mLoadMore = false;
            new Thread(Utils.networkAvaiable(mActivity)
                    ? getNetworkData : getLocalData).start();


        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            String label = DateUtils.formatDateTime(mActivity, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            mIsHead = false;
            mLoadMore = true;
            new Thread(Utils.networkAvaiable(mActivity)
                    ? getNetworkData : getLocalData).start();


        }
    };

    private void onLoad() {
        if(mPullToRefreshListView.isRefreshing())
            mPullToRefreshListView.onRefreshComplete();
    }
}
