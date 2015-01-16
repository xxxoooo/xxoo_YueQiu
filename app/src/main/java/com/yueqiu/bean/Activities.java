package com.yueqiu.bean;

/**
 * Created by yinfeng on 15/1/3.
 */
public class Activities {
    private String id;
    private String username;
    private String sex;//男1女2
    private String img_url;
    private String type;
    private String title;
    private String address;
    private String begin_time;
    private String end_time;
    private String model;
    private String content;
    private String create_time;
    private String look_num;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBegin_time() {
        return begin_time;
    }

    public void setBegin_time(String begin_time) {
        this.begin_time = begin_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getLook_num() {
        return look_num;
    }

    public void setLook_num(String look_num) {
        this.look_num = look_num;
    }


    @Override
    public String toString() {
        return "Activities{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", sex='" + sex + '\'' +
                ", img_url='" + img_url + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", address='" + address + '\'' +
                ", begin_time='" + begin_time + '\'' +
                ", end_time='" + end_time + '\'' +
                ", model='" + model + '\'' +
                ", content='" + content + '\'' +
                ", create_time='" + create_time + '\'' +
                ", look_num=" + look_num +
                '}';
    }
}
