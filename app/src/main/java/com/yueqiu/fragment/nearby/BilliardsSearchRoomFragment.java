package com.yueqiu.fragment.nearby;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import com.yueqiu.activity.SearchBilliardRoomActivity;
import com.yueqiu.adapter.SearchPopupBaseAdapter;
import com.yueqiu.adapter.SearchRoomSubFragmentListAdapter;
import com.yueqiu.bean.SearchRoomSubFragmentRoomBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
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
 * @author scguo
 *         <p/>
 *         这个是用于显示球厅的Fragment
 *         球厅的Fragment同MateFragment结构相同，但是ListView的差别很大。我们需要完全重新创建一个ListView
 *         用于显示关于每一个球厅的ListView item
 */
public class BilliardsSearchRoomFragment extends Fragment
{
    private static final String TAG = "BilliardsSearchRoomFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment BilliardsSearchRoomFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BilliardsSearchRoomFragment newInstance(Context context, String param1)
    {
        sContext = context;
        BilliardsSearchRoomFragment fragment = new BilliardsSearchRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    private static Context sContext;

    public BilliardsSearchRoomFragment()
    {
    }

    // 用于判断XListView是否已经到达整个ListView的顶部
    private boolean mIsHead;
    private static boolean sNetworkAvailable;

    private static WorkerHandlerThread sWorkerThread;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mIsHead = false;
        sNetworkAvailable = Utils.networkAvaiable(sContext);
        sWorkerThread = new WorkerHandlerThread();

    }

    private static Button sBtnDistrict, sBtnDistan, sBtnPrice, sBtnApprisal;

    private static PullToRefreshListView sRoomListView;
    private View mView;
    private static List<SearchRoomSubFragmentRoomBean> sRoomList = new ArrayList<SearchRoomSubFragmentRoomBean>();
    private static SearchRoomSubFragmentListAdapter sSearchRoomAdapter;

    // 加载王赟开发的ProgressBar
    private static ProgressBar sPreProgress;
    private static Drawable sProgressDrawable;
    private static TextView sPreTextView;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {


        mView = inflater.inflate(R.layout.search_room_fragment_layout, container, false);

        SubFragmentsCommonUtils.initViewPager(sContext, mView, R.id.room_fragment_gallery_pager, R.id.room_fragment_gallery_pager_indicator_group);

        (sBtnDistrict = (Button) mView.findViewById(R.id.btn_room_district)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnDistan = (Button) mView.findViewById(R.id.btn_room_distance)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnPrice = (Button) mView.findViewById(R.id.btn_room_price)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnApprisal = (Button) mView.findViewById(R.id.btn_room_apprisal)).setOnClickListener(new OnFilterBtnClickListener());

        sRoomListView = (PullToRefreshListView) mView.findViewById(R.id.search_room_subfragment_listview);
        sRoomListView.setMode(PullToRefreshBase.Mode.BOTH);
        sRoomListView.setOnRefreshListener(onRefreshListener);

        sSearchRoomAdapter = new SearchRoomSubFragmentListAdapter(sContext, (ArrayList<SearchRoomSubFragmentRoomBean>) sRoomList);
        sRoomListView.setAdapter(sSearchRoomAdapter);

        // 初始化ProgressBar
        sPreTextView = (TextView) mView.findViewById(R.id.pre_text);
        sPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        sProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();

        Rect bounds = sPreProgress.getIndeterminateDrawable().getBounds();
        sPreProgress.setIndeterminateDrawable(sProgressDrawable);
        sPreProgress.getIndeterminateDrawable().setBounds(bounds);

        // TODO: 我们在获得了供我们调用的网络数据(并且这些数据是解析之后的数据的话，ListView所设置的Adapter是一定会发生变化的，我们需要另外选择通知ListView进行刷新的时机
        // TODO: 否则就会发生IllegalStateException,也就是我们没有进行Adapter.NotifyDatasetChanged()的相关操作，或者仅仅是调用这些方法的时机是不对的)
        sRoomListView.requestLayout();
        sSearchRoomAdapter.notifyDataSetChanged();

        // TODO: 初始化测试数据
//        initListStaticTestData();

        sRoomListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // TODO: 现在的这里似乎是一个XListView引入的自己的内部的一个特性,也就是说XListView当中真正的
                // TODO: 数据开始是从第1条开始的，而不是从第0条开始的。这样我们就需要手动改动(但是具体的内部细节还
                // TODO: 是不清楚)，出现同样的问题还有助教Fragment当中的List(其实只要是涉及到List的点击事件都会需要处理这个问题)
                SearchRoomSubFragmentRoomBean bean = sRoomList.get(position);
                Bundle bundle = new Bundle();
                bundle.putString(SubFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PHOTO, bean.getRoomPhotoUrl());
                bundle.putString(SubFragmentsCommonUtils.KEY_ROOM_FRAGMENT_NAME, bean.getRoomName());
                bundle.putFloat(SubFragmentsCommonUtils.KEY_ROOM_FRAGMENT_LEVEL, bean.getLevel());
                bundle.putDouble(SubFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PRICE, bean.getPrice());
                bundle.putString(SubFragmentsCommonUtils.KEY_ROOM_FRAGMENT_TAG, bean.getRoomTag());
                bundle.putString(SubFragmentsCommonUtils.KEY_ROOM_FRAGMENT_ADDRESS, bean.getDetailedAddress());
                bundle.putString(SubFragmentsCommonUtils.KEY_ROOM_FRAGMENT_DETAILED_INFO, bean.getRoomInfo());
                bundle.putString(SubFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PHONE, bean.getRoomPhone());

                // set the arguments into the bundle, and transferred into the RoomDetailedActivity
                Intent intent = new Intent(sContext, SearchBilliardRoomActivity.class);
                intent.putExtra(SubFragmentsCommonUtils.KEY_BUNDLE_SEARCH_ROOM_FRAGMENT, bundle);

                sContext.startActivity(intent);
            }
        });


        return mView;
    }


    @Override
    public void onResume()
    {
        super.onResume();

        if (null != sWorkerThread && sWorkerThread.getState() == Thread.State.NEW) {
            sWorkerThread.start();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }


    private static class OnFilterBtnClickListener implements View.OnClickListener
    {
        private LayoutInflater layoutInflater = (LayoutInflater) sContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        private PopupWindow mPopupWindow;

        @Override
        public void onClick(View v)
        {
            switch (v.getId()) {
                case R.id.btn_room_district:

                    // TODO: 我们在这里需要注意的是(仅仅是针对球厅Fragment的请求处理方式有一些特别),由于球厅Fragment的数据请求是来自于
                    // TODO: 大众点评的接口，所以初始的请求并不是直接的把所有的数据都可以请求到，然后进行处理，而是在原来的基础上进行SQL检索
                    // TODO: 大众点评的接口是每次进行筛选的时候都是需要重新进行数据的检索
                    final String[] regionStrList = {
                            sContext.getResources().getString(R.string.search_room_popupwindow_region_changping),
                            sContext.getResources().getString(R.string.search_room_popupwindow_region_chaoyang),
                            sContext.getResources().getString(R.string.search_room_popupwindow_region_daxing),
                            sContext.getResources().getString(R.string.search_room_popupwindow_region_dongcheng),
                            sContext.getResources().getString(R.string.search_room_popupwindow_region_xicheng),
                            sContext.getResources().getString(R.string.search_room_popupwindow_region_haidian),
                            sContext.getResources().getString(R.string.search_room_popupwindow_region_feitaiqu),
                            sContext.getResources().getString(R.string.search_room_popupwindow_region_shijingshan),
                            sContext.getResources().getString(R.string.search_room_popupwindow_region_tongzhou)
                    };

                    View regionPopupView = layoutInflater.inflate(R.layout.search_room_subfragment_region_popupwindow, null);
                    Button noFilter = (Button) regionPopupView.findViewById(R.id.btn_search_room_popup_no_filter);
                    noFilter.setOnClickListener(new PopupWindowInternalItemClickHandler());
                    ListView regionList = (ListView) regionPopupView.findViewById(R.id.list_search_room_region_filter_list);
                    regionList.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(regionStrList)));
                    mPopupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnDistrict, regionPopupView);

                    regionList.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            Log.d(TAG, " Inside the popupWindow --> the item of position : " + position + " has been clicked on!!! ");
                            final String regionStr = regionStrList[position];
                            Message filterRegionMsg = sUIEventsHandler.obtainMessage(REQUEST_ROOM_INFO_REGION_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_REGION, regionStr);
                            filterRegionMsg.setData(data);
                            Log.d(TAG, " the string we passed are : " + regionStr);
                            sUIEventsHandler.sendMessage(filterRegionMsg);
                            // 当我们选择了一个条目之后，就需要将popupWindow dismiss掉
                            mPopupWindow.dismiss();
                        }
                    });

                    break;
                case R.id.btn_room_distance:
                    View distancePopupView = layoutInflater.inflate(R.layout.search_mate_subfragment_distance_popupwindow, null);

                    final String[] disStrList = {
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_500_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_1000_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_2000_str),
                            sContext.getResources().getString(R.string.search_mate_popupmenu_item_5000_str)
                    };

                    Button btnDistanceNoFilter = (Button) distancePopupView.findViewById(R.id.search_mate_popupwindow_intro);
                    btnDistanceNoFilter.setOnClickListener(new PopupWindowInternalItemClickHandler());
                    ListView distanList = (ListView) distancePopupView.findViewById(R.id.list_search_mate_distance_filter_list);
                    distanList.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(disStrList)));
                    mPopupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnDistan, distancePopupView);

                    distanList.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            Log.d(TAG, " we have selected the position : " + position + " : " + disStrList[position]);
                            final String rangeStr = disStrList[position];
                            Message msg = sUIEventsHandler.obtainMessage(REQUEST_ROOM_INFO_RANGE_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_RANGE, rangeStr);
                            msg.setData(data);
                            sUIEventsHandler.sendMessage(msg);

                            mPopupWindow.dismiss();
                        }
                    });


                    break;
                case R.id.btn_room_price:
                    final String[] priceList = {
                            sContext.getResources().getString(R.string.search_room_price_popupwindow_hightolow),
                            sContext.getResources().getString(R.string.search_room_price_popupwindow_lowtohigh)
                    };

                    View pricePopupView = layoutInflater.inflate(R.layout.search_room_subfragment_price_popupwindow, null);
                    Button btnNoFilter = (Button) pricePopupView.findViewById(R.id.search_room_popupwindow_nofilter);
                    btnNoFilter.setOnClickListener(new PopupWindowInternalItemClickHandler());

                    ListView priceListView = (ListView) pricePopupView.findViewById(R.id.list_search_room_price_filter_list);
                    priceListView.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(priceList)));

                    mPopupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnPrice, pricePopupView);

                    priceListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            final String priceStr = priceList[position];
                            Message msg = sUIEventsHandler.obtainMessage(REQUEST_ROOM_INFO_PRICE_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_PRICE, priceStr);
                            msg.setData(data);
                            sUIEventsHandler.sendMessage(msg);

                            mPopupWindow.dismiss();
                        }
                    });

                    break;
                case R.id.btn_room_apprisal:

                    final String[] apprisalArr = {
                            sContext.getResources().getString(R.string.search_room_filter_list_star),
                            sContext.getResources().getString(R.string.search_room_filter_list_comment),
                            sContext.getResources().getString(R.string.search_room_filter_list_environment),
                            sContext.getResources().getString(R.string.search_room_filter_list_service),
                            sContext.getResources().getString(R.string.search_room_filter_list_product)
                    };

                    View apprisalPopupView = layoutInflater.inflate(R.layout.search_room_subfragment_apprisal_popupwindow, null);
                    Button apprisalNoFilter = (Button) apprisalPopupView.findViewById(R.id.btn_search_room_apprisal_no_filter);
                    apprisalNoFilter.setOnClickListener(new PopupWindowInternalItemClickHandler());

                    ListView apprisalFilterList = (ListView) apprisalPopupView.findViewById(R.id.list_search_room_apprisal_list);
                    apprisalFilterList.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(apprisalArr)));

                    mPopupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnApprisal, apprisalPopupView);

                    apprisalFilterList.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            final String apprisalStr = apprisalArr[position];
                            Message msg = sUIEventsHandler.obtainMessage(REQUEST_ROOM_INFO_APPRISAL_FILTERED);
                            Bundle data = new Bundle();
                            data.putString(KEY_REQUEST_APPRISAL, apprisalStr);
                            msg.setData(data);
                            sUIEventsHandler.sendMessage(msg);
                            mPopupWindow.dismiss();
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 用于处理popupWindow内部的各个item被点击之后的处理事件
     */
    private static final class PopupWindowInternalItemClickHandler implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId()) {
                case R.id.btn_search_room_popup_no_filter:
                    Log.d(TAG, " the button inside the popupWindow has been clicked on : ");
                    break;

                case R.id.btn_search_room_apprisal_no_filter:
                    Log.d(TAG, " the button of the apprisal has been clicked on ");
                    break;
            }
        }
    }


    // TODO: 以下是用于从大众点评的接里面请求的关键字
    private static final String REQUEST_KEYWORD = "台球,桌球室,台球室,桌球";
    private static ConcurrentHashMap<String, String> sRequestParams = new ConcurrentHashMap<String, String>();

    /**
     * 对于大众点评提供的接口，我们默认的category是“台球”
     *
     * @param city   选择的城市，目前约球的默认城市是北京(这个参数我们暂时不使用，只是留到以后供扩展，我们现在只是支持北京地区,所以我们先将参数写死)
     * @param region 区域范围，例如朝阳，昌平(这些区域都是依靠我们之前选定的city做为基准的)
     * @param range  范围(例如1000米以内),这个值默认为1000，对应到具体的参数名就是radius
     *               但是这里有一个问题，就是如果我们需要传入radius作为请求参数的话，我们必须还要同时提供当前用户的经纬度
     *               因为大众点评是需要通过用户当前的经纬度来确定用户的大致范围的(我们需要单独提供用户的经纬度信息)
     * @param sort   我们本地的客户端提供了四种排序选择1. 区域；2. 距离；3. 价格；4. 好评
     *               对应于大众点评可以接受的参数则是1. 默认，2. 星级高优先(也就是好评度)，8. 人均价格低优先，9. 人均价格高优先
     *               也就是说区域是通过region指定的，距离是通过radius来指定的(单位为米，最小值为1，最大值为5000，默认是1000)
     *               价格对应于sort的8和9，好评对应于sort的2.
     *               我们在请求所有参数的时候默认sort为1
     * @param limit  我们在请求所有参数的时候，默认limit的值为40，然后每次用户滑动的时候再进行加载(这个值默认为20，最小为1，最大为40)
     *               注意，这个值指定是每一个page当中所包含的准确的item的数目
     * @param page   在我们向大众点评的Service请求数据时，他是不提供我们请求的开始条数，和结束条数的，提供的仅仅是页数，即我们可以指定
     *               请求的页面的数目
     */
    private static void retrieveRoomListInfo(final String city, final String region, final String range, final int sort, final int limit, final int page)
    {
        if (!sNetworkAvailable) {
            Log.d(TAG, " the network are really sucked off, and we have to stop fetching any more data from the server any more ");
            Message failMsg = sUIEventsHandler.obtainMessage(STATE_FETCH_DATA_FAILED);
            Bundle failData = new Bundle();
            failData.putString(KEY_FETCH_DATA_FAILED, sContext.getResources().getString(R.string.network_unavailable_hint_info_str));
            failMsg.setData(failData);
            sUIEventsHandler.sendMessage(failMsg);
            // 在请求网络任务的刚开始的时候，我们已经打开了ProgressDialog，现在既然已经确定无法继续请求，所以应该先把已经打开的ProgressDialog关闭
            sUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
        }

        sRequestParams.put("keyword", REQUEST_KEYWORD);
        sRequestParams.put("city", city);
        sRequestParams.put("region", region);
        sRequestParams.put("sort", sort + "");
        sRequestParams.put("limit", limit + "");
        // TODO: 以下是添加我们所挑选的商店的附近的商店列表的参数值(但是我们传递这个参数的前提是先要将用户当前的经度和纬度信息作为参数传递到Server端)
//        requestParams.put("range", "");
        sRequestParams.put("format", "json");
        sRequestParams.put("has_coupon", 0 + "");
        sRequestParams.put("page", page + "");

        // TODO: 得到当前用户的经纬度信息,因为我们需要这两个值才能获得以当前用户为中心，附近指定范围内的球店信息


        String rawResult = HttpUtil.dpUrlClient(HttpConstants.DP_BASE_URL, HttpConstants.DP_RELATIVE_URL, HttpConstants.DP_APP_KEY, HttpConstants.DP_APP_SECRET, sRequestParams);
        Log.d(TAG, " the raw result we get are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult)) {
            try {
                JSONObject resultJsonObj = new JSONObject(rawResult);
                Log.d(TAG, " the initial json data of the room fragment we get are : " + resultJsonObj.toString());
                if (! resultJsonObj.isNull("status"))
                {
                    String status = resultJsonObj.getString("status");
                    if (status.equals("OK"))
                    {
                        // TODO: 现阶段，由于返回的原始的Json包含了两个int值，一个是total_count,一个是count
                        // TODO: 但是我们不确定究竟是哪一个代表的是我们获得的数据的条数，我们暂时先使用total_count这个值作为数据的条数
                        final int count = resultJsonObj.getInt("total_count");
                        JSONArray businessJsonArr = resultJsonObj.getJSONArray("businesses");
                        final int size = businessJsonArr.length();
                        Log.d(TAG, " the total json objects we get are : " + size);
                        int i;
                        for (i = 0; i < size; ++i)
                        {
                            JSONObject businessObj = (JSONObject) businessJsonArr.get(i);
                            Log.d(TAG, " the sub json object we parsed out are : " + businessObj.toString());
                            final long businessId = businessObj.getLong("business_id");
                            String roomName = businessObj.getString("name");
                            float level = businessObj.getInt("service_score");
                            double price = businessObj.getDouble("avg_price");
                            String address = businessObj.getString("address");
                            String distance = String.valueOf(businessObj.getInt("distance"));
                            String roomPhoto = businessObj.getString("photo_list_url");

                            // TODO: 我们以下解析的数据全部都是为下一个即RoomDetailedActivity当中的数据(这些数据都是需要传动到球厅详情Activity当中的)
                            String roomPhoneNum = businessObj.getString("telephone");

                            // 我们从一个jsonArray当中解析出球厅详情Activity当中需要的关于球厅的tag
                            JSONArray regionJsonArr = businessObj.getJSONArray("regions");
                            Log.d(TAG, " the sub-sub json object we get are : " + regionJsonArr.toString());
                            String roomTag = parseRoomTag(regionJsonArr);
                            Log.d(TAG, " the tag we get for the room are : " + roomTag);
                            JSONArray roomDetailedInfoArr = businessObj.getJSONArray("deals");
                            String detailedRoomInfo = parseRoomDetailedInfo(roomDetailedInfoArr);
                            Log.d(TAG, " the room detailed info we get are : " + detailedRoomInfo);

                            Log.d(TAG, " after totally parsed this json obj : " + businessId + " , " + roomName + " , " + level + " , " + price + " , " + distance + " , " + roomPhoto + " , " + address);
                            // String roomPhoto, String roomName, float level, double price, String address, String distance
                            SearchRoomSubFragmentRoomBean roomBean = new SearchRoomSubFragmentRoomBean(String.valueOf(businessId), roomPhoto, roomName, level, price, address, distance, roomPhoneNum, roomTag, detailedRoomInfo);
                            // TODO: 将这条数据加入到roomList当中(现在由于数据不完整，所以暂时不添加，等数据完整性已经比较好的时候再进行添加)
                            sRoomList.add(roomBean);

                        }
                        sUIEventsHandler.obtainMessage(STATE_FETCH_DATA_SUCCESS, sRoomList).sendToTarget();
                        // 进行到这里，我们基本上也已经把所有的数据都解析完并且也加载完了。现在我们可以通过UI线程停止显示Dialog了
                        sUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);

                    } else if (status.equals("ERROR"))
                    {
                        JSONObject errorObj = resultJsonObj.getJSONObject("error");
                        final int errorCode = errorObj.getInt("errorCode");
                        final String errorMsgStr = errorObj.getString("errorMessage");
                        StringBuilder errorInfo = new StringBuilder();
                        errorInfo.append("Error Code : ").append(errorCode).append("; Error Info : ").append(errorMsgStr);

                        Message errorMsg = sUIEventsHandler.obtainMessage(PublicConstant.REQUEST_ERROR);
                        Bundle data = new Bundle();
                        data.putString(KEY_REQUEST_ERROR_MSG_ROOM, errorInfo.toString());

                        errorMsg.setData(data);
                        sUIEventsHandler.sendMessage(errorMsg);
                    }
                } else
                {
                    // 什么错误信息都没有获取到，甚至连error都没有
                    sUIEventsHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                }


            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, " Exception happened in parsing the resulted json object we get, and the detailed reason are : " + e.toString());
            }
        }
    }

    private static String parseRoomTag(JSONArray srcArr)
    {
        StringBuilder tagStr = new StringBuilder();
        final int len = srcArr.length();
        int i;
        for (i = 0; i < len; ++i) {
            try {
                tagStr.append(srcArr.get(i));
                tagStr.append(" ");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return tagStr.toString();
    }

    // TODO: 从我们得到的原始数据当中解析出关于球厅Activity当中的球厅详情字段
    // TODO: 但是现阶段我们是采用json array当中的打折信息(即deals字段)
    // TODO: 在这里我们需要将字段进行一些格式化处理，至少看起来很像ListView
    private static String parseRoomDetailedInfo(JSONArray srcArr)
    {
        StringBuilder infoStr = new StringBuilder();
        final int len = srcArr.length();
        int i;
        for (i = 0; i < len; ++i) {
            try {
                JSONObject subObj = srcArr.getJSONObject(i);
                infoStr.append(subObj.get("description"));
                infoStr.append("\n");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return infoStr.toString();
    }


    @Override
    public void onPause()
    {
        super.onPause();
    }

    private static final String KEY_FETCH_DATA_FAILED = "keyFetchDataFailed";
    private static final String WORKER_HANDLER_THREAD_NAME = "workerHandlerThread";

    private static final int UI_SHOW_DIALOG = 1 << 6;
    private static final int UI_HIDE_DIALOG = 1 << 7;

    private static final int STATE_FETCH_DATA_FAILED = 1 << 8;
    private static final int STATE_FETCH_DATA_SUCCESS = 1 << 9;

    private static final int REQUEST_ALL_ROOM_INFO = 1 << 1;
    // 以下是按FIlterButton当中当中弹出的List进行筛选时的List的点击的事件的处理
    private static final int REQUEST_ROOM_INFO_REGION_FILTERED = 1 << 2;
    private static final int REQUEST_ROOM_INFO_RANGE_FILTERED = 1 << 3;
    private static final int REQUEST_ROOM_INFO_PRICE_FILTERED = 1 << 4;
    private static final int REQUEST_ROOM_INFO_APPRISAL_FILTERED = 1 << 5;

    // TODO: 因为我们总是需要在主线程当中进行关于Adapter的更新操作，因此我们将所有的涉及到Adapter的更新就
    // TODO: 发送到sUIEventsHandler当中执行
    private static final int DATA_HAS_BEEN_UPDATED = 1 << 10;

    private static final String KEY_REQUEST_ERROR_MSG_ROOM = "keyRequestErrorMsgRoom";

    private static Handler sUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case UI_SHOW_DIALOG:
                    showProgress();
                    break;

                case UI_HIDE_DIALOG:
                    // 此时数据已经加载完毕，我们需要通知List的Adpter数据源已经发生改变
                    sSearchRoomAdapter.notifyDataSetChanged();
                    hideProgress();
                    break;

                case STATE_FETCH_DATA_FAILED:
                    Bundle failData = msg.getData();
                    String failStr = failData.getString(KEY_FETCH_DATA_FAILED);
                    Log.d(TAG, " we have fail to fetch the data for the room fragment, and the reason are : " + failStr);
                    break;
                case STATE_FETCH_DATA_SUCCESS:

                    List<SearchRoomSubFragmentRoomBean> roomList = (ArrayList<SearchRoomSubFragmentRoomBean>) msg.obj;
                    final int size = roomList.size();
                    int i;
                    for (i = 0; i < size; ++i)
                    {
                        if (! sRoomList.contains(roomList.get(i)))
                        {
                            sRoomList.add(roomList.get(i));
                        }
                    }

                    // TODO: 更新数据库

                    if (sRoomList.isEmpty())
                    {
                        loadEmptyTv();
                    }

                    break;

                case REQUEST_ROOM_INFO_RANGE_FILTERED:
                    Log.d(TAG, "inside the sUIEventsHandler --> the REQUEST_ROOM_INFO_RANGE_FILTERED ");
                    Bundle rangeData = msg.getData();
                    String rangeStr = rangeData.getString(KEY_REQUEST_RANGE);
                    sWorkerThread.fetchRoomDataRangeFiltered(rangeStr);
                    Log.d(TAG, " the range string we get in the sUIEventsHandler are : " + rangeStr);

                    break;

                case REQUEST_ROOM_INFO_PRICE_FILTERED:
                    Log.d(TAG, " inside the sUIEventsHandler --> the REQUEST_ROOM_INFO_PRICE_FILTERED ");
                    Bundle priceData = msg.getData();
                    String priceStr = priceData.getString(KEY_REQUEST_PRICE);
                    sWorkerThread.fetchRoomDataPriceFiltered(priceStr);
                    Log.d(TAG, " the price str we get in the sUIEventsHandler are : " + priceStr);
                    break;

                case REQUEST_ROOM_INFO_APPRISAL_FILTERED:
                    Log.d(TAG, " inside the sUIEventsHandler --> the REQUEST_ROOM_INFO_APPRISAL_FILTERED ");
                    Bundle apprisalData = msg.getData();
                    String apprisalStr = apprisalData.getString(KEY_REQUEST_APPRISAL);
                    sWorkerThread.fetchRoomDataApprisalFiltered(apprisalStr);
                    Log.d(TAG, " the apprisal str we get are : " + apprisalStr);

                    break;
                case REQUEST_ROOM_INFO_REGION_FILTERED:
                    Log.d(TAG, " inside the sUIEventsHandler --> the REQUEST_ROOM_INFO_REGION_FILTERED ");
                    Bundle regionData = msg.getData();
                    String regionStr = regionData.getString(KEY_REQUEST_REGION);
                    sWorkerThread.fetchRoomDataRegionFiltered(regionStr);
                    Log.d(TAG, " the region str we get are : " + regionStr);
                    break;

                case DATA_HAS_BEEN_UPDATED:
                    // TODO: 考虑移除这个标签，因为这会使Adapter的更新发生异常
                    break;

                // 对于大众点评的服务牛逼的一点在于所有的请求错误，会有详细的错误信息供我们向用户展示
                // 所以我们不需要单独的额外的判断errorCode来判断具体的错误信息，我们只需要定义一条信息就可以了
                case PublicConstant.REQUEST_ERROR:
                    Bundle errorData = msg.getData();
                    if (null != errorData)
                    {
                        Log.d(TAG , " the error data we get are : " + errorData);
                        Utils.showToast(sContext, errorData.getString(KEY_REQUEST_ERROR_MSG_ROOM));
                    } else {
                        Utils.showToast(sContext, sContext.getString(R.string.http_request_error));
                    }

                    if (sRoomList.isEmpty()) {
                        loadEmptyTv();
                    }

                    break;
            }

            sSearchRoomAdapter = new SearchRoomSubFragmentListAdapter(sContext, (ArrayList<SearchRoomSubFragmentRoomBean>) sRoomList);
            sRoomListView.setAdapter(sSearchRoomAdapter);
            sSearchRoomAdapter.notifyDataSetChanged();

        }
    };

    private static void loadEmptyTv()
    {
        SubFragmentsCommonUtils.setFragmentEmptyTextView(sContext, sRoomListView, sContext.getString(R.string.search_activity_subfragment_empty_tv_str));
    }

    private static void showProgress()
    {
        sPreTextView.setVisibility(View.VISIBLE);
        sPreProgress.setVisibility(View.VISIBLE);
    }

    private static void hideProgress()
    {
        sPreProgress.setVisibility(View.GONE);
        sPreTextView.setVisibility(View.GONE);
    }

    private static final String KEY_REQUEST_REGION = "keyRequestRegion";
    private static final String KEY_REQUEST_PRICE = "keyRequestPrice";
    private static final String KEY_REQUEST_APPRISAL = "keyRequestApprisal";
    private static final String KEY_REQUEST_RANGE = "keyRequestRange";

    private static class WorkerHandlerThread extends HandlerThread
    {

        public WorkerHandlerThread()
        {
            super(WORKER_HANDLER_THREAD_NAME, Process.THREAD_PRIORITY_BACKGROUND);
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
                        case REQUEST_ALL_ROOM_INFO:
                            // 通知UI线程开始显示dialog
                            sUIEventsHandler.sendEmptyMessage(UI_SHOW_DIALOG);
                            // 然后开始正式的加载数据
                            retrieveRoomListInfo("北京", "海淀区", "1000", 2, 20, 1);
                            break;
                        case REQUEST_ROOM_INFO_APPRISAL_FILTERED:
                            // TODO: 按商家的好评度来进行筛选
                            // TODO: 我们可以避免再次进行网络请求来进行筛选，因为这样太耗费时间了，而且用户体验也特别差，我们应该是
                            // TODO: 将所有的数据获取之后保存到本地，也就是SQLite当中，然后在进行筛选才可以
                            // TODO: 这也是SearchActivity当中所有的数据请求所遵循的设计原则
                            Bundle apprisalData = msg.getData();
                            String apprisalStr = apprisalData.getString(KEY_REQUEST_APPRISAL);
                            Log.d(TAG, " in the internal mWorkThread --> the data we received to fetch the data based on the apprisal rule are : " + apprisalStr);
                            sUIEventsHandler.sendEmptyMessage(UI_SHOW_DIALOG);
                            // TODO: 具体的请求方法

                            sUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);

                            break;
                        case REQUEST_ROOM_INFO_PRICE_FILTERED:
                            Bundle priceData = msg.getData();
                            String priceStr = priceData.getString(KEY_REQUEST_PRICE);
                            Log.d(TAG, "in the internal mWorkThread --> the price data we get are : " + priceStr);
                            sUIEventsHandler.sendEmptyMessage(UI_SHOW_DIALOG);

                            // TODO:

                            sUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);

                            break;
                        case REQUEST_ROOM_INFO_RANGE_FILTERED:
                            Bundle rangeData = msg.getData();
                            String rangeStr = rangeData.getString(KEY_REQUEST_RANGE);
                            Log.d(TAG, " in the internal mWorkThread --> the range string we get are : " + rangeStr);
                            sUIEventsHandler.sendEmptyMessage(UI_SHOW_DIALOG);
                            // TODO:

                            sUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                            break;

                        case REQUEST_ROOM_INFO_REGION_FILTERED:
                            Log.d(TAG, " In the mWorkerHandler : we have received the message to handle the task of region filtering ");
                            Bundle regionData = msg.getData();
                            String regionStr = regionData.getString(KEY_REQUEST_REGION);
                            Log.d(TAG, " we have received the string to send the message, and the string are : " + regionStr);
                            // 我们在这里开始真正的请求过程(即进行网络请求，请求参数即为我们这里获取到的region字符串)
                            sUIEventsHandler.sendEmptyMessage(UI_SHOW_DIALOG);
                            // TODO: 但是我们这里需要先将ListView之前当中的数据清空，否则我们似乎无法直接
                            // TODO: 将listView当中的数据进行替换
                            retrieveRoomListInfo("北京", regionStr, "1000", 2, 20, 1);
                            sUIEventsHandler.sendEmptyMessage(UI_HIDE_DIALOG);
                            break;

                    }
                }
            };
            fetchRoomData();
        }

        public void fetchRoomData()
        {
            mWorkerHandler.sendEmptyMessage(REQUEST_ALL_ROOM_INFO);
        }

        public void fetchRoomDataRegionFiltered(String regionStr)
        {
            Log.d(TAG, " in the BackgroundWorkerThread : the region str we get are : " + regionStr);
            Message msg = mWorkerHandler.obtainMessage(REQUEST_ROOM_INFO_REGION_FILTERED);
            Bundle data = new Bundle();
            data.putString(KEY_REQUEST_REGION, regionStr);
            msg.setData(data);
            mWorkerHandler.sendMessage(msg);
        }

        public void fetchRoomDataPriceFiltered(String priceStr)
        {
            Message msg = mWorkerHandler.obtainMessage(REQUEST_ROOM_INFO_PRICE_FILTERED);
            Bundle data = new Bundle();
            data.putString(KEY_REQUEST_PRICE, priceStr);
            msg.setData(data);
            mWorkerHandler.sendMessage(msg);
        }

        public void fetchRoomDataRangeFiltered(String rangeStr)
        {
            Message msg = mWorkerHandler.obtainMessage(REQUEST_ROOM_INFO_RANGE_FILTERED);
            Bundle data = new Bundle();
            data.putString(KEY_REQUEST_RANGE, rangeStr);
            msg.setData(data);
            mWorkerHandler.sendMessage(msg);
        }

        public void fetchRoomDataApprisalFiltered(String apprisalStr)
        {
            Message msg = mWorkerHandler.obtainMessage(REQUEST_ROOM_INFO_APPRISAL_FILTERED);
            Bundle data = new Bundle();
            data.putString(KEY_REQUEST_APPRISAL, apprisalStr);
            msg.setData(data);
            mWorkerHandler.sendMessage(msg);

        }
    }

    // 定义的用于下拉刷新过程当中需要用到的变量
    private static boolean sLoadMore, sRefresh;

    // TODO: 尝试将下面的这个过程抽象出来，单独形成一个Module放大SearchFragmentCommonUtils里面
    /**
     * 实现RoomFragment当中的ListView的下拉刷新的实现逻辑
     * 这个过程是可以抽象出来的
     */
    private PullToRefreshBase.OnRefreshListener2<ListView> onRefreshListener = new PullToRefreshBase.OnRefreshListener2<ListView>()
    {

        /**
         * onPullDownToRefresh will be called only when the user has Pulled from
         * the start, and released.
         *
         * @param refreshView
         */
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            String label = SubFragmentsCommonUtils.getLastedTime(sContext);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (Utils.networkAvaiable(sContext)) {
                        sRefresh = true;
                        sLoadMore = false;
                        // 我们将我们的page数增加
                        // 因为我们向下拉的时候，加载的总是最新的数据，所以我们将这里的page的数目置成1，
                        // 因为第一页当中的数据总是最新的
                        sRequestParams.put("page", 1 + "");
                        retrieveRoomListInfo("北京", "海淀区", "1000", 2, 20, 1);
                    } else {
                        Toast.makeText(sContext, sContext.getString(R.string.network_not_available), Toast.LENGTH_LONG).show();
                    }
                }
            }).start();
        }

        /**
         * onPullUpToRefresh will be called only when the user has Pulled from
         * the end, and released.
         *
         * @param refreshView
         */
        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
        {
            String label = SubFragmentsCommonUtils.getLastedTime(sContext);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

        }
    };

    // TODO: 以下仅仅是测试数据，在项目最终通过测试之后再删除下面的静态数据部分
    // use the static data to init the BilliardsSearchRoomFragment
    private void initListStaticTestData()
    {
        Resources mRes = sContext.getResources();
        String roomName = mRes.getString(R.string.search_room_sub_fragment_listitem_roomname);
        float level = 3.5f;
        double price = 36;
        String distance = mRes.getString(R.string.search_room_sub_fragment_listitem_roomdistance);
        String address = mRes.getString(R.string.search_room_sub_fragment_listitem_roomaddress);

        String roomPhoneNum = "110-120-119";
        String roomTag = "西城区 五道口 清华大学旁边厕所附近";
        String roomDetailedInfo = "办会员卡，容声VIP会员。我是屌丝，我为屌丝代言";
        int i;
        for (i = 0; i < 100; ++i) {
            sRoomList.add(new SearchRoomSubFragmentRoomBean("", "", roomName, level, price, address, distance, roomPhoneNum, roomTag, roomDetailedInfo));
        }
    }
}
