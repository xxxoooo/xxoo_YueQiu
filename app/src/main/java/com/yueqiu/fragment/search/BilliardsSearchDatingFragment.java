package com.yueqiu.fragment.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.yueqiu.R;
import com.yueqiu.activity.searchmenu.nearby.SearchBilliardsDatingActivity;
import com.yueqiu.adapter.SearchDatingSubFragmentListAdapter;
import com.yueqiu.bean.SearchDatingSubFragmentDatingBean;
import com.yueqiu.fragment.search.common.SubFragmentsCommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 14/12/30.
 * <p/>
 * 用于SearchActivity当中的约球子Fragment的实现
 */
public class BilliardsSearchDatingFragment extends Fragment
{
    private static final String TAG = "BilliardsSearchDatingFragment";

    private static Context sContext;

    public BilliardsSearchDatingFragment()
    {
    }

    public static BilliardsSearchDatingFragment newInstance(Context context, String params)
    {
        sContext = context;
        BilliardsSearchDatingFragment instance = new BilliardsSearchDatingFragment();

        Bundle args = new Bundle();
        args.putString(KEY_DATING_FRAGMENT, params);
        instance.setArguments(args);

        return instance;
    }


    public static final String KEY_DATING_FRAGMENT = "BilliardsSearchDatingFragment";

    private View mView;
    private static Button sBtnDistan, sBtnPublishDate;
    private ListView mDatingListView;
    private List<SearchDatingSubFragmentDatingBean> mDatingList = new ArrayList<SearchDatingSubFragmentDatingBean>();

    // TODO: mArgs是我们在初始化Fragment时需要接受来自初始化这个Fragment所传递的参数的容器，
    // TODO: 只不过我们现在没有用到，但是这个参数是我们更好的封装Fragment的基础，不要忽略
    private Bundle mArgs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.search_dating_fragment_layout, null);

        SubFragmentsCommonUtils.initViewPager(sContext, mView, R.id.dating_frament_gallery_pager, R.id.dating_fragment_gallery_pager_indicator_group);

        (sBtnDistan = (Button) mView.findViewById(R.id.btn_dating_distance)).setOnClickListener(new OnFilterBtnClickListener());
        (sBtnPublishDate = (Button) mView.findViewById(R.id.btn_dating_publichdate)).setOnClickListener(new OnFilterBtnClickListener());

        Bundle args = getArguments();
        mArgs = args;

        mDatingListView = (ListView) mView.findViewById(R.id.search_dating_subfragment_list);
        initTestData();
        mDatingListView.setAdapter(new SearchDatingSubFragmentListAdapter(sContext, (ArrayList<SearchDatingSubFragmentDatingBean>) mDatingList));

        mDatingListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                SearchDatingSubFragmentDatingBean bean = mDatingList.get(position);
                Bundle args = new Bundle();
                args.putString(SubFragmentsCommonUtils.KEY_DATING_FRAGMENT_PHOTO, bean.getUserPhoto());


                Intent intent = new Intent(sContext, SearchBilliardsDatingActivity.class);
                intent.putExtra(SubFragmentsCommonUtils.KEY_BUNDLE_SEARCH_DATING_FRAGMENT, args);
                sContext.startActivity(intent);
            }
        });

        return mView;
    }

    private static class OnFilterBtnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId()) {
                case R.id.btn_dating_distance:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnDistan, R.layout.search_mate_subfragment_distance_popupwindow);
                    break;
                case R.id.btn_dating_publichdate:
                    SubFragmentsCommonUtils.initPopupWindow(sContext, sBtnPublishDate, R.layout.search_mate_subfragment_gender_popupwindow);
                    break;
                default:
                    break;

            }
        }
    }


    // TODO: 以下就是获取约球信息列表的网络请求处理过程
    // TODO: 这个方法获取到是原始的json数据，我们需要转换成Java bean列表
    private String retrieveDatingInfo()
    {
        return "";
    }


    // TODO: 以下都是测试数据,在测试接口的时候将他们删除掉
    private void initTestData()
    {
        int i;
        for (i = 0; i < 200; ++i) {
            mDatingList.add(new SearchDatingSubFragmentDatingBean("", "月夜流水", "第N届斯诺克大力神杯就要开始，一起参加啊！", "230米以内"));
        }
    }


}



















































































































































































































