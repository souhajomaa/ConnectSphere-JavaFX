package com.example.projetjavafx.root.social;

import com.example.projetjavafx.root.group.GroupProfileRepository;
import com.example.projetjavafx.root.group.GroupFeedController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class GroupSearchResultController {
    @FXML
    private HBox groupContainer;
    @FXML
    private ImageView groupProfileImage;
    @FXML
    private Label groupNameLabel;
    @FXML
    private Label groupDescriptionLabel;
    
    private GroupProfileRepository group;
    
    public void setData(GroupProfileRepository group) {
        this.group = group;
        
        // Set group name and description
        groupNameLabel.setText(group.getName());
        groupDescriptionLabel.setText(group.getDescription());
        
        // Set group profile image
        String profilePicData = group.getProfilePicture();
        if (profilePicData != null && !profilePicData.isEmpty()) {
            try {
                // Essayer de décoder comme Base64 d'abord
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(profilePicData);
                    Image avatar = new Image(new ByteArrayInputStream(imageBytes));
                    groupProfileImage.setImage(avatar);
                    applyCircularClipToImageView(groupProfileImage);
                } catch (IllegalArgumentException e) {
                    // Si ce n'est pas du Base64 valide, essayer comme URL
                    Image avatar = new Image(profilePicData);
                    groupProfileImage.setImage(avatar);
                    applyCircularClipToImageView(groupProfileImage);
                }
            } catch (Exception e) {
                System.err.println("Error loading group picture: " + e.getMessage());
                loadDefaultGroupPicture();
            }
        } else {
            loadDefaultGroupPicture();
        }
    }
    
    // Helper method to load default group picture
    private void loadDefaultGroupPicture() {
        try {
            // Try to load from resources first
            InputStream is = getClass().getResourceAsStream("/com/example/projetjavafx/social/img/groupprofile.png");
            if (is != null) {
                Image avatar = new Image(is);
                groupProfileImage.setImage(avatar);
                applyCircularClipToImageView(groupProfileImage);
            } else {
                // Fallback to a file path if resource not found
                groupProfileImage.setFitWidth(40);
                groupProfileImage.setFitHeight(40);
                applyCircularClipToImageView(groupProfileImage);
            }
        } catch (Exception e) {
            System.err.println("Error loading group avatar: " + e.getMessage());
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
    private void onGroupClicked() {
        // Récupérer l'ID du groupe à partir du nom
        int groupId = group.getGroupIdByName(group.getName());
        if (groupId == -1) {
            System.err.println("Erreur : Impossible de trouver l'ID du groupe pour " + group.getName());
            return;
        }

        try {
            // Charger la vue du flux du groupe
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/group/group-feed-view.fxml"));
            Parent root = loader.load();

            // Configurer l'ID du groupe dans le contrôleur
            GroupFeedController controller = loader.getController();
            controller.setGroupId(groupId);

            // Récupérer la fenêtre actuelle
            Window window = groupContainer.getScene().getWindow();

            if (window instanceof Popup) {
                // Si c'est un Popup (UserSearchPopup), le fermer
                Popup popup = (Popup) window;
                popup.hide();

                // Créer un nouveau Stage pour afficher le flux du groupe
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.setTitle("Flux du groupe : " + group.getName()); // Optionnel : titre personnalisé
                newStage.show();
            } else if (window instanceof Stage) {
                // Si c'est déjà un Stage (cas improbable ici), mettre à jour sa scène
                Stage stage = (Stage) window;
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la navigation vers le flux du groupe : " + e.getMessage());
            e.printStackTrace();
        }
    }

}