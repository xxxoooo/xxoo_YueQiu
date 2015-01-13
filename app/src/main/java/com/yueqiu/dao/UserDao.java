package com.yueqiu.dao;

import com.yueqiu.bean.UserInfo;

import java.util.Map;

/**
 * Created by wangyun on 15/1/12.
 */
public interface UserDao {

    public long insertUserInfo(Map<String,String> map);

    public boolean queryUserId(Map<String,String> map);

    public long updateUserInfo(Map<String,String> map);

    public UserInfo getUserByUserId(String userId);
}
