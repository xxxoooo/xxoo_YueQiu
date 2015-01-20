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
import com.yueqiu.adapter.SearchCoauchSubFragmentListAdapter;
import com.yueqiu.adapter.SearchPopupBaseAdapter;
import com.yueqiu.bean.SearchCoauchSubFragmentCoauchBean;
import com.yueqiu.constant.HttpConstants;
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
 * Created by scguo on 14/12/30.
 * <p/>
 * 用于SearchActivity当中的教练子Fragment的实现
 */
@SuppressLint("ValidFragment")
public class BilliardsSearchCoauchFragment extends Fragment
{
    private static final String TAG = "BilliardsSearchCoauchFragment";

    private static final String FRAGMENT_TAG = "BilliardsSearchAssistCoauchFragment";

    // 这是用于整个BilliardsSearchCoauchFragment当中的layout view
    private View mView;
    private static Button sBtnAbility, sBtnKinds;

    private ListView mCoauchListView;

    private static Context sContext;

    @SuppressLint("ValidFragment")
    public BilliardsSearchCoauchFragment()
    {
    }

    private static final String PARAMS_KEY = "BilliardsSearchCoauchFragment";

    public static BilliardsSearchCoauchFragment newInstance(Context context, String params)
    {
        sContext = context;
        BilliardsSearchCoauchFragment instance = new BilliardsSearchCoauchFragment();

        Bundle args = new Bundle();
        args.putString(PARAMS_KEY, params);
        instance.setArguments(args);

        return instance;
    }

    private boolean mIsHead;
    private static boolean sNetworkAvailable;

    private static BackgroundWorkerThread sWorker;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mIsHead = false;
        sNetworkAvailable = Utils.networkAvaiable(sContext);
        sWorker = new BackgroundWorkerThread();

    }

    private String mArgs;

    private static ProgressBar sPreProgress;
    private static TextView sPreTextView;
    private static Drawable sProgressDrawable;

    private static List<SearchCoauchSubFragmentCoauchBean> sCoauchList = new ArrayList<SearchCoauchSubFragmentCoauchBean>();
    private static SearchCoauchSubFragmentListAdapter sCoauchListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.search_coauch_fragment_layout, container, false);

        SubFragmentsCommonUtils.initViewPager(sContext, mView, R.id.coauch_fragment_gallery_pager, R.id.coauch_fragment_gallery_pager_indicator_group);

        (sBtnAbility = (Button) mView.findViewById(R.id.btn_coauch_ability)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnKinds = (Button) mView.findViewById(R.id.btn_coauch_kinds)).setOnClickListener(new OnFilterBtnClickListener());

        mCoauchListView = (ListView) mView.findViewById(R.id.search_coauch_subfragment_list);

        sPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        sPreTextView = (TextView) mView.findViewById(R.id.pre_text);
        sProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = sPreProgress.getIndeterminateDrawable().getBounds();
        sPreProgress.setIndeterminateDrawable(sProgressDrawable);
        sPreProgress.getIndeterminateDrawable().setBounds(bounds);

        Bundle args = getArguments();
        mArgs = args.getString(PARAMS_KEY);

        // TODO: 这里加载的是测试数据,暂时还不能删除这个方法，因为我们还要查看总的UI加载效果
//        initListViewTestData();

        sCoauchListAdapter = new SearchCoauchSubFragmentListAdapter(sContext, (ArrayList<SearchCoauchSubFragmentCoauchBean>) sCoauchList);
        Log.d(TAG, " the source list content are : " + sCoauchList.size());
        mCoauchListView.setAdapter(sCoauchListAdapter);
        sCoauchListAdapter.notifyDataSetChanged();

        return mView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (sWorker != null && sWorker.getState() == Thread.State.NEW) {
            // TODO: 我们需要在sWorker当中的开始方法加一些判断，用以判断当前的网络情况，
            // TODO: 然后决定是从本地数据库，还是从网络当中进行数据的检索(这里，对于每一个Fragment当中的BackgroundHandlerThread的处理流程都是一样的)
            sWorker.start();
        }
    }

    @Override
    public void onDestroy()
    {
        sWorker.quit();

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
                case R.id.btn_coauch_ability:
                    View levelPopupView = inflater.inflate(R.layout.search_coauch_subfragment_level_popupwindow, null);

                    final String[] levelStrList = {
                            sContext.getResources().getString(R.string.search_coauch_filter_level_guojiadui),
                            sContext.getResources().getString(R.string.search_coauch_filter_level_in_guojiadui),
                            sContext.getResources().getString(R.string.search_coauch_filter_level_pre_guojiadui),
                    };

                    Button btnLevelNoFilter = (Button) levelPopupView.findViewById(R.id.btn_search_coauch_level_popup_no_filter);
                    btnLevelNoFilter.setOnClickListener(new CoauchFilterPopupInternalHandler());
                    ListView levelList = (ListView) levelPopupView.findViewById(R.id.list_search_coauch_level_filter_list);
                    levelList.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(levelStrList)));
                    popupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnAbility, levelPopupView);

                    levelList.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            final String levelStr = levelStrList[position];
                            Message msg = sUIEventsHandler.obtainMessage(RETRIEVE_COAUCH_WITH_LEVEL_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_LEVEL_FILTER, levelStr);
                            msg.setData(data);
                            sUIEventsHandler.sendMessage(msg);

                            popupWindow.dismiss();
                        }
                    });

                    break;
                case R.id.btn_coauch_kinds:
                    final String[] kindsStrList = {
                            sContext.getResources().getString(R.string.search_coauch_filter_kinds_desk),
                            sContext.getResources().getString(R.string.search_coauch_filter_kinds_jiuqiu),
                            sContext.getResources().getString(R.string.search_coauch_filter_kinds_sinuoke)
                    };

                    View kindsPopupView = inflater.inflate(R.layout.search_coauch_subfragment_kinds_popupwindow, null);

                    Button btnKindsNoFilter = (Button) kindsPopupView.findViewById(R.id.btn_search_coauch_kinds_popup_no_filter);
                    btnKindsNoFilter.setOnClickListener(new CoauchFilterPopupInternalHandler());
                    ListView kindsList = (ListView) kindsPopupView.findViewById(R.id.list_search_coauch_kinds_filter_list);
                    kindsList.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(kindsStrList)));
                    popupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnKinds, kindsPopupView);

                    kindsList.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            final String clazzStr = kindsStrList[position];
                            Message msg = sUIEventsHandler.obtainMessage(RETRIEVE_COAUCH_WITH_CLASS_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_CLAZZ_FILTER, clazzStr);
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

    private final static class CoauchFilterPopupInternalHandler implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId()) {
                case R.id.btn_search_coauch_level_popup_no_filter:
                    Toast.makeText(sContext, "do not filter the level", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_search_coauch_kinds_popup_no_filter:
                    Toast.makeText(sContext, "do not filter the kinds", Toast.LENGTH_SHORT).show();

            }
        }
    }


    private static void retrieveInitialCoauchInfo(final int startNo, final int endNo)
    {
        if (!sNetworkAvailable) {
            Message failInfo = sUIEventsHandler.obtainMessage(STATE_FETCH_DATA_FAILED);
            Bundle failData = new Bundle();
            failData.putString(KEY_DATA_FAILED_INFO, sContext.getResources().getString(R.string.network_unavailable_hint_info_str));
            failInfo.setData(failData);
            sUIEventsHandler.sendMessage(failInfo);

            return;
        }
        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("start_no", startNo + "");
        requestParams.put("end_no", endNo + "");

        String rawResult = HttpUtil.urlClient(HttpConstants.SearchCoauch.URL, requestParams, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the raw result we get for the Coauch are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult)) {
            try {
                JSONObject initialResultJson = new JSONObject(rawResult);
                final int status = initialResultJson.getInt("code");
                if (status == HttpConstants.ResponseCode.NORMAL) {
                    JSONObject resultJsonObj = initialResultJson.getJSONObject("result");
                    Log.d(TAG, " the final json data we need to parse are : " + resultJsonObj);
                    JSONArray dataArr = resultJsonObj.getJSONArray("list_data");
                    final int len = dataArr.length();
                    int i;
                    for (i = 0; i < len; ++i) {
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

                        SearchCoauchSubFragmentCoauchBean coauchBean = new SearchCoauchSubFragmentCoauchBean(
                                userId,
                                photoUrl,
                                userName,
                                SubFragmentsCommonUtils.parseGenderStr(sContext, sex),
                                String.valueOf(range),
                                SubFragmentsCommonUtils.parseCoauchLevel(sContext, level),
                                SubFragmentsCommonUtils.parseBilliardsKinds(sContext, kinds));
                        sCoauchList.add(coauchBean);
                        // TODO: 我们应该在这里把所有的我们解析到的数据插入到数据库当中
                        // TODO: 当时这里有一个很重要的一点就是我们将数据插入数据库时，千万不要一条一条的插！！！
                        // TODO: 因为这样很浪费资源，数据库的打开和关闭是一个很大的开销，我们需要借用
                        // TODO: 数据库的transaction业务进行操作，即批量的插入数据和检索数据，这样可以加快我们的插入过程
                        // TODO: 这是十分需要注意的！！！


                        // TODO: 我们暂时还不确定，是否是需要每增加一条数据，就要通过Adapter我们进行了数据源的更新，
                        // TODO: 还是在我们已经获取完所有的数据之后在进行这个操作？？？？
                        sUIEventsHandler.sendEmptyMessage(DATA_HAS_BEEN_UPDATED);
                    }

                    // TODO: 这时，数据已经完全检索完毕，我们可以取消dialog的显示了
                    sUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);

                }


            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, " exception happened while we parsing the json object we retrieved, and the reason are : " + e.toString());
            }
        }
    }

    private static final int DATA_HAS_BEEN_UPDATED = 1 << 8;

    private static final String KEY_REQUEST_LEVEL_FILTER = "keyRequestLevelFiltered";
    private static final String KEY_REQUEST_CLAZZ_FILTER = "keyRequestClazzFiltered";

    private static final String KEY_DATA_FAILED_INFO = "keyDataFailedInfo";

    private static final String BACKGROUND_WORKER_NAME = "BackgroundWorkerThread";

    private static final int STATE_FETCH_DATA_FAILED = 1 << 4;
    private static final int STATE_FETCH_DATA_SUCCESS = 1 << 5;

    private static final int RETRIEVE_ALL_COAUCH_INFO = 1 << 1;

    private static final int RETRIEVE_COAUCH_WITH_LEVEL_FILTERED = 1 << 2;
    private static final int RETRIEVE_COAUCH_WITH_CLASS_FILTERED = 1 << 3;

    private static final int UI_SHOW_PROGRESS = 1 << 6;
    private static final int UI_HIDE_PROGRESS = 1 << 7;

    private static Handler sUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case UI_SHOW_PROGRESS:
                    showProgress();
                    Log.d(TAG, " start showing the progress bar in the Coauch fragment ");
                    break;
                case UI_HIDE_PROGRESS:

                    sCoauchListAdapter.notifyDataSetChanged();
                    hideProgress();
                    Log.d(TAG, " hide the progress bar that in the coauch fragment ");
                    break;

                case STATE_FETCH_DATA_FAILED:
                    Bundle faileData = msg.getData();
                    String reasonDesc = faileData.getString(KEY_DATA_FAILED_INFO);
                    Log.d(TAG, " fail to get the data we need, and the detailed reason for that are : " + reasonDesc);
                    Toast.makeText(sContext, reasonDesc, Toast.LENGTH_SHORT).show();
                    break;
                case STATE_FETCH_DATA_SUCCESS:
                    break;

                case RETRIEVE_COAUCH_WITH_LEVEL_FILTERED:
                    Bundle levelData = msg.getData();
                    String level = levelData.getString(KEY_REQUEST_LEVEL_FILTER);

                    sWorker.fetchDataWithLevelFiltered(level);
                    break;

                case RETRIEVE_COAUCH_WITH_CLASS_FILTERED:
                    Bundle clazzData = msg.getData();
                    String clazz = clazzData.getString(KEY_REQUEST_CLAZZ_FILTER);

                    sWorker.fetchDataWithClazzFiltered(clazz);

                    break;

                case DATA_HAS_BEEN_UPDATED:

                    sCoauchListAdapter.notifyDataSetChanged();
                    Log.d(TAG, " the adapter has been updated ");
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

    private static class BackgroundWorkerThread extends HandlerThread
    {
        public BackgroundWorkerThread()
        {
            super(BACKGROUND_WORKER_NAME, Process.THREAD_PRIORITY_BACKGROUND);
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
                        case RETRIEVE_ALL_COAUCH_INFO:
                            // 通知UIHandler开始显示Dialog
                            sUIEventsHandler.sendEmptyMessage(UI_SHOW_PROGRESS);
                            retrieveInitialCoauchInfo(0, 9);

                            break;
                        case RETRIEVE_COAUCH_WITH_CLASS_FILTERED:
                            Bundle clazzData = msg.getData();
                            String clazz = clazzData.getString(KEY_REQUEST_CLAZZ_FILTER);
                            sUIEventsHandler.sendEmptyMessage(UI_SHOW_PROGRESS);
                            Log.d(TAG, " inside the BackgroundThread --> the clazz string we get in the CouachFragment are : " + clazz);
                            // TODO: 进行真正的本地检索过程


                            sUIEventsHandler.sendEmptyMessage(UI_HIDE_PROGRESS);
                            break;
                        case RETRIEVE_COAUCH_WITH_LEVEL_FILTERED:
                            Bundle levelData = msg.getData();
                            String level = levelData.getString(KEY_REQUEST_LEVEL_FILTER);

                            Log.d(TAG, " inside the BackgroundThread --> the level string we get in the CoauchFragment are : " + level);

                            break;
                    }
                }
            };
            fetchAllData();
        }

        public void fetchAllData()
        {
            mWorkerHandler.sendEmptyMessage(RETRIEVE_ALL_COAUCH_INFO);
        }

        public void fetchDataWithClazzFiltered(String clazz)
        {
            Message msg = mWorkerHandler.obtainMessage(RETRIEVE_COAUCH_WITH_CLASS_FILTERED);
            Bundle data = new Bundle();
            data.putString(KEY_REQUEST_CLAZZ_FILTER, clazz);
            msg.setData(data);
            mWorkerHandler.sendMessage(msg);
        }

        public void fetchDataWithLevelFiltered(String level)
        {
            Message msg = mWorkerHandler.obtainMessage(RETRIEVE_COAUCH_WITH_LEVEL_FILTERED);
            Bundle data = new Bundle();
            data.putString(KEY_REQUEST_LEVEL_FILTER, level);
            msg.setData(data);
            mWorkerHandler.sendMessage(msg);
        }

    }


    // TODO: 以下是测试数据,在测试接口的时候，将以下的初始化过程删除
    private void initListViewTestData()
    {
        int i;
        for (i = 0; i < 200; ++i) {
            sCoauchList.add(new SearchCoauchSubFragmentCoauchBean("", "", "大力水手", "男", "2000米以内", "前国家队队员", "九球"));
        }
    }
}






































































































































