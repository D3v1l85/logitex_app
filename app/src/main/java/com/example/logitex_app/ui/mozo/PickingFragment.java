package com.example.logitex_app.ui.mozo;

import android.annotation.SuppressLint;
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
import com.example.logitex_app.models.Palet;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickingFragment extends Fragment {

    private RecyclerView recyclerView;
    private PaletAdapter adapter;
    private List<Palet> listaPalets = new ArrayList<>();

    public PickingFragment() { }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picking, container, false);

        recyclerView = view.findViewById(R.id.rvPickingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Conectamos el adaptador con nuestra lista (que al principio estará vacía)
        adapter = new PaletAdapter(listaPalets);
        recyclerView.setAdapter(adapter);

        // Llamamos a la función que descarga los datos de Internet
        obtenerPaletsDeLaApi();

        return view;
    }

    private void obtenerPaletsDeLaApi() {
        // 1. Recuperamos el Token de la memoria del móvil
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");

        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Error: Sessió caducada", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Preparamos la llamada
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // ⚠️ ATENCIÓN: Por estándar de seguridad, casi todos los servidores requieren
        // que la palabra "Bearer " vaya delante del token.
        // Si al probar os da error 401, probad a quitar la palabra "Bearer " y dejar solo el token.
        Call<List<Palet>> call = apiService.getPales("Bearer " + token);

        // 3. Ejecutamos la llamada en segundo plano
        call.enqueue(new Callback<List<Palet>>() {
            @Override
            public void onResponse(Call<List<Palet>> call, Response<List<Palet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ¡Éxito! Vaciamos la lista vieja y metemos los palets reales
                    listaPalets.clear();
                    listaPalets.addAll(response.body());
                    adapter.notifyDataSetChanged(); // Avisamos a la pantalla de que hay datos nuevos
                } else {
                    Toast.makeText(getContext(), "Error al descarregar els palets", Toast.LENGTH_SHORT).show();
                    Log.e("API_PALES", "Error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Palet>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de connexió", Toast.LENGTH_SHORT).show();
                Log.e("API_PALES", "Fallo de red: " + t.getMessage());
            }
        });
    }

    // =======================================================================
    // NUEVO ADAPTADOR (Preparado para trabajar con objetos Palet)
    // =======================================================================
    class PaletAdapter extends RecyclerView.Adapter<PaletAdapter.ViewHolder> {
        private List<Palet> palets;

        public PaletAdapter(List<Palet> palets) {
            this.palets = palets;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Reutilizamos el diseño de tarjeta que hiciste el otro día
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Palet palet = palets.get(position);

            // Rellenamos la tarjeta con los datos reales
            holder.tvPaletId.setText("Lot: " + palet.getLot());
            holder.tvUbicacion.setText("Estat: " + palet.getEstat()); // Mostramos el estado temporalmente

            // Al hacer clic, vamos al detalle pasando los datos
            holder.itemView.setOnClickListener(v -> {
                DetallePickingFragment detalleFrag = new DetallePickingFragment();
                Bundle args = new Bundle();
                args.putString("palet_id", "Lot: " + palet.getLot() + " (ID: " + palet.getId() + ")");
                args.putString("palet_ub", "Estat actual: " + palet.getEstat());
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
            return palets.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPaletId, tvUbicacion;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPaletId = itemView.findViewById(R.id.tvPaletId);
                tvUbicacion = itemView.findViewById(R.id.tvUbicacion);
            }
        }
    }
}