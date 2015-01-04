package com.yueqiu.fragment.search.common;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.yueqiu.R;
import com.yueqiu.adapter.SearchMateFragmentViewPagerImgAdapter;

/**
 * Created by scguo on 15/1/4.
 * <p/>
 * 用于实现所有的位于SearchActivity当中的子Fragment的实现的一些公有的方法的实现，
 * 在这里我们主要是实现加载ViewPager(因为这个ViewPager在每一个子Fragment当中都有)，
 * 实现PopupWindow的加载
 */
public class SubFragmentsCommonUtils
{
    private SubFragmentsCommonUtils(){}

    /**
     * @param context
     * @param anchorView  当前的popupWindow是依附于具体的哪一个View组件
     * @param layoutResId 用于显示当前的PopupWindow的具体的布局文件
     */
    public static void initPopupWindow(Context context, View anchorView, int layoutResId)
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
                switch (v.getId()) {
                    default:
                        break;
                }
                return true;

            }
        });

        popupWindow.showAsDropDown(anchorView);
    }



    private static void setImgBackground(int selectedItem)
    {
        int i;
        final int size = sPagerIndicatorImgList.length;
        for (i = 0; i < size; ++i)
        {
            if (i == selectedItem)
            {
                sPagerIndicatorImgList[i].setBackgroundResource(R.drawable.page_indicator_focused);
            } else
            {
                sPagerIndicatorImgList[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }
        }

    }

    private static ImageView[] sPagerIndicatorImgList;
    private static ImageView[] sPagerImgArr;

    private static LinearLayout sGalleryIndicatorGroup;
    private static SearchMateFragmentViewPagerImgAdapter sGalleryImgAdapter;

    private static int[] sPagerImgResArr;
    private static ViewPager sImgGalleryViewPager;

    /**
     *
     * @param context
     * @param parentView 用于加载ViewPager的父View
     * @param viewPagerId ViewPager在parentView当中的id值
     * @param galleryIndiGroupId ViewPager底部的indicator所在的布局中的id值
     */
    public static void initViewPager(Context context, View parentView, final int viewPagerId, final int galleryIndiGroupId)
    {
        // TODO: 以下仅仅是测试数据，在测试接口的时候就删除掉
        sPagerImgResArr = new int[]{R.drawable.test_pager_1, R.drawable.test_pager_2, R.drawable.test_pager_3, R.drawable.test_pager_4};

        sImgGalleryViewPager = (ViewPager) parentView.findViewById(viewPagerId);
        sGalleryIndicatorGroup = (LinearLayout) parentView.findViewById(galleryIndiGroupId);

        sPagerIndicatorImgList = new ImageView[sPagerImgResArr.length];

        final int size = sPagerIndicatorImgList.length;

        ImageView indicatorView;
        int i;
        for (i = 0; i < size; ++i)
        {
            indicatorView = new ImageView(context);
            indicatorView.setLayoutParams(new ViewGroup.LayoutParams(10, 10));

            sPagerIndicatorImgList[i] = indicatorView;
            if (i == 0)
            {
                sPagerIndicatorImgList[i].setBackgroundResource(R.drawable.page_indicator_focused);
            } else
            {
                sPagerIndicatorImgList[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            params.leftMargin = 5;
            params.rightMargin = 5;

            sGalleryIndicatorGroup.addView(indicatorView, params);
        }

        sPagerImgArr = new ImageView[size];
        int j;
        for (j = 0; j < size; ++j)
        {
            ImageView imgView = new ImageView(context);
            sPagerImgArr[j] = imgView;
            imgView.setBackgroundResource(sPagerImgResArr[j]);
        }

        sGalleryImgAdapter = new SearchMateFragmentViewPagerImgAdapter(sPagerImgArr);
        sImgGalleryViewPager.setAdapter(sGalleryImgAdapter);
        sImgGalleryViewPager.setCurrentItem(sPagerImgArr.length * 100);
        sImgGalleryViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {

            @Override
            public void onPageScrolled(int i, float v, int i2)
            {

            }

            @Override
            public void onPageSelected(int i)
            {
                setImgBackground(i % sPagerImgArr.length);

            }

            @Override
            public void onPageScrollStateChanged(int i)
            {

            }
        });
    }
}
