package com.yueqiu.fragment.group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.BilliardGroupDetailActivity;
import com.yueqiu.activity.PlayDetailActivity;
import com.yueqiu.activity.SearchResultActivity;
import com.yueqiu.adapter.GroupBasicAdapter;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.GroupInfoDao;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.acl.Group;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangyun on 14/12/17.
 * 台球圈基础的Fragment
 */
public class BilliardGroupBasicFragment extends Fragment implements AdapterView.OnItemClickListener{
    private static final String SAVE_GROUP_KEY = "save_group";
    private static final String SAVE_REFRESH_KEY = "save_refresh";
    private static final String SAVE_LOAD_MORE_KEY = "save_load_more";
    private static final String SAVED_INSTANCE = "saved_instance";
    private View mView;
    private Activity mActivity;
    private RadioGroup mGroup;
    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;
    private GroupBasicAdapter mAdapter;
    private ProgressBar mPreProgress;
    private TextView mPreText,mEmptyView;
    private Drawable mProgressDrawable;
    private String mEmptyTypeStr;
    private GroupInfoDao mGroupDao;
    private int mGroupType;
    private int mStart_no = 0,mEnd_no = 9,mTimeStart = 0,mTimeEnd = 9,mPopStart = 0,mPopEnd = 9;
    private int mBeforeCount,mAfterCount;
    private int mCurrPosition;
    private boolean mRefresh,mLoadMore,mIsSavedInstance,mIsListEmpty;
    private boolean mTimeDesc,mPopularityDesc,mNormal;

    private Map<String,Integer> mParamMap = new HashMap<String, Integer>();
    private Map<String,String> mUrlAndMethodMap = new HashMap<String, String>();
    private ArrayList<GroupNoteInfo> mList = new ArrayList<GroupNoteInfo>();
    private List<GroupNoteInfo> mCacheList;
    private List<GroupNoteInfo> mInsertList = new ArrayList<GroupNoteInfo>();
    private List<GroupNoteInfo> mUpdateList = new ArrayList<GroupNoteInfo>();
    private Map<String,String> mTimeParam = new HashMap<String, String>();
    private Map<String,String> mPopParam = new HashMap<String, String>();
    private SearchView mSearchView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = getActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_GROUP_KEY,mList);
        outState.putBoolean(SAVE_REFRESH_KEY,mRefresh);
        outState.putBoolean(SAVE_LOAD_MORE_KEY,mLoadMore);
        outState.putBoolean(SAVED_INSTANCE,true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_billiard_group_basic,null);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        mGroupType = args.getInt("type");
        mGroupDao = DaoFactory.getGroupDao(mActivity);
        mAdapter = new GroupBasicAdapter(mActivity,mList);
        mNormal = true;
        mPopularityDesc = false;
        mTimeDesc = false;
        initView();
        setEmptyTypeStr();

        /////////////////////////////////////////////////////////////////////////////////////
        //TODO:目前不需要缓存，后期需要缓存的时候再把这段代码加上
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                //TODO:缓存的list，
//                if(mGroupType == PublicConstant.GROUP_ALL) {
//                    mCacheList = mGroupDao.getAllGroupInfoLimit(mStart_no, 10);
//                }else{
//                    mCacheList = mGroupDao.getGroupInfoByType(mGroupType,mStart_no,10);
//                }
//                if(!mCacheList.isEmpty()){
//                    mHandler.obtainMessage(PublicConstant.USE_CACHE,mCacheList).sendToTarget();
//                }
//            }
//        }).start();
        /////////////////////////////////////////////////////////////////////////////////////

        if(savedInstanceState != null){
            mRefresh = savedInstanceState.getBoolean(SAVE_REFRESH_KEY);
            mLoadMore = savedInstanceState.getBoolean(SAVE_LOAD_MORE_KEY);
            mIsSavedInstance = savedInstanceState.getBoolean(SAVED_INSTANCE);

            mCacheList = savedInstanceState.getParcelableArrayList(SAVE_GROUP_KEY);
            mHandler.obtainMessage(PublicConstant.USE_CACHE,mCacheList).sendToTarget();
        }
        //TODO:savedInstance为null证明该是第一次进入到viewpager,需要从数据库中读缓存
        //TODO:后期要做缓存的时候可以将上面读缓存的代码放到else里面
//        else{
//
//        }


        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Utils.networkAvaiable(mActivity)){
            mLoadMore = false;
            mRefresh = false;
            if(mNormal) {
                requestGroup();
            }
            if(mTimeDesc){
                getGroupByTime();
            }
            if(mPopularityDesc){
                getGroupByPop();
            }
        }else{
            mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
        }
        if(mSearchView != null) {
            mSearchView.clearFocus();
        }

    }

    private void initView(){

        mGroup = (RadioGroup) mView.findViewById(R.id.billiard_radio_group);
//        ((RadioButton)mGroup.findViewById(R.id.billiard_time_sort)).setChecked(true);
        mPreText = (TextView) mView.findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        mPullToRefreshListView = (PullToRefreshListView) mView.findViewById(R.id.billiard_group_listview);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView = mPullToRefreshListView.getRefreshableView();
        mPullToRefreshListView.setOnRefreshListener(mOnRefreshListener);
        mEmptyView = new TextView(mActivity);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(mActivity).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);

        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.billiard_time_sort:
//                        Collections.sort(mList,new TimeComparator());
                        //TODO:更新adapter
                        mList.clear();
                        mAdapter.notifyDataSetChanged();
                        getGroupByTime();
                        break;
                    case R.id.billiard_popularity_sort:
//                        Collections.sort(mList,new PopularityComparator());
                        //TODO:更新adapter
                        mList.clear();
                        mAdapter.notifyDataSetChanged();
                        getGroupByPop();
                        break;
                }
            }
        });

        mListView.setOnItemClickListener(this);
    }

    private void getGroupByTime(){

        mTimeDesc = true;
        mPopularityDesc = false;
        mNormal = false;

        if(mGroupType != PublicConstant.GROUP_ALL) {
            mTimeParam.put(HttpConstants.GroupList.TYPE, String.valueOf(mGroupType));
        }

        mTimeParam.put(HttpConstants.GroupList.STAR_NO,String.valueOf(mTimeStart));
        mTimeParam.put(HttpConstants.GroupList.END_NO,String.valueOf(mTimeEnd));
        mTimeParam.put(HttpConstants.GroupList.TIME,"desc");

        mPreProgress.setVisibility(View.VISIBLE);
        mPreText.setVisibility(View.VISIBLE);

        Log.d("group","time desc param ->" + mTimeParam);

        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.GroupList.URL,mTimeParam,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("group","time desc group response ->" + response);
                try {
                    if (!response.isNull("code")) {
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            if(response.getJSONObject("result") != null){
                                List<GroupNoteInfo> list = setGroupInfoByJSON(response);
                                if(list.isEmpty()){
                                    mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                                }else {
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                                }
                            }else{
                                mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                            }
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    } else {
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
            }
        });
    }

    private void getGroupByPop(){

        mPopularityDesc = true;
        mTimeDesc = false;
        mNormal = false;

        if (mGroupType != PublicConstant.GROUP_ALL){
            mPopParam.put(HttpConstants.GroupList.TYPE, String.valueOf(mGroupType));
        }
        mPopParam.put(HttpConstants.GroupList.STAR_NO,String.valueOf(mPopStart));
        mPopParam.put(HttpConstants.GroupList.END_NO,String.valueOf(mPopEnd));
        mPopParam.put(HttpConstants.GroupList.WEIGHT,"desc");

        mPreProgress.setVisibility(View.VISIBLE);
        mPreText.setVisibility(View.VISIBLE);

        Log.d("group","pop desc param ->" + mPopParam);

        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.GroupList.URL,mPopParam,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("group","popularity desc group response ->" + response);
                try {
                    if (!response.isNull("code")) {
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            if(response.getJSONObject("result") != null){
                                List<GroupNoteInfo> list = setGroupInfoByJSON(response);
                                if(list.isEmpty()){
                                    mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                                }else {
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                                }
                            }else{
                                mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                            }
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    } else {
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
            }
        });

    }

    private void setEmptyTypeStr(){
        switch(mGroupType){
            case PublicConstant.GROUP_GET_MASTER:
                mEmptyTypeStr = mActivity.getString(R.string.billiard_get_master);
                break;
            case PublicConstant.GROUP_BE_MASTER:
                mEmptyTypeStr = mActivity.getString(R.string.billiard_be_master);
                break;
            case PublicConstant.GROUP_GET_FRIEND:
                mEmptyTypeStr = mActivity.getString(R.string.billiard_find_friend);
                break;
            case PublicConstant.GROUP_EQUIP:
                mEmptyTypeStr = mActivity.getString(R.string.billiard_equipment);
                break;
            case PublicConstant.GROUP_OTHER:
                mEmptyTypeStr = mActivity.getString(R.string.billiard_other);
                break;

        }
    }
    private void setEmptyViewVisible(){

        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        mEmptyView.setTextColor(mActivity.getResources().getColor(R.color.md__defaultBackground));
        if(mGroupType == PublicConstant.GROUP_ALL) {
            mEmptyView.setText(mActivity.getString(R.string.no_group_info));
        }else{
            mEmptyView.setText(mActivity.getString(R.string.no_group_type_info,mEmptyTypeStr));
        }
        mPullToRefreshListView.setEmptyView(mEmptyView);
    }

    private void setmEmptyViewGone(){
        if(null != mEmptyView){
            mEmptyView.setVisibility(View.GONE);
        }
    }

    private void requestGroup(){


        mNormal = true;
        mTimeDesc = false;
        mPopularityDesc = false;

        if(mGroupType != PublicConstant.GROUP_ALL) {
            mParamMap.put(HttpConstants.GroupList.TYPE, mGroupType);
        }
        mParamMap.put(HttpConstants.GroupList.STAR_NO,mStart_no);
        mParamMap.put(HttpConstants.GroupList.END_NO,mEnd_no);

        mPreProgress.setVisibility(View.VISIBLE);
        if(mList.isEmpty()) {
            mPreText.setVisibility(View.VISIBLE);
        }

        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.GroupList.URL,mParamMap,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","group response ->" + response);
                try {
                    if (!response.isNull("code")) {
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            if(response.getJSONObject("result") != null){
                                List<GroupNoteInfo> list = setGroupInfoByJSON(response);
                                if(list.isEmpty()){
                                    mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                                }else {
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                                }
                            }else{
                                mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                            }
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    } else {
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
            }
        });
    }

    private List<GroupNoteInfo> setGroupInfoByJSON(JSONObject object){
        List<GroupNoteInfo> infos = new ArrayList<GroupNoteInfo>();
        try {
            if(object.getJSONObject("result").get("list_data").equals("null")){
                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
            }
            else {
                JSONArray list_data = object.getJSONObject("result").getJSONArray("list_data");
                if(list_data.length() < 1){
                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                }else {
                    for (int i = 0; i < list_data.length(); i++) {
                        GroupNoteInfo info = new GroupNoteInfo();
                        info.setNoteId(Integer.parseInt(list_data.getJSONObject(i).getString("id")));
                        info.setUserName(list_data.getJSONObject(i).getString("username"));
                        info.setImg_url(list_data.getJSONObject(i).getString("u_img_url"));
                        info.setTitle(list_data.getJSONObject(i).getString("title"));
                        info.setContent(list_data.getJSONObject(i).getString("content"));
                        info.setIssueTime(list_data.getJSONObject(i).getString("create_time"));
                        info.setCommentCount(list_data.getJSONObject(i).getInt("comment_num"));
                        info.setBrowseCount(list_data.getJSONObject(i).getInt("look_number"));
                        info.setExtra_img_url(list_data.getJSONObject(i).getString("img_url"));
                        if (mGroupType != PublicConstant.GROUP_ALL) {
                            info.setType(mGroupType);
                        }
                        infos.add(info);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return infos;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GroupNoteInfo info = (GroupNoteInfo) mAdapter.getItem(position-1);
        Intent intent = new Intent(mActivity, BilliardGroupDetailActivity.class);
        Bundle args = new Bundle();
        args.putInt(DatabaseConstant.GroupInfo.NOTE_ID,info.getNoteId());
        args.putInt(DatabaseConstant.GroupInfo.COMMENT_COUNT,info.getCommentCount());
        intent.putExtras(args);
        startActivity(intent);
        mActivity.overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPreProgress.setVisibility(View.GONE);
            mPreText.setVisibility(View.GONE);
            if(mPullToRefreshListView.getMode() == PullToRefreshBase.Mode.DISABLED){
                mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
            }
            /**
             * 统一停止PullToRefreshView的刷新动作
             */
            if(mPullToRefreshListView.isRefreshing())
                mPullToRefreshListView.onRefreshComplete();
            switch(msg.what){
                case PublicConstant.USE_CACHE:
                    setmEmptyViewGone();
                    List<GroupNoteInfo> cacheList = (List<GroupNoteInfo>) msg.obj;
                    mList.addAll(cacheList);
                    if(mList.isEmpty()){
                        setEmptyViewVisible();
                    }
                    break;
                case PublicConstant.GET_SUCCESS:
                    setmEmptyViewGone();
                    /**
                     * 保存还未更新的list的size
                     */
                    mBeforeCount = mList.size();
                    mIsListEmpty = mList.isEmpty();
                    List<GroupNoteInfo> list = (List<GroupNoteInfo>) msg.obj;
                    for(GroupNoteInfo info : list){
                        /**
                         * UI list与数据库无关
                         */
                        //TODO:有可能存在服务器那边删了数据，但数据库中还会存在这条数据
                        //TODO:需要再加一定的逻辑判断，如果发生这样的情况，该如何处理
                         if (!mList.contains(info)) {

                             if(!mIsListEmpty && mList.get(0).getNoteId() < info.getNoteId()){
                                 mList.add(0,info);
                             }else {
                                 mList.add(info);
                             }
                         }
                        //TODO:目前不需要缓存，所以这块先不需要操作数据库
                        /**
                         * 数据库里的所有数据都不包含这条数据才插入数据,否则应该更新数据库
                         * 这里用全局map来保存数据库内容，避免每次创建都去读取数据库
                         */
//                        if(!YueQiuApp.sGroupDbMap.containsKey(info.getNoteId())){
//                            mInsertList.add(info);
//                        }else{
//                            mUpdateList.add(info);
//                        }
//                        YueQiuApp.sGroupDbMap.put(info.getNoteId(),info);
                    }
                    /**
                     * 保存更新完list以后的size,如果更新完以后的list的size没有变化
                     * 则意味着list没有改变，在下拉刷新时就意味着没有更新的数据
                     */
                    mAfterCount = mList.size();
                    /**
                     * 如果list为空，设置emptyView
                     */
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }else {
                        if (mRefresh) {
                            if (mAfterCount == mBeforeCount) {
                                Utils.showToast(mActivity, mActivity.getString(R.string.no_newer_info));
                            } else {
                                Utils.showToast(mActivity, mActivity.getString(R.string.have_already_update_info, mAfterCount - mBeforeCount));
                            }
                        }
                    }
                    //TODO:目前不需要缓存，所以这块先不需要操作数据库
                    /**
                     * 另起线程更新数据库
                     */
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            updateGroupInfoDB();
//                        }
//                    }).start();

                    break;
                case PublicConstant.REQUEST_ERROR:

                    if(mList.isEmpty()) {

                        setEmptyViewVisible();
                        if(null == msg.obj){
                            mEmptyView.setText(mActivity.getString(R.string.http_request_error));
                        }else{
                            mEmptyView.setText((String) msg.obj);
                        }
                   }else{
                        setmEmptyViewGone();
                        if(null == msg.obj){
                            Utils.showToast(mActivity,mActivity.getString(R.string.http_request_error));
                        }else{
                            Utils.showToast(mActivity, (String) msg.obj);
                        }
                    }

                    break;
                case PublicConstant.NO_RESULT:
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }else{
                        /**
                         * 上拉加载时，没有更多数据
                         */
                        if(mLoadMore) {
                            if(mGroupType == PublicConstant.GROUP_ALL) {
                                Utils.showToast(mActivity, mActivity.getString(R.string.no_more_group_info));
                            }else{
                                Utils.showToast(mActivity,mActivity.getString(R.string.no_more_info,mEmptyTypeStr));
                            }
                        }
                    }
                    break;
                case PublicConstant.NO_NETWORK:
                    Utils.showToast(mActivity,mActivity.getString(R.string.network_not_available));
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }
                    break;
            }
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            if(mLoadMore && !mList.isEmpty()){
                mListView.setSelection(mCurrPosition - 1);
            }
        }
    };

    //TODO:由于目前不需要缓存，所以暂时先不调用该方法
    private void updateGroupInfoDB(){
        if(!mUpdateList.isEmpty()){
            mGroupDao.updateGroupInfo(mUpdateList);
        }
        if(!mInsertList.isEmpty()){
            long result = mGroupDao.insertGroupInfo(mInsertList);
            //TODO:目前noteId没有unique，可能还会存在重复插入的问题，还需要再验证
            /**
             * 插入失败，则更新数据库
             */
            if(result == -1){
                mGroupDao.updateGroupInfo(mInsertList);
            }
        }
    }

    private PullToRefreshBase.OnRefreshListener2 mOnRefreshListener = new PullToRefreshBase.OnRefreshListener2() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView) {
            //TODO:需要把刷新时间保存起来
            String label = DateUtils.formatDateTime(mActivity, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            mRefresh = true;
            mLoadMore = false;
            mInsertList.clear();
            mUpdateList.clear();
            if(mEmptyView.getVisibility() == View.VISIBLE){
                mEmptyView.setVisibility(View.GONE);
            }
            if(Utils.networkAvaiable(mActivity)){
                if(mNormal) {
                    mParamMap.put(HttpConstants.GroupList.STAR_NO, 0);
                    mParamMap.put(HttpConstants.GroupList.END_NO, 9);
                    requestGroup();
                }
                if(mTimeDesc){
                    mTimeParam.put(HttpConstants.GroupList.STAR_NO, String.valueOf(0));
                    mTimeParam.put(HttpConstants.GroupList.END_NO,String.valueOf(9));
                    getGroupByTime();
                }

                if(mPopularityDesc){
                    mPopParam.put(HttpConstants.GroupList.STAR_NO,String.valueOf(0));
                    mPopParam.put(HttpConstants.GroupList.END_NO,String.valueOf(9));
                    getGroupByPop();
                }
            }else{
                mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            String label = DateUtils.formatDateTime(mActivity, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            mLoadMore = true;
            mCurrPosition = mList.size() ;
            mInsertList.clear();
            mUpdateList.clear();
            if(mEmptyView.getVisibility() == View.VISIBLE){
                mEmptyView.setVisibility(View.GONE);
            }
            /**
             * 如果要加载前先进行过下拉刷新，同时数据有更新，则此时再加载时分页的
             * start，end应该相应的增加
             */
            if(mBeforeCount != mAfterCount && mRefresh){
                if(mNormal) {
                    mStart_no = mEnd_no + (mAfterCount - mBeforeCount);
                    mEnd_no += 10 + (mAfterCount - mBeforeCount);
                }
                if(mTimeDesc){
                    mTimeStart = mTimeEnd + (mAfterCount - mBeforeCount);
                    mTimeEnd += 10 + (mAfterCount - mBeforeCount);
                }
                if(mPopularityDesc){
                    mPopStart = mPopEnd + (mAfterCount - mBeforeCount);
                    mPopEnd += 10 + (mAfterCount - mBeforeCount);
                }
            }else{
                if(mNormal) {
                    mStart_no = mEnd_no + 1;
                    mEnd_no += 10;
                }
                if(mTimeDesc){
                    mTimeStart = mTimeEnd + 1;
                    mTimeEnd += 10;
                }
                if(mPopularityDesc){
                    mPopStart = mPopEnd + 1;
                    mPopEnd += 10;
                }
            }
            mRefresh = false;
            if(Utils.networkAvaiable(mActivity)){
                if(mNormal) {
                    mParamMap.put(HttpConstants.GroupList.STAR_NO, mStart_no);
                    mParamMap.put(HttpConstants.GroupList.END_NO, mEnd_no);
                    requestGroup();
                }
                if(mTimeDesc){
                    mTimeParam.put(HttpConstants.GroupList.STAR_NO,String.valueOf(mTimeStart));
                    mTimeParam.put(HttpConstants.GroupList.END_NO,String.valueOf(mTimeEnd));
                    getGroupByTime();
                }
                if(mPopularityDesc){
                    mPopParam.put(HttpConstants.GroupList.STAR_NO,String.valueOf(mPopStart));
                    mPopParam.put(HttpConstants.GroupList.END_NO,String.valueOf(mPopEnd));
                    getGroupByPop();
                }
            }
            //TODO:由于目前不需要缓存，所以当没有网络时，就直接toast无网络
            //TODO:如果后面需要缓存，再替换成注释掉的代码
            else{
                mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
            }
//            /**
//             * 没有网络的时候，应该从数据库中加载
//             */
//            else{
//                List<GroupNoteInfo> list;
//                /**
//                * 如果type为0，则查全部，否则查相应的类型
//                */
//                if(mGroupType == PublicConstant.GROUP_ALL){
//                    list = mGroupDao.getAllGroupInfoLimit(mStart_no,10);
//                }else{
//                    list = mGroupDao.getGroupInfoByType(mGroupType,mStart_no,10);
//                }
//                if(list.isEmpty()){
//                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
//                }else{
//                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS,list);
//                }
//             }
        }
    };

    private class TimeComparator implements Comparator<GroupNoteInfo>{

        @Override
        public int compare(GroupNoteInfo lhs, GroupNoteInfo rhs) {
            long lhsTime = 0,rhsTime = 0;
            try {
                lhsTime = Utils.stringToLong(lhs.getIssueTime(),"yyyy-MM-dd HH:mm:ss");
                rhsTime = Utils.stringToLong(rhs.getIssueTime(),"yyyy-MM-dd HH:mm:ss");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(lhsTime == rhsTime){
                return lhs.getNoteId() > rhs.getNoteId() ? -1 : 1;
            }
            return lhsTime > rhsTime ? -1 : 1;
        }
    }
    private class PopularityComparator implements Comparator<GroupNoteInfo>{
        @Override
        public int compare(GroupNoteInfo lhs, GroupNoteInfo rhs) {
            int lhsLoveNums = lhs.getLoveNums();
            int rhsLoveNums = rhs.getLoveNums();
            if(lhsLoveNums == rhsLoveNums){
                return lhs.getNoteId() > rhs.getNoteId() ? 1 : -1;
            }
            return lhsLoveNums > rhsLoveNums ? 1 : -1;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mSearchView =(SearchView) menu.findItem(R.id.group_nemu_search).getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //TODO:将搜索结果传到SearResultActivity，在SearchResultActivity中进行搜索
                if(Utils.networkAvaiable(mActivity)) {
                    Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                    Bundle args = new Bundle();
                    args.putInt(PublicConstant.SEARCH_TYPE, PublicConstant.SEARCH_GROUP);
                    args.putString(PublicConstant.SEARCH_KEYWORD, query);
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
