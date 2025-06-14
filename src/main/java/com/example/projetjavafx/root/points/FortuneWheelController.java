package com.example.projetjavafx.root.points;

import com.example.projetjavafx.root.auth.SessionManager;
import com.example.projetjavafx.root.models.Point;
import com.example.projetjavafx.root.services.PointsService;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Random;

public class FortuneWheelController {
    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button logoutButtonn;

    @FXML
    private Label profile;

    @FXML
    private Canvas wheelCanvas;

    @FXML
    private Button spinButton;

    @FXML
    private MenuButton pointMenu;

    private String[] wheelSections = {"100 Points", "0 Points", "20 Points", "5 Points", "40 Points", "50 points"};
    private Color[] wheelColors = {
            Color.web("#2A3F8A"), // Bleu nuit profond (proche de la headbar)
            Color.web("#6B728E"), // Bleu ciel doux
            Color.web("#40C4FF"), // Bleu turquoise
            Color.web("#B39DDB"), // Violet pastel
            Color.web("#FFCDD2"), // Rose pâle (rappel du bouton "Logout")
            Color.web("#CFD8DC")  // Gris perle
    };
    private Integer[] wheelWin = {100, 0, 20, 5, 40, 50};

    private double currentAngle = 0;
    private Random random = new Random();

    private PointsService pointsService = new PointsService();

    @FXML
    public void initialize() {
        System.out.println("Bienvenue sur la roue de la fortune !");
        drawWheel();
        setupSpinButton();

        // Récupérer l'ID de l'utilisateur connecté depuis SessionManager
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();

        // Vérifier si un utilisateur est connecté
        if (currentUserId == -1) {
            System.out.println("Aucun utilisateur connecté. Redirection vers la page de connexion...");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/root/Login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) pointMenu.getScene().getWindow();
                stage.setTitle("Login");
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        // Update pointMenu with user's points
        int userPoints = pointsService.getUserPoints(currentUserId);
        pointMenu.setText("Points : " + userPoints);
    }

    private void showConfirmationDialog(int point) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Félicitation tu as gagné");
        alert.setHeaderText("Tu as gagné " + point + " point");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("User clicked OK");
            } else {
                System.out.println("User clicked Cancel or closed the dialog");
            }
        });
    }

    private void drawWheel() {
        GraphicsContext gc = wheelCanvas.getGraphicsContext2D();
        double centerX = wheelCanvas.getWidth() / 2;
        double centerY = wheelCanvas.getHeight() / 2;
        double radius = Math.min(centerX, centerY) * 0.9; // Réduire légèrement le rayon pour laisser de la place

        // Ajouter une ombre autour de la roue
        gc.setFill(Color.web("#000000", 0.2)); // Ombre légère (noir avec opacité 20%)
        gc.fillOval(centerX - radius - 5, centerY - radius - 5, radius * 2 + 10, radius * 2 + 10);

        double angle = 360.0 / wheelSections.length;

        // Dessiner chaque section de la roue
        for (int i = 0; i < wheelSections.length; i++) {
            // Remplir la section avec la couleur
            gc.setFill(wheelColors[i]);
            gc.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, i * angle, angle, ArcType.ROUND);

            // Ajouter une bordure blanche entre les sections
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeArc(centerX - radius, centerY - radius, radius * 2, radius * 2, i * angle, angle, ArcType.ROUND);

            // Centrer le texte
            gc.setFill(Color.WHITE);
            gc.setFont(new javafx.scene.text.Font(16));
            double textAngle = Math.toRadians(i * angle + angle / 2);
            double textX = centerX + Math.cos(textAngle) * (radius * 0.6);
            double textY = centerY - Math.sin(textAngle) * (radius * 0.6);

            // Ajuster la position du texte pour qu'il soit centré
            gc.save();
            gc.translate(textX, textY);
            gc.rotate(-(i * angle + angle / 2));
            gc.fillText(wheelSections[i], -gc.getFont().getSize() * wheelSections[i].length() / 4, 0);
            gc.restore();
        }

        // Dessiner le triangle indicateur comme partie de la roue (à 0 degré)
        double triangleSize = 20; // Taille du triangle
        gc.setFill(Color.web("#FF6F61")); // Couleur du triangle (rose/rouge clair, comme le bouton "Logout")
        gc.setStroke(Color.WHITE); // Bordure blanche pour le triangle
        gc.setLineWidth(2);

        // Coordonnées des trois points du triangle (positionné à 0 degré sur la roue, pointe vers l'extérieur)
        double[] xPoints = {
                centerX, // Sommet du triangle (pointe vers l'extérieur)
                centerX - triangleSize / 2, // Coin gauche (sur le bord de la roue)
                centerX + triangleSize / 2  // Coin droit (sur le bord de la roue)
        };
        double[] yPoints = {
                centerY - radius, // Sommet (sur le bord de la roue)
                centerY - radius - triangleSize, // Coin gauche (au-dessus de la roue)
                centerY - radius - triangleSize  // Coin droit (au-dessus de la roue)
        };

        gc.fillPolygon(xPoints, yPoints, 3); // Remplir le triangle
        gc.strokePolygon(xPoints, yPoints, 3); // Dessiner la bordure
    }

    private void setupSpinButton() {
        spinButton.setOnAction(event -> spinWheel());
    }

    public void spinWheel() {
        // Choisir une section gagnante au hasard avant de faire tourner la roue
        int winningSectionIndex = random.nextInt(wheelSections.length);
        System.out.println("Section gagnante choisie : " + wheelSections[winningSectionIndex] + " (index " + winningSectionIndex + ")");

        // Calculer l'angle pour aligner la section gagnante avec le triangle (0 degré)
        double sectionAngle = 360.0 / wheelSections.length; // Angle par section (60 degrés pour 6 sections)
        double targetAngle = winningSectionIndex * sectionAngle; // Angle où la section gagnante commence
        double centerOfSection = targetAngle + (sectionAngle / 2); // Centre de la section gagnante

        // Ajuster l'angle pour que le centre de la section gagnante soit à 0 degré (sous le triangle)
        double rotationAngle = (360 - centerOfSection) % 360;

        // Ajouter plusieurs tours pour l'effet visuel (par exemple, 5 tours complets)
        double totalRotation = (360 * 5) + rotationAngle;

        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(3), wheelCanvas);
        rotateTransition.setByAngle(totalRotation);
        rotateTransition.setOnFinished(event -> {
            currentAngle = (currentAngle + totalRotation) % 360;
            int selectedIndex = winningSectionIndex; // Utiliser directement l'index de la section gagnante
            int pointsWon = wheelWin[selectedIndex]; // Points gagnés

            // Récupérer l'ID de l'utilisateur connecté
            SessionManager sessionManager = SessionManager.getInstance();
            int currentUserId = sessionManager.getCurrentUserId();

            // Mettre à jour les points de l'utilisateur
            int currentPoints = pointsService.getUserPoints(currentUserId);
            pointsService.updateUserPoints(currentUserId, currentPoints + pointsWon);

            // Enregistrer le gain dans l'historique
            Point newPointEntry = new Point("gain", pointsWon, "roulette");
            pointsService.addPoints(currentUserId, newPointEntry);

            // Mettre à jour l'affichage des points dans pointMenu
            int updatedPoints = pointsService.getUserPoints(currentUserId);
            pointMenu.setText("Points : " + updatedPoints);

            // Afficher la confirmation
            Platform.runLater(() -> {
                showConfirmationDialog(pointsWon);
            });

            // Highlight the selected section
            highlightSection(selectedIndex);
        });

        rotateTransition.play();
    }

    private void highlightSection(int sectionIndex) {
        GraphicsContext gc = wheelCanvas.getGraphicsContext2D();
        double centerX = wheelCanvas.getWidth() / 2;
        double centerY = wheelCanvas.getHeight() / 2;
        double radius = Math.min(centerX, centerY) * 0.9;
        double sectionAngle = 360.0 / wheelSections.length;

        gc.clearRect(0, 0, wheelCanvas.getWidth(), wheelCanvas.getHeight());
        drawWheel();

        gc.setFill(Color.web("#FFFFFF", 0.5)); // Surligner la section avec un blanc semi-transparent
        gc.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                sectionIndex * sectionAngle, sectionAngle, ArcType.ROUND);
    }

    @FXML
    public void retrunToProfile(MouseEvent mouseEvent) {
        loadFXMLView(mouseEvent, "/com/example/projetjavafx/points/Home.fxml", "Home");
    }

    @FXML
    private void onLoginClick(ActionEvent event) {
        System.out.println("Login button clicked");
        loadFXMLView(event, "/com/example/projetjavafx/root/Login.fxml", "Login");
    }

    @FXML
    private void onRegisterClick(ActionEvent event) {
        System.out.println("Register button clicked");
        loadFXMLView(event, "/com/example/projetjavafx/root/Register.fxml", "Register");
    }

    @FXML
    private void onLogout(ActionEvent event) {
        System.out.println("Logout button clicked");
        // Effacer la session lors de la déconnexion
        SessionManager.getInstance().logout();
        loadFXMLView(event, "/com/example/projetjavafx/root/root-view.fxml", "Connect Sphere");
    }

    @FXML
    private void onOrganizerButtonClick(ActionEvent event) {
        loadFXMLView(event, "/com/example/projetjavafx/organizer/organizer-view.fxml", "Dashboard");
    }

    @FXML
    private void onEventsClick(ActionEvent event) {
        loadFXMLView(event, "/com/example/projetjavafx/events/events-view.fxml", "Events");
    }

    @FXML
    private void onGroupButtonClick(ActionEvent event) {
        loadFXMLView(event, "/com/example/projetjavafx/group/group-profile-view.fxml", "Group");
    }

    @FXML
    private void onJobFeedClick(ActionEvent event) {
        loadFXMLView(event, "/com/example/projetjavafx/jobfeed/job-feed-view.fxml", "Job Feed");
    }

    @FXML
    private void onCreateJobClick(ActionEvent event) {
        loadFXMLView(event, "/com/example/projetjavafx/organizer/create-job-offer-view.fxml", "Create Job");
    }

    @FXML
    private void onProfileClick(ActionEvent event) {
        loadFXMLView(event, "/com/example/projetjavafx/profile/profile-view.fxml", "Profile");
    }

    @FXML
    private void onsocialButtonClick(ActionEvent event) {
        loadFXMLView(event, "/com/example/projetjavafx/social/Feed.fxml", "Social");
    }

    @FXML
    private void onChatbotClick(ActionEvent event) {
        loadFXMLView(event, "/com/example/projetjavafx/chatbot/chatbot.fxml", "AI Assistant");
    }

    @FXML
    private void onPointClick(ActionEvent event) {
        System.out.println("Already on Points page!");
        loadFXMLView(event, "/com/example/projetjavafx/points/Home.fxml", "Points");
    }

    @FXML
    private void onConvertClick(ActionEvent event) {
        loadFXMLView(event, "/com/example/projetjavafx/points/Convert_Consult.fxml", "Convert");
    }

    @FXML
    private void openFortuneWheelInterface(ActionEvent event) {
        System.out.println("Already on Fortune Wheel page!");
    }

    private void loadFXMLView(Event event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                System.err.println("FXML file not found: " + fxmlPath);
                return;
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage;

            if (event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else if (event.getSource() instanceof MenuItem) {
                MenuItem menuItem = (MenuItem) event.getSource();
                ContextMenu contextMenu = menuItem.getParentPopup();
                Node node = contextMenu.getOwnerNode();
                stage = (Stage) node.getScene().getWindow();
            } else {
                return;
            }

            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML file: " + fxmlPath);
            e.printStackTrace();
        }
    }
}