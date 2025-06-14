package com.example.projetjavafx.root.group;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.auth.SessionManager;
import com.example.projetjavafx.root.profile.ProfileRepository;
import com.example.projetjavafx.root.social.Post;
import com.example.projetjavafx.root.social.PostDAO;
import com.example.projetjavafx.root.social.PostItemController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class GroupFeedController {

    @FXML private ImageView groupProfileImage;
    @FXML private Label groupNameLabel;
    @FXML private Label groupDescriptionLabel;
    @FXML private Label memberCountLabel;
    @FXML private Label postCountLabel;
    @FXML private Button joinLeaveButton;
    @FXML private HBox adminButtonsContainer;
    @FXML private VBox postCreationArea;
    @FXML private TextArea newPostContent;
    @FXML private FlowPane postsContainer;
    @FXML private TextField searchField;
    @FXML private ImageView userProfilePic;
    @FXML private Label userName;
    @FXML private Button organizerButton;
    @FXML private Button eventsButton;
    @FXML private Button groupButton;
    @FXML private Button jobButton;
    @FXML private Button createJobButton;
    @FXML private Button profileButton;
    @FXML private Button chatbotButton;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button logoutButtonn;
    @FXML private HBox authButtons;
    @FXML private Button addPostButton;
    
    private GroupProfileRepository groupModel = new GroupProfileRepository(null, null, null, null);
    private GroupAddMemberRepository groupMemberModel = new GroupAddMemberRepository();
    private PostDAO postDAO;
    private Connection connection;
    private int currentGroupId;
    private int currentUserId;
    private String selectedImagePath;
    
    @FXML
    public void initialize() {
        // Initialize connection
        try {
            connection = AivenMySQLManager.getConnection();
            postDAO = new PostDAO(connection);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not connect to database: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Get current user ID
        currentUserId = SessionManager.getInstance().getCurrentUserId();
        
        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchPosts(newValue);
        });
        
        // Setup UI based on authentication status
        authButtons.visibleProperty().bind(SessionManager.getInstance().isUserNotConnected());
        logoutButtonn.visibleProperty().bind(SessionManager.getInstance().canLogout());
        
        // Load user profile information
        loadUserProfileInfo();
    }
    
    /**
     * Loads the user profile information using ProfileRepository
     */
    private void loadUserProfileInfo() {
        try {
            int userId = SessionManager.getInstance().getCurrentUserId();
            if (userId != -1) {
                // Load user profile data from ProfileRepository
                com.example.projetjavafx.root.profile.ProfileRepository userProfile = 
                    com.example.projetjavafx.root.profile.ProfileRepository.getUserProfile(userId);
                
                if (userProfile != null) {
                    // Set username from profile
                    userName.setText(userProfile.getUsername());
                    
                    // Load profile image if available
                    String imageUrl = userProfile.getProfilePicture();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        userProfilePic.setImage(new Image(imageUrl));
                    } else {
                        // Load default image if no profile picture is set
                        InputStream is = getClass().getResourceAsStream("/com/example/projetjavafx/social/img/userprofile.png");
                        if (is != null) {
                            Image avatar = new Image(is);
                            userProfilePic.setImage(avatar);
                        }
                    }
                } else {
                    // Fallback to default values if profile couldn't be loaded
                    userName.setText("User");
                    InputStream is = getClass().getResourceAsStream("/com/example/projetjavafx/social/img/userprofile.png");
                    if (is != null) {
                        Image avatar = new Image(is);
                        userProfilePic.setImage(avatar);
                    }
                }
                applyCircularClipToImageView(userProfilePic);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading user profile: " + e.getMessage());
        }
    }
    private void applyCircularClipToImageView(ImageView imageView) {
        if (imageView == null) return;

        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(
                imageView.getFitWidth() / 2,
                imageView.getFitHeight() / 2,
                Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2
        );
        imageView.setClip(clip);
    }

    /**
     * Sets the group ID and loads the group data
     */
    public void setGroupId(int groupId) {
        this.currentGroupId = groupId;
        loadGroupData();
        loadGroupPosts();
        updateUIBasedOnMembership();
    }
    
    /**
     * Loads the group data from the database
     */
    private void loadGroupData() {
        try {
            GroupProfileRepository groupData = groupModel.getGroupById(currentGroupId);
            if (groupData != null) {
                String name = groupData.getName();
                String description = groupData.getDescription();
                String profilePicture = groupData.getProfilePicture();
                
                // Set group name and description
                groupNameLabel.setText(name);
                groupDescriptionLabel.setText(description);
                
                // Set group profile image
                if (profilePicture != null && !profilePicture.isEmpty()) {
                    byte[] imageBytes = Base64.getDecoder().decode(profilePicture);
                    Image image = new Image(new ByteArrayInputStream(imageBytes));
                    groupProfileImage.setImage(image);
                } else {
                    loadDefaultGroupPicture();
                }
                
                // Update member count
                int memberCount = groupMemberModel.getGroupMemberCount(currentGroupId);
                memberCountLabel.setText("Members: " + memberCount);
                
                // Update post count
                int postCount = getGroupPostCount();
                postCountLabel.setText("Posts: " + postCount);
            } else {
                showAlert("Error", "Group not found.");
            }
        } catch (Exception e) {
            showAlert("Database Error", "Could not load group data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the default group picture
     */
    private void loadDefaultGroupPicture() {
        try {
            // Try to load from resources first
            InputStream is = getClass().getResourceAsStream("/com/example/projetjavafx/social/img/groupprofile.png");
            if (is != null) {
                Image avatar = new Image(is);
                groupProfileImage.setImage(avatar);
            } else {
                // Fallback to a file path if resource not found
                groupProfileImage.setFitWidth(100);
                groupProfileImage.setFitHeight(100);
            }
        } catch (Exception e) {
            System.err.println("Error loading group avatar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Updates the UI based on the user's membership status
     */
    private void updateUIBasedOnMembership() {
        boolean isMember = groupMemberModel.isUserMemberOfGroup(currentGroupId, currentUserId);
        boolean isAdmin = groupMemberModel.isUserGroupCreator(currentGroupId, currentUserId);
        
        // Update join/leave button
        if (isMember) {
            joinLeaveButton.setText("Leave Group");
            postCreationArea.setVisible(true);
            postCreationArea.setManaged(true);
        } else {
            joinLeaveButton.setText("Join Group");
            postCreationArea.setVisible(false);
            postCreationArea.setManaged(false);
        }
        
        // Show admin buttons if user is admin
        adminButtonsContainer.setVisible(isAdmin);
        adminButtonsContainer.setManaged(isAdmin);
    }
    
    /**
     * Loads the posts for the current group
     */
    public void loadGroupPosts() {
        try {
            postsContainer.getChildren().clear();
            List<Post> posts = postDAO.getPostsByGroupId(currentGroupId);
            
            for (Post post : posts) {
                addPostToFeed(post);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load group posts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Adds a post to the feed
     */
    private void addPostToFeed(Post post) {
        try {
            URL fxmlLocation = getClass().getResource("/com/example/projetjavafx/social/PostItem.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: PostItem.fxml not found");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent postNode = loader.load();
            postsContainer.getChildren().add(postNode);
            
            PostItemController controller = loader.getController();
            controller.setData(post, connection, currentUserId, this); // Pass 'this' as GroupFeedController
            controller.setParentController(this); // Ensure this matches the new method signature
            controller.setPostId(post.getPostId());
        } catch (IOException e) {
            System.err.println("Error loading post item: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Navigation methods
    private void loadView(String fxmlPath) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                Stage stage = (Stage) groupNameLabel.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error loading view: " + e.getMessage());
            }
        });
    }
    
    @FXML
    public void onHomeClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/root/root-view.fxml");
    }
    
    @FXML
    public void onOrganizerButtonClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId() == -1) {
            loadView("/com/example/projetjavafx/auth/login-view.fxml");
        } else {
            loadView("/com/example/projetjavafx/organizer/organizer-view.fxml");
        }
    }
    
    @FXML
    public void onEventsClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/events/events-view.fxml");
    }
    
    @FXML
    public void onGroupButtonClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId() == -1) {
            loadView("/com/example/projetjavafx/auth/login-view.fxml");
        } else {
            loadView("/com/example/projetjavafx/group/group-profile-view.fxml");
        }
    }
    
    @FXML
    public void onJobFeedClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId() == -1) {
            loadView("/com/example/projetjavafx/auth/login-view.fxml");
        } else {
            loadView("/com/example/projetjavafx/jobfeed/job-feed-view.fxml");
        }
    }
    
    @FXML
    public void onCreateJobClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId() == -1) {
            loadView("/com/example/projetjavafx/auth/login-view.fxml");
        } else {
            loadView("/com/example/projetjavafx/organizer/create-job-offer-view.fxml");
        }
    }
    
    @FXML
    public void onProfileClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId() == -1) {
            loadView("/com/example/projetjavafx/auth/login-view.fxml");
        } else {
            loadView("/com/example/projetjavafx/profile/profile-view.fxml");
        }
    }
    
    @FXML
    public void onChatbotClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/chatbot/chatbot.fxml");
    }
    
    @FXML
    public void onLoginClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/auth/login-view.fxml");
    }
    
    @FXML
    public void onRegisterClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/auth/register-view.fxml");
    }
    
    @FXML
    public void onLogout() {
        SessionManager.getInstance().logout();
        loadView("/com/example/projetjavafx/auth/login-view.fxml");
    }
    
    /**
     * Gets the number of posts in the current group
     */
    private int getGroupPostCount() {
        try {
            List<Post> posts = postDAO.getPostsByGroupId(currentGroupId);
            return posts.size();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Handles the join/leave group button click
     */
    @FXML
    private void handleJoinLeaveButton() {
        boolean isMember = groupMemberModel.isUserMemberOfGroup(currentGroupId, currentUserId);
        
        if (isMember) {
            // Leave group
            boolean success = groupMemberModel.removeUserFromGroup(currentGroupId, currentUserId);
            if (success) {
                showAlert("Success", "You have left the group.");
                updateUIBasedOnMembership();
            } else {
                showAlert("Error", "Could not leave the group.");
            }
        } else {
            // Join group
            boolean success = groupMemberModel.addMemberToGroup(currentGroupId, currentUserId, "member");
            if (success) {
                showAlert("Success", "You have joined the group.");
                updateUIBasedOnMembership();
            } else {
                showAlert("Error", "Could not join the group.");
            }
        }
    }
    
    /**
     * Handles the create post button click
     */
    @FXML
    private void handleCreatePost() {
        String content = newPostContent.getText().trim();
        if (content.isEmpty() && selectedImagePath == null) {
            showAlert("Error", "Post cannot be empty.");
            return;
        }
        
        try {
            Post post = new Post(currentUserId, content, new Date(), selectedImagePath);
            post.setGroupId(currentGroupId);
            
            postDAO.savePost(post);
            newPostContent.clear();
            selectedImagePath = null;
            
            // Refresh posts
            loadGroupPosts();
            
            // Update post count
            int postCount = getGroupPostCount();
            postCountLabel.setText("Posts: " + postCount);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not create post: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the select image button click
     */
    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(postsContainer.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Create directory if it doesn't exist
                Path imagesDir = Paths.get("src/main/resources/com/example/projetjavafx/img/posts");
                if (!Files.exists(imagesDir)) {
                    Files.createDirectories(imagesDir);
                }
                
                // Generate unique filename
                String uniqueFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = imagesDir.resolve(uniqueFileName);
                
                // Copy file
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Set image path
                selectedImagePath = "/com/example/projetjavafx/img/posts/" + uniqueFileName;
            } catch (IOException e) {
                showAlert("Error", "Could not save image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Handles the edit group button click
     */
    @FXML
    private void handleEditGroup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/group/group-update-view.fxml"));
            Parent root = loader.load();
            
            GroupUpdateController controller = loader.getController();
            controller.setGroupId(currentGroupId);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Update Group");
            stage.showAndWait();
            
            // Refresh group data after update
            loadGroupData();
        } catch (IOException e) {
            showAlert("Error", "Could not open edit group view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the delete group button click
     */
    @FXML
    private void handleDeleteGroup() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this group? This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = groupModel.deleteGroup(currentGroupId);
            if (success) {
                showAlert("Success", "Group deleted successfully.");
                
                // Navigate back to group list
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/group/group-profile-view.fxml"));
                    Parent root = loader.load();
                    
                    Stage stage = (Stage) groupNameLabel.getScene().getWindow();
                    stage.setScene(new Scene(root));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                showAlert("Error", "Could not delete group.");
            }
        }
    }
    
    /**
     * Handles the home button click
     */
    @FXML
    private void handleHomeButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/root/home/home-view.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) groupNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert("Error", "Could not navigate to home: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method for navigation
    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Shows an alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Searches posts in the current group
     * @param query The search query
     */
    private void searchPosts(String query) {
        try {
            postsContainer.getChildren().clear();
            if (query == null || query.trim().isEmpty()) {
                // If search query is empty, load all posts
                loadGroupPosts();
                return;
            }
            
            // Get posts that match the search query
            List<Post> posts = postDAO.getPostsByGroupId(currentGroupId);
            for (Post post : posts) {
                // Simple search in post content
                if (post.getContent() != null && post.getContent().toLowerCase().contains(query.toLowerCase())) {
                    addPostToFeed(post);
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Could not search posts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes a post from the group feed
     * @param postId The ID of the post to delete
     */
    public void deletePost(int postId) {
        try {
            Post post = postDAO.getPostById(postId);
            if (post == null) {
                showAlert("Error", "Post not found.");
                return;
            }
            
            // Check if the current user is the post owner or a group admin
            boolean isPostOwner = post.getUserId() == currentUserId;
            boolean isGroupAdmin = groupMemberModel.isUserGroupCreator(currentGroupId, currentUserId);
            
            if (isPostOwner || isGroupAdmin) {
                postDAO.deletePost(postId);
                loadGroupPosts(); // Refresh posts after deletion
                
                // Update post count
                int postCount = getGroupPostCount();
                postCountLabel.setText("Posts: " + postCount);
            } else {
                showAlert("Permission Denied", "You can only delete your own posts or posts in groups you administer.");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Could not delete post: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath, ActionEvent event) {
        try {


            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);




            stage.show();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    @FXML
    protected void onsocialButtonClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){
            loadView("/com/example/projetjavafx/auth/login-view.fxml", event);
        } else {
            // Make sure this path is correct and the file exists
            loadView("/com/example/projetjavafx/social/Feed.fxml", event);
        }
    }
    /**
     * Edits a post in the group feed
     * @param postId The ID of the post to edit
     * @param newContent The new content for the post
     * @param newImagePath The new image path for the post
     */
    public void editPost(int postId, String newContent, String newImagePath) {
        try {
            // Ensure postDAO is initialized
            if (postDAO == null) {
                connection = AivenMySQLManager.getConnection();
                postDAO = new PostDAO(connection);
            }
            
            Post post = postDAO.getPostById(postId);
            if (post == null) {
                showAlert("Error", "Post not found.");
                return;
            }
            
            // Check if the current user is the post owner
            if (post.getUserId() == currentUserId) {
                postDAO.updatePost(postId, newContent, newImagePath);
                loadGroupPosts(); // Refresh posts after edit
            } else {
                showAlert("Permission Denied", "You can only edit your own posts.");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Could not edit post: " + e.getMessage());
            e.printStackTrace();
        }
    }


}