package com.example.logitex_app.ui.mozo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.example.logitex_app.api.ApiService;
import com.example.logitex_app.api.RetrofitClient;
import com.example.logitex_app.models.Incidencia;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Fragment que s'encarrega de mostrar la llista d'incidències assignades al mozo
public class IncidenciaListFragment extends Fragment {

    private RecyclerView recyclerView;
    private IncidenciaListAdapter adapter;
    private List<Incidencia> listaIncidencias = new ArrayList<>();

    public IncidenciaListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla el disseny xml per al fragment de la llista
        View view = inflater.inflate(R.layout.fragment_incidencia_list, container, false);

        // Configura el recyclerview per renderitzar les targetes
        recyclerView = view.findViewById(R.id.rvIncidenciasList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView tvHeader = view.findViewById(R.id.tvIncidenciasHeader);
        if (tvHeader != null) {
            tvHeader.setText(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Incidències Assignades", "Assigned Incidents"));
        }

        // Assigna l'adaptador amb la llista buida inicial
        adapter = new IncidenciaListAdapter(listaIncidencias);
        recyclerView.setAdapter(adapter);

        // Descarrega les incidències des del servidor
        obtenerIncidenciasDeLaApi();

        return view;
    }

    // Consulta les incidències de l'usuari actual connectant amb el servidor
    private void obtenerIncidenciasDeLaApi() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        int userId = prefs.getInt("USER_ID", 0);

        if (token.isEmpty()) {
            Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.sessionExpired(getContext()), Toast.LENGTH_SHORT).show();
            return;
        }

        // Crea la petició a la api demanant les incidències del rol de mozo
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Incidencia>> call = apiService.getIncidencias("Bearer " + token, "MOZO", userId);

        call.enqueue(new Callback<List<Incidencia>>() {
            @Override
            public void onResponse(Call<List<Incidencia>> call, Response<List<Incidencia>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Neteja les dades anteriors i afegeix les noves incidències rebudes
                    listaIncidencias.clear();
                    listaIncidencias.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Error al descarregar les incidències", "Error downloading incidents"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Incidencia>> call, Throwable t) {
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.connectionError(getContext()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Envia el nou estat seleccionat al servidor per actualitzar la incidència
    private void cambiarEstadoIncidencia(int idIncidencia, String nuevoEstado) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        String nomUsuari = prefs.getString("USER_NOM", "Usuari");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Incidencia> call = apiService.cambiarEstadoIncidencia("Bearer " + token, idIncidencia, nuevoEstado, nomUsuari);

        call.enqueue(new Callback<Incidencia>() {
            @Override
            public void onResponse(Call<Incidencia> call, Response<Incidencia> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Estat actualitzat correctament", "Status updated successfully"), Toast.LENGTH_SHORT).show();
                    // Torna a carregar les incidències per actualitzar la vista
                    obtenerIncidenciasDeLaApi();
                } else {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Error al modificar l'estat", "Error modifying status"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Incidencia> call, Throwable t) {
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Error de connexió al modificar l'estat", "Connection error when modifying status"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Retorna el text amigable de l'estat per mostrar a l'usuari
    private String obtenerTextoEstat(String estatDb) {
        if (estatDb == null) return com.example.logitex_app.utils.TranslationHelper.get(getContext(), "obert", "open");
        
        switch (estatDb.toLowerCase().trim()) {
            case "obert":
                return com.example.logitex_app.utils.TranslationHelper.get(getContext(), "obert", "open");
            case "en_proces":
            case "en proces":
                return com.example.logitex_app.utils.TranslationHelper.get(getContext(), "en procés", "in progress");
            case "resolt":
                return com.example.logitex_app.utils.TranslationHelper.get(getContext(), "resolt", "resolved");
            case "tancat":
                return com.example.logitex_app.utils.TranslationHelper.get(getContext(), "tancat", "closed");
            default:
                return estatDb;
        }
    }

    // Retorna el color associat a cada estat per distingir-los visualment
    private int obtenerColorEstat(String estatDb) {
        if (estatDb == null) return Color.parseColor("#d32f2f");
        
        switch (estatDb.toLowerCase().trim()) {
            case "obert":
                return Color.parseColor("#d32f2f");
            case "en_proces":
            case "en proces":
                return Color.parseColor("#FFA500");
            case "resolt":
                return Color.parseColor("#4CAF50");
            case "tancat":
                return Color.GRAY;
            default:
                return Color.BLACK;
        }
    }

    // Retorna el color de text per a cada prioritat
    private int obtenerColorPrioritat(String prioritat) {
        if (prioritat == null) return Color.parseColor("#0056b3");
        
        switch (prioritat.toLowerCase().trim()) {
            case "alta":
                return Color.parseColor("#d32f2f");
            case "baixa":
                return Color.parseColor("#4CAF50");
            case "mitjana":
            default:
                return Color.parseColor("#0056b3");
        }
    }

    // Adaptador encarregat d'enllaçar el llistat amb els elements de la interfície gràfica
    class IncidenciaListAdapter extends RecyclerView.Adapter<IncidenciaListAdapter.ViewHolder> {
        private List<Incidencia> incidencias;

        public IncidenciaListAdapter(List<Incidencia> incidencias) {
            this.incidencias = incidencias;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incidencia, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Incidencia inc = incidencias.get(position);

            // Carrega els textos principals
            holder.tvTitulo.setText(inc.getTitol());
            holder.tvDescripcio.setText(inc.getDescripcio());
            holder.tvAutor.setText(com.example.logitex_app.utils.TranslationHelper.get(holder.itemView.getContext(), "Per: ", "By: ") + (inc.getReportatPerNom() != null ? inc.getReportatPerNom() : com.example.logitex_app.utils.TranslationHelper.get(holder.itemView.getContext(), "Desconegut", "Unknown")));

            // Aplica el format gràfic de l'estat actual
            String estatDb = inc.getEstat();
            holder.tvEstado.setText(obtenerTextoEstat(estatDb));
            holder.tvEstado.setTextColor(obtenerColorEstat(estatDb));

            // Aplica el format gràfic de la prioritat
            String prioritat = inc.getPrioritat();
            String displayPrioritat = "Mitjana";
            if (prioritat != null) {
                if (prioritat.equalsIgnoreCase("alta") || prioritat.equalsIgnoreCase("high")) {
                    displayPrioritat = com.example.logitex_app.utils.TranslationHelper.get(holder.itemView.getContext(), "Alta", "High");
                } else if (prioritat.equalsIgnoreCase("baixa") || prioritat.equalsIgnoreCase("low")) {
                    displayPrioritat = com.example.logitex_app.utils.TranslationHelper.get(holder.itemView.getContext(), "Baixa", "Low");
                } else {
                    displayPrioritat = com.example.logitex_app.utils.TranslationHelper.get(holder.itemView.getContext(), "Mitjana", "Medium");
                }
            } else {
                displayPrioritat = com.example.logitex_app.utils.TranslationHelper.get(holder.itemView.getContext(), "Mitjana", "Medium");
            }
            holder.tvPrioritat.setText(com.example.logitex_app.utils.TranslationHelper.get(holder.itemView.getContext(), "Prioritat: ", "Priority: ") + displayPrioritat);
            holder.tvPrioritat.setTextColor(obtenerColorPrioritat(prioritat));

            // Escolta els clics a cada element per obrir el selector d'estat
            holder.itemView.setOnClickListener(v -> {
                boolean isEn = com.example.logitex_app.utils.LocaleHelper.getLanguage(getContext()).equalsIgnoreCase("en");
                CharSequence[] displayOptions = isEn ?
                    new CharSequence[]{"open", "in progress", "resolved", "closed", "Cancel"} :
                    new CharSequence[]{"obert", "en procés", "resolt", "tancat", "Cancel·lar"};
                String[] apiOptions = {"obert", "en_proces", "resolt", "tancat"};
                
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Modificar estat de la incidència", "Modify incident status"));
                builder.setItems(displayOptions, (dialog, which) -> {
                    if (which < 4) {
                        String nouEstat = apiOptions[which];
                        cambiarEstadoIncidencia(inc.getId(), nouEstat);
                    }
                });
                builder.show();
            });
        }

        @Override
        public int getItemCount() {
            return incidencias.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitulo, tvDescripcio, tvEstado, tvPrioritat, tvAutor;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitulo = itemView.findViewById(R.id.tvIncidenciaTitulo);
                tvDescripcio = itemView.findViewById(R.id.tvIncidenciaDescripcio);
                tvEstado = itemView.findViewById(R.id.tvIncidenciaEstado);
                tvPrioritat = itemView.findViewById(R.id.tvIncidenciaPrioritat);
                tvAutor = itemView.findViewById(R.id.tvIncidenciaAutor);
            }
        }
    }
}
