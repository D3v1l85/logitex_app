package com.example.logitex_app.ui.transportista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.logitex_app.R;

import java.util.Arrays;
import java.util.List;

public class RutasFragment extends Fragment {

    public RutasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Aquí usamos "container", no "parent"
        View view = inflater.inflate(R.layout.fragment_rutas, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvRutasList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Datos Mock para las rutas
        List<String> rutes = Arrays.asList(
                "Albarà: #ALB-501 - C/ Aragó 123 (Barcelona)",
                "Albarà: #ALB-502 - Polígon Sud, Nau 4 (Tarragona)",
                "Albarà: #ALB-503 - Av. Diagonal 450 (Barcelona)",
                "Albarà: #ALB-504 - C/ Major 12 (Girona)"
        );

        recyclerView.setAdapter(new RutasAdapter(rutes));
        return view;
    }

    // --- CLASE ADAPTADOR (Aquí es donde sí existe "parent") ---
    private class RutasAdapter extends RecyclerView.Adapter<RutasAdapter.ViewHolder> {
        private List<String> dades;

        public RutasAdapter(List<String> dades) { this.dades = dades; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Fíjate que aquí usamos R.layout.item_ruta (esto quita tu error de la línea 54)
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ruta, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String[] parts = dades.get(position).split(" - ");
            holder.tvRutaId.setText(parts[0]);
            holder.tvDestino.setText(parts[1]);

            holder.btnScan.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Obrint càmera per confirmar: " + parts[0], Toast.LENGTH_SHORT).show()
            );

            holder.itemView.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Detalls de l'entrega: " + parts[1], Toast.LENGTH_SHORT).show()
            );
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
