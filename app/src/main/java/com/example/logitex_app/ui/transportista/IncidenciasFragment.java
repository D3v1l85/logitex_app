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

    private Spinner spinnerRutas, spinnerTipo;
    private EditText etDesc;
    // Guardaremos solo las órdenes activas (no entregadas)
    private List<Ordre> misOrdenesActivas = new ArrayList<>();

    // Opciones del frontend
    private final String[] tiposUI = {
            "Selecciona un tipus...",
            "Retard entrega",
            "Error adreça",
            "Mercaderia danyada",
            "Problema qualitat",
            "Altres"
    };

    public IncidenciasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incidencias, container, false);

        spinnerRutas = view.findViewById(R.id.spinnerRutas);
        spinnerTipo = view.findViewById(R.id.spinnerTipoIncidencia);
        etDesc = view.findViewById(R.id.etDescripcion);
        Button btnEnviar = view.findViewById(R.id.btnEnviarIncidencia);

        // 1. Configurar el Spinner de Tipos (con adaptador personalizado para color negro)
        ArrayAdapter<String> adapterTipos = crearAdaptadorNegro(tiposUI);
        spinnerTipo.setAdapter(adapterTipos);

        // 2. Cargar las rutas vivas
        cargarRutasReales();

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
                    nombresSpinner.add("Selecciona una ordre..."); // Opción vacía por defecto

                    for (Ordre o : response.body()) {
                        // FILTRO: Solo añadimos si NO está entregado
                        if (!"ENTREGAT".equalsIgnoreCase(o.getEstat())) {
                            misOrdenesActivas.add(o);
                            nombresSpinner.add(o.getIdentificador() + " (" + o.getDireccio() + ")");
                        }
                    }

                    if (getContext() != null) {
                        ArrayAdapter<String> adapterRutas = crearAdaptadorNegro(nombresSpinner.toArray(new String[0]));
                        spinnerRutas.setAdapter(adapterRutas);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Ordre>> call, Throwable t) {}
        });
    }

    private void enviarIncidenciaReal() {
        int indexRuta = spinnerRutas.getSelectedItemPosition();
        int indexTipo = spinnerTipo.getSelectedItemPosition();

        // Validamos que no hayan elegido la opción "Selecciona..."
        if (indexRuta == 0) {
            Toast.makeText(getContext(), "Si us plau, selecciona l'ordre afectada", Toast.LENGTH_SHORT).show();
            return;
        }
        if (indexTipo == 0) {
            Toast.makeText(getContext(), "Si us plau, selecciona el tipus d'incidència", Toast.LENGTH_SHORT).show();
            return;
        }

        String desc = etDesc.getText().toString().trim();
        if (desc.isEmpty()) {
            Toast.makeText(getContext(), "La descripció és obligatòria", Toast.LENGTH_SHORT).show();
            return;
        }

        // Restamos 1 al index porque el 0 es "Selecciona..."
        int idOrdre = misOrdenesActivas.get(indexRuta - 1).getId();
        String tipoSeleccionado = tiposUI[indexTipo];

        // Mapeamos el texto de la interfaz al ENUM exacto de la base de datos
        String tipoEnumDB = mapearTipoBackend(tipoSeleccionado);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_AUTH", "");
        String nomUsuari = prefs.getString("USER_NOM", "Desconegut");
        int rolUsuari = prefs.getInt("USER_ROL", 0);

        JsonObject json = new JsonObject();
        json.addProperty("idOrdre", idOrdre);
        json.addProperty("tipus", tipoEnumDB); // Enum de la BD: retard, error_adreca, dany, etc.
        json.addProperty("titol", tipoSeleccionado);
        json.addProperty("descripcio", desc);
        // Enviamos constancia explícita de quién lo crea
        json.addProperty("creador_nom", nomUsuari);
        json.addProperty("creador_rol", rolUsuari);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.crearIncidencia("Bearer " + token, json).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Incidència enviada correctament", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack(); // Volver atrás
                } else {
                    Toast.makeText(getContext(), "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "Error de connexió", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Traduce lo que ve el usuario a lo que la Base de Datos acepta
    private String mapearTipoBackend(String tipoUI) {
        switch (tipoUI) {
            case "Retard entrega": return "retard";
            case "Error adreça": return "error_adreca";
            case "Mercaderia danyada": return "dany";
            case "Problema qualitat": return "qualitat";
            default: return "altre";
        }
    }

    // Creador de adaptadores personalizado para forzar la letra en negro
    private ArrayAdapter<String> crearAdaptadorNegro(String[] datos) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, datos) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK); // Forzamos el texto del selector a NEGRO
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }
}