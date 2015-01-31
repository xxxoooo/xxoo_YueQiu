package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.PublishedDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/1/12.
 */
public class PublishedImpl implements PublishedDao{

    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDB;

    public PublishedImpl(Context context){
        mContext = context;
        mDBUtils = DBUtils.getInstance(mContext);
    }

    @Override
    public synchronized long insertPublishInfo(List<PublishedInfo> list) {
        mDB = mDBUtils.getWritableDatabase();
        long result = -1;
        mDB.beginTransaction();
        try {
            for (PublishedInfo info : list) {
                ContentValues values = new ContentValues();
                //TODO:img_url没有建立，有可能会不要头像
                values.put(DatabaseConstant.PublishInfoTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
                values.put(DatabaseConstant.PublishInfoTable.TABLE_ID, info.getTable_id());
                values.put(DatabaseConstant.PublishInfoTable.TYPE, info.getType());
                values.put(DatabaseConstant.PublishInfoTable.TITLE, info.getTitle());
                values.put(DatabaseConstant.PublishInfoTable.CONTENT, info.getContent());
                values.put(DatabaseConstant.PublishInfoTable.DATETIME, info.getDateTime());
                result = mDB.insert(DatabaseConstant.PublishInfoTable.TABLE, null, values);
            }
            mDB.setTransactionSuccessful();
            return result;
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            mDB.endTransaction();
        }
        return result;
    }

    @Override
    public synchronized long updatePublishInfo(List<PublishedInfo> list) {
        mDB = mDBUtils.getWritableDatabase();
        long result = -1;
        mDB.beginTransaction();
        try {
            for (PublishedInfo info : list) {
                ContentValues values = new ContentValues();
                values.put(DatabaseConstant.PublishInfoTable.TABLE_ID, info.getTable_id());
                values.put(DatabaseConstant.PublishInfoTable.TITLE, info.getTitle());
                values.put(DatabaseConstant.PublishInfoTable.CONTENT, info.getContent());
                values.put(DatabaseConstant.PublishInfoTable.DATETIME, info.getDateTime());
                values.put(DatabaseConstant.PublishInfoTable.TYPE,info.getType());

                result = mDB.update(DatabaseConstant.PublishInfoTable.TABLE, values, DatabaseConstant.PublishInfoTable.USER_ID + "=? and " +
                                DatabaseConstant.PublishInfoTable.TABLE_ID + "=? and " + DatabaseConstant.PublishInfoTable.TYPE + "=?",
                        new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()), String.valueOf(info.getTable_id()),String.valueOf(info.getType())});
            }
            mDB.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            mDB.endTransaction();
        }
        return result;
    }

    @Override
    public synchronized List<PublishedInfo> getPublishedInfo(int userId, int type,int start,int number) {
        mDB = mDBUtils.getReadableDatabase();
        List<PublishedInfo> list = new ArrayList<PublishedInfo>();
        String itemSql = "SELECT * FROM " + DatabaseConstant.PublishInfoTable.TABLE + " where " + DatabaseConstant.PublishInfoTable.USER_ID + "=?"
                + " and " +  DatabaseConstant.PublishInfoTable.TYPE + "=?"  + " order by " + DatabaseConstant.PublishInfoTable.TABLE_ID + " desc limit " +
                start + "," + number;
        Cursor itemCursor = mDB.rawQuery(itemSql,new String[]{String.valueOf(userId),String.valueOf(type)});
        if(itemCursor != null && itemCursor.getCount() != 0 ){
            itemCursor.moveToFirst();
            do{
                PublishedInfo info = new PublishedInfo();
                info.setType(itemCursor.getInt(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.TYPE)));
                info.setTitle(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.TITLE)));
                info.setContent(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.CONTENT)));
                info.setDateTime(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.DATETIME)));
                info.setTable_id(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.TABLE_ID)));
                list.add(info);
            }while(itemCursor.moveToNext());
        }
        itemCursor.close();
        return list;
    }

    @Override
    public List<PublishedInfo> getAllPublishedInfo(int userId) {
        mDB = mDBUtils.getReadableDatabase();
        List<PublishedInfo> list = new ArrayList<PublishedInfo>();
        String itemSql = "SELECT * FROM " + DatabaseConstant.PublishInfoTable.TABLE + " where " + DatabaseConstant.PublishInfoTable.USER_ID + "=?"
                + " order by " + DatabaseConstant.PublishInfoTable.TABLE_ID + " desc";
        Cursor itemCursor = mDB.rawQuery(itemSql,new String[]{String.valueOf(userId)});
        if(itemCursor != null && itemCursor.getCount() != 0 ){
            itemCursor.moveToFirst();
            do{
                PublishedInfo info = new PublishedInfo();
                info.setType(itemCursor.getInt(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.TYPE)));
                info.setTitle(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.TITLE)));
                info.setContent(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.CONTENT)));
                info.setDateTime(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.DATETIME)));
                info.setTable_id(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoTable.TABLE_ID)));
                list.add(info);
            }while(itemCursor.moveToNext());
        }
        itemCursor.close();
        return list;
    }
}
