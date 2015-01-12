package com.yueqiu.fragment.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.adapter.SearchMateFragmentViewPagerImgAdapter;
import com.yueqiu.adapter.SearchMateSubFragmentListAdapter;
import com.yueqiu.adapter.SearchPopupBaseAdapter;
import com.yueqiu.bean.SearchMateSubFragmentUserBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.fragment.search.common.SubFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;

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

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.search_mate_fragment_layout, container, false);
        // then, inflate the image view pager
        SubFragmentsCommonUtils.initViewPager(sContext, mView, R.id.mate_fragment_gallery_pager, R.id.mate_fragment_gallery_pager_indicator_group);

        mSubFragmentList = (ListView) mView.findViewById(R.id.search_sub_fragment_list);

        (sBtnDistanceFilter = (Button) mView.findViewById(R.id.btn_mate_distance)).setOnClickListener(new BtnFilterClickListener());
        (sBtnGenderFilter = (Button) mView.findViewById(R.id.btn_mate_gender)).setOnClickListener(new BtnFilterClickListener());

        Bundle args = getArguments();
        mArgs = args.getString(BILLIARD_SEARCH_TAB_NAME);


        sHandler.sendEmptyMessage(START_RETRIEVE_ALL_DATA);

        initListViewDataSrc();
        mSubFragmentList.setAdapter(new SearchMateSubFragmentListAdapter(sContext, (ArrayList<SearchMateSubFragmentUserBean>) mUserList));

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
                    View genderFilerView = inflater.inflate(R.layout.search_mate_subfragment_gender_popupwindow, null);

                    Button btnMaleFilter = (Button) genderFilerView.findViewById(R.id.search_mate_popupwindow_male);
                    Button btnFemaleFilter = (Button) genderFilerView.findViewById(R.id.search_mate_popupwindow_female);

                    btnMaleFilter.setOnClickListener(new MatePopupInternalItemHandler());
                    btnFemaleFilter.setOnClickListener(new MatePopupInternalItemHandler());

                    popupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnGenderFilter, genderFilerView);
                    break;
                case R.id.btn_mate_distance:
                    String[] disStrList = {
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




    // TODO: 以下我们暂时采用的默认数据是1000米，筛选性别为女性作为默认的条件(需要同服务器端进一步确定默认的条件)

    /**
     * 在以下的选择当中，我们需要指定默认的情况，即默认情况下我们应该获取的是多少距离范围内的数据，还有
     * 默认情况下我们指定的应该是女性还是男性
     *
     * @param userId
     * @param range  这个是用于筛选的条件之一：距离，我们通过传送用户指定的距离的数据，即可以或得到指定的数据
     * @param sex    这也是用于筛选的条件之一：性别，我们通过FilterButton当中用户的选择来获得特定的列表数据
     */
    @Deprecated
    private static void retrieveMateRawInfo(final String userId, final String range, final int sex, final int startNum, final int endNum)
    {
        // 我们采用ConcurrentHashMap来保存请求参数，因为我们为了加速请求过程会使用多线程，这样更安全一些
        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("user_id", userId);
        requestParams.put("range", range);
        requestParams.put("sex", sex + "");
        requestParams.put("start_no", startNum + "");
        requestParams.put("end_no", endNum + "");
        String rawResult = HttpUtil.urlClient(HttpConstants.SearchMate.URL, requestParams, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the raw result we get are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult))
        {
            JSONObject resultJson = Utils.parseJson(rawResult);
            if (!TextUtils.isEmpty(String.valueOf(resultJson)))
            {
                try
                {   if(!resultJson.isNull("code"))
                    {
                        final int retCode = resultJson.getInt("code");
                        if (retCode == HttpConstants.ResponseCode.NORMAL) {
                            Log.d(TAG, "we have retrieved the data successfully");
                            JSONObject rawSrcData = resultJson.getJSONObject("result");
                            JSONArray srcDataList = rawSrcData.getJSONArray("list_data");
                            final int dataSize = srcDataList.length();
                            Log.d(TAG, " the data size are : " + dataSize);
                            int i;
                            for (i = 0; i < dataSize; ++i) {
                                JSONObject dataUnit = srcDataList.getJSONObject(i);
                                Log.d(TAG, "dataUnit we get are : " + dataUnit.toString());
    //                        String name = String.valueOf(dataUnit.get("username"));
    //                        String distance = String.valueOf(dataUnit.get("range"));
    //                        String gender = dataUnit.getString("sex");
    //                        String userDistrict = dataUnit.getString("district");

                                SearchMateSubFragmentUserBean beanData = new SearchMateSubFragmentUserBean(dataUnit);
                                Log.d(TAG, " after converted, the bean data are : " + beanData);

                                // TODO: 然后就是把这个解析后的bean文件添加到mUserList

                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // TODO: 添加一些容错处理，我们不能使程序在这里直接崩溃，而是添加一些措施，
            // TODO: 例如提醒用户网络不好，或者用户需要登录之后才能查看最新的数据。或者只是单纯的现实以前加载的数据


            return;
        }
    }

    /**
     * 用于请求首页当中的球友的信息列表
     * 这里不需要任何请求参数
     */
    private static void retrieveInitialMateInfoList()
    {
        String rawResult = HttpUtil.urlClient(HttpConstants.SearchMate.URL, null, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the raw result we get for the mate fragment are : " + rawResult);
        if (! TextUtils.isEmpty(rawResult))
        {
            try
            {
                JSONObject initialObj = new JSONObject(rawResult);

                final int dataCount = initialObj.getInt("count");
                JSONArray dataList = initialObj.getJSONArray("list_data");
                int i;
                for (i = 0; i < dataCount; ++i)
                {
                    JSONObject dataObj = (JSONObject) dataList.get(i);
                    String imgUrl = dataObj.getString("img_url");
                    // TODO: 这个字段很奇怪，在原型图当中是没有这个字段的。而且也没必要有这个字段。我们需要同服务器端进一步确定一下？？？
//                    String money = dataObj.getString("money");

                }

                Log.d(TAG, " the initial json object we need to parse are : " + initialObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private static final int START_RETRIEVE_ALL_DATA = 1 << 1;
    private static final int DATA_RETRIEVE_SUCCESS = 1 << 2;
    private static final int DATA_RETRIEVE_FAILED = 1 << 3;
    private static final int START_RETRIEVE_DATA_WITH_RANGE_FILTER = 1 << 4;
    private static final int START_RETRIEVE_DATA_WITH_GENDER_FILTER = 1 << 5;

    private static Handler sHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case START_RETRIEVE_ALL_DATA:
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Log.d(TAG, "start retrieving the list info ");
                            retrieveInitialMateInfoList();
                        }
                    }).start();
                    break;
                case START_RETRIEVE_DATA_WITH_GENDER_FILTER:
//                    msg.obj
                    break;
                case START_RETRIEVE_DATA_WITH_RANGE_FILTER:

                    break;
                case DATA_RETRIEVE_FAILED:
                    Toast.makeText(sContext, "", Toast.LENGTH_LONG).show();
                    break;
                case DATA_RETRIEVE_SUCCESS:
                    Toast.makeText(sContext, "", Toast.LENGTH_LONG).show();
                    break;
            }

        }
    };


    private List<SearchMateSubFragmentUserBean> mUserList = new ArrayList<SearchMateSubFragmentUserBean>();

    // TODO: the following are just for testing
    // TODO: and remove all of them out with the true data we retrieved from RESTful WebService
    private void initListViewDataSrc()
    {
        int i;
        for (i = 0; i < 100; ++i) {
            mUserList.add(new SearchMateSubFragmentUserBean("", "月夜流沙", "男", "昌平区", "20000米以内"));
        }
    }
}


















