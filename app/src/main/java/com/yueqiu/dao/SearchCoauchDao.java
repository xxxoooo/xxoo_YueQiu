package com.yueqiu.dao;

import com.yueqiu.bean.SearchCoauchSubFragmentCoauchBean;

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
    public List<SearchCoauchSubFragmentCoauchBean> getCoauchList(String level, String clazz);

}
