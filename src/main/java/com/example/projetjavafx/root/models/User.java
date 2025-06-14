package com.example.projetjavafx.root.models;

public class User {
    private int id;
    private String username;
    private int points;

    public User(int id, String username, int points) {
        this.id = id;
        this.username = username;
        this.points = points;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public int getPoints() { return points; }

    public void setPoints(int points) { this.points = points; }
}
