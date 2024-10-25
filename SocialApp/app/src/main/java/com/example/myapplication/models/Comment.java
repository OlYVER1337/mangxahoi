package com.example.myapplication.models;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Comment {
    private String id;
    private String userId;
    private String userName;
    private String userImage;
    private String content;
    private String imageUrl;
    private Timestamp timestamp;

    // Constructor, getters, and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedDate() {
        Date commentDate;
        if (timestamp != null) {
            commentDate = timestamp.toDate();
        } else {
            return "";
        }

        Date now = new Date();
        long diffInMillis = now.getTime() - commentDate.getTime();
        long diffInSeconds = diffInMillis / 1000;
        long diffInMinutes = diffInSeconds / 60;
        long diffInHours = diffInMinutes / 60;
        long diffInDays = diffInHours / 24;
        long diffInMonths = diffInDays / 30;

        if (diffInDays > 30) {
            return diffInMonths + " tháng trước";
        } else if (diffInDays > 0) {
            return diffInDays + " ngày trước";
        } else if (diffInHours > 0) {
            return diffInHours + " giờ trước";
        } else if (diffInMinutes > 0) {
            return diffInMinutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }
}
