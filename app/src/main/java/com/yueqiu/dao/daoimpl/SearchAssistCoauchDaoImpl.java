package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yueqiu.bean.SearchAssistCoauchSubFragmentBean;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.SearchAssistCoauchDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by scguo on 15/1/13.
 */
public class SearchAssistCoauchDaoImpl implements SearchAssistCoauchDao
{
    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDatabase;

    public SearchAssistCoauchDaoImpl(Context context)
    {
        this.mContext = context;
        this.mDBUtils = DBUtils.getInstance(context);
        this.mDatabase = mDBUtils.getWritableDatabase();

    }

    /**
     * 插入一条完整的助教的信息
     *
     * @param assistCoauchItem
     * @return
     */
    @Override
    public long insertAssistCoauchItem(SearchAssistCoauchSubFragmentBean assistCoauchItem)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.SearchAssistCoauchTable.NAME, assistCoauchItem.getName());
        values.put(DatabaseConstant.SearchAssistCoauchTable.SEX, assistCoauchItem.getGender());
        values.put(DatabaseConstant.SearchAssistCoauchTable.MONEY, assistCoauchItem.getPrice());
        values.put(DatabaseConstant.SearchAssistCoauchTable.PHOTO_URL, assistCoauchItem.getPhoto());
        values.put(DatabaseConstant.SearchAssistCoauchTable.CLASS, assistCoauchItem.getKinds());
        values.put(DatabaseConstant.SearchAssistCoauchTable.RANGE, assistCoauchItem.getDistance());

        long insertId = mDatabase.insert(
                DatabaseConstant.SearchAssistCoauchTable.ASSISTCOAUCH_TABLE_NAME,
                null,
                values
        );

        return insertId;
    }

    @Override
    public long updateAssistCoauchItem(SearchAssistCoauchSubFragmentBean assistCoauchItem)
    {
        return 0;
    }

    /**
     *
     * @param distance 距离
     * @param cost 花费(即请助教的费用)
     * @param clazz 助教的球种
     * @param level 助教的水平
     * @return
     */
    @Override
    public List<SearchAssistCoauchSubFragmentBean> getAssistCoauchList(String distance, String cost, String clazz, String level)
    {
        List<SearchAssistCoauchSubFragmentBean> asList = new ArrayList<SearchAssistCoauchSubFragmentBean>();

        String[] allColumns = {
                DatabaseConstant.SearchAssistCoauchTable._ID,
                DatabaseConstant.SearchAssistCoauchTable.USER_ID,
                DatabaseConstant.SearchAssistCoauchTable.NAME,
                DatabaseConstant.SearchAssistCoauchTable.PHOTO_URL,
                DatabaseConstant.SearchAssistCoauchTable.CLASS,
                DatabaseConstant.SearchAssistCoauchTable.MONEY,
                DatabaseConstant.SearchAssistCoauchTable.RANGE,
                DatabaseConstant.SearchAssistCoauchTable.SEX
        };

        Cursor cursor = mDatabase.query(
                DatabaseConstant.SearchAssistCoauchTable.ASSISTCOAUCH_TABLE_NAME,
                allColumns,
                null,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();
        while (! cursor.isAfterLast())
        {
            SearchAssistCoauchSubFragmentBean asBean = cursorToAssistCoauch(cursor);
            asList.add(asBean);
            cursor.moveToNext();
        }

        cursor.close();
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
    private SearchAssistCoauchSubFragmentBean cursorToAssistCoauch(Cursor cursor)
    {
        SearchAssistCoauchSubFragmentBean bean = new SearchAssistCoauchSubFragmentBean();



        return bean;
    }

}
