package com.yueqiu.dao;

import com.yueqiu.bean.NearbyCoauchSubFragmentCoauchBean;
import com.yueqiu.constant.DatabaseConstant;

import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public interface NearbyCoauchDao
{

    public long insertCoauchItem(NearbyCoauchSubFragmentCoauchBean coauchItem);

    public long updateCoauchItem(NearbyCoauchSubFragmentCoauchBean coauchItem);

    public long insertCoauchItemBatch(List<NearbyCoauchSubFragmentCoauchBean> coauchList);

    public long updateCoauchItemBatch(List<NearbyCoauchSubFragmentCoauchBean> coauchList);

    public List<NearbyCoauchSubFragmentCoauchBean> getCoauchList(final int startNum, final int limit);

    // 这是我们对于教练Fragment当中的List所有都可用的Columns(由于我们无论在何时进行何种筛选，所需要获取的都是全部的columns，
    // 所以我们在这里都是全部获取)
    public static final String[] allColumns = {
            DatabaseConstant.SearchCoauchTable._ID,
            DatabaseConstant.SearchCoauchTable.USER_ID,
            DatabaseConstant.SearchCoauchTable.NAME,
            DatabaseConstant.SearchCoauchTable.PHOTO_URL,
            DatabaseConstant.SearchCoauchTable.CLASS,
            DatabaseConstant.SearchCoauchTable.LEVEL,
            DatabaseConstant.SearchCoauchTable.RANGE,
            DatabaseConstant.SearchCoauchTable.SEX
    };

}
