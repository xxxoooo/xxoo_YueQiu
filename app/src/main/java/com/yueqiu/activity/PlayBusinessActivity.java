package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.adapter.PlayListViewAdapter;
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.PlayDao;
import com.yueqiu.dao.DaoFactory;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayBusinessActivity extends Activity implements AdapterView.OnItemClickListener{

    private ActionBar mActionBar;
    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;
    private PlayListViewAdapter mAdapter;
    private PlayDao mPlayDao;
    private ProgressBar mPreProgressBar;
    private TextView mEmptyView,mPreTextView;
    private boolean mRefresh,mLoadMore;
    private Drawable mProgressDrawable;
    private int mStart = 0,mEnd = 9;
    private int mBeforeCount,mAfterCount;
    private int mCurrPosition;
    private Map<String,Integer> mParamMap = new HashMap<String, Integer>();
    private Map<String,String> mUrlAndMethodMap = new HashMap<String, String>();
    private List<PlayInfo> mList = new ArrayList<PlayInfo>();
    private List<PlayInfo> mInsertList = new ArrayList<PlayInfo>();
    private List<PlayInfo> mUpdateList = new ArrayList<PlayInfo>();
    private List<PlayInfo> mCacheList = new ArrayList<PlayInfo>();
    private SearchView mSearchView;
    private boolean mIsListEmpty;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_bussiness);
        initActionBar();
        initView();
        mPlayDao = DaoFactory.getPlay(this);
        mAdapter = new PlayListViewAdapter(this,mList);

        /////////////////////////////////////////////////////////////////////////////////
        //TODO:先去掉缓存功能，后期再根据需求加回来，目前逻辑没问题
        //TODO:同样是可以考虑用更有效率的loader或其他异步方法，而不是
        //TODO:简单地用线程，线程的生命周期不可控
        /**
         * mCacheList是缓存list，从数据库中根据当前type值获取到前十条
         * 如果mCacheList不为空就先使用该list填充listview
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCacheList = mPlayDao.getPlayInfoLimit(PublicConstant.PLAY_BUSSINESS,mStart,10);
                if(!mCacheList.isEmpty()){
                    mHandler.obtainMessage(PublicConstant.USE_CACHE,mCacheList).sendToTarget();
                }
            }
        }).start();
        ///////////////////////////////////////////////////////////////////////////////////


        if(Utils.networkAvaiable(this)){
            mLoadMore = false;
            mRefresh = false;
            requestPlay();
        }else{
            mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
        if(mSearchView != null){
            mSearchView.clearFocus();
        }
    }

    private void initView(){
        mPullToRefreshListView = (PullToRefreshListView)findViewById(R.id.activity_activities_lv);
        mListView = mPullToRefreshListView.getRefreshableView();
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshListView.setOnRefreshListener(mRefreshListener);
        mPreProgressBar = (ProgressBar)findViewById(R.id.pre_progress);
        mPreTextView = (TextView)findViewById(R.id.pre_text);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgressBar.getIndeterminateDrawable().getBounds();
        mPreProgressBar.setIndeterminateDrawable(mProgressDrawable);
        mPreProgressBar.getIndeterminateDrawable().setBounds(bounds);

        mListView.setOnItemClickListener(this);

    }

    private void setEmptyViewVisible(){
        mEmptyView = new TextView(this);
        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        mEmptyView.setTextColor(getResources().getColor(R.color.md__defaultBackground));
        mEmptyView.setText(getString(R.string.no_business_activity));
        mPullToRefreshListView.setEmptyView(mEmptyView);
    }

    private void initActionBar(){
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(getString(R.string.business_play));
    }
    private void requestPlay(){
//        mParamMap.put(HttpConstants.Play.TYPE, PublicConstant.PLAY_BUSSINESS);
        mParamMap.put(HttpConstants.Play.START_NO,mStart);
        mParamMap.put(HttpConstants.Play.END_NO,mEnd);

        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.Play.BUSINESS,mParamMap,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","bussiness response ->" + response);
                try{
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            if(response.getString("result") != null){
                                List<PlayInfo> list = setPlayByJSON(response);
                                if(list.isEmpty()){
                                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                }else{
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS,list).sendToTarget();
                                }
                            }else{
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                            mHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
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

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PlayInfo info = (PlayInfo) mAdapter.getItem(position-1);
        Intent intent = new Intent(this, PlayDetailActivity.class);
        Bundle args = new Bundle();
        args.putInt(PublicConstant.PLAY_TYPE,PublicConstant.PLAY_BUSSINESS);
        args.putInt(DatabaseConstant.PlayTable.TABLE_ID,Integer.parseInt(info.getTable_id()));
        args.putString(DatabaseConstant.PlayTable.CREATE_TIME,info.getCreate_time());
        args.putString(DatabaseConstant.PlayTable.TYPE,info.getType());
        intent.putExtras(args);
        startActivity(intent);
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
                info.setType(String.valueOf(PublicConstant.PLAY_BUSSINESS));
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
            mPreProgressBar.setVisibility(View.GONE);
            mPreTextView.setVisibility(View.GONE);
            if(mPullToRefreshListView.getMode() == PullToRefreshBase.Mode.DISABLED){
                mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
            }
            if(mPullToRefreshListView.isRefreshing())
                mPullToRefreshListView.onRefreshComplete();
            switch (msg.what){
                case PublicConstant.USE_CACHE:
                    List<PlayInfo> cacheList = (List<PlayInfo>) msg.obj;
                    mList.addAll(cacheList);
                    break;
                case PublicConstant.GET_SUCCESS:
                    /**
                     * mBeforeCount是从网络成功获取数据前的list的size
                     * mAfterCount是成功获取数据后的list的size
                     * 这两个值是用来判断更新了多少条
                     */
                    mBeforeCount = mList.size();
                    mIsListEmpty = mList.isEmpty();
                    List<PlayInfo> list = (List<PlayInfo>) msg.obj;
                    for(PlayInfo info : list){
                        if (!mList.contains(info)) {

                            if(!mIsListEmpty && Integer.valueOf(mList.get(0).getTable_id()) < Integer.valueOf(info.getTable_id())){
                                mList.add(0,info);
                            }else {
                                mList.add(info);
                            }
                        }
                        //TODO:下面的逻辑是用来更新缓存的，不过目前先不需要缓存
                        //TODO:如果后期功能需要加缓存再加回来，目前的逻辑暂时没问题
                        /**
                         * 根据这条playinfo的type和tableId值生成唯一的key值
                         * 如果全局的playMap里有这条数据，则将这条数据加入
                         * updateList，如果这条数据在playMap里不存在将将这条数据加入insertList
                         */
//                        PlayIdentity identity = new PlayIdentity();
//                        identity.type = info.getType();
//                        identity.table_id = info.getTable_id();
//                        if(!YueQiuApp.sPlayMap.containsKey(identity)){
//                            mInsertList.add(info);
//                        }else{
//                            mUpdateList.add(info);
//                        }
                        /**
                         * Map在put值的时候，如果key值已经存在则会用新的value覆盖
                         * 原先的value，如果key值不存在则直接放入，所以这里playInfo是
                         * 放入updateList还是insertList，这里都要执行以下put操作，用来更新
                         * 全局的map
                         */
//                        YueQiuApp.sPlayMap.put(identity,info);
                    }
                    mAfterCount = mList.size();
                    if(mList.isEmpty()){
                        setEmptyViewVisible();
                    }else{
                        if(mRefresh){
                            if (mAfterCount == mBeforeCount) {
                                Utils.showToast(PlayBusinessActivity.this, getString(R.string.no_newer_info));
                            } else {
                                Utils.showToast(PlayBusinessActivity.this, getString(R.string.have_already_update_info, mAfterCount - mBeforeCount));
                            }
                        }
                    }
                    //TODO:是否有更有效率的异步方法？
                    //TODO:而不是简单地用线程，不过插入和更新操作都是使用事务，效率已经很高了
                    /**
                     * 异步更新数据库
                     */
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            updatePlayInfoDB();
//                        }
//                    }).start();
                    break;
                case PublicConstant.NO_RESULT:
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }else{
                        if(mLoadMore) {
                            Utils.showToast(PlayBusinessActivity.this,getString(R.string.no_more_business_activity));
                        }
                    }
                    break;
//                case PublicConstant.TIME_OUT:
//                    Utils.showToast(PlayBusinessActivity.this, getString(R.string.http_request_time_out));
//                    if(mList.isEmpty()) {
//                        setEmptyViewVisible();
//                    }
//                    break;
                case PublicConstant.REQUEST_ERROR:

                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                        if(null == msg.obj){
                            mEmptyView.setText(getString(R.string.http_request_error));
                        }else{
                            mEmptyView.setText((String) msg.obj);
                        }
                    }else{
                        if(null == msg.obj){
                            Utils.showToast(PlayBusinessActivity.this,getString(R.string.http_request_error));
                        }else{
                            Utils.showToast(PlayBusinessActivity.this, (String) msg.obj);
                        }
                    }
                    break;
                case PublicConstant.NO_NETWORK:

                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                        mEmptyView.setText(getString(R.string.network_not_available));
                    }else{
                        Utils.showToast(PlayBusinessActivity.this,getString(R.string.network_not_available));
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

    /**
     * 用来更新数据库的函数
     */
    //TODO：由于暂时不需要缓存功能，所以该方法暂时不执行
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.billiard_search, menu);

        SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView =(SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) mSearchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.LTGRAY);

        // 用于改变SearchView当中的icon
        mSearchView.setIconifiedByDefault(false);
        try {
            Field searchField = SearchView.class.getDeclaredField("mSearchHintIcon");
            searchField.setAccessible(true);
            ImageView searchHintIcon = (ImageView) searchField.get(mSearchView);
            searchHintIcon.setImageResource(R.drawable.search);
        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //TODO:将搜索结果传到SearResultActivity，在SearchResultActivity中进行搜索
                if(Utils.networkAvaiable(PlayBusinessActivity.this)) {
                    Intent intent = new Intent(PlayBusinessActivity.this, SearchResultActivity.class);
                    Bundle args = new Bundle();
                    args.putInt(PublicConstant.SEARCH_TYPE, PublicConstant.SEARCH_BUSINESS_PLAY);
                    args.putString(PublicConstant.SEARCH_KEYWORD, query);
                    intent.putExtras(args);
                    startActivity(intent);
                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                            .toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }else{
                    Utils.showToast(PlayBusinessActivity.this,getString(R.string.network_not_available));
                }
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }


    private PullToRefreshBase.OnRefreshListener2<ListView> mRefreshListener = new PullToRefreshBase.OnRefreshListener2<ListView>() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
            String label = DateUtils.formatDateTime(PlayBusinessActivity.this, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            mRefresh = true;
            mLoadMore = false;
            mInsertList.clear();
            mUpdateList.clear();
            /**
             * 下拉刷新始终都是请求最新的数据,无网络时无法使用缓存
             */
            if(Utils.networkAvaiable(PlayBusinessActivity.this)){
                mParamMap.put(HttpConstants.GroupList.STAR_NO,0);
                mParamMap.put(HttpConstants.GroupList.END_NO,9);
                requestPlay();
            }else{
                mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            String label = DateUtils.formatDateTime(PlayBusinessActivity.this, System.currentTimeMillis(),
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
            if(Utils.networkAvaiable(PlayBusinessActivity.this)){
                mParamMap.put(HttpConstants.GroupList.STAR_NO,mStart);
                mParamMap.put(HttpConstants.GroupList.END_NO,mEnd);
                requestPlay();
            }
            else{
                mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
            }
            //TODO:由于目前不需要缓存功能，所以当没有网络时就直接toast无网络
            //TODO:如果后期需要缓存功能再重新加回来
            /**
             * 无网络时从当前最后一条数据之后再查十条数据
             */
//            else{
//                List<PlayInfo> list = mPlayDao.getPlayInfoLimit(PublicConstant.PLAY_BUSSINESS,mStart,10);
//                if(list.isEmpty()){
//                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
//                }else{
//                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS,list);
//                }
//            }

        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                if(Utils.networkAvaiable(PlayBusinessActivity.this)) {
                    if (mList.isEmpty()) {
                        mLoadMore = false;
                        mRefresh = false;
                        mParamMap.put(HttpConstants.GroupList.STAR_NO,0);
                        mParamMap.put(HttpConstants.GroupList.END_NO,9);
                        requestPlay();
                    }
                }
            }
        }
    };

}
