package com.yueqiu.fragment.slidemenu;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.FavorBasicAdapter;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.db.DBUtils;
import com.yueqiu.util.AsyncTaskUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.XListView;
import com.yueqiu.view.YueQiuDialogBuilder;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by wangyun on 14/12/30.
 */
public class MyFavorBasicFragment extends Fragment implements XListView.IXListViewListener{
    private static final int GET_SUCCESS = 0;
    private static final int NO_RESULT   = -1;
    private View mView;
    private XListView mListView;
    private TextView mEmptyView,mPreText;
    private ProgressBar mPreProgress;
    private Drawable mProgressDrawable;
    private SearchView mSearchView;
    private FavorBasicAdapter mAdapter;
    private int mType;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDB;
    private PublishedInfo mPublishedInfo;
    private List<PublishedInfo.PublishedItemInfo> mList;

    private Map<String,Integer> mResultMap = new HashMap<String, Integer>();
    private Map<String,String> mRequestParamMap = new HashMap<String, String>();
    private boolean isExistPublished;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_favor_basic_layout,null);
        Bundle mArgs = getArguments();
        mType = mArgs.getInt("type");


        mListView = (XListView) mView.findViewById(R.id.favor_basic_listView);
        mListView.setPullLoadEnable(true);
        mListView.setXListViewListener(this);

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //上下文模式
        mListView.setMultiChoiceModeListener(new ActionModeCallback());

        mEmptyView = (TextView) mView.findViewById(R.id.favor_is_empty);
        mPreText = (TextView) mView.findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);

        isExistPublished = isExistPublishedInfo();

        if(Utils.networkAvaiable(getActivity())){
            requestPublishedInfo();
        }else{
            if(isExistPublished){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mPublishedInfo = getPublishedInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()),mType);
                        mHandler.obtainMessage(GET_SUCCESS, mPublishedInfo).sendToTarget();
                    }
                }).start();

            }else{
                mHandler.obtainMessage(NO_RESULT).sendToTarget();
            }
        }
        setHasOptionsMenu(true);

        return mView;
    }


    private void requestPublishedInfo(){
        mResultMap.put(DatabaseConstant.UserTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        mResultMap.put(HttpConstants.Published.TYPE,mType);
        mResultMap.put(HttpConstants.Published.STAR_NO,0);
        mResultMap.put(HttpConstants.Published.END_NO, 9);

        mRequestParamMap.put(PublicConstant.URL,HttpConstants.Published.URL);
        mRequestParamMap.put(PublicConstant.METHOD,HttpConstants.RequestMethod.GET);

        new RequestAsyncTask(mResultMap,mPreProgress,mPreText).execute(mRequestParamMap);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case GET_SUCCESS:
                    mPublishedInfo = (PublishedInfo) msg.obj;
                    mList = mPublishedInfo.mList;
                    mAdapter = new FavorBasicAdapter(getActivity(),mList);
                    mListView.setAdapter(mAdapter);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(isExistPublished){
                                updatePublishInfo(mPublishedInfo);
                            }else {
                                insertPublishInfo(mPublishedInfo);
                            }
                        }
                    }).start();

                    break;
                case NO_RESULT:
                    mEmptyView.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                    mListView.stopRefresh();
                    mListView.stopLoadMore();
                    mListView.setRefreshTime("刚刚");

            }
        }, 2000);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                    mListView.stopRefresh();
                    mListView.stopLoadMore();
                    mListView.setRefreshTime("刚刚");

            }
        }, 2000);
    }


    private class RequestAsyncTask extends AsyncTaskUtil<Integer>{


        public RequestAsyncTask(Map<String, Integer> map, ProgressBar progressBar, TextView textView) {
            super(map, progressBar, textView);
        }

        @Override
        protected void onPostExecute(JSONObject jsonResult) {
            super.onPostExecute(jsonResult);
            try {
                if(!jsonResult.isNull("code")) {
                    if (jsonResult.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                        PublishedInfo published = new PublishedInfo();
                        if (jsonResult.getJSONObject("result") != null) {
                            published.setStart_no(jsonResult.getJSONObject("result").getInt("start_no"));
                            published.setEnd_no(jsonResult.getJSONObject("result").getInt("end_no"));
                            published.setSumCount(jsonResult.getJSONObject("result").getInt("count"));
                            JSONArray list_data = jsonResult.getJSONObject("result").getJSONArray("list_data");

                            for (int i = 0; i < list_data.length(); i++) {
                                PublishedInfo.PublishedItemInfo itemInfo = published.new PublishedItemInfo();
                                itemInfo.setTable_id(list_data.getJSONObject(i).getString("id"));
                                itemInfo.setImage_url(list_data.getJSONObject(i).getString("img_url"));
                                itemInfo.setTitle(list_data.getJSONObject(i).getString("title"));
                                itemInfo.setContent(list_data.getJSONObject(i).getString("content"));
                                itemInfo.setDateTime(list_data.getJSONObject(i).getString("create_time"));
                                itemInfo.setChecked(false);
                                published.mList.add(itemInfo);
                            }
                            mHandler.obtainMessage(GET_SUCCESS, published).sendToTarget();
                        }
                    }else{
                        mHandler.obtainMessage(NO_RESULT).sendToTarget();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ActionModeCallback implements ListView.MultiChoiceModeListener{

        private View mCustomActionBarView;
        private TextView mActionModeTitle,mActionModeSelCount;
        private HashSet<Integer> mSelectedItems;


        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            mSelectedItems.add(position);
            final int checkedCount = mListView.getCheckedItemCount();
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
            mActionModeTitle.setText(getActivity().getString(R.string.published_action_mode_title));
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
            mListView.getAdapter();
            mAdapter.unCheckAll();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void insertPublishInfo(PublishedInfo info){
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.PublishInfoTable.USER_ID,YueQiuApp.sUserInfo.getUser_id());
        values.put(DatabaseConstant.PublishInfoTable.TYPE,mType);
        values.put(DatabaseConstant.PublishInfoTable.START_NO,info.getStart_no());
        values.put(DatabaseConstant.PublishInfoTable.END_NO,info.getEnd_no());
        values.put(DatabaseConstant.PublishInfoTable.COUNT,info.getSumCount());

        mDBUtils = new DBUtils(getActivity(),DatabaseConstant.PublishInfoTable.CRAETE_SQL);
        mDB = mDBUtils.getWritableDatabase();
        mDBUtils.onUpgrade(mDB,DatabaseConstant.VERSION,DatabaseConstant.VERSION);
        mDB.insert(DatabaseConstant.PublishInfoTable.TABLE, null, values);

        insertPublishItemInfo(info);
    }

    private void insertPublishItemInfo(PublishedInfo info){
        mDBUtils = new DBUtils(getActivity(),DatabaseConstant.PublishInfoItemTable.CREATE_URL);
        mDB = mDBUtils.getWritableDatabase();
        mDBUtils.onUpgrade(mDB,DatabaseConstant.VERSION,DatabaseConstant.VERSION);
        for(int i=0;i<info.mList.size();i++) {
            ContentValues values = new ContentValues();
            PublishedInfo.PublishedItemInfo itemInfo = info.mList.get(i);
            values.put(DatabaseConstant.PublishInfoItemTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
            values.put(DatabaseConstant.PublishInfoItemTable.TABLE_ID, itemInfo.getTable_id());
            values.put(DatabaseConstant.PublishInfoItemTable.TYPE, mType);
            values.put(DatabaseConstant.PublishInfoItemTable.IMAGE_URL, itemInfo.getImage_url());
            values.put(DatabaseConstant.PublishInfoItemTable.TITLE, itemInfo.getTitle());
            values.put(DatabaseConstant.PublishInfoItemTable.CONTENT, itemInfo.getContent());
            values.put(DatabaseConstant.PublishInfoItemTable.DATETIME, itemInfo.getDateTime());
            mDB.insert(DatabaseConstant.PublishInfoItemTable.TABLE,null,values);
        }
    }

    private void updatePublishInfo(PublishedInfo info){
        mDBUtils = new DBUtils(getActivity(),DatabaseConstant.PublishInfoItemTable.CREATE_URL);
        mDB = mDBUtils.getWritableDatabase();
        mDBUtils.onUpgrade(mDB,DatabaseConstant.VERSION,DatabaseConstant.VERSION);

        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.PublishInfoTable.START_NO,info.getStart_no());
        values.put(DatabaseConstant.PublishInfoTable.END_NO,info.getEnd_no());
        values.put(DatabaseConstant.PublishInfoTable.COUNT,info.getSumCount());

        mDB.update(DatabaseConstant.PublishInfoTable.TABLE,values, DatabaseConstant.PublishInfoTable.USER_ID + "=? and " +
                DatabaseConstant.PublishInfoTable.TYPE + "=?",
                new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()),String.valueOf(info.getType())});

        for(int i=0;i<info.mList.size();i++){
            PublishedInfo.PublishedItemInfo item =  info.mList.get(i);
            if(isExistPublishedItemInfo(Integer.valueOf(item.getTable_id()))){
                updatePublishedItemInfo(info);
            }else{
                insertPublishItemInfo(info);
            }
        }

    }
    private void updatePublishedItemInfo(PublishedInfo info){
        mDBUtils = new DBUtils(getActivity(),DatabaseConstant.PublishInfoItemTable.CREATE_URL);
        mDB = mDBUtils.getWritableDatabase();
        mDBUtils.onUpgrade(mDB,DatabaseConstant.VERSION,DatabaseConstant.VERSION);

        for(int i=0;i<info.mList.size();i++) {
            ContentValues values = new ContentValues();
            PublishedInfo.PublishedItemInfo itemInfo = info.mList.get(i);
            values.put(DatabaseConstant.PublishInfoItemTable.TABLE_ID, itemInfo.getTable_id());
            values.put(DatabaseConstant.PublishInfoItemTable.IMAGE_URL, itemInfo.getImage_url());
            values.put(DatabaseConstant.PublishInfoItemTable.TITLE, itemInfo.getTitle());
            values.put(DatabaseConstant.PublishInfoItemTable.CONTENT, itemInfo.getContent());
            values.put(DatabaseConstant.PublishInfoItemTable.DATETIME, itemInfo.getDateTime());
            mDB.update(DatabaseConstant.PublishInfoItemTable.TABLE,values,DatabaseConstant.PublishInfoItemTable.USER_ID + "=? and " +
                DatabaseConstant.PublishInfoItemTable.TABLE_ID + "=?",
                    new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()),String.valueOf(itemInfo.getTable_id())});
        }
    }
    private boolean isExistPublishedInfo(){
        mDBUtils = new DBUtils(getActivity(),DatabaseConstant.PublishInfoTable.CRAETE_SQL);
        mDB = mDBUtils.getReadableDatabase();
        mDBUtils.onUpgrade(mDB,DatabaseConstant.VERSION,DatabaseConstant.VERSION);
        Cursor cursor = mDB.query(DatabaseConstant.PublishInfoTable.TABLE,null,DatabaseConstant.PublishInfoTable.USER_ID + "=? and "
                            + DatabaseConstant.PublishInfoTable.TYPE + "=?",new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()),
                            String.valueOf(mType)},null,null,null);
        if(cursor == null || cursor.getCount() == 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }
    private boolean isExistPublishedItemInfo(int tableId){
        mDBUtils = new DBUtils(getActivity(),DatabaseConstant.PublishInfoItemTable.CREATE_URL);
        mDB = mDBUtils.getReadableDatabase();
        mDBUtils.onUpgrade(mDB,DatabaseConstant.VERSION,DatabaseConstant.VERSION);
        Cursor cursor = mDB.query(DatabaseConstant.PublishInfoItemTable.TABLE,null,DatabaseConstant.PublishInfoItemTable.USER_ID + "=? and " +
                DatabaseConstant.PublishInfoItemTable.TABLE_ID + "=?",new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()),String.valueOf(tableId)},
                null,null,null);
        if(cursor == null || cursor.getCount() == 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }
    private PublishedInfo getPublishedInfo(String userId,int type){
        mDBUtils = new DBUtils(getActivity(),DatabaseConstant.PublishInfoTable.CRAETE_SQL);
        mDB = mDBUtils.getReadableDatabase();
        PublishedInfo info = new PublishedInfo();
        String infoSql = "SELECT * FROM " + DatabaseConstant.PublishInfoTable.TABLE + " where " + DatabaseConstant.PublishInfoTable.USER_ID + "=?"
                + " and " + DatabaseConstant.PublishInfoTable.TYPE + "=?";
        Cursor infoCursor = mDB.rawQuery(infoSql,new String[]{userId,String.valueOf(type)});
        if(infoCursor != null && infoCursor.getCount() != 0){
            infoCursor.moveToFirst();
            do{
                info.setUser_id(YueQiuApp.sUserInfo.getUser_id());
                info.setStart_no(infoCursor.getInt(infoCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.START_NO)));
                info.setEnd_no(infoCursor.getInt(infoCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.END_NO)));
                info.setType(infoCursor.getInt(infoCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.TYPE)));
                info.setSumCount(infoCursor.getInt(infoCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.COUNT)));
            }while(infoCursor.moveToNext());
        }
        infoCursor.close();


        String itemSql = "SELECT * FROM " + DatabaseConstant.PublishInfoItemTable.TABLE + " where " + DatabaseConstant.PublishInfoItemTable.USER_ID + "=?"
                + " and " +  DatabaseConstant.PublishInfoItemTable.TYPE + "=?" ;
        Cursor itemCursor = mDB.rawQuery(itemSql,new String[]{userId,String.valueOf(type)});
        if(itemCursor != null || itemCursor.getCount() != 0 ){
            itemCursor.moveToFirst();
            do{
                PublishedInfo.PublishedItemInfo item = info.new PublishedItemInfo();
                item.setType(itemCursor.getInt(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoItemTable.TYPE)));
                item.setTitle(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoItemTable.TITLE)));
                item.setContent(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoItemTable.CONTENT)));
                item.setDateTime(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoItemTable.DATETIME)));
                item.setImage_url(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoItemTable.IMAGE_URL)));
                item.setTable_id(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoItemTable.TABLE_ID)));
                info.mList.add(item);
            }while(itemCursor.moveToNext());
        }
        itemCursor.close();
        return info;
    }
}
