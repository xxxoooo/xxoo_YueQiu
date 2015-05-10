package com.yueqiu.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.AddAdapter;
import com.yueqiu.adapter.FavorBasicAdapter;
import com.yueqiu.adapter.GroupBasicAdapter;
import com.yueqiu.adapter.NearbyAssistCoauchSubFragmentListAdapter;
import com.yueqiu.adapter.NearbyCoauchSubFragmentListAdapter;
import com.yueqiu.adapter.NearbyDatingSubFragmentListAdapter;
import com.yueqiu.adapter.NearbyMateSubFragmentListAdapter;
import com.yueqiu.adapter.NearbyRoomSubFragmentListAdapter;
import com.yueqiu.adapter.PartInAdapter;
import com.yueqiu.adapter.PlayListViewAdapter;
import com.yueqiu.adapter.PublishedBasicAdapter;
import com.yueqiu.bean.FavorInfo;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.bean.ISlideMenuBasic;
import com.yueqiu.bean.NearbyAssistCoauchSubFragmentBean;
import com.yueqiu.bean.NearbyCoauchSubFragmentCoauchBean;
import com.yueqiu.bean.NearbyDatingSubFragmentDatingBean;
import com.yueqiu.bean.NearbyMateSubFragmentUserBean;
import com.yueqiu.bean.NearbyPeopleInfo;
import com.yueqiu.bean.NearbyRoomBean;
//import com.yueqiu.bean.NearbyRoomSubFragmentRoomBean;
import com.yueqiu.bean.PartInInfo;
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.chatbar.AddPersonFragment;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.fragment.nearby.common.NearbyParamsPreference;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import android.app.ActionBar;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.concurrent.ConcurrentHashMap;

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
    private int mBeforeCount,mAfterCount;

    private Map<String, String> mParams = new HashMap<String, String>();
    private ArrayList<PlayInfo> mPlayList = new ArrayList<PlayInfo>();
    private ArrayList<PlayInfo> mBusinessList = new ArrayList<PlayInfo>();
    private ArrayList<GroupNoteInfo> mGroupList = new ArrayList<GroupNoteInfo>();
    private ArrayList<ISlideMenuBasic> mFavorList = new ArrayList<ISlideMenuBasic>();
    private ArrayList<ISlideMenuBasic> mJoinList = new ArrayList<ISlideMenuBasic>();
    private ArrayList<ISlideMenuBasic> mPublishedList = new ArrayList<ISlideMenuBasic>();

    // 提供所有同附近Activity当中的Fragment相关的List数据
    private ArrayList<NearbyMateSubFragmentUserBean> mNearbyMateList = new ArrayList<NearbyMateSubFragmentUserBean>();
    private ArrayList<NearbyDatingSubFragmentDatingBean> mNearbyDatingList = new ArrayList<NearbyDatingSubFragmentDatingBean>();
    private ArrayList<NearbyAssistCoauchSubFragmentBean> mNearbyASList = new ArrayList<NearbyAssistCoauchSubFragmentBean>();
    private ArrayList<NearbyCoauchSubFragmentCoauchBean> mNearbyCoauchList = new ArrayList<NearbyCoauchSubFragmentCoauchBean>();
    private ArrayList<NearbyRoomBean> mNearbyRoomList = new ArrayList<NearbyRoomBean>();

    private ArrayList<NearbyPeopleInfo.SearchPeopleItemInfo> mFriendList = new ArrayList<NearbyPeopleInfo.SearchPeopleItemInfo>();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.billiard_search, menu);

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
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        mSearchView.setQuery(mQueryResult, false);
        mSearchView.setOnQueryTextListener(this);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void initActionBar() {

        mActionBar = getActionBar();
//        View customSearchView = LayoutInflater.from(this).inflate(R.layout.custom_actionbar_layout, null);
//        int searchViewWidth = getResources().getDimensionPixelSize(R.dimen.search_view_width);
//        if (searchViewWidth == 0) {
//            searchViewWidth = ActionBar.LayoutParams.MATCH_PARENT;
//        }
//        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(searchViewWidth, ActionBar.LayoutParams.WRAP_CONTENT);
//        mSearchView = (SearchView) customSearchView.findViewById(R.id.search_view);
//        View backView = customSearchView.findViewById(R.id.back_menu_item);
//        backView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//        mSearchView.setOnCloseListener(this);

//        mActionBar.setCustomView(customSearchView, layoutParams);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setTitle(getString(R.string.btn_back));
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
                mAdapter = new NearbyMateSubFragmentListAdapter(this, mNearbyMateList);
                break;
            case PublicConstant.SEARCH_NEARBY_DATE:
                mAdapter = new NearbyDatingSubFragmentListAdapter(this, mNearbyDatingList);
                break;
            case PublicConstant.SEARCH_NEARBY_COACH:
                mAdapter = new NearbyCoauchSubFragmentListAdapter(this, mNearbyCoauchList);
                break;
            case PublicConstant.SEARCH_NEARBY_ASSITANT:
                mAdapter = new NearbyAssistCoauchSubFragmentListAdapter(this, mNearbyASList);
                break;
            case PublicConstant.SEARCH_NEARBY_ROOM:
                // TODO: 大众点评提供的是单独的接口，暂时先不做了(因为大众点评当中提供筛选和搜索是两个不同的过程，
                // TODO: 因为搜索需要的是任意的例如者球厅的名字左右搜索的关键字，但是筛选提供的只是一个单纯的
                // TODO: 将我们的请求参数的当中的关键字做了以下筛选，所以如果要做搜索的筛选，我们还是需要单独
                // TODO: 的使用另外的接口，但是大众点评并没有提供供第三方程序使用的搜索接口
                // TODO: 关于大众点评的搜索实现的另外一种思路就是本地搜索，即我们通过将RESTful service当中
                // TODO: 请求到的所有的数据保存到本地，然后通过SQLite当中提供特定的关键字进行搜索，这样我们的搜索
                // TODO: 范围就全部限定到了本地的数据，但是是可行的)

                // 现在对于台球厅的搜索方案暂时定为采用keyWord的方式进行搜索
                mAdapter = new NearbyRoomSubFragmentListAdapter(this, mNearbyRoomList);

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
                mAdapter = new PlayListViewAdapter(this,mBusinessList);
                break;
            case PublicConstant.SEARCH_FRIEND:
                mAdapter = new AddAdapter(this,mFriendList);
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
        if(mEmptyView != null){
            mEmptyView.setVisibility(View.GONE);
        }
        switch (mSearchType) {
            case PublicConstant.SEARCH_NEARBY_MATE:
                mNearbyMateList.clear();
                break;
            case PublicConstant.SEARCH_NEARBY_DATE:
                mNearbyDatingList.clear();
                break;
            case PublicConstant.SEARCH_NEARBY_COACH:
                mNearbyCoauchList.clear();
                break;
            case PublicConstant.SEARCH_NEARBY_ASSITANT:
                mNearbyASList.clear();
                break;
            case PublicConstant.SEARCH_NEARBY_ROOM:
                mNearbyRoomList.clear();
                break;
            case PublicConstant.SEARCH_FAVOR:
                mFavorList.clear();
                break;
            case PublicConstant.SEARCH_JOIN:
                mJoinList.clear();
                break;
            case PublicConstant.SEARCH_PUBLISH:
                mPublishedList.clear();
                break;
            case PublicConstant.SEARCH_PLAY:
                mPlayList.clear();
                break;
            case PublicConstant.SEARCH_GROUP:
                mGroupList.clear();
                break;
            case PublicConstant.SEARCH_BUSINESS_PLAY:
                mBusinessList.clear();
                break;
            case PublicConstant.SEARCH_FRIEND:
                mFriendList.clear();
                break;
        }
        mAdapter.notifyDataSetChanged();
        mQueryResult = query;
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
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
                Log.d(TAG, " 1. start search the mate info list ");
                searchMateInfo();
                break;
            case PublicConstant.SEARCH_NEARBY_DATE:
                searchDatingInfo();
                break;
            case PublicConstant.SEARCH_NEARBY_COACH:
                searchCoauchInfo();
                break;
            case PublicConstant.SEARCH_NEARBY_ASSITANT:
                searchASInfo();
                break;
            case PublicConstant.SEARCH_NEARBY_ROOM:
                // 我们需要将我们从球厅搜索传递过来的所有数据
                searchRoomInfo();
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
            case PublicConstant.SEARCH_FRIEND:
                searchFriend();
                break;
        }
    }
    private static final String TAG = "search_nearby_tag";
    // 以下的四个方法是具体的搜索实现过程
    private void searchMateInfo()
    {
        Log.d(TAG, "2 search mate info list ... ");
        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        mParams.put(HttpConstants.Play.START_NO, String.valueOf(mStart));
        mParams.put(HttpConstants.Play.END_NO, String.valueOf(mEnd));
        mParams.put(HttpConstants.Play.KEYWORD, mQueryResult);
        Log.d(TAG, " params : " + mParams);

        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.NearbyMate.URL, mParams, HttpConstants.RequestMethod.GET, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                super.onSuccess(statusCode, headers, response);
                Log.d(TAG, " 3. the initial mate response we get are : " + response);
                try {
                    if (!response.isNull("code"))
                    {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL)
                        {
                            if (response.getString("result") != null)
                            {
                                List<NearbyMateSubFragmentUserBean> list = setMateInfoByJson(response);
                                if (list.isEmpty())
                                {
                                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                } else
                                {
                                    Log.d(TAG, "4. successfully get the data, and send this data to the mHandler, let the Handler to process it. the list size : " + list.size());
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                                }
                            } else
                            {
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT)
                        {
                            mHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT)
                        {
                            mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        } else
                        {
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                        }
                    } else
                    {
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                    Log.d(TAG, " exception happened while we search mate info and reason goes to : " + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });
    }

    private void searchDatingInfo()
    {
        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        mParams.put(HttpConstants.Play.START_NO, String.valueOf(mStart));
        mParams.put(HttpConstants.Play.END_NO, String.valueOf(mEnd));
        mParams.put(HttpConstants.Play.KEYWORD, mQueryResult);

        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.NearbyDating.URL, mParams, HttpConstants.RequestMethod.GET, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d(TAG, " search dating info , initial result are : " + response);
                try
                {
                    if (!response.isNull("code"))
                    {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL)
                        {
                            if (response.getString("result") != null)
                            {
                                List<NearbyDatingSubFragmentDatingBean> list = setDatingInfoByJson(response);
                                if (list.isEmpty())
                                {
                                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                } else
                                {
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                                }
                            } else
                            {
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT)
                        {
                            mHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT)
                        {
                            mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        } else
                        {
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                        }
                    } else
                    {
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable)
            {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });

    }

    private void searchASInfo()
    {
        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        mParams.put(HttpConstants.Play.START_NO, String.valueOf(mStart));
        mParams.put(HttpConstants.Play.END_NO, String.valueOf(mEnd));
        mParams.put(HttpConstants.Play.KEYWORD, mQueryResult);

        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.NearbyAssistCoauch.URL, mParams, HttpConstants.RequestMethod.GET, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                super.onSuccess(statusCode, headers, response);
                Log.d(TAG, " search AS info , initial result are : " + response);
                try {
                    if (!response.isNull("code"))
                    {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL)
                        {
                            if (response.getString("result") != null) {
                                List<NearbyAssistCoauchSubFragmentBean> list = setASInfoByJson(response);
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

    private void searchCoauchInfo()
    {
        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        mParams.put(HttpConstants.Play.START_NO, String.valueOf(mStart));
        mParams.put(HttpConstants.Play.END_NO, String.valueOf(mEnd));
        mParams.put(HttpConstants.Play.KEYWORD, mQueryResult);

        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.NearbyCoauch.URL, mParams, HttpConstants.RequestMethod.GET, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d(TAG, " search coauch info : " + response);
                try{
                    if (!response.isNull("code")){
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            if (response.getString("result") != null){
                                List<NearbyCoauchSubFragmentCoauchBean> list = setCoauchInfoByJson(response);
                                if (list.isEmpty()){
                                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                } else{
                                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                                }
                            } else{
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                            mHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                        } else if (response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                            mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        } else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                        }
                    } else{
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable)
            {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });
    }
    private static final String REQUEST_KEYWORD = "台球,桌球室,台球室,桌球";
    private void searchRoomInfo(){
        // 我们在搜索时，是不需要替用户指定搜索的区，因为用于搜索肯定指的是北京城范围内的
        // 并且搜索时我们也不应该指定搜索的排名规则，所以我们就按默认的排名顺序规则，即sort=1
        // 同时，我们也不应指定当前的距离范围
        NearbyParamsPreference paramsPreference = NearbyParamsPreference.getInstance();

        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        Map<String,String> params = new HashMap<String, String>();
        params.put(HttpConstants.GET_ROOM.START_NO,String.valueOf(mStart));
        params.put(HttpConstants.GET_ROOM.END_NO,String.valueOf(mEnd));
        float lng = paramsPreference.getRoomLongi(this);
        float lat = paramsPreference.getRoomLati(this);
        params.put(HttpConstants.GET_ROOM.LAT,String.valueOf(lat));
        params.put(HttpConstants.GET_ROOM.LNG,String.valueOf(lng));
        params.put(HttpConstants.Play.KEYWORD, mQueryResult);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.GET_ROOM.URL,params,HttpConstants.RequestMethod.GET, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                super.onSuccess(statusCode, headers, response);
                Log.d(TAG, "room response ->" + response);
                try {
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            List<NearbyRoomBean> roomList = new ArrayList<NearbyRoomBean>();
                            if(response.get("result").equals("null")){
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }else{
                                JSONArray list_data = response.getJSONArray("result");
                                for (int i = 0; i < list_data.length(); i++) {
                                    NearbyRoomBean room = new NearbyRoomBean();
                                    room.setId(list_data.getJSONObject(i).getString("id"));
                                    room.setName(list_data.getJSONObject(i).getString("name"));
                                    room.setAddress(list_data.getJSONObject(i).getString("address"));
                                    room.setTelephone(list_data.getJSONObject(i).getString("telephone"));
                                    room.setDetail_info(list_data.getJSONObject(i).getString("detail_info"));
                                    room.setPrice(list_data.getJSONObject(i).getString("price"));
                                    room.setShop_hours(list_data.getJSONObject(i).getString("shop_hours"));
                                    room.setRange(list_data.getJSONObject(i).getString("range"));
                                    room.setOverall_rating(list_data.getJSONObject(i).getString("overall_rating"));
                                    room.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                                    roomList.add(room);
                                }
                            }
                            if (roomList.isEmpty()) {
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            } else {
                                mHandler.obtainMessage(PublicConstant.GET_SUCCESS, roomList).sendToTarget();
                            }
                        } else {
                            mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable)
            {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });

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

    private void searchFriend(){

        mFriendList.clear();

        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        Map<String, String> map = new HashMap<String, String>();
        map.put(HttpConstants.SearchPeopleByKeyword.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));//
        map.put(HttpConstants.SearchPeopleByKeyword.KEYWORDS, mQueryResult);


        Log.d("wy","search friend param ->" + map);

        HttpUtil.requestHttp(HttpConstants.SearchPeopleByKeyword.URL, map, HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","add friend response ->" + response);
                try{
                    if (!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            NearbyPeopleInfo searchPeople = new NearbyPeopleInfo();
                            JSONArray list_data = response.getJSONObject("result").getJSONArray("list_data");
                            for (int i = 0; i < list_data.length(); i++) {
                                NearbyPeopleInfo.SearchPeopleItemInfo itemInfo = searchPeople.new SearchPeopleItemInfo();
                                itemInfo.setUser_id(list_data.getJSONObject(i).getInt("user_id"));
                                itemInfo.setUsername(list_data.getJSONObject(i).getString("username"));
                                itemInfo.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                                itemInfo.setSex(list_data.getJSONObject(i).getInt("sex"));
                                itemInfo.setDistrict(list_data.getJSONObject(i).getString("district"));
                                searchPeople.mList.add(itemInfo);
                            }
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS, searchPeople).sendToTarget();
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
                }catch (JSONException e){
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

                    if(mAdapter.getCount() == 0) {
                        setEmptyViewVisible();
                        if(null == msg.obj){
                            mEmptyView.setText(getString(R.string.http_request_error));
                        }else{
                            mEmptyView.setText((String) msg.obj);
                        }
                    }else{
                        if(null == msg.obj){
                            Utils.showToast(SearchResultActivity.this, getString(R.string.http_request_error));
                        }else{
                            Utils.showToast(SearchResultActivity.this, (String) msg.obj);
                        }
                    }
                    break;
                case PublicConstant.NO_RESULT:
                    if(mAdapter.getCount() == 0){
                        setEmptyViewVisible();
                        if(mSearchType == PublicConstant.SEARCH_FRIEND){
                            mEmptyView.setText(mEmptyTypeStr);
                        }
                    }else{
                        if(!mLoadMore) {
                            Utils.showToast(SearchResultActivity.this, getString(R.string.no_search_info, mEmptyTypeStr));
                        }
                    }
                    break;
                case PublicConstant.NO_NETWORK:
                    if(mAdapter.getCount() == 0){
                        setEmptyViewVisible();
                        mEmptyView.setText(getString(R.string.network_not_available));
                    }else{
                        Utils.showToast(SearchResultActivity.this,getString(R.string.network_not_available));
                    }

                    break;
            }
            mPullToRefreshListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
    };

    private List<NearbyMateSubFragmentUserBean> setMateInfoByJson(JSONObject object)
    {
        List<NearbyMateSubFragmentUserBean> mateList = new ArrayList<NearbyMateSubFragmentUserBean>();
        try
        {
            if (! TextUtils.isEmpty(object.get("code").toString()))
            {
                final int code = object.getInt("code");
                JSONObject resultJson = object.getJSONObject("result");
                if (code == HttpConstants.ResponseCode.NORMAL)
                {
                    if(resultJson != null)
                    {
                        final int dataCount = resultJson.getInt("count");
                        if(resultJson.get("list_data").equals("null")){
                            mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        }else {
                            JSONArray dataList = resultJson.getJSONArray("list_data");
                            if(dataList.length() < 1) {
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }else{
                                int i;
                                final int len = dataList.length();
                                for (i = 0; i < len; ++i) {
                                    JSONObject dataObj = (JSONObject) dataList.get(i);
                                    String imgUrl = dataObj.getString("img_url");
                                    String sex = dataObj.getString("sex");
                                    String userName = dataObj.getString("username");
                                    String userId = dataObj.getString("user_id");
                                    int range = dataObj.getInt("range");
                                    String district = dataObj.getString("district");
                                    NearbyMateSubFragmentUserBean mateUserBean = new NearbyMateSubFragmentUserBean(
                                            userId,
                                            imgUrl,
                                            userName,
                                            NearbyFragmentsCommonUtils.parseGenderStr(SearchResultActivity.this, sex),
                                            district,
                                            String.valueOf(range));

                                    mateList.add(mateUserBean);
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
            Log.d(TAG, " exception happened while we parsing the initial result json object to the target mate bean, and the reason goes to : " + e.toString());
        }

        return mateList;
    }

    private List<NearbyDatingSubFragmentDatingBean> setDatingInfoByJson(JSONObject object)
    {
        List<NearbyDatingSubFragmentDatingBean> datingList = new ArrayList<NearbyDatingSubFragmentDatingBean>();

        try
        {
            if (!TextUtils.isEmpty(object.toString()))
            {
                if (! TextUtils.isEmpty(object.get("code").toString()))
                {
                    final int code = object.getInt("code");
                    if (code == HttpConstants.ResponseCode.NORMAL)
                    {
                        if (! "null".equals(object.get("result").toString()))
                        {
                            JSONArray resultJsonArr = object.getJSONArray("result");
                            final int size = resultJsonArr.length();
                            if(size < 1)
                            {
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }else {
                                for (int i = 0; i < size; ++i) {
                                    JSONObject subJsonObj = (JSONObject) resultJsonArr.get(i);
                                    String imgUrl = subJsonObj.getString("img_url");
                                    String datingId = subJsonObj.getString("id");
                                    String title = subJsonObj.getString("title");
                                    String userName = subJsonObj.getString("username");
                                    long distance = subJsonObj.getLong("range");
                                    NearbyDatingSubFragmentDatingBean datingBean = new NearbyDatingSubFragmentDatingBean(datingId, imgUrl, userName, title, String.valueOf(distance));

                                    // 将我们解析得到的datingBean插入到我们创建的数据库当中
                                    datingList.add(datingBean);
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        return datingList;
    }

    private List<NearbyAssistCoauchSubFragmentBean> setASInfoByJson(JSONObject jsonObject)
    {
        List<NearbyAssistCoauchSubFragmentBean> asList = new ArrayList<NearbyAssistCoauchSubFragmentBean>();

        try
        {
            if (! TextUtils.isEmpty(jsonObject.get("code").toString()))
            {
                final int status = jsonObject.getInt("code");
                if (status == HttpConstants.ResponseCode.NORMAL)
                {
                    JSONObject resultJsonObj = jsonObject.getJSONObject("result");
                    if(resultJsonObj.get("list_data").equals("null")){
                        mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                    }else {
                        JSONArray resultArr = resultJsonObj.getJSONArray("list_data");
                        final int count = resultArr.length();
                        if(count < 1){
                            mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        }else {
                            for (int i = 0; i < count; ++i) {
                                JSONObject dataUnit = resultArr.getJSONObject(i);

                                // TODO: 现在服务器端还没有提供完整的数据，现在的字段都不是完整的(他返回的是球友列表的内容，我们暂时先这么做，等服务器那边改了以后再做修改)
                                // TODO: ？？？另外就是有一个问题比较重要，那就是助教的demo当中是没有划分level的，即假设每一个助教的水平都是相同的？？？
                                // TODO: ？？？但是在列表的筛选button当中却有一个一个按助教的水平来进行筛选的条件。这是一个很bug的地方。同Server端的同学进行协商？？？
                                String userId = dataUnit.getString("user_id");
                                String photoUrl = dataUnit.getString("img_url");
                                String name = dataUnit.getString("username");
                                String sex = dataUnit.getString("sex");
                                String money = dataUnit.getString("money");
                                long range = dataUnit.getLong("range");
                                String kinds = dataUnit.getString("class");
                                String district = dataUnit.getString("district");

                                NearbyAssistCoauchSubFragmentBean assistCoauchBean = new NearbyAssistCoauchSubFragmentBean(
                                        userId,
                                        photoUrl,
                                        name,
                                        NearbyFragmentsCommonUtils.parseGenderStr(SearchResultActivity.this, sex),
                                        NearbyFragmentsCommonUtils.parseBilliardsKinds(SearchResultActivity.this, kinds),
                                        money,
                                        String.valueOf(range)
                                );
                                asList.add(assistCoauchBean);
                            }
                        }
                    }
                }
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return asList;
    }

    private List<NearbyCoauchSubFragmentCoauchBean> setCoauchInfoByJson(JSONObject response)
    {
        List<NearbyCoauchSubFragmentCoauchBean> coauchList = new ArrayList<NearbyCoauchSubFragmentCoauchBean>();

        try
        {
            if (!TextUtils.isEmpty(response.get("code").toString()))
            {
                final int status = response.getInt("code");
                if (status == HttpConstants.ResponseCode.NORMAL)
                {
                    JSONObject resultJsonObj = response.getJSONObject("result");
                    if (null != resultJsonObj)
                    {
                        if(!resultJsonObj.get("list_data").equals("null")) {
                            JSONArray dataArr = resultJsonObj.getJSONArray("list_data");
                            final int len = dataArr.length();
                            if(len < 1)
                            {
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }else {
                                for (int i = 0; i < len; ++i) {
                                    JSONObject dataUnit = dataArr.getJSONObject(i);

                                    // TODO: 部分字段的值还需要进一步的确认
                                    String userId = dataUnit.getString("user_id");
                                    String photoUrl = dataUnit.getString("img_url");
                                    String userName = dataUnit.getString("username");
                                    // TODO: 这里需要注意的是我们得到的关于资质的字段值是一个数字，我们还需要进一步同服务器端确定以下这几个数字分别代表的具体的含义
                                    String level = dataUnit.getString("zizhi");
                                    String sex = dataUnit.getString("sex");
                                    String kinds = dataUnit.getString("class");
                                    String district = dataUnit.getString("district");
                                    long range = dataUnit.getLong("range");

                                    NearbyCoauchSubFragmentCoauchBean coauchBean = new NearbyCoauchSubFragmentCoauchBean(
                                            userId,
                                            photoUrl,
                                            userName,
                                            NearbyFragmentsCommonUtils.parseGenderStr(SearchResultActivity.this, sex),
                                            String.valueOf(range),
                                            NearbyFragmentsCommonUtils.parseCoauchLevel(SearchResultActivity.this, level),
                                            NearbyFragmentsCommonUtils.parseBilliardsKinds(SearchResultActivity.this, kinds),
                                            district);

                                    coauchList.add(coauchBean);
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return coauchList;
    }



    private static String parseRoomTag(JSONArray srcArr){
        StringBuilder tagStr = new StringBuilder();
        final int len = srcArr.length();
        int i;
        for (i = 0; i < len; ++i) {
            try {
                tagStr.append(srcArr.get(i));
                tagStr.append(" ");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return tagStr.toString();
    }

    // TODO: 从我们得到的原始数据当中解析出关于球厅Activity当中的球厅详情字段
    // TODO: 但是现阶段我们是采用json array当中的打折信息(即deals字段)
    // TODO: 在这里我们需要将字段进行一些格式化处理，至少看起来很像ListView
    private static String parseRoomDetailedInfo(JSONArray srcArr){
        StringBuilder infoStr = new StringBuilder();
        final int len = srcArr.length();
        int i;
        for (i = 0; i < len; ++i) {
            try {
                JSONObject subObj = srcArr.getJSONObject(i);
                infoStr.append(subObj.get("description"));
                infoStr.append("\n");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return infoStr.toString();
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

            mLoadMore = true;
            if(mEmptyView.getVisibility() == View.VISIBLE){
                mEmptyView.setVisibility(View.GONE);
            }
            if(mBeforeCount != mAfterCount && mRefresh){
                mStart = mEnd + (mAfterCount - mBeforeCount);
                mEnd += 10 + (mAfterCount - mBeforeCount);
            }else{
                mStart = mEnd + 1;
                mEnd += 10;
            }
            mRefresh = false;
            if(Utils.networkAvaiable(SearchResultActivity.this)){
                mParams.put(HttpConstants.GroupList.STAR_NO,String.valueOf(mStart));
                mParams.put(HttpConstants.GroupList.END_NO,String.valueOf(mEnd));
                requestSearch();
            }
            //TODO:由于目前不需要缓存，所以当没有网络时，就直接toast无网络
            //TODO:如果后面需要缓存，再替换成注释掉的代码
            else{
                mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
            }
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
                mEmptyTypeStr = getString(R.string.billiard_dating) + getString(R.string.info);
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
                mEmptyTypeStr  = getString(R.string.play) + getString(R.string.info);
                break;
            case PublicConstant.SEARCH_GROUP:
                mEmptyTypeStr = getString(R.string.billiard_group) + getString(R.string.info);
                break;
            case PublicConstant.SEARCH_BUSINESS_PLAY:
                mEmptyTypeStr = getString(R.string.business_play) + getString(R.string.info);
                break;
            case PublicConstant.SEARCH_FRIEND:
                mEmptyTypeStr = getString(R.string.no_good_buddy);
                break;
        }
    }

    private void handleResultWhenSuccess(Message msg){
        switch(mSearchType){
            case PublicConstant.SEARCH_NEARBY_MATE:
                Log.d(TAG, "5, then process the list of data we get ");
                mIsListEmpty = mNearbyMateList.isEmpty();
                List<NearbyMateSubFragmentUserBean> mateList = (List<NearbyMateSubFragmentUserBean>) msg.obj;
                Log.d(TAG, "6, the finally list we need to process are : " + mateList.size());
                for(NearbyMateSubFragmentUserBean mateBean : mateList)
                {
                    if (!mNearbyMateList.contains(mateBean))
                    {
                        if(!mIsListEmpty)
                        {
                            Log.d(TAG, "7.1 we have add the list to the mate list start from 0");
                            mNearbyMateList.add(0, mateBean);
                        }else
                        {
                            Log.d(TAG, "7.2 we have add the list in the normal way ");
                            mNearbyMateList.add(mateBean);
                        }
                    }
                }
                if(mNearbyMateList.isEmpty())
                {
                    setEmptyViewVisible();
                }

                break;
            case PublicConstant.SEARCH_NEARBY_DATE:
                mIsListEmpty = mNearbyDatingList.isEmpty();
                List<NearbyDatingSubFragmentDatingBean> datingList = (List<NearbyDatingSubFragmentDatingBean>) msg.obj;
                for(NearbyDatingSubFragmentDatingBean datingBean : datingList)
                {
                    if (!mNearbyDatingList.contains(datingBean))
                    {
                        if(!mIsListEmpty)
                        {
                            mNearbyDatingList.add(0,datingBean);
                        } else
                        {
                            mNearbyDatingList.add(datingBean);
                        }
                    }
                }
                if(mNearbyDatingList.isEmpty())
                {
                    setEmptyViewVisible();
                }

                break;
            case PublicConstant.SEARCH_NEARBY_COACH:

                mIsListEmpty = mNearbyCoauchList.isEmpty();
                List<NearbyCoauchSubFragmentCoauchBean> coauchList = (List<NearbyCoauchSubFragmentCoauchBean>) msg.obj;
                for(NearbyCoauchSubFragmentCoauchBean coauchBean : coauchList)
                {
                    if (!mNearbyCoauchList.contains(coauchBean))
                    {
                        if(!mIsListEmpty)
                        {
                            mNearbyCoauchList.add(0,coauchBean);
                        } else
                        {
                            mNearbyCoauchList.add(coauchBean);
                        }
                    }
                }
                if(mNearbyCoauchList.isEmpty())
                {
                    setEmptyViewVisible();
                }

                break;
            case PublicConstant.SEARCH_NEARBY_ASSITANT:
                mIsListEmpty = mNearbyASList.isEmpty();
                List<NearbyAssistCoauchSubFragmentBean> asList = (List<NearbyAssistCoauchSubFragmentBean>) msg.obj;
                for(NearbyAssistCoauchSubFragmentBean asBean : asList)
                {
                    if (!mNearbyASList.contains(asBean))
                    {
                        if(!mIsListEmpty)
                        {
                            mNearbyASList.add(0,asBean);
                        } else
                        {
                            mNearbyASList.add(asBean);
                        }
                    }
                }
                if(mNearbyASList.isEmpty())
                {
                    setEmptyViewVisible();
                }

                break;
            case PublicConstant.SEARCH_NEARBY_ROOM:
                mIsListEmpty = mNearbyMateList.isEmpty();
                List<NearbyRoomBean> roomList = (List<NearbyRoomBean>) msg.obj;
                for (NearbyRoomBean roomBean : roomList){
                    if (!mNearbyRoomList.contains(roomBean)){
                        if (!mIsListEmpty){
                            mNearbyRoomList.add(0, roomBean);
                        } else{
                            mNearbyRoomList.add(roomBean);
                        }
                    }
                }

                if (mNearbyRoomList.isEmpty())
                {
                    setEmptyViewVisible();
                }

                break;
            case PublicConstant.SEARCH_FAVOR:
                mBeforeCount = mFavorList.size();
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
                mAfterCount = mFavorList.size();
                if(mFavorList.isEmpty()){
                    setEmptyViewVisible();
                }
                break;
            case PublicConstant.SEARCH_JOIN:
                mBeforeCount = mJoinList.size();
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
                mAfterCount = mJoinList.size();
                if(mJoinList.isEmpty()){
                    setEmptyViewVisible();
                }
                break;
            case PublicConstant.SEARCH_PUBLISH:
                mBeforeCount = mPublishedList.size();
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
                mBeforeCount = mPublishedList.size();
                if(mPublishedList.isEmpty()){
                    setEmptyViewVisible();
                }
                break;
            case PublicConstant.SEARCH_PLAY:
                mBeforeCount = mPlayList.size();
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
                mBeforeCount = mPlayList.size();
                if(mPlayList.isEmpty()){
                    setEmptyViewVisible();
                }

                break;
            case PublicConstant.SEARCH_GROUP:
                mBeforeCount = mGroupList.size();
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
                mAfterCount = mGroupList.size();
                if(mGroupList.isEmpty()) {
                    setEmptyViewVisible();
                }
                break;
            case PublicConstant.SEARCH_BUSINESS_PLAY:
                mBeforeCount = mBusinessList.size();
                mIsListEmpty = mBusinessList.isEmpty();
                List<PlayInfo> list = (List<PlayInfo>) msg.obj;
                for(PlayInfo info : list){
                    if (!mBusinessList.contains(info)) {

                        if(!mIsListEmpty && Integer.valueOf(mBusinessList.get(0).getTable_id()) < Integer.valueOf(info.getTable_id())){
                            mBusinessList.add(0,info);
                        }else {
                            mBusinessList.add(info);
                        }
                    }
                }
                mAfterCount = mBusinessList.size();
                if(mBusinessList.isEmpty()){
                    setEmptyViewVisible();
                }
                break;
            case PublicConstant.SEARCH_FRIEND:
                NearbyPeopleInfo searchPeopleInfo = (NearbyPeopleInfo) msg.obj;
                for(NearbyPeopleInfo.SearchPeopleItemInfo info : searchPeopleInfo.mList ){
                    if(!mFriendList.contains(info)){
                        mFriendList.add(info);
                    }
                }
                if (mFriendList.size() > 0)
                    mEmptyView.setVisibility(View.GONE);
                else
                    mEmptyView.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void itemClick(int position){
        Intent intent = null;
        Bundle args = new Bundle();
        switch(mSearchType){
            case PublicConstant.SEARCH_NEARBY_MATE:
                // 不处理(助教的信息只是用于展示，并没有点击之后的处理)
                return;
            case PublicConstant.SEARCH_NEARBY_DATE:
                // 点击之后跳转到约球详情Activity当中
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        Log.d(TAG, "8, while we click on the dating list we get, we start the DatingDetail activity ");
                        NearbyDatingSubFragmentDatingBean bean = mNearbyDatingList.get(position - 1);
                        Bundle args = new Bundle();
                        args.putString(NearbyFragmentsCommonUtils.KEY_DATING_FRAGMENT_PHOTO, bean.getUserPhoto());
                        args.putInt(NearbyFragmentsCommonUtils.KEY_DATING_TABLE_ID, Integer.parseInt(bean.getId()));
                        args.putString(NearbyFragmentsCommonUtils.KEY_DATING_USER_NAME, bean.getUserName());
                        Intent intent = new Intent(SearchResultActivity.this, NearbyDatingDetailActivity.class);
                        intent.putExtras(args);
                        SearchResultActivity.this.startActivity(intent);
                    }
                });
                break;
            case PublicConstant.SEARCH_NEARBY_COACH:
                // 不处理(助教的信息只是用于展示，并没有点击之后的处理)
                return;
            case PublicConstant.SEARCH_NEARBY_ASSITANT:
                // 不处理(助教的信息只是用于展示，并没有点击之后的处理)
                return;
            case PublicConstant.SEARCH_NEARBY_ROOM:
                NearbyRoomBean bean = mNearbyRoomList.get(position - 1);
                args.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PHOTO, bean.getImg_url());
                args.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_NAME, bean.getName());
                args.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_LEVEL, bean.getOverall_rating());
                args.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PRICE, bean.getPrice());
                args.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_ADDRESS, bean.getAddress());
                args.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_DETAILED_INFO, bean.getDetail_info());
                args.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PHONE, bean.getTelephone());
                args.putString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_SHOP_HOURS,bean.getShop_hours());
                intent = new Intent(SearchResultActivity.this, NearbyRoomDetailActivity.class);
//                intent.putExtra(NearbyFragmentsCommonUtils.KEY_BUNDLE_SEARCH_ROOM_FRAGMENT, bundle);

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

                        intent = new Intent(this, NearbyDatingDetailActivity.class);
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

                        intent = new Intent(this, NearbyDatingDetailActivity.class);

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

                        intent = new Intent(this, NearbyDatingDetailActivity.class);
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
            case PublicConstant.SEARCH_FRIEND:
                intent = new Intent(this, RequestAddFriendActivity.class);
                int friendUserId = mFriendList.get(position-1).getUser_id();
                String username = mFriendList.get(position-1).getUsername();
                intent.putExtra(AddPersonFragment.FRIEND_INFO_USER_ID, friendUserId);
                intent.putExtra(AddPersonFragment.FRIEND_INFO_USERNAME, username);
                break;
        }
        intent.putExtras(args);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
    }

}
