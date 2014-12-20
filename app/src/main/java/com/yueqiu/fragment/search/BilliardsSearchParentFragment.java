package com.yueqiu.fragment.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yueqiu.R;
import com.yueqiu.fragment.search.subfragment.SubBaseFragment;

/**
 * Created by scguo on 14/12/17.
 * <p/>
 * 球友Fragment
 */
public class BilliardsSearchParentFragment extends Fragment
{
    private static final String TAG = "DeskBallFragment";

    public static final String BILLIARD_SEARCH_TAB_NAME = "billiard_search_tab_name";
    private View mView;
    private String mArgs;
    private FragmentTabHost mTabHost;

    public BilliardsSearchParentFragment()
    {
    }

    private static final String KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT = "keyBilliardsSearchParentFragment";

    public static BilliardsSearchParentFragment newInstance(String params)
    {
        BilliardsSearchParentFragment fragment = new BilliardsSearchParentFragment();
        Bundle args = new Bundle();
        args.putString(KEY_BILLIARDS_SEARCH_PARENT_FRAGMENT, params);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.mate_fragment_layout, null);
        Bundle args = getArguments();
        mArgs = args.getString(BILLIARD_SEARCH_TAB_NAME);

        SubBaseFragment child1 = SubBaseFragment.newInstance("fuck");
        SubBaseFragment child2 = SubBaseFragment.newInstance("shit");

        Bundle args1 = new Bundle();
        args1.putString(BILLIARD_SEARCH_TAB_NAME, mArgs + 1);
        child1.setArguments(args1);

        Bundle args2 = new Bundle();
        args2.putString(BILLIARD_SEARCH_TAB_NAME, mArgs + 2);
        child2.setArguments(args2);

        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.search_mate_group_layout);
        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.search_mate_subfragment_title_distance)).setIndicator(getString(R.string.search_mate_subfragment_title_distance)),
                child1.getClass(), null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.search_mate_subfragment_title_gender)).setIndicator(getString(R.string.search_mate_subfragment_title_gender)),
                child2.getClass(), null);

        return mTabHost;
    }
}


















