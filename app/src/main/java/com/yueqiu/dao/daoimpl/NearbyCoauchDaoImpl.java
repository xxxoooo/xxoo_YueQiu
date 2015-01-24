package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.yueqiu.bean.NearbyCoauchSubFragmentCoauchBean;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.NearbyCoauchDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public class NearbyCoauchDaoImpl implements NearbyCoauchDao
{
    private static final String TAG = "NearbyCoauchDaoImpl";

    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDatabase;

    public NearbyCoauchDaoImpl(Context context)
    {
        this.mContext = context;
        this.mDBUtils = DBUtils.getInstance(context);

    }

    /**
     * 插入一条完整的教练信息
     *
     * @param coauchItem
     * @return
     */
    @Override
    public synchronized long insertCoauchItem(NearbyCoauchSubFragmentCoauchBean coauchItem)
    {
        this.mDatabase = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.SearchCoauchTable.NAME, coauchItem.getUserName());
        values.put(DatabaseConstant.SearchCoauchTable.PHOTO_URL, coauchItem.getUserPhoto());
        values.put(DatabaseConstant.SearchCoauchTable.SEX, coauchItem.getUserGender());
        values.put(DatabaseConstant.SearchCoauchTable.RANGE, coauchItem.getUserDistance());
        values.put(DatabaseConstant.SearchCoauchTable.CLASS, coauchItem.getmBilliardKind());
        values.put(DatabaseConstant.SearchCoauchTable.LEVEL, coauchItem.getUserLevel());

        this.mDatabase = mDBUtils.getWritableDatabase();
        long insertId = mDatabase.insert(
                DatabaseConstant.SearchCoauchTable.COAUCH_TABLE_NAME,
                null,
                values
        );

        return insertId;
    }

    @Override
    public synchronized long updateCoauchItem(NearbyCoauchSubFragmentCoauchBean coauchItem)
    {
        return 0;
    }

    @Override
    public synchronized long insertCoauchItemBatch(List<NearbyCoauchSubFragmentCoauchBean> coauchList)
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
                NearbyCoauchSubFragmentCoauchBean coauchItem = coauchList.get(i);
                ContentValues values = new ContentValues();
                values.put(DatabaseConstant.SearchCoauchTable.NAME, coauchItem.getUserName());
                values.put(DatabaseConstant.SearchCoauchTable.PHOTO_URL, coauchItem.getUserPhoto());
                values.put(DatabaseConstant.SearchCoauchTable.SEX, coauchItem.getUserGender());
                values.put(DatabaseConstant.SearchCoauchTable.RANGE, coauchItem.getUserDistance());
                values.put(DatabaseConstant.SearchCoauchTable.CLASS, coauchItem.getmBilliardKind());
                values.put(DatabaseConstant.SearchCoauchTable.LEVEL, coauchItem.getUserLevel());

                insertResult = mDatabase.insert(
                        DatabaseConstant.SearchCoauchTable.COAUCH_TABLE_NAME,
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
    public synchronized long updateCoauchItemBatch(List<NearbyCoauchSubFragmentCoauchBean> coauchList)
    {
        return 0;
    }

    @Override
    public List<NearbyCoauchSubFragmentCoauchBean> getCoauchList(final int startNum, final int limit)
    {
        this.mDatabase = mDBUtils.getReadableDatabase();
        List<NearbyCoauchSubFragmentCoauchBean> coauchList = new ArrayList<NearbyCoauchSubFragmentCoauchBean>();


        String coauchInfoSql = " SELECT * FROM " + DatabaseConstant.SearchCoauchTable.COAUCH_TABLE_NAME
                + " ORDER BY " + DatabaseConstant.SearchCoauchTable.USER_ID
                + " DESC LIMIT " + startNum + " , " + limit;

        Cursor cursor = mDatabase.rawQuery(
                coauchInfoSql,
                null
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            NearbyCoauchSubFragmentCoauchBean coauchBean = cursorToCoauchBean(cursor);
            coauchList.add(coauchBean);
            cursor.moveToNext();
        }

        cursor.close();
        Log.d(TAG, " the finally coauch list we get from the coauch table are : " + coauchList.size());
        return coauchList;
    }




    /**
     *
     * 以下是我们创建Coauch表的时候，创建Columns的顺序
     * 0. public static final String _ID = "_id";
     * 1. public static final String USER_ID = "user_id";
     * 2. public static final String NAME = "username";
     * 3. public static final String PHOTO_URL = "photo_url";
     * 4. public static final String CLASS = "class"; // 球种(例如九球，斯诺克等)
     * 5. public static final String LEVEL = "level"; // 教练的资质(例如国家队还是北京队)
     * 6. public static final String RANGE = "range";
     * 7. public static final String SEX = "sex";
     *
     * @param cursor
     * @return
     */
    private NearbyCoauchSubFragmentCoauchBean cursorToCoauchBean(Cursor cursor)
    {
        NearbyCoauchSubFragmentCoauchBean coauchBean = new NearbyCoauchSubFragmentCoauchBean();
        coauchBean.setUserName(cursor.getString(2));
        coauchBean.setUserPhoto(cursor.getString(3));
        coauchBean.setBilliardKind(cursor.getString(4));
        coauchBean.setUserLevel(cursor.getString(5));
        coauchBean.setUserDistance(cursor.getString(6));
        coauchBean.setUserGender(cursor.getString(7));

        return coauchBean;
    }
}
