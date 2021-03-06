package com.yueqiu.fragment.nearby;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.activity.RequestAddFriendActivity;
import com.yueqiu.activity.SearchResultActivity;
import com.yueqiu.adapter.NearbyCoauchSubFragmentListAdapter;
import com.yueqiu.bean.NearbyCoauchSubFragmentCoauchBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.chatbar.AddPersonFragment;
import com.yueqiu.fragment.nearby.common.NearbyPopBasicClickListener;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.fragment.nearby.common.NearbyParamsPreference;
import com.yueqiu.fragment.nearby.common.NearbySubFragmentConstants;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by scguo on 14/12/30.
 * <p/>
 * 用于SearchActivity当中的教练子Fragment的实现
 */
@SuppressLint("ValidFragment")
public class BilliardsNearbyCoachFragment extends Fragment implements AdapterView.OnItemClickListener
{
    private static final String TAG = "BilliardsNearbyCoauchFragment";

    private static final String FRAGMENT_TAG = "BilliardsNearbyAssistCoauchFragment";

    // 这是用于整个BilliardsSearchCoauchFragment当中的layout view
    private View mView;
    private Button mBtnAbility, mBtnKinds;

    private PullToRefreshListView mCoauchListView;

    private Context mContext;
    private SearchView mSearchView;

    @SuppressLint("ValidFragment")
    public BilliardsNearbyCoachFragment()
    {
    }

    private static final String PARAMS_KEY = "BilliardsNearbyCoauchFragment";

    public static BilliardsNearbyCoachFragment newInstance(Context context, String params)
    {
//        mContext = context;
        BilliardsNearbyCoachFragment instance = new BilliardsNearbyCoachFragment();

        Bundle args = new Bundle();
        args.putString(PARAMS_KEY, params);
        instance.setArguments(args);

        return instance;
    }
    private boolean mNetworkAvailable;

    private BackgroundWorkerThread mWorker;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mNetworkAvailable = Utils.networkAvaiable(mContext);

    }

    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;
    private boolean mIsListEmpty;

    private List<NearbyCoauchSubFragmentCoauchBean> mCoauchList = new ArrayList<NearbyCoauchSubFragmentCoauchBean>();
    private List<NearbyCoauchSubFragmentCoauchBean> mCachedList = new ArrayList<NearbyCoauchSubFragmentCoauchBean>();

    private NearbyCoauchSubFragmentListAdapter mCoauchListAdapter;
    private NearbyPopBasicClickListener mClickListener;

    private NearbyFragmentsCommonUtils.ControlPopupWindowCallback mCallback;

    private int mRequestFlag;
    private String mArgs;

    private float mLat, mLng;
    private double mGetLat,mGetLng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_nearby_coauch_layout, container, false);
        setHasOptionsMenu(true);

        NearbyFragmentsCommonUtils commonUtils = new NearbyFragmentsCommonUtils(mContext);
        commonUtils.initViewPager(mContext, mView);

        mClickListener = new NearbyPopBasicClickListener(mContext,mUIEventsHandler,sParamsPreference);
        (mBtnAbility = (Button) mView.findViewById(R.id.btn_coauch_ability)).setOnClickListener(mClickListener);
        (mBtnKinds = (Button) mView.findViewById(R.id.btn_coauch_kinds)).setOnClickListener(mClickListener);
        mEmptyView = new TextView(getActivity());

        mCallback = mClickListener;

        mCoauchListView = (PullToRefreshListView) mView.findViewById(R.id.search_coauch_subfragment_list);
        mCoauchListView.setMode(PullToRefreshBase.Mode.BOTH);
        mCoauchListView.setOnRefreshListener(mOnRefreshListener);
        mCoauchListView.setOnItemClickListener(this);

        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        mPreTextView = (TextView) mView.findViewById(R.id.pre_text);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);

        Bundle args = getArguments();
        mArgs = args.getString(NearbySubFragmentConstants.BILLIARD_SEARCH_TAB_NAME);

        // TODO: 这里加载的是测试数据,暂时还不能删除这个方法，因为我们还要查看总的UI加载效果
//        initListViewTestData();
        mCoauchListAdapter = new NearbyCoauchSubFragmentListAdapter(mContext, (ArrayList<NearbyCoauchSubFragmentCoauchBean>) mCoauchList);
        Log.d(TAG, " the source list content are : " + mCoauchList.size());
        mCoauchListView.setAdapter(mCoauchListAdapter);
        mCoauchListAdapter.notifyDataSetChanged();

        if (null != savedInstanceState)
        {
            mRefresh = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_REFRESH);
            mLoadMore = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_LOAD_MORE);
            mIsSavedInstance = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_INSTANCE);

            mCachedList = savedInstanceState.getParcelableArrayList(NearbyFragmentsCommonUtils.KEY_SAVED_LISTVIEW);
            mUIEventsHandler.obtainMessage(PublicConstant.USE_CACHE, mCachedList);

        }

        mLoadMore = false;
        mRefresh = false;


        return mView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(NearbyFragmentsCommonUtils.KEY_SAVED_LISTVIEW, (ArrayList<? extends android.os.Parcelable>) mCoauchList);
        outState.putBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_LOAD_MORE, mLoadMore);
        outState.putBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_REFRESH, mRefresh);
        outState.putBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_INSTANCE, true);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(mReceiver,filter);

        if(mSearchView != null){
            mSearchView.clearFocus();
        }
        mWorker = new BackgroundWorkerThread(mStartNum, mEndNum);
        if (mWorker.getState() == Thread.State.NEW)
        {
            mWorker.start();
        }
        if ( !Utils.networkAvaiable(mContext))
        {
            mUIEventsHandler.sendEmptyMessage(NO_NETWORK);
        }
    }

    @Override
    public void onPause()
    {
        if (null != mWorker)
        {
            mWorker.interrupt();
            mWorker = null;
        }
        mCallback.closePopupWindow();
        super.onPause();
    }

    @Override
    public void onStop()
    {
        mCallback.closePopupWindow();
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        // 将所有的筛选参数置空
        sParamsPreference.setCouchClazz(mContext, "");
        sParamsPreference.setCouchLevel(mContext, "");

        super.onDestroy();
    }

    private static NearbyParamsPreference sParamsPreference = NearbyParamsPreference.getInstance();

    private void retrieveInitialCoauchInfo(final float lati, final float lng, final String clazzRequest, String levelRequest, final int startNo, final int endNo)
    {
        if (!Utils.networkAvaiable(getActivity()))
        {
            mUIEventsHandler.obtainMessage(STATE_FETCH_DATA_FAILED,
                    mContext.getResources().getString(R.string.network_not_available)).sendToTarget();
            return;
        }
        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        // 我们直接在请求方法内部进行关于请求参数的可行性判断
        if (! TextUtils.isEmpty(clazzRequest))
        {
            requestParams.put("class", clazzRequest);
        }
        if (! TextUtils.isEmpty(levelRequest))
        {
            requestParams.put("zizhi", levelRequest);
        }

        // 将经纬度作为参数添加上
        requestParams.put("lat", String.valueOf(lati));
        requestParams.put("lng", String.valueOf(lng));
        requestParams.put("start_no", startNo + "");
        requestParams.put("end_no", endNo + "");

        Log.d("wy","coach request param ->" + requestParams);

        final List<NearbyCoauchSubFragmentCoauchBean> cacheCoauchList = new ArrayList<NearbyCoauchSubFragmentCoauchBean>();

        mUIEventsHandler.sendEmptyMessage(SET_PULLREFRESH_DISABLE);
        
        HttpUtil.requestHttp(HttpConstants.NearbyCoauch.URL, requestParams, HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","coach response ->" + response);
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
                                    if(len < 1) {
                                        mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
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
                                                    NearbyFragmentsCommonUtils.parseGenderStr(mContext, sex),
                                                    String.valueOf(range),
                                                    NearbyFragmentsCommonUtils.parseCoachZizhi(mContext, level),
                                                    NearbyFragmentsCommonUtils.parseBilliardsKinds(mContext, kinds),
                                                    district);

                                            cacheCoauchList.add(coauchBean);
                                        }

                                        if (cacheCoauchList.isEmpty()) {
                                            mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                        } else {
                                            mUIEventsHandler.obtainMessage(STATE_FETCH_DATA_SUCCESS, cacheCoauchList).sendToTarget();
//                                mUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                                        }
                                    }
                                }else{
                                    mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                }

                            } else
                            {
//                            mUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                                mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }
                        } else if (status == HttpConstants.ResponseCode.TIME_OUT)
                        {
                            mUIEventsHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
//                        mUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                        } else if (status == HttpConstants.ResponseCode.NO_RESULT)
                        {
                            mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
//                        mUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                        } else
                        {
                            Message errorMsg = mUIEventsHandler.obtainMessage(PublicConstant.REQUEST_ERROR);
                            Bundle errorData = new Bundle();
                            String errorStr = response.getString("msg");
                            Log.d(TAG, "inside the couach list, and the error info we get are : " + errorStr);
                            if (! TextUtils.isEmpty(errorStr))
                            {
                                errorData.putString(KEY_REQUEST_ERROR_MSG, errorStr);
                            }
                            errorMsg.setData(errorData);
                            mUIEventsHandler.sendMessage(errorMsg);
//                        mUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                        }
                    } else
                    {
                        mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
//                    mUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                    mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
//                mUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                    Log.d(TAG, " exception happened while we parsing the json object we retrieved, and the reason are : " + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });

    }

    private static final String KEY_REQUEST_ERROR_MSG = "requestErrorMsg";
    private static final String KEY_REQUEST_START_NUM = "requestStartNum";
    private static final String KEY_REQUEST_END_NUM = "requestEndNum";

    private static final int NO_NETWORK = 1 << 9;

    private static final int DATA_HAS_BEEN_UPDATED = 1 << 8;

    private static final String BACKGROUND_WORKER_NAME = "BackgroundWorkerThread";

    private static final int STATE_FETCH_DATA_FAILED = 1 << 4;
    private static final int STATE_FETCH_DATA_SUCCESS = 1 << 5;

    private static final int RETRIEVE_ALL_COAUCH_INFO = 1 << 1;

    // 由于这些常量值被定义到同一个地方进行使用，所以我们将教练Fragment当中的常量值定义为从40开始
    public static final int RETRIEVE_COAUCH_WITH_LEVEL_FILTERED = 40 << 2;
    public static final int RETRIEVE_COAUCH_WITH_CLASS_FILTERED = 40 << 3;

    private static final int UI_SHOW_PROGRESS = 1 << 6;

    private static final int SET_PULLREFRESH_DISABLE = 42;
    public static final int GET_LOCATION = 43;
    public static final int LOCATION_HAS_GOT = 44;

    private  Handler mUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(mCoauchListView.getMode() == PullToRefreshBase.Mode.DISABLED){
                mCoauchListView.setMode(PullToRefreshBase.Mode.BOTH);
            }
            if (mCoauchListView.isRefreshing())
            {
                mCoauchListView.onRefreshComplete();
            }
            switch (msg.what)
            {
                case UI_SHOW_PROGRESS:
                    showProgress();
                    if(mEmptyView.getVisibility() == View.VISIBLE){
                        mCoauchListView.setEmptyView(null);
                        mEmptyView.setVisibility(View.GONE);
                    }
                    Log.d(TAG, " start showing the progress bar in the Coauch fragment ");
                    break;
//                case UI_HIDE_PROGRESS:
//                    mCoauchListAdapter.notifyDataSetChanged();
//                    hideProgress();
//                    if (mCoauchListView.isRefreshing())
//                    {
//                        mCoauchListView.onRefreshComplete();
//                    }
//                    Log.d(TAG, " hide the progress bar that in the coauch fragment ");
//                    break;

                case STATE_FETCH_DATA_FAILED:
                    String reasonDesc = (String) msg.obj;
                    Log.d(TAG, " fail to get the data we need, and the detailed reason for that are : " + reasonDesc);
                    Toast.makeText(mContext, reasonDesc, Toast.LENGTH_SHORT).show();
                    if(mCoauchList.isEmpty())
                        setEmptyViewVisible();
                    hideProgress();
                    break;
                case PublicConstant.USE_CACHE:
                    ArrayList<NearbyCoauchSubFragmentCoauchBean> cachedList = (ArrayList<NearbyCoauchSubFragmentCoauchBean>) msg.obj;
                    if (cachedList.size() > 0)
                    {
                        // 首先将我们的EmptyView隐藏掉
                        setEmptyViewGone();
                    }
                    mCoauchList.addAll(cachedList);
                    if(mCoauchList.isEmpty())
                        setEmptyViewVisible();
                    break;
                case STATE_FETCH_DATA_SUCCESS:
                    // 首先将我们的Empty隐藏掉，每次重新获得数据之后都需要先将EmptyView隐藏掉，然后需要重新判断
                    setEmptyViewGone();
                    hideProgress();
                    mBeforeCount = mCoauchList.size();
                    mIsListEmpty = mCoauchList.isEmpty();
                    List<NearbyCoauchSubFragmentCoauchBean> coauchList = (ArrayList<NearbyCoauchSubFragmentCoauchBean>) msg.obj;
                    for (NearbyCoauchSubFragmentCoauchBean bean : coauchList)
                    {
                        if (! mCoauchList.contains(bean))
                        {
                            if(!mIsListEmpty && Integer.valueOf(mCoauchList.get(0).getId()) < Integer.valueOf(bean.getId())){
                                mCoauchList.add(0,bean);
                            }else {
                                mCoauchList.add(bean);
                            }
//                            if (mRefresh && !mIsListEmpty)
//                            {
//                                mCoauchList.add(0, bean);
//                            } else
//                            {
//                                if (mIsSavedInstance)
//                                {
//                                    mCoauchList.add(0, bean);
//                                } else
//                                {
//                                    mCoauchList.add(bean);
//                                }
//                            }
                        }else{
                            int index = mCoauchList.indexOf(bean);
                            if(!bean.getUserPhoto().equals(mCoauchList.get(index).getUserPhoto()) ||
                                    !bean.getUserName().equals(mCoauchList.get(index))){
                                mCoauchList.remove(index);
                                mCoauchList.add(index,bean);
                            }
                        }
                    }
                    Collections.sort(mCoauchList,new DescComparator());
                    mAfterCount = mCoauchList.size();
                    // TODO: 在这里进行一下更新数据库的操作
                    if (mCoauchList.isEmpty())
                    {
                        Log.d(TAG, " inside the coauchFragment mUIEventsHandler --> we start to load the emptyView");
                        setEmptyViewVisible();
                    } else
                    {
                        if (mRefresh)
                        {
                            if (mAfterCount == mBeforeCount)
                            {
                                Utils.showToast(mContext, mContext.getString(R.string.no_newer_info));
                            } else
                            {
                                Utils.showToast(mContext, mContext.getString(R.string.have_already_update_info, mAfterCount - mBeforeCount));
                            }
                        }
                    }
                    break;

                case RETRIEVE_COAUCH_WITH_LEVEL_FILTERED:
                    String level = (String) msg.obj;
                    if (null != mWorker)
                    {
                        Log.d(TAG, " inside the CoauchFragment UIEventsHandler --> and the level info we get are : " + level);
                        mWorker.fetchDataWithLevelFiltered(level);
                    }
                    break;

                case RETRIEVE_COAUCH_WITH_CLASS_FILTERED:
                    String clazz = (String) msg.obj;
                    if (mWorker != null)
                    {
                        mWorker.fetchDataWithClazzFiltered(clazz);
                    }

                    break;

                case DATA_HAS_BEEN_UPDATED:
                    mCoauchListAdapter.notifyDataSetChanged();
                    Log.d(TAG, " the adapter has been updated ");
                    break;

                case NO_NETWORK:
                    hideProgress();
                    if (mCoauchList.isEmpty())
                    {
                        setEmptyViewVisible();
                        mEmptyView.setText(mContext.getString(R.string.network_not_available));
                    }else {
                        Utils.showToast(mContext, mContext.getString(R.string.network_not_available));
                    }
                    break;
//                case PublicConstant.TIME_OUT:
//                    // 超时之后的处理策略
//                    Utils.showToast(mContext, mContext.getString(R.string.http_request_time_out));
//                    if (mCoauchList.isEmpty())
//                    {
//                        setEmptyViewVisible();
//                    }
//                    hideProgress();
//                    break;

                case PublicConstant.NO_RESULT:
                    if (mCoauchList.isEmpty())
                    {
                        setEmptyViewVisible();
                    } else
                    {
                        if (mLoadMore)
                        {
                            Utils.showToast(mContext, mContext.getString(R.string.no_more_info, mContext.getString(R.string.nearby_billiard_coauch_str)));
                        }
                    }
                    hideProgress();
                    break;

                case PublicConstant.REQUEST_ERROR:
                    Bundle errorData = msg.getData();
                    String errorStr = errorData.getString(KEY_REQUEST_ERROR_MSG);


                    if (mCoauchList.isEmpty())
                    {
                        setEmptyViewVisible();
                        if (! TextUtils.isEmpty(errorStr))
                        {
                            mEmptyView.setTag(errorStr);
                        } else
                        {
                            mEmptyView.setText(mContext.getString(R.string.http_request_error));
                        }
                    }else{
                        if (! TextUtils.isEmpty(errorStr))
                        {
                            Utils.showToast(mContext, errorStr);
                        } else
                        {
                            Utils.showToast(mContext, mContext.getString(R.string.http_request_error));
                        }
                    }
                    hideProgress();
                    break;
                case SET_PULLREFRESH_DISABLE:
                    mCoauchListView.setMode(PullToRefreshBase.Mode.DISABLED);
                    break;
                case GET_LOCATION:
                    showProgress();
                    mCoauchListView.setMode(PullToRefreshBase.Mode.DISABLED);
                    if (mCoauchListView.getVisibility() == View.VISIBLE)
                    {
                        mCoauchListView.setEmptyView(null);
                        mEmptyView.setVisibility(View.GONE);
                    }
                    mRequestFlag = msg.arg1;
                    mArgs = (String) msg.obj;

                    getLocation();
                    break;
                case LOCATION_HAS_GOT:
                    Bundle args = (Bundle) msg.obj;
                    mLat = args.getFloat("lat");
                    mLng = args.getFloat("lng");
                    switch (mRequestFlag)
                    {
                        case RETRIEVE_ALL_COAUCH_INFO:
                            String cacheClazz = sParamsPreference.getCouchClazz(mContext);
                            String cacheLevel = sParamsPreference.getCouchLevel(mContext);
                            retrieveInitialCoauchInfo(mLat, mLng, cacheClazz, cacheLevel, mStartNum, mEndNum);
                            break;
                        case RETRIEVE_COAUCH_WITH_CLASS_FILTERED:
                            if (! mCoauchList.isEmpty())
                            {
                                mCoauchList.clear();
                                mUIEventsHandler.sendEmptyMessage(DATA_HAS_BEEN_UPDATED);
                            }
                            String clazzL = mArgs;
                            mUIEventsHandler.sendEmptyMessage(UI_SHOW_PROGRESS);
                            Log.d(TAG, " inside the BackgroundThread --> the clazz string we get in the CouachFragment are : " + clazzL);
                            String clazzCacheLevel = sParamsPreference.getCouchLevel(mContext);
                            // 我们每次的筛选都要从零开始请求最新的数据
                            retrieveInitialCoauchInfo(mLat, mLng, clazzL, clazzCacheLevel, 0, 9);
                            break;
                        case RETRIEVE_COAUCH_WITH_LEVEL_FILTERED:
                            if (! mCoauchList.isEmpty())
                            {
                                mCoauchList.clear();
                                mUIEventsHandler.sendEmptyMessage(DATA_HAS_BEEN_UPDATED);
                            }
                            String levelL = mArgs;
                            Log.d(TAG, " inside the BackgroundThread --> the level string we get in the CoauchFragment are : " + levelL);
                            String levelCacheClazz = sParamsPreference.getCouchClazz(mContext);
                            // 同样的道理，我们筛选的数据请求都要从零开始重新请求
                            retrieveInitialCoauchInfo(mLat, mLng, levelCacheClazz, levelL, 0, 9);
                            break;
                    }
                    break;

            }
            mCoauchListAdapter.notifyDataSetChanged();
            if(mLoadMore && !mCoauchList.isEmpty())
            {
                mCoauchListView.getRefreshableView().setSelection(mCurrentPos - 1 );
            }
        }
    };



    private TextView mEmptyView;
    // 我们通过将disable的值设置为false来进行加载EmptyView
    // 通过将disable的值设置为true来隐藏emptyView
    private void setEmptyViewVisible()
    {
        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mEmptyView.setTextColor(mContext.getResources().getColor(R.color.md__defaultBackground));
        mEmptyView.setText(mContext.getString(R.string.search_activity_subfragment_empty_tv_str));
        mCoauchListView.setEmptyView(mEmptyView);
    }

    private void setEmptyViewGone()
    {
        if (null != mEmptyView)
        {
            mEmptyView.setVisibility(View.GONE);
            mCoauchListView.setEmptyView(null);
        }
    }

    private void showProgress()
    {
        mPreProgress.setVisibility(View.VISIBLE);
        if(mCoauchList.isEmpty()) {
            mPreTextView.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgress()
    {
        mPreProgress.setVisibility(View.GONE);
        mPreTextView.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getActivity(), RequestAddFriendActivity.class);
        int friendUserId = Integer.valueOf(mCoauchList.get(i-1).getId());
        String username = mCoauchList.get(i-1).getUserName();
        intent.putExtra(AddPersonFragment.FRIEND_INFO_USER_ID, friendUserId);
        intent.putExtra(AddPersonFragment.FRIEND_INFO_USERNAME, username);
        startActivity(intent);
    }

    private class BackgroundWorkerThread extends HandlerThread
    {
        private final int mInitStartNO, mInitEndNO;
        public BackgroundWorkerThread(int startNum, int endNum)
        {
            super(BACKGROUND_WORKER_NAME, Process.THREAD_PRIORITY_BACKGROUND);
            this.mInitStartNO = startNum;
            this.mInitEndNO = endNum;
        }

        // 参照MateFragment当中的理解
        private Handler mWorkerHandler = new Handler();

        @Override
        protected void onLooperPrepared()
        {
            super.onLooperPrepared();
            mWorkerHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    super.handleMessage(msg);
                    switch (msg.what) {
//                        case RETRIEVE_ALL_COAUCH_INFO:
//                            // 通知UIHandler开始显示Dialog
//                            mUIEventsHandler.sendEmptyMessage(UI_SHOW_PROGRESS);
//                            Bundle numData = msg.getData();
//                            final int startNum = numData.getInt(KEY_REQUEST_START_NUM);
//                            final int endNum = numData.getInt(KEY_REQUEST_END_NUM);
//                            String cacheClazz = sParamsPreference.getCouchClazz(mContext);
//                            String cacheLevel = sParamsPreference.getCouchLevel(mContext);
//                            retrieveInitialCoauchInfo(cacheClazz, cacheLevel, startNum, endNum);
//
//                            break;
//                        case RETRIEVE_COAUCH_WITH_CLASS_FILTERED:
//                            String clazz = (String) msg.obj;
//                            mUIEventsHandler.sendEmptyMessage(UI_SHOW_PROGRESS);
//                            Log.d(TAG, " inside the BackgroundThread --> the clazz string we get in the CouachFragment are : " + clazz);
//                            String clazzCacheLevel = sParamsPreference.getCouchLevel(mContext);
//                            // 我们每次的筛选都要从零开始请求最新的数据
//                            retrieveInitialCoauchInfo(clazz, clazzCacheLevel, 0, 9);
//
//                            break;
//                        case RETRIEVE_COAUCH_WITH_LEVEL_FILTERED:
//                            mUIEventsHandler.sendEmptyMessage(UI_SHOW_PROGRESS);
//                            String level = (String) msg.obj;
//                            Log.d(TAG, " inside the BackgroundThread --> the level string we get in the CoauchFragment are : " + level);
//                            String levelCacheClazz = sParamsPreference.getCouchClazz(mContext);
//                            // 同样的道理，我们筛选的数据请求都要从零开始重新请求
//                            retrieveInitialCoauchInfo(levelCacheClazz, level, 0, 9);


//                        case RETRIEVE_ALL_COAUCH_INFO:
//                            // 通知UIHandler开始显示Dialog
//                            mUIEventsHandler.sendEmptyMessage(UI_SHOW_PROGRESS);
//                            Bundle numData = msg.getData();
//                            final int startNum = numData.getInt(KEY_REQUEST_START_NUM);
//                            final int endNum = numData.getInt(KEY_REQUEST_END_NUM);
//                            String cacheClazz = sParamsPreference.getCouchClazz(mContext);
//                            String cacheLevel = sParamsPreference.getCouchLevel(mContext);
//                            retrieveInitialCoauchInfo(cacheClazz, cacheLevel, startNum, endNum);
//
//                            break;
//                        case RETRIEVE_COAUCH_WITH_CLASS_FILTERED:
//                            String clazz = (String) msg.obj;
//                            mUIEventsHandler.sendEmptyMessage(UI_SHOW_PROGRESS);
//                            Log.d(TAG, " inside the BackgroundThread --> the clazz string we get in the CouachFragment are : " + clazz);
//                            String clazzCacheLevel = sParamsPreference.getCouchLevel(mContext);
//                            // 我们每次的筛选都要从零开始请求最新的数据
//                            retrieveInitialCoauchInfo(clazz, clazzCacheLevel, 0, 9);
//
//                            break;
//                        case RETRIEVE_COAUCH_WITH_LEVEL_FILTERED:
//                            String level = (String) msg.obj;
//                            Log.d(TAG, " inside the BackgroundThread --> the level string we get in the CoauchFragment are : " + level);
//                            String levelCacheClazz = sParamsPreference.getCouchClazz(mContext);
//                            // 同样的道理，我们筛选的数据请求都要从零开始重新请求
//                            retrieveInitialCoauchInfo(levelCacheClazz, level, 0, 9);
//
//                            break;
                        default:
                            break;
                    }
                }
            };
            if (Utils.networkAvaiable(mContext))
            {
                // 初始请求，肯定是请求最新的，即从第0条开始请求就可以了
                fetchAllData(mInitStartNO, mInitEndNO);
            }
        }

        public void fetchAllData(final int startNum, final int endNum)
        {
            if (null != mWorkerHandler)
            {
//                Message requestMsg = mWorkerHandler.obtainMessage(RETRIEVE_ALL_COAUCH_INFO);
//                Bundle requestData = new Bundle();
//                requestData.putInt(KEY_REQUEST_START_NUM, startNum);
//                requestData.putInt(KEY_REQUEST_END_NUM, endNum);
//                requestMsg.setData(requestData);
//                mWorkerHandler.sendMessage(requestMsg);
                mUIEventsHandler.obtainMessage(
                        GET_LOCATION,
                        RETRIEVE_ALL_COAUCH_INFO,
                        0).sendToTarget();
            }
        }

        public void fetchDataWithClazzFiltered(String clazz)
        {
            if (! TextUtils.isEmpty(clazz) && null != mWorkerHandler)
            {
                mWorkerHandler.obtainMessage(RETRIEVE_COAUCH_WITH_CLASS_FILTERED, clazz).sendToTarget();
            }
        }

        public void fetchDataWithLevelFiltered(String level)
        {
            if (! TextUtils.isEmpty(level) && null != mWorkerHandler)
            {
                mWorkerHandler.obtainMessage(RETRIEVE_COAUCH_WITH_LEVEL_FILTERED, level).sendToTarget();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        super.onCreateOptionsMenu(menu, inflater);
        mSearchView =(SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //TODO:将搜索结果传到SearResultActivity，在SearchResultActivity中进行搜索
                if(Utils.networkAvaiable(mContext)) {
                    Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                    Bundle args = new Bundle();
                    args.putInt(PublicConstant.SEARCH_TYPE, PublicConstant.SEARCH_NEARBY_COACH);
                    args.putString(PublicConstant.SEARCH_KEYWORD, query);
                    intent.putExtras(args);
                    startActivity(intent);
                }else{
                    Utils.showToast(mContext,getString(R.string.network_not_available));
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private boolean mLoadMore;
    private boolean mRefresh;
    private boolean mIsSavedInstance;

    private int mCurrentPos;
    private int mBeforeCount;
    private int mAfterCount;

    private int mStartNum = 0;
    private int mEndNum = 9;

    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener = new PullToRefreshBase.OnRefreshListener2<ListView>()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            String label = NearbyFragmentsCommonUtils.getLastedTime(mContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            if (Utils.networkAvaiable(mContext))
            {
                mLoadMore = false;
                mRefresh = true;
                if (null != mWorker)
                {
                    // 每一次的下拉刷新我们都是要从0开始请求最新的数据
                    mWorker.fetchAllData(0, 9);
                }
            } else
            {
                mUIEventsHandler.sendEmptyMessage(STATE_FETCH_DATA_FAILED);
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            String label = NearbyFragmentsCommonUtils.getLastedTime(mContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            mLoadMore = true;
            mCurrentPos = mCoauchList.size();
            if (mBeforeCount != mAfterCount && mRefresh)
            {
                mStartNum = mEndNum + (mAfterCount - mBeforeCount);
                mEndNum += 10 + (mAfterCount - mBeforeCount);
            } else
            {
                mStartNum = mEndNum + 1;
                mEndNum += 10;
            }
            mRefresh = false;
            if (Utils.networkAvaiable(mContext))
            {
                if (null != mWorker)
                {
                    mWorker.fetchAllData(mStartNum, mEndNum);
                }

            } else
            {
                mUIEventsHandler.sendEmptyMessage(NO_NETWORK);
            }
        }
    };

    private LocationManagerProxy mLocationManagerProxy;

    /**
     * 初始化定位,用高德SDK获取经纬度，准确率貌似更高点，
     * 之后可能会加功能，会用到高德的SDK
     */
    private void getLocation() {

        mLocationManagerProxy = LocationManagerProxy.getInstance(getActivity());

        //此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        //注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
        //在定位结束后，在合适的生命周期调用destroy()方法
        //其中如果间隔时间为-1，则定位只定一次
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15,mAmapLocationListener);
        mLocationManagerProxy.setGpsEnable(false);
        CountDownTimer timer = new CountDownTimer(8100,100) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if(mGetLng == 0 && mGetLat == 0){
                    mLocationManagerProxy.removeUpdates(mAmapLocationListener);

                    sParamsPreference.ensurePreference(mContext);
                    sParamsPreference.setRoomLati(mContext, (float) mGetLat);
                    sParamsPreference.setRoomLongi(mContext, (float) mGetLng);

                    Bundle args = new Bundle();
                    args.putFloat("lat", (float) mGetLat);
                    args.putFloat("lng", (float) mGetLng);
                    mUIEventsHandler.obtainMessage(LOCATION_HAS_GOT, args).sendToTarget();
                }
            }
        };
        timer.start();
    }

    private AMapLocationListener mAmapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if(aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0){
                //获取位置信息
                mGetLat = aMapLocation.getLatitude();
                mGetLng = aMapLocation.getLongitude();

                // 我们此时可以将我们获取到的当前用户的位置信息用来进行球厅的位置筛选操作
                sParamsPreference.ensurePreference(mContext);
                sParamsPreference.setRoomLati(mContext, (float) mGetLat);
                sParamsPreference.setRoomLongi(mContext, (float) mGetLng);

                Bundle args = new Bundle();
                args.putFloat("lat", (float) mGetLat);
                args.putFloat("lng", (float) mGetLng);
                mUIEventsHandler.obtainMessage(LOCATION_HAS_GOT,args).sendToTarget();

            }
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };



    /**
     * 由于服务器是按降序排序，但是从网络获取到的json却是升序，所以重新排序一下
     */
    private class DescComparator implements Comparator<NearbyCoauchSubFragmentCoauchBean> {

        @Override
        public int compare(NearbyCoauchSubFragmentCoauchBean lhs, NearbyCoauchSubFragmentCoauchBean rhs) {
            int lhsUserId = Integer.valueOf(lhs.getId());
            int rhsUserId = Integer.valueOf(rhs.getId());
            return lhsUserId > rhsUserId ? -1 : 1;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                if(Utils.networkAvaiable(getActivity())) {
                    NearbyFragmentsCommonUtils commonUtils = new NearbyFragmentsCommonUtils(mContext);
                    commonUtils.initViewPager(mContext, mView);
                    if (mCoauchList.isEmpty()) {
                        mLoadMore = false;
                        mRefresh = false;
                        if (null != mWorker) {
                            mWorker.fetchAllData(0, 9);
                        }
                    }
                }
            }
        }
    };

}






































































































































