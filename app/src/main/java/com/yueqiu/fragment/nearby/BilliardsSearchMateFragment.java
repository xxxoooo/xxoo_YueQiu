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
import android.text.format.DateUtils;
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
import com.yueqiu.adapter.SearchMateSubFragmentListAdapter;
import com.yueqiu.adapter.SearchPopupBaseAdapter;
import com.yueqiu.bean.SearchMateSubFragmentUserBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.daoimpl.SearchMateDaoImpl;
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

import java.util.ArrayList;
import java.util.Arrays;
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
public class BilliardsSearchMateFragment extends Fragment
{
    private static final String TAG = "DeskBallFragment";

    public static final String BILLIARD_SEARCH_TAB_NAME = "billiard_search_tab_name";
    private View mView;
    private String mArgs;
    private static Context sContext;

    private static PullToRefreshListView sSubFragmentList;

    private static Button sBtnDistanceFilter, sBtnGenderFilter;

    @SuppressLint("ValidFragment")
    public BilliardsSearchMateFragment()
    {
    }

    private static final String KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT = "keyBilliardsSearchParentFragment";

    private static SearchParamsPreference sParamsPreference = SearchParamsPreference.getInstance();

    public static BilliardsSearchMateFragment newInstance(Context context, String params)
    {
        sContext = context;
        BilliardsSearchMateFragment fragment = new BilliardsSearchMateFragment();

        Bundle args = new Bundle();
        args.putString(KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT, params);

        return fragment;
    }

    private static SearchMateDaoImpl sMateDaoImpl;

    // mIsHead用于控制数据的加载到List当中的方向(即加载到头部还是加载到尾部)
    private boolean mIsHead;
    private boolean mIsNetworkAvailable;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        sWorker = new BackgroundWorker();

        sMateDaoImpl = new SearchMateDaoImpl(sContext);

        mIsHead = false;
        mIsNetworkAvailable = Utils.networkAvaiable(sContext);

    }

    private static ProgressBar sPreProgress;
    private static TextView sPreTextView;
    private static Drawable sProgressDrawable;
    private static List<SearchMateSubFragmentUserBean> sUserList = new ArrayList<SearchMateSubFragmentUserBean>();

    private static SearchMateSubFragmentListAdapter sMateListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.search_mate_fragment_layout, container, false);
        // then, inflate the image view pager
        SubFragmentsCommonUtils.initViewPager(sContext, mView, R.id.mate_fragment_gallery_pager, R.id.mate_fragment_gallery_pager_indicator_group);

        sSubFragmentList = (PullToRefreshListView) mView.findViewById(R.id.search_sub_fragment_list);
        sSubFragmentList.setMode(PullToRefreshBase.Mode.BOTH);
        sSubFragmentList.setOnRefreshListener(mOnRefreshListener);

        sPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        sPreTextView = (TextView) mView.findViewById(R.id.pre_text);
        sProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = sPreProgress.getIndeterminateDrawable().getBounds();
        sPreProgress.setIndeterminateDrawable(sProgressDrawable);
        sPreProgress.getIndeterminateDrawable().setBounds(bounds);

        (sBtnDistanceFilter = (Button) mView.findViewById(R.id.btn_mate_distance)).setOnClickListener(new BtnFilterClickListener());
        (sBtnGenderFilter = (Button) mView.findViewById(R.id.btn_mate_gender)).setOnClickListener(new BtnFilterClickListener());

        Bundle args = getArguments();
        mArgs = args.getString(BILLIARD_SEARCH_TAB_NAME);

        // TODO: 以下加载是测试数据，暂时不能删除(因为现在的数据不完整，我们还需要这些测试数据来查看数据加载完整的具体的具体的UI效果)
//        initListViewDataSrc();
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

        if (sWorker != null && sWorker.getState() == Thread.State.NEW) {
            Log.d(TAG, " the sWorker has started ");
            sWorker.start();
        }
    }

    @Override
    public void onPause()
    {
        // TODO: 如果此时我们请求到新的数据或者服务器端提供了消息推送的服务，我们这个时候需要
        // TODO: 以Notification的方式来通知用户消息的接收

        super.onPause();
    }

    @Override
    public void onStop()
    {
        // TODO: 我们在这里进行一些停止数据更新的操作，即停止任何同数据请求和处理的相关工作,然后再调用super.onStop()
        // TODO: 我们目前采用的策略只是简单的直接获取数据的方式，如果需要升级我们还需要通过添加BroadcastReceiver来
        // TODO: 监听数据的获取状态，然后在onStop()方法当中解注册这个BroadcastReceiver

        super.onStop();
    }


    /**
     * the button on click listener for the button to filter out the
     * list item we need
     */
    private static class BtnFilterClickListener implements View.OnClickListener
    {
        private LayoutInflater inflater = (LayoutInflater) sContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        private PopupWindow popupWindow;

        @Override
        public void onClick(View v)
        {
            switch (v.getId()) {
                case R.id.btn_mate_gender:
                    final String[] genderStrList = {
                            sContext.getResources().getString(R.string.man), // 对于男，我们不是直接传递“男”，而是传递“1”，可以减少我们转换的开销
                            sContext.getResources().getString(R.string.woman) // 对于这个值，我们直接传递代表“女”的数字值“2”，减少我们转换的开销
                    };
                    View genderFilerView = inflater.inflate(R.layout.search_mate_subfragment_gender_popupwindow, null);

                    Button btnGenderNoFilter = (Button) genderFilerView.findViewById(R.id.btn_search_mate_gender_no_filter);

                    btnGenderNoFilter.setOnClickListener(new MatePopupInternalItemHandler());
                    ListView genderListView = (ListView) genderFilerView.findViewById(R.id.list_search_mate_gender_filter_list);
                    genderListView.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(genderStrList)));

                    popupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnGenderFilter, genderFilerView);
                    genderListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            final String gender = String.valueOf(position + 1);
                            sParamsPreference.setMateGender(sContext, gender);
                            Message msg = sUIEventsHandler.obtainMessage(START_RETRIEVE_DATA_WITH_GENDER_FILTER);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_GENDER_FILTERED, gender);
                            msg.setData(data);
                            sUIEventsHandler.sendMessage(msg);
                            popupWindow.dismiss();

                        }
                    });
                    break;
                case R.id.btn_mate_distance:
                    final String[] disStrList = {
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_500_str), // 500米以内
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_1000_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_2000_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_5000_str)
                    };

                    View distanceFilterView = inflater.inflate(R.layout.search_mate_subfragment_distance_popupwindow, null);
                    Button btnDistanceNoFilter = (Button) distanceFilterView.findViewById(R.id.search_mate_popupwindow_intro);
                    btnDistanceNoFilter.setOnClickListener(new MatePopupInternalItemHandler());
                    ListView distanList = (ListView) distanceFilterView.findViewById(R.id.list_search_mate_distance_filter_list);
                    distanList.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(disStrList)));

                    popupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnDistanceFilter, distanceFilterView);

                    distanList.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            // 我们要将我们传递的“500米以内”截取成“500”
                            String rawDistanceStr = disStrList[position];
                            final int len = rawDistanceStr.length();
                            String distanceVal = rawDistanceStr.substring(0, len - 3);
                            Log.d(TAG, " the finally string we need to request are : " + distanceVal);
                            sParamsPreference.setMateRange(sContext, distanceVal);
                            Message msg = sUIEventsHandler.obtainMessage(START_RETRIEVE_DATA_WITH_RANGE_FILTER);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_RANGE_FILTERED, distanceVal);
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

    private static final class MatePopupInternalItemHandler implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {

        }
    }


    /**
     * 用于请求首页当中的球友的信息列表
     *
     * @param startNo
     * @param endNo
     * @param distance 这个参数是可以为空的，当不为空的时候，就是我们进行筛选的时候
     * @param gender 这个参数是可以为空的，当不为空的时候，就是我们进行筛选的时候
     */
    private static void retrieveInitialMateInfoList(final int startNo, final int endNo, final String distance, final String gender)
    {
        if (!Utils.networkAvaiable(sContext)) {
            sUIEventsHandler.sendEmptyMessage(DATA_RETRIEVE_FAILED);
            sUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
            return;
        }

        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("start_no", startNo + "");
        requestParams.put("end_no", endNo + "");
        if (! TextUtils.isEmpty(distance))
        {
            requestParams.put("range", distance);
        }

        if (! TextUtils.isEmpty(gender))
        {
            requestParams.put("gender", gender);
        }

        String rawResult = HttpUtil.urlClient(HttpConstants.SearchMate.URL, requestParams, HttpConstants.RequestMethod.GET);

        Log.d(TAG, " the raw result we get for the mate fragment are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult)) {
            try {
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
                            SearchMateSubFragmentUserBean mateUserBean = new SearchMateSubFragmentUserBean(userId, imgUrl, userName, SubFragmentsCommonUtils.parseGenderStr(sContext, sex), district, String.valueOf(range));

                            sUserList.add(mateUserBean);
                        }
                        // TODO: 数据获取完之后，我们需要停止显示ProgressBar(这部分功能还需要进一步测试)
                        sUIEventsHandler.obtainMessage(DATA_RETRIEVE_SUCCESS, sUserList).sendToTarget();

                        sUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
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
                        data.putString(KEY_REQUEST_ERROR_MSG_MATE, initialObj.getString("msg"));
                        msg.setData(data);
                        sUIEventsHandler.sendMessage(msg);
                    }
                } else
                {
                    sUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                }


            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, " exception happened in parsing the json data we get, and the detailed reason are : " + e.toString());
            }
        }
    }

    private static final String KEY_REQUEST_ERROR_MSG_MATE = "keyRequestErrorMsgMate";

    private static final int DATA_HAS_BEEN_UPDATED = 1 << 8;

    private static final String KEY_REQUEST_RANGE_FILTERED = "keyRequestRangeFiltered";
    private static final String KEY_REQUEST_GENDER_FILTERED = "keyRequestGenderFiltered";

    private static final int START_RETRIEVE_ALL_DATA = 1 << 1;

    // 这里的KEY_MATE_LIST是用于向Bundle当中存储和获取我们从SQLite当中以及从网络当中获取到的mateList的内容
    private static final String KEY_MATE_LIST = "keyMateList";

    private static final int DATA_RETRIEVE_SUCCESS = 1 << 2;
    private static final int DATA_RETRIEVE_FAILED = 1 << 3;

    private static final int START_RETRIEVE_DATA_WITH_RANGE_FILTER = 1 << 4;
    private static final int START_RETRIEVE_DATA_WITH_GENDER_FILTER = 1 << 5;

    // 同UI相关的事件的两个消息
    private static final int SHOW_PROGRESSBAR = 1 << 6;
    private static final int HIDE_PROGRESSBAR = 1 << 7;

    private static final int UPDATE_LOCAL_MATE_TABLE = 1 << 9;

    // 这个Handler主要是用于处理UI相关的事件,例如涉及到UI的事件的直接处理，例如Toast或者ProgressBar的显示控制
    private static Handler sUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case DATA_RETRIEVE_FAILED:
                    Toast.makeText(sContext, sContext.getResources().getString(R.string.network_unavailable_hint_info_str), Toast.LENGTH_SHORT).show();
                    hideProgress();

                    break;
                case DATA_RETRIEVE_SUCCESS:
                    // TODO: 我们会将我们从网络上以及从本地数据库当中检索到的数据
                    // TODO: 都会通过消息通知的形式发送到这里，因为这样就可以保证我们所有的涉及到UI工作都是在UI线程当中完成的
                    sBeforeCount = sUserList.size();
                    List<SearchMateSubFragmentUserBean> mateList = (ArrayList<SearchMateSubFragmentUserBean>) msg.obj;
                    final int size = mateList.size();
                    int i;
                    for (i = 0; i < size; ++i)
                    {
                        if (! sUserList.contains(mateList.get(i)))
                        {
                            sUserList.add(mateList.get(i));
                        }
                    }

                    // 更新一下本地数据库
                    sWorker.updateMateTable(sUserList);

                    if (sUserList.isEmpty())
                    {
                        loadEmptyTv();
                    }

                    break;
                case SHOW_PROGRESSBAR:
                    showProgress();
                    Log.d(TAG, " start showing the progress bar ");

                    break;
                case HIDE_PROGRESSBAR:
                    sMateListAdapter.notifyDataSetChanged();
                    hideProgress();
                    Log.d(TAG, " hiding the progress bar ");
                    break;

                case START_RETRIEVE_DATA_WITH_GENDER_FILTER:
                    Bundle genderData = msg.getData();
                    String gender = genderData.getString(KEY_REQUEST_GENDER_FILTERED);
                    sWorker.fetchDataWithGenderFiltered(gender);

                    break;

                case START_RETRIEVE_DATA_WITH_RANGE_FILTER:
                    Bundle rangeData = msg.getData();
                    String range = rangeData.getString(KEY_REQUEST_RANGE_FILTERED);
                    sWorker.fetchDataWithRangeFilter(range);

                    break;

                case DATA_HAS_BEEN_UPDATED:

                    sMateListAdapter.notifyDataSetChanged();
                    Log.d(TAG, " the adapter has been notified ");
                    break;

                case PublicConstant.TIME_OUT:
                    // 超时之后的处理策略
                    Utils.showToast(sContext, sContext.getString(R.string.http_request_time_out));
                    if (sUserList.isEmpty()) {
                        loadEmptyTv();
                    }
                    break;

                case PublicConstant.NO_RESULT:
                    if (sUserList.isEmpty()) {
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
                        Utils.showToast(sContext, errorData.getString(KEY_REQUEST_ERROR_MSG_MATE));
                    } else {
                        Utils.showToast(sContext, sContext.getString(R.string.http_request_error));
                    }

                    if (sUserList.isEmpty()) {
                        loadEmptyTv();
                    }

                    break;
            }
            sMateListAdapter = new SearchMateSubFragmentListAdapter(sContext, (ArrayList<SearchMateSubFragmentUserBean>) sUserList);
            sSubFragmentList.setAdapter(sMateListAdapter);
            sMateListAdapter.notifyDataSetChanged();
        }
    };

    private static void loadEmptyTv()
    {
        SubFragmentsCommonUtils.setFragmentEmptyTextView(sContext, sSubFragmentList, sContext.getString(R.string.search_activity_subfragment_empty_tv_str));
    }

    private static void showProgress()
    {
        Log.d(TAG, " showing the progress bar ");
        sPreProgress.setVisibility(View.VISIBLE);
        sPreTextView.setVisibility(View.VISIBLE);
    }

    private static void hideProgress()
    {
        Log.d(TAG, " hiding the progress bar ");
        sPreProgress.setVisibility(View.GONE);
        sPreTextView.setVisibility(View.GONE);
    }

    private static final String WORKER_NAME = "BackgroundWorker";
    private static BackgroundWorker sWorker;

    // 这个Handler是真正在后台当中控制所有繁重任务的Handler，包括基本的网络请求和从数据库当中检索数据
    private static class BackgroundWorker extends HandlerThread
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
                            sUIEventsHandler.sendEmptyMessage(SHOW_PROGRESSBAR);
                            retrieveInitialMateInfoList(0, 9, "", "");

                            break;
                        case START_RETRIEVE_DATA_WITH_RANGE_FILTER:
                            Bundle rangeData = msg.getData();
                            String range = rangeData.getString(KEY_REQUEST_RANGE_FILTERED);
                            Log.d(TAG, " inside the workThread --> start filtering the mate list based on the range of the current user " + range);

                            sUIEventsHandler.sendEmptyMessage(SHOW_PROGRESSBAR);
                            // TODO: 对于筛选工作，我们通过网络请求来完成
                            if (! TextUtils.isEmpty(sParamsPreference.getMateGender(sContext)))
                            {
                                retrieveInitialMateInfoList(0, 9, range, sParamsPreference.getMateGender(sContext));
                            }

                            break;
                        case START_RETRIEVE_DATA_WITH_GENDER_FILTER:
                            Bundle genderData = msg.getData();
                            String gender = genderData.getString(KEY_REQUEST_GENDER_FILTERED);
                            Log.d(TAG, " inside the workThread --> start filtering the mate list based on the gender of the current user " + gender);
                            sUIEventsHandler.sendEmptyMessage(SHOW_PROGRESSBAR);

                            if (! TextUtils.isEmpty(sParamsPreference.getMateRange(sContext)))
                            {
                                // TODO: 现在我们正式完成了筛选的工作了，但是却还有一个问题，那就是我们
                                // TODO: 确定所要加载的条目
                                retrieveInitialMateInfoList(0, 9, sParamsPreference.getMateRange(sContext), gender);
                            }

                            break;

                        case UPDATE_LOCAL_MATE_TABLE:
                            // 更新我们所获得的本地的数据库
                            List<SearchMateSubFragmentUserBean> mateListToLocal = (ArrayList<SearchMateSubFragmentUserBean>) msg.obj;
                            // TODO: 同本地数据库建立连接，用于更新我们的本地数据

                            break;

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
            Message msg = mBackgroundHandler.obtainMessage(START_RETRIEVE_DATA_WITH_RANGE_FILTER);
            Bundle data = msg.getData();
            data.putString(KEY_REQUEST_RANGE_FILTERED, range);
            msg.setData(data);

            mBackgroundHandler.sendMessage(msg);
        }

        public void fetchDataWithGenderFiltered(String gender)
        {
            Message msg = mBackgroundHandler.obtainMessage(START_RETRIEVE_DATA_WITH_GENDER_FILTER);
            Bundle data = msg.getData();
            data.putString(KEY_REQUEST_GENDER_FILTERED, gender);
            msg.setData(data);

            mBackgroundHandler.sendMessage(msg);
        }


        /**
         * 用于将我们得到的mateList来更新我们创建的本地数据库
         *
         * @param mateList
         */
        public void updateMateTable(final List<SearchMateSubFragmentUserBean> mateList)
        {
            if (mateList != null && ! mateList.isEmpty())
            {
                mBackgroundHandler.obtainMessage(UPDATE_LOCAL_MATE_TABLE, mateList);
            }
        }

        public void exit()
        {
            mBackgroundHandler.getLooper().quit();
        }
    }

    @Override
    public void onDestroy()
    {
        if (null != sWorker) {
            sWorker.exit();
        }
        super.onDestroy();
    }

    private static boolean sRefresh;
    private static boolean sLoadMore;

    private static int sStartNum = 0;
    private static int sEndNum = 9;
    // 用于定义当前MateList当中的list的position，帮助我们确定从第几条开始请求数据
    private static int sCurrentPos;
    private static int sBeforeCount, sAfterCount;

    private PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener = new PullToRefreshBase.OnRefreshListener2<ListView>()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            String lable = SubFragmentsCommonUtils.getLastedTime(sContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(lable);

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (Utils.networkAvaiable(sContext))
                    {
                        sRefresh = true;
                        sLoadMore = false;
                        // 跟分析球厅RoomFragment当中请求最新数据的原理一样，当用户进行下拉刷新
                        // 的时候，一定是再要求最新的数据，那么我们一定是要从第0条开始加载，一次加载10条，
                        // 即从0到9
                        retrieveInitialMateInfoList(0, 9, "", "");
                    } else
                    {
                        Toast.makeText(sContext, sContext.getString(R.string.network_not_available), Toast.LENGTH_LONG).show();
                    }

                }
            }).start();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            Log.d(TAG, " the user has touched the end of the current list in the BilliardsSearchMateFragment ");
            String label = SubFragmentsCommonUtils.getLastedTime(sContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            sUIEventsHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    sLoadMore = true;
                    sCurrentPos = sUserList.size();
                    if (sBeforeCount != sAfterCount)
                    {
                        sStartNum = sEndNum + (sAfterCount - sBeforeCount);
                        sEndNum += 10 - (sAfterCount - sBeforeCount);
                    } else {
                        sStartNum = sEndNum + 1;
                        sEndNum += 10;
                    }

                    if (Utils.networkAvaiable(sContext))
                    {
                        retrieveInitialMateInfoList(sStartNum, sEndNum, "", "");
                    } else
                    {
                        // 从数据库当中进行检索
                        // 此时用于不一定已经滑动了最底部，而只是可能我们一开始只是加载了10条，我们现在
                        // 需要加载更多，但是如果网络不行的话，我们不能通过下拉刷新加载更新的数据，但是我们
                        // 还是可以通过数据库检索我们之前加载过的历史数据(如果我们发现数据库当中也没有更多的数据可供加载了，那么我们就要
                        // 告诉用户没有可用的数据供加载了)
                        // 所以以下就是我们加载历史数据的过程
                        // 因为我们对历史数据的加载也是有条数限制的，在这里我们也是限制为一次加载10条
                        Log.d(TAG, " have touch the end of the list--> we need to fetch data from database , " +
                                "and the startNumber are : " + sStartNum + " , and the endNumber are : " + sEndNum);
                        List<SearchMateSubFragmentUserBean> mateList = sMateDaoImpl.getMateList(sStartNum, (sEndNum - sStartNum) + 1);
                        Log.d(TAG, " the mate list we get from the SQLite are : " + mateList.size());
                        if (! mateList.isEmpty())
                        {
                            sUIEventsHandler.obtainMessage(DATA_RETRIEVE_SUCCESS, mateList).sendToTarget();
                        } else
                        {
                            sUIEventsHandler.obtainMessage(DATA_RETRIEVE_FAILED, mateList).sendToTarget();
                        }
                    }
                }
            }, 1000);

        }
    };



    // TODO: the following are just for testing
    // TODO: and remove all of them out with the true data we retrieved from RESTful WebService
    private void initListViewDataSrc()
    {
        int i;
        for (i = 0; i < 100; ++i) {
            sUserList.add(new SearchMateSubFragmentUserBean("", "", "月夜流沙", "男", "昌平区", "20000米以内"));
        }
    }
}


















