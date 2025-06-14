package com.example.projetjavafx.root.models;

import java.time.LocalDate;

public class Point {
    private int id;
    private int user_id;
    private String type;
    private int points;
    private String raison;
    private LocalDate timestamp;

    public Point(int id, int user_id, String type, LocalDate timestamp, int points, String raison) {
        this.id = id;
        this.user_id = user_id;
        this.type = type;
        this.timestamp = timestamp;
        this.points = points;
        this.raison = raison;
    }

    public Point(String type, int points, String raison, LocalDate timestamp) {
        this.type = type;
        this.points = points;
        this.raison = raison;
        this.timestamp = timestamp;
    }

    public Point(String type, int points, String raison) {
        this.type = type;
        this.points = points;
        this.raison = raison;
        this.timestamp = LocalDate.now(); // Ajout d'une valeur par défaut pour le timestamp
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getRaison() {
        return raison;
    }

    public void setRaison(String raison) {
        this.raison = raison;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }
}