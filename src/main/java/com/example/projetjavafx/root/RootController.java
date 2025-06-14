package com.example.projetjavafx.root;

import com.example.projetjavafx.root.auth.SessionManager;
import com.mysql.cj.Session;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class RootController {
    public Button organizerButton;
    public Button eventsButton;
    public Button profileButton;
    public Button registerButton;
    public Button loginButton;
    public Button groupButton;
    public Button createJobButton;
    public Button jobButton;
    @FXML
    private ImageView imageView;
    @FXML
    public HBox authButtons ;

    @FXML
    public Button logoutButtonn ;


    @FXML
    private void initialize() {



        authButtons.visibleProperty().bind(SessionManager.getInstance().isUserNotConnected());

        logoutButtonn.visibleProperty().bind(SessionManager.getInstance().canLogout());


    }
    // Method to check if the user is logged in
    private boolean isUserLoggedIn() {
        return SessionManager.getInstance().getCurrentUserId() != -1;
    }

    // Method to handle logout and redirect to the login page
    @FXML
    private void onLogout() {
        System.out.println("logout");
        SessionManager.getInstance().logout();
        // Clear session

    }


    // Method to redirect to the login page
    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projetjavafx/auth/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Parent) root).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @FXML
    protected void onOrganizerButtonClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
        loadView("/com/example/projetjavafx/organizer/organizer-view.fxml", event);}
    }

//    @FXML
//    protected void onEventsClick(ActionEvent event) {
//        loadView("/com/example/projetjavafx/events/events-view.fxml", event);
//    }

    @FXML
    protected void onProfileClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
        loadView("/com/example/projetjavafx/profile/profile-view.fxml", event);}
    }

    @FXML
    protected void onLoginClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/auth/login-view.fxml", event);
    }
    @FXML
    protected void onEventsClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
            loadView("/com/example/projetjavafx/events/events-view.fxml", event);}
    }

    @FXML
    protected void onGroupButtonClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
        loadView("/com/example/projetjavafx/group/group-profile-view.fxml", event);}
    }

    @FXML
    protected void onRegisterClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/auth/register-view.fxml", event);
    }

    @FXML
    protected void onCreateJobClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
        loadView("/com/example/projetjavafx/organizer/create-job-offer-view.fxml", event);}
    }

    public void onDachboardClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/organizer/organizer-view.fxml", event);
    }

    @FXML
    protected void onAnalyticsClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
        loadView("/com/example/projetjavafx/organizer/analytics-view.fxml", event);}
    }

    @FXML
    protected void onJobFeedClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
        loadView("/com/example/projetjavafx/jobfeed/job-feed-view.fxml", event);}
    }


    @FXML
    protected void onsocialButtonClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){
            loadView("/com/example/projetjavafx/auth/login-view.fxml", event);
        } else {
            // Make sure this path is correct and the file exists
            loadView("/com/example/projetjavafx/social/Feed.fxml", event);
        }
    }
    @FXML
    protected void onPointClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){

            loadView("/com/example/projetjavafx/auth/login-view.fxml", event);
        }else {
            loadView("/com/example/projetjavafx/points/Home.fxml", event);}

    }

    @FXML
    protected void onMessagerieClick(ActionEvent event) {
        if (SessionManager.getInstance().getCurrentUserId()==-1){
            loadView("/com/example/projetjavafx/auth/login-view.fxml",event);
        }else {
            loadView("/com/example/projetjavafx/messagerie/discussion.fxml", event);
        }
    }

    public void onChatbotClick(ActionEvent event) {
        loadView("/com/example/projetjavafx/chatbot/chatbot.fxml", event);
    }
}