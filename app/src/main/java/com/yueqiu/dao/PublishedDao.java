package com.yueqiu.dao;

import com.yueqiu.bean.PublishedInfo;

import java.util.List;

/**
 * Created by wangyun on 15/1/12.
 */
public interface PublishedDao {


    public long insertPublishInfo(List<PublishedInfo> list);

    public long updatePublishInfo(List<PublishedInfo> list);

    public List<PublishedInfo> getPublishedInfo(int userId,int type,int start,int num);

    public List<PublishedInfo> getAllPublishedInfo(int userId);

//    public long insertPublishInfo(PublishedInfo info);
//
//    public long insertPublishItemInfo(PublishedInfo info);
//
//    public long updatePublishInfo(PublishedInfo info);
//
//    public List<Long> updatePublishedItemInfo(PublishedInfo info);
//
//    public boolean isExistPublishedInfo(int type);
//
//    public boolean isExistPublishedItemInfo(int tableId,int type);
//
//    public PublishedInfo getPublishedInfo(String userId,int type,int start,int end);
}
