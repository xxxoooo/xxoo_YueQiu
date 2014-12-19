package com.yueqiu.fragment.group;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yueqiu.R;

/**
 * Created by wangyun on 14/12/17.
 */
public class BilliardGroupChildFragment extends Fragment {
    public static final String BILLIARD_TAB_NAME = "billiard_tab_name";
    private View mView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_billard_group_basic,null);
        Bundle args = getArguments();
        ((TextView)mView.findViewById(R.id.text)).setText(args.getString(BILLIARD_TAB_NAME));
        return mView;
    }
}
