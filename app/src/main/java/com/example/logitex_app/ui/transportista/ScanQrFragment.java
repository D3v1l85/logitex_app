package com.example.logitex_app.ui.transportista;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.logitex_app.R;
import com.example.logitex_app.api.ApiService;
import com.example.logitex_app.api.RetrofitClient;
import com.example.logitex_app.models.Ordre;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanQrFragment extends Fragment {

    private TextView tvStatus;
    private ProgressBar pbLoading;
    private EditText etManual;
    private boolean shouldAutoScan = true;

    // Llançador de l'escaner en format emergent
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String scannedText = result.getContents();
                    procesarCodiEscanejat(scannedText);
                } else {
                    tvStatus.setText(com.example.logitex_app.utils.TranslationHelper.scanCancelled(getContext()));
                }
            }
    );

    public ScanQrFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_qr, container, false);

        tvStatus = view.findViewById(R.id.tvScanStatus);
        pbLoading = view.findViewById(R.id.pbScanLoading);
        etManual = view.findViewById(R.id.etManualOrderId);
        Button btnOpen = view.findViewById(R.id.btnOpenScanner);
        ImageButton btnSearch = view.findViewById(R.id.btnSearchManual);

        // Tradueix els textos estatics de la pantalla d'escaner
        TextView tvScanTitle = view.findViewById(R.id.tvScanTitle);
        TextView tvScanDescription = view.findViewById(R.id.tvScanDescription);
        TextView tvScanManualText = view.findViewById(R.id.tvScanManualText);

        if (tvScanTitle != null) tvScanTitle.setText(com.example.logitex_app.utils.TranslationHelper.scanTitle(getContext()));
        if (tvScanDescription != null) tvScanDescription.setText(com.example.logitex_app.utils.TranslationHelper.scanDescription(getContext()));
        if (tvStatus != null) tvStatus.setText(com.example.logitex_app.utils.TranslationHelper.readyToScan(getContext()));
        if (btnOpen != null) btnOpen.setText(com.example.logitex_app.utils.TranslationHelper.openCamera(getContext()));
        if (tvScanManualText != null) tvScanManualText.setText(com.example.logitex_app.utils.TranslationHelper.orWriteManual(getContext()));
        if (etManual != null) etManual.setHint(com.example.logitex_app.utils.TranslationHelper.manualHint(getContext()));

        btnOpen.setOnClickListener(v -> abrirEscanerPopup());

        btnSearch.setOnClickListener(v -> {
            String manualText = etManual.getText().toString().trim();
            if (!manualText.isEmpty()) {
                procesarCodiEscanejat(manualText);
            } else {
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.enterCodeFirst(getContext()), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void abrirEscanerPopup() {
        tvStatus.setText(com.example.logitex_app.utils.TranslationHelper.scanScanning(getContext()));
        ScanOptions options = new ScanOptions();
        options.setPrompt(com.example.logitex_app.utils.TranslationHelper.promptScanDefault(getContext()));
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(false);
        options.setOrientationLocked(false);
        options.setCameraId(0);
        barcodeLauncher.launch(options);
    }

    private void procesarCodiEscanejat(String rawCode) {
        String parsedCode = parseScannedCode(rawCode);
        if (parsedCode.isEmpty()) {
            Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.invalidCode(getContext()), Toast.LENGTH_SHORT).show();
            return;
        }

        buscarOrdenEnServidor(parsedCode);
    }

    // Metode per desxifrar qualsevol format de codi QR del sistema
    private String parseScannedCode(String scannedText) {
        if (scannedText == null) return "";
        scannedText = scannedText.trim();

        // Si es tracta d'un codi d'albara
        if (scannedText.startsWith("ALB-")) {
            try {
                String[] parts = scannedText.split("-");
                if (parts.length >= 3) {
                    int numericVal = Integer.parseInt(parts[2]);
                    int idOrdre = numericVal - 10000;
                    return String.valueOf(idOrdre);
                }
            } catch (Exception e) {
                // fall back to default
            }
        }
        return scannedText;
    }

    private void buscarOrdenEnServidor(String idOReferencia) {
        tvStatus.setText(com.example.logitex_app.utils.TranslationHelper.searchingOrder(getContext(), idOReferencia));
        pbLoading.setVisibility(View.VISIBLE);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Ordre> call = apiService.getOrdreById("Bearer " + token, idOReferencia);

        call.enqueue(new Callback<Ordre>() {
            @Override
            public void onResponse(@NonNull Call<Ordre> call, @NonNull Response<Ordre> response) {
                if (getContext() == null) return;
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Ordre order = response.body();
                    tvStatus.setText(com.example.logitex_app.utils.TranslationHelper.orderFound(getContext()));

                    // Netegem el camp de cerca manual
                    etManual.setText("");

                    int rolLogueado = prefs.getInt("USER_ROL", 4);
                    Fragment nextFragment;
                    Bundle args = new Bundle();
                    args.putInt("id_orden", order.getId());
                    args.putString("orden_nom", order.getIdentificador());
                    args.putString("albara_id", order.getReferencia());
                    args.putString("albara_dir", order.getDireccio());
                    args.putString("albara_estat", order.getEstat());
                    args.putString("tenda_destinataria", order.getClient());

                    if (rolLogueado == 3 || rolLogueado == 8) {
                        String estat = order.getEstat();
                        if (estat == null || (!estat.equalsIgnoreCase("PENDENT_PREPARACIO") && !estat.equalsIgnoreCase("PREPARACIO_EN_CURS") && !estat.equalsIgnoreCase("PREPARACIO_FINALITZADA"))) {
                            Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Ordre no disponible", "Order not available"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        nextFragment = new com.example.logitex_app.ui.mozo.DetallPickingFragment();
                    } else {
                        nextFragment = new DetallRutaFragment();
                    }
                    nextFragment.setArguments(args);

                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, nextFragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    tvStatus.setText(com.example.logitex_app.utils.TranslationHelper.orderNotFoundOrError(getContext()));
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.orderNotFound(getContext()), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Ordre> call, @NonNull Throwable t) {
                if (getContext() == null) return;
                pbLoading.setVisibility(View.GONE);
                tvStatus.setText(com.example.logitex_app.utils.TranslationHelper.connectionError(getContext()));
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.serverError(getContext()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
