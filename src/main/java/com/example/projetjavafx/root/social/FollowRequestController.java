package com.example.projetjavafx.root.social;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.auth.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.example.projetjavafx.utils.ImageUtils;

public class FollowRequestController {
    @FXML
    private VBox requestsContainer;
    @FXML
    private Label noRequestsLabel;
    
    private Connection connection;
    private FollowRequestDAO followRequestDAO;
    private int currentUserId;
    
    public void initialize() {
        try {
            connection = AivenMySQLManager.getConnection();
            followRequestDAO = new FollowRequestDAO(connection);
            currentUserId = SessionManager.getInstance().getCurrentUserId();
            
            System.out.println("DEBUG: FollowRequestController initialized with user ID: " + currentUserId);
            
            if (currentUserId <= 0) {
                System.err.println("ERROR: Invalid user ID: " + currentUserId);
                showError("Authentication Error", "User not properly logged in. User ID: " + currentUserId);
                return;
            }
            
            loadPendingRequests();
        } catch (SQLException e) {
            System.err.println("Error initializing FollowRequestController: " + e.getMessage());
            showError("Database Error", "Could not connect to database: " + e.getMessage());
        }
    }
    
    private void loadPendingRequests() {
        requestsContainer.getChildren().clear();
        
        System.out.println("DEBUG: Loading pending requests for user ID: " + currentUserId);
        
        List<FollowRequest> pendingRequests = followRequestDAO.getPendingRequests(currentUserId);
        
        System.out.println("DEBUG: Found " + pendingRequests.size() + " pending requests");
        
        if (pendingRequests.isEmpty()) {
            System.out.println("DEBUG: No pending requests found, showing 'no requests' label");
            noRequestsLabel.setVisible(true);
            noRequestsLabel.setManaged(true);
        } else {
            System.out.println("DEBUG: Found pending requests, hiding 'no requests' label");
            noRequestsLabel.setVisible(false);
            noRequestsLabel.setManaged(false);
            
            for (FollowRequest request : pendingRequests) {
                System.out.println("DEBUG: Processing request from user " + request.getSenderUsername() + " (ID: " + request.getSenderId() + ")");
                VBox requestItem = createRequestItem(request);
                requestsContainer.getChildren().add(requestItem);
            }
        }
    }
    
    private VBox createRequestItem(FollowRequest request) {
        VBox requestItem = new VBox(10);
        requestItem.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-border-width: 1; -fx-padding: 15;");
        
        HBox userInfo = new HBox(15);
        userInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Profile image
        ImageView profileImage = new ImageView();
        profileImage.setFitHeight(50);
        profileImage.setFitWidth(50);
        profileImage.setPreserveRatio(true);
        
        if (request.getSenderProfilePicture() != null && !request.getSenderProfilePicture().isEmpty()) {
            try {
                Image avatar;
                String profilePictureData = request.getSenderProfilePicture();
                // Check if it's Base64 encoded data
                if (ImageUtils.isValidBase64Image(profilePictureData)) {
                    avatar = ImageUtils.base64ToImage(profilePictureData);
                } else {
                    // Legacy support for URL-based images
                    avatar = new Image(profilePictureData);
                }
                
                if (avatar != null && !avatar.isError()) {
                    profileImage.setImage(avatar);
                } else {
                    setDefaultProfileImage(profileImage);
                }
            } catch (Exception e) {
                setDefaultProfileImage(profileImage);
            }
        } else {
            setDefaultProfileImage(profileImage);
        }
        
        // Apply circular clip
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(25);
        profileImage.setClip(clip);
        
        // User details
        VBox userDetails = new VBox(5);
        Label usernameLabel = new Label(request.getSenderUsername());
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label emailLabel = new Label(request.getSenderEmail());
        emailLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        Label requestDateLabel = new Label("Requested: " + request.getRequestDate().toLocalDate());
        requestDateLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
        
        userDetails.getChildren().addAll(usernameLabel, emailLabel, requestDateLabel);
        
        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button acceptButton = new Button("Accept");
        acceptButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        acceptButton.setOnAction(e -> acceptRequest(request));
        
        Button rejectButton = new Button("Reject");
        rejectButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        rejectButton.setOnAction(e -> rejectRequest(request));
        
        actionButtons.getChildren().addAll(acceptButton, rejectButton);
        
        userInfo.getChildren().addAll(profileImage, userDetails);
        
        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        HBox mainContainer = new HBox();
        mainContainer.getChildren().addAll(userInfo, spacer, actionButtons);
        mainContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        requestItem.getChildren().add(mainContainer);
        
        return requestItem;
    }
    
    private void setDefaultProfileImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/projetjavafx/social/img/user.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            // If default image fails, create a simple colored circle
            imageView.setStyle("-fx-background-color: #cccccc; -fx-background-radius: 25;");
        }
    }
    
    private void acceptRequest(FollowRequest request) {
        if (followRequestDAO.acceptFollowRequest(request.getId())) {
            showSuccess("Request Accepted", "You are now being followed by " + request.getSenderUsername());
            loadPendingRequests(); // Refresh the list
            
            // Notify any open profile views to update their counters
            notifyProfileUpdate();
        } else {
            showError("Error", "Could not accept the follow request. Please try again.");
        }
    }
    
    /**
     * Notifies other controllers that profile information should be updated
     */
    private void notifyProfileUpdate() {
        // This could be implemented with an event system or observer pattern
        // For now, we'll just print a message
        System.out.println("Profile counters should be updated after follow request acceptance");
    }
    
    private void rejectRequest(FollowRequest request) {
        if (followRequestDAO.rejectFollowRequest(request.getId())) {
            showInfo("Request Rejected", "Follow request from " + request.getSenderUsername() + " has been rejected.");
            loadPendingRequests(); // Refresh the list
        } else {
            showError("Error", "Could not reject the follow request. Please try again.");
        }
    }
    
    @FXML
    private void goBackToFeed() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/social/UserProfile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) requestsContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading social feed: " + e.getMessage());
            showError("Navigation Error", "Could not return to social feed.");
        }
    }
    
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void showSuccess(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}