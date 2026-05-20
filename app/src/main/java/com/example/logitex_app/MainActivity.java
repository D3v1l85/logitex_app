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
import com.example.logitex_app.ui.mozo.UbicacioFragment;
import com.example.logitex_app.ui.transportista.ScanQrFragment;
import com.example.logitex_app.ui.transportista.IncidenciaFragment;
import com.example.logitex_app.ui.transportista.RutasFragment;
import com.example.logitex_app.ui.transportista.HistorialFragment;
import com.example.logitex_app.ui.transportista.DetallRutaFragment;
import com.example.logitex_app.ui.perfil.PerfilFragment;
import com.example.logitex_app.utils.HelpDialogHelper;
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

        // Logica de tancament de sessio per esborrar les dades de memoria
        btnLogout.setOnClickListener(v -> {
            // Esborrem el token i el rol de les preferencies
            SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Tornem a la pantalla d'inici de sessio
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            Toast.makeText(this, getString(R.string.desc_logout), Toast.LENGTH_SHORT).show();
        });

        // Boto d'ajuda contextual integrada a l'aplicacio
        ImageView btnHelp = findViewById(R.id.btnHeaderHelp);
        btnHelp.setOnClickListener(v -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            boolean isEn = com.example.logitex_app.utils.LocaleHelper.getLanguage(this).equalsIgnoreCase("en");

            if (currentFragment instanceof RutasFragment) {
                if (isEn) {
                    HelpDialogHelper.mostrarAjuda(this, "Assigned Routes",
                        new String[]{"1. Route List", "2. Start Route", "3. In Transit Status", "4. Auto-refresh"},
                        new String[]{
                            "View the orders assigned to you as a carrier that are ready for delivery.",
                            "Swipe or click an order pending preparation to officially start your delivery route.",
                            "When the route starts, the order status changes to 'In transit', enabling the delivery scanner.",
                            "The list fully reloads in the background every 10 seconds to receive new assignments instantly."
                        });
                } else {
                    HelpDialogHelper.mostrarAjuda(this, "Rutes Assignades",
                        new String[]{"1. Llistat de Rutes", "2. Iniciar Ruta", "3. Estat en Trànsit", "4. Actualització"},
                        new String[]{
                            "Visualitza les comandes que tens assignades com a transportista i que ja estan llestes per al lliurament.",
                            "Llisca o prem una comanda pendent de preparació per donar inici oficial a la teva ruta de repartiment.",
                            "Quan la ruta comenci, l'estat de l'ordre passarà a 'En trànsit', habilitant l'escaner de lliurament.",
                            "La llista es recarrega completament en segon pla cada 10 segons per rebre noves assignacions a l'instant."
                        });
                }
            } else if (currentFragment instanceof DetallRutaFragment) {
                if (isEn) {
                    HelpDialogHelper.mostrarAjuda(this, "Order Details",
                        new String[]{"1. Core Information", "2. Embedded Map", "3. Scan QR", "4. Report Incident"},
                        new String[]{
                            "Contains the order name, delivery note reference, delivery address, and current logistical status.",
                            "View delivery location. Tapping the map card opens Google Maps to guide you to the destination.",
                            "When at the delivery point, tap the green button to open the camera and scan the box's QR code to confirm delivery.",
                            "If there's any unforeseen issue (recipient absent, damaged package, etc.), use the red button to describe the incident."
                        });
                } else {
                    HelpDialogHelper.mostrarAjuda(this, "Detall de la Comanda",
                        new String[]{"1. Informació Principal", "2. Mapa Incorporat", "3. Escanejar QR", "4. Notificar Incidència"},
                        new String[]{
                            "Conté el nom de la comanda, referència d'albarà, adreça i estat logístic actual.",
                            "Visualitza la ubicació. En prémer la targeta del mapa, s'obrirà Google Maps per guiar-te a la destinació.",
                            "Quan estiguis al punto de lliurament, prem el botó verd per obrir la càmera i escanejar el codi QR de la caixa per confirmar l'entrega.",
                            "Si hi ha algun imprevist (destinatari absent, paquet danyat, etc.), utilitza el botó vermell per descriure el cas."
                        });
                }
            } else if (currentFragment instanceof ScanQrFragment) {
                if (isEn) {
                    HelpDialogHelper.mostrarAjuda(this, "Scan QR Code",
                        new String[]{"1. Open Camera", "2. Auto Search", "3. Secure Reading"},
                        new String[]{
                            "Tap the center button to activate your device's camera.",
                            "Frame the package QR code. The scanner decodes the ID and automatically opens the corresponding order details.",
                            "Only valid QR codes from the Logitex platform are accepted."
                        });
                } else {
                    HelpDialogHelper.mostrarAjuda(this, "Escanejar Codi QR",
                        new String[]{"1. Obrir Càmera", "2. Cerca Automàtica", "3. Lectura Segura"},
                        new String[]{
                            "Prem el botó central per activar la càmera del dispositiu.",
                            "Enquadra el codi QR del paquet. El lector desxifrarà la ID i obrirà automàticament la pantalla de detalls de l'ordre corresponent.",
                            "Només s'accepten codis QR vàlids de la plataforma Logitex."
                        });
                }
            } else if (currentFragment instanceof IncidenciaFragment) {
                if (isEn) {
                    HelpDialogHelper.mostrarAjuda(this, "Incident Reporting",
                        new String[]{"1. Linked Order", "2. Assignee Group", "3. Priority", "4. Creator"},
                        new String[]{
                            "The affected order is pre-selected if you come from its details sheet, or you can choose another from the menu.",
                            "Select which warehouse group should receive the resolution alert based on the nature of the issue.",
                            "Set urgency to High, Medium, or Low depending on the route impact.",
                            "The incident is saved automatically under your active carrier profile."
                        });
                } else {
                    HelpDialogHelper.mostrarAjuda(this, "Notificació d'Incidències",
                        new String[]{"1. Comanda Associada", "2. Responsable", "3. Prioritat", "4. Autor"},
                        new String[]{
                            "L'ordre afectada es pre-selecciona sola si vens de la seva fitxa de detall, o pots escollir-ne una altra del menú.",
                            "Selecciona quin grup de mozos ha de rebre l'alerta de resolució segons la naturalesa del problema.",
                            "Defineix la urgència en Alta, Mitjana o Baixa segons l'impacte en la ruta.",
                            "La incidència es desarà automàticament sota el teu perfil de transportista actiu."
                        });
                }
            } else if (currentFragment instanceof HistorialFragment) {
                if (isEn) {
                    HelpDialogHelper.mostrarAjuda(this, "Delivery History",
                        new String[]{"1. Your Records", "2. Query Details", "3. Traceability"},
                        new String[]{
                            "Browse the complete history of all orders you have delivered or handled.",
                            "Tap any history order to check its parameters and report an incident if necessary.",
                            "View the entire timeline to verify the exact day, hour, and operator who changed the status."
                        });
                } else {
                    HelpDialogHelper.mostrarAjuda(this, "Historial de Lliuraments",
                        new String[]{"1. El teu Registre", "2. Consulta de Detalls", "3. Traçabilitat"},
                        new String[]{
                            "Consulta el llistat històric complet de totes les ordres que has lliurat o gestionat.",
                            "Prem sobre qualsevol comanda històrica per comprovar ses seves dades i obrir la incidència si escau.",
                            "Pots veure la línia de temps sencera per verificar quin dia, hora i usuari va canviar l'estat."
                        });
                }
            } else if (currentFragment instanceof PickingFragment) {
                if (isEn) {
                    HelpDialogHelper.mostrarAjuda(this, "Picking Tasks (Mozo)",
                        new String[]{"1. Package Preparation", "2. Confirm Pallets", "3. Code Validation"},
                        new String[]{
                            "List of orders assigned to your warehouse group for immediate preparation.",
                            "Tap to start the preparation process and confirm the items in each pallet.",
                            "Complete package preparations using scans or QR codes for full traceability."
                        });
                } else {
                    HelpDialogHelper.mostrarAjuda(this, "Tasques de Picking (Mozo)",
                        new String[]{"1. Preparació de Paquets", "2. Confirmar Palets", "3. Validació de Codi"},
                        new String[]{
                            "Llista de comandes assignades al teu grup de mozos per a la preparació immediata.",
                            "Prem per obrir el procés i confirmar el contingut de cada palet.",
                            "Realitza les preparacions amb escaneig o codis QR per a una traçabilitat absoluta."
                        });
                }
            } else if (currentFragment instanceof UbicacioFragment) {
                if (isEn) {
                    HelpDialogHelper.mostrarAjuda(this, "Location Management (Mozo)",
                        new String[]{"1. Scan Pallet", "2. Assign Shelf", "3. Warehouse Control"},
                        new String[]{
                            "Activate the scanner to identify the pallet you want to relocate.",
                            "The app guides you to the optimal warehouse zone to store the pallet.",
                            "Register the storage change instantly on the server to avoid stock discrepancies."
                        });
                } else {
                    HelpDialogHelper.mostrarAjuda(this, "Gestió de Ubicació (Mozo)",
                        new String[]{"1. Escanejar Palet", "2. Assignar Prestatge", "3. Control de Magatzem"},
                        new String[]{
                            "Activa l'escaner per identificar el palet que vols reubicar.",
                            "La app t'indicarà la zona de magatzem òptima per desar el contingut.",
                            "Registra el canvi a l'instant al servidor per evitar pèrdues de stock."
                        });
                }
            } else if (currentFragment instanceof PerfilFragment) {
                if (isEn) {
                    HelpDialogHelper.mostrarAjuda(this, "User Profile",
                        new String[]{"1. Personal Data", "2. Authentication"},
                        new String[]{
                            "Displays your active credentials, including your email and phone number fetched from the server.",
                            "If any of this information is incorrect, please contact the central warehouse administrator to update it."
                        });
                } else {
                    HelpDialogHelper.mostrarAjuda(this, "Perfil d'Usuari",
                        new String[]{"1. Dades Personals", "2. Autenticació"},
                        new String[]{
                            "Mostra les teves credencials actives, incloent-hi el correu electrònic i el telèfon vinculats al servidor.",
                            "Si alguna d'aquestes dades és incorrecta, posa't en contacte amb l'administrador del magatzem central per actualitzar-les."
                        });
                }
            } else {
                if (isEn) {
                    HelpDialogHelper.mostrarAjuda(this, "Logitex Help",
                        new String[]{"Welcome to Logitex App", "How does it work?"},
                        new String[]{
                            "This mobile application allows you to manage transport and picking tasks integrated with the web platform.",
                            "Choose an option from the bottom navigation bar to get started. Tap the info 'i' icon in the top header on any screen to receive contextual help."
                        });
                } else {
                    HelpDialogHelper.mostrarAjuda(this, "Logitex Help",
                        new String[]{"Benvingut a Logitex App", "Com funciona?"},
                        new String[]{
                            "Aquesta aplicació mòbil permet gestionar tasques de transport i picking de forma integrada amb la plataforma web.",
                            "Tria una opció del menú inferior de navegació per començar a operar. Prem la icona de la lletra 'i' superior en qualsevol pantalla per rebre ajuda contextualitzada."
                        });
                }
            }
        });

        // Boto per canviar d'idioma
        TextView btnLang = findViewById(R.id.btnHeaderLang);
        String currentLang = com.example.logitex_app.utils.LocaleHelper.getLanguage(this);
        btnLang.setText(currentLang.equalsIgnoreCase("en") ? "EN" : "CAT");

        btnLang.setOnClickListener(v -> {
            String newLang = com.example.logitex_app.utils.LocaleHelper.getLanguage(this).equalsIgnoreCase("en") ? "ca" : "en";
            com.example.logitex_app.utils.LocaleHelper.setLocale(this, newLang);
            recreate();
        });

        // Recuperem les dades de les preferencies i si no hi son les agafem del intent
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String rol = prefs.getString("ROL_USUARIO", getIntent().getStringExtra("USER_ROLE"));

        // Configurem el menu de navegacio segons el rol de l'usuari
        if (rol != null) {
            if (rol.equalsIgnoreCase("MOZO") || rol.equalsIgnoreCase("MOSSO")) {
                configurarMenuMozo();
                cargarFragment(new PickingFragment());
            } else if (rol.equalsIgnoreCase("TRANSPORTISTA")) {
                configurarMenuTransportista();
                cargarFragment(new RutasFragment());
            } else {
                // En cas que el rol no sigui cap dels definits
                Toast.makeText(this, com.example.logitex_app.utils.TranslationHelper.get(this, "Rol desconegut: ", "Unknown role: ") + rol, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.error_rol), Toast.LENGTH_SHORT).show();
        }
    }

    // Metode centralitzat per carregar i canviar de fragment a la pantalla
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
                cargarFragment(new ScanQrFragment());
                return true;
            } else if (itemId == R.id.nav_incidencies_list) {
                cargarFragment(new com.example.logitex_app.ui.mozo.IncidenciaListFragment());
                return true;
            } else if (itemId == R.id.nav_perfil) {
                cargarFragment(new PerfilFragment());
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
            } else if (itemId == R.id.nav_qr) {
                cargarFragment(new ScanQrFragment());
                return true;
            } else if (itemId == R.id.nav_incidencias) {
                cargarFragment(new IncidenciaFragment());
                return true;
            } else if (itemId == R.id.nav_historial) {
                cargarFragment(new HistorialFragment());
                return true;
            } else if (itemId == R.id.nav_perfil) {
                cargarFragment(new PerfilFragment());
                return true;
            }
            return false;
        });
    }

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.example.logitex_app.utils.LocaleHelper.onAttach(newBase, "ca"));
    }
}