package com.yueqiu.fragment.slidemenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.SearchView;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.BilliardGroupDetailActivity;
import com.yueqiu.activity.NearbyBilliardsDatingActivity;
import com.yueqiu.activity.PlayDetailActivity;
import com.yueqiu.activity.SearchResultActivity;
import com.yueqiu.adapter.PublishedBasicAdapter;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.bean.Identity;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.PublishedDao;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/1/15.
 */
public class PublishedFragment extends SlideMenuBasicFragment implements AdapterView.OnItemClickListener{
    private static final String SAVE_PUBLISH_KEY = "save_publish";
    private static final String SAVE_PUBLISH_REFRESH = "save_refresh";
    private static final String SAVE_PUBLISH_LOAD_MORE = "save_load_more";
    private static final String SAVE_PUBLISH_INSTANCE = "saved_instance";
    private PublishedBasicAdapter mPublishedAdapter;
    private PublishedDao mPublishedDao;
    //跟数据库相关的list
    private ArrayList<PublishedInfo> mCacheList;
    private SearchView mSearchView;
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO:是否需要在这里保存数据？如果在滑动过程中断网了，那么在滑回来的时候
        //TODO:按照现在不用缓存的逻辑页面就是空的
        outState.putParcelableArrayList(SAVE_PUBLISH_KEY, mList);
        outState.putBoolean(SAVE_PUBLISH_REFRESH,mRefresh);
        outState.putBoolean(SAVE_PUBLISH_LOAD_MORE,mLoadMore);
        outState.putBoolean(SAVE_PUBLISH_INSTANCE,true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);
        setHasOptionsMenu(true);
        mPublishedAdapter = new PublishedBasicAdapter(mActivity,mList);
        mPublishedDao = DaoFactory.getPublished(mActivity);
        /////////////////////////////////////////////////////////////////////////////////////////////////
        //TODO:先去掉缓存功能，后期再根据需求加回来，目前逻辑没问题
        //TODO:同样是可以考虑用更有效率的loader或其他异步方法，而不是
        //TODO:简单地用线程，线程的生命周期不可控
        /**
         * mCacheList是缓存list，从数据库中根据当前type值获取到前十条
         * 如果mCacheList不为空就先使用该list填充listview
         */
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mCacheList = mPublishedDao.getPublishedInfo(YueQiuApp.sUserInfo.getUser_id(),mType,mStart_no,10);
//                if(!mCacheList.isEmpty()){
//                    mHandler.obtainMessage(PublicConstant.USE_CACHE,mCacheList).sendToTarget();
//                }
//            }
//        }).start();
        //////////////////////////////////////////////////////////////////////////////////////////////////

        if(savedInstanceState != null){
            mRefresh = savedInstanceState.getBoolean(SAVE_PUBLISH_REFRESH);
            mLoadMore = savedInstanceState.getBoolean(SAVE_PUBLISH_LOAD_MORE);
            mIsSavedInstance = savedInstanceState.getBoolean(SAVE_PUBLISH_INSTANCE);
            mCacheList = savedInstanceState.getParcelableArrayList(SAVE_PUBLISH_KEY);
            mHandler.obtainMessage(PublicConstant.USE_CACHE,mCacheList).sendToTarget();
        }
        //TODO:savedInstance为null证明该是第一次进入到viewpager,需要从数据库中读缓存
        //TODO:后期要做缓存的时候可以将上面读缓存的代码放到else里面
//        else{
//
//        }
        


        mListView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Utils.networkAvaiable(mActivity)){
            mLoadMore = false;
            mRefresh = false;
            requestResult();
        }else{
            mHandler.obtainMessage(PublicConstant.NO_NETWORK).sendToTarget();

        }
        if(mSearchView != null){
            mSearchView.clearFocus();
        }
    }

    protected void setEmptyViewText(){
        switch(mType){
            case PublicConstant.PUBLISHED_DATE_TYPE:
                mEmptyTypeStr = getString(R.string.nearby_billiard_dating_str);
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

        mPreProgress.setVisibility(View.VISIBLE);
        if(mList.isEmpty()) {
            mPreText.setVisibility(View.VISIBLE);
        }

        mParamsMap.put(DatabaseConstant.UserTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        mParamsMap.put(HttpConstants.Published.TYPE,mType);
        mParamsMap.put(HttpConstants.Published.START_NO,mStart_no);
        mParamsMap.put(HttpConstants.Published.END_NO, mEnd_no);

        HttpUtil.requestHttp(HttpConstants.Published.URL,mParamsMap,HttpConstants.RequestMethod.GET,new ResponseHandler<PublishedInfo>());

    }

    @Override
    protected List<PublishedInfo> setBeanByJSON(JSONObject jsonResult) {
        List<PublishedInfo> list = new ArrayList<PublishedInfo>();
        try {
            if(jsonResult.getJSONObject("result").get("list_data").equals("null")){
                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
            }else {
                JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");
                if (list_data.length() < 1) {
                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                } else {
                    for (int i = 0; i < list_data.length(); i++) {
                        PublishedInfo itemInfo = new PublishedInfo();
                        itemInfo.setTable_id(list_data.getJSONObject(i).getString("id"));
                        itemInfo.setTitle(list_data.getJSONObject(i).getString("title"));
                        itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                        itemInfo.setDateTime(list_data.getJSONObject(i).getString("create_time"));
                        itemInfo.setType(Integer.valueOf(list_data.getJSONObject(i).getString("type_id")));
                        //TODO:根据服务器确定的字段,如果需要缓存应该再加一个字段subtype,代表这条数据是type中的那个子类型
                        //TODO:不过目前服务器那边说不传，不做缓存的话，倒是用不到这个字段
                        //itemInfo.setSubType(list_data.getJSONObject(i).getInt("subtype"));
                        itemInfo.setChecked(false);
                        list.add(itemInfo);
                    }
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
        //TODO:由于不用缓存，所以直接toast无网络，如果后期需要缓存了，再换回这段代码

//        List<PublishedInfo> info = mPublishedDao.getPublishedInfo(YueQiuApp.sUserInfo.getUser_id(),mType,mStart_no,10);
//        if(!info.isEmpty()) {
//            mHandler.obtainMessage(PublicConstant.GET_SUCCESS, info).sendToTarget();
//        }else{
//            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
//        }
        mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
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
    //TODO:目前不需要缓存，所以这个方法暂时不调用
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
                    setEmptyViewGone();
                    List<PublishedInfo> cacheList = (List<PublishedInfo>) msg.obj;
                    mList.addAll(cacheList);
                    if(mList.isEmpty())
                        setEmptyViewVisible(mActivity.getString(R.string.your_published_info_is_empty,mEmptyTypeStr));
                    break;
                case PublicConstant.GET_SUCCESS:
                    setEmptyViewGone();
                    mBeforeCount = mList.size();
                    mIsListEmpty = mList.isEmpty();
                    List<PublishedInfo> list = (List<PublishedInfo>) msg.obj;
                    for(PublishedInfo info : list){
                        if (!mList.contains(info)) {
                            if(!mIsListEmpty && Integer.valueOf(((PublishedInfo)mList.get(0)).getTable_id()) < Integer.valueOf(info.getTable_id())){
                                mList.add(0,info);
                            }else {
                                mList.add(info);
                            }
                        }
                        //////////////////////////////////////////////////
                        //TODO:下面的逻辑是用来更新缓存的，不过目前先不需要缓存
                        //TODO:如果后期功能需要加缓存再加回来，目前的逻辑暂时没问题
                        /**
                         * 根据这条publishinfo的type和tableId,以及userId值生成唯一的key值
                         * 如果全局的publishMap里有这条数据，则将这条数据加入
                         * updateList，如果这条数据在playMap里不存在将将这条数据加入insertList
                         */
//                        Identity identity = new Identity();
//                        identity.user_id = YueQiuApp.sUserInfo.getUser_id();
//                        identity.table_id = info.getTable_id();
//                        identity.type = info.getType();
//                        if(!YueQiuApp.sPublishMap.containsKey(identity)){
//                            mPublishInsertList.add(info);
//                        }else{
//                            mPublishUpdateList.add(info);
//                        }
                        /**
                         * Map在put值的时候，如果key值已经存在则会用新的value覆盖
                         * 原先的value，如果key值不存在则直接放入，所以这里publishInfo是
                         * 放入updateList还是insertList，这里都要执行以下put操作，用来更新
                         * 全局的map
                         */
//                        YueQiuApp.sPublishMap.put(identity,info);
                        /////////////////////////////////////////////////////
                    }
                    mAfterCount = mList.size();
                    if(mList.isEmpty()){
                        setEmptyViewVisible(mActivity.getString(R.string.no_your_published_info,mEmptyTypeStr));
                    }else{
                        if(mRefresh){
                            if (mAfterCount == mBeforeCount) {
                                Utils.showToast(mActivity, mActivity.getString(R.string.no_newer_info));
                            } else {
                                Utils.showToast(mActivity, mActivity.getString(R.string.have_already_update_info, mAfterCount - mBeforeCount));
                            }
                        }
                    }

                    //TODO:更新数据库,目前不需要缓存，所以先不用执行这一步
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                           updatePublishDB();
//
//                        }
//                    }).start();


                    break;
            }
            mListView.setAdapter(mPublishedAdapter);
            mPublishedAdapter.notifyDataSetChanged();
            if(mLoadMore && !mList.isEmpty()){
                mListView.setSelection(mCurrPosition - 1);
            }
        }
    };



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent;
        PublishedInfo info = (PublishedInfo) mPublishedAdapter.getItem(position-1);
        int table_id = Integer.valueOf(info.getTable_id());
        String username = YueQiuApp.sUserInfo.getUsername();
        String img_url = YueQiuApp.sUserInfo.getImg_url();

        String create_time = info.getDateTime();

        switch (mType){
            case PublicConstant.PUBLISHED_ACTIVITY_TYPE:
                Bundle playArg = new Bundle();
                playArg.putInt(DatabaseConstant.PlayTable.TABLE_ID,table_id);
                playArg.putString(DatabaseConstant.PlayTable.CREATE_TIME,create_time);

                intent = new Intent(mActivity, PlayDetailActivity.class);
                intent.putExtras(playArg);
                startActivity(intent);
                break;
            case PublicConstant.PUBLISHED_DATE_TYPE:
                Bundle dateArg = new Bundle();
                dateArg.putInt(NearbyFragmentsCommonUtils.KEY_DATING_TABLE_ID,table_id);
                dateArg.putString(NearbyFragmentsCommonUtils.KEY_DATING_FRAGMENT_PHOTO,img_url);
                dateArg.putString(NearbyFragmentsCommonUtils.KEY_DATING_USER_NAME,username);

                intent = new Intent(mActivity, NearbyBilliardsDatingActivity.class);
                intent.putExtras(dateArg);
                startActivity(intent);
                break;
            case PublicConstant.PUBLISHED_GROUP_TYPE:
                Bundle groupArg = new Bundle();
                groupArg.putInt(DatabaseConstant.GroupInfo.NOTE_ID,table_id);

                intent = new Intent(mActivity, BilliardGroupDetailActivity.class);
                intent.putExtras(groupArg);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mSearchView =(SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //TODO:将搜索结果传到SearResultActivity，在SearchResultActivity中进行搜索
                if(Utils.networkAvaiable(mActivity)) {
                    Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                    Bundle args = new Bundle();
                    args.putInt(PublicConstant.SEARCH_TYPE, PublicConstant.SEARCH_PUBLISH);
                    args.putString(PublicConstant.SEARCH_KEYWORD, query);
                    args.putInt(PublicConstant.TYPE,mType);
                    intent.putExtras(args);
                    startActivity(intent);
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                            .toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }else{
                    Utils.showToast(mActivity,getString(R.string.network_not_available));
                }
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
}
