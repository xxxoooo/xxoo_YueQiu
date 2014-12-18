package com.yueqiu.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yueqiu.R;

/**
 * Created by wangyun on 14/12/17.
 * 台球圈基础的Fragment
 */
public class BilliardGroupBasicFragment extends Fragment {
    private FragmentTabHost mTabHost;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(),getChildFragmentManager(),R.id.group_main_content);
        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.billiard_time)).setIndicator(getString(R.string.billiard_time)),
                BilliardGroupChildFragment.class,null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.billiard_poplarity)).setIndicator(getString(R.string.billiard_poplarity)),
                BilliardGroupChildFragment.class,null);

        return mTabHost;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTabHost = null;
    }
}
