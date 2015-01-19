package com.yueqiu.fragment.search;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.yueqiu.adapter.SearchAssistCoauchSubFragmentListAdapter;
import com.yueqiu.adapter.SearchPopupBaseAdapter;
import com.yueqiu.bean.SearchAssistCoauchSubFragmentBean;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by scguo on 14/12/30.
 * <p/>
 * 用于SearchActivity当中的助教子Fragment的实现
 */
@SuppressLint("ValidFragment")
public class BilliardsSearchAssistCoauchFragment extends Fragment implements XListView.IXListViewListener
{
    private static final String TAG = "BilliardsSearchAssistCoauchFragment";

    public static final String BILLIARDS_SEARCH_ASSIST_COAUCH_FRAGMENT_TAB_NAME = "BilliardsSearchAssistCoauchFragment";

    // 用于展示助教信息的ListView
    private XListView mListView;
    private static Context sContext;

    @SuppressLint("ValidFragment")
    public BilliardsSearchAssistCoauchFragment()
    {
    }

    private static final String KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT = "keyBilliardsSearchAssistCoauchFragment";

    public static BilliardsSearchAssistCoauchFragment newInstance(Context context, String params)
    {
        sContext = context;
        BilliardsSearchAssistCoauchFragment instance = new BilliardsSearchAssistCoauchFragment();
        Bundle args = new Bundle();
        args.putString(KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT, params);

        return instance;
    }

    private boolean mIsHead;
    private static boolean sNetworkAvailable;

    private static BackgroundWorkerHandler sWorker;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mIsHead = false;

        sNetworkAvailable = Utils.networkAvaiable(sContext);
        sWorker = new BackgroundWorkerHandler();

    }

    private View mView;

    private static Button sBtnDistance, sBtnCost, sBtnKinds, sBtnLevel;

    private static ProgressBar sPreProgress;
    private static TextView sPreTextView;
    private static Drawable sProgressDrawable;

    private static SearchAssistCoauchSubFragmentListAdapter sAssistCoauchListAdapter;

    private static List<SearchAssistCoauchSubFragmentBean> sAssistCoauchList = new ArrayList<SearchAssistCoauchSubFragmentBean>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        mView = inflater.inflate(R.layout.search_assistcoauch_fragment_layout, container, false);

        SubFragmentsCommonUtils.initViewPager(sContext, mView, R.id.assistcoauch_fragment_gallery_pager, R.id.assistcoauch_fragment_gallery_pager_indicator_group);

        (sBtnDistance = (Button) mView.findViewById(R.id.btn_assistcoauch_distance)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnCost = (Button) mView.findViewById(R.id.btn_assistcoauch_cost)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnKinds = (Button) mView.findViewById(R.id.btn_assistcoauch_kinds)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnLevel = (Button) mView.findViewById(R.id.btn_assistcoauch_level)).setOnClickListener(new OnFilterBtnClickListener());

        mListView = (XListView) mView.findViewById(R.id.search_assistcoauch_subfragment_listview);
        mListView.setPullLoadEnable(true);
        mListView.setXListViewListener(this);

        sPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        sPreTextView = (TextView) mView.findViewById(R.id.pre_text);
        sProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = sPreProgress.getIndeterminateDrawable().getBounds();
        sPreProgress.setIndeterminateDrawable(sProgressDrawable);
        sPreProgress.getIndeterminateDrawable().setBounds(bounds);

        sUIEventsHandler.sendEmptyMessage(RETRIEVE_ALL_RAW_INFO);

        // TODO: 以下加载的是测试数据,但是我们目前还不能删除这个方法，因为我们还需要这些测试数据来查看整体的UI加载效果
//        initTestData();

        sAssistCoauchListAdapter = new SearchAssistCoauchSubFragmentListAdapter(sContext, (ArrayList<SearchAssistCoauchSubFragmentBean>) sAssistCoauchList);
        mListView.setAdapter(sAssistCoauchListAdapter);
        sAssistCoauchListAdapter.notifyDataSetChanged();

        return mView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (null != sWorker && sWorker.getState() == Thread.State.NEW)
        {
            sWorker.start();
        }
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    // TODO: 向下拉加载更多
    // TODO: 我们会通过mIsHead变量来控制新数据添加的方向，即是添加到List的头部还是尾部
    @Override
    public void onRefresh()
    {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mIsHead = true;

    }

    // TODO: 向上拉加载更多
    @Override
    public void onLoadMore()
    {
        mListView.stopRefresh();
        mListView.stopLoadMore();

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
                case R.id.btn_assistcoauch_distance:
                    final String[] disStrList = {
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_500_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_1000_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_2000_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_5000_str)
                    };

                    View distanceFilterView = inflater.inflate(R.layout.search_mate_subfragment_distance_popupwindow, null);
                    Button btnDistanceNoFilter = (Button) distanceFilterView.findViewById(R.id.search_mate_popupwindow_intro);
                    btnDistanceNoFilter.setOnClickListener(new AssistCoauchPopupInternalHandler());
                    ListView distanList = (ListView) distanceFilterView.findViewById(R.id.list_search_mate_distance_filter_list);
                    distanList.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(disStrList)));

                    popupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnDistance, distanceFilterView);

                    distanList.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            final String distanStr = disStrList[position];
                            Message msg = sUIEventsHandler.obtainMessage(RETRIEVE_INFO_WITH_DISTANCE_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_DISTANCE_FILTERED, distanStr);
                            msg.setData(data);

                            sUIEventsHandler.sendMessage(msg);

                            popupWindow.dismiss();
                        }
                    });

                    break;
                case R.id.btn_assistcoauch_cost:
                    final String[] priceArr = {
                            sContext.getResources().getString(R.string.search_room_price_popupwindow_lowtohigh),
                            sContext.getResources().getString(R.string.search_room_price_popupwindow_lowtohigh)

                    };
                    View priceFilterView = inflater.inflate(R.layout.search_assistcoauch_subfragment_price_popupwindow, null);
                    Button btnPriceNoFilter = (Button) priceFilterView.findViewById(R.id.btn_search_assistcoauch_price_popupwindow_intro);
                    btnPriceNoFilter.setOnClickListener(new AssistCoauchPopupInternalHandler());
                    ListView priceList = (ListView) priceFilterView.findViewById(R.id.list_assistcoauch_price_filter_list);
                    priceList.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(priceArr)));

                    popupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnCost, priceFilterView);

                    priceList.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            final String priceStr = priceArr[position];
                            Message msg = sUIEventsHandler.obtainMessage(RETRIEVE_INFO_WITH_PRICE_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_PRICE_FILTERED, priceStr);
                            msg.setData(data);
                            sUIEventsHandler.sendMessage(msg);

                            popupWindow.dismiss();
                        }
                    });

                    break;
                case R.id.btn_assistcoauch_kinds:
                    final String[] kindsArr = {
                            sContext.getResources().getString(R.string.search_coauch_filter_kinds_sinuoke),
                            sContext.getResources().getString(R.string.search_coauch_filter_kinds_desk),
                            sContext.getResources().getString(R.string.search_coauch_filter_kinds_jiuqiu)
                    };
                    View kindsFilterView = inflater.inflate(R.layout.search_assistcoauch_clazz_popupwindow, null);
                    Button btnClazzNoFilter = (Button) kindsFilterView.findViewById(R.id.btn_search_assistcoauch_kinds_popupwindow_intro);
                    btnClazzNoFilter.setOnClickListener(new AssistCoauchPopupInternalHandler());
                    ListView clazzList = (ListView) kindsFilterView.findViewById(R.id.list_search_assistcoauch_clazz_filter_list);
                    clazzList.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(kindsArr)));

                    popupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnKinds, kindsFilterView);
                    clazzList.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            final String clazzStr = kindsArr[position];
                            Message msg = sUIEventsHandler.obtainMessage(RETREIVE_INFO_WITH_KINDS_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_KINDS_FILTERED, clazzStr);
                            msg.setData(data);
                            sUIEventsHandler.sendMessage(msg);
                            popupWindow.dismiss();
                        }
                    });

                    break;
                case R.id.btn_assistcoauch_level:

                    final String[] levelsArr = {
                            sContext.getResources().getString(R.string.level_base),
                            sContext.getResources().getString(R.string.level_middle),
                            sContext.getResources().getString(R.string.level_master),
                            sContext.getResources().getString(R.string.level_super_master)
                    };

                    View levelFilterView = inflater.inflate(R.layout.search_assistcoauch_subfragment_level_popupwindow, null);
                    Button btnLevelNoFilter = (Button) levelFilterView.findViewById(R.id.search_assistcoauch_level_popupwindow_intro);
                    btnLevelNoFilter.setOnClickListener(new AssistCoauchPopupInternalHandler());

                    ListView levelList = (ListView) levelFilterView.findViewById(R.id.list_search_assistcoauch_level_filter_list);
                    levelList.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(levelsArr)));

                    popupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnLevel, levelFilterView);

                    levelList.setOnItemClickListener( new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            final String levelStr = levelsArr[position];
                            Message msg = sUIEventsHandler.obtainMessage(RETRIEVE_INFO_WITH_LEVEL_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_LEVEL_FILTERED, levelStr);
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

    private final static class AssistCoauchPopupInternalHandler implements View.OnClickListener
    {

        @Override
        public void onClick(View v)
        {

        }
    }

    private static void retrieveAllInitialAssistCoauchInfo(final int startNo, final int endNo)
    {
        if (!sNetworkAvailable)
        {
            Message failMsg = sUIEventsHandler.obtainMessage(STATE_FETCH_DATA_FAILED);
            Bundle data = new Bundle();
            data.putString(KEY_DATA_FETCH_FAILED_INFO, sContext.getResources().getString(R.string.network_unavailable_hint_info_str));
            failMsg.setData(data);
            sUIEventsHandler.sendMessage(failMsg);

            return ;
        }

        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("start_no", startNo + "");
        requestParams.put("end_no", endNo + "");
        String rawResult = HttpUtil.urlClient(HttpConstants.SearchAssistCoauch.URL, requestParams, HttpConstants.RequestMethod.GET);

        Log.d(TAG, " the raw result we get for the AssistCoauch info are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult))
        {
            try
            {
                JSONObject initialResultJsonObj = new JSONObject(rawResult);
                Log.d(TAG, " the initial resulted json data are : " + initialResultJsonObj.toString());

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

                        SearchAssistCoauchSubFragmentBean assistCoauchBean = new SearchAssistCoauchSubFragmentBean(
                                userId,
                                photoUrl,
                                name,
                                SubFragmentsCommonUtils.parseGenderStr(sContext, sex),
                                SubFragmentsCommonUtils.parseBilliardsKinds(sContext, kinds),
                                money,
                                String.valueOf(range)
                        );
                        sAssistCoauchList.add(assistCoauchBean);

                        // TODO: 我们需要将我们在这里解析得到的完整的数据插入到数据库当中
                        // TODO: ????????????????? 将数据插入到SQLite当中

                        sUIEventsHandler.sendEmptyMessage(DATA_HAS_BEEN_UPDATED);
                    }

                    // TODO: 到这里，我们基本上就已经完成了数据检索的工作了，现在我们需要的就是通知用户已经完成数据检索工作，我们可以取消ProgressDialog的显示了
                    sUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                }
            } catch (JSONException e)
            {
                e.printStackTrace();
                Log.d(TAG, " exception happened in parsing the json data we get, and the detailed reason are : " + e.toString());
            }
        }
    }

    private static final int DATA_HAS_BEEN_UPDATED = 1 << 10;

    private static final String KEY_REQUEST_LEVEL_FILTERED = "keyRequestLevelFiltered";
    private static final String KEY_REQUEST_PRICE_FILTERED = "keyRequestPriceFiltered";
    private static final String KEY_REQUEST_DISTANCE_FILTERED = "keyRequestDistanceFiltered";
    private static final String KEY_REQUEST_KINDS_FILTERED = "keyRequestKindsFiltered";


    private static final String KEY_DATA_FETCH_FAILED_INFO = "keyDataFetchFailedInfo";

    private static final int RETRIEVE_ALL_RAW_INFO = 1 << 1;
    private static final int RETREIVE_INFO_WITH_KINDS_FILTERED = 1 << 2;
    private static final int RETRIEVE_INFO_WITH_LEVEL_FILTERED = 1 << 3;
    private static final int RETRIEVE_INFO_WITH_PRICE_FILTERED = 1 << 4;
    private static final int RETRIEVE_INFO_WITH_DISTANCE_FILTERED = 1 << 5;

    private static final int UI_SHOW_DIALOG = 1 << 6;
    private static final int UI_HIDE_DIALOG = 1 << 7;

    private static final int STATE_FETCH_DATA_SUCCESS = 1 << 8;
    private static final int STATE_FETCH_DATA_FAILED = 1 << 9;

    private static Handler sUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case UI_SHOW_DIALOG:
                    Log.d(TAG, " start showing the dialog ");
                    showProgress();
                    break;
                case UI_HIDE_DIALOG:
                    Log.d(TAG, " hiding the dialog ");
                    sAssistCoauchListAdapter.notifyDataSetChanged();

                    hideProgress();
                    break;

                case STATE_FETCH_DATA_FAILED:
                    Bundle failureReasonInfo = msg.getData();
                    String reasonStr = failureReasonInfo.getString(KEY_DATA_FETCH_FAILED_INFO);
                    Log.d(TAG, " fail to fetch the data, and the reason are : " + reasonStr);
                    break;

                case STATE_FETCH_DATA_SUCCESS:

                    break;

                case RETRIEVE_INFO_WITH_DISTANCE_FILTERED:
                    Bundle rangeData = msg.getData();
                    String rangeStr = rangeData.getString(KEY_REQUEST_DISTANCE_FILTERED);

                    sWorker.fetchDataWithRangeFilter(rangeStr);

                    break;

                case RETRIEVE_INFO_WITH_LEVEL_FILTERED:
                    Bundle levelData = msg.getData();
                    String levelStr = levelData.getString(KEY_REQUEST_LEVEL_FILTERED);
                    sWorker.fetchDataWithLevelFilter(levelStr);

                    break;

                case RETRIEVE_INFO_WITH_PRICE_FILTERED:
                    Bundle priceData = msg.getData();
                    String priceStr = priceData.getString(KEY_REQUEST_PRICE_FILTERED);

                    sWorker.fetchDataWithPriceFilter(priceStr);

                    break;
                case RETREIVE_INFO_WITH_KINDS_FILTERED:
                    Bundle clazzData = msg.getData();
                    String clazz = clazzData.getString(KEY_REQUEST_KINDS_FILTERED);

                    sWorker.fetchDataWithClazzFilter(clazz);
                    break;

                case DATA_HAS_BEEN_UPDATED:

                    sAssistCoauchListAdapter.notifyDataSetChanged();
                    Log.d(TAG, " the data set has been updated ");
                    break;
                default:
                    break;

            }
        }
    };

    private static void showProgress()
    {
        sPreProgress.setVisibility(View.VISIBLE);
        sPreTextView.setVisibility(View.VISIBLE);
    }

    private static void hideProgress()
    {
        sPreProgress.setVisibility(View.GONE);
        sPreTextView.setVisibility(View.GONE);
    }

    private static final String BACKGROUND_HANDLER_NAME = "BackgroundWorkerHandler";

    // 用于处理后台任务的处理器
    private static class BackgroundWorkerHandler extends HandlerThread
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
                            sUIEventsHandler.sendEmptyMessage(UI_SHOW_DIALOG);
                            retrieveAllInitialAssistCoauchInfo(0, 9);

                            break;
                        // TODO: 以下的四种操作虽然耗时，但是都是不涉及到网络的操作，以下四种检索都是直接从本地的我们创建的数据库当中进行检索
                        case RETRIEVE_INFO_WITH_LEVEL_FILTERED:
                            // TODO: 进行具体的本地数据库检索操作
                            Bundle levelData = msg.getData();
                            String level = levelData.getString(KEY_REQUEST_LEVEL_FILTERED);
                            Log.d(TAG, " Inside the WorkerThread --> the level data we need to filter are : " + level);

                            break;
                        case RETREIVE_INFO_WITH_KINDS_FILTERED:
                            // TODO: 进行具体的本地数据库检索操作，以用户的选择的球种做为筛选条件
                            // TODO: 在检索到相应的数据之后，还要对ListView进行相关的操作

                            Bundle clazzData = msg.getData();
                            String clazz = clazzData.getString(KEY_REQUEST_KINDS_FILTERED);
                            Log.d(TAG, " Inside the WorkerThread --> the clazz we need to filter are : " + clazz);

                            break;
                        case RETRIEVE_INFO_WITH_PRICE_FILTERED:
                            Bundle priceData = msg.getData();
                            String price = priceData.getString(KEY_REQUEST_PRICE_FILTERED);
                            Log.d(TAG, " Inside the WorkerThread --> the price we need to filter are : " + price);


                            break;
                        case RETRIEVE_INFO_WITH_DISTANCE_FILTERED:
                            Bundle distanceData = msg.getData();
                            String distance = distanceData.getString(KEY_REQUEST_DISTANCE_FILTERED);
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
            Message msg = mBackgroundHandler.obtainMessage(RETRIEVE_INFO_WITH_PRICE_FILTERED);
            Bundle data = new Bundle();
            data.putString(KEY_REQUEST_PRICE_FILTERED, price);
            msg.setData(data);

            mBackgroundHandler.sendMessage(msg);
        }

        public void fetchDataWithRangeFilter(String range)
        {
            Message msg = mBackgroundHandler.obtainMessage(RETRIEVE_INFO_WITH_DISTANCE_FILTERED);
            Bundle data = new Bundle();
            data.putString(KEY_REQUEST_DISTANCE_FILTERED, range);
            msg.setData(data);

            mBackgroundHandler.sendMessage(msg);
        }

        public void fetchDataWithLevelFilter(String level)
        {
            Message msg = mBackgroundHandler.obtainMessage(RETRIEVE_INFO_WITH_LEVEL_FILTERED);
            Bundle data = new Bundle();
            data.putString(KEY_REQUEST_LEVEL_FILTERED, level);
            msg.setData(data);

            mBackgroundHandler.sendMessage(msg);
        }

        public void fetchDataWithClazzFilter(String clazz)
        {
            Message msg = mBackgroundHandler.obtainMessage(RETREIVE_INFO_WITH_KINDS_FILTERED);
            Bundle data = new Bundle();
            data.putString(KEY_REQUEST_KINDS_FILTERED, clazz);
            msg.setData(data);

            mBackgroundHandler.sendMessage(msg);
        }
    }

    // TODO: 在测试接口的时候删除下面的方法
    //以下是用于初始化过程当中的测试数据
    private void initTestData()
    {
        int i;
        for (i = 0; i < 100; i++) {
            sAssistCoauchList.add(new SearchAssistCoauchSubFragmentBean("", "", "月夜刘莎", "女", "斯诺克", "38", "1000米"));
        }
    }


}































































































































































































