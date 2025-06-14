package com.example.projetjavafx.root.social;

import java.util.Date;

public class Comment {
    private int commentId;
    private int postId;
    private int userId;
    private String content;
    private Date createdAt;
    private String userName; // Add this field
    
    // Add this to your constructor
    public Comment(int commentId, int postId, int userId, String content, Date createdAt) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.userName = "User " + userId; // Default username based on userId
    }
    
    // Add this constructor if you want to set the username explicitly
    public Comment(int commentId, int postId, int userId, String userName, String content, Date createdAt) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
    }
    
    // Add this getter method
    public String getUserName() {
        return userName;
    }
    
    // Add this setter method
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    // Getters and setters
    public int getCommentId() {
        return commentId;
    }
    
    public void setCommentId(int commentId) {
        this.commentId = commentId;
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
    
    // Add this method to get the timestamp as a string
    public String getTimestamp() {
        if (createdAt == null) {
            return "";
        }
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm");
        return sdf.format(createdAt);
    }
}