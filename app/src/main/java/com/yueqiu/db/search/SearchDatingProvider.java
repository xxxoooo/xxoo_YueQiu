package com.yueqiu.db.search;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by scguo on 15/1/10.
 *
 * 创建用于SearchActivity当中的DatingFragment(约球Fragment)当中需要用到的本地缓存SQLite
 */
public class SearchDatingProvider extends ContentProvider
{
    private static final String TAG = "SearchDatingProvider";


    private static final String DATING_TABLE_NAME = "dating_table";

    private static final String CREATE_SQL = " CREATE TABLE "
            + DATING_TABLE_NAME + " ( "
            + SearchBaseColumns.DatingColumnds._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SearchBaseColumns.DatingColumnds.USER_ID + " TEXT NOT NULL, "
            + SearchBaseColumns.DatingColumnds.NAME + " TEXT NOT NULL, "
            + SearchBaseColumns.DatingColumnds.TITLE + " TEXT NOT NULL, "
            + SearchBaseColumns.DatingColumnds.RANGE + " TEXT NOT NULL); ";



    public static final String AUTHORITY = SearchBaseColumns.AUTHORITY_PREFIX + "SearchDatingProvider";

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
