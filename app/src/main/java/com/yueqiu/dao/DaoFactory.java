package com.yueqiu.dao;

import android.content.Context;

import com.yueqiu.dao.daoimpl.ActivitiesDaoImpl;
import com.yueqiu.dao.daoimpl.MyCollectionDaoImpl;
import com.yueqiu.dao.daoimpl.PublishedImpl;
import com.yueqiu.dao.daoimpl.UserDaoImpl;

/**
 * Created by yinfeng on 15/1/10.
 */
public class DaoFactory {

    public static ActivitiesDao getActivities(Context context)
    {
        return new ActivitiesDaoImpl(context);
    }

    public static UserDao getUser(Context context){

        return new UserDaoImpl(context);
    }

    public static PublishedDao getPublished(Context context){

        return new PublishedImpl(context);
    }

    public static MyCollectionDao getMyCollection(Context context){

        return new MyCollectionDaoImpl(context);
    }

}
