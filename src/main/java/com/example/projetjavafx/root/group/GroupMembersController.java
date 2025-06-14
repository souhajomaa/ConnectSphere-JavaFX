package com.example.projetjavafx.root.group;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.sql.ResultSet;

public class GroupMembersController {

    @FXML
    private ListView<String> membersListView;

    private int groupId; // ID du groupe
    private GroupMembersRepository groupMembersModel = new GroupMembersRepository();

    // Méthode pour définir l'ID du groupe
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    // Méthode pour charger les membres du groupe
    public void loadMembers() {
        ResultSet resultSet = groupMembersModel.getGroupMembers(groupId);
        if (resultSet == null) {
            return;
        }

        membersListView.getItems().clear(); // Effacer les anciens résultats
        try {
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String role = resultSet.getString("role");
                membersListView.getItems().add(username + " - " + role); // Afficher le nom et le rôle
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}