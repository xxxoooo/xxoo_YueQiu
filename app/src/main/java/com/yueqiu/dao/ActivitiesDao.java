package com.yueqiu.dao;

import com.yueqiu.bean.Activities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinfeng on 15/1/10.
 */
public interface ActivitiesDao {

    public boolean insertActiviesList(ArrayList<Activities> list);

    public boolean insertActivities(Activities activities);

    public boolean updateActivities(Activities activities);

    public ArrayList<Activities> getActivities(int start, int end);

    public Activities getActivities(int id);


    public String getRefreshTime();

    public boolean UpdateRefreshTime(String time);

    public boolean addRefreshTime(String time);
}
