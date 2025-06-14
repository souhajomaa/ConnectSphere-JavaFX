package com.example.projetjavafx.root.events;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import javafx.event.ActionEvent;

public class EventDetailsController {

    @FXML
    private ImageView eventImage;
    @FXML
    private Label eventName;
    @FXML
    private Text eventDescription;
    @FXML
    private Text eventDate;
    @FXML
    private Text eventLocation;

    private Event currentEvent; // Pour stocker l'événement actuel

    // Getter pour accéder à l'événement actuel
    public Event getCurrentEvent() {
        return currentEvent;
    }

    @FXML
    public void initialize() {
        // Appliquer le style CSS global à la fenêtre popup
        eventImage.getStyleClass().add("popup-image");
        eventName.getStyleClass().add("popup-title");
        eventDescription.getStyleClass().add("popup-text");
        eventDate.getStyleClass().add("popup-text");
        eventLocation.getStyleClass().add("popup-text");
    }


    public void setEventDetails(String name, String description, String startDate, String endDate, String location, String imageBase64) {
        eventName.setText(name);
        eventDescription.setText(description);
        eventDate.setText("Date: " + startDate + " - " + endDate);
        eventLocation.setText("Location: " + location);

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            Image image = new Image(new ByteArrayInputStream(imageBytes));
            eventImage.setImage(image);
        }

        // Créer un objet Event pour stocker les détails
        currentEvent = new Event();
        currentEvent.setName(name);
        currentEvent.setDescription(description);
        currentEvent.setStartDate(startDate);
        currentEvent.setEndDate(endDate);
        currentEvent.setLocation(location);
        currentEvent.setImageBase64(imageBase64);
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) eventName.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void shareEvent(ActionEvent event) {
        try {
            // Vérifier si l'utilisateur est connecté
            int currentUserId = com.example.projetjavafx.root.auth.SessionManager.getInstance().getCurrentUserId();
            if (currentUserId == -1) {
                showAlert("Connexion requise", "Veuillez vous connecter pour partager cet événement.");
                return;
            }

            // Créer un menu contextuel pour les options de partage
            javafx.scene.control.ContextMenu shareMenu = new javafx.scene.control.ContextMenu();

            // Option pour partager sur le feed interne
            javafx.scene.control.MenuItem feedItem = new javafx.scene.control.MenuItem("Partager sur mon feed");
            feedItem.setOnAction(e -> shareToFeed(currentUserId));

            // Options pour partager sur les réseaux sociaux externes
            javafx.scene.control.MenuItem facebookItem = createSocialMenuItem("Partager sur Facebook", "/assets/facebook.svg", "Facebook");
            javafx.scene.control.MenuItem whatsappItem = createSocialMenuItem("Partager sur WhatsApp", "/assets/whatsapp.svg", "WhatsApp");
            javafx.scene.control.MenuItem messengerItem = createSocialMenuItem("Partager sur Messenger", "/assets/messenger.svg", "Messenger");
            javafx.scene.control.MenuItem twitterItem = new javafx.scene.control.MenuItem("Partager sur Twitter");
            twitterItem.setOnAction(e -> shareToExternalSocial("Twitter"));
            javafx.scene.control.MenuItem linkedinItem = new javafx.scene.control.MenuItem("Partager sur LinkedIn");
            linkedinItem.setOnAction(e -> shareToExternalSocial("LinkedIn"));

            // Ajouter tous les items au menu
            shareMenu.getItems().addAll(feedItem, facebookItem, whatsappItem, messengerItem, twitterItem, linkedinItem);

            // Récupérer le bouton qui a déclenché l'action
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();

            // Afficher le menu contextuel sous le bouton
            shareMenu.show(source, javafx.geometry.Side.BOTTOM, 0, 0);

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de partager l'événement: " + e.getMessage());
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
    private javafx.scene.control.MenuItem createSocialMenuItem(String text, String iconPath, String platform) {
        javafx.scene.control.MenuItem menuItem = new javafx.scene.control.MenuItem(text);

        try {
            // Charger l'icône SVG
            java.io.InputStream iconStream = getClass().getResourceAsStream(iconPath);
            if (iconStream != null) {
                javafx.scene.image.Image icon = new javafx.scene.image.Image(iconStream);
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(icon);
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

    // Méthode pour partager l'événement sur le feed interne
    private void shareToFeed(int currentUserId) {
        try {
            // Créer un contenu pour le post
            String content = "J'ai partagé l'événement : " + currentEvent.getName() + "\n" +
                    "Date: " + currentEvent.getStartDate() + " - " + currentEvent.getEndDate() + "\n" +
                    "Lieu: " + currentEvent.getLocation();

            // Créer un nouveau post avec l'ID de l'événement
            com.example.projetjavafx.root.social.Post post = new com.example.projetjavafx.root.social.Post(
                    currentUserId,
                    content,
                    null, // La date sera automatiquement définie dans le constructeur
                    null  // Pas d'image spécifique pour le post, on utilisera celle de l'événement
            );

            // Définir l'ID de l'événement dans le post
            post.setEventId(currentEvent.getEventId());

            // Enregistrer le post dans la base de données
            com.example.projetjavafx.root.DbConnection.AivenMySQLManager.getConnection();
            com.example.projetjavafx.root.social.PostDAO postDAO =
                    new com.example.projetjavafx.root.social.PostDAO(com.example.projetjavafx.root.DbConnection.AivenMySQLManager.getConnection());
            postDAO.addPost(post);

            // Afficher un message de confirmation
            showAlert("Partage réussi", "L'événement a été partagé sur votre feed avec succès!");

            // Fermer la fenêtre de détails
            closeWindow();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de partager l'événement sur le feed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour partager l'événement sur les réseaux sociaux externes
    private void shareToExternalSocial(String platform) {
        try {
            // Préparer le contenu à partager
            String title = "Événement: " + currentEvent.getName();
            String content = currentEvent.getDescription() + "\n" +
                    "Date: " + currentEvent.getStartDate() + " - " + currentEvent.getEndDate() + "\n" +
                    "Lieu: " + currentEvent.getLocation();
            String url = "https://notre-app.com/events/" + currentEvent.getEventId(); // URL fictive

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
                            java.net.URLEncoder.encode(title + "\n" + content, "UTF-8") +
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

            // Afficher un message de confirmation après le partage
            showAlert("Partage réussi", "L'événement a été partagé sur " + platform + " avec succès!");

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
}