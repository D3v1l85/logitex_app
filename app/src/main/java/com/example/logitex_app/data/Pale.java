package com.example.logitex_app.data;

public class Pale {
    private String sscc;    // Codi únic de 18 dígits
    private String lot;     // Lot de fabricació
    private String estat;   // disponible, assignat, en_ordre, entregada

    public Pale(String sscc, String lot, String estat) {
        this.sscc = sscc;
        this.lot = lot;
        this.estat = estat;
    }
    // Getters...
}
