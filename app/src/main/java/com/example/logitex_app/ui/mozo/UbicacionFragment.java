package com.example.logitex_app.ui.mozo;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.logitex_app.api.ApiService;
import com.example.logitex_app.api.RetrofitClient;
import com.google.gson.JsonObject;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UbicacionFragment extends Fragment {

    private DecoratedBarcodeView barcodeView;

    private final ActivityResultLauncher<String> peticionPermisoCamara =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    iniciarCamara();
                } else {
                    Toast.makeText(getContext(), getString(R.string.msg_permis_camera), Toast.LENGTH_LONG).show();
                }
            });

    public UbicacionFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ubicacion, container, false);

        barcodeView = view.findViewById(R.id.barcode_scanner);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara();
        } else {
            peticionPermisoCamara.launch(Manifest.permission.CAMERA);
        }

        return view;
    }

    private void iniciarCamara() {
        barcodeView.resume();

        barcodeView.decodeContinuous(result -> {
            if (result.getText() != null) {
                barcodeView.pause();

                String codigoLeido = result.getText();
                Toast.makeText(getContext(), getString(R.string.format_codi_llegit, codigoLeido), Toast.LENGTH_SHORT).show();

                try {
                    int idPalet = Integer.parseInt(codigoLeido.trim());
                    enviarNuevoEstado(idPalet, "RECOLLIT");

                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), getString(R.string.err_codi_qr_invalid), Toast.LENGTH_LONG).show();
                    barcodeView.resume();
                }
            }
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void enviarNuevoEstado(int idPalet, String nuevoEstado) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<JsonObject> call = apiService.cambiarEstado("Bearer " + token, idPalet, nuevoEstado);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), getString(R.string.format_estat_actualitzat, nuevoEstado), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.err_servidor, response.code()), Toast.LENGTH_LONG).show();
                }

                barcodeView.postDelayed(() -> barcodeView.resume(), 2000);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.format_error_connexio_detall, t.getMessage()), Toast.LENGTH_LONG).show();
                barcodeView.resume();
            }
        });
    }

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
        barcodeView.pause();
    }
}