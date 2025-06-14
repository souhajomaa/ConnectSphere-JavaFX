package com.example.projetjavafx.root.messagerie.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger l'interface graphique depuis le fichier FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ChatView.fxml"));
        Parent root = loader.load();

        // Récupérer le contrôleur associé
        ChatController controller = loader.getController();

        // Simule un utilisateur connecté (À remplacer par un vrai système de connexion)
        //String currentUsername = getLoggedInUsername(); // Nouvelle méthode pour récupérer l'utilisateur

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/chat-style.css").toExternalForm());

        primaryStage.setTitle("Real-time Chat");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Démarrer le serveur dans un thread séparé
        Thread serverThread = new Thread(() -> {
            boolean serverStarted = ServerManager.ensureServerRunning();
            System.out.println("Server startup result: " + (serverStarted ? "SUCCESS" : "FAILED"));

            if (serverStarted) {
                // Une fois le serveur démarré, initialiser le client
                Platform.runLater(() -> {
                    try {
                       // System.out.println("Initializing client with username: " + currentUsername);
                        controller.initializeClient();
                    } catch (Exception e) {
                        System.err.println("Error initializing client: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } else {
                System.err.println("⚠ Le serveur n'a pas pu démarrer. L'initialisation du client est annulée.");
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Gérer la fermeture de l'application
        primaryStage.setOnCloseRequest(event -> {
            if (controller.getClient() != null) {
                controller.getClient().close();
            }
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Récupère le nom de l'utilisateur connecté.
     * Ici, on met une valeur statique, mais en réalité, cela devrait venir d'un système d'authentification.
     */


    public static void main(String[] args) {
        launch(args);
    }
}
