package com.example.logitex_app.ui.transportista;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.logitex_app.R;
import com.example.logitex_app.api.ApiService;
import com.example.logitex_app.api.RetrofitClient;
import com.example.logitex_app.models.Ordre;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialFragment extends Fragment {

    private final List<Ordre> listaHistorial = new ArrayList<>();
    private HistorialAdapter adapter;
    private ProgressBar pbLoading;

    public HistorialFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        pbLoading = view.findViewById(R.id.pbHistorialLoading);
        RecyclerView recyclerView = view.findViewById(R.id.rvHistorialList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView tvHeader = view.findViewById(R.id.tvHistorialHeader);
        if (tvHeader != null) tvHeader.setText(com.example.logitex_app.utils.TranslationHelper.historyHeader(getContext()));

        adapter = new HistorialAdapter(listaHistorial);
        recyclerView.setAdapter(adapter);

        cargarHistorialDelServidor();

        return view;
    }

    private void cargarHistorialDelServidor() {
        pbLoading.setVisibility(View.VISIBLE);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        String nombreLogueado = prefs.getString("USER_NOM", "Usuari");
        int rolLogueado = prefs.getInt("USER_ROL", 4);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Ordre>> call = apiService.getOrdres("Bearer " + token, rolLogueado, nombreLogueado);

        call.enqueue(new Callback<List<Ordre>>() {
            @Override
            public void onResponse(@NonNull Call<List<Ordre>> call, @NonNull Response<List<Ordre>> response) {
                if (getContext() == null) return;
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    listaHistorial.clear();
                    // Filtrem per mostrar unicament les ordres lliurades o en transit
                    for (Ordre o : response.body()) {
                        String estat = o.getEstat();
                        if (estat != null && (estat.equalsIgnoreCase("ENTREGAT") || estat.equalsIgnoreCase("EN_TRANSIT") || estat.equalsIgnoreCase("EN RUTA"))) {
                            listaHistorial.add(o);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (listaHistorial.isEmpty()) {
                        Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.historyEmpty(getContext()), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Error del servidor: ", "Server error: ") + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Ordre>> call, @NonNull Throwable t) {
                if (getContext() == null) return;
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.serverError(getContext()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

        private final List<Ordre> dades;

        public HistorialAdapter(List<Ordre> dades) {
            this.dades = dades;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Ordre o = dades.get(position);

            holder.tvId.setText(o.getIdentificador());
            holder.tvDesti.setText(com.example.logitex_app.utils.TranslationHelper.destiLabel(holder.itemView.getContext()) + o.getDireccio());

            String estat = o.getEstat();
            if (estat == null) estat = "DESCONEGUT";

            if (estat.equalsIgnoreCase("ENTREGAT")) {
                holder.tvEstat.setText(com.example.logitex_app.utils.TranslationHelper.delivered(holder.itemView.getContext()));
                holder.tvEstat.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Verd
            } else if (estat.equalsIgnoreCase("EN_TRANSIT") || estat.equalsIgnoreCase("EN RUTA")) {
                holder.tvEstat.setText(com.example.logitex_app.utils.TranslationHelper.inTransit(holder.itemView.getContext()));
                holder.tvEstat.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800"))); // Taronga
            } else {
                holder.tvEstat.setText(estat.toUpperCase());
                holder.tvEstat.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3"))); // Blau
            }

            // En fer clic obrim el fragment amb el detall de la ruta
            holder.itemView.setOnClickListener(v -> {
                DetallRutaFragment detalleFrag = new DetallRutaFragment();
                Bundle args = new Bundle();
                args.putInt("id_orden", o.getId());
                args.putString("orden_nom", o.getIdentificador());
                args.putString("albara_id", o.getReferencia());
                args.putString("albara_dir", o.getDireccio());
                args.putString("albara_estat", o.getEstat());
                detalleFrag.setArguments(args);

                requireActivity().getSupportFragmentManager().beginTransaction()
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
            TextView tvId, tvDesti, tvEstat;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvId = itemView.findViewById(R.id.tvHistorialId);
                tvDesti = itemView.findViewById(R.id.tvHistorialDestino);
                tvEstat = itemView.findViewById(R.id.tvHistorialEstado);
            }
        }
    }
}
