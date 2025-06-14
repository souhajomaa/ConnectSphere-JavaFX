package com.example.projetjavafx.root.organizer;

import com.example.projetjavafx.root.auth.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class AnalyticsController {

    // FXML Injections
    @FXML private ComboBox<String> eventComboBox; // Changed from EventStats to String
    @FXML private Label totalParticipantsLabel;
    @FXML private Label maleParticipantsLabel;
    @FXML private Label femaleParticipantsLabel;
    @FXML private BarChart<String, Number> genderBarChart;
    @FXML private LineChart<String, Number> participationLineChart;


    private Map<String, EventStats> eventStatsMap = new HashMap<>();// Added mapping
    SessionManager sessionManager = SessionManager.getInstance();
    private int   organizerId = sessionManager.getCurrentUserId();


    @FXML
    public void initialize() {
        setupChartAxes();
        setupComboBoxListener();
        loadEventData();
    }

//    public void setOrganizerId(int organizerId) throws SQLException {
//        this.organizerId = organizerId;
//        loadEventData();
//    }

    private void setupChartAxes() {
        ((CategoryAxis) genderBarChart.getXAxis()).setLabel("Gender");
        ((NumberAxis) genderBarChart.getYAxis()).setLabel("Participants");
        ((CategoryAxis) participationLineChart.getXAxis()).setLabel("Events");
        ((NumberAxis) participationLineChart.getYAxis()).setLabel("Average Age");
    }

    private void loadEventData() {
        try {


            // Get data from repository
            List<EventStats> allEventStats = AnalyticsRepository.getEventStats(organizerId);
            List<EventAgeStats> ageStats = AnalyticsRepository.getEventAvgAgeStats(organizerId);

            // Populate event map and ComboBox
            eventStatsMap.clear();
            List<String> eventNames = new ArrayList<>();
            for (EventStats stats : allEventStats) {
                eventStatsMap.put(stats.getEventName(), stats);
                eventNames.add(stats.getEventName());
            }

            // Update ComboBox
            eventComboBox.setItems(FXCollections.observableArrayList(eventNames));

            // Update line chart
            XYChart.Series<String, Number> ageSeries = new XYChart.Series<>();
            for (EventAgeStats stat : ageStats) {
                ageSeries.getData().add(new XYChart.Data<>(
                        stat.getEventName(),
                        stat.getAverageAge()
                ));
            }
            participationLineChart.getData().setAll(ageSeries);

            // Select first item if available
            if (!eventNames.isEmpty()) {
                eventComboBox.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load event data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupComboBoxListener() {
        eventComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                EventStats selectedStats = eventStatsMap.get(newVal);
                if (selectedStats != null) {
                    updateStatsDisplay(selectedStats);
                    updateGenderChart(selectedStats);
                }
            }
        });
    }

    private void updateStatsDisplay(EventStats stats) {
        totalParticipantsLabel.setText(String.valueOf(stats.getTotalParticipants()));
        maleParticipantsLabel.setText(String.valueOf(stats.getMaleCount()));
        femaleParticipantsLabel.setText(String.valueOf(stats.getFemaleCount()));
    }

    private void updateGenderChart(EventStats stats) {
        genderBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Male", stats.getMaleCount()));
        series.getData().add(new XYChart.Data<>("Female", stats.getFemaleCount()));
        genderBarChart.getData().add(series);
    }

    // Navigation methods (keep existing implementations)
    @FXML


    private void loadView(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    protected void onDashboardClick(javafx.event.ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/organizer-view.fxml", event);
    }

    @FXML
    protected void onEventsClick(javafx.event.ActionEvent event) {
        loadView("/com/example/projetjavafx/events/events-view.fxml", event);
    }

    @FXML
    protected void onAnalyticsClick(javafx.event.ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/analytics-view.fxml", event);
    }

    public void onJobApplicationsButtonClick(javafx.event.ActionEvent event) {
        loadView("/com/example/projetjavafx/JobApplications/application_review-view.fxml", event);
    }

    public void onJobFeedButtonClick(javafx.event.ActionEvent event) {
        loadView("/com/example/projetjavafx/jobfeed/job-feed-view.fxml", event);
    }

    public void onCreateJobButtonClick(javafx.event.ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/create-job-offer-view.fxml", event);
    }

    public void onHomeButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/root/root-view.fxml", event);
    }
}

