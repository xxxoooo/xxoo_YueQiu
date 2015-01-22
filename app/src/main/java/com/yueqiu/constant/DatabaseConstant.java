package com.yueqiu.constant;

public class DatabaseConstant
{

    public static final String DATABASENAME = "yueqiu";

    public static final int VERSION = 1;


    public final class UserTable
    {
        public static final String TABLE = "userinfo";

        public static final String _ID = "id";

        public static final String IMG_URL = "img_url";

        public static final String IMG_REAL = "img_real";

        public static final String USERNAME = "username";

        public static final String PHONE = "phone";

        public static final String PASSWORD = "password";

        public static final String SEX = "sex";

        public static final String TITLE = "title";


        public static final String NICK = "nick";

        public static final String DISTRICT = "district";

        public static final String LEVEL = "level";

        public static final String BALL_CLASS = "ball_class";

        public static final String BALL_TYPE = "ball_type";

        public static final String APPOINT_DATE = "appoint_date";

        public static final String BALLARM = "ballArm";

        public static final String USERDTYPE = "usedType";

        public static final String BALLAGE = "ballAge";

        public static final String IDOL = "idol";

        public static final String IDOL_NAME = "idol_name";

        public static final String NEW_IMG = "new_img";

        public static final String NEW_IMG_REAL = "new_img_real";

        public static final String LOGIN_TIME = "login_time";

        public static final String USER_ID = "user_id";


        public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USER_ID + " INTEGER NOT NULL, " + USERNAME + " VARCHAR(255) NOT NULL, " + PHONE + " VARCHAR(50) NOT NULL, " + PASSWORD + " VARCHAR(100), " +
                SEX + " INTEGER DEFAULT 1, " + TITLE + " VARCHAR(50), " + IMG_URL + " VARCHAR(255), " + IMG_REAL + " VARCHAR(255), " + NICK +
                " VARCHAR(255), " + DISTRICT + " VARCHAR(255), " + LEVEL + " INTEGER DEFAULT 1, " + BALL_TYPE + " INTEGER DEFAULT 1, " + APPOINT_DATE +
                " VARCHAR(255), " + BALLARM + " INTEGER DEFAULT 1, " + USERDTYPE + " INTEGER DEFAULT 1, " + BALLAGE + " INTEGER, " + IDOL + " VARCHAR(255), "
                + IDOL_NAME + " VARCHAR(255), " + NEW_IMG + " VARCHAR(255), " + NEW_IMG_REAL + " VARCHAR(255), " + LOGIN_TIME + " VARCHAR(255)" + ")";

        public static final String DROP_SQL = "DROP TABLE IF EXISTS " + TABLE;

    }

    public final class PublishInfoTable
    {

        public static final String TABLE = "published_info";

        public static final String _ID = "id";

        public static final String USER_ID = "user_id";

        public static final String TYPE = "type";

        public static final String START_NO = "start_no";

        public static final String END_NO = "end_no";

        public static final String COUNT = "count";

        public static final String CRAETE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USER_ID + " INTEGER NOT NULL, " + TYPE + " INTEGER NOT NULL DEFAULT 1, " + START_NO + " INTEGER DEFAULT 0, " +
                END_NO + " INTEGER DEFAULT 9, " + COUNT + " INTEGER" + ")";
        public static final String DROP_SQL = "DROP TABLE IF EXISTS " + TABLE;


    }

    public final class PublishInfoItemTable
    {

        public static final String TABLE = "published_item_info";

        public static final String _ID = "id";

        public static final String USER_ID = "user_id";

        public static final String TABLE_ID = "table_id";

        public static final String TYPE = "type";

        public static final String IMAGE_URL = "image_url";

        public static final String TITLE = "title";

        public static final String CONTENT = "content";

        public static final String DATETIME = "datetime";

        public static final String CREATE_URL = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USER_ID + " INTEGER NOT NULL, " + TABLE_ID + " INTEGER, " + TYPE + " INTEGER DEFAULT 1, "
                + TITLE + " VARCHAR(255), " + CONTENT + " VARCHAR(500), " + DATETIME + " VARCHAR(50)" + ")";
        public static final String DROP_SQL = "DROP TABLE IF EXISTS " + TABLE;


    }

    public final class ChatMessageTable
    {
        public static final String TABLE = "chat_msg_entity";

        public static final String _ID = "id";

        public static final String USER_ID = "user_id";

        public static final String IMG_URL = "image_url";

        public static final String USERNAME = "username";

        public static final String MESSAGE_CONTENT = "message_content";

        public static final String DATETIME = "datetime";

        public static final String IS_COME = "isCome";

        public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USER_ID + " INTEGER NOT NULL, " + USERNAME + " VARCHAR(255) NOT NULL, " + IMG_URL + " VARCHAR(255), " + MESSAGE_CONTENT + " VARCHAR(255), "
                + DATETIME + " VARCHAR(255), " + IS_COME + " INTEGER DEFAULT 0" + ")";
        public static final String DROP_SQL = "DROP TABLE IF EXISTS " + TABLE;
    }

    public final class FriendsTable {
        public static final String TABLE        = "friends";

        public static final String _ID          = "id";

        public static final String USER_ID      = "user_id";

        public static final String GROUP_ID      = "group_id";

        public static final String IMG_URL    = "img_url";

        public static final String USERNAME     = "username";

        public static final String LAST_MESSAGE = "content";

        public static final String DATETIME     = "create_time";

        public static final String CREATE_SQL   =  "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USER_ID + " INTEGER NOT NULL, " + USERNAME + " VARCHAR(255) NOT NULL, " + IMG_URL + " VARCHAR(255), " + LAST_MESSAGE + " VARCHAR(255), "
                + DATETIME + " VARCHAR(255), " + GROUP_ID + " INTEGER NOT NULL" + ")";
        public static final String DROP_SQL   = "DROP TABLE IF EXISTS " + TABLE;
    }

    public final class FriendsApplication {
        public static final String TABLE        = "application";//好友申请表

        public static final String _ID          = "id";

        public static final String APPLICATION_ID = "application_id";

        public static final String USERNAME     = "username";

        public static final String NICK         = "nick";

        public static final String CREATE_TIME  = "create_time";

        public static final String IMG_URL      = "img_url";

        public static final String IS_AGREE     = "is_agree";

        public static final String CREATE_SQL   = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + APPLICATION_ID + " INTEGER NOT NULL, " + USERNAME + " VARCHAR(255) NOT NULL, " + IMG_URL + " VARCHAR(255), " + NICK + " VARCHAR(255), "
                + CREATE_TIME + " VARCHAR(255), " + IS_AGREE + " INTEGER NOT NULL" + ")";

        public static final String DROP_SQL     = "DROP TABLE IF EXISTS " + TABLE;
    }

    public final class ActivitiesTable {
        public static final String TABLENAME = "activities";

        public static final String _ID = "id";

        public static final String USERNAME = "username";

        public static final String SEX = "sex";

        public static final String IMG_URL = "img_url";

        public static final String LOOK_NUM = "look_num";
        /**
         * 1群活动2球星汇3台球展4赛事5其它
         */
        public static final String TYPE = "type";

        public static final String TITLE = "title";

        public static final String ADDRESS = "address";

        public static final String BEGIN_TIME = "begin_time";

        public static final String END_TIME = "end_time";

        public static final String MODEL = "model";

        public static final String CONTENT = "content";

        public static final String CREATE_TIME = "create_time";

        public static final String SQL = "CREATE TABLE IF NOT EXISTS " + TABLENAME + " ( " +
                _ID + " INTEGERR PRIMARY KEY UNIQUE, " +
                USERNAME + " VARCHAR(50) ," + SEX + " VARCHAR(2)," + IMG_URL + " VARCHAR(100), " + LOOK_NUM + " INTEGER," +
                TYPE + " VARCHAR(2), " + TITLE + " VARCHAR(50), " + ADDRESS + " VARCHAR(100), " +
                BEGIN_TIME + " VARCHAR(20), " + END_TIME + " VARCHAR(20)," +
                MODEL + " VARCHAR(2), " + CONTENT + " VARCHAR(500) ," + CREATE_TIME + " VARCHAR(20));";
        public static final String DROP_SQL = "DROP TABLE IF EXISTS " + TABLENAME;
    }

    public final class RefreshTime
    {
        public static final String REFRESH_TIME_TABLE = "refreshtime";

        public static final String _ID = "id";

        public static final String TABLE_NAME = "table_name";

        public static final String REFRESH_TIME = "refresh_time";

        public static final String SQL = "CREATE TABLE IF NOT EXISTS " + REFRESH_TIME_TABLE + " ( " +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TABLE_NAME + " VARCHAR(20), " + REFRESH_TIME
                + " VARCHAR(50) );";
        public static final String DROP_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public final class FavorInfoTable
    {

        public static final String TABLE = "my_store_info";

        public static final String _ID = "id";

        public static final String USER_ID = "user_id";

        public static final String TYPE = "type";

        public static final String START_NO = "start_no";

        public static final String END_NO = "end_no";

        public static final String COUNT = "count";


        public static final String CRAETE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USER_ID + " INTEGER NOT NULL, " + TYPE + " INTEGER NOT NULL DEFAULT 1, " + START_NO + " INTEGER DEFAULT 0, " +
                END_NO + " INTEGER DEFAULT 9, " + COUNT + " INTEGER" + ")";
        public static final String DROP_SQL = "DROP TABLE IF EXISTS " + TABLE;


    }

    public final class FavorInfoItemTable
    {

        // 以下是用于创建SearchActivity当中的5张表的SQL语句
        // 1. 球友Fragment的table
        public final class SearchMateTable
        {
            public static final String _ID = "_id";
            public static final String USER_ID = "user_id";
            public static final String NAME = "name";
            public static final String PHOTO_URL = "photo_url";
            public static final String SEX = "sex";
            public static final String DISTRICT = "district"; // 球友的地区
            public static final String RANGE = "range";

            public static final String MATE_TABLE = "mate_table";

            public static final String CREATE_SQL = " CREATE TABLE IF NOT EXISTS "
                    + MATE_TABLE + " ( "
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + USER_ID + " VARCHAR(20) NOT NULL, "
                    + NAME + " VARCHAR(50) NOT NULL, "
                    + PHOTO_URL + " VARCHAR(50) NOT NULL, "
                    + SEX + " VARCHAR(2) NOT NULL, "
                    + DISTRICT + " VARCHAR(100) NOT NULL, "
                    + RANGE + " VARCHAR(20) NOT NULL); ";

            public static final String DROP_SQL = "DTOP TABLE IF EXISTS " + MATE_TABLE;

        }

        // 2. 约球Fragment的table
        public final class SearchDatingTable
        {
            public static final String _ID = "_id";
            public static final String USER_ID = "user_id";
            public static final String NAME = "username";
            public static final String PHOTO_URL = "photo_url";
            public static final String TITLE = "title"; // 当前所发布的约球的主题内容(例如"大奖赛开幕了，一起参加"的形式的字符串)
            public static final String RANGE = "range";

            public static final String DATING_TABLE_NAME = "dating_table";

            public static final String CREATE_SQL = " CREATE TABLE IF NOT EXISTS "
                    + DATING_TABLE_NAME + " ( "
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + USER_ID + " VARCHAR(20) NOT NULL, "
                    + NAME + " VARCHAR(100) NOT NULL, "
                    + PHOTO_URL + " VARCHAR(50) NOT NULL, "
                    + TITLE + " VARCHAR(200) NOT NULL, "
                    + RANGE + " VARCHAR(20) NOT NULL); ";

            public static final String DROP_SQL = "DROP TABLE IF EXISTS " + DATING_TABLE_NAME;

        }

        // 3. 助教Fragment的table
        public final class SearchAssistCoauchTable
        {
            public static final String _ID = "_id";
            public static final String USER_ID = "user_id";
            public static final String NAME = "username";
            public static final String PHOTO_URL = "photo_url";
            public static final String CLASS = "class"; // 球种
            public static final String MONEY = "money"; // 助教的费用
            public static final String RANGE = "range";
            public static final String SEX = "sex";

            public static final String ASSISTCOAUCH_TABLE_NAME = "assist_coauch_table";

            public static final String CREATE_SQL = " CREATE TABLE IF NOT EXISTS "
                    + ASSISTCOAUCH_TABLE_NAME + " ( "
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + USER_ID + " VARCHAR(20) NOT NULL, "
                    + NAME + " VARCHAR(50) NOT NULL, "
                    + PHOTO_URL + " VARCHAR(50) NOT NULL, "
                    + SEX + " VARCHAR(2) NOT NULL, "
                    + CLASS + " VARCHAR(50) NOT NULL, "
                    + MONEY + " VARCHAR(20) NOT NULL, "
                    + RANGE + " VARCHAR(50) NOT NULL); ";

            public static final String DROP_SQL = "DROP TABLE IF EXISTS " + ASSISTCOAUCH_TABLE_NAME;
        }

        // 4. 教练Fragment的table
        public final class SearchCoauchTable
        {
            public static final String _ID = "_id";
            public static final String USER_ID = "user_id";
            public static final String NAME = "username";
            public static final String PHOTO_URL = "photo_url";
            public static final String CLASS = "class"; // 球种(例如九球，斯诺克等)
            public static final String LEVEL = "level"; // 教练的资质(例如国家队还是北京队)
            public static final String RANGE = "range";
            public static final String SEX = "sex";

            public static final String COAUCH_TABLE_NAME = "coauch_table";

            public static final String CREATE_SQL = " CREATE TABLE IF NOT EXISTS "
                    + COAUCH_TABLE_NAME + " ( "
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + USER_ID + " VARCHAR(20) NOT NULL , "
                    + NAME + " VARCHAR(50) NOT NULL , "
                    + PHOTO_URL + " VARCHAR(50) NOT NULL, "
                    + SEX + " VARCHAR(2) NOT NULL , "
                    + CLASS + " VARCHAR(50) NOT NULL , "
                    + LEVEL + " VARCHAR(50) NOT NULL, "
                    + RANGE + " VARCHAR(50) NOT NULL); ";

            public static final String DROP_SQL = "DROP TABLE IF EXISTS " + COAUCH_TABLE_NAME;

        }

        // 5. 球厅Fragment的table
        public final class SearchRoomTable
        {
            public static final String _ID = "_id";
            public static final String ROOM_ID = "room_id";
            public static final String NAME = "room_name";
            public static final String ROOM_URL = "room_url";
            public static final String DETAILED_ADDRESS = "detailed_address";
            public static final String ROOM_LEVEL = "room_level"; // 球厅的星级
            public static final String RANGE = "range";
            // 以下的新增的字段是为球厅详情Activity所存储的(这样可以减少我们进行Http请求的次数，我们仅需请求一次然后保存下来,毕竟本地的数据库获取比网络请求要快很多)
            public static final String PHONE_NUM = "phone_num";
            public static final String TAG = "tag";
            public static final String DETAILED_INFO = "room_info";


            public static final String ROOM_TABLE_NAME = "room_table";

            // TODO: 球厅Fragment当中的List暂时还没有确定最终的数据格式
            public static final String CREATE_SQL = " CREATE TABLE IF NOT EXISTS "
                    + ROOM_TABLE_NAME + " ( "
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                    + ROOM_ID + " VARCHAR(20) NOT NULL , "
                    + NAME + " VARCHAR(50) NOT NULL , "
                    + ROOM_URL + " VARCHAR(50) NOT NULL, "
                    + DETAILED_ADDRESS + " VARCHAR(250) NOT NULL, "
                    + ROOM_LEVEL + " VARCHAR(20) NOT NULL, "
                    + RANGE + " VARCHAR(50) NOT NULL, "
                    + PHONE_NUM + " VARCHAR(60) NOT NULL, "
                    + TAG + " VARCHAR(200) NOT NULL, "
                    + DETAILED_INFO + " VARCHAR(500) NOT NULL); ";

            public static final String DROP_SQL = "DROP TABLE IF EXISTS " + ROOM_TABLE_NAME;

        }


        public static final String TABLE = "store_item_info";

        public static final String _ID = "id";

        public static final String USER_ID = "user_id";

        public static final String TABLE_ID = "table_id";

        public static final String TYPE = "type";

        public static final String IMAGE_URL = "image_url";

        public static final String TITLE = "title";

        public static final String CONTENT = "content";

        public static final String DATETIME = "datetime";

        public static final String USER_NAME = "username";

        public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USER_ID + " INTEGER NOT NULL, " + TABLE_ID + " INTEGER, " + TYPE + " INTEGER DEFAULT 1, " + IMAGE_URL + " VARCHAR(255), "
                + TITLE + " VARCHAR(255), " + CONTENT + " VARCHAR(500), " + DATETIME + " VARCHAR(50), " + USER_NAME + " VARCHAR(255)" + ")";
        public static final String DROP_SQL = "DROP TABLE IF EXISTS " + TABLE;


    }

    public static class GroupInfo{

        public static final String TABLE = "group_info";

        public static final String _ID = "id";

        public static final String NOTE_ID = "note_id";

        public static final String USER_NAME = "user_name";

        public static final String SEX = "sex";

        public static final String BROWSE_COUNT = "browse_count";

        public static final String ISSUE_TIME = "issue_time";

        public static final String TITLE = "title";

        public static final String CONTENT = "content";

        public static final String COMMENT_COUNT = "comment_count";

        public static final String IMG_URL = "img_url";

        public static final String PRAISE_NUM = "praise_num";

        public static final String TYPE = "type";

        public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NOTE_ID + " INTEGER NOT NULL, "  + TYPE + " INTEGER, "+ USER_NAME + " VARCHAR(50), " + SEX + " INTEGER, " + BROWSE_COUNT + " INTEGER, " + ISSUE_TIME
                + " VARCHAR(50), " + TITLE + " VARCHAR(255), " + CONTENT + " TEXT, " + COMMENT_COUNT + " INTEGER, " + IMG_URL + " VARCHAR(255), "
                + PRAISE_NUM + " INTEGER" + ")";
        public static final String INSERT_SQL = "INSERT INTO " + TABLE + "(" + NOTE_ID + "," + TYPE + "," + USER_NAME + "," + SEX + "," + BROWSE_COUNT + "," + ISSUE_TIME
                + "," + TITLE + "," + CONTENT + "," + COMMENT_COUNT + "," + IMG_URL + "," + PRAISE_NUM + ") values(?,?,?,?,?,?,?,?,?,?,?)";
        public static final String DROP_SQL = "DROP TABLE IF EXISTS " + TABLE;

    }

}
