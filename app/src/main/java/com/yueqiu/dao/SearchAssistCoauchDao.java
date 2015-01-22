package com.yueqiu.dao;

import com.yueqiu.bean.SearchAssistCoauchSubFragmentBean;
import com.yueqiu.constant.DatabaseConstant;

import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public interface SearchAssistCoauchDao
{

    public long insertAssistCoauchItem(SearchAssistCoauchSubFragmentBean assistCoauchItem);

    public long updateAssistCoauchItem(SearchAssistCoauchSubFragmentBean assistCoauchItem);

    public long insertAssistCoauchItemBatch(List<SearchAssistCoauchSubFragmentBean> assistCoauchList);

    public long updateAssistCoauchItemBatch(List<SearchAssistCoauchSubFragmentBean> assistCoauchList);

    /**
     *
     * @param limit 我们每次请求的数据的条数的限制
     *
     * @return
     */
    public List<SearchAssistCoauchSubFragmentBean> getAssistCoauchList(final int startNum, final int limit);

    public static final String[] allColumns = {
            DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable._ID,
            DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.USER_ID,
            DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.NAME,
            DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.PHOTO_URL,
            DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.CLASS,
            DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.MONEY,
            DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.RANGE,
            DatabaseConstant.FavorInfoItemTable.SearchAssistCoauchTable.SEX
    };
}
