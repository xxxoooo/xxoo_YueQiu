package com.yueqiu.fragment.slidemenu;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.bean.FavorInfo;
import com.yueqiu.bean.ISlideMenuBasic;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.YueQiuDialogBuilder;
import com.yueqiu.view.progress.FoldingCirclesDrawable;
import com.yueqiu.view.pullrefresh.PullToRefreshBase;
import com.yueqiu.view.pullrefresh.PullToRefreshListView;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by wangyun on 15/1/20.
 */
public abstract class SlideMenuBasicFragment extends Fragment {
    private static final String TAG = "SlideMenu";
    protected Activity mActivity;
    protected View mView;
    protected ProgressBar mPreProgress;
    protected TextView mPreText,mEmptyView;
    protected Drawable mProgressDrawable;
    protected String mEmptyTypeStr;
    protected PullToRefreshListView mPullToRefreshListView;
    protected ListView mListView;
    protected int mType;
    protected boolean mLoadMore,mRefresh,mIsSavedInstance,mIsListEmpty;
    private BasicHandler mHandler;
    protected int mStart_no = 0,mEnd_no = 9;
    protected int mCurrPosition;
    protected int mAfterCount,mBeforeCount;
    protected ArrayList<ISlideMenuBasic> mList = new ArrayList<ISlideMenuBasic>();
    protected Map<String,String>  mUrlAndMethodMap = new HashMap<String, String>();
    protected Map<String,Integer> mParamsMap = new HashMap<String, Integer>();
    protected List<PublishedInfo> mPublishInsertList = new ArrayList<PublishedInfo>();
    protected List<PublishedInfo> mPublishUpdateList = new ArrayList<PublishedInfo>();
    protected List<FavorInfo> mFavorInsertList = new ArrayList<FavorInfo>();
    protected List<FavorInfo> mFavorUpdateList = new ArrayList<FavorInfo>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;

    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_favor_basic_layout,null);
        mType = getType();
        initView();
        setEmptyViewText();
        mHandler = getHandler();
        return mView;
    }

    protected void initView(){
        mPullToRefreshListView = (PullToRefreshListView) mView.findViewById(R.id.favor_basic_listView);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshListView.setOnRefreshListener(mOnRefreshListener);
        mListView = mPullToRefreshListView.getRefreshableView();
//        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//        mListView.setMultiChoiceModeListener(new ActionModeCallback());
        mEmptyView = new TextView(mActivity);


        mPreText = (TextView) mView.findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(mActivity).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);


    }
    protected void setEmptyViewVisible(String text){

        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        mEmptyView.setTextColor(mActivity.getResources().getColor(R.color.md__defaultBackground));
        mEmptyView.setText(text);
        mPullToRefreshListView.setEmptyView(mEmptyView);
    }

    protected void setEmptyViewGone(){
        if(null != mEmptyView){
            mEmptyView.setVisibility(View.GONE);
        }
    }


    private int getType(){
        Bundle args = getArguments();
        int type = args.getInt("type");
        return type;
    }


    protected class ResponseHandler<T> extends JsonHttpResponseHandler{
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            Log.d("wy","response ->" + response);
            try {
                if(!response.isNull("code")) {
                    if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                        if (response.getJSONObject("result") != null) {
                            List<T> list = setBeanByJSON(response);
                            if(list.isEmpty()){
                                mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                            }else{
                                mHandler.obtainMessage(PublicConstant.GET_SUCCESS, list).sendToTarget();
                            }

                        }else{
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        }
                    }
                    else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                        mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                    }
                    else if(response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                        mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                    }
                    else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                    }
                }else{
                    mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            super.onFailure(statusCode, headers, responseString, throwable);
            mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
        }
    }


    protected class BasicHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPreProgress.setVisibility(View.GONE);
            mPreText.setVisibility(View.GONE);
            if(mPullToRefreshListView.getMode() == PullToRefreshBase.Mode.DISABLED){
                mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
            }
            if(mPullToRefreshListView.isRefreshing())
                mPullToRefreshListView.onRefreshComplete();
            switch (msg.what){
                case PublicConstant.NO_RESULT:
                    if(mList.isEmpty()) {
                        setEmptyViewVisible(mActivity.getString(R.string.your_published_info_is_empty,mEmptyTypeStr));
                    }else{
                        if(mLoadMore)
                            Utils.showToast(mActivity, mActivity.getString(R.string.no_more_info, mEmptyTypeStr));
                    }
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(mActivity,mActivity.getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(mActivity, (String) msg.obj);
                    }
                    if(mList.isEmpty()) {
                        setEmptyViewVisible(mActivity.getString(R.string.your_published_info_is_empty,mEmptyTypeStr));
                    }
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(mActivity,mActivity.getString(R.string.http_request_time_out));
                    if(mList.isEmpty()) {
                        setEmptyViewVisible(mActivity.getString(R.string.your_published_info_is_empty,mEmptyTypeStr));
                    }
                    break;
                case PublicConstant.NO_NETWORK:
                    Utils.showToast(mActivity,mActivity.getString(R.string.network_not_available));
                    if(mList.isEmpty())
                        setEmptyViewVisible(mActivity.getString(R.string.your_published_info_is_empty,mEmptyTypeStr));
                    break;
            }
        }
    }

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
            onItemStateChanged(position,checked);

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
                mActionModeTitle.setText(getActionModeTitle());
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

                mActionModeTitle.setText(getActionModeTitle());
                mode.setCustomView(view);
            }
            return true;
        }


        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()){
                case R.id.delete:
                    mode.finish();
                    View contents = View.inflate(mActivity,R.layout.dialog_delete_content_layout, null);
                    TextView msg = (TextView) contents.findViewById(R.id.confir_dialog_message);
                    msg.setText(mActivity.getString(R.string.published_delete_content,mSelectedItems.size()));
                    YueQiuDialogBuilder builder = new YueQiuDialogBuilder(mActivity);
                    builder.setTitle(R.string.action_delete);
                    builder.setIcon(R.drawable.warning_white);
                    builder.setView(contents);
                    SpannableString confirmSpanStr = new SpannableString(mActivity.getString(R.string.published_confirm_str));
                    confirmSpanStr.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.md__defaultBackground)), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setPositiveButton(confirmSpanStr,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    SpannableString cancelSpanStr = new SpannableString(mActivity.getString(R.string.published_cancel_str));
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
            unCheckAll();
        }
    }

    private PullToRefreshBase.OnRefreshListener2 mOnRefreshListener = new PullToRefreshBase.OnRefreshListener2() {
        /**
         * onPullDownToRefresh will be called only when the user has Pulled from
         * the start, and released.
         *
         * @param refreshView
         */
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView) {
            String label = DateUtils.formatDateTime(mActivity, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            mRefresh = true;
            mLoadMore = false;
            mPublishInsertList.clear();
            mPublishUpdateList.clear();
            mFavorInsertList.clear();
            mFavorInsertList.clear();
            if(mEmptyView.getVisibility() == View.VISIBLE){
                mEmptyView.setVisibility(View.GONE);
            }
            if(Utils.networkAvaiable(mActivity)){
               mParamsMap.put(HttpConstants.Published.START_NO,0);
               mParamsMap.put(HttpConstants.Published.END_NO, 9);
               requestResult();
            }else{
               mHandler.obtainMessage(PublicConstant.NO_NETWORK).sendToTarget();
            }
        }

        /**
         * onPullUpToRefresh will be called only when the user has Pulled from
         * the end, and released.
         *
         * @param refreshView
         */
        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            String label = DateUtils.formatDateTime(mActivity, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

            refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

            mLoadMore = true;
            mPublishInsertList.clear();
            mPublishUpdateList.clear();
            mFavorInsertList.clear();
            mFavorUpdateList.clear();
            mCurrPosition = mList.size() ;

            if(mEmptyView.getVisibility() == View.VISIBLE){
                mEmptyView.setVisibility(View.GONE);
            }

            if(mBeforeCount != mAfterCount && mRefresh){
                  mStart_no = mEnd_no + (mAfterCount-mBeforeCount);
                  mEnd_no += 10 + (mAfterCount-mBeforeCount);
             }else{
                  mStart_no = mEnd_no + 1;
                  mEnd_no += 10;
             }
            mRefresh = false;
             if(Utils.networkAvaiable(mActivity)) {
                  mParamsMap.put(HttpConstants.Published.START_NO,mStart_no);
                  mParamsMap.put(HttpConstants.Published.END_NO, mEnd_no);
                  requestResult();
             }else{
                  onPullUpWhenNetNotAvailable();
             }
        }
    };


    protected abstract void unCheckAll();

    protected abstract String getActionModeTitle();

    protected abstract void setEmptyViewText();

    protected abstract void requestResult();

    protected abstract <T> List<T> setBeanByJSON(JSONObject jsonResult);

    protected abstract BasicHandler getHandler();

    protected abstract void onItemStateChanged(int position,boolean checked);

    protected abstract void onPullUpWhenNetNotAvailable();
}
