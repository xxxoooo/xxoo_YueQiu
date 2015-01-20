package com.yueqiu.fragment.group;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.BilliardGroupDetailActivity;
import com.yueqiu.adapter.GroupBasicAdapter;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by wangyun on 14/12/17.
 * 台球圈基础的Fragment
 */
public class BilliardGroupBasicFragment extends Fragment {
    private View mView;
    private Activity mActivity;
    private RadioGroup mGroup;
    private ListView mListView;
    private List<GroupNoteInfo> mList = new ArrayList<GroupNoteInfo>();
    private GroupBasicAdapter mAdapter;
    private ProgressBar mPreProgress;
    private TextView mPreText;
    private Drawable mProgressDrawable;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_billard_group_basic,null);
        mGroup = (RadioGroup) mView.findViewById(R.id.billiard_radio_group);
        ((RadioButton)mGroup.findViewById(R.id.billiard_time_sort)).setChecked(true);

        mListView = (ListView) mView.findViewById(R.id.billiard_group_listview);
        mList.clear();
        //ToDo:测试数据
        GroupNoteInfo bean1 = new GroupNoteInfo();
        bean1.setTitle("求助台球高手们");
        bean1.setContent("我接触台球时间挺长...");
        //bean1.set
        bean1.setBrowseCount(213);
        bean1.setCommentCount(23);
        bean1.setIssueTime("12-20 21:20");
        bean1.setLoveNums(5);

        GroupNoteInfo bean2 = new GroupNoteInfo();
        bean2.setTitle("台球收徒");
        bean2.setContent("首先声明，我并不是为钱...");
        bean2.setBrowseCount(46);
        bean2.setCommentCount(23);
        bean2.setIssueTime("12-11 11:20");
        bean1.setLoveNums(10);


        mList.add(bean1);
        mList.add(bean2);
        Collections.sort(mList,new TimeComparator());



        mAdapter = new GroupBasicAdapter(getActivity(),mList);
        mListView.setAdapter(mAdapter);


        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.billiard_time_sort:
                        Collections.sort(mList,new TimeComparator());
                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.billiard_popularity_sort:
                        Collections.sort(mList,new PopularityComparator());
                        mAdapter.notifyDataSetChanged();
                        break;
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                YueQiuApp.sGroupInfo = (GroupNoteInfo) mAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), BilliardGroupDetailActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.group_in_to_left,R.anim.group_out_to_left);
            }
        });

        return mView;
    }


    private void initView(){
        mPreText = (TextView) mView.findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(mActivity).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mPreText.setText(getString(R.string.activity_issuing));
    }



    private class TimeComparator implements Comparator<GroupNoteInfo>{

        @Override
        public int compare(GroupNoteInfo lhs, GroupNoteInfo rhs) {
            long lhsTime = 0,rhsTime = 0;
            try {
                lhsTime = Utils.stringToLong(lhs.getIssueTime(),"MM-dd HH:mm");
                rhsTime = Utils.stringToLong(rhs.getIssueTime(),"MM-dd HH:mm");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(lhsTime == rhsTime){
                return lhs.getNoteId() > rhs.getNoteId() ? -1 : 1;
            }
            return lhsTime > rhsTime ? -1 : 1;
        }
    }
    private class PopularityComparator implements Comparator<GroupNoteInfo>{
        @Override
        public int compare(GroupNoteInfo lhs, GroupNoteInfo rhs) {
            int lhsLoveNums = lhs.getLoveNums();
            int rhsLoveNums = rhs.getLoveNums();
            if(lhsLoveNums == rhsLoveNums){
                return lhs.getNoteId() > rhs.getNoteId() ? 1 : -1;
            }
            return lhsLoveNums > rhsLoveNums ? 1 : -1;
        }
    }


}
