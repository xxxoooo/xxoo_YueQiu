package com.yueqiu.constant;


/**
 * Created by yinfeng on 14/12/22.
 */
public class HttpConstants
{

    /**
     * 请求方式
     */
    public static final class RequestMethod
    {
        public static final String GET = "GET";

        public static final String POST = "POST";
    }



    /**
     * 用户注册请求参数
     * 发送Post请求
     *
     * @author yinfeng
     */
    public static final class RegisterConstant
    {
        /*
         * 接口
         */
        public static final String URL = "/user/register";


        public static final String VERFICATION_CODE = "verfication_code";

        /*
         * 账号
         */
        public static final String ACCOUNT = "username";

        /*
         * 手机号
         */
        public static final String PHONE = "phone";

        /*
         * 性别 1男2女
         */
        public static final String SEX = "sex";

        /*
         * 密码
         */
        public static final String PASSWORD = "password";
    }

    /**
     * 登录请求参数
     * 发送Post请求
     *
     * @author yinfeng
     */
    public static final class LoginConstant
    {

        public static final String URL = "/user/login";
        /*
         * 手机号或账号
         */
        public static final String USERNAME = "username";

        /*
         * 密码
         */
        public static final String PASSWORD = "password";
    }

    /**
     * 登出
     */
    public static final class LogoutConstant
    {

        public static final String URL = "/user/logout";
    }

    public static final class PartIn{

        public static final String URL = "/center/getJoin";
    }

    /**
     * 我的收藏
     */
    public static final class Favor{

        public static final String URL = "/store/getStore";

        public static final String STORE_URL = "/store/storeSave";

        public static final String TYPE = "type";

        public static final String START_NO = "start_no";

        public static final String END_NO = "end_no";
    }

    /**
     * 我的发布
     */
    public static final class Published
    {

        public static final String URL = "/center/getPublishList";

        public static final String TYPE = "type";

        public static final String START_NO = "start_no";

        public static final String END_NO = "end_no";
    }

    /*
     * 我的资料请求参数
     * 发送POST请求
     */
    public static final class GetMyInfo
    {
        public static final String URL = "/center/getInfo";

        /*
         * 用户ID
         */
        public static final String USER_ID = "user_id";
    }

    /**
     * 球友的请求参数
     * 发送Get请求
     */
    public static final class NearbyMate
    {
        public static final String URL = "/home/friend";
    }

    /**
     * 约球的请求参数
     * 发送GET请求
     */
    public static final class NearbyDating
    {
        public static final String URL = "/home/appointBall";

        // 用于实现约球详情页面的用户点击我要参加活动的url
        public static final String URL_JOIN_ACTIVITY = "/home/join";

        // 用于获取约球详情的信息列表的URL
        public static final String URL_DATING_DETAILE = "/home/appointBllDetail";

        public static final String USER_ID = "user_id";

        public static final String TYPE_ID = "type_id";

        public static final String P_ID = "p_id";
    }

    /**
     * 助教的请求参数
     *
     */
    public static final class NearbyAssistCoauch
    {
       public static final String URL = "/home/tutor";
    }

    /**
     * 用于请求教练的相关信息的Http url常量
     */
    public static final class NearbyCoauch
    {
        public static final String URL = "/home/coach";

    }

    /**
     * 用于请求球厅的相关信息的列表
     * 这里我们需要注意的是，当我们请求的是球厅Fragment当中的listView当中的信息时，我们使用的是
     * 大众点评的接口来获得大众点评推荐的所有的球厅的列表的信息
     * 也就是说这里我们SearchRoom使用的是大众点评的接口，而不是服务器端的同志开发的接口
     */
    public static final class NearbyRoom
    {
        public static final String URL = "";
    }

    /**
     * 这是用于请求商家推荐的球厅Url地址
     * 我们通过这个接口获得的信息主要是用于显示球厅Fragment上面显示的Image Gallery列表
     *
     */
    public static final class NearbyRoomRecommendation
    {
        public static final String URL = "/home/GetBallHall";
    }


    /*
     * 球厅、约球详情、发布约球
     * 发送POST请求
     */
    public static final class AppointBll
    {
        public static final String URL = "center/publish";

        public static final String USER_ID = "user_id";

        public static final String TITLE = "title";

        public static final String ADDRESS = "address";

        public static final String BEGIN_TIME = "begin_time";

        public static final String END_TIME = "end_time";

        public static final String MODULE = "module";

        public static final String CONTENT = "content";

        public static final String CONTACT = "contact";

        public static final String PHONE  = "phone";

    }

    /*
     * 我的收藏(包含：约球，活动，台球圈)
     */
    public static final class GetStore
    {
        public static final String URL = "/user/getStore";

        /*
         * 用户ID
         */
        public static final String USER_ID = "user_id";

        /*
         * 发布类型
         * 1、约球
         * 2、活动
         * 3、台球圈
         */
        public static final String TYPE_ID = "type_id";

        /*
         * 搜索关键字
         */
        public static final String KEYWORDS = "keywords";

        /*
         * 开始条数（默认为0）
         */
        public static final String START_NO = "start_no";

        /*
         * 结束条数
         */
        public static final String END_NO = "end_no";
    }

    /*
     * 聊吧中添加好友页面通过附近的人获取好友列表
     */
    public static final class SearchPeopleByNearby
    {
        public static final String URL = "/friend/nearby";

        //请求参数：位置坐标
        public static final String USER_ID = "user_id";
        public static final String LAT = "lat";
        public static final String LNG = "lng";
    }

    /*
     * 聊吧中添加好友页面通过查询手机号或账号获取好友
     */
    public static final class SearchPeopleByKeyword
    {
        public static final String URL = "/friend/word";

        //请求参数：位置坐标
        public static final String USER_ID = "user_id";
        public static final String KEYWORDS = "keywords";
    }

    /*
     * 聊吧中获取好友列表s
     */
    public static final class ContactsList
    {
        public static final String URL = "/friend/getList";

        //请求参数：位置坐标
        public static final String USER_ID = "user_id";
        public static final String GROUP_ID = "group_id";
    }

    /**
     * 聊吧中添加好友身份验证下一步
     * post
     */
    public static final class FriendManage
    {
        public static final String URL = "/friend/manage";

        public static final String MY_ID = "my_id";
        public static final String ASK_ID = "ask_id";
        public static final String GROUP_ID = "group_id";
        public static final String REMARK = "remark";
        public static final String TAG = "tag";
    }

    /**
     * 聊吧中添加好友身份验证
     * post
     */
    public static final class FriendSendAsk
    {
        public static final String URL = "/friend/sendAsk";

        public static final String MY_ID = "my_id";
        public static final String ASK_ID = "ask_id";
        public static final String NEWS = "news";
    }

    /**
     * 聊吧中处理好友身份验证
     * post
     */
    public static final class AskManage
    {
        public static final String URL = "/friend/askManage";

        public static final String ASK_ID = "id";
        public static final String GROUP_ID = "group_id";
        public static final String REMARK = "remark";
        public static final String TAG = "tag";
    }

    /**
     * 聊吧中好友申请
     * post
     */
    public static final class GetAsk
    {
        public static final String URL = "/friend/getAsk";

        public static final String USER_ID = "user_id";
    }

    /**
     * 活动接口
     */
    public static class Play
    {
        public static final String PUBLISH = "/play/publish";

        public static final String GETLISTEE = "/play/getList";

        public static final String GETDETAIL = "/play/deatil";

        public static final String TYPE = "type";

        public static final String ID = "id";

        public static final String START_NO = "start_no";

        public static final String END_NO = "end_no";

        public static final String USER_ID = "user_id";
    }

    /**
     * 发表话题
     */
    public static class GroupIssue{

        public static final String URL      = "/cricle/publish";

        public static final String TYPE     = "type";

        public static final String USER_ID  = "user_id";

        public static final String TITLE    = "title";

        public static final String CONTENT  = "content";
    }

    /**
     * 台球圈List
     */
    public static class GroupList{

        public static final String URL      = "/cricle/getList";

        public static final String TYPE     = "type";

        public static final String STAR_NO  = "start_no";

        public static final String END_NO   = "end_no";


    }

    public static class StroeSave{
        public static final String URL      = "/store/storeSave";
    }


    /**
     * 台球圈详情
     */
    public static class GroupDetail{

        public static final String URL = "/cricle/getDetail";

        public static final String ID  = "id";
    }

    /**
     * Json 状态码
     *
     * @author yinfeng
     */
    public static final class ResponseCode
    {
        /*
         *  正常
         */
        public static final int NORMAL = 1001;

        /*
         * 缺少参数
         */
        public static final int a1 = 1002;

        /*
         * 数据格式错误
         */
        public static final int a2 = 1003;

        /*
         * 无权限
         */
        public static final int a3 = 1004;

        /*
         * 找不到记录
         */
        public static final int NO_RESULT = 1005;

        /*
         * 服务器内部错误
         */
        public static final int a5 = 1006;

        /*
         * Token错误
         */
        public static final int a6 = 1007;

        /*
         * 请求接口错误
         */
        public static final int a7 = 1008;

        /*
         * 数据库错误
         */
        public static final int a8 = 1009;
        /*
        * 向网络请求时，发生错误，JSON转化发生异常
        */
        public static final int REQUEST_ERROR = 1010;
        /*
         * 请求超时
         */
        public static final int TIME_OUT = 1011;
    }

    public static class FeedBack{
        public static final String URL = "/center/suggest";

        public static final String USER_ID = "user_id";

        public static final String TITLE = "title";

        public static final String CONTENT = "content";
    }

    public static class Captcha{

        public static final String URL = "/user/sendCode";

        public static final String PHONE = "phone";

        public static final String ACTION_TYPE = "action_type";
    }

    public static class ChangePhoto{

        //TODO:服务端接口还没定
        public static final String URL = "/user/ImgUp";

        //TODO:服务端要穿的参数也没定,要根据服务器那边确定参数名称
        public static final String IMG_DATA = "img_data";

        //TODO:应该有user_id
        public static final String USER_ID = "user_id";

        public static final String IMG_SUFFIX = "img_suffix";


    }


    // 这是SearchActivity当中的球厅Fragment当中的请求数据的过程
    // 因为服务器端使用的是大众点评的SDK，所以我们的请求地址是以大众点评为基础的
    public static final String DP_BASE_URL = "http://api.dianping.com/v1";
    public static final String DP_RELATIVE_URL = "/business/find_businesses";
    public static final String DP_APP_KEY = "0786070696";
    public static final String DP_APP_SECRET = "f3e6c9dbe811446884f9a5010b8729f4";


}
