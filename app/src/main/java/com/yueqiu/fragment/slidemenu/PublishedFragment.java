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
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ActionMode;
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
import com.yueqiu.adapter.PublishedBasicAdapter;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.PublishedDao;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.XListView;
import com.yueqiu.view.YueQiuDialogBuilder;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by wangyun on 15/1/15.
 */
public class PublishedFragment extends Fragment implements XListView.IXListViewListener{
    private View mView;
    private XListView mListView;
    private ProgressBar mPreProgress;
    private TextView mEmptyView,mPreText;
    private Drawable mProgressDrawable;
    private PublishedBasicAdapter mAdapter;
    private int mPublishedType;
    private PublishedInfo mPublishedInfo;
    private PublishedDao mPublishedDao;
    private String mEmptyTypeStr;
    private boolean mLoadMore;
    private boolean mIsExsitsPublished;
    private Activity mActivity;
    private int start_no = 0;
    private int end_no = 9;
    private int mCurrPosition;

    private List<PublishedInfo.PublishedItemInfo> mList = new ArrayList<PublishedInfo.PublishedItemInfo>();
    private Map<String,Integer> mParamsMap = new HashMap<String, Integer>();
    private Map<String,String>  mUrlAndMethodMap = new HashMap<String, String>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPublishedDao = DaoFactory.getPublished(mActivity);
        Bundle args = getArguments();
        mPublishedType = args.getInt("type");
        try {
            if(mPublishedType == PublicConstant.PUBLISHED_DATE_TYPE){
               YueQiuApp.sPublishedInfo = YueQiuApp.sDatePublishedTask.get();
            }else if(mPublishedType == PublicConstant.PUBLISHED_ACTIVITY_TYPE){
               YueQiuApp.sPublishedInfo = YueQiuApp.sActivityPublishedTask.get();
            }else if(mPublishedType == PublicConstant.PUBLISHED_GROUP_TYPE){
               YueQiuApp.sPublishedInfo = YueQiuApp.sGroupPublishedTask.get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        mIsExsitsPublished = mPublishedDao.isExistPublishedInfo(mPublishedType);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_favor_basic_layout,null);
        initView();
        setEmptyViewText();

        if(!YueQiuApp.sPublishedInfo.mList.isEmpty()){
            mHandler.obtainMessage(PublicConstant.USE_CACHE,YueQiuApp.sPublishedInfo).sendToTarget();
        }

        if(Utils.networkAvaiable(mActivity)){
            requestPublished();
        }else{
            if(mList.isEmpty()){
                mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
            }
        }
        return mView;
    }

    private void initView(){
        mListView = (XListView) mView.findViewById(R.id.favor_basic_listView);
        mListView.setPullLoadEnable(true);
        mListView.setXListViewListener(this);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new ActionModeCallback());
        mEmptyView = (TextView) mView.findViewById(R.id.favor_is_empty);
        mPreText = (TextView) mView.findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
    }
    private void setEmptyViewText(){
        switch(mPublishedType){
            case 1:
                mEmptyTypeStr = getString(R.string.search_billiard_dating_str);
                break;
            case 2:
                mEmptyTypeStr = getString(R.string.tab_title_activity);
                break;
            case 3:
                mEmptyTypeStr = getString(R.string.tab_title_billiards_circle);
                break;

        }
        mEmptyView.setText(getString(R.string.your_published_info_is_empty,mEmptyTypeStr));
    }

    private void setEmptyViewVisible(){
        mListView.setPullLoadEnable(false);
        mListView.setDividerHeight(0);
        mEmptyView.setVisibility(View.VISIBLE);
    }
    private void requestPublished(){
        mParamsMap.put(DatabaseConstant.UserTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        mParamsMap.put(HttpConstants.Published.TYPE,mPublishedType);
        mParamsMap.put(HttpConstants.Published.START_NO,start_no);
        mParamsMap.put(HttpConstants.Published.END_NO, end_no);

        mUrlAndMethodMap.put(PublicConstant.URL, HttpConstants.Published.URL);
        mUrlAndMethodMap.put(PublicConstant.METHOD, HttpConstants.RequestMethod.GET);

        new RequestAsyncTask(mParamsMap).execute(mUrlAndMethodMap);
    }

    private void updatePublishedDB(final PublishedInfo info){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mIsExsitsPublished){
                    if(mPublishedDao.updatePublishInfo(info) != -1){
                        for(int i=0;i<info.mList.size();i++){
                            PublishedInfo.PublishedItemInfo item =  info.mList.get(i);
                            if(mPublishedDao.isExistPublishedItemInfo(Integer.valueOf(item.getTable_id()),mPublishedType)){
                                mPublishedDao.updatePublishedItemInfo(info);
                            }else{
                                mPublishedDao.insertPublishItemInfo(info);
                            }
                        }
                    }
                }else {
                    mPublishedDao.insertPublishInfo(info);
                    mPublishedDao.insertPublishItemInfo(info);
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
                    mList.addAll(((PublishedInfo)msg.obj).mList);
                    break;
                case PublicConstant.GET_SUCCESS:
                    updatePublishedDB((PublishedInfo) msg.obj);
                    if(mList.isEmpty()){
                        setEmptyViewVisible();
                    }else{
                        int dividerHeight = Utils.dip2px(getActivity(),1.5f);
                        mListView.setDividerHeight(dividerHeight);
                    }
                    break;
                case PublicConstant.NO_RESULT:
                    if(mList.isEmpty()) {
                        setEmptyViewVisible();
                    }else{
                        Toast.makeText(getActivity(), getString(R.string.no_more_info, mEmptyTypeStr), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case PublicConstant.REQUEST_ERROR:
                    Toast.makeText(getActivity(), getString(R.string.http_request_error), Toast.LENGTH_SHORT).show();
                    if(mList.isEmpty()) {
                        mEmptyView.setText(getString(R.string.no_published_info));
                        setEmptyViewVisible();
                    }
                    break;
                case PublicConstant.TIME_OUT:
                    Toast.makeText(getActivity(),getString(R.string.http_request_time_out),Toast.LENGTH_SHORT).show();
                    if(mList.isEmpty()) {
                        mEmptyView.setText(getString(R.string.no_published_info));
                        setEmptyViewVisible();
                    }
                    break;
            }
            mAdapter = new PublishedBasicAdapter(mActivity,mList);
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            if(mLoadMore && !mList.isEmpty()){
                mListView.setSelection(mCurrPosition);
            }
        }
    };

    private class RequestAsyncTask extends AsyncTaskUtil<Integer>{

        public RequestAsyncTask(Map<String, Integer> map) {
            super(map);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!mLoadMore) {
                mPreProgress.setVisibility(View.VISIBLE);
                mPreText.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonResult) {
            super.onPostExecute(jsonResult);
            mPreProgress.setVisibility(View.GONE);
            mPreText.setVisibility(View.GONE);
            if(mLoadMore){
                mListView.stopRefresh();
                mListView.stopLoadMore();
                mListView.setRefreshTime("刚刚");
            }
            try {
                if(!jsonResult.isNull("code")) {
                    if (jsonResult.getInt("code") == HttpConstants.ResponseCode.NORMAL) {

                        if (jsonResult.getJSONObject("result") != null) {
                            PublishedInfo published = setPublishedInfo(jsonResult);
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS, published).sendToTarget();
                        }else{
                            mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                        }
                    }
                    else if(jsonResult.getInt("code") == HttpConstants.ResponseCode.RESULT_NULL){
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,jsonResult.getString("msg")).sendToTarget();
                    }
                    else if(jsonResult.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                        mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                    }
                    else if(jsonResult.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                        mHandler.obtainMessage(PublicConstant.NO_RESULT).sendToTarget();
                    }
                    else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                }else{
                    mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private PublishedInfo setPublishedInfo(JSONObject jsonResult){
        PublishedInfo published = new PublishedInfo();
        published.setType(mPublishedType);
        try {
            published.setStart_no(jsonResult.getJSONObject("result").getInt("start_no"));
            published.setEnd_no(jsonResult.getJSONObject("result").getInt("end_no"));
            published.setSumCount(jsonResult.getJSONObject("result").getInt("count"));
            JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");

            for (int i = 0; i < list_data.length(); i++) {
                PublishedInfo.PublishedItemInfo itemInfo = published.new PublishedItemInfo();
                itemInfo.setTable_id(list_data.getJSONObject(i).getString("id"));
                itemInfo.setTitle(list_data.getJSONObject(i).getString("title"));
                itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                itemInfo.setDateTime(list_data.getJSONObject(i).getString("create_time"));
                itemInfo.setChecked(false);
                published.mList.add(itemInfo);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return published;
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoadMore = true;
                mCurrPosition = mList.size() - 1;
                start_no = end_no + 1;
                end_no += 10;
                requestPublished();
            }
        }, 1500);

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
            PublishedInfo.PublishedItemInfo itemInfo = (PublishedInfo.PublishedItemInfo) mListView.getItemAtPosition(position);
            itemInfo.setChecked(checked);
            mAdapter.notifyDataSetChanged();

        }
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.clear();
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.published_action_mode_menu,menu);
            mSelectedItems = new HashSet<Integer>();
            if(mCustomActionBarView == null) {
                mCustomActionBarView = LayoutInflater.from(getActivity()).inflate(R.layout.custom_published_action_bar_layout, null);

                mActionModeTitle = (TextView) mCustomActionBarView.findViewById(R.id.action_mode_title_tv);
                mActionModeSelCount = (TextView) mCustomActionBarView.findViewById(R.id.action_mode_selected_count);

                mActionModeTitle.setText(getActivity().getString(R.string.published_action_mode_title));
            }
            mode.setCustomView(mCustomActionBarView);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            if(mCustomActionBarView == null){
                ViewGroup view = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.custom_actionbar_layout,null);
                mActionModeTitle = (TextView) view.findViewById(R.id.action_mode_title_tv);
                mActionModeSelCount = (TextView) view.findViewById(R.id.action_mode_selected_count);

                mActionModeTitle.setText(getActivity().getString(R.string.published_action_mode_title));
                mode.setCustomView(view);
            }
            return true;
        }


        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()){
                case R.id.delete:
                    mode.finish();
                    View contents = View.inflate(getActivity(),R.layout.confirm_delete_content_layout, null);
                    TextView msg = (TextView) contents.findViewById(R.id.confir_dialog_message);
                    msg.setText(getString(R.string.published_delete_content,mSelectedItems.size()));
                    YueQiuDialogBuilder builder = new YueQiuDialogBuilder(getActivity());
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
}
