package com.example.logitex_app.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // ⚠️ ATENCIÓN AQUÍ: Si el backend de tu equipo está en tu propio PC y usas el emulador de Android,
    // Android no entiende "localhost". Tienes que usar "10.0.2.2".
    // Si la web ya está subida a internet, pon aquí su URL completa (ej: "https://midominio.com/").
    private static final String BASE_URL = "http://172.19.100.221:8080/"; // Cambia el 8000 por vuestro puerto real

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Este interceptor es un "chivato" maravilloso. Nos mostrará en la consola
            // exactamente qué envía la app y qué responde el servidor. Nos salvará la vida buscando errores.
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // El traductor mágico de JSON a Java
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
