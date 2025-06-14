package com.example.projetjavafx.root.events;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import com.example.projetjavafx.root.auth.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class CreateEventController {

    // Existing fields remain unchanged
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ImageView imagePreview;
    private String base64Image;

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

    // Existing methods remain unchanged
    @FXML
    public void initialize() {
        loadCategories();
    }

    @FXML
    public void onUploadImageClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                base64Image = Base64.getEncoder().encodeToString(fileContent);
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onSaveEventClick() {
        String name = nameField.getText();
        String description = descriptionField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String location = locationField.getText();
        String category = categoryComboBox.getValue();

        if (name.isEmpty() || startDate == null || endDate == null
                || category == null || base64Image == null) {
            System.out.println("Veuillez remplir tous les champs.");
            return;
        }

        if (!startDate.isBefore(endDate)) {
            showAlert("Invalid Date", "The start date must be before the end date.");
            return;
        }

        int organizerId = SessionManager.getInstance().getCurrentUserId();
        int categoryId = Integer.parseInt(category.split(" - ")[0]);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (Connection connection = AivenMySQLManager.getConnection()) {
            String query = "INSERT INTO events (name, description, start_time, end_time, location, organizer_id_id, category_id_id, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setString(3, startDate.format(formatter));
            statement.setString(4, endDate.format(formatter));
            statement.setString(5, location);
            statement.setInt(6, organizerId);
            statement.setInt(7, categoryId);
            statement.setString(8, base64Image);
            statement.executeUpdate();
            showAlert("Success", "Event created successfully!");
            loadView("/com/example/projetjavafx/events/events-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCategories() {
        ObservableList<String> categories = FXCollections.observableArrayList();
        try (Connection connection = AivenMySQLManager.getConnection()) {
            ResultSet resultSet = connection.createStatement()
                    .executeQuery("SELECT id, name FROM category");
            while (resultSet.next()) {
                categories.add(resultSet.getInt("id") + " - " + resultSet.getString("name"));
            }
            categoryComboBox.setItems(categories);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancelClick() {
        loadView("/com/example/projetjavafx/events/events-view.fxml");
    }

    // Navigation helper method
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}