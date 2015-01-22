package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.GroupInfoDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/1/20.
 */
public class GroupInfoDaoImpl implements GroupInfoDao{

    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDB;

    public GroupInfoDaoImpl(Context context){
        this.mContext = context;
        mDBUtils = DBUtils.getInstance(mContext);
    }

    @Override
    public synchronized long insertGroupInfo(List<GroupNoteInfo> infos) {
        mDB = mDBUtils.getWritableDatabase();
        long result = -1;
        SQLiteStatement state = mDB.compileStatement(DatabaseConstant.GroupInfo.INSERT_SQL);
        mDB.beginTransaction();
        try {
            for(int i=0;i<infos.size();i++) {
                state.bindLong(1, infos.get(i).getNoteId());
                state.bindLong(2, infos.get(i).getType());
                state.bindString(3, infos.get(i).getUserName());
                state.bindLong(4, infos.get(i).getSex() == 0 ? 1 : infos.get(i).getSex());
                state.bindLong(5, infos.get(i).getBrowseCount());
                state.bindString(6, infos.get(i).getIssueTime());
                state.bindString(7, infos.get(i).getTitle());
                state.bindString(8, infos.get(i).getContent());
                state.bindLong(9, infos.get(i).getCommentCount());
                state.bindString(10, infos.get(i).getImg_url());
                state.bindLong(11, infos.get(i).getLoveNums());
                result = state.executeInsert();
            }
            mDB.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mDB.endTransaction();
        }
        return result;
    }

    @Override
    public synchronized long updateGroupInfo(List<GroupNoteInfo> infos) {
        long result = -1;
        mDB = mDBUtils.getWritableDatabase();
        mDB.beginTransaction();
        try{
            for(int i=0;i<infos.size();i++) {
                ContentValues values = new ContentValues();
                values.put(DatabaseConstant.GroupInfo.NOTE_ID, infos.get(i).getNoteId());
                if(infos.get(i).getType() != PublicConstant.GROUP_ALL) {
                    values.put(DatabaseConstant.GroupInfo.TYPE, infos.get(i).getType());
                }
                values.put(DatabaseConstant.GroupInfo.USER_NAME, infos.get(i).getUserName());
                values.put(DatabaseConstant.GroupInfo.SEX, infos.get(i).getSex());
                values.put(DatabaseConstant.GroupInfo.BROWSE_COUNT, infos.get(i).getBrowseCount());
                values.put(DatabaseConstant.GroupInfo.ISSUE_TIME, infos.get(i).getIssueTime());
                values.put(DatabaseConstant.GroupInfo.TITLE, infos.get(i).getTitle());
                values.put(DatabaseConstant.GroupInfo.CONTENT, infos.get(i).getContent());
                values.put(DatabaseConstant.GroupInfo.COMMENT_COUNT, infos.get(i).getCommentCount());
                values.put(DatabaseConstant.GroupInfo.IMG_URL, infos.get(i).getImg_url());
                values.put(DatabaseConstant.GroupInfo.PRAISE_NUM, infos.get(i).getLoveNums());
                result = mDB.update(DatabaseConstant.GroupInfo.TABLE, values, DatabaseConstant.GroupInfo.NOTE_ID + "=?", new String[]{String.valueOf(infos.get(i).getNoteId())});
            }
            mDB.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDB.endTransaction();
        }
        return result;

    }



    @Override
    public  List<GroupNoteInfo> getAllGroupInfoLimit(int start,int num) {
        mDB = mDBUtils.getReadableDatabase();
        String sql = "SELECT * FROM " + DatabaseConstant.GroupInfo.TABLE + " order by " + DatabaseConstant.GroupInfo.NOTE_ID + " desc limit "
                 + start + "," + num;
        Cursor cursor = mDB.rawQuery(sql,null);
        List<GroupNoteInfo> list = new ArrayList<GroupNoteInfo>();
        if(null != cursor && cursor.getCount() != 0){
            cursor.moveToFirst();
            do{
                GroupNoteInfo info = new GroupNoteInfo();
                info.setNoteId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo._ID)));
                info.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.USER_NAME)));
                info.setType(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.TYPE)));
                info.setSex(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.SEX)));
                info.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.TITLE)));
                info.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.CONTENT)));
                info.setBrowseCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.BROWSE_COUNT)));
                info.setCommentCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.COMMENT_COUNT)));
                info.setLoveNums(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.PRAISE_NUM)));
                info.setImg_url(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.IMG_URL)));
                info.setIssueTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.ISSUE_TIME)));
                list.add(info);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @Override
    public  List<GroupNoteInfo> getGroupInfoByType(int type,int start,int num) {
        mDB = mDBUtils.getReadableDatabase();
        String sql = "SELECT * FROM " + DatabaseConstant.GroupInfo.TABLE + " where " + DatabaseConstant.GroupInfo.TYPE  + "=? order by "
                + DatabaseConstant.GroupInfo.NOTE_ID + " desc limit " + start + "," + num;
        Cursor cursor = mDB.rawQuery(sql,new String[]{String.valueOf(type)});
        List<GroupNoteInfo> list = new ArrayList<GroupNoteInfo>();
        if(cursor != null && cursor.getCount() != 0){
            cursor.moveToFirst();
            do{
                GroupNoteInfo info = new GroupNoteInfo();
                info.setNoteId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.NOTE_ID)));
                info.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.USER_NAME)));
                info.setType(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.TYPE)));
                info.setSex(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.SEX)));
                info.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.TITLE)));
                info.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.CONTENT)));
                info.setBrowseCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.BROWSE_COUNT)));
                info.setCommentCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.COMMENT_COUNT)));
                info.setLoveNums(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.PRAISE_NUM)));
                info.setImg_url(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.IMG_URL)));
                info.setIssueTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.ISSUE_TIME)));
                list.add(info);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @Override
    public List<GroupNoteInfo> getAllGroupInfo() {
        mDB = mDBUtils.getReadableDatabase();
        String sql = "SELECT * FROM " + DatabaseConstant.GroupInfo.TABLE + " order by " + DatabaseConstant.GroupInfo.NOTE_ID + " desc";
        Cursor cursor = mDB.rawQuery(sql,null);
        List<GroupNoteInfo> list = new ArrayList<GroupNoteInfo>();
        if(null != cursor && cursor.getCount() != 0){
            cursor.moveToFirst();
            do{
                GroupNoteInfo info = new GroupNoteInfo();
                info.setNoteId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo._ID)));
                info.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.USER_NAME)));
                info.setType(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.TYPE)));
                info.setSex(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.SEX)));
                info.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.TITLE)));
                info.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.CONTENT)));
                info.setBrowseCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.BROWSE_COUNT)));
                info.setCommentCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.COMMENT_COUNT)));
                info.setLoveNums(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.PRAISE_NUM)));
                info.setImg_url(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.IMG_URL)));
                info.setIssueTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.GroupInfo.ISSUE_TIME)));
                list.add(info);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
