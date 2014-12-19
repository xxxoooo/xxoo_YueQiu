package com.yueqiu.fragment.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.Toast;


import com.yueqiu.R;
import com.yueqiu.activity.searchmenu.ActivitiesIssueActivity;
import com.yueqiu.adapter.ActivitiesListViewAdapter;
import com.yueqiu.view.XListView;

import java.util.ArrayList;



/**
 * Created by yinfeng on 14/12/19.
 */
public class ActivitiesFragment1 extends Fragment implements  View.OnClickListener {

    private XListView mListView;
    private ArrayList<String> mList;
    private ActivitiesListViewAdapter mAdapter;
    private Activity mActivity;
    private Button mBtn_Issue;
    private int y1,y2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activities_listview,null);
        mActivity = getActivity();
        mListView = (XListView)view.findViewById(R.id.activities_lv);
        mBtn_Issue = (Button)view.findViewById(R.id.activities_btn_issue);
        data();
        mAdapter = new ActivitiesListViewAdapter(mList,mActivity);
        mListView.setAdapter(mAdapter);
        mListView.setOnTouchListener(new ListViewOnTouchListener());
//        mListView.setOnScrollListener(new MyXListViewListener());
        mBtn_Issue.setOnClickListener(this);
        return view;
    }
    private void data()
    {
        mList = new ArrayList<String>();
        for(int i = 0; i < 60; ++i)
        {
            mList.add(System.currentTimeMillis()+"");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.activities_btn_issue:
                startActivity(new Intent(mActivity, ActivitiesIssueActivity.class));
                break;

        }
    }

    private class ListViewOnTouchListener implements View.OnTouchListener{

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    y1 = (int)motionEvent.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    y2 = (int) motionEvent.getY();
                    if(y2 - y1 > 60)
                    {
                        Animation animation = AnimationUtils.loadAnimation(mActivity,R.anim.push_top_out);
                        mBtn_Issue.setVisibility(View.VISIBLE);
                        mBtn_Issue.setAnimation(animation);
                    }
                    else
                    {
                        Animation animation = AnimationUtils.loadAnimation(mActivity,R.anim.push_top_in);
                        mBtn_Issue.setVisibility(View.GONE);
                        mBtn_Issue.setAnimation(animation);
                    }
                    break;
            }
            return false;
        }
    }


    private class MyXListViewListener implements XListView.OnXScrollListener{

        @Override
        public void onXScrolling(View view) {

        }

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {

        }

        @Override
        public void onScroll(AbsListView absListView, int i, int i2, int i3) {

        }
    }

}
