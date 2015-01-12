package com.yueqiu.db.search;

import android.provider.BaseColumns;

import java.io.StringReader;

/**
 * Created by scguo on 15/1/10.
 *
 * 用于整个SearchActivity当中所有的Provider的BaseColumns
 */
public interface SearchBaseColumns
{
    public static final String AUTHORITY_PREFIX = "com.yueqiu.db.search.";

    public static final String SEARCH_DB_NAME = "search_database";
    public static final int SEARCH_DB_VERSION = 1;

    // 用于球友Provider当中的BaseColumns
    public static interface MateColumns extends BaseColumns
    {
        public static final String USER_ID = "user_id";
        public static final String NAME = "name";
        public static final String SEX = "sex";
        public static final String DISTRICT = "district"; // 球友的地区
        public static final String RANGE = "range";
    }

    // 用于约球Provider当中的BaseColumns
    public static interface DatingColumnds extends BaseColumns
    {
        public static final String USER_ID = "user_id";
        public static final String NAME = "username";
        public static final String TITLE = "title"; // 当前所发布的约球的主题内容(例如"大奖赛开幕了，一起参加"的形式的字符串)
        public static final String RANGE = "range";
    }

    // 用于助教Provider当中的BaseColumns
    public static interface AssistCoauchColumns extends BaseColumns
    {
        public static final String USER_ID = "user_id";
        public static final String NAME = "username";
        public static final String CLASS = "class"; // 球种
        public static final String MONEY = "money"; // 助教的费用
        public static final String RANGE = "range";
        public static final String SEX = "sex";
    }

    // 用于教练Provider当中的BaseColumns
    public static interface CoauchColumns extends BaseColumns
    {
        public static final String USER_ID = "user_id";
        public static final String NAME = "username";
        public static final String CLASS = "class"; // 球种(例如九球，斯诺克等)
        public static final String LEVEL = "level"; // 教练的资质(例如国家队还是北京队)
        public static final String RANGE = "range";
        public static final String SEX = "sex";
    }

    // TODO: 由于球厅的数据是来自于大众点评的，所以我们要进行进一步确认，究竟需要哪些字段
    // 用于球厅Provider当中的BaseColumns
    public static interface RoomColumns extends BaseColumns
    {
        public static final String ROOM_ID = "room_id";
        public static final String NAME = "room_name";
    }


}
