package com.yueqiu.dao;

import android.content.Context;

import com.yueqiu.dao.daoimpl.ActivitiesDaoImpl;

/**
 * Created by yinfeng on 15/1/10.
 */
public class DaoFactory {

    public static ActivitiesDao getActivities(Context context)
    {
        return new ActivitiesDaoImpl(context);
    }

}
