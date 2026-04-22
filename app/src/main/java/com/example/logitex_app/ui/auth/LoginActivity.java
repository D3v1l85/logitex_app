package com.example.logitex_app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.logitex_app.MainActivity;
import com.example.logitex_app.R;
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
                // Lògica provisional de rols segons el document de viabilitat
                String role = "";
                if (user.equalsIgnoreCase("mosso")) {
                    role = "MOZO"; // Rol per a Picking/Ubicació [cite: 161]
                } else if (user.equalsIgnoreCase("xofer")) {
                    role = "TRANSPORTISTA"; // Rol per a Rutes [cite: 162]
                }

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("USER_ROLE", role);
                startActivity(intent);
                finish();
            }
        });
    }
}
