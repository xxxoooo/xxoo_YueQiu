package com.yueqiu.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 用户基本信息实体类
 * Created by yinfeng on 14/12/23.
 */
public class UserInfo {
    private int id;
    private String img_url = "";//头像在网络中的地址
    private String img_real;//头像在本地的地址
    private String account = "";//账号
    private String phone;//电话
    private String password;//密码
    private int sex;//性别
    private String title;//职称
    private String username = "";//昵称
    private String district = "";//区域
    private String level = "";//水平
    private String ball_type = "";//球种
    private String appoint_date;//约球时间
    private String ballArm = "";//使用球杆
    private String usedType = "";//使用习惯
    private int ballAge;//球龄
    private String idol = "";//偶像
    private String idol_name = "";//签名
    private String new_img = "";//最新照片在网络中的位置
    private String new_img_real;//最新照片在本地的位置

    private static final String JSON_IMGREAL = "img_url";
    private static final String JSON_ACCOUNT = "account";
    private static final String JSON_SEX = "sex";
    private static final String JSON_USERNAME = "username";
    private static final String JSON_DISTRICT = "district";
    private static final String JSON_LEVEL = "level";
    private static final String JSON_BALL_TYPE = "ball_type";
    private static final String JSON_BALL_ARM = "ball_arm";
    private static final String JSON_USED_TYPE = "used_type";
    private static final String JSON_BALL_AGE = "ball_age";
    private static final String JSON_IDOL = "idol";
    private static final String JSON_SIGN = "sign";
    private static final String JSON_NEW_IMG = "new_img";


    private String token;
    private int user_id;
    private String login_time;//登录时间

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

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
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
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getBall_type() {
        return ball_type;
    }

    public void setBall_type(String ball_type) {
        this.ball_type = ball_type;
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
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getLogin_time() {
        return login_time;
    }

    public void setLogin_time(String login_time) {
        this.login_time = login_time;
    }


    public String getBallArm() {
        return ballArm;
    }

    public void setBallArm(String ballArm) {
        this.ballArm = ballArm;
    }

    public String getUsedType() {
        return usedType;
    }

    public void setUsedType(String usedType) {
        this.usedType = usedType;
    }

    public int getBallAge() {
        return ballAge;
    }

    public void setBallAge(int ballAge) {
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

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_IMGREAL, img_url);
        json.put(JSON_ACCOUNT, account);
        json.put(JSON_SEX, String.valueOf(sex));
        json.put(JSON_USERNAME, username);
        json.put(JSON_DISTRICT, district);
        json.put(JSON_LEVEL, level);
        json.put(JSON_BALL_TYPE, ball_type);
        json.put(JSON_BALL_ARM, ballArm);
        json.put(JSON_USED_TYPE, usedType);
        json.put(JSON_BALL_AGE, String.valueOf(ballAge));
        json.put(JSON_IDOL, idol);
        json.put(JSON_SIGN, idol_name);
        json.put(JSON_NEW_IMG, new_img_real);
        return json;
    }

    public UserInfo(JSONObject obj) throws JSONException {
        img_url = String.valueOf(obj.getString(JSON_IMGREAL));
        account = String.valueOf(obj.getString(JSON_ACCOUNT));
        username = String.valueOf(obj.getString(JSON_USERNAME));
        district = obj.getString(JSON_DISTRICT);
        level = String.valueOf(obj.getString(JSON_LEVEL));
        ball_type = String.valueOf(obj.getString(JSON_BALL_TYPE));
//        appoint_date = obj.getString("appoint_date");
        ballArm = obj.getString(JSON_BALL_ARM);
        usedType = obj.getString(JSON_USED_TYPE);
        ballAge = obj.getInt(JSON_BALL_AGE);
        idol = String.valueOf(obj.getString(JSON_IDOL));
        idol_name = obj.getString(JSON_SIGN);
//        new_img = obj.getString(JSON_NEW_IMG);
//        JSONArray list_data = obj.getJSONArray("sex");//TODO

    }

    public UserInfo() {

    }
}
