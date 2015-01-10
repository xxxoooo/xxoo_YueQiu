package com.yueqiu.constant;

public class DatabaseConstant {

    public static final String DATABASENAME = "yueqiu";

    public static final int VERSION = 1;


    public final class UserTable
    {
        public static final String TABLE        = "userinfo";

        public static final String _ID          = "id";

        public static final String IMG_URL      = "img_url";

        public static final String IMG_REAL     = "img_real";

        public static final String USERNAME      = "username";

        public static final String PHONE        = "phone";

        public static final String PASSWORD     = "password";

        public static final String SEX          = "sex";

        public static final String TITLE        = "title";

        public static final String NICK         = "nick";

        public static final String DISTRICT     = "district";

        public static final String LEVEL        = "level";

        public static final String BALL_TYPE    = "ball_type";

        public static final String APPOINT_DATE = "appoint_date";

        public static final String BALLARM      = "ballArm";

        public static final String USERDTYPE    = "usedType";

        public static final String BALLAGE      = "ballAge";

        public static final String IDOL         = "idol";

        public static final String IDOL_NAME    = "idol_name";

        public static final String NEW_IMG      = "new_img";

        public static final String NEW_IMG_REAL = "new_img_real";

        public static final String LOGIN_TIME   = "login_time";

        public static final String USER_ID      = "user_id";

        public static final String CREATE_SQL   =  "CREATE TABLE IF NOT EXISTS " + TABLE + "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USER_ID + " INTEGER NOT NULL, " + USERNAME + " VARCHAR(255) NOT NULL, " + PHONE + " VARCHAR(50) NOT NULL, " + PASSWORD + " VARCHAR(100), " +
                SEX + " INTEGER DEFAULT 1, " + TITLE + " VARCHAR(50), " + IMG_URL + " VARCHAR(255), " + IMG_REAL + " VARCHAR(255), " + NICK +
                " VARCHAR(255), " + DISTRICT + " VARCHAR(255), " + LEVEL + " INTEGER DEFAULT 1, " + BALL_TYPE + " INTEGER DEFAULT 1, " + APPOINT_DATE +
                " VARCHAR(255), " + BALLARM  + " INTEGER DEFAULT 1, " + USERDTYPE + " INTEGER DEFAULT 1, " + BALLAGE + " INTEGER, " + IDOL + " VARCHAR(255), "
                + IDOL_NAME + " VARCHAR(255), " + NEW_IMG + " VARCHAR(255), " + NEW_IMG_REAL + " VARCHAR(255), " + LOGIN_TIME + " VARCHAR(255)" + ")";


    }

    public final class PublishInfoTable{

        public static final String TABLE        = "published_info";

        public static final String _ID          = "id";

        public static final String USER_ID      = "user_id";

        public static final String TYPE         = "type";

        public static final String START_NO     = "start_no";

        public static final String END_NO       = "end_no";

        public static final String COUNT        = "count";

        public static final String CRAETE_SQL   = "CREATE TABLE IF NOT EXISTS " + TABLE + "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USER_ID + " INTEGER NOT NULL, " + TYPE + " INTEGER NOT NULL DEFAULT 1, " + START_NO + " INTEGER DEFAULT 0, " +
                END_NO + " INTEGER DEFAULT 9, " + COUNT + " INTEGER" + ")";


    }

    public final class PublishInfoItemTable{

        public static final String TABLE        = "published_item_info";

        public static final String _ID          = "id";

        public static final String USER_ID      = "user_id";

        public static final String TABLE_ID     = "table_id";

        public static final String TYPE         = "type";

        public static final String IMAGE_URL    = "image_url";

        public static final String TITLE        = "title";

        public static final String CONTENT      = "content";

        public static final String DATETIME     = "datetime";

        public static final String CREATE_URL   = "CREATE TABLE IF NOT EXISTS " + TABLE + "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USER_ID + " INTEGER NOT NULL, " + TABLE_ID + " INTEGER, " + TYPE + " INTEGER DEFAULT 1, " + IMAGE_URL + " VARCHAR(255), "
                + TITLE + " VARCHAR(255), " + CONTENT + " VARCHAR(500), " + DATETIME + " VARCHAR(50)" + ")";


    }

}
