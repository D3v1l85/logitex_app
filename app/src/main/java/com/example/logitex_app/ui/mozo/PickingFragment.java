package com.example.logitex_app.ui.mozo;

import android.os.Bundle;
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

import java.util.Arrays;
import java.util.List;

public class PickingFragment extends Fragment {

    public PickingFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picking, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvPickingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Dades de prova (Mock Data) fins que connectem amb l'API
        List<String> tasques = Arrays.asList(
                "Palet #1024 - Passadís 2, Est. A",
                "Palet #1025 - Passadís 4, Est. C",
                "Palet #1026 - Passadís 1, Est. B",
                "Palet #1027 - Passadís 5, Est. A"
        );

        // Configurem l'adaptador ràpid
        MockPickingAdapter adapter = new MockPickingAdapter(tasques);
        recyclerView.setAdapter(adapter);

        return view;
    }

    // --- ADAPTADOR RÀPID (Només per veure disseny) ---
    private class MockPickingAdapter extends RecyclerView.Adapter<MockPickingAdapter.ViewHolder> {
        private List<String> dades;

        public MockPickingAdapter(List<String> dades) {
            this.dades = dades;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String[] parts = dades.get(position).split(" - ");
            holder.tvPaletId.setText(parts[0]);
            holder.tvUbicacion.setText(parts[1]);

            // Acció en clicar un palet
            holder.itemView.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Has seleccionat: " + parts[0], Toast.LENGTH_SHORT).show()
            );
        }

        @Override
        public int getItemCount() {
            return dades.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPaletId, tvUbicacion;
            ViewHolder(View itemView) {
                super(itemView);
                tvPaletId = itemView.findViewById(R.id.tvPaletId);
                tvUbicacion = itemView.findViewById(R.id.tvUbicacion);
            }
        }
    }
}