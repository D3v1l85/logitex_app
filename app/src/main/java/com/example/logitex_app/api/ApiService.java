package com.example.logitex_app.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // Aquí iremos mapeando la lista de endpoints que me pasaste.
    // Empezamos por el primero de tu lista para probar: Autenticación.

    @POST("api/auth/login")
    Call<JsonObject> loginUser(@Body JsonObject loginData);

    // Más adelante pondremos aquí los @GET("api/pales") y demás...
}
