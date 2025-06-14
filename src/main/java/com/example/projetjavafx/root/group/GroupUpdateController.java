package com.example.projetjavafx.root.group;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class GroupUpdateController {
    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionArea;

    private int groupId;
    private GroupUpdateRepository groupModel = new GroupUpdateRepository(null, null);

    public void setGroupId(int groupId) {
        this.groupId = groupId;
        loadGroupData();
    }

    private void loadGroupData() {
        GroupUpdateRepository groupData = groupModel.loadGroupData(groupId);
        if (groupData != null) {
            nameField.setText(groupData.getName());
            descriptionArea.setText(groupData.getDescription());
        } else {
            showAlert("Error", "Unable to load group data.");
        }
    }

    @FXML
    private void onSaveClick() {
        String name = nameField.getText();
        String description = descriptionArea.getText();

        if (name.isEmpty() || description.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        boolean success = groupModel.updateGroup(groupId, name, description);
        if (success) {
            showAlert("Success", "Group updated successfully.");
            closeWindow();
        } else {
            showAlert("Error", "Unable to update the group.");
        }
    }

    @FXML
    private void onCancelClick() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}