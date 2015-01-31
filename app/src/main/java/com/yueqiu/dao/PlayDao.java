package com.yueqiu.dao;

import com.yueqiu.bean.PlayInfo;

import java.util.List;

/**
 * Created by yinfeng on 15/1/10.
 */
public interface PlayDao {

    public long insertPlayInfo(List<PlayInfo> list);

    public long updatesDetailPlayInfo(List<PlayInfo> list);

    public long updatesPlayInfo(List<PlayInfo> list);

    public List<PlayInfo> getAllPlayInfo();

    public List<PlayInfo> getPlayInfoLimit(int type,int start,int num);

    public PlayInfo getPlayInfoById(int tableId,int type);

}
