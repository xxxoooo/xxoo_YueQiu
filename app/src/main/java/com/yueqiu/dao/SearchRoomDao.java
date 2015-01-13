package com.yueqiu.dao;

import com.yueqiu.bean.SearchRoomSubFragmentRoomBean;

import java.util.List;

/**
 * Created by scguo on 15/1/13.
 *
 * 用于搜索当前的可用的球厅的接口
 *
 */
public interface SearchRoomDao
{
    public long insertRoomItem(SearchRoomSubFragmentRoomBean roomItem);

    public long updateRoomItem(SearchRoomSubFragmentRoomBean roomItem);

    /**
     * 当用户进行球厅信息的获取时，会有根据球厅的区域，距离，价格以及好评度四项标准进行筛选
     * @param district 区域
     * @param distance 距离
     * @param price 价格
     * @param level 好评度
     * @return 一条完整的关于球厅信息，即一个RoomBean
     */
    public List<SearchRoomSubFragmentRoomBean> getRoomList(String district, String distance, String price, String level);
}
