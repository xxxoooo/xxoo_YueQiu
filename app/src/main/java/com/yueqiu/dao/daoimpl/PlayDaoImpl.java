package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.yueqiu.bean.PlayInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.PlayDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;


public class PlayDaoImpl implements PlayDao {
    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDB;

    public PlayDaoImpl(Context context) {
        this.mContext = context;
        this.mDBUtils = DBUtils.getInstance(mContext);
    }

    private String checkIsEmpty(String param){
        if(TextUtils.isEmpty(param))
            return "";
        return param;
    }

    @Override
    public synchronized long insertPlayInfo(List<PlayInfo> list) {
        long result = -1;
        mDB = mDBUtils.getWritableDatabase();
        mDB.beginTransaction();
        try{
            for(PlayInfo info : list){
                ContentValues values = new ContentValues();
                values.put(DatabaseConstant.PlayTable.TABLE_ID,info.getTable_id());
                values.put(DatabaseConstant.PlayTable.USERNAME, checkIsEmpty(info.getUsername()));
                values.put(DatabaseConstant.PlayTable.SEX, checkIsEmpty(info.getSex()));
                values.put(DatabaseConstant.PlayTable.IMG_URL,info.getImg_url());
                values.put(DatabaseConstant.PlayTable.LOOK_NUM,info.getLook_num());
                values.put(DatabaseConstant.PlayTable.TYPE,checkIsEmpty(info.getType()));
                values.put(DatabaseConstant.PlayTable.TITLE,info.getTitle());
                values.put(DatabaseConstant.PlayTable.ADDRESS,checkIsEmpty(info.getAddress()));
                values.put(DatabaseConstant.PlayTable.BEGIN_TIME,checkIsEmpty(info.getBegin_time()));
                values.put(DatabaseConstant.PlayTable.END_TIME,checkIsEmpty(info.getBegin_time()));
                values.put(DatabaseConstant.PlayTable.MODEL,checkIsEmpty(info.getBegin_time()));
                values.put(DatabaseConstant.PlayTable.CONTENT,info.getContent());
                values.put(DatabaseConstant.PlayTable.CREATE_TIME,info.getCreate_time());
                values.put(DatabaseConstant.PlayTable.CONTACT,info.getContact());
                values.put(DatabaseConstant.PlayTable.PHONE,info.getPhone());
                result = mDB.insert(DatabaseConstant.PlayTable.TABLENAME,null,values);
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
    public long updatesPlayInfo(List<PlayInfo> list) {
        long result = -1;
        mDB = mDBUtils.getWritableDatabase();
        mDB.beginTransaction();
        try{
            for(PlayInfo info : list){
                ContentValues values = new ContentValues();
                values.put(DatabaseConstant.PlayTable.TABLE_ID,info.getTable_id());
                values.put(DatabaseConstant.PlayTable.TYPE,checkIsEmpty(info.getType()));
                values.put(DatabaseConstant.PlayTable.TITLE,info.getTitle());
                values.put(DatabaseConstant.PlayTable.CONTENT,info.getContent());
                values.put(DatabaseConstant.PlayTable.CREATE_TIME,info.getCreate_time());
                result = mDB.update(DatabaseConstant.PlayTable.TABLENAME, values, DatabaseConstant.PublishInfoTable.TABLE_ID + "=? and "
                                + DatabaseConstant.PublishInfoTable.TYPE + "=?",
                        new String[]{String.valueOf(info.getTable_id()), String.valueOf(info.getType())});
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
    public synchronized long updatesDetailPlayInfo(List<PlayInfo> list) {
        long result = -1;
        mDB = mDBUtils.getWritableDatabase();
        mDB.beginTransaction();
        try{
            for(PlayInfo info : list){
                ContentValues values = new ContentValues();
                values.put(DatabaseConstant.PlayTable.TABLE_ID,info.getTable_id());
                values.put(DatabaseConstant.PlayTable.USERNAME, checkIsEmpty(info.getUsername()));
                values.put(DatabaseConstant.PlayTable.SEX, checkIsEmpty(info.getSex()));
                values.put(DatabaseConstant.PlayTable.IMG_URL,info.getImg_url());
                values.put(DatabaseConstant.PlayTable.LOOK_NUM,info.getLook_num());
                values.put(DatabaseConstant.PlayTable.TYPE,checkIsEmpty(info.getType()));
                values.put(DatabaseConstant.PlayTable.TITLE,info.getTitle());
                values.put(DatabaseConstant.PlayTable.ADDRESS,checkIsEmpty(info.getAddress()));
                values.put(DatabaseConstant.PlayTable.BEGIN_TIME,checkIsEmpty(info.getBegin_time()));
                values.put(DatabaseConstant.PlayTable.END_TIME,checkIsEmpty(info.getBegin_time()));
                values.put(DatabaseConstant.PlayTable.MODEL,checkIsEmpty(info.getModel()));
                values.put(DatabaseConstant.PlayTable.CONTENT,info.getContent());
                values.put(DatabaseConstant.PlayTable.CREATE_TIME,info.getCreate_time());
                values.put(DatabaseConstant.PlayTable.CONTACT,info.getContact());
                values.put(DatabaseConstant.PlayTable.PHONE,info.getPhone());
                result = mDB.update(DatabaseConstant.PlayTable.TABLENAME, values, DatabaseConstant.PublishInfoTable.TABLE_ID + "=? and "
                                + DatabaseConstant.PublishInfoTable.TYPE + "=?",
                        new String[]{String.valueOf(info.getTable_id()), String.valueOf(info.getType())});
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
    public List<PlayInfo> getAllPlayInfo() {
        mDB = mDBUtils.getReadableDatabase();
        List<PlayInfo> list = new ArrayList<PlayInfo>();
        String sql = "SELECT * FROM " + DatabaseConstant.PlayTable.TABLENAME
                + " order by " + DatabaseConstant.PlayTable.TABLE_ID + " desc";
        Cursor cursor = mDB.rawQuery(sql,null);
        if(cursor != null && cursor.getCount() != 0){
            cursor.moveToFirst();
            do{
                PlayInfo info = new PlayInfo();
                info.setTable_id(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.TABLE_ID)));
                info.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.USERNAME)));
                info.setImg_url(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.IMG_URL)));
                info.setSex(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.SEX)));
                info.setLook_num(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.LOOK_NUM)));
                info.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.TYPE)));
                info.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.TITLE)));
                info.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.CONTENT)));
                info.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.ADDRESS)));
                info.setBegin_time(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.BEGIN_TIME)));
                info.setEnd_time(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.END_TIME)));
                info.setModel(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.MODEL)));
                info.setCreate_time(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.CREATE_TIME)));
                info.setContact(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.CONTACT)));
                info.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.PHONE)));
                list.add(info);
            }while(cursor.moveToNext());
        }
        return list;
    }

    @Override
    public List<PlayInfo> getPlayInfoLimit(int type, int start, int num) {
        mDB = mDBUtils.getReadableDatabase();
        List<PlayInfo> list = new ArrayList<PlayInfo>();
        String sql = "SELECT * FROM " + DatabaseConstant.PlayTable.TABLENAME + " where " + DatabaseConstant.PlayTable.TYPE + "=?"
                + " order by " + DatabaseConstant.PlayTable.TABLE_ID + " desc limit " + start + "," + num;
        Cursor cursor = mDB.rawQuery(sql,new String[]{String.valueOf(type)});
        if(cursor != null && cursor.getCount() != 0){
            cursor.moveToFirst();
            do{
                PlayInfo info = new PlayInfo();
                info.setTable_id(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.TABLE_ID)));
                info.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.USERNAME)));
                info.setImg_url(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.IMG_URL)));
                info.setSex(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.SEX)));
                info.setLook_num(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.LOOK_NUM)));
                info.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.TYPE)));
                info.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.TITLE)));
                info.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.CONTENT)));
                info.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.ADDRESS)));
                info.setBegin_time(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.BEGIN_TIME)));
                info.setEnd_time(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.END_TIME)));
                info.setModel(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.MODEL)));
                info.setCreate_time(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.CREATE_TIME)));
                info.setContact(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.CONTACT)));
                info.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.PHONE)));
                list.add(info);
            }while(cursor.moveToNext());
        }
        return list;
    }

    @Override
    public PlayInfo getPlayInfoById(int tableId, int type) {
        mDB = mDBUtils.getReadableDatabase();
        PlayInfo info = new PlayInfo();
        String sql = "SELECT * FROM " + DatabaseConstant.PlayTable.TABLENAME + " where " + DatabaseConstant.PlayTable.TYPE + "=?"
                + " and " + DatabaseConstant.PlayTable.TABLE_ID + "=?";
        Cursor cursor = mDB.rawQuery(sql,new String[]{String.valueOf(type),String.valueOf(tableId)});
        Log.d("wy","cursor.count->" + cursor.getCount());
        if(cursor != null && cursor.getCount() != 0){
            cursor.moveToFirst();
            do{

                info.setTable_id(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.TABLE_ID)));
                info.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.USERNAME)));
                info.setImg_url(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.IMG_URL)));
                info.setSex(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.SEX)));
                info.setLook_num(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.LOOK_NUM)));
                info.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.TYPE)));
                info.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.TITLE)));
                info.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.CONTENT)));
                info.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.ADDRESS)));
                info.setBegin_time(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.BEGIN_TIME)));
                info.setEnd_time(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.END_TIME)));
                info.setModel(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.MODEL)));
                info.setCreate_time(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.CREATE_TIME)));
                info.setContact(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.CONTACT)));
                info.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.PlayTable.PHONE)));
            }while(cursor.moveToNext());
        }
        return info;
    }
}
