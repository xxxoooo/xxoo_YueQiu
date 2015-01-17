package com.yueqiu.fragment.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.yueqiu.R;
import com.yueqiu.activity.ActivitiesDetail;
import com.yueqiu.adapter.ActivitiesListViewAdapter;
import com.yueqiu.bean.Activities;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.dao.ActivitiesDao;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.XListView;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yinfeng on 15/1/12.
 */
public class CompleteFragment extends Fragment implements XListView.IXListViewListener{

    private static final String TAG = "ActivitiesActivity";
    private ActionBar mActionBar;
    private XListView mListView;
    private ActivitiesListViewAdapter mAdapter;
    private ArrayList<Activities> mListData;

    private static final int REQUEST_CODE_SUCCESS = 1001;
    private static final int GET_DATA_ERROR = 0x00;
    private static final int GET_DATA_SUCCESS = 0x01;
    private static final int GET_DATE_EMPTY = 0x02;
    private static final int LENGTH = 10;
    private ActivitiesDao mDao;
    private ProgressBar mPb;
    private int mNetstart;
    private int mNetend;
    private int mLocalStart;
    private int mLocalEnd;
    private boolean isHead;
    private boolean mNetworkAvailable;
    private Activity mActivity;
    private View mView;
    private int mType;
    private Drawable mProgressDrawable;
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPb.setVisibility(View.INVISIBLE);
            switch (msg.what)
            {
                case GET_DATA_ERROR:
                    onLoad();
                    break;
                case GET_DATA_SUCCESS:

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

                    mListView.setVisibility(View.VISIBLE);
                    mListView.setRefreshTime(Utils.getNowTime());
                    onLoad();
                    break;
                case GET_DATE_EMPTY:
                    onLoad();
                    break;
            }
        }
    };

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_activites,null);
        mNetstart = 0;
        mNetend = 9;
        mLocalStart = 0;
        mLocalEnd = 10;
        mListData = new ArrayList<Activities>();
        mDao = DaoFactory.getActivities(mActivity);
        initView();
        mNetworkAvailable = Utils.networkAvaiable(mActivity);
//        handler.post(getInternetDataThread);
        isHead = false;
        mType = getArguments().getInt("type",4);
        new Thread(mNetworkAvailable ? getNetworkData : getLocalData).start();
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initView()
    {
        mListView = (XListView)mView.findViewById(R.id.activity_activities_lv);
        mPb = (ProgressBar) mView.findViewById(R.id.pb_loading);
        mListView.setVisibility(View.GONE);
        mPb.setVisibility(View.VISIBLE);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(mActivity).build();
        Rect bounds = mPb.getIndeterminateDrawable().getBounds();
        mPb.setIndeterminateDrawable(mProgressDrawable);
        mPb.getIndeterminateDrawable().setBounds(bounds);
        mListView.setOnItemClickListener(itemClickListener);
        mListView.setPullLoadEnable(true);
        mListView.setXListViewListener(this);
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
//            Log.i("Demo", String.valueOf(mLocalStart) + "-----" + String.valueOf(mLocalEnd));
            ArrayList<Activities> list = mDao.getActivities(mLocalStart, mLocalEnd);
            mLocalStart += LENGTH;
            mLocalEnd += LENGTH;
            Message msg = new Message();
            if(list == null)
            {
                msg.what = GET_DATE_EMPTY;
            }
            else
            {
                msg.what = GET_DATA_SUCCESS;
                mListData.addAll(list);
                msg.obj = mListData;

            }
            handler.sendMessage(msg);
        }
    };

    private void getDatafromInternet()
    {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("start_no",mNetstart);
        map.put("end_no",mNetend);
        map.put("type",mType);
        String retStr = HttpUtil.urlClient(
                HttpConstants.Play.GETLISTEE, map, HttpConstants.RequestMethod.GET);
        JSONObject object = Utils.parseJson(retStr);
        try {
            Message msg = new Message();
            if(object.getInt("code") != REQUEST_CODE_SUCCESS)
            {
                msg.what = GET_DATA_ERROR;
            }
            else
            {
                JSONObject result = object.getJSONObject("result");
                JSONArray array = result.getJSONArray("list_data");
                int length = array.length();
                if(length == 0)
                {
                    msg.what = GET_DATE_EMPTY;
                }
                else
                {
                    ArrayList<Activities> list = new ArrayList<Activities>();
                    for(int i = 0; i < length; i++)
                    {

                        JSONObject item = array.getJSONObject(i);
                        Activities activityItem = Utils.mapingObject(Activities.class, item);
                        boolean flag = false;
                        for(int j = 0; j < mListData.size(); j++)
                        {
                            if(mListData.get(j).getId().equals(activityItem.getId()))
                            {
                                flag = true;
                            }
                        }

                        if(!flag)
                        {
                            list.add(activityItem);
                        }
                    }
                    mDao.insertActiviesList(mListData);
                    msg.what = GET_DATA_SUCCESS;
                    if(!isHead)
                    {
                        mListData.addAll(list);
                    }
                    else
                    {
                        mListData.addAll(0,list);
                    }
                    msg.obj = mListData;
                    mNetstart += LENGTH;
                    mNetend += LENGTH;
                }
            }
            handler.sendMessage(msg);

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


    @Override
    public void onRefresh() {
        new Thread(Utils.networkAvaiable(mActivity)
                ? getNetworkData : getLocalData).start();
        mNetstart = 0;
        mNetend = 9;
        isHead = true;
    }

    @Override
    public void onLoadMore() {
        new Thread(Utils.networkAvaiable(mActivity)
                ? getNetworkData : getLocalData).start();

        isHead = false;
    }

    private void onLoad() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mListView.setRefreshTime("刚刚");
    }
}