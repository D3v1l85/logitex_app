package com.example.logitex_app.api;

import com.example.logitex_app.models.Palet; // Fíjate que se importa tu nuevo modelo
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // 1. LOGIN
    @POST("api/auth/login")
    Call<JsonObject> loginUser(@Body JsonObject loginData);

    // 2. OBTENER PALETS (Para el Mosso)
    // Usamos @Header para meter el Token en la mochila de la petición
    @GET("api/pales")
    Call<List<Palet>> getPales(@Header("Authorization") String token);

    // 3. CAMBIAR ESTADO DE UN PALET / ORDEN
    // @Path mete el número en el {id} y @Query añade el ?nouEstat=... al final
    @PUT("api/pales/{id}/estat") // <-- CÁMBIALO SI VUESTRA RUTA ES DISTINTA
    Call<JsonObject> cambiarEstado(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Query("nouEstat") String nouEstat
    );

}
