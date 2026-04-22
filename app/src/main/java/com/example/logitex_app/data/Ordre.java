package com.example.logitex_app.data;

public class Ordre {
    private String identificador; // Ej: ORD-001
    private String estat;         // esborrany, pendent_preparacio, etc.
    private String prioritat;     // urgent, normal
    private String adreca;        // Direcció d'entrega

    public Ordre(String identificador, String estat, String prioritat, String adreca) {
        this.identificador = identificador;
        this.estat = estat;
        this.prioritat = prioritat;
        this.adreca = adreca;
    }

    // Getters necesarios para mostrar los datos en las tarjetas
    public String getIdentificador() { return identificador; }
    public String getEstat() { return estat; }
    public String getAdreca() { return adreca; }
}
