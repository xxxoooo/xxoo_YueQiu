package com.yueqiu.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.FavorBasicAdapter;
import com.yueqiu.adapter.GroupBasicAdapter;
import com.yueqiu.adapter.PartInAdapter;
import com.yueqiu.adapter.PlayListViewAdapter;
import com.yueqiu.adapter.PublishedBasicAdapter;
import com.yueqiu.bean.FavorInfo;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.bean.ISlideListItem;
import com.yueqiu.bean.ISlideMenuBasic;
import com.yueqiu.bean.PartInInfo;
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.fragment.slidemenu.SlideMenuBasicFragment;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import android.app.ActionBar;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangyun on 14/12/30.
 */
public class SearchResultActivity extends Activity implements SearchView.OnQueryTextListener ,AdapterView.OnItemClickListener {
    private SearchView mSearchView;
    private ActionBar mActionBar;
    private String mQueryResult;
    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;
    private ProgressBar mPreProgressBar;
    private TextView mEmptyView, mPreTextView;
    private Drawable mProgressDrawable;
    private int mSearchType, mReceiveTypeParam;
    private int mStart = 0, mEnd = 9;
    private String mEmptyTypeStr;
    private BaseAdapter mAdapter;
    private boolean mRefresh, mLoadMore, mIsListEmpty;

    private Map<String, String> mParams = new HashMap<String, String>();
    private ArrayList<PlayInfo> mPlayList = new ArrayList<PlayInfo>();
    private ArrayList<PlayInfo> mBusinessList = new ArrayList<PlayInfo>();
    private ArrayList<GroupNoteInfo> mGroupList = new ArrayList<GroupNoteInfo>();
    private ArrayList<ISlideMenuBasic> mFavorList = new ArrayList<ISlideMenuBasic>();
    private ArrayList<ISlideMenuBasic> mJoinList = new ArrayList<ISlideMenuBasic>();
    private ArrayList<ISlideMenuBasic> mPublishedList = new ArrayList<ISlideMenuBasic>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result_layout);
        handleIntent(getIntent());
        initActionBar();
        initView();
        initAdapter();
        setmEmptyStr();
        requestSearch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            mQueryResult = intent.getStringExtra(SearchManager.QUERY);
//
//        }
        mSearchType = intent.getExtras().getInt(PublicConstant.SEARCH_TYPE);
        mQueryResult = intent.getExtras().getString(PublicConstant.SEARCH_KEYWORD);
        mReceiveTypeParam = intent.getExtras().getInt(PublicConstant.TYPE, -1);
    }

    private void initActionBar() {

        mActionBar = getActionBar();

        View customSearchView = LayoutInflater.from(this).inflate(R.layout.custom_actionbar_layout, null);
        int searchViewWidth = getResources().getDimensionPixelSize(R.dimen.search_view_width);
        if (searchViewWidth == 0) {
            searchViewWidth = ActionBar.LayoutParams.MATCH_PARENT;
        }
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(searchViewWidth, ActionBar.LayoutParams.WRAP_CONTENT);
        mSearchView = (SearchView) customSearchView.findViewById(R.id.search_view);
        mSearchView.setIconified(true);
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
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);

        View backView = customSearchView.findViewById(R.id.back_menu_item);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//        mSearchView.setOnCloseListener(this);
        mSearchView.setQuery(mQueryResult, false);
        mSearchView.setOnQueryTextListener(this);
        mActionBar.setCustomView(customSearchView, layoutParams);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
    }

    private void initView() {
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.search_listview);
        mListView = mPullToRefreshListView.getRefreshableView();
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshListView.setOnRefreshListener(mRefreshListener);
        mPreProgressBar = (ProgressBar) findViewById(R.id.pre_progress);
        mPreTextView = (TextView) findViewById(R.id.pre_text);
        mPreTextView.setText(getString(R.string.searching));
        mEmptyView = new TextView(this);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgressBar.getIndeterminateDrawable().getBounds();
        mPreProgressBar.setIndeterminateDrawable(mProgressDrawable);
        mPreProgressBar.getIndeterminateDrawable().setBounds(bounds);

        mListView.setOnItemClickListener(this);
    }

    private void initAdapter() {
        switch (mSearchType) {
            case PublicConstant.SEARCH_NEARBY_MATE:

                break;
            case PublicConstant.SEARCH_NEARBY_DATE:

                break;
            case PublicConstant.SEARCH_NEARBY_COACH:

                break;
            case PublicConstant.SEARCH_NEARBY_ASSITANT:

                break;
            case PublicConstant.SEARCH_NEARBY_ROOM:

                break;
            case PublicConstant.SEARCH_FAVOR:
                mAdapter = new FavorBasicAdapter(this,mFavorList);
                break;
            case PublicConstant.SEARCH_JOIN:
                mAdapter = new PartInAdapter(this,mJoinList);
                break;
            case PublicConstant.SEARCH_PUBLISH:
                mAdapter = new PublishedBasicAdapter(this,mPublishedList);
                break;
            case PublicConstant.SEARCH_PLAY:
                mAdapter = new PlayListViewAdapter(this, mPlayList);
                break;
            case PublicConstant.SEARCH_GROUP:
                mAdapter = new GroupBasicAdapter(this, mGroupList);
                break;
            case PublicConstant.SEARCH_BUSINESS_PLAY:

                break;
        }
    }

    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        mQueryResult = query;
        requestSearch();
        return true;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        itemClick(position);
    }

    private void requestSearch() {
        switch (mSearchType) {
            case PublicConstant.SEARCH_NEARBY_MATE:

                break;
            case PublicConstant.SEARCH_NEARBY_DATE:

                break;
            case PublicConstant.SEARCH_NEARBY_COACH:

                break;
            case PublicConstant.SEARCH_NEARBY_ASSITANT:

                break;
            case PublicConstant.SEARCH_NEARBY_ROOM:

                break;
            case PublicConstant.SEARCH_FAVOR:
                searchFavorInfo();
                break;
            case PublicConstant.SEARCH_JOIN:
                searchPartInInfo();
                break;
            case PublicConstant.SEARCH_PUBLISH:
                searchPublishedInfo();
                break;
            case PublicConstant.SEARCH_PLAY:
                searchPlayInfo();
                break;
            case PublicConstant.SEARCH_GROUP:
                searchGroupInfo();
                break;
            case PublicConstant.SEARCH_BUSINESS_PLAY:
                searchBussinessPlay();
                break;
        }
    }

    private void searchPlayInfo() {

        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

//        params.put(HttpConstants.Play.TYPE, String.valueOf(mPlayType));
        mParams.put(HttpConstants.Play.START_NO, String.valueOf(mStart));
        mParams.put(HttpConstants.Play.END_NO, String.valueOf(mEnd));
        mParams.put(HttpConstants.Play.KEYWORD, mQueryResult);

        Log.d("wy", "search play params ->" + mParams);

        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.Play.GETLISTEE, mParams, HttpConstants.RequestMethod.GET, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy", "play search response ->" + response);
                try {
                    if (!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            if (response.getString("result") != null) {
                                List<PlayInfo> list = setPlayByJSON(response);
                                if (list.isEmpty()) {
                                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                } else {
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                                }
                            } else {
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                            mHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT) {
                            mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        } else {
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                        }
                    } else {
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                } catch (JSONException e) {
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

    private void searchBussinessPlay() {

        mParams.put(HttpConstants.Play.START_NO, String.valueOf(mStart));
        mParams.put(HttpConstants.Play.END_NO, String.valueOf(mEnd));
        mParams.put(HttpConstants.Play.KEYWORD, mQueryResult);

        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.Play.BUSINESS, mParams, HttpConstants.RequestMethod.GET, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if (!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            if (response.getString("result") != null) {
                                List<PlayInfo> list = setPlayByJSON(response);
                                if (list.isEmpty()) {
                                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                } else {
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                                }
                            } else {
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                            mHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT) {
                            mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        } else {
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                        }
                    } else {
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                } catch (JSONException e) {
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

    private void searchGroupInfo() {

        mParams.put(HttpConstants.GroupList.STAR_NO, String.valueOf(mStart));
        mParams.put(HttpConstants.GroupList.END_NO, String.valueOf(mEnd));
        mParams.put(HttpConstants.GroupList.KEYWORD, mQueryResult);

        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.GroupList.URL, mParams, HttpConstants.RequestMethod.GET, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy", "group response ->" + response);
                try {
                    if (!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            if (response.getJSONObject("result") != null) {
                                List<GroupNoteInfo> list = setGroupInfoByJSON(response);
                                if (list.isEmpty()) {
                                    mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                                } else {
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                                }
                            } else {
                                mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                            }
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT) {
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        } else {
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR, response.getString("msg")).sendToTarget();
                        }
                    } else {
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                } catch (JSONException e) {
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

    private void searchFavorInfo(){
        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        mParams.put(DatabaseConstant.UserTable.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        mParams.put(HttpConstants.Favor.TYPE,String.valueOf(mReceiveTypeParam == 1 ? 1 : mReceiveTypeParam + 1));
        mParams.put(HttpConstants.Favor.START_NO,String.valueOf(mStart));
        mParams.put(HttpConstants.Favor.END_NO, String.valueOf(mEnd));
        mParams.put(HttpConstants.Favor.KEYWORD,mQueryResult);

        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.Favor.URL,mParams,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if(!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            if (response.getJSONObject("result") != null) {
                                List<FavorInfo> list = setFavorByJSON(response);
                                if(list.isEmpty()){
                                    mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                                }else{
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                                }

                            }else{
                                mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                            }
                        }
                        else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        }
                        else if(response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        }
                        else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                } catch (JSONException e) {
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

    private void searchPartInInfo(){
        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        mParams.put(DatabaseConstant.UserTable.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        mParams.put(HttpConstants.Published.TYPE,String.valueOf(mReceiveTypeParam));
        mParams.put(HttpConstants.Published.START_NO,String.valueOf(mStart));
        mParams.put(HttpConstants.Published.END_NO, String.valueOf(mEnd));
        mParams.put(HttpConstants.Published.KEYWORD,mQueryResult);

        HttpUtil.requestHttp(HttpConstants.PartIn.URL,mParams,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                Log.d("wy","search join response ->" + response);
                try {
                    if(!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            if (response.getJSONObject("result") != null) {
                                List<PartInInfo> list = setPartInByJSON(response);
                                if(list.isEmpty()){
                                    mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                                }else{
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                                }

                            }else{
                                mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                            }
                        }
                        else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        }
                        else if(response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        }
                        else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                } catch (JSONException e) {
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

    private void searchPublishedInfo(){
        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        mParams.put(DatabaseConstant.UserTable.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        mParams.put(HttpConstants.Published.TYPE,String.valueOf(mReceiveTypeParam));
        mParams.put(HttpConstants.Published.START_NO,String.valueOf(mStart));
        mParams.put(HttpConstants.Published.END_NO, String.valueOf(mEnd));
        mParams.put(HttpConstants.Published.KEYWORD,mQueryResult);

        HttpUtil.requestHttp(HttpConstants.Published.URL,mParams,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if(!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            if (response.getJSONObject("result") != null) {
                                List<PublishedInfo> list = setPublishedInfoByJSON(response);
                                if(list.isEmpty()){
                                    mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                                }else{
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                                }

                            }else{
                                mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                            }
                        }
                        else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        }
                        else if(response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        }
                        else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                } catch (JSONException e) {
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

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPreProgressBar.setVisibility(View.GONE);
            mPreTextView.setVisibility(View.GONE);
            if(mPullToRefreshListView.getMode() == PullToRefreshBase.Mode.DISABLED){
                mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
            }
            if(mPullToRefreshListView.isRefreshing()){
                mPullToRefreshListView.onRefreshComplete();
            }
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    setEmptyViewGone();
                    handleResultWhenSuccess(msg);
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(SearchResultActivity.this, getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(SearchResultActivity.this, (String) msg.obj);
                    }
                    switch(mReceiveTypeParam){

                    }
                    if(mAdapter.getCount() == 0) {
                        setEmptyViewVisible();
                    }
                    break;
                case PublicConstant.NO_RESULT:
                    if(mAdapter.getCount() == 0){
                        setEmptyViewVisible();
                    }else{
                        Utils.showToast(SearchResultActivity.this,getString(R.string.no_search_info, mEmptyTypeStr));
                    }
                    break;
                case PublicConstant.NO_NETWORK:
                    Utils.showToast(SearchResultActivity.this,getString(R.string.network_not_available));
                    break;
            }
            mPullToRefreshListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
    };

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

                        infos.add(info);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return infos;
    }

    private List<PlayInfo> setPlayByJSON(JSONObject object){
        List<PlayInfo> list = new ArrayList<PlayInfo>();
        try{
            JSONArray list_data = object.getJSONObject("result").getJSONArray("list_data");
            if(object.getJSONObject("result").get("list_data").equals("null")){
                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
            }else {
                if (list_data.length() < 1) {
                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                } else {
                    for (int i = 0; i < list_data.length(); i++) {
                        PlayInfo info = new PlayInfo();
                        info.setTable_id(list_data.getJSONObject(i).getString("id"));
                        info.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                        info.setTitle(list_data.getJSONObject(i).getString("title"));
                        info.setContent(list_data.getJSONObject(i).getString("content"));
                        info.setCreate_time(list_data.getJSONObject(i).getString("create_time"));
//                        info.setType(String.valueOf(mPlayType));
                        list.add(info);
                    }
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return list;
    }

    private List<FavorInfo> setFavorByJSON(JSONObject jsonResult) {
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
                        itemInfo.setTable_id(list_data.getJSONObject(i).getString("id"));
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

    private List<PartInInfo> setPartInByJSON(JSONObject jsonResult) {
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

    private List<PublishedInfo> setPublishedInfoByJSON(JSONObject jsonResult){
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

    private PullToRefreshListView.OnRefreshListener2 mRefreshListener = new PullToRefreshBase.OnRefreshListener2() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView) {
            String label = DateUtils.formatDateTime(SearchResultActivity.this, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            if(mEmptyView.getVisibility() == View.VISIBLE){
                mEmptyView.setVisibility(View.GONE);
            }
            mRefresh = true;
            mLoadMore = false;
            if(Utils.networkAvaiable(SearchResultActivity.this)){
                mParams.put(HttpConstants.GroupList.STAR_NO,String.valueOf(0));
                mParams.put(HttpConstants.GroupList.END_NO,String.valueOf(9));
                requestSearch();
            }else{
                mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            String label = DateUtils.formatDateTime(SearchResultActivity.this, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
        }
    };

    /**
     * 为pull-to-refresh-listview设定EmptyView
     */
    private void setEmptyViewVisible(){
        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        mEmptyView.setTextColor(getResources().getColor(R.color.md__defaultBackground));
        mEmptyView.setText(getString(R.string.no_search_info, mEmptyTypeStr));
        mPullToRefreshListView.setEmptyView(mEmptyView);
    }

    protected void setEmptyViewGone(){
        if(null != mEmptyView){
            mEmptyView.setVisibility(View.GONE);
        }
    }
    /**
     * 根据不同的type值设定EmptyView显示的文字
     */
    private void setmEmptyStr(){
        switch(mSearchType){
            case PublicConstant.SEARCH_NEARBY_MATE:
                mEmptyTypeStr = getString(R.string.nearby_billiard_mate_str);
                break;
            case PublicConstant.SEARCH_NEARBY_DATE:
                mEmptyTypeStr = getString(R.string.billiard_dating);
                break;
            case PublicConstant.SEARCH_NEARBY_COACH:
                mEmptyTypeStr = getString(R.string.nearby_billiard_coauch_str);
                break;
            case PublicConstant.SEARCH_NEARBY_ASSITANT:
                mEmptyTypeStr = getString(R.string.nearby_billiard_assist_coauch_str);
                break;
            case PublicConstant.SEARCH_NEARBY_ROOM:
                mEmptyTypeStr = getString(R.string.nearby_billiard_coauch_str);
                break;
            case PublicConstant.SEARCH_FAVOR:
                String favorType = null;
                switch(mReceiveTypeParam){
                    case PublicConstant.FAVOR_DATE_TYPE:
                        favorType = getString(R.string.nearby_billiard_dating_str);
                        break;
                    case PublicConstant.FAVPR_ROOM_TYPE:
                        //TODO:由于球厅先不做，所以改变一下
                        //mEmptyTypeStr = mActivity.getString(R.string.nearby_billiard_coauch_str);
                        favorType = getString(R.string.tab_title_activity);
                        break;
                    case PublicConstant.FAVOR_PLAY_TYPE:
                        //mEmptyTypeStr = mActivity.getString(R.string.tab_title_activity);
                        favorType = getString(R.string.tab_title_billiards_circle);
                        break;
                }
                mEmptyTypeStr = getString(R.string.store_to_favor) + getString(R.string.of) + favorType + getString(R.string.info);
                break;
            case PublicConstant.SEARCH_JOIN:
                String joinType = null;
                switch(mReceiveTypeParam){
                    case PublicConstant.PART_IN_DATE_TYPE:
                        joinType = getString(R.string.nearby_billiard_dating_str);
                        break;
                    case PublicConstant.PART_IN_PLAY_TYPE:
                        joinType = getString(R.string.tab_title_activity);
                        break;

                }
                mEmptyTypeStr = getString(R.string.join_str) + getString(R.string.of) + joinType + getString(R.string.info);
                break;
            case PublicConstant.SEARCH_PUBLISH:
                String publishStr = null;
                switch(mReceiveTypeParam){
                    case PublicConstant.PUBLISHED_DATE_TYPE:
                        publishStr = getString(R.string.nearby_billiard_dating_str);
                        break;
                    case PublicConstant.PUBLISHED_ACTIVITY_TYPE:
                        publishStr = getString(R.string.tab_title_activity);
                        break;
                    case PublicConstant.PUBLISHED_GROUP_TYPE:
                        publishStr = getString(R.string.tab_title_billiards_circle);
                        break;

                }
                mEmptyTypeStr = getString(R.string.issue) + getString(R.string.of) + publishStr + getString(R.string.info);
                break;
            case PublicConstant.SEARCH_PLAY:
                mEmptyTypeStr  = getString(R.string.play);
                break;
            case PublicConstant.SEARCH_GROUP:
                mEmptyTypeStr = getString(R.string.billiard_group);
                break;
            case PublicConstant.SEARCH_BUSINESS_PLAY:
                mEmptyTypeStr = getString(R.string.business_play);
                break;
        }
    }

    private void handleResultWhenSuccess(Message msg){
        switch(mSearchType){
            case PublicConstant.SEARCH_NEARBY_MATE:

                break;
            case PublicConstant.SEARCH_NEARBY_DATE:

                break;
            case PublicConstant.SEARCH_NEARBY_COACH:

                break;
            case PublicConstant.SEARCH_NEARBY_ASSITANT:

                break;
            case PublicConstant.SEARCH_NEARBY_ROOM:

                break;
            case PublicConstant.SEARCH_FAVOR:
                mIsListEmpty = mFavorList.isEmpty();
                List<FavorInfo> favorList = (List<FavorInfo>) msg.obj;
                for(FavorInfo info : favorList){
//                    if (!mFavorList.contains(info)) {
//
//                        if(!mIsListEmpty && Integer.valueOf(((FavorInfo)mFavorList.get(0)).getTable_id()) < Integer.valueOf(info.getTable_id())){
//                            mFavorList.add(0,info);
//                        }else {
//                            mFavorList.add(info);
//                        }
//                    }
                    if(mRefresh && !mIsListEmpty) {
                        mFavorList.add(0,info);
                    }else{
                        mFavorList.add(info);
                    }

                }
                if(mFavorList.isEmpty()){
                    setEmptyViewVisible();
                }
                break;
            case PublicConstant.SEARCH_JOIN:
                mIsListEmpty = mJoinList.isEmpty();
                List<PartInInfo> joinList = (List<PartInInfo>) msg.obj;
                for(PartInInfo info : joinList){
//                    if(!mJoinList.contains(info)){
//                        if(!mIsListEmpty && Integer.valueOf(((PartInInfo)mJoinList.get(0)).getTable_id()) < Integer.valueOf(info.getTable_id())){
//                            mJoinList.add(0,info);
//                        }else {
//                            mJoinList.add(info);
//                        }
//                    }
                    if(mRefresh && !mIsListEmpty) {
                        mJoinList.add(0,info);
                    }else{
                        mJoinList.add(info);
                    }
                }
                if(mJoinList.isEmpty()){
                    setEmptyViewVisible();
                }
                break;
            case PublicConstant.SEARCH_PUBLISH:
                mIsListEmpty = mPublishedList.isEmpty();
                List<PublishedInfo> publishedInfoList = (List<PublishedInfo>) msg.obj;
                for(PublishedInfo info : publishedInfoList){
                    if (!mPublishedList.contains(info)) {
                        if(!mIsListEmpty && Integer.valueOf(((PublishedInfo)mPublishedList.get(0)).getTable_id()) < Integer.valueOf(info.getTable_id())){
                            mPublishedList.add(0,info);
                        }else {
                            mPublishedList.add(info);
                        }
                    }
                }
                if(mPublishedList.isEmpty()){
                    setEmptyViewVisible();
                }
                break;
            case PublicConstant.SEARCH_PLAY:

                mIsListEmpty = mPlayList.isEmpty();
                List<PlayInfo> playList = (List<PlayInfo>) msg.obj;
                for(PlayInfo info : playList){
                    if (!mPlayList.contains(info)) {

                        if(!mIsListEmpty && Integer.valueOf(mPlayList.get(0).getTable_id()) < Integer.valueOf(info.getTable_id())){
                            mPlayList.add(0, info);
                        }else {
                            mPlayList.add(info);
                        }
                    }
                }
                if(mPlayList.isEmpty()){
                    setEmptyViewVisible();
                }

                break;
            case PublicConstant.SEARCH_GROUP:
                mIsListEmpty = mGroupList.isEmpty();
                List<GroupNoteInfo> groupList = (List<GroupNoteInfo>) msg.obj;
                for(GroupNoteInfo info : groupList){

                    if (!mGroupList.contains(info)) {

                        if(!mIsListEmpty && mGroupList.get(0).getNoteId() < info.getNoteId()){
                            mGroupList.add(0,info);
                        }else {
                            mGroupList.add(info);
                        }
                    }
                }

                if(mGroupList.isEmpty()) {
                    setEmptyViewVisible();
                }
                break;
            case PublicConstant.SEARCH_BUSINESS_PLAY:
                List<PlayInfo> businessList = (List<PlayInfo>) msg.obj;
                for(PlayInfo info : businessList){
                    if(!mBusinessList.contains(info)){
                        mBusinessList.add(info);
                    }
                }
                if(mBusinessList.isEmpty()){
                    setEmptyViewVisible();
                }
                break;
        }
    }

    private void itemClick(int position){
        Intent intent = null;
        Bundle args = new Bundle();
        switch(mSearchType){
            case PublicConstant.SEARCH_NEARBY_MATE:

                break;
            case PublicConstant.SEARCH_NEARBY_DATE:

                break;
            case PublicConstant.SEARCH_NEARBY_COACH:

                break;
            case PublicConstant.SEARCH_NEARBY_ASSITANT:

                break;
            case PublicConstant.SEARCH_NEARBY_ROOM:

                break;
            case PublicConstant.SEARCH_FAVOR:
                FavorInfo info = (FavorInfo) mAdapter.getItem(position-1);
                //TODO:不做缓存，所以这个字段先不要
//        int subType = info.getSubType();
                int favor_table_id = Integer.valueOf(info.getRid());
                String favor_username = info.getUserName();
                String favor_img_url = info.getImg_url();

                String favor_create_time = info.getCreateTime();

                switch(mReceiveTypeParam){
                    case PublicConstant.FAVOR_GROUP_TYPR:
                        break;
                    case PublicConstant.FAVPR_ROOM_TYPE:
                        //TODO:由于先不做球厅，所以这里实际是PLAY
                        args.putInt(DatabaseConstant.PlayTable.TABLE_ID,favor_table_id);
                        args.putString(DatabaseConstant.PlayTable.CREATE_TIME,favor_create_time);

                        intent = new Intent(this, PlayDetailActivity.class);
                        break;
                    case PublicConstant.FAVOR_DATE_TYPE:
                        args.putInt(NearbyFragmentsCommonUtils.KEY_DATING_TABLE_ID,favor_table_id);
                        args.putString(NearbyFragmentsCommonUtils.KEY_DATING_FRAGMENT_PHOTO,favor_img_url);
                        args.putString(NearbyFragmentsCommonUtils.KEY_DATING_USER_NAME,favor_username);

                        intent = new Intent(this, NearbyBilliardsDatingActivity.class);
                        break;
                    case PublicConstant.FAVOR_PLAY_TYPE:
                        //TODO:由于先不做球厅，所以这里实际是GROUP
                        args.putInt(DatabaseConstant.GroupInfo.NOTE_ID,favor_table_id);

                        intent = new Intent(this, BilliardGroupDetailActivity.class);
                        break;
                }
                break;
            case PublicConstant.SEARCH_JOIN:
                PartInInfo joinInfo = (PartInInfo) mAdapter.getItem(position - 1);

                int join_table_id = Integer.valueOf(joinInfo.getTable_id());
                String join_username = joinInfo.getUsername();
                String join_img_url = joinInfo.getImg_url();

                String join_create_time = joinInfo.getDateTime();

                switch (mReceiveTypeParam){
                    case PublicConstant.PART_IN_DATE_TYPE:
                        args.putInt(NearbyFragmentsCommonUtils.KEY_DATING_TABLE_ID,join_table_id);
                        args.putString(NearbyFragmentsCommonUtils.KEY_DATING_FRAGMENT_PHOTO,join_img_url);
                        args.putString(NearbyFragmentsCommonUtils.KEY_DATING_USER_NAME,join_username);

                        intent = new Intent(this, NearbyBilliardsDatingActivity.class);

                        break;
                    case PublicConstant.PART_IN_PLAY_TYPE:
                        args.putInt(DatabaseConstant.PlayTable.TABLE_ID,join_table_id);
                        args.putString(DatabaseConstant.PlayTable.CREATE_TIME,join_create_time);

                        intent = new Intent(this, PlayDetailActivity.class);
                        break;
                }
                break;
            case PublicConstant.SEARCH_PUBLISH:
                PublishedInfo publishedInfo = (PublishedInfo) mAdapter.getItem(position-1);
                int published_table_id = Integer.valueOf(publishedInfo .getTable_id());
                String published_username = YueQiuApp.sUserInfo.getUsername();
                String published_img_url = YueQiuApp.sUserInfo.getImg_url();

                String published_create_time = publishedInfo .getDateTime();

                switch (mReceiveTypeParam){
                    case PublicConstant.PUBLISHED_ACTIVITY_TYPE:
                        args.putInt(DatabaseConstant.PlayTable.TABLE_ID,published_table_id);
                        args.putString(DatabaseConstant.PlayTable.CREATE_TIME,published_create_time);

                        intent = new Intent(this, PlayDetailActivity.class);
                        break;
                    case PublicConstant.PUBLISHED_DATE_TYPE:
                        args.putInt(NearbyFragmentsCommonUtils.KEY_DATING_TABLE_ID,published_table_id);
                        args.putString(NearbyFragmentsCommonUtils.KEY_DATING_FRAGMENT_PHOTO,published_img_url);
                        args.putString(NearbyFragmentsCommonUtils.KEY_DATING_USER_NAME,published_username);

                        intent = new Intent(this, NearbyBilliardsDatingActivity.class);
                        break;
                    case PublicConstant.PUBLISHED_GROUP_TYPE:
                        args.putInt(DatabaseConstant.GroupInfo.NOTE_ID,published_table_id);

                        intent = new Intent(this, BilliardGroupDetailActivity.class);
                        break;
                }

                break;
            case PublicConstant.SEARCH_PLAY:
                PlayInfo playInfo = (PlayInfo) mAdapter.getItem(position-1);
                intent = new Intent(this, PlayDetailActivity.class);
                args.putInt(DatabaseConstant.PlayTable.TABLE_ID,Integer.parseInt(playInfo.getTable_id()));
                args.putString(DatabaseConstant.PlayTable.CREATE_TIME,playInfo.getCreate_time());
                args.putString(DatabaseConstant.PlayTable.TYPE,playInfo.getType());

                break;
            case PublicConstant.SEARCH_GROUP:
                GroupNoteInfo groupInfo = (GroupNoteInfo) mAdapter.getItem(position-1);
                intent = new Intent(this, BilliardGroupDetailActivity.class);
                args.putInt(DatabaseConstant.GroupInfo.NOTE_ID,groupInfo.getNoteId());
                args.putInt(DatabaseConstant.GroupInfo.COMMENT_COUNT,groupInfo.getCommentCount());
                break;
            case PublicConstant.SEARCH_BUSINESS_PLAY:
                PlayInfo BusinessInfo = (PlayInfo) mAdapter.getItem(position-1);
                intent = new Intent(this, PlayDetailActivity.class);
                args.putInt(PublicConstant.PLAY_TYPE,PublicConstant.PLAY_BUSSINESS);
                args.putInt(DatabaseConstant.PlayTable.TABLE_ID,Integer.parseInt(BusinessInfo.getTable_id()));
                args.putString(DatabaseConstant.PlayTable.CREATE_TIME,BusinessInfo.getCreate_time());
                args.putString(DatabaseConstant.PlayTable.TYPE,BusinessInfo.getType());
                break;
        }
        intent.putExtras(args);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
    }

}
