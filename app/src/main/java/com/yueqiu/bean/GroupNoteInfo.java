package com.yueqiu.bean;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 14/12/30.
 */
public class GroupNoteInfo implements Parcelable{

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
//    private String snippet;//内容的片段摘要
//    private String attachMent;//附件流
    private String img_url;
    private String extra_img_url;

    public List<UserInfo> mCommentList = new ArrayList<UserInfo>();

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

    public String getExtra_img_url() {
        return extra_img_url;
    }

    public void setExtra_img_url(String extra_img_url) {
        this.extra_img_url = extra_img_url;
    }

    //    public String getSnippet() {
//            return snippet;
//        }
//
//    public void setSnippet(String snippet) {
//            this.snippet = snippet;
//        }
//
//    public String getAttachMent() {
//            return attachMent;
//        }
//
//    public void setAttachMent(String attachMent) {
//            this.attachMent = attachMent;
//        }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public GroupNoteInfo(Parcel in) {
        type = in.readInt();
        noteId = in.readInt();
        userName = in.readString();
        sex = in.readInt();
        browseCount = in.readInt();
        issueTime = in.readString();
        title = in.readString();
        content = in.readString();
        loveNums = in.readInt();
        commentCount = in.readInt();
        img_url = in.readString();
        extra_img_url = in.readString();
    }

    public GroupNoteInfo() {
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

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeInt(noteId);
        dest.writeString(userName);
        dest.writeInt(sex);
        dest.writeInt(browseCount);
        dest.writeString(issueTime);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeInt(loveNums);
        dest.writeInt(commentCount);
        dest.writeString(img_url);
        dest.writeString(extra_img_url);
    }
    public static final Creator<GroupNoteInfo> CREATOR = new Creator<GroupNoteInfo>() {
        @Override
        public GroupNoteInfo createFromParcel(Parcel source) {
            return new GroupNoteInfo(source);
        }

        @Override
        public GroupNoteInfo[] newArray(int size) {
            return new GroupNoteInfo[size];
        }
    };

}
