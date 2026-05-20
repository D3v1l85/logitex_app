package com.example.logitex_app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Ordre {

    @SerializedName("id_ordre") // El servidor envia id_ordre
    private int id;

    @SerializedName("identificador")
    private String identificador;

    @SerializedName("codiAlbara") // El servidor envia codiAlbara
    private String referencia;

    @SerializedName("estat")
    private String estat;

    @SerializedName("adreca") // El servidor envia adreca
    private String direccio;

    @SerializedName("tendaDestinataria") // Coincideix amb el JSON
    private String client;

    @SerializedName("historial")
    private List<Tracking> historial;

    // Getters per utilitzar les dades en el codi de l'aplicacio
    public int getId() { return id; }
    public String getIdentificador() { return identificador; } // Per mostrar el format d'ordre ORD-XXXX
    public String getReferencia() { return referencia; } // Per a l'escaner
    public String getEstat() { return estat; }
    public String getDireccio() { return direccio; }
    public String getClient() { return client; }
    public List<Tracking> getHistorial() { return historial; }
}
