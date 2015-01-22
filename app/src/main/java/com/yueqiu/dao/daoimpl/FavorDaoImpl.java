package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.FavorInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.FavorDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/1/13.
 */
public class FavorDaoImpl implements FavorDao {

    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDB;

    public FavorDaoImpl(Context context){
        mContext = context;
        mDBUtils = DBUtils.getInstance(mContext);
    }
    @Override
    public synchronized long insertFavorInfo(FavorInfo info) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.FavorInfoTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        values.put(DatabaseConstant.FavorInfoTable.TYPE,info.getType());
        values.put(DatabaseConstant.FavorInfoTable.START_NO,info.getStart_no());
        values.put(DatabaseConstant.FavorInfoTable.END_NO,info.getEnd_no());
        values.put(DatabaseConstant.FavorInfoTable.COUNT,info.getCount());

        mDB = mDBUtils.getWritableDatabase();
        long result = -1;
        mDB.beginTransaction();
        try {
            result = mDB.insert(DatabaseConstant.FavorInfoTable.TABLE, null, values);
            mDB.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mDB.endTransaction();
        }
        return result;
    }

    @Override
    public synchronized long insertFavorItemInfo(FavorInfo info) {
        mDB = mDBUtils.getWritableDatabase();
        long result = 0;
        mDB.beginTransaction();
        try {
            for (int i = 0; i < info.mList.size(); i++) {
                ContentValues values = new ContentValues();
                FavorInfo.FavorItemInfo itemInfo = info.mList.get(i);
                values.put(DatabaseConstant.FavorInfoItemTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
                values.put(DatabaseConstant.FavorInfoItemTable.TABLE_ID, itemInfo.getTable_id());
                values.put(DatabaseConstant.FavorInfoItemTable.TYPE, info.getType());
                values.put(DatabaseConstant.FavorInfoItemTable.IMAGE_URL, itemInfo.getImage_url());
                values.put(DatabaseConstant.FavorInfoItemTable.TITLE, itemInfo.getTitle());
                values.put(DatabaseConstant.FavorInfoItemTable.CONTENT, itemInfo.getContent());
                values.put(DatabaseConstant.FavorInfoItemTable.DATETIME, itemInfo.getDateTime());
                values.put(DatabaseConstant.FavorInfoItemTable.USER_NAME,itemInfo.getUserName());
                result = mDB.insert(DatabaseConstant.FavorInfoItemTable.TABLE, null, values);
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
    public synchronized long updateFavorInfo(FavorInfo info) {
        mDB = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.FavorInfoTable.START_NO,info.getStart_no());
        values.put(DatabaseConstant.FavorInfoTable.END_NO,info.getEnd_no());
        values.put(DatabaseConstant.FavorInfoTable.COUNT,info.getCount());
        values.put(DatabaseConstant.FavorInfoTable.TYPE,info.getType());

        long result = -1;
        mDB.beginTransaction();
        try {
            result = mDB.update(DatabaseConstant.FavorInfoTable.TABLE, values, DatabaseConstant.FavorInfoTable.USER_ID + "=? and " +
                            DatabaseConstant.FavorInfoTable.TYPE + "=?",
                    new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()), String.valueOf(info.getType())});
            mDB.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            mDB.endTransaction();
        }
        return result;
    }

    @Override
    public synchronized List<Long> updateFavorItemInfo(FavorInfo info) {
        mDB = mDBUtils.getWritableDatabase();
        List<Long> list = new ArrayList<Long>();
        mDB.beginTransaction();
        try {
            for (int i = 0; i < info.mList.size(); i++) {
                ContentValues values = new ContentValues();
                FavorInfo.FavorItemInfo itemInfo =  info.mList.get(i);
                values.put(DatabaseConstant.FavorInfoItemTable.TABLE_ID, itemInfo.getTable_id());
                values.put(DatabaseConstant.FavorInfoItemTable.IMAGE_URL, itemInfo.getImage_url());
                values.put(DatabaseConstant.FavorInfoItemTable.TITLE, itemInfo.getTitle());
                values.put(DatabaseConstant.FavorInfoItemTable.CONTENT, itemInfo.getContent());
                values.put(DatabaseConstant.FavorInfoItemTable.DATETIME, itemInfo.getDateTime());
                values.put(DatabaseConstant.FavorInfoItemTable.USER_NAME,itemInfo.getUserName());
                values.put(DatabaseConstant.FavorInfoItemTable.TYPE,itemInfo.getType());
                long result = mDB.update(DatabaseConstant.FavorInfoItemTable.TABLE, values, DatabaseConstant.FavorInfoItemTable.USER_ID + "=? and " +
                                DatabaseConstant.FavorInfoItemTable.TABLE_ID + "=? and " + DatabaseConstant.FavorInfoItemTable.TYPE + "=?",
                        new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()), String.valueOf(itemInfo.getTable_id()),String.valueOf(itemInfo.getType())});
                list.add(result);
            }
            mDB.setTransactionSuccessful();
            return list;
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            mDB.endTransaction();
        }
        return null;
    }

    @Override
    public synchronized boolean isExistFavorInfo(int type) {
        mDB = mDBUtils.getReadableDatabase();
        Cursor cursor = mDB.query(DatabaseConstant.FavorInfoTable.TABLE,null,DatabaseConstant.FavorInfoTable.USER_ID + "=? and "
                + DatabaseConstant.FavorInfoTable.TYPE + "=?",new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()),
                String.valueOf(type)},null,null,null);
        if(cursor == null || cursor.getCount() == 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    @Override
    public boolean isExistFavorItemInfo(int tableId, int type) {
        mDB = mDBUtils.getReadableDatabase();
        Cursor cursor = mDB.query(DatabaseConstant.FavorInfoItemTable.TABLE,null,DatabaseConstant.FavorInfoItemTable.USER_ID + "=? and " +
                        DatabaseConstant.FavorInfoItemTable.TABLE_ID + "=? and " + DatabaseConstant.FavorInfoItemTable.TYPE + "=?",
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
    public synchronized FavorInfo getFavorInfo(String userId, int type,int start,int num) {
        mDB = mDBUtils.getReadableDatabase();
        FavorInfo info = new FavorInfo();
        String infoSql = "SELECT * FROM " + DatabaseConstant.FavorInfoTable.TABLE + " where " + DatabaseConstant.FavorInfoTable.USER_ID + "=?"
                + " and " + DatabaseConstant.FavorInfoTable.TYPE + "=?";
        Cursor infoCursor = mDB.rawQuery(infoSql,new String[]{userId,String.valueOf(type)});
        if(infoCursor != null && infoCursor.getCount() != 0){
            infoCursor.moveToFirst();
            do{
                info.setUser_id(YueQiuApp.sUserInfo.getUser_id());
                info.setStart_no(infoCursor.getInt(infoCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoTable.START_NO)));
                info.setEnd_no(infoCursor.getInt(infoCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoTable.END_NO)));
                info.setType(infoCursor.getInt(infoCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoTable.TYPE)));
                info.setCount(infoCursor.getInt(infoCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoTable.COUNT)));
            }while(infoCursor.moveToNext());
        }
        infoCursor.close();


        String itemSql = "SELECT * FROM " + DatabaseConstant.FavorInfoItemTable.TABLE + " where " + DatabaseConstant.FavorInfoItemTable.USER_ID + "=?"
                + " and " +  DatabaseConstant.FavorInfoItemTable.TYPE + "=?" + " order by " + DatabaseConstant.FavorInfoItemTable.TABLE_ID + " desc limit "
                + start + "," + num;
        Cursor itemCursor = mDB.rawQuery(itemSql,new String[]{userId,String.valueOf(type)});
        if(itemCursor != null && itemCursor.getCount() != 0 ){
            itemCursor.moveToFirst();
            do{
                FavorInfo.FavorItemInfo item = info.new FavorItemInfo();
                item.setType(itemCursor.getInt(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.TYPE)));
                item.setTitle(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.TITLE)));
                item.setContent(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.CONTENT)));
                item.setDateTime(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.DATETIME)));
                item.setImage_url(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.IMAGE_URL)));
                item.setTable_id(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.TABLE_ID)));
                item.setUserName(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.USER_NAME)));
                info.mList.add(item);
            }while(itemCursor.moveToNext());
        }
        itemCursor.close();
        return info;
    }
}
