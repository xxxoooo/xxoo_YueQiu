package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.yueqiu.bean.NearbyRoomSubFragmentRoomBean;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.NearbyRoomDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public class NearbyRoomDaoImpl implements NearbyRoomDao
{
    private static final String TAG = "NearbyRoomDaoImpl";

    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDatabase;

    public NearbyRoomDaoImpl(Context context)
    {
        this.mContext = context;
        this.mDBUtils = DBUtils.getInstance(context);

    }

    /**
     * 向我们创建的RoomTable当中插入一条RoomBean数据
     *
     * @param roomItem
     * @return
     */
    @Override
    public synchronized long insertRoomItem(NearbyRoomSubFragmentRoomBean roomItem)
    {
        this.mDatabase = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.SearchRoomTable.NAME, roomItem.getRoomName());
        values.put(DatabaseConstant.SearchRoomTable.ROOM_URL, roomItem.getRoomPhotoUrl());
        values.put(DatabaseConstant.SearchRoomTable.ROOM_LEVEL, roomItem.getLevel());
        values.put(DatabaseConstant.SearchRoomTable.RANGE, roomItem.getDistance());
        values.put(DatabaseConstant.SearchRoomTable.DETAILED_ADDRESS, roomItem.getDetailedAddress());

        this.mDatabase = mDBUtils.getWritableDatabase();
        long insertId = mDatabase.insert(
                DatabaseConstant.SearchRoomTable.ROOM_TABLE_NAME,
                null, // null column hack
                values
        );
        return insertId;
    }

    @Override
    public synchronized long updateRoomItem(NearbyRoomSubFragmentRoomBean roomItem)
    {
        return 0;
    }

    @Override
    public synchronized long updateRoomItemBatch(List<NearbyRoomSubFragmentRoomBean> roomList)
    {
        return 0;
    }

    @Override
    public synchronized long insertRoomItemBatch(List<NearbyRoomSubFragmentRoomBean> roomList)
    {
        this.mDatabase = mDBUtils.getWritableDatabase();
        long insertResult = 0;

        final int size = roomList.size();
        int i;
        mDatabase.beginTransaction();
        try
        {
            for (i = 0; i < size; ++i)
            {
                ContentValues values = new ContentValues();
                NearbyRoomSubFragmentRoomBean roomItem = new NearbyRoomSubFragmentRoomBean();
                values.put(DatabaseConstant.SearchRoomTable.ROOM_ID, roomItem.getRoomId());
                values.put(DatabaseConstant.SearchRoomTable.NAME, roomItem.getRoomName());
                values.put(DatabaseConstant.SearchRoomTable.ROOM_URL, roomItem.getRoomPhotoUrl());
                values.put(DatabaseConstant.SearchRoomTable.ROOM_LEVEL, roomItem.getLevel());
                values.put(DatabaseConstant.SearchRoomTable.RANGE, roomItem.getDistance());
                values.put(DatabaseConstant.SearchRoomTable.DETAILED_ADDRESS, roomItem.getDetailedAddress());

                insertResult = mDatabase.insert(
                        DatabaseConstant.SearchRoomTable.ROOM_TABLE_NAME,
                        null, // null column hack
                        values
                );
                mDatabase.setTransactionSuccessful();
                return insertResult;
            }
        } catch (final Exception e)
        {
            Log.d(TAG, " exception happened while we insert data into the room table, and the reason are : " + e.toString());
        } finally {
            mDatabase.endTransaction();
        }

        return -1;
    }

    /**
     * 通过特定的筛选条件使我们获得的数据按照筛选条件进行排序,
     * 在进行排序的时候，我们可以将这些String解析成Integer或者Float，然后就可以直接通过
     * 使用SQL的检索语句进行排序了
     *
     * @param startNum 这就是我们每次开始请求的数据的开始的条目数,我们需要结合startNum的值和limit的值来
     *                 实现上拉刷新(即滑动到ListView的最底部的时候执行加载更多)
     * @param limit 每次我们可以获取到的数据条数(我们不能一次就将所有的数据一次性检索出来)
     *
     * @return
     */
    @Override
    public List<NearbyRoomSubFragmentRoomBean> getRoomList(final int startNum, final int limit)
    {
        this.mDatabase = mDBUtils.getReadableDatabase();
        Log.d(TAG, " we have retrieved the data set from the room table, and the start number are : " + startNum + " , the limit are : " + limit);
        List<NearbyRoomSubFragmentRoomBean> roomList = new ArrayList<NearbyRoomSubFragmentRoomBean>();

        String roomInfoSql = " SELECT * FROM " + DatabaseConstant.SearchRoomTable.ROOM_TABLE_NAME
                + " ORDER BY " + DatabaseConstant.SearchRoomTable.ROOM_ID
                + " DESC LIMIT " + startNum + " , " + limit;

        // TODO: 以下这种检索只是单纯的将所有的数据不加选择的一次性检索出来(这也是我们默认的检索加载方式)
        // TODO: 现在客户端的实现准则是所有的数据都是先从Server端的Service当中检索
        // TODO: 出来，然后存到我们建立的本地数据库当中。然后再从数据库当中进行检索
        // TODO: 这样我们所经过的筛选条件就是完整的SQL语句进行检索了，而不是通过添加请求参数进行检索的

        Cursor cursor = mDatabase.rawQuery(
                roomInfoSql,
                null);

        cursor.moveToFirst();
        while (! cursor.isAfterLast())
        {
            NearbyRoomSubFragmentRoomBean roomBean = cursorToRoomBean(cursor);
            roomList.add(roomBean);
            cursor.moveToNext();
        }
        cursor.close();
        Log.d(TAG, " we have get the room info list we just need, and the content are : " + roomList.size());

        return roomList;
    }



    // TODO: 现阶段球厅的table具体字段内容还没有确定，还需要服务器端的进一步确定
    /**
     * 以下是RoomTable创建时各个Column创建的准确顺序:
     * 0. public static final String _ID = "_id";
     * 1. public static final String ROOM_ID = "room_id";
     * 2. public static final String NAME = "room_name";
     * 3. public static final String ROOM_URL = "room_url";
     * 4. public static final String DETAILED_ADDRESS = "detailed_address";
     * 5. public static final String ROOM_LEVEL = "room_level"; // 球厅的星级
     * 6. public static final String RANGE = "range";
     * 7. public static final String PHONE_NUM = "phone_num";
     * 8. public static final String TAG = "tag";
     * 9. public static final String DETAILED_INFO = "room_info";
     *
     * @param cursor
     *
     * @return 将一个Cursor对象转换成我们的bean对象
     */
    private NearbyRoomSubFragmentRoomBean cursorToRoomBean(Cursor cursor)
    {
        NearbyRoomSubFragmentRoomBean roomBean = new NearbyRoomSubFragmentRoomBean();
        roomBean.setRoomId(cursor.getString(1));
        roomBean.setRoomName(cursor.getString(2));
        roomBean.setRoomPhotoUrl(cursor.getString(3));
        roomBean.setDetailedAddress(cursor.getString(4));
        roomBean.setLevel(Integer.parseInt(cursor.getString(5)));
        roomBean.setDistance(cursor.getString(6));
        roomBean.setRoomPhone(cursor.getString(7));
        roomBean.setRoomTag(cursor.getString(8));
        roomBean.setDetailedAddress(cursor.getString(9));

        return roomBean;
    }
}






































