package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.yueqiu.bean.NearbyMateSubFragmentUserBean;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.NearbyMateDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 15/1/13.
 *
 * 用于实现在球友的SQL Table同球友Fragment之间建立连接的DAO的具体实现
 *
 */
public class NearbyMateDaoImpl implements NearbyMateDao
{
    private static final String TAG = "NearbyMateDaoImpl";

    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mSQLdatabase;

    public NearbyMateDaoImpl(Context context)
    {
        this.mContext = context;
        this.mDBUtils = DBUtils.getInstance(context);
    }

    @Override
    public synchronized long insertMateItem(NearbyMateSubFragmentUserBean mateItem)
    {
        this.mSQLdatabase = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseConstant.SearchMateTable.USER_ID, mateItem.getUserId());
        values.put(DatabaseConstant.SearchMateTable.NAME, mateItem.getUserNickName());
        values.put(DatabaseConstant.SearchMateTable.PHOTO_URL, mateItem.getUserPhotoUrl());
        values.put(DatabaseConstant.SearchMateTable.SEX, mateItem.getUserGender());
        values.put(DatabaseConstant.SearchMateTable.DISTRICT, mateItem.getUserDistrict());
        values.put(DatabaseConstant.SearchMateTable.RANGE, mateItem.getUserDistance());

        mSQLdatabase = mDBUtils.getWritableDatabase();
        long insertId = mSQLdatabase.insert(
                DatabaseConstant.SearchMateTable.MATE_TABLE,
                null,
                values
        );

        return insertId;
    }

    @Override
    public synchronized long updateMateInfo(NearbyMateSubFragmentUserBean mateItem)
    {
        this.mSQLdatabase = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.SearchMateTable.USER_ID, mateItem.getUserId());
        values.put(DatabaseConstant.SearchMateTable.NAME, mateItem.getUserNickName());
        values.put(DatabaseConstant.SearchMateTable.PHOTO_URL, mateItem.getUserPhotoUrl());
        values.put(DatabaseConstant.SearchMateTable.SEX, mateItem.getUserGender());
        values.put(DatabaseConstant.SearchMateTable.DISTRICT, mateItem.getUserDistrict());
        values.put(DatabaseConstant.SearchMateTable.RANGE, mateItem.getUserDistance());

        return 0;
    }

    @Override
    public synchronized long updateMateInfoBatch(List<NearbyMateSubFragmentUserBean> mateList)
    {
        long result = -1;
        mSQLdatabase = mDBUtils.getWritableDatabase();
        mSQLdatabase.beginTransaction();

        if (mateList != null)
        {
            try
            {
                for (NearbyMateSubFragmentUserBean userBean : mateList)
                {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseConstant.SearchMateTable.USER_ID, userBean.getUserId());
                    values.put(DatabaseConstant.SearchMateTable.NAME, userBean.getUserNickName());
                    values.put(DatabaseConstant.SearchMateTable.PHOTO_URL, userBean.getUserPhotoUrl());
                    values.put(DatabaseConstant.SearchMateTable.SEX, userBean.getUserGender());
                    values.put(DatabaseConstant.SearchMateTable.DISTRICT, userBean.getUserDistrict());
                    values.put(DatabaseConstant.SearchMateTable.RANGE, userBean.getUserDistance());

                    result = mSQLdatabase.update(DatabaseConstant.SearchMateTable.MATE_TABLE,
                            values,
                            DatabaseConstant.SearchMateTable.USER_ID + " =? ",
                            new String[]{userBean.getUserId()});
                }
                mSQLdatabase.setTransactionSuccessful();
            } catch (final Exception e)
            {
                Log.d(TAG, " exception happened while we updating the Search mate table, and the detailed reason are : " + e.toString());
            } finally {
                mSQLdatabase.endTransaction();
            }
        }
        return result;
    }

    /**
     * 用于批零的插入数据
     * 因为Android当中每次同SQLite建立连接的过程都是一个很耗时的过程，所以我们一旦同SQLite建立的连接，
     * 就充分的利用这次连接，将我们的所有的已经获得的数据插入到SQLite当中我们创建的table当中.
     * 在批量插入数据的过程当中我们还要用到database transaction来进行操作
     *
     * @param mateList
     * @return
     */
    @Override
    public synchronized long insertMateItemBatch(List<NearbyMateSubFragmentUserBean> mateList)
    {
        this.mSQLdatabase = mDBUtils.getWritableDatabase();
        long result = 0;
        mSQLdatabase.beginTransaction();
        final int size = mateList.size();
        int i;
        try
        {
            for (i = 0; i < size; ++i)
            {
                ContentValues values = new ContentValues();
                NearbyMateSubFragmentUserBean mateItem = mateList.get(i);
                values.put(DatabaseConstant.SearchMateTable.USER_ID, mateItem.getUserId());
                values.put(DatabaseConstant.SearchMateTable.NAME, mateItem.getUserNickName());
                values.put(DatabaseConstant.SearchMateTable.PHOTO_URL, mateItem.getUserPhotoUrl());
                values.put(DatabaseConstant.SearchMateTable.SEX, mateItem.getUserGender());
                values.put(DatabaseConstant.SearchMateTable.DISTRICT, mateItem.getUserDistrict());
                values.put(DatabaseConstant.SearchMateTable.RANGE, mateItem.getUserDistance());

                result = mSQLdatabase.insert(DatabaseConstant.SearchMateTable.MATE_TABLE, null, values);
            }
            mSQLdatabase.setTransactionSuccessful();
            return result;
        } catch (final Exception e)
        {
            Log.d(TAG, " exception happened in inserting the data into the mate table batch : " + e.toString());
        } finally {
            mSQLdatabase.endTransaction();
        }

        return -1;
    }

    /**
     * 用于获取球友的信息列表
     * 这个获取过程是没有筛选条件的，即将我们所有的插入的数据直接不加筛选的获取到就可以了
     *
     * @param startNum 每次我们请求数据是的数据的ID值，这个值是以我们创建mate表时的USER_ID的值作为判断依据的
     * @param limit
     * @return
     */
    @Override
    public List<NearbyMateSubFragmentUserBean> getMateList(final int startNum, final int limit)
    {
        this.mSQLdatabase = mDBUtils.getReadableDatabase();
        List<NearbyMateSubFragmentUserBean> mateList = new ArrayList<NearbyMateSubFragmentUserBean>();


        String mateInfoSql = " SELECT * FROM " + DatabaseConstant.SearchMateTable.MATE_TABLE
                + " ORDER BY " + DatabaseConstant.SearchMateTable.USER_ID
                + " DESC LIMIT " + startNum + " , " + limit;

        Cursor cursor = mSQLdatabase.rawQuery(
                mateInfoSql,
                null
        );
        
        if (cursor != null && cursor.getCount() != 0)
        {
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                NearbyMateSubFragmentUserBean mateBean = cursorToMateBean(cursor);
                mateList.add(mateBean);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return mateList;
    }

    /**
     * 这个是没有数目限制的，我们可以一次性的将我们在mate table当中保存的所有的数据一次性全部取出来
     *
     * @return 返回我们在mateTable当中保存的所有的数据
     */
    public List<NearbyMateSubFragmentUserBean> getAllMateList()
    {
        this.mSQLdatabase = mDBUtils.getReadableDatabase();
        List<NearbyMateSubFragmentUserBean> resultMateList = new ArrayList<NearbyMateSubFragmentUserBean>();

        String allMateInfoSql = " SELECT * FROM " + DatabaseConstant.SearchMateTable.MATE_TABLE
                + " ORDER BY " + DatabaseConstant.SearchMateTable.USER_ID
                + " DESC ";

        Cursor cursor = mSQLdatabase.rawQuery(
                allMateInfoSql,
                null
        );

        if (null != cursor && cursor.getCount() != 0)
        {
            cursor.moveToFirst();
            while (cursor.isAfterLast())
            {
                NearbyMateSubFragmentUserBean userBean = cursorToMateBean(cursor);
                resultMateList.add(userBean);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return resultMateList;
    }


    /**
     * 这是一个转化类，用于将我们得到的cursor对象直接转换成一个完整的SearchMateSubFragmentUserBean对象，
     * 这样可以使我们的检索所有数据的方法更加的简介，模块化
     *
     * 以下是我们创建Mate表的时候的Columns的创建顺序
     * 0. public static final String _ID = "_id";
     * 1. public static final String USER_ID = "user_id";
     * 2. public static final String NAME = "name";
     * 3. public static final String PHOTO_URL = "photo_url";
     * 4. public static final String SEX = "sex";
     * 5. public static final String DISTRICT = "district"; // 球友的地区
     * 6. public static final String RANGE = "range";
     *
     * @param cursor
     * @return
     */
    private NearbyMateSubFragmentUserBean cursorToMateBean(Cursor cursor)
    {
        NearbyMateSubFragmentUserBean mateBean = new NearbyMateSubFragmentUserBean();
        mateBean.setUserNickName(cursor.getString(2));
        mateBean.setUserPhotoUrl(cursor.getString(3));
        mateBean.setUserGender(cursor.getString(4));
        mateBean.setUserDistrict(cursor.getString(5));
        mateBean.setUserDistance(cursor.getString(6));
        return mateBean;
    }


}






































