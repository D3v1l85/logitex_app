package com.example.logitex_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.logitex_app.R;
import com.example.logitex_app.data.Ordre;
import java.util.List;

public class OrdreAdapter extends RecyclerView.Adapter<OrdreAdapter.OrdreViewHolder> {

    private List<Ordre> llistaOrdres;

    public OrdreAdapter(List<Ordre> llistaOrdres) {
        this.llistaOrdres = llistaOrdres;
    }

    @NonNull
    @Override
    public OrdreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ordre, parent, false);
        return new OrdreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdreViewHolder holder, int position) {
        Ordre ordre = llistaOrdres.get(position);
        holder.tvIdOrdre.setText(ordre.getIdentificador());
        holder.tvEstat.setText(ordre.getEstat());
        holder.tvAdreca.setText(ordre.getAdreca());
    }

    @Override
    public int getItemCount() {
        return llistaOrdres.size();
    }

    public static class OrdreViewHolder extends RecyclerView.ViewHolder {
        TextView tvIdOrdre, tvEstat, tvAdreca;

        public OrdreViewHolder(@NonNull View itemView) {
            super(itemView);
            // Vigila aquí: assegura't que aquests IDs existeixen al teu item_ordre.xml
            tvIdOrdre = itemView.findViewById(R.id.tvIdOrdre);
            tvEstat = itemView.findViewById(R.id.tvEstat);
            tvAdreca = itemView.findViewById(R.id.tvAdreca);
        }
    }
}
