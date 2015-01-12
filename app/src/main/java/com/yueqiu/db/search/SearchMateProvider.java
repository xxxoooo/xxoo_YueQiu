package com.yueqiu.db.search;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.yueqiu.db.DBUtils;

/**
 * Created by scguo on 15/1/10.
 *
 * 创建用于SearchActivity当中的MateFragment当中的ListView需要用到的本地缓存SQLite
 *
 */
public class SearchMateProvider extends ContentProvider
{
    private static final String TAG = "SearchMateProvider";

    public static final String AUTHORITY = SearchBaseColumns.AUTHORITY_PREFIX + "SearchMateFragment";

    public static final String MATE_TABLE = "mate_table";

    public static final String CREATE_SQL = " CREATE TABLE "
            + MATE_TABLE + " ( "
            + SearchBaseColumns.MateColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SearchBaseColumns.MateColumns.USER_ID + " TEXT NOT NULL, "
            + SearchBaseColumns.MateColumns.NAME + " TEXT NOT NULL, "
            + SearchBaseColumns.MateColumns.SEX + " TEXT NOT NULL, "
            + SearchBaseColumns.MateColumns.DISTRICT + " TEXT NOT NULL, "
            + SearchBaseColumns.MateColumns.RANGE + " TEXT NOT NULL); ";




    private static final String KEY_COLUMN_NAME = SearchBaseColumns.MateColumns.USER_ID;

    private SearchDBHelper mMateSQLiteHelper;

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // 初始化UriMatcher(用于权限配置申请和其他的数据Retrieve)
    static
    {
//        sUriMatcher.addURI(AUTHORITY, "", "");

    }



    @Override
    public boolean onCreate()
    {
        mMateSQLiteHelper = new SearchDBHelper(getContext(), SearchBaseColumns.SEARCH_DB_NAME, SearchBaseColumns.SEARCH_DB_VERSION, CREATE_SQL, MATE_TABLE, KEY_COLUMN_NAME);
        return true;
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
