package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.yueqiu.bean.ContactsList;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.ContactsDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by doushuqi on 15/1/13.
 */
public class ContactsDaoImpl implements ContactsDao {
    private Context mContext;
    private DBUtils mDBUtils;

    public ContactsDaoImpl(Context context) {
        mContext = context;
        mDBUtils = DBUtils.getInstance(mContext);
    }

    @Override
    public long insertContact(Map<String, String> map) {
        if (queryUserId(map)) {
            return 0;
        }
        ContentValues values = new ContentValues();

        values.put(DatabaseConstant.FriendsTable.USER_ID, map.get(DatabaseConstant.FriendsTable.USER_ID));
        values.put(DatabaseConstant.FriendsTable.GROUP_ID, map.get(DatabaseConstant.FriendsTable.GROUP_ID));
        values.put(DatabaseConstant.FriendsTable.USERNAME, map.get(DatabaseConstant.FriendsTable.USERNAME));
        values.put(DatabaseConstant.FriendsTable.IMG_URL, map.get(DatabaseConstant.FriendsTable.IMG_URL));
        values.put(DatabaseConstant.FriendsTable.LAST_MESSAGE, map.get(DatabaseConstant.FriendsTable.LAST_MESSAGE) == null ?
                "" : map.get(DatabaseConstant.FriendsTable.LAST_MESSAGE));
        values.put(DatabaseConstant.FriendsTable.DATETIME, map.get(DatabaseConstant.FriendsTable.DATETIME) == null ?
                "" : map.get(DatabaseConstant.FriendsTable.DATETIME));
        SQLiteDatabase db = mDBUtils.getWritableDatabase();
        long result = db.insert(DatabaseConstant.FriendsTable.TABLE, null, values);
        db.close();
        return result;
    }

    @Override
    public long insertContact(HashMap<Integer, List<ContactsList.Contacts>> map) {
        return 0;
    }

    @Override
    public boolean queryUserId(Map<String, String> map) {
        SQLiteDatabase db = mDBUtils.getReadableDatabase();
        Cursor cursor = db.query(DatabaseConstant.FriendsTable.TABLE, null, DatabaseConstant.FriendsTable.USER_ID + "=?",
                new String[]{map.get(DatabaseConstant.FriendsTable.USER_ID)}, null, null, null);
        if (cursor == null || cursor.getCount() == 0) {
            return false;
        }
        cursor.close();
        db.close();
        return true;
    }

    @Override
    public boolean queryGroupId(Map<String, String> map) {
        SQLiteDatabase db = mDBUtils.getReadableDatabase();
        Cursor cursor = db.query(DatabaseConstant.FriendsTable.TABLE, null, DatabaseConstant.FriendsTable.GROUP_ID + "=?",
                new String[]{map.get(DatabaseConstant.FriendsTable.GROUP_ID)}, null, null, null);
        if (cursor == null || cursor.getCount() == 0) {
            return false;
        }
        cursor.close();
        db.close();
        return true;
    }

    @Override
    public long updateContact(Map<String, String> map) {
        SQLiteDatabase db = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
            values.put(entry.getKey(), entry.getValue());
        }
        long result = db.update(DatabaseConstant.FriendsTable.TABLE, values, DatabaseConstant.FriendsTable.USER_ID + "=?",
                new String[]{map.get(DatabaseConstant.FriendsTable.USER_ID)});
        db.close();
        return result;
    }

    @Override
    public ContactsList.Contacts getContact(String userId) {
        SQLiteDatabase db = mDBUtils.getReadableDatabase();
        ContactsList.Contacts info = new ContactsList().new Contacts();
        String sql = "select * from " + DatabaseConstant.FriendsTable.TABLE + " where " + DatabaseConstant.FriendsTable.USER_ID + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{userId});
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            info.setUser_id(Integer.parseInt(userId));
            info.setGroup_id(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsTable.GROUP_ID))));
            info.setUsername(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsTable.USERNAME)));
            info.setImg_url(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsTable.IMG_URL)));
            info.setContent(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsTable.LAST_MESSAGE)));
            info.setCreate_time(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsTable.DATETIME)));
            cursor.close();
        }

        db.close();
        return info;
    }

    @Override
    public HashMap<Integer, List<ContactsList.Contacts>> getContactList() {
        SQLiteDatabase db = mDBUtils.getReadableDatabase();
        HashMap<Integer, List<ContactsList.Contacts>> contactsMap = new HashMap<Integer, List<ContactsList.Contacts>>();
        String sql = "select * from " + DatabaseConstant.FriendsTable.TABLE + " where " + DatabaseConstant.FriendsTable.GROUP_ID + "=?";
        for (int i = 0; i < 3; i++) {
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(i + 1)});
            if (cursor != null && cursor.getCount() != 0) {
                List<ContactsList.Contacts> list = new ArrayList<ContactsList.Contacts>();
                while (cursor.moveToNext()) {
                    ContactsList.Contacts info = new ContactsList().new Contacts();
                    info.setUser_id(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsTable.USER_ID))));
                    info.setGroup_id(i);
                    info.setUsername(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsTable.USERNAME)));
                    info.setImg_url(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsTable.IMG_URL)));
                    info.setContent(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsTable.LAST_MESSAGE)));
                    info.setCreate_time(cursor.getString(cursor.getColumnIndex(DatabaseConstant.FriendsTable.DATETIME)));
                    list.add(info);
                }
                contactsMap.put(i, list);
            }
        }
        return contactsMap;
    }
}
