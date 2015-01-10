package com.yueqiu.fragment.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.activity.SearchBilliardsDatingActivity;
import com.yueqiu.adapter.SearchDatingSubFragmentListAdapter;
import com.yueqiu.bean.SearchDatingSubFragmentDatingBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.fragment.search.common.SubFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by scguo on 14/12/30.
 * <p/>
 * 用于SearchActivity当中的约球子Fragment的实现
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


    public static final String KEY_DATING_FRAGMENT = "BilliardsSearchDatingFragment";

    private View mView;
    private static Button sBtnDistan, sBtnPublishDate;
    private ListView mDatingListView;
    private List<SearchDatingSubFragmentDatingBean> mDatingList = new ArrayList<SearchDatingSubFragmentDatingBean>();

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

        mDatingListView = (ListView) mView.findViewById(R.id.search_dating_subfragment_list);

        sHandler.sendEmptyMessage(START_RETRIEVE_ALL_DATA);

        sHandler.sendEmptyMessage(START_RETRIEVE_ALL_DATA);

        // TODO: 以下加载的是测试数据，我们以后需要移除
        initTestData();
        mDatingListView.setAdapter(new SearchDatingSubFragmentListAdapter(sContext, (ArrayList<SearchDatingSubFragmentDatingBean>) mDatingList));

        mDatingListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                SearchDatingSubFragmentDatingBean bean = mDatingList.get(position);
                Bundle args = new Bundle();
                args.putString(SubFragmentsCommonUtils.KEY_DATING_FRAGMENT_PHOTO, bean.getUserPhoto());


                Intent intent = new Intent(sContext, SearchBilliardsDatingActivity.class);
                intent.putExtra(SubFragmentsCommonUtils.KEY_BUNDLE_SEARCH_DATING_FRAGMENT, args);
                sContext.startActivity(intent);
            }
        });

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
                case R.id.btn_dating_distance:
                    String[] disStrList = {
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_500_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_1000_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_2000_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_5000_str)
                    };

                    View distanPopupView = inflater.inflate(R.layout.search_mate_subfragment_distance_popupwindow, null);

                    Button btnDistanceNoFilter = (Button) distanPopupView.findViewById(R.id.search_mate_popupwindow_intro);
                    btnDistanceNoFilter.setOnClickListener(new DatingPopupWindowInternalClickHandler());
                    ListView distanList = (ListView) distanPopupView.findViewById(R.id.list_search_mate_distance_filter_list);
                    distanList.setAdapter(new ArrayAdapter<String>(sContext, android.R.layout.simple_list_item_1, disStrList));

                    SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnDistan, distanPopupView);

                    break;
                case R.id.btn_dating_publichdate:

                    String[] dateStrList = {
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
                    dateList.setAdapter(new ArrayAdapter<String>(sContext, android.R.layout.simple_list_item_1, dateStrList));

                    popupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnPublishDate, datePopupView);
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
        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("user_id", userId);
        requestParams.put("range", range);
        requestParams.put("date", date + "");
        requestParams.put("start_no", startNum + "");
        requestParams.put("end_no", endNum + "");

        String rawResult = HttpUtil.urlClient(HttpConstants.SearchDating.URL, requestParams, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the raw result we get for the dating info are : " + rawResult);
//        if (!TextUtils.isEmpty(rawResult))
//        {
//
//        }



    }

    private static final int START_RETRIEVE_ALL_DATA = 1 << 1;
    private static final int RETRIEVE_DATA_WITH_RANGE_FILTERED = 1 << 2;
    private static final int RETRIEVE_DATA_WITH_DATE_FILTERED = 1 << 3;

    private static Handler sHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case START_RETRIEVE_ALL_DATA:
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            retrieveDatingInfo("1", "", 1, 1, 1);
                        }
                    }).start();
                    break;
                case RETRIEVE_DATA_WITH_DATE_FILTERED:
                    break;
                case RETRIEVE_DATA_WITH_RANGE_FILTERED:
                    break;
            }
        }
    };

    // TODO: 以下都是测试数据,在测试接口的时候将他们删除掉
    private void initTestData()
    {
        int i;
        for (i = 0; i < 200; ++i) {
            mDatingList.add(new SearchDatingSubFragmentDatingBean("", "月夜流水", "第N届斯诺克大力神杯就要开始，一起参加啊！", "230米以内"));
        }
    }


}



















































































































































































































