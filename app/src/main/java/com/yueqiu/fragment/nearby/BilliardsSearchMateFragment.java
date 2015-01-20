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
import com.yueqiu.adapter.SearchMateSubFragmentListAdapter;
import com.yueqiu.adapter.SearchPopupBaseAdapter;
import com.yueqiu.bean.SearchMateSubFragmentUserBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.dao.daoimpl.SearchMateDaoImpl;
import com.yueqiu.fragment.nearby.common.SubFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

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

    private ListView mSubFragmentList;

    private static Button sBtnDistanceFilter, sBtnGenderFilter;

    @SuppressLint("ValidFragment")
    public BilliardsSearchMateFragment()
    {
    }

    private static final String KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT = "keyBilliardsSearchParentFragment";

    public static BilliardsSearchMateFragment newInstance(Context context, String params)
    {
        sContext = context;
        BilliardsSearchMateFragment fragment = new BilliardsSearchMateFragment();

        Bundle args = new Bundle();
        args.putString(KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT, params);

        return fragment;
    }

    private static SearchMateDaoImpl sMateDaoIns;

    // mIsHead用于控制数据的加载到List当中的方向(即加载到头部还是加载到尾部)
    private boolean mIsHead;
    private boolean mIsNetworkAvailable;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        sWorker = new BackgroundWorker();

        sMateDaoIns = new SearchMateDaoImpl(sContext);

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

        mSubFragmentList = (ListView) mView.findViewById(R.id.search_sub_fragment_list);

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

        sMateListAdapter = new SearchMateSubFragmentListAdapter(sContext, (ArrayList<SearchMateSubFragmentUserBean>) sUserList);
        mSubFragmentList.setAdapter(sMateListAdapter);
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

        if (sWorker != null && sWorker.getState() == Thread.State.NEW)
        {
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
                            sContext.getResources().getString(R.string.woman),
                            sContext.getResources().getString(R.string.man)
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
                            final String gender = genderStrList[position];
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
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_500_str),
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
                            final String distance = disStrList[position];
                            Message msg = sUIEventsHandler.obtainMessage(START_RETRIEVE_DATA_WITH_RANGE_FILTER);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_RANGE_FILTERED, distance);
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
     * 这里不需要任何请求参数
     */
    private static void retrieveInitialMateInfoList(final int startNo, final int endNo)
    {
        if (! Utils.networkAvaiable(sContext))
        {
            Toast.makeText(sContext, sContext.getResources().getString(R.string.network_unavailable_hint_info_str), Toast.LENGTH_LONG).show();
            sUIEventsHandler.sendEmptyMessage(DATA_RETRIEVE_FAILED);
            sUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
            return;
        }

        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("start_no", startNo + "");
        requestParams.put("end_no", endNo + "");

        String rawResult = HttpUtil.urlClient(HttpConstants.SearchMate.URL, requestParams, HttpConstants.RequestMethod.GET);

        Log.d(TAG, " the raw result we get for the mate fragment are : " + rawResult);
        if (! TextUtils.isEmpty(rawResult))
        {
            try
            {
                // initialObj当中包含的是最原始的JSON data，这个Json对象当中还包含了一些包含我们的请求状态的字段值
                // 我们还需要从initialObj当中解析出我们真正需要的Json对象
                JSONObject initialObj = new JSONObject(rawResult);
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

                        // TODO: 这里有一个不确定的地方，就是我们在向表当中插入数据时，是一次性插入，还是分批插入
                        // TODO: 还是就是我们获取数据的时候的先后顺序，是先将数据保存到本地，还是直接将从网络上面获取到的数据直接插入？？？？
                        // TODO: 我们在这里将解析成功的Json data存储到我们创建的MateTable当中
                        // TODO: 在这里jsonData同MateTable之间的交互过程是通过SearchMateDaoImpl来完成的
                        // TODO: MateDaoImpl当中直接接受所插入的数据就是SearchMateBean对象
                        sMateDaoIns.insertMateItem(mateUserBean);
                        sUserList.add(mateUserBean);


                        sUIEventsHandler.sendEmptyMessage(DATA_HAS_BEEN_UPDATED);
                    }
                    // TODO: 数据获取完之后，我们需要停止显示ProgressBar(这部分功能还需要进一步测试)
                    sUIEventsHandler.sendEmptyMessage(DATA_RETRIEVE_SUCCESS);
                    sUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, " exception happened in parsing the json data we get, and the detailed reason are : " + e.toString());
            }
        }
    }

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
                    break;
                case DATA_RETRIEVE_SUCCESS:

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
            }
        }
    };

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
                            retrieveInitialMateInfoList(0, 9);

                            break;
                        case START_RETRIEVE_DATA_WITH_RANGE_FILTER:
                            Bundle rangeData = msg.getData();
                            String range = rangeData.getString(KEY_REQUEST_RANGE_FILTERED);
                            Log.d(TAG, " inside the workThread --> start filtering the mate list based on the range of the current user " + range);

                            sUIEventsHandler.sendEmptyMessage(SHOW_PROGRESSBAR);

                            // TODO: 在这里进行本地的数据库检索操作，将检索到的数据插入到我们的sList当中，然后还需要更新一下
                            // TODO: list当中的Adapter(一定要注意更新Adapter的操作是只能在MainUIThread当中进行的，
                            // TODO: 也就是说我们需要经更新Adapter的操作发送给sUIEventsHandler 才可以)

                            sUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
                            break;
                        case START_RETRIEVE_DATA_WITH_GENDER_FILTER:
                            Bundle genderData = msg.getData();
                            String gender = genderData.getString(KEY_REQUEST_GENDER_FILTERED);
                            Log.d(TAG, " inside the wokkThread --> start filtering the mate list based on the gender of the current user " + gender);
                            sUIEventsHandler.sendEmptyMessage(SHOW_PROGRESSBAR);


                            sUIEventsHandler.sendEmptyMessage(HIDE_PROGRESSBAR);
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

        public void exit()
        {
            mBackgroundHandler.getLooper().quit();
        }
    }

    @Override
    public void onDestroy()
    {
        if (null != sWorker)
        {
            sWorker.exit();
        }


        super.onDestroy();
    }


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


















