package com.yueqiu.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import com.yueqiu.constant.DatabaseConstant;


public class DBUtils extends SQLiteOpenHelper {

    public  static DBUtils mDBHelper;

    public static DBUtils getInstance(Context context)
    {
        return (null == mDBHelper) ? mDBHelper = new DBUtils(context) : mDBHelper;
    }

    private DBUtils(Context context) {
        super(context, DatabaseConstant.DATABASENAME, null, DatabaseConstant.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseConstant.UserTable.CREATE_SQL);
        db.execSQL(DatabaseConstant.PublishInfoTable.CREATE_SQL);
        db.execSQL(DatabaseConstant.ChatMessageTable.CREATE_SQL);
        db.execSQL(DatabaseConstant.FriendsTable.CREATE_SQL);
        db.execSQL(DatabaseConstant.FriendsApplication.CREATE_SQL);
        db.execSQL(DatabaseConstant.PlayTable.SQL);
        db.execSQL(DatabaseConstant.RefreshTime.SQL);
        db.execSQL(DatabaseConstant.FavorInfoItemTable.CREATE_SQL);
        db.execSQL(DatabaseConstant.GroupInfo.CREATE_SQL);

        // 加载用于SearchActivity当中的Fragment的table
        db.execSQL(DatabaseConstant.SearchMateTable.CREATE_SQL);
        db.execSQL(DatabaseConstant.SearchDatingTable.CREATE_SQL);
        db.execSQL(DatabaseConstant.SearchAssistCoauchTable.CREATE_SQL);
        db.execSQL(DatabaseConstant.SearchCoauchTable.CREATE_SQL);
        db.execSQL(DatabaseConstant.SearchRoomTable.CREATE_SQL);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseConstant.UserTable.DROP_SQL);
        db.execSQL(DatabaseConstant.PublishInfoTable.DROP_SQL);
        db.execSQL(DatabaseConstant.PlayTable.DROP_SQL);
        db.execSQL(DatabaseConstant.RefreshTime.SQL);
        db.execSQL(DatabaseConstant.ChatMessageTable.DROP_SQL);
        db.execSQL(DatabaseConstant.FriendsTable.DROP_SQL);
        db.execSQL(DatabaseConstant.FriendsApplication.DROP_SQL);

        // 加载用于SearchActivity当中的Fragment的Drop sql
        db.execSQL(DatabaseConstant.SearchMateTable.DROP_SQL);
        db.execSQL(DatabaseConstant.SearchDatingTable.DROP_SQL);
        db.execSQL(DatabaseConstant.SearchAssistCoauchTable.DROP_SQL);
        db.execSQL(DatabaseConstant.SearchCoauchTable.DROP_SQL);
        db.execSQL(DatabaseConstant.SearchRoomTable.DROP_SQL);
        db.execSQL(DatabaseConstant.FavorInfoItemTable.DROP_SQL);
        db.execSQL(DatabaseConstant.GroupInfo.DROP_SQL);
        onCreate(db);
    }

}
