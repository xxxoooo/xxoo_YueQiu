package com.yueqiu.dao;

import android.content.Context;

import com.yueqiu.dao.daoimpl.ActivitiesDaoImpl;
import com.yueqiu.dao.daoimpl.ApplicationDaoImpl;
import com.yueqiu.dao.daoimpl.ContactsDaoImpl;
import com.yueqiu.dao.daoimpl.FavorDaoImpl;
import com.yueqiu.dao.daoimpl.GroupInfoDaoImpl;
import com.yueqiu.dao.daoimpl.PublishedImpl;
import com.yueqiu.dao.daoimpl.SearchAssistCoauchDaoImpl;
import com.yueqiu.dao.daoimpl.SearchCoauchDaoImpl;
import com.yueqiu.dao.daoimpl.SearchDatingDaoImpl;
import com.yueqiu.dao.daoimpl.SearchMateDaoImpl;
import com.yueqiu.dao.daoimpl.SearchRoomDaoImpl;
import com.yueqiu.dao.daoimpl.UserDaoImpl;

/**
 * Created by yinfeng on 15/1/10.
 */
public class DaoFactory {

    public static ActivitiesDao getActivities(Context context) {
        return new ActivitiesDaoImpl(context);
    }

    public static UserDao getUser(Context context) {

        return new UserDaoImpl(context);
    }

    public static PublishedDao getPublished(Context context) {

        return new PublishedImpl(context);
    }

    public static ContactsDao getContacts(Context context) {

        return new ContactsDaoImpl(context);
    }

    public static ApplicationDao getApplication(Context context) {
        return new ApplicationDaoImpl(context);
    }

    public static FavorDao getFavor(Context context) {

        return new FavorDaoImpl(context);
    }

    public static GroupInfoDao getGroupDao(Context context){

        return new GroupInfoDaoImpl(context);
    }

    public static SearchMateDao getSearchMateDao(Context context)
    {
        return new SearchMateDaoImpl(context);
    }

    public static SearchAssistCoauchDao getSearchASCoauchDao(Context context)
    {
        return new SearchAssistCoauchDaoImpl(context);
    }

    public static SearchCoauchDao getSearchCoauchDao(Context context)
    {
        return new SearchCoauchDaoImpl(context);
    }

    public static SearchDatingDao getSearchDatingDao(Context context)
    {
        return new SearchDatingDaoImpl(context);
    }

    public static SearchRoomDao getSearchRoomDao(Context context)
    {
        return new SearchRoomDaoImpl(context);
    }

}
