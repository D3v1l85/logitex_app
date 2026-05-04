package com.example.logitex_app.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.logitex_app.MainActivity;
import com.example.logitex_app.R;
import com.example.logitex_app.api.ApiService;
import com.example.logitex_app.api.RetrofitClient;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ¡ATENCIÓN! Asegúrate de que este ID sea el correcto (etUser o el que pusiste antes)
        etEmail = findViewById(R.id.etUser);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) progressBar.setVisibility(View.GONE);

        btnLogin.setOnClickListener(v -> realizarLoginReal());
    }

    private void realizarLoginReal() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Omple tots els camps", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        JsonObject jsonLogin = new JsonObject();
        jsonLogin.addProperty("email", email);
        jsonLogin.addProperty("password", password);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<JsonObject> call = apiService.loginUser(jsonLogin);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    // 1. Extraemos el token del JSON
                    String token = response.body().get("token").getAsString();

                    // 2. Desencriptamos el token para ver qué rol tiene
                    String rolTexto = descifrarRolDelToken(token);

                    // 3. Guardamos el token en la memoria del móvil
                    guardarSesion(token, rolTexto);

                    Toast.makeText(LoginActivity.this, "Benvingut!", Toast.LENGTH_SHORT).show();

                    // 4. Vamos a la pantalla principal
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USER_ROLE", rolTexto);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(LoginActivity.this, "Email o contrasenya incorrectes", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Error de connexió al servidor", Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- MAGIA DE JWT: Función para sacar el rol oculto en el token ---
    private String descifrarRolDelToken(String token) {
        try {
            // El JWT tiene 3 partes separadas por puntos. La información está en la parte del medio (índice 1)
            String[] split = token.split("\\.");
            String payloadBase64 = split[1];

            // Lo desciframos de Base64 a Texto normal
            String payloadJson = new String(Base64.decode(payloadBase64, Base64.URL_SAFE));

            // Lo convertimos en JSON para leer el "idRol"
            JSONObject jsonObject = new JSONObject(payloadJson);
            int idRol = jsonObject.getInt("idRol");


            if (idRol == 3) {
                return "mosso";
            } else if (idRol == 4) { // Suponiendo que el 4 sea transportista
                return "transportista";
            } else {
                return "admin";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "mosso"; // Por defecto si algo falla
        }
    }

    // --- Función para guardar los datos en SharedPreferences ---
    private void guardarSesion(String token, String rol) {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("TOKEN_AUTH", token);
        editor.putString("ROL_USUARIO", rol);
        editor.apply(); // Guarda de forma asíncrona
    }
}
