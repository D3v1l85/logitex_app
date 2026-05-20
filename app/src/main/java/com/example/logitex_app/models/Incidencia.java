package com.example.logitex_app.models;

import com.google.gson.annotations.SerializedName;

public class Incidencia {

    @SerializedName("id_incidencia")
    private int id;

    @SerializedName("titol")
    private String titol;

    @SerializedName("descripcio")
    private String descripcio;

    @SerializedName("idOrdre")
    private int idOrdre;

    @SerializedName("userId")
    private int userId;

    @SerializedName("estat")
    private String estat;

    @SerializedName("prioritat")
    private String prioritat;

    @SerializedName("reportatPerNom")
    private String reportatPerNom;

    @SerializedName("assignatANom")
    private String assignatANom;

    public Incidencia() {}

    public int getId() { 
        return id; 
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public String getTitol() { 
        return titol; 
    }

    public String getDescripcio() { 
        return descripcio; 
    }

    public int getIdOrdre() {
        return idOrdre;
    }

    public String getEstat() {
        return estat;
    }

    public void setEstat(String estat) {
        this.estat = estat;
    }

    public String getPrioritat() {
        return prioritat;
    }

    public String getReportatPerNom() {
        return reportatPerNom;
    }

    public String getAssignatANom() {
        return assignatANom;
    }
}
