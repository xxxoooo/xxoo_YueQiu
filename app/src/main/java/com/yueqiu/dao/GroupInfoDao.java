package com.yueqiu.dao;

import com.yueqiu.bean.GroupNoteInfo;

import java.security.acl.Group;

/**
 * Created by wangyun on 15/1/20.
 */
public interface GroupInfoDao {

    public long insertGroupInfo(GroupNoteInfo info);

    public long updateGroupInfo(GroupNoteInfo info);
    
}
