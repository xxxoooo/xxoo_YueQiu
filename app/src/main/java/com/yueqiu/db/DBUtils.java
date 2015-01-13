package com.yueqiu.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.yueqiu.constant.DatabaseConstant;


public class DBUtils extends SQLiteOpenHelper {

    private String sCreateSQL;

    public DBUtils(Context context, String create) {
        super(context, DatabaseConstant.DATABASENAME, null, DatabaseConstant.VERSION);
        this.sCreateSQL = create;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseConstant.UserTable.CREATE_SQL);
        db.execSQL(DatabaseConstant.PublishInfoItemTable.CREATE_URL);
        db.execSQL(DatabaseConstant.ActivitiesTable.SQL);
//        db.execSQL(DatabaseConstant.RefreshTime.SQL);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

}
