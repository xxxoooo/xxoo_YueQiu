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
    public long insertPublishInfo(PublishedInfo info) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.PublishInfoTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        values.put(DatabaseConstant.PublishInfoTable.TYPE,info.getType());
        values.put(DatabaseConstant.PublishInfoTable.START_NO,info.getStart_no());
        values.put(DatabaseConstant.PublishInfoTable.END_NO,info.getEnd_no());
        values.put(DatabaseConstant.PublishInfoTable.COUNT,info.getSumCount());

        mDB = mDBUtils.getWritableDatabase();
        long result = mDB.insert(DatabaseConstant.PublishInfoTable.TABLE, null, values);
        return result;
    }

    @Override
    public long insertPublishItemInfo(PublishedInfo info) {
        mDB = mDBUtils.getWritableDatabase();
        long result = 0;
        mDB.beginTransaction();
        try {
            for (int i = 0; i < info.mList.size(); i++) {
                ContentValues values = new ContentValues();
                PublishedInfo.PublishedItemInfo itemInfo = info.mList.get(i);
                values.put(DatabaseConstant.PublishInfoItemTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
                values.put(DatabaseConstant.PublishInfoItemTable.TABLE_ID, itemInfo.getTable_id());
                values.put(DatabaseConstant.PublishInfoItemTable.TYPE, info.getType());
                values.put(DatabaseConstant.PublishInfoItemTable.TITLE, itemInfo.getTitle());
                values.put(DatabaseConstant.PublishInfoItemTable.CONTENT, itemInfo.getContent());
                values.put(DatabaseConstant.PublishInfoItemTable.DATETIME, itemInfo.getDateTime());
                result = mDB.insert(DatabaseConstant.PublishInfoItemTable.TABLE, null, values);
            }
            mDB.setTransactionSuccessful();
            return result;
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            mDB.endTransaction();
        }
        return -1;
    }

    @Override
    public long updatePublishInfo(PublishedInfo info) {
        mDB = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.PublishInfoTable.START_NO,info.getStart_no());
        values.put(DatabaseConstant.PublishInfoTable.END_NO,info.getEnd_no());
        values.put(DatabaseConstant.PublishInfoTable.COUNT,info.getSumCount());

        long result = mDB.update(DatabaseConstant.PublishInfoTable.TABLE,values, DatabaseConstant.PublishInfoTable.USER_ID + "=? and " +
                        DatabaseConstant.PublishInfoTable.TYPE + "=?",
                new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()),String.valueOf(info.getType())});

        return result;
    }

    @Override
    public List<Long> updatePublishedItemInfo(PublishedInfo info) {
        mDB = mDBUtils.getWritableDatabase();
        List<Long> list = new ArrayList<Long>();
        mDB.beginTransaction();
        try {
            for (int i = 0; i < info.mList.size(); i++) {
                ContentValues values = new ContentValues();
                PublishedInfo.PublishedItemInfo itemInfo =  info.mList.get(i);
                values.put(DatabaseConstant.PublishInfoItemTable.TABLE_ID, itemInfo.getTable_id());
                values.put(DatabaseConstant.PublishInfoItemTable.TITLE, itemInfo.getTitle());
                values.put(DatabaseConstant.PublishInfoItemTable.CONTENT, itemInfo.getContent());
                values.put(DatabaseConstant.PublishInfoItemTable.DATETIME, itemInfo.getDateTime());
                long result = mDB.update(DatabaseConstant.PublishInfoItemTable.TABLE, values, DatabaseConstant.PublishInfoItemTable.USER_ID + "=? and " +
                                DatabaseConstant.PublishInfoItemTable.TABLE_ID + "=?",
                        new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()), String.valueOf(itemInfo.getTable_id())});
                list.add(result);
            }
            mDB.setTransactionSuccessful();

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            mDB.endTransaction();
        }
        return list;

    }

    @Override
    public boolean isExistPublishedInfo(int type) {
        mDB = mDBUtils.getReadableDatabase();
        Cursor cursor = mDB.query(DatabaseConstant.PublishInfoTable.TABLE,null,DatabaseConstant.PublishInfoTable.USER_ID + "=? and "
                + DatabaseConstant.PublishInfoTable.TYPE + "=?",new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()),
                String.valueOf(type)},null,null,null);
        if(cursor == null || cursor.getCount() == 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    @Override
    public boolean isExistPublishedItemInfo(int tableId,int type) {
        mDB = mDBUtils.getReadableDatabase();
        Cursor cursor = mDB.query(DatabaseConstant.PublishInfoItemTable.TABLE,null,DatabaseConstant.PublishInfoItemTable.USER_ID + "=? and " +
                        DatabaseConstant.PublishInfoItemTable.TABLE_ID + "=? and " + DatabaseConstant.PublishInfoItemTable.TYPE + "=?",
                new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()),String.valueOf(tableId),String.valueOf(type)},
                null,null,null);
        if(cursor == null || cursor.getCount() == 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    @Override
    public PublishedInfo getPublishedInfo(String userId, int type,int start,int number) {
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
                + " and " +  DatabaseConstant.PublishInfoItemTable.TYPE + "=?"  + " order by " + DatabaseConstant.PublishInfoItemTable.TABLE_ID + " desc limit " +
                start + "," + number;
        Cursor itemCursor = mDB.rawQuery(itemSql,new String[]{userId,String.valueOf(type)});
        if(itemCursor != null && itemCursor.getCount() != 0 ){
            itemCursor.moveToFirst();
            do{
                PublishedInfo.PublishedItemInfo item = info.new PublishedItemInfo();
                item.setType(itemCursor.getInt(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoItemTable.TYPE)));
                item.setTitle(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoItemTable.TITLE)));
                item.setContent(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoItemTable.CONTENT)));
                item.setDateTime(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoItemTable.DATETIME)));
                item.setTable_id(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.PublishInfoItemTable.TABLE_ID)));
                info.mList.add(item);
            }while(itemCursor.moveToNext());
        }
        itemCursor.close();
        return info;
    }
}
