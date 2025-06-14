package com.example.projetjavafx.root.social;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.sql.SQLException;
import java.util.List;

public class GroupController {

    @FXML private ListView<Post> groupFeedListView;
    @FXML private TextArea newPostContent;
    @FXML private Button postButton;

    private PostDAO postDAO;
    private int currentGroupId;
    private int currentUserId = 1; // Default user ID, should be set from login

    public void initialize() {
        loadGroupPosts();
        postButton.setOnAction(e -> {
            try {
                handleNewPost();
            } catch (SQLException ex) {
                showAlert("Database Error", "Failed to create new post: " + ex.getMessage());
            }
        });
    }

    // Improved method to convert paths to valid URLs
    private String convertPathToURL(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        try {
            // Handle file paths
            if (!path.startsWith("http") && !path.startsWith("file:")) {
                // Replace backslashes with forward slashes
                path = path.replace('\\', '/');
                
                // Handle Windows drive letters (like Z:)
                if (path.matches("[a-zA-Z]:.*")) {
                    // Remove the colon after the drive letter
                    path = "file:///" + path.charAt(0) + path.substring(2);
                } else {
                    path = "file:///" + path;
                }
            }
            
            // Validate the URL
            java.net.URL url = new java.net.URL(path);
            return url.toString();
        } catch (Exception e) {
            System.err.println("Error converting path to URL: " + path + " - " + e.getMessage());
            
            // Try to load from resources as fallback
            try {
                String resourcePath = "/images/default_avatar.png"; // Default image in resources
                java.net.URL resourceUrl = getClass().getResource(resourcePath);
                if (resourceUrl != null) {
                    return resourceUrl.toExternalForm();
                }
            } catch (Exception ex) {
                System.err.println("Failed to load default image: " + ex.getMessage());
            }
            
            return null;
        }
    }

    // Improved method to load group posts with proper image handling
    // Update this method to load like information
    private void loadGroupPosts() {
        try {
            if (postDAO == null) {
                System.err.println("PostDAO is null. Make sure it's properly initialized.");
                return;
            }
            
            List<Post> groupPosts = postDAO.getPostsByGroupId(currentGroupId);
            
            // Process each post to ensure image paths are valid and load like info
            for (Post post : groupPosts) {
                // Handle image paths
                String imagePath = post.getImagePath();
                if (imagePath != null && !imagePath.isEmpty()) {
                    String validUrl = convertPathToURL(imagePath);
                    post.setImagePath(validUrl);
                }
                
                // Load like information
                try {
                    int likeCount = postDAO.getLikeCount(post.getPostId());
                    post.setLikeCount(likeCount);
                    
                    boolean userLiked = postDAO.hasUserLikedPost(post.getPostId(), currentUserId);
                    post.setLikedByCurrentUser(userLiked);
                } catch (SQLException e) {
                    System.err.println("Error loading like info for post " + post.getPostId() + ": " + e.getMessage());
                }
            }
            
            groupFeedListView.getItems().setAll(groupPosts);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load group posts: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error loading posts: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleNewPost() throws SQLException {
        String content = newPostContent.getText().trim();
        if (!content.isEmpty()) {
            Post newPost = new Post(1, content, null, null); // Remplacez 1 par l'ID utilisateur actuel
            newPost.setGroupId(currentGroupId);
            
            // Fix: use newPost instead of post
            postDAO.addPost(newPost);
            loadGroupPosts();
            newPostContent.clear();
        }
    }

    @FXML
    private void handlePostSubmit() {
        String content = newPostContent.getText().trim();
        
        if (!content.isEmpty()) {
            try {
                // Create a new Post object with the current group ID
                Post post = new Post(
                    currentUserId,
                    content,
                    null, // This will use the current timestamp
                    null  // No image for now
                );
                
                // Set the group ID for this post
                post.setGroupId(currentGroupId);
                
                // Save the post to the database
                postDAO.savePost(post);
                
                // Clear the input field
                newPostContent.clear();
                
                // Refresh the posts list
                loadGroupPosts();
                
            } catch (SQLException e) {
                showAlert("Error", "Failed to post: " + e.getMessage());
            }
        }
    }

    // Method to set up the controller with necessary data
    public void setup(PostDAO postDAO, int groupId, int userId) {
        this.postDAO = postDAO;
        this.currentGroupId = groupId;
        this.currentUserId = userId;
        
        // Load posts after setting up
        loadGroupPosts();
    }
    
    // Add this method to handle like functionality
    @FXML
    public void handleLikePost(Post post) {
        try {
            if (postDAO == null) {
                System.err.println("PostDAO is null. Cannot toggle like.");
                return;
            }
            
            // Toggle like status
            boolean success = postDAO.toggleLike(post.getPostId(), currentUserId);
            if (success) {
                // Refresh the posts to update like counts
                loadGroupPosts();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Error toggling like: " + e.getMessage());
        }
    }
}
