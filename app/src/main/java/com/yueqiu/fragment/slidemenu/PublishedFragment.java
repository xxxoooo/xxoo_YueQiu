package com.yueqiu.fragment.slidemenu;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.PublishedBasicAdapter;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.bean.Identity;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.PublishedDao;
import com.yueqiu.util.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/1/15.
 */
public class PublishedFragment extends SlideMenuBasicFragment {
    private PublishedBasicAdapter mPublishedAdapter;
    private PublishedDao mPublishedDao;
    //跟数据库相关的list
    private List<PublishedInfo> mDBList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);
        mPublishedAdapter = new PublishedBasicAdapter(mActivity,mList);
        mPublishedDao = DaoFactory.getPublished(mActivity);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mDBList = mPublishedDao.getPublishedInfo(YueQiuApp.sUserInfo.getUser_id(),mType,mStart_no,10);
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

    protected void setEmptyViewText(){
        switch(mType){
            case PublicConstant.PUBLISHED_DATE_TYPE:
                mEmptyTypeStr = getString(R.string.search_billiard_dating_str);
                break;
            case PublicConstant.PUBLISHED_ACTIVITY_TYPE:
                mEmptyTypeStr = getString(R.string.tab_title_activity);
                break;
            case PublicConstant.PUBLISHED_GROUP_TYPE:
                mEmptyTypeStr = getString(R.string.tab_title_billiards_circle);
                break;

        }
    }

    @Override
    protected void requestResult() {
        mParamsMap.put(DatabaseConstant.UserTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        mParamsMap.put(HttpConstants.Published.TYPE,mType);
        mParamsMap.put(HttpConstants.Published.START_NO,mStart_no);
        mParamsMap.put(HttpConstants.Published.END_NO, mEnd_no);

        mUrlAndMethodMap.put(PublicConstant.URL, HttpConstants.Published.URL);
        mUrlAndMethodMap.put(PublicConstant.METHOD, HttpConstants.RequestMethod.GET);

        new RequestAsyncTask<PublishedInfo>(mParamsMap).execute(mUrlAndMethodMap);
    }

    @Override
    protected List<PublishedInfo> setBeanByJSON(JSONObject jsonResult) {
        List<PublishedInfo> list = new ArrayList<PublishedInfo>();
        try {
            JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");
            if(list_data.length() < 1){
                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
            }else {
                for (int i = 0; i < list_data.length(); i++) {
                    PublishedInfo itemInfo = new PublishedInfo();
                    itemInfo.setTable_id(list_data.getJSONObject(i).getString("id"));
                    itemInfo.setTitle(list_data.getJSONObject(i).getString("title"));
                    itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                    itemInfo.setDateTime(list_data.getJSONObject(i).getString("create_time"));
                    itemInfo.setType(Integer.valueOf(list_data.getJSONObject(i).getString("type_id")));
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
    protected void onItemStateChanged(int position,boolean checked) {
        PublishedInfo itemInfo = (PublishedInfo) mListView.getItemAtPosition(position);
        itemInfo.setChecked(checked);
        mPublishedAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPullUpWhenNetNotAvailable() {
        //TODO:有错误,还要改，limit的number不应该用mEnd,固定为10条
        List<PublishedInfo> info = mPublishedDao.getPublishedInfo(YueQiuApp.sUserInfo.getUser_id(),mType,mStart_no,10);
        if(!info.isEmpty()) {
            mHandler.obtainMessage(PublicConstant.GET_SUCCESS, info).sendToTarget();
        }else{
            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
        }
    }

    protected void setEmptyViewVisible(){
        super.setEmptyViewVisible();
        mEmptyView.setText(getString(R.string.your_published_info_is_empty,mEmptyTypeStr));
        mPullToRefreshListView.setEmptyView(mEmptyView);
    }

    @Override
    protected void unCheckAll() {
        mPublishedAdapter.unCheckAll();
        mPublishedAdapter.notifyDataSetChanged();

    }

    @Override
    protected String getActionModeTitle() {
        return mActivity.getString(R.string.published_action_mode_title);
    }

    private void updatePublishDB(){
        if(!mPublishUpdateList.isEmpty()){
            mPublishedDao.updatePublishInfo(mPublishUpdateList);
        }
        if(!mPublishInsertList.isEmpty()){
            long result = mPublishedDao.insertPublishInfo(mPublishInsertList);
            //TODO:目前noteId没有unique，可能还会存在重复插入的问题，还需要再验证
            /**
             * 插入失败，则更新数据库
             */
            if(result == -1){
                mPublishedDao.updatePublishInfo(mPublishInsertList);
            }
        }
    }

    private  BasicHandler mHandler = new BasicHandler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.USE_CACHE:
                    List<PublishedInfo> cacheList = (List<PublishedInfo>) msg.obj;
                    mList.addAll(cacheList);
                    break;
                case PublicConstant.GET_SUCCESS:
                    mBeforeCount = mList.size();
                    List<PublishedInfo> list = (List<PublishedInfo>) msg.obj;
                    for(PublishedInfo info : list){
                        if(!mList.contains(info)){
                            mList.add(info);
                        }
                        Identity identity = new Identity();
                        identity.user_id = YueQiuApp.sUserInfo.getUser_id();
                        identity.table_id = info.getTable_id();
                        identity.type = info.getType();
                        Log.d("wy",YueQiuApp.sPublishMap.containsKey(identity) + "");
                        if(!YueQiuApp.sPublishMap.containsKey(identity)){
                            mPublishInsertList.add(info);
                        }else{
                            mPublishUpdateList.add(info);
                        }
                        YueQiuApp.sPublishMap.put(identity,info);
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

                    //TODO:更新数据库
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                           updatePublishDB();

                        }
                    }).start();


                    break;
            }
            mListView.setAdapter(mPublishedAdapter);
            mPublishedAdapter.notifyDataSetChanged();
            if(mLoadMore && !mList.isEmpty()){
                mListView.setSelection(mCurrPosition);
            }
        }
    };


}
