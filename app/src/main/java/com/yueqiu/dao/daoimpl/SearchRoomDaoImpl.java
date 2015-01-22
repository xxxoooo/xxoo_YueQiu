package com.yueqiu.dao.daoimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

import com.yueqiu.bean.SearchRoomSubFragmentRoomBean;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.dao.SearchRoomDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public class SearchRoomDaoImpl implements SearchRoomDao
{
    private Context mContext;
    private DBUtils mDBUtils;
    private SQLiteDatabase mDatabase;

    public SearchRoomDaoImpl(Context context)
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
    public long insertRoomItem(SearchRoomSubFragmentRoomBean roomItem)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstant.FavorInfoItemTable.SearchRoomTable.NAME, roomItem.getRoomName());
        values.put(DatabaseConstant.FavorInfoItemTable.SearchRoomTable.ROOM_URL, roomItem.getRoomPhotoUrl());
        values.put(DatabaseConstant.FavorInfoItemTable.SearchRoomTable.ROOM_LEVEL, roomItem.getLevel());
        values.put(DatabaseConstant.FavorInfoItemTable.SearchRoomTable.RANGE, roomItem.getDistance());
        values.put(DatabaseConstant.FavorInfoItemTable.SearchRoomTable.DETAILED_ADDRESS, roomItem.getDetailedAddress());

        this.mDatabase = mDBUtils.getWritableDatabase();
        long insertId = mDatabase.insert(
                DatabaseConstant.FavorInfoItemTable.SearchRoomTable.ROOM_TABLE_NAME,
                null, // null column hack
                values
        );

        return insertId;
    }

    @Override
    public long updateRoomItem(SearchRoomSubFragmentRoomBean roomItem)
    {
        return 0;
    }

    /**
     * 通过特定的筛选条件使我们获得的数据按照筛选条件进行排序,
     * 在进行排序的时候，我们可以将这些String解析成Integer或者Float，然后就可以直接通过
     * 使用SQL的检索语句进行排序了
     *
     * @param district 区域，这个值默认是筛选条件列表当中的第一个区
     * @param distance 距离，我们就直接将最原始获得的数据展现给用户，然后用户可以再次通过距离来进行筛选
     * @param price 价格
     * @param level 好评度
     * @return
     */
    @Override
    public List<SearchRoomSubFragmentRoomBean> getRoomList(String district, String distance, String price, String level)
    {
        List<SearchRoomSubFragmentRoomBean> roomList = new ArrayList<SearchRoomSubFragmentRoomBean>();
        String[] allColumns = {
                DatabaseConstant.FavorInfoItemTable.SearchRoomTable._ID,
                DatabaseConstant.FavorInfoItemTable.SearchRoomTable.ROOM_ID,
                DatabaseConstant.FavorInfoItemTable.SearchRoomTable.NAME,
                DatabaseConstant.FavorInfoItemTable.SearchRoomTable.ROOM_URL,
                DatabaseConstant.FavorInfoItemTable.SearchRoomTable.ROOM_LEVEL,
                DatabaseConstant.FavorInfoItemTable.SearchRoomTable.RANGE,
                DatabaseConstant.FavorInfoItemTable.SearchRoomTable.PHONE_NUM,
                DatabaseConstant.FavorInfoItemTable.SearchRoomTable.TAG,
                DatabaseConstant.FavorInfoItemTable.SearchRoomTable.DETAILED_ADDRESS
        };

        // TODO: 以下这种检索只是单纯的将所有的数据不加选择的一次性检索出来(这也是我们默认的检索加载方式)
        // TODO: 现在客户端的实现准则是所有的数据都是先从Server端的Service当中检索
        // TODO: 出来，然后存到我们建立的本地数据库当中。然后再从数据库当中进行检索
        // TODO: 这样我们所经过的筛选条件就是完整的SQL语句进行检索了，而不是通过添加请求参数进行检索的
        this.mDatabase = mDBUtils.getReadableDatabase();
        Cursor cursor = mDatabase.query(
                DatabaseConstant.FavorInfoItemTable.SearchRoomTable.ROOM_TABLE_NAME,
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
            SearchRoomSubFragmentRoomBean roomBean = cursorToRoomBean(cursor);
            roomList.add(roomBean);
            cursor.moveToNext();
        }
        cursor.close();

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
    private SearchRoomSubFragmentRoomBean cursorToRoomBean(Cursor cursor)
    {
        SearchRoomSubFragmentRoomBean roomBean = new SearchRoomSubFragmentRoomBean();
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






































