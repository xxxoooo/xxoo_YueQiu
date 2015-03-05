package com.yueqiu.fragment.nearby;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.adapter.NearbyAssistCoauchSubFragmentListAdapter;
import com.yueqiu.bean.NearbyAssistCoauchSubFragmentBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.fragment.nearby.common.NearbyParamsPreference;
import com.yueqiu.fragment.nearby.common.NearbyPopBasicClickListener;
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by scguo on 14/12/30.
 * <p/>
 * 用于NearbyActivity当中的助教子Fragment的实现
 */
@SuppressLint("ValidFragment")
public class BilliardsNearbyAssistCoauchFragment extends Fragment
{
    private final String TAG = "BilliardsNearbyAssistCoauchFragment";
    public final String BILLIARDS_Nearby_ASSIST_COAUCH_FRAGMENT_TAB_NAME = "BilliardsNearbyAssistCoauchFragment";

    // 用于展示助教信息的ListView
    private PullToRefreshListView mListView;
    private Context mContext;

    @SuppressLint("ValidFragment")
    public BilliardsNearbyAssistCoauchFragment()
    {
    }

    private static final String KEY_BILLIARDS_NEARBY_PARENT_FRAGMENT = "keyBilliardsNearbyAssistCoauchFragment";

    public static BilliardsNearbyAssistCoauchFragment newInstance(Context context, String params)
    {
        // TODO: 以下获取Context实例的方法是完全不符合Fragment的设计理念的
//        mContext = context;
        BilliardsNearbyAssistCoauchFragment instance = new BilliardsNearbyAssistCoauchFragment();
        Bundle args = new Bundle();
        args.putString(KEY_BILLIARDS_NEARBY_PARENT_FRAGMENT, params);

        return instance;
    }
    private boolean mNetworkAvailable;

    private BackgroundWorkerHandler mWorker;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mNetworkAvailable = Utils.networkAvaiable(mContext);
    }

    private View mView;

    private Button mBtnDistance, mBtnCost, mBtnKinds, mBtnLevel;
    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;

    private NearbyAssistCoauchSubFragmentListAdapter mAssistCoauchListAdapter;

    private ArrayList<NearbyAssistCoauchSubFragmentBean> mAssistCoauchList = new ArrayList<NearbyAssistCoauchSubFragmentBean>();
    private ArrayList<NearbyAssistCoauchSubFragmentBean> mCachedList = new ArrayList<NearbyAssistCoauchSubFragmentBean>();
    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//    private List<NearbyAssistCoauchSubFragmentBean> mUpdateList = new ArrayList<NearbyAssistCoauchSubFragmentBean>();
//    private List<NearbyAssistCoauchSubFragmentBean> mInsertList = new ArrayList<NearbyAssistCoauchSubFragmentBean>();
//    private List<NearbyAssistCoauchSubFragmentBean> mDbList = new ArrayList<NearbyAssistCoauchSubFragmentBean>();
    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

    private NearbyPopBasicClickListener mClickListener;

    private NearbyFragmentsCommonUtils.ControlPopupWindowCallback mCallback;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        mView = inflater.inflate(R.layout.fragment_nearby_assistcoauch_layout, container, false);

        NearbyFragmentsCommonUtils commonUtils = new NearbyFragmentsCommonUtils(getActivity());
        commonUtils.initViewPager(getActivity(),mView);

        mClickListener = new NearbyPopBasicClickListener(mContext, mUIEventsHandler, sParamsPreference);
        (mBtnDistance = (Button) mView.findViewById(R.id.btn_assistcoauch_distance)).setOnClickListener(mClickListener);
        (mBtnCost = (Button) mView.findViewById(R.id.btn_assistcoauch_cost)).setOnClickListener(mClickListener);
        (mBtnKinds = (Button) mView.findViewById(R.id.btn_assistcoauch_kinds)).setOnClickListener(mClickListener);
        (mBtnLevel = (Button) mView.findViewById(R.id.btn_assistcoauch_level)).setOnClickListener(mClickListener);
        mEmptyView = new TextView(getActivity());

        mCallback = mClickListener;

        mListView = (PullToRefreshListView) mView.findViewById(R.id.search_assistcoauch_subfragment_listview);
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(mOnRefreshListener);


        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        mPreTextView = (TextView) mView.findViewById(R.id.pre_text);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);


        // TODO: 以下加载的是测试数据,但是我们目前还不能删除这个方法，因为我们还需要这些测试数据来查看整体的UI加载效果
//        initTestData();

        mAssistCoauchListAdapter = new NearbyAssistCoauchSubFragmentListAdapter(mContext, mAssistCoauchList);
        mListView.setAdapter(mAssistCoauchListAdapter);

        if (null != savedInstanceState)
        {
            mRefresh = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_REFRESH);
            mLoadMore = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_LOAD_MORE);
            mIsSavedInstance = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_INSTANCE);
            mCachedList = savedInstanceState.getParcelableArrayList(NearbyFragmentsCommonUtils.KEY_SAVED_LISTVIEW);
            mUIEventsHandler.obtainMessage(PublicConstant.USE_CACHE, mCachedList).sendToTarget();
        }

        mWorker = new BackgroundWorkerHandler(mStartNum, mEndNum);
        mLoadMore = false;
        mRefresh = false;
        if (mWorker.getState() == Thread.State.NEW)
        {
            mWorker.start();
        }
        if (! Utils.networkAvaiable(getActivity()))
        {
            mUIEventsHandler.sendEmptyMessage(NETWORK_UNAVAILABLE);
        }
        return mView;
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(NearbyFragmentsCommonUtils.KEY_SAVED_LISTVIEW, mAssistCoauchList);
        outState.putBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_LOAD_MORE, mLoadMore);
        outState.putBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_REFRESH, mRefresh);
        outState.putBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_INSTANCE, true);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        if (mWorker != null)
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
        // 将我们的请求筛选参数置空
        sParamsPreference.setAScouchLevel(mContext, "");
        sParamsPreference.setAScouchPrice(mContext, "");
        sParamsPreference.setAScouchClazz(mContext, "");
        sParamsPreference.setAScouchRange(mContext, "");

        super.onDestroy();
    }

    private NearbyParamsPreference sParamsPreference = NearbyParamsPreference.getInstance();

    /**
     * 对于可选参数，如果不为空的话，我们才进行参数的插入，否则我们直接采用默认值即“”就可以完成请求过程
     *
     * @param rangeParam 可选参数，主要是用于按距离进行筛选
     * @param priceParam 可选参数，主要是用于按价格花费进行筛选
     * @param clazzParam 可选参数，主要是用于按球种进行筛选
     * @param levelParam 可选参数，主要是用于按助教的水平筛选
     * @param startNo
     * @param endNo
     */
    private void retrieveAllInitialAssistCoauchInfo(String rangeParam, String priceParam, String clazzParam, String levelParam, final int startNo, final int endNo)
    {
        if (!Utils.networkAvaiable(getActivity())) {
            mUIEventsHandler.obtainMessage(STATE_FETCH_DATA_FAILED,
                    mContext.getResources().getString(R.string.network_not_available)).sendToTarget();
            return;
        }

        final List<NearbyAssistCoauchSubFragmentBean> cacheASCoauchList = new ArrayList<NearbyAssistCoauchSubFragmentBean>();

        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        if (! TextUtils.isEmpty(rangeParam))
        {
            requestParams.put("range", rangeParam);
        }
        if (! TextUtils.isEmpty(priceParam))
        {
            requestParams.put("price", priceParam);
        }

        if (! TextUtils.isEmpty("clazz"))
        {
            requestParams.put("clazz", clazzParam);
        }

        if (! TextUtils.isEmpty("level"))
        {
            requestParams.put("level", levelParam);
        }

        requestParams.put("start_no", startNo + "");
        requestParams.put("end_no", endNo + "");

        mListView.setMode(PullToRefreshBase.Mode.DISABLED);

        HttpUtil.requestHttp(HttpConstants.NearbyAssistCoauch.URL, requestParams, HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","assistant response ->" + response);
                try
                {
                    if (! response.isNull("code"))
                    {
                        final int status = response.getInt("code");
                        if (status == HttpConstants.ResponseCode.NORMAL)
                        {
                            JSONObject resultJsonObj = response.getJSONObject("result");
                            if(resultJsonObj.get("list_data").equals("null")){
                                mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }else {
                                JSONArray resultArr = resultJsonObj.getJSONArray("list_data");
                                if(resultArr.length() < 1){
                                    mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                }else {
                                    final int count = resultArr.length();
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
                                                NearbyFragmentsCommonUtils.parseGenderStr(mContext, sex),
                                                NearbyFragmentsCommonUtils.parseBilliardsKinds(mContext, kinds),
                                                money,
                                                String.valueOf(range)
                                        );
                                        cacheASCoauchList.add(assistCoauchBean);
                                    }
                                    if (cacheASCoauchList.isEmpty()) {
                                        mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                    } else {
                                        // 然后我们直接将我们得到助教List直接发送到mUIEventHandler进行处理，因为我们只能在MainUIThread当中进行有关于数据的更新操作
                                        mUIEventsHandler.obtainMessage(STATE_FETCH_DATA_SUCCESS, cacheASCoauchList).sendToTarget();
//                            mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                                    }
                                }
                            }
                        } else if (status == HttpConstants.ResponseCode.TIME_OUT)
                        {
                            // 进行超时处理的请求
                            mUIEventsHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                        } else if (status == HttpConstants.ResponseCode.NO_RESULT)
                        {
                            mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        } else
                        {
                            // 这里需要注意的是，服务器端可能会把msg内容置为null，即错误了，但是没有返回任何内容
                            // 如果我们采用下面的方法，空的bundle会被传输，然后就显示一个空的Toast
//                        mUIEventsHandler.obtainMessage(PublicConstant.REQUEST_ERROR,
//                                initialResultJsonObj.getString("msg")).sendToTarget();
                            Message errorMsg = mUIEventsHandler.obtainMessage(PublicConstant.REQUEST_ERROR);
                            Bundle errorData = new Bundle();
                            String errorStr = response.getString("msg");
                            if (! TextUtils.isEmpty(errorStr))
                            {
                                errorData.putString(KEY_REQUEST_ERROR_MSG, errorStr);
                            }
                            errorMsg.setData(errorData);
                            mUIEventsHandler.sendMessage(errorMsg);
                        }
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                    mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    Log.d(TAG, " exception happened in parsing the json data we get, and the detailed reason are : " + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });

    }


    private TextView mEmptyView;
    // 我们通过将disable的值设置为false来进行加载EmptyView
    // 通过将disable的值设置为true来隐藏emptyView
    private void setEmptyViewVisible()
    {
        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mEmptyView.setTextColor(mContext.getResources().getColor(R.color.md__defaultBackground));
        mEmptyView.setText(mContext.getString(R.string.search_activity_subfragment_empty_tv_str));
        mListView.setEmptyView(mEmptyView);
    }

    private void setEmptyViewGone()
    {
        if (null != mEmptyView)
        {
            mEmptyView.setVisibility(View.GONE);
            mListView.setEmptyView(null);
        }
    }


    private static final String KEY_REQUEST_ERROR_MSG = "keyRequestErrorMsg";
    private static final String KEY_REQUEST_START_NUM = "keyRequestStartNum";
    private static final String KEY_REQUEST_END_NUM = "keyRequestEndNum";

    private static final int DATA_HAS_BEEN_UPDATED = 1 << 10;

    private static final int RETRIEVE_ALL_RAW_INFO = 1 << 1;

    // 由于这些常量值被定义到同一个地方进行使用，所以我们将助教Fragment当中的常量值定义为从30开始
    public static final int RETREIVE_INFO_WITH_KINDS_FILTERED = 30 << 2;
    public static final int RETRIEVE_INFO_WITH_LEVEL_FILTERED = 30 << 3;
    public static final int RETRIEVE_INFO_WITH_PRICE_FILTERED = 30 << 4;
    public static final int RETRIEVE_INFO_WITH_DISTANCE_FILTERED = 30 << 5;

    private static final int UI_SHOW_DIALOG = 1 << 6;

    private static final int STATE_FETCH_DATA_SUCCESS = 1 << 8;
    private static final int STATE_FETCH_DATA_FAILED = 1 << 9;

    private static final int NETWORK_UNAVAILABLE = 1 << 11;

    private Handler mUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)           
        {
            if(mListView.getMode() == PullToRefreshBase.Mode.DISABLED){
                mListView.setMode(PullToRefreshBase.Mode.BOTH);
            }
            if (mListView.isRefreshing())
            {
                mListView.onRefreshComplete();
            }
            switch (msg.what)
            {
                case UI_SHOW_DIALOG:
                    Log.d(TAG, " start showing the dialog ");
                    showProgress();
                    if(mEmptyView.getVisibility() == View.VISIBLE){
                        mListView.setEmptyView(null);
                        mEmptyView.setVisibility(View.GONE);
                    }
                    break;
                case STATE_FETCH_DATA_FAILED:
                    String reasonStr = (String) msg.obj;
                    if (! TextUtils.isEmpty(reasonStr))
                    {
                        Utils.showToast(mContext, reasonStr);
                    }
                    if (mAssistCoauchList.isEmpty())
                    {
                        setEmptyViewVisible();
                    }
                    hideProgress();
                    Log.d(TAG, " fail to fetch the data, and the reason are : " + reasonStr);
                    break;

                case PublicConstant.USE_CACHE:
                    setEmptyViewGone();
                    ArrayList<NearbyAssistCoauchSubFragmentBean> cachedList = (ArrayList<NearbyAssistCoauchSubFragmentBean>) msg.obj;
                    mAssistCoauchList.addAll(cachedList);
                    if(mAssistCoauchList.isEmpty()){
                        setEmptyViewVisible();
                    }
                    break;
                case STATE_FETCH_DATA_SUCCESS:
                    // 首先隐藏EmptyView

                    setEmptyViewGone();
                    hideProgress();

                    mBeforeCount = mAssistCoauchList.size();
                    List<NearbyAssistCoauchSubFragmentBean> asList = (ArrayList<NearbyAssistCoauchSubFragmentBean>) msg.obj;
                    boolean isListEmpty = mAssistCoauchList.isEmpty();
                    for (NearbyAssistCoauchSubFragmentBean asBean : asList)
                    {
                        if (! mAssistCoauchList.contains(asBean))
                        {
                            if(!isListEmpty && Integer.valueOf(mAssistCoauchList.get(0).getUserId()) < Integer.valueOf(asBean.getUserId())){
                                mAssistCoauchList.add(0,asBean);
                            }else {
                                mAssistCoauchList.add(asBean);
                            }
                        }
                    }
                    mAfterCount = mAssistCoauchList.size();

                    // 判断一下，当前的List是否是空的，如果是空的，我们就需要加载一下当list为空时，显示的TextView
                    if (mAssistCoauchList.isEmpty())
                    {
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
                case RETRIEVE_INFO_WITH_DISTANCE_FILTERED:
                    String rangeStr = (String) msg.obj;
                    if (null != mWorker)
                    {
                        Log.d(TAG, " inside the UIEventsHandler, and the range data we get are : " + rangeStr);
                        mWorker.fetchDataWithRangeFilter(rangeStr);
                    }
                    break;
                case RETRIEVE_INFO_WITH_LEVEL_FILTERED:
                    String levelStr = (String) msg.obj;
                    if (null != mWorker)
                    {
                        Log.d(TAG, " inside the UIEventsHandler, and the levelStr we get are : " + levelStr);
                        mWorker.fetchDataWithLevelFilter(levelStr);
                    }
                    break;
                case RETRIEVE_INFO_WITH_PRICE_FILTERED:
                    String priceStr = (String) msg.obj;
                    if (null != mWorker)
                    {
                        Log.d(TAG, " inside the UIEventsHandler, and the priceStr we get are : " + priceStr);
                        mWorker.fetchDataWithPriceFilter(priceStr);
                    }
                    break;
                case RETREIVE_INFO_WITH_KINDS_FILTERED:
                    String clazz = (String) msg.obj;
                    if (null != mWorker)
                    {
                        Log.d(TAG, " inside the UIEventsHandler, and the clazzStr we get are : " + clazz);
                        mWorker.fetchDataWithClazzFilter(clazz);
                    }
                    break;

                case NETWORK_UNAVAILABLE:
                    if (mAssistCoauchList.isEmpty())
                    {
                        Log.d(TAG, " inside the UIEvents Handler, and we have loaded the EmptyView here ");
                        setEmptyViewVisible();
                    }
                    Utils.showToast(mContext, mContext.getString(R.string.network_not_available));
                    // 当网络不可行时，我们需要将ProgressBar不再显示
                    hideProgress();
                    break;
                case DATA_HAS_BEEN_UPDATED:
                    mAssistCoauchListAdapter.notifyDataSetChanged();
                    Log.d(TAG, " the data set has been updated ");
                    break;
                case PublicConstant.TIME_OUT:
                    // 超时之后的处理策略
                    Utils.showToast(mContext, mContext.getString(R.string.http_request_time_out));
                    if (mAssistCoauchList.isEmpty())
                    {

                        setEmptyViewVisible();
                    }

                    hideProgress();
                    break;
                case PublicConstant.NO_RESULT:
                    if (mAssistCoauchList.isEmpty())
                    {
                        setEmptyViewVisible();
                    } else
                    {
                        if (mLoadMore)
                        {
                            Utils.showToast(mContext, mContext.getString(R.string.no_more_info, mContext.getString(R.string.search_billiard_assist_coauch_str)));
                        }
                    }
                    hideProgress();
                    break;

                case PublicConstant.REQUEST_ERROR:
                    Bundle errorData = msg.getData();
                    String errorInfo = errorData.getString(KEY_REQUEST_ERROR_MSG);
                    if (! TextUtils.isEmpty(errorInfo))
                    {
                        Utils.showToast(mContext, errorInfo);
                    } else
                    {
                        Utils.showToast(mContext, mContext.getString(R.string.http_request_error));
                    }
                    if (mAssistCoauchList.isEmpty())
                    {
                        setEmptyViewVisible();
                    }

                    hideProgress();
                    break;
            }

            mAssistCoauchListAdapter.notifyDataSetChanged();
        }
    };

    private void showProgress()
    {
        mPreProgress.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);
    }

    private void hideProgress()
    {
        mPreProgress.setVisibility(View.GONE);
        mPreTextView.setVisibility(View.GONE);
    }

    private final String BACKGROUND_HANDLER_NAME = "BackgroundWorkerHandler";

    // 用于处理后台任务的处理器
    private class BackgroundWorkerHandler extends HandlerThread
    {
        private final int mInitStartNum , mInitEndNum;

        public BackgroundWorkerHandler(int startNum, int endNum)
        {
            super(BACKGROUND_HANDLER_NAME, Process.THREAD_PRIORITY_BACKGROUND);
            this.mInitStartNum = startNum;
            this.mInitEndNum = endNum;
        }

        // 参照MateFragment当中的理解
        private Handler mBackgroundHandler = new Handler();

        @Override
        protected void onLooperPrepared()
        {
            super.onLooperPrepared();
            mBackgroundHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    super.handleMessage(msg);
                    switch (msg.what)
                    {
                        case RETRIEVE_ALL_RAW_INFO:
                            mUIEventsHandler.sendEmptyMessage(UI_SHOW_DIALOG);
                            Bundle requestData = msg.getData();
                            final int startNum = requestData.getInt(KEY_REQUEST_START_NUM);
                            final int endNum = requestData.getInt(KEY_REQUEST_END_NUM);
                            String cacheRange = sParamsPreference.getAScouchRange(mContext);
                            String cachePrice = sParamsPreference.getASCouchPrice(mContext);
                            String cacheLevel = sParamsPreference.getASCouchLevel(mContext);
                            String cacheClazz = sParamsPreference.getASCouchClazz(mContext);
                            retrieveAllInitialAssistCoauchInfo(cacheRange, cachePrice, cacheClazz, cacheLevel, startNum, endNum);

                            break;
                        case RETRIEVE_INFO_WITH_LEVEL_FILTERED:
                            if (! mAssistCoauchList.isEmpty())
                            {
                                mAssistCoauchList.clear();
                            }
                            String level = (String) msg.obj;
                            Log.d(TAG, " Inside the WorkerThread --> the level data we need to filter are : " + level);
                            String levelCacheClazz = sParamsPreference.getASCouchClazz(mContext);
                            String levelCachePrice = sParamsPreference.getASCouchPrice(mContext);
                            String levelCacheRange = sParamsPreference.getAScouchRange(mContext);
                            retrieveAllInitialAssistCoauchInfo(levelCacheRange, levelCachePrice, levelCacheClazz, level, 0, 9);

                            break;
                        case RETREIVE_INFO_WITH_KINDS_FILTERED:
                            if (! mAssistCoauchList.isEmpty())
                            {
                                mAssistCoauchList.clear();
                            }
                            String clazz = (String) msg.obj;
                            Log.d(TAG, " Inside the WorkerThread --> the clazz we need to filter are : " + clazz);
                            String clazzCacheRange = sParamsPreference.getAScouchRange(mContext);
                            String clazzCachePrice = sParamsPreference.getASCouchPrice(mContext);
                            String clazzCacheLevel = sParamsPreference.getASCouchLevel(mContext);
                            retrieveAllInitialAssistCoauchInfo(clazzCacheRange, clazzCachePrice, clazz, clazzCacheLevel, 0, 9);

                            break;
                        case RETRIEVE_INFO_WITH_PRICE_FILTERED:
                            if (! mAssistCoauchList.isEmpty())
                            {
                                mAssistCoauchList.clear();
                            }
                            String price = (String) msg.obj;
                            Log.d(TAG, " Inside the WorkerThread --> the price we need to filter are : " + price);
                            String priceCacheRange = sParamsPreference.getAScouchRange(mContext);
                            String priceCacheClazz = sParamsPreference.getASCouchClazz(mContext);
                            String priceCacheLevel = sParamsPreference.getASCouchLevel(mContext);
                            retrieveAllInitialAssistCoauchInfo(priceCacheRange, price, priceCacheClazz, priceCacheLevel, 0, 9);

                            break;
                        case RETRIEVE_INFO_WITH_DISTANCE_FILTERED:
                            if (! mAssistCoauchList.isEmpty())
                            {
                                mAssistCoauchList.clear();
                            }
                            String distance = (String) msg.obj;
                            Log.d(TAG, " Inside the WorkerThread --> the distance we need to filter are : " + distance);
                            String rangeCachePrice = sParamsPreference.getASCouchPrice(mContext);
                            String rangeCacheClazz = sParamsPreference.getASCouchClazz(mContext);
                            String rangeCacheLevel = sParamsPreference.getASCouchLevel(mContext);
                            retrieveAllInitialAssistCoauchInfo(distance, rangeCachePrice, rangeCacheClazz, rangeCacheLevel, 0, 9);

                            break;
                    }
                }
            };
            if (Utils.networkAvaiable(getActivity()))
            {
                // 我们初始请求的数据肯定都是最新的数据，所以从0条开始请求
                fetchAllData(mInitStartNum, mInitEndNum);
            }
        }

        public void fetchAllData(final int startNum, final int endNum)
        {
            if (null != mBackgroundHandler)
            {
                Message msg = mBackgroundHandler.obtainMessage(RETRIEVE_ALL_RAW_INFO);
                Bundle requestData = new Bundle();
                requestData.putInt(KEY_REQUEST_START_NUM, startNum);
                requestData.putInt(KEY_REQUEST_END_NUM, endNum);
                msg.setData(requestData);
                mBackgroundHandler.sendMessage(msg);
            }
        }

        public void fetchDataWithPriceFilter(String price)
        {
            if (! TextUtils.isEmpty(price) && null != mBackgroundHandler)
            {
                mBackgroundHandler.obtainMessage(RETRIEVE_INFO_WITH_PRICE_FILTERED,price).sendToTarget();
            }
        }

        public void fetchDataWithRangeFilter(String range)
        {
            if (! TextUtils.isEmpty(range) && null != mBackgroundHandler)
            {
                mBackgroundHandler.obtainMessage(RETRIEVE_INFO_WITH_DISTANCE_FILTERED,range).sendToTarget();
            }
        }

        public void fetchDataWithLevelFilter(String level)
        {
            if (! TextUtils.isEmpty(level) && null != mBackgroundHandler)
            {
                mBackgroundHandler.obtainMessage(RETRIEVE_INFO_WITH_LEVEL_FILTERED,level).sendToTarget();
            }
        }

        public void fetchDataWithClazzFilter(String clazz)
        {
            if (! TextUtils.isEmpty(clazz) && null != mBackgroundHandler)
            {
                mBackgroundHandler.obtainMessage(RETREIVE_INFO_WITH_KINDS_FILTERED,clazz).sendToTarget();
            }
        }
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
                if (mWorker != null)
                {
                    mUIEventsHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mWorker.fetchAllData(0, 9);
                        }
                    },300);

                }
            } else
            {
                mUIEventsHandler.sendEmptyMessage(NETWORK_UNAVAILABLE);
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            String label = NearbyFragmentsCommonUtils.getLastedTime(mContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            mLoadMore = true;

            mCurrentPos = mAssistCoauchList.size();

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
                    mUIEventsHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mWorker.fetchAllData(mStartNum, mEndNum);
                        }
                    },300);

                }
            } else
            {
                mUIEventsHandler.sendEmptyMessage(NETWORK_UNAVAILABLE);
            }
        }
    };



}































































































































































































