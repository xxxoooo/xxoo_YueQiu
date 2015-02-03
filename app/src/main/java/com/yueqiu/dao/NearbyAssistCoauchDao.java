package com.yueqiu.dao;

import com.yueqiu.bean.NearbyAssistCoauchSubFragmentBean;
import com.yueqiu.constant.DatabaseConstant;

import java.util.List;

/**
 * Created by scguo on 15/1/13.
 */
public interface NearbyAssistCoauchDao
{

    public long insertAssistCoauchItemBatch(List<NearbyAssistCoauchSubFragmentBean> assistCoauchList);

    public long updateAssistCoauchItemBatch(List<NearbyAssistCoauchSubFragmentBean> assistCoauchList);

    /**
     *
     * @param limit 我们每次请求的数据的条数的限制
     *
     * @return
     */
    public List<NearbyAssistCoauchSubFragmentBean> getAssistCoauchList(final int startNum, final int limit);

    public static final String[] allColumns = {
            DatabaseConstant.SearchAssistCoauchTable._ID,
            DatabaseConstant.SearchAssistCoauchTable.USER_ID,
            DatabaseConstant.SearchAssistCoauchTable.NAME,
            DatabaseConstant.SearchAssistCoauchTable.PHOTO_URL,
            DatabaseConstant.SearchAssistCoauchTable.CLASS,
            DatabaseConstant.SearchAssistCoauchTable.MONEY,
            DatabaseConstant.SearchAssistCoauchTable.RANGE,
            DatabaseConstant.SearchAssistCoauchTable.SEX
    };
}
