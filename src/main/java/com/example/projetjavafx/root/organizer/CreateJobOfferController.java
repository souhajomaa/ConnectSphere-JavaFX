package com.example.projetjavafx.root.organizer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class CreateJobOfferController {

    @FXML
    private TextField jobTitleField;

    @FXML
    private TextField eventTitleField;

    @FXML
    private TextField jobLocationField;

    @FXML
    private ComboBox<String> employmentTypeComboBox;

    @FXML
    private DatePicker applicationDeadlinePicker;

    @FXML
    private TextField minSalaryField;

    @FXML
    private TextField maxSalaryField;

    @FXML
    private ComboBox<String> currencyComboBox;

    @FXML
    private TextArea jobDescriptionArea;

    @FXML
    private TextField recruiterNameField;

    @FXML
    private TextField recruiterEmailField;

    @FXML
    private Button postJobButton;

    @FXML
    private GridPane formGridPane;

    @FXML
    public void initialize() {

        // Initialize ComboBoxes
        employmentTypeComboBox.getItems().addAll("Full-Time", "Part-Time", "Contract", "Internship");
        currencyComboBox.getItems().addAll("USD", "EUR", "TND");

        // Set default values
        employmentTypeComboBox.setValue("Full-Time");
        currencyComboBox.setValue("USD");

        // Add event handlers
        postJobButton.setOnAction(event -> handlePostJob());

    }

    @FXML
    private void handlePostJob() {
        if (!validateForm()) {
            showAlert("Error", "Please fill all required fields.");
            return;
        }

        // Insert job offer into the database
        try {
            String jobTitle = jobTitleField.getText();
            String eventTitle = eventTitleField.getText();
            String jobLocation = jobLocationField.getText();
            String employmentType = employmentTypeComboBox.getValue();
            String applicationDeadline = applicationDeadlinePicker.getValue().toString();
            String minSalary = String.valueOf(Double.parseDouble(minSalaryField.getText()));
            String maxSalary = String.valueOf(Double.parseDouble(maxSalaryField.getText()));
            String currency = currencyComboBox.getValue();
            String jobDescription = jobDescriptionArea.getText();
            String recruiterName = recruiterNameField.getText();
            String recruiterEmail = recruiterEmailField.getText();

            JobRepository.createJob(jobTitle, eventTitle, jobLocation, employmentType, applicationDeadline, minSalary, maxSalary, currency, jobDescription, recruiterName, recruiterEmail);

            showAlert("Success", "Job offer posted successfully!");

        } catch (SQLException e) {
            showAlert("Error", "Failed to post job offer: " + e.getMessage());
        }
    }
    private boolean validateForm() {
        // Check if all required fields are filled
        return !jobTitleField.getText().isEmpty() &&
                !eventTitleField.getText().isEmpty() &&
                !jobLocationField.getText().isEmpty() &&
                applicationDeadlinePicker.getValue() != null &&
                !minSalaryField.getText().isEmpty() &&
                !maxSalaryField.getText().isEmpty() &&
                !jobDescriptionArea.getText().isEmpty() &&
                !recruiterNameField.getText().isEmpty() &&
                !recruiterEmailField.getText().isEmpty();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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

    // New navigation methods
    @FXML
    public void onOrganizerButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/organizer-view.fxml",event);
    }

    @FXML
    public void onEventsClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/events/events-view.fxml",event);
    }

    @FXML
    public void onGroupButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/group/group-profile-view.fxml",event);
    }

    @FXML
    public void onJobFeedClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/jobfeed/job-feed-view.fxml",event);
    }

    @FXML
    public void onCreateJobClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/create-job-offer-view.fxml",event);
    }

    @FXML
    public void onProfileClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/profile/profile-view.fxml",event);
    }

    @FXML
    public void onsocialButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/social/Feed.fxml",event);
    }

    @FXML
    public void onMessagerieClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/messagerie/discussion.fxml",event);
    }

    @FXML
    public void onPointClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/points/Home.fxml",event);
    }

    @FXML
    public void onChatbotClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/chatbot/chatbot-view.fxml",event);
    }

    public void handleHomeButton(ActionEvent event) {
        loadView("/com/example/projetjavafx/root/root-view.fxml",event);
    }}
