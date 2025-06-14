package com.example.projetjavafx.root.social;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.Connection;
import java.sql.SQLException;
import com.example.projetjavafx.utils.ImageUtils;

/**
 * Contrôleur spécifique pour les posts d'événements partagés dans le feed
 * Étend le PostItemController standard mais avec des fonctionnalités spécifiques aux événements
 */
public class EventPostItemController extends PostItemController {
    
    @Override
    public void refreshLikeStatus() {
        try {
            // S'assurer que le post est bien initialisé
            if (getPost() == null || getPost().getPostId() <= 0) {
                return;
            }
            
            // Récupérer le nombre de likes pour ce post d'événement
            int likes = getLikeDAO().getLikeCount(getPost().getPostId());
            getLikeCount().setText(likes + " Like");

            // Vérifier si l'utilisateur actuel a liké ce post
            boolean isLiked = getLikeDAO().isPostLikedByUser(getPost().getPostId(), getCurrentUserId());
            if (isLiked) {
                getLikeButton().setText("Unlike");
                getLikeButton().setStyle("-fx-text-fill: #1877F2;");
            } else {
                getLikeButton().setText("Like");
                getLikeButton().setStyle("-fx-text-fill: black;");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut like pour un événement : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void refreshComments() {
        try {
            // S'assurer que le post est bien initialisé
            if (getPost() == null || getPost().getPostId() <= 0) {
                return;
            }
            
            // Récupérer les commentaires pour ce post d'événement
            getCommentsContainer().getChildren().clear();
            java.util.List<Comment> comments = getCommentDAO().getCommentsForPost(getPost().getPostId());

            // Afficher chaque commentaire
            for (Comment comment : comments) {
                addCommentToUI(comment);
            }

            // Mettre à jour le lien pour voir les commentaires
            getViewCommentsLink().setText("Voir " + comments.size() + " comments");
        } catch (SQLException e) {
            System.err.println("Erreur lors du rafraîchissement des commentaires pour un événement : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ajoute un commentaire à l'interface utilisateur
     */
    private void addCommentToUI(Comment comment) {
        // Créer les éléments d'interface pour le commentaire
        Label commentLabel = new Label(comment.getContent());
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-padding: 5; -fx-background-color: #f0f2f5; -fx-background-radius: 10;");

        // Récupérer les informations de l'auteur du commentaire
        com.example.projetjavafx.root.profile.ProfileRepository commentAuthor = 
                com.example.projetjavafx.root.profile.ProfileRepository.getUserProfile(comment.getUserId());
        String authorName = commentAuthor != null ? commentAuthor.getUsername() : "User " + comment.getUserId();
        
        Label authorLabel = new Label(authorName);
        authorLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 0 5;");

        // Ajouter l'avatar de l'auteur du commentaire
        ImageView authorAvatar = new ImageView();
        authorAvatar.setFitHeight(24);
        authorAvatar.setFitWidth(24);
        
        if (commentAuthor != null && commentAuthor.getProfilePicture() != null && !commentAuthor.getProfilePicture().isEmpty()) {
            try {
                Image avatar;
                String profilePictureData = commentAuthor.getProfilePicture();
                // Check if it's Base64 encoded data
                if (ImageUtils.isValidBase64Image(profilePictureData)) {
                    avatar = ImageUtils.base64ToImage(profilePictureData);
                } else {
                    // Legacy support for URL-based images
                    avatar = new Image(profilePictureData);
                }
                
                if (avatar != null && !avatar.isError()) {
                    authorAvatar.setImage(avatar);
                    applyCircularClipToImageView(authorAvatar);
                } else {
                    loadDefaultCommentAvatar(authorAvatar);
                }
            } catch (Exception e) {
                loadDefaultCommentAvatar(authorAvatar);
            }
        } else {
            loadDefaultCommentAvatar(authorAvatar);
        }
        
        // Créer la disposition pour l'auteur et le commentaire
        javafx.scene.layout.HBox authorBox = new javafx.scene.layout.HBox(5, authorAvatar, authorLabel);
        authorBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        javafx.scene.layout.VBox commentBox = new javafx.scene.layout.VBox(5, authorBox, commentLabel);
        commentBox.setStyle("-fx-padding: 5 0;");

        // Ajouter le commentaire au conteneur
        getCommentsContainer().getChildren().add(commentBox);
    }
}