package com.example.logitex_app.models;

import com.google.gson.annotations.SerializedName;

public class Ordre {

    @SerializedName("id_ordre") // El servidor envía "id_ordre"
    private int id;

    @SerializedName("identificador")
    private String identificador;

    @SerializedName("codiAlbara") // El servidor envía "codiAlbara"
    private String referencia;

    @SerializedName("estat")
    private String estat;

    @SerializedName("adreca") // El servidor envía "adreca"
    private String direccio;

    @SerializedName("tendaDestinataria") // Coincide con el JSON
    private String client;

    // Getters: Estos nombres los usas tú en el código, no hace falta que cambien
    public int getId() { return id; }
    public String getIdentificador() { return identificador; } // Para mostrar ORD-XXXX
    public String getReferencia() { return referencia; } // Para el escaneo
    public String getEstat() { return estat; }
    public String getDireccio() { return direccio; }
    public String getClient() { return client; }
}
