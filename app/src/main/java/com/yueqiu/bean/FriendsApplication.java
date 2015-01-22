package com.yueqiu.bean;

/**
 * Created by doushuqi on 15/1/14.
 * 好友申请
 */
public class FriendsApplication {
    private String id;//申请id
    private String nick;//昵称
    private String username;//账户
    private String create_time;//请求时间
    private String img_url;//头像
    private int isAgree;//0:未同意；1:同意

    public FriendsApplication(String id, String nick, String username, String create_time, String img_url, int isAgree) {
        this.id = id;
        this.nick = nick;
        this.username = username;
        this.create_time = create_time;
        this.img_url = img_url;
        this.isAgree = isAgree;
    }

    public FriendsApplication() {
    }

    public void setIsAgree(int isAgree) {
        this.isAgree = isAgree;
    }

    public int getIsAgree() {

        return isAgree;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getId() {

        return id;
    }

    public String getNick() {
        return nick;
    }

    public String getUsername() {
        return username;
    }

    public String getCreate_time() {
        return create_time;
    }

    public String getImg_url() {
        return img_url;
    }
}
