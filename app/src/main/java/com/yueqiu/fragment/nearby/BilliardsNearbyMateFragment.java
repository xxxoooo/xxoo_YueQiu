package com.yueqiu.fragment.nearby;

import android.annotation.SuppressLint;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

    public static final String BILLIARD_SEARCH_TAB_NAME = "billiard_search_tab_name";
    private View mView;
    private String mArgs;
    private static Context sContext;

    private PullToRefreshListView mSubFragmentListView;

    @SuppressLint("ValidFragment")
    public BilliardsNearbyMateFragment()
    {
    }

    private static final String KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT = "keyBilliardsSearchParentFragment";

    private static NearbyParamsPreference sParamsPreference = NearbyParamsPreference.getInstance();

    public static BilliardsNearbyMateFragment newInstance(Context context, String params)
    {
        sContext = context;
        BilliardsNearbyMateFragment fragment = new BilliardsNearbyMateFragment();

        Bundle args = new Bundle();
        args.putString(KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT, params);

        return fragment;
    }

    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//    private static NearbyMateDaoImpl mMateListAdapterateDaoImpl;
    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

    // mIsHead用于控制数据的加载到List当中的方向(即加载到头部还是加载到尾部)
    private boolean mIsNetworkAvailable;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mWorker = new BackgroundWorker();
        // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//        mMateListAdapterateDaoImpl = new NearbyMateDaoImpl(sContext);
        // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

        mIsNetworkAvailable = Utils.networkAvaiable(sContext);
    }

    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;
    private List<NearbyMateSubFragmentUserBean> mUserList = new ArrayList<NearbyMateSubFragmentUserBean>();

    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//    private List<NearbyMateSubFragmentUserBean> mUpdateList = new ArrayList<NearbyMateSubFragmentUserBean>();
//    private List<NearbyMateSubFragmentUserBean> mInsertList = new ArrayList<NearbyMateSubFragmentUserBean>();
//    private List<NearbyMateSubFragmentUserBean> mDBList = new ArrayList<NearbyMateSubFragmentUserBean>();
    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

    private NearbyMateSubFragmentListAdapter mMateListAdapter;
    private NearbyPopBasicClickListener mClickListener;


    private boolean mRefresh;
    private boolean mLoadMore;

    private int mStartNum = 0;
    private int mEndNum = 9;
    // 用于定义当前MateList当中的list的position，帮助我们确定从第几条开始请求数据
    private int mCurrentPos;
    private int mBeforeCount, mAfterCount;

    public NearbyFragmentsCommonUtils.ControlPopupWindowCallback mPopupwindowCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_nearby_mate_layout, container, false);
        // then, inflate the image view pager
        NearbyFragmentsCommonUtils.initViewPager(sContext, mView, R.id.mate_fragment_gallery_pager, R.id.mate_fragment_gallery_pager_indicator_group);

        mSubFragmentListView = (PullToRefreshListView) mView.findViewById(R.id.search_sub_fragment_list);
        mSubFragmentListView.setMode(PullToRefreshBase.Mode.BOTH);
        mSubFragmentListView.setOnRefreshListener(mOnRefreshListener);

        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        mPreTextView = (TextView) mView.findViewById(R.id.pre_text);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);

        mClickListener = new NearbyPopBasicClickListener(sContext, mUIEventsHandler, sParamsPreference);
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

        mMateListAdapter = new NearbyMateSubFragmentListAdapter(sContext, (ArrayList<NearbyMateSubFragmentUserBean>) mUserList);
        mSubFragmentListView.setAdapter(mMateListAdapter);

        mMateListAdapter.notifyDataSetChanged();
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
        mView.postInvalidate();

        if (mWorker != null && mWorker.getState() == Thread.State.NEW)
        {
            Log.d(TAG, " the mWorker has started ");
            mWorker.start();
        }
    }

    @Override
    public void onPause()
    {
        // TODO: 如果此时我们请求到新的数据或者服务器端提供了消息推送的服务，我们这个时候需要
        // TODO: 以Notification的方式来通知用户消息的接收

        Log.d("mate_onpause", " inside mate fragment --> the current mate fragment is onPause ... ");
        mPopupwindowCallback.closePopupWindow();
        super.onPause();
    }

    @Override
    public void onStop()
    {
        // TODO: 我们在这里进行一些停止数据更新的操作，即停止任何同数据请求和处理的相关工作,然后再调用super.onStop()
        // TODO: 我们目前采用的策略只是简单的直接获取数据的方式，如果需要升级我们还需要通过添加BroadcastReceiver来
        // TODO: 监听数据的获取状态，然后在onStop()方法当中解注册这个BroadcastReceiver

        Log.d("mate_onstop", " inside mate fragment --> the current mate fragment is onStop ... ");
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
    private void retrieveInitialMateInfoList(final int startNo, final int endNo, final String distance, final String gender)
    {
        if (!Utils.networkAvaiable(sContext))
        {
            mUIEventsHandler.sendEmptyMessage(DATA_RETRIEVE_FAILED);
            mUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
            return;
        }

        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("start_no", startNo + "");
        requestParams.put("end_no", endNo + "");
        if (!TextUtils.isEmpty(distance))
        {
            requestParams.put("range", distance);
        }

        if (!TextUtils.isEmpty(gender))
        {
            requestParams.put("gender", gender);
        }

        List<NearbyMateSubFragmentUserBean> cacheMateList = new ArrayList<NearbyMateSubFragmentUserBean>();

        String rawResult = HttpUtil.urlClient(HttpConstants.NearbyMate.URL, requestParams, HttpConstants.RequestMethod.GET);

        Log.d(TAG, " the raw result we get for the mate fragment are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult))
        {
            try
            {
                // initialObj当中包含的是最原始的JSON data，这个Json对象当中还包含了一些包含我们的请求状态的字段值
                // 我们还需要从initialObj当中解析出我们真正需要的Json对象
                JSONObject initialObj = new JSONObject(rawResult);

                if (!initialObj.isNull("code"))
                {
                    final int statusCode = initialObj.getInt("code");
                    JSONObject resultJson = initialObj.getJSONObject("result");
                    Log.d(TAG, " the initial json object we get are : " + initialObj + " ; and the result are : " + resultJson);
                    if (statusCode == HttpConstants.ResponseCode.NORMAL)
                    {
                        Log.d(TAG, " all are ok in for now ");
                        final int dataCount = resultJson.getInt("count");
                        Log.d(TAG, " the dataCount we get are : " + dataCount);
                        JSONArray dataList = resultJson.getJSONArray("list_data");
                        int i;
                        for (i = 0; i < dataCount; ++i)
                        {
                            JSONObject dataObj = (JSONObject) dataList.get(i);
                            String imgUrl = dataObj.getString("img_url");
                            String sex = dataObj.getString("sex");
                            String userName = dataObj.getString("username");
                            String userId = dataObj.getString("user_id");
                            int range = dataObj.getInt("range");
                            String district = dataObj.getString("district");
                            NearbyMateSubFragmentUserBean mateUserBean = new NearbyMateSubFragmentUserBean(userId, imgUrl, userName, NearbyFragmentsCommonUtils.parseGenderStr(sContext, sex), district, String.valueOf(range));

                            cacheMateList.add(mateUserBean);
                        }
                        // TODO: 数据获取完之后，我们需要停止显示ProgressBar(这部分功能还需要进一步测试)
                        mUIEventsHandler.obtainMessage(DATA_RETRIEVE_SUCCESS, cacheMateList).sendToTarget();

                        mUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
                    } else if (statusCode == HttpConstants.ResponseCode.TIME_OUT)
                    {
                        mUIEventsHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                    } else if (statusCode == HttpConstants.ResponseCode.NO_RESULT)
                    {
                        mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                    } else
                    {
                        mUIEventsHandler.obtainMessage(PublicConstant.REQUEST_ERROR, initialObj.getString("msg")).sendToTarget();
                    }
                } else
                {
                    mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                }
            } catch (JSONException e)
            {
                e.printStackTrace();
                Log.d(TAG, " exception happened in parsing the json data we get, and the detailed reason are : " + e.toString());
            }
        }
    }

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

    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//    private static final int UPDATE_LOCAL_MATE_TABLE = 1 << 9;
    // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

    private static final int USE_CACHE = 1 << 10;

    private static final int NO_NETWORK = 1 << 11;

    // 这个Handler主要是用于处理UI相关的事件,例如涉及到UI的事件的直接处理，例如Toast或者ProgressBar的显示控制
    private Handler mUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case DATA_RETRIEVE_FAILED:
                    Toast.makeText(sContext, sContext.getResources().getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                    hideProgress();
                    break;
                case DATA_RETRIEVE_SUCCESS:
                    // TODO: 我们会将我们从网络上以及从本地数据库当中检索到的数据
                    // TODO: 都会通过消息通知的形式发送到这里，因为这样就可以保证我们所有的涉及到UI工作都是在UI线程当中完成的
                    mBeforeCount = mUserList.size();
                    List<NearbyMateSubFragmentUserBean> mateList = (ArrayList<NearbyMateSubFragmentUserBean>) msg.obj;
                    for (NearbyMateSubFragmentUserBean mateBean : mateList)
                    {
                        if (!mUserList.contains(mateBean))
                        {
                            mUserList.add(mateBean);
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

                    mAfterCount = mUserList.size();
                    Log.d(TAG, " mUIEventsHandler --> the final user list size are : " + mAfterCount);

                    if (mUserList.isEmpty())
                    {
                        loadEmptyTv();
                    } else
                    {
                        // 如果触发DATA_RETRIEVE_SUCCESS的事件是来自用户的下拉刷新
                        // 事件，那么我们需要根据我们得到更新后的List来判断数据的加载是否是成功的(上拉刷新是不需要判断的，
                        // TODO: 上拉刷新理论上所有的数据都应该保存到本地的数据库当中,如果没有保存的话，那么就是我们程序的问题了)
                        if (mRefresh)
                        {
                            if (mAfterCount == mBeforeCount)
                            {
                                Utils.showToast(sContext, sContext.getString(R.string.no_newer_info));
                            } else
                            {
                                Utils.showToast(sContext, sContext.getString(R.string.have_already_update_info, mAfterCount - mBeforeCount));
                            }
                        }
                    }
                    break;

                case USE_CACHE:
                    List<NearbyMateSubFragmentUserBean> dbCacheList = (ArrayList<NearbyMateSubFragmentUserBean>) msg.obj;
                    mUserList.addAll(dbCacheList);

                    mMateListAdapter.notifyDataSetChanged();
                    break;

                case NO_NETWORK:
                    Utils.showToast(sContext, sContext.getString(R.string.network_not_available));

                    if (mUserList.isEmpty())
                    {
                        loadEmptyTv();
                    }

                    break;
                case SHOW_PROGRESSBAR:
                    showProgress();
                    Log.d(TAG, " start showing the progress bar ");

                    break;
                case HIDE_PROGRESSBAR:

                    mMateListAdapter.notifyDataSetChanged();
                    hideProgress();
                    Log.d(TAG, " hiding the progress bar ");
                    break;

                case START_RETRIEVE_DATA_WITH_GENDER_FILTER:
                    String gender = (String) msg.obj;
                    Log.d(TAG, " inside the mate fragment UIEventsHandler --> the gender filtering string we get are : " + gender);
                    mWorker.fetchDataWithGenderFiltered(gender);

                    break;

                case START_RETRIEVE_DATA_WITH_RANGE_FILTER:
                    String range = (String) msg.obj;
                    Log.d(TAG, " inside the mate fragment UIEventsHandler --> the range filtering string we get are : " + range);
                    mWorker.fetchDataWithRangeFilter(range);
                    break;

                case DATA_HAS_BEEN_UPDATED:
                    mMateListAdapter.notifyDataSetChanged();
                    Log.d(TAG, " the adapter has been notified ");
                    break;

                case PublicConstant.TIME_OUT:
                    // 超时之后的处理策略
                    Utils.showToast(sContext, sContext.getString(R.string.http_request_time_out));
                    if (mUserList.isEmpty()) {
                        loadEmptyTv();
                    }
                    break;

                case PublicConstant.NO_RESULT:
                    if (mUserList.isEmpty()) {
                        loadEmptyTv();
                    } else {
                        if (mLoadMore) {
                            Utils.showToast(sContext, sContext.getString(R.string.no_more_info));
                        }
                    }
                    break;

                case PublicConstant.REQUEST_ERROR:
                    Bundle errorData = msg.getData();
                    if (null != errorData) {
                        Utils.showToast(sContext, (String) msg.obj);
                    } else {
                        Utils.showToast(sContext, sContext.getString(R.string.http_request_error));
                    }
                    if (mUserList.isEmpty()) {
                        loadEmptyTv();
                    }
                    break;
            }
            mMateListAdapter.notifyDataSetChanged();
        }
    };

    private void loadEmptyTv()
    {
        // 先把正在显示的ProgressBar隐藏掉
        if (mSubFragmentListView.isRefreshing())
            mSubFragmentListView.onRefreshComplete();

        NearbyFragmentsCommonUtils.setFragmentEmptyTextView(sContext, mSubFragmentListView, sContext.getString(R.string.search_activity_subfragment_empty_tv_str));
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
        private Handler mBackgroundHandler;

        public BackgroundWorker()
        {
            super(WORKER_NAME, Process.THREAD_PRIORITY_BACKGROUND);
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
                        case START_RETRIEVE_ALL_DATA:
                            Log.d(TAG, " have received the message to retrieving all the list data  ");
                            // 开始获取数据，我们首先将我们的ProgressBar显示出来
                            mUIEventsHandler.sendEmptyMessage(SHOW_PROGRESSBAR);
                            retrieveInitialMateInfoList(0, 9, "", "");

                            break;
                        case START_RETRIEVE_DATA_WITH_RANGE_FILTER:
                            String range = (String) msg.obj;
                            Log.d(TAG, " inside the workThread --> start filtering the mate list based on the range of the current user " + range);

                            mUIEventsHandler.sendEmptyMessage(SHOW_PROGRESSBAR);
                            // TODO: 对于筛选工作，我们通过网络请求来完成
                            if (!TextUtils.isEmpty(sParamsPreference.getMateGender(sContext))) {
                                retrieveInitialMateInfoList(0, 9, range, sParamsPreference.getMateGender(sContext));
                            }

                            break;
                        case START_RETRIEVE_DATA_WITH_GENDER_FILTER:
                            String gender = (String) msg.obj;
                            Log.d(TAG, " inside the workThread --> start filtering the mate list based on the gender of the current user " + gender);
                            mUIEventsHandler.sendEmptyMessage(SHOW_PROGRESSBAR);

                            if (!TextUtils.isEmpty(sParamsPreference.getMateRange(sContext))) {
                                // TODO: 现在我们正式完成了筛选的工作了，但是却还有一个问题，那就是我们
                                // TODO: 确定所要加载的条目
                                retrieveInitialMateInfoList(0, 9, sParamsPreference.getMateRange(sContext), gender);
                            }

                            break;

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
            fetchAllData();
        }

        public void fetchAllData()
        {
            mBackgroundHandler.sendEmptyMessage(START_RETRIEVE_ALL_DATA);
        }

        public void fetchDataWithRangeFilter(String range)
        {
            mBackgroundHandler.obtainMessage(START_RETRIEVE_DATA_WITH_RANGE_FILTER, range).sendToTarget();
        }

        public void fetchDataWithGenderFiltered(String gender)
        {
            mBackgroundHandler.obtainMessage(START_RETRIEVE_DATA_WITH_GENDER_FILTER, gender).sendToTarget();
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

        public void exit()
        {
            mBackgroundHandler.getLooper().quit();
        }
    }

    @Override
    public void onDestroy()
    {
        if (null != mWorker)
        {
            mWorker.exit();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

    }

    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener = new PullToRefreshBase.OnRefreshListener2<ListView>()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            String label = NearbyFragmentsCommonUtils.getLastedTime(sContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (Utils.networkAvaiable(sContext))
                    {
                        mRefresh = true;
                        mLoadMore = false;
                        // 跟分析球厅RoomFragment当中请求最新数据的原理一样，当用户进行下拉刷新
                        // 的时候，一定是再要求最新的数据，那么我们一定是要从第0条开始加载，一次加载10条，
                        // 即从0到9
                        retrieveInitialMateInfoList(0, 9, "", "");
                    } else
                    {
                        mUIEventsHandler.sendEmptyMessage(NO_NETWORK);
                    }
                }
            }).start();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            Log.d(TAG, " the user has touched the end of the current list in the BilliardsNearbyMateFragment ");
            String label = NearbyFragmentsCommonUtils.getLastedTime(sContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            mLoadMore = true;
            mRefresh = false;
            mCurrentPos = mUserList.size();

            // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------
//            mInsertList.clear();
//            mUpdateList.clear();
            // TODO: ------------------------UNCOMMENT LATER--------------------------------------------------------------

            if (mBeforeCount != mAfterCount)
            {
                mStartNum = mEndNum + (mAfterCount - mBeforeCount);
                mEndNum += 10 + (mAfterCount - mBeforeCount);
            } else
            {
                mStartNum = mEndNum + 1;
                mEndNum += 10;
            }

            if (Utils.networkAvaiable(sContext))
            {
                // 网络可行的情况,我们直接在我们已经得到的startNum和endNum基础之上进行数据的请求
                // TODO: 我们应该在这里首先判断，用户是都已经添加了筛选参数，
                // TODO: 如果用户已经添加了筛选参数，则我们需要在筛选参数的基础之上进行网络请求的工作
                // TODO: 用户的请求应该是累加的，即用户真正希望的效果是用户在选择了"500米以内"之后
                // TODO: 再选择"男"是可以选出“500米以内的男性球友”,而不是每次只能选出一个
                // TODO: ??????????Implemented??????????????



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


    // TODO: the following are just for testing
    // TODO: and remove all of them out with the true data we retrieved from RESTful WebService
    private void initListViewDataSrc()
    {
        int i;
        for (i = 0; i < 100; ++i) {
            mUserList.add(new NearbyMateSubFragmentUserBean("", "", "月夜流沙", "男", "昌平区", "20000米以内"));
        }
    }
}


















