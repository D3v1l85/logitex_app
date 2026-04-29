package com.example.logitex_app.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("nom")
    private String nom;

    @SerializedName("rolId")
    private int rolId;

    public String getToken() { return token; }
    public String getNom() { return nom; }
    public int getRolId() { return rolId; }
}
