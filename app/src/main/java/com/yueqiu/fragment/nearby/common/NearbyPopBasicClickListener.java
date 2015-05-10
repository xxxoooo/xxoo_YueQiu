package com.yueqiu.fragment.nearby.common;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

import java.util.Arrays;

import static com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils.getFilterPopupWindow;

/**
 * Created by wangyun on 15/1/24.
 */
public class NearbyPopBasicClickListener implements View.OnClickListener, NearbyFragmentsCommonUtils.ControlPopupWindowCallback{
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

    public NearbyPopBasicClickListener(Context context, Handler handler, NearbyParamsPreference preference){
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

    @Override
    public void onClick(View view){
        if (mContext == null){
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

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        final int posVal = position;
                        String rawDistance = disStrList[posVal];
                        final int len = rawDistance.length();
                        String distance = rawDistance.substring(0, len - 3);
                        mParamsPreference.setAScouchRange(mContext, distance);
                        mUIEventsHandler.obtainMessage(
                                BilliardsNearbyAssistCoauchFragment.GET_LOCATION,
                                BilliardsNearbyAssistCoauchFragment.RETRIEVE_INFO_WITH_DISTANCE_FILTERED,
                                0,
                                distance).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });

                break;
            case R.id.btn_assistcoauch_cost:
                String[] priceArr = {
                        mContext.getResources().getString(R.string.search_room_price_popupwindow_lowtohigh),
                        mContext.getResources().getString(R.string.search_room_price_popupwindow_hightolow)
                };
                mPopupTitleView.setText(R.string.search_room_price_popupwindow_no_filter);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(priceArr)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);
                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        String posVal = String.valueOf(position + 1);
//                        final String priceStr = String.valueOf(posVal + 1);
                        mParamsPreference.setAScouchPrice(mContext, posVal);
                        mUIEventsHandler.obtainMessage(
                                BilliardsNearbyAssistCoauchFragment.GET_LOCATION,
                                BilliardsNearbyAssistCoauchFragment.RETRIEVE_INFO_WITH_PRICE_FILTERED,
                                0,
                                posVal).sendToTarget();
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
                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        String posVal = String.valueOf(position + 1);
//                        final String clazzStr = String.valueOf(posVal + 1);
                        mParamsPreference.setAScouchLevel(mContext, posVal);
                        mUIEventsHandler.obtainMessage(
                                BilliardsNearbyAssistCoauchFragment.GET_LOCATION,
                                BilliardsNearbyAssistCoauchFragment.RETREIVE_INFO_WITH_KINDS_FILTERED,
                                0,
                                posVal).sendToTarget();

                        mPopupWindow.dismiss();
                    }
                });

                break;
            case R.id.btn_assistcoauch_level:

                String[] levelsArr = {
                        mContext.getResources().getString(R.string.level_base), // 我们需要传递到服务器端的准确值是“1”
                        mContext.getResources().getString(R.string.level_middle), // 我们需要传递到服务器端的准确值是“2”
                        mContext.getResources().getString(R.string.level_master), // 我们需要传递到服务器端的准确值是“3”
//                        mContext.getResources().getString(R.string.level_super_master)
                };

                mPopupTitleView.setText(R.string.search_assistcoauch_filter_level_no_filter);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(levelsArr)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);
                mPopupWindow.update();
                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        String posVal = String.valueOf(position + 1);
//                        final String levelStr = String.valueOf(posVal + 1);
                        mParamsPreference.setAScouchLevel(mContext, posVal);
                        mUIEventsHandler.obtainMessage(
                                BilliardsNearbyAssistCoauchFragment.GET_LOCATION,
                                BilliardsNearbyAssistCoauchFragment.RETRIEVE_INFO_WITH_LEVEL_FILTERED,
                                0,
                                posVal).sendToTarget();
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
                        mUIEventsHandler.obtainMessage(
                                BilliardsNearbyMateFragment.GET_LOCATION,
                                BilliardsNearbyMateFragment.START_RETRIEVE_DATA_WITH_GENDER_FILTER,
                                0,
                                gender).sendToTarget();

//                        mUIEventsHandler.obtainMessage(BilliardsNearbyMateFragment.START_RETRIEVE_DATA_WITH_GENDER_FILTER, gender).sendToTarget();
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
                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        Log.d(TAG_1, " the current selected position are : " + position +", and the list size are : " + mate_distance.length);
                        // 我们要将我们传递的“500米以内”截取成“500”
                        final int posVal = position;
                        String rawDistanceStr = mate_distance[posVal];
                        final int len = rawDistanceStr.length();
                        String distanceVal = rawDistanceStr.substring(0, len - 3);
                        mParamsPreference.setMateRange(mContext, distanceVal);
                        mUIEventsHandler.obtainMessage(
                                BilliardsNearbyMateFragment.GET_LOCATION,
                                BilliardsNearbyMateFragment.START_RETRIEVE_DATA_WITH_RANGE_FILTER,
                                0,
                                distanceVal).sendToTarget();
                        mPopupWindow.dismiss();
                        Log.d(TAG_1, " we are almost come to the end of the popupWindow processing ");
                    }
                });
                break;
            //coach
            case R.id.btn_coauch_ability:
                final String[] levelStrList = {
                    mContext.getString(R.string.zizhi_country_team_member),
                    mContext.getString(R.string.zizhi_profession_memeber),
                    mContext.getString(R.string.zizhi_coach),
                    mContext.getString(R.string.search_dating_popupwindow_other)
                };

                mPopupTitleView.setText(R.string.search_coauch_filter_level_no_filter);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(levelStrList)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        String posVal = String.valueOf(position + 1);
                        mParamsPreference.setCouchLevel(mContext,posVal);
                        mUIEventsHandler.obtainMessage(
                                BilliardsNearbyCoachFragment.GET_LOCATION,
                                BilliardsNearbyCoachFragment.RETRIEVE_COAUCH_WITH_LEVEL_FILTERED,
                                0,
                                posVal).sendToTarget();
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

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final int posVal = position;
                        String clazzStr = String.valueOf(posVal + 1);
                        mParamsPreference.setCouchClazz(mContext, clazzStr);
                        mUIEventsHandler.obtainMessage(
                                BilliardsNearbyCoachFragment.GET_LOCATION,
                                BilliardsNearbyCoachFragment.RETRIEVE_COAUCH_WITH_CLASS_FILTERED,
                                0,
                                clazzStr).sendToTarget();
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
                        mUIEventsHandler.obtainMessage(BilliardsNearbyDatingFragment.GET_LOCATION,
                                BilliardsNearbyDatingFragment.RETRIEVE_DATA_WITH_RANGE_FILTERED,
                                0,
                                range
                                ).sendToTarget();
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

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                       String posVal = String.valueOf(position + 1);
                        mParamsPreference.setDatingPublishedDate(mContext, posVal);

                        mUIEventsHandler.obtainMessage(
                                BilliardsNearbyDatingFragment.GET_LOCATION,
                                BilliardsNearbyDatingFragment.RETRIEVE_DATA_WITH_DATE_FILTERED,
                                0,
                                posVal).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });
                break;
            //room
            case R.id.btn_room_district:
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

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        String regionStr = regionStrList[position];
                        mParamsPreference.setRoomRegion(mContext, regionStr);
                        mUIEventsHandler.obtainMessage(
                                BilliardsNearbyRoomFragment.GET_LOCATION,
                                BilliardsNearbyRoomFragment.REQUEST_ROOM_INFO_REGION_FILTERED,
                                0,
                                regionStr).sendToTarget();
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

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        String rawRangeStr = room_distance[position];
                        int len = rawRangeStr.length();
                        String range = rawRangeStr.substring(0, len - 3);
                        mParamsPreference.setRoomRange(mContext, range);
                        mUIEventsHandler.obtainMessage(
                                BilliardsNearbyRoomFragment.GET_LOCATION,
                                BilliardsNearbyRoomFragment.REQUEST_ROOM_INFO_RANGE_FILTERED,
                                0,
                                range).sendToTarget();
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

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        String priceStr = String.valueOf(position + 1);
                        mParamsPreference.setRoomPrice(mContext, priceStr);
                        mUIEventsHandler.obtainMessage(
                                BilliardsNearbyRoomFragment.GET_LOCATION,
                                BilliardsNearbyRoomFragment.REQUEST_ROOM_INFO_PRICE_FILTERED,
                                0,
                                priceStr).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });

                break;
            case R.id.btn_room_apprisal:

                final String[] apprisalArr = {
                        mContext.getResources().getString(R.string.search_room_filter_list_star), // 星级高1
                        mContext.getResources().getString(R.string.search_room_filter_list_product), // 产品2
                        mContext.getResources().getString(R.string.search_room_filter_list_environment), // 环境3
                        mContext.getResources().getString(R.string.search_room_filter_list_service), // 服务4
                        mContext.getResources().getString(R.string.search_room_filter_list_comment), //  点评数量5
                };
                mPopupTitleView.setText(R.string.search_room_filter_no_filter);
                mPopupListView.setAdapter(new NearbyPopupBaseAdapter(mContext, Arrays.asList(apprisalArr)));
                mPopupWindow = getFilterPopupWindow(mContext, view, mPopupBaseView);

                mPopupListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        String evaluate = String.valueOf(position + 1);
                        mParamsPreference.setRoomApprisal(mContext, evaluate);
                        mParamsPreference.setRoomRange(mContext,"");
                        mUIEventsHandler.obtainMessage(
                                BilliardsNearbyRoomFragment.GET_LOCATION,
                                BilliardsNearbyRoomFragment.REQUEST_ROOM_INFO_APPRISAL_FILTERED,
                                0,
                                evaluate).sendToTarget();
                        mPopupWindow.dismiss();
                    }
                });
                break;
        }
    }

    @Override
    public void closePopupWindow(){
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            Log.d(TAG, " inside the popupWindow onclick listener --> and we dismiss the popupWindow ");
            mPopupWindow.dismiss();
        }
    }
}
