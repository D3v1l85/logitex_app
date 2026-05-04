package com.example.logitex_app.models;

import com.google.gson.annotations.SerializedName;

public class Palet {

    @SerializedName("id")
    private int id;

    @SerializedName("lot")
    private String lot;

    @SerializedName("sscc")
    private String sscc;

    @SerializedName("estat")
    private String estat;

    // Getters para poder leer los datos luego
    public int getId() {
        return id;
    }

    public String getLot() {
        return lot;
    }

    public String getSscc() {
        return sscc;
    }

    public String getEstat() {
        return estat;
    }
}
