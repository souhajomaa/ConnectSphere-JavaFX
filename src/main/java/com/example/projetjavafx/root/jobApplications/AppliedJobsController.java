package com.example.projetjavafx.root.jobApplications;

import com.example.projetjavafx.root.events.Event;
import com.example.projetjavafx.root.organizer.Job;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AppliedJobsController {
    // Existing job-related @FXML elements
    @FXML private TableView<Job> jobsTable;
    @FXML private TableColumn<Job, Number> jobIdColumn;
    @FXML private TableColumn<Job, String> jobTitleColumn;
    @FXML private TableColumn<Job, String> eventTitleColumn;
    @FXML private TableColumn<Job, String> jobLocationColumn;
    @FXML private TableColumn<Job, String> applicationDeadlineColumn;
    @FXML private TableColumn<Job, String> recruiterColumn;
    @FXML private TableColumn<Job, Void> actionColumn;

    // New event-related @FXML elements
    @FXML private TableView<Event> eventsTable;
    @FXML private TableColumn<Event, String> eventNameColumn;
    @FXML private TableColumn<Event, String> eventLocationColumn;
    @FXML private TableColumn<Event, String> startDateColumn;
    @FXML private TableColumn<Event, String> endDateColumn;
    @FXML private TableColumn<Event, Void> cancelColumn;

    private final ObservableList<Job> jobs = FXCollections.observableArrayList();
    private final ObservableList<Event> events = FXCollections.observableArrayList();
    private int userId;

    @FXML
    public void initialize() {
        setupJobTableColumns();
        setupEventTableColumns();
        loadJobsAppliedByUser();
        loadParticipatedEvents();
    }

    // Existing job table setup
    private void setupJobTableColumns() {
        jobIdColumn.setCellValueFactory(new PropertyValueFactory<>("jobId"));
        jobTitleColumn.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        eventTitleColumn.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));
        jobLocationColumn.setCellValueFactory(new PropertyValueFactory<>("jobLocation"));
        applicationDeadlineColumn.setCellValueFactory(new PropertyValueFactory<>("applicationDeadline"));
        recruiterColumn.setCellValueFactory(new PropertyValueFactory<>("recruiterName"));

        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            {
                viewButton.setOnAction(event -> {
                    Job job = getTableView().getItems().get(getIndex());
                    handleViewJobDetails(job);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });
        jobsTable.setItems(jobs);
    }

    // New event table setup
    private void setupEventTableColumns() {
        eventNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        eventLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        cancelColumn.setCellFactory(column -> new TableCell<>() {
            private final Button cancelButton = new Button("Cancel");
            {
                cancelButton.setOnAction(event -> {
                    Event participatedEvent = getTableView().getItems().get(getIndex());
                    handleCancelParticipation(participatedEvent);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : cancelButton);
            }
        });
        eventsTable.setItems(events);
    }

    // Existing job loading
    public void setUserId(int userId) {
        this.userId = userId;
        loadJobsAppliedByUser();
    }

    private void loadJobsAppliedByUser() {
        try {
            List<Job> jobsList = AppliedJobsRepository.getJobsAppliedByUser(userId);
            jobs.setAll(jobsList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load applied jobs", e.getMessage());
        }
    }

    // New event loading
    private void loadParticipatedEvents() {
        try {
            List<Event> eventsList = AppliedJobsRepository.getParticipatedEvents();
            events.setAll(eventsList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load events", e.getMessage());
        }
    }

    // Existing job details handler
    private void handleViewJobDetails(Job job) {
        try {
            String status = AppliedJobsRepository.getApplicationStatusForJob(userId, job.getJobId());
            String message;
            switch (status.toLowerCase()) {
                case "accepted": message = "Congrats, you have been accepted!"; break;
                case "rejected": message = "Sorry, you have been rejected."; break;
                case "pending": message = "Your application is still pending."; break;
                default: message = "Status: " + status;
            }
            showAlert(Alert.AlertType.INFORMATION, "Application Status", null, message);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", null, e.getMessage());
        }
    }

    // New cancellation handler
    private void handleCancelParticipation(Event event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Participation");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will permanently cancel your participation in: " + event.getName());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = AppliedJobsRepository.cancelParticipation(event.getEventId());
                    if (success) {
                        events.remove(event);
                        showAlert(Alert.AlertType.INFORMATION, "Success", null, "Participation canceled!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", null, "Cancellation failed.");
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", null, e.getMessage());
                }
            }
        });
    }

    // Existing navigation methods
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
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

    @FXML protected void onDashboardClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/organizer-view.fxml", event);
    }

    @FXML protected void onEventsClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/events/events-view.fxml", event);
    }

    @FXML protected void onAnalyticsClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/analytics-view.fxml", event);
    }

    public void onJobApplicationsButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/JobApplications/application_review-view.fxml", event);
    }

    public void onJobFeedButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/jobfeed/job-feed-view.fxml", event);
    }

    public void onCreateJobButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/create-job-offer-view.fxml", event);
    }

    public void onHomeButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/root/root-view.fxml", event);
    }
}