package com.yueqiu.dao;

import com.yueqiu.bean.MyCollectionInfo;

import java.util.List;

/**
 * Created by wangyun on 15/1/13.
 */
public interface MyCollectionDao {

    public long insertMyCollectionInfo(MyCollectionInfo info);

    public long insertMyCollectionItemInfo(MyCollectionInfo info);

    public long updateMyCollectionInfo(MyCollectionInfo info);

    public List<Long> updateMyCollectionItemInfo(MyCollectionInfo info);

    public boolean isExistMyCollectionInfo(int type);

    public boolean isExistMyCollectionItemInfo(int tableId,int type);

    public MyCollectionInfo getMyCollectionInfo(String userId,int type);
}
