package com.yueqiu.fragment.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.yueqiu.R;
import com.yueqiu.adapter.SearchCoauchSubFragmentListAdapter;
import com.yueqiu.bean.SearchCoauchSubFragmentCoauchBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.fragment.search.common.SubFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by scguo on 14/12/30.
 * <p/>
 * 用于SearchActivity当中的教练子Fragment的实现
 */
@SuppressLint("ValidFragment")
public class BilliardsSearchCoauchFragment extends Fragment
{
    private static final String TAG = "BilliardsSearchAssistCoauchFragment";

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


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    private String mArgs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.search_coauch_fragment_layout, container, false);

        SubFragmentsCommonUtils.initViewPager(sContext, mView, R.id.coauch_fragment_gallery_pager, R.id.coauch_fragment_gallery_pager_indicator_group);

        (sBtnAbility = (Button) mView.findViewById(R.id.btn_coauch_ability)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnKinds = (Button) mView.findViewById(R.id.btn_coauch_kinds)).setOnClickListener(new OnFilterBtnClickListener());

        mCoauchListView = (ListView) mView.findViewById(R.id.search_coauch_subfragment_list);

        Bundle args = getArguments();
        mArgs = args.getString(PARAMS_KEY);

        initListViewTestData();
        Log.d(TAG, " the source list content are : " + mCoauchList.size());
        mCoauchListView.setAdapter(new SearchCoauchSubFragmentListAdapter(sContext, (ArrayList<SearchCoauchSubFragmentCoauchBean>) mCoauchList));

        return mView;
    }

    private static class OnFilterBtnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId()) {
                case R.id.btn_coauch_ability:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnAbility, R.layout.search_mate_subfragment_distance_popupwindow);
                    break;
                case R.id.btn_coauch_kinds:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnKinds, R.layout.search_mate_subfragment_distance_popupwindow);
                    break;
                default:
                    break;
            }

        }
    }


    // TODO: 用于获取教练的列表信息的网络请求处理过程
    private static void retrieveCoauchInfo(final String userId, final String range, final int level, final int startNum, final int endNum)
    {
        // 创建请求参数
        HashMap<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("user_id", userId);
        requestParams.put("range", range);
        requestParams.put("level", level + "");
        requestParams.put("start_no", startNum + "");
        requestParams.put("end_no", endNum + "");

        String rawResult = HttpUtil.urlClient(HttpConstants.SearchCoauch.URL, requestParams, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the raw result we get are : " + rawResult);
        JSONObject object = Utils.parseJson(rawResult);



    }

    private static final int RETRIEVE_ALL_COAUCH_INFO = 1 << 1;
    private static final int RETRIEVE_COAUCH_WITH_LEVEL_FILTERED = 1 << 2;
    private static final int RETRIEVE_COAUCH_WITH_CLASS_FILTERED = 1 << 3;

    private static Handler sHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case RETRIEVE_ALL_COAUCH_INFO:
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
//                            retrieveCoauchInfo("1233aa", "200", 1 + "", 1 + "", 1 + "");
                        }
                    }).start();
                    break;
                case RETRIEVE_COAUCH_WITH_CLASS_FILTERED:
                    break;
                case RETRIEVE_COAUCH_WITH_LEVEL_FILTERED:

                    break;

            }
        }
    };


    // TODO: 以下是测试数据,在测试接口的时候，将以下的初始化过程删除
    private List<SearchCoauchSubFragmentCoauchBean> mCoauchList = new ArrayList<SearchCoauchSubFragmentCoauchBean>();

    private void initListViewTestData()
    {
        int i;
        for (i = 0; i < 200; ++i) {
            mCoauchList.add(new SearchCoauchSubFragmentCoauchBean("", "大力水手", "男", "2000米以内", "前国家队队员", "九球"));
        }
    }
}






































































































































