package com.example.logitex_app;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private TextView tvPlaceholder, tvHeaderTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNav = findViewById(R.id.bottom_navigation);
        tvPlaceholder = findViewById(R.id.tvPlaceholder);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);

        String rol = getIntent().getStringExtra("USER_ROLE");

        if (rol != null) {
            if (rol.equals("MOZO")) {
                configurarMenuMozo();
            } else if (rol.equals("TRANSPORTISTA")) {
                configurarMenuTransportista();
            }
        } else {
            Toast.makeText(this, getString(R.string.error_rol), Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarMenuMozo() {
        tvHeaderTitle.setText(getString(R.string.header_mozo));
        bottomNav.inflateMenu(R.menu.menu_mozo);
        tvPlaceholder.setText(getString(R.string.msg_picking));

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_picking) {
                tvPlaceholder.setText(getString(R.string.msg_picking));
                return true;
            } else if (itemId == R.id.nav_qr) {
                tvPlaceholder.setText(getString(R.string.msg_escaner));
                return true;
            }
            return false;
        });
    }

    private void configurarMenuTransportista() {
        tvHeaderTitle.setText(getString(R.string.header_transportista));
        bottomNav.inflateMenu(R.menu.menu_transportista);
        tvPlaceholder.setText(getString(R.string.msg_rutas));

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_rutas) {
                tvPlaceholder.setText(getString(R.string.msg_rutas));
                return true;
            } else if (itemId == R.id.nav_incidencias) {
                tvPlaceholder.setText(getString(R.string.msg_incidencias));
                return true;
            }
            return false;
        });
    }
}