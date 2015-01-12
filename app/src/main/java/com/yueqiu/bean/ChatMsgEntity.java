package com.yueqiu.bean;

/**
 * Created by doushuqi on 15/1/12.
 * 聊天详情实体类
 */
public class ChatMsgEntity {
    private String name;// 消息来自
    private String date;// 消息日期
    private String message;// 消息内容
    private int img;
    private boolean isComMsg = true;// 是否为收到的消息

    public ChatMsgEntity() {

    }

    public ChatMsgEntity(String message, String date, boolean isComMsg) {
        this.message = message;
        this.date = date;
        this.isComMsg = isComMsg;
    }

    public ChatMsgEntity(String name, String date, String text, int img,
                         boolean isComMsg) {
        super();
        this.name = name;
        this.date = date;
        this.message = text;
        this.img = img;
        this.isComMsg = isComMsg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setComMsg(boolean isComMsg) {
        this.isComMsg = isComMsg;
    }

    public boolean isComMsg() {

        return isComMsg;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }
}
