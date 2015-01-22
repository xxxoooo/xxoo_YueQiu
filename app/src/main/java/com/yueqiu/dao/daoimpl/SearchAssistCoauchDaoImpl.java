package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.yueqiu.bean.SearchAssistCoauchSubFragmentBean;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.SearchAssistCoauchDao;
import com.yueqiu.db.DBUtils;

import org.apache.http.impl.DefaultHttpServerConnection;
import org.w3c.dom.Text;

import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by scguo on 15/1/13.
 */
public class SearchAssistCoauchDaoImpl implements SearchAssistCoauchDao
{
    private static final String TAG = "SearchAssistCoauchDaoImpl";


    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDatabase;

    public SearchAssistCoauchDaoImpl(Context context)
    {
        this.mContext = context;
        this.mDBUtils = DBUtils.getInstance(context);


    }

    /**
     * 插入一条完整的助教的信息
     *
     * @param assistCoauchItem
     * @return
     */
    @Override
    public synchronized long insertAssistCoauchItem(SearchAssistCoauchSubFragmentBean assistCoauchItem)
    {
        this.mDatabase = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.NAME, assistCoauchItem.getName());
        values.put(DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.SEX, assistCoauchItem.getGender());
        values.put(DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.MONEY, assistCoauchItem.getPrice());
        values.put(DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.PHOTO_URL, assistCoauchItem.getPhoto());
        values.put(DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.CLASS, assistCoauchItem.getKinds());
        values.put(DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.RANGE, assistCoauchItem.getDistance());

        this.mDatabase = mDBUtils.getWritableDatabase();
        long insertId = mDatabase.insert(
                DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.ASSISTCOAUCH_TABLE_NAME,
                null,
                values
        );

        return insertId;
    }

    @Override
    public synchronized long updateAssistCoauchItem(SearchAssistCoauchSubFragmentBean assistCoauchItem)
    {
        return 0;
    }

    @Override
    public synchronized long insertAssistCoauchItemBatch(List<SearchAssistCoauchSubFragmentBean> assistCoauchList)
    {
        this.mDatabase = mDBUtils.getWritableDatabase();
        long insertResult = 0;
        final int size = assistCoauchList.size();
        int i;
        mDatabase.beginTransaction();
        try
        {
            for (i = 0; i < size; ++i)
            {
                ContentValues values = new ContentValues();
                SearchAssistCoauchSubFragmentBean assistCoauchItem = assistCoauchList.get(i);
                // TODO: 但是我们现在确不确定，插入userId是否是有必要的一个字段，因为SearchActivity当中所有的内容都是不需要user在登录的情况下就可以查看的
                values.put(DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.USER_ID, assistCoauchItem.getUserId());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.NAME, assistCoauchItem.getName());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.SEX, assistCoauchItem.getGender());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.MONEY, assistCoauchItem.getPrice());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.PHOTO_URL, assistCoauchItem.getPhoto());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.CLASS, assistCoauchItem.getKinds());
                values.put(DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.RANGE, assistCoauchItem.getDistance());

                insertResult = mDatabase.insert(
                        DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.ASSISTCOAUCH_TABLE_NAME,
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
    public synchronized long updateAssistCoauchItemBatch(List<SearchAssistCoauchSubFragmentBean> assistCoauchList)
    {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public List<SearchAssistCoauchSubFragmentBean> getAssistCoauchList(final int startNum, final int limit)
    {
        this.mDatabase = mDBUtils.getReadableDatabase();
        List<SearchAssistCoauchSubFragmentBean> asList = new ArrayList<SearchAssistCoauchSubFragmentBean>();


        String asInfoSql = " SELECT * FROM" + DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.ASSISTCOAUCH_TABLE_NAME
                + " ORDER BY " + DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.USER_ID
                + " DESC LIMIT " + startNum + " , " + limit;

        // 这是最基本的筛选操作，得到的是全部的list
        Cursor cursor = mDatabase.rawQuery(
                asInfoSql,
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
    private SearchAssistCoauchSubFragmentBean cursorToAssistCoauch(Cursor cursor)
    {
        SearchAssistCoauchSubFragmentBean bean = new SearchAssistCoauchSubFragmentBean();
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
