package com.example.projetjavafx.root.auth;

import com.example.projetjavafx.root.auth.PasswordRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class PasswordController {

    @FXML
    private TextField newpassword;

    @FXML
    private TextField comfirmpassword;

    @FXML
    private Button buttonconfirmpass;

    private String email; // Récupéré depuis UsernameController

    private PasswordRepository passwordModel = new PasswordRepository();

    public void setEmail(String email) {
        this.email = email;
        System.out.println("Email received in PasswordController: " + email);  // Debug
    }

    @FXML
    private void initialize() {
        buttonconfirmpass.setOnAction(event -> changePassword());
    }

    private void changePassword() {
        String newPass = newpassword.getText();
        String confirmPass = comfirmpassword.getText();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Error", "Fields must not be empty.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            return;
        }

        if (passwordModel.updatePassword(email, newPass)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully!");
            openLoginView();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "User not found!");
        }
    }

    private void openLoginView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/auth/login-view.fxml"));
            Stage stage = (Stage) buttonconfirmpass.getScene().getWindow();
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to open the login page.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}