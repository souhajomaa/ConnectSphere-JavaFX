package com.example.projetjavafx.root.organizer;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.Map;

public class ModifyJobController {

    @FXML private TextField titleField;
    @FXML private TextField eventTitleField;
    @FXML private TextField locationField;
    @FXML private TextField employmentField;
    @FXML private TextField deadlineField;
    @FXML private TextField minSalaryField;
    @FXML private TextField maxSalaryField;
    @FXML private TextField currencyField;
    @FXML private TextField descriptionField;
    @FXML private TextField recruiterNameField;
    @FXML private TextField recruiterEmailField;

    // Store the original job title to use as a key in the update query.
    private String originalJobTitle;

    // Called by the main controller to pre-populate the fields
    public void setJobData(Map<String, String> jobData) {
        titleField.setText(jobData.get("job_title"));
        eventTitleField.setText(jobData.get("event_title"));
        locationField.setText(jobData.get("job_location"));
        employmentField.setText(jobData.get("employment_type"));
        deadlineField.setText(jobData.get("application_deadline"));
        minSalaryField.setText(jobData.get("min_salary"));
        maxSalaryField.setText(jobData.get("max_salary"));
        currencyField.setText(jobData.get("currency"));
        descriptionField.setText(jobData.get("job_description"));
        recruiterNameField.setText(jobData.get("recruiter_name"));
        recruiterEmailField.setText(jobData.get("recruiter_email"));
        originalJobTitle = jobData.get("job_title");
    }

    @FXML
    private void handleSave() {
        try {
            int currentUserId = 1; // Replace with the actual user ID
            boolean updated = OrganizerRepository.updateOrganizerJob(
                    currentUserId,
                    originalJobTitle,
                    titleField.getText(),
                    eventTitleField.getText(),
                    locationField.getText(),
                    employmentField.getText(),
                    deadlineField.getText(),
                    minSalaryField.getText(),
                    maxSalaryField.getText(),
                    currencyField.getText(),
                    descriptionField.getText(),
                    recruiterNameField.getText(),
                    recruiterEmailField.getText()
            );
            if (updated) {
                System.out.println("Job updated in DB successfully.");
            } else {
                System.out.println("Failed to update job.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Close the popup window
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
