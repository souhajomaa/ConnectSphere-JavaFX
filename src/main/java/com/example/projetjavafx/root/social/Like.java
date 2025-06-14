package com.example.projetjavafx.root.social;

import java.time.LocalDateTime;

public class Like {
    private int likeId;
    private int postId;
    private int userId;
    private String timestamp;
    private String userName; // For display purposes

    public Like(int postId, int userId) {
        this.postId = postId;
        this.userId = userId;
        this.timestamp = LocalDateTime.now().toString();
    }

    public int getLikeId() {
        return likeId;
    }

    public void setLikeId(int likeId) {
        this.likeId = likeId;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}