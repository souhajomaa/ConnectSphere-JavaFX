package com.example.projetjavafx.root.auth;

import com.example.projetjavafx.root.DbConnection.AivenMySQLManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import jdk.jfr.Event;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
        private static SessionManager instance;
        private int currentUserId = -1; // ID de l'utilisateur connecté
        private String currentUserEmail; // Email de l'utilisateur connecté (optionnel)


        List<Integer> paricipatedEvents = new ArrayList<Integer>();

    private  BooleanProperty isUserNotLoggedIn = new SimpleBooleanProperty(true);
        // Constructeur privé pour empêcher l'instanciation directe
        private SessionManager() {}


        public ObservableValue<? extends Boolean> isUserNotConnected (){
            return isUserNotLoggedIn;
        }

    public ObservableValue<? extends Boolean> canLogout (){
        return isUserNotLoggedIn.not();
    }

        // Méthode pour obtenir l'instance unique de SessionManager
        public static SessionManager getInstance() {
            if (instance == null) {
                instance = new SessionManager();
            }
            return instance;
        }

        // Méthode pour définir l'utilisateur connecté
        public void setCurrentUser(int userId, String email) {

            // get from db participated evnets



            this.isUserNotLoggedIn.set(false);
            this.currentUserId = userId;
            this.currentUserEmail = email;
            updatepart();
        }

        // Méthode pour obtenir l'ID de l'utilisateur connecté
        public int getCurrentUserId() {
            return currentUserId;
        }

        public void updatepart(){
            String sql = "select event_id from participation where id = "+currentUserId;

            try (Connection connection = AivenMySQLManager.getConnection();
                 Statement stmt = connection.createStatement();
                 ResultSet resultSet = stmt.executeQuery(sql)) {
                while (resultSet.next()) {
                    int event_id = resultSet.getInt("event_id");
                    paricipatedEvents.add(event_id);

                }

                System.out.println(paricipatedEvents);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Méthode pour obtenir l'email de l'utilisateur connecté (optionnel)
        public String getCurrentUserEmail() {
            return currentUserEmail;
        }

        // Méthode pour déconnecter l'utilisateur
        public void logout() {
            this.isUserNotLoggedIn.set(true);
            this.currentUserId = -1;
            this.currentUserEmail = null;
        }


        public boolean isUserParticipatedInEvent(int eventId){
            return paricipatedEvents.contains(eventId);
        }
    }
