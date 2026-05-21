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
import com.example.logitex_app.models.LoginResponse;
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
        Call<LoginResponse> call = apiService.loginUser(jsonLogin);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginData = response.body();

                    String token = loginData.getToken();
                    String nombreReal = loginData.getNom();
                    int idRol = loginData.getRolId();

                    // MEJORA: Búsqueda exhaustiva del nombre en el Token si viene nulo
                    if (nombreReal == null || nombreReal.isEmpty()) {
                        nombreReal = extraerDatoDelToken(token, "nom", "Usuari");
                    }
                    if (idRol == 0) {
                        try {
                            idRol = Integer.parseInt(extraerDatoDelToken(token, "idRol", "4"));
                        } catch (Exception e) { idRol = 4; } // Por defecto transportista si falla
                    }

                    String rolTexto = determinarRolTexto(idRol);
                    guardarSesion(token, rolTexto, nombreReal, idRol);

                    Toast.makeText(LoginActivity.this, "Login OK: " + nombreReal, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USER_ROLE", rolTexto);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(LoginActivity.this, "Email o contrasenya incorrectes", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Error de connexió al servidor", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String determinarRolTexto(int idRol) {
        if (idRol == 3) return "mosso";
        if (idRol == 4) return "transportista";
        return "admin";
    }

    private void guardarSesion(String token, String rolTexto, String nombre, int rolId) {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("TOKEN_AUTH", token);
        editor.putString("ROL_USUARIO", rolTexto);
        editor.putString("USER_NOM", nombre);
        editor.putInt("USER_ROL", rolId);
        editor.apply();
    }

    // --- Lector Inteligente del Token ---
    private String extraerDatoDelToken(String token, String key, String valorPorDefecto) {
        try {
            String[] split = token.split("\\.");
            String payloadJson = new String(Base64.decode(split[1], Base64.URL_SAFE));

            // ¡CHIVATO! Esto saldrá en el Logcat de Android Studio para ver qué envía el backend
            Log.d("TOKEN_PAYLOAD", "Contenido del JWT: " + payloadJson);

            JSONObject jsonObject = new JSONObject(payloadJson);

            if (jsonObject.has(key)) return jsonObject.getString(key);
            // Intentamos alternativas comunes si la clave original no existe
            if (key.equals("nom") && jsonObject.has("nombre")) return jsonObject.getString("nombre");
            if (key.equals("nom") && jsonObject.has("username")) return jsonObject.getString("username");
            if (key.equals("nom") && jsonObject.has("sub")) return jsonObject.getString("sub"); // El email a veces va aquí

            return valorPorDefecto;
        } catch (Exception e) {
            e.printStackTrace();
            return valorPorDefecto;
        }
    }
}
