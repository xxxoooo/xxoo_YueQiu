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
    public long insertFavorInfo(List<FavorInfo> list) {
        mDB = mDBUtils.getWritableDatabase();
        long result = -1;
        mDB.beginTransaction();
        try {
            for (FavorInfo info : list) {
                ContentValues values = new ContentValues();
                values.put(DatabaseConstant.FavorInfoItemTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
                values.put(DatabaseConstant.FavorInfoItemTable.TABLE_ID, info.getTable_id());
                values.put(DatabaseConstant.FavorInfoItemTable.TYPE, info.getType());
                values.put(DatabaseConstant.FavorInfoItemTable.TITLE, info.getTitle());
                values.put(DatabaseConstant.FavorInfoItemTable.CONTENT, info.getContent());
                values.put(DatabaseConstant.FavorInfoItemTable.DATETIME, info.getCreateTime());
                values.put(DatabaseConstant.FavorInfoItemTable.USER_NAME,info.getUserName());
                //TODO:加入缓存后可定得加入这个字段
//                values.put(DatabaseConstant.FavorInfoItemTable.SUBTYPE,info.getSubType());
                result = mDB.insert(DatabaseConstant.FavorInfoItemTable.TABLE, null, values);
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
    public long updateFavorInfo(List<FavorInfo> list) {
        long result = -1;
        mDB = mDBUtils.getWritableDatabase();
        mDB.beginTransaction();
        try {
            for (FavorInfo info :list) {
                ContentValues values = new ContentValues();
                values.put(DatabaseConstant.FavorInfoItemTable.TABLE_ID, info.getTable_id());
                values.put(DatabaseConstant.FavorInfoItemTable.TITLE, info.getTitle());
                values.put(DatabaseConstant.FavorInfoItemTable.CONTENT, info.getContent());
                values.put(DatabaseConstant.FavorInfoItemTable.DATETIME, info.getCreateTime());
                values.put(DatabaseConstant.FavorInfoItemTable.USER_NAME,info.getUserName());
                values.put(DatabaseConstant.FavorInfoItemTable.TYPE,info.getType());
                //TODO:加入缓存后可定得加入这个字段
//                values.put(DatabaseConstant.FavorInfoItemTable.SUBTYPE,info.getSubType());
                result = mDB.update(DatabaseConstant.FavorInfoItemTable.TABLE, values, DatabaseConstant.FavorInfoItemTable.USER_ID + "=? and " +
                                DatabaseConstant.FavorInfoItemTable.TABLE_ID + "=? and " + DatabaseConstant.FavorInfoItemTable.TYPE + "=?",
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
    public List<FavorInfo> getAllFavor(int userId) {
        mDB = mDBUtils.getReadableDatabase();
        List<FavorInfo> list = new ArrayList<FavorInfo>();
        String itemSql = "SELECT * FROM " + DatabaseConstant.FavorInfoItemTable.TABLE + " where " + DatabaseConstant.FavorInfoItemTable.USER_ID + "=?"
                + " order by " + DatabaseConstant.FavorInfoItemTable.TABLE_ID + " desc";
        Cursor itemCursor = mDB.rawQuery(itemSql,new String[]{String.valueOf(userId)});
        if(itemCursor != null && itemCursor.getCount() != 0 ){
            itemCursor.moveToFirst();
            do{
                FavorInfo item = new FavorInfo();
                item.setType(itemCursor.getInt(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.TYPE)));
                item.setTitle(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.TITLE)));
                item.setContent(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.CONTENT)));
                item.setCreateTime(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.DATETIME)));
                item.setTable_id(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.TABLE_ID)));
                item.setUserName(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.USER_NAME)));
                //TODO:加入缓存后可定得加入这个字段
//                item.setSubType(itemCursor.getInt(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.SUBTYPE)));
                list.add(item);
            }while(itemCursor.moveToNext());
        }
        itemCursor.close();
        return list;
    }

    @Override
    public List<FavorInfo> getFavorLimit(int userId, int type, int start, int num) {
        mDB = mDBUtils.getReadableDatabase();
        List<FavorInfo> list = new ArrayList<FavorInfo>();
        String itemSql = "SELECT * FROM " + DatabaseConstant.FavorInfoItemTable.TABLE + " where " + DatabaseConstant.FavorInfoItemTable.USER_ID + "=?"
                + " and " +  DatabaseConstant.FavorInfoItemTable.TYPE + "=?" + " order by " + DatabaseConstant.FavorInfoItemTable.TABLE_ID + " desc limit "
                + start + "," + num;
        Cursor itemCursor = mDB.rawQuery(itemSql,new String[]{String.valueOf(userId),String.valueOf(type)});
        if(itemCursor != null && itemCursor.getCount() != 0 ){
            itemCursor.moveToFirst();
            do{
                FavorInfo item = new FavorInfo();
                item.setType(itemCursor.getInt(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.TYPE)));
                item.setTitle(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.TITLE)));
                item.setContent(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.CONTENT)));
                item.setCreateTime(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.DATETIME)));
                item.setTable_id(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.TABLE_ID)));
                item.setUserName(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.USER_NAME)));
                //TODO:加入缓存后可定得加入这个字段
//                item.setSubType(itemCursor.getInt(itemCursor.getColumnIndexOrThrow(DatabaseConstant.FavorInfoItemTable.SUBTYPE)));
                list.add(item);
            }while(itemCursor.moveToNext());
        }
        itemCursor.close();
        return list;
    }
}
