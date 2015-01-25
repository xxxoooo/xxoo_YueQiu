package com.yueqiu.fragment.nearby;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
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
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.SearchMateDao;
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

    private static PullToRefreshListView sSubFragmentListView;

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

    private static SearchMateDao sMateDao;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        sWorker = new BackgroundWorker();

        sMateDao = DaoFactory.getSearchMateDao(sContext);
    }

    private static ProgressBar sPreProgress;
    private static TextView sPreTextView;
    private static Drawable sProgressDrawable;
    private static List<SearchMateSubFragmentUserBean> sUserList = new ArrayList<SearchMateSubFragmentUserBean>();

    // 以下的三个List都是与我们创建的mateTable相关的
    private static List<SearchMateSubFragmentUserBean> sInsertList = new ArrayList<SearchMateSubFragmentUserBean>();
    private static List<SearchMateSubFragmentUserBean> sUpdateList = new ArrayList<SearchMateSubFragmentUserBean>();
    private static List<SearchMateSubFragmentUserBean> sDBList = new ArrayList<SearchMateSubFragmentUserBean>();

    private static SearchMateSubFragmentListAdapter sMateListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.search_mate_fragment_layout, container, false);
        // then, inflate the image view pager
        SubFragmentsCommonUtils.initViewPager(sContext, mView, R.id.mate_fragment_gallery_pager, R.id.mate_fragment_gallery_pager_indicator_group);

        sSubFragmentListView = (PullToRefreshListView) mView.findViewById(R.id.search_sub_fragment_list);

        sSubFragmentListView.setMode(PullToRefreshBase.Mode.BOTH);
        sSubFragmentListView.setOnRefreshListener(mOnRefreshListener);

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
        initListViewDataSrc();

        // 加载数据库当中的全部数据
        // TODO: 我们在这里暂时先采用一个不是太好的实现策略，即直接采用粗糙的 new Thread()的方式
        // TODO: 这样创建线程不太好管理，所以我们应该优化一下这里，将这里创建的thread移到Handler当中进行统一管理
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                sDBList = sMateDao.getMateList(sStartNum, sEndNum + 1);
                if (! sDBList.isEmpty())
                {
                    sUIEventsHandler.obtainMessage(USE_CACHE, sDBList).sendToTarget();
                }
            }
        }).start();

        // TODO: 暂时定于在这里进行Adapter的更新操作
        Log.d(TAG, " onCreateView --> the adapter is notified here ");

        sMateListAdapter = new SearchMateSubFragmentListAdapter(sContext, (ArrayList<SearchMateSubFragmentUserBean>) sUserList);
        sSubFragmentListView.setAdapter(sMateListAdapter);
        sMateListAdapter.notifyDataSetChanged();

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
        if (sSubFragmentListView.isRefreshing())
            sSubFragmentListView.onRefreshComplete();
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

    // TODO: 但是对于PopupWindow当中的button的点击是否进行处理，暂时还没有确定是否要对里面的点击事件
    // TODO: 进行响应
    /**
     * 用于处理当popupWindow当中的Button(也就是显示(智能筛选)的button的点击时间)
     */
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
        List<SearchMateSubFragmentUserBean> resultCacheList = new ArrayList<SearchMateSubFragmentUserBean>();

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

                            resultCacheList.add(mateUserBean);
                            // TODO: fuck goes here
//                            sUserList.add(mateUserBean);
                        }
                        // TODO: 数据获取完之后，我们需要停止显示ProgressBar(这部分功能还需要进一步测试)
                        sUIEventsHandler.obtainMessage(DATA_RETRIEVE_SUCCESS, resultCacheList).sendToTarget();
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


            } catch (JSONException e)
            {
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

    private static final int DATA_RETRIEVE_SUCCESS = 1 << 2;
    private static final int DATA_RETRIEVE_FAILED = 1 << 3;

    private static final int START_RETRIEVE_DATA_WITH_RANGE_FILTER = 1 << 4;
    private static final int START_RETRIEVE_DATA_WITH_GENDER_FILTER = 1 << 5;

    // 同UI相关的事件的两个消息
    private static final int SHOW_PROGRESSBAR = 1 << 6;
    private static final int HIDE_PROGRESSBAR = 1 << 7;

    private static final int UPDATE_LOCAL_MATE_TABLE = 1 << 9;

    // 主要对应于当前用户进行数据请求时，但是网络不可行的情况
    private static final int NO_NETWORK = 1 << 10;

    // 我们采用本地数据库当中存取的静态数据，而不是从Server端获取到的数据
    private static final int USE_CACHE = 1 << 11;

    // 这个Handler主要是用于处理UI相关的事件,例如涉及到UI的事件的直接处理，例如Toast或者ProgressBar的显示控制
    private static Handler sUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (sSubFragmentListView.isRefreshing())
            {
                sSubFragmentListView.onRefreshComplete();
            }

            switch (msg.what)
            {
                case DATA_RETRIEVE_FAILED:
                    Toast.makeText(sContext, sContext.getResources().getString(R.string.network_unavailable_hint_info_str), Toast.LENGTH_SHORT).show();
                    hideProgress();
                    break;
                case DATA_RETRIEVE_SUCCESS:
                    Log.d(TAG, " sUIEventsHandler --> have received the data from the network ");
                    // 我们会将我们从网络上以及从本地数据库当中检索到的数据
                    // 都会通过消息通知的形式发送到这里，因为这样就可以保证我们所有的涉及到UI工作都是在UI线程当中完成的
                    // 使用sBeforeCount来保存还没有更新过的List的size值
                    sBeforeCount = sUserList.size();
                    List<SearchMateSubFragmentUserBean> mateList = (ArrayList<SearchMateSubFragmentUserBean>) msg.obj;
                    for (SearchMateSubFragmentUserBean userBean : mateList)
                    {
                        // TODO: 这里存在的问题就是，如果server端已经将数据删除了，但是Local DB当中还有
                        // TODO: 这个的数据的备份, 我们需要更改一些逻辑
                        if (! sUserList.contains(userBean))
                        {
                            sUserList.add(userBean);
                        }

                        // 当数据库中都不包含这条数据时，我们才将这条数据插入到数据库当中
                        // 如果本地数据库当中已经包含了这个数据，那么我们就直接更新这条已有的数据就可以了
                        // 我们并不是直接在这里(UI线程)直接进行数据库的数据插入和更新操作
                        // 我们只是将我们得到的需要插入和更新的数据放入到list当中，然后发送到sWorker当中进行具体的更新过程
                        if (! sDBList.isEmpty())
                        {
                            if (! sDBList.contains(userBean))
                            {
                                sInsertList.add(userBean);
                            } else
                            {
                                sUpdateList.add(userBean);
                            }
                        }
                    }

                    // 保存更新完的List的size
                    sAfterCount = sUserList.size();
                    Log.d(TAG, " sUiEventHandler --> the user list size are : " + sAfterCount);

                    if (sUserList.isEmpty())
                    {
                        loadEmptyTv();
                    } else
                    {
                        // 如果触发DATA_RETRIEVE_SUCCESS的事件是来自用户的下拉刷新
                        // 事件，那么我们需要根据我们得到更新后的List来判断数据的加载是否是成功的(上拉刷新是不需要判断的，
                        // TODO: 上拉刷新理论上所有的数据都应该保存到本地的数据库当中,如果没有保存的话，那么就是我们程序的问题了)
                        if (sRefresh)
                        {
                            if (sAfterCount == sBeforeCount)
                            {
                                Utils.showToast(sContext, sContext.getString(R.string.no_newer_info));
                            } else
                            {
                                Utils.showToast(sContext, sContext.getString(R.string.have_already_update_info, sAfterCount - sBeforeCount));
                            }
                        }
                    }

                    // 更新一下本地数据库
                    sWorker.updateMateTable();
                    sMateListAdapter.notifyDataSetChanged();
                    Log.d(TAG, " sUiEventHandler --> have notified the dataSet change event for the adapter ");
                    sUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
                    break;
                case NO_NETWORK:
                    Utils.showToast(sContext, sContext.getString(R.string.network_not_available));

                    if (sUserList.isEmpty())
                    {
                        loadEmptyTv();
                    }
                    break;

                case USE_CACHE:
                    List<SearchMateSubFragmentUserBean> localMateList = (ArrayList<SearchMateSubFragmentUserBean>) msg.obj;
                    sUserList.addAll(localMateList);

                    // TODO: 注意我们在这里进行更新Adapter的操作，注意测试是否会引发异常
                    sMateListAdapter.notifyDataSetChanged();

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


        }
    };

    private static void loadEmptyTv()
    {
        SubFragmentsCommonUtils.setFragmentEmptyTextView(sContext, sSubFragmentListView, sContext.getString(R.string.search_activity_subfragment_empty_tv_str));
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

        if (sSubFragmentListView.isRefreshing())
            sSubFragmentListView.onRefreshComplete();
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
            Log.d("wy", "Thread name->" + Thread.currentThread().getName());
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
                            if (! sUpdateList.isEmpty())
                            {
                                sMateDao.updateMateInfoBatch(sUpdateList);
                            }

                            if (! sInsertList.isEmpty())
                            {
                                long insertResult = sMateDao.insertMateItemBatch(sInsertList);
                                if (insertResult == -1)
                                {
                                    // TODO: 我们应该在这里添加插入数据的异常处理机制
                                    // TODO: 我们这里的暂时的处理机制就是简单不能插入就更新
                                    sMateDao.updateMateInfoBatch(sInsertList);
                                }
                            }

                            break;

                    }
                }
            };
            fetchAllData();
        }

        public void fetchAllData()
        {
            Log.d(TAG, " sWorker : fetchAllData() --> Work has started ");
            if (Utils.networkAvaiable(sContext))
            {
                sLoadMore = false;
                sRefresh = false;
                mBackgroundHandler.sendEmptyMessage(START_RETRIEVE_ALL_DATA);
            } else
            {
                sUIEventsHandler.sendEmptyMessage(NO_NETWORK);
            }
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

        public void updateMateTable()
        {
            mBackgroundHandler.sendEmptyMessage(UPDATE_LOCAL_MATE_TABLE);
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

    // 我们在整个类初始化时，就已经将sStartNum和sEndNum初始化为0和9
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

            sRefresh = true;
            sLoadMore = false;
            sInsertList.clear();
            sUpdateList.clear();
            if (Utils.networkAvaiable(sContext))
            {
                // TODO: 直接在这里进行网络请求，由于是在请求最新的数据，所以我们直接将我们
                // TODO: 的数据的startNum置为0，请求的是0~9条数据就是符合下拉刷新的逻辑过程的
                // TODO: 但是我们在这里请求时，也是要考虑到用户的筛选参数的请求


            } else
            {
                // 下拉刷新的过程代表用户请求的是最新的数据，如果没有网络的话，那就是确定请求不到了，本地数据库
                // 保存都是我们请求过的历史数据,所以在这里，我们直接告诉用户没有网络就可以了
                sUIEventsHandler.sendEmptyMessage(NO_NETWORK);
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            Log.d(TAG, " the user has touched the end of the current list in the BilliardsSearchMateFragment ");
            String label = SubFragmentsCommonUtils.getLastedTime(sContext);
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            sLoadMore = true;
            sRefresh = false;
            sCurrentPos = sUserList.size();
            sInsertList.clear();
            sUpdateList.clear();

            if (sBeforeCount != sAfterCount)
            {
                sStartNum = sEndNum + (sAfterCount - sBeforeCount);
                sEndNum += 10 + (sAfterCount - sBeforeCount);
            } else
            {
                sStartNum = sEndNum + 1;
                sEndNum += 10;
            }

            if (Utils.networkAvaiable(sContext))
            {
                // 网络可行的情况
                // TODO: 我们应该在这里首先判断，用户是都已经添加了筛选参数，
                // TODO: 如果用户已经添加了筛选参数，则我们需要在筛选参数的基础之上进行网络请求的工作
                // TODO: 用户的请求应该是累加的，即用户真正希望的效果是用户在选择了"500米以内"之后
                // TODO: 再选择"男"是可以选出“500米以内的男性球友”,而不是每次只能选出一个
                // TODO: ??????????Implemented??????????????

            } else
            {
                // 网络暂时不可行，我们需要从本地的数据库进行数据的请求工作
                // 在进行本地请求时，我们要注意不能阻塞当前的主线程(上拉刷新的过程是运行于主线程当中的)
                List<SearchMateSubFragmentUserBean> localRetrievedList = sMateDao.getMateList(sStartNum, 10);
                if (! localRetrievedList.isEmpty())
                {
                    Log.d(TAG, " onPullUpToRefresh --> we have send the list that we retrieved from the local list to the sUIEvensHandler, and" +
                            "the list size are : " + localRetrievedList.size());

                    sUIEventsHandler.obtainMessage(DATA_RETRIEVE_SUCCESS, localRetrievedList).sendToTarget();
                } else
                {
                    // 我们没有检索到任何数据，用户还没有插入任何数据，我们现在，仅仅需要告诉用户没有检索到数据就可以了
                    sUIEventsHandler.sendEmptyMessage(DATA_RETRIEVE_FAILED);
                }
            }
        }
    };

    // TODO: the following are just for testing
    // TODO: and remove all of them out with the true data we retrieved from RESTful WebService
    private void initListViewDataSrc()
    {
        int i;
        for (i = 0; i < 2; ++i) {
            sUserList.add(new SearchMateSubFragmentUserBean("", "", "月夜流沙", "男", "昌平区", "20000米以内"));
        }
    }
}


















