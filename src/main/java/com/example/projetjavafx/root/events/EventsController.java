package com.example.projetjavafx.root.events;

import com.example.projetjavafx.root.auth.SessionManager;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Optional;

public class EventsController {

    // Fields injected from FXML
    public TextField searchField;
    @FXML
    private FlowPane eventsContainer;
    @FXML
    private ComboBox<String> categoryFilter;

    // Variable to store the current running task
    private Task<ObservableList<Event>> currentLoadTask;

    @FXML
    public void initialize() {
        // Load categories and set in the ComboBox
        ObservableList<String> categories = EventsRepository.getCategories();
        categoryFilter.setItems(categories);
        categoryFilter.getSelectionModel().select("All");

        // Load events initially when the scene is attached
        Platform.runLater(() -> loadEvents(searchField.getText(), categoryFilter.getValue()));

        // Add a listener to the search field to reload events on text changes
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadEvents(newValue, categoryFilter.getValue());
        });

        // Note: We removed the categoryFilter property listener from here
        // to avoid duplicate invocations. Filtering by category is now handled via the onCategorySelected() method.
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) eventsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadEvents(String searchText, String category) {
        // Cancel any currently running task
        if (currentLoadTask != null && currentLoadTask.isRunning()) {
            currentLoadTask.cancel();
        }

        // Create a custom loading window
        Stage loadingStage = new Stage();
        loadingStage.initStyle(StageStyle.UNDECORATED);
        loadingStage.initModality(Modality.NONE);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        Label loadingMessage = new Label("Loading...");

        VBox loadingRoot = new VBox(10);
        loadingRoot.setAlignment(Pos.CENTER);
        loadingRoot.getChildren().addAll(progressIndicator, loadingMessage);
        loadingRoot.setStyle("-fx-padding: 20; -fx-background-color: rgba(0,0,0,0.3);");

        Scene loadingScene = new Scene(loadingRoot, 200, 150);
        loadingStage.setScene(loadingScene);

        if (eventsContainer.getScene() != null) {
            Stage mainStage = (Stage) eventsContainer.getScene().getWindow();
            loadingStage.setX(mainStage.getX() + (mainStage.getWidth() - 200) / 2);
            loadingStage.setY(mainStage.getY() + (mainStage.getHeight() - 150) / 2);
        } else {
            loadingStage.centerOnScreen();
        }

        loadingStage.show();

        // Clear the events container before loading new events
        eventsContainer.getChildren().clear();

        // Create a background task to fetch events
        Task<ObservableList<Event>> loadTask = new Task<ObservableList<Event>>() {
            @Override
            protected ObservableList<Event> call() {
                return EventsRepository.getEvents(searchText, category);
            }
        };

        // Save the running task
        currentLoadTask = loadTask;

        loadTask.setOnSucceeded(e -> {
            ObservableList<Event> events = loadTask.getValue();
            for (Event event : events) {
                VBox eventBox = new VBox(10);
                eventBox.getStyleClass().add("event-card");

                String start = event.getStartDate() != null ? event.getStartDate() : "N/A";
                String end = event.getEndDate() != null ? event.getEndDate() : "N/A";
                String categoryDisplay = event.getCategoryName() != null ? event.getCategoryName() : "No Category";

                Text eventText = new Text(
                        event.getName() + "\n" +
                                event.getDescription() + "\n" +
                                start + " - " + end + "\n" +
                                event.getLocation() + "\nCatégorie : " + categoryDisplay
                );
                eventText.getStyleClass().add("event-description");

                ImageView imageView = new ImageView();
                if (event.getImageBase64() != null && !event.getImageBase64().isEmpty()) {
                    try {
                        byte[] imageBytes = Base64.getDecoder().decode(event.getImageBase64());
                        Image image = new Image(new ByteArrayInputStream(imageBytes));
                        imageView.setImage(image);
                        imageView.setFitWidth(200);
                        imageView.setPreserveRatio(true);
                    } catch (IllegalArgumentException ex) {
                        System.err.println("Données image invalides pour l'événement : " + event.getName());
                    }
                }
                imageView.getStyleClass().add("event-image");

                Button viewDetailsButton = new Button("View Details");
                viewDetailsButton.getStyleClass().add("event-button");
                viewDetailsButton.setOnAction(ev -> showEventDetails(event));

                Button participateButton = new Button("Participer");
                participateButton.getStyleClass().add("event-button-secondary");
                participateButton.setOnAction(ev -> confirmParticipation(event.getEventId()));

                HBox buttonContainer = new HBox(10, viewDetailsButton, participateButton);
                buttonContainer.getStyleClass().add("event-buttons");

                eventBox.getChildren().addAll(imageView, eventText, buttonContainer);
                eventsContainer.getChildren().add(eventBox);
            }
            Platform.runLater(loadingStage::close);
        });

        loadTask.setOnCancelled(e -> Platform.runLater(loadingStage::close));

        loadTask.setOnFailed(e -> {
            Platform.runLater(loadingStage::close);
            loadTask.getException().printStackTrace();
        });

        new Thread(loadTask).start();
    }

    private void confirmParticipation(int eventId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Confirm your particiption");
        alert.setHeaderText(null);
        alert.setContentText("You wanna confirm your participation");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
           if(!SessionManager.getInstance().isUserParticipatedInEvent(eventId)){
               EventsRepository.participateInEvent(eventId);
           }else {
               System.out.println("already participated.");
           }


        } else {
            System.out.println("Participation denied.");
        }
    }

    private void showEventDetails(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/events/event-details.fxml"));
            Parent root = loader.load();

            EventDetailsController controller = loader.getController();
            String start = event.getStartDate() != null ? event.getStartDate() : "N/A";
            String end = event.getEndDate() != null ? event.getEndDate() : "N/A";
            controller.setEventDetails(event.getName(), event.getDescription(), start, end, event.getLocation(), event.getImageBase64());

            Stage stage = new Stage();
            Scene scene = new Scene(root);

            String cssFile = getClass().getResource("/css/event-details.css") != null ?
                    getClass().getResource("/css/event-details.css").toExternalForm() : null;
            if (cssFile != null) {
                scene.getStylesheets().add(cssFile);
            } else {
                System.out.println("⚠ Fichier CSS non trouvé !");
            }
            stage.setScene(scene);
            stage.setTitle("Event Details");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onNewEventClick() {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml");
        }else {
            loadView("/com/example/projetjavafx/events/create-events.fxml");
        }
    }

    // This method is linked via FXML to the category filter (e.g., onAction)
    @FXML
    private void onCategorySelected() {
        loadEvents(searchField.getText(), categoryFilter.getValue());
    }


    @FXML
    public void onOrganizerButtonClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/organizer-view.fxml");
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
        loadView("/com/example/projetjavafx/chatbot/chatbot.fxml");
    }


    public void handleHomeButton(ActionEvent event) {
        loadView( "/com/example/projetjavafx/root/root-view.fxml");

    }
}
