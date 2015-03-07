package com.yueqiu.dao;

import android.content.Context;

import com.yueqiu.dao.daoimpl.ApplicationDaoImpl;
import com.yueqiu.dao.daoimpl.ContactsDaoImpl;
import com.yueqiu.dao.daoimpl.FavorDaoImpl;
import com.yueqiu.dao.daoimpl.GroupInfoDaoImpl;
import com.yueqiu.dao.daoimpl.NearbyAssistCoauchDaoImpl;
import com.yueqiu.dao.daoimpl.NearbyCoauchDaoImpl;
import com.yueqiu.dao.daoimpl.NearbyDatingDaoImpl;
import com.yueqiu.dao.daoimpl.NearbyMateDaoImpl;
import com.yueqiu.dao.daoimpl.NearbyRoomDaoImpl;
import com.yueqiu.dao.daoimpl.PlayDaoImpl;
import com.yueqiu.dao.daoimpl.PublishedImpl;
import com.yueqiu.dao.daoimpl.UserDaoImpl;

/**
 * Created by yinfeng on 15/1/10.
 */
public class DaoFactory {

    public static PlayDao getPlay(Context context) {
        return new PlayDaoImpl(context);
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

    public static NearbyMateDao getSearchMateDao(Context context)
    {
        return new NearbyMateDaoImpl(context);
    }

    public static NearbyAssistCoauchDao getSearchASCoauchDao(Context context)
    {
        return new NearbyAssistCoauchDaoImpl(context);
    }

    public static NearbyCoauchDao getSearchCoauchDao(Context context)
    {
        return new NearbyCoauchDaoImpl(context);
    }

    public static NearbyDatingDao getSearchDatingDao(Context context)
    {
        return new NearbyDatingDaoImpl(context);
    }

    public static NearbyRoomDao getSearchRoomDao(Context context)
    {
        return new NearbyRoomDaoImpl(context);
    }

}
