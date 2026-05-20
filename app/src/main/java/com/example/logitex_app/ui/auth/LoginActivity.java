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

        if (progressBar != null)
            progressBar.setVisibility(View.GONE);

        // Tradueix els textos estatics del formulari d'inici de sessio
        android.widget.TextView tvWelcome = findViewById(R.id.tvWelcomeTitle);
        com.google.android.material.textfield.TextInputLayout tilUser = findViewById(R.id.tilUser);
        com.google.android.material.textfield.TextInputLayout tilPassword = findViewById(R.id.tilPassword);

        if (tvWelcome != null)
            tvWelcome.setText(com.example.logitex_app.utils.TranslationHelper.welcomeTitle(this));
        if (tilUser != null)
            tilUser.setHint(com.example.logitex_app.utils.TranslationHelper.userHint(this));
        if (tilPassword != null)
            tilPassword.setHint(com.example.logitex_app.utils.TranslationHelper.passwordHint(this));
        if (btnLogin != null)
            btnLogin.setText(com.example.logitex_app.utils.TranslationHelper.loginButtonText(this));

        btnLogin.setOnClickListener(v -> realizarLoginReal());
    }

    // Realitza l'autenticacio contra el servidor enviant l'usuari i la contrasenya
    private void realizarLoginReal() {
        // Obtenim els camps de text eliminant els espais sobrants
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Si algun camp esta buit mostrem un avis i parem
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, com.example.logitex_app.utils.TranslationHelper.fillAllFields(this),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Activem l'indicador de carrega i bloquegem el boto d'acces
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Preparem el cos de la peticio en format JSON
        JsonObject jsonLogin = new JsonObject();
        jsonLogin.addProperty("email", email);
        jsonLogin.addProperty("password", password);

        // Executem la crida a la API de forma asincrona
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<LoginResponse> call = apiService.loginUser(jsonLogin);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Ocultem l'indicador de carrega i reactivem el boto
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                // Si el login ha estat exitos i el servidor ha respones amb dades
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginData = response.body();

                    String token = loginData.getToken();
                    String nombreReal = loginData.getNom();
                    int idRol = loginData.getRolId();

                    // Extreu el nom directament del token JWT si ve buit
                    if (nombreReal == null || nombreReal.isEmpty()) {
                        nombreReal = extraerDatoDelToken(token, "nom", "Usuari");
                    }
                    // Extreu el rol directament del token JWT si ve buit
                    if (idRol == 0) {
                        try {
                            idRol = Integer.parseInt(extraerDatoDelToken(token, "idRol", "4"));
                        } catch (Exception e) {
                            idRol = 4;
                        }
                    }
                    // Extreu l'identificador d'usuari directament del token JWT si ve buit
                    int userId = 0;
                    try {
                        userId = Integer.parseInt(extraerDatoDelToken(token, "userId", "0"));
                    } catch (Exception e) {
                        userId = 0;
                    }

                    // Validem que l'usuari tingui un rol autoritzat per fer servir l'aplicacio
                    if (idRol != 3 && idRol != 4) {
                        Toast.makeText(LoginActivity.this,
                                com.example.logitex_app.utils.TranslationHelper.roleNotAllowed(LoginActivity.this),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String rolTexto = determinarRolTexto(idRol);
                    // Desem les dades de sessio de forma persistent al dispositiu
                    guardarSesion(token, rolTexto, nombreReal, idRol, userId);

                    Toast.makeText(LoginActivity.this,
                            com.example.logitex_app.utils.TranslationHelper.loginOk(LoginActivity.this, nombreReal),
                            Toast.LENGTH_SHORT).show();

                    // Redirigim l'usuari a la pantalla principal
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USER_ROLE", rolTexto);
                    startActivity(intent);
                    finish();

                } else {
                    // Si les credencials no son correctes avisem l'usuari
                    Toast.makeText(LoginActivity.this,
                            com.example.logitex_app.utils.TranslationHelper.loginIncorrect(LoginActivity.this),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Si falla la connexio amb el servidor mostrem un missatge d'error
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this,
                        com.example.logitex_app.utils.TranslationHelper.connectionError(LoginActivity.this),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Tradueix l'identificador numeric de rol a la seva representacio en text
    private String determinarRolTexto(int idRol) {
        if (idRol == 3)
            return "mosso";
        if (idRol == 4)
            return "transportista";
        return "admin";
    }

    // Desa la informacio de l'usuari i el token de sessio de forma persistent al telefon
    private void guardarSesion(String token, String rolTexto, String nombre, int rolId, int userId) {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("TOKEN_AUTH", token);
        editor.putString("ROL_USUARIO", rolTexto);
        editor.putString("USER_NOM", nombre);
        editor.putInt("USER_ROL", rolId);
        editor.putInt("USER_ID", userId);
        editor.apply();
    }

    // Desxifra i extreu un camp especific dins de la carrega util del token JWT
    private String extraerDatoDelToken(String token, String key, String valorPorDefecto) {
        try {
            String[] split = token.split("\\.");
            String payloadJson = new String(Base64.decode(split[1], Base64.URL_SAFE));

            // Imprimeix al log el contingut del JWT per facilitar la depuracio
            Log.d("TOKEN_PAYLOAD", "Contenido del JWT: " + payloadJson);

            JSONObject jsonObject = new JSONObject(payloadJson);

            if (jsonObject.has(key))
                return jsonObject.getString(key);
            
            // Si la clau original no existeix provem alternatives comunes de noms en JWT
            if (key.equals("nom") && jsonObject.has("nombre"))
                return jsonObject.getString("nombre");
            if (key.equals("nom") && jsonObject.has("username"))
                return jsonObject.getString("username");
            if (key.equals("nom") && jsonObject.has("sub"))
                return jsonObject.getString("sub"); 

            return valorPorDefecto;
        } catch (Exception e) {
            e.printStackTrace();
            return valorPorDefecto;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(com.example.logitex_app.utils.LocaleHelper.onAttach(newBase, "ca"));
    }
}
