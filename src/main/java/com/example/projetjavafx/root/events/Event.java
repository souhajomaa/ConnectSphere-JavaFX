package com.example.projetjavafx.root.events;

public class Event {
    private String name;
    private String description;
    private String startDate; // Stockée en String
    private String endDate;   // Stockée en String
    private String location;
    private String imageBase64;
    private String categoryName;
    private int eventId ;

    // Constructeur sans argument
    public Event() {
    }

    public int getEventId (){return this.eventId;}

    public void setEventId(int id ){
        this.eventId = id;
    }

    // Getters et Setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageBase64() {
        return imageBase64;
    }
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getCategoryName() {
        return categoryName;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
