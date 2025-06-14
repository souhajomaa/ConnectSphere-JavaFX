package com.example.projetjavafx.root.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField username;

    @FXML
    private TextField password;

    @FXML
    private Button login;

    @FXML
    private Button signup;

    private final LoginRepository userModel = new LoginRepository();

    @FXML
    private void initialize() {
        // Set the login button to call handleLogin when clicked
        login.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String user = username.getText().trim();
        String pass = password.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        try (ResultSet resultSet = userModel.authenticate(user, pass)) {
            if (resultSet != null && resultSet.next()) { // Vérifie si le résultat contient des données
                int userId = resultSet.getInt("id");
                String email = resultSet.getString("email");

                // Stocker les informations de l'utilisateur connecté
                SessionManager.getInstance().setCurrentUser(userId, email);

                showAlert("Success", "Login successful");
                openHomeView();
            } else {
                showAlert("Error", "Incorrect username or password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while logging in.");
        }
    }

    @FXML
    private void openHomeView() {
        try {
            // Corrected the FXML path to use an absolute path
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/root/root-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage currentStage = (Stage) login.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("Accueil");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to open the home page.");
        }
    }

    @FXML
    private void onLoginClick(ActionEvent event) {
        // Corrected the FXML path to use an absolute path
        loadView("/com/example/projetjavafx/auth/register-view.fxml", event);
    }

    @FXML
    private void goToForgotPasswordPage(ActionEvent event) {
        // Corrected the FXML path to use an absolute path
        loadView("/com/example/projetjavafx/auth/username-view.fxml", event);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadView(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load the view: " + fxmlPath);
        }
    }
}