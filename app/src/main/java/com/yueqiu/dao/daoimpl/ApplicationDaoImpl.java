package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yueqiu.bean.FriendsApplication;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.ApplicationDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by doushuqi on 15/1/14.
 */
public class ApplicationDaoImpl implements ApplicationDao {
    private Context mContext;
    private DBUtils mDBUtils;

    public ApplicationDaoImpl(Context context) {
        mContext = context;
        mDBUtils = DBUtils.getInstance(mContext);
    }

    @Override
    public void insertApplication(List<FriendsApplication> list) {
        SQLiteDatabase db = mDBUtils.getWritableDatabase();
        for (FriendsApplication application : list) {
            if (queryApplicationById(application.getId()))
                continue;
            ContentValues values = new ContentValues();
            values.put(DatabaseConstant.FriendsApplication.APPLICATION_ID, application.getId());
            values.put(DatabaseConstant.FriendsApplication.NICK, application.getNick());
            values.put(DatabaseConstant.FriendsApplication.USERNAME, application.getUsername());
            values.put(DatabaseConstant.FriendsApplication.CREATE_TIME, application.getCreate_time());
            values.put(DatabaseConstant.FriendsApplication.IMG_URL, application.getImg_url());
            values.put(DatabaseConstant.FriendsApplication.IS_AGREE, application.getIsAgree());

            db.insert(DatabaseConstant.FriendsApplication.TABLE, null, values);
        }
        db.close();
    }

    @Override
    public long insertApplication(FriendsApplication application) {
        if (queryApplicationById(application.getId()))
            return 0;
        SQLiteDatabase db = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.FriendsApplication.APPLICATION_ID, application.getId());
        values.put(DatabaseConstant.FriendsApplication.NICK, application.getNick());
        values.put(DatabaseConstant.FriendsApplication.USERNAME, application.getUsername());
        values.put(DatabaseConstant.FriendsApplication.CREATE_TIME, application.getCreate_time());
        values.put(DatabaseConstant.FriendsApplication.IMG_URL, application.getImg_url());
        values.put(DatabaseConstant.FriendsApplication.IS_AGREE, application.getIsAgree());
        long result = db.insert(DatabaseConstant.FriendsApplication.TABLE, null, values);
        db.close();
        return result;
    }

    @Override
    public List<FriendsApplication> getApplication() {
        SQLiteDatabase db = mDBUtils.getReadableDatabase();
        List<FriendsApplication> list = new ArrayList<FriendsApplication>();
        String sql = "select * from " + DatabaseConstant.FriendsApplication.TABLE;
        Cursor cursor = db.query(DatabaseConstant.FriendsApplication.TABLE, null, null, null, null, null, null);
        if (cursor != null && cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                FriendsApplication application = new FriendsApplication();
                application.setId(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsApplication.APPLICATION_ID)));
                application.setNick(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsApplication.NICK)));
                application.setUsername(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsApplication.USERNAME)));
                application.setCreate_time(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsApplication.CREATE_TIME)));
                application.setImg_url(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsApplication.IMG_URL)));
                application.setIsAgree(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.FriendsApplication.IS_AGREE)));
                list.add(application);
            }
            cursor.close();
        }
        db.close();
        return list;
    }

    @Override
    public long updateFriendsApplication(String id, int value) {
        SQLiteDatabase db = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.FriendsApplication.IS_AGREE, value);
        long result = db.update(DatabaseConstant.FriendsApplication.TABLE, values,
                DatabaseConstant.FriendsApplication.APPLICATION_ID + "=?",
                new String[]{id});
        db.close();
        return result;
    }

    @Override
    public boolean queryApplicationById(String id) {
        SQLiteDatabase db = mDBUtils.getReadableDatabase();
        String sql = "select * from " + DatabaseConstant.FriendsApplication.TABLE + " where " +
                DatabaseConstant.FriendsApplication.APPLICATION_ID + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{id});
        if (cursor == null || cursor.getCount() == 0) {
            db.close();
            return false;
        }
        cursor.close();
        db.close();
        return true;
    }

    @Override
    public boolean clearData() {
        SQLiteDatabase db = mDBUtils.getWritableDatabase();
        int result = db.delete(DatabaseConstant.FriendsApplication.TABLE, null, null);
        return result == 1;
    }
}
