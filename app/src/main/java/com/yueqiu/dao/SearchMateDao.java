package com.yueqiu.dao;

import com.yueqiu.bean.SearchMateSubFragmentUserBean;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by scguo on 15/1/13.
 */
public interface SearchMateDao
{
    public long insertMateItem(SearchMateSubFragmentUserBean mateItem);

    public long updateMateInfo(SearchMateSubFragmentUserBean mateImte);

    /**
     * 任何用户都可以在没有登录的情况下查看当前所有的球友列表的信息的，并不会限制于用户的具体的UserId
     * 但是我们在获取球友列表的时候，会有一个情况就是用户可以根据球友的距离和性别进行筛选
     *
     * @param distance 距离
     * @param gender 性别
     * @return
     */
    public List<SearchMateSubFragmentUserBean> getMateList(String distance, String gender);
}
