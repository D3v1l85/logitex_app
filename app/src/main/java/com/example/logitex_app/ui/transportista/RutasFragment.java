package com.example.logitex_app.ui.transportista;

import android.annotation.SuppressLint;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    private final List<Ordre> listaOrdenes = new ArrayList<>();
    private RutasAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    actualizarEstadoRapido(idOrdenParaEscanerRapido);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rutas, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvRutasList);
        swipeRefresh = view.findViewById(R.id.swipeRefreshRutas);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RutasAdapter(listaOrdenes);
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::obtenerOrdenesDelServidor);

        swipeRefresh.setRefreshing(true);
        obtenerOrdenesDelServidor();

        return view;
    }

    private void obtenerOrdenesDelServidor() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        String nombreLogueado = prefs.getString("USER_NOM", "Usuari");
        int rolLogueado = prefs.getInt("USER_ROL", 4);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Ordre>> call = apiService.getOrdres("Bearer " + token, rolLogueado, nombreLogueado);

        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Ordre>> call, @NonNull Response<List<Ordre>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    listaOrdenes.clear();
                    listaOrdenes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Ordre>> call, @NonNull Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), getString(R.string.err_connexio), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarEstadoRapido(String idOrden) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        int idLimpio = Integer.parseInt(idOrden.replaceAll("[^0-9]", ""));

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<JsonObject> call = apiService.cambiarEstadoOrden("Bearer " + token, idLimpio, "EN_TRANSIT");

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), getString(R.string.toast_ruta_iniciada), Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(true);
                    obtenerOrdenesDelServidor();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.err_connexio), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class RutasAdapter extends RecyclerView.Adapter<RutasAdapter.ViewHolder> {
        private final List<Ordre> dades;
        public RutasAdapter(List<Ordre> dades) { this.dades = dades; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ruta, parent, false);
            return new ViewHolder(v);
        }

        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Ordre ordre = dades.get(position);

            holder.tvRutaId.setText(ordre.getIdentificador());

            if (ordre.getEstat().equalsIgnoreCase("ENTREGAT")) {
                holder.btnScan.setVisibility(View.GONE);
                holder.tvDestino.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                holder.tvDestino.setText(getString(R.string.format_estat_finalitzada, ordre.getDireccio()));
            } else {
                holder.btnScan.setVisibility(View.VISIBLE);
                holder.tvDestino.setTextColor(android.graphics.Color.parseColor("#444444"));
                holder.tvDestino.setText(getString(R.string.format_desti, ordre.getDireccio()));
            }

            holder.btnScan.setOnClickListener(v -> {
                idOrdenParaEscanerRapido = String.valueOf(ordre.getId());
                ScanOptions options = new ScanOptions();
                options.setPrompt(getString(R.string.prompt_escaneja_albara, ordre.getReferencia()));
                options.setOrientationLocked(false);
                barcodeLauncher.launch(options);
            });

            holder.itemView.setOnClickListener(v -> {
                DetalleRutaFragment detalleFrag = new DetalleRutaFragment();
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
        public int getItemCount() { return dades.size(); }

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
