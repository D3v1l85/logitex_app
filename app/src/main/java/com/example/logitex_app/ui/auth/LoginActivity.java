package com.example.logitex_app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.logitex_app.MainActivity;
import com.example.logitex_app.R;
//import com.example.logitex_app.model.LoginResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUser, etPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUser = findViewById(R.id.etUser);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String user = etUser.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Omple tots els camps", Toast.LENGTH_SHORT).show();
            } else {
                // Verificació manual per a usuaris de prova
                if (user.equalsIgnoreCase("mosso") && pass.equals("1234")) {
                    iniciarSessio("MOZO");
                } else if (user.equalsIgnoreCase("xofer") && pass.equals("1234")) {
                    iniciarSessio("TRANSPORTISTA");
                } else {
                    Toast.makeText(this, "Credencials incorrectes. Prova: mosso/1234 o xofer/1234", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void iniciarSessio(String rol) {
        /*if (response.isSuccessful() && response.body() != null) {
            LoginResponse loginResponse = response.body();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

            // Pasamos el nombre que acabamos de definir en la clase superior
            intent.putExtra("USER_NAME", loginResponse.getNom());

            // Determinamos el rol según el ID numérico de vuestra base de datos
            String role = (loginResponse.getRolId() == 3) ? "MOZO" : "TRANSPORTISTA";
            intent.putExtra("USER_ROLE", role);

            startActivity(intent);
            finish();
        }*/
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

        // Posem dades simulades (mock) de moment per provar la interfície
        intent.putExtra("USER_NAME", "Bagner (Prova)");
        intent.putExtra("USER_ROLE", "TRANSPORTISTA"); // O "MOZO", el que vulguis provar

        startActivity(intent);
        finish();
    }
}
