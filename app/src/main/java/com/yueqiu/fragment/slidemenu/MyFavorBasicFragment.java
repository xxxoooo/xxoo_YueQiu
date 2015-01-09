package com.yueqiu.fragment.slidemenu;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.XListView;
import com.yueqiu.view.contacts.LoadingView;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yueqiu.bean.PublishedInfo.*;

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
    private FavorBasicAdapter mAdapter;
    private int mType;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDB;
    private PublishedInfo mPublishedInfo;
    private List<PublishedInfo.PublishedItemInfo> mList;

    private Map<String,Integer> mResultMap = new HashMap<String, Integer>();
    private Map<String,String> mRequestParamMap = new HashMap<String, String>();

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
        mEmptyView = (TextView) mView.findViewById(R.id.favor_is_empty);
        mPreText = (TextView) mView.findViewById(R.id.pre_text);
        mPreProgress = (ProgressBar) mView.findViewById(R.id.pre_progress);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(getActivity()).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);


        if(Utils.networkAvaiable(getActivity())){
            requestPublishedInfo();
        }else{
            if(isExistPublishedInfo()){
                mPublishedInfo = getPublishedInfo(String.valueOf(YueQiuApp.sUserInfo.getUser_id()),mType);
                mHandler.obtainMessage(GET_SUCCESS, mPublishedInfo).sendToTarget();
            }else{
                mHandler.obtainMessage(NO_RESULT).sendToTarget();
            }
        }

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
                                published.mList.add(itemInfo);
                            }
                            mHandler.obtainMessage(GET_SUCCESS, published).sendToTarget();
                            insertPublishInfo(published);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
    private boolean isExistPublishedInfo(){
        mDBUtils = new DBUtils(getActivity(),DatabaseConstant.PublishInfoTable.CRAETE_SQL);
        mDB = mDBUtils.getReadableDatabase();
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
