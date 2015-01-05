package com.yueqiu.fragment.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.yueqiu.R;
import com.yueqiu.adapter.SearchMateFragmentViewPagerImgAdapter;
import com.yueqiu.adapter.SearchMateSubFragmentListAdapter;
import com.yueqiu.bean.SearchMateSubFragmentUserBean;
import com.yueqiu.fragment.search.common.SubFragmentsCommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 14/12/17.
 * <p/>
 * 球友Fragment,助教Fragment,教练Fragment,球厅Fragment,约球Fragment这五个Fragment的父类
 *
 * 对于球友Fragment,我们需要创建的内容是一个基于ViewPager的Gallery(图片是由服务器动态获取的)，这个ViewPager
 * 的地步有一些圆点用于indicator。然后在ViewPager的下面就直接就是两个Fragment了，对于这两个Fragment我们就直接
 * 使用RadioButton来进行控制了。
 *
 *
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
        mView = inflater.inflate(R.layout.search_mate_fragment_layout, null);
        mSubFragmentList = (ListView) mView.findViewById(R.id.search_sub_fragment_list);

        (sBtnDistanceFilter = (Button) mView.findViewById(R.id.btn_mate_distance)).setOnClickListener(new BtnFilterClickListener());
        (sBtnGenderFilter = (Button) mView.findViewById(R.id.btn_mate_gender)).setOnClickListener(new BtnFilterClickListener());

        Bundle args = getArguments();
        mArgs = args.getString(BILLIARD_SEARCH_TAB_NAME);

        // then, inflate the image view pager
        SubFragmentsCommonUtils.initViewPager(sContext, mView, R.id.mate_fragment_gallery_pager, R.id.mate_fragment_gallery_pager_indicator_group);

        initListViewDataSrc();
        mSubFragmentList.setAdapter(new SearchMateSubFragmentListAdapter(sContext, (ArrayList<SearchMateSubFragmentUserBean>) mUserList));

        return mView;
    }


    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    /**
     * the button on click listener for the button to filter out the
     * list item we need
     */
    private static class BtnFilterClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.btn_mate_gender:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnGenderFilter, R.layout.search_mate_subfragment_gender_popupwindow);
                    break;
                case R.id.btn_mate_distance:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnDistanceFilter, R.layout.search_mate_subfragment_distance_popupwindow);
                    break;
                default:
                    break;
            }
        }
    }

    // TODO: 用于获取球友列表中的信息
    private void retrieveMateRawInfo()
    {

    }



    private List<SearchMateSubFragmentUserBean> mUserList = new ArrayList<SearchMateSubFragmentUserBean>();
    // TODO: the following are just for testing
    // TODO: and remove all of them out with the true data we retrieved from RESTful WebService
    private void initListViewDataSrc()
    {
        int i;
        for (i = 0; i < 100; ++i)
        {
            mUserList.add(new SearchMateSubFragmentUserBean("", "月夜流沙", "男", "昌平区", "20000米以内"));
        }
    }
}


















