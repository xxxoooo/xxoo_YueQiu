package com.yueqiu.bean;

/**
 * Created by wangyun on 15/3/5.
 */
public class GroupDetailCommentItem implements IGroupDetailItem{

    private String username;

    private String img_url;

    private String create_time;

    private String content;

    public GroupDetailCommentItem(String username, String img_url, String create_time, String content) {
        this.username = username;
        this.img_url = img_url;
        this.create_time = create_time;
        this.content = content;
    }

    @Override
    public int getType() {
        return COMMENT_TYPE;
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

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
