package com.example.projetjavafx.root.social;

import com.example.projetjavafx.root.group.GroupFeedController;
import com.example.projetjavafx.root.profile.ProfileRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import javafx.stage.FileChooser;
import com.example.projetjavafx.utils.ImageUtils;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class PostItemController {
    @FXML private Button commentButton;
    @FXML private HBox commentInputBox;
    @FXML private Button postMenuButton;
    @FXML private Label postContent;
    @FXML private ImageView postImage;
    @FXML private Label likeCount;
    @FXML private Button likeButton;
    @FXML private Button shareButton;
    @FXML private Label viewCommentsLink;
    @FXML private VBox commentsContainer;
    @FXML private TextField commentField;
    @FXML private Label userName;
    @FXML private Label postTimestamp;
    @FXML private ImageView userAvatar;  // For post author's profile picture
    @FXML private Label groupNameLabel; // Pour afficher le nom du groupe
    private Post post;
    private Connection connection;
    private int currentUserId;
    private GroupFeedController parentController;
    private SocialController socialController; // Add SocialController reference
    private LikeDAO likeDAO;
    private CommentDAO commentDAO;
    private int postId; // Add this declaration for postId

    public void setData(Post post, Connection connection, int currentUserId, Object controller) {
        // Check if controller is GroupFeedController or SocialController
        if (controller instanceof GroupFeedController) {
            this.parentController = (GroupFeedController) controller;
        } else if (controller instanceof SocialController) {
            this.socialController = (SocialController) controller;
        }
        this.post = post;
        this.connection = connection;
        this.currentUserId = currentUserId;

        this.likeDAO = new LikeDAO(connection);
        this.commentDAO = new CommentDAO(connection);

        // Vérifier si c'est un post de partage d'événement
        boolean isEventShare = post.getEventId() > 0;

        // Vérifier si le post appartient à un groupe et afficher le nom du groupe
        if (post.getGroupId() > 0) {
            try {
                // Récupérer le nom du groupe à partir de l'ID du groupe
                com.example.projetjavafx.root.group.GroupProfileRepository groupRepo = new com.example.projetjavafx.root.group.GroupProfileRepository(null, null, null, null);
                com.example.projetjavafx.root.group.GroupProfileRepository groupData = groupRepo.getGroupById(post.getGroupId());
                if (groupData != null) {
                    groupNameLabel.setText(" ----> " + groupData.getName());
                    groupNameLabel.setVisible(true);
                } else {
                    groupNameLabel.setVisible(false);
                }
            } catch (Exception e) {
                System.err.println("Error loading group name: " + e.getMessage());
                groupNameLabel.setVisible(false);
            }
        } else {
            groupNameLabel.setVisible(false);
        }

        // Set post data with user profile information
        ProfileRepository userProfile = ProfileRepository.getUserProfile(post.getUserId());
        if (userProfile != null) {
            userName.setText(userProfile.getUsername());

            // Set user profile pic if available
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
                    userAvatar.setImage(avatar);
                    userAvatar.setVisible(true);
                    // Apply circular clip to the avatar
                    applyCircularClipToImageView(userAvatar);
                } else {
                    System.err.println("Avatar image not found, using default");
                    loadDefaultProfilePicture();
                }
            } catch (Exception e) {
                System.err.println("Error loading profile picture: " + e.getMessage());
                System.err.println("Avatar image not found, using default");
                loadDefaultProfilePicture();
            }
        } else {
            System.err.println("Avatar image not found");
            loadDefaultProfilePicture();
        }
        } else {
            userName.setText("User " + post.getUserId()); // Fallback to user ID if profile not found
            System.err.println("Avatar image not found");
            loadDefaultProfilePicture();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        if (post.getCreatedAt() != null) {
            postTimestamp.setText(sdf.format(post.getCreatedAt()));
        } else {
            postTimestamp.setText("Date indisponible"); // Valeur par défaut en cas de null
        }

        if (post.getContent() != null && !post.getContent().isEmpty()) {
            postContent.setText(post.getContent());
            postContent.setVisible(true);
        } else {
            postContent.setVisible(false);
        }

        // Vérifier si c'est un post d'événement et récupérer l'image de l'événement si nécessaire
        if (post.getEventId() > 0) {
            try {
                // Récupérer l'événement depuis la base de données
                Connection eventConn = com.example.projetjavafx.root.DbConnection.AivenMySQLManager.getConnection();
                String eventQuery = "SELECT image FROM events WHERE event_id = ?";
                try (PreparedStatement stmt = eventConn.prepareStatement(eventQuery)) {
                    stmt.setInt(1, post.getEventId());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String imageBase64 = rs.getString("image");
                        if (imageBase64 != null && !imageBase64.isEmpty()) {
                            byte[] imageBytes = java.util.Base64.getDecoder().decode(imageBase64);
                            Image image = new Image(new java.io.ByteArrayInputStream(imageBytes));
                            postImage.setImage(image);
                            postImage.setVisible(true);
                        } else {
                            postImage.setVisible(false);
                        }
                    }
                }

                // Assurer que les likes et commentaires sont initialisés pour les posts d'événements
                refreshLikeStatus();
                refreshComments();

            } catch (Exception e) {
                System.err.println("Error loading event image: " + e.getMessage());
                e.printStackTrace();
                // Si l'image de l'événement ne peut pas être chargée, essayer d'utiliser l'image du post
                loadPostImage();
            }
        } else {
            // Si ce n'est pas un post d'événement, charger l'image du post normalement
            loadPostImage();
        }
    }

    // Méthode pour charger l'image du post
    private void loadPostImage() {
        if (post.getImagePath() != null && !post.getImagePath().isEmpty()) {
            try {
                String imagePath = post.getImagePath();
                
                // Vérifier si c'est une image en Base64
                if (ImageUtils.isValidBase64Image(imagePath)) {
                    // Charger l'image depuis la chaîne Base64
                    Image image = ImageUtils.base64ToImage(imagePath);
                    if (image != null && !image.isError()) {
                        postImage.setImage(image);
                        postImage.setVisible(true);
                        System.out.println("Image loaded from Base64 string.");
                    } else {
                        System.err.println("Error loading Base64 image.");
                        postImage.setVisible(false);
                    }
                } else {
                    // Support pour l'ancien format de chemin de fichier
                    imagePath = imagePath.replace("\\", "/");
                    System.out.println("Trying to load image from path: " + imagePath);

                    // 1. Essayer depuis les ressources (packagées dans le JAR)
                    InputStream resourceStream = getClass().getResourceAsStream(imagePath.startsWith("/") ? imagePath : "/" + imagePath);
                    if (resourceStream != null) {
                        Image image = new Image(resourceStream);
                        postImage.setImage(image);
                        postImage.setVisible(true);
                        System.out.println("Image loaded from resources.");
                        return;
                    }

                    // 2. Essayer depuis le dossier 'uploads' du projet
                    File localFile = new File(imagePath.startsWith("/") ? imagePath.substring(1) : imagePath);
                    if (!localFile.exists()) {
                        // Fallback : chemin sans dossier /uploads/images
                        localFile = new File("uploads/images", new File(imagePath).getName());
                    }

                    if (localFile.exists()) {
                        Image image = new Image(localFile.toURI().toString());
                        postImage.setImage(image);
                        postImage.setVisible(true);
                        System.out.println("Image loaded from /uploads/images/ folder: " + localFile.getAbsolutePath());
                    } else {
                        System.err.println("Post image not found: " + imagePath);
                        postImage.setVisible(false);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading post image: " + e.getMessage());
                e.printStackTrace();
                postImage.setVisible(false);
            }
        } else {
            postImage.setVisible(false);
        }

        refreshLikeStatus();
        refreshComments();
    }

    @FXML
    private void toggleLike() {
        try {
            if (likeDAO.isPostLikedByUser(post.getPostId(), currentUserId)) {
                likeDAO.unlikePost(post.getPostId(), currentUserId);
            } else {
                likeDAO.likePost(post.getPostId(), currentUserId);
            }
            refreshLikeStatus();
        } catch (SQLException e) {
            System.err.println("Erreur lors du basculement du like : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void addComment() {
        String content = commentField.getText().trim();
        if (!content.isEmpty()) {
            try {
                commentDAO.addComment(post.getPostId(), currentUserId, content);
                commentField.clear();
                refreshComments();
            } catch (SQLException e) {
                System.err.println("Error adding comment: " + e.getMessage());
            }
        }
    }

    public void refreshLikeStatus() {
        try {
            int likes = likeDAO.getLikeCount(post.getPostId());
            likeCount.setText(likes + " Like");

            boolean isLiked = likeDAO.isPostLikedByUser(post.getPostId(), currentUserId);
            if (isLiked) {
                likeButton.setText("Unlike");
                likeButton.setStyle("-fx-text-fill: #1877F2;");
            } else {
                likeButton.setText("Like");
                likeButton.setStyle("-fx-text-fill: black;");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut like : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refreshComments() {
        try {
            List<Comment> comments = commentDAO.getCommentsForPost(post.getPostId());
            commentsContainer.getChildren().clear();

            for (Comment comment : comments) {
                Label commentLabel = new Label(comment.getContent());
                commentLabel.setWrapText(true);
                commentLabel.setStyle("-fx-padding: 5; -fx-background-color: #f0f2f5; -fx-background-radius: 10;");

                // Get user profile information for comment author
                ProfileRepository commentAuthor = ProfileRepository.getUserProfile(comment.getUserId());
                String authorName = commentAuthor != null ? commentAuthor.getUsername() : "User " + comment.getUserId();

                Label authorLabel = new Label(authorName);
                authorLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 0 5;");

                // Add profile picture for comment author
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
                            // Apply circular clip to comment author avatar
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

                HBox authorBox = new HBox(5, authorAvatar, authorLabel);
                authorBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                VBox commentBox = new VBox(5, authorBox, commentLabel);
                commentBox.setStyle("-fx-padding: 5 0;");

                commentsContainer.getChildren().add(commentBox);
            }

            viewCommentsLink.setText("Voir " + comments.size() + " comments");
        } catch (SQLException e) {
            System.err.println("Error refreshing comments: " + e.getMessage());
        }
    }

    public Post getPost() {
        return post;
    }

    // Getter methods for fields needed by subclasses
    protected LikeDAO getLikeDAO() {
        return likeDAO;
    }

    protected Label getLikeCount() {
        return likeCount;
    }

    protected Button getLikeButton() {
        return likeButton;
    }

    protected int getCurrentUserId() {
        return currentUserId;
    }

    protected VBox getCommentsContainer() {
        return commentsContainer;
    }

    protected CommentDAO getCommentDAO() {
        return commentDAO;
    }

    protected Label getViewCommentsLink() {
        return viewCommentsLink;
    }

    public void focusCommentInput(String text) {
        if (text != null && !text.isEmpty()) {
            commentField.setText(text);
        }
        commentField.requestFocus();
    }

    @FXML
    private void onEditPost() {
        openEditDialog(post);
    }

    @FXML
    public void setParentController(GroupFeedController parentController) {
        this.parentController = parentController;
    }

    public void setPostId(int postId) {
        this.postId = postId; // Initialize postId
    }

    @FXML
    private void onDeletePost(ActionEvent event) {
        if (parentController != null) {
            parentController.deletePost(postId); // Use postId here
        } else if (socialController != null) {
            socialController.deletePost(postId); // Use socialController if parentController is null
        } else {
            System.err.println("Error: No controller initialized for post deletion.");
        }
    }

    @FXML
    private void showPostOptions() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(event -> onEditPost());

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> onDeletePost(event)); // Pass ActionEvent

        contextMenu.getItems().addAll(editItem, deleteItem);
        contextMenu.show(postMenuButton, Side.BOTTOM, 0, 0);
    }

    private void openEditDialog(Post post) {
        TextInputDialog dialog = new TextInputDialog(post.getContent());
        dialog.setTitle("Edit Post");
        dialog.setHeaderText("Edit your post content");
        dialog.setContentText("Content:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            String newImagePath = selectNewImage();
            // Use the appropriate controller to edit the post
            if (parentController != null) {
                parentController.editPost(post.getPostId(), newContent, newImagePath);
            } else if (socialController != null) {
                socialController.editPost(post.getPostId(), newContent, newImagePath);
            }
        });
    }

    private String selectNewImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select New Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                Path imagesDir = Paths.get("src/main/resources/com/example/projetjavafx/img/posts");
                if (!Files.exists(imagesDir)) {
                    Files.createDirectories(imagesDir);
                }
                String uniqueFileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path destination = imagesDir.resolve(uniqueFileName);
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                return "/com/example/projetjavafx/img/posts/" + uniqueFileName;
            } catch (IOException e) {
                System.err.println("Error processing new image: " + e.getMessage());
            }
        }
        return post.getImagePath();
    }

    @FXML
    private void toggleCommentInput() {
        boolean isVisible = commentInputBox.isVisible();
        commentInputBox.setVisible(!isVisible);
        commentInputBox.setManaged(!isVisible);
    }

    @FXML
    private void toggleCommentsVisibility() {
        boolean isVisible = commentsContainer.isVisible();
        commentsContainer.setVisible(!isVisible);
        commentsContainer.setManaged(!isVisible);

        // If we're showing comments, refresh them to ensure we only show comments for this post
        if (!isVisible) {
            refreshComments();
        }
    }

    @FXML
    private void sharePost() {
        try {
            // Vérifier si c'est un post d'événement
            if (post.getEventId() > 0) {
                shareEventToSocialMedia();
            } else {
                shareRegularPost();
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de partager le post: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void shareEventToSocialMedia() {
        // Créer un menu contextuel pour les options de partage
        ContextMenu shareMenu = new ContextMenu();

        MenuItem feedItem = new MenuItem("Partager sur mon feed");
        feedItem.setOnAction(event -> shareToFeed());

        // Créer les items de menu avec des icônes pour les réseaux sociaux
        MenuItem facebookItem = createSocialMenuItem("Partager sur Facebook", "/assets/facebook.svg", "Facebook");
        MenuItem whatsappItem = createSocialMenuItem("Partager sur WhatsApp", "/assets/whatsapp.svg", "WhatsApp");
        MenuItem messengerItem = createSocialMenuItem("Partager sur Messenger", "/assets/messenger.svg", "Messenger");
        MenuItem twitterItem = new MenuItem("Partager sur Twitter");
        twitterItem.setOnAction(event -> shareToExternalSocial("Twitter"));
        MenuItem linkedinItem = new MenuItem("Partager sur LinkedIn");
        linkedinItem.setOnAction(event -> shareToExternalSocial("LinkedIn"));

        shareMenu.getItems().addAll(feedItem, facebookItem, whatsappItem, messengerItem, twitterItem, linkedinItem);
        shareMenu.show(shareButton, Side.BOTTOM, 0, 0);
    }

    private void shareRegularPost() {
        // Partager un post normal
        ContextMenu shareMenu = new ContextMenu();

        MenuItem feedItem = new MenuItem("Partager sur mon feed");
        feedItem.setOnAction(event -> shareToFeed());

        // Créer les items de menu avec des icônes pour les réseaux sociaux
        MenuItem facebookItem = createSocialMenuItem("Partager sur Facebook", "/assets/facebook.svg", "Facebook");
        MenuItem whatsappItem = createSocialMenuItem("Partager sur WhatsApp", "/assets/whatsapp.svg", "WhatsApp");
        MenuItem messengerItem = createSocialMenuItem("Partager sur Messenger", "/assets/messenger.svg", "Messenger");
        MenuItem twitterItem = new MenuItem("Partager sur Twitter");
        twitterItem.setOnAction(event -> shareToExternalSocial("Twitter"));
        MenuItem linkedinItem = new MenuItem("Partager sur LinkedIn");
        linkedinItem.setOnAction(event -> shareToExternalSocial("LinkedIn"));

        shareMenu.getItems().addAll(feedItem, facebookItem, whatsappItem, messengerItem, twitterItem, linkedinItem);
        shareMenu.show(shareButton, Side.BOTTOM, 0, 0);
    }

    private void shareToFeed() {
        try {
            // Créer un nouveau post qui partage le post actuel
            String content = "I shared a post from " + userName.getText();

            // Si c'est un événement, ajouter les détails de l'événement
            if (post.getEventId() > 0) {
                content = "\n" +
                        "I shared the event : " + post.getContent();
            }

            // Créer un nouveau post
            Post sharedPost = new Post(
                    currentUserId,
                    content,
                    null, // La date sera automatiquement définie dans le constructeur
                    post.getImagePath() // Utiliser la même image que le post original
            );

            // Si c'est un événement, définir l'ID de l'événement dans le nouveau post
            if (post.getEventId() > 0) {
                sharedPost.setEventId(post.getEventId());
            }

            // Enregistrer le post dans la base de données
            PostDAO postDAO = new PostDAO(connection);
            postDAO.addPost(sharedPost);

            // Afficher un message de confirmation
            showAlert("Successful sharing", "\n" +
                    "The post has been successfully shared to your feed.!");

            // Rafraîchir le feed si nécessaire
            if (socialController != null) {
                socialController.loadAllPosts();
            } else if (parentController != null) {
                parentController.loadGroupPosts();
            }

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de partager le post: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void shareToExternalSocial(String platform) {
        try {
            // Préparer le contenu à partager
            String title = "Événement partagé depuis notre application";
            String content = post.getContent();
            String url = "https://notre-app.com/events/" + post.getEventId(); // URL fictive

            // Utiliser des liens profonds pour partager sur différentes plateformes
            switch(platform) {
                case "Facebook":
                    // Lien profond pour Facebook
                    String facebookUrl = "https://www.facebook.com/sharer/sharer.php?u=" +
                            java.net.URLEncoder.encode(url, "UTF-8") +
                            "&quote=" + java.net.URLEncoder.encode(content, "UTF-8");
                    openInBrowser(facebookUrl);
                    break;

                case "WhatsApp":
                    // Lien profond pour WhatsApp
                    String whatsappUrl = "https://api.whatsapp.com/send?text=" +
                            java.net.URLEncoder.encode(title + "\n" + content + "\n" + url, "UTF-8");
                    openInBrowser(whatsappUrl);
                    break;

                case "Messenger":
                    // Lien profond pour Messenger
                    String messengerUrl = "https://www.facebook.com/dialog/send?app_id=123456789" +
                            "&link=" + java.net.URLEncoder.encode(url, "UTF-8") +
                            "&redirect_uri=" + java.net.URLEncoder.encode("https://notre-app.com", "UTF-8");
                    openInBrowser(messengerUrl);
                    break;

                case "Twitter":
                    // Lien profond pour Twitter
                    String twitterUrl = "https://twitter.com/intent/tweet?text=" +
                            java.net.URLEncoder.encode(content, "UTF-8") +
                            "&url=" + java.net.URLEncoder.encode(url, "UTF-8");
                    openInBrowser(twitterUrl);
                    break;

                case "LinkedIn":
                    // Lien profond pour LinkedIn
                    String linkedinUrl = "https://www.linkedin.com/sharing/share-offsite/?url=" +
                            java.net.URLEncoder.encode(url, "UTF-8");
                    openInBrowser(linkedinUrl);
                    break;

                default:
                    showAlert("Partage externe", "Partage sur " + platform + " non supporté pour le moment.");
            }
        } catch (Exception e) {
            showAlert("Erreur de partage", "Impossible de partager sur " + platform + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour ouvrir un lien dans le navigateur par défaut
    private void openInBrowser(String url) {
        try {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                desktop.browse(new java.net.URI(url));
            } else {
                showAlert("Navigateur non supporté", "Votre système ne supporte pas l'ouverture automatique du navigateur.");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le navigateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour créer un item de menu avec une icône
    private MenuItem createSocialMenuItem(String text, String iconPath, String platform) {
        MenuItem menuItem = new MenuItem(text);

        try {
            // Charger l'icône SVG
            InputStream iconStream = getClass().getResourceAsStream(iconPath);
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                ImageView imageView = new ImageView(icon);
                imageView.setFitHeight(16);
                imageView.setFitWidth(16);
                menuItem.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'icône: " + e.getMessage());
        }

        // Définir l'action pour le partage sur la plateforme spécifiée
        menuItem.setOnAction(event -> shareToExternalSocial(platform));

        return menuItem;
    }

    // Helper method to load default profile picture for post author
    private void loadDefaultProfilePicture() {
        try {
            // Try to load from resources first
            InputStream is = getClass().getResourceAsStream("/com/example/projetjavafx/social/img/userprofile.png");
            if (is != null) {
                Image avatar = new Image(is);
                userAvatar.setImage(avatar);
                userAvatar.setVisible(true);
                // Apply circular clip to the avatar
                applyCircularClipToImageView(userAvatar);
                System.out.println("Avatar image loaded successfully");
            } else {
                // Fallback to a file path if resource not found
                File file = new File("src/main/resources/com/example/projetjavafx/social/img/userprofile.png");
                if (file.exists()) {
                    Image avatar = new Image(file.toURI().toString());
                    userAvatar.setImage(avatar);
                    // Apply circular clip to the avatar
                    applyCircularClipToImageView(userAvatar);
                    System.out.println("Avatar image loaded from file");
                } else {
                    System.err.println("Avatar image not found, using default");
                    userAvatar.setVisible(false);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading avatar: " + e.getMessage());
            userAvatar.setVisible(false);
        }
    }

    // Helper method to load default avatar for comment authors
    protected void loadDefaultCommentAvatar(ImageView avatarView) {
        try {
            InputStream is = getClass().getResourceAsStream("/com/example/projetjavafx/social/img/userprofile.png");
            if (is != null) {
                Image avatar = new Image(is);
                avatarView.setImage(avatar);
                // Apply circular clip to the avatar
                applyCircularClipToImageView(avatarView);
            } else {
                File file = new File("src/main/resources/com/example/projetjavafx/social/img/userprofile.png");
                if (file.exists()) {
                    Image avatar = new Image(file.toURI().toString());
                    avatarView.setImage(avatar);
                    // Apply circular clip to the avatar
                    applyCircularClipToImageView(avatarView);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading comment avatar: " + e.getMessage());
        }
    }

    // Helper method to apply circular clip to ImageView
    protected void applyCircularClipToImageView(ImageView imageView) {
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(
                imageView.getFitWidth() / 2,
                imageView.getFitHeight() / 2,
                Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2
        );
        imageView.setClip(clip);
    }
}


