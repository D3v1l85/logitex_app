package com.example.logitex_app.api;

import com.example.logitex_app.models.Incidencia;
import com.example.logitex_app.models.LoginResponse;
import com.example.logitex_app.models.Ordre;
import com.example.logitex_app.models.Palet; // Importa el model de palets
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Inici de sessio
    @POST("api/auth/login")
    Call<LoginResponse> loginUser(@Body JsonObject loginData);

    // Obtenir usuari
    @GET("api/usuaris/{id}")
    Call<com.example.logitex_app.models.Usuari> getUsuariById(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    // Obtenir palets per al mozo de magatzem
    // S'utilitza el parametre header per enviar el token d'autoritzacio
    @GET("api/pales")
    Call<List<Palet>> getPales(@Header("Authorization") String token);

    // Canviar l'estat d'un palet o ordre
    // El parametre path introdueix l'identificador i query afegeix el nou estat
    @PUT("api/pales/{id}/estat")
    Call<JsonObject> cambiarEstado(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Query("nouEstat") String nouEstat
    );

    // Peticions relacionades amb el transportista

    // Obtenir les ordres assignades
    // Filtra per identificador de rol i nom de l'usuari
    @GET("api/ordres")
    Call<List<Ordre>> getOrdres(
            @Header("Authorization") String token,
            @Query("rolId") int rolId,
            @Query("nom") String nom
    );

    // Canviar l'estat de la comanda
    @PUT("api/ordres/{id}/estat")
    Call<JsonObject> cambiarEstadoOrden(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Query("nouEstat") String nouEstat,
            @Query("userId") Integer userId
    );

    // Obtenir el detall d'una ordre segons el seu identificador
    @GET("api/ordres/{id}")
    Call<Ordre> getOrdreById(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    // Peticions relacionades amb les incidencies

    // Obtenir la llista d'incidencies
    @GET("api/incidencies")
    Call<List<Incidencia>> getIncidencias(
            @Header("Authorization") String token,
            @Query("rol") String rol,
            @Query("userId") int userId
    );

    // Crear una nova incidencia
    @POST("api/incidencies")
    Call<JsonObject> crearIncidencia(
            @Header("Authorization") String token,
            @Body JsonObject datosIncidencia
    );

    // Obtenir tots els grups de mozos
    @GET("api/grupmozos")
    Call<List<com.example.logitex_app.models.GrupMozos>> getGrupMozos(
            @Header("Authorization") String token
    );

    // Canviar l'estat de la incidencia
    @PATCH("api/incidencies/{id}/estat")
    Call<Incidencia> cambiarEstadoIncidencia(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Query("nouEstat") String nouEstat,
            @Query("autor") String autor
    );

}
