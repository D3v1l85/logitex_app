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
    private ImageView btnLogout;

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
        btnLogout = findViewById(R.id.btnLogout);

        // --- NUEVA LÓGICA DE LOGOUT (Borrar la memoria) ---
        btnLogout.setOnClickListener(v -> {
            // Borramos el token y el rol de las preferencias
            SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Volvemos al Login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            Toast.makeText(this, getString(R.string.desc_logout), Toast.LENGTH_SHORT).show();
        });

        // 1. Recuperamos los datos de SharedPreferences primero, y si no, del Intent
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String rol = prefs.getString("ROL_USUARIO", getIntent().getStringExtra("USER_ROLE"));
        String nombre = getIntent().getStringExtra("USER_NAME"); // Dejamos el nombre por defecto de momento

        // 2. Configuramos el menú según el rol (Ignorando mayúsculas y aceptando mozo/mosso)
        if (rol != null) {
            if (rol.equalsIgnoreCase("MOZO") || rol.equalsIgnoreCase("MOSSO")) {
                configurarMenuMozo();
            } else if (rol.equalsIgnoreCase("TRANSPORTISTA")) {
                configurarMenuTransportista();
            } else {
                // Por si acaso el rol no es ninguno de los dos
                Toast.makeText(this, "Rol desconegut: " + rol, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.error_rol), Toast.LENGTH_SHORT).show();
        }

        // 3. Cargamos el HomeFragment (Bienvenida) como pantalla fija inicial
        HomeFragment home = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("arg_nombre", nombre);
        home.setArguments(args);

        // Esta será la única carga de fragmento al iniciar la actividad
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