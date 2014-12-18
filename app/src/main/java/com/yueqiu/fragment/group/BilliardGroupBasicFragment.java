package com.yueqiu.fragment.group;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yueqiu.R;

/**
 * Created by wangyun on 14/12/17.
 * 台球圈基础的Fragment
 */
public class BilliardGroupBasicFragment extends Fragment {
    public static final String BILLIARD_TAB_NAME = "billiard_tab_name";
    private View mView;
    private String mArgs;
    private FragmentTabHost mTabHost;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.tabhost_layout,null);
        Bundle args = getArguments();
        mArgs = args.getString(BILLIARD_TAB_NAME);

        BilliardGroupChildFragment child1 = new BilliardGroupChildFragment();
        BilliardGroupChildFragment child2 = new BilliardGroupChildFragment();

        Bundle args1 = new Bundle();
        args1.putString(BILLIARD_TAB_NAME,mArgs + 1);
        child1.setArguments(args1);

        Bundle args2 = new Bundle();
        args2.putString(BILLIARD_TAB_NAME,mArgs + 2);
        child2.setArguments(args2);

        //mTabHost = (FragmentTabHost) mView.findViewById(android.R.id.tabhost);
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(),getChildFragmentManager(),R.id.group_main_content);
        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.billiard_time)).setIndicator(getString(R.string.billiard_time)),
                child1.getClass(),null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.billiard_poplarity)).setIndicator(getString(R.string.billiard_poplarity)),
                child2.getClass(),null);

        return mTabHost;
    }
}
