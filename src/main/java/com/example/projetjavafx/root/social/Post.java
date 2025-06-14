package com.example.projetjavafx.root.social;

import java.util.Date;

public class Post {
    private int postId; // Assuming there's a postId field
    private int userId;
    private String content;
    private Date createdAt;
    private String imagePath;
    private int likeCount;
    private int commentCount;
    private boolean likedByCurrentUser;
    
    // Constructor for new posts (without ID)
    public Post(int userId, String content, Date createdAt, String imagePath) {
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt != null ? createdAt : new Date();
        this.imagePath = imagePath;
        this.likeCount = 0;
        this.commentCount = 0;
        this.likedByCurrentUser = false;
    }
    
    // Constructor for posts from database (with ID)
    public Post(int postId, int userId, String content, Date createdAt, String imagePath, int likeCount, int commentCount) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.imagePath = imagePath;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.likedByCurrentUser = false;
    }
    
    // Getters and setters
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public int getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
    
    public int getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
    
    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }
    
    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }
    
    // Add this field to your Post class
    private int groupId;
    
    // Add these getter and setter methods
    public int getGroupId() {
        return groupId;
    }
    
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
    
    // Field for shared events
    private int eventId;
    
    // Getter and setter for eventId
    public int getEventId() {
        return eventId;
    }
    
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
}