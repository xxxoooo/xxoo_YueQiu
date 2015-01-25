package com.yueqiu.dao;

import com.yueqiu.bean.SearchDatingSubFragmentDatingBean;
import com.yueqiu.constant.DatabaseConstant;

import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public interface SearchDatingDao
{

    public long insertDatingItemBatch(List<SearchDatingSubFragmentDatingBean> datingList);

    public long updateDatingItemBatch(List<SearchDatingSubFragmentDatingBean> datingList);

    /**
     * 我们根据距离和发布时间来检索不同的数据
     *
     * @return
     */
    public List<SearchDatingSubFragmentDatingBean> getDatingList(final int startNum, final int limit);

    /**
     *
     * @return 我们所有的已经插入到的数据库当中的数据，没有数目限制
     */
    public List<SearchDatingSubFragmentDatingBean> getAllDatingList();

    public static final String[] columns = {
            DatabaseConstant.FavorInfoItemTable.SearchDatingTable._ID,
            DatabaseConstant.FavorInfoItemTable.SearchDatingTable.USER_ID,
            DatabaseConstant.FavorInfoItemTable.SearchDatingTable.NAME,
            DatabaseConstant.FavorInfoItemTable.SearchDatingTable.PHOTO_URL,
            DatabaseConstant.FavorInfoItemTable.SearchDatingTable.TITLE,
            DatabaseConstant.FavorInfoItemTable.SearchDatingTable.RANGE
    };
}
