package com.yueqiu.fragment.slidemenu;


import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.FavorBasicAdapter;
import com.yueqiu.bean.FavorInfo;
import com.yueqiu.bean.Identity;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.FavorDao;
import com.yueqiu.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wangyun on 15/1/16.
 */
public class FavorBasicFragment extends SlideMenuBasicFragment {
    private FavorBasicAdapter mAdapter;
    private FavorDao mFavorDao;

    //跟数据库相关的list
    private List<FavorInfo> mDBList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);
        mFavorDao = DaoFactory.getFavor(mActivity);
        mAdapter = new FavorBasicAdapter(mActivity,mList);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mDBList = mFavorDao.getFavorLimit(YueQiuApp.sUserInfo.getUser_id(),mType,mStart_no,10);
                if(!mDBList.isEmpty()){
                    mHandler.obtainMessage(PublicConstant.USE_CACHE,mDBList).sendToTarget();
                }
            }
        }).start();

        if(Utils.networkAvaiable(mActivity)){
            mLoadMore = false;
            mRefresh = false;
            requestResult();
        }else{
            mHandler.obtainMessage(PublicConstant.NO_NETWORK).sendToTarget();
        }
        return view;
    }

    @Override
    protected void unCheckAll() {
        mAdapter.unCheckAll();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected String getActionModeTitle() {
        return getString(R.string.my_colloec_action_mode_title);
    }

    @Override
    protected void setEmptyViewText(){
        switch(mType){
            case PublicConstant.FAVOR_DATE_TYPE:
                mEmptyTypeStr = getString(R.string.search_billiard_dating_str);
                break;
            case PublicConstant.FAVPR_ROOM_TYPE:
                mEmptyTypeStr = getString(R.string.search_billiard_room_str);
                break;
            case PublicConstant.FAVOR_ACTIVITY_TYPE:
                mEmptyTypeStr = getString(R.string.tab_title_activity);
                break;
            case PublicConstant.FAVOR_GROUP_TYPR:
                mEmptyTypeStr = getString(R.string.tab_title_billiards_circle);
                break;
        }
    }



    @Override
    protected void requestResult() {
        mParamsMap.put(DatabaseConstant.UserTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        mParamsMap.put(HttpConstants.Favor.TYPE,mType);
        mParamsMap.put(HttpConstants.Favor.START_NO,mStart_no);
        mParamsMap.put(HttpConstants.Favor.END_NO, mEnd_no);

        mUrlAndMethodMap.put(PublicConstant.URL, HttpConstants.Favor.URL);
        mUrlAndMethodMap.put(PublicConstant.METHOD, HttpConstants.RequestMethod.GET);

        new RequestAsyncTask<FavorInfo>(mParamsMap).execute(mUrlAndMethodMap);
    }

    @Override
    protected List<FavorInfo> setBeanByJSON(JSONObject jsonResult) {
        List<FavorInfo> list = new ArrayList<FavorInfo>();
        try {
            JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");
            if(list_data.length() < 1){
                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
            }else{
                for (int i = 0; i < list_data.length(); i++) {
                    FavorInfo itemInfo = new FavorInfo();
                    itemInfo.setTable_id(list_data.getJSONObject(i).getString("id"));
                    itemInfo.setTitle(list_data.getJSONObject(i).getString("title"));

                    itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                    itemInfo.setCreateTime(list_data.getJSONObject(i).getString("create_time"));
                    itemInfo.setUserName(list_data.getJSONObject(i).getString("username"));
                    itemInfo.setType(Integer.valueOf(list_data.getJSONObject(i).getString("type")));
                    itemInfo.setChecked(false);
                    list.add(itemInfo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    protected BasicHandler getHandler() {
        return mHandler;
    }

    @Override
    protected void onItemStateChanged(int position, boolean checked) {
        FavorInfo itemInfo = (FavorInfo) mListView.getItemAtPosition(position);
        itemInfo.setChecked(checked);
    }

    @Override
    protected void onPullUpWhenNetNotAvailable() {
        List<FavorInfo> list= mFavorDao.getFavorLimit(YueQiuApp.sUserInfo.getUser_id(), mType, mStart_no, 10);
        if(!list.isEmpty()) {
            mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
        }else{
            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
        }
    }

    @Override
    protected void setEmptyViewVisible(){
        super.setEmptyViewVisible();
        mEmptyView.setText(getString(R.string.your_favor_is_empty,mEmptyTypeStr));
        mPullToRefreshListView.setEmptyView(mEmptyView);

    }

    private void updateDB(){
        if(!mFavorUpdateList.isEmpty()){
            mFavorDao.updateFavorInfo(mFavorUpdateList);
        }
        if(!mFavorInsertList.isEmpty()){
            long result = mFavorDao.insertFavorInfo(mFavorInsertList);
            //TODO:目前noteId没有unique，可能还会存在重复插入的问题，还需要再验证
            /**
             * 插入失败，则更新数据库
             */
            if(result == -1){
                mFavorDao.updateFavorInfo(mFavorInsertList);
            }
        }
    }

    private BasicHandler mHandler = new BasicHandler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.USE_CACHE:
                    List<FavorInfo> cacheList = (List<FavorInfo>) msg.obj;
                    mList.addAll(cacheList);
                    break;
                case PublicConstant.GET_SUCCESS:
                    mBeforeCount = mList.size();
                    List<FavorInfo> list = (List<FavorInfo>) msg.obj;
                    for(FavorInfo info : list){
                        if(!mList.contains(info)){
                            mList.add(info);
                        }
                        Identity identity = new Identity();
                        identity.user_id = YueQiuApp.sUserInfo.getUser_id();
                        identity.table_id = info.getTable_id();
                        identity.type = info.getType();
                        if(!YueQiuApp.sFavorMap.containsKey(identity)){
                            mFavorInsertList.add(info);
                        }else{
                            mFavorUpdateList.add(info);
                        }
                        YueQiuApp.sFavorMap.put(identity,info);
                    }
                    mAfterCount = mList.size();
                    if(mList.isEmpty()){
                        setEmptyViewVisible();
                    }else{
                        if(mRefresh){
                            if (mAfterCount == mBeforeCount) {
                                Utils.showToast(mActivity, getString(R.string.no_newer_info));
                            } else {
                                Utils.showToast(mActivity, getString(R.string.have_already_update_info, mAfterCount - mBeforeCount));
                            }
                        }
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            updateDB();
                        }
                    }).start();
                    break;
            }
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            if(mLoadMore && !mList.isEmpty()){
                mListView.setSelection(mCurrPosition);
            }
        }
    };







}
