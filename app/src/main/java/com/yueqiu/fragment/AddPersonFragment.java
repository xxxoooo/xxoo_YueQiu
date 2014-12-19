package com.yueqiu.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.yueqiu.R;
import com.yueqiu.SearchResultActivity;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧添加好友Fragment
 */
public class AddPersonFragment extends Fragment {
    private static final String TAG = "AddPersonFragment";
    Button mSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_persion, null);
        mSearch = (Button) view.findViewById(R.id.chatbar_btn_add_persion_search);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:result of search，new Activity or fragment
                Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

}
