package com.example.logitex_app.ui.transportista;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.gson.JsonObject;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleRutaFragment extends Fragment {

    private String estadoActual;
    private int idOrdenActual;
    private Button btnEscaner;
    private TextView tvEstado;
    private EditText etManual;

    // Lanzador de la cámara en Popup
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    procesarAccion();
                } else {
                    Toast.makeText(getContext(), "Escaneig cancel·lat", Toast.LENGTH_SHORT).show();
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

        if (getArguments() != null) {
            idOrdenActual = getArguments().getInt("id_orden", 0);
            String ordenNom = getArguments().getString("orden_nom", "ORD-000"); // Título principal
            String albara = getArguments().getString("albara_id", "ALB-000");     // Secundario
            String direccio = getArguments().getString("albara_dir", "Desconeguda");
            estadoActual = getArguments().getString("albara_estat", "PENDENT");

            // Título principal con el nombre de la orden
            tvAlbara.setText("Ordre: " + ordenNom);

            // Mostramos el albarán en la dirección o un campo extra
            tvDireccion.setText("Albarà: " + albara + "\nDirecció: " + direccio);

            actualizarInterfaz();
        }

        // Abrir la cámara en Popup (Configuración final para vertical)
        btnEscaner.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt(estadoActual.equalsIgnoreCase("PENDENT") ? "Enquadra el codi QR per REBRE" : "Enquadra el codi QR per LLIURAR");
            options.setBeepEnabled(true);
            options.setBarcodeImageEnabled(false);
            options.setOrientationLocked(false);
            options.setCameraId(0);
            barcodeLauncher.launch(options);
        });

        // Botón de confirmación manual
        btnConfirmarManual.setOnClickListener(v -> {
            String codigo = etManual.getText().toString().trim();
            if (!codigo.isEmpty()) {
                procesarAccion();
            } else {
                Toast.makeText(getContext(), "Introdueix el codi primer", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón de Incidencias
        btnIncidencia.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new IncidenciasFragment())
                    .addToBackStack(null).commit();
        });

        return view;
    }

    // --- AQUÍ ESTÁ LA MAGIA VISUAL ---
    private void actualizarInterfaz() {
        tvEstado.setText("Estat: " + estadoActual);

        if (estadoActual.equalsIgnoreCase("ENTREGAT")) {
            // 1. Si está entregado: Texto en verde y ocultamos toda la interacción
            tvEstado.setTextColor(android.graphics.Color.parseColor("#4CAF50"));

            btnEscaner.setVisibility(View.GONE);
            etManual.setVisibility(View.GONE);

            if (getView() != null) {
                // Buscamos los otros botones directamente en la vista para ocultarlos
                View btnConfirmar = getView().findViewById(R.id.btnConfirmarManual);
                if (btnConfirmar != null) btnConfirmar.setVisibility(View.GONE);

                View btnIncidencia = getView().findViewById(R.id.btnAbrirIncidencia);
                if (btnIncidencia != null) btnIncidencia.setVisibility(View.GONE);

                // Ocultamos el texto de ayuda si le pusiste el ID "tvManualHint" en el XML
                View tvHint = getView().findViewById(R.id.tvManualHint);
                if (tvHint != null) tvHint.setVisibility(View.GONE);
            }

        } else if (estadoActual.equalsIgnoreCase("PENDENT") || estadoActual.equalsIgnoreCase("PENDENT_ENTREGA")) {
            // 2. Si está pendiente: Botón azul
            tvEstado.setTextColor(android.graphics.Color.parseColor("#0056b3"));
            btnEscaner.setVisibility(View.VISIBLE);
            btnEscaner.setText("REBRE MERCADERIA (Càmera)");
            btnEscaner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0056b3")));

        } else if (estadoActual.equalsIgnoreCase("EN_TRANSIT") || estadoActual.equalsIgnoreCase("EN RUTA")) {
            // 3. Si está en tránsito: Botón verde para entregar
            tvEstado.setTextColor(android.graphics.Color.parseColor("#FF9800")); // Naranja para destacar que está en camino
            btnEscaner.setVisibility(View.VISIBLE);
            btnEscaner.setText("CONFIRMAR LLIURAMENT (Càmera)");
            btnEscaner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
        } else {
            // Por si acaso recibe un estado desconocido
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

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Ordre actualitzada a " + nuevoEstado, Toast.LENGTH_SHORT).show();
                    estadoActual = nuevoEstado;
                    actualizarInterfaz();
                    etManual.setText("");
                } else {
                    Toast.makeText(getContext(), "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "Error de connexió", Toast.LENGTH_SHORT).show();
            }
        });
    }
}