package com.example.projetjavafx.root.social;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.auth.SessionManager;
import com.example.projetjavafx.root.group.GroupFeedController;
import com.example.projetjavafx.root.group.GroupProfileRepository;
import com.example.projetjavafx.root.profile.ProfileRepository;
import java.awt.MouseInfo;
import java.awt.Point;
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
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.example.projetjavafx.utils.ImageUtils;

public class SocialController {
    @FXML
    private VBox createPostForm;

    @FXML
    private void handleAddPostClick() {
        try {
            // Charger la vue de création de post
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/social/PostCreation.fxml"));
            Parent root = loader.load();
            
            // Configurer le contrôleur
            PostController controller = loader.getController();
            controller.setSocialController(this);
            
            // Créer et configurer la nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Créer une publication");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(true);
            
            // Centrer la fenêtre sur l'écran
            stage.centerOnScreen();
            
            // Configurer comme fenêtre modale
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(postsContainer.getScene().getWindow());
            
            // Afficher la fenêtre en mode modal
            stage.showAndWait();
            
        } catch (IOException e) {
            showError("Error", "Could not open post creation window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private FlowPane postsContainer;
    @FXML
    private TextArea postContent;
    @FXML
    private Button followRequestsButton;
    @FXML
    private ImageView selectedImage;
    @FXML
    private TextField userSearchField; // Changed from searchField to match the FXML
    @FXML
    private Label userName;
    @FXML
    private ImageView userProfilePic;
    @FXML
    private ImageView userProfilePic2; // Added for the second profile pic in the create post form
    @FXML
    private VBox postCreationArea; // Zone de création de post dans le feed
    @FXML
    private Button followButton;
    @FXML
    private Label followersCountLabel;
    @FXML
    private Label followingCountLabel;
    @FXML
    private Label profileNameLabel;

    
    // Replace the dropdown controller with the popup
    private UserSearchPopup userSearchPopup;

    private Connection connection;
    private PostDAO postDAO;
    private LikeDAO likeDAO;
    private CommentDAO commentDAO;
    private FollowRequestDAO followRequestDAO;
    private int currentUserId = 1; // Default user ID, should be set from login
    private String selectedImagePath = null;

    public void initialize() {
        // Initialize database connection
        try {
            connection = AivenMySQLManager.getConnection();
            postDAO = new PostDAO(connection);
            likeDAO = new LikeDAO(connection);
            commentDAO = new CommentDAO(connection);
            followRequestDAO = new FollowRequestDAO(connection);

            // Initialize the user search popup
            userSearchPopup = new UserSearchPopup();
            
            // Set current user info from SessionManager
            currentUserId = SessionManager.getInstance().getCurrentUserId();
            
            // Load user profile data from ProfileRepository
            ProfileRepository userProfile = ProfileRepository.getUserProfile(currentUserId);
            if (userProfile != null) {
                userName.setText(userProfile.getUsername());
                
                // Set user profile pic
            String profilePictureData = userProfile.getProfilePicture();
            if (profilePictureData != null && !profilePictureData.isEmpty()) {
                try {
                    Image avatar;
                    // Check if it's Base64 encoded data
                    if (ImageUtils.isValidBase64Image(profilePictureData)) {
                        avatar = ImageUtils.base64ToImage(profilePictureData);
                    } else {
                        // Legacy support for URL-based images
                        avatar = new Image(profilePictureData);
                    }
                    
                    if (avatar != null && !avatar.isError()) {
                        userProfilePic.setImage(avatar);
                        // Check if userProfilePic2 is not null before setting image
                        if (userProfilePic2 != null) {
                            userProfilePic2.setImage(avatar); // Set the same avatar for the second profile pic
                            // Apply circular clip to the second profile picture
                            applyCircularClipToImageView(userProfilePic2);
                        }
                        // Apply circular clip to the first profile picture
                        applyCircularClipToImageView(userProfilePic);
                    } else {
                        loadDefaultProfilePicture();
                    }
                } catch (Exception e) {
                    System.err.println("Error loading profile picture: " + e.getMessage());
                    loadDefaultProfilePicture();
                }
            } else {
                loadDefaultProfilePicture();
            }
            } else {
                userName.setText("User " + currentUserId); // Fallback to user ID if profile not found
                loadDefaultProfilePicture();
            }
            System.out.println("postCreationArea : " + postCreationArea); // Affiche "null" si non injecté
            // Load posts
            loadAllPosts();

            // Add search functionality
            userSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                searchUsers(newValue);
            });
            
            // Add focus listener to hide popup when search field loses focus
            userSearchField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) { // If focus lost
                    // Add a small delay to allow for clicking on search results
                    javafx.application.Platform.runLater(() -> {
                        // Obtain the current mouse position using MouseInfo
                        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
                        double mouseX = mousePosition.getX();
                        double mouseY = mousePosition.getY();
                        // Only hide if the popup doesn't contain the mouse
                        if (!userSearchPopup.isShowing() || !userSearchPopup.contains(mouseX, mouseY)) {
                            userSearchPopup.hide();
                        }
                    });
                }
            });

        } catch (SQLException e) {
            showError("Database Error", "Could not connect to database: " + e.getMessage());
        }
    }
    
    // Helper method to load default profile picture
    @FXML
private void handleSelectImage() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
    );
    
    File selectedFile = fileChooser.showOpenDialog(postsContainer.getScene().getWindow());
    if (selectedFile != null) {
        selectedImagePath = selectedFile.toURI().toString();
        // Here you would add logic to handle the selected image
    }
}

private void loadDefaultProfilePicture() {
        try {
            // Try to load from resources first
            InputStream is = getClass().getResourceAsStream("/com/example/projetjavafx/social/img/userprofile.png");
            if (is != null) {
                Image avatar = new Image(is);
                userProfilePic.setImage(avatar);
                // Check if userProfilePic2 is not null before setting image
                if (userProfilePic2 != null) {
                    userProfilePic2.setImage(avatar); // Set the same avatar for the second profile pic
                    // Apply circular clip to the second profile picture
                    applyCircularClipToImageView(userProfilePic2);
                }
                // Apply circular clip to the first profile picture
                applyCircularClipToImageView(userProfilePic);
            } else {
                // Fallback to a file path if resource not found
                File file = new File("src/main/resources/com/example/projetjavafx/social/img/userprofile.png");
                if (file.exists()) {
                    Image avatar = new Image(file.toURI().toString());
                    userProfilePic.setImage(avatar);
                    // Check if userProfilePic2 is not null before setting image
                    if (userProfilePic2 != null) {
                        userProfilePic2.setImage(avatar);
                        // Apply circular clip to the second profile picture
                        applyCircularClipToImageView(userProfilePic2);
                    }
                    // Apply circular clip to the first profile picture
                    applyCircularClipToImageView(userProfilePic);
                } else {
                    // Use a default image or placeholder if all else fails
                    System.err.println("Avatar image not found, using default");
                    // Create a simple circle as a placeholder
                    userProfilePic.setFitWidth(40);
                    userProfilePic.setFitHeight(40);
                    // Check if userProfilePic2 is not null before setting dimensions
                    if (userProfilePic2 != null) {
                        userProfilePic2.setFitWidth(40);
                        userProfilePic2.setFitHeight(40);
                        // Apply circular clip to the second profile picture
                        applyCircularClipToImageView(userProfilePic2);
                    }
                    // Apply circular clip to the first profile picture
                    applyCircularClipToImageView(userProfilePic);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading avatar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openPostCreationModal() {
        createPostForm.setVisible(true);
        createPostForm.setManaged(true);
        postContent.requestFocus();
    }

    @FXML
    private void cancelPost() {
        // Vérifier quelle zone de création de post est active
        if (createPostForm != null) {
            createPostForm.setVisible(false);
            createPostForm.setManaged(false);
        } else if (postCreationArea != null) {
            postCreationArea.setVisible(false);
            postCreationArea.setManaged(false);
        }
        
        // Nettoyer les champs
        if (postContent != null) {
            postContent.clear();
        }
        if (newPostContent != null) {
            newPostContent.clear();
        }
        if (selectedImage != null) {
            selectedImage.setImage(null);
            selectedImage.setVisible(false);
            selectedImage.setManaged(false);
        }
        selectedImagePath = null;
    }

    @FXML
    private void addImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(postsContainer.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Create images directory if it doesn't exist
                Path imagesDir = Paths.get("src/main/resources/com/example/projetjavafx/img/posts");
                if (!Files.exists(imagesDir)) {
                    Files.createDirectories(imagesDir);
                }

                // Generate unique filename
                String uniqueFileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path destination = imagesDir.resolve(uniqueFileName);

                // Copy file to resources
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                // Set image preview
                Image image = new Image(destination.toUri().toString());
                selectedImage.setImage(image);
                selectedImage.setVisible(true);
                selectedImage.setManaged(true);

                // Store path for database
                selectedImagePath = "/com/example/projetjavafx/img/posts/" + uniqueFileName;

            } catch (IOException e) {
                showError("File Error", "Could not process image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void publishPost() {
        String content = postContent.getText().trim();

        if (content.isEmpty() && selectedImagePath == null) {
            showError("Invalid Post", "Please add text or an image to your post.");
            return;
        }

        try {
            Post post = new Post(currentUserId, content, null, selectedImagePath);
            postDAO.addPost(post);

            // Clear form
            cancelPost();

            // Refresh posts
            refreshPosts();

        } catch (SQLException e) {
            showError("Database Error", "Could not publish post: " + e.getMessage());
        }
    }

    public void addPostToFeed(Post post) {
        try {
            URL fxmlLocation = SocialController.class.getResource("/com/example/projetjavafx/social/PostItem.fxml");
            if (fxmlLocation == null) {
                System.err.println("Erreur : PostItem.fxml non trouvé à /com/example/projetjavafx/social/PostItem.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            javafx.scene.Node postNode = loader.load();
            postsContainer.getChildren().add(0, postNode);

            PostItemController controller = loader.getController();
            // Pass this SocialController as the parent controller
            controller.setData(post, connection, currentUserId, this); // Pass this SocialController
            controller.setParentController(null); // Set to null since we're using SocialController directly
            controller.setPostId(post.getPostId()); // Set the post ID
            postNode.getProperties().put("controller", controller);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de PostItem.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadAllPosts() {
        try {
            // Reset mode
            isUserPostsMode = false;
            currentViewedUserId = -1;
            currentViewedUsername = "";
            
            // Show post creation area
            if (postCreationArea != null) {
                postCreationArea.setVisible(true);
                postCreationArea.setManaged(true);
            } else {
                System.err.println("Erreur : postCreationArea est null !");
            }
            
            // Load all posts
            List<Post> posts = postDAO.getAllPosts();
            postsContainer.getChildren().clear();

            for (Post post : posts) {
                addPostToFeed(post);
            }
        } catch (SQLException e) {
            showError("Database Error", "Could not load posts: " + e.getMessage());
        }
    }

    /**
     * Search for users and groups based on the query string
     * @param query The search query
     */
    private void searchUsers(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                // If query is empty, hide the popup
                userSearchPopup.hide();
            } else {
                // Use UserSearchRepository to search for users
                UserSearchRepository userSearchRepo = new UserSearchRepository(connection);
                List<ProfileRepository> users = userSearchRepo.searchUsers(query);
                
                // Use GroupSearchRepository to search for groups
                GroupSearchRepository groupSearchRepo = new GroupSearchRepository(connection);
                List<GroupProfileRepository> groups = groupSearchRepo.searchGroups(query);
                
                // Update the popup with combined search results
                userSearchPopup.updateCombinedResults(users, groups);
                
                // Show the popup below the search field
                userSearchPopup.showBelow(userSearchField);
                
                System.out.println("Updated search results with " + users.size() + " users and " + groups.size() + " groups");
            }
        } catch (SQLException e) {
            showError("Search Error", "Could not search users or groups: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error in searchUsers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Adds a user search result to the feed
     * @param profile The user profile to display
     */
    private void addUserToSearchResults(ProfileRepository profile) {
        try {
            URL fxmlLocation = SocialController.class.getResource("/com/example/projetjavafx/social/UserSearchResult.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: UserSearchResult.fxml not found");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            javafx.scene.Node userNode = loader.load();
            postsContainer.getChildren().add(userNode);
            
            UserSearchResultController controller = loader.getController();
            controller.setData(profile);
        } catch (IOException e) {
            System.err.println("Error loading UserSearchResult.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Variables pour le mode d'affichage des posts
    private boolean isUserPostsMode = false;
    private int currentViewedUserId = -1;
    private String currentViewedUsername = "";
    
    /**
     * Loads posts from a specific user
     * @param userId The ID of the user
     * @param username The username to display
     */
    public void loadUserPosts(int userId, String username) {
        try {
            // Set user posts mode
            isUserPostsMode = true;
            currentViewedUserId = userId;
            currentViewedUsername = username;
            
            // Update UI to show we're viewing a specific user's posts
            updateUIForUserPostsMode();
            
            // Load the user's posts
            List<Post> posts = postDAO.getPostsByUserId(userId);
            postsContainer.getChildren().clear();
            
            // Add posts
            for (Post post : posts) {
                addPostToFeed(post);
            }
            
            if (posts.isEmpty()) {
                Label noPostsLabel = new Label(username + " hasn't posted anything yet.");
                noPostsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #65676B; -fx-padding: 20;");
                postsContainer.getChildren().add(noPostsLabel);
            }
        } catch (SQLException e) {
            showError("Database Error", "Could not load user posts: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to find a node by its ID in the scene graph
     * @param id The ID of the node to find
     * @return The node if found, null otherwise
     */
    public javafx.scene.Node lookup(String id) {
        if (postsContainer == null || postsContainer.getScene() == null) {
            System.err.println("Warning: Scene is not available yet for lookup of " + id);
            return null;
        }
        return postsContainer.getScene().lookup(id);
    }
    
    /**
     * Gets the posts container
     * @return The posts container FlowPane
     */
    public FlowPane getPostsContainer() {
        return postsContainer;
    }
    
    /**
     * Public method to refresh profile information (counters and button state)
     * Can be called from other controllers when follow status changes
     */
    public void refreshProfileInfo() {
        if (isUserPostsMode && currentViewedUserId > 0) {
            updateProfileInfo();
        }
    }
    
    /**
     * Handles the follow button click
     */
    @FXML
    public void handleFollowButton() {
        if (currentViewedUserId <= 0) {
            System.out.println("Cannot follow: No user selected");
            return;
        }
        
        // Check if user is trying to follow themselves
        if (currentViewedUserId == currentUserId) {
            showError("Error", "You cannot follow yourself!");
            return;
        }
        
        // Check if already following - if so, unfollow
        if (followRequestDAO.isFollowing(currentUserId, currentViewedUserId)) {
            handleUnfollow();
            return;
        }
        
        // Check if request already exists
        if (followRequestDAO.requestExists(currentUserId, currentViewedUserId)) {
            showInfo("Request Pending", "You have already sent a follow request to " + currentViewedUsername);
            return;
        }
        
        // Send follow request
        if (followRequestDAO.sendFollowRequest(currentUserId, currentViewedUserId)) {
            showSuccess("Request Sent", "Follow request sent to " + currentViewedUsername + "!\nThey will be notified and can accept or reject your request.");
            System.out.println("Follow request sent to user: " + currentViewedUsername + " (ID: " + currentViewedUserId + ")");
            
            // Update the follow button state to show "Pending"
            updateFollowButtonState();
        } else {
            showError("Error", "Could not send follow request. Please try again.");
        }
    }
    
    /**
     * Handles unfollowing a user
     */
    private void handleUnfollow() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Unfollow User");
        confirmAlert.setHeaderText("Unfollow " + currentViewedUsername + "?");
        confirmAlert.setContentText("Are you sure you want to unfollow this user?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (followRequestDAO.unfollowUser(currentUserId, currentViewedUserId)) {
                showSuccess("Unfollowed", "You have unfollowed " + currentViewedUsername);
                
                // Update the UI
                updateFollowCounts();
                updateFollowButtonState();
            } else {
                showError("Error", "Could not unfollow user. Please try again.");
            }
        }
    }
    
    /**
     * Handles the message button click
     * Opens the messaging page to chat with the selected user
     */
    @FXML
    public void handleMessageButton() {
        if (currentViewedUserId <= 0) {
            System.out.println("Cannot message: No user selected");
            return;
        }
        
        System.out.println("Opening chat with user: " + currentViewedUsername + " (ID: " + currentViewedUserId + ")");
        
        try {
            // Charger la vue de messagerie
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/messagerie/discussion.fxml"));
            Parent root = loader.load();
            
            // Récupérer le contrôleur de messagerie
            com.example.projetjavafx.root.messagerie.client.ChatController chatController = loader.getController();
            
            // Initialiser le client de messagerie
            chatController.initializeClient();
            
            // Créer une nouvelle scène
            Scene scene = new Scene(root);
            
            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) postsContainer.getScene().getWindow();
            
            // Définir la nouvelle scène
            stage.setScene(scene);
            stage.setTitle("Discussion avec " + currentViewedUsername);
            stage.show();
            
            // Définir l'utilisateur cible dans le ChatController
            chatController.setTargetUser(currentViewedUserId, currentViewedUsername);
            
            // Ajouter un délai pour permettre l'initialisation complète du chat
            javafx.application.Platform.runLater(() -> {
                System.out.println("Chat initialisé avec l'utilisateur " + currentViewedUsername);
            });
            
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture de la messagerie: " + e.getMessage());
            e.printStackTrace();
            
            // Afficher une alerte en cas d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'ouvrir la messagerie: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Updates the UI to reflect that we're in user posts mode
     */
    private void updateUIForUserPostsMode() {
        if (postCreationArea != null) {
            if (currentViewedUserId != currentUserId) {
                postCreationArea.setVisible(false);
                postCreationArea.setManaged(false);
            } else {
                postCreationArea.setVisible(true);
                postCreationArea.setManaged(true);
            }
        } else {
            System.err.println("Erreur : postCreationArea est null dans updateUIForUserPostsMode !");
        }
        
        // Update profile information and counters
        updateProfileInfo();
    }
    
    /**
     * Updates the profile information including counters and follow button
     */
    private void updateProfileInfo() {
        if (currentViewedUserId <= 0) return;
        
        try {
            // Update profile name
            if (profileNameLabel != null) {
                profileNameLabel.setText(currentViewedUsername);
            }
            
            // Update followers and following counts
            updateFollowCounts();
            
            // Update follow button state
            updateFollowButtonState();
            
        } catch (Exception e) {
            System.err.println("Error updating profile info: " + e.getMessage());
        }
    }
    
    /**
     * Updates the followers and following counts
     */
    private void updateFollowCounts() {
        if (followRequestDAO == null) return;
        
        try {
            int followersCount = followRequestDAO.getFollowersCount(currentViewedUserId);
            int followingCount = followRequestDAO.getFollowingCount(currentViewedUserId);
            
            if (followersCountLabel != null) {
                followersCountLabel.setText("Followers: " + followersCount);
            }
            
            if (followingCountLabel != null) {
                followingCountLabel.setText("Following: " + followingCount);
            }
            
        } catch (Exception e) {
            System.err.println("Error updating follow counts: " + e.getMessage());
        }
    }
    
    /**
     * Updates the follow button state based on current relationship
     */
    private void updateFollowButtonState() {
        if (followButton == null || followRequestDAO == null) return;
        
        try {
            // Hide follow button if viewing own profile
            if (currentViewedUserId == currentUserId) {
                followButton.setVisible(false);
                followButton.setManaged(false);
                return;
            }
            
            followButton.setVisible(true);
            followButton.setManaged(true);
            
            // Check if already following
            if (followRequestDAO.isFollowing(currentUserId, currentViewedUserId)) {
                followButton.setText("Following");
                followButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 4;");
            } else if (followRequestDAO.requestExists(currentUserId, currentViewedUserId)) {
                followButton.setText("Pending");
                followButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-background-radius: 4;");
            } else {
                followButton.setText("Follow");
                followButton.setStyle("-fx-background-color: #1c2b5d; -fx-text-fill: white; -fx-background-radius: 4;");
            }
            
        } catch (Exception e) {
            System.err.println("Error updating follow button state: " + e.getMessage());
        }
    }
    /**
     * Resets the UI to normal mode and loads all posts
     */
    public void resetAndLoadAllPosts() {
        loadAllPosts();
    }
    
    private void searchPosts(String query) {
        try {
            List<Post> posts;
            if (query == null || query.trim().isEmpty()) {
                posts = postDAO.getAllPosts();
            } else {
                posts = postDAO.searchPosts(query);
            }

            postsContainer.getChildren().clear();
            for (Post post : posts) {
                addPostToFeed(post);
            }
        } catch (SQLException e) {
            showError("Search Error", "Could not search posts: " + e.getMessage());
        }
    }

    public void refreshPosts() {
        loadAllPosts();
    }

    public void refreshPostEngagement(int postId) {
        // Implementation that's more targeted than refreshing all posts
        try {
            // Find the specific post in the container and refresh just that one
            for (int i = 0; i < postsContainer.getChildren().size(); i++) {
                javafx.scene.Node node = postsContainer.getChildren().get(i);
                if (node.getProperties().containsKey("controller")) {
                    PostItemController controller = (PostItemController) node.getProperties().get("controller");
                    if (controller.getPost().getPostId() == postId) {
                        // Refresh just this post
                        controller.refreshComments();
                        controller.refreshLikeStatus();
                        return;
                    }
                }
            }
            // If post not found, refresh all
            refreshPosts();
        } catch (Exception e) {
            System.err.println("Error in refreshPostEngagement: " + e.getMessage());
            // Fallback to refreshing all posts
            refreshPosts();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to set current user ID (called from login)
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;

        // Update user info with profile data
        ProfileRepository userProfile = ProfileRepository.getUserProfile(currentUserId);
        if (userProfile != null) {
            userName.setText(userProfile.getUsername());
            
            // Set user profile pic
            String profilePictureData = userProfile.getProfilePicture();
            if (profilePictureData != null && !profilePictureData.isEmpty()) {
                try {
                    Image avatar;
                    // Check if it's Base64 encoded data
                    if (ImageUtils.isValidBase64Image(profilePictureData)) {
                        avatar = ImageUtils.base64ToImage(profilePictureData);
                    } else {
                        // Legacy support for URL-based images
                        avatar = new Image(profilePictureData);
                    }
                    
                    if (avatar != null && !avatar.isError()) {
                        userProfilePic.setImage(avatar);
                        if (userProfilePic2 != null) {
                            userProfilePic2.setImage(avatar); // Set the same avatar for the second profile pic
                        }
                    } else {
                        loadDefaultProfilePicture();
                    }
                } catch (Exception e) {
                    System.err.println("Error loading profile picture: " + e.getMessage());
                    loadDefaultProfilePicture();
                }
            } else {
                loadDefaultProfilePicture();
            }
        } else {
            userName.setText("User " + currentUserId); // Fallback to user ID if profile not found
            loadDefaultProfilePicture();
        }

        // Refresh posts to update like/comment status
        refreshPosts();
    }

    public void deletePost(int postId) {
        try {
            Post post = postDAO.getPostById(postId); // Assuming you have a method to fetch a post by its ID
            if (post.getUserId() == currentUserId) {
                postDAO.deletePost(postId);
                refreshPosts();
            } else {
                showError("Permission Denied", "You can only delete your own posts.");
            }
        } catch (SQLException e) {
            showError("Database Error", "Could not delete post: " + e.getMessage());
        }
    }

    public void editPost(int postId, String newContent, String newImagePath) {
        try {
            Post post = postDAO.getPostById(postId); // Assuming you have a method to fetch a post by its ID
            if (post.getUserId() == currentUserId) {
                postDAO.updatePost(postId, newContent, newImagePath);
                refreshPosts();
            } else {
                showError("Permission Denied", "You can only edit your own posts.");
            }
        } catch (SQLException e) {
            showError("Database Error", "Could not edit post: " + e.getMessage());
        }
    }

    // Helper method to apply circular clip to ImageView
    private void applyCircularClipToImageView(ImageView imageView) {
        if (imageView == null) return;
        
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(
            imageView.getFitWidth() / 2,
            imageView.getFitHeight() / 2,
            Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2
        );
        imageView.setClip(clip);
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

    public void onChatbotClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/chatbot/chatbot.fxml", event);
    }

    @FXML
    protected void onOrganizerButtonClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
            loadView("/com/example/projetjavafx/organizer/organizer-view.fxml", event);}
    }

    @FXML
    protected void onEventsClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/events/events-view.fxml", event);
    }

    @FXML
    protected void onProfileClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
            loadView("/com/example/projetjavafx/profile/profile-view.fxml", event);}
    }

    @FXML
    protected void onGroupButtonClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
            loadView("/com/example/projetjavafx/group/group-profile-view.fxml", event);}
    }


    @FXML
    protected void onCreateJobClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
            loadView("/com/example/projetjavafx/organizer/create-job-offer-view.fxml", event);}
    }

    public void onDachboardClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/organizer-view.fxml", event);
    }

    @FXML
    protected void onAnalyticsClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
            loadView("/com/example/projetjavafx/organizer/analytics-view.fxml", event);}
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
    @FXML
    public void onJobFeedClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){
            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
            loadView("/com/example/projetjavafx/jobfeed/job-feed-view.fxml", event);}
    }
    @FXML
    public void onPointClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){
            loadView("/com/example/projetjavafx/auth/login-view.fxml", event);
        }else {
            loadView("/com/example/projetjavafx/points/Home.fxml", event);}
    }

    // Method to handle logout and redirect to the login page
    @FXML
    public void onLogout(ActionEvent event) {
        System.out.println("logout");
        SessionManager.getInstance().logout();
        // Redirect to login page
        loadView("/com/example/projetjavafx/auth/login-view.fxml", event);
    }


@FXML
private TextArea newPostContent;

@FXML
private void handleCreatePost() {
    String content = newPostContent.getText().trim();
    if (!content.isEmpty() || selectedImagePath != null) {
        try {
            Post post = new Post(currentUserId, content, null, selectedImagePath);
            postDAO.addPost(post);
            
            // Clear form
            newPostContent.clear();
            selectedImagePath = null;
            
            // Refresh posts
            refreshPosts();
            
        } catch (SQLException e) {
            showError("Database Error", "Could not publish post: " + e.getMessage());
        }
    }
}

    @FXML
    protected void onMessagerieClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){
            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
            loadView("/com/example/projetjavafx/messagerie/discussion.fxml", event);
        }
    }
    
    @FXML
    protected void onFollowRequestsClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId() == -1) {
            loadView("/com/example/projetjavafx/auth/login-view.fxml", event);
        } else {
            loadView("/com/example/projetjavafx/social/FollowRequests.fxml", event);
        }
    }
/**
 * Enregistre un post dans la base de données
 * @param post Le post à enregistrer
 * @throws SQLException Si une erreur survient lors de l'enregistrement
 */
public void savePostToDatabase(Post post) throws SQLException {
    if (postDAO == null) {
        throw new SQLException("PostDAO n'est pas initialisé");
    }
    postDAO.addPost(post);
}

    // Utility methods for showing alerts
    /*private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }*/
    
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