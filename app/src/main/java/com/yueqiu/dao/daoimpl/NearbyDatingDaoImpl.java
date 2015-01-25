package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.yueqiu.bean.NearbyDatingSubFragmentDatingBean;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.NearbyDatingDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public class NearbyDatingDaoImpl implements NearbyDatingDao
{
    private static final String TAG = "NearbyDatingDaoImpl";

    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDatabase;

    public NearbyDatingDaoImpl(Context context)
    {
        this.mContext = context;
        this.mDBUtils = DBUtils.getInstance(context);
    }
    /**
     * 插入一条约球数据
     *
     * @param datingItem
     * @return
     */
    @Override
    public synchronized long insertDatingItem(NearbyDatingSubFragmentDatingBean datingItem)
    {
        this.mDatabase = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.SearchDatingTable.NAME, datingItem.getUserName());
        values.put(DatabaseConstant.SearchDatingTable.PHOTO_URL, datingItem.getUserPhoto());
        values.put(DatabaseConstant.SearchDatingTable.TITLE, datingItem.getUserDeclare());
        values.put(DatabaseConstant.SearchDatingTable.RANGE, datingItem.getUserDistance());

        this.mDatabase = mDBUtils.getWritableDatabase();
        long insertId = mDatabase.insert(
                DatabaseConstant.SearchDatingTable.DATING_TABLE_NAME,
                null,
                values
        );

        return insertId;
    }



    @Override
    public synchronized long updateDatingItem(NearbyDatingSubFragmentDatingBean datingItem)
    {
        return 0;
    }

    @Override
    public synchronized long insertDatingItemBatch(List<NearbyDatingSubFragmentDatingBean> datingList)
    {
        this.mDatabase = mDBUtils.getWritableDatabase();
        final int size = datingList.size();
        long result = 0;
        mDatabase.beginTransaction();
        int i;
        try
        {
            for (i = 0; i < size; ++i)
            {
                ContentValues values = new ContentValues();
                NearbyDatingSubFragmentDatingBean datingItem = datingList.get(i);
                values.put(DatabaseConstant.SearchDatingTable.NAME, datingItem.getUserName());
                values.put(DatabaseConstant.SearchDatingTable.PHOTO_URL, datingItem.getUserPhoto());
                values.put(DatabaseConstant.SearchDatingTable.TITLE, datingItem.getUserDeclare());
                values.put(DatabaseConstant.SearchDatingTable.RANGE, datingItem.getUserDistance());

                result = mDatabase.insert(
                        DatabaseConstant.SearchDatingTable.DATING_TABLE_NAME,
                        null,
                        values);
            }
            mDatabase.setTransactionSuccessful();
            return result;
        } catch (final Exception e)
        {
            Log.d(TAG, " exception happened while we inserting the list of data into the table, and the reason are : " + e.toString());
        } finally {
            mDatabase.endTransaction();
        }

        return -1;
    }


    @Override
    public synchronized long updateDatingItemBatch(List<NearbyDatingSubFragmentDatingBean> datingList)
    {
        return 0;
    }

    @Override
    public List<NearbyDatingSubFragmentDatingBean> getDatingList(final int startNum, final int limit)
    {
        this.mDatabase = mDBUtils.getReadableDatabase();
        List<NearbyDatingSubFragmentDatingBean> datingBeanList = new ArrayList<NearbyDatingSubFragmentDatingBean>();

        String datingInfoSql = " SELECT * FROM " + DatabaseConstant.SearchDatingTable.DATING_TABLE_NAME
                + " ORDER BY " + DatabaseConstant.SearchDatingTable.USER_ID
                + " DESC LIMIT " + startNum + " , " + limit;

        Cursor cursor = mDatabase.rawQuery(
                datingInfoSql,
                null
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            NearbyDatingSubFragmentDatingBean bean = cursorToDatingBean(cursor);
            datingBeanList.add(bean);
            cursor.moveToNext();
        }
        cursor.close();


        return datingBeanList;
    }

    /**
     * 以下是我们创建约球的表的时候创建各个Column时的从顺序
     *
     * 0. public static final String _ID = "_id";
     * 1. public static final String USER_ID = "user_id";
     * 2. public static final String NAME = "username";
     * 3. public static final String PHOTO_URL = "photo_url";
     * 4. public static final String TITLE = "title"; // 当前所发布的约球的主题内容(例如"大奖赛开幕了，一起参加"的形式的字符串)
     * 5. public static final String RANGE = "range";
     *
     * @param cursor
     * @return
     */
    private NearbyDatingSubFragmentDatingBean cursorToDatingBean(Cursor cursor)
    {
        NearbyDatingSubFragmentDatingBean bean = new NearbyDatingSubFragmentDatingBean();
        bean.setUserName(cursor.getString(2));
        bean.setUserPhoto(cursor.getString(3));
        bean.setUserDeclare(cursor.getString(4));
        bean.setUserDistance(cursor.getString(5));

        return bean;
    }

}











































