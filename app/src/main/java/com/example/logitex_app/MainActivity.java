package com.example.logitex_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.logitex_app.ui.home.HomeFragment;
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

        // 1. Inicializamos las vistas correctamente (he borrado la línea que tenías duplicada aquí)
        bottomNav = findViewById(R.id.bottom_navigation);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        ImageView btnLogout = findViewById(R.id.btnLogout);

        // --- LÓGICA DE LOGOUT (Borrar la memoria) ---
        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
            Toast.makeText(this, getString(R.string.desc_logout), Toast.LENGTH_SHORT).show();
        });

        // 2. Recuperamos los datos
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String rol = prefs.getString("ROL_USUARIO", getIntent().getStringExtra("USER_ROLE"));
        String nombre = getIntent().getStringExtra("USER_NAME");

        // 3. Configuramos el menú según el rol
        if (rol != null && (rol.equalsIgnoreCase("MOZO") || rol.equalsIgnoreCase("MOSSO"))) {
            configurarMenuMozo();
        } else if (rol != null && rol.equalsIgnoreCase("TRANSPORTISTA")) {
            configurarMenuTransportista();
        } else {
            // Si el rol es nulo o está corrupto, cerramos sesión por seguridad sin mostrar Toasts raros
            getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return; // Cortamos la ejecución aquí
        }

        // 4. Cargamos el HomeFragment (Bienvenida)
        HomeFragment home = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("arg_nombre", nombre);
        home.setArguments(args);

        cargarFragment(home);
    }

    // Método centralizado para cambiar de pantalla
    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void configurarMenuMozo() {
        tvHeaderTitle.setText(getString(R.string.header_mozo));
        bottomNav.inflateMenu(R.menu.menu_mozo);

        // ¡MAGIA AQUÍ! Tras inflar el menú del mozo, seleccionamos el ítem invisible
        bottomNav.setSelectedItemId(R.id.menu_none);

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

        // ¡MAGIA AQUÍ! Tras inflar el menú del transportista, seleccionamos el ítem invisible
        bottomNav.setSelectedItemId(R.id.menu_none);

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