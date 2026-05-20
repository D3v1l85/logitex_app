package com.example.logitex_app.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Atencio: Si el backend de l'equip esta al mateix ordinador i s'utilitza l'emulador d'Android
    // Android no detecta localhost i s'ha de fer servir 10.0.2.2
    // Si el servidor ja esta penjat a internet cal posar la seva adreça completa
    private static final String BASE_URL = "http://10.147.17.250:8084/"; // Canvia el port pel vostre port real

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Aquest interceptor serveix per mostrar per la consola del depurador
            // exactament què envia l'aplicacio i què respon el servidor per trobar errors de forma senzilla
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // El traductor de format JSON a objectes de Java
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
