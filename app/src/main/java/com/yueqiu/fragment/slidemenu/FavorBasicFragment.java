package com.yueqiu.fragment.slidemenu;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.FavorBasicAdapter;
import com.yueqiu.bean.FavorInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.FavorDao;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.YueQiuDialogBuilder;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by wangyun on 15/1/16.
 */
public class FavorBasicFragment extends Fragment {
    private Activity mActivity;
    private View mView;
    private ProgressBar mPreProgress;
    private TextView mPreText;
    private Drawable mProgressDrawable;
    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;
    private FavorBasicAdapter mAdapter;
    private int mFavorType;
    private FavorInfo mFavorInfo;
    private FavorDao mFavorDao;
    private String mEmptyTypeStr;
    private boolean mLoadMore,mRefresh;
    private boolean mIsExsitsFavor;
    private int start_no = 0;
    private int end_no = 9;
    private int mCurrPosition;
    private int mBeforeCount,mAfterCount;

    private List<FavorInfo.FavorItemInfo> mList = new ArrayList<FavorInfo.FavorItemInfo>();
    private Map<String,Integer> mParamsMap = new HashMap<String, Integer>();
    private Map<String,String>  mUrlAndMethodMap = new HashMap<String, String>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_favor_basic_layout,null);
        Bundle args = getArguments();
        mFavorType = args.getInt("type");
        initView();
        setEmptyViewText();

        mFavorDao = DaoFactory.getFavor(mActivity);
        mIsExsitsFavor = mFavorDao.isExistFavorInfo(mFavorType);
        //先从缓存读取
        //TODO:逻辑可能再改
        if(mIsExsitsFavor){
            mFavorInfo = mFavorDao.getFavorInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()),mFavorType,start_no,end_no+1);
            mHandler.obtainMessage(PublicConstant.USE_CACHE,mFavorInfo).sendToTarget();
        }

        if(Utils.networkAvaiable(mActivity)){
            mLoadMore = false;
            mRefresh = false;
            requestFavor();
        }else{
            if(mList.isEmpty()){
                mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
            }
        }
        return mView;
    }
    private void initView(){
        mPullToRefreshListView = (PullToRefreshListView) mView.findViewById(R.id.favor_basic_listView);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshListView.setOnRefreshListener(onRefreshListener);
        mListView = mPullToRefreshListView.getRefreshableView();
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new ActionModeCallback());

        //初始化ProgressBar
        mPreText = (TextView) mView.findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(mActivity).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
    }

    /**
     * 设置EmptyView的内容
     */
    private void setEmptyViewText(){
        switch(mFavorType){
            case PublicConstant.FAVOR_DATE_TYPE:
                mEmptyTypeStr = getString(R.string.search_billiard_dating_str);
                break;
            case PublicConstant.FAVPR_ROOM_TYPE:
                mEmptyTypeStr = getString(R.string.search_billiard_room_str);
                break;
            case PublicConstant.FAVOR_ACTIVITY_TYPE:
                mEmptyTypeStr = getString(R.string.tab_title_activity);
                break;
            case PublicConstant.FAVOR_GROUP_TYPR:
                mEmptyTypeStr = getString(R.string.tab_title_billiards_circle);
                break;
        }
    }

    /**
     * 设置EmptyView可见
     */
    private void setEmptyViewVisible(String content){
        TextView emptyView = new TextView(mActivity);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        emptyView.setTextColor(getResources().getColor(R.color.md__defaultBackground));
        emptyView.setText(content);
        mPullToRefreshListView.setEmptyView(emptyView);

    }

    private void requestFavor(){
        mParamsMap.put(DatabaseConstant.UserTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        mParamsMap.put(HttpConstants.Favor.TYPE,mFavorType);
        mParamsMap.put(HttpConstants.Favor.START_NO,start_no);
        mParamsMap.put(HttpConstants.Favor.END_NO, end_no);

        mUrlAndMethodMap.put(PublicConstant.URL, HttpConstants.Favor.URL);
        mUrlAndMethodMap.put(PublicConstant.METHOD, HttpConstants.RequestMethod.GET);

        new RequestAsyncTask(mParamsMap).execute(mUrlAndMethodMap);
    }
    private void updateFavorDB(final FavorInfo info){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mIsExsitsFavor){
                    if(mFavorDao.updateFavorInfo(info) != -1){
                        for(int i=0;i<info.mList.size();i++){
                            FavorInfo.FavorItemInfo item =  info.mList.get(i);
                            if(mFavorDao.isExistFavorItemInfo(Integer.valueOf(item.getTable_id()),item.getType())){
                                mFavorDao.updateFavorItemInfo(info);
                            }else{
                                mFavorDao.insertFavorItemInfo(info);
                            }
                        }
                    }
                }else {
                    mFavorDao.insertFavorInfo(info);
                    mFavorDao.insertFavorItemInfo(info);
                }
            }
        }).start();
    }


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case PublicConstant.USE_CACHE:
                    mList.addAll(((FavorInfo)msg.obj).mList);
                    break;
                case PublicConstant.GET_SUCCESS:
                    mBeforeCount = mList.size();
                    FavorInfo info = (FavorInfo) msg.obj;
                    for(int i=0;i<info.mList.size();i++){
                        if(!mList.contains(info.mList.get(i))){
                            mList.add(info.mList.get(i));
                        }
                    }
                    updateFavorDB((FavorInfo) msg.obj);
                    mAfterCount = mList.size();
                    if(mList.isEmpty()){
                        setEmptyViewVisible(getString(R.string.your_favor_is_empty,mEmptyTypeStr));
                    }
                    break;
                case PublicConstant.NO_RESULT:
                    if(mList.isEmpty()) {
                        setEmptyViewVisible(getString(R.string.your_favor_is_empty,mEmptyTypeStr));
                    }else{
                        if(mLoadMore)
                            Utils.showToast(mActivity,getString(R.string.no_more_info, mEmptyTypeStr));
                    }
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(mActivity,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(mActivity, (String) msg.obj);
                    }
                    if(mList.isEmpty()) {
                        setEmptyViewVisible(getString(R.string.no_store_info));
                    }
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(mActivity,getString(R.string.http_request_time_out));
                    if(mList.isEmpty()) {
                        setEmptyViewVisible(getString(R.string.no_store_info));
                    }
                    break;
            }
            mAdapter = new FavorBasicAdapter(mActivity,mList);
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            if(mLoadMore && !mList.isEmpty()){
                mListView.setSelection(mCurrPosition);
            }
        }
    };

    private class RequestAsyncTask extends AsyncTaskUtil<Integer> {

        public RequestAsyncTask(Map<String, Integer> map) {
            super(map);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!mLoadMore && !mRefresh) {
                mPreProgress.setVisibility(View.VISIBLE);
                mPreText.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonResult) {
            super.onPostExecute(jsonResult);
            mPreProgress.setVisibility(View.GONE);
            mPreText.setVisibility(View.GONE);

            mPullToRefreshListView.onRefreshComplete();
            try {
                if(!jsonResult.isNull("code")) {
                    if (jsonResult.getInt("code") == HttpConstants.ResponseCode.NORMAL) {

                        if (jsonResult.getJSONObject("result") != null) {
                            FavorInfo Favor = setFavorInfo(jsonResult);
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS, Favor).sendToTarget();
                        }else{
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        }
                    }
                    else if(jsonResult.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                        mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                    }
                    else if(jsonResult.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                        mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                    }
                    else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,jsonResult.getString("msg")).sendToTarget();
                    }
                }else{
                    mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据json对象设置Favor
     * @param jsonResult
     * @return
     */
    private FavorInfo setFavorInfo(JSONObject jsonResult){
        FavorInfo info = new FavorInfo();
        info.setType(mFavorType);
        try {
            info.setStart_no(jsonResult.getJSONObject("result").getInt("start_no"));
            info.setEnd_no(jsonResult.getJSONObject("result").getInt("end_no"));
            info.setCount(jsonResult.getJSONObject("result").getInt("count"));
            JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");

            for (int i = 0; i < list_data.length(); i++) {
                FavorInfo.FavorItemInfo itemInfo = info.new FavorItemInfo();
                itemInfo.setTable_id(list_data.getJSONObject(i).getString("id"));
                itemInfo.setImage_url(list_data.getJSONObject(i).getString("img_url"));
                itemInfo.setTitle(list_data.getJSONObject(i).getString("title"));
                itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                itemInfo.setDateTime(list_data.getJSONObject(i).getString("create_time"));
                itemInfo.setUserName(list_data.getJSONObject(i).getString("username"));
                itemInfo.setType(Integer.valueOf(list_data.getJSONObject(i).getString("type_id")));
                itemInfo.setChecked(false);
                info.mList.add(itemInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * 下拉刷新和上拉加载的回调接口
     */
    private PullToRefreshBase.OnRefreshListener2<ListView> onRefreshListener = new PullToRefreshBase.OnRefreshListener2<ListView>() {
        @Override
        public void onPullDownToRefresh( PullToRefreshBase<ListView> refreshView) {
            String label = DateUtils.formatDateTime(mActivity, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            //设置上次更新时间
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(Utils.networkAvaiable(mActivity)){
                        mRefresh = true;
                        mLoadMore = false;
                        mParamsMap.put(HttpConstants.Published.START_NO,0);
                        mParamsMap.put(HttpConstants.Published.END_NO, 9);
                        requestFavor();
                    }else{
                        Toast.makeText(mActivity,getString(R.string.network_not_available),Toast.LENGTH_SHORT).show();
                    }
                }
            }).start();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            String label = DateUtils.formatDateTime(mActivity, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
            //设置上次更新时间
            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLoadMore = true;
                    mCurrPosition = mList.size() ;
                    if(mBeforeCount != mAfterCount){
                        start_no = end_no + (mAfterCount-mBeforeCount);
                        end_no += 10 + (mAfterCount-mBeforeCount);
                    }else{
                        start_no = end_no + 1;
                        end_no += 10;
                    }
                    if(Utils.networkAvaiable(mActivity)) {
                        mParamsMap.put(HttpConstants.Published.START_NO,start_no);
                        mParamsMap.put(HttpConstants.Published.END_NO, end_no);
                        requestFavor();
                    }else{

                        FavorInfo info= mFavorDao.getFavorInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()), mFavorType, start_no, end_no + 1);
                        if(!info.mList.isEmpty()) {
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS, info).sendToTarget();
                        }else{
                            mHandler.obtainMessage(PublicConstant.NO_RESULT,info).sendToTarget();
                        }
                    }
                }
            }, 1000);
        }
    };
    /**
     * ActionMode，ListView长按
     */
    private class ActionModeCallback implements ListView.MultiChoiceModeListener{

        private View mCustomActionBarView;
        private TextView mActionModeTitle,mActionModeSelCount;
        private HashSet<Integer> mSelectedItems;

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            mSelectedItems.add(position);
            int checkedCount = mListView.getCheckedItemCount();
            mActionModeSelCount.setText(Integer.toString(checkedCount));
            FavorInfo.FavorItemInfo itemInfo = (FavorInfo.FavorItemInfo) mListView.getItemAtPosition(position);
            itemInfo.setChecked(checked);
            mAdapter.notifyDataSetChanged();

        }
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.clear();
            MenuInflater inflater = mActivity.getMenuInflater();
            inflater.inflate(R.menu.published_action_mode_menu,menu);
            mSelectedItems = new HashSet<Integer>();
            if(mCustomActionBarView == null) {
                mCustomActionBarView = LayoutInflater.from(mActivity).inflate(R.layout.custom_published_action_bar_layout, null);

                mActionModeTitle = (TextView) mCustomActionBarView.findViewById(R.id.action_mode_title_tv);
                mActionModeSelCount = (TextView) mCustomActionBarView.findViewById(R.id.action_mode_selected_count);

                mActionModeTitle.setText(mActivity.getString(R.string.my_colloec_action_mode_title));
            }
            mode.setCustomView(mCustomActionBarView);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            if(mCustomActionBarView == null){
                ViewGroup view = (ViewGroup) LayoutInflater.from(mActivity).inflate(R.layout.custom_actionbar_layout, null);
                mActionModeTitle = (TextView) view.findViewById(R.id.action_mode_title_tv);
                mActionModeSelCount = (TextView) view.findViewById(R.id.action_mode_selected_count);

                mActionModeTitle.setText(mActivity.getString(R.string.my_colloec_action_mode_title));
                mode.setCustomView(view);
            }
            return true;
        }


        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()){
                case R.id.delete:
                    mode.finish();
                    View contents = View.inflate(mActivity,R.layout.confirm_delete_content_layout, null);
                    TextView msg = (TextView) contents.findViewById(R.id.confir_dialog_message);
                    msg.setText(getString(R.string.published_delete_content,mSelectedItems.size()));
                    YueQiuDialogBuilder builder = new YueQiuDialogBuilder(mActivity);
                    builder.setTitle(R.string.action_delete);
                    builder.setIcon(R.drawable.warning_white);
                    builder.setView(contents);
                    SpannableString confirmSpanStr = new SpannableString(getString(R.string.published_confirm_str));
                    confirmSpanStr.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.md__defaultBackground)), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setPositiveButton(confirmSpanStr,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    SpannableString cancelSpanStr = new SpannableString(getString(R.string.published_cancel_str));
                    cancelSpanStr.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.md__defaultBackground)), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setNegativeButton(cancelSpanStr,null);
                    builder.show();
                    break;
                default:
                    break;
            }
            return true;
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.unCheckAll();
            mAdapter.notifyDataSetChanged();
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;

    }





}
