package com.yueqiu.fragment.search;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.yueqiu.R;
import com.yueqiu.adapter.SearchRoomSubFragmentListAdapter;
import com.yueqiu.bean.SearchRoomSubFragmentRoomBean;
import com.yueqiu.fragment.search.common.SubFragmentsCommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author scguo
 *
 * 这个是用于显示球厅的Fragment
 * 球厅的Fragment同MateFragment结构相同，但是ListView的差别很大。我们需要完全重新创建一个ListView
 * 用于显示关于每一个球厅的ListView item
 *
 *
 */
public class BilliardsSearchRoomFragment extends Fragment
{
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.search_room_fragment_layout, container, false);

        SubFragmentsCommonUtils.initViewPager(sContext, mView, R.id.room_fragment_gallery_pager, R.id.room_fragment_gallery_pager_indicator_group);

                (sBtnDistrict = (Button) mView.findViewById(R.id.btn_room_district)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnDistan = (Button) mView.findViewById(R.id.btn_room_distance)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnPrice = (Button) mView.findViewById(R.id.btn_room_price)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnApprisal = (Button) mView.findViewById(R.id.btn_room_apprisal)).setOnClickListener(new OnFilterBtnClickListener());

        mRoomListView = (ListView) mView.findViewById(R.id.search_room_subfragment_listview);
        initListStaticTestData();
        mRoomListView.setAdapter(new SearchRoomSubFragmentListAdapter(sContext, (ArrayList<SearchRoomSubFragmentRoomBean>) mRoomList));

        return mView;
    }

    private static class OnFilterBtnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.btn_room_district:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnDistrict, R.layout.search_mate_subfragment_gender_popupwindow);
                    break;
                case R.id.btn_room_distance:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnDistan, R.layout.search_mate_subfragment_gender_popupwindow);
                    break;
                case R.id.btn_room_price:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnPrice, R.layout.search_mate_subfragment_distance_popupwindow);
                    break;
                case R.id.btn_room_apprisal:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnApprisal, R.layout.search_mate_subfragment_distance_popupwindow);
                    break;
                default:
                    break;
            }
        }
    }


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
        for (i = 0; i < 100; ++i)
        {
            mRoomList.add(new SearchRoomSubFragmentRoomBean("", roomName, level, price, address, distance));
        }
    }
}
