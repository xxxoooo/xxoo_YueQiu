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
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.activity.SearchBilliardsDatingActivity;
import com.yueqiu.adapter.SearchDatingSubFragmentListAdapter;
import com.yueqiu.adapter.SearchPopupBaseAdapter;
import com.yueqiu.bean.SearchDatingDetailedAlreadyBean;
import com.yueqiu.bean.SearchDatingSubFragmentDatingBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.nearby.common.SearchParamsPreference;
import com.yueqiu.fragment.nearby.common.SubFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
public class BilliardsSearchDatingFragment extends Fragment
{
    private static final String TAG = "BilliardsSearchDatingFragment";

    private static Context sContext;

    public BilliardsSearchDatingFragment()
    {
    }

    public static BilliardsSearchDatingFragment newInstance(Context context, String params)
    {
        sContext = context;
        BilliardsSearchDatingFragment instance = new BilliardsSearchDatingFragment();

        Bundle args = new Bundle();
        args.putString(KEY_DATING_FRAGMENT, params);
        instance.setArguments(args);

        return instance;
    }

    private static boolean sNetworkAvailable;

    private static BackgroundWorkerHandler sBackgroundHandler;

    private static ProgressBar sPreProgress;
    private static Drawable sProgressDrawable;
    private static TextView sPreText;

    private static SearchParamsPreference sParamsPreference = SearchParamsPreference.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        sBackgroundHandler = new BackgroundWorkerHandler();

        // TODO: 我们最好将判断网络状况的变量放到onResume()方法当中进行判断，而不是放到onCreate()方法，因为这里调用的次数有限
        // TODO: 但是将她直接放到onResume()方法当中又会使Fragment之间的切换变的卡。稍后在做决定？？？？
        sNetworkAvailable = Utils.networkAvaiable(sContext);
    }

    public static final String KEY_DATING_FRAGMENT = "BilliardsSearchDatingFragment";

    private View mView;
    private static Button sBtnDistan, sBtnPublishDate;
    private static PullToRefreshListView sDatingListView;
    private static List<SearchDatingSubFragmentDatingBean> sDatingList = new ArrayList<SearchDatingSubFragmentDatingBean>();

    private static SearchDatingSubFragmentListAdapter sDatingListAdapter;
    // TODO: mArgs是我们在初始化Fragment时需要接受来自初始化这个Fragment所传递的参数的容器，
    // TODO: 只不过我们现在没有用到，但是这个参数是我们更好的封装Fragment的基础，不要忽略
    private Bundle mArgs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.search_dating_fragment_layout, container, false);

        SubFragmentsCommonUtils.initViewPager(sContext, mView, R.id.dating_frament_gallery_pager, R.id.dating_fragment_gallery_pager_indicator_group);

        (sBtnDistan = (Button) mView.findViewById(R.id.btn_dating_distance)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnPublishDate = (Button) mView.findViewById(R.id.btn_dating_publichdate)).setOnClickListener(new OnFilterBtnClickListener());

        Bundle args = getArguments();
        mArgs = args;

        sDatingListView = (PullToRefreshListView) mView.findViewById(R.id.search_dating_subfragment_list);
        sDatingListView.setMode(PullToRefreshBase.Mode.BOTH);
        sDatingListView.setOnRefreshListener(mOnRefreshListener);


        // 加载Progressbar
        sPreText = (TextView) mView.findViewById(R.id.pre_text);
        sPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        sProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = sPreProgress.getIndeterminateDrawable().getBounds();
        sPreProgress.setIndeterminateDrawable(sProgressDrawable);
        sPreProgress.getIndeterminateDrawable().setBounds(bounds);

        // TODO: 以下加载的是测试数据，我们以后需要移除, 但是目前还不能移除，只是暂时注释掉，用于展示完整的UI效果
//        initTestData();

        // TODO: 我们仍然需要进一步的测试，来确定Adapter的最终正确的加载的位置
        sDatingListAdapter = new SearchDatingSubFragmentListAdapter(sContext, (ArrayList<SearchDatingSubFragmentDatingBean>) sDatingList);
        sDatingListView.setAdapter(sDatingListAdapter);
        sDatingListAdapter.notifyDataSetChanged();
        sDatingListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                SearchDatingSubFragmentDatingBean bean = sDatingList.get(position);
                Bundle args = new Bundle();
                args.putString(SubFragmentsCommonUtils.KEY_DATING_FRAGMENT_PHOTO, bean.getUserPhoto());


                Intent intent = new Intent(sContext, SearchBilliardsDatingActivity.class);
                intent.putExtra(SubFragmentsCommonUtils.KEY_BUNDLE_SEARCH_DATING_FRAGMENT, args);
                sContext.startActivity(intent);
            }
        });

        return mView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (sBackgroundHandler != null && sBackgroundHandler.getState() == Thread.State.NEW) {
            Log.d(TAG, " start the background handler ");
            sBackgroundHandler.start();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    private static class OnFilterBtnClickListener implements View.OnClickListener
    {
        private LayoutInflater inflater = (LayoutInflater) sContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        private PopupWindow popupWindow;

        @Override
        public void onClick(View v)
        {
            switch (v.getId()) {
                case R.id.btn_dating_distance:
                    final String[] disStrList = {
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_500_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_1000_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_2000_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_5000_str)
                    };

                    View distanPopupView = inflater.inflate(R.layout.search_mate_subfragment_distance_popupwindow, null);

                    Button btnDistanceNoFilter = (Button) distanPopupView.findViewById(R.id.search_mate_popupwindow_intro);
                    btnDistanceNoFilter.setOnClickListener(new DatingPopupWindowInternalClickHandler());
                    ListView distanList = (ListView) distanPopupView.findViewById(R.id.list_search_mate_distance_filter_list);
                    distanList.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(disStrList)));

                    popupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnDistan, distanPopupView);
                    distanList.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            String rawRangeStr = disStrList[position];
                            final int len = rawRangeStr.length();
                            String range = rawRangeStr.substring(0, len - 3);

                            sParamsPreference.setDatingRange(sContext, range);

                            Message msg = sUIEventsHandler.obtainMessage(RETRIEVE_DATA_WITH_RANGE_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_RANGE_STR, range);
                            msg.setData(data);
                            sUIEventsHandler.sendMessage(msg);

                            popupWindow.dismiss();
                        }
                    });

                    break;
                case R.id.btn_dating_publichdate:

                    final String[] dateStrList = {
                            sContext.getResources().getString(R.string.search_dating_popupwindow_one),
                            sContext.getResources().getString(R.string.search_dating_popupwindow_two),
                            sContext.getResources().getString(R.string.search_dating_popupwindow_three),
                            sContext.getResources().getString(R.string.search_dating_popupwindow_four),
                            sContext.getResources().getString(R.string.search_dating_popupwindow_five),
                            sContext.getResources().getString(R.string.search_dating_popupwindow_six),
                            sContext.getResources().getString(R.string.search_dating_popupwindow_seven),
                            sContext.getResources().getString(R.string.billiard_other),
                    };

                    View datePopupView = inflater.inflate(R.layout.search_dating_subfragment_date_popupwindow, null);
                    Button btnDateNoFilter = (Button) datePopupView.findViewById(R.id.btn_search_dating_popup_no_filter);
                    btnDateNoFilter.setOnClickListener(new DatingPopupWindowInternalClickHandler());
                    ListView dateList = (ListView) datePopupView.findViewById(R.id.list_search_dating_date_filter_list);
                    dateList.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(dateStrList)));

                    popupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnPublishDate, datePopupView);

                    dateList.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            Log.d(TAG, " the item of the list date are clicked , and the date string are : " + dateStrList[position]);
                            final int timeInterval = position + 1;
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                            calendar.setTime(new Date());
                            // 因为我们需要的是以前发布的数据，所以我们这里是将我们得到的值进行减值操作，即直接加一个负值就可以完成操作了
                            calendar.add(Calendar.DAY_OF_MONTH, -timeInterval);
                            String specifiedDate = formatter.format(calendar.getTime());
                            sParamsPreference.setDatingPublishedDate(sContext, specifiedDate);
                            Log.d(TAG, " the data we need are : " + specifiedDate);

                            Message msg = sUIEventsHandler.obtainMessage(RETRIEVE_DATA_WITH_DATE_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_DATE_STR, specifiedDate);
                            msg.setData(data);
                            sUIEventsHandler.sendMessage(msg);

                            popupWindow.dismiss();
                        }
                    });
                    break;
                default:
                    break;

            }
        }
    }


    private static class DatingPopupWindowInternalClickHandler implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId()) {
                case R.id.btn_search_dating_popup_no_filter:
                    Toast.makeText(sContext, "No filtering, list all", Toast.LENGTH_LONG).show();
                    break;

            }
        }
    }

    /**
     * @param userId
     * @param range    发布约球信息的大致距离,距离范围。例如1000米以内,具体传递的形式例如range=100
     * @param date     发布日期，例如date=2014-04-04
     * @param startNum 请求信息的开始的条数的数目(当我们进行分页请求的时候，我们就会用到这个特性，即每次当用户滑动到列表低端或者当用户滑动更新的时候，我们需要
     *                 通过更改startNum的值来进行分页加载的具体实现)
     *                 例如start_no=0
     * @param endNum   请求列表信息的结束条目，例如我们可以一次只加载10条，当用户请求的时候再加载更多的数据,例如end_no=9
     */
    private static void retrieveDatingInfo(final String userId, final String range, final int date, final int startNum, final int endNum)
    {
        if (!sNetworkAvailable) {
            Message failMsg = sUIEventsHandler.obtainMessage(FETCH_DATA_FAILED);
            Bundle failInfo = new Bundle();
            failInfo.putString(KEY_REASON_FAIL, sContext.getResources().getString(R.string.network_unavailable_hint_info_str));
            failMsg.setData(failInfo);
            Log.d(TAG, "we have send the fail info to the UIEventsHandler ");
            sUIEventsHandler.sendMessage(failMsg);

            return;
        }

        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("user_id", userId);
        requestParams.put("range", range);
        requestParams.put("date", date + "");
        requestParams.put("start_no", startNum + "");
        requestParams.put("end_no", endNum + "");

        String rawResult = HttpUtil.urlClient(HttpConstants.SearchDating.URL, requestParams, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the raw result we get for the dating info are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult)) {
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
                                SearchDatingSubFragmentDatingBean datingBean = new SearchDatingSubFragmentDatingBean(datingId, imgUrl, userName, title, String.valueOf(distance));

                                // 将我们解析得到的datingBean插入到我们创建的数据库当中
                                sDatingList.add(datingBean);
                            }

                            // TODO: 我们应该在这里通知UI主线程数据请求工作已经全部完成了，停止显示ProgressBar或者显示一个Toast全部数据已经加载完的提示
                            sUIEventsHandler.obtainMessage(FETCH_DATA_SUCCESSED, sDatingList).sendToTarget();
                            sUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                        } else if (statusCode == HttpConstants.ResponseCode.TIME_OUT)
                        {
                            sUIEventsHandler.sendEmptyMessage(PublicConstant.TIME_OUT);

                        } else if (statusCode == HttpConstants.ResponseCode.NO_RESULT)
                        {
                            sUIEventsHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        } else
                        {
                            Message msg = sUIEventsHandler.obtainMessage(PublicConstant.REQUEST_ERROR);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_ERROR_DATING, rawJsonObj.getString("msg"));
                            Log.d(TAG, " the detailed request error message we retrieved are : " + rawJsonObj.get("msg"));
                            msg.setData(data);
                            sUIEventsHandler.sendMessage(msg);
                        }
                    } else
                    {
                        sUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, " exception happened in parsing the json data we get, and the detailed reason are : " + e.toString());
            }
        }
    }

    private static final String KEY_REQUEST_ERROR_DATING = "keyRequestErrorDating";

    private static final int DATA_HAS_BEEN_UPDATED = 1 << 8;


    private static final String KEY_REQUEST_RANGE_STR = "keyRequestRangeStr";
    private static final String KEY_REQUEST_DATE_STR = "keyRequestDateStr";

    private static final String KEY_REASON_FAIL = "reason_failure";


    private static final int UI_SHOW_DIALOG = 1 << 4;
    private static final int UI_HIDE_DIALOG = 1 << 5;

    private static final int START_RETRIEVE_ALL_DATA = 1 << 1;

    private static final int RETRIEVE_DATA_WITH_RANGE_FILTERED = 1 << 2;
    private static final int RETRIEVE_DATA_WITH_DATE_FILTERED = 1 << 3;

    private static final int FETCH_DATA_SUCCESSED = 1 << 6;
    private static final int FETCH_DATA_FAILED = 1 << 7;


    private static Handler sUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UI_HIDE_DIALOG:
                    sDatingListAdapter.notifyDataSetChanged();

                    hideProgress();
                    Log.d(TAG, " hiding the dialog ");
                    break;
                case UI_SHOW_DIALOG:
                    showProgress();
                    Log.d(TAG, " start showing the dialog ");
                    break;
                case FETCH_DATA_FAILED:
                    Log.d(TAG, " we have received the information of the failure network request ");
                    Bundle reasonBundle = msg.getData();
                    String infoStr = reasonBundle.getString(KEY_REASON_FAIL);
                    if (sDatingList.isEmpty())
                    {
                        loadEmptyTv();
                    }
                    Toast.makeText(sContext, infoStr, Toast.LENGTH_SHORT).show();

                    Log.d(TAG, " fail to get data due to the reason as : " + infoStr);
                    break;
                case FETCH_DATA_SUCCESSED:
                    List<SearchDatingSubFragmentDatingBean> datingList = (ArrayList<SearchDatingSubFragmentDatingBean>) msg.obj;
                    final int size = datingList.size();
                    int i;
                    for (i = 0; i < size; ++i)
                    {
                        if (! sDatingList.contains(datingList.get(i)))
                        {
                            sDatingList.add(datingList.get(i));
                        }
                    }

                    // TODO: 然后我们将我们在这里得到的sDatingList同步更新到数据库当中

                    if (sDatingList.isEmpty())
                    {
                        loadEmptyTv();
                    }

                    break;

                case RETRIEVE_DATA_WITH_RANGE_FILTERED:
                    Bundle rangeData = msg.getData();
                    String range = rangeData.getString(KEY_REQUEST_RANGE_STR);
                    sBackgroundHandler.fetchDatingWithRangeFilter(range);

                    break;
                case RETRIEVE_DATA_WITH_DATE_FILTERED:
                    Bundle publishDateData = msg.getData();
                    String publishDate = publishDateData.getString(KEY_REQUEST_DATE_STR);
                    Log.d(TAG, " inside the sUIEventsHandler --> we have received the date need to filter are : " + publishDate);
                    sBackgroundHandler.fetchDatingWithPublishDateFilter(publishDate);

                    break;

                case DATA_HAS_BEEN_UPDATED:

                    sDatingListAdapter.notifyDataSetChanged();
                    Log.d(TAG, " the adapter eof the DatingFragment has been updated ");
                    break;

                case PublicConstant.TIME_OUT:
                    // 超时之后的处理策略
                    Utils.showToast(sContext, sContext.getString(R.string.http_request_time_out));
                    if (sDatingList.isEmpty()) {
                        loadEmptyTv();
                    }
                    break;

                case PublicConstant.NO_RESULT:
                    if (sDatingList.isEmpty()) {
                        loadEmptyTv();
                    } else {
                        if (sLoadMore) {
                            Utils.showToast(sContext, sContext.getString(R.string.no_more_info));
                        }
                    }
                    break;

                case PublicConstant.REQUEST_ERROR:
                    Bundle errorData = msg.getData();
                    if (null != errorData) {
                        Utils.showToast(sContext, errorData.getString(KEY_REQUEST_ERROR_DATING));
                    } else {
                        Utils.showToast(sContext, sContext.getString(R.string.http_request_error));
                    }

                    if (sDatingList.isEmpty())
                    {
                        loadEmptyTv();
                    }

                    break;
            }


        }
    };

    private static void loadEmptyTv()
    {
        SubFragmentsCommonUtils.setFragmentEmptyTextView(sContext, sDatingListView, sContext.getString(R.string.search_activity_subfragment_empty_tv_str));
    }

    private static void showProgress()
    {
        sPreProgress.setVisibility(View.VISIBLE);
        sPreText.setVisibility(View.VISIBLE);
    }

    private static void hideProgress()
    {
        sPreProgress.setVisibility(View.GONE);
        sPreText.setVisibility(View.GONE);
    }

    private static final String BACKGROUDN_WORKER_NAME = "BackgroundWorkerHandler";

    private static class BackgroundWorkerHandler extends HandlerThread
    {
        public BackgroundWorkerHandler()
        {
            super(BACKGROUDN_WORKER_NAME, Process.THREAD_PRIORITY_BACKGROUND);
        }

        private Handler mWorkerHandler;

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
                            sUIEventsHandler.sendEmptyMessage(UI_SHOW_DIALOG);
                            retrieveDatingInfo("1", "", 1, 1, 1);
                            break;

                        case RETRIEVE_DATA_WITH_DATE_FILTERED:
                            Bundle publishDateData = msg.getData();
                            String publishDate = publishDateData.getString(KEY_REQUEST_DATE_STR);

                            Log.d(TAG, " inside the dating fragment BackgroundHandlerThread --> the publishDate we need to filter are : " + publishDate);

                            break;


                        case RETRIEVE_DATA_WITH_RANGE_FILTERED:
                            Bundle rangeData = msg.getData();
                            String range = rangeData.getString(KEY_REQUEST_RANGE_STR);

                            Log.d(TAG, " inside the dating fragment BackgroundHandlerThread --> the range we need to filter are : " + range);


                            break;
                    }
                }
            };

            fetchDatingData();
        }

        public void fetchDatingData()
        {
            mWorkerHandler.sendEmptyMessage(START_RETRIEVE_ALL_DATA);
        }

        public void fetchDatingWithRangeFilter(String range)
        {
            Message msg = mWorkerHandler.obtainMessage(RETRIEVE_DATA_WITH_RANGE_FILTERED);
            Bundle data = new Bundle();
            data.putString(KEY_REQUEST_RANGE_STR, range);
            msg.setData(data);
            mWorkerHandler.sendMessage(msg);
        }

        public void fetchDatingWithPublishDateFilter(String publishDate)
        {
            Log.d(TAG, " inside the method of BackgroundHandler --> the published date we get are : " + publishDate);
            Message msg = mWorkerHandler.obtainMessage(RETRIEVE_DATA_WITH_DATE_FILTERED);
            Bundle data = new Bundle();
            data.putString(KEY_REQUEST_DATE_STR, publishDate);
            msg.setData(data);
            mWorkerHandler.sendMessage(msg);
        }
    }

    private static boolean sRefresh;
    private static boolean sLoadMore;

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
            String label = SubFragmentsCommonUtils.getLastedTime(sContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (Utils.networkAvaiable(sContext)) {
                        sRefresh = true;
                        sLoadMore = false;
                        retrieveDatingInfo("1", "", 1, 0, 9);
                    } else {
                        Toast.makeText(sContext, sContext.getString(R.string.network_not_available), Toast.LENGTH_LONG).show();
                    }
                }
            }).start();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            String label = SubFragmentsCommonUtils.getLastedTime(sContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            sUIEventsHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    sLoadMore = true;
                    mCurrentPos = sDatingList.size();
                    if (mBeforeCount != mAfterCount) {
                        mStarNum = mEndNum + (mAfterCount - mBeforeCount);
                        mEndNum += 10 + (mAfterCount - mBeforeCount);
                    } else {
                        mStarNum = mEndNum + 1;
                        mEndNum += 10;
                    }

                    if (Utils.networkAvaiable(sContext)) {
                        retrieveDatingInfo("1", "", 1, mStarNum, mEndNum);
                    } else {
                        // TODO: 从本地的数据库进行检索

                    }
                }
            }, 1000);

        }
    };


    // TODO: 以下都是测试数据,在测试接口的时候将他们删除掉
    private void initTestData()
    {
        int i;
        for (i = 0; i < 200; ++i) {
            sDatingList.add(new SearchDatingSubFragmentDatingBean("", "", "月夜流水", "第N届斯诺克大力神杯就要开始，一起参加啊！", "230米以内"));
        }
    }


}



















































































































































































































