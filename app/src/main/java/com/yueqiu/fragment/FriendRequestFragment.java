package com.yueqiu.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yueqiu.R;

/**
 * Created by doushuqi on 14/12/20.
 * 提到我中好友请求标签页
 */
public class FriendRequestFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_request, container, false);


        return view;
    }
}
