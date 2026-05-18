package com.example.logitex_app.ui.transportista;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.logitex_app.R;
import com.example.logitex_app.api.ApiService;
import com.example.logitex_app.api.RetrofitClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleRutaFragment extends Fragment implements OnMapReadyCallback {

    private String estadoActual;
    private int idOrdenActual;
    private String direccioDesti = "";

    private Button btnEscaner;
    private TextView tvEstado;
    private EditText etManual;

    private MapView mapView;
    private GoogleMap mGoogleMap;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    procesarAccion();
                } else {
                    Toast.makeText(getContext(), getString(R.string.msg_escaneig_cancel), Toast.LENGTH_SHORT).show();
                }
            });

    public DetalleRutaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_ruta, container, false);

        TextView tvAlbara = view.findViewById(R.id.tvDetalleAlbara);
        TextView tvDireccion = view.findViewById(R.id.tvDetalleDireccion);
        tvEstado = view.findViewById(R.id.tvEstadoActual);
        btnEscaner = view.findViewById(R.id.btnAbrirEscaner);
        etManual = view.findViewById(R.id.etCodigoManualRuta);
        Button btnConfirmarManual = view.findViewById(R.id.btnConfirmarManual);
        Button btnIncidencia = view.findViewById(R.id.btnAbrirIncidencia);
        Button btnVerMapa = view.findViewById(R.id.btnVerMapa);

        mapView = view.findViewById(R.id.mapViewDetalle);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        if (getArguments() != null) {
            idOrdenActual = getArguments().getInt("id_orden", 0);
            String ordenNom = getArguments().getString("orden_nom", "ORD-000");
            String albara = getArguments().getString("albara_id", "ALB-000");
            direccioDesti = getArguments().getString("albara_dir", "Desconeguda");
            estadoActual = getArguments().getString("albara_estat", "PENDENT");

            tvAlbara.setText(getString(R.string.format_ordre, ordenNom));
            tvDireccion.setText(getString(R.string.format_albara_direccio, albara, direccioDesti));

            actualizarInterfaz();
        }

        btnEscaner.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt(estadoActual.equalsIgnoreCase("PENDENT") ? getString(R.string.prompt_rebre) : getString(R.string.prompt_lliurar));
            options.setBeepEnabled(true);
            options.setBarcodeImageEnabled(false);
            options.setOrientationLocked(false);
            options.setCameraId(0);
            barcodeLauncher.launch(options);
        });

        btnConfirmarManual.setOnClickListener(v -> {
            String codigo = etManual.getText().toString().trim();
            if (!codigo.isEmpty()) {
                procesarAccion();
            } else {
                Toast.makeText(getContext(), getString(R.string.msg_introdueix_codi), Toast.LENGTH_SHORT).show();
            }
        });

        btnIncidencia.setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new IncidenciasFragment())
                .addToBackStack(null).commit());

        btnVerMapa.setOnClickListener(v -> {
            if (!direccioDesti.isEmpty() && !direccioDesti.equalsIgnoreCase("Desconeguda")) {
                android.net.Uri gmmIntentUri = android.net.Uri.parse("google.navigation:q=" + android.net.Uri.encode(direccioDesti));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        if (!direccioDesti.isEmpty() && !direccioDesti.equalsIgnoreCase("Desconeguda")) {
            posicionarDireccionEnMapa(direccioDesti);
        }
    }

    private void posicionarDireccionEnMapa(String direccionTexto) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> direccionesEncontradas = geocoder.getFromLocationName(direccionTexto, 1);
            if (direccionesEncontradas != null && !direccionesEncontradas.isEmpty()) {
                Address ubicacion = direccionesEncontradas.get(0);
                LatLng destinoLatLng = new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude());

                mGoogleMap.addMarker(new MarkerOptions()
                        .position(destinoLatLng)
                        .title(getString(R.string.menu_rutas)));

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinoLatLng, 15f));
            }
        } catch (IOException e) {
            Log.e("DetalleRuta", "Error al posicionar el mapa", e);
        }
    }

    private void actualizarInterfaz() {
        tvEstado.setText(getString(R.string.format_estat, estadoActual));

        // Buscamos los botones e inputs adicionales para poder gestionarlos
        Button btnIncidencia = requireView().findViewById(R.id.btnAbrirIncidencia);
        View cardMapa = requireView().findViewById(R.id.cardMapa);
        View layoutManualInput = requireView().findViewById(R.id.layoutManualInput);

        if (estadoActual.equalsIgnoreCase("ENTREGAT")) {
            tvEstado.setTextColor(android.graphics.Color.parseColor("#4CAF50"));

            // ESCONDEMOS ABSOLUTAMENTE TODO LO INTERACTIVO
            btnEscaner.setVisibility(View.GONE);
            if (btnIncidencia != null) btnIncidencia.setVisibility(View.GONE);
            if (cardMapa != null) cardMapa.setVisibility(View.GONE);
            if (layoutManualInput != null) layoutManualInput.setVisibility(View.GONE);

        } else if (estadoActual.equalsIgnoreCase("PENDENT") || estadoActual.equalsIgnoreCase("PENDENT_ENTREGA")) {
            tvEstado.setTextColor(android.graphics.Color.parseColor("#0056b3"));
            btnEscaner.setVisibility(View.VISIBLE);
            if (btnIncidencia != null) btnIncidencia.setVisibility(View.VISIBLE);
            if (cardMapa != null) cardMapa.setVisibility(View.VISIBLE);
            if (layoutManualInput != null) layoutManualInput.setVisibility(View.VISIBLE);

            btnEscaner.setText(getString(R.string.btn_rebre_mercaderia));
            btnEscaner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0056b3")));

        } else if (estadoActual.equalsIgnoreCase("EN_TRANSIT") || estadoActual.equalsIgnoreCase("EN RUTA")) {
            tvEstado.setTextColor(android.graphics.Color.parseColor("#FF9800"));
            btnEscaner.setVisibility(View.VISIBLE);
            if (btnIncidencia != null) btnIncidencia.setVisibility(View.VISIBLE);
            if (cardMapa != null) cardMapa.setVisibility(View.VISIBLE);
            if (layoutManualInput != null) layoutManualInput.setVisibility(View.VISIBLE);

            btnEscaner.setText(getString(R.string.btn_confirmar_lliurament));
            btnEscaner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
        } else {
            btnEscaner.setVisibility(View.GONE);
        }
    }

    private void procesarAccion() {
        String proximoEstado = (estadoActual.equalsIgnoreCase("PENDENT") || estadoActual.equalsIgnoreCase("PENDENT_ENTREGA"))
                ? "EN_TRANSIT" : "ENTREGAT";
        enviarCambioEstado(proximoEstado);
    }

    private void enviarCambioEstado(String nuevoEstado) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<JsonObject> call = apiService.cambiarEstadoOrden("Bearer " + token, idOrdenActual, nuevoEstado);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), getString(R.string.format_ordre_actualitzada, nuevoEstado), Toast.LENGTH_SHORT).show();
                    estadoActual = nuevoEstado;
                    actualizarInterfaz();
                    etManual.setText("");
                } else {
                    Toast.makeText(getContext(), getString(R.string.err_servidor, response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.err_connexio), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }
}