package com.example.projetjavafx.root.organizer;

import com.example.projetjavafx.root.auth.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class OrganizerController {

    // Event Table Components
    @FXML private TableView<Map<String, String>> eventsTable;
    @FXML private TableColumn<Map<String, String>, String> eventNameColumn;
    @FXML private TableColumn<Map<String, String>, String> eventDescColumn;
    @FXML private TableColumn<Map<String, String>, String> eventStartColumn;
    @FXML private TableColumn<Map<String, String>, String> eventEndColumn;
    @FXML private TableColumn<Map<String, String>, String> eventLocationColumn;
    @FXML private TableColumn<Map<String, String>, Void> deleteEventColumn;
    @FXML private TableColumn<Map<String, String>, Void> modifyEventColumn;

    // Job Table Components
    @FXML private TableView<Map<String, String>> jobsTable;
    @FXML private TableColumn<Map<String, String>, String> jobTitleColumn;
    @FXML private TableColumn<Map<String, String>, String> eventTitleColumn;
    @FXML private TableColumn<Map<String, String>, String> jobLocationColumn;
    @FXML private TableColumn<Map<String, String>, String> employmentTypeColumn;
    @FXML private TableColumn<Map<String, String>, String> applicationDeadlineColumn;
    @FXML private TableColumn<Map<String, String>, String> minSalaryColumn;
    @FXML private TableColumn<Map<String, String>, String> maxSalaryColumn;
    @FXML private TableColumn<Map<String, String>, String> currencyColumn;
    @FXML private TableColumn<Map<String, String>, String> jobDescColumn;
    @FXML private TableColumn<Map<String, String>, String> recruiterNameColumn;
    @FXML private TableColumn<Map<String, String>, String> recruiterEmailColumn;
    @FXML private TableColumn<Map<String, String>, String> createdAtColumn;
    @FXML private TableColumn<Map<String, String>, Void> deleteJobColumn;
    @FXML private TableColumn<Map<String, String>, Void> modifyJobColumn;

    // Participant Table Components
    @FXML private TableView<Map<String, String>> participantsTable;
    @FXML private TableColumn<Map<String, String>, String> participantNameColumn;
    @FXML private TableColumn<Map<String, String>, String> participantAgeColumn;
    @FXML private TableColumn<Map<String, String>, String> participantGenderColumn;
    @FXML private TableColumn<Map<String, String>, Void> deleteParticipantColumn;

    @FXML private ComboBox<String> eventDropdown; // Dropdown to select events

    private Map<String, Integer> eventIdMap = new HashMap<>(); // To map event names to IDs

    // Current user ID (for demonstration, using 1)
    SessionManager sessionManager = SessionManager.getInstance();
    int currentUserId = sessionManager.getCurrentUserId();


    public void initialize() {
        setupEventTableColumns();
        setupJobTableColumns();
        setupEventActionColumns();
        setupJobActionColumns();
        setupParticipantTableColumns();
        setupEventDropdown();
        loadData();
    }

    private void setupEventTableColumns() {
        eventNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("name")));
        eventDescColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("description")));
        eventStartColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("start_time")));
        eventEndColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("end_time")));
        eventLocationColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("location")));
    }

    private void setupJobTableColumns() {
        jobTitleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("job_title")));
        eventTitleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("event_title")));
        jobLocationColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("job_location")));
        employmentTypeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("employment_type")));
        applicationDeadlineColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("application_deadline")));
        minSalaryColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("min_salary")));
        maxSalaryColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("max_salary")));
        currencyColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("currency")));
        jobDescColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("job_description")));
        recruiterNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("recruiter_name")));
        recruiterEmailColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("recruiter_email")));
        createdAtColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("created_at")));
    }

    private void setupEventActionColumns() {
        // Delete button for events
        deleteEventColumn.setCellFactory(col -> new TableCell<Map<String, String>, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(event -> {
                    Map<String, String> eventData = getTableView().getItems().get(getIndex());
                    deleteEvent(eventData);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        // Modify button for events
        modifyEventColumn.setCellFactory(col -> new TableCell<Map<String, String>, Void>() {
            private final Button modifyButton = new Button("Modify");
            {
                modifyButton.setOnAction(event -> {
                    Map<String, String> eventData = getTableView().getItems().get(getIndex());
                    openModifyEventPopup(eventData);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : modifyButton);
            }
        });
    }

    private void setupJobActionColumns() {
        // Delete button for jobs
        deleteJobColumn.setCellFactory(col -> new TableCell<Map<String, String>, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(event -> {
                    Map<String, String> jobData = getTableView().getItems().get(getIndex());
                    deleteJob(jobData);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        // Modify button for jobs
        modifyJobColumn.setCellFactory(col -> new TableCell<Map<String, String>, Void>() {
            private final Button modifyButton = new Button("Modify");
            {
                modifyButton.setOnAction(event -> {
                    Map<String, String> jobData = getTableView().getItems().get(getIndex());
                    openModifyJobPopup(jobData);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : modifyButton);
            }
        });
    }

    private void setupParticipantTableColumns() {
        participantNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("name")));
        participantAgeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("age")));
        participantGenderColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("gender")));

        // Set up the delete button column
        deleteParticipantColumn.setCellFactory(col -> new TableCell<Map<String, String>, Void>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    Map<String, String> participantData = getTableView().getItems().get(getIndex());
                    deleteParticipant(participantData);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
    }

    private void setupEventDropdown() {
        try {
            List<Map<String, String>> events = OrganizerRepository.getOrganizerEvents(currentUserId);
            ObservableList<String> eventNames = FXCollections.observableArrayList();
            for (Map<String, String> event : events) {
                String eventName = event.get("name");
                int eventId = Integer.parseInt(event.get("event_id"));
                eventNames.add(eventName);
                eventIdMap.put(eventName, eventId); // Map event name to ID
            }
            eventDropdown.setItems(eventNames);
            eventDropdown.setOnAction(this::onEventSelected);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load events for dropdown.");
        }
    }

    private void onEventSelected(ActionEvent event) {
        String selectedEvent = eventDropdown.getValue();
        if (selectedEvent != null) {
            int eventId = eventIdMap.get(selectedEvent);
            loadParticipants(eventId);
        }
    }

    private void loadParticipants(int eventId) {
        try {
            List<Map<String, String>> participants = OrganizerRepository.getParticipantsForEvent(eventId);
            ObservableList<Map<String, String>> participantItems = FXCollections.observableArrayList(participants);
            participantsTable.setItems(participantItems);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load participants.");
        }
    }

    @FXML
    public void onRefreshParticipants(ActionEvent event) {
        String selectedEvent = eventDropdown.getValue();
        if (selectedEvent != null) {
            int eventId = eventIdMap.get(selectedEvent);
            loadParticipants(eventId);
        }
    }

    private void loadEvents() {
        try {
            List<Map<String, String>> events = OrganizerRepository.getOrganizerEvents(currentUserId);
            ObservableList<Map<String, String>> eventItems = FXCollections.observableArrayList(events);
            eventsTable.setItems(eventItems);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load events.");
        }
    }

    private void loadJobs() {
        try {
            List<Map<String, String>> jobs = OrganizerRepository.getOrganizerJobs(currentUserId);
            ObservableList<Map<String, String>> jobItems = FXCollections.observableArrayList(jobs);
            jobsTable.setItems(jobItems);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load jobs.");
        }
    }

    private void loadData() {
        loadEvents();
        loadJobs();
    }

    private void deleteEvent(Map<String, String> eventData) {
        try {
            int eventId = Integer.parseInt(eventData.get("event_id"));
            boolean success = OrganizerRepository.deleteOrganizerEvent(currentUserId, eventId);
            if (success) {
                eventsTable.getItems().remove(eventData);
                System.out.println("Event deleted from database: " + eventData.get("name"));
            } else {
                showAlert("Error", "Failed to delete event from database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while deleting event.");
        }
    }

    private void deleteJob(Map<String, String> jobData) {
        try {
            boolean success = OrganizerRepository.deleteOrganizerJob(currentUserId, jobData.get("job_title"));
            if (success) {
                jobsTable.getItems().remove(jobData);
                System.out.println("Job deleted from database: " + jobData.get("job_title"));
            } else {
                showAlert("Error", "Failed to delete job from database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while deleting job.");
        }
    }

    private void deleteParticipant(Map<String, String> participantData) {
        try {
            String participantName = participantData.get("name");
            int eventId = eventIdMap.get(eventDropdown.getValue()); // Get the selected event ID
            boolean success = OrganizerRepository.deleteParticipant(eventId, participantName);
            if (success) {
                participantsTable.getItems().remove(participantData);
                System.out.println("Participant deleted from database: " + participantName);
            } else {
                showAlert("Error", "Failed to delete participant from database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while deleting participant.");
        }
    }

    private void openModifyEventPopup(Map<String, String> eventData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/organizer/modify-event-popup.fxml"));
            Parent root = loader.load();
            ModifyEventController controller = loader.getController();
            controller.setEventData(eventData);
            Stage popupStage = new Stage();
            popupStage.setTitle("Modify Event");
            popupStage.setScene(new Scene(root));
            popupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load modify event popup.");
        }
    }

    private void openModifyJobPopup(Map<String, String> jobData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/organizer/modify-job-popup.fxml"));
            Parent root = loader.load();
            ModifyJobController controller = loader.getController();
            controller.setJobData(jobData);
            Stage popupStage = new Stage();
            popupStage.setTitle("Modify Job Post");
            popupStage.setScene(new Scene(root));
            popupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load modify job popup.");
        }
    }

    @FXML
    public void onRefreshEvents(ActionEvent event) {
        loadEvents();
    }

    @FXML
    public void onRefreshJobs(ActionEvent event) {
        loadJobs();
    }

    @FXML
    public void onNewJobPostClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/organizer/create-job-offer-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Button)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onCreateJobClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/organizer/create-job-offer-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Button)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHomeButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/root/root-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onAnalyticsClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/organizer/analytics-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onJobFeedButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/jobfeed/job-feed-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onReviewApplicationsButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/JobApplications/application_review-view.fxml"));
            Parent root = loader.load();
            Scene scene = ((Button) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load Job Applications View.");
        }
    }

    public void onAppliedAtButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/jobApplications/applied-jobs.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void onCreateEventClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/events/create-events.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}