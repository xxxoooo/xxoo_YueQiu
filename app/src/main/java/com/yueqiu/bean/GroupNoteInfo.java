package com.yueqiu.bean;


/**
 * Created by wangyun on 14/12/30.
 */
public class GroupNoteInfo {

    private int type;

    private int noteId;
//    private String userId;
    private String userName;
    private int sex;
    private int browseCount;
    private String issueTime;
    private String title;
    private String content;
    private int loveNums;
    private int commentCount;
    private String snippet;//内容的片段摘要
    private String attachMent;//附件流
    private String img_url;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }



    public int getLoveNums() {
            return loveNums;
        }

    public void setLoveNums(int loveNums) {
            this.loveNums = loveNums;
        }

    public int getNoteId() {
            return noteId;
        }

    public void setNoteId(int noteId) {
            this.noteId = noteId;
        }

//    public String getUserId() {
//            return userId;
//        }
//
//    public void setUserId(String userId) {
//            this.userId = userId;
//        }

    public String getUserName() {
            return userName;
        }

    public void setUserName(String userName) {
            this.userName = userName;
        }

    public int getSex() {
            return sex;
        }

    public void setSex(int sex) {
            this.sex = sex;
        }

    public String getIssueTime() {
            return issueTime;
        }

    public void setIssueTime(String issueTime) {
            this.issueTime = issueTime;
        }

    public int getBrowseCount() {
            return browseCount;
        }

    public void setBrowseCount(int browseCount) {
            this.browseCount = browseCount;
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

    public int getCommentCount() {
            return commentCount;
        }

    public void setCommentCount(int commentCount) {
            this.commentCount = commentCount;
        }

    public String getSnippet() {
            return snippet;
        }

    public void setSnippet(String snippet) {
            this.snippet = snippet;
        }

    public String getAttachMent() {
            return attachMent;
        }

    public void setAttachMent(String attachMent) {
            this.attachMent = attachMent;
        }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    @Override
    public boolean equals(Object o) {
        GroupNoteInfo info = (GroupNoteInfo) o;
        if(this.getNoteId() == info.getNoteId()){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result  = 37 * result + this.noteId;
        return result;
    }

    @Override
    public String toString() {
        return "note_id->" + noteId;
    }
}
