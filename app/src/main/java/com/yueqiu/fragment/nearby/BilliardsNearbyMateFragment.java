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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.adapter.NearbyMateSubFragmentListAdapter;
import com.yueqiu.bean.NearbyDatingDetailedAlreadyBean;
import com.yueqiu.bean.NearbyMateSubFragmentUserBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.daoimpl.NearbyMateDaoImpl;
import com.yueqiu.fragment.nearby.common.NearbyPopBasicClickListener;
import com.yueqiu.fragment.nearby.common.NearbyParamsPreference;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.LocationUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils.KEY_SAVED_LISTVIEW;

/**
 * Created by scguo on 14/12/17.
 * <p/>
 * 球友Fragment,助教Fragment,教练Fragment,球厅Fragment,约球Fragment这五个Fragment的父类
 * <p/>
 * 对于球友Fragment,我们需要创建的内容是一个基于ViewPager的Gallery(图片是由服务器动态获取的)，这个ViewPager
 * 的地步有一些圆点用于indicator。然后在ViewPager的下面就直接就是两个Fragment了，对于这两个Fragment我们就直接
 * 使用RadioButton来进行控制了。
 */
@SuppressLint("ValidFragment")
public class BilliardsNearbyMateFragment extends Fragment
{
    private static final String TAG = "DeskBallFragment";
    private static final String TAG_2 = "data_retrieve_debug";
    public static final String BILLIARD_SEARCH_TAB_NAME = "billiard_search_tab_name";
    private View mView;
    private String mArgs;
    private Context mContext;

    private int mRequestFlag;
    private float mLat;
    private float mLng;

    private LocationManagerProxy mLocationManagerProxy;

    private PullToRefreshListView mSubFragmentListView;

    @SuppressLint("ValidFragment")
    public BilliardsNearbyMateFragment()
    {
    }

    private static final String KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT = "keyBilliardsSearchParentFragment";

    private static NearbyParamsPreference sParamsPreference = NearbyParamsPreference.getInstance();

    public static BilliardsNearbyMateFragment newInstance(Context context, String params)
    {
//        mContext = context;
        BilliardsNearbyMateFragment fragment = new BilliardsNearbyMateFragment();

        Bundle args = new Bundle();
        args.putString(KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT, params);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.mContext = activity;
    }

    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//    private static NearbyMateDaoImpl mMateListAdapterateDaoImpl;
    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

    // mIsHead用于控制数据的加载到List当中的方向(即加载到头部还是加载到尾部)
    private boolean mIsNetworkAvailable;
    private NearbyParamsPreference mRequestParms;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // 得到用于保存请求参数的SharedPreference实例
        mRequestParms = NearbyParamsPreference.getInstance();

        // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//        mMateListAdapterateDaoImpl = new NearbyMateDaoImpl(mContext);
        // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

        mIsNetworkAvailable = Utils.networkAvaiable(mContext);
    }

    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;
    private ArrayList<NearbyMateSubFragmentUserBean> mUserList = new ArrayList<NearbyMateSubFragmentUserBean>();

    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//    private List<NearbyMateSubFragmentUserBean> mUpdateList = new ArrayList<NearbyMateSubFragmentUserBean>();
//    private List<NearbyMateSubFragmentUserBean> mInsertList = new ArrayList<NearbyMateSubFragmentUserBean>();
//    private List<NearbyMateSubFragmentUserBean> mDBList = new ArrayList<NearbyMateSubFragmentUserBean>();
    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

    private NearbyMateSubFragmentListAdapter mMateListAdapter;
    private NearbyPopBasicClickListener mClickListener;


    private boolean mRefresh;
    private boolean mLoadMore;
    private boolean mIsSavedInstance;
    private boolean mIsListEmpty;

    private int mStartNum = 0;
    private int mEndNum = 9;
    // 用于定义当前MateList当中的list的position，帮助我们确定从第几条开始请求数据
    private int mCurrentPos;
    private int mBeforeCount, mAfterCount;

    private ArrayList<NearbyMateSubFragmentUserBean> mCachedMateList = new ArrayList<NearbyMateSubFragmentUserBean>();

    public NearbyFragmentsCommonUtils.ControlPopupWindowCallback mPopupwindowCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_nearby_mate_layout, container, false);
        // then, inflate the image view pager
        NearbyFragmentsCommonUtils commonUtils = new NearbyFragmentsCommonUtils(mContext);
        commonUtils.initViewPager(mContext, mView);

        mSubFragmentListView = (PullToRefreshListView) mView.findViewById(R.id.search_sub_fragment_list);
        mSubFragmentListView.setMode(PullToRefreshBase.Mode.BOTH);
        mSubFragmentListView.setOnRefreshListener(mOnRefreshListener);

        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        mPreTextView = (TextView) mView.findViewById(R.id.pre_text);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mEmptyView = new TextView(getActivity());

        mClickListener = new NearbyPopBasicClickListener(mContext, mUIEventsHandler, sParamsPreference);
        (mView.findViewById(R.id.btn_mate_distance)).setOnClickListener(mClickListener);
        (mView.findViewById(R.id.btn_mate_gender)).setOnClickListener(mClickListener);
        mPopupwindowCallback = mClickListener;

        Bundle args = getArguments();
        mArgs = args.getString(BILLIARD_SEARCH_TAB_NAME);

        // TODO: 以下加载是测试数据，暂时不能删除(因为现在的数据不完整，我们还需要这些测试数据来查看数据加载完整的具体的具体的UI效果)
//        initListViewDataSrc();

        // TODO: 我们现在由于不需要数据库缓存方面的逻辑需要，所以现在我们先将下面的代码注释掉，但是如果
        // TODO: 后期添加的时候主需要更改一下就可以了
        // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//        new Thread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                mDBList = mMateListAdapterateDaoImpl.getMateList(mStartNum, mEndNum + 1);
//                if (!mDBList.isEmpty())
//                {
//                    mUIEventsHandler.obtainMessage(USE_CACHE, mDBList).sendToTarget();
//                }
//            }
//        }).start();
        // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

        // 我们将所有的数据请求的工作放到onCreateView()当中，而不是放到onResume()方法当中。
        // 因为在onResume()当中请求太频繁了

        if (null != savedInstanceState)
        {
            mRefresh = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_REFRESH);
            mLoadMore = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_LOAD_MORE);
            mIsSavedInstance = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_INSTANCE);

            mCachedMateList = savedInstanceState.getParcelableArrayList(KEY_SAVED_LISTVIEW);

            mUIEventsHandler.obtainMessage(PublicConstant.USE_CACHE, mCachedMateList).sendToTarget();
        }


        mMateListAdapter = new NearbyMateSubFragmentListAdapter(mContext,  mUserList);
        mSubFragmentListView.setAdapter(mMateListAdapter);

//        mMateListAdapter.notifyDataSetChanged();

        mLoadMore = false;
        mRefresh = false;
        mWorker = new BackgroundWorker(mStartNum, mEndNum);
        if (mWorker.getState() == Thread.State.NEW)
        {
            Log.d(TAG_2, " 1. the mWorker has started ");
            mWorker.start();
        }

        if (! Utils.networkAvaiable(mContext))
        {
            // 我们的数据请求只是发生于网络可行的情况下
            mUIEventsHandler.sendEmptyMessage(NO_NETWORK);
        }

        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
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
        mPopupwindowCallback.closePopupWindow();
        super.onPause();
    }

    @Override
    public void onStop()
    {
        // TODO: 我们在这里进行一些停止数据更新的操作，即停止任何同数据请求和处理的相关工作,然后再调用super.onStop()
        // TODO: 我们目前采用的策略只是简单的直接获取数据的方式，如果需要升级我们还需要通过添加BroadcastReceiver来
        // TODO: 监听数据的获取状态，然后在onStop()方法当中解注册这个BroadcastReceiver

        mPopupwindowCallback.closePopupWindow();
        super.onStop();
    }


    /**
     * 用于请求首页当中的球友的信息列表
     *
     * @param startNo
     * @param endNo
     * @param distance 这个参数是可以为空的，当不为空的时候，就是我们进行筛选的时候
     * @param gender   这个参数是可以为空的，当不为空的时候，就是我们进行筛选的时候
     */
    private void retrieveInitialMateInfoList(final int startNo, final int endNo, final String distance, final String gender,float lat, float lng)
    {
        if (!Utils.networkAvaiable(mContext))
        {
            mUIEventsHandler.sendEmptyMessage(DATA_RETRIEVE_FAILED);
            mUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
            return;
        }
        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("start_no", startNo + "");
        requestParams.put("end_no", endNo + "");
        requestParams.put("lat",String.valueOf(lat));
        requestParams.put("lng",String.valueOf(lng));
        if (!TextUtils.isEmpty(distance))
        {
            requestParams.put("range", distance);
        }
        if (!TextUtils.isEmpty(gender))
        {
            requestParams.put("sex", gender);
        }
        Log.d("wy","mate params->" + requestParams);
        final List<NearbyMateSubFragmentUserBean> cacheMateList = new ArrayList<NearbyMateSubFragmentUserBean>();

        mUIEventsHandler.sendEmptyMessage(SET_PULLREFRESH_DISABLE);
        
        HttpUtil.requestHttp(HttpConstants.NearbyMate.URL, requestParams, HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","mate response ->" + response);
                try
                {
                    if (! TextUtils.isEmpty(response.get("code").toString()))
                    {
                        final int code = response.getInt("code");
                        JSONObject resultJson = response.getJSONObject("result");
                        Log.d(TAG, " the initial json object we get are : " + response + " ; and the result are : " + resultJson);
                        if (code == HttpConstants.ResponseCode.NORMAL)
                        {
                            if(resultJson != null)
                            {
                                Log.d(TAG, " all are ok in for now ");
                                final int dataCount = resultJson.getInt("count");
                                Log.d(TAG, " the dataCount we get are : " + dataCount);
                                if(resultJson.get("list_data").equals("null")){
                                    mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                }else {
                                    JSONArray dataList = resultJson.getJSONArray("list_data");
                                    if(dataList.length() < 1) {
                                        mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                    }else{
                                        int i;
                                        for (i = 0; i < dataList.length(); ++i) {
                                            JSONObject dataObj = (JSONObject) dataList.get(i);
                                            String imgUrl = dataObj.getString("img_url");
                                            String sex = dataObj.getString("sex");
                                            String userName = dataObj.getString("username");
                                            String userId = dataObj.getString("user_id");
                                            int range = dataObj.getInt("range");
                                            String district = dataObj.getString("district");
                                            NearbyMateSubFragmentUserBean mateUserBean = new NearbyMateSubFragmentUserBean(userId, imgUrl, userName, NearbyFragmentsCommonUtils.parseGenderStr(mContext, sex), district, String.valueOf(range));

                                            cacheMateList.add(mateUserBean);
                                        }
                                        // TODO: 数据获取完之后，我们需要停止显示ProgressBar(这部分功能还需要进一步测试)
                                        if (cacheMateList.isEmpty()) {
                                            Log.d(TAG_2, " before 5, bugs here ");
                                            mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);

                                        } else {
                                            Log.d(TAG_2, " 5. we have get all the data ");
                                            mUIEventsHandler.obtainMessage(DATA_RETRIEVE_SUCCESS, cacheMateList).sendToTarget();
                                            mUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
                                        }
                                    }
                                }
                            } else
                            {
                                Log.d(TAG_2, " 5.1 bug report 1 ");
                                mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                mUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
                            }
                        } else if (code == HttpConstants.ResponseCode.TIME_OUT)
                        {
                            Log.d(TAG_2, "5.2 bug report 2 ");
                            mUIEventsHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                            mUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
                        } else if (code == HttpConstants.ResponseCode.NO_RESULT)
                        {
                            Log.d(TAG_2, "5.3 bug report 3 ");
                            mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            mUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
                        } else
                        {
                            Log.d(TAG_2, "5.4 bug report 4 ");
                            String errorStr = response.getString("msg");
                            Log.d(TAG, " inside the initial mate data request method, and the error info we get are : " + errorStr);
                            Message errorMsg = mUIEventsHandler.obtainMessage(PublicConstant.REQUEST_ERROR);
                            Bundle data = new Bundle();
                            if (! TextUtils.isEmpty(errorStr))
                            {
                                data.putString(KEY_REQUEST_ERROR_MSG, errorStr);
                            }
                            errorMsg.setData(data);
                            mUIEventsHandler.sendMessage(errorMsg);
                        }
                    } else
                    {
                        Log.d(TAG_2, " bug report 5 ");
                        mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                        mUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
                    }
                } catch (JSONException e)
                {
                    Log.d(TAG_2, " bug report 6, exception happened in parsing json , reason are : " + e.toString());
                    // 一旦异常发生，我们应该停止数据的加载工作了，而不是继续的加载
                    e.printStackTrace();
                    Log.d(TAG, " exception happened in parsing the json data we get, and the detailed reason are : " + e.toString());
                    mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    mUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                mUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
            }
        });
    }

    private static final String KEY_REQUEST_ERROR_MSG = "keyRequestErrorMsg";
    private static final String KEY_REQUEST_START_NUM = "requestStartNum";
    private static final String KEY_REQUEST_END_NUM = "requestEndNum";

    private static final int DATA_HAS_BEEN_UPDATED = 1 << 8;
    private static final int START_RETRIEVE_ALL_DATA = 1 << 1;

    private static final int DATA_RETRIEVE_SUCCESS = 1 << 2;
    private static final int DATA_RETRIEVE_FAILED = 1 << 3;

    // 由于我们现在将所有的筛选请求已经全部移到了同一个Listener当中进行统一管理，所以我们
    // 需要将这个请求的整数常量值集中起来，防止重复, 我们将mate Fragment定义从10开始
    public static final int START_RETRIEVE_DATA_WITH_RANGE_FILTER = 10 << 4;
    public static final int START_RETRIEVE_DATA_WITH_GENDER_FILTER = 10 << 5;


    // 同UI相关的事件的两个消息
    private static final int SHOW_PROGRESSBAR = 1 << 6;
    private static final int HIDE_PROGRESSBAR = 1 << 7;

    public static final int SET_PULLREFRESH_DISABLE = 42;
    public static final int GET_LOCATION = 43;
    public static final int LOCATION_HAS_GOT = 44;


    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//    private static final int UPDATE_LOCAL_MATE_TABLE = 1 << 9;
//    private static final int USE_CACHE = 1 << 10;
    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

    private static final int NO_NETWORK = 1 << 11;

    // 这个Handler主要是用于处理UI相关的事件,例如涉及到UI的事件的直接处理，例如Toast或者ProgressBar的显示控制
    private Handler mUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {

            if(mSubFragmentListView.getMode() == PullToRefreshBase.Mode.DISABLED){
                mSubFragmentListView.setMode(PullToRefreshBase.Mode.BOTH);
            }
            if (mSubFragmentListView.isRefreshing())
            {
                mSubFragmentListView.onRefreshComplete();
            }
            switch (msg.what)
            {
                case DATA_RETRIEVE_FAILED:
                    setEmptyViewGone();
                    if (mUserList.isEmpty())
                    {
                        Log.d("scguo_tag", "load emptyView 1");
                        setEmptyVewVisible();
                    }
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                    hideProgress();
                    break;
                case PublicConstant.USE_CACHE:
                    // 我们需要首先将我们之前加载的EmptyView不再显示
                    setEmptyViewGone();
                    List<NearbyMateSubFragmentUserBean> cacheList = (ArrayList<NearbyMateSubFragmentUserBean>) msg.obj;
                    mUserList.addAll(cacheList);
                    if(mUserList.isEmpty()){
                        setEmptyVewVisible();
                    }
                    break;

                case DATA_RETRIEVE_SUCCESS:
                    // 首先我们需要将我们的EmptyView隐藏掉
                    setEmptyViewGone();
                    Log.d(TAG_2, "6. retrieved successfully, and we are processing it in the UI events handler ... ");
                    mBeforeCount = mUserList.size();
                    mIsListEmpty = mUserList.isEmpty();
                    List<NearbyMateSubFragmentUserBean> mateList = (ArrayList<NearbyMateSubFragmentUserBean>) msg.obj;
                    for (NearbyMateSubFragmentUserBean mateBean : mateList)
                    {
                        if (!mUserList.contains(mateBean))
                        {
//                            if (mRefresh && !mIsListEmpty)
//                            {
//                                mUserList.add(0, mateBean);
//                            } else
//                            {
//                                if (mIsSavedInstance)
//                                {
//                                    mUserList.add(0, mateBean);
//                                } else
//                                {
//                                    mUserList.add(mateBean);
//                                }
//                            }
                            if(!mIsListEmpty && Integer.valueOf(mUserList.get(0).getUserId()) < Integer.valueOf(mateBean.getUserId())){
                                mUserList.add(0,mateBean);
                            }else {
                                mUserList.add(mateBean);
                            }
                        }
                        // TODO: ------------------------UNCOMMENT LATER------------------------------------------
//                        if (!mDBList.isEmpty())
//                        {
//                            if (! mDBList.contains(mateBean))
//                            {
//                                mInsertList.add(mateBean);
//                            } else
//                            {
//                                mUpdateList.add(mateBean);
//                            }
//                        }
                        // TODO: ------------------------UNCOMMENT LATER-------------------------------------------
                    }
                    Collections.sort(mUserList,new DescComparator());
                    mAfterCount = mUserList.size();
                    Log.d(TAG_2, " 7. mUIEventsHandler --> the final user list size are : " + mAfterCount);

                    if (mUserList.isEmpty())
                    {
                        Log.d("scguo_tag", "load emptyView 2");
                        setEmptyVewVisible();
                    } else
                    {
                        // 如果触发DATA_RETRIEVE_SUCCESS的事件是来自用户的下拉刷新
                        // 事件，那么我们需要根据我们得到更新后的List来判断数据的加载是否是成功的(上拉刷新是不需要判断的，
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

                    mRefresh = false;
                    mLoadMore = false;
                    break;
                // TODO: ------------------------UNCOMMENT LATER------------------------------------------
                // TODO: 以下的代码是用于数据库的缓存处理过程
//                case USE_CACHE:
//                    List<NearbyMateSubFragmentUserBean> dbCacheList = (ArrayList<NearbyMateSubFragmentUserBean>) msg.obj;
//                    mUserList.addAll(dbCacheList);
//
//                    mMateListAdapter.notifyDataSetChanged();
//                    break;
                // TODO: ------------------------UNCOMMENT LATER------------------------------------------
                case NO_NETWORK:
                    setEmptyViewGone();
                    Utils.showToast(mContext, mContext.getString(R.string.network_not_available));

                    if (mUserList.isEmpty())
                    {
                        Log.d("scguo_tag", "load emptyView 3");
                        setEmptyVewVisible();
                    }

                    mRefresh = false;
                    mLoadMore = false;
                    break;
                case SHOW_PROGRESSBAR:
                    showProgress();
                    if(mEmptyView.getVisibility() == View.VISIBLE){
                        mSubFragmentListView.setEmptyView(null);
                        mEmptyView.setVisibility(View.GONE);
                    }
                    Log.d(TAG, " start showing the progress bar ");

                    break;
                case HIDE_PROGRESSBAR:
                    setEmptyViewGone();
                    mMateListAdapter.notifyDataSetChanged();
                    hideProgress();
                    // 当我们将数据获取完之后，就需要经Refresh的标记去掉，否则会一直在那里转
                    if (mSubFragmentListView.isRefreshing())
                    {
                        mSubFragmentListView.onRefreshComplete();
                    }

                    if (mUserList.isEmpty())
                    {
                        Log.d("scguo_tag", "load emptyView 4");
                        setEmptyVewVisible();
                    }
                    Log.d(TAG, " hiding the progress bar ");

                    break;
//                case START_RETRIEVE_DATA_WITH_GENDER_FILTER:
//                    String gender = (String) msg.obj;
//                    if (null != mWorker)
//                    {
//                        mWorker.fetchDataWithGenderFiltered(gender);
//                    }
//
//                    break;
//
//                case START_RETRIEVE_DATA_WITH_RANGE_FILTER:
//                    String range = (String) msg.obj;
//                    if (null != mWorker)
//                    {
//                        mWorker.fetchDataWithRangeFilter(range);
//                    }
//                    break;

                case DATA_HAS_BEEN_UPDATED:
                    mMateListAdapter.notifyDataSetChanged();
                    Log.d(TAG, " inside the UIEventsHandler --> the adapter has been notified ");

                    break;

                case PublicConstant.TIME_OUT:
                    setEmptyViewGone();
                    // 超时之后的处理策略
                    Utils.showToast(mContext, mContext.getString(R.string.http_request_time_out));
                    if (mUserList.isEmpty())
                    {
                        Log.d("scguo_tag", "load emptyView 5");
                        setEmptyVewVisible();
                    }
                    hideProgress();
                    mRefresh = false;
                    mLoadMore = false;
                    break;

                case PublicConstant.NO_RESULT:
                    setEmptyViewGone();
                    if (mUserList.isEmpty())
                    {
                        Log.d("scguo_tag", "load emptyView 6");
                        setEmptyVewVisible();
                    } else
                    {
                        if (mLoadMore)
                        {
                            Utils.showToast(mContext, mContext.getString(R.string.no_more_info, mContext.getString(R.string.nearby_billiard_mate_str)));
                        }
                    }
                    hideProgress();

                    mRefresh = false;
                    mLoadMore = false;
                    break;

                case PublicConstant.REQUEST_ERROR:
                    setEmptyViewGone();
                    Bundle errorData = msg.getData();
                    String errorInfo = errorData.getString(KEY_REQUEST_ERROR_MSG);
                    Log.d(TAG, " inside the UIEventsProcessingHandler --> have exception while we make the network request, and the error msg : " + errorInfo);
                    if (! TextUtils.isEmpty(errorInfo))
                    {
                        Utils.showToast(mContext, errorInfo);
                    } else
                    {
                        Utils.showToast(mContext, mContext.getString(R.string.http_request_error));
                    }

                    if (mUserList.isEmpty())
                    {
                        Log.d("scguo_tag", " fuck goes here ! load emptyView 7");
                        setEmptyVewVisible();
                    }

                    mRefresh = false;
                    mLoadMore = false;
                    hideProgress();
                    break;
                case SET_PULLREFRESH_DISABLE:
                    mSubFragmentListView.setMode(PullToRefreshBase.Mode.DISABLED);
                    break;
                case GET_LOCATION:
//                    mPreTextView.setText("正在获取当前坐标");
                    showProgress();
                    mSubFragmentListView.setMode(PullToRefreshBase.Mode.DISABLED);
                    if(mEmptyView.getVisibility() == View.VISIBLE){
                        mSubFragmentListView.setEmptyView(null);
                        mEmptyView.setVisibility(View.GONE);
                    }
                    mRequestFlag = msg.arg1;
                    mArgs = (String) msg.obj;
//                    getActivity().startService(new Intent(getActivity(), LocationUtil.class));
                    getLocation();
                    break;
                case LOCATION_HAS_GOT:
                    Bundle args = (Bundle) msg.obj;
                    mLat = args.getFloat("lat");
                    mLng = args.getFloat("lng");
                    switch(mRequestFlag){

                        case START_RETRIEVE_ALL_DATA:
                            String rangeParams = mRequestParms.getMateRange(mContext);
                            String genderParams = mRequestParms.getMateGender(mContext);
//                            mPreTextView.setText(getString(R.string.pre_request_text));
                            retrieveInitialMateInfoList(mStartNum, mEndNum, rangeParams, genderParams, mLat, mLng);
                            break;
                        case START_RETRIEVE_DATA_WITH_RANGE_FILTER:
                            if (!mUserList.isEmpty())
                            {
                                Log.d(TAG, "inside the UIEventsHandler, and the list is not empty, and we should empty it at first");
                                mUserList.clear();
                                // 然后通知Adapter当中的数据源已经发生变化，所以更新Adapter
                                mUIEventsHandler.sendEmptyMessage(DATA_HAS_BEEN_UPDATED);
                            }
                            Log.d(TAG, " inside the workThread --> start filtering the mate list based on the range of the current user " + mArgs);
                            // 每次筛选，都是从第0条开始请求最新的数据
                            // 因为这相当于完全的重新开始了，所以我们需要将我们已经获得的UserList清空才可以
                            if (sParamsPreference.getMateGender(mContext) != null) {
                                retrieveInitialMateInfoList(0, 9, mArgs, sParamsPreference.getMateGender(mContext), mLat, mLng);
                            }
                            break;
                        case START_RETRIEVE_DATA_WITH_GENDER_FILTER:
                            if (!mUserList.isEmpty())
                            {
                                Log.d(TAG, " inside the UIEventsHandler, and the list is not empty, and we should empty it at first");
                                mUserList.clear();
                                mUIEventsHandler.sendEmptyMessage(DATA_HAS_BEEN_UPDATED);
                            }
                            Log.d(TAG, " inside the workThread --> start filtering the mate list based on the gender of the current user " + mArgs);
                            if (sParamsPreference.getMateRange(mContext) != null) {
                                // TODO: 现在我们正式完成了筛选的工作了，但是却还有一个问题，那就是我们
                                // TODO: 确定所要加载的条目
                                retrieveInitialMateInfoList(0, 9, sParamsPreference.getMateRange(mContext), mArgs,mLat,mLng);

                            }
                            break;
                    }
                    break;
            }
            mMateListAdapter.notifyDataSetChanged();
        }
    };

    private TextView mEmptyView;
    // 我们通过将disable的值设置为false来进行加载EmptyView
    // 通过将disable的值设置为true来隐藏emptyView
    private void setEmptyVewVisible()
    {

        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mEmptyView.setTextColor(mContext.getResources().getColor(R.color.md__defaultBackground));
        mEmptyView.setText(mContext.getString(R.string.search_activity_subfragment_empty_tv_str));
        mSubFragmentListView.setEmptyView(mEmptyView);
    }

    private void setEmptyViewGone()
    {
        if (null != mEmptyView)
        {
            mEmptyView.setVisibility(View.GONE);
            mSubFragmentListView.setEmptyView(null);
        }
    }

    private void showProgress()
    {
        Log.d(TAG, " showing the progress bar ");
        mPreProgress.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);
    }

    private void hideProgress()
    {
        Log.d(TAG, " hiding the progress bar ");
        mPreProgress.setVisibility(View.GONE);
        mPreTextView.setVisibility(View.GONE);
    }

    private static final String WORKER_NAME = "BackgroundWorker";
    private BackgroundWorker mWorker;

    // 这个Handler是真正在后台当中控制所有繁重任务的Handler，包括基本的网络请求和从数据库当中检索数据
    private class BackgroundWorker extends HandlerThread
    {
        // 注意这里我们必须先要将Handler初始化一下，否则在没有网络的情况下，mHandler就是一个空的
        // 因为我们对Handler的初始化是在网络可行的情况下才调用HandlerThread的start()方法，而
        // new HandlerThread().start()的方法的调用才会调用onLooperPrepared()方法，只有这样才可以
        // 初始化我们内部的Handler
        private Handler mBackgroundHandler;
        private int mStartNO, mEndNO;
        public BackgroundWorker(final int initStartNum, final int initEndNum)
        {
            super(WORKER_NAME, Process.THREAD_PRIORITY_BACKGROUND);
            this.mStartNO = initStartNum;
            this.mEndNO = initEndNum;
        }

        @Override
        protected void onLooperPrepared()
        {
            super.onLooperPrepared();
            mBackgroundHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    switch (msg.what)
                    {
//                        case START_RETRIEVE_ALL_DATA:
//                            Log.d(TAG_2, " 4. have received the message to retrieving all the list data  ");
//                            Bundle requestInfo = msg.getData();
//                            int startNum = requestInfo.getInt(KEY_REQUEST_START_NUM);
//                            int endNum = requestInfo.getInt(KEY_REQUEST_END_NUM);
//                            Log.d(TAG, "inside the mWorker event processing Looper, and the startNum : " + startNum + " , and the endNum are : " + endNum);
//                            String rangeParams = mRequestParms.getMateRange(mContext);
//                            String genderParams = mRequestParms.getMateGender(mContext);
////                            Log.d(TAG, " inside the mWorker event processing Looper --> And the request params that we stored in the SharedPreference : range : " + rangeParams + " , gender " + genderParams);
//
//                            retrieveInitialMateInfoList(startNum, endNum, rangeParams, genderParams,mLat,mLng);
//                            break;
//                        case START_RETRIEVE_DATA_WITH_RANGE_FILTER:
//                            if (!mUserList.isEmpty())
//                            {
//                                Log.d(TAG, "inside the UIEventsHandler, and the list is not empty, and we should empty it at first");
//                                mUserList.clear();
//                                // 然后通知Adapter当中的数据源已经发生变化，所以更新Adapter
//                                mUIEventsHandler.sendEmptyMessage(DATA_HAS_BEEN_UPDATED);
//                            }
//                            String range = (String) msg.obj;
//                            Log.d(TAG, " inside the workThread --> start filtering the mate list based on the range of the current user " + range);
//                            mUIEventsHandler.obtainMessage(GET_LOCATION,START_RETRIEVE_DATA_WITH_RANGE_FILTER,0,range).sendToTarget();
//                            // 每次筛选，都是从第0条开始请求最新的数据
//                            // 因为这相当于完全的重新开始了，所以我们需要将我们已经获得的UserList清空才可以
//                            if (sParamsPreference.getMateGender(mContext) != null) {
//                                retrieveInitialMateInfoList(0, 9, range, sParamsPreference.getMateGender(mContext));
//                            }
//
//                            break;
//                        case START_RETRIEVE_DATA_WITH_GENDER_FILTER:
//                            if (!mUserList.isEmpty())
//                            {
//                                Log.d(TAG, " inside the UIEventsHandler, and the list is not empty, and we should empty it at first");
//                                mUserList.clear();
//                                mUIEventsHandler.sendEmptyMessage(DATA_HAS_BEEN_UPDATED);
//                            }
//                            String gender = (String) msg.obj;
//                            Log.d(TAG, " inside the workThread --> start filtering the mate list based on the gender of the current user " + gender);
//                            if (sParamsPreference.getMateRange(mContext) != null) {
//                                // TODO: 现在我们正式完成了筛选的工作了，但是却还有一个问题，那就是我们
//                                // TODO: 确定所要加载的条目
//                                retrieveInitialMateInfoList(0, 9, sParamsPreference.getMateRange(mContext), gender,mLat,mLng);
//
//                            }
//
//                            break;

                        // TODO: ------------------------UNCOMMENT LATER-------------------------------------------------------------
//                        case UPDATE_LOCAL_MATE_TABLE:
//                            // 更新我们所获得的本地的数据库
//                            if (!mUpdateList.isEmpty())
//                            {
//                                mMateListAdapterateDaoImpl.updateMateInfoBatch(mUpdateList);
//                            }
//
//                            if (!mInsertList.isEmpty())
//                            {
//                                long insertResult = mMateListAdapterateDaoImpl.insertMateItemBatch(mInsertList);
//                                if (insertResult == -1)
//                                {
//                                    mMateListAdapterateDaoImpl.updateMateInfoBatch(mInsertList);
//                                }
//                            }
//
//                            break;
                        // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

                    }
                }
            };

            // TODO: 当我们需要处理缓存时，我们就需要重新设计这里了，因为我们还需要考虑到我们曾经加载过的历史数据
            // 这里，我们需要理清一个基本前提的逻辑，那就是我们现在
            // 是没有保持任何缓存的，所以我们在任何时候进入到这个应用的时候，也就是onResume()方法当中的时候，
            // 我们仅仅只是单纯的加载我们所需要的最新的10条数据就可以了
            if (Utils.networkAvaiable(mContext))
            {
                Log.d(TAG_2, " 2. on Background handler start, we start retrieving all data by default ");
                fetchAllData(mStartNO, mEndNO);
            }
        }

        public void fetchAllData(final int startNum, final int endNum)
        {
            if (null != mBackgroundHandler)
            {
                Log.d(TAG_2, " 3. inside the workThread, and the startNum and the endNum we need to retrieve are : " + startNum + " , " + endNum);
//                Message requetMsg = mBackgroundHandler.obtainMessage(START_RETRIEVE_ALL_DATA);
//                Bundle data = new Bundle();
//                data.putInt(KEY_REQUEST_START_NUM, startNum);
//                data.putInt(KEY_REQUEST_END_NUM, endNum);
//                requetMsg.setData(data);
//                mBackgroundHandler.sendMessage(requetMsg);
                mUIEventsHandler.obtainMessage(GET_LOCATION,START_RETRIEVE_ALL_DATA,0).sendToTarget();
            }
        }

        public void fetchDataWithRangeFilter(String range)
        {
            // 当用户添加了筛选之后，我们现在就是一个重新请求的过程了，因此我们需要先将我们之前获得的数据清除才可以
            Log.d(TAG, " inside the mWorker --> fetchDataWithRangeFiltered --> the range data we get are : " + range);
            if (! TextUtils.isEmpty(range) && mBackgroundHandler != null)
            {
                mBackgroundHandler.obtainMessage(START_RETRIEVE_DATA_WITH_RANGE_FILTER, range).sendToTarget();
            }
        }

        public void fetchDataWithGenderFiltered(String gender)
        {
            // 同筛选距离的过程一样，我们也是需要先将我们之前的List清除掉，然后开始重新请求(因为我们不清楚我们之前
            // 的请求过程是否时成功的)
            Log.d(TAG, " inside the mWorker --> fetchDataWithGenderFiltered --> the gender data we get are : " + gender);
            if (! TextUtils.isEmpty(gender) && mBackgroundHandler != null)
            {
                mBackgroundHandler.obtainMessage(START_RETRIEVE_DATA_WITH_GENDER_FILTER, gender).sendToTarget();
            }
        }

        // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//        /**
//         * 用于将我们得到的mateList来更新我们创建的本地数据库
//         *
//         */
//        public void updateMateTable()
//        {
//            mBackgroundHandler.sendEmptyMessage(UPDATE_LOCAL_MATE_TABLE);
//        }
        // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

        // 这个方法暂时不使用，因为这个方法会导致我们的任务处理终止
        public void exit()
        {
            if (null != mBackgroundHandler)
            {
                mBackgroundHandler.getLooper().quit();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        // 将所有的预筛选参数全部置空
        sParamsPreference.setMateRange(mContext, "");
        sParamsPreference.setMateGender(mContext, "");
        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_SAVED_LISTVIEW, mUserList);
        outState.putBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_LOAD_MORE, mLoadMore);
        outState.putBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_REFRESH, mRefresh);
        outState.putBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_INSTANCE, true);
    }


    //TODO:有问题
    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener = new PullToRefreshBase.OnRefreshListener2<ListView>()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            String label = NearbyFragmentsCommonUtils.getLastedTime(mContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            if (Utils.networkAvaiable(getActivity()))
            {
                mRefresh = true;
                mLoadMore = false;
                if (null != mWorker)
                {
                    // 跟分析球厅RoomFragment当中请求最新数据的原理一样，当用户进行下拉刷新
                    // 的时候，一定是再要求最新的数据，那么我们一定是要从第0条开始加载，一次加载10条，
                    // 即从0到9
                    mWorker.fetchAllData(0, 9);
                }
            } else
            {
                mUIEventsHandler.sendEmptyMessage(NO_NETWORK);
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            Log.d(TAG, " the user has touched the end of the current list in the BilliardsNearbyMateFragment ");
            String label = NearbyFragmentsCommonUtils.getLastedTime(mContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            mLoadMore = true;

            mCurrentPos = mUserList.size();

            // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//            mInsertList.clear();
//            mUpdateList.clear();
            // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

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
            if (Utils.networkAvaiable(getActivity()))
            {
                // 网络可行的情况,我们直接在我们已经得到的startNum和endNum基础之上进行数据的请求
                // 我们应该在这里首先判断，用户是都已经添加了筛选参数，
                // 如果用户已经添加了筛选参数，则我们需要在筛选参数的基础之上进行网络请求的工作
                // 用户的请求应该是累加的，即用户真正希望的效果是用户在选择了"500米以内"之后
                // 再选择"男"是可以选出“500米以内的男性球友”,而不是每次只能选出一个
                // 但是我们并不在这里进行判断，而是把这个操作也代理处理因为我们每次加载都是需要判断
                // 用户当前是否添加了筛选的参数，所以我们直接在mWorker当中创建一个函数，这个函数
                // 会接受筛选的参数，然后直接根据加载的开始条数和结束条数就可以了
                if (null != mWorker)
                {
                    mWorker.fetchAllData(mStartNum, mEndNum);
                }
            } else
            {
                // 当网络不可行时，我们需要告诉用户当前网络不可行
                mUIEventsHandler.sendEmptyMessage(NO_NETWORK);
            }
            // TODO: 我们现在暂时不需要在网络不可行的时候从本地数据库当中检索数据，还是直接从网络当中进行请求
            // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//            else
//            {
//                List<NearbyMateSubFragmentUserBean> localRetrieveList = mMateListAdapterateDaoImpl.getMateList(mStartNum, 10);
//
//                if (! localRetrieveList.isEmpty())
//                {
//                    Log.d(TAG, " the current net is unavailable, and the list size we get are : " + localRetrieveList.size());
//                    mUIEventsHandler.obtainMessage(DATA_RETRIEVE_SUCCESS, localRetrieveList).sendToTarget();
//                } else
//                {
//                    mUIEventsHandler.sendEmptyMessage(DATA_RETRIEVE_FAILED);
//                }
            // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//            }
        }
    };


    /**
     * 初始化定位,用高德SDK获取经纬度，准确率貌似更高点，
     * 之后可能会加功能，会用到高德的SDK
     */
    public void getLocation() {

        mLocationManagerProxy = LocationManagerProxy.getInstance(getActivity());

        //此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        //注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
        //在定位结束后，在合适的生命周期调用destroy()方法
        //其中如果间隔时间为-1，则定位只定一次
        mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, -1, 15, new AMapLocationListener() {
                    @Override
                    public void onLocationChanged(AMapLocation aMapLocation) {
                        if(aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0){
                            //获取位置信息
                            double latitude = aMapLocation.getLatitude();
                            double longitude = aMapLocation.getLongitude();

                            Log.d("wy","latitude ->" + latitude);
                            Log.d("wy","longitude ->" + longitude);
                            // 我们此时可以将我们获取到的当前用户的位置信息用来进行球厅的位置筛选操作
                            sParamsPreference.ensurePreference(mContext);
                            sParamsPreference.setRoomLati(mContext, (float) latitude);
                            sParamsPreference.setRoomLongi(mContext, (float) longitude);

                            Bundle args = new Bundle();
                            args.putFloat("lat", (float) latitude);
                            args.putFloat("lng", (float) longitude);
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
                });

        mLocationManagerProxy.setGpsEnable(false);
    }

    /**
     * 由于服务器是按降序排序，但是从网络获取到的json却是升序，所以重新排序一下
     */
    private class DescComparator implements Comparator<NearbyMateSubFragmentUserBean> {

        @Override
        public int compare(NearbyMateSubFragmentUserBean lhs, NearbyMateSubFragmentUserBean rhs) {
            int lhsUserId = Integer.valueOf(lhs.getUserId());
            int rhsUserId = Integer.valueOf(rhs.getUserId());
            return lhsUserId > rhsUserId ? -1 : 1;
        }
    }



}


















