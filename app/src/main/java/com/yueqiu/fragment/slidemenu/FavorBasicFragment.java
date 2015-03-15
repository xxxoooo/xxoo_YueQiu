package com.yueqiu.fragment.slidemenu;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
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

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.BilliardGroupDetailActivity;
import com.yueqiu.activity.NearbyBilliardsDatingActivity;
import com.yueqiu.activity.PlayDetailActivity;
import com.yueqiu.activity.SearchResultActivity;
import com.yueqiu.adapter.FavorBasicAdapter;
import com.yueqiu.bean.FavorInfo;
import com.yueqiu.bean.Identity;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.FavorDao;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wangyun on 15/1/16.
 */
public class FavorBasicFragment extends SlideMenuBasicFragment implements AdapterView.OnItemClickListener{
    private static final String SAVE_FAVOR_KEY = "save_favor";
    private static final String SAVE_FAVOR_REFRESH = "save_refresh";
    private static final String SAVE_FAVOR_LOAD_MORE = "save_load_more";
    private static final String SAVE_FAVOR_INSTANCE = "saved_instance";
    private FavorBasicAdapter mAdapter;
    private FavorDao mFavorDao;
    //跟数据库相关的list
    private List<FavorInfo> mCacheList;
    private SearchView mSearchView;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO:是否需要在这里保存数据？如果在滑动过程中断网了，那么在滑回来的时候
        //TODO:按照现在不用缓存的逻辑页面就是空的
        outState.putParcelableArrayList(SAVE_FAVOR_KEY, mList);
        outState.putBoolean(SAVE_FAVOR_REFRESH,mRefresh);
        outState.putBoolean(SAVE_FAVOR_LOAD_MORE,mLoadMore);
        outState.putBoolean(SAVE_FAVOR_INSTANCE,true);

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);
        setHasOptionsMenu(true);
        mFavorDao = DaoFactory.getFavor(mActivity);
        mAdapter = new FavorBasicAdapter(mActivity,mList);
        mListView.setOnItemClickListener(this);
        ////////////////////////////////////////////////////////////////////////////////////////////
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
//                mCacheList = mFavorDao.getFavorLimit(YueQiuApp.sUserInfo.getUser_id(),mType,mStart_no,10);
//                if(!mCacheList.isEmpty()){
//                    mHandler.obtainMessage(PublicConstant.USE_CACHE,mCacheList).sendToTarget();
//                }
//            }
//        }).start();
        ///////////////////////////////////////////////////////////////////////////////////////////

        if(savedInstanceState != null){
            mRefresh = savedInstanceState.getBoolean(SAVE_FAVOR_REFRESH);
            mLoadMore = savedInstanceState.getBoolean(SAVE_FAVOR_LOAD_MORE);
            mIsSavedInstance = savedInstanceState.getBoolean(SAVE_FAVOR_INSTANCE);
            mCacheList = savedInstanceState.getParcelableArrayList(SAVE_FAVOR_KEY);
            mHandler.obtainMessage(PublicConstant.USE_CACHE,mCacheList).sendToTarget();
        }
        //TODO:savedInstance为null证明该是第一次进入到viewpager,需要从数据库中读缓存
        //TODO:后期要做缓存的时候可以将上面读缓存的代码放到else里面
//        else{
//
//        }


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(mReceiver,filter);
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
                mEmptyTypeStr = mActivity.getString(R.string.nearby_billiard_dating_str);
                break;
            case PublicConstant.FAVPR_ROOM_TYPE:
                //TODO:由于球厅先不做，所以改变一下
                //mEmptyTypeStr = mActivity.getString(R.string.nearby_billiard_coauch_str);
                mEmptyTypeStr = mActivity.getString(R.string.tab_title_activity);
                break;
            case PublicConstant.FAVOR_PLAY_TYPE:
                //mEmptyTypeStr = mActivity.getString(R.string.tab_title_activity);
                mEmptyTypeStr = mActivity.getString(R.string.tab_title_billiards_circle);
                break;
//            case PublicConstant.FAVOR_GROUP_TYPR:
//                mEmptyTypeStr = mActivity.getString(R.string.tab_title_billiards_circle);
//                break;
        }
    }



    @Override
    protected void requestResult() {

        mPreProgress.setVisibility(View.VISIBLE);
        if(mList.isEmpty()) {
            mPreText.setVisibility(View.VISIBLE);
        }

        mParamsMap.put(DatabaseConstant.UserTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        mParamsMap.put(HttpConstants.Favor.TYPE,mType == 1 ? 1 : mType + 1);
        mParamsMap.put(HttpConstants.Favor.START_NO,mStart_no);
        mParamsMap.put(HttpConstants.Favor.END_NO, mEnd_no);
        Log.d("wy","favor params ->" + mParamsMap);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.Favor.URL,mParamsMap,HttpConstants.RequestMethod.GET,new ResponseHandler<FavorInfo>());

    }

    @Override
    protected List<FavorInfo> setBeanByJSON(JSONObject jsonResult) {
        List<FavorInfo> list = new ArrayList<FavorInfo>();
        try {
            if(jsonResult.getJSONObject("result").get("list_data").equals("null")){
                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
            }else {
                JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");
                if (list_data.length() < 1) {
                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                } else {
                    for (int i = 0; i < list_data.length(); i++) {
                        FavorInfo itemInfo = new FavorInfo();
                        //现数据表
                        itemInfo.setTable_id(list_data.getJSONObject(i).getString("id"));
                        //源数据表
                        itemInfo.setRid(list_data.getJSONObject(i).getInt("rid"));
                        itemInfo.setTitle(list_data.getJSONObject(i).getString("title"));
                        itemInfo.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                        itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                        itemInfo.setCreateTime(list_data.getJSONObject(i).getString("create_time"));
                        itemInfo.setUserName(list_data.getJSONObject(i).getString("username"));
                        itemInfo.setType(Integer.valueOf(list_data.getJSONObject(i).getString("type")));
                        //TODO:根据服务器确定的字段,如果不做缓存这个字段不需要，但是如果后期要加缓存，这个字段必须有
//                    itemInfo.setSubType(list_data.getJSONObject(i).getInt("subtype"));
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
    protected void onItemStateChanged(int position, boolean checked) {
        FavorInfo itemInfo = (FavorInfo) mListView.getItemAtPosition(position);
        itemInfo.setChecked(checked);
    }

    @Override
    protected void onPullUpWhenNetNotAvailable() {
        //TODO:由于不用缓存，所以直接toast无网络，如果后期需要缓存了，再换回这段代码
//        List<FavorInfo> list= mFavorDao.getFavorLimit(YueQiuApp.sUserInfo.getUser_id(), mType, mStart_no, 10);
//        if(!list.isEmpty()) {
//            mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
//        }else{
//            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
//        }

        mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
    }

    //TODO:目前不需要缓存，所以这个方法先不调用
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
                    setEmptyViewGone();
                    List<FavorInfo> cacheList = (List<FavorInfo>) msg.obj;
                    mList.addAll(cacheList);
                    if(mList.isEmpty())
                        setEmptyViewVisible(mActivity.getString(R.string.your_published_info_is_empty,mEmptyTypeStr));
                    break;
                case PublicConstant.GET_SUCCESS:
                    setEmptyViewGone();
                    mBeforeCount = mList.size();
                    mIsListEmpty = mList.isEmpty();
                    List<FavorInfo> list = (List<FavorInfo>) msg.obj;
                    for(FavorInfo info : list){
                        if (!mList.contains(info)) {

                            if(!mIsListEmpty && Integer.valueOf(((FavorInfo)mList.get(0)).getTable_id()) < Integer.valueOf(info.getTable_id())){
                                mList.add(0,info);
                            }else {
                                mList.add(info);
                            }
//                            if(mRefresh && !mIsListEmpty) {
//                                mList.add(0,info);
//                            }else{
//                                if(mIsSavedInstance){
//                                    mList.add(0,info);
//                                }else{
//                                    mList.add(info);
//                                }
//                            }
                        }
                        //////////////////////////////////////////////////////
                        //TODO:下面的逻辑是用来更新缓存的，不过目前先不需要缓存
                        //TODO:如果后期功能需要加缓存再加回来，目前的逻辑暂时没问题
                        /**
                         * 根据这条favorinfo的type和tableId,以及userId值生成唯一的key值
                         * 如果全局的favorMap里有这条数据，则将这条数据加入
                         * updateList，如果这条数据在favorMap里不存在将将这条数据加入insertList
                         */
//                        Identity identity = new Identity();
//                        identity.user_id = YueQiuApp.sUserInfo.getUser_id();
//                        identity.table_id = info.getTable_id();
//                        identity.type = info.getType();
//                        if(!YueQiuApp.sFavorMap.containsKey(identity)){
//                            mFavorInsertList.add(info);
//                        }else{
//                            mFavorUpdateList.add(info);
//                        }
                        /**
                         * Map在put值的时候，如果key值已经存在则会用新的value覆盖
                         * 原先的value，如果key值不存在则直接放入，所以这里favorInfo是
                         * 放入updateList还是insertList，这里都要执行以下put操作，用来更新
                         * 全局的map
                         */
//                        YueQiuApp.sFavorMap.put(identity,info);
                        //////////////////////////////////////////////////////
                    }
                    mAfterCount = mList.size();
                    if(mList.isEmpty()){
                        setEmptyViewVisible(mActivity.getString(R.string.no_favor_info,mEmptyTypeStr));
                    }else{
                        if(mRefresh){
                            if (mAfterCount == mBeforeCount) {
                                Utils.showToast(mActivity, mActivity.getString(R.string.no_newer_info));
                            } else {
                                Utils.showToast(mActivity, mActivity.getString(R.string.have_already_update_info, mAfterCount - mBeforeCount));
                            }
                        }
                    }

                    //TODO:由于目前不需要做缓存，所以先不操作数据库，后期需要缓存的时候再改回来
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            updateDB();
//                        }
//                    }).start();

                    break;
            }
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            if(mLoadMore && !mList.isEmpty()){
                mListView.setSelection(mCurrPosition - 1);
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent;
        FavorInfo info = (FavorInfo) mAdapter.getItem(position-1);
        //TODO:不做缓存，所以这个字段先不要
//        int subType = info.getSubType();
        int table_id = Integer.valueOf(info.getRid());
        String username = info.getUserName();
        String img_url = info.getImg_url();

        String create_time = info.getCreateTime();

        switch(mType){
            case PublicConstant.FAVOR_GROUP_TYPR:
                break;
            case PublicConstant.FAVPR_ROOM_TYPE:
                //TODO:由于先不做球厅，所以这里实际是PLAY
                Bundle playArg = new Bundle();
                playArg.putInt(DatabaseConstant.PlayTable.TABLE_ID,table_id);
                playArg.putString(DatabaseConstant.PlayTable.CREATE_TIME,create_time);

                intent = new Intent(mActivity, PlayDetailActivity.class);
                intent.putExtras(playArg);
                startActivity(intent);
                break;
            case PublicConstant.FAVOR_DATE_TYPE:
                Bundle dateArg = new Bundle();
                dateArg.putInt(NearbyFragmentsCommonUtils.KEY_DATING_TABLE_ID,table_id);
                dateArg.putString(NearbyFragmentsCommonUtils.KEY_DATING_FRAGMENT_PHOTO,img_url);
                dateArg.putString(NearbyFragmentsCommonUtils.KEY_DATING_USER_NAME,username);

                intent = new Intent(mActivity, NearbyBilliardsDatingActivity.class);
                intent.putExtras(dateArg);
                startActivity(intent);
                break;
            case PublicConstant.FAVOR_PLAY_TYPE:
                //TODO:由于先不做球厅，所以这里实际是GROUP
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
                    args.putInt(PublicConstant.SEARCH_TYPE, PublicConstant.SEARCH_FAVOR);
                    args.putString(PublicConstant.SEARCH_KEYWORD, query);
                    args.putInt(PublicConstant.TYPE, mType);
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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                if(Utils.networkAvaiable(getActivity())) {
                    if (mList.isEmpty()) {
                        mLoadMore = false;
                        mRefresh = false;
                        mParamsMap.put(HttpConstants.Published.START_NO,0);
                        mParamsMap.put(HttpConstants.Published.END_NO, 9);
                        requestResult();
                    }
                }
            }
        }
    };
}
