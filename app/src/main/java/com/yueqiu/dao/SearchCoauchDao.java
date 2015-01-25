package com.yueqiu.dao;

import com.yueqiu.bean.SearchCoauchSubFragmentCoauchBean;
import com.yueqiu.constant.DatabaseConstant;

import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public interface SearchCoauchDao
{


    public long insertCoauchItemBatch(List<SearchCoauchSubFragmentCoauchBean> coauchList);

    public long updateCoauchItemBatch(List<SearchCoauchSubFragmentCoauchBean> coauchList);

    public List<SearchCoauchSubFragmentCoauchBean> getCoauchList(final int startNum, final int limit);

    public List<SearchCoauchSubFragmentCoauchBean> getAllCoauchList();

    // 这是我们对于教练Fragment当中的List所有都可用的Columns(由于我们无论在何时进行何种筛选，所需要获取的都是全部的columns，
    // 所以我们在这里都是全部获取)
    public static final String[] allColumns = {
            DatabaseConstant.FavorInfoItemTable.SearchCoauchTable._ID,
            DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.USER_ID,
            DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.NAME,
            DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.PHOTO_URL,
            DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.CLASS,
            DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.LEVEL,
            DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.RANGE,
            DatabaseConstant.FavorInfoItemTable.SearchCoauchTable.SEX
    };

}
