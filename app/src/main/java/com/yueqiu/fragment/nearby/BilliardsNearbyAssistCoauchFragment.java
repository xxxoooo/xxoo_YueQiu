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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.adapter.NearbyAssistCoauchSubFragmentListAdapter;
import com.yueqiu.adapter.NearbyPopupBaseAdapter;
import com.yueqiu.bean.NearbyAssistCoauchSubFragmentBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.nearby.common.BasicCLickListener;
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
import java.util.Arrays;
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
    private static Context sContext;

    @SuppressLint("ValidFragment")
    public BilliardsNearbyAssistCoauchFragment()
    {
    }

    private static final String KEY_BILLIARDS_NEARBY_PARENT_FRAGMENT = "keyBilliardsNearbyAssistCoauchFragment";

    public static BilliardsNearbyAssistCoauchFragment newInstance(Context context, String params)
    {
        sContext = context;
        BilliardsNearbyAssistCoauchFragment instance = new BilliardsNearbyAssistCoauchFragment();
        Bundle args = new Bundle();
        args.putString(KEY_BILLIARDS_NEARBY_PARENT_FRAGMENT, params);

        return instance;
    }
    private boolean mNetworkAvailable;

    private BackgroundWorkerHandler mWorker;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mNetworkAvailable = Utils.networkAvaiable(sContext);
        mWorker = new BackgroundWorkerHandler();

    }

    private View mView;

    private Button mBtnDistance, mBtnCost, mBtnKinds, mBtnLevel;
    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;

    private NearbyAssistCoauchSubFragmentListAdapter mAssistCoauchListAdapter;

    private List<NearbyAssistCoauchSubFragmentBean> mAssistCoauchList = new ArrayList<NearbyAssistCoauchSubFragmentBean>();
    private BasicCLickListener mClickListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        mView = inflater.inflate(R.layout.fragment_nearby_assistcoauch_layout, container, false);

        NearbyFragmentsCommonUtils.initViewPager(sContext, mView, R.id.assistcoauch_fragment_gallery_pager, R.id.assistcoauch_fragment_gallery_pager_indicator_group);

        mClickListener = new BasicCLickListener(sContext,mUIEventsHandler,sParamsPreference);
        (mBtnDistance = (Button) mView.findViewById(R.id.btn_assistcoauch_distance)).setOnClickListener(mClickListener);
        (mBtnCost = (Button) mView.findViewById(R.id.btn_assistcoauch_cost)).setOnClickListener(mClickListener);
        (mBtnKinds = (Button) mView.findViewById(R.id.btn_assistcoauch_kinds)).setOnClickListener(mClickListener);
        (mBtnLevel = (Button) mView.findViewById(R.id.btn_assistcoauch_level)).setOnClickListener(mClickListener);

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

        return mView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (null != mWorker && mWorker.getState() == Thread.State.NEW) {
            mWorker.start();
        }
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    private NearbyParamsPreference sParamsPreference = NearbyParamsPreference.getInstance();


    private void retrieveAllInitialAssistCoauchInfo(final int startNo, final int endNo)
    {
        if (!mNetworkAvailable) {
            mUIEventsHandler.obtainMessage(STATE_FETCH_DATA_FAILED,
                    sContext.getResources().getString(R.string.network_not_available)).sendToTarget();

            return;
        }

        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("start_no", startNo + "");
        requestParams.put("end_no", endNo + "");
        String rawResult = HttpUtil.urlClient(HttpConstants.NearbyAssistCoauch.URL, requestParams, HttpConstants.RequestMethod.GET);

        Log.d(TAG, " the raw result we get for the AssistCoauch info are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult))
        {
            try
            {
                JSONObject initialResultJsonObj = new JSONObject(rawResult);
                Log.d(TAG, " the initial resulted json data are : " + initialResultJsonObj.toString());

                if (! initialResultJsonObj.isNull("code"))
                {
                    final int status = initialResultJsonObj.getInt("code");
                    if (status == HttpConstants.ResponseCode.NORMAL)
                    {
                        JSONObject resultJsonObj = initialResultJsonObj.getJSONObject("result");
                        Log.d(TAG, " the really part we need to parse are : " + resultJsonObj);
                        JSONArray resultArr = resultJsonObj.getJSONArray("list_data");
                        final int count = resultArr.length();
                        int i;
                        for (i = 0; i < count; ++i)
                        {
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
                                    NearbyFragmentsCommonUtils.parseGenderStr(sContext, sex),
                                    NearbyFragmentsCommonUtils.parseBilliardsKinds(sContext, kinds),
                                    money,
                                    String.valueOf(range)
                            );
                            mAssistCoauchList.add(assistCoauchBean);
                        }
                        // 然后我们直接将我们得到助教List直接发送到mUIEventHandler进行处理，因为我们只能在MainUIThread当中进行有关于数据的更新操作
                        mUIEventsHandler.obtainMessage(STATE_FETCH_DATA_SUCCESS, mAssistCoauchList).sendToTarget();

                    } else if (status == HttpConstants.ResponseCode.TIME_OUT)
                    {
                        // 进行超时处理的请求
                        mUIEventsHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                    } else if (status == HttpConstants.ResponseCode.NO_RESULT)
                    {
                        mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                    } else
                    {
                        mUIEventsHandler.obtainMessage(PublicConstant.REQUEST_ERROR,
                                initialResultJsonObj.getString("msg")).sendToTarget();

                        // 这里需要注意的是，服务器端可能会把msg内容置为null，即错误了，但是没有返回任何内容
                    }
                    // TODO: 到这里，我们基本上就已经完成了数据检索的工作了，现在我们需要的就是通知用户已经完成数据检索工作，我们可以取消ProgressDialog的显示了
                    mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, " exception happened in parsing the json data we get, and the detailed reason are : " + e.toString());
            }
        }
    }

    private void loadEmptyTv()
    {
        NearbyFragmentsCommonUtils.setFragmentEmptyTextView(sContext, mListView, sContext.getString(R.string.search_activity_subfragment_empty_tv_str));
    }

    private static final int DATA_HAS_BEEN_UPDATED = 1 << 10;

    private static final int RETRIEVE_ALL_RAW_INFO = 1 << 1;
    public static final int RETREIVE_INFO_WITH_KINDS_FILTERED = 1 << 2;
    public static final int RETRIEVE_INFO_WITH_LEVEL_FILTERED = 1 << 3;
    public static final int RETRIEVE_INFO_WITH_PRICE_FILTERED = 1 << 4;
    public static final int RETRIEVE_INFO_WITH_DISTANCE_FILTERED = 1 << 5;

    private static final int UI_SHOW_DIALOG = 1 << 6;
    private static final int UI_HIDE_DIALOG = 1 << 7;

    private static final int STATE_FETCH_DATA_SUCCESS = 1 << 8;
    private static final int STATE_FETCH_DATA_FAILED = 1 << 9;

    private Handler mUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UI_SHOW_DIALOG:
                    Log.d(TAG, " start showing the dialog ");
                    showProgress();
                    break;
                case UI_HIDE_DIALOG:
                    Log.d(TAG, " hiding the dialog ");
                    mAssistCoauchListAdapter.notifyDataSetChanged();
                    hideProgress();
                    break;

                case STATE_FETCH_DATA_FAILED:
                    String reasonStr = (String) msg.obj;
                    Log.d(TAG, " fail to fetch the data, and the reason are : " + reasonStr);
                    break;

                case STATE_FETCH_DATA_SUCCESS:
                    List<NearbyAssistCoauchSubFragmentBean> asList = (ArrayList<NearbyAssistCoauchSubFragmentBean>) msg.obj;
                    final int size = asList.size();
                    int i;
                    for (i = 0; i < size; ++i)
                    {
                        if (mAssistCoauchList.contains(asList.get(i)))
                        {
                            mAssistCoauchList.add(asList.get(i));
                        }
                    }

                    // TODO: 我们已经获取到了更新的数据，现在需要做的就是更新一下本地的我们创建的助教 table

                    // 判断一下，当前的List是否是空的，如果是空的，我们就需要加载一下当list为空时，显示的TextView
                    if (mAssistCoauchList.isEmpty())
                        loadEmptyTv();
                    break;
                case RETRIEVE_INFO_WITH_DISTANCE_FILTERED:
                    String rangeStr = (String) msg.obj;
                    mWorker.fetchDataWithRangeFilter(rangeStr);
                    break;
                case RETRIEVE_INFO_WITH_LEVEL_FILTERED:
                    String levelStr = (String) msg.obj;
                    mWorker.fetchDataWithLevelFilter(levelStr);
                    break;
                case RETRIEVE_INFO_WITH_PRICE_FILTERED:
                    String priceStr = (String) msg.obj;
                    mWorker.fetchDataWithPriceFilter(priceStr);
                    break;
                case RETREIVE_INFO_WITH_KINDS_FILTERED:
                    String clazz = (String) msg.obj;
                    mWorker.fetchDataWithClazzFilter(clazz);
                    break;

                case DATA_HAS_BEEN_UPDATED:
                    mAssistCoauchListAdapter.notifyDataSetChanged();
                    Log.d(TAG, " the data set has been updated ");
                    break;
                case PublicConstant.TIME_OUT:
                    // 超时之后的处理策略
                    Utils.showToast(sContext, sContext.getString(R.string.http_request_time_out));
                    if (mAssistCoauchList.isEmpty())
                    {
                        loadEmptyTv();
                    }
                    break;
                case PublicConstant.NO_RESULT:
                    if (mAssistCoauchList.isEmpty())
                    {
                        loadEmptyTv();
                    } else
                    {
                        if (mLoadMore)
                        {
                            Utils.showToast(sContext, sContext.getString(R.string.no_more_info));
                        }
                    }
                    break;

                case PublicConstant.REQUEST_ERROR:
                    Bundle errorData = msg.getData();
                    if (null != errorData)
                    {
                        Utils.showToast(sContext, (String) msg.obj);
                    } else
                    {
                        Utils.showToast(sContext, sContext.getString(R.string.http_request_error));
                    }
                    if (mAssistCoauchList.isEmpty())
                    {
                        loadEmptyTv();
                    }
                    break;
            }
            mAssistCoauchListAdapter = new NearbyAssistCoauchSubFragmentListAdapter(sContext, (ArrayList<NearbyAssistCoauchSubFragmentBean>) mAssistCoauchList);
            mListView.setAdapter(mAssistCoauchListAdapter);
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

        public BackgroundWorkerHandler()
        {
            super(BACKGROUND_HANDLER_NAME, Process.THREAD_PRIORITY_BACKGROUND);
        }

        private Handler mBackgroundHandler;

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
                            retrieveAllInitialAssistCoauchInfo(0, 9);

                            break;

                        // TODO: 以下的四种操作虽然耗时，但是都是不涉及到网络的操作，以下四种检索都是直接从本地的我们创建的数据库当中进行检索
                        case RETRIEVE_INFO_WITH_LEVEL_FILTERED:
                            // TODO: 进行具体的本地数据库检索操作
                            String level = (String) msg.obj;
                            Log.d(TAG, " Inside the WorkerThread --> the level data we need to filter are : " + level);

                            break;
                        case RETREIVE_INFO_WITH_KINDS_FILTERED:
                            // TODO: 进行具体的本地数据库检索操作，以用户的选择的球种做为筛选条件
                            // TODO: 在检索到相应的数据之后，还要对ListView进行相关的操作
                            String clazz = (String) msg.obj;
                            Log.d(TAG, " Inside the WorkerThread --> the clazz we need to filter are : " + clazz);

                            break;
                        case RETRIEVE_INFO_WITH_PRICE_FILTERED:
                            String price = (String) msg.obj;
                            Log.d(TAG, " Inside the WorkerThread --> the price we need to filter are : " + price);


                            break;
                        case RETRIEVE_INFO_WITH_DISTANCE_FILTERED:
                            String distance = (String) msg.obj;
                            Log.d(TAG, " Inside the WorkerThread --> the distance we need to filter are : " + distance);

                            break;
                    }
                }
            };
            fetchAllData();
        }

        public void fetchAllData()
        {
            mBackgroundHandler.sendEmptyMessage(RETRIEVE_ALL_RAW_INFO);
        }

        public void fetchDataWithPriceFilter(String price)
        {
            mBackgroundHandler.obtainMessage(RETRIEVE_INFO_WITH_PRICE_FILTERED,price).sendToTarget();
        }

        public void fetchDataWithRangeFilter(String range)
        {
            mBackgroundHandler.obtainMessage(RETRIEVE_INFO_WITH_DISTANCE_FILTERED,range).sendToTarget();
        }

        public void fetchDataWithLevelFilter(String level)
        {
            mBackgroundHandler.obtainMessage(RETRIEVE_INFO_WITH_LEVEL_FILTERED,level).sendToTarget();
        }

        public void fetchDataWithClazzFilter(String clazz)
        {
            mBackgroundHandler.obtainMessage(RETREIVE_INFO_WITH_KINDS_FILTERED,clazz).sendToTarget();

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

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (Utils.networkAvaiable(sContext)) {
                        mLoadMore = false;
                        mRefresh = true;
                        retrieveAllInitialAssistCoauchInfo(0, 9);
                    } else {
                        Toast.makeText(sContext, sContext.getString(R.string.network_not_available), Toast.LENGTH_LONG).show();
                    }
                }
            }).start();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            String label = NearbyFragmentsCommonUtils.getLastedTime(sContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            mUIEventsHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mLoadMore = true;
                    mCurrentPos = mAssistCoauchList.size();
                    if (mBeforeCount != mAfterCount) {
                        mStartNum = mEndNum + (mAfterCount - mBeforeCount);
                        mEndNum += 10 + (mAfterCount - mBeforeCount);
                    } else {
                        mStartNum = mEndNum + 1;
                        mEndNum += 10;
                    }

                    Log.d(TAG, " loading more data --> the start num are : " + mStartNum + " , and the end are : " + mEndNum);
                    if (Utils.networkAvaiable(sContext)) {
                        retrieveAllInitialAssistCoauchInfo(mStartNum, mEndNum);
                    } else {
                        // TODO: 从本地的数据库当中进行检索

                    }

                }
            }, 1000);

        }
    };


    // TODO: 在测试接口的时候删除下面的方法
    //以下是用于初始化过程当中的测试数据
    private void initTestData()
    {
        int i;
        for (i = 0; i < 100; i++) {
            mAssistCoauchList.add(new NearbyAssistCoauchSubFragmentBean("", "", "月夜刘莎", "女", "斯诺克", "38", "1000米"));
        }
    }


}































































































































































































