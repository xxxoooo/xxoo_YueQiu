package com.yueqiu.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yueqiu.constant.DatabaseConstant;

/**
 * Created by yinfeng on 14/12/26.
 */
public class DBUtils extends SQLiteOpenHelper {

    public static DBUtils mDBHelper;
    public static DBUtils getInstance(Context context)
    {
          return (null == mDBHelper) ? mDBHelper = new DBUtils(context) : mDBHelper;
    }
    public DBUtils(Context context) {
        super(context, DatabaseConstant.DATABASENAME, null, DatabaseConstant.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUserInfoTable(db);
    }

    private void createUserInfoTable(SQLiteDatabase db)
    {
        StringBuilder userInfoSql = new StringBuilder();
        userInfoSql.append("CREATE TABLE IF NOT EXISTS ").append(DatabaseConstant.UserTable.TABLE).
                append("( ").append(DatabaseConstant.UserTable._ID).
                append(" INTEGER PRIMARY KEY AUTOINCREMENT, ").
                append(DatabaseConstant.UserTable.IMG_URL).append(" VARCHAR(255), ").
                append(DatabaseConstant.UserTable.IMG_REAL).append(" VARCHAR(255), ").
                append(DatabaseConstant.UserTable.ACCOUNT).append(" VARCHAR(50), ").
                append(DatabaseConstant.UserTable.PHONE).append(" VARCHAR(50), ").
                append(DatabaseConstant.UserTable.PASSWORD).append(" VARCHAR(100), ").
                append(DatabaseConstant.UserTable.SEX).append(" int, ").
                append(DatabaseConstant.UserTable.TITLE).append(" VARCHAR(50), ").
                append(DatabaseConstant.UserTable.USERNAME).append(" VARCHAR(50), ").
                append(DatabaseConstant.UserTable.DISTRICT).append(" VARCHAR(100), ").
                append(DatabaseConstant.UserTable.LEVEL).append(" VARCHAR(50), ").
                append(DatabaseConstant.UserTable.BALL_TYPE).append(" VARCHAR(50), ").
                append(DatabaseConstant.UserTable.APPOINT_DATE).append(" VARCHAR(100), ").
                append(DatabaseConstant.UserTable.BALLARM).append(" VARCHAR(100), ").
                append(DatabaseConstant.UserTable.USERDTYPE).append(" VARHCAR(50),").
                append(DatabaseConstant.UserTable.BALLAGE).append(" int, ").
                append(DatabaseConstant.UserTable.IDOL).append(" VARCHAR(50), ").
                append(DatabaseConstant.UserTable.IDOL_NAME).append(" VARCHAR(255), ").
                append(DatabaseConstant.UserTable.NEW_IMG).append(" VARCHAR(255), ").
                append(DatabaseConstant.UserTable.NEW_IMG_REAL).append(" VARCHAR(255), ").
//                append(DatabaseConstant.UserTable.)
                append(");");
        db.execSQL(userInfoSql.toString());
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
