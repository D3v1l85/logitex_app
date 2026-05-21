package com.example.logitex_app.api;

import com.example.logitex_app.models.Incidencia;
import com.example.logitex_app.models.LoginResponse;
import com.example.logitex_app.models.Ordre;
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
    Call<LoginResponse> loginUser(@Body JsonObject loginData);

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

    // ==========================================
    //          RUTAS DEL TRANSPORTISTA
    // ==========================================

    // 1. Obtener las órdenes (rutas).
    // Según tu API, filtra por rolId y nom, así que los pasamos como Query.
    @GET("api/ordres")
    Call<List<Ordre>> getOrdres(
            @Header("Authorization") String token,
            @Query("rolId") int rolId,
            @Query("nom") String nom // Supongo que es el nombre del transportista
    );

    // 2. Cambiar el estado de la orden (ej: ENTREGAT)
    @PUT("api/ordres/{id}/estat")
    Call<JsonObject> cambiarEstadoOrden(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Query("nouEstat") String nouEstat
    );

    // ==========================================
    //        INCIDENCIAS (TRANSPORTISTA)
    // ==========================================

    // 3. Obtener la lista de incidencias
    @GET("api/incidencies")
    Call<List<Incidencia>> getIncidencias(
            @Header("Authorization") String token,
            @Query("rol") String rol,
            @Query("userId") int userId
    );

    // 4. Crear una nueva incidencia
    @POST("api/incidencies")
    Call<JsonObject> crearIncidencia(
            @Header("Authorization") String token,
            @Body JsonObject datosIncidencia
    );

}
