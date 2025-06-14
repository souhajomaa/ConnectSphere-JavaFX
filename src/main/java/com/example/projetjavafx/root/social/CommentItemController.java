package com.example.projetjavafx.root.social;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class CommentItemController {
    @FXML private ImageView userAvatar;
    @FXML private Label userName;
    @FXML private Label commentContent;
    @FXML private Label timestamp;
    @FXML private Button likeCommentButton;
    @FXML private Button replyButton;
    @FXML private Button editCommentButton; // Ensure this is declared
    @FXML private Button deleteCommentButton; // Ensure this is declared
    
    private Comment comment;
    private CommentDAO commentDAO;
    private int currentUserId;
    private PostItemController parentController;
    
    public void initialize() {
        // Default initialization
    }
    
    public void setData(Comment comment, CommentDAO commentDAO, int currentUserId, PostItemController parentController) {
        this.comment = comment;
        this.commentDAO = commentDAO;
        this.currentUserId = currentUserId;
        this.parentController = parentController;
        
        userName.setText(comment.getUserName());
        commentContent.setText(comment.getContent());
        
        // Format timestamp
        try {
            LocalDateTime dateTime = LocalDateTime.parse(comment.getTimestamp());
            timestamp.setText(dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        } catch (Exception e) {
            timestamp.setText(comment.getTimestamp());
        }
        
        // Set user avatar
        try {
            Image avatar = new Image(getClass().getResourceAsStream("/com/example/projetjavafx/social/img/userprofile.png"));
            userAvatar.setImage(avatar);
            // Apply circular clip to the avatar
            applyCircularClipToImageView(userAvatar);
        } catch (Exception e) {
            System.err.println("Error loading avatar: " + e.getMessage());
        }
        
        // Show edit and delete buttons if the comment belongs to the current user
        if (comment.getUserId() == currentUserId) {
            deleteCommentButton.setVisible(true);
            deleteCommentButton.setManaged(true);
            editCommentButton.setVisible(true);
            editCommentButton.setManaged(true);
        }
    }
    
    @FXML
    private void handleLikeComment() {
        // Implement comment like functionality
        likeCommentButton.getStyleClass().add("liked");
    }
    
    @FXML
    private void handleReply() {
        // Implement reply functionality
        parentController.focusCommentInput("@" + comment.getUserName() + " ");
    }
    
    @FXML
    private void handleEditComment() {
        TextInputDialog dialog = new TextInputDialog(comment.getContent());
        dialog.setTitle("Edit Comment");
        dialog.setHeaderText("Edit your comment");
        dialog.setContentText("Content:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            try {
                commentDAO.updateComment(comment.getCommentId(), newContent);
                commentContent.setText(newContent);
            } catch (SQLException e) {
                System.err.println("Error updating comment: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteComment() {
        try {
            commentDAO.deleteComment(comment.getCommentId());
            parentController.refreshComments();
        } catch (SQLException e) {
            System.err.println("Error deleting comment: " + e.getMessage());
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
}