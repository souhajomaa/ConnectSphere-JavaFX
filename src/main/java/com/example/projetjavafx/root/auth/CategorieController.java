package com.example.projetjavafx.root.auth;

import com.example.projetjavafx.root.auth.CategorieRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
public class CategorieController {

    @FXML
    private CheckBox music, sport, gaming, education, culturel, social, technologique;

    @FXML
    private Button valide;

    private int userId;  // ID de l'utilisateur qui vient de s'inscrire

    private CategorieRepository categorieModel = new CategorieRepository();

    public void setUserId(int userId) {
        this.userId = userId;  // Sauvegarde l'ID de l'utilisateur
    }

    @FXML
    public void onValideClick() {
        try {
            if (music.isSelected()) {
                categorieModel.insertUserInterest(userId, categorieModel.getCategoryId("Music"));
            }
            if (sport.isSelected()) {
                categorieModel.insertUserInterest(userId, categorieModel.getCategoryId("Sport"));
            }
            if (gaming.isSelected()) {
                categorieModel.insertUserInterest(userId, categorieModel.getCategoryId("Gaming"));
            }
            if (education.isSelected()) {
                categorieModel.insertUserInterest(userId, categorieModel.getCategoryId("Éducation"));
            }
            if (culturel.isSelected()) {
                categorieModel.insertUserInterest(userId, categorieModel.getCategoryId("Cultural"));
            }
            if (social.isSelected()) {
                categorieModel.insertUserInterest(userId, categorieModel.getCategoryId("Social"));
            }
            if (technologique.isSelected()) {
                categorieModel.insertUserInterest(userId, categorieModel.getCategoryId("Technologique"));
            }

            System.out.println("User categories and interests added successfully!");
            openLoginView();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openLoginView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/auth/login-view.fxml"));
            Stage stage = (Stage) valide.getScene().getWindow();
            Scene scene = new Scene(fxmlLoader.load());

            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while loading the login page.");
        }
    }

}
