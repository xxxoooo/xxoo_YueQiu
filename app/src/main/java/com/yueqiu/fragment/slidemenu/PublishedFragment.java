package com.yueqiu.fragment.slidemenu;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.PublishedBasicAdapter;
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

/**
 * Created by wangyun on 15/1/15.
 */
public class PublishedFragment extends SlideMenuBasicFragment {
    private PublishedBasicAdapter mPublishedAdapter;
    private PublishedInfo mPublishedInfo;
    private PublishedDao mPublishedDao;
    private boolean mIsExsitsPublished;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);

        mPublishedDao = DaoFactory.getPublished(mActivity);
        mIsExsitsPublished = mPublishedDao.isExistPublishedInfo(mType);
        //TODO://可能会更改逻辑
        if(mIsExsitsPublished){
            mPublishedInfo = mPublishedDao.getPublishedInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()),mType,mStart_no,mEnd_no+1);
            mHandler.obtainMessage(PublicConstant.USE_CACHE,mPublishedInfo).sendToTarget();
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

        new RequestAsyncTask(mParamsMap).execute(mUrlAndMethodMap);
    }

    @Override
    protected Object setBeanByJSON(JSONObject jsonResult) {
        PublishedInfo published = new PublishedInfo();
        published.setType(mType);
        try {
            published.setStart_no(jsonResult.getJSONObject("result").getInt("start_no"));
            published.setEnd_no(jsonResult.getJSONObject("result").getInt("end_no"));
            published.setSumCount(jsonResult.getJSONObject("result").getInt("count"));
            JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");

            for (int i = 0; i < list_data.length(); i++) {
                PublishedInfo.PublishedItemInfo itemInfo = published.new PublishedItemInfo();
                itemInfo.setTable_id(list_data.getJSONObject(i).getString("id"));
                itemInfo.setTitle(list_data.getJSONObject(i).getString("title"));
                itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                itemInfo.setDateTime(list_data.getJSONObject(i).getString("create_time"));
                itemInfo.setType(Integer.valueOf(list_data.getJSONObject(i).getString("type_id")));
                itemInfo.setChecked(false);
                published.mList.add(itemInfo);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return published;
    }

    @Override
    protected BasicHandler getHandler() {
        return mHandler;
    }

    @Override
    protected void onItemStateChanged(int position,boolean checked) {
        PublishedInfo.PublishedItemInfo itemInfo = (PublishedInfo.PublishedItemInfo) mListView.getItemAtPosition(position);
        itemInfo.setChecked(checked);
        mPublishedAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPullUpWhenNetNotAvailable() {
        PublishedInfo info = mPublishedDao.getPublishedInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()),mType,mStart_no,mEnd_no+1);
        if(!info.mList.isEmpty()) {
            mHandler.obtainMessage(PublicConstant.GET_SUCCESS, info).sendToTarget();
        }else{
            mHandler.obtainMessage(PublicConstant.NO_RESULT,info).sendToTarget();
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

    private void updatePublishedDB(final PublishedInfo info){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mPublishedDao.isExistPublishedInfo(info.getType())){
                    if(mPublishedDao.updatePublishInfo(info) != -1){
                        for(int i=0;i<info.mList.size();i++){
                            PublishedInfo.PublishedItemInfo item =  info.mList.get(i);
                            if(mPublishedDao.isExistPublishedItemInfo(Integer.valueOf(item.getTable_id()),item.getType())){
                                mPublishedDao.updatePublishedItemInfo(info);
                            }else{
                                mPublishedDao.insertPublishItemInfo(info);
                            }
                        }
                    }
                }else {
                    mPublishedDao.insertPublishInfo(info);
                    mPublishedDao.insertPublishItemInfo(info);
                }
            }
        }).start();
    }


    private  BasicHandler mHandler = new BasicHandler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.USE_CACHE:
                    mList.addAll(((PublishedInfo)msg.obj).mList);
                    break;
                case PublicConstant.GET_SUCCESS:
                    if(mPullToRefreshListView.isRefreshing())
                        mPullToRefreshListView.onRefreshComplete();
                    mBeforeCount = mList.size();
                    PublishedInfo info = (PublishedInfo) msg.obj;
                    for(int i=0;i<info.mList.size();i++){
                        if(!mList.contains(info.mList.get(i))){
                            mList.add(info.mList.get(i));
                        }
                    }
                    updatePublishedDB((PublishedInfo) msg.obj);
                    mAfterCount = mList.size();
                    if(mList.isEmpty()){
                        setEmptyViewVisible();
                    }
                    break;
            }
            mPublishedAdapter = new PublishedBasicAdapter(mActivity,mList);
            mListView.setAdapter(mPublishedAdapter);
            if(mLoadMore || mRefresh) {
                mPublishedAdapter.notifyDataSetChanged();
            }
            if(mLoadMore && !mList.isEmpty()){
                mListView.setSelection(mCurrPosition);
            }
        }
    };
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mList.clear();
    }

}
