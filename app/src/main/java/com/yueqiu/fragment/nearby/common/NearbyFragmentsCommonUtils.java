package com.yueqiu.fragment.nearby.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.activity.NearbyBilliardRoomActivity;
import com.yueqiu.adapter.NearbyMateFragmentViewPagerImgAdapter;
import com.yueqiu.bean.NearbyRoomSubFragmentRoomBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/2/9.
 */
public class NearbyFragmentsCommonUtils{
    private static final String TAG = "NearbyFragmentsCommonUtils";

    public static interface ControlPopupWindowCallback
    {
        public void closePopupWindow();
    }

    // 定义用于保存NearbyActivity当中的Fragment的List以及一些ListView的position的值
    public static final String KEY_SAVED_LISTVIEW = "savedListView";
    public static final String KEY_SAVED_REFRESH = "savedRefresh";
    public static final String KEY_SAVED_LOAD_MORE = "savedLoadMore";
    public static final String KEY_SAVED_INSTANCE = "savedInstance";

    // 定义用于处理从Fragment的ListView点击之后切换到具体的Activity时的切换过程
    // 以下是用于球厅Fragment当中需要传输的数据的详细的key值
    public static final String KEY_BUNDLE_SEARCH_ROOM_FRAGMENT = "searchRoomFragment";
    public static final String KEY_ROOM_FRAGMENT_PRICE = "roomPrice";
    public static final String KEY_ROOM_FRAGMENT_TAG = "roomTag";
    public static final String KEY_ROOM_FRAGMENT_ADDRESS = "roomAddress";
    public static final String KEY_ROOM_FRAGMENT_PHONE = "roomPhone";
    public static final String KEY_ROOM_FRAGMENT_PHOTO = "roomPhoto";
    public static final String KEY_ROOM_FRAGMENT_NAME = "roomName";
    public static final String KEY_ROOM_FRAGMENT_LEVEL = "roomLevel";
    public static final String KEY_ROOM_FRAGMENT_DETAILED_INFO = "roomDetailedInfo";
    public static final String KEY_ROOM_FRAGMENT_SHOP_HOURS = "roomShopHours"; // 球厅的营业时间
    public static final String KEY_ROOM_FRAGMENT_LAT = "roomLatitude"; // 球厅的纬度
    public static final String KEY_ROOM_FRAGMENT_LNG = "roomLongtitude"; // 球厅的经度

    // 以下是用于球友Fragment当中需要传输的数据的详细的key值
    public static final String KEY_BUNDLE_SEARCH_MATE_FRAGMENT = "searchMateFragment";

    // 以下是用于约球Fragment当中需要传输的数据的详细的key值
    public static final String KEY_BUNDLE_SEARCH_DATING_FRAGMENT = "searchDatingFragment";
    public static final String KEY_DATING_FRAGMENT_PHOTO = "datingPhoto";
    public static final String KEY_DATING_FRAGMENT_NAME = "datingName";
    public static final String KEY_DATING_FRAGMENT_GENDER = "datingGender";
    public static final String KEY_DATING_FRAGMENT_FOLLOWNUM = "datingFollowNum";
    public static final String KEY_DATING_PUBLISH_TIME = "datingTime";
    public static final String KEY_DATING_TABLE_ID = "datingTableId";
    public static final String KEY_DATING_USER_NAME = "datingUserName";

    // 以下是用于教练Fragment当中需要传输的数据的详细的key值
    public static final String KEY_BUNDLE_SEARCH_COAUCH_FRAGMENT = "searchCoauchFragment";

    // 以下是用于助教Fragment当中需要传输的数据的详细的key值
    public static final String KEY_BUNDLE_SEARCH_ASSISTCOAUCH_FRAGMENT = "searchAssistCoauchFragment";

    public NearbyFragmentsCommonUtils(Context context) {
        this.mContext = context;
    }

    public static String getLastedTime(Context context)
    {
        return DateUtils.formatDateTime(context, System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
    }

//    public static void setFragmentEmptyTextView(Context context, final PullToRefreshListView listView, TextView emptyView,final String emptyText, boolean disable)
//    {
//        emptyView.setGravity(Gravity.CENTER);
//        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//        emptyView.setTextColor(context.getResources().getColor(R.color.md__defaultBackground));
//        emptyView.setText(emptyText);
//        if (disable)
//        {
//            emptyView.setVisibility(View.GONE);
//        } else
//        {
//            listView.setEmptyView(emptyView);
//        }
//    }

    public static PopupWindow getFilterPopupWindow(Context context, View anchorView, View popupLayoutView)
    {
        final int popupWidth = LinearLayout.LayoutParams.MATCH_PARENT;
        final int popupHeight = LinearLayout.LayoutParams.WRAP_CONTENT;

        final PopupWindow popupWindow = new PopupWindow(context);
        popupWindow.setContentView(popupLayoutView);
        popupWindow.setWidth(popupWidth);
        popupWindow.setHeight(popupHeight);
        popupWindow.setFocusable(true);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.popup_window_bg));

        Log.d(TAG, " inside the popupWindow creator --> before we display the popupWindow --> the anchor view state are : " + (anchorView == null));
        if (anchorView != null)
        {
            popupWindow.showAsDropDown(anchorView);
        }

        Log.d(TAG, " inside the popupWindow creator --> after we display the popupWindow --> the anchor view state are : " + (anchorView == null));

        return popupWindow;
    }

    // TODO: 这里我们使用的是服务器端的同学开发的接口
    // TODO: 这里我们加载的是商家推荐的信息的列表，也就是显示在每一个Fragment当中的最上面的滚动的Image Gallery
    private  void retrieveRecommdedRoomInfo()
    {
        Log.d(TAG, " start retrieving the recommendation image gallery info ... ");
        String rawResult = HttpUtil.urlClient(HttpConstants.NearbyRoomRecommendation.URL, null, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the recommendation info we get are : " + rawResult);

        List<NearbyRoomSubFragmentRoomBean> cacheRoomList = new ArrayList<NearbyRoomSubFragmentRoomBean>();

        if (!TextUtils.isEmpty(rawResult))
        {
            try
            {
                JSONObject initialJsonData = new JSONObject(rawResult);
                Log.d(TAG, " the initial json data we get are : " + initialJsonData.toString());
                if(!initialJsonData.isNull("code")) {
                    final int status = initialJsonData.getInt("code");
                    if (status == HttpConstants.ResponseCode.NORMAL) {
                        JSONArray resultJsonArr = initialJsonData.getJSONArray("result");
                        final int count = resultJsonArr.length();
                        for (int i = 0; i < count; ++i) {
                            JSONObject dataUnit = resultJsonArr.getJSONObject(i);
                            String roomId = dataUnit.getString("id");
                            String photoUrl = dataUnit.getString("img_url");
                            String roomName = dataUnit.getString("name");
                            String roomAddress = dataUnit.getString("address");
                            String roomTelephone = dataUnit.getString("telephone");
                            String roomDetailInfo = dataUnit.getString("detail_info");
                            String roomPrice = dataUnit.getString("price");
                            String roomStarLevel = dataUnit.getString("overall_rating");
                            String roomShopHours = dataUnit.getString("shop_hours");
                            // TODO: 以下的关于球厅的经度和纬度信息我们暂时还不需要，而且球厅当中也没有定义关于经度纬度信息的详细的存放的位置
                            // TODO: 所以我们暂时先不管这两个字段了
                            String roomLongitude = dataUnit.getString("lng");
                            String roomLatitude = dataUnit.getString("lat");

                            int roomLevelVal = 0;
                            double roomPriceVal = 0;
                            try {
                                // 我们需要将我们的room的评分的星级的总星级数目设置为100
                                roomLevelVal = Integer.parseInt(roomStarLevel);
                                roomPriceVal = Double.parseDouble(roomPrice);
                            } catch (final Exception e) {
                                Log.d(TAG, " exception happened while we parse the start number and the room price, cause to : " + e.toString());
                            }
                            NearbyRoomSubFragmentRoomBean roomItem = new NearbyRoomSubFragmentRoomBean(
                                    roomId, // room id
                                    photoUrl, // room photo
                                    roomName, // room name
                                    roomLevelVal, // room level
                                    roomPriceVal, // room price
                                    roomAddress, // room address
                                    "", // room distance(这个在最新版的接口当中被取消了)
                                    roomTelephone, // roomPhone
                                    "", // roomTag
                                    roomDetailInfo,// roomInfo
                                    roomShopHours // shopHours营业时间
                            );
                            cacheRoomList.add(roomItem);
                            Log.d(TAG, " ----> the photo url we get for the recommendation are : " + roomItem.getRoomPhotoUrl() + "; "
                                    + roomAddress + "; " + roomLatitude + " ; " + roomLongitude + "; " + roomPrice
                                    + "; " + roomPriceVal);
                        }
                        // 现在我们就需要将我们获得的数据传递出去
                        mInternalHandler.obtainMessage(DATA_RETRIEVE_SUCCESS, cacheRoomList).sendToTarget();
                    }
                }
                else{
                    mInternalHandler.sendEmptyMessage(DATA_RETRIEVE_FAILED);
                }
            } catch (JSONException e)
            {
                // TODO: 我们在后期加入错误的原因，用于Toast的内容显示
                mInternalHandler.obtainMessage(DATA_RETRIEVE_FAILED, "").sendToTarget();
                e.printStackTrace();
                Log.d(TAG, " exception happened in parsing the room recommendation detailed information, and the detailed reason are : " + e.toString());
            }
        }
        //TODO:这应该加一个else判断吧？如果不加的话，之前的代码都没有return的地方，岂不是不管怎样都会执行DATA_RETRIEVE_DATA
        else {
            mInternalHandler.sendEmptyMessage(DATA_RETRIEVE_FAILED);
        }
    }

    private static final int START_RETRIEVING_DATA = 1 << 1;
    private static final int DATA_RETRIEVE_SUCCESS = 1 << 2;
    private static final int DATA_RETRIEVE_FAILED = 1 << 3;



    private Handler mInternalHandler = new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case DATA_RETRIEVE_SUCCESS:
                    //TODO:获取成功以后，不应该再显示那一行小字
                    mNoDataIndicatorText.setVisibility(View.INVISIBLE);
                    mGlobalRoomList = (List<NearbyRoomSubFragmentRoomBean>) msg.obj;
                    mPagerIndicatorImgList = new ImageView[mGlobalRoomList.size()];

                    final int size = mPagerIndicatorImgList.length;
                    ImageView indicatorView;
                    // 用于初始化和控制Image Gallery下面的indicator圆点
                    for (int i = 0; i < size; ++i)
                    {
                        indicatorView = new ImageView(mContext);
                        indicatorView.setLayoutParams(new ViewGroup.LayoutParams(10, 10));

                        mPagerIndicatorImgList[i] = indicatorView;
                        // 用于初始化所有的indicator的初始状态
                        if (i == 0)
                        {
                            mPagerIndicatorImgList[i].setBackgroundResource(R.drawable.page_indicator_focused);
                        } else
                        {
                            mPagerIndicatorImgList[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
                        }

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        params.leftMargin = 5;
                        params.rightMargin = 5;

                        mGalleryIndicatorGroup.addView(mPagerIndicatorImgList[i], params);
                    }

                    mPagerImgArr = new NetworkImageView[size];
                    // 用于初始化具体的ImageGallery
                    for (int i = 0; i < size; ++i)
                    {
                        if (mGlobalRoomList.size() > 0)
                        {
                            final NearbyRoomSubFragmentRoomBean roomItem = mGlobalRoomList.get(i);
                            NetworkImageView imgView = new NetworkImageView(mContext);
                            imgView.setScaleType(ImageView.ScaleType.FIT_XY);
                            imgView.setDefaultImageResId(R.drawable.default_reommend_img);
                            imgView.setImageUrl(roomItem.getRoomPhotoUrl(), mImgLoader);

                            imgView.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    // 我们在这里处理当约球顶部的ImageView被点击之后的处理事件，
                                    // 这里我们设定当被点击之后跳转到球厅详情Activity当中
                                    Intent intent = new Intent(mContext, NearbyBilliardRoomActivity.class);
                                    Bundle imgRoomData = new Bundle();

                                    imgRoomData.putString(KEY_ROOM_FRAGMENT_PHOTO, roomItem.getRoomPhotoUrl());
                                    imgRoomData.putString(KEY_ROOM_FRAGMENT_ADDRESS, roomItem.getDetailedAddress());
                                    imgRoomData.putString(KEY_ROOM_FRAGMENT_NAME, roomItem.getRoomName());
                                    imgRoomData.putDouble(KEY_ROOM_FRAGMENT_PRICE, roomItem.getPrice());
                                    imgRoomData.putFloat(KEY_ROOM_FRAGMENT_LEVEL, roomItem.getLevel());
                                    imgRoomData.putString(KEY_ROOM_FRAGMENT_SHOP_HOURS, roomItem.getShopHours());
                                    imgRoomData.putString(KEY_ROOM_FRAGMENT_PHONE, roomItem.getRoomPhone());
                                    // TODO: 对于经纬度信息我们暂时不传递了,因为原型界面当中并没有提供经纬度的相关信息
//                                  imgRoomData.putString(KEY_ROOM_FRAGMENT_LAT, roomItem.get);
                                    intent.putExtra(KEY_BUNDLE_SEARCH_ROOM_FRAGMENT, imgRoomData);
                                    mContext.startActivity(intent);
                                }
                            });
                            mPagerImgArr[i] = imgView;
                        }
                    }
                    mGalleryImgAdapter = new NearbyMateFragmentViewPagerImgAdapter(mPagerImgArr);
                    mImgGalleryViewPager.setAdapter(mGalleryImgAdapter);
                    mImgGalleryViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
                    {
                        @Override
                        public void onPageScrolled(int i, float v, int i2)
                        {
                        }
                        @Override
                        public void onPageSelected(int i)
                        {
                            int j;
                            for (j = 0; j < size; ++j)
                            {
                                if (j == (i % size))
                                {
                                    mPagerIndicatorImgList[j].setBackgroundResource(R.drawable.page_indicator_focused);
                                } else
                                {
                                    mPagerIndicatorImgList[j].setBackgroundResource(R.drawable.page_indicator_unfocused);
                                }
                            }
                        }
                        @Override
                        public void onPageScrollStateChanged(int i)
                        {

                        }
                    });
                    break;
                case DATA_RETRIEVE_FAILED:
                    //TODO:获取失败时，也应该显示默认图片，然后底下的那一行小字要显示出来，获取失败，小圆点也不应该显示
                    //TODO:同时，由于获取失败不应该有点击事件
                    mNoDataIndicatorText.setVisibility(View.VISIBLE);
                    mPagerImgArr = new NetworkImageView[1];
                    NetworkImageView imgView = new NetworkImageView(mContext);
                    imgView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imgView.setDefaultImageResId(R.drawable.default_reommend_img);
                    imgView.setImageUrl("", mImgLoader);
                    mPagerImgArr[0] = imgView;
                    mGalleryImgAdapter = new NearbyMateFragmentViewPagerImgAdapter(mPagerImgArr);
                    mImgGalleryViewPager.setAdapter(mGalleryImgAdapter);
                    break;
            }
        }
    };

    private List<NearbyRoomSubFragmentRoomBean> mGlobalRoomList = new ArrayList<NearbyRoomSubFragmentRoomBean>();

    private NetworkImageView[] mPagerImgArr;

    private LinearLayout mGalleryIndicatorGroup;

    private NearbyMateFragmentViewPagerImgAdapter mGalleryImgAdapter;

    private ViewPager mImgGalleryViewPager;

    private TextView mNoDataIndicatorText;

    private ImageLoader mImgLoader;

    private ImageView[] mPagerIndicatorImgList;

    private Context mContext;
    /**
     * @param context
     * @param parentView         用于加载ViewPager的父View
     */
    public void initViewPager(final Context context, View parentView)
    {
        // 我们现在需要增加这个判断条件，因为我们现在的Fragment的创建方式不是符合创建Fragment的最佳实践，即我们
        // 在子Fragment当中获取Context实例不是通过onAttachedToActivity()这个生命周期方法来获得的，而是通过
        // 最原始的Fragment的构造方法来获得的。
        // 通过构造方法来获取就会产生一个Bug，那就是Fragment所依附的Activity已经被回收了，但是Fragment还没有被回收,
        // 然后我们进入程序时导致Fragment使用的还是旧的Context(在这里就是我们的Activity实例)，由于Activity已经被回收了，
        // 所以导致传过来的context参数就是空的，所以程序会崩溃。
        // 但是我们现在不能直接修改，主要是我前期设计上的缺陷导致的，应该从一开始就遵循Fragment的最佳实现模式.放到下一个项目当中再改吧
        if (null == context)
            return;

        mImgGalleryViewPager = (ViewPager) parentView.findViewById(R.id.fragment_gallery_pager);
        mGalleryIndicatorGroup = (LinearLayout) parentView.findViewById(R.id.fragment_gallery_pager_indicator_group);
        mNoDataIndicatorText = (TextView) parentView.findViewById(R.id.tv_gallery_view_pager_indication);

        mImgLoader = VolleySingleton.getInstance().getImgLoader();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                retrieveRecommdedRoomInfo();
            }
        }).start();
    }
    // 通过得到的关于性别的字符串来解析成具体的男女的字符串
    public static String parseGenderStr(Context context, String sexVal)
    {
        if (!TextUtils.isEmpty(sexVal)) {
            return sexVal.equals("1") ? context.getString(R.string.man) : context.getString(R.string.woman);
        }
        return "";
    }

    public static final int parseGenderDrawable(String sexVal)
    {
        if (!TextUtils.isEmpty(sexVal))
        {
            Log.d(TAG, " the sex val we get are :" + sexVal);
            return sexVal.equals("男") ? R.drawable.male : R.drawable.female;
        }
        return R.drawable.female;
    }

    public static final String parseBilliardsKinds(Context context, String kindVal)
    {
        String ballKinds = "";
        if (!TextUtils.isEmpty(kindVal))
        {
            if (kindVal.equals("1"))
            {
                ballKinds = context.getString(R.string.ball_type_1);
            } else if (kindVal.equals("2"))
            {
                ballKinds = context.getString(R.string.ball_type_2);
            } else if (kindVal.equals("3"))
            {
                ballKinds = context.getString(R.string.ball_type_3);
            } else
            {
                // TODO: 这是默认的球种类型，具体还要征求产品的要求，即在默认情况下，默认的球种是什么？？？
                ballKinds = context.getString(R.string.ball_type_3);
            }
        }

        return ballKinds;
    }
    // 解析教练的水平
    public static final String parseCoauchLevel(Context context, String levelVal)
    {
        String levelStr = "";
        if (!TextUtils.isEmpty(levelVal)) {
            if (levelVal.equals("1")) {
                levelStr = context.getString(R.string.level_base);
            } else if (levelVal.equals("2")) {
                levelStr = context.getString(R.string.level_middle);
            } else if (levelVal.equals("3")) {
                levelStr = context.getString(R.string.level_master);
            } else if (levelVal.equals("4")) {
                levelStr = context.getString(R.string.level_super_master);
            } else {
                levelStr = context.getString(R.string.level_middle);
            }
        }
        return levelStr;
    }
}
