package com.yueqiu.fragment.group;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.GroupBasicAdapter;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.GroupInfoDao;
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
    private static final String SAVE_GROUP_KEY = "save_group";
    private View mView;
    private Activity mActivity;
    private RadioGroup mGroup;
    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;
    private GroupBasicAdapter mAdapter;
    private ProgressBar mPreProgress;
    private TextView mPreText,mEmptyView;
    private Drawable mProgressDrawable;
    private String mEmptyTypeStr;
    private GroupInfoDao mGroupDao;
    private int mGroupType;
    private int mStart_no = 0,mEnd_no = 9;
    private int mBeforeCount,mAfterCount;
    private int mCurrPosition;
    private boolean mRefresh,mLoadMore;

    private Map<String,Integer> mParamMap = new HashMap<String, Integer>();
    private Map<String,String> mUrlAndMethodMap = new HashMap<String, String>();
    private ArrayList<GroupNoteInfo> mList = new ArrayList<GroupNoteInfo>();
    private List<GroupNoteInfo> mCacheList;
    private List<GroupNoteInfo> mInsertList = new ArrayList<GroupNoteInfo>();
    private List<GroupNoteInfo> mUpdateList = new ArrayList<GroupNoteInfo>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = getActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_GROUP_KEY,mList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_billiard_group_basic,null);
        Bundle args = getArguments();
        mGroupType = args.getInt("type");
        mGroupDao = DaoFactory.getGroupDao(mActivity);
        mAdapter = new GroupBasicAdapter(mActivity,mList);
        initView();
        setEmptyTypeStr();

        /////////////////////////////////////////////////////////////////////////////////////
        //TODO:目前不需要缓存，后期需要缓存的时候再把这段代码加上
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                //TODO:缓存的list，
//                if(mGroupType == PublicConstant.GROUP_ALL) {
//                    mCacheList = mGroupDao.getAllGroupInfoLimit(mStart_no, 10);
//                }else{
//                    mCacheList = mGroupDao.getGroupInfoByType(mGroupType,mStart_no,10);
//                }
//                if(!mCacheList.isEmpty()){
//                    mHandler.obtainMessage(PublicConstant.USE_CACHE,mCacheList).sendToTarget();
//                }
//            }
//        }).start();
        /////////////////////////////////////////////////////////////////////////////////////

        if(savedInstanceState != null){
            mCacheList = savedInstanceState.getParcelableArrayList(SAVE_GROUP_KEY);
            mHandler.obtainMessage(PublicConstant.USE_CACHE,mCacheList).sendToTarget();
        }
        //TODO:savedInstance为null证明该是第一次进入到viewpager,需要从数据库中读缓存
        //TODO:后期要做缓存的时候可以将上面读缓存的代码放到else里面
//        else{
//
//        }

        if(Utils.networkAvaiable(mActivity)){
            mLoadMore = false;
            mRefresh = false;
            requestGroup();
        }else{
            mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
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
        mPullToRefreshListView.setOnRefreshListener(mOnRefreshListener);
        mEmptyView = new TextView(mActivity);

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
                mEmptyTypeStr = mActivity.getString(R.string.billiard_get_master);
                break;
            case PublicConstant.GROUP_BE_MASTER:
                mEmptyTypeStr = mActivity.getString(R.string.billiard_be_master);
                break;
            case PublicConstant.GROUP_GET_FRIEND:
                mEmptyTypeStr = mActivity.getString(R.string.billiard_find_friend);
                break;
            case PublicConstant.GROUP_EQUIP:
                mEmptyTypeStr = mActivity.getString(R.string.billiard_equipment);
                break;
            case PublicConstant.GROUP_OTHER:
                mEmptyTypeStr = mActivity.getString(R.string.billiard_other);
                break;

        }
    }
    private void setEmptyViewVisible(){

        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        mEmptyView.setTextColor(mActivity.getResources().getColor(R.color.md__defaultBackground));
        if(mGroupType == PublicConstant.GROUP_ALL) {
            mEmptyView.setText(mActivity.getString(R.string.no_group_info));
        }else{
            mEmptyView.setText(mActivity.getString(R.string.no_group_type_info,mEmptyTypeStr));
        }
        mPullToRefreshListView.setEmptyView(mEmptyView);
    }

    private void setmEmptyViewGone(){
        if(null != mEmptyView){
            mEmptyView.setVisibility(View.GONE);
        }
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
             for (int i = 0; i < list_data.length(); i++) {
                 GroupNoteInfo info = new GroupNoteInfo();
                 info.setNoteId(Integer.parseInt(list_data.getJSONObject(i).getString("id")));
                 info.setUserName(list_data.getJSONObject(i).getString("username"));
                 info.setImg_url(list_data.getJSONObject(i).getString("img_url"));
                 info.setTitle(list_data.getJSONObject(i).getString("title"));
                 info.setContent(list_data.getJSONObject(i).getString("content"));
                 info.setIssueTime(list_data.getJSONObject(i).getString("create_time"));
                 info.setCommentCount(list_data.getJSONObject(i).getInt("comment_num"));
                 info.setBrowseCount(list_data.getJSONObject(i).getInt("look_number"));
                 if (mGroupType != PublicConstant.GROUP_ALL) {
                     info.setType(mGroupType);
                 }
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
                            List<GroupNoteInfo> list = setGroupInfoByJSON(jsonObject);
                            if(list.isEmpty()){
                                mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                            }else {
                                mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                            }
                        }else{
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        }
                    }else if(jsonObject.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                        mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                    }else if(jsonObject.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                        mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                    }else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,jsonObject.getString("msg")).sendToTarget();
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
            /**
             * 统一停止PullToRefreshView的刷新动作
             */
            if(mPullToRefreshListView.isRefreshing())
                mPullToRefreshListView.onRefreshComplete();
            switch(msg.what){
                case PublicConstant.USE_CACHE:
                    setmEmptyViewGone();
                    List<GroupNoteInfo> cacheList = (List<GroupNoteInfo>) msg.obj;
                    mList.addAll(cacheList);
                    break;
                case PublicConstant.GET_SUCCESS:
                    setmEmptyViewGone();
                    /**
                     * 保存还未更新的list的size
                     */
                    mBeforeCount = mList.size();

                    List<GroupNoteInfo> list = (List<GroupNoteInfo>) msg.obj;
                    for(GroupNoteInfo info : list){
                        /**
                         * UI list与数据库无关
                         */
                        //TODO:有可能存在服务器那边删了数据，但数据库中还会存在这条数据
                        //TODO:需要再加一定的逻辑判断，如果发生这样的情况，该如何处理
                         if (!mList.contains(info)) {
                             mList.add(info);
                         }
                        //TODO:目前不需要缓存，所以这块先不需要操作数据库
                        /**
                         * 数据库里的所有数据都不包含这条数据才插入数据,否则应该更新数据库
                         * 这里用全局map来保存数据库内容，避免每次创建都去读取数据库
                         */
//                        if(!YueQiuApp.sGroupDbMap.containsKey(info.getNoteId())){
//                            mInsertList.add(info);
//                        }else{
//                            mUpdateList.add(info);
//                        }
//                        YueQiuApp.sGroupDbMap.put(info.getNoteId(),info);
                    }
                    /**
                     * 保存更新完list以后的size,如果更新完以后的list的size没有变化
                     * 则意味着list没有改变，在下拉刷新时就意味着没有更新的数据
                     */
                    mAfterCount = mList.size();
                    /**
                     * 如果list为空，设置emptyView
                     */
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }else {
                        if (mRefresh) {
                            if (mAfterCount == mBeforeCount) {
                                Utils.showToast(mActivity, mActivity.getString(R.string.no_newer_info));
                            } else {
                                Utils.showToast(mActivity, mActivity.getString(R.string.have_already_update_info, mAfterCount - mBeforeCount));
                            }
                        }
                    }
                    //TODO:目前不需要缓存，所以这块先不需要操作数据库
                    /**
                     * 另起线程更新数据库
                     */
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            updateGroupInfoDB();
//                        }
//                    }).start();

                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(mActivity,mActivity.getString(R.string.http_request_time_out));
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(mActivity,mActivity.getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(mActivity, (String) msg.obj);
                    }
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }
                    break;
                case PublicConstant.NO_RESULT:
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }else{
                        /**
                         * 上拉加载时，没有更多数据
                         */
                        if(mLoadMore) {
                            if(mGroupType == PublicConstant.GROUP_ALL) {
                                Utils.showToast(mActivity, mActivity.getString(R.string.no_more_group_info));
                            }else{
                                Utils.showToast(mActivity,mActivity.getString(R.string.no_more_info,mEmptyTypeStr));
                            }
                        }
                    }
                    break;
                case PublicConstant.NO_NETWORK:
                    Utils.showToast(mActivity,mActivity.getString(R.string.network_not_available));
                    if(mList.isEmpty())
                        setEmptyViewVisible();
                    break;
            }
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            if(mLoadMore && !mList.isEmpty()){
                mListView.setSelection(mCurrPosition);
            }
        }
    };

    //TODO:由于目前不需要缓存，所以暂时先不调用该方法
    private void updateGroupInfoDB(){
        if(!mUpdateList.isEmpty()){
            mGroupDao.updateGroupInfo(mUpdateList);
        }
        if(!mInsertList.isEmpty()){
            long result = mGroupDao.insertGroupInfo(mInsertList);
            //TODO:目前noteId没有unique，可能还会存在重复插入的问题，还需要再验证
            /**
             * 插入失败，则更新数据库
             */
            if(result == -1){
                mGroupDao.updateGroupInfo(mInsertList);
            }
        }
    }

    private PullToRefreshBase.OnRefreshListener2 mOnRefreshListener = new PullToRefreshBase.OnRefreshListener2() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView) {
            //TODO:需要把刷新时间保存起来
            String label = DateUtils.formatDateTime(mActivity, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            mRefresh = true;
            mLoadMore = false;
            mInsertList.clear();
            mUpdateList.clear();
            if(Utils.networkAvaiable(mActivity)){

                mParamMap.put(HttpConstants.GroupList.STAR_NO,0);
                mParamMap.put(HttpConstants.GroupList.END_NO,9);
                requestGroup();
            }else{
                mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            String label = DateUtils.formatDateTime(mActivity, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            mLoadMore = true;
            mRefresh = false;
            mCurrPosition = mList.size() ;
            mInsertList.clear();
            mUpdateList.clear();
            /**
             * 如果要加载前先进行过下拉刷新，同时数据有更新，则此时再加载时分页的
             * start，end应该相应的增加
             */
            if(mBeforeCount != mAfterCount){
                mStart_no = mEnd_no + (mAfterCount - mBeforeCount);
                mEnd_no += 10 + (mAfterCount - mBeforeCount);
            }else{
                mStart_no = mEnd_no + 1;
                mEnd_no += 10;
            }
            if(Utils.networkAvaiable(mActivity)){
                mParamMap.put(HttpConstants.GroupList.STAR_NO,mStart_no);
                mParamMap.put(HttpConstants.GroupList.END_NO,mEnd_no);
                requestGroup();
            }
            //TODO:由于目前不需要缓存，所以当没有网络时，就直接toast无网络
            //TODO:如果后面需要缓存，再替换成注释掉的代码
            else{
                mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
            }
//            /**
//             * 没有网络的时候，应该从数据库中加载
//             */
//            else{
//                List<GroupNoteInfo> list;
//                /**
//                * 如果type为0，则查全部，否则查相应的类型
//                */
//                if(mGroupType == PublicConstant.GROUP_ALL){
//                    list = mGroupDao.getAllGroupInfoLimit(mStart_no,10);
//                }else{
//                    list = mGroupDao.getGroupInfoByType(mGroupType,mStart_no,10);
//                }
//                if(list.isEmpty()){
//                    mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
//                }else{
//                    mHandler.obtainMessage(PublicConstant.GET_SUCCESS,list);
//                }
//             }
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
