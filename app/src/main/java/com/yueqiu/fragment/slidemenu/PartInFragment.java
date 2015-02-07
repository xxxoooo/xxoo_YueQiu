package com.yueqiu.fragment.slidemenu;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.PartInAdapter;
import com.yueqiu.bean.PartInInfo;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/2/7.
 */
public class PartInFragment extends SlideMenuBasicFragment{
    private static final String SAVE_PARTIN_KEY = "save_publish";
    private static final String SAVE_PARTIN_REFRESH = "save_refresh";
    private static final String SAVE_PARTIN_LOAD_MORE = "save_load_more";
    private static final String SAVE_PARTIN_INSTANCE = "saved_instance";
    private PartInAdapter mPartInAdapter;
    //跟数据库相关的list
    private ArrayList<PartInInfo> mCacheList;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_PARTIN_KEY, mList);
        outState.putBoolean(SAVE_PARTIN_REFRESH,mRefresh);
        outState.putBoolean(SAVE_PARTIN_LOAD_MORE,mLoadMore);
        outState.putBoolean(SAVE_PARTIN_INSTANCE,true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);
        mPartInAdapter = new PartInAdapter(mActivity,mList);

        if(savedInstanceState != null){
            mRefresh = savedInstanceState.getBoolean(SAVE_PARTIN_REFRESH);
            mLoadMore = savedInstanceState.getBoolean(SAVE_PARTIN_LOAD_MORE);
            mIsSavedInstance = savedInstanceState.getBoolean(SAVE_PARTIN_INSTANCE);
            mCacheList = savedInstanceState.getParcelableArrayList(SAVE_PARTIN_KEY);
            mHandler.obtainMessage(PublicConstant.USE_CACHE,mCacheList).sendToTarget();
        }

        if(Utils.networkAvaiable(mActivity)){
            mLoadMore = false;
            mRefresh = false;
            requestResult();
        }else{
            mHandler.obtainMessage(PublicConstant.NO_NETWORK).sendToTarget();

        }
        return view;
    }

    private BasicHandler mHandler = new BasicHandler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.USE_CACHE:
                    setEmptyViewGone();
                    List<PartInInfo> cacheList = (List<PartInInfo>) msg.obj;
                    mList.addAll(cacheList);
                    break;
                case PublicConstant.GET_SUCCESS:
                    setEmptyViewGone();
                    mBeforeCount = mList.size();
                    List<PartInInfo> list = (List<PartInInfo>) msg.obj;
                    for(PartInInfo info : list){
                        if(!mList.contains(info)){
                            if(mRefresh){
                                mList.add(0,info);
                            }else{
                                if(mIsSavedInstance){
                                    mList.add(0,info);
                                }else{
                                    mList.add(info);
                                }
                            }
                        }
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
                    break;
            }
            mListView.setAdapter(mPartInAdapter);
            mPartInAdapter.notifyDataSetChanged();
            if(mLoadMore && !mList.isEmpty()){
                mListView.setSelection(mCurrPosition);
            }

        }
    };

    @Override
    protected void unCheckAll() {
        mPartInAdapter.unCheckAll();
        mPartInAdapter.notifyDataSetChanged();
    }

    @Override
    protected String getActionModeTitle() {
        return mActivity.getString(R.string.part_in_action_mode_title);
    }

    @Override
    protected void setEmptyViewText() {
        switch(mType){
            case PublicConstant.PART_IN_DATE_TYPE:
                mEmptyTypeStr = getString(R.string.search_billiard_dating_str);
                break;
            case PublicConstant.PART_IN_PLAY_TYPE:
                mEmptyTypeStr = getString(R.string.tab_title_activity);
                break;

        }
    }

    @Override
    protected void requestResult() {
        mParamsMap.put(DatabaseConstant.UserTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        mParamsMap.put(HttpConstants.Published.TYPE,mType);
        mParamsMap.put(HttpConstants.Published.START_NO,mStart_no);
        mParamsMap.put(HttpConstants.Published.END_NO, mEnd_no);

        mUrlAndMethodMap.put(PublicConstant.URL, HttpConstants.PartIn.URL);
        mUrlAndMethodMap.put(PublicConstant.METHOD, HttpConstants.RequestMethod.GET);

        new RequestAsyncTask<PublishedInfo>(mParamsMap).execute(mUrlAndMethodMap);
    }

    @Override
    protected List<PartInInfo> setBeanByJSON(JSONObject jsonResult) {
        List<PartInInfo> list = new ArrayList<PartInInfo>();
        try {
            JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");
            if(list_data.length() < 1){
                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
            }else {
                for (int i = 0; i < list_data.length(); i++) {
                    PartInInfo itemInfo = new PartInInfo();
                    itemInfo.setTable_id(list_data.getJSONObject(i).getString("id"));
                    itemInfo.setTitle(list_data.getJSONObject(i).getString("title"));
                    itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                    itemInfo.setDateTime(list_data.getJSONObject(i).getString("create_time"));
                    itemInfo.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                    itemInfo.setType(Integer.valueOf(list_data.getJSONObject(i).getString("type")));
                    itemInfo.setUsername(list_data.getJSONObject(i).getString("username"));
                    //TODO:根据服务器确定的字段,如果需要缓存应该再加一个字段subtype,代表这条数据是type中的那个子类型
                    //TODO:不过目前服务器那边说不传，不做缓存的话，倒是用不到这个字段
                    //itemInfo.setSubType(list_data.getJSONObject(i).getInt("subtype"));
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
        PartInInfo itemInfo = (PartInInfo) mListView.getItemAtPosition(position);
        itemInfo.setChecked(checked);
        mPartInAdapter .notifyDataSetChanged();
    }

    @Override
    protected void onPullUpWhenNetNotAvailable() {
        mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
    }
}
