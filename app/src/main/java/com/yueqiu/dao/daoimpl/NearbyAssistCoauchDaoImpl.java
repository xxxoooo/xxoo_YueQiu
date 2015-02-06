package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.yueqiu.bean.NearbyAssistCoauchSubFragmentBean;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.NearbyAssistCoauchDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public class NearbyAssistCoauchDaoImpl implements NearbyAssistCoauchDao
{
    private static final String TAG = "NearbyAssistCoauchDaoImpl";

    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDatabase;

    public NearbyAssistCoauchDaoImpl(Context context)
    {
        this.mContext = context;
        this.mDBUtils = DBUtils.getInstance(context);
    }


    @Override
    public synchronized long insertAssistCoauchItemBatch(List<NearbyAssistCoauchSubFragmentBean> assistCoauchList)
    {
        this.mDatabase = mDBUtils.getWritableDatabase();
        long insertResult = 0;
        mDatabase.beginTransaction();
        try
        {
            for (NearbyAssistCoauchSubFragmentBean assistCoauchItem : assistCoauchList)
            {
                ContentValues values = new ContentValues();
                values.put(DatabaseConstant.SearchAssistCoauchTable.USER_ID, assistCoauchItem.getUserId());
                values.put(DatabaseConstant.SearchAssistCoauchTable.NAME, assistCoauchItem.getName());
                values.put(DatabaseConstant.SearchAssistCoauchTable.SEX, assistCoauchItem.getGender());
                values.put(DatabaseConstant.SearchAssistCoauchTable.MONEY, assistCoauchItem.getPrice());
                values.put(DatabaseConstant.SearchAssistCoauchTable.PHOTO_URL, assistCoauchItem.getPhoto());
                values.put(DatabaseConstant.SearchAssistCoauchTable.CLASS, assistCoauchItem.getKinds());
                values.put(DatabaseConstant.SearchAssistCoauchTable.RANGE, assistCoauchItem.getDistance());

                insertResult = mDatabase.insert(
                        DatabaseConstant.SearchAssistCoauchTable.ASSISTCOAUCH_TABLE_NAME,
                        null,
                        values
                );
            }
            mDatabase.setTransactionSuccessful();

            return insertResult;
        } catch (final Exception e)
        {
            Log.d(TAG, " exception happened while we inserting data into the AssistCoauch table, reason are : " + e.toString());
        } finally {
            mDatabase.endTransaction();
        }
        return -1;
    }

    @Override
    public synchronized long updateAssistCoauchItemBatch(List<NearbyAssistCoauchSubFragmentBean> assistCoauchList)
    {
        long updateResult = -1;
        mDatabase = mDBUtils.getWritableDatabase();
        mDatabase.beginTransaction();
        try
        {
            for (NearbyAssistCoauchSubFragmentBean assistCoauchItem : assistCoauchList)
            {
                ContentValues values = new ContentValues();
                values.put(DatabaseConstant.SearchAssistCoauchTable.USER_ID, assistCoauchItem.getUserId());
                values.put(DatabaseConstant.SearchAssistCoauchTable.NAME, assistCoauchItem.getName());
                values.put(DatabaseConstant.SearchAssistCoauchTable.SEX, assistCoauchItem.getGender());
                values.put(DatabaseConstant.SearchAssistCoauchTable.MONEY, assistCoauchItem.getPrice());
                values.put(DatabaseConstant.SearchAssistCoauchTable.PHOTO_URL, assistCoauchItem.getPhoto());
                values.put(DatabaseConstant.SearchAssistCoauchTable.CLASS, assistCoauchItem.getKinds());
                values.put(DatabaseConstant.SearchAssistCoauchTable.RANGE, assistCoauchItem.getDistance());

                updateResult = mDatabase.update(
                        DatabaseConstant.SearchAssistCoauchTable.ASSISTCOAUCH_TABLE_NAME,
                        values,
                        DatabaseConstant.SearchAssistCoauchTable.USER_ID + " =? ",
                        new String[]{assistCoauchItem.getUserId()}
                );
            }
            mDatabase.setTransactionSuccessful();

        } catch (final Exception e)
        {
            Log.d(TAG, " Exception happened while we updating the AssistCoauch table, and the reason goes to : " + e.toString());
        } finally {
            mDatabase.endTransaction();
        }

        return updateResult;
    }

    /**
     * @return
     */
    @Override
    public List<NearbyAssistCoauchSubFragmentBean> getAssistCoauchList(final int startNum, final int limit)
    {
        this.mDatabase = mDBUtils.getReadableDatabase();
        List<NearbyAssistCoauchSubFragmentBean> asList = new ArrayList<NearbyAssistCoauchSubFragmentBean>();


        String asInfoSql = " SELECT * FROM" + DatabaseConstant.SearchAssistCoauchTable.ASSISTCOAUCH_TABLE_NAME
                + " ORDER BY " + DatabaseConstant.SearchAssistCoauchTable.USER_ID
                + " DESC LIMIT " + startNum + " , " + limit;

        // 这是最基本的筛选操作，得到的是全部的list
        Cursor cursor = mDatabase.rawQuery(
                asInfoSql,
                null
        );
        cursor.moveToFirst();
        while (! cursor.isAfterLast())
        {
            NearbyAssistCoauchSubFragmentBean asBean = cursorToAssistCoauch(cursor);
            asList.add(asBean);
            cursor.moveToNext();
        }

        cursor.close();
        Log.d(TAG, " the finally assistCoauch list we get are : " + asList.size());

        return asList;
    }

    public List<NearbyAssistCoauchSubFragmentBean> getAllASCoauchList()
    {
        this.mDatabase = mDBUtils.getReadableDatabase();
        List<NearbyAssistCoauchSubFragmentBean> asList = new ArrayList<NearbyAssistCoauchSubFragmentBean>();


        String asInfoSql = " SELECT * FROM" + DatabaseConstant.SearchAssistCoauchTable.ASSISTCOAUCH_TABLE_NAME
                + " ORDER BY " + DatabaseConstant.SearchAssistCoauchTable.USER_ID
                + " DESC ";

        // 这是最基本的筛选操作，得到的是全部的list
        Cursor cursor = mDatabase.rawQuery(
                asInfoSql,
                null
        );
        cursor.moveToFirst();
        while (! cursor.isAfterLast())
        {
            NearbyAssistCoauchSubFragmentBean asBean = cursorToAssistCoauch(cursor);
            asList.add(asBean);
            cursor.moveToNext();
        }

        cursor.close();
        Log.d(TAG, " the finally assistCoauch list we get are : " + asList.size());

        return asList;
    }

    /**
     * 以下是助教表创建Columns时的准确顺序，如果顺序乱了，检索到的数据就不对了
     * 0. public static final String _ID = "_id";
     * 1. public static final String USER_ID = "user_id";
     * 3. public static final String NAME = "username";
     * 4. public static final String PHOTO_URL = "photo_url";
     * 5. public static final String CLASS = "class"; // 球种
     * 6. public static final String MONEY = "money"; // 助教的费用
     * 7. public static final String RANGE = "range";
     * 8. public static final String SEX = "sex";
     *
     *
     * @param cursor
     * @return
     */
    private NearbyAssistCoauchSubFragmentBean cursorToAssistCoauch(Cursor cursor)
    {
        NearbyAssistCoauchSubFragmentBean bean = new NearbyAssistCoauchSubFragmentBean();
        bean.setUserId(cursor.getString(1));
        bean.setName(cursor.getString(2));
        bean.setPhoto(cursor.getString(3));
        bean.setKinds(cursor.getString(4));
        bean.setPrice(cursor.getString(5));
        bean.setDistance(cursor.getString(6));
        bean.setGender(cursor.getString(7));

        return bean;
    }
}
