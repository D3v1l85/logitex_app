package com.example.logitex_app.models;

import com.google.gson.annotations.SerializedName;

public class Tracking {
    @SerializedName("id")
    private int id;

    @SerializedName("etapa")
    private String etapa;

    @SerializedName("ubicacio")
    private String ubicacio;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("usuari")
    private String usuari;

    @SerializedName("notes")
    private String notes;

    public int getId() { return id; }
    public String getEtapa() { return etapa; }
    public String getUbicacio() { return ubicacio; }
    public String getTimestamp() { return timestamp; }
    public String getUsuari() { return usuari; }
    public String getNotes() { return notes; }
}
