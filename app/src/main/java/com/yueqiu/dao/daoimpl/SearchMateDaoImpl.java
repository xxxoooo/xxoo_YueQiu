package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

import com.yueqiu.bean.SearchMateSubFragmentUserBean;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.SearchMateDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 15/1/13.
 *
 * 用于实现在球友的SQL Table同球友Fragment之间建立连接的DAO的具体实现
 *
 */
public class SearchMateDaoImpl implements SearchMateDao
{
    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mSQLdatabase;

    public SearchMateDaoImpl(Context context)
    {
        this.mContext = context;
        this.mDBUtils = DBUtils.getInstance(context);
        // 因为我们DAO的作用不仅仅是获取数据，还会涉及到添加数据，所以我们需要得到的是WritableDatabase，而不是ReadableDatabase


    }

    @Override
    public long insertMateItem(SearchMateSubFragmentUserBean mateItem)
    {
        ContentValues values = new ContentValues();

        values.put(DatabaseConstant.FavorInfoItemTable.SearchMateTable.NAME, mateItem.getUserNickName());
        values.put(DatabaseConstant.FavorInfoItemTable.SearchMateTable.PHOTO_URL, mateItem.getUserPhotoUrl());
        values.put(DatabaseConstant.FavorInfoItemTable.SearchMateTable.SEX, mateItem.getUserGender());
        values.put(DatabaseConstant.FavorInfoItemTable.SearchMateTable.DISTRICT, mateItem.getUserDistrict());
        values.put(DatabaseConstant.FavorInfoItemTable.SearchMateTable.RANGE, mateItem.getUserDistance());

        mSQLdatabase = mDBUtils.getWritableDatabase();
        long insertId = mSQLdatabase.insert(
                DatabaseConstant.FavorInfoItemTable.SearchMateTable.MATE_TABLE,
                null,
                values
        );

        return insertId;
    }

    @Override
    public long updateMateInfo(SearchMateSubFragmentUserBean mateImte)
    {
        return 0;
    }

    /**
     * 用于获取球友的信息列表
     *
     * @param distance 距离
     * @param gender 性别
     * @return
     */
    @Override
    public List<SearchMateSubFragmentUserBean> getMateList(String distance, String gender)
    {
        List<SearchMateSubFragmentUserBean> mateList = new ArrayList<SearchMateSubFragmentUserBean>();
        String[] allColumns = {
                DatabaseConstant.FavorInfoItemTable.SearchMateTable._ID,
                DatabaseConstant.FavorInfoItemTable.SearchMateTable.USER_ID,
                DatabaseConstant.FavorInfoItemTable.SearchMateTable.NAME,
                DatabaseConstant.FavorInfoItemTable.SearchMateTable.PHOTO_URL,
                DatabaseConstant.FavorInfoItemTable.SearchMateTable.SEX,
                DatabaseConstant.FavorInfoItemTable.SearchMateTable.DISTRICT,
                DatabaseConstant.FavorInfoItemTable.SearchMateTable.RANGE
        };
        mSQLdatabase = mDBUtils.getReadableDatabase();
        Cursor cursor = mSQLdatabase.query(
                DatabaseConstant.FavorInfoItemTable.SearchMateTable.MATE_TABLE, // table 用于查询的表的名字
                allColumns, // columns 查询之后我们所需要返回的所有的Column的集合
                null, // selection 用于过滤的语句
                null, // selectionArgs
                null, // groupBy
                null, // having
                null // orderBy
        );



        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            SearchMateSubFragmentUserBean mateBean = cursorToMateBean(cursor);
            mateList.add(mateBean);
            cursor.moveToNext();
        }
        cursor.close();

        return mateList;
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
    private SearchMateSubFragmentUserBean cursorToMateBean(Cursor cursor)
    {
        SearchMateSubFragmentUserBean mateBean = new SearchMateSubFragmentUserBean();
        mateBean.setUserNickName(cursor.getString(2));
        mateBean.setUserPhotoUrl(cursor.getString(3));
        mateBean.setUserGender(cursor.getString(4));
        mateBean.setUserDistrict(cursor.getString(5));
        mateBean.setUserDistance(cursor.getString(6));
        return mateBean;
    }


}






































