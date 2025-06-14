package com.example.projetjavafx.root.models;

import java.util.Date;

public class HistoriqueTransaction {

    private int id;
    private int idUser;
    private String type;
    private double montant;
    private String devise;
    private int pointConvertis;
    private Date date;

    public HistoriqueTransaction(Date date, int pointConvertis, String devise, double montant) {
        this.date = date;
        this.pointConvertis = pointConvertis;
        this.devise = devise;
        this.montant = montant;
    }

    public HistoriqueTransaction(int idUser, String type, double montant, String devise, int pointConvertis) {
        this.idUser = idUser;
        this.type = type;
        this.montant = montant;
        this.devise = devise;
        this.pointConvertis = pointConvertis;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public int getPointConvertis() {
        return pointConvertis;
    }

    public void setPointConvertis(int pointConvertis) {
        this.pointConvertis = pointConvertis;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
