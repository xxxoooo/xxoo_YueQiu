package com.yueqiu.fragment.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.yueqiu.bean.SearchDatingSubFragmentDatingBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.fragment.search.common.SubFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.XListView;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by scguo on 14/12/30.
 * <p/>
 * 用于SearchActivity当中的约球子Fragment的实现
 */
public class BilliardsSearchDatingFragment extends Fragment implements XListView.IXListViewListener
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

    private boolean mIsHead;
    private static boolean sNetworkAvailable;

    private static BackgroundWorkerHandler sBackgroundHandler;

    private static ProgressBar sPreProgress;
    private static Drawable sProgressDrawable;
    private static TextView sPreText;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        sBackgroundHandler = new BackgroundWorkerHandler();

        mIsHead = false;

        // TODO: 我们最好将判断网络状况的变量放到onResume()方法当中进行判断，而不是放到onCreate()方法，因为这里调用的次数有限
        // TODO: 但是将她直接放到onResume()方法当中又会使Fragment之间的切换变的卡。稍后在做决定？？？？
        sNetworkAvailable = Utils.networkAvaiable(sContext);
    }

    public static final String KEY_DATING_FRAGMENT = "BilliardsSearchDatingFragment";

    private View mView;
    private static Button sBtnDistan, sBtnPublishDate;
    private XListView mDatingListView;
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

        mDatingListView = (XListView) mView.findViewById(R.id.search_dating_subfragment_list);
        mDatingListView.setXListViewListener(this);

        // 加载Progressbar
        sPreText = (TextView) mView.findViewById(R.id.pre_text);
        sPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        sProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = sPreProgress.getIndeterminateDrawable().getBounds();
        sPreProgress.setIndeterminateDrawable(sProgressDrawable);
        sPreProgress.getIndeterminateDrawable().setBounds(bounds);

        sUIEventsHandler.sendEmptyMessage(START_RETRIEVE_ALL_DATA);

        sUIEventsHandler.sendEmptyMessage(START_RETRIEVE_ALL_DATA);

        // TODO: 以下加载的是测试数据，我们以后需要移除, 但是目前还不能移除，只是暂时注释掉，用于展示完整的UI效果
//        initTestData();

        sDatingListAdapter = new SearchDatingSubFragmentListAdapter(sContext, (ArrayList<SearchDatingSubFragmentDatingBean>) sDatingList);
        mDatingListView.setAdapter(sDatingListAdapter);
        sDatingListAdapter.notifyDataSetChanged();
        mDatingListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                SearchDatingSubFragmentDatingBean bean = sDatingList.get(position - 1);
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
        if (sBackgroundHandler != null && sBackgroundHandler.getState() == Thread.State.NEW)
        {
            Log.d(TAG, " start the background handler ");
            sBackgroundHandler.start();
        }
    }

    @Override
    public void onDestroy()
    {
        sBackgroundHandler.quit();
        super.onDestroy();
    }

    // TODO: 一下就是XListView的监听事件，分别用于处理下滑加载更多的处理事件(通常而言我们用到的更多的事件处理过程都是下滑加载更多)
    // pullToRefresh(即下滑加载更过)
    @Override
    public void onRefresh()
    {
        Log.d(TAG, " to add more data while we pull the listView ");

        mIsHead = true;

    }
    // 上滑加载更多
    @Override
    public void onLoadMore()
    {
        Log.d(TAG, " add more data while we have touched the end of the ListView ");

        mIsHead = false;
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
                            final String rangeStr = disStrList[position];
                            Message msg = sUIEventsHandler.obtainMessage(RETRIEVE_DATA_WITH_RANGE_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_RANGE_STR, rangeStr);
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
                            sContext.getResources().getString(R.string.search_dating_popupwindow_other),
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
                            final String dateStr = dateStrList[position];
                            Message msg = sUIEventsHandler.obtainMessage(RETRIEVE_DATA_WITH_DATE_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_DATE_STR, dateStr);
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
            switch (v.getId())
            {
                case R.id.btn_search_dating_popup_no_filter:
                    Toast.makeText(sContext, "No filtering, list all", Toast.LENGTH_LONG).show();
                    break;

            }
        }
    }

    /**
     *
     * @param userId
     * @param range 发布约球信息的大致距离,距离范围。例如1000米以内,具体传递的形式例如range=100
     * @param date 发布日期，例如date=2014-04-04
     * @param startNum 请求信息的开始的条数的数目(当我们进行分页请求的时候，我们就会用到这个特性，即每次当用户滑动到列表低端或者当用户滑动更新的时候，我们需要
     *                 通过更改startNum的值来进行分页加载的具体实现)
     *                 例如start_no=0
     * @param endNum 请求列表信息的结束条目，例如我们可以一次只加载10条，当用户请求的时候再加载更多的数据,例如end_no=9
     *
     */
    private static void retrieveDatingInfo(final String userId, final String range, final int date, final int startNum, final int endNum)
    {
        if (! sNetworkAvailable)
        {
            Message failMsg = sUIEventsHandler.obtainMessage(FETCH_DATA_FAILED);
            Bundle failInfo = new Bundle();
            failInfo.putString(KEY_REASON_FAIL, sContext.getResources().getString(R.string.network_unavailable_hint_info_str));
            failMsg.setData(failInfo);
            Log.d(TAG, "we have send the fail info to the UIEventsHandler ");
            sUIEventsHandler.sendMessage(failMsg);

            return ;
        }

        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("user_id", userId);
        requestParams.put("range", range);
        requestParams.put("date", date + "");
        requestParams.put("start_no", startNum + "");
        requestParams.put("end_no", endNum + "");

        String rawResult = HttpUtil.urlClient(HttpConstants.SearchDating.URL, requestParams, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the raw result we get for the dating info are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult))
        {
            try {
                JSONObject rawJsonObj = new JSONObject(rawResult);
                Log.d(TAG, " the rawJson object we get are : " + rawJsonObj);
                if (! TextUtils.isEmpty(rawJsonObj.toString()))
                {
                    final int statusCode = rawJsonObj.getInt("code");
                    if (statusCode == HttpConstants.ResponseCode.NORMAL)
                    {
                        // TODO: 然后进行以后的具体的解析过程的处理
                        JSONArray resultJsonArr = rawJsonObj.getJSONArray("result");
                        final int size = resultJsonArr.length();
                        int i;
                        for ( i = 0; i < size; ++i)
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
                            // TODO: 我们应该在这里通知UI主线程数据请求工作已经全部完成了，停止显示ProgressBar或者显示一个Toast全部数据已经加载完的提示
                            sUIEventsHandler.sendEmptyMessage(FETCH_DATA_SUCCESSED);
                            sUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                        }

                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, " exception happened in parsing the json data we get, and the detailed reason are : " + e.toString());
            }
        }
    }

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
                    Log.d(TAG, " fail to get data due to the reason as : " + infoStr);
                    break;
                case FETCH_DATA_SUCCESSED:
                    break;

                case RETRIEVE_DATA_WITH_RANGE_FILTERED:
                    Bundle rangeData = msg.getData();
                    String range = rangeData.getString(KEY_REQUEST_RANGE_STR);
                    sBackgroundHandler.fetchDatingWithRangeFilter(range);

                    break;
                case RETRIEVE_DATA_WITH_DATE_FILTERED:
                    Bundle publishDateData = msg.getData();
                    String publishDate = publishDateData.getString(KEY_REQUEST_DATE_STR);
                    sBackgroundHandler.fetchDatingWithPublishDateFilter(publishDate);

                    break;

            }
        }
    };

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
                    switch (msg.what)
                    {
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
            Message msg = mWorkerHandler.obtainMessage(RETRIEVE_DATA_WITH_DATE_FILTERED);
            Bundle data = new Bundle();
            data.putString(KEY_REQUEST_DATE_STR, publishDate);
            msg.setData(data);
            mWorkerHandler.sendMessage(msg);
        }

    }

    // TODO: 以下都是测试数据,在测试接口的时候将他们删除掉
    private void initTestData()
    {
        int i;
        for (i = 0; i < 200; ++i) {
            sDatingList.add(new SearchDatingSubFragmentDatingBean("", "", "月夜流水", "第N届斯诺克大力神杯就要开始，一起参加啊！", "230米以内"));
        }
    }


}



















































































































































































































