package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.MyCollectionInfo;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.MyCollectionDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/1/13.
 */
public class MyCollectionDaoImpl implements MyCollectionDao{

    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDB;

    public MyCollectionDaoImpl(Context context){
        mContext = context;
        mDBUtils = DBUtils.getInstance(mContext);
    }
    @Override
    public long insertMyCollectionInfo(MyCollectionInfo info) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.MyCollectionInfoTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        values.put(DatabaseConstant.MyCollectionInfoTable.TYPE,info.getType());
        values.put(DatabaseConstant.MyCollectionInfoTable.START_NO,info.getStart_no());
        values.put(DatabaseConstant.MyCollectionInfoTable.END_NO,info.getEnd_no());
        values.put(DatabaseConstant.MyCollectionInfoTable.COUNT,info.getCount());

        mDB = mDBUtils.getWritableDatabase();
        long result = mDB.insert(DatabaseConstant.MyCollectionInfoTable.TABLE, null, values);
        return result;
    }

    @Override
    public long insertMyCollectionItemInfo(MyCollectionInfo info) {
        mDB = mDBUtils.getWritableDatabase();
        long result = 0;
        mDB.beginTransaction();
        try {
            for (int i = 0; i < info.mList.size(); i++) {
                ContentValues values = new ContentValues();
                MyCollectionInfo.CollectionItemInfo itemInfo = info.mList.get(i);
                values.put(DatabaseConstant.MyCollectionInfoItemTable.USER_ID, YueQiuApp.sUserInfo.getUser_id());
                values.put(DatabaseConstant.MyCollectionInfoItemTable.TABLE_ID, itemInfo.getTable_id());
                values.put(DatabaseConstant.MyCollectionInfoItemTable.TYPE, info.getType());
                values.put(DatabaseConstant.MyCollectionInfoItemTable.IMAGE_URL, itemInfo.getImage_url());
                values.put(DatabaseConstant.MyCollectionInfoItemTable.TITLE, itemInfo.getTitle());
                values.put(DatabaseConstant.MyCollectionInfoItemTable.CONTENT, itemInfo.getContent());
                values.put(DatabaseConstant.MyCollectionInfoItemTable.DATETIME, itemInfo.getDateTime());
                values.put(DatabaseConstant.MyCollectionInfoItemTable.USER_NAME,itemInfo.getUserName());
                result = mDB.insert(DatabaseConstant.MyCollectionInfoItemTable.TABLE, null, values);
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
    public long updateMyCollectionInfo(MyCollectionInfo info) {
        mDB = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.MyCollectionInfoTable.START_NO,info.getStart_no());
        values.put(DatabaseConstant.MyCollectionInfoTable.END_NO,info.getEnd_no());
        values.put(DatabaseConstant.MyCollectionInfoTable.COUNT,info.getCount());

        long result = mDB.update(DatabaseConstant.MyCollectionInfoTable.TABLE,values, DatabaseConstant.MyCollectionInfoTable.USER_ID + "=? and " +
                        DatabaseConstant.MyCollectionInfoTable.TYPE + "=?",
                new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()),String.valueOf(info.getType())});

        return result;
    }

    @Override
    public List<Long> updateMyCollectionItemInfo(MyCollectionInfo info) {
        mDB = mDBUtils.getWritableDatabase();
        List<Long> list = new ArrayList<Long>();
        mDB.beginTransaction();
        try {
            for (int i = 0; i < info.mList.size(); i++) {
                ContentValues values = new ContentValues();
                MyCollectionInfo.CollectionItemInfo itemInfo = (MyCollectionInfo.CollectionItemInfo) info.mList.get(i);
                values.put(DatabaseConstant.MyCollectionInfoItemTable.TABLE_ID, itemInfo.getTable_id());
                values.put(DatabaseConstant.MyCollectionInfoItemTable.IMAGE_URL, itemInfo.getImage_url());
                values.put(DatabaseConstant.MyCollectionInfoItemTable.TITLE, itemInfo.getTitle());
                values.put(DatabaseConstant.MyCollectionInfoItemTable.CONTENT, itemInfo.getContent());
                values.put(DatabaseConstant.MyCollectionInfoItemTable.DATETIME, itemInfo.getDateTime());
                values.put(DatabaseConstant.MyCollectionInfoItemTable.USER_NAME,itemInfo.getUserName());
                long result = mDB.update(DatabaseConstant.MyCollectionInfoItemTable.TABLE, values, DatabaseConstant.MyCollectionInfoItemTable.USER_ID + "=? and " +
                                DatabaseConstant.MyCollectionInfoItemTable.TABLE_ID + "=?",
                        new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()), String.valueOf(itemInfo.getTable_id())});
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
    public boolean isExistMyCollectionInfo(int type) {
        mDB = mDBUtils.getReadableDatabase();
        Cursor cursor = mDB.query(DatabaseConstant.MyCollectionInfoTable.TABLE,null,DatabaseConstant.MyCollectionInfoTable.USER_ID + "=? and "
                + DatabaseConstant.MyCollectionInfoTable.TYPE + "=?",new String[]{String.valueOf(YueQiuApp.sUserInfo.getUser_id()),
                String.valueOf(type)},null,null,null);
        if(cursor == null || cursor.getCount() == 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    @Override
    public boolean isExistMyCollectionItemInfo(int tableId, int type) {
        mDB = mDBUtils.getReadableDatabase();
        Cursor cursor = mDB.query(DatabaseConstant.MyCollectionInfoItemTable.TABLE,null,DatabaseConstant.MyCollectionInfoItemTable.USER_ID + "=? and " +
                        DatabaseConstant.MyCollectionInfoItemTable.TABLE_ID + "=? and " + DatabaseConstant.MyCollectionInfoItemTable.TYPE + "=?",
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
    public MyCollectionInfo getMyCollectionInfo(String userId, int type) {
        mDB = mDBUtils.getReadableDatabase();
        MyCollectionInfo info = new MyCollectionInfo();
        String infoSql = "SELECT * FROM " + DatabaseConstant.MyCollectionInfoTable.TABLE + " where " + DatabaseConstant.MyCollectionInfoTable.USER_ID + "=?"
                + " and " + DatabaseConstant.MyCollectionInfoTable.TYPE + "=?";
        Cursor infoCursor = mDB.rawQuery(infoSql,new String[]{userId,String.valueOf(type)});
        if(infoCursor != null && infoCursor.getCount() != 0){
            infoCursor.moveToFirst();
            do{
                info.setUser_id(YueQiuApp.sUserInfo.getUser_id());
                info.setStart_no(infoCursor.getInt(infoCursor.getColumnIndexOrThrow(DatabaseConstant.MyCollectionInfoTable.START_NO)));
                info.setEnd_no(infoCursor.getInt(infoCursor.getColumnIndexOrThrow(DatabaseConstant.MyCollectionInfoTable.END_NO)));
                info.setType(infoCursor.getInt(infoCursor.getColumnIndexOrThrow(DatabaseConstant.MyCollectionInfoTable.TYPE)));
                info.setCount(infoCursor.getInt(infoCursor.getColumnIndexOrThrow(DatabaseConstant.MyCollectionInfoTable.COUNT)));
            }while(infoCursor.moveToNext());
        }
        infoCursor.close();


        String itemSql = "SELECT * FROM " + DatabaseConstant.MyCollectionInfoItemTable.TABLE + " where " + DatabaseConstant.MyCollectionInfoItemTable.USER_ID + "=?"
                + " and " +  DatabaseConstant.MyCollectionInfoItemTable.TYPE + "=?" ;
        Cursor itemCursor = mDB.rawQuery(itemSql,new String[]{userId,String.valueOf(type)});
        if(itemCursor != null || itemCursor.getCount() != 0 ){
            itemCursor.moveToFirst();
            do{
                MyCollectionInfo.CollectionItemInfo item = info.new CollectionItemInfo();
                item.setType(itemCursor.getInt(itemCursor.getColumnIndexOrThrow(DatabaseConstant.MyCollectionInfoItemTable.TYPE)));
                item.setTitle(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.MyCollectionInfoItemTable.TITLE)));
                item.setContent(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.MyCollectionInfoItemTable.CONTENT)));
                item.setDateTime(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.MyCollectionInfoItemTable.DATETIME)));
                item.setImage_url(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.MyCollectionInfoItemTable.IMAGE_URL)));
                item.setTable_id(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.MyCollectionInfoItemTable.TABLE_ID)));
                item.setUserName(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseConstant.MyCollectionInfoItemTable.USER_NAME)));
                info.mList.add(item);
            }while(itemCursor.moveToNext());
        }
        itemCursor.close();
        return info;
    }
}
