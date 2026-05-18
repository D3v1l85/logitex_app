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

public class IncidenciasFragment extends Fragment {

    private Spinner spinnerRutas, spinnerTipo, spinnerMozos;
    private EditText etDesc;
    private final List<Ordre> misOrdenesActivas = new ArrayList<>();
    private final List<String> listaMozosDB = new ArrayList<>();

    public IncidenciasFragment() {}

    private String[] getTiposUI() {
        return new String[]{
                getString(R.string.opcio_selecciona_tipus),
                getString(R.string.tipus_retard),
                getString(R.string.tipus_error_adreca),
                getString(R.string.tipus_dany),
                getString(R.string.tipus_qualitat),
                getString(R.string.tipus_altres)
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incidencias, container, false);

        spinnerRutas = view.findViewById(R.id.spinnerRutas);
        spinnerTipo = view.findViewById(R.id.spinnerTipoIncidencia);
        spinnerMozos = view.findViewById(R.id.spinnerMozos);
        etDesc = view.findViewById(R.id.etDescripcion);
        Button btnEnviar = view.findViewById(R.id.btnEnviarIncidencia);

        spinnerTipo.setAdapter(crearAdaptadorNegro(getTiposUI()));

        cargarRutasReales();
        cargarMozosDisponibles(); // Carga la lista de Mozos

        btnEnviar.setOnClickListener(v -> enviarIncidenciaReal());

        return view;
    }

    private void cargarMozosDisponibles() {
        listaMozosDB.clear();
        listaMozosDB.add(getString(R.string.opcio_selecciona_mozo));

        // Simulación de Mozos activos del sistema (Se puede sustituir por una llamada API corta)
        listaMozosDB.add("Joan Martínez (ID: 301)");
        listaMozosDB.add("Albert Torres (ID: 304)");
        listaMozosDB.add("Carlos López (ID: 309)");

        spinnerMozos.setAdapter(crearAdaptadorNegro(listaMozosDB.toArray(new String[0])));
    }

    private void cargarRutasReales() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        String nombre = prefs.getString("USER_NOM", "");
        int rol = prefs.getInt("USER_ROL", 4);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getOrdres("Bearer " + token, rol, nombre).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Ordre>> call, @NonNull Response<List<Ordre>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    misOrdenesActivas.clear();
                    List<String> nombresSpinner = new ArrayList<>();
                    nombresSpinner.add(getString(R.string.opcio_selecciona_ordre));

                    for (Ordre o : response.body()) {
                        if (!"ENTREGAT".equalsIgnoreCase(o.getEstat())) {
                            misOrdenesActivas.add(o);
                            nombresSpinner.add(o.getIdentificador() + " (" + o.getDireccio() + ")");
                        }
                    }

                    if (getContext() != null) {
                        spinnerRutas.setAdapter(crearAdaptadorNegro(nombresSpinner.toArray(new String[0])));
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Ordre>> call, @NonNull Throwable t) {}
        });
    }

    private void enviarIncidenciaReal() {
        int indexRuta = spinnerRutas.getSelectedItemPosition();
        int indexTipo = spinnerTipo.getSelectedItemPosition();
        int indexMozo = spinnerMozos.getSelectedItemPosition();

        if (indexRuta == 0) {
            Toast.makeText(getContext(), getString(R.string.err_selecciona_ordre), Toast.LENGTH_SHORT).show();
            return;
        }
        if (indexMozo == 0) {
            Toast.makeText(getContext(), getString(R.string.err_selecciona_mozo), Toast.LENGTH_SHORT).show();
            return;
        }
        if (indexTipo == 0) {
            Toast.makeText(getContext(), getString(R.string.err_selecciona_tipus), Toast.LENGTH_SHORT).show();
            return;
        }

        String desc = etDesc.getText().toString().trim();
        if (desc.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.err_descripcio_buida), Toast.LENGTH_SHORT).show();
            return;
        }

        int idOrdre = misOrdenesActivas.get(indexRuta - 1).getId();
        String mozoAsignado = listaMozosDB.get(indexMozo);
        String tipoSeleccionado = getTiposUI()[indexTipo];
        String tipoEnumDB = mapearTipoBackend(tipoSeleccionado);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        String nomUsuari = prefs.getString("USER_NOM", "Desconegut");
        int rolUsuari = prefs.getInt("USER_ROL", 0);

        JsonObject json = new JsonObject();
        json.addProperty("idOrdre", idOrdre);
        json.addProperty("tipus", tipoEnumDB);
        json.addProperty("titol", tipoSeleccionado);
        json.addProperty("descripcio", desc);
        json.addProperty("mozo_responsable", mozoAsignado); // Enviamos el mozo vinculado
        json.addProperty("creador_nom", nomUsuari);
        json.addProperty("creador_rol", rolUsuari);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.crearIncidencia("Bearer " + token, json).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), getString(R.string.msg_incidencia_ok), Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), getString(R.string.err_servidor, response.code()), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.err_connexio), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String mapearTipoBackend(String tipoUI) {
        if (tipoUI.equals(getString(R.string.tipus_retard))) return "retard";
        if (tipoUI.equals(getString(R.string.tipus_error_adreca))) return "error_adreca";
        if (tipoUI.equals(getString(R.string.tipus_dany))) return "dany";
        if (tipoUI.equals(getString(R.string.tipus_qualitat))) return "qualitat";
        return "altre";
    }

    private ArrayAdapter<String> crearAdaptadorNegro(String[] datos) {
        return new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, datos) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK);
                return view;
            }
        };
    }
}