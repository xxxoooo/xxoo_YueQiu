package com.yueqiu.db.search;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by scguo on 15/1/10.
 *
 * 创建用于SearchActivity当中的AssistCoauchFragment(助教Fragment)当中的ListView需要用到的本地缓存的SQLite
 *
 */
public class SearchAssistCoauchProvider extends ContentProvider
{
    private static final String TAG = "SearchAssistCoauchProvider";

    public static final String AUTHORITY = SearchBaseColumns.AUTHORITY_PREFIX + "SearchAssistCoauchProvider";

    private static final String ASSISTCOAUCH_TABLE_NAME = "assist_coauch_table";

    private static final String CREATE_SQL = " CREATE TABLE "
            + ASSISTCOAUCH_TABLE_NAME + " ( "
            + SearchBaseColumns.AssistCoauchColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SearchBaseColumns.AssistCoauchColumns.USER_ID + " TEXT NOT NULL, "
            + SearchBaseColumns.AssistCoauchColumns.NAME + " TEXT NOT NULL, "
            + SearchBaseColumns.AssistCoauchColumns.SEX + " TEXT NOT NULL, "
            + SearchBaseColumns.AssistCoauchColumns.CLASS + " TEXT NOT NULL, "
            + SearchBaseColumns.AssistCoauchColumns.MONEY + " TEXT NOT NULL, "
            + SearchBaseColumns.AssistCoauchColumns.RANGE + " TEXT NOT NULL); ";

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
