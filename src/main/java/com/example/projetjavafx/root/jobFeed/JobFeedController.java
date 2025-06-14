package com.example.projetjavafx.root.jobFeed;

import com.example.projetjavafx.root.auth.SessionManager;
import com.example.projetjavafx.root.organizer.Job;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class JobFeedController {
    public Button createJobButton;
    public Button jobApplicationsButton;
    public Button analyticsButton;
    @FXML private FlowPane jobsContainer;
    @FXML private Button homeButton, profileButton, logoutButton, dashboardButton, eventsButton;
    // using eya's session manager and singelton Class
    SessionManager sessionManager = SessionManager.getInstance();
    int currentUserId = sessionManager.getCurrentUserId();

    @FXML
    public void initialize() {
        try {
            loadJobs();
        } catch (SQLException e) {
            e.printStackTrace();
        }

//        // Button actions
//        homeButton.setOnAction(this::onHomeClick);
//        profileButton.setOnAction(this::onProfileClick);
//        logoutButton.setOnAction(this::onLogoutClick);
//        dashboardButton.setOnAction(this::onDashboardClick);
//        eventsButton.setOnAction(this::onEventsClick);
//        createJobButton.setOnAction(this::onCreateJobButtonClick);
    }

    private void loadJobs() throws SQLException {
        List<Job> jobs = JobFeedRepository.getAllJobs();
        jobsContainer.getChildren().clear();

        for (Job job : jobs) {
            VBox card = createJobCard(job);
            jobsContainer.getChildren().add(card);
        }

        jobsContainer.setPrefWrapLength(5 * 270); // Ensures 5 cards per row
    }

    private VBox createJobCard(Job job) {
        VBox card = new VBox();
        card.getStyleClass().add("job-card");
        card.setPrefWidth(270);
        card.setSpacing(10);

        Label title = new Label(job.getJobTitle());
        title.getStyleClass().add("job-title");

        Label event = new Label("Event: " + job.getEventTitle());
        Label location = new Label("📍 " + job.getJobLocation());
        Label type = new Label("⚡ " + job.getEmploymentType());
        Label salary = new Label(String.format("💰 %s - %s %s", job.getMinSalary(), job.getMaxSalary(), job.getCurrency()));
        Label deadline = new Label("⏰ Apply by: " + job.getApplicationDeadline());

        Label recruiter = new Label("👤 " + job.getRecruiterName());
        Label email = new Label("📧 " + job.getRecruiterEmail());

        Button applyButton = new Button("Apply Now");
        applyButton.getStyleClass().add("apply-button");

        // Set action for the Apply button
        applyButton.setOnAction(e -> handleApplyButton(job.getJobId()));

        card.getChildren().addAll(title, event, location, type, salary, deadline, recruiter, email, applyButton);
        return card;
    }

    // Handle Apply Button Click
    private void handleApplyButton(int jobId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/jobfeed/application-form.fxml"));
            Parent root = loader.load();

            ApplicationFormController controller = loader.getController();
            controller.setJobId(jobId);
            controller.setUserId(currentUserId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Application Form");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Cannot open form");
            errorAlert.setContentText("Failed to load application form.");
            errorAlert.showAndWait();
        }
    }


    // Helper method for navigation
    private void loadView(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
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
    }

}