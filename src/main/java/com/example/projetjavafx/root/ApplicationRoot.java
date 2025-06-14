package com.example.projetjavafx.root;

import com.example.projetjavafx.root.auth.SessionManager;
import com.example.projetjavafx.root.services.VisitService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ApplicationRoot extends Application {

    private VisitService visitService;
    private SessionManager sessionManager;

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        // Instancier les services
        visitService = new VisitService();
        sessionManager = SessionManager.getInstance();

        // Vérifier si un utilisateur est connecté
        int currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId == -1) {
            System.out.println("Aucun utilisateur connecté. Redirection vers la page de connexion...");
            loadLoginView(stage);
        } else {
            // Un utilisateur est connecté, appliquer la logique de streak et charger l'interface principale
            visitService.updateStreak(currentUserId);
            loadMainView(stage);
        }
    }

    // Méthode pour charger la vue principale (root-view.fxml)
    private void loadMainView(Stage stage) throws IOException {
        String mainFxmlPath = "/com/example/projetjavafx/root/root-view.fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationRoot.class.getResource(mainFxmlPath));
        if (fxmlLoader.getLocation() == null) {
            throw new IOException("Le fichier FXML " + mainFxmlPath + " n'a pas été trouvé.");
        }
        Parent root = fxmlLoader.load();
        double x = root.prefWidth(-1);
        double y = root.prefHeight(-1);
        Scene scene = new Scene(root, x, y);
        stage.setTitle("Accueil - Utilisateur " + sessionManager.getCurrentUserEmail());
        stage.setScene(scene);
        stage.show();
    }










    // Méthode pour charger la vue de connexion (Login.fxml)
    private void loadLoginView(Stage stage) throws IOException {
        // Ajuster le chemin si nécessaire (vérifie le nom exact du fichier)
        String loginFxmlPath = "/com/example/projetjavafx/root/root-view.fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationRoot.class.getResource(loginFxmlPath));
        if (fxmlLoader.getLocation() == null) {
            throw new IOException("Le fichier FXML " + loginFxmlPath + " n'a pas été trouvé. Vérifie l'emplacement et le nom du fichier.");
        }
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}