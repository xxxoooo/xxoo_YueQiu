package com.yueqiu.dao;

import com.yueqiu.bean.NearbyMateSubFragmentUserBean;
import com.yueqiu.constant.DatabaseConstant;

import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public interface NearbyMateDao
{
    public long insertMateItem(NearbyMateSubFragmentUserBean mateItem);

    public long updateMateInfo(NearbyMateSubFragmentUserBean mateItem);

    public long updateMateInfoBatch(List<NearbyMateSubFragmentUserBean> mateList);

    public long insertMateItemBatch(List<NearbyMateSubFragmentUserBean> mateList);

    /**
     * 任何用户都可以在没有登录的情况下查看当前所有的球友列表的信息的，并不会限制于用户的具体的UserId
     * 但是我们在获取球友列表的时候，会有一个情况就是用户可以根据球友的距离和性别进行筛选
     *
     * @return
     */
    public List<NearbyMateSubFragmentUserBean> getMateList(final int startNum, final int limit);

    public static final String[] allColumns = {
            DatabaseConstant.SearchMateTable._ID,
            DatabaseConstant.SearchMateTable.USER_ID,
            DatabaseConstant.SearchMateTable.NAME,
            DatabaseConstant.SearchMateTable.PHOTO_URL,
            DatabaseConstant.SearchMateTable.SEX,
            DatabaseConstant.SearchMateTable.DISTRICT,
            DatabaseConstant.SearchMateTable.RANGE
    };

}
