package com.example.projetjavafx.root.group;

import com.example.projetjavafx.root.auth.SessionManager;
import com.example.projetjavafx.root.group.GroupAddRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

public class GroupAddController {
    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ImageView imagePreview;

    private String imageBase64; // Pour stocker l'image encodée en Base64

    private GroupAddRepository groupModel = new GroupAddRepository();

    @FXML
    private void initialize() {
        // Initialisation si nécessaire
    }

    @FXML
    private void onUploadImageClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Convertir l'image en Base64
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                imageBase64 = Base64.getEncoder().encodeToString(imageBytes);

                // Afficher l'image dans l'ImageView
                Image image = new Image(new FileInputStream(selectedFile));
                imagePreview.setImage(image);

                fileInputStream.close();
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Unable to load the image.");
            }
        }
    }
    // Helper method for navigation
    private void loadView(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void onCancelClick(ActionEvent event) {
        // Retourner à la page précédente
        loadView("/com/example/projetjavafx/group/group-add-view.fxml", event);
    }

    @FXML
    private void onSaveGroupClick(ActionEvent event) {
        String name = nameField.getText();
        String description = descriptionField.getText();

        // Validation des champs
        if (name.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Error", "Please fill in all fields.");
            return;
        }

        if (imageBase64 == null || imageBase64.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Error", "Please upload an image.");
            return;
        }

        // Confirmation avant de sauvegarder
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to create this group?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Récupérer l'ID de l'utilisateur connecté
            int userId = SessionManager.getInstance().getCurrentUserId();

            // Définir le rôle de l'utilisateur
            String role = "admin"; // Par défaut, l'utilisateur connecté est admin

            // Créer le groupe et ajouter l'utilisateur avec le rôle spécifié
            if (groupModel.saveGroup(name, description, imageBase64, userId, role)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Group created successfully!");
                loadView ("/com/example/projetjavafx/group/group-profile-view.fxml", event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Unable to create the group.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }






    // New navigation methods
    @FXML
    public void onOrganizerButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/organizer-view.fxml",event);
    }

    @FXML
    public void onEventsClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/events/events-view.fxml",event);
    }

    @FXML
    public void onGroupButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/group/group-profile-view.fxml",event);
    }

    @FXML
    public void onJobFeedClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/jobfeed/job-feed-view.fxml",event);
    }

    @FXML
    public void onCreateJobClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/create-job-offer-view.fxml",event);
    }

    @FXML
    public void onProfileClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/profile/profile-view.fxml",event);
    }

    @FXML
    public void onsocialButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/social/Feed.fxml",event);
    }

    @FXML
    public void onMessagerieClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/messagerie/discussion.fxml",event);
    }

    @FXML
    public void onPointClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/points/Home.fxml",event);
    }

    @FXML
    public void onChatbotClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/chatbot/chatbot-view.fxml",event);
    }

    public void handleHomeButton(ActionEvent event) {
        loadView("/com/example/projetjavafx/root/root-view.fxml",event);
    }
}
