package com.yueqiu.dao;

import com.yueqiu.bean.SearchCoauchSubFragmentCoauchBean;
import com.yueqiu.constant.DatabaseConstant;

import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public interface SearchCoauchDao
{

    public long insertCoauchItem(SearchCoauchSubFragmentCoauchBean coauchItem);

    public long updateCoauchItem(SearchCoauchSubFragmentCoauchBean coauchItem);

    /**
     *
     * @param level 教练的资质
     * @param clazz 教练的球种
     * @return
     */
    public List<SearchCoauchSubFragmentCoauchBean> getCoauchList(String level, String clazz, final int limit);

    /**
     * 我们对于教练Fragment当中的List的筛选，不是一个排序的过程，而是直接的一个select的过程，就是按照我们指定的level
     * 进行筛选就可以了，不需要进行其他的额外的排序
     *
     * @param level
     * @return
     */
    public List<SearchCoauchSubFragmentCoauchBean> getCouchListWithLevelFiltered(String level, final int limit);

    /**
     * 我们对于教练Fragment当中的List的筛选，不是一个排序的过程，而是直接的一个select的过程，就是按照我们指定的clazz
     * 直接进行select操作就可以了，不用排序
     *
     * 当然对于助教Fragment当中的花费(price)筛选条件，是一个sort的过程，即按我们获得的price的值进行sorting就可以了，
     * 而不是一个select的过程了
     *
     * @param clazz
     * @return
     */
    public List<SearchCoauchSubFragmentCoauchBean> getCouchListWithKindsFiltered(String clazz, final int limit);

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
