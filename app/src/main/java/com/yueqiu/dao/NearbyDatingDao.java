package com.yueqiu.dao;

import com.yueqiu.bean.NearbyDatingSubFragmentDatingBean;
import com.yueqiu.constant.DatabaseConstant;

import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public interface NearbyDatingDao
{

    public long insertDatingItem(NearbyDatingSubFragmentDatingBean datingItem);

    public long updateDatingItem(NearbyDatingSubFragmentDatingBean datingItem);

    public long insertDatingItemBatch(List<NearbyDatingSubFragmentDatingBean> datingList);

    public long updateDatingItemBatch(List<NearbyDatingSubFragmentDatingBean> datingList);

    /**
     * 我们根据距离和发布时间来检索不同的数据
     *
     * @return
     */
    public List<NearbyDatingSubFragmentDatingBean> getDatingList(final int startNum, final int limit);

    public static final String[] columns = {
            DatabaseConstant.SearchDatingTable._ID,
            DatabaseConstant.SearchDatingTable.USER_ID,
            DatabaseConstant.SearchDatingTable.NAME,
            DatabaseConstant.SearchDatingTable.PHOTO_URL,
            DatabaseConstant.SearchDatingTable.TITLE,
            DatabaseConstant.SearchDatingTable.RANGE
    };
}
