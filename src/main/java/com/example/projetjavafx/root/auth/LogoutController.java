package com.example.projetjavafx.root.auth;

import com.example.projetjavafx.root.auth.SessionManager;
import javafx.fxml.FXML;


public class LogoutController {
    @FXML
    private void onLogoutButtonClick() {
        // Déconnecter l'utilisateur
        SessionManager.getInstance().logout();

        // Rediriger vers la page de connexion
        openLoginView();
    }

    private void openLoginView() {
        // Charger et afficher la vue de connexion (login)
        // (Voir l'étape 5 pour un exemple complet)
    }
}
