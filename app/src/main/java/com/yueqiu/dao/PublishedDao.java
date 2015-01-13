package com.yueqiu.dao;

import com.yueqiu.bean.PublishedInfo;

import java.util.List;

/**
 * Created by wangyun on 15/1/12.
 */
public interface PublishedDao {

    public long insertPublishInfo(PublishedInfo info);

    public long insertPublishItemInfo(PublishedInfo info);

    public long updatePublishInfo(PublishedInfo info);

    public List<Long> updatePublishedItemInfo(PublishedInfo info);

    public boolean isExistPublishedInfo(int type);

    public boolean isExistPublishedItemInfo(int tableId,int type);

    public PublishedInfo getPublishedInfo(String userId,int type);
}
