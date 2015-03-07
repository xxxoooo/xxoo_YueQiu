package com.yueqiu.bean;


import org.json.JSONException;
import org.json.JSONObject;


public class UserInfo {
    private int id;
    private String img_url;//头像在网络中的地址
    private String img_real;//头像在本地的地址
    private String username;//账号
    private String phone;//电话
    private String password;//密码
    private String sex;//性别
    private String title;//职称
    private String nick;//昵称
    private String district;//区域
    private String level;//水平
    private String appoint_date;//约球时间
    private String ballArm;//使用球杆
    private String usedType;//使用习惯
    private String ballAge;//球龄
    private String idol;//偶像
    private String idol_name;//签名
    private String new_img;//最新照片在网络中的位置
    private String new_img_real;//最新照片在本地的位置
    private String ball_class;


    private String ball_type;
    private String token;
    private String user_id;
    private String login_time;//登录时间

    private String cost;
    private String my_type;
    private String work_live;

    private String comment_time;
    private String content;

    private static final String JSON_USER_ID = "user_id";
    private static final String JSON_IMGREAL = "img_url";
    private static final String JSON_ACCOUNT = "username";//账户
    private static final String JSON_SEX = "sex";
    private static final String JSON_USERNAME = "nick";//昵称
    private static final String JSON_DISTRICT = "district";
    private static final String JSON_LEVEL = "level";
    private static final String JSON_BALL_ARM = "ballArm";
    private static final String JSON_USED_TYPE = "usedType";
    private static final String JSON_BALL_AGE = "ballAge";
    private static final String JSON_IDOL = "idol";
    private static final String JSON_SIGN = "idol_name";
    private static final String JSON_NEW_IMG = "new_img";
    private static final String JSON_CLASS = "class";
    private static final String JSON_APPOINT_DATE = "appoint_date";
    //TODO
    private static final String JSON_COST = "moneys";
    private static final String JSON_WORK_LIVE = "work_live";
    private static final String JSON_MY_TYPE = "mytype";

    public void setBall_type(int ball_type) {
        this.ball_type = String.valueOf(ball_type);
    }

    public int getBall_type() {
        return Integer.parseInt(ball_type);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {

        return username;
    }
    public void setNick(String nick) {
        this.nick = nick;
    }


    public String getNick() {

        return nick;
    }


    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getImg_real() {
        return img_real;
    }

    public void setImg_real(String img_real) {
        this.img_real = img_real;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getSex() {
        return Integer.parseInt(sex);
    }

    public void setSex(int sex) {
        this.sex = String.valueOf(sex);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public int getLevel() {
        return Integer.parseInt(level);
    }

    public void setLevel(int level) {
        this.level = String.valueOf(level);
    }

    public String getAppoint_date() {
        return appoint_date;
    }

    public void setAppoint_date(String appoint_date) {
        this.appoint_date = appoint_date;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUser_id() {
        return Integer.parseInt(user_id);
    }

    public void setUser_id(int user_id) {
        this.user_id = String.valueOf(user_id);
    }

    public String getLogin_time() {
        return login_time;
    }

    public void setLogin_time(String login_time) {
        this.login_time = login_time;
    }


    public int getBallArm() {
        return Integer.parseInt(ballArm);
    }

    public void setBallArm(int ballArm) {
        this.ballArm = String.valueOf(ballArm);
    }

    public int getUsedType() {
        return Integer.parseInt(usedType);
    }

    public void setUsedType(int usedType) {
        this.usedType = String.valueOf(usedType);
    }

    public String getBallAge() {
        return ballAge;
    }

    public void setBallAge(String ballAge) {
        this.ballAge = ballAge;
    }

    public String getIdol() {
        return idol;
    }

    public void setIdol(String idol) {
        this.idol = idol;
    }

    public String getIdol_name() {
        return idol_name;
    }

    public void setIdol_name(String idol_name) {
        this.idol_name = idol_name;
    }

    public String getNew_img() {
        return new_img;
    }

    public void setNew_img(String new_img) {
        this.new_img = new_img;
    }

    public String getNew_img_real() {
        return new_img_real;
    }

    public void setNew_img_real(String new_img_real) {
        this.new_img_real = new_img_real;
    }

    public String getBall_class() {
        return ball_class;
    }

    public void setBall_class(String ball_class) {
        this.ball_class = ball_class;
    }


    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public int getMy_type() {
        return Integer.valueOf(my_type);
    }

    public void setMy_type(int my_type) {
        this.my_type = String.valueOf(my_type);
    }

    public String getWork_live() {
        return work_live;
    }

    public void setWork_live(String work_live) {
        this.work_live = work_live;
    }

    public String getComment_time() {
        return comment_time;
    }

    public void setComment_time(String comment_time) {
        this.comment_time = comment_time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_USER_ID, user_id);
        json.put(JSON_IMGREAL, img_url);
        json.put(JSON_ACCOUNT, username);
        json.put(JSON_SEX, String.valueOf(sex));
        json.put(JSON_USERNAME, nick);
        json.put(JSON_DISTRICT, district);
        json.put(JSON_LEVEL, level);
        json.put(JSON_BALL_ARM, ballArm);
        json.put(JSON_USED_TYPE, usedType);
        json.put(JSON_BALL_AGE, String.valueOf(ballAge));
        json.put(JSON_IDOL, idol);
        json.put(JSON_SIGN, idol_name);
        json.put(JSON_NEW_IMG, new_img);
        json.put(JSON_CLASS, ball_class);
        json.put(JSON_APPOINT_DATE, appoint_date);
        return json;
    }
}
