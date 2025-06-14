module com.example.projetjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires mysql.connector.j;
    requires jdk.jfr;
    requires okhttp3;
    requires org.json;
    requires flexmark;
    requires java.mail;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires java.desktop;
    requires com.google.gson;
    requires org.java_websocket;
    requires java.sql;

    // Root package
    exports com.example.projetjavafx.root;
    opens com.example.projetjavafx.root to javafx.fxml;
    opens com.example.projetjavafx.root.jobFeed to javafx.fxml;
    opens com.example.projetjavafx.root.points to javafx.fxml;



    // Controller package accesses
    opens com.example.projetjavafx.root.auth to javafx.fxml;
    opens com.example.projetjavafx.root.events to javafx.fxml;
    opens com.example.projetjavafx.root.explore to javafx.fxml;
    opens com.example.projetjavafx.root.organizer to javafx.fxml;
    opens com.example.projetjavafx.root.group to javafx.fxml;
    opens com.example.projetjavafx.root.profile to javafx.fxml;
    opens com.example.projetjavafx.root.social to javafx.fxml;
    // CSS directories
    opens com.example.projetjavafx.auth.css to javafx.fxml;
//    opens com.example.projetjavafx.events.css to javafx.fxml;
    opens com.example.projetjavafx.explore.css to javafx.fxml;
    opens com.example.projetjavafx.group.css to javafx.fxml;
    opens com.example.projetjavafx.organizer.css to javafx.fxml;
    opens com.example.projetjavafx.profile.css to javafx.fxml;
    opens com.example.projetjavafx.root.css to javafx.fxml;
    opens com.example.projetjavafx.social.css to javafx.fxml;
    opens com.example.projetjavafx.points to javafx.fxml;

    // Explicit exports for public controller classes
    exports com.example.projetjavafx.root.auth;
    exports com.example.projetjavafx.root.chatbot;
    exports com.example.projetjavafx.root.events;
    exports com.example.projetjavafx.root.explore;
    exports com.example.projetjavafx.root.organizer;
    exports com.example.projetjavafx.root.group;
    exports com.example.projetjavafx.root.profile;
    exports com.example.projetjavafx.root.social;
    exports com.example.projetjavafx.root.points;
    opens com.example.projetjavafx.root.models to javafx.fxml;
    opens com.example.projetjavafx.root.services to javafx.fxml;
    // CSS directories
    // Explicit exports for public controller classes
    exports com.example.projetjavafx.root.models;
    exports com.example.projetjavafx.root.services;
    // Add to exports section
    exports com.example.projetjavafx.root.jobFeed;
    exports com.example.projetjavafx.root.jobApplications;
    opens com.example.projetjavafx.root.jobApplications to javafx.fxml;
    opens com.example.projetjavafx.root.chatbot to javafx.fxml;

// partie souha Ouvrir les packages nécessaires pour GSON
    opens com.example.projetjavafx.root.messagerie.model to com.google.gson;
    opens com.example.projetjavafx.root.messagerie.util to com.google.gson;
    opens com.example.projetjavafx.root.messagerie.client to javafx.fxml;
    exports com.example.projetjavafx.root.messagerie.model;
    exports com.example.projetjavafx.root.messagerie.client;
    exports com.example.projetjavafx.root.messagerie.server;
    opens com.example.projetjavafx.messagerie.css to javafx.fxml;



}