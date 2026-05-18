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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

    private PaletAdapter adapter;
    private final List<Palet> listaPalets = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;

    public PickingFragment() { }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picking, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvPickingList);
        swipeRefresh = view.findViewById(R.id.swipeRefreshPicking);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PaletAdapter(listaPalets);
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::obtenerPaletsDeLaApi);

        swipeRefresh.setRefreshing(true);
        obtenerPaletsDeLaApi();

        return view;
    }

    private void obtenerPaletsDeLaApi() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");

        if (token.isEmpty()) {
            swipeRefresh.setRefreshing(false);
            Toast.makeText(getContext(), getString(R.string.err_sessio_caducada), Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Palet>> call = apiService.getPales("Bearer " + token);

        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Palet>> call, @NonNull Response<List<Palet>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    listaPalets.clear();
                    listaPalets.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), getString(R.string.err_descarregar_palets), Toast.LENGTH_SHORT).show();
                    Log.e("API_PALES", "Error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Palet>> call, @NonNull Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), getString(R.string.err_connexio), Toast.LENGTH_SHORT).show();
                Log.e("API_PALES", "Fallo de red: " + t.getMessage());
            }
        });
    }

    class PaletAdapter extends RecyclerView.Adapter<PaletAdapter.ViewHolder> {
        private final List<Palet> palets;

        public PaletAdapter(List<Palet> palets) {
            this.palets = palets;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Palet palet = palets.get(position);

            holder.tvPaletId.setText(getString(R.string.format_lot, palet.getLot()));
            holder.tvUbicacion.setText(getString(R.string.format_estat, palet.getEstat()));

            holder.itemView.setOnClickListener(v -> {
                DetallePickingFragment detalleFrag = new DetallePickingFragment();
                Bundle args = new Bundle();
                args.putString("palet_id", getString(R.string.format_lot_id, palet.getLot(), String.valueOf(palet.getId())));
                args.putString("palet_ub", getString(R.string.format_estat_actual, palet.getEstat()));
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