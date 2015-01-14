package com.yueqiu.dao;

import com.yueqiu.bean.FriendsApplication;

import java.util.List;

/**
 * Created by doushuqi on 15/1/14.
 */
public interface ApplicationDao {

    public void insertApplication(List<FriendsApplication> list);

    public long insertApplication(FriendsApplication application);

    public List<FriendsApplication> getApplication();

    public long updateFriendsApplication(String id, int value);

    public boolean queryApplicationById(String id);

    public boolean clearData();
}
