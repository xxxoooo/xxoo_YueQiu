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
    public BilliardsSearchMateFragment(Context context)
    {
        sContext = context;
    }

    private static final String KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT = "keyBilliardsSearchParentFragment";

    public static BilliardsSearchMateFragment newInstance(Context context, String params)
    {
        BilliardsSearchMateFragment fragment = new BilliardsSearchMateFragment(context);

        Bundle args = new Bundle();
        args.putString(KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT, params);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.mate_fragment_layout, null);
        mSubFragmentList = (ListView) mView.findViewById(R.id.search_sub_fragment_list);

        (sBtnDistanceFilter = (Button) mView.findViewById(R.id.btn_mate_distance)).setOnClickListener(new BtnFilterClickListener());
        (sBtnGenderFilter = (Button) mView.findViewById(R.id.btn_mate_gender)).setOnClickListener(new BtnFilterClickListener());

        Bundle args = getArguments();
        mArgs = args.getString(BILLIARD_SEARCH_TAB_NAME);

        // then, inflate the image view pager
        initViewPager();

        initListViewDataSrc();
        mSubFragmentList.setAdapter(new SearchMateSubFragmentListAdapter(sContext, (ArrayList<SearchMateSubFragmentUserBean>) mUserList));

        return mView;
    }

    private ViewPager mImgGalleryViewPager;
    private int[] mPagerImgResArr;
    private ImageView[] mPagerImgViewArr;

    private ImageView[] mPagerIndicatorImgList;
    private SearchMateFragmentViewPagerImgAdapter mGalleryImgAdapter;
    private LinearLayout mGalleryIndicatorGroup;

    private void initViewPager()
    {
        // TODO: the following are just for testing data source, and we should use the VolleyNetworkImageView to retrieve such images
        mPagerImgResArr = new int[] {R.drawable.test_pager_1, R.drawable.test_pager_2, R.drawable.test_pager_3, R.drawable.test_pager_4};

        mImgGalleryViewPager = (ViewPager) mView.findViewById(R.id.mate_fragment_gallery_pager);
        mGalleryIndicatorGroup = (LinearLayout) mView.findViewById(R.id.mate_fragment_gallery_pager_indicator_group);

        // init the viewpager indicator group
        mPagerIndicatorImgList = new ImageView[mPagerImgResArr.length];
        int i;
        final int size = mPagerIndicatorImgList.length;
        ImageView indicatorView;
        for (i = 0; i < size; ++i)
        {
            indicatorView = new ImageView(sContext);
            indicatorView.setLayoutParams(new ViewGroup.LayoutParams(10, 10));

            mPagerIndicatorImgList[i] = indicatorView;
            if (i == 0)
            {
                mPagerIndicatorImgList[i].setBackgroundResource(R.drawable.page_indicator_focused);
            } else
            {
                mPagerIndicatorImgList[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            params.leftMargin = 5;
            params.rightMargin = 5;
            mGalleryIndicatorGroup.addView(indicatorView, params);
        }

        // load the image into the ImageView array
        mPagerImgViewArr = new ImageView[size];
        final int imgSize = mPagerImgResArr.length;
        int j;
        for (j = 0; j < imgSize; ++j)
        {
            ImageView imgView = new ImageView(sContext);
            mPagerImgViewArr[j] = imgView;
            imgView.setBackgroundResource(mPagerImgResArr[j]);
        }


        mGalleryImgAdapter = new SearchMateFragmentViewPagerImgAdapter(mPagerImgViewArr);

        mImgGalleryViewPager.setAdapter(mGalleryImgAdapter);

        mImgGalleryViewPager.setCurrentItem(mPagerImgViewArr.length * 100); // this is the default displaying page

        mImgGalleryViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float v, int i2)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                // change the background of the indicator that below the gallery image view
                setImageBackground(position % mPagerImgViewArr.length);

            }

            @Override
            public void onPageScrollStateChanged(int i)
            {

            }
        });
    }

    /**
     * method use to change the image that corresponds to the gallery image
     * @param selectedItem
     */
    private void setImageBackground(int selectedItem)
    {
        int i;
        final int size = mPagerIndicatorImgList.length;
        for (i = 0; i < size; ++i)
        {
            if (i == selectedItem)
            {
                mPagerIndicatorImgList[i].setBackgroundResource(R.drawable.page_indicator_focused);
            } else
            {
                mPagerIndicatorImgList[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }
        }
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
                    initPopupWindow(sContext, sBtnGenderFilter, R.layout.search_mate_subfragment_gender_popupwindow);
                    break;
                case R.id.btn_mate_distance:
                    initPopupWindow(sContext, sBtnDistanceFilter, R.layout.search_mate_subfragment_distance_popupwindow);
                    break;
                default:
                    break;
            }
        }
    }
    // this is for the popupWindow that use to displayed that correspond to the filter button
    private static void initPopupWindow(Context context, View anchorView, int layoutResId)
    {
        final int popupWidth = LinearLayout.LayoutParams.MATCH_PARENT;
        final int popupHeight = LinearLayout.LayoutParams.WRAP_CONTENT;

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupWindowLayout = layoutInflater.inflate(layoutResId, null);

        final PopupWindow popupWindow = new PopupWindow(context);
        popupWindow.setContentView(popupWindowLayout);
        popupWindow.setWidth(popupWidth);
        popupWindow.setHeight(popupHeight);
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.popup_window_bg));

        popupWindow.setTouchInterceptor(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (v.getId())
                {
                    default:
                        break;
                }
                return true;

            }
        });

        popupWindow.showAsDropDown(anchorView);
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


















