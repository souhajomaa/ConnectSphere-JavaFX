package com.example.projetjavafx.root.jobFeed;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.sql.SQLException;

public class ApplicationFormController {
    @FXML private TextArea coverLetterArea;
    @FXML private TextField resumePathField;
    @FXML private Button browseButton;
    @FXML private Button submitButton;

    private int userId;
    private int jobId;

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    @FXML
    public void initialize() {
        // Set up the browse button action
        browseButton.setOnAction(e -> handleBrowse());

        // Set up the submit button action
        submitButton.setOnAction(e -> handleSubmit());
    }

    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Resume File");

        // Set extension filter for PDF files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show the file chooser dialog
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            // Set the selected file path to the text field
            resumePathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSubmit() {
        // Get the input values
        String coverLetter = coverLetterArea.getText().trim();
        String resumePath = resumePathField.getText().trim();

        // Validate input fields
        if (coverLetter.isEmpty()) {
            showAlert("Error", "Please write a cover letter.");
            return;
        }

        if (resumePath.isEmpty()) {
            showAlert("Error", "Please select a resume file.");
            return;
        }

        // Check if the resume file exists
        File resumeFile = new File(resumePath);
        if (!resumeFile.exists()) {
            showAlert("Error", "The selected resume file does not exist.");
            return;
        }

        try {
            // Save the application to the database
            ApplicationRepository.saveApplication(userId, jobId, coverLetter, resumePath);

            // Show success message
            showAlert("Success", "Application submitted successfully!");

            // Clear the form after submission
            clearForm();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to submit application: " + e.getMessage());
        }
    }

    private void clearForm() {
        // Clear the form fields
        coverLetterArea.clear();
        resumePathField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}