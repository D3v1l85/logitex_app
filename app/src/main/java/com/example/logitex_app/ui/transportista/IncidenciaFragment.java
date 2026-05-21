package com.example.logitex_app.ui.transportista;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class IncidenciaFragment extends Fragment {

    private Spinner spinnerRutas, spinnerTipo, spinnerGrupMozos, spinnerPrioritat;
    private EditText etDesc;
    // Guardarem nomes les ordres actives que no estiguin entregades
    private List<Ordre> misOrdenesActivas = new ArrayList<>();
    // Guardarem els grups de mozos carregats
    private List<com.example.logitex_app.models.GrupMozos> misGruposMozos = new ArrayList<>();

    private String[] getTiposUI() {
        boolean isEn = com.example.logitex_app.utils.LocaleHelper.getLanguage(getContext()).equalsIgnoreCase("en");
        return isEn ? new String[]{
                "Select type...",
                "Delivery delay",
                "Address error",
                "Damaged goods",
                "Quality issue",
                "Other"
        } : new String[]{
                "Selecciona un tipus...",
                "Retard entrega",
                "Error adreça",
                "Mercaderia danyada",
                "Problema qualitat",
                "Altres"
        };
    }

    private String[] getPrioritatsUI() {
        boolean isEn = com.example.logitex_app.utils.LocaleHelper.getLanguage(getContext()).equalsIgnoreCase("en");
        return isEn ? new String[]{
                "Select priority...",
                "High",
                "Medium",
                "Low"
        } : new String[]{
                "Selecciona la prioritat...",
                "Alta",
                "Mitjana",
                "Baixa"
        };
    }

    public IncidenciaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incidencia, container, false);

        spinnerRutas = view.findViewById(R.id.spinnerRutas);
        spinnerTipo = view.findViewById(R.id.spinnerTipoIncidencia);
        spinnerGrupMozos = view.findViewById(R.id.spinnerGrupMozos);
        spinnerPrioritat = view.findViewById(R.id.spinnerPrioritat);
        etDesc = view.findViewById(R.id.etDescripcion);
        Button btnEnviar = view.findViewById(R.id.btnEnviarIncidencia);

        // Tradueix els textos estatics de la pantalla d'incidencies
        TextView tvIncidentsTitle = view.findViewById(R.id.tvIncidentsTitle);
        TextView tvSelectOrderLabel = view.findViewById(R.id.tvSelectOrderLabel);
        TextView tvIncidentTypeLabel = view.findViewById(R.id.tvIncidentTypeLabel);
        TextView tvPriorityLabel = view.findViewById(R.id.tvPriorityLabel);
        TextView tvMozoGroupLabel = view.findViewById(R.id.tvMozoGroupLabel);
        TextView tvDescriptionLabel = view.findViewById(R.id.tvDescriptionLabel);

        if (tvIncidentsTitle != null) tvIncidentsTitle.setText(com.example.logitex_app.utils.TranslationHelper.incidentsTitle(getContext()));
        if (tvSelectOrderLabel != null) tvSelectOrderLabel.setText(com.example.logitex_app.utils.TranslationHelper.selectOrderLabel(getContext()));
        if (tvIncidentTypeLabel != null) tvIncidentTypeLabel.setText(com.example.logitex_app.utils.TranslationHelper.incidentTypeLabel(getContext()));
        if (tvPriorityLabel != null) tvPriorityLabel.setText(com.example.logitex_app.utils.TranslationHelper.priorityLabel(getContext()));
        if (tvMozoGroupLabel != null) tvMozoGroupLabel.setText(com.example.logitex_app.utils.TranslationHelper.mozoGroupLabel(getContext()));
        if (tvDescriptionLabel != null) tvDescriptionLabel.setText(com.example.logitex_app.utils.TranslationHelper.descriptionLabel(getContext()));
        if (etDesc != null) etDesc.setHint(com.example.logitex_app.utils.TranslationHelper.descriptionHint(getContext()));
        if (btnEnviar != null) btnEnviar.setText(com.example.logitex_app.utils.TranslationHelper.sendReportButton(getContext()));

        // Configura el selector de tipus d'incidencia amb un adaptador personalitzat en color negre
        ArrayAdapter<String> adapterTipos = crearAdaptadorNegro(getTiposUI());
        spinnerTipo.setAdapter(adapterTipos);

        // Configura el selector per escollir la prioritat
        ArrayAdapter<String> adapterPrioritats = crearAdaptadorNegro(getPrioritatsUI());
        spinnerPrioritat.setAdapter(adapterPrioritats);

        // Carrega les rutes actives que estan pendents de lliurar
        cargarRutasReales();

        // Carrega els grups de mozos disponibles
        cargarGruposDeMozos();

        btnEnviar.setOnClickListener(v -> enviarIncidenciaReal());

        return view;
    }

    private void cargarRutasReales() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        String nombre = prefs.getString("USER_NOM", "");
        int rol = prefs.getInt("USER_ROL", 4);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getOrdres("Bearer " + token, rol, nombre).enqueue(new Callback<List<Ordre>>() {
            @Override
            public void onResponse(Call<List<Ordre>> call, Response<List<Ordre>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    misOrdenesActivas.clear();
                    List<String> nombresSpinner = new ArrayList<>();
                    nombresSpinner.add(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Selecciona una ordre...", "Select an order...")); // Opcio buida per defecte

                    for (Ordre o : response.body()) {
                        // Filtre per afegir l'ordre nomes si no esta entregada
                        if (!"ENTREGAT".equalsIgnoreCase(o.getEstat())) {
                            misOrdenesActivas.add(o);
                            nombresSpinner.add(o.getIdentificador() + " (" + o.getDireccio() + ")");
                        }
                    }

                    // Si hi ha una ordre preseleccionada i no es a la llista la afegim al final per poder seleccionar la
                    if (getArguments() != null && getArguments().containsKey("preselected_order_id")) {
                        int preselectedId = getArguments().getInt("preselected_order_id");
                        boolean encontrada = false;
                        for (Ordre o : misOrdenesActivas) {
                            if (o.getId() == preselectedId) {
                                encontrada = true;
                                break;
                            }
                        }
                        if (!encontrada) {
                            for (Ordre o : response.body()) {
                                if (o.getId() == preselectedId) {
                                    misOrdenesActivas.add(o);
                                    nombresSpinner.add(o.getIdentificador() + " (" + o.getDireccio() + ") " + com.example.logitex_app.utils.TranslationHelper.get(getContext(), "[Lliurada]", "[Delivered]"));
                                    break;
                                }
                            }
                        }
                    }

                    if (getContext() != null) {
                        ArrayAdapter<String> adapterRutas = crearAdaptadorNegro(nombresSpinner.toArray(new String[0]));
                        spinnerRutas.setAdapter(adapterRutas);

                        // Preseleccio de l'ordre des del detall de la ruta
                        if (getArguments() != null && getArguments().containsKey("preselected_order_id")) {
                            int preselectedId = getArguments().getInt("preselected_order_id");
                            for (int i = 0; i < misOrdenesActivas.size(); i++) {
                                if (misOrdenesActivas.get(i).getId() == preselectedId) {
                                    spinnerRutas.setSelection(i + 1); // Afegim un al index perque la primera posicio es la d'instruccio
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Ordre>> call, Throwable t) {}
        });
    }

    private void cargarGruposDeMozos() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getGrupMozos("Bearer " + token).enqueue(new Callback<List<com.example.logitex_app.models.GrupMozos>>() {
            @Override
            public void onResponse(Call<List<com.example.logitex_app.models.GrupMozos>> call, Response<List<com.example.logitex_app.models.GrupMozos>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    misGruposMozos.clear();
                    misGruposMozos.addAll(response.body());

                    List<String> nombresGrupos = new ArrayList<>();
                    nombresGrupos.add(com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Selecciona el grup responsable...", "Select the responsible group..."));
                    for (com.example.logitex_app.models.GrupMozos g : misGruposMozos) {
                        nombresGrupos.add(g.getNom() + " (" + g.getDescripcio() + ")");
                    }

                    if (getContext() != null) {
                        ArrayAdapter<String> adapterGrupos = crearAdaptadorNegro(nombresGrupos.toArray(new String[0]));
                        spinnerGrupMozos.setAdapter(adapterGrupos);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<com.example.logitex_app.models.GrupMozos>> call, Throwable t) {
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Error al carregar els grups de mozos", "Error loading mozo groups"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarIncidenciaReal() {
        int indexRuta = spinnerRutas.getSelectedItemPosition();
        int indexTipo = spinnerTipo.getSelectedItemPosition();
        int indexGrupo = spinnerGrupMozos.getSelectedItemPosition();
        int indexPrioridad = spinnerPrioritat.getSelectedItemPosition();

        // Validem que l'usuari hagi triat una opcio valida diferent de la de per defecte
        if (indexRuta == 0) {
            Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Si us plau, selecciona l'ordre afectada", "Please select the affected order"), Toast.LENGTH_SHORT).show();
            return;
        }
        if (indexTipo == 0) {
            Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Si us plau, selecciona el tipus d'incidència", "Please select the incident type"), Toast.LENGTH_SHORT).show();
            return;
        }
        if (indexPrioridad == 0) {
            Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Si us plau, selecciona la prioritat", "Please select the priority"), Toast.LENGTH_SHORT).show();
            return;
        }
        if (indexGrupo == 0) {
            Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Si us plau, selecciona el grup de mozos responsable", "Please select the responsible mozo group"), Toast.LENGTH_SHORT).show();
            return;
        }

        String desc = etDesc.getText().toString().trim();
        if (desc.isEmpty()) {
            Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "La descripció és obligatòria", "Description is required"), Toast.LENGTH_SHORT).show();
            return;
        }

        // Restem un a l'index perque la primera posicio serveix de titol
        int idOrdre = misOrdenesActivas.get(indexRuta - 1).getId();
        String tipoSeleccionado = getTiposUI()[indexTipo];
        // Definim les prioritats com alta mitjana o baixa
        String prioridadSeleccionada = getPrioritatsUI()[indexPrioridad].toLowerCase();
        if (prioridadSeleccionada.equalsIgnoreCase("medium") || prioridadSeleccionada.equalsIgnoreCase("mitjana")) {
            prioridadSeleccionada = "mitjana";
        } else if (prioridadSeleccionada.equalsIgnoreCase("high") || prioridadSeleccionada.equalsIgnoreCase("alta")) {
            prioridadSeleccionada = "alta";
        } else if (prioridadSeleccionada.equalsIgnoreCase("low") || prioridadSeleccionada.equalsIgnoreCase("baixa")) {
            prioridadSeleccionada = "baixa";
        }
        int idGrupResponsable = misGruposMozos.get(indexGrupo - 1).getId();

        // Traduim el text visible a la interficie amb el valor enum equivalent per a la base de dades
        String tipoEnumDB = mapearTipoBackend(tipoSeleccionado);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        String nomUsuari = prefs.getString("USER_NOM", "Desconegut");
        int rolUsuari = prefs.getInt("USER_ROL", 0);
        int userId = prefs.getInt("USER_ID", 0);

        JsonObject json = new JsonObject();
        json.addProperty("idOrdre", idOrdre);
        json.addProperty("tipus", tipoEnumDB); // Enum de la BD: retard, error_adreca, dany, etc.
        json.addProperty("titol", tipoSeleccionado);
        json.addProperty("prioritat", prioridadSeleccionada); // Enum de la BD: alta, mitjana, baixa
        json.addProperty("descripcio", desc);
        // Enviem el grup assignat com a responsable
        json.addProperty("assignatA", idGrupResponsable);
        // Enviem l'identificador de l'usuari que ha reportat la incidencia
        if (userId > 0) {
            json.addProperty("reportatPer", userId);
        }
        // Enviem les dades explicites de l'usuari creador
        json.addProperty("creador_nom", nomUsuari);
        json.addProperty("creador_rol", rolUsuari);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.crearIncidencia("Bearer " + token, json).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.incidentSent(getContext()), Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack(); // Torna a la pantalla anterior
                } else {
                    Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.get(getContext(), "Error del servidor: ", "Server error: ") + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), com.example.logitex_app.utils.TranslationHelper.connectionError(getContext()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Tradueix la seleccio de la interficie al valor acceptat pel servidor
    private String mapearTipoBackend(String tipoUI) {
        if (tipoUI.equalsIgnoreCase("Retard entrega") || tipoUI.equalsIgnoreCase("Delivery delay")) return "retard";
        if (tipoUI.equalsIgnoreCase("Error adreça") || tipoUI.equalsIgnoreCase("Address error")) return "error_adreca";
        if (tipoUI.equalsIgnoreCase("Mercaderia danyada") || tipoUI.equalsIgnoreCase("Damaged goods")) return "dany";
        if (tipoUI.equalsIgnoreCase("Problema qualitat") || tipoUI.equalsIgnoreCase("Quality issue")) return "qualitat";
        return "altre";
    }

    // Creador d'adaptadors personalitzat per forçar el text en color negre
    private ArrayAdapter<String> crearAdaptadorNegro(String[] datos) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, datos) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK); // Forcem el text del selector a color negre
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }
}