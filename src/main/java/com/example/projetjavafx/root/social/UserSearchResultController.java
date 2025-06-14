package com.example.projetjavafx.root.social;

import com.example.projetjavafx.root.profile.ProfileRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import com.example.projetjavafx.utils.ImageUtils;

public class UserSearchResultController {
    @FXML
    private HBox userContainer;
    @FXML
    private ImageView userProfileImage;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label emailLabel;
    
    private ProfileRepository profile;
    
    public void setData(ProfileRepository profile) {
        this.profile = profile;
        
        // Set username and email
        usernameLabel.setText(profile.getUsername());
        emailLabel.setText(profile.getEmail());
        
        // Set profile image
        String profilePictureData = profile.getProfilePicture();
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
                    userProfileImage.setImage(avatar);
                    applyCircularClipToImageView(userProfileImage);
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
    }
    
    // Helper method to load default profile picture
    private void loadDefaultProfilePicture() {
        try {
            // Try to load from resources first
            InputStream is = getClass().getResourceAsStream("/com/example/projetjavafx/social/img/userprofile.png");
            if (is != null) {
                Image avatar = new Image(is);
                userProfileImage.setImage(avatar);
                applyCircularClipToImageView(userProfileImage);
            } else {
                // Fallback to a file path if resource not found
                userProfileImage.setFitWidth(40);
                userProfileImage.setFitHeight(40);
                applyCircularClipToImageView(userProfileImage);
            }
        } catch (Exception e) {
            System.err.println("Error loading avatar: " + e.getMessage());
            e.printStackTrace();
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
    
    @FXML
    private void onUserClicked() {
        // This method will be called when a user result is clicked
        System.out.println("User clicked: " + profile.getUsername());
        
        try {
            // Get the SocialController instance with UserProfile.fxml
            // The file exists at /com/example/projetjavafx/social/UserProfile.fxml
            URL fxmlLocation = SocialController.class.getResource("/com/example/projetjavafx/social/UserProfile.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: UserProfile.fxml not found at /com/example/projetjavafx/social/UserProfile.fxml");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            SocialController socialController = loader.getController();
            
            // Vérifier que l'ID utilisateur est valide
            int userId = profile.getId();
            if (userId <= 0) {
                System.err.println("Invalid user ID: " + userId + " for user: " + profile.getUsername());
                return;
            }
            
            // Load the user's posts
            socialController.loadUserPosts(userId, profile.getUsername());
            
            // Set user profile details in the header
            setupUserProfileHeader(socialController, profile);
            
            // Get the current window
            javafx.stage.Window window = userContainer.getScene().getWindow();
            
            if (window instanceof javafx.stage.Popup) {
                // If it's a Popup (UserSearchPopup), hide it
                javafx.stage.Popup popup = (javafx.stage.Popup) window;
                popup.hide();
                
                // Create a new Stage to display the user's feed
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.setTitle("Profile: " + profile.getUsername());
                newStage.show();
            } else if (window instanceof Stage) {
                // If it's already a Stage, update its scene
                Stage stage = (Stage) window;
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (Exception e) {
            System.err.println("Error navigating to user profile: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sets up the user profile header with user details
     * @param socialController The SocialController instance
     * @param profile The user profile to display
     */
    private void setupUserProfileHeader(SocialController socialController, ProfileRepository profile) {
        try {
            // Utiliser Platform.runLater pour s'assurer que la scène est complètement chargée
            javafx.application.Platform.runLater(() -> {
                try {
                    // Get references to the UI elements in the UserProfile.fxml
                    ImageView profileImage = (ImageView) socialController.lookup("#profileImage");
                    Label profileNameLabel = (Label) socialController.lookup("#profileNameLabel");
                    Label profileEmailLabel = (Label) socialController.lookup("#profileEmailLabel");
                    Label profileBioLabel = (Label) socialController.lookup("#profileBioLabel");
                    Label postCountLabel = (Label) socialController.lookup("#postCountLabel");
                    VBox postCreationArea = (VBox) socialController.lookup("#postCreationArea");
                    
                    // Set user details
                    if (profileNameLabel != null) profileNameLabel.setText(profile.getUsername());
                    if (profileEmailLabel != null) profileEmailLabel.setText(profile.getEmail());
                    if (profileBioLabel != null) profileBioLabel.setText(profile.getBio() != null ? profile.getBio() : "No bio available");
                    
                    // Set post count (we'll need to count the posts from the database)
                    if (postCountLabel != null) {
                        try {
                            // Get post count from the database
                            java.sql.Connection conn = new com.example.projetjavafx.root.DbConnection.AivenMySQLManager().getConnection();
                            PostDAO postDAO = new PostDAO(conn);
                            java.util.List<Post> userPosts = postDAO.getPostsByUserId(profile.getId());
                            postCountLabel.setText("Posts: " + userPosts.size());
                        } catch (Exception e) {
                            System.err.println("Error getting post count: " + e.getMessage());
                            postCountLabel.setText("Posts: 0");
                        }
                    }
                    
                    // Gérer la visibilité du formulaire de création de post
                    // Afficher uniquement si l'utilisateur consulte son propre profil
                    if (postCreationArea != null) {
                        int currentUserId = com.example.projetjavafx.root.auth.SessionManager.getInstance().getCurrentUserId();
                        boolean isOwnProfile = profile.getId() == currentUserId;
                        postCreationArea.setVisible(isOwnProfile);
                        postCreationArea.setManaged(isOwnProfile);
                        System.out.println("Post creation area visibility set to: " + isOwnProfile + " for user ID: " + profile.getId());
                    }
                    
                    // Set profile image
                    if (profileImage != null) {
                        String profilePictureData = profile.getProfilePicture();
                        if (profilePictureData != null && !profilePictureData.isEmpty()) {
                            try {
                                javafx.scene.image.Image avatar;
                                // Check if it's Base64 encoded data
                                if (ImageUtils.isValidBase64Image(profilePictureData)) {
                                    avatar = ImageUtils.base64ToImage(profilePictureData);
                                } else {
                                    // Legacy support for URL-based images
                                    avatar = new javafx.scene.image.Image(profilePictureData);
                                }
                                
                                if (avatar != null && !avatar.isError()) {
                                    profileImage.setImage(avatar);
                                    
                                    // Apply circular clip to the image
                                    javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(
                                        profileImage.getFitWidth() / 2,
                                        profileImage.getFitHeight() / 2,
                                        Math.min(profileImage.getFitWidth(), profileImage.getFitHeight()) / 2
                                    );
                                    profileImage.setClip(clip);
                                } else {
                                    loadDefaultProfileImage(profileImage);
                                }
                            } catch (Exception e) {
                                System.err.println("Error loading profile picture: " + e.getMessage());
                                loadDefaultProfileImage(profileImage);
                            }
                        } else {
                            loadDefaultProfileImage(profileImage);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error setting up user profile header: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Error in setupUserProfileHeader: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads a default profile image for the user profile header
     * @param imageView The ImageView to set the default image to
     */
    private void loadDefaultProfileImage(ImageView imageView) {
        try {
            // Try to load from resources first
            InputStream is = getClass().getResourceAsStream("/com/example/projetjavafx/social/img/userprofile.png");
            if (is != null) {
                Image avatar = new Image(is);
                imageView.setImage(avatar);
                
                // Apply circular clip
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(
                    imageView.getFitWidth() / 2,
                    imageView.getFitHeight() / 2,
                    Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2
                );
                imageView.setClip(clip);
            } else {
                // Fallback to a file path if resource not found
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                
                // Apply circular clip
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(
                    imageView.getFitWidth() / 2,
                    imageView.getFitHeight() / 2,
                    Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2
                );
                imageView.setClip(clip);
            }
        } catch (Exception e) {
            System.err.println("Error loading default profile image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}