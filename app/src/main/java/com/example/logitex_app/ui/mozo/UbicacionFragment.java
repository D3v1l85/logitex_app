package com.example.logitex_app.ui.mozo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.logitex_app.R;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.logitex_app.api.ApiService;
import com.example.logitex_app.api.RetrofitClient;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UbicacionFragment extends Fragment {

    private DecoratedBarcodeView barcodeView;

    // 1. Esto es un "lanzador" mágico de Android que pide el permiso al usuario en pantalla
    private final ActivityResultLauncher<String> peticionPermisoCamara =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    iniciarCamara(); // Si le da a "Permitir", encendemos la cámara
                } else {
                    Toast.makeText(getContext(), "Necessitem la càmera per escanejar els palets", Toast.LENGTH_LONG).show();
                }
            });

    public UbicacionFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ubicacion, container, false);

        barcodeView = view.findViewById(R.id.barcode_scanner);

        // 2. Comprobamos si YA tenemos el permiso concedido de antes
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara();
        } else {
            // Si no lo tenemos, lanzamos la pregunta al usuario
            peticionPermisoCamara.launch(Manifest.permission.CAMERA);
        }

        return view;
    }

    private void iniciarCamara() {
        barcodeView.resume();

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    barcodeView.pause(); // Pausamos para no enviar 40 peticiones por segundo

                    String codigoLeido = result.getText();
                    Toast.makeText(getContext(), "Codi llegit: " + codigoLeido, Toast.LENGTH_SHORT).show();

                    try {
                        // Suponemos que el QR contiene el número de ID directamente (Ej: "5")
                        int idPalet = Integer.parseInt(codigoLeido.trim());

                        // Llamamos al servidor y le decimos que el nuevo estado es "RECOLLIT" (o el que queráis)
                        enviarNuevoEstado(idPalet, "RECOLLIT");

                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Error: El codi QR no és un ID vàlid", Toast.LENGTH_LONG).show();
                        barcodeView.resume(); // Volvemos a encender la cámara si falló
                    }
                }
            }
        });
    }

    // --- NUEVA FUNCIÓN PARA LLAMAR A LA API ---
    private void enviarNuevoEstado(int idPalet, String nuevoEstado) {
        // 1. Sacamos el Token
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");

        // 2. Preparamos Retrofit
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<JsonObject> call = apiService.cambiarEstado("Bearer " + token, idPalet, nuevoEstado);

        // 3. Enviamos la petición
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Estat actualitzat correctament a " + nuevoEstado, Toast.LENGTH_LONG).show();
                    // Opcional: Podríamos volver a la pantalla anterior o dejar que escanee otro
                } else {
                    Toast.makeText(getContext(), "Error al actualitzar: " + response.code(), Toast.LENGTH_LONG).show();
                }

                // Damos un par de segundos de respiro y reactivamos la cámara por si quiere leer otro
                barcodeView.postDelayed(() -> barcodeView.resume(), 2000);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "Error de connexió: " + t.getMessage(), Toast.LENGTH_LONG).show();
                barcodeView.resume(); // Reactivamos la cámara
            }
        });
    }

    // --- Ciclo de vida de la app (Para no gastar batería si la app se minimiza) ---
    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            barcodeView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause(); // Apagamos la cámara si minimizan la app
    }
}