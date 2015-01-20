package com.yueqiu.dao;

import com.yueqiu.bean.SearchAssistCoauchSubFragmentBean;

import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public interface SearchAssistCoauchDao
{

    public long insertAssistCoauchItem(SearchAssistCoauchSubFragmentBean assistCoauchItem);

    public long updateAssistCoauchItem(SearchAssistCoauchSubFragmentBean assistCoauchItem);

    /**
     *
     * @param distance 距离
     * @param cost 花费(即请助教的费用)
     * @param clazz 助教的球种
     * @param level 助教的水平
     * @return
     */
    public List<SearchAssistCoauchSubFragmentBean> getAssistCoauchList(String distance, String cost, String clazz, String level);

}
