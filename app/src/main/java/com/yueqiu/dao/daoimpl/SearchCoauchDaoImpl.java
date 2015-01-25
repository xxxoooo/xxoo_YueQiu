package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;

import com.yueqiu.bean.SearchCoauchSubFragmentCoauchBean;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.SearchCoauchDao;
import com.yueqiu.db.DBUtils;

import org.apache.http.impl.DefaultHttpServerConnection;

import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public class SearchCoauchDaoImpl implements SearchCoauchDao
{
    private static final String TAG = "SearchCoauchDaoImpl";

    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDatabase;

    public SearchCoauchDaoImpl(Context context)
    {
        this.mContext = context;
        this.mDBUtils = DBUtils.getInstance(context);

    }

    @Override
    public synchronized long insertCoauchItemBatch(List<SearchCoauchSubFragmentCoauchBean> coauchList)
    {
        this.mDatabase = mDBUtils.getWritableDatabase();
        long insertResult = 0;

        final int size = coauchList.size();
        int i;
        mDatabase.beginTransaction();
        try
        {
            for (i = 0; i < size; ++i)
            {
                SearchCoauchSubFragmentCoauchBean coauchItem = coauchList.get(i);
                ContentValues values = new ContentValues();
                values.put(DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.NAME, coauchItem.getUserName());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.PHOTO_URL, coauchItem.getUserPhoto());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.SEX, coauchItem.getUserGender());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.CLASS, coauchItem.getmBilliardKind());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.LEVEL, coauchItem.getUserLevel());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.RANGE, coauchItem.getUserDistance());

                insertResult = mDatabase.insert(
                        DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.COAUCH_TABLE_NAME,
                        null,
                        values
                );
            }

            mDatabase.setTransactionSuccessful();
            return insertResult;
        } catch (final Exception e)
        {
            Log.d(TAG, " exception happened while we insert data into the Coauch table by batch, and the reason are : " + e.toString());
        } finally {
            mDatabase.endTransaction();
        }

        return -1;
    }

    @Override
    public synchronized long updateCoauchItemBatch(List<SearchCoauchSubFragmentCoauchBean> coauchList)
    {
        long result = -1;
        mDatabase = mDBUtils.getWritableDatabase();
        mDatabase.beginTransaction();
        try
        {
            for (SearchCoauchSubFragmentCoauchBean coauchItem : coauchList)
            {
                ContentValues values = new ContentValues();
                values.put(DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.NAME, coauchItem.getUserName());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.PHOTO_URL, coauchItem.getUserPhoto());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.SEX, coauchItem.getUserGender());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.CLASS, coauchItem.getmBilliardKind());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.LEVEL, coauchItem.getUserLevel());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.RANGE, coauchItem.getUserDistance());

                result = mDatabase.update(DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.COAUCH_TABLE_NAME,
                        values,
                        DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.USER_ID + " =? ",
                        new String[]{coauchItem.getId()});
            }
            mDatabase.setTransactionSuccessful();
        } catch (final Exception e)
        {
            Log.d(TAG, " exception happened while we updating the coauch table, and the reason goes to : " + e.toString());
        } finally
        {
            mDatabase.endTransaction();
        }

        return result;
    }

    @Override
    public List<SearchCoauchSubFragmentCoauchBean> getCoauchList(final int startNum, final int limit)
    {
        this.mDatabase = mDBUtils.getReadableDatabase();
        List<SearchCoauchSubFragmentCoauchBean> coauchList = new ArrayList<SearchCoauchSubFragmentCoauchBean>();

        String coauchInfoSql = " SELECT * FROM " + DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.COAUCH_TABLE_NAME
                + " ORDER BY " + DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.USER_ID
                + " DESC LIMIT " + startNum + " , " + limit;

        Cursor cursor = mDatabase.rawQuery(
                coauchInfoSql,
                null
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            SearchCoauchSubFragmentCoauchBean coauchBean = cursorToCoauchBean(cursor);
            coauchList.add(coauchBean);
            cursor.moveToNext();
        }

        cursor.close();
        Log.d(TAG, " the finally coauch list we get from the coauch table are : " + coauchList.size());
        return coauchList;
    }

    @Override
    public List<SearchCoauchSubFragmentCoauchBean> getAllCoauchList()
    {
        this.mDatabase = mDBUtils.getReadableDatabase();
        List<SearchCoauchSubFragmentCoauchBean> coauchList = new ArrayList<SearchCoauchSubFragmentCoauchBean>();

        String coauchInfoSql = " SELECT * FROM " + DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.COAUCH_TABLE_NAME
                + " ORDER BY " + DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.USER_ID
                + " DESC ";

        Cursor cursor = mDatabase.rawQuery(
                coauchInfoSql,
                null
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            SearchCoauchSubFragmentCoauchBean coauchBean = cursorToCoauchBean(cursor);
            coauchList.add(coauchBean);
            cursor.moveToNext();
        }

        cursor.close();
        Log.d(TAG, " the finally coauch list we get from the coauch table are : " + coauchList.size());
        return coauchList;

    }

    /**
     *
     * @param cursor
     * @return
     */
    private SearchCoauchSubFragmentCoauchBean cursorToCoauchBean(Cursor cursor)
    {
        SearchCoauchSubFragmentCoauchBean coauchBean = new SearchCoauchSubFragmentCoauchBean();
        coauchBean.setId(cursor.getString(1));
        coauchBean.setUserName(cursor.getString(2));
        coauchBean.setUserPhoto(cursor.getString(3));
        coauchBean.setUserGender(cursor.getString(4));
        coauchBean.setBilliardKind(cursor.getString(5));
        coauchBean.setUserLevel(cursor.getString(6));
        coauchBean.setUserDistance(cursor.getString(7));

        return coauchBean;
    }
}
