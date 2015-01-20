package com.yueqiu.fragment.group;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;
    private List<GroupNoteInfo> mList = new ArrayList<GroupNoteInfo>();
    private GroupBasicAdapter mAdapter;
    private ProgressBar mPreProgress;
    private TextView mPreText,mEmptyView;
    private Drawable mProgressDrawable;
    private String mEmptyTypeStr;
    private int mGroupType;
    private int mStart_no = 0,mEnd_no = 9;

    private Map<String,Integer> mParamMap = new HashMap<String, Integer>();
    private Map<String,String> mUrlAndMethodMap = new HashMap<String, String>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_billard_group_basic,null);
        Bundle args = getArguments();
        mGroupType = args.getInt("type");
        initView();
        setEmptyTypeStr();

        if(Utils.networkAvaiable(mActivity)){
            requestGroup();
        }else{

        }


        return mView;
    }


    private void initView(){

        mGroup = (RadioGroup) mView.findViewById(R.id.billiard_radio_group);
        ((RadioButton)mGroup.findViewById(R.id.billiard_time_sort)).setChecked(true);
        mPreText = (TextView) mView.findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        mPullToRefreshListView = (PullToRefreshListView) mView.findViewById(R.id.billiard_group_listview);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView = mPullToRefreshListView.getRefreshableView();

        mProgressDrawable = new FoldingCirclesDrawable.Builder(mActivity).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);


        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.billiard_time_sort:
                        Collections.sort(mList,new TimeComparator());
                        //TODO:更新adapter
                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.billiard_popularity_sort:
                        Collections.sort(mList,new PopularityComparator());
                        //TODO:更新adapter
                        mAdapter.notifyDataSetChanged();
                        break;
                }
            }
        });
    }
    private void setEmptyTypeStr(){
        switch(mGroupType){
            case PublicConstant.GROUP_GET_MASTER:
                mEmptyTypeStr = getString(R.string.billiard_get_master);
                break;
            case PublicConstant.GROUP_BE_MASTER:
                mEmptyTypeStr = getString(R.string.billiard_be_master);
                break;
            case PublicConstant.GROUP_GET_FRIEND:
                mEmptyTypeStr = getString(R.string.billiard_find_friend);
                break;
            case PublicConstant.GROUP_EQUIP:
                mEmptyTypeStr = getString(R.string.billiard_equipment);
                break;
            case PublicConstant.GROUP_OTHER:
                mEmptyTypeStr = getString(R.string.billiard_other);
                break;

        }
    }
    private void setEmptyViewVisible(){
        mEmptyView = new TextView(mActivity);
        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        mEmptyView.setTextColor(getResources().getColor(R.color.md__defaultBackground));
        if(mGroupType == PublicConstant.GROUP_ALL) {
            mEmptyView.setText(getString(R.string.no_group_info));
        }else{
            mEmptyView.setText(getString(R.string.no_group_type_info,mEmptyTypeStr));
        }
        mPullToRefreshListView.setEmptyView(mEmptyView);
    }

    private void requestGroup(){
        if(mGroupType != PublicConstant.GROUP_ALL) {
            mParamMap.put(HttpConstants.GroupList.TYPE, mGroupType);
        }
        mParamMap.put(HttpConstants.GroupList.STAR_NO,mStart_no);
        mParamMap.put(HttpConstants.GroupList.END_NO,mEnd_no);

        mUrlAndMethodMap.put(PublicConstant.URL,HttpConstants.GroupList.URL);
        mUrlAndMethodMap.put(PublicConstant.METHOD,HttpConstants.RequestMethod.GET);

        new RequestGroupTask(mParamMap).execute(mUrlAndMethodMap);
    }

    private List<GroupNoteInfo> setGroupInfoByJSON(JSONObject object){
        List<GroupNoteInfo> infos = new ArrayList<GroupNoteInfo>();
        try {
            JSONArray list_data = object.getJSONObject("result").getJSONArray("list_data");
            for(int i=0;i<list_data.length();i++) {
                GroupNoteInfo info = new GroupNoteInfo();
                info.setNoteId(Integer.valueOf(list_data.getJSONObject(i).getString("id")));
                info.setUserName(list_data.getJSONObject(i).getString("username"));
                info.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                info.setTitle(list_data.getJSONObject(i).getString("title"));
                info.setContent(list_data.getJSONObject(i).getString("content"));
                info.setIssueTime(list_data.getJSONObject(i).getString("create_time"));
                info.setCommentCount(list_data.getJSONObject(i).getInt("comment_num"));
                info.setBrowseCount(list_data.getJSONObject(i).getInt("look_number"));
                infos.add(info);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return infos;
    }

    private class RequestGroupTask extends AsyncTaskUtil<Integer>{

        public RequestGroupTask(Map<String, Integer> map) {
            super(map);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreProgress.setVisibility(View.VISIBLE);
            mPreText.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mPreProgress.setVisibility(View.GONE);
            mPreText.setVisibility(View.GONE);

            if(mPullToRefreshListView.isRefreshing())
                mPullToRefreshListView.onRefreshComplete();
            try {
                if (!jsonObject.isNull("code")) {
                    if(jsonObject.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                        if(jsonObject.getJSONObject("result") != null){
                            List<GroupNoteInfo> infos = setGroupInfoByJSON(jsonObject);
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS,infos).sendToTarget();
                        }else{
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        }
                    }else if(jsonObject.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                        mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                    }else if(jsonObject.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                        mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                    }else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                } else {
                    mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    if(mPullToRefreshListView.isRefreshing())
                        mPullToRefreshListView.onRefreshComplete();
                    List<GroupNoteInfo> list = (List<GroupNoteInfo>) msg.obj;
                    for(int i=0;i<list.size();i++){
                         if (!mList.contains(list.get(i)))
                                mList.add(list.get(i));
                    }
                    if(mList.isEmpty())
                        setEmptyViewVisible();
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(mActivity,getString(R.string.http_request_time_out));
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(mActivity,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(mActivity, (String) msg.obj);
                    }
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }
                    break;
                case PublicConstant.NO_RESULT:
                    break;
            }

            mAdapter = new GroupBasicAdapter(mActivity,mList);
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
    };

    private class TimeComparator implements Comparator<GroupNoteInfo>{

        @Override
        public int compare(GroupNoteInfo lhs, GroupNoteInfo rhs) {
            long lhsTime = 0,rhsTime = 0;
            try {
                lhsTime = Utils.stringToLong(lhs.getIssueTime(),"yyyy-MM-dd HH:mm:ss");
                rhsTime = Utils.stringToLong(rhs.getIssueTime(),"yyyy-MM-dd HH:mm:ss");
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
