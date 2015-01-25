package com.yueqiu.dao;

import com.yueqiu.bean.SearchRoomSubFragmentRoomBean;
import com.yueqiu.constant.DatabaseConstant;

import java.util.List;

/**
 * Created by scguo on 15/1/13.
 *
 * 用于搜索当前的可用的球厅的接口
 *
 */
public interface SearchRoomDao
{
    public long updateRoomItemBatch(List<SearchRoomSubFragmentRoomBean> roomList);

    public long insertRoomItemBatch(List<SearchRoomSubFragmentRoomBean> roomList);

    /**
     * 当用户进行球厅信息的获取时，会有根据球厅的区域，距离，价格以及好评度四项标准进行筛选
     * @param limit 每次我们可以获取到的数据条数(我们不能一次就将所有的数据一次性检索出来)
     *
     * @return 一条完整的关于球厅信息，即一个RoomBean
     *
     */
    public List<SearchRoomSubFragmentRoomBean> getRoomList(final int startNum, final int limit);

    /**
     * 直接返回我们存储的所有的List，没有条数限制，用于判断我们
     * 是否重复插入了某些数据
     *
     * @return
     */
    public List<SearchRoomSubFragmentRoomBean> getAllRoomList();

    public static final String[] allColumns = {
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
}
