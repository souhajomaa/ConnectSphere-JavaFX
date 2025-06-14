package com.example.projetjavafx.root.profile;

import com.example.projetjavafx.root.auth.SessionManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.InputStream;
import com.example.projetjavafx.utils.ImageUtils;

public class ProfileController {
    @FXML private Label name;
    @FXML private Label mail;
    @FXML private Label bio;
    @FXML private ImageView profileImageView;
    @FXML private Button buttonconfirmpass;
    @FXML private FlowPane profileContainer;

    public void initialize() {
        loadUserData();
        // Apply a circular clip to the ImageView
        double radius = Math.min(profileImageView.getFitWidth(), profileImageView.getFitHeight()) / 2;
        Circle clip = new Circle(radius, radius, radius);
        profileImageView.setClip(clip);
    }

    private void loadUserData() {
        int currentUserId = SessionManager.getInstance().getCurrentUserId();
        ProfileRepository userProfile = ProfileRepository.getUserProfile(currentUserId);
        if (userProfile != null) {
            name.setText(userProfile.getUsername());
            mail.setText(userProfile.getEmail());
            bio.setText(userProfile.getBio());
            String profilePictureData = userProfile.getProfilePicture();
            if (profilePictureData != null && !profilePictureData.isEmpty()) {
                try {
                    Image img;
                    // Check if it's Base64 encoded data
                    if (ImageUtils.isValidBase64Image(profilePictureData)) {
                        img = ImageUtils.base64ToImage(profilePictureData);
                    } else {
                        // Legacy support for URL-based images
                        img = new Image(profilePictureData);
                    }
                    
                    if (img != null && !img.isError()) {
                        profileImageView.setImage(img);
                    } else {
                        // Load default image if conversion fails
                        Image defaultImg = ImageUtils.getDefaultProfileImage();
                        if (defaultImg != null) {
                            profileImageView.setImage(defaultImg);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error loading profile picture: " + e.getMessage());
                    // Load default image on error
                    Image defaultImg = ImageUtils.getDefaultProfileImage();
                    if (defaultImg != null) {
                        profileImageView.setImage(defaultImg);
                    }
                }
            } else {
                // Load default image if no profile picture is set
                Image defaultImg = ImageUtils.getDefaultProfileImage();
                if (defaultImg != null) {
                    profileImageView.setImage(defaultImg);
                }
            }
        }
    }

    @FXML
    private void goToEditProfile() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/profile/edit-profile-view.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) buttonconfirmpass.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error while loading edit-profile-view.fxml: " + e.getMessage());
            }
        });
    }

    // Common navigation method wrapped in Platform.runLater()
    private void loadView(String fxmlPath) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                Stage stage = (Stage) (profileContainer != null && profileContainer.getScene() != null
                        ? profileContainer.getScene().getWindow() : buttonconfirmpass.getScene().getWindow());
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

    // Navigation methods (using ActionEvent)
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



    // Modified onCreateEvent method similar to onNewEventClick in EventsController
    @FXML
    public void onCreateEvent(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId() == -1) {
            loadView("/com/example/projetjavafx/auth/login-view.fxml");
        } else {

            loadView("/com/example/projetjavafx/events/create-events.fxml");
        }
    }


}
