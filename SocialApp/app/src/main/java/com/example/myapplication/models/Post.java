package com.example.myapplication.models;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;

public class Post implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String content;
    private String imageUrl;
    private String videoUrl;
    private long timestamp; // Lưu trữ dưới dạng long cho serialization
    private int likes;
    private List<String> likedBy;
    private List<String> commentIds;
    private String userName;
    private String userImage;
    private int commentCount;

    // Constructor mặc định cần thiết cho Firestore
    public Post() {}

    // Constructor sử dụng Timestamp
    public Post(String id, String userId, String content, String imageUrl, String videoUrl, Timestamp timestamp, int likes, List<String> commentIds, int commentCount) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        setTimestamp(timestamp);
        this.likes = likes;
        this.commentIds = commentIds;
        this.commentCount = commentCount;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public String getVideoUrl() { return videoUrl; }
    public Timestamp getTimestamp() {
        return new Timestamp(new Date(timestamp));
    }
    public int getLikes() { return likes; }
    public List<String> getLikedBy() {
        return likedBy;
    }
    public List<String> getCommentIds() { return commentIds; }
    public String getUserName() { return userName; }
    public String getUserImage() { return userImage; }
    public int getCommentCount() {
        return commentCount;
    }


    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setContent(String content) { this.content = content; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
            this.timestamp = timestamp.toDate().getTime();
        } else {
            this.timestamp = 0;
        }
    }
    public void setLikes(int likes) { this.likes = likes; }
    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }
    public void setCommentIds(List<String> commentIds) { this.commentIds = commentIds; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserImage(String userImage) { this.userImage = userImage; }
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    // Phương thức tiện ích


    public Date getDate() {
        return new Date(timestamp);
    }
    public String getFormattedDate() {
        if (timestamp == 0) {
            return "";
        }

        Date postDate = new Date(timestamp);
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
    public boolean isLikedBy(String userId) {

        return likedBy != null && likedBy.contains(userId);
    }

    public void toggleLike(String userId) {
        if (likedBy == null) {
            likedBy = new ArrayList<>();
        }
        if (likedBy.contains(userId)) {
            likedBy.remove(userId);
            likes--;
        } else {
            likedBy.add(userId);
            likes++;
        }
    }

   /* public boolean canViewPost(User currentUser) {
        return currentUser.isFriend(this.userId) || currentUser.getId().equals(this.userId);
    }*/

    public void addCommentId(String commentId) {
        this.commentIds.add(commentId);
    }

    public void removeCommentId(String commentId) {
        this.commentIds.remove(commentId);
    }
}
