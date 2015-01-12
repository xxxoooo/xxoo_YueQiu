package com.yueqiu.db.search;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.io.StringReader;

/**
 * Created by scguo on 15/1/10.
 *
 * 创建用于SearchActivity当中的CoauchFramgnet(教练Fragment)当中的ListView需要用到的本地缓存的SQLite
 */
public class SearchCoauchProvider extends ContentProvider
{
    private static final String TAG = "SearchCoauchProvider";

    public static final String AUTHORITY = SearchBaseColumns.AUTHORITY_PREFIX + "SearchCoauchProvider";

    private static final String COAUCH_TABLE_NAME = "coauch_table";

    private static final String CREATE_SQL = " CREATE TABLE "
            + COAUCH_TABLE_NAME + " ( "
            + SearchBaseColumns.CoauchColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SearchBaseColumns.CoauchColumns.USER_ID + " TEXT NOT NULL , "
            + SearchBaseColumns.CoauchColumns.NAME + " TEXT NOT NULL , "
            + SearchBaseColumns.CoauchColumns.SEX + " TEXT NOT NULL , "
            + SearchBaseColumns.CoauchColumns.CLASS + " TEXT NOT NULL , "
            + SearchBaseColumns.CoauchColumns.LEVEL + " TEXT NOT NULL, "
            + SearchBaseColumns.CoauchColumns.RANGE + " TEXT NOT NULL); ";

    @Override
    public boolean onCreate()
    {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        return null;
    }

    @Override
    public String getType(Uri uri)
    {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        return 0;
    }
}
