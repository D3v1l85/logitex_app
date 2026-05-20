package com.example.logitex_app.ui.transportista;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.logitex_app.R;
import com.example.logitex_app.api.ApiService;
import com.example.logitex_app.api.RetrofitClient;
import com.example.logitex_app.models.Ordre;
import com.google.gson.JsonObject;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RutasFragment extends Fragment {

    private String idOrdenParaEscanerRapido = "";
    private List<Ordre> listaOrdenes = new ArrayList<>();
    private RutasAdapter adapter;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    actualizarEstadoRapido(idOrdenParaEscanerRapido);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rutas, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rvRutasList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView tvHeader = view.findViewById(R.id.tvRutasHeader);
        if (tvHeader != null) {
            tvHeader.setText(com.example.logitex_app.utils.TranslationHelper.routesHeader(getContext()));
        }

        adapter = new RutasAdapter(listaOrdenes);
        recyclerView.setAdapter(adapter);

        obtenerOrdenesDelServidor();
        return view;
    }

    private void obtenerOrdenesDelServidor() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");

        // Recuperem les dades de l'usuari real
        String nombreLogueado = prefs.getString("USER_NOM", "Usuari");
        int rolLogueado = prefs.getInt("USER_ROL", 4);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Ordre>> call = apiService.getOrdres("Bearer " + token, rolLogueado, nombreLogueado);

        call.enqueue(new Callback<List<Ordre>>() {
            @Override
            public void onResponse(Call<List<Ordre>> call, Response<List<Ordre>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaOrdenes.clear();

                    // Filtrem la llista per mostrar únicament les ordres que estan llestes per
                    // repartir o ja en trànsit
                    for (Ordre o : response.body()) {
                        String estat = o.getEstat();
                        if (estat != null && (estat.equalsIgnoreCase("PREPARACIO_FINALITZADA")
                                || estat.equalsIgnoreCase("PENDENT_CONFIRMAR")
                                || estat.equalsIgnoreCase("EN_TRANSIT")
                                || estat.equalsIgnoreCase("EN RUTA"))) {
                            listaOrdenes.add(o);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (listaOrdenes.isEmpty()) {
                        Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "No hi ha rutes actives preparades o en curs", "No active routes prepared or in progress"), Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Ordre>> call, Throwable t) {
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.connectionError(getContext()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarEstadoRapido(String idOrden) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        int userId = prefs.getInt("USER_ID", 0);
        int idLimpio = Integer.parseInt(idOrden.replaceAll("[^0-9]", ""));

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<JsonObject> call = apiService.cambiarEstadoOrden("Bearer " + token, idLimpio, "EN_TRANSIT", userId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Ruta en marxa!", "Route started!"), Toast.LENGTH_SHORT).show();
                    obtenerOrdenesDelServidor();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.serverError(getContext()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class RutasAdapter extends RecyclerView.Adapter<RutasAdapter.ViewHolder> {
        private List<Ordre> dades;

        public RutasAdapter(List<Ordre> dades) {
            this.dades = dades;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ruta, parent, false);
            return new ViewHolder(v);
        }

        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Ordre ordre = dades.get(position);

            // Mostrem l'identificador de la ruta en comptes de l'albara
            holder.tvRutaId.setText(ordre.getIdentificador());
            holder.tvDestino.setText(com.example.logitex_app.utils.TranslationHelper.destiLabel(holder.itemView.getContext()) + ordre.getDireccio());

            holder.btnScan.setOnClickListener(v -> {
                idOrdenParaEscanerRapido = String.valueOf(ordre.getId());
                ScanOptions options = new ScanOptions();
                // El prompt de l'escaner pot continuar indicant la referencia d'albara
                options.setPrompt(com.example.logitex_app.utils.TranslationHelper.get(holder.itemView.getContext(), "Escaneja per l'albarà: ", "Scan for delivery note: ") + ordre.getReferencia());
                options.setOrientationLocked(false);
                barcodeLauncher.launch(options);
            });

            holder.itemView.setOnClickListener(v -> {
                DetallRutaFragment detalleFrag = new DetallRutaFragment();
                Bundle args = new Bundle();
                args.putInt("id_orden", ordre.getId());
                args.putString("orden_nom", ordre.getIdentificador());
                args.putString("albara_id", ordre.getReferencia());
                args.putString("albara_dir", ordre.getDireccio());
                args.putString("albara_estat", ordre.getEstat());
                detalleFrag.setArguments(args);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detalleFrag)
                        .addToBackStack(null)
                        .commit();
            });
        }

        @Override
        public int getItemCount() {
            return dades.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRutaId, tvDestino;
            ImageButton btnScan;

            ViewHolder(View itemView) {
                super(itemView);
                tvRutaId = itemView.findViewById(R.id.tvRutaId);
                tvDestino = itemView.findViewById(R.id.tvDestino);
                btnScan = itemView.findViewById(R.id.btnScanRuta);
            }
        }
    }
}
