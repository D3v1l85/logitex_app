package com.example.logitex_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.logitex_app.ui.auth.LoginActivity;
import com.example.logitex_app.ui.mozo.PickingFragment;
import com.example.logitex_app.ui.mozo.UbicacionFragment;
import com.example.logitex_app.ui.transportista.IncidenciasFragment;
import com.example.logitex_app.ui.transportista.RutasFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private TextView tvHeaderTitle;

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
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        ImageView btnLogout = findViewById(R.id.btnLogout);

        // Lógica para cerrar sesión
        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            Toast.makeText(this, getString(R.string.desc_logout), Toast.LENGTH_SHORT).show();
        });

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

    // --- MÈTODE MÀGIC PER CANVIAR DE PANTALLA ---
    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void configurarMenuMozo() {
        tvHeaderTitle.setText(getString(R.string.header_mozo));
        bottomNav.inflateMenu(R.menu.menu_mozo);

        // Carregar pantalla per defecte a l'entrar
        cargarFragment(new PickingFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_picking) {
                cargarFragment(new PickingFragment());
                return true;
            } else if (itemId == R.id.nav_qr) {
                cargarFragment(new UbicacionFragment());
                return true;
            }
            return false;
        });
    }

    private void configurarMenuTransportista() {
        tvHeaderTitle.setText(getString(R.string.header_transportista));
        bottomNav.inflateMenu(R.menu.menu_transportista);

        // Carregar pantalla per defecte a l'entrar
        cargarFragment(new RutasFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_rutas) {
                cargarFragment(new RutasFragment());
                return true;
            } else if (itemId == R.id.nav_incidencias) {
                cargarFragment(new IncidenciasFragment());
                return true;
            }
            return false;
        });
    }
}