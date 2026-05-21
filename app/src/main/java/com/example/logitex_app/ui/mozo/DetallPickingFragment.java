package com.example.logitex_app.ui.mozo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.logitex_app.R;
import com.example.logitex_app.api.ApiService;
import com.example.logitex_app.api.RetrofitClient;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Fragment que gestiona el detall d'una ordre de picking seleccionada per un mozo
public class DetallPickingFragment extends Fragment {

    private int idOrden;
    private String ordenNom;
    private String albaraId;
    private String albaraDir;
    private String estat;
    private String client;

    private TextView tvDetallePalet;
    private TextView tvDetalleUbicacion;
    private TextView tvDetalleEstado;
    private Button btnAccion;
    private Button btnIncidencia;

    public DetallPickingFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Infla la interfície gràfica de detall de picking
        View view = inflater.inflate(R.layout.fragment_detall_picking, container, false);

        // Enllaça els components gràfics de text i botons
        tvDetallePalet = view.findViewById(R.id.tvDetallePalet);
        tvDetalleUbicacion = view.findViewById(R.id.tvDetalleUbicacion);
        tvDetalleEstado = view.findViewById(R.id.tvDetalleEstado);
        btnAccion = view.findViewById(R.id.btnEscanearPalet);
        btnIncidencia = view.findViewById(R.id.btnAbrirIncidenciaPicking);

        // Si hi ha arguments de l'ordre els carrega per omplir la pantalla
        if (getArguments() != null) {
            idOrden = getArguments().getInt("id_orden", 0);
            ordenNom = getArguments().getString("orden_nom", "Desconeguda");
            albaraId = getArguments().getString("albara_id", "");
            albaraDir = getArguments().getString("albara_dir", "");
            estat = getArguments().getString("albara_estat", "");
            client = getArguments().getString("tenda_destinataria", "");

            actualizarUI();
        }

        // Acció de començar o finalitzar la preparació segons l'estat actual de l'ordre
        btnAccion.setOnClickListener(v -> {
            if ("PENDENT_PREPARACIO".equalsIgnoreCase(estat)) {
                actualizarEstado("PREPARACIO_EN_CURS");
            } else if ("PREPARACIO_EN_CURS".equalsIgnoreCase(estat)) {
                actualizarEstado("PREPARACIO_FINALITZADA");
            }
        });

        // Configura el botó per notificar incidències associant l'id d'aquesta ordre
        if (btnIncidencia != null) {
            btnIncidencia.setText(com.example.logitex_app.utils.TranslationHelper.reportIncidentButton(getContext()));
            btnIncidencia.setOnClickListener(v -> {
                com.example.logitex_app.ui.transportista.IncidenciaFragment incFrag = new com.example.logitex_app.ui.transportista.IncidenciaFragment();
                Bundle args = new Bundle();
                args.putInt("preselected_order_id", idOrden);
                incFrag.setArguments(args);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, incFrag)
                        .addToBackStack(null).commit();
            });
        }

        return view;
    }

    // Actualitza l'estat de la interfície i els colors del text segons l'estat de
    // l'ordre
    private void actualizarUI() {
        tvDetallePalet.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Ordre: ", "Order: ") + ordenNom);
        tvDetalleUbicacion.setText(client != null && !client.isEmpty() ? client : albaraDir);

        if ("PENDENT_PREPARACIO".equalsIgnoreCase(estat)) {
            tvDetalleEstado.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Estat: Pendent preparació", "Status: Pending preparation"));
            tvDetalleEstado.setTextColor(Color.parseColor("#FFA500"));
            btnAccion.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "COMENÇAR PREPARACIÓ", "START PREPARATION"));
            btnAccion.setEnabled(true);
            btnAccion.setVisibility(View.VISIBLE);
        } else if ("PREPARACIO_EN_CURS".equalsIgnoreCase(estat)) {
            tvDetalleEstado.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Estat: Preparació en curs", "Status: Preparation in progress"));
            tvDetalleEstado.setTextColor(Color.parseColor("#0056b3"));
            btnAccion.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "FINALITZAR PREPARACIÓ", "FINISH PREPARATION"));
            btnAccion.setEnabled(true);
            btnAccion.setVisibility(View.VISIBLE);
        } else if ("PREPARACIO_FINALITZADA".equalsIgnoreCase(estat)) {
            tvDetalleEstado.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Estat: Preparació finalitzada", "Status: Preparation finished"));
            tvDetalleEstado.setTextColor(Color.parseColor("#4CAF50"));
            btnAccion.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "PREPARACIÓ FINALITZADA", "PREPARATION FINISHED"));
            btnAccion.setEnabled(false);
            btnAccion.setVisibility(View.GONE);
        } else {
            tvDetalleEstado.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Estat: ", "Status: ") + estat);
            btnAccion.setVisibility(View.GONE);
        }
    }

    // Connecta amb el servidor mitjançant api per actualitzar l'estat de l'ordre
    private void actualizarEstado(String nuevoEstado) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        int userId = prefs.getInt("USER_ID", 0);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<JsonObject> call = apiService.cambiarEstadoOrden("Bearer " + token, idOrden, nuevoEstado, userId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Estat actualitzat correctament", "Status updated successfully"), Toast.LENGTH_SHORT).show();
                    estat = nuevoEstado;
                    actualizarUI();
                } else {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Error al actualitzar l'estat", "Error updating status"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.connectionError(getContext()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}