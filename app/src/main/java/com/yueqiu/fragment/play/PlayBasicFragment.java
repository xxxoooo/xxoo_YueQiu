package com.yueqiu.fragment.play;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.PlayDetailActivity;
import com.yueqiu.adapter.PlayListViewAdapter;
import com.yueqiu.bean.PlayIdentity;
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.PlayDao;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayBasicFragment extends Fragment implements AdapterView.OnItemClickListener{
    private Activity mActivity;
    private View mView;
    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;
    private PlayListViewAdapter mAdapter;
    private PlayDao mPlayDao;
    private ProgressBar mPreProgressBar;
    private TextView mEmptyView,mPreTextView;
    private boolean mRefresh,mLoadMore;
    private int mPlayType;
    private Drawable mProgressDrawable;
    private String mEmptyTypeStr;
    private int mStart = 0,mEnd = 9;
    private int mBeforeCount,mAfterCount;
    private int mCurrPosition;
    private Map<String,Integer> mParamMap = new HashMap<String, Integer>();
    private Map<String,String> mUrlAndMethodMap = new HashMap<String, String>();
    private List<PlayInfo> mList = new ArrayList<PlayInfo>();
    private List<PlayInfo> mInsertList = new ArrayList<PlayInfo>();
    private List<PlayInfo> mUpdateList = new ArrayList<PlayInfo>();
    private List<PlayInfo> mDBList = new ArrayList<PlayInfo>();
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_play_bussiness,null);
        mPlayType = getArguments().getInt("type");
        mPlayDao = DaoFactory.getPlay(mActivity);
        mAdapter = new PlayListViewAdapter(mActivity,mList);
        initView();
        setmEmptyStr();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mDBList = mPlayDao.getPlayInfoLimit(mPlayType,mStart,10);
                if(!mDBList.isEmpty()){
                    mHandler.obtainMessage(PublicConstant.USE_CACHE,mDBList).sendToTarget();
                }
            }
        }).start();

        if(Utils.networkAvaiable(mActivity)){
            mLoadMore = false;
            mRefresh = false;
            requestPlay();
        }else{
            mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
        }

        return mView;
    }

    private void initView()
    {
        mPullToRefreshListView = (PullToRefreshListView) mView.findViewById(R.id.activity_activities_lv);
        mListView = mPullToRefreshListView.getRefreshableView();
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshListView.setOnRefreshListener(mRefreshListener);
        mPreProgressBar = (ProgressBar) mView.findViewById(R.id.pre_progress);
        mPreTextView = (TextView) mView.findViewById(R.id.pre_text);
        mEmptyView = new TextView(mActivity);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(mActivity).build();
        Rect bounds = mPreProgressBar.getIndeterminateDrawable().getBounds();
        mPreProgressBar.setIndeterminateDrawable(mProgressDrawable);
        mPreProgressBar.getIndeterminateDrawable().setBounds(bounds);

        mListView.setOnItemClickListener(this);

    }
    private void setmEmptyStr(){
        switch(mPlayType){
            case PublicConstant.PLAY_GROUP:
                mEmptyTypeStr = mActivity.getString(R.string.group_activity);
                break;
            case PublicConstant.PLAY_MEET_STAR:
                mEmptyTypeStr = mActivity.getString(R.string.star_meet);
                break;
            case PublicConstant.PLAY_BILLIARD_SHOW:
                mEmptyTypeStr = mActivity.getString(R.string.billiard_show);
                break;
            case PublicConstant.PLAY_COMPETITION:
                mEmptyTypeStr = mActivity.getString(R.string.complete);
                break;
            case PublicConstant.PLAY_OTHER_ACTIVITY:
                mEmptyTypeStr = mActivity.getString(R.string.billiard_other);
                break;
        }
    }

    private void setEmptyViewVisible(){

        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        mEmptyView.setTextColor(getResources().getColor(R.color.md__defaultBackground));
        mEmptyView.setText(mActivity.getString(R.string.no_play_info, mEmptyTypeStr));
        mPullToRefreshListView.setEmptyView(mEmptyView);
    }
    protected void setEmptyViewGone(){
        if(null != mEmptyView){
            mEmptyView.setVisibility(View.GONE);
        }
    }

    private void requestPlay(){
        mParamMap.put(HttpConstants.Play.TYPE, mPlayType);
        mParamMap.put(HttpConstants.Play.START_NO,mStart);
        mParamMap.put(HttpConstants.Play.END_NO,mEnd);

        mUrlAndMethodMap.put(PublicConstant.URL,HttpConstants.Play.GETLISTEE);
        mUrlAndMethodMap.put(PublicConstant.METHOD,HttpConstants.RequestMethod.GET);

        new RequestPlayTask(mParamMap).execute(mUrlAndMethodMap);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PlayInfo info = (PlayInfo) mAdapter.getItem(position-1);
        Intent intent = new Intent(mActivity, PlayDetailActivity.class);
        Bundle args = new Bundle();
        args.putInt(DatabaseConstant.PlayTable.TABLE_ID,Integer.parseInt(info.getTable_id()));
        args.putString(DatabaseConstant.PlayTable.CREATE_TIME,info.getCreate_time());
        args.putString(DatabaseConstant.PlayTable.TYPE,info.getType());
        intent.putExtras(args);
        startActivity(intent);
        mActivity.overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
    }

    private class RequestPlayTask extends AsyncTaskUtil<Integer>{

        public RequestPlayTask(Map<String, Integer> map) {
            super(map);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreProgressBar.setVisibility(View.VISIBLE);
            mPreTextView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mPreProgressBar.setVisibility(View.GONE);
            mPreTextView.setVisibility(View.GONE);
            try{
                if(!jsonObject.isNull("code")){
                    if(jsonObject.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                        if(jsonObject.getString("result") != null){
                            List<PlayInfo> list = setPlayByJSON(jsonObject);
                            if(list.isEmpty()){
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }else{
                                mHandler.obtainMessage(PublicConstant.GET_SUCCESS,list).sendToTarget();
                            }
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

    private List<PlayInfo> setPlayByJSON(JSONObject object){
        List<PlayInfo> list = new ArrayList<PlayInfo>();
        try{
            JSONArray list_data = object.getJSONObject("result").getJSONArray("list_data");
            for(int i=0;i<list_data.length();i++){
                PlayInfo info = new PlayInfo();
                info.setTable_id(list_data.getJSONObject(i).getString("id"));
                info.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                info.setTitle(list_data.getJSONObject(i).getString("title"));
                info.setContent(list_data.getJSONObject(i).getString("content"));
                info.setCreate_time(list_data.getJSONObject(i).getString("create_time"));
                info.setType(String.valueOf(mPlayType));
                list.add(info);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return list;
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mPullToRefreshListView.isRefreshing())
                mPullToRefreshListView.onRefreshComplete();
            switch (msg.what){
                case PublicConstant.USE_CACHE:
                    setEmptyViewGone();
                    List<PlayInfo> cacheList = (List<PlayInfo>) msg.obj;
                    mList.addAll(cacheList);
                    break;
                case PublicConstant.GET_SUCCESS:
                    setEmptyViewGone();
                    mBeforeCount = mList.size();
                    List<PlayInfo> list = (List<PlayInfo>) msg.obj;
                    for(PlayInfo info : list){
                        if(!mList.contains(info)){
                            mList.add(info);
                        }
                        PlayIdentity identity = new PlayIdentity();
                        identity.type = info.getType();
                        identity.table_id = info.getTable_id();
                        if(!YueQiuApp.sPlayMap.containsKey(identity)){
                            mInsertList.add(info);
                        }else{
                            mUpdateList.add(info);
                        }
                        YueQiuApp.sPlayMap.put(identity,info);
                    }
                    mAfterCount = mList.size();
                    if(mList.isEmpty()){
                       setEmptyViewVisible();
                    }else{
                        if(mRefresh){
                            if (mAfterCount == mBeforeCount) {
                                Utils.showToast(mActivity, mActivity.getString(R.string.no_newer_info));
                            } else {
                                Utils.showToast(mActivity, mActivity.getString(R.string.have_already_update_info, mAfterCount - mBeforeCount));
                            }
                        }
                    }
                    //TODO:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            updatePlayInfoDB();
                        }
                    }).start();


                    break;
                case PublicConstant.NO_RESULT:
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }else{
                        if(mLoadMore) {
                            Utils.showToast(mActivity,mActivity.getString(R.string.no_more_info,mEmptyTypeStr));
                        }
                    }
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(mActivity, mActivity.getString(R.string.http_request_time_out));
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(mActivity,mActivity.getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(mActivity, (String) msg.obj);
                    }
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }
                    break;
                case PublicConstant.NO_NETWORK:
                    Utils.showToast(mActivity,mActivity.getString(R.string.network_not_available));
                    if(mList.isEmpty())
                        setEmptyViewVisible();
                    break;
            }
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            if(mLoadMore && !mList.isEmpty()){
                mListView.setSelection(mCurrPosition);
            }
        }
    };

    private void updatePlayInfoDB(){
        if(!mUpdateList.isEmpty()){
            mPlayDao.updatesPlayInfo(mUpdateList);
        }
        if(!mInsertList.isEmpty()){
            long result = mPlayDao.insertPlayInfo(mInsertList);
            //TODO:目前noteId没有unique，可能还会存在重复插入的问题，还需要再验证
            /**
             * 插入失败，则更新数据库
             */
            if(result == -1){
                mPlayDao.updatesPlayInfo(mInsertList);
            }
        }
    }

    private PullToRefreshBase.OnRefreshListener2<ListView> mRefreshListener = new PullToRefreshBase.OnRefreshListener2<ListView>() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
            String label = DateUtils.formatDateTime(mActivity, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            mRefresh = true;
            mLoadMore = false;
            mInsertList.clear();
            mUpdateList.clear();
            if(Utils.networkAvaiable(mActivity)){
                mParamMap.put(HttpConstants.GroupList.STAR_NO,0);
                mParamMap.put(HttpConstants.GroupList.END_NO,9);
                requestPlay();
            }else{
                mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
            }


        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            String label = DateUtils.formatDateTime(mActivity, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            mLoadMore = true;
            mRefresh = false;
            mCurrPosition = mList.size() ;
            mInsertList.clear();
            mUpdateList.clear();

            if(mBeforeCount != mAfterCount){
                mStart = mEnd + (mAfterCount - mBeforeCount);
                mEnd += 10 + (mAfterCount - mBeforeCount);
            }else{
                mStart = mEnd + 1;
                mEnd += 10;
            }
            if(Utils.networkAvaiable(mActivity)){
                mParamMap.put(HttpConstants.GroupList.STAR_NO,mStart);
                mParamMap.put(HttpConstants.GroupList.END_NO,mEnd);
                requestPlay();
            }
            else{
                List<PlayInfo> list = mPlayDao.getPlayInfoLimit(mPlayType, mStart, 10);
                if(list.isEmpty()){
                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                }else{
                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS,list);
                }
            }


        }
    };

}
