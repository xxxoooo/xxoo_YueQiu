package com.yueqiu.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.yueqiu.constant.DatabaseConstant;


public class DBUtils extends SQLiteOpenHelper {

    public static DBUtils mDBHelper;
    public static DBUtils getInstance(Context context)
    {
        return (null == mDBHelper) ? mDBHelper = new DBUtils(context) : mDBHelper;
    }

    private DBUtils(Context context) {
        super(context, DatabaseConstant.DATABASENAME, null, DatabaseConstant.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

}
