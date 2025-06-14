package com.example.projetjavafx.root.organizer;



public class EventAgeStats {
    private final int eventId;
    private final String eventName;
    private final double averageAge;

    public EventAgeStats(int eventId, String eventName, double averageAge) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.averageAge = averageAge;
    }

    public int getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public double getAverageAge() {
        return averageAge;
    }

    @Override
    public String toString() {
        return "EventAgeStats{" +
                "eventId=" + eventId +
                ", eventName='" + eventName + '\'' +
                ", averageAge=" + averageAge +
                '}';
    }
}
