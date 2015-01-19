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
        this.mDatabase = mDBUtils.getWritableDatabase();
    }

    /**
     * 插入一条完整的教练信息
     *
     * @param coauchItem
     * @return
     */
    @Override
    public long insertCoauchItem(SearchCoauchSubFragmentCoauchBean coauchItem)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.SearchCoauchTable.NAME, coauchItem.getUserName());
        values.put(DatabaseConstant.SearchCoauchTable.PHOTO_URL, coauchItem.getUserPhoto());
        values.put(DatabaseConstant.SearchCoauchTable.SEX, coauchItem.getUserGender());
        values.put(DatabaseConstant.SearchCoauchTable.RANGE, coauchItem.getUserDistance());
        values.put(DatabaseConstant.SearchCoauchTable.CLASS, coauchItem.getmBilliardKind());
        values.put(DatabaseConstant.SearchCoauchTable.LEVEL, coauchItem.getUserLevel());

        long insertId = mDatabase.insert(
                DatabaseConstant.SearchCoauchTable.COAUCH_TABLE_NAME,
                null,
                values
        );

        return insertId;
    }

    @Override
    public long updateCoauchItem(SearchCoauchSubFragmentCoauchBean coauchItem)
    {
        return 0;
    }

    @Override
    public List<SearchCoauchSubFragmentCoauchBean> getCoauchList(String level, String clazz, final int limit)
    {
        List<SearchCoauchSubFragmentCoauchBean> coauchList = new ArrayList<SearchCoauchSubFragmentCoauchBean>();

        Cursor cursor = mDatabase.query(
                DatabaseConstant.SearchCoauchTable.COAUCH_TABLE_NAME,
                allColumns,
                null,
                null,
                null,
                null,
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

        return coauchList;
    }

    /**
     * 我们对于教练Fragment当中的List的筛选，不是一个排序的过程，而是直接的一个select的过程，就是按照我们指定的level
     * 进行筛选就可以了，不需要进行其他的额外的排序
     *
     * @param level
     * @return
     */
    @Override
    public List<SearchCoauchSubFragmentCoauchBean> getCouchListWithLevelFiltered(String level, final int limit)
    {
        Log.d(TAG, " the level we need to filter out are : " + level);
        List<SearchCoauchSubFragmentCoauchBean> coauchLevelFilteredList = new ArrayList<SearchCoauchSubFragmentCoauchBean>();
        Cursor levelCursor = mDatabase.query(
                DatabaseConstant.SearchCoauchTable.COAUCH_TABLE_NAME,
                allColumns,
                "",
                null,
                "",
                "",
                "",
                ""
        );


        return null;
    }

    /**
     * 我们对于教练Fragment当中的List的筛选，不是一个排序的过程，而是直接的一个select的过程，就是按照我们指定的clazz
     * 直接进行select操作就可以了，不用排序
     * <p/>
     * 当然对于助教Fragment当中的花费(price)筛选条件，是一个sort的过程，即按我们获得的price的值进行sorting就可以了，
     * 而不是一个select的过程了
     *
     * @param clazz
     * @return
     */
    @Override
    public List<SearchCoauchSubFragmentCoauchBean> getCouchListWithKindsFiltered(String clazz, final int limit)
    {
        List<SearchCoauchSubFragmentCoauchBean> coauchKindsFilteredList = new ArrayList<SearchCoauchSubFragmentCoauchBean>();



        return null;
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
    private SearchCoauchSubFragmentCoauchBean cursorToCoauchBean(Cursor cursor)
    {
        SearchCoauchSubFragmentCoauchBean coauchBean = new SearchCoauchSubFragmentCoauchBean();
        coauchBean.setUserName(cursor.getString(2));
        coauchBean.setUserPhoto(cursor.getString(3));
        coauchBean.setBilliardKind(cursor.getString(4));
        coauchBean.setUserLevel(cursor.getString(5));
        coauchBean.setUserDistance(cursor.getString(6));
        coauchBean.setUserGender(cursor.getString(7));

        return coauchBean;
    }
}
