package com.example.projetjavafx.root.profile;

import com.example.projetjavafx.root.auth.SessionManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import com.example.projetjavafx.utils.ImageUtils;

public class EditProfileController {
    @FXML private TextField username;
    @FXML private TextField email;
    @FXML private TextField bio;
    @FXML private TextField profilePicture;
    @FXML private ImageView profileImageView;
    private String profilePictureBase64; // Store Base64 encoded image
    @FXML private Button save;
    @FXML private Button cancel;
    @FXML private FlowPane editProfileContainer;

    private String imageUrl; // To store the selected image URL

    @FXML
    public void initialize() {
        loadUserData();
        // Apply a circular clip to the ImageView
        double radius = Math.min(profileImageView.getFitWidth(), profileImageView.getFitHeight()) / 2;
        Circle clip = new Circle(radius, radius, radius);
        profileImageView.setClip(clip);
    }

    private void loadUserData() {
        int currentUserId = SessionManager.getInstance().getCurrentUserId();
        EditProfileRepository userProfile = EditProfileRepository.loadUserData(currentUserId);
        if (userProfile != null) {
            username.setText(userProfile.getUsername());
            email.setText(userProfile.getEmail());
            bio.setText(userProfile.getBio());
            String profilePictureData = userProfile.getProfilePicture();
            profilePictureBase64 = profilePictureData; // Store the Base64 data
            
            if (profilePictureData != null && !profilePictureData.isEmpty()) {
                try {
                    Image img;
                    // Check if it's Base64 encoded data
                    if (ImageUtils.isValidBase64Image(profilePictureData)) {
                        img = ImageUtils.base64ToImage(profilePictureData);
                        profilePicture.setText("[Image Base64 Data]"); // Display placeholder text
                    } else {
                        // Legacy support for URL-based images
                        if (profilePictureData.startsWith("file:") || profilePictureData.startsWith("http")) {
                            img = new Image(profilePictureData);
                        } else {
                            String imagePath = "/com/example/projetjavafx/images/" + profilePictureData;
                            InputStream is = getClass().getResourceAsStream(imagePath);
                            if (is == null) {
                                System.err.println("Ressource image introuvable dans le classpath: " + imagePath);
                            }
                            img = new Image(is);
                        }
                        profilePicture.setText(profilePictureData);
                    }
                    profileImageView.setImage(img);
                } catch (Exception e) {
                   // System.err.println("Exception lors du chargement de l'image : " + imageName);
                    e.printStackTrace();
                }
            }


        }
    }

    @FXML
    private void handleSave() {
        String newUsername = username.getText();
        String newEmail = email.getText();
        String newBio = bio.getText();
        // Use Base64 data if available, otherwise use text field content
        String newProfilePicture = (profilePictureBase64 != null && !profilePictureBase64.isEmpty()) 
                                  ? profilePictureBase64 
                                  : profilePicture.getText();
        int userId = SessionManager.getInstance().getCurrentUserId();

        EditProfileRepository repository = new EditProfileRepository(null, null, null, null);
        boolean success = repository.updateProfile(userId, newUsername, newEmail, newBio, newProfilePicture);

        if (success) {
            System.out.println("Profile updated successfully!");
            // After saving, navigate to the profile view
            loadView("/com/example/projetjavafx/profile/profile-view.fxml");
        } else {
            System.err.println("No rows updated. Check the user ID and data.");
        }
    }

    @FXML
    private void handleChangePhoto() {
        if (profileImageView == null) {
            System.err.println("Error: profileImageView has not been initialized.");
            return;
        }
        
        File selectedFile = ImageUtils.chooseImageFile(profileImageView.getScene().getWindow(), "Choose a profile picture");
        if (selectedFile != null) {
            try {
                // Convert image to Base64
                profilePictureBase64 = ImageUtils.imageToBase64(selectedFile);
                
                // Display the image
                Image image = ImageUtils.base64ToImage(profilePictureBase64);
                profileImageView.setImage(image);
                
                // Update text field to show that an image is selected
                profilePicture.setText("[Image Base64 Data - " + selectedFile.getName() + "]");
                
                // Apply circular clip to the new image
                double radius = Math.min(profileImageView.getFitWidth(), profileImageView.getFitHeight()) / 2;
                Circle clip = new Circle(radius, radius, radius);
                profileImageView.setClip(clip);
                
                System.out.println("Image successfully converted to Base64 and loaded");
            } catch (Exception e) {
                System.err.println("Error converting image to Base64: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Common navigation method wrapped in Platform.runLater()
    private void loadView(String fxmlPath) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                Stage stage = (Stage) (editProfileContainer != null && editProfileContainer.getScene() != null
                        ? editProfileContainer.getScene().getWindow() : username.getScene().getWindow());
                if (stage != null) {
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    System.err.println("Stage is null, cannot load view.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Navigation methods

    // New navigation methods
    @FXML
    public void onOrganizerButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/organizer-view.fxml");
    }

    @FXML
    public void onEventsClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/events/events-view.fxml");
    }

    @FXML
    public void onGroupButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/group/group-profile-view.fxml");
    }

    @FXML
    public void onJobFeedClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/jobfeed/job-feed-view.fxml");
    }

    @FXML
    public void onCreateJobClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/create-job-offer-view.fxml");
    }

    @FXML
    public void onProfileClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/profile/profile-view.fxml");
    }

    @FXML
    public void onsocialButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/social/Feed.fxml");
    }

    @FXML
    public void onMessagerieClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/messagerie/discussion.fxml");
    }

    @FXML
    public void onPointClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/points/Home.fxml");
    }

    @FXML
    public void onChatbotClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/chatbot/chatbot-view.fxml");
    }

    public void handleHomeButton(ActionEvent event) {
        loadView("/com/example/projetjavafx/root/root-view.fxml");
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // Instead of closing the stage, load the profile view
        loadView("/com/example/projetjavafx/profile/profile-view.fxml");
    }
}
