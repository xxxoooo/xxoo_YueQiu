package com.yueqiu.dao;

import com.yueqiu.bean.GroupNoteInfo;

import java.security.acl.Group;
import java.util.List;

/**
 * Created by wangyun on 15/1/20.
 */
public interface GroupInfoDao {

    public long insertGroupInfo(List<GroupNoteInfo> infos);

    public long updateGroupInfo(List<GroupNoteInfo> info);

    public List<GroupNoteInfo> getAllGroupInfoLimit(int start,int num);

    public List<GroupNoteInfo> getGroupInfoByType(int type,int start,int num);

    public List<GroupNoteInfo> getAllGroupInfo();

}
