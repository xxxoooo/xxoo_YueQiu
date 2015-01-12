package com.yueqiu.fragment.search;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.activity.SearchBilliardRoomActivity;
import com.yueqiu.adapter.SearchPopupBaseAdapter;
import com.yueqiu.adapter.SearchRoomSubFragmentListAdapter;
import com.yueqiu.bean.SearchRoomSubFragmentRoomBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.fragment.search.common.SubFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;

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

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    private static Button sBtnDistrict, sBtnDistan, sBtnPrice, sBtnApprisal;

    private ListView mRoomListView;
    private View mView;
    private List<SearchRoomSubFragmentRoomBean> mRoomList = new ArrayList<SearchRoomSubFragmentRoomBean>();

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

        mRoomListView = (ListView) mView.findViewById(R.id.search_room_subfragment_listview);

        // TODO: 以下是测试网络接口的可行性
        mHandler.sendEmptyMessage(REQUEST_ALL_ROOM_INFO);

        // TODO: 初始化测试数据
        initListStaticTestData();
        mRoomListView.setAdapter(new SearchRoomSubFragmentListAdapter(sContext, (ArrayList<SearchRoomSubFragmentRoomBean>) mRoomList));

        mRoomListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                SearchRoomSubFragmentRoomBean bean = mRoomList.get(position);
                Bundle bundle = new Bundle();
                bundle.putString(SubFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PHOTO, bean.getRoomPhotoUrl());
                bundle.putString(SubFragmentsCommonUtils.KEY_ROOM_FRAGMENT_NAME, bean.getRoomName());
                bundle.putFloat(SubFragmentsCommonUtils.KEY_ROOM_FRAGMENT_LEVEL, bean.getLevel());
                bundle.putDouble(SubFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PRICE, bean.getPrice());
                bundle.putString(SubFragmentsCommonUtils.KEY_ROOM_FRAGMENT_ADDRESS, bean.getDetailedAddress());

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
//                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnDistrict, R.layout.search_room_subfragment_region_popupwindow);

                    String[] regionStrList = {
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
                    TextView noFilter = (Button) regionPopupView.findViewById(R.id.btn_search_room_popup_no_filter);
                    ListView regionList = (ListView) regionPopupView.findViewById(R.id.list_search_room_region_filter_list);
                    regionList.setAdapter(new SearchPopupBaseAdapter(sContext, Arrays.asList(regionStrList)));

                    mPopupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnDistrict, regionPopupView);

                    break;
                case R.id.btn_room_distance:
                    View distancePopupView = layoutInflater.inflate(R.layout.search_mate_subfragment_distance_popupwindow, null);

                    String[] disStrList = {
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

                    break;
                case R.id.btn_room_price:
                    View pricePopupView = layoutInflater.inflate(R.layout.search_room_subfragment_price_popupwindow, null);
                    Button btnNoFilter = (Button) pricePopupView.findViewById(R.id.search_room_popupwindow_nofilter);
                    Button btnLowToHigh = (Button) pricePopupView.findViewById(R.id.search_room_popupwindow_lowtohigh);
                    Button btnHighToLow = (Button) pricePopupView.findViewById(R.id.search_room_popupwindow_hightolow);

                    btnNoFilter.setOnClickListener(new PopupWindowInternalItemClickHandler());
                    btnLowToHigh.setOnClickListener(new PopupWindowInternalItemClickHandler());
                    btnHighToLow.setOnClickListener(new PopupWindowInternalItemClickHandler());

                    mPopupWindow = SubFragmentsCommonUtils.getFilterPopupWindow(sContext, sBtnPrice, pricePopupView);
                    break;
                case R.id.btn_room_apprisal:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnApprisal, R.layout.search_mate_subfragment_distance_popupwindow);


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


            }
        }
    }

    private static final int REQUEST_ALL_ROOM_INFO = 1 << 1;
    private static final int REQUEST_ROOM_INFO_REGION_FILTERED = 1 << 2;
    private static final int REQUEST_ROOM_INFO_RANGE_FILTERED = 1 << 3;
    private static final int REQUEST_ROOM_INFO_PRICE_FILTERED = 1 << 4;
    private static final int REQUEST_ROOM_INFO_APPRISAL_FILTERED = 1 << 5;

    /**
     * * 对于大众点评提供的接口，我们默认的category是“台球”
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
     */
    private void retrieveRoomListInfo(final String city, final String region, final String range, final int sort, final int limit)
    {
        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("keyword", "台球,桌球室,台球室,桌球");
        requestParams.put("city", "北京");
        requestParams.put("region", "朝阳区");
        requestParams.put("sort", sort + "");
        requestParams.put("limit", limit + "");

        requestParams.put("format", "json");

        // TODO: 得到当前用户的经纬度信息,因为我们需要这两个值才能获得以当前用户为中心，附近指定范围内的球店信息


        String rawResult = HttpUtil.dpUrlClient(HttpConstants.DP_BASE_URL, HttpConstants.DP_RELATIVE_URL, HttpConstants.DP_APP_KEY, HttpConstants.DP_APP_SECRET, requestParams);
        Log.d(TAG, " the raw result we get are : " + rawResult);
        if (!TextUtils.isEmpty(rawResult))
        {
            try
            {
                JSONObject resultJsonObj = new JSONObject(rawResult);
                String status = resultJsonObj.getString("status");
                if (status.equals("OK")) {
                    // TODO: 现阶段，由于返回的原始的Json包含了两个int值，一个是total_count,一个是count
                    // TODO: 但是我们不确定究竟是哪一个代表的是我们获得的数据的条数，我们暂时先使用total_count这个值作为数据的条数
                    final int count = resultJsonObj.getInt("total_count");
                    JSONArray businessJsonArr = resultJsonObj.getJSONArray("businesses");
                    final int size = businessJsonArr.length();
                    int i;
                    for (i = 0; i < size; ++i)
                    {
                        JSONObject businessObj = (JSONObject) businessJsonArr.get(i);
                        String roomName = businessObj.getString("name");
                        float level = businessObj.getInt("service_score");
                        double price = businessObj.getDouble("avg_price");
                        String address = businessObj.getString("address");
                        String distance = String.valueOf(businessObj.getInt("distance"));
                        String roomPhoto = businessObj.getString("photo_list_url");

                        Log.d(TAG, " the parsed json data are : " + roomName + " , " + level + " , " + price + " , " + distance + " , " + roomPhoto + " , " + address);
                        // String roomPhoto, String roomName, float level, double price, String address, String distance
                        SearchRoomSubFragmentRoomBean roomBean = new SearchRoomSubFragmentRoomBean(roomPhoto, roomName, level, price, address, distance);
                        // TODO: 将这条数据加入到roomList当中(现在由于数据不完整，所以暂时不添加，等数据完整性已经比较好的时候再进行添加)
//                        mRoomList.add(roomBean);

                        // TODO: 然后我们还需要本地缓存我们所获得到的这条数据

                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, " Exception happened in parsing the resulted json object we get, and the detailed reason are : " + e.toString());
            }
        }


    }


    @Override
    public void onPause()
    {

        super.onPause();
    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case REQUEST_ALL_ROOM_INFO:
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            retrieveRoomListInfo("", "", "1000", 1, 20);
                        }
                    }).start();
                    break;

                case REQUEST_ROOM_INFO_APPRISAL_FILTERED:
                    // TODO: 按商家的好评度来进行筛选
                    // TODO: 我们可以避免再次进行网络请求来进行筛选，因为这样太耗费时间了，而且用户体验也特别差，我们应该是
                    // TODO: 将所有的数据获取之后保存到本地，也就是SQLite当中，然后在进行筛选才可以
                    // TODO: 这也是SearchActivity当中所有的数据请求所遵循的设计原则

                    break;
                case REQUEST_ROOM_INFO_PRICE_FILTERED:


                    break;
                case REQUEST_ROOM_INFO_RANGE_FILTERED:


                    break;
                default:
                    break;
            }

        }
    };


    // use the static data to init the BilliardsSearchRoomFragment
    private void initListStaticTestData()
    {
        Resources mRes = sContext.getResources();
        String roomName = mRes.getString(R.string.search_room_sub_fragment_listitem_roomname);
        float level = 3.5f;
        double price = 36;
        String distance = mRes.getString(R.string.search_room_sub_fragment_listitem_roomdistance);
        String address = mRes.getString(R.string.search_room_sub_fragment_listitem_roomaddress);

        int i;
        for (i = 0; i < 100; ++i) {
            mRoomList.add(new SearchRoomSubFragmentRoomBean("", roomName, level, price, address, distance));
        }
    }
}
