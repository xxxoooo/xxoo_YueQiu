package com.yueqiu.dao;

import com.yueqiu.bean.FavorInfo;

import java.util.List;

/**
 * Created by wangyun on 15/1/13.
 */
public interface FavorDao {

    public long insertFavorInfo(FavorInfo info);

    public long insertFavorItemInfo(FavorInfo info);

    public long updateFavorInfo(FavorInfo info);

    public List<Long> updateFavorItemInfo(FavorInfo info);

    public boolean isExistFavorInfo(int type);

    public boolean isExistFavorItemInfo(int tableId,int type);

    public FavorInfo getFavorInfo(String userId,int type,int start,int num);
}
