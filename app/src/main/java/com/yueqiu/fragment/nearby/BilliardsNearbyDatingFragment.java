package com.yueqiu.fragment.nearby;

import android.app.Activity;
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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.NearbyBilliardsDatingActivity;
import com.yueqiu.adapter.NearbyDatingSubFragmentListAdapter;
import com.yueqiu.bean.NearbyDatingSubFragmentDatingBean;
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
 * 用于SearchActivity当中的约球子Fragment的实现
 *
 */
public class BilliardsNearbyDatingFragment extends Fragment
{
    private static final String TAG = "BilliardsNearbyDatingFragment";
    private Context mContext;

    public BilliardsNearbyDatingFragment()
    {
    }

    public static BilliardsNearbyDatingFragment newInstance(Context context, String params)
    {
//        mContext = context;
        BilliardsNearbyDatingFragment instance = new BilliardsNearbyDatingFragment();

        Bundle args = new Bundle();
        args.putString(KEY_DATING_FRAGMENT, params);
        instance.setArguments(args);

        return instance;
    }

    private boolean mNetworkAvailable,mIsListEmpty;

    private BackgroundWorkerHandler mBackgroundHandler;

    private ProgressBar mPreProgress;
    private Drawable mProgressDrawable;
    private TextView mPreText;

    private static NearbyParamsPreference sParamsPreference = NearbyParamsPreference.getInstance();

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

        // TODO: 我们最好将判断网络状况的变量放到onResume()方法当中进行判断，而不是放到onCreate()方法，因为这里调用的次数有限
        // TODO: 但是将她直接放到onResume()方法当中又会使Fragment之间的切换变的卡。稍后在做决定？？？？
        mNetworkAvailable = Utils.networkAvaiable(mContext);
    }

    public static final String KEY_DATING_FRAGMENT = "BilliardsNearbyDatingFragment";

    private View mView;
    private Button mBtnDistan, mBtnPublishDate;
    private PullToRefreshListView mDatingListView;
    private ArrayList<NearbyDatingSubFragmentDatingBean> mDatingList = new ArrayList<NearbyDatingSubFragmentDatingBean>();

    // 用于保存在我们的状态当中的cacheList
    private ArrayList<NearbyDatingSubFragmentDatingBean> mCachedDatingList = new ArrayList<NearbyDatingSubFragmentDatingBean>();
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

        NearbyFragmentsCommonUtils commonUtils = new NearbyFragmentsCommonUtils(mContext);
        commonUtils.initViewPager(mContext, mView);

        mClickListener = new NearbyPopBasicClickListener(mContext,mUIEventsHandler,sParamsPreference);
        (mBtnDistan = (Button) mView.findViewById(R.id.btn_dating_distance)).setOnClickListener(mClickListener);
        (mBtnPublishDate = (Button) mView.findViewById(R.id.btn_dating_publichdate)).setOnClickListener(mClickListener);
        mEmptyView = new TextView(getActivity());

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

        mDatingListAdapter = new NearbyDatingSubFragmentListAdapter(mContext, (ArrayList<NearbyDatingSubFragmentDatingBean>) mDatingList);
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
                Intent intent = new Intent(mContext, NearbyBilliardsDatingActivity.class);
                intent.putExtras(args);
                Log.d(TAG, " the current dating info table id we get are : " + bean.getId());
                mContext.startActivity(intent);
            }
        });

        if (null != savedInstanceState)
        {
            mRefresh = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_REFRESH);
            mLoadMore = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_LOAD_MORE);
            mIsSavedInstance = savedInstanceState.getBoolean(NearbyFragmentsCommonUtils.KEY_SAVED_INSTANCE);

            mCachedDatingList = savedInstanceState.getParcelableArrayList(NearbyFragmentsCommonUtils.KEY_SAVED_LISTVIEW);
            mUIEventsHandler.obtainMessage(PublicConstant.USE_CACHE, mCachedDatingList).sendToTarget();
        }


        mBackgroundHandler = new BackgroundWorkerHandler(mStarNum, mEndNum);
        mLoadMore = false;
        mRefresh = false;
        // 我们仅在网络可行的情况下进行网络请求，减少不必要的网络请求
        if (mBackgroundHandler.getState() == Thread.State.NEW)
        {
            Log.d(TAG, " in the onCreateView --> start the background handler ");
            mBackgroundHandler.start();
        }
        if (! Utils.networkAvaiable(getActivity())){
            mUIEventsHandler.sendEmptyMessage(NETWORK_UNAVAILABLE);
        }
        return mView;
    }




    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(NearbyFragmentsCommonUtils.KEY_SAVED_LISTVIEW, mDatingList);
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
        Log.d(TAG, " the onPause method has been called ");
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
        if (null != mBackgroundHandler)
        {
            Log.d(TAG, " we need to stop the Background Handler ");
            mBackgroundHandler.interrupt();
            mBackgroundHandler = null;
        }

        // 将所有的筛选参数置空
        sParamsPreference.setDatingPublishedDate(mContext, "");
        sParamsPreference.setDatingRange(mContext, "");

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
        if (!Utils.networkAvaiable(getActivity()))
        {
            mUIEventsHandler.obtainMessage(FETCH_DATA_FAILED,
                    mContext.getResources().getString(R.string.network_not_available)).sendToTarget();
            return;
        }

        // TODO: 以下的部分是用于判断当前的用户是否登录的逻辑，但是现在不需要了，所以我们注掉了
        // TODO: 但是不知道会不会变策略，所以暂时还是不要直接删掉了
//        try
//        {
//            final int userIdInt = Integer.parseInt(userId);
//            Log.d(TAG, " the finally user id we get for requesting the user info are : " + userId);
//            if (userIdInt < 1)
//            {
//                // 当前用户并没有登录，是不可能得到相关的约球信息的，所以必须要先登录才可以
//                mUIEventsHandler.sendEmptyMessage(USER_HAS_NOT_LOGIN);
//                return;
//            }
//        } catch (final Exception e)
//        {
//            Log.d(TAG, " exception happened while we parse the user id value from the YueQiuApp, and cause to : " + e.toString());
//        }


        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        // TODO: 现在约球的设计是不需要传递UserId的
//        requestParams.put("user_id", userId);

        // 我们将range的默认值置为1000
        // 默认不传参数，即空参数
        if (! TextUtils.isEmpty(range))
        {
//            String rangeVal = TextUtils.isEmpty(range) ? "1000" : range;
            requestParams.put("range", range);
        }
        // 现在的策略是如果没有需要的话，默认的date参数直接置为空就可以了，即不传递这个参数
//        // 我们将date的默认值设置为当前的请求时间，日期格式设置为2015-01-31
//        Calendar calendar = Calendar.getInstance();
//        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
//        calendar.setTime(new Date());
//        String currentDateStr = dateFormatter.format(calendar.getTime());
//        Log.d(TAG, " the detailed date we get for today are : " + currentDateStr);
        if (! TextUtils.isEmpty(date))
        {
            requestParams.put("date", date);
        }
        requestParams.put("start_no", startNum + "");
        requestParams.put("end_no", endNum + "");

        final List<NearbyDatingSubFragmentDatingBean> cacheDatingList = new ArrayList<NearbyDatingSubFragmentDatingBean>();

        mDatingListView.setMode(PullToRefreshBase.Mode.DISABLED);
        
        HttpUtil.requestHttp(HttpConstants.NearbyDating.URL, requestParams, HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","date response ->" + response);
                try {
                    if (!TextUtils.isEmpty(response.toString()))
                    {
                        if (!response.isNull("code"))
                        {
                            final int code = response.getInt("code");
                            if (code == HttpConstants.ResponseCode.NORMAL)
                            {
                                if (! "null".equals(response.get("result").toString()))
                                {
                                    JSONArray resultJsonArr = response.getJSONArray("result");
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
                                    if (cacheDatingList.isEmpty())
                                    {
                                        mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);

                                    } else
                                    {
                                        // TODO: 我们应该在这里通知UI主线程数据请求工作已经全部完成了，停止显示ProgressBar或者显示一个Toast全部数据已经加载完的提示
                                        mUIEventsHandler.obtainMessage(FETCH_DATA_SUCCESSED, cacheDatingList).sendToTarget();
                                    }
                                } else
                                {
                                    mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                                }
                            } else if (code == HttpConstants.ResponseCode.TIME_OUT)
                            {
                                mUIEventsHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                            } else if (code == HttpConstants.ResponseCode.NO_RESULT)
                            {
                                mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            } else
                            {
                                Message errorMsg = mUIEventsHandler.obtainMessage(PublicConstant.REQUEST_ERROR);
                                Bundle errorData = new Bundle();
                                String errorStr = response.getString("msg");
                                if (! TextUtils.isEmpty(errorStr))
                                {
                                    errorData.putString(KEY_REQUEST_ERROR_DATING, errorStr);
                                }
                                errorMsg.setData(errorData);
                                mUIEventsHandler.sendMessage(errorMsg);
                            }
                        } else
                        {
                            mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        }
                    } else
                    {
                        mUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                    // 发生异常了之后，我们应该准确的通知UIHandler没有获取到任何的数据
                    mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    //mUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });
    }

    private static final String KEY_REQUEST_ERROR_DATING = "keyRequestErrorDating";

    private static final String KEY_REQUEST_START_NUM = "keyDatingRequestStartNum";
    private static final String KEY_REQUEST_END_NUM = "keyDatingRequestEndNum";

    private static final int DATA_HAS_BEEN_UPDATED = 1 << 8;

    private static final int UI_SHOW_DIALOG = 1 << 4;
//    private static final int UI_HIDE_DIALOG = 1 << 5;

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
            if(mDatingListView.getMode() == PullToRefreshBase.Mode.DISABLED){
                mDatingListView.setMode(PullToRefreshBase.Mode.BOTH);
            }

            if (mDatingListView.isRefreshing())
            {
                mDatingListView.onRefreshComplete();
            }
            switch (msg.what)
            {
//                case UI_HIDE_DIALOG:
//                    mDatingListAdapter.notifyDataSetChanged();
//                    hideProgress();
//                    if (mDatingListView.isRefreshing())
//                    {
//                        mDatingListView.onRefreshComplete();
//                    }
//                    Log.d(TAG, " hiding the dialog ");
//
//                    break;
                case UI_SHOW_DIALOG:
                    showProgress();
                    if(mEmptyView.getVisibility() == View.VISIBLE){
                        mDatingListView.setEmptyView(null);
                        mEmptyView.setVisibility(View.GONE);
                    }
                    Log.d(TAG, " start showing the dialog ");
                    break;
                case FETCH_DATA_FAILED:
                    Log.d(TAG, " we have received the information of the failure network request ");
                    String infoStr = (String) msg.obj;
                    if (mDatingList.isEmpty())
                    {
                        Log.d(TAG, " the current list should be empty here ??? ");
                        setEmptyViewVisible();
                    }
                    Toast.makeText(mContext, infoStr, Toast.LENGTH_SHORT).show();
                    hideProgress();
                    Log.d(TAG, " fail to get data due to the reason as : " + infoStr);
                    break;
                case PublicConstant.USE_CACHE:
                    // 首先将我们的EmptyView隐藏掉
                    setEmptyViewGone();
                    ArrayList<NearbyDatingSubFragmentDatingBean> cachedList = (ArrayList<NearbyDatingSubFragmentDatingBean>) msg.obj;
                    mDatingList.addAll(cachedList);
                    if(mDatingList.isEmpty()) {
//                        loadEmptyTv(R.string.search_activity_subfragment_empty_tv_str,false);
                        setEmptyViewVisible();
                    }
                    break;

                case FETCH_DATA_SUCCESSED:
                    setEmptyViewGone();
                    hideProgress();
                    mBeforeCount = mDatingList.size();
                    mIsListEmpty = mDatingList.isEmpty();
                    List<NearbyDatingSubFragmentDatingBean> datingList = (ArrayList<NearbyDatingSubFragmentDatingBean>) msg.obj;
                    for (NearbyDatingSubFragmentDatingBean datingBean : datingList)
                    {
                        if (! mDatingList.contains(datingBean))
                        {
                            if(!mIsListEmpty && Integer.valueOf(mDatingList.get(0).getId()) < Integer.valueOf(datingBean.getId())){
                                mDatingList.add(0,datingBean);
                            }else {
                                mDatingList.add(datingBean);
                            }
                        }
                    }
                    mAfterCount = mDatingList.size();
                    if (mDatingList.isEmpty())
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
                case RETRIEVE_DATA_WITH_RANGE_FILTERED:
                    String range = (String) msg.obj;
                    if (mBackgroundHandler != null)
                    {
                        mBackgroundHandler.fetchDatingWithRangeFilter(range);
                    }

                    break;
                case RETRIEVE_DATA_WITH_DATE_FILTERED:
                    String publishDate = (String) msg.obj;
                    Log.d(TAG, " inside the mUIEventsHandler --> we have received the date need to filter are : " + publishDate);
                    if (null != mBackgroundHandler)
                    {
                        mBackgroundHandler.fetchDatingWithPublishDateFilter(publishDate);
                    }
                    break;
                case DATA_HAS_BEEN_UPDATED:
                    mDatingListAdapter.notifyDataSetChanged();
                    Log.d(TAG, " the adapter eof the DatingFragment has been updated ");
                    break;

                case NETWORK_UNAVAILABLE:
                    hideProgress();
                    if (mDatingList.isEmpty())
                    {
                        setEmptyViewVisible();
                    }
                    Utils.showToast(mContext, mContext.getString(R.string.network_not_available));

                    break;
                case PublicConstant.TIME_OUT:
                    // 超时之后的处理策略
                    Utils.showToast(mContext, mContext.getString(R.string.http_request_time_out));
                    if (mDatingList.isEmpty()) {
                        setEmptyViewVisible();
                    }
                    hideProgress();
                    break;
                case PublicConstant.NO_RESULT:
                    if (mDatingList.isEmpty()) {
                        setEmptyViewVisible();
                    } else {
                        if (mLoadMore)
                        {
                            Utils.showToast(mContext, mContext.getString(R.string.no_more_info, mContext.getString(R.string.search_billiard_dating_str)));
                        }
                    }
                    hideProgress();
                    break;

                case PublicConstant.REQUEST_ERROR:
                    Bundle errorData = msg.getData();
                    String errorInfo = errorData.getString(KEY_REQUEST_ERROR_DATING);
                    if (! TextUtils.isEmpty(errorInfo))
                    {
                        Utils.showToast(mContext, errorInfo);
                    } else
                    {
                        Utils.showToast(mContext, mContext.getString(R.string.http_request_error));
                    }

                    if (mDatingList.isEmpty())
                    {
                        setEmptyViewVisible();
                    }

                    hideProgress();
                    break;

                case USER_HAS_NOT_LOGIN:
                    //Utils.showToast(mContext, mContext.getString(R.string.please_login_first));
                    hideProgress();
                    mDatingListAdapter.notifyDataSetChanged();
                    if (mDatingList.isEmpty())
                    {
                        setEmptyViewVisible();
                    }
                    break;
            }
            mDatingListAdapter.notifyDataSetChanged();
        }
    };


    private TextView mEmptyView;
    // 我们通过将disable的值设置为false来进行加载EmptyView
    // 通过将disable的值设置为true来隐藏emptyView
    private void setEmptyViewVisible(){
        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mEmptyView.setTextColor(getActivity().getResources().getColor(R.color.md__defaultBackground));
        mEmptyView.setText(R.string.search_activity_subfragment_empty_tv_str);
        mDatingListView.setEmptyView(mEmptyView);
    }

    private void setEmptyViewGone()
    {
        if (null != mEmptyView)
        {
            mEmptyView.setVisibility(View.GONE);
            mDatingListView.setEmptyView(null);
        }
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
        private final int mStartNO, mEndNO;
        public BackgroundWorkerHandler(int startNum, int endNum)
        {
            super(BACKGROUDN_WORKER_NAME, Process.THREAD_PRIORITY_BACKGROUND);
            this.mStartNO = startNum;
            this.mEndNO = endNum;
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
                            String cacheRange = sParamsPreference.getDatingRange(mContext);
                            String cacheDate = sParamsPreference.getDatingPublishedDate(mContext);
                            retrieveDatingInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()), cacheRange, cacheDate, startNum, endNum);

                            break;
                        case RETRIEVE_DATA_WITH_DATE_FILTERED:
                            mUIEventsHandler.sendEmptyMessage(UI_SHOW_DIALOG);
                            String publishDate = (String) msg.obj;
                            Log.d(TAG, " inside the dating fragment BackgroundHandlerThread --> the publishDate we need to filter are : " + publishDate);
                            String dateCacheRange = sParamsPreference.getDatingRange(mContext);
                            // 每次筛选请求都是从零开始的重新请求
                            retrieveDatingInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()), dateCacheRange, publishDate, 0, 9);
                            break;
                        case RETRIEVE_DATA_WITH_RANGE_FILTERED:
                            mUIEventsHandler.sendEmptyMessage(UI_SHOW_DIALOG);
                            String range = (String) msg.obj;
                            Log.d(TAG, " inside the dating fragment BackgroundHandlerThread --> the range we need to filter are : " + range);
                            String rangeCacheDate = sParamsPreference.getDatingPublishedDate(mContext);
                            // 我们需要从0开始重新请求
                            retrieveDatingInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()), range, rangeCacheDate, 0, 9);
                            break;
                    }
                }
            };
            if (Utils.networkAvaiable(mContext))
            {
                fetchDatingData(mStartNO, mEndNO);
            }
        }

        public void fetchDatingData(final int startNum, final int endNum)
        {
            if (null != mWorkerHandler)
            {
                Message requestMsg = mWorkerHandler.obtainMessage(START_RETRIEVE_ALL_DATA);
                Bundle data = new Bundle();
                data.putInt(KEY_REQUEST_START_NUM, startNum);
                data.putInt(KEY_REQUEST_END_NUM, endNum);
                requestMsg.setData(data);
                mWorkerHandler.sendMessage(requestMsg);
            }
        }

        public void fetchDatingWithRangeFilter(String range)
        {
            if (! TextUtils.isEmpty(range) && mWorkerHandler != null)
            {
                mWorkerHandler.obtainMessage(RETRIEVE_DATA_WITH_RANGE_FILTERED, range).sendToTarget();
            }
        }

        public void fetchDatingWithPublishDateFilter(String publishDate)
        {
            Log.d(TAG, " inside the method of BackgroundHandler --> the published date we get are : " + publishDate);
            if (! TextUtils.isEmpty(publishDate) && mWorkerHandler != null)
            {
                mWorkerHandler.obtainMessage(RETRIEVE_DATA_WITH_DATE_FILTERED, publishDate).sendToTarget();
            }
        }

    }

    private boolean mRefresh;
    private boolean mLoadMore;
    private boolean mIsSavedInstance;

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
            String label = NearbyFragmentsCommonUtils.getLastedTime(mContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            if (Utils.networkAvaiable(getActivity()))
            {
                mRefresh = true;
                mLoadMore = false;
                if (null != mBackgroundHandler)
                {
                    mUIEventsHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBackgroundHandler.fetchDatingData(0, 9);
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

            mCurrentPos = mDatingList.size();

            if (mBeforeCount != mAfterCount && mRefresh)
            {
                mStarNum = mEndNum + (mAfterCount - mBeforeCount);
                mEndNum += 10 + (mAfterCount - mBeforeCount);
            } else
            {
                mStarNum = mEndNum + 1;
                mEndNum += 10;
            }
            mRefresh = false;
            if (Utils.networkAvaiable(getActivity()))
            {
                if (null != mBackgroundHandler)
                {
                    mUIEventsHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBackgroundHandler.fetchDatingData(mStarNum, mEndNum);
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



















































































































































































































