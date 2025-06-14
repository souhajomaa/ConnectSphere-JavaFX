package com.example.projetjavafx.root.social;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.auth.SessionManager;
import com.example.projetjavafx.root.profile.ProfileRepository;
import com.example.projetjavafx.utils.ImageUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PostController {

    // Références FXML
    @FXML private TextArea postContent;
    @FXML private Button publishButton;
    @FXML private ImageView mediaPreview;
    @FXML private StackPane mediaPreviewContainer;
    @FXML private ComboBox<String> privacyComboBox;

    private SocialController socialController;
    private File selectedFile;

    public void setSocialController(SocialController controller) {
        this.socialController = controller;
    }

    @FXML
    public void initialize() {
        configurePrivacyOptions();
        setupDragAndDrop();
        publishButton.setDisable(true);

        postContent.textProperty().addListener((obs, oldVal, newVal) ->
                checkPublishButtonState()
        );
    }

    private void configurePrivacyOptions() {
        privacyComboBox.getItems().addAll("Public");
        privacyComboBox.setValue("Public");
    }

    private void setupDragAndDrop() {
        mediaPreviewContainer.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        mediaPreviewContainer.setOnDragDropped(event -> {
            List<File> files = event.getDragboard().getFiles();
            if (!files.isEmpty()) {
                handleFileSelection(files.get(0));
            }
            event.setDropCompleted(true);
            event.consume();
        });
    }

    @FXML
    private void handleAddMediaClick(MouseEvent event) {
        chooseFile();
    }

    @FXML
    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.mov")
        );

        File file = fileChooser.showOpenDialog(publishButton.getScene().getWindow());
        if (file != null) {
            handleFileSelection(file);
        }
    }

    private void handleFileSelection(File file) {
        selectedFile = file;
        if (file.getName().matches("(?i).*\\.(mp4|mov)$")) {
            mediaPreview.setVisible(false);
        } else {
            mediaPreview.setImage(new Image(file.toURI().toString()));
            mediaPreview.setVisible(true);
        }
        mediaPreviewContainer.setVisible(true);
        checkPublishButtonState();
    }

    @FXML
    private void removeMedia() {
        selectedFile = null;
        mediaPreview.setImage(null);
        mediaPreviewContainer.setVisible(false);
        checkPublishButtonState();
    }

    private void checkPublishButtonState() {
        boolean hasContent = !postContent.getText().trim().isEmpty();
        boolean hasMedia = selectedFile != null;
        publishButton.setDisable(!(hasContent || hasMedia));
    }

    @FXML
    private void handlePostSubmission() {
        String content = postContent.getText().trim();
        String imagePath = null;

        if (selectedFile != null) {
            try {
                // Convertir l'image en Base64 au lieu de la copier dans un répertoire
                imagePath = ImageUtils.imageToBase64(selectedFile);
            } catch (IOException e) {
                showAlert("Error", "Failed to convert image to Base64: " + e.getMessage());
                return;
            }
        }


        String privacy = privacyComboBox.getValue();

        // Récupérer l'ID utilisateur actuel depuis SessionManager
        int userId = SessionManager.getInstance().getCurrentUserId();
        
        Post newPost = new Post(
                userId,
                content,
                null, // La date sera automatiquement définie dans le constructeur
                imagePath
        );

        if (socialController != null) {
            try {
                // Enregistrer le post dans la base de données
                socialController.savePostToDatabase(newPost);
                // Ajouter le post au feed
                socialController.addPostToFeed(newPost);
            } catch (SQLException e) {
                showAlert("Erreur de base de données", "Impossible d'enregistrer le post: " + e.getMessage());
                return;
            }
        }

        closeWindow();
    }

    private void closeWindow() {
        ((Stage) publishButton.getScene().getWindow()).close();
    }
    
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}