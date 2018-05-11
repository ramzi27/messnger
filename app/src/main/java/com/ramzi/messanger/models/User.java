package com.ramzi.messanger.models;

import java.io.Serializable;

/**
 * Created by Ramzi on 31-Mar-18.
 */

public class User implements Serializable {
    private String imgUrl, name;
    private int msgNo;
    private String userId;
    private boolean status;

    public User() {
    }

    public User(String imgUrl, String name, int msgNo, boolean isOnline) {
        this.imgUrl = imgUrl;
        this.name = name;
        this.msgNo = msgNo;
//        this.isOnline = isOnline;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
    //    boolean isOnline;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMsgNo() {
        return msgNo;
    }

    public void setMsgNo(int msgNo) {
        this.msgNo = msgNo;
    }
}

//    public boolean isOnline() {
//        return isOnline;
//    }
//
//    public void setOnline(boolean online) {
//        isOnline = online;
//    }
//}
