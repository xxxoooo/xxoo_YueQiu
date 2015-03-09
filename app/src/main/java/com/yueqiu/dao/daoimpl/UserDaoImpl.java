package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.UserDao;
import com.yueqiu.db.DBUtils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by wangyun on 15/1/12.
 */
public class UserDaoImpl implements UserDao{
    private Context mContext;

    private DBUtils mDBUtils;

    public UserDaoImpl(Context context){
        this.mContext = context;
        mDBUtils = DBUtils.getInstance(mContext);
    }

    @Override
    public synchronized long insertUserInfo(Map<String, String> map) {
        ContentValues values = new ContentValues();

        values.put(DatabaseConstant.UserTable.USER_ID,map.get(DatabaseConstant.UserTable.USER_ID));
        values.put(DatabaseConstant.UserTable.USERNAME,map.get(DatabaseConstant.UserTable.USERNAME));
        values.put(DatabaseConstant.UserTable.PHONE, map.get(DatabaseConstant.UserTable.PHONE));
        String password = map.get(DatabaseConstant.UserTable.PASSWORD);
        String sign = TextUtils.isEmpty(password) ? "" : new String(Hex.encodeHex(DigestUtils.sha(password))).toUpperCase();
        values.put(DatabaseConstant.UserTable.PASSWORD,sign);
        values.put(DatabaseConstant.UserTable.SEX,map.get(DatabaseConstant.UserTable.SEX) == null ?
                "" : map.get(DatabaseConstant.UserTable.SEX));
        values.put(DatabaseConstant.UserTable.TITLE,map.get(DatabaseConstant.UserTable.TITLE) == null ?
                "": map.get(DatabaseConstant.UserTable.TITLE));
        values.put(DatabaseConstant.UserTable.IMG_URL,map.get(DatabaseConstant.UserTable.IMG_URL) == null ?
                "" : map.get(DatabaseConstant.UserTable.IMG_URL));
        values.put(DatabaseConstant.UserTable.IMG_REAL,map.get(DatabaseConstant.UserTable.IMG_REAL) == null ?
                "" : map.get(DatabaseConstant.UserTable.IMG_REAL));
        values.put(DatabaseConstant.UserTable.NICK,map.get(DatabaseConstant.UserTable.NICK) == null ?
                "" : map.get(DatabaseConstant.UserTable.NICK));
        values.put(DatabaseConstant.UserTable.DISTRICT,map.get(DatabaseConstant.UserTable.DISTRICT) == null ?
                "" : map.get(DatabaseConstant.UserTable.DISTRICT));
        values.put(DatabaseConstant.UserTable.LEVEL, map.get(DatabaseConstant.UserTable.LEVEL) == null ?
                1 : Integer.valueOf(map.get(DatabaseConstant.UserTable.LEVEL)));
        values.put(DatabaseConstant.UserTable.BALL_TYPE,map.get(DatabaseConstant.UserTable.BALL_TYPE) == null ?
                1 : Integer.valueOf(map.get(DatabaseConstant.UserTable.BALL_TYPE)));
        values.put(DatabaseConstant.UserTable.APPOINT_DATE,"");
        values.put(DatabaseConstant.UserTable.BALLARM,map.get(DatabaseConstant.UserTable.BALLARM) == null ?
                2 : Integer.valueOf(DatabaseConstant.UserTable.BALLARM));
        values.put(DatabaseConstant.UserTable.USERDTYPE,map.get(DatabaseConstant.UserTable.USERDTYPE) == null ?
                1 : Integer.valueOf(map.get(DatabaseConstant.UserTable.USERDTYPE)));
        values.put(DatabaseConstant.UserTable.BALLAGE, map.get(DatabaseConstant.UserTable.BALLAGE) == null ?
                3 : Integer.valueOf(map.get(DatabaseConstant.UserTable.BALLAGE)));
        values.put(DatabaseConstant.UserTable.IDOL,map.get(DatabaseConstant.UserTable.IDOL) == null ?
                "" : map.get(DatabaseConstant.UserTable.IDOL));
        values.put(DatabaseConstant.UserTable.IDOL_NAME,map.get(DatabaseConstant.UserTable.IDOL_NAME) == null ?
                "" : map.get(DatabaseConstant.UserTable.IDOL_NAME));
        values.put(DatabaseConstant.UserTable.NEW_IMG,map.get(DatabaseConstant.UserTable.NEW_IMG) == null ?
                "" : map.get(DatabaseConstant.UserTable.NEW_IMG));
        values.put(DatabaseConstant.UserTable.NEW_IMG_REAL,map.get(DatabaseConstant.UserTable.NEW_IMG_REAL) == null ?
                "" : map.get(DatabaseConstant.UserTable.NEW_IMG_REAL));
        values.put(DatabaseConstant.UserTable.LOGIN_TIME,map.get(DatabaseConstant.UserTable.LOGIN_TIME) == null ?
                "" : map.get(DatabaseConstant.UserTable.LOGIN_TIME));
        values.put(DatabaseConstant.UserTable.COST,map.get(DatabaseConstant.UserTable.COST) == null ?
                "" : map.get(DatabaseConstant.UserTable.COST));
        values.put(DatabaseConstant.UserTable.MY_TYPE,map.get(DatabaseConstant.UserTable.MY_TYPE) == null ?
                "" : map.get(DatabaseConstant.UserTable.MY_TYPE));
        values.put(DatabaseConstant.UserTable.WORK_LIVE,map.get(DatabaseConstant.UserTable.WORK_LIVE) == null ?
                "" : map.get(DatabaseConstant.UserTable.WORK_LIVE));

        SQLiteDatabase db = mDBUtils.getWritableDatabase();
        long result = -1;
        db.beginTransaction();
        try{
            result = db.insert(DatabaseConstant.UserTable.TABLE,null,values);
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }

        return result;
    }

    @Override
    public boolean queryUserId(Map<String, String> map) {
        SQLiteDatabase db = mDBUtils.getReadableDatabase();
        Cursor cursor = db.query(DatabaseConstant.UserTable.TABLE,null,DatabaseConstant.UserTable.USER_ID + "=?",
                new String[]{map.get(DatabaseConstant.UserTable.USER_ID)},null,null,null);
        if(cursor == null || cursor.getCount() == 0){
            return false;
        }
        cursor.close();
        return true;
    }

    @Override
    public synchronized long updateUserInfo(Map<String, String> map) {
        SQLiteDatabase db = mDBUtils.getWritableDatabase();
        long result = -1;
        ContentValues values = new ContentValues();
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
            values.put(entry.getKey(), entry.getValue());
        }
        db.beginTransaction();
        try {
            result = db.update(DatabaseConstant.UserTable.TABLE, values, DatabaseConstant.UserTable.USER_ID + "=?",
                    new String[]{map.get(DatabaseConstant.UserTable.USER_ID)});
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
        return result;
    }

    @Override
    public synchronized UserInfo getUserByUserId(String userId) {
        SQLiteDatabase db = mDBUtils.getReadableDatabase();
        UserInfo info = new UserInfo();
        String sql = "select * from " + DatabaseConstant.UserTable.TABLE + " where " + DatabaseConstant.UserTable.USER_ID + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{userId});
        if (cursor != null || cursor.getCount() != 0) {
            cursor.moveToFirst();
            info.setUser_id(Integer.valueOf(userId));
            info.setUsername(cursor.getString(cursor.getColumnIndex(DatabaseConstant.UserTable.USERNAME)));
            info.setPhone(cursor.getString(cursor.getColumnIndex(DatabaseConstant.UserTable.PHONE)));
            info.setPassword(cursor.getString(cursor.getColumnIndex(DatabaseConstant.UserTable.PASSWORD)));
            info.setSex(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.UserTable.SEX)));
            info.setTitle(cursor.getString(cursor.getColumnIndex(DatabaseConstant.UserTable.TITLE)));
            info.setImg_url(cursor.getString(cursor.getColumnIndex(DatabaseConstant.UserTable.IMG_URL)));
            info.setNick(cursor.getString(cursor.getColumnIndex(DatabaseConstant.UserTable.USERNAME)));
            info.setDistrict(cursor.getString(cursor.getColumnIndex(DatabaseConstant.UserTable.DISTRICT)));
            info.setLevel(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.UserTable.LEVEL)));
            info.setBall_type(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.UserTable.BALL_TYPE)));
            info.setAppoint_date(cursor.getString(cursor.getColumnIndex(DatabaseConstant.UserTable.APPOINT_DATE)));
            info.setBallArm(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.UserTable.BALLARM)));
            info.setUsedType(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.UserTable.USERDTYPE)));
            info.setBallAge(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseConstant.UserTable.BALLAGE))));
            info.setIdol(cursor.getString(cursor.getColumnIndex(DatabaseConstant.UserTable.IDOL)));
            info.setIdol_name(cursor.getString(cursor.getColumnIndex(DatabaseConstant.UserTable.IDOL_NAME)));
            info.setNew_img(cursor.getString(cursor.getColumnIndex(DatabaseConstant.UserTable.NEW_IMG)));
            info.setLogin_time(cursor.getString(cursor.getColumnIndex(DatabaseConstant.UserTable.LOGIN_TIME)));
            info.setCost(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.UserTable.COST)));
            String str = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.UserTable.MY_TYPE));
            info.setMy_type(Integer.valueOf(str.equals("") ? "-1": str));
            info.setWork_live(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstant.UserTable.WORK_LIVE)));
        }
        cursor.close();
        return info;
    }
}
