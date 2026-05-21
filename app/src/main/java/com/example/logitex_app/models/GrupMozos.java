package com.example.logitex_app.models;

import com.google.gson.annotations.SerializedName;

public class GrupMozos {

    @SerializedName("id_grup")
    private int id;

    @SerializedName("nom")
    private String nom;

    @SerializedName("descripcio")
    private String descripcio;

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getDescripcio() {
        return descripcio;
    }
}
