package com.yueqiu.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yueqiu.R;

/**
 * Created by wangyun on 14/12/30.
 */
public class MyFavorBasicFragment extends Fragment{
    private View mView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_favor_basic_layout,null);
        return mView;
    }
}
