package com.yueqiu.fragment.slidemenu;


import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.FavorBasicAdapter;
import com.yueqiu.bean.FavorInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.FavorDao;
import com.yueqiu.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by wangyun on 15/1/16.
 */
public class FavorBasicFragment extends SlideMenuBasicFragment {
    private FavorBasicAdapter mAdapter;
    private FavorInfo mFavorInfo;
    private FavorDao mFavorDao;
    private boolean mIsExsitsFavor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);
        mFavorDao = DaoFactory.getFavor(mActivity);
        mIsExsitsFavor = mFavorDao.isExistFavorInfo(mType);
        //先从缓存读取
        //TODO:逻辑可能再改
        if(mIsExsitsFavor){
            mFavorInfo = mFavorDao.getFavorInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()),mType,mStart_no,mEnd_no+1);
            mHandler.obtainMessage(PublicConstant.USE_CACHE,mFavorInfo).sendToTarget();
        }
        if(Utils.networkAvaiable(mActivity)){
            mLoadMore = false;
            mRefresh = false;
            requestResult();
        }else{
            if(mList.isEmpty()){
                mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
            }
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

        new RequestAsyncTask(mParamsMap).execute(mUrlAndMethodMap);
    }

    @Override
    protected Object setBeanByJSON(JSONObject jsonResult) {
        FavorInfo info = new FavorInfo();
        info.setType(mType);
        try {
            info.setStart_no(jsonResult.getJSONObject("result").getInt("start_no"));
            info.setEnd_no(jsonResult.getJSONObject("result").getInt("end_no"));
            info.setCount(jsonResult.getJSONObject("result").getInt("count"));
            JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");

            for (int i = 0; i < list_data.length(); i++) {
                FavorInfo.FavorItemInfo itemInfo = info.new FavorItemInfo();
                itemInfo.setTable_id(list_data.getJSONObject(i).getString("id"));
                itemInfo.setImage_url(list_data.getJSONObject(i).getString("img_url"));
                itemInfo.setTitle(list_data.getJSONObject(i).getString("title"));
                itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                itemInfo.setDateTime(list_data.getJSONObject(i).getString("create_time"));
                itemInfo.setUserName(list_data.getJSONObject(i).getString("username"));
                itemInfo.setType(Integer.valueOf(list_data.getJSONObject(i).getString("type_id")));
                itemInfo.setChecked(false);
                info.mList.add(itemInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return info;
    }

    @Override
    protected BasicHandler getHandler() {
        return mHandler;
    }

    @Override
    protected void onItemStateChanged(int position, boolean checked) {
        FavorInfo.FavorItemInfo itemInfo = (FavorInfo.FavorItemInfo) mListView.getItemAtPosition(position);
        itemInfo.setChecked(checked);
    }

    @Override
    protected void onPullUpWhenNetNotAvailable() {
        FavorInfo info= mFavorDao.getFavorInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()), mType, mStart_no, mEnd_no + 1);
        if(!info.mList.isEmpty()) {
            mHandler.obtainMessage(PublicConstant.GET_SUCCESS, info).sendToTarget();
        }else{
            mHandler.obtainMessage(PublicConstant.NO_RESULT,info).sendToTarget();
        }
    }

    @Override
    protected void setEmptyViewVisible(){
        super.setEmptyViewVisible();
        mEmptyView.setText(getString(R.string.your_favor_is_empty,mEmptyTypeStr));
        mPullToRefreshListView.setEmptyView(mEmptyView);

    }

    private void updateFavorDB(final FavorInfo info){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mIsExsitsFavor){
                    if(mFavorDao.updateFavorInfo(info) != -1){
                        for(int i=0;i<info.mList.size();i++){
                            FavorInfo.FavorItemInfo item =  info.mList.get(i);
                            if(mFavorDao.isExistFavorItemInfo(Integer.valueOf(item.getTable_id()),item.getType())){
                                mFavorDao.updateFavorItemInfo(info);
                            }else{
                                mFavorDao.insertFavorItemInfo(info);
                            }
                        }
                    }
                }else {
                    mFavorDao.insertFavorInfo(info);
                    mFavorDao.insertFavorItemInfo(info);
                }
            }
        }).start();
    }


    private BasicHandler mHandler = new BasicHandler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.USE_CACHE:
                    mList.addAll(((FavorInfo)msg.obj).mList);
                    break;
                case PublicConstant.GET_SUCCESS:
                    if(mPullToRefreshListView.isRefreshing())
                        mPullToRefreshListView.onRefreshComplete();
                    mBeforeCount = mList.size();
                    FavorInfo info = (FavorInfo) msg.obj;
                    for(int i=0;i<info.mList.size();i++){
                        if(!mList.contains(info.mList.get(i))){
                            mList.add(info.mList.get(i));
                        }
                    }
                    updateFavorDB((FavorInfo) msg.obj);
                    mAfterCount = mList.size();
                    if(mList.isEmpty()){
                        setEmptyViewVisible();
                    }
                    break;
            }
            mAdapter = new FavorBasicAdapter(mActivity,mList);
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            if(mLoadMore && !mList.isEmpty()){
                mListView.setSelection(mCurrPosition);
            }
        }
    };







}
