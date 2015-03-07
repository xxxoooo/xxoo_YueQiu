package com.yueqiu.bean;

/**
 * Created by wangyun on 15/3/5.
 */
public class GroupDetailContentItem implements IGroupDetailItem{

    private String title;

    private String content;

    private String img_url;

    public GroupDetailContentItem(String title,String content,String img_url){
        this.title = title;
        this.content = content;
        this.img_url = img_url;
    }
    @Override
    public int getType() {
        return CONTENT_TYPE;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
}
