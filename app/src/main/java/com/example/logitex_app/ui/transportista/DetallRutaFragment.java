package com.example.logitex_app.ui.transportista;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.logitex_app.R;
import com.example.logitex_app.api.ApiService;
import com.example.logitex_app.api.RetrofitClient;
import com.example.logitex_app.models.Ordre;
import com.example.logitex_app.models.Tracking;
import com.google.gson.JsonObject;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallRutaFragment extends Fragment {

    private String estadoActual;
    private int idOrdenActual;
    private String ordenNom;
    private String albara;
    private String direccio;
    private Button btnEscaner;
    private TextView tvEstado;
    private View cardHistorial;
    private LinearLayout llHistorialCambios;

    // Llançador de la càmera en format emergent
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String scannedContent = result.getContents().trim();
                    if (scannedContent.equals(String.valueOf(idOrdenActual)) 
                            || scannedContent.equalsIgnoreCase(ordenNom)
                            || scannedContent.equalsIgnoreCase(albara)) {
                        procesarAccion();
                    } else {
                        Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.errorQrMismatch(getContext()), Toast.LENGTH_LONG).show();
                        if (getActivity() != null) {
                            getParentFragmentManager().popBackStack();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.scanCancelled(getContext()), Toast.LENGTH_SHORT).show();
                }
            });

    public DetallRutaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detall_ruta, container, false);

        TextView tvAlbara = view.findViewById(R.id.tvDetalleAlbara);
        TextView tvDireccion = view.findViewById(R.id.tvDetalleDireccion);
        tvEstado = view.findViewById(R.id.tvEstadoActual);
        btnEscaner = view.findViewById(R.id.btnAbrirEscaner);
        Button btnIncidencia = view.findViewById(R.id.btnAbrirIncidencia);
        cardHistorial = view.findViewById(R.id.cardHistorial);
        llHistorialCambios = view.findViewById(R.id.llHistorialCambios);

        // Tradueix els textos estatics del detall de la ruta
        TextView tvHistorialTitle = view.findViewById(R.id.tvHistorialTitle);
        TextView tvGoogleMapsText = view.findViewById(R.id.tvGoogleMapsText);
        TextView tvAdrecaTitle = view.findViewById(R.id.tvAdrecaTitle);

        if (tvHistorialTitle != null) tvHistorialTitle.setText(com.example.logitex_app.utils.TranslationHelper.historialTitle(getContext()));
        if (tvGoogleMapsText != null) tvGoogleMapsText.setText(com.example.logitex_app.utils.TranslationHelper.seeOnGoogleMaps(getContext()));
        if (tvAdrecaTitle != null) tvAdrecaTitle.setText(com.example.logitex_app.utils.TranslationHelper.deliveryAddressTitle(getContext()));
        if (btnIncidencia != null) btnIncidencia.setText(com.example.logitex_app.utils.TranslationHelper.reportIncidentButton(getContext()));

        if (getArguments() != null) {
            idOrdenActual = getArguments().getInt("id_orden", 0);
            ordenNom = getArguments().getString("orden_nom", "ORD-000"); // Títol principal
            albara = getArguments().getString("albara_id", "ALB-000");     // Secundari
            String direccio = getArguments().getString("albara_dir", "Desconeguda");
            estadoActual = getArguments().getString("albara_estat", "PENDENT");

            // Titol principal amb el nom de l'ordre
            tvAlbara.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Ordre: ", "Order: ") + ordenNom);

            // Mostrem l'albara a la direccio o un camp extra
            tvDireccion.setText(com.example.logitex_app.utils.TranslationHelper.albaraLabel(getContext()) + albara + "\n" + com.example.logitex_app.utils.TranslationHelper.direccioLabel(getContext()) + direccio);

            // Configurar el mapa
            TextView tvMapDireccion = view.findViewById(R.id.tvMapDireccion);
            if (tvMapDireccion != null) {
                tvMapDireccion.setText(direccio);
            }

            com.google.android.material.card.MaterialCardView cardMap = view.findViewById(R.id.cardMap);
            if (cardMap != null) {
                cardMap.setOnClickListener(v -> abrirGoogleMaps(direccio));
            }

            actualizarInterfaz();
        }

        // Acció del boto principal per obrir l'escaner i validar el codi QR
        btnEscaner.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            if (estadoActual.equalsIgnoreCase("PREPARACIO_FINALITZADA") || estadoActual.equalsIgnoreCase("PENDENT_CONFIRMAR")) {
                options.setPrompt(com.example.logitex_app.utils.TranslationHelper.promptScanTransit(getContext()));
            } else if (estadoActual.equalsIgnoreCase("EN_TRANSIT") || estadoActual.equalsIgnoreCase("EN RUTA")) {
                options.setPrompt(com.example.logitex_app.utils.TranslationHelper.promptScanDeliver(getContext()));
            } else {
                options.setPrompt(com.example.logitex_app.utils.TranslationHelper.promptScanDefault(getContext()));
            }
            options.setBeepEnabled(true);
            options.setBarcodeImageEnabled(false);
            options.setOrientationLocked(false);
            options.setCameraId(0);
            barcodeLauncher.launch(options);
        });

        // Boto d'incidencies amb preseleccio automatica de l'ordre
        btnIncidencia.setOnClickListener(v -> {
            IncidenciaFragment incFrag = new IncidenciaFragment();
            Bundle args = new Bundle();
            args.putInt("preselected_order_id", idOrdenActual);
            incFrag.setArguments(args);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, incFrag)
                    .addToBackStack(null).commit();
        });

        return view;
    }

    // Actualitza la interficie grafica segons l'estat actual de l'ordre
    private void actualizarInterfaz() {
        tvEstado.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Estat: ", "Status: ") + estadoActual);

        View btnIncidencia = null;
        if (getView() != null) {
            btnIncidencia = getView().findViewById(R.id.btnAbrirIncidencia);
        }

        if (estadoActual.equalsIgnoreCase("ENTREGAT")) {
            // Si la comanda s'ha lliurat mostrem l'estat en verd i amaguem el boto d'escaner
            tvEstado.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Estat: Entregat", "Status: Delivered"));
            tvEstado.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Green

            btnEscaner.setVisibility(View.GONE);
            if (btnIncidencia != null) btnIncidencia.setVisibility(View.VISIBLE); // El boto de reportar incidencia sempre es visible a l'historial
            if (cardHistorial != null) cardHistorial.setVisibility(View.VISIBLE); // El llistat d'historial de canvis es visible
            cargarHistorialDeCambios();

        } else if (estadoActual.equalsIgnoreCase("PREPARACIO_FINALITZADA") || estadoActual.equalsIgnoreCase("PENDENT_CONFIRMAR")) {
            // Si esta pendent de confirmar activem el boto per iniciar la ruta
            tvEstado.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Estat: Pendent de confirmar", "Status: Pending confirmation"));
            tvEstado.setTextColor(android.graphics.Color.parseColor("#0056b3")); // Blue

            btnEscaner.setVisibility(View.VISIBLE);
            btnEscaner.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "INICIAR RUTA", "START ROUTE"));
            btnEscaner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0056b3")));

            if (btnIncidencia != null) btnIncidencia.setVisibility(View.GONE);
            if (cardHistorial != null) cardHistorial.setVisibility(View.GONE);

        } else if (estadoActual.equalsIgnoreCase("EN_TRANSIT") || estadoActual.equalsIgnoreCase("EN RUTA")) {
            // Si esta en transit activem el boto per confirmar el lliurament i permetem reportar incidencies
            tvEstado.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Estat: En trànsit", "Status: In transit"));
            tvEstado.setTextColor(android.graphics.Color.parseColor("#FF9800")); // Orange

            btnEscaner.setVisibility(View.VISIBLE);
            btnEscaner.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "CONFIRMAR LLIURAMENT", "CONFIRM DELIVERY"));
            btnEscaner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50"))); // Green

            if (btnIncidencia != null) btnIncidencia.setVisibility(View.VISIBLE);
            if (cardHistorial != null) cardHistorial.setVisibility(View.GONE);
        } else {
            // Estat per defecte de l'ordre
            tvEstado.setTextColor(android.graphics.Color.parseColor("#0056b3"));
            btnEscaner.setVisibility(View.VISIBLE);
            btnEscaner.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "INICIAR RUTA", "START ROUTE"));
            btnEscaner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0056b3")));

            if (btnIncidencia != null) btnIncidencia.setVisibility(View.GONE);
            if (cardHistorial != null) cardHistorial.setVisibility(View.GONE);
        }
    }

    private void procesarAccion() {
        String proximoEstado = (estadoActual.equalsIgnoreCase("PREPARACIO_FINALITZADA") || estadoActual.equalsIgnoreCase("PENDENT_CONFIRMAR"))
                ? "EN_TRANSIT" : "ENTREGAT";
        enviarCambioEstado(proximoEstado);
    }

    private void cargarHistorialDeCambios() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");


        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getOrdreById("Bearer " + token, String.valueOf(idOrdenActual)).enqueue(new Callback<Ordre>() {
            @Override
            public void onResponse(Call<Ordre> call, Response<Ordre> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Ordre o = response.body();
                    List<com.example.logitex_app.models.Tracking> list = o.getHistorial();

                    if (llHistorialCambios != null) {
                        llHistorialCambios.removeAllViews();
                        if (list == null || list.isEmpty()) {
                            TextView tvEmpty = new TextView(getContext());
                            tvEmpty.setText(com.example.logitex_app.utils.TranslationHelper.noChangesRegistered(getContext()));
                            tvEmpty.setTextColor(android.graphics.Color.GRAY);
                            tvEmpty.setPadding(0, 10, 0, 10);
                            llHistorialCambios.addView(tvEmpty);
                        } else {
                            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                            for (com.example.logitex_app.models.Tracking t : list) {
                                View item = layoutInflater.inflate(R.layout.item_historial_cambio, llHistorialCambios, false);
                                TextView tvEtapa = item.findViewById(R.id.tvEtapa);
                                TextView tvAutor = item.findViewById(R.id.tvAutor);
                                TextView tvData = item.findViewById(R.id.tvData);
                                TextView tvNotes = item.findViewById(R.id.tvNotes);
                                View timelineDot = item.findViewById(R.id.timelineDot);

                                String rawEtapa = t.getEtapa();
                                String etapaLegible;
                                if (rawEtapa == null) {
                                    etapaLegible = com.example.logitex_app.utils.TranslationHelper.get(getContext(), "CANVI", "CHANGE");
                                } else if (rawEtapa.equalsIgnoreCase("ENTREGAT")) {
                                    etapaLegible = com.example.logitex_app.utils.TranslationHelper.get(getContext(), "ENTREGAT", "DELIVERED");
                                } else if (rawEtapa.equalsIgnoreCase("EN_TRANSIT") || rawEtapa.equalsIgnoreCase("EN RUTA")) {
                                    etapaLegible = com.example.logitex_app.utils.TranslationHelper.get(getContext(), "EN TRÀNSIT", "IN TRANSIT");
                                } else if (rawEtapa.equalsIgnoreCase("PREPARACIO_FINALITZADA")) {
                                    etapaLegible = com.example.logitex_app.utils.TranslationHelper.get(getContext(), "PREPARACIÓ FINALITZADA", "PREPARATION FINISHED");
                                } else if (rawEtapa.equalsIgnoreCase("PREPARACIO_EN_CURS")) {
                                    etapaLegible = com.example.logitex_app.utils.TranslationHelper.get(getContext(), "PREPARACIÓ EN CURS", "PREPARATION IN PROGRESS");
                                } else if (rawEtapa.equalsIgnoreCase("PENDENT_PREPARACIO")) {
                                    etapaLegible = com.example.logitex_app.utils.TranslationHelper.get(getContext(), "PENDENT PREPARACIÓ", "PENDING PREPARATION");
                                } else {
                                    etapaLegible = rawEtapa.replace("_", " ").toUpperCase();
                                }
                                tvEtapa.setText(etapaLegible);

                                // Assigna un color dinamic al punt i al titol de la linia temporal segons l'estat
                                if (t.getEtapa() != null && t.getEtapa().equalsIgnoreCase("ENTREGAT")) {
                                    tvEtapa.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Verd
                                    if (timelineDot != null) timelineDot.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
                                } else if (t.getEtapa() != null && (t.getEtapa().equalsIgnoreCase("EN_TRANSIT") || t.getEtapa().equalsIgnoreCase("EN RUTA"))) {
                                    tvEtapa.setTextColor(android.graphics.Color.parseColor("#FF9800")); // Taronga
                                    if (timelineDot != null) timelineDot.setBackgroundColor(android.graphics.Color.parseColor("#FF9800"));
                                } else {
                                    tvEtapa.setTextColor(android.graphics.Color.parseColor("#0056b3")); // Blau
                                    if (timelineDot != null) timelineDot.setBackgroundColor(android.graphics.Color.parseColor("#0056b3"));
                                }

                                tvAutor.setText(com.example.logitex_app.utils.TranslationHelper.modifiedBy(getContext()) + (t.getUsuari() != null ? t.getUsuari() : com.example.logitex_app.utils.TranslationHelper.systemUser(getContext())));

                                if (t.getTimestamp() != null) {
                                    String time = t.getTimestamp().replace("T", " ");
                                    if (time.contains(".")) {
                                        time = time.substring(0, time.indexOf("."));
                                    }
                                    tvData.setText(com.example.logitex_app.utils.TranslationHelper.dateLabel(getContext()) + time);
                                    tvData.setVisibility(View.VISIBLE);
                                } else {
                                    tvData.setVisibility(View.GONE);
                                }

                                if (t.getNotes() != null && !t.getNotes().isEmpty()) {
                                    tvNotes.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Notes: ", "Notes: ") + t.getNotes());
                                    tvNotes.setVisibility(View.VISIBLE);
                                } else {
                                    tvNotes.setVisibility(View.GONE);
                                }

                                llHistorialCambios.addView(item);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Ordre> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Error al carregar l'historial de canvis", "Error loading change history"), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void enviarCambioEstado(String nuevoEstado) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        int userId = prefs.getInt("USER_ID", 0);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<JsonObject> call = apiService.cambiarEstadoOrden("Bearer " + token, idOrdenActual, nuevoEstado, userId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    String stTexto = nuevoEstado.equalsIgnoreCase("EN_TRANSIT") ?
                        com.example.logitex_app.utils.TranslationHelper.get(getContext(), "En trànsit", "In transit") :
                        com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Entregat", "Delivered");
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Ordre actualitzada a: ", "Order updated to: ") + stTexto, Toast.LENGTH_SHORT).show();
                    estadoActual = nuevoEstado;
                    actualizarInterfaz();
                } else {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Error del servidor: ", "Server error: ") + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.connectionError(getContext()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirGoogleMaps(String direccion) {
        if (direccion == null || direccion.trim().isEmpty() || direccion.equals("Desconeguda")) {
            Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Adreça no disponible", "Address not available"), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(direccion));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } catch (Exception e) {
            try {
                Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(direccion));
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                startActivity(webIntent);
            } catch (Exception ex) {
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "No s'ha pogut obrir Google Maps", "Could not open Google Maps"), Toast.LENGTH_SHORT).show();
            }
        }
    }
}