package com.yueqiu.fragment.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.yueqiu.R;
import com.yueqiu.adapter.SearchAssistCoauchSubFragmentListAdapter;
import com.yueqiu.bean.SearchAssistCoauchSubFragmentBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.fragment.search.common.SubFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by scguo on 14/12/30.
 * <p/>
 * 用于SearchActivity当中的助教子Fragment的实现
 */
@SuppressLint("ValidFragment")
public class BilliardsSearchAssistCoauchFragment extends Fragment
{
    private static final String TAG = "BilliardsSearchAssistCoauchFragment";

    public static final String BILLIARDS_SEARCH_ASSIST_COAUCH_FRAGMENT_TAB_NAME = "BilliardsSearchAssistCoauchFragment";

    // 用于展示助教信息的ListView
    private ListView mListView;
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


    private View mView;

    private static Button sBtnDistance, sBtnCost, sBtnKinds, sBtnLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.search_assistcoauch_fragment_layout, container, false);

        SubFragmentsCommonUtils.initViewPager(sContext, mView, R.id.assistcoauch_fragment_gallery_pager, R.id.assistcoauch_fragment_gallery_pager_indicator_group);

        (sBtnDistance = (Button) mView.findViewById(R.id.btn_assistcoauch_distance)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnCost = (Button) mView.findViewById(R.id.btn_assistcoauch_cost)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnKinds = (Button) mView.findViewById(R.id.btn_assistcoauch_kinds)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnLevel = (Button) mView.findViewById(R.id.btn_assistcoauch_level)).setOnClickListener(new OnFilterBtnClickListener());

        mListView = (ListView) mView.findViewById(R.id.search_assistcoauch_subfragment_listview);

        sHandler.sendEmptyMessage(RETRIEVE_ALL_RAW_INFO);
        // TODO: 以下加载的是测试数据
        initTestData();
        mListView.setAdapter(new SearchAssistCoauchSubFragmentListAdapter(sContext, (ArrayList<SearchAssistCoauchSubFragmentBean>) mAssistCoauchList));

        return mView;
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
                    String[] disStrList = {
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_500_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_1000_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_2000_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_5000_str)
                    };

                    View distanceFilterView = inflater.inflate(R.layout.search_mate_subfragment_distance_popupwindow, null);
                    Button btnDistanceNoFilter = (Button) distanceFilterView.findViewById(R.id.search_mate_popupwindow_intro);
                    btnDistanceNoFilter.setOnClickListener(new AssistCoauchPopupInternalHandler());
                    ListView distanList = (ListView) distanceFilterView.findViewById(R.id.list_search_mate_distance_filter_list);
                    distanList.setAdapter(new ArrayAdapter<String>(sContext, android.R.layout.simple_list_item_1, disStrList));

                    popupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnDistance, distanceFilterView);

                    break;
                case R.id.btn_assistcoauch_cost:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnCost, R.layout.search_mate_subfragment_gender_popupwindow);


                    break;
                case R.id.btn_assistcoauch_kinds:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnCost, R.layout.search_mate_subfragment_distance_popupwindow);
                    break;
                case R.id.btn_assistcoauch_level:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnCost, R.layout.search_mate_subfragment_distance_popupwindow);
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

    private static final int RETRIEVE_ALL_RAW_INFO = 1 << 1;
    private static final int RETREIVE_INFO_WITH_KINDS_FILTERED = 1 << 2;
    private static final int RETRIEVE_INFO_WITH_LEVEL_FILTERED = 1 << 3;
    private static final int RETRIEVE_INFO_WITH_PRICE_FILTERED = 1 << 4;
    private static final int RETRIEVE_INFO_WITH_DISTANCE_FILTERED = 1 << 5;

    // TODO: 以下这个方法将被不再使用，在准确确定之后，删掉这个方法，因为服务器端确定通过不传递任何参数来接受参数

    /**
     * @param userId
     * @param range    助教的距离
     * @param money    助教的费用，也就是price
     * @param classes  助教的球种，也就是kinds
     * @param level    助教的级别
     * @param startNum 请求信息的开始的条数
     * @param endNum   请求信息的结束条数
     */
    private void retrieveAssistCoauchRawInfo(final String userId, final String range, final int money, final int classes, final int level, final int startNum, final int endNum)
    {
        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("user_id", userId);
        requestParams.put("range", range);
        requestParams.put("money", money + "");
        requestParams.put("class", classes + "");
        requestParams.put("start_no", startNum + "");
        requestParams.put("end_no", endNum + "");

        String rawResult = HttpUtil.urlClient(HttpConstants.SearchAssistCoauch.URL, requestParams, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the rawResult we get are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult)) {

        }

    }

    private static void retrieveAllInitialAssistCoauchInfo()
    {
        String rawResult = HttpUtil.urlClient(HttpConstants.SearchAssistCoauch.URL, null, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the raw result we get for the AssistCoauch info are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult))
        {
            try
            {
                JSONObject initialResultJsonObj = new JSONObject(rawResult);
                Log.d(TAG, " the initial resulted json data are : " + initialResultJsonObj.toString());

                JSONArray resultArr = initialResultJsonObj.getJSONArray("list_data");
                final int count = resultArr.length();
                int i;
                for (i = 0; i < count; ++i)
                {
                    JSONObject dataUnit = resultArr.getJSONObject(i);

                    // TODO: 现在服务器端还没有提供完整的数据，现在的字段都不是完整的


                }


            } catch (JSONException e)
            {
                e.printStackTrace();
                Log.d(TAG, " exception happened in parsing the json data we get, and the detailed reason are : " + e.toString());
            }
        }


    }

    private static Handler sHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case RETRIEVE_ALL_RAW_INFO:
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            retrieveAllInitialAssistCoauchInfo();
                        }
                    }).start();
                    break;
                case RETRIEVE_INFO_WITH_DISTANCE_FILTERED:
                    break;
                case RETREIVE_INFO_WITH_KINDS_FILTERED:
                    break;
                case RETRIEVE_INFO_WITH_PRICE_FILTERED:
                    break;
                case RETRIEVE_INFO_WITH_LEVEL_FILTERED:

                    break;

                default:
                    break;

            }
        }
    };

    // TODO: 在测试接口的时候删除下面的方法
    //以下是用于初始化过程当中的测试数据
    private List<SearchAssistCoauchSubFragmentBean> mAssistCoauchList = new ArrayList<SearchAssistCoauchSubFragmentBean>();

    private void initTestData()
    {
        int i;
        for (i = 0; i < 100; i++) {
            mAssistCoauchList.add(new SearchAssistCoauchSubFragmentBean("", "月夜刘莎", "女", "斯诺克", "38", "1000米"));
        }
    }


}































































































































































































