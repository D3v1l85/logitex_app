package com.example.logitex_app.ui.transportista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.logitex_app.R;

public class IncidenciasFragment extends Fragment {

    public IncidenciasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incidencias, container, false);

        Spinner spinnerRutas = view.findViewById(R.id.spinnerRutas);
        Spinner spinnerTipo = view.findViewById(R.id.spinnerTipoIncidencia);
        EditText etDesc = view.findViewById(R.id.etDescripcion);
        Button btnEnviar = view.findViewById(R.id.btnEnviarIncidencia);

        // 1. Configurar desplegable de Rutas (Mock data por ahora)
        String[] rutasAsignadas = {
                "Selecciona una opció...",
                "ALB-501 (C/ Aragó 123)",
                "ALB-502 (Polígon Sud)",
                "Avaria General (Sense albarà)"
        };
        ArrayAdapter<String> adapterRutas = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, rutasAsignadas);
        spinnerRutas.setAdapter(adapterRutas);

        // 2. Configurar desplegable de Tipos predeterminados
        String[] tiposIncidencia = {
                "Client absent",
                "Mercaderia danyada",
                "Retard per trànsit",
                "Vehicle avariat",
                "Altres"
        };
        ArrayAdapter<String> adapterTipos = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, tiposIncidencia);
        spinnerTipo.setAdapter(adapterTipos);

        // 3. Lógica del botón enviar
        btnEnviar.setOnClickListener(v -> {
            String rutaSel = spinnerRutas.getSelectedItem().toString();
            String tipoSel = spinnerTipo.getSelectedItem().toString();
            String desc = etDesc.getText().toString().trim();

            if (rutaSel.equals("Selecciona una opció...")) {
                Toast.makeText(getContext(), "Has de seleccionar un albarà afectat", Toast.LENGTH_SHORT).show();
                return;
            }

            if (desc.isEmpty()) {
                Toast.makeText(getContext(), "La descripció és obligatòria", Toast.LENGTH_SHORT).show();
                return;
            }

            // Aquí en el futuro usaremos Retrofit para enviar estos datos al Backend
            String mensaje = "Enviant a l'API:\nRuta: " + rutaSel + "\nTipus: " + tipoSel;
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();

            // Limpiar formulario
            spinnerRutas.setSelection(0);
            spinnerTipo.setSelection(0);
            etDesc.setText("");
        });

        return view;
    }
}