package com.example.projetjavafx.root.group;

import com.example.projetjavafx.root.auth.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Optional;

public class GroupProfileController {

    @FXML
    private TextField searchField;
    @FXML
    private FlowPane groupsContainer;

    private GroupProfileRepository groupModel = new GroupProfileRepository(null, null, null, null);
    private GroupAddMemberRepository groupMemberModel = new GroupAddMemberRepository();

    @FXML
    public void initialize() {
        loadGroups("");
        // Listener pour filtrer les groupes lors de la saisie
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadGroups(newValue);
        });
    }

    private void loadGroups(String searchText) {
        groupsContainer.getChildren().clear(); // Efface les groupes existants

        try (ResultSet resultSet = groupModel.getGroups(searchText)) {
            if (resultSet == null) {
                showAlert("Erreur", "Problème de connexion à la base de données.");
                return;
            }

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                String rules = resultSet.getString("rules");
                String imageBase64 = resultSet.getString("profile_picture");

                // Créer une carte de groupe
                VBox groupBox = new VBox(10);
                groupBox.getStyleClass().add("group-card");

                Text groupText = new Text(name + "\n" + description + "\n" + rules);
                groupText.getStyleClass().add("group-description");

                ImageView imageView = new ImageView();
                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
                    Image image = new Image(new ByteArrayInputStream(imageBytes));
                    imageView.setImage(image);
                    imageView.setFitWidth(200);
                    imageView.setPreserveRatio(true);
                }
                imageView.getStyleClass().add("group-image");

                // Bouton "View Details"
                Button viewDetailsButton = new Button("View Details");
                viewDetailsButton.getStyleClass().add("group-button");
                viewDetailsButton.setOnAction(event -> showGroupDetails(name, description, rules, imageBase64));

                // Bouton "Add Member"
                Button addMemberButton = new Button("Add Member");
                addMemberButton.getStyleClass().add("group-button-secondary");
                addMemberButton.setOnAction(event -> showAddMemberDialog(name));

                // Bouton "Rejoindre"
                Button joinButton = new Button("Join");
                joinButton.getStyleClass().add("group-button-secondary");

                // Bouton "Modifier"
                Button updateButton = new Button("Update");
                updateButton.getStyleClass().add("group-button-secondary");

                // Bouton "Supprimer"
                Button deleteButton = new Button("Delete");
                deleteButton.getStyleClass().add("group-button-secondary");

                // Vérifier si l'utilisateur est déjà membre ou administrateur
                int userId = SessionManager.getInstance().getCurrentUserId();
                int groupId = groupModel.getGroupIdByName(name);

                boolean isAdmin = groupMemberModel.isUserGroupCreator(groupId, userId);
                boolean isMember = groupMemberModel.isUserMemberOfGroup(groupId, userId);

                if (userId != -1 && groupId != -1) {
                    if (isAdmin) {
                        deleteButton.setOnAction(event -> deleteGroup(groupId));
                        updateButton.setOnAction(event -> updateGroup(groupId));
                    } else {
                        deleteButton.setVisible(false);
                        updateButton.setVisible(false);
                    }

                    if (isMember) {
                        joinButton.setDisable(true);
                        joinButton.setText("Already a member");
                    } else {
                        joinButton.setOnAction(event -> joinGroup(name));
                    }
                }

                // Bouton "Afficher les membres"
                Button showMembersButton = new Button("Show members");
                showMembersButton.getStyleClass().add("group-button-secondary");
                showMembersButton.setOnAction(event -> showGroupMembers(name));

                // Conteneur des boutons
                VBox buttonContainer = new VBox(10, viewDetailsButton, addMemberButton, showMembersButton, joinButton, updateButton, deleteButton);
                buttonContainer.getStyleClass().add("group-buttons");

                // Ajout des éléments à la carte
                groupBox.getChildren().addAll(imageView, groupText, buttonContainer);
                groupsContainer.getChildren().add(groupBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showGroupDetails(String name, String description, String rules, String imageBase64) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/group/group-details.fxml"));
            Parent root = loader.load();

            GroupDetailsController controller = loader.getController();
            controller.setGroupDetails(name, description, rules, imageBase64);

            Stage stage = new Stage();
            Scene scene = new Scene(root);

            // Ajouter le fichier CSS si nécessaire
            String cssFile = getClass().getResource("/com/example/projetjavafx/group/css/group_profile.css") != null ?
                    getClass().getResource("/com/example/projetjavafx/group/css/group_profile.css").toExternalForm() : null;

            if (cssFile != null) {
                scene.getStylesheets().add(cssFile);
            } else {
                System.out.println("⚠ Fichier CSS non trouvé !");
            }

            stage.setScene(scene);
            stage.setTitle("Group Details");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onNewGroupClick() {
        loadView("/com/example/projetjavafx/group/group-add-view.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) groupsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAddMemberDialog(String groupName) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Member to " + groupName);

        TextField searchUserField = new TextField();
        searchUserField.setPromptText("Search user...");

        ListView<String> userListView = new ListView<>();

        searchUserField.textProperty().addListener((observable, oldValue, newValue) -> {
            try (ResultSet resultSet = groupMemberModel.searchUsers(newValue)) {
                userListView.getItems().clear();
                while (resultSet != null && resultSet.next()) {
                    String username = resultSet.getString("username");
                    userListView.getItems().add(username);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Button addButton = new Button("Add");
        addButton.setOnAction(event -> {
            String selectedUser = userListView.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                int groupId = groupModel.getGroupIdByName(groupName);
                int userId = groupModel.getUserIdByUsername(selectedUser); // Utilisation du repository

                if (groupId != -1 && userId != -1) {
                    if (groupMemberModel.isUserMemberOfGroup(groupId, userId)) {
                        showAlert("Error", "This user is already a member of the group.");
                        return;
                    }

                    boolean isCreator = groupMemberModel.isUserGroupCreator(groupId, userId);
                    String role = isCreator ? "admin" : "member";

                    boolean success = groupMemberModel.addMemberToGroup(groupId, userId, role);
                    if (success) {
                        showAlert("Success", "Member added successfully.");
                        dialog.close();
                    } else {
                        showAlert("Error", "Failed to add member.");
                    }
                }
            } else {
                showAlert("Error", "Please select a user.");
            }
        });

        VBox dialogVBox = new VBox(10, searchUserField, userListView, addButton);
        dialog.getDialogPane().setContent(dialogVBox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showGroupMembers(String groupName) {
        try {
            int groupId = groupModel.getGroupIdByName(groupName);
            if (groupId == -1) {
                showAlert("Error", "Group not found.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/group/group-members-view.fxml"));
            Parent root = loader.load();

            GroupMembersController controller = loader.getController();
            controller.setGroupId(groupId);
            controller.loadMembers();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Group Members: " + groupName);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unable to display group members.");
        }
    }

    private void joinGroup(String groupName) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == -1) {
            showAlert("Error", "User not logged in.");
            return;
        }

        int groupId = groupModel.getGroupIdByName(groupName);
        if (groupId == -1) {
            showAlert("Error", "Group not found.");
            return;
        }

        if (groupMemberModel.isUserMemberOfGroup(groupId, userId)) {
            showAlert("Error", "You are already a member of this group.");
            return;
        }

        boolean success = groupMemberModel.addMemberToGroup(groupId, userId, "member");
        if (success) {
            showAlert("Success", "You have successfully joined the group.");
            loadGroups("");
        } else {
            showAlert("Error", "Unable to join the group.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void deleteGroup(int groupId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this group?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = groupModel.deleteGroup(groupId);
            if (success) {
                showAlert("Success", "Group deleted successfully.");
                loadGroups("");
            } else {
                showAlert("Error", "Unable to delete the group.");
            }
        }
    }

    private void updateGroup(int groupId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/group/group-update-view.fxml"));
            Parent root = loader.load();

            GroupUpdateController controller = loader.getController();
            controller.setGroupId(groupId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Update group");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unable to open the group edit view.");
        }
    }

    // New navigation methods
    @FXML
    public void onOrganizerButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/organizer-view.fxml");
    }

    @FXML
    public void onEventsClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/events/events-view.fxml");
    }

    @FXML
    public void onGroupButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/group/group-profile-view.fxml");
    }

    @FXML
    public void onJobFeedClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/jobfeed/job-feed-view.fxml");
    }

    @FXML
    public void onCreateJobClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/create-job-offer-view.fxml");
    }

    @FXML
    public void onProfileClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/profile/profile-view.fxml");
    }

    @FXML
    public void onsocialButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/social/Feed.fxml");
    }

    @FXML
    public void onMessagerieClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/messagerie/discussion.fxml");
    }

    @FXML
    public void onPointClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/points/Home.fxml");
    }

    @FXML
    public void onChatbotClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/chatbot/chatbot-view.fxml");
    }

    public void handleHomeButton(ActionEvent event) {
        loadView("/com/example/projetjavafx/root/root-view.fxml");
    }



}



