package com.yueqiu.db.search;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by scguo on 15/1/10.
 *
 * 创建SearchActivity当中的RoomFragment(球厅Fragment)当中的ListView需要用到的本地缓存的SQLite
 *
 */
public class SearchRoomProvider extends ContentProvider
{
    private static final String TAG = "SearchRoomProvider";

    public static final String AUTHORITY = SearchBaseColumns.AUTHORITY_PREFIX + "SearchRoomProvider";


    private static final String SQL_CREATE_TABLE = "";



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
