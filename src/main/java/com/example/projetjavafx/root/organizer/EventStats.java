package com.example.projetjavafx.root.organizer;

public class EventStats {
    private int eventId;
    private String eventName;
    private int totalParticipants;
    private int maleCount;
    private int femaleCount;

    public EventStats(int eventId, String eventName, int totalParticipants, int maleCount, int femaleCount) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.totalParticipants = totalParticipants;
        this.maleCount = maleCount;
        this.femaleCount = femaleCount;
    }

    public int getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public int getTotalParticipants() { return totalParticipants; }
    public int getMaleCount() { return maleCount; }
    public int getFemaleCount() { return femaleCount; }

    @Override
    public String toString() {
        return eventName; // Display event name in ComboBox
    }
}





