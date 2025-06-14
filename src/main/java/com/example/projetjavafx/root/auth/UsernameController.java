package com.example.projetjavafx.root.auth;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.auth.UsernameRepository;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UsernameController {

    @FXML
    private TextField usernameField;

    @FXML
    private Button checkButton;

    private UsernameRepository usernameModel = new UsernameRepository();

    @FXML
    public void initialize() {
        checkButton.setOnAction(event -> {
            try {
                checkUsername();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @FXML
    private void checkUsername() throws SQLException {
        String email = usernameField.getText().trim();

        if (email.isEmpty()) {
            showError("Enter your Email");
            return;
        }

        if (usernameModel.usernameExists(email)) {
            sendEmail(email);
            System.out.println(email);


        } else {
            showError("Email not found");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void loadPasswordPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/auth/password-view.fxml"));
            Parent passwordView = loader.load();

            // Passer l'email au PasswordController
            PasswordController passwordController = loader.getController();
            passwordController.setEmail(usernameField.getText());

            Scene scene = new Scene(passwordView);
            Stage stage = (Stage) checkButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading password page");
        }
    }

    private void sendEmail(String email) throws SQLException, SQLException {





            String username = "meyssemmufti@gmail.com";
            String password = "owcl kwes xlmh jkot"; // If 2FA is enabled, use an App Password.
            String from = "meyssemmufti@gmail.com";


            String subject = "Connect Sphere";

            String userPass ="";
            Connection cnn = AivenMySQLManager.getConnection();

        String query = "SELECT password FROM users WHERE email = ?";
        PreparedStatement stmt = cnn.prepareStatement(query);
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userPass = rs.getString("password");
            }
        String body = "Hello,\n\nThis is Your password don't forget it again \n\n😁😁😁\n\n"+userPass;
            System.out.println(userPass);
            EmailSender.sendEmail(username, password, from, email, subject, body);


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Password Reset");
        alert.setHeaderText(null);
        alert.setContentText("Check your email ");

        alert.showAndWait();
        openHomeView();


    }

    @FXML
    private void openHomeView() {
        try {
            // Corrected the FXML path to use an absolute path
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/root/root-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("Accueil");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
