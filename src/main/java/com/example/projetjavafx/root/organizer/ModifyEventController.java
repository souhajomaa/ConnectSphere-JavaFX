package com.example.projetjavafx.root.organizer;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.Map;

public class ModifyEventController {

    @FXML private TextField nameField;
    @FXML private TextField descField;
    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private TextField locationField;

    // Store the event's primary key so we can update the correct record
    private int eventId;

    // Called by the main controller to pre-populate the fields
    public void setEventData(Map<String, String> eventData) {
        nameField.setText(eventData.get("name"));
        descField.setText(eventData.get("description"));
        startField.setText(eventData.get("start_time"));
        endField.setText(eventData.get("end_time"));
        locationField.setText(eventData.get("location"));
        eventId = Integer.parseInt(eventData.get("event_id")); // make sure event_id is available in the map
    }

    @FXML
    private void handleSave() {
        try {
            // For demonstration, we use 1 as the currentUserId.
            // Replace this with the actual user ID from your session management.
            int currentUserId = 1;
            boolean updated = OrganizerRepository.updateOrganizerEvent(
                    currentUserId,
                    eventId,
                    nameField.getText(),
                    descField.getText(),
                    startField.getText(),
                    endField.getText(),
                    locationField.getText()
            );
            if (updated) {
                System.out.println("Event updated in DB successfully.");
            } else {
                System.out.println("Failed to update event.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Close the popup window
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
