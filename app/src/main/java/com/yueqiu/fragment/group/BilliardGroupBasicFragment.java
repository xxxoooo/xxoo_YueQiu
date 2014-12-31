package com.yueqiu.fragment.group;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.yueqiu.BilliardGroupActivity;
import com.yueqiu.R;
import com.yueqiu.adapter.GroupBasicAdapter;
import com.yueqiu.bean.GroupNoteBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 14/12/17.
 * 台球圈基础的Fragment
 */
public class BilliardGroupBasicFragment extends Fragment {
    private View mView;
    private RadioGroup mGroup;
    private ListView mListView;
    private List<GroupNoteBean> mList = new ArrayList<GroupNoteBean>();
    private GroupBasicAdapter mAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_billard_group_basic,null);
        mGroup = (RadioGroup) mView.findViewById(R.id.billiard_radio_group);
        ((RadioButton)mGroup.findViewById(R.id.billiard_time_sort)).setChecked(true);

        mListView = (ListView) mView.findViewById(R.id.billiard_group_listview);

        GroupNoteBean bean1 = new GroupNoteBean();
        bean1.setTitle("求助台球高手们");
        bean1.setContent("我接触台球时间挺长...");
        bean1.setBrowseCount(213);
        bean1.setCommentCount(23);
        bean1.setIssueTime("12-20  21:20");


        for(int i=0;i<5;i++){
            mList.add(bean1);
        }

        mAdapter = new GroupBasicAdapter(getActivity(),mList);
        mListView.setAdapter(mAdapter);

        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
