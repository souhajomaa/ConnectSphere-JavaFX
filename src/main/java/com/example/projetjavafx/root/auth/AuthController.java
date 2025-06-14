package com.example.projetjavafx.root.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class AuthController {

    @FXML
    private TextField username;

    @FXML
    private TextField email;

    @FXML
    private TextField password;

    @FXML
    private TextField age;

    @FXML
    private RadioButton male;

    @FXML
    private RadioButton female;

    @FXML
    private Button register;

    @FXML
    private Button login;

    private ToggleGroup genderGroup;


    private AuthRepository userModel = new AuthRepository();


    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur...");

        // Initialisation du ToggleGroup pour le sexe
        genderGroup = new ToggleGroup();
        male.setToggleGroup(genderGroup);
        female.setToggleGroup(genderGroup);

        // S'assurer qu'au moins un choix est obligatoire
        genderGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                male.setSelected(true); // Par défaut, sélectionner "Male"
            }
        });
    }
    @FXML
    private void onSignupClick(ActionEvent event) {
        if (genderGroup.getSelectedToggle() == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner votre sexe.");
            return;
        }

        String usernameText = username.getText();
        String emailText = email.getText();
        String passwordText = password.getText();
        String ageText = age.getText();

        if (usernameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty() || ageText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs vides", "Veuillez remplir tous les champs.");
            return;
        }

        int ageValue;
        try {
            ageValue = Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Âge invalide", "Veuillez entrer un âge valide.");
            return;
        }

        String sexe = male.isSelected() ? "Male" : "Female";

        try {
            if (userModel.userExists(emailText)) {
                showAlert(Alert.AlertType.ERROR, "Échec de l'inscription", "L'utilisateur existe déjà !");
            } else {
                int userId = userModel.insertUser(usernameText, emailText, passwordText, ageValue, sexe);
                if (userId > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription réussie !");
                    clearFields();
                    loadView("/com/example/projetjavafx/auth/categories-view.fxml", event, userId);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter l'utilisateur.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de connexion", "Problème de connexion à la base de données.");
        }
    }

    @FXML
    private void onLoginClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/auth/login-view.fxml", event, -1);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        username.clear();
        email.clear();
        password.clear();
        age.clear();
        genderGroup.selectToggle(null);

    }

    private void loadView(String fxmlPath, ActionEvent event, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (userId > 0) {
                CategorieController categorieController = loader.getController();
                categorieController.setUserId(userId);
            }
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}