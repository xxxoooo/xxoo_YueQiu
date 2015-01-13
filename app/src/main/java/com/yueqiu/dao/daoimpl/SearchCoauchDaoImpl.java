package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

import com.yueqiu.bean.SearchCoauchSubFragmentCoauchBean;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.SearchCoauchDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public class SearchCoauchDaoImpl implements SearchCoauchDao
{
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
    public List<SearchCoauchSubFragmentCoauchBean> getCoauchList(String level, String clazz)
    {
        List<SearchCoauchSubFragmentCoauchBean> coauchList = new ArrayList<SearchCoauchSubFragmentCoauchBean>();

        String[] allColumns = {
                DatabaseConstant.SearchCoauchTable._ID,
                DatabaseConstant.SearchCoauchTable.USER_ID,
                DatabaseConstant.SearchCoauchTable.NAME,
                DatabaseConstant.SearchCoauchTable.PHOTO_URL,
                DatabaseConstant.SearchCoauchTable.CLASS,
                DatabaseConstant.SearchCoauchTable.LEVEL,
                DatabaseConstant.SearchCoauchTable.RANGE,
                DatabaseConstant.SearchCoauchTable.SEX
        };

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
