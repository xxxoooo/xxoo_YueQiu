package com.yueqiu.dao;

import com.yueqiu.bean.SearchDatingSubFragmentDatingBean;

import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public interface SearchDatingDao
{

    public long insertDatingItem(SearchDatingSubFragmentDatingBean datingItem);

    public long updateDatingItem(SearchDatingSubFragmentDatingBean datingItem);

    /**
     * 我们根据距离和发布时间来检索不同的数据
     *
     * @param distance 距离
     * @param publishedDate 约球的发布时间
     * @return
     */
    public List<SearchDatingSubFragmentDatingBean> getDatingList(String distance, String publishedDate);

}
