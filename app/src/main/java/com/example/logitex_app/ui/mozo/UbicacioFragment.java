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

// Fragment que gestiona l'escaner de codis QR per a la ubicacio i recollida de palets
public class UbicacioFragment extends Fragment {

    private DecoratedBarcodeView barcodeView;

    // Llançador d'Android que sol·licita el permís de càmera a l'usuari en pantalla
    private final ActivityResultLauncher<String> peticionPermisoCamara =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    iniciarCamara(); // Si l'usuari accepta el permís activem la càmera
                } else {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Necessitem la càmera per escanejar els palets", "We need the camera to scan pallets"), Toast.LENGTH_LONG).show();
                }
            });

    public UbicacioFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ubicacio, container, false);

        barcodeView = view.findViewById(R.id.barcode_scanner);

        // Comprovem si ja tenim el permís de càmera concedit anteriorment
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara();
        } else {
            // Si no tenim el permís demanem la confirmació a l'usuari
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
                    barcodeView.pause(); // Pausem l'escaner per evitar enviar múltiples peticions alhora

                    String codigoLeido = result.getText();
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Codi llegit: ", "Code read: ") + codigoLeido, Toast.LENGTH_SHORT).show();

                    try {
                        // Suposem que el codi QR conté el número d'identificador del palet
                        int idPalet = Integer.parseInt(codigoLeido.trim());

                        // Truquem al servidor per indicar que el nou estat del palet és recollit
                        enviarNuevoEstado(idPalet, "RECOLLIT");

                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Error: El codi QR no és un ID vàlid", "Error: The QR code is not a valid ID"), Toast.LENGTH_LONG).show();
                        barcodeView.resume(); // Tornem a activar la càmera en cas que falli la lectura
                    }
                }
            }
        });
    }

    // Envia el nou estat del palet al servidor
    private void enviarNuevoEstado(int idPalet, String nuevoEstado) {
        // Obtenim el token d'autenticació guardat
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");

        // Preparem la connexió amb la interfície api
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<JsonObject> call = apiService.cambiarEstado("Bearer " + token, idPalet, nuevoEstado);

        // Enviem la petició de canvi d'estat
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Estat actualitzat correctament a ", "Status updated successfully to ") + nuevoEstado, Toast.LENGTH_LONG).show();
                    // Podríem tornar enrere o deixar que continuï escanejant més palets
                } else {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Error al actualitzar: ", "Error updating: ") + response.code(), Toast.LENGTH_LONG).show();
                }

                // Esperem un parell de segons i reactivem la càmera per poder fer una nova lectura
                barcodeView.postDelayed(() -> barcodeView.resume(), 2000);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Error de connexió: ", "Connection error: ") + t.getMessage(), Toast.LENGTH_LONG).show();
                barcodeView.resume(); // Tornem a engegar la càmera
            }
        });
    }

    // Gestió del cicle de vida de la pantalla per estalviar bateria
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
        barcodeView.pause(); // Apaguem la càmera si l'usuari surt de l'aplicació
    }
}