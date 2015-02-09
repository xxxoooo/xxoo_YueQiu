package com.yueqiu.fragment.nearby;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.NearbyBilliardsDatingActivity;
import com.yueqiu.adapter.NearbyDatingSubFragmentListAdapter;
import com.yueqiu.bean.NearbyDatingDetailedAlreadyBean;
import com.yueqiu.bean.NearbyDatingSubFragmentDatingBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
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

import java.awt.font.TextAttribute;
import java.io.BufferedReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by scguo on 14/12/30.
 * <p/>
 * 用于SearchActivity当中的约球子Fragment的实现
 *
 */
public class BilliardsNearbyDatingFragment extends Fragment
{
    private static final String TAG = "BilliardsNearbyDatingFragment";

    private static Context sContext;

    public BilliardsNearbyDatingFragment()
    {
    }

    public static BilliardsNearbyDatingFragment newInstance(Context context, String params)
    {
        sContext = context;
        BilliardsNearbyDatingFragment instance = new BilliardsNearbyDatingFragment();

        Bundle args = new Bundle();
        args.putString(KEY_DATING_FRAGMENT, params);
        instance.setArguments(args);

        return instance;
    }

    private boolean mNetworkAvailable;

    private BackgroundWorkerHandler mBackgroundHandler;

    private ProgressBar mPreProgress;
    private Drawable mProgressDrawable;
    private TextView mPreText;

    private static NearbyParamsPreference sParamsPreference = NearbyParamsPreference.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // TODO: 我们最好将判断网络状况的变量放到onResume()方法当中进行判断，而不是放到onCreate()方法，因为这里调用的次数有限
        // TODO: 但是将她直接放到onResume()方法当中又会使Fragment之间的切换变的卡。稍后在做决定？？？？
        mNetworkAvailable = Utils.networkAvaiable(sContext);
    }

    public static final String KEY_DATING_FRAGMENT = "BilliardsNearbyDatingFragment";

    private View mView;
    private Button mBtnDistan, mBtnPublishDate;
    private PullToRefreshListView mDatingListView;
    private List<NearbyDatingSubFragmentDatingBean> mDatingList = new ArrayList<NearbyDatingSubFragmentDatingBean>();

    private NearbyDatingSubFragmentListAdapter mDatingListAdapter;
    // TODO: mArgs是我们在初始化Fragment时需要接受来自初始化这个Fragment所传递的参数的容器，
    // TODO: 只不过我们现在没有用到，但是这个参数是我们更好的封装Fragment的基础，不要忽略
    private Bundle mArgs;
    private NearbyPopBasicClickListener mClickListener;

    private NearbyFragmentsCommonUtils.ControlPopupWindowCallback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_nearby_dating_layout, container, false);

        NearbyFragmentsCommonUtils.initViewPager(sContext, mView, R.id.dating_frament_gallery_pager, R.id.dating_fragment_gallery_pager_indicator_group);

        mClickListener = new NearbyPopBasicClickListener(sContext,mUIEventsHandler,sParamsPreference);
        (mBtnDistan = (Button) mView.findViewById(R.id.btn_dating_distance)).setOnClickListener(mClickListener);
        (mBtnPublishDate = (Button) mView.findViewById(R.id.btn_dating_publichdate)).setOnClickListener(mClickListener);

        mCallback = mClickListener;

        Bundle args = getArguments();
        mArgs = args;

        mDatingListView = (PullToRefreshListView) mView.findViewById(R.id.search_dating_subfragment_list);
        mDatingListView.setMode(PullToRefreshBase.Mode.BOTH);
        mDatingListView.setOnRefreshListener(mOnRefreshListener);

        // 加载Progressbar
        mPreText = (TextView) mView.findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);

        // TODO: 以下加载的是测试数据，我们以后需要移除, 但是目前还不能移除，只是暂时注释掉，用于展示完整的UI效果
//        initTestData();

        mDatingListAdapter = new NearbyDatingSubFragmentListAdapter(sContext, (ArrayList<NearbyDatingSubFragmentDatingBean>) mDatingList);
        mDatingListView.setAdapter(mDatingListAdapter);

        mDatingListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                NearbyDatingSubFragmentDatingBean bean = mDatingList.get(position - 1);
                Bundle args = new Bundle();
                args.putString(NearbyFragmentsCommonUtils.KEY_DATING_FRAGMENT_PHOTO, bean.getUserPhoto());
                args.putInt(NearbyFragmentsCommonUtils.KEY_DATING_TABLE_ID, Integer.parseInt(bean.getId()));
                args.putString(NearbyFragmentsCommonUtils.KEY_DATING_USER_NAME, bean.getUserName());
                Intent intent = new Intent(sContext, NearbyBilliardsDatingActivity.class);
                intent.putExtra(NearbyFragmentsCommonUtils.KEY_BUNDLE_SEARCH_DATING_FRAGMENT, args);
                Log.d(TAG, " the current dating info table id we get are : " + bean.getId());
                sContext.startActivity(intent);
            }
        });

        mBackgroundHandler = new BackgroundWorkerHandler();
        Log.d(TAG, " DatingFragment --> inside the onCreateView() method");
        if (Utils.networkAvaiable(sContext))
        {
            Log.d(TAG, " inside the onCreateView() method, and we have detected the netWork is available, but just cannot get data ");
            mLoadMore = false;
            mRefresh = false;
            Log.d(TAG, " the state of the current HandlerThread are : " + mBackgroundHandler.getState() + " , " + (mBackgroundHandler == null));
            // 我们仅在网络可行的情况下进行网络请求，减少不必要的网络请求
            if (mBackgroundHandler != null && mBackgroundHandler.getState() == Thread.State.NEW)
            {
                Log.d(TAG, " in the onCreateView --> start the background handler ");
                mBackgroundHandler.start();
            }
        } else
        {
            Log.d(TAG, " in the onCreateView --> we have detected network unavailable ");
            mUIEventsHandler.sendEmptyMessage(NETWORK_UNAVAILABLE);
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
        Log.d(TAG, " the onPause method has been called ");
        mCallback.closePopupWindow();
        if (null != mBackgroundHandler)
        {
            Log.d(TAG, " we need to stop the Background Handler ");
            mBackgroundHandler.interrupt();
            mBackgroundHandler = null;
        }


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

    /**
     * @param userId
     * @param range    发布约球信息的大致距离,距离范围。例如1000米以内,具体传递的形式例如range=100
     * @param date     发布日期，例如date=2014-04-04，在这里，我们默认的就选择今天就可以了
     * @param startNum 请求信息的开始的条数的数目(当我们进行分页请求的时候，我们就会用到这个特性，即每次当用户滑动到列表低端或者当用户滑动更新的时候，我们需要
     *                 通过更改startNum的值来进行分页加载的具体实现)
     *                 例如start_no=0
     * @param endNum   请求列表信息的结束条目，例如我们可以一次只加载10条，当用户请求的时候再加载更多的数据,例如end_no=9
     */
    private void retrieveDatingInfo(final String userId, final String range, final String date, final int startNum, final int endNum)
    {
        if (!mNetworkAvailable) {
            mUIEventsHandler.obtainMessage(FETCH_DATA_FAILED,
                    sContext.getResources().getString(R.string.network_not_available)).sendToTarget();
            return;
        }

        try
        {
            final int userIdInt = Integer.parseInt(userId);
            Log.d(TAG, " the finally user id we get for requesting the user info are : " + userId);
            if (userIdInt < 1)
            {
                // 当前用户并没有登录，是不可能得到相关的约球信息的，所以必须要先登录才可以
                mUIEventsHandler.sendEmptyMessage(USER_HAS_NOT_LOGIN);
                return;
            }
        } catch (final Exception e)
        {
            Log.d(TAG, " exception happened while we parse the user id value from the YueQiuApp, and cause to : " + e.toString());
        }


        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("user_id", userId);
        // 我们将range的默认值置为1000
        String rangeVal = TextUtils.isEmpty(range) ? "1000" : range;
        requestParams.put("range", rangeVal);
        // 我们将date的默认值设置为当前的请求时间，日期格式设置为2015-01-31
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        calendar.setTime(new Date());
        String currentDateStr = dateFormatter.format(calendar.getTime());
        Log.d(TAG, " the detailed date we get for today are : " + currentDateStr);
        String dateStr = TextUtils.isEmpty(date) ? currentDateStr : date;
        requestParams.put("date", dateStr);
        requestParams.put("start_no", startNum + "");
        requestParams.put("end_no", endNum + "");

        List<NearbyDatingSubFragmentDatingBean> cacheDatingList = new ArrayList<NearbyDatingSubFragmentDatingBean>();

        String rawResult = HttpUtil.urlClient(HttpConstants.NearbyDating.URL, requestParams, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the raw result we get for the dating info are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult))
        {
            try {
                JSONObject rawJsonObj = new JSONObject(rawResult);
                Log.d(TAG, " the rawJson object we get are : " + rawJsonObj);
                if (!TextUtils.isEmpty(rawJsonObj.toString()))
                {
                    if (!rawJsonObj.isNull("code"))
                    {
                        final int statusCode = rawJsonObj.getInt("code");
                        if (statusCode == HttpConstants.ResponseCode.NORMAL)
                        {
                            // TODO: 然后进行以后的具体的解析过程的处理
                            JSONArray resultJsonArr = rawJsonObj.getJSONArray("result");
                            final int size = resultJsonArr.length();
                            int i;
                            for (i = 0; i < size; ++i)
                            {
                                JSONObject subJsonObj = (JSONObject) resultJsonArr.get(i);
                                String imgUrl = subJsonObj.getString("img_url");
                                String datingId = subJsonObj.getString("id");
                                String title = subJsonObj.getString("title");
                                String userName = subJsonObj.getString("username");
                                long distance = subJsonObj.getLong("range");
                                NearbyDatingSubFragmentDatingBean datingBean = new NearbyDatingSubFragmentDatingBean(datingId, imgUrl, userName, title, String.valueOf(distance));

                                // 将我们解析得到的datingBean插入到我们创建的数据库当中
                                cacheDatingList.add(datingBean);
                            }

                            // TODO: 我们应该在这里通知UI主线程数据请求工作已经全部完成了，停止显示ProgressBar或者显示一个Toast全部数据已经加载完的提示
                            mUIEventsHandler.obtainMessage(FETCH_DATA_SUCCESSED, cacheDatingList).sendToTarget();
                            mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);

                        } else if (statusCode == HttpConstants.ResponseCode.TIME_OUT)
                        {
                            mUIEventsHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                            mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                        } else if (statusCode == HttpConstants.ResponseCode.NO_RESULT)
                        {
                            mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                        } else
                        {
                            Message errorMsg = mUIEventsHandler.obtainMessage(PublicConstant.REQUEST_ERROR);
                            Bundle errorData = new Bundle();
                            String errorStr = rawJsonObj.getString("msg");
                            if (! TextUtils.isEmpty(errorStr))
                            {
                                errorData.putString(KEY_REQUEST_ERROR_DATING, errorStr);
                            }
                            errorMsg.setData(errorData);
                            mUIEventsHandler.sendMessage(errorMsg);
                            mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                        }
                    } else
                    {
                        mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                        mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                    }
                }
            } catch (JSONException e)
            {
                e.printStackTrace();
                // 发生异常了之后，我们应该准确的通知UIHandler没有获取到任何的数据
                mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                Log.d(TAG, " exception happened in parsing the json data we get, and the detailed reason are : " + e.toString());
            }
        } else
        {
            Log.d(TAG, " exception happened on some special devices, and this should occur for the network failure, but the Android does" +
                    "not detect it as netWork failure, and it sign it as Server(the service provider) error ");
            mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
        }
    }

    private static final String KEY_REQUEST_ERROR_DATING = "keyRequestErrorDating";

    private static final String KEY_REQUEST_START_NUM = "keyDatingRequestStartNum";
    private static final String KEY_REQUEST_END_NUM = "keyDatingRequestEndNum";

    private static final int DATA_HAS_BEEN_UPDATED = 1 << 8;

    private static final int UI_SHOW_DIALOG = 1 << 4;
    private static final int UI_HIDE_DIALOG = 1 << 5;

    private static final int START_RETRIEVE_ALL_DATA = 1 << 1;

    // 由于这些常量值被定义到同一个地方进行使用，所以我们将Dating Fragment当中的常量值定义为从20开始
    public static final int RETRIEVE_DATA_WITH_RANGE_FILTERED = 20 << 2;
    public static final int RETRIEVE_DATA_WITH_DATE_FILTERED = 20 << 3;

    private static final int FETCH_DATA_SUCCESSED = 1 << 6;
    private static final int FETCH_DATA_FAILED = 1 << 7;

    private static final int NETWORK_UNAVAILABLE = 1 << 9;
    private static final int USER_HAS_NOT_LOGIN = 1 << 10;

    private Handler mUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UI_HIDE_DIALOG:
                    mDatingListAdapter.notifyDataSetChanged();
                    hideProgress();
                    if (mDatingListView.isRefreshing())
                    {
                        mDatingListView.onRefreshComplete();
                    }
                    Log.d(TAG, " hiding the dialog ");

                    break;
                case UI_SHOW_DIALOG:
                    showProgress();
                    Log.d(TAG, " start showing the dialog ");
                    break;
                case FETCH_DATA_FAILED:
                    Log.d(TAG, " we have received the information of the failure network request ");
                    String infoStr = (String) msg.obj;
                    if (mDatingList.isEmpty())
                    {
                        Log.d(TAG, " the current list should be empty here ??? ");
                        loadEmptyTv(R.string.search_activity_subfragment_empty_tv_str);
                    }
                    Toast.makeText(sContext, infoStr, Toast.LENGTH_SHORT).show();
                    hideProgress();
                    Log.d(TAG, " fail to get data due to the reason as : " + infoStr);
                    break;
                case FETCH_DATA_SUCCESSED:
                    mBeforeCount = mDatingList.size();
                    List<NearbyDatingSubFragmentDatingBean> datingList = (ArrayList<NearbyDatingSubFragmentDatingBean>) msg.obj;
                    for (NearbyDatingSubFragmentDatingBean datingBean : datingList)
                    {
                        if (! mDatingList.contains(datingBean))
                        {
                            mDatingList.add(datingBean);
                        }
                    }
                    mAfterCount = mDatingList.size();
                    if (mDatingList.isEmpty())
                    {
                        loadEmptyTv(R.string.search_activity_subfragment_empty_tv_str);
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
                    mDatingListAdapter.notifyDataSetChanged();
                    break;
                case RETRIEVE_DATA_WITH_RANGE_FILTERED:
                    String range = (String) msg.obj;
                    mBackgroundHandler.fetchDatingWithRangeFilter(range);

                    break;
                case RETRIEVE_DATA_WITH_DATE_FILTERED:
                    String publishDate = (String) msg.obj;
                    Log.d(TAG, " inside the mUIEventsHandler --> we have received the date need to filter are : " + publishDate);
                    mBackgroundHandler.fetchDatingWithPublishDateFilter(publishDate);
                    break;
                case DATA_HAS_BEEN_UPDATED:
                    mDatingListAdapter.notifyDataSetChanged();
                    Log.d(TAG, " the adapter eof the DatingFragment has been updated ");
                    break;

                case NETWORK_UNAVAILABLE:
                    hideProgress();
                    if (mDatingList.isEmpty())
                    {
                        loadEmptyTv(R.string.search_activity_subfragment_empty_tv_str);
                    }
                    Utils.showToast(sContext, sContext.getString(R.string.network_not_available));

                    break;
                case PublicConstant.TIME_OUT:
                    // 超时之后的处理策略
                    Utils.showToast(sContext, sContext.getString(R.string.http_request_time_out));
                    if (mDatingList.isEmpty()) {
                        loadEmptyTv(R.string.search_activity_subfragment_empty_tv_str);
                    }
                    hideProgress();
                    break;
                case PublicConstant.NO_RESULT:
                    if (mDatingList.isEmpty()) {
                        loadEmptyTv(R.string.search_activity_subfragment_empty_tv_str);
                    } else {
                        if (mLoadMore) {
                            Utils.showToast(sContext, sContext.getString(R.string.no_more_info));
                        }
                    }
                    hideProgress();
                    break;

                case PublicConstant.REQUEST_ERROR:
                    Bundle errorData = msg.getData();
                    String errorInfo = errorData.getString(KEY_REQUEST_ERROR_DATING);
                    if (! TextUtils.isEmpty(errorInfo))
                    {
                        Utils.showToast(sContext, errorInfo);
                    } else
                    {
                        Utils.showToast(sContext, sContext.getString(R.string.http_request_error));
                    }

                    if (mDatingList.isEmpty())
                    {
                        loadEmptyTv(R.string.search_activity_subfragment_empty_tv_str);
                    }

                    hideProgress();
                    break;

                case USER_HAS_NOT_LOGIN:
                    Utils.showToast(sContext, sContext.getString(R.string.please_login_first));
                    hideProgress();
                    mDatingListAdapter.notifyDataSetChanged();
                    if (mDatingList.isEmpty())
                    {
                        loadEmptyTv(R.string.search_dating_login_first);
                    }
                    break;
            }
            mDatingListAdapter.notifyDataSetChanged();
        }
    };

    private void loadEmptyTv(final int contentStrId)
    {
        if (mDatingListView.isRefreshing())
        {
            mDatingListView.onRefreshComplete();
        }

        NearbyFragmentsCommonUtils.setFragmentEmptyTextView(sContext, mDatingListView, sContext.getString(contentStrId));
    }

    private void showProgress()
    {
        Log.d(TAG, " inside the showProgress internal method ");
        mPreProgress.setVisibility(View.VISIBLE);
        mPreText.setVisibility(View.VISIBLE);
    }

    private void hideProgress()
    {
        Log.d(TAG, " inside the hideProgress internal method ");
        mPreProgress.setVisibility(View.GONE);
        mPreText.setVisibility(View.GONE);
    }

    private static final String BACKGROUDN_WORKER_NAME = "BackgroundWorkerHandler";

    private class BackgroundWorkerHandler extends HandlerThread
    {
        public BackgroundWorkerHandler()
        {
            super(BACKGROUDN_WORKER_NAME, Process.THREAD_PRIORITY_BACKGROUND);
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
                        case START_RETRIEVE_ALL_DATA:
                            mUIEventsHandler.sendEmptyMessage(UI_SHOW_DIALOG);
                            Bundle requestInfo = msg.getData();
                            final int startNum = requestInfo.getInt(KEY_REQUEST_START_NUM);
                            final int endNum = requestInfo.getInt(KEY_REQUEST_END_NUM);
                            Log.d(TAG, " inside the dating fragment, in the workHandler --> and the startNum : " + startNum + " , the endNum : " + endNum);
                            String cacheRange = sParamsPreference.getDatingRange(sContext);
                            String cacheDate = sParamsPreference.getDatingPublishedDate(sContext);
                            retrieveDatingInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()), cacheRange, cacheDate, startNum, endNum);

                            break;
                        case RETRIEVE_DATA_WITH_DATE_FILTERED:                           
                            String publishDate = (String) msg.obj;
                            Log.d(TAG, " inside the dating fragment BackgroundHandlerThread --> the publishDate we need to filter are : " + publishDate);
                            String dateCacheRange = sParamsPreference.getDatingRange(sContext);
                            // 每次筛选请求都是从零开始的重新请求
                            retrieveDatingInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()), dateCacheRange, publishDate, 0, 9);
                            break;
                        case RETRIEVE_DATA_WITH_RANGE_FILTERED:                          
                            String range = (String) msg.obj;
                            Log.d(TAG, " inside the dating fragment BackgroundHandlerThread --> the range we need to filter are : " + range);
                            String rangeCacheDate = sParamsPreference.getDatingPublishedDate(sContext);
                            // 我们需要从0开始重新请求
                            retrieveDatingInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()), range, rangeCacheDate, 0, 9);
                            break;
                    }
                }
            };
            fetchDatingData(0, 9);
        }

        public void fetchDatingData(final int startNum, final int endNum)
        {
            Message requestMsg = mWorkerHandler.obtainMessage(START_RETRIEVE_ALL_DATA);
            Bundle data = new Bundle();
            data.putInt(KEY_REQUEST_START_NUM, startNum);
            data.putInt(KEY_REQUEST_END_NUM, endNum);
            requestMsg.setData(data);
            mWorkerHandler.sendMessage(requestMsg);
        }

        public void fetchDatingWithRangeFilter(String range)
        {
            if (! TextUtils.isEmpty(range))
            {
                mWorkerHandler.obtainMessage(RETRIEVE_DATA_WITH_RANGE_FILTERED, range).sendToTarget();
            }
        }

        public void fetchDatingWithPublishDateFilter(String publishDate)
        {
            Log.d(TAG, " inside the method of BackgroundHandler --> the published date we get are : " + publishDate);
            if (! TextUtils.isEmpty(publishDate))
            {
                mWorkerHandler.obtainMessage(RETRIEVE_DATA_WITH_DATE_FILTERED, publishDate).sendToTarget();
            }
        }

    }

    private boolean mRefresh;
    private boolean mLoadMore;

    // 用于请求更多数据时的开始条目和结束条目
    private int mStarNum = 0;
    public int mEndNum = 9;

    private int mCurrentPos;
    private int mBeforeCount;
    private int mAfterCount;

    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener = new PullToRefreshBase.OnRefreshListener2<ListView>()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            String label = NearbyFragmentsCommonUtils.getLastedTime(sContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            if (Utils.networkAvaiable(sContext))
            {
                mRefresh = true;
                mLoadMore = false;
                if (null != mBackgroundHandler)
                {
                    mBackgroundHandler.fetchDatingData(0, 9);
                }
            } else
            {
                mUIEventsHandler.sendEmptyMessage(NETWORK_UNAVAILABLE);
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            String label = NearbyFragmentsCommonUtils.getLastedTime(sContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            mLoadMore = true;
            mRefresh = false;
            mCurrentPos = mDatingList.size();
            if (mBeforeCount != mAfterCount)
            {
                mStarNum = mEndNum + (mAfterCount - mBeforeCount);
                mEndNum += 10 + (mAfterCount - mBeforeCount);
            } else
            {
                mStarNum = mEndNum + 1;
                mEndNum += 10;
            }

            if (Utils.networkAvaiable(sContext))
            {
                if (null != mBackgroundHandler)
                {
                    Log.d(TAG, " in the dating fragment --> loading more data --> startNum : " + mStarNum + " , and the endNum : " + mEndNum);
                    mBackgroundHandler.fetchDatingData(mStarNum, mEndNum);
                }
            } else
            {
                mUIEventsHandler.sendEmptyMessage(NETWORK_UNAVAILABLE);
            }
        }
    };


    // TODO: 以下都是测试数据,在测试接口的时候将他们删除掉
    private void initTestData()
    {
        int i;
        for (i = 0; i < 200; ++i) {
            mDatingList.add(new NearbyDatingSubFragmentDatingBean("", "", "月夜流水", "第N届斯诺克大力神杯就要开始，一起参加啊！", "230米以内"));
        }
    }

}



















































































































































































































