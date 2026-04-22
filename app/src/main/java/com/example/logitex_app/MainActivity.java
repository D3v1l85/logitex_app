package com.example.logitex_app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Configuración para el diseño de pantalla completa
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Recuperem el rol enviat des de LoginActivity
        String rol = getIntent().getStringExtra("USER_ROLE");

        // 2. Comprovem el rol i configurem la pantalla d'inici
        if (rol != null) {
            if (rol.equals("MOZO")) {
                configurarInterficieMozo();
            } else if (rol.equals("TRANSPORTISTA")) {
                configurarInterficieTransportista();
            }
        } else {
            Toast.makeText(this, "Error: No s'ha rebut cap rol", Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarInterficieMozo() {
        Toast.makeText(this, "Benvingut al mòdul de Magatzem (Mosso)", Toast.LENGTH_LONG).show();
        // Més endavant, aquí activarem els botons de Lector QR i Picking
    }

    private void configurarInterficieTransportista() {
        Toast.makeText(this, "Benvingut al mòdul de Transport (Xofer)", Toast.LENGTH_LONG).show();
        // Més endavant, aquí activarem els botons de Rutes i Albarans
    }
}