package com.example.projetjavafx.root.points;

import com.example.projetjavafx.root.auth.SessionManager;
import com.example.projetjavafx.root.services.PointsService;
import com.example.projetjavafx.root.services.VisitService;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HomeController {

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button logoutButtonn;

    @FXML
    private MenuButton pointMenu;

    @FXML
    private MenuItem convertMenuItem;

    @FXML
    private MenuItem fortuneWheelMenuItem;

    @FXML
    private MenuItem playGameMenuItem;

    @FXML
    private LineChart<String, Number> pointChart;

    @FXML
    private Label serieLabel;

    private PointsService service = new PointsService();
    private VisitService visitService = new VisitService();

    @FXML
    public void initialize() {
        // Récupérer l'ID de l'utilisateur connecté depuis SessionManager
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();
        System.out.println("ID de l'utilisateur connecté : " + currentUserId);

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

        // Mettre à jour le streak et récupérer la nouvelle valeur
        System.out.println("Appel de updateStreak pour l'utilisateur " + currentUserId);
        visitService.updateStreak(currentUserId);
        System.out.println("updateStreak terminé");
        int serie = visitService.getStreak(currentUserId);
        System.out.println("Streak après mise à jour : " + serie);
        if (serieLabel != null) {
            // Option pour forcer une valeur pour tester (décommenter si nécessaire)
            // int serie = 10; // Forcer une valeur pour tester
            // System.out.println("Streak forcé à : " + serie);

            serieLabel.setText(Integer.toString(serie));
            System.out.println("Texte de serieLabel mis à jour à : " + serieLabel.getText());
            // Animation de fondu désactivée temporairement pour tester
            /*
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), serieLabel);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            fadeTransition.setCycleCount(2);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
            System.out.println("Animation de fondu appliquée");
            */
        } else {
            System.out.println("Erreur : serieLabel est null !");
        }

        // Mettre à jour les points
        int point = service.getUserPoints(currentUserId);
        System.out.println("Points de l'utilisateur : " + point);
        if (pointMenu != null) {
            pointMenu.setText("Points : " + point);
        } else {
            System.out.println("Erreur : pointMenu est null !");
        }

        // Fetch points history by day for gains and losses
        Map<String, Integer> pointsDataWin = service.getPointsHistoryByDay(currentUserId, "gain");
        Map<String, Integer> pointsDataLoss = service.getPointsHistoryByDay(currentUserId, "perte");

        // Generate the last 7 days' dates
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        Map<String, Integer> last7DaysDataWin = new LinkedHashMap<>(); // For gains
        Map<String, Integer> last7DaysDataLoss = new LinkedHashMap<>(); // For losses

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String formattedDate = date.format(formatter);
            last7DaysDataWin.put(formattedDate, pointsDataWin.getOrDefault(formattedDate, 0));
            last7DaysDataLoss.put(formattedDate, pointsDataLoss.getOrDefault(formattedDate, 0));
        }

        // Sort the data by date
        List<Map.Entry<String, Integer>> sortedEntriesWin = new ArrayList<>(last7DaysDataWin.entrySet());
        sortedEntriesWin.sort(Map.Entry.comparingByKey()); // Sort by date (key)

        List<Map.Entry<String, Integer>> sortedEntriesLoss = new ArrayList<>(last7DaysDataLoss.entrySet());
        sortedEntriesLoss.sort(Map.Entry.comparingByKey()); // Sort by date (key)

        // Set chart title
        pointChart.setTitle("Points Gained and Lost in the Last 7 Days");

        // Create a series for gains
        XYChart.Series<String, Number> seriesWin = new XYChart.Series<>();
        seriesWin.setName("Points Gained");

        // Add sorted data to the gains series with tooltips
        for (Map.Entry<String, Integer> entry : sortedEntriesWin) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            seriesWin.getData().add(data);
        }

        // Create a series for losses
        XYChart.Series<String, Number> seriesLoss = new XYChart.Series<>();
        seriesLoss.setName("Points Lost");

        // Add sorted data to the losses series with tooltips
        for (Map.Entry<String, Integer> entry : sortedEntriesLoss) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            seriesLoss.getData().add(data);
        }

        // Add both series to the chart
        pointChart.getData().addAll(seriesWin, seriesLoss);

        // Appliquer les tooltips après que les données sont ajoutées
        seriesWin.getData().forEach(data -> {
            if (data.getNode() != null) {
                Tooltip tooltip = new Tooltip("Date: " + data.getXValue() + "\nPoints Gained: " + data.getYValue());
                Tooltip.install(data.getNode(), tooltip);
            } else {
                // Ajouter un listener pour appliquer le tooltip une fois que le nœud est créé
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        Tooltip tooltip = new Tooltip("Date: " + data.getXValue() + "\nPoints Gained: " + data.getYValue());
                        Tooltip.install(newNode, tooltip);
                    }
                });
            }
        });

        seriesLoss.getData().forEach(data -> {
            if (data.getNode() != null) {
                Tooltip tooltip = new Tooltip("Date: " + data.getXValue() + "\nPoints Lost: " + data.getYValue());
                Tooltip.install(data.getNode(), tooltip);
            } else {
                // Ajouter un listener pour appliquer le tooltip une fois que le nœud est créé
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        Tooltip tooltip = new Tooltip("Date: " + data.getXValue() + "\nPoints Lost: " + data.getYValue());
                        Tooltip.install(newNode, tooltip);
                    }
                });
            }
        });
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
    }

    @FXML
    private void openFortuneWheelInterface(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/points/FortuneWheel.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            MenuItem menuItem = (MenuItem) actionEvent.getSource();
            ContextMenu contextMenu = menuItem.getParentPopup();
            Node node = contextMenu.getOwnerNode();
            Stage currentStage = (Stage) node.getScene().getWindow();
            currentStage.setTitle("Fortune Wheel");
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'interface FortuneWheel.fxml");
        }
    }

    @FXML
    private void onConvertClick(ActionEvent actionEvent) {
        try {
            System.out.println("Clic sur le bouton 'convert' (onConvertClick)");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/points/Convert_Consult.fxml"));
            System.out.println("Chargement de Convert_Consult.fxml...");
            Parent root = loader.load();
            System.out.println("Convert_Consult.fxml chargé avec succès");
            Scene scene = new Scene(root);
            System.out.println("Nouvelle scène créée : " + scene);
            MenuItem menuItem = (MenuItem) actionEvent.getSource();
            System.out.println("MenuItem récupéré : " + menuItem);
            ContextMenu contextMenu = menuItem.getParentPopup();
            System.out.println("ContextMenu récupéré : " + contextMenu);
            Node node = contextMenu.getOwnerNode();
            System.out.println("Node récupéré : " + node);
            Stage currentStage = (Stage) node.getScene().getWindow();
            System.out.println("Stage récupérée : " + currentStage);
            currentStage.setTitle("Convert Points");
            currentStage.setScene(scene);
            System.out.println("Nouvelle scène définie sur la Stage");
            currentStage.show();
            System.out.println("Stage affichée");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'interface Convert_Consult.fxml : " + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.err.println("Erreur de NullPointerException : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur inattendue : " + e.getMessage());
        }
    }

    private void openPlayGameInterface() {
        System.out.println("Play Game interface opened");
    }

    private void loadFXMLView(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de " + fxmlPath);
        }
    }
}