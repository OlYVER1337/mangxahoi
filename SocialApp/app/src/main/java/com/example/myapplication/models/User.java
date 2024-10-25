package com.example.myapplication.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User implements Serializable {
    public String id;
    public String name;
    public String email;
    public String image;
    public String token;
    public List<String> posts;
    public List<String> friendRequests; // Danh sách ID của người gửi lời mời kết bạn
    public List<String> sentFriendRequests; // Danh sách ID của người nhận lời mời kết bạn từ user này
    public List<String> friends;
    public boolean hasPendingRequest;
    public com.google.firebase.Timestamp timestamp;

    public User() {
        // Default constructor required for Firestore
        friends = new ArrayList<>();
        friendRequests = new ArrayList<>();
        sentFriendRequests = new ArrayList<>();

    }

    public User(String id, String name, String email, String image) {
        this();
        this.id = id;
        this.name = name;
        this.email = email;
        this.image = image;

    }
    public com.google.firebase.Timestamp getTimestamp() {
        return timestamp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }
    public void setTimestamp(com.google.firebase.Timestamp timestamp) {
        this.timestamp = timestamp;
    }


    public void setImage(String image) {
        this.image = image;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public List<String> getPosts() {
        return posts;
    }

    public void setPosts(List<String> posts) {
        this.posts = posts;
    }

    public boolean hasPendingRequest() {
        return hasPendingRequest;
    }

    public void setHasPendingRequest(boolean hasPendingRequest) {
        this.hasPendingRequest = hasPendingRequest;
    }

    public String getFormattedDate() {
        if (timestamp == null) {
            return "";
        }
        Date postDate = timestamp.toDate();
        Date now = new Date();
        long diffInMillis = now.getTime() - postDate.getTime();
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


