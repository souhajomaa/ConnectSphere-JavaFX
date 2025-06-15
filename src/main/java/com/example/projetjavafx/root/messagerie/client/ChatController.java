package com.example.projetjavafx.root.messagerie.client;

import com.example.projetjavafx.root.auth.SessionManager;
import com.example.projetjavafx.root.messagerie.db.MessageDB;
import com.example.projetjavafx.root.messagerie.model.Message;
import com.example.projetjavafx.root.messagerie.model.User;
import com.example.projetjavafx.root.messagerie.db.UserDB;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;



public class ChatController {

    @FXML private ListView<User> userListView;
    @FXML private TextField searchField;
    @FXML private VBox chatBox;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Label selectedUserLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private Label currentUserLabel;
    @FXML private BorderPane chatPane;
    private Button retourButton; // Référence au bouton Retour (optionnel)
    //adpate hadiiil
    // Variables pour stocker l'ID de l'utilisateur avec lequel on veut discuter
    private int targetUserId = -1;
    private String targetUsername = null;
///wfee update
    private ChatClient client;
    private int currentUserId;
    private String currentUsername;
    private int selectedUserId = -1;
    private UserDB userDB = new UserDB();
    private MessageDB messageDB = new MessageDB();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private Map<Integer, Boolean> unreadMessages = new HashMap<>();
    @FXML
    private void loadView(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void onRetourClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/root/root-view.fxml", event);
    }

    public void initialize() {
        // Set up UI components
        userListView.setItems(userList);
        userListView.setCellFactory(lv -> new UserListCell());

        // Charger la liste des utilisateurs immédiatement
        loadUsers();

        // Initialize connection status
        connectionStatusLabel.setText("Disconnected");
        connectionStatusLabel.setTextFill(Color.RED);

        // Handle user selection
        userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedUserId = newVal.getUserId();
                selectedUserLabel.setText("Chat with " + newVal.getUsername());

                // Clear previous chat
                chatBox.getChildren().clear();

                // Load previous messages for this conversation
                loadConversationHistory(currentUserId, selectedUserId);

                // Marquer les messages comme lus
                unreadMessages.put(selectedUserId, false);

                // Mettre à jour l'affichage de la liste des utilisateurs
                userListView.refresh();

                // Enable message input
                messageField.setDisable(false);
                sendButton.setDisable(false);

                // Scroll to bottom of chat
                Platform.runLater(() -> scrollToBottom());
            }
        });

        // Set up search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchUsers(newVal);
        });

        // Set up message sending
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        // Disable message input until a user is selected
        messageField.setDisable(true);
        sendButton.setDisable(true);

        // Ensure the Messages table exists in the database
        messageDB.createMessagesTableIfNotExists();

        // Ajouter un log pour le débogage
        System.out.println("ChatController initialized");

        // user id
        initializeClient();
    }

    public void initializeClient() {
        try {
            System.out.println("Initializing client...");

            // Assurez-vous que le serveur est en cours d'exécution
            ServerManager.ensureServerRunning();

            // Récupérer l'ID de l'utilisateur depuis SessionManager
            SessionManager sessionManager = SessionManager.getInstance();
            currentUserId = sessionManager.getCurrentUserId();
            currentUsername = sessionManager.getCurrentUserEmail();  // Récupérer l'email ou le nom d'utilisateur

            // Vérifier si l'utilisateur est trouvé dans le sessionManager
            if (currentUserId == -1) {
                // Si l'utilisateur n'est pas trouvé, définir un ID par défaut
                System.out.println("User not found, creating a test user.");
                currentUserId = 1; // ID par défaut pour les tests
            }

            System.out.println("User ID from sessionManager: " + currentUserId);

            // Mettre à jour le label avec le nom d'utilisateur
          //  currentUserLabel.setText(currentUsername);

            // Connexion au serveur WebSocket
            connectToServer();

        } catch (Exception e) {
            System.err.println("Error initializing client: " + e.getMessage());
            e.printStackTrace();
            showAlert("Initialization Error", "Failed to initialize chat client: " + e.getMessage());
        }
    }

    private void connectToServer() {
        try {
            System.out.println("Connecting to WebSocket server...");
            connectionStatusLabel.setText("Connecting...");
            connectionStatusLabel.setTextFill(Color.ORANGE);

            // Make sure server is running before attempting to connect
            boolean serverRunning = ServerManager.ensureServerRunning();
            if (!serverRunning) {
                System.err.println("Server is not running and could not be started");
                connectionStatusLabel.setText("Server Unavailable");
                connectionStatusLabel.setTextFill(Color.RED);
                showAlert("Connection Error", "Could not connect to chat server. Please try again later.");
                return;
            }

            // Wait a bit to ensure server is fully initialized
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Create and connect the client
            client = new ChatClient("ws://localhost:8887", this, currentUserId);

            // Set connection timeout
            client.setConnectionLostTimeout(0); // Disable timeout for testing

            // Connect with explicit try-catch
            try {
                client.connectBlocking(5, TimeUnit.SECONDS); // Wait up to 5 seconds for connection
                System.out.println("WebSocket connection established: " + client.isOpen());
            } catch (Exception e) {
                System.err.println("Error during WebSocket connection: " + e.getMessage());
                e.printStackTrace();
                connectionStatusLabel.setText("Connection Failed");
                connectionStatusLabel.setTextFill(Color.RED);
                showAlert("Connection Error", "Failed to connect to chat server: " + e.getMessage());
            }
        } catch (URISyntaxException e) {
            System.err.println("Invalid WebSocket URI: " + e.getMessage());
            e.printStackTrace();
            connectionStatusLabel.setText("Connection Error");
            connectionStatusLabel.setTextFill(Color.RED);
            showAlert("Connection Error", "Invalid WebSocket server address");
        }
    }

    public void onConnectionEstablished() {
        Platform.runLater(() -> {
            connectionStatusLabel.setText("Connected");
            connectionStatusLabel.setTextFill(Color.GREEN);

            // Display system message
            Message systemMsg = new Message("SYSTEM", 0, currentUserId,
                    "Connected to chat server. You can now send messages.");
            displayMessage(systemMsg);
        });
    }

    public void onConnectionClosed(String reason) {
        Platform.runLater(() -> {
            connectionStatusLabel.setText("Disconnected");
            connectionStatusLabel.setTextFill(Color.RED);

            // Display system message
            Message systemMsg = new Message("SYSTEM", 0, currentUserId,
                    "Disconnected from server: " + reason);
            displayMessage(systemMsg);
        });
    }

    public void onConnectionError(String errorMessage) {
        Platform.runLater(() -> {
            connectionStatusLabel.setText("Connection Error");
            connectionStatusLabel.setTextFill(Color.RED);

            // Display system message
            Message systemMsg = new Message("SYSTEM", 0, currentUserId,
                    "Connection error: " + errorMessage);
            displayMessage(systemMsg);
        });
    }

    private void loadUsers() {
        // Fetch all users from the database
        List<User> users = userDB.getAllUsers();

        // For testing, if the database is empty, add some sample users
        if (users.isEmpty()) {
            // Add some sample users for testing
            users.add(new User(1, "user1"));
            users.add(new User(2, "user2"));
            users.add(new User(3, "user3"));
        }

        Platform.runLater(() -> {
            userList.clear();
            userList.addAll(users);
            /////update hadil
            // Sélectionner l'utilisateur cible s'il a été défini
            if (targetUserId > 0) {
                selectTargetUserInList();
            }
            //////fin update
        });
    }

    private void searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            loadUsers();
            return;
        }

        // Filter users based on search term
        List<User> filteredUsers = userDB.searchUsers(searchTerm);

        // If no results from database or for testing
        if (filteredUsers.isEmpty()) {
            // Filter the existing list
            filteredUsers = new ArrayList<>();
            for (User user : userList) {
                if (user.getUsername().toLowerCase().contains(searchTerm.toLowerCase())) {
                    filteredUsers.add(user);
                }
            }
        }

        List<User> finalFilteredUsers = filteredUsers;
        Platform.runLater(() -> {
            userList.clear();
            userList.addAll(finalFilteredUsers);
        });
    }

    private void loadConversationHistory(int currentUserId, int selectedUserId) {
        // Load previous messages from database
        List<Message> messages = messageDB.getConversation(currentUserId, selectedUserId);

        // Display messages in the chat
        for (Message message : messages) {
            displayMessageOnly(message); // Utiliser displayMessageOnly pour éviter de sauvegarder à nouveau
        }

        // If no messages, display a welcome message
        if (messages.isEmpty()) {
            Message welcomeMsg = new Message("SYSTEM", 0, currentUserId,
                    "Start a new conversation with " + getUsernameById(selectedUserId));
            displayMessageOnly(welcomeMsg); // Utiliser displayMessageOnly pour éviter de sauvegarder
        }

        // Mark messages as read
        messageDB.markMessagesAsRead(currentUserId, selectedUserId);

        // Scroll to bottom
        Platform.runLater(() -> scrollToBottom());
    }

    private String getUsernameById(int userId) {
        for (User user : userList) {
            if (user.getUserId() == userId) {
                return user.getUsername();
            }
        }
        return "User " + userId;
    }

    private void sendMessage() {
        if (selectedUserId == -1) {
            showAlert("Error", "Please select a user to chat with");
            return;
        }

        String content = messageField.getText().trim();
        if (content.isEmpty()) {
            return;
        }

        // Vérifier si le client est connecté
        if (client == null || !client.isConnected()) {
            System.err.println("WebSocket client is not connected. Attempting to reconnect...");

            // Display reconnecting message
            Message systemMsg = new Message("SYSTEM", 0, currentUserId,
                    "Not connected to server. Attempting to reconnect...");
            displayMessageOnly(systemMsg); // Utiliser displayMessageOnly pour éviter de sauvegarder

            // Try to reconnect
            connectToServer();

            // Wait a bit and check if we're connected
            new Thread(() -> {
                try {
                    Thread.sleep(2000);

                    Platform.runLater(() -> {
                        if (client != null && client.isConnected()) {
                            // Now we're connected, try to send the message
                            sendMessageInternal(content);
                        } else {
                            // Still not connected
                            Message errorMsg = new Message("SYSTEM", 0, currentUserId,
                                    "Could not connect to server. Please try again later.");
                            displayMessageOnly(errorMsg); // Utiliser displayMessageOnly pour éviter de sauvegarder
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            return;
        }

        // We're connected, send the message
        sendMessageInternal(content);
    }

    private void sendMessageInternal(String content) {
        try {
            // Create message object
            Message msg = new Message("MESSAGE", currentUserId, selectedUserId, content);

            // Sauvegarder le message dans la base de données
            boolean saved = messageDB.saveMessage(msg);
            if (!saved) {
                System.err.println("Failed to save message to database");
            }

            // Send message via WebSocket
            client.sendMessage(selectedUserId, content);

            // Display the sent message in our own chat
            displayMessageOnly(msg); // Utiliser displayMessageOnly pour éviter de sauvegarder à nouveau

            // Clear the input field
            messageField.clear();

            // Scroll to bottom
            Platform.runLater(() -> scrollToBottom());
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();

            // Display error message
            Message errorMsg = new Message("SYSTEM", 0, currentUserId,
                    "Failed to send message: " + e.getMessage());
            displayMessageOnly(errorMsg); // Utiliser displayMessageOnly pour éviter de sauvegarder
        }
    }

    // Méthode pour afficher un message sans le sauvegarder
    private void displayMessageOnly(Message message) {
        Platform.runLater(() -> {
            // Only display messages that are relevant to the current chat
            if (message.getType().equals("SYSTEM") ||
                    (message.getSenderId() == selectedUserId && message.getRecipientId() == currentUserId) ||
                    (message.getSenderId() == currentUserId && message.getRecipientId() == selectedUserId)) {

                HBox messageContainer = new HBox(10);

                // Create message bubble
                TextFlow bubble = new TextFlow();
                Text text = new Text(message.getContent());
                bubble.getChildren().add(text);

                // Style based on sender
                if (message.getType().equals("SYSTEM")) {
                    bubble.getStyleClass().add("system-message");
                    messageContainer.setAlignment(Pos.CENTER);
                } else if (message.getSenderId() == currentUserId) {
                    // Message envoyé par l'utilisateur actuel -> aligné à droite
                    bubble.getStyleClass().add("sent-message");
                    messageContainer.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    // Message reçu -> aligné à gauche
                    bubble.getStyleClass().add("received-message");
                    messageContainer.setAlignment(Pos.CENTER_LEFT);
                }

                // Add timestamp
                Text timeText = new Text(message.getTimestamp().format(timeFormatter));
                timeText.getStyleClass().add("time-text");

                // Ajouter le timestamp à gauche pour les messages reçus
                // et à droite pour les messages envoyés
                if (message.getSenderId() == currentUserId) {
                    messageContainer.getChildren().addAll(timeText, bubble);
                } else {
                    messageContainer.getChildren().addAll(bubble, timeText);
                }

                chatBox.getChildren().add(messageContainer);
            }
        });
    }

    // Méthode pour afficher un message et gérer les notifications
    public void displayMessage(Message message) {
        // Si c'est un message normal (pas système) et qu'il vient d'un autre utilisateur
        if (!message.getType().equals("SYSTEM") && message.getSenderId() != currentUserId) {
            // Sauvegarder le message reçu dans la base de données
            boolean saved = messageDB.saveMessage(message);
            if (!saved) {
                System.err.println("Failed to save received message to database");
            }

            // Marquer comme non lu si ce n'est pas l'utilisateur actuellement sélectionné
            if (message.getSenderId() != selectedUserId) {
                unreadMessages.put(message.getSenderId(), true);
                userListView.refresh();

                // Notifier l'utilisateur du nouveau message
                notifyNewMessage(message.getSenderId());
            }
        }

        // Afficher le message s'il est pertinent pour la conversation actuelle
        displayMessageOnly(message);
    }

    // Vérifier si un utilisateur est actuellement sélectionné
    public boolean isUserSelected(int userId) {
        return selectedUserId == userId;
    }

    // Notifier l'utilisateur d'un nouveau message
    public void notifyNewMessage(int senderId) {
        String senderName = getUsernameById(senderId);

        // Créer une notification visuelle (par exemple, une alerte)
        Platform.runLater(() -> {
            Alert notification = new Alert(Alert.AlertType.INFORMATION);
            notification.setTitle("New Message");
            notification.setHeaderText("New message from " + senderName);
            notification.setContentText("You have received a new message. Click on the user to view it.");
            notification.show();
        });
    }

    // Scroll chat to bottom
    private void scrollToBottom() {
        //chatScrollPane.setVvalue(1.0);
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // Custom cell for user list
    private class UserListCell extends ListCell<User> {
        @Override
        protected void updateItem(User user, boolean empty) {
            super.updateItem(user, empty);

            if (empty || user == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox container = new HBox(10);
                Label nameLabel = new Label(user.getUsername());

                // Create status indicator
                javafx.scene.shape.Circle statusIndicator = UserStatusCircle.createStatusCircle(5);

                // Si l'utilisateur a des messages non lus, changer la couleur de l'indicateur
                if (unreadMessages.getOrDefault(user.getUserId(), false)) {
                    statusIndicator.setFill(Color.RED);
                    nameLabel.setStyle("-fx-font-weight: bold;");
                } else {
                    statusIndicator.setFill(Color.GRAY);
                    nameLabel.setStyle("");
                }

                container.getChildren().addAll(statusIndicator, nameLabel);
                setGraphic(container);
            }
        }
    }

    // Ajouter une méthode pour recharger les conversations
    //public void reloadConversations() {
    //    System.out.println("Reloading conversations");
    //    loadConversations();
    //}

    // Ajouter une méthode getter pour le client
    public ChatClient getClient() {
        return client;
        //update hadil
    }

    
    /**
     * Définit l'utilisateur cible avec lequel on veut discuter
     * @param userId L'ID de l'utilisateur cible
     * @param username Le nom d'utilisateur de l'utilisateur cible
     */
    public void setTargetUser(int userId, String username) {
        this.targetUserId = userId;
        this.targetUsername = username;
        System.out.println("Target user set: " + username + " (ID: " + userId + ")");
        
        // Si la liste des utilisateurs est déjà chargée, sélectionner l'utilisateur cible
        if (userListView != null && userListView.getItems() != null && !userListView.getItems().isEmpty()) {
            selectTargetUserInList();
        }
    }
    
    /**
     * Sélectionne l'utilisateur cible dans la liste des contacts
     */
    private void selectTargetUserInList() {
        if (targetUserId <= 0 || userListView == null) {
            return;
        }
        
        // Parcourir la liste des utilisateurs pour trouver l'utilisateur cible
        for (int i = 0; i < userListView.getItems().size(); i++) {
            User user = userListView.getItems().get(i);
            if (user.getUserId() == targetUserId) {
                // Sélectionner l'utilisateur dans la liste
                userListView.getSelectionModel().select(i);
                userListView.scrollTo(i);
                System.out.println("Target user selected in list: " + user.getUsername());
                return;
            }
        }
        
        // Si l'utilisateur n'est pas trouvé dans la liste, afficher un message
        System.out.println("Target user not found in the list: " + targetUsername);
    }
    //fin update 

    // Modifier la méthode displayMessage pour utiliser processReceivedMessage
    //public void displayMessage(Message message) {
    //    processReceivedMessage(message);
    //}

}