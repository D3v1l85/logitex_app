package com.example.logitex_app.models;

import com.google.gson.annotations.SerializedName;

public class Incidencia {

    @SerializedName("id")
    private int id;

    @SerializedName("titol") // O el campo "tipo" según vuestra API
    private String titol;

    @SerializedName("descripcio")
    private String descripcio;

    @SerializedName("idOrdre")
    private int idOrdre;

    @SerializedName("userId")
    private int userId;

    // Constructor vacío necesario para GSON
    public Incidencia() {}

    // Constructor para crear incidencias nuevas
    public Incidencia(String titol, String descripcio, int idOrdre, int userId) {
        this.titol = titol;
        this.descripcio = descripcio;
        this.idOrdre = idOrdre;
        this.userId = userId;
    }

    // Getters
    public int getId() { return id; }
    public String getTitol() { return titol; }
    public String getDescripcio() { return descripcio; }
}
