package com.yueqiu.fragment.nearby.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.yueqiu.R;
import com.yueqiu.adapter.NearbyPopupBaseAdapter;
import com.yueqiu.fragment.nearby.BilliardsNearbyAssistCoauchFragment;
import com.yueqiu.fragment.nearby.BilliardsNearbyCoachFragment;
import com.yueqiu.fragment.nearby.BilliardsNearbyDatingFragment;
import com.yueqiu.fragment.nearby.BilliardsNearbyMateFragment;
import com.yueqiu.fragment.nearby.BilliardsNearbyRoomFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils.getFilterPopupWindow;

/**
 * Created by wangyun on 15/1/24.
 */
public class NearbyPopBasicClickListener implements View.OnClickListener, NearbyFragmentsCommonUtils.ControlPopupWindowCallback
{
    private static final String TAG = "NearbyPopBasicClickListener";
    private static final String TAG_1 = "mate_fragemnt_test";
    private Context mContext;
    private LayoutInflater mInflater;
    private View mPopupBaseView;
    private PopupWindow mPopupWindow;
    private Button mPopupTitleView;
    private ListView mPopupListView;
    private Handler mUIEventsHandler;
    private NearbyParamsPreference mParamsPreference;

    public NearbyPopBasicClickListener(Context context, Handler handler, NearbyParamsPreference preference)
    {
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mPopupBaseView = mInflater.inflate(R.layout.popupwindow_basic_layout, null);
        this.mPopupTitleView = (Button) mPopupBaseView.findViewById(R.id.popup_base_button);
        this.mPopupListView = (ListView) mPopupBaseView.findViewById(R.id.popup_base_listview);

        // 注意，我们只需要对Android 5.0需要设置这个额外的参数，5.0之前是不需要添加这个额外的参数的
        // 对于Android 5.0，我们需要对这个ListView设置marginBottom参数，我们目前的值是@dimen/near_activity_subfragment_margin_botton
        LinearLayout.LayoutParams listViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        listViewParams.bottomMargin = (int) context.getResources().getDimension(R.dimen.nearby_subfragment_listview_margin_bottom);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            this.mPopupListView.setLayoutParams(listViewParams);
        }

        this.mUIEventsHandler = handler;
        this.mParamsPreference = preference;
    }

    /**
     * 我们需要注意的是“所有的筛选过程都要是重新从网络上请求数据”来完成的，否则我们将不能完成筛选的过程
     * 因为我们如果采用先将所有的数据保存到本地，然后进行筛选，我们是无法进行上拉刷新或者下拉刷新的
     * 过程的，也就是说我们的List将会变成是死的，而不是活的。
     * 所以我们只能采用全部的筛选过程都是直接从网络上获取
     *
     * @param view
     */
    @Override
    public void onClick(View view)
    {
        if (mContext == null)
        {
            return;
        }
        switch (view.getId()) {
            //assistance
            case R.id.btn_assistcoauch_distance:
                final String[] disStrList = {
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_500_str),
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_1000_str),
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_2000_str),
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_5000_str)
                };
                mPopupTitleView.setText(R.string.search_mate_popupmenu_item_filter_str);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(disStrList)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        final int posVal = position;
                        String rawDistance = disStrList[posVal];
                        final int len = rawDistance.length();
                        String distance = rawDistance.substring(0, len - 3);
                        mParamsPreference.setAScouchRange(mContext, distance);
                        mUIEventsHandler.obtainMessage(BilliardsNearbyAssistCoauchFragment.RETRIEVE_INFO_WITH_DISTANCE_FILTERED, distance).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });

                break;
            case R.id.btn_assistcoauch_cost:
                String[] priceArr = {
                        mContext.getResources().getString(R.string.search_room_price_popupwindow_lowtohigh), // TODO: ??????从到到低，对应的数字值是????????????
                        mContext.getResources().getString(R.string.search_room_price_popupwindow_lowtohigh) // TODO: ??????从到到低，对应的数字值是????????????
                };

                mPopupTitleView.setText(R.string.search_room_price_popupwindow_no_filter);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(priceArr)));

                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        final int posVal = position;
                        final String priceStr = String.valueOf(posVal + 1);
                        mParamsPreference.setAScouchPrice(mContext, priceStr);
                        mUIEventsHandler.obtainMessage(BilliardsNearbyAssistCoauchFragment.RETRIEVE_INFO_WITH_PRICE_FILTERED, priceStr).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });

                break;
            case R.id.btn_assistcoauch_kinds:
                String[] kindsArr = {
                        mContext.getResources().getString(R.string.search_coauch_filter_kinds_desk), // 我们需要传递的值为1,中式球对应的值为“1”
                        mContext.getResources().getString(R.string.search_coauch_filter_kinds_sinuoke), // 我们需要传递的值为2
                        mContext.getResources().getString(R.string.search_coauch_filter_kinds_jiuqiu) // 我们需要传递的值为3
                };
                mPopupTitleView.setText(R.string.search_coauch_filter_kinds_no_filter);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(kindsArr)));

                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);
                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        final int posVal = position;
                        final String clazzStr = String.valueOf(posVal + 1);
                        mParamsPreference.setAScouchLevel(mContext, clazzStr);
                        mUIEventsHandler.obtainMessage(BilliardsNearbyAssistCoauchFragment.RETREIVE_INFO_WITH_KINDS_FILTERED, clazzStr).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });

                break;
            case R.id.btn_assistcoauch_level:

                String[] levelsArr = {
                        mContext.getResources().getString(R.string.level_base), // 我们需要传递到服务器端的准确值是“1”
                        mContext.getResources().getString(R.string.level_middle), // 我们需要传递到服务器端的准确值是“2”
                        mContext.getResources().getString(R.string.level_master), // 我们需要传递到服务器端的准确值是“3”
                        mContext.getResources().getString(R.string.level_super_master) // TODO: 我们需要传递到服务器端的准确值是“4”,服务器端还没有提供这个值，但是传递给我们的数据出现了，所以我们暂时先这个定义了
                };

                mPopupTitleView.setText(R.string.search_assistcoauch_filter_level_no_filter);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(levelsArr)));

                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);

                mPopupWindow.update();

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        final int posVal = position;
                        final String levelStr = String.valueOf(posVal + 1);
                        mParamsPreference.setAScouchLevel(mContext, levelStr);

                        mUIEventsHandler.obtainMessage(BilliardsNearbyAssistCoauchFragment.RETRIEVE_INFO_WITH_LEVEL_FILTERED, levelStr).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });
                break;
            //mate
            case R.id.btn_mate_gender:
                final String[] genderStrList = {
                        mContext.getResources().getString(R.string.man), // 对于男，我们不是直接传递“男”，而是传递“1”，可以减少我们转换的开销
                        mContext.getResources().getString(R.string.woman) // 对于这个值，我们直接传递代表“女”的数字值“2”，减少我们转换的开销
                };

                mPopupTitleView.setText(R.string.gender);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(genderStrList)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        final int posVal = position;
                        final String gender = String.valueOf(posVal + 1);
                        mParamsPreference.setMateGender(mContext, gender);
                        mUIEventsHandler.obtainMessage(BilliardsNearbyMateFragment.START_RETRIEVE_DATA_WITH_GENDER_FILTER, gender).sendToTarget();
                        mPopupWindow.dismiss();

                    }
                });
                break;
            case R.id.btn_mate_distance:
                final String[] mate_distance = {
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_500_str), // 500米以内
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_1000_str),
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_2000_str),
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_5000_str)
                };

                mPopupTitleView.setText(R.string.search_mate_popupmenu_item_filter_str);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(mate_distance)));

                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);
                Log.d(TAG_1, " the popupWindow are --> " + (mPopupWindow != null) + ", and the base view are : " + (mPopupBaseView != null));
                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        Log.d(TAG_1, " the current selected position are : " + position +", and the list size are : " + mate_distance.length);
                        // 我们要将我们传递的“500米以内”截取成“500”
                        final int posVal = position;
                        String rawDistanceStr = mate_distance[posVal];
                        final int len = rawDistanceStr.length();
                        String distanceVal = rawDistanceStr.substring(0, len - 3);
                        mParamsPreference.setMateRange(mContext, distanceVal);
                        mUIEventsHandler.obtainMessage(BilliardsNearbyMateFragment.START_RETRIEVE_DATA_WITH_RANGE_FILTER, distanceVal).sendToTarget();
                        mPopupWindow.dismiss();
                        Log.d(TAG_1, " we are almost come to the end of the popupWindow processing ");
                    }
                });
                break;
            //coach
            case R.id.btn_coauch_ability:
                final String[] levelStrList = {
                        // TODO: 现在服务端还没有定义每一个教练所对应的级别的具体的称号
                        mContext.getResources().getString(R.string.search_coauch_filter_level_guojiadui),
                        // TODO: 我们这里也是暂时命名 1
                        mContext.getResources().getString(R.string.search_coauch_filter_level_in_guojiadui), // TODO: 2
                        mContext.getResources().getString(R.string.search_coauch_filter_level_pre_guojiadui), // TODO: 3 注意这里的值还有待服务器端的确定
                };

                mPopupTitleView.setText(R.string.search_coauch_filter_level_no_filter);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(levelStrList)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        final int posVal = position;
                        String levelStr = levelStrList[posVal];
                        mUIEventsHandler.obtainMessage(BilliardsNearbyCoachFragment.RETRIEVE_COAUCH_WITH_LEVEL_FILTERED, levelStr).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });

                break;
            case R.id.btn_coauch_kinds:
                final String[] kindsStrList = {
                        mContext.getResources().getString(R.string.search_coauch_filter_kinds_desk), // 中式球对应的值为1
                        mContext.getResources().getString(R.string.search_coauch_filter_kinds_sinuoke), // 斯诺克对应的参数值为2
                        mContext.getResources().getString(R.string.search_coauch_filter_kinds_jiuqiu) // 九球对应的参数值为3
                };
                mPopupTitleView.setText(R.string.search_coauch_filter_kinds_no_filter);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(kindsStrList)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        final int posVal = position;
                        String clazzStr = String.valueOf(posVal + 1);
                        mParamsPreference.setCouchClazz(mContext, clazzStr);
                        mUIEventsHandler.obtainMessage(BilliardsNearbyCoachFragment.RETRIEVE_COAUCH_WITH_CLASS_FILTERED, clazzStr).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });

                break;
            //dating
            case R.id.btn_dating_distance:
                final String[] dating_distance = {
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_500_str),
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_1000_str),
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_2000_str),
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_5000_str)
                };
                mPopupTitleView.setText(R.string.search_mate_popupmenu_item_filter_str);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(dating_distance)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);
                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        final int posVal = position;
                        String rawRangeStr = dating_distance[posVal];
                        int len = rawRangeStr.length();
                        String range = rawRangeStr.substring(0, len - 3);
                        mParamsPreference.setDatingRange(mContext, range);
                        mUIEventsHandler.obtainMessage(BilliardsNearbyDatingFragment.RETRIEVE_DATA_WITH_RANGE_FILTERED, range).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });

                break;
            case R.id.btn_dating_publichdate:

                final String[] dateStrList = {
                        mContext.getResources().getString(R.string.search_dating_popupwindow_one),
                        mContext.getResources().getString(R.string.search_dating_popupwindow_two),
                        mContext.getResources().getString(R.string.search_dating_popupwindow_three),
                        mContext.getResources().getString(R.string.search_dating_popupwindow_four),
                        mContext.getResources().getString(R.string.search_dating_popupwindow_five),
                        mContext.getResources().getString(R.string.search_dating_popupwindow_six),
                        mContext.getResources().getString(R.string.search_dating_popupwindow_seven),
                        mContext.getResources().getString(R.string.billiard_other)
                };

                mPopupTitleView.setText(R.string.search_dating_popupwindow_no_filter);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(dateStrList)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        final int posVal = position;
                        final int timeInterval = posVal + 1;
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        calendar.setTime(new Date());
                        // 因为我们需要的是以前发布的数据，所以我们这里是将我们得到的值进行减值操作，即直接加一个负值就可以完成操作了
                        calendar.add(Calendar.DAY_OF_MONTH, -timeInterval);
                        String specifiedDate = formatter.format(calendar.getTime());
                        mParamsPreference.setDatingPublishedDate(mContext, specifiedDate);

                        mUIEventsHandler.obtainMessage(BilliardsNearbyDatingFragment.RETRIEVE_DATA_WITH_DATE_FILTERED, specifiedDate).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });
                break;
            //room
            case R.id.btn_room_district:

                // TODO: 我们在这里需要注意的是(仅仅是针对球厅Fragment的请求处理方式有一些特别),由于球厅Fragment的数据请求是来自于
                // TODO: 大众点评的接口，所以初始的请求并不是直接的把所有的数据都可以请求到，然后进行处理，而是在原来的基础上进行SQL检索
                // TODO: 大众点评的接口是每次进行筛选的时候都是需要重新进行数据的检索
                final String[] regionStrList = {
                        mContext.getResources().getString(R.string.search_room_popupwindow_region_changping),
                        mContext.getResources().getString(R.string.search_room_popupwindow_region_chaoyang),
                        mContext.getResources().getString(R.string.search_room_popupwindow_region_daxing),
                        mContext.getResources().getString(R.string.search_room_popupwindow_region_dongcheng),
                        mContext.getResources().getString(R.string.search_room_popupwindow_region_xicheng),
                        mContext.getResources().getString(R.string.search_room_popupwindow_region_haidian),
                        mContext.getResources().getString(R.string.search_room_popupwindow_region_feitaiqu),
                        mContext.getResources().getString(R.string.search_room_popupwindow_region_shijingshan),
                        mContext.getResources().getString(R.string.search_room_popupwindow_region_tongzhou)
                };

                mPopupTitleView.setText(R.string.search_room_popupwindow_do_notfilter);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(regionStrList)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        final int posVal = position;
                        String regionStr = regionStrList[posVal];
                        mParamsPreference.setRoomRegion(mContext, regionStr);
                        Log.d(TAG, " inside the popupBase click listener, and the params we transfer are : " + regionStr);
                        mUIEventsHandler.obtainMessage(BilliardsNearbyRoomFragment.REQUEST_ROOM_INFO_REGION_FILTERED, regionStr).sendToTarget();
                        // 当我们选择了一个条目之后，就需要将popupWindow dismiss掉
                        mPopupWindow.dismiss();
                    }
                });

                break;
            case R.id.btn_room_distance:

                final String[] room_distance = {
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_500_str),
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_1000_str),
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_2000_str),
                        mContext.getResources().getString(R.string.search_mate_popupmenu_item_5000_str)
                };

                mPopupTitleView.setText(R.string.search_mate_popupmenu_item_filter_str);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(room_distance)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        final int posVal = position;
                        String rawRangeStr = room_distance[posVal];
                        int len = rawRangeStr.length();
                        String range = rawRangeStr.substring(0, len - 3);
                        mParamsPreference.setRoomRange(mContext, range);
                        mUIEventsHandler.obtainMessage(BilliardsNearbyRoomFragment.REQUEST_ROOM_INFO_RANGE_FILTERED, range).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });


                break;
            case R.id.btn_room_price:
                final String[] priceList = {
                        mContext.getResources().getString(R.string.search_room_price_popupwindow_hightolow), // 对应于大众点评可以接受的sort值为9
                        mContext.getResources().getString(R.string.search_room_price_popupwindow_lowtohigh) // 对应于大众点评可以接受的sort值为8
                };
                mPopupTitleView.setText(R.string.search_room_price_popupwindow_no_filter);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(priceList)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        int priceSort = 8;
                        if (position == 0)
                        {
                            priceSort = 9;
                        }
                        String priceStr = String.valueOf(priceSort);
                        mParamsPreference.setRoomPrice(mContext, priceStr);
                        mUIEventsHandler.obtainMessage(BilliardsNearbyRoomFragment.REQUEST_ROOM_INFO_PRICE_FILTERED, priceStr).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });

                break;
            case R.id.btn_room_apprisal:

                final String[] apprisalArr = {
                        mContext.getResources().getString(R.string.search_room_filter_list_star), // 星级高2
                        mContext.getResources().getString(R.string.search_room_filter_list_product), // 产品3
                        mContext.getResources().getString(R.string.search_room_filter_list_environment), // 环境4
                        mContext.getResources().getString(R.string.search_room_filter_list_service), // 服务5
                        mContext.getResources().getString(R.string.search_room_filter_list_comment), //  点评数量6
                };
                mPopupTitleView.setText(R.string.search_room_filter_no_filter);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(apprisalArr)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        final int posVal = position;
                        int apprisalValSort = posVal + 2;
                        String apprisalStr = String.valueOf(apprisalValSort);
                        mParamsPreference.setRoomApprisal(mContext, apprisalStr);
                        Log.d(TAG, " inside the FilterOnClickListener --> the apprisal sort value we get from user are : " + apprisalValSort);
                        mUIEventsHandler.obtainMessage(BilliardsNearbyRoomFragment.REQUEST_ROOM_INFO_APPRISAL_FILTERED, apprisalStr).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });
                break;
        }
    }

    @Override
    public void closePopupWindow()
    {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            Log.d(TAG, " inside the popupWindow onclick listener --> and we dismiss the popupWindow ");
            mPopupWindow.dismiss();
        }
    }
}
