package com.yueqiu.dao;

import com.yueqiu.bean.ContactsList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by doushuqi on 15/1/13.
 */
public interface ContactsDao {
    public long insertContact(HashMap<Integer, List<ContactsList.Contacts>> map);

    public long insertContact(Map<String, String> map);

    public boolean queryUserId(Map<String, String> map);

    public boolean queryGroupId(Map<String, String> map);

    public long updateContact(Map<String, String> map);

    public ContactsList.Contacts getContact(String userId);

    public HashMap<Integer, List<ContactsList.Contacts>> getContactList();
}
