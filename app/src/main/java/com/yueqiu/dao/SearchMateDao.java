package com.yueqiu.dao;

import com.yueqiu.bean.SearchMateSubFragmentUserBean;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by scguo on 15/1/13.
 */
public interface SearchMateDao
{
    public long updateMateInfoBatch(List<SearchMateSubFragmentUserBean> mateList);

    public long insertMateItemBatch(List<SearchMateSubFragmentUserBean> mateList);

    /**
     * 任何用户都可以在没有登录的情况下查看当前所有的球友列表的信息的，并不会限制于用户的具体的UserId
     * 但是我们在获取球友列表的时候，会有一个情况就是用户可以根据球友的距离和性别进行筛选
     *
     * 这个是有数目限制的，即我们每次只是请求10条
     * @return
     */
    public List<SearchMateSubFragmentUserBean> getMateList(final int startNum, final int limit);

    /**
     * 这个是没有数目限制的，我们可以一次性的将我们在mate table当中保存的所有的数据一次性全部取出来
     *
     * @return 返回我们在mateTable当中保存的所有的数据
     */
    public List<SearchMateSubFragmentUserBean> getAllMateList();


    public static final String[] allColumns = {
            DatabaseConstant.FavorInfoItemTable.SearchMateTable._ID,
            DatabaseConstant.FavorInfoItemTable.SearchMateTable.USER_ID,
            DatabaseConstant.FavorInfoItemTable.SearchMateTable.NAME,
            DatabaseConstant.FavorInfoItemTable.SearchMateTable.PHOTO_URL,
            DatabaseConstant.FavorInfoItemTable.SearchMateTable.SEX,
            DatabaseConstant.FavorInfoItemTable.SearchMateTable.DISTRICT,
            DatabaseConstant.FavorInfoItemTable.SearchMateTable.RANGE
    };

}
