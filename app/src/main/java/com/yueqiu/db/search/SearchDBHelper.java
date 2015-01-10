package com.yueqiu.db.search;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.awt.font.TextAttribute;
import java.io.StringReader;

/**
 * Created by scguo on 15/1/10.
 *
 * 用于SearchActivity当中所有已经创建的table的SQLite helper类
 *
 */
public class SearchDBHelper extends SQLiteOpenHelper
{
    private static final String TAG = "SearchDBHelper";

    private final String CREATE_SQL;
    private final String TABLE_NAME;
    private final String COLUMN_NAME;

    public SearchDBHelper(Context context, String dbName, int dbVersion, String createSql, String tableName, String columnName)
    {
        super(context, dbName, null, dbVersion);
        this.CREATE_SQL = createSql;
        this.TABLE_NAME = tableName;
        this.COLUMN_NAME = columnName;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.d(TAG, " create the database, and the creation sql are : " + CREATE_SQL);
        db.execSQL(CREATE_SQL);

        // TODO: 在教程当中还提供了一个用于创建INDEX_SQL的部分，但是不知道具体的用法
        // TODO: ???????????????????????????????????????????????????????

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion < newVersion)

        {
            db.execSQL("ALTER TABLE " + TABLE_NAME +
                    " ADD COLUMN" + COLUMN_NAME + " TEXT");
            // TODO: 在教程当中还提供了一个用于创建INDEX_SQL的部分，但是不知道具体的用法
            // TODO: ???????????????????????????????????????????????????????
//            db.execSQL();
        }
    }
}
