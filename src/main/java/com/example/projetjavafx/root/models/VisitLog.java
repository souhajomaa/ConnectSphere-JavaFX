package com.example.projetjavafx.root.models;

import java.util.Date;

public class VisitLog {

    private int idUser;
    private Date lastVisit;
    private int streak;

    public VisitLog(int idUser, Date lastVisit, int streak) {
        this.idUser = idUser;
        this.lastVisit = lastVisit;
        this.streak = streak;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public Date getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(Date lastVisit) {
        this.lastVisit = lastVisit;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }
}
