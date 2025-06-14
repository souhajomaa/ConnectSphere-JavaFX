package com.example.projetjavafx.root.group;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.sql.ResultSet;

public class GroupAddMemberController {
    @FXML
    private TextField searchUserField;

    @FXML
    private ListView<String> userListView;

    private SearchMemberRepository userModel = new SearchMemberRepository();
    private GroupAddMemberRepository groupMembersModel = new GroupAddMemberRepository();
    private int currentGroupId;

    public void setCurrentGroupId(int groupId) {
        this.currentGroupId = groupId;
    }

    @FXML
    private void onSearchUserClick() {
        String searchText = searchUserField.getText();
        if (searchText.isEmpty()) {
            showAlert("Error", "Please enter a name or email to search.");
            return;
        }

        ResultSet resultSet = userModel.searchUsers(searchText);
        if (resultSet == null) {
            showAlert("Error", "Database connection issue.");
            return;
        }

        userListView.getItems().clear();
        try {
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String email = resultSet.getString("email");
                int userId = resultSet.getInt("user_id");

                userListView.getItems().add(username + " (" + email + ") - ID: " + userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddUserToGroupClick() {
        String selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Error", "Please select a user.");
            return;
        }

        int userId = Integer.parseInt(selectedUser.split(" - ID: ")[1]);

        if (currentGroupId <= 0) {
            showAlert("Error", "No group selected.");
            return;
        }

        // Check if the user is already a member of the group
        if (groupMembersModel.isUserMemberOfGroup(currentGroupId, userId)) {
            showAlert("Error", "This user is already a member of the group.");
            return;
        }

        // Vérifier si l'utilisateur est le créateur du groupe
        boolean isCreator = groupMembersModel.isUserGroupCreator(currentGroupId, userId);

        // Attribuer le rôle automatiquement
        String role = isCreator ? "admin" : "member";

        boolean success = groupMembersModel.addMemberToGroup(currentGroupId, userId, role);
        if (success) {
            showAlert("Success", "User added to the group successfully.");
        } else {
            showAlert("Error", "Failed to add the user to the group.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}