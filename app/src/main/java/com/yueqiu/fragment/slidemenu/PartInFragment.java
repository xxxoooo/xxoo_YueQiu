package com.yueqiu.fragment.slidemenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.yueqiu.activity.NearbyBilliardsDatingActivity;
import com.yueqiu.activity.PlayDetailActivity;
import com.yueqiu.activity.SearchResultActivity;
import com.yueqiu.adapter.PartInAdapter;
import com.yueqiu.bean.ISlideMenuBasic;
import com.yueqiu.bean.PartInInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wangyun on 15/2/7.
 */
public class PartInFragment extends SlideMenuBasicFragment implements AdapterView.OnItemClickListener{
    private static final String SAVE_PARTIN_KEY = "save_publish";
    private static final String SAVE_PARTIN_REFRESH = "save_refresh";
    private static final String SAVE_PARTIN_LOAD_MORE = "save_load_more";
    private static final String SAVE_PARTIN_INSTANCE = "saved_instance";
    private PartInAdapter mPartInAdapter;
    //跟数据库相关的list
    private ArrayList<PartInInfo> mCacheList;
    private SearchView mSearchView;

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
        setHasOptionsMenu(true);
        mPartInAdapter = new PartInAdapter(mActivity,mList);

        if(savedInstanceState != null){
            mRefresh = savedInstanceState.getBoolean(SAVE_PARTIN_REFRESH);
            mLoadMore = savedInstanceState.getBoolean(SAVE_PARTIN_LOAD_MORE);
            mIsSavedInstance = savedInstanceState.getBoolean(SAVE_PARTIN_INSTANCE);
            mCacheList = savedInstanceState.getParcelableArrayList(SAVE_PARTIN_KEY);
            mHandler.obtainMessage(PublicConstant.USE_CACHE,mCacheList).sendToTarget();
        }



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

    private BasicHandler mHandler = new BasicHandler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.USE_CACHE:
                    setEmptyViewGone();
                    List<PartInInfo> cacheList = (List<PartInInfo>) msg.obj;
                    mList.addAll(cacheList);
                    if(mList.isEmpty())
                        setEmptyViewVisible(mActivity.getString(R.string.your_published_info_is_empty,mEmptyTypeStr));
                    break;
                case PublicConstant.GET_SUCCESS:
                    setEmptyViewGone();
                    mBeforeCount = mList.size();
                    mIsListEmpty = mList.isEmpty();
                    List<PartInInfo> list = (List<PartInInfo>) msg.obj;
                    for(PartInInfo info : list){
//                        if(!mList.contains(info)){
//                            if(!mIsListEmpty && Integer.valueOf(((PartInInfo)mList.get(0)).getTable_id()) < Integer.valueOf(info.getTable_id())){
//                                mList.add(0,info);
//                            }else {
//                                mList.add(info);
//                            }
//                        }
                        if(mRefresh && !mIsListEmpty) {
                            mList.add(0,info);
                        }else{
                            if(mIsSavedInstance){
                                mList.add(0,info);
                            }else{
                                mList.add(info);
                            }
                        }
                    }
//                    Collections.sort(mList,new DescComparator());
                    mAfterCount = mList.size();
                    if(mList.isEmpty()){
                        setEmptyViewVisible(mActivity.getString(R.string.no_part_in_info,mEmptyTypeStr));
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
                mEmptyTypeStr = getString(R.string.nearby_billiard_dating_str);
                break;
            case PublicConstant.PART_IN_PLAY_TYPE:
                mEmptyTypeStr = getString(R.string.tab_title_activity);
                break;

        }
    }

    @Override
    protected void requestResult() {

        mPreProgress.setVisibility(View.VISIBLE);
        mPreText.setVisibility(View.VISIBLE);

        mParamsMap.put(DatabaseConstant.UserTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        mParamsMap.put(HttpConstants.Published.TYPE,mType);
        mParamsMap.put(HttpConstants.Published.START_NO,mStart_no);
        mParamsMap.put(HttpConstants.Published.END_NO, mEnd_no);

        HttpUtil.requestHttp(HttpConstants.PartIn.URL,mParamsMap,HttpConstants.RequestMethod.GET,new ResponseHandler<PartInInfo>());
    }

    @Override
    protected List<PartInInfo> setBeanByJSON(JSONObject jsonResult) {
        List<PartInInfo> list = new ArrayList<PartInInfo>();
        try {
            if(jsonResult.getJSONObject("result").get("list_data").equals("null")){
                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
            }else {
                JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");
                if (list_data.length() < 1) {
                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                } else {
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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent;
        PartInInfo info = (PartInInfo) mPartInAdapter.getItem(position - 1);

        int table_id = Integer.valueOf(info.getTable_id());
        String username = info.getUsername();
        String img_url = info.getImg_url();

        String create_time = info.getDateTime();

        switch (mType){
            case PublicConstant.PART_IN_DATE_TYPE:
                Bundle dateArg = new Bundle();
                dateArg.putInt(NearbyFragmentsCommonUtils.KEY_DATING_TABLE_ID,table_id);
                dateArg.putString(NearbyFragmentsCommonUtils.KEY_DATING_FRAGMENT_PHOTO,img_url);
                dateArg.putString(NearbyFragmentsCommonUtils.KEY_DATING_USER_NAME,username);

                intent = new Intent(mActivity, NearbyBilliardsDatingActivity.class);
                intent.putExtras(dateArg);
                startActivity(intent);
                break;
            case PublicConstant.PART_IN_PLAY_TYPE:
                Bundle playArg = new Bundle();
                playArg.putInt(DatabaseConstant.PlayTable.TABLE_ID,table_id);
                playArg.putString(DatabaseConstant.PlayTable.CREATE_TIME,create_time);

                intent = new Intent(mActivity, PlayDetailActivity.class);
                intent.putExtras(playArg);
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
                    args.putInt(PublicConstant.SEARCH_TYPE, PublicConstant.SEARCH_JOIN);
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

    /**
     * 由于服务器是按降序排序，但是从网络获取到的json却是升序，所以重新排序一下
     */
    private class DescComparator implements Comparator<ISlideMenuBasic> {

        @Override
        public int compare(ISlideMenuBasic lhs, ISlideMenuBasic rhs) {
            int lhsUserId = Integer.valueOf(((PartInInfo)lhs).getTable_id());
            int rhsUserId = Integer.valueOf(((PartInInfo)rhs).getTable_id());
            return lhsUserId > rhsUserId ? -1 : 1;
        }
    }
}
