package com.example.projetjavafx.root.points;

import com.example.projetjavafx.root.auth.SessionManager;
import com.example.projetjavafx.root.models.HistoriqueTransaction;
import com.example.projetjavafx.root.models.Point;
import com.example.projetjavafx.root.services.PointsService;
import com.example.projetjavafx.root.services.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ConversionController {

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button logoutButtonn;

    @FXML
    private MenuButton pointMenu;

    @FXML
    private TableView<Point> historique;

    @FXML
    private TableColumn<Point, LocalDate> Temp;

    @FXML
    private TableColumn<Point, Integer> Point;

    @FXML
    private TableColumn<Point, String> Type;

    @FXML
    private TableColumn<Point, String> Raison;

    @FXML
    private TableView<HistoriqueTransaction> convert;

    @FXML
    private TableColumn<HistoriqueTransaction, Date> date;

    @FXML
    private TableColumn<HistoriqueTransaction, String> devise;

    @FXML
    private TableColumn<HistoriqueTransaction, Integer> Converti;

    @FXML
    private TableColumn<HistoriqueTransaction, Double> montant;

    @FXML
    private MenuButton deviseSelect;

    @FXML
    private Spinner<Integer> num_point;

    @FXML
    private Label point;

    @FXML
    private Label montantLabel;

    @FXML
    private TextField searchHistorique;

    @FXML
    private TextField searchDevise;

    @FXML
    private MenuButton searchBy;

    private PointsService service = new PointsService();

    private TransactionService transactionService = new TransactionService();

    private int currentUserId;

    @FXML
    public void initialize() {
        // Récupérer l'ID de l'utilisateur connecté depuis SessionManager
        SessionManager sessionManager = SessionManager.getInstance();
        currentUserId = sessionManager.getCurrentUserId();

        // Vérifier si un utilisateur est connecté
        if (currentUserId == -1) {
            System.out.println("Aucun utilisateur connecté. Redirection vers la page de connexion...");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/root/Login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) point.getScene().getWindow();
                stage.setTitle("Login");
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        // Récupérer les points de l'utilisateur
        int numPoint = service.getUserPoints(currentUserId);
        System.out.println("Points de l'utilisateur : " + numPoint); // Pour déboguer
        point.setText(Integer.toString(numPoint));
        pointMenu.setText("Points : " + numPoint);

        // Récupérer l'argent de l'utilisateur
        double montantValue = service.getUserMoney(currentUserId);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(3);
        montantLabel.setText(df.format(montantValue));

        // Configurer le Spinner avec validation
        int minValue = 100; // Valeur minimale du Spinner
        int initialValue = 100; // Valeur initiale du Spinner
        int step = 100; // Pas d'incrémentation

        // Vérifier si l'utilisateur a assez de points pour convertir
        if (numPoint < minValue) {
            // Si l'utilisateur n'a pas assez de points, désactiver le Spinner
            num_point.setDisable(true);
            num_point.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0, step));
            // Afficher une alerte pour informer l'utilisateur
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Points insuffisants");
            alert.setHeaderText("Vous n'avez pas assez de points pour convertir.");
            alert.setContentText("Vous avez besoin d'au moins " + minValue + " points pour effectuer une conversion.");
            alert.showAndWait();
        } else {
            // Configuration normale du Spinner
            num_point.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, numPoint, initialValue, step));
        }

        // Configurer les colonnes de la table "Historique de point"
        Temp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        Point.setCellValueFactory(new PropertyValueFactory<>("points"));
        Type.setCellValueFactory(new PropertyValueFactory<>("type"));
        Raison.setCellValueFactory(new PropertyValueFactory<>("raison"));

        // Récupérer les données de l'historique des points
        List<Point> pointsLog = service.getPointsLog(currentUserId);
        historique.getItems().addAll(pointsLog);

        // Mettre à jour la liste des transactions
        updateTransactionList();
    }

    private void showConfirmationDialog(int point, double montant, String devise) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conversion avec succès");
        alert.setHeaderText("Tu as converti " + point + " en " + montant + " " + devise);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("User clicked OK");
            } else {
                System.out.println("User clicked Cancel or closed the dialog");
            }
        });
    }

    public double fetchChangeAPI(String currency, String from) {
        try {
            URL url = new URL("https://v6.exchangerate-api.com/v6/28a0a39673f4e884948759e9/latest/" + from);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseMap = objectMapper.readValue(response.toString(), Map.class);
            Map<String, Double> conversionRates = (Map<String, Double>) responseMap.get("conversion_rates");
            return conversionRates.get(currency);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void updateTransactionList() {
        // Récupérer l'ID de l'utilisateur connecté depuis SessionManager
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();

        convert.getItems().clear();
        date.setCellValueFactory(new PropertyValueFactory<>("date"));
        devise.setCellValueFactory(new PropertyValueFactory<>("devise"));
        Converti.setCellValueFactory(new PropertyValueFactory<>("pointConvertis"));
        montant.setCellValueFactory(new PropertyValueFactory<>("montant"));

        List<HistoriqueTransaction> transactionLog = transactionService.getTransactionLog(currentUserId);
        convert.getItems().addAll(transactionLog);
    }

    @FXML
    public void retrunToProfile(MouseEvent mouseEvent) {
        loadFXMLView(mouseEvent, "/com/example/projetjavafx/points/Home.fxml", "Home");
    }

    @FXML
    private void deviceToEUR(ActionEvent actionEvent) {
        // Récupérer l'ID de l'utilisateur connecté depuis SessionManager
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();

        deviseSelect.setText("EUR");
        double montantValue = service.getUserMoney(currentUserId) * fetchChangeAPI("EUR", "TND");
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(3);
        montantLabel.setText(df.format(montantValue));
    }

    @FXML
    private void deviceToTND(ActionEvent actionEvent) {
        // Récupérer l'ID de l'utilisateur connecté depuis SessionManager
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();

        deviseSelect.setText("TND");
        double montantValue = service.getUserMoney(currentUserId);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(3);
        montantLabel.setText(df.format(montantValue));
    }

    @FXML
    private void deviceToUSD(ActionEvent actionEvent) {
        // Récupérer l'ID de l'utilisateur connecté depuis SessionManager
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();

        deviseSelect.setText("USD");
        double montantValue = service.getUserMoney(currentUserId) * fetchChangeAPI("USD", "TND");
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(3);
        montantLabel.setText(df.format(montantValue));
    }

    @FXML
    public void convertPointToMoney(MouseEvent mouseEvent) {
        // Récupérer l'ID de l'utilisateur connecté depuis SessionManager
        SessionManager sessionManager = SessionManager.getInstance();
        int currentUserId = sessionManager.getCurrentUserId();

        // Récupérer la valeur du Spinner
        int numPoint = num_point.getValue();
        int userPoints = service.getUserPoints(currentUserId);

        // Vérifier si l'utilisateur a assez de points
        if (numPoint > userPoints) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de conversion");
            alert.setHeaderText("Points insuffisants");
            alert.setContentText("Vous n'avez pas assez de points pour effectuer cette conversion. Points disponibles : " + userPoints);
            alert.showAndWait();
            return;
        }

        // Effectuer la conversion
        String deviseAjouter = deviseSelect.getText();
        double money = service.getUserMoney(currentUserId);
        double montant = 0;
        if (deviseAjouter.equals("EUR")) {
            montant = ((double) numPoint / 100) * 2.5;
            money += ((double) numPoint / 100) * 2.5 * fetchChangeAPI("TND", "EUR");
        } else if (deviseAjouter.equals("TND")) {
            montant = ((double) numPoint / 100) * 5;
            money += ((double) numPoint / 100) * 5;
        } else if (deviseAjouter.equals("USD")) {
            montant = ((double) numPoint / 100) * 3;
            money += ((double) numPoint / 100) * 3 * fetchChangeAPI("TND", "USD");
        }

        // Ajouter la transaction à l'historique
        HistoriqueTransaction newTransaction = new HistoriqueTransaction(currentUserId, "retrait", montant, deviseAjouter, numPoint);
        transactionService.add(newTransaction, currentUserId);

        // Mettre à jour la liste des transactions
        updateTransactionList();

        // Mettre à jour les points et l'argent de l'utilisateur
        service.updateUserPoints(currentUserId, service.getUserPoints(currentUserId) - numPoint);
        service.updateUserArgent(currentUserId, money);

        // Afficher une confirmation
        showConfirmationDialog(numPoint, montant, deviseAjouter);

        // Mettre à jour les labels
        int numPointUser = service.getUserPoints(currentUserId);
        point.setText(Integer.toString(numPointUser));
        pointMenu.setText("Points : " + numPointUser);

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(3);
        montantLabel.setText(df.format(service.getUserMoney(currentUserId) * fetchChangeAPI(deviseAjouter, "TND")));

        // Mettre à jour le Spinner après la conversion
        int minValue = 100;
        int step = 100;
        if (numPointUser < minValue) {
            num_point.setDisable(true);
            num_point.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0, step));
            // Afficher une alerte pour informer l'utilisateur
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Points insuffisants");
            alert.setHeaderText("Vous n'avez plus assez de points pour convertir.");
            alert.setContentText("Vous avez besoin d'au moins " + minValue + " points pour effectuer une conversion.");
            alert.showAndWait();
        } else {
            num_point.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, numPointUser, minValue, step));
        }
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
        System.out.println("Already on Convert page!");
    }

    @FXML
    private void openFortuneWheelInterface(ActionEvent event) {
        loadFXMLView(event, "/com/example/projetjavafx/points/FortuneWheel.fxml", "Fortune Wheel");
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

    public void onSearchHistory(KeyEvent keyEvent) {
        if (!searchBy.getText().equals("tous")) {
            historique.getItems().clear();
            Temp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
            Point.setCellValueFactory(new PropertyValueFactory<>("points"));
            Type.setCellValueFactory(new PropertyValueFactory<>("type"));
            Raison.setCellValueFactory(new PropertyValueFactory<>("raison"));

            List<Point> pointsLog;

            if (searchBy.getText().equals("type")) {
                pointsLog = service.getPointsLogByType(currentUserId, searchHistorique.getText());
            } else {
                pointsLog = service.getPointsLogByRaison(currentUserId, searchHistorique.getText());
            }
            historique.getItems().addAll(pointsLog);
        }
    }

    public void onAllSelected(Event event) {
        searchBy.setText("tous");

        historique.getItems().clear();

        Temp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        Point.setCellValueFactory(new PropertyValueFactory<>("points"));
        Type.setCellValueFactory(new PropertyValueFactory<>("type"));
        Raison.setCellValueFactory(new PropertyValueFactory<>("raison"));

        // Récupérer les données de l'historique des points
        List<Point> pointsLog = service.getPointsLog(currentUserId);
        historique.getItems().addAll(pointsLog);
    }

    public void onTypeSelected(Event event) {
        searchBy.setText("type");
    }

    public void onRaisonSelected(Event event) {
        searchBy.setText("raison");
    }

    public void onTransactionSearch(KeyEvent keyEvent) {
        if (!searchDevise.getText().isEmpty()) {
            convert.getItems().clear();
            date.setCellValueFactory(new PropertyValueFactory<>("date"));
            devise.setCellValueFactory(new PropertyValueFactory<>("devise"));
            Converti.setCellValueFactory(new PropertyValueFactory<>("pointConvertis"));
            montant.setCellValueFactory(new PropertyValueFactory<>("montant"));

            List<HistoriqueTransaction> transactionLog = transactionService.getTransactionLogByDevise(currentUserId, searchDevise.getText());
            convert.getItems().addAll(transactionLog);
        } else {
            convert.getItems().clear();
            date.setCellValueFactory(new PropertyValueFactory<>("date"));
            devise.setCellValueFactory(new PropertyValueFactory<>("devise"));
            Converti.setCellValueFactory(new PropertyValueFactory<>("pointConvertis"));
            montant.setCellValueFactory(new PropertyValueFactory<>("montant"));

            List<HistoriqueTransaction> transactionLog = transactionService.getTransactionLog(currentUserId);
            convert.getItems().addAll(transactionLog);
        }
    }
}