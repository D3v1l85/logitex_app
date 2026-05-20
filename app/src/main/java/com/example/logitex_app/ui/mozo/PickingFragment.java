package com.example.logitex_app.ui.mozo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Fragment que mostra la llista d'ordres de picking pendents per al mozo de magatzem
public class PickingFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrdreAdapter adapter;
    private List<Ordre> listaOrdenes = new ArrayList<>();

    public PickingFragment() {
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picking, container, false);

        // Configura la llista visual d'elements
        recyclerView = view.findViewById(R.id.rvPickingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView tvHeader = view.findViewById(R.id.tvPickingHeader);
        if (tvHeader != null) {
            tvHeader.setText(com.example.logitex_app.utils.TranslationHelper.pickingHeader(getContext()));
        }

        adapter = new OrdreAdapter(listaOrdenes);
        recyclerView.setAdapter(adapter);

        obtenerOrdenesDeLaApi();

        return view;
    }

    // Obté les ordres assignades des del servidor utilitzant la sessio de l'usuari
    private void obtenerOrdenesDeLaApi() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        String nombreLogueado = prefs.getString("USER_NOM", "Usuari");
        int rolLogueado = prefs.getInt("USER_ROL", 3); // 3 es Mozo

        // Si no hi ha cap token d'autenticacio desat cancel·lem l'operacio
        if (token.isEmpty()) {
            Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.sessionExpired(getContext()), Toast.LENGTH_SHORT).show();
            return;
        }

        // Fem la crida asincrona per obtenir la llista d'ordres del mozo
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Ordre>> call = apiService.getOrdres("Bearer " + token, rolLogueado, nombreLogueado);

        call.enqueue(new Callback<List<Ordre>>() {
            @Override
            public void onResponse(Call<List<Ordre>> call, Response<List<Ordre>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaOrdenes.clear();
                    for (Ordre o : response.body()) {
                        String estat = o.getEstat();
                        // Filtrem les ordres mostrant nomes les que estan pendents o en curs de preparacio
                        if (estat != null && (estat.equalsIgnoreCase("PENDENT_PREPARACIO")
                                || estat.equalsIgnoreCase("PREPARACIO_EN_CURS"))) {
                            listaOrdenes.add(o);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Error al descarregar les ordres", "Error downloading orders"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Ordre>> call, Throwable t) {
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.connectionError(getContext()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Canvia l'estat d'una ordre enviant l'identificador de l'usuari responsable al servidor
    private void cambiarEstadoOrden(int idOrden, String nouEstat) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        int userId = prefs.getInt("USER_ID", 0);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<JsonObject> call = apiService.cambiarEstadoOrden("Bearer " + token, idOrden, nouEstat, userId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Estat actualitzat correctament", "Status updated successfully"), Toast.LENGTH_SHORT).show();
                    obtenerOrdenesDeLaApi(); // Recargar la lista
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

    // Adaptador per gestionar i mostrar les dades de cada ordre a la llista
    class OrdreAdapter extends RecyclerView.Adapter<OrdreAdapter.ViewHolder> {
        private List<Ordre> ordres;

        public OrdreAdapter(List<Ordre> ordres) {
            this.ordres = ordres;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Ordre ordre = ordres.get(position);

            // Mostra els textos corresponents a l'ordre i el seu estat actual
            holder.tvPaletId.setText(com.example.logitex_app.utils.TranslationHelper.get(holder.itemView.getContext(), "Ordre: ", "Order: ") + ordre.getIdentificador());
            holder.tvUbicacion
                    .setText(com.example.logitex_app.utils.TranslationHelper.destiLabel(holder.itemView.getContext()) + (ordre.getClient() != null ? ordre.getClient() : ordre.getDireccio()));

            String displayEstat = ordre.getEstat();
            if ("PENDENT_PREPARACIO".equalsIgnoreCase(displayEstat))
                displayEstat = com.example.logitex_app.utils.TranslationHelper.get(holder.itemView.getContext(), "Pendent", "Pending");
            else if ("PREPARACIO_EN_CURS".equalsIgnoreCase(displayEstat))
                displayEstat = com.example.logitex_app.utils.TranslationHelper.get(holder.itemView.getContext(), "En curs", "In progress");

            holder.tvEstado.setText(displayEstat);

            // Obre el detall de l'ordre de picking en premer sobre un element de la llista
            holder.itemView.setOnClickListener(v -> {
                DetallPickingFragment detalleFrag = new DetallPickingFragment();
                Bundle args = new Bundle();
                args.putInt("id_orden", ordre.getId());
                args.putString("orden_nom", ordre.getIdentificador());
                args.putString("albara_id", ordre.getReferencia());
                args.putString("albara_dir", ordre.getDireccio());
                args.putString("albara_estat", ordre.getEstat());
                args.putString("tenda_destinataria", ordre.getClient());
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
            return ordres.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPaletId, tvUbicacion, tvEstado;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPaletId = itemView.findViewById(R.id.tvPaletId);
                tvUbicacion = itemView.findViewById(R.id.tvUbicacion);
                tvEstado = itemView.findViewById(R.id.tvEstado);
            }
        }
    }
}