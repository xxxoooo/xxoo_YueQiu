package com.yueqiu.fragment.nearby.common;

import android.content.Context;
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

import com.yueqiu.R;
import com.yueqiu.adapter.NearbyMateFragmentViewPagerImgAdapter;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by scguo on 15/1/4.
 * <p/>
 * 用于实现所有的位于SearchActivity当中的子Fragment的实现的一些公有的方法的实现，
 * 在这里我们主要是实现加载ViewPager(因为这个ViewPager在每一个子Fragment当中都有)，
 * 实现PopupWindow的加载
 */
public class NearbyFragmentsCommonUtils
{
    private static final String TAG = "NearbyFragmentsCommonUtils";
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


    // 以下是用于球友Fragment当中需要传输的数据的详细的key值
    public static final String KEY_BUNDLE_SEARCH_MATE_FRAGMENT = "searchMateFragment";

    // 以下是用于约球Fragment当中需要传输的数据的详细的key值
    public static final String KEY_BUNDLE_SEARCH_DATING_FRAGMENT = "searchDatingFragment";
    public static final String KEY_DATING_FRAGMENT_PHOTO = "datingPhoto";
    public static final String KEY_DATING_FRAGMENT_NAME = "datingName";
    public static final String KEY_DATING_FRAGMENT_GENDER = "datingGender";
    public static final String KEY_DATING_FRAGMENT_FOLLOWNUM = "datingFollowNum";
    public static final String KEY_DATING_PUBLISH_TIME = "datingTime";

    // 以下是用于教练Fragment当中需要传输的数据的详细的key值
    public static final String KEY_BUNDLE_SEARCH_COAUCH_FRAGMENT = "searchCoauchFragment";

    // 以下是用于助教Fragment当中需要传输的数据的详细的key值
    public static final String KEY_BUNDLE_SEARCH_ASSISTCOAUCH_FRAGMENT = "searchAssistCoauchFragment";


    private NearbyFragmentsCommonUtils()
    {
    }

    public static String getLastedTime(Context context)
    {
        return DateUtils.formatDateTime(context, System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
    }

    public static void setFragmentEmptyTextView(Context context, final PullToRefreshListView listView, final String emptyText)
    {
        TextView emptyView = new TextView(context);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        emptyView.setTextColor(context.getResources().getColor(R.color.md__defaultBackground));
        emptyView.setText(emptyText);
        listView.setEmptyView(emptyView);
    }

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

        popupWindow.showAsDropDown(anchorView);

        return popupWindow;
    }


    // TODO: 这里我们使用的是服务器端的同学开发的接口
    // TODO: 这里我们加载的是商家推荐的信息的列表，也就是显示在每一个Fragment当中的最上面的滚动的Image Gallery
    private static void retrieveRecommdedRoomInfo()
    {
        String rawResult = HttpUtil.urlClient(HttpConstants.NearbyRoomRecommendation.URL, null, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the recommendation info we get are : " + rawResult);

        if (!TextUtils.isEmpty(rawResult)) {
            try {
                JSONObject initialJsonData = new JSONObject(rawResult);
                Log.d(TAG, " the initial json data we get are : " + initialJsonData.toString());
                final int status = initialJsonData.getInt("code");
                if (status == HttpConstants.ResponseCode.NORMAL) {
                    JSONArray resultJsonArr = initialJsonData.getJSONArray("result");
                    final int count = resultJsonArr.length();
                    int i;
                    for (i = 0; i < count; ++i) {
                        JSONObject dataUnit = resultJsonArr.getJSONObject(i);
                        String photoUrl = dataUnit.getString("img_url");

                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, " exception happened in parsing the room recommendation detailed information, and the detailed reason are : " + e.toString());
            }
        }
    }

    private static ImageView[] sPagerImgArr;
    private static LinearLayout sGalleryIndicatorGroup;
    private static NearbyMateFragmentViewPagerImgAdapter sGalleryImgAdapter;
    private static int[] sPagerImgResArr;
    private static ViewPager sImgGalleryViewPager;

    /**
     * @param context
     * @param parentView         用于加载ViewPager的父View
     * @param viewPagerId        ViewPager在parentView当中的id值
     * @param galleryIndiGroupId ViewPager底部的indicator所在的布局中的id值
     */
    public static void initViewPager(final Context context, View parentView, final int viewPagerId, final int galleryIndiGroupId)
    {
        final ImageView[] sPagerIndicatorImgList;

        // TODO: 以下仅仅是测试数据，在测试接口的时候就删除掉
        // TODO: 我们通过网络请求将以下的数据获得
        sPagerImgResArr = new int[]{R.drawable.test_pager_1, R.drawable.test_pager_2, R.drawable.test_pager_3, R.drawable.test_pager_4};

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Log.d(TAG, " start retrieving the view pager data here ");
//                retrieveRecommdedRoomInfo();
            }
        }).start();

        sImgGalleryViewPager = (ViewPager) parentView.findViewById(viewPagerId);
        sGalleryIndicatorGroup = (LinearLayout) parentView.findViewById(galleryIndiGroupId);

        sPagerIndicatorImgList = new ImageView[sPagerImgResArr.length];

        final int size = sPagerIndicatorImgList.length;
        Log.d(TAG, " the size we get are : " + size);
        ImageView indicatorView;
        int i;
        for (i = 0; i < size; ++i) {
            indicatorView = new ImageView(context);
            indicatorView.setLayoutParams(new ViewGroup.LayoutParams(10, 10));

            sPagerIndicatorImgList[i] = indicatorView;
            // 用于初始化所有的indicator的初始状态
            if (i == 0) {
                sPagerIndicatorImgList[i].setBackgroundResource(R.drawable.page_indicator_focused);
            } else {
                sPagerIndicatorImgList[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            params.leftMargin = 5;
            params.rightMargin = 5;

            sGalleryIndicatorGroup.addView(indicatorView, params);
        }
        sPagerImgArr = new ImageView[size];
        int j;
        for (j = 0; j < size; ++j) {
            ImageView imgView = new ImageView(context);
            sPagerImgArr[j] = imgView;
            imgView.setBackgroundResource(sPagerImgResArr[j]);
        }

        sGalleryImgAdapter = new NearbyMateFragmentViewPagerImgAdapter(sPagerImgArr);
        sImgGalleryViewPager.setAdapter(sGalleryImgAdapter);

        sImgGalleryViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int i, float v, int i2)
            {
            }
            @Override
            public void onPageSelected(int i)
            {
                Log.d(TAG, " the current page index are : " + i + ", and the selected index are : " + i % sPagerImgArr.length);

                int j;
                for (j = 0; j < size; ++j) {
                    if (j == (i % size)) {
                        sPagerIndicatorImgList[j].setBackgroundResource(R.drawable.page_indicator_focused);
                    } else {
                        sPagerIndicatorImgList[j].setBackgroundResource(R.drawable.page_indicator_unfocused);
                    }
                }
            }
            @Override
            public void onPageScrollStateChanged(int i)
            {

            }
        });
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
        if (!TextUtils.isEmpty(sexVal)) {
            Log.d(TAG, " the sex val we get are :" + sexVal);
            return sexVal.equals("男") ? R.drawable.male : R.drawable.female;
        }
        return R.drawable.female;
    }

    public static final String parseBilliardsKinds(Context context, String kindVal)
    {
        String ballKinds = "";
        if (!TextUtils.isEmpty(kindVal)) {
            if (kindVal.equals("1")) {
                ballKinds = context.getString(R.string.ball_type_1);
            } else if (kindVal.equals("2")) {
                ballKinds = context.getString(R.string.ball_type_2);
            } else if (kindVal.equals("3")) {
                ballKinds = context.getString(R.string.ball_type_3);
            } else {
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
