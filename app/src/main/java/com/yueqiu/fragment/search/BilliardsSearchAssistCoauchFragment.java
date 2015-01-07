package com.yueqiu.fragment.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.yueqiu.R;
import com.yueqiu.adapter.SearchAssistCoauchSubFragmentListAdapter;
import com.yueqiu.bean.SearchAssistCoauchSubFragmentBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.fragment.search.common.SubFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;

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
        initTestData();
        mListView.setAdapter(new SearchAssistCoauchSubFragmentListAdapter(sContext, (ArrayList<SearchAssistCoauchSubFragmentBean>) mAssistCoauchList));

        return mView;
    }


    private static class OnFilterBtnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId()) {
                case R.id.btn_assistcoauch_distance:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnDistance, R.layout.search_mate_subfragment_distance_popupwindow);
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


    // TODO: 用于获取助教信息列表的方法

    /**
     *
     * @param userId
     * @param range 助教的距离
     * @param money 助教的费用，也就是price
     * @param classes 助教的球种，也就是kinds
     * @param level 助教的级别
     * @param startNum 请求信息的开始的条数
     * @param endNum 请求信息的结束条数
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
        if (!TextUtils.isEmpty(rawResult))
        {

        }

    }


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































































































































































































