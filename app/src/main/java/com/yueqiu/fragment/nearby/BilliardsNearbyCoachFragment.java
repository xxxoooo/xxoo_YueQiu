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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.adapter.NearbyCoauchSubFragmentListAdapter;
import com.yueqiu.bean.NearbyCoauchSubFragmentCoauchBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.nearby.common.NearbyPopBasicClickListener;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.fragment.nearby.common.NearbyParamsPreference;
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
 * Created by scguo on 14/12/30.
 * <p/>
 * 用于SearchActivity当中的教练子Fragment的实现
 */
@SuppressLint("ValidFragment")
public class BilliardsNearbyCoachFragment extends Fragment
{
    private static final String TAG = "BilliardsNearbyCoauchFragment";

    private static final String FRAGMENT_TAG = "BilliardsNearbyAssistCoauchFragment";

    // 这是用于整个BilliardsSearchCoauchFragment当中的layout view
    private View mView;
    private Button mBtnAbility, mBtnKinds;

    private PullToRefreshListView mCoauchListView;

    private static Context sContext;

    @SuppressLint("ValidFragment")
    public BilliardsNearbyCoachFragment()
    {
    }

    private static final String PARAMS_KEY = "BilliardsNearbyCoauchFragment";

    public static BilliardsNearbyCoachFragment newInstance(Context context, String params)
    {
        sContext = context;
        BilliardsNearbyCoachFragment instance = new BilliardsNearbyCoachFragment();

        Bundle args = new Bundle();
        args.putString(PARAMS_KEY, params);
        instance.setArguments(args);

        return instance;
    }
    private boolean mNetworkAvailable;

    private BackgroundWorkerThread mWorker;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mNetworkAvailable = Utils.networkAvaiable(sContext);

    }

    private String mArgs;

    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;

    private List<NearbyCoauchSubFragmentCoauchBean> mCoauchList = new ArrayList<NearbyCoauchSubFragmentCoauchBean>();
    private NearbyCoauchSubFragmentListAdapter mCoauchListAdapter;
    private NearbyPopBasicClickListener mClickListener;

    private NearbyFragmentsCommonUtils.ControlPopupWindowCallback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_nearby_coauch_layout, container, false);

        NearbyFragmentsCommonUtils.initViewPager(sContext, mView, R.id.coauch_fragment_gallery_pager, R.id.coauch_fragment_gallery_pager_indicator_group);

        mClickListener = new NearbyPopBasicClickListener(sContext,mUIEventsHandler,sParamsPreference);
        (mBtnAbility = (Button) mView.findViewById(R.id.btn_coauch_ability)).setOnClickListener(mClickListener);
        (mBtnKinds = (Button) mView.findViewById(R.id.btn_coauch_kinds)).setOnClickListener(mClickListener);

        mCallback = mClickListener;

        mCoauchListView = (PullToRefreshListView) mView.findViewById(R.id.search_coauch_subfragment_list);
        mCoauchListView.setMode(PullToRefreshBase.Mode.BOTH);
        mCoauchListView.setOnRefreshListener(mOnRefreshListener);

        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        mPreTextView = (TextView) mView.findViewById(R.id.pre_text);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);

        Bundle args = getArguments();
        mArgs = args.getString(PARAMS_KEY);

        // TODO: 这里加载的是测试数据,暂时还不能删除这个方法，因为我们还要查看总的UI加载效果
//        initListViewTestData();
        mCoauchListAdapter = new NearbyCoauchSubFragmentListAdapter(sContext, (ArrayList<NearbyCoauchSubFragmentCoauchBean>) mCoauchList);
        Log.d(TAG, " the source list content are : " + mCoauchList.size());
        mCoauchListView.setAdapter(mCoauchListAdapter);
        mCoauchListAdapter.notifyDataSetChanged();

        mWorker = new BackgroundWorkerThread();
        if (Utils.networkAvaiable(sContext))
        {
            mLoadMore = false;
            mRefresh = false;
            if (mWorker != null && mWorker.getState() == Thread.State.NEW)
            {
                mWorker.start();
            }
        } else
        {
            mUIEventsHandler.sendEmptyMessage(NO_NETWORK);
        }


        return mView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

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
        super.onDestroy();
    }

    private static NearbyParamsPreference sParamsPreference = NearbyParamsPreference.getInstance();

    private void retrieveInitialCoauchInfo(final String clazzRequest, String levelRequest, final int startNo, final int endNo)
    {
        if (!mNetworkAvailable)
        {
            mUIEventsHandler.obtainMessage(STATE_FETCH_DATA_FAILED,
                    sContext.getResources().getString(R.string.network_not_available)).sendToTarget();
            return;
        }
        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();

        if (! TextUtils.isEmpty(clazzRequest))
        {
            requestParams.put("clazz", clazzRequest);
        }
        if (! TextUtils.isEmpty(levelRequest))
        {
            requestParams.put("level", levelRequest);
        }

        requestParams.put("start_no", startNo + "");
        requestParams.put("end_no", endNo + "");

        List<NearbyCoauchSubFragmentCoauchBean> cacheCoauchList = new ArrayList<NearbyCoauchSubFragmentCoauchBean>();

        String rawResult = HttpUtil.urlClient(HttpConstants.NearbyCoauch.URL, requestParams, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the raw result we get for the Coauch are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult))
        {
            try
            {
                JSONObject initialResultJson = new JSONObject(rawResult);
                if (! initialResultJson.isNull("code"))
                {
                    final int status = initialResultJson.getInt("code");
                    if (status == HttpConstants.ResponseCode.NORMAL)
                    {
                        JSONObject resultJsonObj = initialResultJson.getJSONObject("result");
                        Log.d(TAG, " the final json data we need to parse are : " + resultJsonObj);
                        JSONArray dataArr = resultJsonObj.getJSONArray("list_data");
                        final int len = dataArr.length();
                        int i;
                        for (i = 0; i < len; ++i)
                        {
                            JSONObject dataUnit = dataArr.getJSONObject(i);
                            Log.d(TAG, " the sub data json unit we get are : " + dataUnit);

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
                                    NearbyFragmentsCommonUtils.parseGenderStr(sContext, sex),
                                    String.valueOf(range),
                                    NearbyFragmentsCommonUtils.parseCoauchLevel(sContext, level),
                                    NearbyFragmentsCommonUtils.parseBilliardsKinds(sContext, kinds));

                            cacheCoauchList.add(coauchBean);
                        }

                        mUIEventsHandler.obtainMessage(STATE_FETCH_DATA_SUCCESS, cacheCoauchList).sendToTarget();

                        mUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                    } else if (status == HttpConstants.ResponseCode.TIME_OUT)
                    {
                        mUIEventsHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                        mUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                    } else if (status == HttpConstants.ResponseCode.NO_RESULT)
                    {
                        mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        mUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                    } else
                    {
                        Message errorMsg = mUIEventsHandler.obtainMessage(PublicConstant.REQUEST_ERROR);
                        Bundle errorData = new Bundle();
                        String errorStr = initialResultJson.getString("msg");
                        Log.d(TAG, "inside the couach list, and the error info we get are : " + errorStr);
                        if (! TextUtils.isEmpty(errorStr))
                        {
                            errorData.putString(KEY_REQUEST_ERROR_MSG, errorStr);
                        }
                        errorMsg.setData(errorData);
                        mUIEventsHandler.sendMessage(errorMsg);
                        mUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                        // 下面的写法理论上是正确的，但是当“msg”为空的时候，Bundle并不为空，
                        // 所以UIHandler当中仍然会接收到这里传送的数据，但是由于msg是空的,
                        // 所以我们还是要使用最原始的初始化Message Bundle的做法
                        // 以下的传递数据的方法虽然简练，但是不正确，会导致空数据的传输,谨慎
//                        mUIEventsHandler.obtainMessage(PublicConstant.REQUEST_ERROR, initialResultJson.getString("msg")).sendToTarget();
                    }
                } else
                {
                    mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    mUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                mUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                Log.d(TAG, " exception happened while we parsing the json object we retrieved, and the reason are : " + e.toString());
            }
        }
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
    private static final int UI_HIDE_PROGRESS = 1 << 7;

    private  Handler mUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UI_SHOW_PROGRESS:
                    showProgress();
                    Log.d(TAG, " start showing the progress bar in the Coauch fragment ");
                    break;
                case UI_HIDE_PROGRESS:
                    mCoauchListAdapter.notifyDataSetChanged();
                    hideProgress();
                    if (mCoauchListView.isRefreshing())
                    {
                        mCoauchListView.onRefreshComplete();
                    }
                    Log.d(TAG, " hide the progress bar that in the coauch fragment ");
                    break;

                case STATE_FETCH_DATA_FAILED:
                    String reasonDesc = (String) msg.obj;
                    Log.d(TAG, " fail to get the data we need, and the detailed reason for that are : " + reasonDesc);
                    Toast.makeText(sContext, reasonDesc, Toast.LENGTH_SHORT).show();
                    break;
                case STATE_FETCH_DATA_SUCCESS:
                    mBeforeCount = mCoauchList.size();
                    List<NearbyCoauchSubFragmentCoauchBean> coauchList = (ArrayList<NearbyCoauchSubFragmentCoauchBean>) msg.obj;
                    for (NearbyCoauchSubFragmentCoauchBean bean : coauchList)
                    {
                        if (! mCoauchList.contains(bean))
                        {
                            mCoauchList.add(bean);
                        }
                    }
                    mAfterCount = mCoauchList.size();
                    // TODO: 在这里进行一下更新数据库的操作
                    if (mCoauchList.isEmpty())
                    {
                        Log.d(TAG, " inside the coauchFragment mUIEventsHandler --> we start to load the emptyView");
                        loadEmptyTv();
                    } else
                    {
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
                    mCoauchListAdapter.notifyDataSetChanged();
                    break;

                case RETRIEVE_COAUCH_WITH_LEVEL_FILTERED:
                    String level = (String) msg.obj;
                    Log.d(TAG, " inside the CoauchFragment UIEventsHandler --> and the level info we get are : " + level);
                    mWorker.fetchDataWithLevelFiltered(level);
                    break;

                case RETRIEVE_COAUCH_WITH_CLASS_FILTERED:
                    String clazz = (String) msg.obj;
                    mWorker.fetchDataWithClazzFiltered(clazz);

                    break;

                case DATA_HAS_BEEN_UPDATED:
                    mCoauchListAdapter.notifyDataSetChanged();
                    Log.d(TAG, " the adapter has been updated ");
                    break;

                case NO_NETWORK:
                    hideProgress();
                    if (mCoauchList.isEmpty())
                    {
                        loadEmptyTv();
                    }
                    Utils.showToast(sContext, sContext.getString(R.string.network_not_available));
                    break;
                case PublicConstant.TIME_OUT:
                    // 超时之后的处理策略
                    Utils.showToast(sContext, sContext.getString(R.string.http_request_time_out));
                    if (mCoauchList.isEmpty())
                    {
                        loadEmptyTv();
                    }
                    hideProgress();
                    break;

                case PublicConstant.NO_RESULT:
                    if (mCoauchList.isEmpty())
                    {
                        loadEmptyTv();
                    } else
                    {
                        if (mLoadMore)
                        {
                            Utils.showToast(sContext, sContext.getString(R.string.no_more_info));
                        }
                    }
                    hideProgress();
                    break;

                case PublicConstant.REQUEST_ERROR:
                    Bundle errorData = msg.getData();
                    String errorStr = errorData.getString(KEY_REQUEST_ERROR_MSG);
                    if (! TextUtils.isEmpty(errorStr))
                    {
                        Utils.showToast(sContext, errorStr);
                    } else
                    {
                        Utils.showToast(sContext, sContext.getString(R.string.http_request_error));
                    }

                    if (mCoauchList.isEmpty())
                    {
                        loadEmptyTv();
                    }
                    hideProgress();
                    break;
            }
            mCoauchListAdapter.notifyDataSetChanged();
        }
    };

    private void loadEmptyTv()
    {
        Log.d(TAG, " inside the loadEmptyTV method --> we are start loading the empty view from here ");
        if (mCoauchListView.isRefreshing())
        {
            mCoauchListView.onRefreshComplete();
        }
        NearbyFragmentsCommonUtils.setFragmentEmptyTextView(sContext, mCoauchListView, sContext.getString(R.string.search_activity_subfragment_empty_tv_str));
    }

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

    private class BackgroundWorkerThread extends HandlerThread
    {
        public BackgroundWorkerThread()
        {
            super(BACKGROUND_WORKER_NAME, Process.THREAD_PRIORITY_BACKGROUND);
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
                        case RETRIEVE_ALL_COAUCH_INFO:
                            // 通知UIHandler开始显示Dialog
                            mUIEventsHandler.sendEmptyMessage(UI_SHOW_PROGRESS);
                            String cacheClazz = sParamsPreference.getCouchClazz(sContext);
                            String cacheLevel = sParamsPreference.getCouchLevel(sContext);
                            retrieveInitialCoauchInfo(cacheClazz, cacheLevel, 0, 9);

                            break;
                        case RETRIEVE_COAUCH_WITH_CLASS_FILTERED:
                            String clazz = (String) msg.obj;
                            mUIEventsHandler.sendEmptyMessage(UI_SHOW_PROGRESS);
                            Log.d(TAG, " inside the BackgroundThread --> the clazz string we get in the CouachFragment are : " + clazz);
                            String clazzCacheLevel = sParamsPreference.getCouchLevel(sContext);
                            // 我们每次的筛选都要从零开始请求最新的数据
                            retrieveInitialCoauchInfo(clazz, clazzCacheLevel, 0, 9);

                            break;
                        case RETRIEVE_COAUCH_WITH_LEVEL_FILTERED:
                            String level = (String) msg.obj;
                            Log.d(TAG, " inside the BackgroundThread --> the level string we get in the CoauchFragment are : " + level);
                            String levelCacheClazz = sParamsPreference.getCouchClazz(sContext);
                            // 同样的道理，我们筛选的数据请求都要从零开始重新请求
                            retrieveInitialCoauchInfo(levelCacheClazz, level, 0, 9);

                            break;
                    }
                }
            };
            // 初始请求，肯定是请求最新的，即从第0条开始请求就可以了
            fetchAllData(0, 9);
        }

        public void fetchAllData(final int startNum, final int endNum)
        {
            Message requestMsg = mWorkerHandler.obtainMessage(RETRIEVE_ALL_COAUCH_INFO);
            Bundle requestData = new Bundle();
            requestData.putInt(KEY_REQUEST_START_NUM, startNum);
            requestData.putInt(KEY_REQUEST_END_NUM, endNum);
            requestMsg.setData(requestData);
            mWorkerHandler.sendMessage(requestMsg);
        }

        public void fetchDataWithClazzFiltered(String clazz)
        {
            if (! TextUtils.isEmpty(clazz))
            {
                mWorkerHandler.obtainMessage(RETRIEVE_COAUCH_WITH_CLASS_FILTERED, clazz).sendToTarget();
            }
        }

        public void fetchDataWithLevelFiltered(String level)
        {
            if (! TextUtils.isEmpty(level))
            {
                mWorkerHandler.obtainMessage(RETRIEVE_COAUCH_WITH_LEVEL_FILTERED, level).sendToTarget();
            }
        }
    }

    private boolean mLoadMore;
    private boolean mRefresh;

    private int mCurrentPos;
    private int mBeforeCount;
    private int mAfterCount;

    private int mStartNum;
    private int mEndNum;

    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener = new PullToRefreshBase.OnRefreshListener2<ListView>()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            String label = NearbyFragmentsCommonUtils.getLastedTime(sContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            if (Utils.networkAvaiable(sContext))
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
            String label = NearbyFragmentsCommonUtils.getLastedTime(sContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            mLoadMore = true;
            mCurrentPos = mCoauchList.size();
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


    // TODO: 以下是测试数据,在测试接口的时候，将以下的初始化过程删除
    private void initListViewTestData()
    {
        int i;
        for (i = 0; i < 200; ++i) {
            mCoauchList.add(new NearbyCoauchSubFragmentCoauchBean("", "", "大力水手", "男", "2000米以内", "前国家队队员", "九球"));
        }
    }
}






































































































































