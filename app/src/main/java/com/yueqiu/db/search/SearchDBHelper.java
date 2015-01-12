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

    /**
     *
     * @param context
     * @param dbName 数据库的名字(Search数据库当中一共包含了5张表，分别是mateTable, datingTable, coauchTable, searchCoauchTable, roomTable)
     * @param dbVersion 当前数据库的版本
     * @param createSql 创建数据库当中的具体的表的sql语句
     * @param tableName 所创建的数据库当中的表的名字
     * @param columnName 说实话，这个字段目前还没有用到？？？
     */
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
            Log.d(TAG, " we need to execute the updating of the database we created ");

            db.execSQL("ALTER TABLE " + TABLE_NAME +
                    " ADD COLUMN" + COLUMN_NAME + " TEXT");
            // TODO: 在教程当中还提供了一个用于创建INDEX_SQL的部分，但是不知道具体的用法
            // TODO: ???????????????????????????????????????????????????????
//            db.execSQL();
        }
    }
}
